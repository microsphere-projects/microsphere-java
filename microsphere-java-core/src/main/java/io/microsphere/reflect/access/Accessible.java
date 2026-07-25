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

package io.microsphere.reflect.access;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Accessible interface for {@link Member}, which indicates that the {@link Member} can be accessed by the current caller.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AccessibleObject
 * @see Member
 * @see Field
 * @see Method
 * @see Constructor
 * @since 1.0.0
 */
public interface Accessible<M extends AccessibleObject & Member> {

    /**
     * Indicates whether the {@link Member} is accessible by the current caller.
     *
     * @param member the {@link Member} to check
     * @return true if the {@link Member} is accessible by the current caller, false otherwise
     */
    boolean isAccessible(M member);

    /**
     * Sets the accessible flag for this object to the indicated boolean value.
     *
     * @param accessible the new value for the accessible flag
     */
    default void setAccessible(M member, boolean accessible) {
        member.setAccessible(accessible);
    }

    /**
     * Tries to set the accessible flag for this object to true.
     *
     * @param member the {@link Member} to set accessible
     * @return true if the accessible flag was set to true, false otherwise
     */
    default boolean trySetAccessible(M member) {
        boolean accessible = isAccessible(member);
        if (accessible) {
            return true;
        }
        setAccessible(member, true);
        return isAccessible(member);
    }
}
