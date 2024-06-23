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
import static java.lang.String.valueOf;
import static javax.lang.model.SourceVersion.latest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link VersionUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see VersionUtils
 * @since 1.0.0
 */
public class VersionUtilsTest {

    @Test
    public void testLatestJavaVersion() {
        assertSame(LATEST_JAVA_VERSION, latest());
    }

    @Test
    public void testCurrentJavaVersion() {
        String releaseVersion = LATEST_JAVA_VERSION.name();
        String version = substringAfter(releaseVersion, "RELEASE_");
        assertEquals(LATEST_JAVA_VERSION.ordinal(), CURRENT_JAVA_VERSION.getMajor());
        assertEquals(version, valueOf(CURRENT_JAVA_VERSION.getMajor()));
    }

    @Test
    public void testJavaVersions() {
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
}
