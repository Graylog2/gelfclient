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
import java.util.Objects;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class GelfMessage {
    private final GelfMessageVersion version;
    private final String host;
    private final String message;
    private String fullMessage;
    private double timestamp = System.currentTimeMillis() / 1000D;
    private GelfMessageLevel level = GelfMessageLevel.ALERT;
    private final Map<String, Object> additionalFields = new HashMap<>();

    public GelfMessage(final String message) {
        this(message, "localhost");
    }

    public GelfMessage(final String message, final String host) {
        this(message, host, GelfMessageVersion.V1_1);
    }

    public GelfMessage(final String message, final String host, final GelfMessageVersion version) {
        this.message = message;
        this.host = host;
        this.version = version;
    }

    public GelfMessageVersion getVersion() {
        return version;
    }

    public String getHost() {
        return host;
    }

    public String getMessage() {
        return message;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public void setFullMessage(final String fullMessage) {
        this.fullMessage = fullMessage;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final double timestamp) {
        this.timestamp = timestamp;
    }

    public GelfMessageLevel getLevel() {
        return level;
    }

    public void setLevel(final GelfMessageLevel level) {
        this.level = level;
    }

    public Map<String, Object> getAdditionalFields() {
        return additionalFields;
    }

    public void addAdditionalField(final String key, final Object value) {
        if (key == null) {
            return;
        }

        String realKey = key.startsWith("_") ? key : ("_" + key);

        additionalFields.put(realKey, value);
    }

    public void addAdditionalFields(final Map<String, Object> additionalFields) {
        this.additionalFields.putAll(additionalFields);
    }

    @Override
    public String toString() {
        return String.format("GelfMessage{version=\"%s\" timestamp=\"%.3f\" short_message=\"%s\", level=\"%s\"}",
                version, timestamp, message, level);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final GelfMessage that = (GelfMessage) o;

        if (version != that.version) return false;
        if (!message.equals(that.message)) return false;
        if (!host.equals(that.host)) return false;
        if (level != that.level) return false;
        if (Double.compare(that.timestamp, timestamp) != 0) return false;
        if (fullMessage != null ? !fullMessage.equals(that.fullMessage) : that.fullMessage != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, host, message, fullMessage, timestamp);
    }
}
