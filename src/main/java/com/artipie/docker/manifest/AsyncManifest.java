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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.json.Json;
import javax.json.JsonObject;

/**
 * Flow {@link Processor} to convert byte stream into {@link JsonObject}.
 * @since 1.0
 */
public final class AsyncManifest implements Future<JsonObject>, Flow.Subscriber<Byte> {

    /**
     * Default buffer capacity.
     */
    private static final int BUF_CAP = 1024;

    /**
     * Buffer for receiving data.
     */
    private final List<Byte> buf;

    /**
     * Target future.
     */
    private final CompletableFuture<JsonObject> target;

    /**
     * List of subscriptions.
     */
    private final List<Subscription> subs;

    /**
     * Ctor.
     */
    public AsyncManifest() {
        this.buf = new ArrayList<>(AsyncManifest.BUF_CAP);
        this.subs = new LinkedList<>();
        this.target = new CompletableFuture<>();
    }

    @Override
    public void onNext(final Byte item) {
        this.buf.add(item);
    }

    @Override
    public void onError(final Throwable err) {
        this.target.completeExceptionally(err);
    }

    @Override
    public void onComplete() {
        final byte[] arr = new byte[this.buf.size()];
        for (int pos = 0; pos < this.buf.size(); ++pos) {
            arr[pos] = this.buf.get(pos);
        }
        final JsonObject json = Json.createReader(new ByteArrayInputStream(arr)).readObject();
        this.target.complete(json);
    }

    @Override
    public void onSubscribe(final Subscription sub) {
        this.subs.add(sub);
    }

    @Override
    public boolean cancel(final boolean interrupt) {
        final boolean canc = this.target.cancel(interrupt);
        if (canc) {
            this.subs.forEach(Subscription::cancel);
        }
        return canc;
    }

    @Override
    public boolean isCancelled() {
        return this.target.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.target.isDone();
    }

    @Override
    public JsonObject get() throws InterruptedException, ExecutionException {
        return this.target.get();
    }

    @Override
    public JsonObject get(final long timeout, final TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return this.target.get(timeout, unit);
    }
}
