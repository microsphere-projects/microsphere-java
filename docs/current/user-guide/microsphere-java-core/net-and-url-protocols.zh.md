# 网络与 URL 协议

| | |
|---|---|
| **构件** | `io.github.microsphere-projects:microsphere-java-core`（`0.3.20-SNAPSHOT`，`release` 分支） |
| **包** | `io.microsphere.net` — 10 个主源码类型 · `io.microsphere.net.classpath` — 1 个 · `io.microsphere.net.console` — 2 个（共 **13** 个） |
| **JDK 级别** | 反应堆中**不存在**任何 `maven.compiler.*` / `<release>` 属性 —— 字节码级别从外部父 POM `microsphere-build:0.3.16` 继承；CI 在 Temurin JDK 8 / 11 / 17 / 21 / 25 上测试 |
| **依赖的其他 `core` 包** | `annotation`（`@Nonnull`/`@Nullable`/`@Immutable`）、`collection`（`ListUtils`、`SetUtils`、`MapUtils`、`Lists`、`CollectionUtils.size`）、`constants`（`SymbolConstants`、`PathConstants`、`ProtocolConstants`、`SeparatorConstants`）、`lang.Prioritized`、`logging`、`reflect`（`FieldUtils`、`ConstructorUtils`）、`text.FormatUtils`、`util`（`StringUtils`、`Assert`、`ArrayUtils`、`CharSequenceUtils`、`ClassUtils`、`ClassLoaderUtils`、`ClassPathUtils`、`ServiceLoaderUtils`、`SystemUtils`、`Utils`）、`util.jar.JarUtils`、`io.IOUtils` |
| **外部依赖** | 无 —— 纯 JDK（`java.net`、`java.io`、`java.util.ServiceLoader`） |
| **被谁使用** | `classloading`（`classloading/URLClassPathHandle.java:26` 导入了 `URLUtils.EMPTY_URL_ARRAY`）、`io.scanner`（`io/scanner/SimpleClassScanner.java:23-24`）、`util.ClassLoaderUtils`（`:46`）、`util.ClassUtils`（`:58`）、`util.jar.JarUtils`（`:37-39`） |

本页面中所有 `file:line` 引用都相对于 `microsphere-java-core/src/main/java/io/microsphere/`
（主干源码）或 `microsphere-java-core/src/test/java/io/microsphere/`（测试），读取自 `release` 工作树。
首次提及之后简写为 `net/URLUtils.java:40-55`。

---

## 1. 这一组的用途

**当你需要 JDK 并未自带的 URL scheme，并且还需要它扛住 `java.net.URL.setURLStreamHandlerFactory(...)` 每个 JVM
只能调用一次这一事实时，使用 `io.microsphere.net`。** 该包提供五个可组合而非相互冲突的
`URLStreamHandlerFactory` 实现
（`net/CompositeURLStreamHandlerFactory.java:56-135`、`net/MutableURLStreamHandlerFactory.java:64-139`、
`net/DelegatingURLStreamHandlerFactory.java:56-84`、`net/StandardURLStreamHandlerFactory.java:56-98`、
`net/ServiceLoaderURLStreamHandlerFactory.java:61-92`），外加你真正应当调用的那个入口：
`URLUtils.attachURLStreamHandlerFactory(factory)`（`net/URLUtils.java:1365-1389`），以及它的 SPI 风格快捷方式
`ServiceLoaderURLStreamHandlerFactory.attach()`（`net/ServiceLoaderURLStreamHandlerFactory.java:71-74`）。

**当一个 scheme 需要扇出到多种方言时，使用 `ExtendableProtocolURLStreamHandler`
（`net/ExtendableProtocolURLStreamHandler.java:132-420`）。** 它通过把子协议在解析期改写成一个矩阵参数
（`;_sp=mysql`）（`:328-337`、`:370-392`），再分发给第一个既 `supports(...)` 又能产出非空连接的
`SubProtocolURLConnectionFactory`（`:271-286`），从而为 URL 增加*子协议* ——
`jdbc:mysql://host/db`、`console:text://host:12345/abc`。开箱即用的处理器有两个：
`classpath:`（`net/classpath/Handler.java:40-64`）与 `console:`（`net/console/Handler.java:32-38` +
`net/console/ConsoleURLConnection.java:35-60`）。

**URL/URI 字符串操作中那些 `java.net.URL` 拒绝承担的部分，使用 `URLUtils`（`net/URLUtils.java:90-1724`）**：
矩阵参数（`:362-381`）、子协议抽取（`:1046-1050`、`:1131-1144`）、`jar:`/`file:` URL 的归档条目路径与归档文件
解析（`:211-220`、`:274-301`）、与 JDK 等价且保留矩阵段的 `toExternalForm`（`:932-1016`），以及对 JDK 私有全局
工厂字段的反射读写（`:1423-1426`、`:1641-1643`）。

> [!IMPORTANT]
> 仅仅把 jar 放到 classpath 上，这一组中的任何东西都不会自我注册。`microsphere-java-core` 的主资源中**没有**
> `META-INF/services/java.net.URLStreamHandlerFactory` —— 该文件只存在于本模块的**测试**资源里
> （`src/test/resources/META-INF/services/java.net.URLStreamHandlerFactory`，指向
> `io.microsphere.net.StandardURLStreamHandlerFactory`）。主资源确实声明了
> `META-INF/services/io.microsphere.net.ExtendableProtocolURLStreamHandler`，内容是 `classpath` 和 `console` 两个处理器。
> **你必须在启动时调用一次 `ServiceLoaderURLStreamHandlerFactory.attach()`** —— 见 §7。

---

## 2. 坐标与依赖

不需要额外的构件，也不需要任何第三方库：全部 13 个类型只针对 `java.net` 与 `java.io` 编译。

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

---

## 3. 版本信息与兼容性

| 事实 | 值 | 证据（`release`） |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | 根 `pom.xml:12` |
| `artifactId` | `microsphere-java-core` | 模块 `pom.xml` |
| 版本属性 | `${revision}`，扁平化处理 | 根 `pom.xml` |
| `release` 上 `${revision}` 的值 | `0.3.20-SNAPSHOT` | 根 `pom.xml:54` |
| 最新已发布标签 | `0.3.20` | 分组索引中的发布线表（`docs/current/user-guide/microsphere-java-core/README.md:66-72`） |
| 导入的 BOM | `microsphere-all-bom:0.3.13` | `microsphere-java-parent/pom.xml:22`、`:33-36` |
| Spring BOM 导入 | `spring-framework-bom` | `microsphere-java-parent/pom.xml:40-46` |
| Spring Framework，JDK 17+ | `7.0.9` | `microsphere-java-parent/pom.xml:23` |
| Spring Framework，JDK `[1.8,17)` | `5.3.39`，通过 profile `java8-16` | `microsphere-java-parent/pom.xml:54-62`（激活条件 `<jdk>[1.8,17)</jdk>` 在 `:57`） |
| CI 矩阵 | Temurin JDK `8, 11, 17, 21, 25` | `.github/workflows/maven-build.yml:25`，`distribution: 'temurin'` 在 `:33` |
| 字节码级别 | 从外部父 POM `microsphere-build:0.3.16` 继承；反应堆不声明任何 `maven.compiler.*` / `<release>` 属性 | 反应堆各 POM |
| 这一组的兼容范围 | JDK 8 到 JDK 25（含两端）—— 但**机制随 JDK 而变**，见 §7.4 | `net/StandardURLStreamHandlerFactory.java:66-85` |

> [!NOTE]
> 这一组中的每个类型都在 javadoc 上打了 `@since 1.0.0` 标签（`net/URLUtils.java:88`、
> `net/ExtendableProtocolURLStreamHandler.java:130`、`net/classpath/Handler.java:38`、
> `net/console/ConsoleURLConnection.java:33`）。该标签是一个**源码级**标记，与本文档所记录的 Maven
> `0.3.x` 发布线无关；不要把它读成"在 1.x 中已被移除"—— 并不存在 `1.x` 线。


### 3.1 `0.3.20` 的发布日期与变更摘要

| 条目 | 值 | 依据 |
|---|---|---|
| 文档所在线 | `release` 分支，`${revision}` = `0.3.20-SNAPSHOT` | 根 `pom.xml:54` |
| 最新已发布版本 | `0.3.20`，标签日期 **2026-10-16** | 标签 `0.3.20` 的提交信息为 `chore: merge main into release [skip ci]`，提交时间 `2026-10-16T03:43:20Z` |
| 上一版本 | `0.3.19`，标签日期 **2026-08-30** | 标签 `0.3.19`，提交时间 `2026-08-30T12:51:37Z` |
| 变更日志来源 | 仓库根目录的 `release-notes.md` | 本项目不发布 `CHANGELOG.md`；`release-notes.md` 的 `v0.3.16`–`v0.3.20` 段落写着 *Release notes generation failed* 并保留原始提交列表，因此下文一律以提交主题引用，并逐条与 `release` 源码核对。 |

`0.3.20` 在这一区域没有新增 API 面——`0.3.19...0.3.20` 区间是一次加固性发布。它实际改变了什么、以及这些改动触及本页源码的程度如下：

* **空值契约。** 本页覆盖范围内 3 / 13 个主源码在签名上声明了 `@Nonnull` / `@Nullable`。没有单独点名本区域的提交，由全库性的 `34e27823 Refine nullability contracts across core APIs` 覆盖。
* **空输入与行为修复。** `574364c7 Handle null URL components safely` / `ffa038ae Harden URLUtils close() unit tests`。

> [!NOTE]
> 如何读这份提交清单：该区间没有以 `io.microsphere.net` 点名的提交，但 13 个源码中已有 3 个带注解——全库性的契约改造在未单独提交的情况下覆盖了它。

---

## 4. `classpath:` URL

### 4.1 API

`io.microsphere.net.classpath.Handler`（`net/classpath/Handler.java:40`）是一个名为 `Handler` 的顶层类，
声明在包 `io.microsphere.net.classpath` 中，并且它只覆写了
`openConnection(URL, Proxy)`（`:43-63`）。由于它使用继承来的无参构造函数，它的协议名由
**其自身包名的最后一段** —— `classpath` —— 推导而来，推导地点是
`ExtendableProtocolURLStreamHandler#resolveConventionProtocol`（`net/ExtendableProtocolURLStreamHandler.java:174-177`）
与 `getProtocol()`（`:412-414`）。

解析规则，按顺序（`net/classpath/Handler.java:44-62`）：

| 步骤 | 代码 | 效果 |
|---|---|---|
| 1 | `u.getAuthority()` + `u.getPath()`（`:44-45`） | 两部分都会被读取 |
| 2 | `if (isNotEmpty(authority)) classPath = authority + path;`（`:49-51`） | authority 与 path 被**拼接**，因此斜杠的切分点无关紧要 |
| 3 | `while (classPath.indexOf(SLASH_CHAR) == 0) substring(1)`（`:53-55`） | 每个前导 `/` 都被剥掉 —— `classpath:////x/y` → `x/y` |
| 4 | `getClassLoader(Handler.class).getResource(classPath)`（`:57-58`） | 用的是加载 **microsphere core** 的那个加载器，而不是线程上下文加载器 |
| 5 | `throw new IOException("No Resource[classpath='" + classPath + "'] was not found!")`（`:59-61`） | 资源缺失是硬失败，*不是*静默降级 |
| 6 | `return url.openConnection()`（`:62`） | 返回的连接是真正的 `file:`/`jar:` 连接 —— 不涉及 `DelegatingURLConnection` |

### 4.2 示例

改写自 `net/classpath/HandlerTest.java:42-46` 与 `net/ServiceLoaderURLStreamHandlerFactoryTest.java:71-76`：

```java
ServiceLoaderURLStreamHandlerFactory.attach();                 // ServiceLoaderURLStreamHandlerFactoryTest.java:47, :73

String url = "classpath://META-INF/test.properties";           // HandlerTest.java:42
assertEquals("name = 测试名称", IOUtils.toString(new URL(url).openStream(), "UTF-8"));   // :75

// the authority/path split does not matter — this four-slash form resolves the same resource  (HandlerTest.java:44)
URL other = new URL("classpath:////META-INF/services/io.microsphere.event.EventListener");
assertNotNull(other.openConnection());                          // exercises Handler.java:49-55
```

_Source: net/classpath/HandlerTest.java:42-46, net/ServiceLoaderURLStreamHandlerFactoryTest.java:71-76_

资源缺失时会在打开阶段抛出 —— `HandlerTest.java:58-62` 断言的正是这一点：

```java
URL url = new URL("classpath://META-INF/not-found.res");        // :46
assertThrows(IOException.class, url::openStream);               // :61
```

_Source: net/classpath/HandlerTest.java:46, :58-62_

---

## 5. `console:` URL

### 5.1 API

`io.microsphere.net.console.Handler`（`net/console/Handler.java:32`）是尽可能最小的子类：它覆写
`openConnection(URL, Proxy)` 并返回一个新的 `ConsoleURLConnection`（`:35-37`）。没有注册任何子协议工厂，
因此 `console:` 处理器会*直接*应答**任何**子协议。

`ConsoleURLConnection`（`net/console/ConsoleURLConnection.java:35`）继承 `java.net.URLConnection` 并实现
三方法契约：

| 方法 | 方法体 | 证据 |
|---|---|---|
| `ConsoleURLConnection(URL url)` | 只有 `super(url)` —— 不开 socket，不开流 | `net/console/ConsoleURLConnection.java:43-45` |
| `connect()` | 空 —— 连接永远处于"已打开"状态 | `:47-49` |
| `getInputStream()` | `System.in`（`java.lang.System.in`，静态导入 `:26`） | `:51-54` |
| `getOutputStream()` | `System.out`（静态导入 `:27`） | `:56-59` |

这是本库对"`URLConnection` 完全可以不碰网络"的经典演示：它让 `Scanner` 风格的代码、`URLUtils.close(...)`
以及流的拼接可以把标准 I/O 当作一个资源 URL 来对待。

### 5.2 示例

取自 `net/console/ConsoleURLConnectionTest.java:23-43`：

```java
Handler handler = new Handler();                                                   // :25
ConsoleURLConnection connection = (ConsoleURLConnection) handler.openConnection(        // :26
        new URL("console://localhost:12345/abc"), NO_PROXY);

connection.connect();                                 // no-op, must not throw   — :31
assertSame(System.in, connection.getInputStream());   // :32, :37
assertSame(System.out, connection.getOutputStream()); // :42
```

_Source: net/console/ConsoleURLConnectionTest.java:23-43_

当处理器所在包已被发布到 `java.protocol.handler.pkgs` 之后，同一个 URL 也能在 JDK 的查找中往返成功
（`net/console/HandlerTest.java:38-44`、`:62-69`）：

```java
public static final String TEST_CONSOLE_URL                = "console://localhost:12345/abc";           // :38
public static final String TEST_CONSOLE_URL_WITH_SP        = "console:text://localhost:12345/abc";      // :40
public static final String TEST_CONSOLE_URL_WITH_SP_PARAMS = TEST_CONSOLE_URL_WITH_SP + ";ref=top?n=1"; // :42
// :63 — url.openStream() and handler.openConnection(url, NO_PROXY).getInputStream() are the SAME object
```

_Source: net/console/HandlerTest.java:38-44, :61-69_

---

## 6. 自定义与可扩展协议处理器

### 6.1 两个构造函数，以及它们所强制的约定

| 构造函数 | 行为 | 证据 |
|---|---|---|
| `ExtendableProtocolURLStreamHandler()` | 断言三条约定，然后从包名推导协议，**并且**把父包追加到 `java.protocol.handler.pkgs` 系统属性 | `net/ExtendableProtocolURLStreamHandler.java:146-151`、`:179-210`、`:212-235` |
| `ExtendableProtocolURLStreamHandler(String protocol)` | 完全信任你：不断言，**也不注册属性** | `:153-155` |

无参路径所检查的约定（`:185-210`，全部是 `static` 且包可见，以便测试可以直接驱动）：

1. 该类必须是顶层类 —— 局部类、匿名类和成员类都会被拒绝（`:185-189`）；
2. 简单类名必须正好是 `Handler`（`:191-196`，对照 `URLUtils.HANDLER_CONVENTION_CLASS_NAME`，`net/URLUtils.java:149`）；
3. 该类不能位于默认/内置包 `sun.net.www.protocol`（`:198-210`，前缀取自 `net/URLUtils.java:139`），也不能位于未命名的顶层包（`:203-205`）。

违反约定会抛出 `IllegalArgumentException` —— 这是除 `classpath:` 的"未找到"情形之外，本组唯一的有意不降级路径。

### 6.2 子协议：改写与分发

`parseURL` 是 `final` 的（`:328-337`）。当协议之后 spec 中仍包含 `://` 时，二者之间的子协议文本会被提升为一个名为
`_sp` 的矩阵参数（`URLUtils.SUB_PROTOCOL_MATRIX_NAME`，`net/URLUtils.java:154`），余下部分交给 `super.parseURL`。
因此 `jdbc:mysql://localhost:3307/mydb?charset=UTF-8#top` 会变成
`jdbc://localhost:3307/mydb;_sp=mysql?charset=UTF-8#top`（`:340-368` javadoc，实现 `:370-392`）。

`openConnection(URL, Proxy)`（`:271-286`）遍历已排序的工厂列表，取**第一个**既 `supports(u, subProtocols)`
又返回非 `null` 连接的工厂；其余情况落到 `openFallbackConnection(u, p)`，而它**默认返回 `null`**（`:297-299`）。

| 成员 | 角色 | 证据 |
|---|---|---|
| `init()` | 按 `Prioritized.COMPARATOR` 排序工厂，并注册处理器自身 | `:237-247` |
| `initSubProtocolURLConnectionFactories(List)` | protected 扩展钩子，默认为空 | `:254-255` |
| `customizeSubProtocolURLConnectionFactories(Consumer)` | 构造之后对活动列表做变更 | `:262-264` |
| `resolveSubProtocols(URL)` / `resolveAuthority(URL)` / `resolvePath(URL)` | 基于 `URLUtils` 的 `protected` 便捷方法 | `:400-410` |
| `equals`、`hashCode`、`hostsEqual`、`toExternalForm`、`openConnection(URL)`、`parseURL` | `final` —— 一旦被覆写，对所有子类生效 | `:266-269`、`:301-326`、`:328-337` |

`SubProtocolURLConnectionFactory` 自身是一个两方法契约（`net/SubProtocolURLConnectionFactory.java:68-92`）：
`supports(URL, List<String>)` 与 `create(URL, List<String>, Proxy) throws IOException`。
`CompositeSubProtocolURLConnectionFactory`（`net/CompositeSubProtocolURLConnectionFactory.java:76-152`）聚合它们，
每次 `add`/`remove` 之后重新排序（`:90-122`），并挑选第一个支持者（`:135-151`）。

### 6.3 示例

测试夹具处理器是最小的合法子类（`net/test/Handler.java:27-37`），而测试证明了整个闭环
（`net/ExtendableProtocolURLStreamHandlerTest.java:63-72`、`:157-172`、`:186-198`、`:232-235`）：

```java
// net/test/Handler.java:27 — top-level, named Handler, package io.microsphere.net.test ⇒ protocol "test"
public class Handler extends ExtendableProtocolURLStreamHandler {
    public Handler() { super(); }                       // :29-31
    public Handler(String protocol) { super(protocol); } // :33-35
}
// ExtendableProtocolURLStreamHandlerTest.java
private static final String TEST_URL          = "test://localhost:12345/abc";        // :63
private static final String TEST_URL_WITH_SP  = "test:text://localhost:12345/abc";   // :65
// 1. no factory registered ⇒ URL.openConnection() is null (fallback default), and the URL string is untouched
URL url = new URL(TEST_URL);
assertNull(url.openConnection());                     // :159
assertEquals(TEST_URL, url.toString());               // :160
// 2. a supporting factory that returns null still falls through
handler.customizeSubProtocolURLConnectionFactories(factories -> {
    factories.add(0, new FalseSubProtocolURLConnectionFactory());   // supports() == false   :168
    factories.add(1, new NullSubProtocolURLConnectionFactory());    // create() == null      :169
});
assertNull(handler.openConnection(url));              // :171
// 3. a real factory makes the sub-protocol URL work
handler.customizeSubProtocolURLConnectionFactories(f -> f.add(new ConsoleSubProtocolURLConnectionFactory())); // :187-189
assertSame(url.openConnection(NO_PROXY).getInputStream(), handler.openConnection(new URL(TEST_CONSOLE_URL_WITH_SP), NO_PROXY).getInputStream()); // :196
// 4. sub-protocols are readable
assertEquals(ofList("text"), handler.resolveSubProtocols(new URL(TEST_URL_WITH_SP))); // :234
```

_Source: net/test/Handler.java:27-37, net/ExtendableProtocolURLStreamHandlerTest.java:63-72, :157-172, :186-198, :232-235_

直接组合工厂，取自 `net/CompositeSubProtocolURLConnectionFactoryTest.java:46-95`：

```java
CompositeSubProtocolURLConnectionFactory composite = new CompositeSubProtocolURLConnectionFactory();  // :48
ConsoleSubProtocolURLConnectionFactory instance = new ConsoleSubProtocolURLConnectionFactory();        // :54
composite.add(instance);                                                                               // :55
URL url = new URL("ftp://...");                                                                        // :57
List<String> subProtocols = emptyList();
if (composite.supports(url, subProtocols)) {                                                          // :59
    URLConnection c = composite.create(url, subProtocols, NO_PROXY);                                  // :60
    assertEquals(ConsoleURLConnection.class, c.getClass());                                           // :61
}
composite.remove(instance);
assertFalse(composite.supports(url, subProtocols));                                                   // :65
composite.add(instance, instance);                                                                    // varargs add de-duplicates (:96-109)
composite.remove(instance);
assertFalse(composite.supports(url, subProtocols));                                                   // :69
assertFalse(composite.remove(null));                                                                  // :75
```

_Source: net/CompositeSubProtocolURLConnectionFactoryTest.java:46-95_

---

## 7. 注册处理器：五个工厂与 `attach()`

### 7.1 哪个工厂对应哪项工作

| 类 | 类别 | 关键成员 | `null` / 失败策略 |
|---|---|---|---|
| `ServiceLoaderURLStreamHandlerFactory`（`net/ServiceLoaderURLStreamHandlerFactory.java:61`） | **入口点**；继承 `DelegatingURLStreamHandlerFactory` | `public static void attach()`（`:71-74`）、`static Map<String, ExtendableProtocolURLStreamHandler> loadHandlers()`（`:84-91`） | 没有 SPI ⇒ 空列表，不抛异常 |
| `CompositeURLStreamHandlerFactory`（`net/CompositeURLStreamHandlerFactory.java:56`） | 工厂链，带优先级 | `final createURLStreamHandler(String)`（`:74-86`）、`addURLStreamHandlerFactory`（`:94-107`）、`protected getFactories()`/`getComparator()`（`:114-125`） | `null` 工厂和 `this` 被忽略（`:95`）；嵌套的 composite 会被**扁平化**（`:97-100`）；重复项由 `contains` 丢弃（`:101`） |
| `MutableURLStreamHandlerFactory<H>`（`net/MutableURLStreamHandlerFactory.java:64`） | 协议 → 处理器注册表 | `addURLStreamHandler`（`:91-94`）、`removeURLStreamHandler`（`:102-104`）、`getURLStreamHandler`（`:112-114`）、`getHandlers()`（`:121-123`）、`clearHandlers()`（`:135-138`） | 未知协议 ⇒ `null`；文档明确声明**非线程安全**（`:53-56`） |
| `DelegatingURLStreamHandlerFactory`（`net/DelegatingURLStreamHandlerFactory.java:56`） | 单委托包装器 | 构造函数用 `assertNotNull` 保护（`:66-69`）、`protected final getDelegate()`（`:81-83`） | `null` 委托 ⇒ `IllegalArgumentException`（`:67`） |
| `StandardURLStreamHandlerFactory`（`net/StandardURLStreamHandlerFactory.java:56`） | 把 JDK 自身的协议重新暴露为一个工厂 | `createURLStreamHandler(String)`（`:70-73`）、包可见的测试接缝 `createURLStreamHandler(Field, String)`（`:75-85`） | 未知协议 ⇒ `null` —— 但见陷阱 7 |

### 7.2 `attachURLStreamHandlerFactory` —— 唯一该发出的调用

`URLUtils.attachURLStreamHandlerFactory(factory)`（`net/URLUtils.java:1365-1389`）的存在是因为 JDK 的那个槽位
只可写一次。它的三个分支：

| 情形 | 动作 | 行号 |
|---|---|---|
| `factory == null` | 立即返回（无操作） | `:1366-1368` |
| 尚无全局工厂 | `URL.setURLStreamHandlerFactory(factory)` 并返回 | `:1373-1375` |
| 全局工厂已经是一个 `CompositeURLStreamHandlerFactory` | 追加到它 | `:1376-1377`、`:1388` |
| 装着一个外来的工厂 | 把旧+新包进一个新 composite，**反射式地把 `URL.factory` 置空**，安装 composite，然后追加 | `:1378-1385` |

```java
URLStreamHandlerFactory factory = new StandardURLStreamHandlerFactory();
attachURLStreamHandlerFactory(factory);
assertSame(factory, getURLStreamHandlerFactory());                    // URLUtilsTest.java:557 — installed directly
attachURLStreamHandlerFactory(factory);                               // second call must NOT hit the JDK's write-once slot
CompositeURLStreamHandlerFactory composite = (CompositeURLStreamHandlerFactory) getURLStreamHandlerFactory();
assertNotSame(factory, composite);                                    // :561
assertEquals(1, composite.getFactories().size());                     // :562 — the duplicate was rejected by contains()
assertSame(factory, composite.getFactories().get(0));                 // :563
assertEquals(CompositeURLStreamHandlerFactory.class, composite.getClass()); // :564
attachURLStreamHandlerFactory(null);                                  // :569 — silent no-op
```

_Source: net/URLUtilsTest.java:553-571_

对测试而言，`clearURLStreamHandlerFactory()`（`net/URLUtils.java:1641-1643`，`protected`）通过
`FieldUtils.setStaticFieldValue(true, URL.class, "factory", null)` 重置槽位；`URLUtilsTest.java:149-152`
在 `@AfterEach` 中运行它。

### 7.3 不借助工厂注册单个处理器

```java
Handler handler = new io.microsphere.net.console.Handler();     // URLUtilsTest.java:580
registerURLStreamHandler(handler);                             // :581 → URLUtils.java:1461-1463
URL url = ofURL("console://localhost:12345/abc");              // :582, console/HandlerTest.java:38
assertSame(System.in, url.openStream());                       // :583 — the global factory now answers "console"
```

_Source: net/URLUtilsTest.java:578-584_

机制上，`registerURLStreamHandler(String, URLStreamHandler)`（`:1502-1506`）取得或创建一个
`MutableURLStreamHandlerFactory`（`getMutableURLStreamHandlerFactory(true)`，`:1614-1624`），把处理器按其协议放入，
然后 attach 该工厂。不带该标志的 `getMutableURLStreamHandlerFactory()` 在什么都没挂载时返回 `null`
（`:1610-1612`，由 `URLUtilsTest.java:607-611` 断言），且查找会先搜当前工厂、再搜 composite 的子成员
（`:1626-1639`，在 `:613-618` 与 `:626-633` 处断言）。

### 7.4 哪种技术跑在哪个 JDK 上

| JDK | 这一组实际使用的技术 | 证据 |
|---|---|---|
| 8 | `StandardURLStreamHandlerFactory` 找不到 `URL.defaultFactory`（`findField` ⇒ `null`），于是它拼出类名 `sun.net.www.protocol.<protocol>.Handler`，解析该类并反射实例化 | `net/StandardURLStreamHandlerFactory.java:68`、`:75-85`、`:87-93`；`net/URLUtils.java:139` |
| 9+ | 私有静态 `URL.defaultFactory` 字段**存在**，因此读出 JDK 自身的工厂对象并委托给它 | `net/StandardURLStreamHandlerFactory.java:66-68`（"`URL#defaultFactory` static field since JDK 9+"）、`:94-96` |
| 所有 JDK | `java.protocol.handler.pkgs` —— JDK 自己的搜索属性 —— 每当通过无参构造函数创建 `ExtendableProtocolURLStreamHandler` 时都会被**追加**处理器的父包；该常量及其文档化语义位于 `net/URLUtils.java:120-134` | `net/ExtendableProtocolURLStreamHandler.java:212-235`、`:170-172` |
| 所有 JDK | `URL.setURLStreamHandlerFactory` 每 JVM 一次 —— 通过反射读写 `URL.factory` 并做组合来绕过 | `net/URLUtils.java:1365-1389`、`:1423-1426`、`:1641-1643` |
| 服务加载 | 本发布版**不使用** `URLStreamHandlerProvider`（Java 9+ 的 `java.net.spi`）：注册全部通过上述工厂 + 系统属性路径完成 | `net` 源码中不存在任何 `URLStreamHandlerProvider` 引用 |

> [!WARNING]
> 反射路径会读写 `java.net.URL` 的私有静态字段。在 JDK 16+ 上，强封装会让 `java.base/java.net` 上的
> `setAccessible(true)` 失败，除非 JVM 以 `--add-opens java.base/java.net=ALL-UNNAMED` 启动。本库不会替你加这个
> 标志；本组自身的构建只为测试 JVM 提供了等价的开放项。当你*在*另一个库已经安装好工厂之后才去 attach 一个工厂时，
> 请把这一点纳入预算（§7.2，第四行）。

---

## 8. URL 与 URI 工具（`URLUtils`）

`URLUtils` 是 `public abstract class ... implements Utils`，构造函数是 `private` 的
（`net/URLUtils.java:90`、`:1722-1723`）—— 一个纯粹的静态门面。

### 8.1 值得了解的常量

| 常量 | 值 / 来源 | 行号 |
|---|---|---|
| `DEFAULT_ENCODING` | `SystemUtils.FILE_ENCODING`，即 `file.encoding` 系统属性 —— **不是**硬编码的 `UTF-8` | `:97`（`util/SystemUtils.java:332`） |
| `EMPTY_URL_ARRAY` | 别名指向 `ArrayUtils.EMPTY_URL_ARRAY` | `:103` |
| `FILE_URL_PREFIX` | `"file:/"` | `:113` |
| `HANDLER_PACKAGES_PROPERTY_NAME` | `"java.protocol.handler.pkgs"` | `:134` |
| `DEFAULT_HANDLER_PACKAGE_PREFIX` | `"sun.net.www.protocol"` | `:139` |
| `HANDLER_PACKAGES_SEPARATOR_CHAR` | `'|'` | `:144` |
| `HANDLER_CONVENTION_CLASS_NAME` | `"Handler"` | `:149` |
| `SUB_PROTOCOL_MATRIX_NAME` | `"_sp"` | `:154` |

### 8.2 API 参考（节选）

| 方法 | 契约 | 证据 |
|---|---|---|
| `ofURL(String)` | `@Nonnull`；把**任何**异常包装成 `IllegalArgumentException` | `:179-186` |
| `resolveArchiveEntryPath(URL)` | `@Nullable` —— 不含 `!/` 时返回 `null`；会解码条目路径 | `:211-220` |
| `resolveBasePath(URL)` / `resolvePath(URL)` | `@Nonnull`；base 丢弃归档条目，完整路径保留它 | `:244-248`、`:1315-1318` |
| `resolveArchiveFile(URL)` | `@Nullable`；`file:` → 归档目录搜索，否则做存在性检查 | `:274-301` |
| `resolveQueryParameters(String)` / `resolveMatrixParameters(String)` | `@Nonnull @Immutable` 的 `Map<String, List<String>>`，不可修改 | `:322-327`、`:362-381` |
| `normalizePath(String)` | `@Nullable`；空白输入原样返回；`\`→`/`，`//`→`/` | `:402-420` |
| `encode`/`decode(String[, encoding])` | `@Nonnull`；**所有**失败都以 `IllegalArgumentException` 浮出 | `:456-459`、`:497-506`、`:542-545`、`:581-590` |
| `isDirectoryURL(URL)` | `null` ⇒ `false` | `:622-635` |
| `isJarURL(URL)` / `isArchiveURL(URL)` | `null` ⇒ `NullPointerException` | `:667-682`、`:719-725` |
| `isArchiveProtocol(String)` | `jar`/`zip`/`war`/`ear`，区分大小写 | `:754-757` |
| `buildURI(String...)` | `@Nonnull`；空/`null` 数组 ⇒ `"/"` | `:788-809` |
| `buildMatrixString(Map)` | `@Nullable` | `:841-856` |
| `buildMatrixString(String, String...)` | 标注了 `@Nonnull` 却可能返回 `null` —— 见陷阱 5 | `:886-889`、`:1693-1698` |
| `toExternalForm(URL)` | `@Nonnull`；重建 protocol、`//authority`、path、matrix、query、fragment | `:932-1016` |
| `getSubProtocol(String)` / `resolveSubProtocols(URL\|String)` | 第一个 `_sp` 值 / 完整的不可修改列表 | `:1046-1050`、`:1085-1089`、`:1131-1144` |
| `resolveProtocol(String)` | `@Nullable`；冒号前存在空白 ⇒ `null` + trace 日志 | `:1183-1201` |
| `resolveAuthority(URL\|String)` | 截断矩阵段之后的 authority | `:1247-1249`、`:1282-1285` |
| `close(URLConnection)` | 只调用 `HttpURLConnection.disconnect()`；其余（含 `null`）一律忽略 | `:1528-1532` |

### 8.3 示例

```java
// parsing — URLUtilsTest.java
assertEquals("%2Fabc%2Fdef", encode("/abc/def"));                    // :291
assertEquals("/abc/def", decode("%2Fabc%2Fdef"));                     // :311
assertThrows(IllegalArgumentException.class, () -> encode("/abc/def", "unknown-encoding")); // :306
// matrix vs query parameters — net/URLUtilsTest.java:207-257
String queryURL = "http://localhost/?q=java&oq=java&sourceid=chrome&es_sm=122&ie=UTF-8";  // :121, :125
Map<String, List<String>> q = resolveQueryParameters(queryURL);
assertEquals(ofList("java"), q.get("q"));                                                  // :212
assertEquals(5, q.size());                                                                 // :211-218
Map<String, List<String>> m = resolveMatrixParameters("http://localhost/;q=java;oq=java");  // :127, :233-242
assertEquals(ofList("java"), m.get("oq"));                                                  // :237
// paths — :259-287, :419-435, :658-685
assertEquals("/abc/", normalizePath("//abc///"));                     // :278-281
assertEquals("/a/b/c", buildURI("a", "b", "c"));                      // :421-422
assertEquals("/", buildURI());                                        // :430-431
assertEquals("D:/test", resolvePathFromFile(ofURL("file:///D:/test"), true));  // :671-672
assertEquals("/dir/x.jar", resolvePathFromJar(ofURL("file:/dir/x.jar"), false)); // :664-666
// sub-protocols — :499-526
assertEquals("text", getSubProtocol("http://localhost/;_sp=text;_sp=properties")); // :501
assertEquals(ofList("text", "properties"), resolveSubProtocols(ofURL("http://localhost/;_sp=text;_sp=properties"))); // :511-515
new io.microsphere.net.console.Handler();                             // :520 — publishes io.microsphere.net
assertEquals(2, resolveSubProtocols(ofURL("console:text:properties://localhost")).size()); // :521-525
// round-trip against the JDK — :456-497
assertEquals(url.toExternalForm(), toExternalForm(url));              // :496
```

_Source: net/URLUtilsTest.java:109-131, :207-291, :306-322, :419-454, :456-526, :528-539, :658-691_

---

## 9. 包装既有连接：`DelegatingURLConnection`

`net/DelegatingURLConnection.java:43-261` 继承 `URLConnection`，并把 **42** 个方法（`:52-260`）转发给委托对象 ——
超时、内容元数据、全部五个 header 访问器、content、permission、两个流、`doInput`/`doOutput`/`useCaches`/
`allowUserInteraction`/`ifModifiedSince`/`defaultUseCaches` 这些标志位成对方法，以及四个请求属性方法。`toString()`
同样转发（`:177-180`）。这个类就是你要继承的基类，用以在不重新实现的前提下拦截 `jar:`/`file:`/`http:` 连接的某个行为；
与 `collection` 的 `Delegating*` 家族不同，它没有实现任何 `DelegatingWrapper` 标记，也不暴露 `getDelegate()` ——
委托字段是 `private final` 的（`:45`）。

```java
URL baseURL = getClass().getProtectionDomain().getCodeSource().getLocation();     // DelegatingURLConnectionTest.java:65
URL url = new URL(baseURL.toString() + "META-INF/test.properties");               // :66
URLConnection delegate = url.openConnection();                                    // :67
DelegatingURLConnection connection = new DelegatingURLConnection(delegate);       // :68
connection.connect();                                        // no-op for file:, must not throw   — :77
assertEquals(19, connection.getContentLength());              // :112
assertEquals("content/unknown", connection.getContentType()); // :122
assertEquals("19", connection.getHeaderField("content-length")); // :148
assertSame(System.out, connection.getOutputStream());         // after attach() on a console: URL   — :216-220
assertDoesNotThrow(() -> connection.setRequestProperty("key-1", "value-1")); // :305
```

_Source: net/DelegatingURLConnectionTest.java:63-69, :76-79, :110-150, :200-221, :301-312_

---

## 10. 常见陷阱

1. **`attach()` 不是自动的。** 加载 `microsphere-java-core` 不会注册任何东西；在
   `ServiceLoaderURLStreamHandlerFactory.attach()`（`net/ServiceLoaderURLStreamHandlerFactory.java:71-74`）或
   `registerURLStreamHandler(...)`（`net/URLUtils.java:1461-1506`）运行之前，`new URL("classpath://…")` 都会以
   `MalformedURLException`（"unknown protocol"）失败。
2. **在这里 `URL.openConnection()` 返回 `null` 是合法的，在 JDK 里则不合法。** `openFallbackConnection` 默认返回 `null`
   （`net/ExtendableProtocolURLStreamHandler.java:297-299`），测试也对此做了断言
   （`net/ExtendableProtocolURLStreamHandlerTest.java:159`、`net/test/HandlerTest.java:57-59`）；因此
   `new URL("test://x").openStream()` 抛的是 `NullPointerException`，不是 `IOException`。请覆写 `openFallbackConnection`。
3. **要子类化 `openConnection(URL, Proxy)`，绝不要子类化 `openConnection(URL)`。** 单参数形式是 `final` 的
   （`net/ExtendableProtocolURLStreamHandler.java:266-269`），它以 `Proxy.NO_PROXY` 委托给双参数形式。
   同样的限制适用于 `equals`、`hashCode`、`hostsEqual`、`toExternalForm` 与 `parseURL`（`:301-337`）。
4. **`java.protocol.handler.pkgs` 这一招只在无参构造函数上发生。** 使用
   `ExtendableProtocolURLStreamHandler(String protocol)`（`net/ExtendableProtocolURLStreamHandler.java:153-155`）会同时跳过
   约定断言与 `appendHandlerPackage`（`:212-235`），因此 JDK 自己的查找永远找不到你的
   类 —— 你必须 attach 一个工厂。
5. **`buildMatrixString(String, String...)` 标注 `@Nonnull` 却返回 `null`。** 该重载
   （`net/URLUtils.java:886-889`）委托给 `buildString`，后者对空或 `null` 的值数组返回 `null`（`:1693-1698`）；
   它自己的 javadoc 甚至打印出 `null`（`:874-879`）。请使用 `Map` 重载，它是老老实实的
   `@Nullable`（`:841-842`），或者对结果加防护。
6. **同一个类内部 null 策略不统一。** `isDirectoryURL(null)` ⇒ `false`（`net/URLUtils.java:622-625`）、
   `close(null)` ⇒ 无操作（`:1528-1532`）、`attachURLStreamHandlerFactory(null)` ⇒ 无操作（`:1366-1368`）—— 但
   `isJarURL(null)` 与 `isArchiveURL(null)` 抛 `NullPointerException`（`:668`、`:720`；在
   `net/URLUtilsTest.java:379-382` 断言），而 `ofURL` 对无法解析的字符串抛 `IllegalArgumentException`（`:180-186`；在
   `:159-162` 断言）。
7. **`StandardURLStreamHandlerFactory` 对 JDK 字段的解引用没有 null 防护。** 它检查了 `Field`
   （`net/StandardURLStreamHandlerFactory.java:88-93`），随后却对读到的值调用 `factory.createURLStreamHandler(protocol)`
   （`:94-95`）。凡读取降级为 `null` 的地方，该调用抛 `NullPointerException`，而不是落到
   JDK 8 的类名路径（`:77-83`）。要么捕获它，要么安装一个同时持有 `MutableURLStreamHandlerFactory` 的 composite。
8. **除非工厂实现 `Prioritized`，否则工厂顺序就是插入顺序。** `CompositeURLStreamHandlerFactory.getComparator()`
   返回 `Prioritized.COMPARATOR`（`net/CompositeURLStreamHandlerFactory.java:123-125`），而该比较器对两个非
   `Prioritized` 对象返回 `0`（`lang/Prioritized.java:61-76`），`Collections.sort` 会把这理解为"保持顺序"。随库发布的
   工厂没有一个实现 `Prioritized`，因此由 SPI 顺序决定。请记住 `MAX_PRIORITY` 是 `Integer.MIN_VALUE`
   （`lang/Prioritized.java:81`）—— 越小代表越高。
9. **`MutableURLStreamHandlerFactory` 不是线程安全的，而且它的 `getHandlers()` 视图是活动的。**
   文档见 `net/MutableURLStreamHandlerFactory.java:53-56`；`getHandlers()` 返回 `handlers.values()`
   （`:121-123`），所以修改它就等于修改注册表。`addURLStreamHandler` 从不校验协议（`:91-94`），因此
   `null` 键会被接受。
10. **`classpath:` 是相对 microsphere 自己的类加载器解析的。**
    `getClassLoader(Handler.class)`（`net/classpath/Handler.java:57`）是加载了 `core` 的那个加载器，不是线程上下文
    加载器；只对子加载器可见的资源无法解析。未命中是 `IOException`（`:59-61`），
    这是除约定断言之外本组唯一的即时失败。
11. **`_sp` 矩阵在 `URL.toString()` 里不可见，但在 equals 比较里可见。** `parseURL` 保留改写后的
    外部形式（`net/ExtendableProtocolURLStreamHandler.java:328-337`），而 `final` 的 `equals`/`hashCode` 比较重建出的
    外部形式（`:301-309`），于是 `test://localhost:12345/abc` 与 `test:text://localhost:12345/abc` **不**相等，
    而 `hostsEqual` 却说它们相等（`:311-314`；在
    `net/ExtendableProtocolURLStreamHandlerTest.java:206-223` 断言）。
12. **`clearURLStreamHandlerFactory()` 不会清除 JDK 的按协议处理器缓存。** 它只写 `factory`
    字段（`net/URLUtils.java:1641-1643`）；一旦 `java.net.URL` 通过 `java.protocol.handler.pkgs` 解析出某个协议，它就会把
    那个实例保留到 JVM 结束，这正是测试重置*属性*而非缓存的原因（`net/AbstractExtendableProtocolURLStreamHandlerTest.java:69-72`）。
13. **没有任何工厂支持该 URL 时，`CompositeSubProtocolURLConnectionFactory.create` 会响亮地失败。** `selectFactory` 用
    `selectFactoryIndex` 返回的 `-1` 去索引列表（`net/CompositeSubProtocolURLConnectionFactory.java:130-138`）
    → `IndexOutOfBoundsException`；请先用 `supports(...)` 做防护（`net/CompositeSubProtocolURLConnectionFactoryTest.java:59-62`）。
14. **`console:` 写的是进程的标准输出。** `getOutputStream()` 返回的就是 `System.out` 本身
    （`net/console/ConsoleURLConnection.java:56-59`），所以关闭这个流关闭的是控制台，不是 socket。请把它当作共享资源。

---

## 11. 参见

* [I/O、NIO、扫描与文件过滤器](./io-and-scanning.zh.md) — `IOUtils.close` 以及读取 `jar:`/`classpath:` URL 的那些扫描器
* [类加载与构件](./classloading-and-artifacts.zh.md) — `URLUtils`（`EMPTY_URL_ARRAY`）最大的消费者
* [语言抽象](./language-abstractions.zh.md) — `Prioritized`，本组中每个工厂链与子协议链的排序依据
* [集合与过滤器](./collections-and-filters.zh.md) — `SetUtils.ofSet`、`MapUtils.toFixedMap` 与 `ListUtils.newLinkedList`，在 §7 中贯穿始终
* [工具类与常量](./util-and-constants.zh.md) — `StringUtils.split`、`SymbolConstants`、`ProtocolConstants`、`SeparatorConstants.ARCHIVE_ENTRY_SEPARATOR`
* [类型转换](./type-conversion.zh.md) — 委托进本包的 `URL`/`URI` 转换器
* 网络与 URL 协议 —— 开发者指南：[Networking and URL protocols — Developer Guide](../../developer-guide/microsphere-java-core/net-and-url-protocols.zh.md)
* [English User Guide](./net-and-url-protocols.md)
