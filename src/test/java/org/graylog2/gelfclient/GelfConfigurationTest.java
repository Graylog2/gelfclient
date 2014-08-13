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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;

import static org.testng.AssertJUnit.assertEquals;

public class GelfConfigurationTest {
    private GelfConfiguration config;

    @BeforeMethod
    public void setup() {
        this.config = new GelfConfiguration();
    }

    @Test
    public void testQueueSize() {
        // Check default value.
        assertEquals(512, config.getQueueSize());

        config.queueSize(124);

        assertEquals(124, config.getQueueSize());
    }

    @Test
    public void testRemoteAddress() {
        assertEquals(new InetSocketAddress("127.0.0.1", 12201), config.getRemoteAddress());
        assertEquals(InetSocketAddress.createUnresolved("10.0.0.1", 12345),
                new GelfConfiguration(InetSocketAddress.createUnresolved("10.0.0.1", 12345)).getRemoteAddress());
    }

    @Test
    public void testPort() {
        assertEquals(12201, config.getRemoteAddress().getPort());
        assertEquals(10000, new GelfConfiguration(10000).getRemoteAddress().getPort());
    }

    @Test
    public void testHostName() {
        assertEquals("127.0.0.1", config.getRemoteAddress().getHostString());
        assertEquals("example.com", new GelfConfiguration("example.com").getRemoteAddress().getHostName());
    }

    @Test
    public void testTransport() {
        assertEquals(GelfTransports.TCP, config.getTransport());
        config.transport(GelfTransports.UDP);
        assertEquals(GelfTransports.UDP, config.getTransport());
    }

    @Test
    public void testReconnectDelay() {
        // Check default value.
        assertEquals(500, config.getReconnectDelay());

        config.reconnectDelay(5000);

        assertEquals(5000, config.getReconnectDelay());
    }

    @Test
    public void testConnectTimeout() {
        // Check default value.
        assertEquals(1000, config.getConnectTimeout());

        config.connectTimeout(10000);

        assertEquals(10000, config.getConnectTimeout());
    }

    @Test
    public void testtcpNoDelay() {
        // Check default value.
        assertEquals(false, config.isTcpNoDelay());

        config.tcpNoDelay(true);

        assertEquals(true, config.isTcpNoDelay());
    }

    @Test
    public void testSendBufferSize() {
        // Check default value.
        assertEquals(-1, config.getSendBufferSize());

        config.sendBufferSize(32768);

        assertEquals(32768, config.getSendBufferSize());
    }
}