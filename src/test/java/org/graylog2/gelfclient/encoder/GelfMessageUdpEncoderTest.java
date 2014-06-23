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