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
import org.junit.Test;

import static io.microsphere.util.Version.getValue;
import static io.microsphere.util.Version.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link Version} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class VersionTest extends AbstractTestCase {

    @Test
    public void testGetValue() {
        assertEquals(1, getValue("1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueOnFailed() {
        assertEquals(1, getValue("a"));
    }

    @Test(expected = NullPointerException.class)
    public void testOfOnNullPointException() {
        of(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOfOnIllegalArgumentException() {
        of(" ");
    }

    @Test
    public void testOf() {
        Version version = of("1.2.3");
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(3, version.getPatch());
    }

    @Test
    public void testEquals() {
        Version version = of("1.2.3");
        assertTrue(version.eq(of("1.2.3")));
        assertTrue(version.equals((Object) of("1.2.3")));
    }

    @Test
    public void testGt() {
        Version version = of("1.2.3");
        assertFalse(version.gt(of("1.2.3")));
        assertTrue(version.gt(of("1.2.2")));
        assertTrue(version.gt(of("1.2.1")));
        assertTrue(version.gt(of("1.2.0")));
        assertTrue(version.gt(of("1.2")));
        assertTrue(version.gt(of("1.1.0")));
        assertTrue(version.gt(of("1.1")));
        assertTrue(version.gt(of("1")));
    }

    @Test
    public void testGe() {
        Version version = of("1.2.3");
        assertTrue(version.ge(of("1.2.3")));
        assertTrue(version.gt(of("1.2.2")));
        assertTrue(version.gt(of("1.2.1")));
        assertTrue(version.gt(of("1.2.0")));
        assertTrue(version.gt(of("1.2")));
        assertTrue(version.gt(of("1.1.0")));
        assertTrue(version.gt(of("1.1")));
        assertTrue(version.gt(of("1")));
    }

    @Test
    public void testLt() {
        Version version = of("1.2.3");
        assertFalse(version.lt(of("1.2.3")));
        assertFalse(version.lt(of("1.2.2")));
        assertFalse(version.lt(of("1.2.1")));
        assertFalse(version.lt(of("1.2.0")));
        assertFalse(version.lt(of("1.2")));
        assertFalse(version.lt(of("1.1.0")));
        assertFalse(version.lt(of("1.1")));
        assertFalse(version.lt(of("1")));
    }

    @Test
    public void testLe() {
        Version version = of("1.2.3");
        assertTrue(version.le(of("1.2.3")));
        assertFalse(version.le(of("1.2.2")));
        assertFalse(version.le(of("1.2.1")));
        assertFalse(version.le(of("1.2.0")));
        assertFalse(version.le(of("1.2")));
        assertFalse(version.le(of("1.1.0")));
        assertFalse(version.le(of("1.1")));
        assertFalse(version.le(of("1")));
    }

    @Test
    public void testToString() {
        assertEquals("Version{major=1, minor=2, patch=3}", of("1.2.3").toString());
    }
}
