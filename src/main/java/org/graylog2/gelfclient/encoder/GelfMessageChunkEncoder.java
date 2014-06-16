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

package org.graylog2.gelfclient.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.graylog2.gelfclient.GelfConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Bernd Ahlers <bernd@torch.sh>
 */
@ChannelHandler.Sharable
public class GelfMessageChunkEncoder extends MessageToMessageEncoder<ByteBuf> {
    private static final Logger LOG = LoggerFactory.getLogger(GelfMessageChunkEncoder.class);
    private static final int MAX_CHUNKS = 128;
    private static final int MAX_CHUNK_SIZE = 1420;
    private static final int MAX_MESSAGE_SIZE = (MAX_CHUNKS * MAX_CHUNK_SIZE);
    private static final byte[] CHUNK_MAGIC_BYTES = new byte[]{0x1e, 0x0f};

    private final GelfConfiguration config;

    private class Chunker {
        private final byte[] sequenceCount;
        private int sequenceNumber = 0;
        private final byte[] messageId = generateMessageId();

        public Chunker(final int messageSize) {
            int sequenceCount = (messageSize / MAX_CHUNK_SIZE);

            // Check if we have to add another chunk due to integer division.
            if ((messageSize % MAX_CHUNK_SIZE) != 0) {
                sequenceCount++;
            }

            this.sequenceCount = new byte[]{(byte) sequenceCount};
        }

        public ByteBuf nextChunk(final ByteBuf chunk) {
            final byte[] sequenceNumber = new byte[]{(byte) this.sequenceNumber++};
            final byte[] data = new byte[chunk.readableBytes()];

            chunk.readBytes(data);

            LOG.debug("nextChunk bytes magicBytes={} messageId={} sequenceNumber={} sequenceCount={} data={}",
                CHUNK_MAGIC_BYTES.length, messageId.length, sequenceNumber.length, sequenceCount.length, data.length
            );

            return Unpooled.copiedBuffer(CHUNK_MAGIC_BYTES, messageId, sequenceNumber, sequenceCount, data);
        }

        private byte[] generateMessageId() {
            final ByteBuf messageId = Unpooled.buffer(8);
            final ByteBuf hostname = Unpooled.wrappedBuffer(config.getHost().getBytes());

            messageId.writeInt((int) System.currentTimeMillis());

            try {
                byte[] h = new byte[4];

                if (hostname.readableBytes() > 4) {
                    hostname.slice(0, 4).readBytes(h);
                } else {
                    hostname.readBytes(h);
                }

                messageId.writeBytes(h);
            } finally {
                hostname.release();
            }

            byte[] messageBytes = new byte[messageId.readableBytes()];

            messageId.readBytes(messageBytes);
            messageId.release();

            return messageBytes;
        }
    }

    public GelfMessageChunkEncoder(final GelfConfiguration config) {
        this.config = config;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        if (buf.readableBytes() > MAX_MESSAGE_SIZE) {
            throw new EncoderException("Message too big. " + buf.readableBytes() + " bytes (max " + MAX_MESSAGE_SIZE + ")");
        }

        if (buf.readableBytes() <= MAX_CHUNK_SIZE) {
            // Need to retain() the buffer here to avoid a io.netty.util.IllegalReferenceCountException. ???
            out.add(buf.retain());
        } else {
            final Chunker chunker = new Chunker(buf.readableBytes());

            try {
                while (buf.readableBytes() > 0) {
                    if (buf.readableBytes() >= MAX_CHUNK_SIZE) {
                        out.add(chunker.nextChunk(buf.readSlice(MAX_CHUNK_SIZE)));
                    } else {
                        out.add(chunker.nextChunk(buf.readSlice(buf.readableBytes())));
                    }
                }
            } catch (Exception e) {
                LOG.error("Chunk encoder error", e);
                buf.release();
            }
        }
    }
}
