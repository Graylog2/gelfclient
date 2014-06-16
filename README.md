Graylog2 GELFClient
===================

## Usage

```java
public class Application {
    public static void main(String[] args) {
        GelfConfiguration config = new GelfConfiguration();

        config.setTransport(GelfTransports.TCP);
        config.setHost("127.0.0.1");
        config.setPort(12201);
        config.setReconnectDelay(1000);
        config.setQueueSize(512);

        GelfMessageBuilder builder = new GelfMessageBuilder(GelfMessageVersion.V1_1);
        GelfMessage messageTemplate = builder.addHost("localhost").addAdditionalField("_foo", "bar");

        GelfTransport transport = GelfTransports.create(config);

        for (int i = 0; i < 100; i++) {
            GelfMessage message = messageTemplate.build();

            message.addMessage("This is message #" + i);
            message.addAdditionalField("_count", i);

            transport.send(message);
        }

        transport.stop();
    }
}
```
