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

import io.microsphere.AbstractTestCase;
import io.microsphere.util.Version.Operator;
import org.junit.jupiter.api.Test;

import static io.microsphere.util.Version.Operator.EQ;
import static io.microsphere.util.Version.Operator.GE;
import static io.microsphere.util.Version.Operator.GT;
import static io.microsphere.util.Version.Operator.LE;
import static io.microsphere.util.Version.Operator.LT;
import static io.microsphere.util.Version.getValue;
import static io.microsphere.util.Version.getVersion;
import static io.microsphere.util.Version.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Version} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Version
 * @since 1.0.0
 */
public class VersionTest extends AbstractTestCase {

    private static final int MAJOR = 1;

    private static final int MINOR = 2;

    private static final int PATCH = 3;

    private static final String VERSION = MAJOR + "." + MINOR + "." + PATCH;

    private static final Version TEST_VERSION = of(VERSION);

    @Test
    public void testGetValue() {
        assertEquals(MAJOR, getValue("1"));
        assertEquals(MINOR, getValue("2"));
        assertEquals(PATCH, getValue("3"));

        assertEquals(MAJOR, getValue(" 1"));
        assertEquals(MINOR, getValue("2 "));
        assertEquals(PATCH, getValue(" 3 "));
    }

    @Test
    public void testGetValueOnFailed() {
        assertThrows(IllegalArgumentException.class, () -> getValue(""));
        assertThrows(IllegalArgumentException.class, () -> getValue(" "));
        assertThrows(IllegalArgumentException.class, () -> getValue("a"));
    }

    @Test
    public void testOfWithMajor() {
        Version version = of(MAJOR);
        assertEquals(MAJOR, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(0, version.getPatch());
    }

    @Test
    public void testOfWithMajorAndMinor() {
        Version version = of(MAJOR, MINOR);
        assertEquals(MAJOR, version.getMajor());
        assertEquals(MINOR, version.getMinor());
        assertEquals(0, version.getPatch());
    }

    @Test
    public void testOfWithMajorAndMinorAndPatch() {
        Version version = of(MAJOR, MINOR, PATCH);
        assertEquals(MAJOR, version.getMajor());
        assertEquals(MINOR, version.getMinor());
        assertEquals(PATCH, version.getPatch());
    }

    @Test
    public void testOfWithVersion() {
        Version version = of(VERSION);
        assertEquals(MAJOR, version.getMajor());
        assertEquals(MINOR, version.getMinor());
        assertEquals(PATCH, version.getPatch());
    }

    @Test
    public void testOfOnNullPointException() {
        assertThrows(IllegalArgumentException.class, () -> of(null));
    }

    @Test
    public void testOfOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> of(" "));
    }

    @Test
    public void testEquals() {
        assertTrue(TEST_VERSION.equals(TEST_VERSION));
        assertTrue(TEST_VERSION.equals((Object) TEST_VERSION));
        assertTrue(TEST_VERSION.equals(of("1.2.3")));
    }

    @Test
    public void testEqualsOnNull() {
        assertFalse(TEST_VERSION.equals(null));
        assertFalse(TEST_VERSION.equals((Object) null));
    }

    @Test
    public void testEq() {
        assertTrue(TEST_VERSION.eq(TEST_VERSION));
        assertTrue(TEST_VERSION.eq(of("1.2.3")));
    }

    @Test
    public void testEqOnNull() {
        assertFalse(TEST_VERSION.eq(null));
        assertFalse(TEST_VERSION.equals(null));
    }

    @Test
    public void testGt() {
        assertFalse(TEST_VERSION.gt(TEST_VERSION));
        assertFalse(TEST_VERSION.gt(of("1.2.3")));
        assertTrue(TEST_VERSION.gt(of("1.2.2")));
        assertTrue(TEST_VERSION.gt(of("1.2.1")));
        assertTrue(TEST_VERSION.gt(of("1.2.0")));
        assertTrue(TEST_VERSION.gt(of("1.2")));
        assertTrue(TEST_VERSION.gt(of("1.1.0")));
        assertTrue(TEST_VERSION.gt(of("1.1")));
        assertTrue(TEST_VERSION.gt(of("1")));
    }

    @Test
    public void testGtOnNull() {
        assertFalse(TEST_VERSION.gt(null));
    }

    @Test
    public void testGe() {
        assertTrue(TEST_VERSION.ge(TEST_VERSION));
        assertTrue(TEST_VERSION.ge(of("1.2.3")));
        assertTrue(TEST_VERSION.gt(of("1.2.2")));
        assertTrue(TEST_VERSION.gt(of("1.2.1")));
        assertTrue(TEST_VERSION.gt(of("1.2.0")));
        assertTrue(TEST_VERSION.gt(of("1.2")));
        assertTrue(TEST_VERSION.gt(of("1.1.0")));
        assertTrue(TEST_VERSION.gt(of("1.1")));
        assertTrue(TEST_VERSION.gt(of("1")));
    }

    @Test
    public void testGeOnNull() {
        assertFalse(TEST_VERSION.ge(null));
    }

    @Test
    public void testLt() {
        assertFalse(TEST_VERSION.lt(TEST_VERSION));
        assertFalse(TEST_VERSION.lt(of("1.2.3")));
        assertFalse(TEST_VERSION.lt(of("1.2.2")));
        assertFalse(TEST_VERSION.lt(of("1.2.1")));
        assertFalse(TEST_VERSION.lt(of("1.2.0")));
        assertFalse(TEST_VERSION.lt(of("1.2")));
        assertFalse(TEST_VERSION.lt(of("1.1.0")));
        assertFalse(TEST_VERSION.lt(of("1.1")));
        assertFalse(TEST_VERSION.lt(of("1")));
    }

    @Test
    public void testLtOnNull() {
        assertFalse(TEST_VERSION.lt(null));
    }

    @Test
    public void testLe() {
        assertTrue(TEST_VERSION.le(TEST_VERSION));
        assertTrue(TEST_VERSION.le(of("1.2.3")));
        assertFalse(TEST_VERSION.le(of("1.2.2")));
        assertFalse(TEST_VERSION.le(of("1.2.1")));
        assertFalse(TEST_VERSION.le(of("1.2.0")));
        assertFalse(TEST_VERSION.le(of("1.2")));
        assertFalse(TEST_VERSION.le(of("1.1.0")));
        assertFalse(TEST_VERSION.le(of("1.1")));
        assertFalse(TEST_VERSION.le(of("1")));
    }

    @Test
    public void testLeOnNull() {
        assertFalse(TEST_VERSION.le(null));
    }

    @Test
    public void testGetVersion() {
        Version version = getVersion(Test.class);
        assertNotNull(version);
    }

    @Test
    public void testGetVersionOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> getVersion(VersionTest.class));
    }

    @Test
    public void testOperator() {
        assertEquals(EQ, Operator.of("="));
        assertEquals(LT, Operator.of("<"));
        assertEquals(LE, Operator.of("<="));
        assertEquals(GT, Operator.of(">"));
        assertEquals(GE, Operator.of(">="));
    }

    @Test
    public void testToString() {
        assertEquals("Version{major=1, minor=2, patch=3}", TEST_VERSION.toString());
    }
}
