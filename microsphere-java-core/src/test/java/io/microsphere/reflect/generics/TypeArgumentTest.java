package io.microsphere.reflect.generics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.reflect.generics.TypeArgument.create;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link TypeArgument} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see TypeArgument
 * @since 1.0.0
 */
public class TypeArgumentTest {

    private TypeArgument typeArgument;

    @BeforeEach
    void setUp() {
        typeArgument = create(String.class, 0);
    }

    @Test
    void testToString() {
        assertEquals("TypeArgument{type=class java.lang.String, index=0}", typeArgument.toString());
    }

    @Test
    void testGetType() {
        assertEquals(String.class, typeArgument.getType());
    }

    @Test
    void testGetIndex() {
        assertEquals(0, typeArgument.getIndex());
    }

    @Test
    void testEquals() {
        assertEquals(typeArgument, typeArgument);
    }

    @Test
    void testEqualsOnSameType() {
        assertEquals(typeArgument, create(String.class, 0));
        assertNotEquals(typeArgument, create(String.class, 1));
        assertNotEquals(typeArgument, create(Object.class, 2));
    }

    @Test
    void testEqualsOnDifferentType() {
        assertNotEquals(typeArgument, Object.class);
    }

    @Test
    void testHashCode() {
        assertEquals(typeArgument.hashCode(), typeArgument.hashCode());
    }

    @Test
    void testHashCodeOnSameType() {
        assertEquals(typeArgument.hashCode(), create(String.class, 0).hashCode());
        assertNotEquals(typeArgument.hashCode(), create(String.class, 1).hashCode());
        assertNotEquals(typeArgument.hashCode(), create(Object.class, 2).hashCode());
    }

    @Test
    void testHashCodeOnDifferentType() {
        assertNotEquals(typeArgument.hashCode(), Object.class.hashCode());
    }

    @Test
    void testCreateOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> create(null, 0));
        assertThrows(IllegalArgumentException.class, () -> create(String.class, -1));
    }
}