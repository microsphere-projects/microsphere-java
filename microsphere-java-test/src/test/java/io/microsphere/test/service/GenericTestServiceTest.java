package io.microsphere.test.service;

import io.microsphere.test.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the GenericTestService class.
 */
class GenericTestServiceTest {

    private GenericTestService genericTestService;

    @BeforeEach
    void setUp() {
        genericTestService = new GenericTestService();
    }

    /**
     * Test the echo method.
     * Verifies that the method correctly prefixes the input message with "[ECHO] ".
     */
    @Test
    void testEcho() {
        String input = "Hello, World!";
        String expectedOutput = "[ECHO] Hello, World!";
        String actualOutput = genericTestService.echo(input);
        assertEquals(expectedOutput, actualOutput, "The echo method should prefix the message with '[ECHO] '.");
    }

    /**
     * Test inheritance from DefaultTestService.
     * Verifies that methods from the parent class are accessible and functional.
     */
    @Test
    void testInheritedMethods() {
        // Example: Testing a method inherited from DefaultTestService
        // Assuming DefaultTestService has a method like model(Model model)
        // Uncomment and adapt the following lines if such a method exists:
        Model inputModel = new Model();
        Model outputModel = genericTestService.model(inputModel);
        assertSame(inputModel, outputModel, "The inherited model method should return the same Model object.");
    }

    /**
     * Test implementation of EventListener interface.
     * Verifies that the class correctly implements the EventListener marker interface.
     */
    @Test
    void testEventListenerImplementation() {
        assertTrue(genericTestService instanceof java.util.EventListener,
                "GenericTestService should implement the EventListener interface.");
    }
}
