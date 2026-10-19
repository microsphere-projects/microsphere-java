# Concurrency, process, management and security

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-java-core` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Packages** | `concurrent` (4), `process` (6), `management` (3), `management.builder` (9), `security` (1) — 23 main-source types |
| **Bytecode level** | No `maven.compiler.*` / `<release>` property exists anywhere in the reactor; the level is inherited from the external parent `microsphere-build:0.3.16`. CI tests on JDK 8 / 11 / 17 / 21 / 25 (Temurin) |
| **Depends on `io.microsphere.util`** | `ShutdownHookUtils.addShutdownHookCallback`, `ServiceLoaderUtils.loadServicesList`, `ClassLoaderUtils.resolveClass`, `ClassUtils.getTypeName`, `ObjectUtils.defaultIfNull`, `StringUtils.isNumeric` / `substringBefore`, `ArrayUtils`, `Assert.assertNotNull`, `ExceptionUtils.wrap`, `Utils` |
| **Other `core` packages used** | `lang.Prioritized` / `lang.Wrapper` / `lang.DelegatingWrapper`, `reflect.MethodUtils` / `reflect.FieldUtils`, `collection.MapUtils` / `collection.ListUtils`, `logging.Logger` / `LoggerFactory`, `io.FastByteArray*`, `text.FormatUtils`, `constants.SymbolConstants`, `annotation.*` (from `microsphere-java-annotations`) |
| **External dependencies** | none — JDK `java.util.concurrent`, `java.lang.management`, `javax.management` only |

This group covers four small, independent capabilities: **how threads and executors are created and retired**, **how the
current process id is discovered and how child processes are run and tracked**, **how JMX metadata is read and
constructed**, and **one helper for the `java.security.policy` system property**.

> [!NOTE]
> The `process` package has **no** aggregator class named `ProcessIdResolvers`; the service loader + priority selection that
> turns three `ProcessIdResolver` implementations into one `long` lives in `management/ManagementUtils.java:24-32` — the
> management package drives the process package, not the other way round.

---

## 1. Scope

| Package | Types | Read this page for |
|---|---|---|
| `concurrent` | `CustomizedThreadFactory`, `ExecutorUtils`, `DelegatingBlockingQueue`, `DelegatingScheduledExecutorService` | deterministic thread names, daemon threads, JVM-exit executor shutdown, decorator bases |
| `process` | `ProcessIdResolver`, `ModernProcessIdResolver`, `VirtualMachineProcessIdResolver`, `ClassicProcessIdResolver`, `ProcessManager`, `ProcessExecutor` | a portable PID, tracked child processes, command execution with a timeout |
| `management` | `ManagementUtils`, `JmxUtils`, `MBeanAttribute` | the PID façade, the `ManagementFactory` facade, attribute snapshots |
| `management.builder` | 9 `MBean*InfoBuilder` types | programmatic `MBeanInfo` construction from `BeanInfo` / reflection |
| `security` | `SecurityUtils` | reading / writing `java.security.policy` |

Out of scope: thread-pool sizing policy (`Executors` is used as-is), JMX connectors/remotes, `HotSpotDiagnosticMXBean` wrappers and `AccessController`/`SecurityManager` installation — none of which this group provides.

---

## 2. Coordinates

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version>
</dependency>
```

The BOM route works as well: `microsphere-java-parent/pom.xml` imports `microsphere-all-bom:0.3.13` (`:22`, `:33-36`) and
`spring-framework-bom` (`:40-46`), with Spring `7.0.9` by default (`:23`) and Spring `5.3.39` under the `java8-16` profile
activated by `<jdk>[1.8,17)</jdk>` (`:54-62`, activation at `:57`). None of the 23 types requires Spring — plain JDK utilities.

---

## 3. Version information and compatibility range

### 3.1 The compatibility matrix that matters here

| | JDK 8 | JDK 11 | JDK 17 | JDK 21 | JDK 25 |
|---|---|---|---|---|---|
| `java.lang.ProcessHandle` exists | no | yes | yes | yes | yes |
| Resolver that wins the PID lookup | `VirtualMachineProcessIdResolver` (prio 5) | `ModernProcessIdResolver` (prio 1) | Modern | Modern | Modern |
| Fallback if the winner returns `null` | `ClassicProcessIdResolver` (prio 9) | `VirtualMachine`, then `Classic` | same | same | same |
| `SecurityUtils` (`java.security.policy` property only) | works | works | works | works | works |
| `SecurityManager` / `System.setSecurityManager` | works | deprecated | deprecated **for removal** (JEP 411) | deprecated for removal | unusable — JEP 486 (JDK 24) makes installing a manager throw |
| `sun.management.VMManagementImpl` reflective path | works | works (never reached) | strongly encapsulated (never reached) | same | same |

Consequences: on **JDK 8** the PID cannot come from `ProcessHandle` (it does not exist), so the chain falls to
`VirtualMachineProcessIdResolver`, which reflects the private `jvm` field of the `RuntimeMXBean`
(`process/VirtualMachineProcessIdResolver.java:81`, `:89-97`), then to `ClassicProcessIdResolver`, which parses
`RuntimeMXBean.getName()` as `pid@host` (`process/ClassicProcessIdResolver.java:73-80`). All `SecurityManager`-adjacent
code here is dead — see §7.

> [!IMPORTANT]
> The PID is resolved **once**, in the static initializer `static final long currentProcessId = resolveCurrentProcessId();`
> (`management/ManagementUtils.java:22`); every later `getCurrentProcessId()` returns that cached field (`:56-58`). It is
> not refreshed, and it is resolved eagerly the first time the class is touched.

### 3.2 Configuration: system properties

| Name | Read by | Default | Notes |
|---|---|---|---|
| `process.execution.timeout` | `ProcessExecutor.DEFAULT_TIMEOUT` (`process/ProcessExecutor.java:78-88`) | `30000` (ms) | `@ConfigurationProperty(type = long.class, source = SYSTEM_PROPERTIES_SOURCE)`; read **once** in the static initializer at `:88`, so use `-D` |
| `microsphere.service-loader.cached` | `ServiceLoaderUtils.SERVICE_LOADER_CACHED` (`util/ServiceLoaderUtils.java:138`), used by `ManagementUtils` | `false` | `ManagementUtils` calls `loadServicesList(ProcessIdResolver.class)` uncached (`management/ManagementUtils.java:25`), but the result is cached anyway by the static field |
| `java.security.policy` | `SecurityUtils` (`security/SecurityUtils.java:45`, `:52-54`, `:70-73`) | unset | get/set only; nothing reads it here |

```bash
java -Dprocess.execution.timeout=60000 -jar app.jar
```

### 3.3 `@Since` history

All 23 types record `@since 1.0.0` in their Javadoc — including the newest-looking helpers (`CustomizedThreadFactory`,
`DelegatingScheduledExecutorService`, `SecurityUtils`) and the `process` resolvers. There is therefore no version signal
inside this group separating newer additions from the originals; `0.3.20` is the published tag that ships them.


### 3.4 Release date and what changed in `0.3.20`

| Item | Value | Evidence |
|---|---|---|
| Documented line | `release`, `${revision}` = `0.3.20-SNAPSHOT` | root `pom.xml:54` |
| Newest published release | `0.3.20`, tagged **2026-10-16** | the `0.3.20` tag resolves to `chore: merge main into release [skip ci]`, committed `2026-10-16T03:43:20Z` |
| Previous release | `0.3.19`, tagged **2026-08-30** | the `0.3.19` tag, committed `2026-08-30T12:51:37Z` |
| Changelog source | `release-notes.md` at the repository root | The project publishes no `CHANGELOG.md`; the `v0.3.16`–`v0.3.20` blocks of `release-notes.md` read *Release notes generation failed* and carry raw commit lists, so every entry below is quoted as a commit subject and checked against the `release` sources. |

`0.3.20` adds no new API surface for this area — the `0.3.19...0.3.20` range is a hardening release. What it does change, and how far that reaches the sources this page documents:

* **Nullability contracts.** 11 of 23 main sources in the scope of this page declare `@Nonnull` / `@Nullable`. Subjects in the range that name this area: `588b93d3 Add nullability annotations to concurrency APIs`.
* **Null-input and behaviour fixes.** `52d4d4aa Fix compilation MXBean lazy initialization` / `170582a6 Use Objects.isNull for MXBean guard`.

> [!NOTE]
> Reading the commit list: The concurrency subject does **not** show up in `io.microsphere.concurrent`: all four sources (`CustomizedThreadFactory`, `DelegatingBlockingQueue`, `DelegatingScheduledExecutorService`, `ExecutorUtils`) are unannotated on this line. The 11 annotated sources are `io.microsphere.management` (9) plus `process.ProcessManager` and `security.SecurityUtils`.

---

## 4. Thread factories and executor helpers (`io.microsphere.concurrent`)

### 4.1 `CustomizedThreadFactory` — naming and daemon-ness

`CustomizedThreadFactory` implements `java.util.concurrent.ThreadFactory` (`concurrent/CustomizedThreadFactory.java:40`) and is the only way in this group to give threads stable, greppable names.

```java
public class CustomizedThreadFactory implements ThreadFactory {                    // :40
    public static final boolean DEFAULT_DAEMON = true;  // :45 — also DEFAULT_PRIORITY :50, DEFAULT_STACK_SIZE :55
    protected CustomizedThreadFactory(String namePrefix, boolean daemon, int priority, long stackSize)  // :69
    public static ThreadFactory newThreadFactory(String namePrefix);               // :78
    public static ThreadFactory newThreadFactory(String namePrefix, boolean daemon);                     // :82
    public static ThreadFactory newThreadFactory(String namePrefix, boolean daemon, int priority);      // :86
    public static ThreadFactory newThreadFactory(String namePrefix, boolean daemon, int priority, long stackSize); // :90
    public Thread newThread(Runnable r);   // :94 — group, namePrefix + N, daemon, priority
}
```

Three behaviours to rely on: **names are `<prefix>-thread-<n>`** (the suffix is appended to your prefix at `:72`, and the
counter is an `AtomicInteger` starting at **1**, `:71`/`:95`, so the first thread is `worker-thread-1`); **threads are
daemon by default** (`DEFAULT_DAEMON` is `true`, `:45`, so `newThreadFactory("worker")` produces threads that do not keep
the JVM alive — pass `false` explicitly for non-daemon workers); and **the `ThreadGroup` is snapshotted at construction**
(`this.group = currentThread().getThreadGroup()`, `:70`) — the factory inherits the group of whoever builds it, not of
whoever later calls `newThread(...)`.

> [!WARNING]
> **There is no uncaught-exception handler in this package.** A repository-wide search for
> `UncaughtExceptionHandler` across `microsphere-java-core/src/main/java/io/microsphere/` returns nothing.
> `newThread(Runnable)` sets only group, name, daemon flag and priority (`:94-99`). If you need to observe failed tasks,
> install `Thread.setDefaultUncaughtExceptionHandler` yourself or wrap the `Runnable`.

Plugging it into an `ExecutorService` is the ordinary `Executors` factory-argument route, and it is exactly what
`ProcessExecutor` does internally (`process/ProcessExecutor.java:112`): `newSingleThreadExecutor(newThreadFactory("process-exec", true))`.

### 4.2 `ExecutorUtils` — retiring executors

```java
public abstract class ExecutorUtils implements Utils {
    public static void shutdownOnExit(Executor one, Executor... others);   // :63
    public static boolean shutdown(Executor executor);                     // :96
    public static boolean shutdown(ExecutorService executorService);       // :131
}
```

* `shutdownOnExit(one, others...)` registers **one** shutdown-hook callback that shuts down every executor when the JVM
  exits (`:63-68`) via `ShutdownHookUtils.addShutdownHookCallback` (`util/ShutdownHookUtils.java:288-294`); the callbacks land in a `PriorityBlockingQueue`, so repeated calls accumulate rather than replace.
* Both overloads degrade on `null`: `shutdown((Executor) null)` fails the `instanceof` test and returns `false`
  (`:96-101`), and `shutdown((ExecutorService) null)` returns `false` from its own explicit guard (`:132-134`).
* `shutdown(ExecutorService)` returns **`true` even when the executor was already shut down** — the `isShutdown()` check
  only guards the `shutdown()` call, not the return value (`:135-141`).

```java
ExecutorService executorService = newSingleThreadExecutor(newThreadFactory("ExecutorUtilsTest-", true));
executorService.execute(() -> System.out.println("Running..."));

assertTrue(shutdown((Executor) executorService));   // first call: actually shuts down
assertTrue(shutdown((Executor) executorService));   // second call: still true — see above
assertFalse(shutdown((Executor) null));             // silent degradation, never throws
assertFalse(shutdown(null));                        // ExecutorService overload, null-safe
```

_Sources: `concurrent/ExecutorUtilsTest.java:36-45`, `:69-89`; `concurrent/CustomizedThreadFactoryTest.java:40-61`_

`ExecutorUtilsTest.testShutdownOnExit` (`:47-67`) drains the hook queue and runs the registered callbacks manually instead of exiting the JVM — the pattern to copy in your own tests.

### 4.3 The two delegating bases

| Type | Wraps | Delegate mutability | Notes |
|---|---|---|---|
| `DelegatingBlockingQueue<E>` | `BlockingQueue<E>` | `final` (`:57`) | implements `BlockingQueue<E>, DelegatingWrapper` (`:55`); every `BlockingQueue` method is a one-line forward |
| `DelegatingScheduledExecutorService` | `ScheduledExecutorService` | `volatile`, replaceable via `setDelegate` (`:58`, `:64-66`) | implements the interface directly; it is **not** a `DelegatingWrapper` |

`DelegatingBlockingQueue`'s constructor flattens nested wrappers (`:59-62`): `tryUnwrap(delegate, BlockingQueue.class)`
returns the inner queue when the argument is itself a matching `Wrapper`, so
`new DelegatingBlockingQueue<>(new DelegatingBlockingQueue<>(raw))` points at `raw`, not at the outer decorator
(`lang/Wrapper.java:113-121`, `util/ObjectUtils.defaultIfNull`). `equals` (`:110-122`) and `hashCode` (`:124-127`) are
delegate-based, so a delegating queue compares equal to the queue it wraps.

`DelegatingScheduledExecutorService` is the base to extend when you want to observe or intercept scheduling: `delegate` is
`volatile` and swappable, so you can hot-swap the real executor at runtime — but the javadoc claim "this class is
thread-safe" (`:49-50`) only covers the reference swap; a `setDelegate` between `schedule(...)` and its `Future` result
simply means the task ran on the previous executor.

```java
ScheduledExecutorService delegate = newSingleThreadScheduledExecutor();
DelegatingScheduledExecutorService instance = new DelegatingScheduledExecutorService(delegate);
assertSame(delegate, instance.getDelegate());
assertEquals("Hello,World", instance.schedule(() -> "Hello,World", 1, MILLISECONDS).get());

BlockingQueue<Integer> raw = new LinkedBlockingDeque<>();
DelegatingBlockingQueue<Integer> queue = new DelegatingBlockingQueue<>(raw);
assertTrue(queue.offer(1));
assertEquals(MAX_VALUE, queue.remainingCapacity());   // unbounded delegate
assertEquals(queue, raw);                             // delegate-based equals
```

_Sources: `concurrent/DelegatingScheduledExecutorServiceTest.java:28-50`, `concurrent/DelegatingBlockingQueueTest.java:36-134`_

---

## 5. Current process id, child processes and exit (`io.microsphere.process`)

### 5.1 Getting a PID portably

Callers never pick a resolver; they call `ManagementUtils.getCurrentProcessId()`
(`management/ManagementUtils.java:56-58`), which yields `ProcessIdResolver.UNKNOWN_PROCESS_ID` (`-1L`,
`process/ProcessIdResolver.java:59`) when nothing resolved. Behind it, three `ProcessIdResolver` implementations are
discovered through SPI, sorted by priority, and the first that both `supports()` the JVM and returns a non-`null`
`current()` wins (`management/ManagementUtils.java:24-32`):

```java
public interface ProcessIdResolver extends Prioritized {   // process/ProcessIdResolver.java:54
    long UNKNOWN_PROCESS_ID = -1L;                        // :59
    boolean supports();                                   // :66
    Long current();                                       // :73
}
```

| Resolver | `getPriority()` | `supports()` is | PID source | Active on |
|---|---|---|---|---|
| `ModernProcessIdResolver` | `NORMAL_PRIORITY + 1` = **1** (`:73-76`) | `java.lang.ProcessHandle` resolves (`:58-61`) | `ProcessHandle.current().pid()` — reflectively (`:64-71`) | JDK 11/17/21/25 |
| `VirtualMachineProcessIdResolver` | `NORMAL_PRIORITY + 5` = **5** (`:99-102`) | the private `jvm` field exists on the `RuntimeMXBean` (`:83-86`) | `sun.management.VMManagementImpl#getProcessId()` (`:88-97`) | JDK 8 (and, if reached, 9+) |
| `ClassicProcessIdResolver` | `NORMAL_PRIORITY + 9` = **9** (`:91-94`) | the `pid@host` prefix is numeric (`:77-80`) | `RuntimeMXBean.getName()` substring before `@` (`:73-89`) | every JDK, as last resort |

Lower numbers run first: `lang/Prioritized.java:102-105` compares `getPriority()` numerically and `ServiceLoaderUtils`
sorts with `Prioritized.COMPARATOR` (`util/ServiceLoaderUtils.java:890`). Note the inversion — `MAX_PRIORITY` is
`Integer.MIN_VALUE` and `MIN_PRIORITY` is `Integer.MAX_VALUE` (`lang/Prioritized.java:81`, `:86`), so "max priority" means "smallest number".

```java
long processId = getCurrentProcessId();
assertTrue(processId > 0);
assertEquals(currentProcessId, getCurrentProcessId());   // the cached static field

// Per-resolver, version-conditional usage:
static final boolean isGEJava9 = testCurrentJavaVersion(GE, JAVA_VERSION_9);
ProcessIdResolver resolver = new ModernProcessIdResolver();
assertEquals(isGEJava9, resolver.supports());
if (isGEJava9) assertNotNull(resolver.current());
else assertThrows(IllegalArgumentException.class, resolver::current);  // JDK 8: PROCESS_HANDLE_CLASS is null
```

_Sources: `management/ManagementUtilsTest.java:19-24`, `process/ModernProcessIdResolverTest.java:26-45`, `process/ClassicProcessIdResolverTest.java:28-41`, `process/VirtualMachineProcessIdResolverTest.java:28-41`_

> [!NOTE]
> That `else` branch is why `supports()` is mandatory: `current()` is *unsupported-throwing*, not null-safe
> (`process/ModernProcessIdResolver.java:65`, pinned by `process/ModernProcessIdResolverTest.java:43`), so
> `ManagementUtils` filters on `supports()` before mapping (`management/ManagementUtils.java:27-28`).

### 5.2 Registered SPI implementations

`microsphere-java-core/src/main/resources/META-INF/services/io.microsphere.process.ProcessIdResolver` lists
`ModernProcessIdResolver`, `VirtualMachineProcessIdResolver` and `ClassicProcessIdResolver` — one of the 13 service files
in core, and the only one contributed by this group (`management` and `security` register none).

Declaration order is **irrelevant** — `ServiceLoaderUtils.loadServicesAsList` re-sorts with `Prioritized.COMPARATOR`
(`util/ServiceLoaderUtils.java:888-892`). What matters is that the file exists: if shading drops it, `loadServicesAsList`
throws `IllegalArgumentException` (`:882-886`) from `ManagementUtils`' **static initializer**
(`management/ManagementUtils.java:22`), so the failure is an `ExceptionInInitializerError` on first touch, not a `-1`.

### 5.3 `ProcessManager` — tracking unfinished child processes

```java
public class ProcessManager {                                                       // :40
    public static final ProcessManager INSTANCE = new ProcessManager();             // :45
    final ConcurrentMap<Process, String> unfinishedProcessesCache = newConcurrentHashMap();  // :47
    protected ProcessManager addUnfinishedProcess(Process process, String arguments);    // :52
    protected ProcessManager removeUnfinishedProcess(Process process, String arguments); // :57
    @Nonnull @Immutable public Map<Process, String> unfinishedProcessesMap();            // :67-71
}
```

The public surface is **only** the singleton and the read-only snapshot. `addUnfinishedProcess` / `removeUnfinishedProcess`
are `protected` (`:52`, `:57`), so package code — `ProcessExecutor` — and subclasses are the only writers, even though the
class javadoc's example calls `ProcessManager.INSTANCE.addUnfinishedProcess(process, "some-command")` from outside
(`:25-27`). There is likewise **no `destroy(Process)` method** anywhere in the class (`:40-72`), despite the javadoc bullet
"Safely destroy a process" (`:19`) and its example line `ProcessManager.INSTANCE.destroy(process)` (`:30`). Do not document
or code against those; call `Process.destroy()` on the JDK object directly.

```java
INSTANCE.unfinishedProcessesCache.clear();                      // package-visible test hook

ProcessExecutor processExecutor = new ProcessExecutor("java", "-version");
ExecutorService executorService = newFixedThreadPool(1);
ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8 * 1024);
Future<Boolean> future = executorService.submit(
        () -> { processExecutor.execute(outputStream, SECONDS.toMillis(2)); return true; });

while (!future.isDone())
    INSTANCE.unfinishedProcessesMap().forEach((process, options) -> {
        assertNotNull(process);
        assertEquals(" -version", options);   // options, with the leading space
    });
executorService.shutdown();
```

_Sources: `process/ProcessManagerTest.java:28-60`_

The `while` loop genuinely observes the registry: `ProcessExecutor` puts the `Process` before draining its stream and
removes it in a `finally` (`process/ProcessExecutor.java:161`, `:175`).

### 5.4 `ProcessExecutor` — execution with a timeout

```java
public class ProcessExecutor {                                                              // :58
    public static final String DEFAULT_PROCESS_EXECUTION_TIMEOUT_PROPERTY_VAUE = "30000";   // :65 (name typo is in the API)
    public static final long DEFAULT_PROCESS_EXECUTION_TIMEOUT = 30000L;                    // :70
    @ConfigurationProperty(...) public static final String PROCESS_EXECUTION_TIMEOUT_PROPERTY_NAME = "process.execution.timeout"; // :78-83
    public static final long DEFAULT_TIMEOUT = getLong(PROCESS_EXECUTION_TIMEOUT_PROPERTY_NAME, 30000L); // :88
    public ProcessExecutor(String command, String... options)                                // :104-115
    public void execute(OutputStream outputStream) throws IOException, TimeoutException;     // :124-126
    public void execute(OutputStream outputStream, long timeoutInMilliseconds);              // :136-138
    public void execute(OutputStream outputStream, long timeout, TimeUnit timeUnit);         // :149-192
}
```

The command line is joined with spaces and then **split on `\s+`** (`:105-113`, `:154`), so quoted arguments containing spaces
are not preserved. Each instance owns a **single-thread executor** built from `newThreadFactory("process-exec", true)` and
registers it with `shutdownOnExit` (`:112-114`) — a daemon thread that dies with the JVM. `stderr` is merged into `stdout`
via `redirectErrorStream(true)` (`:155`), so one drain suffices and the pipe-full deadlock cannot occur. A non-zero exit code
raises `IOException` (`:170-173`); a timeout raises `TimeoutException` after `future.cancel(true)` (`:186-188`); anything else is re-wrapped into `IOException` (`:189-191`).

```java
FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream(8 * 1024);

new ProcessExecutor("java", "-version").execute(outputStream);   // happy path
assertTrue(outputStream.size() > 0);

assertThrows(TimeoutException.class, () -> new ProcessExecutor("javac", "--help").execute(outputStream, 1));
assertThrows(IOException.class, () -> new ProcessExecutor("javac", "-a").execute(outputStream));       // non-zero exit
assertThrows(IOException.class, () -> new ProcessExecutor("not-found-command").execute(outputStream)); // command missing
```

_Sources: `process/ProcessExecutorTest.java:38-64`_

---

## 6. Management: MXBean facade, attributes and MBean builders

### 6.1 `JmxUtils` — the `ManagementFactory` facade

`JmxUtils` (`management/JmxUtils.java:87`) is a static utility that lazily caches the platform MXBeans in plain
(non-`volatile`) static fields (`:101-117`) and exposes attribute introspection:

| Group | Methods | Returns |
|---|---|---|
| singletons | `getClassLoadingMXBean` `:132-138`, `getMemoryMXBean` `:153-159`, `getThreadMXBean` `:174-180`, `getRuntimeMXBean` `:195-201`, `getOperatingSystemMXBean` `:248-254` | the same instance every call |
| optional | `getCompilationMXBean()` `:223-229` | `Optional<CompilationMXBean>` — empty when the JVM has no compiler |
| lists | `getMemoryPoolMXBeans` `:275-282`, `getMemoryManagerMXBeans` `:306-313`, `getGarbageCollectorMXBeans` `:337-344` | `unmodifiableList`, cached once |
| attributes | `getMBeanAttributesMap(server, name)` `:370-385`, `getMBeanAttributes(...)` `:413-429`, `getAttribute(...)` `:455-458` / `:483-486`, `findMBeanAttributeInfo(...)` `:513-534` | `MBeanAttribute` snapshots (`management/MBeanAttribute.java`); `EMPTY_*` arrays at `:91-99` |
| metadata | `getMBeanInfo(server, name)` `:560-568`, `methodSignature(Method)` `:573-575`, `signature(Parameter[])` `:577-582`, `descriptorForElement(...)` `:588-591`, `descriptorForAnnotations(...)` `:597-619` | port of `com.sun.jmx.mbeanserver.Introspector` behaviour |

Degradation is the rule: an unknown `ObjectName` yields `EMPTY_MBEAN_ATTRIBUTE_ARRAY` (`:416-418`) or the shared
`emptyMap()` (`:375-377`), `getMBeanInfo` returns `null` after logging (`:560-568`), and a missing attribute yields `null`
plus a `warn` (`:529-531`). `descriptorForAnnotations` returns `ImmutableDescriptor.EMPTY_DESCRIPTOR` when no
`@DescriptorKey` member is present (`:614-618`; both branches asserted at `.../management/JmxUtilsTest.java:329-343`).

```java
MBeanServer mBeanServer = getPlatformMBeanServer();
ObjectName objectName = getInstance("java.lang:type=ClassLoading");

Map<String, MBeanAttribute> attributesMap = getMBeanAttributesMap(mBeanServer, objectName);
assertEquals(5, attributesMap.size());   // Verbose, Total/Loaded/UnloadedClassCount, ObjectName
assertNotNull(getAttribute(mBeanServer, objectName, "Verbose"));
assertEquals(objectName, getAttribute(mBeanServer, objectName, "ObjectName"));

ObjectName notFoundObjectName = getInstance("java.lang:type=NotFound");   // silent degradation
assertSame(EMPTY_MBEAN_ATTRIBUTE_ARRAY, getMBeanAttributes(mBeanServer, notFoundObjectName));
assertSame(emptyMap(), getMBeanAttributesMap(mBeanServer, notFoundObjectName));
assertNull(getAttribute(mBeanServer, notFoundObjectName, "Verbose"));
assertNull(getAttribute(mBeanServer, objectName, "NotFound"));
```

_Sources: `management/JmxUtilsTest.java:107-113`, `:179-199`, `:201-205`, `:208-225`_

> [!WARNING]
> The one exception is `doGetAttribute` (`:621-631`): it swallows `ReflectionException`, `InstanceNotFoundException` and
> `AttributeNotFoundException`, but re-throws **everything else** as `wrap(e, RuntimeException.class)` (`:627-628`), so a
> `RuntimeMBeanException` caused by the MBean's own getter escapes — asserted at `.../management/JmxUtilsTest.java:293`.

`MBeanAttribute` (`management/MBeanAttribute.java:59`) is the value object: it pairs a `MBeanInfo`, a
`MBeanAttributeInfo` and the read value, delegating `getName` / `getType` / `isReadable` / `isWritable` / `isIs` to the info
(`:86-123`) and exposing the value via `getValue()` (`:136-138`). Its constructor (`requireNonNull` on the two metadata
arguments, `:70-71`) is the only throw site in the class.

### 6.2 `@DescriptorKey` → `Descriptor`

`descriptorForElement(AnnotatedElement)` reads an element's annotations and turns every `@DescriptorKey`-annotated
annotation member into a descriptor field (`:588-619`). The fixture shows the intended use: `management/CacheControlMBean.java:29-32`
annotates with `@Units("bytes")`, and `Units.value()` is `@DescriptorKey("units")` (`management/Units.java:41-42`).

```java
Method method = findMethod(CacheControlMBean.class, "getCacheSize");
Descriptor descriptor = descriptorForElement(method);
assertArrayEquals(ofArray("units"), ((ImmutableDescriptor) descriptor).getFieldNames());
assertEquals("bytes", ((ImmutableDescriptor) descriptor).getFieldValue("units"));
```

_Sources: `management/JmxUtilsTest.java:319-326`_

### 6.3 The builder API (`io.microsphere.management.builder`)

Nine types build `javax.management` metadata objects. They are a **family**, not a fluent chain on one class:

| Builder | Extends | Adds | `build()` produces |
|---|---|---|---|
| `MBeanDescribableBuilder<B>` (`:34`, abstract) | — | `description`, `descriptor` (`:40`, `:46`; setters `:51-59`) | abstract, `:61` |
| `MBeanFeatureInfoBuilder<B>` (`:32`) | describable | `name` (`:40`, `:45-48`) | `MBeanFeatureInfo` (`:50-52`) |
| `MBeanAttributeInfoBuilder` (`:34`) | feature | `type`/`write`/`read`/`is` (`:40-55`, `:61-74`) | `MBeanAttributeInfo` (`:76-79`) |
| `MBeanParameterInfoBuilder` (`:40`) | feature | `type` (`:46`) | `MBeanParameterInfo` (`:52-54`) |
| `MBeanExecutableInfoBuilder<B>` (`:48`, abstract) | feature | `parameters`, `signature(...)`, `param(...)`, `from(Executable)` (`:50-88`) | inherited |
| `MBeanConstructorInfoBuilder` (`:33`) | executable | — | `MBeanConstructorInfo` (`:39-41`) |
| `MBeanOperationInfoBuilder` (`:38`) | executable | `returnType`, `impact` + nested `enum Impact` (`:44-116`) | `MBeanOperationInfo` (`:62-64`) |
| `MBeanNotificationInfoBuilder` (`:40`) | describable | `types` (`:43`, `:49-56`) | `MBeanNotificationInfo` (`:58-60`) |
| `MBeanInfoBuilder` (`:48`) | **describable** (not feature) | `className` + four builder lists (`:54-66`) | `MBeanInfo` (`:127-130`) |

Every setter returns the builder (`(B) this`), every builder exposes a static factory (`mbeanInfo`, `attribute`, `operation`, `constructor`, `notification`, `parameter`), and `build()` maps onto exactly one JDK constructor.

> [!IMPORTANT]
> **What actually gets registered, and under what name:** nothing in this group calls `MBeanServer.registerMBean`. The
> builders only produce `MBeanInfo` / `MBean*Info` metadata; **the caller registers**. In the tests the object names are
> caller-chosen — `io.microsphere.management:type=Test` and `io.microsphere.management:type=CacheControl`
> (`.../management/JmxUtilsTest.java:234`+`:267`, `:284`+`:291`) — alongside the platform `java.lang:type=…` names used for
> reading. A duplicate registration is an error you must handle yourself (see pitfall 8).

```java
MBeanInfoBuilder builder = mbeanInfo(Data.class.getName());

builder.attribute("test-name", String.class, b -> { b.is(true); b.read(true); b.write(true); });
builder.operation("test-name", String.class, b -> b.impact(ACTION));
builder.constructor(b -> { b.name(TEST_NAME); b.description(TEST_DESCRIPTION); });
builder.notification(String.class);

// Or derive the whole descriptor graph from a JavaBean:
BeanInfo beanInfo = getBeanMetadata(Data.class).getBeanInfo();
MBeanInfo info = mbeanInfo(beanInfo).build();
assertEquals(getTypeName(Data.class), info.getClassName());
assertEquals(beanInfo.toString(), info.getDescription());
```

`mbeanInfo(BeanInfo)` walks `PropertyDescriptor[]` → attributes (with `is`/`read`/`write` derived from the accessor methods,
`management/builder/MBeanInfoBuilder.java:75-85`), `MethodDescriptor[]` → operations (`:94-103`) and
`getDeclaredConstructors()` → constructors (`:179-182`).

_Sources: `management/builder/MBeanInfoBuilderTest.java:59-97`, `:162-204`; `management/builder/AbstractMBeanFeatureInfoBuilderTest.java:42-66`_

---

## 7. Security (`io.microsphere.security`)

`SecurityUtils` is a three-method helper for one system property (`security/SecurityUtils.java:39`):

```java
public abstract class SecurityUtils implements Utils {
    @ConfigurationProperty(source = SYSTEM_PROPERTIES_SOURCE) public static final String JAVA_SECURITY_POLICY_FILE_PROPERTY_NAME = "java.security.policy";  // :44-45
    public static void setJavaSecurityPolicyFile(@Nonnull String javaSecurityPolicyFilePath);     // :52-54
    public static void setJavaSecurityPolicyFile(@Nonnull File javaSecurityPolicyFile);           // :61-63
    @Nullable public static String getJavaSecurityPolicyFile();                                    // :70-73
}
```

What it does: writes/reads `java.security.policy`, nothing else — the `File` overload just forwards `getAbsolutePath()`
(`:62`). What it does **not** do: install a `SecurityManager`, read the file, parse grants, or force a re-load; its javadoc's
`@see SecurityManager` (`:35`) is a documentation link only. Callers touch it when an application or test needs the policy
file location published for the JDK's own policy machinery, typically from a launcher or a `@BeforeAll` hook.

```java
File policyFile = new File(getResource("META-INF/class-load-test.policy").getFile());
setJavaSecurityPolicyFile(policyFile);
assertEquals(policyFile.getAbsolutePath(), getJavaSecurityPolicyFile());
clearProperty(JAVA_SECURITY_POLICY_FILE_PROPERTY_NAME);   // tear down
```

_Sources: `security/SecurityUtilsTest.java:46-71`_

> [!WARNING]
> Do not build on `SecurityManager`: deprecated **for removal** since JDK 17 (JEP 411) and permanently disabled in JDK 24
> (JEP 486), where `System.setSecurityManager(non-null)` throws `UnsupportedOperationException`. `SecurityUtils` sidesteps
> this because it only manipulates a property, so it is green on all five CI JDKs (8/11/17/21/25). The repository's
> `TestSecurityManager` helper (`.../test/java/io/microsphere/security/TestSecurityManager.java:27`, `:38`, `:52`) would not
> run on 24+ — but nothing references it, so it is dead test code, not a supported feature.

---

## 8. Common pitfalls

1. **JDK 8 has no `ProcessHandle`.** `resolveClass("java.lang.ProcessHandle")` returns `null`, so
   `ModernProcessIdResolver.supports()` is `false` (`process/ModernProcessIdResolver.java:54-61`) and its `current()` *throws*
   `IllegalArgumentException` rather than returning `null` (`:65`, pinned by `process/ModernProcessIdResolverTest.java:39-44`).
   The fallback that serves JDK 8 is `VirtualMachineProcessIdResolver` (priority 5), `ClassicProcessIdResolver` as net (§5.1).
2. **`SecurityManager` deprecation across JDK 17 → 25.** Nothing here installs one, but deprecated-for-removal (JEP 411,
   JDK 17) plus the JDK 24 disablement (JEP 486) mean any caller that follows `SecurityUtils`' `@see SecurityManager` link
   breaks on CI's 21/25 legs. Use `SecurityUtils` for the property only.
3. **`ManagementUtils.getCurrentProcessId()` never refreshes.** Captured in a static initializer
   (`management/ManagementUtils.java:22`); a re-exec'd JVM or a container re-entering a PID namespace keeps the value
   resolved at first class load.
4. **A missing SPI file is fatal, not `-1`.** `loadServicesAsList` throws `IllegalArgumentException` when nothing is
   registered (`util/ServiceLoaderUtils.java:882-886`), and `ManagementUtils` calls it from the static initializer at
   `management/ManagementUtils.java:22`/`:25` — so shade-stripping the service file yields `ExceptionInInitializerError`.
5. **A resolver that throws escapes the "silent degradation".** The chain tolerates only `null`
   (`management/ManagementUtils.java:26-31`); `UNKNOWN_PROCESS_ID` is the fallback for *returning* `null`, never for
   *throwing*. The javadoc promise at `:51` is stronger than the code.
6. **Container / cgroup PID expectations.** All three sources report the PID as the *current PID namespace* sees it, so in
   a container sharing one (Docker/Kubernetes default) you get the namespace-local PID — typically `1` for a `PID 1`
   entrypoint — not the host PID. `ClassicProcessIdResolver` parses `RuntimeMXBean.getName()`'s `pid@host` with a plain
   `substringBefore` (`process/ClassicProcessIdResolver.java:73-75`), so a JVM name lacking `@` makes `supports()`
   `false` (`:77-80`) and leaves you with `-1`.
7. **`process.execution.timeout` is read once, at class load.** `DEFAULT_TIMEOUT` is a static field initialised through
   `getLong(...)` (`process/ProcessExecutor.java:88`); `System.setProperty` after loading has no effect — use `-D`. The
   constant name is misspelled (`DEFAULT_PROCESS_EXECUTION_TIMEOUT_PROPERTY_VAUE`, `:65`) and is public API on `0.3.20`.
8. **MBean registration fails when the name is already registered.** This group only builds `MBeanInfo`; the
   `registerMBean(...)` call is yours, and a duplicate `ObjectName` raises `InstanceAlreadyExistsException`. `JmxUtils` will
   not warn you — `getAttribute(...)` on the *stale* registration silently returns the old value (`management/JmxUtils.java:621-631`).
   Unregister in `@AfterEach`/shutdown or guard with `isRegistered(name)`; the tests register each name once per JVM
   (`.../management/JmxUtilsTest.java:267`, `:291`).
9. **`build()` performs almost no validation of its own.** The builders pre-fill no defaults — `mbeanInfo(String)` leaves
   `description` and `descriptor` `null` (`.../management/builder/MBeanInfoBuilderTest.java:154-159`) — and `build()` forwards
   them straight to the JDK constructor (`management/builder/MBeanInfoBuilder.java:127-130`). Only
   `MBeanAttributeInfoBuilder.attribute(String)` guards itself, via `assertNotNull` → `IllegalArgumentException`
   (`management/builder/MBeanAttributeInfoBuilder.java:86`, `util/Assert.java:149-153`); anything else surfaces from `javax.management`.
10. **`ExecutorUtils.shutdown(ExecutorService)` returns `true` twice.** First and second calls on the same executor both
    return `true` (`concurrent/ExecutorUtils.java:131-142`; asserted at `.../concurrent/ExecutorUtilsTest.java:81-84`), so
    it is not a "did I shut it down just now?" signal; only the non-`ExecutorService`/`null` branches yield `false`.
11. **`shutdownOnExit` never removes what it registered.** Each call adds one callback to the shared hook queue
    (`concurrent/ExecutorUtils.java:63-68`), and `ProcessExecutor` registers per instance
    (`process/ProcessExecutor.java:114`) — so pool `ProcessExecutor`s, don't create them in loops.
12. **`ProcessExecutor` argument splitting.** `commandLine.split("\\s+")` (`process/ProcessExecutor.java:154`) breaks any
    option containing whitespace, and the join at `:105-113` keeps a leading space in the recorded `options` — which is why
    `ProcessManagerTest` asserts `" -version"`, not `"version"` (`.../process/ProcessManagerTest.java:52`).
13. **`ProcessManager` has no `destroy(...)`.** Its javadoc advertises one (`process/ProcessManager.java:19`, `:30`) but the
    class body (`:40-72`) defines none, and the add/remove writers are `protected` (`:52`, `:57`); external code can only
    read `unfinishedProcessesMap()`.

---

## 9. See also

* [Language abstractions](./language-abstractions.md) — `Prioritized`, `Wrapper` / `DelegatingWrapper`, which drive resolver ordering and queue unwrapping
* [Utilities, constants and text](./util-and-constants.md) — `ShutdownHookUtils`, `ServiceLoaderUtils`, `ClassLoaderUtils.resolveClass`, `Assert`
* [Reflection, types and beans](./reflection-and-beans.md) — `MethodUtils` / `FieldUtils` (the reflective PID paths) and `BeanUtils.getBeanMetadata`
* [Collections and filters](./collections-and-filters.md) — `MapUtils.newConcurrentHashMap`, `ListUtils.newLinkedList`
* [IO and scanning](./io-and-scanning.md) — `FastByteArrayOutputStream` / `IOUtils.copy`, used by `ProcessExecutor`
* [Logging](./logging.md) — the `Logger` every type in this group obtains from `LoggerFactory`
* [Class loading and artifact detection](./classloading-and-artifacts.md) — the sibling JDK-version-dispatch story
* Developer-facing deep dive: [Concurrency, process, management and security — Developer Guide](../../developer-guide/microsphere-java-core/concurrency-process-and-management.md)
