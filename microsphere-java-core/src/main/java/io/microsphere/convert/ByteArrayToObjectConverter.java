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
package io.microsphere.convert;

import io.microsphere.io.DefaultDeserializer;

import java.io.IOException;
import java.io.Serializable;


/**
 * The class coverts the {@link byte[] byte array} object to be a {@link Object} instance .
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Serializable
 * @since 1.0.0
 */
public class ByteArrayToObjectConverter implements Converter<byte[], Object> {

    /**
     * The Singleton instance of {@link ByteArrayToObjectConverter}
     */
    public static final ByteArrayToObjectConverter INSTANCE = new ByteArrayToObjectConverter();

    @Override
    public Serializable convert(byte[] source) {
        try {
            return (Serializable) DefaultDeserializer.INSTANCE.deserialize(source);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
