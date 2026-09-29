# Reference

> Read this page in: [中文](../zh/reference.md) · [English](reference.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| SPI files in `microsphere-java-core` | 13 service files, 78 implementation lines (77 unique classes) |
| SPI files in `microsphere-annotation-processor` | 1 — `javax.annotation.processing.Processor` |
| `microsphere.*` system properties | 10 keys, all read once in a `static` initializer |
| Microsphere resource root | `META-INF/microsphere/` |
| CI-tested JDKs | 8, 11, 17, 21, 25 (`temurin`, `ubuntu-latest`) |
| Managed artifacts in the BOM | 7 modules, packaging `jar`, plus 2 `pom` artifacts |

> [!IMPORTANT]
> Every `microsphere.*` tunable is captured into a `public static final` field when its owner class is
> initialized (`System.getProperty` / `getInteger` in a static context). Setting it later with
> `System.setProperty` has no effect on that JVM — pass it as `-D` on the command line, or set it before
> the owning class is first touched.

---

## 1. SPI registry

All rows are the contents of
`microsphere-java-core/src/main/resources/META-INF/services/`, verified file by file.

| Interface (FQN) | Registered implementations | How it is loaded | Notes |
|---|---|---|---|
| `io.microsphere.classloading.ArtifactResourceResolver` | `MavenArtifactResourceResolver`, `ManifestArtifactResourceResolver`, `ArchiveFileArtifactResourceResolver` | `ArtifactDetector` → `loadServicesList(ArtifactResourceResolver.class, classLoader, true)` | Always cached. Priorities 1 / 5 / 9 (`DEFAULT_PRIORITY` constants); detection stops at the first resolver that returns a non-null `Artifact` |
| `io.microsphere.classloading.URLClassPathHandle` | `ClassicURLClassPathHandle`, `ModernURLClassPathHandle`, `NoOpURLClassPathHandle` | `ServiceLoadingURLClassPathHandle` → `loadServicesList(URLClassPathHandle.class)` | Cache follows `microsphere.service-loader.cached`. `Classic`/`Modern` share priority `MAX_PRIORITY + 99999`; `NoOp` uses the interface default `MIN_PRIORITY` so it sorts last and only wins when `supports()` of the others is `false` |
| `io.microsphere.convert.Converter` | 35 declared lines / 34 unique classes (`StringTo*`, `ObjectTo*`, `NumberTo*`, `ByteArrayToObjectConverter`, `MapToPropertiesConverter`, `PropertiesToStringConverter`, `StringToDurationConverter`, `StringToClassConverter`, `StringToInputStreamConverter`, `ObjectToOptionalConverter`) | `Converters.loadConvertersList()` → `loadServicesList(Converter.class, classLoader, true)` | Always cached, then indexed by `(sourceType, targetType)` in `Converters.convertersCache`. Note the file lists `io.microsphere.convert.ObjectToOptionalConverter` twice — harmless, the second entry is a duplicate of the first |
| `io.microsphere.convert.multiple.MultiValueConverter` | `StringToArrayConverter`, `StringToBlockingDequeConverter`, `StringToBlockingQueueConverter`, `StringToCollectionConverter`, `StringToDequeConverter`, `StringToListConverter`, `StringToNavigableSetConverter`, `StringToQueueConverter`, `StringToSetConverter`, `StringToSortedSetConverter`, `StringToTransferQueueConverter` (11) | `MultiValueConverter` static lookup → `loadServicesList(MultiValueConverter.class, classLoader)` | Cache follows `microsphere.service-loader.cached` |
| `io.microsphere.event.EventDispatcher` | `DirectEventDispatcher`, `ParallelEventDispatcher` | Registered as a discovery point; nothing in core loads `EventDispatcher.class` through the service loader | `EventDispatcher.newDefault()` constructs `DirectEventDispatcher` directly (`Runnable::run` executor); `EventDispatcher.parallel(executor)` constructs `ParallelEventDispatcher`. No-arg `ParallelEventDispatcher` uses `ForkJoinPool.commonPool()` |
| `io.microsphere.io.serializer.Serializer` | `BooleanSerializer`, `ByteSerializer`, `CharacterSerializer`, `ShortSerializer`, `IntegerSerializer`, `LongSerializer`, `FloatSerializer`, `DoubleSerializer`, `StringSerializer`, `DefaultSerializer` (10) | `Serializers` → `loadServicesList(Serializer.class, classLoader, true)` | Always cached. The eight primitive types extend `AbstractSerializer<T>`, which implements `Serializer<T>` **and** `Deserializer<T>` |
| `io.microsphere.io.serializer.Deserializer` | the same 8 `AbstractSerializer` subclasses, plus `StringDeserializer`, `DefaultDeserializer` (10) | `Deserializers` → `loadServicesList(Deserializer.class, classLoader)` | Cache follows `microsphere.service-loader.cached` |
| `io.microsphere.logging.LoggerFactory` | `Sfl4jLoggerFactory`, `ACLLoggerFactory`, `JDKLoggerFactory`, `NoOpLoggerFactory` | `java.util.ServiceLoader.load(LoggerFactory.class, classLoader)` inside `LoggerFactory.loadFactories()`, then `sort(factories, COMPARATOR)`, then `removeIf(!isAvailable())`, then `get(0)` | Loaded by the JDK loader, **not** by `ServiceLoaderUtils`, so the caching flag does not apply. Availability = the delegate logger class resolves (`org.slf4j.Logger` → `org.apache.commons.logging.Log` → `java.util.logging.Logger`); `NoOpLoggerFactory.isAvailable()` is hard-coded `true`, so the chain always terminates |
| `io.microsphere.metadata.ConfigurationPropertyGenerator` | `DefaultConfigurationPropertyGenerator` | `ConfigurationPropertyJSONElementVisitor` / the annotation processor | Turns a `ConfigurationProperty` model into the JSON fragment written to metadata |
| `io.microsphere.metadata.ConfigurationPropertyLoader` | `AdditionalMetadataResourceConfigurationPropertyLoader` | `ConfigurationPropertyLoader.loadAll()` → `loadServicesList(ConfigurationPropertyLoader.class)` | Each loader's `load()` is wrapped in `try/catch (Throwable)`; failures are logged as `error` and skipped. `MetadataResourceConfigurationPropertyLoader` (reads `configuration-properties.json`) exists but is **not** registered here — register it yourself if you want runtime loading of the generated file |
| `io.microsphere.metadata.ConfigurationPropertyReader` | `DefaultConfigurationPropertyReader` | `ClassPathResourceConfigurationPropertyLoader` instantiates `DefaultConfigurationPropertyReader` directly | Parses JSON into `io.microsphere.beans.ConfigurationProperty`; priority `MIN_PRIORITY` |
| `io.microsphere.net.ExtendableProtocolURLStreamHandler` | `io.microsphere.net.classpath.Handler` (`classpath:`), `io.microsphere.net.console.Handler` (`console:`) | `ServiceLoaderURLStreamHandlerFactory.loadHandlers()` → `loadServicesList(ExtendableProtocolURLStreamHandler.class)`, indexed by protocol | Construction enforces conventions: top-level class, simple name exactly `Handler`, package not under `sun.net.www.protocol`. The package prefix is appended to `java.protocol.handler.pkgs` |
| `io.microsphere.process.ProcessIdResolver` | `ModernProcessIdResolver`, `VirtualMachineProcessIdResolver`, `ClassicProcessIdResolver` | `ManagementUtils` → `loadServicesList(ProcessIdResolver.class)`, `.filter(supports()).findFirst()` | Cache follows `microsphere.service-loader.cached`. Priorities 1 / 5 / 9 (`NORMAL_PRIORITY + n`), i.e. Modern first, Classic last; falls back to `UNKNOWN_PROCESS_ID` (`-1`) |

### Ordering semantics (`io.microsphere.lang.Prioritized`)

`ServiceLoaderUtils.loadServicesAsList(...)` sorts with `Prioritized.COMPARATOR`:
`Prioritized` instances compare by `getPriority()` ascending and always sort **before**
non-`Prioritized` instances.

| Constant | Numeric value | Effect |
|---|---|---|
| `Prioritized.MAX_PRIORITY` | `Integer.MIN_VALUE` | Sorted first — the name means "highest precedence", the value is the smallest int |
| `Prioritized.NORMAL_PRIORITY` | `0` | Default for `getPriority()` |
| `Prioritized.MIN_PRIORITY` | `Integer.MAX_VALUE` | Sorted last — typical for a no-op fallback (`NoOpLoggerFactory` uses it) |

> [!WARNING]
> `ServiceLoaderUtils.loadServicesAsList` throws `IllegalArgumentException` when a service file yields no
> providers. A service with "zero implementations" is a hard failure, not an empty list.

---

## 2. Registering your own SPI implementation

1. Implement the service interface on the classpath. If the interface extends `Prioritized`
   (`LoggerFactory`, `ArtifactResourceResolver`, `URLClassPathHandle`, `ProcessIdResolver`,
   `ConfigurationPropertyLoader`, `ExtendableProtocolURLStreamHandler`), override `getPriority()`.
2. Provide a public no-arg constructor — `java.util.ServiceLoader` requires it.
3. Create the file `src/main/resources/META-INF/services/<fully-qualified-interface-name>` and write one
   implementation FQN per line. Comments (`#`) and blank lines are ignored by the JDK loader.
4. To override a built-in implementation, ship your own copy of the same service file: every file named
   `META-INF/services/<type>` on the classpath is read, and `loadFirstService(...)` picks the winner after
   priority sorting.

```
src/main/resources/META-INF/services/io.microsphere.classloading.ArtifactResourceResolver
```

```
com.acme.AcmecArtifactResourceResolver
```

Ordering example — a resolver that must run before `MavenArtifactResourceResolver` (priority `1`):

```java
public class AcmecArtifactResourceResolver extends AbstractArtifactResourceResolver {

    public AcmecArtifactResourceResolver() {
        super(0); // lower int than Maven's DEFAULT_PRIORITY = 1 => evaluated first
    }

    @Override
    public Artifact resolve(URL resourceURL) {
        // return null to hand the URL to the next resolver
        return null;
    }
}
```

For `ServiceLoaderUtils.getServiceClasses(...)` / `getServiceClassNames(...)`, implementation classes are
resolved by name and validated against the service type; with `failFast = true` (the default) an unloadable
or non-assignable class throws `IllegalStateException`, with `failFast = false` it is skipped.

---

## 3. System properties and tunables

Only keys that exist in the source are listed. "Read by" is the class that reads the value at class
initialization time.

| Key | Default | Read by | Effect |
|---|---|---|---|
| `microsphere.service-loader.cached` | `false` | `io.microsphere.util.ServiceLoaderUtils` | Enables the process-wide `servicesCache` for `ServiceLoaderUtils` calls that do not pass an explicit `cached` argument |
| `microsphere.io.buffer.size` | `2048` | `io.microsphere.io.IOUtils` | Buffer size used when copying streams / reading resources |
| `microsphere.shutdown-hook.callbacks-capacity` | `512` | `io.microsphere.util.ShutdownHookUtils` | Initial capacity of the priority queue holding shutdown-hook callbacks |
| `microsphere.reflect.resolved-generic-types.cache.size` | `256` | `io.microsphere.reflect.TypeUtils` | Initial capacity of the resolved-generic-types cache |
| `microsphere.reflect.banned-methods` | *(unset)* | `io.microsphere.reflect.MethodUtils` | Pipe-separated signatures, e.g. `java.lang.String#substring() \| java.lang.String#substring(int,int)`; matched methods are treated as not found |
| `microsphere.bean.properties.max-resolved-depth` | `100` | `io.microsphere.beans.BeanUtils` | Recursion guard for nested bean property resolution |
| `microsphere.bean.metadata.cache.size` | `64` | `io.microsphere.beans.BeanUtils` | Initial capacity of the `BeanMetadata` cache |
| `microsphere.file-watch-service.thread-name-prefix` | `microsphere-file-watch-service` | `io.microsphere.io.StandardFileWatchService` | Thread name prefix of the file-watch event loop |
| `microsphere.artifact-id.manifest-attribute-names` | `Bundle-Name,Automatic-Module-Name,Implementation-Title` | `io.microsphere.classloading.ManifestArtifactResourceResolver` | Ordered, comma-separated `META-INF/MANIFEST.MF` attribute names used as the artifact id |
| `microsphere.artifact-version.manifest-attribute-names` | `Bundle-Version,Implementation-Version` | `io.microsphere.classloading.ManifestArtifactResourceResolver` | Same, for the artifact version |
| `process.execution.timeout` | `30000` (ms) | `io.microsphere.process.ProcessExecutor` | Default timeout applied to executed external processes |

Standard JDK properties the library reads (not microsphere-owned, listed because behaviour depends on them):

| Key | Read by | Effect |
|---|---|---|
| `java.protocol.handler.pkgs` | `io.microsphere.net.URLUtils`, `io.microsphere.net.ExtendableProtocolURLStreamHandler` | Colon-separated package list the JDK consults for protocol handlers; microsphere **appends** its handler packages when an `ExtendableProtocolURLStreamHandler` is instantiated |
| `java.util.PropertyResourceBundle.encoding` | `io.microsphere.util.PropertyResourceBundleUtils` | Default encoding for `PropertyResourceBundle`; falls back to the platform file encoding |
| `java.security.policy` | `io.microsphere.security.SecurityUtils` | Read to locate the policy file used by security-related helpers |
| `java.version`, `java.specification.version`, `java.class.path`, `java.home` | `io.microsphere.util.SystemUtils`, `io.microsphere.classloading.ArtifactDetector` | JDK-version predicates, classpath enumeration, JDK-library filtering |

---

## 4. Resource paths

| Path | Written / read by | Notes |
|---|---|---|
| `META-INF/services/` | `ServiceLoaderUtils.SERVICES_PROVIDER_LOCATION` | Also exposed as the pattern `META-INF/services/{}` (`SERVICE_PROVIDER_CONFIG_FILES_LOCATION_PATTERN`) |
| `META-INF/` | `ResourceConstants.METADATA_RESOURCE` | Metadata root |
| `META-INF/microsphere/` | `ResourceConstants.MICROSPHERE_METADATA_RESOURCE` | Microsphere metadata root |
| `META-INF/microsphere/configuration-properties.json` | written by `ConfigurationPropertyAnnotationProcessor` (via `Filer`, `CLASS_OUTPUT`); constant `CONFIGURATION_PROPERTY_METADATA_RESOURCE`; read by `MetadataResourceConfigurationPropertyLoader` | A copy ships inside `microsphere-java-core.jar` and lists every `@ConfigurationProperty` key above |
| `META-INF/microsphere/additional-configuration-properties.json` | `AdditionalMetadataResourceConfigurationPropertyLoader` (registered SPI) | Hand-written additions merged into the loader chain; priority `MIN_PRIORITY + 9` |
| `META-INF/MANIFEST.MF` | `JarUtils.MANIFEST_RESOURCE_PATH`, `ManifestArtifactResourceResolver` | Artifact id / version attributes are configurable (see §3) |
| `META-INF/maven/**/pom.properties` | `MavenArtifactResourceResolver` (`MAVEN_POM_PROPERTIES_RESOURCE_PREFIX` + `/pom.properties`) | Keys read: `groupId`, `artifactId`, `version` |
| `META-INF/banned-artifacts` | `BannedArtifactClassLoadingExecutor.CONFIG_LOCATION` | One banned artifact per line for class-loading enforcement |

> [!NOTE]
> `io.microsphere.classloading.StreamArtifactResourceResolver` is an abstract base for resolvers that read
> a metadata entry from an archive; it is intentionally absent from the service file — only concrete
> resolvers are registered.

---

## 5. JDK compatibility matrix

| Concern | JDK 8 | JDK 9 – 15 | JDK 16+ |
|---|---|---|---|
| `URLClassPath` access | `ClassicURLClassPathHandle` (`sun.misc.URLClassPath`, field `urls`) | `ModernURLClassPathHandle` (`jdk.internal.loader.URLClassPath`, field `unopenedUrls`) | `ModernURLClassPathHandle`, plus open-module flags |
| Fallback when neither supports | `NoOpURLClassPathHandle` (`supports()` always `true`, returns no URLs, `removeURL` returns `false`) | same | same |
| Process id | `VirtualMachineProcessIdResolver` (`sun.management` `jvm` field, priority 5) then `ClassicProcessIdResolver` (priority 9) | `ModernProcessIdResolver` (`java.lang.ProcessHandle`, priority 1) wins | `ModernProcessIdResolver` wins; reflective resolvers only work with `--add-opens` |
| Illegal reflective access | n/a | `--illegal-access=permit` (build profile `java9-15`) | Strong encapsulation; `setAccessible` on a non-opened package throws `InaccessibleObjectException` |
| Logging delegate resolution | identical | identical | identical (`Class.forName`-style resolution via the `LoggerFactory` class loader) |

The build parent `io.github.microsphere-projects:microsphere-build:0.3.16` activates JDK profiles by
`<jdk>` range:

| Profile | Activation | What it does |
|---|---|---|
| `java8-16` (repository parent `microsphere-java-parent`) | `[1.8,17)` | pins Spring Framework to `5.3.39` for older JDKs; `[17,)` gets `7.0.9` |
| `java9+` | `[9,)` | sets `maven.compiler.release` (still `8`) |
| `java11+` | `[11,)` | Javadoc `<source>` and tool-version adjustments |
| `java9-15` | `[9,15]` | `jvm.argLine = --illegal-access=permit` |
| `java16+` | `[16,)` | forks `javac` with `-J--add-opens=java.base/java.lang=ALL-UNNAMED` and `-J--add-opens=java.base/java.lang.invoke=ALL-UNNAMED`; sets `jvm.argLine` to the same two flags and feeds Surefire `<argLine>@{jacoco.argLine} ${jvm.argLine}` |

CI (`.github/workflows/maven-build.yml`) builds the matrix `['8','11','17','21','25']` on `ubuntu-latest`
with `temurin`, running `mvn ... -Drevision=0.0.1-SNAPSHOT -Dsurefire.useSystemClassLoader=false test
--activate-profiles test,coverage`.

> [!IMPORTANT]
> If you add your own JDK to a matrix and hit reflective-access failures on `AbstractURLClassPathHandle`,
> `VirtualMachineProcessIdResolver`, or `URLUtils.getURLStreamHandlerFactory()`, add
> `--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED` to your
> own `argLine`, and open the specific package you touch (for example `java.base/jdk.internal.loader`).

---

## 6. Modules and BOM reference

`groupId` is `io.github.microsphere-projects` for every artifact; `${revision}` is `0.3.20-SNAPSHOT`.

| artifactId | Packaging | Purpose |
|---|---|---|
| `microsphere-java` | `pom` | Reactor root; aggregates all modules, inherits the external `microsphere-build` parent |
| `microsphere-java-parent` | `pom` | Parent for the library modules; imports `microsphere-all-bom` and `spring-framework-bom`, adds the `java8-16` JDK profile |
| `microsphere-java-dependencies` | `pom` | The BOM: `dependencyManagement` for the 7 jar modules below |
| `microsphere-java-annotations` | `jar` | Marker/semantic annotations (`@Since`, `@Nullable`, `@Nonnull`, `@Immutable`, `@Experimental`, `@ThreadSafe`, `@ConfigurationProperty`) |
| `microsphere-java-core` | `jar` | All `io.microsphere.*` utilities and every SPI listed in §1 |
| `microsphere-jdk-tools` | `jar` | `io.microsphere.jdk.tools.compiler.Compiler` — programmatic `javac` access |
| `microsphere-java-test` | `jar` | JUnit 5 fixtures and test helpers |
| `microsphere-annotation-test` | `jar` | `AbstractAnnotationProcessingTest` harness that runs a real compilation round in-process |
| `microsphere-lang-model` | `jar` | `javax.lang.model` helpers (elements, types, messages) |
| `microsphere-annotation-processor` | `jar` | `ConfigurationPropertyAnnotationProcessor`, registered through `META-INF/services/javax.annotation.processing.Processor` |

The BOM (`microsphere-java-dependencies`) manages only these seven artifacts, all at `${revision}`:
`microsphere-java-annotations`, `microsphere-java-core`, `microsphere-jdk-tools`, `microsphere-java-test`,
`microsphere-annotation-test`, `microsphere-lang-model`, `microsphere-annotation-processor`. Third-party
versions come from `microsphere-all-bom` and `spring-framework-bom`, imported by `microsphere-java-parent`.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-java-dependencies</artifactId>
            <version>0.3.19</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

`microsphere-java-core` declares `javax.annotation-api`, `slf4j-api` and `commons-logging` as `optional`
compile dependencies; JUnit, Logback, Spring Core and JMH appear only in `test` scope.

---

## 7. Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `IllegalArgumentException: No Service interface[type : ...] implementation was defined in service loader configuration file[/META-INF/services/...]` | The service file is missing, misnamed, or not on the class loader used by `ServiceLoaderUtils` (`loadServicesAsList` never returns empty) | Name the file with the interface's fully qualified **binary** name; run `mvn package` and confirm the file is inside the jar; pass the right `ClassLoader` overload |
| `IllegalStateException: The service class[name : '{}'] can't be loaded by {}` from `getServiceClasses` | An entry in the service file points at a class that is not on that class loader, and `failFast` is `true` (the default) | Remove the stale entry, add the missing dependency, or call `getServiceClasses(type, classLoader, false)` to skip unloadable entries |
| `ServiceConfigurationError` / implementation silently ignored | The class lacks a public no-arg constructor, or does not implement the service interface | Add the constructor; check the `implements` clause — `Prioritized` sub-interfaces must match exactly |
| Your `Prioritized` implementation is never picked first | Priority direction: smaller int = earlier. Built-ins already use `0`, `1`, `5`, `9`, or `MAX_PRIORITY + 99999` | Return a smaller value than the built-in you want to beat, or use `Prioritized.MAX_PRIORITY` |
| `NoClassDefFoundError: org/slf4j/Logger` (or `org/apache/commons/logging/Log`) | An optional integration is on the compile classpath but missing at runtime, and something touched the adapter class directly rather than the facade | Keep only the facade (`io.microsphere.logging.LoggerFactory.getLogger`) in your code, or add the real `slf4j-api` / `commons-logging` dependency |
| All log output disappears | `NoOpLoggerFactory` won the `isAvailable()` vote because no delegate class resolves | Add SLF4J (or `java.util.logging` is always present — check you did not shadow it) |
| `InaccessibleObjectException` on JDK 17/21/25 | `AbstractURLClassPathHandle`, `VirtualMachineProcessIdResolver`, `URLUtils.getURLStreamHandlerFactory()` and `ProcessHandle`-free paths use `setAccessible(true)` on JDK internals | Add `--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED` (the build's `java16+` profile does exactly this) plus the package you touch, e.g. `--add-opens java.base/jdk.internal.loader=ALL-UNNAMED` |
| `URL.setURLStreamHandlerFactory` can only be called once / `IllegalStateException` | Another library already installed the JVM-wide factory | Use `URLUtils.attachURLStreamHandlerFactory(factory)`: it reads the current factory through reflection, wraps old + new in a `CompositeURLStreamHandlerFactory`, clears and re-installs; registering two different plain factories still fails |
| `classpath:` / `console:` URLs throw `Unknown protocol` | `ServiceLoaderURLStreamHandlerFactory.attach()` was never called, and `java.protocol.handler.pkgs` only takes effect for handlers instantiated by the JVM lookup | Call `ServiceLoaderURLStreamHandlerFactory.attach()` once at startup; make sure your `Handler` class is top-level, named exactly `Handler`, and lives in `<package>.<protocol>.Handler` |
| `META-INF/microsphere/configuration-properties.json` is missing | The processor did not run: no `<annotationProcessorPaths>`, `-proc:none`, or the module was compiled without the processor jar | Add `microsphere-annotation-processor` to `annotationProcessorPaths`; verify `@SupportedAnnotationTypes` sees `io.microsphere.annotation.ConfigurationProperty`; on JDK 16+ keep the two `-J--add-opens` compiler args |
| A `@ConfigurationProperty` key is missing from the generated metadata | Metadata is generated from root elements plus the `ConfigurationPropertyGenerator` SPI; runtime-only keys are not visible at compile time | Declare the constant with `@ConfigurationProperty`, or provide a `ConfigurationPropertyLoader` that supplies it |
| Your test class never runs | Surefire includes only `**/*Test.java` and `**/*Tests.java` and excludes `**/Abstract*.java` | Rename to `FooTest` / `FooTests`; keep shared bases prefixed with `Abstract` |
| `microsphere.*` system property appears ignored | Value is read once in a `static final` initializer, often before your `System.setProperty` line | Move it to the command line as `-D`, or to `surefire.argLine` |
| Artifact / classpath detection returns nothing on JDK 9+ | `findAllClassPathURLs` depends entirely on `URLClassPathHandle.getURLs` and then on `ClassLoaderUtils.findURLClassLoader`, which walks up for a `URLClassLoader`. When `ModernURLClassPathHandle.supports()` is `false` (no access to `jdk.internal.loader.URLClassPath` / the `ucp` field) and no ancestor is a `URLClassLoader`, the result is empty and `NoOpURLClassPathHandle` is the only match | Open the module (`--add-opens java.base/jdk.internal.loader=ALL-UNNAMED`), or run the code under a `URLClassLoader` (this is why CI passes `-Dsurefire.useSystemClassLoader=false`), or call `ArtifactDetector.detect(Set<URL>)` with URLs you supply |

---

## 8. Where to read next

| Subsystem | Page |
|---|---|
| Service loading, `XxxUtils` conventions | [Core Utilities](core-utilities.md) |
| `Prioritized`, comparators, throwable-aware functions | [Language Abstractions](language-abstractions.md) |
| `Converter` / `MultiValueConverter` SPI | [Type Conversion](type-conversion.md) |
| `EventDispatcher` and `EventListener` auto-loading | [Event Dispatching](events.md) |
| `IOUtils.BUFFER_SIZE`, `StandardFileWatchService` | [I/O and File Watching](io-and-file-watch.md) |
| `java.protocol.handler.pkgs`, protocol handlers | [Networking and URL Protocols](networking-and-url-protocols.md) |
| `LoggerFactory` delegation chain | [Logging](logging.md) |
| `ProcessIdResolver`, `ProcessExecutor` timeouts | [Concurrency, Processes and JMX](concurrency-process-jmx.md) |
| `ArtifactResourceResolver`, `URLClassPathHandle`, banned artifacts | [Class Loading and Artifacts](classloading-and-artifacts.md) |
| Reader / Loader / Generator chain | [Configuration Property Metadata](configuration-metadata.md) |
| Processor registration and `--add-opens` | [Annotation Processing](annotation-processing.md) |
| Surefire include/exclude rules | [Testing Support](testing.md) |

## See also

* [Getting Started](getting-started.md) — coordinates, BOM import, first call.
* [Annotations](annotations.md) — `@ConfigurationProperty`, the input to metadata generation.
* The upstream guide: `docs/user-guide/README.md` (English-only, per-module task guide).

[← Handbook index](../README.md) · [Previous: Testing Support](testing.md)
