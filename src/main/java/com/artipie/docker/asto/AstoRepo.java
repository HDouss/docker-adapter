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
import java.util.concurrent.CompletableFuture;
import javax.json.JsonObject;

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
     * Ctor.
     * @param asto Asto storage
     */
    public AstoRepo(final Storage asto) {
        this.asto = asto;
    }

    @Override
    public Digest layer(final String alg, final String digest) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public CompletableFuture<JsonObject> manifest(final RepoName name, final ManifestRef link) {
        final Key key = new Key.From(
            RegistryRoot.V2, "repositories", name.value(),
            "_manifests", link.string()
        );
        return this.asto.value(key)
            .thenCompose(pub -> new BytesFlowAs.Text(pub).future())
            .thenApply(Digest.FromLink::new)
            .thenApply(digest -> new Key.From(new BlobRef(digest), "data"))
            .thenCompose(blob -> this.asto.value(new Key.From(RegistryRoot.V2, blob.string())))
            .thenCompose(pub -> new BytesFlowAs.JsonObject(pub).future());
    }
}
