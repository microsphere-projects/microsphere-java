package io.microsphere.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ServiceLoaderUtilsTest}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see ServiceLoaderUtilsTest
 * @since 1.0.0
 */
public class ServiceLoaderUtilsTest {

    @Test
    public void testLoadServicesList() throws Exception {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<CharSequence> charSequenceList = ServiceLoaderUtils.loadServicesList(CharSequence.class, classLoader);
        assertEquals(1, charSequenceList.size());

        CharSequence charSequence = charSequenceList.get(0);
        CharSequence firstService = ServiceLoaderUtils.loadFirstService(CharSequence.class, classLoader);
        CharSequence lastService = ServiceLoaderUtils.loadLastService(CharSequence.class, classLoader);

        assertNotNull(charSequence);
        assertEquals(charSequence, firstService);
        assertEquals(charSequence, lastService);
        assertEquals(firstService, lastService);

        String string = charSequence.toString();
        assertTrue(string.isEmpty());

        IllegalArgumentException e = null;

        try {
            ServiceLoaderUtils.loadServicesList(Set.class, classLoader);
        } catch (IllegalArgumentException e_) {
            e = e_;
            e.printStackTrace();
        }

        assertNotNull(e);

    }
}
