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
 * A {@link GelfTransport} implementation that uses UDP to send GELF messages.
 *
 * <p>This class is thread-safe.</p>
 *
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class GelfUdpTransport implements GelfTransport {
    private final Logger LOG = LoggerFactory.getLogger(GelfUdpTransport.class);
    private final GelfConfiguration config;
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final BlockingQueue<GelfMessage> queue;

    /**
     * Creates a new UDP GELF transport.
     *
     * @param config the client configuration
     */
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
