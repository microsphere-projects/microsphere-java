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

package io.microsphere.annotation.processor;


import io.microsphere.test.annotation.processing.AbstractAnnotationProcessingTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import javax.tools.FileObject;
import java.lang.reflect.Method;
import java.util.Optional;

import static io.microsphere.annotation.processor.ResourceProcessor.FOR_READING;
import static io.microsphere.annotation.processor.ResourceProcessor.FOR_WRITING;
import static io.microsphere.annotation.processor.ResourceProcessor.exists;
import static io.microsphere.io.IOUtils.copyToString;
import static io.microsphere.nio.charset.CharsetUtils.DEFAULT_CHARSET;
import static java.lang.Boolean.FALSE;
import static java.lang.System.currentTimeMillis;
import static java.util.Optional.empty;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.tools.StandardLocation.SOURCE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ResourceProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourceProcessor
 * @since 1.0.0
 */
class ResourceProcessorTest extends AbstractAnnotationProcessingTest {

    static final String JAVA_SOURCE_RESOURCE_NAME = "io/microsphere/annotation/processor/ResourceProcessorTest.java";

    private ResourceProcessor classOutputProcessor;

    private ResourceProcessor sourcePathProcessor;

    private String randomResourceName;

    @Override
    protected void beforeTest(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) {
        this.classOutputProcessor = new ResourceProcessor(super.processingEnv, CLASS_OUTPUT);
        this.sourcePathProcessor = new ResourceProcessor(super.processingEnv, SOURCE_PATH);
        this.randomResourceName = "test/" + currentTimeMillis() + ".txt";
    }

    @Override
    protected void afterTest(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext, Object result, Throwable failure) {
        this.classOutputProcessor.getResource(this.randomResourceName, FOR_WRITING).ifPresent(FileObject::delete);
    }

    @Test
    void testProcessInResourceForReading() {
        assertEquals(FALSE, this.classOutputProcessor.processInResource(this.randomResourceName, FOR_READING, fileObject -> fileObject.isPresent()));
    }

    @Test
    void testProcessInResourceOnFailed() {
        assertThrows(RuntimeException.class, () -> this.classOutputProcessor.processInResource(this.randomResourceName, FOR_READING, fileObject -> {
            throw new RuntimeException();
        }));

        assertNull(this.classOutputProcessor.processInResource(this.randomResourceName, FOR_READING, fileObject -> {
            throw new RuntimeException();
        }, e -> null));
    }

    @Test
    void testGetResource() {
        Optional<FileObject> resource = this.sourcePathProcessor.getResource(JAVA_SOURCE_RESOURCE_NAME, FOR_READING);
        assertTrue(resource.isPresent());
    }

    @Test
    void testProcessInResourceInputStream() {
        Optional<String> content = this.sourcePathProcessor.processInResourceInputStream(JAVA_SOURCE_RESOURCE_NAME, inputStream -> copyToString(inputStream));
        assertTrue(content.isPresent());
        assertNotNull(content.get());
    }

    @Test
    void testProcessInResourceInputStreamOnFailed() {
        assertThrows(RuntimeException.class, () -> this.sourcePathProcessor.processInResourceInputStream(JAVA_SOURCE_RESOURCE_NAME, inputStream -> {
            throw new RuntimeException();
        }));

        assertSame(empty(), this.sourcePathProcessor.processInResourceInputStream(JAVA_SOURCE_RESOURCE_NAME, inputStream -> {
            throw new RuntimeException();
        }, (f, e) -> null));
    }

    @Test
    void testProcessInResourceReader() {
        Optional<String> content = this.sourcePathProcessor.processInResourceReader(JAVA_SOURCE_RESOURCE_NAME, reader -> copyToString(reader));
        assertTrue(content.isPresent());
        assertNotNull(content.get());
    }

    @Test
    void testProcessInResourceReaderOnFailed() {
        assertThrows(RuntimeException.class, () -> this.sourcePathProcessor.processInResourceReader(JAVA_SOURCE_RESOURCE_NAME, reader -> {
            throw new RuntimeException();
        }));

        assertSame(empty(), this.sourcePathProcessor.processInResourceReader(JAVA_SOURCE_RESOURCE_NAME, reader -> {
            throw new RuntimeException();
        }, (f, e) -> null));
    }

    @Test
    void testProcessInResourceContent() {
        Optional<String> content = this.sourcePathProcessor.processInResourceContent(JAVA_SOURCE_RESOURCE_NAME, String::valueOf);
        assertNotNull(content);
    }

    @Test
    void testProcessInResourceContentOnFailed() {
        assertThrows(RuntimeException.class, () -> this.sourcePathProcessor.processInResourceContent(JAVA_SOURCE_RESOURCE_NAME, content -> {
            throw new RuntimeException();
        }));

        assertSame(empty(), this.sourcePathProcessor.processInResourceContent(JAVA_SOURCE_RESOURCE_NAME, content -> {
            throw new RuntimeException();
        }, (f, e) -> null));
    }

    @Test
    void testProcessInResourceOutputStream() {
        this.classOutputProcessor.processInResourceOutputStream(randomResourceName, outputStream -> {
            outputStream.write(randomResourceName.getBytes(DEFAULT_CHARSET));
        });
    }

    @Test
    void testProcessInResourceOutputStreamOnFailed() {
        assertThrows(RuntimeException.class, () -> this.classOutputProcessor.processInResourceOutputStream(this.randomResourceName, outputStream -> {
            throw new RuntimeException();
        }));

        this.classOutputProcessor.processInResourceOutputStream(this.randomResourceName, outputStream -> {
            throw new RuntimeException();
        }, (f, e) -> {
            assertNotNull(e);
        });
    }

    @Test
    void testProcessInResourceOnWriter() {
        this.classOutputProcessor.processInResourceWriter(randomResourceName, writer -> {
            writer.write(randomResourceName);
        });
    }

    @Test
    void testProcessInResourceOnWriterOnFailed() {
        assertThrows(RuntimeException.class, () -> this.classOutputProcessor.processInResourceWriter(randomResourceName, writer -> {
            throw new RuntimeException();
        }));

        this.classOutputProcessor.processInResourceWriter(this.randomResourceName, writer -> {
            throw new RuntimeException();
        }, (f, e) -> {
            assertNotNull(e);
        });
    }

    @Test
    void testExistsOnNull() {
        assertFalse(exists(null));
    }
}