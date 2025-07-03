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

import java.io.File;

/**
 * File Watch Service
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
/**
 * A service that watches files or directories for changes and notifies registered listeners.
 * <p>
 * This interface provides methods to register listeners for specific files or directories,
 * specifying the types of events the listener is interested in. When a watched file or directory
 * experiences an event (e.g., creation, modification, deletion), the associated listeners are notified.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Example 1: Watching a single file with a single listener and specific event kinds
 * FileWatchService watchService = ...; // implementation instance
 * File fileToWatch = new File("/path/to/file.txt");
 * FileChangedListener listener = event -> System.out.println("File changed: " + event.getFile());
 *
 * watchService.watch(fileToWatch, listener, FileChangedEvent.Kind.MODIFIED);
 *
 * // Example 2: Watching a directory with multiple listeners and all event kinds
 * File dirToWatch = new File("/path/to/directory");
 * List<FileChangedListener> listeners = Arrays.asList(
 *     event -> System.out.println("Listener 1 triggered: " + event),
 *     event -> System.out.println("Listener 2 triggered: " + event)
 * );
 *
 * watchService.watch(dirToWatch, listeners); // All kinds by default
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface FileWatchService {

    /**
     * Watch the specified file associating a {@link FileChangedListener listener} with
     * interest {@link FileChangedEvent.Kind kinds}
     *
     * @param file     the file or directory
     * @param listener one  {@link FileChangedListener listener}
     * @param kinds    one or more {@link FileChangedEvent.Kind kinds of File Changed Events},
     *                 all kinds should be interested if blank
     */
    void watch(File file, FileChangedListener listener, FileChangedEvent.Kind... kinds);

    /**
     * Watch the specified file associating the {@link FileChangedListener listeners} with
     * interest {@link FileChangedEvent.Kind kinds}
     *
     * @param file      the file or directory
     * @param listeners one or more {@link FileChangedListener listeners}
     * @param kinds     one or more {@link FileChangedEvent.Kind kinds of File Changed Events},
     *                  all kinds should be interested if blank
     */
    default void watch(File file, Iterable<FileChangedListener> listeners, FileChangedEvent.Kind... kinds) {
        listeners.forEach(listener -> watch(file, listener, kinds));
    }
}
