/*
 * Copyright 2012-2014 TORCH GmbH
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.graylog2.gelfclient.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.graylog2.gelfclient.Configuration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageEncoder;
import org.graylog2.gelfclient.encoder.GelfMessageTcpEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class GelfTcpTransport implements GelfTransport {
    private final Logger LOG = LoggerFactory.getLogger(GelfTcpTransport.class);
    private final Configuration config;
    private final GelfMessageEncoder encoder;
    private final BlockingQueue<GelfMessage> queue;

    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public GelfTcpTransport(Configuration config, GelfMessageEncoder encoder) {
        this.config = config;
        this.encoder = encoder;
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
                        ch.pipeline().addLast(new GelfMessageTcpEncoder(config, encoder));
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

    @Override
    public void send(GelfMessage message) {
        LOG.debug("Sending message: {}", message.toString());
        queue.offer(message);
    }

    @Override
    public void stop() {
        workerGroup.shutdownGracefully();
    }
}
