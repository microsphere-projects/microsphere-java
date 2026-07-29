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

import java.util.Map;

import static io.microsphere.collection.MapUtils.ofMap;

/**
 * The utilities class for Size
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Utils
 * @since 1.0.0
 */
public abstract class SizeUtils implements Utils {

    /**
     * The size in bytes of an unbounded type.
     */
    public static final int UNBOUND_BYTES_SIZE = -1;

    /**
     * The size in bytes of <code>boolean</code> type.
     */
    public static final int BOOLEAN_BYTES_SIZE = 1;

    /**
     * The size in bytes of <code>byte</code> type.
     */
    public static final int BYTE_BYTES_SIZE = Byte.BYTES;

    /**
     * The size in bytes of <code>short</code> type.
     */
    public static final int SHORT_BYTES_SIZE = Short.BYTES;

    /**
     * The size in bytes of <code>char</code> type.
     */
    public static final int CHAR_BYTES_SIZE = Character.BYTES;

    /**
     * The size in bytes of <code>int</code> type.
     */
    public static final int INTEGER_BYTES_SIZE = Integer.BYTES;

    /**
     * The size in bytes of <code>float</code> type.
     */
    public static final int FLOAT_BYTES_SIZE = Float.BYTES;

    /**
     * The size in bytes of <code>long</code> type.
     */
    public static final int LONG_BYTES_SIZE = Long.BYTES;

    /**
     * The size in bytes of <code>double</code> type.
     */
    public static final int DOUBLE_BYTES_SIZE = Double.BYTES;

    private static final Map<Class<?>, Integer> BYTES_SIZE_MAP = ofMap(
            boolean.class, BOOLEAN_BYTES_SIZE,
            byte.class, BYTE_BYTES_SIZE,
            short.class, SHORT_BYTES_SIZE,
            char.class, CHAR_BYTES_SIZE,
            int.class, INTEGER_BYTES_SIZE,
            float.class, FLOAT_BYTES_SIZE,
            long.class, LONG_BYTES_SIZE,
            double.class, DOUBLE_BYTES_SIZE,
            Boolean.class, BOOLEAN_BYTES_SIZE,
            Byte.class, BYTE_BYTES_SIZE,
            Short.class, SHORT_BYTES_SIZE,
            Character.class, CHAR_BYTES_SIZE,
            Integer.class, INTEGER_BYTES_SIZE,
            Float.class, FLOAT_BYTES_SIZE,
            Long.class, LONG_BYTES_SIZE,
            Double.class, DOUBLE_BYTES_SIZE
    );

    /**
     * Returns the size in bytes of the specified type.
     *
     * @param type the type to get the size in bytes for
     * @return the size in bytes of the specified type, or {@link #UNBOUND_BYTES_SIZE} if the type is unbounded
     */
    public static int bytesSize(Class<?> type) {
        return BYTES_SIZE_MAP.getOrDefault(type, UNBOUND_BYTES_SIZE);
    }

    private SizeUtils() {
    }
}
