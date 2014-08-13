/**
 * A simple GELF client library with support for different transport mechanisms.
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
 */
package org.graylog2.gelfclient;