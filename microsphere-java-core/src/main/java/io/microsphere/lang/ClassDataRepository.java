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

import io.microsphere.collection.CollectionUtils;
import io.microsphere.util.ClassPathUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.util.ClassPathUtils.getBootstrapClassPaths;
import static io.microsphere.util.ClassPathUtils.getClassPaths;
import static io.microsphere.util.ClassUtils.findClassNamesInClassPath;
import static io.microsphere.util.ClassUtils.resolvePackageName;
import static io.microsphere.util.StringUtils.isNotBlank;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * The Class Data Repository
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ClassPathUtils
 * @since 1.0.0
 */
public class ClassDataRepository {

    /**
     * Singleton instance of {@link ClassDataRepository}
     */
    public static final ClassDataRepository INSTANCE = new ClassDataRepository();

    private final Map<String, Set<String>> classPathToClassNamesMap = initClassPathToClassNamesMap();

    private final Map<String, String> classNameToClassPathsMap = initClassNameToClassPathsMap();

    private final Map<String, Set<String>> packageNameToClassNamesMap = initPackageNameToClassNamesMap();

    private ClassDataRepository() {
    }

    /**
     * Get all package names in {@link ClassPathUtils#getClassPaths() class paths}
     *
     * @return all package names in class paths
     */
    @Nonnull
    public Set<String> getAllPackageNamesInClassPaths() {
        return packageNameToClassNamesMap.keySet();
    }

    /**
     * Find class path under specified class name
     *
     * @param type class
     * @return class path
     */
    @Nullable
    public String findClassPath(Class<?> type) {
        return findClassPath(type.getName());
    }

    /**
     * Find class path under specified class name
     *
     * @param className class name
     * @return class path
     */
    @Nullable
    public String findClassPath(String className) {
        return classNameToClassPathsMap.get(className);
    }

    /**
     * Gets class name {@link Set} under specified class path
     *
     * @param classPath class path
     * @param recursive is recursive on sub directories
     * @return non-null {@link Set}
     */
    @Nonnull
    public Set<String> getClassNamesInClassPath(String classPath, boolean recursive) {
        Set<String> classNames = classPathToClassNamesMap.get(classPath);
        if (isEmpty(classNames)) {
            classNames = findClassNamesInClassPath(classPath, recursive);
        }
        return classNames;
    }

    /**
     * Gets class name {@link Set} under specified package
     *
     * @param onePackage one package
     * @return non-null {@link Set}
     */
    @Nonnull
    public Set<String> getClassNamesInPackage(Package onePackage) {
        return getClassNamesInPackage(onePackage.getName());
    }

    /**
     * Gets class name {@link Set} under specified package name
     *
     * @param packageName package name
     * @return non-null {@link Set}
     */
    @Nonnull
    public Set<String> getClassNamesInPackage(String packageName) {
        Set<String> classNames = packageNameToClassNamesMap.get(packageName);
        return classNames == null ? emptySet() : classNames;
    }

    /**
     * The map of all class names in {@link ClassPathUtils#getClassPaths() class path} , the class path for one {@link
     * JarFile} or classes directory as key , the class names set as value
     *
     * @return Read-only
     */
    @Nonnull
    public Map<String, Set<String>> getClassPathToClassNamesMap() {
        return classPathToClassNamesMap;
    }

    /**
     * The set of all class names in {@link ClassPathUtils#getClassPaths() class path}
     *
     * @return Read-only
     */
    @Nonnull
    public Set<String> getAllClassNamesInClassPaths() {
        Set<String> allClassNames = new LinkedHashSet();
        for (Set<String> classNames : classPathToClassNamesMap.values()) {
            allClassNames.addAll(classNames);
        }
        return unmodifiableSet(allClassNames);
    }

    /**
     * Get {@link Class}'s code source location URL
     *
     * @param type
     * @return If , return <code>null</code>.
     * @throws NullPointerException If <code>type</code> is <code>null</code> , {@link NullPointerException} will be thrown.
     */
    public URL getCodeSourceLocation(Class<?> type) throws NullPointerException {

        URL codeSourceLocation = null;
        ClassLoader classLoader = type.getClassLoader();

        if (classLoader == null) { // Bootstrap ClassLoader or type is primitive or void
            String path = findClassPath(type);
            if (isNotBlank(path)) {
                try {
                    codeSourceLocation = new File(path).toURI().toURL();
                } catch (MalformedURLException ignored) {
                    codeSourceLocation = null;
                }
            }
        } else {
            ProtectionDomain protectionDomain = type.getProtectionDomain();
            CodeSource codeSource = protectionDomain == null ? null : protectionDomain.getCodeSource();
            codeSourceLocation = codeSource == null ? null : codeSource.getLocation();
        }
        return codeSourceLocation;
    }

    private Map<String, Set<String>> initClassPathToClassNamesMap() {
        Map<String, Set<String>> classPathToClassNamesMap = new LinkedHashMap<>();
        Set<String> classPaths = new LinkedHashSet<>();
        classPaths.addAll(getBootstrapClassPaths());
        classPaths.addAll(getClassPaths());
        for (String classPath : classPaths) {
            Set<String> classNames = findClassNamesInClassPath(classPath, true);
            classPathToClassNamesMap.put(classPath, classNames);
        }
        return unmodifiableMap(classPathToClassNamesMap);
    }

    private Map<String, String> initClassNameToClassPathsMap() {
        Map<String, String> classNameToClassPathsMap = new LinkedHashMap<>();

        for (Map.Entry<String, Set<String>> entry : classPathToClassNamesMap.entrySet()) {
            String classPath = entry.getKey();
            Set<String> classNames = entry.getValue();
            for (String className : classNames) {
                classNameToClassPathsMap.put(className, classPath);
            }
        }

        return unmodifiableMap(classNameToClassPathsMap);
    }

    private Map<String, Set<String>> initPackageNameToClassNamesMap() {
        Map<String, Set<String>> packageNameToClassNamesMap = new LinkedHashMap();
        for (Map.Entry<String, String> entry : classNameToClassPathsMap.entrySet()) {
            String className = entry.getKey();
            String packageName = resolvePackageName(className);
            Set<String> classNamesInPackage = packageNameToClassNamesMap.get(packageName);
            if (classNamesInPackage == null) {
                classNamesInPackage = new LinkedHashSet();
                packageNameToClassNamesMap.put(packageName, classNamesInPackage);
            }
            classNamesInPackage.add(className);
        }

        return unmodifiableMap(packageNameToClassNamesMap);
    }
}
