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

/**
 * The configuration used by a {@link org.graylog2.gelfclient.transport.GelfTransport}.
 */
public class GelfConfiguration {
    private String host = "127.0.0.1";
    private int port = 12201;
    private int queueSize = 512;
    private GelfTransports transport = GelfTransports.TCP;
    private int reconnectDelay = 500;
    private int connectTimeout = 1000;
    private boolean tcpNoDelay = false;
    private int sendBufferSize = -1;

    /**
     * Get the hostname or IP address of the GELF server (e. g. Graylog2).
     *
     * @return the hostname or IP address of the GELF server
     */
    public String getHost() {
        return host;
    }

    /**
     * Set the hostname or IP address of the GELF server (e. g. Graylog2).
     *
     * @param host the hostname or IP address of the GELF server
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Get the port of the GELF server.
     *
     * @return the port of the GELF server
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the port of the GELF server.
     *
     * @param port the port of the GELF server
     */
    public void setPort(int port) {
        this.port = port;
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
     */
    public void setTransport(GelfTransports transport) {
        this.transport = transport;
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
     */
    public void setQueueSize(int size) {
        queueSize = size;
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
     */
    public void setReconnectDelay(int reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
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
     * @see io.netty.channel.ChannelOption#CONNECT_TIMEOUT_MILLIS
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
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
     * @see io.netty.channel.ChannelOption#TCP_NODELAY
     */
    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
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
     * @see io.netty.channel.ChannelOption#SO_SNDBUF
     */
    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }
}
