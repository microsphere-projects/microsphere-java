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

import io.microsphere.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.net.URLUtils.resolveArchiveFile;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;

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
        Artifact artifact = null;
        try {
            artifact = doResolve(resourceURL, this.classLoader);
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("The Artifact can't be resolved from the resource URL : {}", resourceURL, e);
            }
        }
        return artifact;
    }

    protected abstract Artifact doResolve(URL resourceURL, ClassLoader classLoader) throws IOException;

    @Override
    public final int getPriority() {
        return priority;
    }

    protected URL resolveArtifactResourceURL(URL resourceURL) throws IOException {
        File archiveFile = resolveArchiveFile(resourceURL);
        if (archiveFile != null) {
            return archiveFile.toURI().toURL();
        }
        return null;
    }
}
