# Microsphere Java User Guide

A complete, task-oriented guide to the [`microsphere-java`](https://github.com/microsphere-projects/microsphere-java)
foundational library: every module, the public API surface you can actually call, the SPI extension points,
and the runtime tunables.

All signatures, resource paths, system-property keys and service registrations in this guide were read from the
`0.3.20-SNAPSHOT` sources in this repository.

---

## Before you start

* **Version in this guide**: `0.3.20-SNAPSHOT` (latest published release: `0.3.19`).
* **Group ID for every artifact**: `io.github.microsphere-projects`.
* **Language level**: all modules compile at **Java 8** (`java.version=8` in the external `microsphere-build` parent),
  and are tested on JDK 8, 11, 17, 21 and 25.
* **Package root**: `io.microsphere.*`.

---

## Pages

| # | Page | Covers |
|---|------|--------|
| 1 | [Getting Started](getting-started.md) | Requirements, BOM import, Maven/Gradle coordinates, building from source, JDK matrix |
| 2 | [Annotations](annotations.md) | `microsphere-java-annotations`: `@Since`, `@Nullable`, `@Nonnull`, `@Immutable`, `@Experimental`, `@ThreadSafe`, `@ConfigurationProperty` |
| 3 | [Core Utilities](core-utilities.md) | `io.microsphere.util`, `constants`, `text`: strings, classes, class loaders, service loading, assertions, versions, stopwatch, JARs |
| 4 | [Collections and Filters](collections-and-filters.md) | `io.microsphere.collection`, `io.microsphere.filter`: null-safe factories, immutable collections, singletons, delegating types, predicates and file filters |
| 5 | [Language Abstractions](language-abstractions.md) | `io.microsphere.lang`, `lang.function`, `lang.invoke`, `invoke`: `Prioritized`, `Wrapper`, `Deprecation`, throwable-aware functional interfaces, method handles |
| 6 | [Reflection and Types](reflection-and-types.md) | `io.microsphere.reflect`, `internal.reflect`, `beans`: methods, fields, constructors, generic type resolution, `JavaType`, bean introspection |
| 7 | [Type Conversion](type-conversion.md) | `io.microsphere.convert`, `convert.multiple`, `io.serializer`: the `Converter` SPI, multi-value conversion, binary serialization SPI |
| 8 | [Event Dispatching](events.md) | `io.microsphere.event`: `Event`, `EventListener`, `EventDispatcher`, conditional and generic listeners, SPI auto-loading |
| 9 | [I/O, Files and Watching](io-and-file-watch.md) | `io.microsphere.io`, `io.event`, `io.scanner`, `nio`: stream helpers, file watch service, class/file/JAR scanning |
| 10 | [Networking and URL Protocols](networking-and-url-protocols.md) | `io.microsphere.net`: `classpath:` and `console:` protocols, sub-protocol chaining, handler registration |
| 11 | [Logging](logging.md) | `io.microsphere.logging`: the `Logger` facade and the `LoggerFactory` SPI delegation chain |
| 12 | [Concurrency, Processes and JMX](concurrency-process-jmx.md) | `concurrent`, `process`, `management`, `security`: thread factories, executors, process ids and exit, MBean builders |
| 13 | [Class Loading and Artifacts](classloading-and-artifacts.md) | `io.microsphere.classloading`: `Artifact` detection from classpath URLs, resolver SPI, banned-artifact enforcement |
| 14 | [JSON](json.md) | `io.microsphere.json`: dependency-free `JSONObject` / `JSONArray` / `JSONTokener` plus `JSONUtils` binding helpers |
| 15 | [Configuration Property Metadata](configuration-metadata.md) | `io.microsphere.metadata` + `beans.ConfigurationProperty`: the reader/loader/generator SPI chain |
| 16 | [Annotation Processing](annotation-processing.md) | `microsphere-annotation-processor`, `microsphere-lang-model`, `microsphere-jdk-tools`: compile-time metadata generation and how to build your own processor |
| 17 | [Testing Support](testing.md) | `microsphere-java-test`, `microsphere-annotation-test`: fixtures and the in-process annotation-processing harness |
| 18 | [Reference](reference.md) | Complete SPI registry table, system-property table, thread-safety matrix, design-pattern map, troubleshooting |

---

## Suggested reading paths

**I just need helpers in my app** → [Getting Started](getting-started.md) →
[Core Utilities](core-utilities.md) → [Reflection and Types](reflection-and-types.md) →
[Type Conversion](type-conversion.md)

**I want to decouple components with events** → [Event Dispatching](events.md) →
[Language Abstractions](language-abstractions.md) (`Prioritized`)

**I want IDE-friendly configuration metadata** → [Annotations](annotations.md) →
[Configuration Property Metadata](configuration-metadata.md) → [Annotation Processing](annotation-processing.md)

**I am writing a library on top of Microsphere** → [Reference](reference.md) → then the SPI section of any
feature page — every subsystem (`Converter`, `EventListener`, `LoggerFactory`, `ArtifactResourceResolver`,
`ProcessIdResolver`, `ConfigurationPropertyLoader`) is an SPI you can implement and register.

---

## Conventions used in this guide

* `public static` helper classes are non-instantiable (`abstract class X implements Utils` with a private
  constructor). The guide shows them as `ClassName.method(...)`.
* **Signature notation**: `getType(Object obj | Class<?> type)` is shorthand for two overloads that differ only by
  parameter type. Everything else — method names, generic parameters, return types, `static`/`default`/`final`
  modifiers, thrown exceptions — is copied from the source.
* Where a Javadoc in the source contradicts the code, this guide documents the **code** and calls out the
  discrepancy in a *Warning* note.
* Every `> [!NOTE]`-style callout marks behaviour that is not inferable from the class name
  (for example: which methods are `final`, which caches never evict, which SPI file name is actually read).
