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

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;

public class GelfMessageBuilderTest {
    @Test
    public void testBuilder() throws Exception {
        final Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("_qux", 456.789d);

        final GelfMessage gelfMessage =
                new GelfMessageBuilder("hello builder message", "example.org")
                        .timestamp(0.0d)
                        .additionalField("_foo", "bar")
                        .additionalField("_baz", 123)
                        .additionalFields(additionalFields)
                        .build();

        assertEquals("hello builder message", gelfMessage.getMessage());
        assertEquals("example.org", gelfMessage.getHost());
        assertEquals(GelfMessageVersion.V1_1, gelfMessage.getVersion());
        assertEquals(0.0d, gelfMessage.getTimestamp());
        assertEquals("bar", gelfMessage.getAdditionalFields().get("_foo"));
        assertEquals(123, gelfMessage.getAdditionalFields().get("_baz"));
        assertEquals(456.789d, gelfMessage.getAdditionalFields().get("_qux"));
    }

    @Test
    public void testReturnsNewMessageObjectEveryTime() throws Exception {
        final GelfMessageBuilder builder = new GelfMessageBuilder("hello builder message", "example.org");

        assertNotSame(builder.build(), builder.build());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testMissingMessageThrowsException() throws Exception {
        new GelfMessageBuilder(null).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testMissingHostThrowsException() throws Exception {
        new GelfMessageBuilder("Test", null).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testEmptyHostThrowsException() throws Exception {
        new GelfMessageBuilder("Test", "").build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBlankHostThrowsException() throws Exception {
        new GelfMessageBuilder("Test", "  ").build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testMissingVersionThrowsException() throws Exception {
        new GelfMessageBuilder("Test", "localhost", null).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testMissingLevelThrowsException() throws Exception {
        new GelfMessageBuilder("Test").level(null).build();
    }
}