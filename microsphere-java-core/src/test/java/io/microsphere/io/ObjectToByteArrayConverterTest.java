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

import io.microsphere.convert.ByteArrayToObjectConverter;
import io.microsphere.convert.ObjectToByteArrayConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

/**
 * {@link ObjectToByteArrayConverter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ObjectToByteArrayConverter
 * @since 1.0.0
 */
public class ObjectToByteArrayConverterTest {
    ObjectToByteArrayConverter converter = ObjectToByteArrayConverter.INSTANCE;

    ByteArrayToObjectConverter converter2 = ByteArrayToObjectConverter.INSTANCE;

    @Test
    public void test() {
        String source = "Hello,World";
        byte[] bytes = converter.convert(source);
        Object target = converter2.convert(bytes);
        assertEquals(source, target);
    }

    @Test
    public void testOnFailed() {
        assertThrowsExactly(RuntimeException.class, () -> converter.convert(new Object()));
        assertThrowsExactly(RuntimeException.class, () -> converter2.convert(new byte[0]));
    }
}
