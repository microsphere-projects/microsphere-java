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

import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.net.URLUtils.DEFAULT_HANDLER_PACKAGE_PREFIX;
import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.newInstance;

/**
 * Standard implementation of {@link URLStreamHandlerFactory} that creates a new instance of
 * URLStreamHandler for a given protocol.
 *
 * <p>This class uses reflection to access the internal JDK mechanism for retrieving the default
 * factory since JDK 9+, and falls back to loading the handler class via its fully qualified name
 * for JDK 8 and below.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * URLStreamHandlerFactory factory = new StandardURLStreamHandlerFactory();
 * URL.setURLStreamHandlerFactory(factory);
 * }</pre>
 *
 * <p>This will allow the application to handle custom protocols by locating the appropriate
 * URLStreamHandler implementation, typically in the package defined by
 * {@link io.microsphere.net.URLUtils#DEFAULT_HANDLER_PACKAGE_PREFIX}.</p>
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

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        return createURLStreamHandler(defaultFactoryField, protocol);
    }

    URLStreamHandler createURLStreamHandler(Field defaultFactoryField, String protocol) {
        URLStreamHandler handler = createURLStreamHandlerFromDefaultFactory(defaultFactoryField, protocol);
        if (handler == null) { // <= JDK 8 works
            String name = DEFAULT_HANDLER_PACKAGE_PREFIX + DOT + protocol + DOT + "Handler";
            Class<?> handlerClass = resolveClass(name);
            if (handlerClass != null) {
                handler = (URLStreamHandler) newInstance(handlerClass);
            }
        }
        return handler;
    }

    URLStreamHandler createURLStreamHandlerFromDefaultFactory(Field defaultFactoryField, String protocol) {
        if (defaultFactoryField == null) {
            logger.trace("The 'defaultFactory' field can't be found in the class URL.");
            return null;
        }
        URLStreamHandlerFactory factory = getStaticFieldValue(defaultFactoryField);
        URLStreamHandler handler = factory.createURLStreamHandler(protocol);
        return handler;
    }
}