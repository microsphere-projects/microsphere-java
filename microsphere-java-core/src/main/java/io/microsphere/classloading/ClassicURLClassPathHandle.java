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
package io.microsphere.classloading;

/**
 * Classic {@link URLClassPathHandle} for {@link sun.misc.URLClassPath} since JDK 1 to 8
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see sun.misc.URLClassPath
 * @see ModernURLClassPathHandle
 * @see URLClassPathHandle
 * @since 1.0.0
 */
public class ClassicURLClassPathHandle extends AbstractURLClassPathHandle {

    public ClassicURLClassPathHandle() {
        super();
    }

    @Override
    protected String getURLClassPathClassName() {
        return "sun.misc.URLClassPath";
    }

    @Override
    protected String getUrlsFieldName() {
        return "urls";
    }
}
