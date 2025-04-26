package io.microsphere.reflect;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link ConstructorDefinition} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConstructorDefinition
 * @since 1.0.0
 */
public class ConstructorDefinitionTest extends AbstractExecutableDefinitionTest<ConstructorDefinition> {

    public static class TestData {

        private String name;

        public TestData(String name) {
            this.name = name;
        }
    }

    @Override
    protected String getClassName() {
        return TestData.class.getName();
    }

    @Override
    protected List<Object> getTailConstructorArguments() {
        return singletonList(ofArray("java.lang.String"));
    }

    @Test
    public void testGetConstructor() {
        for (ConstructorDefinition definition : definitions) {
            assertNotNull(definition.getConstructor());
        }
    }

    @Test
    public void testNewInstance() {
        for (ConstructorDefinition definition : definitions) {
            TestData testData = definition.newInstance("test");
            assertEquals("test", testData.name);
        }
    }

}