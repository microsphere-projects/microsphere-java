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

import io.microsphere.util.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

/**
 * Java Reflection {@link Member} Utilities class
 *
 * @since 1.0.0
 */
public abstract class MemberUtils implements Utils {

    /**
     * The {@link Predicate} reference to {@link #isStatic(Member)}
     */
    public final static Predicate<? super Member> STATIC_MEMBER_PREDICATE = MemberUtils::isStatic;

    /**
     * The {@link Predicate} reference to {@link #isNonStatic(Member)}
     */
    public final static Predicate<? super Member> NON_STATIC_MEMBER_PREDICATE = MemberUtils::isNonStatic;

    /**
     * The {@link Predicate} reference to {@link #isFinal(Member)}
     */
    public final static Predicate<? super Member> FINAL_MEMBER_PREDICATE = MemberUtils::isFinal;

    /**
     * The {@link Predicate} reference to {@link #isPublic(Member)}
     */
    public final static Predicate<? super Member> PUBLIC_MEMBER_PREDICATE = MemberUtils::isPublic;

    /**
     * The {@link Predicate} reference to {@link #isNonPrivate(Member)}
     */
    public final static Predicate<? super Member> NON_PRIVATE_MEMBER_PREDICATE = MemberUtils::isNonPrivate;

    /**
     * Checks whether the specified {@link Member} is declared as {@code static}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     static int staticField;
     *     int instanceField;
     *
     *     static void staticMethod() {}
     *     void instanceMethod() {}
     * }
     *
     * Field staticField = Example.class.getField("staticField");
     * boolean result1 = MemberUtils.isStatic(staticField); // true
     *
     * Field instanceField = Example.class.getField("instanceField");
     * boolean result2 = MemberUtils.isStatic(instanceField); // false
     *
     * Method staticMethod = Example.class.getMethod("staticMethod");
     * boolean result3 = MemberUtils.isStatic(staticMethod); // true
     *
     * Method instanceMethod = Example.class.getMethod("instanceMethod");
     * boolean result4 = MemberUtils.isStatic(instanceMethod); // false
     * }</pre>
     *
     * @param member the {@link Member} instance to check, such as a {@link Constructor}, {@link Method}, or {@link Field}
     * @return <code>true</code> if the member is static; <code>false</code> otherwise
     */
    public static boolean isStatic(Member member) {
        return member != null && Modifier.isStatic(member.getModifiers());
    }

    /**
     * Checks whether the specified {@link Member} is declared as {@code abstract}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public abstract class AbstractExample {
     *     abstract void abstractMethod();
     * }
     *
     * public class ConcreteExample extends AbstractExample {
     *     void abstractMethod() {}
     * }
     *
     * Method abstractMethod = AbstractExample.class.getMethod("abstractMethod");
     * boolean result1 = MemberUtils.isAbstract(abstractMethod); // true
     *
     * Method concreteMethod = ConcreteExample.class.getMethod("abstractMethod");
     * boolean result2 = MemberUtils.isAbstract(concreteMethod); // false
     *
     * Class<AbstractExample> abstractClass = AbstractExample.class;
     * boolean result3 = MemberUtils.isAbstract(abstractClass); // true
     *
     * Class<ConcreteExample> concreteClass = ConcreteExample.class;
     * boolean result4 = MemberUtils.isAbstract(concreteClass); // false
     * }</pre>
     *
     * @param member the {@link Member} instance to check, such as a {@link Method} or {@link Class}
     * @return <code>true</code> if the member is abstract; <code>false</code> otherwise
     */
    public static boolean isAbstract(Member member) {
        return member != null && Modifier.isAbstract(member.getModifiers());
    }

    /**
     * Checks whether the specified {@link Member} is declared as non-static.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     static int staticField;
     *     int instanceField;
     *
     *     static void staticMethod() {}
     *     void instanceMethod() {}
     * }
     *
     * Field staticField = Example.class.getField("staticField");
     * boolean result1 = MemberUtils.isNonStatic(staticField); // false
     *
     * Field instanceField = Example.class.getField("instanceField");
     * boolean result2 = MemberUtils.isNonStatic(instanceField); // true
     *
     * Method staticMethod = Example.class.getMethod("staticMethod");
     * boolean result3 = MemberUtils.isNonStatic(staticMethod); // false
     *
     * Method instanceMethod = Example.class.getMethod("instanceMethod");
     * boolean result4 = MemberUtils.isNonStatic(instanceMethod); // true
     * }</pre>
     *
     * @param member the {@link Member} instance to check, such as a {@link Constructor}, {@link Method}, or {@link Field}
     * @return <code>true</code> if the member is non-static; <code>false</code> otherwise
     */
    public static boolean isNonStatic(Member member) {
        return member != null && !Modifier.isStatic(member.getModifiers());
    }

    /**
     * Checks whether the specified {@link Member} is declared as {@code final}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     final int finalField = 0;
     *     int nonFinalField;
     *
     *     final void finalMethod() {}
     *     void nonFinalMethod() {}
     * }
     *
     * Field finalField = Example.class.getField("finalField");
     * boolean result1 = MemberUtils.isFinal(finalField); // true
     *
     * Field nonFinalField = Example.class.getField("nonFinalField");
     * boolean result2 = MemberUtils.isFinal(nonFinalField); // false
     *
     * Method finalMethod = Example.class.getMethod("finalMethod");
     * boolean result3 = MemberUtils.isFinal(finalMethod); // true
     *
     * Method nonFinalMethod = Example.class.getMethod("nonFinalMethod");
     * boolean result4 = MemberUtils.isFinal(nonFinalMethod); // false
     * }</pre>
     *
     * @param member the {@link Member} instance to check, such as a {@link Constructor}, {@link Method}, or {@link Field}
     * @return <code>true</code> if the member is final; <code>false</code> otherwise
     */
    public static boolean isFinal(Member member) {
        return member != null && Modifier.isFinal(member.getModifiers());
    }

    /**
     * Checks whether the specified {@link Member} is declared as {@code private}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     private int privateField;
     *     int defaultField;
     *
     *     private void privateMethod() {}
     *     void defaultMethod() {}
     * }
     *
     * Field privateField = Example.class.getDeclaredField("privateField");
     * boolean result1 = MemberUtils.isPrivate(privateField); // true
     *
     * Field defaultField = Example.class.getField("defaultField");
     * boolean result2 = MemberUtils.isPrivate(defaultField); // false
     *
     * Method privateMethod = Example.class.getDeclaredMethod("privateMethod");
     * boolean result3 = MemberUtils.isPrivate(privateMethod); // true
     *
     * Method defaultMethod = Example.class.getMethod("defaultMethod");
     * boolean result4 = MemberUtils.isPrivate(defaultMethod); // false
     * }</pre>
     *
     * @param member the {@link Member} instance to check, such as a {@link Constructor}, {@link Method}, or {@link Field}
     * @return <code>true</code> if the member is private; <code>false</code> otherwise
     */
    public static boolean isPrivate(Member member) {
        return member != null && Modifier.isPrivate(member.getModifiers());
    }

    /**
     * Checks whether the specified {@link Member} is declared as {@code public}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     public int publicField;
     *     private void privateMethod() {}
     *
     *     public void publicMethod() {}
     * }
     *
     * Field publicField = Example.class.getField("publicField");
     * boolean result1 = MemberUtils.isPublic(publicField); // true
     *
     * Method privateMethod = Example.class.getDeclaredMethod("privateMethod");
     * boolean result2 = MemberUtils.isPublic(privateMethod); // false
     *
     * Method publicMethod = Example.class.getMethod("publicMethod");
     * boolean result3 = MemberUtils.isPublic(publicMethod); // true
     * }</pre>
     *
     * @param member the {@link Member} instance to check, such as a {@link Constructor}, {@link Method}, or {@link Field}
     * @return <code>true</code> if the member is public; <code>false</code> otherwise
     */
    public static boolean isPublic(Member member) {
        return member != null && Modifier.isPublic(member.getModifiers());
    }

    /**
     * check the specified {@link Member member} is non-private or not ?
     *
     * @param member {@link Member} instance, e.g, {@link Constructor}, {@link Method} or {@link Field}
     * @return Iff <code>member</code> is non-private one, return <code>true</code>, or <code>false</code>
     */
    /**
     * Checks whether the specified {@link Member} is declared as non-private.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     private int privateField;
     *     int defaultField;
     *     protected int protectedField;
     *     public int publicField;
     *
     *     private void privateMethod() {}
     *     void defaultMethod() {}
     *     protected void protectedMethod() {}
     *     public void publicMethod() {}
     * }
     *
     * Field privateField = Example.class.getDeclaredField("privateField");
     * boolean result1 = MemberUtils.isNonPrivate(privateField); // false
     *
     * Field defaultField = Example.class.getField("defaultField");
     * boolean result2 = MemberUtils.isNonPrivate(defaultField); // true
     *
     * Field protectedField = Example.class.getField("protectedField");
     * boolean result3 = MemberUtils.isNonPrivate(protectedField); // true
     *
     * Field publicField = Example.class.getField("publicField");
     * boolean result4 = MemberUtils.isNonPrivate(publicField); // true
     *
     * Method privateMethod = Example.class.getDeclaredMethod("privateMethod");
     * boolean result5 = MemberUtils.isNonPrivate(privateMethod); // false
     *
     * Method defaultMethod = Example.class.getMethod("defaultMethod");
     * boolean result6 = MemberUtils.isNonPrivate(defaultMethod); // true
     *
     * Method protectedMethod = Example.class.getMethod("protectedMethod");
     * boolean result7 = MemberUtils.isNonPrivate(protectedMethod); // true
     *
     * Method publicMethod = Example.class.getMethod("publicMethod");
     * boolean result8 = MemberUtils.isNonPrivate(publicMethod); // true
     * }</pre>
     *
     * @param member the {@link Member} instance to check, such as a {@link Constructor}, {@link Method}, or {@link Field}
     * @return <code>true</code> if the member is non-private; <code>false</code> otherwise
     */
    public static boolean isNonPrivate(Member member) {
        return member != null && !Modifier.isPrivate(member.getModifiers());
    }

    /**
     * Attempts to cast the provided object to an instance of {@link Member}.
     *
     * <p>This method checks whether the given object is an instance of the {@link Member} interface.
     * If it is, the object is casted and returned as a {@link Member}. Otherwise, this method returns
     * {@code null}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Field field = Example.class.getField("publicField");
     * Member member1 = MemberUtils.asMember(field);
     * System.out.println(member1 == field); // true
     *
     * Method method = Example.class.getMethod("publicMethod");
     * Member member2 = MemberUtils.asMember(method);
     * System.out.println(member2 == method); // true
     *
     * String notAMember = "This is not a Member";
     * Member member3 = MemberUtils.asMember(notAMember);
     * System.out.println(member3 == null); // true
     * }</pre>
     *
     * @param object the object to be casted to a {@link Member}, may be {@code null}
     * @return the casted {@link Member} instance if the object is a valid {@link Member}; otherwise,
     * returns {@code null}
     */
    public static Member asMember(Object object) {
        return object instanceof Member ? (Member) object : null;
    }

    private MemberUtils() {
    }
}