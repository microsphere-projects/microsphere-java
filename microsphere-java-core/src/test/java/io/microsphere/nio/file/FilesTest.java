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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.microsphere.nio.file.Files.readLines;
import static io.microsphere.util.ClassLoaderUtils.getResource;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link Files} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Files
 * @since 1.0.0
 */
class FilesTest {

    private static final String TEST_FILE_PATH = "test.txt";

    private final URL TEST_FILE_RESOURCE = getResource(TEST_FILE_PATH);

    private Path testFilePath;

    private File testFile;

    @BeforeEach
    void setUp() throws Throwable {
        this.testFilePath = Paths.get(TEST_FILE_RESOURCE.toURI());
        this.testFile = this.testFilePath.toFile();
    }

    @Test
    void testReadLines() throws IOException {
        String[] lines = readLines(this.testFilePath);
        String[] lines2 = readLines(this.testFile);
        assertArrayEquals(lines, lines2);
        assertEquals(1, lines.length);
        assertEquals("test", lines[0]);
    }
}