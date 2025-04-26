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

import static io.microsphere.convert.ByteArrayToObjectConverter.INSTANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

/**
 * {@link ObjectToByteArrayConverter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ObjectToByteArrayConverter
 * @since 1.0.0
 */
public class ObjectToByteArrayConverterTest {

    private ByteArrayToObjectConverter instance = INSTANCE;

    @Test
    public void testCovert() {
        ObjectToByteArrayConverter converter = ObjectToByteArrayConverter.INSTANCE;
        String source = "Hello,World";
        byte[] bytes = converter.convert(source);
        Object target = instance.convert(bytes);
        assertEquals(source, target);
    }

    @Test
    public void testCovertOnNull() {
        assertNull(instance.convert(null));
        assertNull(instance.convert(new byte[0]));
    }

    @Test
    public void testCovertOnFailed() {
        assertThrowsExactly(RuntimeException.class, () -> instance.convert(new byte[]{1}));
    }
}
