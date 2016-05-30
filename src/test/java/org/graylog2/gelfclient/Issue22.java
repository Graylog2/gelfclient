/*
 * Copyright 2016 TORCH GmbH
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

import org.graylog2.gelfclient.transport.GelfTransport;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.util.Date;

public class Issue22 {

    @Test(enabled = false)
    public void shouldNotOOM() throws InterruptedException {
        final GelfMessageBuilder builder = new GelfMessageBuilder("", "GELF-CLIENT-TEST").level(GelfMessageLevel.INFO);

        final GelfConfiguration config = new GelfConfiguration(new InetSocketAddress("localhost", 12202))
                .transport(GelfTransports.TCP)
                .tcpKeepAlive(true)
                .queueSize(512)
                .connectTimeout(5000)
                .reconnectDelay(5000)
                .tcpNoDelay(true)
                .sendBufferSize(1024);

        final GelfTransport transport = GelfTransports.create(config);

        for (int i = 0; i < 2_000_000; i++) {
            final GelfMessage message = builder.message(i + " Test " + new Date()).additionalField("foo", i + " foo " + i).build();
            transport.send(message);
//            if (i % 1_000 == 0) System.out.println(i +" - "+ new Date());
        }

        Thread.sleep(5 * 60_000);
    }

}
