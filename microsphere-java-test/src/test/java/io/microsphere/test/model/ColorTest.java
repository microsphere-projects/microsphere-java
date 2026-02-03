package io.microsphere.test.model;

import org.junit.jupiter.api.Test;

import static io.microsphere.test.model.Color.BLUE;
import static io.microsphere.test.model.Color.RED;
import static io.microsphere.test.model.Color.YELLOW;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the Color enum
 */
class ColorTest {

    @Test
    void testEnumValues() {
        // Verify that all enum constants exist and are accessible
        assertNotNull(RED, "RED color should exist");
        assertNotNull(YELLOW, "YELLOW color should exist");
        assertNotNull(BLUE, "BLUE color should exist");
    }

    @Test
    void testGetValueMethod() {
        // Verify the value associated with each color
        assertEquals(1, RED.getValue(), "RED should have value 1");
        assertEquals(2, YELLOW.getValue(), "YELLOW should have value 2");
        assertEquals(3, BLUE.getValue(), "BLUE should have value 3");
    }

    @Test
    void testToStringMethod() {
        // Verify the string representation of each color
        String redString = RED.toString();
        assertTrue(redString.contains("Color{value=1}"), "RED toString should contain value=1");
        assertTrue(redString.endsWith("RED"), "RED toString should end with RED");

        String yellowString = YELLOW.toString();
        assertTrue(yellowString.contains("Color{value=2}"), "YELLOW toString should contain value=2");
        assertTrue(yellowString.endsWith("YELLOW"), "YELLOW toString should end with YELLOW");

        String blueString = BLUE.toString();
        assertTrue(blueString.contains("Color{value=3}"), "BLUE toString should contain value=3");
        assertTrue(blueString.endsWith("BLUE"), "BLUE toString should end with BLUE");
    }

    @Test
    void testEnumOrdinality() {
        // Verify that enum constants have expected ordinal positions
        assertEquals(0, RED.ordinal(), "RED should be at ordinal position 0");
        assertEquals(1, YELLOW.ordinal(), "YELLOW should be at ordinal position 1");
        assertEquals(2, BLUE.ordinal(), "BLUE should be at ordinal position 2");
    }

    @Test
    void testEnumName() {
        // Verify that enum constants have correct names
        assertEquals("RED", RED.name(), "RED name should be 'RED'");
        assertEquals("YELLOW", YELLOW.name(), "YELLOW name should be 'YELLOW'");
        assertEquals("BLUE", BLUE.name(), "BLUE name should be 'BLUE'");
    }

    @Test
    void testEnumEquality() {
        // Verify that enum equality works correctly
        assertEquals(RED, RED, "RED should equal itself");
        assertEquals(YELLOW, YELLOW, "YELLOW should equal itself");
        assertEquals(BLUE, BLUE, "BLUE should equal itself");

        assertNotEquals(RED, BLUE, "RED should not equal BLUE");
        assertNotEquals(RED, YELLOW, "RED should not equal YELLOW");
        assertNotEquals(BLUE, YELLOW, "BLUE should not equal YELLOW");
    }

    @Test
    void testValueImmutability() {
        // Verify that the value cannot be changed (as it's final)
        int redValue = RED.getValue();
        int yellowValue = YELLOW.getValue();
        int blueValue = BLUE.getValue();

        // Values should remain constant across multiple calls
        assertEquals(redValue, RED.getValue(), "RED value should be immutable");
        assertEquals(yellowValue, YELLOW.getValue(), "YELLOW value should be immutable");
        assertEquals(blueValue, BLUE.getValue(), "BLUE value should be immutable");
    }

    @Test
    void testAllEnumConstantsExist() {
        // Verify that we can retrieve all enum constants
        Color[] allColors = Color.values();
        assertEquals(3, allColors.length, "There should be exactly 3 color constants");
        
        // Verify that all expected colors are present
        assertTrue(asList(allColors).contains(RED), "All colors should include RED");
        assertTrue(asList(allColors).contains(YELLOW), "All colors should include YELLOW");
        assertTrue(asList(allColors).contains(BLUE), "All colors should include BLUE");
    }
}
