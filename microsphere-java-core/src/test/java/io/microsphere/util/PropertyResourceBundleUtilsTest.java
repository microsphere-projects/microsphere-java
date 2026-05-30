/**
 *
 */
package io.microsphere.util;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static io.microsphere.AbstractTestCase.TEST_CLASS_LOADER;
import static io.microsphere.util.PropertyResourceBundleUtils.DEFAULT_ENCODING;
import static io.microsphere.util.PropertyResourceBundleUtils.DEFAULT_ENCODING_PROPERTY_NAME;
import static io.microsphere.util.PropertyResourceBundleUtils.getBundle;
import static io.microsphere.util.SystemUtils.FILE_ENCODING;
import static java.util.Locale.ROOT;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PropertyResourceBundleUtils} {@link Test}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PropertyResourceBundleUtils
 * @since 1.0.0
 */
class PropertyResourceBundleUtilsTest {

    private static final String BASE_NAME = "META-INF.test";

    private static final Locale TEST_LOCALE = ROOT;

    private static final String TEST_ENCODING = DEFAULT_ENCODING;

    @Test
    void testConstants() {
        assertEquals("java.util.PropertyResourceBundle.encoding", DEFAULT_ENCODING_PROPERTY_NAME);
        assertEquals(FILE_ENCODING, DEFAULT_ENCODING);
    }

    @Test
    void testGetBundleWithBaseName() {
        ResourceBundle resourceBundle = getBundle(BASE_NAME);
        assertResourceBundle(resourceBundle);
    }

    @Test
    void testGetBundleWithBaseNameAndEncoding() {
        ResourceBundle resourceBundle = getBundle(BASE_NAME, TEST_ENCODING);
        assertResourceBundle(resourceBundle);
    }

    @Test
    void testGetBundleWithBaseNameAndLocaleAndEncoding() {
        ResourceBundle resourceBundle = getBundle(BASE_NAME, TEST_LOCALE, TEST_ENCODING);
        assertResourceBundle(resourceBundle);
    }

    @Test
    void testGetBundleWithBaseNameAndLocaleAndEncodingAndClassLoader() {
        ResourceBundle resourceBundle = getBundle(BASE_NAME, TEST_LOCALE, TEST_CLASS_LOADER, TEST_ENCODING);
        assertResourceBundle(resourceBundle);
    }

    private void assertResourceBundle(ResourceBundle resourceBundle) {
        assertEquals("测试名称", resourceBundle.getString("name"));
    }

}
