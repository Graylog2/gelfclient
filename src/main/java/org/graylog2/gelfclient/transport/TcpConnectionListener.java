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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class TcpConnectionListener implements ChannelFutureListener {
    private final Logger LOG = LoggerFactory.getLogger(TcpConnectionListener.class);
    private final GelfTcpTransport transport;

    public TcpConnectionListener(GelfTcpTransport transport) {
        this.transport = transport;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            LOG.debug("Connected!");
        } else {
            LOG.error("Connection failed: {}", future.cause().getMessage());

            final EventLoop loop = future.channel().eventLoop();
            loop.schedule(new Runnable() {
                @Override
                public void run() {
                    LOG.debug("Starting reconnect!");
                    transport.createBootstrap(loop);
                }
            }, transport.getReconnectDelay(), TimeUnit.MILLISECONDS);
        }
    }
}
