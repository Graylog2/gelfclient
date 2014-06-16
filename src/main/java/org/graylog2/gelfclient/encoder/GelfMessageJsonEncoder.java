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
import com.fasterxml.jackson.core.JsonGenerator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.graylog2.gelfclient.GelfMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
@ChannelHandler.Sharable
public class GelfMessageJsonEncoder extends MessageToMessageEncoder<GelfMessage> {
    private final Logger LOG = LoggerFactory.getLogger(GelfMessageJsonEncoder.class);
    private final JsonFactory jsonFactory = new JsonFactory();

    public GelfMessageJsonEncoder() {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("JSON encoding error", cause);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, GelfMessage msg, List<Object> out) throws Exception {
        final byte[] message = toJson(msg);

        if (message != null) {
            out.add(Unpooled.wrappedBuffer(message));
        }
    }

    private byte[] toJson(GelfMessage message) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            JsonGenerator jg = jsonFactory.createGenerator(out);

            jg.writeStartObject();
            jg.writeStringField("version", message.getVersion().toString());
            jg.writeNumberField("timestamp", message.getTimestamp());
            jg.writeStringField("host", message.getHost());
            jg.writeStringField("short_message", message.getMessage());

            for (Map.Entry<String, Object> field : message.getAdditionalFields().entrySet()) {
                if (field.getValue() instanceof Number) {
                    // Let Jackson figure out how to write Number values.
                    jg.writeObjectField(field.getKey(), field.getValue());
                } else {
                    jg.writeStringField(field.getKey(), field.getValue().toString());
                }
            }

            jg.writeEndObject();
            jg.close();
        } catch (IOException e) {
            LOG.error("Message encoding failed", e);
            return null;
        }

        // Graylog2 GELF TCP input uses NULL-byte as separator.
        out.write('\0');

        return out.toByteArray();
    }
}
