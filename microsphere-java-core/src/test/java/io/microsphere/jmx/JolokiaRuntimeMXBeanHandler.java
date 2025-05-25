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

package io.microsphere.jmx;

import io.microsphere.io.IOUtils;

import java.io.InputStream;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;

import static java.lang.Thread.currentThread;
import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * {@link InvocationHandler} for {@link RuntimeMXBean}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see InvocationHandler
 * @since 1.0.0
 */
public class JolokiaRuntimeMXBeanHandler implements InvocationHandler {

    private static final String baseURI = "http://127.0.0.1:8080/jolokia/read/";

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String objectName = "java.lang:type=Runtime";

        String methodName = method.getName();

        String attributeName = methodName.substring(3);

        String jmxResourceURI = baseURI + objectName + "/" + attributeName;

        URL url = new URL(jmxResourceURI);
        try (InputStream inputStream = url.openStream()) {
            String content = IOUtils.copyToString(inputStream);
            return content;
        }
    }

    public static void main(String[] args) throws Throwable {
        RuntimeMXBean runtimeMXBean = (RuntimeMXBean) newProxyInstance(currentThread().getContextClassLoader(),
                new Class[]{RuntimeMXBean.class}, new JolokiaRuntimeMXBeanHandler());
        System.out.println(runtimeMXBean.getClassPath());
    }
}
