/*
 * Copyright 2018 Graylog, Inc.
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

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class GelfHttpEncoderTest {

    @Test(expectedExceptions = EncoderException.class)
    public void testExceptionIsPassedThrough() throws Exception {
        final EmbeddedChannel channel = new EmbeddedChannel(new GelfHttpEncoder(null));
        channel.writeOutbound(Unpooled.EMPTY_BUFFER);
    }

    @Test
    public void testEncode() throws Exception {
        final URI uri = URI.create("http://example.org:8080/gelf");
        final EmbeddedChannel channel = new EmbeddedChannel(new GelfHttpEncoder(uri));
        assertTrue(channel.writeOutbound(Unpooled.copiedBuffer("{}", StandardCharsets.UTF_8)));
        assertTrue(channel.finish());

        final FullHttpRequest request = channel.readOutbound();
        assertEquals(HttpMethod.POST, request.method());
        assertEquals("/gelf", request.uri());
        assertEquals("application/json", request.headers().get(HttpHeaderNames.CONTENT_TYPE));
        assertEquals("2", request.headers().get(HttpHeaderNames.CONTENT_LENGTH));

        final byte[] bytes = ByteBufUtil.getBytes(request.content());
        assertEquals(new byte[]{'{', '}'}, bytes);
    }
}