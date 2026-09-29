# Microsphere Java Developer Handbook · Microsphere Java 开发者手册

> A bilingual developer handbook for the [`microsphere-java`](https://github.com/microsphere-projects/microsphere-java)
> foundational library — one page per topic, in English and in Chinese.
>
> `microsphere-java` 基础类库的双语开发者手册 —— 每个主题一页，提供英文与中文两个版本。

**Version covered / 适用版本**: `0.3.20-SNAPSHOT` (latest release / 最新发布版 `0.3.19`)
**Group ID**: `io.github.microsphere-projects` · **Package root / 包根**: `io.microsphere.*`
**Java level / Java 版本**: compiled at Java 8, tested on JDK 8 / 11 / 17 / 21 / 25

---

## Pages / 页面

| # | English | 中文 | Covers / 内容 |
|---|---------|------|---------------|
| 1 | [Getting Started](en/getting-started.md) | [快速开始](zh/getting-started.md) | Requirements, modules, Maven/Gradle coordinates, first call, building from source / 环境要求、模块选择、依赖引入、首次调用、源码构建 |
| 2 | [Annotations](en/annotations.md) | [注解](zh/annotations.md) | `microsphere-java-annotations`: `@Since`, `@Nullable`, `@Nonnull`, `@Immutable`, `@Experimental`, `@ThreadSafe`, `@ConfigurationProperty` |
| 3 | [Core Utilities](en/core-utilities.md) | [核心工具](zh/core-utilities.md) | `io.microsphere.util`, `constants`, `text`: strings, classes, class loaders, service loading, assertions, versions / 字符串、类、类加载器、服务加载、断言、版本 |
| 4 | [Collections and Filters](en/collections-and-filters.md) | [集合与过滤器](zh/collections-and-filters.md) | `io.microsphere.collection`, `io.microsphere.filter`: null-safe factories, immutable collections, predicates, file filters / 空安全工厂、不可变集合、断言器、文件过滤器 |
| 5 | [Language Abstractions](en/language-abstractions.md) | [语言抽象](zh/language-abstractions.md) | `io.microsphere.lang`, `lang.function`, `lang.invoke`, `invoke`: `Prioritized`, `Wrapper`, `Deprecation`, throwable-aware functional interfaces, method handles |
| 6 | [Reflection and Types](en/reflection-and-types.md) | [反射与类型](zh/reflection-and-types.md) | `io.microsphere.reflect`, `internal.reflect`, `beans`: methods, fields, constructors, generic type resolution, `JavaType`, bean introspection / 泛型解析与 JavaBean 内省 |
| 7 | [Type Conversion](en/type-conversion.md) | [类型转换](zh/type-conversion.md) | `io.microsphere.convert`, `convert.multiple`, `io.serializer`: the `Converter` SPI, multi-value conversion, binary serialization / 转换与序列化 SPI |
| 8 | [Event Dispatching](en/events.md) | [事件分发](zh/events.md) | `io.microsphere.event`: `Event`, `EventListener`, `EventDispatcher`, conditional and generic listeners, SPI auto-loading / 监听器与分发器 |
| 9 | [I/O and File Watching](en/io-and-file-watch.md) | [I/O 与文件监听](zh/io-and-file-watch.md) | `io.microsphere.io`, `io.event`, `io.scanner`, `nio`: stream helpers, file watch service, class/file/JAR scanning / 流工具、文件监听、classpath 扫描 |
| 10 | [Networking and URL Protocols](en/networking-and-url-protocols.md) | [网络与 URL 协议](zh/networking-and-url-protocols.md) | `io.microsphere.net`: `classpath:` and `console:` protocols, sub-protocol chaining, handler registration / 自定义协议与 handler 注册 |
| 11 | [Logging](en/logging.md) | [日志](zh/logging.md) | `io.microsphere.logging`: the `Logger` facade and the `LoggerFactory` SPI delegation chain / 日志门面与委托链 |
| 12 | [Concurrency, Processes and JMX](en/concurrency-process-jmx.md) | [并发、进程与 JMX](zh/concurrency-process-jmx.md) | `concurrent`, `process`, `management`, `security`: thread factories, executors, process ids and exit, MBean builders / 线程工厂、进程管理、MBean |
| 13 | [Class Loading and Artifacts](en/classloading-and-artifacts.md) | [类加载与构件](zh/classloading-and-artifacts.md) | `io.microsphere.classloading`: `Artifact` detection from classpath URLs, resolver SPI, banned-artifact enforcement / 构件探测与版本校验 |
| 14 | [JSON](en/json.md) | [JSON](zh/json.md) | `io.microsphere.json`: dependency-free `JSONObject` / `JSONArray` / `JSONTokener` plus `JSONUtils` binding helpers / 零依赖 JSON 实现 |
| 15 | [Configuration Property Metadata](en/configuration-metadata.md) | [配置属性元数据](zh/configuration-metadata.md) | `io.microsphere.metadata` + `@ConfigurationProperty`: the reader/loader/generator SPI chain / 读取、加载与生成链路 |
| 16 | [Annotation Processing](en/annotation-processing.md) | [注解处理](zh/annotation-processing.md) | `microsphere-annotation-processor`, `microsphere-lang-model`, `microsphere-jdk-tools`: compile-time metadata generation and building your own processor / 编译期元数据与自研处理器 |
| 17 | [Testing Support](en/testing.md) | [测试支持](zh/testing.md) | `microsphere-java-test`, `microsphere-annotation-test`: fixtures and the in-process annotation-processing harness / 测试夹具与注解处理测试框架 |
| 18 | [Reference](en/reference.md) | [参考手册](zh/reference.md) | SPI registry, system properties and tunables, resource paths, JDK compatibility matrix, BOM coordinates, troubleshooting / SPI 清单、系统属性、JDK 兼容矩阵、故障排查 |

---

## Reading paths / 阅读路径

**I just need helpers in my app / 只想在应用里用工具方法**
[Getting Started](en/getting-started.md) → [Core Utilities](en/core-utilities.md) →
[Reflection and Types](en/reflection-and-types.md) → [Type Conversion](en/type-conversion.md)
（中文：[快速开始](zh/getting-started.md) → [核心工具](zh/core-utilities.md) → [反射与类型](zh/reflection-and-types.md) → [类型转换](zh/type-conversion.md)）

**I want to decouple components with events / 想用事件解耦组件**
[Event Dispatching](en/events.md) → [Language Abstractions](en/language-abstractions.md) (`Prioritized`)
（中文：[事件分发](zh/events.md) → [语言抽象](zh/language-abstractions.md)）

**I want IDE-friendly configuration metadata / 想要 IDE 可识别的配置元数据**
[Annotations](en/annotations.md) → [Configuration Property Metadata](en/configuration-metadata.md) →
[Annotation Processing](en/annotation-processing.md)
（中文：[注解](zh/annotations.md) → [配置属性元数据](zh/configuration-metadata.md) → [注解处理](zh/annotation-processing.md)）

**I am writing a library on top of Microsphere / 在 Microsphere 之上开发自己的库**
[Reference](en/reference.md) → then the SPI section of any feature page — every subsystem (`Converter`,
`EventListener`, `LoggerFactory`, `ArtifactResourceResolver`, `ProcessIdResolver`,
`ConfigurationPropertyLoader`) is an SPI you can implement and register.
（中文：[参考手册](zh/reference.md) → 再看各主题页的 SPI 小节；每个子系统都是可实现、可注册的 SPI。）

---

## Conventions / 手册约定

* English and Chinese pages are separate files with **identical structure**: same section numbers, same
  tables, same code blocks. Chinese pages translate inline code comments; English pages keep them in English.
  中英文页面分文件存放，**章节编号、表格与代码块完全对应**；中文页会翻译代码内的注释。
* `public static` helper classes are non-instantiable (`public abstract class XxxUtils implements Utils` with a
  private constructor) and are written as `ClassName.method(...)`.
  工具类不可实例化（`public abstract class XxxUtils implements Utils` + private 构造器），手册中一律写作 `ClassName.method(...)`。
* **Signature notation / 签名简写**: `getType(Object obj | Class<?> type)` stands for two overloads that differ
  only by parameter type. Everything else — method names, generic parameters, return types, `static`/`default`/
  `final` modifiers, thrown exceptions — is copied from the source.
* Where a Javadoc in the source contradicts the code, the handbook documents the **code** and calls out the
  discrepancy in a `> [!WARNING]` note. Javadoc 与代码不一致时，以**代码**为准，并用 `> [!WARNING]` 标注。
* Every `> [!NOTE]` / `> [!TIP]` / `> [!IMPORTANT]` / `> [!WARNING]` callout marks behaviour that is not inferable
  from the class name: which methods are `final`, which caches never evict, which SPI file is actually read.
  提示块记录仅凭类名无法推断的行为：方法是否为 `final`、缓存是否会失效、实际读取的是哪个 SPI 文件。
* This handbook is written for **users of the library**. Repository internals (contributing workflow, CI
  publishing, code-style enforcement) are covered only where they change what you can rely on at runtime.
  本手册面向**库的使用者**；仓库内部事务（贡献流程、发布 CI、风格约束）仅在影响运行时可靠性时提及。

---

## Relationship to the user guide / 与 user-guide 的关系

[`docs/user-guide/`](../user-guide/README.md) is the original English, API-inventory-oriented guide (18 pages,
including a `reference` page that is linked but not yet written). This handbook is a standalone, task-oriented
reorganisation of the same ground, available in both languages; the two coexist and are not kept
line-for-line in sync.

[`docs/user-guide/`](../user-guide/README.md) 是最初的英文版 API 清单式指南（18 页，其中 `reference` 页在索引中被引用但尚未编写）。
本手册是对同样内容的独立、面向任务的重组，提供中英两版；两者并存，不逐行保持同步。
