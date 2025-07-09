package io.microsphere.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link DefaultDeserializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DefaultDeserializer
 * @since 1.0.0
 */
class DefaultDeserializerTest {

    private DefaultDeserializer deserializer = DefaultDeserializer.INSTANCE;

    @Test
    void testDeserializeOnNull() throws IOException {
        assertNull(deserializer.deserialize(null));
    }
}