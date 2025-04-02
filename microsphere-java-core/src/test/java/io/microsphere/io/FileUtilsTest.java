package io.microsphere.io;

import io.microsphere.AbstractTestCase;
import io.microsphere.process.ProcessExecutor;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static io.microsphere.io.FileUtils.cleanDirectory;
import static io.microsphere.io.FileUtils.deleteDirectory;
import static io.microsphere.io.FileUtils.forceDelete;
import static io.microsphere.io.FileUtils.forceDeleteOnExit;
import static io.microsphere.io.FileUtils.getFileExtension;
import static io.microsphere.io.FileUtils.isSymlink;
import static io.microsphere.io.FileUtils.resolveRelativePath;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static io.microsphere.util.ClassLoaderUtils.getResource;
import static io.microsphere.util.SystemUtils.IS_OS_WINDOWS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FileUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see FileUtils
 * @since 1.0.0
 */
public class FileUtilsTest extends AbstractTestCase {

    private final URL classFileResource = getClassResource(TEST_CLASS_LOADER, FileUtilsTest.class);

    private final URL packageResource = getResource(TEST_CLASS_LOADER, "io.microsphere");

    private final File classFile = new File(classFileResource.getFile());

    private final File packageDirectory = new File(packageResource.getFile());

    @Test
    public void testConstructor() {
        assertThrows(IllegalStateException.class, () -> new FileUtils(){});
    }

    @Test
    public void testResolveRelativePath() throws Exception {
        assertEquals("/io/FileUtilsTest.class", resolveRelativePath(packageDirectory, classFile));
    }

    @Test
    public void testResolveRelativePathOnNotRelativePath() throws Exception {
        assertNull(resolveRelativePath(classFile, packageDirectory));
    }

    @Test
    public void testGetFileExtension() {
        assertNull(getFileExtension(null));
        assertEquals("class", getFileExtension(classFile.getName()));
    }

    @Test
    public void testDeleteDirectory() throws IOException {
        File testDir = createRandomTempDirectory();
        // exists
        deleteDirectory(testDir);
    }

    @Test
    public void testDeleteDirectoryOnNotExists() throws IOException {
        File testDir = newRandomTempFile();
        // not exists
        deleteDirectory(testDir);
    }

    @Test
    public void testDeleteDirectoryOnIOException() throws IOException {
        File testDir = createRandomTempDirectory();
        testDir.setReadOnly();
        // not exists
        deleteDirectory(testDir);
    }


    @Test
    public void testCleanDirectory() throws IOException {
        File testDir = createRandomTempDirectory();
        File testFile = createRandomFile(testDir);
        cleanDirectory(testDir);
        assertFalse(testFile.exists());
    }

    @Test
    public void testCleanDirectoryOnNotExists() throws IOException {
        File testDir = newRandomTempFile();
        assertFalse(testDir.exists());
        assertThrows(IOException.class, () -> cleanDirectory(testDir));
    }

    @Test
    public void testCleanDirectoryOnFile() throws IOException {
        File tempFile = createRandomTempFile();
        assertThrows(IOException.class, () -> cleanDirectory(tempFile));
    }

    @Test
    public void testForceDeleteOnDirectory() throws IOException {
        File tempDir = createRandomTempDirectory();
        forceDelete(tempDir);
    }

    @Test
    public void testForceDeleteOnFile() throws IOException {
        File tempFile = createRandomTempFile();
        forceDelete(tempFile);
    }

    @Test
    public void testForceDeleteOnFileNotExists() {
        File tempFile = newRandomTempFile();
        assertThrows(IOException.class, () -> forceDelete(tempFile));
    }

    @Test
    public void testForceDeleteOnExit() throws IOException {
        File tempFile = createRandomTempFile();
        forceDeleteOnExit(tempFile);
    }

    @Test
    public void testDeleteDirectoryOnExit() throws IOException {
        File tempDir = createRandomTempDirectory();
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                createRandomDirectory(tempDir);
            } else {
                createRandomFile(tempDir);
            }
        }
        forceDeleteOnExit(tempDir);
    }

    @Test
    public void testIsSymlink() throws IOException {
        assertThrows(NullPointerException.class, () -> isSymlink(null));
        if (IS_OS_WINDOWS) {
            assertFalse(isSymlink(new File("")));
        } else {
            File tempDir = createRandomTempDirectory();
            File targetFile = createRandomFile(tempDir);
            File linkFile = new File(tempDir, "link");
            ProcessExecutor processExecutor = new ProcessExecutor("ln", "-s", targetFile.getAbsolutePath(), linkFile.getAbsolutePath());
            processExecutor.execute(System.out);
            assertTrue(isSymlink(linkFile));
        }
    }

}