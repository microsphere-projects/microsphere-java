/**
 *
 */
package io.microsphere.util;

import org.junit.jupiter.api.Test;

import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PropertyResourceBundleUtils} {@link Test}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PropertyResourceBundleUtilsTest
 * @since 1.0.0
 */
public class PropertyResourceBundleUtilsTest {

    @Test
    public void testGetBundle() {
        ResourceBundle resourceBundle = PropertyResourceBundleUtils.getBundle("META-INF.test", "UTF-8");
        String expected = "测试名称";
        String value = resourceBundle.getString("name");
        assertEquals(expected, value);
    }

}
