# Configuration Property Metadata

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-java-core` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Package** | `io.microsphere.metadata` — 9 main-source types |
| **Model type** | `io.microsphere.beans.ConfigurationProperty` + nested `ConfigurationProperty.Metadata` |
| **JDK level** | Compiled at Java 8; CI-tested on JDK 8 / 11 / 17 / 21 / 25 |
| **Depends on `io.microsphere.util`** | `ServiceLoaderUtils.loadServicesList`, `ClassLoaderUtils.nullSafeClassLoader`, `ClassUtils.getTypeName`, `Assert.assertNotEmpty` / `assertNotNull` |
| **Other `core` packages used** | `io.microsphere.json` (`JSONUtils.readValues` / `append` / `appendName` / `writeBeanAsString`), `io.microsphere.beans`, `io.microsphere.collection`, `io.microsphere.constants` (`ResourceConstants`, `SymbolConstants`), `io.microsphere.io.IOUtils`, `io.microsphere.nio.charset.CharsetUtils`, `io.microsphere.logging`, `io.microsphere.lang.Prioritized`, `io.microsphere.lang.function.ThrowableSupplier`, `io.microsphere.annotation.*` (from `microsphere-java-annotations`) |
| **External dependencies** | none — the JSON reader/writer is the in-repo `io.microsphere.json` model, not Jackson or Gson |

Configuration metadata is the machine-readable description of the properties an application exposes: *name, type,
default value, whether it is required, a human description, and where it is declared*. Spring IDE reads an equivalent
document to offer autocompletion and documentation popups on `application.properties`; Microsphere ships the same idea
under `META-INF/microsphere/configuration-properties.json`, and the `io.microsphere.metadata` package is the runtime
counterpart of that file — it **reads** it, **loads** it from the classpath (possibly from many jars), and **generates**
it. The compile-time half lives in the sibling module
[`microsphere-annotation-processor`](../module-microsphere-annotation-processor.md), which is documented separately.

| Contract | Kind | One-line responsibility | Shipped implementations |
|---|---|---|---|
| `ConfigurationPropertyReader` | SPI interface | parse metadata content into `List<ConfigurationProperty>` | `DefaultConfigurationPropertyReader` (JSON) |
| `ConfigurationPropertyLoader` | SPI interface | obtain metadata from a source and merge everything found | `MetadataResourceConfigurationPropertyLoader`, `AdditionalMetadataResourceConfigurationPropertyLoader`, abstract `ClassPathResourceConfigurationPropertyLoader` |
| `ConfigurationPropertyGenerator` | SPI interface | render one `ConfigurationProperty` as JSON text | `DefaultConfigurationPropertyGenerator`, `ReflectiveConfigurationPropertyGenerator` |

---

## 1. Version information and compatibility range

All nine types in the package, and the `ConfigurationProperty` model they operate on, record Javadoc `@since 1.0.0`
(`metadata/ConfigurationPropertyReader.java:39`, `metadata/ConfigurationPropertyLoader.java:45`,
`metadata/ConfigurationPropertyGenerator.java:54`, `beans/ConfigurationProperty.java:61`), so the contract has been
stable across the whole `0.x` line.

| | |
|---|---|
| **GroupId** | `io.github.microsphere-projects` (`pom.xml:12`) |
| **Reactor version** | `${revision}` = `0.3.20-SNAPSHOT` (`pom.xml:54`); tag `0.3.20` is published |
| **BOM** | `microsphere-java-parent/pom.xml` imports `microsphere-all-bom:0.3.13` (`:22`, `:33-34`) |
| **Spring** | `spring-framework-bom` is imported (`microsphere-java-parent/pom.xml:40-46`); `7.0.9` by default (`:23`), `5.3.39` under profile `java8-16` activated by `<jdk>[1.8,17)</jdk>` (`:54-62`, activation `:57`) |
| **JDK matrix** | Temurin 8, 11, 17, 21, 25 (`.github/workflows/maven-build.yml:25`, `distribution: 'temurin'` `:33`) |

> [!NOTE]
> There is **no** `maven.compiler.*` or `<release>` property anywhere in this reactor. The bytecode level is inherited
> from the external parent `io.github.microsphere-projects:microsphere-build:0.3.16` (`pom.xml:6-10`). The package
> itself only uses JDK 8 APIs (`ClassLoader.getResourceAsStream`, `Enumeration<URL>`, `try`-with-resources), so it is
> usable from Java 8 upwards without a multi-release jar.

Spring is listed above for completeness only: nothing in `io.microsphere.metadata` imports Spring. The JSON document
format is defined by the in-repo `io.microsphere.json` model, which the reader and the generators call directly — see
[JSON support](./json.md) rather than assuming a third-party mapper.

Add the dependency (the annotation processor is the compile-time companion and is usually `provided`):

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version>
</dependency>

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-annotation-processor</artifactId>
    <version>0.3.20</version>
    <scope>provided</scope>
</dependency>
```


### 1.1 Release date and what changed in `0.3.20`

| Item | Value | Evidence |
|---|---|---|
| Documented line | `release`, `${revision}` = `0.3.20-SNAPSHOT` | root `pom.xml:54` |
| Newest published release | `0.3.20`, tagged **2026-10-16** | the `0.3.20` tag resolves to `chore: merge main into release [skip ci]`, committed `2026-10-16T03:43:20Z` |
| Previous release | `0.3.19`, tagged **2026-08-30** | the `0.3.19` tag, committed `2026-08-30T12:51:37Z` |
| Changelog source | `release-notes.md` at the repository root | The project publishes no `CHANGELOG.md`; the `v0.3.16`–`v0.3.20` blocks of `release-notes.md` read *Release notes generation failed* and carry raw commit lists, so every entry below is quoted as a commit subject and checked against the `release` sources. |

`0.3.20` adds no new API surface for this area — the `0.3.19...0.3.20` range is a hardening release. What it does change, and how far that reaches the sources this page documents:

* **Nullability contracts.** 4 of 9 main sources in the scope of this page declare `@Nonnull` / `@Nullable`. Subjects in the range that name this area: `2b0989d3 Allow nullable configuration descriptions` / `e3a84c77 Add nullability annotations to resolvers`.
* **No other change in this range touches the area this page covers.**

> [!NOTE]
> Reading the commit list: `2b0989d3 Allow nullable configuration descriptions` concerns the `description` member of `@ConfigurationProperty`, whose generating side lives in the annotation-processor module, so its effect is split between this page and that module's page. The range also carries `da6586f1 Assert resource processing does not throw`, but no test file on this line matches it, so it is left unattributed.

---

## 2. The property model: `ConfigurationProperty` and `Metadata`

The model lives in `io.microsphere.beans` (`beans/ConfigurationProperty.java:63`) because it is a JavaBean, but it is
the vocabulary of this package and is described here.

| Member | Declaration | Notes |
|---|---|---|
| `getName()` | `final String name`, `:68-69`, `:127` | immutable; asserted non-null in the constructor (`:120`) |
| `getType()` | `String type`, `:74-75`, `:132` | a *type name*, not a `Class` — see pitfall 8 |
| `setType(Class)` / `setType(String)` | `:136-139`, `:141-144` | the `Class` overload stores `ClassUtils.getTypeName(type)` |
| `getValue()` / `getDefaultValue()` | `Object`, `:80-81`, `:86-87` | `@Nullable`, untyped |
| `isRequired()` | `boolean required`, `:92`, `:164` | always serialised, never omitted |
| `getDescription()` | `String`, `:97-98`, `:101` | `@Nullable` |
| `getMetadata()` | `final Metadata metadata`, `:112-113`, `:173` | created eagerly in the constructor (`:123`) |
| `Metadata.getSources()` / `getTargets()` | `:258-263`, `:282-287` | lazily initialised `LinkedHashSet<String>(2)` — insertion-ordered and de-duplicated |
| `Metadata.getDeclaredClass()` / `getDeclaredField()` | `:294-295`, `:304-305` | `@Nullable`; the declaring field the IDE should jump to |

Two constructors exist: `ConfigurationProperty(String name)` defaults the type to `String`
(`beans/ConfigurationProperty.java:115-117`), and `ConfigurationProperty(String name, Class<?> type)` (`:119-124`).
`equals`/`hashCode` compare **all seven** members including `Metadata` (`:178-202`), which is what makes the
"merge across jars" question in section 4 observable: two properties with the same name but different descriptions are
*not* equal.

```java
ConfigurationProperty property = new ConfigurationProperty("server.port", Integer.class);
property.setValue(8080);
property.setDefaultValue(8080);
property.setRequired(true);
property.setDescription("The port number for the server");

ConfigurationProperty.Metadata metadata = property.getMetadata();
metadata.getSources().add("application.properties");
metadata.getTargets().add("server");
metadata.setDeclaredClass("com.example.ServerConfig");
metadata.setDeclaredField("port");
```

_Source: `microsphere-java-core/src/test/java/io/microsphere/JSONTestUtils.java:51-64`_

The canonical source of a property name is the annotation `io.microsphere.annotation.ConfigurationProperty`
(`microsphere-java-annotations/src/main/java/io/microsphere/annotation/ConfigurationProperty.java:60-88`), whose
`name`/`type`/`defaultValue`/`required`/`description` members map one-to-one onto the model, and whose three source
constants — `system-properties`, `environment-variables`, `application` (`:43`, `:48`, `:53`) — are the values you will
see in `Metadata.sources`.

---

## 3. Reading metadata: `ConfigurationPropertyReader`

`ConfigurationPropertyReader` (`metadata/ConfigurationPropertyReader.java:41`) extends `Prioritized` and exposes three
`read` overloads that cascade into one another:

| Method | Source | Delegates to |
|---|---|---|
| `List<ConfigurationProperty> read(InputStream)` | `:66-68` (default) | wraps in `InputStreamReader` with `CharsetUtils.DEFAULT_CHARSET` |
| `List<ConfigurationProperty> read(Reader)` | `:94-96` (default) | `IOUtils.copyToString(reader)` then `read(String)` |
| `List<ConfigurationProperty> read(String content)` | `:119` (abstract) | the actual parsing, format-specific |

All three are `@Nonnull @Immutable` and declare `throws Throwable` — a checked, deliberately untyped failure channel
(see pitfall 9).

`DefaultConfigurationPropertyReader` (`metadata/DefaultConfigurationPropertyReader.java:39`) is the only shipped
implementation and the only supported format. It parses the content as a JSON array of objects
(`:43` — `JSONUtils.readValues(content, List.class, Map.class)`) and maps six keys plus a nested `metadata` object per
entry (`:59-89`):

```java
@Override
public List<ConfigurationProperty> read(String content) throws Throwable {
    List<Map<String, Object>> configurationPropertiesMaps = readValues(content, List.class, Map.class);
    ...
    return unmodifiableList(configurationProperties);
}
```

Reading from a resource is the usual entry point:

```java
ClassLoader classLoader = getClass().getClassLoader();
try (InputStream inputStream = classLoader.getResourceAsStream(ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE)) {
    List<ConfigurationProperty> configurationProperties = new DefaultConfigurationPropertyReader().read(inputStream);
    assertEquals(2, configurationProperties.size());
}
```

_Source: `metadata/DefaultConfigurationPropertyReaderTest.java:47-55`_

The same reader pointed at the file the annotation processor produces yields the module's seven documented properties:

```java
private static final String JSON_RESOURCE_NAME = "META-INF/microsphere/configuration-properties.json";

ClassLoader classLoader = getClassLoader(getClass());
try (InputStream inputStream = classLoader.getResourceAsStream(JSON_RESOURCE_NAME)) {
    List<ConfigurationProperty> configurationProperties = reader.read(inputStream);
    assertEquals(7, configurationProperties.size());
}
```

_Source: `metadata/ConfigurationPropertiesJSONReaderTest.java:39`, `:48-55`_

> [!IMPORTANT]
> `DefaultConfigurationPropertyReader.getPriority()` returns `MIN_PRIORITY`
> (`metadata/DefaultConfigurationPropertyReader.java:54-57`), i.e. the *lowest* precedence — remember that
> `Prioritized.MIN_PRIORITY` is `Integer.MAX_VALUE` and `MAX_PRIORITY` is `Integer.MIN_VALUE`
> (`lang/Prioritized.java:86`, `:81`). Your own reader, with any smaller numeric priority, wins the sort.

---

## 4. Loading metadata: `ConfigurationPropertyLoader`

### 4.1 Which resources, and in what order

The two resource locations are constants, not conventions you have to remember:

| Constant | Value | Declared at |
|---|---|---|
| `ResourceConstants.MICROSPHERE_METADATA_RESOURCE` | `META-INF/microsphere/` | `constants/ResourceConstants.java:38` |
| `CONFIGURATION_PROPERTY_METADATA_RESOURCE` | `META-INF/microsphere/configuration-properties.json` | `:54` (file name `:43`) |
| `ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE` | `META-INF/microsphere/additional-configuration-properties.json` | `:60` (file name `:48`) |

`ConfigurationPropertyLoader.loadAll()` (`metadata/ConfigurationPropertyLoader.java:91-106`) is the aggregate entry
point. It discovers every registered loader through Java SPI —
`ServiceLoaderUtils.loadServicesList(ConfigurationPropertyLoader.class)` at `:93`, which reads
`META-INF/services/io.microsphere.metadata.ConfigurationPropertyLoader` and sorts the instances with
`Prioritized.COMPARATOR` (`util/ServiceLoaderUtils.java:890`) — then calls `load()` on each in that order and appends
the results into one unmodifiable list (`:94-105`). A loader that returns `null` or an empty list contributes nothing
(`isNotEmpty` guard at `:98`); a loader that throws is logged at `error` level and skipped (`:101-103`).

| Loader | Resource | Priority | Registered in `META-INF/services`? |
|---|---|---|---|
| `MetadataResourceConfigurationPropertyLoader` | `configuration-properties.json` | `MIN_PRIORITY` = `Integer.MAX_VALUE` (`:40-42`) | **no** (see the warning below) |
| `AdditionalMetadataResourceConfigurationPropertyLoader` | `additional-configuration-properties.json` | `MIN_PRIORITY + 9` (`:40-42`) — overflows, see pitfall 7 | yes |
| `ClassPathResourceConfigurationPropertyLoader` (abstract) | whatever the constructor names | inherited | no — extend it |
| test-only `DefaultConfigurationPropertyLoader` / `Empty…` / `Null…` / `Error…` | in-memory | default `NORMAL_PRIORITY` | test scope only |

### 4.2 Single-hit versus all-hit lookup

`ClassPathResourceConfigurationPropertyLoader` (`metadata/ClassPathResourceConfigurationPropertyLoader.java:47`) is the
base you extend. Its `load()` (`:80-92`) has exactly two behaviours, selected by the `loadedAll` flag:

* `loadedAll == false` → `classLoader.getResourceAsStream(resourceName)` (`:89`) — **the first copy on the classpath
  only**. Every other jar contributing the same path is silently ignored.
* `loadedAll == true` → `classLoader.getResources(resourceName)` (`:83`) and one `read` per URL (`:84-87`) — every
  jar's copy is parsed and appended.

Either way an unopenable stream is not an error: `load(list, streamSupplier)` (`:94-104`) logs at `trace` and returns
(`:96-101`). The class loader is never `null` — the constructor funnels it through
`ClassLoaderUtils.nullSafeClassLoader` (`:74`, implemented at `util/ClassLoaderUtils.java:218-220`), and a blank
resource name is rejected with `IllegalArgumentException` (`:72`).

```java
ClassPathResourceConfigurationPropertyLoader loader =
    new ClassPathResourceConfigurationPropertyLoader(ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE) {
    };
List<ConfigurationProperty> configurationProperties = loader.load();
assertEquals(2, configurationProperties.size());
```

_Source: `metadata/ClassPathResourceConfigurationPropertyLoaderTest.java:41-46`, `:93-96`_

The `loadedAll` variants — `(String, boolean)` and `(String, ClassLoader, boolean)` — are exercised by the same suite
(`:56-60`, `:76-85`), and both missing-resource cases assert an **empty list rather than an exception** (`:62-74`). The
reader is hard-wired inside the base class (`new DefaultConfigurationPropertyReader()`, `:76`), so a custom reader does
not reach this path automatically (pitfall 6).

Aggregation, in the shape the tests pin down:

```java
List<ConfigurationProperty> configurationProperties = loadAll();
assertEquals(3, configurationProperties.size());                     // 2 (additional) + 1 (test loader)
assertEquals(newConfigurationProperty(), configurationProperties.get(2));
```

_Source: `metadata/ConfigurationPropertyLoaderTest.java:44-49`_

> [!WARNING]
> Two loader implementations are effectively unavailable in `0.3.20` and you should not rely on them being invoked:
>
> 1. `MetadataResourceConfigurationPropertyLoader` is **not registered** in
>    `microsphere-java-core/src/main/resources/META-INF/services/io.microsphere.metadata.ConfigurationPropertyLoader`
>    — that file contains only `io.microsphere.metadata.AdditionalMetadataResourceConfigurationPropertyLoader`.
>    Consequently `loadAll()` never reads `META-INF/microsphere/configuration-properties.json` on its own; instantiate
>    the loader directly if you need it.
> 2. `loadAll()` documents "an empty list if no loaders are found" (`metadata/ConfigurationPropertyLoader.java:84`),
>    but the discovery call at `:93` sits **outside** the `try`/`catch` at `:96-103`, and `ServiceLoaderUtils` throws
>    `IllegalArgumentException` when no implementation is declared (`util/ServiceLoaderUtils.java:882-886`). If the
>    service file is stripped (shading, or a `module-info` migration), `loadAll()` throws instead of degrading.

---

## 5. Generating metadata: `ConfigurationPropertyGenerator`

`ConfigurationPropertyGenerator` (`metadata/ConfigurationPropertyGenerator.java:56`) is the smallest contract:
`String generate(ConfigurationProperty)` returning JSON text, `@Nonnull`, throwing
`IllegalArgumentException` on a `null` argument (`:66`). It is what the annotation processor calls to append
loader-supplied properties to the generated file
(`microsphere-annotation-processor/src/main/java/io/microsphere/annotation/processor/ConfigurationPropertyAnnotationProcessor.java:143-146`),
resolved with `loadFirstService(ConfigurationPropertyGenerator.class)`
(`ConfigurationPropertyJSONElementVisitor.java:65`).

| Implementation | Mechanism | `getPriority()` | Registered? |
|---|---|---|---|
| `DefaultConfigurationPropertyGenerator` | hand-built `StringBuilder`, `JSONUtils.append` | `MIN_PRIORITY` (`:161-164`) | yes — the only registration |
| `ReflectiveConfigurationPropertyGenerator` | `JSONUtils.writeBeanAsString(property)` (`:78`) | inherits `NORMAL_PRIORITY` (`lang/Prioritized.java:98-100`) | no |

Both reject `null` up front (`DefaultConfigurationPropertyGenerator.java:82`,
`ReflectiveConfigurationPropertyGenerator.java:77`).

```java
ConfigurationProperty configurationProperty = new ConfigurationProperty("server.port", Integer.class);
String json = generator.generate(configurationProperty);
JSONObject jsonObject = new JSONObject(json);
assertEquals("server.port", jsonObject.getString("name"));
assertEquals("java.lang.Integer", jsonObject.getString("type"));

configurationProperty.setValue(8080);
json = generator.generate(configurationProperty);
jsonObject = new JSONObject(json);
assertEquals(8080, jsonObject.getInt("value"));
```

_Source: `metadata/DefaultConfigurationPropertyGeneratorTest.java:59-72`_

The default generator emits `name` and `type` unconditionally, `required` always, and **omits** `value`,
`defaultValue`, `description` when they are `null` (`:95`, `:101`, `:110`); `metadata` is always emitted (`:116-117`),
and within it `sources`, `targets`, `declaredClass` and `declaredField` are each omitted when empty or `null`, with the
trailing comma trimmed (`:123-159`). An empty `Metadata` therefore renders as `{}`:

```java
Metadata metadata = new Metadata();
String json = generator.generate(metadata);
assertEquals("{}", json);
assertNull(jsonObject.opt("sources"));
assertNull(jsonObject.opt("targets"));
```

_Source: `metadata/DefaultConfigurationPropertyGeneratorTest.java:102-111`_

The reflective variant is the one to compare against when you need the bean's full property set, and it is the same
call the tests use to assert the round-trip shape:

```java
ConfigurationProperty configurationProperty = newConfigurationProperty();
String json = generator.generate(configurationProperty);   // writeBeanAsString(...)
assertConfigurationPropertyJSON(json);
```

_Source: `metadata/ReflectiveConfigurationPropertyGeneratorTest.java:46-51`_

> [!TIP]
> To confirm that the metadata you expect really reached the file, watch the build output: on the final round the
> processor prints the complete generated JSON through `printNote(...)`
> (`ConfigurationPropertyAnnotationProcessor.java:154`), loader-contributed entries included.

---

## 6. Common pitfalls

1. **A missing metadata resource is not an error.** `ClassPathResourceConfigurationPropertyLoader.load(...)`
   (`:94-104`) treats a `null` `InputStream` as "nothing to do", logs at `trace`, and returns an empty list. Nothing in
   the chain complains if you expected `configuration-properties.json` but never generated it. Assert on the *content*,
   or check that `microsphere-annotation-processor` is on the annotation-processor path
   (`META-INF/services/javax.annotation.processing.Processor`).
2. **Merging and de-duplication do not happen by property name.** `loadAll()` concatenates
   (`metadata/ConfigurationPropertyLoader.java:99`) and the base loader concatenates per URL (`:86`). The same property
   declared in two jars arrives twice. De-duplication only happens *inside* one property, where `sources` and `targets`
   are `LinkedHashSet`s (`beans/ConfigurationProperty.java:258-263`, `:282-287`). If you need "one entry per name", do
   it yourself — `ConfigurationProperty.equals` covers all seven fields (`:178-190`), so a `Set` will not collapse
   near-duplicates either.
3. **`loadedAll=false` is `classpath:`, `loadedAll=true` is `classpath*:`.** The all-hit branch uses
   `ClassLoader.getResources` (`:83`), which walks the parent class-loader chain and yields one URL per jar entry — the
   JDK equivalent of Spring's `classpath*:`. The single-hit branch uses `getResourceAsStream` (`:89`) and stops at the
   first winner, so with `configuration-properties.json` present in ten jars you read exactly one. Both built-in
   loaders use the single-hit constructors (`MetadataResourceConfigurationPropertyLoader.java:36`,
   `AdditionalMetadataResourceConfigurationPropertyLoader.java:36`), so neither merges across jars by default.
   `getResources` matches an **exact** resource path — no `**` patterns, no file-name globs, no directory scanning
   (that is `io.microsphere.io.scanner`, see [I/O and scanning](./io-and-scanning.md)).
4. **`null` and empty are tolerated; exceptions are swallowed — but not all of them.** `@Nullable load()`
   (`metadata/ConfigurationPropertyLoader.java:67-68`) explicitly permits `null`, and `loadAll()` filters it
   (`:98`). A throwing loader is caught as `Throwable` and logged (`:101-103`) — your metadata can silently shrink to
   an empty list with only one `error` line as evidence. The exception to the degradation policy is discovery itself:
   no service file ⇒ `IllegalArgumentException` escapes (see the warning in section 4.1).
5. **The reader is strict about the keys it assumes are present.** `readConfigurationProperty` unboxes
   `(Boolean) map.get("required")` (`metadata/DefaultConfigurationPropertyReader.java:64`), so a JSON entry without
   `required` raises `NullPointerException`; `initMetadata` dereferences the nested object without a null check
   (`:79-80`), so an entry without `metadata` also raises `NullPointerException`; and
   `configurationProperty.setType(type)` rejects a missing/empty `type` with `IllegalArgumentException`
   (`beans/ConfigurationProperty.java:141-144`). These contradict the package's silent-degradation policy and are
   tracked in the project's NPE-hardening audit.
6. **The reader SPI is registered but never consulted.**
   `META-INF/services/io.microsphere.metadata.ConfigurationPropertyReader` names `DefaultConfigurationPropertyReader`,
   yet nothing in the reactor calls `loadServicesList(ConfigurationPropertyReader.class)`; the loader base constructs
   the default reader directly (`metadata/ClassPathResourceConfigurationPropertyLoader.java:76`). To change the format,
   subclass the loader and use the package-visible `load(List, ThrowableSupplier)` hook (`:94`) yourself.
7. **`MIN_PRIORITY + 9` overflows.** `AdditionalMetadataResourceConfigurationPropertyLoader.getPriority()` returns
   `MIN_PRIORITY + 9` (`:41`) and `MIN_PRIORITY` is `Integer.MAX_VALUE` (`lang/Prioritized.java:86`), so the value wraps
   to `-2147483640` — near `MAX_PRIORITY`, i.e. the loader sorts **first**, not last. The accompanying test compares
   against the same overflowing expression (`AdditionalMetadataResourceConfigurationPropertyLoaderTest.java:51-54`), so
   the defect is invisible there. Order `loadAll()` output by your own explicit priorities.
8. **`type` is a string, and this package never resolves it.** `ConfigurationProperty.getType()` returns the recorded
   type name (`beans/ConfigurationProperty.java:132`); the reader passes the raw JSON string straight through
   (`metadata/DefaultConfigurationPropertyReader.java:61`, `:67`). Resolving `java.lang.String[]` into a `Class` and
   converting `defaultValue` into that type is the job of `io.microsphere.reflect` and `io.microsphere.convert`, which
   the JSON layer already uses underneath (`json/JSONUtils.java:63`, `:66-67`, `:1427-1442`). See
   [Type conversion](./type-conversion.md) and [Reflection and beans](./reflection-and-beans.md).
9. **`throws Throwable` is part of the contract.** Both reader methods (`metadata/ConfigurationPropertyReader.java:66`,
   `:94`, `:119`) and `ConfigurationPropertyLoader.load()` (`:68`) declare `throws Throwable`, so a `catch (Exception)`
   block does not satisfy the compiler. `loadAll()` is the only `throws`-free API (`:91`).
10. **Do not hand-edit the generated file.** `META-INF/microsphere/configuration-properties.json` is rewritten by the
    annotation processor on every compile
    (`ConfigurationPropertyAnnotationProcessor.java:149-156`); the seven entries currently shipped in
    `microsphere-java-core/src/main/resources/META-INF/microsphere/configuration-properties.json` are generated output.
    Extra properties belong in `additional-configuration-properties.json`, contributed by a `ConfigurationPropertyLoader`.

---

## 7. See also

* [Type conversion](./type-conversion.md) — turning `getType()` / `getDefaultValue()` into usable values
* [Reflection and beans](./reflection-and-beans.md) — `ConfigurationProperty` as a JavaBean, `BeanUtils.resolvePropertiesAsMap`
* [Utilities and constants](./util-and-constants.md) — `ServiceLoaderUtils`, `ClassLoaderUtils`, `ResourceConstants`
* [JSON support](./json.md) — the `io.microsphere.json` model behind the reader and both generators
* [Annotation processor — module page](../module-microsphere-annotation-processor.md) — the compile-time half that writes these files
* Developer-facing deep dive: [Configuration Property Metadata — Developer Guide](../../developer-guide/microsphere-java-core/configuration-metadata.md)
