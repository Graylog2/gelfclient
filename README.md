GELF Client
===========

[![Maven Central](https://img.shields.io/maven-central/v/org.graylog2/gelfclient.svg)](https://mvnrepository.com/artifact/org.graylog2/gelfclient)
[![Build](https://github.com/Graylog2/gelfclient/actions/workflows/build.yml/badge.svg)](https://github.com/Graylog2/gelfclient/actions/workflows/build.yml)

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
  <version>1.5.1</version>
</dependency>
```

### Example

```java
public class Application {
    public static void main(String[] args) {
        final GelfConfiguration config = new GelfConfiguration(new InetSocketAddress("example.com", 12201))
              .transport(GelfTransports.UDP)
              .queueSize(512)
              .connectTimeout(5000)
              .reconnectDelay(1000)
              .tcpNoDelay(true)
              .sendBufferSize(32768);

        final GelfTransport transport = GelfTransports.create(config);
        final GelfMessageBuilder builder = new GelfMessageBuilder("", "example.com")
                .level(GelfMessageLevel.INFO)
                .additionalField("_foo", "bar");

        boolean blocking = false;
        for (int i = 0; i < 100; i++) {
            final GelfMessage message = builder.message("This is message #" + i)
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
