# Event Dispatching

> Read this page in: [中文](../zh/events.md) · [English](events.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Module / package | `microsphere-java-core` / `io.microsphere.event` |
| Scope | in-JVM observer pattern only — no broker, no remoting |
| Entry points | `EventDispatcher.newDefault()`, `parallel(Executor)`, `of(Executor)` |
| Ordering | `io.microsphere.lang.Prioritized` — **lowest integer runs first** |
| Listener auto-registration | `META-INF/services/io.microsphere.event.EventListener` |
| Extra dependencies | none beyond the JDK |

Three types carry the whole model: `Event` (a `java.util.EventObject` plus a timestamp),
`EventListener<E>` (one method plus a priority) and `EventDispatcher` (a registry keyed by event type that
runs listeners inside an `Executor`). There is **no** singleton dispatcher: every factory call returns a new
instance with its own registry.

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

`Listenable` is a standalone registration contract you can reuse outside event dispatching;
`AbstractEventDispatcher` is the only type you normally extend.

---

## 2. Define an event

`Event` is `public abstract class Event extends java.util.EventObject` with `Event(Object source)` (source
must not be `null`) and `public long getTimestamp()`, assigned from `System.currentTimeMillis()` in the
constructor — subclasses must not re-record it. Events should be immutable: final fields, no setters,
`source` pointing at whatever fired them.

```java
public class OrderPlaced extends Event {

    private final String orderId;
    private final BigDecimal amount;

    public OrderPlaced(Object source, String orderId, BigDecimal amount) {
        super(source);
        this.orderId = orderId;
        this.amount = amount;
    }

    public String getOrderId()    { return orderId; }
    public BigDecimal getAmount() { return amount; }
}
```

To carry a payload without declaring a class, use `new GenericEvent<>("order-001")` and subscribe with
`EventListener<GenericEvent>`; `GenericEvent#getSource()` narrows the return type to `S`, but the dispatcher
keys on the listener's declared event type, never on the payload type.

---

## 3. Implement a listener

```java
@FunctionalInterface
public interface EventListener<E extends Event> extends java.util.EventListener, Prioritized {

    void onEvent(E event);
    default int getPriority()   // returns Prioritized.NORMAL_PRIORITY (0)
}
```

The generic parameter `E` **is** the subscription: the dispatcher resolves it at **registration time** via
the static helpers `EventListener.findEventType(EventListener<?>)`, `findEventType(Class<?>)` and
`findEventType(ParameterizedType)`, so `E` must be a concrete type in the `implements` clause.

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
> A raw `implements EventListener` (or a type variable as the argument) resolves to a `null` event type, and
> `AbstractEventDispatcher` skips the registration silently: no exception, no log line, no callbacks.

Priorities follow `Prioritized` and sorting is **ascending**, so the smallest integer runs first:

| Constant | Value | Effect |
|---|---|---|
| `Prioritized.MAX_PRIORITY` | `Integer.MIN_VALUE` | runs first |
| `Prioritized.NORMAL_PRIORITY` | `0` | default; after every negative-priority listener |
| `Prioritized.MIN_PRIORITY` | `Integer.MAX_VALUE` | runs last |

> [!NOTE]
> The Javadoc of `EventListener.getPriority()` claims the default is `Integer#MAX_VALUE`; the code returns
> `NORMAL_PRIORITY` (`0`). Trust the code.

Lambdas work, because `findEventType` resolves the single parameter type through
`io.microsphere.lang.invoke.LambdaUtils` — but the target type must be explicit and priority stays normal:

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
> The factory names are exactly `newDefault()`, `parallel(Executor)`, `of(Executor)` — there is no
> `getInstance()`, `create()`, `sequential(...)` or `builder()`. Each call builds a **new** dispatcher with
> its own registry, so keep the instance (a `static final` field or a DI-managed singleton) rather than
> calling a factory per event.

```java
private static final EventDispatcher sync  = EventDispatcher.newDefault();
private static final EventDispatcher async = EventDispatcher.parallel(pool);   // you own the pool

sync.dispatch(new OrderPlaced(this, orderId, amount));   // returns after every listener has run
```

`ParallelEventDispatcher` also has a no-arg constructor that uses `ForkJoinPool.commonPool()` — handy in
tests, but it shares the pool with parallel streams, so pass your own `Executor` in production:

```java
ExecutorService pool = Executors.newFixedThreadPool(4,
        CustomizedThreadFactory.newThreadFactory("event-dispatch"));
ExecutorUtils.shutdownOnExit(pool);              // closes the pool on JVM exit
```

`EventDispatcher.of(executor)` is the null-safe choice when the executor comes from configuration: `null`
selects the direct dispatcher.

---

## 5. Register and remove listeners

```java
public interface Listenable<E extends EventListener<?>> {

    static void assertListener(EventListener<?> listener) throws IllegalArgumentException   // null-check only

    void addEventListener(E listener) throws NullPointerException, IllegalArgumentException;
    void removeEventListener(E listener) throws NullPointerException, IllegalArgumentException;
    @Nonnull List<E> getAllEventListeners();

    default void addEventListeners(E listener, E... others)
    default void addEventListeners(Iterable<E> listeners)
    default void removeEventListeners(Iterable<E> listeners)
    default void removeAllEventListeners()   // removeEventListeners(getAllEventListeners())
}
```

| Behaviour | Detail to rely on |
|---|---|
| Duplicate add | ignored — added only if absent (`equals`-based) |
| One instance, two event types | impossible; stored under its single resolved type |
| Removal | needs the same instance; the type is re-resolved, so an unresolvable listener is a no-op |
| `getAllEventListeners()` | unmodifiable, de-duplicated, priority-sorted snapshot; mutating it cannot change the registry |

> [!NOTE]
> `assertListener` currently only null-checks. The "listener must not be a final class / proxy" rejection
> described in its Javadoc is commented out in the source — do not design around it.

> [!WARNING]
> The registry holds **strong** references. A listener on a long-lived dispatcher is never garbage
> collected, even after its owner (servlet, window, request-scoped bean) should be unreachable — a leak, not
> a feature. Remove listeners in a `finally` block, a shutdown callback or the owner's destroy method; there
> is no weak-reference mode.

---

## 6. Conditional and generic listeners

`ConditionalEventListener<E extends Event>` adds `boolean accept(E event)`. The dispatcher checks it on the
dispatch thread right before `onEvent`; a rejected event never reaches the handler.

```java
public class HighValueOrderListener
        implements EventListener<OrderPlaced>, ConditionalEventListener<OrderPlaced> {

    @Override
    public boolean accept(OrderPlaced event) {
        return event.getAmount().compareTo(BigDecimal.valueOf(10_000)) > 0;
    }

    @Override
    public void onEvent(OrderPlaced event) { riskTeam.alert(event.getOrderId()); }
}
```

`GenericEventListener` inverts the model: it is an abstract `EventListener<Event>` whose
`public final void onEvent(Event)` reflects each event to matching methods, so one listener handles many
types. `protected boolean isHandleEventMethod(Method)` decides eligibility — override it to widen or narrow
the filter. Since `onEvent` is `final`, extend by adding methods, never by overriding:

```java
public class OrderAnalytics extends GenericEventListener {

    public void onOrderPlaced(OrderPlaced event)       { counters.increment("orders"); }
    public void onOrderCancelled(OrderCancelled event) { counters.decrement("orders"); }
}
```

All rules below are checked by `isHandleEventMethod` against `getClass().getMethods()`:

| Rule | Consequence |
|---|---|
| Not `onEvent(Event)` itself | the router method is never a handler |
| Return type `void` | fluent / `Optional` methods are skipped |
| Declares **no** exception types | `throws IOException` disqualifies it, even checked |
| Exactly one parameter | zero- and multi-parameter methods are skipped |
| Parameter is an `Event` subtype | `String` / `Integer` handlers are ignored |

> [!IMPORTANT]
> Two routing details, both from the source: a `GenericEventListener` resolves to event type `Event`, so it
> is stored under the `Event` key and invoked for **every** dispatched event; and inside `onEvent` handlers
> come from `handleEventMethods.get(event.getClass())` — an **exact class match**, so a handler declared for
> a base type does *not* run for subclass events, unlike the dispatcher-level matching in section 9.

---

## 7. SPI auto-loading of listeners

The last statement of `AbstractEventDispatcher`'s constructor is `loadEventListenerInstances()`:

```java
protected void loadEventListenerInstances() {
    execute(() -> loadServicesList(EventListener.class, getDefaultClassLoader(), true)
            .stream().sorted().forEach(this::addEventListener),
            e -> logger.trace(e.getMessage()));
}
```

You provide a file named after **`EventListener`**, one class per line, each with a public no-arg
constructor: `src/main/resources/META-INF/services/io.microsphere.event.EventListener` containing
`com.example.listener.AuditListener` and similar.

> [!IMPORTANT]
> Two traps:
> 1. The `META-INF/services/io.microsphere.event.EventDispatcher` file shipped inside `microsphere-java-core`
>    (listing `DirectEventDispatcher`, `ParallelEventDispatcher`) is **not** read by anything in
>    `AbstractEventDispatcher`; listener auto-loading never consults it.
> 2. Failures are swallowed at trace level. Without an `...EventListener` service file `loadServicesList`
>    throws `IllegalArgumentException`, and `loadEventListenerInstances` logs it with `logger.trace` — so a
>    missing or mis-named file simply looks like "listeners don't fire". Enable `io.microsphere` TRACE while
>    wiring this up (see [Logging](logging.md)).

* Loading uses the **default class loader**
  (`io.microsphere.util.ClassLoaderUtils#getDefaultClassLoader`); listeners invisible to it are invisible to
  the dispatcher.
* The `true` argument is a literal here, so loaded instances are memoised in `ServiceLoaderUtils`' cache.
  The `microsphere.service-loader.cached` system property
  (`ServiceLoaderUtils.SERVICE_LOADER_CACHED_PROPERTY_NAME`, default `false`) does **not** affect listener
  auto-loading; it only defaults the flag for `ServiceLoaderUtils` overloads that pass no explicit value.
* Caching is per service type, so **every dispatcher sees the same listener objects** under a given class
  loader — keep SPI listeners stateless and thread-safe.
* Override `loadEventListenerInstances()` in a subclass to disable or replace SPI loading.

---

## 8. Dispatch mechanics, custom dispatchers, thread safety

Storage is a `ConcurrentMap<Class<? extends Event>, List<EventListener>>` keyed by event type.

* **Mutation** (`addEventListener` / `removeEventListener`) runs inside `synchronized (mutex)`, appends or
  removes, then re-sorts that list with `Collections.sort` — sorting happens at mutation time, never at
  dispatch time.
* **Dispatch** is a lock-free read: entries whose key satisfies `key.isAssignableFrom(event.getClass())`
  contribute their listeners, the combined stream is `.sorted()`, `ConditionalEventListener.accept` gates
  each callback, then `onEvent` runs — all inside `getExecutor().execute(...)`.

`isAssignableFrom` matching means a listener for a **base** type receives subclass events, while a listener
for a subclass never receives parent events.

```java
public class MdcEventDispatcher extends AbstractEventDispatcher {

    // Only requirement: a non-null Executor passed to super(...). getExecutor() is final, so context
    // propagation goes into the executor wrapper, not into an override.
    public MdcEventDispatcher(Executor delegate) {
        super(command -> delegate.execute(() -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try { command.run(); } finally { if (previous != null) MDC.setContextMap(previous); else MDC.clear(); }
        }));
    }
}
```

Other protected extension points: `logger` (`protected final Logger`), `sortedListeners()`,
`sortedListeners(Predicate<Map.Entry<Class<? extends Event>, List<EventListener>>>)`,
`doInListener(EventListener<?>, Consumer<Collection<EventListener>>)` (mutate one list under the mutex) and
`loadEventListenerInstances()`.

| Operation | Guarantee |
|---|---|
| `dispatch(Event)` | lock-free read of the `ConcurrentMap`; safe to call concurrently |
| add / remove listener | synchronised on an internal mutex; blocks other mutations |
| `DirectEventDispatcher` callbacks | caller's thread, priority order; exceptions propagate to the caller |
| `ParallelEventDispatcher` callbacks | concurrent — listeners must be thread-safe; exceptions stay in the task |
| SPI-loaded listeners | one shared instance set per service type/class loader, loaded in the constructor |

Adding a listener while another thread dispatches is safe; the in-flight dispatch may simply not see it.

> [!WARNING]
> A listener that throws under `DirectEventDispatcher` aborts the remaining listeners for that event and the
> exception surfaces at the `dispatch` call site — catch inside `onEvent` if later listeners must run. Under
> `ParallelEventDispatcher` failures are never reported to the caller: log them in the listener or wrap the
> executor.

> [!TIP]
> `parallel(executor)` / `of(executor)` cover most needs; extend `AbstractEventDispatcher` only to change
> loading or interception. Annotate shared listeners with
> [`@ThreadSafe` / `@NotThreadSafe`](annotations.md#1-reference) so the contract lives on the type.

---

## See also

* [I/O and File Watching](io-and-file-watch.md) — `FileChangedEvent` / `FileChangedListener`, the framework's own event consumers
* [Annotations](annotations.md) — `@ThreadSafe`, `@Immutable` on event and listener types
* [Reflection and Types](reflection-and-types.md) — how `findEventType` resolves the generic parameter
* [Logging](logging.md) — TRACE output for SPI loading
* [Reference](reference.md) — full SPI file inventory, including `io.microsphere.event.EventDispatcher`

[← Handbook index](../README.md) · [Previous: Type Conversion](type-conversion.md) · [Next: I/O and File Watching →](io-and-file-watch.md)
