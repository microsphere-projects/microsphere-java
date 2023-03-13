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
package io.github.microsphere.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.List;
import java.util.Map;

/**
 * Delegating {@link URLConnection}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class DelegatingURLConnection extends URLConnection {

    private final URLConnection delegate;

    public DelegatingURLConnection(URLConnection delegate) {
        super(delegate.getURL());
        this.delegate = delegate;
    }

    @Override
    public void connect() throws IOException {
        delegate.connect();
    }

    @Override
    public void setConnectTimeout(int timeout) {
        delegate.setConnectTimeout(timeout);
    }

    @Override
    public int getConnectTimeout() {
        return delegate.getConnectTimeout();
    }

    @Override
    public void setReadTimeout(int timeout) {
        delegate.setReadTimeout(timeout);
    }

    @Override
    public int getReadTimeout() {
        return delegate.getReadTimeout();
    }

    @Override
    public URL getURL() {
        return delegate.getURL();
    }

    @Override
    public int getContentLength() {
        return delegate.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        return delegate.getContentLengthLong();
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public String getContentEncoding() {
        return delegate.getContentEncoding();
    }

    @Override
    public long getExpiration() {
        return delegate.getExpiration();
    }

    @Override
    public long getDate() {
        return delegate.getDate();
    }

    @Override
    public long getLastModified() {
        return delegate.getLastModified();
    }

    @Override
    public String getHeaderField(String name) {
        return delegate.getHeaderField(name);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return delegate.getHeaderFields();
    }

    @Override
    public int getHeaderFieldInt(String name, int Default) {
        return delegate.getHeaderFieldInt(name, Default);
    }

    @Override
    public long getHeaderFieldLong(String name, long Default) {
        return delegate.getHeaderFieldLong(name, Default);
    }

    @Override
    public long getHeaderFieldDate(String name, long Default) {
        return delegate.getHeaderFieldDate(name, Default);
    }

    @Override
    public String getHeaderFieldKey(int n) {
        return delegate.getHeaderFieldKey(n);
    }

    @Override
    public String getHeaderField(int n) {
        return delegate.getHeaderField(n);
    }

    @Override
    public Object getContent() throws IOException {
        return delegate.getContent();
    }

    @Override
    public Object getContent(Class[] classes) throws IOException {
        return delegate.getContent(classes);
    }

    @Override
    public Permission getPermission() throws IOException {
        return delegate.getPermission();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return delegate.getOutputStream();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public void setDoInput(boolean doinput) {
        delegate.setDoInput(doinput);
    }

    @Override
    public boolean getDoInput() {
        return delegate.getDoInput();
    }

    @Override
    public void setDoOutput(boolean dooutput) {
        delegate.setDoOutput(dooutput);
    }

    @Override
    public boolean getDoOutput() {
        return delegate.getDoOutput();
    }

    @Override
    public void setAllowUserInteraction(boolean allowuserinteraction) {
        delegate.setAllowUserInteraction(allowuserinteraction);
    }

    @Override
    public boolean getAllowUserInteraction() {
        return delegate.getAllowUserInteraction();
    }

    @Override
    public void setUseCaches(boolean usecaches) {
        delegate.setUseCaches(usecaches);
    }

    @Override
    public boolean getUseCaches() {
        return delegate.getUseCaches();
    }

    @Override
    public void setIfModifiedSince(long ifmodifiedsince) {
        delegate.setIfModifiedSince(ifmodifiedsince);
    }

    @Override
    public long getIfModifiedSince() {
        return delegate.getIfModifiedSince();
    }

    @Override
    public boolean getDefaultUseCaches() {
        return delegate.getDefaultUseCaches();
    }

    @Override
    public void setDefaultUseCaches(boolean defaultusecaches) {
        delegate.setDefaultUseCaches(defaultusecaches);
    }

    @Override
    public void setRequestProperty(String key, String value) {
        delegate.setRequestProperty(key, value);
    }

    @Override
    public void addRequestProperty(String key, String value) {
        delegate.addRequestProperty(key, value);
    }

    @Override
    public String getRequestProperty(String key) {
        return delegate.getRequestProperty(key);
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        return delegate.getRequestProperties();
    }
}
