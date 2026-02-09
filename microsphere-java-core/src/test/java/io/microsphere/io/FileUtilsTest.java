package io.microsphere.io;

import io.microsphere.AbstractTestCase;
import io.microsphere.lang.function.ThrowableConsumer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.microsphere.concurrent.CustomizedThreadFactory.newThreadFactory;
import static io.microsphere.concurrent.ExecutorUtils.shutdown;
import static io.microsphere.constants.FileConstants.CLASS;
import static io.microsphere.io.FileUtils.EMPTY_FILE_ARRAY;
import static io.microsphere.io.FileUtils.cleanDirectory;
import static io.microsphere.io.FileUtils.deleteDirectory;
import static io.microsphere.io.FileUtils.deleteDirectoryOnExit;
import static io.microsphere.io.FileUtils.forceDelete;
import static io.microsphere.io.FileUtils.forceDeleteOnExit;
import static io.microsphere.io.FileUtils.getCanonicalFile;
import static io.microsphere.io.FileUtils.getFileExtension;
import static io.microsphere.io.FileUtils.isSymlink;
import static io.microsphere.io.FileUtils.listFiles;
import static io.microsphere.io.FileUtils.resolveRelativePath;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static io.microsphere.util.ClassLoaderUtils.getResource;
import static io.microsphere.util.ExceptionUtils.wrap;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static io.microsphere.util.SystemUtils.JAVA_IO_TMPDIR;
import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
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
class FileUtilsTest extends AbstractTestCase {

    private final URL classFileResource = getClassResource(TEST_CLASS_LOADER, FileUtilsTest.class);

    private final URL packageResource = getResource(TEST_CLASS_LOADER, "io.microsphere");

    private final File classFile = new File(classFileResource.getFile());

    private final File packageDirectory = new File(packageResource.getFile());

    @Test
    void testResolveRelativePath() {
        assertEquals("io/FileUtilsTest.class", resolveRelativePath(packageDirectory, classFile));
    }

    @Test
    void testResolveRelativePathOnSameDirectory() {
        assertEquals(EMPTY_STRING, resolveRelativePath(packageDirectory, packageDirectory));
    }

    @Test
    void testResolveRelativePathOnFile() {
        assertNull(resolveRelativePath(classFile, packageDirectory));
    }

    @Test
    void testResolveRelativePathOnNotRelativePath() {
        assertNull(resolveRelativePath(packageDirectory, new File(JAVA_IO_TMPDIR)));
    }

    @Test
    void testGetFileExtension() {
        assertNull(getFileExtension(null));
        assertEquals(CLASS, getFileExtension(classFile.getName()));
    }

    @Test
    void testDeleteDirectoryOnEmptyDirectory() throws IOException {
        File testDir = createRandomTempDirectory();
        // exists
        assertEquals(1, deleteDirectory(testDir));
        assertFalse(testDir.exists());
    }

    @Test
    void testDeleteDirectory() throws IOException {
        File testDir = createRandomTempDirectory();
        int times = random.nextInt(3, 10);
        for (int i = 0; i < times; i++) {
            createRandomFile(testDir);
        }
        assertEquals(times + 1, deleteDirectory(testDir));
        assertFalse(testDir.exists());
    }

    @Test
    void testDeleteDirectoryOnNotExists() throws IOException {
        File testDir = newRandomTempFile();
        // not exists
        assertEquals(0, deleteDirectory(testDir));
        assertFalse(testDir.exists());
    }

    @Test
    void testDeleteDirectoryOnIOException() throws Exception {
        handleDirectoryOnIOException(FileUtils::deleteDirectory);
    }

    @Test
    void testDeleteDirectoryOnLink() throws IOException {
        File testDir = createRandomTempDirectory();
        File linkDir = makeLinkFile(testDir);
        assertEquals(1, deleteDirectory(linkDir));
    }

    @Test
    void testCleanDirectory() throws IOException {
        File testDir = createRandomTempDirectory();
        int times = random.nextInt(3, 10);
        for (int i = 0; i < times; i++) {
            createRandomFile(testDir);
        }
        assertEquals(times, cleanDirectory(testDir));
        assertTrue(testDir.exists());
    }

    @Test
    void testCleanDirectoryOnNotExists() throws IOException {
        File testDir = newRandomTempFile();
        assertFalse(testDir.exists());
        assertEquals(0, cleanDirectory(testDir));
    }

    @Test
    void testCleanDirectoryOnFile() throws IOException {
        File tempFile = createRandomTempFile();
        assertEquals(0, cleanDirectory(tempFile));
    }

    @Test
    void testForceDeleteOnEmptyDirectory() throws IOException {
        File tempDir = createRandomTempDirectory();
        assertEquals(1, forceDelete(tempDir));
        assertFalse(tempDir.exists());
    }

    @Test
    void testForceDeleteOnSingleFile() throws IOException {
        File tempFile = createRandomTempFile();
        assertEquals(1, forceDelete(tempFile));
        assertFalse(tempFile.exists());
    }

    @Test
    void testListFiles() throws IOException {
        File testDir = createRandomTempDirectory();
        assertEquals(EMPTY_FILE_ARRAY, listFiles(testDir));

        createRandomFile(testDir);
        assertTrue(isNotEmpty(listFiles(testDir)));

        deleteDirectoryOnExit(testDir);
    }

    @Test
    void testListFilesOnNull() {
        assertEquals(EMPTY_FILE_ARRAY, listFiles(null));
    }

    @Test
    void testListFilesOnNotFound() {
        assertEquals(EMPTY_FILE_ARRAY, listFiles(new File("not-found")));
    }

    @Test
    void testListFilesOnFile() throws IOException {
        assertEquals(EMPTY_FILE_ARRAY, listFiles(createRandomTempFile()));
    }

    @Test
    void testForceDelete() throws IOException {
        File testDir = createRandomTempDirectory();
        int times = random.nextInt(3, 10);
        for (int i = 0; i < times; i++) {
            createRandomFile(testDir);
        }
        assertEquals(times + 1, forceDelete(testDir));
        assertFalse(testDir.exists());
    }

    @Test
    void testForceDeleteOnFileNotExists() {
        File tempFile = newRandomTempFile();
        assertThrows(IOException.class, () -> forceDelete(tempFile));
    }

    @Test
    void testForceDeleteOnIOException() throws Exception {
        File testFile = createRandomTempFile();

        ExecutorService executor = newFixedThreadPool(3, newThreadFactory("testForceDelete-", true));

        // status : 0 -> init
        // status : 1 -> writing
        // status : 2 -> deleting
        int init = 0;
        int writing = 1;
        int deleting = 2;
        AtomicInteger status = new AtomicInteger(init);

        executor.submit(() -> {
            synchronized (testFile) {
                try (FileOutputStream outputStream = new FileOutputStream(testFile, true)) {
                    outputStream.write('a');
                    status.set(writing);
                    // wait for notification
                    testFile.wait();
                }
            }
            return null;
        });

        executor.submit(() -> {
            while (status.get() != writing) {
            }
            assertThrows(IOException.class, () -> forceDelete(testFile));
            status.set(deleting);
            return null;
        });

        executor.submit(() -> {
            while (status.get() != deleting) {
            }
            synchronized (testFile) {
                testFile.notify();
            }
            return null;
        });

        executor.awaitTermination(100, MILLISECONDS);

        executor.shutdown();
    }

    @Test
    void testForceDeleteOnExit() throws IOException {
        File tempFile = createRandomTempFile();
        forceDeleteOnExit(tempFile);
    }

    @Test
    void testDeleteDirectoryOnExit() throws IOException {
        File tempDir = createRandomTempDirectory();

        File link = makeLinkFile(tempDir);
        deleteDirectoryOnExit(link);

        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                createRandomDirectory(tempDir);
            } else {
                createRandomFile(tempDir);
            }
        }
        deleteDirectoryOnExit(tempDir);
    }

    @Test
    void testDeleteDirectoryOnExitOnNotExists() throws IOException {
        deleteDirectoryOnExit(new File("not-exists"));
    }

    @Test
    void testIsSymlink() throws IOException {
        File tempDir = createRandomTempDirectory();
        File targetFile = createRandomFile(tempDir);
        File linkFile = makeLinkFile(targetFile);
        assertTrue(isSymlink(linkFile));
    }

    @Test
    void testIsSymlinkOnNull() {
        assertThrows(NullPointerException.class, () -> isSymlink(null));
    }

    @Test
    void testGetCanonicalFile() {
        File tempFile = newRandomTempFile();
        assertEquals(getCanonicalFile(tempFile), getCanonicalFile(getCanonicalFile(tempFile)));
    }

    void handleDirectoryOnIOException(ThrowableConsumer<File> directoryHandler) throws Exception {
        ExecutorService executor = newSingleThreadExecutor();
        executor.submit(() -> handleDirectoryOnIOException0(directoryHandler));
        shutdown(executor);
        executor.awaitTermination(1, SECONDS);
    }

    File handleDirectoryOnIOException0(ThrowableConsumer<File> directoryHandler) throws Exception {
        File testDir = createRandomTempDirectory();

        ExecutorService fileCreationExecutor = newSingleThreadExecutor();

        AtomicBoolean creatingFile = new AtomicBoolean(true);

        long waitTime = 50L;

        fileCreationExecutor.submit(() -> {
            while (creatingFile.get()) {
                createRandomFile(testDir);
            }
            return null;
        });

        AtomicReference<IOException> ioExceptionReference = new AtomicReference<>();

        AtomicBoolean deletingDirectory = new AtomicBoolean(true);

        ExecutorService directoryDeletionExecutor = newSingleThreadExecutor();

        directoryDeletionExecutor.submit(() -> {
            while (deletingDirectory.get()) {
                try {
                    directoryHandler.accept(testDir);
                    sleep(waitTime / 10);
                } catch (IOException e) {
                    ioExceptionReference.set(e);
                    creatingFile.set(false);
                    deletingDirectory.set(false);
                    logger.trace(e.getMessage(), e);
                    break;
                } catch (Throwable e) {
                    throw wrap(e, Exception.class);
                }
            }
            return null;
        });

        for (int i = 0; i < 100; i++) {
            if (creatingFile.get()) {
                sleep(waitTime);
            }
        }

        shutdown(fileCreationExecutor);
        shutdown(directoryDeletionExecutor);

        assertNotNull(ioExceptionReference.get());
        assertFalse(creatingFile.get());
        assertFalse(deletingDirectory.get());

        return testDir;
    }

}