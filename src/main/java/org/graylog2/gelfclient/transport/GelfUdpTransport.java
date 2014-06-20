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
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfSenderThread;
import org.graylog2.gelfclient.encoder.GelfCompressionEncoder;
import org.graylog2.gelfclient.encoder.GelfMessageChunkEncoder;
import org.graylog2.gelfclient.encoder.GelfMessageJsonEncoder;
import org.graylog2.gelfclient.encoder.GelfMessageUdpEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class GelfUdpTransport implements GelfTransport {
    private final Logger LOG = LoggerFactory.getLogger(GelfUdpTransport.class);
    private final GelfConfiguration config;
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final BlockingQueue<GelfMessage> queue;

    public GelfUdpTransport(GelfConfiguration config) {
        this.config = config;
        this.queue = new LinkedBlockingQueue<>(config.getQueueSize());

        createBootstrap(workerGroup);
    }

    public void createBootstrap(EventLoopGroup workerGroup) {
        final Bootstrap bootstrap = new Bootstrap();
        final GelfSenderThread senderThread = new GelfSenderThread(queue);
        final InetSocketAddress remoteAddress = new InetSocketAddress(config.getHost(), config.getPort());

        bootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new GelfMessageUdpEncoder(remoteAddress));
                        ch.pipeline().addLast(new GelfMessageChunkEncoder(config));
                        ch.pipeline().addLast(new GelfCompressionEncoder());
                        ch.pipeline().addLast(new GelfMessageJsonEncoder());
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<DatagramPacket>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                                // We do not receive data.
                            }

                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                senderThread.start(ctx.channel());
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                senderThread.stop();
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

        bootstrap.bind(0);
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
