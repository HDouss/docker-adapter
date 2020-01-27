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

import com.artipie.asto.Storage;
import com.artipie.docker.BlobStore;
import com.artipie.docker.Digest;
import com.artipie.docker.ref.BlobRef;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * Asto {@link BlobStore} implementation.
 * @since 1.0
 */
public final class AstoBlobs implements BlobStore {

    /**
     * Storage.
     */
    private final Storage asto;

    /**
     * Ctor.
     * @param asto Storage
     */
    public AstoBlobs(final Storage asto) {
        this.asto = asto;
    }

    @Override
    public CompletableFuture<Flow.Publisher<Byte>> blob(final Digest digest) {
        return this.asto.value(new BlobRef(digest));
    }

    @Override
    public CompletableFuture<Digest> put(final Flow.Publisher<Byte> blob) {
        final CompletableFuture<Digest> future = new CompletableFuture<>();
        try {
            final FileChannel out = FileChannel.open(
                Files.createTempFile(this.getClass().getSimpleName(), ".blob.tmp"),
                StandardOpenOption.WRITE
            );
            blob.subscribe(new BlobDigestSub(future, out));
            future.whenComplete(
                (dgst, err) -> {
                    try {
                        out.close();
                    } catch (final IOException iox) {
                        Logger.warn(this, "failed to close blob output: %s", iox);
                    }
                }
            );
        } catch (final IOException err) {
            future.completeExceptionally(err);
        }
        return future;
    }
}
