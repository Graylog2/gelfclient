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
            jg.writeStringField("full_message", message.getFullMessage());

            for (Map.Entry<String, Object> field : message.getAdditionalFields().entrySet()) {
                if (field.getValue() instanceof Number) {
                    // Let Jackson figure out how to write Number values.
                    jg.writeObjectField(field.getKey(), field.getValue());
                } else if (field.getValue() == null) {
                    jg.writeNullField(field.getKey());
                } else {
                    jg.writeStringField(field.getKey(), field.getValue().toString());
                }
            }

            jg.writeEndObject();
            jg.close();
        } catch (IOException e) {
            LOG.error("Message encoding failed", e);
            return null;
        } catch (Exception e) {
            LOG.error("JSON encoding error", e);
            return null;
        }

        // Graylog2 GELF TCP input uses NULL-byte as separator.
        out.write('\0');

        return out.toByteArray();
    }
}
