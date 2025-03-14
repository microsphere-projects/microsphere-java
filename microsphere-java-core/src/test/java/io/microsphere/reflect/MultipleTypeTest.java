package io.microsphere.reflect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static io.microsphere.reflect.MultipleType.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * {@link MultipleType} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class MultipleTypeTest {

    private MultipleType multipleType;

    @BeforeEach
    public void setUp() {
        this.multipleType = of(String.class, Object.class);
    }

    @Test
    public void testHashCode() {
        assertEquals(Objects.hash(String.class, Object.class), multipleType.hashCode());
    }

    @Test
    public void testEquals() {
        assertEquals(of(String.class, Object.class), multipleType);
        assertEquals(of(String.class, Object.class, Integer.class), of(String.class, Object.class, Integer.class));
        assertNotEquals(of(String.class, Object.class, Integer.class), multipleType);
    }

    @Test
    public void testToString() {
        assertEquals("MultipleType : [class java.lang.String, class java.lang.Object]", multipleType.toString());
    }
}