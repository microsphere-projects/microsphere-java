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


import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static io.microsphere.io.event.FileChangedEvent.Kind.CREATED;
import static io.microsphere.io.event.FileChangedEvent.Kind.DELETED;
import static io.microsphere.io.event.FileChangedEvent.Kind.MODIFIED;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link LoggingFileChangedListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggingFileChangedListener
 * @since 1.0.0
 */
class LoggingFileChangedListenerTest extends AbstractTestCase {

    private LoggingFileChangedListener listener;

    @BeforeEach
    void setUp() {
        this.listener = new LoggingFileChangedListener();
    }

    @Test
    void testOnFileCreated() throws IOException {
        File file = createRandomTempFile();
        FileChangedEvent event = new FileChangedEvent(file, CREATED);
        listener.onFileCreated(event);
        assertSame(file, event.getFile());
        assertSame(CREATED, event.getKind());
    }

    @Test
    void testOnFileModified() throws IOException {
        File file = createRandomTempFile();
        FileChangedEvent event = new FileChangedEvent(file, MODIFIED);
        listener.onFileModified(event);
        assertSame(file, event.getFile());
        assertSame(MODIFIED, event.getKind());
    }

    @Test
    void testOnFileDeleted() throws IOException {
        File file = createRandomTempFile();
        FileChangedEvent event = new FileChangedEvent(file, DELETED);
        listener.onFileDeleted(event);
        assertSame(file, event.getFile());
        assertSame(DELETED, event.getKind());
    }
}