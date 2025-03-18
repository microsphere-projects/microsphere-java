/**
 *
 */
package io.microsphere.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ResourceBundle;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link PropertyResourceBundleControl} {@link Test}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PropertyResourceBundleControlTest
 * @since 1.0.0
 */
public class PropertyResourceBundleControlTest {

    @Test
    public void testNewControl() {
        SortedMap<String, Charset> charsetsSortedMap = Charset.availableCharsets();
        for (String encoding : charsetsSortedMap.keySet()) {
            ResourceBundle.Control control = PropertyResourceBundleControl.newControl(encoding);
            assertNotNull(control);
        }
    }

    @Test
    public void testNewControlOnException() {
        assertThrows(UnsupportedCharsetException.class, () -> PropertyResourceBundleControl.newControl("NON-SUPPORTED-ENCODING"));
    }
}
