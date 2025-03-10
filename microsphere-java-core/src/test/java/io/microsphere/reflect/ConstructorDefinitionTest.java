package io.microsphere.reflect;

import io.microsphere.lang.Deprecation;
import io.microsphere.util.Version;
import org.junit.jupiter.api.Test;

import static io.microsphere.lang.DeprecationTest.DEPRECATION;
import static io.microsphere.lang.DeprecationTest.SINCE;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.Version.ofVersion;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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

    static class TestData {

        private final String name;

        public TestData() {
            this(null);
        }

        public TestData(String name) {
            this.name = name;
        }
    }

    @Test
    public void test() {
        assertConstructorDefinition(SINCE, DEPRECATION);
        assertConstructorDefinition(SINCE, DEPRECATION, "test");
        assertConstructorDefinition(ofVersion(SINCE), DEPRECATION, "test");

        assertConstructorDefinition(SINCE);
        assertConstructorDefinition(SINCE, "test");
        assertConstructorDefinition(ofVersion(SINCE), "test");
    }
    
    private void assertConstructorDefinition(String since, Object... args) {
        assertConstructorDefinition(since, null, args);
    }

    private void assertConstructorDefinition(Version since, Object... args) {
        assertConstructorDefinition(since, null, args);
    }

    private void assertConstructorDefinition(String since, Deprecation deprecation, Object... args) {
        String className = getTypeName(TestData.class);
        String[] parameterClassNames = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterClassNames[i] = getTypeName(args[i].getClass());
        }
        ConstructorDefinition cd =
                deprecation == null ? new ConstructorDefinition(since, className, parameterClassNames) :
                        new ConstructorDefinition(since, deprecation, className, parameterClassNames);

        assertConstructorDefinition(cd, className, parameterClassNames, ofVersion(since), deprecation, args);
    }


    private void assertConstructorDefinition(Version since, Deprecation deprecation, Object... args) {
        String className = getTypeName(TestData.class);
        String[] parameterClassNames = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterClassNames[i] = getTypeName(args[i].getClass());
        }
        ConstructorDefinition cd =
                deprecation == null ? new ConstructorDefinition(since, className, parameterClassNames) :
                        new ConstructorDefinition(since, deprecation, className, parameterClassNames);
        assertConstructorDefinition(cd, className, parameterClassNames, since, deprecation, args);
    }

    private void assertConstructorDefinition(ConstructorDefinition cd, String className, String[] parameterClassNames,
                                             Version since, Deprecation deprecation, Object... args) {

        assertEquals(since, cd.getSince());
        assertEquals(className, cd.getDeclaredClassName());
        assertArrayEquals(parameterClassNames, cd.getParameterClassNames());
        assertEquals(cd, new ConstructorDefinition(since, deprecation, className, parameterClassNames));
        assertNotNull(cd.toString());
        assertNotNull(cd.hashCode());
        assertNotNull(cd.getConstructor());
        assertNotNull(cd.getDeclaredClass());
        assertNotNull(cd.getParameterTypes());

        TestData testData = cd.newInstance(args);
        assertNotNull(testData);
        assertEquals(args.length == 0 ? null : args[0], testData.name);
    }

}