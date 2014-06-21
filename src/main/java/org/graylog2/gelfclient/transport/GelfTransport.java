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

package org.graylog2.gelfclient.transport;

import org.graylog2.gelfclient.GelfMessage;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
public interface GelfTransport {
    /**
     * Sends the given message to the remote host. This <strong>blocks</strong> until there is sufficient capacity to
     * process the message. It is not guaranteed that the message has been sent once the method call returns because
     * a queue might be used to dispatch the message.
     *
     * @param message message to send to the remote host
     * @throws InterruptedException
     */
    public void send(GelfMessage message) throws InterruptedException;

    /**
     * Tries to send the given message to the remote host. It does <strong>not block</strong> if there is not enough
     * capacity to process the message. It is not guaranteed that the message has been sent once the method call
     * returns because a queue might be used to dispatch the message.
     *
     * @param message message to send to the remote host
     * @return true if the message could be dispatched, false otherwise
     */
    public boolean trySend(GelfMessage message);

    /**
     * Stops the transport. Should be used to gracefully shutdown the backend.
     */
    public void stop();
}
