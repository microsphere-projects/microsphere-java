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

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.util.Utils;

import java.io.File;

import static io.microsphere.annotation.ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;

/**
 * The utilities class for Java Security
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SecurityManager
 * @see SecurityException
 * @since 1.0.0
 */
public abstract class SecurityUtils implements Utils {

    /**
     * The System Property name of Java Security Policy File.
     */
    @ConfigurationProperty(source = SYSTEM_PROPERTIES_SOURCE)
    public static final String JAVA_SECURITY_POLICY_FILE_PROPERTY_NAME = "java.security.policy";

    /**
     * Set the Java Security Policy File
     *
     * @param javaSecurityPolicyFilePath the absolute path of Java Security Policy File
     */
    public static void setJavaSecurityPolicyFile(@Nonnull String javaSecurityPolicyFilePath) {
        setProperty(JAVA_SECURITY_POLICY_FILE_PROPERTY_NAME, javaSecurityPolicyFilePath);
    }

    /**
     * Set the Java Security Policy File
     *
     * @param javaSecurityPolicyFile the Java Security Policy File
     */
    public static void setJavaSecurityPolicyFile(@Nonnull File javaSecurityPolicyFile) {
        setJavaSecurityPolicyFile(javaSecurityPolicyFile.getAbsolutePath());
    }

    /**
     * Get the Java Security Policy File
     *
     * @return the Java Security Policy File
     */
    @Nullable
    public static String getJavaSecurityPolicyFile() {
        return getProperty(JAVA_SECURITY_POLICY_FILE_PROPERTY_NAME);
    }

    private SecurityUtils() {
    }
}