/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional inpatternion regarding copyright ownership.
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

import io.microsphere.util.Utils;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;

import static io.microsphere.annotation.processor.util.LoggerUtils.debug;
import static io.microsphere.annotation.processor.util.LoggerUtils.error;
import static io.microsphere.annotation.processor.util.LoggerUtils.info;
import static io.microsphere.annotation.processor.util.LoggerUtils.warn;
import static io.microsphere.text.FormatUtils.format;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.MANDATORY_WARNING;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * {@link Messager} utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Messager
 * @since 1.0.0
 */
public interface MessagerUtils extends Utils {

    /**
     * Prints a note message using the {@link ProcessingEnvironment}'s {@link Messager}.
     *
     * <p>
     * This method formats the message using the provided pattern and arguments,
     * and delegates to the underlying {@link Messager} obtained from the processing environment.
     * </p>
     *
     * <h3>Example:</h3>
     * <pre>
     * ProcessingEnvironment processingEnv = ...; // Obtain processing environment
     * printNote(processingEnv, "Found {0} elements matching criteria", count);
     * </pre>
     *
     * @param processingEnv the processing environment to obtain the messager from
     * @param pattern       the message pattern to format (supports {@link String#format} syntax)
     * @param args          the arguments for the message pattern
     */
    static void printNote(ProcessingEnvironment processingEnv, String pattern, Object... args) {
        printNote(processingEnv.getMessager(), pattern, args);
    }

    /**
     * Prints a note message using the provided {@link Messager}.
     *
     * <p>
     * This method formats the message using the provided pattern and arguments,
     * and forwards it to the underlying {@link Messager}.
     * </p>
     *
     * <h3>Example:</h3>
     * <pre>
     * Messager messager = processingEnv.getMessager();
     * printNote(messager, "Found {0} elements matching criteria", count);
     * </pre>
     *
     * @param messager the messager to use for printing the message
     * @param pattern  the message pattern to format (supports {@link String#format} syntax)
     * @param args     the arguments for the message pattern
     */
    /**
     * Prints a note message using the provided {@link Messager}.
     *
     * <p>
     * This method formats the message using the provided pattern and arguments,
     * and forwards it to the underlying {@link Messager}.
     * </p>
     *
     * <h3>Example:</h3>
     * <pre>
     * Messager messager = processingEnv.getMessager();
     * printNote(messager, "Found {0} elements matching criteria", count);
     * </pre>
     *
     * @param messager the messager to use for printing the message
     * @param pattern  the message pattern to format (supports {@link String#format} syntax)
     * @param args     the arguments for the message pattern
     */
    static void printNote(Messager messager, String pattern, Object... args) {
        printMessage(messager, NOTE, pattern, args);
    }

    /**
     * Prints a warning message using the {@link ProcessingEnvironment}'s {@link Messager}.
     *
     * <p>
     * This method formats the message using the provided pattern and arguments,
     * and delegates to the underlying {@link Messager} obtained from the processing environment.
     * The message will be logged at the warning level through both the {@link Messager}
     * and internal logging utilities.
     * </p>
     *
     * <h3>Example:</h3>
     * <pre>
     * ProcessingEnvironment processingEnv = ...; // Obtain processing environment
     * printWarning(processingEnv, "Found {0} deprecated elements", count);
     * </pre>
     *
     * @param processingEnv the processing environment to obtain the messager from
     * @param pattern       the message pattern to format (supports {@link String#format} syntax)
     * @param args          the arguments for the message pattern
     */
    static void printWarning(ProcessingEnvironment processingEnv, String pattern, Object... args) {
        printWarning(processingEnv.getMessager(), pattern, args);
    }

    /**
     * Prints a warning message using the provided {@link Messager}.
     *
     * <p>
     * This method formats the message using the provided pattern and arguments,
     * and forwards it to the underlying {@link Messager} as a warning level message.
     * The message will also be logged through internal logging utilities at the warning level.
     * </p>
     *
     * <h3>Example:</h3>
     * <pre>
     * Messager messager = processingEnv.getMessager();
     * printWarning(messager, "Found {0} deprecated elements", count);
     * </pre>
     *
     * @param messager the messager to use for printing the message
     * @param pattern  the message pattern to format (supports {@link String#format} syntax)
     * @param args     the arguments for the message pattern
     */
    static void printWarning(Messager messager, String pattern, Object... args) {
        printMessage(messager, WARNING, pattern, args);
    }

    /**
     * Prints a mandatory warning message using the {@link ProcessingEnvironment}'s {@link Messager}.
     *
     * <p>
     * This method formats the message using the provided pattern and arguments,
     * and delegates to the underlying {@link Messager} obtained from the processing environment.
     * The message will be logged at the warning level through both the {@link Messager}
     * and internal logging utilities.
     * </p>
     *
     * <h3>Example:</h3>
     * <pre>
     * ProcessingEnvironment processingEnv = ...; // Obtain processing environment
     * printMandatoryWarning(processingEnv, "Found {0} obsolete elements", count);
     * </pre>
     *
     * @param processingEnv the processing environment to obtain the messager from
     * @param pattern       the message pattern to format (supports {@link String#format} syntax)
     * @param args          the arguments for the message pattern
     */
    static void printMandatoryWarning(ProcessingEnvironment processingEnv, String pattern, Object... args) {
        printMandatoryWarning(processingEnv.getMessager(), pattern, args);
    }

    /**
     * Prints a mandatory warning message using the provided {@link Messager}.
     *
     * <p>
     * This method formats the message using the provided pattern and arguments,
     * and forwards it to the underlying {@link Messager} as a mandatory warning level message.
     * The message will also be logged through internal logging utilities at the warning level.
     * </p>
     *
     * <h3>Example:</h3>
     * <pre>
     * Messager messager = processingEnv.getMessager();
     * printMandatoryWarning(messager, "Found {0} obsolete elements", count);
     * </pre>
     *
     * @param messager the messager to use for printing the message
     * @param pattern  the message pattern to format (supports {@link String#format} syntax)
     * @param args     the arguments for the message pattern
     */
    static void printMandatoryWarning(Messager messager, String pattern, Object... args) {
        printMessage(messager, MANDATORY_WARNING, pattern, args);
    }

    /**
     * Prints an error message using the {@link ProcessingEnvironment}'s {@link Messager}.
     *
     * <p>
     * This method formats the message using the provided pattern and arguments,
     * and delegates to the underlying {@link Messager} obtained from the processing environment.
     * The message will be logged at the error level through both the {@link Messager}
     * and internal logging utilities.
     * </p>
     *
     * <h3>Example:</h3>
     * <pre>
     * ProcessingEnvironment processingEnv = ...; // Obtain processing environment
     * printError(processingEnv, "Failed to process {0} elements", count);
     * </pre>
     *
     * @param processingEnv the processing environment to obtain the messager from
     * @param pattern       the message pattern to format (supports {@link String#format} syntax)
     * @param args          the arguments for the message pattern
     */
    static void printError(ProcessingEnvironment processingEnv, String pattern, Object... args) {
        printError(processingEnv.getMessager(), pattern, args);
    }

    /**
     * Prints an error message using the provided {@link Messager}.
     *
     * <p>
     * This method formats the message using the provided pattern and arguments,
     * and forwards it to the underlying {@link Messager} as an error level message.
     * The message will also be logged through internal logging utilities at the error level.
     * </p>
     *
     * <h3>Example:</h3>
     * <pre>
     * Messager messager = processingEnv.getMessager();
     * printError(messager, "Failed to process {0} elements", count);
     * </pre>
     *
     * @param messager the messager to use for printing the message
     * @param pattern  the message pattern to format (supports {@link String#format} syntax)
     * @param args     the arguments for the message pattern
     */
    static void printError(Messager messager, String pattern, Object... args) {
        printMessage(messager, ERROR, pattern, args);
    }

    /**
     * Prints a message of the specified kind using the {@link ProcessingEnvironment}'s {@link Messager}.
     *
     * <p>
     * This method retrieves the {@link Messager} from the provided {@link ProcessingEnvironment}
     * and delegates to the {@link #printMessage(Messager, Kind, String, Object...)} method
     * to handle message formatting and output.
     * </p>
     *
     * <h3>Example:</h3>
     * <pre>
     * ProcessingEnvironment processingEnv = ...; // Obtain processing environment
     * printMessage(processingEnv, Kind.WARNING, "Found {0} deprecated elements", count);
     * </pre>
     *
     * @param processingEnv the processing environment to obtain the messager from
     * @param kind          the kind of message to print (e.g., error, warning, note)
     * @param pattern       the message pattern to format (supports {@link String#format} syntax)
     * @param args          the arguments for the message pattern
     */
    static void printMessage(ProcessingEnvironment processingEnv, Kind kind, String pattern, Object... args) {
        printMessage(processingEnv.getMessager(), kind, pattern, args);
    }

    /**
     * Prints a message of the specified kind using the provided {@link Messager}.
     *
     * <p>
     * This method formats the message using the provided pattern and arguments,
     * sends it to the annotation processor's messager, and also logs it using
     * internal logging utilities at the appropriate level.
     * </p>
     *
     * <h3>Example:</h3>
     * <pre>
     * Messager messager = processingEnv.getMessager();
     * printMessage(messager, Kind.ERROR, "Failed to process {0} elements", count);
     * </pre>
     *
     * @param messager the messager to use for printing the message
     * @param kind     the kind of message to print (e.g., error, warning, note)
     * @param pattern  the message pattern to format (supports {@link String#format} syntax)
     * @param args     the arguments for the message pattern
     */
    static void printMessage(Messager messager, Kind kind, String pattern, Object... args) {
        String message = format(pattern, args);
        messager.printMessage(kind, message);
        switch (kind) {
            case ERROR:
                error(pattern, args);
                break;
            case WARNING:
            case MANDATORY_WARNING:
                warn(pattern, args);
                break;
            case NOTE:
                info(pattern, args);
                break;
            default:
                debug(pattern, args);
        }
    }
}
