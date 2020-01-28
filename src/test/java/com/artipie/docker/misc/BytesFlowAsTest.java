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

import com.artipie.asto.ByteArray;
import io.reactivex.rxjava3.core.Flowable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.json.Json;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;

/**
 * Test case for {@link BytesFlowAs}.
 *
 * @since 1.0
 */
final class BytesFlowAsTest {

    @Test
    void parsesText() throws Exception {
        final String txt = "hello";
        final Charset charset = StandardCharsets.UTF_8;
        MatcherAssert.assertThat(
            new BytesFlowAs.Text(
                FlowAdapters.toFlowPublisher(
                    Flowable.fromArray(new ByteArray(txt.getBytes(charset)).boxedBytes())
                ),
                charset
            ).future().get(),
            Matchers.equalTo(txt)
        );
    }

    @Test
    void parsesJson() throws Exception {
        MatcherAssert.assertThat(
            new BytesFlowAs.JsonObject(
                FlowAdapters.toFlowPublisher(
                    Flowable.fromArray(
                        new ByteArray(
                            "{\"one\":1}".getBytes(StandardCharsets.UTF_8)
                        ).boxedBytes()
                    )
                )
            ).future().get(),
            Matchers.equalTo(
                Json.createObjectBuilder()
                    .add("one", 1)
                    .build()
            )
        );
    }
}
