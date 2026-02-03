package io.microsphere.test.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the Model class
 */
class ModelTest {

    private Model model;

    @BeforeEach
    void setUp() {
        model = new Model();
    }

    @Test
    void testDefaultValues() {
        // Verify all primitive fields have their default values
        assertEquals(0.0f, model.getF(), "float field f should be 0.0f by default");
        assertEquals(0.0d, model.getD(), "double field d should be 0.0d by default");
        
        // Verify all object fields are null by default
        assertNull(model.getTu(), "TimeUnit field tu should be null by default");
        assertNull(model.getStr(), "String field str should be null by default");
        assertNull(model.getBi(), "BigInteger field bi should be null by default");
        assertNull(model.getBd(), "BigDecimal field bd should be null by default");
    }

    @Test
    void testSetGetFloat() {
        float testValue = 3.14f;
        model.setF(testValue);
        assertEquals(testValue, model.getF(), "getF should return the set value");
    }

    @Test
    void testSetGetDouble() {
        double testValue = 2.71828d;
        model.setD(testValue);
        assertEquals(testValue, model.getD(), "getD should return the set value");
    }

    @Test
    void testSetGetTimeUnit() {
        TimeUnit testValue = TimeUnit.SECONDS;
        model.setTu(testValue);
        assertEquals(testValue, model.getTu(), "getTu should return the set value");
    }

    @Test
    void testSetGetString() {
        String testValue = "Hello World";
        model.setStr(testValue);
        assertEquals(testValue, model.getStr(), "getStr should return the set value");
    }

    @Test
    void testSetGetBigInteger() {
        BigInteger testValue = new BigInteger("123456789012345678901234567890");
        model.setBi(testValue);
        assertEquals(testValue, model.getBi(), "getBi should return the set value");
    }

    @Test
    void testSetGetBigDecimal() {
        BigDecimal testValue = new BigDecimal("1234567890.12345678901234567890");
        model.setBd(testValue);
        assertEquals(testValue, model.getBd(), "getBd should return the set value");
    }

    @Test
    void testInheritanceFromParent() {
        // Verify that the model inherits from Parent class
        assertTrue(model instanceof Parent, "Model should extend Parent class");
    }

    @Test
    void testMultipleValueChanges() {
        // Test changing values multiple times
        model.setF(1.0f);
        assertEquals(1.0f, model.getF(), "f should be 1.0f");
        
        model.setF(2.0f);
        assertEquals(2.0f, model.getF(), "f should be 2.0f after second assignment");
        
        model.setD(10.0d);
        assertEquals(10.0d, model.getD(), "d should be 10.0d");
        
        model.setD(20.0d);
        assertEquals(20.0d, model.getD(), "d should be 20.0d after second assignment");
    }

    @Test
    void testNullHandling() {
        // Test setting object fields to null
        model.setTu(null);
        model.setStr(null);
        model.setBi(null);
        model.setBd(null);
        
        assertNull(model.getTu(), "tu should be null after setting to null");
        assertNull(model.getStr(), "str should be null after setting to null");
        assertNull(model.getBi(), "bi should be null after setting to null");
        assertNull(model.getBd(), "bd should be null after setting to null");
    }
}
