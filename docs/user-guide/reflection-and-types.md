# Reflection and Types

**Module**: `io.github.microsphere-projects:microsphere-java-core`
**Packages**: `io.microsphere.reflect`, `io.microsphere.reflect.generics`, `io.microsphere.internal.reflect`,
`io.microsphere.beans`

---

## 1. Shape of the helpers

Every class in `io.microsphere.reflect` is:

```java
public abstract class XxxUtils implements io.microsphere.util.Utils {
    private XxxUtils() {}
}
```

Note they are **`abstract` classes implementing the `Utils` marker**, not `final` classes. All methods are `static`.

| Class | One-line purpose |
|---|---|
| `MethodUtils` | find/filter/invoke methods, including inherited ones; method banning |
| `FieldUtils` | find/read/write fields, incl. static and forced-access |
| `ConstructorUtils` | find and instantiate constructors |
| `ReflectionUtils` | caller-class detection, field→Map reading, `InaccessibleObjectException` detection |
| `TypeUtils` | generic `java.lang.reflect.Type` resolution with caching |
| `JavaType` | object model of a `Type` (kind, supertype, interfaces, type arguments) |
| `ClassDefinition` / `MethodDefinition` / `FieldDefinition` / `ConstructorDefinition` | versioned, deprecation-aware reflective handles |
| `Modifier` | enum view of `java.lang.reflect.Modifier` bits |
| `ProxyUtils`, `MemberUtils`, `ExecutableUtils`, `AccessibleObjectUtils` | narrower helpers |
| `generics.ParameterizedTypeImpl`, `generics.TypeArgument` | `ParameterizedType` construction |
| `MultipleType` | pair/tuple of types for comparison |
| `internal.reflect.ConstantPoolUtils` | class-file constant-pool access (JDK-internals) |

---

## 2. `MethodUtils`

### Discovery

```java
public static List<Method> getDeclaredMethods(Class<?> targetClass)
public static List<Method> getMethods(Class<?> targetClass)
public static List<Method> getAllDeclaredMethods(Class<?> targetClass)
public static List<Method> getAllMethods(Class<?> targetClass)

public static List<Method> findDeclaredMethods(Class<?> targetClass, Predicate<? super Method>... methodsToFilter)
public static List<Method> findMethods(Class<?> targetClass, Predicate<? super Method>... methodsToFilter)
public static List<Method> findAllDeclaredMethods(Class<?> targetClass, Predicate<? super Method>... methodsToFilter)
public static List<Method> findAllMethods(Class<?> targetClass, Predicate<? super Method>... methodsToFilter)
public static List<Method> findMethods(Class<?> targetClass, boolean includeInheritedTypes, boolean publicOnly,
                                       Predicate<? super Method>... methodsToFilter)

public static Method findMethod(Class targetClass, String methodName)                        // raw Class param
public static Method findMethod(Class targetClass, String methodName, Class<?>... parameterTypes)
public static Method findDeclaredMethod(Class<?> targetClass, String methodName, Class<?>... parameterTypes)
```

`get*` vs `find*`: `get` returns everything, `find` applies your predicates while walking.
`getAll*` / `findAll*` include inherited members; the unprefixed forms are declared-only.

### Invocation

```java
public static <R> R invokeMethod(Object object, String methodName, Object... arguments)
public static <R> R invokeMethod(boolean forceAccess, Object object, String methodName, Object... arguments)
public static <R> R invokeMethod(Object object, Class<?> type, String methodName, Object... arguments)
public static <R> R invokeMethod(boolean forceAccess, Object object, Class<?> type, String methodName, Object... arguments)
public static <R> R invokeMethod(Object instance, Method method, Object... arguments)
public static <R> R invokeMethod(boolean forceAccess, Object instance, Method method, Object... arguments)

public static <R> R invokeStaticMethod(Class<?> targetClass, String methodName, Object... arguments)
public static <R> R invokeStaticMethod(boolean forceAccess, Class<?> targetClass, String methodName, Object... arguments)
public static <R> R invokeStaticMethod(Method method, Object... arguments)
public static <R> R invokeStaticMethod(boolean forceAccess, Method method, Object... arguments)
```

The `forceAccess` variants call `AccessibleObjectUtils.trySetAccessible` and are what let you invoke non-public
members on JDK 9+ — see the module-path note in [Reference](reference.md#troubleshooting).

### Overriding, predicates and signatures

```java
public static boolean overrides(Method overrider, Method overridden)
public static Method findNearestOverriddenMethod(Method overrider)
public static Method findOverriddenMethod(Method overrider, Class<?> targetClass)
public static Method findFunctionalInterfaceMethod(Class<?> type)

public static String getSignature(Method method)
public static String buildSignature(Class<?> declaringClass, String methodName, Class<?>... parameterTypes)
// format: "io.microsphere.Foo#bar(java.lang.String,int)"

public static boolean isObjectMethod(Method method)
public static boolean isOverridenObjectMethod(Method method)     // sic: "Overriden"
public static boolean isCallerSensitiveMethod(Method method)
public static boolean isGetterMethod(Method method)
public static boolean isSetterMethod(Method method)
public static boolean isIsMethod(Method method)
public static String  getMethodName(Method method)
public static boolean matchesParameterCount(Method method, int parameterCount)
public static boolean matchesReturnType(Method method, Class<?> returnType)

public static Predicate<? super Method> excludedDeclaredClass(Class<?> declaredClass)
```

Ready-made predicates: `OBJECT_PUBLIC_METHODS`, `OBJECT_DECLARED_METHODS`, `OBJECT_METHOD_PREDICATE`,
`PUBLIC_METHOD_PREDICATE`, `STATIC_METHOD_PREDICATE`, `NON_STATIC_METHOD_PREDICATE`, `FINAL_METHOD_PREDICATE`,
`NON_PRIVATE_METHOD_PREDICATE`; name-prefix constants `GET_METHOD_NAME_PREFIX`, `SET_METHOD_NAME_PREFIX`,
`IS_METHOD_NAME_PREFIX`.

```java
// Non-static, non-private methods declared on Foo
List<Method> accessible = MethodUtils.findDeclaredMethods(Foo.class,
        MethodUtils.NON_STATIC_METHOD_PREDICATE,
        MethodUtils.NON_PRIVATE_METHOD_PREDICATE);

// Every public method anywhere in the hierarchy that carries @Exported
List<Method> exported = MethodUtils.findAllMethods(Foo.class,
        MethodUtils.PUBLIC_METHOD_PREDICATE,
        method -> method.isAnnotationPresent(Exported.class));

// Property accessors only
List<Method> getters = MethodUtils.findAllMethods(Foo.class, MethodUtils::isGetterMethod);
List<Method> setters = MethodUtils.findAllMethods(Foo.class, MethodUtils::isSetterMethod);
```

### Banning methods

```java
public static final String BANNED_METHODS_PROPERTY_NAME = "microsphere.reflect.banned-methods";

public static void initBannedMethods()
public static Method banMethod(Class<?> declaredClass, String methodName, Class<?>... parameterTypes)
public static void clearMethodsCache()
public static void clearBannedMethodsCache()
public static void clearDeclaredMethodsCache()
```

`initBannedMethods()` reads `microsphere.reflect.banned-methods` — a `|`-separated list of
`fully.qualified.Class#method(param.Type,other.Type)` signatures — and removes them from discovery results.
**It is not called automatically**; invoke it once at startup if you rely on the property. Programmatic banning
uses `banMethod(...)` directly. Cache sizes are hard-coded (`methodsCache` 256, `declaredMethodsCache` 256,
`bannedMethodsCache` 16); the three `clear*Cache()` methods are the eviction hooks.

---

## 3. `FieldUtils`

```java
// Discovery
public static Field findField(Object object, String fieldName)
public static Field findField(Class<?> klass, String fieldName)
public static Field findField(Class<?> klass, String fieldName, Class<?> fieldType)
public static Field findField(Class<?> klass, String fieldName, Predicate<? super Field>... predicates)
public static Field getDeclaredField(Class<?> declaredClass, String fieldName)
public static Set<Field> findAllFields(Class<?> declaredClass, Predicate<? super Field>... fieldFilters)
public static Set<Field> findAllDeclaredFields(Class<?> declaredClass, Predicate<? super Field>... fieldFilters)

// Reading instance fields
public static <V> V getFieldValue(Object instance, String fieldName)
public static <V> V getFieldValue(boolean forceAccess, Object instance, String fieldName)
public static <V> V getFieldValue(Object instance, String fieldName, V defaultValue)
public static <V> V getFieldValue(boolean forceAccess, Object instance, String fieldName, V defaultValue)
public static <V> V getFieldValue(Object instance, String fieldName, Class<V> fieldType)
public static <V> V getFieldValue(boolean forceAccess, Object instance, String fieldName, Class<V> fieldType)
public static <V> V getFieldValue(Object instance, Field field)
public static <V> V getFieldValue(boolean forceAccess, Object instance, Field field)

// Reading / writing static fields
public static <T> T getStaticFieldValue(Class<?> klass, String fieldName)
public static <T> T getStaticFieldValue(boolean forceAccess, Class<?> klass, String fieldName)
public static <T> T getStaticFieldValue(Field field)
public static <T> T getStaticFieldValue(boolean forceAccess, Field field)
public static <V> V setStaticFieldValue(Class<?> klass, String fieldName, V fieldValue)
public static <V> V setStaticFieldValue(boolean forceAccess, Class<?> klass, String fieldName, V fieldValue)

// Writing instance fields (returns the previous value)
public static <V> V setFieldValue(Object instance, String fieldName, V value)
public static <V> V setFieldValue(boolean forceAccess, Object instance, String fieldName, V value)
public static <V> V setFieldValue(Object instance, Field field, V value)
public static <V> V setFieldValue(boolean forceAccess, Object instance, Field field, V value)

public static void assertFieldMatchType(Object instance, String fieldName, Class<?> expectedType)
```

> [!NOTE]
> There is no `readFieldValue` — the getter is `getFieldValue`, and every read has a `forceAccess` overload for
> private/protected members. `setFieldValue(...)` gives you the old value, so a swap-and-restore is two lines.

```java
Object logger = FieldUtils.getFieldValue(service, "logger");       // null-safe only in the defaultValue form
FieldUtils.setFieldValue(target, "cache", newCache);               // returns previous cache
```

---

## 4. `ConstructorUtils`

```java
public static final Constructor NOT_FOUND_CONSTRUCTOR;   // null
public static boolean isNonPrivateConstructorWithoutParameters(Constructor<?> constructor)
public static boolean hasNonPrivateConstructorWithoutParameters(Class<?> type)
public static List<Constructor<?>> findConstructors(Class<?> type, Predicate<? super Constructor<?>>... filters)
public static List<Constructor<?>> findDeclaredConstructors(Class<?> type, Predicate<? super Constructor<?>>... filters)
public static <T> Constructor<T> getConstructor(Class<T> type, Class<?>... parameterTypes)
public static <T> Constructor<T> getDeclaredConstructor(Class<T> type, Class<?>... parameterTypes)
public static <T> Constructor<T> findConstructor(Class<T> type, Class<?>... parameterTypes)   // null if absent
public static <T> T newInstance(Class<T> type, Object... args)
public static <T> T newInstance(boolean forceAccess, Class<T> type, Object... args)
public static <T> T newInstance(Constructor<T> constructor, Object... args)
public static <T> T newInstance(boolean forceAccess, Constructor<T> constructor, Object... args)
```

`getConstructor` / `getDeclaredConstructor` throw when absent; `findConstructor` returns `null`
(compare with `NOT_FOUND_CONSTRUCTOR`).

---

## 5. `ReflectionUtils`

```java
public static Class<?> getCallerClass() throws IllegalStateException
public static String getCallerClassName()
public static <T> List<T> toList(Object array) throws IllegalArgumentException
public static Map<String, Object> readFieldsAsMap(Object object)
public static boolean isInaccessibleObjectException(Throwable failure)
public static boolean isInaccessibleObjectException(Class<?> throwableClass)

public static final Class<?> SUN_REFLECT_REFLECTION_CLASS;          // nullable
public static final Class<?> STACK_WALKER_CLASS;                     // nullable
public static final Class<?> STACK_WALKER_STACK_FRAME_CLASS;         // nullable
public static final Class<? extends Throwable> INACCESSIBLE_OBJECT_EXCEPTION_CLASS;  // nullable
public static boolean isSupportedSunReflectReflection()
```

* `getCallerClass()` uses `StackWalker` when present and falls back to `sun.reflect.Reflection` — it is how the
  framework picks a default class loader without the caller passing one.
* `readFieldsAsMap(object)` snapshots every non-static field (nested POJOs become nested maps) — handy for `toString`
  and assertions in tests.
* `isInaccessibleObjectException(...)` lets you detect JDK 16+ strong-encapsulation failures portably, since
  `java.lang.reflect.InaccessibleObjectException` does not exist on JDK 8.

---

## 6. `TypeUtils` and generic resolution

### The cache

```java
public static final String RESOLVED_GENERIC_TYPES_CACHE_SIZE_PROPERTY_NAME = "microsphere.reflect.resolved-generic-types.cache.size";
public static final int DEFAULT_RESOLVED_GENERIC_TYPES_CACHE_SIZE = 256;
public static final int RESOLVED_GENERIC_TYPES_CACHE_SIZE;   // read once at class load
```

Resolution results are cached in a `ConcurrentHashMap` bounded by that size; reads are concurrent-safe. Set
`-Dmicrosphere.reflect.resolved-generic-types.cache.size=1024` if you resolve very many parameterized types.

### Resolving type arguments — the main use case

```java
@Nonnull public static List<Type>  resolveActualTypeArguments(Type type, Type baseType)
@Nonnull public static List<Type>  resolveActualTypeArguments(Type type, Class baseClass)
@Nonnull public static Type        resolveActualTypeArgument(Type type, Type baseType, int index)
@Nonnull public static List<Class> resolveActualTypeArgumentClasses(Type type, Type baseType)
@Nullable public static Class      resolveActualTypeArgumentClass(Type type, Class baseType, int index)
@Nonnull public static List<Type>  resolveTypeArguments(Class<?> targetClass)
@Nonnull public static List<Class<?>> resolveTypeArgumentClasses(Class<?> targetClass)
```

> [!IMPORTANT]
> The parameters are `java.lang.reflect.Type`, not `Class` — so a `Type` argument may itself be parameterized, which
> is exactly what lets nested generics resolve. `resolveActualTypeArgumentClass(...)` returns the **raw** `Class` for
> one index, or `null` when the argument is a type variable/wildcard that cannot be reduced.

```java
class StringArrayList extends ArrayList<String> {}

Class<?> elementType = TypeUtils.resolveActualTypeArgumentClass(StringArrayList.class, List.class, 0);
// elementType == String.class

List<Class> args = TypeUtils.resolveActualTypeArgumentClasses(MyRepo.class, JpaRepository.class);
// [User.class, Long.class] — for `class MyRepo implements JpaRepository<User, Long>`
```

This same call is how `Converter<S,T>` and `MultiValueConverter<S>` discover their own type parameters without
declarative metadata — see [Type Conversion](type-conversion.md#5-writing-a-custom-converter).

### Type shape helpers

```java
public static boolean isClass(Object type) / isObjectClass(Class<?>) / isObjectType(Object type)
public static boolean isParameterizedType(Object type) / isTypeVariable(Object type)
public static boolean isWildcardType(Object type) / isGenericArrayType(Object type)
public static boolean isActualType(Type type)

@Nullable public static Type    getRawType(Type type)
@Nullable public static Class<?> getRawClass(Type type)
@Nullable public static Class<?> asClass(Type type)
@Nullable public static ParameterizedType asParameterizedType(Type type)
@Nullable public static TypeVariable asTypeVariable(Type type)
@Nullable public static WildcardType asWildcardType(Type type)
@Nullable public static GenericArrayType asGenericArrayType(Type type)
@Nullable public static Type getComponentType(Type type)

public static boolean isAssignableFrom(Type superType, Type targetType)
public static boolean isAssignableFrom(Class<?> superClass, Type targetType)

@Nullable public static String getClassName(Type type)
@Nullable public static String getTypeName(Type type)
@Nonnull  public static String[] getTypeNames(Type... types)
@Nonnull  public static Set<String> getClassNames(Iterable<? extends Type> types)
```

### Hierarchy walking over `Type`

```java
public static List<Type>           getAllGenericSuperclasses(Type type)
public static List<Type>           findAllGenericSuperclasses(Type type, Predicate<? super Type>... typeFilters)
public static List<Type>           getAllGenericInterfaces(Type type)
public static List<Type>           findAllGenericInterfaces(Type type, Predicate<? super Type>... typeFilters)
public static List<ParameterizedType> getParameterizedTypes(Type type)
public static List<ParameterizedType> findParameterizedTypes(Type type, Predicate<? super ParameterizedType>... filters)
public static List<ParameterizedType> getAllParameterizedTypes(Type type)
public static List<ParameterizedType> findAllParameterizedTypes(Type type, Predicate<? super ParameterizedType>... filters)
public static List<Type>           getHierarchicalTypes(Type type)
public static List<Type>           findHierarchicalTypes(Type type, Predicate<? super Type>... typeFilters)
public static List<Type>           getAllTypes(Type type)
public static List<Type>           findAllTypes(Type type, Predicate<? super Type>... typeFilters)
```

Reusable predicates: `NON_OBJECT_TYPE_FILTER`, `NON_OBJECT_CLASS_FILTER`, `TYPE_VARIABLE_FILTER`,
`PARAMETERIZED_TYPE_FILTER`, `WILDCARD_TYPE_FILTER`, `GENERIC_ARRAY_TYPE_FILTER`.

```java
// Parameterized supertypes of MyRepo that are JpaRepository instances
List<ParameterizedType> repositories = TypeUtils.findAllParameterizedTypes(MyRepo.class,
        pt -> TypeUtils.getRawClass(pt) == JpaRepository.class);

// All generic interfaces excluding java.lang.Object's types
List<Type> interfaces = TypeUtils.findAllGenericInterfaces(MyRepo.class, TypeUtils.NON_OBJECT_TYPE_FILTER);
```

### Building a `ParameterizedType` by hand

```java
public class ParameterizedTypeImpl implements ParameterizedType {
    public static ParameterizedTypeImpl of(Class<?> rawType, Type... actualTypeArguments)
    public static ParameterizedTypeImpl of(Class<?> rawType, Type[] actualTypeArguments, Type ownerType)
    public Type[] getActualTypeArguments()
    public Class<?> getRawType()
    public Type getOwnerType()
}

public class TypeArgument {
    public static TypeArgument create(Type type, int index)
    public Type getType()
    public int getIndex()
}
```

```java
ParameterizedType listOfStrings = ParameterizedTypeImpl.of(List.class, String.class);
// TypeUtils.getTypeName(...) renders it as java.util.List<java.lang.String>
```

`ParameterizedTypeImpl`'s constructor is private — always go through `of(...)`.

---

## 7. `JavaType` — an object model of a `Type`

`public class JavaType implements Serializable`, `protected` constructors, static factories:

```java
public static final JavaType[] EMPTY_JAVA_TYPE_ARRAY;
public static final JavaType OBJECT_JAVA_TYPE;
public static final JavaType NULL_JAVA_TYPE;

// From a class or Type
@Nonnull public static JavaType from(Class<?> targetClass)
@Nonnull public static JavaType from(Type type)

// From members
@Nullable public static JavaType fromField(Class<?> declaredClass, String fieldName)
@Nonnull  public static JavaType fromField(Field field)
@Nonnull  public static JavaType fromMethodReturnType(Class<?> declaredClass, String methodName, Class<?>... parameterTypes)
@Nonnull  public static JavaType fromMethodReturnType(Method method)
@Nonnull  public static JavaType[] fromMethodParameters(Class<?> declaredClass, String methodName, Class<?>... parameterTypes)
@Nonnull  public static JavaType[] fromMethodParameters(Method method)
@Nonnull  public static JavaType fromMethodParameter(Method method, int parameterIndex)

// Navigation
@Nonnull  public Type getType()
@Nonnull  public Kind getKind()
@Nullable public JavaType getSource()
@Nullable public JavaType getRootSource()
@Nullable public Type getRawType()
@Nullable public JavaType getSuperType()
@Nonnull  public JavaType[] getInterfaces()
@Nonnull  public JavaType getInterface(int interfaceIndex) throws IndexOutOfBoundsException
@Nonnull  public JavaType[] getGenericTypes()
@Nonnull  public JavaType getGenericType(int genericTypeIndex) throws IndexOutOfBoundsException
@Nullable public JavaType as(Class<?> targetClass)     // navigate this type "as" a supertype view

// Narrowing
@Nullable public <T> Class<T> toClass()
@Nullable public ParameterizedType toParameterizedType()
@Nullable public TypeVariable toTypeVariable()
@Nullable public WildcardType toWildcardType()
@Nullable public GenericArrayType toGenericArrayType()

// Tests
public boolean isSource() / isRootSource() / isClass() / isParameterizedType()
public boolean isTypeVariable() / isWildCardType() / isGenericArrayType() / isUnknownType()

public enum Kind {
    CLASS, PARAMETERIZED_TYPE, TYPE_VARIABLE, WILDCARD_TYPE, GENERIC_ARRAY_TYPE, UNKNOWN;
    public static Kind valueOf(Type type)
    public Type getRawType(Type type)
    public Type getSuperType(Type type)
    public Type[] getInterfaces(Type type)
    public Type[] getGenericTypes(JavaType javaType)
}
```

> [!NOTE]
> The boolean test is `isWildCardType()` (capital `C`), while the narrowing method is `toWildcardType()` (lowercase
> `c`). And `as(Class)` is the way to re-root a view: `javaType.as(Map.class)` gives you the same type expressed
> against `Map`, so its `getGenericTypes()` are `Map`'s `K`/`V`.

```java
JavaType type = JavaType.fromField(Order.class, "lines");
type.getKind();                        // PARAMETERIZED_TYPE
type.getRawType();                     // java.util.List
type.getGenericTypes()[0].getType();   // java.lang.String  (for List<String> lines)
```

`JavaType` is what you want when you must walk generics as *objects with provenance* rather than as raw `Type`s.

---

## 8. `Modifier`, `ProxyUtils`, `MemberUtils`, `ExecutableUtils`, `AccessibleObjectUtils`

### `Modifier` (enum)

```java
public enum Modifier {
    PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, SYNCHRONIZED, VOLATILE, TRANSIENT, NATIVE,
    INTERFACE, ABSTRACT, STRICT, BRIDGE, VARARGS, SYNTHETIC, ANNOTATION, ENUM, MANDATED;

    public int getValue()
    public boolean matches(int mod)

    public static boolean isPublic(int modifiers)      // and isPrivate/isProtected/isStatic/isFinal/
    public static boolean matchesAny(int modifiers, Modifier... toMatch)          // isSynchronized/.../isMandated
    public static boolean matchesAll(int modifiers, Modifier... toMatch)
    public static boolean matchesAny(Supplier<Integer> modifiersSupplier, Modifier... toMatch)
    public static boolean matchesAll(Supplier<Integer> modifiersSupplier, Modifier... toMatch)
}
```

The last six constants (`BRIDGE 0x40`, `VARARGS 0x80`, `SYNTHETIC 0x1000`, `ANNOTATION 0x2000`, `ENUM 0x4000`,
`MANDATED 0x8000`) are bits `java.lang.reflect.Modifier` does not expose as named helpers — which is why filtering
generated/synthetic members needs this enum rather than `Modifier.isStatic(...)`.

### The rest

```java
// ProxyUtils
public static boolean isProxyable(Class<?> type)

// AccessibleObjectUtils
public static boolean trySetAccessible(AccessibleObject accessibleObject)   // false instead of throwing on JDK 16+
public static boolean canAccess(Object target, AccessibleObject accessibleObject)

// MemberUtils
public static boolean isStatic(Member m) / isAbstract / isNonStatic / isFinal / isPrivate / isPublic / isNonPrivate
public static Member asMember(Object obj)
public static boolean isInvalidDeclaringClass(Class<?> type)
// ...plus member predicates

// ExecutableUtils
public static <E extends Executable & Member> void execute(E executable, ThrowableConsumer<E> consumer)
public static <E extends Executable & Member, R> R execute(E executable, ThrowableSupplier<R> supplier)
public static <E extends Executable & Member, R> R execute(E executable, ThrowableFunction<E, R> function)
public static boolean matchParameterTypes(Executable executable, Object... arguments)
```

`ExecutableUtils.execute(...)` is the bridge to [Language Abstractions](language-abstractions.md#5-throwable-aware-functional-interfaces):
it lets a reflective call site use `Throwable*` lambdas without an inline try/catch.

---

## 9. Reflective definitions (`ClassDefinition`, `MethodDefinition`, …)

A `*Definition` is an immutable, versioned, deprecation-aware handle to a member that may not exist in the current
runtime — designed for libraries that support a range of dependency versions.

```java
public abstract class ReflectiveDefinition implements Serializable {
    public final Version getSince()
    public final Deprecation getDeprecation()
    public final String getClassName()
    public final Class<?> getResolvedClass()
    public final boolean isDeprecated()
    public abstract boolean isPresent();
}

@Immutable public final class ClassDefinition extends ReflectiveDefinition {
    public ClassDefinition(String since, String className)
    public ClassDefinition(String since, Deprecation deprecation, String className)
    public ClassDefinition(Version since, String className)
    public ClassDefinition(Version since, Deprecation deprecation, String className)
    @Override public boolean isPresent()
}

public abstract class MemberDefinition<M extends Member> extends ReflectiveDefinition {
    public final String getName()
    public final String getDeclaredClassName()
    public final Class<?> getDeclaredClass()
    public final M getMember()
    public boolean isPresent()
}

public final class MethodDefinition extends ExecutableDefinition<Method> {
    public MethodDefinition(String since, String declaredClassName, String methodName, String... parameterClassNames)
    public MethodDefinition(String since, Deprecation deprecation, String declaredClassName, String methodName, String... parameterClassNames)
    public MethodDefinition(Version since, String declaredClassName, String methodName, String... parameterClassNames)
    public MethodDefinition(Version since, Deprecation deprecation, String declaredClassName, String methodName, String... parameterClassNames)
    @Nonnull public String getMethodName()
    @Nullable public Method getMethod()
    public <R> R invoke(Object instance, Object... args) throws IllegalStateException, IllegalArgumentException, RuntimeException
}

public class ExecutableDefinition<E extends Executable> extends MemberDefinition<E> {
    public final String[] getParameterClassNames()
    public final Class<?>[] getParameterTypes()
}

@Immutable public final class FieldDefinition extends MemberDefinition<Field> {
    @Nonnull public String getFieldName()
    public Field getResolvedField()
    public <T> T get(Object instance)
    public <T> T set(Object instance, T fieldValue)
}

@Immutable public final class ConstructorDefinition extends ExecutableDefinition<Constructor> {
    public Constructor<?> getConstructor()
    public <T> T newInstance(Object... args)
}
```

`ConstructorDefinition`'s parameter list is `(since, declaredClassName, parameterClassNames...)` — the class name and
its parameter type names are all strings, exactly as in `MethodDefinition`:

```java
ConstructorDefinition stringFromInt = new ConstructorDefinition("1.0.0", "java.lang.String", "java.lang.Integer");
String value = stringFromInt.newInstance(123);
```

```java
MethodDefinition optionalEmpty = new MethodDefinition("1.8.0", "java.util.Optional", "empty");

if (optionalEmpty.isPresent()) {
    Object empty = optionalEmpty.invoke(null);   // static: instance is null
}
```

Parameters are given as **class names** (strings), so constructing a definition never fails on a missing class —
resolution is deferred to `getMethod()` / `isPresent()`. `Deprecation` from
[Language Abstractions](language-abstractions.md#3-deprecation) attaches the migration story.

---

## 10. `ConstantPoolUtils` (`io.microsphere.internal.reflect`)

Reads a class's constant pool through `jdk.internal.reflect.ConstantPool` (JDK 9+) or
`sun.reflect.ConstantPool` (JDK 8):

```java
public static final Class<?> CONSTANT_POOL_CLASS;
public static int getSize(Class<?> targetClass)
public static Class<?> getClassAt(Class<?> targetClass, int index)
public static Class<?> getClassAtIfLoaded(Class<?> targetClass, int index)
public static Integer getClassRefIndexAt(Class<?> targetClass, int index)
public static Member getMethodAt(Class<?> targetClass, int index) / getMethodAtIfLoaded(...)
public static Field getFieldAt(Class<?> targetClass, int index) / getFieldAtIfLoaded(...)
public static String[] getMemberRefInfoAt(Class<?> targetClass, int index)
public static Integer getNameAndTypeRefIndexAt(Class<?> targetClass, int index)
public static String[] getNameAndTypeRefInfoAt(Class<?> targetClass, int index)
public static Integer getIntAt(Class<?>, int) / Long getLongAt / Float getFloatAt / Double getDoubleAt
public static String getStringAt(Class<?>, int) / getUTF8At(Class<?>, int)
```

This requires `--add-opens java.base/jdk.internal.reflect=ALL-UNNAMED` (and the equivalent for `sun.reflect`) on
modern JDKs and is **internal**: it exists so bytecode-level tooling in this ecosystem works, and its API may change.

---

## 11. `io.microsphere.beans` — bean introspection

### `BeanUtils`

```java
public static final String BEAN_PROPERTIES_MAX_RESOLVED_DEPTH_PROPERTY_NAME = "microsphere.bean.properties.max-resolved-depth"; // default 100
public static final String BEAN_METADATA_CACHE_SIZE_PROPERTY_NAME          = "microsphere.bean.metadata.cache.size";        // default 64

@Nonnull public static Map<String, Object> resolvePropertiesAsMap(Object bean)
@Nonnull public static Map<String, Object> resolvePropertiesAsMap(Object bean, int maxResolvedDepth)
@Nonnull public static BeanMetadata getBeanMetadata(Class<?> beanClass) throws RuntimeException
public static Method findWriteMethod(BeanMetadata beanMetadata, String propertyName)
public static PropertyDescriptor findPropertyDescriptor(BeanMetadata beanMetadata, String propertyName)
```

`resolvePropertiesAsMap` walks nested beans (bounded by `maxResolvedDepth`, default 100) and is the engine behind
`ReflectiveConfigurationPropertyGenerator` ([Configuration Property Metadata](configuration-metadata.md)).

### `BeanMetadata` / `BeanProperty`

```java
public class BeanMetadata {
    public static BeanMetadata of(Class<?> beanClass) throws RuntimeException
    public BeanInfo getBeanInfo()
    public Class<?> getBeanClass()
    @Nonnull public Collection<PropertyDescriptor> getPropertyDescriptors()
    public PropertyDescriptor getPropertyDescriptor(String propertyName)   // keys are uncapitalized
    @Nonnull public Map<String, PropertyDescriptor> getPropertyDescriptorsMap()
}

public class BeanProperty {
    public BeanProperty(String name, Class<?> beanClass, PropertyDescriptor descriptor)
    public static BeanProperty of(Object bean, String propertyName)
    @Nonnull public String getName()
    @Nullable public Object getValue()
    public void setValue(Object value)
    @Nonnull public Class<?> getBeanClass()
    @Nonnull public PropertyDescriptor getDescriptor()
}
```

`BeanMetadata` wraps `Introspector.getBeanInfo(beanClass, Object.class)`, so the four `Object` methods are excluded,
and property keys are **uncapitalized** (`getName` → `"name"`).

### `ConfigurationProperty` (the POJO)

`io.microsphere.beans.ConfigurationProperty` is the data object shared by the annotation, the metadata SPI and the
annotation processor. Note that `getType()` returns a **`String`** type name, not a `Class`:

```java
public ConfigurationProperty(String name)                       // type defaults to String.class
public ConfigurationProperty(String name, Class<?> type)

@Nonnull public String getName()
@Nonnull public String getType()                               // e.g. "int", "java.time.Duration"
public void setType(Class<?> type)
public void setType(String type)
@Nullable public Object getValue()
public void setValue(Object value)
@Nullable public Object getDefaultValue()
public void setDefaultValue(Object defaultValue)
public boolean isRequired()
public void setRequired(boolean required)
@Nonnull public String getDescription()
public void setDescription(String description)
@Nonnull public Metadata getMetadata()

public static class Metadata {
    public Set<String> getSources()        // lazily created, never null
    public Set<String> getTargets()
    public String getDeclaredClass()
    public String getDeclaredField()
    public void setDeclaredClass(String declaredClass)
    public void setDeclaredField(String declaredField)
}
```

---

## 12. See also

* [Type Conversion](type-conversion.md) — the largest consumer of `TypeUtils`
* [Configuration Property Metadata](configuration-metadata.md) — `ConfigurationProperty` and its readers
* [Reference](reference.md) — reflective access under strong encapsulation

[← Previous: Language Abstractions](language-abstractions.md) · [Index](README.md) · [Next: Type Conversion →](type-conversion.md)
