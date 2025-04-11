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
package io.microsphere.classloading;

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.microsphere.collection.CollectionUtils.first;
import static io.microsphere.collection.ListUtils.first;
import static io.microsphere.io.FileUtils.resolveRelativePath;
import static io.microsphere.io.IOUtils.close;
import static io.microsphere.io.scanner.SimpleFileScanner.INSTANCE;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.net.URLUtils.resolveArchiveFile;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.jar.JarUtils.filter;

/**
 * Abstract {@link ArtifactResourceResolver} class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ArtifactResourceResolver
 * @since 1.0.0
 */
public abstract class AbstractArtifactResourceResolver implements ArtifactResourceResolver {

    protected final Logger logger = getLogger(getClass());

    protected final ClassLoader classLoader;

    protected final int priority;

    protected AbstractArtifactResourceResolver(int priority) {
        this(getDefaultClassLoader(), priority);
    }

    protected AbstractArtifactResourceResolver(ClassLoader classLoader, int priority) {
        assertNotNull(classLoader, () -> "The 'classLoader' must not be null");
        this.classLoader = classLoader;
        this.priority = priority;
    }

    @Override
    public final Artifact resolve(URL resourceURL) {
        if (resourceURL == null) {
            return null;
        }

        File archiveFile = resolveArchiveFile(resourceURL);
        InputStream artifactMetadataData = null;
        Artifact artifact = null;
        try {
            if (archiveFile == null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("The resourceURL['{}'] can't be resolved to be an archive file", resourceURL);
                }
                artifactMetadataData = readArtifactMetadataDataFromResource(resourceURL, classLoader);
            } else {
                artifactMetadataData = readArtifactMetadataDataFromArchiveFile(archiveFile);
            }
            artifact = resolve(resourceURL, artifactMetadataData, classLoader);

        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("The Artifact can't be resolved from the resource URL : {}", resourceURL, e);
            }
        } finally {
            // close the InputStream
            close(artifactMetadataData);
        }
        return artifact;
    }

    @Nullable
    protected InputStream readArtifactMetadataDataFromResource(URL resourceURL, ClassLoader classLoader) {
        return null;
    }

    @Nullable
    protected InputStream readArtifactMetadataDataFromArchiveFile(File archiveFile) throws IOException {
        InputStream artifactMetadataData = null;
        if (archiveFile.isFile()) {
            artifactMetadataData = readArtifactMetadataDataFromFile(archiveFile);
        } else if (archiveFile.isDirectory()) {
            artifactMetadataData = readArtifactMetadataDataFromDirectory(archiveFile);
        }
        return artifactMetadataData;
    }

    @Nullable
    protected InputStream readArtifactMetadataDataFromFile(File archiveFile) throws IOException {
        JarFile jarFile = new JarFile(archiveFile);
        JarEntry jarEntry = resolveArtifactMetadataEntry(jarFile);
        if (jarEntry == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("The artifact metadata entry can't be resolved from the JarFile[path: '{}']", archiveFile);
            }
            return null;
        }
        return jarFile.getInputStream(jarEntry);
    }

    @Nullable
    protected InputStream readArtifactMetadataDataFromDirectory(File directory) throws IOException {
        Set<File> files = INSTANCE.scan(directory, true, file -> isArtifactMetadataFile(directory, file));
        File artifactMetadataFile = first(files);
        if (artifactMetadataFile == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("The artifact metadata file can't be found in the directory[path: '{}']", directory);
            }
            return null;
        }
        return new FileInputStream(artifactMetadataFile);
    }

    protected JarEntry resolveArtifactMetadataEntry(JarFile jarFile) throws IOException {
        List<JarEntry> entries = filter(jarFile, this::isArtifactMetadataEntry);
        return first(entries);
    }

    protected boolean isArtifactMetadataEntry(JarEntry jarEntry) {
        return isArtifactMetadata(jarEntry.getName());
    }

    protected boolean isArtifactMetadataFile(File directory, File file) {
        String path = resolveRelativePath(directory, file);
        return isArtifactMetadata(path);
    }

    protected boolean isArtifactMetadata(String relativePath) {
        return false;
    }

    protected abstract Artifact resolve(URL resourceURL, @Nullable InputStream artifactMetadataData, ClassLoader classLoader) throws IOException;

    @Override
    public final int getPriority() {
        return priority;
    }
}
