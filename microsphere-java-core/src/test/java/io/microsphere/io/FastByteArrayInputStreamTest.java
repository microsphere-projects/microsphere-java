package io.microsphere.io;

import jdk.internal.util.xml.impl.Input;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

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