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

package io.microsphere.internal.access;

import io.microsphere.annotation.Nonnull;
import io.microsphere.util.Utils;

import static io.microsphere.util.ClassLoaderUtils.loadClass;
import static io.microsphere.util.ObjectUtils.defaultIfNull;
import static java.lang.ClassLoader.getSystemClassLoader;

/**
 * The utility class for {@link sun.misc.JavaLangAccess} or {@link jdk.internal.access.JavaLangAccess}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see sun.misc.JavaLangAccess
 * @see jdk.internal.access.JavaLangAccess
 * @since 1.0.0
 */
public abstract class JavaLangAccessUtils implements Utils {

    /**
     * The class name of {@link sun.misc.JavaLangAccess}
     */
    public static final String LEGACY_JAVA_LANG_ACCESS_CLASS_NAME = "sun.misc.JavaLangAccess";

    /**
     * The class name of {@link jdk.internal.access.JavaLangAccess}
     */
    public static final String JAVA_LANG_ACCESS_CLASS_NAME = "jdk.internal.access.JavaLangAccess";

    /**
     * The {@link Class} of {@link sun.misc.JavaLangAccess} or {@link jdk.internal.access.JavaLangAccess}
     */
    @Nonnull
    public static final Class<?> JAVA_LANG_ACCESS_CLASS = loadJavaLangAccessClass();

    private static Class<?> loadJavaLangAccessClass() {
        ClassLoader classLoader = getSystemClassLoader();
        Class<?> javaLangAccessClass = loadClass(classLoader, LEGACY_JAVA_LANG_ACCESS_CLASS_NAME);
        return defaultIfNull(javaLangAccessClass, () -> loadClass(classLoader, JAVA_LANG_ACCESS_CLASS_NAME));
    }

    private JavaLangAccessUtils() {
    }
}
