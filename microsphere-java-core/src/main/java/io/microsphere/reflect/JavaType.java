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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Objects;

import static io.microsphere.reflect.JavaType.Kind.CLASS;
import static io.microsphere.reflect.JavaType.Kind.GENERIC_ARRAY_TYPE;
import static io.microsphere.reflect.JavaType.Kind.PARAMETERIZED_TYPE;
import static io.microsphere.reflect.JavaType.Kind.TYPE_VARIABLE;
import static io.microsphere.reflect.JavaType.Kind.UNKNOWN;
import static io.microsphere.reflect.JavaType.Kind.WILDCARD_TYPE;
import static io.microsphere.reflect.JavaType.Kind.valueOf;
import static io.microsphere.reflect.TypeUtils.asGenericArrayType;
import static io.microsphere.reflect.TypeUtils.asParameterizedType;
import static io.microsphere.reflect.TypeUtils.asTypeVariable;
import static io.microsphere.reflect.TypeUtils.asWildcardType;
import static io.microsphere.reflect.TypeUtils.getRawClass;
import static io.microsphere.reflect.TypeUtils.isActualType;
import static io.microsphere.reflect.TypeUtils.isObjectClass;
import static io.microsphere.reflect.TypeUtils.isObjectType;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArguments;
import static io.microsphere.util.ArrayUtils.EMPTY_TYPE_ARRAY;
import static io.microsphere.util.ArrayUtils.asArray;
import static io.microsphere.util.ArrayUtils.length;
import static java.util.Objects.hash;

/**
 * Represents a Java type, encapsulating various operations and utilities for handling different kinds of types,
 * including classes, parameterized types, type variables, wildcard types, and generic array types.
 * <p>
 * This class provides the ability to:
 * <ul>
 *     <li>Retrieve the supertype of this type ({@link #getSuperType()})</li>
 *     <li>Access implemented interfaces ({@link #getInterfaces()})</li>
 *     <li>Obtain generic type arguments ({@link #getGenericTypes()})</li>
 *     <li>Get the raw class representation ({@link #getRawType()})</li>
 *     <li>Trace back to the original source type ({@link #getSource()})</li>
 *     <li>Determine if this type is an instance of another type ({@link #as(Class)})</li>
 * </ul>
 * <p>
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Create JavaType from a field
 * Field field = MyClass.class.getField("myField");
 * JavaType javaType = JavaType.from(field);
 *
 * // Get generic type information
 * JavaType[] genericTypes = javaType.getGenericTypes();
 *
 * // Cast JavaType to a specific class type
 * JavaType stringType = javaType.as(String.class);
 *
 * // Get the raw class
 * Class<?> rawClass = javaType.getRawType();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Type
 * @since 1.0.0
 */
public class JavaType implements Serializable {

    public static final JavaType[] EMPTY_JAVA_TYPE_ARRAY = new JavaType[0];

    public static final JavaType OBJECT_JAVA_TYPE = from(Object.class, CLASS);

    public static final JavaType NULL_JAVA_TYPE = from(null, UNKNOWN);

    private final Type type;

    private final transient Kind kind;

    private final JavaType source;

    // Local cache fields

    private volatile JavaType superType;

    private volatile JavaType[] interfaces;

    private volatile JavaType[] genericTypes;

    protected JavaType(Type type) {
        this(type, valueOf(type));
    }

    protected JavaType(Type type, Kind kind) {
        this(type, kind, null);
    }

    protected JavaType(Type type, JavaType source) {
        this(type, valueOf(type), source);
    }

    protected JavaType(Type type, Kind kind, JavaType source) {
        this.type = type;
        this.kind = kind;
        this.source = source;
    }

    @Nonnull
    public Type getType() {
        return type;
    }

    @Nonnull
    public Kind getKind() {
        return kind;
    }

    @Nullable
    public JavaType getSource() {
        return this.source;
    }

    @Nullable
    public JavaType getRootSource() {
        JavaType currentSource = this.source;
        if (currentSource == null) {
            return null;
        }
        JavaType rootSource;
        do {
            rootSource = currentSource.source;
            if (rootSource == null) {
                rootSource = currentSource;
                break;
            } else {
                currentSource = currentSource.source;
            }
        } while (true);

        return rootSource;
    }

    @Nullable
    public Type getRawType() {
        return this.kind.getRawType(this.type);
    }

    @Nullable
    public JavaType getSuperType() {
        JavaType superType = this.superType;
        if (superType == null) {
            superType = resolveSuperType();
            this.superType = superType;
        }
        return superType;
    }

    protected JavaType resolveSuperType() {
        Type superType = getSuperType(this.kind, this.type);
        return superType == null ? null : from(superType, this);
    }

    @Nonnull
    public JavaType[] getInterfaces() {
        JavaType[] interfaces = this.interfaces;
        if (interfaces == null) {
            interfaces = resolveInterfaces();
            this.interfaces = interfaces;
        }
        return interfaces;
    }

    protected JavaType[] resolveInterfaces() {
        Type[] interfaces = kind.getInterfaces(type);
        return from(interfaces, this);
    }

    @Nonnull
    public JavaType getInterface(int interfaceIndex) throws IndexOutOfBoundsException {
        return getInterfaces()[interfaceIndex];
    }

    @Nonnull
    public JavaType[] getGenericTypes() {
        JavaType[] genericTypes = this.genericTypes;
        if (genericTypes == null) {
            genericTypes = resolveGenericTypes();
            this.genericTypes = genericTypes;
        }
        return genericTypes;
    }

    @Nonnull
    protected JavaType[] resolveGenericTypes() {
        if (isObjectType(this.type)) {
            return EMPTY_JAVA_TYPE_ARRAY;
        }
        Kind kind = this.kind;
        Type[] genericTypes = kind.getGenericTypes(this);
        return from(genericTypes, this);
    }

    @Nonnull
    public JavaType getGenericType(int genericTypeIndex) throws IndexOutOfBoundsException {
        return getGenericTypes()[genericTypeIndex];
    }

    /**
     * Get the JavaType presenting the target class
     *
     * @param targetClass the target class
     * @return <code>null</code> if can't cast to the target class
     */
    @Nullable
    public JavaType as(Class<?> targetClass) {
        if (isObjectClass(targetClass)) {
            return from(Object.class, CLASS, this);
        }
        Type typeToMatch = this.type;
        Type targetType = searchSuperType(targetClass, typeToMatch);
        if (targetType == null) {
            targetType = searchInterfaceType(targetClass, typeToMatch);
        }
        return targetType == null ? null : from(targetType, this);
    }

    @Nullable
    public <T> Class<T> toClass() {
        Type rawType = getRawType();
        return rawType == null ? null : (Class<T>) rawType;
    }

    @Nullable
    public ParameterizedType toParameterizedType() {
        return asParameterizedType(this.type);
    }

    @Nullable
    public TypeVariable toTypeVariable() {
        return asTypeVariable(this.type);
    }

    @Nullable
    public WildcardType toWildcardType() {
        return asWildcardType(this.type);
    }

    @Nullable
    public GenericArrayType toGenericArrayType() {
        return asGenericArrayType(this.type);
    }

    public boolean isSource() {
        return this.source == null;
    }

    public boolean isRootSource() {
        return getRootSource() == null;
    }

    public boolean isClass() {
        return CLASS.equals(this.kind);
    }

    public boolean isParameterizedType() {
        return PARAMETERIZED_TYPE.equals(this.kind);
    }

    public boolean isTypeVariable() {
        return TYPE_VARIABLE.equals(this.kind);
    }

    public boolean isWildCardType() {
        return WILDCARD_TYPE.equals(this.kind);
    }

    public boolean isGenericArrayType() {
        return GENERIC_ARRAY_TYPE.equals(this.kind);
    }

    public boolean isUnknownType() {
        return UNKNOWN.equals(this.kind);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JavaType)) return false;
        JavaType javaType = (JavaType) o;
        return Objects.equals(type, javaType.type)
                && kind == javaType.kind
                && Objects.equals(source, javaType.source);
    }

    @Override
    public int hashCode() {
        return hash(type, kind, source);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JavaType{");
        sb.append("type=").append(type);
        sb.append(", kind=").append(kind);
        sb.append(", source=").append(source);
        sb.append('}');
        return sb.toString();
    }

    @Nonnull
    public static JavaType from(Field field) {
        return from(field.getGenericType());
    }

    @Nonnull
    public static JavaType fromMethodReturnType(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        return from(genericReturnType);
    }

    @Nonnull
    public static JavaType[] fromMethodParameters(Method method) {
        Type[] genericParameterType = method.getGenericParameterTypes();
        return from(genericParameterType);
    }

    @Nonnull
    public static JavaType fromMethodParameter(Method method, int parameterIndex) {
        Type genericParameterType = method.getGenericParameterTypes()[parameterIndex];
        return from(genericParameterType);
    }

    @Nonnull
    public static JavaType from(Class<?> targetClass) {
        return from(targetClass, CLASS);
    }

    @Nonnull
    public static JavaType from(Type type) {
        return new JavaType(type);
    }

    @Nonnull
    protected static JavaType from(Type type, Kind kind) {
        return new JavaType(type, kind);
    }

    @Nonnull
    protected static JavaType from(Type type, JavaType source) {
        return new JavaType(type, source);
    }

    @Nonnull
    protected static JavaType from(Type type, Kind kind, JavaType source) {
        return new JavaType(type, kind, source);
    }

    @Nonnull
    protected static JavaType[] from(Type[] types) {
        return from(types, null);
    }

    @Nonnull
    protected static JavaType[] from(Type[] types, JavaType source) {
        int length = length(types);
        if (length == 0) {
            return EMPTY_JAVA_TYPE_ARRAY;
        }
        JavaType[] javaTypes = new JavaType[length];
        for (int i = 0; i < length; i++) {
            javaTypes[i] = from(types[i], source);
        }
        return javaTypes;
    }

    static Type searchSuperType(Class<?> targetClass, Type typeToMatch) {
        Type superType = getSuperType(typeToMatch);
        if (superType == null) {
            return null;
        }
        Type targetType = null;
        while (!matches(Object.class, superType)) {
            if (matches(targetClass, superType)) {
                // super type matched
                targetType = superType;
                break;
            }

            // To search interface type from super type
            targetType = searchInterfaceType(targetClass, superType);

            if (targetType == null) {
                superType = getSuperType(superType);
            } else {
                break;
            }

        }

        return targetType;
    }

    static Type searchInterfaceType(Class<?> targetClass, Type typeToMatch) {
        Type targetType = null;
        Type[] interfaces = getInterfaces(typeToMatch);
        for (Type interfaceType : interfaces) {
            if (matches(targetClass, interfaceType)) {
                targetType = interfaceType;
                break;
            } else {
                targetType = searchInterfaceType(targetClass, interfaceType);
                if (targetType != null) {
                    break;
                }
            }
        }
        return targetType;
    }

    static Type getSuperType(Type type) {
        return getSuperType(valueOf(type), type);
    }

    static Type getSuperType(Kind kind, Type type) {
        return kind.getSuperType(type);
    }

    static Type[] getInterfaces(Type type) {
        Kind kind = valueOf(type);
        return kind.getInterfaces(type);
    }

    private static boolean matches(Class<?> targetClass, Type typeToMatch) {
        JavaType.Kind kind = valueOf(typeToMatch);
        Type rawType = kind.getRawType(typeToMatch);
        return Objects.equals(targetClass, rawType);
    }

    /**
     * The kind of Java type
     */
    public enum Kind {

        /**
         * The type kind presents Java {@link Class}
         */
        CLASS(Class.class) {
            @Override
            public Type getSuperType(Type type) {
                Class klass = as(type);
                return klass.getGenericSuperclass();
            }

            @Override
            public Type getRawType(Type type) {
                return type;
            }

            @Override
            public Type[] getInterfaces(Type type) {
                Class klass = as(type);
                return klass.getGenericInterfaces();
            }

            @Override
            public Type[] getGenericTypes(JavaType javaType) {
                Class klass = as(javaType.type);
                TypeVariable<Class>[] typeParameters = klass.getTypeParameters();
                int length = typeParameters.length;
                return length == 0 ? super.getGenericTypes(javaType) : new Type[length];
            }

            private Class<?> as(Type type) {
                return getRawClass(type);
            }
        },

        /**
         * The type kind presents Java {@link ParameterizedType}
         */
        PARAMETERIZED_TYPE(ParameterizedType.class) {
            @Override
            public Type getSuperType(Type type) {
                Type rawType = getRawType(type);
                Kind rawTypeKind = valueOf(rawType);
                return rawTypeKind.getSuperType(rawType);
            }

            @Override
            public Type getRawType(Type type) {
                ParameterizedType pType = as(type);
                Type rawType = pType.getRawType();
                return rawType;
            }

            @Override
            public Type[] getInterfaces(Type type) {
                Type rawType = getRawType(type);
                Kind rawTypeKind = valueOf(rawType);
                return rawTypeKind.getInterfaces(rawType);
            }

            @Override
            public Type[] getGenericTypes(JavaType javaType) {
                Type type = resolveType(javaType);
                Type baseType = javaType.type;
                final Type[] genericTypes;
                List<Type> genericTypesList = resolveActualTypeArguments(type, baseType);
                if (genericTypesList.isEmpty()) {
                    genericTypes = as(baseType).getActualTypeArguments();
                    toActualTypes(genericTypes);
                } else {
                    genericTypes = asArray(genericTypesList, Type.class);
                }
                return genericTypes;
            }

            private void toActualTypes(Type[] genericTypes) {
                int length = genericTypes.length;
                for (int i = 0; i < length; i++) {
                    Type genericType = genericTypes[i];
                    if (!isActualType(genericType)) {
                        // set null if type is not an actual type
                        genericTypes[i] = null;
                    }
                }
            }

            private Type resolveType(JavaType javaType) {
                JavaType source = javaType.getRootSource();
                if (source == null) {
                    source = javaType.getSource();
                }
                return source == null ? javaType.type : source.type;
            }

            private ParameterizedType as(Type type) {
                return (ParameterizedType) type;
            }
        },

        /**
         * The type kind presents Java {@link TypeVariable}
         */
        TYPE_VARIABLE(TypeVariable.class),

        /**
         * The type kind presents Java {@link WildcardType}
         */
        WILDCARD_TYPE(WildcardType.class),

        /**
         * The type kind presents Java {@link GenericArrayType}
         */
        GENERIC_ARRAY_TYPE(GenericArrayType.class),

        /**
         * The unknown type kind
         */
        UNKNOWN(Type.class);

        final Class<? extends Type> typeClass;

        Kind(Class<? extends Type> typeClass) {
            this.typeClass = typeClass;
        }

        /**
         * Get the raw type from the specified {@link Type type}
         *
         * @param type the specified {@link Type type}
         * @return <code>null</code> as default
         */
        public Type getRawType(Type type) {
            return null;
        }

        /**
         * Get the super type from the specified {@link Type type}
         *
         * @param type the specified {@link Type type}
         * @return <code>null</code> as default
         */
        public Type getSuperType(Type type) {
            return null;
        }

        public Type[] getInterfaces(Type type) {
            return EMPTY_TYPE_ARRAY;
        }

        public Type[] getGenericTypes(JavaType javaType) {
            return EMPTY_TYPE_ARRAY;
        }

        public static Kind valueOf(Type type) {
            Kind kind = UNKNOWN;
            for (Kind e : values()) {
                if (e.typeClass.isInstance(type)) {
                    kind = e;
                    break;
                }
            }
            return kind;
        }

    }

}
