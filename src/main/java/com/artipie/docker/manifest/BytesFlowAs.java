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

package com.artipie.docker.manifest;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import javax.json.Json;

/**
 * This class represents {@link Flow.Subscriber} as converted value.
 * @param <T> Representing type
 * @since 1.0
 */
public abstract class BytesFlowAs<T> {
    /**
     * Byte flow.
     */
    private final Flow.Publisher<Byte> flow;

    /**
     * Function to convert byte flow to new type.
     */
    private final Function<byte[], T> func;

    /**
     * Ctor.
     * @param flow Flow of bytes
     * @param func Function to convert bytes to target type
     */
    protected BytesFlowAs(final Flow.Publisher<Byte> flow,
        final Function<byte[], T> func) {
        this.flow = flow;
        this.func = func;
    }

    /**
     * Async future.
     * @return Completable future
     */
    public final CompletableFuture<T> future() {
        final CompletableFuture<T> future = new CompletableFuture<>();
        this.flow.subscribe(new FutureSubscriber<>(future, this.func));
        return future;
    }

    /**
     * Subscriber for bytes flow.
     * @param <T> Target type
     * @since 1.0
     */
    private static final class FutureSubscriber<T> implements Flow.Subscriber<Byte> {

        /**
         * Buffer size.
         */
        private static final int BUF_SIZE = 1024;

        /**
         * Future.
         */
        private final CompletableFuture<T> future;

        /**
         * Function to convert bytes to target type.
         */
        private final Function<byte[], T> func;

        /**
         * Subscription.
         */
        private final AtomicReference<Subscription> sub;

        /**
         * Byte buffer.
         */
        private final List<Byte> buf;

        /**
         * Ctor.
         * @param future Result future
         * @param func Function to converty btyes to target
         */
        FutureSubscriber(final CompletableFuture<T> future,
            final Function<byte[], T> func) {
            this.future = future;
            this.func = func;
            this.buf = new ArrayList<>(BytesFlowAs.FutureSubscriber.BUF_SIZE);
            this.sub = new AtomicReference<>();
        }

        @Override
        public void onSubscribe(final Subscription subs) {
            if (!this.sub.compareAndSet(null, subs)) {
                throw new IllegalStateException("flow already subscribed");
            }
            subs.request(BytesFlowAs.FutureSubscriber.BUF_SIZE);
        }

        @Override
        public void onNext(final Byte item) {
            if (this.future.isCancelled()) {
                this.sub.get().cancel();
            }
            this.buf.add(item);
            if (this.buf.size() % BytesFlowAs.FutureSubscriber.BUF_SIZE == 0) {
                this.sub.get().request(BytesFlowAs.FutureSubscriber.BUF_SIZE);
            }
        }

        @Override
        public void onError(final Throwable err) {
            this.future.completeExceptionally(err);
        }

        @Override
        @SuppressWarnings("PMD.AvoidCatchingThrowable")
        public void onComplete() {
            final byte[] arr = new byte[this.buf.size()];
            for (int pos = 0; pos < this.buf.size(); ++pos) {
                arr[pos] = this.buf.get(pos);
            }
            try {
                final T target = this.func.apply(arr);
                this.future.complete(target);
                // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Throwable err) {
                this.future.completeExceptionally(err);
            }
        }
    }

    /**
     * Bytes flow as Json object.
     * @since 1.0
     */
    public static final class JsonObject extends BytesFlowAs<javax.json.JsonObject> {
        /**
         * Ctor.
         * @param flow Bytes flow
         */
        public JsonObject(final Flow.Publisher<Byte> flow) {
            super(
                flow,
                bytes -> Json.createReader(new ByteArrayInputStream(bytes)).readObject()
            );
        }
    }
}
