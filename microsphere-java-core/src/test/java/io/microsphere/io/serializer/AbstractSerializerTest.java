package io.microsphere.io.serializer;

import io.microsphere.reflect.JavaType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static io.microsphere.reflect.JavaType.from;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Abstract {@link Serializer} Test
 *
 * @param <T> Serialization type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractSerializer
 * @since 1.0.0
 */
abstract class AbstractSerializerTest<T> {

    @Test
    void test() throws IOException {
        test(this::getValue);
    }

    @Test
    void testNull() throws IOException {
        test(this::getNullValue);
    }

    void test(Supplier<T> valueSupplier) throws IOException {
        T value = valueSupplier.get();
        AbstractSerializer<T> serializer = getSerializer();
        byte[] bytes = serializer.serialize(value);
        T deserialized = serializer.deserialize(bytes);
        if (value != null && deserialized != null) {
            assertEquals(getTestData(value), getTestData(deserialized));
        } else {
            assertEquals(value, deserialized);
        }

        Class<?> targetType = serializer.getTargetType();
        JavaType javaType = from(getClass()).as(AbstractSerializerTest.class).getGenericType(0);
        Class<?> parameterType = javaType.toClass();

        assertSame(targetType, parameterType);
    }

    protected Object getTestData(T value) {
        return value;
    }

    protected abstract AbstractSerializer<T> getSerializer();

    protected abstract T getValue();

    protected T getNullValue() {
        return null;
    }
}
