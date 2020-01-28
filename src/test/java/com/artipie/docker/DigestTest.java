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

package com.artipie.docker;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Digest}.
 * @since 0.1
 */
public final class DigestTest {

    @Test
    void parsesValidLink() {
        final Digest dgst = new Digest.FromLink("sha256:1234");
        MatcherAssert.assertThat("bad algorithm", dgst.alg(), Matchers.is("sha256"));
        MatcherAssert.assertThat("bad digest", dgst.digest(), Matchers.is("1234"));
    }

    @Test
    void failsOnInvalidLink() {
        final Digest dgst = new Digest.FromLink("asd");
        Assertions.assertThrows(
            IllegalStateException.class, () -> dgst.alg(), "alg() didn't fail"
        );
        Assertions.assertThrows(
            IllegalStateException.class, () -> dgst.digest(), "digest() didn't fail"
        );
    }
}
