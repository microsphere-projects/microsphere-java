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

package io.microsphere.annotation.processor.annotation;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.annotation.Since;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.DAYS;

/**
 * The {@link Annotation} for testing
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Annotation
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface TestAnnotation {

    boolean z() default false;

    char c() default 'a';

    byte b() default 1;

    short s() default 2;

    int i() default 3;

    long l() default 4L;

    float f() default 5.0f;

    double d() default 6.0d;

    String string() default "string";

    Class<?> type() default String.class;

    Class<?>[] types() default {String.class, Integer.class};

    TimeUnit timeUnit() default DAYS;

    Since since();

    ConfigurationProperty[] properties() default {};

}
