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

import io.microsphere.logging.Logger;
import io.microsphere.nio.charset.CharsetUtils;
import io.microsphere.util.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.nio.charset.CharsetUtils.DEFAULT_CHARSET;
import static io.microsphere.util.ArrayUtils.EMPTY_BYTE_ARRAY;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.SystemUtils.FILE_ENCODING;
import static java.lang.Integer.getInteger;
import static java.nio.charset.Charset.forName;
import static java.util.Objects.requireNonNull;

/**
 * The utilities class for I/O
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Files
 * @see Paths
 * @since 1.0.0
 */
public abstract class IOUtils implements Utils {

    private static final Logger logger = getLogger(IOUtils.class);

    /**
     * The default buffer size for I/O
     */
    public static final int DEFAULT_BUFFER_SIZE = 2048;

    /**
     * The buffer size for I/O
     */
    public static final int BUFFER_SIZE = getInteger("microsphere.io.buffer.size", DEFAULT_BUFFER_SIZE);

    /**
     * Copies the content of the given {@link InputStream} into a new byte array.
     * <p>
     * This method leaves the input stream open after copying is done.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * InputStream inputStream = new FileInputStream("example.txt");
     * try {
     *     byte[] byteArray = IOUtils.toByteArray(inputStream);
     * } finally {
     *     IOUtils.closeQuietly(inputStream);
     * }
     * }</pre>
     *
     * @param in the InputStream to copy from (may be {@code null} or empty)
     * @return a newly created byte array containing the copied data (possibly empty)
     * @throws IOException if an I/O error occurs during copying
     */
    public static byte[] toByteArray(InputStream in) throws IOException {
        if (in == null) {
            return EMPTY_BYTE_ARRAY;
        }
        FastByteArrayOutputStream out = new FastByteArrayOutputStream(BUFFER_SIZE);
        copy(in, out);
        return out.toByteArray();
    }


    /**
     * Converts the content of the provided {@link InputStream} into a new {@link String}
     * using the default charset.
     *
     * <p>This method internally uses {@link #copyToString(InputStream)} to perform the conversion.
     * It leaves the input stream open after the operation is complete.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * InputStream inputStream = getClass().getResourceAsStream("/example.txt");
     * try {
     *     String result = IOUtils.toString(inputStream);
     *     System.out.println(result);
     * } finally {
     *     IOUtils.closeQuietly(inputStream);
     * }
     * }</pre>
     *
     * @param in the InputStream to convert (may be {@code null} or empty)
     * @return the resulting String from the input stream, or {@code null} if the stream is empty or null
     * @throws IOException if an I/O error occurs during reading from the stream
     * @see #copyToString(InputStream)
     */
    public static String toString(InputStream in) throws IOException {
        return copyToString(in);
    }

    /**
     * Converts the content of the provided {@link InputStream} into a new {@link String}
     * using the specified character encoding.
     *
     * <p>This method internally uses {@link #copyToString(InputStream, String)} to perform the conversion.
     * It leaves the input stream open after the operation is complete.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * InputStream inputStream = getClass().getResourceAsStream("/example.txt");
     * try {
     *     String result = IOUtils.toString(inputStream, "UTF-8");
     *     System.out.println(result);
     * } finally {
     *     IOUtils.closeQuietly(inputStream);
     * }
     * }</pre>
     *
     * @param in       the InputStream to convert (may be {@code null} or empty)
     * @param encoding the character encoding to use; defaults to system file encoding if null or blank
     * @return the resulting String from the input stream, or {@code null} if the stream is empty or null
     * @throws IOException if an I/O error occurs during reading from the stream
     * @see #copyToString(InputStream, String)
     */
    public static String toString(InputStream in, String encoding) throws IOException {
        return copyToString(in, encoding);
    }

    /**
     * Converts the content of the provided {@link InputStream} into a new {@link String}
     * using the specified character set.
     *
     * <p>This method internally uses {@link #copyToString(InputStream, Charset)} to perform the conversion.
     * It leaves the input stream open after the operation is complete.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * InputStream inputStream = getClass().getResourceAsStream("/example.txt");
     * try {
     *     Charset charset = StandardCharsets.UTF_8;
     *     String result = IOUtils.toString(inputStream, charset);
     *     System.out.println(result);
     * } finally {
     *     IOUtils.closeQuietly(inputStream);
     * }
     * }</pre>
     *
     * @param in      the InputStream to convert (may be {@code null} or empty)
     * @param charset the character set to use; defaults to {@link CharsetUtils#DEFAULT_CHARSET} if null
     * @return the resulting String from the input stream, or {@code null} if the stream is empty or null
     * @throws IOException if an I/O error occurs during reading from the stream
     * @see #copyToString(InputStream, Charset)
     */
    public static String toString(InputStream in, Charset charset) throws IOException {
        return copyToString(in, charset);
    }

    /**
     * Converts the content of the provided {@link Reader} into a new {@link String}.
     *
     * <p>This method internally uses {@link #copyToString(Reader)} to perform the conversion.
     * It leaves the reader open after the operation is complete.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Reader reader = new FileReader("example.txt");
     * try {
     *     String result = IOUtils.toString(reader);
     *     System.out.println(result);
     * } finally {
     *     IOUtils.closeQuietly(reader);
     * }
     * }</pre>
     *
     * @param reader the Reader to convert (may be {@code null} or empty)
     * @return the resulting String from the reader, or {@code null} if the reader is empty or null
     * @throws IOException if an I/O error occurs during reading from the reader
     * @see #copyToString(Reader)
     */
    public static String toString(Reader reader) throws IOException {
        return copyToString(reader);
    }

    /**
     * Copies the contents of the given {@link InputStream} into a new {@link String} using the specified character encoding.
     *
     * <p>If the provided encoding is blank (null, empty, or whitespace-only), the system's default file encoding
     * ({@link io.microsphere.util.SystemUtils#FILE_ENCODING}) is used.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * try (InputStream inputStream = getClass().getResourceAsStream("/example.txt")) {
     *     String content = IOUtils.copyToString(inputStream, "UTF-8");
     *     System.out.println(content);
     * }
     * }</pre>
     *
     * @param in      the InputStream to copy from (may be {@code null} or empty)
     * @param encoding the name of the character encoding to use; defaults to system file encoding if blank
     * @return the resulting String from the input stream, or {@code null} if the stream is empty or null
     * @throws IOException if an I/O error occurs during reading from the stream
     */
    public static String copyToString(InputStream in, String encoding) throws IOException {
        String charset = isBlank(encoding) ? FILE_ENCODING : encoding;
        return copyToString(in, forName(charset));
    }

    /**
     * Converts the content of the provided {@link InputStream} into a new {@link String}
     * using the default charset ({@link CharsetUtils#DEFAULT_CHARSET}).
     *
     * <p>This method internally uses {@link #copyToString(InputStream, Charset)} to perform the conversion.
     * It leaves the input stream open after the operation is complete.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * try (InputStream inputStream = getClass().getResourceAsStream("/example.txt")) {
     *     String result = IOUtils.copyToString(inputStream);
     *     System.out.println(result);
     * }
     * }</pre>
     *
     * @param in the InputStream to convert (may be {@code null} or empty)
     * @return the resulting String from the input stream, or {@code null} if the stream is empty or null
     * @throws IOException if an I/O error occurs during reading from the stream
     * @see #copyToString(InputStream, Charset)
     */
    public static String copyToString(InputStream in) throws IOException {
        return copyToString(in, DEFAULT_CHARSET);
    }

    /**
     * Converts the content of the provided {@link InputStream} into a new {@link String}
     * using the specified character set.
     *
     * <p>This method reads all available bytes from the input stream and decodes them into a string
     * using the given charset. If the input stream is empty or null, this method returns null.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * try (InputStream inputStream = getClass().getResourceAsStream("/example.txt")) {
     *     Charset charset = StandardCharsets.UTF_8;
     *     String result = IOUtils.copyToString(inputStream, charset);
     *     System.out.println(result);
     * }
     * }</pre>
     *
     * @param in      the InputStream to convert (may be {@code null} or empty)
     * @param charset the character set to use; defaults to {@link CharsetUtils#DEFAULT_CHARSET} if null
     * @return the resulting String from the input stream, or {@code null} if the stream is empty or null
     * @throws IOException if an I/O error occurs during reading from the stream
     */
    public static String copyToString(InputStream in, Charset charset) throws IOException {
        byte[] bytes = toByteArray(in);
        if (EMPTY_BYTE_ARRAY == bytes) {
            return null;
        }
        return new String(bytes, charset == null ? DEFAULT_CHARSET : charset);
    }

    /**
     * Converts the content of the provided {@link Reader} into a new {@link String}.
     *
     * <p>This method reads all available characters from the reader and appends them to a
     * {@link StringBuilderWriter}, which is then converted to a String. If the reader is empty or null,
     * this method returns {@code null}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * try (Reader reader = new FileReader("example.txt")) {
     *     String result = IOUtils.copyToString(reader);
     *     System.out.println(result);
     * }
     * }</pre>
     *
     * @param reader the Reader to convert (may be {@code null} or empty)
     * @return the resulting String from the reader, or {@code null} if the reader is empty or null
     * @throws IOException if an I/O error occurs during reading from the reader
     */
    public static String copyToString(Reader reader) throws IOException {
        if (reader == null) {
            return null;
        }
        StringBuilderWriter stringWriter = new StringBuilderWriter();
        copy(reader, stringWriter);
        return stringWriter.toString();
    }

    /**
     * Copies all data from the given {@link InputStream} to the specified {@link OutputStream}.
     * <p>
     * This method uses a buffer of size {@link #BUFFER_SIZE} for efficient copying.
     * Both streams remain open after this operation. The total number of bytes copied is returned.
     * </p>
     *
     * <p>If either stream is {@code null}, an {@link IllegalArgumentException} will be thrown.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * try (InputStream inputStream = new FileInputStream("source.txt");
     *      OutputStream outputStream = new FileOutputStream("destination.txt")) {
     *     int byteCount = IOUtils.copy(inputStream, outputStream);
     *     System.out.println("Copied " + byteCount + " bytes.");
     * }
     * }</pre>
     *
     * @param in  the source InputStream to read from; must not be {@code null}
     * @param out the target OutputStream to write to; must not be {@code null}
     * @return the number of bytes copied from the input stream to the output stream
     * @throws IOException if an I/O error occurs while reading or writing
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        requireNonNull(in, "No InputStream specified");
        requireNonNull(out, "No OutputStream specified");

        int byteCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            byteCount += bytesRead;
        }
        out.flush();
        if (logger.isTraceEnabled()) {
            logger.trace("Copied {} bytes[buffer size : {}] from InputStream[{}] to OutputStream[{}]", byteCount, BUFFER_SIZE, in, out);
        }
        return byteCount;
    }

    /**
     * Copies all characters from the given {@link Reader} to the specified {@link Writer}.
     * <p>
     * This method uses a buffer of size {@link #BUFFER_SIZE} for efficient copying.
     * Both the reader and writer remain open after this operation. The total number of characters copied is returned.
     * </p>
     *
     * <p>If either the reader or writer is {@code null}, an {@link IllegalArgumentException} will be thrown.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * try (Reader reader = new FileReader("input.txt");
     *      Writer writer = new FileWriter("output.txt")) {
     *     int charCount = IOUtils.copy(reader, writer);
     *     System.out.println("Copied " + charCount + " characters.");
     * }
     * }</pre>
     *
     * @param reader the source Reader to read from; must not be {@code null}
     * @param writer the target Writer to write to; must not be {@code null}
     * @return the number of characters copied from the reader to the writer
     * @throws IOException if an I/O error occurs while reading or writing
     */
    public static int copy(Reader reader, Writer writer) throws IOException {
        requireNonNull(reader, "No Reader specified");
        requireNonNull(writer, "No Writer specified");

        int charsCount = 0;
        char[] buffer = new char[BUFFER_SIZE];
        int charsRead;
        while ((charsRead = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, charsRead);
            charsCount += charsRead;
        }
        writer.flush();
        if (logger.isTraceEnabled()) {
            logger.trace("Copied {} bytes[buffer size : {}] from Reader[{}] to Writer[{}]", charsCount, BUFFER_SIZE, reader, writer);
        }
        return charsCount;
    }

    /**
     * Closes the specified {@link Closeable} object quietly, suppressing any {@link IOException}.
     *
     * <p>If the provided {@code closeable} is {@code null}, this method does nothing.
     * Any I/O errors that occur during closing are logged at the TRACE level.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * InputStream inputStream = null;
     * try {
     *     inputStream = new FileInputStream("example.txt");
     *     // perform operations with inputStream
     * } catch (IOException e) {
     *     // handle exception
     * } finally {
     *     IOUtils.close(inputStream);
     * }
     * }</pre>
     *
     * @param closeable the Closeable object to be closed, may be {@code null}
     */
    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("The Closeable[{}] can't be closed", closeable, e);
            }
        }
    }

    private IOUtils() {
    }
}
