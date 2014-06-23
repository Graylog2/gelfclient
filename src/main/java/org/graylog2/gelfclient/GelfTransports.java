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

import org.graylog2.gelfclient.transport.GelfTcpTransport;
import org.graylog2.gelfclient.transport.GelfTransport;
import org.graylog2.gelfclient.transport.GelfUdpTransport;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public enum GelfTransports {
    TCP,
    UDP;

    public static GelfTransport create(GelfConfiguration config) {
        GelfTransport transport = null;

        switch (config.getTransport()) {
            case TCP:
                transport = new GelfTcpTransport(config);
                break;
            case UDP:
                transport = new GelfUdpTransport(config);
                break;
        }

        return transport;
    }
}
