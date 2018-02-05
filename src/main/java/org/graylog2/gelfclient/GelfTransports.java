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

import org.graylog2.gelfclient.transport.GelfHttpTransport;
import org.graylog2.gelfclient.transport.GelfTcpTransport;
import org.graylog2.gelfclient.transport.GelfTransport;
import org.graylog2.gelfclient.transport.GelfUdpTransport;

/**
 * Factory for building a {@link GelfTransport}.
 */
public enum GelfTransports {
    TCP,
    UDP,
    HTTP;

    /**
     * Creates a {@link GelfTransport} from the given protocol and configuration.
     *
     * @param transport the transport protocol to use
     * @param config    the {@link GelfConfiguration} to pass to the transport
     * @return An initialized and started {@link GelfTransport}
     */
    public static GelfTransport create(final GelfTransports transport, final GelfConfiguration config) {
        GelfTransport gelfTransport;

        switch (transport) {
            case TCP:
                gelfTransport = new GelfTcpTransport(config);
                break;
            case UDP:
                gelfTransport = new GelfUdpTransport(config);
                break;
            case HTTP:
                gelfTransport = new GelfHttpTransport(config);
                break;
            default:
                throw new IllegalArgumentException("Unsupported GELF transport: " + transport);
        }

        return gelfTransport;
    }

    /**
     * Creates a {@link GelfTransport} from the given configuration.
     *
     * @param config the {@link GelfConfiguration} to pass to the transport
     * @return An initialized and started {@link GelfTransport}
     */
    public static GelfTransport create(final GelfConfiguration config) {
        return create(config.getTransport(), config);
    }
}
