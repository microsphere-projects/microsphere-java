package io.microsphere.convert;

import java.time.Duration;

import static io.microsphere.convert.StringToDurationConverter.INSTANCE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * {@link StringToDurationConverter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see StringToDurationConverter
 * @since 1.0.0
 */
class StringToDurationConverterTest extends BaseConverterTest<String, Duration> {

    @Override
    protected AbstractConverter<String, Duration> createConverter() {
        return INSTANCE;
    }

    @Override
    protected String getSource() throws Throwable {
        return "PT12.345S";
    }

    @Override
    protected Duration getTarget() throws Throwable {
        return Duration.ofSeconds(12, MILLISECONDS.toNanos(345));
    }
}