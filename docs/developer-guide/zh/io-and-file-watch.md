# I/O、文件与监听

> 语言版本：[中文](io-and-file-watch.md) · [English](../en/io-and-file-watch.md)
> [← 手册目录](../README.md)

## 一览

| 要点 | 值 |
|---|---|
| 所属模块 | `io.github.microsphere-projects:microsphere-java-core` |
| 涉及包 | `io.microsphere.io`、`io.microsphere.io.event`、`io.microsphere.io.filter`、`io.microsphere.io.scanner`、`io.microsphere.nio.charset`、`io.microsphere.nio.file` |
| 流拷贝缓冲区 | 系统属性 `microsphere.io.buffer.size`，默认 `2048` |
| 监听线程命名 | 系统属性 `microsphere.file-watch-service.thread-name-prefix`，默认 `microsphere-file-watch-service` |
| 文件变更事件类型 | `FileChangedEvent.Kind`：`CREATED`、`MODIFIED`、`DELETED` |
| Java 版本 | 8（底层使用 `java.nio.file.WatchService`，无第三方依赖） |

本页覆盖四块内容：不留泄漏地读取与拷贝流（`IOUtils`）、文件与字符集工具（`FileUtils`、`CharsetUtils`、`io.microsphere.nio.file.Files`）、监听目录变化（`FileWatchService` 加 `io.microsphere.io.event` 中的事件模型）、以及扫描类、文件与 JAR 条目（`io.microsphere.io.scanner`）。

> [!IMPORTANT]
> 包的实际位置和需求者直觉不一致：
> `FileWatchService` 与 `StandardFileWatchService` 位于 **`io.microsphere.io`**，而不是某个 `event` 包。
> `FileChangedEvent` / `FileChangedListener` 位于 **`io.microsphere.io.event`**。
> `IOFileFilter` 的实现位于 **`io.microsphere.io.filter`**，它与泛型 `Scanner<S, R>` 接口所使用的顶层
> `io.microsphere.filter` 包是两回事。

---

## 1. 读取与拷贝流：`IOUtils`

`IOUtils`（`public abstract class IOUtils implements Utils`）是在裸流之上提供空安全、显式字符集的一层：

```java
public static String[] readLines(InputStream in) throws IOException            // 另有 (in, String) / (in, Charset) 重载
public static byte[]   toByteArray(InputStream in) throws IOException
public static String   toString(InputStream in) throws IOException             // 另有 (in, String) / (in, Charset) / (Reader) 重载
public static String   copyToString(InputStream in) throws IOException         // 另有 (in, String) / (in, Charset) / (Reader) 重载
public static int      copy(InputStream in, OutputStream out) throws IOException
public static int      copy(Reader reader, Writer writer) throws IOException
public static void     close(Closeable closeable)
```

```java
try (InputStream in = new FileInputStream("pom.xml");
     ByteArrayOutputStream out = new ByteArrayOutputStream()) {

    String content = IOUtils.toString(in, StandardCharsets.UTF_8); // 读取；流保持打开
    int bytes = IOUtils.copy(in, out);                              // 拷贝剩余内容
}   // 由 try-with-resources 关闭；IOUtils 从不替你关闭
```

两条规则解释了绝大多数误用：

* **读取与拷贝方法不会关闭流。** 流的所有权在调用方。`toString(...)` 与 `copyToString(...)` 行为一致，任选其一即可。
* **`close(Closeable)` 是为 `finally` 块准备的。** 它接受 `null`、吞掉 `IOException`、永不重抛——在无法使用 try-with-resources 时用它。

所有带缓冲的操作都使用 `IOUtils.BUFFER_SIZE`，该值在类加载时从系统属性 `microsphere.io.buffer.size` 读取一次（默认即 `IOUtils.DEFAULT_BUFFER_SIZE` = 2048）。若要拷贝超大文件，请在 JVM 启动参数中调大它。

### 1.1 快速内存流

| 类型 | 构造器 | 说明 |
|---|---|---|
| `FastByteArrayInputStream` | `(byte[] buf)`、`(byte[] buf, int offset, int length)` | 继承 `ByteArrayInputStream` |
| `FastByteArrayOutputStream` | `()`、`(int size)` | 继承 `ByteArrayOutputStream`；有 `writeTo(OutputStream)`、`toByteArray()`、`toString(String charsetName)` |
| `StringBuilderWriter` | `()`、`(int capacity)`、`(StringBuilder builder)` | 一个把内容追加进 `StringBuilder` 的 `Writer`；用 `getBuilder()` 取回 |

进程内调用 javac 编译时，就是用 `StringBuilderWriter` 捕获编译器输出的——详见注解处理相关页面。

---

## 2. 文件与字符集工具

### 2.1 `FileUtils`

```java
public static final File[] EMPTY_FILE_ARRAY;                                  // 永不返回 null 的哨兵值

@Nullable public static String resolveRelativePath(File parentDirectory, File targetFile)
@Nullable public static String getFileExtension(String fileName)

public static int  deleteDirectory(File directory) throws IOException
public static int  cleanDirectory(File directory) throws IOException          // 只清空内容，保留目录本身
public static int  forceDelete(File file) throws NoSuchFileException, IOException
public static void forceDeleteOnExit(File file)
public static void deleteDirectoryOnExit(File directory)

public static File[] listFiles(File directory)          // 永不返回 null；失败时返回 EMPTY_FILE_ARRAY
public static boolean isSymlink(File file)
public static File getCanonicalFile(File file)          // 把 IOException 包装后重抛，而非声明抛出
```

`delete*` / `clean*` 系列方法返回**被删除的条目数**，因此测试可以直接断言清理结果，而不只是调用后听天由命。

### 2.2 `CharsetUtils`

`CharsetUtils.DEFAULT_CHARSET` 是一个在类加载时一次性解析好的 `Charset`（`public static final Charset DEFAULT_CHARSET = defaultCharset()`）。它是不带字符集参数的 `IOUtils.toString(InputStream)` / `readLines(InputStream)` 重载所使用的字符集；在 `file.encoding` 与 `native.encoding` 不一致的老版本 JDK 上，其行为至少是稳定且可查证的。字符集关键的场景，请始终显式传入 `Charset` 或编码名。

### 2.3 `io.microsphere.nio.file.Files`

> [!WARNING]
> `io.microsphere.nio.file.Files` **与 `java.nio.file.Files` 同名**，且只是一个按行读取的工具：
>
> ```java
> @Nonnull public static String[] readLines(File file) throws IOException
> @Nonnull public static String[] readLines(File file, Charset charset) throws IOException
> @Nonnull public static String[] readLines(Path filePath) throws IOException
> @Nonnull public static String[] readLines(Path filePath, Charset charset) throws IOException
> ```
>
> 它返回 `String[]` 而不是 `Stream`，内部委托给 `IOUtils.readLines`。需要 copy/move/create 等操作请使用
> `java.nio.file.Files`——两者只 import 其一，切勿同时引入。本框架中不存在 `Charsets` 类。

---

## 3. 文件监听：契约

事件模型位于 `io.microsphere.io.event`，构建在框架通用的 `Event` / `EventListener` 之上（见事件分发页）：

```java
public interface FileWatchService {
    void watch(File file, FileChangedListener listener, FileChangedEvent.Kind... kinds);
    default void watch(File file, Iterable<FileChangedListener> listeners, FileChangedEvent.Kind... kinds);
}

public class FileChangedEvent extends Event {          // 不可变
    public FileChangedEvent(File file, Kind kind)      // 参数为 null 时抛 IllegalArgumentException
    public File getFile()                               // 即 (File) getSource()
    public Kind getKind()
    public enum Kind { CREATED, MODIFIED, DELETED }
}

public interface FileChangedListener extends EventListener<FileChangedEvent> {
    @Override default void onEvent(FileChangedEvent event)   // 按 Kind 分发到下面三个方法
    default void onFileCreated(FileChangedEvent event)       // 空实现——按需覆写
    default void onFileModified(FileChangedEvent event)      // 空实现
    default void onFileDeleted(FileChangedEvent event)       // 空实现
}
```

由于 `FileChangedEvent` 继承自 `Event`，每次回调都免费带有 `getTimestamp()`；而 `FileChangedListener` 就是一个普通的 `EventListener`——你也可以把它注册到 `EventDispatcher` 上，尽管监听服务会直接把它投递。`LoggingFileChangedListener` 是一个现成的实现，以 debug 级别记录每种事件类型，适合在搭建监听器初期用于排查。

---

## 4. `StandardFileWatchService`：生命周期与用法

```java
public class StandardFileWatchService implements FileWatchService, AutoCloseable {

    public static final String DEFAULT_THREAD_NAME_PREFIX = "microsphere-file-watch-service";
    public static final String THREAD_NAME_PREFIX_PROPERTY_NAME = "microsphere.file-watch-service.thread-name-prefix";
    public static final String THREAD_NAME_PREFIX;              // 类加载时读取一次

    public StandardFileWatchService()                             // 监听回调：DIRECT_EXECUTOR
    public StandardFileWatchService(Executor eventHandlerExecutor)
    public StandardFileWatchService(Executor eventHandlerExecutor, ExecutorService eventLoopExecutor)

    public void start() throws Exception
    public void stop() throws Exception
    public boolean isStarted()
    public void close() throws Exception
    public void watch(File file, FileChangedListener listener, FileChangedEvent.Kind... kinds)
}
```

```java
StandardFileWatchService watcher = new StandardFileWatchService();

// 1. 必须在 start() 之前注册：start() 才会把目录注册进 JDK 的 WatchService
watcher.watch(new File("config/application.yml"), event -> {
    switch (event.getKind()) {
        case CREATED:
        case MODIFIED: reload(event.getFile()); break;
        case DELETED:  clear();                 break;
    }
}, FileChangedEvent.Kind.CREATED, FileChangedEvent.Kind.MODIFIED);

// 2. 启动事件循环
watcher.start();

// 3. 结束时：stop() 终止事件循环；close() = stop() + 关闭 JDK WatchService + 关停两个 executor
watcher.close();
```

线程模型有两个维度，你选择的构造器同时决定它们：

| 组成部分 | 运行位置 | 影响 |
|---|---|---|
| 事件循环（执行 `watchService.take()` 的循环） | 永远独占一个**单线程守护 `ExecutorService`**，线程名取自 `THREAD_NAME_PREFIX` | 不受监听器拖累；JVM 退出时被关闭（构造器里注册了 `shutdownOnExit`） |
| 监听器回调 | 由**处理 executor** 决定。无参构造器 → `DIRECT_EXECUTOR`，即回调直接跑在监听线程上 | 使用 `DIRECT_EXECUTOR` 时，一个缓慢或阻塞的监听器会卡住后续所有事件投递——处理逻辑可能阻塞就传入 `Executor` |

> [!NOTE]
> 内部实现上，每个被监听的目录都配有一个 `EventDispatcher.parallel(eventHandlerExecutor)`，监听器是通过框架的事件机制
> 而非临时直调来分发的。因此事件分发页中描述的监听器优先级与 `EventListener` 语义在这里同样适用。

---

## 5. `java.nio.file.WatchService` 实际能给你什么

以下每一条要么能在 `StandardFileWatchService` 源码中看到，要么是其封装的 JDK `WatchService` 的已知平台行为。在把文件监听当作正确性依据之前，务必读完。

* **只能注册目录。** 传入普通文件时，服务会取 `file.toPath().getParent()`（传入目录则直接监听该目录）。子目录**不会**被监听——没有递归选项；要跟踪整棵目录树，需在 `start()` 之前自己注册每一层目录。
* **你会收到整个被监听目录的所有事件，而不只是你注册的那个文件。** 分发循环的放行条件是"被注册路径是目录"（它永远是目录）——因此同一目录下其他文件的 `CREATED`/`MODIFIED`/`DELETED` 事件也会投递给该目录下注册的所有监听器。请在监听器里按 `event.getFile()` 自行过滤。
* **事件类型按目录约定，首次注册生效。** `watch()` 会按目录合并监听器，但保留的是该目录**第一次**调用传入的 `Kind...`；之后再为同目录其他文件调用 `watch()` 不会拓宽注册到 JDK 的类型。请在首次调用时就传入你关心的类型全集；或者干脆不传——不传 kinds 时服务会注册全部三种（`ENTRY_CREATE`、`ENTRY_MODIFY`、`ENTRY_DELETE`）。
* **`start()` 之后再 `watch()` 不会发生注册。** 目录只在 `start()` 内部推送给 JDK `WatchService`。`start()` 之后为**新**目录调用 `watch()` 只记录元数据、从不注册路径，因此不会有任何事件。重复 `start()` 会抛 `IllegalStateException("StandardFileWatchService has started")`。
* **`OVERFLOW` 会杀死事件循环。** 当操作系统事件队列溢出（变更风暴、消费太慢）时，JDK 会上报一个 `OVERFLOW` 监听事件。而 `StandardFileWatchService.toKind(...)` 只接受 create/modify/delete，对其他类型直接抛 `IllegalArgumentException`，且循环体没有 `catch`——于是循环线程死亡，而 `isStarted()` **仍然返回 true**。如果事件在一波变更后不再到达，先怀疑这里。缓解手段：使用非 `DIRECT_EXECUTOR` 的处理 executor、减少监听目录数；一旦投递停止，重建服务（新实例、`watch(...)`、`start()`）。
* **平台差异依然存在。** 在 JDK 实现之上，本服务没有加任何延迟控制、重试或事件合并的补偿。Linux（inotify）监听的是目录 inode，目录被"重命名覆盖"后即失去跟踪；macOS 与网络/NFS 文件系统的事件合并与丢弃素来不可靠；Windows 则会报出多于实际需要的事件（你会看到莫名的 `MODIFIED`）。请按"至多一次、会被去重"来对待投递——把事件当作重新读取状态的提示，而不是状态变更本身。
* **`stop()` 是尽力而为的。** 它以 100 毫秒为步长等待事件循环终止，超时后用 `Future.cancel(true)` 中断它（使阻塞中的 `take()` 以 `InterruptedException` 退出）。`close()` 还会额外关闭 JDK `WatchService`、清空目录缓存并关停两个 executor——正因如此它实现了 `AutoCloseable`。

> [!TIP]
> "变更即重载"的模式应做成幂等的：把重载逻辑放进一个防抖任务里执行，而不是逐事件触发，因为编辑器一次保存通常会产生多个 `MODIFIED` 事件。

---

## 6. 扫描类、文件与 JAR 条目

### 6.1 通用契约 `Scanner<S, R>`

```java
public interface Scanner<S, R> {
    @Nonnull Set<R> scan(S source) throws IllegalArgumentException, IllegalStateException;
    @Nonnull Set<R> scan(S source, Filter<R> filter) throws IllegalArgumentException, IllegalStateException;
}
```

> [!NOTE]
> 下面几个具体的扫描器并**不**实现 `Scanner<S, R>`——它们需要额外参数（递归、类加载），且使用 `IOFileFilter` /
> `JarEntryFilter` / `Predicate` 而非 `io.microsphere.filter.Filter`。把 `Scanner` 视为你自研扫描器时应遵循的词汇表。

### 6.2 `SimpleClassScanner`

```java
public class SimpleClassScanner {
    public final static SimpleClassScanner INSTANCE;

    @Nonnull @Immutable public Set<Class<?>> scan(ClassLoader classLoader, String packageName)
    @Nonnull @Immutable public Set<Class<?>> scan(ClassLoader classLoader, String packageName, boolean recursive)
    @Nonnull @Immutable public Set<Class<?>> scan(ClassLoader classLoader, String packageName,
                                                  boolean recursive, boolean requiredLoad)
    @Nonnull public Set<Class<?>> scan(ClassLoader classLoader, URL resourceInArchive,
                                       boolean requiredLoad, Predicate<? super Class<?>>... classFilters)
    @Nonnull public Set<Class<?>> scan(ClassLoader classLoader, File archiveFile,
                                       boolean requiredLoad, Predicate<? super Class<?>>... classFilters)
}
```

```java
// 递归扫描一个包下的全部类，并加载
Set<Class<?>> services = SimpleClassScanner.INSTANCE
        .scan(getClass().getClassLoader(), "com.example.spi", true);

// 扫描 JAR/WAR 归档（URL 或 File），只保留筛选通过的类
Set<Class<?>> rest = SimpleClassScanner.INSTANCE.scan(classLoader, jarUrl, true,
        clazz -> !Modifier.isAbstract(clazz.getModifiers()));
```

`requiredLoad = false` 只返回无需加载即可解析的类。若只需类名、完全不加载任何类，请使用
`ClassUtils.findClassNamesInClassPath(...)`（见核心工具页）。

### 6.3 `SimpleFileScanner` 与 `io.microsphere.io.filter`

```java
public class SimpleFileScanner {
    public final static SimpleFileScanner INSTANCE;

    @Nonnull @Immutable public Set<File> scan(File rootDirectory, boolean recursive)
    @Nonnull @Immutable public Set<File> scan(File rootDirectory, boolean recursive, IOFileFilter ioFileFilter)
}
```

```java
Set<File> yamls = SimpleFileScanner.INSTANCE.scan(new File("src/main/resources"), true,
        FileExtensionFilter.of("yml"));
```

过滤器实现 `io.microsphere.io.filter.IOFileFilter`（`accept(File)` 和/或 `accept(File, String)`）：

| 过滤器 | 语义 |
|---|---|
| `TrueFileFilter.INSTANCE` | 全部接受 |
| `DirectoryFileFilter.INSTANCE` | 只接受目录 |
| `NameFileFilter` | 按文件名接受 |
| `FileExtensionFilter.of("yml")` | 按扩展名接受 |

### 6.4 `SimpleJarEntryScanner`

```java
public class SimpleJarEntryScanner {
    public static final SimpleJarEntryScanner INSTANCE;

    @Nonnull @Immutable public Set<JarEntry> scan(URL jarURL, boolean recursive) throws IOException
    @Nonnull @Immutable public Set<JarEntry> scan(URL jarURL, boolean recursive, JarEntryFilter jarEntryFilter) throws IOException
    public Set<JarEntry> scan(JarFile jarFile, boolean recursive) throws IOException
    @Nonnull @Immutable public Set<JarEntry> scan(JarFile jarFile, boolean recursive, JarEntryFilter jarEntryFilter) throws IOException
}
```

```java
Set<JarEntry> classEntries = SimpleJarEntryScanner.INSTANCE.scan(jarUrl, true,
        entry -> entry.getName().endsWith(".class"));
```

`recursive` 控制是否枚举 JAR 内部的嵌套目录。对 null 参数或非 JAR 输入，扫描器还会声明抛出
`NullPointerException` / `IllegalArgumentException`——调用前先校验 URL。提取内容请配合 `JarUtils`，判断 JAR 身份请配合
`ArtifactDetector`（见类加载页）。

---

## 参见

* [事件分发](events.md) — `FileChangedEvent` 所依赖的 `Event` / `EventListener` / `EventDispatcher` 模型。
* [核心工具](core-utilities.md) — `ClassUtils.findClassNamesInClassPath`、`JarUtils`。
* [快速上手](getting-started.md) — 模块与依赖配置。
* [参考](reference.md) — `microsphere.io.buffer.size`、`microsphere.file-watch-service.thread-name-prefix`。

[← 手册目录](../README.md) · [上一篇：事件分发](events.md) · [下一篇：网络与 URL 协议 →](networking-and-url-protocols.md)
