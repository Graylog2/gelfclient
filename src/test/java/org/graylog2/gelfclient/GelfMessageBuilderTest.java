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
import static org.testng.AssertJUnit.*;

public class GelfMessageBuilderTest {
    private GelfMessageBuilder builder;

    @BeforeMethod
    public void setUp() {
        builder = new GelfMessageBuilder(GelfMessageVersion.V1_1);
    }

    @Test
    public void testBuilder() throws Exception {
        builder.setHost("localhost2")
               .setMessage("hello builder message")
               .addAdditionalField("_foo", "bar")
               .addAdditionalField("baz", 123);

        assertEquals("localhost2", builder.build().getHost());
        assertEquals("hello builder message", builder.build().getMessage());
        assertEquals("bar", builder.build().getAdditionalFields().get("_foo"));
        assertEquals(123, builder.build().getAdditionalFields().get("_baz"));
    }

    @Test
    public void testReturnsNewMessageObjectEveryTime() throws Exception {
        assertNotSame(builder.build(), builder.build());
    }
}