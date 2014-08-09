GELF Client
===========

[![Build Status](https://travis-ci.org/Graylog2/gelfclient.svg)](https://travis-ci.org/Graylog2/gelfclient)
[![Coverage Status](https://img.shields.io/coveralls/Graylog2/gelfclient.svg)](https://coveralls.io/r/Graylog2/gelfclient)

A Java GELF client library with support for different transports.

Available transports:

* TCP
* UDP

All default transport implementations use a queue to send messages in a
background thread to avoid blocking the calling thread until a message has
been sent. That means that the `send()` and `trySend()` methods do not
actually send the messages but add them to a queue where the background
thread will pick them up. This is important to keep in mind when it comes to
message delivery guarantees.

The library uses [Netty v4](http://netty.io/) to handle all network related
tasks and [Jackson](https://github.com/FasterXML/jackson) for JSON encoding.

## Usage

### Maven Dependency

```xml
<dependency>
  <groupId>org.graylog2</groupId>
  <artifactId>gelfclient</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Example

```java
public class Application {
    public static void main(String[] args) {
        final GelfConfiguration config = new GelfConfiguration();

        // Optional but recommended settings
        config.setHost("127.0.0.1");
        config.setPort(12201);
        config.setTransport(GelfTransports.TCP);

        // Optional settings
        config.setConnectTimeout(5000);
        config.setReconnectDelay(1000);
        config.setTcpNoDelay(true);
        config.setQueueSize(512);
        config.setSendBufferSize(32768);

        final GelfTransport transport = GelfTransports.create(config);

        boolean blocking = false;
        for (int i = 0; i < 100; i++) {
            final GelfMessage message = new GelfMessageBuilder("This is message #" + i, "localhost")
                    .level(GelfMessageLevel.INFORMATIONAL)
                    .additionalField("_foo", "bar")
                    .additionalField("_count", i)
                    .build();

            if (blocking) {
                // Blocks until there is capacity in the queue
                transport.send(message);
            } else {
                // Returns false if there isn't enough room in the queue
                boolean enqueued = transport.trySend(message);
            }
        }
    }
}
```

## Contributing

Please see [CONTRIBUTING](CONTRIBUTING.md) for details.

## License

Apache License, Version 2.0 -- http://www.apache.org/licenses/LICENSE-2.0
