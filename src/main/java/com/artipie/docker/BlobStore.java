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

package com.artipie.docker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * Docker registry blob store.
 * @since 0.1
 * @todo #6:30min Add put method which should put new layer data
 *  into blob-store, then compute it's digest using SHA256 and return
 *  digest as a result. See SPEC.md and docker registry API spec for
 *  more details.
 */
public interface BlobStore {

    /**
     * Load blob by digest.
     * @param digest Blob digest
     * @return Async publisher output
     */
    CompletableFuture<Flow.Publisher<Byte>> blob(Digest digest);

    /**
     * Put data into blob store and calculate its digest.
     * @param blob Data flow
     * @return Future with digest
     */
    CompletableFuture<Digest> put(Flow.Publisher<Byte> blob);
}

