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

/**
 * A functional interface for deserializing byte arrays into objects of type {@code T}.
 * <p>
 * Implementations of this interface must be thread-safe.
 * </p>
 *
 * <h3>Example usage</h3>
 * <pre>{@code
 * Deserializer<String> deserializer = bytes -> new String(bytes, StandardCharsets.UTF_8);
 * String data = deserializer.deserialize("Hello, World!".getBytes(StandardCharsets.UTF_8));
 * System.out.println(data); // Output: Hello, World!
 * }</pre>
 * </p>
 *
 * @param <T> the type to be deserialized from a byte array
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Serializer
 * @since 1.0.0
 */
@FunctionalInterface
public interface Deserializer<T> {

    T deserialize(byte[] bytes) throws IOException;
}
