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
package io.microsphere.io.event;

import io.microsphere.event.Event;
import io.microsphere.event.EventListener;

/**
 * The event listener for {@link FileChangedEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see FileChangedEvent
 * @see EventListener
 * @see Event
 * @since 1.0.0
 */
public interface FileChangedListener extends EventListener<FileChangedEvent> {

    default void onEvent(FileChangedEvent event) {
        // DO NOTHING
        FileChangedEvent.Kind kind = event.getKind();
        switch (kind) {
            case CREATED:
                onFileCreated(event);
                break;
            case MODIFIED:
                onFileModified(event);
                break;
            case DELETED:
                onFileDeleted(event);
                break;
        }
    }

    /**
     * Invoked when the file has been created
     *
     * @param event the {@link FileChangedEvent.Kind#CREATED created} {@link FileChangedEvent event}
     */
    default void onFileCreated(FileChangedEvent event) {
    }

    /**
     * Invoked when the file has been modified
     *
     * @param event the {@link FileChangedEvent.Kind#MODIFIED modified} {@link FileChangedEvent event}
     */
    default void onFileModified(FileChangedEvent event) {
    }

    /**
     * Invoked when the file has been deleted
     *
     * @param event the {@link FileChangedEvent.Kind#DELETED deleted} {@link FileChangedEvent event}
     */
    default void onFileDeleted(FileChangedEvent event) {
    }
}
