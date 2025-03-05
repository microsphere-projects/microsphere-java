package io.microsphere.io;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.Integer.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link FastByteArrayInputStream} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see FastByteArrayInputStream
 * @since 1.0.0
 */
public class FastByteArrayInputStreamTest {

    private static final String TEST_VALUE = "Hello";

    private static final int TEST_OFFSET = 2;

    private FastByteArrayInputStream inputStream;

    private FastByteArrayInputStream inputStream2;

    @BeforeEach
    public void init() {
        byte[] bytes = TEST_VALUE.getBytes();
        inputStream = new FastByteArrayInputStream(bytes);
        inputStream2 = new FastByteArrayInputStream(TEST_VALUE.getBytes(), TEST_OFFSET, TEST_VALUE.length());
    }

    @AfterEach
    public void destroy() throws IOException {
        inputStream.close();
        inputStream2.close();
    }


    @Test
    public void testRead() {
        assertEquals('H', inputStream.read());
        assertEquals('e', inputStream.read());
        assertEquals('l', inputStream.read());
        assertEquals('l', inputStream.read());
        assertEquals('o', inputStream.read());

        assertEquals('l', inputStream2.read());
        assertEquals('l', inputStream2.read());
        assertEquals('o', inputStream2.read());
    }

    @Test
    public void testRead1() {
        byte[] bytes = new byte[8];
        int offset = 0;
        int length = inputStream.available();
        assertEquals(TEST_VALUE.length(), inputStream.read(bytes, offset, length));
        assertEquals(TEST_VALUE, new String(bytes, offset, length));

        length = TEST_VALUE.length() - TEST_OFFSET;
        assertEquals(length, inputStream2.read(bytes, offset, length));
        assertEquals("llo", new String(bytes, offset, length));
    }

    @Test
    public void testRead1OnNullPointer() {
        assertThrows(NullPointerException.class, () -> inputStream.read(null, 0, 0));
        assertThrows(NullPointerException.class, () -> inputStream2.read(null, 0, 0));
    }

    @Test
    public void testRead1OnIndexOutOfBounds() {
        byte[] bytes = new byte[8];

        assertThrows(IndexOutOfBoundsException.class, () -> inputStream.read(bytes, -1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> inputStream2.read(bytes, -1, 0));

        assertThrows(IndexOutOfBoundsException.class, () -> inputStream.read(bytes, 0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> inputStream2.read(bytes, 0, -1));

        assertThrows(IndexOutOfBoundsException.class, () -> inputStream.read(bytes, 0, MAX_VALUE));
        assertThrows(IndexOutOfBoundsException.class, () -> inputStream2.read(bytes, 0, MAX_VALUE));
    }

    @Test
    public void testSkip() {
        assertEquals(0, inputStream.skip(0));
        assertEquals(1, inputStream.skip(1));
    }

    @Test
    public void testAvailable() throws IOException {
        testAvailable(inputStream, inputStream.available());
        testAvailable(inputStream2, inputStream2.available());
    }

    private void testAvailable(InputStream inputStream, int length) throws IOException {
        for (int i = 0; i < length; i++) {
            assertEquals(length - i, inputStream.available());
            inputStream.read();
        }
    }

    @Test
    public void testReset() {
        testRead();
        inputStream.reset();
        inputStream2.reset();
        assertEquals(TEST_VALUE.length(), inputStream.available());
        assertEquals(TEST_VALUE.length() - TEST_OFFSET, inputStream2.available());
    }
}