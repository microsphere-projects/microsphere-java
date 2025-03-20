package io.microsphere.io;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;

import static io.microsphere.io.IOUtils.BUFFER_SIZE;
import static io.microsphere.io.IOUtils.DEFAULT_BUFFER_SIZE;
import static io.microsphere.io.IOUtils.close;
import static io.microsphere.io.IOUtils.copy;
import static io.microsphere.io.IOUtils.copyToString;
import static io.microsphere.io.IOUtils.toByteArray;
import static io.microsphere.util.SystemUtils.FILE_ENCODING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link IOUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see IOUtils
 * @since 1.0.0
 */
public class IOUtilsTest extends AbstractTestCase {

    private static final String TEST_VALUE = "Hello";

    private static final byte[] TEST_BYTES = TEST_VALUE.getBytes();

    private FastByteArrayInputStream inputStream;

    @BeforeEach
    public void init() {
        inputStream = new FastByteArrayInputStream(TEST_BYTES);
    }

    @AfterEach
    public void destroy() {
        inputStream.close();
    }

    @Test
    public void testConstants() {
        assertEquals(2048, DEFAULT_BUFFER_SIZE);
        assertEquals(2048, BUFFER_SIZE);
    }

    @Test
    public void testToByteArray() throws IOException {
        assertArrayEquals(TEST_BYTES, toByteArray(inputStream));
    }

    @Test
    public void testToStringOnNullEncoding() throws IOException {
        assertEquals(TEST_VALUE, IOUtils.toString(inputStream, TEST_NULL_STRING));
    }

    @Test
    public void testToStringOnEmptyEncoding() throws IOException {
        assertEquals(TEST_VALUE, IOUtils.toString(inputStream, ""));
    }

    @Test
    public void testToString() throws IOException {
        assertEquals(TEST_VALUE, IOUtils.toString(inputStream, FILE_ENCODING));
    }

    @Test
    public void testToString1OnNullCharset() throws IOException {
        assertEquals(TEST_VALUE, IOUtils.toString(inputStream, (Charset) null));
    }

    @Test
    public void testToString1() throws IOException {
        assertEquals(TEST_VALUE, IOUtils.toString(inputStream, UTF_8));
    }

    @Test
    public void testToStringOnDefaultCharset() throws IOException {
        assertEquals(TEST_VALUE, IOUtils.toString(inputStream));
    }


    @Test
    public void testToStringOnNullInputStream() throws IOException {
        assertNull(IOUtils.toString(null));
    }

    @Test
    public void testToStringOnNull() throws IOException {
        assertNull(IOUtils.toString(null, (Charset) null));
    }

    @Test
    public void testToStringOnNull1() throws IOException {
        assertNull(IOUtils.toString(null, TEST_NULL_STRING));
    }

    @Test
    public void testCopyToStringOnNullEncoding() throws IOException {
        assertEquals(TEST_VALUE, copyToString(inputStream, TEST_NULL_STRING));
    }

    @Test
    public void testCopyToStringOnEmptyEncoding() throws IOException {
        assertEquals(TEST_VALUE, copyToString(inputStream, ""));
    }

    @Test
    public void testCopyToString() throws IOException {
        assertEquals(TEST_VALUE, copyToString(inputStream, FILE_ENCODING));
    }

    @Test
    public void testCopyToString1OnNullCharset() throws IOException {
        assertEquals(TEST_VALUE, copyToString(inputStream, (Charset) null));
    }

    @Test
    public void testCopyToStringOnUTF8Charset() throws IOException {
        assertEquals(TEST_VALUE, copyToString(inputStream, UTF_8));
    }

    @Test
    public void testCopyToStringWithDefaultCharset() throws IOException {
        assertEquals(TEST_VALUE, copyToString(inputStream));
    }

    @Test
    public void testCopyToStringOnNullInputStream() throws IOException {
        assertNull(copyToString(null));
    }

    @Test
    public void testCopyToStringOnNull() throws IOException {
        assertNull(copyToString(null, (Charset) null));
    }

    @Test
    public void testCopyToStringOnNull1() throws IOException {
        assertNull(copyToString(null, TEST_NULL_STRING));
    }

    @Test
    public void testCopy() throws IOException {
        FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream(TEST_BYTES.length);
        copy(inputStream, outputStream);
        assertArrayEquals(TEST_BYTES, outputStream.toByteArray());
    }

    @Test
    public void testClose() {
        destroy();
        close(new FastByteArrayOutputStream(0));
    }

    @Test
    public void testCloseOnNull() {
        close(null);
    }

    @Test
    public void testCloseOnIOException() {
        close(() -> {
            throw new IOException("For testing");
        });
    }
}