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
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GelfCompressionGzipEncoderTest {

    @Test
    public void testEncode() throws Exception {
        final EmbeddedChannel channel = new EmbeddedChannel(new GelfCompressionGzipEncoder());
        final String message = "Test string";

        assertTrue(channel.writeOutbound(Unpooled.wrappedBuffer(message.getBytes(StandardCharsets.UTF_8))));
        assertTrue(channel.finish());

        final ByteBufInputStream byteBufInputStream = new ByteBufInputStream((ByteBuf) channel.readOutbound());
        final GZIPInputStream gzipInputStream = new GZIPInputStream(byteBufInputStream);

        byte[] bytes = new byte[message.length()];

        assertEquals(message.length(), gzipInputStream.read(bytes, 0, message.length()));
        assertEquals(message, new String(bytes, StandardCharsets.UTF_8));
    }
}