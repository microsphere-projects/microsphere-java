package io.microsphere.test.service;

import io.microsphere.test.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for the DefaultTestService class.
 */
class DefaultTestServiceTest {

    private DefaultTestService defaultTestService;

    @BeforeEach
    void setUp() {
        defaultTestService = new DefaultTestService();
    }

    /**
     * Test the echo method.
     * Verifies that the method correctly prefixes the input message with "[ECHO] ".
     */
    @Test
    void testEcho() {
        String input = "Hello, World!";
        String expectedOutput = "[ECHO] Hello, World!";
        String actualOutput = defaultTestService.echo(input);
        assertEquals(expectedOutput, actualOutput, "The echo method should prefix the message with '[ECHO] '.");
    }

    /**
     * Test the model method.
     * Verifies that the method returns the same Model object that was passed in.
     */
    @Test
    void testModel() {
        Model inputModel = new Model();
        Model outputModel = defaultTestService.model(inputModel);
        assertSame(inputModel, outputModel, "The model method should return the same Model object.");
    }

    /**
     * Test the testPrimitive method.
     * Currently, this method returns null, so the test verifies that null is returned.
     */
    @Test
    void testTestPrimitive() {
        boolean z = true;
        int i = 42;
        String result = defaultTestService.testPrimitive(z, i);
        assertNull(result, "The testPrimitive method should return null.");
    }

    /**
     * Test the testEnum method.
     * Currently, this method returns null, so the test verifies that null is returned.
     */
    @Test
    void testTestEnum() {
        TimeUnit timeUnit = TimeUnit.SECONDS;
        Model result = defaultTestService.testEnum(timeUnit);
        assertNull(result, "The testEnum method should return null.");
    }

    /**
     * Test the testArray method.
     * Currently, this method returns null, so the test verifies that null is returned.
     */
    @Test
    void testTestArray() {
        String[] strArray = {"a", "b", "c"};
        int[] intArray = {1, 2, 3};
        Model[] modelArray = {new Model(), new Model()};
        String result = defaultTestService.testArray(strArray, intArray, modelArray);
        assertNull(result, "The testArray method should return null.");
    }
}
