/**
 *
 */
package io.microsphere.util;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.PropertyResourceBundleUtils.DEFAULT_ENCODING;
import static io.microsphere.util.PropertyResourceBundleUtils.DEFAULT_ENCODING_PROPERTY_NAME;
import static io.microsphere.util.PropertyResourceBundleUtils.getBundle;
import static io.microsphere.util.SystemUtils.FILE_ENCODING;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PropertyResourceBundleUtils} {@link Test}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PropertyResourceBundleUtilsTest
 * @since 1.0.0
 */
public class PropertyResourceBundleUtilsTest {

    private static final String BASE_NAME = "META-INF.test";

    private static final Locale TEST_LOCALE = Locale.ROOT;

    private static final String TEST_ENCODING = DEFAULT_ENCODING;

    private static final ClassLoader TEST_CLASS_LOADER = getDefaultClassLoader();

    @Test
    public void testConstants() {
        assertEquals("java.util.PropertyResourceBundle.encoding", DEFAULT_ENCODING_PROPERTY_NAME);
        assertEquals(FILE_ENCODING, DEFAULT_ENCODING);
    }

    @Test
    public void testGetBundle() {
        ResourceBundle resourceBundle = getBundle("META-INF.test", "UTF-8");
        String expected = "测试名称";
        String value = resourceBundle.getString("name");
        assertEquals(expected, value);
    }

}
