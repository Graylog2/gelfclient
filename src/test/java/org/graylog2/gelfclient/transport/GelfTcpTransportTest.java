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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.util.Uninterruptibles;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class GelfTcpTransportTest {

    private EventLoopGroup eventLoopGroup;
    private Channel channel;
    private TcpRequestHandler requestHandler;

    @BeforeMethod
    public void setUp() throws Exception {
        eventLoopGroup = new NioEventLoopGroup();
        requestHandler = new TcpRequestHandler();
        final ServerBootstrap bootstrap = new ServerBootstrap()
                .group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new ChannelInitializer<ServerSocketChannel>() {
                    @Override
                    protected void initChannel(ServerSocketChannel ch) throws Exception {
                        ch.pipeline().addLast("logging", new LoggingHandler());
                    }
                })
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(requestHandler);
                    }
                });
        channel = bootstrap.bind(InetAddress.getLoopbackAddress(), 0).sync().channel();

    }

    @AfterMethod
    public void tearDown() throws Exception {
        channel.close();
        channel.closeFuture().syncUninterruptibly();
        eventLoopGroup.shutdownGracefully();
    }

    @Test
    public void gelfTcp() throws Exception {
        final GelfMessage message = new GelfMessage("Test", "example.org");
        final InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
        final GelfConfiguration configuration = new GelfConfiguration(localAddress)
                .transport(GelfTransports.TCP);
        final GelfTransport transport = GelfTransports.create(configuration);
        transport.send(message);

        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);

        assertFalse(requestHandler.getRequests().isEmpty());

        final byte[] payload = requestHandler.getRequests().get(0);
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode tree = objectMapper.readTree(payload);
        assertEquals(tree.path("version").asText(), "1.1");
        assertEquals(tree.path("host").asText(), "example.org");
        assertEquals(tree.path("short_message").asText(), "Test");

        transport.stop();
    }

    public static class TcpRequestHandler extends SimpleChannelInboundHandler<ByteBuf> {
        private final List<byte[]> requests = new ArrayList<>();

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            requests.add(ByteBufUtil.getBytes(msg));
        }

        public List<byte[]> getRequests() {
            return requests;
        }
    }
}