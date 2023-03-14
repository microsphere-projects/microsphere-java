/**
 *
 */
package io.github.microsphere.net;

import io.github.microsphere.util.jar.JarUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.URLStreamHandlerFactory;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.github.microsphere.constants.PathConstants.BACK_SLASH;
import static io.github.microsphere.constants.PathConstants.DOUBLE_SLASH;
import static io.github.microsphere.constants.PathConstants.SLASH;
import static io.github.microsphere.constants.ProtocolConstants.FILE_PROTOCOL;
import static io.github.microsphere.constants.ProtocolConstants.JAR_PROTOCOL;
import static io.github.microsphere.constants.SeparatorConstants.ARCHIVE_ENTITY_SEPARATOR;
import static io.github.microsphere.constants.SymbolConstants.AND_CHAR;
import static io.github.microsphere.constants.SymbolConstants.COLON;
import static io.github.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.github.microsphere.constants.SymbolConstants.EQUAL_CHAR;
import static io.github.microsphere.constants.SymbolConstants.QUERY_STRING;
import static io.github.microsphere.constants.SymbolConstants.QUERY_STRING_CHAR;
import static io.github.microsphere.constants.SymbolConstants.SEMICOLON_CHAR;
import static io.github.microsphere.constants.SymbolConstants.SHARP_CHAR;
import static io.github.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static io.github.microsphere.reflect.FieldUtils.setStaticFieldValue;
import static java.lang.reflect.Array.getLength;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringUtils.length;
import static org.apache.commons.lang3.StringUtils.split;

/**
 * {@link URL} Utility class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see URL
 * @see URLEncoder
 * @see URLDecoder
 * @since 1.0.0
 */
public abstract class URLUtils {

    /**
     * The default encoding : "UTF-8"
     */
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * The matrix name for the URLs' sub-protocol
     */
    public static final String SUB_PROTOCOL_MATRIX_NAME = "_sp";

    /**
     * Resolve Relative path from Archive File URL
     *
     * @param archiveFileURL Archive File URL
     * @return Relative path in archive
     * @throws NullPointerException <code>archiveFileURL</code> is <code>null</code>
     * @version 1.0.0
     * @since 1.0.0
     */
    public static String resolveRelativePath(URL archiveFileURL) throws NullPointerException {
        // NPE check
        String path = archiveFileURL.getPath();
        if (path.contains(ARCHIVE_ENTITY_SEPARATOR)) {
            String relativePath = StringUtils.substringAfterLast(path, ARCHIVE_ENTITY_SEPARATOR);
            return decode(relativePath);
        }
        return null;
    }

    /**
     * Resolve archive file
     *
     * @param archiveFileURL           archive file  URL
     * @param archiveFileExtensionName archive file extension name
     * @return Resolve archive file If exists
     * @throws NullPointerException
     */
    public static File resolveArchiveFile(URL archiveFileURL, String archiveFileExtensionName) throws NullPointerException {
        String archiveFilePath = archiveFileURL.getPath();
        String prefix = ":/";
        boolean hasJarEntryPath = archiveFilePath.contains(ARCHIVE_ENTITY_SEPARATOR);
        String suffix = hasJarEntryPath ? ARCHIVE_ENTITY_SEPARATOR : archiveFileExtensionName;
        String jarPath = StringUtils.substringBetween(archiveFilePath, prefix, suffix);
        File archiveFile = null;
        if (StringUtils.isNotBlank(jarPath)) {
            jarPath = SLASH + URLUtils.decode(jarPath);
            archiveFile = new File(jarPath);
            archiveFile = archiveFile.exists() ? archiveFile : null;
        }
        return archiveFile;
    }


    /**
     * Resolve the query parameters {@link Map} from specified URL，The parameter name as key ，parameter value list as key
     *
     * @param url URL
     * @return Non-null and Read-only {@link Map} , the order of parameters is determined by query string
     */
    @Nonnull
    public static Map<String, List<String>> resolveQueryParameters(String url) {
        String queryString = StringUtils.substringAfterLast(url, QUERY_STRING);
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
     * @version 1.0.0
     * @since 1.0.0
     */
    public static String normalizePath(final String path) {

        if (StringUtils.isBlank(path)) {
            return path;
        }

        String resolvedPath = path.trim();

        while (resolvedPath.contains(BACK_SLASH)) {
            resolvedPath = StringUtils.replace(resolvedPath, BACK_SLASH, SLASH);
        }

        while (resolvedPath.contains(DOUBLE_SLASH)) {
            resolvedPath = StringUtils.replace(resolvedPath, DOUBLE_SLASH, SLASH);
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
     * @since 1.4
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
                    JarFile jarFile = JarUtils.toJarFile(url); // Test whether valid jar or not
                    final String relativePath = JarUtils.resolveRelativePath(url);
                    if (StringUtils.EMPTY.equals(relativePath)) { // root directory in jar
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
            try {
                File file = new File(url.toURI());
                JarFile jarFile = new JarFile(file);
                flag = jarFile != null;
            } catch (Exception e) {
            }
        } else if (JAR_PROTOCOL.equals(protocol)) {
            flag = true;
        }
        return flag;
    }

    /**
     * Build multiple paths to URI
     *
     * @param paths multiple paths
     * @return URI
     */
    public static String buildURI(String... paths) {
        int length = getLength(paths);
        if (length < 1) {
            return SLASH;
        }

        StringBuilder uriBuilder = new StringBuilder(SLASH);
        for (int i = 0; i < length; i++) {
            String path = paths[i];
            uriBuilder.append(path);
            if (i < length - 1) {
                uriBuilder.append(SLASH);
            }
        }

        return normalizePath(uriBuilder.toString());
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
    public static String toString(URL url) throws NullPointerException {
        return toExternalForm(url);
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

        int authorityLen = length(authority);
        int pathLen = length(path);
        int queryLen = length(query);
        int refLen = length(ref);

        boolean hasAuthority = authorityLen > 0;
        boolean hasPath = pathLen > 0;
        boolean hasQuery = queryLen > 0;
        boolean hasRef = refLen > 0;

        int len = 1;

        if (hasAuthority) {
            len += 2 + authority.length();
        }

        if (hasPath) {
            protocol = reformProtocol(protocol, path);
            path = resolvePath(path);
        }

        len += length(path);
        len += length(protocol);

        if (hasQuery) {
            len += 1 + queryLen;
        }

        if (hasRef) {
            len += 1 + refLen;
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

    public static List<String> resolveSubProtocols(String url) {
        Map<String, List<String>> parameters = resolveMatrixParameters(url);
        List<String> values = parameters.get(SUB_PROTOCOL_MATRIX_NAME);
        return values == null ? emptyList() : unmodifiableList(values);
    }

    public static String resolvePath(String path) {
        int lastIndex = path.lastIndexOf(SEMICOLON_CHAR);
        return lastIndex > -1 ? path.substring(0, lastIndex) : path;
    }

    /**
     * Reset the {@link URLStreamHandlerFactory} for {@link URL URL's}
     *
     * @param factory {@link URLStreamHandlerFactory}
     */
    public static void resetURLStreamHandlerFactory(URLStreamHandlerFactory factory) {
        if (factory == null) {
            return;
        }
        URLStreamHandlerFactory oldFactory = getURLStreamHandlerFactory();
        CompositeURLStreamHandlerFactory compositeFactory;

        if (oldFactory instanceof CompositeURLStreamHandlerFactory) {
            compositeFactory = (CompositeURLStreamHandlerFactory) oldFactory;
        } else {
            compositeFactory = new CompositeURLStreamHandlerFactory();
        }

        compositeFactory.addURLStreamHandlerFactory(factory);
        clearURLStreamHandlerFactory();
        URL.setURLStreamHandlerFactory(compositeFactory);
    }

    public static URLStreamHandlerFactory getURLStreamHandlerFactory() {
        return getStaticFieldValue(URL.class, "factory");
    }

    protected static void clearURLStreamHandlerFactory() {
        setStaticFieldValue(URL.class, "factory", null);
    }

    protected static String reformProtocol(String protocol, String path) {
        List<String> subProtocols = resolveSubProtocols(path);
        int size = subProtocols.size();
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
        int paramsLen = params == null ? 0 : params.length;
        if (paramsLen == 0) {
            return emptyMap();
        }

        Map<String, List<String>> parametersMap = new LinkedHashMap(paramsLen, Float.MIN_NORMAL);

        for (int i = 0; i < paramsLen; i++) {
            String param = params[i];
            String[] nameAndValue = split(param, EQUAL_CHAR);
            int len = nameAndValue.length;
            if (len > 0) {
                String name = nameAndValue[0];
                String value = len > 1 ? nameAndValue[1] : StringUtils.EMPTY;
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
        int len = getLength(values);

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
}
