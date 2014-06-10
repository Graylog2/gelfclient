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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.graylog2.gelfclient.Configuration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class GelfTcpTransport implements GelfTransport {
    private final Logger LOG = LoggerFactory.getLogger(GelfTcpTransport.class);
    private final Configuration config;
    private final GelfMessageEncoder encoder;

    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;
    private final GelfTcpChannelHandler handler;
    private final ChannelFuture channelFuture;

    public GelfTcpTransport(Configuration config, GelfMessageEncoder encoder) {
        this.config = config;
        this.encoder = encoder;

        this.workerGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        this.handler = new GelfTcpChannelHandler(config, encoder);

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(config.getHost(), config.getPort())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(handler);
                    }
                });

        this.channelFuture = bootstrap.connect();

        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    LOG.debug("Connected!");
                } else {
                    LOG.error("Connection failed", future.cause());
                    future.cause().printStackTrace();
                }
            }
        });
    }

    @Override
    public void send(GelfMessage message) {
        handler.send(message);
    }

    @Override
    public void stop() {
        handler.stop();
        workerGroup.shutdownGracefully();
    }

    @Override
    public void sync() throws InterruptedException {
        channelFuture.sync();
    }
}
