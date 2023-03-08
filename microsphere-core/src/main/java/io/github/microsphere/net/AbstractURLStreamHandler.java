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
package io.github.microsphere.net;

import java.net.URL;
import java.net.URLStreamHandler;
import java.util.Objects;

import static io.github.microsphere.constants.Constants.EQUAL;
import static io.github.microsphere.constants.Constants.SEMICOLON;

/**
 * Abstract {@link URLStreamHandler} class overrides these methods making final:
 * <ul>
 *     <li>{@link #parseURL(URL, String, int, int)}</li>
 *     <li>{@link #equals(URL, URL)}</li>
 *     <li>{@link #hostsEqual(URL, URL)}</li>
 *     <li>{@link #hashCode(URL)}</li>
 *     <li>{@link #toExternalForm(URL)}</li>
 * </ul>
 * <p>
 * The implementation class must the obey conventions as follow:
 * <ul>
 *     <li>The class must be the top level</li>
 *     <li>The simple class name must be "Handler"</li>
 *     <li>The class must not be present in the builtin package : {@link #DEFAULT_HANDLER_PACKAGE_NAME "sun.net.www.protocol"}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see URLStreamHandler
 * @since 1.0.0
 */
public abstract class AbstractURLStreamHandler extends URLStreamHandler {

    /**
     * The property which specifies the package prefix list to be scanned
     * for protocol handlers.  The value of this property (if any) should
     * be a vertical bar delimited list of package names to search through
     * for a protocol handler to load.  The policy of this class is that
     * all protocol handlers will be in a class called <protocolname>.Handler,
     * and each package in the list is examined in turn for a matching
     * handler.  If none are found (or the property is not specified), the
     * default package prefix, sun.net.www.protocol, is used.  The search
     * proceeds from the first package in the list to the last and stops
     * when a match is found.
     *
     * @see {@link URL#protocolPathProp}
     */
    public static final String HANDLER_PACKAGES_PROPERTY_NAME = "java.protocol.handler.pkgs";

    /**
     * The default package name
     */
    public static final String DEFAULT_HANDLER_PACKAGE_NAME = "sun.net.www.protocol";

    /**
     * The separator of Handler packages.
     */
    public static final String HANDLER_PACKAGES_SEPARATOR = "|";

    /**
     * The convention class name.
     */
    public static final String CONVENTION_CLASS_NAME = "Handler";

    private static final String SCHEME_EXTENSION_TYPE_MATRIX_NAME = "_SET_";

    public AbstractURLStreamHandler() {
        Class<?> currentClass = getClass();
        assertConventions(currentClass);
        appendHandlerPackage(currentClass);
    }

    private static void assertConventions(Class<?> type) {
        assertClassTopLevel(type);
        assertClassName(type);
        assertPackage(type);
    }

    private static void assertClassTopLevel(Class<?> type) {
        if (type.isLocalClass() || type.isAnonymousClass() || type.isMemberClass()) {
            throw new IllegalStateException("The implementation " + type + " must be the top level");
        }
    }

    private static void assertClassName(Class<?> type) {
        String simpleClassName = type.getSimpleName();
        if (!Objects.equals(CONVENTION_CLASS_NAME, simpleClassName)) {
            throw new IllegalStateException("The implementation class must name '" + CONVENTION_CLASS_NAME + "', actual : '" + simpleClassName + "'");
        }
    }

    private static void assertPackage(Class<?> type) {
        String className = type.getName();
        if (className.startsWith(DEFAULT_HANDLER_PACKAGE_NAME)) {
            throw new IllegalStateException("The implementation class must not be present in the builtin package : '" + DEFAULT_HANDLER_PACKAGE_NAME + "'");
        }
    }

    private static void appendHandlerPackage(Class<?> type) {
        String packageName = type.getPackage().getName();
        appendHandlePackage(packageName);
    }

    static void appendHandlePackage(String packageName) {
        String handlePackage = packageName.substring(0, packageName.lastIndexOf('.'));
        String currentHandlePackages = getHandlePackages();
        String handlePackages = null;
        if (currentHandlePackages == null || currentHandlePackages.isEmpty()) {
            handlePackages = handlePackage;
        } else {
            handlePackages = currentHandlePackages + HANDLER_PACKAGES_SEPARATOR + handlePackage;
        }
        System.setProperty(HANDLER_PACKAGES_PROPERTY_NAME, handlePackages);
    }

    public static String getHandlePackages() {
        return System.getProperty(HANDLER_PACKAGES_PROPERTY_NAME);
    }

    @Override
    protected final boolean equals(URL u1, URL u2) {
        return Objects.equals(toExternalForm(u1), toExternalForm(u2));
    }

    @Override
    protected final int hashCode(URL u) {
        return toExternalForm(u).hashCode();
    }

    @Override
    protected final boolean hostsEqual(URL u1, URL u2) {
        return Objects.equals(u1.getHost(), u2.getHost());
    }

    /**
     * Reuses the algorithm of {@link URLStreamHandler#toExternalForm(URL)} using the {@link StringBuilder} to
     * the {@link StringBuilder}.
     *
     * @param u the URL.
     * @return a string representation of the URL argument.
     */
    @Override
    protected final String toExternalForm(URL u) {
        // pre-compute length of StringBuilder
        int len = u.getProtocol().length() + 1;
        if (u.getAuthority() != null && u.getAuthority().length() > 0) len += 2 + u.getAuthority().length();
        if (u.getPath() != null) {
            len += u.getPath().length();
        }
        if (u.getQuery() != null) {
            len += 1 + u.getQuery().length();
        }
        if (u.getRef() != null) len += 1 + u.getRef().length();

        StringBuilder result = new StringBuilder(len);
        result.append(u.getProtocol());
        result.append(":");
        if (u.getAuthority() != null && u.getAuthority().length() > 0) {
            result.append("//");
            result.append(u.getAuthority());
        }
        if (u.getPath() != null) {
            result.append(u.getPath());
        }
        if (u.getQuery() != null) {
            result.append('?');
            result.append(u.getQuery());
        }
        if (u.getRef() != null) {
            result.append("#");
            result.append(u.getRef());
        }
        return result.toString();
    }

    @Override
    protected final void parseURL(URL u, String spec, int start, int limit) {
        int end = spec.indexOf("://", start);
        if (end > start) { // The sub-protocol was found
            String actualSpec = reformSpec(u, spec, start, end, limit);
            super.parseURL(u, actualSpec, start, actualSpec.length());
        } else {
            super.parseURL(u, spec, start, limit);
        }
    }

    /**
     * Reform the string of specified {@link URL} if its' scheme presents an extension type of protocol, e,g.
     * A string representing the URL is "jdbc:mysql://localhost:3307/mydb?charset=UTF-8#top", its'
     * <ul>
     *     <li>scheme : "jdbc:mysql"</li>
     *     <li>host : "localhost"</li>
     *     <li>port : 3307</li>
     *     <li>path : "/mydb"</li>
     *     <li>query : "charset=UTF-8"</li>
     *     <li>ref : "top"</li>
     * </ul>
     * <p>
     * This scheme contains two parts, the former is "jdbc" as the protocol, the later is "mysql" called the the extension
     * type of protocol which is convenient to extend the fine-grain {@link URLStreamHandler}.
     * In this case, the reformed string of specified {@link URL} will be "jdbc://localhost:3307/mydb?charset=UTF-8;_SET_=mysql#top".
     *
     * @param url   the {@code URL} to receive the result of parsing
     *              the spec.
     * @param spec  the {@code String} representing the URL that
     *              must be parsed.
     * @param start the character index at which to begin parsing. This is
     *              just past the '{@code :}' (if there is one) that
     *              specifies the determination of the protocol name.
     * @param end   the index of the string "://" present in the URL from the
     *              <code>start</code> index, its' value is greater or equal 0.
     * @param limit the character position to stop parsing at. This is the
     *              end of the string or the position of the
     *              "{@code #}" character, if present. All information
     *              after the sharp sign indicates an anchor.
     * @return reformed the string of specified {@link URL} if the suffix o
     */
    protected String reformSpec(URL url, String spec, int start, int end, int limit) {
        String protocol = url.getProtocol();
        String matrix = SEMICOLON + SCHEME_EXTENSION_TYPE_MATRIX_NAME + EQUAL + spec.substring(start, end);
        String suffix = spec.substring(end, limit);

        // int queryIndex = spec.inde

        int length = protocol.length() + matrix.length() + suffix.length();

        StringBuilder newSpecBuilder = new StringBuilder(length);

        newSpecBuilder.append(protocol).append(suffix);

        int matrixIndex = newSpecBuilder.indexOf(SEMICOLON, end);

        if (matrixIndex > end) {
            newSpecBuilder.insert(matrixIndex, matrix);
        } else {
            newSpecBuilder.append(matrix);
        }

        return newSpecBuilder.toString();
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("{defaultPort=").append(getDefaultPort());
        sb.append('}');
        return sb.toString();
    }
}
