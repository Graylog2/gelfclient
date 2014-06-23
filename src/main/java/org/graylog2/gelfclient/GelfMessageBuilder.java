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
