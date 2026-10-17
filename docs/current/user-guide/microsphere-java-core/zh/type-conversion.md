# 类型转换

| | |
|---|---|
| **构件** | `io.github.microsphere-projects:microsphere-java-core`（`0.3.20-SNAPSHOT`，`release` 分支） |
| **包** | `io.microsphere.convert` — 38 个主源文件类型 · `io.microsphere.convert.multiple` — 14 个主源文件类型（共 52） |
| **JDK 级别** | 以 Java 8 编译；CI 在 JDK 8 / 11 / 17 / 21 / 25 上测试 |
| **依赖的其他 `core` 包** | `io.microsphere.reflect`（`TypeUtils.resolveActualTypeArgumentClass`）、`io.microsphere.lang.Prioritized`、`io.microsphere.util`（`ClassUtils`、`ClassLoaderUtils`、`ServiceLoaderUtils`、`ExceptionUtils`、`StringUtils`、`ArrayUtils`、`CharSequenceUtils`）、`io.microsphere.collection`（`ListUtils`、`MapUtils`）、`io.microsphere.constants.SymbolConstants`、`io.microsphere.logging`、`io.microsphere.annotation`（`@Nonnull` / `@Nullable`）、`io.microsphere.io.serializer`（byte[] ⇄ Object）、`io.microsphere.io` + `io.microsphere.nio.charset`（String→`InputStream`） |
| **被谁使用** | `metadata`（配置属性强制转换）以及任何需要把配置字符串转成类型化值的应用 |

本页所有 `file:line` 引用都相对于 `microsphere-java-core/src/main/java/io/microsphere/`
（主源码）或 `microsphere-java-core/src/test/java/io/microsphere/`（测试），来自 `release` 工作树。
首次完整出现后，缩写为 `convert/Foo.java:40-55` / `convert/multiple/Foo.java:40-55`。

---

## 1. 这个包组是做什么用的

**当 `io.microsphere.convert` 手上有值的运行时类型并非所需类型时就该用它——最常见的情况是一个来自配置、请求参数或
环境变量的 `String`，而你想要背后的 `Integer`、`Boolean`、`Duration`、`char`、`Class` 或 `InputStream`。** 该包提供
一个精简的 `Converter<S, T>` SPI（`convert/Converter.java:71-110`），带 34 个自动注册的具体实现，一个按
`(sourceType, targetType)` 对缓存所选策略的注册表（`convert/Converters.java:46-69`），以及由
`io.microsphere.lang.Prioritized` 驱动的“排序后首个匹配”查找。两个入口覆盖了几乎全部调用点：
`Converter.getConverter(sourceType, targetType)`（`convert/Converter.java:135-137`）和空值容错的
`Converter.convertIfPossible(source, targetType)`（`convert/Converter.java:159-168`）。

**当*结果*是一个容器时应使用 `io.microsphere.convert.multiple`**——数组或 `Collection` / `List` / `Set` / `SortedSet` /
`NavigableSet` / `Queue` / `Deque` / `BlockingQueue` / `BlockingDeque` / `TransferQueue`——而其*元素*仍然来自单个以逗号
分隔的 `String`。`MultiValueConverter<S>`（`convert/multiple/MultiValueConverter.java:36-91`）按逗号切分源值，然后对
每个元素递归调用单值 `Converter` SPI（`convert/multiple/StringToIterableConverter.java:65-67`），因此
`StringToSetConverter` 通过把每个元素委派给 `StringToIntegerConverter`，把 `"1,2,3"` 变成 `HashSet<Integer>`。

贯穿两个包的设计原则是**全量、不抛异常的降级**：缺失输入与缺失转换器都产生 `null`，绝不抛异常
（`convert/AbstractConverter.java:102-104`、`convert/Converters.java:68`、`convert/multiple/MultiValueConverter.java:89`）。
只有当确实找到了转换器但源值本身无法解析时，异常才会浮现——见 §3.3 的行为表。

> [!NOTE]
> 本子系统**没有 `String`→`Date` 转换器**。测试套件证明了这一点：`convertIfPossible("1", Date.class)` 断言为 `null`
> （`convert/ConverterTest.java:50`）。唯一的时间转换器是 `StringToDurationConverter`
> （`convert/StringToDurationConverter.java:37-38`），它通过 `java.time.Duration.parse` 解析 ISO-8601 时长。

---

## 2. 坐标与版本支持

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- 最新已发布标签；0.3.20-SNAPSHOT 是同一行的工作版本 -->
</dependency>
```

| 事实 | 值 | 证据（`release`） |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | 根 `pom.xml:12` |
| `artifactId` | `microsphere-java-core` | 模块 `pom.xml` |
| 版本属性 | `${revision}` | 根 `pom.xml:14` |
| `release` 上 `${revision}` 的值 | `0.3.20-SNAPSHOT` | 根 `pom.xml:54` |
| 构建父 POM | `microsphere-build:0.3.16` | 根 `pom.xml:6-10` |
| Java 级别 | 8（整个反应堆内没有任何 `maven.compiler.*` / `<release>` 属性 → 级别继承自外部父 POM） | CI 矩阵 `java: [ '8', '11', '17', '21', '25' ]`，`.github/workflows/maven-build.yml:25`，`distribution: 'temurin'` `:33`，构建命令 `:38-45` |
| Spring Framework（对本构件运行时无影响，仅为反应堆一致性列出） | 默认 `7.0.9` | `microsphere-java-parent/pom.xml:23` |
| profile `java8-16`（`<jdk>[1.8,17)</jdk>`）下的 Spring Framework | `5.3.39` | `microsphere-java-parent/pom.xml:54-62`，激活条件 `:57` |
| 导入的 BOM | `microsphere-all-bom:0.3.13` + `spring-framework-bom` | `microsphere-java-parent/pom.xml:22`、`:33-36`、`:40-46` |

> [!TIP]
> `microsphere-java-core` **不携带 Spring 依赖**，因此本页没有任何内容会在 Spring 5 与 Spring 7 profile 之间改变。
> `java8-16` profile 是构建期属性开关，只有当使用 Spring 的模块加入同一反应堆时才重要。

---

## 3. API 参考

### 3.1 应用真正调用的入口点

SPI 契约（`convert/Converter.java:81-110`）。`convert` 是唯一的抽象方法；其余都是读取类自身泛型签名的 `default` 方法。

| 签名 | 可空性 / 行为 | 证据 |
|---|---|---|
| `boolean accept(Class<?> sourceType, Class<?> targetType)` | `default`：当转换器自身的 `getSourceType()`/`getTargetType()` 可从所请求的源/目标 assignable 时为 true | `convert/Converter.java:81-83` |
| `@Nullable T convert(@Nullable S source)` | 单值转换；`@FunctionalInterface` 方法 | `convert/Converter.java:91-92` |
| `default Class<S> getSourceType()` / `default Class<T> getTargetType()` | 通过 `reflect` 从泛型类型参数解析 | `convert/Converter.java:99-110` |
| `static <S,T> Converter<S,T> getConverter(Class<S>, Class<T>)` | 返回优先级最高的匹配转换器，**或 `null`** | `convert/Converter.java:135-137` → `convert/Converters.java:62-69` |
| `static <T> T convertIfPossible(@Nullable Object source, Class<T> targetType)` | `null` 源 → `null`；无转换器 → `null`；否则 `converter.convert(source)` | `convert/Converter.java:159-168` |

多值契约（`convert/multiple/MultiValueConverter.java:45-90`）：

| 签名 | 可空性 / 行为 | 证据 |
|---|---|---|
| `boolean accept(Class<S> sourceType, Class<?> multiValueType)` | 逐目标判定谓词 | `convert/multiple/MultiValueConverter.java:45` |
| `Object convert(S source, Class<?> multiValueType, Class<?> elementType)` | 构建容器；`null` 源 → `null` | `convert/multiple/MultiValueConverter.java:55`、`:35-37` |
| `static MultiValueConverter<?> find(Class<?> sourceType, Class<?> targetType)` | 排序 + 过滤 + `findFirst`，**或 `null`** | `convert/multiple/MultiValueConverter.java:73-81` |
| `static <T> T convertIfPossible(Object source, Class<?> multiValueType, Class<?> elementType)` | 无匹配多值转换器时返回 `null` | `convert/multiple/MultiValueConverter.java:83-90` |

具体转换器都暴露一个公共的 `INSTANCE` 单例，可直接调用而绕过注册表，例如
`StringToIntegerConverter.INSTANCE.convert("42")`（`convert/StringToIntegerConverter.java:32-37`）。完整注册集合见
`src/main/resources/META-INF/services/io.microsphere.convert.Converter`（参见开发者指南 §6）。

### 3.2 转换器支持矩阵（源类型 → 目标类型）

每一行都是一个已注册的非抽象转换器。多值目标必须调用 `MultiValueConverter` 入口点（§3.1）；单值 `Converter` SPI
不产生多值结果。

| 源类型 | 支持的目标类型（单值） | 支撑转换器 |
|---|---|---|
| `String` | `Boolean`、`Byte`、`Character`、`char[]`、`Short`、`Integer`、`Long`、`Float`、`Double`、`String`、`Duration`、`Class`、`InputStream` | `convert/StringTo*Converter.java`、`convert/StringToCharArrayConverter.java:31-34`、`convert/StringToDurationConverter.java:37-38`、`convert/StringToClassConverter.java:38-41`、`convert/StringToInputStreamConverter.java:55-59` |
| `String`（多值） | 任意数组（`T[]`）、`Collection`、`List`、`Set`、`SortedSet`、`NavigableSet`、`Queue`、`Deque`、`BlockingQueue`、`BlockingDeque`、`TransferQueue` | `convert/multiple/StringToArrayConverter.java`、`.../StringToCollectionConverter.java`、`.../StringToListConverter.java`、`.../StringToSetConverter.java`、`.../StringToSortedSetConverter.java`、`.../StringToNavigableSetConverter.java`、`.../StringToQueueConverter.java`、`.../StringToDequeConverter.java`、`.../StringToBlockingQueueConverter.java`、`.../StringToBlockingDequeConverter.java`、`.../StringToTransferQueueConverter.java` |
| `Number` | `Byte`、`Character`、`Short`、`Integer`、`Long`、`Float`、`Double` | `convert/NumberTo*Converter.java` |
| `Object` | `Boolean`、`byte[]`、`Byte`、`Character`、`Short`、`Integer`、`Long`、`Float`、`Double`、`Optional`、`String` | `convert/ObjectTo*Converter.java` |
| `byte[]` | `Object`（一个 `Serializable`） | `convert/ByteArrayToObjectConverter.java:38-45` |
| `Map` | `Properties` | `convert/MapToPropertiesConverter.java:32-37` |
| `Properties` | `String` | `convert/PropertiesToStringConverter.java:35-40` |

矩阵说明：
- `Object→T` 目标通过**递归**解析：例如 `ObjectToIntegerConverter` 当值本身已是 `Integer` 时原样返回，否则委派给
  `Converter.convertIfPossible(source, Integer.class)`（`convert/ObjectToIntegerConverter.java:38-44`；
  `Boolean`/`Byte`/`Character`/`Double`/`Float`/`Long`/`Short` 形状相同）。
- `Object→String` *不是*递归的——它是 `source.toString()`（`convert/ObjectToStringConverter.java:35-37`）。
- `Object→Optional` 总是成功且从不包装 `null`（`Optional.ofNullable`，`convert/ObjectToOptionalConverter.java:35-43`，
  优先级 `MIN_PRIORITY`）。

### 3.3 有损 / 舍入 / 溢出行为

| 转换 | 输入边界情况 | 结果 | 证据 |
|---|---|---|---|
| `String→Integer/Long/Short/Byte/Float/Double` | 格式错误**或**超出范围 | `NumberFormatException`（非受检，原样传播） | `convert/StringToIntegerConverter.java:35-37` 处的 `valueOf`；传播由 `convert/StringToIntegerConverterTest.java:47-50` 证明，且按设计重抛 `convert/AbstractConverter.java:107-111` + `util/ExceptionUtils.java:98-104` |
| `Number→Integer/Long/Short/Byte` | 带小数的 `Number` | **向零截断**，不舍入 | `source.intValue()` `convert/NumberToIntegerConverter.java:36-41`；由 `convert/NumberToIntegerConverterTest.java:50-53` 证明（`Double 1.0` → `Integer 1`） |
| `Number→Byte/Short` | 超出目标范围的值 | **静默窄化并回绕**（不抛异常） | `source.byteValue()` / `shortValue()` `convert/NumberToByteConverter.java:36-41`、`convert/NumberToShortConverter.java:40` |
| `Number→Float/Double` | 大整数 | 精度损失（加宽，`floatValue`/`doubleValue`） | `convert/NumberToDoubleConverter.java:36-41`、`convert/NumberToFloatConverter.java:40` |
| `String→Boolean` | `"true"`/`"True"`/`"TRUE"` | `true`；任何其他非空文本 → `false`；空或空串 → **`null`** | `isNotEmpty(source) ? valueOf(source) : null` `convert/StringToBooleanConverter.java:35-38`；由 `convert/StringToBooleanConverterTest.java:49-55` 证明 |
| `String→Character` | 0 个字符 | `null` | `convert/StringToCharacterConverter.java:37-39`；由 `convert/StringToCharacterConverterTest.java:48-51` 证明 |
| `String→Character` | 多于 1 个字符 | `IllegalArgumentException` | `convert/StringToCharacterConverter.java:40-42`；由 `convert/StringToCharacterConverterTest.java:53-56` 证明 |
| `String→Duration` | 非 ISO-8601 文本 | `DateTimeParseException`（严格，来自 `Duration.parse`） | `convert/StringToDurationConverter.java:37-38`；正例 `"PT12.345S"` `convert/StringToDurationConverterTest.java:22-30` |
| `String→Class` | 未知类名 | `null`（resolve 返回 null，不抛异常） | `resolveClass` `convert/StringToClassConverter.java:38-41` |
| 任何 `AbstractConverter` | `null` 源 | `null`（从不进入 `doConvert`） | `convert/AbstractConverter.java:101-104` |
| `Map→Properties` | 含 `null` 值的 map | `NullPointerException`（`Properties` extends `Hashtable`，拒绝 `null` 值；`AbstractConverter.convert` 的包装会原样重抛 NPE） | `convert/MapToPropertiesConverter.java:32-37`；由 `convert/MapToPropertiesConverterTest.java:53-57` 证明 |
| `Object→Optional` | `null` 源 | **`Optional.empty()`**（这是唯一一个在 `null` 输入下*不*返回 `null` 的已注册转换器，因为它直接 implements `Converter` 而非 extends `AbstractConverter`） | `convert/ObjectToOptionalConverter.java:28-43`；由 `convert/ObjectToOptionalConverterTest.java:48-52` 证明 |

### 3.4 调用模式

调用该子系统有四种真实方式；按你的失败策略择一使用。

**直接单例**——完全绕过注册表，当类型对在编译期已知且你希望零查找时使用：

```java
Integer n = StringToIntegerConverter.INSTANCE.convert("42"); // convert/StringToIntegerConverter.java:32-37
```

**两步（先 `getConverter` 后 `convert`）**——取一次策略并复用；无匹配转换器时返回 `null`，因此这里是插入自定义默认值的地方：

```java
Converter<String, Integer> c = Converter.getConverter(String.class, Integer.class); // convert/Converter.java:135
Integer n = (c == null) ? fallback : c.convert(text);                               // 仅当 SPI 无匹配时返回 null
```

**一步式（`convertIfPossible`）**——空值容错的便捷方法；`null` 源或不支持的类型对 → `null`，缺失转换器不抛异常
（但*找到的*转换器若其 `doConvert` 拒绝输入仍可能抛异常）：

```java
Integer n = Converter.convertIfPossible("123", Integer.class);   // convert/Converter.java:159-168
Duration d = Converter.convertIfPossible("PT2S", Duration.class); // convert/StringToDurationConverter.java:37-38
Date   dt = Converter.convertIfPossible("1", Date.class);         // null —— 未注册 String→Date 转换器（convert/ConverterTest.java:50）
```

**多值**——把分隔字符串拆成某元素类型的容器；容器类由所选转换器固定，而非由所请求的接口决定：

```java
Set<Integer> s = MultiValueConverter.convertIfPossible("1,2,3", Set.class, Integer.class); // HashSet {1,2,3}；convert/multiple/MultiValueConverter.java:83-90
Integer[]  a = (Integer[]) MultiValueConverter.find(String.class, Integer[].class)
        .convert("1,2,3", Integer[].class, null);                                          // [1,2,3]；convert/multiple/StringToArrayConverter.java:36-49
```

**null 与抛异常的一句话判定：** 找不到转换器时得到 `null`；找到转换器且其 `doConvert` 抛异常时，你得到原始非受检异常
（`NumberFormatException`、`IllegalArgumentException`、`NullPointerException`）或一个新的 `RuntimeException`（针对受检异常，
如反序列化路径上的 `IOException`）——见 `convert/AbstractConverter.java:106-112` 与 `util/ExceptionUtils.java:98-104`。

---

## 4. 来自测试的示例

下面每段代码都从 `release` 分支的真实测试主体精简而来；引用给出了文件与行号。

**注册表查找为某一类型对返回最具体（最匹配）的转换器**——遍历每个已注册转换器，确认 `getConverter` 交回同一类的实例：

> `convert/ConverterTest.java:38-43`
```java
loadServicesList(Converter.class, Converter.class.getClassLoader()).stream().sorted().forEach(converter -> {
    assertSame(converter.getClass(), getConverter(converter.getSourceType(), converter.getTargetType()).getClass());
});
```

**`convertIfPossible`——含“不支持 ⇒ null”规则**（注意 `Date` 例子）：

> `convert/ConverterTest.java:45-51`
```java
assertEquals(Integer.valueOf(2), convertIfPossible("2", Integer.class));
assertEquals(FALSE, convertIfPossible("false", Boolean.class));
assertEquals(Double.valueOf(1), convertIfPossible("1", Double.class));
assertNull(convertIfPossible("1", Date.class)); // 没有注册 String->Date 转换器
```

**String→Boolean 宽松性**——`"True"` 为 true，任意词为 false，空串和 `null` 为 `null`：

> `convert/StringToBooleanConverterTest.java:49-55`
```java
assertTrue(converter.convert("True"));
assertFalse(converter.convert("a"));
assertNull(converter.convert(""));
assertNull(converter.convert(null));
```

**String→Integer 失败模式**——无法解析的字符串抛 `NumberFormatException`（不会被吞成 `null`）：

> `convert/StringToIntegerConverterTest.java:47-50`
```java
assertThrows(NumberFormatException.class, () -> converter.convert("a"));
```

**Number→Integer 截断**——带小数的 `Number` 被向零截断：

> `convert/NumberToIntegerConverterTest.java:50-53`
```java
assertEquals(Integer.valueOf(1), this.converter.convert(Double.valueOf(1.0)));
```

**多值分发为所请求的目标类型挑选最具体的容器**：

> `convert/multiple/MultiValueConverterTest.java:44-75`
```java
assertEquals(StringToArrayConverter.class,     MultiValueConverter.find(String.class, String[].class).getClass());
assertEquals(StringToCollectionConverter.class, MultiValueConverter.find(String.class, Collection.class).getClass());
assertEquals(StringToSetConverter.class,        MultiValueConverter.find(String.class, Set.class).getClass());
assertEquals(StringToDequeConverter.class,      MultiValueConverter.find(String.class, Deque.class).getClass());
```

**String→array**（按逗号切分，然后逐元素 `String→Integer`；单个 token 产生单元素数组，空串产生空数组，`null`
产生 `null`）：

> `convert/multiple/StringToArrayConverterTest.java:53-59`
```java
assertArrayEquals(new Integer[]{123},    (Integer[]) converter.convert("123",   Integer[].class, Integer.class));
assertArrayEquals(new Integer[]{1,2,3},  (Integer[]) converter.convert("1,2,3", Integer[].class, null));
assertArrayEquals(EMPTY_INTEGER_OBJECT_ARRAY, (Integer[]) converter.convert("", Integer[].class, null));
assertNull(converter.convert(null, Integer[].class, null));
```

**String→Collection** 构建类 `List` 的 `Collection`；**String→Set** 无论所传的 `multiValueType` 参数如何都构建
`HashSet`（具体容器由转换器固定，而非由所请求的接口决定）：

> `convert/multiple/StringToCollectionConverterTest.java:88-107`
```java
Collection result = (Collection<Long>) converter.convert("1,2,3", Collection.class, Long.class);
assertEquals(ofList(1L, 2L, 3L), result);
assertEquals(emptyList(), converter.convert("", Collection.class, Integer.class));
assertNull(converter.convert(null, Collection.class, Integer.class));
```
> `convert/multiple/StringToSetConverterTest.java:89-106`
```java
Set result = (Set<Double>) converter.convert("1.0,2.0,3.0", Queue.class, Double.class); // 仍是 HashSet
assertTrue(CollectionUtils.equals(new HashSet(ofList(1.0, 2.0, 3.0)), result));
assertNull(converter.convert("", Collection.class, null)); // null 元素类型 -> 无元素转换器 -> null
```

**把 `Number` 加宽为 `Double`**——`Integer` 源变成等值的 `Double`（加宽时无截断）：

> `convert/NumberToDoubleConverterTest.java:50-53`
```java
assertEquals(Double.valueOf(1.0), this.converter.convert(1)); // int 1 -> double 1.0
```

**含 `null` 值的 Map→Properties 是一个真正的抛异常点：**

> `convert/MapToPropertiesConverterTest.java:53-57`
```java
Map<String, Object> map = singletonMap("key", null);
assertThrows(NullPointerException.class, () -> this.converter.convert(map));
```

**Properties→String 内嵌时间戳**，因此输出在不同调用间*不*稳定（`Properties.store` 插入的 `#<date>` 注释行每次都变）：

> `convert/PropertiesToStringConverterTest.java:57-66`
```java
return "#Generated by Microsphere" + LINE_SEPARATOR   // 固定横幅，来自 store(writer, "Generated by Microsphere")
        + "#" + new Date() + LINE_SEPARATOR           // 变动的时间戳行
        + "A=1" + LINE_SEPARATOR;
```

**`Object→Optional` 与不支持的多值目标**返回中性值而非抛异常：

> `convert/multiple/MultiValueConverterTest.java:77-81`
```java
assertEquals(ofSet(1, 2, 3), convertIfPossible("1,2,3", Set.class, Integer.class));
assertNull(convertIfPossible("1,2,3", Array.class, String.class)); // java.lang.reflect.Array 不是容器类型
```

---

## 5. 兼容性说明

- **`java.time` 与 `Date`**：本子系统的方向目标是 `java.time.Duration`（`convert/StringToDurationConverter.java`），
  **不**转到 `java.util.Date` 或其他任何 `java.time` 类型。`Date`/`Instant`/`LocalDate` 解析超出范围——
  `convertIfPossible("1", Date.class)` 为 `null`（`convert/ConverterTest.java:50`）。
- **`Duration.parse` 是严格的**，要求 ISO-8601（`PT…`）；自 Java 8 起可用，因此没有版本门槛。
- **`Character` 与 `char[]`**：`String→Character` 只接受*单个*字符（`convert/StringToCharacterConverter.java:40-42`）；
  转换整串请使用 `String→char[]`（`convert/StringToCharArrayConverter.java:31-34`）。两种目标类型都已注册，因此
  `getConverter(String.class, Character.class)` 与 `getConverter(String.class, char[].class)` 解析为不同的策略。
- **`String→InputStream` 字符集**：单例 `INSTANCE` 使用平台 `DEFAULT_CHARSET`（`convert/StringToInputStreamConverter.java:43-45`）；
  当需要字节级确定性时，用显式 `Charset`/encoding 自行构造实例（`:47-53`）。该转换器依赖 `io.FastByteArrayInputStream`
  与 `nio.charset.CharsetUtils`。
- **序列化往返**：`byte[] ⇄ Object`（`convert/ByteArrayToObjectConverter.java`、`convert/ObjectToByteArrayConverter.java`）
  委派给 `io.microsphere.io.serializer`；Object 侧必须是 `Serializable`（`convert/ByteArrayToObjectConverter.java:39-45`）。

---

## 6. 陷阱与已知限制

- **不支持的转换是 `null`，而非异常。** 没有转换器接受该对时 `getConverter` 返回 `null`（`convert/Converters.java:68`），
  随后 `convertIfPossible` 返回 `null`（`convert/Converter.java:164-167`）。若下游代码无法容忍 `null`，请显式检查
  转换器（或结果）。
- **找到的转换器失败时仍然抛异常。** `AbstractConverter.convert` 把 `doConvert` 包在 `try/catch` 中，记录警告，并通过
  `ExceptionUtils.wrap(e, RuntimeException.class)` 重抛（`convert/AbstractConverter.java:106-112`）。由于 `wrap` 在源
  已是 `RuntimeException` 时原样返回（`util/ExceptionUtils.java:98-100`），`NumberFormatException` /
  `IllegalArgumentException` 以原始类型传播。`ByteArrayToObjectConverter` 对垃圾字节抛 `RuntimeException`
  （`convert/ByteArrayToObjectConverter.java:42-44`；由 `convert/ByteArrayToObjectConverterTest.java:49-52` 证明）。
- **`StringToArrayConverter` 不检查元素转换器是否为 null。** 它解析
  `Converter.getConverter(String.class, componentType)` 并立即解引用
  （`convert/multiple/StringToArrayConverter.java:40-46`）；若数组的*组件类型*没有已注册的 `String` 转换器，就会抛
  `NullPointerException`。基于 `Iterable` 的转换器更安全：它们先检查，元素转换器缺失时返回 `null`
  （`convert/multiple/StringToIterableConverter.java:43-44,60`）。
- **对集合而言 `multiValueType` 参数只是建议。** `StringToIterableConverter` 在 `createMultiValue` 中固定了具体容器——
  `StringToSetConverter` 即使你传 `Queue.class` 也返回 `HashSet`（`convert/multiple/StringToSetConverter.java:29-32`，
  `convert/multiple/StringToIterableConverter.java:47`）。不要假设拿到的正是所请求的那个类。
- **逗号是唯一的分隔符。** `StringToMultiValueConverter` 按 `SymbolConstants.COMMA_CHAR` 切分
  （`convert/multiple/StringToMultiValueConverter.java:40`）；含逗号的字符串不转义就无法表示为多值，且此处没有可配置分隔符。
- **`Object`→数字是两跳递归。** `ObjectToIntegerConverter` 回落到 `convertIfPossible`
  （`convert/ObjectToIntegerConverter.java:39-43`），其本身可能又路由过另一个 `Object→…` 转换器；因此深度模糊的对可能
  返回 `null`，而如果你直接请求 `String→Integer` 则会成功。
- **优先级是深度推导出来的，而非手工设置。** `AbstractConverter.resolvePriority()` 由源和目标类型的继承深度计算一个负值
  （`convert/AbstractConverter.java:139-148`），因此更具体的转换器排在前面。多值转换器把优先级覆写为
  `MIN_PRIORITY - depth`（`convert/multiple/StringToIterableConverter.java:74-78`）——这正是
  `find(String.class, Set.class)` 胜过通用 `Collection` 转换器的原因（`convert/multiple/StringToSetConverterTest.java:113-116`）。
- **“空进空出”规则有一个例外。** `ObjectToOptionalConverter` *不* extends `AbstractConverter`，因此 `convert(null)` 返回
  `Optional.empty()` 而非 `null`（`convert/ObjectToOptionalConverter.java:28-43`、
  `convert/ObjectToOptionalConverterTest.java:48-52`）。若你的代码对 `null` 结果做特殊处理，不要假设它对 `Optional` 目标也成立。
- **`Map→Properties` 拒绝 `null` 值。** 由于 `Properties` extends `Hashtable`，
  `MapToPropertiesConverter.convert(mapWithNullValue)` 抛 `NullPointerException`
  （`convert/MapToPropertiesConverter.java:32-37`、`convert/MapToPropertiesConverterTest.java:53-57`）——单值集合中唯一因
  *输入形状*（而非仅可解析性）导致非受检抛出之处。
- **`Properties→String` 非确定性。** `PropertiesToStringConverter` 调用
  `source.store(writer, "Generated by Microsphere")`（`convert/PropertiesToStringConverter.java:37-39`），而 `store` 会追加一行
  `#<当前日期>` 注释，因此不同调用之间不保证逐字节相等（`convert/PropertiesToStringConverterTest.java:57-66`）。

---

## 7. 另见

- [语言抽象](language-abstractions.md) — 驱动转换器选择的 `Prioritized` 排序运算。
- [反射、类型与 beans](reflection-and-beans.md) — `TypeUtils.resolveActualTypeArgumentClass`，用于推导
  `getSourceType()` / `getTargetType()`（`convert/Converter.java:99-110`）。
- [序列化](serialization.md) — `byte[] ⇄ Object` 背后的 `io.serializer` 后端
  （`convert/ByteArrayToObjectConverter.java`、`convert/ObjectToByteArrayConverter.java`）。
- [集合与过滤器](collections-and-filters.md) — 多值转换器调用到的容器工厂。
- [配置属性元数据](configuration-metadata.md) — 库内主要的类型转换消费者。
- [类型转换 — 开发者指南](../../../developer-guide/microsphere-java-core/zh/type-conversion.md) — 选择/缓存内部机制、
  分发流水线，以及如何通过 SPI 注册你自己的转换器。
