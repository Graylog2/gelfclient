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
import io.netty.handler.codec.EncoderException;
import org.testng.annotations.Test;

import java.util.Random;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

public class GelfMessageChunkEncoderTest {
    @Test
    public void testChunkedEncode() throws Exception {
        final EmbeddedChannel channel = new EmbeddedChannel(new GelfMessageChunkEncoder());
        final String largeMessage = largeMessage(1500);

        channel.writeOutbound(Unpooled.wrappedBuffer(largeMessage.getBytes()));

        final ByteBuf chunk1 = (ByteBuf) channel.readOutbound();
        final ByteBuf chunk2 = (ByteBuf) channel.readOutbound();

        // Generates two messages
        assertNull(channel.readOutbound());

        // Check for GELF chunked magic bytes
        assertEquals((byte) 0x1e, chunk1.readByte());
        assertEquals((byte) 0x0f, chunk1.readByte());
        assertEquals((byte) 0x1e, chunk2.readByte());
        assertEquals((byte) 0x0f, chunk2.readByte());

        // 8 bytes for the message ID
        assertEquals(8, chunk1.readBytes(8).array().length);
        assertEquals(8, chunk2.readBytes(8).array().length);

        // 1 byte sequence number
        assertEquals((byte) 0, chunk1.readByte());
        assertEquals((byte) 1, chunk2.readByte());

        // 1 byte sequence count
        assertEquals((byte) 2, chunk1.readByte());
        assertEquals((byte) 2, chunk2.readByte());

        // data bytes
        assertEquals(1420, chunk1.readableBytes());
        assertEquals(largeMessage.length() - 1420, chunk2.readableBytes());
    }

    @Test
    public void testChunkedEncodeExactChunkSize() throws Exception {
        final EmbeddedChannel channel = new EmbeddedChannel(new GelfMessageChunkEncoder());
        final byte[] largeMessage = new byte[2840];

        channel.writeOutbound(Unpooled.wrappedBuffer(largeMessage));

        final ByteBuf chunk1 = (ByteBuf) channel.readOutbound();
        final ByteBuf chunk2 = (ByteBuf) channel.readOutbound();

        // Generates two messages
        assertNull(channel.readOutbound());

        // Check for GELF chunked magic bytes
        assertEquals((byte) 0x1e, chunk1.readByte());
        assertEquals((byte) 0x0f, chunk1.readByte());
        assertEquals((byte) 0x1e, chunk2.readByte());
        assertEquals((byte) 0x0f, chunk2.readByte());

        // 8 bytes for the message ID
        assertEquals(8, chunk1.readBytes(8).array().length);
        assertEquals(8, chunk2.readBytes(8).array().length);

        // 1 byte sequence number
        assertEquals((byte) 0, chunk1.readByte());
        assertEquals((byte) 1, chunk2.readByte());

        // 1 byte sequence count
        assertEquals((byte) 2, chunk1.readByte());
        assertEquals((byte) 2, chunk2.readByte());

        // data bytes
        assertEquals(1420, chunk1.readableBytes());
        assertEquals(largeMessage.length - 1420, chunk2.readableBytes());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testShortMachineIdentifierShouldThrowException() throws Exception {
        new GelfMessageChunkEncoder(new byte[1]);
    }

    @Test(expectedExceptions = EncoderException.class)
    public void testTooLargeMessage() throws Exception {
        final EmbeddedChannel channel = new EmbeddedChannel(new GelfMessageChunkEncoder());

        channel.writeOutbound(Unpooled.wrappedBuffer(new byte[1420 * 128 + 1]));
    }

    private String largeMessage(int limit) {
        Random r = new Random();
        String largeMessage = "";

        while (largeMessage.length() < limit) {
            largeMessage += r.nextInt();
        }

        return largeMessage;
    }
}