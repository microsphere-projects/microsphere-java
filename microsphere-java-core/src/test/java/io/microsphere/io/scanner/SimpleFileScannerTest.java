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

import static io.microsphere.util.SystemUtils.JAVA_HOME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@link SimpleFileScanner} {@link Test}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see SimpleFileScanner
 * @since 1.0.0
 */
public class SimpleFileScannerTest extends AbstractTestCase {

    private SimpleFileScanner simpleFileScanner = SimpleFileScanner.INSTANCE;

    @Test
    public void testScan() {
        File jarHome = new File(JAVA_HOME);

        Set<File> directories = simpleFileScanner.scan(jarHome, true);
        assertFalse(directories.isEmpty());

        directories = simpleFileScanner.scan(jarHome, true, DirectoryFileFilter.INSTANCE);
        assertFalse(directories.isEmpty());

        directories = simpleFileScanner.scan(jarHome, false, new NameFileFilter("bin"));
        assertEquals(1, directories.size());
    }
}
