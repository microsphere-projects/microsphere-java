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

import java.io.File;
import java.net.URL;

import static io.microsphere.classloading.Artifact.create;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.constants.SymbolConstants.HYPHEN;
import static io.microsphere.net.URLUtils.resolveArchiveFile;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.StringUtils.split;
import static io.microsphere.util.StringUtils.substringBeforeLast;

/**
 * A concrete implementation of {@link ArtifactResourceResolver} that resolves artifacts based on archive files.
 *
 * <p>{@code ArchiveFileArtifactResourceResolver} interprets the naming convention of archive files to extract
 * artifact metadata such as {@code artifactId} and {@code version}. The resolver expects file names to follow a
 * specific format: {@code [artifactId]-[version].[extension]}, where only the {@code artifactId} is mandatory.
 *
 * <h3>Resolution Logic</h3>
 * <ul>
 *     <li>If the given URL points to a directory or cannot be resolved to a valid archive file, resolution fails.</li>
 *     <li>The filename is parsed using hyphen ({@code '-'}) as the delimiter between artifact ID and version.</li>
 *     <li>If no hyphen is found, only the artifact ID will be extracted, and the version will be set to null.</li>
 * </ul>
 *
 * <h3>Example Usages</h3>
 * <pre>{@code
 * // Example 1: With both artifactId and version
 * URL url = new File("my-artifact-1.0.0.jar").toURI().toURL();
 * Artifact artifact = resolver.resolve(url);
 * System.out.println(artifact.getArtifactId()); // Outputs: "my-artifact"
 * System.out.println(artifact.getVersion());    // Outputs: "1.0.0"
 *
 * // Example 2: With only artifactId
 * URL url = new File("my-artifact.jar").toURI().toURL();
 * Artifact artifact = resolver.resolve(url);
 * System.out.println(artifact.getArtifactId()); // Outputs: "my-artifact"
 * System.out.println(artifact.getVersion());    // Outputs: null
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractArtifactResourceResolver
 * @see ArtifactResourceResolver
 * @since 1.0.0
 */
public class ArchiveFileArtifactResourceResolver extends AbstractArtifactResourceResolver {

    public static final int DEFAULT_PRIORITY = 9;

    public ArchiveFileArtifactResourceResolver() {
        this(DEFAULT_PRIORITY);
    }

    public ArchiveFileArtifactResourceResolver(int priority) {
        super(priority);
    }

    public ArchiveFileArtifactResourceResolver(ClassLoader classLoader, int priority) {
        super(classLoader, priority);
    }

    @Override
    public Artifact resolve(URL resourceURL) {
        if (resourceURL == null) {
            return null;
        }

        File archiveFile = resolveArchiveFile(resourceURL);
        if (archiveFile == null || archiveFile.isDirectory()) {
            return null;
        }

        String archiveFileName = archiveFile.getName();

        String fileNameWithoutExtension = substringBeforeLast(archiveFileName, DOT);

        String[] parts = split(fileNameWithoutExtension, HYPHEN);

        int length = length(parts);

        if (length < 1) {
            return null;
        }

        String artifactId = parts[0];
        String version = length > 1 ? parts[1] : null;
        return create(artifactId, version, resourceURL);
    }

}
