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

/**
 * Digest for image layer.
 * @since 0.1
 */
public interface Digest {

    /**
     * Link digest algorithm name.
     * @return Algorith name string
     */
    String alg();

    /**
     * Link digest hex.
     * @return Link digest hex string
     */
    String digest();

    /**
     * SHA256 digest implementation.
     * @since 0.1
     */
    final class Sha256 implements Digest {

        /**
         * SHA256 hex string.
         */
        private final String hex;

        /**
         * Ctor.
         * @param hex SHA256 hex string
         */
        public Sha256(final String hex) {
            this.hex = hex;
        }

        @Override
        public String alg() {
            return "sha256";
        }

        @Override
        public String digest() {
            return this.hex;
        }
    }

    /**
     * Digest parsed from link reference.
     * <p>
     * Docker registry link is a text file with digest formatted
     * by joining algorithm name with hex string using {@code :} as separator.
     * E.g. if algorihm is {@code sha256} and the digest is {@code 0000}, the link will be
     * {@code sha256:0000}.
     * @since 0.1
     */
    final class FromLink implements Digest {

        /**
         * Digest link.
         */
        private final String link;

        /**
         * Ctor.
         * @param link Link reference
         */
        public FromLink(final String link) {
            this.link = link;
        }

        @Override
        public String alg() {
            return this.part(0);
        }

        @Override
        public String digest() {
            return this.part(1);
        }

        /**
         * Part from input string splitted by {@code :}.
         * @param pos Part position
         * @return Part
         */
        private String part(final int pos) {
            final String[] parts = this.link.split(":");
            if (parts.length != 2) {
                throw new IllegalStateException(
                    String.format(
                        "Broken link, expected two parts separated by `:`, but was %s", this.link
                    )
                );
            }
            return parts[pos];
        }
    }
}
