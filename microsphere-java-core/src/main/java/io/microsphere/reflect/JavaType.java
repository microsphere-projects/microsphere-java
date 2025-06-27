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

import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.reflect.JavaType.Kind.CLASS;
import static io.microsphere.reflect.JavaType.Kind.GENERIC_ARRAY_TYPE;
import static io.microsphere.reflect.JavaType.Kind.PARAMETERIZED_TYPE;
import static io.microsphere.reflect.JavaType.Kind.TYPE_VARIABLE;
import static io.microsphere.reflect.JavaType.Kind.UNKNOWN;
import static io.microsphere.reflect.JavaType.Kind.WILDCARD_TYPE;
import static io.microsphere.reflect.JavaType.Kind.valueOf;
import static io.microsphere.reflect.MethodUtils.findMethod;
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
 *  private static HashMap<Integer, List<String>> mapField;
 *
 *  public void example() throws Throwable {
 *     JavaType javaType = fromField(getClass(), "mapField");
 *     javaType.getSuperType();          // AbstractMap<Integer, List<String>>
 *     javaType.asMap();                 // Map<Integer, List<String>>
 *     javaType.getGeneric(0).resolve(); // Integer
 *     javaType.getGeneric(1).resolve(); // List
 *     javaType.getGeneric(1);           // List<String>
 *     javaType.resolveGeneric(1, 0);    // String
 *  }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Type
 * @see Class
 * @see Field
 * @see Method
 * @since 1.0.0
 */
public class JavaType implements Serializable {

    /**
     * A shared empty array of JavaType, used to avoid unnecessary object creation for methods
     * that return arrays of JavaType instances.
     */
    public static final JavaType[] EMPTY_JAVA_TYPE_ARRAY = new JavaType[0];

    /**
     * A JavaType instance representing the {@link Object} class, which serves as the root class for all Java types.
     * This constant provides a convenient way to reference the most general type in the Java type system.
     */
    public static final JavaType OBJECT_JAVA_TYPE = from(Object.class, CLASS);

    /**
     * A JavaType instance representing a null value, which is used when a JavaType cannot be determined.
     * This constant provides a convenient way to handle cases where a JavaType is not available.
     */
    public static final JavaType NULL_JAVA_TYPE = from(null, UNKNOWN);

    private final Type type;

    private final transient Kind kind;

    private final JavaType source;

    // Local cache fields

    private volatile JavaType superType;

    private volatile JavaType[] interfaces;

    private volatile JavaType[] genericTypes;

    /**
     * Constructs a new JavaType instance with the specified type.
     * <p>
     * This constructor initializes the JavaType by determining its kind based on the provided type.
     * </p>
     *
     * @param type the Type object to be encapsulated by this JavaType instance
     */
    protected JavaType(Type type) {
        this(type, valueOf(type));
    }

    /**
     * Constructs a new JavaType instance with the specified type and kind.
     * <p>
     * This constructor initializes the JavaType with a known type kind, allowing for more precise handling of the type.
     * </p>
     *
     * @param type The Type object to be encapsulated by this JavaType instance
     * @param kind The Kind of the type, indicating what sort of Java type this represents (e.g., CLASS, PARAMETERIZED_TYPE)
     */
    protected JavaType(Type type, Kind kind) {
        this(type, kind, null);
    }

    /**
     * Constructs a new {@link JavaType} instance with the specified type and source.
     * <p>
     * This constructor is used when creating a JavaType that is derived from another JavaType,
     * such as when resolving nested or parameterized types. The source provides context for
     * resolving the type information more accurately.
     * </p>
     *
     * @param type   the Type object to be encapsulated by this JavaType instance
     * @param source the source JavaType from which this type was derived, may be null
     */
    protected JavaType(Type type, JavaType source) {
        this(type, valueOf(type), source);
    }

    /**
     * Constructs a new {@link JavaType} instance with the specified type, kind, and source.
     * <p>
     * This constructor is used when creating a JavaType with a known type kind and an associated source,
     * which provides context for resolving the type information more accurately.
     * </p>
     *
     * @param type   the {@link Type} object to be encapsulated by this JavaType instance
     * @param kind   the {@link Kind} of the type, indicating what sort of Java type this represents
     *               (e.g., CLASS, PARAMETERIZED_TYPE)
     * @param source the source JavaType from which this type was derived, may be null
     */
    protected JavaType(Type type, Kind kind, JavaType source) {
        this.type = type;
        this.kind = kind;
        this.source = source;
    }

    /**
     * Retrieves the underlying {@link Type} object that this JavaType represents.
     *
     * <p>This method provides access to the raw type information encapsulated by this JavaType instance,
     * which can be useful when further low-level type operations are required.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *  private static HashMap<Integer, List<String>> mapField;
     *
     *  public void example() throws Throwable {
     *     JavaType javaType = fromField(getClass(), "mapField");
     *     Type type = javaType.getType(); // Returns the actual Type object, e.g., ParameterizedType
     *  }
     * }</pre>
     *
     * @return the underlying Type object; never null
     */
    @Nonnull
    public Type getType() {
        return type;
    }

    /**
     * Retrieves the {@link Kind} of this JavaType, indicating what sort of type it represents.
     *
     * <p>The possible kinds include:
     * <ul>
     *     <li>{@link Kind#CLASS} for regular classes and interfaces</li>
     *     <li>{@link Kind#PARAMETERIZED_TYPE} for parameterized types (e.g., {@code List<String>})</li>
     *     <li>{@link Kind#TYPE_VARIABLE} for type variables (e.g., {@code T} in a generic class)</li>
     *     <li>{@link Kind#WILDCARD_TYPE} for wildcard types (e.g., {@code ? extends Number})</li>
     *     <li>{@link Kind#GENERIC_ARRAY_TYPE} for generic array types (e.g., {@code T[]})</li>
     *     <li>{@link Kind#UNKNOWN} if the type cannot be classified</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *  private static HashMap<Integer, List<String>> mapField;
     *
     *  public void example() throws Throwable {
     *     JavaType javaType = fromField(getClass(), "mapField");
     *     javaType.asMap().getKind();       // returns PARAMETERIZED_TYPE -> Map<Integer, List<String>>
     *     javaType.getGeneric(0).getKind(); // returns CLASS -> Integer
     *  }
     * }</pre>
     *
     * @return the kind of this JavaType; never null
     */
    @Nonnull
    public Kind getKind() {
        return kind;
    }

    /**
     * Retrieves the source JavaType from which this type was derived, if available.
     *
     * <p>This method returns the JavaType that served as the origin for this type,
     * typically used when resolving nested or parameterized types to provide context.</p>
     *
     * @return the source JavaType, or null if no source is available
     */
    @Nullable
    public JavaType getSource() {
        return this.source;
    }

    /**
     * Retrieves the root source JavaType from which this type was ultimately derived, if available.
     *
     * <p>The "root source" refers to the original JavaType at the beginning of a chain of derived types.
     * This method traverses through the source hierarchy until it reaches the topmost non-null source.</p>
     *
     * @return the root source JavaType, or null if no source is available at any level in the chain
     */
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

    /**
     * Retrieves the raw type represented by this JavaType.
     *
     * <p>The "raw type" refers to the underlying class or interface type before any generic type parameters are applied.
     * For example, for a parameterized type like {@code List<String>}, the raw type would be {@code List}.</p>
     *
     * @return the raw type as a Class object; may be null if the type cannot be resolved to a class
     */
    @Nullable
    public Type getRawType() {
        return this.kind.getRawType(this.type);
    }

    /**
     * Retrieves the supertype of this JavaType.
     *
     * <p>This method returns the direct superclass of the type represented by this JavaType instance.
     * The returned JavaType may be {@code null} if the type does not have a superclass (e.g., for the {@link Object} class).</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *  private static HashMap<Integer, List<String>> mapField;
     *
     *  public void example() throws Throwable {
     *     JavaType javaType = fromField(getClass(), "mapField");
     *     javaType.getSuperType();          // AbstractMap<Integer, List<String>>
     *  }
     * }</pre>
     *
     * @return the supertype as a JavaType, or {@code null} if no superclass exists
     */
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

    /**
     * Retrieves the interfaces directly implemented by the class or type represented by this JavaType.
     *
     * <p>This method returns an array of JavaType objects representing the interfaces
     * that the underlying type directly implements. If the type does not implement any interfaces,
     * or if it represents a primitive or void type, then an empty array is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JavaType javaType = JavaType.from(ArrayList.class); // ArrayList<E>
     * JavaType[] interfaces = javaType.getInterfaces();   // e.g., List<E>, RandomAccess, etc.
     * }</pre>
     *
     * @return an array of JavaType representing the interfaces directly implemented by this type;
     * never null but may be empty
     */
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

    /**
     * Retrieves the interface at the specified index from the interfaces directly implemented by this type.
     *
     * <p>This method provides access to individual interfaces that the underlying type directly implements.
     * The returned JavaType represents the specific interface at the given index in the array of interfaces.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JavaType javaType = JavaType.from(ArrayList.class); // ArrayList<E>
     * JavaType listInterface = javaType.getInterface(0);  // List<E>
     * }</pre>
     *
     * @param interfaceIndex the index of the interface to retrieve
     * @return the JavaType representing the interface at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= number of interfaces)
     */
    @Nonnull
    public JavaType getInterface(int interfaceIndex) throws IndexOutOfBoundsException {
        return getInterfaces()[interfaceIndex];
    }

    /**
     * Retrieves the generic type arguments associated with this JavaType.
     *
     * <p>This method provides access to the generic type parameters that define this type,
     * such as the key and value types in a parameterized type like {@code Map<K, V>}.
     * The returned array contains one element for each generic type argument.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *  private static HashMap<Integer, List<String>> mapField;
     *
     *  public void example() throws Throwable {
     *     JavaType javaType = fromField(getClass(), "mapField");
     *     javaType.getGenericTypes();       // [Integer, List<String>]
     *  }
     * }</pre>
     *
     * @return an array of JavaType objects representing the generic type arguments;
     * never null but may be empty if no generic type arguments are present
     */
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

    /**
     * Retrieves the generic type argument at the specified index from this JavaType.
     *
     * <p>This method provides access to individual generic type parameters that define this type.
     * For example, in a parameterized type like {@code Map<K, V>}, this method can be used to retrieve
     * either the key type ({@code K}) or the value type ({@code V}) by index.</p>
     *
     * @param genericTypeIndex the index of the generic type argument to retrieve
     * @return the JavaType representing the generic type argument at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= number of generic type arguments)
     */
    @Nonnull
    public JavaType getGenericType(int genericTypeIndex) throws IndexOutOfBoundsException {
        return getGenericTypes()[genericTypeIndex];
    }

    /**
     * Retrieves a JavaType instance representing the specified target class if it is assignable from this type.
     *
     * <p>This method checks whether the provided target class can be assigned from the current type,
     * and returns a corresponding JavaType instance if the assignment is valid. If the target class
     * cannot be assigned, then {@code null} is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *  private static HashMap<Integer, List<String>> mapField;
     *
     *  public void example() throws Throwable {
     *     JavaType javaType = fromField(getClass(), "mapField");
     *     javaType.as(Map.class);                 // Map<Integer, List<String>
     *  }
     * }</pre>
     *
     * @param targetClass the target class to check assignability against this type
     * @return a JavaType instance representing the target class if it is assignable from this type;
     * otherwise, {@code null}
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

    /**
     * Converts this JavaType to a {@link Class} if possible.
     *
     * <p>This method attempts to represent this JavaType as a concrete class.
     * It will return null if the type cannot be resolved to a class, such as when it represents
     * a parameterized type, wildcard type, or generic array type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *  private static HashMap<Integer, List<String>> mapField;
     *
     *  public void example() throws Throwable {
     *     JavaType javaType = fromField(getClass(), "mapField");
     *     javaType.toClass();          // HashMap
     *  }
     * }</pre>
     *
     * @param <T> the type of the class returned
     * @return the Class object representing this type, or null if it cannot be resolved to a class
     */
    @Nullable
    public <T> Class<T> toClass() {
        Type rawType = getRawType();
        return rawType == null ? null : (Class<T>) rawType;
    }

    /**
     * Converts this JavaType to a {@link ParameterizedType} if possible.
     *
     * <p>This method attempts to represent this JavaType as a parameterized type,
     * such as {@code List<String>}. If the underlying type is not a parameterized type,
     * this method will return null.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *  private static HashMap<Integer, List<String>> mapField;
     *
     *  public void example() throws Throwable {
     *     JavaType javaType = fromField(getClass(), "mapField");
     *     javaType.toParameterizedType();          // ParameterizedType -> HashMap<Integer, List<String>>
     *  }
     * }</pre>
     *
     * @return the ParameterizedType representation of this JavaType, or null if it is not a parameterized type
     */
    @Nullable
    public ParameterizedType toParameterizedType() {
        return asParameterizedType(this.type);
    }

    /**
     * Converts this JavaType to a {@link TypeVariable} if possible.
     *
     * <p>This method attempts to represent this JavaType as a type variable,
     * such as {@code T} in a generic class definition. If the underlying type is not
     * a type variable, this method will return null.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example<T> {
     *     private T value;
     *
     *     public void exampleMethod() throws Throwable {
     *         JavaType javaType = JavaType.fromField(Example.class, "value");
     *         TypeVariable typeVariable = javaType.toTypeVariable(); // Returns TypeVariable for T
     *     }
     * }
     * }</pre>
     *
     * @return the TypeVariable representation of this JavaType, or null if it is not a type variable
     */
    @Nullable
    public TypeVariable toTypeVariable() {
        return asTypeVariable(this.type);
    }

    /**
     * Converts this JavaType to a {@link WildcardType} if possible.
     *
     * <p>This method attempts to represent this JavaType as a wildcard type,
     * such as {@code ? extends Number} or {@code ? super String}. If the underlying type is not
     * a wildcard type, this method will return null.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     private List<? extends Number> list;
     *
     *     public void exampleMethod() throws Throwable {
     *         JavaType javaType = JavaType.fromField(Example.class, "list");
     *         WildcardType wildcardType = javaType.toWildcardType(); // Returns WildcardType for "? extends Number"
     *     }
     * }
     * }</pre>
     *
     * @return the WildcardType representation of this JavaType, or null if it is not a wildcard type
     */
    @Nullable
    public WildcardType toWildcardType() {
        return asWildcardType(this.type);
    }

    /**
     * Converts this JavaType to a {@link GenericArrayType} if possible.
     *
     * <p>This method attempts to represent this JavaType as a generic array type,
     * such as {@code T[]}. If the underlying type is not a generic array type,
     * this method will return null.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     private Integer[] numbers;
     *
     *     public void exampleMethod() throws Throwable {
     *         JavaType javaType = JavaType.fromField(Example.class, "numbers");
     *         GenericArrayType arrayType = javaType.toGenericArrayType(); // Returns GenericArrayType for "Integer[]"
     *     }
     * }
     * }</pre>
     *
     * @return the GenericArrayType representation of this JavaType, or null if it is not a generic array type
     */
    @Nullable
    public GenericArrayType toGenericArrayType() {
        return asGenericArrayType(this.type);
    }

    /**
     * Checks whether this JavaType is the source type.
     *
     * <p>A source JavaType is the origin of a chain of derived types. This method returns true if this JavaType
     * has no associated source, meaning it's at the top of the derivation hierarchy.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JavaType javaType = JavaType.from(ArrayList.class); // ArrayList<E>
     * boolean isSourceType = javaType.isSource();         // true (no source)
     * }</pre>
     *
     * @return true if this JavaType has no source and is therefore considered the source itself; false otherwise
     */
    public boolean isSource() {
        return this.source == null;
    }

    /**
     * Checks whether this JavaType is the root source type.
     *
     * <p>A root source JavaType is at the very top of the derivation chain, meaning it has no parent sources.
     * This method returns true if calling {@link #getRootSource()} returns null, indicating that this type
     * is not derived from any other type in the hierarchy.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JavaType javaType = JavaType.from(ArrayList.class); // ArrayList<E>
     * boolean isRootSource = javaType.isRootSource();     // true (no source)
     * }</pre>
     *
     * @return true if this JavaType is the root source; false otherwise
     */
    public boolean isRootSource() {
        return getRootSource() == null;
    }

    /**
     * Checks if this JavaType represents a regular class or interface.
     *
     * <p>This method returns true if the underlying type is a standard class or interface,
     * as opposed to a parameterized type, type variable, wildcard type, or generic array type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JavaType javaType = JavaType.from(String.class);
     * boolean isClass = javaType.isClass(); // true, because String is a regular class
     *
     * JavaType mapType = JavaType.from(Map.class);
     * boolean isMapClass = mapType.isClass(); // true, because Map is a regular class
     *
     * }</pre>
     *
     * @return true if this JavaType represents a regular class or interface; false otherwise
     */
    public boolean isClass() {
        return CLASS.equals(this.kind);
    }

    /**
     * Checks if this JavaType represents a parameterized type.
     *
     * <p>A parameterized type is a type that has generic type arguments, such as {@code List<String>}
     * or {@code Map<Integer, List<String>>}. This method returns true if the underlying type is
     * a parameterized type.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * private static HashMap<Integer, List<String>> mapField;
     *
     * public void example() throws Throwable {
     *     JavaType javaType = JavaType.fromField(getClass(), "mapField");
     *     boolean isParameterized = javaType.isParameterizedType(); // true -> HashMap<Integer, List<String>>
     * }
     * }</pre>
     *
     * @return true if this JavaType represents a parameterized type; false otherwise
     */
    public boolean isParameterizedType() {
        return PARAMETERIZED_TYPE.equals(this.kind);
    }

    /**
     * Checks if this JavaType represents a type variable.
     *
     * <p>A type variable is a placeholder for a concrete type that will be specified later,
     * typically used in generic class or method declarations. For example, in a generic class
     * like {@code public class Box<T>}, {@code T} is a type variable.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example<T> {
     *     private T value;
     *
     *     public void exampleMethod() throws Throwable {
     *         JavaType javaType = JavaType.fromField(Example.class, "value");
     *         boolean isTypeVariable = javaType.isTypeVariable(); // true -> T
     *     }
     * }
     * }</pre>
     *
     * @return true if this JavaType represents a type variable; false otherwise
     */
    public boolean isTypeVariable() {
        return TYPE_VARIABLE.equals(this.kind);
    }

    /**
     * Checks if this JavaType represents a wildcard type.
     *
     * <p>A wildcard type is an unknown type represented by a question mark (?),
     * often used in generic programming to denote an unspecified type. Examples include
     * {@code ? extends Number} or {@code ? super String}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     private List<?> list;
     *
     *     public void exampleMethod() throws Throwable {
     *         JavaType javaType = JavaType.fromField(Example.class, "list");
     *         boolean isWildcard = javaType.isWildCardType(); // true -> ? wildcard type
     *     }
     * }
     * }</pre>
     *
     * @return true if this JavaType represents a wildcard type; false otherwise
     */
    public boolean isWildCardType() {
        return WILDCARD_TYPE.equals(this.kind);
    }

    /**
     * Checks if this JavaType represents a generic array type.
     *
     * <p>A generic array type is an array whose component type is itself a parameterized type,
     * type variable, wildcard type, or another generic array type. For example, {@code List<String>[]}
     * or {@code T[][]} where {@code T} is a type variable.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     private List<String>[] listArray;
     *
     *     public void exampleMethod() throws Throwable {
     *         JavaType javaType = JavaType.fromField(Example.class, "listArray");
     *         boolean isGenericArray = javaType.isGenericArrayType(); // true -> List<String>[]
     *     }
     * }
     * }</pre>
     *
     * @return true if this JavaType represents a generic array type; false otherwise
     */
    public boolean isGenericArrayType() {
        return GENERIC_ARRAY_TYPE.equals(this.kind);
    }

    /**
     * Checks if this JavaType represents an unknown type.
     *
     * <p>This method returns true if the underlying type could not be classified into any known
     * category such as a class, parameterized type, type variable, wildcard type, or generic array type.
     * An unknown type typically indicates that the type information is unavailable or could not be resolved.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JavaType javaType = JavaType.from((Type) null);
     * boolean isUnknown = javaType.isUnknownType(); // true -> UNKNOWN type
     * }</pre>
     *
     * @return true if this JavaType represents an unknown type; false otherwise
     */
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

    /**
     * Creates a JavaType instance from the field with the specified name in the given class.
     *
     * <p>This method looks up the field using {@link FieldUtils#findField(Class, String)} and creates
     * a JavaType instance based on the field's generic type. If the field is not found, it returns
     * a special "unknown" JavaType.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     private Map<String, Integer> exampleField;
     *
     *     public static void main(String[] args) {
     *         JavaType javaType = JavaType.fromField(Example.class, "exampleField");
     *         // javaType now represents Map<String, Integer>
     *     }
     * }
     * }</pre>
     *
     * @param declaredClass The class that declares the field
     * @param fieldName     The name of the field to retrieve the type from
     * @return A JavaType instance representing the field's type, or NULL_JAVA_TYPE if the field is not found
     */
    @Nullable
    public static JavaType fromField(Class<?> declaredClass, String fieldName) {
        Field field = findField(declaredClass, fieldName);
        return field == null ? NULL_JAVA_TYPE : from(field);
    }

    /**
     * Creates a JavaType instance from the given {@link Field}.
     *
     * <p>This method retrieves the generic type of the field and constructs a JavaType
     * to represent it. The resulting JavaType can be used for further type inspection
     * and manipulation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     private Map<String, Integer> exampleField;
     *
     *     public static void main(String[] args) throws Throwable {
     *         Field field = Example.class.getDeclaredField("exampleField");
     *         JavaType javaType = JavaType.from(field);
     *         // javaType now represents Map<String, Integer>
     *     }
     * }
     * }</pre>
     *
     * @param field the field to derive the JavaType from
     * @return a JavaType representing the field's type; never null
     */
    @Nonnull
    public static JavaType from(Field field) {
        return from(field.getGenericType());
    }

    /**
     * Creates a JavaType instance representing the return type of a method with the specified name and parameter types.
     *
     * <p>This method looks up the method using {@link MethodUtils#findMethod(Class, String, Class...)} and creates
     * a JavaType instance based on the method's generic return type. If the method is not found, it returns
     * a special "unknown" JavaType.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     public Map<String, Integer> getExampleMap() {
     *         return new HashMap<>();
     *     }
     *
     *     public static void main(String[] args) {
     *         JavaType returnType = JavaType.fromMethodReturnType(Example.class, "getExampleMap");
     *         // returnType now represents Map<String, Integer>
     *     }
     * }
     * }</pre>
     *
     * @param declaredClass  The class that declares the method
     * @param methodName     The name of the method to retrieve the return type from
     * @param parameterTypes The parameter types of the method (used to uniquely identify the method)
     * @return A JavaType instance representing the method's return type, or NULL_JAVA_TYPE if the method is not found
     */
    @Nonnull
    public static JavaType fromMethodReturnType(Class<?> declaredClass, String methodName, Class<?>... parameterTypes) {
        Method method = findMethod(declaredClass, methodName, parameterTypes);
        return method == null ? NULL_JAVA_TYPE : fromMethodReturnType(method);
    }

    /**
     * Creates a JavaType instance representing the return type of the specified method.
     *
     * <p>This method retrieves the generic return type of the given method and constructs
     * a JavaType to represent it. The resulting JavaType can be used for further type inspection,
     * such as retrieving generic type arguments or resolving parameterized types.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     public Map<String, Integer> getExampleMap() {
     *         return new HashMap<>();
     *     }
     *
     *     public static void main(String[] args) throws Throwable {
     *         Method method = Example.class.getDeclaredMethod("getExampleMap");
     *         JavaType returnType = JavaType.fromMethodReturnType(method);
     *         // returnType now represents Map<String, Integer>
     *     }
     * }
     * }</pre>
     *
     * @param method the method to derive the JavaType from
     * @return a JavaType representing the method's return type; never null
     */
    @Nonnull
    public static JavaType fromMethodReturnType(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        return from(genericReturnType);
    }

    /**
     * Creates an array of JavaType instances representing the parameter types of a method with the specified name and parameters.
     *
     * <p>This method looks up the method using {@link MethodUtils#findMethod(Class, String, Class...)} and creates
     * JavaType instances based on the method's generic parameter types. If the method is not found, it returns
     * an empty array of JavaType.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     public void exampleMethod(String param1, Integer param2) {
     *         // Method body
     *     }
     *
     *     public static void main(String[] args) {
     *         JavaType[] parameterTypes = JavaType.fromMethodParameters(Example.class, "exampleMethod", String.class, Integer.class);
     *         // parameterTypes now contains two JavaType objects: String and Integer
     *     }
     * }
     * }</pre>
     *
     * @param declaredClass  The class that declares the method
     * @param methodName     The name of the method to retrieve the parameter types from
     * @param parameterTypes The parameter types of the method (used to uniquely identify the method)
     * @return An array of JavaType representing the method's parameter types, or an empty array if the method is not found
     */
    @Nonnull
    public static JavaType[] fromMethodParameters(Class<?> declaredClass, String methodName, Class<?>... parameterTypes) {
        Method method = findMethod(declaredClass, methodName, parameterTypes);
        return method == null ? EMPTY_JAVA_TYPE_ARRAY : fromMethodParameters(method);
    }

    /**
     * Creates an array of JavaType instances representing the parameter types of the specified method.
     *
     * <p>This method retrieves the generic parameter types of the given method and constructs
     * corresponding JavaType objects to represent them. Each JavaType can be used for further type inspection,
     * such as retrieving generic type arguments or resolving parameterized types.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     public void exampleMethod(String param1, Map<String, Integer> param2) {
     *         // Method body
     *     }
     *
     *     public static void main(String[] args) throws Throwable {
     *         Method method = Example.class.getDeclaredMethod("exampleMethod", String.class, Map.class);
     *         JavaType[] parameterTypes = JavaType.fromMethodParameters(method);
     *         // parameterTypes now contains two JavaType objects:
     *         // - JavaType representing String
     *         // - JavaType representing Map<String, Integer>
     *     }
     * }
     * }</pre>
     *
     * @param method the method to derive JavaType instances from
     * @return an array of JavaType objects representing the method's parameter types; never null
     */
    @Nonnull
    public static JavaType[] fromMethodParameters(Method method) {
        Type[] genericParameterType = method.getGenericParameterTypes();
        return from(genericParameterType);
    }

    /**
     * Creates a JavaType instance representing the parameter type of a method at the specified index.
     *
     * <p>This method retrieves the generic parameter type at the given index from the method's array
     * of generic parameter types and constructs a corresponding JavaType instance to represent it.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     public void exampleMethod(String param1, Map<String, Integer> param2) {
     *         // Method body
     *     }
     *
     *     public static void main(String[] args) throws Throwable {
     *         Method method = Example.class.getDeclaredMethod("exampleMethod", String.class, Map.class);
     *         JavaType firstParamType = JavaType.fromMethodParameter(method, 0); // Represents String
     *         JavaType secondParamType = JavaType.fromMethodParameter(method, 1); // Represents Map<String, Integer>
     *     }
     * }
     * }</pre>
     *
     * @param method         The method whose parameter type is to be retrieved
     * @param parameterIndex The index of the parameter in the method's parameter list
     * @return a JavaType representing the method's parameter type at the specified index; never null
     */
    @Nonnull
    public static JavaType fromMethodParameter(Method method, int parameterIndex) {
        Type genericParameterType = method.getGenericParameterTypes()[parameterIndex];
        return from(genericParameterType);
    }

    /**
     * Creates a JavaType instance representing the specified class.
     *
     * <p>This method encapsulates the given class into a JavaType object, which allows for more advanced type operations,
     * such as retrieving generic type information, interfaces, and supertypes. This is particularly useful when working
     * with generic classes or parameterized types.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * JavaType javaType = JavaType.from(String.class);
     * // javaType now represents String.class with its associated type information
     *
     * JavaType mapJavaType = JavaType.from(Map.class);
     * // mapJavaType now represents Map.class, allowing access to its generic type parameters
     * }</pre>
     *
     * @param targetClass the class to be encapsulated in a JavaType instance
     * @return a JavaType representing the given class; never null
     */
    @Nonnull
    public static JavaType from(Class<?> targetClass) {
        return from(targetClass, CLASS);
    }

    /**
     * Creates a JavaType instance from the specified Type object.
     *
     * <p>This method encapsulates the given Type into a JavaType, allowing for more advanced type operations
     * such as retrieving generic type information, interfaces, and supertypes. This is particularly useful
     * when working with generic types, parameterized types, or when dealing with complex type hierarchies.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * public class Example {
     *     private Map<String, Integer> exampleField;
     *
     *     public static void main(String[] args) throws Throwable {
     *         Field field = Example.class.getDeclaredField("exampleField");
     *         Type fieldType = field.getGenericType();
     *         JavaType javaType = JavaType.from(fieldType); // javaType represents Map<String, Integer>
     *         JavaType mapType = JavaType.from(Map.class);  // mapType represents Map
     *     }
     * }
     * }</pre>
     *
     * @param type the Type object to be encapsulated in a JavaType instance
     * @return a JavaType representing the given type; never null
     */
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
