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
    public void testSTATIC_METHOD_PREDICATE() {
        assertTrue(STATIC_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "staticMethod")));
    }

    @Test
    public void testNON_STATIC_METHOD_PREDICATE() {
        assertTrue(NON_STATIC_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "privateMethod")));
    }

    @Test
    public void testFINAL_METHOD_PREDICATE() {
        assertTrue(FINAL_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "errorMethod")));
    }

    @Test
    public void testPUBLIC_METHOD_PREDICATE() {
        assertTrue(PUBLIC_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "publicMethod", int.class)));
    }

    @Test
    public void testNON_PRIVATE_METHOD_PREDICATE() {
        assertTrue(NON_PRIVATE_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "publicMethod", int.class)));
        assertTrue(NON_PRIVATE_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "protectedMethod", Object[].class)));
        assertTrue(NON_PRIVATE_MEMBER_PREDICATE.test(findMethod(ReflectionTest.class, "packagePrivateMethod", String.class)));
    }

    @Test
    public void testIsStatic() {
        assertTrue(isStatic(findField(ReflectionTest.class, "staticField")));
    }

    @Test
    public void testIsStaticOnNonStaticField() {
        assertFalse(isStatic(findField(ReflectionTest.class, "field")));
    }

    @Test
    public void testIsStaticOnNull() {
        assertFalse(isStatic(null));
    }


    @Test
    public void testIsAbstract() {
        assertTrue(isAbstract(findMethod(Map.class, "size")));
    }

    @Test
    public void testIsAbstractOnNonAbstract() {
        assertFalse(isStatic(findMethod(ReflectionTest.class, "privateMethod")));
    }

    @Test
    public void testIsAbstractOnNull() {
        assertFalse(isStatic(null));
    }

    @Test
    public void testIsNonStatic() {
        assertTrue(isNonStatic(findField(ReflectionTest.class, "privateField")));
    }

    @Test
    public void testIsNonStaticOnStaticField() {
        assertFalse(isNonStatic(findField(ReflectionTest.class, "staticField")));
    }

    @Test
    public void testIsNonStaticOnNull() {
        assertFalse(isNonStatic(null));
    }

    @Test
    public void testIsFinal() {
        assertTrue(isFinal(findField(ReflectionTest.class, "privateField")));
    }

    @Test
    public void testIsFinalOnNonFinalField() {
        assertFalse(isFinal(findField(ReflectionTest.class, "packagePrivateField")));
    }

    @Test
    public void testIsFinalOnNull() {
        assertFalse(isFinal(null));
    }

    @Test
    public void testIsPrivate() {
        assertTrue(isPrivate(findField(ReflectionTest.class, "privateField")));
    }

    @Test
    public void testIsPrivateOnNonPrivateField() {
        assertFalse(isPrivate(findField(ReflectionTest.class, "packagePrivateField")));
    }

    @Test
    public void testIsPrivateOnNull() {
        assertFalse(isPrivate(null));
    }

    @Test
    public void testIsPublic() {
        assertTrue(isPublic(findField(ReflectionTest.class, "publicField")));
    }

    @Test
    public void testIsPublicOnNonPublicField() {
        assertFalse(isPublic(findField(ReflectionTest.class, "packagePrivateField")));
    }

    @Test
    public void testIsPublicOnNull() {
        assertFalse(isPublic(null));
    }

    @Test
    public void testIsNonPrivate() {
        assertTrue(isNonPrivate(findField(ReflectionTest.class, "publicField")));
        assertTrue(isNonPrivate(findField(ReflectionTest.class, "packagePrivateField")));
        assertTrue(isNonPrivate(findField(ReflectionTest.class, "protectedField")));
    }

    @Test
    public void testIsNonPrivateOnPrivateField() {
        assertFalse(isNonPrivate(findField(ReflectionTest.class, "privateField")));
    }

    @Test
    public void testIsNonPrivateOnNull() {
        assertFalse(isNonPrivate(null));
    }

    @Test
    public void testAsMember() {
        assertNotNull(asMember(findField(ReflectionTest.class, "publicField")));
    }

    @Test
    public void testAsMemberOnNotMember() {
        assertNull(asMember(new Object()));
    }

    @Test
    public void testAsMemberOnNull() {
        assertNull(asMember(null));
    }
}