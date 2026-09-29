# Reflection and Types

> Read this page in: [中文](../zh/reflection-and-types.md) · [English](reflection-and-types.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Packages | `io.microsphere.reflect`, `io.microsphere.reflect.generics`, `io.microsphere.internal.reflect`, `io.microsphere.beans` |
| Style | `public abstract class XxxUtils implements Utils` + private constructor; all methods `static` |
| Generic resolution | `TypeUtils` (cached by `MultipleType` key) and `JavaType` (object model of a `Type`) |
| Bean introspection | `BeanUtils` / `BeanMetadata` / `BeanProperty` over `java.beans.Introspector` |
| JDK 9+ access | reads/invokes have `forceAccess` overloads; `trySetAccessible` returns `false` instead of throwing |

---

## 1. Inventory

| Class | One-line purpose |
|---|---|
| `MethodUtils` | find/filter/invoke methods incl. inherited ones; method banning |
| `FieldUtils` | find/read/write fields, incl. static and forced-access |
| `ConstructorUtils` | find and instantiate constructors |
| `ReflectionUtils` | caller-class detection, fields→Map reading, `InaccessibleObjectException` detection |
| `TypeUtils` | generic `java.lang.reflect.Type` resolution with caching |
| `JavaType` | object model of a `Type` (kind, supertype, interfaces, type arguments) |
| `ClassDefinition` / `MethodDefinition` / `FieldDefinition` / `ConstructorDefinition` | versioned, deprecation-aware reflective handles |
| `Modifier` | enum view of `java.lang.reflect.Modifier` bits |
| `ProxyUtils`, `MemberUtils`, `ExecutableUtils`, `AccessibleObjectUtils` | narrower helpers |
| `MultipleType` | pair/tuple of types — the cache key of `TypeUtils` |
| `generics.ParameterizedTypeImpl`, `generics.TypeArgument` | `ParameterizedType` construction |
| `internal.reflect.ConstantPoolUtils` | class-file constant-pool access (JDK internals, unstable) |

---

## 2. `MethodUtils`

`getX` returns everything; `findX` applies your predicates while walking. `getAll*` / `findAll*` include
inherited members; the unprefixed forms are declared-only.

```java
List<Method> getDeclaredMethods / getMethods / getAllDeclaredMethods / getAllMethods(Class<?> targetClass)
List<Method> findDeclaredMethods / findMethods / findAllDeclaredMethods / findAllMethods(
        Class<?> targetClass, Predicate<? super Method>... methodsToFilter)
List<Method> findMethods(Class<?> targetClass, boolean includeInheritedTypes, boolean publicOnly,
        Predicate<? super Method>... methodsToFilter)
Method findMethod(Class targetClass, String methodName [, Class<?>... parameterTypes])  // raw Class param
Method findDeclaredMethod(Class<?> targetClass, String methodName, Class<?>... parameterTypes)

// Ready-made filters and names
List<Method>  OBJECT_PUBLIC_METHODS, OBJECT_DECLARED_METHODS;
Predicate     OBJECT_METHOD_PREDICATE, PUBLIC_METHOD_PREDICATE, STATIC_METHOD_PREDICATE,
              NON_STATIC_METHOD_PREDICATE, FINAL_METHOD_PREDICATE, NON_PRIVATE_METHOD_PREDICATE;
String        GET_METHOD_NAME_PREFIX, SET_METHOD_NAME_PREFIX, IS_METHOD_NAME_PREFIX; // "get" "set" "is"
Predicate<? super Method> excludedDeclaredClass(Class<?> declaredClass);
```

```java
// Every public method anywhere in the hierarchy that carries @Exported
List<Method> exported = MethodUtils.findAllMethods(Foo.class,
        MethodUtils.PUBLIC_METHOD_PREDICATE,
        method -> method.isAnnotationPresent(Exported.class));
```

### Invocation

```java
R invokeMethod(Object object, String methodName, Object... arguments)
R invokeMethod(Object object, Class<?> type, String methodName, Object... arguments)
R invokeMethod(Object instance, Method method, Object... arguments)
R invokeStaticMethod(Class<?> targetClass, String methodName, Object... arguments)
R invokeStaticMethod(Method method, Object... arguments)
// every form above also exists with a leading `boolean forceAccess`
```

`forceAccess` variants call `AccessibleObjectUtils.trySetAccessible` (returns `false` instead of throwing) —
this is how you reach non-public members on JDK 9+ without crashing on strong encapsulation.

### Overriding, signatures, classification

```java
boolean overrides(Method overrider, Method overridden)
Method  findNearestOverriddenMethod / findOverriddenMethod(Method overrider [, Class<?> targetClass])
Method  findFunctionalInterfaceMethod(Class<?> type)
String  getSignature(Method method)
String  buildSignature(Class<?> declaringClass, String methodName, Class<?>... parameterTypes)
        // format: "io.microsphere.Foo#bar(java.lang.String,int)"
boolean isObjectMethod / isOverridenObjectMethod(Method)   // sic: "Overriden" is the real name
boolean isCallerSensitiveMethod / isGetterMethod / isSetterMethod / isIsMethod(Method)
String  getMethodName(Method method)                       // null-safe method.getName()
boolean matchesParameterCount(Method, int) / matchesReturnType(Method, Class<?>)
```

### Banning methods

```java
String   BANNED_METHODS_PROPERTY_NAME;   // "microsphere.reflect.banned-methods" — a |-separated list of
void     initBannedMethods()             // fully.qualified.Class#method(param.Type,other.Type) signatures
Method   banMethod(Class<?> declaredClass, String methodName, Class<?>... parameterTypes)
void     clearMethodsCache() / clearBannedMethodsCache() / clearDeclaredMethodsCache()
```

`initBannedMethods()` removes the property-listed methods from discovery results and **is not called
automatically** — invoke it once at startup if you rely on the property. Caches: `methodsCache` 256,
`declaredMethodsCache` 256, `bannedMethodsCache` 16; the `clear*Cache()` methods are the only eviction hooks.

---

## 3. `FieldUtils` and `ConstructorUtils`

```java
// FieldUtils — discovery (findField walks superclasses; getDeclaredField does not)
Field findField(Object object, String fieldName)
Field findField(Class<?> klass, String fieldName [, Class<?> fieldType | Predicate<? super Field>... predicates])
Field getDeclaredField(Class<?> declaredClass, String fieldName)
Set<Field> findAllFields / findAllDeclaredFields(Class<?> declaredClass, Predicate<? super Field>... fieldFilters)
// FieldUtils — access; EVERY form below also exists with a leading boolean forceAccess.
// There is no readFieldValue; setFieldValue / setStaticFieldValue return the PREVIOUS value.
V getFieldValue(Object instance, String fieldName [, V defaultValue | Class<V> fieldType] | Field field)
V setFieldValue(Object instance, String fieldName | Field field, V value)
T getStaticFieldValue(Class<?> klass, String fieldName | Field field)
V setStaticFieldValue(Class<?> klass, String fieldName, V fieldValue)
void assertFieldMatchType(Object instance, String fieldName, Class<?> expectedType)
```

```java
// ConstructorUtils
Constructor NOT_FOUND_CONSTRUCTOR;                    // null
boolean isNonPrivateConstructorWithoutParameters(Constructor<?>) / hasNonPrivateConstructorWithoutParameters(Class<?>)
List<Constructor<?>> findConstructors / findDeclaredConstructors(Class<?> type, Predicate<? super Constructor<?>>... filters)
Constructor<T> getConstructor / getDeclaredConstructor(Class<T> type, Class<?>... parameterTypes)   // throw when absent
Constructor<T> findConstructor(Class<T> type, Class<?>... parameterTypes)                            // null when absent
T newInstance(Class<T> type | Constructor<T> constructor, Object... args)   // also + leading boolean forceAccess
```

---

## 4. `ReflectionUtils`

```java
Class<?> getCallerClass() throws IllegalStateException   // StackWalker, falling back to sun.reflect.Reflection;
                                                         // how the library picks a default class loader
String   getCallerClassName()
<T> List<T> toList(Object array)
Map<String, Object> readFieldsAsMap(Object object)       // snapshot of all non-static fields, nested POJOs -> nested maps
boolean isInaccessibleObjectException(Throwable failure | Class<?> throwableClass)  // portable on Java 8
Class<?> SUN_REFLECT_REFLECTION_CLASS, STACK_WALKER_CLASS, STACK_WALKER_STACK_FRAME_CLASS;   // nullable
Class<? extends Throwable> INACCESSIBLE_OBJECT_EXCEPTION_CLASS;                              // nullable
boolean isSupportedSunReflectReflection()
```

---

## 5. `TypeUtils` — generic resolution

```java
List<Type> resolveActualTypeArguments(Type type, Type baseType | Class baseClass)
Type       resolveActualTypeArgument(Type type, Type baseType, int index)
List<Class> resolveActualTypeArgumentClasses(Type type, Type baseType)
Class      resolveActualTypeArgumentClass(Type type, Class baseType, int index)   // raw Class or null
List<Type> resolveTypeArguments(Class<?> targetClass)
List<Class<?>> resolveTypeArgumentClasses(Class<?> targetClass)
```

> [!IMPORTANT]
> The parameters are `java.lang.reflect.Type`, not `Class` — a `Type` argument may itself be parameterized,
> which is what lets nested generics resolve. `resolveActualTypeArgumentClass` returns the **raw** `Class`
> for one index, or `null` when the argument is a type variable/wildcard that cannot be reduced.

```java
class StringArrayList extends ArrayList<String> {}
Class<?> elementType = TypeUtils.resolveActualTypeArgumentClass(StringArrayList.class, List.class, 0);
// elementType == String.class
```

This same call is how `Converter<S, T>` and `MultiValueConverter<S>` discover their own type parameters
without declarative metadata — see [Type Conversion](type-conversion.md). Results are cached in a
concurrent map keyed by `MultipleType.of(type, baseType)`, sized at class load by
`-Dmicrosphere.reflect.resolved-generic-types.cache.size` (default 256).

### Shape tests, narrowing, hierarchy walking

```java
boolean isClass / isObjectClass / isObjectType / isParameterizedType / isTypeVariable / isWildcardType
        / isGenericArrayType(Object type | Type)      // per-shape tests
boolean isActualType(Type)
Type     getRawType / Class<?> getRawClass / Class<?> asClass(Type)
ParameterizedType asParameterizedType / TypeVariable asTypeVariable / WildcardType asWildcardType
GenericArrayType asGenericArrayType / Type getComponentType / String getTypeName / String getClassName(Type)
String[] getTypeNames(Type...) / Set<String> getClassNames(Iterable<? extends Type>)
boolean isAssignableFrom(Type superType | Class<?> superClass, Type targetType)

List<Type> getAllGenericSuperclasses / findAllGenericSuperclasses(Type [, Predicate<? super Type>...])
List<Type> getAllGenericInterfaces / findAllGenericInterfaces(Type [, ...])
List<ParameterizedType> getParameterizedTypes / findParameterizedTypes(Type [, Predicate<? super ParameterizedType>...])
List<ParameterizedType> getAllParameterizedTypes / findAllParameterizedTypes(Type [, ...])
List<Type> getHierarchicalTypes / findHierarchicalTypes / getAllTypes / findAllTypes(Type [, ...])
```

`get*` is `find*` with an empty filter; the narrowing helpers return `null` when the shape does not match.
Reusable predicates: `NON_OBJECT_TYPE_FILTER`, `NON_OBJECT_CLASS_FILTER`, `TYPE_VARIABLE_FILTER`,
`PARAMETERIZED_TYPE_FILTER`, `WILDCARD_TYPE_FILTER`, `GENERIC_ARRAY_TYPE_FILTER`.

### Building a `ParameterizedType` by hand

```java
ParameterizedType listOfStrings = ParameterizedTypeImpl.of(List.class, String.class);
// TypeUtils.getTypeName(...) renders it as java.util.List<java.lang.String>
```

`ParameterizedTypeImpl.of(rawType, Type... args)` and `of(rawType, Type[] args, Type ownerType)` are the
only entry points (private constructor); `TypeArgument.create(Type type, int index)` pairs a type with its
index.

---

## 6. `JavaType` — an object model of a `Type`

`public class JavaType implements Serializable`, `protected` constructors, static factories:

```java
JavaType[] EMPTY_JAVA_TYPE_ARRAY;  JavaType OBJECT_JAVA_TYPE, NULL_JAVA_TYPE;
// Factories
JavaType from(Class<?> | Type)
JavaType fromField(Class<?> declaredClass, String fieldName)   // @Nullable — absent field
JavaType fromField(Field) / fromMethodReturnType(Class<?> + name + params | Method)
JavaType[] fromMethodParameters(Class<?> + name + params | Method)
JavaType fromMethodParameter(Method, int parameterIndex)
// Navigation
Type getType();  Kind getKind();  Type getRawType();
JavaType getSource();  JavaType getRootSource();                // provenance of this node
JavaType getSuperType();  JavaType[] getInterfaces();  JavaType getInterface(int)
JavaType[] getGenericTypes();  JavaType getGenericType(int)
JavaType as(Class<?> targetClass)                               // re-root the view against a supertype
// Narrowing (null when the type is not of that shape)
Class<T> toClass();  ParameterizedType toParameterizedType();  TypeVariable toTypeVariable()
WildcardType toWildcardType();  GenericArrayType toGenericArrayType()
// Tests
boolean isSource / isRootSource / isClass / isParameterizedType / isTypeVariable
boolean isWildCardType / isGenericArrayType / isUnknownType
enum Kind { CLASS, PARAMETERIZED_TYPE, TYPE_VARIABLE, WILDCARD_TYPE, GENERIC_ARRAY_TYPE, UNKNOWN;
    Kind valueOf(Type);  Type getRawType(Type);  Type getSuperType(Type);
    Type[] getInterfaces(Type);  Type[] getGenericTypes(JavaType); }
```

> [!NOTE]
> The test is `isWildCardType()` (capital `C`) while the narrowing method is `toWildcardType()` (lowercase
> `c`). `as(Class)` re-roots a view: `javaType.as(Map.class)` expresses the same type against `Map`, so its
> `getGenericTypes()` are `Map`'s `K`/`V`.

```java
JavaType type = JavaType.fromField(Order.class, "lines");
type.getKind();                        // PARAMETERIZED_TYPE
type.getRawType();                     // java.util.List
type.getGenericTypes()[0].getType();   // java.lang.String  (for List<String> lines)
```

Use `JavaType` when generics must be walked as *objects with provenance*; raw `Type`s from `TypeUtils`
suffice otherwise.

---

## 7. Version-aware handles: `*Definition`

A `*Definition` is an immutable, deprecation-aware handle to a class or member that **may not exist in the
current runtime**. Parameters are **class-name strings**, so construction never fails on a missing class;
resolution is deferred to `isPresent()` / `getResolvedClass()` / `getMethod()`.

```java
abstract class ReflectiveDefinition implements Serializable {
    Version getSince();  Deprecation getDeprecation();  String getClassName();
    Class<?> getResolvedClass();  boolean isDeprecated();  abstract boolean isPresent();
}
final class ClassDefinition extends ReflectiveDefinition {
    ClassDefinition(String since | Version since, [Deprecation,] String className)
}
abstract class MemberDefinition<M extends Member> extends ReflectiveDefinition {
    String getName();  String getDeclaredClassName();  Class<?> getDeclaredClass();  M getMember();
}
final class MethodDefinition extends ExecutableDefinition<Method> {
    MethodDefinition(String since | Version since, [Deprecation,]
                     String declaredClassName, String methodName, String... parameterClassNames)
    String getMethodName();  Method getMethod();  R invoke(Object instance, Object... args)
}
class ExecutableDefinition<E extends Executable> extends MemberDefinition<E> {
    String[] getParameterClassNames();  Class<?>[] getParameterTypes();
}
final class FieldDefinition extends MemberDefinition<Field> {
    String getFieldName();  Field getResolvedField();
    T get(Object instance);  T set(Object instance, T fieldValue)
}
final class ConstructorDefinition extends ExecutableDefinition<Constructor> {
    Constructor<?> getConstructor();  T newInstance(Object... args)
}
```

```java
MethodDefinition optionalEmpty = new MethodDefinition("1.8.0", "java.util.Optional", "empty");
if (optionalEmpty.isPresent()) {
    Object empty = optionalEmpty.invoke(null);        // static: instance is null
}
ConstructorDefinition c = new ConstructorDefinition("1.0.0", "java.lang.String", "java.lang.Integer");
String value = c.newInstance(123);
```

`Version` and `Deprecation` come from [Language Abstractions](language-abstractions.md).

---

## 8. `Modifier` and the narrower helpers

```java
enum Modifier {   // java.lang.reflect.Modifier bits as an enum
    PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, SYNCHRONIZED, VOLATILE, TRANSIENT, NATIVE,
    INTERFACE, ABSTRACT, STRICT, BRIDGE, VARARGS, SYNTHETIC, ANNOTATION, ENUM, MANDATED;
    int getValue();  boolean matches(int mod);
    boolean isPublic(int) ... isMandated(int);                    // one per constant
    boolean matchesAny / matchesAll(int modifiers, Modifier...);  // (+ Supplier<Integer> variants)
}
```

The last six constants (`BRIDGE 0x40`, `VARARGS 0x80`, `SYNTHETIC 0x1000`, `ANNOTATION 0x2000`,
`ENUM 0x4000`, `MANDATED 0x8000`) are bits `java.lang.reflect.Modifier` does not expose as named helpers —
which is why filtering generated/synthetic members needs this enum.

```java
boolean ProxyUtils.isProxyable(Class<?> type)
boolean AccessibleObjectUtils.trySetAccessible(AccessibleObject)   // false, not an exception, on JDK 16+
boolean AccessibleObjectUtils.canAccess(Object target, AccessibleObject)
// MemberUtils (+ predicates *_MEMBER_PREDICATE for static/non-static/final/public/non-private)
boolean MemberUtils.isStatic / isAbstract / isNonStatic / isFinal / isPrivate / isPublic / isNonPrivate(Member)
Member  MemberUtils.asMember(Object obj)
boolean MemberUtils.isInvalidDeclaringClass(Class<?> type)
// ExecutableUtils — bridges reflection to Throwable* lambdas (no inline try/catch)
void    ExecutableUtils.execute(E executable, ThrowableConsumer<E>)          // E extends Executable & Member
R       ExecutableUtils.execute(E, ThrowableSupplier<R> | ThrowableFunction<E, R>)
boolean ExecutableUtils.matchParameterTypes(Executable, Object... arguments)
```

`ConstantPoolUtils` (`io.microsphere.internal.reflect`) reads a class's constant pool through
`jdk.internal.reflect.ConstantPool` (JDK 9+) or `sun.reflect.ConstantPool` (JDK 8): `getSize`,
`getClassAt`, `getMethodAt`, `getFieldAt`, `getStringAt`, ... all `(Class<?>, int index)`. It requires
`--add-opens java.base/jdk.internal.reflect=ALL-UNNAMED` and is **internal** — stay off it unless you write
bytecode tooling.

---

## 9. Bean introspection (`io.microsphere.beans`)

```java
String BEAN_PROPERTIES_MAX_RESOLVED_DEPTH_PROPERTY_NAME;  // "microsphere.bean.properties.max-resolved-depth", default 100
String BEAN_METADATA_CACHE_SIZE_PROPERTY_NAME;            // "microsphere.bean.metadata.cache.size", default 64

Map<String, Object> BeanUtils.resolvePropertiesAsMap(Object bean [, int maxResolvedDepth])  // nested beans -> nested maps
BeanMetadata BeanUtils.getBeanMetadata(Class<?> beanClass)      // cached; throws RuntimeException on failure
Method BeanUtils.findWriteMethod(BeanMetadata, String propertyName)
PropertyDescriptor BeanUtils.findPropertyDescriptor(BeanMetadata, String propertyName)

BeanMetadata.of(Class<?>)   // getBeanInfo(), getBeanClass(), getPropertyDescriptors(),
                            // getPropertyDescriptor(name), getPropertyDescriptorsMap()
BeanProperty.of(Object bean, String propertyName)  // getName/getValue/setValue/getBeanClass/getDescriptor
```

> [!NOTE]
> `BeanMetadata` wraps `Introspector.getBeanInfo(beanClass, Object.class)`, so the four `Object` methods are
> excluded and property keys are **uncapitalized** (`getName` → `"name"`). `resolvePropertiesAsMap` is the
> engine behind `ReflectiveConfigurationPropertyGenerator`
> ([Configuration Property Metadata](configuration-metadata.md)).

`io.microsphere.beans.ConfigurationProperty` is the POJO shared by the annotation, the metadata SPI and the
annotation processor. Note `getType()` returns a **`String`** type name, not a `Class`:

```java
ConfigurationProperty(String name)                      // type defaults to String.class
ConfigurationProperty(String name, Class<?> type)
String getName();  String getType();                    // e.g. "int", "java.time.Duration"
Object getValue();  Object getDefaultValue();  boolean isRequired();  String getDescription();
Metadata getMetadata();   // getSources()/getTargets() lazily created Sets; getDeclaredClass()/getDeclaredField()
```

---

## 10. Pitfalls

* **Banning is opt-in** — `initBannedMethods()` is never called for you; reflection caches (256/256/16) are
  evicted only via the three `clear*Cache()` hooks.
* **Real typos, forever API:** `isOverridenObjectMethod` (one `r`); `JavaType.isWildCardType()` vs
  `toWildcardType()`.
* **Declared vs inherited reach:** `findField` walks superclasses but `getDeclaredField` returns `null` for
  inherited fields; `getConstructor` throws when absent but `findConstructor` returns `null`.
* **Java 8 source level:** you cannot catch `InaccessibleObjectException` by type — use
  `ReflectionUtils.isInaccessibleObjectException(...)`.

---

## See also

* [Type Conversion](type-conversion.md) — the largest consumer of `TypeUtils`
* [Language Abstractions](language-abstractions.md) — `Version`, `Deprecation`, `Throwable*` interfaces
* [Configuration Property Metadata](configuration-metadata.md) — `ConfigurationProperty` and its readers

[← Handbook index](../README.md) · [Previous: Language Abstractions](language-abstractions.md) · [Next: Type Conversion →](type-conversion.md)
