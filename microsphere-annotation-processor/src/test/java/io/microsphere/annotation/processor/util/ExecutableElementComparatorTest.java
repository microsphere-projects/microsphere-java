package io.microsphere.annotation.processor.util;

import io.microsphere.annotation.processor.AbstractAnnotationProcessingTest;
import io.microsphere.annotation.processor.model.Model;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import static io.microsphere.annotation.processor.util.ExecutableElementComparator.INSTANCE;
import static io.microsphere.annotation.processor.util.MethodUtils.findMethod;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ExecutableElementComparator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ExecutableElementComparator
 * @since 1.0.0
 */
public class ExecutableElementComparatorTest extends AbstractAnnotationProcessingTest {

    private final ExecutableElementComparator comparator = INSTANCE;

    @Override
    protected void addCompiledClasses(Set<Class<?>> compiledClasses) {
        compiledClasses.add(Model.class);
    }

    @Test
    public void testCompare() {
        TypeElement type = getTypeElement(Model.class);
        // Test methods from java.lang.Object
        // Object#toString()
        String toStringMethodName = "toString";
        ExecutableElement toStringMethod = findMethod(type.asType(), toStringMethodName);

        String hashCodeMethodName = "hashCode";
        ExecutableElement hashCodeMethod = findMethod(type.asType(), hashCodeMethodName);
        assertEquals(0, comparator.compare(toStringMethod, toStringMethod));
        assertEquals(0, comparator.compare(hashCodeMethod, hashCodeMethod));
        assertEquals(toStringMethodName.compareTo(hashCodeMethodName), comparator.compare(toStringMethod, hashCodeMethod));

        // Object#equals
        assertEquals(0, comparator.compare(findMethod(getTypeMirror(getClass()), "equals", Object.class),
                findMethod(getTypeMirror(Object.class), "equals", Object.class)));
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }
}