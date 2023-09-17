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
