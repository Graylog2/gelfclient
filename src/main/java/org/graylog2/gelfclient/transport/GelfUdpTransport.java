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
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.graylog2.gelfclient.Configuration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageEncoder;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class GelfUdpTransport implements GelfTransport {
    private final Configuration config;
    private final GelfMessageEncoder encoder;
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private AtomicReference<GelfUdpChannelHandler> handler = new AtomicReference<>(null);

    public GelfUdpTransport(Configuration config, GelfMessageEncoder encoder) {
        this.config = config;
        this.encoder = encoder;

        createBootstrap(workerGroup);
    }

    public void createBootstrap(EventLoopGroup workerGroup) {
        final Bootstrap bootstrap = new Bootstrap();
        final GelfUdpChannelHandler handler = new GelfUdpChannelHandler(config, encoder);

        bootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .handler(handler);

        this.handler.set(handler);

        bootstrap.bind(0);
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
