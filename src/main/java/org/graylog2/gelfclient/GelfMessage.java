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

package org.graylog2.gelfclient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class GelfMessage {
    private final GelfMessageVersion version;
    private final double timestamp;
    private String message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
        fields.put(key, value);
    }

    @Override
    public String toString() {
        return String.format("[GelfMessage] version=\"%s\" timestamp=\"%.3f\" short_message=\"%s\"", getVersion().toString(), getTimestamp(), getMessage());
    }
}
