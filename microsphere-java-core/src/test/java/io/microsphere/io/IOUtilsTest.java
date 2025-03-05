package io.microsphere.io;

import org.junit.jupiter.api.Test;

import static io.microsphere.io.IOUtils.BUFFER_SIZE;
import static io.microsphere.io.IOUtils.DEFAULT_BUFFER_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link IOUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see IOUtils
 * @since 1.0.0
 */
public class IOUtilsTest {

    @Test
    public void testConstants() {
        assertEquals(2048, DEFAULT_BUFFER_SIZE);
        assertEquals(2048, BUFFER_SIZE);
    }

    @Test
    public void testToByteArray() {
    }

    @Test
    public void testToString() {
    }

    @Test
    public void testToString1() {
    }

    @Test
    public void testCopy() {
    }

    @Test
    public void testClose() {
    }

    @Test
    public void testClose0() {
    }
}