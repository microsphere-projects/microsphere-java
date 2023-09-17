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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

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

    private CountDownLatch countDownLatch;

    @Before
    public void init() throws Exception {
        fileWatchService = new StandardFileWatchService();
        URL resource = getResource(this.getClass().getClassLoader(), TEST_FILE_LOCATION);
        String resourceFilePath = resource.getFile();
        File resourceFile = new File(resourceFilePath);
        countDownLatch = new CountDownLatch(1);
        fileWatchService.watch(resourceFile, new MyFileChangedListener(countDownLatch));
    }

    @After
    public void destroy() throws Exception {
        fileWatchService.stop();
    }

    @Test
    public void test() throws Exception {
        fileWatchService.start();
        countDownLatch.await();
    }

    private static class MyFileChangedListener implements FileChangedListener {

        private CountDownLatch countDownLatch;

        public MyFileChangedListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onFileCreated(FileChangedEvent event) {
            countDownLatch.countDown();
            System.out.println(event);
        }

        @Override
        public void onFileModified(FileChangedEvent event) {
            countDownLatch.countDown();
            System.out.println(event);
        }

        @Override
        public void onFileDeleted(FileChangedEvent event) {
            countDownLatch.countDown();
            System.out.println(event);
        }
    }

}
