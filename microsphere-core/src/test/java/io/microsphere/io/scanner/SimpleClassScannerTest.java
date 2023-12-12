/**
 * 
 */
package io.microsphere.io.scanner;

import io.microsphere.AbstractTestCase;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@link SimpleClassScannerTest}
 *
 * @author <a href="mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see SimpleClassScannerTest
 * @since 1.0.0
 */
public class SimpleClassScannerTest extends AbstractTestCase {

    private SimpleClassScanner simpleClassScanner = SimpleClassScanner.INSTANCE;

    @Test
    public void testScan() {
        Set<Class<?>> classesSet = simpleClassScanner.scan(classLoader, "io.microsphere.commons");
        assertFalse(classesSet.isEmpty());
        info(classesSet);

        classesSet = simpleClassScanner.scan(classLoader, "io.microsphere.io.scanner");
        assertFalse(classesSet.isEmpty());
        info(classesSet);
    }
}
