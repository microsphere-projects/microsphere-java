# 日志

| | |
|---|---|
| **构件** | `io.github.microsphere-projects:microsphere-java-core`（`0.3.20-SNAPSHOT`，`release` 分支） |
| **包** | `io.microsphere.logging` — 9 个顶层类型（另有 3 个嵌套适配器类） |
| **字节码 / JDK** | 整个 reactor 中不存在任何 `maven.compiler.*` 或 `<release>` 属性；字节码级别继承自外部构建父工程 `microsphere-build:0.3.16`。CI 在 Temurin JDK 8 / 11 / 17 / 21 / 25 上测试 |
| **可选的第三方 API** | `org.slf4j:slf4j-api`（`microsphere-java-core/pom.xml:39-43`）与 `commons-logging:commons-logging`（`:46-50`），二者都是 compile 作用域下的 `<optional>true</optional>` |
| **依赖的其他 `core` 包** | `io.microsphere.lang`（`Prioritized`）、`io.microsphere.util`（`ClassUtils.getTypeName`、`StringUtils.isBlank` / `substringBefore`、`ArrayUtils.length`）、`io.microsphere.collection`（`ListUtils.newLinkedList`）、`io.microsphere.text`（`FormatUtils.format`）、`io.microsphere.constants`（`SymbolConstants.DOLLAR`）、`io.microsphere.annotation`（`@Nonnull` / `@Nullable`） |
| **被谁使用** | 约 37 个其他 `core` 类（例如 `convert/AbstractConverter`、`reflect/MethodUtils`、`io/IOUtils`、`event/AbstractEventDispatcher`、`classloading/ArtifactDetector`）——`logging` 是本库通往日志框架的唯一出口 |

本页所有 `file:line` 引用都相对于 `microsphere-java-core/src/main/java/io/microsphere/`（主源码）或
`microsphere-java-core/src/test/java/io/microsphere/`（测试），读取自 `release` 工作树。首次完整出现后，
缩写为 `logging/LoggerFactory.java:88-102`。

---

## 1. 这个包组是做什么用的

**当库代码需要输出日志、又不希望本 artifact 把日志框架拉进消费者的 classpath 时，就该使用
`io.microsphere.logging`。** 该包是一个自给自足的薄门面：一个 `Logger` 接口（`logging/Logger.java:62`）、
一个通过 `ServiceLoader` 解析后端的抽象工厂（`logging/LoggerFactory.java:65`）、三个覆盖本库视为可选之
框架的适配器（SLF4J、Apache Commons Logging、`java.util.logging`），以及一个永远可用的 no-op 兜底
（`logging/NoOpLoggerFactory.java:39`）。除这三个适配器之外，`microsphere-java-core` 中没有任何代码 import
`org.slf4j` 或 `org.apache.commons.logging`——这正是 `slf4j-api` 与 `commons-logging` 能够被声明为
`<optional>true</optional>` 的原因（`microsphere-java-core/pom.xml:39-43`、`:46-50`）。

```java
Logger logger = LoggerFactory.getLogger(MyClass.class);      // logging/LoggerFactory.java:97-100
Logger named  = LoggerFactory.getLogger("io.microsphere.x"); // logging/LoggerFactory.java:108-111
logger.info("Started in {} ms", elapsed);                    // logging/Logger.java:164
// No per-class Logger field? LoggerUtils derives the name for you:
LoggerUtils.debug(logger -> logger.debug("Parsed {} members", n)); // logging/LoggerUtils.java:52-54
```

设计原则是**静默降级，外加一个有文档记录的例外**：后端缺失是正常状态，绝不是错误。
`getDelegateLoggerClass()` 吞掉 `ClassLoader.loadClass` 抛出的 `Throwable` 并返回 `null`
（`logging/LoggerFactory.java:127-136`），`isAvailable()` 把它变成 `false`（`:118-120`），候选列表是被过滤
而不是被判定失败（`:78-83`），而 `NoOpLoggerFactory`——永远可用（`:46-50`）——排在最后，因此存活列表永不
为空。唯一刻意的抛出是 `NoOpLoggerFactory.getDelegateLoggerClassName()` 的
`UnsupportedOperationException`（`logging/NoOpLoggerFactory.java:41-44`）；见 §7.5。

> [!NOTE]
> 这是一个**门面，而不是配置系统**：`LoggerFactory` 从不读取日志属性文件、从不设置级别、也从不安装
> appender——它只决定你会得到*哪个*后端。配置仍然留在 SLF4J / Commons Logging / JUL 那一侧。

---

## 2. 坐标与依赖

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

不需要 `<exclusions>`，也不必额外添加日志框架：当 classpath 上一个框架都没有时，门面会静默选择
`java.util.logging`。若要让 SLF4J 分支可被选中，请自行引入 `slf4j-api` 及其绑定——
`microsphere-java-core` 不会替你做。用于*构建*本 artifact 的 `slf4j-api` 与 `commons-logging` 版本并未在
本 reactor 中钉住；它们来自外部导入的 BOM（`microsphere-java-parent/pom.xml:31-37`；
`microsphere-all-bom` 版本 `0.3.13` 声明于 `:22`）。

| 事实 | 取值 | 证据（`release`） |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | 根 `pom.xml:12` |
| `artifactId` | `microsphere-java-core` | 模块 `pom.xml:14` |
| 版本属性 | `${revision}` | 根 `pom.xml:14`、`:53-55` |
| `release` 上 `${revision}` 的值 | `0.3.20-SNAPSHOT` | 根 `pom.xml:54` |
| 该版本线已发布的 tag | `0.3.20` | 文档所采用的发布线 |
| 构建父工程 | `microsphere-build:0.3.16` | 根 `pom.xml:6-10` |
| Java 级别 | reactor 内未声明——继承自外部父工程 `microsphere-build:0.3.16` | reactor 中唯一出现的 `maven-compiler-plugin` 在 `microsphere-annotation-processor/pom.xml:98`，且任何位置都没有 `maven.compiler.*` / `<release>` 属性 |
| CI 矩阵 | Temurin JDK `8 / 11 / 17 / 21 / 25` | `.github/workflows/maven-build.yml:25`，`distribution: 'temurin'` `:33`，构建命令 `:38-42` |
| Spring Framework（仅构建期；与 `logging` 运行时无关） | 默认 `7.0.9` / profile `java8-16`（`<jdk>[1.8,17)</jdk>`）下 `5.3.39` | `microsphere-java-parent/pom.xml:23`，BOM 导入 `:40-46`，profile `:54-62`，激活条件 `:57` |
| 导入的 BOM | `microsphere-all-bom:0.3.13` + `spring-framework-bom` | `microsphere-java-parent/pom.xml:22`、`:33-36`、`:40-46` |

---

## 3. 版本信息与兼容范围

| 项目 | `release` 上的状态 | 证据 |
|---|---|---|
| 包内每个类型的 `@since` | `1.0.0`——整个包都是稳定 API，没有实验性标记 | `logging/Logger.java:60`、`logging/AbstractLogger.java:61`、`logging/LoggerFactory.java:63`、`logging/Sfl4jLoggerFactory.java:42`、`logging/ACLLoggerFactory.java:46`、`logging/JDKLoggerFactory.java:52`、`logging/NoOpLoggerFactory.java:37`、`logging/LoggerUtils.java:34` |
| 支持的 JDK 范围 | 8 → 25（CI 已验证）；包内没有使用任何晚于 Java 8 的 API | `.github/workflows/maven-build.yml:25` |
| SLF4J | classpath 上任何携带 `org.slf4j.Logger` 的 `slf4j-api` 都能满足探测；适配器只调用 `isXxxEnabled()` 以及 `(String)` / `(String, Object[])` / `(String, Throwable)` 重载 | 探测 `logging/Sfl4jLoggerFactory.java:46`，委托调用 `:72-170` |
| Commons Logging | classpath 上任何 `org.apache.commons.logging.Log`；适配器使用 `LogFactory.getLog(String)` | 探测 `logging/ACLLoggerFactory.java:50`、`:71-74` |
| `java.util.logging` | JVM 中始终存在；当前两者都解析不到时被使用 | `logging/JDKLoggerFactory.java:56`、`:77-80` |
| no-op 兜底 | 始终存在（随本 artifact 一起发布），最后手段 | `logging/NoOpLoggerFactory.java:46-50`、`:56-59` |

> [!TIP]
> `logging` **不含任何 Spring 或 JDK 版本分支**：在 `java8-16`（`5.3.39`）与默认（`7.0.9`）两个 profile 下
> 行为完全一致，那两个 profile 只切换一个本包从不读取的 Spring 属性。


### 3.1 `0.3.20` 的发布日期与变更摘要

| 条目 | 值 | 依据 |
|---|---|---|
| 文档所在线 | `release` 分支，`${revision}` = `0.3.20-SNAPSHOT` | 根 `pom.xml:54` |
| 最新已发布版本 | `0.3.20`，标签日期 **2026-10-16** | 标签 `0.3.20` 的提交信息为 `chore: merge main into release [skip ci]`，提交时间 `2026-10-16T03:43:20Z` |
| 上一版本 | `0.3.19`，标签日期 **2026-08-30** | 标签 `0.3.19`，提交时间 `2026-08-30T12:51:37Z` |
| 变更日志来源 | 仓库根目录的 `release-notes.md` | 本项目不发布 `CHANGELOG.md`；`release-notes.md` 的 `v0.3.16`–`v0.3.20` 段落写着 *Release notes generation failed* 并保留原始提交列表，因此下文一律以提交主题引用，并逐条与 `release` 源码核对。 |

`0.3.20` 在这一区域没有新增 API 面——`0.3.19...0.3.20` 区间是一次加固性发布。它实际改变了什么、以及这些改动触及本页源码的程度如下：

* **空值契约。** 本页覆盖范围内 1 / 9 个主源码在签名上声明了 `@Nonnull` / `@Nullable`。区间内点名本区域的主题：`a2824fff Add nullability annotations to logging APIs`。
* **空输入与行为修复。** `6f83fb21 Document no-op logger methods`。

> [!NOTE]
> 如何读这份提交清单：带注解的只有 `LoggerFactory`；`AbstractLogger` 与各适配器未被契约改造触及，这与本页记录的空操作降级契约一致。

---

## 4. 获取 `Logger`

### 4.1 两个入口点

| 签名 | 行为 | 证据 |
|---|---|---|
| `static Logger getLogger(Class<?> type)` | `@Nonnull`；以 `type.getName()` 委托给名称形式 | `logging/LoggerFactory.java:97-100` |
| `static Logger getLogger(String name)` | `@Nonnull`；在唯一已解析的工厂上调用 `factory.createLogger(name)` | `logging/LoggerFactory.java:108-111` |
| `abstract Logger createLogger(String name)` | 为自定义后端而覆写的每工厂工厂方法 | `logging/LoggerFactory.java:152` |
| `static void LoggerUtils.trace/debug/info/warn/error(Consumer<Logger>)` | 惰性的单次日志；名称取自调用者所在的类 | `logging/LoggerUtils.java:43-81` |

工厂**只解析一次**，位置在 `LoggerFactory` 的静态初始化块中，结果写入一个 `private static final` 字段
（`logging/LoggerFactory.java:69-70`）；此后每次 `getLogger` 调用都只是读字段加一次 `createLogger` 调用——
没有重新探测，也没有加锁。

> [!WARNING]
> **Logger 没有被缓存。** `getLogger(name)` 每次调用都返回*新的适配器实例*
> （`logging/LoggerFactory.java:110` → `logging/Sfl4jLoggerFactory.java:54-56`、
> `logging/ACLLoggerFactory.java:58-60`、`logging/JDKLoggerFactory.java:64-66`），因此两次
> `getLogger(Foo.class)` 得到的对象并不相同（`!=`），尽管它们背后的 SLF4J/JUL/ACL logger 由该后端共享。
> 请把结果保存在 `static final` 字段里；不要每条语句都调用 `getLogger`，也不要用 `==` 比较 logger。

### 4.2 从 lambda 推导 logger 名称（`LoggerUtils`）

`LoggerUtils` 没有可供保存名称的每类字段，因此它从 lambda 自身的类名重建调用者类名，并在第一个 `$` 处
切断：`getLoggerName` 就是 `substringBefore(getTypeName(loggerConsumer), DOLLAR)`
（`logging/LoggerUtils.java:101-103`），而 `log` 只在级别守卫通过后才会调用 consumer
（`logging/LoggerUtils.java:89-94`）。`ClassUtils.getTypeName(Object)` 返回
`value.getClass().getName()`（`util/ClassUtils.java:2025-2028`），`StringUtils.substringBefore` 在
`SymbolConstants.DOLLAR` = `"$"` 处切断（`constants/SymbolConstants.java:228`、
`util/StringUtils.java:472`），于是在 `com.acme.OrderService` 内部声明的 lambda 得到的 logger 名称就是
`com.acme.OrderService`。

**可运行示例**——取自本包自己的测试，它覆盖了每一个级别以及 `Predicate` 重载：

```java
import static io.microsphere.logging.LoggerUtils.*;

assertDoesNotThrow(() -> info(logger -> logger.info("Hello,World")));
assertDoesNotThrow(() -> info(logger -> logger.info("Hello,{}", "World")));
assertDoesNotThrow(() -> info(logger -> logger.info("{},{}", "Hello", "World")));
assertDoesNotThrow(() -> log(logger -> false, logger -> logger.info("Hello,World")));
```

_Source: logging/LoggerUtilsTest.java:55-58,76-78_

**可运行示例**——分别按类与按名称获取 logger，然后用三种调用形态驱动全部五个级别：

```java
Logger logger = getLogger(LoggerFactoryTest.class);   // logging/LoggerFactoryTest.java:52
log(logger, "Hello,World");                                        // String form
log(logger, "Hello,World {}", getInstance().get(YEAR));            // varargs form
log(logger, "Hello,World", new Throwable("Testing"));              // Throwable form
```

_Source: logging/LoggerFactoryTest.java:51-71_

---

## 5. 日志级别 API 全景

`Logger` 声明了 **21 个方法**：一个 `getName()`，再加上 `TRACE`、`DEBUG`、`INFO`、`WARN`、`ERROR` 五个级别
各自的守卫与三种发射形态（`logging/Logger.java:69-239`）。

| 级别 | 守卫 | 纯文本 | 格式化 | 带异常 |
|---|---|---|---|---|
| TRACE | `isTraceEnabled()` `Logger.java:77` | `trace(String)` `:84` | `trace(String, Object...)` `:98` | `trace(String, Throwable)` `:107` |
| DEBUG | `isDebugEnabled()` `:115` | `debug(String)` `:122` | `debug(String, Object...)` `:131` | `debug(String, Throwable)` `:140` |
| INFO | `isInfoEnabled()` `:148` | `info(String)` `:155` | `info(String, Object...)` `:164` | `info(String, Throwable)` `:173` |
| WARN | `isWarnEnabled()` `:181` | `warn(String)` `:188` | `warn(String, Object...)` `:197` | `warn(String, Throwable)` `:206` |
| ERROR | `isErrorEnabled()` `:214` | `error(String)` `:221` | `error(String, Object...)` `:230` | `error(String, Throwable)` `:239` |

### 5.1 占位符语法是 SLF4J 风格的 `{}`，不是 `%s`

`AbstractLogger.resolveMessage` 委托给 `FormatUtils.format`，后者的 `DEFAULT_PLACEHOLDER` 就是字面的两个
字符 `{}`（`text/FormatUtils.java:17`、`:38-40`）。替换是按位置、按顺序进行的：第一个 `{}` 得到 `args[0]`，
第二个得到 `args[1]`，依此类推。未被消费的占位符原样保留在输出中（`text/FormatUtils.java:73-80`），并且每个
参数都用 `String.valueOf` 字符串化，所以 `null` 渲染为 `"null"` 而不会抛异常。

| 调用 | 结果 | 原因 |
|---|---|---|
| `info("Hello, {}", "World")` | `Hello, World` | 一个占位符、一个参数 |
| `info("{},{}", "Hello", "World")` | `Hello,World` | 两个占位符按序消费 |
| `info("plain", "extra")` | `plain` | 没有占位符 → 循环在 `index == -1` 处中断（`text/FormatUtils.java:75-77`） |
| `info("a {} b {}", "x")` | `a x b {}` | 占位符多于参数：余下的原样保留 |

### 5.2 谁来格式化：SLF4J 适配器与另外两个不同

- **`Sfl4jLogger` 覆写了每一个变参方法**并直接转发给 SLF4J
  （`logging/Sfl4jLoggerFactory.java:82-85`、`:102-105`、`:122-125`、`:142-145`、`:162-165`），因此格式化发生在
  SLF4J *内部*且是惰性的，`AbstractLogger` 的流水线被完全绕过。这是本包影响最大的一个不对称点。
- **`ACLLogger` 与 `JDKLogger` 没有覆写变参方法。** 它们继承
  `AbstractLogger.trace/debug/info/warn/error(String, Object...)`（`logging/AbstractLogger.java:76-99`），
  后者会先调用 `FormatUtils.format` 急切地拼出消息，再去询问后端——即使该级别处于关闭状态。

```java
protected void log(Consumer<String> messageHandler, BiConsumer<String, Throwable> messageThrowableHandler,
                   String format, Object... arguments) {                       // logging/AbstractLogger.java:107
    final String message; int length = length(arguments);
    if (length < 1) { message = format; }                                      // :110-111
    else {
        message = resolveMessage(format, arguments);                           // :113
        Object lastArgument = arguments[length - 1];
        if (lastArgument instanceof Throwable)                                 // :115
            messageThrowableHandler.accept(message, (Throwable) lastArgument); // :116
    }
    messageHandler.accept(message);                                            // :119
}
```

> [!IMPORTANT]
> **当最后一个参数是 `Throwable` 时，`AbstractLogger.log` 会发射两次**——它在
> `logging/AbstractLogger.java:116` 调用 `(String, Throwable)` 处理器，随后没有 `return` 就直接落到
> `logging/AbstractLogger.java:119` 的纯 `(String)` 处理器。因此在 **Commons Logging** 与 **JUL** 后端下，
> `logger.error("boom {}", e)` 会由一次调用产生*两条*日志记录。SLF4J 后端看不出这个问题，因为
> `Sfl4jLogger` 覆写了变参方法（`logging/Sfl4jLoggerFactory.java:122-125`）。当把尾随的 `Throwable` 作为
> 格式参数传入时，不要指望"一次调用 = 一条记录"；请改用显式的 `error(String, Throwable)` 重载。
> 这也意味着与 `{}` 占位符对齐的尾随 `Throwable` 会被 `String.valueOf(...)` 字符串化进消息正文（见上文
> `a {} b {}` 那一行），把它的 `toString()` 泄漏到消息里。

### 5.3 `getName()`

`AbstractLogger` 在构造器里把名称存一次，并把访问器锁定为 `final`，因此子类无法改变它
（`logging/AbstractLogger.java:65-74`）。`NoOpLogger` 会对空白输入做归一化——
`super(isBlank(name) ? "" : name)`（`logging/NoOpLogger.java:43-45`）——所以 `"test"` 保留原名，而 `""`、
`SPACE`（`" "`）与 `null` 都解析为 `""`。

_Source: logging/NoOpLoggerFactoryTest.java:46-58_

> [!WARNING]
> 空白名称守卫**只**存在于 `NoOpLogger` 中。`LoggerFactory.getLogger(String)` 不做任何校验
> （`logging/LoggerFactory.java:108-111`），而是把值转发给被选中的适配器，后者再交给
> `java.util.logging.Logger.getLogger`（`logging/JDKLoggerFactory.java:79`）、`LogFactory.getLog`
> （`logging/ACLLoggerFactory.java:73`）或 `org.slf4j.LoggerFactory.getLogger`
> （`logging/Sfl4jLoggerFactory.java:69`）。`getLogger(Class)` 还会立即解引用它的参数
> （`type.getName()`，`logging/LoggerFactory.java:99`），因此传入 `null` 的 `Class` 会直接抛出
> `NullPointerException`——这是对静默降级策略的一次真实偏离，已记录到 NPE 加固审计中。

**可运行示例**——每个适配器都必须通过的共享契约测试，它驱动全部 21 个方法：

```java
@Test
void testInfo() {
    assertTrue(logger.isInfoEnabled());
    assertDoesNotThrow(() -> {
        logger.info("test");
        logger.info("test", EMPTY_OBJECT_ARRAY);            // varargs, zero arguments
        logger.info("test : {}", "a");                      // formatted
        logger.info("test : {}", "a", new Throwable());     // formatted + trailing Throwable
        logger.info("test", new Throwable());               // explicit Throwable overload
    });
}
```

_Source: logging/AbstractLoggerTest.java:89-99_（`trace`/`debug`/`warn`/`error` 是同样的形态，位于 `:55-65`、
`:71-82`、`:106-116`、`:123-133`。）

---

## 6. 后端如何被选中

### 6.1 委托顺序

`LoggerFactory` 本身就是一个 SPI。四个内置实现按此顺序注册在
`src/main/resources/META-INF/services/io.microsphere.logging.LoggerFactory:1-4` 中：
`Sfl4jLoggerFactory`、`ACLLoggerFactory`、`JDKLoggerFactory`、`NoOpLoggerFactory`。

每个工厂都声明了它所包装的后端类名（`logging/Sfl4jLoggerFactory.java:46`、
`logging/ACLLoggerFactory.java:50`、`logging/JDKLoggerFactory.java:56`，以及永远可用的
`logging/NoOpLoggerFactory.java:46-50`），并通过 `io.microsphere.lang.Prioritized` 钉住自己的秩：

| 工厂 | 探测的委托类 | `getPriority()` | 数值 | 实际名次 |
|---|---|---|---|---|
| `Sfl4jLoggerFactory` | `org.slf4j.Logger` | `NORMAL_PRIORITY`（`logging/Sfl4jLoggerFactory.java:58-61`） | `0` | 第 1 |
| `ACLLoggerFactory` | `org.apache.commons.logging.Log` | `NORMAL_PRIORITY + 5`（`logging/ACLLoggerFactory.java:62-65`） | `5` | 第 2 |
| `JDKLoggerFactory` | `java.util.logging.Logger` | `NORMAL_PRIORITY + 10`（`logging/JDKLoggerFactory.java:68-71`） | `10` | 第 3 |
| `NoOpLoggerFactory` | 无（覆写了 `isAvailable`） | `MIN_PRIORITY`（`logging/NoOpLoggerFactory.java:56-59`） | `Integer.MAX_VALUE`（`lang/Prioritized.java:86`） | 第 4，永远 |

**这些常量与名称的含义是相反的**：`Prioritized.MAX_PRIORITY = Integer.MIN_VALUE`
（`lang/Prioritized.java:81`）、`MIN_PRIORITY = Integer.MAX_VALUE`（`:86`）、`NORMAL_PRIORITY = 0`（`:91`）。
`compareTo` 就是朴素的 `compare(this.getPriority(), that.getPriority())`（`:102-105`），而
`LoggerFactory.loadFactories()` 使用 `Collections.sort(factories, COMPARATOR)` 排序
（`logging/LoggerFactory.java:85-89`），所以**数值最小的优先级排在最前并胜出**。因此"最小优先级"的含义正是
"最后手段"——这也正是 `NoOpLoggerFactory` 返回 `MIN_PRIORITY` 的原因。

### 6.2 逐步解析算法

```java
private static final ClassLoader classLoader = LoggerFactory.class.getClassLoader();  // logging/LoggerFactory.java:67
private static final LoggerFactory factory = loadFactory();                           // :69-70
private static LoggerFactory loadFactory() {                                          // :72-76
    return loadAvailableFactories().get(0);
}
static List<LoggerFactory> loadAvailableFactories() {                                 // :78-83
    List<LoggerFactory> factories = loadFactories();
    factories.removeIf(LoggerFactory::isAvailable.negate());
    return factories;
}
static List<LoggerFactory> loadFactories() {                                          // :85-89
    LinkedList<LoggerFactory> factories = newLinkedList(load(LoggerFactory.class, classLoader));
    sort(factories, COMPARATOR);
    return factories;
}
protected boolean isAvailable() { return nonNull(getDelegateLoggerClass()); }         // :118-120
@Nullable private Class<?> getDelegateLoggerClass() {                                 // :127-136
    Class<?> c = null;
    try { c = classLoader.loadClass(getDelegateLoggerClassName()); }                  // :132
    catch (Throwable e) { /* deliberately empty */ }                                  // :131-134
    return c;
}
```

1. `ServiceLoader.load(LoggerFactory.class, classLoader)` 实例化每一个已注册的工厂（`:86`）；`classLoader` 是
   加载 `LoggerFactory` 自身的那个加载器（`:67`）。
2. 列表按 `Prioritized.COMPARATOR` 排序（`:87`、`lang/Prioritized.java:61-76`）。
3. `removeIf(isAvailable.negate())` 剔除每一个委托类无法加载的工厂（`:78-83`）。
4. `get(0)` 取走优先级数值最小的存活者（`:75`）。

可用性判断是一个**类加载探测**，而不是 `instanceof` 检查或静态引用：后端 jar 存在时 `loadClass` 成功，不存在
时抛 `ClassNotFoundException`，而 `catch` 块刻意留空（`logging/LoggerFactory.java:131-134`）——这正是让缺失的
SLF4J 成为一件平常事而非崩溃的原因。

### 6.3 推演结果

| classpath | 胜出者 | 得到的 `Logger` 实现 |
|---|---|---|
| `slf4j-api` + 某个绑定存在 | `Sfl4jLoggerFactory`（优先级 `0`） | `Sfl4jLoggerFactory.Sfl4jLogger`（`logging/Sfl4jLoggerFactory.java:63`） |
| `slf4j-api` 缺失、`commons-logging` 存在 | `ACLLoggerFactory`（`5`） | `ACLLoggerFactory.ACLLogger`（`logging/ACLLoggerFactory.java:67`） |
| 两者都不存在 | `JDKLoggerFactory`（`10`） | `JDKLoggerFactory.JDKLogger`（`logging/JDKLoggerFactory.java:73`） |
| `java.util.logging` 不可用（非标准运行时） | `NoOpLoggerFactory`（`MAX_VALUE`） | `NoOpLogger`（`logging/NoOpLogger.java:41`） |

**可运行示例**——证明"过滤且排序后的列表"端到端可用的那个测试：它遍历*每一个*通过可用性探测的工厂，并把
三种调用形态全部压进该工厂创建的 logger。

```java
@Test
void testLoadAvailableFactories() {
    loadAvailableFactories().forEach(this::testLoggerFactory);   // logging/LoggerFactoryTest.java:57-59
}
private void testLoggerFactory(LoggerFactory loggerFactory) {
    Logger logger = loggerFactory.createLogger("io.microsphere");
    assertLevel(logger);                                          // all five isXxxEnabled() must be true
    log(logger, "Hello,World");                                   // :68
    log(logger, "Hello,World {}", getInstance().get(YEAR));       // :69
    log(logger, "Hello,World", new Throwable("Testing"));         // :70
}
```

_Source: logging/LoggerFactoryTest.java:56-71_

> [!NOTE]
> `loadAvailableFactories()` 与 `loadFactories()` **刻意是包私有的**（`logging/LoggerFactory.java:78`、`:85`）：
> 它们是那个"观察接缝"，让 `io.microsphere.logging` 包内的测试能够枚举存活的候选者，而不只是看到胜出者。
> 在该包之外，请通过 `getLogger(...).getClass()` 观察最终选择。

---

## 7. 在测试中强制与观察后端

没有 setter，也没有系统属性可以切换后端：解析发生在静态初始化块中且只执行一次
（`logging/LoggerFactory.java:69-70`），而且字段是 `final`。仓库转而使用下面这些技巧。

### 7.1 注册一个必然被过滤掉的工厂

`NoDelegateLoggerFactory` 是一个仅测试用的 `LoggerFactory`，它的委托类名永远无法解析
（`return "class-not-found"`，`logging/NoDelegateLoggerFactory.java:29-32`；`createLogger` 返回 `null`，
`:34-37`）。它注册在*测试* SPI 文件
`src/test/resources/META-INF/services/io.microsphere.logging.LoggerFactory:1` 里，与四个主条目并列，于是
`loadClass("class-not-found")` 抛异常、`isAvailable()` 未被覆写，`removeIf` 便把它静默剔除
（`logging/LoggerFactory.java:78-83`）——在不改动 classpath 的前提下构造出探测的反例。

### 7.2 显式触达未胜出的工厂

要覆盖那些落败的分支，请从已加载的列表里取工厂，而不是从 `getLogger` 里取，并按类型扫描：

```java
List<LoggerFactory> factories = loadAvailableFactories();
for (LoggerFactory f : factories) {
    if (f instanceof NoOpLoggerFactory) { factory = (NoOpLoggerFactory) f; }
}
```

_Source: logging/NoOpLoggerFactoryTest.java:25-33_

直接构造工厂同样可行，而且各适配器测试就是这么做的（`logging/ACLLoggerFactoryTest.java:13-15` 的
`new ACLLoggerFactory().createLogger(...)`、`logging/JDKLoggerFactoryTest.java:28-30` 的
`new JDKLoggerFactory().createLogger(...)`、`logging/Sfl4jLoggerFactoryTest.java:13-15` 的
`new Sfl4jLoggerFactory().createLogger(...)`）。

### 7.3 把 JUL 后端强制到断言所需的级别

`JDKLogger.isTraceEnabled()` 把 TRACE 映射到 `Level.ALL`（`logging/JDKLoggerFactory.java:82-85`，import 在
`:23`），而 `AbstractLoggerTest` 断言它返回 `true`（`logging/AbstractLoggerTest.java:50-53`）。JUL 的默认配置
不会打开 TRACE，因此测试在任何东西运行之前安装自己的配置：

```java
@BeforeAll
static void beforeAll() throws IOException {
    URL resource = LoggerFactoryTest.class.getResource("/META-INF/logging.properties");
    try (InputStream inputStream = resource.openStream()) {
        LogManager.getLogManager().readConfiguration(inputStream);
    }
}
```

_Source: logging/LoggerFactoryTest.java:42-48（同样的代码块出现在 `logging/JDKLoggerFactoryTest.java:19-25`）_

它加载的文件是 `handlers = java.util.logging.ConsoleHandler` 加上 `.level = ALL`
（`microsphere-java-core/src/test/resources/META-INF/logging.properties:1-3`）。

> [!WARNING]
> `LogManager.readConfiguration` 改动的是**整个 JVM**，而不是某个测试类。两处加载都写在 `@BeforeAll` 里，而
> `ACLLoggerFactoryTest`（`logging/ACLLoggerFactoryTest.java:10-15`）在**没有**安装任何自己的配置的情况下，
> 对一个 Commons-Logging logger 断言 `isTraceEnabled()`。因此该断言是否成立，可能取决于是否已有别的测试类先
> 跑过。请把 `META-INF/logging.properties` 当作全局测试状态看待，并为任何依赖 JUL 级别的新测试补上那个
> `@BeforeAll` 代码块。

### 7.4 观察 SLF4J 后端的输出

`microsphere-java-core` 以 test 作用域引入 `ch.qos.logback:logback-classic`
（`microsphere-java-core/pom.xml:81-85`）以及 `microsphere-java-test`（`:66-71`）。后者提供
`logback-test.xml`，它把 root 保持在 stdout 的 `INFO`，但把 `io.microsphere` 以 `TRACE` 路由到文件 appender
且关闭 additivity（`microsphere-java-test/src/main/resources/logback-test.xml:24-30`）：

```xml
<logger name="io.microsphere" level="TRACE" additivity="false">
    <appender-ref ref="ASYNC"/>
</logger>
```

这就是 SLF4J 适配器下 `isTraceEnabled()` 为 `true` 的原因：每个测试 logger 的名字都是 `io.microsphere.logging.*`
——由被测类推导（`logging/Sfl4jLoggerFactoryTest.java:13-15`）或来自显式的 `"io.microsphere"` 参数
（`logging/LoggerFactoryTest.java:62`）——因此都落在该 `TRACE` logger 之下。这套测试断言的是*是否启用*与
*不抛异常*的行为，而不是输出文本（`logging/AbstractLoggerTest.java:45-133`）。

### 7.5 "应当永不抛异常"在实践中意味着什么

每个适配器测试都归结到 `assertDoesNotThrow`，包括那些别扭的形态——零变参、带尾随 `Throwable` 的格式化字符串，
以及显式的 `Throwable` 重载。唯一断言*会抛*的测试针对的是静默降级的那一个有文档记录的例外：
`assertThrows(UnsupportedOperationException.class, factory::getDelegateLoggerClassName)`，随后是
`assertTrue(factory.isAvailable())`。

_Source: logging/NoOpLoggerFactoryTest.java:35-43_

> [!TIP]
> `getDelegateLoggerClassName()` 之所以只是*可能*抛出，是因为 `isAvailable()` 被覆写为 `true`
> （`logging/NoOpLoggerFactory.java:46-50`），从而短路了基类对 `getDelegateLoggerClass()` 的调用
> （`logging/LoggerFactory.java:118-120`）。去掉那个覆写，`UnsupportedOperationException` 就会在静态初始化块
> 里触发，并连带把整个 `LoggerFactory` 类拖成 `ExceptionInInitializerError`。

---

## 8. 常见陷阱

- **不要仅仅为了使用门面就添加对 SLF4J 的硬依赖。** `slf4j-api` 是 `<optional>`
  （`microsphere-java-core/pom.xml:39-43`）。完全没有框架时 `LoggerFactory` 依然工作，并静默落在 JUL 上；
  加入 `slf4j-api` 只改变你在 §6.3 表格里得到哪一行。
- **自定义工厂必须覆写 `getPriority()`。** `LoggerFactory` 实现了 `Prioritized` 却从不覆写 `getPriority()`，
  所以你的子类会继承接口默认值 `NORMAL_PRIORITY` = `0`（`lang/Prioritized.java:98-100`）——与
  `Sfl4jLoggerFactory` 打平（`logging/Sfl4jLoggerFactory.java:58-61`）。排序是稳定的
  （`logging/LoggerFactory.java:87`），因此平局由 service 文件的 classpath 顺序决定，你的工厂可能在两次构建
  之间无声地胜出或落败。请选一个有意的值（`NORMAL_PRIORITY - 1` 能盖过 SLF4J）。此外
  `Prioritized.COMPARATOR` 对任何 `Prioritized` 与非 `Prioritized` 的组合都返回 `-1`
  （`lang/Prioritized.java:61-76`），所以它不是通用排序器。
- **扩展加载使用的是*定义*类加载器，而非线程上下文类加载器。**
  `load(LoggerFactory.class, classLoader)` 读的是 `LoggerFactory.class.getClassLoader()`
  （`logging/LoggerFactory.java:67`、`:86`）。只对 webapp/TCCL 加载器可见的工厂不会被发现；而且与同类 SPI
  工具不同，`LoggerFactory` 没有走 `ClassLoaderUtils.getClassLoader(...)` / `nullSafeClassLoader`
  （`util/ClassLoaderUtils.java:298-303`）——`null`（bootstrap）加载器会被原样传给 `ServiceLoader`。
- **引导过程不具备容错性，而且选出胜者的一步没有守卫。** `ServiceLoader` 的迭代是急切的
  （`newLinkedList(load(...))`，`logging/LoggerFactory.java:86`），并且运行在静态初始化块内（`:69-70`），所以
  一条畸形的 service 条目——缺失的类、非 public 构造器、抽象实现——会以 `ServiceConfigurationError` →
  `ExceptionInInitializerError` 浮现，此后**每一次** `getLogger` 调用都会以 `NoClassDefFoundError` 失败。
  同样地，`availableFactories.get(0)`（`:75`）没有空列表守卫：它之所以安全，仅仅是因为
  `NoOpLoggerFactory` 永远可用，因此把该条目从
  `META-INF/services/io.microsphere.logging.LoggerFactory` 中删掉，就会在类初始化时得到
  `IndexOutOfBoundsException`。两者都偏离了静默降级：每工厂的探测是有守卫的（`:131-134`），加载与选取没有。
- **`error(format, args…, throwable)` 在 ACL 与 JUL 下会双重记录**——见 §5.2
  （`logging/AbstractLogger.java:116` 之后是 `:119`，没有 `return`）。只记异常时请使用
  `error(String, Throwable)` 重载。
- **在 ACL/JUL 下格式化开销不会被推迟。** 两者都继承 `AbstractLogger` 的变参方法，所以
  `FormatUtils.format` 在询问后端之前就已运行（`logging/AbstractLogger.java:113`、
  `text/FormatUtils.java:62-82`）。在热点 DEBUG/TRACE 路径上请用 `isDebugEnabled()` 显式守卫，或者把调用改走
  `LoggerUtils`（`logging/LoggerUtils.java:52-54`），它会先测试谓词（`:89-94`）。
- **`NoOpLogger` 报告所有级别都已启用**（`logging/NoOpLogger.java:47-50`、`:60-63`、`:73-76`、`:86-89`、
  `:99-102`）却丢弃一切，所以
  `if (logger.isInfoEnabled()) logger.info(buildExpensiveString());` 仍然要为
  `buildExpensiveString()` 买单。`isXxxEnabled()` 并不是"输出有去处"的代理指标。
- **JUL 的 TRACE 映射到 `Level.ALL`，而不是一个专门的级别**（`logging/JDKLoggerFactory.java:82-85`）：
  只要 JUL 级别是 `ALL` 或更低，`isTraceEnabled()` 就是 `true`，而 `trace(...)` 以 `ALL` 记录
  （`:88-90`、`:93-95`）。
- **`LoggerUtils` 把 logger 命名成 lambda 的*定义*类，而不是发起日志的类。** 写在 `com.acme.Bar` 里、但由
  `com.acme.Foo` 交给 `LoggerUtils` 的 lambda 会变成 `com.acme.Bar`，因为切分依据是
  `loggerConsumer.getClass().getName()`（`logging/LoggerUtils.java:101-103`）；方法引用取目标方法的声明类，
  而匿名内部类会被折叠（`Foo$1` → `Foo`）。
- **`LoggerUtils` 缺少 `util.Utils` 标记接口**（`logging/LoggerUtils.java:36`），而其他 `core` 的 `*Utils`
  类都实现了它（`util/Utils.java:41`）；它是 `abstract` 且带私有构造器
  （`logging/LoggerUtils.java:105-106`），因此任何基于约定的工具类扫描都会漏掉它。
- **不要跨类加载器边界持有 `Logger`。** Logger 是每次调用新建的、未被缓存的对象（§4.1），包装着来自各后端
  自身注册表的 logger（`logging/Sfl4jLoggerFactory.java:69`、`logging/ACLLoggerFactory.java:73`、
  `logging/JDKLoggerFactory.java:79`），而探测所用的加载器固定在 `logging/LoggerFactory.java:67`；
  一个隔离或重新加载的加载器会为它自己那份副本重新解析 `LoggerFactory`。

---

## 9. 另见

- [语言抽象](./language-abstractions.zh.md) — `Prioritized`，其反向常量与 `COMPARATOR` 定义了后端顺序（§6.1）。
- [工具类、常量与文本](./util-and-constants.zh.md) — `ClassUtils.getTypeName`、`StringUtils.substringBefore` /
  `isBlank`、`FormatUtils.format` 与 `SymbolConstants.DOLLAR`，即 `LoggerUtils` 与 `AbstractLogger` 所依托的基座。
- [集合与过滤器](./collections-and-filters.zh.md) — `ListUtils.newLinkedList`，SPI 加载中的急切消费那一步
  （`logging/LoggerFactory.java:86`）。
- [类型转换](./type-conversion.zh.md) — 采用相同 `Prioritized` + 缓存列表选择形态的兄弟 SPI，也是约 37 个库内
  `Logger` 消费者之一。
- [事件](./events.zh.md) · [反射、类型与 beans](./reflection-and-beans.zh.md) ·
  [I/O、NIO、扫描与文件过滤器](./io-and-scanning.zh.md) ·
  [类加载与构件](./classloading-and-artifacts.zh.md) · [JSON](./json.zh.md) ·
  [配置属性元数据](./configuration-metadata.zh.md) ·
  [并发、进程与管理](./concurrency-process-and-management.zh.md) — 其他通过本门面输出日志、而不 import 框架的
  `core` 分组。
- [日志 — 开发者指南](../../developer-guide/microsphere-java-core/logging.zh.md) — 解析链、适配器内部实现、
  缓存/线程安全分析、设计模式以及各类图表。
