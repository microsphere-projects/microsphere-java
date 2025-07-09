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
 * A {@link Deserializer} implementation for converting byte arrays into {@link String} instances.
 * <p>
 * This class uses a specified {@link Charset} to decode the byte array into a string. If no charset is provided,
 * it defaults to using UTF-8.
 * </p>
 *
 * <h3>Thread Safety</h3>
 * <p>
 * This implementation is thread-safe as it does not maintain any internal state that may be modified after construction.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Using default charset (UTF-8)
 * Deserializer<String> deserializer = new StringDeserializer();
 * String result = deserializer.deserialize("Hello, World!".getBytes(StandardCharsets.UTF_8));
 * System.out.println(result); // Output: Hello, World!
 * }</pre>
 *
 * <pre>{@code
 * // Using custom charset (e.g., ISO-8859-1)
 * Deserializer<String> deserializer = new StringDeserializer(StandardCharsets.ISO_8859_1);
 * String result = deserializer.deserialize("Sample".getBytes(StandardCharsets.ISO_8859_1));
 * System.out.println(result); // Output: Sample
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Deserializer
 * @see Serializer
 * @since 1.0.0
 */
public class StringDeserializer implements Deserializer<String> {

    private final Charset charset;

    public StringDeserializer() {
        this(UTF_8);
    }

    public StringDeserializer(Charset charset) {
        this.charset = charset;
    }

    @Override
    public String deserialize(byte[] bytes) throws IOException {
        return new String(bytes, this.charset);
    }
}
