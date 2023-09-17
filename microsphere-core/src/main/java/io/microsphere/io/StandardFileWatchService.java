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

import io.microsphere.concurrent.CustomizedThreadFactory;
import io.microsphere.event.EventDispatcher;
import io.microsphere.io.event.FileChangedEvent;
import io.microsphere.io.event.FileChangedListener;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;

import static io.microsphere.event.EventDispatcher.getDefault;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Standard {@link FileWatchService} implementation based on JDK 7
 * {@link WatchService}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WatchService
 * @since 1.0.0
 */
public class StandardFileWatchService implements FileWatchService {

    private WatchService watchService;

    private EventDispatcher eventDispatcher;

    private final Executor executor;

    private final Map<Path, EventDispatcher> fileChangedEventDispatchersMap = new TreeMap<>();

    private volatile boolean started;

    public StandardFileWatchService() {
        this(newSingleThreadExecutor(new CustomizedThreadFactory("FileWatchService", true)));
    }

    public StandardFileWatchService(Executor executor) {
        this.executor = executor;
    }

    public void start() throws Exception {
        if (started) {
            throw new IllegalStateException("StandardFileWatchService has started");
        }
        FileSystem fileSystem = FileSystems.getDefault();
        this.watchService = fileSystem.newWatchService();
        started = true;
    }

    @Override
    public void watch(File file, FileChangedListener listener, FileChangedEvent.Kind... kinds) {
        Path filePath = file.toPath();
        Path dirPath = null;
        if (Files.isDirectory(filePath, LinkOption.NOFOLLOW_LINKS)) {
            dirPath = filePath;
        } else {
            dirPath = filePath.getParent();
        }

        EventDispatcher eventDispatcher = fileChangedEventDispatchersMap.computeIfAbsent(dirPath, k -> getDefault());
        eventDispatcher.addEventListener(listener);

    }

    public void stop() throws Exception {
        if (started) {
            if (watchService != null) {
                watchService.close();
            }
        }
    }
}
