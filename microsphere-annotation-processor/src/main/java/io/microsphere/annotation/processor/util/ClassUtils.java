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

import io.microsphere.util.Utils;

import javax.lang.model.type.TypeMirror;

import static io.microsphere.annotation.processor.util.TypeUtils.ofTypeElement;
import static io.microsphere.constants.SymbolConstants.DOLLAR_CHAR;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;

/**
 * The utilities class for {@link Class}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Class
 * @since 1.0.0
 */
public interface ClassUtils extends Utils {

    static String getClassName(TypeMirror type) {
        return ofTypeElement(type).getQualifiedName().toString();
    }

    static Class loadClass(TypeMirror type) {
        return loadClass(getClassName(type));
    }

    static Class loadClass(String className) {
        ClassLoader classLoader = getClassLoader(ClassUtils.class);
        Class<?> klass = resolveClass(className, classLoader);
        if (klass == null) {
            int index = className.lastIndexOf(DOT_CHAR);
            if (index > 0) {
                className = className.substring(0, index) + DOLLAR_CHAR + className.substring(index + 1);
            }
        }
        return resolveClass(className, classLoader);
    }
}
