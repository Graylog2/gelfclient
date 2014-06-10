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

    public GelfMessage(GelfMessageVersion version) {
        this.version = version;
        this.timestamp = System.currentTimeMillis() / 1000D;
    }

    public GelfMessageVersion getVersion() {
        return version;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return "This is the short_message.";
    }

    public String getHost() {
        return "localhost";
    }

    public Map<String, Object> getAdditionalFields() {
        Map<String, Object> fields = new HashMap<>();

        fields.put("_foo", "bar");

        return fields;
    }

    public String toString() {
        return String.format("[GelfMessage] version=\"%s\" short_message=\"%s\"", getVersion().toString(), getMessage());
    }
}
