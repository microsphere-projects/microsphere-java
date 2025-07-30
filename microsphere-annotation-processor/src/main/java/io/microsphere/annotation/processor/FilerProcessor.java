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

import io.microsphere.lang.function.ThrowableFunction;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileManager;
import java.util.function.BiFunction;

import static io.microsphere.annotation.processor.util.MessagerUtils.printMandatoryWarning;
import static io.microsphere.reflect.FieldUtils.getFieldValue;

/**
 * A processor class that provides safe and exception-handled operations for interacting with the {@link Filer}
 * in an annotation processing environment. This class wraps calls to the underlying {@link Filer} instance
 * obtained from the provided {@link ProcessingEnvironment}, ensuring robust resource handling and simplifying
 * error management through functional interfaces.
 *
 * <p>It supports executing operations on the {@link Filer} using a callback model, allowing custom logic to be
 * applied while handling exceptions gracefully using provided handlers.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>
 * // Creating an instance of FilerProcessor
 * FilerProcessor filerProcessor = new FilerProcessor(processingEnv);
 *
 * // Using processInFiler to create a source file
 * filerProcessor.processInFiler(filer -> {
 *     JavaFileObject file = filer.createSourceFile("com.example.GeneratedClass");
 *     try (Writer writer = file.openWriter()) {
 *         writer.write("// Auto-generated class\npublic class GeneratedClass {}");
 *     }
 *     return null;
 * });
 *
 * // Using processInFiler with a custom exception handler
 * filerProcessor.processInFiler(
 *     filer -> {
 *         JavaFileObject file = filer.createSourceFile("com.example.AnotherGeneratedClass");
 *         try (Writer writer = file.openWriter()) {
 *             writer.write("// Auto-generated class\npublic class AnotherGeneratedClass {}");
 *         }
 *         return null;
 *     },
 *     (filer, e) -> {
 *         System.err.println("Failed to generate file: " + e.getMessage());
 *         return null;
 *     }
 * );
 * </pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see #processInFiler(ThrowableFunction)
 * @see #processInFiler(ThrowableFunction, BiFunction)
 * @since 1.0.0
 */
public class FilerProcessor {

    private final ProcessingEnvironment processingEnv;

    public FilerProcessor(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public <T> T processInFiler(ThrowableFunction<Filer, T> filerCallback) {
        return processInFiler(filerCallback, (filer, e) -> {
            printMandatoryWarning(this.processingEnv, "[FilerProcessor] Failed to process in Filer : {}", filer, e);
            return null;
        });
    }

    public <T> T processInFiler(ThrowableFunction<Filer, T> filerCallback, BiFunction<Filer, Throwable, T> exceptionHandler) {
        Filer filer = processingEnv.getFiler();
        return filerCallback.execute(filer, exceptionHandler);
    }

    /**
     * Get the {@link JavaFileManager}
     *
     * @return the {@link JavaFileManager}
     */
    public JavaFileManager getJavaFileManager() {
        return processInFiler(filer -> getFieldValue(filer, "fileManager"));
    }

}
