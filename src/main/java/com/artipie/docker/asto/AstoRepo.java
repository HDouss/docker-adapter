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

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.docker.Digest;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import com.artipie.docker.misc.BytesFlowAs;
import com.artipie.docker.ref.BlobRef;
import com.artipie.docker.ref.ManifestRef;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;

/**
 * Asto implementation of {@link Repo}.
 * @since 0.1
 */
public final class AstoRepo implements Repo {

    /**
     * Asto storage.
     */
    private final Storage asto;

    /**
     * Repository name.
     */
    private final RepoName name;

    /**
     * Ctor.
     * @param asto Asto storage
     * @param name Repository name
     */
    public AstoRepo(final Storage asto, final RepoName name) {
        this.asto = asto;
        this.name = name;
    }

    @Override
    public Digest layer(final String alg, final String digest) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Flow.Publisher<ByteBuffer> manifest(final ManifestRef link) {
        final Key key = new Key.From(
            RegistryRoot.V2, "repositories", this.name.value(),
            "_manifests", link.string()
        );
        return new AstoRepo.PubFromFuture<>(
            this.asto.value(key)
            .thenCompose(pub -> new BytesFlowAs.Text(pub).future())
            .thenApply(Digest.FromLink::new)
            .thenApply(digest -> new Key.From(new BlobRef(digest), "data"))
            .thenCompose(blob -> this.asto.value(new Key.From(RegistryRoot.V2, blob.string())))
        );
    }

    /**
     * Flow publisher from future.
     * @param <T> Publisher type
     * @since 1.0
     * @todo #57:30min Extract this class from AstoRepo.
     *  Maybe move it to separate library, since it's not related to
     *  artipie docker library.
     */
    private static final class PubFromFuture<T> implements Flow.Publisher<T> {

        /**
         * Async pubisher.
         */
        private final CompletionStage<Flow.Publisher<T>> source;

        /**
         * Ctor.
         * @param source Future of publisher
         */
        PubFromFuture(final CompletionStage<Flow.Publisher<T>> source) {
            this.source = source;
        }

        @Override
        public void subscribe(final Subscriber<? super T> sub) {
            this.source.thenAccept(pub -> pub.subscribe(sub));
        }
    }
}
