/**
 *
 */
package io.microsphere.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.PropertyResourceBundleControl.DEFAULT_CONTROL;
import static io.microsphere.util.PropertyResourceBundleControl.newControl;
import static io.microsphere.util.SystemUtils.FILE_ENCODING;
import static java.nio.charset.Charset.availableCharsets;
import static java.util.Locale.SIMPLIFIED_CHINESE;
import static java.util.ResourceBundle.Control.FORMAT_PROPERTIES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link PropertyResourceBundleControl} {@link Test}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PropertyResourceBundleControlTest
 * @since 1.0.0
 */
public class PropertyResourceBundleControlTest {

    private static final String BASE_NAME = "META-INF.test";

    private static final String TEST_FORMAT = FORMAT_PROPERTIES.get(0);

    private static final ClassLoader classLoader = getDefaultClassLoader();

    @Test
    public void testDefaultConstructor() {
        PropertyResourceBundleControl control = new PropertyResourceBundleControl();
        assertEquals(FILE_ENCODING, control.getEncoding());
        assertEquals(FILE_ENCODING, DEFAULT_CONTROL.getEncoding());
    }

    @Test
    public void testNewControl() {
        for (String encoding : availableCharsets().keySet()) {
            assertNotNull(newControl(encoding));
        }
    }

    @Test
    public void testNewControlOnException() {
        assertThrows(UnsupportedCharsetException.class, () -> newControl("NON-SUPPORTED-ENCODING"));
    }

    @Test
    public void testGetFormats() {
        assertSame(FORMAT_PROPERTIES, DEFAULT_CONTROL.getFormats("basename"));
    }

    @Test
    public void testGetFormatsOnNull() {
        assertThrows(NullPointerException.class, () -> DEFAULT_CONTROL.getFormats(null));
    }

    @Test
    public void testNewBundle() throws IOException {
        assertNewBundle(false);
    }

    @Test
    public void testNewBundleOnReload() throws IOException {
        assertNewBundle(true);
    }

    @Test
    public void testNewBundleOnNotFound() {
        assertThrows(IOException.class, () -> newBundle("Not-Found-Bundle", true));
        assertThrows(IOException.class, () -> newBundle("Not-Found-Bundle", false));
    }

    private void assertNewBundle(boolean reload) throws IOException {
        ResourceBundle bundle = newBundle(reload);
        assertEquals("测试名称", bundle.getString("name"));
        assertThrows(MissingResourceException.class, () -> bundle.getString("not-found"));
    }

    private ResourceBundle newBundle(boolean reload) throws IOException {
        return newBundle(BASE_NAME, reload);
    }

    private ResourceBundle newBundle(String baseName, boolean reload) throws IOException {
        return DEFAULT_CONTROL.newBundle(baseName, SIMPLIFIED_CHINESE, TEST_FORMAT, classLoader, reload);
    }
}
