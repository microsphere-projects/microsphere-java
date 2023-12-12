package io.microsphere.classloading;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link WindowsRedefinedClassLoader} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class WindowsRedefinedClassLoaderTest {

    private static final String TEST_CLASS_NAME1 = "io.microsphere.classloading.WindowsRedefinedClassLoaderTest";

    private static final String TEST_CLASS_NAME2 = "io.microsphere.classloading.AConfig";

    @Test
    public void testLoadClass() throws ClassNotFoundException {
        WindowsRedefinedClassLoader classLoader = new WindowsRedefinedClassLoader(getClass().getClassLoader());
        classLoader.loadClass(TEST_CLASS_NAME1);
    }

    @Test
    public void testLoadAbsentClass() {
        assertThrows(ClassNotFoundException.class, () -> {
            WindowsRedefinedClassLoader classLoader = new WindowsRedefinedClassLoader(getClass().getClassLoader());
            classLoader.loadClass(TEST_CLASS_NAME2);
        });
    }
}
