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

package io.microsphere.security;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static io.microsphere.security.SecurityUtils.JAVA_SECURITY_POLICY_FILE_PROPERTY_NAME;
import static io.microsphere.security.SecurityUtils.getJavaSecurityPolicyFile;
import static io.microsphere.security.SecurityUtils.setJavaSecurityPolicyFile;
import static io.microsphere.util.ClassLoaderUtils.ResourceType.CLASS;
import static io.microsphere.util.ClassLoaderUtils.getResource;
import static io.microsphere.util.StringUtils.substringBefore;
import static java.lang.System.clearProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link SecurityUtils}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SecurityUtils
 * @since 1.0.0
 */
class SecurityUtilsTest {

    private static final String MODULE_BASE_PROPERTY_NAME = "module.base";

    private static final URL policyResource = getResource("META-INF/class-load-test.policy");

    private static final File policyFile = new File(policyResource.getFile());

    static {
        URL classResource = getResource(SecurityUtilsTest.class.getClassLoader(), CLASS, SecurityUtilsTest.class.getName() + ".class");
        String moduleBasePath = substringBefore(classResource.getFile(), "target/");
        System.setProperty(MODULE_BASE_PROPERTY_NAME, moduleBasePath);
    }

    @AfterEach
    void tearDown() {
        clearProperty(JAVA_SECURITY_POLICY_FILE_PROPERTY_NAME);
        clearProperty(MODULE_BASE_PROPERTY_NAME);
    }

    @Test
    void testtConstants() {
        assertEquals("java.security.policy", JAVA_SECURITY_POLICY_FILE_PROPERTY_NAME);
    }

    @Test
    void testJavaSecurityPolicyFile() {
        setJavaSecurityPolicyFile(policyFile);
        assertEquals(policyFile.getAbsolutePath(), getJavaSecurityPolicyFile());
    }
}