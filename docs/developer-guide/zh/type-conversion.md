# 类型转换

> 语言版本：[中文](type-conversion.md) · [English](../en/type-conversion.md)
> [← 手册目录](../README.md)

## 一览

| 项目 | 说明 |
|---|---|
| 包 | `io.microsphere.convert`、`io.microsphere.convert.multiple`、`io.microsphere.io.serializer` |
| SPI 文件 | `META-INF/services/io.microsphere.convert.Converter`（34 项）、`...convert.multiple.MultiValueConverter`（11 项）、`...io.serializer.Serializer` / `Deserializer`（各 10 项） |
| 公开入口 | `Converter`、`MultiValueConverter` 接口上的静态方法；实例化的 `Serializers` / `Deserializers` |
| 排序规则 | 所有转换器 `extends Prioritized`，更具体的自动胜出 |
| 类型发现 | `S`/`T` 由 `TypeUtils.resolveActualTypeArgumentClass` 从 `implements` 子句解析 |

三个相关的 SPI：标量转换（`Converter`）、集合/数组转换（`MultiValueConverter`）、二进制序列化
（`Serializer` / `Deserializer`）。它们都通过 [`ServiceLoaderUtils`](core-utilities.md) 加载，并按
`Prioritized.COMPARATOR` 排序。

---

## 1. `Converter<S, T>` —— 标量 SPI

`io.microsphere.convert.Converter`：

```java
@FunctionalInterface
public interface Converter<S, T> extends Prioritized {

    @Nullable T convert(@Nullable S source);                       // lambda 目标方法

    default boolean accept(Class<?> sourceType, Class<?> targetType)
    default Class<S> getSourceType()
    default Class<T> getTargetType()

    static <S, T> Converter<S, T> getConverter(Class<S> sourceType, Class<T> targetType)
    static <T> T convertIfPossible(Object source, Class<T> targetType)
}
```

> [!IMPORTANT]
> `Converter` 是一个 **SPI**：实现类注册在 `META-INF/services/io.microsphere.convert.Converter` 中，由
> Java `ServiceLoader` 加载。引擎类 `Converters` 是**包级私有**的——永远不要引用它。公开入口是接口上的
> 两个 `static` 方法（Java 8 接口静态方法）：`Converter.getConverter(...)` 与
> `Converter.convertIfPossible(...)`。

```java
Integer fortyTwo = Converter.convertIfPossible("42", Integer.class);       // 42
String text      = Converter.convertIfPossible(42, String.class);          // "42"
Duration five    = Converter.convertIfPossible("PT5S", Duration.class);    // PT5S

Converter<String, Integer> converter = Converter.getConverter(String.class, Integer.class);
Integer value = converter.convert("7");
```

找不到匹配转换器时 `convertIfPossible` 返回 **`null`**（但 source 为 `null` 时会 NPE——它会调用
`source.getClass()`）；`getConverter` 返回优先级最高的匹配。默认的 `accept` 用 `isAssignableFrom` 对比
`getSourceType()` / `getTargetType()`。

### 内置转换器

| 源类型 | 目标类型 |
|---|---|
| `String` | `Boolean`、`Byte`、`Character`、`char[]`、`Short`、`Integer`、`Long`、`Float`、`Double`、`String`、`Class`、`Duration`、`InputStream` |
| `Number` | `Byte`、`Short`、`Integer`、`Long`、`Float`、`Double`、`Character` |
| `Object` | `String`、`Boolean`、`Byte`、`Character`、`Short`、`Integer`、`Long`、`Float`、`Double`、`byte[]`、`Optional` |
| `byte[]` | `Object` |
| `Map` | `Properties` |
| `Properties` | `String` |

`StringToClassConverter`、`StringToDurationConverter`、`StringToInputStreamConverter` 常让人惊讶——它们
已经内置了。

> [!NOTE]
> 自带的 services 文件里 `ObjectToOptionalConverter` **出现了两次**。Java SPI 在加载时按类名去重，所以
> 无害，但不要模仿这种写法。

---

## 2. 查找、优先级与缓存

干活的是包级私有的 `io.microsphere.convert.Converters`：

1. 类加载时，所有已注册实现被加载、**按 `Prioritized` 排序**，再按各自的
   `getSourceType()` / `getTargetType()` 归组进以 `Map.Entry<源类型, 目标类型>` 为键的 `ConcurrentMap`。
2. `findConverter(sourceType, targetType)` 随后对该 Map 做 `computeIfAbsent`，用
   `converter.accept(sourceType, targetType)` 过滤，返回第一个（优先级最高）匹配。

冲突转换器之间的先后由 `AbstractConverter.resolvePriority()` 决定：其值为
`-((getAllClasses(sourceType).size() << 16) | getAllClasses(targetType).size())`，是一个**负数**（高于
常规优先级），且类型越派生、绝对值越小。因此 `String → Integer` 的转换器自动胜过泛化的
`Object → Integer`。

> [!WARNING]
> **没有任何缓存失效 API**。不存在 `Converters.invalidate()`，按类型对的缓存没有容量上限也没有 LRU，
> 并且 SPI 列表是显式以 `cached = true` 加载的——`microsphere.service-loader.cached` 对它不起作用。
> 在 `Converters` 初始化之后才加入 classpath 的转换器（热部署容器、OSGi refresh）在 class loader 被替换
> 之前不会被感知。请重启 class loader，不要试图调用不存在的方法。

---

## 3. 编写自定义转换器

### 3.1 实现

稍复杂的转换器优先继承 `AbstractConverter<S, T>`：

```java
public abstract class AbstractConverter<S, T> implements Converter<S, T> {

    protected final Logger logger;                       // io.microsphere.logging

    public AbstractConverter()                           // 通过 resolvePriority() 计算优先级

    @Nullable public final T convert(@Nullable S source) // final：判空 + try/catch + 包装为非受检异常
    @Nullable protected abstract T doConvert(@Nonnull S source) throws Throwable;

    protected Integer resolvePriority()
    @Override public int getPriority()
}
```

`convert` 是 `final`：`null` 源直接返回 `null`；执行 `doConvert` 时若抛出任何 `Throwable`，会先记一条
warning 日志，再包装成 `RuntimeException` 重抛。子类只需实现 **`doConvert`**，其中可以随意抛异常。

```java
public class StringToCurrencyConverter extends AbstractConverter<String, Currency> {

    @Override
    protected Currency doConvert(String source) {
        return Currency.getInstance(source.trim());     // Throwable 由基类记日志并包装
    }
}
```

一行的小转换器直接实现 `Converter` 也可以——它是 `@FunctionalInterface`——但判空和优先级要自己负责：

```java
public class UUIDToStringConverter implements Converter<UUID, String> {

    @Override
    public String convert(UUID source) {
        return source == null ? null : source.toString();
    }

    @Override
    public int getPriority() {
        return Prioritized.MAX_PRIORITY + 1;      // 压过 ObjectToStringConverter
    }
}
```

> [!IMPORTANT]
> `S` 和 `T` 必须在 **`implements` 子句中静态可见**，因为默认的 `getSourceType()` / `getTargetType()`
> 靠 `TypeUtils.resolveActualTypeArgumentClass` 解析它们。裸的 `implements Converter` 或赋给 `Converter`
> 变量的 lambda 无法按类型对被发现——解析出的类型会是 `null`。

可以覆写的扩展点：

| 方法 | 何时覆写 |
|---|---|
| `convert(S)` | 直接实现 `Converter`（它是函数式方法） |
| `doConvert(S)` | 继承 `AbstractConverter`（推荐） |
| `accept(Class, Class)` | 匹配范围比精确类型对更宽（例如“任意 `CharSequence` 源”） |
| `getPriority()` | 需要相对某个内置转换器抢先或让位 |
| `getSourceType()` / `getTargetType()` | 类无法在 `implements` 子句里表达类型 |

`StringConverter<T>`（`@FunctionalInterface`，`extends Converter<String, T>`）是面向字符串源转换器的
收窄契约。

### 3.2 通过 SPI 注册

`src/main/resources/META-INF/services/io.microsphere.convert.Converter`：

```
com.example.convert.StringToCurrencyConverter
```

### 3.3 使用

```java
String s = Converter.convertIfPossible(UUID.randomUUID(), String.class);
// 需要拿到实例本身时（例如查看优先级）
Converter<UUID, String> c = Converter.getConverter(UUID.class, String.class);
```

---

## 4. `MultiValueConverter<S>` —— 分隔字符串转集合

`io.microsphere.convert.multiple.MultiValueConverter`：

```java
public interface MultiValueConverter<S> extends Prioritized {

    boolean accept(Class<S> sourceType, Class<?> multiValueType);
    Object convert(S source, Class<?> multiValueType, Class<?> elementType);

    default Class<S> getSourceType()

    static MultiValueConverter<?> find(Class<?> sourceType, Class<?> targetType)
    static <T> T convertIfPossible(Object source, Class<?> multiValueType, Class<?> elementType)
}
```

已注册实现（11 个）：`StringToArrayConverter`、`StringToCollectionConverter`、`StringToDequeConverter`、
`StringToBlockingDequeConverter`、`StringToBlockingQueueConverter`、`StringToQueueConverter`、
`StringToTransferQueueConverter`、`StringToListConverter`、`StringToSetConverter`、
`StringToNavigableSetConverter`、`StringToSortedSetConverter`。

> [!NOTE]
> `StringToIterableConverter` 和基类 `StringToMultiValueConverter` 存在于源码树中但**未注册**，因此以
> `Iterable` 为目标时会落到已注册的 list/set 行为上。

```java
String[] array = MultiValueConverter.convertIfPossible("a,b,c", String[].class, String.class);  // String[]
List<String> list = MultiValueConverter.convertIfPossible("a,b,c", List.class, String.class);   // [a, b, c]
Set<Integer> ids = MultiValueConverter.convertIfPossible("1,2,3", Set.class, Integer.class);    // [1, 2, 3]

MultiValueConverter<?> converter = MultiValueConverter.find(String.class, SortedSet.class);
```

元素值由标量 `Converter` SPI 转换，所以上面的 `Set<Integer>` 能工作是因为注册了
`StringToIntegerConverter`。`find` 按 `Prioritized` 排序候选并返回第一个 `accept` 的——与 `Converter`
同一套优先级规则。

> [!TIP]
> 与 `Converters` 不同，`MultiValueConverter.find` **没有结果缓存**：每次调用都会加载并排序服务列表
> （受 `microsphere.service-loader.cached` 影响，其默认值为 `false`）。在热点循环里，请用
> `find(...)` 解析一次转换器并复用。

这一层负责把单个配置字符串变成带类型的集合——见[配置属性元数据](configuration-metadata.md)。

---

## 5. `Serializer` / `Deserializer` —— 二进制 SPI

`io.microsphere.io.serializer`：

```java
public interface Serializer<S> {
    byte[] serialize(S source) throws IOException;
}

public interface Deserializer<T> {
    T deserialize(byte[] bytes) throws IOException;
}
```

查找是**基于实例**的，不是静态的，而且必须显式加载 SPI：

```java
public class Serializers {
    public Serializers()
    public Serializers(ClassLoader classLoader)
    public void loadSPI()                                                 // 不调用就一直为空
    @Nonnull public <S> List<Serializer<S>> get(Class<S> serializedType)
    public <S> Serializer<S> getHighestPriority(Class<S> serializedType)
    public <S> Serializer<S> getLowestPriority(Class<S> serializedType)
    public Serializer<?> getMostCompatible(Class<?> serializedType)
}
```

`Deserializers` 以 `Deserializer` 类型镜像了同一组方法。

```java
Serializers serializers = new Serializers();
serializers.loadSPI();                       // 必须调用

byte[] bytes = serializers.getHighestPriority(String.class).serialize("microsphere");

Deserializers deserializers = new Deserializers();
deserializers.loadSPI();
String back = deserializers.getHighestPriority(String.class).deserialize(bytes);
```

已注册实现：

| Services 文件 | 实现类 |
|---|---|
| `META-INF/services/io.microsphere.io.serializer.Serializer` | `BooleanSerializer`、`ByteSerializer`、`CharacterSerializer`、`ShortSerializer`、`IntegerSerializer`、`LongSerializer`、`FloatSerializer`、`DoubleSerializer`、`StringSerializer`、`DefaultSerializer` |
| `META-INF/services/io.microsphere.io.serializer.Deserializer` | 八个数值序列化器（它们同时实现两个接口），外加 `StringDeserializer`、`DefaultDeserializer` |

语义说明：`getHighestPriority(X)` = 第一个匹配（`getPriority()` 最小）；`getLowestPriority(X)` = 最后一个；
`getMostCompatible(X)` = 优先级最高的匹配，找不到时回退到 `getLowestPriority(Object.class)`——即它总能
返回东西。`DefaultSerializer` / `DefaultDeserializer`（Java 序列化）是安全网，不是错误。

> [!WARNING]
> 两个必须知道的约束：
> 1. `EnumSerializer` 存在于源码树中但**未注册**，因此枚举序列化会落到 `DefaultSerializer` 上。需要时请
>    在你自己的 services 文件里注册它。
> 2. 映射表是**每实例一份**（`Map<Class<?>, List<...>>`，以每个实现的第一个类型参数为键），并且
>    `loadSPI()` 是**追加**语义——同一实例调用两次会插入重复条目。向 classpath 添加服务后，正确做法是
> new 一个 `Serializers`；与 `Converters` 不同，这份缓存不是 JVM 全局的。

---

## 6. 三者如何取舍

| 需求 | 使用 |
|---|---|
| 一个值进、一个值出 | `Converter<S, T>` |
| 一个分隔字符串进、集合/数组出 | `MultiValueConverter<S>` |
| 值与字节互转 | `Serializer` / `Deserializer` |
| 从 JSON 整体绑定到 Bean | `JSONUtils.readValue(...)` —— 见 [JSON](json.md) |

---

## 参见

* [反射与类型](reflection-and-types.md) —— `S`/`T` 如何被发现（`TypeUtils`）
* [语言抽象](language-abstractions.md) —— `Prioritized` 常量与排序陷阱
* [核心工具](core-utilities.md) —— `ServiceLoaderUtils` 与 `microsphere.service-loader.cached`

[← 手册目录](../README.md) · [上一篇：反射与类型](reflection-and-types.md) · [下一篇：事件分发 →](events.md)
