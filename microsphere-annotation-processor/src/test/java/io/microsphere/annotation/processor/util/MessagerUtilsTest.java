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

package io.microsphere.annotation.processor.util;


import io.microsphere.annotation.processor.AbstractAnnotationProcessingTest;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.Messager;

import static io.microsphere.annotation.processor.util.MessagerUtils.printError;
import static io.microsphere.annotation.processor.util.MessagerUtils.printMandatoryWarning;
import static io.microsphere.annotation.processor.util.MessagerUtils.printMessage;
import static io.microsphere.annotation.processor.util.MessagerUtils.printNote;
import static io.microsphere.annotation.processor.util.MessagerUtils.printWarning;
import static javax.tools.Diagnostic.Kind.OTHER;

/**
 * {@link MessagerUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MessagerUtils
 * @see Messager
 * @since 1.0.0
 */
class MessagerUtilsTest extends AbstractAnnotationProcessingTest {

    private Messager messager;

    @Override
    protected void beforeTest() {
        this.messager = this.processingEnv.getMessager();
    }

    @Test
    void testPrintNote() {
        printNote(this.processingEnv, "Hello, {}!", "printNote");
        printNote(this.messager, "Hello, {}!", "printNote");
    }

    @Test
    void testPrintWarning() {
        printWarning(this.processingEnv, "Hello, {}!", "printWarning");
        printWarning(this.messager, "Hello, {}!", "printWarning");
    }

    @Test
    void testPrintMandatoryWarning() {
        printMandatoryWarning(this.processingEnv, "Hello, {}!", "printMandatoryWarning");
        printMandatoryWarning(this.messager, "Hello, {}!", "printMandatoryWarning");
    }

    @Test
    void testPrintError() {
        printError(this.processingEnv, "Hello, {}!", "printError");
        printError(this.messager, "Hello, {}!", "printError");
    }

    @Test
    void testPrintMessage() {
        printMessage(this.processingEnv, OTHER, "Hello, {}!", "printMessage");
        printMessage(this.messager, OTHER, "Hello, {}!", "printMessage");
    }
}