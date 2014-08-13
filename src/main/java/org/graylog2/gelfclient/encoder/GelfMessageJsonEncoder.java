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

import com.fasterxml.jackson.core.JsonEncoding;
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
import java.util.List;
import java.util.Map;

/**
 * A Netty channel handler encoding {@link GelfMessage} into valid JSON according to the
 * <a href="http://graylog2.org/gelf#specs">GELF specification</a>.
 */
@ChannelHandler.Sharable
public class GelfMessageJsonEncoder extends MessageToMessageEncoder<GelfMessage> {
    private static final Logger LOG = LoggerFactory.getLogger(GelfMessageJsonEncoder.class);
    private final JsonFactory jsonFactory;

    /**
     * Creates a new instance of this channel handler with the default {@link com.fasterxml.jackson.core.JsonFactory}.
     */
    public GelfMessageJsonEncoder() {
        this(new JsonFactory());
    }

    /**
     * Creates a new instance of this channel handler with the given {@link com.fasterxml.jackson.core.JsonFactory}.
     *
     * @param jsonFactory the Jackson {@link com.fasterxml.jackson.core.JsonFactory} to use for constructing a GELF message payload
     */
    public GelfMessageJsonEncoder(final JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        LOG.error("JSON encoding error", cause);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, GelfMessage message, List<Object> out) throws Exception {
        out.add(Unpooled.wrappedBuffer(toJson(message)));
    }

    private byte[] toJson(final GelfMessage message) throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (final JsonGenerator jg = jsonFactory.createGenerator(out, JsonEncoding.UTF8)) {
            jg.writeStartObject();

            jg.writeStringField("version", message.getVersion().toString());
            jg.writeNumberField("timestamp", message.getTimestamp());
            jg.writeStringField("host", message.getHost());
            jg.writeStringField("short_message", message.getMessage());
            jg.writeStringField("full_message", message.getFullMessage());
            jg.writeNumberField("level", message.getLevel().getLevel());

            for (Map.Entry<String, Object> field : message.getAdditionalFields().entrySet()) {
                final String realKey = field.getKey().startsWith("_") ? field.getKey() : ("_" + field.getKey());

                if (field.getValue() instanceof Number) {
                    // Let Jackson figure out how to write Number values.
                    jg.writeObjectField(realKey, field.getValue());
                } else if (field.getValue() == null) {
                    jg.writeNullField(realKey);
                } else {
                    jg.writeStringField(realKey, field.getValue().toString());
                }
            }

            jg.writeEndObject();
        }

        // Graylog2 GELF TCP input uses NULL-byte as separator.
        out.write('\0');

        return out.toByteArray();
    }
}
