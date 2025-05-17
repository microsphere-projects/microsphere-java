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

import io.microsphere.logging.Logger;

import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static io.microsphere.collection.CollectionUtils.addAll;
import static io.microsphere.collection.CollectionUtils.first;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.constants.FileConstants.JAVA_EXTENSION;
import static io.microsphere.constants.ProtocolConstants.FILE_PROTOCOL;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.io.scanner.SimpleFileScanner.INSTANCE;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.StringUtils.substringBefore;
import static java.io.File.separatorChar;
import static java.util.Collections.singleton;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.tools.StandardLocation.SOURCE_OUTPUT;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

/**
 * The Java Compiler
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class Compiler {

    private static final Logger logger = getLogger(Compiler.class);

    private final Set<File> sourcePaths;

    private final JavaCompiler javaCompiler;

    private final StandardJavaFileManager javaFileManager;

    private final Set<Processor> processors = new LinkedHashSet<>();

    public Compiler() throws IOException {
        this(defaultTargetDirectory());
    }

    public Compiler(File targetDirectory) throws IOException {
        this(defaultSourceDirectory(), targetDirectory);
    }

    public Compiler(File defaultSourceDirectory, File targetDirectory) throws IOException {
        this.sourcePaths = newLinkedHashSet(defaultSourceDirectory);
        this.javaCompiler = getSystemJavaCompiler();
        this.javaFileManager = javaCompiler.getStandardFileManager(null, null, null);
        this.javaFileManager.setLocation(CLASS_OUTPUT, singleton(targetDirectory));
        this.javaFileManager.setLocation(SOURCE_OUTPUT, singleton(targetDirectory));
    }

    public Compiler sourcePaths(File... sourcePaths) {
        addAll(this.sourcePaths, sourcePaths);
        return this;
    }

    public Compiler sourcePaths(Iterable<Class<?>> sourceClasses) {
        for (Class<?> sourceClass : sourceClasses) {
            sourcePath(sourceClass);
        }
        return this;
    }

    public Compiler sourcePath(Class<?> sourceClass) {
        File sourcePath = detectSourcePath(sourceClass);
        if (sourcePath != null) {
            return sourcePaths(sourcePath);
        }
        return this;
    }

    static File defaultSourceDirectory() {
        return detectSourcePath(Compiler.class);
    }

    static File defaultRootDirectory() {
        return detectRootDirectory(Compiler.class);
    }

    static File defaultTargetDirectory() {
        File dir = new File(defaultRootDirectory(), "target/generated-classes");
        dir.mkdirs();
        return dir;
    }

    static File detectSourcePath(Class<?> sourceClass) {
        File rootDirectory = detectRootDirectory(sourceClass);
        String javaSourceFileRelativePath = resolveJavaSourceFileRelativePath(sourceClass);

        Set<File> sourceFiles = INSTANCE.scan(rootDirectory, true,
                file -> file.getAbsolutePath().endsWith(javaSourceFileRelativePath));
        if (sourceFiles.isEmpty()) {
            if (logger.isTraceEnabled()) {
                logger.trace("The source files of class[name : '{}'] can't be found in the root directory[path :'{}']",
                        getTypeName(sourceClass), rootDirectory.getAbsolutePath());
            }
            return null;
        }

        File sourceFile = first(sourceFiles);
        String javaSourceFilePath = sourceFile.getAbsolutePath();
        String javaSourcePath = substringBefore(javaSourceFilePath, javaSourceFileRelativePath);
        File sourcePath = new File(javaSourcePath);

        if (logger.isTraceEnabled()) {
            logger.trace("The source file[path : '{}] of class[name : '{}'] was found in the source directory[path :'{}']",
                    sourceFile.getAbsolutePath(), getTypeName(sourceClass), sourcePath.getAbsolutePath());
        }

        return sourcePath.exists() ? sourcePath : null;
    }

    static File detectRootDirectory(Class<?> sourceClass) {
        File classPath = detectClassPath(sourceClass);
        // classPath : "${rootDirectory}/target/classes"
        File rootDirectory = classPath.getParentFile().getParentFile();
        if (logger.isTraceEnabled()) {
            logger.trace("The root directory[path : '{}'] was found by the source class[name : '{}']",
                    rootDirectory.getAbsolutePath(), getTypeName(sourceClass));
        }
        return rootDirectory;
    }

    static File detectClassPath(Class<?> sourceClass) {
        URL classFileURL = sourceClass.getProtectionDomain().getCodeSource().getLocation();
        if (FILE_PROTOCOL.equals(classFileURL.getProtocol())) {
            return new File(classFileURL.getPath());
        } else {
            throw new RuntimeException("No support");
        }
    }

    public Compiler processors(Processor... processors) {
        addAll(this.processors, processors);
        return this;
    }

    private Iterable<? extends JavaFileObject> getJavaFileObjects(Class<?>... sourceClasses) {
        int size = sourceClasses == null ? 0 : sourceClasses.length;
        List<File> javaSourceFiles = newArrayList(size);
        for (int i = 0; i < size; i++) {
            File javaSourceFile = searchJavaSourceFile(sourceClasses[i]);
            if (javaSourceFile != null) {
                javaSourceFiles.add(javaSourceFile);
            }
        }
        return javaFileManager.getJavaFileObjects(javaSourceFiles.toArray(new File[0]));
    }

    private File searchJavaSourceFile(Class<?> sourceClass) {
        String javaSourceFilePath = resolveJavaSourceFileRelativePath(sourceClass);
        for (File sourceDirectory : sourcePaths) {
            File javaSourceFile = new File(sourceDirectory, javaSourceFilePath);
            if (javaSourceFile.exists()) {
                return javaSourceFile;
            }
        }
        return null;
    }

    static String resolveJavaSourceFileRelativePath(Class<?> sourceClass) {
        return sourceClass.getName().replace(DOT_CHAR, separatorChar).concat(JAVA_EXTENSION);
    }

    public boolean compile(Class<?>... sourceClasses) {
        CompilationTask task = javaCompiler.getTask(null, this.javaFileManager, null,
                ofList("-parameters", "-Xlint:unchecked", "-nowarn", "-Xlint:deprecation"),
//                null,
                null, getJavaFileObjects(sourceClasses));
        if (!processors.isEmpty()) {
            task.setProcessors(processors);
        }
        return task.call();
    }

    public JavaCompiler getJavaCompiler() {
        return javaCompiler;
    }
}
