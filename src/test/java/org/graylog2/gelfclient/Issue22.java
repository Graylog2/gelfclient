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
