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

import io.microsphere.filter.Filter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * An interface which brings the {@link FileFilter} and {@link FilenameFilter}
 * interfaces together.
 *
 * @see FileFilter
 * @see FilenameFilter
 * @see Filter
 * @since 1.0.0
 */
public interface IOFileFilter extends FileFilter, FilenameFilter {

    @Override
    boolean accept(File file);

    @Override
    default boolean accept(File dir, String name) {
        return accept(new File(dir, name));
    }
}
