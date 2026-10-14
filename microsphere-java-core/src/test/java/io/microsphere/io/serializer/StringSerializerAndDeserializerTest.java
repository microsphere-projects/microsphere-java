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
package io.microsphere.io.serializer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link StringSerializer} and {@link StringDeserializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class StringSerializerAndDeserializerTest {

    private StringSerializer serializer = StringSerializer.INSTANCE;

    private StringDeserializer deserializer = StringDeserializer.INSTANCE;

    @Test
    void test() throws IOException {
        String value = "Test";
        byte[] bytes = StringSerializer.INSTANCE.serialize(value);
        assertArrayEquals(value.getBytes(UTF_8), bytes);
        assertEquals(value, deserializer.deserialize(bytes));
    }

    @Test
    void testWithCustomCharset() throws IOException {
        Charset charset = ISO_8859_1;
        StringSerializer customSerializer = new StringSerializer(charset);
        StringDeserializer customDeserializer = new StringDeserializer(charset);
        String value = "Test";
        byte[] bytes = customSerializer.serialize(value);
        assertArrayEquals(value.getBytes(charset), bytes);
        assertEquals(value, customDeserializer.deserialize(bytes));
    }

    @Test
    void testSerializeEmptyString() throws IOException {
        byte[] bytes = serializer.serialize("");
        assertArrayEquals(new byte[0], bytes);
        assertEquals("", deserializer.deserialize(bytes));
    }

    @Test
    void testRoundTripWithUTF16() throws IOException {
        Charset charset = StandardCharsets.UTF_16;
        StringSerializer utf16Serializer = new StringSerializer(charset);
        StringDeserializer utf16Deserializer = new StringDeserializer(charset);
        String value = "Hello UTF-16";
        assertEquals(value, utf16Deserializer.deserialize(utf16Serializer.serialize(value)));
    }

    @Test
    void testNull() throws IOException {
        assertEquals(null, serializer.serialize(null));
        assertEquals(null, deserializer.deserialize(null));
    }
}
