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
package io.microsphere.net;

import io.microsphere.logging.Logger;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.net.URLUtils.DEFAULT_HANDLER_PACKAGE_PREFIX;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.reflect.ReflectionUtils.isInaccessibleObjectException;

/**
 * Standard {@link URLStreamHandlerFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see java.net.URL#getURLStreamHandler(String)
 * @since 1.0.0
 */
public class StandardURLStreamHandlerFactory implements URLStreamHandlerFactory {

    private static final Logger logger = getLogger(StandardURLStreamHandlerFactory.class);

    /**
     * The field name of {@link URL#defaultFactory}
     */
    private static final String defaultFactoryFieldName = "defaultFactory";

    /**
     * {@link URL#defaultFactory} static field since JDK 9+
     */
    private static final Field defaultFactoryField = findField(URL.class, defaultFactoryFieldName); // JDK 9+

    private final Map<String, URLStreamHandler> handlersCache = new HashMap<>();

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        return handlersCache.computeIfAbsent(protocol, this::doCreateURLStreamHandler);
    }

    URLStreamHandler doCreateURLStreamHandler(String protocol) {
        URLStreamHandler handler = createURLStreamHandlerFromDefaultFactory(protocol);
        if (handler == null) { // <= JDK 8 works
            String name = DEFAULT_HANDLER_PACKAGE_PREFIX + "." + protocol + ".Handler";
            try {
                Object o = Class.forName(name).newInstance();
                return (URLStreamHandler) o;
            } catch (Exception x) {
                // For compatibility, all Exceptions are ignored.
                // any number of exceptions can get thrown here
            }
        }
        return handler;
    }

    URLStreamHandler createURLStreamHandlerFromDefaultFactory(String protocol) {
        if (defaultFactoryField == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("The 'defaultFactory' field can't be found in the class URL.");
            }
            return null;
        }
        URLStreamHandler handler = null;
        try {
            trySetAccessible(defaultFactoryField);
            URLStreamHandlerFactory factory = (URLStreamHandlerFactory) defaultFactoryField.get(null);
            handler = factory.createURLStreamHandler(protocol);
        } catch (Exception e) {
            // ignore
        }
        return handler;
    }
}
