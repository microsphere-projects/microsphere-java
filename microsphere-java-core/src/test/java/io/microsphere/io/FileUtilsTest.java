package io.microsphere.io;

import io.microsphere.AbstractTestCase;
import io.microsphere.io.event.FileChangedEvent;
import io.microsphere.io.event.FileChangedListener;
import io.microsphere.process.ProcessExecutor;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.microsphere.io.FileUtils.cleanDirectory;
import static io.microsphere.io.FileUtils.deleteDirectory;
import static io.microsphere.io.FileUtils.forceDelete;
import static io.microsphere.io.FileUtils.forceDeleteOnExit;
import static io.microsphere.io.FileUtils.getCanonicalFile;
import static io.microsphere.io.FileUtils.getFileExtension;
import static io.microsphere.io.FileUtils.isSymlink;
import static io.microsphere.io.FileUtils.resolveRelativePath;
import static io.microsphere.io.event.FileChangedEvent.Kind.DELETED;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static io.microsphere.util.ClassLoaderUtils.getResource;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static io.microsphere.util.SystemUtils.IS_OS_WINDOWS;
import static io.microsphere.util.SystemUtils.JAVA_IO_TMPDIR;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        assertThrows(IllegalStateException.class, () -> new FileUtils() {
        });
    }

    @Test
    public void testResolveRelativePath() {
        assertEquals("io/FileUtilsTest.class", resolveRelativePath(packageDirectory, classFile));
    }

    @Test
    public void testResolveRelativePathOnSameDirectory() {
        assertEquals(EMPTY_STRING, resolveRelativePath(packageDirectory, packageDirectory));
    }

    @Test
    public void testResolveRelativePathOnFile() {
        assertNull(resolveRelativePath(classFile, packageDirectory));
    }

    @Test
    public void testResolveRelativePathOnNotRelativePath() {
        assertNull(resolveRelativePath(packageDirectory, new File(JAVA_IO_TMPDIR)));
    }

    @Test
    public void testGetFileExtension() {
        assertNull(getFileExtension(null));
        assertEquals("class", getFileExtension(classFile.getName()));
    }

    @Test
    public void testDeleteDirectoryOnEmptyDirectory() throws IOException {
        File testDir = createRandomTempDirectory();
        // exists
        assertEquals(1, deleteDirectory(testDir));
        assertFalse(testDir.exists());
    }

    @Test
    public void testDeleteDirectory() throws IOException {
        File testDir = createRandomTempDirectory();
        int times = random.nextInt(3, 10);
        for (int i = 0; i < times; i++) {
            createRandomFile(testDir);
        }
        assertEquals(times + 1, deleteDirectory(testDir));
        assertFalse(testDir.exists());
    }

    @Test
    public void testDeleteDirectoryOnNotExists() throws IOException {
        File testDir = newRandomTempFile();
        // not exists
        assertEquals(0, deleteDirectory(testDir));
        assertFalse(testDir.exists());
    }

    @Test
    public void testDeleteDirectoryOnIOException() throws Exception {
        File testDir = createRandomTempDirectory();
        createRandomFile(testDir);

        AtomicBoolean running = new AtomicBoolean(true);

        ExecutorService executor = newSingleThreadExecutor();

        StandardFileWatchService fileWatchService = new StandardFileWatchService();

        IOException exception = null;

        try {
            fileWatchService.watch(testDir, new FileChangedListener() {
                @Override
                public void onFileDeleted(FileChangedEvent event) {
                    File deletedFile = event.getFile();
                    assertFalse(deletedFile.exists());
                    assertEquals(testDir, deletedFile.getParentFile());
                    try {
                        createRandomFile(testDir);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }, DELETED);

            fileWatchService.start();

            executor.submit(() -> {
                while (running.get()) {
                    createRandomFile(testDir);
                }
                return null;
            });


            while (true) {
                try {
                    deleteDirectory(testDir);
                    Thread.sleep(500);
                } catch (IOException e) {
                    exception = e;
                    running.set(false);
                    break;
                }
            }

        } finally {
            fileWatchService.stop();
            executor.shutdown();
        }

        assertNotNull(exception);

    }

    @Test
    public void testCleanDirectory() throws IOException {
        File testDir = createRandomTempDirectory();
        int times = random.nextInt(3, 10);
        for (int i = 0; i < times; i++) {
            createRandomFile(testDir);
        }
        assertEquals(times, cleanDirectory(testDir));
        assertTrue(testDir.exists());
    }

    @Test
    public void testCleanDirectoryOnNotExists() throws IOException {
        File testDir = newRandomTempFile();
        assertFalse(testDir.exists());
        assertEquals(0, cleanDirectory(testDir));
    }

    @Test
    public void testCleanDirectoryOnFile() throws IOException {
        File tempFile = createRandomTempFile();
        assertEquals(0, cleanDirectory(tempFile));
    }

    @Test
    public void testForceDeleteOnEmptyDirectory() throws IOException {
        File tempDir = createRandomTempDirectory();
        assertEquals(1, forceDelete(tempDir));
        assertFalse(tempDir.exists());
    }

    @Test
    public void testForceDeleteOnSingleFile() throws IOException {
        File tempFile = createRandomTempFile();
        assertEquals(1, forceDelete(tempFile));
        assertFalse(tempFile.exists());
    }

    @Test
    public void testForceDelete() throws IOException {
        File testDir = createRandomTempDirectory();
        int times = random.nextInt(3, 10);
        for (int i = 0; i < times; i++) {
            createRandomFile(testDir);
        }
        assertEquals(times + 1, forceDelete(testDir));
        assertFalse(testDir.exists());
    }

    @Test
    public void testForceDeleteOnFileNotExists() {
        File tempFile = newRandomTempFile();
        assertThrows(IOException.class, () -> forceDelete(tempFile));
    }

    @Test
    public void testForceDeleteOnIOException() throws Exception {
        File testDir = createRandomTempDirectory();
        File testFile = createRandomFile(testDir);

        ExecutorService executor = newSingleThreadExecutor();

        executor.submit(() -> {
            synchronized (testFile) {
                FileOutputStream outputStream = new FileOutputStream(testFile);
                for (int i = 0; i < 10000; i++) {
                    outputStream.write(i);
                    // wait for notification
                    testFile.wait(10);
                }
                outputStream.close();
            }
            return null;
        });

        assertThrows(IOException.class, () -> forceDelete(testFile));

        synchronized (testFile) {
            testFile.notify();
        }

        executor.shutdown();

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

    @Test
    public void testGetCanonicalFile() throws IOException {
        File tempFile = newRandomTempFile();
        assertEquals(tempFile, getCanonicalFile(tempFile));

        tempFile = createRandomTempFile();
        assertEquals(tempFile, getCanonicalFile(tempFile));
    }

}