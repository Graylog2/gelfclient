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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import org.graylog2.gelfclient.GelfMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class GelfTcpChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger LOG = LoggerFactory.getLogger(GelfTcpChannelHandler.class);
    private final GelfSenderThread senderThread;
    private final GelfTcpTransport transport;

    public GelfTcpChannelHandler(final BlockingQueue<GelfMessage> queue, GelfTcpTransport transport) {
        this.transport = transport;
        this.senderThread = new GelfSenderThread(queue);

        senderThread.start();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        LOG.debug("Received data!");
        LOG.debug(msg.toString());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        senderThread.setChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("Channel disconnected!");

        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(new Runnable() {
            @Override
            public void run() {
                LOG.debug("Starting reconnect!");
                transport.createBootstrap(loop);
            }
        }, transport.getReconnectDelay(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("Exception caught", cause);
    }

    public void stop() {
        senderThread.stop();
    }
}
