package io.microsphere.io;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link FastByteArrayOutputStream} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see FastByteArrayOutputStream
 * @since 1.0.0
 */
class FastByteArrayOutputStreamTest {

    private static final String TEST_VALUE = "Hello";

    private static final byte[] TEST_BYTES = TEST_VALUE.getBytes();

    private FastByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        outputStream = new FastByteArrayOutputStream(2);
    }

    @AfterEach
    void destroy() {
        outputStream.close();
    }

    @Test
    void testConstructorOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new FastByteArrayOutputStream(-1));
    }

    @Test
    void testWrite() {
        for (byte b : TEST_BYTES) {
            outputStream.write(b);
        }
        assertArrayEquals(outputStream.toByteArray(), TEST_BYTES);
    }

    @Test
    void testWrite0() throws IOException {
        outputStream.write(TEST_BYTES);
        assertArrayEquals(outputStream.toByteArray(), TEST_BYTES);
    }

    @Test
    void testWrite0OnNullPointerException() {
        assertThrows(NullPointerException.class, () -> outputStream.write(null));
    }

    @Test
    void testWrite0OnIndexOutOfBoundsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> outputStream.write(TEST_BYTES, -1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> outputStream.write(TEST_BYTES, TEST_BYTES.length + 1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> outputStream.write(TEST_BYTES, 0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> outputStream.write(TEST_BYTES, 0, TEST_BYTES.length + 1));
    }

    @Test
    void testWriteTo() throws IOException {
        FastByteArrayOutputStream toOutputStream = new FastByteArrayOutputStream();
        outputStream.write(TEST_BYTES);
        outputStream.writeTo(toOutputStream);
        assertArrayEquals(outputStream.toByteArray(), toOutputStream.toByteArray());
    }

    @Test
    void testReset() throws IOException {
        testSize();

        outputStream.reset();
        assertEquals(0, outputStream.size());
    }

    @Test
    void testToByteArray() throws IOException {
        testWrite0();
    }

    @Test
    void testSize() throws IOException {
        assertEquals(0, outputStream.size());
        outputStream.write(TEST_BYTES);
        assertEquals(TEST_BYTES.length, outputStream.size());
    }

    @Test
    void testToString() throws IOException {
        outputStream.write(TEST_BYTES);
        assertEquals(TEST_VALUE, outputStream.toString());
    }

    @Test
    void testToString1() throws IOException {
        outputStream.write(TEST_BYTES);
        assertEquals(TEST_VALUE, outputStream.toString("UTF-8"));
    }

    @Test
    void testClose() {
        destroy();
    }
}