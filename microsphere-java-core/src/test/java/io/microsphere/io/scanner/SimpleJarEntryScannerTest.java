/**
 *
 */
package io.microsphere.io.scanner;

import io.microsphere.AbstractTestCase;
import io.microsphere.util.jar.JarUtils;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.microsphere.io.scanner.SimpleJarEntryScanner.INSTANCE;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static io.microsphere.util.ClassLoaderUtils.getResource;
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

    private static final SimpleJarEntryScanner simpleJarEntryScanner = INSTANCE;

    @Test
    public void testScanInJarURL() throws IOException {
        URL resourceURL = getClassResource(classLoader, Nonnull.class);
        Set<JarEntry> jarEntrySet = simpleJarEntryScanner.scan(resourceURL, true);
        assertEquals(1, jarEntrySet.size());

        resourceURL = getResource(classLoader, "javax.annotation.concurrent");
        jarEntrySet = simpleJarEntryScanner.scan(resourceURL, false);
        assertEquals(5, jarEntrySet.size());
    }


    @Test
    public void testScanInJarFile() throws IOException {
        URL resourceURL = getClassResource(classLoader, Nonnull.class);
        JarFile jarFile = JarUtils.toJarFile(resourceURL);
        Set<JarEntry> jarEntrySet = simpleJarEntryScanner.scan(jarFile, true);
        assertTrue(jarEntrySet.size() > 1);

        jarEntrySet = simpleJarEntryScanner.scan(jarFile, true, jarEntry -> jarEntry.getName().equals("javax/annotation/Nonnull.class"));
        assertEquals(1, jarEntrySet.size());
    }
}
