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
package io.github.microsphere.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Encapsulates a Java Type(Immutable), providing access to supertypes and generic parameters.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Type
 * @since 1.0.0
 */
public class JavaType {

    private final Type type;

    private final Kind kind;

    public JavaType(Type type) {
        this.type = type;
        this.kind = Kind.valueOf(type);
    }

    public Type getType() {
        return type;
    }

    public Type getRawType() {
        return this.kind.getRawType(this.type);
    }

    public Kind getKind() {
        return kind;
    }

    public JavaType getSuperType() {
        Type superType = kind.getSuperType(type);
        return from(superType);
    }

    public JavaType[] getGenericTypes() {
        if (Kind.PARAMETERIZED_TYPE.equals(this.kind)) {
            ParameterizedType pType = (ParameterizedType) this.type;
            Type[] typeArguments = pType.getActualTypeArguments();
            return from(typeArguments);
        } else {
            // Can't resolve the generic types
            return new JavaType[0];
        }
    }

    public JavaType getGenericType(int genericTypeIndex) {
        return getGenericTypes()[genericTypeIndex];
    }

    public JavaType as(Class<?> type) {
        // TODO
        return null;
    }

    public static JavaType from(Field field) {
        return from(field.getGenericType());
    }

    public static JavaType fromMethodReturnType(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        return from(genericReturnType);
    }

    public static JavaType[] fromMethodParameters(Method method) {
        Type[] genericParameterType = method.getGenericParameterTypes();
        return from(genericParameterType);
    }

    public static JavaType fromMethodParameter(Method method, int parameterIndex) {
        Type genericParameterType = method.getGenericParameterTypes()[parameterIndex];
        return from(genericParameterType);
    }

    public static JavaType from(Type type) {
        return new JavaType(type);
    }

    public static JavaType[] from(Type[] types) {
        int length = types.length;
        JavaType[] javaTypes = new JavaType[length];
        for (int i = 0; i < length; i++) {
            javaTypes[i] = from(types[i]);
        }
        return javaTypes;
    }


    public static enum Kind {

        /**
         * The type kind presents Java {@link Class}
         */
        CLASS(Class.class) {
            @Override
            public Type getSuperType(Type type) {
                Class klass = (Class) type;
                return klass.getGenericSuperclass();
            }

            @Override
            public Type getRawType(Type type) {
                return type;
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

            private ParameterizedType as(Type type) {
                return (ParameterizedType) type;
            }
        },

        /**
         * The type kind presents Java {@link GenericArrayType}
         */
        GENERIC_ARRAY_TYPE(GenericArrayType.class),

        /**
         * The type kind presents Java {@link WildcardType}
         */
        WILDCARD_TYPE(WildcardType.class),

        // TODO More Types

        UNKNOWN(Type.class);

        private final Class<? extends Type> typeClass;

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


        public static Kind valueOf(Type type) {
            Kind kind = Kind.UNKNOWN;
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
