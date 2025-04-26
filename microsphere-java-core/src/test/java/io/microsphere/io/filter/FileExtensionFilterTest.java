package io.microsphere.io.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static io.microsphere.io.filter.FileExtensionFilter.of;
import static io.microsphere.util.SystemUtils.JAVA_HOME;
import static io.microsphere.util.SystemUtils.JAVA_IO_TMPDIR;
import static io.microsphere.util.SystemUtils.USER_DIR;
import static java.io.File.createTempFile;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FileExtensionFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see FileExtensionFilter
 * @since 1.0.0
 */
class FileExtensionFilterTest {

    private FileExtensionFilter instance;

    @BeforeEach
    public void init() {
        instance = of("txt");
    }

    @Test
    public void testAcceptOnNull() {
        assertFalse(instance.accept(null));
    }

    @Test
    public void testAcceptOnDirectory() {
        assertFalse(instance.accept(new File(JAVA_HOME)));
        assertFalse(instance.accept(new File(USER_DIR)));
        assertFalse(instance.accept(new File(JAVA_IO_TMPDIR)));
    }

    @Test
    public void testAcceptOnFile() throws IOException {
        File testFile = createTempFile("test", ".txt");
        assertTrue(instance.accept(testFile));
        testFile.deleteOnExit();
    }
}