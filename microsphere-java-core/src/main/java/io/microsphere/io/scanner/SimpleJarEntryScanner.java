/**
 *
 */
package io.microsphere.io.scanner;

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.constants.PathConstants;
import io.microsphere.filter.JarEntryFilter;
import io.microsphere.util.jar.JarUtils;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.microsphere.util.StringUtils.EMPTY;
import static io.microsphere.util.jar.JarUtils.filter;
import static io.microsphere.util.jar.JarUtils.resolveRelativePath;
import static io.microsphere.util.jar.JarUtils.toJarFile;
import static java.util.Collections.unmodifiableSet;

/**
 * A simple scanner for {@link JarEntry} that provides methods to scan and filter entries within a JAR file.
 * <p>
 * This class allows recursive or non-recursive scanning of JAR entries, optionally applying a filter to match specific
 * entries. It returns an immutable set of matching jar entries.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Example 1: Scan all entries recursively from a JAR URL
 * SimpleJarEntryScanner scanner = new SimpleJarEntryScanner();
 * URL jarUrl = new URL("jar:file:/path/to/your.jar!/");
 * Set<JarEntry> entries = scanner.scan(jarUrl, true);
 * }</pre>
 *
 * <pre>{@code
 * // Example 2: Scan with a filter to find only .class files
 * JarEntryFilter filter = entry -> entry.getName().endsWith(".class");
 * Set<JarEntry> classEntries = scanner.scan(jarUrl, true, filter);
 * }</pre>
 *
 * <pre>{@code
 * // Example 3: Scan a specific directory inside a JarFile non-recursively
 * JarFile jarFile = new JarFile("/path/to/your.jar");
 * Set<JarEntry> entries = scanner.scan(jarFile, false);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see JarEntry
 * @see JarFile
 * @see JarEntryFilter
 * @since 1.0.0
 */
public class SimpleJarEntryScanner {

    /**
     * Singleton
     */
    public static final SimpleJarEntryScanner INSTANCE = new SimpleJarEntryScanner();

    public SimpleJarEntryScanner() {
    }

    /**
     * @param jarURL    {@link URL} of {@link JarFile} or {@link JarEntry}
     * @param recursive recursive
     * @return Read-only {@link Set}
     * @throws NullPointerException     If argument <code>null</code>
     * @throws IllegalArgumentException <ul> <li>{@link JarUtils#resolveRelativePath(URL)}
     * @throws IOException              <ul> <li>{@link JarUtils#toJarFile(URL)}
     */
    @Nonnull
    @Immutable
    public Set<JarEntry> scan(URL jarURL, final boolean recursive) throws NullPointerException, IllegalArgumentException, IOException {
        return scan(jarURL, recursive, null);
    }

    /**
     * @param jarURL         {@link URL} of {@link JarFile} or {@link JarEntry}
     * @param recursive      recursive
     * @param jarEntryFilter {@link JarEntryFilter}
     * @return Read-only {@link Set}
     * @throws NullPointerException     If argument <code>null</code>
     * @throws IllegalArgumentException {@link JarUtils#resolveJarAbsolutePath(URL)}
     * @throws IOException              {@link JarUtils#toJarFile(URL)}
     * @see JarEntryFilter
     */
    @Nonnull
    @Immutable
    public Set<JarEntry> scan(URL jarURL, final boolean recursive, JarEntryFilter jarEntryFilter) throws NullPointerException, IllegalArgumentException, IOException {
        String relativePath = resolveRelativePath(jarURL);
        JarFile jarFile = toJarFile(jarURL);
        return scan(jarFile, relativePath, recursive, jarEntryFilter);
    }


    /**
     * @param jarFile
     * @param recursive
     * @return
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public Set<JarEntry> scan(JarFile jarFile, final boolean recursive) throws NullPointerException, IllegalArgumentException, IOException {
        return scan(jarFile, recursive, null);
    }

    /**
     * @param jarFile
     * @param recursive
     * @param jarEntryFilter
     * @return
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Nonnull
    @Immutable
    public Set<JarEntry> scan(JarFile jarFile, final boolean recursive, JarEntryFilter jarEntryFilter) throws NullPointerException, IllegalArgumentException, IOException {
        return scan(jarFile, EMPTY, recursive, jarEntryFilter);
    }

    @Nonnull
    @Immutable
    protected Set<JarEntry> scan(JarFile jarFile, String relativePath, final boolean recursive, JarEntryFilter jarEntryFilter) throws NullPointerException, IllegalArgumentException, IOException {
        Set<JarEntry> jarEntriesSet = new LinkedHashSet<>();
        List<JarEntry> jarEntriesList = filter(jarFile, jarEntryFilter);

        for (JarEntry jarEntry : jarEntriesList) {
            String jarEntryName = jarEntry.getName();

            boolean accept = false;
            if (recursive) {
                accept = jarEntryName.startsWith(relativePath);
            } else {
                if (jarEntry.isDirectory()) {
                    accept = jarEntryName.equals(relativePath);
                } else {
                    int beginIndex = jarEntryName.indexOf(relativePath);
                    if (beginIndex == 0) {
                        accept = jarEntryName.indexOf(PathConstants.SLASH, relativePath.length()) < 0;
                    }
                }
            }
            if (accept) {
                jarEntriesSet.add(jarEntry);
            }
        }
        return unmodifiableSet(jarEntriesSet);
    }
}
