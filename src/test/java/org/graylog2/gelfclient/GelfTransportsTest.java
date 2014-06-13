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
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class GelfTransportsTest {
    @Test
    public void testCreateUdp() throws Exception {
        GelfConfiguration config = new GelfConfiguration();

        config.setTransport(GelfTransports.UDP);

        GelfTransport transportUdp = GelfTransports.create(config);

        assertEquals(GelfUdpTransport.class, transportUdp.getClass());
    }

    @Test
    public void testCreateTcp() throws Exception {
        GelfConfiguration config = new GelfConfiguration();

        config.setTransport(GelfTransports.TCP);

        GelfTransport transportTcp = GelfTransports.create(config);

        assertEquals(GelfTcpTransport.class, transportTcp.getClass());
    }
}