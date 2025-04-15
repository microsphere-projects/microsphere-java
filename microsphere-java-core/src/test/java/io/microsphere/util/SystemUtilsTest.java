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

import io.microsphere.reflect.MemberUtils;
import org.junit.jupiter.api.Test;

import javax.lang.model.SourceVersion;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.util.StringUtils.substringAfter;
import static io.microsphere.util.SystemUtils.FILE_ENCODING;
import static io.microsphere.util.SystemUtils.FILE_ENCODING_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.FILE_SEPARATOR_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.IS_JAVA_11;
import static io.microsphere.util.SystemUtils.IS_JAVA_17;
import static io.microsphere.util.SystemUtils.IS_JAVA_21;
import static io.microsphere.util.SystemUtils.IS_JAVA_8;
import static io.microsphere.util.SystemUtils.IS_LTS_JAVA_VERSION;
import static io.microsphere.util.SystemUtils.JAVA_CLASS_PATH;
import static io.microsphere.util.SystemUtils.JAVA_CLASS_PATH_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_CLASS_VERSION;
import static io.microsphere.util.SystemUtils.JAVA_CLASS_VERSION_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_COMPILER;
import static io.microsphere.util.SystemUtils.JAVA_COMPILER_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_EXT_DIRS;
import static io.microsphere.util.SystemUtils.JAVA_EXT_DIRS_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_HOME;
import static io.microsphere.util.SystemUtils.JAVA_HOME_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_IO_TMPDIR;
import static io.microsphere.util.SystemUtils.JAVA_IO_TMPDIR_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_LIBRARY_PATH;
import static io.microsphere.util.SystemUtils.JAVA_LIBRARY_PATH_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_SPECIFICATION_NAME;
import static io.microsphere.util.SystemUtils.JAVA_SPECIFICATION_NAME_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_SPECIFICATION_VENDOR;
import static io.microsphere.util.SystemUtils.JAVA_SPECIFICATION_VENDOR_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_SPECIFICATION_VERSION;
import static io.microsphere.util.SystemUtils.JAVA_SPECIFICATION_VERSION_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_VENDOR;
import static io.microsphere.util.SystemUtils.JAVA_VENDOR_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_VENDOR_URL;
import static io.microsphere.util.SystemUtils.JAVA_VENDOR_URL_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_VERSION;
import static io.microsphere.util.SystemUtils.JAVA_VERSION_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_VM_NAME;
import static io.microsphere.util.SystemUtils.JAVA_VM_NAME_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_VM_SPECIFICATION_NAME;
import static io.microsphere.util.SystemUtils.JAVA_VM_SPECIFICATION_NAME_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_VM_SPECIFICATION_VENDOR;
import static io.microsphere.util.SystemUtils.JAVA_VM_SPECIFICATION_VENDOR_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_VM_SPECIFICATION_VERSION;
import static io.microsphere.util.SystemUtils.JAVA_VM_SPECIFICATION_VERSION_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_VM_VENDOR;
import static io.microsphere.util.SystemUtils.JAVA_VM_VENDOR_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.JAVA_VM_VERSION;
import static io.microsphere.util.SystemUtils.JAVA_VM_VERSION_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.LINE_SEPARATOR_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.NATIVE_ENCODING;
import static io.microsphere.util.SystemUtils.NATIVE_ENCODING_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.OS_ARCH;
import static io.microsphere.util.SystemUtils.OS_ARCH_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.OS_NAME;
import static io.microsphere.util.SystemUtils.OS_NAME_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.OS_VERSION;
import static io.microsphere.util.SystemUtils.OS_VERSION_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.PATH_SEPARATOR_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.USER_DIR;
import static io.microsphere.util.SystemUtils.USER_DIR_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.USER_HOME;
import static io.microsphere.util.SystemUtils.USER_HOME_PROPERTY_KEY;
import static io.microsphere.util.SystemUtils.USER_NAME;
import static io.microsphere.util.SystemUtils.USER_NAME_PROPERTY_KEY;
import static java.lang.System.getProperty;
import static javax.lang.model.SourceVersion.latest;
import static javax.lang.model.SourceVersion.values;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link SystemUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see 1.0.0
 * @since 1.0.0
 */
public class SystemUtilsTest {

    private static final Class<SystemUtils> CLASS = SystemUtils.class;

    private static final String IS_JAVA_VERSION_FIELD_NAME_PREFIX = "IS_JAVA_";

    private static final Field[] IS_JAVA_VERSION_FIELDS = findIsJavaVersionFields();

    private static final Map<Integer, String> versionedClassNames = ofMap(
            24, "java.lang.classfile.Annotation",
            23, "java.lang.runtime.ExactConversionsSupport",
            22, "java.lang.foreign.Arena",
            21, "java.util.SequencedSet",
            20, "java.lang.reflect.ClassFileFormatVersion",
            19, "java.util.concurrent.Future.State",
            18, "java.net.spi.InetAddressResolverProvider",
            17, "java.util.random.RandomGenerator",
            16, "java.lang.Record",
            15, "java.lang.invoke.MethodHandles.Lookup.ClassOption",
            14, "java.io.Serial",
            13, "com.sun.source.util.ParameterNameProvider",
            12, "java.lang.constant.Constable",
            11, "java.net.http.HttpClient",
            10, "",
            9, "java.lang.ProcessHandle",
            8, "java.util.concurrent.CompletableFuture"
    );

    private static final SourceVersion[] versions = values();

    private static Field[] findIsJavaVersionFields() {
        return Stream.of(CLASS.getFields())
                .filter(MemberUtils::isStatic)
                .filter(field -> field.getName().startsWith(IS_JAVA_VERSION_FIELD_NAME_PREFIX))
                .toArray(Field[]::new);
    }

    @Test
    public void testSystemPropertyKeys() {
        assertEquals("java.version", JAVA_VERSION_PROPERTY_KEY);
        assertEquals("java.vendor", JAVA_VENDOR_PROPERTY_KEY);
        assertEquals("java.vendor.url", JAVA_VENDOR_URL_PROPERTY_KEY);
        assertEquals("java.home", JAVA_HOME_PROPERTY_KEY);
        assertEquals("java.vm.specification.version", JAVA_VM_SPECIFICATION_VERSION_PROPERTY_KEY);
        assertEquals("java.vm.specification.vendor", JAVA_VM_SPECIFICATION_VENDOR_PROPERTY_KEY);
        assertEquals("java.vm.specification.name", JAVA_VM_SPECIFICATION_NAME_PROPERTY_KEY);
        assertEquals("java.vm.version", JAVA_VM_VERSION_PROPERTY_KEY);
        assertEquals("java.vm.vendor", JAVA_VM_VENDOR_PROPERTY_KEY);
        assertEquals("java.vm.name", JAVA_VM_NAME_PROPERTY_KEY);
        assertEquals("java.specification.version", JAVA_SPECIFICATION_VERSION_PROPERTY_KEY);
        assertEquals("java.specification.vendor", JAVA_SPECIFICATION_VENDOR_PROPERTY_KEY);
        assertEquals("java.specification.name", JAVA_SPECIFICATION_NAME_PROPERTY_KEY);
        assertEquals("java.class.version", JAVA_CLASS_VERSION_PROPERTY_KEY);
        assertEquals("java.class.path", JAVA_CLASS_PATH_PROPERTY_KEY);
        assertEquals("java.library.path", JAVA_LIBRARY_PATH_PROPERTY_KEY);
        assertEquals("java.io.tmpdir", JAVA_IO_TMPDIR_PROPERTY_KEY);
        assertEquals("java.compiler", JAVA_COMPILER_PROPERTY_KEY);
        assertEquals("java.ext.dirs", JAVA_EXT_DIRS_PROPERTY_KEY);
        assertEquals("os.name", OS_NAME_PROPERTY_KEY);
        assertEquals("os.arch", OS_ARCH_PROPERTY_KEY);
        assertEquals("os.version", OS_VERSION_PROPERTY_KEY);
        assertEquals("file.separator", FILE_SEPARATOR_PROPERTY_KEY);
        assertEquals("path.separator", PATH_SEPARATOR_PROPERTY_KEY);
        assertEquals("line.separator", LINE_SEPARATOR_PROPERTY_KEY);
        assertEquals("user.name", USER_NAME_PROPERTY_KEY);
        assertEquals("user.home", USER_HOME_PROPERTY_KEY);
        assertEquals("user.dir", USER_DIR_PROPERTY_KEY);
        assertEquals("file.encoding", FILE_ENCODING_PROPERTY_KEY);
        assertEquals("native.encoding", NATIVE_ENCODING_PROPERTY_KEY);
    }

    @Test
    public void testSystemProperties() {
        assertEquals(getProperty("java.version"), JAVA_VERSION);
        assertEquals(getProperty("java.vendor"), JAVA_VENDOR);
        assertEquals(getProperty("java.vendor.url"), JAVA_VENDOR_URL);
        assertEquals(getProperty("java.home"), JAVA_HOME);
        assertEquals(getProperty("java.vm.specification.version"), JAVA_VM_SPECIFICATION_VERSION);
        assertEquals(getProperty("java.vm.specification.vendor"), JAVA_VM_SPECIFICATION_VENDOR);
        assertEquals(getProperty("java.vm.specification.name"), JAVA_VM_SPECIFICATION_NAME);
        assertEquals(getProperty("java.vm.version"), JAVA_VM_VERSION);
        assertEquals(getProperty("java.vm.vendor"), JAVA_VM_VENDOR);
        assertEquals(getProperty("java.vm.name"), JAVA_VM_NAME);
        assertEquals(getProperty("java.specification.version"), JAVA_SPECIFICATION_VERSION);
        assertEquals(getProperty("java.specification.vendor"), JAVA_SPECIFICATION_VENDOR);
        assertEquals(getProperty("java.specification.name"), JAVA_SPECIFICATION_NAME);
        assertEquals(getProperty("java.class.version"), JAVA_CLASS_VERSION);
        assertEquals(getProperty("java.class.path"), JAVA_CLASS_PATH);
        assertEquals(getProperty("java.library.path"), JAVA_LIBRARY_PATH);
        assertEquals(getProperty("java.io.tmpdir"), JAVA_IO_TMPDIR);
        assertEquals(getProperty("java.compiler"), JAVA_COMPILER);
        assertEquals(getProperty("java.ext.dirs"), JAVA_EXT_DIRS);
        assertEquals(getProperty("os.name"), OS_NAME);
        assertEquals(getProperty("os.arch"), OS_ARCH);
        assertEquals(getProperty("os.version"), OS_VERSION);
        assertEquals(getProperty("user.name"), USER_NAME);
        assertEquals(getProperty("user.home"), USER_HOME);
        assertEquals(getProperty("user.dir"), USER_DIR);
        assertEquals(getProperty("file.encoding"), FILE_ENCODING);
        assertEquals(getProperty("native.encoding"), NATIVE_ENCODING);
    }

    @Test
    public void testJavaVersion() throws Throwable {
        assertEquals(IS_LTS_JAVA_VERSION, IS_JAVA_8 || IS_JAVA_11 || IS_JAVA_17 || IS_JAVA_21);
        assertJavaVersion();
    }

    private void assertJavaVersion() throws Throwable {
        SourceVersion currentVersion = latest();
        String javaMajorVersion = substringAfter(currentVersion.name(), "RELEASE_");
        String targetFiledName = IS_JAVA_VERSION_FIELD_NAME_PREFIX + javaMajorVersion;
        for (Field field : IS_JAVA_VERSION_FIELDS) {
            String fieldName = field.getName();
            Object fieldValue = field.get(null);
            assertEquals(Objects.equals(targetFiledName, fieldName), fieldValue);
        }
    }
}
