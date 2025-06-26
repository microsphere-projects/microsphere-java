/**
 *
 */
package io.microsphere.filter;

import java.util.jar.JarEntry;

/**
 * A filter for {@link JarEntry} objects. Implementations of this interface can be used to
 * selectively process or ignore entries within a JAR file.
 *
 * <h3>Example Usage</h3>
 * <p>A simple implementation might accept only entries whose names end with a ".class"
 * extension:
 *
 * <pre>{@code
 * JarEntryFilter classFileFilter = entry -> entry.getName().endsWith(".class");
 * }</pre>
 *
 * <p>This interface is designed to be used in conjunction with the methods of the
 * {@link java.util.jar.JarFile} class and other utilities that process JAR entries.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see JarEntry
 * @see Filter
 * @since 1.0.0
 */
public interface JarEntryFilter extends Filter<JarEntry> {
}
