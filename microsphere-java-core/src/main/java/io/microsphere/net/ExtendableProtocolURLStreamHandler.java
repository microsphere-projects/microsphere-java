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
package io.microsphere.net;

import io.microsphere.lang.Prioritized;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.constants.SymbolConstants.QUERY_STRING;
import static io.microsphere.net.URLUtils.DEFAULT_HANDLER_PACKAGE_PREFIX;
import static io.microsphere.net.URLUtils.HANDLER_CONVENTION_CLASS_NAME;
import static io.microsphere.net.URLUtils.HANDLER_PACKAGES_PROPERTY_NAME;
import static io.microsphere.net.URLUtils.HANDLER_PACKAGES_SEPARATOR_CHAR;
import static io.microsphere.net.URLUtils.SUB_PROTOCOL_MATRIX_NAME;
import static io.microsphere.net.URLUtils.buildMatrixString;
import static io.microsphere.net.URLUtils.registerURLStreamHandler;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.StringUtils.split;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static java.net.Proxy.NO_PROXY;
import static java.util.Collections.sort;

/**
 * Extendable Protocol {@link URLStreamHandler} class supports the sub-protocols,
 * like "{protocol}:{sub-protocols[0]}: ... :{sub-protocols[n]}://...",
 * <ul>
 *     <li>{protocol} : The protocol of {@link URLStreamHandler URLStreamHandler} is recognized by {@link URL} (required) </li>
 *     <li>{sub-protocols} : the list of sub-protocols that is {@link #resolveSubProtocols(URL) resolved} from {@link URL} (optional) </li>
 * </ul>
 * <p>
 * The method {@link #initSubProtocolURLConnectionFactories(List)} that is overridden allows the sub-protocols to be extended,
 * the prerequisite is the method {@link #init() being invoked later.
 * <p>
 * If no {@link SubProtocolURLConnectionFactory} initialized or {@link URLConnection} open,
 * the {@link #openFallbackConnection(URL, Proxy) fallback strategy} will be applied.
 * <p>
 * If there is no requirement to support the sub-protocol, the subclass only needs to override {@link #openConnection(URL, Proxy)} method.
 * <p>
 * If an instance is instantiated by the default constructor, the implementation class must the obey conventions as follow:
 * <ul>
 *     <li>The class must be the top level</li>
 *     <li>The simple class name must be "Handler"</li>
 *     <li>The class must not be present in the "default" or builtin package({@linkURLUtils #DEFAULT_HANDLER_PACKAGE_PREFIX "sun.net.www.protocol"})</li>
 * </ul>
 * <p>
 * A new instance also can specify some protocol via {@link #ExtendableProtocolURLStreamHandler(String) the constructor with the protocol argument}.
 * <p>
 * Node: these methods are overridden making final:
 * <ul>
 *     <li>{@link #openConnection(URL)}</li>
 *     <li>{@link #parseURL(URL, String, int, int)}</li>
 *     <li>{@link #equals(URL, URL)}</li>
 *     <li>{@link #hostsEqual(URL, URL)}</li>
 *     <li>{@link #hashCode(URL)}</li>
 *     <li>{@link #toExternalForm(URL)}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SubProtocolURLConnectionFactory
 * @see URLStreamHandler
 * @since 1.0.0
 */
public abstract class ExtendableProtocolURLStreamHandler extends URLStreamHandler {

    private final String protocol;

    private final List<SubProtocolURLConnectionFactory> factories = new ArrayList<>();

    /**
     * The default constructor must obey the following conventions:
     * <ul>
     *     <li>The class must be the top level</li>
     *     <li>The simple class name must be "Handler"</li>
     *     <li>The class must not be present in the "default" or builtin package({@link URLUtils#DEFAULT_HANDLER_PACKAGE_PREFIX "sun.net.www.protocol"})</li>
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
        return getProperty(HANDLER_PACKAGES_PROPERTY_NAME);
    }

    public void init() {
        initSubProtocolURLConnectionFactories();
        // register self
        registerURLStreamHandler(this);
    }

    private void initSubProtocolURLConnectionFactories() {
        List<SubProtocolURLConnectionFactory> factories = this.factories;
        initSubProtocolURLConnectionFactories(factories);
        sort(factories, Prioritized.COMPARATOR);
    }

    /**
     * Initialize {@link SubProtocolURLConnectionFactory SubProtocolURLConnectionFactories}
     *
     * @param factories the collection of {@link SubProtocolURLConnectionFactory SubProtocolURLConnectionFactories}
     */
    protected void initSubProtocolURLConnectionFactories(List<SubProtocolURLConnectionFactory> factories) {
    }

    /**
     * Customize {@link SubProtocolURLConnectionFactory SubProtocolURLConnectionFactories}
     *
     * @param factoriesCustomizer the customizer for collection of {@link SubProtocolURLConnectionFactory SubProtocolURLConnectionFactories}
     */
    public void customizeSubProtocolURLConnectionFactories(Consumer<List<SubProtocolURLConnectionFactory>> factoriesCustomizer) {
        factoriesCustomizer.accept(this.factories);
    }

    @Override
    protected final URLConnection openConnection(URL u) throws IOException {
        return openConnection(u, NO_PROXY);
    }

    @Override
    public URLConnection openConnection(URL u, Proxy p) throws IOException {
        List<String> subProtocols = resolveSubProtocols(u);
        URLConnection urlConnection = null;
        int size = factories.size();
        for (int i = 0; i < size; i++) {
            SubProtocolURLConnectionFactory factory = factories.get(i);
            if (factory.supports(u, subProtocols)) {
                urlConnection = factory.create(u, subProtocols, p);
                if (urlConnection != null) {
                    break;
                }
            }
        }
        return urlConnection == null ? openFallbackConnection(u, p) : urlConnection;
    }

    /**
     * The subclass can override this method to open the fallback {@link URLConnection} if
     * any {@link SubProtocolURLConnectionFactory} does not create the {@link URLConnection}.
     *
     * @param url   the URL that this connects to
     * @param proxy {@link Proxy} the proxy through which the connection will be made. If direct connection is desired, Proxy.NO_PROXY should be specified.
     * @return <code>null</code> as default
     * @throws IOException
     */
    protected URLConnection openFallbackConnection(URL url, Proxy proxy) throws IOException {
        return null;
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

    /**
     * Get the sub-protocols from the specified {@link URL}
     *
     * @param url {@link URL}
     * @return non-null
     */
    protected List<String> resolveSubProtocols(URL url) {
        return URLUtils.resolveSubProtocols(url);
    }

    protected String resolveAuthority(URL url) {
        return URLUtils.resolveAuthority(url);
    }

    protected String resolvePath(URL url) {
        return URLUtils.resolvePath(url);
    }

    public final String getProtocol() {
        return protocol;
    }

    @Override
    public String toString() {
        return format("{} {defaultPort = {} , protocol = '{}'}", getClass().getName(), getDefaultPort(), getProtocol());
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

    static void assertClassTopLevel(Class<?> type) {
        if (type.isLocalClass() || type.isAnonymousClass() || type.isMemberClass()) {
            throw new IllegalArgumentException("The implementation " + type + " must be the top level");
        }
    }

    static void assertClassName(Class<?> type) {
        String simpleClassName = type.getSimpleName();
        if (!HANDLER_CONVENTION_CLASS_NAME.equals(simpleClassName)) {
            throw new IllegalArgumentException("The implementation class must name '" + HANDLER_CONVENTION_CLASS_NAME + "', actual : '" + simpleClassName + "'");
        }
    }

    static void assertPackage(Class<?> type) {
        String className = type.getName();
        if (className.indexOf(DOT_CHAR) < 0) {
            throw new IllegalArgumentException("The Handler class must not be present at the top package!");
        }
        String packagePrefix = DEFAULT_HANDLER_PACKAGE_PREFIX;
        if (className.startsWith(packagePrefix)) {
            throw new IllegalArgumentException("The Handler class must not be present in the builtin package : '" + packagePrefix + "'");
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

        setProperty(HANDLER_PACKAGES_PROPERTY_NAME, handlePackages);
    }
}
