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

import java.io.IOException;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A {@link Serializer} implementation for converting {@link String} objects into byte arrays using a specified charset.
 * <p>
 * This class provides a thread-safe mechanism for serializing strings. By default, it uses UTF-8 encoding if no specific
 * charset is provided during construction.
 *
 * <p><b>Example usage:</b>
 * <pre>{@code
 * // Using the default UTF-8 charset
 * Serializer<String> serializer = new StringSerializer();
 * byte[] bytes = serializer.serialize("Hello, World!");
 *
 * // Using a custom charset
 * Serializer<String> serializerWithCharset = new StringSerializer(StandardCharsets.ISO_8859_1);
 * byte[] customEncodedBytes = serializerWithCharset.serialize("Sample Text");
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class StringSerializer implements Serializer<String> {

    private final Charset charset;

    public StringSerializer() {
        this(UTF_8);
    }

    public StringSerializer(Charset charset) {
        this.charset = charset;
    }

    @Override
    public byte[] serialize(String source) throws IOException {
        return source.getBytes(this.charset);
    }
}
