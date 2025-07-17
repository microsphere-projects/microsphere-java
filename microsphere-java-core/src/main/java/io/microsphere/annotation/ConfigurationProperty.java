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
package io.microsphere.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The metadata annotation used to declare on the Java field whose modifiers usually are static and final
 * for the configuration property.
 * <p>
 * The module that named "microsphere-annotation-processor" generates the metadata resource in the classpath.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface ConfigurationProperty {

    /**
     * The name of the configuration property.
     *
     * @return empty content as default
     */
    String name() default "";

    /**
     * The type of the configuration property.
     *
     * @reurn the default value is {@link String}
     */
    Class<?> type() default String.class;

    /**
     * The default value of the configuration property.
     *
     * @return empty content as default
     */
    String defaultValue() default "";

    /**
     * Whether the configuration property is required.
     *
     * @return true if required, otherwise false
     */
    boolean required() default false;

    /**
     * The description of the configuration property.
     *
     * @return empty content as default
     */
    String description() default "";

    /**
     * The source of the configuration property.
     *
     * @return empty array as default
     */
    String[] source() default {};

    /**
     * The source constants of the configuration property.
     */
    interface Sources {

        /**
         * JDK System Properties
         */
        String SYSTEM_PROPERTIES = "system-properties";

        /**
         * OS Environment Variables
         */
        String ENVIRONMENT_VARIABLES = "environment-variables";

        /**
         * Application
         */
        String APPLICATION = "application";
    }
}
