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

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link StringBuilderWriter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see StringBuilderWriter
 * @since 1.0.0
 */
class StringBuilderWriterTest extends AbstractTestCase {

    private StringBuilderWriter writer;

    @BeforeEach
    void setUp() {
        this.writer = new StringBuilderWriter();
    }

    @Test
    void testConstructorWithCapacity() {
        int size = random.nextInt(1024);
        StringBuilderWriter writer = new StringBuilderWriter(size);
        assertEquals(size, writer.getBuilder().capacity());
    }

    @Test
    void testConstructorWithStringBuilder() {
        StringBuilderWriter writer = new StringBuilderWriter(new StringBuilder("Hello"));
        assertEquals("Hello", writer.toString());
    }

    @Test
    void testWriteWithChar() {
        writer.write('H');
        assertEquals("H", writer.toString());
    }

    @Test
    void testWriteWithChars() {
        writer.write("Hello".toCharArray());
        assertEquals("Hello", writer.toString());
    }

    @Test
    void testWriteWithCharsOnEmpty() {
        writer.write((char[]) null);
        writer.write(new char[0]);
    }

    @Test
    void testWriteWithCharsAndRange() {
        writer.write("Hello".toCharArray(), 0, 2);
        assertEquals("He", writer.toString());
    }

    @Test
    void testWriteWithCharsAndRangeOnNull() {
        writer.write((char[]) null, 0, 0);
        writer.write(new char[0], 0, 0);
    }

    @Test
    void testWriteWithString() {
        writer.write("Hello");
        assertEquals("Hello", writer.toString());
    }

    @Test
    void testWriteWithStringOnEmpty() {
        writer.write(TEST_NULL_STRING);
        writer.write(EMPTY_STRING);
    }

    @Test
    void testWriteWithStringAndRange() {
        writer.write("Hello", 0, 2);
        assertEquals("He", writer.toString());
    }

    @Test
    void testWriteWithStringAndRangeOnEmpty() {
        writer.write(TEST_NULL_STRING, 0, 0);
        writer.write(EMPTY_STRING, 0, 0);
    }

    @Test
    void testAppendWithChar() {
        writer.append('H');
        assertEquals("H", writer.toString());
    }

    @Test
    void testAppendWithCharSequence() {
        writer.append("Hello");
        assertEquals("Hello", writer.toString());
    }

    @Test
    void testAppendWithCharSequenceOnEmpty() {
        writer.append(TEST_NULL_STRING);
        writer.append(EMPTY_STRING);
    }

    @Test
    void testAppendWithCharSequenceAndRange() {
        writer.append("Hello", 0, 2);
        assertEquals("He", writer.toString());
    }

    @Test
    void testAppendWithCharSequenceAndRangeOnEmpty() {
        writer.append(TEST_NULL_STRING, 0, 0);
        writer.append(EMPTY_STRING, 0, 0);
    }

    @Test
    void testClose() {
        writer.close();
    }

    @Test
    void testFlush() {
        writer.flush();
    }
}