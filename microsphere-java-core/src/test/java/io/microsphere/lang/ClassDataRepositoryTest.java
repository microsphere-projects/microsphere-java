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
package io.microsphere.lang;

import io.microsphere.AbstractTestCase;
import io.microsphere.reflect.ReflectionUtils;
import io.microsphere.util.VersionUtils;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import static io.microsphere.util.ClassPathUtils.getClassPaths;
import static io.microsphere.util.VersionUtils.CURRENT_JAVA_VERSION;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ClassDataRepository} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ClassDataRepository
 * @since 1.0.0
 */
class ClassDataRepositoryTest extends AbstractTestCase {

    private static final ClassDataRepository repository = ClassDataRepository.INSTANCE;

    @Test
    void testGetClassNamesInClassPath() {
        Set<String> classPaths = getClassPaths();
        for (String classPath : classPaths) {
            Set<String> classNames = repository.getClassNamesInClassPath(classPath, true);
            assertNotNull(classNames);
        }
    }

    @Test
    void testGetClassNamesInPackageWithName() {
        Set<String> packageNames = repository.getAllPackageNamesInClassPaths();
        for (String packageName : packageNames) {
            Set<String> classNames = repository.getClassNamesInPackage(packageName);
            assertFalse(classNames.isEmpty());
            assertThrows(UnsupportedOperationException.class, classNames::clear);
        }
    }

    @Test
    void testGetClassNamesInPackage() {
        Package pkg = ClassDataRepositoryTest.class.getPackage();
        Set<String> classNames = repository.getClassNamesInPackage(pkg);
        assertFalse(classNames.isEmpty());
        assertThrows(UnsupportedOperationException.class, classNames::clear);
    }


    @Test
    void testGetAllPackageNamesInClassPaths() {
        Set<String> packageNames = repository.getAllPackageNamesInClassPaths();
        assertFalse(packageNames.isEmpty());
        assertThrows(UnsupportedOperationException.class, packageNames::clear);
    }

    @Test
    void testFindClassPath() {
        String classPath = repository.findClassPath(ReflectionUtils.class);
        assertNotNull(classPath);

        classPath = repository.findClassPath(Nonnull.class);
        assertNotNull(classPath);
    }

    @Test
    void testGetAllClassNamesMapInClassPath() {
        Map<String, Set<String>> allClassNamesMapInClassPath = repository.getClassPathToClassNamesMap();
        assertFalse(allClassNamesMapInClassPath.isEmpty());
        assertThrows(UnsupportedOperationException.class, allClassNamesMapInClassPath::clear);

    }

    @Test
    void testGetAllClassNamesInClassPath() {
        Set<String> allClassNames = repository.getAllClassNamesInClassPaths();
        assertFalse(allClassNames.isEmpty());
        assertThrows(UnsupportedOperationException.class, allClassNames::clear);

    }

    @Test
    void testGetCodeSourceLocation() {
        URL codeSourceLocation = null;

        codeSourceLocation = repository.getCodeSourceLocation(ClassDataRepositoryTest.class);
        assertNotNull(codeSourceLocation);

        codeSourceLocation = repository.getCodeSourceLocation(Nonnull.class);
        assertNotNull(codeSourceLocation);

        codeSourceLocation = repository.getCodeSourceLocation(String.class);
        if (CURRENT_JAVA_VERSION.le(VersionUtils.JAVA_VERSION_8)) {
            assertNotNull(codeSourceLocation);
        } else {
            assertNull(codeSourceLocation);
        }
    }

}
