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
 * {@link GelfMessageBuilder} can be used to build one or more valid {@link GelfMessage}s.
 * <p>
 * Since the message properties are stored in the {@link GelfMessageBuilder} instance, it
 * can be used as a template for building {@link GelfMessage}s.
 * </p>
 * <p>
 * This class is <em>not</em> thread-safe.
 * </p>
 *
 * @author <a href="bernd@torch.sh">Bernd Ahlers</a>
 * @see GelfMessage
 */
public class GelfMessageBuilder {
    private final GelfMessageVersion version;
    private final String host;
    private String message;
    private String fullMessage;
    private Double timestamp;
    private GelfMessageLevel level = GelfMessageLevel.ALERT;
    private final Map<String, Object> fields = new HashMap<>();

    /**
     * Construct a {@link GelfMessageBuilder} instance with the given message.
     *
     * @param message The message of the {@link GelfMessage}
     */
    public GelfMessageBuilder(final String message) {
        this(message, "localhost");
    }

    /**
     * Construct a {@link GelfMessageBuilder} instance with the given message and @{code host} field.
     *
     * @param message The message of the {@link GelfMessage}
     * @param host    The contents of the {@code host} field of the {@link GelfMessage}
     */
    public GelfMessageBuilder(final String message, final String host) {
        this(message, host, GelfMessageVersion.V1_1);
    }

    /**
     * Construct a {@link GelfMessageBuilder} instance with the given message and @{code host} field.
     *
     * @param message The message of the {@link GelfMessage}
     * @param host    The contents of the {@code host} field of the {@link GelfMessage}
     * @param version The version of the <a href="http://graylog2.org/gelf#specs">GELF specification</a> to use
     */
    public GelfMessageBuilder(final String message, final String host, final GelfMessageVersion version) {
        this.message = message;
        this.host = host;
        this.version = version;
    }

    /**
     * Set the (short) message of the {@link GelfMessage}.
     *
     * @param message the (short) message of the {@link GelfMessage}
     * @return {@code this} instance
     */
    public GelfMessageBuilder message(final String message) {
        this.message = message;
        return this;
    }

    /**
     * Set the full message (e. g. containing a stack trace) of the {@link GelfMessage}.
     *
     * @param fullMessage the full message of the {@link GelfMessage}
     * @return {@code this} instance
     */
    public GelfMessageBuilder fullMessage(final String fullMessage) {
        this.fullMessage = fullMessage;
        return this;
    }

    /**
     * Set the timestamp (seconds since UNIX epoch) of the {@link GelfMessage}.
     *
     * @param timestamp the timestamp of the {@link GelfMessage} (seconds since UNIX epoch)
     * @return {@code this} instance
     */
    public GelfMessageBuilder timestamp(final double timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Set the timestamp (milliseconds since UNIX epoch) of the {@link GelfMessage}.
     *
     * @param millis the timestamp of the {@link GelfMessage} (milliseconds since UNIX epoch)
     * @return {@code this} instance
     */
    public GelfMessageBuilder timestamp(final long millis) {
        this.timestamp = millis / 1000D;
        return this;
    }

    /**
     * Set the level (priority) of the {@link GelfMessage}.
     *
     * Can be set to {@code null} to remove the (optional) level from the {@link GelfMessage}.
     *
     * @param level the {@link GelfMessageLevel} of the {@link GelfMessage}.
     * @return {@code this} instance
     */
    public GelfMessageBuilder level(final GelfMessageLevel level) {
        this.level = level;
        return this;
    }

    /**
     * Add an additional field (key-/value-pair) to the {@link GelfMessage}.
     *
     * @param key   the key of the additional field
     * @param value the value of the additional field
     * @return {@code this} instance
     */
    public GelfMessageBuilder additionalField(final String key, final Object value) {
        fields.put(key, value);
        return this;
    }

    /**
     * Add the contents of a {@link Map} as additional fields (key-/value-pairs) to the {@link GelfMessage}.
     *
     * @param additionalFields the {@link Map} which will be added as additional fields
     * @return {@code this} instance
     */
    public GelfMessageBuilder additionalFields(final Map<String, Object> additionalFields) {
        fields.putAll(additionalFields);
        return this;
    }

    /**
     * Build a new {@link GelfMessage} with all information from the {@link GelfMessageBuilder}.
     *
     * @return a new {@link GelfMessage} instance.
     * @throws IllegalArgumentException if any mandatory information is missing.
     */
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

        final GelfMessage gelfMessage = new GelfMessage(message, host, version);

        gelfMessage.setLevel(level);

        if (fullMessage != null) {
            gelfMessage.setFullMessage(fullMessage);
        }

        if (timestamp != null) {
            gelfMessage.setTimestamp(timestamp);
        }

        gelfMessage.addAdditionalFields(fields);

        return gelfMessage;
    }
}
