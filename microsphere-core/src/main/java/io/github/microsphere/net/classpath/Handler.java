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
package io.github.microsphere.net.classpath;

import io.github.microsphere.net.ExtendableProtocolURLStreamHandler;
import io.github.microsphere.util.ClassLoaderUtils;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import static io.github.microsphere.constants.PathConstants.SLASH_CHAR;
import static io.github.microsphere.util.ClassLoaderUtils.getClassLoader;

/**
 * The "classpath" protocol {@link URLStreamHandler} based on {@link ClassLoader}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ClassLoader#getResource(String)
 * @see ClassLoaderUtils#getClassLoader()
 * @since 1.0.0
 */
public class Handler extends ExtendableProtocolURLStreamHandler {

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        String authority = u.getAuthority();
        String path = u.getPath();

        String classPath = path;

        if (authority != null && !authority.isEmpty()) {
            classPath = authority + path;
        }

        while (classPath.indexOf(SLASH_CHAR) == 0) {
            classPath = classPath.substring(1);
        }

        ClassLoader classLoader = getClassLoader();
        URL url = classLoader.getResource(classPath);
        if (url == null) {
            throw new IOException("No Resource[classpath='" + classPath + "'] was not found!");
        }
        return url.openConnection();
    }

    @Override
    protected URLConnection openConnection(URL u, Proxy p) throws IOException {
        return openConnection(u);
    }
}
