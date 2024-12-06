package io.microsphere.reflect;

import io.microsphere.util.Version;
import org.junit.jupiter.api.Test;

import static io.microsphere.lang.DeprecationTest.SINCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link ConstructorDefinition} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConstructorDefinition
 * @since 1.0.0
 */
public class ConstructorDefinitionTest {

    private static final String CLASS_NAME = "io.microsphere.reflect.ConstructorDefinitionTest";

    @Test
    public void test() {
        ConstructorDefinition cd = new ConstructorDefinition(SINCE, CLASS_NAME);

        assertEquals(Version.of(SINCE), cd.getSince());
        assertEquals(CLASS_NAME, cd.getDeclaredClassName());
        assertNotNull(cd.getConstructor());

        ConstructorDefinitionTest test = cd.newInstance();
        assertNotNull(test);
    }

}