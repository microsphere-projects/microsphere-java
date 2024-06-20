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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.microsphere.util.StringUtils.startsWith;

/**
 * The utilities class for {@link System}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see System
 * @since 1.0.0
 */
public abstract class SystemUtils extends BaseUtils {

    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);

    private static final String OS_NAME_WINDOWS_PREFIX = "Windows";

    /**
     * The System property key for the Java class path.
     */
    public static final String JAVA_CLASS_PATH_PROPERTY_KEY = "java.class.path";

    /**
     * The System property key for the User's account name.
     */
    public static final String USER_NAME_PROPERTY_KEY = "user.name";

    /**
     * The System property key for the Path of extension directory or directories Deprecated. This property, and the mechanism which implements it, may be removed in a future release..
     */
    public static final String JAVA_EXT_DIRS_PROPERTY_KEY = "java.ext.dirs";

    /**
     * The System property key for the Java Runtime Environment vendor.
     */
    public static final String JAVA_VENDOR_PROPERTY_KEY = "java.vendor";

    /**
     * The System property key for the Java Runtime Environment specification version.
     */
    public static final String JAVA_SPECIFICATION_VERSION_PROPERTY_KEY = "java.specification.version";

    /**
     * The System property key for the Line separator ("\n" on UNIX).
     */
    public static final String LINE_SEPARATOR_PROPERTY_KEY = "line.separator";

    /**
     * The System property key for the Java class format version number.
     */
    public static final String JAVA_CLASS_VERSION_PROPERTY_KEY = "java.class.version";

    /**
     * The System property key for the Java Runtime Environment specification name.
     */
    public static final String JAVA_SPECIFICATION_NAME_PROPERTY_KEY = "java.specification.name";

    /**
     * The System property key for the Java vendor URL.
     */
    public static final String JAVA_VENDOR_URL_PROPERTY_KEY = "java.vendor.url";

    /**
     * The System property key for the Java Virtual Machine implementation version.
     */
    public static final String JAVA_VM_VERSION_PROPERTY_KEY = "java.vm.version";

    /**
     * The System property key for the Operating system name.
     */
    public static final String OS_NAME_PROPERTY_KEY = "os.name";

    /**
     * The System property key for the Operating system architecture.
     */
    public static final String OS_ARCH_PROPERTY_KEY = "os.arch";

    /**
     * The System property key for the Java installation directory.
     */
    public static final String JAVA_HOME_PROPERTY_KEY = "java.home";

    /**
     * The System property key for the Operating system version.
     */
    public static final String OS_VERSION_PROPERTY_KEY = "os.version";

    /**
     * The System property key for the Name of JIT compiler to use.
     */
    public static final String JAVA_COMPILER_PROPERTY_KEY = "java.compiler";

    /**
     * The System property key for the Java Runtime Environment version.
     */
    public static final String JAVA_VERSION_PROPERTY_KEY = "java.version";

    /**
     * The System property key for the Java Virtual Machine specification version.
     */
    public static final String JAVA_VM_SPECIFICATION_VERSION_PROPERTY_KEY = "java.vm.specification.version";

    /**
     * The System property key for the User's current working directory.
     */
    public static final String USER_DIR_PROPERTY_KEY = "user.dir";

    /**
     * The System property key for the Java Runtime Environment specification vendor.
     */
    public static final String JAVA_SPECIFICATION_VENDOR_PROPERTY_KEY = "java.specification.vendor";

    /**
     * The System property key for the Java Virtual Machine specification name.
     */
    public static final String JAVA_VM_SPECIFICATION_NAME_PROPERTY_KEY = "java.vm.specification.name";

    /**
     * The System property key for the Java Virtual Machine implementation vendor.
     */
    public static final String JAVA_VM_VENDOR_PROPERTY_KEY = "java.vm.vendor";

    /**
     * The System property key for the File separator ("/" on UNIX).
     */
    public static final String FILE_SEPARATOR_PROPERTY_KEY = "file.separator";

    /**
     * The System property key for the Path separator (":" on UNIX).
     */
    public static final String PATH_SEPARATOR_PROPERTY_KEY = "path.separator";

    /**
     * The System property key for the List of paths to search when loading libraries.
     */
    public static final String JAVA_LIBRARY_PATH_PROPERTY_KEY = "java.library.path";

    /**
     * The System property key for the User's home directory.
     */
    public static final String USER_HOME_PROPERTY_KEY = "user.home";

    /**
     * The System property key for the Java Virtual Machine implementation name.
     */
    public static final String JAVA_VM_NAME_PROPERTY_KEY = "java.vm.name";

    /**
     * The System property key for the Java Virtual Machine specification vendor.
     */
    public static final String JAVA_VM_SPECIFICATION_VENDOR_PROPERTY_KEY = "java.vm.specification.vendor";

    /**
     * The System property key for the Default temp file path.
     */
    public static final String JAVA_IO_TMPDIR_PROPERTY_KEY = "java.io.tmpdir";

    /**
     * The System property key for the file encoding
     */
    public static final String FILE_ENCODING_PROPERTY_KEY = "file.encoding";

    /**
     * The System property for the Java class path.
     */
    public static final String JAVA_CLASS_PATH = getSystemProperty(JAVA_CLASS_PATH_PROPERTY_KEY);

    /**
     * The System property for the User's account name.
     */
    public static final String USER_NAME = getSystemProperty(USER_NAME_PROPERTY_KEY);

    /**
     * The System property for the Path of extension directory or directories Deprecated. This property, and the mechanism which implements it, may be removed in a future release..
     */
    public static final String JAVA_EXT_DIRS = getSystemProperty(JAVA_EXT_DIRS_PROPERTY_KEY);

    /**
     * The System property for the Java Runtime Environment vendor.
     */
    public static final String JAVA_VENDOR = getSystemProperty(JAVA_VENDOR_PROPERTY_KEY);

    /**
     * The System property for the Java Runtime Environment specification version.
     */
    public static final String JAVA_SPECIFICATION_VERSION = getSystemProperty(JAVA_SPECIFICATION_VERSION_PROPERTY_KEY);

    /**
     * The System property for the Line separator ("\n" on UNIX).
     */
    public static final String LINE_SEPARATOR = getSystemProperty(LINE_SEPARATOR_PROPERTY_KEY);

    /**
     * The System property for the Java class format version number.
     */
    public static final String JAVA_CLASS_VERSION = getSystemProperty(JAVA_CLASS_VERSION_PROPERTY_KEY);

    /**
     * The System property for the Java Runtime Environment specification name.
     */
    public static final String JAVA_SPECIFICATION_NAME = getSystemProperty(JAVA_SPECIFICATION_NAME_PROPERTY_KEY);

    /**
     * The System property for the Java vendor URL.
     */
    public static final String JAVA_VENDOR_URL = getSystemProperty(JAVA_VENDOR_URL_PROPERTY_KEY);

    /**
     * The System property for the Java Virtual Machine implementation version.
     */
    public static final String JAVA_VM_VERSION = getSystemProperty(JAVA_VM_VERSION_PROPERTY_KEY);

    /**
     * The System property for the Operating system name.
     */
    public static final String OS_NAME = getSystemProperty(OS_NAME_PROPERTY_KEY);

    /**
     * The System property for the Operating system architecture.
     */
    public static final String OS_ARCH = getSystemProperty(OS_ARCH_PROPERTY_KEY);

    /**
     * The System property for the Java installation directory.
     */
    public static final String JAVA_HOME = getSystemProperty(JAVA_HOME_PROPERTY_KEY);

    /**
     * The System property for the Operating system version.
     */
    public static final String OS_VERSION = getSystemProperty(OS_VERSION_PROPERTY_KEY);

    /**
     * The System property for the Name of JIT compiler to use.
     */
    public static final String JAVA_COMPILER = getSystemProperty(JAVA_COMPILER_PROPERTY_KEY);

    /**
     * The System property for the Java Runtime Environment version.
     */
    public static final String JAVA_VERSION = getSystemProperty(JAVA_VERSION_PROPERTY_KEY);

    /**
     * The System property for the Java Virtual Machine specification version.
     */
    public static final String JAVA_VM_SPECIFICATION_VERSION = getSystemProperty(JAVA_VM_SPECIFICATION_VERSION_PROPERTY_KEY);

    /**
     * The System property for the User's current working directory.
     */
    public static final String USER_DIR = getSystemProperty(USER_DIR_PROPERTY_KEY);

    /**
     * The System property for the Java Runtime Environment specification vendor.
     */
    public static final String JAVA_SPECIFICATION_VENDOR = getSystemProperty(JAVA_SPECIFICATION_VENDOR_PROPERTY_KEY);

    /**
     * The System property for the Java Virtual Machine specification name.
     */
    public static final String JAVA_VM_SPECIFICATION_NAME = getSystemProperty(JAVA_VM_SPECIFICATION_NAME_PROPERTY_KEY);

    /**
     * The System property for the Java Virtual Machine implementation vendor.
     */
    public static final String JAVA_VM_VENDOR = getSystemProperty(JAVA_VM_VENDOR_PROPERTY_KEY);

    /**
     * The System property for the File separator ("/" on UNIX).
     */
    public static final String FILE_SEPARATOR = getSystemProperty(FILE_SEPARATOR_PROPERTY_KEY);

    /**
     * The System property for the Path separator (":" on UNIX).
     */
    public static final String PATH_SEPARATOR = getSystemProperty(PATH_SEPARATOR_PROPERTY_KEY);

    /**
     * The System property for the List of paths to search when loading libraries.
     */
    public static final String JAVA_LIBRARY_PATH = getSystemProperty(JAVA_LIBRARY_PATH_PROPERTY_KEY);

    /**
     * The System property for the User's home directory.
     */
    public static final String USER_HOME = getSystemProperty(USER_HOME_PROPERTY_KEY);

    /**
     * The System property for the Java Virtual Machine implementation name.
     */
    public static final String JAVA_VM_NAME = getSystemProperty(JAVA_VM_NAME_PROPERTY_KEY);

    /**
     * The System property for the Java Virtual Machine specification vendor.
     */
    public static final String JAVA_VM_SPECIFICATION_VENDOR = getSystemProperty(JAVA_VM_SPECIFICATION_VENDOR_PROPERTY_KEY);

    /**
     * The System property for the Default temp file path.
     */
    public static final String JAVA_IO_TMPDIR = getSystemProperty(JAVA_IO_TMPDIR_PROPERTY_KEY);

    /**
     * The System property for the file encoding, the default is "UTF-8"
     */
    public static final String FILE_ENCODING = getSystemProperty(FILE_ENCODING_PROPERTY_KEY, "UTF-8");

    /**
     * <p>
     * Is {@code true} if this is Windows.
     * </p>
     * <p>
     * The field will return {@code false} if {@code OS_NAME} is {@code null}.
     * </p>
     */
    public static final boolean IS_OS_WINDOWS = startsWith(OS_NAME, OS_NAME_WINDOWS_PREFIX);

    /**
     * <p>
     * Gets a System property, defaulting to {@code null} if the property cannot be read.
     * </p>
     * <p>
     * If a {@code SecurityException} is caught, the return value is {@code null} and a message is written to
     * {@code System.err}.
     * </p>
     *
     * @param key the property key
     * @return the system property value or {@code null} if a security problem occurs
     */
    public static String getSystemProperty(String key) {
        return getSystemProperty(key, null);
    }

    /**
     * <p>
     * Gets a System property, <code>defaultValue</code> if the property cannot be read.
     * </p>
     * <p>
     * If a {@code SecurityException} is caught, the return value is <code>defaultValue</code> and a message is written
     * to {@code System.err}.
     * </p>
     *
     * @param key          the property key
     * @param defaultValue the default value of property
     * @return the system property value or <code>defaultValue</code> if a security problem occurs
     */
    public static String getSystemProperty(String key, String defaultValue) {
        try {
            return System.getProperty(key, defaultValue);
        } catch (final SecurityException ex) {
            logger.error("Caught a SecurityException reading the system property '{}'; " +
                    "the SystemUtils property value will be : '{}'", key, defaultValue);
            return defaultValue;
        }
    }
}
