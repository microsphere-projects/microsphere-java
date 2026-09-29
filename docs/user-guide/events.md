# Event Dispatching

**Module**: `io.github.microsphere-projects:microsphere-java-core`
**Package**: `io.microsphere.event`

A dependency-free observer implementation: in-JVM only, priority-ordered, optionally parallel, with SPI
auto-registration of listeners.

---

## 1. The types

```
Event (abstract, extends java.util.EventObject)
 ├── GenericEvent
 └── FileChangedEvent                      -> io-and-file-watch.md

EventListener<E extends Event>  (interface, extends java.util.EventListener + Prioritized)
 ├── ConditionalEventListener<E>           accept() gate before onEvent()
 └── GenericEventListener                  one listener, many handler methods

Listenable<E extends EventListener<?>>     add / remove / query listeners
 └── EventDispatcher extends Listenable<EventListener<?>>
      ├── AbstractEventDispatcher          shared storage + sorting + SPI loading
      │    ├── DirectEventDispatcher       same-thread
      │    └── ParallelEventDispatcher     Executor-backed
```

---

## 2. `Event`

```java
public abstract class Event extends java.util.EventObject {
    public Event(Object source)
    public long getTimestamp()      // System.currentTimeMillis() at construction
}
```

`source` is required (`EventObject` contract) and is your usual "who fired it" reference. The timestamp is captured
in the constructor, so subclasses need not (and should not) re-record it.

```java
public class OrderPlaced extends Event {

    private final String orderId;
    private final BigDecimal amount;

    public OrderPlaced(Object source, String orderId, BigDecimal amount) {
        super(source);
        this.orderId = orderId;
        this.amount = amount;
    }

    public String getOrderId()  { return orderId; }
    public BigDecimal getAmount() { return amount; }
}
```

`GenericEvent` exists when you want a ready-made event carrying an arbitrary payload instead of declaring a subclass.

---

## 3. `EventListener<E>`

```java
@FunctionalInterface
public interface EventListener<E extends Event> extends java.util.EventListener, Prioritized {

    void onEvent(E event);

    default int getPriority()      // returns NORMAL_PRIORITY (0)

    static Class<? extends Event> findEventType(EventListener<?> listener)
    static Class<? extends Event> findEventType(Class<?> listenerClass)
    static Class<? extends Event> findEventType(ParameterizedType parameterizedType)
}
```

The generic parameter `E` **is** the subscription. The dispatcher reads it at runtime via
`findEventType(...)`, so `E` must be a concrete type in the `implements` clause — the same constraint as
[Converter's `S`/`T`](type-conversion.md#3-writing-a-custom-converter). A raw `implements EventListener` will never
receive events.

> [!WARNING]
> The Javadoc on `EventListener.getPriority()` claims the default is `Integer#MAX_VALUE`. It is not: the code
> returns `Prioritized.NORMAL_PRIORITY` (`0`).

Listeners are **priority-sorted ascending** — the lowest integer runs first:

```java
public class AuditListener implements EventListener<OrderPlaced> {

    @Override
    public void onEvent(OrderPlaced event) {
        audit.record(event.getOrderId(), event.getTimestamp());
    }

    @Override
    public int getPriority() {
        return Prioritized.MAX_PRIORITY;    // Integer.MIN_VALUE -> runs before everyone
    }
}
```

A lambda listener is legal but has no way to express priority or event type unless the target type is explicit:

```java
dispatcher.addEventListener((EventListener<OrderPlaced>) event -> log.info("order {}", event.getOrderId()));
```

---

## 4. `EventDispatcher`

```java
public interface EventDispatcher extends Listenable<EventListener<?>> {

    Executor DIRECT_EXECUTOR = command -> command.run();   // Runnable::run

    void dispatch(Event event);

    default Executor getExecutor() { return DIRECT_EXECUTOR; }

    @Nonnull static EventDispatcher newDefault()
    @Nonnull static EventDispatcher parallel(@Nonnull Executor executor)
    @Nonnull static EventDispatcher of(@Nullable Executor executor)   // null -> newDefault()
}
```

> [!NOTE]
> The factory names are `newDefault()`, `parallel(Executor)` and `of(Executor)`. There is **no**
> `sequential(...)` / `create()` / `builder()`.

```java
EventDispatcher dispatcher = EventDispatcher.newDefault();          // DirectEventDispatcher
dispatcher.addEventListener(new AuditListener());
dispatcher.dispatch(new OrderPlaced(this, "ORD-001", amount));
```

For parallel dispatch you own the executor's lifecycle:

```java
ExecutorService pool = Executors.newFixedThreadPool(4,
        CustomizedThreadFactory.newThreadFactory("event-dispatch"));

ExecutorUtils.shutdownOnExit(pool);              // registers a shutdown-hook callback

EventDispatcher dispatcher = EventDispatcher.parallel(pool);
```

See [Concurrency, Processes and JMX](concurrency-process-jmx.md) for `CustomizedThreadFactory` and `ExecutorUtils`.

`ParallelEventDispatcher` also has a no-arg constructor that uses `ForkJoinPool.commonPool()` — convenient, but it
shares the common pool with parallel streams, so prefer your own executor in production.

---

## 5. `Listenable` — registration API

```java
public interface Listenable<E extends EventListener<?>> {

    static void assertListener(EventListener<?> listener)

    void addEventListener(E listener) throws NullPointerException, IllegalArgumentException;
    void removeEventListener(E listener) throws NullPointerException, IllegalArgumentException;
    @Nonnull List<E> getAllEventListeners();

    default void addEventListeners(E listener, E... others)
    default void addEventListeners(Iterable<E> listeners)
    default void removeEventListeners(Iterable<E> listeners)
    default void removeAllEventListeners()
}
```

```java
dispatcher.addEventListeners(auditListener, notifyListener, metricsListener);
dispatcher.getAllEventListeners();     // unmodifiable, priority-sorted
dispatcher.removeAllEventListeners();
```

> [!NOTE]
> `assertListener` currently only null-checks: the "must not be a final class / proxy" rejection documented in the
> Javadoc is commented out in the source. Do not design around it.

---

## 6. Conditional and generic listeners

### `ConditionalEventListener<E>`

Filter at the dispatcher level; `onEvent` is simply never called for a rejected event.

```java
public interface ConditionalEventListener<E extends Event> extends EventListener<E> {
    boolean accept(E event);
}
```

```java
public class HighValueOrderListener implements EventListener<OrderPlaced>, ConditionalEventListener<OrderPlaced> {

    @Override
    public boolean accept(OrderPlaced event) {
        return event.getAmount().compareTo(BigDecimal.valueOf(10_000)) > 0;
    }

    @Override
    public void onEvent(OrderPlaced event) {
        riskTeam.alert(event.getOrderId());
    }
}
```

### `GenericEventListener`

One listener, many handler methods — the listener declares no event type; instead every eligible method becomes a
handler for its parameter's event type.

```java
public abstract class GenericEventListener implements EventListener<Event> {

    public final void onEvent(Event event)                 // dispatches reflectively to handlers
    protected boolean isHandleEventMethod(Method method)   // override to widen/narrow eligibility
}
```

A method is a handler when it is public, returns `void`, declares no checked exceptions, takes exactly one
`Event`-subtype parameter, and is not `onEvent` itself.

```java
public class OrderAnalytics extends GenericEventListener {

    public void handle(OrderPlaced event) {          // handler for OrderPlaced
        counters.increment("orders");
    }

    public void handle(OrderCancelled event) {       // handler for OrderCancelled
        counters.decrement("orders");
    }
}
```

`onEvent` is `final`, so you cannot override it in a `GenericEventListener`; add handler methods instead.

---

## 7. SPI auto-loading of listeners

`AbstractEventDispatcher`'s constructor calls `loadEventListenerInstances()`:

```java
protected void loadEventListenerInstances() {
    loadServicesList(EventListener.class, getDefaultClassLoader(), true)
            .stream()
            .sorted()
            .forEach(this::addEventListener);
}
```

So the file your application must provide is:

```
src/main/resources/META-INF/services/io.microsphere.event.EventListener
```

containing one implementation class name per line:

```
com.example.listener.AuditListener
com.example.listener.HighValueOrderListener
```

> [!IMPORTANT]
> Two things people get wrong:
> 1. The service interface file is named **`io.microsphere.event.EventListener`**. The
>    `META-INF/services/io.microsphere.event.EventDispatcher` file that ships inside `microsphere-java-core`
>    (listing `DirectEventDispatcher` and `ParallelEventDispatcher`) is **not** used for listener auto-loading —
>    nothing in `AbstractEventDispatcher` reads it.
> 2. Loading happens **per dispatcher instance**, from the *default class loader*, cached
>    (`loadServicesList(..., true)`). If no `io.microsphere.event.EventListener` service file exists at all, the
>    call throws `IllegalArgumentException`; the dispatcher swallows it at `logger.trace`, so a missing/mis-named
>    file fails **silently at trace level**. Turn on `io.microsphere` trace logging while wiring this up.

Every dispatcher you create gets its own listener instances — SPI-loaded listeners are **not shared** between
dispatchers. Keep them stateless or thread-safe if you create several dispatchers, or share one dispatcher instance.

---

## 8. Dispatch mechanics and extending the dispatcher

Storage: `ConcurrentMap<Class<? extends Event>, List<EventListener>> listenersCache`, keyed by event type.

* **Mutation path** (`addEventListener` / `removeEventListener`) runs under a private `mutex`, re-sorts the affected
  list with `Prioritized.COMPARATOR`, and invalidates derived caches. Sorting happens at mutation time, not at
  dispatch time.
* **Dispatch path** is a lock-free read: every cached entry whose key `isAssignableFrom(event.getClass())`
  contributes listeners, the combined stream is `.sorted()`, `ConditionalEventListener.accept` gates each callback,
  then `onEvent` runs — inside `getExecutor()`.

Because matching uses `isAssignableFrom`, a listener for a **base** event type also receives subclass events.

```java
public class MdcEventDispatcher extends AbstractEventDispatcher {

    // The only real requirement: supply the Executor to super(...)
    public MdcEventDispatcher(Executor executor) {
        super(executor);
    }

    // getExecutor() is FINAL in AbstractEventDispatcher and returns the constructor-supplied executor,
    // so context propagation belongs in the executor wrapper, not in an override:
    public static EventDispatcher newMdcDispatcher(Executor delegate) {
        Executor mdcAware = command -> delegate.execute(() -> {
            Map<String, String> context = MDC.getCopyOfContextMap();
            try {
                command.run();
            } finally {
                if (context != null) MDC.setContextMap(context); else MDC.clear();
            }
        });
        return new MdcEventDispatcher(mdcAware);
    }
}
```

Protected extension points on `AbstractEventDispatcher`:

```java
protected final Logger logger;
protected Stream<EventListener> sortedListeners()
protected Stream<EventListener> sortedListeners(Predicate<? super Map.Entry<Class<? extends Event>, List<EventListener>>> filter)
protected void doInListener(EventListener<?> listener, Consumer<Collection<EventListener>> callback)
protected void loadEventListenerInstances()
```

`parallel(Executor)` / `of(Executor)` are the supported way to plug in your own executor; extending
`AbstractEventDispatcher` is only necessary when you must change listener loading or interception.

---

## 9. Thread-safety

| Operation | Guarantee |
|---|---|
| `dispatch(Event)` | lock-free read of the `ConcurrentMap`; safe to call concurrently |
| `addEventListener` / `removeEventListener` | synchronized on an internal mutex; blocks other mutations |
| `DirectEventDispatcher` callbacks | run on the caller's thread — order equals priority order, exceptions propagate to the caller |
| `ParallelEventDispatcher` callbacks | run concurrently — **listeners must be thread-safe**, exceptions are absorbed by the executor task |
| SPI-loaded listener instances | one instance per dispatcher, loaded once at construction |

Annotate your listeners accordingly with
[`@ThreadSafe` / `@NotThreadSafe`](annotations.md#1-reference) when they are shared.

> [!WARNING]
> A listener throwing in `DirectEventDispatcher` stops the remaining listeners for that event (the exception
> propagates). Catch inside `onEvent` if the remaining listeners must still run. With `ParallelEventDispatcher`
> failures are not surfaced to the caller at all — log them yourself.

---

## 10. End-to-end example

```java
import io.microsphere.event.*;
import io.microsphere.lang.Prioritized;

public class Orders {

    private static final EventDispatcher dispatcher = EventDispatcher.newDefault();

    public static void place(String id, BigDecimal amount) {
        dispatcher.dispatch(new OrderPlaced(Orders.class, id, amount));
    }

    static {
        dispatcher.addEventListeners(new AuditListener(), new HighValueOrderListener());
    }
}

class AuditListener implements EventListener<OrderPlaced> {

    @Override
    public void onEvent(OrderPlaced event) {
        System.out.printf("AUDIT %s at %d%n", event.getOrderId(), event.getTimestamp());
    }

    @Override
    public int getPriority() {
        return Prioritized.MAX_PRIORITY;   // runs first
    }
}
```

```
AUDIT ORD-001 at 1759108800000
```

---

## 11. See also

* [I/O, Files and Watching](io-and-file-watch.md) — `FileChangedEvent` / `FileChangedListener`, the framework's own
  event consumers
* [Language Abstractions](language-abstractions.md#1-prioritized--the-ordering-spine) — priority semantics
* [Reference](reference.md#1-spi-registry) — service file inventory

[← Previous: Type Conversion](type-conversion.md) · [Index](README.md) · [Next: I/O, Files and Watching →](io-and-file-watch.md)
