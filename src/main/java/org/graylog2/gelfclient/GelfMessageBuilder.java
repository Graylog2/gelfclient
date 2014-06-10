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

import java.util.Map;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class GelfMessageBuilder {
    private final GelfMessage templateMessage;

    public GelfMessageBuilder(GelfMessageVersion version) {
        this.templateMessage = new GelfMessage(version);
    }

    public GelfMessageBuilder setHost(String host) {
        templateMessage.setHost(host);

        return this;
    }

    public GelfMessageBuilder setMessage(String message) {
        templateMessage.setMessage(message);

        return this;
    }

    public GelfMessageBuilder addAdditionalField(String key, Object value) {
        templateMessage.addAdditionalField(key, value);

        return this;
    }

    public GelfMessage build() {
        final GelfMessage message = new GelfMessage(templateMessage.getVersion());

        message.setHost(templateMessage.getHost());
        message.setMessage(templateMessage.getMessage());

        for (final Map.Entry<String, Object> entry : templateMessage.getAdditionalFields().entrySet()) {
            message.addAdditionalField(entry.getKey(), entry.getValue());
        }

        return message;
    }
}
