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
import java.io.ObjectInputStream;
import java.io.Serializable;

import static io.microsphere.util.ArrayUtils.isEmpty;

/**
 * Default implementation of the {@link Deserializer} interface using Java's built-in serialization mechanism.
 *
 * <p>This class provides a default way to deserialize objects from byte arrays, leveraging
 * standard Java serialization via {@link ObjectInputStream}. The deserialization process
 * ensures that the input byte array is not empty before attempting to reconstruct the object.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * byte[] serializedData = ...; // previously serialized data
 * DefaultDeserializer deserializer = DefaultDeserializer.INSTANCE;
 * try {
 *     Object deserialized = deserializer.deserialize(serializedData);
 * } catch (IOException e) {
 *     e.printStackTrace();
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Deserializer
 * @see ObjectInputStream
 * @see Serializable
 * @since 1.0.0
 */
public class DefaultDeserializer implements Deserializer<Object> {

    public static final DefaultDeserializer INSTANCE = new DefaultDeserializer();

    @Override
    public Object deserialize(byte[] bytes) throws IOException {
        if (isEmpty(bytes)) {
            return null;
        }
        Object value = null;
        try (FastByteArrayInputStream inputStream = new FastByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
        ) {
            // byte[] -> Value
            value = objectInputStream.readObject();
        } catch (Exception e) {
            throw new IOException(e);
        }
        return value;
    }
}
