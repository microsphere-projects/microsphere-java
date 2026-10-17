# Annotation Processor

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-annotation-processor` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Main sources** | 4 types in `io.microsphere.annotation.processor` (`src/main/java/io/microsphere/annotation/processor/`) |
| **Tests** | `ConfigurationPropertyAnnotationProcessorTest`, `ConfigurationPropertyJSONElementVisitorTest`, `FilerProcessorTest`, `ResourceProcessorTest` + the `TestConfigurationPropertyLoader` SPI fixture |
| **Compile dependencies** | `microsphere-java-core` (`pom.xml:24-28`, artifactId `:26`), `microsphere-lang-model` (`pom.xml:31-35`, artifactId `:33`) |
| **Test dependencies** | `microsphere-annotation-test` (`:51-56`, artifactId `:53`, scope `test` `:55`), `junit-jupiter` (`:38-42`), `junit-jupiter-engine` (`:44-48`), `logback-classic` (`:59-63`), `javax.ws.rs-api` (`:66-70`), `jaxws-api` (`:72-76`), `spring-context` (`:80-84`), `spring-web` (`:86-90`) |
| **Processor SPI file** | `src/main/resources/META-INF/services/javax.annotation.processing.Processor:1` |
| **Packaging quirk** | runs `maven-shade-plugin` (`pom.xml:105`) and embeds `microsphere-java-core` into its own artifact (`pom.xml:115`) |
| **JDK level** | bytecode inherited from the external parent `microsphere-build:0.3.16`; CI-tested on JDK 8 / 11 / 17 / 21 / 25 |

`microsphere-annotation-processor` is the **compile-time half** of Microsphere's configuration-metadata feature. It
turns every field annotated with `io.microsphere.annotation.ConfigurationProperty` into a JSON document —
`META-INF/microsphere/configuration-properties.json` — that is packaged into your jar next to your classes. That
document is what IDEs read to offer property-name completion and documentation popups, and what the runtime package
`io.microsphere.metadata` reads back into `ConfigurationProperty` objects. The runtime counterpart — readers, loaders
and generators — is documented in
[Configuration Property Metadata](microsphere-java-core/configuration-metadata.md); this page covers only what happens
inside `javac`.

| Half | Module | Direction | Documented in |
|---|---|---|---|
| Compile time | `microsphere-annotation-processor` | **writes** `META-INF/microsphere/configuration-properties.json` | this page |
| Runtime | `microsphere-java-core` · `io.microsphere.metadata` | **reads** it, and feeds `ConfigurationPropertyLoader` results back into it | [Configuration Property Metadata](microsphere-java-core/configuration-metadata.md) |

---

## 1. What this module does

One processor is shipped: `ConfigurationPropertyAnnotationProcessor`
(`annotation/processor/ConfigurationPropertyAnnotationProcessor.java:78`), registered through the standard Java EE
processor SPI so that `javac` finds it automatically on the processor path
(`src/main/resources/META-INF/services/javax.annotation.processing.Processor:1`). Its whole job, in four steps:

1. For every compilation round that still has work, walk the round's root elements
   (`ConfigurationPropertyAnnotationProcessor.java:107-110`) and let a visitor
   (`annotation/processor/ConfigurationPropertyJSONElementVisitor.java:57`) collect a JSON object per annotated field
   into one `StringBuilder`.
2. Append the properties contributed by every `ConfigurationPropertyLoader` registered on the *compile* classpath
   (`ConfigurationPropertyAnnotationProcessor.java:134-140`).
3. On the final round, parse the accumulated text as a `JSONArray`, pretty-print it with two-space indentation and
   write it as a class-output resource
   (`ConfigurationPropertyAnnotationProcessor.java:149-156`).
4. Print the complete generated document to the build log as a compiler `NOTE`, so you can see it without opening the
   jar (`ConfigurationPropertyAnnotationProcessor.java:154`).

The output is a machine-owned file; the hand-written companion is
`META-INF/microsphere/additional-configuration-properties.json`
(`microsphere-java-core/src/main/java/io/microsphere/constants/ResourceConstants.java:60`). The seven
entries currently shipped in `microsphere-java-core` are exactly this processor's output — see section 4.

> [!NOTE]
> The processor is *not* a code generator: it never produces `.java` files. It produces one JSON resource. Everything
> else in this module (`FilerProcessor`, `ResourceProcessor`) exists to make resource I/O inside `javac` safe.

---

## 2. Coordinates and how to enable it

Group id `io.github.microsphere-projects` (`pom.xml:12`), version `${revision}` = `0.3.20-SNAPSHOT` (`pom.xml:54`), tag
`0.3.20` published. Two ways to turn the processor on.

### 2.1 Plain dependency — auto-discovery via the service file

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-annotations</artifactId>
    <version>0.3.20</version>
</dependency>

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-annotation-processor</artifactId>
    <version>0.3.20</version>
    <scope>provided</scope>
</dependency>
```

`javac` scans the compile classpath for `META-INF/services/javax.annotation.processing.Processor` and instantiates
every listed class, so no compiler configuration is needed. Two consequences of this route: the processor and its
transitive dependencies pollute your **compile** classpath (hence `provided`, so they stay out of the runtime classpath),
and recent JDKs emit a warning when annotation processing is implicitly enabled by classpath discovery — silence it
with explicit `annotationProcessorPaths` below, or with `-proc:none` where processing is not wanted.

The `microsphere-java-annotations` dependency is **not optional**: the visitor resolves the annotation type through
`Elements.getTypeElement("io.microsphere.annotation.ConfigurationProperty")` and fails fast when the lookup returns
`null` (`microsphere-lang-model/src/main/java/io/microsphere/lang/model/util/AnnotatedElementJSONElementVisitor.java:109-110`).

### 2.2 Explicit `annotationProcessorPaths`

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>io.github.microsphere-projects</groupId>
                <artifactId>microsphere-annotation-processor</artifactId>
                <version>0.3.20</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

| | `<annotationProcessorPaths>` | `<compilerArgument>` / `-processor` |
|---|---|---|
| Processor classpath | isolated from the compile classpath | shared with it |
| Needs the annotation API on the compile classpath | yes, still (`provided` is fine) | yes |
| Must you list the processor's own transitive deps | yes, `annotationProcessorPaths` resolves its own tree — but here `microsphere-java-core` is already **shaded into** the processor jar (`pom.xml:105-121`), so one `<path>` entry is enough | no |
| Naming a single processor explicitly | not needed | `-processor io.microsphere.annotation.processor.ConfigurationPropertyAnnotationProcessor` forces it even when other processors are present |
| Reproducibility | version pinned independently of the project's dependency management | inherits whatever the compile classpath resolves |
| JDK 8 | works; `javax.annotation.processing` lives in `rt.jar`, `javac` itself in `tools.jar` | same |

`<compilerArgument>` is the legacy single-value form; prefer `<compilerArgs><arg>…</arg></compilerArgs>` when you need
more than one, and remember that a `<proc>none</proc>` (or `-proc:none`) anywhere in the effective configuration turns
the processor off **even when the dependency is present**.

### 2.3 What this repository's own build does

Neither. Grepping every `pom.xml` in the reactor finds no `<annotationProcessorPaths>` at all, and the only
`maven-compiler-plugin` block is in this module:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgument>-proc:none</compilerArgument>
    </configuration>
</plugin>
```

_Source: `microsphere-annotation-processor/pom.xml:96-102` (`-proc:none` at `:100`)_

That is the bootstrap exception, not an example to copy: the module compiles the processor itself, so running it on
its own sources would be circular. Everything else in the reactor simply compiles with processing disabled by absence —
which is why `microsphere-java-core`'s `META-INF/microsphere/configuration-properties.json` is a **checked-in,
manually regenerated** file rather than a build product. The test harness (section 7) is the one place the processor
really runs.

---

## 3. Version information and compatibility range

| | |
|---|---|
| **GroupId** | `io.github.microsphere-projects` (`pom.xml:12`) |
| **Reactor version** | `${revision}` = `0.3.20-SNAPSHOT` (`pom.xml:54`); tag `0.3.20` is published |
| **BOM** | `microsphere-java-parent/pom.xml` imports `microsphere-all-bom:0.3.13` (`:22`, `:31-37`) |
| **Spring** | `spring-framework-bom` imported (`microsphere-java-parent/pom.xml:40-46`); `7.0.9` by default (`:23`), `5.3.39` under profile `java8-16` activated by `<jdk>[1.8,17)</jdk>` (`:54-62`, activation `:57`, pin `:60`) — test scope only (`pom.xml:80-90`) |
| **JDK matrix** | Temurin 8, 11, 17, 21, 25 (`.github/workflows/maven-build.yml:25`, `distribution: 'temurin'` `:33`) |
| **`@since`** | `1.0.0` on all four main types (`ConfigurationPropertyAnnotationProcessor.java:75`, `ConfigurationPropertyJSONElementVisitor.java:55`, `FilerProcessor.java:74`, `ResourceProcessor.java:82`) |

Supported source version is *open-ended*: `getSupportedSourceVersion()` returns `latestSupported()`
(`ConfigurationPropertyAnnotationProcessor.java:162-165`), so on any JDK the processor claims the newest source level
that JDK offers. Supported annotation types come from the class-level
`@SupportedAnnotationTypes(CONFIGURATION_PROPERTY_ANNOTATION_CLASS_NAME)`
(`ConfigurationPropertyAnnotationProcessor.java:77`), whose value is the string
`"io.microsphere.annotation.ConfigurationProperty"` (`ConfigurationPropertyJSONElementVisitor.java:59`). Supported
options: none — `getSupportedOptions()` is **not overridden**, so `AbstractProcessor`'s default reads the absent
`@SupportedOptions` annotation and yields an empty set; there is no `-A` flag this processor understands.

> [!NOTE]
> There is **no** `maven.compiler.*` or `<release>` property anywhere in this reactor; the bytecode level comes from the
> external parent `io.github.microsphere-projects:microsphere-build:0.3.16`. That is a deliberate tension worth knowing
> about: the *artifact* is Java-8 bytecode and loads on JDK 8+, while the *processor* claims `latestSupported()`, so on
> JDK 25 it will happily process Java 25 sources. Anything that works on the claimed source level and on the JDK-6-era
> `ElementKindVisitor6` base (`microsphere-lang-model/src/main/java/io/microsphere/lang/model/util/JSONElementVisitor.java:57`)
> is fair game; new element kinds are silently skipped (see the developer guide).

---

## 4. What gets generated

### 4.1 Where

One file, one location, always:

| | |
|---|---|
| Resource name | `META-INF/microsphere/configuration-properties.json` (`microsphere-java-core/src/main/java/io/microsphere/constants/ResourceConstants.java:54`, file name `:43`) |
| Written to | `StandardLocation.CLASS_OUTPUT` (`ConfigurationPropertyAnnotationProcessor.java:46`, `:93`) |
| Effect | lands in `target/classes`, therefore inside your jar, next to your `.class` files |
| Formatting | `new JSONArray(text).toString(2)` — two-space pretty-print (`ConfigurationPropertyAnnotationProcessor.java:151-152`) |

There is no way to relocate or rename it: the constant is hard-coded in the import at
`ConfigurationPropertyAnnotationProcessor.java:39` and passed straight to the writer at `:150`.

### 4.2 The JSON shape

A real fragment of the generated file shipped in `microsphere-java-core` (all seven entries were produced by this
processor):

```json
[
  {
    "name": "microsphere.io.buffer.size",
    "type": "int",
    "defaultValue": "2048",
    "required": false,
    "description": "The buffer size for I/O",
    "metadata": {
      "sources": [
        "system-properties"
      ],
      "declaredClass": "io.microsphere.io.IOUtils",
      "declaredField": "BUFFER_SIZE"
    }
  }
]
```

_Source: `microsphere-java-core/src/main/resources/META-INF/microsphere/configuration-properties.json:30-43`_

| Key | Emitted when | Proven by |
|---|---|---|
| `name` | always | `microsphere-java-core/src/main/java/io/microsphere/metadata/DefaultConfigurationPropertyGenerator.java:88` |
| `type` | always | `:90-92` |
| `value` | only if non-`null` — the processor never sets it, so it never appears in generated files | `:94-98`; no `setValue(...)` call in `ConfigurationPropertyJSONElementVisitor.java:73-99` |
| `defaultValue` | only if the annotation states one explicitly | `:100-104` + `ConfigurationPropertyJSONElementVisitor.java:85-87`, `:138-141` |
| `required` | always | `:106-107` + `ConfigurationPropertyJSONElementVisitor.java:88-90` |
| `description` | only if non-`null`, including a Javadoc-derived value | `:109-113` + `ConfigurationPropertyJSONElementVisitor.java:130-136` |
| `metadata` | always | `:115-117` |
| `metadata.sources` / `metadata.targets` | only when non-empty | `:127-137` |
| `metadata.declaredClass` / `metadata.declaredField` | always, both set from the field | `:139-149` + `ConfigurationPropertyJSONElementVisitor.java:98-99` |

The last entry of the shipped file is the interesting one for IDE authors: because `description` falls back to the raw
Javadoc (`ConfigurationPropertyJSONElementVisitor.java:133`), `microsphere.reflect.banned-methods` carries an entire
`<h3>`/`<pre>{@code …}` block into the JSON (`…/configuration-properties.json:90`), with `/` escaped as `\/`.

### 4.3 Properties that come from loaders, not from annotations

Before the file is written, the processor calls `ConfigurationPropertyLoader.loadAll()`
(`ConfigurationPropertyAnnotationProcessor.java:135`, statically imported at `:44`) and renders every returned property
through the same `ConfigurationPropertyGenerator` the visitor uses (`:142-147`). That is the extension point: a module
can publish `META-INF/services/io.microsphere.metadata.ConfigurationPropertyLoader` and have its properties merged into
the generated file at compile time. This module does exactly that in its test scope:

```
microsphere-annotation-processor/src/test/resources/META-INF/services/io.microsphere.metadata.ConfigurationPropertyLoader
io.microsphere.annotation.processor.TestConfigurationPropertyLoader
```

with the loader generating one property per JDK simple type, including random `sources`/`targets`
(`io/microsphere/annotation/processor/TestConfigurationPropertyLoader.java:41-71`).

> [!IMPORTANT]
> Loader-contributed properties are appended **inside** the `if (!elements.isEmpty())` block
> (`ConfigurationPropertyAnnotationProcessor.java:116`, call at `:123`). A module whose sources contain no root
> elements worth visiting therefore produces `[]` and silently drops everything your loaders would have contributed.
> Annotations and loaders must live in the same compilation unit to merge.

---

## 5. Which annotated elements are visited

`@ConfigurationProperty` declares `@Target(FIELD)` and `@Retention(RUNTIME)`
(`microsphere-java-annotations/src/main/java/io/microsphere/annotation/ConfigurationProperty.java:36-38`), so the set of
candidate elements is fields only. Within that, the traversal is decided by three layers:

| Layer | Rule | Citation |
|---|---|---|
| Round selection | every **root element** of the round is offered to the visitor — not `RoundEnvironment.getElementsAnnotatedWith(...)` | `ConfigurationPropertyAnnotationProcessor.java:108`, `:117-121` |
| Kind filtering | `AnnotatedElementJSONElementVisitor.supports(Element)` matches the element's kind against the annotation's declared `@Target` set | `microsphere-lang-model/.../AnnotatedElementJSONElementVisitor.java:111`, `:124-126` |
| Type entry | `supportsType` is overridden to `true` so a type is always descended into | `ConfigurationPropertyJSONElementVisitor.java:109-112` |
| Member walk | `visitType` calls `super.visitType` and then walks `e.getEnclosedElements()` | `microsphere-lang-model/.../JSONElementVisitor.java:87-101`, `:111-119` |
| Field payload | `visitVariableAsField` produces JSON only when the `@ConfigurationProperty` mirror is present | `ConfigurationPropertyJSONElementVisitor.java:70-72`, `:106` |

Practical reading of those rules:

* **Only declared members count.** `getEnclosedElements()` does not include inherited fields, so a field annotated in a
  superclass is not emitted when the subclass is compiled (pitfall 6).
* **Local variables, parameters, enum constants and record components are ignored** — they enter `visitVariable` but
  never reach the `FIELD` branch that `visitVariableAsField` overrides
  (`microsphere-lang-model/.../JSONElementVisitor.java:71-77`).
* **The annotation mirror, not the compile-time annotation, is the source of truth**: each attribute is read from the
  `AnnotationMirror` in declaration order (`ConfigurationPropertyJSONElementVisitor.java:71`, `:74-97`), so no
  reflection on the annotated class happens and the annotated class need not be loadable.
* **Defaults mean "derive it"**: an omitted `name` becomes the field's *constant value* — which is why
  `public static final String BUFFER_SIZE = "microsphere.io.buffer.size"` needs no `name` at all
  (`ConfigurationPropertyJSONElementVisitor.java:119-122`); an omitted `type` becomes the field's declared type
  (`:124-128`); an omitted `description` becomes its Javadoc (`:130-136`).
* **`source` is the only array-valued attribute** and is the fall-through branch (`:94-96`, `:143-152`); the three
  constants `system-properties`, `environment-variables`, `application`
  (`ConfigurationProperty.java:43`, `:48`, `:53`) are the values you will see in `metadata.sources`.

---

## 6. `FilerProcessor` and `ResourceProcessor`

Two helpers isolate `javax.annotation.processing.Filer` behaviour, and they differ in how they fail — which matters for
pitfalls 2 and 8.

| Type | Visibility | Responsibility | Failure behaviour |
|---|---|---|---|
| `FilerProcessor` (`annotation/processor/FilerProcessor.java:76`) | `public` | obtain the `Filer` from the `ProcessingEnvironment` and run a callback against it (`:92-96`); expose the underlying `JavaFileManager` (`:103-106`) | default handler prints a **mandatory warning** and returns `null` (`:84-90`) |
| `ResourceProcessor` (`annotation/processor/ResourceProcessor.java:84`) | `public` | one `Location` + `moduleAndPackage` (`:90-94`), cached `FileObject`s keyed by read/write + name (`:98`, `:123-130`), existence check (`:136-142`, `:254-256`), and typed access to `InputStream`/`Reader`/`CharSequence` (`:156-213`) and `OutputStream`/`Writer` (`:215-252`) | default handlers warn **and rethrow** as an unchecked exception (`:112-118`, `:235-240`) |

The processor itself uses only the write path: `processInResourceWriter(CONFIGURATION_PROPERTY_METADATA_RESOURCE, …)`
with `CLASS_OUTPUT` (`ConfigurationPropertyAnnotationProcessor.java:150`, `:93`). `FilerProcessor`'s
`getJavaFileManager()` reaches into the `Filer`'s private `fileManager` field reflectively
(`FilerProcessor.java:105`) — that is the one API here with an implementation detail of `javac` baked into it.

`ResourceProcessor` distinguishes reading from writing with two package-private flags, `FOR_READING = false` and
`FOR_WRITING = true` (`ResourceProcessor.java:86`, `:88`), and a resource "exists" only when
`FileObject.getLastModified() > 0` (`:254-256`).

---

## 7. Verifying the output in your own build

Three levels, cheapest first.

1. **Read the build log.** On the final round the processor emits a `NOTE` containing the *complete* generated document,
   loader-contributed entries included (`ConfigurationPropertyAnnotationProcessor.java:154`). `javac` prints notes by
   default; Maven surfaces them with the module's compiler output.
2. **Open the artifact.** `META-INF/microsphere/configuration-properties.json` is class output, so it is inside your
   jar: `unzip -p target/your.jar META-INF/microsphere/configuration-properties.json`. If the entry is missing, the
   processor did not run (pitfall 1) rather than ran and found nothing — an empty result is still written as `[]`
   (`ConfigurationPropertyAnnotationProcessor.java:113`, `:126-131`).
3. **Run it in-process.** The four test classes extend `AbstractAnnotationProcessingTest`
   (`io/microsphere/annotation/processor/ConfigurationPropertyAnnotationProcessorTest.java:34`), which is wired to a
   JUnit 5 `InvocationInterceptor` (`microsphere-annotation-test/src/main/java/io/microsphere/test/annotation/processing/AbstractAnnotationProcessingTest.java:53`).
   The interceptor compiles the test class plus whatever `addCompiledClasses` adds
   (`AbstractAnnotationProcessingTest.java:133-134`) with an in-process `io.microsphere.jdk.tools.compiler.Compiler`,
   registers a test driver processor, then **adds every `Processor` found on the test classpath**
   (`microsphere-annotation-test/src/main/java/io/microsphere/test/annotation/processing/CompilerInvocationInterceptor.java:54-63`)
   — so this module's processor is exercised automatically by any test that uses the harness. On JDK 8 the classes it
   drives live in `tools.jar`, which is why `microsphere-jdk-tools` exists as the wrapper.

The suites pin the surrounding contracts: `ResourceProcessorTest` reads its own source file back through
`SOURCE_PATH` (`io/microsphere/annotation/processor/ResourceProcessorTest.java:56`, `:66-68`, `:92-138`), writes and
deletes random class-output resources (`:151-189`, cleanup at `:71-74`), and asserts the rethrowing behaviour with
`assertThrows(RuntimeException.class, …)` (`:82-90`); `FilerProcessorTest` asserts the `null`-returning degradation
(`io/microsphere/annotation/processor/FilerProcessorTest.java:58-62`).

> [!WARNING]
> There is **no test in the repository that asserts the content of the generated file.** The only content-level test
> builds an empty element set and calls `assertNotNull` on the resulting string
> (`ConfigurationPropertyAnnotationProcessorTest.java:37-43`) — a non-null check, not a comparison with `[]`. Treat the
> shape in section 4.2, taken from `microsphere-java-core`'s checked-in output, as the specification.

---

## 8. Common pitfalls

1. **The processor never ran.** No `META-INF/microsphere/configuration-properties.json` in `target/classes` means the
   processor was not on the *processor* path: the dependency is missing, is `provided` but the module also sets
   `<annotationProcessorPaths>` (which replaces classpath discovery entirely), or `-proc:none` is in effect — note that
   this module's own build uses exactly that (`pom.xml:100`), and a `<proc>none</proc>` in a shared parent propagates.
   Nothing downstream complains: the runtime loaders treat a missing resource as "nothing to do", see
   [Configuration Property Metadata — pitfalls](microsphere-java-core/configuration-metadata.md).
2. **`microsphere-java-annotations` is missing from the compile classpath.** The visitor's superclass resolves the
   annotation type eagerly and throws when it cannot (`AnnotatedElementJSONElementVisitor.java:110`); because that
   happens in `init()` (`ConfigurationPropertyAnnotationProcessor.java:94`), the failure surfaces as "processor failed
   to initialize" and kills the build.
3. **`ConfigurationPropertyGenerator` must be discoverable at compile time.** The visitor's constructor calls
   `loadFirstService(ConfigurationPropertyGenerator.class)` (`ConfigurationPropertyJSONElementVisitor.java:65`), and
   that API is `@Nonnull` and throws `IllegalArgumentException` when no implementation is declared
   (`microsphere-java-core/src/main/java/io/microsphere/util/ServiceLoaderUtils.java:599-602`). This is the loudest
   failure in the module: a stripped service file means a broken compile, not degraded metadata.
4. **Incremental builds and stale resources.** `ResourceProcessor` caches the `FileObject` per direction and name for
   the lifetime of the `ProcessingEnvironment` (`ResourceProcessor.java:98`, `:123-130`), so repeated writes in one
   compile reuse the handle — but the cache does not survive across builds, and `Filer.createResource` is the "create"
   API, so a leftover output tree plus a second processor instance can produce a `FileAlreadyExistsException`. That
   path is *not* silent: the default handler rethrows wrapped
   (`ResourceProcessor.java:235-240` via `:114-117`), and the `NOTE` printer at `ConfigurationPropertyAnnotationProcessor.java:150-155`
   is the first thing to hit it. Run `mvn clean` when in doubt, and never edit the generated file by hand — the next
   compile overwrites it.
5. **The shaded copy of `microsphere-java-core`.** `maven-shade-plugin` runs at `package` (`pom.xml:103-121`, plugin at
   `:105`) with an `artifactSet` that includes exactly one artifact: `io.github.microsphere-projects:microsphere-java-core`
   (`:113-117`). There are **no `<relocations>`, no `<minimizeJar>`, no `<filters>`, no `<shadedArtifactAttached>`** in
   the whole `<configuration>` block (`:112-118`), so the plugin does the bare-minimum job: every class of core is
   copied into the processor jar **under its original package names**, and the shaded jar *replaces*
   `target/microsphere-annotation-processor-<version>.jar` as the main artifact (the pre-shade original is moved to
   `original-*.jar`). Because nothing is relocated, the duplicate-class consequence for consumers is direct: if your
   build depends on both `microsphere-annotation-processor` and `microsphere-java-core`, the same
   `io.microsphere.metadata.DefaultConfigurationPropertyGenerator` exists twice on the classpath and the winner is
   classpath order, not the declared version. Both copies also carry the same `META-INF/services/*` entries, so
   `ServiceLoader` de-duplicates by class name and the symptom is usually *version* skew (the embedded copy is pinned
   to `${revision}` at build time, `pom.xml:54` in the root reactor) rather than a `ClassNotFoundException`. Practical
   rules: keep the processor at `provided` scope, exclude nothing from core, and if you must depend on a *different*
   core version, remember the embedded one travels inside the processor.
   The upside of the same configuration: `annotationProcessorPaths` needs only one `<path>` entry, because core is
   already inside the processor artifact (section 2.2).
6. **Inheritance and cross-module aggregation are not merged.** Within a compilation, the metadata accumulates in one
   `StringBuilder` across rounds and is written once, to *this* module's class output
   (`ConfigurationPropertyAnnotationProcessor.java:82`, `:112-132`, `:149-156`). Nothing reads an existing
   `configuration-properties.json` to merge with: `processInResourceWriter` uses `createResource`
   (`ResourceProcessor.java:127`), not a read-modify-write. A multi-module build therefore produces **one file per jar**
   at the same classpath path; at runtime the built-in loaders read only the first hit
   (`microsphere-java-core/src/main/java/io/microsphere/metadata/ClassPathResourceConfigurationPropertyLoader.java:89`),
   so the "aggregate across modules" behaviour you may expect does not exist on either side. Same reason inherited
   fields are invisible: only `getEnclosedElements()` is walked (`microsphere-lang-model/.../JSONElementVisitor.java:96`).
7. **The same property name twice is not an error.** Two annotated fields with the same name (easy to produce, because a
   missing `name` silently takes the field's constant value, `ConfigurationPropertyJSONElementVisitor.java:119-122`)
   append two independent JSON objects — there is no name index, no overwrite, no warning; the generator is called once
   per field (`:101-103`) and `resolveMetadata` only trims the trailing comma
   (`ConfigurationPropertyAnnotationProcessor.java:126-131`). De-duplication exists *inside* one property only, where
   `sources` and `targets` are insertion-ordered `LinkedHashSet`s
   (`microsphere-java-core/src/main/java/io/microsphere/beans/ConfigurationProperty.java:258-263`, `:282-287`). If you
   need one entry per name, deduplicate after reading.
8. **A field whose constant value is not a `String` blows up loudly.** `resolveName` casts the fallback straight to
   `String` (`ConfigurationPropertyJSONElementVisitor.java:120-121`) and the model rejects a `null` name
   (`microsphere-java-core/src/main/java/io/microsphere/beans/ConfigurationProperty.java:120`), so annotating an
   `int`-typed constant field without `name = …` throws inside the visitor instead of skipping the field. Always state
   `name` explicitly for non-`String` constants.

---

## 9. See also

* [Configuration Property Metadata — User Guide](microsphere-java-core/configuration-metadata.md) — the runtime half:
  readers, loaders, generators, and what happens when the file is missing
* [Configuration Property Metadata — Developer Guide](../developer-guide/microsphere-java-core/configuration-metadata.md) — the runtime half's source analysis
* [Annotation Processor — Developer Guide](../developer-guide/microsphere-annotation-processor.md) — rounds, visitor, `Filer`, defects
* [microsphere-lang-model](microsphere-lang-model.md) — the `javax.lang.model` facade and the `JSONElementVisitor` family
* [microsphere-annotation-test](microsphere-annotation-test.md) — the in-process `javac` harness used by the tests here
* [microsphere-java-annotations](microsphere-java-annotations.md) — the module that owns `@ConfigurationProperty`
* [microsphere-jdk-tools](microsphere-jdk-tools.md) — the `tools.jar`/`javax.tools` wrapper the harness compiles with
* Chinese version: [中文用户指南](zh/microsphere-annotation-processor.md)
