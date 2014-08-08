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

package gelfclient.play;

import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageVersion;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

import java.util.Random;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class Play {
    public static void main(String... args) throws InterruptedException {
        final GelfConfiguration config = new GelfConfiguration();

        config.setHost("127.0.0.1");
        //config.setPort(12203);
        //config.setTransport(GelfTransports.TCP);
        config.setPort(12201);
        config.setTransport(GelfTransports.UDP);
        config.setReconnectDelay(5000);
        config.setQueueSize(1024);
        //config.setSendBufferSize(32768);

        GelfTransport transport = GelfTransports.create(config);

        int count = 0;

        String largeMessage = largeMessage();

        while (true) {
            GelfMessage msg = new GelfMessage("Hello world! " + count + " " + config.getTransport().toString());

            count++;

            msg.addAdditionalField("_count", count);
            msg.addAdditionalField("_oink", 1.231);
            msg.addAdditionalField("_objecttest", new Object());

            transport.send(msg);
            Thread.sleep(5000);
        }
    }

    private static String largeMessage() {
        Random r = new Random();
        String largeMessage = "";

        for (int i = 0; i < 1500; i++) {
            largeMessage += r.nextInt();
        }

        return largeMessage;
    }
}
