Graylog2 GELFClient
===================

```java
public class Application {
    public static void main(String[] args) {
        double timestamp = System.currentTimeMillis() / 1000D;

        GelfMessageBuilder builder = new GelfMessageBuilder(GelfMessageVersion.V1_1);
        GelfMessage message = builder.addHost("localhost")
                                     .addMessage("This will be the short_message.")
                                     .addAdditionalField("_foo", "bar")
                                     .build();

        Configuration config = new Configuration();

        config.setProtocol(GelfTransports.TCP);
        config.setHost("127.0.0.1");
        config.setPort(12201);
        config.setReconnectDelay(1000);

        GelfTransport transport = GelfTransports.create(config);
        GelfMessageValidator validator = GelfMessageValidator.create(message.getVersion());

        if (validator.isValid(message)) {
            transport.send(message);
        }

        transport.stop();
    }
}
```
