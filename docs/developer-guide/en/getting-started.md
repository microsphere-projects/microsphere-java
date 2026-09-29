# Getting Started

> Read this page in: [中文](../zh/getting-started.md) · [English](getting-started.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Group ID for every artifact | `io.github.microsphere-projects` |
| Working version | `0.3.20-SNAPSHOT` · latest release `0.3.19` |
| Java level | compiled at **Java 8**, tested on JDK **8 / 11 / 17 / 21 / 25** |
| Package root | `io.microsphere.*` |
| Third-party dependencies | none in compile scope — optional integrations only |

`microsphere-java` is a foundational library: it fills in the gaps of the JDK standard API
(null-safe collections, reflection, type conversion, class-loading, event dispatching, logging
facade, JSON) without pulling in a framework. If you only remember one thing: add
`microsphere-java-core` and start calling `XxxUtils` static methods.

---

## 1. Requirements

| Requirement | Detail |
|---|---|
| Java | 8 or later. Every module sets `maven.compiler.source/target = 8`. |
| Tested JDKs | 8, 11, 17, 21, 25 (GitHub Actions matrix, `temurin`, `ubuntu-latest`) |
| Maven | 3.6+ for consumers; the repository itself pins Maven `3.9.16` through `mvnw` |
| Build parent | `io.github.microsphere-projects:microsphere-build:0.3.16` — an external artifact resolved from Maven Central |

Nothing in `microsphere-java-core` requires a framework. Optional integrations activate themselves:

| On your classpath | Effect |
|---|---|
| `org.slf4j:slf4j-api` | `io.microsphere.logging` delegates to SLF4J (highest priority) |
| `commons-logging` | Fallback delegate, used when SLF4J is absent |
| `javax.annotation-api` (JSR-250/305) | Annotation meta-model support in `microsphere-java-annotations` |
| A JDK, not a JRE | Required for `microsphere-jdk-tools` and `microsphere-annotation-test` |

---

## 2. Which module do I need?

| Your task | Add this artifact |
|---|---|
| Utility methods in an application | `microsphere-java-core` |
| `@Since` / `@Nullable` / `@Nonnull` / `@ConfigurationProperty` on your API | `microsphere-java-annotations` |
| Generate IDE-friendly configuration metadata | `microsphere-annotation-processor` (compile-time only) |
| Unit-test fixtures (models, services) | `microsphere-java-test` (`test` scope) |
| Test an annotation processor you wrote | `microsphere-annotation-test` (`test` scope) |
| Helpers over `javax.lang.model.*` | `microsphere-lang-model` |
| Drive `javax.tools.JavaCompiler` in-process | `microsphere-jdk-tools` |

`microsphere-java-core` already brings `microsphere-java-annotations` transitively, so in most
projects one dependency is enough.

The reactor builds nine modules; `microsphere-java-parent` (`pom`) is the shared parent and
`microsphere-java-dependencies` (`pom`) is the BOM. The BOM manages **exactly** these seven jar
modules: `microsphere-java-annotations`, `microsphere-java-core`, `microsphere-jdk-tools`,
`microsphere-java-test`, `microsphere-annotation-test`, `microsphere-lang-model`,
`microsphere-annotation-processor`.

---

## 3. Add the dependency

### 3.1 Maven: import the BOM

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-java-dependencies</artifactId>
            <version>0.3.19</version> <!-- or any released version -->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then declare only what you need, without versions:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-java-core</artifactId>
    </dependency>

    <!-- Compile-time only; generates configuration metadata -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-annotation-processor</artifactId>
        <optional>true</optional>
    </dependency>

    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-java-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Without the BOM, pin each version yourself:
`io.github.microsphere-projects:microsphere-java-core:0.3.19`.

### 3.2 Gradle

```groovy
dependencies {
    implementation platform("io.github.microsphere-projects:microsphere-java-dependencies:0.3.19")

    implementation "io.github.microsphere-projects:microsphere-java-core"

    annotationProcessor "io.github.microsphere-projects:microsphere-annotation-processor"

    testImplementation "io.github.microsphere-projects:microsphere-java-test"
    testImplementation "io.github.microsphere-projects:microsphere-annotation-test"
}
```

### 3.3 The transitive graph to expect

* `microsphere-java-core` → `microsphere-java-annotations` (compile); `javax.annotation-api`,
  `slf4j-api`, `commons-logging` are all `optional`.
* `microsphere-lang-model` and `microsphere-jdk-tools` → `microsphere-java-core`.
* `microsphere-annotation-processor` → `microsphere-java-core` + `microsphere-lang-model`. Its build
  **shades `microsphere-java-core` into the processor jar**, so the processor stays self-contained
  even when your application does not depend on core.
* `microsphere-java-test` and `microsphere-annotation-test` declare every dependency as `optional`
  so you can reuse them — which means **you** must provide JUnit Jupiter, Mockito, Logback,
  `javax.ws.rs-api`, `jaxws-api`, `spring-context` / `spring-web` for the types that reference them.

> [!TIP]
> Because the optional dependencies are genuinely optional, a `NoClassDefFoundError` in a test that
> touches a Spring or JAX-RS fixture usually means the test module needs that library added, not
> that Microsphere is broken.

---

## 4. First call

```java
import io.microsphere.collection.ListUtils;
import io.microsphere.convert.Converter;
import io.microsphere.util.ClassLoaderUtils;

Integer count = Converter.convertIfPossible("42", Integer.class);      // resolved through the Converter SPI
List<String> tags = ListUtils.of("a", "b", "c");                       // immutable, never null
ClassLoader loader = ClassLoaderUtils.getClassLoader(tags.getClass());  // null-safe, never throws
```

Two idioms cover the whole library. Helper classes are
`public abstract class XxxUtils implements Utils` with a private constructor, so you always write
`XxxUtils.method(...)`. SPI-backed subsystems put their static entry points on the interface itself —
`Converter.getConverter(sourceType, targetType)`, `Converter.convertIfPossible(source, targetType)` —
while the registry class (`Converters`) stays package-private.

---

## 5. Version alignment with Spring

`microsphere-java-parent` imports `spring-framework-bom` and switches the resolved line by JDK:

| JDK | Spring Framework (build-time) |
|---|---|
| `[1.8, 17)` — profile `java8-16` | `5.3.39` |
| `[17, ∞)` | `7.0.9` |

> [!IMPORTANT]
> This is build-time alignment **inside this repository**, not a requirement on your application.
> `microsphere-java-core` does not depend on Spring at all; only `microsphere-java-test` references
> Spring types, and marks those dependencies `optional`. Your own Spring Boot / Framework version
> wins — nothing here forces an upgrade.

---

## 6. Building from source

Only needed if you want to patch the library or run its tests:

```bash
git clone https://github.com/microsphere-projects/microsphere-java.git
cd microsphere-java

./mvnw package -DskipTests                       # fast first build
./mvnw test                                      # full test run
./mvnw test --activate-profiles test,coverage     # as CI does (adds JaCoCo, failsafe, checks)
./mvnw test -pl microsphere-java-core            # one module
./mvnw test -pl microsphere-java-core -Dtest=StringUtilsTest   # one test class
```

On Windows use `mvnw.cmd`. The wrapper is the `only-script` type (`wrapperVersion=3.3.4`,
Maven `apache-maven-3.9.16`).

### 6.1 JDK 16+ specifics

Handled by profiles inherited from `microsphere-build` — you do not add flags manually:

| Profile | Activation | Effect |
|---|---|---|
| `java9+` | `[9,)` | sets `maven.compiler.release=${java.version}` (8) |
| `java16+` | `[16,)` | forks the compiler with `-J--add-opens=java.base/java.lang=ALL-UNNAMED` and `java.base/java.lang.invoke`; feeds the same `--add-opens` to Surefire via `jvm.argLine` |
| `java9-15` | `[9,15]` | `jvm.argLine=--illegal-access=permit` |

If you add a newer JDK to your own matrix and see reflective-access failures, add those two
`--add-opens` entries to your `argLine`.

### 6.2 What CI runs

```bash
mvn --batch-mode --update-snapshots --file pom.xml \
    -Drevision=0.0.1-SNAPSHOT \
    -Dsurefire.useSystemClassLoader=false \
    test --activate-profiles test,coverage
```

Releases are deployed to Maven Central by a separate workflow on pushes to the `release` branch,
through `central-publishing-maven-plugin` (`server-id: ossrh`, `autoPublish=true`).

### 6.3 Test naming

Surefire includes `**/*Test.java` and `**/*Tests.java` and **excludes `**/Abstract*.java`** — which
is why the shared harness is named `AbstractAnnotationProcessingTest`.

---

## 7. Versions and release notes

* `${revision}` in the working tree is `0.3.20-SNAPSHOT`; it drives every module version.
* Released versions carry a bare-semver git tag (for example `0.3.19`), and each release also
  creates a GitHub Release.
* `release-notes.md` is **appended** by the publish workflow, so its oldest entry (`v0.2.7`) sits at
  the top and the newest at the bottom. Newest notes live in GitHub Releases.

> [!WARNING]
> `${revision}` is a Maven CI-friendly property flattened at build time. Editing it locally changes
> the version of every module in the reactor at once, including the BOM entries.

---

## See also

* [Annotations](annotations.md) — five minutes, useful in any codebase.
* [Core Utilities](core-utilities.md) — the page you will link most often.
* [Reference](reference.md) — every SPI file, system property and tunable in one place.

[← Handbook index](../README.md) · [Next: Annotations →](annotations.md)
