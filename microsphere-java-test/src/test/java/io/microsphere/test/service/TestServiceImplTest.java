package io.microsphere.test.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TestServiceImpl class.
 */
class TestServiceImplTest {

    private TestServiceImpl testService;
    private ApplicationContext mockContext;
    private Environment mockEnvironment;

    @BeforeEach
    void setUp() {
        // Mock dependencies
        mockContext = mock(ApplicationContext.class);
        mockEnvironment = mock(Environment.class);

        // Initialize the service with mocked dependencies
        testService = new TestServiceImpl(mockEnvironment);
        testService.context = mockContext; // Inject mocked ApplicationContext
    }

    /**
     * Test the echo method.
     * Verifies that the method correctly prefixes the input message with "[ECHO] ".
     */
    @Test
    void testEcho() {
        String input = "Hello, World!";
        String expectedOutput = "[ECHO] Hello, World!";
        String actualOutput = testService.echo(input);
        assertEquals(expectedOutput, actualOutput, "The echo method should prefix the message with '[ECHO] '.");
    }

    /**
     * Test the close method.
     * Verifies that the method does not throw any exceptions.
     */
    @Test
    void testClose() {
        assertDoesNotThrow(() -> testService.close(), "The close method should not throw any exceptions.");
    }

    /**
     * Test constructor with Environment parameter.
     * Verifies that the environment is properly injected.
     */
    @Test
    void testConstructorWithEnvironment() {
        assertNotNull(testService.environment, "The environment should be injected via constructor.");
        assertSame(mockEnvironment, testService.environment, "The injected environment should match the mocked instance.");
    }

    /**
     * Test default constructor.
     * Verifies that the service can be instantiated without an Environment.
     */
    @Test
    void testDefaultConstructor() {
        TestServiceImpl service = new TestServiceImpl();
        assertNull(service.environment, "The environment should be null when using the default constructor.");
    }
}
