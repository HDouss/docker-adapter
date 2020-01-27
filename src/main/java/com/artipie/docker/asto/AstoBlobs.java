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
import com.artipie.docker.Digest;
import com.artipie.docker.ref.BlobRef;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * Asto {@link BlobStore} implementation.
 * @since 1.0
 * @todo #11:30min Implement put() method.
 *  It should store all data somewhere temporary and calcualte the digest,
 *  then compute blob path from digest and save it by correct blob path
 *  using ASTO storage.
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
}
