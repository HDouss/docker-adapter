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

import com.artipie.asto.FileStorage;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import com.artipie.docker.ref.ManifestRef;
import java.nio.file.Path;
import javax.json.JsonObject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link AstoRepo}.
 * @since 1.0
 * @todo #29:30min This test is failing. Manifest resolution was fixed and it
 *  works correctly. But blob path resolution is wrong. It's trying to access
 *  blob from manifest link by wrong path. Fix it and enable the test.
 */
public final class AstoRepoITCase {

    @Test
    void readsManifestJson() throws Exception {
        final Path dir = Path.of(
            Thread.currentThread().getContextClassLoader()
                .getResource("docker").toURI()
        ).getParent();
        final Repo repo = new AstoRepo(new FileStorage(dir));
        final JsonObject json = repo.manifest(
            new RepoName.Simple("test"), new ManifestRef("1")
        ).get();
        MatcherAssert.assertThat(
            json.getJsonObject("config").getString("digest"),
            Matchers.is("sha256:e56378c5af5160fd8b7d8ad97a9c0aeef08ed31abcc431048c876602e1bdac4d")
        );
    }
}
