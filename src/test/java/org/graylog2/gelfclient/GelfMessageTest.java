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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

public class GelfMessageTest {
    private GelfMessage msg;
    private final double timestamp = System.currentTimeMillis() / 1000D;

    @BeforeMethod
    public void setUp() {
        msg = new GelfMessage(GelfMessageVersion.V1_1, timestamp);
    }

    @Test
    public void testGetVersion() throws Exception {
        assertEquals(GelfMessageVersion.V1_1, msg.getVersion());
    }

    @Test
    public void testGetTimestamp() throws Exception {
        assertEquals(timestamp, msg.getTimestamp());
    }

    @Test
    public void testAutoTimestamp() throws Exception {
        GelfMessage message = new GelfMessage(GelfMessageVersion.V1_1);

        assertNotNull(message.getTimestamp());
    }

    @Test
    public void testGetMessage() throws Exception {
        msg.setMessage("Hello world!");

        assertEquals("Hello world!", msg.getMessage());
    }

    @Test
    public void testGetHost() throws Exception {
        // Check default.
        assertEquals("localhost", msg.getHost());

        msg.setHost("my-machine");

        assertEquals("my-machine", msg.getHost());
    }

    @Test
    public void testGetAdditionalFields() throws Exception {
        Map<String, String> data = new HashMap<>();

        assertEquals(data, msg.getAdditionalFields());
    }

    @Test
    public void testAddAdditionalField() throws Exception {
        Map<String, Object> data = new HashMap<>();

        data.put("_foo", "test");
        data.put("_bar", 10);
        msg.addAdditionalField("_foo", "test");
        msg.addAdditionalField("_bar", 10);

        assertEquals(data, msg.getAdditionalFields());
    }

    @Test
    public void testAddAdditionalFieldWithoutUnderscore() {
        msg.addAdditionalField("foobar", "test");

        assertEquals("test", msg.getAdditionalFields().get("_foobar"));
        assertNull(msg.getAdditionalFields().get("foobar"));
    }
}