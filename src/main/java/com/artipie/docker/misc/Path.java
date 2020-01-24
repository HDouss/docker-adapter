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

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract data path, used by asto library.
 * @since 1.0
 */
public interface Path {

    /**
     * Asto data key.
     * @return Key string
     */
    String key();

    /**
     * Path from parts.
     * @since 1.0
     */
    final class From implements Path {

        /**
         * Parts.
         */
        private final Iterable<String> parts;

        /**
         * Ctor.
         * @param parts Parts
         */
        public From(final String... parts) {
            this(Arrays.asList(parts));
        }

        /**
         * From base path and parts.
         * @param base Base path
         * @param parts Parts
         */
        public From(final Path base, final String... parts) {
            this(
                Stream.concat(
                    Arrays.asList(base.key().split("/")).stream(),
                    Arrays.asList(parts).stream()
                ).collect(Collectors.toList())
            );
        }

        /**
         * Ctor.
         * @param parts Parts
         */
        public From(final Collection<String> parts) {
            this.parts = parts;
        }

        @Override
        public String key() {
            return String.join("/", this.parts);
        }
    }
}
