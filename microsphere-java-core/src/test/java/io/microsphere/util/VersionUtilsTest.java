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

import static io.microsphere.util.StringUtils.substringAfter;
import static io.microsphere.util.Version.Operator.of;
import static io.microsphere.util.Version.ofVersion;
import static io.microsphere.util.VersionUtils.CURRENT_JAVA_VERSION;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_10;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_11;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_12;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_13;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_14;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_15;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_16;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_17;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_18;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_19;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_20;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_21;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_22;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_8;
import static io.microsphere.util.VersionUtils.JAVA_VERSION_9;
import static io.microsphere.util.VersionUtils.LATEST_JAVA_VERSION;
import static io.microsphere.util.VersionUtils.detectJavaMajorVersion;
import static io.microsphere.util.VersionUtils.testCurrentJavaVersion;
import static io.microsphere.util.VersionUtils.testVersion;
import static java.lang.String.valueOf;
import static javax.lang.model.SourceVersion.latest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link VersionUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see VersionUtils
 * @since 1.0.0
 */
public class VersionUtilsTest {

    private static final String TEST_VERSION_STRING_1_1_0 = "1.1.0";

    private static final String TEST_VERSION_STRING_1_2_3 = "1.2.3";

    private static final String TEST_VERSION_STRING_1_3_0 = "1.3.0";

    private static final Version TEST_VERSION_1_1_0 = ofVersion(TEST_VERSION_STRING_1_1_0);

    private static final Version TEST_VERSION_1_2_3 = ofVersion(TEST_VERSION_STRING_1_2_3);

    private static final Version TEST_VERSION_1_3_0 = ofVersion(TEST_VERSION_STRING_1_3_0);

    private static final String TEST_OPERATOR_STRING_GREATER_THAN = ">";

    private static final Version.Operator TEST_OPERATOR_GREATER_THAN = of(TEST_OPERATOR_STRING_GREATER_THAN);

    @Test
    public void testVersionLatestJavaVersion() {
        assertSame(LATEST_JAVA_VERSION, latest());
    }

    @Test
    public void testVersionCurrentJavaMajorVersion() {
        String releaseVersion = LATEST_JAVA_VERSION.name();
        String version = substringAfter(releaseVersion, "RELEASE_");
        assertEquals(LATEST_JAVA_VERSION.ordinal(), CURRENT_JAVA_VERSION.getMajor());
        assertEquals(version, valueOf(CURRENT_JAVA_VERSION.getMajor()));
    }

    @Test
    public void testVersionJavaVersions() {
        assertEquals(8, JAVA_VERSION_8.getMajor());
        assertEquals(9, JAVA_VERSION_9.getMajor());
        assertEquals(10, JAVA_VERSION_10.getMajor());
        assertEquals(11, JAVA_VERSION_11.getMajor());
        assertEquals(12, JAVA_VERSION_12.getMajor());
        assertEquals(13, JAVA_VERSION_13.getMajor());
        assertEquals(14, JAVA_VERSION_14.getMajor());
        assertEquals(15, JAVA_VERSION_15.getMajor());
        assertEquals(16, JAVA_VERSION_16.getMajor());
        assertEquals(17, JAVA_VERSION_17.getMajor());
        assertEquals(18, JAVA_VERSION_18.getMajor());
        assertEquals(19, JAVA_VERSION_19.getMajor());
        assertEquals(20, JAVA_VERSION_20.getMajor());
        assertEquals(21, JAVA_VERSION_21.getMajor());
        assertEquals(22, JAVA_VERSION_22.getMajor());
    }

    @Test
    public void testTestCurrentJavaVersion() {
        assertEquals(testCurrentJavaVersion(TEST_OPERATOR_GREATER_THAN, JAVA_VERSION_8),
                TEST_OPERATOR_GREATER_THAN.test(CURRENT_JAVA_VERSION, JAVA_VERSION_8));
    }

    @Test
    public void testTestVersion() {
        assertTrue(testVersion(TEST_VERSION_1_2_3, TEST_OPERATOR_GREATER_THAN, TEST_VERSION_1_1_0));
        assertFalse(testVersion(TEST_VERSION_1_2_3, TEST_OPERATOR_GREATER_THAN, TEST_VERSION_1_3_0));
    }

    @Test
    public void testTestVersionOnNull() {
        assertFalse(testVersion((Version) null, null, null));
        assertFalse(testVersion(TEST_VERSION_1_2_3, null, null));
        assertFalse(testVersion(TEST_VERSION_1_2_3, TEST_OPERATOR_GREATER_THAN, null));
    }

    @Test
    public void testTestVersionWithStringArgs() {
        assertTrue(testVersion(TEST_VERSION_STRING_1_2_3, TEST_OPERATOR_STRING_GREATER_THAN, TEST_VERSION_STRING_1_1_0));
        assertFalse(testVersion(TEST_VERSION_STRING_1_2_3, TEST_OPERATOR_STRING_GREATER_THAN, TEST_VERSION_STRING_1_3_0));
    }

    @Test
    public void testTestVersionWithStringArgsOnNull() {
        assertFalse(testVersion((String) null, null, null));
        assertFalse(testVersion(TEST_VERSION_STRING_1_2_3, null, null));
        assertFalse(testVersion(TEST_VERSION_STRING_1_2_3, TEST_OPERATOR_STRING_GREATER_THAN, null));
    }

    @Test
    public void testDetectJavaMajorVersion() {
        assertEquals("1", detectJavaMajorVersion("1"));
        assertEquals("9", detectJavaMajorVersion("9.0.0"));
        assertEquals("11", detectJavaMajorVersion("11.0.0"));
        assertEquals("17", detectJavaMajorVersion("17.0.0"));
        assertEquals("21", detectJavaMajorVersion("21.0.0"));
        assertEquals("5", detectJavaMajorVersion("1.5.0"));
        assertEquals("8", detectJavaMajorVersion("1.8.0"));
    }
}
