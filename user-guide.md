# Microsphere Java- User Guide

## 🔭 Brief Overview

`microsphere-java` is a **Java utility library** — a reusable toolkit that makes everyday Java programming tasks easier. Think of it like a Swiss Army knife: it doesn't do one big thing, it gives you many small, well-crafted tools. It contains **~340 Java source files** organized into focused modules and packages.

---

## 📦 Module Breakdown

The project is split into several Maven modules (sub-projects):

| Module | What it does |
|---|---|
| `microsphere-java-annotations` | Custom Java annotations (labels you attach to code) |
| `microsphere-java-core` | The main utility library |
| `microsphere-java-test` | Test utilities |
| `microsphere-annotation-processor` | Compile-time annotation processing |
| `microsphere-jdk-tools` | JDK compiler tools |
| `microsphere-lang-model` | Language model utilities |

---

## 🧩 Step-by-Step Breakdown of Each Package

---

### 1. `io.microsphere.annotation` — Custom Annotations

**What it is:** Java annotations are like sticky notes you attach to your code. These custom ones communicate intent to developers.

Key annotations:
- `@Since("1.0.0")` — marks when a class/method was introduced (like a changelog label)
- `@Experimental` — warns "this API might change"
- `@Immutable` — signals an object won't change after creation
- `@Nullable` / `@Nonnull` — tells readers (and tools) whether a value can be `null`

```java
// Example: Mark a method as introduced in version 1.0
@Since("1.0.0")
public void myMethod() { ... }

// Warn a caller that null might come back
@Nullable
public String findUser(int id) { ... }
```

---

### 2. `io.microsphere.event` — Event Publishing System

**What it is:** An implementation of the **Observer pattern** — when something happens (an event), anyone who cares (a listener) gets notified automatically.

Key classes:
- `Event` — the base class for every event; records the timestamp automatically
- `EventListener<E>` — an interface you implement to react to events; supports **priority** (lower number = higher priority)
- `EventDispatcher` — the hub that registers listeners and fires events; two flavors:
  - `DirectEventDispatcher` — notifies listeners one-by-one in the same thread
  - `ParallelEventDispatcher` — notifies listeners using a thread pool (in parallel)
- `AbstractEventDispatcher` — the shared logic; caches which listener handles which event type

```java
// 1. Define an event
class UserLoggedIn extends Event {
    public UserLoggedIn(Object source) { super(source); }
}

// 2. Define a listener
class WelcomeListener implements EventListener<UserLoggedIn> {
    public void onEvent(UserLoggedIn event) {
        System.out.println("Welcome! Logged in at: " + event.getTimestamp());
    }
}

// 3. Wire them up
EventDispatcher dispatcher = EventDispatcher.newDefault();
dispatcher.addEventListener(new WelcomeListener());
dispatcher.dispatch(new UserLoggedIn("myApp"));  // triggers WelcomeListener
```

---

### 3. `io.microsphere.lang` — Core Language Abstractions

**What it is:** Small but powerful interfaces and classes that improve code design.

- **`Prioritized`** — anything that needs ordering (lower integer = first). Used by event listeners, converters, etc.
- **`Wrapper`** — lets one object "wrap" another and expose `unwrap(MyType.class)` to retrieve the inner object. Very common in proxy/decorator patterns (like JDBC `Connection` wrappers).
- **`DelegatingWrapper`** — a default wrapper that delegates all calls to the wrapped object.
- **`MutableInteger`** — a mutable `int` holder (handy in lambdas where variables must be "effectively final").

```java
// Prioritized: higher-priority tasks run first
class HighPriorityTask implements Prioritized {
    public int getPriority() { return Prioritized.MAX_PRIORITY; } // runs first
}
```

---

### 4. `io.microsphere.lang.function` — Throwable Functional Interfaces

**What it is:** Java's standard `Function`, `Consumer`, `Supplier` etc. cannot declare checked exceptions. These classes solve that limitation.

- `ThrowableFunction<T,R>` — like `Function<T,R>` but `apply()` can throw any exception
- `ThrowableConsumer<T>` — like `Consumer<T>` but can throw
- `ThrowableSupplier<T>` — like `Supplier<T>` but can throw
- `ThrowableAction` — like `Runnable` but can throw
- `Streams` / `Predicates` — stream/filter helpers

```java
// Parse a file path that might throw IOException
ThrowableFunction<String, byte[]> reader = path -> Files.readAllBytes(Paths.get(path));

// Safe execution: wraps checked exception in RuntimeException
byte[] data = reader.execute("/some/file.txt");

// Or with a fallback:
byte[] data = reader.execute("/some/file.txt", (path, ex) -> new byte[0]);
```

---

### 5. `io.microsphere.collection` — Collection Utilities

**What it is:** Utility methods and classes on top of Java's built-in collections.

Key utilities:
- `CollectionUtils` — `isEmpty()`, `isNotEmpty()`, `asIterable()`, etc.
- `MapUtils` / `ListUtils` / `SetUtils` — helpers for Maps, Lists, Sets
- `ArrayStack` — a stack backed by an ArrayList
- `SingletonIterator` / `SingletonDeque` — collections holding exactly one element
- `UnmodifiableDeque` / `ReadOnlyIterator` — unmodifiable wrappers
- `DelegatingIterator` / `DelegatingDeque` — decorators that forward calls to an inner object

```java
List<String> list = null;
if (CollectionUtils.isEmpty(list)) {
    System.out.println("Nothing here"); // safe — no NullPointerException
}
```

---

### 6. `io.microsphere.convert` — Type Conversion

**What it is:** A flexible strategy for converting values from one type to another (e.g., `String` → `Integer`).

- `Converter<S,T>` — functional interface: given a source type `S`, produce a target type `T`
- `Converters` — discovers all registered converters via Java's SPI (Service Provider Interface)
- Multiple converters can coexist; `Prioritized` decides which one wins

```java
// Convert a String to an Integer if a converter is registered
Integer value = Converter.convertIfPossible("42", Integer.class);
```

---

### 7. `io.microsphere.util` — General Utilities

**What it is:** A broad set of helper classes for common operations.

Major utilities:
- `StringUtils` — string checks, manipulations
- `ClassUtils` — class loading, type checking, hierarchy traversal
- `AnnotationUtils` — reading and searching annotations on classes/methods
- `ArrayUtils` — array length, containment, etc.
- `ExceptionUtils` / `ThrowableUtils` — wrap/unwrap exceptions cleanly
- `ServiceLoaderUtils` — load SPI services with error handling
- `StopWatch` — measure elapsed time for profiling
- `Version` / `VersionUtils` — parse and compare `1.2.3`-style version strings
- `ShutdownHookUtils` — register cleanup code that runs on JVM shutdown

```java
// Time how long something takes
StopWatch sw = new StopWatch();
sw.start("my task");
// ... do work ...
sw.stop();
System.out.println(sw.prettyPrint());

// Compare versions
Version v1 = Version.of("1.2.3");
Version v2 = Version.of("1.3.0");
System.out.println(v1.compareTo(v2)); // negative: v1 < v2
```

---

### 8. `io.microsphere.reflect` — Reflection Utilities

**What it is:** Java reflection lets you inspect and call classes/methods/fields at runtime. These utilities wrap the boilerplate and handle edge cases.

Key classes:
- `MethodUtils` / `FieldUtils` / `ConstructorUtils` — find and invoke methods/fields/constructors safely
- `TypeUtils` — generic type resolution (e.g., "what is the `T` in `List<T>`?")
- `ReflectionUtils` — common reflection helpers
- `ProxyUtils` — work with JDK dynamic proxies

```java
// Find a method by name and invoke it
Method m = MethodUtils.findMethod(MyClass.class, "doSomething", String.class);
Object result = MethodUtils.invokeMethod(instance, m, "hello");
```

---

### 9. `io.microsphere.io` — I/O Utilities

**What it is:** Helpers for reading files, scanning classpaths, and filtering resources.

- `io.scanner` — scan classpath entries (JARs, directories) for classes or resources
- `io.filter` — file/resource filters
- `io.event` — file change events (e.g., watch a directory)

---

### 10. `io.microsphere.logging` — Logging Abstraction

**What it is:** A thin abstraction over whatever logging framework is on the classpath (SLF4J, JUL, etc.).

```java
Logger logger = LoggerFactory.getLogger(MyClass.class);
logger.info("Starting up...");
```

---

### 11. `io.microsphere.concurrent` — Concurrency Utilities

**What it is:** Thread-safe helpers and `ThreadLocal` tools for concurrent programs.

---

### 12. `io.microsphere.net` — Network Utilities

**What it is:** URL/URI building utilities, classpath URL handlers, and console URL handlers.

---

### 13. `io.microsphere.json` — JSON Utilities

**What it is:** Lightweight JSON parsing and creation (`JSONObject`, `JSONArray`, `JSONTokener`) — a local, dependency-free JSON library similar to `org.json`.

---

### 14. `io.microsphere.metadata` — Configuration Property Metadata

**What it is:** Reads and generates Spring-Boot-style `additional-spring-configuration-metadata.json` configuration property metadata, allowing IDE auto-completion for configuration keys.

---

## 🔑 Key Concepts / Terminology

| Term | Plain English |
|---|---|
| **Interface** | A contract: defines *what* methods must exist, not *how* |
| **`@FunctionalInterface`** | An interface with exactly one method — can be used as a lambda |
| **Generics (`<T>`)** | Placeholder for a type that gets filled in later — avoids casts |
| **SPI (Service Provider Interface)** | A Java mechanism to load implementations by placing a file in `META-INF/services/` |
| **Observer Pattern** | Publisher fires events; subscribers (listeners) react — they're decoupled |
| **Wrapper/Decorator Pattern** | Wrap an object to add or change behavior without modifying the original |
| **Reflection** | Inspect or invoke classes/methods at runtime, even if you don't know them at compile time |

---

## 🚀 Common Use Cases

- **Add custom annotations** to your API (use `microsphere-java-annotations`) to document stability, nullability, and version history.
- **Publish/subscribe to events** within a JVM (use `EventDispatcher`) without a message broker.
- **Safely call code that throws** checked exceptions inside streams or lambdas (use `ThrowableFunction`).
- **Convert types** in a pluggable, prioritized way (use `Converter`).
- **Traverse class hierarchies / read generics at runtime** (use `TypeUtils`, `ClassUtils`).
- **Measure performance** during debugging (use `StopWatch`).
- **Sort tasks/listeners by priority** (implement `Prioritized`).

---

## 🧑‍💻 How It All Fits Together (mini end-to-end example)

```java
// 1. Define a typed event
class OrderPlaced extends Event {
    private final String orderId;
    public OrderPlaced(Object source, String orderId) {
        super(source);
        this.orderId = orderId;
    }
    public String getOrderId() { return orderId; }
}

// 2. Implement a listener with HIGH priority
class AuditListener implements EventListener<OrderPlaced> {
    public void onEvent(OrderPlaced e) {
        System.out.println("AUDIT: order " + e.getOrderId() + " at " + e.getTimestamp());
    }
    public int getPriority() { return Prioritized.MAX_PRIORITY; } // runs first
}

// 3. Wire up and fire
EventDispatcher dispatcher = EventDispatcher.newDefault();
dispatcher.addEventListener(new AuditListener());
dispatcher.dispatch(new OrderPlaced(this, "ORD-001"));
// → AUDIT: order ORD-001 at 1716726766000
```

This library's design keeps individual pieces small, testable, and composable — a great pattern to study when learning professional Java development.

---

## 🛠️ Intermediate Developer Guide

This section assumes solid Java knowledge (generics, lambdas, streams, SPI) and focuses on how to integrate `microsphere-java` components into real applications.

---

### Plugging in a Custom Converter via SPI

The `Converter<S,T>` system is fully open/closed: you add new conversions without touching existing code.

**Step 1 — Implement `Converter`:**

```java
package com.example.convert;

import io.microsphere.convert.Converter;
import io.microsphere.lang.Prioritized;

// Generic type parameters are read at runtime via reflection —
// you must provide concrete types, not raw/wildcard types.
public class UUIDToStringConverter implements Converter<UUID, String> {

    @Override
    public String convert(UUID source) {
        return source == null ? null : source.toString();
    }

    // Override to outrank the built-in ObjectToStringConverter
    @Override
    public int getPriority() {
        return Prioritized.MAX_PRIORITY + 1; // very high priority
    }
}
```

**Step 2 — Register via SPI:**

Create `src/main/resources/META-INF/services/io.microsphere.convert.Converter` and add one class name per line:

```
com.example.convert.UUIDToStringConverter
```

**Step 3 — Use it:**

```java
// Lookup by type pair (cached after first call)
Converter<UUID, String> c = Converter.getConverter(UUID.class, String.class);
String s = c.convert(UUID.randomUUID());

// Or the one-liner (returns null if no converter found)
String s2 = Converter.convertIfPossible(UUID.randomUUID(), String.class);
```

**Priority resolution:** multiple converters for the same `<S,T>` pair are sorted by `getPriority()`. Lower integer value wins (executes first). `Prioritized.MAX_PRIORITY` is `Integer.MIN_VALUE` (highest), `Prioritized.NORMAL_PRIORITY` is `0`, `Prioritized.MIN_PRIORITY` is `Integer.MAX_VALUE`.

---

### Writing a ConditionalEventListener

Sometimes a listener only cares about a subset of events of the same type. Implement `ConditionalEventListener` to filter at the dispatcher level — `onEvent` is never called for rejected events.

```java
import io.microsphere.event.ConditionalEventListener;
import io.microsphere.event.Event;
import io.microsphere.event.EventListener;

public class CriticalOrderListener
        implements EventListener<OrderPlaced>, ConditionalEventListener {

    // Called before onEvent; return false to skip this listener
    @Override
    public boolean accept(Event event) {
        OrderPlaced order = (OrderPlaced) event;
        return order.getAmount().compareTo(BigDecimal.valueOf(10_000)) > 0;
    }

    @Override
    public void onEvent(OrderPlaced event) {
        // Only called for orders > 10,000
        alertRiskTeam(event);
    }
}
```

Auto-load listeners via SPI by creating `META-INF/services/io.microsphere.event.EventListener`:

```
com.example.CriticalOrderListener
```

Listeners registered via SPI are loaded automatically by `AbstractEventDispatcher` during construction.

---

### ThrowableFunction Composition

`ThrowableFunction<T,R>` supports `andThen` / `compose` just like `java.util.function.Function`, but both sides may throw:

```java
ThrowableFunction<String, Path>   toPath   = Paths::get;
ThrowableFunction<Path,  byte[]>  readFile = Files::readAllBytes;
ThrowableFunction<byte[], String> decode   = b -> new String(b, StandardCharsets.UTF_8);

// Build a pipeline: String -> Path -> byte[] -> String
ThrowableFunction<String, String> readText = toPath.andThen(readFile).andThen(decode);

// Execute with a fallback instead of a RuntimeException
String content = readText.execute("/etc/hosts", (path, ex) -> {
    log.warn("Cannot read {}: {}", path, ex.getMessage());
    return "";
});
```

Use `ThrowableAction` for void operations that can throw, and `ThrowableSupplier<T>` when there is no input:

```java
ThrowableAction   action   = connection::close;   // Closeable in a lambda
ThrowableSupplier<Config> loader = () -> Config.load("/app.yml");

ThrowableAction.execute(action, ex -> log.error("Close failed", ex));
Config cfg = loader.execute(ex -> Config.defaults());
```

---

### Wrapper and DelegatingWrapper

Use `Wrapper` when you build decorator/proxy chains and callers need to "look through" the chain to get the real underlying object (classic JDBC pattern):

```java
// Implement DelegatingWrapper — only getDelegate() is required
public class MetricsDataSource implements DataSource, DelegatingWrapper {
    private final DataSource delegate;

    public MetricsDataSource(DataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object getDelegate() { return delegate; }

    @Override
    public Connection getConnection() throws SQLException {
        long start = System.nanoTime();
        try {
            return delegate.getConnection();
        } finally {
            metrics.record(System.nanoTime() - start);
        }
    }
    // ... rest of DataSource ...
}
```

A caller that holds a `MetricsDataSource` can transparently reach the real `DataSource`:

```java
DataSource real = Wrapper.tryUnwrap(metricsDs, HikariDataSource.class);
// real != null if the delegate is a HikariDataSource (or itself implements Wrapper)
```

---

### Resolving Generic Type Arguments at Runtime

`TypeUtils` solves a common reflection problem: "given an instance of `MyRepo extends JpaRepository<User, Long>`, what are the entity and ID types?"

```java
// 1. Resolve a single type argument by index
Class<?> entityType = TypeUtils.resolveActualTypeArgumentClass(
        MyRepo.class,        // the concrete class
        JpaRepository.class, // the generic base
        0);                  // argument index (0 = first)
// entityType == User.class

// 2. Resolve all type arguments
List<Class> args = TypeUtils.resolveActualTypeArgumentClasses(
        MyRepo.class, JpaRepository.class);
// [User.class, Long.class]

// 3. Check at Type level before casting
Type t = TypeUtils.resolveActualTypeArgument(MyRepo.class, JpaRepository.class, 0);
if (TypeUtils.isParameterizedType(t)) { ... }
```

Results are cached in an LRU cache (default size 256, tunable via system property `microsphere.reflect.resolved-generic-types.cache.size`).

---

### Traversing Class Hierarchies

```java
// All interfaces, including inherited ones
List<Class<?>> interfaces = ClassUtils.getAllInterfaces(ArrayList.class);

// All superclasses up to Object
List<Class<?>> supers = ClassUtils.getAllSuperClasses(ArrayList.class);

// Everything together (superclasses + interfaces)
List<Class<?>> hierarchy = ClassUtils.getHierarchy(ArrayList.class);
```

---

### Testing Utilities

`microsphere-java-test` provides pre-built model classes for testing generic type resolution and annotation processing:

```java
// Pre-built type models for parameterized-type tests
StringArrayList list = new StringArrayList(); // extends ArrayList<String>
Class<?> t = TypeUtils.resolveActualTypeArgumentClass(
        StringArrayList.class, List.class, 0);
// t == String.class  ✓

// Base class for annotation-processor unit tests
class MyProcessorTest extends AbstractAnnotationProcessingTest {
    @Test
    public void testOutput() {
        // Compiler is wired automatically; just verify generated files
    }
}
```

---

## ⚙️ Seasoned Developer Guide

This section targets architects and library authors who need to extend or build on top of `microsphere-java`. It covers internal design, thread-safety guarantees, extension points, and system-level tunables.

---

### Architecture and Design Patterns

`microsphere-java` is built around a small set of cross-cutting patterns:

| Pattern | Where used |
|---|---|
| **SPI (Service Provider Interface)** | `Converter`, `EventListener`, `URLStreamHandlerFactory`, all service lookups |
| **Template Method** | `AbstractEventDispatcher` — subclasses provide only the `Executor` |
| **Strategy** | `Converter<S,T>`, `EventDispatcher` implementations, `URLStreamHandlerFactory` |
| **Wrapper / Decorator** | `Wrapper`, `DelegatingWrapper`, `DelegatingIterator`, `UnmodifiableDeque` |
| **Composite** | `CompositeURLStreamHandlerFactory` — chains multiple factories in priority order |
| **Visitor** | `ConfigurationPropertyJSONElementVisitor` — walks the annotation-processor AST |
| **Factory Method** | `EventDispatcher.newDefault()`, `EventDispatcher.parallel(Executor)` |
| **Prioritized Ordering** | `Prioritized` + `Prioritized.COMPARATOR` — used everywhere SPI lists are sorted |

---

### `AbstractEventDispatcher` Internals

**Listener storage:**

```
ConcurrentMap<Class<? extends Event>, List<EventListener>>  listenerCache
```

The key is the *event type*, not the listener type. The map is populated lazily: when the first event of a given type is dispatched, the dispatcher scans all registered listeners for those whose generic type argument `E` is assignment-compatible with the event type, then caches that filtered, priority-sorted list.

**Mutation path (add/remove listener):**

```
synchronized(mutex) {
    modify listeners list
    sort(listeners)      // O(n log n) on every mutation, not on every dispatch
    invalidate listenerCache
}
```

Because sorting happens at mutation time, dispatch itself is a read-only path with no locking beyond the `ConcurrentMap` read.

**Dispatch path:**

```
executor.execute(() -> {
    for (EventListener listener : sortedListeners(event.getClass())) {
        if (listener instanceof ConditionalEventListener) {
            if (!((ConditionalEventListener) listener).accept(event)) continue;
        }
        listener.onEvent(event);
    }
})
```

**Extending `AbstractEventDispatcher`:**

```java
public class MdcEventDispatcher extends AbstractEventDispatcher {
    private final Executor executor;

    public MdcEventDispatcher(Executor executor) {
        this.executor = executor;
    }

    @Override
    protected Executor getExecutor() {
        // Wrap the executor to propagate MDC context
        return command -> executor.execute(() -> {
            Map<String, String> ctx = MDC.getCopyOfContextMap();
            try { command.run(); }
            finally { if (ctx != null) MDC.setContextMap(ctx); else MDC.clear(); }
        });
    }
}
```

---

### `Converters` Cache Architecture

`Converters` maintains a two-level lookup structure:

```
static ConcurrentMap<Entry<Class<?>, Class<?>>, List<Converter>>  converterCache
```

1. **Bootstrap**: at first access, `ServiceLoaderUtils.loadServicesList(Converter.class)` loads and sorts *all* registered converters.
2. **Lookup for `<S,T>`**: filters the master list to those where `converter.accept(S, T)` returns true. The default `accept` implementation compares `getSourceType()` / `getTargetType()` (both resolved via `TypeUtils.resolveActualTypeArgumentClass`).
3. **Result**: the first entry (highest priority) is returned and the filtered list is cached under the `<S,T>` key.

Custom converters added after bootstrap will not appear in an already-cached `<S,T>` entry. If dynamic registration is required, call `Converters.invalidate()` (if available) or restart the `Converters` singleton state.

---

### URL Handler Extension Point

`microsphere-java` exposes a `URLStreamHandler` extension mechanism that supports nested / sub-protocol URLs like:

```
classpath:com/example/config.yml
jar:classpath:lib/my.jar!/META-INF/MANIFEST.MF
```

**Extension SPI:**

Implement `URLStreamHandlerFactory` and register it via:

```
META-INF/services/java.net.URLStreamHandlerFactory
→ com.example.net.MyProtocolHandlerFactory
```

`ServiceLoaderURLStreamHandlerFactory.attach()` installs a global factory that:
1. Loads all `URLStreamHandlerFactory` SPI implementations (priority-ordered).
2. Falls back to `ExtendableProtocolURLStreamHandler` for sub-protocol chaining.

**Sub-protocol handler:**

```java
public class ClasspathURLStreamHandler extends ExtendableProtocolURLStreamHandler {
    @Override
    protected URLConnection openSubProtocolConnection(URL u, URLStreamHandler subHandler)
            throws IOException {
        // resolve 'classpath:' to a jar URL and open it
    }
}
```

Register under `META-INF/services/io.microsphere.net.ExtendableProtocolURLStreamHandler`.

---

### Compile-Time Annotation Processing

`microsphere-annotation-processor` generates Spring-Boot-compatible configuration metadata JSON at compile time.

**Attach the processor** (Maven):

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-annotation-processor</artifactId>
    <scope>provided</scope>
</dependency>
```

**Annotate configuration holders:**

```java
@ConfigurationProperty(
    name        = "app.cache.ttl",
    defaultValue = "300",
    description  = "Cache TTL in seconds",
    source       = ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE
)
public class CacheConfig { ... }
```

At compile time, `ConfigurationPropertyAnnotationProcessor` walks the annotation tree via `ConfigurationPropertyJSONElementVisitor` and writes:

```
META-INF/configuration-property-metadata.json
```

This file is picked up by Spring Boot's IDE support for auto-completion of `application.properties` / `application.yml` keys.

**Writing a custom annotation processor** that uses the test harness:

```java
public class MyProcessorTest extends AbstractAnnotationProcessingTest {

    @Override
    protected Processor processor() {
        return new MyAnnotationProcessor();
    }

    @Test
    public void testGeneratedOutput() throws Exception {
        // compile test sources, processor runs automatically
        // assert generated files exist and have expected content
    }
}
```

---

### Thread-Safety Model Summary

| Component | Thread-safety guarantee |
|---|---|
| `AbstractEventDispatcher` — dispatch | Lock-free read from `ConcurrentMap`; safe for concurrent dispatch |
| `AbstractEventDispatcher` — add/remove | Synchronized on internal mutex; blocks during mutation |
| `Converters` cache | `ConcurrentMap`; lazy population is idempotent (same result if races occur) |
| `ServiceLoaderUtils` | Optional LRU cache guarded internally; disable with `microsphere.service-loader.cached=false` |
| `TypeUtils` generic cache | LRU with configurable size; safe for concurrent reads |
| `ParallelEventDispatcher` | Listener callbacks run concurrently — listeners **must** be thread-safe |

**`ParallelEventDispatcher` — executor lifecycle:**

```java
// Use a managed pool; register shutdown on JVM exit
ExecutorService pool = Executors.newFixedThreadPool(4,
        new CustomizedThreadFactory("event-dispatch"));
ExecutorUtils.shutdownOnExit(pool);     // registers ShutdownHook

EventDispatcher dispatcher = EventDispatcher.parallel(pool);
```

---

### System-Level Tunables

These system properties control caches and loader behaviour:

| Property | Default | Effect |
|---|---|---|
| `microsphere.service-loader.cached` | `true` | Set to `false` to disable ServiceLoader result caching |
| `microsphere.reflect.resolved-generic-types.cache.size` | `256` | LRU cache size for `TypeUtils` generic resolution |

Set via JVM argument: `-Dmicrosphere.service-loader.cached=false`

---

### Extending `Prioritized` in Your Own APIs

`Prioritized` is a general-purpose ordering contract. Adopt it in your own SPIs to get automatic priority-based sorting for free:

```java
// Your SPI
public interface DataValidator extends Prioritized {
    ValidationResult validate(Object data);
}

// Registration and lookup
List<DataValidator> validators = ServiceLoaderUtils.loadServicesList(DataValidator.class);
// validators are already sorted: lowest getPriority() value first

// Run in order
for (DataValidator v : validators) {
    ValidationResult r = v.validate(data);
    if (!r.isValid()) break;  // short-circuit on first failure
}
```

Built-in `Prioritized.COMPARATOR` works on any `Object` (non-`Prioritized` objects are treated as equal to each other and placed after all `Prioritized` objects), so it can be used as a general-purpose `Comparator` for heterogeneous lists.
