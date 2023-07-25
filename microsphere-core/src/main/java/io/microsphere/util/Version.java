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

import java.util.StringTokenizer;

import static java.lang.Integer.compare;

/**
 * The value object to represent a version consisting of major, minor and patch part.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class Version implements Comparable<Version> {

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

    public static Version of(int major) {
        return new Version(major);
    }

    public static Version of(int major, int minor) {
        return new Version(major, minor);
    }

    public static Version of(int major, int minor, int patch) {
        return new Version(major, minor, patch);
    }

    public static Version of(String version) {

        if (version == null) {
            throw new NullPointerException("The 'version' argument must not be null!");
        }

        version = version.trim();

        if (version.isEmpty()) {
            throw new IllegalArgumentException("The 'version' argument must not be blank!");
        }

        StringTokenizer st = new StringTokenizer(version, ".");

        int major = getValue(st);
        int minor = getValue(st);
        int patch = getValue(st);

        return of(major, minor, patch);
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
            value = Integer.parseInt(part);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The 'version' argument contains the non-number part : " + part, e);
        }
        return value;
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
        return this.compareTo(that) > 0;
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
        return this.compareTo(that) >= 0;
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
        return this.compareTo(that) < 0;
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
        return this.compareTo(that) <= 0;
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
        if (this.major != that.major) return false;
        if (this.minor != that.minor) return false;
        return patch == that.patch;
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
        final StringBuilder sb = new StringBuilder("Version{");
        sb.append("major=").append(major);
        sb.append(", minor=").append(minor);
        sb.append(", patch=").append(patch);
        sb.append('}');
        return sb.toString();
    }
}
