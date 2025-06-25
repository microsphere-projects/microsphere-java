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
import io.microsphere.util.SystemUtils;
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
     * Copy the contents of the given InputStream into a new byte array.
     * <p>Leaves the stream open when done.
     *
     * @param in the stream to copy from (may be {@code null} or empty)
     * @return the new byte array that has been copied to (possibly empty)
     * @throws IOException in case of I/O errors
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
     * {@link #copyToString(InputStream)} as recommended
     *
     * @see #copyToString(InputStream)
     */
    public static String toString(InputStream in) throws IOException {
        return copyToString(in);
    }

    /**
     * {@link #copyToString(InputStream, String)} as recommended
     *
     * @see #copyToString(InputStream, String)
     */
    public static String toString(InputStream in, String encoding) throws IOException {
        return copyToString(in, encoding);
    }

    /**
     * {@link #copyToString(InputStream, Charset)}  as recommended
     *
     * @see #copyToString(InputStream, Charset)
     */
    public static String toString(InputStream in, Charset charset) throws IOException {
        return copyToString(in, charset);
    }

    /**
     * {@link #copyToString(Reader)}  as recommended
     *
     * @see #copyToString(Reader)
     */
    public static String toString(Reader reader) throws IOException {
        return copyToString(reader);
    }

    /**
     * Copy the contents of the given InputStream into a new {@link String}.
     * <p>Leaves the stream open when done.
     *
     * @param in       the stream to copy from (may be {@code null} or empty)
     * @param encoding the encoding to use, if it's <code>null</code>, take the {@link SystemUtils#FILE_ENCODING} as default
     * @return the new byte array that has been copied to (possibly empty)
     * @throws IOException in case of I/O errors
     */
    public static String copyToString(InputStream in, String encoding) throws IOException {
        String charset = isBlank(encoding) ? FILE_ENCODING : encoding;
        return copyToString(in, forName(charset));
    }

    /**
     * Copy the contents of the given InputStream into a new {@link String} using {@link CharsetUtils#DEFAULT_CHARSET}.
     * <p>Leaves the stream open when done.
     *
     * @param in the stream to copy from (may be {@code null} or empty)
     * @return the new byte array that has been copied to (possibly empty)
     * @throws IOException in case of I/O errors
     */
    public static String copyToString(InputStream in) throws IOException {
        return copyToString(in, DEFAULT_CHARSET);
    }

    /**
     * See {@link #toString(InputStream, Charset)}
     */
    public static String copyToString(InputStream in, Charset charset) throws IOException {
        byte[] bytes = toByteArray(in);
        if (EMPTY_BYTE_ARRAY == bytes) {
            return null;
        }
        return new String(bytes, charset == null ? DEFAULT_CHARSET : charset);
    }

    /**
     * Copy the contents of the given Reader into a new {@link String}.
     * <p>Leaves the Reader open when done.</p>
     *
     * @param reader the Reader to copy from (may be {@code null} or empty)
     * @return the new String that has been copied to (possibly empty)
     * @throws IOException in case of I/O errors
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
     * Copy the contents of the given InputStream to the given OutputStream.
     * <p>Leaves both streams open when done.
     *
     * @param in  the InputStream to copy from
     * @param out the OutputStream to copy to
     * @return the number of bytes copied
     * @throws IOException in case of I/O errors
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
     * Copy the contents of the given Reader to the given Writer.
     * <p>Leaves both streams open when done.
     *
     * @param reader the Reader to copy from
     * @param writer the Writer to copy to
     * @return the number of chars copied
     * @throws IOException in case of I/O errors
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
     * Unconditionally close a <code>Closeable</code>.
     * <p>
     * Equivalent to {@link Closeable#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     * <p>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     *   Closeable closeable = null;
     *   try {
     *       closeable = new FileReader("foo.txt");
     *       // process closeable
     *       closeable.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(closeable);
     *   }
     * }</pre>
     *
     * @param closeable the object to close, may be null or already closed
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
