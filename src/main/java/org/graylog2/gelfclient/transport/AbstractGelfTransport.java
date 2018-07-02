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

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An abstract {@link GelfTransport} implementation serving as parent for the concrete implementations.
 * <p>This class is thread-safe.</p>
 */
public abstract class AbstractGelfTransport implements GelfTransport {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractGelfTransport.class);

    protected final GelfConfiguration config;
    protected final BlockingQueue<GelfMessage> queue;

    private final EventLoopGroup workerGroup;

    /**
     * Creates a new GELF transport with the given configuration and {@link java.util.concurrent.BlockingQueue}.
     *
     * @param config the client configuration
     * @param queue  the {@link BlockingQueue} used to buffer GELF messages
     */
    public AbstractGelfTransport(final GelfConfiguration config, final BlockingQueue<GelfMessage> queue) {
        this.config = config;
        this.queue = queue;
        this.workerGroup = new NioEventLoopGroup(config.getThreads(), new DefaultThreadFactory(getClass(), true));
        createBootstrap(workerGroup);
    }

    /**
     * Creates a new GELF transport with the given configuration.
     *
     * @param config the client configuration
     */
    public AbstractGelfTransport(final GelfConfiguration config) {
        this(config, new LinkedBlockingQueue<GelfMessage>(config.getQueueSize()));

    }

    protected abstract void createBootstrap(final EventLoopGroup workerGroup);

    protected void scheduleReconnect(final EventLoopGroup workerGroup) {
        workerGroup.schedule(new Runnable() {
            @Override
            public void run() {
                LOG.debug("Starting reconnect!");
                createBootstrap(workerGroup);
            }
        }, config.getReconnectDelay(), TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is backed by a {@link java.util.concurrent.BlockingQueue}. When this method returns the
     * message has been added to the {@link java.util.concurrent.BlockingQueue} but has not been sent to the remote
     * host yet.</p>
     *
     * @param message message to send to the remote host
     */
    @Override
    public void send(final GelfMessage message) throws InterruptedException {
        LOG.debug("Sending message: {}", message);
        queue.put(message);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is backed by a {@link java.util.concurrent.BlockingQueue}. When this method returns the
     * message has been added to the {@link java.util.concurrent.BlockingQueue} but has not been sent to the remote
     * host yet.</p>
     *
     * @param message message to send to the remote host
     * @return true if the message could be dispatched, false otherwise
     */
    @Override
    public boolean trySend(final GelfMessage message) {
        LOG.debug("Trying to send message: {}", message);
        return queue.offer(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        workerGroup.shutdownGracefully().syncUninterruptibly();
    }
}
