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

package io.microsphere.util;


import org.junit.jupiter.api.Test;


import static io.microsphere.util.SizeUtils.BOOLEAN_BYTES_SIZE;
import static io.microsphere.util.SizeUtils.BYTE_BYTES_SIZE;
import static io.microsphere.util.SizeUtils.CHAR_BYTES_SIZE;
import static io.microsphere.util.SizeUtils.DOUBLE_BYTES_SIZE;
import static io.microsphere.util.SizeUtils.FLOAT_BYTES_SIZE;
import static io.microsphere.util.SizeUtils.INTEGER_BYTES_SIZE;
import static io.microsphere.util.SizeUtils.LONG_BYTES_SIZE;
import static io.microsphere.util.SizeUtils.SHORT_BYTES_SIZE;
import static io.microsphere.util.SizeUtils.UNBOUND_BYTES_SIZE;
import static io.microsphere.util.SizeUtils.bytesSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link SizeUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SizeUtils
 * @since 1.0.0
 */
class SizeUtilsTest {

    @Test
    void testConstants() {
        assertEquals(-1, UNBOUND_BYTES_SIZE);
        assertEquals(1, BOOLEAN_BYTES_SIZE);
        assertEquals(1, BYTE_BYTES_SIZE);
        assertEquals(2, SHORT_BYTES_SIZE);
        assertEquals(2, CHAR_BYTES_SIZE);
        assertEquals(4, INTEGER_BYTES_SIZE);
        assertEquals(4, FLOAT_BYTES_SIZE);
        assertEquals(8, LONG_BYTES_SIZE);
        assertEquals(8, DOUBLE_BYTES_SIZE);
    }

    @Test
    void testBytesSize() {
        assertEquals(UNBOUND_BYTES_SIZE, bytesSize(Object.class));
        assertEquals(BOOLEAN_BYTES_SIZE, bytesSize(boolean.class));
        assertEquals(BOOLEAN_BYTES_SIZE, bytesSize(Boolean.class));
        assertEquals(BYTE_BYTES_SIZE, bytesSize(byte.class));
        assertEquals(BYTE_BYTES_SIZE, bytesSize(Byte.class));
        assertEquals(SHORT_BYTES_SIZE, bytesSize(short.class));
        assertEquals(SHORT_BYTES_SIZE, bytesSize(Short.class));
        assertEquals(CHAR_BYTES_SIZE, bytesSize(char.class));
        assertEquals(CHAR_BYTES_SIZE, bytesSize(Character.class));
        assertEquals(INTEGER_BYTES_SIZE, bytesSize(int.class));
        assertEquals(INTEGER_BYTES_SIZE, bytesSize(Integer.class));
        assertEquals(FLOAT_BYTES_SIZE, bytesSize(float.class));
        assertEquals(FLOAT_BYTES_SIZE, bytesSize(Float.class));
        assertEquals(LONG_BYTES_SIZE, bytesSize(long.class));
        assertEquals(LONG_BYTES_SIZE, bytesSize(Long.class));
        assertEquals(DOUBLE_BYTES_SIZE, bytesSize(double.class));
        assertEquals(DOUBLE_BYTES_SIZE, bytesSize(Double.class));
    }
}