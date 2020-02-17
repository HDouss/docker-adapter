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

import com.artipie.asto.Remaining;
import com.artipie.asto.fs.FileStorage;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import com.artipie.docker.ref.ManifestRef;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;

/**
 * Integration tests for {@link AstoRepo}.
 * @since 0.1
 */
final class AstoRepoITCase {
    @Test
    void readsManifestJson() throws Exception {
        final Path dir = Path.of(
            Thread.currentThread().getContextClassLoader()
                .getResource("docker").toURI()
        ).getParent();
        final Repo repo = new AstoRepo(new FileStorage(dir), new RepoName.Simple("test"));
        final byte[] content = new Remaining(
            Flowable.fromPublisher(FlowAdapters.toPublisher(repo.manifest(new ManifestRef("1"))))
                .toList()
                .blockingGet()
                .stream()
                .reduce(
                    (left, right) -> ByteBuffer.allocate(left.remaining() + right.remaining())
                        .put(left)
                        .put(right)
                ).orElse(ByteBuffer.allocate(0))
        ).bytes();
        // @checkstyle MagicNumberCheck (1 line)
        MatcherAssert.assertThat(content.length, Matchers.equalTo(942));
    }
}
