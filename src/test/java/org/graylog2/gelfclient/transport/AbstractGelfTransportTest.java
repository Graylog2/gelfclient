/*
 * Copyright 2018 Graylog, Inc.
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
package org.graylog2.gelfclient.transport;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.graylog2.gelfclient.GelfConfiguration;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class AbstractGelfTransportTest {

    @Test
    public void testThreads() throws Exception {
        final int threads = 23;
        final GelfConfiguration configuration = new GelfConfiguration().threads(threads);
        final AbstractGelfTransport transport = new AbstractGelfTransport(configuration) {
            @Override
            protected void createBootstrap(EventLoopGroup workerGroup) {
                final NioEventLoopGroup eventLoopGroup = (NioEventLoopGroup) workerGroup;
                assertEquals(threads, eventLoopGroup.executorCount());
            }

            @Override
            public void flushAndStop() {
                // Nothing do to here.
            }
        };
        transport.stop();
    }
}