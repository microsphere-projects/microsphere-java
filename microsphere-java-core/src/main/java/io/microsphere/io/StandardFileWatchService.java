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

import io.microsphere.annotation.Nonnull;
import io.microsphere.event.EventDispatcher;
import io.microsphere.io.event.FileChangedEvent;
import io.microsphere.io.event.FileChangedEvent.Kind;
import io.microsphere.io.event.FileChangedListener;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.microsphere.collection.MapUtils.newTreeMap;
import static io.microsphere.concurrent.CustomizedThreadFactory.newThreadFactory;
import static io.microsphere.concurrent.ExecutorUtils.shutdown;
import static io.microsphere.concurrent.ExecutorUtils.shutdownOnExit;
import static io.microsphere.event.EventDispatcher.DIRECT_EXECUTOR;
import static io.microsphere.event.EventDispatcher.parallel;
import static io.microsphere.io.event.FileChangedEvent.Kind.CREATED;
import static io.microsphere.io.event.FileChangedEvent.Kind.DELETED;
import static io.microsphere.io.event.FileChangedEvent.Kind.MODIFIED;
import static io.microsphere.util.ArrayUtils.length;
import static java.lang.System.getProperty;
import static java.nio.file.FileSystems.getDefault;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Standard {@link FileWatchService} implementation based on JDK 7
 * {@link WatchService}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WatchService
 * @since 1.0.0
 */
public class StandardFileWatchService implements FileWatchService, AutoCloseable {

    /**
     * The default thread name prefix : "microsphere-file-watch-service"
     */
    public static final String DEFAULT_THREAD_NAME_PREFIX = "microsphere-file-watch-service";

    /**
     * The thread name prefix property name : "microsphere.file-watch-service.thread-name-prefix
     */
    public static final String THREAD_NAME_PREFIX_PROPERTY_NAME = "microsphere.file-watch-service.thread-name-prefix";

    /**
     * The thread name prefix , default value : "microsphere-file-watch-service-"
     */
    public static final String THREAD_NAME_PREFIX = getProperty(THREAD_NAME_PREFIX_PROPERTY_NAME, DEFAULT_THREAD_NAME_PREFIX);

    private static final WatchEvent.Kind<?>[] ALL_WATCH_EVENT_KINDS = {
            ENTRY_CREATE,
            ENTRY_DELETE,
            ENTRY_MODIFY
    };

    private WatchService watchService;

    private final ExecutorService eventLoopExecutor;

    private final Executor eventHandlerExecutor;

    private final Map<Path, FileChangedMetadata> fileChangedMetadataCache = newTreeMap();

    private volatile boolean started;

    private Future eventLoopFuture;

    public StandardFileWatchService() {
        this(DIRECT_EXECUTOR);
    }

    public StandardFileWatchService(Executor eventHandlerExecutor) {
        this(eventHandlerExecutor, newSingleThreadExecutor(newThreadFactory(THREAD_NAME_PREFIX, true)));
    }

    public StandardFileWatchService(Executor eventHandlerExecutor, ExecutorService eventLoopExecutor) {
        this.eventLoopExecutor = eventLoopExecutor;
        this.eventHandlerExecutor = eventHandlerExecutor;
        // shutdown the ExecutorService when JVM exits
        shutdownOnExit(eventLoopExecutor, eventHandlerExecutor);
    }

    public void start() throws Exception {
        if (started) {
            throw new IllegalStateException("StandardFileWatchService has started");
        }

        started = true;

        FileSystem fileSystem = getDefault();

        WatchService watchService = fileSystem.newWatchService();

        registerDirectoriesToWatchService(watchService);

        dispatchFileChangedEvents(watchService);

        this.watchService = watchService;

    }

    private void dispatchFileChangedEvents(WatchService watchService) {
        eventLoopFuture = eventLoopExecutor.submit(() -> {
            while (started) {
                WatchKey watchKey = null;
                try {
                    watchKey = watchService.take();
                    if (watchKey.isValid()) {
                        for (WatchEvent event : watchKey.pollEvents()) {
                            Watchable watchable = watchKey.watchable();
                            Path dirPath = (Path) watchable;
                            Path fileRelativePath = (Path) event.context();

                            FileChangedMetadata metadata = fileChangedMetadataCache.get(dirPath);
                            if (metadata != null) {
                                Path filePath = dirPath.resolve(fileRelativePath);
                                if (isDirectory(dirPath, NOFOLLOW_LINKS) || metadata.filePaths.contains(filePath)) {
                                    EventDispatcher eventDispatcher = metadata.eventDispatcher;
                                    WatchEvent.Kind watchEventKind = event.kind();
                                    dispatchFileChangedEvent(filePath, watchEventKind, eventDispatcher);
                                }
                            }
                        }
                    }
                } finally {
                    if (watchKey != null) {
                        watchKey.reset();
                    }
                }
            }
            return null;
        });
    }

    private void dispatchFileChangedEvent(Path filePath, WatchEvent.Kind watchEventKind, EventDispatcher eventDispatcher) {
        File file = filePath.toFile();
        Kind kind = toKind(watchEventKind);
        FileChangedEvent fileChangedEvent = new FileChangedEvent(file, kind);
        eventDispatcher.dispatch(fileChangedEvent);
    }

    private void registerDirectoriesToWatchService(WatchService watchService) throws Exception {
        for (Map.Entry<Path, FileChangedMetadata> entry : fileChangedMetadataCache.entrySet()) {
            Path directoryPath = entry.getKey();
            FileChangedMetadata metadata = entry.getValue();
            WatchEvent.Kind[] kinds = metadata.watchEventKinds;
            directoryPath.register(watchService, kinds);
        }
    }

    @Override
    public void watch(File file, FileChangedListener listener, Kind... kinds) {
        Path filePath = file.toPath();
        FileChangedMetadata metadata = getMetadata(filePath, kinds);
        metadata.filePaths.add(filePath);
        metadata.eventDispatcher.addEventListener(listener);
    }

    private FileChangedMetadata getMetadata(Path filePath, Kind... kinds) {
        final Path dirPath = isDirectory(filePath, NOFOLLOW_LINKS) ? filePath : filePath.getParent();
        return fileChangedMetadataCache.computeIfAbsent(dirPath, k -> {
            FileChangedMetadata metadata = new FileChangedMetadata();
            metadata.eventDispatcher = parallel(this.eventHandlerExecutor);
            metadata.watchEventKinds = toWatchEventKinds(kinds);
            return metadata;
        });
    }

    @Nonnull
    private WatchEvent.Kind<?>[] toWatchEventKinds(Kind[] kinds) {
        int size = length(kinds);
        if (size < 1) {
            return ALL_WATCH_EVENT_KINDS;
        }
        WatchEvent.Kind<?>[] watchEventKinds = new WatchEvent.Kind[size];
        for (int i = 0; i < size; i++) {
            Kind kind = kinds[i];
            watchEventKinds[i] = toWatchEventKind(kind);
        }
        return watchEventKinds;
    }

    @Nonnull
    private WatchEvent.Kind<?> toWatchEventKind(Kind kind) {
        WatchEvent.Kind<?> watchEventKind = OVERFLOW;
        switch (kind) {
            case CREATED:
                watchEventKind = ENTRY_CREATE;
                break;
            case MODIFIED:
                watchEventKind = ENTRY_MODIFY;
                break;
            case DELETED:
                watchEventKind = ENTRY_DELETE;
                break;
        }
        return watchEventKind;
    }

    @Nonnull
    private Kind toKind(WatchEvent.Kind<?> watchEventKind) {
        final Kind kind;
        if (ENTRY_CREATE.equals(watchEventKind)) {
            kind = CREATED;
        } else if (ENTRY_MODIFY.equals(watchEventKind)) {
            kind = MODIFIED;
        } else {
            kind = DELETED;
        }
        return kind;
    }

    public void stop() throws Exception {
        if (started) {
            // set the flag "started" to false
            started = false;
            // wait for the event loop to complete
            if (!eventLoopExecutor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                // if the event loop is not done, try to cancel the task
                eventLoopFuture.cancel(true);
            }

            if (watchService != null) {
                watchService.close();
            }
            fileChangedMetadataCache.clear();
            shutdown(eventLoopExecutor);
            shutdown(eventHandlerExecutor);
        }
    }

    @Override
    public void close() throws Exception {
        this.stop();
    }

    private static class FileChangedMetadata {

        private final Set<Path> filePaths = new TreeSet<>();

        private EventDispatcher eventDispatcher;

        private WatchEvent.Kind<?>[] watchEventKinds;
    }

}
