/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.artipie.docker.asto;

import com.artipie.docker.Digest;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;

/**
 * Blob store digest subscription.
 * @since 1.0
 */
final class BlobDigestSub implements Flow.Subscriber<Byte> {

    /**
     * Default buffer size.
     */
    private static final int BUF_SIZE = 8192;

    /**
     * Digest future.
     */
    private final CompletableFuture<Digest> future;

    /**
     * Temporary buffer.
     */
    private final ByteBuffer buf;

    /**
     * Output channel.
     */
    private final FileChannel out;

    /**
     * Subscription list.
     */
    private final List<Subscription> subs;

    /**
     * Message digest to calculate SHA-256.
     */
    private final MessageDigest mdst;

    /**
     * Ctor.
     * @param future Digest future
     * @param out Channel to write
     */
    BlobDigestSub(final CompletableFuture<Digest> future, final FileChannel out) {
        this.out = out;
        this.future = future;
        this.buf = ByteBuffer.allocate(BlobDigestSub.BUF_SIZE);
        this.subs = new LinkedList<>();
        this.mdst = BlobDigestSub.sha256();
    }

    @Override
    public void onSubscribe(final Subscription subscription) {
        this.subs.add(subscription);
        subscription.request(this.buf.capacity());
    }

    @Override
    public void onNext(final Byte item) {
        this.buf.put(item);
        this.mdst.update(item);
        if (this.buf.position() == this.buf.capacity() - 1) {
            this.flush();
        }
    }

    @Override
    public void onError(final Throwable err) {
        this.future.completeExceptionally(err);
    }

    @Override
    public void onComplete() {
        this.flush();
        this.future.complete(new Digest.Sha256(BlobDigestSub.toHex(this.mdst.digest())));
    }

    /**
     * Flush the buffer.
     */
    private void flush() {
        this.buf.flip();
        while (this.buf.hasRemaining()) {
            try {
                this.out.write(this.buf);
            } catch (final IOException err) {
                this.future.completeExceptionally(err);
                this.subs.forEach(Subscription::cancel);
            }
        }
        this.buf.clear();
    }

    /**
     * Convert bytes to hex.
     * @param bytes Bytes
     * @return Hex string
     */
    private static String toHex(final byte[] bytes) {
        final StringBuilder str = new StringBuilder(bytes.length * 2);
        for (final byte item : bytes) {
            str.append(String.format("%02X ", item));
        }
        return str.toString();
    }

    /**
     * Get SHA-256 message digest instance.
     * @return Message digest of SHA-256
     * @checkstyle MethodNameCheck (5 lines)
     */
    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException err) {
            throw new IllegalStateException("This runtime doesn't have SHA-256 algorithm", err);
        }
    }
}
