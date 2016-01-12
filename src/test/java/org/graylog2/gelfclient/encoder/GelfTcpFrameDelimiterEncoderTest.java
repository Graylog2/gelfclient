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
import io.netty.channel.embedded.EmbeddedChannel;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class GelfTcpFrameDelimiterEncoderTest {

    @Test
    public void testEncode() throws Exception {
        final EmbeddedChannel channel = new EmbeddedChannel(new GelfTcpFrameDelimiterEncoder());
        final byte[] message = "Test string".getBytes(StandardCharsets.UTF_8);

        assertTrue(channel.writeOutbound(Unpooled.wrappedBuffer(message)));
        assertTrue(channel.finish());

        final ByteBuf outboundBuffer = (ByteBuf) channel.readOutbound();
        final byte[] bytes = outboundBuffer.array();

        assertEquals(bytes[bytes.length - 1], (byte) 0);
        assertEquals(bytes.length, message.length + 1);
        assertNull(channel.readOutbound());
    }

    @Test
    public void testEncodeMultipleMessages() throws Exception {
        final EmbeddedChannel channel = new EmbeddedChannel(new GelfTcpFrameDelimiterEncoder());
        final byte[] message1 = "Test1".getBytes(StandardCharsets.UTF_8);
        final byte[] message2 = "Test2".getBytes(StandardCharsets.UTF_8);

        assertTrue(channel.writeOutbound(Unpooled.wrappedBuffer(message1)));
        assertTrue(channel.writeOutbound(Unpooled.wrappedBuffer(message2)));
        assertTrue(channel.finish());

        final ByteBuf outboundBuffer1 = (ByteBuf) channel.readOutbound();
        final byte[] bytes1 = outboundBuffer1.array();
        assertEquals(bytes1[bytes1.length - 1], (byte) 0);
        assertEquals(bytes1.length, message1.length + 1);

        final ByteBuf outboundBuffer2 = (ByteBuf) channel.readOutbound();
        final byte[] bytes2 = outboundBuffer2.array();

        assertEquals(bytes2[bytes2.length - 1], (byte) 0);
        assertEquals(bytes2.length, message2.length + 1);
        assertNull(channel.readOutbound());
    }
}