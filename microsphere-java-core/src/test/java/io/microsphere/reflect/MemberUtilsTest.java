package io.microsphere.reflect;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.reflect.MemberUtils.FINAL_MEMBER_PREDICATE;
import static io.microsphere.reflect.MemberUtils.NON_PRIVATE_MEMBER_PREDICATE;
import static io.microsphere.reflect.MemberUtils.NON_STATIC_MEMBER_PREDICATE;
import static io.microsphere.reflect.MemberUtils.PUBLIC_MEMBER_PREDICATE;
import static io.microsphere.reflect.MemberUtils.STATIC_MEMBER_PREDICATE;
import static io.microsphere.reflect.MemberUtils.asMember;
import static io.microsphere.reflect.MemberUtils.isAbstract;
import static io.microsphere.reflect.MemberUtils.isFinal;
import static io.microsphere.reflect.MemberUtils.isNonPrivate;
import static io.microsphere.reflect.MemberUtils.isNonStatic;
import static io.microsphere.reflect.MemberUtils.isPrivate;
import static io.microsphere.reflect.MemberUtils.isPublic;
import static io.microsphere.reflect.MemberUtils.isStatic;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MemberUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MemberUtils
 * @since 1.0.0
 */
public class MemberUtilsTest {

    @Test
    void testSTATIC_METHOD_PREDICATE() {
        assertTrue(STATIC_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "staticMethod")));
    }

    @Test
    void testNON_STATIC_METHOD_PREDICATE() {
        assertTrue(NON_STATIC_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "privateMethod")));
    }

    @Test
    void testFINAL_METHOD_PREDICATE() {
        assertTrue(FINAL_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "errorMethod")));
    }

    @Test
    void testPUBLIC_METHOD_PREDICATE() {
        assertTrue(PUBLIC_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "publicMethod", int.class)));
    }

    @Test
    void testNON_PRIVATE_METHOD_PREDICATE() {
        assertTrue(NON_PRIVATE_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "publicMethod", int.class)));
        assertTrue(NON_PRIVATE_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "protectedMethod", Object[].class)));
        assertTrue(NON_PRIVATE_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "packagePrivateMethod", String.class)));
    }

    @Test
    void testIsStatic() {
        assertTrue(isStatic(findField(ReflectionTest.class, "staticField")));
    }

    @Test
    void testIsStaticOnNonStaticField() {
        assertFalse(isStatic(findField(ReflectionTest.class, "field")));
    }

    @Test
    void testIsStaticOnNull() {
        assertFalse(isStatic(null));
    }


    @Test
    void testIsAbstract() {
        assertTrue(isAbstract(findMethod(Map.class, "size")));
    }

    @Test
    void testIsAbstractOnNonAbstract() {
        assertFalse(isStatic(findMethod(ReflectionTest.class, "privateMethod")));
    }

    @Test
    void testIsAbstractOnNull() {
        assertFalse(isStatic(null));
    }

    @Test
    void testIsNonStatic() {
        assertTrue(isNonStatic(findField(ReflectionTest.class, "privateField")));
    }

    @Test
    void testIsNonStaticOnStaticField() {
        assertFalse(isNonStatic(findField(ReflectionTest.class, "staticField")));
    }

    @Test
    void testIsNonStaticOnNull() {
        assertFalse(isNonStatic(null));
    }

    @Test
    void testIsFinal() {
        assertTrue(isFinal(findField(ReflectionTest.class, "privateField")));
    }

    @Test
    void testIsFinalOnNonFinalField() {
        assertFalse(isFinal(findField(ReflectionTest.class, "packagePrivateField")));
    }

    @Test
    void testIsFinalOnNull() {
        assertFalse(isFinal(null));
    }

    @Test
    void testIsPrivate() {
        assertTrue(isPrivate(findField(ReflectionTest.class, "privateField")));
    }

    @Test
    void testIsPrivateOnNonPrivateField() {
        assertFalse(isPrivate(findField(ReflectionTest.class, "packagePrivateField")));
    }

    @Test
    void testIsPrivateOnNull() {
        assertFalse(isPrivate(null));
    }

    @Test
    void testIsPublic() {
        assertTrue(isPublic(findField(ReflectionTest.class, "publicField")));
    }

    @Test
    void testIsPublicOnNonPublicField() {
        assertFalse(isPublic(findField(ReflectionTest.class, "packagePrivateField")));
    }

    @Test
    void testIsPublicOnNull() {
        assertFalse(isPublic(null));
    }

    @Test
    void testIsNonPrivate() {
        assertTrue(isNonPrivate(findField(ReflectionTest.class, "publicField")));
        assertTrue(isNonPrivate(findField(ReflectionTest.class, "packagePrivateField")));
        assertTrue(isNonPrivate(findField(ReflectionTest.class, "protectedField")));
    }

    @Test
    void testIsNonPrivateOnPrivateField() {
        assertFalse(isNonPrivate(findField(ReflectionTest.class, "privateField")));
    }

    @Test
    void testIsNonPrivateOnNull() {
        assertFalse(isNonPrivate(null));
    }

    @Test
    void testAsMember() {
        assertNotNull(asMember(findField(ReflectionTest.class, "publicField")));
    }

    @Test
    void testAsMemberOnNotMember() {
        assertNull(asMember(new Object()));
    }

    @Test
    void testAsMemberOnNull() {
        assertNull(asMember(null));
    }
}