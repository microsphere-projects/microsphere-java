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
import java.nio.charset.StandardCharsets;

/**
 * A {@link Deserializer} implementation for converting byte arrays into {@link String} instances.
 * <p>
 * This class uses the UTF-8 character encoding to decode byte arrays into strings. It ensures that the deserialization
 * process is consistent and reliable across different platforms and environments.
 * </p>
 *
 * <h3>Thread Safety</h3>
 * <p>
 * This implementation is thread-safe, as it does not maintain any internal state that could be affected by concurrent
 * access. Multiple threads can safely share and use a single instance of this class.
 * </p>
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * Deserializer<String> stringDeserializer = new StringDeserializer();
 * byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);
 * String result = stringDeserializer.deserialize(data);
 * System.out.println(result); // Output: Hello, World!
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class StringDeserializer implements Deserializer<String> {

    @Override
    public String deserialize(byte[] bytes) throws IOException {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
