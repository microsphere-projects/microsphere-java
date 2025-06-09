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

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import static io.microsphere.annotation.processor.ResourceProcessor.exists;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link FilerProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourceProcessor
 * @since 1.0.0
 */
class FilerProcessorTest extends AbstractAnnotationProcessingTest {

    private FilerProcessor processor;

    @Override
    protected void beforeTest() {
        this.processor = new FilerProcessor(super.processingEnv);
    }

    @Test
    void testProcessInFiler() {
        JavaFileObject sourceFile = processor.processInFiler(filer -> filer.createSourceFile("io.microsphere.annotation.processor.test.Test"));
        assertNotNull(sourceFile);
        assertFalse(exists(sourceFile));
    }

    @Test
    void testProcessInFilerOnFailed() {
        assertNull(processor.processInFiler(filer -> {
            throw new RuntimeException();
        }));
    }

    @Test
    void testGetJavaFileManager() {
        JavaFileManager javaFileManager = processor.getJavaFileManager();
        assertNotNull(javaFileManager);
    }
}