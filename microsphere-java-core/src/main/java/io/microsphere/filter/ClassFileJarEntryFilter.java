/**
 *
 */
package io.microsphere.filter;

import java.util.jar.JarEntry;

import static io.microsphere.constants.FileConstants.CLASS_EXTENSION;

/**
 * A {@link JarEntryFilter} implementation that filters only Java class files in a JAR.
 *
 * <p>This filter checks whether a given {@link JarEntry} represents a Java class file by:
 * <ul>
 *     <li>Ensuring it is not a directory</li>
 *     <li>Checking if its name ends with the ".class" extension</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * JarFile jarFile = new JarFile("example.jar");
 * Enumeration<JarEntry> entries = jarFile.stream()
 *     .filter(ClassFileJarEntryFilter.INSTANCE::accept)
 *     .iterator();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JarEntryFilter
 * @see JarEntry
 * @since 1.0.0
 */
public class ClassFileJarEntryFilter implements JarEntryFilter {

    /**
     * {@link ClassFileJarEntryFilter} Singleton instance
     */
    public static final ClassFileJarEntryFilter INSTANCE = new ClassFileJarEntryFilter();

    protected ClassFileJarEntryFilter() {

    }

    @Override
    public boolean accept(JarEntry jarEntry) {
        return !jarEntry.isDirectory() && jarEntry.getName().endsWith(CLASS_EXTENSION);
    }
}
