/**
 *
 */
package io.microsphere.util.jar;

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.constants.ProtocolConstants;
import io.microsphere.filter.JarEntryFilter;
import io.microsphere.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.constants.ProtocolConstants.FILE_PROTOCOL;
import static io.microsphere.constants.ProtocolConstants.JAR_PROTOCOL;
import static io.microsphere.constants.SeparatorConstants.ARCHIVE_ENTRY_SEPARATOR;
import static io.microsphere.io.IOUtils.close;
import static io.microsphere.io.IOUtils.copy;
import static io.microsphere.net.URLUtils.decode;
import static io.microsphere.net.URLUtils.normalizePath;
import static io.microsphere.net.URLUtils.resolveArchiveFile;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.StringUtils.substringAfter;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * Jar Utility class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see JarEntry
 * @see JarFile
 * @since 1.0.0
 */
public abstract class JarUtils implements Utils {

    /**
     * The resource path of Manifest file in JAR archive.
     * Typically located under {@code META-INF/MANIFEST.MF} in standard Java archives.
     */
    public static final String MANIFEST_RESOURCE_PATH = "META-INF/MANIFEST.MF";

    /**
     * Creates a {@link JarFile} from the specified {@link URL}.
     *
     * <p>
     * This method resolves the absolute path of the JAR file from the provided URL and constructs
     * a new {@link JarFile} instance. If the URL does not point to a valid JAR or file resource,
     * this method returns {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * URL jarURL = new URL("jar:file:/path/to/file.jar!/entry");
     * JarFile jarFile = JarUtils.toJarFile(jarURL);
     * }</pre>
     *
     * @param jarURL the URL pointing to a JAR file or entry; must not be {@code null}
     * @return a new {@link JarFile} instance if resolved successfully, or {@code null} if resolution fails
     * @throws IOException if an I/O error occurs while creating the JAR file
     */
    @Nullable
    public static JarFile toJarFile(URL jarURL) throws IOException {
        JarFile jarFile = null;
        final String jarAbsolutePath = resolveJarAbsolutePath(jarURL);
        if (jarAbsolutePath == null)
            return null;
        jarFile = new JarFile(jarAbsolutePath);
        return jarFile;
    }

    /**
     * Assert <code>jarURL</code> argument is valid , only supported protocols : {@link ProtocolConstants#JAR_PROTOCOL jar} and
     * {@link ProtocolConstants#FILE_PROTOCOL file}
     *
     * @param jarURL {@link URL} of {@link JarFile} or {@link JarEntry}
     * @throws NullPointerException     If <code>jarURL</code> is <code>null</code>
     * @throws IllegalArgumentException If {@link URL#getProtocol()} is not {@link ProtocolConstants#JAR_PROTOCOL jar} or {@link ProtocolConstants#FILE_PROTOCOL
     *                                  file}
     */
    protected static void assertJarURLProtocol(URL jarURL) throws NullPointerException, IllegalArgumentException {
        final String protocol = jarURL.getProtocol(); //NPE check
        if (!JAR_PROTOCOL.equals(protocol) && !FILE_PROTOCOL.equals(protocol)) {
            String message = format("the protocol['{}'] of 'jarURL' is unsupported, except '{}' and '{}' ", protocol, JAR_PROTOCOL, FILE_PROTOCOL);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Resolves the relative path from the given JAR URL.
     *
     * <p>This method extracts the part of the URL after the archive entry separator (e.g., "!/")
     * and decodes it to provide a normalized relative path within the JAR archive.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * URL jarURL = new URL("jar:file:/path/to/file.jar!/com/example/resource.txt");
     * String relativePath = JarUtils.resolveRelativePath(jarURL);
     * System.out.println(relativePath);  // Output: com/example/resource.txt
     * }</pre>
     *
     * @param jarURL the URL pointing to a resource within a JAR file; must not be {@code null}
     * @return the resolved relative path within the JAR archive
     * @throws NullPointerException     if the provided {@code jarURL} is {@code null}
     * @throws IllegalArgumentException if the URL protocol is neither "jar" nor "file"
     */
    @Nullable
    public static String resolveRelativePath(URL jarURL) throws NullPointerException, IllegalArgumentException {
        assertJarURLProtocol(jarURL);
        String form = jarURL.toExternalForm();
        String relativePath = substringAfter(form, ARCHIVE_ENTRY_SEPARATOR);
        relativePath = normalizePath(relativePath);
        return decode(relativePath);
    }

    /**
     * Resolves the absolute path of the JAR file from the provided URL.
     *
     * <p>
     * This method ensures that the URL protocol is either "jar" or "file", and then resolves
     * the absolute path to the corresponding JAR archive on the file system. If the URL does not
     * point to a valid JAR or file resource, this method returns {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * URL jarURL = new URL("jar:file:/path/to/file.jar!/com/example/resource.txt");
     * String absolutePath = JarUtils.resolveJarAbsolutePath(jarURL);
     * System.out.println(absolutePath);  // Output: /path/to/file.jar
     * }</pre>
     *
     * @param jarURL the URL pointing to a JAR file or entry; must not be {@code null}
     * @return the resolved absolute path of the JAR file if successful, or {@code null} if resolution fails
     * @throws NullPointerException     if the provided {@code jarURL} is {@code null}
     * @throws IllegalArgumentException if the URL protocol is neither "jar" nor "file"
     */
    @Nullable
    public static String resolveJarAbsolutePath(URL jarURL) throws NullPointerException, IllegalArgumentException {
        assertJarURLProtocol(jarURL);
        File archiveFile = resolveArchiveFile(jarURL);
        return archiveFile == null ? null : archiveFile.getAbsolutePath();
    }

    /**
     * Filters the entries of a JAR file based on the provided {@link JarEntryFilter}.
     *
     * <p>This method iterates through all entries in the given JAR file and applies the filter to selectively include
     * or exclude entries. If the provided {@link JarFile} is null or empty, an empty list is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JarFile jarFile = new JarFile("example.jar");
     * JarEntryFilter classFileFilter = entry -> entry.getName().endsWith(".class");
     * List<JarEntry> filteredEntries = JarUtils.filter(jarFile, classFileFilter);
     * }</pre>
     *
     * @param jarFile        The source JAR file; may be null.
     * @param jarEntryFilter The filter used to determine which entries to include; may be null (no filtering).
     * @return A read-only list of filtered JAR entries. Never null.
     */
    @Nonnull
    @Immutable
    public static List<JarEntry> filter(JarFile jarFile, JarEntryFilter jarEntryFilter) {
        if (jarFile == null) {
            return emptyList();
        }
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        List<JarEntry> jarEntriesList = ofList(jarEntries);
        return doFilter(jarEntriesList, jarEntryFilter);
    }

    @Nonnull
    @Immutable
    protected static List<JarEntry> doFilter(Iterable<JarEntry> jarEntries, JarEntryFilter jarEntryFilter) {
        List<JarEntry> jarEntriesList = new LinkedList<>();
        for (JarEntry jarEntry : jarEntries) {
            if (jarEntryFilter == null || jarEntryFilter.accept(jarEntry)) {
                jarEntriesList.add(jarEntry);
            }
        }
        return unmodifiableList(jarEntriesList);
    }

    /**
     * Finds and returns the {@link JarEntry} from the specified JAR URL.
     *
     * <p>
     * This method resolves the relative path within the JAR archive from the provided URL and retrieves
     * the corresponding entry. If the entry does not exist or if there's an issue accessing the JAR file,
     * an exception may be thrown or a null value returned depending on underlying behavior.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * URL jarURL = new URL("jar:file:/path/to/file.jar!/com/example/resource.txt");
     * JarEntry jarEntry = JarUtils.findJarEntry(jarURL);
     * System.out.println(jarEntry.getName());  // Output: com/example/resource.txt
     * }</pre>
     *
     * @param jarURL the URL pointing to a resource within a JAR file; must not be {@code null}
     * @return the resolved {@link JarEntry} if found, or {@code null} if no such entry exists
     * @throws IOException if an I/O error occurs while reading the JAR file or resolving the entry
     */
    @Nullable
    public static JarEntry findJarEntry(URL jarURL) throws IOException {
        JarFile jarFile = toJarFile(jarURL);
        final String relativePath = resolveRelativePath(jarURL);
        JarEntry jarEntry = jarFile.getJarEntry(relativePath);
        return jarEntry;
    }

    /**
     * Extracts the contents of the specified JAR file to the given target directory.
     *
     * <p>
     * This method extracts all entries from the provided JAR file into the target directory,
     * preserving the original directory structure. If the JAR file contains nested directories,
     * they will be recreated under the target directory.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * File jarFile = new File("example.jar");
     * File outputDir = new File("/path/to/output");
     * JarUtils.extract(jarFile, outputDir);
     * }</pre>
     *
     * @param jarSourceFile   the source JAR file to extract; must not be {@code null}
     * @param targetDirectory the target directory where contents will be extracted; must not be {@code null}
     * @throws IOException if an I/O error occurs during extraction or if the provided file is not a valid JAR
     */
    public static void extract(File jarSourceFile, File targetDirectory) throws IOException {
        extract(jarSourceFile, targetDirectory, null);
    }

    /**
     * Extracts the contents of the specified JAR file to the given target directory, optionally filtering entries.
     *
     * <p>
     * This method extracts entries from the provided JAR file into the target directory. If a filter is provided,
     * only entries accepted by the filter will be extracted. The original directory structure of the JAR is preserved
     * under the target directory.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * File jarFile = new File("example.jar");
     * File outputDir = new File("/path/to/output");
     *
     * // Extract all entries
     * JarUtils.extract(jarFile, outputDir, null);
     *
     * // Extract only .class files
     * JarEntryFilter classFileFilter = entry -> entry.getName().endsWith(".class");
     * JarUtils.extract(jarFile, outputDir, classFileFilter);
     * }</pre>
     *
     * @param jarSourceFile   the source JAR file to extract; must not be {@code null}
     * @param targetDirectory the target directory where contents will be extracted; must not be {@code null}
     * @param jarEntryFilter  an optional filter to restrict which entries are extracted; may be {@code null} to extract all entries
     * @throws IOException if an I/O error occurs during extraction or if the provided file is not a valid JAR
     */
    public static void extract(File jarSourceFile, File targetDirectory, JarEntryFilter jarEntryFilter) throws IOException {
        final JarFile jarFile = new JarFile(jarSourceFile);
        extract(jarFile, targetDirectory, jarEntryFilter);
    }

    /**
     * Extracts entries from a JAR file to the specified target directory, optionally filtering which entries to extract.
     *
     * <p>This method filters the entries in the JAR file using the provided {@link JarEntryFilter}, and extracts only those
     * entries that are accepted by the filter. If no filter is provided (i.e., {@code null}), all entries will be extracted.
     * The directory structure within the JAR is preserved under the target directory.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JarFile jarFile = new JarFile("example.jar");
     * File outputDir = new File("/path/to/output");
     *
     * // Extract all entries
     * JarUtils.extract(jarFile, outputDir, null);
     *
     * // Extract only .class files
     * JarEntryFilter classFileFilter = entry -> entry.getName().endsWith(".class");
     * JarUtils.extract(jarFile, outputDir, classFileFilter);
     * }</pre>
     *
     * @param jarFile         the source JAR file to extract from; must not be {@code null}
     * @param targetDirectory the directory where the contents should be extracted; must not be {@code null}
     * @param jarEntryFilter  an optional filter to determine which entries to extract; if {@code null}, all entries are extracted
     * @throws IOException if an I/O error occurs during extraction or if the JAR file is invalid
     */
    public static void extract(JarFile jarFile, File targetDirectory, JarEntryFilter jarEntryFilter) throws IOException {
        List<JarEntry> jarEntriesList = filter(jarFile, jarEntryFilter);
        doExtract(jarFile, jarEntriesList, targetDirectory);
    }

    /**
     * Extracts entries from a JAR resource pointed by the given URL to the specified target directory,
     * optionally filtering which entries to extract.
     *
     * <p>
     * This method resolves the JAR file and relative path from the provided URL, filters the entries
     * using the given {@link JarEntryFilter}, and extracts them to the target directory while preserving
     * the original directory structure. If no filter is provided (i.e., {@code null}), all entries under
     * the resolved path will be extracted.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * URL jarURL = new URL("jar:file:/path/to/file.jar!/com/example/");
     * File outputDir = new File("/path/to/output");
     *
     * // Extract all entries under 'com/example/'
     * JarUtils.extract(jarURL, outputDir, null);
     *
     * // Extract only .class files under 'com/example/'
     * JarEntryFilter classFileFilter = entry -> entry.getName().endsWith(".class");
     * JarUtils.extract(jarURL, outputDir, classFileFilter);
     * }</pre>
     *
     * @param jarResourceURL  the URL pointing to a resource within a JAR file; must not be {@code null}
     * @param targetDirectory the directory where the contents should be extracted; must not be {@code null}
     * @param jarEntryFilter  an optional filter to determine which entries to extract; if {@code null}, all entries are extracted
     * @throws IOException if an I/O error occurs during extraction or resolving the JAR resource
     */
    public static void extract(URL jarResourceURL, File targetDirectory, JarEntryFilter jarEntryFilter) throws IOException {
        final JarFile jarFile = toJarFile(jarResourceURL);
        final String relativePath = resolveRelativePath(jarResourceURL);
        final JarEntry jarEntry = jarFile.getJarEntry(relativePath);
        final boolean isDirectory = jarEntry.isDirectory();
        List<JarEntry> jarEntriesList = filter(jarFile, new JarEntryFilter() {
            @Override
            public boolean accept(JarEntry filteredObject) {
                String name = filteredObject.getName();
                if (isDirectory && name.equals(relativePath)) {
                    return true;
                } else return name.startsWith(relativePath);
            }
        });

        jarEntriesList = doFilter(jarEntriesList, jarEntryFilter);

        doExtract(jarFile, jarEntriesList, targetDirectory);
    }

    protected static void doExtract(JarFile jarFile, Iterable<JarEntry> jarEntries, File targetDirectory) throws IOException {
        if (jarEntries != null) {
            for (JarEntry jarEntry : jarEntries) {
                String jarEntryName = jarEntry.getName();
                File targetFile = new File(targetDirectory, jarEntryName);
                if (jarEntry.isDirectory()) {
                    targetFile.mkdirs();
                } else {
                    InputStream inputStream = null;
                    OutputStream outputStream = null;
                    try {
                        inputStream = jarFile.getInputStream(jarEntry);
                        if (inputStream != null) {
                            File parentFile = targetFile.getParentFile();
                            if (!parentFile.exists()) {
                                parentFile.mkdirs();
                            }
                            outputStream = new FileOutputStream(targetFile);
                            copy(inputStream, outputStream);
                        }
                    } finally {
                        close(outputStream);
                        close(inputStream);
                    }
                }
            }
        }
    }

    private JarUtils() {
    }
    
}
