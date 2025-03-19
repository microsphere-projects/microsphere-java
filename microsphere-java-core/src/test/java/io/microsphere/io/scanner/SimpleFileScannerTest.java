/**
 *
 */
package io.microsphere.io.scanner;

import io.microsphere.AbstractTestCase;
import io.microsphere.io.filter.DirectoryFileFilter;
import io.microsphere.io.filter.NameFileFilter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static io.microsphere.io.scanner.SimpleFileScanner.INSTANCE;
import static io.microsphere.util.SystemUtils.JAVA_HOME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@link SimpleFileScanner} {@link Test}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see SimpleFileScanner
 * @since 1.0.0
 */
public class SimpleFileScannerTest extends AbstractTestCase {

    private static final SimpleFileScanner simpleFileScanner = INSTANCE;

    private static final File JAVA_HOME_DIR = new File(JAVA_HOME);

    @Test
    public void testScan() {
        Set<File> directories = simpleFileScanner.scan(JAVA_HOME_DIR, true);
        assertFalse(directories.isEmpty());

        directories = simpleFileScanner.scan(JAVA_HOME_DIR, false);
        assertFalse(directories.isEmpty());
    }

    @Test
    public void testScanOnDirectory() {
        Set<File> directories = simpleFileScanner.scan(JAVA_HOME_DIR, true, DirectoryFileFilter.INSTANCE);
        assertFalse(directories.isEmpty());
    }

    @Test
    public void testScanOnBinDirectory() {
        Set<File> directories = simpleFileScanner.scan(JAVA_HOME_DIR, true, new NameFileFilter("bin"));
        assertEquals(1, directories.size());
    }
}
