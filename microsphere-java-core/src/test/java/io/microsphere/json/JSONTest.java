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

package io.microsphere.json;


import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import static io.microsphere.json.JSON.checkDouble;
import static io.microsphere.json.JSON.toBoolean;
import static io.microsphere.json.JSON.toDouble;
import static io.microsphere.json.JSON.toInteger;
import static io.microsphere.json.JSON.toLong;
import static io.microsphere.json.JSON.typeMismatch;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link JSON} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JSON
 * @since 1.0.0
 */
class JSONTest extends AbstractTestCase {

    @Test
    void testCheckDouble() throws Throwable {
        double d = random.nextDouble(100.0);
        assertEquals(d, checkDouble(d));
    }

    @Test
    void testCheckDoubleOnInfinite() {
        assertThrows(JSONException.class, () -> checkDouble(POSITIVE_INFINITY));
    }

    @Test
    void testCheckDoubleOnNaN() {
        assertThrows(JSONException.class, () -> checkDouble(NaN));
    }

    @Test
    void testToBoolean() {
        assertEquals(TRUE, toBoolean(TRUE));
        assertEquals(TRUE, toBoolean("true"));
        assertEquals(FALSE, toBoolean("false"));
        assertNull(toBoolean("t"));
        assertNull(toBoolean(null));
    }

    @Test
    void testToDouble() {
        assertEquals(Double.valueOf(1.0), toDouble(Double.valueOf(1.0)));
        assertEquals(Double.valueOf(1.0), toDouble("1.0"));
        assertEquals(Double.valueOf(1.0), toDouble(Integer.valueOf(1)));
        assertNull(toDouble("t"));
        assertNull(toDouble(null));
    }

    @Test
    void testToInteger() {
        assertEquals(Integer.valueOf(1), toInteger(Integer.valueOf(1)));
        assertEquals(Integer.valueOf(1), toInteger(Double.valueOf(1)));
        assertEquals(Integer.valueOf(1), toInteger("1.0"));
        assertNull(toInteger("t"));
        assertNull(toInteger(null));
    }

    @Test
    void testToLong() {
        assertEquals(Long.valueOf(1), toLong(Long.valueOf(1)));
        assertEquals(Long.valueOf(1), toLong(Double.valueOf(1)));
        assertEquals(Long.valueOf(1), toLong("1.0"));
        assertNull(toLong("t"));
        assertNull(toLong(null));
    }

    @Test
    void testToString() {
        assertEquals("1", JSON.toString("1"));
        assertEquals("1", JSON.toString(Integer.valueOf(1)));
        assertNull(JSON.toString(null));
    }

    @Test
    void testTypeMismatch() {
        assertThrows(JSONException.class, () -> typeMismatch("1", 1, "String"));
        assertThrows(JSONException.class, () -> typeMismatch(1, "String"));
    }

    @Test
    void testTypeMismatchOnActualNull() {
        assertThrows(JSONException.class, () -> typeMismatch(null, null, "String"));
        assertThrows(JSONException.class, () -> typeMismatch(null, "String"));
    }

}