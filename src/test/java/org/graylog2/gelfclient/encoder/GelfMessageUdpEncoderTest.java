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

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class GelfMessageUdpEncoderTest {

    @Test
    public void testEncode() throws Exception {
        InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 12201);
        EmbeddedChannel channel = new EmbeddedChannel(new GelfMessageUdpEncoder(remoteAddress));

        // Test writing.
        assertTrue(channel.writeOutbound(Unpooled.wrappedBuffer("test".getBytes())));
        assertTrue(channel.finish());

        // Test reading.
        DatagramPacket datagramPacket = (DatagramPacket) channel.readOutbound();

        byte[] bytes = new byte[datagramPacket.content().readableBytes()];

        datagramPacket.content().getBytes(0, bytes);

        assertEquals(remoteAddress, datagramPacket.recipient());
        assertEquals("test", new String(bytes));
    }
}