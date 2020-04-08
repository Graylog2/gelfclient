package org.graylog2.gelfclient.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

public class GelfHttpEncoder extends MessageToMessageEncoder<ByteBuf> {
    private static final Logger LOG = LoggerFactory.getLogger(GelfHttpEncoder.class);

    private final URI uri;

    public GelfHttpEncoder(URI uri) {
        this.uri = uri;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf msg, List<Object> list) throws Exception {
        final FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST, uri.getRawPath(), msg.retain());
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, msg.readableBytes());
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

        list.add(request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("Error while encoding HTTP request", cause);
        ctx.close();
    }
}
