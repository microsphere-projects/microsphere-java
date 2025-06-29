/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.microsphere.io;


import java.io.StringWriter;
import java.io.Writer;

import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.CharSequenceUtils.isNotEmpty;

/**
 * {@link Writer} implementation that outputs to a {@link StringBuilder}.
 * <p>
 * <strong>NOTE:</strong> This implementation, as an alternative to
 * {@code java.io.StringWriter}, provides
 * <ul>
 *     <li>an <i>un-synchronized</i> (i.e. for use in a single thread) implementation for better performance.
 *     For safe usage with multiple {@link Thread}s then {@code StringWriter} should be used.
 *     </li>
 *     <li>
 *         the write methods do not throw any {@link java.io.IOException} declaration:
 *         <ul>
 *             <li>{@link #write(int)}</li>
 *             <li>{@link #write(char[])}</li>
 *             <li>{@link #write(char[], int, int)}</li>
 *             <li>{@link #write(String)}</li>
 *             <li>{@link #write(String, int, int)}</li>
 *         </ul>
 *     </li>
 * </ul>
 * <p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *     StringBuilderWriter writer = new StringBuilderWriter();
 *     writer.write("Hello, World!");
 *     System.out.println(writer.toString()); // Output: Hello, World!
 * }</pre>
 *
 * <p>
 * The class can also be used with a pre-defined capacity:
 * <pre>{@code
 *     StringBuilderWriter writer = new StringBuilderWriter(1024);
 *     writer.write("Data");
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see StringWriter
 * @see Writer
 */
public class StringBuilderWriter extends Writer {

    private final StringBuilder builder;

    /**
     * Constructs a new {@link StringBuilder} instance with default capacity.
     */
    public StringBuilderWriter() {
        this(null);
    }

    /**
     * Constructs a new {@link StringBuilder} instance with the specified capacity.
     *
     * @param capacity The initial capacity of the underlying {@link StringBuilder}
     */
    public StringBuilderWriter(final int capacity) {
        this(new StringBuilder(capacity));
    }

    /**
     * Constructs a new instance with the specified {@link StringBuilder}.
     *
     * <p>If {@code builder} is null a new instance with default capacity will be created.</p>
     *
     * @param builder The String builder. May be null.
     */
    public StringBuilderWriter(final StringBuilder builder) {
        this.builder = builder != null ? builder : new StringBuilder();
    }

    /**
     * Writes a single character to the {@link StringBuilder}.
     *
     * @param c The integer value of the character to write
     */
    @Override
    public void write(int c) {
        builder.append((char) c);
    }

    /**
     * Writes an array of characters to the {@link StringBuilder}.
     *
     * @param value The character array to write. May be null.
     */
    @Override
    public void write(char[] value) {
        if (isNotEmpty(value)) {
            builder.append(value, 0, value.length);
        }
    }

    /**
     * Writes a portion of a character array to the {@link StringBuilder}.
     *
     * @param value  The value to write
     * @param offset The index of the first character
     * @param length The number of characters to write
     */
    @Override
    public void write(final char[] value, final int offset, final int length) {
        if (isNotEmpty(value)) {
            builder.append(value, offset, length);
        }
    }

    /**
     * Writes a String to the {@link StringBuilder}.
     *
     * @param value The value to write
     */
    @Override
    public void write(final String value) {
        if (isNotEmpty(value)) {
            builder.append(value);
        }
    }

    @Override
    public void write(String value, int off, int len) {
        if (isNotEmpty(value)) {
            builder.append(value, off, len);
        }
    }

    /**
     * Appends a single character to this Writer.
     *
     * @param value The character to append
     * @return This writer instance
     */
    @Override
    public Writer append(final char value) {
        builder.append(value);
        return this;
    }

    /**
     * Appends a character sequence to this Writer.
     *
     * @param value The character to append
     * @return This writer instance
     */
    @Override
    public Writer append(final CharSequence value) {
        if (isNotEmpty(value)) {
            builder.append(value);
        }
        return this;
    }

    /**
     * Appends a portion of a character sequence to the {@link StringBuilder}.
     *
     * @param value The character to append
     * @param start The index of the first character
     * @param end   The index of the last character + 1
     * @return This writer instance
     */
    @Override
    public Writer append(final CharSequence value, final int start, final int end) {
        if (isNotEmpty(value)) {
            builder.append(value, start, end);
        }
        return this;
    }

    /**
     * Closing this writer has no effect.
     */
    @Override
    public void close() {
        // no-op
    }

    /**
     * Flushing this writer has no effect.
     */
    @Override
    public void flush() {
        // no-op
    }

    /**
     * Returns the underlying builder.
     *
     * @return The underlying builder
     */
    public StringBuilder getBuilder() {
        return builder;
    }

    /**
     * Returns {@link StringBuilder#toString()}.
     *
     * @return The contents of the String builder.
     */
    @Override
    public String toString() {
        return builder.toString();
    }
}
