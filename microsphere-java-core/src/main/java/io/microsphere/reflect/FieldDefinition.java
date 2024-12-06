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

import io.microsphere.lang.Deprecation;
import io.microsphere.util.Version;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.reflect.FieldUtils.setFieldValue;

/**
 * The definition class of {@link Field}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Field
 * @since 1.0.0
 */
public class FieldDefinition extends MemberDefinition<Field> {

    /**
     * @param since             the 'since' version
     * @param declaredClassName the name of declared class
     * @param name              the member name
     */
    public FieldDefinition(@Nonnull String since, @Nonnull String declaredClassName, @Nonnull String name) {
        super(since, declaredClassName, name);
    }

    /**
     * @param since             the 'since' version
     * @param deprecation       the deprecation
     * @param declaredClassName the name of declared class
     * @param name              the member name
     */
    public FieldDefinition(@Nonnull String since, @Nullable Deprecation deprecation, @Nonnull String declaredClassName, @Nonnull String name) {
        super(since, deprecation, declaredClassName, name);
    }

    /**
     * @param since             the 'since' version
     * @param declaredClassName the name of declared class
     * @param name              the member name
     */
    public FieldDefinition(@Nonnull Version since, @Nonnull String declaredClassName, @Nonnull String name) {
        super(since, declaredClassName, name);
    }

    /**
     * @param since             the 'since' version
     * @param deprecation       the deprecation
     * @param declaredClassName the name of declared class
     * @param name              the member name
     */
    public FieldDefinition(@Nonnull Version since, @Nullable Deprecation deprecation, @Nonnull String declaredClassName, @Nonnull String name) {
        super(since, deprecation, declaredClassName, name);
    }

    @Override
    protected Field resolveMember() {
        return findField(getResolvedClass(), getFieldName());
    }

    @Nonnull
    public String getFieldName() {
        return super.getName();
    }

    @Nullable
    public Field getResolvedField() {
        return getMember();
    }

    /**
     * Get the field value
     *
     * @param instance the instance
     * @param <T>      the field type
     * @return <code>null</code>
     * @throws IllegalStateException    if this Field object is enforcing Java language access control and the underlying
     *                                  field is inaccessible.
     * @throws IllegalArgumentException if the specified object is not an instance of the class or interface declaring
     *                                  the underlying field (or a subclass or implementor thereof).
     */
    public <T> T get(Object instance) throws IllegalStateException, IllegalArgumentException {
        return getFieldValue(instance, getResolvedField());
    }

    /**
     * Set the field value
     *
     * @param instance   the instance
     * @param fieldValue the value of field to be set
     * @param <T>        the field type
     * @return the previous value of the specified {@link Field}
     * @throws IllegalStateException    if this Field object is enforcing Java language access control and the underlying
     *                                  field is inaccessible.
     * @throws IllegalArgumentException if the specified object is not an instance of the class or interface declaring
     *                                  the underlying field (or a subclass or implementor thereof).
     */
    public <T> T set(Object instance, T fieldValue) {
        return setFieldValue(instance, getResolvedField(), fieldValue);
    }
}
