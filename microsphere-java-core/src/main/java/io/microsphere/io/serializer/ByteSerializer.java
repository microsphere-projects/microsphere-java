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

import java.io.IOException;

import static io.microsphere.util.SizeUtils.BYTE_BYTES_SIZE;

/**
 * Java {@code byte} or {@link Byte} type {@link Serializer} and {@link Deserializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractSerializer
 * @since 1.0.0
 */
public class ByteSerializer extends AbstractSerializer<Byte> {

    public static final ByteSerializer BYTE_SERIALIZER = new ByteSerializer();

    @Override
    protected int calcBytesLength() {
        return BYTE_BYTES_SIZE;
    }

    @Override
    protected byte[] doSerialize(Byte b) throws IOException {
        return new byte[]{b};
    }

    @Override
    protected Byte doDeserialize(byte[] bytes) throws IOException {
        return bytes[0];
    }
}
