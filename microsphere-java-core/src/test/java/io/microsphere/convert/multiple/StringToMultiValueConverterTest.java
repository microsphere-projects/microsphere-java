package io.microsphere.convert.multiple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * {@link StringToMultiValueConverter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see StringToMultiValueConverter
 * @since
 */
class StringToMultiValueConverterTest {

    private StringToMultiValueConverter converter;

    @BeforeEach
    void setUp() {
        converter = new StringToMultiValueConverter() {
            @Override
            public boolean accept(Class<String> sourceType, Class<?> multiValueType) {
                return false;
            }

            @Override
            public Object convert(String[] segments, int size, Class<?> targetType, Class<?> elementType) {
                return segments;
            }
        };
    }

    @Test
    void testConvert() {
        String source = "";
        assertArrayEquals(EMPTY_STRING_ARRAY, (String[]) converter.convert(source, null, null));

        source = "a,b,c";
        assertArrayEquals(ofArray("a", "b", "c"), (String[]) converter.convert(source, null, null));
    }

    @Test
    void testConvertOnNull() {
        assertArrayEquals(null, (String[]) converter.convert(null, null, null));
    }
}