# JDK Tools: In-Process Compilation

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-jdk-tools` (`0.3.20-SNAPSHOT` on `release`, published as `0.3.20`) |
| **Types** | exactly **one** main type: `io.microsphere.jdk.tools.compiler.Compiler` (`src/main/java/io/microsphere/jdk/tools/compiler/Compiler.java:72`) |
| **Depends on** | `microsphere-java-core` only (`microsphere-jdk-tools/pom.xml:24-28`, version `${revision}`) |
| **JDKs tested** | Temurin 8 / 11 / 17 / 21 / 25 (`.github/workflows/maven-build.yml:25`, `:33`) |
| **Compiler API used** | `javax.tools.ToolProvider.getSystemJavaCompiler()` — imported at `Compiler.java:64`, called at `:107` |
| **Test surface** | `src/test/java/io/microsphere/jdk/tools/compiler/CompilerTest.java` (11 test methods) |

This module gives you a **portable handle on the JDK's own compiler**, so one line of code runs in-process
compilation on every JDK from 8 to 25. It is the layer the annotation-processing test stack sits on: it turns
`Class` objects into source files, runs `javac` inside your JVM, and hands the work to the annotation processors you
register.

> [!IMPORTANT]
> **This module does not touch `com.sun.tools.javac` internals.** There is no `tools.jar` path arithmetic, no
> `Class.forName("com.sun.tools.javac.…")`, no reflection anywhere in its main source. Portability is delegated
> entirely to the standard `javax.tools` API, which the JDK itself adapts per release. Everything in the per-JDK
> table below follows from that single call site plus the null check that guards it.

---

## 1. What you get, and when you need it

You need this module when a **test or tool must compile Java source inside the running JVM** rather than shelling out
to `javac`:

* **Annotation-processor tests.** Run your processor over real classes and assert on what it produced.
  `microsphere-annotation-test` does exactly this: `CompilerInvocationInterceptor` builds a `Compiler`, feeds it the
  test's compiled classes as source paths, registers the processor under test plus every `Processor` found through
  SPI, and calls `compile(...)`
  (`microsphere-annotation-test/src/main/java/io/microsphere/test/annotation/processing/CompilerInvocationInterceptor.java:54-63`).
* **Source-to-class generation pipelines** — generate a file, compile it, load it.
* **"Can this class still be compiled the way I expect?"** checks that must run on five different JDKs in CI.

What you get, concretely:

| Capability | Entry point |
|---|---|
| Locate the JDK compiler, fail loudly if absent | `new Compiler(...)` → `Compiler.java:103-109` |
| Compile given classes by their **source files** | `compile(Class<?>...)` → `Compiler.java:156-167` |
| Register annotation processors | `processors(Processor...)` → `:136-139` |
| Collect diagnostics | `diagnosticListener(...)` → `:141-144` |
| Discover the project root / source dir of any class | `detectRootDirectory`, `detectSourcePath` → `:264-271`, `:242-262` |
| Control javac options, locale, encoding | `options`, `locale`, `charset` → `:111-114`, `:146-154` |

The path-discovery statics (`detectClassPath` / `detectRootDirectory` / `detectSourcePath`) are the reason this
module exists at all: `javax.tools` compiles `File`s, and the caller normally holds `Class` objects. `Compiler` maps
each class to `<name>.java` (`resolveJavaSourceFileRelativePath`, `Compiler.java:284-286`), searches the configured
source paths for it (`:217-226`), and drops classes whose source it cannot find (`:209-215`).

---

## 2. Coordinates and in-repo consumers

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-jdk-tools</artifactId>
    <version>0.3.20</version>
</dependency>
```

`${revision}` is `0.3.20-SNAPSHOT` on `release` (root `pom.xml:54`); the group id is fixed at root `pom.xml:12`. The
version is also managed by the reactor BOM (`microsphere-java-dependencies/pom.xml:38`).

| In-repo consumer | Scope | POM evidence |
|---|---|---|
| `microsphere-annotation-test` | compile | `microsphere-annotation-test/pom.xml:33` |
| `microsphere-lang-model` | **test** | `microsphere-lang-model/pom.xml:46`, `<scope>test</scope>` at `:48` |
| `microsphere-jdk-tools` itself | compile | `microsphere-jdk-tools/pom.xml:14` |

`microsphere-annotation-processor` has **no direct** dependency on this module; it reaches it transitively through
`microsphere-annotation-test` (test scope, `microsphere-annotation-processor/pom.xml:53`) and through
`microsphere-lang-model` (`microsphere-annotation-processor/pom.xml:33`).

> [!NOTE]
> `microsphere-lang-model` declares the dependency but no `microsphere-lang-model` source or test references
> `io.microsphere.jdk.tools.compiler.Compiler` (verified across the module's sources). Treat that edge as reserved
> for tests that drive compilation, not as a runtime requirement.

Transitively you also get `microsphere-java-core`, whose helpers this module uses heavily — `CollectionUtils`, `Lists`,
`Sets`, `SetUtils`, `FileConstants`, `SymbolConstants`, `StringBuilderWriter`, `SimpleFileScanner`, `LoggerFactory`,
`FormatUtils`, `Assert`, `ClassUtils`, `StringUtils` (`Compiler.java:19-63`). Core needs no runtime logging backend;
its facade picks the first available `LoggerFactory` (`microsphere-java-core/src/main/java/io/microsphere/logging/LoggerFactory.java:73-96`).

---

## 3. Version information and compatibility range

### 3.1 Where the version is 1.0.0 and where it is 0.3.20

| Item | Value | Evidence |
|---|---|---|
| Javadoc `@since` of `Compiler` | `1.0.0` | `Compiler.java:70` |
| Release line documented here | `release` branch, `${revision}` = `0.3.20-SNAPSHOT` | root `pom.xml:54` |
| Published tag | `0.3.20` | repository tag; group id root `pom.xml:12` |
| Bytecode level | **not declared in this reactor** — inherited from the external parent `microsphere-build:0.3.16` (root `pom.xml:8-9`) | no `maven.compiler.*` / `<release>` property exists anywhere in the module tree |

> [!WARNING]
> Do not quote a `-source`/`-target` number for this module. The `release` branch configures none: the only
> `maven-compiler-plugin` configuration in the whole reactor is `-proc:none` in
> `microsphere-annotation-processor`, and `microsphere-jdk-tools` has no `<build>` section at all
> (`microsphere-jdk-tools/pom.xml` ends at `:66`). The effective level comes from `microsphere-build:0.3.16`.

### 3.2 Per-JDK behaviour, as derived from the code

`Compiler.java` contains **no version branch** — no `if (javaVersion >= 9)`, no `JavaVersion`/`SystemUtils` check, no
`Runtime.version()` call. Two indirect branches exist and they are the whole story:

1. `getSystemJavaCompiler()` (`Compiler.java:107`) — resolved by the JDK's own `javax.tools.ToolProvider`, which
   returns `null` when no compiler implementation is reachable; `Compiler` then throws (`:108`).
2. `Lists.ofList` / `Sets.ofSet` — used by `options(...)` (`:112`) and `processors(...)` (`:137`). They bind
   `List.of`/`Set.of` method handles if those methods exist (`microsphere-java-core/src/main/java/io/microsphere/collection/Lists.java:101`,
   `Sets.java:99`) and fall back to `unmodifiableList(asList(…))` / a linked-hash set otherwise, inside a
   `catch (Throwable)` (`Lists.java:520-529`, `Sets.java:559-568`). This changes only the **implementation class** of
   the stored options/processors, never the API contract.

| | JDK 8 | JDK 11 | JDK 17 | JDK 21 | JDK 25 |
|---|---|---|---|---|---|
| `getSystemJavaCompiler()` source (`Compiler.java:107`) | `com.sun.tools.javac.Main` from `<jdk>/lib/tools.jar`, located by `ToolProvider` | `jdk.compiler` module, `javax.tools.JavaCompiler` service | same | same | same |
| `tools.jar` | present in a JDK, absent from a JRE | **does not exist** | absent | absent | absent |
| Does `Compiler` look for `tools.jar`? | **No** — on every JDK it only null-checks the returned `JavaCompiler` (`:107-108`) | no | no | no | no |
| `--add-exports` / `--add-opens` needed by this module | none | none | none | none | none |
| Result on a JRE-only runtime | `null` → `IllegalArgumentException` (`:108`) | `null` if `jdk.compiler` absent from the image (e.g. a `jlink` image built without it) → same exception | same | same | same |
| `options(...)` / `processors(...)` collection impl (`:112`, `:137`) | `Arrays.asList`-backed unmodifiable list / linked hash set | `List.of`/`Set.of` (immutable) | immutable | immutable | immutable |
| Bytecode produced by `compile(...)` | class-file level of the running JDK, because no `--source`/`--release` option is ever passed (`DEFAULT_OPTIONS`, `:76-77`) | same | same | same | same |
| Source-file discovery (`:242-271`) | via `CodeSource` location, `target/classes` layout assumed | same | same | same | same |

### 3.3 The `--add-exports` question, answered precisely

The flag that usually circulates for this territory is
`--add-exports java.compiler/com.sun.tools.javac.*=ALL-UNNAMED`. Two corrections for the `release` line:

* The package `com.sun.tools.javac.*` is **not** in module `java.compiler` (that module holds the standard
  `javax.tools` / `javax.lang.model` / `javax.annotation.processing` API). The implementation lives in
  **`jdk.compiler`**.
* `microsphere-jdk-tools` never reads those packages, so it needs no such flag on any JDK in the CI matrix. Flags
  only become relevant for **your** code or a harness that casts `JavaCompiler` down to a `com.sun.tools.javac` type.

> [!TIP]
> CI proves the point: the build command is
> `mvn --batch-mode --update-snapshots --file pom.xml -Drevision=0.0.1-SNAPSHOT -Dsurefire.useSystemClassLoader=false test --activate-profiles test,coverage`
> (`.github/workflows/maven-build.yml:38-45`). No `--add-opens` / `--add-exports` is passed, and there is
> **no `.mvn/jvm.config`** in the repository (only `.mvn/wrapper/maven-wrapper.properties`). So `CompilerTest` must —
> and does — run on a stock JVM on all five JDKs. If your own test needs a module flag, that requirement comes from
> your code, not from `Compiler`.

---

## 4. API reference

All signatures are copied verbatim from `release`. The type is `public class Compiler` (`Compiler.java:72`) with
fluent setters returning `this`.

```java
public class Compiler {

    // Compiler.java:76-77
    public static final String[] DEFAULT_OPTIONS = {"-parameters", "-Xlint:-unchecked", "-nowarn", "-Xlint:deprecation"};

    // Constructors — Compiler.java:95-109
    public Compiler()                                       // source dir + target dir detected from Compiler.class
    public Compiler(File targetDirectory)                   // source dir detected, explicit target
    public Compiler(File defaultSourceDirectory, File targetDirectory)
        // throws IllegalArgumentException("No Java compiler available. Ensure this process is running
        //         on a JDK (not just a JRE).")  — via Assert.assertNotNull, Compiler.java:108

    // Fluent configuration — all return this
    public Compiler options(String... options)              // :111-114  REPLACES the list (no append)
    public Compiler sourcePaths(File... sourcePaths)        // :116-119  APPENDS to the set
    public Compiler sourcePaths(Class<?>... sourceClasses)  // :121-126  appends each class's detected source path
    public Compiler sourcePath(Class<?> sourceClass)        // :128-134  no-op when the source cannot be located
    public Compiler processors(Processor... processors)     // :136-139  REPLACES the set
    public Compiler diagnosticListener(DiagnosticListener<? super JavaFileObject> l)  // :141-144
    public Compiler locale(Locale locale)                   // :146-149
    public Compiler charset(Charset charset)                // :151-154

    // Terminal operation — Compiler.java:156-167
    public boolean compile(Class<?>... sourceClasses) throws IOException;

    // Accessors — Compiler.java:169-207
    public JavaCompiler getJavaCompiler();                                  // never null after construction
    public StandardJavaFileManager getJavaFileManager() throws IOException;  // caller must close
    public DiagnosticListener<? super JavaFileObject> getDiagnosticListener(); // null unless set
    public Locale getLocale();                                              // null unless set
    public Charset getCharset();                                            // null unless set
    public List<String> getOptions();                                       // never null; empty when cleared
    public Set<Processor> getProcessors();                                  // never null

    // Static discovery — Compiler.java:228-286
    public static File detectSourcePath(Class<?> sourceClass);              // null if source not found (:242-262)
    public static File detectRootDirectory(Class<?> sourceClass);           // :264-271
    public static File detectClassPath(Class<?> sourceClass);               // throws UnsupportedOperationException (:273-282)
    public static String resolveJavaSourceFileRelativePath(Class<?> sourceClass); // :284-286
    static File defaultSourceDirectory()    // package-private, :228-230
    static File defaultRootDirectory()      // package-private, :232-234
    static File defaultTargetDirectory()    // package-private, :236-240, mkdirs() with no success check
}
```

### 4.1 Contracts the code actually implements

| Method | Contract, from the source |
|---|---|
| `compile(Class...)` | Builds one `StandardJavaFileManager` per call (`:159`), sets `SOURCE_PATH` to the configured source dirs, `CLASS_OUTPUT` **and** `SOURCE_OUTPUT` to the single target dir (`:175-177`), passes the file manager's `Writer` capture, calls `task.setProcessors(getProcessors())` and returns `task.call()` (`:162-163`). `true` = compilation succeeded. |
| `compile` and missing sources | Classes whose `.java` cannot be found are **silently dropped** — `.filter(Objects::nonNull)` (`:209-215`). If *all* of them are dropped, javac receives an empty compilation unit list and `compile` normally still returns `true`. |
| `getOptions()` | `unmodifiableList` copy of the configured options; `emptyList()` when the list is empty (`:193-199`). |
| `getProcessors()` | `unmodifiableSet`, or `emptySet()` when never set (`:201-207`). `compile()` always calls `task.setProcessors(getProcessors())` (`:162`), and `javax.tools.CompilationTask` runs annotation processing **only** for a non-empty set — so leaving the field unset (`:85`) means *no* processing at all, not "let javac discover processors on the classpath". Register your processors explicitly; that is why `CompilerInvocationInterceptor` loads them through SPI first (`microsphere-annotation-test/.../CompilerInvocationInterceptor.java:60-62`). |
| `getJavaFileManager()` | **Not** closed by this accessor; `compile` closes it via try-with-resources (`:159`), a direct caller must close it too. |
| Nullability | `Compiler.java` carries **no** `@Nullable` / `@Nonnull` annotations at all (the file's only Javadoc block is the class comment at `:66-71`); the table above is therefore the contract, read from the code. |
| Thread safety | `javaCompiler` is `final` and safely published (`:83`, `:107`); every other field is mutable and unsynchronised (`:85-93`) — see pitfall 7. |

### 4.2 What `DEFAULT_OPTIONS` means for you

`-parameters` keeps method parameter names in the class file — which annotation processors and any
reflection-based metadata reader depends on. `-Xlint:-unchecked` and `-nowarn` suppress warning output, while
`-Xlint:deprecation` asks for deprecation notes; the combination is not a contradiction to javac, but it does mean
warnings rarely reach the captured writer (pitfall 4).

---

## 5. A runnable example

Taken from `microsphere-jdk-tools/src/test/java/io/microsphere/jdk/tools/compiler/CompilerTest.java` — the full
configuration chain plus a compile call:

```java
Compiler compiler = new Compiler();

boolean ok = compiler.compile(Compiler.class, Test.class);   // ok == true

compiler.options()                                            // clears DEFAULT_OPTIONS
        .processors(new AbstractProcessor() {
            @Override
            public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                return false;
            }
        })
        .diagnosticListener(diagnostic -> { })
        .locale(getDefault())
        .charset(UTF_8);

ok = compiler.compile(Compiler.class);
```

Source: `CompilerTest.java:143-170` (`testCompile`). Related assertions:

* `CompilerTest.java:38-44` — the no-arg constructor yields a non-null `JavaCompiler`.
* `CompilerTest.java:86-93` — `detectClassPath(Compiler.class)` exists; `detectClassPath(String.class)` throws
  `UnsupportedOperationException`.
* `CompilerTest.java:107-121` — `sourcePaths(...)` returns the same instance (fluent) and accepts both `File` and
  `Class` varargs.
* `CompilerTest.java:181-187` — `detectSourcePath` returns `null` for a class whose source is not in the project tree
  (`assertNull(detectSourcePath(Test.class))`).

> [!NOTE]
> The comments inside `testCompile` say "with mocked compiler it's safe to call" (`CompilerTest.java:146`). Nothing is
> mocked in that test — `CompilerTest.java:1-28` imports no Mockito type, and `mockito-core` is only declared
> (`microsphere-jdk-tools/pom.xml:52-56`). The test really runs `javac` over `Compiler.java` and `CompilerTest.java`.

---

## 6. Common pitfalls

1. **JDK 8 and `tools.jar` are *assumed*, never detected.** `Compiler` calls `ToolProvider.getSystemJavaCompiler()`
   (`:107`) and only checks the result for `null` (`:108`). It never inspects `java.home`, never builds a class loader
   over `<jdk>/lib/tools.jar`, and never reports *why* the compiler is missing. On JDK 8 that means: if you run
   against a JRE — or a JDK whose `tools.jar` your build omitted from a shaded/uber classpath that `ToolProvider`
   cannot see — you get one exception with a fixed message and no diagnostic about the missing jar.
2. **The failure is an exception, not silent degradation.** `assertNotNull` is core's assertion helper and throws
   `IllegalArgumentException` (`microsphere-java-core/src/main/java/io/microsphere/util/Assert.java:149-153`). Exact
   message text (`Compiler.java:108`):
   `No Java compiler available. Ensure this process is running on a JDK (not just a JRE).`
   The rest of the library's convention is to degrade quietly (`detectSourcePath` returns `null` at `:250-253`,
   `searchJavaSourceFile` returns `null` at `:225`); the constructor is the module's **only** deliberate
   fail-fast. Catch it at construction time, not around `compile`.
3. **JRE versus JDK.** A JRE (and a `jlink` image built without `jdk.compiler`) gives the exception above. Note the
   asymmetry: the exception arrives when you build a `Compiler`, so even calling
   `Compiler.detectSourcePath(...)` first is safe on a JRE — only the constructors fail.
4. **Compiler output is trace-only.** `compile()` writes javac's output into a `StringBuilderWriter` (`:158`) and
   discards it; the only place it is surfaced is `logger.trace(...)` in the `finally` block (`:165`). If you pass no
   `diagnosticListener(...)` (the field starts `null`, `:89`) **and** your logging level is above `TRACE`, a failed
   compilation returns `false` with zero explanation. Either register a listener or enable trace on
   `io.microsphere.jdk.tools.compiler.Compiler`.
5. **`sourcePaths(File...)` appends, `options(...)` and `processors(...)` replace.** `sourcePaths` calls
   `CollectionUtils.addAll` into the existing `LinkedHashSet` (`:117`, core `CollectionUtils.java:454`), while
   `options` reassigns the field (`:112`) and `processors` reassigns too (`:137`). Calling `options()` with no
   arguments therefore drops `-parameters` — silent behaviour change for processors that read parameter names.
6. **`SOURCE_OUTPUT` and `CLASS_OUTPUT` are both the single target directory** (`:176-177`). Generated sources and
   generated `.class` files land in the same tree, so a `clean` that deletes the target dir also deletes generated
   sources you might have wanted to inspect.
7. **One instance, many threads is unsupported.** Every configuration field is mutable and non-final
   (`:79-93`). Two threads calling `compile(...)` on the same instance while a third reconfigures it can interleave a
   `LinkedHashSet` mutation (`sourcePaths`, `:117`) with the iteration inside `searchJavaSourceFile` (`:219`) —
   `ConcurrentModificationException`. Use one `Compiler` per test/compilation, or synchronise externally.
8. **The default target directory is derived from *this module's* location, not yours.** `defaultTargetDirectory()`
   is `<root of Compiler.class> /target/generated-classes` (`:236-240`), where the root comes from
   `detectRootDirectory(Compiler.class)` = `classPath.parent.parent` (`:264-271`). In a Maven build that resolves to
   `microsphere-jdk-tools/target/generated-classes`; when the jar is resolved from your local repository the two
   `getParentFile()` hops land **inside the repository layout**. `dir.mkdirs()` is called without checking the result
   (`:238`). Pass your own `targetDirectory` if the default matters —
   `new Compiler(new File("src/main/java"), new File("target/generated-classes"))`.
9. **Missing class path throws.** `detectClassPath` throws `UnsupportedOperationException` with the formatted message
   `"The source {} is based on the file system, the class path can't be detected."` — `{}` is core's
   `FormatUtils` placeholder (`microsphere-java-core/src/main/java/io/microsphere/text/FormatUtils.java:17`, `:38`),
   replaced by the class — when the class has no `CodeSource` (`:276-281`, text at `:280`). In practice that means
   classes from the runtime image or from a class loader that reports no code source. `detectSourcePath` and
   `sourcePath(Class)` are the degrading variants (`:250-253`); `detectRootDirectory` and the
   `Compiler()`/`Compiler(File)` constructors are not — they can throw through `defaultSourceDirectory()` →
   `detectSourcePath` (null-safe) but `defaultRootDirectory()` → `detectClassPath` (`:232-234`, `:265`) throws first.
10. **Source lookup is a recursive, unbounded filesystem scan.** `detectSourcePath` scans the whole module root with
    `SimpleFileScanner.INSTANCE.scan(rootDirectory, true, …)` (`:246-247`, core
    `io/scanner/SimpleFileScanner.java:57`, `:87`), matching `getAbsolutePath().endsWith(relativePath)` (`:247`).
    On a big tree that is an expensive walk, and a stale copy under `target/` or a generated-sources directory can win
    the match — the result is `first(sourceFiles)` (`:249`), whose order comes from `File#listFiles()`.
11. **Windows paths are taken literally.** `detectClassPath` converts the code-source URL with
    `new File(location.getPath())` (`:277-278`), which skips `%XX` decoding and URI handling. Project paths with
    spaces or non-ASCII characters are the cases to test on Windows before you rely on source discovery.
12. **`-encoding` is not in the defaults.** The `charset` field starts `null` (`:93`), so `getStandardFileManager(...)`
    (`:174`) uses the JVM default. A source file with non-ASCII literals on a non-UTF-8 default platform charset can
    change javac's diagnostics or corrupt string constants. Call `.charset(UTF_8)` (as `CompilerTest.java:164` does).
13. **Forgetting `.processors(...)` silently skips the thing you wanted to test.** `compile()` passes
    `getProcessors()` unconditionally (`:162`); with the field unset that is `emptySet()` (`:201-207`), and
    `CompilationTask` only runs annotation processing for a non-empty set. The result looks like a successful compile
    with no generated output and no processor-side assertion firing.
14. **The encoding is supplied as an object, not a name.** `getJavaFileManager()` hands `getCharset()` —
    `public Charset getCharset()` (`:189-191`) — to `JavaCompiler.getStandardFileManager(...)`, whose third argument is
    the character-encoding **name** per the `javax.tools` contract; this module contains no `Charset` → name adapter
    between the setter (`:151-154`) and the call site (`:174`). The pairing is a type-level inconsistency worth
    verifying before you depend on non-default encodings; the intended value is the name
    (`getCharset().name()`), and `CompilerTest.java:164-167` is the code that exercises it.

---

## 7. See also

* [microsphere-annotation-test](microsphere-annotation-test.md) — the JUnit 5 extension that drives this module
* [microsphere-annotation-processor](microsphere-annotation-processor.md) — the processors most often compiled with it
* [microsphere-lang-model](microsphere-lang-model.md) — the `javax.lang.model` facade of the same toolchain
* [Class Loading and Artifact Detection — User Guide](microsphere-java-core/classloading-and-artifacts.md) — sibling
  JDK-internal-access theme in `core`
* [microsphere-jdk-tools — Developer Guide](../developer-guide/microsphere-jdk-tools.md) — line-by-line `Compiler.java`
  analysis and diagrams
* Chinese version: [中文用户指南](zh/microsphere-jdk-tools.md)
