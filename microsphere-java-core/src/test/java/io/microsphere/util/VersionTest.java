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
import org.junit.jupiter.api.Test;

import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.constants.SymbolConstants.HYPHEN;
import static io.microsphere.constants.SymbolConstants.SPACE;
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
class VersionTest extends AbstractTestCase {

    private static final int MAJOR = 1;

    private static final int MINOR = 2;

    private static final int PATCH = 3;

    private static final String ALPHA_PRE_RELEASE = "alpha";

    private static final String BETA_PRE_RELEASE = "beta";

    private static final String VERSION = MAJOR + DOT + MINOR + DOT + PATCH;

    private static final String ALPHA_PRE_RELEASE_VERSION = VERSION + HYPHEN + ALPHA_PRE_RELEASE;

    private static final String BETA_PRE_RELEASE_VERSION = VERSION + HYPHEN + BETA_PRE_RELEASE;

    private static final Version TEST_VERSION = of(VERSION);

    private static final Version TEST_ALPHA_PRE_RELEASE_VERSION = of(ALPHA_PRE_RELEASE_VERSION);

    private static final Version TEST_BETA_PRE_RELEASE_VERSION = of(BETA_PRE_RELEASE_VERSION);

    @Test
    void testGetValue() {
        assertEquals(MAJOR, getValue("1"));
        assertEquals(MINOR, getValue("2"));
        assertEquals(PATCH, getValue("3"));

        assertEquals(MAJOR, getValue(" 1"));
        assertEquals(MINOR, getValue("2 "));
        assertEquals(PATCH, getValue(" 3 "));
    }

    @Test
    void testGetValueOnFailed() {
        assertThrows(IllegalArgumentException.class, () -> getValue(""));
        assertThrows(IllegalArgumentException.class, () -> getValue(SPACE));
        assertThrows(IllegalArgumentException.class, () -> getValue("a"));
    }

    @Test
    void testOfWithMajor() {
        Version version = of(MAJOR);
        assertEquals(MAJOR, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(0, version.getPatch());
    }

    @Test
    void testOfWithMajorAndMinor() {
        Version version = of(MAJOR, MINOR);
        assertEquals(MAJOR, version.getMajor());
        assertEquals(MINOR, version.getMinor());
        assertEquals(0, version.getPatch());
    }

    @Test
    void testOfWithMajorAndMinorAndPatch() {
        Version version = of(MAJOR, MINOR, PATCH);
        assertEquals(MAJOR, version.getMajor());
        assertEquals(MINOR, version.getMinor());
        assertEquals(PATCH, version.getPatch());
    }

    @Test
    void testOfWithVersion() {
        Version version = of(VERSION);
        assertEquals(MAJOR, version.getMajor());
        assertEquals(MINOR, version.getMinor());
        assertEquals(PATCH, version.getPatch());
    }

    @Test
    void testOfOnNullPointException() {
        assertThrows(IllegalArgumentException.class, () -> of(null));
    }

    @Test
    void testOfOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> of(SPACE));
    }

    @Test
    void testInvalidVersion() {
        assertThrows(IllegalArgumentException.class, () -> of(-1, 2, 3));
        assertThrows(IllegalArgumentException.class, () -> of(1, -1, 3));
        assertThrows(IllegalArgumentException.class, () -> of(1, 2, -1));
        assertThrows(IllegalArgumentException.class, () -> of(0, 0, 0));
    }

    @Test
    void testEquals() {
        assertTrue(TEST_VERSION.equals(TEST_VERSION));
        assertTrue(TEST_VERSION.equals((Object) TEST_VERSION));
        assertTrue(TEST_VERSION.equals(of(MAJOR, MINOR, PATCH)));
        assertTrue(TEST_ALPHA_PRE_RELEASE_VERSION.equals(of(MAJOR, MINOR, PATCH, ALPHA_PRE_RELEASE)));
        assertTrue(TEST_BETA_PRE_RELEASE_VERSION.equals(of(MAJOR, MINOR, PATCH, BETA_PRE_RELEASE)));

        assertFalse(TEST_VERSION.equals("test"));
        assertFalse(TEST_VERSION.equals((Object) of(MAJOR)));
        assertFalse(TEST_VERSION.equals((Object) of(MAJOR, MINOR)));
        assertFalse(TEST_VERSION.equals((Object) TEST_ALPHA_PRE_RELEASE_VERSION));
        assertFalse(TEST_BETA_PRE_RELEASE_VERSION.equals((Object) TEST_ALPHA_PRE_RELEASE_VERSION));
    }

    @Test
    void testHashCode() {
        assertEquals(TEST_VERSION.hashCode(), TEST_VERSION.hashCode());
        assertEquals(TEST_ALPHA_PRE_RELEASE_VERSION.hashCode(), TEST_ALPHA_PRE_RELEASE_VERSION.hashCode());
    }

    @Test
    void testEqualsOnNull() {
        assertFalse(TEST_VERSION.equals(null));
        assertFalse(TEST_VERSION.equals((Object) null));
    }

    @Test
    void testEq() {
        assertTrue(TEST_VERSION.eq(TEST_VERSION));
        assertTrue(TEST_VERSION.eq(of(MAJOR, MINOR, PATCH)));
    }

    @Test
    void testEqOnNull() {
        assertFalse(TEST_VERSION.eq(null));
        assertFalse(TEST_VERSION.equals(null));
    }

    @Test
    void testGt() {
        assertFalse(TEST_VERSION.gt(TEST_VERSION));

        assertFalse(TEST_VERSION.gt(of(MAJOR, MINOR, PATCH)));
        assertTrue(TEST_VERSION.gt(of(MAJOR, MINOR, PATCH - 1)));
        assertTrue(TEST_VERSION.gt(of(MAJOR, MINOR, PATCH - 2)));
        assertTrue(TEST_VERSION.gt(of(MAJOR, MINOR, PATCH - 3)));
        assertTrue(TEST_VERSION.gt(of(MAJOR, MINOR)));
        assertTrue(TEST_VERSION.gt(of(MAJOR, MINOR - 1)));
        assertTrue(TEST_VERSION.gt(of(MAJOR, MINOR - 2)));
        assertTrue(TEST_VERSION.gt(of(MAJOR)));

        assertFalse(TEST_VERSION.gt(of(MAJOR + 1)));
        assertFalse(TEST_VERSION.gt(of(MAJOR, MINOR + 1)));
        assertFalse(TEST_VERSION.gt(of(MAJOR, MINOR, PATCH + 1)));

        assertTrue(TEST_VERSION.gt(TEST_ALPHA_PRE_RELEASE_VERSION));
        assertTrue(TEST_VERSION.gt(TEST_BETA_PRE_RELEASE_VERSION));
        assertTrue(TEST_BETA_PRE_RELEASE_VERSION.gt(TEST_ALPHA_PRE_RELEASE_VERSION));
    }

    @Test
    void testGtOnNull() {
        assertFalse(TEST_VERSION.gt(null));
    }

    @Test
    void testGe() {
        assertTrue(TEST_VERSION.ge(TEST_VERSION));

        assertTrue(TEST_VERSION.ge(of(MAJOR, MINOR, PATCH)));
        assertTrue(TEST_VERSION.ge(of(MAJOR, MINOR, PATCH - 1)));
        assertTrue(TEST_VERSION.ge(of(MAJOR, MINOR, PATCH - 2)));
        assertTrue(TEST_VERSION.ge(of(MAJOR, MINOR, PATCH - 3)));
        assertTrue(TEST_VERSION.ge(of(MAJOR, MINOR)));
        assertTrue(TEST_VERSION.ge(of(MAJOR, MINOR - 1)));
        assertTrue(TEST_VERSION.ge(of(MAJOR, MINOR - 2)));
        assertTrue(TEST_VERSION.ge(of(MAJOR)));

        assertFalse(TEST_VERSION.ge(of(MAJOR + 1)));
        assertFalse(TEST_VERSION.ge(of(MAJOR, MINOR + 1)));
        assertFalse(TEST_VERSION.ge(of(MAJOR, MINOR, PATCH + 1)));

        assertTrue(TEST_VERSION.ge(TEST_ALPHA_PRE_RELEASE_VERSION));
        assertTrue(TEST_VERSION.ge(TEST_BETA_PRE_RELEASE_VERSION));
        assertTrue(TEST_BETA_PRE_RELEASE_VERSION.ge(TEST_ALPHA_PRE_RELEASE_VERSION));
    }

    @Test
    void testGeOnNull() {
        assertFalse(TEST_VERSION.ge(null));
    }

    @Test
    void testLt() {
        assertFalse(TEST_VERSION.lt(TEST_VERSION));

        assertFalse(TEST_VERSION.lt(of(MAJOR, MINOR, PATCH)));
        assertFalse(TEST_VERSION.lt(of(MAJOR, MINOR, PATCH - 1)));
        assertFalse(TEST_VERSION.lt(of(MAJOR, MINOR, PATCH - 2)));
        assertFalse(TEST_VERSION.lt(of(MAJOR, MINOR, PATCH - 3)));
        assertFalse(TEST_VERSION.lt(of(MAJOR, MINOR)));
        assertFalse(TEST_VERSION.lt(of(MAJOR, MINOR - 1)));
        assertFalse(TEST_VERSION.lt(of(MAJOR, MINOR - 2)));
        assertFalse(TEST_VERSION.lt(of(MAJOR)));

        assertTrue(TEST_VERSION.lt(of(MAJOR + 1)));
        assertTrue(TEST_VERSION.lt(of(MAJOR, MINOR + 1)));
        assertTrue(TEST_VERSION.lt(of(MAJOR, MINOR, PATCH + 1)));

        assertTrue(TEST_ALPHA_PRE_RELEASE_VERSION.lt(TEST_VERSION));
        assertTrue(TEST_BETA_PRE_RELEASE_VERSION.lt(TEST_VERSION));
        assertTrue(TEST_ALPHA_PRE_RELEASE_VERSION.lt(TEST_BETA_PRE_RELEASE_VERSION));
        assertFalse(TEST_ALPHA_PRE_RELEASE_VERSION.lt(of(MAJOR, MINOR)));
        assertFalse(TEST_ALPHA_PRE_RELEASE_VERSION.lt(of(MAJOR, MINOR, PATCH - 1)));
        assertFalse(TEST_ALPHA_PRE_RELEASE_VERSION.lt(of(MAJOR, MINOR, PATCH - 2)));
        assertFalse(TEST_ALPHA_PRE_RELEASE_VERSION.lt(of(MAJOR)));
    }

    @Test
    void testLtOnNull() {
        assertFalse(TEST_VERSION.lt(null));
    }

    @Test
    void testLe() {
        assertTrue(TEST_VERSION.le(TEST_VERSION));

        assertTrue(TEST_VERSION.le(of(MAJOR, MINOR, PATCH)));
        assertFalse(TEST_VERSION.le(of(MAJOR, MINOR, PATCH - 1)));
        assertFalse(TEST_VERSION.le(of(MAJOR, MINOR, PATCH - 2)));
        assertFalse(TEST_VERSION.le(of(MAJOR, MINOR, PATCH - 3)));
        assertFalse(TEST_VERSION.le(of(MAJOR, MINOR)));
        assertFalse(TEST_VERSION.le(of(MAJOR, MINOR - 1)));
        assertFalse(TEST_VERSION.le(of(MAJOR, MINOR - 2)));
        assertFalse(TEST_VERSION.le(of(MAJOR)));

        assertTrue(TEST_VERSION.le(of(MAJOR + 1)));
        assertTrue(TEST_VERSION.le(of(MAJOR, MINOR + 1)));
        assertTrue(TEST_VERSION.le(of(MAJOR, MINOR, PATCH + 1)));

        assertTrue(TEST_BETA_PRE_RELEASE_VERSION.le(TEST_VERSION));
        assertTrue(TEST_ALPHA_PRE_RELEASE_VERSION.le(TEST_BETA_PRE_RELEASE_VERSION));
        assertFalse(TEST_ALPHA_PRE_RELEASE_VERSION.le(of(MAJOR, MINOR)));
        assertFalse(TEST_ALPHA_PRE_RELEASE_VERSION.le(of(MAJOR, MINOR, PATCH - 1)));
        assertFalse(TEST_ALPHA_PRE_RELEASE_VERSION.le(of(MAJOR, MINOR, PATCH - 2)));
        assertFalse(TEST_ALPHA_PRE_RELEASE_VERSION.le(of(MAJOR)));

        assertFalse(TEST_VERSION.le(TEST_ALPHA_PRE_RELEASE_VERSION));
        assertFalse(TEST_VERSION.le(TEST_BETA_PRE_RELEASE_VERSION));
        assertFalse(TEST_BETA_PRE_RELEASE_VERSION.le(TEST_ALPHA_PRE_RELEASE_VERSION));
    }

    @Test
    void testLeOnNull() {
        assertFalse(TEST_VERSION.le(null));
    }

    @Test
    void testGetVersion() {
        Version version = getVersion(Test.class);
        assertNotNull(version);
    }

    @Test
    void testGetVersionOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> getVersion(VersionTest.class));
    }

    @Test
    void testToString() {
        assertToString("1.0.0");
        assertToString("1.2.3-alpha");
        assertToString("4.5.6-beta");
        assertToString("7.8.9-RC1");
    }

    @Test
    void testGetPreRelease() {
        assertEquals("alpha", TEST_ALPHA_PRE_RELEASE_VERSION.getPreRelease());
        assertEquals("beta", TEST_BETA_PRE_RELEASE_VERSION.getPreRelease());
    }

    void assertToString(String version) {
        assertEquals(version, of(version).toString());
    }
}
