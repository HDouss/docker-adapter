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

import com.artipie.asto.ByteArray;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.docker.BlobStore;
import com.artipie.docker.Digest;
import com.artipie.docker.ref.BlobRef;
import hu.akarnokd.rxjava3.jdk8interop.SingleInterop;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableFromPublisher;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import org.cactoos.io.BytesOf;
import org.cactoos.text.HexOf;
import org.reactivestreams.FlowAdapters;

/**
 * Asto {@link BlobStore} implementation.
 * @since 1.0
 * @todo #41:30min Implement integration test for this class.
 *  It should verify that blob data can be stored via ASTO storage
 *  at correct path (see README and SPEC),
 *  and calculate the digest correctly.
 * @todo #41:30min Refactor this class, make it more readable.
 *  Put method is overcomplicated right now, try to decompose it,
 *  move some logic into new classes or methods.
 * @checkstyle ReturnCountCheck (500 lines)
 */
public final class AstoBlobs implements BlobStore {

    /**
     * Default buffer size for put publisher.
     */
    private static final int PUB_BUF_SIZE = 8192;

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
    @SuppressWarnings("PMD.OnlyOneReturn")
    public CompletableFuture<Digest> put(final Flow.Publisher<Byte> blob) {
        final MessageDigest sha;
        try {
            sha = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException err) {
            throw new IllegalStateException("This runtime doesn't have SHA-256 algorithm", err);
        }
        final Path tmp;
        final FileChannel out;
        try {
            tmp = Files.createTempFile(this.getClass().getSimpleName(), ".blob.tmp");
            out = FileChannel.open(tmp, StandardOpenOption.WRITE);
        } catch (final IOException err) {
            return CompletableFuture.failedFuture(err);
        }
        return new FlowableFromPublisher<>(FlowAdapters.toPublisher(blob))
            .buffer(AstoBlobs.PUB_BUF_SIZE)
            .map(buf -> new ByteArray(buf).primitiveBytes())
            .flatMapCompletable(
                buf -> Completable.mergeArray(
                    Completable.fromAction(() -> sha.update(buf)),
                    Completable.fromAction(
                        () -> {
                            final ByteBuffer wrap = ByteBuffer.wrap(buf);
                            while (wrap.hasRemaining()) {
                                out.write(wrap);
                            }
                        }
                    )
                )
            ).andThen(Single.fromCallable(() -> new HexOf(new BytesOf(sha.digest())).asString()))
            .map(Digest.Sha256::new)
            .cast(Digest.class)
            .doOnTerminate(out::close)
            .flatMap(
                digest -> SingleInterop.fromFuture(
                    this.asto.save(
                        new Key.From(RegistryRoot.V2, new BlobRef(digest).string(), "data"),
                        FlowAdapters.toFlowPublisher(
                            Flowable.fromArray(new ByteArray(Files.readAllBytes(tmp)).boxedBytes())
                        )
                    ).thenApply(none -> digest)
                )
            ).doOnTerminate(() -> Files.delete(tmp))
            .to(SingleInterop.get()).toCompletableFuture();
    }
}
