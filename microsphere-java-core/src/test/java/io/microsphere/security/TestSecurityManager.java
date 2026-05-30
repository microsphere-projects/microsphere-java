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

import java.security.Permission;

/**
 * {@link SecurityManager} for Testing
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class TestSecurityManager extends SecurityManager {

    private static ThreadLocal<Permission> denyPermissionHolder = new ThreadLocal();

    public static void denyRuntimePermission(String name, Runnable action) {
        deny(new RuntimePermission(name), action);
    }

    public static void deny(Permission permission, Runnable action) {
        try {
            TestSecurityManager securityManager = new TestSecurityManager();
            System.setSecurityManager(securityManager);
            denyPermissionHolder.set(permission);
            action.run();
        } finally {
            clear();
        }
    }

    public static Permission getDenyPermission() {
        return denyPermissionHolder.get();
    }

    public static void clear() {
        denyPermissionHolder.remove();
        System.setSecurityManager(null);
    }

    @Override
    public void checkPermission(Permission perm) {
        Permission denyPermission = getDenyPermission();
        if (perm.equals(denyPermission)) {
            throw new SecurityException("Permission " + perm.getName() + "is deny!");
        }
    }
}
