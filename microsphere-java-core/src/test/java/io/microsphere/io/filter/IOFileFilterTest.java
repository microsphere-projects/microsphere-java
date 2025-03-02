package io.microsphere.io.filter;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link IOFileFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see IOFileFilter
 * @since 1.0.0
 */
public class IOFileFilterTest {
    
    @Test
    public void tes() {
        File file = new File("test");
        IOFileFilter filter = f -> Objects.equals(f, file);
        assertTrue(filter.accept(file));
        assertFalse(filter.accept(null));
        assertTrue(filter.accept(file.getParentFile(), file.getName()));
    }

}