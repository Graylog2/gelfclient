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
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.graylog2.gelfclient.Configuration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class GelfTcpTransport implements GelfTransport {
    private final Logger LOG = LoggerFactory.getLogger(GelfTcpTransport.class);
    private final Configuration config;
    private final GelfMessageEncoder encoder;

    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private AtomicReference<GelfTcpChannelHandler> handler = new AtomicReference<>(null);

    public GelfTcpTransport(Configuration config, GelfMessageEncoder encoder) {
        this.config = config;
        this.encoder = encoder;

        createBootstrap(workerGroup);
    }

    public void createBootstrap(EventLoopGroup workerGroup) {
        final Bootstrap bootstrap = new Bootstrap();
        final GelfTcpChannelHandler handler = new GelfTcpChannelHandler(config, encoder, this);

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout())
                .option(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
                .remoteAddress(config.getHost(), config.getPort())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(handler);
                    }
                });

        this.handler.set(handler);

        bootstrap.connect().addListener(new TcpConnectionListener(this));
    }

    public int getReconnectDelay() {
        return config.getReconnectDelay();
    }

    @Override
    public void send(GelfMessage message) {
        handler.get().send(message);
    }

    @Override
    public void stop() {
        handler.get().stop();
        workerGroup.shutdownGracefully();
    }
}
