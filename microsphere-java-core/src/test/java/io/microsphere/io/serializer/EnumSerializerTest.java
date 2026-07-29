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


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.values;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link EnumSerializer}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnumSerializer
 * @since 1.0.0
 */
class EnumSerializerTest {

    @Test
    void test() throws IOException {
        EnumSerializer<TimeUnit> serializer = new EnumSerializer<>(TimeUnit.class);
        assertSame(TimeUnit.class, serializer.getEnumType());
        assertSame(TimeUnit.class, serializer.getTargetType());
        assertEquals(1, serializer.getBytesLength());
        for (TimeUnit timeUnit : values()) {
            String name = timeUnit.name();
            byte[] bytes = serializer.serialize(timeUnit);
            TimeUnit deserializedTimeUnit = serializer.deserialize(bytes);
            assertEquals(timeUnit, deserializedTimeUnit);
            assertEquals(name, deserializedTimeUnit.name());
        }

        assertNull(serializer.serialize(null));
        assertNull(serializer.deserialize(null));
    }

    @Test
    void test2() throws IOException {
        EnumSerializer<E> serializer = new EnumSerializer<>(E.class);
        assertSame(E.class, serializer.getEnumType());
        assertEquals(2, serializer.getBytesLength());
        for (E e : E.values()) {
            String name = e.name();
            byte[] bytes = serializer.serialize(e);
            E deserializedE = serializer.deserialize(bytes);
            assertEquals(e, deserializedE);
            assertEquals(name, deserializedE.name());
        }
    }

    static enum E {
        E1, E2, E3, E4, E5, E6, E7, E8, E9, E10,
        E11, E12, E13, E14, E15, E16, E17, E18, E19, E20,
        E21, E22, E23, E24, E25, E26, E27, E28, E29, E30,
        E31, E32, E33, E34, E35, E36, E37, E38, E39, E40,
        E41, E42, E43, E44, E45, E46, E47, E48, E49, E50,
        E51, E52, E53, E54, E55, E56, E57, E58, E59, E60,
        E61, E62, E63, E64, E65, E66, E67, E68, E69, E70,
        E71, E72, E73, E74, E75, E76, E77, E78, E79, E80,
        E81, E82, E83, E84, E85, E86, E87, E88, E89, E90,
        E91, E92, E93, E94, E95, E96, E97, E98, E99, E100,
        E101, E102, E103, E104, E105, E106, E107, E108, E109, E110,
        E111, E112, E113, E114, E115, E116, E117, E118, E119, E120,
        E121, E122, E123, E124, E125, E126, E127, E128, E129, E130,
        E131, E132, E133, E134, E135, E136, E137, E138, E139, E140,
        E141, E142, E143, E144, E145, E146, E147, E148, E149, E150,
        E151, E152, E153, E154, E155, E156, E157, E158, E159, E160,
        E161, E162, E163, E164, E165, E166, E167, E168, E169, E170,
        E171, E172, E173, E174, E175, E176, E177, E178, E179, E180,
        E181, E182, E183, E184, E185, E186, E187, E188, E189, E190,
        E191, E192, E193, E194, E195, E196, E197, E198, E199, E200
    }
}