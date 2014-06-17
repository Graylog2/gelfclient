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

package org.graylog2.gelfclient.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.graylog2.gelfclient.GelfConfiguration;
import org.testng.annotations.Test;

import java.util.Random;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

public class GelfMessageChunkEncoderTest {
    @Test
    public void testChunkedEncode() throws Exception {
        GelfConfiguration config = new GelfConfiguration();
        EmbeddedChannel channel = new EmbeddedChannel(new GelfMessageChunkEncoder(config));
        String largeMessage = largeMessage(1500);

        channel.writeOutbound(Unpooled.wrappedBuffer(largeMessage.getBytes()));

        ByteBuf chunk1 = (ByteBuf) channel.readOutbound();
        ByteBuf chunk2 = (ByteBuf) channel.readOutbound();

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

    private String largeMessage(int limit) {
        Random r = new Random();
        String largeMessage = "";

        while (largeMessage.length() < limit) {
            largeMessage += r.nextInt();
        }

        return largeMessage;
    }
}