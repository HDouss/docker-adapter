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

package com.artipie.docker.storage;

import com.artipie.asto.Storage;
import com.artipie.docker.Digest;
import com.artipie.docker.Repo;
import com.artipie.docker.manifest.ManifestRef;
import com.artipie.docker.misc.BytesFlowAs;
import java.nio.file.Path;
import java.util.concurrent.CompletionStage;
import javax.json.JsonObject;

/**
 * Asto implementation of {@link Repo}.
 * @since 1.0
 */
public final class AstoRepo implements Repo {

    /**
     * Base repos path.
     * <p>
     * It should be decoupled from {@link java.nio.Path} with #18 ticket.
     * </p>
     */
    private static final Path REPO_BASE = Path.of("docker/registry/v2/repositories");

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
    public CompletionStage<JsonObject> manifest(final ManifestRef link) {
        return this.asto.value(AstoRepo.REPO_BASE.resolve(link.path().toASCIIString()).toString())
            .thenCompose(pub -> new BytesFlowAs.Text(pub).future())
            .thenApply(text -> new Digest.FromLink(text))
            .thenApply(digest -> new BlobPath(digest))
            .thenCompose(path -> this.asto.value(path.toString()))
            .thenCompose(pub -> new BytesFlowAs.JsonObject(pub).future());
    }
}
