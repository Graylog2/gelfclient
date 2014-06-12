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

import org.graylog2.gelfclient.transport.GelfTcpTransport;
import org.graylog2.gelfclient.transport.GelfTransport;
import org.graylog2.gelfclient.transport.GelfUdpTransport;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public enum GelfTransports {
    TCP,
    UDP;

    public static GelfTransport create(Configuration config) {
        return create(config, new GelfMessageEncoder());
    }

    public static GelfTransport create(Configuration config, GelfMessageEncoder encoder) {
        GelfTransport transport = null;

        switch (config.getProtocol()) {
            case TCP:
                transport = new GelfTcpTransport(config, encoder);
                break;
            case UDP:
                transport = new GelfUdpTransport(config, encoder);
                break;
        }

        return transport;
    }
}
