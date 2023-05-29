package io.github.microsphere.util;

import junit.framework.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;

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
        Assert.assertEquals(1, charSequenceList.size());

        CharSequence charSequence = charSequenceList.get(0);
        CharSequence firstService = ServiceLoaderUtils.loadFirstService(CharSequence.class, classLoader);
        CharSequence lastService = ServiceLoaderUtils.loadLastService(CharSequence.class, classLoader);

        Assert.assertNotNull(charSequence);
        Assert.assertEquals(charSequence, firstService);
        Assert.assertEquals(charSequence, lastService);
        Assert.assertEquals(firstService, lastService);

        String string = charSequence.toString();
        Assert.assertTrue(string.isEmpty());

        IllegalArgumentException e = null;

        try {
            ServiceLoaderUtils.loadServicesList(Set.class, classLoader);
        } catch (IllegalArgumentException e_) {
            e = e_;
            e.printStackTrace();
        }

        Assert.assertNotNull(e);

    }
}
