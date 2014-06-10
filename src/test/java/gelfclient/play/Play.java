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

package gelfclient.play;

import org.graylog2.gelfclient.Configuration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageVersion;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public class Play {
    public static void main(String... args) throws InterruptedException {
        final Configuration config = new Configuration();

        config.setHost("127.0.0.1");
        config.setPort(12203);
        config.setReconnectDelay(5000);
        config.setQueueSize(1024);

        GelfTransport transport = GelfTransports.create(config);

        int count = 0;

        while (true) {
            GelfMessage msg = new GelfMessage(GelfMessageVersion.V1_1);

            count++;

            msg.setMessage("Hello world! " + count);
            msg.addAdditionalField("_count", count);
            msg.addAdditionalField("_oink", 1.231);
            msg.addAdditionalField("_objecttest", new Object());

            transport.send(msg);
            Thread.sleep(5000);
        }
    }
}
