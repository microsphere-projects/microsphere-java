package io.microsphere.management;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Map;
import java.util.function.Consumer;

import static io.microsphere.management.JmxUtils.getMBeanAttributesMap;
import static io.microsphere.management.JmxUtils.getMBeanInfo;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static javax.management.ObjectName.getInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link MBeanAttribute} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MBeanAttribute
 * @since 1.0.0
 */
class MBeanAttributeTest {

    private static MBeanServer mBeanServer;

    private static ObjectName objectName;

    private static ObjectName notFoundObjectName;

    private static String notFoundAttributeName;

    private static Map<String, MBeanAttribute> mBeanAttributesMap;

    @BeforeAll
    static void beforeAll() throws Throwable {
        mBeanServer = getPlatformMBeanServer();
        objectName = getInstance("java.lang:type=ClassLoading");
        notFoundObjectName = getInstance("java.lang:type=NotFound");
        notFoundAttributeName = "NotFound";
        mBeanAttributesMap = getMBeanAttributesMap(mBeanServer, objectName);
    }

    @Test
    void testGetDeclaringMBeanInfo() {
        MBeanInfo mbeanInfo = getMBeanInfo(mBeanServer, objectName);
        forEach(mBeanAttribute -> assertSame(mbeanInfo, mBeanAttribute.getDeclaringMBeanInfo()));
    }

    @Test
    void testGetName() {
        forEach(mBeanAttribute -> assertEquals(mBeanAttribute.getAttributeInfo().getName(), mBeanAttribute.getName()));
    }

    @Test
    void testGetType() {
        forEach(mBeanAttribute -> assertEquals(mBeanAttribute.getAttributeInfo().getType(), mBeanAttribute.getType()));
    }

    @Test
    void testIsReadable() {
        forEach(mBeanAttribute -> assertEquals(mBeanAttribute.getAttributeInfo().isReadable(), mBeanAttribute.isReadable()));
    }

    @Test
    void testIsWritable() {
        forEach(mBeanAttribute -> assertEquals(mBeanAttribute.getAttributeInfo().isWritable(), mBeanAttribute.isWritable()));

    }

    @Test
    void testIsIs() {
        forEach(mBeanAttribute -> assertEquals(mBeanAttribute.getAttributeInfo().isIs(), mBeanAttribute.isIs()));

    }

    @Test
    void testGetAttributeInfo() {
        forEach(mBeanAttribute -> assertNotNull(mBeanAttribute.getAttributeInfo()));
    }

    @Test
    void testGetValue() {
        forEach(mBeanAttribute -> assertNotNull(mBeanAttribute.getValue()));
    }

    private void forEach(Consumer<MBeanAttribute> mBeanAttributeConsumer) {
        for (Map.Entry<String, MBeanAttribute> entry : mBeanAttributesMap.entrySet()) {
            MBeanAttribute mBeanAttribute = entry.getValue();
            mBeanAttributeConsumer.accept(mBeanAttribute);
        }
    }
}