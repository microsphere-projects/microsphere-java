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
package io.microsphere.jdk.tools.compiler;

import io.microsphere.logging.Logger;

import javax.annotation.processing.Processor;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import static io.microsphere.collection.CollectionUtils.addAll;
import static io.microsphere.collection.CollectionUtils.first;
import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.constants.FileConstants.JAVA_EXTENSION;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.io.scanner.SimpleFileScanner.INSTANCE;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.StringUtils.substringBefore;
import static java.io.File.separatorChar;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Stream.of;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.tools.StandardLocation.SOURCE_OUTPUT;
import static javax.tools.StandardLocation.SOURCE_PATH;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

/**
 * The Java Compiler
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class Compiler {

    private static final Logger logger = getLogger(Compiler.class);

    public static final String[] DEFAULT_OPTIONS = ofArray("-parameters", "-Xlint:unchecked", "-nowarn", "-Xlint:deprecation");

    private final Set<File> sourcePaths;

    private final File targetDirectory;

    private final JavaCompiler javaCompiler;

    private Set<Processor> processors;

    private List<String> options;

    private DiagnosticListener<? super JavaFileObject> diagnosticListener;

    private Locale locale;

    private Charset charset;

    public Compiler() {
        this(defaultTargetDirectory());
    }

    public Compiler(File targetDirectory) {
        this(defaultSourceDirectory(), targetDirectory);
    }

    public Compiler(File defaultSourceDirectory, File targetDirectory) {
        options(DEFAULT_OPTIONS);
        this.sourcePaths = newLinkedHashSet(defaultSourceDirectory);
        this.targetDirectory = targetDirectory;
        this.javaCompiler = getSystemJavaCompiler();
    }

    public Compiler options(String... options) {
        this.options = ofList(options);
        return this;
    }

    public Compiler sourcePaths(File... sourcePaths) {
        addAll(this.sourcePaths, sourcePaths);
        return this;
    }

    public Compiler sourcePaths(Class<?>... sourceClasses) {
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

    public Compiler processors(Processor... processors) {
        this.processors = ofSet(processors);
        return this;
    }

    public Compiler diagnosticListener(DiagnosticListener<? super JavaFileObject> diagnosticListener) {
        this.diagnosticListener = diagnosticListener;
        return this;
    }

    public Compiler locale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public Compiler charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public boolean compile(Class<?>... sourceClasses) throws IOException {
        JavaCompiler javaCompiler = getJavaCompiler();
        StandardJavaFileManager javaFileManager = getJavaFileManager();
        CompilationTask task = javaCompiler.getTask(null, javaFileManager,
                getDiagnosticListener(), getOptions(), null, getJavaFileObjects(javaFileManager, sourceClasses));
        task.setProcessors(this.getProcessors());
        return task.call();
    }

    public JavaCompiler getJavaCompiler() {
        return this.javaCompiler;
    }

    public StandardJavaFileManager getJavaFileManager() throws IOException {
        StandardJavaFileManager javaFileManager = getJavaCompiler().getStandardFileManager(getDiagnosticListener(), getLocale(), getCharset());
        javaFileManager.setLocation(SOURCE_PATH, this.sourcePaths);
        javaFileManager.setLocation(CLASS_OUTPUT, singleton(this.targetDirectory));
        javaFileManager.setLocation(SOURCE_OUTPUT, singleton(this.targetDirectory));
        return javaFileManager;
    }

    public DiagnosticListener<? super JavaFileObject> getDiagnosticListener() {
        return this.diagnosticListener;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public List<String> getOptions() {
        List<String> options = this.options;
        if (isEmpty(options)) {
            return emptyList();
        }
        return unmodifiableList(options);
    }

    public Set<Processor> getProcessors() {
        Set<Processor> processors = this.processors;
        if (processors == null) {
            return emptySet();
        }
        return unmodifiableSet(processors);
    }

    private Iterable<? extends JavaFileObject> getJavaFileObjects(StandardJavaFileManager javaFileManager, Class<?>... sourceClasses) {
        File[] javaFiles = of(sourceClasses)
                .map(this::searchJavaSourceFile)
                .filter(Objects::nonNull)
                .toArray(File[]::new);
        return javaFileManager.getJavaFileObjects(javaFiles);
    }

    private File searchJavaSourceFile(Class<?> sourceClass) {
        String javaSourceFilePath = resolveJavaSourceFileRelativePath(sourceClass);
        for (File sourceDirectory : this.sourcePaths) {
            File javaSourceFile = new File(sourceDirectory, javaSourceFilePath);
            if (javaSourceFile.exists()) {
                return javaSourceFile;
            }
        }
        return null;
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

    public static File detectSourcePath(Class<?> sourceClass) {
        File rootDirectory = detectRootDirectory(sourceClass);
        String javaSourceFileRelativePath = resolveJavaSourceFileRelativePath(sourceClass);

        Set<File> sourceFiles = INSTANCE.scan(rootDirectory, true,
                file -> file.getAbsolutePath().endsWith(javaSourceFileRelativePath));

        File sourceFile = first(sourceFiles);
        if (sourceFile == null) {
            logger.trace("The source files of {} can't be found in the root directory[path :'{}']", sourceClass, rootDirectory);
            return null;
        }

        String javaSourceFilePath = sourceFile.getAbsolutePath();
        String javaSourcePath = substringBefore(javaSourceFilePath, javaSourceFileRelativePath);
        File sourcePath = new File(javaSourcePath);

        logger.trace("The source file[path : '{}] of {} was found in the source directory[path :'{}']", sourceFile, sourceClass, sourcePath);

        return sourcePath;
    }

    public static File detectRootDirectory(Class<?> sourceClass) {
        File classPath = detectClassPath(sourceClass);
        // classPath : "${rootDirectory}/target/classes"
        File rootDirectory = classPath.getParentFile().getParentFile();
        logger.trace("The root directory[path : '{}'] was found by the source class[name : '{}']",
                rootDirectory.getAbsolutePath(), getTypeName(sourceClass));
        return rootDirectory;
    }

    public static File detectClassPath(Class<?> sourceClass) {
        ProtectionDomain protectionDomain = sourceClass.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        if (codeSource != null) {
            URL location = codeSource.getLocation();
            return new File(location.getPath());
        }
        String message = format("The source {} is based on the file system, the class path can't be detected.", sourceClass);
        throw new UnsupportedOperationException(message);
    }

    public static String resolveJavaSourceFileRelativePath(Class<?> sourceClass) {
        return sourceClass.getName().replace(DOT_CHAR, separatorChar).concat(JAVA_EXTENSION);
    }
}