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

import io.microsphere.util.Version.Operator;
import org.junit.jupiter.api.Test;

import static io.microsphere.util.Version.Operator.EQ;
import static io.microsphere.util.Version.Operator.GE;
import static io.microsphere.util.Version.Operator.GT;
import static io.microsphere.util.Version.Operator.LE;
import static io.microsphere.util.Version.Operator.LT;
import static io.microsphere.util.Version.Operator.of;
import static io.microsphere.util.Version.ofVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The {@link Operator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Operator
 * @since 1.0.0
 */
class VersionOperatorTest {

    private static final Version TEST_VERSION = ofVersion("1.2.3");

    @Test
    void testOf() {
        assertEquals(EQ, of("="));
        assertEquals(LT, of("<"));
        assertEquals(LE, of("<="));
        assertEquals(GT, of(">"));
        assertEquals(GE, of(">="));
    }

    @Test
    void test() {
        assertTrue(EQ.test(TEST_VERSION, TEST_VERSION));
        assertFalse(EQ.test(TEST_VERSION, ofVersion("1.2.4")));

        assertTrue(LT.test(TEST_VERSION, ofVersion("1.2.4")));
        assertFalse(LT.test(TEST_VERSION, TEST_VERSION));

        assertTrue(LE.test(TEST_VERSION, ofVersion("1.2.4")));
        assertTrue(LE.test(TEST_VERSION, TEST_VERSION));

        assertFalse(GT.test(TEST_VERSION, ofVersion("1.2.4")));
        assertTrue(GT.test(TEST_VERSION, ofVersion("1.2.2")));

        assertFalse(GE.test(TEST_VERSION, ofVersion("1.2.4")));
        assertTrue(GE.test(TEST_VERSION, TEST_VERSION));
    }

    @Test
    void testWithNull() {
        assertTrue(EQ.test(null, null));
        assertFalse(EQ.test(TEST_VERSION, null));
        assertFalse(EQ.test(null, TEST_VERSION));

        assertFalse(LT.test(null, null));
        assertFalse(LT.test(TEST_VERSION, null));
        assertFalse(LT.test(null, TEST_VERSION));

        assertTrue(LE.test(null, null));
        assertFalse(LE.test(TEST_VERSION, null));
        assertFalse(LE.test(null, TEST_VERSION));

        assertFalse(GT.test(null, null));
        assertFalse(GT.test(TEST_VERSION, null));
        assertFalse(GT.test(null, TEST_VERSION));

        assertTrue(GE.test(null, null));
        assertFalse(GE.test(TEST_VERSION, null));
        assertFalse(GE.test(null, TEST_VERSION));
    }

    @Test
    void testOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> of(""));
        assertThrows(IllegalArgumentException.class, () -> of("@"));
        assertThrows(IllegalArgumentException.class, () -> of("-"));
    }
}
