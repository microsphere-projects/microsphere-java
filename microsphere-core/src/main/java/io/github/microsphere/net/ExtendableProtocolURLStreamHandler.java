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
import java.util.Set;

import static io.github.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.github.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.github.microsphere.constants.SymbolConstants.QUERY_STRING;
import static io.github.microsphere.net.URLUtils.SUB_PROTOCOL_MATRIX_NAME;
import static io.github.microsphere.net.URLUtils.buildMatrixString;
import static io.github.microsphere.util.CollectionUtils.ofSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;

/**
 * Extendable Protocol {@link URLStreamHandler} class overrides these methods making final:
 * <ul>
 *     <li>{@link #parseURL(URL, String, int, int)}</li>
 *     <li>{@link #equals(URL, URL)}</li>
 *     <li>{@link #hostsEqual(URL, URL)}</li>
 *     <li>{@link #hashCode(URL)}</li>
 *     <li>{@link #toExternalForm(URL)}</li>
 * </ul>
 * <p>
 * If an instance is instantiated by the default constructor, the implementation class must the obey conventions as follow:
 * <ul>
 *     <li>The class must be the top level</li>
 *     <li>The simple class name must be "Handler"</li>
 *     <li>The class must not be present in the "default" or builtin package({@link #DEFAULT_HANDLER_PACKAGE_NAME "sun.net.www.protocol"})</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see URLStreamHandler
 * @since 1.0.0
 */
public abstract class ExtendableProtocolURLStreamHandler extends URLStreamHandler {

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
     * The separator character of Handler packages.
     */
    public static final char HANDLER_PACKAGES_SEPARATOR_CHAR = '|';

    /**
     * The convention class name.
     */
    public static final String CONVENTION_CLASS_NAME = "Handler";

    private final String protocol;

    /**
     * The default constructor must obey the following conventions:
     * <ul>
     *     <li>The class must be the top level</li>
     *     <li>The simple class name must be "Handler"</li>
     *     <li>The class must not be present in the "default" or builtin package({@link #DEFAULT_HANDLER_PACKAGE_NAME "sun.net.www.protocol"})</li>
     * </ul>
     */
    public ExtendableProtocolURLStreamHandler() {
        Class<?> currentClass = getClass();
        assertConventions(currentClass);
        String packageName = appendHandlerPackage(currentClass);
        this.protocol = resolveConventionProtocol(packageName);
    }

    public ExtendableProtocolURLStreamHandler(String protocol) {
        this.protocol = protocol;
    }

    private static String resolveConventionProtocol(String packageName) {
        int lastIndex = packageName.lastIndexOf(DOT_CHAR);
        return packageName.substring(lastIndex + 1);
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
        if (className.indexOf(DOT_CHAR) < 0) {
            throw new IllegalStateException("The Handler class must not be present at the top package!");
        }
        if (className.startsWith(DEFAULT_HANDLER_PACKAGE_NAME)) {
            throw new IllegalStateException("The Handler class must not be present in the builtin package : '" + DEFAULT_HANDLER_PACKAGE_NAME + "'");
        }

    }

    private static String appendHandlerPackage(Class<?> type) {
        String packageName = type.getPackage().getName();
        appendHandlePackage(packageName);
        return packageName;
    }

    static void appendHandlePackage(String packageName) {
        String handlePackage = packageName.substring(0, packageName.lastIndexOf('.'));
        Set<String> packages = getHandlePackages();

        if (packages.contains(handlePackage)) {
            return;
        }

        String currentHandlerPackages = getHandlePackagesPropertyValue();
        String handlePackages = null;
        if (isBlank(currentHandlerPackages)) {
            handlePackages = handlePackage;
        } else {
            handlePackages = currentHandlerPackages + HANDLER_PACKAGES_SEPARATOR_CHAR + handlePackage;
        }

        System.setProperty(HANDLER_PACKAGES_PROPERTY_NAME, handlePackages);
    }

    public static Set<String> getHandlePackages() {
        String value = getHandlePackagesPropertyValue();
        String[] packages = split(value, HANDLER_PACKAGES_SEPARATOR_CHAR);
        return ofSet(packages);
    }

    /**
     * Get the {@link System} property value of the packages of {@link URLStreamHandler URLStreamHandlers}.
     *
     * @return <code>null</code> if absent
     */
    public static String getHandlePackagesPropertyValue() {
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
        return URLUtils.toExternalForm(u);
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
     * Reform the string of specified {@link URL} if its' scheme presents the sub-protocol, e,g.
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
     * This scheme contains two parts, the former is "jdbc" as the protocol, the later is "mysql" called sub-protocol
     * which is convenient to extend the fine-grain {@link URLStreamHandler}.
     * In this case, the reformed string of specified {@link URL} will be "jdbc://localhost:3307/mydb;_sp=mysql?charset=UTF-8#top".
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
        String subProtocol = spec.substring(start, end);
        String[] subProtocols = split(subProtocol, COLON_CHAR);
        String matrix = buildMatrixString(SUB_PROTOCOL_MATRIX_NAME, subProtocols);
        String suffix = spec.substring(end, limit);

        int capacity = protocol.length() + matrix.length() + suffix.length();

        StringBuilder newSpecBuilder = new StringBuilder(capacity);

        newSpecBuilder.append(protocol).append(suffix);

        int insertIndex = newSpecBuilder.indexOf(QUERY_STRING, end);

        if (insertIndex > end) {
            newSpecBuilder.insert(insertIndex, matrix);
        } else {
            newSpecBuilder.append(matrix);
        }

        return newSpecBuilder.toString();
    }

    public final String getProtocol() {
        return protocol;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("{defaultPort=").append(getDefaultPort());
        sb.append(",protocol=").append(getProtocol());
        sb.append('}');
        return sb.toString();
    }
}
