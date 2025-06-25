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

import io.microsphere.lang.Prioritized;
import io.microsphere.logging.Logger;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;

/**
 * An abstract base class for implementing {@link ArtifactResourceResolver}.
 *
 * <p>This class provides a skeletal implementation to reduce the effort required to create
 * concrete implementations. It handles common concerns such as priority management, logging,
 * and classloader usage, allowing subclasses to focus on the specific logic for resolving
 * artifact resources.</p>
 *
 * <h3>Key Features</h3>
 * <ul>
 *     <li><b>Priority Management:</b> Implements the {@link Prioritized} interface to allow sorting
 *         of resolvers based on their priorities.</li>
 *     <li><b>Logging Support:</b> Provides a pre-configured logger instance for subclasses to use.</li>
 *     <li><b>ClassLoader Handling:</b> Stores and exposes a ClassLoader instance that can be used by
 *         subclasses when resolving or processing artifacts.</li>
 * </ul>
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * public class CustomArtifactResourceResolver extends AbstractArtifactResourceResolver {
 *
 *     public CustomArtifactResourceResolver(ClassLoader classLoader, int priority) {
 *         super(classLoader, priority);
 *     }
 *
 *     @Override
 *     public Artifact resolve(@Nullable URL resourceURL) {
 *         if (resourceURL == null) {
 *             return null;
 *         }
 *
 *         getLog().info("Resolving artifact from URL: {}", resourceURL);
 *
 *         // Custom resolution logic here
 *         if (isSupported(resourceURL)) {
 *             return new DefaultArtifact(resourceURL);
 *         }
 *
 *         return null;
 *     }
 *
 *     private boolean isSupported(URL url) {
 *         // Determine if this resolver supports the given URL
 *         return url.getProtocol().equals("file");
 *     }
 * }
 * }</pre>
 *
 * <p>In the example above:
 * <ul>
 *     <li>The constructor takes a ClassLoader and a priority, passing them to the superclass.</li>
 *     <li>The overridden {@code resolve()} method uses the provided ClassLoader and logger,
 *         performs custom logic to determine if the URL should be resolved, and returns an
 *         appropriate artifact instance.</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ArtifactResourceResolver
 * @see Prioritized
 * @see Logger
 * @since 1.0.0
 */
public abstract class AbstractArtifactResourceResolver implements ArtifactResourceResolver {

    protected final Logger logger = getLogger(getClass());

    protected final ClassLoader classLoader;

    protected final int priority;

    public AbstractArtifactResourceResolver(int priority) {
        this(getDefaultClassLoader(), priority);
    }

    public AbstractArtifactResourceResolver(ClassLoader classLoader, int priority) {
        this.classLoader = classLoader;
        this.priority = priority;
    }

    @Override
    public final int getPriority() {
        return priority;
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append('{');
        sb.append("classLoader=").append(classLoader);
        sb.append(", priority=").append(priority);
        sb.append('}');
        return sb.toString();
    }
}
