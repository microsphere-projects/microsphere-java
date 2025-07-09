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

import io.microsphere.io.DefaultSerializer;
import org.junit.jupiter.api.Test;

import static io.microsphere.convert.ObjectToByteArrayConverter.INSTANCE;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ObjectToByteArrayConverter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ObjectToByteArrayConverter
 * @since 1.0.0
 */
class ObjectToByteArrayConverterTest extends BaseConverterTest<Object, byte[]> {

    @Override
    protected AbstractConverter<Object, byte[]> createConverter() {
        return INSTANCE;
    }

    @Override
    protected Object getSource() {
        return "Hello,World";
    }

    @Override
    protected byte[] getTarget() throws Throwable {
        return DefaultSerializer.INSTANCE.serialize(getSource());
    }

    @Test
    void testCovertOnFailed() {
        assertThrows(RuntimeException.class, () -> converter.convert(new Object()));
    }
}
