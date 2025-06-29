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
package io.microsphere.io.filter;

import java.io.File;

/**
 * This filter accepts {@link File} objects that are directories.
 *
 * <h3>Example Usage</h3>
 * <h4>Example 1 - Using the Singleton Instance</h4>
 * <pre>{@code
 * File dir = new File(".");
 * File[] files = dir.listFiles(DirectoryFileFilter.INSTANCE);
 * }</pre>
 *
 * <h4>Example 2 - Using the Class Directly</h4>
 * <pre>{@code
 * File dir = new File(".");
 * DirectoryFileFilter filter = new DirectoryFileFilter();
 * File[] files = dir.listFiles(filter);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see IOFileFilter
 * @since 1.0.0
 */
public class DirectoryFileFilter implements IOFileFilter {

    /**
     * Singleton instance of {@link DirectoryFileFilter}.
     */
    public static final DirectoryFileFilter INSTANCE = new DirectoryFileFilter();

    protected DirectoryFileFilter() {
    }

    @Override
    public boolean accept(File file) {
        return file != null && file.isDirectory();
    }
}
