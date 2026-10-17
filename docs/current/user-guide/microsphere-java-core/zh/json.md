# JSON

| | |
|---|---|
| **构件（Artifact）** | `io.github.microsphere-projects:microsphere-java-core`（`0.3.20-SNAPSHOT`，`release` 分支） |
| **包（Package）** | `io.microsphere.json` —— 8 个主源码类型 |
| **JDK 级别** | Java 8 字节码，继承自外部构建父 POM；CI 在 JDK 8 / 11 / 17 / 21 / 25 上验证 |
| **来源（Derived from）** | 一个 **`org.json` 的内部 fork（vendored fork）**，具体是 Android 的 `org.json` 实现 —— `json/package-info.java:19` 明确写着 "Fork the source code from org.json"，且 `JSON.java:2`、`JSONArray.java:2`、`JSONException.java:2`、`JSONObject.java:2`、`JSONStringer.java:2`、`JSONTokener.java:2` 都带有 `Copyright (C) 2010 The Android Open Source Project`。`JSONUtils.java:2-16` 与 `package-info.java:2-16` 使用 Apache 许可证头，是 Microsphere 的原创代码。 |
| **依赖 `core` 中的其他包** | `io.microsphere.beans`（`BeanUtils.resolvePropertiesAsMap`、`BeanUtils.findWriteMethod`、`BeanUtils.getBeanMetadata`、`BeanMetadata`）、`io.microsphere.collection`（`ListUtils`、`MapUtils`、`EnumerationUtils`、`QueueUtils`、`SetUtils`）、`io.microsphere.util`（`ClassUtils`、`CharSequenceUtils`、`StringUtils`、`ExceptionUtils`、`IterableUtils`、`Utils`）、`io.microsphere.reflect`（`ConstructorUtils`、`MethodUtils`、`TypeUtils`）、`io.microsphere.convert`（`Converter.convertIfPossible`）、`io.microsphere.constants`（`SymbolConstants`）、`io.microsphere.lang.function`（`ThrowableAction`）、`io.microsphere.annotation.Nonnull` / `Nullable`（属于独立构件） |
| **外部依赖** | 无 —— 纯 JDK。整个 reactor 中**没有**任何为这个包引入的 Jackson、Gson、`org.json`、`jakarta.json` 或 `javax.json` 依赖；`microsphere-java-core/pom.xml:22-74` 只列出了 `microsphere-java-annotations`、两个可选日志后端以及 test 作用域的构件。 |
| **被谁使用** | `io.microsphere.metadata`（core）、`microsphere-lang-model`、`microsphere-annotation-processor`。已在开发者页面的 §1 中核实。 |

本页面所有 `file:line` 引用都相对于 `microsphere-java-core/src/main/java/io/microsphere/`（主源码）
或 `microsphere-java-core/src/test/java/io/microsphere/`（测试），且读取自 `release` worktree。
首次提及之后会简写为 `json/Foo.java:40-55`。

---

## 1. 这个包是做什么的

**当你需要一个体积很小、零依赖的 JSON 对象模型，而所在库又无法承受引入第三方 JSON 栈的代价时，就应该使用
`io.microsphere.json`。** 这个包是 Android `org.json` 的内部 fork（`json/package-info.java:19`），所以它的形状就是
经典的 `JSONObject` / `JSONArray` / `JSONTokener` / `JSONStringer` / `JSONException` 那一套，再加上一个原创工具类
`JSONUtils`，负责把 JSON 桥接到 Java 集合与 JavaBean。`microsphere-java-core` 中所有其他产生或读取 JSON 的代码
都使用这些类型 —— `metadata` 用 `JSONUtils.append` 写出配置属性文档，用 `JSONUtils.readValues` 读回来
（`metadata/DefaultConfigurationPropertyGenerator.java:29-30`、`metadata/DefaultConfigurationPropertyReader.java:29,43`）。

**当你要解析的是*一串值*而不是单个文档时，请使用 `JSONTokener`** —— 它同时是词法分析器和递归下降语法分析器，
`nextValue()` 会返回下一个出现的 JSON 类型（`json/JSONTokener.java:102-122`）。

**当你希望 JSON 这一侧完全消失、只留下 Java 类型时，请使用 `JSONUtils`** ——
`readValue(json, Person.class)`、`readValues(json, List.class, Data.class)`、`readArray(json, String.class)`、
`readValueAsMap(jsonObject)`、`writeValueAsString(anyObject)`、`writeBeanAsString(bean)`
（`json/JSONUtils.java:884`、`:1041`、`:1172`、`:948`、`:1235`、`:1273`）。

**不要用它的场景：**

* **你需要一个生产级的 JSON 栈。** 这里没有流式解析器、没有 `JsonPointer`、没有 schema、没有 `BigDecimal` 精度保持、
  没有可配置的 `Mapper`，除了 `readValues(JSONArray, Type)`（`json/JSONUtils.java:1074`）之外也没有基于类型令牌
  （type-token）的泛型绑定 API。解析器就是 Android 那个，连同 Android 的全部宽松行为。
* **你需要严格符合 RFC 的读取器。** `JSONTokener` 是**故意**接受注释、`#` 注释、单引号、`=`/`=>` 作为键值分隔符、
  `;` 作为元素分隔符、`0x`/`0` 整数前缀，以及表示 `null` 的悬空分隔符的
  （`json/JSONTokener.java:44-62`）。解析成功并不能证明输入是合法 JSON。
* **你需要 `null` 只有一种含义。** 该模型携带两种互不兼容的 null —— Java 的 `null` 引用和 `JSONObject.NULL`
  哨兵对象（`json/JSONObject.java:101-114`，以及 `json/JSONArray.java:49-52` 处的警告）。见 §10。
* **你需要线程安全。** 包中没有任何类型可以共享；其中三个类明确说明了这一点
  （`json/JSONTokener.java:64-68`、`json/JSONArray.java:71-74`、`json/JSONStringer.java:64-68`），而 `JSONObject`
  对此**只字未提**。
* **你想要的是 `java.util.Map` 或 `java.util.List`。** `JSONObject` 不实现任何接口（`json/JSONObject.java:83`），
  `JSONArray` 同样不实现（`json/JSONArray.java:76`），两者也都不是 `Serializable`。没有 `entrySet()`，没有值上的
  `Iterator`，也没有 `List` 视图。请显式使用 `JSONUtils.readValueAsMap` 转换（`json/JSONUtils.java:948-957`）。

> [!IMPORTANT]
> **这个包是本库对"静默降级"错误策略的、已被文档记录的例外。** 在别处 `microsphere-java` 会返回中性值或干脆空操作；
> 而在这里，解析以及带类型的*取值方法*会抛出受检的 `JSONException`（`json/JSONException.java:41-46`）。
> 精确的分界线见 §3.3 —— 而四条读/写路径之间本身并不一致，这恰恰是关于这个包最需要注意的一件事。

---

## 2. 依赖坐标

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- 最新已发布的 tag；0.3.20-SNAPSHOT 是同一分支线上的开发版本 -->
</dependency>
```

针对 JSON 没有任何需要额外添加的依赖。`io.microsphere.json` 就打包在 `microsphere-java-core` 里，
不会拖入任何传递性的 JSON 构件。

---

## 3. 版本信息与兼容区间

### 3.1 事实清单

| 事实 | 值 | 证据（`release`） |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | `pom.xml:12` |
| `artifactId` | `microsphere-java-core` | 模块 `pom.xml:14` |
| 版本属性 | `${revision}`，会被 flatten | `pom.xml:14`、`:15` |
| `release` 上 `${revision}` 的值 | `0.3.20-SNAPSHOT` | `pom.xml:54` |
| 最新已发布 tag | `0.3.20` | 分组索引中的发布线表格（`docs/current/user-guide/microsphere-java-core/README.md:70`） |
| 构建父 POM | `microsphere-build:0.3.16` | `pom.xml:6-10` |
| Java 级别 | reactor 中任何位置都**没有** `maven.compiler.*` / `<release>` 属性；字节码版本继承自外部父 POM `microsphere-build:0.3.16` | CI 矩阵 `java: [ '8', '11', '17', '21', '25' ]`，`.github/workflows/maven-build.yml:25`，`distribution: 'temurin'` `:33` |
| Spring Framework，JDK 17+ | `7.0.9` | `microsphere-java-parent/pom.xml:23` |
| Spring Framework，JDK `[1.8,17)` | `5.3.39`，通过 profile `java8-16`，激活条件 `<jdk>[1.8,17)</jdk>` | `microsphere-java-parent/pom.xml:54-62`，激活条件在 `:57` |
| 导入的 BOM | `microsphere-all-bom:0.3.13`（`:22`、`:33-36`）、`spring-framework-bom`（`:40-46`） | `microsphere-java-parent/pom.xml` |
| 原创类型的 `@since` | `1.0.0` | `json/package-info.java:22`、`json/JSONUtils.java:111` |
| fork 来的类型的 `@since` | **不存在** —— `JSONObject`、`JSONArray`、`JSONTokener`、`JSONStringer`、`JSONException`、`JSON` 完全没有 `@author`/`@since` | 对本包的 grep：只有两个 `@since` 标签 |
| 源码中引用的语法规范 | RFC 4627 | `json/JSONTokener.java:29` |
| 源码中引用的转义规范 | RFC 7159 | `json/JSONUtils.java:121-129` |

### 3.2 兼容区间

整包在 CI 矩阵列出的 JDK（8、11、17、21、25）上都能编译和运行，不使用任何 JDK 内部 API，没有 `sun.*`，
没有 `Unsafe`，没有 JPMS `module-info`（整个仓库里都不存在）。包内没有任何代码读取系统属性或与 Spring 交互，
因此 `java8-16` profile 在这里完全不相关。

API 表面就是 Android `org.json` 的表面，上游是稳定的；请把它当作**只增不改**来看待 ——
你的代码不应依赖包私有的辅助物（`json/JSON.java:25` 是包私有的，`JSONStringer.Scope` 在
`json/JSONStringer.java:81-118` 是包私有的嵌套枚举）。

### 3.3 抛异常还是给默认值：界限到底在哪里

这是必须记住的表格。"中性值"指 `null`、某个默认值，或原样返回输入。

| 路径 | 在格式错误 / 键缺失时的行为 | 证据 |
|---|---|---|
| `new JSONObject(String)` / `new JSONArray(String)` / `JSONTokener.nextValue()` | **抛受检 `JSONException`** | `json/JSONObject.java:178-180`、`json/JSONArray.java:140-142`、`json/JSONTokener.java:102-122`；`json/JSONObjectTest.java:105-107`、`json/JSONTokenerTest.java:111-118` |
| `JSONObject.get(name)` / `JSONArray.get(index)` 以及每一个 `getX` | **抛受检 `JSONException`** | `json/JSONObject.java:393-399`、`json/JSONArray.java:333-343`；`json/JSONObjectTest.java:218-220` |
| 每一个 `optX`（`opt`、`optString`、`optInt`、`optJSONObject`，……） | **永不抛异常** —— 返回 `null`、`""`、`0`、`0L`、`false`、`NaN` 或你传入的 `fallback` | `json/JSONObject.java:407-409`、`:436-452`、`:479-495`、`:521-537`、`:566-584`、`:609-625`、`:652-655`、`:682-685`；`json/JSONObjectTest.java:307-339` |
| `JSONObject.put(name, value)` | `name` 为 `null` 时抛 `JSONException`，`NaN`/无穷数时也抛；值为 `null` 时**删除该键** | `json/JSONObject.java:274-285`、`:340-350`；`json/JSONObjectTest.java:174-176`、`:537-541` |
| `JSONArray.put(double)` / `put(int, Object)` | `NaN`/无穷数时抛 `JSONException` | `json/JSONArray.java:189-192`、`:299-310`；`json/JSONArrayTest.java:450-461` |
| `JSONArray.put(Object)` | **完全没有检查** —— 源码自己承认了这一点 | `json/JSONArray.java:225-228`，在 `json/JSONObject.java:323-324` 处被承认 |
| `toString()` / `toString(int)` | 抛出**非受检**的 `RuntimeException`，包裹着 `JSONException` | `json/JSONObject.java:762-766`、`json/JSONArray.java:713-717`、`lang/function/ThrowableAction.java:79-81` |
| `JSONUtils.jsonObject(String)` / `jsonArray(String)` | 抛出**非受检**的 `IllegalArgumentException`（cause 为原来的 `JSONException`） | `json/JSONUtils.java:803-812`、`:843-852` |
| `JSONUtils.writeValueAsString(Object)` | 当对象不可包装时返回 **`null`** | `json/JSONUtils.java:1234-1241`；`json/JSONUtilsTest.java:678-687` |
| `JSONObject.wrap(Object)` | 任何失败都返回 **`null`**，异常被吞掉 | `json/JSONObject.java:867-885`；`json/JSONObjectTest.java:518-522` |
| `JSONUtils.escape(String)` | 原样返回输入；**`null` 进 → `null` 出** | `json/JSONUtils.java:1342-1386`；`json/JSONUtilsTest.java:760-762` |

> [!WARNING]
> 一个包中并存着四套失败词汇：受检 `JSONException`（解析与 `getX`）、非受检 `RuntimeException`（`toString`）、
> 非受检 `IllegalArgumentException`（`JSONUtils.jsonObject`/`jsonArray`）、以及静默 `null`
> （`wrap`、`writeValueAsString`、`readValues` 遇到不支持的目标类型）。如果你只捕获 `JSONException`，
> 后面三种会击穿你。§10 的第 5、8、9、11 条指出了具体的非受检逃逸口。

---

## 4. 类型清单

| # | 类型 | 可见性 | 职责 | 是否 fork 自 AOSP？ |
|---|---|---|---|---|
| 1 | `JSONObject` | `public` | 保序的 name→value 映射；`NULL` 哨兵；`wrap`、`quote`、`numberToString` | 是（`json/JSONObject.java:2`） |
| 2 | `JSONArray` | `public` | 紧凑的索引序列；与 `JSONObject` 相同的取值/强制转换规则 | 是（`json/JSONArray.java:2`） |
| 3 | `JSONTokener` | `public` | 作用于单个 `String` 的词法器 + 递归下降解析器 | 是（`json/JSONTokener.java:2`） |
| 4 | `JSONStringer` | `public` | `JSONObject`/`JSONArray` 唯一的写出器；作用域栈 + 缩进 | 是（`json/JSONStringer.java:2`） |
| 5 | `JSONException` | `public` | 本包唯一的受检异常 | 是（`json/JSONException.java:2`） |
| 6 | `JSON` | **package-private** | 强制转换辅助（`toBoolean`/`toDouble`/`toInteger`/`toLong`/`toString`）、`checkDouble`、`typeMismatch` 工厂 | 是（`json/JSON.java:2`、`:25`） |
| 7 | `JSONUtils` | `public abstract … implements Utils` | Microsphere 原创的桥接层：`StringBuilder` JSON 写出器 + JSON↔集合/bean 读取器 | 否 —— ASF 许可证头，`@since 1.0.0`（`json/JSONUtils.java:2`、`:109-113`） |
| 8 | `package-info.java` | — | 声明了这个 fork："Fork the source code from org.json" | —（`json/package-info.java:19-22`） |

---

## 5. 把文本解析为 `JSONObject` / `JSONArray`

### 5.1 单参数构造函数

```java
JSONObject object = new JSONObject("{\"name\":\"Mercy\",\"age\":18}");
assertTrue("Mercy".equals(object.getString("name")));   // JSONException 是受检异常 —— 必须声明或捕获
```

`json/JSONObjectTest.java:110-114` 的压缩版本，该测试同时证明了宽松的 `=` 分隔符：

```java
// json/JSONObjectTest.java:110-113
JSONObject jsonObject = new JSONObject("{ \"name\" : \"Mercy\" , \"age\" = 18 }");
assertEquals("Mercy", jsonObject.getString("name"));
assertEquals(18, jsonObject.getInt("age"));
```

`new JSONArray(String)` 是其镜像（`json/JSONArray.java:140-142`），在 `json/JSONArrayTest.java:98-104` 得到证明：

```java
// json/JSONArrayTest.java:99-103
JSONArray jsonArray = new JSONArray("[ 1 ,2 , 3]");
assertEquals(3, jsonArray.length());
assertEquals(1, jsonArray.getInt(0));
```

两个构造函数都委托给 `JSONTokener`，然后*窃取*解析出来的内部 map/list
（`json/JSONObject.java:158-169`、`json/JSONArray.java:120-131`）。如果字符串中的第一个值不是正确的 JSON 类型，
你会从 `JSON.typeMismatch` 得到 `JSONException`（`json/JSON.java:117-123`）——
在 `json/JSONArrayTest.java:92-95`（把 `{}` 喂给 `new JSONArray(tokener)`）与 `json/JSONObjectTest.java:105-107`
（把 `Hello` 喂给 `new JSONObject(tokener)`）处有断言。

### 5.2 直接使用 `JSONTokener`

当输入包含多个值，或者你想用词法器自己的方法时，直接使用 tokener：

```java
// json/JSONTokenerTest.java:59-65 —— 该资源是 microsphere-java-core/src/test/resources/test/json/data.json
JSONTokener jsonTokener = new JSONTokener(getResourceAsString("test/json/data.json"));
JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
assertEquals("Mercy", jsonObject.getString("name"));
```

`nextValue()` 可能返回 `JSONObject`、`JSONArray`、`String`、`Boolean`、`Integer`、`Long`、`Double`、
`JSONObject.NULL` 或未加引号的 `String` —— 见 `json/JSONTokener.java:95-101` 处的 javadoc 与 `:102-122` 处的分派。
字符串可以用双引号或单引号，裸词也被接受为字面量：

```java
// json/JSONTokenerTest.java:76-88
assertEquals("Hello", new JSONTokener("\"Hello\"").nextValue());
assertEquals("hello", new JSONTokener("'hello'").nextValue());
assertEquals("Hello", new JSONTokener("Hello").nextValue());   // 未加引号的字面量 → String
```

前导的 UTF-8 BOM 会在构造函数中被剥离一次（`json/JSONTokener.java:87-93`）；
`json/JSONTokenerTest.java:43-46` 构造了 `new JSONTokener("\ufeffHello")`。

`readObject()` 与 `readArray()` 是 public 的，且假定左括号已经被消费掉了 —— 这就是直接调用它们时看起来奇怪的
原因（`json/JSONTokener.java:360-413`、`:415-461`）。测试就是这样调用的：

```java
// json/JSONTokenerTest.java:92-98 —— 注意：没有前导的 "{"
JSONObject empty = new JSONTokener("}").readObject();                  // length 0
JSONObject one   = new JSONTokener("\"name\" : \"Mercy\" }").readObject();
assertEquals("Mercy", one.getString("name"));
```

### 5.3 `JSONUtils` 提供的便捷解析入口

如果你不想处理受检异常：

```java
// json/JSONUtilsTest.java:508-512
JSONObject jsonObject = JSONUtils.jsonObject("{\"name\":\"John\", \"age\":30}");
assertEquals("John", jsonObject.getString("name"));
assertEquals(30, jsonObject.getInt("age"));

// json/JSONUtilsTest.java:521-526
JSONArray jsonArray = JSONUtils.jsonArray("[1,2,3]");
assertEquals(1, jsonArray.getInt(0));
```

格式错误的输入会被重新包装为非受检的 `IllegalArgumentException`，其 cause 是原来的 `JSONException`
（`json/JSONUtils.java:803-812`、`:843-852`，经由 `util/ExceptionUtils.java:97-104` 与 `:304-308`）：

```java
// json/JSONUtilsTest.java:516-518 与 :529-531
assertThrows(IllegalArgumentException.class, () -> jsonObject("{name}"));
assertThrows(IllegalArgumentException.class, () -> jsonArray("[1,2,3"));
```

---

## 6. 构造对象与数组

`put` 返回 `this`，因此构造可以链式流畅书写。每个 `put` 重载也都声明 `throws JSONException`
（`json/JSONObject.java:218-260`），连 `boolean`/`int`/`long` 那几个也不例外，因为它们都要经过 `checkName`
（`:345-350`）。

```java
// json/JSONObjectTest.java:123-136
long time = currentTimeMillis();
jsonObject.put("name", "Mercy")
          .put("age", 18)
          .put("workingTime", time)
          .put("sex", true)
          .put("salary", 1000.0)
          .put("salary", null)                      // 删除该映射，见 §10 第 1 条
          .put("salary", Double.valueOf(1000.0));
assertEquals("Mercy", jsonObject.get("name"));
assertEquals(1000.0, jsonObject.getDouble("salary"));
assertTrue(jsonObject.getBoolean("sex"));
```

`putOpt` 是 null 容忍的变体 —— 任一参数为 `null` 时它静默空操作
（`json/JSONObject.java:296-301`，在 `json/JSONObjectTest.java:140-145` 得到证明）。

`accumulate` 让一个名字逐个累积成一个数组；同一个名字的第二个值会把第一个值提升进一个 `JSONArray`
（`json/JSONObject.java:317-338`）：

```java
// json/JSONObjectTest.java:155-170
jsonObject.accumulate("alias", "mercyblitz");
jsonObject.accumulate("alias", "mz");
assertEquals("mercyblitz", jsonObject.getJSONArray("alias").get(0));
assertEquals("mz", jsonObject.getJSONArray("alias").get(1));
```

数组也支持按下标写入，中间的空洞用 null 填充（`json/JSONArray.java:299-310`）：

```java
// json/JSONArrayTest.java:143-148
jsonArray.put(0, true).put(1, 1).put(2, 2L).put(3, 3.0).put(4, "4");
assertEquals(3.0, jsonArray.get(3));
```

`remove` 返回被移出的值或 `null`，且永不抛异常（`json/JSONObject.java:359-361`、`json/JSONArray.java:366-371`；
测试在 `json/JSONObjectTest.java:179-184`、`json/JSONArrayTest.java:187-198`）。`isNull` 与 `has` 是 Membership
探测的两个入口（`json/JSONObject.java:370-373`、`:382-384`；`json/JSONArray.java:319-322`；
测试 `json/JSONObjectTest.java:187-203`、`json/JSONArrayTest.java:152-158`）。

---

## 7. 读取值：强制转换与默认值

### 7.1 取值方法矩阵

`JSONObject` 与 `JSONArray` 暴露完全相同的取值方法族；只有键类型不同（`String` 名字 vs `int` 下标）。
强制转换本身实现在包私有的 `JSON` 辅助类中。

| 取值方法 | 接受的输入 | 失败时 | `JSONObject` 行号 | `JSONArray` 行号 |
|---|---|---|---|---|
| `get` / `opt` | 任意对象 | `get` 抛 `JSONException`，`opt` 返回 `null` | `:393-399` / `:407-409` | `:333-343` / `:352-357` |
| `getBoolean` / `optBoolean` | `Boolean`，或字符串 `"true"`/`"false"`（忽略大小写） | 抛异常 / `false` 或 `fallback` | `:420-427` / `:436-452` | `:382-389` / `:398-414` |
| `getDouble` / `optDouble` | 任意 `Number`，或可解析的 `String` | 抛异常 / `NaN` 或 `fallback` | `:463-470` / `:479-495` | `:425-432` / `:441-457` |
| `getInt` / `optInt` | 任意 `Number`（会窄化），或可解析的 `String` | 抛异常 / `0` 或 `fallback` | `:505-512` / `:521-537` | `:468-475` / `:484-500` |
| `getLong` / `optLong` | 任意 `Number`（会窄化），或可解析的 `String` | 抛异常 / `0L` 或 `fallback` | `:548-555` / `:566-584` | `:511-518` / `:527-543` |
| `getString` / `optString` | `String`，或**任何东西** —— 非 `null` 的值都走 `String.valueOf` | 抛异常 / `""` 或 `fallback` | `:593-600` / `:609-625` | `:552-559` / `:568-584` |
| `getJSONArray` / `optJSONArray` | 只接受 `JSONArray` | 抛异常 / `null` | `:636-643` / `:652-655` | `:595-602` / `:611-614` |
| `getJSONObject` / `optJSONObject` | 只接受 `JSONObject` | 抛异常 / `null` | `:666-673` / `:682-685` | `:625-632` / `:641-644` |

上面每一个格子都在测试套件中被断言过 —— 例如无 fallback 的 `opt` 形式在 `json/JSONObjectTest.java:307-339`，
显式 fallback 形式在 `:544-577`；数组侧在 `json/JSONArrayTest.java:463-509`。

### 7.2 跨类型的双向强制转换

数字不会保持你放进去时的类型。`JSON.toDouble` / `toInteger` / `toLong` 接受*任意* `Number` 与任意可解析的
`String`（`json/JSON.java:50-96`），于是：

```java
// json/JSONArrayTest.java:226-232、255-261、285-291
jsonArray.put(1.0);  jsonArray.put("2.0");  jsonArray.put(3);
assertEquals(2.0, jsonArray.getDouble(1));   // String → double
assertEquals(2,   jsonArray.getInt(1));      // String → double → int
assertEquals(3.0, jsonArray.getDouble(2));   // int → double
assertEquals(3L,  jsonArray.getLong(2));     // double → long（有损；javadoc 在 json/JSONObject.java:540-544 承认了这点）
```

而非数字字符串则直接失败：

```java
// json/JSONObjectTest.java:229-233、242-246
jsonObject.put("value", "Hello");
assertThrows(JSONException.class, () -> jsonObject.getDouble("value"));
assertThrows(JSONException.class, () -> jsonObject.getInt("value"));
```

`getString` 是唯一一个把*所有东西*都强制转换的取值方法，因为 `JSON.toString` 对任何非 `String` 都会落到
`String.valueOf(value)`（`json/JSON.java:98-106`）：

```java
// json/JSONTest.java:99-103
assertEquals("1", JSON.toString("1"));
assertEquals("1", JSON.toString(Integer.valueOf(1)));   // number → 其文本
assertNull(JSON.toString(null));
```

> [!NOTE]
> 美化输出的参数名是 **`indentSpaces`**，不是 `indentFactor` —— 见 `json/JSONObject.java:758-762` 与
> `json/JSONArray.java:706-713`。它是**每个嵌套层级所用的空格字符个数**，没有制表符模式，也没有键排序选项。

---

## 8. 序列化与美化输出

### 8.1 `toString()` 与 `toString(int indentSpaces)`

```java
// json/JSONObjectTest.java:419-420
assertEquals("{\"name\":\"Mercy\",\"age\":18,\"workingTime\":" + time + "}", jsonObject.toString());
assertEquals("{\n \"name\": \"Mercy\",\n \"age\": 18,\n \"workingTime\": " + time + "\n}",
             jsonObject.toString(1));

// json/JSONArrayTest.java:414-415
assertEquals("[1,2,3]", jsonArray.toString());
assertEquals("[\n 1,\n 2,\n 3\n]", jsonArray.toString(1));
```

注意 `indentSpaces = 1` 时的精确形状：每个成员前是一个换行加**一个**空格，键与值之间是 `": "`
（`json/JSONStringer.java:388-397`、`:451-453`）。闭合括号永远不会被缩进。缩进字符串在每个实例上只构建一次
（`json/JSONStringer.java:136-144`），然后按栈深度重复拼接（`:394-396`）。

`toString(0)` —— 因而 `toString()` 也是 —— 是紧凑形式；`toString(负数)` 同样是紧凑形式，因为只要参数 `<= 0`，
`indent` 字段就保持 `null`（`json/JSONStringer.java:137-143`）。

### 8.2 直接使用 `JSONStringer`，以及 `quote`、`numberToString` 与 `join`

`JSONStringer` 是 public 的，但基本上属于实现细节（`json/JSONStringer.java:33-35`）。它的值分派值得细读，
因为它定义了每个非标量值会变成什么：

```java
// json/JSONStringerTest.java:136-161 —— 同样九个值，分别带缩进与不带缩进
"[null,true,null,1,2,3,4,\"5\",\"java.lang.String\"]"
```

`Float 4.0f` 输出为 `4`，`Double 2.0` 输出为 `2`（`json/JSONObject.java:816-819`）；`StringBuilder` 输出为带引号的
`toString()`（`json/JSONStringer.java:282-284`）；`Class` 输出为其类型名（`:280-281`）。

其余三个 public 编码器：

```java
// json/JSONObjectTest.java:437-442
assertEquals("0",   numberToString(Double.valueOf(0.0)));   // 整数值的 double → 不输出 ".0"
assertEquals("-0",  numberToString(NEGATIVE_ZERO));         // json/JSONObject.java:811-814
assertEquals("1.23", numberToString(Double.valueOf(1.23)));

// json/JSONObjectTest.java:457-464
assertEquals("\"Mercy\"", quote("Mercy"));
assertEquals("\"\"", quote(null));                          // json/JSONObject.java:832-834

// json/JSONArrayTest.java:402-407
jsonArray.put(1).put(2).put(3);
assertEquals("1,2,3", jsonArray.join(","));                 // json/JSONArray.java:679-690
```

`quote` 与 `join` 都走 `JSONStringer` 的 `Scope.NULL` 无括号模式
（`json/JSONObject.java:835-841`、`json/JSONArray.java:680-689`），因此它们输出裸值，不带 `{}`/`[]`。

### 8.3 `JSONUtils.append` —— 第二个、手写的写出器

`JSONUtils` 把 JSON 直接写进你提供的 `StringBuilder`，中间不经过任何对象模型。它有 34 个 `append` 重载 ——
每种基本类型、包装类型、`String[]`、基本类型数组、对象数组以及 `Object` 各一个
（`json/JSONUtils.java:146-467`）—— 外加 `appendName`（`:500-508`）与 `appendValue`（`:469-486`）。
这正是 `metadata` 使用的路径（`metadata/DefaultConfigurationPropertyGenerator.java:88-117`）。

```java
// json/JSONUtilsTest.java:121-124、230-232、205-208
StringBuilder builder = new StringBuilder();
append(builder, "name", 1);              // "name":1
append(builder, "name", "a");            // "name":"a"     （经由 JSONUtils.escape 转义）
append(builder, "name", String.class);   // "name":"java.lang.String"
```

`appendValue` 按运行时类型分派：数组 → `[...]`，`Map` → `{...}`，`Iterable` → `[...]`，
`Character`/`CharSequence`/`Enum` → 带引号，`Type` → 带引号的类型名，其余全部走 `StringBuilder.append(value)`
（`json/JSONUtils.java:469-486`，由 `json/JSONUtilsTest.java:312-369` 证明）。

> [!WARNING]
> `JSONUtils.append` 与 `JSONObject.toString()` 是**两个不同的编码器，有不同的转义规则和不同的校验规则**。
> `append` 不做有限性检查，也不转义名字；`toString()` 两者都做。不要在同一个文档中混用它们还期待字节一致。
> §10 的第 4、6、13 条给出了具体的差异点。

---

## 9. JSON 与 Java 集合、bean 之间的转换

这就是 `JSONUtils`，也是 `io.microsphere.json` 不只是一个 fork 的原因。

### 9.1 Java → JSON 字符串

```java
// json/JSONUtilsTest.java:663-675
String json = writeValueAsString(ofMap("key1", "value1", "key2", 2));
assertEquals(new JSONObject(map).toString(), json);

Object array = ofArray("value1", "value2");
assertEquals(new JSONArray(array).toString(), writeValueAsString(array));
```

`writeValueAsString` 委托给 `JSONObject.wrap`，当 wrap 的结果不是 JSON 容器时返回 `null`
（`json/JSONUtils.java:1234-1241`）—— 所以 `writeValueAsString("Hello")` 是 `null`，而不是 `"\"Hello\""`
（`json/JSONUtilsTest.java:678-681`）。

`wrap` 的阶梯就是整个转换契约（`json/JSONObject.java:859-886`），测试套件钉住了每一级：

```java
// json/JSONObjectTest.java:467-515（节选）
assertEquals(NULL, wrap(null));                                          // :469
assertEquals(new JSONArray(ofList(values)), wrap(ofList(values)));         // :479 Iterable
assertEquals(new JSONArray(ofArray(values)), wrap(ofArray(values)));       // :481 array
assertEquals(new JSONArray(ofEnumeration(values)), wrap(ofEnumeration(values))); // :483
assertEquals(new JSONObject(ofMap("name", "Mercy")), wrap(ofMap("name", "Mercy"))); // :485
assertEquals("java.lang.String", wrap(String.class));                     // :507
assertEquals("NEW", wrap(NEW));                                           // :509 enum → toString()
assertEquals(date.toString(), wrap(date));                                // :512 任何 java.* → toString()
assertEquals(new JSONObject(), wrap(this));                               // :514 JavaBean → 属性
```

bean 会经过 `BeanUtils.resolvePropertiesAsMap`（`json/JSONObject.java:881`），`writeBeanAsString` 是这条路的直接
入口（`json/JSONUtils.java:1272-1277`，在 `json/JSONUtilsTest.java:689-694` 得到证明）。

### 9.2 JSON → 集合与数组

```java
// json/JSONUtilsTest.java:594-599（List）
List<Data> dataList = ofList(createData());
String json = writeValueAsString(dataList);
List<Data> copy = readValues(json, List.class, Data.class);
assertEquals(dataList, copy);
```

`readValues(String, Class, Class)` 恰好支持六种目标形状，其他一律返回 `null`
（`json/JSONUtils.java:1125-1141`）：数组（`isArray`）、`List`、`Set`、`Queue`、`Enumeration`、`Iterable`
（其余情况）。`null` 兜底在 `json/JSONUtilsTest.java:639-645` 被断言：

```java
Object dataCopy = readValues(json, Object.class, Data.class);   // 不支持 → null，不抛异常
assertNull(dataCopy);
```

其他入口：

```java
// json/JSONUtilsTest.java:647-654（从 JSON 字符串得到类型化数组）
Data[] dataArrayCopy = readArray(json, Data.class);
assertArrayEquals(dataArray, dataArrayCopy);

// json/JSONUtilsTest.java:534-548（JSONObject → Map，尽可能转换值）
Map<String, Object> readValue = readValue(json, Map.class);
assertEquals(data.getName(), readValue.get("name"));
assertArrayEquals(data.getNames(), (String[]) readValue.get("names"));
```

`readValue(json, Map.class)` 之所以能走到 `readValueAsMap`，靠的是 `isAssignableFrom(Map.class, targetType)`
分支（`json/JSONUtils.java:917-922`、`:948-957`）。还有一个感知 `Type` 的重载 ——
`readValues(JSONArray, Type)`（`json/JSONUtils.java:1073-1094`）—— 当目标未被参数化时，它借助
`determineElementClass`（`:1302-1322`）从内容推断元素类型。

### 9.3 JSON → JavaBean

```java
// json/JSONUtilsTest.java:551-557
Data data = createData();
String json = writeValueAsString(data);
Data readValue = readValue(json, Data.class);   // Data 具备无参构造 + setter
assertEquals(data, readValue);
```

以及多值 bean，这才是有意思的情形，因为每个属性会落进不同的集合类型：

```java
// json/JSONUtilsTest.java:560-571
md.setStringList(ofList("a", "b", "c"));
md.setIntegerSet(ofSet(1, 2, 3));
md.setDataQueue(ofQueue(createData()));
md.setClassEnumeration(ofEnumeration(String.class, Integer.class));
md.setObjects(ofArray("Hello", 123, true, 45.67));
MultipleValueData readValue = readValueAsBean(jsonObject(writeValueAsString(md)), MultipleValueData.class);
assertEquals(md, readValue);
```

其机制是：先用 `ConstructorUtils.newInstance(true, beanClass)` 实例化，然后为每个 JSON 键找到写方法，并把值
转换为目标属性的*泛型*参数类型（`json/JSONUtils.java:988-1007`）。任何匹配不上的东西都会被安静跳过 ——
见 `json/JSONUtilsTest.java:573-581`，那里一个值为 `null` 的键和一个无关的 `"test-name"` 键都消失了。

> [!TIP]
> 让 `char` 类型的 bean 属性经 JSON 往返会丢失其类型：`Character` 是包装类型，所以 `wrap` 原样返回它
> （`json/JSONObject.java:864`），写出器给它加引号（`json/JSONStringer.java:282-284`），回来时它就变成了一个字符的
> `String`。测试套件在 `json/JSONUtilsTest.java:546` 精确记录了这一点
> （用 `String.valueOf(data.getSex())` 与读回的值比较）。同样，在 `Map` 路径中 `short`/`byte` 会以 `Integer` 回来
> （`:544-545`），`float` 会以 `Double` 回来（`:542`）。

---

## 10. 常见陷阱与已知限制

1. **有两种 null，它们不能互换。** `JSONObject.NULL` 是一个哨兵，它的 `equals` **故意**对 `null` 返回 `true`
   （`json/JSONObject.java:101-114`，在 `json/JSONObjectTest.java:525-529` 得到断言），而 Java 的 `null` 引用并
   *不是*合法的 map 值。后果是：`put(name, null)` 会**删除**该键（`json/JSONObject.java:275-278`），所以
   `new JSONObject().put("a", null)` 序列化为 `{}`，而 `new JSONObject("{\"a\":null}")` 序列化为 `{"a":null}`，
   并且 `has("a")` 为 `true`（`:382-384`），但 `isNull("a")` 也为 `true`（`:370-373`、
   `json/JSONObjectTest.java:187-195`）。`JSONArray.get(index)` 遇到裸 `null` 元素会抛异常，遇到 `NULL` 则成功
   （`json/JSONArray.java:333-343`，在 `:49-52` 有警告，在 `json/JSONArrayTest.java:167-170` 有断言）。
   如果你想要一个统一覆盖两者的谓词，请使用 `JSONUtils.isNull(Object)`（`json/JSONUtils.java:697-699`）。

2. **`getX` 对于它报告的是*哪一种*失败并不统一。** `getInt`、`getLong`、`getDouble`、`getJSONArray` 和
   `getJSONObject` 全都从 `get(name)` 出发，后者对缺失键抛 `"No value for <name>"`
   （`json/JSONObject.java:393-399`，被 `:463`、`:505`、`:548`、`:636`、`:666` 使用）。而 `getBoolean` 与
   `getString` 从 `opt(name)` 出发（`:421`、`:594`），所以同一个缺失键会产生来自 `JSON.typeMismatch` 的
   `"Value at <name> is null."`（`json/JSON.java:108-115`）。如果你靠匹配消息来分支，你会漏掉一半情形。

3. **`JSONObject.getString` 报告了错误的目标类型。** 在 `json/JSONObject.java:597` 它调用的是
   `typeMismatch(name, object, "long")` —— 一个复制粘贴缺陷；它的孪生方法 `json/JSONArray.java:556` 正确传入了
   `"String"`。抛出的异常类型是对的，只是消息说成"cannot be converted to long"。

4. **`NaN` / `Infinity` 在一条写入路径上被拒绝，在另一条上被原样写出。** 模型路径拒绝它们：`JSON.checkDouble`
   （`json/JSON.java:27-32`）会被 `put(String,double)`（`json/JSONObject.java:232-234`）、
   `put(String,Object)` 中对任意 `Number` 的检查（`:279-283`）、`accumulate`（`:324-326`）、
   `JSONArray.put(double)`（`json/JSONArray.java:189-192`）以及 `put(int,Object)`（`:299-304`）调用；测试在
   `json/JSONObjectTest.java:537-541` 与 `json/JSONArrayTest.java:450-461`。`JSONUtils.append` 完全不做这些事 ——
   `append(builder, name, 1.0)` 直接走到 `StringBuilder.append(double)`（`json/JSONUtils.java:176-179`），
   而 `append(builder, name, (Object) Double.NaN)` 落到 `appendValue` 的 `else` 分支（`:483-485`）。
   两者都会产出 `NaN` / `Infinity`，这**不是合法 JSON**，且不抛任何异常。更糟的是，
   `JSON.toDouble` 因为调用 `Double.valueOf(String)`，会欣然*解析* `"NaN"`、`"Infinity"` 和 `"-Infinity"`
   （`json/JSON.java:50-64`）。

5. **`JSONArray.put(Object)` 没有任何防护，而失败会在之后以非受检异常的形式浮出。** `json/JSONArray.java:225-228`
   这个重载不做任何 `Number` 检查；`json/JSONObject.java:323-324` 甚至写了原因
   （"check in accumulate, since array.put(Object) doesn't do any checking"）。所以
   `new JSONArray().put((Object) NaN)` 会成功，而之后的 `toString()` 会抛出包裹着 `JSONException` 的
   `RuntimeException`（`json/JSONArray.java:713-717` → `lang/function/ThrowableAction.java:79-81`）。
   没有任何测试覆盖这个形状 —— `json/JSONArrayTest.java:450-461` 只测了带检查的 `put(double)` 与 `put(int,double)`
   重载。

6. **两个编码器，两套转义规则。** `JSONStringer.string()` 转义 `"`、`\`、**`/`**、六个具名转义以及所有
   `<= 0x1F`（`json/JSONStringer.java:338-386`；`json/JSONStringerTest.java:188-194` 钉住了
   `"\"\\\//\t\b\n\r\fa"` → `"\"\\\\\\/\\t\\b\\n\\r\\fa"` 以及 U+001F → `\u001f`）。`JSONUtils.escape()` 转义 `"`、
   `\` 和控制字符区间，**不**转义 `/`，但**会**转义 `U+2028`/`U+2029`（`json/JSONUtils.java:115-144`、
   `:1342-1386`；`json/JSONUtilsTest.java:806-815`）。因此 `JSONObject.toString()` 产出 `\/` 的地方
   `JSONUtils.append` 产出 `/`，反之 U+2028 处亦然。

7. **`JSONUtils` 从不转义属性名，也从不转义 `char` 值。** `appendName` 写的是
   `'"' + name + '"' + ':'`，毫无转义（`json/JSONUtils.java:500-508`），而
   `append(builder, name, 'a')` / `append(builder, name, Character.valueOf('a'))` 会把字符原样写出（`:181-186`、
   `:223-228`）。含 `"` 的名字或取值为 `"` 的 `char` 会静默地产出畸形 JSON。测试只使用了无害的名字
   （`json/JSONUtilsTest.java:372-384`、`:144-148`、`:192-196`）。

8. **畸形的 `\uXXXX` 转义抛的是 `NumberFormatException`，不是 `JSONException`。**
   `readEscapeCharacter` 用 `Integer.parseInt(hex, 16)` 解析四个十六进制字符且不捕获任何东西
   （`json/JSONTokener.java:254-260`）；javadoc 在 `:200` 与 `:248` 承认了这一点。因此 `"\uZZZZ"` 会从
   `catch (JSONException)` 逃逸出去。测试套件只覆盖了被截断的变体 `"u123"`，它命中的是*长度*检查，
   确实抛 `JSONException`（`json/JSONTokenerTest.java:223-227`）。

9. **两侧都没有嵌套深度限制，也没有环检测。** 解析器递归 `nextValue()` → `readObject()`/`readArray()` →
   `nextValue()`（`json/JSONTokener.java:102-122`、`:367-413`、`:423-461`），没有计数器；写出器递归
   `JSONStringer.value(Object)` → `JSONObject.writeTo(stringer)` → `stringer.value(...)`
   （`json/JSONStringer.java:266-271`、`json/JSONObject.java:768-774`）。因此深层嵌套的输入会以
   `StackOverflowError` 告终，那是一个 `catch (JSONException | RuntimeException)` 拦不住的 `Error`。
   `JSONStringer` 的类 javadoc 仍然声称"at most 20 levels of nesting"（`json/JSONStringer.java:61-62`），
   而紧接其下的字段注释说的恰恰相反 —— "Unlike the original implementation, this stack isn't limited to
   20 levels of nesting"（`:120-123`）。环也可以手工构造出来
   （`json/JSONArrayTest.java:338-341` 把一个数组放进了它自己），对这样的数组调用 `toString()` 就会栈溢出。

10. **数字永远不会变成 `BigDecimal` 或 `BigInteger`。** `readLiteral` 先试 `Integer`，再试 `Long`，再试 `Double`，
    最后退化为未加引号的 `String`（`json/JSONTokener.java:305-339`）；整包对 `BigDecimal`/`BigInteger` 的引用为
    零。`21474836470` 会解析为 `Long`（`json/JSONTokenerTest.java:241`），但任何超过 `Long.MAX_VALUE` 的值都会
    静默变成 `Double` 并丢失精度。`JSON.toInteger` 与 `toLong` 用 C 风格的强制转换窄化（`json/JSON.java:75`、
    `:91`），所以 `getInt("999999999999")` 返回 `Integer.MAX_VALUE` 而不是报错 ——
    `JSONTest.java:84` 已经展示了 `"1.0"` 变成 `1`。

11. **`jsonObject(null)` / `jsonArray(null)` 抛的是 `NullPointerException`，而不是文档承诺的
    `IllegalArgumentException`。** `new JSONTokener(null)` 接受这个 `null` 并存下来
    （`json/JSONTokener.java:87-93`，javadoc 在 `:84-85` 承认了这点）；第一次读取会在 `:501-503` 解引用它。
    由于 `JSONUtils.jsonObject` 只捕获 `JSONException`（`json/JSONUtils.java:806-810`），NPE 会不被包装地逃逸出去，
    `:801` 处的 `@throws IllegalArgumentException` 契约没有被满足。没有针对 `null` 输入的测试 ——
    `json/JSONTokenerTest.java:49-51` 用 `null` 构造了 tokener 却从不从中读取。

12. **`wrap` 与 `readValues` 会把失败吞成 `null`。** `JSONObject.wrap` 把整个分派包在
    `catch (Exception ignored) { }` 里（`json/JSONObject.java:867-885`），所以键为 `null` 的 map、getter 抛异常的
    bean、或者自毁 `equals` 的值，最后都会变成 `null` —— 在 `json/JSONObjectTest.java:518-522` 有断言，
    然后又被 `JSONUtils.writeValueAsString` 序列化掉（`json/JSONUtils.java:1235-1241`、
    `json/JSONUtilsTest.java:678-687`）。`readValues` 同样对不支持的目标类型返回 `null`（`:1125-1141`、
    `json/JSONUtilsTest.java:639-645`）。两者都不算 bug，但它们都意味着**一个 `null` 结果并不能告诉你为什么**。

13. **`JSONUtils` 的 null 处理与模型的恰好相反。** `appendValue` 的第一条语句就是 `value.getClass()`
    （`json/JSONUtils.java:469-470`），所以 `append(builder, "k", (Object) null)` 抛 `NullPointerException` ——
    任何包含一个 `null` 元素的 `Map` 或 `Iterable` 也一样，经由 `appendMap`/`appendIterable`（`:1388-1415`）。
    `determineElementClass` 同理，它会解引用每一个元素（`:1303-1310`）；`readValueAsMap`/`readValueAsBean` 也一样，
    它们在任何 null 检查之前就调用 `jsonObject.length()` / `getBeanMetadata(...)`（`:948-957`、`:988-990`）。
    `json/JSONUtilsTest.java:319-334` 中的测试对 map 这一情形**没有做任何断言** —— 那个测试根本没有断言。

---

## 11. 参见

* [配置属性元数据](configuration-metadata.md) —— `metadata` 是库内最大的消费者：
  `DefaultConfigurationPropertyGenerator` 用 `JSONUtils.append` 写 JSON
  （`metadata/DefaultConfigurationPropertyGenerator.java:29-30,88-117`），
  `ReflectiveConfigurationPropertyGenerator` 用 `writeBeanAsString`
  （`metadata/ReflectiveConfigurationPropertyGenerator.java:24,78`），
  `DefaultConfigurationPropertyReader` 用 `readValues(content, List.class, Map.class)` 读
  （`metadata/DefaultConfigurationPropertyReader.java:29,43`）。
* [反射、类型与 JavaBean](reflection-and-beans.md) —— `BeanUtils.resolvePropertiesAsMap` 让 `JSONObject.wrap`
  能够处理 JavaBean（`json/JSONObject.java:25,881`），而 `ConstructorUtils` / `MethodUtils` / `TypeUtils`
  驱动着 `readValueAsBean`（`json/JSONUtils.java:988-1007`）。
* [类型转换](type-conversion.md) —— `Converter.convertIfPossible` 是每一次 JSON→Java 标量转换的叶子节点
  （`json/JSONUtils.java:63,1427-1442`）。
* [集合与过滤器](collections-and-filters.md) —— `ListUtils.newArrayList` 与 `MapUtils.newLinkedHashMap`
  决定了 `JSONArray` 的存储方式与 `JSONObject` 的键顺序（`json/JSONArray.java:84`、`json/JSONObject.java:122`）。
* [工具类与常量](util-and-constants.md) —— `ClassUtils` 提供 `wrap` 用到的所有类型谓词
  （`json/JSONObject.java:39-45`），`SymbolConstants` 提供 `JSONUtils` 用到的标点（`json/JSONUtils.java:56-62`）。
* [序列化](serialization.md) —— 相邻的 `io.microsphere.io.serializer` 包，处理二进制的 `Object` ↔ `byte[]`；
  当你*不*想要文本格式时它是正确的选择。
* [开发者指南 — JSON](../../../developer-guide/microsphere-java-core/zh/json.md) —— tokener 状态机、
  `JSONObject` 的 map 选型、写出器的作用域栈、异常分类学、设计模式清单、包依赖图与评审意见。
