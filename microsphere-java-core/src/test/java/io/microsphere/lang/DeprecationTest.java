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
package io.microsphere.lang;

import io.microsphere.util.Version;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link Deprecation} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Deprecation
 * @since 1.0.0
 */
public class DeprecationTest {

    public static final String SINCE = "1.0.0";

    public static final String REASON = "No Reason";

    public static final String REPLACEMENT = "No Replacement";

    public static final String LINK = "https://github.com/microsphere-projects/microsphere-java";

    public static Deprecation.Level LEVEL = Deprecation.Level.REMOVAL;

    public static Deprecation DEPRECATION = Deprecation.of(SINCE, REPLACEMENT, REASON, LINK, LEVEL);

    @Test
    public void test() {
        Deprecation deprecation = Deprecation.of(SINCE, REPLACEMENT, REASON, LINK, LEVEL);

        assertObjectMethods(deprecation);
        assertEquals(Version.of(SINCE), deprecation.getSince());
        assertEquals(REPLACEMENT, deprecation.getReplacement());
        assertEquals(REASON, deprecation.getReason());
        assertEquals(LINK, deprecation.getLink());
        assertEquals(LEVEL, deprecation.getLevel());
        assertNotNull(deprecation.toString());
        assertEquals(deprecation, new Deprecation(deprecation));
        assertEquals(deprecation.hashCode(), new Deprecation(deprecation).hashCode());

        deprecation = Deprecation.of(SINCE, REPLACEMENT, REASON, LINK);
        assertObjectMethods(deprecation);
        assertEquals(Version.of(SINCE), deprecation.getSince());
        assertEquals(REPLACEMENT, deprecation.getReplacement());
        assertEquals(REASON, deprecation.getReason());
        assertEquals(LINK, deprecation.getLink());
        assertEquals(Deprecation.Level.DEFAULT, deprecation.getLevel());
        assertNotNull(deprecation.toString());
        assertEquals(deprecation, new Deprecation(deprecation));
        assertEquals(deprecation.hashCode(), new Deprecation(deprecation).hashCode());

        deprecation = Deprecation.of(SINCE, REPLACEMENT, REASON);
        assertObjectMethods(deprecation);
        assertEquals(Version.of(SINCE), deprecation.getSince());
        assertEquals(REPLACEMENT, deprecation.getReplacement());
        assertEquals(REASON, deprecation.getReason());
        assertNull(deprecation.getLink());
        assertEquals(Deprecation.Level.DEFAULT, deprecation.getLevel());
        assertNotNull(deprecation.toString());
        assertEquals(deprecation, new Deprecation(deprecation));
        assertEquals(deprecation.hashCode(), new Deprecation(deprecation).hashCode());

        deprecation = Deprecation.of(SINCE, REPLACEMENT, REASON);
        assertObjectMethods(deprecation);
        assertEquals(Version.of(SINCE), deprecation.getSince());
        assertEquals(REPLACEMENT, deprecation.getReplacement());
        assertEquals(REASON, deprecation.getReason());
        assertNull(deprecation.getLink());
        assertEquals(Deprecation.Level.DEFAULT, deprecation.getLevel());
        assertNotNull(deprecation.toString());
        assertEquals(deprecation, new Deprecation(deprecation));
        assertEquals(deprecation.hashCode(), new Deprecation(deprecation).hashCode());

        deprecation = Deprecation.of(SINCE, REPLACEMENT);
        assertObjectMethods(deprecation);
        assertEquals(Version.of(SINCE), deprecation.getSince());
        assertEquals(REPLACEMENT, deprecation.getReplacement());
        assertNull(deprecation.getReason());
        assertNull(deprecation.getLink());
        assertEquals(Deprecation.Level.DEFAULT, deprecation.getLevel());
        assertNotNull(deprecation.toString());
        assertEquals(deprecation, new Deprecation(deprecation));
        assertEquals(deprecation.hashCode(), new Deprecation(deprecation).hashCode());

        deprecation = Deprecation.of(SINCE);
        assertObjectMethods(deprecation);
        assertEquals(Version.of(SINCE), deprecation.getSince());
        assertNull(deprecation.getReplacement());
        assertNull(deprecation.getReason());
        assertNull(deprecation.getLink());
        assertEquals(Deprecation.Level.DEFAULT, deprecation.getLevel());
        assertNotNull(deprecation.toString());
        assertEquals(deprecation, new Deprecation(deprecation));
        assertEquals(deprecation.hashCode(), new Deprecation(deprecation).hashCode());

    }

    private void assertObjectMethods(Deprecation deprecation) {
        Deprecation copy = new Deprecation(deprecation);
        assertEquals(deprecation, copy);
        assertEquals(deprecation.hashCode(), copy.hashCode());
        assertEquals(deprecation.toString(), copy.toString());
    }
}
