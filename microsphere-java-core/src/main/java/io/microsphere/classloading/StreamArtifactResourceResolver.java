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
 * An abstract base class for implementing {@link ArtifactResourceResolver} that provides a 
 * skeletal implementation to resolve artifact resources from either a streamable resource (like a URL)
 * or an archive file (like a JAR). This class handles common concerns such as:
 *
 * <ul>
 *     <li><b>Stream Handling:</b> Reads and processes artifact metadata from various sources including URLs, JAR files, and directories.</li>
 *     <li><b>Error Handling:</b> Provides consistent error logging when reading or resolving artifacts fails.</li>
 *     <li><b>Archive Support:</b> Offers utility methods to extract metadata from JARs or files within a directory structure.</li>
 *     <li><b>Extensibility:</b> Declares abstract methods that subclasses must implement to define custom logic for identifying 
 *         metadata locations and constructing artifact instances.</li>
 * </ul>
 *
 * <h3>Key Abstract Methods</h3>
 * <ul>
 *     <li>{@link #isArtifactMetadata(String)}: Determines if the given relative path represents an artifact metadata file.</li>
 *     <li>{@link #resolve(URL, InputStream, ClassLoader)}: Constructs an artifact from the provided metadata stream.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class CustomStreamArtifactResourceResolver extends StreamArtifactResourceResolver {
 *
 *     public CustomStreamArtifactResourceResolver(ClassLoader classLoader, int priority) {
 *         super(classLoader, priority);
 *     }
 *
 *     @Override
 *     protected boolean isArtifactMetadata(String relativePath) {
 *         return relativePath.equals("META-INF/custom-artifact.properties");
 *     }
 *
 *     @Override
 *     protected Artifact resolve(URL resourceURL, InputStream artifactMetadataData, ClassLoader classLoader) throws IOException {
 *         Properties properties = new Properties();
 *         properties.load(artifactMetadataData);
 *         return new DefaultArtifact(resourceURL, properties);
 *     }
 * }
 * }</pre>
 *
 * <p>In the example above:
 * <ul>
 *     <li>The constructor forwards the class loader and priority to the superclass.</li>
 *     <li>{@code isArtifactMetadata()} checks whether a given path matches the expected metadata location.</li>
 *     <li>{@code resolve()} parses the metadata stream into a custom artifact instance.</li>
 * </ul>
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

    protected abstract boolean isArtifactMetadata(String relativePath);

    protected abstract Artifact resolve(URL resourceURL, InputStream artifactMetadataData, ClassLoader classLoader) throws IOException;
}
