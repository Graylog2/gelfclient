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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class GelfMessageJsonEncoderTest {
    private EmbeddedChannel channel;
    private GelfMessage message;

    @BeforeMethod
    public void setup() {
        channel = new EmbeddedChannel(new GelfMessageJsonEncoder());
        message = new GelfMessageBuilder("test")
                .fullMessage("The full message!")
                .additionalField("_foo", 1.0)
                .additionalField("_bar", 128)
                .additionalField("_baz", "a value")
                .build();

        assertTrue(channel.writeOutbound(message));
        assertTrue(channel.finish());
    }

    private byte[] readBytes() {
        ByteBuf buf = (ByteBuf) channel.readOutbound();
        byte[] bytes = new byte[buf.readableBytes()];

        buf.getBytes(0, bytes).release();

        return bytes;
    }

    @Test
    public void testLastByteIsNull() throws Exception {
        byte[] bytes = readBytes();

        String s = new String(bytes);

        assertEquals("}".charAt(0), bytes[bytes.length - 2]);
        assertEquals('\0', bytes[bytes.length - 1]);
    }

    @Test
    public void testNullValue() throws Exception {
        channel = new EmbeddedChannel(new GelfMessageJsonEncoder());
        message = new GelfMessage("test");
        message.addAdditionalField("_null", null);

        assertTrue(channel.writeOutbound(message));
    }

    @Test
    public void testEncode() throws Exception {
        byte[] bytes = readBytes();

        JsonFactory json = new JsonFactory();
        JsonParser parser = json.createParser(bytes);

        String version = null;
        Number timestamp = null;
        String host = null;
        String short_message = null;
        String full_message = null;
        Number _foo = null;
        Number _bar = null;
        String _baz = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String key = parser.getCurrentName();

            if (key == null) {
                continue;
            }

            parser.nextToken();

            switch (key) {
                case "version":
                    version = parser.getText();
                    break;
                case "timestamp":
                    timestamp = parser.getNumberValue();
                    break;
                case "host":
                    host = parser.getText();
                    break;
                case "short_message":
                    short_message = parser.getText();
                    break;
                case "full_message":
                    full_message = parser.getText();
                    break;
                case "_foo":
                    _foo = parser.getNumberValue();
                    break;
                case "_bar":
                    _bar = parser.getNumberValue();
                    break;
                case "_baz":
                    _baz = parser.getText();
                    break;
                default:
                    throw new Exception("Found unexpected field in JSON payload: " + key);
            }
        }

        assertEquals(message.getVersion().toString(), version);
        assertEquals(message.getTimestamp(), timestamp);
        assertEquals(message.getHost(), host);
        assertEquals(message.getMessage(), short_message);
        assertEquals(message.getFullMessage(), full_message);
        assertEquals(1.0, _foo);
        assertEquals(128, _bar);
        assertEquals("a value", _baz);
    }
}