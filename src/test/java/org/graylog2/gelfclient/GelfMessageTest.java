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

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class GelfMessageTest {
    @Test
    public void testGetVersion() throws Exception {
        assertEquals(GelfMessageVersion.V1_1, new GelfMessage("Test").getVersion());
    }

    @Test
    public void testSetTimestamp() throws Exception {
        final double timestamp = System.currentTimeMillis() / 1000d;
        final GelfMessage message = new GelfMessage("Test");
        message.setTimestamp(timestamp);

        assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    public void testAutoTimestamp() throws Exception {
        final GelfMessage message = new GelfMessage("Test");

        assertTrue(message.getTimestamp() <= System.currentTimeMillis() / 1000d);
    }

    @Test
    public void testGetMessage() throws Exception {
        final GelfMessage message = new GelfMessage("Hello world!");

        assertEquals("Hello world!", message.getMessage());
    }

    @Test
    public void testGetFullMessage() throws Exception {
        final GelfMessage message = new GelfMessage("Test");
        message.setFullMessage("Hello full world!");

        assertEquals("Hello full world!", message.getFullMessage());
    }

    @Test
    public void testGetHost() throws Exception {
        assertEquals("localhost", new GelfMessage("Test").getHost());
        assertEquals("example.com", new GelfMessage("Test", "example.com").getHost());
    }

    @Test
    public void testGetAdditionalFields() throws Exception {
        assertTrue(new GelfMessage("Test").getAdditionalFields().isEmpty());
    }

    @Test
    public void testAddAdditionalField() throws Exception {
        final Map<String, Object> data = new HashMap<>();
        data.put("_foo", "test");
        data.put("_bar", 10);

        final GelfMessage message = new GelfMessage("Test");
        message.addAdditionalField("_foo", "test");
        message.addAdditionalField("_bar", 10);

        assertEquals(data, message.getAdditionalFields());
    }

    @Test
    public void testAddAdditionalFields() throws Exception {
        final Map<String, Object> data = new HashMap<>();
        data.put("_foo", "test");
        data.put("_bar", 10);

        final GelfMessage message = new GelfMessage("Test");
        message.addAdditionalFields(data);


        assertEquals(data, message.getAdditionalFields());
    }

    @Test
    public void testAddAdditionalFieldWithNullKey() throws Exception {
        final GelfMessage message = new GelfMessage("Test");
        message.addAdditionalField(null, "null");

        assertTrue(message.getAdditionalFields().isEmpty());
    }

    @Test
    public void testAddAdditionalFieldWithNullValue() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("_null", null);

        final GelfMessage message = new GelfMessage("Test");
        message.addAdditionalField("_null", null);

        assertEquals(data, message.getAdditionalFields());
    }

    @Test
    public void testEquals() throws Exception {
        final GelfMessage message = new GelfMessage("Test");

        assertEquals(new GelfMessage("Test"), message);
        assertNotEquals(new GelfMessage("Not Equal"), message);
    }

    @Test
    public void testHashCode() throws Exception {
        final GelfMessage message = new GelfMessage("Test");

        assertEquals(new GelfMessage("Test").hashCode(), message.hashCode());
        assertNotEquals(new GelfMessage("Not Equal"), message);
    }

    @Test
    public void testHashCodeIgnoresAdditionalFields() throws Exception {
        final GelfMessage message = new GelfMessage("Test");
        message.addAdditionalField("key", "value");

        assertEquals(new GelfMessage("Test").hashCode(), message.hashCode());
        assertNotEquals(new GelfMessage("NotEqual").hashCode(), message.hashCode());
    }

    @Test
    public void testToString() throws Exception {
        final GelfMessage message = new GelfMessage("Test");
        message.setTimestamp(123456.0d);
        message.addAdditionalField("additional_key", "additional_value");

        assertTrue(message.toString().contains("1.1"));
        assertTrue(message.toString().contains("Test"));
        assertTrue(message.toString().contains("123456"));
        assertTrue(message.toString().contains("ALERT"));
        assertFalse(message.toString().contains("additional_key"));
    }
}