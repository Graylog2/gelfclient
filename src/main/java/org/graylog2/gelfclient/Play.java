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

package org.graylog2.gelfclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.graylog2.gelfclient.transport.GelfTcpChannelHandler;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class Play {
    public static void main(String... args) throws InterruptedException {
        final GelfMessageEncoder encoder = new GelfMessageEncoder();
        final Configuration config = new Configuration();

        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        final Bootstrap bootstrap = new Bootstrap();
        final GelfTcpChannelHandler handler = new GelfTcpChannelHandler(config, encoder);

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress("127.0.0.1", 12203)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                       ch.pipeline().addLast(handler);
                    }
                });

        ChannelFuture channelFuture = bootstrap.connect();

        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("Connected!");
                } else {
                    System.err.println("Connection failed!");
                    future.cause().printStackTrace();
                }
            }
        });

        GelfMessage msg = new GelfMessage(GelfMessageVersion.V1_1);

        handler.send(msg);

        channelFuture.sync();

    }
}
