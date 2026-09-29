# Getting Started

[← Index](README.md)

---

## 1. Requirements

| Requirement | Detail |
|---|---|
| Java | 8 or later. All modules are compiled at `maven.compiler.source/target = 8`. |
| Tested JDKs | 8, 11, 17, 21, 25 (GitHub Actions matrix, `temurin` distribution, `ubuntu-latest`) |
| Maven | 3.6+ for consumers; the repository itself pins Maven `3.9.16` through the wrapper (`mvnw`) |
| Build parent | `io.github.microsphere-projects:microsphere-build:0.3.16` (external artifact, resolved from Maven Central) |

Nothing in `microsphere-java-core` requires a framework. Optional integrations:

| On classpath | Effect |
|---|---|
| `org.slf4j:slf4j-api` | `io.microsphere.logging` delegates to SLF4J (highest priority) |
| `commons-logging` | Fallback delegate, used when SLF4J is absent |
| `javax.annotation-api` (JSR-250/305) | Annotation meta-model support in `microsphere-java-annotations` |
| JDK (not JRE) | Required for `microsphere-jdk-tools` and `microsphere-annotation-test` |

---

## 2. Module inventory

The reactor (`pom.xml`, `revision = 0.3.20-SNAPSHOT`) builds nine modules in this order:

| Module | Packaging | What it is for |
|---|---|---|
| `microsphere-java-parent` | `pom` | Shared parent: imports `microsphere-all-bom:0.3.12` and `spring-framework-bom` |
| `microsphere-java-dependencies` | `pom` (BOM) | Manages the seven jar modules at `${revision}` |
| `microsphere-java-annotations` | `jar` | Common source/stability annotations + `@ConfigurationProperty` |
| `microsphere-java-core` | `jar` | ~355 sources: all utility packages |
| `microsphere-jdk-tools` | `jar` | In-process `javax.tools.JavaCompiler` wrapper |
| `microsphere-java-test` | `jar` | Shared test fixtures (models, services, annotations) |
| `microsphere-annotation-test` | `jar` | JUnit 5 harness that runs real annotation processing inside a test |
| `microsphere-lang-model` | `jar` | Helpers over `javax.lang.model.*` |
| `microsphere-annotation-processor` | `jar` | Compile-time `@ConfigurationProperty` metadata generator |

> [!NOTE]
> The BOM manages **exactly** these seven artifacts: `microsphere-java-annotations`, `microsphere-java-core`,
> `microsphere-jdk-tools`, `microsphere-java-test`, `microsphere-annotation-test`, `microsphere-lang-model`,
> `microsphere-annotation-processor`. It does not manage itself, the parent, or the aggregator.

---

## 3. Maven

### 3.1 Import the BOM

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

### 3.2 Declare only what you need (no versions)

```xml
<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-java-core</artifactId>
    </dependency>

    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-java-annotations</artifactId>
    </dependency>

    <!-- Compile-time only; see the Annotation Processing page -->
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

    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-annotation-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Without the BOM, pin versions explicitly:
`io.github.microsphere-projects:microsphere-java-core:0.3.19`.

### 3.3 Dependency graph you should expect

* `microsphere-java-core` → `microsphere-java-annotations` (compile);
  `javax.annotation-api`, `slf4j-api`, `commons-logging` (all `optional`).
* `microsphere-lang-model` → `microsphere-java-core`.
* `microsphere-jdk-tools` → `microsphere-java-core`.
* `microsphere-annotation-processor` → `microsphere-java-core` + `microsphere-lang-model`.
  Its build **shades `microsphere-java-core` into the processor jar** (via `maven-shade-plugin`) so the processor
  is self-contained; `microsphere-lang-model` stays a normal transitive dependency.
* `microsphere-annotation-test` → `microsphere-java-test` + `microsphere-jdk-tools` (non-optional), and
  JUnit/Mockito/Logback/Spring/JAX-RS as `optional` compile deps — **declare those yourself**.
* `microsphere-java-test` declares every dependency as `optional`; a consumer must supply JUnit Jupiter,
  Mockito, Logback, `javax.ws.rs-api`, `jaxws-api`, `spring-context`, `spring-web` for the types that reference them.

---

## 4. Gradle

```groovy
dependencies {
    implementation platform("io.github.microsphere-projects:microsphere-java-dependencies:0.3.19")

    implementation "io.github.microsphere-projects:microsphere-java-core"
    implementation "io.github.microsphere-projects:microsphere-java-annotations"

    annotationProcessor "io.github.microsphere-projects:microsphere-annotation-processor"

    testImplementation "io.github.microsphere-projects:microsphere-java-test"
    testImplementation "io.github.microsphere-projects:microsphere-annotation-test"
}
```

`annotationProcessor` (or `compileOnly` + `annotationProcessor`) is what activates the metadata generator; see
[Annotation Processing](annotation-processing.md).

---

## 5. Version alignment with Spring

`microsphere-java-parent` imports `spring-framework-bom` and switches the resolved line by JDK:

| JDK | Spring Framework | JUnit Jupiter / Mockito |
|---|---|---|
| `[1.8, 17)` — profile `java8-16` | `5.3.39` | `5.14.4` / `4.11.0` |
| `[17, ∞)` | `7.0.9` | `6.1.3` / `5.23.0` |

> [!IMPORTANT]
> This is *build-time* alignment inside this repository, not a requirement on your application.
> `microsphere-java-core` does not depend on Spring at all. Only `microsphere-java-test` references Spring types,
> and it marks those dependencies `optional`.

---

## 6. Building this repository from source

```bash
git clone https://github.com/microsphere-projects/microsphere-java.git
cd microsphere-java

# 1. Fast first build — downloads dependencies, skips tests
./mvnw package -DskipTests

# 2. Full test run
./mvnw test

# 3. As CI does (adds checkstyle/failsafe wiring + JaCoCo coverage)
./mvnw test --activate-profiles test,coverage

# 4. One module
./mvnw test -pl microsphere-java-core

# 5. One test class
./mvnw test -pl microsphere-java-core -Dtest=StringUtilsTest
```

On Windows use `mvnw.cmd`. The wrapper is `only-script` type
(`wrapperVersion=3.3.4`, Maven `apache-maven-3.9.16`).

### 6.1 JDK 16+ specifics

Handled automatically by profiles inherited from `microsphere-build`:

| Profile | Activation | Effect |
|---|---|---|
| `java9+` | `[9,)` | sets `maven.compiler.release=${java.version}` (8) |
| `java16+` | `[16,)` | forks the compiler with `-J--add-opens=java.base/java.lang=ALL-UNNAMED` and `-J--add-opens=java.base/java.lang.invoke=ALL-UNNAMED`; sets `jvm.argLine` to the same two `--add-opens` and feeds it to Surefire `<argLine>` |
| `java9-15` | `[9,15]` | `jvm.argLine=--illegal-access=permit` |

You do **not** need to add flags manually. If you see reflective-access failures on a JDK you added to the matrix
yourself, add the two `--add-opens` above to your own `argLine`.

### 6.2 What CI runs

`.github/workflows/maven-build.yml` (JDK 8, 11, 17, 21, 25):

```bash
mvn --batch-mode --update-snapshots --file pom.xml \
    -Drevision=0.0.1-SNAPSHOT \
    -Dsurefire.useSystemClassLoader=false \
    test --activate-profiles test,coverage
```

`.github/workflows/maven-publish.yml` deploys on pushes to `release` (JDK 11):

```bash
./mvnw --batch-mode --update-snapshots --file pom.xml \
    -Drevision=${revision} -Dgpg.skip=true \
    deploy --activate-profiles publish,ci
```

Artifacts are published to Maven Central through `central-publishing-maven-plugin` (`server-id: ossrh`,
`autoPublish=true`). There is no `<distributionManagement>` block in this repository — publishing is
plugin-driven.

### 6.3 Test naming

Surefire includes `**/*Test.java` and `**/*Tests.java`, and **excludes `**/Abstract*.java`** — which is why the
shared harness is named `AbstractAnnotationProcessingTest`.

---

## 7. Versions

* `${revision}` in the working tree is `0.3.20-SNAPSHOT`; never edit it by hand — it drives every module version.
* `release-notes.md` is **appended** by the publish workflow, so its *oldest* entry (`v0.2.7`) is at the top and
  the *newest* (`v0.3.19`) is at the bottom.
* `onboarding-plan.md` is a contributor walkthrough; note that its stated revision (`0.3.4-SNAPSHOT`) is stale.

---

## 8. Next steps

* [Annotations](annotations.md) — five minutes, useful in any codebase.
* [Core Utilities](core-utilities.md) — the page you will link most often.
* [Reference](reference.md) — every SPI file and every tunable in one place.

[← Index](README.md) · [Next: Annotations →](annotations.md)
