# 反射与类型

> 语言版本：[中文](reflection-and-types.md) · [English](../en/reflection-and-types.md)
> [← 手册目录](../README.md)

## 一览

| 项目 | 说明 |
|---|---|
| 模块 | `microsphere-java-core` |
| 包 | `io.microsphere.reflect`、`io.microsphere.reflect.generics`、`io.microsphere.internal.reflect`、`io.microsphere.beans` |
| 风格 | `public abstract class XxxUtils implements Utils` + 私有构造器，方法全部 `static` |
| 泛型解析 | `TypeUtils`（带缓存）与 `JavaType`（`Type` 的对象模型） |
| Bean 内省 | `BeanUtils` / `BeanMetadata` / `BeanProperty`，基于 `java.beans.Introspector` |
| JDK 9+ 访问 | 每个读取/调用方法都有 `forceAccess` 重载；`trySetAccessible` 不抛异常 |

这一组包回答四个问题：*找到成员*（`MethodUtils`、`FieldUtils`、`ConstructorUtils`）、
*解析泛型类型*（`TypeUtils`、`JavaType`）、*引用在当前运行时可能不存在的成员*（`*Definition`）、
*把类当作 Bean 处理*（`io.microsphere.beans`）。

---

## 1. 工具类的统一形态

所有工具类遵守同样两条规则：

```java
public abstract class MethodUtils implements io.microsphere.util.Utils {
    private MethodUtils() {}
    // 只有静态方法和静态常量
}
```

注意它们是**实现 `Utils` 标记接口的 `abstract` 类**，而不是 `final` 类。

| 类 | 一句话职责 |
|---|---|
| `MethodUtils` | 查找/过滤/调用方法（含继承来的），支持方法禁用 |
| `FieldUtils` | 查找/读/写字段，含静态字段与强制访问 |
| `ConstructorUtils` | 查找构造器并实例化 |
| `ReflectionUtils` | 调用方类探测、字段转 Map、`InaccessibleObjectException` 检测 |
| `TypeUtils` | 泛型 `java.lang.reflect.Type` 解析，带缓存 |
| `JavaType` | `Type` 的对象模型（种类、父类型、接口、类型参数） |
| `ClassDefinition` / `MethodDefinition` / `FieldDefinition` / `ConstructorDefinition` | 带版本、可感知的弃用信息的反射句柄 |
| `Modifier` | `java.lang.reflect.Modifier` 标志位的枚举视图 |
| `ProxyUtils`、`MemberUtils`、`ExecutableUtils`、`AccessibleObjectUtils` | 更窄职责的辅助类 |
| `MultipleType` | 类型对/元组，是 `TypeUtils` 的缓存键 |
| `generics.ParameterizedTypeImpl`、`generics.TypeArgument` | 构造 `ParameterizedType` |
| `internal.reflect.ConstantPoolUtils` | 读取 class 文件常量池（JDK 内部实现） |

---

## 2. 查找与调用方法（`MethodUtils`）

### 查找

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

public static Method findMethod(Class targetClass, String methodName)                        // 原始 Class 参数
public static Method findMethod(Class targetClass, String methodName, Class<?>... parameterTypes)
public static Method findDeclaredMethod(Class<?> targetClass, String methodName, Class<?>... parameterTypes)
```

`get*` 与 `find*` 的区别：`get` 返回全部，`find` 在遍历过程中应用你传入的断言。
`getAll*` / `findAll*` 包含继承来的成员，无前缀的版本只看本类声明的。

现成的断言与名称前缀常量：

```java
// List<Method> 常量
public final static List<Method> OBJECT_PUBLIC_METHODS;
public final static List<Method> OBJECT_DECLARED_METHODS;
// Predicate<? super Method> 常量
public final static Predicate<? super Method> OBJECT_METHOD_PREDICATE;
public final static Predicate<? super Method> PUBLIC_METHOD_PREDICATE;
public final static Predicate<? super Method> STATIC_METHOD_PREDICATE;
public final static Predicate<? super Method> NON_STATIC_METHOD_PREDICATE;
public final static Predicate<? super Method> FINAL_METHOD_PREDICATE;
public final static Predicate<? super Method> NON_PRIVATE_METHOD_PREDICATE;
// String 常量
public static final String GET_METHOD_NAME_PREFIX;  // "get"
public static final String SET_METHOD_NAME_PREFIX;  // "set"
public static final String IS_METHOD_NAME_PREFIX;   // "is"

public static Predicate<? super Method> excludedDeclaredClass(Class<?> declaredClass)
```

```java
// Foo 上声明的非 static、非 private 方法
List<Method> accessible = MethodUtils.findDeclaredMethods(Foo.class,
        MethodUtils.NON_STATIC_METHOD_PREDICATE,
        MethodUtils.NON_PRIVATE_METHOD_PREDICATE);

// 整个继承体系中带 @Exported 的所有 public 方法
List<Method> exported = MethodUtils.findAllMethods(Foo.class,
        MethodUtils.PUBLIC_METHOD_PREDICATE,
        method -> method.isAnnotationPresent(Exported.class));

// 只取属性访问器
List<Method> getters = MethodUtils.findAllMethods(Foo.class, MethodUtils::isGetterMethod);
```

### 调用

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

`forceAccess` 版本会调用 `AccessibleObjectUtils.trySetAccessible`，后者返回 `false` 而不抛异常——
这正是 JDK 9+ 上访问非 public 成员又不被强封装击溃的关键。

### 覆写、签名与分类

```java
public static boolean overrides(Method overrider, Method overridden)
public static Method findNearestOverriddenMethod(Method overrider)
public static Method findOverriddenMethod(Method overrider, Class<?> targetClass)
public static Method findFunctionalInterfaceMethod(Class<?> type)

public static String getSignature(Method method)
public static String buildSignature(Class<?> declaringClass, String methodName, Class<?>... parameterTypes)
// 格式："io.microsphere.Foo#bar(java.lang.String,int)"

public static boolean isObjectMethod(Method method)
public static boolean isOverridenObjectMethod(Method method)     // 源码即如此拼写："Overriden"
public static boolean isCallerSensitiveMethod(Method method)
public static boolean isGetterMethod(Method method)
public static boolean isSetterMethod(Method method)
public static boolean isIsMethod(Method method)
public static String  getMethodName(Method method)               // "getFoo" -> "foo"
public static boolean matchesParameterCount(Method method, int parameterCount)
public static boolean matchesReturnType(Method method, Class<?> returnType)
```

---

## 3. 字段与构造器（`FieldUtils`、`ConstructorUtils`）

### `FieldUtils`

```java
// 查找
public static Field findField(Object object, String fieldName)
public static Field findField(Class<?> klass, String fieldName)
public static Field findField(Class<?> klass, String fieldName, Class<?> fieldType)
public static Field findField(Class<?> klass, String fieldName, Predicate<? super Field>... predicates)
public static Field getDeclaredField(Class<?> declaredClass, String fieldName)
public static Set<Field> findAllFields(Class<?> declaredClass, Predicate<? super Field>... fieldFilters)
public static Set<Field> findAllDeclaredFields(Class<?> declaredClass, Predicate<? super Field>... fieldFilters)

// 读实例字段 — getFieldValue(Object instance, String fieldName [, V defaultValue | Class<V> fieldType])
public static <V> V getFieldValue(Object instance, String fieldName)
public static <V> V getFieldValue(boolean forceAccess, Object instance, String fieldName)
public static <V> V getFieldValue(Object instance, String fieldName, V defaultValue)
public static <V> V getFieldValue(boolean forceAccess, Object instance, String fieldName, V defaultValue)
public static <V> V getFieldValue(Object instance, String fieldName, Class<V> fieldType)
public static <V> V getFieldValue(boolean forceAccess, Object instance, String fieldName, Class<V> fieldType)
public static <V> V getFieldValue(Object instance, Field field)
public static <V> V getFieldValue(boolean forceAccess, Object instance, Field field)

// 静态字段
public static <T> T getStaticFieldValue(Class<?> klass, String fieldName)
public static <T> T getStaticFieldValue(boolean forceAccess, Class<?> klass, String fieldName)
public static <T> T getStaticFieldValue(Field field)
public static <T> T getStaticFieldValue(boolean forceAccess, Field field)
public static <V> V setStaticFieldValue(Class<?> klass, String fieldName, V fieldValue)
public static <V> V setStaticFieldValue(boolean forceAccess, Class<?> klass, String fieldName, V fieldValue)

// 写实例字段（返回旧值）
public static <V> V setFieldValue(Object instance, String fieldName, V value)
public static <V> V setFieldValue(boolean forceAccess, Object instance, String fieldName, V value)
public static <V> V setFieldValue(Object instance, Field field, V value)
public static <V> V setFieldValue(boolean forceAccess, Object instance, Field field, V value)

public static void assertFieldMatchType(Object instance, String fieldName, Class<?> expectedType)
```

> [!NOTE]
> 没有 `readFieldValue`——读取方法叫 `getFieldValue`，且每个读/写都有 `forceAccess` 重载用于
> private/protected 成员。`setFieldValue(...)` 返回旧值，因此“替换再恢复”只需两行。

### `ConstructorUtils`

```java
public static final Constructor NOT_FOUND_CONSTRUCTOR;   // null
public static boolean isNonPrivateConstructorWithoutParameters(Constructor<?> constructor)
public static boolean hasNonPrivateConstructorWithoutParameters(Class<?> type)
public static List<Constructor<?>> findConstructors(Class<?> type, Predicate<? super Constructor<?>>... filters)
public static List<Constructor<?>> findDeclaredConstructors(Class<?> type, Predicate<? super Constructor<?>>... filters)
public static <T> Constructor<T> getConstructor(Class<T> type, Class<?>... parameterTypes)
public static <T> Constructor<T> getDeclaredConstructor(Class<T> type, Class<?>... parameterTypes)
public static <T> Constructor<T> findConstructor(Class<T> type, Class<?>... parameterTypes)   // 找不到返回 null
public static <T> T newInstance(Class<T> type, Object... args)
public static <T> T newInstance(boolean forceAccess, Class<T> type, Object... args)
public static <T> T newInstance(Constructor<T> constructor, Object... args)
public static <T> T newInstance(boolean forceAccess, Constructor<T> constructor, Object... args)
```

`getConstructor` / `getDeclaredConstructor` 找不到时抛异常；`findConstructor` 返回 `null`
（可与 `NOT_FOUND_CONSTRUCTOR` 对比）。

---

## 4. `ReflectionUtils` —— 调用方探测与可移植性探针

```java
public static Class<?> getCallerClass() throws IllegalStateException
public static String getCallerClassName()
public static <T> List<T> toList(Object array) throws IllegalArgumentException
public static Map<String, Object> readFieldsAsMap(Object object)
public static boolean isInaccessibleObjectException(Throwable failure)
public static boolean isInaccessibleObjectException(Class<?> throwableClass)

public static final Class<?> SUN_REFLECT_REFLECTION_CLASS;          // 可能为 null
public static final Class<?> STACK_WALKER_CLASS;                     // 可能为 null
public static final Class<?> STACK_WALKER_STACK_FRAME_CLASS;         // 可能为 null
public static final Class<? extends Throwable> INACCESSIBLE_OBJECT_EXCEPTION_CLASS;  // 可能为 null
public static boolean isSupportedSunReflectReflection()
```

* `getCallerClass()` 优先使用 `StackWalker`，缺失时退回 `sun.reflect.Reflection`——这是库在不要求调用方
  传入 class loader 的情况下选出默认 loader 的手段。
* `readFieldsAsMap(object)` 把全部非静态字段快照成 Map（嵌套 POJO 变成嵌套 Map），适合写 `toString`
  和测试断言。
* `isInaccessibleObjectException(...)` 可移植地检测 JDK 16+ 的强封装失败，因为
  `java.lang.reflect.InaccessibleObjectException` 在 Java 8 源码级别下不存在。

---

## 5. 泛型类型解析（`TypeUtils`）

### 解析实际类型参数——最主要用例

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
> 参数类型是 `java.lang.reflect.Type` 而不是 `Class`——传入的 `Type` 本身可以是参数化类型，这正是嵌套
> 泛型能被解析的前提。`resolveActualTypeArgumentClass(...)` 返回某个下标对应的**原始** `Class`；当该参数
> 是无法归约的类型变量或通配符时返回 `null`。

```java
class StringArrayList extends ArrayList<String> {}

Class<?> elementType = TypeUtils.resolveActualTypeArgumentClass(StringArrayList.class, List.class, 0);
// elementType == String.class
```

`Converter<S, T>` 与 `MultiValueConverter<S>` 发现自身类型参数用的正是同一个调用——
见[类型转换](type-conversion.md)。

### 缓存

```java
public static final String RESOLVED_GENERIC_TYPES_CACHE_SIZE_PROPERTY_NAME = "microsphere.reflect.resolved-generic-types.cache.size";
public static final int DEFAULT_RESOLVED_GENERIC_TYPES_CACHE_SIZE = 256;
public static final int RESOLVED_GENERIC_TYPES_CACHE_SIZE;   // 类加载时读取一次
```

解析结果以 `MultipleType(type, baseType)` 为键缓存；如果你解析海量参数化类型，可设置
`-Dmicrosphere.reflect.resolved-generic-types.cache.size=1024`。

### 类型形状判断、窄化与层级遍历

```java
public static boolean isClass(Object type) / isObjectClass(Class<?>) / isObjectType(Object type)
public static boolean isParameterizedType(Object type) / isTypeVariable(Object type)
public static boolean isWildcardType(Object type) / isGenericArrayType(Object type)
public static boolean isActualType(Type type)

@Nullable public static Type     getRawType(Type type)
@Nullable public static Class<?> getRawClass(Type type)
@Nullable public static Class<?> asClass(Type type)
@Nullable public static ParameterizedType asParameterizedType(Type type)
@Nullable public static TypeVariable asTypeVariable(Type type)
@Nullable public static WildcardType asWildcardType(Type type)
@Nullable public static GenericArrayType asGenericArrayType(Type type)
@Nullable public static Type getComponentType(Type type)

public static boolean isAssignableFrom(Type superType, Type targetType)
public static boolean isAssignableFrom(Class<?> superClass, Type targetType)

@Nullable public static String   getClassName(Type type)
@Nullable public static String   getTypeName(Type type)
@Nonnull  public static String[] getTypeNames(Type... types)
@Nonnull  public static Set<String> getClassNames(Iterable<? extends Type> types)
```

在 `Type`（而非 `Class`）维度遍历类型层级：

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

// 可复用断言
NON_OBJECT_TYPE_FILTER, NON_OBJECT_CLASS_FILTER, TYPE_VARIABLE_FILTER,
PARAMETERIZED_TYPE_FILTER, WILDCARD_TYPE_FILTER, GENERIC_ARRAY_TYPE_FILTER
```

### 手工构造 `ParameterizedType`

```java
public class ParameterizedTypeImpl implements ParameterizedType {
    public static ParameterizedTypeImpl of(Class<?> rawType, Type... actualTypeArguments)
    public static ParameterizedTypeImpl of(Class<?> rawType, Type[] actualTypeArguments, Type ownerType)
}

public class TypeArgument {
    public static TypeArgument create(Type type, int index)
    public Type getType()
    public int getIndex()
}
```

```java
ParameterizedType listOfStrings = ParameterizedTypeImpl.of(List.class, String.class);
// TypeUtils.getTypeName(...) 会渲染为 java.util.List<java.lang.String>
```

`ParameterizedTypeImpl` 的构造器是 private——只能走 `of(...)`。

---

## 6. `JavaType` —— `Type` 的对象模型

`public class JavaType implements Serializable`，构造器为 `protected`，通过静态工厂创建：

```java
public static final JavaType[] EMPTY_JAVA_TYPE_ARRAY;
public static final JavaType OBJECT_JAVA_TYPE;
public static final JavaType NULL_JAVA_TYPE;

// 从 Class 或 Type 创建
@Nonnull public static JavaType from(Class<?> targetClass)
@Nonnull public static JavaType from(Type type)

// 从成员创建
@Nullable public static JavaType fromField(Class<?> declaredClass, String fieldName)
@Nonnull  public static JavaType fromField(Field field)
@Nonnull  public static JavaType fromMethodReturnType(Class<?> declaredClass, String methodName, Class<?>... parameterTypes)
@Nonnull  public static JavaType fromMethodReturnType(Method method)
@Nonnull  public static JavaType[] fromMethodParameters(Class<?> declaredClass, String methodName, Class<?>... parameterTypes)
@Nonnull  public static JavaType[] fromMethodParameters(Method method)
@Nonnull  public static JavaType fromMethodParameter(Method method, int parameterIndex)

// 导航
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
@Nullable public JavaType as(Class<?> targetClass)   // 把当前视图“重新锚定”到某个父类型

// 窄化
@Nullable public <T> Class<T> toClass()
@Nullable public ParameterizedType toParameterizedType()
@Nullable public TypeVariable toTypeVariable()
@Nullable public WildcardType toWildcardType()
@Nullable public GenericArrayType toGenericArrayType()

// 判断
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
> 判断方法是 `isWildCardType()`（大写 `C`），而窄化方法是 `toWildcardType()`（小写 `c`）。
> `as(Class)` 用于重新锚定视图：`javaType.as(Map.class)` 把同一个类型按 `Map` 表达，此时
> `getGenericTypes()` 就是 `Map` 的 `K`/`V`。

```java
JavaType type = JavaType.fromField(Order.class, "lines");
type.getKind();                        // PARAMETERIZED_TYPE
type.getRawType();                     // java.util.List
type.getGenericTypes()[0].getType();   // java.lang.String（对应 List<String> lines）
```

需要把泛型当作**带来源的对象**来遍历时用 `JavaType`（`getSource()` / `getRootSource()` 会告诉你每个节点
从哪来）；只要原始 `Type` 够用就直接用 `TypeUtils`。

---

## 7. 版本感知句柄：`ClassDefinition` 一族

`*Definition` 是指向类或成员的不可变句柄，该成员**可能在当前运行时并不存在**，同时携带弃用信息——
专为需要兼容某个版本区间依赖的库设计。

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
    public MethodDefinition(Version since, Deprecation deprecation,
                            String declaredClassName, String methodName, String... parameterClassNames)
    // （另有 String/弃用 与 Version/无弃用 的组合重载）
    @Nonnull public String getMethodName()
    @Nullable public Method getMethod()
    public <R> R invoke(Object instance, Object... args)
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

参数全部以**类名（字符串）**给出，因此构造 definition 时不会因类缺失而失败——解析推迟到
`getMethod()` / `isPresent()`。`Deprecation` 与 `Version` 来自
[语言抽象](language-abstractions.md)。

```java
MethodDefinition optionalEmpty = new MethodDefinition("1.8.0", "java.util.Optional", "empty");
if (optionalEmpty.isPresent()) {
    Object empty = optionalEmpty.invoke(null);   // 静态方法：instance 传 null
}

ConstructorDefinition stringFromInt = new ConstructorDefinition("1.0.0", "java.lang.String", "java.lang.Integer");
String value = stringFromInt.newInstance(123);
```

---

## 8. `Modifier` 与更窄的辅助类

### `Modifier`（枚举）

```java
public enum Modifier {
    PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, SYNCHRONIZED, VOLATILE, TRANSIENT, NATIVE,
    INTERFACE, ABSTRACT, STRICT, BRIDGE, VARARGS, SYNTHETIC, ANNOTATION, ENUM, MANDATED;

    public int getValue()
    public boolean matches(int mod)

    public static boolean isPublic(int modifiers)   // isPrivate/isProtected/isStatic/isFinal/isSynchronized/
    public static boolean matchesAny(int modifiers, Modifier... toMatch)                // ...一直到 isMandated
    public static boolean matchesAll(int modifiers, Modifier... toMatch)
    public static boolean matchesAny(Supplier<Integer> modifiersSupplier, Modifier... toMatch)
    public static boolean matchesAll(Supplier<Integer> modifiersSupplier, Modifier... toMatch)
}
```

最后六个常量（`BRIDGE 0x40`、`VARARGS 0x80`、`SYNTHETIC 0x1000`、`ANNOTATION 0x2000`、`ENUM 0x4000`、
`MANDATED 0x8000`）对应的标志位，`java.lang.reflect.Modifier` 并没有提供同名辅助方法——这就是过滤
生成/合成成员需要这个枚举、而不是 `Modifier.isStatic(...)` 的原因。

### 其余辅助类

```java
// ProxyUtils
public static boolean isProxyable(Class<?> type)

// AccessibleObjectUtils
public static boolean trySetAccessible(AccessibleObject accessibleObject)   // JDK 16+ 返回 false 而非抛异常
public static boolean canAccess(Object target, AccessibleObject accessibleObject)

// MemberUtils —— 另有断言 STATIC_MEMBER_PREDICATE、NON_STATIC_MEMBER_PREDICATE、
// FINAL_MEMBER_PREDICATE、PUBLIC_MEMBER_PREDICATE、NON_PRIVATE_MEMBER_PREDICATE
public static boolean isStatic(Member m) / isAbstract / isNonStatic / isFinal / isPrivate / isPublic / isNonPrivate
public static Member asMember(Object obj)
public static boolean isInvalidDeclaringClass(Class<?> type)

// ExecutableUtils
public static <E extends Executable & Member> void execute(E executable, ThrowableConsumer<E> consumer)
public static <E extends Executable & Member, R> R execute(E executable, ThrowableSupplier<R> supplier)
public static <E extends Executable & Member, R> R execute(E executable, ThrowableFunction<E, R> function)
public static boolean matchParameterTypes(Executable executable, Object... arguments)
```

`ExecutableUtils.execute(...)` 是通往[语言抽象](language-abstractions.md)的桥梁：让反射调用点可以直接使用
`Throwable*` lambda，而无需手写 try/catch。

---

## 9. Bean 内省（`io.microsphere.beans`）

### `BeanUtils` 与 `BeanMetadata`

```java
public static final String BEAN_PROPERTIES_MAX_RESOLVED_DEPTH_PROPERTY_NAME = "microsphere.bean.properties.max-resolved-depth"; // 默认 100
public static final String BEAN_METADATA_CACHE_SIZE_PROPERTY_NAME          = "microsphere.bean.metadata.cache.size";        // 默认 64

@Nonnull public static Map<String, Object> resolvePropertiesAsMap(Object bean)
@Nonnull public static Map<String, Object> resolvePropertiesAsMap(Object bean, int maxResolvedDepth)
@Nonnull public static BeanMetadata getBeanMetadata(Class<?> beanClass) throws RuntimeException
public static Method findWriteMethod(BeanMetadata beanMetadata, String propertyName)
public static PropertyDescriptor findPropertyDescriptor(BeanMetadata beanMetadata, String propertyName)
```

```java
public class BeanMetadata {
    public static BeanMetadata of(Class<?> beanClass) throws RuntimeException
    public BeanInfo getBeanInfo()
    public Class<?> getBeanClass()
    @Nonnull public Collection<PropertyDescriptor> getPropertyDescriptors()
    public PropertyDescriptor getPropertyDescriptor(String propertyName)
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

`BeanMetadata` 包装的是 `Introspector.getBeanInfo(beanClass, Object.class)`，因此排除了 `Object` 的四个
方法，且属性键是**首字母小写**的（`getName` → `"name"`）。`getBeanMetadata` 的结果会缓存
（`microsphere.bean.metadata.cache.size`，默认 64）。`resolvePropertiesAsMap` 会递归处理嵌套 Bean
（深度上限为 `maxResolvedDepth`，默认 100），它是 `ReflectiveConfigurationPropertyGenerator` 的底层引擎
（见[配置属性元数据](configuration-metadata.md)）。

### `ConfigurationProperty`（数据对象）

`io.microsphere.beans.ConfigurationProperty` 是注解、元数据 SPI 与注解处理器共用的数据对象。注意
`getType()` 返回的是 **`String`** 类型名，而不是 `Class`：

```java
public ConfigurationProperty(String name)                       // 类型默认 String.class
public ConfigurationProperty(String name, Class<?> type)

@Nonnull public String getName()
@Nonnull public String getType()                               // 例如 "int"、"java.time.Duration"
@Nullable public Object getValue()
@Nullable public Object getDefaultValue()
public boolean isRequired()
@Nonnull public String getDescription()
@Nonnull public Metadata getMetadata()

public static class Metadata {
    public Set<String> getSources()        // 懒创建，永不为 null
    public Set<String> getTargets()
    public String getDeclaredClass()
    public String getDeclaredField()
}
```

---

## 10. 常见坑

* **禁用方法需要手动开启。** `MethodUtils.initBannedMethods()` 读取 `microsphere.reflect.banned-methods`
  属性——用 `|` 分隔的 `fully.qualified.Class#method(param.Type,other.Type)` 签名列表——并把命中的方法从
  查找结果中剔除。**它不会被自动调用**；如果依赖该属性，请在启动时调用一次。程序化禁用直接用
  `banMethod(Class, String, Class...)`。
* **反射缓存容量固定，只能手动清理。** `methodsCache`（256）、`declaredMethodsCache`（256）、
  `bannedMethodsCache`（16）；驱逐钩子是 `clearMethodsCache()`、`clearDeclaredMethodsCache()`、
  `clearBannedMethodsCache()`。
* **必须接受的拼写：** `isOverridenObjectMethod`（少了一个 `r`）就是真实方法名。
* **`findField` 与 `getDeclaredField`：** `findField` 会向上遍历父类；`getDeclaredField` 不会，
  继承来的字段返回 `null`。
* **Java 8 源码级别：** 无法按类型 catch `InaccessibleObjectException`——请用
  `ReflectionUtils.isInaccessibleObjectException(...)`。
* **`ConstantPoolUtils`**（`io.microsphere.internal.reflect`）通过 `jdk.internal.reflect.ConstantPool`
  （JDK 9+）或 `sun.reflect.ConstantPool`（JDK 8）读取类常量池。现代 JDK 上需要
  `--add-opens java.base/jdk.internal.reflect=ALL-UNNAMED`，且它属于**内部 API**，随时可能变化。
  除非你在做字节码级工具，否则不要依赖它。

---

## 参见

* [类型转换](type-conversion.md) —— `TypeUtils` 最大的消费方
* [语言抽象](language-abstractions.md) —— `Version`、`Deprecation`、`Throwable*` 函数式接口
* [配置属性元数据](configuration-metadata.md) —— `ConfigurationProperty` 及其读取器

[← 手册目录](../README.md) · [上一篇：语言抽象](language-abstractions.md) · [下一篇：类型转换 →](type-conversion.md)
