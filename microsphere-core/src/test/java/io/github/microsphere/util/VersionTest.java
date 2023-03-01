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
package io.github.microsphere.util;

import io.github.microsphere.AbstractTestCase;
import org.junit.Test;

import static io.github.microsphere.util.Version.getValue;
import static io.github.microsphere.util.Version.of;
import static org.junit.Assert.assertEquals;

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
}
