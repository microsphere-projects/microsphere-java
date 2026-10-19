# Event dispatching

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-java-core` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Package** | `io.microsphere.event` — 10 main-source types · consumed by `io.microsphere.io.event` (3 types) and `io.microsphere.io.StandardFileWatchService` |
| **JDK level** | Compiled at Java 8; CI-tested on JDK 8 / 11 / 17 / 21 / 25 |
| **Depends on other `core` packages** | `io.microsphere.lang` (`Prioritized`), `io.microsphere.lang.invoke` (`LambdaUtils`), `io.microsphere.reflect` (`TypeUtils`), `io.microsphere.collection` (`MapUtils`, `ListUtils`, `SetUtils`, `MapUtils.newConcurrentHashMap`), `io.microsphere.util` (`Assert`, `ClassLoaderUtils`, `ServiceLoaderUtils`), `io.microsphere.lang.function` (`ThrowableAction`, `ThrowableConsumer`, `ThrowableFunction`), `io.microsphere.logging` (`Logger`, `LoggerFactory`), `io.microsphere.annotation` (separate `microsphere-java-annotations` artifact) |
| **External dependencies** | none — pure JDK (`java.util.EventObject`, `java.util.ServiceLoader`, `java.util.concurrent.Executor`) |
| **Used by** | `io` (`StandardFileWatchService`), `io.event` (`FileChangedEvent`, `FileChangedListener`, `LoggingFileChangedListener`) |

All `file:line` references in this page are relative to `microsphere-java-core/src/main/java/io/microsphere/`
(main sources) or `microsphere-java-core/src/test/java/io/microsphere/` (tests), read from the `release` worktree.
After the first mention they are shortened to `event/EventDispatcher.java:40-55` / `event/EventDispatcherTest.java:40-55`.

---

## 1. What this package is for

**Reach for `io.microsphere.event` when you want observer-style notifications inside a plain Java object — with no
container, no application context and no annotation processor in the loop.** The whole engine is 10 types
(`event/Event.java`, `event/GenericEvent.java`, `event/EventListener.java`, `event/ConditionalEventListener.java`,
`event/GenericEventListener.java`, `event/Listenable.java`, `event/EventDispatcher.java`,
`event/AbstractEventDispatcher.java`, `event/DirectEventDispatcher.java`, `event/ParallelEventDispatcher.java`), and the
only thing you must construct is a dispatcher object:

```java
EventDispatcher dispatcher = EventDispatcher.newDefault();   // event/EventDispatcher.java:70-73
dispatcher.addEventListener(listener);                       // event/AbstractEventDispatcher.java:116-121
dispatcher.dispatch(new EchoEvent("Hello,World"));           // event/AbstractEventDispatcher.java:162-180
```

**Contrast with Spring's `ApplicationEventPublisher` — stated only where the code shows a difference:**

| | `io.microsphere.event` | Spring `ApplicationEventPublisher` |
|---|---|---|
| What holds the listeners | the `EventDispatcher` instance itself, in a per-instance `ConcurrentMap` (`event/AbstractEventDispatcher.java:99`) | the `ApplicationContext` |
| How a listener is declared | implement `EventListener<E>` (`event/EventListener.java:71-72`) or extend `GenericEventListener` (`event/GenericEventListener.java:80`) | annotate a **method** on a bean |
| Annotation required | **no annotation type exists in this package at all** — `io.microsphere.event` declares zero `@interface` types | `@EventListener`, `@TransactionalEventListener`, `@ApplicationModuleListener` |
| Which events a listener gets | derived from the listener's generic parameter via `EventListener.findEventType(...)` (`event/EventListener.java:101-150`) | derived from the annotated method's parameter type |
| Extra listeners at startup | Java `ServiceLoader` — `loadEventListenerInstances()` (`event/AbstractEventDispatcher.java:214-219`) | bean scanning |
| Threading | one `Executor` per dispatcher: `DIRECT_EXECUTOR` (`event/EventDispatcher.java:63`) or a supplied one (`event/EventDispatcher.java:81-84`) | `@Async` / a configured `ApplicationEventMulticaster` |
| Vetoing one listener | `ConditionalEventListener.accept(E)` (`event/ConditionalEventListener.java:55`) | `@Conditional` on beans / `if` inside the method |
| Stopping propagation | **not supported — see §3.6** | `PublishEvent` has no stop either; Spring offers none |

**Do not reach for it when you need a transactional, ordered-across-threads, or hierarchical (parent-context) event
bus** — none of those exist here; there is a single flat listener registry per dispatcher.

> [!NOTE]
> The `event` package is also the base for `io.microsphere.io.event`: `FileChangedEvent extends Event`
> (`io/event/FileChangedEvent.java:34`) and `FileChangedListener extends EventListener<FileChangedEvent>`
> (`io/event/FileChangedListener.java:95`). `StandardFileWatchService` builds a real dispatcher per watched directory
> with `EventDispatcher.parallel(...)` (`io/StandardFileWatchService.java:278`), registers `FileChangedListener`
> instances on it (`io/StandardFileWatchService.java:271`) and dispatches from the watch loop
> (`io/StandardFileWatchService.java:247`).

---

## 2. Coordinates and version support

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

| Fact | Value | Evidence (`release`) |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | `pom.xml:12` |
| `artifactId` | `microsphere-java-core` | module `pom.xml` |
| Version property | `${revision}`, flattened | `pom.xml:14` |
| Value of `${revision}` on `release` | `0.3.20-SNAPSHOT` | `pom.xml:54` |
| Newest published tag | `0.3.20` | release-line table in the group index (`docs/current/user-guide/microsphere-java-core/README.md:70`) |
| Build parent | `microsphere-build:0.3.16` | `pom.xml:6-10` |
| Java level | 8 (no `maven.compiler.*` / `<release>` property in the reactor; the level is inherited from the external parent) | CI matrix `java: [ '8' , '11' , '17' , '21' , '25' ]`, `.github/workflows/maven-build.yml:25`, `distribution: 'temurin'` `:33` |
| CI command | `mvn --batch-mode --update-snapshots --file pom.xml -Drevision=0.0.1-SNAPSHOT -Dsurefire.useSystemClassLoader=false test --activate-profiles test,coverage` | `.github/workflows/maven-build.yml:38-45` |
| Spring Framework, JDK 17+ | `7.0.9` | `microsphere-java-parent/pom.xml:23` |
| Spring Framework, JDK `[1.8,17)` | `5.3.39` via profile `java8-16` | `microsphere-java-parent/pom.xml:54-62` (activation `<jdk>[1.8,17)</jdk>` at `:57`) |
| Imported BOMs | `microsphere-all-bom:0.3.13` (`microsphere-java-parent/pom.xml:22`, `:31-37`), `spring-framework-bom` (`:39-46`) | `microsphere-java-parent/pom.xml` |
| Bytecode/API relevant to this page | no `record`, no `var`, no `List.of`, no `instanceof` pattern matching | `EventDispatcher.parallel(@Nonnull Executor)` returns `new ParallelEventDispatcher(executor)` — a plain factory (`event/EventDispatcher.java:82-84`) |

> [!TIP]
> The `java8-16` profile is a **build-time** property switch, not a runtime one; `io.microsphere.event` has no Spring
> dependency whatsoever, so nothing on this page changes between Spring 5 and Spring 7. It matters only when you add
> Spring-facing `microsphere-java-*` modules to the same reactor.


### 2.1 Release date and what changed in `0.3.20`

| Item | Value | Evidence |
|---|---|---|
| Documented line | `release`, `${revision}` = `0.3.20-SNAPSHOT` | root `pom.xml:54` |
| Newest published release | `0.3.20`, tagged **2026-10-16** | the `0.3.20` tag resolves to `chore: merge main into release [skip ci]`, committed `2026-10-16T03:43:20Z` |
| Previous release | `0.3.19`, tagged **2026-08-30** | the `0.3.19` tag, committed `2026-08-30T12:51:37Z` |
| Changelog source | `release-notes.md` at the repository root | The project publishes no `CHANGELOG.md`; the `v0.3.16`–`v0.3.20` blocks of `release-notes.md` read *Release notes generation failed* and carry raw commit lists, so every entry below is quoted as a commit subject and checked against the `release` sources. |

`0.3.20` adds no new API surface for this area — the `0.3.19...0.3.20` range is a hardening release. What it does change, and how far that reaches the sources this page documents:

* **Nullability contracts.** 2 of 10 main sources in the scope of this page declare `@Nonnull` / `@Nullable`. Subjects in the range that name this area: `45c4b6b7 Add nullability annotations to event APIs`.
* **No other change in this range touches the area this page covers.**

> [!NOTE]
> Reading the commit list: `EventDispatcher` and `Listenable` are the only two annotated sources; nothing else in the range touches `io.microsphere.event`.

---

## 3. API reference

### 3.1 Type inventory

| Type | Kind | `@since` | Role |
|---|---|---|---|
| `Event` (`event/Event.java:29`) | abstract class, `extends java.util.EventObject` | 1.0.0 | carries a `final long timestamp` set to `System.currentTimeMillis()` in the ctor (`:36`, `:44-47`), exposed by `getTimestamp()` (`:49-51`) |
| `GenericEvent<S>` (`event/GenericEvent.java:42`) | concrete class | 1.0.0 | typed source accessor: `public S getSource()` casts `super.getSource()` (`:48-50`) |
| `EventListener<E extends Event>` (`event/EventListener.java:72`) | `@FunctionalInterface`, extends `java.util.EventListener` + `Prioritized` | 1.0.0 | `void onEvent(E event)` (`:205`) + default `int getPriority()` returning `NORMAL_PRIORITY` (`:214-216`) |
| `ConditionalEventListener<E extends Event>` (`event/ConditionalEventListener.java:47`) | interface | 1.0.0 | adds `boolean accept(E event)` (`:55`) — per-listener veto |
| `GenericEventListener` (`event/GenericEventListener.java:80`) | abstract class, `implements EventListener<Event>` | 1.0.0 | one listener, many handler methods dispatched by the event's runtime class |
| `Listenable<E extends EventListener<?>>` (`event/Listenable.java:49`) | interface | 1.0.0 | the register / remove / query contract, incl. `assertListener` (`:80-90`) |
| `EventDispatcher` (`event/EventDispatcher.java:58`) | interface, `extends Listenable<EventListener<?>>` | 1.0.0 | `dispatch(Event)` (`:102`), `DIRECT_EXECUTOR` (`:63`), factories (`:70-95`), default `getExecutor()` (`:111-113`) |
| `AbstractEventDispatcher` (`event/AbstractEventDispatcher.java:93`) | abstract class | 1.0.0 | the registry, the cache, the matching loop and the SPI bootstrap |
| `DirectEventDispatcher` (`event/DirectEventDispatcher.java:56`) | `final` class | 1.0.0 | `super(DIRECT_EXECUTOR)` (`:58-60`) — synchronous, sequential |
| `ParallelEventDispatcher` (`event/ParallelEventDispatcher.java:33`) | class | 1.0.0 | no-arg ctor uses `ForkJoinPool.commonPool()` (`:35-37`); `ParallelEventDispatcher(Executor)` (`:39-41`) |

There is **no** `CancelDispatcherOperationException`, no `EventDispatcher` builder and no `@interface` in this package.
The `Listenable` javadoc still advertises a final-class rejection (`event/Listenable.java:67-75`), but the check is
commented out (`event/Listenable.java:83-89`) — see §6.

### 3.2 Registration, removal, query

| Signature | Declared at | Null / duplicate behaviour |
|---|---|---|
| `void addEventListener(E listener) throws NullPointerException, IllegalArgumentException` | `event/Listenable.java:100`; impl `event/AbstractEventDispatcher.java:116-121` | `null` → `IllegalArgumentException` (not NPE) from `assertListener` → `Assert.assertNotNull` (`event/Listenable.java:81`, `util/Assert.java:149-153`); duplicate is dropped by `addIfAbsent` (`event/AbstractEventDispatcher.java:119`, `:155-159`) |
| `default void addEventListeners(E listener, E... others)` | `event/Listenable.java:110-116` | concatenates then delegates to the `Iterable` overload |
| `default void addEventListeners(Iterable<E> listeners)` | `event/Listenable.java:125-127` | one `addEventListener` call per element |
| `void removeEventListener(E listener)` | `event/Listenable.java:137`; impl `event/AbstractEventDispatcher.java:124-127` | `listeners.remove(listener)` — **`equals()`-based** |
| `default void removeEventListeners(Iterable<E> listeners)` | `event/Listenable.java:148-150` | idem, per element |
| `default void removeAllEventListeners()` | `event/Listenable.java:157-159` | `@return a amount of removed listeners` in the javadoc, but the method is `void` |
| `@Nonnull List<E> getAllEventListeners()` | `event/Listenable.java:167-168`; impl `event/AbstractEventDispatcher.java:130-139` | `@Immutable`; de-duplicated, priority-sorted snapshot wrapped by `Collections.unmodifiableList` |

### 3.3 Dispatch and execution strategy

| Signature | Declared at | Behaviour |
|---|---|---|
| `void dispatch(Event event)` | `event/EventDispatcher.java:102`; impl `event/AbstractEventDispatcher.java:162-180` | submits **one** task to the dispatcher's `Executor`, which then walks the matching listeners sequentially |
| `@Nonnull static EventDispatcher newDefault()` | `event/EventDispatcher.java:70-73` | `new DirectEventDispatcher()` |
| `@Nonnull static EventDispatcher parallel(@Nonnull Executor executor)` | `event/EventDispatcher.java:81-84` | `new ParallelEventDispatcher(executor)`; the `@Nonnull` annotation is declarative — there is no runtime null check in the factory |
| `@Nonnull static EventDispatcher of(@Nullable Executor executor)` | `event/EventDispatcher.java:92-95` | `executor == null ? newDefault() : parallel(executor)` — the documented null-tolerant entry point |
| `Executor DIRECT_EXECUTOR = Runnable::run` | `event/EventDispatcher.java:63` | runs the task on the calling thread |
| `default Executor getExecutor()` | `event/EventDispatcher.java:111-113` | `DIRECT_EXECUTOR` for ad-hoc `EventDispatcher` implementations |
| `public final Executor getExecutor()` | `event/AbstractEventDispatcher.java:186-188` | `final` — a subclass of `AbstractEventDispatcher` **cannot** change its executor; the ctor value is fixed (`:109-113`) |
| `protected AbstractEventDispatcher(Executor executor)` | `event/AbstractEventDispatcher.java:109-113` | `null` → `IllegalArgumentException` (`:110`, `util/Assert.java:149-153`) — proven by `event/AbstractEventDispatcherTest.java:36-40` |

### 3.4 Listener-matching rules

A dispatch delivers an event to a listener **only if both** rules hold:

| # | Rule | Where | Notes |
|---|---|---|---|
| 1 | **Registration key**: the listener is stored under the single `Class<? extends Event>` resolved from its generic parameter | `event/AbstractEventDispatcher.java:190-192` (`doInListener` → `findEventType(listener)`), keys at `:99` | `findEventType` returns `null` for unresolvable listeners and the listener is then **silently not registered** (`event/AbstractEventDispatcher.java:194-195`) — the degradation policy, not a throw |
| 2 | **Delivery test**: `entry.getKey().isAssignableFrom(event.getClass())` | `event/AbstractEventDispatcher.java:168` | so a listener registered for `Event` receives every subclass; a listener registered for `EchoEvent` does **not** receive a plain `Event` — `event/DirectEventDispatcherTest.java:111-124` |
| 3 | **Veto** (only for `ConditionalEventListener`) | `event/AbstractEventDispatcher.java:170-175` | `accept(...)` false ⇒ that one listener is skipped, the rest still run |

`findEventType` resolution order (`event/EventListener.java:131-150`):

| Case | Mechanism | Where |
|---|---|---|
| Lambda / anonymous functional form | `LambdaUtils.resolveLambdaMethodParameterTypes(listenerClass, EventListener.class)`; a single resolved parameter type wins | `event/EventListener.java:135-138` |
| Normal class | `TypeUtils.getAllParameterizedTypes(...)` walked, first non-null `ParameterizedType` match wins, else recurse into the superclass | `event/EventListener.java:141-146` |
| `ParameterizedType` inspection | raw type assignable to `EventListener`, first actual type argument assignable to `Event` wins | `event/EventListener.java:182-198` |
| `null` / non-listener class | returns `null` (`isAssignableFrom` guard at `:134`) | proven by `event/EventListenerTest.java:58-61` |

### 3.5 Conditional filtering and handler methods — no annotations exist

> [!IMPORTANT]
> `io.microsphere.event` declares **zero** annotation types — there is no `@interface` anywhere in the package's 10
> main-source files, so no per-annotation condition table can be written and nothing here carries the `@Since`
> annotation. `@since` appears only as a Javadoc tag, and every one of the 10 types is tagged `1.0.0`
> (`event/Event.java:27`, `event/GenericEvent.java:40`, `event/EventListener.java:69`,
> `event/ConditionalEventListener.java:45`, `event/GenericEventListener.java:78`, `event/Listenable.java:47`,
> `event/EventDispatcher.java:56`, `event/AbstractEventDispatcher.java:91`, `event/DirectEventDispatcher.java:54`,
> `event/ParallelEventDispatcher.java:31`). Conditional filtering is expressed with interfaces and method signatures
> instead:

| Mechanism | Signature | Semantics |
|---|---|---|
| `ConditionalEventListener` | `boolean accept(E event)` (`event/ConditionalEventListener.java:55`) | called **immediately before** `onEvent` for each matching listener, in priority order (`event/AbstractEventDispatcher.java:170-177`); `false` skips that listener only |
| `GenericEventListener` | `public final void onEvent(Event event)` (`event/GenericEventListener.java:106`) | fans out to the handler methods collected in the ctor (`:86-89`, `:95-104`) |

A subclass method qualifies as a handler only if **all five** hold (`event/GenericEventListener.java:128-151`):

| # | Criterion | Line |
|---|---|---|
| 1 | not the `onEvent(Event)` method itself | `:130` |
| 2 | returns `void` | `:134` |
| 3 | declares no exception types | `:138-141` |
| 4 | exactly one parameter | `:144-146` |
| 5 | that parameter is `Event` or a subclass | `:150` |

`public` accessibility is enforced *implicitly*: the candidate pool is `getClass().getMethods()`
(`event/GenericEventListener.java:98`), which yields public members only — `protected void notEvent(EchoEvent)` in
`event/GenericEventListenerTest.java:73-75` is therefore never collected. Method **names do not matter**
(`event/GenericEventListenerTest.java:65`). Dispatch into that map is by **exact runtime class**, not assignability:
`handleEventMethods.getOrDefault(event.getClass(), emptySet())` (`event/GenericEventListener.java:108`).

### 3.6 Priority ordering, "cancel" semantics and propagation

`EventListener` is `Prioritized` (`event/EventListener.java:72`), and `Prioritized.compareTo` compares the raw
`getPriority()` ints ascending (`lang/Prioritized.java:103-105`), so **the smaller number runs first**:

| Constant | Value | Meaning | Line |
|---|---|---|---|
| `MAX_PRIORITY` | `Integer.MIN_VALUE` | highest priority (runs first) | `lang/Prioritized.java:81` |
| `NORMAL_PRIORITY` | `0` | default for a listener that does not override `getPriority()` | `lang/Prioritized.java:91`, `event/EventListener.java:214-216` |
| `MIN_PRIORITY` | `Integer.MAX_VALUE` | lowest priority (runs last) | `lang/Prioritized.java:86` |

Sorting happens twice: once inside the registry after every mutation (`Collections.sort(listeners)`,
`event/AbstractEventDispatcher.java:201`) and once per dispatch (`Stream.sorted()`, `:152`, which relies on the
`Comparable` implemented at `lang/Prioritized.java:103-105`). SPI-loaded listeners are sorted before registration too
(`event/AbstractEventDispatcher.java:215-217`), consistent with the repository-wide rule of never trusting
`ServiceLoader` file order.

**Cancellation — state exactly what exists:** there is none.

* No `CancelDispatcherOperationException` or any other cancel/abort type exists in the `release` worktree (10 types in
  `event/*.java`, all listed in §3.1).
* A listener **cannot stop propagation**. `onEvent` returns `void` (`event/EventListener.java:205`) and the dispatch
  loop ignores every return value and never breaks (`event/AbstractEventDispatcher.java:168-178`).
* The two real short-circuits are:
  1. `accept(...) == false` skips exactly one listener — `return;` inside the `forEach` at
     `event/AbstractEventDispatcher.java:172-174`;
  2. a listener **throwing** aborts the remaining listeners of that dispatch, because `listener.onEvent(event)` at
     `event/AbstractEventDispatcher.java:177` is not wrapped in any `try`/`catch`. With `DirectEventDispatcher` the
     exception leaves `dispatch()` on the caller's thread (`Runnable::run`, `event/EventDispatcher.java:63`); with a
     real `Executor` it is thrown on the worker thread and cannot be caught by the caller.
* `StandardFileWatchService.stop()` cancelling its event loop (`io/StandardFileWatchService.java:192`) is
  `Future.cancel` on the watch loop — unrelated to this package's listener contract.

---

## 4. Examples mined from the tests

### 4.1 Two listeners, one event type each, priority decides the order

`event/DirectEventDispatcherTest.java:63-80` — `EchoEventListener` listens for `EchoEvent`; `EchoEventListener2`
listens for `Event` and returns priority `-1` (`event/EchoEventListener2.java:43-46`), so it sorts first:

```java
dispatcher.addEventListener(echoEventListener);
dispatcher.addEventListener(echoEventListener2);
assertEquals(ofList(echoEventListener2, echoEventListener), dispatcher.getAllEventListeners()); // :68

dispatcher.addEventListener(echoEventListener);           // duplicate add
assertEquals(ofList(echoEventListener2, echoEventListener), dispatcher.getAllEventListeners()); // :71-72

dispatcher.removeEventListener(echoEventListener);
assertEquals(ofList((EventListener) echoEventListener2), dispatcher.getAllEventListeners());     // :75-76
```

### 4.2 Matching is by assignability, and an unmatched dispatch is a no-op

`event/DirectEventDispatcherTest.java:108-124`:

```java
dispatcher.addEventListener(echoEventListener);            // registered for EchoEvent
dispatcher.dispatch(new Event("Test") {});                 // :114-115
assertEquals(0, echoEventListener.getEventOccurs());       // :118  no listener ran

dispatcher.dispatch(new EchoEvent("Hello,World"));         // :121
assertEquals(1, echoEventListener.getEventOccurs());       // :124
```

The same test (`:133-145`) shows the `Event`-typed listener receiving both the plain `Event` and the `EchoEvent`,
i.e. one dispatch can reach listeners registered under several keys.

### 4.3 Conditional filtering

`event/ConditionalEventListenerTest.java:39-76` — `accept` compares the source, `onEvent` records it:

```java
OnlyHelloWorldEventListener listener = new OnlyHelloWorldEventListener();
eventDispatcher.addEventListener(listener);                    // :44

eventDispatcher.dispatch(new EchoEvent("1"));                  // :46
assertNull(listener.getSource());                               // :48  accept() returned false

eventDispatcher.dispatch(new EchoEvent("Hello,World"));        // :50
assertEquals("Hello,World", listener.getSource());             // :52

static class OnlyHelloWorldEventListener implements ConditionalEventListener<EchoEvent> {
    public boolean accept(EchoEvent event) { return "Hello,World".equals(event.getSource()); } // :64-66
    public void onEvent(EchoEvent event) { source = (String) event.getSource(); }               // :69-71
}
```

### 4.4 A generic listener with several handler methods

`event/GenericEventListenerTest.java:48-83` — note the handler named `event(...)`, the two-parameter overload and the
`protected` ones:

```java
class MyGenericEventListener extends GenericEventListener {
    public void onEvent(EchoEvent echoEvent) { this.echoEvent = echoEvent; }          // :61-63  collected
    public void event(EchoEvent echoEvent) { assertEquals("Hello,World", echoEvent.getSource()); } // :65-67  collected
    public void event(EchoEvent echoEvent, Object arg) { this.echoEvent = echoEvent; } // :69-71  rejected (2 params)
    protected void notEvent(EchoEvent echoEvent) { this.echoEvent = echoEvent; }       // :73-75  rejected (not public)
}

eventDispatcher.dispatch(new EchoEvent("Hello,World"));
assertEquals(echoEvent, listener.getEchoEvent());                                      // :52-54
```

### 4.5 Lambda listeners resolve their own event type

`event/DirectEventDispatcherTest.java:153-183`:

```java
EventListener<?> eventListener = event -> { eventHolder.setValue(event); counter.incrementAndGet(); };        // :159-162
EventListener<EchoEvent> echoEventEventListener = event -> { ... };                                           // :164-167

dispatcher.dispatch(new Event("Test") {});          // :175  only the Event-typed lambda fires
assertEquals(1, counter.get());                     // :177
dispatcher.dispatch(new EchoEvent("Hello,World"));  // :180  both fire
assertEquals(3, counter.get());                     // :182
```

`event/EventListenerTest.java:43-56` proves the same resolution for `Event.class`, `EchoEvent.class` and
`FileChangedEvent.class` lambda targets; `:34-41` proves it for the two `EchoEventListener` classes, including the
`Vector<EventListener<Event>>`-subclass case.

### 4.6 Null and failure paths

```java
assertThrows(IllegalArgumentException.class, () -> assertListener(null));           // event/ListenableTest.java:19
assertThrows(IllegalArgumentException.class, () -> new AbstractEventDispatcher(null) {}); // event/AbstractEventDispatcherTest.java:38
assertNull(findEventType((Class) null));                                            // event/EventListenerTest.java:60
assertDoesNotThrow(() -> dispatcher.doInListener((Class) null, eventListeners -> {})); // event/AbstractEventDispatcherTest.java:68-69
assertThrows(IllegalArgumentException.class, () -> new GenericEvent<>(null));        // event/GenericEventTest.java:46-48 (EventObject's own guard)
```

### 4.7 SPI-loaded listeners

`event/EventDispatcherTest.java:47-51` asserts `newDefault().getAllEventListeners().size() == 2` — those two come from
`src/test/resources/META-INF/services/io.microsphere.event.EventListener`, which lists `EchoEventListener` and
`EchoEventListener2`. `event/AbstractEventDispatcherTest.java:42-47` asserts the load produced a non-empty list, and
`:49-59` asserts a broken class loader degrades to a **no-op** rather than an exception (because
`loadEventListenerInstances()` swallows throwables into `logger.trace`, `event/AbstractEventDispatcher.java:215-218`).

### 4.8 Parallel dispatch needs an explicit wait

`event/ParallelEventDispatcherTest.java:46-52`:

```java
eventDispatcher.dispatch(new EchoEvent("Hello,World"));
ForkJoinPool.commonPool().awaitTermination(1, SECONDS);   // :49 — the test must give the worker time
assertEquals(1, listener.getEventOccurs());               // :51
```

---

## 5. Compatibility notes

* **Serialization**: `Event extends java.util.EventObject` (`event/Event.java:29`) and pins
  `serialVersionUID = -1704315605423947137L` (`:31`); the `timestamp` field is `final` and non-transient (`:36`), so it
  travels with the event. `EchoEventListener` is `Serializable` in the tests (`event/EchoEventListener.java:26`) but the
  library places no `Serializable` requirement on `EventListener`.
* **JDK 8 through 25**: the engine uses only `ServiceLoader`, `Executor`, `ConcurrentHashMap`, `LinkedList`, Streams and
  `java.lang.reflect`. Nothing reads JDK internals, so `--add-opens` is not required for this package. The lambda branch
  of `findEventType` (`event/EventListener.java:135-138`) does inspect the generated lambda class's declared methods via
  `LambdaUtils` (`lang/invoke/LambdaUtils.java:98-128`), which is a reflective read of a synthetic class — not a
  restricted `sun.*` access.
* **No Spring coupling**: no type in `event` references `org.springframework`. The `java8-16` profile's Spring version
  swap (`microsphere-java-parent/pom.xml:54-62`) has no effect on behaviour described here.
* **SPI files**: `core` ships 13 `META-INF/services` files on `release`, including
  `META-INF/services/io.microsphere.event.EventDispatcher` which lists `DirectEventDispatcher` and
  `ParallelEventDispatcher`. **No code in this repository loads `EventDispatcher` through `ServiceLoader`** — no
  `EventDispatcher.class` service lookup exists outside the class-identity assertion at
  `event/EventDispatcherTest.java:44`; the file is published for downstream consumers. There is no
  `META-INF/services/io.microsphere.event.EventListener` in main resources — only in test resources (§4.7).
* **Logging dependency direction**: `AbstractEventDispatcher` holds a `Logger`
  (`event/AbstractEventDispatcher.java:21`, `:39`, `:97`) taken from `io.microsphere.logging.LoggerFactory`, so `event`
  sits *above* `logging` in the layering; `logging` itself does not import `event`.

---

## 6. Pitfalls and known limits

1. **The registry is only half thread-safe.** `listenersCache` is a `ConcurrentHashMap`
   (`event/AbstractEventDispatcher.java:99`, created at `collection/MapUtils.java:705-707`), but **its values are plain
   `LinkedList`s** (`event/AbstractEventDispatcher.java:197`, `collection/ListUtils.java:448-450`). Mutations hold
   `synchronized (mutex)` (`:196-202`); the dispatch read path (`:146-152`) takes **no lock at all**. Registering a
   listener while a dispatch is draining the listener stream is therefore unsafe, and there is no test covering it.
   Safe pattern: register everything during start-up, dispatch afterwards.
2. **A dispatch takes a snapshot.** `Stream.sorted()` (`event/AbstractEventDispatcher.java:152`) is a stateful operation
   that fully drains the upstream before `forEach` runs, so the listener set is fixed at the start of each dispatch. A
   listener that registers another listener does not affect the dispatch in progress — only the next one.
3. **Exceptions are not contained.** There is no `try`/`catch` around `listener.onEvent(event)`
   (`event/AbstractEventDispatcher.java:177`); one throwing listener cancels the remaining ones (§3.6). This is the only
   propagation-stop mechanism in the engine, and it is accidental rather than designed.
4. **Reflection inside `GenericEventListener` re-throws as `RuntimeException`.**
   `onEvent` runs each handler through `ThrowableConsumer.execute` (`event/GenericEventListener.java:109-111`), whose
   default handler is `throw new RuntimeException(failure)` (`lang/function/ThrowableConsumer.java:104-106`,
   `:151-153`) — so an `InvocationTargetException` from a handler surfaces wrapped, and, per pitfall 3, aborts the loop.
5. **Handler methods are matched by exact class.** `handleEventMethods.getOrDefault(event.getClass(), emptySet())`
   (`event/GenericEventListener.java:108`) does not walk superclasses: `public void onEvent(Event event)` in a
   `GenericEventListener` subclass will **not** fire for an `EchoEvent`, even though the dispatcher-level rule (§3.4
   rule 2) is assignability-based.
6. **An unresolvable listener is dropped silently.** If `findEventType` returns `null` — e.g. a raw
   `EventListener` with no generic information — `doInListener` skips the registration without any warning
   (`event/AbstractEventDispatcher.java:194-195`). The listener simply never fires. Log `getAllEventListeners()` if a
   listener appears dead.
7. **`assertListener` no longer rejects final classes.** The final-class check is commented out
   (`event/Listenable.java:83-89`) while the javadoc still claims it throws (`:67-75`). The only real rejection is
   `null` → `IllegalArgumentException` (`:81`), asserted by `event/ListenableTest.java:18-20`. Do not code against the
   javadoc here.
8. **`removeEventListener` depends on `equals`.** `listeners.remove(listener)`
   (`event/AbstractEventDispatcher.java:126`) and the `contains` guard in `addIfAbsent` (`:156`) use `equals`, so an
   `equals` override that ignores identity — `event/EchoEventListener2.java:52-60` compares only the class — makes two
   distinct instances interchangeable, and the second add is treated as a duplicate.
9. **"Parallel" means one task, not N tasks.** `dispatch` submits a **single** `Runnable` to the executor
   (`event/AbstractEventDispatcher.java:167`) and loops over listeners inside it, so `ParallelEventDispatcher`
   parallelises *across dispatch calls*, never *across listeners of one dispatch*. Listener order is still sequential,
   and `dispatch()` returns before any listener has run (§4.8).
10. **`EventDispatcher.parallel(...)` performs no null check** (`event/EventDispatcher.java:82-84`); the failure surfaces
    later in `AbstractEventDispatcher`'s ctor as `IllegalArgumentException` (`:110`). Prefer `of(@Nullable Executor)`
    (`:92-95`) when the executor may be absent — that is the documented degradation path, and
    `StandardFileWatchService` shows the intent by defaulting to `DIRECT_EXECUTOR`
    (`io/StandardFileWatchService.java:151-153`).
11. **`removeAllEventListeners()` returns nothing.** Declared `default void` even though the javadoc promises
    "a amount of removed listeners" (`event/Listenable.java:152-159`).
12. **No event hierarchy, no payload, no ordering guarantee across keys.** Listeners registered under different event
    classes are merged from a `ConcurrentHashMap` stream (`event/AbstractEventDispatcher.java:146-152`) and then sorted
    only by priority; equal-priority listeners across different keys have no deterministic relative order.

---

## 7. See also

* [Language abstractions](./language-abstractions.md) — `Prioritized` ordering arithmetic, `LambdaUtils`, the
  `Throwable*` helpers that wrap listener exceptions.
* [I/O, NIO, scanning and file filters](./io-and-scanning.md) — `io.microsphere.io.event` and
  `StandardFileWatchService`, the engine's only in-module consumer.
* [Logging](./logging.md) — the `Logger` that `AbstractEventDispatcher` uses to swallow SPI failures.
* [Collections and filters](./collections-and-filters.md) — `MapUtils.newConcurrentHashMap` and `ListUtils.newLinkedList`,
  the two collections the listener registry is built from.
* [Event dispatching — Developer Guide](../../developer-guide/microsphere-java-core/events.md) — dispatch loop, cache
  invalidation, design patterns, thread-safety analysis and testing strategy.
