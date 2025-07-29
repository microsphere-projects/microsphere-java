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

import io.microsphere.annotation.Immutable;
import io.microsphere.event.Event;

import java.io.File;

/**
 * The event raised when the {@link File file} is changed
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see File
 * @since 1.0.0
 */
@Immutable
public class FileChangedEvent extends Event {

    private final Kind kind;

    /**
     * Constructs a prototypical Event.
     *
     * @param file The object on which the Event initially occurred.
     * @param kind the kind of {@link File} changed
     * @throws IllegalArgumentException if <code>file</code> or <code>kind</code> is null.
     */
    public FileChangedEvent(File file, Kind kind) throws IllegalArgumentException {
        super(file);
        if (kind == null) {
            throw new IllegalArgumentException("The 'kind' argument must not be null");
        }
        this.kind = kind;
    }

    /**
     * @return The file as the event source
     */
    public File getFile() {
        return (File) getSource();
    }

    /**
     * Get the kind of {@link File} changed
     *
     * @return {@link Kind}
     */
    public Kind getKind() {
        return kind;
    }

    @Override
    public String toString() {
        String sb = "FileChangedEvent{" + "kind=" + kind +
                ", file=" + getFile() +
                '}';
        return sb;
    }

    /**
     * The Kind of File Changed Event
     */
    public enum Kind {

        /**
         * The file or directory entry created
         */
        CREATED,

        /**
         * The file or directory entry modified
         */
        MODIFIED,

        /**
         * The file or directory entry deleted
         */
        DELETED
    }
}
