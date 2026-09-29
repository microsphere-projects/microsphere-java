# Event Dispatching

> Read this page in: [中文](../zh/events.md) · [English](events.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Module | `io.github.microsphere-projects:microsphere-java-core` |
| Package | `io.microsphere.event` |
| Scope | in-JVM observer pattern only — no broker, no remoting |
| Entry points | `EventDispatcher.newDefault()`, `parallel(Executor)`, `of(Executor)` |
| Ordering | `io.microsphere.lang.Prioritized` — **lowest integer runs first** |
| Listener auto-registration | `META-INF/services/io.microsphere.event.EventListener` |
| Extra dependencies | none beyond the JDK |

The model is three types: an `Event` (a `java.util.EventObject` plus a timestamp), an
`EventListener<E>` (one method plus a priority) and an `EventDispatcher` (a registry keyed by event type
that runs listeners inside an `Executor`). There is no singleton dispatcher — each factory call returns a
new instance with its own registry.

---

## 1. The type hierarchy

```
Event (abstract, extends java.util.EventObject)
 ├── GenericEvent<S>
 └── FileChangedEvent                -> io-and-file-watch.md

EventListener<E extends Event> (functional interface; extends java.util.EventListener + Prioritized)
 ├── ConditionalEventListener<E>     accept() gate, checked before onEvent()
 └── GenericEventListener            one listener, many handler methods

Listenable<E extends EventListener<?>>        add / remove / query
 └── EventDispatcher extends Listenable<EventListener<?>>
      ├── AbstractEventDispatcher    shared storage, sorting, SPI loading
      │    ├── DirectEventDispatcher         final; same-thread
      │    └── ParallelEventDispatcher       Executor-backed
```

| Type | Role |
|---|---|
| `Event` / `GenericEvent<S>` | payload; `GenericEvent` needs no subclass and narrows `getSource()` to `S` |
| `EventListener<E>` | subscriber; the generic parameter `E` **is** the subscription |
| `ConditionalEventListener<E>` | adds `boolean accept(E)` filtering |
| `GenericEventListener` | routes one `Event` subscription to per-type handler methods by reflection |
| `Listenable<E>` | registration contract, reusable outside event dispatching |
| `AbstractEventDispatcher` | the only implementation you normally extend |

---

## 2. Define an event

```java
public abstract class Event extends java.util.EventObject {

    public Event(Object source)      // source must not be null (EventObject contract)
    public long getTimestamp()       // System.currentTimeMillis(), captured in the constructor
}
```

Make it immutable — final fields, no setters, `source` pointing at whatever fired it:

```java
public class OrderPlaced extends Event {

    private final String orderId;
    private final BigDecimal amount;

    public OrderPlaced(Object source, String orderId, BigDecimal amount) {
        super(source);
        this.orderId = orderId;
        this.amount = amount;
    }

    public String getOrderId()   { return orderId; }
    public BigDecimal getAmount() { return amount; }
}
```

`getTimestamp()` is assigned in `Event`'s constructor, so subclasses must not re-record it. For a quick
payload-only event, `new GenericEvent<>("order-001")` and subscribe with `EventListener<GenericEvent>`:
the dispatcher keys on the listener's declared event type, never on the payload type `S`.

---

## 3. Implement a listener

```java
@FunctionalInterface
public interface EventListener<E extends Event> extends java.util.EventListener, Prioritized {

    void onEvent(E event);
    default int getPriority()   // returns Prioritized.NORMAL_PRIORITY (0)

    static Class<? extends Event> findEventType(EventListener<?> listener)
    static Class<? extends Event> findEventType(Class<?> listenerClass)
    static Class<? extends Event> findEventType(ParameterizedType parameterizedType)
}
```

The dispatcher resolves your event type **at registration time** from the `implements` clause, so `E` must
be a concrete type:

```java
public class AuditListener implements EventListener<OrderPlaced> {

    @Override
    public void onEvent(OrderPlaced event) {
        audit.record(event.getOrderId(), event.getTimestamp());
    }

    @Override
    public int getPriority() {
        return Prioritized.MAX_PRIORITY;   // Integer.MIN_VALUE -> runs before everyone else
    }
}
```

> [!WARNING]
> A raw `implements EventListener` (or a type variable as the argument) resolves to a `null` event type.
> `AbstractEventDispatcher` then skips the registration silently — the listener is never stored and never
> called, with no exception and no log line.

Sorting is **ascending**, so the smallest integer runs first:

| Constant | Value | Effect |
|---|---|---|
| `Prioritized.MAX_PRIORITY` | `Integer.MIN_VALUE` | runs first |
| `Prioritized.NORMAL_PRIORITY` | `0` | default; runs after every negative-priority listener |
| `Prioritized.MIN_PRIORITY` | `Integer.MAX_VALUE` | runs last |

> [!NOTE]
> The Javadoc of `EventListener.getPriority()` states the default is `Integer#MAX_VALUE`; the
> implementation returns `NORMAL_PRIORITY` (`0`). Trust the code.

Lambdas work because `findEventType` resolves the single parameter type through
`io.microsphere.lang.invoke.LambdaUtils`. Spell the target type out, and accept that a lambda always runs
at normal priority:

```java
dispatcher.addEventListener((EventListener<OrderPlaced>) event -> log.info("order {}", event.getOrderId()));
```

---

## 4. Get a dispatcher and dispatch

```java
public interface EventDispatcher extends Listenable<EventListener<?>> {

    Executor DIRECT_EXECUTOR = Runnable::run;   // same-thread execution

    void dispatch(Event event);
    default Executor getExecutor() { return DIRECT_EXECUTOR; }

    @Nonnull static EventDispatcher newDefault()                         // -> DirectEventDispatcher
    @Nonnull static EventDispatcher parallel(@Nonnull Executor executor) // -> ParallelEventDispatcher
    @Nonnull static EventDispatcher of(@Nullable Executor executor)      // null -> newDefault(), else parallel
}
```

> [!IMPORTANT]
> The factory names are exactly `newDefault()`, `parallel(Executor)` and `of(Executor)`. There is no
> `getInstance()`, `create()`, `sequential(...)` or `builder()`. Every call builds a **new** dispatcher with
> its own registry, so keep the instance (a `static final` field or a DI-managed singleton) instead of
> calling a factory per event.

```java
private static final EventDispatcher dispatcher = EventDispatcher.newDefault();

public void place(String orderId, BigDecimal amount) {
    dispatcher.dispatch(new OrderPlaced(this, orderId, amount));  // returns after all listeners ran
}
```

Asynchronous dispatch — you own the executor's lifecycle:

```java
ExecutorService pool = Executors.newFixedThreadPool(4,
        CustomizedThreadFactory.newThreadFactory("event-dispatch"));
ExecutorUtils.shutdownOnExit(pool);              // shuts the pool down on JVM exit

EventDispatcher dispatcher = EventDispatcher.parallel(pool);
```

`ParallelEventDispatcher` also has a no-arg constructor using `ForkJoinPool.commonPool()`: convenient in
tests, but it shares the pool with parallel streams, so pass your own executor in production.
`EventDispatcher.of(executor)` is the null-safe choice when the executor comes from configuration — `null`
yields the direct dispatcher.

---

## 5. Register and remove listeners

```java
public interface Listenable<E extends EventListener<?>> {

    static void assertListener(EventListener<?> listener) throws IllegalArgumentException

    void addEventListener(E listener) throws NullPointerException, IllegalArgumentException;
    void removeEventListener(E listener) throws NullPointerException, IllegalArgumentException;
    default void addEventListeners(E listener, E... others)
    default void addEventListeners(Iterable<E> listeners)
    default void removeEventListeners(Iterable<E> listeners)
    default void removeAllEventListeners()

    @Nonnull List<E> getAllEventListeners();
}
```

```java
dispatcher.addEventListeners(auditListener, notifyListener, metricsListener);
dispatcher.getAllEventListeners();     // unmodifiable, de-duplicated, priority-sorted snapshot
dispatcher.removeEventListener(auditListener);
dispatcher.removeAllEventListeners();
```

| Behaviour | Detail to rely on |
|---|---|
| Duplicate add | ignored — added only if absent (`equals`-based) |
| One instance, two event types | not possible; stored under its single resolved type |
| Removal | needs the same instance; the type is re-resolved, so an unresolvable listener is a no-op |
| `getAllEventListeners()` | `@Immutable` snapshot; mutating it cannot change the registry |

> [!NOTE]
> `assertListener` currently only null-checks its argument. The "listener must not be a final class /
> proxy" rejection described in its Javadoc is commented out in the source — do not design around it.

> [!WARNING]
> The registry holds **strong** references. A listener on a long-lived dispatcher is never garbage
> collected, even after its owner (a servlet, a window, a request-scoped bean) should be unreachable. That
> is a leak, not a feature: remove listeners in a `finally` block, a shutdown callback or the owner's
> destroy method. There is no weak-reference mode.

---

## 6. Filter with `ConditionalEventListener`

`accept` is evaluated by the dispatcher on the dispatch thread, immediately before `onEvent`, for every
event of the matching type; a rejected event never reaches `onEvent`.

```java
public interface ConditionalEventListener<E extends Event> extends EventListener<E> {

    boolean accept(E event);
}

public class HighValueOrderListener
        implements EventListener<OrderPlaced>, ConditionalEventListener<OrderPlaced> {

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

> [!TIP]
> The dispatcher only needs `instanceof ConditionalEventListener`, but declaring `EventListener<E>` as well
> keeps the subscribed event type explicit. Keep `accept` cheap and side-effect-free — it runs once per
> listener per event.

---

## 7. Many handlers in one listener: `GenericEventListener`

```java
public abstract class GenericEventListener implements EventListener<Event> {

    public final void onEvent(Event event)                 // routes reflectively to handlers
    protected boolean isHandleEventMethod(Method method)   // override to widen/narrow eligibility
}

public class OrderAnalytics extends GenericEventListener {

    public void onOrderPlaced(OrderPlaced event)    { counters.increment("orders"); }
    public void onOrderCancelled(OrderCancelled event) { counters.decrement("orders"); }
}
```

A method becomes a handler when every rule in `isHandleEventMethod` passes, over `getClass().getMethods()`:

| Rule | Consequence |
|---|---|
| Not `onEvent(Event)` itself | the router method is never a handler |
| Return type `void` | fluent / `Optional` methods are skipped |
| Declares **no** exception types | `throws IOException` disqualifies it, even checked |
| Exactly one parameter | zero- and multi-parameter methods are skipped |
| Parameter is an `Event` subtype | `String`, `Integer` handlers are ignored |

> [!IMPORTANT]
> Two routing details, both from the source:
> 1. A `GenericEventListener` resolves to event type `Event`, so it is stored under the `Event` key and is
>    invoked for **every** dispatched event.
> 2. Inside `onEvent`, handlers come from `handleEventMethods.get(event.getClass())` — an **exact class
>    match**. A handler declared for a base type does *not* run for subclass events, unlike the
>    dispatcher-level matching in section 9.
>
> `onEvent` is `final`: add handler methods instead of overriding it.

---

## 8. SPI auto-loading of listeners

`AbstractEventDispatcher`'s constructor ends with `loadEventListenerInstances()`:

```java
protected void loadEventListenerInstances() {
    execute(() -> loadServicesList(EventListener.class, getDefaultClassLoader(), true)
            .stream()
            .sorted()
            .forEach(this::addEventListener),
            e -> logger.trace(e.getMessage()));
}
```

The file your application provides is named after **`EventListener`**, one class per line, each with a
public no-arg constructor:

```
src/main/resources/META-INF/services/io.microsphere.event.EventListener
```

```
com.example.listener.AuditListener
com.example.listener.HighValueOrderListener
```

> [!IMPORTANT]
> Two traps:
> 1. The `META-INF/services/io.microsphere.event.EventDispatcher` file shipped inside
>    `microsphere-java-core` (listing `DirectEventDispatcher` and `ParallelEventDispatcher`) is **not**
>    read by anything in `AbstractEventDispatcher`; listener auto-loading never consults it.
> 2. Failures are swallowed at trace level. With no `...EventListener` service file, `loadServicesList`
>    throws `IllegalArgumentException` and `loadEventListenerInstances` logs it via `logger.trace`, so a
>    missing or mis-named file looks like "listeners just don't fire". Enable `io.microsphere` TRACE while
>    wiring this up — see [Logging](logging.md).

* Loading uses the **default class loader**
  (`io.microsphere.util.ClassLoaderUtils#getDefaultClassLoader`); listeners invisible to it are invisible
  to the dispatcher.
* The `true` argument is a literal here, so instances are memoised in `ServiceLoaderUtils`' cache. The
  `microsphere.service-loader.cached` system property
  (`ServiceLoaderUtils.SERVICE_LOADER_CACHED_PROPERTY_NAME`, default `false`) does **not** affect listener
  auto-loading; it only defaults the flag for `ServiceLoaderUtils` overloads that do not pass one.
* Because instances are cached per service type, **every dispatcher sees the same listener objects** for a
  given class loader — keep SPI listeners stateless and thread-safe.
* Override `loadEventListenerInstances()` in a subclass to disable or replace SPI loading.

---

## 9. Dispatch mechanics and custom dispatchers

Storage is a `ConcurrentMap<Class<? extends Event>, List<EventListener>>` keyed by event type.

* **Mutation path** (`addEventListener` / `removeEventListener`) runs inside `synchronized (mutex)`,
  appends or removes, then re-sorts that list with `Collections.sort`. Sorting happens at mutation time,
  never at dispatch time.
* **Dispatch path** is a lock-free read: entries whose key satisfies
  `key.isAssignableFrom(event.getClass())` contribute their listeners, the combined stream is `.sorted()`,
  `ConditionalEventListener.accept` gates each callback, then `onEvent` runs — all inside
  `getExecutor().execute(...)`.

Because matching is `isAssignableFrom`, a listener for a **base** type receives subclass events; a listener
for a subclass never receives parent events.

```java
public class MdcEventDispatcher extends AbstractEventDispatcher {

    public MdcEventDispatcher(Executor executor) {
        super(executor);       // the executor must not be null
    }

    // getExecutor() is final in AbstractEventDispatcher, so context propagation belongs in the
    // executor wrapper rather than in an override:
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

Protected extension points on `AbstractEventDispatcher`: `logger` (`protected final Logger`),
`sortedListeners()`, `sortedListeners(Predicate<Map.Entry<Class<? extends Event>, List<EventListener>>>)`,
`doInListener(EventListener<?>, Consumer<Collection<EventListener>>)` (mutate one list under the mutex) and
`loadEventListenerInstances()`.

> [!TIP]
> `EventDispatcher.parallel(executor)` and `of(executor)` cover most needs. Subclass
> `AbstractEventDispatcher` only when you must change listener loading or interception — and remember that
> `getExecutor()` is `final`, so the executor is a constructor argument, not an override.

---

## 10. Thread safety and failures

| Operation | Guarantee |
|---|---|
| `dispatch(Event)` | lock-free read of the `ConcurrentMap`; safe to call concurrently |
| `addEventListener` / `removeEventListener` | synchronised on an internal mutex; blocks other mutations |
| `DirectEventDispatcher` callbacks | caller's thread, in priority order; exceptions propagate to the caller |
| `ParallelEventDispatcher` callbacks | concurrent — listeners must be thread-safe; exceptions stay in the task |
| SPI-loaded listeners | one shared instance set per service type/class loader, loaded in the constructor |

Registering while another thread dispatches is safe; the in-flight dispatch simply may not see the new
listener. Annotate shared listeners with
[`@ThreadSafe` / `@NotThreadSafe`](annotations.md#1-reference) so the contract lives on the type.

> [!WARNING]
> A listener that throws in `DirectEventDispatcher` aborts the remaining listeners for that event and the
> exception surfaces at the `dispatch` call site; catch inside `onEvent` if later listeners must run. With
> `ParallelEventDispatcher` failures are never reported to the caller — log them inside the listener or
> wrap the executor.

---

## 11. End-to-end example

```java
import io.microsphere.event.*;
import io.microsphere.lang.Prioritized;

public class Orders {

    private static final EventDispatcher dispatcher = EventDispatcher.newDefault();

    static {
        dispatcher.addEventListeners(new AuditListener(), new HighValueOrderListener());
    }

    public static void place(String id, BigDecimal amount) {
        dispatcher.dispatch(new OrderPlaced(Orders.class, id, amount));
    }

    static class AuditListener implements EventListener<OrderPlaced> {

        @Override
        public void onEvent(OrderPlaced event) {
            System.out.printf("AUDIT %s at %d%n", event.getOrderId(), event.getTimestamp());
        }

        @Override
        public int getPriority() {
            return Prioritized.MAX_PRIORITY;   // runs first
        }
    }
}
```

```
AUDIT ORD-001 at 1759108800000
```

---

## See also

* [I/O and File Watching](io-and-file-watch.md) — `FileChangedEvent` / `FileChangedListener`, the framework's own event consumers
* [Annotations](annotations.md) — `@ThreadSafe`, `@Immutable` on event and listener types
* [Reflection and Types](reflection-and-types.md) — how `findEventType` resolves the generic parameter
* [Logging](logging.md) — turning on TRACE to see SPI loading
* [Reference](reference.md) — full SPI file inventory, including `io.microsphere.event.EventDispatcher`

[← Handbook index](../README.md) · [Previous: Type Conversion](type-conversion.md) · [Next: I/O and File Watching →](io-and-file-watch.md)
