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
    public void testSetTimestamp() throws Exception {
        msg.setTimestamp(timestamp);

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
    public void testGetFullMessage() throws Exception {
        msg.setFullMessage("Hello full world!");

        assertEquals("Hello full world!", msg.getFullMessage());
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
    public void testAddAdditionalFieldWithNullKey() throws Exception {
        Map<String, Object> data = new HashMap<>();

        //data.put("_foo", "test");
        msg.addAdditionalField(null, "null");

        assertEquals(data, msg.getAdditionalFields());
    }

    @Test
    public void testAddAdditionalFieldWithNullValue() throws Exception {
        Map<String, Object> data = new HashMap<>();

        data.put("_null", null);
        msg.addAdditionalField("_null", null);

        assertEquals(data, msg.getAdditionalFields());
    }

    @Test
    public void testAddAdditionalFieldWithoutUnderscore() {
        msg.addAdditionalField("foobar", "test");

        assertEquals("test", msg.getAdditionalFields().get("_foobar"));
        assertNull(msg.getAdditionalFields().get("foobar"));
    }
}