/**
 *
 */
package io.microsphere.io.scanner;

import io.microsphere.AbstractTestCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

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
        File jarHome = new File(SystemUtils.JAVA_HOME);
        Set<File> directories = simpleFileScanner.scan(jarHome, true, DirectoryFileFilter.INSTANCE);
        assertFalse(directories.isEmpty());

        directories = simpleFileScanner.scan(jarHome, false, new NameFileFilter("bin"));
        assertEquals(1, directories.size());
    }
}
