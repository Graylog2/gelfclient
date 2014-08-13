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

package org.graylog2.gelfclient;

import java.net.InetSocketAddress;

/**
 * The configuration used by a {@link org.graylog2.gelfclient.transport.GelfTransport}.
 */
public class GelfConfiguration {
    private static final int DEFAULT_PORT = 12201;
    private static final String DEFAULT_HOSTNAME = "127.0.0.1";
    private final InetSocketAddress remoteAddress;
    private GelfTransports transport = GelfTransports.TCP;
    private int queueSize = 512;
    private int reconnectDelay = 500;
    private int connectTimeout = 1000;
    private boolean tcpNoDelay = false;
    private int sendBufferSize = -1;

    /**
     * Creates a new configuration with the given remote address.
     *
     * @param remoteAddress The {@link java.net.InetSocketAddress} of the GELF server
     */
    public GelfConfiguration(final InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    /**
     * Creates a new configuration with the given hostname and the default port (12201).
     *
     * @param hostname The hostname of the GELF-enabled server
     */
    public GelfConfiguration(final String hostname) {
        this(new InetSocketAddress(hostname, DEFAULT_PORT));
    }

    /**
     * Creates a new configuration with the local hostname ("127.0.0.1") and the given port.
     *
     * @param port The port of the GELF-enabled server
     */
    public GelfConfiguration(final int port) {
        this(new InetSocketAddress(DEFAULT_HOSTNAME, port));
    }

    /**
     * Creates a new configuration with the local hostname ("127.0.0.1") and the default port (12201).
     */
    public GelfConfiguration() {
        this(new InetSocketAddress(DEFAULT_HOSTNAME, DEFAULT_PORT));
    }

    /**
     * Get the remote address of the GELF server.
     *
     * @return the remote address of the GELF server.
     */
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Get the transport protocol used with the GELF server.
     *
     * @return the transport protocol used with the GELF server
     */
    public GelfTransports getTransport() {
        return transport;
    }

    /**
     * Set the transport protocol used with the GELF server.
     *
     * @param transport the transport protocol used with the GELF server
     * @return {@code this} instance
     */
    public GelfConfiguration transport(final GelfTransports transport) {
        this.transport = transport;
        return this;
    }

    /**
     * Get the size of the internally used {@link java.util.concurrent.BlockingQueue}.
     *
     * @return the size of the internally used queue
     */
    public int getQueueSize() {
        return queueSize;
    }

    /**
     * Set the size of the internally used {@link java.util.concurrent.BlockingQueue}.
     *
     * @param size the size of the internally used queue
     * @return {@code this} instance
     */
    public GelfConfiguration queueSize(final int size) {
        queueSize = size;
        return this;
    }

    /**
     * Get the time to wait between reconnects in milliseconds.
     *
     * @return the time to wait between reconnects in milliseconds
     */
    public int getReconnectDelay() {
        return reconnectDelay;
    }

    /**
     * Set the time to wait between reconnects in milliseconds.
     *
     * @param reconnectDelay the time to wait between reconnects in milliseconds
     * @return {@code this} instance
     */
    public GelfConfiguration reconnectDelay(final int reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
        return this;
    }

    /**
     * Get the connection timeout for TCP connections in milliseconds.
     *
     * @return the connection timeout for TCP connections in milliseconds
     * @see io.netty.channel.ChannelOption#CONNECT_TIMEOUT_MILLIS
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Set the connection timeout for TCP connections in milliseconds.
     *
     * @param connectTimeout the connection timeout for TCP connections in milliseconds
     * @return {@code this} instance
     * @see io.netty.channel.ChannelOption#CONNECT_TIMEOUT_MILLIS
     */
    public GelfConfiguration connectTimeout(final int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Whether <a href="https://en.wikipedia.org/wiki/Nagle's_algorithm">Nagle's algorithm</a> is enabled for TCP connections.
     *
     * @return {@code true} if Nagle's algorithm is being used for TCP connections
     * @see io.netty.channel.ChannelOption#TCP_NODELAY
     */
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * Whether to use <a href="https://en.wikipedia.org/wiki/Nagle's_algorithm">Nagle's algorithm</a> for TCP connections.
     *
     * @param tcpNoDelay {@code true} if Nagle's algorithm should used for TCP connections, {@code false} otherwise
     * @return {@code this} instance
     * @see io.netty.channel.ChannelOption#TCP_NODELAY
     */
    public GelfConfiguration tcpNoDelay(final boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }

    /**
     * Get the size of the socket send buffer in bytes.
     *
     * @return the size of the socket send buffer in bytes.
     * @see io.netty.channel.ChannelOption#SO_SNDBUF
     */
    public int getSendBufferSize() {
        return sendBufferSize;
    }

    /**
     * Get the size of the socket send buffer in bytes.
     *
     * @param sendBufferSize the size of the socket send buffer in bytes.
     *                       A value of {@code -1} deactivates the socket send buffer.
     * @return {@code this} instance
     * @see io.netty.channel.ChannelOption#SO_SNDBUF
     */
    public GelfConfiguration sendBufferSize(final int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
        return this;
    }
}
