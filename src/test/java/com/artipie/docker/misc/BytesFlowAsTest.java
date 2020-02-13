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

import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import javax.json.Json;
import javax.json.JsonObject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;

/**
 * Test case for {@link BytesFlowAs}.
 * @since 0.1
 */
final class BytesFlowAsTest {

    @Test
    void parsesText() throws Exception {
        final String txt = "hello";
        MatcherAssert.assertThat(
            new BytesFlowAs.Text(
                FlowAdapters.toFlowPublisher(
                    Flowable.fromArray(ByteBuffer.wrap(txt.getBytes(StandardCharsets.UTF_8)))
                ),
                StandardCharsets.UTF_8
            ).future().get(),
            Matchers.equalTo(txt)
        );
    }

    @Test
    void parsesJson() throws Exception {
        final JsonObject json = Json.createObjectBuilder()
            .add("one", 1)
            .build();
        MatcherAssert.assertThat(
            new BytesFlowAs.JsonObject(
                FlowAdapters.toFlowPublisher(
                    Flowable.fromArray(
                        ByteBuffer.wrap(json.toString().getBytes(StandardCharsets.UTF_8))
                    )
                )
            ).future().get(),
            Matchers.equalTo(json)
        );
    }
}
