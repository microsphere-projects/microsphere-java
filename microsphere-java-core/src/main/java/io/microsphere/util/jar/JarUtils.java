/**
 *
 */
package io.microsphere.util.jar;

import io.microsphere.constants.ProtocolConstants;
import io.microsphere.filter.JarEntryFilter;
import io.microsphere.util.BaseUtils;

import javax.annotation.Nonnull;
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
public abstract class JarUtils extends BaseUtils {

    /**
     * Create a {@link JarFile} from specified {@link URL} of {@link JarFile}
     *
     * @param jarURL {@link URL} of {@link JarFile} or {@link JarEntry}
     * @return JarFile
     * @throws IOException If {@link JarFile jar file} is invalid, see {@link JarFile#JarFile(String)}
     */
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
            String message = format("jarURL Protocol['{}'] is unsupported ,except '{}' and '{}' ", protocol, JAR_PROTOCOL, FILE_PROTOCOL);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Resolve Relative path from Jar URL
     *
     * @param jarURL {@link URL} of {@link JarFile} or {@link JarEntry}
     * @return Non-null
     * @throws NullPointerException     see {@link #assertJarURLProtocol(URL)}
     * @throws IllegalArgumentException see {@link #assertJarURLProtocol(URL)}
     */
    @Nonnull
    public static String resolveRelativePath(URL jarURL) throws NullPointerException, IllegalArgumentException {
        assertJarURLProtocol(jarURL);
        String form = jarURL.toExternalForm();
        String relativePath = substringAfter(form, ARCHIVE_ENTRY_SEPARATOR);
        relativePath = normalizePath(relativePath);
        return decode(relativePath);
    }

    /**
     * Resolve absolute path from the {@link URL} of {@link JarEntry}
     *
     * @param jarURL {@link URL} of {@link JarFile} or {@link JarEntry}
     * @return If {@link URL#getProtocol()} equals <code>jar</code> or <code>file</code> , resolves absolute path, or
     * return <code>null</code>
     * @throws NullPointerException     see {@link #assertJarURLProtocol(URL)}
     * @throws IllegalArgumentException see {@link #assertJarURLProtocol(URL)}
     */
    @Nonnull
    public static String resolveJarAbsolutePath(URL jarURL) throws NullPointerException, IllegalArgumentException {
        assertJarURLProtocol(jarURL);
        File archiveFile = resolveArchiveFile(jarURL);
        return archiveFile == null ? null : archiveFile.getAbsolutePath();
    }

    /**
     * Filter {@link JarEntry} list from {@link JarFile}
     *
     * @param jarFile        {@link JarFile}
     * @param jarEntryFilter {@link JarEntryFilter}
     * @return Read-only List
     */
    @Nonnull
    public static List<JarEntry> filter(JarFile jarFile, JarEntryFilter jarEntryFilter) {
        if (jarFile == null) {
            return emptyList();
        }
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        List<JarEntry> jarEntriesList = ofList(jarEntries);
        return doFilter(jarEntriesList, jarEntryFilter);
    }

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
     * Find {@link JarEntry} from specified <code>url</code>
     *
     * @param jarURL jar resource url
     * @return If found , return {@link JarEntry}
     */
    public static JarEntry findJarEntry(URL jarURL) throws IOException {
        JarFile jarFile = toJarFile(jarURL);
        final String relativePath = resolveRelativePath(jarURL);
        JarEntry jarEntry = jarFile.getJarEntry(relativePath);
        return jarEntry;
    }


    /**
     * Extract the source {@link JarFile} to target directory
     *
     * @param jarSourceFile   the source {@link JarFile}
     * @param targetDirectory target directory
     * @throws IOException When the source jar file is an invalid {@link JarFile}
     */
    public static void extract(File jarSourceFile, File targetDirectory) throws IOException {
        extract(jarSourceFile, targetDirectory, null);
    }

    /**
     * Extract the source {@link JarFile} to target directory with specified {@link JarEntryFilter}
     *
     * @param jarSourceFile   the source {@link JarFile}
     * @param targetDirectory target directory
     * @param jarEntryFilter  {@link JarEntryFilter}
     * @throws IOException When the source jar file is an invalid {@link JarFile}
     */
    public static void extract(File jarSourceFile, File targetDirectory, JarEntryFilter jarEntryFilter) throws IOException {

        final JarFile jarFile = new JarFile(jarSourceFile);

        extract(jarFile, targetDirectory, jarEntryFilter);
    }

    /**
     * Extract the source {@link JarFile} to target directory with specified {@link JarEntryFilter}
     *
     * @param jarFile         the source {@link JarFile}
     * @param targetDirectory target directory
     * @param jarEntryFilter  {@link JarEntryFilter}
     * @throws IOException When the source jar file is an invalid {@link JarFile}
     */
    public static void extract(JarFile jarFile, File targetDirectory, JarEntryFilter jarEntryFilter) throws IOException {
        List<JarEntry> jarEntriesList = filter(jarFile, jarEntryFilter);
        doExtract(jarFile, jarEntriesList, targetDirectory);
    }

    /**
     * Extract the source {@link JarFile} to target directory with specified {@link JarEntryFilter}
     *
     * @param jarResourceURL  The resource URL of {@link JarFile} or {@link JarEntry}
     * @param targetDirectory target directory
     * @param jarEntryFilter  {@link JarEntryFilter}
     * @throws IOException When the source jar file is an invalid {@link JarFile}
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


}
