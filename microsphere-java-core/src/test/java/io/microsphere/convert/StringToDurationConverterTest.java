package io.microsphere.convert;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link StringToDurationConverter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see StringToDurationConverter
 * @since 1.0.0
 */
public class StringToDurationConverterTest {

    @Test
    public void testConvert() {
        StringToDurationConverter converter = new StringToDurationConverter();
        Duration duration = converter.convert("PT12.345S");
        assertEquals(12, duration.getSeconds());
        assertEquals(MILLISECONDS.toNanos(345), duration.getNano());
    }
}