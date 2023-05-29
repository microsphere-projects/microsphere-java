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

import io.microsphere.util.ClassLoaderUtils;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Standard {@link URLStreamHandlerFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class StandardURLStreamHandlerFactory implements URLStreamHandlerFactory {

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        String className = "sun.net.www.protocol." + protocol + ".Handler";
        Class<?> handlerClass = ClassLoaderUtils.resolveClass(className, ClassLoaderUtils.getDefaultClassLoader());
        try {
            return (URLStreamHandler) handlerClass.newInstance();
        } catch (Throwable e) {
        }
        return null;
    }
}
