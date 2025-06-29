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

import io.microsphere.io.FileUtils;

import java.io.File;

import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.io.FileUtils.getFileExtension;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.SystemUtils.IS_OS_WINDOWS;

/**
 * A filter that matches files based on their extensions.
 *
 * <p>
 * This class implements the {@link IOFileFilter} interface and provides functionality to filter files
 * by checking if their extension matches a specified extension. The comparison is case-insensitive on Windows systems,
 * while it's case-sensitive on other operating systems.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Create a filter for ".txt" files
 * IOFileFilter txtFilter = FileExtensionFilter.of(".txt");
 *
 * // Use the filter to accept only ".txt" files
 * File dir = new File("/path/to/directory");
 * File[] txtFiles = dir.listFiles((FileFilter) txtFilter);
 * }</pre>
 *
 * <p>
 * The above example creates a filter that accepts only files ending with the ".txt" extension.
 * It then uses this filter to list all matching files in the specified directory.
 * </p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see IOFileFilter
 * @see FileUtils#getFileExtension(String)
 * @since 1.0.0
 */
public class FileExtensionFilter implements IOFileFilter {

    private final String extension;

    protected FileExtensionFilter(String extension) {
        int lastIndex = extension.lastIndexOf(DOT);
        this.extension = lastIndex > -1 ? extension.substring(lastIndex + 1) : extension;
    }

    @Override
    public boolean accept(File file) {
        if (file == null || file.isDirectory()) {
            return false;
        }

        String fileName = file.getName();
        String fileExtension = getFileExtension(fileName);
        if (isBlank(fileExtension)) {
            return false;
        }

        return IS_OS_WINDOWS ? fileExtension.equalsIgnoreCase(extension) : fileExtension.equals(extension);
    }

    /**
     * Creates an instance of {@link FileExtensionFilter} by the given file extension
     *
     * @param extension the file extension
     * @return non-null
     */
    public static FileExtensionFilter of(String extension) {
        return new FileExtensionFilter(extension);
    }
}
