/*
 * Copyright 2018 Graylog, Inc.
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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.encoder.GelfHttpEncoder;
import org.graylog2.gelfclient.encoder.GelfMessageJsonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link GelfTransport} implementation that uses HTTP(S) to send GELF messages.
 * <p>This class is thread-safe.</p>
 */
public class GelfHttpTransport extends AbstractGelfTransport {
    private static final Logger LOG = LoggerFactory.getLogger(GelfHttpTransport.class);

    /**
     * Creates a new TCP GELF transport.
     *
     * @param config the GELF client configuration
     */
    public GelfHttpTransport(GelfConfiguration config) {
        super(config);
    }

    @Override
    protected void createBootstrap(final EventLoopGroup workerGroup) {
        final Bootstrap bootstrap = new Bootstrap();
        final GelfSenderThread senderThread = new GelfSenderThread(queue, config.getMaxInflightSends());
        senderThreadReference.set(senderThread);

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout())
                .option(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
                .option(ChannelOption.SO_KEEPALIVE, config.isTcpKeepAlive())
                .remoteAddress(config.getRemoteAddress())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        if (config.isTlsEnabled()) {
                            LOG.debug("TLS enabled.");
                            final SslContext sslContext;

                            if (!config.isTlsCertVerificationEnabled()) {
                                // If the cert should not be verified just use an insecure trust manager.
                                LOG.debug("TLS certificate verification disabled!");
                                sslContext = SslContextBuilder.forClient()
                                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                        .build();
                            } else if (config.getTlsTrustCertChainFile() != null) {
                                // If a cert chain file is set, use it.
                                LOG.debug("TLS certificate chain file: {}", config.getTlsTrustCertChainFile());
                                sslContext = SslContextBuilder.forClient()
                                        .trustManager(config.getTlsTrustCertChainFile())
                                        .build();
                            } else {
                                // Otherwise use the JVM default cert chain.
                                sslContext = SslContextBuilder.forClient().build();
                            }

                            ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
                        }

                        ch.pipeline().addLast(new HttpClientCodec());
                        ch.pipeline().addLast(new HttpContentDecompressor());
                        ch.pipeline().addLast(new GelfHttpEncoder(config.getUri()));
                        ch.pipeline().addLast(new GelfMessageJsonEncoder());
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                senderThread.start(ctx.channel());
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                LOG.info("Channel disconnected!");
                                senderThread.stop();
                                scheduleReconnect(ctx.channel().eventLoop());
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                LOG.error("Exception caught", cause);
                            }
                        });
                    }
                });

        if (config.getSendBufferSize() != -1) {
            bootstrap.option(ChannelOption.SO_SNDBUF, config.getSendBufferSize());
        }

        bootstrap.connect().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    LOG.debug("Connected!");
                } else {
                    LOG.error("Connection failed: {}", future.cause().getMessage());
                    scheduleReconnect(future.channel().eventLoop());
                }
            }
        });
    }
}
