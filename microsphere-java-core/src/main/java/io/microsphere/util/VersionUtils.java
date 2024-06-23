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
package io.microsphere.util;

import javax.lang.model.SourceVersion;

import static io.microsphere.util.SystemUtils.JAVA_VERSION;
import static io.microsphere.util.Version.of;
import static javax.lang.model.SourceVersion.latest;

/**
 * The utility class for version
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Version
 * @since 1.0.0
 */
public abstract class VersionUtils extends BaseUtils {

    /**
     * The latest {@link SourceVersion Java Release Version}
     */
    public static final SourceVersion LATEST_JAVA_VERSION = latest();

    /**
     * The {@link Version} instance for current Java Version
     */
    public static final Version CURRENT_JAVA_VERSION = of(JAVA_VERSION);

}
