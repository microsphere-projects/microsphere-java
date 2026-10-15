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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;

import java.io.File;

import static io.microsphere.util.Assert.assertNotEmpty;

/**
 * Filters filenames for a certain name.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see IOFileFilter
 * @since 1.0.0
 */
public class NameFileFilter implements IOFileFilter {

    private final String name;

    private final boolean caseSensitive;

    public NameFileFilter(@Nonnull String name) {
        this(name, true);
    }

    public NameFileFilter(@Nonnull String name, boolean caseSensitive) {
        assertNotEmpty(name, () -> "The 'name' argument must not be empty!");
        this.name = name;
        this.caseSensitive = caseSensitive;
    }

    @Override
    public boolean accept(@Nullable File file) {
        if (file == null) {
            return false;
        }
        String fileName = file.getName();
        String name = this.name;
        return caseSensitive ? fileName.equals(name) : fileName.equalsIgnoreCase(name);
    }
}
