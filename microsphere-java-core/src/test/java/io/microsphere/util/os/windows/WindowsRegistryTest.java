/**
 *
 */
package io.microsphere.util.os.windows;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link WindowsRegistry} {@link Test}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see WindowsRegistry
 * @since 1.0.0
 */
public class WindowsRegistryTest {

    @Test
    public void testInit() {
        if (SystemUtils.IS_OS_WINDOWS) {
            WindowsRegistry user = WindowsRegistry.currentUser();
            String key = "\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
            String name = "ABC";
            String value = "value.exe";
            user.set(key, name, value);
            user.flush(key);
            assertEquals(value, user.get(key, name));
            user.remove(key, name);
        }
    }
}
