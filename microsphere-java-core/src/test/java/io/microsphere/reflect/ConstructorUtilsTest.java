package io.microsphere.reflect;

import org.junit.jupiter.api.Test;

import static io.microsphere.reflect.ConstructorUtils.findConstructor;
import static io.microsphere.reflect.ConstructorUtils.findConstructors;
import static io.microsphere.reflect.ConstructorUtils.findDeclaredConstructors;
import static io.microsphere.reflect.ConstructorUtils.getConstructor;
import static io.microsphere.reflect.ConstructorUtils.getDeclaredConstructor;
import static io.microsphere.reflect.ConstructorUtils.hasNonPrivateConstructorWithoutParameters;
import static io.microsphere.reflect.ConstructorUtils.isNonPrivateConstructorWithoutParameters;
import static io.microsphere.reflect.ConstructorUtils.newInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ConstructorUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConstructorUtils
 * @since 1.0.0
 */
class ConstructorUtilsTest {

    static class PublicConstructorClass {
        public PublicConstructorClass() {
        }

        public PublicConstructorClass(String name) {
        }
    }

    static class ProtectedConstructorClass {
        protected ProtectedConstructorClass() {
        }
    }

    static class PackagePrivateConstructorClass {
        PackagePrivateConstructorClass() {
        }
    }

    static class PrivateConstructorClass {
        private PrivateConstructorClass() {
        }
    }

    @Test
    void testIsNonPrivateConstructorWithoutParameters() {
        // null
        assertFalse(isNonPrivateConstructorWithoutParameters(null));

        assertTrue(isNonPrivateConstructorWithoutParameters(findConstructor(PublicConstructorClass.class)));

        // Non Default Constructor
        assertFalse(isNonPrivateConstructorWithoutParameters(findConstructor(PublicConstructorClass.class, String.class)));

        assertTrue(isNonPrivateConstructorWithoutParameters(findConstructor(ProtectedConstructorClass.class)));

        // Can't be found
        assertFalse(isNonPrivateConstructorWithoutParameters(findConstructor(ProtectedConstructorClass.class, String.class)));

        assertTrue(isNonPrivateConstructorWithoutParameters(findConstructor(PackagePrivateConstructorClass.class)));

        assertFalse(isNonPrivateConstructorWithoutParameters(findConstructor(PrivateConstructorClass.class)));
    }

    @Test
    void testHasNonPrivateConstructorWithoutParameters() {
        assertTrue(hasNonPrivateConstructorWithoutParameters(PublicConstructorClass.class));
        assertTrue(hasNonPrivateConstructorWithoutParameters(ProtectedConstructorClass.class));
        assertTrue(hasNonPrivateConstructorWithoutParameters(PackagePrivateConstructorClass.class));
        assertFalse(hasNonPrivateConstructorWithoutParameters(PrivateConstructorClass.class));
    }

    @Test
    void testFindConstructors() {
        assertEquals(2, findConstructors(PublicConstructorClass.class).size());
        assertEquals(0, findConstructors(ProtectedConstructorClass.class).size());
        assertEquals(0, findConstructors(PackagePrivateConstructorClass.class).size());
        assertEquals(0, findConstructors(PrivateConstructorClass.class).size());
    }

    @Test
    void testFindDeclaredConstructors() {
        assertEquals(2, findDeclaredConstructors(PublicConstructorClass.class).size());
        assertEquals(1, findDeclaredConstructors(ProtectedConstructorClass.class).size());
        assertEquals(1, findDeclaredConstructors(PackagePrivateConstructorClass.class).size());
        assertEquals(1, findDeclaredConstructors(PrivateConstructorClass.class).size());
    }

    @Test
    void testGetConstructor() {
        assertNotNull(getConstructor(PublicConstructorClass.class));
        assertThrows(RuntimeException.class, () -> getConstructor(ProtectedConstructorClass.class));
        assertThrows(RuntimeException.class, () -> getConstructor(PackagePrivateConstructorClass.class));
        assertThrows(RuntimeException.class, () -> getConstructor(PrivateConstructorClass.class));
    }

    @Test
    void testGetDeclaredConstructor() {
        assertNotNull(getDeclaredConstructor(PublicConstructorClass.class));
        assertNotNull(getDeclaredConstructor(ProtectedConstructorClass.class));
        assertNotNull(getDeclaredConstructor(PackagePrivateConstructorClass.class));
        assertNotNull(getDeclaredConstructor(PrivateConstructorClass.class));
    }

    @Test
    void testFindConstructor() {
        assertNotNull(findConstructor(PublicConstructorClass.class));
        assertNotNull(findConstructor(ProtectedConstructorClass.class));
        assertNotNull(findConstructor(PackagePrivateConstructorClass.class));
        assertNotNull(findConstructor(PrivateConstructorClass.class));
    }

    @Test
    void testNewInstance() {
        assertNotNull(newInstance(getConstructor(PublicConstructorClass.class)));
        assertThrows(RuntimeException.class, () -> newInstance(getConstructor(ProtectedConstructorClass.class)));
        assertThrows(RuntimeException.class, () -> newInstance(getConstructor(PackagePrivateConstructorClass.class)));
        assertThrows(RuntimeException.class, () -> newInstance(getConstructor(ProtectedConstructorClass.class)));
        assertThrows(RuntimeException.class, () -> newInstance(getConstructor(ProtectedConstructorClass.class)));
    }
}
