/**
 *
 */
package io.microsphere.io.scanner;

import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.Set;

import static io.microsphere.AbstractTestCase.TEST_CLASS_LOADER;
import static io.microsphere.io.scanner.SimpleClassScanner.INSTANCE;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SimpleClassScannerTest}
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @see SimpleClassScannerTest
 * @since 1.0.0
 */
public class SimpleClassScannerTest {

    private static final SimpleClassScanner simpleClassScanner = INSTANCE;

    @Test
    public void testScanPackageInDirectory() {
        Set<Class<?>> classesSet = simpleClassScanner.scan(TEST_CLASS_LOADER, "io.microsphere.io.scanner");
        assertFalse(classesSet.isEmpty());
    }

    @Test
    public void testScanPackageInJar() {
        Set<Class<?>> classesSet = simpleClassScanner.scan(TEST_CLASS_LOADER, "javax.annotation.concurrent", false, true);
        assertEquals(4, classesSet.size());

        classesSet = simpleClassScanner.scan(TEST_CLASS_LOADER, "i", false, true);
        assertTrue(classesSet.isEmpty());
    }

    @Test
    public void testScanInArchive() {
        URL nonnullClassResource = getClassResource(TEST_CLASS_LOADER, Nonnull.class);
        Set<Class<?>> classesSet = simpleClassScanner.scan(TEST_CLASS_LOADER, nonnullClassResource, false);
        assertFalse(classesSet.isEmpty());
    }

}
