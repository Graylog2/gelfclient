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
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * A Netty channel handler which adds a single null byte ({@code \0}).
 */
public class GelfTcpFrameDelimiterEncoder extends MessageToMessageEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        final ByteBuf nullByteBuffer = Unpooled.wrappedBuffer(new byte[]{0});
        final ByteBuf byteBuf = Unpooled.copiedBuffer(msg, nullByteBuffer);
        out.add(byteBuf);
    }
}
