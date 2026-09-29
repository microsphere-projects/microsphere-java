# Concurrency, Processes and JMX

> Read this page in: [中文](../zh/concurrency-process-jmx.md) · [English](concurrency-process-jmx.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Module | `io.github.microsphere-projects:microsphere-java-core` |
| Packages | `io.microsphere.concurrent`, `io.microsphere.process`, `io.microsphere.management`, `io.microsphere.management.builder`, `io.microsphere.security` |
| Content | named thread factories, executor shutdown helpers, external command execution, process id resolution, JMX bean accessors, MBean metadata builders, `java.security.policy` property |
| SPI files | `META-INF/services/io.microsphere.process.ProcessIdResolver` (3 resolvers) |
| Main tunable | system property `process.execution.timeout` (milliseconds, default `30000`) |
| JDK sensitivity | high — the process-id resolvers and `VirtualMachineProcessIdResolver` rely on JDK internals |

These packages are small and mostly stateless. Two needs drive them: naming threads (the JDK gives no
`ThreadFactory` that does it) and reading back what a JVM exposes over JMX (`JmxUtils`, `management.builder`).

---

## 1. `io.microsphere.concurrent`

Four types, no more: `CustomizedThreadFactory`, `ExecutorUtils`, `DelegatingScheduledExecutorService`,
`DelegatingBlockingQueue`.

### 1.1 `CustomizedThreadFactory`

```java
public class CustomizedThreadFactory implements ThreadFactory {

    public static final boolean DEFAULT_DAEMON     = true;
    public static final int     DEFAULT_PRIORITY   = Thread.NORM_PRIORITY;
    public static final long    DEFAULT_STACK_SIZE = 0;

    // protected — subclasses only; applications use the static factories
    protected CustomizedThreadFactory(String namePrefix, boolean daemon, int priority, long stackSize)

    // four overloads: (prefix) / (prefix, daemon) / (prefix, daemon, priority) / (prefix, daemon, priority, stackSize)
    public static ThreadFactory newThreadFactory(String namePrefix, boolean daemon, int priority, long stackSize)

    @Override public Thread newThread(Runnable r)
}
```

* Thread names are `<namePrefix>-thread-<n>`, `n` starting at `1` per factory instance — thread dumps and
  `ThreadMXBean` output become readable.
* The `ThreadGroup` comes from the thread that **creates the factory** (`Thread.currentThread().getThreadGroup()`),
  so build the factory where you build the pool.
* `DEFAULT_DAEMON` is `true`: a pool built on this factory will not keep the JVM alive. Pass `false` explicitly for
  non-daemon threads.
* The constructor is `protected`, so the only supported entry point is `newThreadFactory(...)`.

```java
ExecutorService pool = Executors.newFixedThreadPool(4,
        CustomizedThreadFactory.newThreadFactory("order-dispatch"));

EventDispatcher dispatcher = EventDispatcher.parallel(pool);
```

### 1.2 `ExecutorUtils`

```java
public abstract class ExecutorUtils implements Utils {
    public static void shutdownOnExit(Executor one, Executor... others)
    public static boolean shutdown(Executor executor)
    public static boolean shutdown(ExecutorService executorService)
}
```

| Method | Returns / does |
|---|---|
| `shutdownOnExit(one, others...)` | registers a callback with `ShutdownHookUtils.addShutdownHookCallback(...)` that calls `shutdown(...)` on each executor at JVM exit; returns `void` |
| `shutdown(Executor)` | `false` when the argument is **not** an `ExecutorService` (e.g. `Runnable::run`), otherwise delegates to the overload below |
| `shutdown(ExecutorService)` | `false` only for `null`; `true` otherwise. Calls `ExecutorService#shutdown()` only when `!isShutdown()`, so it is safe to call twice |

`ProcessExecutor` uses both idioms internally: its private executor is created with `newThreadFactory("process-exec", true)`
and passed to `shutdownOnExit(...)`.

### 1.3 Delegating executors and queues

| Type | Declaration |
|---|---|
| `DelegatingScheduledExecutorService` | `implements ScheduledExecutorService`; ctor `(ScheduledExecutorService delegate)`; `setDelegate(...)` / `getDelegate()`; delegates every `schedule` / `scheduleAtFixedRate` / `scheduleWithFixedDelay` / `submit` / `invokeAll` / `invokeAny` / `shutdown` / `shutdownNow` / `isShutdown` / `isTerminated` / `awaitTermination` / `execute` call |
| `DelegatingBlockingQueue<E>` | `implements BlockingQueue<E>, DelegatingWrapper`; ctor `(BlockingQueue<E> delegate)`; `Object getDelegate()`; full delegation |

`DelegatingBlockingQueue` collapses double wrapping: its constructor stores
`tryUnwrap(delegate, BlockingQueue.class)` when the delegate is itself a wrapper. These two are the base classes for
timed / traced executors — extend, override the one method you care about, and let callers recover the real object
through `Wrapper.tryUnwrap`.

```java
public class TimedScheduledExecutor extends DelegatingScheduledExecutorService {

    public TimedScheduledExecutor(ScheduledExecutorService delegate) {
        super(delegate);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(() -> {
            long start = System.nanoTime();
            try { command.run(); } finally { record(System.nanoTime() - start); }
        });
    }
}
```

---

## 2. `io.microsphere.process`

### 2.1 `ProcessExecutor`

```java
public class ProcessExecutor {

    public static final String DEFAULT_PROCESS_EXECUTION_TIMEOUT_PROPERTY_VAUE = "30000"; //sic: the constant name in source
    public static final long   DEFAULT_PROCESS_EXECUTION_TIMEOUT = 30_000L;
    public static final String PROCESS_EXECUTION_TIMEOUT_PROPERTY_NAME = "process.execution.timeout";
    public static final long   DEFAULT_TIMEOUT = /* the property above, falling back to 30000 */;

    public ProcessExecutor(String command, String... options)

    public void execute(OutputStream outputStream)                                        throws IOException, TimeoutException
    public void execute(OutputStream outputStream, long timeoutInMilliseconds)              throws IOException, TimeoutException
    public void execute(OutputStream outputStream, long timeout, TimeUnit timeUnit)         throws IOException, TimeoutException
}
```

```java
ProcessExecutor executor = new ProcessExecutor("git", "rev-parse", "HEAD");
FastByteArrayOutputStream out = new FastByteArrayOutputStream();
executor.execute(out, 10, TimeUnit.SECONDS);
String revision = out.toString("UTF-8").trim();
```

Observed behaviour:

* The command line is built as `command + " " + join(options)` and then split with `commandLine.split("\\s+")`, so an
  **argument containing a space cannot be passed**; keep each token separate and quote-free.
* `ProcessBuilder#redirectErrorStream(true)` merges stderr into the stream you supply.
* Execution runs inside a single-thread, daemon executor named `process-exec-thread-<n>`; the timeout is enforced by
  `future.get(timeout, unit)`. On timeout the task is cancelled with `future.cancel(true)` and `TimeoutException` is
  rethrown — the child process itself is not killed by the library.
* A non-zero exit value produces `IOException` with the message
  `"The command['...'] execution is exited with invalid value : <n>"`, wrapped out of the executor as `IOException`.
* `process.execution.timeout` (no `microsphere.` prefix, read through `Long.getLong`) changes `DEFAULT_TIMEOUT`, which
  only affects the one-argument `execute(OutputStream)` overload.

### 2.2 `ProcessIdResolver` SPI

```java
public interface ProcessIdResolver extends Prioritized {
    long UNKNOWN_PROCESS_ID = -1L;
    boolean supports();
    Long current();   // null when unresolvable
}
```

Registered in `META-INF/services/io.microsphere.process.ProcessIdResolver` and sorted by `Prioritized.COMPARATOR`
(**smaller priority value wins**), so the chain order is:

| Order | Implementation | Mechanism | `getPriority()` | Available when |
|---|---|---|---|---|
| 1 | `ModernProcessIdResolver` | reflective `java.lang.ProcessHandle.current().pid()` | `NORMAL_PRIORITY + 1` | JDK 9+ (`supports()` = the class resolves) |
| 2 | `VirtualMachineProcessIdResolver` | hidden `jvm` field of `RuntimeMXBean` → `getProcessId()` | `NORMAL_PRIORITY + 5` | the `jvm` field is found (HotSpot) |
| 3 | `ClassicProcessIdResolver` | parses `RuntimeMXBean.getName()` (`pid@host`) | `NORMAL_PRIORITY + 9` | the name starts with digits |

`VirtualMachineProcessIdResolver` deserves the detail, because it is the one that breaks across JDKs:

```java
final static Field JVM_FIELD = findField(getRuntimeMXBean(), "jvm"); // resolved once, in the static initializer

@Override public boolean supports() { return nonNull(JVM_FIELD); }   // "the field exists", nothing more

@Override public Long current() {                                     // forceAccess reads, HotSpot only
    Object jvm = getFieldValue(true, getRuntimeMXBean(), JVM_FIELD);
    return valueOf(((Integer) invokeMethod(true, jvm, "getProcessId")).longValue());
}
```

`supports()` only answers "does the field exist", which succeeds even on a locked-down JDK. The `forceAccess` reads
then need the package to be open: on JDK 16+ (JEP 396) a direct call to `current()` fails unless you add
`--add-opens java.management/sun.management=ALL-UNNAMED`. In practice this resolver is only reached on JDK 8, where
`ModernProcessIdResolver` is unavailable, so the chain works without flags.

> [!WARNING]
> Do not call `VirtualMachineProcessIdResolver` directly as a "portable pid" source. On JDK 16+ it throws; on
> non-HotSpot JVMs `supports()` is `false`. Go through `ManagementUtils.getCurrentProcessId()` instead.

### 2.3 `ProcessManager`

```java
public class ProcessManager {
    public static final ProcessManager INSTANCE;
    protected ProcessManager addUnfinishedProcess(Process process, String arguments)
    protected ProcessManager removeUnfinishedProcess(Process process, String arguments)
    @Nonnull @Immutable public Map<Process, String> unfinishedProcessesMap()
}
```

`add` / `remove` are `protected` (used by `ProcessExecutor` in the same package), so an application's only real entry
point is `ProcessManager.INSTANCE.unfinishedProcessesMap()` — an unmodifiable view over a `ConcurrentHashMap` of child
processes that have not finished. Values are the option strings, which makes leaked subprocesses diagnosable at
shutdown.

---

## 3. `io.microsphere.management`

### 3.1 `ManagementUtils`

```java
public abstract class ManagementUtils implements Utils {
    public static long getCurrentProcessId()   // ProcessIdResolver.UNKNOWN_PROCESS_ID (-1) when nothing resolves
}
```

The pid is resolved **once** into a static field when the class initialises: the resolvers are loaded, filtered by
`supports()`, mapped with `current()`, and the first non-`null` result wins; no exception is caught on the way.

> [!IMPORTANT]
> Because the resolution happens in a static initializer and `current()` is not guarded, a resolver that throws turns
> into `ExceptionInInitializerError` (then `NoClassDefFoundError` on later touches) the first time
> `ManagementUtils.getCurrentProcessId()` is called. If you register your own `ProcessIdResolver`, make `current()`
> return `null` instead of throwing.

### 3.2 `JmxUtils`

```java
public abstract class JmxUtils implements Utils {

    // MXBean accessors — lazily resolved once, cached in static fields
    public static ClassLoadingMXBean getClassLoadingMXBean()
    public static MemoryMXBean getMemoryMXBean()
    public static ThreadMXBean getThreadMXBean()
    public static RuntimeMXBean getRuntimeMXBean()
    public static Optional<CompilationMXBean> getCompilationMXBean()   // may be absent
    public static OperatingSystemMXBean getOperatingSystemMXBean()
    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans()
    public static List<MemoryManagerMXBean> getMemoryManagerMXBeans()
    public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans()

    // attribute inspection, never throws
    public static MBeanInfo getMBeanInfo(MBeanServer server, ObjectName objectName)
    public static MBeanAttribute[] getMBeanAttributes(MBeanServer server, ObjectName objectName)
    public static Map<String, MBeanAttribute> getMBeanAttributesMap(MBeanServer server, ObjectName objectName)
    public static Object getAttribute(MBeanServer server, ObjectName objectName, String attributeName)
    public static Object getAttribute(MBeanServer server, ObjectName objectName, MBeanAttributeInfo attributeInfo)
    public static MBeanAttributeInfo findMBeanAttributeInfo(MBeanServer server, ObjectName objectName, String attributeName)

    // DynamicMBean metadata helpers
    public static MBeanParameterInfo[] methodSignature(Method method)
    public static MBeanParameterInfo[] signature(Parameter[] parameters)
    public static Descriptor descriptorForElement(AnnotatedElement annotatedElement)
    public static Descriptor descriptorForAnnotations(Annotation[] annotations)
}
```

| Contract detail | Reality |
|---|---|
| `getCompilationMXBean()` | `Optional` — some JVMs ship without a compiler MBean; the other beans are returned directly |
| `getMBeanInfo(...)` | `null` when the MBean is missing: `InstanceNotFoundException`, `IntrospectionException` and `ReflectionException` are caught and logged |
| `getMBeanAttributes(...)` / `getMBeanAttributesMap(...)` | never `null` — `EMPTY_MBEAN_ATTRIBUTE_ARRAY` / `emptyMap()` when the MBean has no attributes |
| `getAttribute(...)` | `null` when the attribute is unreadable or the read failed |
| `descriptorForElement` / `descriptorForAnnotations` | reimplementations of `com.sun.jmx.mbeanserver.Introspector`, so no `sun.*` import is needed in your code |
| all beans and lists | lazily resolved once and cached in static fields; lists are unmodifiable |

```java
MBeanServer server = ManagementFactory.getPlatformMBeanServer();
ObjectName name = new ObjectName("java.lang:type=Memory");

Map<String, MBeanAttribute> attributes = JmxUtils.getMBeanAttributesMap(server, name);
MBeanAttribute heap = attributes.get("HeapMemoryUsage");   // metadata + read value in one object
System.out.printf("%s = %s%n", heap.getName(), heap.getValue());
```

`MBeanAttribute` bundles both halves of an attribute: a `@Nonnull MBeanInfo getDeclaringMBeanInfo()`,
`MBeanAttributeInfo getAttributeInfo()`, `getName()`, `getType()`, `isReadable()`, `isWritable()`, `isIs()`,
`getValue()`, built with `new MBeanAttribute(declaringMBeanInfo, attributeInfo, value)`.

### 3.3 `management.builder` — MBean metadata DSL

Fluent builders for programmatic `DynamicMBean` definitions. Every builder constructor is **package-private**, so you
always enter through the static factory:

| Builder | Entry points | Setters | Produces |
|---|---|---|---|
| `MBeanInfoBuilder` | `mbeanInfo(String className)`, `mbeanInfo(BeanInfo beanInfo)` | `attribute(String, Class, Consumer<...>)`, `attribute(PropertyDescriptor)`, `operation(String, Class, Consumer<...>)`, `operation(Method)`, `operation(MethodDescriptor)`, `constructor(Constructor<?>)`, `constructor(Consumer<...>)`, `notification(Class...)`, `notification(Consumer<...>)`, `description(String)`, `descriptor(Descriptor)` | `MBeanInfo` |
| `MBeanAttributeInfoBuilder` | `attribute(Class<?> type)`, `attribute(String type)` | `name(String)`, `read(boolean)`, `write(boolean)`, `is(boolean)`, `description(String)`, `descriptor(Descriptor)` | `MBeanAttributeInfo` |
| `MBeanOperationInfoBuilder` | `operation(Class<?> type)`, `operation(String type)`, `operation(Method method)` | `name(String)`, `signature(MBeanParameterInfo...)`, `param(Class, Consumer<...>)`, `from(Executable)`, `impact(Impact)`, `description(String)` | `MBeanOperationInfo` |
| `MBeanConstructorInfoBuilder` | `constructor()`, `constructor(Constructor<?>)` | `name(String)`, `signature(...)`, `param(...)`, `from(Executable)` | `MBeanConstructorInfo` |
| `MBeanParameterInfoBuilder` | `parameter(Class<?>)`, `parameter(String)`, `parameter(Parameter)` | `name(String)`, `description(String)` | `MBeanParameterInfo` |
| `MBeanNotificationInfoBuilder` | `notification()`, `notification(Class...)`, `notification(String...)` | `name(String)`, `types(Class...)`, `types(String...)`, `description(String)` | `MBeanNotificationInfo` |

Shared bases: `MBeanDescribableBuilder<B>` (`description`, `descriptor`, abstract `build()`),
`MBeanFeatureInfoBuilder<B>` (`name`, `build()`), `MBeanExecutableInfoBuilder<B>` (`signature`, `param`,
`from(Executable)`, `toSignature()`).

`MBeanOperationInfoBuilder.impact(...)` takes the builder's own nested enum
`MBeanOperationInfoBuilder.Impact` — `INFO`, `ACTION`, `ACTION_INFO`, `UNKNOWN` — which maps to the `MBeanOperationInfo`
int constants; the default is `UNKNOWN`.

```java
import static io.microsphere.management.builder.MBeanInfoBuilder.mbeanInfo;
import static io.microsphere.management.builder.MBeanOperationInfoBuilder.Impact.ACTION;

MBeanInfo info = mbeanInfo("com.example.CacheAdmin")
        .description("Cache management")
        .operation("clear", void.class, op -> op.name("clear").impact(ACTION))
        .attribute("size", int.class, a -> a.read(true).write(false).description("Entry count"))
        .build();
```

`operation(...)` and `attribute(...)` accept a `Consumer` of the sub-builder, and `from(Executable)` /
`parameter(Parameter)` derive names, signatures and descriptors from real `Method` / `Constructor` objects — so a
`DynamicMBean` can be generated from an interface instead of hand-written.

---

## 4. `io.microsphere.security`

```java
public abstract class SecurityUtils implements Utils {

    @ConfigurationProperty(source = SYSTEM_PROPERTIES_SOURCE)
    public static final String JAVA_SECURITY_POLICY_FILE_PROPERTY_NAME = "java.security.policy";

    public static void setJavaSecurityPolicyFile(@Nonnull String javaSecurityPolicyFilePath)
    public static void setJavaSecurityPolicyFile(@Nonnull File javaSecurityPolicyFile)   // uses getAbsolutePath()
    @Nullable public static String getJavaSecurityPolicyFile()
}
```

A thin wrapper over the `java.security.policy` system property, annotated with `@ConfigurationProperty` so it shows up
in generated configuration metadata. Setting the property only *locates* a policy file — installing a `SecurityManager`
remains your responsibility, and the security manager itself has been deprecated for removal since JDK 17. Treat this
class as legacy-support surface, not a place to build new integrations.

---

## 5. JDK version boundaries you will actually hit

| API | JDK 8 | JDK 9 – 15 | JDK 16+ |
|---|---|---|---|
| `ManagementUtils.getCurrentProcessId()` | `VirtualMachineProcessIdResolver` (reflection on `sun.management`, no module system) | `ModernProcessIdResolver` (`ProcessHandle`) | `ModernProcessIdResolver` |
| `VirtualMachineProcessIdResolver` used directly | works | works, with warning logs | needs `--add-opens java.management/sun.management=ALL-UNNAMED` |
| `CustomizedThreadFactory`, `ExecutorUtils`, delegating types | no difference | no difference | no difference |
| `JmxUtils` MXBean accessors | no difference | no difference | no difference; note `HotSpotOperatingSystemMXBean` is still `com.sun.management.*` and not covered here |
| `management.builder` types | no difference | no difference | no difference |
| `SecurityUtils` | works | works | property only; `SecurityManager` deprecated for removal |

> [!TIP]
> `AccessibleObjectUtils` (used by the reflective resolvers) logs the exact JVM flag it needs when a
> `setAccessible(...)` call fails: `"It's require to add JVM Options '--add-opens=<module>/<package>=ALL-UNNAMED'"`,
> referencing JEP 396. Read that line before guessing which module to open — see
> [Class Loading and Artifacts](classloading-and-artifacts.md) for the classpath-side equivalents
> (`java.base/java.net` and `java.base/jdk.internal.loader`).

---

## See also

* [Class Loading and Artifacts](classloading-and-artifacts.md) — the other JDK-internal-sensitive page
* [Core Utilities](core-utilities.md) — `ShutdownHookUtils`, `ClassLoaderUtils`, `ServiceLoaderUtils`
* [Event Dispatching](events.md) — `EventDispatcher.parallel(Executor)`, the usual consumer of these thread factories
* [Reference](reference.md) — `process.execution.timeout` and the other tunables

[← Handbook index](../README.md) · [Previous: Logging](logging.md) · [Next: Class Loading and Artifacts →](classloading-and-artifacts.md)
