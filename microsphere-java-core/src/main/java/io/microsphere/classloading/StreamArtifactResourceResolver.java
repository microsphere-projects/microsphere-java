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
import static io.microsphere.net.URLUtils.resolveArchiveFile;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.jar.JarUtils.filter;

/**
 * Abstract class of {@link ArtifactResourceResolver} based on Stream
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ArtifactResourceResolver
 * @see AbstractArtifactResourceResolver
 * @since 1.0.0
 */
public abstract class StreamArtifactResourceResolver extends AbstractArtifactResourceResolver {

    public StreamArtifactResourceResolver(int priority) {
        super(priority);
    }

    public StreamArtifactResourceResolver(ClassLoader classLoader, int priority) {
        super(classLoader, priority);
        assertNotNull(classLoader, () -> "The 'classLoader' must not be null");
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
            if (artifactMetadataData != null) {
                artifact = resolve(resourceURL, artifactMetadataData, classLoader);
            }
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
    protected InputStream readArtifactMetadataDataFromResource(URL resourceURL, ClassLoader classLoader) throws IOException {
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
        JarEntry jarEntry = findArtifactMetadataEntry(jarFile);
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
        File artifactMetadataFile = findArtifactMetadata(directory);
        if (artifactMetadataFile == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("The artifact metadata file can't be found in the directory[path: '{}']", directory);
            }
            return null;
        }
        return new FileInputStream(artifactMetadataFile);
    }

    protected JarEntry findArtifactMetadataEntry(JarFile jarFile) throws IOException {
        List<JarEntry> entries = filter(jarFile, this::isArtifactMetadataEntry);
        return first(entries);
    }

    protected File findArtifactMetadata(File directory) throws IOException {
        Set<File> files = INSTANCE.scan(directory, true, file -> isArtifactMetadataFile(directory, file));
        return first(files);
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

    protected abstract Artifact resolve(URL resourceURL, InputStream artifactMetadataData, ClassLoader classLoader) throws IOException;
}
