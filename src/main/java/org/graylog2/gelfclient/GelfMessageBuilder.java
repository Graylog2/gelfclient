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

package org.graylog2.gelfclient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class GelfMessageBuilder {
    private final GelfMessageVersion version;
    private final String host;
    private final String message;
    private String fullMessage;
    private Double timestamp;
    private GelfMessageLevel level = GelfMessageLevel.ALERT;
    private final Map<String, Object> fields = new HashMap<>();

    public GelfMessageBuilder(final String message) {
        this(message, "localhost");
    }

    public GelfMessageBuilder(final String message, final String host) {
        this(message, host, GelfMessageVersion.V1_1);
    }

    public GelfMessageBuilder(final String message, final String host, final GelfMessageVersion version) {
        this.message = message;
        this.host = host;
        this.version = version;
    }

    public GelfMessageBuilder fullMessage(final String fullMessage) {
        this.fullMessage = fullMessage;
        return this;
    }

    public GelfMessageBuilder timestamp(final double timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public GelfMessageBuilder level(final GelfMessageLevel level) {
        this.level = level;
        return this;
    }

    public GelfMessageBuilder additionalField(final String key, final Object value) {
        fields.put(key, value);
        return this;
    }

    public GelfMessageBuilder additionalFields(final Map<String, Object> additionalFields) {
        fields.putAll(additionalFields);
        return this;
    }

    public GelfMessage build() {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null!");
        }

        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("host must not be null or empty!");
        }

        if (version == null) {
            throw new IllegalArgumentException("version must not be null!");
        }

        if (level == null) {
            throw new IllegalArgumentException("level must not be null!");
        }

        final GelfMessage gelfMessage = new GelfMessage(message, host, version);

        gelfMessage.setFullMessage(fullMessage);
        gelfMessage.setLevel(level);

        if (timestamp != null) {
            gelfMessage.setTimestamp(timestamp);
        }

        gelfMessage.addAdditionalFields(fields);

        return gelfMessage;
    }
}
