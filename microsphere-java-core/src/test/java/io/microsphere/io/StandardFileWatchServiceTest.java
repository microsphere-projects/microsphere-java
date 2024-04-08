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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;

import static io.microsphere.util.ClassLoaderUtils.getResource;

/**
 * {@link StandardFileWatchService} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class StandardFileWatchServiceTest {

    private static final String TEST_FILE_LOCATION = "test.txt";

    private StandardFileWatchService fileWatchService;

    private File resourceFile;

    private CountDownLatch countDownLatch;

    @BeforeEach
    public void init() throws Exception {
        fileWatchService = new StandardFileWatchService(ForkJoinPool.commonPool());
        URL resource = getResource(this.getClass().getClassLoader(), TEST_FILE_LOCATION);
        String resourceFilePath = resource.getFile();
        this.resourceFile = new File(resourceFilePath);
        this.countDownLatch = new CountDownLatch(1);
        this.fileWatchService.watch(this.resourceFile, new MyFileChangedListener(countDownLatch));
    }

    @AfterEach
    public void destroy() throws Exception {
        fileWatchService.stop();
    }

    @Test
    public void test() throws Exception {
        fileWatchService.start();
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1 * 100);
                Files.write(resourceFile.toPath(), "Hello,World".getBytes(StandardCharsets.UTF_8));
            } catch (Throwable e) {
            }
        });
        thread.start();
        thread.join();
        countDownLatch.await();
    }

    private static class MyFileChangedListener implements FileChangedListener {

        private static final Logger logger = LoggerFactory.getLogger(MyFileChangedListener.class);

        private CountDownLatch countDownLatch;

        public MyFileChangedListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onFileCreated(FileChangedEvent event) {
            countDownLatch.countDown();
            logger.info(event::toString);
        }

        @Override
        public void onFileModified(FileChangedEvent event) {
            countDownLatch.countDown();
            logger.info(event::toString);
        }

        @Override
        public void onFileDeleted(FileChangedEvent event) {
            countDownLatch.countDown();
            logger.info(event::toString);
        }
    }

}
