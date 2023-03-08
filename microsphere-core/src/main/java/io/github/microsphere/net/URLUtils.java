/**
 *
 */
package io.github.microsphere.net;

import io.github.microsphere.constants.PathConstants;
import io.github.microsphere.constants.ProtocolConstants;
import io.github.microsphere.constants.SeparatorConstants;
import io.github.microsphere.util.jar.JarUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.github.microsphere.constants.Constants.EQUAL;
import static io.github.microsphere.constants.Constants.SEMICOLON;
import static io.github.microsphere.constants.PathConstants.BACK_SLASH;
import static io.github.microsphere.constants.PathConstants.DOUBLE_SLASH;
import static io.github.microsphere.constants.PathConstants.SLASH;
import static io.github.microsphere.constants.ProtocolConstants.FILE_PROTOCOL;
import static io.github.microsphere.constants.ProtocolConstants.JAR_PROTOCOL;
import static io.github.microsphere.constants.SeparatorConstants.ARCHIVE_ENTITY_SEPARATOR;
import static io.github.microsphere.constants.SymbolConstants.AND;
import static io.github.microsphere.constants.SymbolConstants.QUERY_STRING;

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
     * Resolve parameters {@link Map} from specified URL，The parameter name as key ，parameter value list as key
     *
     * @param url URL
     * @return Non-null and Read-only {@link Map} , the order of parameters is determined by query string
     */
    @Nonnull
    public static Map<String, List<String>> resolveParametersMap(String url) {
        String queryString = StringUtils.substringAfterLast(url, QUERY_STRING);
        if (StringUtils.isNotBlank(queryString)) {
            Map<String, List<String>> parametersMap = new LinkedHashMap();
            String[] queryParams = StringUtils.split(queryString, AND);
            if (queryParams != null) {
                for (String queryParam : queryParams) {
                    String[] paramNameAndValue = StringUtils.split(queryParam, EQUAL);
                    if (paramNameAndValue.length > 0) {
                        String paramName = paramNameAndValue[0];
                        String paramValue = paramNameAndValue.length > 1 ? paramNameAndValue[1] : StringUtils.EMPTY;
                        List<String> paramValueList = parametersMap.get(paramName);
                        if (paramValueList == null) {
                            paramValueList = new LinkedList();
                            parametersMap.put(paramName, paramValueList);
                        }
                        paramValueList.add(paramValue);
                    }
                }
            }
            return Collections.unmodifiableMap(parametersMap);
        } else {
            return Collections.emptyMap();
        }
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
    public static String resolvePath(final String path) {

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
        int length = ArrayUtils.getLength(paths);
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

        return resolvePath(uriBuilder.toString());
    }

    /**
     * Build the Matrix String
     *
     * @param name  the name of matrix parameter
     * @param value the value of matrix parameter
     * @return the Matrix String
     */
    public static String buildMatrixString(String name, String value) {
        return SEMICOLON + name + EQUAL + value;
    }

}
