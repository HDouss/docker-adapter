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

package com.artipie.docker.misc;

import com.artipie.asto.Remaining;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import javax.json.Json;
import javax.json.JsonStructure;

/**
 * This class represents {@link Flow.Subscriber} as converted value.
 * @param <T> Representing type
 * @since 0.1
 */
public abstract class BytesFlowAs<T> {
    /**
     * Byte flow.
     */
    private final Flow.Publisher<ByteBuffer> flow;

    /**
     * Byte chunks accumulator.
     */
    private final Accumulator<T> accum;

    /**
     * Ctor.
     * @param flow Flow of byte chunks
     * @param accum Byte chunks accumulator
     */
    protected BytesFlowAs(final Flow.Publisher<ByteBuffer> flow,
        final Accumulator<T> accum) {
        this.flow = flow;
        this.accum = accum;
    }

    /**
     * Async future.
     * @return Completable future
     */
    public final CompletableFuture<T> future() {
        final CompletableFuture<T> future = new CompletableFuture<>();
        this.flow.subscribe(new FutureSubscriber<>(future, this.accum));
        return future;
    }

    /**
     * Subscriber for bytes flow.
     * @param <T> Target type
     * @since 0.1
     */
    private static final class FutureSubscriber<T> implements Flow.Subscriber<ByteBuffer> {

        /**
         * Future.
         */
        private final CompletableFuture<T> future;

        /**
         * Byte chunks accumulator.
         */
        private final Accumulator<T> accum;

        /**
         * Subscription.
         */
        private final AtomicReference<Subscription> sub;

        /**
         * Ctor.
         * @param future Result future
         * @param accum Bytes chunks accumulator
         */
        FutureSubscriber(final CompletableFuture<T> future,
            final Accumulator<T> accum) {
            this.future = future;
            this.sub = new AtomicReference<>();
            this.accum = accum;
        }

        @Override
        public void onSubscribe(final Subscription subs) {
            if (!this.sub.compareAndSet(null, subs)) {
                throw new IllegalStateException("flow already subscribed");
            }
            subs.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(final ByteBuffer chunk) {
            if (this.future.isCancelled()) {
                this.sub.get().cancel();
            }
            try {
                this.accum.accept(chunk);
            } catch (final IOException err) {
                this.sub.get().cancel();
                this.future.completeExceptionally(err);
            }
        }

        @Override
        public void onError(final Throwable err) {
            this.future.completeExceptionally(err);
        }

        @Override
        public void onComplete() {
            try {
                this.future.complete(this.accum.value());
            } catch (final IOException err) {
                this.future.completeExceptionally(err);
            }
        }
    }

    /**
     * Accumulator for incomiing bytes chunks.
     * @param <T> Target type
     * @since 1.0
     */
    public interface Accumulator<T> {

        /**
         * Accept bytes chunk.
         * @param buf Chang buffer
         * @throws IOException On error
         */
        void accept(ByteBuffer buf) throws IOException;

        /**
         * Get the value.
         * @return Value
         * @throws IOException On error
         */
        T value() throws IOException;
    }

    /**
     * Bytes flow as Json object.
     * @since 0.1
     */
    public static final class JsonObject extends BytesFlowAs<JsonStructure> {
        /**
         * Ctor.
         * @param flow Bytes flow
         */
        public JsonObject(final Flow.Publisher<ByteBuffer> flow) {
            super(
                flow,
                new BytesFlowAs.StreamAccum<>(inp -> Json.createReader(inp).read())
            );
        }
    }

    /**
     * Bytes as text.
     * @since 0.1
     */
    public static final class Text extends BytesFlowAs<String> {

        /**
         * Bytes as text with UTF-8 encoding.
         * @param flow Bytes flow
         */
        public Text(final Flow.Publisher<ByteBuffer> flow) {
            this(flow, StandardCharsets.UTF_8);
        }

        /**
         * Bytes as text with specified charset.
         * @param flow Bytes flow
         * @param charset Text encoding
         */
        public Text(final Flow.Publisher<ByteBuffer> flow, final Charset charset) {
            super(
                flow,
                new BytesFlowAs.StreamAccum<>(
                    inp -> new Scanner(inp, charset).useDelimiter("\\A").next()
                )
            );
        }
    }

    /**
     * Input stream accumulator.
     * @param <T> Target type
     * @since 1.0
     */
    private static final class StreamAccum<T> implements BytesFlowAs.Accumulator<T> {

        /**
         * Input pipe.
         */
        private final PipedInputStream inp;

        /**
         * Output pipe.
         */
        private final PipedOutputStream out;

        /**
         * Function to convert input stream tot target object.
         */
        private final Function<InputStream, T> func;

        /**
         * Ctor.
         * @param func Accumulator function
         */
        @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
        StreamAccum(final Function<InputStream, T> func) {
            this.func = func;
            this.out = new PipedOutputStream();
            try {
                this.inp = new PipedInputStream(this.out);
            } catch (final IOException err) {
                throw new UncheckedIOException(err);
            }
        }

        @Override
        public void accept(final ByteBuffer buf) throws IOException {
            this.out.write(new Remaining(buf).bytes());
        }

        @Override
        public T value() throws IOException {
            this.out.close();
            return this.func.apply(this.inp);
        }
    }
}
