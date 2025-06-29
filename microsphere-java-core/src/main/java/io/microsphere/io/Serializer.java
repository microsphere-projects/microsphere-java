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
 * A strategy interface for serializing objects of type {@code S} into a byte array.
 * <p>
 * Implementations of this interface must be thread-safe and should also ensure that the serialization process is consistent
 * and efficient.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class StringSerializer implements Serializer&lt;String&gt; {
 *     public byte[] serialize(String source) throws IOException {
 *         return source.getBytes(StandardCharsets.UTF_8);
 *     }
 * }
 * }</pre>
 *
 * @param <S> the type of the object that will be serialized
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface Serializer<S> {

    byte[] serialize(S source) throws IOException;
}
