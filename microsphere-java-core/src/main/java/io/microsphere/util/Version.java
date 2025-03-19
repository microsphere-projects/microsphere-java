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

import io.microsphere.lang.ClassDataRepository;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.net.URL;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.function.BiPredicate;

import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.constants.SymbolConstants.EQUAL;
import static io.microsphere.constants.SymbolConstants.GREATER_THAN;
import static io.microsphere.constants.SymbolConstants.GREATER_THAN_OR_EQUAL_TO;
import static io.microsphere.constants.SymbolConstants.LESS_THAN;
import static io.microsphere.constants.SymbolConstants.LESS_THAN_OR_EQUAL_TO;
import static io.microsphere.constants.SymbolConstants.QUOTE_CHAR;
import static io.microsphere.constants.SymbolConstants.SPACE_CHAR;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.Assert.assertNotBlank;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.Version.Operator.EQ;
import static io.microsphere.util.Version.Operator.GE;
import static io.microsphere.util.Version.Operator.GT;
import static io.microsphere.util.Version.Operator.LE;
import static io.microsphere.util.Version.Operator.LT;
import static java.lang.Integer.compare;
import static java.lang.Integer.parseInt;

/**
 * The value object to represent a version consisting of major, minor and patch part.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class Version implements Comparable<Version>, Serializable {

    private static final long serialVersionUID = -6434504483466016691L;

    private final int major;

    private final int minor;

    private final int patch;

    public Version(int major) {
        this(major, 0);
    }

    public Version(int major, int minor) {
        this(major, minor, 0);
    }

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * The major version
     *
     * @return major version
     */
    public int getMajor() {
        return major;
    }

    /**
     * The minor version
     *
     * @return minor version
     */
    public int getMinor() {
        return minor;
    }

    /**
     * The patch
     *
     * @return patch
     */
    public int getPatch() {
        return patch;
    }

    /**
     * Current {@link Version} is greater than that
     *
     * @param that the version to be compared
     * @return <code>true</code> if greater than, or <code>false</code>
     * @see #isGreaterThan(Version)
     */
    public boolean gt(Version that) {
        return isGreaterThan(that);
    }

    /**
     * Current {@link Version} is greater than that
     *
     * @param that the version to be compared
     * @return <code>true</code> if greater than, or <code>false</code>
     * @see #gt(Version)
     */
    public boolean isGreaterThan(Version that) {
        return GT.test(this, that);
    }

    /**
     * Current {@link Version} is greater than or equal to that
     *
     * @param that the version to be compared
     * @return <code>true</code> if greater than, or <code>false</code>
     * @see #isGreaterOrEqual(Version)
     */
    public boolean ge(Version that) {
        return isGreaterOrEqual(that);
    }

    /**
     * Current {@link Version} is greater than or equal to that
     *
     * @param that the version to be compared
     * @return <code>true</code> if greater than, or <code>false</code>
     * @see #ge(Version)
     */
    public boolean isGreaterOrEqual(Version that) {
        return GE.test(this, that);
    }

    /**
     * Current {@link Version} is less than that
     *
     * @param that the version to be compared
     * @return <code>true</code> if less than, or <code>false</code>
     */
    public boolean lt(Version that) {
        return isLessThan(that);
    }

    /**
     * Current {@link Version} is less than that
     *
     * @param that the version to be compared
     * @return <code>true</code> if less than, or <code>false</code>
     */
    public boolean isLessThan(Version that) {
        return LT.test(this, that);
    }

    /**
     * Current {@link Version} is less than or equal to that
     *
     * @param that the version to be compared
     * @return <code>true</code> if less than, or <code>false</code>
     * @see #isLessOrEqual(Version)
     */
    public boolean le(Version that) {
        return isLessOrEqual(that);
    }

    /**
     * Current {@link Version} is less than or equal to that
     *
     * @param that the version to be compared
     * @return <code>true</code> if less than, or <code>false</code>
     * @see #le(Version)
     */
    public boolean isLessOrEqual(Version that) {
        return LE.test(this, that);
    }

    /**
     * Current {@link Version} is equal to that
     *
     * @param that the version to be compared
     * @return <code>true</code> if equals, or <code>false</code>
     */
    public boolean eq(Version that) {
        return this.equals(that);
    }

    /**
     * Current {@link Version} is equal to that
     *
     * @param that the version to be compared
     * @return <code>true</code> if equals, or <code>false</code>
     */
    public boolean equals(Version that) {
        return EQ.test(this, that);
    }

    /**
     * Current {@link Version} is equal to that
     *
     * @param o the version to be compared
     * @return <code>true</code> if equals, or <code>false</code>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;

        Version version = (Version) o;
        return equals(version);
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + patch;
        return result;
    }

    @Override
    public int compareTo(Version that) {
        int result = compare(this.major, that.major);

        if (result != 0) {
            return result;
        }

        result = compare(this.minor, that.minor);

        if (result != 0) {
            return result;
        }

        result = compare(this.patch, that.patch);

        return result;
    }


    @Override
    public String toString() {
        return "Version{" +
                "major=" + major +
                ", minor=" + minor +
                ", patch=" + patch +
                '}';
    }

    public static Version of(int major) {
        return ofVersion(major);
    }

    public static Version of(int major, int minor) {
        return ofVersion(major, minor);
    }

    public static Version of(int major, int minor, int patch) {
        return ofVersion(major, minor, patch);
    }

    public static Version of(String version) {
        return ofVersion(version);
    }

    public static Version ofVersion(int major) {
        return new Version(major);
    }

    public static Version ofVersion(int major, int minor) {
        return new Version(major, minor);
    }

    public static Version ofVersion(int major, int minor, int patch) {
        return new Version(major, minor, patch);
    }

    public static Version ofVersion(String version) {
        assertNotNull(version, () -> "The 'version' argument must not be null!");
        assertNotBlank(version, () -> "The 'version' argument must not be blank!");

        StringTokenizer st = new StringTokenizer(version.trim(), DOT);

        int major = getValue(st);
        int minor = getValue(st);
        int patch = getValue(st);

        return of(major, minor, patch);
    }


    /**
     * Class that exposes the version. Fetches the "Implementation-Version" manifest attribute from the jar file.
     *
     * @param targetClass the class to exposes the version
     * @return non-null
     * @throws IllegalArgumentException if the "Implementation-Version" manifest attribute can't be fetched from the jar file.
     */
    @Nonnull
    public static Version getVersion(Class<?> targetClass) throws IllegalArgumentException {
        Package targetPackage = targetClass.getPackage();
        String version = targetPackage.getImplementationVersion();
        if (version == null) {
            ClassDataRepository repository = ClassDataRepository.INSTANCE;
            URL classResource = repository.getCodeSourceLocation(targetClass);
            String errorMessage = format("The 'Implementation-Version' manifest attribute can't be fetched from the jar file[class resource : '{}'] by the target class[name :'{}']", classResource, targetClass.getName());
            throw new IllegalArgumentException(errorMessage);
        }
        return of(version);
    }

    static int getValue(StringTokenizer st) {
        if (st.hasMoreTokens()) {
            return getValue(st.nextToken());
        }
        return 0;
    }

    static int getValue(String part) {
        final int value;
        try {
            value = parseInt(part.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The 'version' argument contains the non-number part : " + part, e);
        }
        return value;
    }

    public enum Operator implements BiPredicate<Version, Version> {

        /**
         * The Operator : "Equal to"
         */
        EQ(EQUAL) {
            @Override
            public boolean test(Version v1, Version v2) {
                if (v1 == v2) {
                    return true;
                }
                if (v2 == null) return false;
                if (v1.major != v2.major) return false;
                if (v1.minor != v2.minor) return false;
                return v1.patch == v2.patch;
            }
        },

        /**
         * The Operator : "Less than"
         */
        LT(LESS_THAN) {
            @Override
            public boolean test(Version v1, Version v2) {
                if (v1 == v2) {
                    return false;
                }
                if (v2 == null) return false;
                return v1.compareTo(v2) < 0;
            }
        },

        /**
         * The Operator : "Less than or equal to"
         */
        LE(LESS_THAN_OR_EQUAL_TO) {
            @Override
            public boolean test(Version v1, Version v2) {
                if (v1 == v2) {
                    return true;
                }
                if (v2 == null) return false;
                return v1.compareTo(v2) <= 0;
            }
        },

        /**
         * The Operator : "Greater than"
         */
        GT(GREATER_THAN) {
            @Override
            public boolean test(Version v1, Version v2) {
                if (v1 == v2) {
                    return false;
                }
                if (v2 == null) return false;
                return v1.compareTo(v2) > 0;
            }
        },

        /**
         * The Operator : "Greater than or equal to"
         */
        GE(GREATER_THAN_OR_EQUAL_TO) {
            @Override
            public boolean test(Version v1, Version v2) {
                if (v1 == v2) {
                    return true;
                }
                if (v2 == null) return false;
                return v1.compareTo(v2) >= 0;
            }
        };

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        /**
         * Find the symbol to an {@link Operator} member if possible
         *
         * @param symbol the operators' symbol
         * @return <code>null</code> if can't be found
         */
        public static Operator of(String symbol) {
            Operator[] operators = values();
            for (Operator operator : operators) {
                if (Objects.equals(symbol, operator.symbol)) {
                    return operator;
                }
            }

            StringBuilder messageBuilder = new StringBuilder("The Operator can't be parsed by the symbol : ")
                    .append(QUOTE_CHAR).append(symbol).append(QUOTE_CHAR)
                    .append(", only supports : ");

            for (int i = 0; i < operators.length; i++) {
                Operator operator = operators[i];
                // append '${operator.symbol}'
                messageBuilder.append(QUOTE_CHAR).append(operator.symbol).append(QUOTE_CHAR);
                if (i < operators.length - 1) {
                    // append ", "
                    messageBuilder.append(COMMA_CHAR).append(SPACE_CHAR);
                }
            }

            throw new IllegalArgumentException(messageBuilder.toString());
        }

    }
}
