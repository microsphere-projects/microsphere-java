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
import io.microsphere.io.event.FileChangedEvent;
import io.microsphere.io.event.FileChangedListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import static io.microsphere.io.event.FileChangedEvent.Kind.CREATED;
import static io.microsphere.io.event.FileChangedEvent.Kind.DELETED;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link StandardFileWatchService} Test For Directory
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class StandardFileWatchServiceTestForDirectory extends AbstractTestCase {

    private StandardFileWatchService fileWatchService;

    private File testDir;

    private final AtomicReference<File> fileReference = new AtomicReference<>();

    @BeforeEach
    public void init() throws Exception {
        StandardFileWatchService fileWatchService = new StandardFileWatchService();
        File testDir = createRandomTempDirectory();

        this.fileWatchService = fileWatchService;
        this.testDir = testDir;
        fileWatchService.watch(testDir, new FileChangedListener() {
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

        assertThrows(IllegalStateException.class, fileWatchService::start);
    }

    @AfterEach
    public void destroy() throws Exception {
        this.fileWatchService.stop();
    }

    @Test
    public void test() throws Exception {

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
    }

}
