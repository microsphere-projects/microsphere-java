# I/O、NIO、扫描与文件过滤器（I/O, NIO, scanning and file filters）

| | |
|---|---|
| **构件（Artifact）** | `io.github.microsphere-projects:microsphere-java-core`（`0.3.20-SNAPSHOT`，`release` 分支） |
| **包** | `io.microsphere.io` —— 7 · `io.microsphere.io.event` —— 3 · `io.microsphere.io.scanner` —— 4 · `io.microsphere.io.filter` —— 5 · `io.microsphere.nio.file` —— 1 · `io.microsphere.nio.charset` —— 1（**合计 21**） |
| **JDK 级别** | 以 Java 8 编译；CI 在 JDK 8 / 11 / 17 / 21 / 25 上测试 |
| **依赖的其它 `core` 包** | `util`（`SystemUtils`、`ClassLoaderUtils`、`StringUtils`、`Utils`）、`constants`、`text`、`collection`、`filter`（泛型 `Filter<T>`）、`event`（`EventDispatcher`）、`logging`、`concurrent`、`annotation` |
| **外部依赖** | 无 —— 纯 JDK（`java.io`、`java.nio.file`、`java.util.jar`） |
| **被谁使用** | `util.ClassUtils`（classpath 扫描）、`net.URLUtils`、`classloading.StreamArtifactResourceResolver`、`process.ProcessExecutor`、`util.ExceptionUtils` |

本页所有 `file:line` 引用均相对于 `microsphere-java-core/src/main/java/io/microsphere/`（主源码）或
`microsphere-java-core/src/test/java/io/microsphere/`（测试），读取自 `release` worktree。
首次出现完整路径后会被缩短 —— 例如 `io/scanner/SimpleFileScanner.java:87-108` 在该包的 `io/` 前缀建立之后
简写为 `scanner/SimpleFileScanner.java:87-108`。

---

## 1. 这一组能力是做什么的

**当你需要一个“传入 `null` 流不会抛异常、也不会去关闭你不拥有的流”的 I/O 助手时，请使用 `io.microsphere.io`。**
`IOUtils`（`io/IOUtils.java:57`）就是全部表面：`toByteArray`、`toString`、`copyToString`、`readLines`、`copy`、`close`。
每个 `copy*`/`toString` 重载对 `null` 源都返回 `null`（`:398-400`、`:424-426`）而不抛异常，`close(null)` 是空操作
（`:535-545`）。本类中没有任何方法会关闭你传进去的流 —— javadoc 反复强调“leaves the input stream open”
（`:99`、`:191`、`:222`），代码也确实如此；`Files.readLines` 才是本切片中唯一自己拥有并关闭流的地方
（`nio/file/Files.java:167`）。

**当你需要“这个目录 / 这个 jar / 这个包里有什么”时，请使用 `io.microsphere.io.scanner`。** 共有三个具体扫描器，
每个都是无状态单例，每个都返回不可变的 `LinkedHashSet`，因此结果顺序跟随文件系统或归档的顺序：

| 扫描器 | 输入源 | 结果 | 单例 |
|---|---|---|---|
| `SimpleFileScanner`（`scanner/SimpleFileScanner.java:52`） | `File` + `boolean recursive` | `Set<File>` | `INSTANCE`（`:57`） |
| `SimpleJarEntryScanner`（`scanner/SimpleJarEntryScanner.java:60`） | `URL` 或 `JarFile` + `boolean recursive` | `Set<JarEntry>` | `INSTANCE`（`:65`） |
| `SimpleClassScanner`（`scanner/SimpleClassScanner.java:64`） | `ClassLoader` + 包名，或归档 `File`/`URL` | `Set<Class<?>>` | `INSTANCE`（`:69`） |

> [!IMPORTANT]
> **`File` 谓词存放在 `io.microsphere.io.filter`**（`IOFileFilter`、`DirectoryFileFilter`、`FileExtensionFilter`、
> `NameFileFilter`、`TrueFileFilter`），而不是在另一个包里。泛型 `Filter<T>` SPI 位于 `io.microsphere.filter`，
> 由[集合与过滤器](./collections-and-filters.zh.md)一页记录。两者不可混用：`SimpleFileScanner` 接收的是 `IOFileFilter`，
> `Scanner.scan(S, Filter<R>)` 接收的是 `Filter<R>`，而 `SimpleJarEntryScanner` 接收的是来自
> `io.microsphere.filter` 的 `JarEntryFilter`。分清它们是这一块最常见的错误。

**当你希望以 `FileChangedEvent` 的形式、通过库自身的事件总线获得文件变更事件时，请使用
`FileWatchService` / `StandardFileWatchService`**，而不是手写 `WatchService` 轮询循环。这座桥约 340 行，它做了三件
容易做错的事：注册的是**目录**（不是文件，因为 JDK `WatchService` 无法监视普通文件）、把 `StandardWatchEventKinds`
映射为 `FileChangedEvent.Kind`、以及通过 `EventDispatcher` 派发，从而让你的监听器获得与 `core` 其它部分一致的
`Prioritized` 排序。

`StringBuilderWriter`、`FastByteArrayInputStream`、`FastByteArrayOutputStream` 是 `core` 其余部分赖以构建的三个内存内
`Writer` / 流类型 —— `util.ExceptionUtils` 使用前者，`process.ProcessExecutor` 与
`classloading.StreamArtifactResourceResolver` 使用后两者（通过 import 验证）。

---

## 2. 坐标、版本支持与平台矩阵

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

| 事实 | 取值 | 证据（`release`） |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | `pom.xml:12` |
| 版本属性 | `${revision}` | `pom.xml:14` |
| `release` 上 `${revision}` 的值 | `0.3.20-SNAPSHOT` | `pom.xml:54` |
| 最新已发布标签 | `0.3.20` | 分组索引中的发行线表（`docs/current/user-guide/microsphere-java-core/README.md:70`） |
| 构建父 POM | `microsphere-build:0.3.16` | `pom.xml:6-10` |
| Java 级别 | reactor 各 POM 中不存在任何 `maven.compiler.*` / `<release>` 属性 —— Java 8 级别继承自外部构建父 POM | CI 矩阵 `java: [ '8', '11', '17', '21', '25' ]`，`.github/workflows/maven-build.yml:25`，`distribution: 'temurin'` `:33`，构建命令 `:38-45` |
| Spring Framework，JDK 17+ | `7.0.9` | `microsphere-java-parent/pom.xml:23` |
| Spring Framework，JDK `[1.8,17)` | `5.3.39`，经 profile `java8-16` | `microsphere-java-parent/pom.xml:54-62`（激活条件 `<jdk>[1.8,17)</jdk>` 在 `:57`） |
| 导入的 BOM | `microsphere-all-bom:0.3.13`、`spring-framework-bom` | `microsphere-java-parent/pom.xml:22`、`:31-37`、`:40-46` |

本切片不依赖 Spring，因此 `java8-16` profile 在这里不会改变任何行为。在本切片中**真正**改变行为的是 JDK 与操作系统，
因为这是接触 `WatchService`、`Path`/`Files` 与平台字符集的层次。

### 2.1 字符集与行分隔符默认值

| 行为 | 来源 | 你实际得到什么 |
|---|---|---|
| `CharsetUtils.DEFAULT_CHARSET` | `Charset.defaultCharset()`（`nio/charset/CharsetUtils.java:43`） | JVM 默认字符集 —— **并未**钉死为 UTF-8 |
| `SystemUtils.FILE_ENCODING` | `getSystemProperty("file.encoding", DEFAULT_CHARSET.name())`（`util/SystemUtils.java:332`） | 属性缺失时回退到默认字符集的名称 |
| `SystemUtils.NATIVE_ENCODING` | `getSystemProperty("native.encoding")`（`util/SystemUtils.java:337`） | 仅在 JDK 18+ 上非 `null`，因为 `native.encoding` 是那时新增的 |
| `IOUtils.readLines(InputStream)` | `readLines(in, FILE_ENCODING)`（`io/IOUtils.java:119-121`） | 用 `file.encoding` 解码，JDK 18 之前依平台而定 |
| `IOUtils.readLines(in, charset)` 的行切分 | `split(content, LINE_SEPARATOR)`（`io/IOUtils.java:183-186`），其中 `LINE_SEPARATOR = System.lineSeparator()`（`constants/SeparatorConstants.java:41`） | **只**按平台分隔符切分 |
| `FileExtensionFilter` 大小写敏感性 | `CASE_SENSITIVE = !IS_OS_WINDOWS`（`io/filter/FileExtensionFilter.java:59`） | `.TXT` 只有在 Windows 上才会匹配 `txt` |

> [!WARNING]
> 上面这几行有两个容易被忽略的后果。第一，`readLines` 按 `System.lineSeparator()` 切分，所以在 Windows 上读取一个
> 仅含 `LF` 的文件会返回**一个**元素，而在 Linux 上读取 `CRLF` 文件会得到以 `\r` 结尾的元素。第二，
> `IOUtils.readLines(in, encoding)` 直接调用 `Charset.forName(encoding)`（`io/IOUtils.java:150-152`）—— 而
> `copyToString(in, encoding)` 会用 `defaultIfNull(encoding, FILE_ENCODING)` 兜底（`:346-349`）。因此给 `readLines` 的
> `String` 编码重载传 `null` 会抛异常，传 `""` 会抛 `IllegalCharsetNameException` —— 后者在 `toString` 路径上由
> `IOUtilsTest.java:117-119` 固定下来。

### 2.2 `WatchService` 的 JDK / 平台语义

| 关注点 | JDK 8 – 10 | JDK 11+ | JDK 18+ | Windows | Linux / macOS |
|---|---|---|---|---|---|
| `FileSystem.newWatchService()`（`io/StandardFileWatchService.java:176`） | JDK 7 起即可用 —— 源码中无任何门控 | 同左 | 同左 | 同左 | 同左 |
| 底层机制 | — | — | — | ReadDirectoryChangesW，事件按**目录**聚合且合并激进；一次写入常触发多次 `MODIFIED` | Linux 上为 inotify（监视描述符是有限资源），macOS 上为 FSEvents |
| 监视普通 `File` | **JDK 完全不支持** —— 本库通过注册 `filePath.getParent()` 绕过（`:275`） | 同左 | 同左 | 同左 | 同左 |
| `native.encoding` 可用于 `SystemUtils.NATIVE_ENCODING` | 不存在 → `null`（`util/SystemUtils.java:337`） | 不存在 → `null` | 存在 | — | — |
| `Charset.defaultCharset()` | 依平台（Windows 上如 Cp1252 / GBK） | 依平台 | **UTF-8**（JEP 400） | 在 18 处发生变化 | 不变化 |
| 注册的事件类型 | 仅 `ENTRY_CREATE/DELETE/MODIFY`（`:133-137`） | 同左 | 同左 | 同左 | 同左 |

由于 `defaultCharset()` 在 JDK 18 起变成 UTF-8（JEP 400），同一段在 JDK 11 上能正确读取 GBK 文件的代码，在 JDK 21 上
会解码错误。如果你依赖不显式传 `Charset` 的 `IOUtils.toString(in)`、`IOUtils.readLines(in)` 或 `Files.readLines(file)`，
请钉死字符集或设置 `-Dfile.encoding` —— 库不会替你选择 UTF-8（`nio/charset/CharsetUtils.java:43` 就是一句
`defaultCharset()`）。

> [!NOTE]
> `CharsetUtils.DEFAULT_CHARSET` 的 javadoc（`nio/charset/CharsetUtils.java:35-42`）声称默认字符集“looks up from …
> `native.encoding` and `file.encoding` … or the default charset is `UTF-8`”。实现只是一句
> `Charset.defaultCharset()`（`:43`）；`NATIVE_ENCODING` 只在 `util/SystemUtils.java:337` 被引用，与本文件无关。
> 请相信代码，不要相信那句话。


### 2.3 `0.3.20` 的发布日期与变更摘要

| 条目 | 值 | 依据 |
|---|---|---|
| 文档所在线 | `release` 分支，`${revision}` = `0.3.20-SNAPSHOT` | 根 `pom.xml:54` |
| 最新已发布版本 | `0.3.20`，标签日期 **2026-10-16** | 标签 `0.3.20` 的提交信息为 `chore: merge main into release [skip ci]`，提交时间 `2026-10-16T03:43:20Z` |
| 上一版本 | `0.3.19`，标签日期 **2026-08-30** | 标签 `0.3.19`，提交时间 `2026-08-30T12:51:37Z` |
| 变更日志来源 | 仓库根目录的 `release-notes.md` | 本项目不发布 `CHANGELOG.md`；`release-notes.md` 的 `v0.3.16`–`v0.3.20` 段落写着 *Release notes generation failed* 并保留原始提交列表，因此下文一律以提交主题引用，并逐条与 `release` 源码核对。 |

`0.3.20` 在这一区域没有新增 API 面——`0.3.19...0.3.20` 区间是一次加固性发布。它实际改变了什么、以及这些改动触及本页源码的程度如下：

* **空值契约。** 本页覆盖范围内 8 / 21 个主源码在签名上声明了 `@Nonnull` / `@Nullable`。区间内点名本区域的主题：`81505bba Add nullability annotations in IO classes` / `441c0b6e Add nullability annotations to IO APIs`。
* **空输入与行为修复。** `07e2d28e Harden NameFileFilter input handling`。

> [!NOTE]
> 如何读这份提交清单：`NameFileFilter` 是 `io.microsphere.io.filter` 的五个类型之一，所以这条*文件过滤器*主题属于本页，而不属于集合/过滤页（后者覆盖 `io.microsphere.filter`）。

---

## 3. API 参考

### 3.1 扫描器入口

| 调用 | 签名（真实） | 说明 |
|---|---|---|
| 扫描目录树 | `SimpleFileScanner.scan(File rootDirectory, boolean recursive)`（`scanner/SimpleFileScanner.java:72-74`） | 以 `TrueFileFilter.INSTANCE` 委托（`:73`） |
| 用 `File` 谓词扫描 | `scan(File rootDirectory, boolean recursive, IOFileFilter ioFileFilter)`（`:87-108`） | 返回 `unmodifiableSet`（`:107`）；**根目录本身**先被判定（`:91-93`） |
| 按 URL 扫描 jar | `SimpleJarEntryScanner.scan(URL jarURL, boolean recursive)`（`scanner/SimpleJarEntryScanner.java:80-82`） | 过滤器为 `null`（`:81`） |
| 按 URL 带过滤器扫描 jar | `scan(URL jarURL, boolean recursive, JarEntryFilter jarEntryFilter)`（`:96-100`） | 先解析相对路径，再打开归档（`:97-99`） |
| 扫描已打开的 `JarFile` | `scan(JarFile jarFile, boolean recursive[, JarEntryFilter])`（`:110-112`、`:125-127`） | 委托给 `protected` 核心重载（`:131-153`） |
| 条目名助手 | `boolean isFileEntry(String jarEntryName, String relativePath)`（`:155-161`） | package-private —— 被 `SimpleJarEntryScannerTest.java:58-61` 使用 |
| 扫描包，仅取名字 | `SimpleClassScanner.scan(ClassLoader cl, String packageName[, boolean recursive])`（`scanner/SimpleClassScanner.java:86-88`、`:103-105`） | `recursive` 默认为 `false` |
| 扫描包，可选加载 | `scan(ClassLoader cl, String packageName, boolean recursive, boolean requiredLoad)`（`:121-158`） | `requiredLoad=false` → 走 `findLoadedClass`，即只返回已加载的类（`:151`） |
| 扫描归档 | `scan(ClassLoader cl, File archiveFile, boolean requiredLoad, Predicate<? super Class<?>>... classFilters)`（`:168-179`） | varargs `Predicate`，由 `filterAll` 应用（`:178`） |
| 按 URL 扫描归档 | `scan(ClassLoader cl, URL resourceInArchive, boolean requiredLoad, Predicate...)`（`:161-165`） | 先 `resolveArchiveFile`（`:163`） |
| 声明出来的抽象 | `Scanner<S,R>.scan(S)` / `scan(S, Filter<R>)`（`scanner/Scanner.java:35`、`:47`） | 见下方提示 |

> [!IMPORTANT]
> **`Scanner<S,R>` 目前没有任何实现。** 在整个 `release` worktree 中 grep
> `implements Scanner`、`extends Scanner`、`ClassPathScanner` 均无结果，也没有发布
> `META-INF/services/io.microsphere.io.scanner.Scanner` 文件。三个 `Simple*Scanner` 是拥有各自具体签名的普通类
> （`scanner/SimpleFileScanner.java:52`、`scanner/SimpleJarEntryScanner.java:60`、`scanner/SimpleClassScanner.java:64`），
> 它们**并不是** `Scanner<S,R>` 的子类型。请把 `Scanner` 看作该抽象的文档化形状，而不是一个你可以注册实现的插件接口；
> `core` 中没有任何代码消费这个接口。

### 3.2 `File` 谓词参考

| 类型 | 形状 | null 处理 | 大小写 / 平台 |
|---|---|---|---|
| `IOFileFilter`（`io/filter/IOFileFilter.java:53`） | `@FunctionalInterface`（`:52`），同时继承 `java.io.FileFilter` 与 `java.io.FilenameFilter` | 由实现者决定 | — |
| `DirectoryFileFilter`（`io/filter/DirectoryFileFilter.java:42`） | `INSTANCE` 单例（`:47`），构造器 `protected`（`:49`） | `file != null && isDirectory()`（`:53-55`）—— **对 null 安全** | — |
| `FileExtensionFilter`（`io/filter/FileExtensionFilter.java:57`） | 工厂 `of(String extension)`（`:93-95`），构造器 `protected`（`:63-66`） | 对 `null` 与目录返回 `false`（`:69-72`）；扩展名为空白时返回 `false`（`:76-78`） | 除 Windows 外均大小写敏感（`:59`、`:83-85`） |
| `NameFileFilter`（`io/filter/NameFileFilter.java:28`） | `new NameFileFilter(name)` / `(name, caseSensitive)`（`:34-41`） | **对 null 不安全**：`:45` 处 `file.getName()` → `NullPointerException` | `caseSensitive` 默认为 `true`（`:35`） |
| `TrueFileFilter`（`io/filter/TrueFileFilter.java:28`） | `INSTANCE`（`:33`），构造器 `protected`（`:35`） | 接受 `null` —— `accept(File)`（`:39-41`）与 `accept(File,String)`（`:44-46`）都无条件返回 `true` | — |

`IOFileFilter` 的第二个方法是一座桥，而不是第二次判定：默认的 `accept(File dir, String name)` 构造出一个 `File`
然后转发（`io/filter/IOFileFilter.java:58-61`）。这正是同一个对象既能用于 `File.listFiles(FileFilter)` 又能用于
`File.list(FilenameFilter)` 的原因。

### 3.3 流、Reader 与 `File` 助手

| 助手 | 行为 | 证据 |
|---|---|---|
| `IOUtils.DEFAULT_BUFFER_SIZE` | `2048`，由字符串 `"2048"` 解析而来 | `io/IOUtils.java:61`、`:66` |
| `IOUtils.BUFFER_SIZE` | `Integer.getInteger("microsphere.io.buffer.size", 2048)` —— 在类初始化时读取**一次** | `:80`、`:91` |
| `toByteArray(InputStream)` | `null` → `EMPTY_BYTE_ARRAY`（`:209-211`）；否则经由 `FastByteArrayOutputStream(BUFFER_SIZE)` 复制（`:212-214`） | `:208-215` |
| `toString(in)` / `(in, encoding)` / `(in, charset)` / `(reader)` | 都是 `copyToString` 家族的薄别名 | `:240-242`、`:268-270`、`:297-299`、`:323-325` |
| `copyToString(InputStream[, charset])` | `null` 流 → `null`；**空**流 → `""` | `:396-402` |
| `copyToString(Reader)` | `null` → `null`，否则排空到一个 `StringBuilderWriter` | `:423-430` |
| `copy(InputStream, OutputStream)` / `copy(Reader, Writer)` | 带缓冲的循环 + `flush()`；两端都保持打开；返回字节/字符计数 | `:455-471`、`:496-512` |
| `close(Closeable)` | `null` → 空操作；`IOException` 被吞掉并只在 `TRACE` 记录 | `:535-545` |
| `Files.readLines(File)` / `(File, Charset)` / `(Path)` / `(Path, Charset)` | 在 try-with-resources 中打开 `java.nio.file.Files.newInputStream`，然后委托给 `IOUtils.readLines` | `nio/file/Files.java:69-71`、`:102-104`、`:133-135`、`:166-170` |
| `FileUtils.resolveRelativePath(File parent, File target)` | 父不是目录、或目标不在父之下时返回 `null`；两者相等返回 `""`；一律使用正斜杠 | `io/FileUtils.java:66-81` |
| `FileUtils.getFileExtension(String)` | `substringAfterLast(name, ".")` —— `"tar.gz"` → `"gz"`，`".hidden"` → `null` | `:100-102` |
| `FileUtils.deleteDirectory(File)` / `cleanDirectory(File)` / `forceDelete(File)` | 返回被删除条目的**计数**；目录不存在时 `deleteDirectory` 返回 `0` | `:121-140`、`:162-168`、`:190-199` |
| `FileUtils.forceDeleteOnExit(File)` / `deleteDirectoryOnExit(File)` | 通过 `File#deleteOnExit` 排队 | `:216-222`、`:240-249` |
| `FileUtils.listFiles(File)` | `null` / 不存在 / 不是目录 → 共享的 `EMPTY_FILE_ARRAY`（`:40`） | `:292-301` |
| `FileUtils.isSymlink(File)` | `Files.isSymbolicLink(file.toPath())` | `:317-319` |
| `FileUtils.getCanonicalFile(File)` | 通过 `ThrowableSupplier.execute` 包装受检 `IOException` | `:339-341` |

### 3.4 `Closeable` 所有权规则 —— 谁关闭什么

| API | 会关闭你的资源吗？ | 它交回来的东西需要你去关吗？ |
|---|---|---|
| `IOUtils.toByteArray`、`toString`、`copyToString`、`readLines` | **不会** —— 调用方保留所有权（`io/IOUtils.java:99`、`:129`、`:191`、`:222`） | 无 |
| `IOUtils.copy(in, out)` / `copy(reader, writer)` | **不会** —— “Both streams remain open after this operation”（`:435`、`:477`） | 无 |
| `IOUtils.close(Closeable)` | 会，并且永不抛出（`:535-545`） | — |
| `nio.file.Files.readLines(File\|Path[, Charset])` | **会** —— 内部 `InputStream` 由 try-with-resources 管理（`nio/file/Files.java:167`） | 无需再关 |
| `SimpleFileScanner.scan(...)` | 不适用 —— 不打开任何流；只用 `File.listFiles()`（`scanner/SimpleFileScanner.java:95`） | — |
| `SimpleJarEntryScanner.scan(URL, ...)` | **不会，也没人关** —— 它调用 `JarUtils.toJarFile(jarURL)`（`scanner/SimpleJarEntryScanner.java:98`），后者每次调用都会新开一个 `JarFile`（`util/jar/JarUtils.java:93`），而扫描器从不关闭它 | 返回的 `JarEntry` 不持有句柄，但每次调用都会泄漏归档句柄 |
| `SimpleJarEntryScanner.scan(JarFile, ...)` | 不会 —— 谁打开谁关闭 | 会 |
| `SimpleClassScanner` | 不打开流；仅当 `requiredLoad` 为 `true` 时才加载类（`scanner/SimpleClassScanner.java:151`） | 类加载不可关闭 —— 请把 `requiredLoad=true` 视为永久性副作用 |
| `StandardFileWatchService` | `close()` 会停止事件循环、经 `IOUtils.close` 关闭 JDK `WatchService` 并清空元数据缓存（`io/StandardFileWatchService.java:200-207`） | 会 —— 请使用 try-with-resources，它是 `AutoCloseable`（`:108`） |

正确的 jar 扫描模式就是 `ClassUtils` 用的那一种：自己在 try-with-resources 中打开归档，然后调用 `JarFile` 重载
（`util/ClassUtils.java:1448-1449`）。

### 3.5 内存内流与 Writer

| 类型 | 基类 | 值得知道的差异 |
|---|---|---|
| `StringBuilderWriter`（`io/StringBuilderWriter.java:66`） | `java.io.Writer` | 包装调用方提供的或新建的 `StringBuilder`（`:73`、`:82`、`:93`）；`close()`（`:198`）与 `flush()`（`:206`）是空操作；`getBuilder()` 暴露内部缓冲（`:215`） |
| `FastByteArrayInputStream`（`io/FastByteArrayInputStream.java:67`） | `ByteArrayInputStream` | 重新实现 `read`/`skip`/`available`/`reset` 且**不带** `synchronized`（`:104-154`）；`close()` 是空操作（`:157-158`）；`equals` 比较**整个**底层数组，忽略 `pos`/`count`（`:161-166`），`hashCode` 为 `hash(buf)`（`:169-171`） |
| `FastByteArrayOutputStream`（`io/FastByteArrayOutputStream.java:55`） | `ByteArrayOutputStream` | 容量倍增并有 `MAX_VALUE - 8` 上限（`:63`、`:104-112`）；`assertCapacity` 显式抛出 `OutOfMemoryError`（`:114-118`）；初始容量为负 → `IllegalArgumentException`（`:76-78`）；各重写方法**没有** `synchronized`（`:123`、`:132`、`:145`、`:159`、`:166`）；`close()` 是空操作且缓冲在关闭后依然可用（`:188-189`） |

---

## 4. 可运行示例（取自真实测试套件）

**带 `File` 谓词的递归扫描** —— 取自
`microsphere-java-core/src/test/java/io/microsphere/io/scanner/SimpleFileScannerTest.java:35-70`：

```java
SimpleFileScanner scanner = SimpleFileScanner.INSTANCE;                 // :31
File javaHome = new File(JAVA_HOME);                                    // :33

Set<File> all = scanner.scan(javaHome, true);                           // :37
assertFalse(all.isEmpty());
all = scanner.scan(javaHome, false);                                    // :40  non-recursive

Set<File> dirs = scanner.scan(javaHome, true, DirectoryFileFilter.INSTANCE);       // :46
assertFalse(dirs.isEmpty());

Set<File> bins = scanner.scan(javaHome, true, new NameFileFilter("bin"));          // :52
assertEquals(1, bins.size());                       // exactly one entry named "bin"

File tempDir = createRandomTempDirectory();                                         // :58-61
assertEquals(1, scanner.scan(tempDir, true).size());   // the root is always included
assertTrue(scanner.scan(tempDir, true).contains(tempDir));

File file = createRandomTempFile();                                                 // :66-69
assertEquals(1, scanner.scan(file, true).size());      // a plain File scans as itself
```

“根目录总是被包含”这一条最值得内化：`SimpleFileScanner` 会先拿过滤器判定 `rootDirectory`，然后才向下递归
（`io/scanner/SimpleFileScanner.java:91-93`），所以 `scan(emptyTempDir, true)` 返回的是只含该目录的单元素集合。

**`FileExtensionFilter` —— Windows 与其它平台** ——
`microsphere-java-core/src/test/java/io/microsphere/io/filter/FileExtensionFilterTest.java:28-79`：

```java
IOFileFilter filter = FileExtensionFilter.of("txt");     // :30

assertFalse(filter.accept(null));                       // :35
assertFalse(filter.accept(new File(JAVA_HOME)));         // :40  directories are rejected

File testFile = createTempFile("test", ".txt");
assertTrue(filter.accept(testFile));                     // :48
assertFalse(filter.accept(createTempFile("test", "")));  // :52  no extension

// the dot prefix is stripped by the constructor (:63-66)
assertTrue(FileExtensionFilter.of(".txt").accept(testFile));            // :66-68

// the FilenameFilter bridge (:58-61) — same object, different call shape
assertTrue(filter.accept(new File(JAVA_IO_TMPDIR), testFile.getName())); // :76
assertFalse(filter.accept(new File(JAVA_IO_TMPDIR), "file.xml"));         // :77

// matches() exposes the case flag directly, so the test is JDK-independent (:83-85)
assertTrue(filter.matches("txt", true));    assertFalse(filter.matches("TXT", true));   // :58-59
assertTrue(filter.matches("txt", false));   assertTrue(filter.matches("TXT", false));   // :60-61
```

**读取行与解码** —— `microsphere-java-core/src/test/java/io/microsphere/nio/file/FilesTest.java:52-65` 展示了
`File` 与 `Path` 两个重载结果一致：

```java
Path testFilePath = Paths.get(TEST_FILE_RESOURCE.toURI());   // :54
String[] lines  = Files.readLines(testFilePath);             // :60
String[] lines2 = Files.readLines(testFile);                 // :61
assertArrayEquals(lines, lines2);
assertEquals(1, lines.length);
assertEquals("test", lines[0]);
```

**缓冲、null 与 `Closeable` 语义** ——
`microsphere-java-core/src/test/java/io/microsphere/io/IOUtilsTest.java:63-221`：

```java
assertEquals(2048, IOUtils.DEFAULT_BUFFER_SIZE);   // :64
assertEquals(2048, IOUtils.BUFFER_SIZE);           // :65

assertEquals("Hello", IOUtils.toString(inputStream));                    // :93
assertNull(IOUtils.toString((InputStream) null));                         // :108
assertEquals("Hello", IOUtils.toString(inputStream, (String) null));       // :113  TEST_NULL_STRING is null (:57 of AbstractTestCase)
assertThrows(IllegalCharsetNameException.class,
        () -> IOUtils.toString(inputStream, ""));                          // :118
assertEquals("Hello", IOUtils.toString(inputStream, (Charset) null));      // :123  null Charset -> DEFAULT_CHARSET (:401)
assertNull(IOUtils.toString(null, (Charset) null));                        // :128
assertNull(IOUtils.toString(null, (String) null));                         // :133
assertEquals("Hello", IOUtils.toString(new StringReader("Hello")));        // :138

// close() is total
assertDoesNotThrow(() -> IOUtils.close(null));                             // :213
assertDoesNotThrow(() -> IOUtils.close(() -> { throw new IOException(); })); // :218-220
```

`IOUtilsTest.java:76-84` 是读取 `META-INF/services` 文件的实用范式：

```java
URL resource = getResource("META-INF/services/io.microsphere.event.EventListener");  // :77
try (InputStream inputStream = resource.openStream()) {                               // :78
    String[] lines = IOUtils.readLines(inputStream);                                  // :79
    assertEquals(2, lines.length);
    assertEquals("io.microsphere.event.EchoEventListener", lines[0]);
}
```

**字节数组流** —— `microsphere-java-core/src/test/java/io/microsphere/io/FastByteArrayInputStreamTest.java:46-139`
与 `.../FastByteArrayOutputStreamTest.java:38-120`：

```java
FastByteArrayInputStream in  = new FastByteArrayInputStream(bytes);            // :36
FastByteArrayInputStream in2 = new FastByteArrayInputStream(bytes, 2, 5);      // :37

assertEquals('H', in.read());        assertEquals(-1, in.read() /* x5 */);      // :48-53
assertEquals(0, in.skip(-1));        assertEquals(4, in.skip(MAX_VALUE));        // :100-103  clamped, never negative
assertThrows(NullPointerException.class, () -> in.read(null, 0, 0));             // :80
assertThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[8], 0, MAX_VALUE)); // :94
assertEquals(in, in2);   // equals() looks only at the shared buf, so pos is ignored (:132 + io/FastByteArrayInputStream.java:161-166)

FastByteArrayOutputStream out = new FastByteArrayOutputStream(2);                // :30  grows 2 -> 4 -> 8 ...
assertThrows(IllegalArgumentException.class, () -> new FastByteArrayOutputStream(-1)); // :40
assertThrows(OutOfMemoryError.class, () -> out.assertCapacity(Integer.MAX_VALUE));     // :119
byte[] before = out.toByteArray();
out.close();
assertArrayEquals(before, out.toByteArray());   // :112-114  close() is a no-op; the buffer survives
```

**监视文件与目录** —— `microsphere-java-core/src/test/java/io/microsphere/io/StandardFileWatchServiceTest.java`。
类型映射契约（`:97-118`）以及那一处刻意验证的失败（`:120-123`）：

```java
assertArrayEquals(ALL_WATCH_EVENT_KINDS, toWatchEventKinds());                  // :99
assertArrayEquals(ALL_WATCH_EVENT_KINDS, toWatchEventKinds(new Kind[0]));        // :100
assertArrayEquals(ALL_WATCH_EVENT_KINDS, toWatchEventKinds((Kind[]) null));      // :101  blank -> all kinds
assertArrayEquals(ofArray(ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE),
                  toWatchEventKinds(CREATED, MODIFIED, DELETED));                // :103  order preserved

assertEquals(CREATED, toKind(ENTRY_CREATE));   // :115
assertThrows(IllegalArgumentException.class, () -> toKind(OVERFLOW));            // :122
```

端到端形态取自 `testFile`（`:126-163`）与 `testDirectory`（`:166-213`）的压缩版本 —— 请注意顺序，它就是 API 契约：

```java
try (StandardFileWatchService watchService = new StandardFileWatchService(commonPool())) {  // :135
    watchService.watch(targetFile, listener, Kind.values());   // :137  register BEFORE start()
    watchService.watch(targetFile, new LoggingFileChangedListener());  // :138
    watchService.start();                                      // :142
    assertTrue(watchService.isStarted());                      // :144
    assertThrows(IllegalStateException.class, watchService::start);  // :147
    Files.copy(sourcePath, targetFilePath, REPLACE_EXISTING);  // :154
    countDownLatch.await();                                    // :157  3 events: CREATED, MODIFIED, DELETED
    watchService.stop();                                       // :159
    assertFalse(watchService.isStarted());                     // :161
}
```

**监听器扇出与默认路由** —— `.../io/event/FileChangedListenerTest.java:45-103` 与
`.../io/FileWatchServiceTest.java:22-31`：

```java
FileChangedListener listener = new DefaultFileChangedListener();   // :37 (an empty impl: io/event/DefaultFileChangedListener.java:26-27)
assertEquals(NORMAL_PRIORITY, listener.getPriority());             // :47  Prioritized default
assertThrows(NullPointerException.class, () -> listener.onEvent(null));  // :80  kind switch at io/event/FileChangedListener.java:97-110

// FileWatchService's Iterable overload just loops (io/FileWatchService.java:76-78)
FileWatchService service = (file, l, kinds) -> log("{}", file);     // :22-24
service.watch(file, new LoggingFileChangedListener(), values());    // :29
service.watch(file, ofList(new LoggingFileChangedListener()), values()); // :30
```

---

## 5. 兼容性说明

| 能力 | Java 8 | Java 9 – 17 | Java 18+ | 备注 |
|---|---|---|---|---|
| `java.nio.file.WatchService`、`Path`、`Files.newInputStream` | 支持（JDK 7） | 支持 | 支持 | 源码中无门控；无条件使用 |
| `Charset.defaultCharset()`（`DEFAULT_CHARSET` 背后，`nio/charset/CharsetUtils.java:43`） | 平台字符集 | 平台字符集 | **UTF-8**（JEP 400） | 这是 CI 矩阵中唯一会静默改变结果的一行 |
| `SystemUtils.NATIVE_ENCODING`（`util/SystemUtils.java:337`） | `null` | `null` | 非 `null` | 本切片的代码路径并不消费它 |
| `System.lineSeparator()`（`constants/SeparatorConstants.java:41`） | Windows 为 `\r\n`，其它为 `\n` | 同左 | 同左 | 决定 `IOUtils.readLines` 的切分 |
| `FileExtensionFilter` 大小写规则（`io/filter/FileExtensionFilter.java:59`） | Windows 不敏感；其它敏感 | 同左 | 同左 | 与 NTFS 大小写不敏感的假设一致 |
| `Integer.getInteger` 缓冲大小（`io/IOUtils.java:91`） | 支持 | 支持 | 支持 | 必须在首次加载 `IOUtils` 类之前设置，否则被忽略 |
| `ByteArrayOutputStream`/`ByteArrayInputStream` 的重写 | 支持 | 支持 | 支持 | `MAX_ARRAY_SIZE = MAX_VALUE - 8` 与 JDK 自身的上限一致（`io/FastByteArrayOutputStream.java:63`） |

跨平台而言，真正会咬人的两个事实是：

* **Windows 上的 `mklink` 需要权限。** 测试助手在 Windows 上用 `mklink /D`、在其它平台用 `ln -s` 创建符号链接
  （`microsphere-java-core/src/test/java/io/microsphere/AbstractTestCase.java:160-176`）。因此
  `FileUtils.isSymlink`（`io/FileUtils.java:317-319`）以及那些感知符号链接的删除路径（`:128-130`、`:246-248`）在 CI
  容器里的表现会不同于开发者笔记本。
* **Windows 的事件合并。** `StandardFileWatchService` 注册的是被监视文件的*父*目录
  （`io/StandardFileWatchService.java:275`），所以一次逻辑上的写入在 Windows 上常会产生多个 `ENTRY_MODIFY` 事件。
  测试套件不是靠数事件，而是靠 `CountDownLatch(3)` 加上显式的 create→modify→delete 编排来吸收这一点
  （`StandardFileWatchServiceTest.java:133-157`、`:216-250`）。

---

## 6. 陷阱与已知限制

1. **`OVERFLOW` 未被处理 —— 它会静默地杀死监视循环。** 只有 `ENTRY_CREATE/DELETE/MODIFY` 会被注册
   （`io/StandardFileWatchService.java:133-137`），但操作系统仍可能投递 `OVERFLOW`（例如 Windows 的积压队列溢出时）。
   `:209-241` 的事件循环遍历 `pollEvents()`（`:216`），并把每一个类型直接交给 `toKind`（`:245`），而 `toKind` 对任何
   无法识别的类型抛出 `IllegalArgumentException`（`:324-327`）—— 这正是 `StandardFileWatchServiceTest.java:120-123`
   固定的行为。循环**没有 `catch`**，只有 `finally { watchKey.reset(); }`（`:233-237`），所以异常逃出被提交的
   `Callable`，被记录进 `eventLoopFuture`（`:210`），又因为该 future 从未被 `get()`，没有人会发现。`isStarted()`
   继续返回 `true`（`:196-198`），但不再派发任何事件。如果你的进程必须在事件丢失后存活，请自己轮询那个 `Future`，
   或者重启服务。
2. **`start()` 之后调用 `watch()` 永远不会注册。** `start()` 会对元数据缓存做一次快照并调用
   `directoryPath.register(watchService, kinds)`（`:178` → `:253-264`），而 `WatchService` 引用是在那之后才被保存的
   （`:182`）。之后调用的 `watch(...)`（`:267-272`）只修改缓存。这里没有“注册进一个已存在的 WatchService”的钩子，
   所以新加的路径永远不会被监视。所有测试都是先 `watch` 再 `start`
   （`StandardFileWatchServiceTest.java:137-142`、`:172-188`、`:298`）。
3. **监视一个文件会上报它整个目录。** 对于文件，缓存键是 `filePath.getParent()`（`:275`），而派发条件是
   `isDirectory(dirPath, NOFOLLOW_LINKS) || metadata.filePaths.contains(filePath)`（`:225`）。由于父目录**就是**目录，
   第一个操作数短路成立，`filePaths` 集合（`:270`、`:333`）实际上从未起到过滤作用 —— 你的监听器会收到它从未关心的
   兄弟文件的事件。事件类型亦然：它们只在缓存条目被**创建**时才被抓取（`:276-281`），因此第一个调用者的 `Kind...`
   会静默地作用于该目录后续的所有监视者。
4. **`Scanner<S,R>` 没有实现。** 见 3.1。不要写 `List<Scanner>` 注入点并期望拿到 `Simple*Scanner` bean；
   它们是互不相关类型的单例（`scanner/SimpleFileScanner.java:57`、`scanner/SimpleJarEntryScanner.java:65`、
   `scanner/SimpleClassScanner.java:69`）。
5. **`SimpleJarEntryScanner.scan(URL, ...)` 会泄漏归档句柄。** 它经 `JarUtils.toJarFile(jarURL)` 取得一个全新的
   `JarFile`（`scanner/SimpleJarEntryScanner.java:98`；分配点在 `util/jar/JarUtils.java:93`），却不关闭任何东西。
   请优先在 try-with-resources 中使用 `JarFile` 重载 —— 即 `util/ClassUtils.java:1448-1449` 的模式。另需注意
   `toJarFile` 在路径无法解析、或抛出它只用 `TRACE` 记录的 `IOException` 时返回 `null`
   （`util/jar/JarUtils.java:88-90`、`:94-98`），而 `JarUtils.filter(null, …)` 降级为空列表（`:194-196`），所以一个损坏
   的 jar 给你的是**空集合**，不是错误。
6. **`SimpleJarEntryScanner` 的非递归规则基于子串。** `isFileEntry` 先要求
   `jarEntryName.indexOf(relativePath) == 0`，再要求其后不再有 `/`
   （`scanner/SimpleJarEntryScanner.java:155-161`）。`SimpleJarEntryScannerTest.java:58-61` 记录了这个尖角：
   `"java/lang/String.class"` 匹配相对路径 `"java/lang/"`，但**不**匹配 `"java/lang"` —— 你必须传入带结尾斜杠的前缀。
7. **`SimpleClassScanner` 在类尚未加载时什么都返回不了。** 默认 `requiredLoad=false` 时，每个类名都走
   `findLoadedClass`，`null` 结果被丢弃（`scanner/SimpleClassScanner.java:150-155`）。要真正加载必须传
   `requiredLoad=true`（副作用、静态初始化、类初始化错误都要考虑）。`SimpleClassScannerTest.java:39-43` 之所以得到 4 个类，
   正是因为它传了 `true`。
8. **`readLines(InputStream, String)` 不容忍 `null` 编码。** `io/IOUtils.java:150-152` 直接调用 `Charset.forName`，
   而 `copyToString(in, encoding)` 会施加 `defaultIfNull(encoding, FILE_ENCODING)`（`:346-349`）。`:145` 的 javadoc 对
   `readLines` 重载承诺“null or blank”时取默认值，代码并未兑现。请改用 `Charset` 重载（`:183-186`）或 `copyToString`。
9. **`copy(in, out)` 抛出的是 `NullPointerException`，不是 `IllegalArgumentException`。** 它使用
   `Objects.requireNonNull`（`io/IOUtils.java:456-457`，import 在 `:47`），而它自己的 javadoc 声称会抛 IAE（`:439`）；
   `copy(Reader, Writer)` 同样（`:497-498` 对 `:480`）。这偏离了库内 `Assert.assertNotNull` 抛 IAE 的统一约定。
10. **`toString`/`copyToString` 只对 `null` 流返回 `null`。** `copyToString` 是与 `EMPTY_BYTE_ARRAY` 做**同一性**比较
    （`io/IOUtils.java:398-400`），而 `toByteArray` **仅**在 `null` 分支返回该共享实例（`:209-211`）；对于一个真实的空流，
    复制结果来自 `FastByteArrayOutputStream.toByteArray()` → `copyOf(buf, count)`
    （`io/FastByteArrayOutputStream.java:159-161`），是一个新数组。所以空资源给出 `""`，只有 `null` 入参给出 `null` ——
    尽管 `:236`、`:293`、`:343` 的措辞是“empty or null”。
11. **`BUFFER_SIZE` 在类初始化时就固定了。** `getInteger` 只在静态字段初始化器里执行一次
    （`io/IOUtils.java:91`）；首次使用之后再 `System.setProperty(IOUtils.BUFFER_SIZE_PROPERTY_NAME, "4096")` 没有效果，
    而非法取值会让类初始化失败。
12. **`NameFileFilter` 是唯一对 null 不安全的谓词。** `accept` 会解引用 `file.getName()`
    （`io/filter/NameFileFilter.java:44-48`）→ 遇 `null` 抛 NPE，而 `DirectoryFileFilter`（`:53-55`）、
    `FileExtensionFilter`（`:69-72`）、`TrueFileFilter`（`:39-41`）都能承受 `null`。由于 `SimpleFileScanner` 会把
    **根**先交给过滤器（`scanner/SimpleFileScanner.java:91`），传 `NameFileFilter` 再加一个 `null` 根会抛异常，
    而不是返回空集合。
13. **`FileExtensionFilter` 永远不匹配目录。** 目录在读名字之前就被拒绝
    （`io/filter/FileExtensionFilter.java:70-72`）。再叠加 `SimpleFileScanner` 的递归 —— 它既把子目录加入结果集、
    又向其深入（`scanner/SimpleFileScanner.java:99-104`）—— 用 `.class` 扩展名过滤器是无法枚举出类目录的。
14. **扫描器没有符号链接防护。** `SimpleFileScanner` 依据 `subFile.isDirectory()` 递归
    （`scanner/SimpleFileScanner.java:102`），而 `isDirectory()` 会跟随链接，且它从不查询 `FileUtils.isSymlink`
    （`io/FileUtils.java:317-319`）。只有*删除*类助手才感知链接（`:128-130`、`:246-248`）。根之下若存在目录链接环，
    递归会一直到栈溢出。
15. **`Fast*` 流不是线程安全的。** `io/FastByteArrayOutputStream.java:123-183` 中的重写都不带 `synchronized` 修饰符，
    与它们替代的 JDK 方法不同；`io/FastByteArrayInputStream.java:104-154` 也在无同步的情况下修改继承来的
    `pos`/`mark`。它们还共享 JDK 的 `protected` 字段 `buf`/`count`，因此对它们做子类化或反射都不安全。
16. **`deleteDirectory` 会计数，且只在最后抛。** 目录不存在返回 `0`（`io/FileUtils.java:122-124`），
    但最后一次 `directory.delete()` 失败会在内容可能已被删除之后抛出 `IOException`（`:132-137`）—— 所以“非零返回值
    加上一个异常”是可能出现的结果，目录树会被删掉一半。`FileUtilsTest.java:126-129`（删除过程中遇 IOException）、
    `:131-136`（删除链接）与 `:219-265`（`testForceDeleteOnIOException`）正是在演练这些竞态。

---

## 7. 参见

* [网络与 URL 协议（Networking and URL protocols）](./net-and-url-protocols.zh.md) —— `net.URLUtils` 消费 `IOUtils`；
  为 `SimpleJarEntryScanner.scan(URL, …)` 提供输入的 `jar:`/`file:` 协议处理
* [类加载与构件（Class loading and artifacts）](./classloading-and-artifacts.zh.md) —— `classloading` 消费
  `FastByteArrayInputStream`；`util.ClassUtils.findClassNamesInClassPath` 是扫描器的主要客户
* [集合与过滤器（Collections and filters）](./collections-and-filters.zh.md) —— **泛型** `Filter<T>` SPI 所在地，即
  `io.microsphere.filter`，与本页使用的、位于 `io.microsphere.io.filter` 的 `File` 谓词相对
* [事件派发（Event dispatching）](./events.zh.md) —— `EventDispatcher`、`EventListener` 与 `Prioritized`，
  `io.event` 就是把它们桥接到文件变更事件上
* [序列化（Serialization）](./serialization.zh.md) —— `io.microsphere.io.serializer`，`io.microsphere.io` 下的兄弟包
* [工具类、常量与文本（Utilities, constants and text）](./util-and-constants.zh.md) —— `SystemUtils.FILE_ENCODING`、
  `SeparatorConstants`、`StringUtils.split` 与 `ClassLoaderUtils`，上述助手都委托给它们
* 面向开发者的深入解析：[I/O、NIO、扫描与文件过滤器 — 开发者指南](../../developer-guide/microsphere-java-core/io-and-scanning.zh.md)
* [English User Guide](./io-and-scanning.md)
