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

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;

import java.util.Map;
import java.util.Properties;

import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.StringUtils.startsWith;
import static java.lang.System.getProperties;
import static java.lang.System.getProperty;

/**
 * The utilities class for {@link System}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see System
 * @since 1.0.0
 */
public abstract class SystemUtils implements Utils {

    private static final Logger logger = getLogger(SystemUtils.class);


    public static final String OS_NAME_WINDOWS_PREFIX = "Windows";

    /**
     * The System property key for the Java class path : "java.class.path"
     */
    public static final String JAVA_CLASS_PATH_PROPERTY_KEY = "java.class.path";

    /**
     * The System property key for the User's account name : "user.name"
     */
    public static final String USER_NAME_PROPERTY_KEY = "user.name";

    /**
     * The System property key for the Path of extension directory or directories Deprecated.
     * This property, and the mechanism which implements it, may be removed in a future release : "java.ext.dirs"
     *
     * @deprecated
     */
    @Deprecated
    public static final String JAVA_EXT_DIRS_PROPERTY_KEY = "java.ext.dirs";

    /**
     * The System property key for the Java Runtime Environment vendor : "java.vendor"
     */
    public static final String JAVA_VENDOR_PROPERTY_KEY = "java.vendor";

    /**
     * The System property key for the Java Runtime Environment specification version : "java.specification.version"
     */
    public static final String JAVA_SPECIFICATION_VERSION_PROPERTY_KEY = "java.specification.version";

    /**
     * The System property key for the Line separator ("\n" on UNIX) : "line.separator"
     */
    public static final String LINE_SEPARATOR_PROPERTY_KEY = "line.separator";

    /**
     * The System property key for the Java class format version number : "java.class.version"
     */
    public static final String JAVA_CLASS_VERSION_PROPERTY_KEY = "java.class.version";

    /**
     * The System property key for the Java Runtime Environment specification name : "java.specification.name"
     */
    public static final String JAVA_SPECIFICATION_NAME_PROPERTY_KEY = "java.specification.name";

    /**
     * The System property key for the Java vendor URL : "java.vendor.url"
     */
    public static final String JAVA_VENDOR_URL_PROPERTY_KEY = "java.vendor.url";

    /**
     * The System property key for the Java Virtual Machine implementation version : "java.vm.version"
     */
    public static final String JAVA_VM_VERSION_PROPERTY_KEY = "java.vm.version";

    /**
     * The System property key for the Operating system name : "os.name"
     */
    public static final String OS_NAME_PROPERTY_KEY = "os.name";

    /**
     * The System property key for the Operating system architecture : "os.arch"
     */
    public static final String OS_ARCH_PROPERTY_KEY = "os.arch";

    /**
     * The System property key for the Java installation directory : "java.home"
     */
    public static final String JAVA_HOME_PROPERTY_KEY = "java.home";

    /**
     * The System property key for the Operating system version : "os.version"
     */
    public static final String OS_VERSION_PROPERTY_KEY = "os.version";

    /**
     * The System property key for the Name of JIT compiler to use : "java.compiler"
     */
    public static final String JAVA_COMPILER_PROPERTY_KEY = "java.compiler";

    /**
     * The System property key for the Java Runtime Environment version : "java.version"
     */
    public static final String JAVA_VERSION_PROPERTY_KEY = "java.version";

    /**
     * The System property key for the Java Virtual Machine specification version : "java.vm.specification.version"
     */
    public static final String JAVA_VM_SPECIFICATION_VERSION_PROPERTY_KEY = "java.vm.specification.version";

    /**
     * The System property key for the User's current working directory : "user.dir"
     */
    public static final String USER_DIR_PROPERTY_KEY = "user.dir";

    /**
     * The System property key for the Java Runtime Environment specification vendor : "java.specification.vendor"
     */
    public static final String JAVA_SPECIFICATION_VENDOR_PROPERTY_KEY = "java.specification.vendor";

    /**
     * The System property key for the Java Virtual Machine specification name : "java.vm.specification.name"
     */
    public static final String JAVA_VM_SPECIFICATION_NAME_PROPERTY_KEY = "java.vm.specification.name";

    /**
     * The System property key for the Java Virtual Machine implementation vendor : "java.vm.vendor"
     */
    public static final String JAVA_VM_VENDOR_PROPERTY_KEY = "java.vm.vendor";

    /**
     * The System property key for the File separator ("/" on UNIX) : "file.separator"
     */
    public static final String FILE_SEPARATOR_PROPERTY_KEY = "file.separator";

    /**
     * The System property key for the Path separator (":" on UNIX) : "path.separator"
     */
    public static final String PATH_SEPARATOR_PROPERTY_KEY = "path.separator";

    /**
     * The System property key for the List of paths to search when loading libraries : "java.library.path"
     */
    public static final String JAVA_LIBRARY_PATH_PROPERTY_KEY = "java.library.path";

    /**
     * The System property key for the User's home directory : "user.home"
     */
    public static final String USER_HOME_PROPERTY_KEY = "user.home";

    /**
     * The System property key for the Java Virtual Machine implementation name : "java.vm.name"
     */
    public static final String JAVA_VM_NAME_PROPERTY_KEY = "java.vm.name";

    /**
     * The System property key for the Java Virtual Machine specification vendor : "java.vm.specification.vendor"
     */
    public static final String JAVA_VM_SPECIFICATION_VENDOR_PROPERTY_KEY = "java.vm.specification.vendor";

    /**
     * The System property key for the Default temp file path : "java.io.tmpdir"
     */
    public static final String JAVA_IO_TMPDIR_PROPERTY_KEY = "java.io.tmpdir";

    /**
     * The System property key for the file encoding : "file.encoding"
     */
    public static final String FILE_ENCODING_PROPERTY_KEY = "file.encoding";

    /**
     * The System property key for the native encoding : "native.encoding"
     */
    public static final String NATIVE_ENCODING_PROPERTY_KEY = "native.encoding";

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
     * The System property for the native encoding
     */
    public static final String NATIVE_ENCODING = getSystemProperty(NATIVE_ENCODING_PROPERTY_KEY);

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
     * Is {@code true} if this is Java 8
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     **/
    public static final boolean IS_JAVA_8 = matchesJavaVersion("1.8");

    /**
     * <p>
     * Is {@code true} if this is Java version 9.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     **/
    public static final boolean IS_JAVA_9 = matchesJavaVersion("9");

    /**
     * <p>
     * Is {@code true} if this is Java version 10.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_10 = matchesJavaVersion("10");

    /**
     * <p>
     * Is {@code true} if this is Java version 11.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_11 = matchesJavaVersion("11");

    /**
     * <p>
     * Is {@code true} if this is Java version 12.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_12 = matchesJavaVersion("12");

    /**
     * <p>
     * Is {@code true} if this is Java version 13.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_13 = matchesJavaVersion("13");

    /**
     * <p>
     * Is {@code true} if this is Java version 14.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_14 = matchesJavaVersion("14");

    /**
     * <p>
     * Is {@code true} if this is Java version 15.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_15 = matchesJavaVersion("15");

    /**
     * <p>
     * Is {@code true} if this is Java version 16.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_16 = matchesJavaVersion("16");

    /**
     * <p>
     * Is {@code true} if this is Java version 17.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_17 = matchesJavaVersion("17");

    /**
     * <p>
     * Is {@code true} if this is Java version 18.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_18 = matchesJavaVersion("18");

    /**
     * <p>
     * Is {@code true} if this is Java version 19.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_19 = matchesJavaVersion("19");

    /**
     * <p>
     * Is {@code true} if this is Java version 20.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_20 = matchesJavaVersion("20");

    /**
     * <p>
     * Is {@code true} if this is Java version 21.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_21 = matchesJavaVersion("21");

    /**
     * <p>
     * Is {@code true} if this is Java version 22.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_22 = matchesJavaVersion("22");

    /**
     * <p>
     * Is {@code true} if this is Java version 23.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_23 = matchesJavaVersion("23");

    /**
     * <p>
     * Is {@code true} if this is Java version 24.x
     * </p>
     * <p>
     * The field will return {@code false} if {@link #JAVA_VERSION} is {@code null}.
     * </p>
     */
    public static final boolean IS_JAVA_24 = matchesJavaVersion("24");

    /**
     * Is <code>true</code> if current Java version is Long Term Supported(LTS)
     */
    public static final boolean IS_LTS_JAVA_VERSION = IS_JAVA_8 || IS_JAVA_11 || IS_JAVA_17 || IS_JAVA_21;

    /**
     * The copy of {@link System#getProperties() JDK System Properties, reduces
     * the performance cost of {@link System#getProperty(String)}.
     */
    private static Map<String, String> systemPropertiesCopy;

    /**
     * <p>
     * Copies the current system properties into an internal map for later access.
     * </p>
     * <p>
     * This method is useful when you want to capture the system properties at a certain point in time
     * and access them later without querying the system again. It ensures that the properties are copied
     * in a thread-safe manner.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * SystemUtils.copySystemProperties();
     * }</pre>
     *
     * @see #getSystemProperty(String)
     * @see #getSystemProperty(String, String)
     */
    public static void copySystemProperties() {
        Properties properties = getProperties();
        synchronized (SystemUtils.class) {
            Map<String, String> copy = systemPropertiesCopy;
            if (copy == null) {
                copy = newHashMap(properties.size());
                systemPropertiesCopy = copy;
            }
            copy.putAll((Map) properties);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("The JDK System Properties has been copied : {}", systemPropertiesCopy);
        }
    }

    /**
     * <p>
     * Gets a system property with the given key, defaulting to {@code null} if the property cannot be read.
     * </p>
     * If a {@code SecurityException} is caught, the return value is {@code null} and a message is written to
     * {@code System.err}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String javaVersion = SystemUtils.getSystemProperty("java.version");
     * }</pre>
     *
     * @param key the name of the system property
     * @return the string value of the system property, or {@code null} if the property is not found or access is denied
     */
    @Nullable
    public static String getSystemProperty(String key) {
        return getSystemProperty(key, null);
    }

    /**
     * <p>
     * Gets a system property with the given key, defaulting to the provided {@code defaultValue} if the property cannot be read.
     * </p>
     *
     * <p>
     * If a {@link SecurityException} is caught while trying to access the system property, the method returns the specified
     * default value and logs a warning message using the logger associated with this class.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String customProperty = SystemUtils.getSystemProperty("my.custom.property", "default-value");
     * }</pre>
     *
     * @param key          the name of the system property
     * @param defaultValue the default value to return if the property is not found or access is denied
     * @return the string value of the system property, or the provided default value if the property is inaccessible or not found
     */
    @Nullable
    public static String getSystemProperty(String key, String defaultValue) {
        String value = getSystemPropertyFromCopy(key);
        return value == null ? getProperty(key, defaultValue) : value;
    }

    /**
     * Reset the {@link #systemPropertiesCopy} to be <code>null</code>,
     * mainly used for testing
     */
    protected static void resetSystemPropertiesCopy() {
        if (logger.isTraceEnabled()) {
            logger.trace("The copy of JDK System Properties has been reset!");
        }
        systemPropertiesCopy = null;
    }

    protected static String getSystemPropertyFromCopy(String key) {
        return systemPropertiesCopy == null ? null : systemPropertiesCopy.get(key);
    }

    private static boolean matchesJavaVersion(final String versionPrefix) {
        return startsWith(JAVA_SPECIFICATION_VERSION, versionPrefix);
    }

    private SystemUtils() {
    }
}
