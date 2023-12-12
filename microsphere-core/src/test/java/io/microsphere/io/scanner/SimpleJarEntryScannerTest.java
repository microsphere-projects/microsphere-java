/**
 *
 */
package io.microsphere.io.scanner;

import io.microsphere.AbstractTestCase;
import io.microsphere.filter.JarEntryFilter;
import io.microsphere.util.ClassLoaderUtils;
import io.microsphere.util.jar.JarUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SimpleJarEntryScanner} {@link Test}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see SimpleJarEntryScannerTest
 * @since 1.0.0
 */
public class SimpleJarEntryScannerTest extends AbstractTestCase {

    private SimpleJarEntryScanner simpleJarEntryScanner = SimpleJarEntryScanner.INSTANCE;

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    @Test
    public void testScan() throws IOException {
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, String.class);
        Set<JarEntry> jarEntrySet = simpleJarEntryScanner.scan(resourceURL, true);
        assertEquals(1, jarEntrySet.size());

        JarFile jarFile = JarUtils.toJarFile(resourceURL);
        jarEntrySet = simpleJarEntryScanner.scan(jarFile, true);
        assertTrue(jarEntrySet.size() > 1000);

        jarEntrySet = simpleJarEntryScanner.scan(jarFile, true, new JarEntryFilter() {
            @Override
            public boolean accept(JarEntry jarEntry) {
                return jarEntry.getName().equals("java/lang/String.class");
            }
        });

        assertEquals(1, jarEntrySet.size());

    }
}
