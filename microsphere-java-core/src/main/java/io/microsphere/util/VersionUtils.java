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

import javax.lang.model.SourceVersion;

import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.util.SystemUtils.JAVA_VERSION;
import static io.microsphere.util.Version.Operator.of;
import static io.microsphere.util.Version.ofVersion;
import static javax.lang.model.SourceVersion.latest;

/**
 * The utility class for version
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Version
 * @since 1.0.0
 */
public abstract class VersionUtils extends BaseUtils {

    /**
     * The latest {@link SourceVersion Java Release Version}
     */
    public static final SourceVersion LATEST_JAVA_VERSION = latest();

    /**
     * The {@link Version} instance for current Java Version
     */
    public static final Version CURRENT_JAVA_VERSION = ofVersion(currentJavaMajorVersion());

    /**
     * The {@link Version} instance for Java 8
     */
    public static final Version JAVA_VERSION_8 = ofVersion(8);

    /**
     * The {@link Version} instance for Java 9
     */
    public static final Version JAVA_VERSION_9 = ofVersion(9);

    /**
     * The {@link Version} instance for Java 10
     */
    public static final Version JAVA_VERSION_10 = ofVersion(10);

    /**
     * The {@link Version} instance for Java 11
     */
    public static final Version JAVA_VERSION_11 = ofVersion(11);

    /**
     * The {@link Version} instance for Java 12
     */
    public static final Version JAVA_VERSION_12 = ofVersion(12);

    /**
     * The {@link Version} instance for Java 13
     */
    public static final Version JAVA_VERSION_13 = ofVersion(13);

    /**
     * The {@link Version} instance for Java 14
     */
    public static final Version JAVA_VERSION_14 = ofVersion(14);

    /**
     * The {@link Version} instance for Java 15
     */
    public static final Version JAVA_VERSION_15 = ofVersion(15);

    /**
     * The {@link Version} instance for Java 16
     */
    public static final Version JAVA_VERSION_16 = ofVersion(16);

    /**
     * The {@link Version} instance for Java 17
     */
    public static final Version JAVA_VERSION_17 = ofVersion(17);

    /**
     * The {@link Version} instance for Java 18
     */
    public static final Version JAVA_VERSION_18 = ofVersion(18);

    /**
     * The {@link Version} instance for Java 19
     */
    public static final Version JAVA_VERSION_19 = ofVersion(19);

    /**
     * The {@link Version} instance for Java 20
     */
    public static final Version JAVA_VERSION_20 = ofVersion(20);

    /**
     * The {@link Version} instance for Java 21
     */
    public static final Version JAVA_VERSION_21 = ofVersion(21);

    /**
     * The {@link Version} instance for Java 22
     */
    public static final Version JAVA_VERSION_22 = ofVersion(22);

    /**
     * The {@link Version} instance for Java 23
     */
    public static final Version JAVA_VERSION_23 = ofVersion(23);

    /**
     * The {@link Version} instance for Java 24
     */
    public static final Version JAVA_VERSION_24 = ofVersion(24);

    /**
     * Determine whether {@link #CURRENT_JAVA_VERSION current Java version} matches the specified version
     *
     * @param operatorSymbol  the {@link Version.Operator}
     * @param comparedVersion the {@link Version} to be compared
     * @return <code>true</code> if {@link Version.Operator} {@link Version.Operator#test(Object, Object) matches}
     * {@link #CURRENT_JAVA_VERSION current Java version} and <code>comparedVersion</code>
     */
    public static boolean testCurrentJavaVersion(String operatorSymbol, Version comparedVersion) {
        return testCurrentJavaVersion(of(operatorSymbol), comparedVersion);
    }

    /**
     * Determine whether {@link #CURRENT_JAVA_VERSION current Java version} matches the specified version
     *
     * @param versionOperator the {@link Version.Operator}
     * @param comparedVersion the {@link Version} to be compared
     * @return <code>true</code> if {@link Version.Operator} {@link Version.Operator#test(Object, Object) matches}
     * {@link #CURRENT_JAVA_VERSION current Java version} and <code>comparedVersion</code>
     */
    public static boolean testCurrentJavaVersion(Version.Operator versionOperator, Version comparedVersion) {
        return testVersion(CURRENT_JAVA_VERSION, versionOperator, comparedVersion);
    }

    /**
     * Determine whether the base version matches the compared version
     *
     * <pre>
     * VersionUtils.testVersion("1.8", "=", "1.8.0") == true
     * VersionUtils.testVersion("1.8", ">=", "1.7") == true
     * VersionUtils.testVersion("1.8", "<=", "1.7") == false
     * VersionUtils.testVersion("1.8", "<", "1.7") == false
     * VersionUtils.testVersion("1.8", ">", "1.7") == true
     * </pre>
     *
     * @param baseVersion     the {@link Version} to be tested
     * @param operatorSymbol  the  symbol of {@link Version.Operator}
     * @param comparedVersion the {@link Version} to be compared
     * @return <code>true</code> if {@link Version.Operator} {@link Version.Operator#test(Object, Object) matches}
     * @throws IllegalArgumentException if the base version or the compared version can't be resolved
     *                                  or the operator symbol is not supported, only supports "=", ">=", "<=", "<", ">"
     */
    public static boolean testVersion(String baseVersion, String operatorSymbol, String comparedVersion) {
        if (baseVersion == null || operatorSymbol == null || comparedVersion == null) {
            return false;
        }
        return testVersion(ofVersion(baseVersion), of(operatorSymbol), ofVersion(comparedVersion));
    }

    /**
     * Determine whether the base version matches the compared version
     *
     * @param baseVersion     the {@link Version} to be tested
     * @param versionOperator the {@link Version.Operator}
     * @param comparedVersion the {@link Version} to be compared
     * @return <code>true</code> if {@link Version.Operator} {@link Version.Operator#test(Object, Object) matches}
     */
    public static boolean testVersion(Version baseVersion, Version.Operator versionOperator, Version comparedVersion) {
        if (baseVersion == null || versionOperator == null || comparedVersion == null) {
            return false;
        }
        return versionOperator.test(baseVersion, comparedVersion);
    }

    static String currentJavaMajorVersion() {
        return detectJavaMajorVersion(JAVA_VERSION);
    }

    static String detectJavaMajorVersion(String javaVersion) {
        int firstDotIndex = javaVersion.indexOf(DOT_CHAR);
        if (firstDotIndex > -1) {
            String majorVersion = javaVersion.substring(0, firstDotIndex);
            if ("1".equals(majorVersion)) { // JDK Version is like "1.x.y"
                // it takes 'x' as the major version
                int startIndex = firstDotIndex + 1;
                int endIndex = javaVersion.indexOf(DOT_CHAR, startIndex);
                majorVersion = javaVersion.substring(startIndex, endIndex);
            }
            return majorVersion;
        }
        return javaVersion;
    }
}
