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

import com.artipie.asto.ByteArray;
import com.artipie.asto.FileStorage;
import com.artipie.docker.Digest;
import com.artipie.docker.asto.AstoBlobs;
import io.reactivex.rxjava3.core.Flowable;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;

/**
 * Test case for {@link AstoBlobs}.
 * @since 0.1
 */
final class AstoBlobsITCase {
    @Test
    void saveBlobDataAtCorrectPath() throws Exception {
        final Path tmp = Files.createTempDirectory(this.getClass().getSimpleName());
        final ByteArray target = new ByteArray(new byte[]{0x00, 0x01, 0x02, 0x03});
        final Digest digest = new AstoBlobs(new FileStorage(tmp)).put(
            FlowAdapters.toFlowPublisher(Flowable.fromArray(target.boxedBytes()))
        ).get();
        MatcherAssert.assertThat(
            "digest alg is not correct",
            digest.alg(), Matchers.equalTo("sha256")
        );
        final String hash = "054edec1d0211f624fed0cbca9d4f9400b0e491c43742af2c5b0abebf0c990d8";
        MatcherAssert.assertThat(
            "digest sum is not correct",
            digest.digest(),
            Matchers.equalTo(hash)
        );
        MatcherAssert.assertThat(
            "file content is not correct",
                Files.readAllBytes(
                    tmp.resolve("docker/registry/v2/blobs/sha256/05").resolve(hash).resolve("data")
            ),
            Matchers.equalTo(target.primitiveBytes())
        );
    }
}
