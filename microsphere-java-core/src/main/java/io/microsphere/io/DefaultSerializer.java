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
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A default implementation of the {@link Serializer} interface that uses Java's standard serialization mechanism.
 * <p>
 * This class leverages Java's built-in serialization through an {@link ObjectOutputStream} to serialize objects into a byte array.
 * It is designed to work with any object that implements the {@link Serializable} interface, ensuring compatibility with Java's
 * serialization framework.
 * </p>
 *
 * <h3>Example usage</h3>
 * <pre>{@code
 * // Create an instance of DefaultSerializer
 * Serializer<MySerializableObject> serializer = new DefaultSerializer();
 *
 * // Serialize an object
 * MySerializableObject obj = new MySerializableObject("example");
 * byte[] serializedData = serializer.serialize(obj);
 * }</pre>
 * </p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class DefaultSerializer implements Serializer<Object> {

    public static final DefaultSerializer INSTANCE = new DefaultSerializer();

    @Override
    public byte[] serialize(Object source) throws IOException {
        byte[] bytes = null;
        try (FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)
        ) {
            // Key -> byte[]
            objectOutputStream.writeObject(source);
            bytes = outputStream.toByteArray();
        }
        return bytes;
    }
}

