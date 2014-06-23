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
public class GelfMessage {
    private final GelfMessageVersion version;
    private double timestamp;
    private String message;
    private String fullMessage;
    private String host = "localhost";
    private final Map<String, Object> fields = new HashMap<>();

    public GelfMessage(GelfMessageVersion version) {
        this(version, System.currentTimeMillis() / 1000D);
    }

    public GelfMessage(GelfMessageVersion version, double timestamp) {
        this.version = version;
        this.timestamp = timestamp;
    }

    public GelfMessageVersion getVersion() {
        return version;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public void setFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Map<String, Object> getAdditionalFields() {
        return fields;
    }

    public void addAdditionalField(String key, Object value) {
        if (key == null) {
            return;
        }
        String realKey = key.startsWith("_") ? key : ("_" + key);

        fields.put(realKey, value);
    }

    @Override
    public String toString() {
        return String.format("[GelfMessage] version=\"%s\" timestamp=\"%.3f\" short_message=\"%s\"", getVersion().toString(), getTimestamp(), getMessage());
    }
}
