/**
 *
 */
package io.github.microsphere.filter;

import java.util.jar.JarEntry;

import static io.github.microsphere.constants.FileConstants.CLASS_EXTENSION;

/**
 * Class File {@link JarEntryFilter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ClassFileJarEntryFilter
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
