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

package io.microsphere.lang.model.util;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import javax.annotation.processing.Messager;
import java.lang.reflect.Method;

import static io.microsphere.lang.model.util.MessagerUtils.printError;
import static io.microsphere.lang.model.util.MessagerUtils.printMandatoryWarning;
import static io.microsphere.lang.model.util.MessagerUtils.printMessage;
import static io.microsphere.lang.model.util.MessagerUtils.printNote;
import static io.microsphere.lang.model.util.MessagerUtils.printWarning;
import static javax.tools.Diagnostic.Kind.OTHER;

/**
 * {@link MessagerUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MessagerUtils
 * @see Messager
 * @since 1.0.0
 */
class MessagerUtilsTest extends UtilTest {

    private Messager messager;

    @Override
    protected void beforeTest(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) {
        super.beforeTest(invocationContext,extensionContext);
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