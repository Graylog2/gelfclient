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

import java.io.File;
import java.net.InetSocketAddress;

/**
 * The configuration used by a {@link org.graylog2.gelfclient.transport.GelfTransport}.
 */
public class GelfConfiguration {
    private static final int DEFAULT_PORT = 12201;
    private static final String DEFAULT_HOSTNAME = "127.0.0.1";
    private final String hostname;
    private final int port;
    private GelfTransports transport = GelfTransports.TCP;
    private Compression compression = Compression.GZIP;
    private int queueSize = 512;
    private boolean tlsEnabled = false;
    private File tlsTrustCertChainFile = null;
    private boolean tlsCertVerificationEnabled = true;
    private boolean tlsClientCertVerificationEnabled = true;
    private File tlsKeyCertChainFile = null;
    private File tlsKeyFile = null;
    private String tlsKeyPassword = null;
    private int reconnectDelay = 500;
    private int connectTimeout = 1000;
    private boolean tcpNoDelay = false;
    private boolean tcpKeepAlive = false;
    private int sendBufferSize = -1;
    private int maxInflightSends = 512;
    private int threads = 0;

    /**
     * Creates a new configuration with the given hostname and port.
     *
     * @param hostname The hostname of the GELF-enabled server
     * @param port The port of the GELF-enabled server
     */
    public GelfConfiguration(final String hostname, final int port) {
        this.hostname = checkHostname(hostname);
        this.port = checkPort(port);
    }

    /**
     * Creates a new configuration with the given remote address.
     *
     * @param remoteAddress The {@link java.net.InetSocketAddress} of the GELF server
     */
    public GelfConfiguration(final InetSocketAddress remoteAddress) {
        this(remoteAddress.getHostString(), remoteAddress.getPort());
    }

    /**
     * Creates a new configuration with the given hostname and the default port (12201).
     *
     * @param hostname The hostname of the GELF-enabled server
     */
    public GelfConfiguration(final String hostname) {
        this(hostname, DEFAULT_PORT);
    }

    /**
     * Creates a new configuration with the local hostname ("127.0.0.1") and the given port.
     *
     * @param port The port of the GELF-enabled server
     */
    public GelfConfiguration(final int port) {
        this(DEFAULT_HOSTNAME, port);
    }

    /**
     * Creates a new configuration with the local hostname ("127.0.0.1") and the default port (12201).
     */
    public GelfConfiguration() {
        this(DEFAULT_HOSTNAME, DEFAULT_PORT);
    }

    /**
     * Get the hostname of the GELF server.
     *
     * @return the hostname of the GELF server.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Get the port of the GELF server.
     *
     * @return the port of the GELF server.
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the remote address of the GELF server.
     *
     * @return the remote address of the GELF server.
     */
    public InetSocketAddress getRemoteAddress() {
        // Always create a new InetSocketAddress to ensure that the hostname is resolved to an ip address again.
        return new InetSocketAddress(hostname, port);
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
     * Get the compression algorithm used for GELF UDP.
     *
     * @return the compression algorithm used for GELF UDP
     */
    public Compression getCompression() {
        return compression;
    }

    /**
     * Set the compression algorithm used for GELF UDP.
     *
     * @param compression the compression algorithm used for GELF UDP
     * @return {@code this} instance
     */
    public GelfConfiguration compression(final Compression compression) {
        this.compression = compression;
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
     * Check if TLS option for the transport is enabled.
     *
     * @return {@code true} if TLS is enabled, {@code false} if disabled
     */
    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    /**
     * Enable TLS for transport.
     *
     * @return {@code this} instance
     */
    public GelfConfiguration enableTls() {
        this.tlsEnabled = true;
        return this;
    }

    /**
     * Disable TLS for transport.
     *
     * @return {@code this} instance
     */
    public GelfConfiguration disableTls() {
        this.tlsEnabled = false;
        return this;
    }

    /**
     * Get the trust certificate chain file for the TLS connection.
     *
     * @return the trust certificate chain file
     */
    public File getTlsTrustCertChainFile() {
        return tlsTrustCertChainFile;
    }

    /**
     * Set the trust certificate chain file for the TLS connection.
     *
     * @param tlsTrustCertChainFile the trust certificate chain file
     * @return {@code this} instance
     */
    public GelfConfiguration tlsTrustCertChainFile(final File tlsTrustCertChainFile) {
        this.tlsTrustCertChainFile = tlsTrustCertChainFile;
        return this;
    }

    /**
     * Check if TLS certificate verification is enabled.
     *
     * @return {@code true} if enabled, {@code false} if disabled
     */
    public boolean isTlsCertVerificationEnabled() {
        return tlsCertVerificationEnabled;
    }

    /**
     * Enable TLS certificate verification for transport.
     *
     * @return {@code this} instance
     */
    public GelfConfiguration enableTlsCertVerification() {
        this.tlsCertVerificationEnabled = true;
        return this;
    }

    /**
     * Disable TLS certificate verification for transport.
     *
     * @return {@code this} instance
     */
    public GelfConfiguration disableTlsCertVerification() {
        this.tlsCertVerificationEnabled = false;
        return this;
    }

    /**
     * Get the X.509 certificate chain file in PEM format for the server TLS connection.
     *
     * @return the trust certificate chain file
     */
    public File getTlsKeyCertChainFile() {
        return tlsKeyCertChainFile;
    }

    /**
     * Set the X.509 certificate chain file in PEM format for the server TLS connection.
     *
     * @param tlsTrustCertChainFile the trust certificate chain file
     * @return {@code this} instance
     */
    public GelfConfiguration tlsKeyCertChainFile(final File tlsKeyCertChainFile) {
        this.tlsKeyCertChainFile = tlsKeyCertChainFile;
        return this;
    }

    /**
     * Get the  PKCS#8 private key file in PEM format for the server TLS connection.
     *
     * @return the trust certificate chain file
     */
    public File getTlsKeyFile() {
        return tlsKeyFile;
    }

    /**
     * Set thePKCS#8 private key file in PEM format  for the server TLS connection.
     *
     * @param tlsTrustCertChainFile the trust certificate chain file
     * @return {@code this} instance
     */
    public GelfConfiguration tlsKeyFile(final File tlsKeyFile) {
        this.tlsKeyFile = tlsKeyFile;
        return this;
    }
    /**
     * Get the password of the tlsKeyFile 
     *
     * @return  the password of the tlsKeyFile .
     */
    public String getTlsKeyPassword() {
        return tlsKeyPassword;
    }

    /**
     * Set the password of the tlsKeyFile, or null if it's not password-protected
     *
     * @return {@code this} instance
     */
    public GelfConfiguration tlsKeyPassword(final String tlsKeyPassword) {
        this.tlsKeyPassword = tlsKeyPassword;
        return this;
    }
    
    
    /**
     * Check if client TLS certificate verification is enabled.
     *
     * @return {@code true} if enabled, {@code false} if disabled
     */
    public boolean isTlsClientCertVerificationEnabled() {
        return tlsClientCertVerificationEnabled;
    }

    /**
     * Enable client TLS certificate verification for transport.
     *
     * @return {@code this} instance
     */
    public GelfConfiguration enableTlsClientCertVerification() {
        this.tlsClientCertVerificationEnabled = true;
        return this;
    }

    /**
     * Disable client TLS certificate verification for transport.
     *
     * @return {@code this} instance
     */
    public GelfConfiguration disableTlsClientCertVerification() {
        this.tlsClientCertVerificationEnabled = false;
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

    public boolean isTcpKeepAlive() {
        return tcpKeepAlive;
    }

    public GelfConfiguration tcpKeepAlive(final boolean tcpKeepAlive) {
        this.tcpKeepAlive = tcpKeepAlive;
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
     * Set the size of the socket send buffer in bytes.
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

    /**
     * Get number of worker threads.
     * @return number of worker threads.
     */
    public int getThreads() {
        return threads;
    }

    /**
     * Set number of worker threads that will be processing gelf messages.
     * @param threads number of worker threads.
     *                A value of {@code 0} sets number of threads to default equal to number of processors * 2.
     *                For details see: {@link org.graylog2.gelfclient.transport.AbstractGelfTransport}
     * @return {@code this} instance
     */
    public GelfConfiguration threads(int threads) {
        this.threads = threads;
        return this;
    }

    private String checkHostname(final String hostname) {
        if (hostname == null) {
            throw new IllegalArgumentException("hostname can't be null");
        }
        if (hostname.trim().isEmpty()) {
            throw new IllegalArgumentException("hostname can't be empty");
        }
        return hostname;
    }

    private int checkPort(final int port) {
        // While 0 is a valid port number, it doesn't make sense here.
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port out of range: " + port);
        }

        return port;
    }

    /**
     * Get the number of max queued network operations.
     *
     * @return max number of queued network operations
     * @since 1.4.0
     */
    public int getMaxInflightSends() {
        return maxInflightSends;
    }

    /**
     * Set the number of max queued network operations.
     *
     * @param maxInflightSends max number of queued network operations
     * @return {@code this} instance
     * @since 1.4.0
     */
    public GelfConfiguration maxInflightSends(int maxInflightSends) {
        this.maxInflightSends = maxInflightSends;
        return this;
    }
}
