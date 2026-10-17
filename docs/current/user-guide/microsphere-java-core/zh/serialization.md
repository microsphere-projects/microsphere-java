# 序列化

| | |
|---|---|
| **构件** | `io.github.microsphere-projects:microsphere-java-core`（`0.3.20-SNAPSHOT`，`release` 分支） |
| **包** | `io.microsphere.io.serializer` — 18 个主干源码类型 |
| **JDK 级别** | 以 Java 8 编译；CI 在 JDK 8 / 11 / 17 / 21 / 25 上测试 |
| **依赖的其他 `core` 包** | `io.microsphere.reflect`（`JavaType`、`MethodUtils.invokeStaticMethod`、`TypeUtils.resolveTypeArgumentClasses`）、`io.microsphere.io`（`FastByteArrayInputStream`、`FastByteArrayOutputStream`、`IOUtils`）、`io.microsphere.util`（`SizeUtils`、`ClassLoaderUtils`、`ServiceLoaderUtils`、`ArrayUtils`）、`io.microsphere.collection`（`ListUtils`、`MapUtils`）、`io.microsphere.annotation.Nonnull` |
| **外部依赖** | 无 — 纯 JDK |
| **被谁使用** | `convert` — `ObjectToByteArrayConverter` 与 `ByteArrayToObjectConverter` 调用 `DefaultSerializer` / `DefaultDeserializer` 单例。依赖方向是 `convert` → `io.serializer`，绝不反向。 |

本页面中所有 `file:line` 引用都相对于 `microsphere-java-core/src/main/java/io/microsphere/`
（主干源码）或 `microsphere-java-core/src/test/java/io/microsphere/`（测试），读取自 `release` 工作树。
首次完整出现后简写为 `io/serializer/Foo.java:40-55`。

---

## 1. 这个包的用途

**当你需要把一小批固定不变的值类型转换成 `byte[]` 并还原，且希望字节排布可预测时，使用 `io.microsphere.io.serializer`。**
该包提供 `Serializer` / `Deserializer` SPI，并内置九个现成实现，覆盖 `boolean`、`byte`、`char`、`short`、`int`、
`long`、`float`、`double` 和 `String`。每个数值实现都是纯粹的位排布编解码器，除结果数组外不做额外分配 ——
例如 `IntegerSerializer` 逐字节手工写出四个大端字节（`io/serializer/IntegerSerializer.java:21-30`），
`BooleanSerializer` 写出一个 `1`/`0` 字节（`io/serializer/BooleanSerializer.java:26-30`）。
如果你的目标是标量类型的紧凑稳定二进制编码，这些就是正确的工具。

**当具体类型只能在运行时确定时，使用 `Serializers` / `Deserializers`** —— 例如缓存键编码器、通用记录存储、
按 Java 类型标记字段的线协议。它们是按 `ClassLoader` 划分的注册中心，会加载所有通过 `META-INF/services`
注册的 `Serializer`，并允许你按类型询问最匹配的项（`io/serializer/Serializers.java:87-129`）。

**当你准备新增一个编解码器时，继承 `AbstractSerializer<T>` 基类。** 它已经处理了 `null`、已经校验了固定字节宽度、
已经推导出你编码的类型，因此新增编解码器只需实现三个小方法（`io/serializer/AbstractSerializer.java:70-137`）。

**以下场景不要使用它：**

* **你需要序列化真实对象图。** 唯一的通用实现 `DefaultSerializer` 只是 `java.io.ObjectOutputStream` 的一层薄包装
  （`io/serializer/DefaultSerializer.java:52-62`）。它就是 JDK 序列化 —— 连同 JDK 的流格式、JDK 的
  `serialVersionUID` 规则，以及 JDK 的反序列化攻击面。请改用带 schema 的格式（Protobuf、Avro、JSON）。
* **你需要处理不可信输入。** `DefaultDeserializer` 调用 `ObjectInputStream.readObject()` 时不带任何类别的过滤器。
  见 §6 —— 这是本页最重要的一条注意事项。
* **你需要通用文本编码。** `StringSerializer`/`StringDeserializer` 是便捷编解码器，不是字符集框架；
  它们也不会以可安全往返 `null` 的方式注册。见 §6。
* **你想通过注册中心编码 `Enum`。** `EnumSerializer` 存在且可用，但它不在 SPI 文件里，所以 `Serializers`
  永远不会把它交给你。见 §5。

---

## 2. 坐标与版本支持

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

| 事实 | 值 | 证据（`release`） |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | `pom.xml:12` |
| `artifactId` | `microsphere-java-core` | 模块 `pom.xml` |
| 版本属性 | `${revision}`，扁平化处理 | `pom.xml:14` |
| `release` 上 `${revision}` 的值 | `0.3.20-SNAPSHOT` | `pom.xml:54` |
| 最新已发布标签 | `0.3.20` | 分组索引中的发布线表（`docs/current/user-guide/microsphere-java-core/README.md:70`） |
| 构建父 POM | `microsphere-build:0.3.16` | `pom.xml:6-10` |
| Java 级别 | 8，从外部构建父 POM 继承；反应堆中没有 `maven.compiler.*` / `<release>` 属性 | CI 矩阵 `java: [ '8', '11', '17', '21', '25' ]`，`.github/workflows/maven-build.yml:25`，`distribution: 'temurin'` `:33`，构建命令 `:38-45` |
| Spring Framework，JDK 17+ | `7.0.9` | `microsphere-java-parent/pom.xml:23` |
| Spring Framework，JDK `[1.8,17)` | `5.3.39`，通过 `java8-16` profile 生效 | `microsphere-java-parent/pom.xml:54-62` |
| 导入的 BOM | 见 `microsphere-java-parent/pom.xml:22`、`:33-36`、`:40-46` | 同上 |
| 本包所有类型的 `@Since` | `1.0.0` | `io/serializer/Serializer.java:38`、`AbstractSerializer.java:52`、`EnumSerializer.java:15`、`DefaultDeserializer.java:49` |

> [!TIP]
> 本包不接触 Spring，所以 `java8-16` profile 与此无关。真正与 JDK 版本敏感的是
> `DefaultSerializer` / `DefaultDeserializer` 这一对：它们的输出就是 JDK 序列化流，而该流的行为与默认过滤器配置
> 在 JDK 8 → 25 之间发生过变化。见 §5。

---

## 3. API 参考

### 3.1 两个 SPI 接口

`Serializer<S>` — 单个方法，且不是函数式接口（`io/serializer/Serializer.java:40-42`）：

```java
public interface Serializer<S> {
    byte[] serialize(S source) throws IOException;
}
```

`Deserializer<T>` — 是 `@FunctionalInterface`，因此 lambda 就是合法实现（`io/serializer/Deserializer.java:40-43`）：

```java
@FunctionalInterface
public interface Deserializer<T> {
    T deserialize(byte[] bytes) throws IOException;
}
```

两个契约都要求实现类**线程安全**（`io/serializer/Serializer.java:24-25`、`io/serializer/Deserializer.java:24`）。
每个内置实现构造完成后即无状态，因此公开的静态单例可以安全共享。

### 3.2 入口点

| 入口点 | 签名 | 说明 |
|---|---|---|
| `new Serializers()` / `new Serializers(ClassLoader)` | `Serializers()`（`io/serializer/Serializers.java:68-70`）、`Serializers(ClassLoader)`（`:64-66`） | 无参形式使用 `ClassLoaderUtils.getDefaultClassLoader()` |
| `Serializers#loadSPI` | `void loadSPI()`（`:72-79`） | 任何查询之前必须调用；无 `@Nonnull` 保护，其幂等性只是巧合 —— 见 §6 |
| `Serializers#getMostCompatible` | `@Nullable Serializer<?> getMostCompatible(Class<?> serializedType)`（`:87-93`） | 回退到 `Object.class` 条目 |
| `Serializers#getHighestPriority` | `@Nullable <S> Serializer<S> getHighestPriority(Class<S> serializedType)`（`:102-105`） | `first(list)`；空则 `null` |
| `Serializers#getLowestPriority` | `@Nullable <S> Serializer<S> getLowestPriority(Class<S> serializedType)`（`:114-117`） | `last(list)`；空则 `null` |
| `Serializers#get` | `@Nonnull <S> List<Serializer<S>> get(Class<S> serializedType)`（`:126-129`） | 返回 `emptyList()`，绝不返回 `null` |
| `Deserializers` | 同样的五个方法形状（`io/serializer/Deserializers.java:64-129`） | 缓存不对称性见 §5 |
| `AbstractSerializer` 单例 | `BOOLEAN_SERIALIZER`、`BYTE_SERIALIZER`、`CHARACTER_SERIALIZER`、`SHORT_SERIALIZER`、`INTEGER_SERIALIZER`、`LONG_SERIALIZER`、`FLOAT_SERIALIZER`、`DOUBLE_SERIALIZER` | 例如 `io/serializer/IntegerSerializer.java:13` |
| `DefaultSerializer.DEFAULT_SERIALIZER` | `public static final DefaultSerializer`（`io/serializer/DefaultSerializer.java:49`） | 面向 `Object` 的 JDK 流序列化器 |
| `DefaultDeserializer.DEFAULT_DESERIALIZER` | `public static final DefaultDeserializer`（`io/serializer/DefaultDeserializer.java:53`） | 面向 `Object` 的 JDK 流反序列化器 |
| `new EnumSerializer<>(Class<E>)` | `io/serializer/EnumSerializer.java:27-31` | 每个枚举一个实例；**未做 SPI 注册** |

### 3.3 支持的类型与字节排布

| 类型 | 实现类 | 宽度 | `calcBytesLength()` 证据 | 字节序 |
|---|---|---|---|---|
| `Boolean` | `BooleanSerializer` | 1 | `io/serializer/BooleanSerializer.java:21-23`（`BOOLEAN_BYTES_SIZE`） | 不适用 |
| `Byte` | `ByteSerializer` | 1 | `io/serializer/ByteSerializer.java:36-38` | 不适用 |
| `Character` | `CharacterSerializer` | 2 | `io/serializer/CharacterSerializer.java:34-36` | 大端（`:39-45`） |
| `Short` | `ShortSerializer` | 2 | `io/serializer/ShortSerializer.java:16-18` | 大端（`:21-27`） |
| `Integer` | `IntegerSerializer` | 4 | `io/serializer/IntegerSerializer.java:16-18` | 大端（`:21-30`） |
| `Float` | `FloatSerializer` | 4 | `io/serializer/FloatSerializer.java:36-38` | 大端，经 `INTEGER_SERIALIZER`（`:41-44`） |
| `Long` | `LongSerializer` | 8 | `io/serializer/LongSerializer.java:16-18` | **小端**（`:21-34`） |
| `Double` | `DoubleSerializer` | 8 | `io/serializer/DoubleSerializer.java:21-23` | **小端**，经 `LONG_SERIALIZER`（`:26-30`） |
| `String` | `StringSerializer` / `StringDeserializer` | 变长 | 按字符集编码；`StringSerializer.java:57-59`、`StringDeserializer.java:69-71` | 不适用 |
| `Enum<E>` | `EnumSerializer` | 1 或 2 | `io/serializer/EnumSerializer.java:33-36` — 常量数小于 `Byte.MAX_VALUE`（128）时为 `1`，否则为 `2` | 2 字节情形下为大端（`:56`、`:69`） |
| `Object`（JDK 流） | `DefaultSerializer` / `DefaultDeserializer` | 变长 | `io/serializer/DefaultSerializer.java:52-62` | JDK 流格式 |

宽度取自 `SizeUtils`：`UNBOUND_BYTES_SIZE = -1`、`BOOLEAN/BYTE = 1`、`SHORT/CHAR = 2`、`INTEGER/FLOAT = 4`、
`LONG/DOUBLE = 8`（`io/util/SizeUtils.java:38-78`）。

> [!WARNING]
> 2/4/8 字节这三族**不共享同一字节序**。`Integer`/`Float` 是大端，`Long`/`Double` 是小端。
> 这一点在源码中直接可见：`IntegerSerializer.java:24` 把最高有效字节放在下标 `0`，
> 而 `LongSerializer.java:24` 把**最低**有效字节放在那里。任何基于这些编解码器构建的跨类型二进制格式
> 都必须显式处理这个切换。见 §6。

### 3.4 流与 `Closeable` 的归属规则

你永远看不到流。`DefaultSerializer` 与 `DefaultDeserializer` 都在单方法作用域的 try-with-resources 块内打开各自的
`FastByteArray*` 与 `Object*Stream` 对象，并在返回前关闭（`io/serializer/DefaultSerializer.java:54-60`、
`io/serializer/DefaultDeserializer.java:61-68`）。

* 缓冲区是 `io.microsphere.io.FastByteArrayOutputStream` / `FastByteArrayInputStream`，**两者的 `close()` 都是空操作**
  （`io/io/FastByteArrayOutputStream.java:188-189`、`io/io/FastByteArrayInputStream.java:157-158`）。
  因此 `close()` 不可能掩盖或抑制你真正的异常。
* `FastByteArrayOutputStream.toByteArray()` 返回 `Arrays.copyOf(buf, count)` —— 一个全新数组
  （`io/io/FastByteArrayOutputStream.java:159-161`）。所以你拿到的 `byte[]` **归你所有**：修改它、
  或在序列化器被 GC 之后继续持有它，都不可能破坏任何东西。
* `FastByteArrayInputStream` 直接建立在调用方的数组上，**不复制**
  （`io/io/FastByteArrayInputStream.java:81-83`，`:73` 处 javadoc 写明 "The buffer array is not copied"）。
  由于该流在 `deserialize` 内部就已消亡，这只意味着一点：不要在另一线程的 `deserialize` 调用进行中修改入参数组。
* `EnumSerializer` 与各 `AbstractSerializer` 子类每次调用都新建数组，不保留任何引用。

### 3.5 null 与非法输入契约

这正是本包刻意保持不一致之处，且它比字节排布更重要：

| 调用 | 结果 | 证据 |
|---|---|---|
| `AbstractSerializer.serialize(null)` | `null` | `io/serializer/AbstractSerializer.java:71-77` |
| `AbstractSerializer.deserialize(null)` | `null` | `io/serializer/AbstractSerializer.java:80-84` |
| `AbstractSerializer.deserialize(宽度不符)` | `null` — 静默，不抛异常 | `io/serializer/AbstractSerializer.java:87-89` |
| `EnumSerializer.serialize(null)` / `deserialize(null)` | `null` | `io/serializer/EnumSerializer.java:44-47`、`:64-67` |
| `DefaultDeserializer.deserialize(null 或空数组)` | `null` | `io/serializer/DefaultDeserializer.java:57-59` |
| `StringSerializer.serialize(null)` | **`NullPointerException`** | `io/serializer/StringSerializer.java:57-59` — 直接 `source.getBytes(charset)`，无保护 |
| `StringDeserializer.deserialize(null)` | **`NullPointerException`** | `io/serializer/StringDeserializer.java:69-71` — `new String(null, charset)` |
| `StringDeserializer.deserialize(new byte[0])` | `""` | 由 `StringSerializerAndDeserializerTest.java:60-64` 断言 |

`宽度不符 → null` 这条规则属于*退化到中性值*，符合全库一贯的策略：长度错误的数组从不抛异常，
只会给你 `null`。也就是说，调用方必须自己检查。

---

## 4. 示例

以下每段代码都压缩自 `microsphere-java-core/src/test/java/io/microsphere/io/serializer/` 中真实的测试方法体。

### 4.1 注册中心查询，然后往返

`SerializersAndDeserializersTest.java:40-46` — 两个注册中心是分别构造并分别加载的：

```java
serializers = new Serializers();
deserializers = new Deserializers();
serializers.loadSPI();
deserializers.loadSPI();
```

`SerializersAndDeserializersTest.java:49-56` — `String` 解析到字符集编解码器，而不是 JDK 流：

```java
Serializer<String> serializer = serializers.getHighestPriority(String.class);
String value = "Test";
byte[] bytes = serializer.serialize(value);
assertArrayEquals(value.getBytes(StandardCharsets.UTF_8), bytes);   // 4 bytes, not a JDK header
Deserializer<String> deserializer = deserializers.getHighestPriority(String.class);
assertEquals(value, deserializer.deserialize(bytes));
```

`SerializersAndDeserializersTest.java:59-65` — `getMostCompatible` 返回的是原始类型序列化器，因此调用点是未参数化的，
强制转换留给了你：

```java
Serializer serializer = serializers.getMostCompatible(Integer.class);
Integer value = 1;
byte[] bytes = serializer.serialize(value);
Deserializer deserializer = deserializers.getMostCompatible(Integer.class);
assertEquals(value, deserializer.deserialize(bytes));
```

### 4.2 未加载前注册中心是空的

`SerializersTest.java:56-67` 与 `DeserializersTest.java:44-53` 都展示了这个两阶段契约。
`loadSPI()` 之前，`get(...)` 是空列表、每个单值查询都是 `null`；之后全部非 `null`：

```java
// SerializersTest.java:58-66
for (Class<?> supportedClass : SUPPORTED_CLASSES) {
    assertEquals(emptyList(), this.serializers.get(supportedClass));  // before loadSPI()
}
this.serializers.loadSPI();
for (Class<?> supportedClass : SUPPORTED_CLASSES) {
    assertNotNull(this.serializers.get(supportedClass));              // after loadSPI()
}
```

`SerializersTest.java:37-47` 所定义的“受支持类型”集合，就是注册中心契约的全部表面 ——
`Boolean`、`Character`、`Short`、`Integer`、`Long`、`Float`、`Double`、`String`，以及 `Object`：

```java
private static final Class<?>[] SUPPORTED_CLASSES = {
        Boolean.class, Character.class, Short.class, Integer.class, Long.class,
        Float.class, Double.class, String.class, Object.class
};
```

`Byte` 和 `Enum` 不在其中 —— `Byte` 在 SPI 文件里但此处未测，`Enum` 根本不在文件里。

### 4.3 直接使用标量编解码器

`IntegerSerializerTest.java:31-41` — 共享测试基类的子类只需点名一个单例和一个值；基类
`AbstractSerializerTest.java:33-51` 负责完成往返、断言 `calcBytesLength() == getBytesLength()`，
并断言 `getTargetType()` 推导出的正是预期类：

```java
class IntegerSerializerTest extends AbstractSerializerTest<Integer> {

    @Override                                              // IntegerSerializerTest.java:33-36
    protected AbstractSerializer<Integer> getSerializer() {
        return INTEGER_SERIALIZER;
    }

    @Override                                              // IntegerSerializerTest.java:38-41
    protected Integer getValue() {
        return MAX_VALUE;                                  // static import of Integer.MAX_VALUE
    }
}
```

`AbstractSerializerTest.java:28-31,59-63` — 同一个基类还会把**一个 `null` 值灌过每个编解码器**，
这正是上文 `null → null` 契约的强制执行方式：

```java
@Test
void testNull() throws IOException {
    test(this::getNullValue);      // getNullValue() returns null by default (AbstractSerializerTest.java:61-63)
}
```

### 4.4 字符集可参数化的 `String` 编解码器

`StringSerializerAndDeserializerTest.java:48-57` — 给两半都传入 `Charset`，其余一切不变：

```java
Charset charset = StandardCharsets.ISO_8859_1;
StringSerializer customSerializer = new StringSerializer(charset);
StringDeserializer customDeserializer = new StringDeserializer(charset);
String value = "Test";
byte[] bytes = customSerializer.serialize(value);
assertArrayEquals(value.getBytes(charset), bytes);
assertEquals(value, customDeserializer.deserialize(bytes));
```

`StringSerializerAndDeserializerTest.java:66-73` — UTF-16 也能往返，但仅仅因为两侧使用的是**同一个** `Charset` 对象：

```java
Charset charset = StandardCharsets.UTF_16;
assertEquals(value, new StringDeserializer(charset).deserialize(new StringSerializer(charset).serialize(value)));
```

### 4.5 用 JDK 序列化通用对象

`DefaultSerializerAndDeserializerTest.java:37-46` — 注意源对象声明为 `Object`，先装 `String`，再装 `Integer`。
两者都能存活，因为 JDK 流自带类描述符：

```java
Object value = "Test";
byte[] bytes = serializer.serialize(value);
assertEquals(value, deserializer.deserialize(bytes));

value = 1;
bytes = serializer.serialize(value);
assertEquals(value, deserializer.deserialize(bytes));
```

`DefaultDeserializerTest.java:18-23` — 空/`null` 输入的退化行为，被直接断言：

```java
private DefaultDeserializer deserializer = DefaultDeserializer.DEFAULT_DESERIALIZER;

@Test
void testDeserializeOnNull() throws IOException {
    assertNull(deserializer.deserialize(null));
}
```

### 4.6 `EnumSerializer` 的宽度

`EnumSerializerTest.java:41-56` — `TimeUnit` 只有 8 个常量，远低于 128，因此一个字节即可承载 `ordinal()`，
且两个方向的 `null` 都返回 `null`：

```java
EnumSerializer<TimeUnit> serializer = new EnumSerializer<>(TimeUnit.class);
assertEquals(1, serializer.getBytesLength());
byte[] bytes = serializer.serialize(timeUnit);
assertEquals(timeUnit, serializer.deserialize(bytes));
assertNull(serializer.serialize(null));
assertNull(serializer.deserialize(null));
```

`EnumSerializerTest.java:59-69,72-93` — 内嵌的 `E` 枚举恰好声明了 200 个常量，把宽度推高到 2：

```java
EnumSerializer<E> serializer = new EnumSerializer<>(E.class);
assertEquals(2, serializer.getBytesLength());   // 200 constants > Byte.MAX_VALUE (127)
```

### 4.7 编写你自己的编解码器

压缩自 `io/serializer/AbstractSerializer.java:22-48` 的 javadoc 示例，那正是九个内置编解码器遵循的形状：

```java
public class IntegerSerializer extends AbstractSerializer<Integer> {

    public static final IntegerSerializer INTEGER_SERIALIZER = new IntegerSerializer();

    @Override
    protected int calcBytesLength() {
        return INTEGER_BYTES_SIZE;   // 4 — enables the width guard in AbstractSerializer:87-89
    }

    @Override
    protected byte[] doSerialize(Integer value) {
        ByteBuffer buffer = ByteBuffer.allocate(INTEGER_BYTES_SIZE);
        buffer.putInt(value);
        return buffer.array();
    }

    @Override
    protected Integer doDeserialize(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }
}
```

变长编解码器从 `calcBytesLength()` 返回 `UNBOUND_BYTES_SIZE`（`-1`），这会关闭宽度校验。

---

## 5. 兼容性注意事项

### 5.1 JDK 序列化行为在各 JDK 之间的差异

`DefaultSerializer` 与 `DefaultDeserializer` 没有引入任何自有格式 —— 它们调用
`new ObjectOutputStream(...).writeObject(source)`（`io/serializer/DefaultSerializer.java:54-59`）和
`new ObjectInputStream(...).readObject()`（`io/serializer/DefaultDeserializer.java:61-65`）。后果是：

* 字节输出就是 JDK 流格式（`0xAC 0xED` 魔数 + 版本 + 类描述符）。它之所以在 CI 矩阵的 JDK 之间保持稳定，
  仅仅因为那本来就是 JDK 自己的稳定格式；面对 `serialVersionUID` 变更、或对端 JVM 的类路径差异，它**并不**稳定。
* `serialize` 要求参数是 `Serializable`。代码本身不检查：它委派给 `ObjectOutputStream`，
  因此不可序列化的参数会以 `java.io.NotSerializableException` 暴露出来，那是 `IOException`
  （`io/serializer/DefaultSerializer.java:52` 声明 `throws IOException` 且没有 `catch`）。
* `DefaultDeserializer` 的行为不同：它的 `catch (Exception e) { throw new IOException(e); }`
  （`io/serializer/DefaultDeserializer.java:66-68`）把**所有**异常 —— `ClassNotFoundException`、
  `InvalidClassException`，乃至目标类 `readObject` 抛出的 `RuntimeException` —— 统一塌缩成一个 `IOException`。
  若你需要区分它们，请捕获 `IOException` 并检查 `getCause()`。
* Java 8 之后引入的 JDK 反序列化加固机制（`ObjectInputFilter`、`jdk.io.objects.*` 全局开关）是 **JDK** 特性，
  不是本库特性。在 JDK 17+ 上你可以全局配置它；本库既不启用它，也不读取它的配置。见 §6。

### 5.2 `serialVersionUID` 处理

本包中**完全没有**相关处理。在 18 个源文件中 grep `serialVersionUID` 返回零结果。
`DefaultSerializer`/`DefaultDeserializer` 里引入的可序列化标记接口只用于 javadoc 的 `@see` 链接
（`io/serializer/DefaultSerializer.java:23`、`io/serializer/DefaultDeserializer.java:23`）。
实际影响：如果你用 `DefaultSerializer` 序列化某个类，之后修改该类的结构却没有声明 `serialVersionUID`，
JDK 会重新计算它，`DefaultDeserializer` 就会抛出 `InvalidClassException` —— 按 §5.1 的说法表现为 `IOException`。
固定 `serialVersionUID` 完全是你自己的责任。

### 5.3 两个注册中心并不对称

这一点很容易踩：

* **Serializer SPI 文件**在第 9 行注册 `StringSerializer`；**Deserializer SPI 文件**在第 9 行注册
  `StringDeserializer`。类名不对称，行为对称 —— `StringSerializer` 只注册为 `Serializer`，反之亦然，
  因为它们各自只实现一个接口（`io/serializer/StringSerializer.java:44`、`io/serializer/StringDeserializer.java:56`）。
* `Serializers.loadSPI()` 请求**带缓存**的服务加载（`io/serializer/Serializers.java:73`，第三个实参是 `true`）；
  `Deserializers.loadSPI()` 没有（`io/serializer/Deserializers.java:73`，走两参重载）。缓存默认值来自
  `SERVICE_LOADER_CACHED`，它由系统属性 `microsphere.service-loader.cached` 解析，**默认为 `false`**
  （`io/util/ServiceLoaderUtils.java:107`、`:112`、`:138`、`:410-412`）。于是调用 `Deserializers.loadSPI()` 两次
  会重新读取并重新实例化提供者，并且 —— 因为 `loadSPI` 是向按类型分组的列表中*追加*
  （`io/serializer/Deserializers.java:76-77`）—— 第二次调用会把每个已注册列表**翻倍**。
  只有在缓存开启时，调用 `Serializers.loadSPI()` 两次才是安全的。
* 排序在两种情况下完全一致，所以这纯粹是缓存/重复调用层面的差异。
  `ServiceLoaderUtils.loadServicesAsList` 总会调用 `sort(serviceList, Prioritized.COMPARATOR)`
  （`io/util/ServiceLoaderUtils.java:890`）。18 个类型中没有任何一个实现 `Prioritized`，
  因此比较器对任意一对都返回 `0`（`io/lang/Prioritized.java:71-75`），而 `Collections.sort` 是稳定排序，
  所以 SPI 文件的声明顺序被原样保留。**即首个匹配项由文件顺序决定，而非计算出的优先级。**

### 5.4 `Object.class` 回退

`getMostCompatible` 有两步规则（`io/serializer/Serializers.java:87-93`）：

```java
Serializer<?> serializer = getHighestPriority(serializedType);
if (serializer == null) {
    serializer = getLowestPriority(Object.class);
}
```

由于 `DefaultSerializer implements Serializer<Object>`（`io/serializer/DefaultSerializer.java:47`），
且注册中心按每个提供者解析出的第一个类型参数分组（`io/serializer/Serializers.java:74-75`），
`Object.class` 这个键下装的正是 `DefaultSerializer` —— SPI 文件的最后一项。**所以一次成功的 `loadSPI()` 之后，
`getMostCompatible` 对*任何*类型都不会返回 `null`。** 询问 `getMostCompatible(SomeDomainObject.class)`，
你会静默拿到 `DefaultSerializer`。`getLowestPriority(Object.class)` 返回 `Object` 键下的*最后*一项，
按文件顺序即 `DefaultSerializer` / `DefaultDeserializer`。

如果你需要的是“只要精确编解码器，否则什么都不给”，那就别用 `getMostCompatible`。
改用 `getHighestPriority` 并自行处理 `null`。

---

## 6. 陷阱与已知限制

> [!SECURITY]
> **`DefaultDeserializer` 执行的是无过滤的 JDK 序列化。不要把它指向你不信任的数据。**
>
> 基于 `release` 分支源码逐条核实：
>
> * `DefaultDeserializer.deserialize` 在调用方字节之上构造了一个朴素的 `java.io.ObjectInputStream`，
>   然后调用 `readObject()` —— `io/serializer/DefaultDeserializer.java:61-65`。这里没有 `ObjectInputFilter`、
>   没有覆写 `resolveClass` 的子类、没有白名单、没有大小上限，也没有深度配置。在 `io/serializer/` 的全部 18 个类型中
>   grep `ObjectInputFilter`、`resolveClass`、`BadPaddingException`，结果为**零匹配**。
> * 因此它**不**防御：gadget chain 实例化、反序列化期间的任意类加载、目标类 `readObject`/`readResolve` 的副作用、
>   反序列化炸弹（深层或宽对象图耗尽堆）、以及基于 `serialVersionUID` 的类型混淆。
>   它只防御*空输入*（`:57-59`）。
> * 该风险可通过本包之外的公共 API 抵达：`ByteArrayToObjectConverter` 调用
>   `DEFAULT_DESERIALIZER.deserialize(...)`（`io/convert/ByteArrayToObjectConverter.java:22,41`），
>   而该转换器注册在 `META-INF/services/io.microsphere.convert.Converter`（第 3 行），
>   于是任何经由 `Converters` 的 `byte[] → Object` 转换都会把不可信字节送到这里。
>
> **加固必须由你自己完成，手段是 JDK 自带的机制。** 自 Java 9（并回移植到 8u121+）起，平台提供
> `java.io.ObjectInputFilter`，可通过 `ObjectInputStream.setObjectFilter(...)` 按流设置，
> 或通过 `jdk.io.objects.readData` / `jdk.serialFilter` 系统属性全局设置。本库的 `ObjectInputStream`
> 创建在 `DefaultDeserializer` 内部且不对外暴露，因此**经由 `DefaultDeserializer` 无法做到按流过滤** ——
> 它能接受的唯一配置就是全局 `jdk.serialFilter` 属性。对于真正不可信的数据，要么设置那个全局过滤器，
> 要么用一个自带过滤逻辑的 `Deserializer` 替换 `Object` 键下的 SPI 注册项。
> 本包没有提供别的杠杆，本文也不会替你编一个。

其他真实限制：

1. **`Long`/`Double` 是小端，其余全是大端。** `IntegerSerializer.java:23-28` 按最高有效字节优先输出；
   `LongSerializer.java:23-32` 按最低有效字节优先输出。`FloatSerializer` 通过委派给 `INTEGER_SERIALIZER`
   继承了大端（`io/serializer/FloatSerializer.java:41-44`），`DoubleSerializer` 通过委派给 `LONG_SERIALIZER`
   继承了小端（`io/serializer/DoubleSerializer.java:26-30`）。也就是说同一条记录里的 4 字节字段和 8 字节字段
   字节序相反。`CharacterSerializer.java:41-44` 与 `ShortSerializer.java:23-25` 属于大端一族。
2. **宽度不符的 `deserialize` 返回 `null`，而不是报错。** 见 `io/serializer/AbstractSerializer.java:87-89`。
   被截断的缓冲区与合法为 `null` 的值无法区分。如果这个歧义对你重要，请自己拿 `getBytesLength()`（`:109-111`）
   与缓冲区长度比对。
3. **`StringSerializer` / `StringDeserializer` 打破了 `null` 契约。** 两者对 `null` 输入都会 NPE
   （`StringSerializer.java:57-59`、`StringDeserializer.java:69-71`），而其他每个编解码器都返回 `null`。
   由于 `String` *确实*在 SPI 文件里（`Serializer` 文件第 9 行、`Deserializer` 文件第 9 行），
   `serializers.getHighestPriority(String.class).serialize(null)` 会抛异常，而 `Integer` 的对应调用不会。
4. **`Charset` 不编码进字节。** `StringSerializer` 写的就是精确的 `source.getBytes(charset)`，
   没有 BOM、没有长度前缀、没有字符集标记（`io/serializer/StringSerializer.java:57-59`）。
   `String` 与 `UTF_16` 的测试之所以通过，只是因为两侧供给了同一个 `Charset` 对象
   （`StringSerializerAndDeserializerTest.java:48-57,66-73`）。在线上混用字符集就是静默的数据损坏。
5. **`EnumSerializer` 不可被发现，且它编码 `ordinal()` 而非 `name()`。** 它不在两个 SPI 文件中，
   所以你必须为每个枚举类型自行构造（`io/serializer/EnumSerializer.java:27-31`）。它写入 `e.ordinal()`（`:49-59`），
   读回时用 `enums[ordinal]`（`:69-70`），这意味着：重排或删除枚举常量会静默改写已存储字节的含义；
   越界的 ordinal 会抛 `ArrayIndexOutOfBoundsException`，而不是 `IOException`。
   `EnumSerializer` 也是唯一*不*继承 `AbstractSerializer` 的编解码器（`:17`），所以它的宽度校验是手写的。
6. **`Serializers` / `Deserializers` 不是线程安全的。** 注册中心的映射表是普通 `HashMap`
   （`io/serializer/Serializers.java:60`，经 `MapUtils.newHashMap()`，后者在 `io/collection/MapUtils.java:478-480`
   返回 `new HashMap<>()`），而 `loadSPI()` 用非原子的 `computeIfAbsent` + `add` 修改它们
   （`io/serializer/Serializers.java:76-77`）。请在启动阶段于单一线程内构建注册中心；此后读方法
   （`get`、`getHighestPriority`、`getMostCompatible`）因为只读，所以可以安全共享。
7. **SPI 文件无提供者时 `loadSPI()` 抛异常。** `ServiceLoaderUtils.loadServicesAsList` 在迭代器为空时抛出
   `IllegalArgumentException`（`io/util/ServiceLoaderUtils.java:882-886`）。这是本包少数真正的抛出点之一，
   如果你移除了服务文件（例如被 shade 插件丢弃了 `META-INF/services`），`new Serializers().loadSPI()` 就会触发它。
   注意：查询未知类型**不**抛异常 —— 那只给 `null`/`emptyList()`。
8. **`getMostCompatible` 返回原始类型 `Serializer<?>`**（`io/serializer/Serializers.java:87`）—— 类型参数丢失，
   必须在调用点强转，正如 `SerializersAndDeserializersTest.java:60-64` 所做的那样。
9. **非受检异常的抛出点在 `convert`，不在本包。** `io.serializer` 内部的每条失败路径要么抛出已声明的
   `IOException`，要么返回中性值。序列化失败变成非受检异常的唯一位置是 `ByteArrayToObjectConverter.doConvert`，
   它捕获 `IOException` 并 `throw new RuntimeException(e)`（`io/convert/ByteArrayToObjectConverter.java:42-44`）——
   并且把结果强转为 `Serializable`（`:41`），额外引入一条 `ClassCastException` 路径。
   若经由 `Converters` 调用，请捕获 `RuntimeException`。

---

## 7. 参见

* [I/O、NIO、扫描与文件过滤器](io-and-scanning.md) — `FastByteArrayInputStream` / `FastByteArrayOutputStream`，
  即两个 `Default*` 类所包装的缓冲区，以及 `io` 包的资源关闭纪律。
* [类型转换](type-conversion.md) — `ObjectToByteArrayConverter` 与 `ByteArrayToObjectConverter`，
  这两个转换器把本包的 JDK 序列化路径经由 `Converter` SPI 暴露了出去。
* [反射、类型与 JavaBean](reflection-and-beans.md) — `JavaType`/`TypeUtils.resolveTypeArgumentClasses`
  决定了每个提供者的注册中心键；`MethodUtils.invokeStaticMethod` 是 `EnumSerializer` 调用 `values()` 的方式。
* [类加载与构件](classloading-and-artifacts.md) — `ClassLoaderUtils.getDefaultClassLoader()` 与 `ServiceLoaderUtils`，
  支撑起按 `ClassLoader` 划分的注册中心及其缓存开关。
* [开发者指南 — 序列化](../../developer-guide/microsphere-java-core/serialization.md) — 选择算法、注册中心循环、
  设计模式、线程安全分析与扩展点。
