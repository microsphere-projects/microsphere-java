/**
 * 
 */
package io.microsphere.io.scanner;

import io.microsphere.AbstractTestCase;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Set;

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
        Assert.assertFalse(classesSet.isEmpty());
        info(classesSet);

        classesSet = simpleClassScanner.scan(classLoader, "io.microsphere.io.scanner");
        Assert.assertFalse(classesSet.isEmpty());
        info(classesSet);
    }
}
