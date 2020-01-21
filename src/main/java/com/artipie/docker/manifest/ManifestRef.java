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

import java.net.URI;

/**
 * Manifest link reference.
 * <p>
 * Can be resolved by image tag or digest.
 * </p>
 * @since 1.0
 */
public interface ManifestRef {

    /**
     * Path to manifest link.
     * @return Relative path for manifest link
     */
    URI path();

    /**
     * Manifest link from tag.
     * @since 1.0
     */
    final class Tag implements ManifestRef {

        /**
         * Image tag.
         */
        private final String tag;

        /**
         * Ctor.
         * @param tag Image tag
         */
        public Tag(final String tag) {
            this.tag = tag;
        }

        @Override
        public URI path() {
            return URI.create(String.join("/", "tags", this.tag, "current", "link"));
        }
    }

    /**
     * Manifest link from digest.
     * @since 1.0
     */
    final class Digest implements ManifestRef {

        /**
         * Digest algorithm.
         */
        private final String alg;

        /**
         * Digest hex.
         */
        private final String hex;

        /**
         * Manifest reference from digest.
         * @param alg Diges algorithm name
         * @param hex Digest encoded to hex string
         */
        public Digest(final String alg, final String hex) {
            this.alg = alg;
            this.hex = hex;
        }

        @Override
        public URI path() {
            return URI.create(String.join("/", "revisions", this.alg, this.hex, "link"));
        }
    }
}
