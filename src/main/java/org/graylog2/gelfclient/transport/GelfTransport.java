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

package org.graylog2.gelfclient.transport;

import org.graylog2.gelfclient.GelfMessage;

import java.util.concurrent.TimeUnit;

/**
 * A common interface for all GELF network transports.
 */
public interface GelfTransport {
    /**
     * Sends the given message to the remote host. This <strong>blocks</strong> until there is sufficient capacity to
     * process the message. It is not guaranteed that the message has been sent once the method call returns because
     * a queue might be used to dispatch the message.
     *
     * @param message message to send to the remote host
     */
    void send(GelfMessage message) throws InterruptedException;

    /**
     * Tries to send the given message to the remote host. It does <strong>not block</strong> if there is not enough
     * capacity to process the message. It is not guaranteed that the message has been sent once the method call
     * returns because a queue might be used to dispatch the message.
     *
     * @param message message to send to the remote host
     * @return true if the message could be dispatched, false otherwise
     */
    boolean trySend(GelfMessage message);

    /**
     * Stops the transport. Can be used to gracefully shutdown the backend.
     */
    void stop();

    /**
     * Blocks and stops the transport after flushing/sending all enqueued messages.
     * Blocking occurs until either all messages are flushed, or the indicated {@code waitDuration}, {@code timeUnit}
     * and {@code retries} have elapsed.
     *
     * Each retry waits for the indicated {@code waitDuration} and {@code timeUnit} again.
     *
     * This can be used to gracefully shutdown the backend.
     *
     * @param waitDuration the wait duration.
     * @param timeUnit the time unit for the {@code waitDuration}.
     * @param retries the number of times to retry and wait for messages to flush. Zero retries indicates that
     *                one initial attempt will be made.
     */
    void flushAndStopSynchronously(int waitDuration, TimeUnit timeUnit, int retries);
}
