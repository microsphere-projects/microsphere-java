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

import io.microsphere.io.event.FileChangedEvent;
import io.microsphere.io.event.FileChangedListener;
import io.microsphere.lang.function.ThrowableAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.microsphere.io.FileUtils.deleteDirectory;
import static io.microsphere.io.FileUtils.forceDelete;
import static io.microsphere.util.ClassLoaderUtils.getResource;
import static io.microsphere.util.SystemUtils.JAVA_IO_TMPDIR;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.platform.commons.logging.LoggerFactory.getLogger;

/**
 * {@link StandardFileWatchService} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class StandardFileWatchServiceTest {

    private static final String TEST_FILE_LOCATION = "test.txt";

    private StandardFileWatchService fileWatchService;


    private File sourceFile;

    private File targetDir;

    private File targetFile;

    private CountDownLatch countDownLatch;

    private ExecutorService executor;

    @BeforeEach
    public void init() throws Exception {
        StandardFileWatchService fileWatchService = new StandardFileWatchService(ForkJoinPool.commonPool());
        URL resource = getResource(this.getClass().getClassLoader(), TEST_FILE_LOCATION);
        String resourceFilePath = resource.getFile();
        this.sourceFile = new File(resourceFilePath);
        File targetDir = new File(JAVA_IO_TMPDIR, "test");
        deleteDirectory(targetDir);
        targetDir.mkdirs();

        this.fileWatchService = fileWatchService;
        this.targetDir = targetDir;
        this.targetFile = new File(targetDir, this.sourceFile.getName());
        this.countDownLatch = new CountDownLatch(3);
        this.executor = newSingleThreadExecutor();

        fileWatchService.watch(targetFile, new MyFileChangedListener(this.countDownLatch));
        fileWatchService.watch(targetFile, new FileChangedListener() {
        });
        fileWatchService.start();
    }

    @AfterEach
    public void destroy() throws Exception {
        this.fileWatchService.stop();
        this.executor.shutdown();
    }

    @Test
    public void test() throws Exception {
        // create file
        Path sourcePath = this.sourceFile.toPath();
        Path targetFilePath = this.targetFile.toPath();
        Files.copy(sourcePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);

        countDownLatch.await();
    }

    private class MyFileChangedListener implements FileChangedListener {

        private final Logger logger = getLogger(getClass());

        private CountDownLatch countDownLatch;

        public MyFileChangedListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onFileCreated(FileChangedEvent event) {
            File targetFile = event.getFile();
            countDownLatch.countDown();
            logger.info(event::toString);

            // modified file
            async(() -> {
                Files.write(targetFile.toPath(), "Hello,World".getBytes(StandardCharsets.UTF_8));
            });
        }

        @Override
        public void onFileModified(FileChangedEvent event) {
            countDownLatch.countDown();
            logger.info(event::toString);

            // delete file
            async(() -> {
                forceDelete(targetFile);
            });
        }

        @Override
        public void onFileDeleted(FileChangedEvent event) {
            countDownLatch.countDown();
            logger.info(event::toString);
        }
    }

    private void async(ThrowableAction task) {
        Future future = executor.submit(() -> {
            try {
                task.execute();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        try {
            future.get(100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
        }
    }

}
