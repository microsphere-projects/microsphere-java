# Logging

**Module**: `io.github.microsphere-projects:microsphere-java-core`
**Package**: `io.microsphere.logging`

A tiny facade that delegates to whatever logging implementation is actually on the classpath, chosen by SPI priority.
Its purpose is internal: `AbstractEventDispatcher`, `AbstractConverter` and the metadata loaders log through it, so
the library never forces SLF4J or Commons Logging on you.

---

## 1. Using the facade

```java
import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;

public class MyService {

    private static final Logger logger = LoggerFactory.getLogger(MyService.class);

    public void run() {
        logger.info("Starting with {} workers", 4);     // {} interpolation
        if (logger.isDebugEnabled()) {
            logger.debug("Detailed state: {}", state, optionalThrowable);
        }
    }
}
```

```java
public interface Logger {

    String getName();

    boolean isTraceEnabled();  void trace(String message);
    void trace(String format, Object... arguments);
    void trace(String message, Throwable throwable);

    boolean isDebugEnabled();  void debug(String message);
    void debug(String format, Object... arguments);
    void debug(String message, Throwable throwable);

    boolean isInfoEnabled();   void info(String message);
    void info(String format, Object... arguments);
    void info(String message, Throwable throwable);

    boolean isWarnEnabled();   void warn(String message);
    void warn(String format, Object... arguments);
    void warn(String message, Throwable throwable);

    boolean isErrorEnabled();  void error(String message);
    void error(String format, Object... arguments);
    void error(String message, Throwable throwable);
}
```

`LoggerFactory` is the entry point:

```java
public abstract class LoggerFactory implements Prioritized {

    @Nonnull public static Logger getLogger(Class<?> type)
    @Nonnull public static Logger getLogger(String name)

    public abstract Logger createLogger(String name);
    protected abstract String getDelegateLoggerClassName();
    protected boolean isAvailable()          // delegate class resolvable on the classpath
}
```

The two static methods resolve a single factory once (class-load) and reuse it for the lifetime of the class loader.
There is no per-call configuration and no re-selection if you change the classpath later.

Formatting is `{}`-style (`io.microsphere.text.FormatUtils` / the delegate's own formatter), **not** `%s`.

---

## 2. Which implementation gets picked

`LoggerFactory.getLogger` loads all `io.microsphere.logging.LoggerFactory` SPI entries, sorts them with
`Prioritized.COMPARATOR` (ascending `getPriority()`), and takes the **first one whose `isAvailable()` is true**.

Registered in `microsphere-java-core/src/main/resources/META-INF/services/io.microsphere.logging.LoggerFactory`:

| Order | Implementation | Priority | Delegate class it probes | Backing framework |
|---|---|---|---|---|
| 1 | `Sfl4jLoggerFactory` | `NORMAL_PRIORITY` (0) | `org.slf4j.Logger` | SLF4J |
| 2 | `ACLLoggerFactory` | `NORMAL_PRIORITY + 5` | `org.apache.commons.logging.Log` | Apache Commons Logging |
| 3 | `JDKLoggerFactory` | `NORMAL_PRIORITY + 10` | `java.util.logging.Logger` | java.util.logging (JUL) |
| 4 | `NoOpLoggerFactory` | `MIN_PRIORITY` (`Integer.MAX_VALUE`) | — | nothing: silent |

So the effective precedence is **SLF4J → Commons Logging → JUL → no-op**. `NoOpLoggerFactory` overrides
`isAvailable()` to return `true`, which is why it can be the guaranteed last resort; its
`getDelegateLoggerClassName()` throws `UnsupportedOperationException` and is never called.

Supporting types: `AbstractLogger` (base for the delegates), `NoOpLogger`, and `LoggerUtils` (helpers over the
facade).

> [!NOTE]
> `slf4j-api` and `commons-logging` are **`optional`** dependencies of `microsphere-java-core`. Adding SLF4J to your
> application is enough to route the library's logs into your existing configuration — you do not configure
> `io.microsphere` anywhere.

To see what the library itself decided, set the chosen backend's level for the framework loggers, e.g. with
Logback:

```xml
<logger name="io.microsphere" level="TRACE"/>
```

This matters for [Event Dispatching](events.md#7-spi-auto-loading-of-listeners), where SPI load failures are logged
at `trace` only.

---

## 3. Adding your own backend

Implement `LoggerFactory`, make the probe honest, and register it with a priority that slots between the built-ins.

```java
package com.example.logging;

import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;

public class ZelogLoggerFactory extends LoggerFactory {

    @Override
    protected String getDelegateLoggerClassName() {
        return "com.example.zelog.ZelogLogger";      // existence decides isAvailable()
    }

    @Override
    public Logger createLogger(String name) {
        return new ZelogLoggerAdapter(com.example.zelog.Zelog.getLogger(name));
    }

    @Override
    public int getPriority() {
        return Prioritized.NORMAL_PRIORITY - 1;       // beats SLF4J (0)
    }
}
```

```
# src/main/resources/META-INF/services/io.microsphere.logging.LoggerFactory
com.example.logging.ZelogLoggerFactory
```

Your `Logger` adapter can extend `io.microsphere.logging.AbstractLogger` to avoid re-implementing the level checks.

> [!WARNING]
> Because availability is judged by class resolution, a factory that is *registered but whose delegate is absent*
> must return `false` from `isAvailable()` — otherwise it is selected and every log call fails. Only override
> `isAvailable()` when the probe needs to be more than "does the class exist".

---

## 4. See also

* [Core Utilities](core-utilities.md#11-textformatutils) — `FormatUtils.format`, the `{}` engine
* [Language Abstractions](language-abstractions.md#1-prioritized--the-ordering-spine) — the priority scale above
* [Reference](reference.md#1-spi-registry) — all service files

[← Previous: Networking and URL Protocols](networking-and-url-protocols.md) · [Index](README.md) · [Next: Concurrency, Processes and JMX →](concurrency-process-jmx.md)
