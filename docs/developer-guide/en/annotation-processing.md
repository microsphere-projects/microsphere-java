# Annotation Processing

> Read this page in: [中文](../zh/annotation-processing.md) · [English](annotation-processing.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Modules | `microsphere-annotation-processor`, `microsphere-lang-model`, `microsphere-jdk-tools` |
| Bundled processor | `io.microsphere.annotation.processor.ConfigurationPropertyAnnotationProcessor` |
| Generated resource | `META-INF/microsphere/configuration-properties.json`, written into `CLASS_OUTPUT` |
| Auto-discovery | service file `META-INF/services/javax.annotation.processing.Processor` inside the processor jar |
| Self-build flag | the processor module compiles itself with `-proc:none` |

This page covers the compile-time half of `@ConfigurationProperty`, the `javax.lang.model` helper layer,
how to write and enable your own processor, and the in-process Java compiler (`microsphere-jdk-tools`) used to
test processors. The test harness itself is on [Testing Support](testing.md).

---

## 1. `ConfigurationPropertyAnnotationProcessor`

`microsphere-annotation-processor/src/main/java/io/microsphere/annotation/processor/`

```java
@SupportedAnnotationTypes(value = CONFIGURATION_PROPERTY_ANNOTATION_CLASS_NAME) // "io.microsphere.annotation.ConfigurationProperty"
public class ConfigurationPropertyAnnotationProcessor extends AbstractProcessor {
    @Override public SourceVersion getSupportedSourceVersion()   // SourceVersion.latestSupported()
    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)  // returns false
}
```

Registered for auto-discovery at
`microsphere-annotation-processor/src/main/resources/META-INF/services/javax.annotation.processing.Processor`:

```
io.microsphere.annotation.processor.ConfigurationPropertyAnnotationProcessor
```

---

## 2. Enabling it in your build

### 2.1 Maven: classpath discovery (default)

Because the jar carries the `Processor` service file, plain javac discovers the processor from the **compile
classpath** — no compiler-plugin configuration is needed:

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-annotation-processor</artifactId>
    <optional>true</optional> <!-- compile-time only; versions come from the BOM -->
</dependency>
```

### 2.2 Maven: `maven-compiler-plugin` `<annotationProcessorPaths>`

Use this when the processor must **not** sit on the compile classpath (e.g. your own processor depends on
libraries you do not want to compile against), or when the compiler plugin is configured with
`<proc>` and an explicit path:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>io.github.microsphere-projects</groupId>
                <artifactId>microsphere-annotation-processor</artifactId>
                <version>0.3.19</version> <!-- annotationProcessorPaths does not read dependencyManagement -->
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

> [!NOTE]
> When `annotationProcessorPaths` is set, javac looks for processors **only there** — classpath discovery stops.

### 2.3 Gradle

```groovy
dependencies {
    // keeps the processor off the compile and runtime classpaths
    annotationProcessor "io.github.microsphere-projects:microsphere-annotation-processor"
}
```

Versions resolve through the BOM (`implementation platform(...)`), see
[Getting Started](getting-started.md#32-gradle).

### 2.4 Building a processor module yourself: `-proc:none`

A module that *contains* a processor must not let that processor run on its own sources. The
`microsphere-annotation-processor` build does exactly this in its `pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgument>-proc:none</compilerArgument>
    </configuration>
</plugin>
```

In Gradle, the equivalent is `tasks.withType(JavaCompile) { options.compilerArgs << "-proc:none" }` for the
module that defines the processor.

> [!IMPORTANT]
> `microsphere-annotation-processor` **shades `microsphere-java-core` into its own jar** (maven-shade-plugin,
> `package` phase, only `io.github.microsphere-projects:microsphere-java-core` is included) so the processor is
> self-contained, and keeps `microsphere-lang-model` as a normal dependency. If you also depend on
> `microsphere-java-core`, expect duplicate `io.microsphere.*` classes at the processor classpath level —
> harmless for javac, but do not put the processor jar on your runtime classpath.

---

## 3. What it generates, round by round

```java
public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
        writeMetadata();           // one file, written on the final round
    } else {
        resolveMetadata(roundEnv); // append entries
    }
    return false;                  // annotations are NOT claimed
}
```

* `resolveMetadata` visits **every root element** of the round (`roundEnv.getRootElements()`, not
  `getElementsAnnotatedWith`) with `ConfigurationPropertyJSONElementVisitor`. Walking roots means annotated
  classes *and* classes referenced in the same compilation are handled.
* Returning `false` lets other processors still see `@ConfigurationProperty`.
* `writeMetadata` runs once on `processingOver()`: the accumulated `StringBuilder` is reparsed as a `JSONArray`,
  pretty-printed with two-space indentation (`jsonArray.toString(2)`), and written through `ResourceProcessor`
  into `CLASS_OUTPUT` (`target/classes`).

Field-level resolution rules (applied by the visitor in `visitVariableAsField`):

| JSON key | Source when the annotation attribute is left at its default |
|---|---|
| `name` | the annotated field's **constant value** (`field.getConstantValue()`) |
| `type` | `field.asType()` rendered via `TypeUtils.getTypeName(...)` |
| `description` | `Elements.getDocComment(field)` |
| `metadata.declaredClass` / `declaredField` | always derived from the field and its enclosing type |
| `metadata.sources` | the annotation's `source()` array |

Annotation members: `name`, `type` (`Class<?>`, default `String.class`), `defaultValue`, `required`,
`description`, `source`.

After the annotation-derived entries, the processor appends whatever the runtime SPI chain produces —
`ConfigurationPropertyLoader.loadAll()` (priority-sorted) rendered by the first
`ConfigurationPropertyGenerator` service (`DefaultConfigurationPropertyGenerator`, registered by
`microsphere-java-core`) and resolved in the visitor's constructor.

> [!NOTE]
> Consequence: the generated file can contain entries you never annotated — they come from loaders visible on
> the **processor's** classpath, including `AdditionalMetadataResourceConfigurationPropertyLoader` reading
> `META-INF/microsphere/additional-configuration-properties.json`. Registering your own
> `ConfigurationPropertyLoader` alongside the processor is the supported way to inject properties that cannot
> be expressed as annotated fields — the module's own test does this via
> `src/test/resources/META-INF/services/io.microsphere.metadata.ConfigurationPropertyLoader`
> (`io.microsphere.annotation.processor.TestConfigurationPropertyLoader`).

Output path: `META-INF/microsphere/configuration-properties.json`
(`ResourceConstants.CONFIGURATION_PROPERTY_METADATA_RESOURCE`). The JSON shape is documented in
[Configuration Property Metadata](configuration-metadata.md).

---

## 4. `microsphere-lang-model` — helpers over `javax.lang.model.*`

16 types in `io.microsphere.lang.model.element` and `io.microsphere.lang.model.util`. Ten are `interface` types
that `extends io.microsphere.util.Utils` and expose **static interface methods** (Java 8+ allows this); you call
them via static import, e.g. `import static io.microsphere.lang.model.util.ElementUtils.matchesElementType;` —
no `INSTANCE`, no factory:

> [!NOTE]
> Do not confuse these with `io.microsphere.reflect.TypeUtils` (runtime `java.lang.reflect.Type`) or the JDK's
> own `javax.lang.model.util.Types`. The `lang.model` family operates on compile-time mirrors.

| Type | Kind | Purpose |
|---|---|---|
| `element.StringAnnotationValue` | `class implements AnnotationValue` | supply a `String` annotation value outside a real `AnnotationMirror` |
| `util.ElementUtils` | interface | `ElementKind`/`ElementType` predicates, element filtering, `matchParameterTypes(...)` |
| `util.AnnotationUtils` | interface | finding and reading `AnnotationMirror`s: `getAnnotation`, `findMetaAnnotation`, `getAttributesMap`, `getAttribute(mirror, name, boolean withDefault)`, `getValue`, `getElementTypes`, `matchesDefaultAttributeValue` |
| `util.TypeUtils` | interface | large static surface over `TypeMirror`/`DeclaredType`/`TypeElement`, incl. `getTypeName`, `getTypeMirror(processingEnv, Type)`, `ofTypeElement` |
| `util.ClassUtils` | interface | `getClassName(TypeMirror)`, `loadClass(TypeMirror)`, `loadClass(String)` |
| `util.MethodUtils` / `FieldUtils` / `ConstructorUtils` / `MemberUtils` | interface | member-element queries (`findMethod`, `findField`, `findConstructor`) |
| `util.MessagerUtils` | interface | `printNote` / `printWarning` / `printMandatoryWarning` / `printError` / `printMessage`, each in `(ProcessingEnvironment, ...)` and `(Messager, ...)` form — routes to javac *and* the logger, `{}` placeholders |
| `util.LoggerUtils` | interface | `trace/debug/info/warn/error(String format, Object... args)` |
| `util.ExecutableElementComparator` | `Comparator<ExecutableElement>` | `public static final INSTANCE`; orders by simple name, then parameter count, then parameter type names |
| `util.JSONElementVisitor` | `abstract class extends ElementKindVisitor6<Boolean, StringBuilder>` | AST→JSON visitor base |
| `util.AnnotatedElementJSONElementVisitor` | `abstract class extends JSONElementVisitor` | same, restricted to elements annotated with one annotation type |
| `util.JSONAnnotationValueVisitor` / `ResolvableAnnotationValueVisitor` | `SimpleAnnotationValueVisitor6` subclasses | render an annotation value as JSON / resolve it to a Java object |

---

## 5. Writing your own processor

### 5.1 The visitor: extend `AnnotatedElementJSONElementVisitor`

`JSONElementVisitor` makes its `visitPackage/visitVariable/visitExecutable/visitType/visitTypeParameter`
methods **final**: the template walks the tree, decides membership through `supports`/`supportsPackage`/
`supportsVariable`/`supportsExecutable`/`supportsType`/`supportsTypeParameter`, and dispatches to your
`doVisit*` hooks (plus `visitMembers(List<? extends Element>, StringBuilder)` for types). So you override
`doVisit*` and `supports*` — never `visit*`.

```java
public class MyElementJSONVisitor extends AnnotatedElementJSONElementVisitor {

    public MyElementJSONVisitor(ProcessingEnvironment processingEnv) {
        super(processingEnv, "com.example.MyAnnotation");
    }

    @Override
    protected boolean doVisitType(TypeElement e, StringBuilder jsonBuilder) {
        // e.getEnclosedElements(), getAnnotation(...), getAttributesMap(...), ...
        return true;
    }
}
```

`AnnotatedElementJSONElementVisitor` exposes `protected final` fields `processingEnv`, `elements`,
`annotationClassName`, `annotationType`, `annotationTypeElement`, `elementTypes`, and its `supports(Element)` is
`matchesElementType(element, elementTypes)` — the "may this annotation legally appear here?" guard.
`ConfigurationPropertyJSONElementVisitor` is exactly this pattern bound to
`io.microsphere.annotation.ConfigurationProperty`, overriding `visitVariableAsField` and `supportsType`.

### 5.2 The processor

```java
@SupportedAnnotationTypes("com.example.MyAnnotation")
public class MyAnnotationProcessor extends AbstractProcessor {

    @Override public SourceVersion getSupportedSourceVersion() { return SourceVersion.latestSupported(); }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        StringBuilder json = new StringBuilder("[");
        roundEnv.getRootElements().forEach(root -> root.accept(new MyElementJSONVisitor(processingEnv), json));
        // write json via a ResourceProcessor(processingEnv, StandardLocation.CLASS_OUTPUT)
        return false; // true = claim the annotation, later processors then never see it
    }
}
```

Register it: create `src/main/resources/META-INF/services/javax.annotation.processing.Processor` containing
`com.example.MyAnnotationProcessor`, put the jar on the consumer classpath (or in `annotationProcessorPaths` /
Gradle `annotationProcessor`), and compile the processor module itself with `-proc:none` (§2.4).

> [!WARNING]
> **JDK 16+ and `com.sun.tools.javac`.** The public `javax.tools` / `javax.annotation.processing` API needs
> nothing special, but reflective access to javac internals does: `FilerProcessor.getJavaFileManager()` reads the
> private `fileManager` field of javac's `JavacFiler`, and casting `ProcessingEnvironment` to
> `JavacProcessingEnvironment` touches module-private packages. On JDK 16+ `jdk.compiler` no longer opens them by
> default, so run the processor (or its harness tests) with JVM flags like
> `--add-opens jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED` on the *forked compiler / Surefire JVM*
> (this repository's `java16+` build profile already opens `java.base/java.lang` and `java.base/java.lang.invoke`
> — see [Getting Started](getting-started.md#61-jdk-16-specifics)). Prefer `javax.tools` APIs; keep internal
> access optional and guarded.

---

## 6. `microsphere-jdk-tools` — compiling from inside a JVM

`io.microsphere.jdk.tools.compiler.Compiler` wraps `javax.tools.JavaCompiler` for in-process compilation, which
is how processor tests run. Fluent setters each return `this`:

```java
public class Compiler {

    public static final String[] DEFAULT_OPTIONS = { "-parameters", "-Xlint:-unchecked", "-nowarn", "-Xlint:deprecation" };

    public Compiler()                                       // target = <root of Compiler.class>/target/generated-classes
    public Compiler(File targetDirectory)
    public Compiler(File defaultSourceDirectory, File targetDirectory)

    public Compiler options(String... options)              // REPLACES the option list
    public Compiler sourcePaths(File... sourcePaths)        // adds
    public Compiler sourcePaths(Class<?>... sourceClasses)  // adds one source path per class
    public Compiler processors(Processor... processors)     // replaces the processor set
    public Compiler diagnosticListener(DiagnosticListener<? super JavaFileObject> diagnosticListener)
    public Compiler locale(Locale locale)
    public Compiler charset(Charset charset)

    public boolean compile(Class<?>... sourceClasses) throws IOException

    public static File detectSourcePath(Class<?> sourceClass)
    public static File detectRootDirectory(Class<?> sourceClass)
    public static File detectClassPath(Class<?> sourceClass)
    public static String resolveJavaSourceFileRelativePath(Class<?> sourceClass)
    // getters: getJavaCompiler, getJavaFileManager, getDiagnosticListener, getLocale, getCharset,
    // getOptions / getProcessors (unmodifiable views)
}
```

```java
Compiler compiler = new Compiler()
        .options("-parameters", "-proc:only")
        .sourcePaths(MyConfiguration.class)
        .processors(new ConfigurationPropertyAnnotationProcessor())
        .diagnosticListener(diagnostic -> System.out.println(diagnostic.getMessage(Locale.getDefault())));
boolean success = compiler.compile(MyConfiguration.class);
```

Constraints you must respect:

* Requires a **JDK**, not a JRE: the constructor asserts `ToolProvider.getSystemJavaCompiler()` is non-null
  with the message *"No Java compiler available. Ensure this process is running on a JDK (not just a JRE)."*
* Source discovery is **file-system based**: `detectRootDirectory(Class)` walks up from the class's code
  location (a Maven-style `.../target/classes` → module root) and finds sources under `src/main/java` /
  `src/test/java`. Classes loaded from a jar cannot be located — `detectSourcePath(Test.class)` returns `null`
  (asserted by `CompilerTest`); `detectClassPath(Class)` throws `UnsupportedOperationException` for classes
  with no `CodeSource` (bootstrap classes such as `java.lang.String`).
* The default target **and** default source directory are derived from the location of **`Compiler.class`
  itself** (`<root>/target/generated-classes`, created with `mkdirs()`), not from the class being compiled.
  Pass `Compiler(File targetDirectory)` when you want output under your own module.
* `options(...)` **replaces** the option list (the constructor seeds it with `DEFAULT_OPTIONS`); to disable
  processing pass `-proc:none` explicitly.

---

## 7. Testing a processor

Two options, in increasing fidelity:

1. **Direct**: drive `Compiler` yourself — construct it, add your processor, compile classes carrying your
   annotation, then assert on the generated resource. Good for "does it produce the file".
2. **Harness**: extend `AbstractAnnotationProcessingTest` from `microsphere-annotation-test`; it compiles the
   test class before each test method and injects live `ProcessingEnvironment`/`Elements`/`Types`/
   `RoundEnvironment` instances — full contract, worked examples and caveats on
   [Testing Support](testing.md).

For the harness to see your processor, register it in
`src/test/resources/META-INF/services/javax.annotation.processing.Processor` — the harness adds every
`Processor` found by `ServiceLoader` on the test classpath.

---

## See also

* [Testing Support](testing.md) — the JUnit 5 processing harness and test fixtures
* [Configuration Property Metadata](configuration-metadata.md) — the runtime reader/loader/generator chain
* [Annotations](annotations.md) — the `@ConfigurationProperty` contract being processed
* [Getting Started](getting-started.md) — BOM import, JDK matrix, build profiles
* [Reference](reference.md) — service file inventory

[← Handbook index](../README.md) · [Previous: Configuration Property Metadata](configuration-metadata.md) · [Next: Testing Support →](testing.md)
