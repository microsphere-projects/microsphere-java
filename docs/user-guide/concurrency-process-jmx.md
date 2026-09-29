# Concurrency, Processes and JMX

**Module**: `io.github.microsphere-projects:microsphere-java-core`
**Packages**: `io.microsphere.concurrent`, `io.microsphere.process`, `io.microsphere.management`,
`io.microsphere.management.builder`, `io.microsphere.security`

---

## 1. `io.microsphere.concurrent`

Four types only — there is no `ThreadLocalUtils`.

### `CustomizedThreadFactory`

```java
public class CustomizedThreadFactory implements ThreadFactory {

    public static final boolean DEFAULT_DAEMON   = true;
    public static final int     DEFAULT_PRIORITY = Thread.NORM_PRIORITY;
    public static final long    DEFAULT_STACK_SIZE = 0;

    protected CustomizedThreadFactory(String namePrefix, boolean daemon, int priority, long stackSize)

    public static ThreadFactory newThreadFactory(String namePrefix)
    public static ThreadFactory newThreadFactory(String namePrefix, boolean daemon)
    public static ThreadFactory newThreadFactory(String namePrefix, boolean daemon, int priority)
    public static ThreadFactory newThreadFactory(String namePrefix, boolean daemon, int priority, long stackSize)

    @Override public Thread newThread(Runnable runnable)
}
```

Threads are named `<namePrefix>-thread-<n>`, which is the whole point: thread dumps and MBeans become readable.
The constructor is `protected` — always go through `newThreadFactory(...)`. Default daemon-ness is `true`, so a pool
built on this factory will not keep the JVM alive.

```java
ExecutorService pool = Executors.newFixedThreadPool(4,
        CustomizedThreadFactory.newThreadFactory("order-dispatch"));
EventDispatcher dispatcher = EventDispatcher.parallel(pool);
```

### `ExecutorUtils`

```java
public abstract class ExecutorUtils implements Utils {
    public static void shutdownOnExit(Executor one, Executor... others)
    public static boolean shutdown(Executor executor)
    public static boolean shutdown(ExecutorService executorService)
}
```

`shutdownOnExit` registers a [`ShutdownHookUtils`](core-utilities.md#shutdownhookutils) callback that calls
`shutdown()` on each executor when the JVM exits — it is how you avoid leaking pools in short-lived processes and
test runners.

Return values: `shutdown(Executor)` returns `false` when the argument is **not** an `ExecutorService` (a plain
`Runnable::run` executor, for instance) and otherwise delegates; `shutdown(ExecutorService)` returns `false` only for
`null`, and `true` after performing an orderly `shutdown()` (it is idempotent — an already-shutdown service still
returns `true` and is not shut down twice).

### Delegating executors and queues

| Type | Declaration |
|---|---|
| `DelegatingScheduledExecutorService` | `implements ScheduledExecutorService`; `(ScheduledExecutorService delegate)`; `setDelegate(...)`, `getDelegate()`; full delegation of `schedule`/`scheduleAtFixedRate`/`scheduleWithFixedDelay`/`submit`/`invokeAll`/`invokeAny`/`shutdown`/`awaitTermination`/`execute` |
| `DelegatingBlockingQueue<E>` | `implements BlockingQueue<E>, DelegatingWrapper`; `(BlockingQueue<E> delegate)`; `Object getDelegate()`; full delegation |

These are the base classes for metrics/tracing wrappers: extend, override the one method you care about, and let
callers recover the real object through [`Wrapper.tryUnwrap`](language-abstractions.md#2-wrapper-and-delegatingwrapper).

```java
public class TimedScheduledExecutor extends DelegatingScheduledExecutorService {

    public TimedScheduledExecutor(ScheduledExecutorService delegate) {
        super(delegate);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(() -> {
            long start = System.nanoTime();
            try { command.run(); } finally { metrics.record(System.nanoTime() - start); }
        });
    }
}
```

---

## 2. `io.microsphere.process`

### `ProcessExecutor`

```java
public class ProcessExecutor {

    public static final String PROCESS_EXECUTION_TIMEOUT_PROPERTY_NAME = "process.execution.timeout";
    public static final long DEFAULT_TIMEOUT = 30_000;    // ms, from the property

    public ProcessExecutor(String command, String... options)

    public void execute(OutputStream outputStream) throws IOException, TimeoutException
    public void execute(OutputStream outputStream, long timeoutInMilliseconds) throws IOException, TimeoutException
    public void execute(OutputStream outputStream, long timeout, TimeUnit timeUnit) throws IOException, TimeoutException
}
```

```java
ProcessExecutor executor = new ProcessExecutor("git", "rev-parse", "HEAD");
FastByteArrayOutputStream out = new FastByteArrayOutputStream();
executor.execute(out, 10, TimeUnit.SECONDS);
String revision = out.toString("UTF-8").trim();
```

Behaviour worth knowing:

* The command string is split on whitespace (`commandLine.split("\\s+")`), so **arguments containing spaces cannot be
  passed** through `new ProcessExecutor("cmd a b")` — build the command from tokens.
* `redirectErrorStream(true)`: stderr is merged into the output you provide.
* Execution runs on a daemon single-thread executor named `process-exec-thread-<n>`, and the timeout is enforced by
  waiting on that executor — hence `TimeoutException` rather than a killed process.
* `process.execution.timeout` (no `microsphere.` prefix, read with `Long.getLong`) changes the default for every
  `execute(OutputStream)` call.

### `ProcessIdResolver` SPI

```java
public interface ProcessIdResolver extends Prioritized {
    long UNKNOWN_PROCESS_ID = -1L;
    boolean supports();
    Long current();
}
```

Registered in `META-INF/services/io.microsphere.process.ProcessIdResolver` and resolved by priority:

| Order | Implementation | How it finds the pid | Priority |
|---|---|---|---|
| 1 | `ModernProcessIdResolver` | `java.lang.management` `ProcessHandle.current().pid()` (Java 9+) | `NORMAL_PRIORITY + 1` |
| 2 | `VirtualMachineProcessIdResolver` | reflective hidden `jvm` field of `RuntimeMXBean` | `NORMAL_PRIORITY + 5` |
| 3 | `ClassicProcessIdResolver` | parses `RuntimeMXBean.getName()` (`pid@host`) | `NORMAL_PRIORITY + 9` |

This is what `ManagementUtils.getCurrentProcessId()` is built on — see [§3](#3-io-microsphere-management).

### `ProcessManager`

```java
public class ProcessManager {
    public static final ProcessManager INSTANCE;
    protected ProcessManager addUnfinishedProcess(Process process, String arguments)
    protected ProcessManager removeUnfinishedProcess(Process process, String arguments)
    public Map<Process, String> unfinishedProcessesMap()
}
```

`add`/`remove` are `protected` (same-package use by `ProcessExecutor`), so an application's only real entry point is
`ProcessManager.INSTANCE.unfinishedProcessesMap()` — a registry of child processes that have not finished, useful for
diagnosing leaked processes at shutdown.

---

## 3. `io.microsphere.management`

### `ManagementUtils`

```java
public abstract class ManagementUtils implements Utils {
    public static long getCurrentProcessId()      // -1 when unresolvable
}
```

Resolved once through the `ProcessIdResolver` chain; returns `ProcessIdResolver.UNKNOWN_PROCESS_ID` (`-1`) rather
than throwing when nothing works.

### `JmxUtils`

```java
public abstract class JmxUtils implements Utils {

    public static ClassLoadingMXBean getClassLoadingMXBean()
    public static MemoryMXBean getMemoryMXBean()
    public static ThreadMXBean getThreadMXBean()
    public static RuntimeMXBean getRuntimeMXBean()
    public static Optional<CompilationMXBean> getCompilationMXBean()   // may be absent
    public static OperatingSystemMXBean getOperatingSystemMXBean()
    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans()
    public static List<MemoryManagerMXBean> getMemoryManagerMXBeans()
    public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans()

    public static MBeanInfo getMBeanInfo(MBeanServer server, ObjectName objectName)
    public static MBeanAttribute[] getMBeanAttributes(MBeanServer server, ObjectName objectName)
    public static Map<String, MBeanAttribute> getMBeanAttributesMap(MBeanServer server, ObjectName objectName)
    public static Object getAttribute(MBeanServer server, ObjectName objectName, String attributeName)
    public static Object getAttribute(MBeanServer server, ObjectName objectName, MBeanAttributeInfo attributeInfo)
    public static MBeanAttributeInfo findMBeanAttributeInfo(MBeanServer server, ObjectName objectName, String attributeName)

    public static MBeanParameterInfo[] methodSignature(Method method)
    public static MBeanParameterInfo[] signature(Parameter[] parameters)
    public static Descriptor descriptorForElement(AnnotatedElement annotatedElement)
    public static Descriptor descriptorForAnnotations(Annotation[] annotations)
}
```

Note `getCompilationMXBean()` returns `Optional` (some JVMs ship without a compiler MBean) while the other beans are
returned directly. `MBeanAttribute` bundles info + value:

```java
public class MBeanAttribute {
    public MBeanAttribute(MBeanInfo declaringMBeanInfo, MBeanAttributeInfo attributeInfo, Object value)
    @Nonnull public MBeanInfo getDeclaringMBeanInfo()
    public MBeanAttributeInfo getAttributeInfo()
    public String getName()
    public String getType()
    public boolean isReadable() / isWritable() / isIs()
    public Object getValue()
}
```

```java
MBeanServer server = ManagementFactory.getPlatformMBeanServer();
ObjectName name = new ObjectName("java.lang:type=Memory");

Map<String, MBeanAttribute> attributes = JmxUtils.getMBeanAttributesMap(server, name);
MBeanAttribute heap = attributes.get("HeapMemoryUsage");
System.out.printf("%s = %s%n", heap.getName(), heap.getValue());
```

### `management.builder` — MBean metadata DSL

Fluent builders with static entry points, intended for programmatic `DynamicMBean` definitions.

| Builder | Entry points | Extra setters | Produces |
|---|---|---|---|
| `MBeanInfoBuilder` | `mbeanInfo(String className)`, `mbeanInfo(BeanInfo beanInfo)` | `attribute(String, Class, Consumer<MBeanAttributeInfoBuilder>)`, `attribute(PropertyDescriptor)`, `operation(String, Class, Consumer<...>)`, `operation(Method)`, `operation(MethodDescriptor)`, `constructor(Constructor)`, `constructor(Consumer<...>)`, `notification(Class...)`, `notification(Consumer<...>)`, `description(String)`, `descriptor(Descriptor)` | `MBeanInfo` |
| `MBeanAttributeInfoBuilder` | `attribute(Class<?> type)`, `attribute(String type)` | `name(String)`, `read(boolean)`, `write(boolean)`, `is(boolean)`, `description(String)`, `descriptor(Descriptor)` | `MBeanAttributeInfo` |
| `MBeanOperationInfoBuilder` | `operation(Class<?> returnType)`, `operation(String returnType)`, `operation(Method method)` | `name(String)`, `signature(MBeanParameterInfo...)`, `param(Class, Consumer<...>)`, `from(Executable)`, `impact(Impact)`, `description(String)` | `MBeanOperationInfo` |
| `MBeanConstructorInfoBuilder` | `constructor()`, `constructor(Constructor<?>)` | `name(String)`, `signature(...)`, `param(...)`, `from(Executable)` | `MBeanConstructorInfo` |
| `MBeanParameterInfoBuilder` | `parameter(Class<?>)`, `parameter(String)`, `parameter(Parameter)` | `name(String)`, `description(String)` | `MBeanParameterInfo` |
| `MBeanNotificationInfoBuilder` | `notification()`, `notification(Class...)`, `notification(String...)` | `name(String)`, `types(Class...)`, `types(String...)`, `description(String)` | `MBeanNotificationInfo` |

Shared abstract bases: `MBeanDescribableBuilder<B>` (`description`, `descriptor`, abstract `build()`),
`MBeanFeatureInfoBuilder<B>` (`name`, `build()`), `MBeanExecutableInfoBuilder<B>` (`signature`, `param`,
`from(Executable)`, `toSignature()`).

```java
import static io.microsphere.management.builder.MBeanInfoBuilder.mbeanInfo;
import static io.microsphere.management.builder.MBeanOperationInfoBuilder.operation;

MBeanInfo info = mbeanInfo("com.example.CacheAdmin")
        .description("Cache management")
        .operation("clear", void.class, op -> op.description("Drop all entries"))
        .attribute("size", int.class, a -> a.read(true).write(false).description("Entry count"))
        .build();
```

`operation(...)`/`attribute(...)` take a `Consumer` of the sub-builder, and the sub-builders' `param(...)` /
`from(Executable)` overloads derive signatures from real `Method`/`Constructor` objects — so a `DynamicMBean` can be
generated from an interface instead of hand-written.

---

## 4. `io.microsphere.security`

```java
public abstract class SecurityUtils implements Utils {

    public static final String JAVA_SECURITY_POLICY_FILE_PROPERTY_NAME = "java.security.policy";

    public static void setJavaSecurityPolicyFile(String javaSecurityPolicyFilePath)
    public static void setJavaSecurityPolicyFile(File javaSecurityPolicyFile)
    @Nullable public static String getJavaSecurityPolicyFile()
}
```

It is a thin, annotated wrapper around the `java.security.policy` system property (`@ConfigurationProperty` with
`source = SYSTEM_PROPERTIES_SOURCE`, so it appears in the generated metadata). Setting the property only *locates* a
policy file; installing a `SecurityManager` is still your responsibility, and on JDK 17+ the security manager itself
is deprecated for removal — treat this as legacy-support surface, not a recommended integration point.

---

## 5. See also

* [Core Utilities](core-utilities.md#shutdownhookutils) — the shutdown-hook registry `ExecutorUtils` uses
* [Event Dispatching](events.md#4-eventdispatcher) — where you hand an `Executor` to `EventDispatcher.parallel(...)`
* [Reference](reference.md) — `process.execution.timeout` and the other tunables

[← Previous: Logging](logging.md) · [Index](README.md) · [Next: Class Loading and Artifacts →](classloading-and-artifacts.md)
