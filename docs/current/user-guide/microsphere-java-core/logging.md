# Logging

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-java-core` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Package** | `io.microsphere.logging` — 9 top-level types (plus 3 nested adapter classes) |
| **Bytecode / JDK** | No `maven.compiler.*` or `<release>` property exists anywhere in the reactor; the level is inherited from the external build parent `microsphere-build:0.3.16`. CI-tested on Temurin JDK 8 / 11 / 17 / 21 / 25 |
| **Optional third-party APIs** | `org.slf4j:slf4j-api` (`microsphere-java-core/pom.xml:39-43`) and `commons-logging:commons-logging` (`:46-50`), both `<optional>true</optional>` at compile scope |
| **Depends on other `core` packages** | `io.microsphere.lang` (`Prioritized`), `io.microsphere.util` (`ClassUtils.getTypeName`, `StringUtils.isBlank` / `substringBefore`, `ArrayUtils.length`), `io.microsphere.collection` (`ListUtils.newLinkedList`), `io.microsphere.text` (`FormatUtils.format`), `io.microsphere.constants` (`SymbolConstants.DOLLAR`), `io.microsphere.annotation` (`@Nonnull` / `@Nullable`) |
| **Used by** | ~37 other `core` classes (e.g. `convert/AbstractConverter`, `reflect/MethodUtils`, `io/IOUtils`, `event/AbstractEventDispatcher`, `classloading/ArtifactDetector`) — `logging` is the library's only outlet to a logging framework |

All `file:line` references in this page are relative to `microsphere-java-core/src/main/java/io/microsphere/`
(main sources) or `microsphere-java-core/src/test/java/io/microsphere/` (tests), read from the `release` worktree.
After the first mention they are shortened to `logging/LoggerFactory.java:88-102`.

---

## 1. What this package group is for

**Reach for `io.microsphere.logging` when library code needs to emit logs without the artifact itself pulling a logging
framework onto the consumer's classpath.** The package is a thin, self-sufficient facade: one `Logger` interface
(`logging/Logger.java:62`), one abstract factory that resolves a backend through `ServiceLoader`
(`logging/LoggerFactory.java:65`), three adapters over the frameworks the library treats as optional
(SLF4J, Apache Commons Logging, `java.util.logging`), and one always-present no-op fallback
(`logging/NoOpLoggerFactory.java:39`). Nothing in `microsphere-java-core` imports `org.slf4j` or
`org.apache.commons.logging` outside those three adapters — which is precisely what lets `slf4j-api` and
`commons-logging` be declared `<optional>true</optional>` (`microsphere-java-core/pom.xml:39-43`, `:46-50`).

```java
Logger logger = LoggerFactory.getLogger(MyClass.class);      // logging/LoggerFactory.java:97-100
Logger named  = LoggerFactory.getLogger("io.microsphere.x"); // logging/LoggerFactory.java:108-111
logger.info("Started in {} ms", elapsed);                    // logging/Logger.java:164
// No per-class Logger field? LoggerUtils derives the name for you:
LoggerUtils.debug(logger -> logger.debug("Parsed {} members", n)); // logging/LoggerUtils.java:52-54
```

The design principle is **silent degradation with one documented exception**: an absent backend is a normal condition,
never an error. `getDelegateLoggerClass()` swallows `Throwable` from `ClassLoader.loadClass` and returns `null`
(`logging/LoggerFactory.java:127-136`), `isAvailable()` turns that into `false` (`:118-120`), the candidate list is
filtered rather than failed (`:78-83`), and `NoOpLoggerFactory` — always available (`:46-50`) — sits last so the survivor
list is never empty. The only deliberate throw is `NoOpLoggerFactory.getDelegateLoggerClassName()`'s
`UnsupportedOperationException` (`logging/NoOpLoggerFactory.java:41-44`); see §7.5.

> [!NOTE]
> This is a **facade, not a configuration system**: `LoggerFactory` never reads a logging properties file, never sets a
> level and never installs an appender — it only decides *which* backend you get. Configuration stays in SLF4J /
> Commons Logging / JUL.

---

## 2. Coordinates and dependency

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

No `<exclusions>` are needed and no logging framework must be added: with none on the classpath the facade silently
selects `java.util.logging`. To make the SLF4J branch selectable, bring in `slf4j-api` plus a binding yourself —
`microsphere-java-core` will not do it for you. The versions of `slf4j-api` and `commons-logging` used to *build* the
artifact are not pinned in this reactor; they come from the externally imported BOM
(`microsphere-java-parent/pom.xml:31-37`; `microsphere-all-bom` version `0.3.13` declared at `:22`).

| Fact | Value | Evidence (`release`) |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | root `pom.xml:12` |
| `artifactId` | `microsphere-java-core` | module `pom.xml:14` |
| Version property | `${revision}` | root `pom.xml:14`, `:53-55` |
| Value of `${revision}` on `release` | `0.3.20-SNAPSHOT` | root `pom.xml:54` |
| Published tag for this line | `0.3.20` | release line in use by the docs |
| Build parent | `microsphere-build:0.3.16` | root `pom.xml:6-10` |
| Java level | none declared in-reactor — inherited from the external parent `microsphere-build:0.3.16` | only `maven-compiler-plugin` appears in the reactor, at `microsphere-annotation-processor/pom.xml:98`, with no `maven.compiler.*` / `<release>` property anywhere |
| CI matrix | Temurin JDK `8 / 11 / 17 / 21 / 25` | `.github/workflows/maven-build.yml:25`, `distribution: 'temurin'` `:33`, build command `:38-42` |
| Spring Framework (build-time only; irrelevant at runtime to `logging`) | `7.0.9` default / `5.3.39` under profile `java8-16` (`<jdk>[1.8,17)</jdk>`) | `microsphere-java-parent/pom.xml:23`, BOM import `:40-46`, profile `:54-62`, activation `:57` |
| Imported BOMs | `microsphere-all-bom:0.3.13` + `spring-framework-bom` | `microsphere-java-parent/pom.xml:22`, `:33-36`, `:40-46` |

---

## 3. Version information and compatibility range

| Item | Status on `release` | Evidence |
|---|---|---|
| `@since` of every type in the package | `1.0.0` — the whole package is stable, no experimental markers | `logging/Logger.java:60`, `logging/AbstractLogger.java:61`, `logging/LoggerFactory.java:63`, `logging/Sfl4jLoggerFactory.java:42`, `logging/ACLLoggerFactory.java:46`, `logging/JDKLoggerFactory.java:52`, `logging/NoOpLoggerFactory.java:37`, `logging/LoggerUtils.java:34` |
| Supported JDK range | 8 → 25 (CI-proven); nothing in the package uses an API newer than Java 8 | `.github/workflows/maven-build.yml:25` |
| SLF4J | Any `org.slf4j.Logger`-bearing `slf4j-api` on the classpath satisfies the probe; the adapter calls only `isXxxEnabled()` and the `(String)` / `(String, Object[])` / `(String, Throwable)` overloads | probe `logging/Sfl4jLoggerFactory.java:46`, delegate calls `:72-170` |
| Commons Logging | Any `org.apache.commons.logging.Log` on the classpath; the adapter uses `LogFactory.getLog(String)` | probe `logging/ACLLoggerFactory.java:50`, `:71-74` |
| `java.util.logging` | Always present on the JVM; used whenever neither of the two above resolves | `logging/JDKLoggerFactory.java:56`, `:77-80` |
| No-op fallback | Always present (shipped inside this artifact), last resort | `logging/NoOpLoggerFactory.java:46-50`, `:56-59` |

> [!TIP]
> `logging` carries **no Spring and no JDK-version branch**: behaviour is identical under the `java8-16` (`5.3.39`) and
> default (`7.0.9`) profiles, which only switch a Spring property that this package never reads.


### 3.1 Release date and what changed in `0.3.20`

| Item | Value | Evidence |
|---|---|---|
| Documented line | `release`, `${revision}` = `0.3.20-SNAPSHOT` | root `pom.xml:54` |
| Newest published release | `0.3.20`, tagged **2026-10-16** | the `0.3.20` tag resolves to `chore: merge main into release [skip ci]`, committed `2026-10-16T03:43:20Z` |
| Previous release | `0.3.19`, tagged **2026-08-30** | the `0.3.19` tag, committed `2026-08-30T12:51:37Z` |
| Changelog source | `release-notes.md` at the repository root | The project publishes no `CHANGELOG.md`; the `v0.3.16`–`v0.3.20` blocks of `release-notes.md` read *Release notes generation failed* and carry raw commit lists, so every entry below is quoted as a commit subject and checked against the `release` sources. |

`0.3.20` adds no new API surface for this area — the `0.3.19...0.3.20` range is a hardening release. What it does change, and how far that reaches the sources this page documents:

* **Nullability contracts.** 1 of 9 main sources in the scope of this page declares `@Nonnull` / `@Nullable`. Subjects in the range that name this area: `a2824fff Add nullability annotations to logging APIs`.
* **Null-input and behaviour fixes.** `6f83fb21 Document no-op logger methods`.

> [!NOTE]
> Reading the commit list: The annotated source is `LoggerFactory` only; `AbstractLogger` and the adapters are untouched by the pass, which is consistent with the no-op-degradation contract documented on this page.

---

## 4. Obtaining a `Logger`

### 4.1 The two entry points

| Signature | Behaviour | Evidence |
|---|---|---|
| `static Logger getLogger(Class<?> type)` | `@Nonnull`; delegates to the name form using `type.getName()` | `logging/LoggerFactory.java:97-100` |
| `static Logger getLogger(String name)` | `@Nonnull`; calls `factory.createLogger(name)` on the single resolved factory | `logging/LoggerFactory.java:108-111` |
| `abstract Logger createLogger(String name)` | the per-backend factory method you override for a custom backend | `logging/LoggerFactory.java:152` |
| `static void LoggerUtils.trace/debug/info/warn/error(Consumer<Logger>)` | lazy one-shot logging; name comes from the caller's class | `logging/LoggerUtils.java:43-81` |

The factory is resolved **once**, in the `LoggerFactory` static initialiser, into a `private static final` field
(`logging/LoggerFactory.java:69-70`); every later `getLogger` call is a field read plus one `createLogger` invocation —
no re-probing, no locking.

> [!WARNING]
> **Loggers are not cached.** `getLogger(name)` returns a *new adapter instance* on every call
> (`logging/LoggerFactory.java:110` → `logging/Sfl4jLoggerFactory.java:54-56`, `logging/ACLLoggerFactory.java:58-60`,
> `logging/JDKLoggerFactory.java:64-66`), so two `getLogger(Foo.class)` calls produce objects that are not identical
> (`!=`) even though the SLF4J/JUL/ACL logger behind them is shared by that backend. Keep the result in a
> `static final` field; never call `getLogger` per statement, and never compare loggers with `==`.

### 4.2 Deriving the logger name from a lambda (`LoggerUtils`)

`LoggerUtils` has no per-class field to hold, so it reconstructs the caller's class name from the lambda's own class
name and cuts it at the first `$`: `getLoggerName` is
`substringBefore(getTypeName(loggerConsumer), DOLLAR)` (`logging/LoggerUtils.java:101-103`), and `log` only invokes the
consumer after the level guard passes (`logging/LoggerUtils.java:89-94`). `ClassUtils.getTypeName(Object)` returns
`value.getClass().getName()` (`util/ClassUtils.java:2025-2028`) and `StringUtils.substringBefore` cuts at
`SymbolConstants.DOLLAR` = `"$"` (`constants/SymbolConstants.java:228`, `util/StringUtils.java:472`), so a lambda
declared inside `com.acme.OrderService` yields the logger name `com.acme.OrderService`.

**Runnable example** — taken from the package's own test, which exercises every level plus the `Predicate` overload:

```java
import static io.microsphere.logging.LoggerUtils.*;

assertDoesNotThrow(() -> info(logger -> logger.info("Hello,World")));
assertDoesNotThrow(() -> info(logger -> logger.info("Hello,{}", "World")));
assertDoesNotThrow(() -> info(logger -> logger.info("{},{}", "Hello", "World")));
assertDoesNotThrow(() -> log(logger -> false, logger -> logger.info("Hello,World")));
```

_Source: logging/LoggerUtilsTest.java:55-58,76-78_

**Runnable example** — obtaining a logger by class and by name, then driving all five levels through three call shapes:

```java
Logger logger = getLogger(LoggerFactoryTest.class);   // logging/LoggerFactoryTest.java:52
log(logger, "Hello,World");                                        // String form
log(logger, "Hello,World {}", getInstance().get(YEAR));            // varargs form
log(logger, "Hello,World", new Throwable("Testing"));              // Throwable form
```

_Source: logging/LoggerFactoryTest.java:51-71_

---

## 5. The log-level API surface

`Logger` declares **21 methods**: one `getName()` plus, for each of the five levels `TRACE`, `DEBUG`, `INFO`, `WARN`,
`ERROR`, a guard and three emission shapes (`logging/Logger.java:69-239`).

| Level | Guard | Plain | Formatted | With exception |
|---|---|---|---|---|
| TRACE | `isTraceEnabled()` `Logger.java:77` | `trace(String)` `:84` | `trace(String, Object...)` `:98` | `trace(String, Throwable)` `:107` |
| DEBUG | `isDebugEnabled()` `:115` | `debug(String)` `:122` | `debug(String, Object...)` `:131` | `debug(String, Throwable)` `:140` |
| INFO | `isInfoEnabled()` `:148` | `info(String)` `:155` | `info(String, Object...)` `:164` | `info(String, Throwable)` `:173` |
| WARN | `isWarnEnabled()` `:181` | `warn(String)` `:188` | `warn(String, Object...)` `:197` | `warn(String, Throwable)` `:206` |
| ERROR | `isErrorEnabled()` `:214` | `error(String)` `:221` | `error(String, Object...)` `:230` | `error(String, Throwable)` `:239` |

### 5.1 Placeholder syntax is SLF4J-style `{}`, not `%s`

`AbstractLogger.resolveMessage` delegates to `FormatUtils.format`, whose `DEFAULT_PLACEHOLDER` is the literal two
characters `{}` (`text/FormatUtils.java:17`, `:38-40`). Substitution is positional and sequential: the first `{}` gets
`args[0]`, the second gets `args[1]`, and so on. Leftover placeholders stay in the output untouched
(`text/FormatUtils.java:73-80`), and each argument is stringified with `String.valueOf`, so `null` renders as `"null"`
rather than throwing.

| Call | Result | Why |
|---|---|---|
| `info("Hello, {}", "World")` | `Hello, World` | one placeholder, one argument |
| `info("{},{}", "Hello", "World")` | `Hello,World` | two placeholders consumed in order |
| `info("plain", "extra")` | `plain` | no placeholder → the loop breaks at `index == -1` (`text/FormatUtils.java:75-77`) |
| `info("a {} b {}", "x")` | `a x b {}` | more placeholders than arguments: the remainder is left as-is |

### 5.2 Who formats: the SLF4J adapter differs from the other two

- **`Sfl4jLogger` overrides every varargs method** and forwards straight to SLF4J
  (`logging/Sfl4jLoggerFactory.java:82-85`, `:102-105`, `:122-125`, `:142-145`, `:162-165`), so formatting happens lazily
  *inside* SLF4J and the `AbstractLogger` pipeline is bypassed. This is the package's most consequential asymmetry.
- **`ACLLogger` and `JDKLogger` do not override the varargs methods.** They inherit
  `AbstractLogger.trace/debug/info/warn/error(String, Object...)` (`logging/AbstractLogger.java:76-99`), which eagerly
  builds the message via `FormatUtils.format` before consulting the backend — even when that level is disabled.

```java
protected void log(Consumer<String> messageHandler, BiConsumer<String, Throwable> messageThrowableHandler,
                   String format, Object... arguments) {                       // logging/AbstractLogger.java:107
    final String message; int length = length(arguments);
    if (length < 1) { message = format; }                                      // :110-111
    else {
        message = resolveMessage(format, arguments);                           // :113
        Object lastArgument = arguments[length - 1];
        if (lastArgument instanceof Throwable)                                 // :115
            messageThrowableHandler.accept(message, (Throwable) lastArgument); // :116
    }
    messageHandler.accept(message);                                            // :119
}
```

> [!IMPORTANT]
> **`AbstractLogger.log` emits twice when the last argument is a `Throwable`** — it invokes the `(String, Throwable)`
> handler at `logging/AbstractLogger.java:116` and then falls through to the plain `(String)` handler at
> `logging/AbstractLogger.java:119`, with no `return` in between. Under the **Commons Logging** and **JUL** backends,
> `logger.error("boom {}", e)` therefore produces *two* log records for one call. The SLF4J backend does not show this
> because `Sfl4jLogger` overrides the varargs methods (`logging/Sfl4jLoggerFactory.java:122-125`). Do not rely on
> "one call = one record" when passing a trailing `Throwable` as a format argument; pass it through the explicit
> `error(String, Throwable)` overload instead. This also means a trailing `Throwable` that lines up with a `{}`
> placeholder gets `String.valueOf(...)`-stringified into the message text (see the `a {} b {}` row above), leaking its
> `toString()` into the message body.

### 5.3 `getName()`

`AbstractLogger` stores the name once in the constructor and locks the accessor `final`, so subclasses cannot change it
(`logging/AbstractLogger.java:65-74`). `NoOpLogger` normalises blank input — `super(isBlank(name) ? "" : name)`
(`logging/NoOpLogger.java:43-45`) — so `"test"` keeps its name while `""`, `SPACE` (`" "`) and `null` all resolve to `""`.

_Source: logging/NoOpLoggerFactoryTest.java:46-58_

> [!WARNING]
> The blank-name guard exists **only** in `NoOpLogger`. `LoggerFactory.getLogger(String)` performs no validation
> (`logging/LoggerFactory.java:108-111`) and forwards the value to the chosen adapter, which hands it to
> `java.util.logging.Logger.getLogger` (`logging/JDKLoggerFactory.java:79`), `LogFactory.getLog`
> (`logging/ACLLoggerFactory.java:73`) or `org.slf4j.LoggerFactory.getLogger` (`logging/Sfl4jLoggerFactory.java:69`).
> `getLogger(Class)` additionally dereferences its argument (`type.getName()`, `logging/LoggerFactory.java:99`), so a
> `null` `Class` is an immediate `NullPointerException` — a real deviation from silent degradation, recorded against the
> NPE-hardening audit.

**Runnable example** — the shared contract test every adapter must pass, driving all 21 methods:

```java
@Test
void testInfo() {
    assertTrue(logger.isInfoEnabled());
    assertDoesNotThrow(() -> {
        logger.info("test");
        logger.info("test", EMPTY_OBJECT_ARRAY);            // varargs, zero arguments
        logger.info("test : {}", "a");                      // formatted
        logger.info("test : {}", "a", new Throwable());     // formatted + trailing Throwable
        logger.info("test", new Throwable());               // explicit Throwable overload
    });
}
```

_Source: logging/AbstractLoggerTest.java:89-99_ (`trace`/`debug`/`warn`/`error` are the same shape at `:55-65`, `:71-82`,
`:106-116`, `:123-133`.)

---

## 6. How the backend is chosen

### 6.1 The delegation order

`LoggerFactory` is itself an SPI. The four shipped implementations are registered, in this order, in
`src/main/resources/META-INF/services/io.microsphere.logging.LoggerFactory:1-4`:
`Sfl4jLoggerFactory`, `ACLLoggerFactory`, `JDKLoggerFactory`, `NoOpLoggerFactory`.

Each declares the class name of the backend it wraps (`logging/Sfl4jLoggerFactory.java:46`,
`logging/ACLLoggerFactory.java:50`, `logging/JDKLoggerFactory.java:56`, and the always-available
`logging/NoOpLoggerFactory.java:46-50`), and each pins its rank through `io.microsphere.lang.Prioritized`:

| Factory | Delegate class probed | `getPriority()` | Numeric value | Effective rank |
|---|---|---|---|---|
| `Sfl4jLoggerFactory` | `org.slf4j.Logger` | `NORMAL_PRIORITY` (`logging/Sfl4jLoggerFactory.java:58-61`) | `0` | 1st |
| `ACLLoggerFactory` | `org.apache.commons.logging.Log` | `NORMAL_PRIORITY + 5` (`logging/ACLLoggerFactory.java:62-65`) | `5` | 2nd |
| `JDKLoggerFactory` | `java.util.logging.Logger` | `NORMAL_PRIORITY + 10` (`logging/JDKLoggerFactory.java:68-71`) | `10` | 3rd |
| `NoOpLoggerFactory` | none (overridden `isAvailable`) | `MIN_PRIORITY` (`logging/NoOpLoggerFactory.java:56-59`) | `Integer.MAX_VALUE` (`lang/Prioritized.java:86`) | 4th, always |

**The constants are inverted** relative to their names: `Prioritized.MAX_PRIORITY = Integer.MIN_VALUE`
(`lang/Prioritized.java:81`), `MIN_PRIORITY = Integer.MAX_VALUE` (`:86`), `NORMAL_PRIORITY = 0` (`:91`).
`compareTo` is a plain `compare(this.getPriority(), that.getPriority())` (`:102-105`) and `LoggerFactory.loadFactories()`
sorts with `Collections.sort(factories, COMPARATOR)` (`logging/LoggerFactory.java:85-89`), so **the smallest numeric
priority sorts first and wins**. "Minimum priority" therefore means "last resort" — which is exactly why
`NoOpLoggerFactory` returns `MIN_PRIORITY`.

### 6.2 The resolution algorithm, step by step

```java
private static final ClassLoader classLoader = LoggerFactory.class.getClassLoader();  // logging/LoggerFactory.java:67
private static final LoggerFactory factory = loadFactory();                           // :69-70
private static LoggerFactory loadFactory() {                                          // :72-76
    return loadAvailableFactories().get(0);
}
static List<LoggerFactory> loadAvailableFactories() {                                 // :78-83
    List<LoggerFactory> factories = loadFactories();
    factories.removeIf(LoggerFactory::isAvailable.negate());
    return factories;
}
static List<LoggerFactory> loadFactories() {                                          // :85-89
    LinkedList<LoggerFactory> factories = newLinkedList(load(LoggerFactory.class, classLoader));
    sort(factories, COMPARATOR);
    return factories;
}
protected boolean isAvailable() { return nonNull(getDelegateLoggerClass()); }         // :118-120
@Nullable private Class<?> getDelegateLoggerClass() {                                 // :127-136
    Class<?> c = null;
    try { c = classLoader.loadClass(getDelegateLoggerClassName()); }                  // :132
    catch (Throwable e) { /* deliberately empty */ }                                  // :131-134
    return c;
}
```

1. `ServiceLoader.load(LoggerFactory.class, classLoader)` instantiates every registered factory (`:86`); `classLoader` is
   the loader that loaded `LoggerFactory` itself (`:67`).
2. The list is sorted by `Prioritized.COMPARATOR` (`:87`, `lang/Prioritized.java:61-76`).
3. `removeIf(isAvailable.negate())` drops every factory whose delegate class cannot be loaded (`:78-83`).
4. `get(0)` takes the survivor with the smallest priority number (`:75`).

Availability is a **class-loading probe**, not an `instanceof` check or a static reference: `loadClass` succeeds when the
backend jar is present and throws `ClassNotFoundException` when it is not, and the `catch` block is intentionally empty
(`logging/LoggerFactory.java:131-134`) — which is what makes an absent SLF4J a non-event rather than a crash.

### 6.3 Worked outcomes

| Classpath | Winner | Resulting `Logger` implementation |
|---|---|---|
| `slf4j-api` + a binding present | `Sfl4jLoggerFactory` (priority `0`) | `Sfl4jLoggerFactory.Sfl4jLogger` (`logging/Sfl4jLoggerFactory.java:63`) |
| `slf4j-api` absent, `commons-logging` present | `ACLLoggerFactory` (`5`) | `ACLLoggerFactory.ACLLogger` (`logging/ACLLoggerFactory.java:67`) |
| Neither present | `JDKLoggerFactory` (`10`) | `JDKLoggerFactory.JDKLogger` (`logging/JDKLoggerFactory.java:73`) |
| `java.util.logging` unavailable (non-standard runtime) | `NoOpLoggerFactory` (`MAX_VALUE`) | `NoOpLogger` (`logging/NoOpLogger.java:41`) |

**Runnable example** — the test that proves the filtered-and-sorted list is usable end to end: it walks *every* factory
that survived the availability probe and pushes all three call shapes through the logger it creates.

```java
@Test
void testLoadAvailableFactories() {
    loadAvailableFactories().forEach(this::testLoggerFactory);   // logging/LoggerFactoryTest.java:57-59
}
private void testLoggerFactory(LoggerFactory loggerFactory) {
    Logger logger = loggerFactory.createLogger("io.microsphere");
    assertLevel(logger);                                          // all five isXxxEnabled() must be true
    log(logger, "Hello,World");                                   // :68
    log(logger, "Hello,World {}", getInstance().get(YEAR));       // :69
    log(logger, "Hello,World", new Throwable("Testing"));         // :70
}
```

_Source: logging/LoggerFactoryTest.java:56-71_

> [!NOTE]
> `loadAvailableFactories()` and `loadFactories()` are **package-private on purpose** (`logging/LoggerFactory.java:78`,
> `:85`): they are the inspection seam that lets a test in `io.microsphere.logging` enumerate the surviving candidates
> instead of only seeing the winner. Outside that package, observe the choice via `getLogger(...).getClass()`.

---

## 7. Forcing and observing a backend in tests

There is no setter and no system property to switch the backend: resolution happens once in a static initialiser
(`logging/LoggerFactory.java:69-70`) and the field is `final`. The repository uses the techniques below instead.

### 7.1 Register a factory that must be filtered out

`NoDelegateLoggerFactory` is a test-only `LoggerFactory` whose delegate class name can never resolve
(`return "class-not-found"`, `logging/NoDelegateLoggerFactory.java:29-32`; `createLogger` returns `null`, `:34-37`). It is
registered in the *test* SPI file
`src/test/resources/META-INF/services/io.microsphere.logging.LoggerFactory:1` alongside the four main entries, so
`loadClass("class-not-found")` throws, `isAvailable()` is not overridden, and `removeIf` silently drops it
(`logging/LoggerFactory.java:78-83`) — the negative case of the probe without touching the classpath.

### 7.2 Reach a non-winning factory explicitly

To exercise the loser branches, take the factory out of the loaded list rather than out of `getLogger` and scan for its
type:

```java
List<LoggerFactory> factories = loadAvailableFactories();
for (LoggerFactory f : factories) {
    if (f instanceof NoOpLoggerFactory) { factory = (NoOpLoggerFactory) f; }
}
```

_Source: logging/NoOpLoggerFactoryTest.java:25-33_

Constructing the factory directly works just as well and is what the per-adapter tests do
(`new ACLLoggerFactory().createLogger(...)` at `logging/ACLLoggerFactoryTest.java:13-15`,
`new JDKLoggerFactory().createLogger(...)` at `logging/JDKLoggerFactoryTest.java:28-30`,
`new Sfl4jLoggerFactory().createLogger(...)` at `logging/Sfl4jLoggerFactoryTest.java:13-15`).

### 7.3 Force the JUL backend to a level the assertions require

`JDKLogger.isTraceEnabled()` maps TRACE onto `Level.ALL` (`logging/JDKLoggerFactory.java:82-85`, import at `:23`), and
`AbstractLoggerTest` asserts it returns `true` (`logging/AbstractLoggerTest.java:50-53`). The default JUL configuration
does not open TRACE, so the tests install their own configuration before anything runs:

```java
@BeforeAll
static void beforeAll() throws IOException {
    URL resource = LoggerFactoryTest.class.getResource("/META-INF/logging.properties");
    try (InputStream inputStream = resource.openStream()) {
        LogManager.getLogManager().readConfiguration(inputStream);
    }
}
```

_Source: logging/LoggerFactoryTest.java:42-48 (same block at `logging/JDKLoggerFactoryTest.java:19-25`)_

The file it loads is `handlers = java.util.logging.ConsoleHandler` plus `.level = ALL`
(`microsphere-java-core/src/test/resources/META-INF/logging.properties:1-3`).

> [!WARNING]
> `LogManager.readConfiguration` mutates the **whole JVM**, not the test class. Both loader methods are `@BeforeAll`, and
> `ACLLoggerFactoryTest` (`logging/ACLLoggerFactoryTest.java:10-15`) asserts `isTraceEnabled()` for a Commons-Logging
> logger *without* installing a configuration of its own. Whether that assertion holds can therefore depend on whether
> another test class already ran. Treat `META-INF/logging.properties` as global test state, and add the `@BeforeAll`
> block to any new test that depends on JUL levels.

### 7.4 Observe the SLF4J backend's output

`microsphere-java-core` pulls `ch.qos.logback:logback-classic` at test scope (`microsphere-java-core/pom.xml:81-85`) and
`microsphere-java-test` at test scope (`:66-71`). The latter ships `logback-test.xml`, which leaves root at `INFO` on
stdout but routes `io.microsphere` to a file appender at `TRACE` with additivity off
(`microsphere-java-test/src/main/resources/logback-test.xml:24-30`):

```xml
<logger name="io.microsphere" level="TRACE" additivity="false">
    <appender-ref ref="ASYNC"/>
</logger>
```

That is why `isTraceEnabled()` is `true` for the SLF4J adapter: every test logger is named `io.microsphere.logging.*` —
derived from the class under test (`logging/Sfl4jLoggerFactoryTest.java:13-15`) or from the explicit `"io.microsphere"`
argument (`logging/LoggerFactoryTest.java:62`) — so it falls under that `TRACE` logger. The suite asserts *enabledness*
and *non-throwing* behaviour rather than output text (`logging/AbstractLoggerTest.java:45-133`).

### 7.5 What "should never throw" means in practice

Every adapter test funnels through `assertDoesNotThrow`, including the awkward shapes — zero-varargs, a formatted string
with a trailing `Throwable`, and the explicit `Throwable` overload. The only test asserting a *throw* targets the one
documented exception to silent degradation:
`assertThrows(UnsupportedOperationException.class, factory::getDelegateLoggerClassName)` followed by
`assertTrue(factory.isAvailable())`.

_Source: logging/NoOpLoggerFactoryTest.java:35-43_

> [!TIP]
> `getDelegateLoggerClassName()` is only *reachable* as a throw because `isAvailable()` is overridden to `true`
> (`logging/NoOpLoggerFactory.java:46-50`), short-circuiting the base call to `getDelegateLoggerClass()`
> (`logging/LoggerFactory.java:118-120`). Remove that override and the `UnsupportedOperationException` fires inside the
> static initialiser, taking the whole `LoggerFactory` class down with an `ExceptionInInitializerError`.

---

## 8. Common pitfalls

- **Do not add a hard dependency on SLF4J just to use the facade.** `slf4j-api` is `<optional>`
  (`microsphere-java-core/pom.xml:39-43`). With no framework at all `LoggerFactory` still works and silently lands on JUL;
  adding `slf4j-api` only changes which row of the §6.3 table you get.
- **A custom factory must override `getPriority()`.** `LoggerFactory` implements `Prioritized` but never overrides
  `getPriority()`, so your subclass inherits the interface default `NORMAL_PRIORITY` = `0` (`lang/Prioritized.java:98-100`)
  — a tie with `Sfl4jLoggerFactory` (`logging/Sfl4jLoggerFactory.java:58-61`). The sort is stable
  (`logging/LoggerFactory.java:87`), so the tie breaks on service-file classpath order and your factory can silently win
  or lose between builds. Pick a deliberate value (`NORMAL_PRIORITY - 1` beats SLF4J). `Prioritized.COMPARATOR` also
  returns `-1` for any `Prioritized`-vs-non-`Prioritized` pair (`lang/Prioritized.java:61-76`), so it is not a
  general-purpose ordering.
- **Extension loading uses the *defining* classloader, not the thread context one.**
  `load(LoggerFactory.class, classLoader)` reads `LoggerFactory.class.getClassLoader()` (`logging/LoggerFactory.java:67`,
  `:86`). A factory visible only to a webapp/TCCL loader is not discovered, and unlike sibling SPI utilities
  `LoggerFactory` does not route through `ClassLoaderUtils.getClassLoader(...)` / `nullSafeClassLoader`
  (`util/ClassLoaderUtils.java:298-303`) — a `null` (bootstrap) loader would be passed straight to `ServiceLoader`.
- **Bootstrap is not failure-tolerant, and the winner pick is unguarded.** The `ServiceLoader` iteration is eager
  (`newLinkedList(load(...))`, `logging/LoggerFactory.java:86`) and runs inside a static initialiser (`:69-70`), so a
  malformed service entry — missing class, non-public constructor, abstract implementation — surfaces as
  `ServiceConfigurationError` → `ExceptionInInitializerError`, after which **every** `getLogger` call fails with
  `NoClassDefFoundError`. Likewise `availableFactories.get(0)` (`:75`) has no empty-list guard: it is safe only because
  `NoOpLoggerFactory` is always available, so dropping that registration from
  `META-INF/services/io.microsphere.logging.LoggerFactory` yields `IndexOutOfBoundsException` at class initialisation.
  Both are deviations from silent degradation: the per-factory probe is guarded (`:131-134`), the load and the pick are not.
- **`error(format, args…, throwable)` double-logs under ACL and JUL** — see §5.2 (`logging/AbstractLogger.java:116` then
  `:119`, no `return`). Use the `error(String, Throwable)` overload for exception-only logging.
- **Formatting cost is not deferred under ACL/JUL.** Both inherit `AbstractLogger`'s varargs methods, so
  `FormatUtils.format` runs before the backend is consulted (`logging/AbstractLogger.java:113`,
  `text/FormatUtils.java:62-82`). On a hot DEBUG/TRACE path guard explicitly with `isDebugEnabled()`, or route the call
  through `LoggerUtils` (`logging/LoggerUtils.java:52-54`), which tests the predicate first (`:89-94`).
- **`NoOpLogger` reports every level enabled** (`logging/NoOpLogger.java:47-50`, `:60-63`, `:73-76`, `:86-89`, `:99-102`)
  while discarding everything, so `if (logger.isInfoEnabled()) logger.info(buildExpensiveString());` still pays for
  `buildExpensiveString()`. `isXxxEnabled()` is not a proxy for "output goes somewhere".
- **JUL TRACE maps to `Level.ALL`, not a dedicated level** (`logging/JDKLoggerFactory.java:82-85`): `isTraceEnabled()` is
  `true` whenever the JUL level is `ALL` or lower, and `trace(...)` records at `ALL` (`:88-90`, `:93-95`).
- **`LoggerUtils` names the logger after the lambda's *defining* class, not the class that logs.** A lambda written in
  `com.acme.Bar` but handed to `LoggerUtils` by `com.acme.Foo` becomes `com.acme.Bar`, because the cut is on
  `loggerConsumer.getClass().getName()` (`logging/LoggerUtils.java:101-103`); a method reference takes the declaring
  class of the target method, and anonymous inner classes collapse (`Foo$1` → `Foo`).
- **`LoggerUtils` omits the `util.Utils` marker** (`logging/LoggerUtils.java:36`) that the other `core` `*Utils` classes
  carry (`util/Utils.java:41`); it is `abstract` with a private constructor (`logging/LoggerUtils.java:105-106`), so any
  convention-based utility scan misses it.
- **Do not hold a `Logger` across a classloader boundary.** Loggers are uncached per-call objects (§4.1) wrapping a
  backend logger from that backend's own registry (`logging/Sfl4jLoggerFactory.java:69`,
  `logging/ACLLoggerFactory.java:73`, `logging/JDKLoggerFactory.java:79`), while the probing loader is fixed at
  `logging/LoggerFactory.java:67`; an isolated/reloaded loader resolves `LoggerFactory` afresh for its own copy.

---

## 9. See also

- [Language abstractions](./language-abstractions.md) — `Prioritized`, whose inverted constants and `COMPARATOR` define the
  backend order (§6.1).
- [Utilities, constants and text](./util-and-constants.md) — `ClassUtils.getTypeName`, `StringUtils.substringBefore` /
  `isBlank`, `FormatUtils.format` and `SymbolConstants.DOLLAR`, the foundation `LoggerUtils` and `AbstractLogger` sit on.
- [Collections and filters](./collections-and-filters.md) — `ListUtils.newLinkedList`, the eager-consumption step of the SPI
  load (`logging/LoggerFactory.java:86`).
- [Type conversion](./type-conversion.md) — the sibling SPI with the same `Prioritized` + cached-list selection shape, and
  one of the ~37 in-library consumers of `Logger`.
- [Events](./events.md) · [Reflection, types and beans](./reflection-and-beans.md) ·
  [I/O, NIO, scanning and file filters](./io-and-scanning.md) ·
  [Class loading and artifacts](./classloading-and-artifacts.md) · [JSON](./json.md) ·
  [Configuration property metadata](./configuration-metadata.md) ·
  [Concurrency, process and management](./concurrency-process-and-management.md) — the other `core` groups that log through
  this facade instead of importing a framework.
- [Logging — Developer Guide](../../developer-guide/microsphere-java-core/logging.md) — the resolution chain, adapter
  internals, caching/thread-safety analysis, design patterns and the diagrams.
