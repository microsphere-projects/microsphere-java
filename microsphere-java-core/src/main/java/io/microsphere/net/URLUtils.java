/**
 *
 */
package io.microsphere.net;

import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import io.microsphere.util.BaseUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.MapUtils.isEmpty;
import static io.microsphere.collection.MapUtils.newFixedLinkedHashMap;
import static io.microsphere.constants.PathConstants.BACK_SLASH_CHAR;
import static io.microsphere.constants.PathConstants.DOUBLE_SLASH;
import static io.microsphere.constants.PathConstants.SLASH;
import static io.microsphere.constants.PathConstants.SLASH_CHAR;
import static io.microsphere.constants.ProtocolConstants.EAR_PROTOCOL;
import static io.microsphere.constants.ProtocolConstants.FILE_PROTOCOL;
import static io.microsphere.constants.ProtocolConstants.JAR_PROTOCOL;
import static io.microsphere.constants.ProtocolConstants.WAR_PROTOCOL;
import static io.microsphere.constants.SeparatorConstants.ARCHIVE_ENTRY_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.AND_CHAR;
import static io.microsphere.constants.SymbolConstants.COLON;
import static io.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.microsphere.constants.SymbolConstants.EQUAL_CHAR;
import static io.microsphere.constants.SymbolConstants.QUERY_STRING;
import static io.microsphere.constants.SymbolConstants.QUERY_STRING_CHAR;
import static io.microsphere.constants.SymbolConstants.SEMICOLON_CHAR;
import static io.microsphere.constants.SymbolConstants.SHARP_CHAR;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static io.microsphere.reflect.FieldUtils.setStaticFieldValue;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ClassPathUtils.getClassPaths;
import static io.microsphere.util.StringUtils.EMPTY;
import static io.microsphere.util.StringUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.StringUtils.length;
import static io.microsphere.util.StringUtils.replace;
import static io.microsphere.util.StringUtils.split;
import static io.microsphere.util.StringUtils.substringAfterLast;
import static io.microsphere.util.SystemUtils.FILE_ENCODING;
import static io.microsphere.util.SystemUtils.IS_OS_WINDOWS;
import static io.microsphere.util.jar.JarUtils.resolveRelativePath;
import static java.lang.Character.isWhitespace;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

/**
 * {@link URL} Utility class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see URL
 * @see URLEncoder
 * @see URLDecoder
 * @since 1.0.0
 */
public abstract class URLUtils extends BaseUtils {

    private static final Logger logger = getLogger(URLUtils.class);

    /**
     * The default encoding : "UTF-8"
     */
    public static final String DEFAULT_ENCODING = FILE_ENCODING;

    /**
     * The empty array of {@link URL}
     */
    public static final URL[] EMPTY_URL_ARRAY = new URL[0];

    /**
     * The length of {@link #ARCHIVE_ENTRY_SEPARATOR_LENGTH}
     */
    private static final int ARCHIVE_ENTRY_SEPARATOR_LENGTH = ARCHIVE_ENTRY_SEPARATOR.length();

    /**
     * The prefix of {@link URL} for file protocol : "file:/"
     */
    public static final String FILE_URL_PREFIX = FILE_PROTOCOL + COLON_CHAR + SLASH_CHAR;

    /**
     * The length of {@link #FILE_URL_PREFIX}
     */
    private static final int FILE_URL_PREFIX_LENGTH = FILE_URL_PREFIX.length();

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
     * The prefix of package for {@link URLStreamHandler Handlers}
     */
    public static final String DEFAULT_HANDLER_PACKAGE_PREFIX = "sun.net.www.protocol";

    /**
     * The separator character of  {@link URLStreamHandler Handlers'} packages.
     */
    public static final char HANDLER_PACKAGES_SEPARATOR_CHAR = '|';

    /**
     * The convention class name of {@link URLStreamHandler Handler}.
     */
    public static final String HANDLER_CONVENTION_CLASS_NAME = "Handler";

    /**
     * The matrix name for the URLs' sub-protocol
     */
    public static final String SUB_PROTOCOL_MATRIX_NAME = "_sp";


    /**
     * Convert the <code>url</code> to {@link URL}
     *
     * @param url
     * @return non-null
     * @throws IllegalArgumentException if <code>url</code> is malformed
     */
    public static URL ofURL(String url) {
        try {
            return new URL(url);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Resolve the entry path from Archive File URL
     *
     * @param archiveFileURL Archive File URL
     * @return Relative path in archive
     * @throws NullPointerException <code>archiveFileURL</code> is <code>null</code>
     */
    public static String resolveArchiveEntryPath(URL archiveFileURL) throws NullPointerException {
        // NPE check
        return doResolveArchiveEntryPath(archiveFileURL.getPath());
    }

    protected static String doResolveArchiveEntryPath(String path) {
        int beginIndex = indexOfArchiveEntry(path);
        if (beginIndex > -1) {
            String relativePath = path.substring(beginIndex + ARCHIVE_ENTRY_SEPARATOR_LENGTH);
            return decode(relativePath);
        }
        return null;
    }

    /**
     * Resolve base path from the specified URL
     *
     * @param url the specified URL
     * @return base path
     * @throws NullPointerException if <code>url</code> is <code>null</code>
     */
    public static String resolveBasePath(URL url) throws NullPointerException {
        // NPE check
        return resolvePath(url, false);
    }

    static String resolvePath(URL url, boolean includeArchiveEntryPath) {
        String protocol = url.getProtocol();

        switch (protocol) {
            case FILE_PROTOCOL:
                return resolvePathFromFile(url);
            case JAR_PROTOCOL:
                return resolvePathFromJar(url, includeArchiveEntryPath);
        }

        String path = url.getPath();

        // path may contain the matrix parameters
        int indexOfMatrixString = indexOfMatrixString(path);
        // path removes the matrix parameters if present
        path = indexOfMatrixString > -1 ? path.substring(0, indexOfMatrixString) : path;

        return path;
    }

    static String resolvePathFromFile(URL url) {
        String path = buildPath(url);
        if (IS_OS_WINDOWS) {
            int index = path.indexOf(SLASH_CHAR);
            if (index == 0) {
                path = path.substring(1);
            }
        }
        return path;
    }

    static String buildPath(URL url) {
        String authority = url.getAuthority();
        String path = url.getPath();
        int length = 0;
        int authorityLength = length(authority);
        if (authorityLength > 0) {
            length += authority.length() + 1;
        }
        int pathLength = length(path);
        if (pathLength > 0) {
            length += pathLength;
        }
        StringBuilder pathBuilder = new StringBuilder(length);
        if (authorityLength > 0) {
            pathBuilder.append(SLASH_CHAR)
                    .append(authority);
        }
        if (pathLength > 0) {
            pathBuilder.append(path);
        }
        return pathBuilder.toString();
    }

    static String resolvePathFromJar(URL url, boolean includeArchiveEntryPath) {
        String path = buildPath(url);
        int filePrefixIndex = path.indexOf(FILE_URL_PREFIX);
        if (filePrefixIndex == 0) { //  path starts with "file:/"
            path = path.substring(FILE_URL_PREFIX_LENGTH);
        }

        // path removes the archive entry path if present
        if (!includeArchiveEntryPath) {
            int indexOfArchiveEntry = indexOfArchiveEntry(path);
            path = indexOfArchiveEntry > -1 ? path.substring(0, indexOfArchiveEntry) : path;
        }

        int indexOfColon = path.indexOf(COLON_CHAR);
        if (indexOfColon == -1) { // the path on the Unix/Linux
            // path adds the leading slash if not present
            int indexOfSlash = path.indexOf(SLASH_CHAR);
            if (indexOfSlash != 0) {
                path = SLASH_CHAR + path;
            }
        } // else the path on the Windows

        return path;
    }

    /**
     * Resolve archive file
     *
     * @param resourceURL the URL of resource
     * @return Resolve archive file If exists
     * @throws NullPointerException
     */
    public static File resolveArchiveFile(URL resourceURL) throws NullPointerException {
        String protocol = resourceURL.getProtocol();
        if (FILE_PROTOCOL.equals(protocol)) {
            return resolveArchiveDirectory(resourceURL);
        } else {
            return doResolveArchiveFile(resourceURL);
        }
    }

    protected static File doResolveArchiveFile(URL url) throws NullPointerException {
        String basePath = resolveBasePath(url);
        File archiveFile = new File(basePath);
        return archiveFile.exists() ? archiveFile : null;
    }

    protected static File resolveArchiveDirectory(URL resourceURL) {
        String resourcePath = new File(resourceURL.getFile()).toString();
        Set<String> classPaths = getClassPaths();
        File archiveDirectory = null;
        for (String classPath : classPaths) {
            if (resourcePath.contains(classPath)) {
                archiveDirectory = new File(classPath);
                break;
            }
        }
        return archiveDirectory;
    }

    /**
     * Resolve the query parameters {@link Map} from specified URL，The parameter name as key ，parameter value list as key
     *
     * @param url URL
     * @return Non-null and Read-only {@link Map} , the order of parameters is determined by query string
     */
    @Nonnull
    public static Map<String, List<String>> resolveQueryParameters(String url) {
        String queryString = substringAfterLast(url, QUERY_STRING);
        return resolveParameters(queryString, AND_CHAR);
    }

    /**
     * Resolve the matrix parameters {@link Map} from specified URL，The parameter name as key ，parameter value list as key
     *
     * @param url URL
     * @return Non-null and Read-only {@link Map} , the order of parameters is determined by matrix string
     */
    @Nonnull
    public static Map<String, List<String>> resolveMatrixParameters(String url) {
        int startIndex = url.indexOf(SEMICOLON_CHAR);
        if (startIndex == -1) { // The matrix separator ";" was not found
            return emptyMap();
        }

        int endIndex = url.indexOf(QUERY_STRING_CHAR);
        if (endIndex == -1) { // The query string separator "?" was not found
            endIndex = url.indexOf(SHARP_CHAR);
        }
        if (endIndex == -1) { // The fragment separator "#" was not found
            endIndex = url.length();
        }

        String matrixString = url.substring(startIndex, endIndex);

        return resolveParameters(matrixString, SEMICOLON_CHAR);
    }

    /**
     * Normalize Path(maybe from File or URL), will remove duplicated slash or backslash from path. For example,
     * <p/>
     * <code> resolvePath("C:\\Windows\\\\temp") == "C:/Windows/temp"; resolvePath("C:\\\\\Windows\\/temp") ==
     * "C:/Windows/temp"; resolvePath("/home/////index.html") == "/home/index.html"; </code>
     *
     * @param path Path
     * @return a newly resolved path
     */
    public static String normalizePath(final String path) {

        if (isBlank(path)) {
            return path;
        }

        String resolvedPath = path.trim();

        while (resolvedPath.indexOf(BACK_SLASH_CHAR) > -1) {
            resolvedPath = resolvedPath.replace(BACK_SLASH_CHAR, SLASH_CHAR);
        }

        while (resolvedPath.contains(DOUBLE_SLASH)) {
            resolvedPath = replace(resolvedPath, DOUBLE_SLASH, SLASH);
        }

        return resolvedPath;
    }

    /**
     * Translates a string into <code>application/x-www-form-urlencoded</code> format using a specific encoding scheme.
     * This method uses the supplied encoding scheme to obtain the bytes for unsafe characters.
     * <p/>
     * <em><strong>Note:</strong> The <a href= "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars"> World
     * Wide Web Consortium Recommendation</a> states that UTF-8 should be used. Not doing so may introduce
     * incompatibilites.</em>
     *
     * @param value    <code>String</code> to be translated.
     * @param encoding The name of a supported character encoding</a>.
     * @return the translated <code>String</code>.
     * @throws IllegalArgumentException If the named encoding is not supported
     * @see URLDecoder#decode(String, String)
     */
    public static String encode(String value, String encoding) throws IllegalArgumentException {
        String encodedValue = null;
        try {
            encodedValue = URLEncoder.encode(value, encoding);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        return encodedValue;
    }

    /**
     * {@link #encode(String, String)} with "UTF-8" encoding
     *
     * @param value the <code>String</code> to decode
     * @return the newly encoded <code>String</code>
     */
    public static String encode(String value) {
        return encode(value, DEFAULT_ENCODING);
    }

    /**
     * {@link #decode(String, String)} with "UTF-8" encoding
     *
     * @param value the <code>String</code> to decode
     * @return the newly decoded <code>String</code>
     */
    public static String decode(String value) {
        return decode(value, DEFAULT_ENCODING);
    }

    /**
     * Decodes a <code>application/x-www-form-urlencoded</code> string using a specific encoding scheme. The supplied
     * encoding is used to determine what characters are represented by any consecutive sequences of the form
     * "<code>%<i>xy</i></code>".
     * <p/>
     * <em><strong>Note:</strong> The <a href= "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars"> World
     * Wide Web Consortium Recommendation</a> states that UTF-8 should be used. Not doing so may introduce
     * incompatibilites.</em>
     *
     * @param value    the <code>String</code> to decode
     * @param encoding The name of a supported encoding
     * @return the newly decoded <code>String</code>
     * @throws IllegalArgumentException If character encoding needs to be consulted, but named character encoding is not supported
     */
    public static String decode(String value, String encoding) throws IllegalArgumentException {
        String decodedValue = null;
        try {
            decodedValue = URLDecoder.decode(value, encoding);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        return decodedValue;
    }

    /**
     * Is directory URL?
     *
     * @param url URL
     * @return if directory , return <code>true</code>
     */
    public static boolean isDirectoryURL(URL url) {
        boolean isDirectory = false;
        if (url != null) {
            String protocol = url.getProtocol();
            try {
                if (JAR_PROTOCOL.equals(protocol)) {
                    JarFile jarFile = toJarFile(url); // Test whether valid jar or not
                    final String relativePath = resolveRelativePath(url);
                    if (EMPTY.equals(relativePath)) { // root directory in jar
                        isDirectory = true;
                    } else {
                        JarEntry jarEntry = jarFile.getJarEntry(relativePath);
                        isDirectory = jarEntry != null && jarEntry.isDirectory();
                    }
                } else if (FILE_PROTOCOL.equals(protocol)) {
                    File classPathFile = new File(url.toURI());
                    isDirectory = classPathFile.isDirectory();
                }
            } catch (Exception e) {
                isDirectory = false;
            }
        }
        return isDirectory;
    }

    /**
     * Is Jar URL?
     *
     * @param url URL
     * @return If jar , return <code>true</code>
     */
    public static boolean isJarURL(URL url) {
        String protocol = url.getProtocol();
        boolean flag = false;
        if (FILE_PROTOCOL.equals(protocol)) {
            JarFile jarFile = toJarFile(url);
            flag = jarFile != null;
        } else if (JAR_PROTOCOL.equals(protocol)) {
            flag = true;
        }
        return flag;
    }

    /**
     * Is an archive URL?
     *
     * @param url URL
     * @return If an archive , return <code>true</code>
     */
    public static boolean isArchiveURL(URL url) {
        String protocol = url.getProtocol();
        boolean flag = false;
        switch (protocol) {
            case JAR_PROTOCOL:
            case WAR_PROTOCOL:
            case EAR_PROTOCOL:
                flag = true;
                break;
            case FILE_PROTOCOL:
                JarFile jarFile = toJarFile(url);
                flag = jarFile != null;
        }
        return flag;
    }

    public static JarFile toJarFile(URL url) {
        String path = buildPath(url);
        File file = new File(path);
        if (!file.exists()) {
            if (logger.isTraceEnabled()) {
                logger.trace("The JarFile is not existed from the url : {}", url);
            }
            return null;
        }
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file);
        } catch (IOException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("The JarFile can't be open from the url : {}", url);
            }
        }
        return jarFile;
    }

    /**
     * Build multiple paths to URI
     *
     * @param paths multiple paths
     * @return URI
     */
    public static String buildURI(String... paths) {
        int length = length(paths);
        if (length < 1) {
            return SLASH;
        }

        int capacity = 0;

        for (int i = 0; i < length; i++) {
            capacity += length(paths[i]) + 1;
        }

        StringBuilder uriBuilder = new StringBuilder(capacity);
        for (int i = 0; i < length; i++) {
            String path = paths[i];
            uriBuilder.append(SLASH_CHAR);
            uriBuilder.append(path);
        }

        return normalizePath(uriBuilder.toString());
    }

    /**
     * Build the Matrix String
     *
     * @param matrixParameters the {@link Map} of matrix parameters
     * @return the Matrix String
     */
    public static String buildMatrixString(Map<String, List<String>> matrixParameters) {
        if (isEmpty(matrixParameters)) {
            return null;
        }
        StringBuilder matrixStringBuilder = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : matrixParameters.entrySet()) {
            String name = entry.getKey();
            if (SUB_PROTOCOL_MATRIX_NAME.equals(name)) {
                continue;
            }
            List<String> values = entry.getValue();
            matrixStringBuilder.append(buildMatrixString(name, values.toArray(EMPTY_STRING_ARRAY)));
        }
        return matrixStringBuilder.toString();
    }

    /**
     * Build the Matrix String
     *
     * @param name   the name of matrix parameter
     * @param values the values of matrix parameter
     * @return the Matrix String
     */
    public static String buildMatrixString(String name, String... values) {
        return buildString(name, values, SEMICOLON_CHAR, EQUAL_CHAR);
    }

    /**
     * Converts a URL of a specific protocol to a String.
     *
     * @param url {@link URL}
     * @return non-null
     * @throws NullPointerException If <code>url</code> is <code>null</code>
     */
    public static String toExternalForm(URL url) throws NullPointerException {
        // pre-compute length of StringBuilder
        String protocol = url.getProtocol();
        String authority = url.getAuthority();
        String path = url.getPath();
        String query = url.getQuery();
        String ref = url.getRef();
        String matrix = null;

        int authorityLen = length(authority);
        int pathLen = length(path);
        int queryLen = length(query);
        int refLen = length(ref);

        boolean hasAuthority = authorityLen > 0;
        boolean hasPath = pathLen > 0;
        boolean hasQuery = queryLen > 0;
        boolean hasRef = refLen > 0;
        boolean hasMatrix = false;

        int len = 1;

        if (hasAuthority) {
            protocol = reformProtocol(protocol, authority);
            authority = resolveAuthority(authority);
            len += 2 + authority.length();
        }

        if (hasPath) {
            int indexOfMatrixString = indexOfMatrixString(path);
            if (indexOfMatrixString > -1) {
                hasMatrix = true;
                Map<String, List<String>> matrixParameters = resolveMatrixParameters(path);
                List<String> subProtocols = matrixParameters.getOrDefault(SUB_PROTOCOL_MATRIX_NAME, emptyList());
                protocol = reformProtocol(protocol, subProtocols);
                matrix = buildMatrixString(matrixParameters);
                path = resolvePath(path, indexOfMatrixString);
            }
        }

        len += length(path);
        len += length(protocol);

        if (hasQuery) {
            len += 1 + queryLen;
        }

        if (hasRef) {
            len += 1 + refLen;
        }

        if (hasMatrix) {
            len += length(matrix);
        }

        StringBuilder result = new StringBuilder(len);
        result.append(protocol);
        result.append(COLON_CHAR);

        if (hasAuthority) {
            result.append(DOUBLE_SLASH);
            result.append(authority);
        }

        if (hasPath) {
            result.append(path);
        }

        if (hasMatrix) {
            result.append(matrix);
        }

        if (hasQuery) {
            result.append(QUERY_STRING_CHAR);
            result.append(query);
        }

        if (hasRef) {
            result.append(SHARP_CHAR);
            result.append(ref);
        }

        return result.toString();
    }

    public static String getSubProtocol(String url) {
        Map<String, List<String>> parameters = resolveMatrixParameters(url);
        return getFirst(parameters, SUB_PROTOCOL_MATRIX_NAME);
    }

    public static List<String> resolveSubProtocols(URL url) {
        return resolveSubProtocols(url.toString());
    }

    public static List<String> resolveSubProtocols(String url) {
        String subProtocolsString = findSubProtocolsString(url);
        final List<String> subProtocols;
        if (subProtocolsString == null) {
            Map<String, List<String>> parameters = resolveMatrixParameters(url);
            subProtocols = parameters.get(SUB_PROTOCOL_MATRIX_NAME);
        } else {
            String[] values = split(subProtocolsString, COLON_CHAR);
            subProtocols = ofList(values);
        }
        return subProtocols == null ? emptyList() : subProtocols;
    }

    /**
     * Resolve the protocol from the specified {@link URL} string
     *
     * @param url the {@link URL} string
     * @return <code>null</code> if can't be resolved
     */
    public static String resolveProtocol(String url) {
        if (isBlank(url)) {
            return null;
        }
        int indexOfColon = url.indexOf(COLON_CHAR);
        if (indexOfColon < 1) { // NOT FOUND
            return null;
        }
        for (int i = 0; i <= indexOfColon; i++) {
            if (isWhitespace(url.charAt(i))) {
                if (logger.isTraceEnabled()) {
                    logger.trace("The protocol content should not contain the whitespace[url : '{}' , index : {}]", url, i);
                }
                return null;
            }
        }
        return url.substring(0, indexOfColon);
    }

    private static String findSubProtocolsString(String url) {
        int startIndex = url.indexOf(COLON_CHAR);
        if (startIndex > -1) {
            int endIndex = url.indexOf("://", startIndex);
            if (endIndex > startIndex) {
                return url.substring(startIndex, endIndex);
            }
        }
        return null;
    }

    public static String resolveAuthority(URL url) {
        return resolveAuthority(url.getAuthority());
    }

    public static String resolveAuthority(String authority) {
        return truncateMatrixString(authority);
    }

    public static String resolvePath(URL url) {
        return resolvePath(url, true);
    }

    static String resolvePath(String value, int indexOfMatrixString) {
        return indexOfMatrixString > -1 ? value.substring(0, indexOfMatrixString) : value;
    }

    protected static String truncateMatrixString(String value) {
        int lastIndex = indexOfMatrixString(value);
        return lastIndex > -1 ? value.substring(0, lastIndex) : value;
    }

    protected static int indexOfMatrixString(String value) {
        return value == null ? -1 : value.indexOf(SEMICOLON_CHAR);
    }

    /**
     * Set the specified {@link URLStreamHandlerFactory} for {@link URL URL's} if not set before, otherwise,
     * add it into {@link CompositeURLStreamHandlerFactory} that will be set.
     *
     * @param factory {@link URLStreamHandlerFactory}
     */
    public static void attachURLStreamHandlerFactory(URLStreamHandlerFactory factory) {
        if (factory == null) {
            return;
        }

        URLStreamHandlerFactory oldFactory = getURLStreamHandlerFactory();

        CompositeURLStreamHandlerFactory compositeFactory;
        if (oldFactory == null) { // old factory is absent
            URL.setURLStreamHandlerFactory(factory);
            return;
        } else if (oldFactory instanceof CompositeURLStreamHandlerFactory) {
            compositeFactory = (CompositeURLStreamHandlerFactory) oldFactory;
        } else {
            compositeFactory = new CompositeURLStreamHandlerFactory();
            // Add the old one
            compositeFactory.addURLStreamHandlerFactory(oldFactory);
            // clear the compositeFactory to ensure the invocation of URL.setURLStreamHandlerFactory successfully
            clearURLStreamHandlerFactory();
            URL.setURLStreamHandlerFactory(compositeFactory);
        }

        // Add the new one
        compositeFactory.addURLStreamHandlerFactory(factory);
    }

    public static URLStreamHandlerFactory getURLStreamHandlerFactory() {
        return getStaticFieldValue(URL.class, "factory");
    }

    /**
     * Register an instance of {@link ExtendableProtocolURLStreamHandler}
     *
     * @param handler {@link ExtendableProtocolURLStreamHandler}
     */
    public static void registerURLStreamHandler(ExtendableProtocolURLStreamHandler handler) {
        registerURLStreamHandler(handler.getProtocol(), handler);
    }

    /**
     * Register an instance of {@link URLStreamHandler} with the specified protocol
     *
     * @param protocol the specified protocol of {@link URL}
     * @param handler  {@link URLStreamHandler}
     */
    public static void registerURLStreamHandler(String protocol, URLStreamHandler handler) {
        MutableURLStreamHandlerFactory factory = getMutableURLStreamHandlerFactory(true);
        factory.addURLStreamHandler(protocol, handler);
        attachURLStreamHandlerFactory(factory);
    }

    /**
     * Closes a URLConnection.
     *
     * @param conn the connection to close.
     */
    public static void close(URLConnection conn) {
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection) conn).disconnect();
        }
    }

    protected static MutableURLStreamHandlerFactory getMutableURLStreamHandlerFactory() {
        return getMutableURLStreamHandlerFactory(false);
    }

    protected static MutableURLStreamHandlerFactory getMutableURLStreamHandlerFactory(boolean createIfAbsent) {
        URLStreamHandlerFactory oldFactory = getURLStreamHandlerFactory();
        MutableURLStreamHandlerFactory factory = findMutableURLStreamHandlerFactory(oldFactory);
        if (oldFactory instanceof CompositeURLStreamHandlerFactory) {
            factory = findMutableURLStreamHandlerFactory((CompositeURLStreamHandlerFactory) oldFactory);
        }
        if (factory == null && createIfAbsent) {
            factory = new MutableURLStreamHandlerFactory();
        }
        return factory;
    }

    private static MutableURLStreamHandlerFactory findMutableURLStreamHandlerFactory(CompositeURLStreamHandlerFactory compositeFactory) {
        MutableURLStreamHandlerFactory target = null;
        for (URLStreamHandlerFactory factory : compositeFactory.getFactories()) {
            target = findMutableURLStreamHandlerFactory(factory);
            if (target != null) {
                break;
            }
        }
        return target;
    }

    private static MutableURLStreamHandlerFactory findMutableURLStreamHandlerFactory(URLStreamHandlerFactory factory) {
        if (factory instanceof MutableURLStreamHandlerFactory) {
            return (MutableURLStreamHandlerFactory) factory;
        }
        return null;
    }

    protected static void clearURLStreamHandlerFactory() {
        setStaticFieldValue(URL.class, "factory", null);
    }

    protected static String reformProtocol(String protocol, String spec) {
        List<String> subProtocols = resolveSubProtocols(spec);
        return reformProtocol(protocol, subProtocols);
    }

    protected static String reformProtocol(String protocol, List<String> subProtocols) {
        int size = size(subProtocols);
        if (size < 1) { // the matrix of sub-protocols was not found
            return protocol;
        }

        StringJoiner protocolJoiner = new StringJoiner(COLON);
        protocolJoiner.add(protocol);
        for (int i = 0; i < size; i++) {
            protocolJoiner.add(subProtocols.get(i));
        }

        return protocolJoiner.toString();
    }

    protected static Map<String, List<String>> resolveParameters(String paramsString, char separatorChar) {
        String[] params = split(paramsString, separatorChar);
        int paramsLen = length(params);
        if (paramsLen == 0) {
            return emptyMap();
        }

        Map<String, List<String>> parametersMap = newFixedLinkedHashMap(paramsLen);

        for (int i = 0; i < paramsLen; i++) {
            String param = params[i];
            String[] nameAndValue = split(param, EQUAL_CHAR);
            int len = nameAndValue.length;
            if (len > 0) {
                String name = nameAndValue[0];
                String value = len > 1 ? nameAndValue[1] : EMPTY;
                List<String> paramValueList = parametersMap.get(name);
                if (paramValueList == null) {
                    paramValueList = new LinkedList();
                    parametersMap.put(name, paramValueList);
                }
                paramValueList.add(value);
            }
        }

        return unmodifiableMap(parametersMap);
    }

    protected static String buildString(String name, String[] values, char separator, char joiner) {
        int len = length(values);

        if (len == 0) {
            return null;
        }

        // 2 = length(separator) + length(joiner)
        int capacity = 2 * len + name.length();

        for (int i = 0; i < len; i++) {
            capacity += length(values[i]);
        }

        StringBuilder stringBuilder = new StringBuilder(capacity);

        for (int i = 0; i < len; i++) {
            // ;{name}={value}
            stringBuilder.append(separator).append(name).append(joiner).append(values[i]);
        }

        return stringBuilder.toString();
    }

    protected static String getFirst(Map<String, List<String>> parameters, String name) {
        List<String> values = parameters.get(name);
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private static int indexOfArchiveEntry(String path) {
        return path.indexOf(ARCHIVE_ENTRY_SEPARATOR);
    }

}
