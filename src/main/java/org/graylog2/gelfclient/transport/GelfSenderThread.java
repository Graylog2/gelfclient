/*
 * Copyright 2014 TORCH GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.graylog2.gelfclient.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.util.Uninterruptibles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

/**
 * The main event thread used by the {@link org.graylog2.gelfclient.transport.GelfTransport}s.
 */
public class GelfSenderThread {
    private static final Logger LOG = LoggerFactory.getLogger(GelfSenderThread.class);

    private static final int FLUSH_WAIT_RETRIES = 250;
    private static final int FLUSH_WAIT_MS = 240;

    private final ReentrantLock lock;
    private final Condition connectedCond;
    private final AtomicBoolean keepRunning = new AtomicBoolean(true);
    private final Thread senderThread;
    private Channel channel;
    private final int maxInflightSends;
    private BlockingQueue<GelfMessage> queue;
    private AtomicInteger inflightSends;

    /**
     * Creates a new sender thread with the given {@link BlockingQueue} as source of messages.
     *
     * @param queue            the {@link BlockingQueue} used as source of {@link GelfMessage}s
     * @param maxInflightSends the maximum number of outstanding network writes/flushes before the sender spins
     */
    public GelfSenderThread(final BlockingQueue<GelfMessage> queue, int maxInflightSends) {
        this.maxInflightSends = maxInflightSends;
        this.lock = new ReentrantLock();
        this.connectedCond = lock.newCondition();
        this.inflightSends = new AtomicInteger(0);
        this.queue = queue;

        if (maxInflightSends <= 0) {
            throw new IllegalArgumentException("maxInflightSends must be larger than 0");
        }

        this.senderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                GelfMessage gelfMessage = null;
                final ChannelFutureListener inflightListener = new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        inflightSends.decrementAndGet();
                    }
                };

                while (keepRunning.get()) {
                    // wait until we are connected to the graylog2 server before polling log events from the queue
                    lock.lock();
                    try {
                        while (channel == null || !channel.isActive()) {
                            try {
                                connectedCond.await();
                            } catch (InterruptedException e) {
                                if (!keepRunning.get()) {
                                    // bail out if we are awoken because the application is stopping
                                    break;
                                }
                            }
                        }
                        // we are connected, let's start sending logs
                        try {
                            // if we have a lingering event already, try to send that instead of polling a new one.
                            if (gelfMessage == null) {
                                gelfMessage = queue.poll(100, TimeUnit.MILLISECONDS);
                            }
                            // if we are still connected, convert LoggingEvent to GELF and send it
                            // but if we aren't connected anymore, we'll have already pulled an event from the queue,
                            // which we keep hanging around in this thread and in the next loop iteration will block until we are connected again.
                            if (gelfMessage != null && channel != null && channel.isActive()) {
                                // Do not allow more than "maxInflightSends" concurrent writes in netty, to avoid having netty buffer
                                // excessively when faced with slower consumers
                                while (inflightSends.get() > GelfSenderThread.this.maxInflightSends) {
                                    Uninterruptibles.sleepUninterruptibly(1, MICROSECONDS);
                                }
                                inflightSends.incrementAndGet();

                                // Write the GELF message to the pipeline. The protocol specific channel handler
                                // will take care of encoding.
                                channel.writeAndFlush(gelfMessage).addListener(inflightListener);
                                gelfMessage = null;
                            }
                        } catch (InterruptedException e) {
                            // ignore, when stopping keepRunning will be set to false outside
                        }
                    } finally {
                        lock.unlock();
                    }
                }

                LOG.debug("GelfSenderThread exiting!");
            }
        });

        this.senderThread.setDaemon(true);
        this.senderThread.setName("GelfSenderThread-" + senderThread.getId());
    }

    public void start(Channel channel) {
        lock.lock();
        try {
            this.channel = channel;
            this.connectedCond.signalAll();
        } finally {
            lock.unlock();
        }
        senderThread.start();
    }

    public void stop() {
        keepRunning.set(false);
        senderThread.interrupt();
    }

    /**
     * Block and wait for all messages in the queue to send. This can be used during shutdown to wait for
     * queued messages to send before shutting down.
     * Waiting will continue for the indicated {@code FLUSH_WAIT_RETRIES} and {@code FLUSH_WAIT_MS.}
     */
    void flushSynchronously() {

        for (int i = 0; i <= FLUSH_WAIT_RETRIES; i++) {
            if (!flushingInProgress()) {
                LOG.debug("Successfully flushed messages. Shutting down now.");
                continue;
            }

            LOG.debug("Flushing in progress. [{}] messages are still enqueued, and [{}] messages are still in-flight.",
                      queue.size(), inflightSends.get());

            try {
                Thread.sleep(FLUSH_WAIT_MS);
            } catch (InterruptedException e) {
                LOG.error("Interrupted message flushing during shutdown after [{}}] attempts.", i);
            }

            if (i == FLUSH_WAIT_MS) {
                LOG.error("Failed to flush messages in [{}] attempts. Shutting down anyway.", FLUSH_WAIT_RETRIES);
            }
        }
    }

    /**
     * @return {@code true} if messages are queued or in-flight.
     */
    private boolean flushingInProgress() {

        return (inflightSends != null && inflightSends.get() != 0) || !queue.isEmpty();
    }
}
