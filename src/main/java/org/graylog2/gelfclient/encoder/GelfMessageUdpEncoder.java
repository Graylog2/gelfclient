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

package org.graylog2.gelfclient.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
@ChannelHandler.Sharable
public class GelfMessageUdpEncoder extends MessageToMessageEncoder<ByteBuf> {
    private final Logger LOG = LoggerFactory.getLogger(GelfMessageUdpEncoder.class);
    private final InetSocketAddress remoteAddress;

    public GelfMessageUdpEncoder(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        // Need to retain() the buffer here to avoid a io.netty.util.IllegalReferenceCountException.
        out.add(new DatagramPacket(buf.retain(), remoteAddress));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("UDP encoding error", cause);
    }
}
