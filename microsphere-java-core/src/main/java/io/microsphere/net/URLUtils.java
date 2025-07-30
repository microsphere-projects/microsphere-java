/**
 *
 */
package io.microsphere.net;

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.constants.SymbolConstants;
import io.microsphere.logging.Logger;
import io.microsphere.util.ArrayUtils;
import io.microsphere.util.Utils;

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
import static io.microsphere.util.CharSequenceUtils.length;
import static io.microsphere.util.ClassPathUtils.getClassPaths;
import static io.microsphere.util.StringUtils.EMPTY;
import static io.microsphere.util.StringUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.StringUtils.replace;
import static io.microsphere.util.StringUtils.split;
import static io.microsphere.util.StringUtils.substringAfterLast;
import static io.microsphere.util.SystemUtils.FILE_ENCODING;
import static io.microsphere.util.SystemUtils.IS_OS_WINDOWS;
import static io.microsphere.util.jar.JarUtils.resolveRelativePath;
import static java.lang.Character.isWhitespace;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
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
public abstract class URLUtils implements Utils {

    private static final Logger logger = getLogger(URLUtils.class);

    /**
     * The default encoding : "UTF-8"
     */
    public static final String DEFAULT_ENCODING = FILE_ENCODING;

    /**
     * The empty array of {@link URL}
     */
    @Immutable
    public static final URL[] EMPTY_URL_ARRAY = ArrayUtils.EMPTY_URL_ARRAY;

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
     * Converts the provided URL string into a {@link URL} object.
     *
     * <p>This method attempts to create a valid {@link URL} from the given string. If the string does not represent a valid URL,
     * an {@link IllegalArgumentException} is thrown.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Valid URL
     * URL validUrl = URLUtils.ofURL("https://www.example.com");
     *
     * // Invalid URL - will throw IllegalArgumentException
     * try {
     *     URL invalidUrl = URLUtils.ofURL("htp:/invalid-url");
     * } catch (IllegalArgumentException e) {
     *     System.out.println("Invalid URL: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param url The string representation of the URL.
     * @return A non-null {@link URL} object if the input string is a valid URL.
     * @throws IllegalArgumentException if the provided string is not a valid URL.
     */
    @Nonnull
    public static URL ofURL(String url) {
        try {
            return new URL(url);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Resolves the archive entry path from the given URL.
     *
     * <p>This method extracts the part of the URL's path that comes after the archive entry separator.
     * If the URL does not contain an archive entry separator, this method returns {@code null}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: URL with archive entry path
     * URL jarURL = new URL("jar:file:/path/to/archive.jar!/entry/path");
     * String entryPath = URLUtils.resolveArchiveEntryPath(jarURL);
     * System.out.println(entryPath); // Output: entry/path
     *
     * // Example 2: URL without archive entry path
     * URL fileURL = new URL("file:/path/to/archive.jar");
     * entryPath = URLUtils.resolveArchiveEntryPath(fileURL);
     * System.out.println(entryPath); // Output: null
     * }</pre>
     *
     * @param archiveFileURL the URL to resolve the archive entry path from
     * @return the resolved archive entry path if present, or {@code null} otherwise
     * @throws NullPointerException if the provided URL is {@code null}
     */
    @Nullable
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
     * Resolves the base path from the specified URL.
     *
     * <p>This method extracts the main path part of the URL, excluding any archive entry path if present.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: File URL without archive entry path
     * URL fileURL = new URL("file:/path/to/resource.txt");
     * String basePath = URLUtils.resolveBasePath(fileURL);
     * System.out.println(basePath); // Output: /path/to/resource.txt
     *
     * // Example 2: JAR URL with archive entry path
     * URL jarURL = new URL("jar:file:/path/to/archive.jar!/entry/path");
     * basePath = URLUtils.resolveBasePath(jarURL);
     * System.out.println(basePath); // Output: /path/to/archive.jar
     * }</pre>
     *
     * @param url the specified URL
     * @return the resolved base path
     * @throws NullPointerException if the provided URL is {@code null}
     */
    @Nonnull
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
     * Resolves the archive file from the specified URL.
     *
     * <p>If the provided URL uses the "file" protocol, this method delegates to
     * {@link #resolveArchiveDirectory(URL)} to resolve the archive directory. Otherwise,
     * it calls {@link #doResolveArchiveFile(URL)} to handle other protocols like "jar".</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: File URL pointing directly to a JAR file
     * URL fileURL = new URL("file:/path/to/archive.jar");
     * File archiveFile = URLUtils.resolveArchiveFile(fileURL);
     * System.out.println(archiveFile.exists()); // Output: true (if the file exists)
     *
     * // Example 2: JAR URL with an entry path
     * URL jarURL = new URL("jar:file:/path/to/archive.jar!/entry/path");
     * archiveFile = URLUtils.resolveArchiveFile(jarURL);
     * System.out.println(archiveFile.exists()); // Output: true (if the archive exists)
     * }</pre>
     *
     * @param resourceURL the URL to resolve the archive file from
     * @return the resolved archive file if found and exists; otherwise, returns null
     * @throws NullPointerException if the provided URL is {@code null}
     */
    @Nullable
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
     * Resolve the query parameters {@link Map} from specified URL. The parameter name is used as the key, and the list of parameter values is used as the value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example URL with query parameters
     * String url = "https://www.example.com?param1=value1&param2=value2&param1=value3";
     * Map<String, List<String>> params = resolveQueryParameters(url);
     *
     * // Resulting map structure:
     * // {
     * //   "param1" : ["value1", "value3"],
     * //   "param2" : ["value2"]
     * // }
     * }</pre>
     *
     * @param url URL string containing optional query parameters
     * @return Non-null and Read-only {@link Map} where each key is a unique parameter name and the value is a list of values associated with that key.
     */
    @Nonnull
    @Immutable
    public static Map<String, List<String>> resolveQueryParameters(String url) {
        String queryString = substringAfterLast(url, QUERY_STRING);
        return resolveParameters(queryString, AND_CHAR);
    }

    /**
     * Extracts and resolves matrix parameters from the provided URL string.
     *
     * <p>Matrix parameters are typically represented in the URL path using semicolons (;) followed by key-value pairs.
     * This method identifies the segment containing these parameters, parses them, and returns a map where each key
     * is a unique parameter name and the corresponding value is a list of values associated with that key.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: URL with matrix parameters
     * String urlWithMatrix = "/path;key1=valueA;key2=valueB;key1=valueC";
     * Map<String, List<String>> matrixParams = URLUtils.resolveMatrixParameters(urlWithMatrix);
     *
     * // Resulting map structure:
     * // {
     * //   "key1" : ["valueA", "valueC"],
     * //   "key2" : ["valueB"]
     * // }
     *
     * // Example 2: URL without matrix parameters
     * String urlWithoutMatrix = "/path/resource";
     * matrixParams = URLUtils.resolveMatrixParameters(urlWithoutMatrix);
     * System.out.println(matrixParams.isEmpty()); // Output: true
     *
     * // Example 3: URL with matrix and query parameters
     * String urlWithBoth = "/path;key=value?queryKey=queryValue";
     * matrixParams = URLUtils.resolveMatrixParameters(urlWithBoth);
     * System.out.println(matrixParams); // Output: { "key" : ["value"] }
     * }</pre>
     *
     * @param url The URL string potentially containing matrix parameters.
     * @return A non-null and unmodifiable {@link Map} where each key is a unique parameter name and the value is a list of parameter values.
     */
    @Nonnull
    @Immutable
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
     * Normalizes a given path by removing redundant slashes or backslashes and standardizing separators.
     *
     * <p>This method ensures that the resulting path uses forward slashes ({@code /}) as separators,
     * regardless of the operating system, and removes any duplicate separator sequences.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * normalizePath("C:\\Windows\\\\temp")        // returns "C:/Windows/temp"
     * normalizePath("C:\\\\Windows\\/temp")      // returns "C:/Windows/temp"
     * normalizePath("/home/////index.html")       // returns "/home/index.html"
     * normalizePath(null)                         // returns null
     * normalizePath("")                           // returns ""
     * normalizePath("  /a//b/c  ")                // returns "/a/b/c"
     * }</pre>
     *
     * @param path The input path to be normalized. Can be blank or contain mixed/escaped separators.
     * @return A newly resolved, normalized path with standardized separators and no redundant segments.
     */
    @Nullable
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
     * Encodes the provided string using URL encoding with the default encoding scheme.
     *
     * <p>This method delegates to {@link #encode(String, String)} using the default system encoding,
     * typically "UTF-8". It is suitable for scenarios where uniform encoding behavior is desired without explicitly specifying it.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Encoding a simple string
     * String input = "Hello World!";
     * String encoded = URLUtils.encode(input);
     * System.out.println(encoded); // Output: Hello+World%21 (assuming UTF-8 as default)
     *
     * // Encoding a string with special characters
     * input = "Español";
     * encoded = URLUtils.encode(input);
     * System.out.println(encoded); // Output: Espa%F1ol (if default encoding is ISO-8859-1)
     *
     * // Empty input remains unchanged
     * encoded = URLUtils.encode("");
     * System.out.println(encoded); // Output: ""
     *
     * // Null input will throw IllegalArgumentException
     * try {
     *     encoded = URLUtils.encode(null);
     * } catch (IllegalArgumentException e) {
     *     System.out.println("Caught expected null value exception");
     * }
     * }</pre>
     *
     * @param value The string to encode.
     * @return The URL-encoded string using the default encoding.
     * @throws IllegalArgumentException if the provided value is null or the default encoding is not supported.
     */
    @Nonnull
    public static String encode(String value) {
        return encode(value, DEFAULT_ENCODING);
    }

    /**
     * Translates a string into <code>application/x-www-form-urlencoded</code> format using a specific encoding scheme.
     * This method uses the supplied encoding to encode unsafe characters as hexadecimal values.
     *
     * <p>If the provided value is empty or blank, this method returns it unchanged.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Encoding with UTF-8
     * String input = "Hello World!";
     * String encoded = URLUtils.encode(input, "UTF-8");
     * System.out.println(encoded); // Output: Hello+World%21
     *
     * // Encoding with ISO-8859-1
     * input = "Español";
     * encoded = URLUtils.encode(input, "ISO-8859-1");
     * System.out.println(encoded); // Output: Espa%F1ol
     *
     * // Empty input
     * encoded = URLUtils.encode("", "UTF-8");
     * System.out.println(encoded); // Output: ""
     *
     * // Null input will throw IllegalArgumentException
     * try {
     *     encoded = URLUtils.encode(null, "UTF-8");
     * } catch (IllegalArgumentException e) {
     *     System.out.println(e.getMessage()); // Output: java.lang.IllegalArgumentException
     * }
     * }</pre>
     *
     * @param value    the string to encode
     * @param encoding The name of a supported character encoding (e.g., "UTF-8", "ISO-8859-1")
     * @return the URL-encoded string
     * @throws IllegalArgumentException if the value is null or the encoding is not supported
     * @see URLEncoder#encode(String, String)
     */
    @Nonnull
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
     * Decodes a <code>application/x-www-form-urlencoded</code> string using the default encoding.
     *
     * <p>This method delegates to {@link #decode(String, String)} with the default encoding set for the environment,
     * typically "UTF-8". It is useful for scenarios where uniform decoding behavior is desired without explicitly specifying it.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Decoding a simple URL-encoded string
     * String encoded = "Hello+World%21";
     * String decoded = URLUtils.decode(encoded);
     * System.out.println(decoded); // Output: Hello World!
     *
     * // Decoding a string encoded with ISO-8859-1
     * encoded = "Espa%F1ol";
     * decoded = URLUtils.decode(encoded);
     * System.out.println(decoded); // Output: Español (if default encoding is "ISO-8859-1")
     *
     * // Empty input remains unchanged
     * decoded = URLUtils.decode("");
     * System.out.println(decoded); // Output: ""
     *
     * // Null input throws an exception
     * try {
     *     decoded = URLUtils.decode(null);
     * } catch (IllegalArgumentException e) {
     *     System.out.println("Caught expected null value exception");
     * }
     * }</pre>
     *
     * @param value The string to decode.
     * @return The decoded string using the default encoding.
     * @throws IllegalArgumentException if the provided value is null or if the default encoding is not supported.
     */
    @Nonnull
    public static String decode(String value) {
        return decode(value, DEFAULT_ENCODING);
    }

    /**
     * Decodes a <code>application/x-www-form-urlencoded</code> string using a specific encoding scheme. The supplied
     * encoding is used to determine what characters are represented by any consecutive sequences of the form
     * "<code>%<i>xy</i></code>".
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Decoding with UTF-8
     * String encoded = "Hello%20World%21";
     * String decoded = URLUtils.decode(encoded, "UTF-8");
     * System.out.println(decoded); // Output: Hello World!
     *
     * // Decoding with ISO-8859-1
     * encoded = "Espa%F1ol";
     * decoded = URLUtils.decode(encoded, "ISO-8859-1");
     * System.out.println(decoded); // Output: Español
     *
     * // Empty input remains unchanged
     * decoded = URLUtils.decode("", "UTF-8");
     * System.out.println(decoded); // Output: ""
     *
     * // Null input will throw IllegalArgumentException
     * try {
     *     decoded = URLUtils.decode(null, "UTF-8");
     * } catch (IllegalArgumentException e) {
     *     System.out.println("Caught expected null value exception");
     * }
     * }</pre>
     *
     * @param value    the <code>String</code> to decode
     * @param encoding The name of a supported character encoding (e.g., "UTF-8", "ISO-8859-1")
     * @return the newly decoded <code>String</code>
     * @throws IllegalArgumentException If character encoding needs to be consulted, but named character encoding is not supported
     */
    @Nonnull
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
     * Determines whether the specified URL refers to a directory.
     *
     * <p>
     * For "file" protocol URLs, it checks if the corresponding file system resource is a directory.
     * For "jar" protocol URLs, it checks if the referenced archive entry exists and is marked as a directory.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: File URL pointing to a directory
     * URL fileDirURL = new URL("file:/path/to/directory/");
     * boolean isDirectory = URLUtils.isDirectoryURL(fileDirURL);
     * System.out.println(isDirectory); // Output: true
     *
     * // Example 2: JAR URL pointing to a directory within the archive
     * URL jarDirURL = new URL("jar:file:/path/to/archive.jar!/directory/");
     * isDirectory = URLUtils.isDirectoryURL(jarDirURL);
     * System.out.println(isDirectory); // Output: true
     *
     * // Example 3: JAR URL pointing to a file inside the archive
     * URL jarFileURL = new URL("jar:file:/path/to/archive.jar!/file.txt");
     * isDirectory = URLUtils.isDirectoryURL(jarFileURL);
     * System.out.println(isDirectory); // Output: false
     * }</pre>
     *
     * @param url the URL to check
     * @return {@code true} if the URL refers to a directory; otherwise, {@code false}
     * @throws NullPointerException if the provided URL is {@code null}
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
     * Determines if the provided URL refers to a JAR file.
     *
     * <p>
     * If the URL uses the "file" protocol, this method attempts to open the file as a JAR file to verify its validity.
     * If the URL uses the "jar" protocol, it is inherently considered a valid JAR URL.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Valid JAR URL using "file" protocol
     * URL fileJarURL = new URL("file:/path/to/archive.jar");
     * boolean result = URLUtils.isJarURL(fileJarURL);
     * System.out.println(result); // Output: true (if the file exists and is a valid JAR)
     *
     * // Valid JAR URL using "jar" protocol
     * URL jarURL = new URL("jar:file:/path/to/archive.jar!/entry/path");
     * result = URLUtils.isJarURL(jarURL);
     * System.out.println(result); // Output: true
     *
     * // Non-JAR URL
     * URL httpURL = new URL("http://example.com");
     * result = URLUtils.isJarURL(httpURL);
     * System.out.println(result); // Output: false
     * }</pre>
     *
     * @param url The URL to check.
     * @return {@code true} if the URL refers to a valid JAR file; otherwise, returns {@code false}.
     * @throws NullPointerException if the provided URL is {@code null}.
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
     * Determines if the provided URL refers to an archive.
     *
     * <p>
     * This method checks whether the URL's protocol corresponds to known archive types such as "jar",
     * "war", or "ear". For URLs using the "file" protocol, it attempts to open the file as a JAR to verify its validity.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Valid JAR file URL
     * URL jarFileURL = new URL("file:/path/to/archive.jar");
     * boolean result = URLUtils.isArchiveURL(jarFileURL);
     * System.out.println(result); // Output: true (if the file exists and is a valid JAR)
     *
     * // Valid WAR file URL
     * URL warFileURL = new URL("war:file:/path/to/application.war!/WEB-INF/web.xml");
     * result = URLUtils.isArchiveURL(warFileURL);
     * System.out.println(result); // Output: true
     *
     * // Non-archive URL
     * URL httpURL = new URL("http://example.com");
     * result = URLUtils.isArchiveURL(httpURL);
     * System.out.println(result); // Output: false
     *
     * // Invalid JAR file URL
     * URL invalidJarURL = new URL("file:/path/to/invalid.jar");
     * result = URLUtils.isArchiveURL(invalidJarURL);
     * System.out.println(result); // Output: false (if the file cannot be opened as a JAR)
     * }</pre>
     *
     * @param url The URL to check.
     * @return {@code true} if the URL refers to a valid archive; otherwise, returns {@code false}.
     * @throws NullPointerException if the provided URL is {@code null}.
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

    /**
     * Converts the provided URL to a {@link JarFile} instance if it refers to a valid JAR file.
     *
     * <p>This method attempts to open the URL as a JAR file. If the URL uses the "file" protocol,
     * it checks whether the corresponding file exists and is a valid JAR. For other protocols (e.g., "jar"),
     * it tries to extract and open the underlying JAR file.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Valid JAR file URL
     * URL jarURL = new URL("file:/path/to/archive.jar");
     * JarFile jarFile = URLUtils.toJarFile(jarURL);
     * if (jarFile != null) {
     *     System.out.println("Successfully opened JAR file.");
     * } else {
     *     System.out.println("Failed to open JAR file.");
     * }
     *
     * // Example 2: Invalid JAR file URL
     * URL invalidURL = new URL("file:/path/to/invalid.jar");
     * jarFile = URLUtils.toJarFile(invalidURL);
     * System.out.println(jarFile == null); // Output: true
     *
     * // Example 3: URL with "jar" protocol
     * URL urlWithJarProtocol = new URL("jar:file:/path/to/archive.jar!/entry/path");
     * jarFile = URLUtils.toJarFile(urlWithJarProtocol);
     * System.out.println(jarFile != null); // Output: true
     * }</pre>
     *
     * @param url The URL to convert into a JAR file.
     * @return A non-null {@link JarFile} if the URL refers to a valid JAR file; otherwise, returns {@code null}.
     * @throws NullPointerException if the provided URL is {@code null}.
     */
    @Nullable
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
     * Builds a normalized URI path by concatenating the provided path segments with slashes.
     *
     * <p>This method constructs a URI path by joining all provided path segments using forward slashes ('/').
     * It ensures that the resulting path is normalized by removing any redundant slashes or backslashes,
     * and standardizing separators to forward slashes regardless of the operating system.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Basic usage with multiple path segments
     * String result = URLUtils.buildURI("users", "profile", "settings");
     * System.out.println(result); // Output: /users/profile/settings
     *
     * // Example 2: Path with mixed slashes
     * result = URLUtils.buildURI("data\\local", "cache/temp");
     * System.out.println(result); // Output: /data/local/cache/temp
     *
     * // Example 3: Empty input returns a single slash
     * result = URLUtils.buildURI();
     * System.out.println(result); // Output: /
     *
     * // Example 4: Null or blank path segments are skipped
     * result = URLUtils.buildURI("home", null, "docs", "", "file.txt");
     * System.out.println(result); // Output: /home/docs/file.txt
     * }</pre>
     *
     * @param paths The variable-length array of path segments to be joined into a URI path.
     * @return A normalized URI path starting with a forward slash ('/') and containing no duplicate or unnecessary separators.
     */
    @Nonnull
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
     * Builds a matrix string from the provided map of matrix parameters.
     *
     * <p>This method constructs a string representation of matrix parameters suitable for inclusion in a URL path.
     * The resulting string starts with a semicolon (;) followed by key-value pairs separated by semicolons.
     * The special parameter named {@link #SUB_PROTOCOL_MATRIX_NAME} is excluded from the output.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Basic usage with multiple parameters
     * Map<String, List<String>> matrixParams = new LinkedHashMap<>();
     * matrixParams.put("key1", Arrays.asList("valueA", "valueB"));
     * matrixParams.put("key2", Collections.singletonList("valueC"));
     * matrixParams.put("_sp", Arrays.asList("subproto1", "subproto2")); // This will be skipped
     *
     * String matrixString = URLUtils.buildMatrixString(matrixParams);
     * System.out.println(matrixString); // Output: ;key1=valueA;key1=valueB;key2=valueC
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Empty input returns null
     * Map<String, List<String>> emptyMap = Collections.emptyMap();
     * String result = URLUtils.buildMatrixString(emptyMap);
     * System.out.println(result == null); // Output: true
     * }</pre>
     *
     * @param matrixParameters A map containing matrix parameter names as keys and lists of values as map values.
     *                         If this map is empty or null, the method returns null.
     * @return A string representing the matrix parameters, or null if no valid parameters are provided.
     */
    @Nullable
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
     * Builds a matrix parameter string in the format ";name=value1;name=value2..." from the provided values.
     *
     * <p>This method constructs a string representation of matrix parameters suitable for inclusion in a URL path.
     * Each value results in a separate key-value pair using the same parameter name.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Basic usage with multiple values
     * String result = URLUtils.buildMatrixString("key", "valueA", "valueB");
     * System.out.println(result); // Output: ;key=valueA;key=valueB
     *
     * // Example 2: Single value
     * result = URLUtils.buildMatrixString("color", "blue");
     * System.out.println(result); // Output: ;color=blue
     *
     * // Example 3: Null or empty input
     * result = URLUtils.buildMatrixString("empty", null);
     * System.out.println(result == null); // Output: true
     *
     * result = URLUtils.buildMatrixString("empty");
     * System.out.println(result == null); // Output: true
     * }</pre>
     *
     * @param name   the name of the matrix parameter
     * @param values an array of values to associate with the parameter
     * @return a string representing the matrix parameter, or null if no valid values are provided
     */
    @Nonnull
    public static String buildMatrixString(String name, String... values) {
        return buildString(name, values, SEMICOLON_CHAR, EQUAL_CHAR);
    }

    /**
     * Converts the provided {@link URL} to its external form as a string, including all components such as protocol,
     * authority, path, matrix parameters, query, and fragment.
     *
     * <p>This method reconstructs the URL in a standardized format, ensuring proper handling of special components like
     * matrix parameters and sub-protocols. It ensures that the resulting string representation is consistent with the
     * original URL structure.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Basic URL with no special components
     * URL url = new URL("http://example.com/path/to/resource");
     * String externalForm = URLUtils.toExternalForm(url);
     * System.out.println(externalForm); // Output: http://example.com/path/to/resource
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: URL with matrix parameters
     * URL url = new URL("http://example.com/path;key1=valueA;key1=valueB/to/resource");
     * String externalForm = URLUtils.toExternalForm(url);
     * System.out.println(externalForm); // Output: http://example.com/path;key1=valueA;key1=valueB/to/resource
     * }</pre>
     *
     * <pre>{@code
     * // Example 3: URL with sub-protocol specified via matrix parameter "_sp"
     * URL url = new URL("http://example.com/path;_sp=subproto1;_sp=subproto2/to/resource");
     * String externalForm = URLUtils.toExternalForm(url);
     * System.out.println(externalForm); // Output: http://example.com/path;_sp=subproto1;_sp=subproto2/to/resource
     * }</pre>
     *
     * <pre>{@code
     * // Example 4: URL with query and fragment
     * URL url = new URL("http://example.com/path?queryKey=queryValue#section1");
     * String externalForm = URLUtils.toExternalForm(url);
     * System.out.println(externalForm); // Output: http://example.com/path?queryKey=queryValue#section1
     * }</pre>
     *
     * @param url The non-null {@link URL} object to convert to an external form string.
     * @return A non-null string representing the full external form of the URL, including all components.
     * @throws NullPointerException if the provided URL is null.
     */
    @Nonnull
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

    /**
     * Extracts the first sub-protocol value from the specified URL string.
     *
     * <p>This method retrieves the list of values associated with the special matrix parameter
     * named {@link #SUB_PROTOCOL_MATRIX_NAME} and returns the first one if available. If no such
     * parameter exists, it returns null.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: URL with a single sub-protocol
     * String urlWithSingleSubProtocol = "/path;_sp=subproto1/resource";
     * String subProtocol = URLUtils.getSubProtocol(urlWithSingleSubProtocol);
     * System.out.println(subProtocol); // Output: "subproto1"
     *
     * // Example 2: URL with multiple sub-protocols
     * String urlWithMultipleSubProtocols = "/path;_sp=subproto1;_sp=subproto2/resource";
     * subProtocol = URLUtils.getSubProtocol(urlWithMultipleSubProtocols);
     * System.out.println(subProtocol); // Output: "subproto1" (only the first one is returned)
     *
     * // Example 3: URL without any sub-protocol
     * String urlWithoutSubProtocol = "/path/to/resource";
     * subProtocol = URLUtils.getSubProtocol(urlWithoutSubProtocol);
     * System.out.println(subProtocol); // Output: null
     * }</pre>
     *
     * @param url The URL string potentially containing sub-protocol information.
     * @return The first sub-protocol value if present, or null if none is found.
     */
    @Nullable
    public static String getSubProtocol(String url) {
        Map<String, List<String>> parameters = resolveMatrixParameters(url);
        return getFirst(parameters, SUB_PROTOCOL_MATRIX_NAME);
    }

    /**
     * Resolves the list of sub-protocols from the specified {@link URL}.
     *
     * <p>This method extracts sub-protocol information from the URL's path using matrix parameters.
     * Sub-protocols are identified by the special matrix parameter name {@link #SUB_PROTOCOL_MATRIX_NAME} ("_sp").
     * If no such parameter is present, an empty list is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: URL with multiple sub-protocols
     * URL urlWithMultipleSubProtocols = new URL("http://example.com/path;_sp=subproto1;_sp=subproto2/resource");
     * List<String> subProtocols = URLUtils.resolveSubProtocols(urlWithMultipleSubProtocols);
     * System.out.println(subProtocols); // Output: [subproto1, subproto2]
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: URL with a single sub-protocol
     * URL urlWithSingleSubProtocol = new URL("http://example.com/path;_sp=subproto1/resource");
     * List<String> subProtocols = URLUtils.resolveSubProtocols(urlWithSingleSubProtocol);
     * System.out.println(subProtocols); // Output: [subproto1]
     * }</pre>
     *
     * <pre>{@code
     * // Example 3: URL without any sub-protocols
     * URL urlWithoutSubProtocols = new URL("http://example.com/path/to/resource");
     * List<String> subProtocols = URLUtils.resolveSubProtocols(urlWithoutSubProtocols);
     * System.out.println(subProtocols); // Output: []
     * }</pre>
     *
     * @param url The non-null {@link URL} object to extract sub-protocols from.
     * @return A non-null list of sub-protocol strings. If no sub-protocols are found, returns an empty list.
     * @throws NullPointerException if the provided URL is null.
     */
    @Nonnull
    @Immutable
    public static List<String> resolveSubProtocols(URL url) {
        return resolveSubProtocols(url.toString());
    }

    /**
     * Resolves the list of sub-protocols from the specified URL string.
     *
     * <p>This method extracts sub-protocol information either from the special matrix parameter named
     * {@link #SUB_PROTOCOL_MATRIX_NAME} ("_sp") or directly from the protocol segment if it contains
     * multiple protocols separated by colons. If no sub-protocols are found, an empty list is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: URL with multiple sub-protocols via matrix parameters
     * String urlWithMatrixSubProtocols = "/path;_sp=subproto1;_sp=subproto2/resource";
     * List<String> subProtocols = URLUtils.resolveSubProtocols(urlWithMatrixSubProtocols);
     * System.out.println(subProtocols); // Output: [subproto1, subproto2]
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: URL with a single sub-protocol in the protocol segment
     * String urlWithProtocolSubProtocol = "http:subproto1://example.com/path";
     * List<String> subProtocols = URLUtils.resolveSubProtocols(urlWithProtocolSubProtocol);
     * System.out.println(subProtocols); // Output: [subproto1]
     * }</pre>
     *
     * <pre>{@code
     * // Example 3: URL with multiple sub-protocols in the protocol segment
     * String urlWithMultipleProtocolSubProtocols = "custom:subproto1:subproto2:path/to/resource";
     * List<String> subProtocols = URLUtils.resolveSubProtocols(urlWithMultipleProtocolSubProtocols);
     * System.out.println(subProtocols); // Output: [subproto1, s
     * ```ubproto2]
     * }</pre>
     *
     * <pre>{@code
     * // Example 4: URL without any sub-protocols
     * String urlWithoutSubProtocols = "http://example.com/path/to/resource";
     * List<String> subProtocols = URLUtils.resolveSubProtocols(urlWithoutSubProtocols);
     * System.out.println(subProtocols); // Output: []
     * }</pre>
     *
     * @param url The URL string potentially containing sub-protocol information.
     * @return A non-null list of sub-protocol strings. If no sub-protocols are found, returns an empty list.
     */
    @Nonnull
    @Immutable
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
        return subProtocols == null ? emptyList() : unmodifiableList(subProtocols);
    }

    /**
     * Resolves the protocol from the specified URL string.
     *
     * <p>This method extracts the protocol component from a given URL string by identifying the portion
     * before the first colon (':') character. It ensures that the protocol does not contain any whitespace
     * characters, logging a trace message and returning {@code null} if such a condition is found.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Basic usage with a standard HTTP URL
     * String url = "http://example.com";
     * String protocol = URLUtils.resolveProtocol(url);
     * System.out.println(protocol); // Output: http
     *
     * // Example 2: URL with sub-protocols in the protocol segment
     * url = "custom:subproto1:subproto2://example.com";
     * protocol = URLUtils.resolveProtocol(url);
     * System.out.println(protocol); // Output: custom
     *
     * // Example 3: URL with matrix parameters containing sub-protocols
     * url = "/path;_sp=subproto1;_sp=subproto2/resource";
     * protocol = URLUtils.resolveProtocol(url);
     * System.out.println(protocol); // Output: null (since no protocol exists before the colon)
     *
     * // Example 4: Invalid URL with whitespace in protocol name
     * url = "ht tp://example.com";
     * protocol = URLUtils.resolveProtocol(url);
     * System.out.println(protocol); // Output: null
     *
     * // Example 5: Blank input returns null
     * protocol = URLUtils.resolveProtocol("   ");
     * System.out.println(protocol); // Output: null
     * }</pre>
     *
     * @param url The URL string to extract the protocol from.
     * @return The resolved protocol if valid; otherwise, returns {@code null}.
     */
    @Nullable
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

    /**
     * Resolves and returns the authority component from the provided {@link URL}.
     *
     * <p>The authority is defined as the part of the URL after the protocol (e.g., "http" or "ftp")
     * and before the path, query, or fragment. It typically includes host and port information.
     * This method delegates to {@link #resolveAuthority(String)} for processing the actual authority string.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Basic usage with a simple URL
     * URL url = new URL("http://example.com/path/to/resource");
     * String authority = URLUtils.resolveAuthority(url);
     * System.out.println(authority); // Output: example.com
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: URL with port number
     * URL urlWithPort = new URL("http://example.com:8080/path/to/resource");
     * String authority = URLUtils.resolveAuthority(urlWithPort);
     * System.out.println(authority); // Output: example.com:8080
     * }</pre>
     *
     * <pre>{@code
     * // Example 3: URL with user info and port
     * URL urlWithUserInfo = new URL("http://user:pass@example.com:9090/path");
     * authority = URLUtils.resolveAuthority(urlWithUserInfo);
     * System.out.println(authority); // Output: user:pass@example.com:9090
     * }</pre>
     *
     * @param url The non-null {@link URL} object to extract the authority from.
     * @return A non-null string representing the resolved authority part of the URL.
     * @throws NullPointerException if the provided URL is null.
     */
    public static String resolveAuthority(URL url) {
        return resolveAuthority(url.getAuthority());
    }

    /**
     * Resolves the authority part from the specified URL string by truncating any matrix parameters.
     *
     * <p>
     * This method removes matrix parameters (identified by a semicolon '{@link SymbolConstants#SEMICOLON_CHAR}') and returns the base authority.
     * It is commonly used to clean up URLs before further processing or comparison.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Authority with matrix parameters
     * String authorityWithMatrix = "example.com;param=value";
     * String resolvedAuthority = URLUtils.resolveAuthority(authorityWithMatrix);
     * System.out.println(resolvedAuthority); // Output: example.com
     *
     * // Example 2: Simple authority without matrix parameters
     * String simpleAuthority = "localhost:8080";
     * resolvedAuthority = URLUtils.resolveAuthority(simpleAuthority);
     * System.out.println(resolvedAuthority); // Output: localhost:8080
     *
     * // Example 3: Null or empty input
     * resolvedAuthority = URLUtils.resolveAuthority("");
     * System.out.println(resolvedAuthority); // Output: (empty string)
     *
     * resolvedAuthority = URLUtils.resolveAuthority(null);
     * System.out.println(resolvedAuthority); // Output: null
     * }</pre>
     *
     * @param authority The URL authority string potentially containing matrix parameters.
     * @return the resolved authority string without matrix parameters, or {@code null} if the input is null or blank after processing
     */
    @Nullable
    public static String resolveAuthority(String authority) {
        return truncateMatrixString(authority);
    }

    /**
     * Resolves the path component from the specified URL, excluding any matrix parameters.
     *
     * <p>This method delegates to {@link #resolvePath(URL, boolean)} with the second argument set to true,
     * ensuring that the returned path includes the archive entry path if present.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: File URL without matrix parameters
     * URL fileURL = new URL("file:/path/to/resource.txt");
     * String path = URLUtils.resolvePath(fileURL);
     * System.out.println(path); // Output: /path/to/resource.txt
     *
     * // Example 2: JAR URL with archive entry path
     * URL jarURL = new URL("jar:file:/path/to/archive.jar!/entry/path");
     * path = URLUtils.resolvePath(jarURL);
     * System.out.println(path); // Output: /path/to/archive.jar!/entry/path
     *
     * // Example 3: URL with matrix parameters (these will be ignored)
     * URL urlWithMatrix = new URL("http://example.com/path;key=value");
     * path = URLUtils.resolvePath(urlWithMatrix);
     * System.out.println(path); // Output: /path
     * }</pre>
     *
     * @param url the URL to extract and resolve the path from
     * @return the resolved path as a string, excluding matrix parameters
     * @throws NullPointerException if the provided URL is null
     */
    @Nonnull
    public static String resolvePath(URL url) {
        return resolvePath(url, true);
    }

    /**
     * Attaches the specified {@link URLStreamHandlerFactory} to the current JVM.
     *
     * <p>This method ensures that the provided factory is integrated into the existing handler chain.
     * If no global factory is currently set, this method directly sets it as the system-wide factory.
     * If an existing factory is already present and it's not a {@link CompositeURLStreamHandlerFactory},
     * a new composite factory will be created to encapsulate both the existing and new factories.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Setting up a custom URLStreamHandlerFactory for "myproto" protocol
     * public class MyProtoURLStreamHandlerFactory implements URLStreamHandlerFactory {
     *     @Override
     *     public URLStreamHandler createURLStreamHandler(String protocol) {
     *         if ("myproto".equals(protocol)) {
     *             return new URLStreamHandler() {
     *                 @Override
     *                 protected URLConnection openConnection(URL u) throws IOException {
     *                     return null; // implementation omitted for brevity
     *                 }
     *             };
     *         }
     *         return null;
     *     }
     * }
     *
     * // Attach the custom factory
     * URLUtils.attachURLStreamHandlerFactory(new MyProtoURLStreamHandlerFactory());
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Adding to an existing factory setup
     * URLStreamHandlerFactory existingFactory = URL.getURLStreamHandlerFactory();
     * // Assume existingFactory is non-null and not a CompositeURLStreamHandlerFactory
     *
     * // Create and attach a new factory
     * URLStreamHandlerFactory newFactory = new MyProtoURLStreamHandlerFactory();
     * URLUtils.attachURLStreamHandlerFactory(newFactory);
     *
     * // The resulting factory will be a CompositeURLStreamHandlerFactory containing both
     * // the existing factory and the new one
     * }</pre>
     *
     * @param factory the URLStreamHandlerFactory to be attached; if null, this method has no effect
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

    /**
     * Retrieves the current system-wide {@link URLStreamHandlerFactory}.
     *
     * <p>This method accesses the private static field "factory" in the {@link URL} class using reflection.
     * It is useful for inspecting or modifying the existing handler factory chain, especially when integrating
     * custom protocol handlers.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Get the current global URLStreamHandlerFactory
     * URLStreamHandlerFactory currentFactory = URLUtils.getURLStreamHandlerFactory();
     * if (currentFactory != null) {
     *     System.out.println("Current factory: " + currentFactory.getClass().getName());
     * } else {
     *     System.out.println("No global factory is currently set.");
     * }
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Check if the current factory is a CompositeURLStreamHandlerFactory
     * URLStreamHandlerFactory factory = URLUtils.getURLStreamHandlerFactory();
     * if (factory instanceof CompositeURLStreamHandlerFactory) {
     *     System.out.println("The current factory is a composite factory.");
     * } else {
     *     System.out.println("The current factory is not a composite factory.");
     * }
     * }</pre>
     *
     * @return The current global {@link URLStreamHandlerFactory}, or null if none is set.
     * @see URLStreamHandlerFactory
     * @see CompositeURLStreamHandlerFactory
     */
    @Nullable
    public static URLStreamHandlerFactory getURLStreamHandlerFactory() {
        return getStaticFieldValue(URL.class, "factory");
    }

    /**
     * Registers the specified {@link ExtendableProtocolURLStreamHandler} for its protocol.
     *
     * <p>This method delegates to
     * {@link #registerURLStreamHandler(String, URLStreamHandler)} using the protocol obtained from
     * the given handler via {@link ExtendableProtocolURLStreamHandler#getProtocol()}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example: Registering a custom URLStreamHandler for the "myproto" protocol
     * public class MyProtoURLStreamHandler extends ExtendableProtocolURLStreamHandler {
     *     public MyProtoURLStreamHandler() {
     *         super("myproto");
     *     }
     *
     *     @Override
     *     protected void initSubProtocolURLConnectionFactories(List<SubProtocolURLConnectionFactory> factories) {
     *         // Initialization logic here
     *     }
     *
     *     @Override
     *     protected URLConnection openFallbackConnection(URL url, Proxy proxy) throws IOException {
     *         return null; // Fallback logic here
     *     }
     * }
     *
     * // Register the handler
     * URLUtils.registerURLStreamHandler(new MyProtoURLStreamHandler());
     * }</pre>
     *
     * @param handler the handler to register for its protocol
     * @throws NullPointerException if the provided handler is null
     */
    public static void registerURLStreamHandler(ExtendableProtocolURLStreamHandler handler) {
        registerURLStreamHandler(handler.getProtocol(), handler);
    }

    /**
     * Registers a {@link URLStreamHandler} for the specified protocol.
     *
     * <p>This method ensures that the provided handler is associated with the given protocol and integrated into the system-wide
     * URL stream handler mechanism. If no global factory is currently set, a new {@link MutableURLStreamHandlerFactory} will be created,
     * and the handler will be registered with it. If there's an existing factory, this method attempts to locate or create a composite
     * structure to include the new handler while preserving existing ones.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Example 1: Registering a custom URLStreamHandler for "myproto" protocol
     * public class MyProtoURLStreamHandler extends URLStreamHandler {
     *     @Override
     *     protected URLConnection openConnection(URL u) throws IOException {
     *         return null; // implementation omitted for brevity
     *     }
     * }
     *
     * // Register the handler
     * URLUtils.registerURLStreamHandler("myproto", new MyProtoURLStreamHandler());
     * }</pre>
     *
     * <pre>{@code
     * // Example 2: Reusing an existing CompositeURLStreamHandlerFactory setup
     * URLStreamHandlerFactory existingFactory = URL.getURLStreamHandlerFactory();
     * // Assume existingFactory is non-null and not a CompositeURLStreamHandlerFactory
     *
     * // Register a new handler
     * URLUtils.registerURLStreamHandler("myproto", new MyProtoURLStreamHandler());
     *
     * // The resulting factory will now be a CompositeURLStreamHandlerFactory containing both
     * // the existing factory and the new one
     * }</pre>
     *
     * @param protocol the name of the protocol for which the handler is being registered
     * @param handler  the URLStreamHandler instance to register; if null, this method has no effect
     */
    public static void registerURLStreamHandler(String protocol, URLStreamHandler handler) {
        MutableURLStreamHandlerFactory factory = getMutableURLStreamHandlerFactory(true);
        factory.addURLStreamHandler(protocol, handler);
        attachURLStreamHandlerFactory(factory);
    }

    /**
     * Closes the specified URL connection gracefully.
     *
     * <p>If the provided connection is an instance of {@link HttpURLConnection},
     * it will be disconnected using its {@code disconnect()} method. This ensures
     * proper resource cleanup for HTTP connections.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * URL url = new URL("https://example.com");
     * URLConnection conn = url.openConnection();
     * try {
     *     // Use the connection
     * } finally {
     *     URLUtils.close(conn);
     * }
     * }</pre>
     *
     * @param conn The URL connection to close; may be null.
     */
    public static void close(URLConnection conn) {
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection) conn).disconnect();
        }
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

    private URLUtils() {
    }
}
