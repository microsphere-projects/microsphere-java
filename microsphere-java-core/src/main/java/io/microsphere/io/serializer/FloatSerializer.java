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

import static io.microsphere.io.IOUtils.FLOAT_BYTES_SIZE;
import static io.microsphere.io.serializer.IntegerSerializer.INTEGER_SERIALIZER;
import static java.lang.Float.floatToIntBits;

/**
 * Java {@code char} or {@link Character} type {@link Serializer} and {@link Deserializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractSerializer
 * @since 1.0.0
 */
public class FloatSerializer extends AbstractSerializer<Float> {

    public static final FloatSerializer FLOAT_SERIALIZER = new FloatSerializer();

    @Override
    protected int calcBytesLength() {
        return FLOAT_BYTES_SIZE;
    }

    @Override
    protected byte[] doSerialize(Float aFloat) {
        int intBits = floatToIntBits(aFloat);
        return INTEGER_SERIALIZER.doSerialize(intBits);
    }

    @Override
    protected Float doDeserialize(byte[] bytes) {
        int intBits = INTEGER_SERIALIZER.doDeserialize(bytes);
        return Float.intBitsToFloat(intBits);
    }
}