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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageVersion;
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
        message = new GelfMessage(GelfMessageVersion.V1_1);

        message.setMessage("test");
        message.addAdditionalField("_foo", 1.0);
        message.addAdditionalField("_bar", 128);
        message.addAdditionalField("_baz", "a value");

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
        message = new GelfMessage(GelfMessageVersion.V1_1);

        message.setMessage("test");
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
        assertEquals(1.0, _foo);
        assertEquals(128, _bar);
        assertEquals("a value", _baz);
    }
}