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


import org.junit.jupiter.api.Test;

import javax.tools.FileObject;
import java.util.Optional;

import static io.microsphere.annotation.processor.ResourceProcessor.FOR_READING;
import static io.microsphere.annotation.processor.ResourceProcessor.FOR_WRITING;
import static io.microsphere.io.IOUtils.copyToString;
import static io.microsphere.nio.charset.CharsetUtils.DEFAULT_CHARSET;
import static java.lang.Boolean.FALSE;
import static java.lang.System.currentTimeMillis;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.tools.StandardLocation.SOURCE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    protected void beforeTest() {
        this.classOutputProcessor = new ResourceProcessor(super.processingEnv, CLASS_OUTPUT);
        this.sourcePathProcessor = new ResourceProcessor(super.processingEnv, SOURCE_PATH);
        this.randomResourceName = "test/" + currentTimeMillis() + ".txt";
    }

    @Override
    public void afterTest() {
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
    }

    @Test
    public void testGetResource() {
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
    public void testProcessInResourceInputStreamOnFailed() {
        assertThrows(RuntimeException.class, () -> this.sourcePathProcessor.processInResourceInputStream(JAVA_SOURCE_RESOURCE_NAME, inputStream -> {
            throw new RuntimeException();
        }));
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
    }

    @Test
    public void testProcessInResourceOutputStream() {
        this.classOutputProcessor.processInResourceOutputStream(randomResourceName, outputStream -> {
            outputStream.write(randomResourceName.getBytes(DEFAULT_CHARSET));
        });
    }

    @Test
    public void testProcessInResourceOutputStreamOnFailed() {
        assertThrows(RuntimeException.class, () -> this.classOutputProcessor.processInResourceOutputStream(this.randomResourceName, outputStream -> {
            throw new RuntimeException();
        }));
    }

    @Test
    public void testProcessInResourceOnWriter() {
        this.classOutputProcessor.processInResourceWriter(randomResourceName, writer -> {
            writer.write(randomResourceName);
        });
    }


    @Test
    public void testProcessInResourceOnWriterOnFailed() {
        assertThrows(RuntimeException.class, () -> this.classOutputProcessor.processInResourceWriter(randomResourceName, writer -> {
            throw new RuntimeException();
        }));
    }
}