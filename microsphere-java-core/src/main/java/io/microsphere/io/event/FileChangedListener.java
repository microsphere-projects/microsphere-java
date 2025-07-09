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
 * A listener interface for receiving file change events.
 *
 * <p>This interface extends {@link EventListener} to handle {@link FileChangedEvent} instances,
 * providing a more specific way to react to changes in files or directories. When implementing
 * this interface, you can either override the general {@link #onEvent(FileChangedEvent)} method
 * or selectively override the more specific methods like {@link #onFileCreated(FileChangedEvent)},
 * {@link #onFileModified(FileChangedEvent)}, and {@link #onFileDeleted(FileChangedEvent)}.</p>
 *
 * <h3>Example Usage</h3>
 * Here's an example of how to implement this interface:
 *
 * <pre>{@code
 * public class MyFileChangeListener implements FileChangedListener {
 *
 *     private final int priority;
 *
 *     public MyFileChangeListener(int priority) {
 *         this.priority = priority;
 *     }
 *
 *     @Override
 *     public void onFileCreated(FileChangedEvent event) {
 *         System.out.println("File created: " + event.getFile().getAbsolutePath());
 *     }
 *
 *     @Override
 *     public void onFileModified(FileChangedEvent event) {
 *         System.out.println("File modified: " + event.getFile().getAbsolutePath());
 *     }
 *
 *     @Override
 *     public void onFileDeleted(FileChangedEvent event) {
 *         System.out.println("File deleted: " + event.getFile().getAbsolutePath());
 *     }
 *
 *     @Override
 *     public int getPriority() {
 *         return priority;
 *     }
 * }
 * }</pre>
 *
 * <p>If you need custom handling logic that applies to all types of file changes, you can also
 * override the general {@code onEvent} method:</p>
 *
 * <pre>{@code
 * @Override
 * public void onEvent(FileChangedEvent event) {
 *     switch (event.getKind()) {
 *         case CREATED:
 *             // Handle file creation
 *             break;
 *         case MODIFIED:
 *             // Handle file modification
 *             break;
 *         case DELETED:
 *             // Handle file deletion
 *             break;
 *     }
 * }
 * }</pre>
 *
 * <p>The default implementation of the {@code onEvent} method routes the event to the appropriate
 * handler based on the event kind.</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see FileChangedEvent
 * @see EventListener
 * @see Event
 * @since 1.0.0
 */
public interface FileChangedListener extends EventListener<FileChangedEvent> {

    default void onEvent(FileChangedEvent event) {
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
