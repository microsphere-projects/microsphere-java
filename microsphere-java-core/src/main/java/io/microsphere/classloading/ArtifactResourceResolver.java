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
import io.microsphere.lang.Prioritized;

import java.net.URL;

/**
 * {@code ArtifactResourceResolver} interface is responsible for resolving resources related to an
 * {@link Artifact}. Implementations of this interface are expected to handle the resolution of
 * artifact resources from a given URL, such as archive files or directories.
 *
 * <p>Implementations should define how the specific resource (e.g., JAR file, exploded directory)
 * should be interpreted and converted into an appropriate {@link Artifact} instance.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class MyArtifactResourceResolver implements ArtifactResourceResolver {
 *     public Artifact resolve(URL resourceURL) {
 *         // Implementation logic to convert the URL to an Artifact
 *         return new DefaultArtifact(resourceURL);
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Artifact
 * @see Prioritized
 * @see StreamArtifactResourceResolver
 * @see ManifestArtifactResourceResolver
 * @see MavenArtifactResourceResolver
 */
public interface ArtifactResourceResolver extends Prioritized {

    /**
     * Resolve an instance {@link Artifact} from {@link URL the resource} of artifact.
     *
     * @param resourceURL {@link URL the resource} of artifact, it may the archive file or the directory
     * @return an instance {@link Artifact} if found, otherwise <code>null</code>
     */
    @Nullable
    Artifact resolve(@Nullable URL resourceURL);
}
