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

import com.artipie.asto.fs.FileStorage;
import com.artipie.docker.BlobStore;
import com.artipie.docker.Digest;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.reactivestreams.FlowAdapters;

/**
 * Integration test for {@link AstoBlobs}.
 * @since 0.1
 * @todo #43:30min Implement more integration tests for AstoBlobs,
 *  we should check negative cases when put() method fails, e.g. if
 *  failed to write a file on IOException.
 */
final class AstoBlobsITCase {
    @Test
    void saveBlobDataAtCorrectPath(@TempDir final Path tmp) throws Exception {
        final BlobStore blobs = new AstoBlobs(new FileStorage(tmp));
        final ByteBuffer buf = ByteBuffer.wrap(new byte[]{0x00, 0x01, 0x02, 0x03});
        final Digest digest = blobs.put(
            FlowAdapters.toFlowPublisher(Flowable.fromArray(buf))
        ).get();
        MatcherAssert.assertThat(
            "Digest alg is not correct",
            digest.alg(), Matchers.equalTo("sha256")
        );
        final String hash = "054edec1d0211f624fed0cbca9d4f9400b0e491c43742af2c5b0abebf0c990d8";
        MatcherAssert.assertThat(
            "Digest sum is not correct",
            digest.digest(),
            Matchers.equalTo(hash)
        );
        MatcherAssert.assertThat(
            "File content is not correct",
            Files.readAllBytes(
                tmp.resolve("docker/registry/v2/blobs/sha256/05").resolve(hash).resolve("data")
            ),
            Matchers.equalTo(buf.array())
        );
    }

    @Test
    void writeAndReadBlob(@TempDir final Path tmp) throws Exception {
        final BlobStore blobs = new AstoBlobs(new FileStorage(tmp));
        final ByteBuffer buf = ByteBuffer.wrap(new byte[]{0x05, 0x06, 0x07, 0x08});
        final Digest digest = blobs.put(
            FlowAdapters.toFlowPublisher(Flowable.fromArray(buf))
        ).get();
        final byte[] read = Flowable.fromPublisher(
            FlowAdapters.toPublisher(blobs.blob(digest).get())
        ).toList().blockingGet().get(0).array();
        MatcherAssert.assertThat(read, Matchers.equalTo(buf.array()));
    }
}
