/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.io;

import io.microsphere.AbstractTestCase;
import io.microsphere.io.event.DefaultFileChangedListener;
import io.microsphere.io.event.FileChangedEvent;
import io.microsphere.io.event.FileChangedEvent.Kind;
import io.microsphere.io.event.FileChangedListener;
import io.microsphere.io.event.LoggingFileChangedListener;
import io.microsphere.lang.function.ThrowableAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static io.microsphere.concurrent.ExecutorUtils.shutdown;
import static io.microsphere.io.FileUtils.deleteDirectory;
import static io.microsphere.io.FileUtils.forceDelete;
import static io.microsphere.io.StandardFileWatchService.ALL_WATCH_EVENT_KINDS;
import static io.microsphere.io.StandardFileWatchService.toKind;
import static io.microsphere.io.StandardFileWatchService.toWatchEventKinds;
import static io.microsphere.io.event.FileChangedEvent.Kind.CREATED;
import static io.microsphere.io.event.FileChangedEvent.Kind.DELETED;
import static io.microsphere.io.event.FileChangedEvent.Kind.MODIFIED;
import static io.microsphere.io.event.FileChangedEvent.Kind.values;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ClassLoaderUtils.getResource;
import static io.microsphere.util.ExceptionUtils.wrap;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.write;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.ForkJoinPool.commonPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link StandardFileWatchService} Test For File
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class StandardFileWatchServiceTest extends AbstractTestCase {

    private File testDir;

    private ExecutorService executor;

    @BeforeEach
    void setUp() throws Exception {
        this.testDir = createRandomTempDirectory();
        this.executor = newSingleThreadExecutor();
    }

    @AfterEach
    void tearDown() throws Exception {
        shutdown(this.executor);
        deleteDirectory(this.testDir);
    }

    @Test
    void testToWatchEventKinds() {
        assertArrayEquals(ALL_WATCH_EVENT_KINDS, toWatchEventKinds());
        assertArrayEquals(ALL_WATCH_EVENT_KINDS, toWatchEventKinds(new Kind[0]));
        assertArrayEquals(ALL_WATCH_EVENT_KINDS, toWatchEventKinds((Kind[]) null));
        assertArrayEquals(ALL_WATCH_EVENT_KINDS, toWatchEventKinds(CREATED, DELETED, MODIFIED));
        assertArrayEquals(ofArray(ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE), toWatchEventKinds(CREATED, MODIFIED, DELETED));
    }

    @Test
    void testToWatchEventKind() {
        assertArrayEquals(ALL_WATCH_EVENT_KINDS, toWatchEventKinds());
        assertArrayEquals(ALL_WATCH_EVENT_KINDS, toWatchEventKinds(new Kind[0]));
        assertArrayEquals(ALL_WATCH_EVENT_KINDS, toWatchEventKinds((Kind[]) null));
    }

    @Test
    void testToKind() {
        assertEquals(CREATED, toKind(ENTRY_CREATE));
        assertEquals(MODIFIED, toKind(ENTRY_MODIFY));
        assertEquals(DELETED, toKind(ENTRY_DELETE));
    }

    @Test
    void testToKindOnOverflow() {
        assertThrows(IllegalArgumentException.class, () -> toKind(OVERFLOW));
    }

    @Test
    void testFile() throws Exception {
        URL resource = getResource(super.classLoader, "test.txt");
        String resourceFilePath = resource.getFile();
        File sourceFile = new File(resourceFilePath);

        File targetFile = new File(this.testDir, sourceFile.getName());

        CountDownLatch countDownLatch = new CountDownLatch(3);

        try (StandardFileWatchService fileWatchService = new StandardFileWatchService(commonPool())) {
            // watch file and attach listeners
            fileWatchService.watch(targetFile, new MyFileChangedListener(targetFile, countDownLatch), values());
            fileWatchService.watch(targetFile, new LoggingFileChangedListener());
            fileWatchService.watch(targetFile, new DefaultFileChangedListener());

            // start StandardFileWatchService
            fileWatchService.start();

            assertTrue(fileWatchService.isStarted());

            // start again
            assertThrows(IllegalStateException.class, fileWatchService::start);

            assertTrue(fileWatchService.isStarted());

            // copy file
            Path sourcePath = sourceFile.toPath();
            Path targetFilePath = targetFile.toPath();
            copy(sourcePath, targetFilePath, REPLACE_EXISTING);

            // await for completion
            countDownLatch.await();

            fileWatchService.stop();

            assertFalse(fileWatchService.isStarted());
        }
    }

    @Test
    void testDirectory() throws Exception {

        AtomicReference<File> fileReference = new AtomicReference<>();

        try (StandardFileWatchService fileWatchService = new StandardFileWatchService()) {

            fileWatchService.watch(this.testDir, new FileChangedListener() {
                @Override
                public void onFileCreated(FileChangedEvent event) {
                    File file = event.getFile();
                    fileReference.set(file);
                    log("The file[path: '{}'] is created", file);
                }

                @Override
                public void onFileDeleted(FileChangedEvent event) {
                    File file = event.getFile();
                    fileReference.set(file);
                    log("The file[path: '{}'] is deleted", file);
                }
            }, CREATED, DELETED);

            fileWatchService.start();

            assertTrue(fileWatchService.isStarted());

            assertThrows(IllegalStateException.class, fileWatchService::start);

            assertTrue(fileWatchService.isStarted());

            // create a test file
            File testFile = createRandomFile(testDir);

            while (!testFile.equals(fileReference.get())) {
                // spin
            }

            // delete the test file
            testFile.delete();

            while (fileReference.get() == null) {
                // spin
            }

            fileWatchService.stop();

            assertFalse(fileWatchService.isStarted());
        }
    }

    private class MyFileChangedListener implements FileChangedListener {

        private final File targetFile;

        private final CountDownLatch countDownLatch;

        public MyFileChangedListener(File targetFile, CountDownLatch countDownLatch) {
            this.targetFile = targetFile;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onFileCreated(FileChangedEvent event) {
            File targetFile = event.getFile();
            countDownLatch.countDown();
            // modified file
            async(() -> {
                write(targetFile.toPath(), "Hello,World".getBytes(UTF_8));
            });
        }

        @Override
        public void onFileModified(FileChangedEvent event) {
            countDownLatch.countDown();
            // delete file
            async(() -> {
                forceDelete(targetFile);
            });
        }

        @Override
        public void onFileDeleted(FileChangedEvent event) {
            countDownLatch.countDown();
        }
    }

    private void async(ThrowableAction task) {
        Future future = executor.submit(() -> {
            try {
                task.execute();
            } catch (Throwable e) {
                wrap(e, Exception.class);
            }
            return null;
        });
        try {
            future.get(100, MILLISECONDS);
        } catch (Exception e) {
            log("Failed to async(timeout : 100ms) : {}", e.getMessage(), e);
            future.cancel(true);
        }
    }

}
