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

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * The {@link URLConnection} factory for the sub protocol
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface SubProtocolURLConnectionFactory {

    /**
     * Supports the current factory to create the {@link URLConnection} or not
     * <p>
     * <p>
     * <p>
     *
     * @param url          the URL that this connects to
     * @param subProtocols the list of sub-protocols
     * @return <code>true</code> if supports,otherwise <code>false</code>
     */
    boolean supports(URL url, List<String> subProtocols);

    /**
     * Create the sub-protocols' {@link URLConnection}
     *
     * @param url          the URL that this connects to
     * @param subProtocols the list of sub-protocols
     * @param proxy        {@link Proxy} the proxy through which the connection will be made. If direct connection is desired, Proxy.NO_PROXY should be specified.
     * @return {@link URLConnection}
     * @throws IOException If the process is failed
     */
    URLConnection create(URL url, List<String> subProtocols, Proxy proxy) throws IOException;
}
