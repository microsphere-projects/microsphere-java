package io.microsphere.io.filter;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static io.microsphere.io.filter.DirectoryFileFilter.INSTANCE;
import static io.microsphere.util.SystemUtils.JAVA_HOME;
import static io.microsphere.util.SystemUtils.JAVA_IO_TMPDIR;
import static io.microsphere.util.SystemUtils.USER_DIR;
import static java.io.File.createTempFile;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DirectoryFileFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DirectoryFileFilter
 * @since 1.0.0
 */
public class DirectoryFileFilterTest {

    @Test
    public void testAcceptOnNull() {
        assertFalse(INSTANCE.accept(null));
    }

    @Test
    public void testAcceptOnDirectory() {
        assertTrue(INSTANCE.accept(new File(JAVA_HOME)));
        assertTrue(INSTANCE.accept(new File(USER_DIR)));
        assertTrue(INSTANCE.accept(new File(JAVA_IO_TMPDIR)));
    }

    @Test
    public void testAcceptOnFile() throws IOException {
        File testFile = createTempFile("test", "txt");
        assertFalse(INSTANCE.accept(testFile));
        testFile.deleteOnExit();
    }

}