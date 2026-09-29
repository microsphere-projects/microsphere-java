# 网络与 URL 协议

> 语言版本：[中文](networking-and-url-protocols.md) · [English](../en/networking-and-url-protocols.md)
> [← 手册目录](../README.md)

## 一览

| 项目 | 取值 |
|---|---|
| 模块 | `microsphere-java-core` |
| 包 | `io.microsphere.net`、`io.microsphere.net.classpath`、`io.microsphere.net.console` |
| 为 `java.net.URL` 新增的协议 | `classpath:` 与 `console:` |
| 一行接入代码 | `ServiceLoaderURLStreamHandlerFactory.attach()` |
| 内置 handler 清单 | `microsphere-java-core/src/main/resources/META-INF/services/io.microsphere.net.ExtendableProtocolURLStreamHandler` |
| JVM 级限制 | `URL.setURLStreamHandlerFactory` 每个 JVM **只能调用一次**；`URLUtils.attachURLStreamHandlerFactory` 采用组合而非报错 |

`io.microsphere.net` 包含两块互不依赖的能力。其一是 `URLUtils`：无状态工具类，负责 URL 的解析、
拼装与编解码，并支持归档（`jar:`/`war:`）检查与 query/matrix 参数解析。其二是可扩展的
`URLStreamHandler` 机制：抽象类 `ExtendableProtocolURLStreamHandler` 提供 `classpath:` 与
`console:` 协议，并支持**子协议链**（协议后接冒号分隔的子协议，如 `console:text://host/path`），
使一个 handler 可以派生到多种连接实现。

---

## 1. `URLUtils`

`URLUtils` 是 `public abstract class URLUtils implements Utils`，构造器为 private，只能调用静态方法。
与 handler 相关的常量：

| 常量 | 取值 / 含义 |
|---|---|
| `HANDLER_PACKAGES_PROPERTY_NAME` | `"java.protocol.handler.pkgs"` —— JDK 查找 handler 包的系统属性 |
| `DEFAULT_HANDLER_PACKAGE_PREFIX` | `"sun.net.www.protocol"` —— JDK 内置前缀，自定义 handler 禁止使用 |
| `HANDLER_CONVENTION_CLASS_NAME` | `"Handler"` —— 每个协议子包下的类名约定 |
| `SUB_PROTOCOL_MATRIX_NAME` | `"_sp"` —— 承载子协议列表的 matrix 参数名 |
| `DEFAULT_ENCODING` | 取自 `SystemUtils.FILE_ENCODING`（即 `file.encoding`），供 `encode`/`decode` 使用 |

代表性方法（均已对照源码核实）：

```java
// 解析与拼装
URL url = URLUtils.ofURL("jar:file:/libs/app.jar!/META-INF/MANIFEST.MF");  // 非法 URL 抛 IllegalArgumentException
URLUtils.buildURI("context", "sub", "resource.yml");                       // "/context/sub/resource.yml"
// 归档检查
URLUtils.isArchiveURL(url);             // true：协议为 jar|zip|war|ear，或可打开为 jar 的 file: URL
URLUtils.resolveArchiveFile(url);       // File(/libs/app.jar) —— 不存在则为 null
URLUtils.resolveArchiveEntryPath(url);  // "META-INF/MANIFEST.MF"
// 参数：返回不可修改的 Map<name, List<value>>；matrix 参数与 buildMatrixString 可往返转换
URLUtils.resolveQueryParameters("http://h/p?a=1&a=2&b=3");  // {a=[1, 2], b=[3]}
URLUtils.resolveMatrixParameters("/p;a=1;b=2?q");           // {a=[1], b=[2]}
URLUtils.buildMatrixString("k", "v1", "v2");                // ";k=v1;k=v2"
URLUtils.resolveSubProtocols("custom:sub1:sub2://host/path"); // [sub1, sub2]
// handler 注册
URLUtils.attachURLStreamHandlerFactory(factory);            // 见第 2 节
URLUtils.registerURLStreamHandler(handler);                 // 以 handler.getProtocol() 为键
URLUtils.close(connection);                                 // 调用 HttpURLConnection.disconnect()，其他类型忽略
```

`attachURLStreamHandlerFactory` 存在的理由：它包装的 JDK 方法
（`java.net.URL.setURLStreamHandlerFactory`）第二次调用会抛 `Error` —— 全局 factory 槽位每个
JVM 只能写一次。因此应用代码应始终使用 Microsphere 的方法，它会做组合：（1）尚无 factory →
直接安装给定的 factory；（2）当前 factory 已是 `CompositeURLStreamHandlerFactory` → 追加；
（3）已装的是外部 factory → 把它和你的 factory 包进新的 composite，反射清空 `URL.factory`
（`clearURLStreamHandlerFactory`），重新安装 composite，再追加。

---

## 2. Stream handler factory 与 `attach()`

| 类 | 关键成员 | 用途 |
|---|---|---|
| `MutableURLStreamHandlerFactory<H extends URLStreamHandler>` | `(Map<String, H>)` 构造器；`addURLStreamHandler(String, H)`；`removeURLStreamHandler(String)`；`getURLStreamHandler(String)`；`getHandlers()`；`clearHandlers()` | 运行期可控的协议 → handler 注册表。**非线程安全**（javadoc 明示） |
| `CompositeURLStreamHandlerFactory` | `addURLStreamHandlerFactory(factory)` → `this`；`final createURLStreamHandler(String)`；`protected getFactories()`、`protected getComparator()` | 第一个返回非 null handler 的子 factory 获胜；子 factory 按 `getComparator()`（默认 `Prioritized.COMPARATOR`）排序 |
| `DelegatingURLStreamHandlerFactory` | `(URLStreamHandlerFactory delegate)`；`protected final getDelegate()` | 拦截 handler 解析 |
| `StandardURLStreamHandlerFactory` | `createURLStreamHandler(String)` | 把 JDK 自带协议集重新包装成 factory |
| `ServiceLoaderURLStreamHandlerFactory` | `public static void attach()` | **总入口** |

`ServiceLoaderURLStreamHandlerFactory.attach()` 一个调用完成全部接线：

1. 通过 SPI（`java.util.ServiceLoader`）加载所有 `java.net.URLStreamHandlerFactory`，
2. 通过 SPI 加载所有 `io.microsphere.net.ExtendableProtocolURLStreamHandler`，以
   `getProtocol()` 为键预填充一个兜底的 `MutableURLStreamHandlerFactory`，
3. 把两组装配进一个 `CompositeURLStreamHandlerFactory`，
4. 经 `URLUtils.attachURLStreamHandlerFactory(...)` 安装。

> [!IMPORTANT]
> 加载 jar 本身不会注册任何 handler。模块的 `src/main/resources` 中**没有**
> `META-INF/services/java.net.URLStreamHandlerFactory` 文件 —— 它只存在于该模块的*测试*资源里
> （指向 `StandardURLStreamHandlerFactory`）。静态入口方法名为 `attach()`，必须在启动时调用一次。

> [!WARNING]
> `attachURLStreamHandlerFactory` 的第（3）步会反射读写私有静态字段 `URL.factory`（经由
> `FieldUtils` 强制访问）。在默认强封装的 JDK 16+（JEP 396）上，若访问被拒，库会打印出需要追加
> 的 JVM 参数：`--add-opens java.base/java.net=ALL-UNNAMED`。只有当别的代码已经安装了全局
> factory 时才会走到这条反射路径。

---

## 3. `classpath:` 协议

`META-INF/services/io.microsphere.net.ExtendableProtocolURLStreamHandler` 注册了两个 handler ——
`io.microsphere.net.classpath.Handler` 与 `io.microsphere.net.console.Handler`；协议名与
`io.microsphere.constants.ProtocolConstants` 一致（`CLASSPATH_PROTOCOL = "classpath"`、
`CONSOLE_PROTOCOL = "console"`，无参构造器下由包名最后一段推导）。

`io.microsphere.net.classpath.Handler` 的解析方式：把 URL 的 authority 与 path 拼接为资源名，
去掉开头的 `/`，然后用加载 Microsphere 核心类的 class loader
（`ClassLoaderUtils.getClassLoader(Handler.class)`）调用 `getResource`：

```java
ServiceLoaderURLStreamHandlerFactory.attach();
URL url = new URL("classpath://META-INF/microsphere/configuration-properties.json");
try (InputStream in = url.openStream()) { /* ... */ }
// authority 与 path 是拼接关系，切分点无所谓；
// 多个开头斜杠的形式同样会被归一化
URL other = new URL("classpath:////META-INF/services/io.microsphere.convert.Converter");
```

找不到时抛 `IOException("No Resource[classpath='...'] was not found!")` —— 该协议从不静默返回空流。
命中时直接委托给*找到的*资源 URL 的连接，因此读取 jar 内资源的行为与 `jar:` 完全一致。

> [!NOTE]
> 解析使用的是 `Handler` 类自身的 loader，而非线程上下文 class loader —— 在扁平的应用
> classpath 上两者视野通常相同，但在容器/模块化环境下需要区分。

---

## 4. `console:` 协议

```java
public class io.microsphere.net.console.Handler extends ExtendableProtocolURLStreamHandler {
    @Override public URLConnection openConnection(URL url, Proxy proxy) throws IOException
        // → new ConsoleURLConnection(url)
}
public class ConsoleURLConnection extends URLConnection {
    @Override public void connect()                 // 空实现
    @Override public InputStream getInputStream()   // System.in
    @Override public OutputStream getOutputStream() // System.out
}
```

任何 console URL 都映射到进程的标准流；host、port、path 全部被忽略：

```java
ServiceLoaderURLStreamHandlerFactory.attach();
URL out = new URL("console://localhost:12345/abc");
try (OutputStream os = out.openConnection().getOutputStream()) {
    os.write("printed via a URL\n".getBytes());
}
```

> [!WARNING]
> 协议名是 `console` —— 本代码库中**没有 `out:` 或 `err:` 协议**，且 `getOutputStream()` 永远返回
> `System.out`，绝不会是 `System.err`。测试里 `console://host:port` 这种 authority 写法只是惯例，
> 并非必需：无论 URL 写什么，拿到的都是同一对标准流。

---

## 5. 子协议链

`console:text://host/path` 这类 URL 是"一个协议 + 一串子协议"（`["text"]`）。基类把这一切透明化：

```
console:text://host/path        →  parseURL/reformSpec  →  console://host/path;_sp=text
           （子协议被转换成 "_sp" matrix 参数）
console://...;_sp=text  →  openConnection(URL)[final] → openConnection(URL, Proxy)
           → resolveSubProtocols = ["text"] → 第一个 supports() 为 true 且
             create() 返回非 null 的 SubProtocolURLConnectionFactory 获胜
           → 全部未命中：openFallbackConnection(URL, Proxy)，默认返回 null
```

分发逻辑位于 `openConnection(URL, Proxy)` 的默认实现中，想支持子协议链的子类**不得**覆盖它。
`openConnection(URL)`、`parseURL`、`equals`、`hostsEqual`、`hashCode`、`toExternalForm` 都是
`final` —— 链式 URL 的解析与相等性判断在设计上就不可覆盖。扩展点：

```java
public abstract class ExtendableProtocolURLStreamHandler extends URLStreamHandler {
    public ExtendableProtocolURLStreamHandler()   // 协议名 = 包名最后一段；并把包追加到 java.protocol.handler.pkgs
    public ExtendableProtocolURLStreamHandler(String protocol)
    public void init()   // 通过下方钩子填充 factory、按 Prioritized.COMPARATOR 排序、注册自身
    protected void initSubProtocolURLConnectionFactories(List<SubProtocolURLConnectionFactory> factories)  // 默认为空
    public void customizeSubProtocolURLConnectionFactories(Consumer<List<SubProtocolURLConnectionFactory>> customizer)
    @Override public URLConnection openConnection(URL u, Proxy p) throws IOException  // 子协议链分发
    protected URLConnection openFallbackConnection(URL url, Proxy proxy) throws IOException  // 默认返回 null
    protected List<String> resolveSubProtocols(URL url)   // 另有：resolveAuthority(URL)、resolvePath(URL)
    public final String getProtocol()
}
public interface SubProtocolURLConnectionFactory {
    boolean supports(URL url, List<String> subProtocols);
    URLConnection create(URL url, List<String> subProtocols, Proxy proxy) throws IOException;
}
```

`CompositeSubProtocolURLConnectionFactory`（`add(...)`、`add(varargs)`、`remove(...)`）把多个
factory 聚合为一个 `SubProtocolURLConnectionFactory`；`DelegatingURLConnection` 包装产出的连接，
转发 `connect`、超时、内容与 header 调用。

> [!IMPORTANT]
> 更正旧文档的两点：（1）`init()` **不会**扫描任何 `SubProtocolURLConnectionFactory` 的 SPI ——
> 它只调用默认为空的 `initSubProtocolURLConnectionFactories(List)` 钩子，factory 只能来自子类的
> 覆盖；（2）`attach()` 构造 SPI 注册的 handler 后**从不**调用其 `init()`，因此需要链式的 handler
> 必须显式自初始化（`handler.init()` 或 `customizeSubProtocolURLConnectionFactories(...)`）。

> [!NOTE]
> 内置的 `classpath` 与 `console` handler 直接覆盖了 `openConnection(URL, Proxy)` —— 这正是
> javadoc 中"无子协议需求"的推荐写法，它们不会执行链式分发：`console:text://...` 打开的就是普通的
> `ConsoleURLConnection`，`text` 仅以解析后的 `_sp` matrix 参数形式存在，不构成行为选择器。

---

## 6. 注册自己的协议

存在两条彼此独立的注册路径：

* **Handler 包名约定** —— `ExtendableProtocolURLStreamHandler` 的无参构造器会把 handler 的父包
  （如 `io.microsphere.net`）追加进 `java.protocol.handler.pkgs` 系统属性，JDK 随后可按反射约定
  找到 `<package>.<protocol>.Handler`；协议名取包名最后一段。构造器同时校验约定：必须是顶层类、
  简单类名为 `Handler`、不得位于 `sun.net.www.protocol`。注意这条约定只在 handler 类被构造过
  *一次之后*才起作用 —— 它无法引导第一个 `classpath:` URL 的解析。
* **显式安装** —— `ServiceLoaderURLStreamHandlerFactory.attach()`（或
  `URLUtils.registerURLStreamHandler(...)`）把 handler 放入全局安装的 `MutableURLStreamHandlerFactory`。
  这是可靠路径，启动时调用即可。

支持链式协议的完整写法（普通 URL 加子协议）：

```java
package com.example.net.vfs;           // 协议名由包名推导："vfs"

import io.microsphere.net.ExtendableProtocolURLStreamHandler;
import io.microsphere.net.SubProtocolURLConnectionFactory;

public class Handler extends ExtendableProtocolURLStreamHandler {

    @Override   // 贡献能处理 "vfs:gzip://..." 的 factory
    protected void initSubProtocolURLConnectionFactories(List<SubProtocolURLConnectionFactory> factories) {
        factories.add(new VfsGzipSubProtocolFactory());
    }

    @Override   // 所有 factory 均未命中时到达，普通 "vfs://..." URL 也走这里
    protected URLConnection openFallbackConnection(URL url, Proxy proxy) throws IOException {
        String path = resolvePath(url);
        if (path == null || path.isEmpty()) {
            return null;                // 返回 null 保持快速失败语义
        }
        return new VfsURLConnection(url, path);
    }
}
```

```
# src/main/resources/META-INF/services/io.microsphere.net.ExtendableProtocolURLStreamHandler
com.example.net.vfs.Handler
```

启动时先 `ServiceLoaderURLStreamHandlerFactory.attach();`，再对需要链式的 handler 调用 `init()`
（见第 5 节 IMPORTANT 标注）。若不需要子协议，改为覆盖 `openConnection(URL, Proxy)` 即可 ——
`console` handler 就是最小参考实现。

---

## 参见

* [核心工具](core-utilities.md) —— `ProtocolConstants`、`SeparatorConstants.ARCHIVE_ENTRY_SEPARATOR`（`"!/"`）、`ClassPathUtils`
* [I/O 与文件监听](io-and-file-watch.md) —— 读取这些 handler 产出的 URL
* [类加载与构件](classloading-and-artifacts.md) —— 归档 URL 作为 `Artifact` 来源
* [参考手册](reference.md) —— 全部 SPI 文件与系统属性一览

[← 手册目录](../README.md) · [上一篇：I/O 与文件监听](io-and-file-watch.md) · [下一篇：日志 →](logging.md)
