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
 * Default {@link ArtifactResourceResolver} based on the archive file
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
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
