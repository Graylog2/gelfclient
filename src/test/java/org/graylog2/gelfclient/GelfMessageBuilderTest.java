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

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;

public class GelfMessageBuilderTest {
    @Test
    public void testBuilder() throws Exception {
        final GelfMessage gelfMessage =
                new GelfMessageBuilder("hello builder message", "example.org")
                        .additionalField("_foo", "bar")
                        .additionalField("_baz", 123)
                        .build();

        assertEquals("hello builder message", gelfMessage.getMessage());
        assertEquals("example.org", gelfMessage.getHost());
        assertEquals(GelfMessageVersion.V1_1, gelfMessage.getVersion());
        assertEquals("bar", gelfMessage.getAdditionalFields().get("_foo"));
        assertEquals(123, gelfMessage.getAdditionalFields().get("_baz"));
    }

    @Test
    public void testReturnsNewMessageObjectEveryTime() throws Exception {
        final GelfMessageBuilder builder = new GelfMessageBuilder("hello builder message", "example.org");

        assertNotSame(builder.build(), builder.build());
    }
}