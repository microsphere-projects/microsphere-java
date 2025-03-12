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
package io.microsphere.reflect;

/**
 * Reflection Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ReflectionTest {

    private final String privateField = "privateField";

    String packagePrivateField = "packagePrivateField";

    protected String protectedField = "protectedField";

    public String publicField = "publicField";

    static String staticField = "staticField";

    private String privateMethod() {
        return "test";
    }

    String packagePrivateMethod(String arg) {
        return arg;
    }

    protected Object[] protectedMethod(Object... args) {
        return args;
    }

    public String publicMethod(int value) {
        return String.valueOf(value);
    }

    public final void errorMethod() {
        throw new RuntimeException("For testing...");
    }

    public static String staticMethod() {
        return "staticMethod";
    }

    private int method(int value) {
        return value;
    }

    public String method(String message) {
        return message;
    }

}
