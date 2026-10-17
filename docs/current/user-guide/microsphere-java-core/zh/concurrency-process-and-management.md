# 并发、进程、管理与安全

| | |
|---|---|
| **构件** | `io.github.microsphere-projects:microsphere-java-core`（`0.3.20-SNAPSHOT`，`release` 分支） |
| **包** | `concurrent`（4）、`process`（6）、`management`（3）、`management.builder`（9）、`security`（1）—— 共 23 个主源码类型 |
| **字节码级别** | 反应堆中**不存在**任何 `maven.compiler.*` / `<release>` 属性 —— 级别从外部父 POM `microsphere-build:0.3.16` 继承；CI 在 JDK 8 / 11 / 17 / 21 / 25（Temurin）上测试 |
| **对 `io.microsphere.util` 的依赖** | `ShutdownHookUtils.addShutdownHookCallback`、`ServiceLoaderUtils.loadServicesList`、`ClassLoaderUtils.resolveClass`、`ClassUtils.getTypeName`、`ObjectUtils.defaultIfNull`、`StringUtils.isNumeric` / `substringBefore`、`ArrayUtils`、`Assert.assertNotNull`、`ExceptionUtils.wrap`、`Utils` |
| **使用的其他 `core` 包** | `lang.Prioritized` / `lang.Wrapper` / `lang.DelegatingWrapper`、`reflect.MethodUtils` / `reflect.FieldUtils`、`collection.MapUtils` / `collection.ListUtils`、`logging.Logger` / `LoggerFactory`、`io.FastByteArray*`、`text.FormatUtils`、`constants.SymbolConstants`、`annotation.*`（来自 `microsphere-java-annotations`） |
| **外部依赖** | 无 —— 仅 JDK 的 `java.util.concurrent`、`java.lang.management`、`javax.management` |

本组覆盖四个小而独立的能力：**线程与执行器如何被创建与退役**、**当前进程 id 如何被发现，以及子进程如何被运行与跟踪**、
**JMX 元数据如何被读取与构造**，以及**一个针对 `java.security.policy` 系统属性的助手**。

> [!NOTE]
> `process` 包中**没有**名为 `ProcessIdResolver` 聚合类的 `ProcessIdResolvers`；把三个 `ProcessIdResolver` 实现变成
> 一个 `long` 的 service loader + 优先级选择逻辑位于 `management/ManagementUtils.java:24-32` —— 是 management 包在驱动
> process 包，而不是相反。

---

## 1. 范围

| 包 | 类型 | 阅读本页的理由 |
|---|---|---|
| `concurrent` | `CustomizedThreadFactory`、`ExecutorUtils`、`DelegatingBlockingQueue`、`DelegatingScheduledExecutorService` | 确定性的线程命名、守护线程、JVM 退出时的执行器关闭、装饰器基类 |
| `process` | `ProcessIdResolver`、`ModernProcessIdResolver`、`VirtualMachineProcessIdResolver`、`ClassicProcessIdResolver`、`ProcessManager`、`ProcessExecutor` | 可移植的 PID、被跟踪的子进程、带超时的命令执行 |
| `management` | `ManagementUtils`、`JmxUtils`、`MBeanAttribute` | PID 门面、`ManagementFactory` 门面、属性快照 |
| `management.builder` | 9 个 `MBean*InfoBuilder` 类型 | 从 `BeanInfo` / 反射以编程方式构造 `MBeanInfo` |
| `security` | `SecurityUtils` | 读取 / 写入 `java.security.policy` |

超出范围：线程池容量策略（`Executors` 原样使用）、JMX connector/remote、`HotSpotDiagnosticMXBean` 包装器以及
`AccessController`/`SecurityManager` 的安装 —— 本组都不提供这些能力。

---

## 2. 坐标

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version>
</dependency>
```

BOM 方式同样可用：`microsphere-java-parent/pom.xml` 导入了 `microsphere-all-bom:0.3.13`（`:22`、`:33-36`）与
`spring-framework-bom`（`:40-46`），默认使用 Spring `7.0.9`（`:23`），在由 `<jdk>[1.8,17)</jdk>` 激活的 `java8-16`
profile 下使用 Spring `5.3.39`（`:54-62`，激活条件在 `:57`）。这 23 个类型中没有一个需要 Spring —— 它们是纯粹的 JDK 工具类。

---

## 3. 版本信息与兼容范围

### 3.1 这里真正重要的兼容性矩阵

| | JDK 8 | JDK 11 | JDK 17 | JDK 21 | JDK 25 |
|---|---|---|---|---|---|
| `java.lang.ProcessHandle` 是否存在 | 否 | 是 | 是 | 是 | 是 |
| 在 PID 查找中胜出的解析器 | `VirtualMachineProcessIdResolver`（prio 5） | `ModernProcessIdResolver`（prio 1） | Modern | Modern | Modern |
| 胜出者返回 `null` 时的回退 | `ClassicProcessIdResolver`（prio 9） | 先 `VirtualMachine`，再 `Classic` | 同上 | 同上 | 同上 |
| `SecurityUtils`（仅 `java.security.policy` 属性） | 可用 | 可用 | 可用 | 可用 | 可用 |
| `SecurityManager` / `System.setSecurityManager` | 可用 | 已废弃 | 已废弃**并计划移除**（JEP 411） | 已废弃并计划移除 | 不可用 —— JEP 486（JDK 24）使安装 manager 直接抛异常 |
| `sun.management.VMManagementImpl` 反射路径 | 可用 | 可用（永远不会走到） | 强封装（永远不会走到） | 同上 | 同上 |

带来的后果：在 **JDK 8** 上 PID 不可能来自 `ProcessHandle`（它不存在），因此链条落到
`VirtualMachineProcessIdResolver`，它反射读取 `RuntimeMXBean` 的私有 `jvm` 字段
（`process/VirtualMachineProcessIdResolver.java:81`、`:89-97`），随后落到 `ClassicProcessIdResolver`，
它把 `RuntimeMXBean.getName()` 按 `pid@host` 解析（`process/ClassicProcessIdResolver.java:73-80`）。
这里所有与 `SecurityManager` 相邻的代码都是死代码 —— 见 §7。

> [!IMPORTANT]
> PID 只解析**一次**，位于静态初始化器 `static final long currentProcessId = resolveCurrentProcessId();`
> （`management/ManagementUtils.java:22`）；之后每次 `getCurrentProcessId()` 返回的都是这个缓存字段（`:56-58`）。
> 它不会刷新，并且在类第一次被触碰时就被急切地解析完成。

### 3.2 配置：系统属性

| 名称 | 读取方 | 默认值 | 说明 |
|---|---|---|---|
| `process.execution.timeout` | `ProcessExecutor.DEFAULT_TIMEOUT`（`process/ProcessExecutor.java:78-88`） | `30000`（毫秒） | `@ConfigurationProperty(type = long.class, source = SYSTEM_PROPERTIES_SOURCE)`；在 `:88` 的静态初始化器中读取**一次**，因此请使用 `-D` |
| `microsphere.service-loader.cached` | `ServiceLoaderUtils.SERVICE_LOADER_CACHED`（`util/ServiceLoaderUtils.java:138`），被 `ManagementUtils` 使用 | `false` | `ManagementUtils` 以非缓存方式调用 `loadServicesList(ProcessIdResolver.class)`（`management/ManagementUtils.java:25`），但由于静态字段的存在，结果仍然被缓存 |
| `java.security.policy` | `SecurityUtils`（`security/SecurityUtils.java:45`、`:52-54`、`:70-73`） | 未设置 | 仅 get/set；本组没有任何代码读取它 |

```bash
java -Dprocess.execution.timeout=60000 -jar app.jar
```

### 3.3 `@Since` 历史

全部 23 个类型都在 Javadoc 中标注了 `@since 1.0.0` —— 包括看起来最新的那些助手类
（`CustomizedThreadFactory`、`DelegatingScheduledExecutorService`、`SecurityUtils`）以及 `process` 的各个解析器。
因此本组内部没有任何版本信号可以把新增内容与原始内容区分开；`0.3.20` 是发布它们的标签。

---

## 4. 线程工厂与执行器助手（`io.microsphere.concurrent`）

### 4.1 `CustomizedThreadFactory` —— 命名与守护属性

`CustomizedThreadFactory` 实现了 `java.util.concurrent.ThreadFactory`（`concurrent/CustomizedThreadFactory.java:40`），
它是本组中唯一能给线程赋予稳定、可 grep 名称的手段。

```java
public class CustomizedThreadFactory implements ThreadFactory {                    // :40
    public static final boolean DEFAULT_DAEMON = true;  // :45 —— 另有 DEFAULT_PRIORITY :50、DEFAULT_STACK_SIZE :55
    protected CustomizedThreadFactory(String namePrefix, boolean daemon, int priority, long stackSize)  // :69
    public static ThreadFactory newThreadFactory(String namePrefix);               // :78
    public static ThreadFactory newThreadFactory(String namePrefix, boolean daemon);                     // :82
    public static ThreadFactory newThreadFactory(String namePrefix, boolean daemon, int priority);      // :86
    public static ThreadFactory newThreadFactory(String namePrefix, boolean daemon, int priority, long stackSize); // :90
    public Thread newThread(Runnable r);   // :94 —— 线程组、namePrefix + N、daemon、priority
}
```

有三条可以依赖的行为：**名称形如 `<prefix>-thread-<n>`**（后缀在 `:72` 附加到你的前缀之后，计数器是从 **1** 开始的
`AtomicInteger`，`:71`/`:95`，因此第一个线程是 `worker-thread-1`）；**线程默认是守护线程**
（`DEFAULT_DAEMON` 为 `true`，`:45`，所以 `newThreadFactory("worker")` 产生的线程不会让 JVM 保持存活 ——
需要非守护 worker 时必须显式传入 `false`）；以及 **`ThreadGroup` 在构造时被快照**
（`this.group = currentThread().getThreadGroup()`，`:70`）—— 工厂继承的是构建它的那个线程的分组，
而不是后来调用 `newThread(...)` 的那个线程的分组。

> [!WARNING]
> **这个包里没有未捕获异常处理器。** 在 `microsphere-java-core/src/main/java/io/microsphere/` 全仓搜索
> `UncaughtExceptionHandler` 返回空。`newThread(Runnable)` 只设置分组、名称、守护标志与优先级（`:94-99`）。
> 如果你需要观察失败的任务，请自行安装 `Thread.setDefaultUncaughtExceptionHandler` 或包装你的 `Runnable`。

把它接入 `ExecutorService` 走的是普通的 `Executors` 工厂参数路径，而 `ProcessExecutor` 内部正是这么做的
（`process/ProcessExecutor.java:112`）：`newSingleThreadExecutor(newThreadFactory("process-exec", true))`。

### 4.2 `ExecutorUtils` —— 让执行器退役

```java
public abstract class ExecutorUtils implements Utils {
    public static void shutdownOnExit(Executor one, Executor... others);   // :63
    public static boolean shutdown(Executor executor);                     // :96
    public static boolean shutdown(ExecutorService executorService);       // :131
}
```

* `shutdownOnExit(one, others...)` 只注册**一个** shutdown-hook 回调，该回调在 JVM 退出时关闭全部执行器
  （`:63-68`），通过 `ShutdownHookUtils.addShutdownHookCallback` 完成（`util/ShutdownHookUtils.java:288-294`）；
  回调落在一个 `PriorityBlockingQueue` 中，因此重复调用是累加而非替换。
* 两个重载都对 `null` 做降级处理：`shutdown((Executor) null)` 无法通过 `instanceof` 判定而返回 `false`
  （`:96-101`），`shutdown((ExecutorService) null)` 从其自身的显式卫兵返回 `false`（`:132-134`）。
* `shutdown(ExecutorService)` **即便执行器早已关闭也返回 `true`** —— `isShutdown()` 检查只保护了 `shutdown()`
  调用本身，并没有保护返回值（`:135-141`）。

```java
ExecutorService executorService = newSingleThreadExecutor(newThreadFactory("ExecutorUtilsTest-", true));
executorService.execute(() -> System.out.println("Running..."));

assertTrue(shutdown((Executor) executorService));   // 首次调用：真正执行关闭
assertTrue(shutdown((Executor) executorService));   // 第二次调用：仍然是 true —— 见上文
assertFalse(shutdown((Executor) null));             // 静默降级，从不抛出
assertFalse(shutdown(null));                        // ExecutorService 重载，对 null 安全
```

_来源：`concurrent/ExecutorUtilsTest.java:36-45`、`:69-89`；`concurrent/CustomizedThreadFactoryTest.java:40-61`_

`ExecutorUtilsTest.testShutdownOnExit`（`:47-67`）排空 hook 队列并手工执行已注册的回调，而不是真的退出 JVM ——
这是在你自己的测试中应当照搬的模式。

### 4.3 两个委托基类

| 类型 | 包装对象 | 被委托对象的可变性 | 说明 |
|---|---|---|---|
| `DelegatingBlockingQueue<E>` | `BlockingQueue<E>` | `final`（`:57`） | 实现 `BlockingQueue<E>, DelegatingWrapper`（`:55`）；每个 `BlockingQueue` 方法都是一行转发 |
| `DelegatingScheduledExecutorService` | `ScheduledExecutorService` | `volatile`，可通过 `setDelegate` 替换（`:58`、`:64-66`） | 直接实现该接口；它**不是** `DelegatingWrapper` |

`DelegatingBlockingQueue` 的构造器会压平嵌套的包装器（`:59-62`）：当参数本身就是一个匹配的 `Wrapper` 时，
`tryUnwrap(delegate, BlockingQueue.class)` 返回内层队列，因此
`new DelegatingBlockingQueue<>(new DelegatingBlockingQueue<>(raw))` 指向的是 `raw`，而不是外层的装饰器
（`lang/Wrapper.java:113-121`、`util/ObjectUtils.defaultIfNull`）。`equals`（`:110-122`）与 `hashCode`（`:124-127`）
基于被委托对象实现，因此一个委托队列与它所包装的队列相等。

`DelegatingScheduledExecutorService` 是当你想要观察或拦截调度时应当继承的基类：`delegate` 是 `volatile` 且可替换的，
所以你可以在运行期热替换真正的执行器 —— 但 Javadoc 中"本类是线程安全的"这一说法（`:49-50`）只覆盖了引用替换；
如果在 `schedule(...)` 与其 `Future` 结果之间发生 `setDelegate`，含义仅仅是任务在前一个执行器上运行了。

```java
ScheduledExecutorService delegate = newSingleThreadScheduledExecutor();
DelegatingScheduledExecutorService instance = new DelegatingScheduledExecutorService(delegate);
assertSame(delegate, instance.getDelegate());
assertEquals("Hello,World", instance.schedule(() -> "Hello,World", 1, MILLISECONDS).get());

BlockingQueue<Integer> raw = new LinkedBlockingDeque<>();
DelegatingBlockingQueue<Integer> queue = new DelegatingBlockingQueue<>(raw);
assertTrue(queue.offer(1));
assertEquals(MAX_VALUE, queue.remainingCapacity());   // 无界委托对象
assertEquals(queue, raw);                             // 基于委托对象的 equals
```

_来源：`concurrent/DelegatingScheduledExecutorServiceTest.java:28-50`、`concurrent/DelegatingBlockingQueueTest.java:36-134`_

---

## 5. 当前进程 id、子进程与退出（`io.microsphere.process`）

### 5.1 以可移植方式获取 PID

调用方从不自行挑选解析器；它们调用 `ManagementUtils.getCurrentProcessId()`
（`management/ManagementUtils.java:56-58`），当什么都没解析出来时它返回
`ProcessIdResolver.UNKNOWN_PROCESS_ID`（`-1L`，`process/ProcessIdResolver.java:59`）。
在其背后，三个 `ProcessIdResolver` 实现通过 SPI 被发现、按优先级排序，第一个既 `supports()` 当前 JVM
又返回非 `null` `current()` 的实现胜出（`management/ManagementUtils.java:24-32`）：

```java
public interface ProcessIdResolver extends Prioritized {   // process/ProcessIdResolver.java:54
    long UNKNOWN_PROCESS_ID = -1L;                        // :59
    boolean supports();                                   // :66
    Long current();                                       // :73
}
```

| 解析器 | `getPriority()` | `supports()` 判定 | PID 来源 | 生效于 |
|---|---|---|---|---|
| `ModernProcessIdResolver` | `NORMAL_PRIORITY + 1` = **1**（`:73-76`） | `java.lang.ProcessHandle` 可解析（`:58-61`） | `ProcessHandle.current().pid()` —— 通过反射（`:64-71`） | JDK 11/17/21/25 |
| `VirtualMachineProcessIdResolver` | `NORMAL_PRIORITY + 5` = **5**（`:99-102`） | `RuntimeMXBean` 上存在私有 `jvm` 字段（`:83-86`） | `sun.management.VMManagementImpl#getProcessId()`（`:88-97`） | JDK 8（以及，若被走到，9+） |
| `ClassicProcessIdResolver` | `NORMAL_PRIORITY + 9` = **9**（`:91-94`） | `pid@host` 前缀是数字（`:77-80`） | `RuntimeMXBean.getName()` 中 `@` 之前的子串（`:73-89`） | 每个 JDK，作为最后手段 |

数字越小越先执行：`lang/Prioritized.java:102-105` 对 `getPriority()` 做数值比较，`ServiceLoaderUtils`
使用 `Prioritized.COMPARATOR` 排序（`util/ServiceLoaderUtils.java:890`）。注意这里的取反 —— `MAX_PRIORITY` 是
`Integer.MIN_VALUE`，`MIN_PRIORITY` 是 `Integer.MAX_VALUE`（`lang/Prioritized.java:81`、`:86`），
所以"最高优先级"意味着"最小的数字"。

```java
long processId = getCurrentProcessId();
assertTrue(processId > 0);
assertEquals(currentProcessId, getCurrentProcessId());   // 缓存的静态字段

// 按解析器分别进行、与版本相关的用法：
static final boolean isGEJava9 = testCurrentJavaVersion(GE, JAVA_VERSION_9);
ProcessIdResolver resolver = new ModernProcessIdResolver();
assertEquals(isGEJava9, resolver.supports());
if (isGEJava9) assertNotNull(resolver.current());
else assertThrows(IllegalArgumentException.class, resolver::current);  // JDK 8：PROCESS_HANDLE_CLASS 为 null
```

_来源：`management/ManagementUtilsTest.java:19-24`、`process/ModernProcessIdResolverTest.java:26-45`、`process/ClassicProcessIdResolverTest.java:28-41`、`process/VirtualMachineProcessIdResolverTest.java:28-41`_

> [!NOTE]
> 那个 `else` 分支正是 `supports()` 必不可少的原因：`current()` 是*在不支持时抛异常*的，而不是 null 安全的
> （`process/ModernProcessIdResolver.java:65`，由 `process/ModernProcessIdResolverTest.java:43` 钉死），
> 因此 `ManagementUtils` 在 map 之前先按 `supports()` 过滤（`management/ManagementUtils.java:27-28`）。

### 5.2 已注册的 SPI 实现

`microsphere-java-core/src/main/resources/META-INF/services/io.microsphere.process.ProcessIdResolver` 列出了
`ModernProcessIdResolver`、`VirtualMachineProcessIdResolver` 与 `ClassicProcessIdResolver` —— 它是 core 中 13 个
service 文件之一，也是本组唯一贡献的一个（`management` 与 `security` 没有注册任何服务）。

声明顺序**无关紧要** —— `ServiceLoaderUtils.loadServicesAsList` 会用 `Prioritized.COMPARATOR` 重新排序
（`util/ServiceLoaderUtils.java:888-892`）。重要的是这个文件必须存在：如果 shade 把它丢掉，`loadServicesAsList`
会抛出 `IllegalArgumentException`（`:882-886`），而调用点在 `ManagementUtils` 的**静态初始化器**中
（`management/ManagementUtils.java:22`），所以失败表现是首次触碰时的 `ExceptionInInitializerError`，而不是一个 `-1`。

### 5.3 `ProcessManager` —— 跟踪未完成的子进程

```java
public class ProcessManager {                                                       // :40
    public static final ProcessManager INSTANCE = new ProcessManager();             // :45
    final ConcurrentMap<Process, String> unfinishedProcessesCache = newConcurrentHashMap();  // :47
    protected ProcessManager addUnfinishedProcess(Process process, String arguments);    // :52
    protected ProcessManager removeUnfinishedProcess(Process process, String arguments); // :57
    @Nonnull @Immutable public Map<Process, String> unfinishedProcessesMap();            // :67-71
}
```

公开面**只有**这个单例和只读快照。`addUnfinishedProcess` / `removeUnfinishedProcess` 是 `protected`（`:52`、`:57`），
所以包内代码 —— `ProcessExecutor` —— 以及子类是唯一的写入者，尽管类 Javadoc 中的示例从外部调用了
`ProcessManager.INSTANCE.addUnfinishedProcess(process, "some-command")`（`:25-27`）。同样，类中**没有 `destroy(Process)`
方法**（`:40-72`），尽管 Javadoc 条目里写着"Safely destroy a process"（`:19`）并且示例中有
`ProcessManager.INSTANCE.destroy(process)`（`:30`）。不要据此编写文档或代码；请直接调用 JDK 对象上的 `Process.destroy()`。

```java
INSTANCE.unfinishedProcessesCache.clear();                      // 包级可见的测试钩子

ProcessExecutor processExecutor = new ProcessExecutor("java", "-version");
ExecutorService executorService = newFixedThreadPool(1);
ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8 * 1024);
Future<Boolean> future = executorService.submit(
        () -> { processExecutor.execute(outputStream, SECONDS.toMillis(2)); return true; });

while (!future.isDone())
    INSTANCE.unfinishedProcessesMap().forEach((process, options) -> {
        assertNotNull(process);
        assertEquals(" -version", options);   // options，带前导空格
    });
executorService.shutdown();
```

_来源：`process/ProcessManagerTest.java:28-60`_

那个 `while` 循环确实在观察注册表：`ProcessExecutor` 在抽取输出流之前把 `Process` 放入注册表，
并在 `finally` 中移除（`process/ProcessExecutor.java:161`、`:175`）。

### 5.4 `ProcessExecutor` —— 带超时的执行

```java
public class ProcessExecutor {                                                              // :58
    public static final String DEFAULT_PROCESS_EXECUTION_TIMEOUT_PROPERTY_VAUE = "30000";   // :65（名称拼写错误属于公开 API）
    public static final long DEFAULT_PROCESS_EXECUTION_TIMEOUT = 30000L;                    // :70
    @ConfigurationProperty(...) public static final String PROCESS_EXECUTION_TIMEOUT_PROPERTY_NAME = "process.execution.timeout"; // :78-83
    public static final long DEFAULT_TIMEOUT = getLong(PROCESS_EXECUTION_TIMEOUT_PROPERTY_NAME, 30000L); // :88
    public ProcessExecutor(String command, String... options)                                // :104-115
    public void execute(OutputStream outputStream) throws IOException, TimeoutException;     // :124-126
    public void execute(OutputStream outputStream, long timeoutInMilliseconds);              // :136-138
    public void execute(OutputStream outputStream, long timeout, TimeUnit timeUnit);         // :149-192
}
```

命令行先用空格拼接，然后**按 `\s+` 切分**（`:105-113`、`:154`），因此包含空格的带引号参数不会被保留。
每个实例拥有一个由 `newThreadFactory("process-exec", true)` 构建的**单线程执行器**，并通过 `shutdownOnExit`
注册它（`:112-114`）—— 一个随 JVM 一起消亡的守护线程。`stderr` 通过 `redirectErrorStream(true)` 合并进 `stdout`
（`:155`），所以一次抽取就够了，不可能出现管道写满导致的死锁。非零退出码抛出 `IOException`（`:170-173`）；
超时在 `future.cancel(true)` 之后抛出 `TimeoutException`（`:186-188`）；其余异常被重新包装为 `IOException`（`:189-191`）。

```java
FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream(8 * 1024);

new ProcessExecutor("java", "-version").execute(outputStream);   // 正常路径
assertTrue(outputStream.size() > 0);

assertThrows(TimeoutException.class, () -> new ProcessExecutor("javac", "--help").execute(outputStream, 1));
assertThrows(IOException.class, () -> new ProcessExecutor("javac", "-a").execute(outputStream));       // 非零退出码
assertThrows(IOException.class, () -> new ProcessExecutor("not-found-command").execute(outputStream)); // 命令不存在
```

_来源：`process/ProcessExecutorTest.java:38-64`_

---

## 6. 管理：MXBean 门面、属性与 MBean 构建器

### 6.1 `JmxUtils` —— `ManagementFactory` 门面

`JmxUtils`（`management/JmxUtils.java:87`）是一个静态工具类，它把平台 MXBean 惰性缓存在普通的（非 `volatile`）
静态字段中（`:101-117`），并对外提供属性自省：

| 分组 | 方法 | 返回 |
|---|---|---|
| 单例 | `getClassLoadingMXBean` `:132-138`、`getMemoryMXBean` `:153-159`、`getThreadMXBean` `:174-180`、`getRuntimeMXBean` `:195-201`、`getOperatingSystemMXBean` `:248-254` | 每次调用返回同一实例 |
| 可选 | `getCompilationMXBean()` `:223-229` | `Optional<CompilationMXBean>` —— JVM 无编译器时为空 |
| 列表 | `getMemoryPoolMXBeans` `:275-282`、`getMemoryManagerMXBeans` `:306-313`、`getGarbageCollectorMXBeans` `:337-344` | `unmodifiableList`，只缓存一次 |
| 属性 | `getMBeanAttributesMap(server, name)` `:370-385`、`getMBeanAttributes(...)` `:413-429`、`getAttribute(...)` `:455-458` / `:483-486`、`findMBeanAttributeInfo(...)` `:513-534` | `MBeanAttribute` 快照（`management/MBeanAttribute.java`）；`:91-99` 处的 `EMPTY_*` 数组 |
| 元数据 | `getMBeanInfo(server, name)` `:560-568`、`methodSignature(Method)` `:573-575`、`signature(Parameter[])` `:577-582`、`descriptorForElement(...)` `:588-591`、`descriptorForAnnotations(...)` `:597-619` | 对 `com.sun.jmx.mbeanserver.Introspector` 行为的移植 |

降级是常态：未知的 `ObjectName` 返回 `EMPTY_MBEAN_ATTRIBUTE_ARRAY`（`:416-418`）或共享的 `emptyMap()`（`:375-377`），
`getMBeanInfo` 在记录日志后返回 `null`（`:560-568`），缺失的属性返回 `null` 并附带一条 `warn`（`:529-531`）。
当没有 `@DescriptorKey` 成员时，`descriptorForAnnotations` 返回 `ImmutableDescriptor.EMPTY_DESCRIPTOR`
（`:614-618`；两个分支都在 `.../management/JmxUtilsTest.java:329-343` 被断言）。

```java
MBeanServer mBeanServer = getPlatformMBeanServer();
ObjectName objectName = getInstance("java.lang:type=ClassLoading");

Map<String, MBeanAttribute> attributesMap = getMBeanAttributesMap(mBeanServer, objectName);
assertEquals(5, attributesMap.size());   // Verbose、Total/Loaded/UnloadedClassCount、ObjectName
assertNotNull(getAttribute(mBeanServer, objectName, "Verbose"));
assertEquals(objectName, getAttribute(mBeanServer, objectName, "ObjectName"));

ObjectName notFoundObjectName = getInstance("java.lang:type=NotFound");   // 静默降级
assertSame(EMPTY_MBEAN_ATTRIBUTE_ARRAY, getMBeanAttributes(mBeanServer, notFoundObjectName));
assertSame(emptyMap(), getMBeanAttributesMap(mBeanServer, notFoundObjectName));
assertNull(getAttribute(mBeanServer, notFoundObjectName, "Verbose"));
assertNull(getAttribute(mBeanServer, objectName, "NotFound"));
```

_来源：`management/JmxUtilsTest.java:107-113`、`:179-199`、`:201-205`、`:208-225`_

> [!WARNING]
> 唯一的例外是 `doGetAttribute`（`:621-631`）：它吞掉了 `ReflectionException`、`InstanceNotFoundException` 与
> `AttributeNotFoundException`，但把**其他一切**以 `wrap(e, RuntimeException.class)` 重新抛出（`:627-628`），
> 因此由 MBean 自身的 getter 引起的 `RuntimeMBeanException` 会逃逸出去 —— 在
> `.../management/JmxUtilsTest.java:293` 有断言。

`MBeanAttribute`（`management/MBeanAttribute.java:59`）是值对象：它把 `MBeanInfo`、`MBeanAttributeInfo` 与读到的值
配成一组，把 `getName` / `getType` / `isReadable` / `isWritable` / `isIs` 委托给 info（`:86-123`），并通过
`getValue()` 暴露该值（`:136-138`）。它的构造器（对两个元数据参数执行 `requireNonNull`，`:70-71`）是这个类中唯一的抛出点。

### 6.2 `@DescriptorKey` → `Descriptor`

`descriptorForElement(AnnotatedElement)` 读取元素的注解，并把每个被 `@DescriptorKey` 标注的注解成员变成一个
descriptor 字段（`:588-619`）。测试夹具展示了预期用法：`management/CacheControlMBean.java:29-32` 使用
`@Units("bytes")` 标注，而 `Units.value()` 带有 `@DescriptorKey("units")`（`management/Units.java:41-42`）。

```java
Method method = findMethod(CacheControlMBean.class, "getCacheSize");
Descriptor descriptor = descriptorForElement(method);
assertArrayEquals(ofArray("units"), ((ImmutableDescriptor) descriptor).getFieldNames());
assertEquals("bytes", ((ImmutableDescriptor) descriptor).getFieldValue("units"));
```

_来源：`management/JmxUtilsTest.java:319-326`_

### 6.3 构建器 API（`io.microsphere.management.builder`）

九个类型用于构造 `javax.management` 元数据对象。它们是**一个族**，而不是同一个类上的链式调用：

| 构建器 | 继承自 | 新增 | `build()` 产出 |
|---|---|---|---|
| `MBeanDescribableBuilder<B>`（`:34`，抽象） | —— | `description`、`descriptor`（`:40`、`:46`；setter 在 `:51-59`） | 抽象，`:61` |
| `MBeanFeatureInfoBuilder<B>`（`:32`） | describable | `name`（`:40`、`:45-48`） | `MBeanFeatureInfo`（`:50-52`） |
| `MBeanAttributeInfoBuilder`（`:34`） | feature | `type`/`write`/`read`/`is`（`:40-55`、`:61-74`） | `MBeanAttributeInfo`（`:76-79`） |
| `MBeanParameterInfoBuilder`（`:40`） | feature | `type`（`:46`） | `MBeanParameterInfo`（`:52-54`） |
| `MBeanExecutableInfoBuilder<B>`（`:48`，抽象） | feature | `parameters`、`signature(...)`、`param(...)`、`from(Executable)`（`:50-88`） | 继承而来 |
| `MBeanConstructorInfoBuilder`（`:33`） | executable | —— | `MBeanConstructorInfo`（`:39-41`） |
| `MBeanOperationInfoBuilder`（`:38`） | executable | `returnType`、`impact` + 内嵌 `enum Impact`（`:44-116`） | `MBeanOperationInfo`（`:62-64`） |
| `MBeanNotificationInfoBuilder`（`:40`） | describable | `types`（`:43`、`:49-56`） | `MBeanNotificationInfo`（`:58-60`） |
| `MBeanInfoBuilder`（`:48`） | **describable**（不是 feature） | `className` + 四个构建器列表（`:54-66`） | `MBeanInfo`（`:127-130`） |

每个 setter 都返回构建器自身（`(B) this`），每个构建器都暴露一个静态工厂（`mbeanInfo`、`attribute`、`operation`、
`constructor`、`notification`、`parameter`），而 `build()` 恰好映射到 JDK 的一个构造器。

> [!IMPORTANT]
> **真正被注册的是什么，以什么名字注册：** 本组中没有任何代码调用 `MBeanServer.registerMBean`。
> 构建器只产出 `MBeanInfo` / `MBean*Info` 元数据；**注册由调用方完成**。在测试里 object name 也是由调用方选定的 ——
> `io.microsphere.management:type=Test` 与 `io.microsphere.management:type=CacheControl`
> （`.../management/JmxUtilsTest.java:234`+`:267`、`:284`+`:291`）—— 与用于读取的平台名 `java.lang:type=…` 并存。
> 重复注册是需要你自己处理的错误（见陷阱 8）。

```java
MBeanInfoBuilder builder = mbeanInfo(Data.class.getName());

builder.attribute("test-name", String.class, b -> { b.is(true); b.read(true); b.write(true); });
builder.operation("test-name", String.class, b -> b.impact(ACTION));
builder.constructor(b -> { b.name(TEST_NAME); b.description(TEST_DESCRIPTION); });
builder.notification(String.class);

// 或者从 JavaBean 推导出整个 descriptor 图：
BeanInfo beanInfo = getBeanMetadata(Data.class).getBeanInfo();
MBeanInfo info = mbeanInfo(beanInfo).build();
assertEquals(getTypeName(Data.class), info.getClassName());
assertEquals(beanInfo.toString(), info.getDescription());
```

`mbeanInfo(BeanInfo)` 遍历 `PropertyDescriptor[]` → 属性（`is`/`read`/`write` 由访问器方法推导，
`management/builder/MBeanInfoBuilder.java:75-85`）、`MethodDescriptor[]` → 操作（`:94-103`）以及
`getDeclaredConstructors()` → 构造器（`:179-182`）。

_来源：`management/builder/MBeanInfoBuilderTest.java:59-97`、`:162-204`；`management/builder/AbstractMBeanFeatureInfoBuilderTest.java:42-66`_

---

## 7. 安全（`io.microsphere.security`）

`SecurityUtils` 是针对一个系统属性的三方法助手（`security/SecurityUtils.java:39`）：

```java
public abstract class SecurityUtils implements Utils {
    @ConfigurationProperty(source = SYSTEM_PROPERTIES_SOURCE) public static final String JAVA_SECURITY_POLICY_FILE_PROPERTY_NAME = "java.security.policy";  // :44-45
    public static void setJavaSecurityPolicyFile(@Nonnull String javaSecurityPolicyFilePath);     // :52-54
    public static void setJavaSecurityPolicyFile(@Nonnull File javaSecurityPolicyFile);           // :61-63
    @Nullable public static String getJavaSecurityPolicyFile();                                    // :70-73
}
```

它做的事：写入/读取 `java.security.policy`，仅此而已 —— `File` 重载只是转发 `getAbsolutePath()`（`:62`）。
它**不**做的事：安装 `SecurityManager`、读取该文件、解析 grant 或强制重新加载；其 Javadoc 中的
`@see SecurityManager`（`:35`）只是一个文档链接。当应用或测试需要把策略文件位置暴露给 JDK 自己的策略机制时
（通常由启动器或 `@BeforeAll` 钩子完成），调用方才会触碰它。

```java
File policyFile = new File(getResource("META-INF/class-load-test.policy").getFile());
setJavaSecurityPolicyFile(policyFile);
assertEquals(policyFile.getAbsolutePath(), getJavaSecurityPolicyFile());
clearProperty(JAVA_SECURITY_POLICY_FILE_PROPERTY_NAME);   // 收尾清理
```

_来源：`security/SecurityUtilsTest.java:46-71`_

> [!WARNING]
> 不要基于 `SecurityManager` 构建任何东西：自 JDK 17 起它已被**废弃并计划移除**（JEP 411），并在 JDK 24 中被永久禁用
> （JEP 486），此时 `System.setSecurityManager(non-null)` 抛出 `UnsupportedOperationException`。
> `SecurityUtils` 绕开了这一问题，因为它只操作一个属性，所以在 CI 的全部五个 JDK（8/11/17/21/25）上都是绿的。
> 仓库中的 `TestSecurityManager` 测试助手（`.../test/java/io/microsphere/security/TestSecurityManager.java:27`、`:38`、`:52`）
> 在 24+ 上无法运行 —— 但没有任何代码引用它，所以它是死测试代码，而不是受支持的特性。

---

## 8. 常见陷阱

1. **JDK 8 没有 `ProcessHandle`。** `resolveClass("java.lang.ProcessHandle")` 返回 `null`，因此
   `ModernProcessIdResolver.supports()` 为 `false`（`process/ModernProcessIdResolver.java:54-61`），而它的 `current()`
   是*抛出* `IllegalArgumentException` 而非返回 `null`（`:65`，由 `process/ModernProcessIdResolverTest.java:39-44` 钉死）。
   服务于 JDK 8 的回退是 `VirtualMachineProcessIdResolver`（优先级 5），兜底是 `ClassicProcessIdResolver`（§5.1）。
2. **`SecurityManager` 在 JDK 17 → 25 期间的废弃。** 这里没有任何代码安装它，但"废弃并计划移除"（JEP 411，JDK 17）
   加上 JDK 24 的禁用（JEP 486）意味着任何沿着 `SecurityUtils` 的 `@see SecurityManager` 链接去做的调用方，
   都会在 CI 的 21/25 流水线上崩掉。`SecurityUtils` 只用于操作那个属性。
3. **`ManagementUtils.getCurrentProcessId()` 永不刷新。** 它在静态初始化器中被捕获
   （`management/ManagementUtils.java:22`）；重新 exec 的 JVM 或重新进入某个 PID 命名空间的容器仍会使用首次类加载时
   解析出的值。
4. **SPI 文件缺失是致命错误，而不是 `-1`。** 当没有任何注册实现时 `loadServicesAsList` 抛出
   `IllegalArgumentException`（`util/ServiceLoaderUtils.java:882-886`），而 `ManagementUtils` 是在静态初始化器中调用它的
   （`management/ManagementUtils.java:22`/`:25`）—— 所以 shade 剥掉 service 文件的结果是 `ExceptionInInitializerError`。
5. **抛异常的解析器会穿透"静默降级"。** 该链条只容忍 `null`（`management/ManagementUtils.java:26-31`）；
   `UNKNOWN_PROCESS_ID` 是对*返回* `null` 的兜底，从来不是对*抛异常*的兜底。`:51` 处的 Javadoc 承诺强于代码实际行为。
6. **容器 / cgroup 的 PID 预期。** 三个来源报告的都是*当前 PID 命名空间*看到的 PID，因此在一个共享命名空间的容器里
   （Docker/Kubernetes 默认行为）你得到的是命名空间内的 PID —— 对于 `PID 1` 入口进程通常是 `1` —— 而不是宿主机 PID。
   `ClassicProcessIdResolver` 用一个朴素的 `substringBefore` 解析 `RuntimeMXBean.getName()` 的 `pid@host`
   （`process/ClassicProcessIdResolver.java:73-75`），所以一个不含 `@` 的 JVM 名称会让 `supports()` 为 `false`（`:77-80`），
   最终留下 `-1`。
7. **`process.execution.timeout` 只在类加载时读取一次。** `DEFAULT_TIMEOUT` 是一个通过 `getLong(...)` 初始化的静态字段
   （`process/ProcessExecutor.java:88`）；加载之后再调用 `System.setProperty` 无效 —— 请使用 `-D`。
   该常量名拼写错误（`DEFAULT_PROCESS_EXECUTION_TIMEOUT_PROPERTY_VAUE`，`:65`），并且在 `0.3.20` 上已是公开 API。
8. **名称已被注册时 MBean 注册失败。** 本组只构建 `MBeanInfo`；`registerMBean(...)` 调用是你的责任，
   而重复的 `ObjectName` 会抛出 `InstanceAlreadyExistsException`。`JmxUtils` 不会警告你 ——
   对*陈旧*注册调用 `getAttribute(...)` 会静默返回旧值（`management/JmxUtils.java:621-631`）。
   请在 `@AfterEach`/关闭逻辑中注销，或用 `isRegistered(name)` 做保护；测试中每个名称在每个 JVM 中只注册一次
   （`.../management/JmxUtilsTest.java:267`、`:291`）。
9. **`build()` 几乎不做自身校验。** 构建器不预填任何默认值 —— `mbeanInfo(String)` 把 `description` 与 `descriptor`
   留为 `null`（`.../management/builder/MBeanInfoBuilderTest.java:154-159`）—— 并且 `build()` 把它们原样转发给 JDK 构造器
   （`management/builder/MBeanInfoBuilder.java:127-130`）。只有 `MBeanAttributeInfoBuilder.attribute(String)` 有自我保护，
   通过 `assertNotNull` → `IllegalArgumentException`（`management/builder/MBeanAttributeInfoBuilder.java:86`、
   `util/Assert.java:149-153`）；其余问题都会从 `javax.management` 中冒出来。
10. **`ExecutorUtils.shutdown(ExecutorService)` 会返回两次 `true`。** 对同一个执行器的第一次和第二次调用都返回 `true`
    （`concurrent/ExecutorUtils.java:131-142`；在 `.../concurrent/ExecutorUtilsTest.java:81-84` 有断言），
    所以它不是一个"我刚刚是否关掉了它？"的信号；只有非 `ExecutorService`/`null` 分支才给出 `false`。
11. **`shutdownOnExit` 从不撤销它的注册。** 每次调用都向共享的 hook 队列追加一个回调
    （`concurrent/ExecutorUtils.java:63-68`），而 `ProcessExecutor` 是按实例注册的
    （`process/ProcessExecutor.java:114`）—— 所以请复用 `ProcessExecutor`，不要在循环里创建。
12. **`ProcessExecutor` 的参数切分。** `commandLine.split("\\s+")`（`process/ProcessExecutor.java:154`）
    会破坏任何含空白的选项，而 `:105-113` 处的拼接会在记录的 `options` 中留下一个前导空格 —— 这就是
    `ProcessManagerTest` 断言 `" -version"` 而不是 `"version"` 的原因（`.../process/ProcessManagerTest.java:52`）。
13. **`ProcessManager` 没有 `destroy(...)`。** 它的 Javadoc 宣称存在该方法（`process/ProcessManager.java:19`、`:30`），
    但类体（`:40-72`）并未定义；add/remove 写入方法是 `protected`（`:52`、`:57`），外部代码只能读取
    `unfinishedProcessesMap()`。

---

## 9. 另见

* [语言抽象](language-abstractions.md) —— `Prioritized`、`Wrapper` / `DelegatingWrapper`，它们驱动了解析器排序与队列解包
* [工具类、常量与文本](util-and-constants.md) —— `ShutdownHookUtils`、`ServiceLoaderUtils`、`ClassLoaderUtils.resolveClass`、`Assert`
* [反射、类型与 Bean](reflection-and-beans.md) —— `MethodUtils` / `FieldUtils`（反射式 PID 路径）与 `BeanUtils.getBeanMetadata`
* [集合与过滤器](collections-and-filters.md) —— `MapUtils.newConcurrentHashMap`、`ListUtils.newLinkedList`
* [IO 与扫描](io-and-scanning.md) —— `FastByteArrayOutputStream` / `IOUtils.copy`，被 `ProcessExecutor` 使用
* [日志](logging.md) —— 本组每个类型都从 `LoggerFactory` 获取的那个 `Logger`
* [类加载与构件探测](classloading-and-artifacts.md) —— 兄弟页中 JDK 版本分派的故事
* 面向源码的深入解读：[并发、进程、管理与安全 —— 开发者指南](../../../developer-guide/microsphere-java-core/zh/concurrency-process-and-management.md)
* 英文原文：[Concurrency, process, management and security](../concurrency-process-and-management.md) · 中文分组索引：[README.md](README.md)
