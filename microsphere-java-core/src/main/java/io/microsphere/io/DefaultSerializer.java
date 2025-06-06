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
 * Default Serializer implementation based on Java Standard Serialization.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ObjectOutputStream
 * @see Serializable
 * Date : 2021-05-02
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

