/**
 * <p>
 *     A simple GELF client library with support for different transport mechanisms.
 * </p>
 * <p>
 *     Currently available transports are
 *     <ul>
 *         <li>UDP</li>
 *         <li>TCP</li>
 *     </ul>
 * </p>
 * <p>
 *     All default transport implementations use a queue (backed by a {@link java.util.concurrent.BlockingQueue})
 *     to send messages in a background thread to avoid blocking the calling thread until a message has
 *     been sent. That means that the {@link org.graylog2.gelfclient.transport.GelfTransport#send(GelfMessage)}
 *     and {@link org.graylog2.gelfclient.transport.GelfTransport#trySend(GelfMessage)} methods do not
 *     actually send the messages but add them to a queue where the background thread will pick them up.
 *     This is important to keep in mind when it comes to message delivery guarantees.
 * </p>
 * <p>
 *     <h1>Example</h1>
 * </p>
 * <pre><code>
 *    final GelfConfiguration config = new GelfConfiguration(new InetSocketAddress("example.com", 12201))
 *          .transport(GelfTransports.TCP)
 *          .queueSize(512)
 *          .connectTimeout(5000)
 *          .reconnectDelay(1000)
 *          .tcpNoDelay(true)
 *          .sendBufferSize(32768);
 *
 *    final GelfTransport transport = GelfTransports.create(config);
 *
 *    boolean blocking = false;
 *    for (int i = 0; i < 100; i++) {
 *        final GelfMessage message = new GelfMessageBuilder("This is message #" + i, "localhost")
 *              .level(GelfMessageLevel.INFORMATIONAL)
 *              .additionalField("_foo", "bar")
 *              .additionalField("_count", i)
 *              .build();
 *        if (blocking) {
 *            // Blocks until there is capacity in the queue
 *            transport.send(message);
 *        } else {
 *            // Returns false if there isn't enough room in the queue
 *            boolean enqueued = transport.trySend(message);
 *        }
 *    }
 * </code></pre>
 */
package org.graylog2.gelfclient;