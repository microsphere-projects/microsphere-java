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
