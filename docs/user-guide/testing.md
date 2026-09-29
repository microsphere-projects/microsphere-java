# Testing Support

Two modules exist to make testing easier — for the library's own test suite and for yours:

| Module | Artifact | Purpose |
|---|---|---|
| `microsphere-java-test` | `io.github.microsphere-projects:microsphere-java-test` | Reusable fixtures: a model type hierarchy, an over-annotated service class, a multi-typed `@TestAnnotation`, plus `junit-platform.properties` and `logback-test.xml` that ship inside the jar |
| `microsphere-annotation-test` | `io.github.microsphere-projects:microsphere-annotation-test` | JUnit 5 harness (`AbstractAnnotationProcessingTest`) that runs a **real** annotation processing round inside a test method, using the in-process `javax.tools` compiler |

Both are versioned by `${revision}` and both are managed by `microsphere-java-dependencies`, so with the BOM imported you
can declare them without a version:

```xml
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
```

---

## 1. `microsphere-java-test`: the fixtures

### 1.1 Every dependency is `optional`

This is the single most important fact about the module. Its `pom.xml` marks **all nine** of its dependencies
`<optional>true</optional>`:

`microsphere-java-annotations`, `junit-jupiter`, `junit-jupiter-engine`, `mockito-core`, `logback-classic`,
`javax.ws.rs-api`, `jaxws-api`, `spring-context`, `spring-web`.

Optional dependencies do not reach the consumer's classpath, so you must declare whatever the fixtures reference
yourself. Otherwise you get `NoClassDefFoundError` at runtime, not at compile time of your own code:

| Fixture | Needs on your test classpath |
|---|---|
| `io.microsphere.test.service.TestService` | `javax.ws.rs-api` (`@Path`, `@GET`, `@PathParam`, `@DefaultValue`) |
| `io.microsphere.test.service.TestServiceImpl` | `spring-context` (`@Service`, `@Autowired`, `@ComponentScan(s)`, `@Cacheable`), `jaxws-api` (`@ServiceMode`), plus `javax.ws.rs-api` transitively through the interface |
| `io.microsphere.test.annotation.TestAnnotation` | `microsphere-java-annotations` |
| everything | `junit-jupiter` if you use the shipped `junit-platform.properties` meaningfully |

`microsphere-annotation-test` declares `microsphere-java-test` and `microsphere-jdk-tools` as ordinary
(non-optional) compile dependencies, so using the harness pulls the fixtures in — but still not their optional deps.

### 1.2 `io.microsphere.test.model` — type-hierarchy fixtures

These exist so generic-type resolution, bean introspection and conversion tests have a stable, non-trivial shape to
resolve against.

| Type | Declaration | Why it is there |
|---|---|---|
| `Ancestor` | `class Ancestor implements Serializable` — `boolean z` (`isZ`/`setZ`) | top of the `Model` hierarchy |
| `Parent` | `class Parent extends Ancestor` — `byte b`, `short s`, `int i`, `long l` | middle level, inherited-property tests |
| `Model` | `class Model extends Parent` — `float f`, `double d`, `TimeUnit tu`, `String str`, `BigInteger bi`, `BigDecimal bd` | leaf with enum + arbitrary-precision types |
| `Color` | `enum Color { RED(1), YELLOW(2), BLUE(3) }` with `int getValue()` | enum conversion tests |
| `PrimitiveTypeModel` | eight `boolean/byte/char/short/int/long/float/double` fields, **getters only, no setters** | write-once/read-only bean case |
| `SimpleTypeModel` | boxed counterparts plus `String`, `BigDecimal`, `BigInteger`, `Date` — and `private int invalid` with **no accessor at all** | "simple type" detection, and a field that must not surface as a property |
| `ArrayTypeModel` | `int[]`, `String[]`, `PrimitiveTypeModel[]`, `Model[]`, `Color[]` | generic-array resolution |
| `CollectionTypeModel` | `Collection<String>`, `List<Color>`, `Queue<PrimitiveTypeModel>`, `Deque<Model>`, `Set<Model[]>` | nested/collection type arguments |
| `MapTypeModel` | `Map`, `SortedMap`, `NavigableMap`, `HashMap`, `TreeMap` with mixed value types | map key/value type arguments |
| `StringArrayList` | `class StringArrayList extends ArrayList<String>` | the canonical case for `resolveActualTypeArgumentClass` |
| `ConfigurationPropertyModel` | five `@ConfigurationProperty(name = "microsphere.annotation.processor.model.*")` fields: `name`, `type`, `defaultValue`, `required`, `description` | input for `ConfigurationPropertyAnnotationProcessor` tests |

### 1.3 `io.microsphere.test.service` — reflection/JAX-RS fixtures

```java
@Path("/echo")
public interface TestService {

    @GET
    <T> String echo(@PathParam("message") @DefaultValue("mercyblitz") String message);

    @POST
    Model model(@PathParam("model") Model model);

    @PUT
    String testPrimitive(boolean z, int i);

    @PUT
    Model testEnum(TimeUnit timeUnit);

    @GET
    String testArray(String[] strArray, int[] intArray, Model[] modelArray);
}
```

* `echo` is deliberately **generic** (`<T>` with an unused variable) — it exercises type-variable handling.
* `DefaultTestService implements TestService` returns `"[ECHO] " + message` from `echo`, `null` from the rest, and has
  an unused `private String name` field.
* `GenericTestService extends DefaultTestService implements TestService, java.util.EventListener` — the marker
  interface makes it a raw-type/wildcard test subject as well as an `EventListener` implementation.
* `TestServiceImpl extends GenericTestService implements TestService, AutoCloseable, Serializable` is the
  "everything at once" fixture used by the annotation-processor and lang-model tests: `@Service("testService")`,
  `@ServiceMode`, a nested `@ComponentScans` with `@ComponentScan.Filter(type = ASPECTJ, ...)`, a fully populated
  `@TestAnnotation`, an `@Autowired` protected field plus an `@Autowired` constructor parameter, and `@Cacheable` on
  `echo`.

### 1.4 `@TestAnnotation`

Every annotation-member type, so annotation-reading code has one thing to read everything from:

```java
@Retention(RUNTIME)
@Target(TYPE)
@Documented
public @interface TestAnnotation {

    boolean z() default false;
    char c() default 'a';
    byte b() default 1;
    short s() default 2;
    int i() default 3;
    long l() default 4L;
    float f() default 5.0f;
    double d() default 6.0d;
    String string() default "string";
    Class<?> type() default String.class;
    Class<?>[] types() default {String.class, Integer.class};
    TimeUnit timeUnit() default DAYS;
    Since since();                              // no default — always required
    ConfigurationProperty[] properties() default {};   // nested annotation array
}
```

> [!IMPORTANT]
> `since()` has **no default**, so `@TestAnnotation` is never usable without it — see the `since = @Since("1.0.0")` in
> `TestServiceImpl`. `type()`/`types()` default to `String.class` and `{String.class, Integer.class}`.

### 1.5 Resources that ship in the jar

`microsphere-java-test/src/main/resources/` (i.e. `main`, not `test`) contains two files that land on **your**
classpath as soon as you depend on the artifact:

**`junit-platform.properties`** — one line:

```properties
junit.jupiter.extensions.autodetection.enabled = true
```

> [!WARNING]
> This changes JUnit behaviour for your whole test suite, not only for microsphere tests: any
> `org.junit.jupiter.api.extension.Extension` implementation with a `META-INF/services/` entry is now registered
> automatically. `microsphere-java-core`'s own tests rely on this (see §3.3). If you do not want auto-detection,
> override the property in your own `src/test/resources/junit-platform.properties` — the first entry on the
> classpath wins, and test resources precede dependency jars.

**`logback-test.xml`** — root at `INFO` to console, and:

```xml
<logger name="io.microsphere" level="TRACE" additivity="false">
    <appender-ref ref="ASYNC"/>
</logger>
```

`ASYNC` wraps a `FileAppender` writing `test.log` with `append=false` and `immediateFlush=false`. Two practical
consequences: every `io.microsphere` logger goes to **TRACE** in your tests, and a `test.log` appears in the *working
directory* of each test run (you can see stray ones in this repository's module folders). Add `test.log` to your
`.gitignore`, or supply your own `logback-test.xml` to override it.

---

## 2. `microsphere-annotation-test`: the processing harness

### 2.1 The contract

```java
@ExtendWith(CompilerInvocationInterceptor.class)
public abstract class AbstractAnnotationProcessingTest {

    // --- wired by the harness before your @Test body runs ---
    protected RoundEnvironment roundEnv;
    protected ProcessingEnvironment processingEnv;
    protected Elements elements;
    protected Types types;
    protected Class<?> testClass;
    protected String testClassName;
    protected TypeElement testTypeElement;
    protected TypeMirror testTypeMirror;
    protected DeclaredType testDeclaredType;

    // --- hooks you may override ---
    protected void addCompiledClasses(Set<Class<?>> compiledClasses) {}
    protected void initTestClass(Class<?> testClass) { /* default: see below */ }
    protected void beforeTest(ReflectiveInvocationContext<Method> invocationContext,
                              ExtensionContext extensionContext) {}
    protected void afterTest(ReflectiveInvocationContext<Method> invocationContext,
                             ExtensionContext extensionContext,
                             @Nullable Object result, @Nullable Throwable failure) {}
}
```

`CompilerInvocationInterceptor` is **package-private** — you never reference it directly; extending the base class is
the only supported entry point.

### 2.2 What actually happens per test method

1. The interceptor (a JUnit `InvocationInterceptor`) collects a `LinkedHashSet<Class<?>>` containing **the test class
   itself**, then calls your `addCompiledClasses(set)` so you can add more.
2. It builds an in-process `io.microsphere.jdk.tools.compiler.Compiler`, calls `sourcePaths(compiledClasses)` and
   `processors(...)`, then `compile(compiledClasses)`.
3. Processor order: `AnnotationProcessingTestProcessor` **first**, then every `javax.annotation.processing.Processor`
   found by `ServiceLoader.load(Processor.class, testClass.getClassLoader())`.
4. During the first round in which `!roundEnv.processingOver()`, the harness:
   * `prepare(...)` — assigns `roundEnv`, `processingEnv`, `elements` (`processingEnv.getElementUtils()`), `types`
     (`processingEnv.getTypeUtils()`), then calls `initTestClass(invocationContext.getTargetClass())`;
   * calls `beforeTest(...)`;
   * calls `invocation.proceed()` — **your `@Test` body executes here**;
   * calls `afterTest(..., result, failure)` in a `finally` block;
   * if the body threw, rethrows `ExceptionUtils.wrap(getRootCause(failure), Error.class)`.
5. `AnnotationProcessingTestProcessor` is `@SupportedAnnotationTypes("*")` with
   `getSupportedSourceVersion() == SourceVersion.latestSupported()`, and `process(...)` returns `false` — it never
   claims the annotations, so the production processors registered in step 3 still see them.

```
compile(test classes + addCompiledClasses)
   └─ round 1 ──► AnnotationProcessingTestProcessor.process()
                    ├─ init fields + initTestClass(testClass)
                    ├─ beforeTest()
                    ├─ invocation.proceed()  ← your @Test body
                    ├─ afterTest(result, failure)
                    └─ (throw → wrapped as Error)
   └─ round n ──► same body runs again (see warning below)
   └─ processingOver() ──► skipped
```

### 2.3 Default `initTestClass` — and why you usually override it

```java
protected void initTestClass(Class<?> testClass) {
    this.testClass = testClass;
    this.testClassName = testClass.getName();
    this.testTypeElement = this.elements.getTypeElement(this.testClassName);
    this.testTypeMirror = this.testTypeElement.asType();
    this.testDeclaredType = (DeclaredType) this.testTypeMirror;
}
```

The default targets the **test class itself**. `initTestClass` does not check for `null`, so if the class under
investigation is not the test class you must call it again — the established pattern is to do it from `beforeTest`,
exactly as `io.microsphere.lang.model.util.UtilTest` does:

```java
@Override
protected void beforeTest(ReflectiveInvocationContext<Method> invocationContext,
                          ExtensionContext extensionContext) {
    initTestClass(TestServiceImpl.class);   // now testTypeElement is the fixture, not the test
}
```

`initTestClass` is safe to call repeatedly, and re-running it is what you want when a fixture's elements are needed.

### 2.4 Null/empty constants

The base class exposes `protected static final` sentinel constants so null-argument tests read cleanly:

| Kind | Constants |
|---|---|
| `TypeMirror` | `NULL_TYPE_MIRROR`, `EMPTY_TYPE_MIRROR_ARRAY`, `NULL_TYPE_MIRROR_ARRAY` |
| `Element` | `NULL_ELEMENT`, `EMPTY_ELEMENT_ARRAY`, `NULL_ELEMENT_ARRAY`, `NULL_TYPE_ELEMENT`, `NULL_ANNOTATED_CONSTRUCT`, `NULL_FIELD` (`VariableElement`), `NULL_METHOD` / `NULL_METHOD_ARRAY` (`ExecutableElement`) |
| `ElementKind` / `Modifier` | `NULL_ELEMENT_KIND`, `NULL_MODIFIER`, `NULL_MODIFIER_ARRAY` |
| `AnnotationMirror` | `NULL_ANNOTATION_MIRROR` |
| `java.lang.reflect.Type` | `NULL_TYPE`, `NULL_TYPE_ARRAY`, `EMPTY_TYPE_ARRAY` |
| `Class` | `NULL_CLASS`, `NULL_CLASS_ARRAY` |
| `Collection` / `List` | `NULL_COLLECTION`, `NULL_LIST`, `EMPTY_COLLECTION_ARRAY` |
| `String` | `NULL_STRING`, `NULL_STRING_ARRAY` |
| `Predicate` | `NULL_PREDICATE_ARRAY` |
| `ProcessingEnvironment` | `NULL_PROCESSING_ENVIRONMENT` |

Note the raw types: `NULL_LIST` and `NULL_COLLECTION` are declared as raw `List` / `Collection[]`-style constants, and
`EMPTY_COLLECTION_ARRAY` is a `Collection[0]`, not a typed list.

### 2.5 Caveats worth knowing

> [!WARNING]
> **Your test body runs once per processing round.** Steps 4a–4e happen for *every* round where
> `!roundEnv.processingOver()`. If a production processor registered in step 3 generates sources that trigger a
> second round, your `@Test` method executes twice — with `initTestClass`, `beforeTest` and `afterTest` re-invoked
> each time. Keep assertions idempotent, or don't add processors that generate sources to the test classpath.

> [!WARNING]
> **Production processors on the test classpath really do run.** `microsphere-annotation-processor`'s tests compile
> against that module, whose `META-INF/services/javax.annotation.processing.Processor` is discovered in step 3, so
> `ConfigurationPropertyAnnotationProcessor` processes the fixture classes and writes
> `META-INF/microsphere/configuration-properties.json` into the compiler output directory.

> [!NOTE]
> **Failures surface as `java.lang.Error`.** The harness takes `getRootCause` of whatever your body threw and wraps it
> into an `Error`, so a JUnit `AssertionFailedError` may be reported with a less familiar wrapper type. `afterTest`
> still receives the original `Throwable`, which is the reliable place to inspect it.

> [!NOTE]
> **Only the classes you list are compiled.** Everything else — the JDK, your dependencies — must already be on the
> classpath; `addCompiledClasses` is for *source* subjects whose `TypeElement`s, Javadoc or source positions matter.
> See the JDK requirement and `-proc`/shading notes in [Annotation Processing](annotation-processing.md).

### 2.6 A complete, working example

Verified against `microsphere-annotation-processor`'s and `microsphere-lang-model`'s own tests:

```java
package com.example.processor;

import io.microsphere.test.annotation.processing.AbstractAnnotationProcessingTest;
import io.microsphere.test.model.ConfigurationPropertyModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import javax.lang.model.element.TypeElement;
import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MyProcessorTest extends AbstractAnnotationProcessingTest {

    @Override
    protected void addCompiledClasses(Set<Class<?>> compiledClasses) {
        compiledClasses.add(ConfigurationPropertyModel.class);   // compiled from source, so Javadoc is available
    }

    @Override
    protected void beforeTest(ReflectiveInvocationContext<Method> invocationContext,
                              ExtensionContext extensionContext) {
        // point the exposed type fields at the fixture instead of this test class
        initTestClass(ConfigurationPropertyModel.class);
    }

    @Test
    void testResolve() {
        TypeElement typeElement = super.testTypeElement;
        assertNotNull(typeElement);

        MyProcessor processor = new MyProcessor();
        processor.init(super.processingEnv);          // processingEnv is already initialised
        processor.process(java.util.Collections.emptySet(), super.roundEnv);
    }
}
```

And the lighter-weight form used when you only need a live `ProcessingEnvironment` — the actual
`ConfigurationPropertyAnnotationProcessorTest` in this repository:

```java
class ConfigurationPropertyAnnotationProcessorTest extends AbstractAnnotationProcessingTest {

    @Test
    void testResolveMetadataOnEmptySet() {
        ConfigurationPropertyAnnotationProcessor processor = new ConfigurationPropertyAnnotationProcessor();
        processor.init(super.processingEnv);
        processor.resolveMetadata(emptySet());
        String json = processor.toJSON();
        assertNotNull("[]", json);
    }
}
```

`ResourceProcessorTest` in the same module demonstrates the cleanup hook — resources written through
`processingEnv.getFiler()` are deleted in `afterTest`:

```java
@Override
protected void afterTest(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext,
                         Object result, Throwable failure) {
    this.classOutputProcessor.getResource(this.randomResourceName, FOR_WRITING).ifPresent(FileObject::delete);
}
```

### 2.7 Alternative: drive the compiler yourself

For cases the harness does not fit (asserting on diagnostics, compiling arbitrary source strings), use
`io.microsphere.jdk.tools.compiler.Compiler` directly — full API on the
[Annotation Processing](annotation-processing.md) page.

---

## 3. Test conventions in this repository

### 3.1 Layout and naming

* Standard Maven layout: `microsphere-java-core/src/test/java` mirrors the main package tree. Roughly 314 test classes
  live in `microsphere-java-core`, 17 in `microsphere-lang-model`, 6 in `microsphere-java-annotations`, 4 in
  `microsphere-annotation-processor`, 1 in `microsphere-jdk-tools`.
* Test classes are package-private (`class StringUtilsTest`), methods follow `test<Unit>` / `test<Unit>On<Case>`:
  `testFindEventTypeOnNull`, `testDeclaredMethodsOnNull`, `testProcessInResourceInputStreamOnFailed`.
* Surefire (configured by the external `microsphere-build` parent) includes `**/*Tests.java` and `**/*Test.java` and
  **excludes `**/Abstract*.java`** — which is why abstract bases such as `UtilTest` and `AbstractAnnotationProcessingTest`
  never run on their own, and why a class named `AbstractFooTest` would be silently skipped.
* JMH benchmarks are named `*Benchmark` and are therefore **not** executed by Surefire; they exist for manual runs
  (`ListsBenchmark`, `ClassUtilsBenchmark`, `MethodHandleUtilsBenchmark`, `ReflectionUtilsBenchmark`).

### 3.2 Running tests

```bash
# whole reactor
./mvnw test

# as CI does (JDK 8/11/17/21/25 matrix): adds Checkstyle + Failsafe + JaCoCo
mvn --batch-mode --update-snapshots --file pom.xml \
    -Drevision=0.0.1-SNAPSHOT \
    -Dsurefire.useSystemClassLoader=false \
    test --activate-profiles test,coverage

# one module, one class
./mvnw test -pl microsphere-java-core -Dtest=StringUtilsTest

# one method (Surefire accepts a pattern)
./mvnw test -pl microsphere-java-core -Dtest=StringUtilsTest#testTrim*
```

`-Dsurefire.useSystemClassLoader=false` is what `.github/workflows/maven-build.yml` passes; keep it when reproducing a
CI failure locally, because classloader- and `URL`-based tests (`ClassLoaderUtils`, `ClassPathUtils`, `URLClassPath`,
`net`) behave differently under Surefire's isolated classloader.

### 3.3 The utilities-class contract is enforced by a JUnit extension

`microsphere-java-core/src/test/java/io/microsphere/junit/jupiter/api/extension/UtilsTestBeforeAllExtension.java` is
a `BeforeAllCallback` registered through
`src/test/resources/META-INF/services/org.junit.jupiter.api.extension.Extension`. It works because
`microsphere-java-test`'s `junit-platform.properties` turns on extension auto-detection.

For each test class it derives the subject name by cutting the class name at the first `"Test"`
(`io.microsphere.util.StringUtilsTest` → `io.microsphere.util.StringUtils`), and if that class exists and implements
the `io.microsphere.util.Utils` marker it asserts:

* the class is `public`;
* the class is `abstract`;
* it declares **exactly one** constructor;
* that constructor is `private` and takes **no parameters**.

So when you add a new `XxxUtils implements Utils`, name its test `XxxUtilsTest` in the mirrored package and the shape
checks itself. If you deliberately want a stateful utility type, the extension will fail the build — that is the intent,
not a bug.

### 3.4 Test-scope SPI registrations

`microsphere-java-core/src/test/resources/META-INF/services/` deliberately registers test doubles, which is how the
SPI-loading code is exercised:

| Service file | Implementations |
|---|---|
| `io.microsphere.event.EventListener` | `EchoEventListener`, `EchoEventListener2` |
| `io.microsphere.logging.LoggerFactory` | `NoDelegateLoggerFactory` |
| `io.microsphere.metadata.ConfigurationPropertyLoader` | `Default…`, `Empty…`, `Null…`, `ErrorConfigurationPropertyLoader` |
| `io.microsphere.util.Utils` | `EchoEventListener` |
| `io.microsphere.util.Version` | `NotFoundVersion` |
| `java.net.URLStreamHandlerFactory` | `StandardURLStreamHandlerFactory` |
| `org.junit.jupiter.api.extension.Extension` | `UtilsTestBeforeAllExtension` |

Other fixtures under `microsphere-java-core/src/test/resources/` used by specific pages of this guide:
`META-INF/banned-artifacts` (classloading), `META-INF/microsphere/additional-configuration-properties.json`
(metadata), `META-INF/class-load-test.policy` (`java.security.policy` tests), `META-INF/MANIFEST.MF` and
`META-INF/maven/.../pom.properties` (artifact detection), `test/json/*.json`.

---

## 4. Adding tests for a new feature — checklist

1. Put the test in the mirrored package of the module you changed; name it `<Subject>Test` (Surefire will pick it up,
   `Abstract*` will not).
2. If the subject is a `Utils`-marked utility class, rely on `UtilsTestBeforeAllExtension` and cover null/empty input
   explicitly — the suite's convention is one `testXxxOnNull` / `testXxxOnEmpty` method per behaviour.
3. Need a fixture type? Reuse `io.microsphere.test.model` / `.service` before inventing one; if the fixture must be
   resolvable *from source*, add it to `addCompiledClasses`.
4. Touching SPI loading? Add a line to the matching `META-INF/services/` file under `src/test/resources` rather than
   mocking `ServiceLoader`.
5. Writing a processor test? Extend `AbstractAnnotationProcessingTest`; keep the body idempotent (§2.5).
6. Run the module locally (`./mvnw test -pl <module>`), then the CI command with the `test,coverage` profiles before
   opening a pull request.

---

← [Annotation Processing](annotation-processing.md) · [Index](README.md) · [Reference](reference.md) →
