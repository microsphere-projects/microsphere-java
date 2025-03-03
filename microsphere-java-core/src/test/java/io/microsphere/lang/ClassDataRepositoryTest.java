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
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import static io.microsphere.util.ClassPathUtils.getClassPaths;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link ClassDataRepository} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ClassDataRepository
 * @since 1.0.0
 */
public class ClassDataRepositoryTest extends AbstractTestCase {

    private static final ClassDataRepository repository = ClassDataRepository.INSTANCE;

    @Test
    public void testGetClassNamesInClassPath() {
        Set<String> classPaths = getClassPaths();
        for (String classPath : classPaths) {
            Set<String> classNames = repository.getClassNamesInClassPath(classPath, true);
            assertNotNull(classNames);
        }
    }

    @Test
    public void testGetClassNamesInPackage() {
        Set<String> packageNames = repository.getAllPackageNamesInClassPaths();
        for (String packageName : packageNames) {
            assertFalse(repository.getClassNamesInPackage(packageName).isEmpty());
        }

        Package pkg = ClassDataRepositoryTest.class.getPackage();
        assertFalse(repository.getClassNamesInPackage(pkg).isEmpty());
    }

    @Test
    public void testGetAllPackageNamesInClassPaths() {
        Set<String> packageNames = repository.getAllPackageNamesInClassPaths();
        assertNotNull(packageNames);
    }

    @Test
    public void testFindClassPath() {
        String classPath = repository.findClassPath(ReflectionUtils.class);
        assertNotNull(classPath);

        classPath = repository.findClassPath(Nonnull.class);
        assertNotNull(classPath);
    }

    @Test
    public void testGetAllClassNamesMapInClassPath() {
        Map<String, Set<String>> allClassNamesMapInClassPath = repository.getClassPathToClassNamesMap();
        assertFalse(allClassNamesMapInClassPath.isEmpty());
    }

    @Test
    public void testGetAllClassNamesInClassPath() {
        Set<String> allClassNames = repository.getAllClassNamesInClassPaths();
        assertFalse(allClassNames.isEmpty());
    }

    @Test
    public void testGetCodeSourceLocation() throws IOException {
        URL codeSourceLocation = null;

        codeSourceLocation = repository.getCodeSourceLocation(ClassDataRepositoryTest.class);
        assertNotNull(codeSourceLocation);

        codeSourceLocation = repository.getCodeSourceLocation(Nonnull.class);
        assertNotNull(codeSourceLocation);

        codeSourceLocation = repository.getCodeSourceLocation(String.class);
        assertNull(codeSourceLocation);
    }

}
