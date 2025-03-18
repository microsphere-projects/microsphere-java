package io.microsphere.reflect;

import io.microsphere.test.StringIntegerToBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static io.microsphere.reflect.TypeFinder.classFinder;
import static io.microsphere.reflect.TypeFinder.genericTypeFinder;
import static io.microsphere.reflect.TypeFinder.of;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link TypeFinder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see TypeFinder
 * @since 1.0.0
 */
public class TypeFinderTest {

    @Test
    public void testConstructorOnNullType() {
        assertThrows(IllegalArgumentException.class, () -> of(null, true, true, true, true, true));
        assertThrows(IllegalArgumentException.class, () -> classFinder(null, TypeFinder.Include.values()));
        assertThrows(IllegalArgumentException.class, () -> classFinder(null, true, true, true, true));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(null, TypeFinder.Include.values()));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(null, true, true, true, true));
    }

    @Test
    public void testConstructorOnNullIncludes() {
        assertThrows(IllegalArgumentException.class, () -> classFinder(StringIntegerToBoolean.class, null));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(StringIntegerToBoolean.class, null));
    }

    @Test
    public void testConstructorOnEmptyIncludes() {
        assertThrows(IllegalArgumentException.class, () -> classFinder(StringIntegerToBoolean.class, new TypeFinder.Include[0]));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(StringIntegerToBoolean.class, new TypeFinder.Include[0]));
    }

    @Test
    public void testConstructorOnNullTypeAndIncludes() {
        assertThrows(IllegalArgumentException.class, () -> classFinder(StringIntegerToBoolean.class, new TypeFinder.Include[]{null}));
        assertThrows(IllegalArgumentException.class, () -> genericTypeFinder(StringIntegerToBoolean.class,new TypeFinder.Include[]{null}));
    }

    @Test
    public void testGetTypes() {
    }

    @Test
    public void testFindTypes() {
    }
}