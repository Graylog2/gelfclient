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

package org.graylog2.gelfclient.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * A Netty channel handler which splits large GELF messages into
 * <a href="http://graylog2.org/gelf#specs">chunked GELF</a> messages.
 */
@ChannelHandler.Sharable
public class GelfMessageChunkEncoder extends MessageToMessageEncoder<ByteBuf> {
    private static final Logger LOG = LoggerFactory.getLogger(GelfMessageChunkEncoder.class);
    private static final int MAX_CHUNKS = 128;
    private static final int MAX_CHUNK_SIZE = 1420;
    private static final int MAX_MESSAGE_SIZE = (MAX_CHUNKS * MAX_CHUNK_SIZE);
    private static final byte[] CHUNK_MAGIC_BYTES = new byte[]{0x1e, 0x0f};
    private final byte[] machineIdentifier = new byte[4];

    /**
     * {@inheritDoc}
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        LOG.error("Chunking error", cause);
    }

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
            // GELF message ID, max 8 bytes
            final ByteBuf messageId = Unpooled.buffer(8, 8);

            // 4 bytes of current time.
            messageId.writeInt((int) System.currentTimeMillis());
            messageId.writeBytes(machineIdentifier, 0, 4);

            return messageId.array();
        }
    }

    /**
     * Creates a new instance with a given machine identifier used in the generation of the message ID.
     * <p>Usually the hostname of the client makes a good enough machine identifier.</p>
     *
     * @param machineIdentifier the machine identifier (only the first 4 bytes are being used)
     */
    public GelfMessageChunkEncoder(final byte[] machineIdentifier) {
        if (machineIdentifier.length < 4) {
            throw new IllegalArgumentException("The machine identifier must at least be 4 bytes long.");
        }

        System.arraycopy(machineIdentifier, 0, this.machineIdentifier, 0, 4);
    }

    /**
     * Creates a new instance with a random machine identifier used in the generation of the message ID.
     */
    public GelfMessageChunkEncoder() {
        this(randomIdentifier(4));
    }

    private static byte[] randomIdentifier(final int length) {
        final byte[] randomIdentifier = new byte[length];
        final Random random = new Random();
        random.nextBytes(randomIdentifier);
        return randomIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        if (buf.readableBytes() > MAX_MESSAGE_SIZE) {
            throw new EncoderException("Message too big. " + buf.readableBytes() + " bytes (max " + MAX_MESSAGE_SIZE + ")");
        }

        if (buf.readableBytes() <= MAX_CHUNK_SIZE) {
            // Need to retain() the buffer here to avoid releasing the buffer too early.
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
