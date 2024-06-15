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

import io.microsphere.invoke.MethodHandleUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import static io.microsphere.invoke.MethodHandleUtils.lookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * Standard {@link URLStreamHandlerFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see java.net.URL#getURLStreamHandler(String)
 * @since 1.0.0
 */
public class StandardURLStreamHandlerFactory implements URLStreamHandlerFactory {

    private static final MethodHandle methodHandle;

    static {
        // Lookup the method : java.net.URL#getURLStreamHandler(String)
        MethodHandles.Lookup lookup = lookup(URL.class, MethodHandleUtils.LookupMode.ALL);
        MethodType methodType = methodType(URLStreamHandler.class, String.class);
        try {
            methodHandle = lookup.findStatic(URL.class, "getURLStreamHandler", methodType);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        URLStreamHandler handler = null;
        try {
            handler = (URLStreamHandler) methodHandle.invokeExact(protocol);
        } catch (Throwable e) {
        }
        return handler;
    }
}
