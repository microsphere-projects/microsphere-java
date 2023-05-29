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
package io.microsphere.beans;

import java.beans.PropertyDescriptor;
import java.util.Objects;

/**
 * The class presenting the Property of Bean
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class BeanProperty {

    private String name;

    private Object value;

    private Class<?> declaringClass;

    private PropertyDescriptor descriptor;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(Class<?> declaringClass) {
        this.declaringClass = declaringClass;
    }

    public PropertyDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(PropertyDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BeanProperty)) return false;

        BeanProperty that = (BeanProperty) o;

        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(value, that.value)) return false;
        if (!Objects.equals(declaringClass, that.declaringClass))
            return false;
        return Objects.equals(descriptor, that.descriptor);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (declaringClass != null ? declaringClass.hashCode() : 0);
        result = 31 * result + (descriptor != null ? descriptor.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BeanProperty{");
        sb.append("name='").append(name).append('\'');
        sb.append(", value=").append(value);
        sb.append(", declaringClass=").append(declaringClass);
        sb.append(", descriptor=").append(descriptor);
        sb.append('}');
        return sb.toString();
    }
}
