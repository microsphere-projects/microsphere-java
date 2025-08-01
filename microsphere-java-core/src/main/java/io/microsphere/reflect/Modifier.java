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
package io.microsphere.reflect;

/**
 * The enumeration class for Java Reflection {@link java.lang.reflect.Modifier}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see java.lang.reflect.Modifier
 * @since 1.0.0
 */
public enum Modifier {

    /**
     * The {@code public} modifier.
     */
    PUBLIC(java.lang.reflect.Modifier.PUBLIC),

    /**
     * The {@code private} modifier.
     */
    PRIVATE(java.lang.reflect.Modifier.PRIVATE),

    /**
     * The {@code protected} modifier.
     */
    PROTECTED(java.lang.reflect.Modifier.PROTECTED),

    /**
     * The {@code static} modifier.
     */
    STATIC(java.lang.reflect.Modifier.STATIC),

    /**
     * The {@code final} modifier.
     */
    FINAL(java.lang.reflect.Modifier.FINAL),

    /**
     * The {@code synchronized} modifier.
     */
    SYNCHRONIZED(java.lang.reflect.Modifier.SYNCHRONIZED),

    /**
     * The {@code volatile} modifier.
     */
    VOLATILE(java.lang.reflect.Modifier.VOLATILE),

    /**
     * The {@code transient} modifier.
     */
    TRANSIENT(java.lang.reflect.Modifier.TRANSIENT),

    /**
     * The {@code native} modifier.
     */
    NATIVE(java.lang.reflect.Modifier.NATIVE),

    /**
     * The {@code interface} modifier.
     */
    INTERFACE(java.lang.reflect.Modifier.INTERFACE),

    /**
     * The {@code abstract} modifier.
     */
    ABSTRACT(java.lang.reflect.Modifier.ABSTRACT),

    /**
     * The {@code strictfp} modifier.
     */
    STRICT(java.lang.reflect.Modifier.STRICT),

    BRIDGE(0x00000040),

    VARARGS(0x00000080),

    SYNTHETIC(0x00001000),

    ANNOTATION(0x00002000),

    ENUM(0x00004000),

    MANDATED(0x00008000);

    private final int value;

    Modifier(int value) {
        this.value = value;
    }

    /**
     * The bit value of modifier
     *
     * @return the bit value of modifier
     */
    public int getValue() {
        return value;
    }

    /**
     * matches the specified modifier
     *
     * @param mod the bit of modifier
     * @return <code>true</code> if matches, otherwise <code>false</code>
     */
    public boolean matches(int mod) {
        return (mod & value) != 0;
    }

    /**
     * matches the specified modifiers
     *
     * @param modifiers the modifier
     * @return <code>true</code> if matches, otherwise <code>false</code>
     */
    public static boolean isPublic(int modifiers) {
        return PUBLIC.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code private} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code private} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#PRIVATE
     * @see #PRIVATE
     */
    public static boolean isPrivate(int modifiers) {
        return PRIVATE.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code protected} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code protected} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#PROTECTED
     * @see #PROTECTED
     */
    public static boolean isProtected(int modifiers) {
        return PROTECTED.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code static} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code static} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#STATIC
     * @see #STATIC
     */
    public static boolean isStatic(int modifiers) {
        return STATIC.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code final} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code final} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#FINAL
     * @see #FINAL
     */
    public static boolean isFinal(int modifiers) {
        return FINAL.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code synchronized} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code synchronized} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#SYNCHRONIZED
     * @see #SYNCHRONIZED
     */
    public static boolean isSynchronized(int modifiers) {
        return SYNCHRONIZED.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code volatile} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code volatile} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#VOLATILE
     * @see #VOLATILE
     */
    public static boolean isVolatile(int modifiers) {
        return VOLATILE.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code transient} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code transient} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#TRANSIENT
     * @see #TRANSIENT
     */
    public static boolean isTransient(int modifiers) {
        return TRANSIENT.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code native} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code native} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#NATIVE
     * @see #NATIVE
     */
    public static boolean isNative(int modifiers) {
        return NATIVE.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code interface} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code interface} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#INTERFACE
     * @see #INTERFACE
     */
    public static boolean isInterface(int modifiers) {
        return INTERFACE.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code abstract} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code abstract} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#ABSTRACT
     * @see #ABSTRACT
     */
    public static boolean isAbstract(int modifiers) {
        return ABSTRACT.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code strict} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code strict} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#STRICT
     * @see #STRICT
     */
    public static boolean isStrict(int modifiers) {
        return STRICT.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code bridge} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code bridge} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#BRIDGE
     * @see #BRIDGE
     */
    public static boolean isBridge(int modifiers) {
        return BRIDGE.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code varargs} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code varargs} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#VARARGS
     * @see #VARARGS
     */
    public static boolean isVarArgs(int modifiers) {
        return VARARGS.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code synthetic} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code synthetic} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#SYNTHETIC
     * @see #SYNTHETIC
     */
    public static boolean isSynthetic(int modifiers) {
        return SYNTHETIC.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code annotation} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code annotation} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#ANNOTATION
     * @see #ANNOTATION
     */
    public static boolean isAnnotation(int modifiers) {
        return ANNOTATION.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code enum} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code enum} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#ENUM
     * @see #ENUM
     */
    public static boolean isEnum(int modifiers) {
        return ENUM.matches(modifiers);
    }

    /**
     * Checks if the specified modifiers contain the {@code mandated} modifier.
     *
     * @param modifiers the bit mask of modifiers to check
     * @return {@code true} if the {@code mandated} modifier is present, otherwise {@code false}
     * @see java.lang.reflect.Modifier#MANDATED
     * @see #MANDATED
     */
    public static boolean isMandated(int modifiers) {
        return MANDATED.matches(modifiers);
    }

}
