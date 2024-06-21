/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.management;

import io.microsphere.util.BaseUtils;
import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static io.microsphere.collection.MapUtils.newHashMap;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;


/**
 * The utilities class for JMX
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class JmxUtils extends BaseUtils {

    private static final Logger logger = LoggerFactory.getLogger(JmxUtils.class);

    private static final MBeanAttribute[] EMPTY_MBEAN_ATTRIBUTE_ARRAY = new MBeanAttribute[0];

    /**
     * Get the {@link Map} with the attribute name and {@link MBeanAttribute MBeanAttributes} from the specified named
     * MBean and its' registered {@link MBeanServer}
     *
     * @param mBeanServer {@link MBeanServer}
     * @param objectName  the name of MBean
     * @return non-null read-only {@link Map}
     */
    @Nonnull
    public static Map<String, MBeanAttribute> getMBeanAttributesMap(MBeanServer mBeanServer, ObjectName objectName) {
        MBeanAttribute[] mBeanAttributes = getMBeanAttributes(mBeanServer, objectName);
        int length = mBeanAttributes.length;
        if (length == 0) {
            return emptyMap();
        }
        Map<String, MBeanAttribute> mBeanAttributesMap = newHashMap(length);
        for (int i = 0; i < length; i++) {
            MBeanAttribute mBeanAttribute = mBeanAttributes[i];
            String attributeName = mBeanAttribute.getName();
            mBeanAttributesMap.put(attributeName, mBeanAttribute);
        }
        return unmodifiableMap(mBeanAttributesMap);
    }

    /**
     * Get the {@link MBeanAttribute MBeanAttributes} from the specified named MBean and its' registered {@link MBeanServer}
     * Note that the array of {@link MBeanAttribute MBeanAttributes} is the same order of {@link MBeanInfo#getAttributes()}
     *
     * @param mBeanServer {@link MBeanServer}
     * @param objectName  the name of MBean
     * @return non-null array
     */
    @Nonnull
    public static MBeanAttribute[] getMBeanAttributes(MBeanServer mBeanServer, ObjectName objectName) {
        MBeanInfo mBeanInfo = getMBeanInfo(mBeanServer, objectName);
        if (mBeanInfo == null) {
            return EMPTY_MBEAN_ATTRIBUTE_ARRAY;
        }
        MBeanAttributeInfo[] attributeInfoList = mBeanInfo.getAttributes();
        int size = attributeInfoList.length;
        MBeanAttribute[] mBeanAttributes = new MBeanAttribute[size];
        for (int i = 0; i < size; i++) {
            MBeanAttributeInfo attributeInfo = attributeInfoList[i];
            Object attributeValue = getAttribute(mBeanServer, objectName, attributeInfo);
            MBeanAttribute mBeanAttribute = new MBeanAttribute(mBeanInfo, attributeInfo, attributeValue);
            mBeanAttributes[i] = mBeanAttribute;
        }
        return mBeanAttributes;
    }

    public static Object getAttribute(MBeanServer mBeanServer, ObjectName objectName, MBeanAttributeInfo attributeInfo) {
        return doGetAttribute(mBeanServer, objectName, attributeInfo, attributeInfo.getName());
    }

    public static Object getAttribute(MBeanServer mBeanServer, ObjectName objectName, String attributeName) {
        return doGetAttribute(mBeanServer, objectName, null, attributeName);
    }

    public static MBeanAttributeInfo findMBeanAttributeInfo(MBeanServer mBeanServer, ObjectName objectName, String attributeName) {
        MBeanInfo mBeanInfo = getMBeanInfo(mBeanServer, objectName);
        if (mBeanInfo == null) {
            return null;
        }
        MBeanAttributeInfo targetAttributeInfo = null;
        MBeanAttributeInfo[] attributeInfoList = mBeanInfo.getAttributes();
        int size = attributeInfoList.length;
        for (int i = 0; i < size; i++) {
            MBeanAttributeInfo attributeInfo = attributeInfoList[i];
            if (Objects.equals(attributeName, attributeInfo.getName())) {
                targetAttributeInfo = attributeInfo;
                break;
            }
        }
        if (targetAttributeInfo == null) {
            handleAttributeNotFoundException(new AttributeNotFoundException(), mBeanServer, objectName, mBeanInfo, attributeName);
        }

        return targetAttributeInfo;
    }

    protected static Object doGetAttribute(MBeanServer mBeanServer, ObjectName objectName, @Nullable MBeanAttributeInfo attributeInfo,
                                           String attributeName) {
        MBeanAttributeInfo mBeanAttributeInfo = attributeInfo;
        if (mBeanAttributeInfo == null) {
            mBeanAttributeInfo = findMBeanAttributeInfo(mBeanServer, objectName, attributeName);
        }
        if (mBeanAttributeInfo == null) {
            return null;
        }

        if (!mBeanAttributeInfo.isReadable()) {
            return null;
        }
        Object attributeValue = null;
        try {
            attributeValue = mBeanServer.getAttribute(objectName, attributeName);
        } catch (InstanceNotFoundException e) {
            handleInstanceNotFoundException(e, mBeanServer, objectName);
        } catch (ReflectionException e) {
            handleReflectionException(e, mBeanServer, objectName);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return attributeValue;
    }

    public static MBeanInfo getMBeanInfo(MBeanServer mBeanServer, ObjectName objectName) {
        MBeanInfo mBeanInfo = null;
        try {
            mBeanInfo = mBeanServer.getMBeanInfo(objectName);
        } catch (InstanceNotFoundException e) {
            handleInstanceNotFoundException(e, mBeanServer, objectName);
        } catch (IntrospectionException e) {
            handleIntrospectionException(e, mBeanServer, objectName);
        } catch (ReflectionException e) {
            handleReflectionException(e, mBeanServer, objectName);
        }
        return mBeanInfo;
    }

    private static void handleInstanceNotFoundException(InstanceNotFoundException e, MBeanServer mBeanServer, ObjectName objectName) {
        if (logger.isWarnEnabled()) {
            logger.warn("the MBean[name : '{}'] can't be found in the MBeanServer[default domain : '{}' , domains : {}]",
                    objectName.getCanonicalName(),
                    mBeanServer.getDefaultDomain(),
                    Arrays.toString(mBeanServer.getDomains()),
                    e
            );
        }
    }

    private static void handleIntrospectionException(IntrospectionException e, MBeanServer mBeanServer, ObjectName objectName) {
        if (logger.isWarnEnabled()) {
            logger.warn("the MBean[name : '{}'] can't be introspected in the MBeanServer[default domain : '{}' , domains : {}]",
                    objectName.getCanonicalName(),
                    mBeanServer.getDefaultDomain(),
                    Arrays.toString(mBeanServer.getDomains()),
                    e
            );
        }
    }

    private static void handleReflectionException(ReflectionException e, MBeanServer mBeanServer, ObjectName objectName) {
        if (logger.isWarnEnabled()) {
            logger.warn("the MBean[name : '{}'] can't be manipulated by the Reflection in the MBeanServer[default domain : '{}' , domains : {}]",
                    objectName.getCanonicalName(),
                    mBeanServer.getDefaultDomain(),
                    Arrays.toString(mBeanServer.getDomains()),
                    e
            );
        }
    }

    private static void handleAttributeNotFoundException(AttributeNotFoundException e, MBeanServer mBeanServer,
                                                         ObjectName objectName, MBeanInfo mBeanInfo, String attributeName) {

        if (logger.isWarnEnabled()) {
            logger.warn("The attribute[name : '{}' ] of MBean[name : '{}'] can't be found in the MBeanServer[default domain : '{}' , domains : {}]",
                    attributeName,
                    objectName.getCanonicalName(),
                    mBeanServer.getDefaultDomain(),
                    Arrays.toString(mBeanServer.getDomains()),
                    e
            );
        }
    }
}
