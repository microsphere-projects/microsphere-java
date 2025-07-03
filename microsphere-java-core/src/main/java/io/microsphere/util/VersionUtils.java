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
public abstract class VersionUtils implements Utils {

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
     * Determines whether the current Java version matches the specified version based on the given operator symbol.
     *
     * <h3>Supported Operators</h3>
     * <ul>
     *   <li>{@code =} - Equal to</li>
     *   <li>{@code >} - Greater than</li>
     *   <li>{@code >=} - Greater than or equal to</li>
     *   <li>{@code <} - Less than</li>
     *   <li>{@code <=} - Less than or equal to</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * VersionUtils.testCurrentJavaVersion("=", VersionUtils.JAVA_VERSION_8);  // true if current Java version is 8
     * VersionUtils.testCurrentJavaVersion(">", VersionUtils.JAVA_VERSION_8);   // true if current Java version is higher than 8
     * VersionUtils.testCurrentJavaVersion("<", VersionUtils.JAVA_VERSION_11);  // true if current Java version is lower than 11
     * }</pre>
     *
     * @param operatorSymbol  the symbol of the comparison operator; must not be null
     * @param comparedVersion the version to compare against; must not be null
     * @return {@code true} if the result of applying the operator to the current Java version and the compared version is true;
     *         otherwise, {@code false}
     * @throws IllegalArgumentException if any argument is null or the operator symbol is not supported
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
     * Determines whether the specified base version matches the compared version based on the given operator symbol.
     *
     * <h3>Supported Operators</h3>
     * <ul>
     *   <li>{@code =} - Equal to</li>
     *   <li>{@code >} - Greater than</li>
     *   <li>{@code >=} - Greater than or equal to</li>
     *   <li>{@code <} - Less than</li>
     *   <li>{@code <=} - Less than or equal to</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * VersionUtils.testVersion("1.8", "=", "1.8.0") == true
     * VersionUtils.testVersion("1.8", ">=", "1.7") == true
     * VersionUtils.testVersion("1.8", "<=", "1.7") == false
     * VersionUtils.testVersion("1.8", "<", "1.7") == false
     * VersionUtils.testVersion("1.8", ">", "1.7") == true
     * }</pre>
     *
     * @param baseVersion     the version to be tested; must not be null
     * @param operatorSymbol  the symbol of the comparison operator; must not be null
     * @param comparedVersion the version to compare against; must not be null
     * @return {@code true} if the result of applying the operator to the base and compared versions is true;
     *         otherwise, {@code false}
     * @throws IllegalArgumentException if any argument is null or the operator symbol is not supported
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
    /**
     * Determines whether the specified base version matches the compared version based on the given operator.
     *
     * <h3>Supported Operators</h3>
     * <ul>
     *   <li>{@code =} - Equal to</li>
     *   <li>{@code >} - Greater than</li>
     *   <li>{@code >=} - Greater than or equal to</li>
     *   <li>{@code <} - Less than</li>
     *   <li>{@code <=} - Less than or equal to</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Version v1 = Version.of("1.8.0");
     * Version v2 = Version.of("1.7.0");
     *
     * Version.Operator eq = Version.Operator.of("=");  // Equals
     * Version.Operator gt = Version.Operator.of(">");  // Greater than
     * Version.Operator lt = Version.Operator.of("<");  // Less than
     *
     * testVersion(v1, eq, v1); // true
     * testVersion(v1, gt, v2); // true
     * testVersion(v2, lt, v1); // true
     * }</pre>
     *
     * @param baseVersion     the version to be tested; must not be null
     * @param versionOperator the comparison operator; must not be null
     * @param comparedVersion the version to compare against; must not be null
     * @return {@code true} if the result of applying the operator to the base and compared versions is true;
     *         otherwise, {@code false}
     * @throws IllegalArgumentException if any argument is null
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

    private VersionUtils() {
    }
}
