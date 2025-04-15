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

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;

/**
 * Abstract {@link ArtifactResourceResolver}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ArtifactResourceResolver
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
