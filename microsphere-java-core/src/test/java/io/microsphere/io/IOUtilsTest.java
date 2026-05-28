package io.microsphere.io;

import io.microsphere.LoggingTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

import static io.microsphere.AbstractTestCase.TEST_NULL_STRING;
import static io.microsphere.io.IOUtils.BUFFER_SIZE;
import static io.microsphere.io.IOUtils.DEFAULT_BUFFER_SIZE;
import static io.microsphere.io.IOUtils.close;
import static io.microsphere.io.IOUtils.copy;
import static io.microsphere.io.IOUtils.copyToString;
import static io.microsphere.io.IOUtils.readLines;
import static io.microsphere.io.IOUtils.toByteArray;
import static io.microsphere.nio.charset.CharsetUtils.DEFAULT_CHARSET;
import static io.microsphere.util.ClassLoaderUtils.getResource;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static io.microsphere.util.SystemUtils.FILE_ENCODING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link IOUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see IOUtils
 * @since 1.0.0
 */
class IOUtilsTest extends LoggingTest {

    private static final String TEST_VALUE = "Hello";

    private static final byte[] TEST_BYTES = TEST_VALUE.getBytes();

    private FastByteArrayInputStream inputStream;

    @BeforeEach
    void setUp() {
        inputStream = new FastByteArrayInputStream(TEST_BYTES);
    }

    @AfterEach
    void tearDown() {
        inputStream.close();
    }

    @Test
    void testConstants() {
        assertEquals(2048, DEFAULT_BUFFER_SIZE);
        assertEquals(2048, BUFFER_SIZE);
    }

    @Test
    void testReadLines() throws IOException {
        String[] lines = readLines(inputStream);
        assertEquals(1, lines.length);
        assertEquals(TEST_VALUE, lines[0]);
    }

    @Test
    void testReadLinesOnMultipleLines() throws IOException {
        URL resource = getResource("META-INF/services/io.microsphere.event.EventListener");
        try (InputStream inputStream = resource.openStream()) {
            String[] lines = readLines(inputStream);
            assertEquals(2, lines.length);
            assertEquals("io.microsphere.event.EchoEventListener", lines[0]);
            assertEquals("io.microsphere.event.EchoEventListener2", lines[1]);
        }
    }

    @Test
    void testToByteArray() throws IOException {
        assertArrayEquals(TEST_BYTES, toByteArray(inputStream));
    }

    @Test
    void testToStringWithInputStream() throws IOException {
        assertEquals(TEST_VALUE, IOUtils.toString(inputStream));
    }

    @Test
    void testToStringWithInputStreamAndEncoding() throws IOException {
        assertEquals(TEST_VALUE, IOUtils.toString(inputStream, FILE_ENCODING));
    }

    @Test
    void testToStringWithInputStreamAndCharset() throws IOException {
        assertEquals(TEST_VALUE, IOUtils.toString(inputStream, DEFAULT_CHARSET));
    }

    @Test
    void testToStringOnNullInputStream() throws IOException {
        assertNull(IOUtils.toString((InputStream) null));
    }

    @Test
    void testToStringWithInputStreamAndNullEncoding() throws IOException {
        assertEquals(TEST_VALUE, IOUtils.toString(inputStream, TEST_NULL_STRING));
    }

    @Test
    void testToStringWithInputStreamAndEmptyEncoding() {
        assertThrows(IllegalCharsetNameException.class, () -> IOUtils.toString(inputStream, EMPTY_STRING));
    }

    @Test
    void testToStringWithInputStreamAndNullCharset() throws IOException {
        assertEquals(TEST_VALUE, IOUtils.toString(inputStream, (Charset) null));
    }

    @Test
    void testToStringWithNullInputStreamAndNullCharset() throws IOException {
        assertNull(IOUtils.toString(null, (Charset) null));
    }

    @Test
    void testToStringWithNullInputStreamAndNullEncoding() throws IOException {
        assertNull(IOUtils.toString(null, TEST_NULL_STRING));
    }

    @Test
    void testToStringWithWithReader() throws IOException {
        assertEquals(TEST_VALUE, IOUtils.toString(new StringReader(TEST_VALUE)));
    }

    @Test
    void testCopyToStringWithInputStreamAndNullEncoding() throws IOException {
        assertEquals(TEST_VALUE, copyToString(inputStream, TEST_NULL_STRING));
    }

    @Test
    void testCopyToStringWithInputStreamAndEmptyEncoding() {
        assertThrows(IllegalCharsetNameException.class, () -> copyToString(inputStream, EMPTY_STRING));
    }

    @Test
    void testCopyToStringWithInputStreamAndEncoding() throws IOException {
        assertEquals(TEST_VALUE, copyToString(inputStream, FILE_ENCODING));
    }

    @Test
    void testCopyToStringWithInputStreamAndNullCharset() throws IOException {
        assertEquals(TEST_VALUE, copyToString(inputStream, (Charset) null));
    }

    @Test
    void testCopyToStringWithInputStreamAndCharset() throws IOException {
        assertEquals(TEST_VALUE, copyToString(inputStream, UTF_8));
    }

    @Test
    void testCopyToStringWithInputStreamAndDefaultCharset() throws IOException {
        assertEquals(TEST_VALUE, copyToString(inputStream));
    }

    @Test
    void testCopyToStringWithNullInputStream() throws IOException {
        assertNull(copyToString((InputStream) null));
    }

    @Test
    void testCopyToStringOnNull() throws IOException {
        assertNull(copyToString(null, (Charset) null));
    }

    @Test
    void testCopyToStringOnNull1() throws IOException {
        assertNull(copyToString(null, TEST_NULL_STRING));
    }

    @Test
    void testCopyToStringWithReader() throws IOException {
        assertEquals(TEST_VALUE, copyToString(new StringReader(TEST_VALUE)));
    }

    @Test
    void testCopyToStringWithNullReader() throws IOException {
        assertNull(copyToString((Reader) null));
    }


    @Test
    void testCopy() throws IOException {
        FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream(TEST_BYTES.length);
        copy(inputStream, outputStream);
        assertArrayEquals(TEST_BYTES, outputStream.toByteArray());
    }

    @Test
    void testClose() {
        tearDown();
        close(new FastByteArrayOutputStream(0));
    }

    @Test
    void testCloseOnNull() {
        close(null);
    }

    @Test
    void testCloseOnIOException() {
        close(() -> {
            throw new IOException("For testing");
        });
    }
}