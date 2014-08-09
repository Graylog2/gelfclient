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
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class GelfTransportsTest {
    @Test
    public void testCreateUdp() throws Exception {
        final GelfTransport transport = GelfTransports.create(GelfTransports.UDP, new GelfConfiguration());

        assertEquals(GelfUdpTransport.class, transport.getClass());
    }

    @Test
    public void testCreateTcp() throws Exception {
        final GelfTransport transport = GelfTransports.create(GelfTransports.TCP, new GelfConfiguration());

        assertEquals(GelfTcpTransport.class, transport.getClass());
    }

    @Test
    public void testCreateTransportFromGelfConfiguration() throws Exception {
        final GelfConfiguration gelfConfiguration = new GelfConfiguration();
        gelfConfiguration.setTransport(GelfTransports.UDP);

        final GelfTransport transport = GelfTransports.create(gelfConfiguration);

        assertEquals(GelfUdpTransport.class, transport.getClass());
    }
}