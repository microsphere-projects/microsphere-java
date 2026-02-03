package io.microsphere.test.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the SimpleTypeModel class
 */
class SimpleTypeModelTest {

    private SimpleTypeModel model;

    @BeforeEach
    void setUp() {
        model = new SimpleTypeModel();
    }

    @Test
    void testDefaultValues() {
        // Verify all object fields are null by default
        assertNull(model.getV(), "Void field v should be null by default");
        assertNull(model.getZ(), "Boolean field z should be null by default");
        assertNull(model.getC(), "Character field c should be null by default");
        assertNull(model.getB(), "Byte field b should be null by default");
        assertNull(model.getS(), "Short field s should be null by default");
        assertNull(model.getI(), "Integer field i should be null by default");
        assertNull(model.getL(), "Long field l should be null by default");
        assertNull(model.getF(), "Float field f should be null by default");
        assertNull(model.getD(), "Double field d should be null by default");
        assertNull(model.getStr(), "String field str should be null by default");
        assertNull(model.getBd(), "BigDecimal field bd should be null by default");
        assertNull(model.getBi(), "BigInteger field bi should be null by default");
        assertNull(model.getDt(), "Date field dt should be null by default");
    }

    @Test
    void testSetGetVoid() {
        // Void is always null, so setting it to null should work
        model.setV(null);
        assertNull(model.getV(), "Void should always be null");
    }

    @Test
    void testSetGetBoolean() {
        Boolean testValue = Boolean.TRUE;
        model.setZ(testValue);
        assertEquals(testValue, model.getZ(), "getZ should return the set value");

        model.setZ(Boolean.FALSE);
        assertEquals(Boolean.FALSE, model.getZ(), "getZ should return the set value");
    }

    @Test
    void testSetGetCharacter() {
        Character testValue = 'A';
        model.setC(testValue);
        assertEquals(testValue, model.getC(), "getC should return the set value");
    }

    @Test
    void testSetGetByte() {
        Byte testValue = (byte) 42;
        model.setB(testValue);
        assertEquals(testValue, model.getB(), "getB should return the set value");
    }

    @Test
    void testSetGetShort() {
        Short testValue = (short) 1000;
        model.setS(testValue);
        assertEquals(testValue, model.getS(), "getS should return the set value");
    }

    @Test
    void testSetGetInteger() {
        Integer testValue = 123456;
        model.setI(testValue);
        assertEquals(testValue, model.getI(), "getI should return the set value");
    }

    @Test
    void testSetGetLong() {
        Long testValue = 9876543210L;
        model.setL(testValue);
        assertEquals(testValue, model.getL(), "getL should return the set value");
    }

    @Test
    void testSetGetFloat() {
        Float testValue = 3.14f;
        model.setF(testValue);
        assertEquals(testValue, model.getF(), "getF should return the set value");
    }

    @Test
    void testSetGetDouble() {
        Double testValue = 2.71828d;
        model.setD(testValue);
        assertEquals(testValue, model.getD(), "getD should return the set value");
    }

    @Test
    void testSetGetString() {
        String testValue = "Hello World";
        model.setStr(testValue);
        assertEquals(testValue, model.getStr(), "getStr should return the set value");
    }

    @Test
    void testSetGetBigDecimal() {
        BigDecimal testValue = new BigDecimal("1234567890.12345678901234567890");
        model.setBd(testValue);
        assertEquals(testValue, model.getBd(), "getBd should return the set value");
    }

    @Test
    void testSetGetBigInteger() {
        BigInteger testValue = new BigInteger("123456789012345678901234567890");
        model.setBi(testValue);
        assertEquals(testValue, model.getBi(), "getBi should return the set value");
    }

    @Test
    void testSetGetDate() {
        Date testValue = new Date();
        model.setDt(testValue);
        assertEquals(testValue, model.getDt(), "getDt should return the set value");
    }

    @Test
    void testNullHandling() {
        // Test setting all fields to null
        model.setZ(null);
        model.setC(null);
        model.setB(null);
        model.setS(null);
        model.setI(null);
        model.setL(null);
        model.setF(null);
        model.setD(null);
        model.setStr(null);
        model.setBd(null);
        model.setBi(null);
        model.setDt(null);

        assertNull(model.getZ(), "z should be null after setting to null");
        assertNull(model.getC(), "c should be null after setting to null");
        assertNull(model.getB(), "b should be null after setting to null");
        assertNull(model.getS(), "s should be null after setting to null");
        assertNull(model.getI(), "i should be null after setting to null");
        assertNull(model.getL(), "l should be null after setting to null");
        assertNull(model.getF(), "f should be null after setting to null");
        assertNull(model.getD(), "d should be null after setting to null");
        assertNull(model.getStr(), "str should be null after setting to null");
        assertNull(model.getBd(), "bd should be null after setting to null");
        assertNull(model.getBi(), "bi should be null after setting to null");
        assertNull(model.getDt(), "dt should be null after setting to null");
    }

    @Test
    void testMultipleValueChanges() {
        // Test changing values multiple times
        model.setI(100);
        assertEquals(Integer.valueOf(100), model.getI(), "i should be 100");

        model.setI(200);
        assertEquals(Integer.valueOf(200), model.getI(), "i should be 200 after second assignment");

        model.setStr("first");
        assertEquals("first", model.getStr(), "str should be 'first'");

        model.setStr("second");
        assertEquals("second", model.getStr(), "str should be 'second' after second assignment");
    }
}
