# 配置属性元数据

| | |
|---|---|
| **构件** | `io.github.microsphere-projects:microsphere-java-core`（`0.3.20-SNAPSHOT`，`release` 分支） |
| **包** | `io.microsphere.metadata` —— 9 个主源码类型 |
| **模型类型** | `io.microsphere.beans.ConfigurationProperty` + 嵌套的 `ConfigurationProperty.Metadata` |
| **JDK 级别** | 按 Java 8 编译；CI 在 JDK 8 / 11 / 17 / 21 / 25 上测试 |
| **依赖 `io.microsphere.util`** | `ServiceLoaderUtils.loadServicesList`、`ClassLoaderUtils.nullSafeClassLoader`、`ClassUtils.getTypeName`、`Assert.assertNotEmpty` / `assertNotNull` |
| **使用的其他 `core` 包** | `io.microsphere.json`（`JSONUtils.readValues` / `append` / `appendName` / `writeBeanAsString`）、`io.microsphere.beans`、`io.microsphere.collection`、`io.microsphere.constants`（`ResourceConstants`、`SymbolConstants`）、`io.microsphere.io.IOUtils`、`io.microsphere.nio.charset.CharsetUtils`、`io.microsphere.logging`、`io.microsphere.lang.Prioritized`、`io.microsphere.lang.function.ThrowableSupplier`、`io.microsphere.annotation.*`（来自 `microsphere-java-annotations`） |
| **外部依赖** | 无 —— JSON 读写使用仓库内的 `io.microsphere.json` 模型，而非 Jackson 或 Gson |

配置元数据是对应用所暴露属性的机器可读描述：*名称、类型、默认值、是否必填、人类可读的说明，以及它声明在哪里*。
Spring IDE 会读取一份等价的文档，从而在 `application.properties` 上提供自动补全和文档悬浮提示；Microsphere 以同样的
思路把文件放在 `META-INF/microsphere/configuration-properties.json`，而 `io.microsphere.metadata` 包就是该文件的运行时
对偶——它负责**读取**该文件、从 classpath（可能来自多个 jar）**加载**它，以及**生成**它。编译期的那一半位于兄弟模块
[`microsphere-annotation-processor`](../../zh/microsphere-annotation-processor.md)，另有专页说明。

| 契约 | 种类 | 一句话职责 | 内置实现 |
|---|---|---|---|
| `ConfigurationPropertyReader` | SPI 接口 | 把元数据内容解析为 `List<ConfigurationProperty>` | `DefaultConfigurationPropertyReader`（JSON） |
| `ConfigurationPropertyLoader` | SPI 接口 | 从某个来源取得元数据，并合并所有能找到的内容 | `MetadataResourceConfigurationPropertyLoader`、`AdditionalMetadataResourceConfigurationPropertyLoader`、抽象的 `ClassPathResourceConfigurationPropertyLoader` |
| `ConfigurationPropertyGenerator` | SPI 接口 | 把单个 `ConfigurationProperty` 渲染为 JSON 文本 | `DefaultConfigurationPropertyGenerator`、`ReflectiveConfigurationPropertyGenerator` |

---

## 1. 版本信息与兼容区间

包内全部九个类型，以及它们所操作的 `ConfigurationProperty` 模型，都在 Javadoc 中记录了 `@since 1.0.0`
（`metadata/ConfigurationPropertyReader.java:39`、`metadata/ConfigurationPropertyLoader.java:45`、
`metadata/ConfigurationPropertyGenerator.java:54`、`beans/ConfigurationProperty.java:61`），因此这套契约在整个 `0.x`
线上始终稳定。

| | |
|---|---|
| **GroupId** | `io.github.microsphere-projects`（`pom.xml:12`） |
| **Reactor 版本** | `${revision}` = `0.3.20-SNAPSHOT`（`pom.xml:54`）；`0.3.20` 标签已发布 |
| **BOM** | `microsphere-java-parent/pom.xml` 导入 `microsphere-all-bom:0.3.13`（`:22`、`:33-34`） |
| **Spring** | 导入 `spring-framework-bom`（`microsphere-java-parent/pom.xml:40-46`）；默认 `7.0.9`（`:23`），在由 `<jdk>[1.8,17)</jdk>` 激活的 `java8-16` profile 下为 `5.3.39`（`:54-62`，激活条件 `:57`） |
| **JDK 矩阵** | Temurin 8、11、17、21、25（`.github/workflows/maven-build.yml:25`，`distribution: 'temurin'` `:33`） |

> [!NOTE]
> 本 reactor 中**不存在**任何 `maven.compiler.*` 或 `<release>` 属性。字节码级别继承自外部父工程
> `io.github.microsphere-projects:microsphere-build:0.3.16`（`pom.xml:6-10`）。该包自身只使用 JDK 8 的 API
> （`ClassLoader.getResourceAsStream`、`Enumeration<URL>`、`try`-with-resources），因此无需 multi-release jar 即可
> 从 Java 8 向上使用。

上表列出 Spring 仅为完整性起见：`io.microsphere.metadata` 中没有任何类型导入 Spring。JSON 文档格式由仓库内的
`io.microsphere.json` 模型定义，reader 与 generator 直接调用它——请阅读 [JSON 支持](json.md)，不要假设有第三方映射器。

添加依赖（annotation processor 是编译期的搭档，通常置于 `provided`）：

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version>
</dependency>

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-annotation-processor</artifactId>
    <version>0.3.20</version>
    <scope>provided</scope>
</dependency>
```

---

## 2. 属性模型：`ConfigurationProperty` 与 `Metadata`

模型定义在 `io.microsphere.beans`（`beans/ConfigurationProperty.java:63`），因为它是一个 JavaBean，但它是本包的词汇表，
故在此说明。

| 成员 | 声明 | 说明 |
|---|---|---|
| `getName()` | `final String name`，`:68-69`、`:127` | 不可变；构造器中断言非 null（`:120`） |
| `getType()` | `String type`，`:74-75`、`:132` | 是*类型名*，不是 `Class` —— 见陷阱 8 |
| `setType(Class)` / `setType(String)` | `:136-139`、`:141-144` | `Class` 重载存入 `ClassUtils.getTypeName(type)` |
| `getValue()` / `getDefaultValue()` | `Object`，`:80-81`、`:86-87` | `@Nullable`，无类型约束 |
| `isRequired()` | `boolean required`，`:92`、`:164` | 始终序列化，从不省略 |
| `getDescription()` | `String`，`:97-98`、`:101` | `@Nullable` |
| `getMetadata()` | `final Metadata metadata`，`:112-113`、`:173` | 在构造器中立即创建（`:123`） |
| `Metadata.getSources()` / `getTargets()` | `:258-263`、`:282-287` | 懒初始化的 `LinkedHashSet<String>(2)`——保持插入顺序且去重 |
| `Metadata.getDeclaredClass()` / `getDeclaredField()` | `:294-295`、`:304-305` | `@Nullable`；IDE 应跳转到的声明字段 |

存在两个构造器：`ConfigurationProperty(String name)` 把类型默认成 `String`（`beans/ConfigurationProperty.java:115-117`），
`ConfigurationProperty(String name, Class<?> type)` 见 `:119-124`。`equals`/`hashCode` 会比较**全部七个**成员，包括
`Metadata`（`:178-202`），这正是第 4 节“跨 jar 合并”问题可被观察到的原因：名称相同但描述不同的两个属性并不相等。
换句话说，整条链路上唯一会自行去重的地方，是 `Metadata` 内部这两个懒创建的 `LinkedHashSet`：同名属性的 `sources`
条目会被合并，而属性条目本身不会被合并。

```java
ConfigurationProperty property = new ConfigurationProperty("server.port", Integer.class);
property.setValue(8080);
property.setDefaultValue(8080);
property.setRequired(true);
property.setDescription("The port number for the server");

ConfigurationProperty.Metadata metadata = property.getMetadata();
metadata.getSources().add("application.properties");
metadata.getTargets().add("server");
metadata.setDeclaredClass("com.example.ServerConfig");
metadata.setDeclaredField("port");
```

_Source: `microsphere-java-core/src/test/java/io/microsphere/JSONTestUtils.java:51-64`_

属性名称的权威来源是注解 `io.microsphere.annotation.ConfigurationProperty`
（`microsphere-java-annotations/src/main/java/io/microsphere/annotation/ConfigurationProperty.java:60-88`），其
`name`/`type`/`defaultValue`/`required`/`description` 成员与模型一一对应，而其三个来源常量——`system-properties`、
`environment-variables`、`application`（`:43`、`:48`、`:53`）——就是你在 `Metadata.sources` 里最常见的取值。

---

## 3. 读取元数据：`ConfigurationPropertyReader`

`ConfigurationPropertyReader`（`metadata/ConfigurationPropertyReader.java:41`）继承 `Prioritized`，暴露三个层层递进的
`read` 重载：

| 方法 | 源码 | 委托给 |
|---|---|---|
| `List<ConfigurationProperty> read(InputStream)` | `:66-68`（default） | 用 `CharsetUtils.DEFAULT_CHARSET` 包装为 `InputStreamReader` |
| `List<ConfigurationProperty> read(Reader)` | `:94-96`（default） | 先 `IOUtils.copyToString(reader)`，再调 `read(String)` |
| `List<ConfigurationProperty> read(String content)` | `:119`（抽象） | 真正的解析，格式由实现决定 |

三者都标注 `@Nonnull @Immutable`，且声明 `throws Throwable`——一条刻意不加类型的受检失败通道（见陷阱 9）。

`DefaultConfigurationPropertyReader`（`metadata/DefaultConfigurationPropertyReader.java:39`）是唯一内置实现，也是唯一
受支持的格式。它把内容解析为 JSON 对象数组（`:43` —— `JSONUtils.readValues(content, List.class, Map.class)`），并为每个
条目映射六个键加一个嵌套的 `metadata` 对象（`:59-89`）：

```java
@Override
public List<ConfigurationProperty> read(String content) throws Throwable {
    List<Map<String, Object>> configurationPropertiesMaps = readValues(content, List.class, Map.class);
    ...
    return unmodifiableList(configurationProperties);
}
```

从资源读取是常规入口：

```java
ClassLoader classLoader = getClass().getClassLoader();
try (InputStream inputStream = classLoader.getResourceAsStream(ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE)) {
    List<ConfigurationProperty> configurationProperties = new DefaultConfigurationPropertyReader().read(inputStream);
    assertEquals(2, configurationProperties.size());
}
```

_Source: `metadata/DefaultConfigurationPropertyReaderTest.java:47-55`_

同一个 reader 指向注解处理器产出的文件时，会得到本模块那七条已记录的属性：

```java
private static final String JSON_RESOURCE_NAME = "META-INF/microsphere/configuration-properties.json";

ClassLoader classLoader = getClassLoader(getClass());
try (InputStream inputStream = classLoader.getResourceAsStream(JSON_RESOURCE_NAME)) {
    List<ConfigurationProperty> configurationProperties = reader.read(inputStream);
    assertEquals(7, configurationProperties.size());
}
```

_Source: `metadata/ConfigurationPropertiesJSONReaderTest.java:39`、`:48-55`_

> [!IMPORTANT]
> `DefaultConfigurationPropertyReader.getPriority()` 返回 `MIN_PRIORITY`
> （`metadata/DefaultConfigurationPropertyReader.java:54-57`），即*最低*优先级——请记住
> `Prioritized.MIN_PRIORITY` 是 `Integer.MAX_VALUE`，而 `MAX_PRIORITY` 是 `Integer.MIN_VALUE`
> （`lang/Prioritized.java:86`、`:81`）。你自己写的 reader 只要数值更小，就会排在前面。

---

## 4. 加载元数据：`ConfigurationPropertyLoader`

### 4.1 资源在哪里，按什么顺序

两个资源位置是常量，不需要你记忆约定：

| 常量 | 取值 | 声明位置 |
|---|---|---|
| `ResourceConstants.MICROSPHERE_METADATA_RESOURCE` | `META-INF/microsphere/` | `constants/ResourceConstants.java:38` |
| `CONFIGURATION_PROPERTY_METADATA_RESOURCE` | `META-INF/microsphere/configuration-properties.json` | `:54`（文件名 `:43`） |
| `ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE` | `META-INF/microsphere/additional-configuration-properties.json` | `:60`（文件名 `:48`） |

`ConfigurationPropertyLoader.loadAll()`（`metadata/ConfigurationPropertyLoader.java:91-106`）是聚合入口。它通过 Java SPI
发现所有已注册的 loader——`:93` 处的 `ServiceLoaderUtils.loadServicesList(ConfigurationPropertyLoader.class)`，该调用读取
`META-INF/services/io.microsphere.metadata.ConfigurationPropertyLoader`，并用 `Prioritized.COMPARATOR` 对实例排序
（`util/ServiceLoaderUtils.java:890`）——然后按该顺序逐个调用 `load()`，把结果追加进一个不可修改的列表（`:94-105`）。
返回 `null` 或空列表的 loader 不贡献任何内容（`:98` 的 `isNotEmpty` 过滤）；抛异常的 loader 会以 `error` 级别记录后被跳过
（`:101-103`）。

| Loader | 资源 | 优先级 | 是否注册在 `META-INF/services`？ |
|---|---|---|---|
| `MetadataResourceConfigurationPropertyLoader` | `configuration-properties.json` | `MIN_PRIORITY` = `Integer.MAX_VALUE`（`:40-42`） | **没有**（见下方警告） |
| `AdditionalMetadataResourceConfigurationPropertyLoader` | `additional-configuration-properties.json` | `MIN_PRIORITY + 9`（`:40-42`）——会溢出，见陷阱 7 | 是 |
| `ClassPathResourceConfigurationPropertyLoader`（抽象） | 由构造器指定 | 继承 | 否——请继承它 |
| 仅测试用 `DefaultConfigurationPropertyLoader` / `Empty…` / `Null…` / `Error…` | 内存 | 默认 `NORMAL_PRIORITY` | 仅 test 作用域 |

### 4.2 单次命中与全量命中

`ClassPathResourceConfigurationPropertyLoader`（`metadata/ClassPathResourceConfigurationPropertyLoader.java:47`）是你需要
继承的基类。它的 `load()`（`:80-92`）只有两种行为，由 `loadedAll` 标志决定：

* `loadedAll == false` → `classLoader.getResourceAsStream(resourceName)`（`:89`）——**只取 classpath 上的第一份**。
  其他 jar 中同路径的文件被静默忽略。
* `loadedAll == true` → `classLoader.getResources(resourceName)`（`:83`），每个 URL 触发一次 `read`（`:84-87`）——所有
  jar 的副本都会被解析并追加。

两种方式下，打不开流都不是错误：`load(list, streamSupplier)`（`:94-104`）会按 `trace` 级别记录然后返回（`:96-101`）。
class loader 永远不为 `null`——构造器把它交给 `ClassLoaderUtils.nullSafeClassLoader`（`:74`，实现见
`util/ClassLoaderUtils.java:218-220`）；空白的资源名会以 `IllegalArgumentException` 拒绝（`:72`）。

```java
ClassPathResourceConfigurationPropertyLoader loader =
    new ClassPathResourceConfigurationPropertyLoader(ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE) {
    };
List<ConfigurationProperty> configurationProperties = loader.load();
assertEquals(2, configurationProperties.size());
```

_Source: `metadata/ClassPathResourceConfigurationPropertyLoaderTest.java:41-46`、`:93-96`_

`loadedAll` 的两种变体——`(String, boolean)` 与 `(String, ClassLoader, boolean)`——在同一测试套中被覆盖（`:56-60`、
`:76-85`），两处资源缺失的用例断言的是**空列表而非异常**（`:62-74`）。reader 在基类内部被硬编码构造
（`new DefaultConfigurationPropertyReader()`，`:76`），因此自定义 reader 不会自动作用于该路径（陷阱 6）。

聚合行为，正如测试所钉住的形态：

```java
List<ConfigurationProperty> configurationProperties = loadAll();
assertEquals(3, configurationProperties.size());                     // 2（additional）+ 1（测试 loader）
assertEquals(newConfigurationProperty(), configurationProperties.get(2));
```

_Source: `metadata/ConfigurationPropertyLoaderTest.java:44-49`_

> [!WARNING]
> `0.3.20` 中有两个 loader 实现实际上不会被调用，不应依赖它们参与 `loadAll()`：
>
> 1. `MetadataResourceConfigurationPropertyLoader` **未注册**在
>    `microsphere-java-core/src/main/resources/META-INF/services/io.microsphere.metadata.ConfigurationPropertyLoader`
>    中——该文件只包含 `io.microsphere.metadata.AdditionalMetadataResourceConfigurationPropertyLoader`。
>    因此 `loadAll()` 自己永远不会读取 `META-INF/microsphere/configuration-properties.json`；确有需要请直接实例化该
>    loader。
> 2. `loadAll()` 的文档声称“找不到 loader 时返回空列表”（`metadata/ConfigurationPropertyLoader.java:84`），但 `:93`
>    处的发现调用位于 `:96-103` 的 `try`/`catch` **之外**，而 `ServiceLoaderUtils` 在没有声明任何实现时会抛出
>    `IllegalArgumentException`（`util/ServiceLoaderUtils.java:882-886`）。如果 service 文件被剥离（shading，或迁移到
>    `module-info`），`loadAll()` 会抛异常而不是优雅降级。

---

## 5. 生成元数据：`ConfigurationPropertyGenerator`

`ConfigurationPropertyGenerator`（`metadata/ConfigurationPropertyGenerator.java:56`）是最小的契约：
`String generate(ConfigurationProperty)` 返回 JSON 文本，标注 `@Nonnull`，参数为 `null` 时抛
`IllegalArgumentException`（`:66`）。注解处理器正是调用它，把 loader 提供的属性追加到生成的文件里
（`microsphere-annotation-processor/src/main/java/io/microsphere/annotation/processor/ConfigurationPropertyAnnotationProcessor.java:143-146`），
并以 `loadFirstService(ConfigurationPropertyGenerator.class)` 解析出实现
（`ConfigurationPropertyJSONElementVisitor.java:65`）。

| 实现 | 机制 | `getPriority()` | 是否注册？ |
|---|---|---|---|
| `DefaultConfigurationPropertyGenerator` | 手工拼装 `StringBuilder`，调用 `JSONUtils.append` | `MIN_PRIORITY`（`:161-164`） | 是——唯一的注册项 |
| `ReflectiveConfigurationPropertyGenerator` | `JSONUtils.writeBeanAsString(property)`（`:78`） | 继承 `NORMAL_PRIORITY`（`lang/Prioritized.java:98-100`） | 否 |

两者都会在入口拒绝 `null`（`DefaultConfigurationPropertyGenerator.java:82`、
`ReflectiveConfigurationPropertyGenerator.java:77`）。

```java
ConfigurationProperty configurationProperty = new ConfigurationProperty("server.port", Integer.class);
String json = generator.generate(configurationProperty);
JSONObject jsonObject = new JSONObject(json);
assertEquals("server.port", jsonObject.getString("name"));
assertEquals("java.lang.Integer", jsonObject.getString("type"));

configurationProperty.setValue(8080);
json = generator.generate(configurationProperty);
jsonObject = new JSONObject(json);
assertEquals(8080, jsonObject.getInt("value"));
```

_Source: `metadata/DefaultConfigurationPropertyGeneratorTest.java:59-72`_

默认 generator 无条件输出 `name` 与 `type`，始终输出 `required`，并在 `value`、`defaultValue`、`description` 为 `null`
时**省略**它们（`:95`、`:101`、`:110`）；`metadata` 总是输出（`:116-117`），其内部的 `sources`、`targets`、`declaredClass`、
`declaredField` 在空或 `null` 时各自省略，末尾多余逗号会被修剪（`:123-159`）。因此一个空的 `Metadata` 渲染为 `{}`：

```java
Metadata metadata = new Metadata();
String json = generator.generate(metadata);
assertEquals("{}", json);
assertNull(jsonObject.opt("sources"));
assertNull(jsonObject.opt("targets"));
```

_Source: `metadata/DefaultConfigurationPropertyGeneratorTest.java:102-111`_

当你需要 bean 的完整属性集时，应与反射变体对齐，测试也用同一个调用来断言往返形态：

```java
ConfigurationProperty configurationProperty = newConfigurationProperty();
String json = generator.generate(configurationProperty);   // writeBeanAsString(...)
assertConfigurationPropertyJSON(json);
```

_Source: `metadata/ReflectiveConfigurationPropertyGeneratorTest.java:46-51`_

> [!TIP]
> 想确认你预期的元数据是否真的写进了文件，观察构建输出即可：处理器在最后一轮通过 `printNote(...)`
> （`ConfigurationPropertyAnnotationProcessor.java:154`）打印出生成的完整 JSON，其中也包含由
> `ConfigurationPropertyLoader.loadAll()` 贡献的条目。如果某个 loader 的产物没有出现在这里，就说明它并未被注册
> （见第 4 节中的警告）。

---

## 6. 常见陷阱

1. **元数据资源缺失不算错误。** `ClassPathResourceConfigurationPropertyLoader.load(...)`（`:94-104`）把 `null` 的
   `InputStream` 视为“无事可做”，按 `trace` 记录后返回空列表。如果你预期存在 `configuration-properties.json` 却从未
   生成过，链路上不会有任何告警。请对*内容*做断言，或者确认 `microsphere-annotation-processor` 确实进入了注解处理器路径
   （`META-INF/services/javax.annotation.processing.Processor`）。
2. **合并与去重不会按属性名发生。** `loadAll()` 做的是拼接（`metadata/ConfigurationPropertyLoader.java:99`），基类
   也是按 URL 拼接（`:86`）。同一个在两个 jar 中声明的属性会出现两次。去重只发生在单个属性*内部*，即 `sources` 与
   `targets` 这两个 `LinkedHashSet`（`beans/ConfigurationProperty.java:258-263`、`:282-287`）。若你需要“同名只留一条”，
   得自己做——`ConfigurationProperty.equals` 覆盖全部七个字段（`:178-190`），所以放进 `Set` 也不会折叠近似重复项。
3. **`loadedAll=false` 相当于 `classpath:`，`loadedAll=true` 相当于 `classpath*:`。** 全量分支使用
   `ClassLoader.getResources`（`:83`），它会沿 class loader 父链遍历，并为每个 jar 条目给出一个 URL——即 Spring
   `classpath*:` 的 JDK 等价物。单次命中分支用 `getResourceAsStream`（`:89`），停在第一个赢家：若十个 jar 都有
   `configuration-properties.json`，你只读到一份。两个内置 loader 都走单次命中的构造器
   （`MetadataResourceConfigurationPropertyLoader.java:36`、`AdditionalMetadataResourceConfigurationPropertyLoader.java:36`），
   所以默认都不跨 jar 合并。`getResources` 匹配的是**精确**资源路径——不支持 `**` 模式、不支持文件名通配、不做目录扫描
   （那是 `io.microsphere.io.scanner`，参见 [I/O 与扫描](io-and-scanning.md)）。
4. **`null` 与空值被容忍，异常被吞掉——但并非全部。** `@Nullable load()`（`metadata/ConfigurationPropertyLoader.java:67-68`）
   明确允许返回 `null`，`loadAll()` 会过滤它（`:98`）。抛异常的 loader 会作为 `Throwable` 被捕获并记录日志（`:101-103`）
   ——你的元数据可能在只留下一行 `error` 日志的情况下静默缩水成空列表。降级策略的例外是发现本身：没有 service 文件 ⇒
   `IllegalArgumentException` 逃逸（见 4.1 节的警告）。
5. **reader 对它假定存在的键十分严格。** `readConfigurationProperty` 会对 `(Boolean) map.get("required")` 拆箱
   （`metadata/DefaultConfigurationPropertyReader.java:64`），因此缺少 `required` 的 JSON 条目会抛
   `NullPointerException`；`initMetadata` 解引用嵌套对象时没有 null 检查（`:79-80`），所以缺少 `metadata` 的条目同样抛
   `NullPointerException`；而 `configurationProperty.setType(type)` 会对缺失或空的 `type` 抛 `IllegalArgumentException`
   （`beans/ConfigurationProperty.java:141-144`）。这些与本包的静默降级策略相矛盾，已记入项目的 NPE 加固审计。
6. **reader 的 SPI 已注册却无人查询。** `META-INF/services/io.microsphere.metadata.ConfigurationPropertyReader` 指明了
   `DefaultConfigurationPropertyReader`，但整个 reactor 中没有任何地方调用
   `loadServicesList(ConfigurationPropertyReader.class)`；loader 基类直接构造默认 reader
   （`metadata/ClassPathResourceConfigurationPropertyLoader.java:76`）。想更换格式，请继承 loader 并自行使用包可见的
   `load(List, ThrowableSupplier)` 钩子（`:94`）。
7. **`MIN_PRIORITY + 9` 会溢出。** `AdditionalMetadataResourceConfigurationPropertyLoader.getPriority()` 返回
   `MIN_PRIORITY + 9`（`:41`），而 `MIN_PRIORITY` 是 `Integer.MAX_VALUE`（`lang/Prioritized.java:86`），于是数值回绕为
   `-2147483640`——接近 `MAX_PRIORITY`，也就是说该 loader 排在**最前**而非最后。配套测试拿同一个会溢出的表达式做断言
   （`AdditionalMetadataResourceConfigurationPropertyLoaderTest.java:51-54`），因此缺陷在那里不可见。请用自己的显式
   优先级来控制 `loadAll()` 的顺序。
8. **`type` 是字符串，本包从不解析它。** `ConfigurationProperty.getType()` 返回记录下来的类型名
   （`beans/ConfigurationProperty.java:132`）；reader 把原始 JSON 字符串原样透传
   （`metadata/DefaultConfigurationPropertyReader.java:61`、`:67`）。把 `java.lang.String[]` 解析成 `Class`、把
   `defaultValue` 转换成该类型，是 `io.microsphere.reflect` 与 `io.microsphere.convert` 的职责，JSON 层底层已经在使用它们
   （`json/JSONUtils.java:63`、`:66-67`、`:1427-1442`）。参见 [类型转换](type-conversion.md) 与
   [反射与 JavaBean](reflection-and-beans.md)。
9. **`throws Throwable` 是契约的一部分。** 两个 reader 方法（`metadata/ConfigurationPropertyReader.java:66`、`:94`、
   `:119`）与 `ConfigurationPropertyLoader.load()`（`:68`）都声明 `throws Throwable`，因此 `catch (Exception)` 无法满足
   编译要求。`loadAll()` 是唯一不 `throws` 的 API（`:91`）。
10. **不要手工编辑生成的文件。** `META-INF/microsphere/configuration-properties.json` 每次编译都会被注解处理器重写
    （`ConfigurationPropertyAnnotationProcessor.java:149-156`）；目前随
    `microsphere-java-core/src/main/resources/META-INF/microsphere/configuration-properties.json` 发布的七条条目就是它的
    产物。额外的属性应放进 `additional-configuration-properties.json`，由某个 `ConfigurationPropertyLoader` 提供。

---

## 7. 另见

* [类型转换](type-conversion.md) —— 把 `getType()` / `getDefaultValue()` 变成可用值
* [反射与 JavaBean](reflection-and-beans.md) —— 作为 JavaBean 的 `ConfigurationProperty`、`BeanUtils.resolvePropertiesAsMap`
* [工具类与常量](util-and-constants.md) —— `ServiceLoaderUtils`、`ClassLoaderUtils`、`ResourceConstants`
* [JSON 支持](json.md) —— reader 与两个 generator 背后的 `io.microsphere.json` 模型
* [注解处理器 —— 模块页](../../zh/microsphere-annotation-processor.md) —— 负责写出这些文件的编译期那一半
* 开发者视角的深入解析：[配置属性元数据 —— 开发者指南](../../../developer-guide/microsphere-java-core/zh/configuration-metadata.md)
