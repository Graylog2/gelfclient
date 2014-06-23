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

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfSenderThread;
import org.graylog2.gelfclient.encoder.GelfMessageJsonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A {@link GelfTransport} implementation that uses TCP to send GELF messages.
 *
 * <p>This class is thread-safe.</p>
 *
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class GelfTcpTransport implements GelfTransport {
    private final Logger LOG = LoggerFactory.getLogger(GelfTcpTransport.class);
    private final GelfConfiguration config;
    private final BlockingQueue<GelfMessage> queue;

    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    /**
     * Creates a new TCP GELF transport.
     *
     * @param config the client configuration
     */
    public GelfTcpTransport(GelfConfiguration config) {
        this.config = config;
        this.queue = new LinkedBlockingQueue<>(config.getQueueSize());

        createBootstrap(workerGroup);
    }

    private void createBootstrap(EventLoopGroup workerGroup) {
        final Bootstrap bootstrap = new Bootstrap();
        final GelfSenderThread senderThread = new GelfSenderThread(queue);

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout())
                .option(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
                .remoteAddress(config.getHost(), config.getPort())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // We cannot use GZIP encoding for TCP because the headers contain '\0'-bytes then.
                        // The graylog2-server uses '\0'-bytes as delimiter for TCP frames.
                        ch.pipeline().addLast(new GelfMessageJsonEncoder());
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                // We do not receive data.
                            }

                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                senderThread.start(ctx.channel());
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                LOG.info("Channel disconnected!");
                                senderThread.stop();
                                scheduleReconnect(ctx.channel().eventLoop());
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                LOG.error("Exception caught", cause);
                            }
                        });
                    }
                });

        if (config.getSendBufferSize() != -1) {
            bootstrap.option(ChannelOption.SO_SNDBUF, config.getSendBufferSize());
        }

        bootstrap.connect().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    LOG.debug("Connected!");
                } else {
                    LOG.error("Connection failed: {}", future.cause().getMessage());
                    scheduleReconnect(future.channel().eventLoop());
                }
            }
        });
    }

    private void scheduleReconnect(final EventLoopGroup workerGroup) {
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
     *
     * <p>This implementation is backed by a {@link java.util.concurrent.BlockingQueue}. When this method returns the
     * message has been added to the {@link java.util.concurrent.BlockingQueue} but has not been sent to the remote
     * host yet.</p>
     *
     * @param message message to send to the remote host
     * @throws InterruptedException
     */
    @Override
    public void send(GelfMessage message) throws InterruptedException {
        LOG.debug("Sending message: {}", message.toString());
        queue.put(message);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation is backed by a {@link java.util.concurrent.BlockingQueue}. When this method returns the
     * message has been added to the {@link java.util.concurrent.BlockingQueue} but has not been sent to the remote
     * host yet.</p>
     *
     * @param message message to send to the remote host
     * @return true if the message could be dispatched, false otherwise
     */
    @Override
    public boolean trySend(GelfMessage message) {
        LOG.debug("Trying to send message: {}", message.toString());
        return queue.offer(message);
    }

    @Override
    public void stop() {
        workerGroup.shutdownGracefully();
    }
}
