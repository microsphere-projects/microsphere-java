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

package io.microsphere.nio.file;

import io.microsphere.annotation.Nonnull;
import io.microsphere.io.IOUtils;
import io.microsphere.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

import static io.microsphere.nio.charset.CharsetUtils.DEFAULT_CHARSET;
import static java.nio.file.Files.newInputStream;

/**
 * The utilties class of {@link File} based on NIO
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Path
 * @since 1.0.0
 */
public abstract class Files implements Utils {

    /**
     * Reads all lines from the given file and returns them as an array of strings,
     * using the default {@link Charset} for decoding.
     *
     * <p>This method reads the entire content of the file, converts it to a string using
     * the default charset, and then splits the string into lines based on the system line separator.
     * The file input stream is automatically closed after this operation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * File file = new File("/example.txt");
     * try {
     *     String[] lines = Files.readLines(file);
     *     for (String line : lines) {
     *         System.out.println(line);
     *     }
     * } catch(IOException e) {
     *     // Handle the exception
     * }
     * }</pre>
     *
     * @param file the file to read from
     * @return an array of strings representing the lines read from the file
     * @throws IOException if an I/O error occurs during reading from the file
     * @see #readLines(File, Charset)
     */
    @Nonnull
    public static String[] readLines(File file) throws IOException {
        return readLines(file, DEFAULT_CHARSET);
    }

    /**
     * Reads all lines from the given file and returns them as an array of strings,
     * using the specified {@link Charset} for decoding.
     *
     * <p>This method reads the entire content of the file, converts it to a string using
     * the provided charset, and then splits the string into lines based on the system line separator.
     * The file input stream is automatically closed after this operation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * File file = new File("/example.txt");
     * Charset charset = java.nio.charset.StandardCharsets.UTF_8;
     * try {
     *     String[] lines = Files.readLines(file, charset);
     *     for (String line : lines) {
     *         System.out.println(line);
     *     }
     * } catch(IOException e) {
     *     // Handle the exception
     * }
     * }</pre>
     *
     * @param file    the file to read from
     * @param charset the {@link Charset} to use for decoding the file content
     * @return an array of strings representing the lines read from the file
     * @throws IOException if an I/O error occurs during reading from the file
     * @see IOUtils#readLines(InputStream, Charset)
     */
    @Nonnull
    public static String[] readLines(File file, Charset charset) throws IOException {
        return readLines(file.toPath(), charset);
    }

    /**
     * Reads all lines from the given file path and returns them as an array of strings,
     * using the default {@link Charset} for decoding.
     *
     * <p>This method reads the entire content of the file, converts it to a string using
     * the default charset, and then splits the string into lines based on the system line separator.
     * The file input stream is automatically closed after this operation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Path filePath = Paths.get("/example.txt");
     * try {
     *     String[] lines = Files.readLines(filePath);
     *     for (String line : lines) {
     *         System.out.println(line);
     *     }
     * } catch(IOException e) {
     *     // Handle the exception
     * }
     * }</pre>
     *
     * @param filePath the file path
     * @return an array of strings representing the lines read from the file
     * @throws IOException if an I/O error occurs during reading from the file
     * @see #readLines(Path, Charset)
     */
    @Nonnull
    public static String[] readLines(Path filePath) throws IOException {
        return readLines(filePath, DEFAULT_CHARSET);
    }

    /**
     * Reads all lines from the given file path and returns them as an array of strings,
     * using the specified {@link Charset} for decoding.
     *
     * <p>This method reads the entire content of the file, converts it to a string using
     * the provided charset, and then splits the string into lines based on the system line separator.
     * The file input stream is automatically closed after this operation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Path filePath = Paths.get("/example.txt");
     * Charset charset = java.nio.charset.StandardCharsets.UTF_8;
     * try {
     *     String[] lines = Files.readLines(filePath, charset);
     *     for (String line : lines) {
     *         System.out.println(line);
     *     }
     * } catch(IOException e) {
     *     // Handle the exception
     * }
     * }</pre>
     *
     * @param filePath the file path
     * @param charset  the {@link Charset} to use for decoding the file content
     * @return an array of strings representing the lines read from the file
     * @throws IOException if an I/O error occurs during reading from the file
     * @see IOUtils#readLines(InputStream, Charset)
     */
    @Nonnull
    public static String[] readLines(Path filePath, Charset charset) throws IOException {
        try (InputStream inputStream = newInputStream(filePath)) {
            return IOUtils.readLines(inputStream, charset);
        }
    }

    private Files() {
    }
}