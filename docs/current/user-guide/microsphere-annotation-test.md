# Annotation Test: The Annotation-Processing Test Harness

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-annotation-test` (`0.3.20-SNAPSHOT` on `release`, published as `0.3.20`) |
| **Packaging / name** | `jar`, `Microsphere :: Annotation :: Test` (`microsphere-annotation-test/pom.xml:16`, `:18`) |
| **Main types** | three, all in `io.microsphere.test.annotation.processing` (`microsphere-annotation-test/src/main/java/io/microsphere/test/annotation/processing/AbstractAnnotationProcessingTest.java:17`): `AbstractAnnotationProcessingTest` (**public**, `:54`), `AnnotationProcessingTestProcessor` (package-private, `AnnotationProcessingTestProcessor.java:46`), `CompilerInvocationInterceptor` (package-private, `CompilerInvocationInterceptor.java:40`) |
| **Compile dependencies** | `microsphere-java-test` (`microsphere-annotation-test/pom.xml:26`), `microsphere-jdk-tools` (`:33`) — both plain compile scope, `${revision}` at `:27`, `:34` |
| **Since** | `1.0.0` (`AbstractAnnotationProcessingTest.java:51`) |
| **JDKs tested** | Temurin 8 / 11 / 17 / 21 / 25 (`.github/workflows/maven-build.yml:25`, `distribution: 'temurin'` at `:33`) |

This module is the **test-support artifact** of the annotation-processing stack: it lets a `@Test` method run *inside a
real `javac` annotation-processing round*, with a live `ProcessingEnvironment`, `Elements`, `Types` and
`RoundEnvironment` handed to you as fields. You extend one class and write ordinary assertions.

> [!IMPORTANT]
> **One type is public; the other two are not, and that is the whole API surface you code against.**
> `AbstractAnnotationProcessingTest` is `public abstract` (`AbstractAnnotationProcessingTest.java:54`) and carries
> `@ExtendWith(CompilerInvocationInterceptor.class)` (`:53`). `AnnotationProcessingTestProcessor`
> (`AnnotationProcessingTestProcessor.java:46`) and `CompilerInvocationInterceptor`
> (`CompilerInvocationInterceptor.java:40`) are package-private — you cannot import, register or subclass them from
> your own package. Extending the base class is the only supported entry point.

**Who this is for:** library authors who ship an annotation processor, or who expose utilities over
`javax.lang.model` types (`Element`, `TypeMirror`, `AnnotationMirror`, …). Reflection-based tests cannot reach those
types — they only exist while a compiler is running. This harness is how `microsphere-lang-model` and
`microsphere-annotation-processor` test themselves (see §5).

---

## 1. What you get, and when you need it

You need this module when the thing under test **only exists during compilation**:

* **Testing an annotation processor.** Assert on what your processor generated, on how it reads
  `RoundEnvironment#getElementsAnnotatedWith`, or on the metadata it accumulates.
  `microsphere-annotation-processor` does exactly this
  (`microsphere-annotation-processor/src/test/java/io/microsphere/annotation/processor/ConfigurationPropertyAnnotationProcessorTest.java:34-43`).
* **Testing `javax.lang.model` utilities.** A `TypeMirror` for an arbitrary class is not obtainable outside a compiler
  session; `microsphere-lang-model`'s test base builds them from the injected `processingEnv`
  (`microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/UtilTest.java:55-69`).
* **Testing `Filer` / generated-file behaviour** with a real `Filer` instead of a mock
  (`microsphere-annotation-processor/src/test/java/io/microsphere/annotation/processor/FilerProcessorTest.java:51-55`).

What you get, per type:

| Type | Visibility | What it does for you |
|---|---|---|
| `AbstractAnnotationProcessingTest` | `public` (`:54`) | the base class you extend: injected state (`:110-126`), four hooks (`:133`, `:150`, `:161`, plus the concrete `initTestClass` at `:136`), 27 typed-null/empty constants (`:56-108`) |
| `AnnotationProcessingTestProcessor` | package-private (`:46`) | the `AbstractProcessor` injected into `javac`; it fills your fields and invokes your test method (`:66-100`) |
| `CompilerInvocationInterceptor` | package-private (`:40`) | the JUnit 5 `InvocationInterceptor` that builds the in-process `Compiler`, collects the classes to compile, registers the processors and starts the compilation (`:43-64`) |

The flow in one sentence: the interceptor adds your test class (and whatever `addCompiledClasses` returns) as
compilation units, injects `AnnotationProcessingTestProcessor` plus every `Processor` visible through SPI, calls
`Compiler.compile(...)` (`CompilerInvocationInterceptor.java:49-63`), and your `@Test` method body executes from inside
that processor's `process()` (`AnnotationProcessingTestProcessor.java:75`).

---

## 2. Coordinates and dependencies

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-annotation-test</artifactId>
    <version>0.3.20</version>
    <scope>test</scope>
</dependency>
```

Declare it in **test scope**. Both in-repo consumers do:
`microsphere-lang-model/pom.xml:52-57` (`<scope>test</scope>` at `:56`) and
`microsphere-annotation-processor/pom.xml:50-56` (`:55`). The version is managed by the reactor BOM at
`microsphere-java-dependencies/pom.xml:50`.

### 2.1 Version and coordinates, with evidence

| Item | Value | Evidence |
|---|---|---|
| Group id | `io.github.microsphere-projects` | root `pom.xml:12`, module `microsphere-annotation-test/pom.xml:13` |
| Artifact id | `microsphere-annotation-test` | `microsphere-annotation-test/pom.xml:14` |
| Source line (release branch) | `${revision}` = `0.3.20-SNAPSHOT` | `microsphere-annotation-test/pom.xml:15`, root `pom.xml:54` |
| Published version used in snippets | `0.3.20` | repository tag `0.3.20` |
| Module parent | `microsphere-java-parent`, `${revision}` | `microsphere-annotation-test/pom.xml:5-10` |
| Reactor parent | `io.github.microsphere-projects:microsphere-build:0.3.16` | root `pom.xml:6-10` |
| Reactor position | 7th of 9 modules, after `microsphere-java-test`, before `microsphere-lang-model` | root `pom.xml:58-66` (`microsphere-annotation-test` at `:64`) |

### 2.2 The dependency list, exactly as declared

| Dependency | POM lines | Scope / flag | Note |
|---|---|---|---|
| `microsphere-java-test` | `microsphere-annotation-test/pom.xml:24-28` | compile (no `<scope>` element) | `test`-oriented helpers; nothing in this module's main source imports from it |
| `microsphere-jdk-tools` | `:31-35` | compile (no `<scope>` element) | supplies `io.microsphere.jdk.tools.compiler.Compiler`, imported at `CompilerInvocationInterceptor.java:19` |
| `org.junit.jupiter:junit-jupiter` | `:38-42` | `<optional>true</optional>` (`:41`) | the JUnit 5 API this module's *main* source compiles against (`InvocationInterceptor`, `ExtendWith`) — **optional, so you must declare JUnit 5 yourself** |
| `org.mockito:mockito-core` | `:45-49` | optional (`:48`) | unused by the module's sources |
| `org.junit.jupiter:junit-jupiter-engine` | `:51-55` | optional (`:54`) | needed to run the tests |
| `ch.qos.logback:logback-classic` | `:58-62` | optional (`:61`) | logging backend for the compile output trace |
| `javax.ws.rs:javax.ws.rs-api` | `:65-69` | optional (`:68`) | fixture classes for processor tests |
| `javax.xml.ws:jaxws-api` | `:72-76` | optional (`:75`) | same |
| `org.springframework:spring-context` | `:79-83` | optional (`:82`) | same |
| `org.springframework:spring-web` | `:85-89` | optional (`:88`) | same |

Because `<optional>` dependencies are **not transitive**, adopting this module gives you JUnit 5, Mockito, Logback, the
JAX-RS/JAX-WS APIs and Spring only if you declare them. `microsphere-jdk-tools` and `microsphere-java-test` *are*
transitive (compile scope), which is why this artifact belongs in test scope in your POM.

---

## 3. Version information and JDK support

### 3.1 Bytecode level

| Item | Value | Evidence |
|---|---|---|
| `@since` on all three types | `1.0.0` | `AbstractAnnotationProcessingTest.java:51`, `AnnotationProcessingTestProcessor.java:43`, `CompilerInvocationInterceptor.java:38` |
| Release line | `release` branch, `${revision}` = `0.3.20-SNAPSHOT` | root `pom.xml:54` |
| Published tag | `0.3.20` | repository tag; group id root `pom.xml:12` |
| Bytecode level | **not declared in this reactor** — inherited from the external parent `microsphere-build:0.3.16` | root `pom.xml:6-10`; no `maven.compiler.*` / `<release>` property exists anywhere in the module tree, and `microsphere-annotation-test/pom.xml` has no `<build>` section at all (the file ends at `:93`) |

### 3.2 Supported JDKs, and how the harness survives the module system

`com.sun.tools.javac` and the old `jdk.tools`/`tools.jar` world behave very differently from JDK 9 onward: the compiler
moves into the `jdk.compiler` module, `ToolProvider` becomes the only supported way to reach it, and unread
`com.sun.tools.javac.*` packages require `--add-exports`. This harness sidesteps all of it:

| Mechanism | Code | Consequence across JDK 8 → 25 |
|---|---|---|
| No JDK-internal type is referenced anywhere in the module | the only JDK-tool import is `io.microsphere.jdk.tools.compiler.Compiler` (`CompilerInvocationInterceptor.java:19`); `com.sun.*`, `tools.jar`, `Class.forName`, `setAccessible` and `--add-exports` appear in **no** source file of `microsphere-annotation-test` | nothing to export, nothing to open — the same jar runs unmodified on every CI JDK |
| The compiler is obtained through `javax.tools` | `microsphere-jdk-tools` calls `ToolProvider.getSystemJavaCompiler()` (`microsphere-jdk-tools/src/main/java/io/microsphere/jdk/tools/compiler/Compiler.java:107`) and fails fast if it is `null` (`:108`) | JDK 8 resolves `lib/tools.jar`, JDK 9+ resolves the `jdk.compiler` provider; the branch lives in the JDK, not here |
| Source version is never pinned | `getSupportedSourceVersion()` returns `latestSupported()` (`AnnotationProcessingTestProcessor.java:102-105`, static import at `:37`) | no "supported source version RELEASE_x is less than -source" warning as CI moves 8 → 25 |
| All annotation types are claimed | `@SupportedAnnotationTypes(WILDCARD)` (`AnnotationProcessingTestProcessor.java:45`; `WILDCARD` = `"*"`, `microsphere-java-core/src/main/java/io/microsphere/constants/SymbolConstants.java:313`) | the processor participates in rounds whatever annotations the compiled sources carry |
| Processors are injected, not discovered by `javac` | `CompilerInvocationInterceptor.java:57-62` builds the list; `Compiler.java:162` calls `task.setProcessors(...)` | on JDK 9+ no `-processorpath` and no module-path processor lookup is involved |
| SPI processors come from **your** class loader | `ClassLoader classLoader = testClass.getClassLoader();` (`:48`) then `load(Processor.class, classLoader)` (`:60`) | the unnamed-module/test class loader is used even when the processor jar is a modular jar |
| No `module-info.java` | none exists in this module (`src/main/java/io/microsphere/test/annotation/processing/` holds exactly the three files) | always on the classpath, so no split-package concerns |

> [!WARNING]
> **A JDK is required, not a JRE.** The compilation is driven by
> `ToolProvider.getSystemJavaCompiler()` inside `microsphere-jdk-tools` (`Compiler.java:107`); on a JRE — or a `jlink`
> image without `jdk.compiler` — that returns `null` and `Compiler`'s constructor throws `IllegalArgumentException`
> (`:108`) before your test body ever runs.

> [!NOTE]
> CI pins the runtime: Temurin `java: [ '8' , '11' , '17' , '21' , '25' ]` (`.github/workflows/maven-build.yml:25`,
> `:33`) and the build passes `-Dsurefire.useSystemClassLoader=false` (`:43`). There is **no**
> `maven-surefire-plugin` configuration in any POM of this reactor, so that flag is CI-only: if you run these tests
> from an IDE or a build that keeps Surefire's system class loader, class visibility for the SPI-loaded processors
> (`CompilerInvocationInterceptor.java:48`, `:60`) is decided differently from CI.

---

## 4. API walkthrough

### 4.1 `AbstractAnnotationProcessingTest` — the class you extend

```java
// AbstractAnnotationProcessingTest.java:53-54
@ExtendWith(CompilerInvocationInterceptor.class)
public abstract class AbstractAnnotationProcessingTest {
```

Injected state, populated during the compile round (`AnnotationProcessingTestProcessor.java:95-99`), all `protected`
and non-final so subclasses read them freely (`AbstractAnnotationProcessingTest.java:110-126`):

| Field | Declaration | Filled from |
|---|---|---|
| `roundEnv` | `protected RoundEnvironment roundEnv` (`:110`) | the live round (`AnnotationProcessingTestProcessor.java:95`) |
| `processingEnv` | `protected ProcessingEnvironment processingEnv` (`:112`) | `AbstractProcessor#processingEnv` (`AnnotationProcessingTestProcessor.java:90`, `:96`) |
| `elements` | `protected Elements elements` (`:114`) | `processingEnv.getElementUtils()` (`:91`, `:97`) |
| `types` | `protected Types types` (`:116`) | `processingEnv.getTypeUtils()` (`:92`, `:98`) |
| `testClass` | `protected Class<?> testClass` (`:118`) | `invocationContext.getTargetClass()` (`:93`, `:99` → `initTestClass`) |
| `testClassName` | `protected String testClassName` (`:120`) | `testClass.getName()` (`:138`) |
| `testTypeElement` | `protected TypeElement testTypeElement` (`:122`) | `elements.getTypeElement(testClassName)` (`:139`) |
| `testTypeMirror` | `protected TypeMirror testTypeMirror` (`:124`) | `testTypeElement.asType()` (`:140`) |
| `testDeclaredType` | `protected DeclaredType testDeclaredType` (`:126`) | `(DeclaredType) testTypeMirror` — unchecked cast (`:141`) |

Four overridable members, plus one concrete helper:

```java
// AbstractAnnotationProcessingTest.java:133-134 — empty by default; add extra compilation units
protected void addCompiledClasses(@Nonnull Set<Class<?>> compiledClasses) {
}

// AbstractAnnotationProcessingTest.java:136-142 — concrete; re-point the test* fields at another class
protected void initTestClass(@Nonnull Class<?> testClass) {
    this.testClass = testClass;
    this.testClassName = testClass.getName();
    this.testTypeElement = this.elements.getTypeElement(this.testClassName);
    this.testTypeMirror = this.testTypeElement.asType();
    this.testDeclaredType = (DeclaredType) this.testTypeMirror;
}

// AbstractAnnotationProcessingTest.java:150-151 — runs once per non-final round, before your test method
protected void beforeTest(@Nonnull ReflectiveInvocationContext<Method> invocationContext, @Nonnull ExtensionContext extensionContext) {
}

// AbstractAnnotationProcessingTest.java:161-163 — always runs, even when the test method threw
protected void afterTest(@Nonnull ReflectiveInvocationContext<Method> invocationContext, @Nonnull ExtensionContext extensionContext,
                         @Nullable Object result, @Nullable Throwable failure) {
}
```

Hook call order, from the processor: `prepare(roundEnv)` (fills the fields, `AnnotationProcessingTestProcessor.java:70`)
→ `beforeTest` (`:73`) → `invocation.proceed()` i.e. **your test method** (`:75`) → `afterTest` in a `finally` (`:79`).
`result` is whatever your `void` test returned (`null` in practice) and `failure` is the raw throwable *before* any
re-wrapping (`:71-79`).

**The 27 typed constants** (`AbstractAnnotationProcessingTest.java:56-108`) exist so that a test can pass a *typed*
`null` to overloaded `javax.lang.model` helpers without a cast — e.g.
`assertTrue(getDeclaredMethods(NULL_TYPE_ELEMENT).isEmpty())`
(`microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/MethodUtilsTest.java:108`). They are plain
`protected static final` fields, and the module's own test asserts each one is `null` or empty
(`microsphere-annotation-test/src/test/java/io/microsphere/test/annotation/processing/AnnotationProcessingTest.java:48-74`).
Note that several are declared with **raw** types — `NULL_COLLECTION` (`:64`), `NULL_LIST` (`:66`), `NULL_CLASS`
(`:90`), `NULL_TYPE` (`:82`), `EMPTY_COLLECTION_ARRAY` (`:62`), `NULL_PREDICATE_ARRAY` (`:96`) — which is deliberate:
`Predicate[]` at `:96` cannot be genericised, and the raw form is what lets them be passed to arbitrary overloads.

### 4.2 `AnnotationProcessingTestProcessor` — the injected processor (package-private)

```java
// AnnotationProcessingTestProcessor.java:45-46
@SupportedAnnotationTypes(WILDCARD)
class AnnotationProcessingTestProcessor extends AbstractProcessor {
```

* Constructor `AnnotationProcessingTestProcessor.java:56-63` (package-private) captures four things: the test instance,
  the JUnit `Invocation<Void>`, the `ReflectiveInvocationContext<Method>` and the `ExtensionContext` (`:48-54`).
* `process(Set<? extends TypeElement>, RoundEnvironment)` (`:66-87`) is where your test actually runs. The
  `if (!roundEnv.processingOver())` guard (`:69`) means the test body is skipped in javac's final round; a compile that
  reaches only the final round therefore never executes your assertions (see pitfall 3).
* It returns `false` (`:86`) — the harness claims nothing, so other processors on the round keep working.
* `prepare(RoundEnvironment)` (`:89-100`) writes the fields listed in §4.1 and calls `initTestClass(testClass)` (`:99`).
* Failure path (`:81-84`): `getRootCause(failure)` (`:82`, `ThrowableUtils.java:47`) then
  `throw wrap(cause, Error.class)` (`:83`, `ExceptionUtils.java:98-104`). An `AssertionError` is already an `Error`, so
  `wrap` returns it unchanged (`ExceptionUtils.java:99-101`); any other throwable is rebuilt as a `java.lang.Error`
  carrying the message and the cause (`ExceptionUtils.java:304-308`) — the original type is lost.

### 4.3 `CompilerInvocationInterceptor` — the JUnit 5 hook (package-private)

```java
// CompilerInvocationInterceptor.java:40-44
class CompilerInvocationInterceptor implements InvocationInterceptor {

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
```

Line by line (`:45-63`):

| Lines | What happens |
|---|---|
| `:45` | `newLinkedHashSet()` — an insertion-ordered, de-duplicating set of classes to compile (core `SetUtils.java:491`) |
| `:46` | the test instance, from `invocationContext.getTarget().get()` |
| `:47-48` | `extensionContext.getTestClass().get()` and **its** class loader |
| `:49` | the test class itself is always a compilation unit |
| `:50` | `test.addCompiledClasses(compiledClassesSet)` — your hook gets its chance to add more |
| `:52` | flattened to `Class<?>[]` via core `ArrayUtils.EMPTY_CLASS_ARRAY` (`ArrayUtils.java:157`) |
| `:54-55` | `new Compiler()` + `sourcePaths(compiledClasses)` — source directories detected from those classes' code sources (`Compiler.java:121-134`) |
| `:57-58` | `AnnotationProcessingTestProcessor` goes **first** in a `LinkedList` |
| `:60-61` | every SPI `Processor` visible to the test class loader is appended |
| `:62` | `compiler.processors(...)` — replaces `Compiler`'s empty default (`Compiler.java:136-139`) |
| `:63` | `compiler.compile(compiledClasses)` — the round trip; its `boolean` result is discarded |

> [!TIP]
> Processor order matters and is fixed by the harness: `AnnotationProcessingTestProcessor` is added before the SPI ones
> (`CompilerInvocationInterceptor.java:57-61`), and `javac` iterates processors in registration order. So your fields
> (`processingEnv`, `roundEnv`, …) are populated before your own processor runs in the same round.

---

## 5. Writing an annotation-processor test, step by step

All code below is copied from consuming modules in the `release` tree.

### Step 1 — declare the harness in test scope

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-annotation-test</artifactId>
    <version>0.3.20</version>
    <scope>test</scope>
</dependency>
```

(Plus JUnit 5 yourself — `junit-jupiter` is `<optional>` at `microsphere-annotation-test/pom.xml:41`.)

### Step 2 — extend the base class and write ordinary assertions

From `microsphere-annotation-processor/src/test/java/io/microsphere/annotation/processor/ConfigurationPropertyAnnotationProcessorTest.java:34-43`:

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

No `@ExtendWith`, no compiler setup, no mocks: `super.processingEnv` is already a real environment by the time the
method body runs (`AnnotationProcessingTestProcessor.java:96`).

### Step 3 — add the extra classes your processor must see

Anything reachable only as a class file may be invisible to your processor's model of the round; name it as a
compilation unit. From `microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/MethodUtilsTest.java:82-85`:

```java
@Override
protected void addCompiledClasses(Set<Class<?>> compiledClasses) {
    compiledClasses.add(PropertyConstants.class);
}
```

and from `microsphere-annotation-processor/src/test/java/io/microsphere/annotation/processor/ConfigurationPropertyJSONElementVisitorTest.java:46-55`:

```java
@Override
protected void addCompiledClasses(Set<Class<?>> compiledClasses) {
    compiledClasses.add(ManifestArtifactResourceResolver.class);
    compiledClasses.add(IOUtils.class);
    compiledClasses.add(StandardFileWatchService.class);
    compiledClasses.add(TypeUtils.class);
    compiledClasses.add(ServiceLoaderUtils.class);
    compiledClasses.add(MethodUtils.class);
    compiledClasses.add(ConfigurationProperty.class);
}
```

`microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/TypeUtilsTest.java:188-194` is the same pattern for
test fixtures (`ArrayTypeModel`, `CollectionTypeModel`, `Color`, `MapTypeModel`).

### Step 4 — build per-test objects in `beforeTest`, not in a `@BeforeEach`

From `microsphere-annotation-processor/src/test/java/io/microsphere/annotation/processor/FilerProcessorTest.java:43-48`:

```java
private FilerProcessor processor;

@Override
protected void beforeTest(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) {
    this.processor = new FilerProcessor(super.processingEnv);
}
```

And from `microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/UtilTest.java:50-53` — re-targeting the
`test*` fields onto the class under analysis instead of the test class:

```java
@Override
protected void beforeTest(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) {
    initTestClass(TestServiceImpl.class);
}
```

### Step 5 — assert against the injected model

`UtilTest` then exposes the whole model surface to its 14 subclasses
(`microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/UtilTest.java:55-92`), which is why subclasses look
like plain unit tests — `microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/ClassUtilsTest.java:38-46`:

```java
@Test
void testGetClassName() {
    assertEquals(this.testClassName, getClassName(this.testTypeMirror));
}

@Test
void testLoadClassOnTypeMirror() {
    assertSame(this.testClass, loadClass(this.testTypeMirror));
}
```

### Step 6 — use `afterTest` for teardown and failure inspection

From `microsphere-annotation-test/src/test/java/io/microsphere/test/annotation/processing/AnnotationProcessingTest.java:84-90`:

```java
@Override
protected void afterTest(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext, Object result, Throwable failure) {
    super.afterTest(invocationContext, extensionContext, result, failure);
    if (failure != null) {
        assertEquals("For testing", failure.getMessage());
    }
}
```

`afterTest` runs inside a `finally` (`AnnotationProcessingTestProcessor.java:78-80`), so it fires even when the test
method threw, and it sees the **raw** throwable — before `wrap(..., Error.class)` replaces it (`:83`).

### Step 7 — check that the compilation really happened

The harness discards `compile()`'s return value (`CompilerInvocationInterceptor.java:63`). If your assertions seem not
to run at all, run once with `TRACE` enabled on `io.microsphere.jdk.tools.compiler.Compiler`, where javac's captured
output is logged (`Compiler.java:165`), and verify the source file is on disk (§6, pitfalls 2 and 3).

---

## 6. Common pitfalls

1. **Never call `@ExtendWith(CompilerInvocationInterceptor.class)` yourself.** It is package-private
   (`CompilerInvocationInterceptor.java:40`), and the base class already declares it
   (`AbstractAnnotationProcessingTest.java:53`). Your extension must be the *test* class.
2. **Test sources must exist on disk, in a `target/classes` layout.** The compilation units are resolved from
   `Class` → `.java` (`Compiler.java:284-286`) by walking the class's code-source root (`Compiler.java:242-262`,
   `:264-271`). Classes whose source is not found are **silently dropped** (`Compiler.java:209-215`), so `addCompiledClasses`
   only helps for classes built from sources in the same reactor — and a test run from a packaged jar has no source to
   compile at all.
3. **A test that never enters a round passes vacuously.** `invocation.proceed()` is called only from inside
   `process()` while `!roundEnv.processingOver()` (`AnnotationProcessingTestProcessor.java:69-80`). If the compilation
   fails first — javac error, empty unit list, `getSystemJavaCompiler()` returning `null` (`Compiler.java:107`) — the
   interceptor simply returns (`CompilerInvocationInterceptor.java:63` ignores the result), JUnit records no failure,
   and your assertions never ran. Treat "green but suspicious" as a signal to check the TRACE log (step 7).
4. **Bodies run once per non-final round, not once per test.** `process()` is a per-round callback
   (`AnnotationProcessingTestProcessor.java:66`), and each non-final round re-executes `prepare` → `beforeTest` →
   `invocation.proceed()` (`:70-75`). A processor that generates new sources provokes further rounds, so your test body
   can execute several times: keep it idempotent, and don't accumulate into static state.
5. **`processingEnv`, `elements`, `types`, `roundEnv` are `null` before the round.** They are assigned at
   `AnnotationProcessingTestProcessor.java:95-98`, i.e. *during* `compiler.compile(...)`
   (`CompilerInvocationInterceptor.java:63`). JUnit `@BeforeAll`/`@BeforeEach` callbacks run before
   `interceptTestMethod` reaches that point, so they cannot use these fields — put that work in `beforeTest`
   (`AbstractAnnotationProcessingTest.java:150`).
6. **Constructor and field initialisers cannot use the model either.** Same timing: the test instance is constructed by
   JUnit before the compilation starts (`invocationContext.getTarget().get()` at `CompilerInvocationInterceptor.java:46`
   already returns a live instance).
7. **Non-assertion throwables lose their type.** `throw wrap(getRootCause(failure), Error.class)`
   (`AnnotationProcessingTestProcessor.java:82-83`) leaves `AssertionError` untouched (`ExceptionUtils.java:99-101`) but
   rebuilds anything else as `java.lang.Error` (`ExceptionUtils.java:102-103`, `:304-308`). Your `catch (MyException e)`
   around a *whole-test* expectation will not see `MyException` at the JUnit level; use `assertThrows` inside the body.
8. **SPI processors are pulled in whether you want them or not.** `load(Processor.class, classLoader)` and
   `forEach(processors::add)` (`CompilerInvocationInterceptor.java:60-61`) register every processor declared under
   `META-INF/services/javax.annotation.processing.Processor` on the test class path. That is what makes your own
   processor run; it is also how an unrelated third-party processor can start generating files into your test output.
9. **Generated output lands in one directory you do not control by default.** `Compiler()` writes both
   `CLASS_OUTPUT` and `SOURCE_OUTPUT` to `<root of Compiler.class>/target/generated-classes`
   (`Compiler.java:236-240`, `:176-177`) — i.e. the `microsphere-jdk-tools` module root inside a reactor build, and
   inside your local repository layout when the jar is resolved from disk. Construct the harness's compiler behaviour
   knowingly, or clean that directory.
10. **`initTestClass` can throw a bare NPE.** `elements.getTypeElement(name)` returns `null` for a name the compiler
    does not know, and the next line dereferences it (`AbstractAnnotationProcessingTest.java:139-140`); the
    `(DeclaredType)` cast at `:141` is unchecked. Passing a nested class whose binary name contains `$`, or a class not
    on the compile classpath, is how you hit it.
11. **Depend on it in test scope only.** Its two compile-scope dependencies
    (`microsphere-annotation-test/pom.xml:24-28`, `:31-35`) would otherwise drag JUnit-adjacent code and a compiler
    driver into your production classpath.

---

## 7. See also

* [microsphere-jdk-tools](microsphere-jdk-tools.md) — the `javax.tools` facade this harness drives (`Compiler.java:107`,
  `:156-167`)
* [microsphere-annotation-processor](microsphere-annotation-processor.md) — processors tested with this harness (§5)
* [microsphere-lang-model](microsphere-lang-model.md) — the `UtilTest` base class built on it
  (`microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/UtilTest.java:48`)
* [microsphere-java-test](microsphere-java-test.md) — the compile-scope dependency
  (`microsphere-annotation-test/pom.xml:26`)
* [Annotation Processing — User Guide](microsphere-java-core/annotation-processing.md) — the `javax.lang.model` layer the
  exposed fields belong to
* [microsphere-annotation-test — Developer Guide](../developer-guide/microsphere-annotation-test.md) — round-trip
  sequence diagram, hook structure, design patterns and the fragility review
* Chinese version: [中文用户指南](zh/microsphere-annotation-test.md)
