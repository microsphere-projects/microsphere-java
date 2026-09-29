# Annotation Processing

**Modules**: `microsphere-annotation-processor`, `microsphere-lang-model`, `microsphere-jdk-tools`

This page covers the compile-time half of `@ConfigurationProperty`, the `javax.lang.model` helper layer the processor
is written against, and the in-process Java compiler used to test processors.

---

## 1. `ConfigurationPropertyAnnotationProcessor`

`microsphere-annotation-processor/src/main/java/io/microsphere/annotation/processor/`

```java
@SupportedAnnotationTypes("io.microsphere.annotation.ConfigurationProperty")
public class ConfigurationPropertyAnnotationProcessor extends AbstractProcessor {
    @Override public SourceVersion getSupportedSourceVersion()   // SourceVersion.latestSupported()
    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
}
```

Registered for auto-discovery:

```
microsphere-annotation-processor/src/main/resources/META-INF/services/javax.annotation.processing.Processor
  -> io.microsphere.annotation.processor.ConfigurationPropertyAnnotationProcessor
```

### 1.1 Enabling it in your build

Because the jar carries the `Processor` service file, javac discovers it from the **compile classpath** — no
`<annotationProcessorPaths>` is needed.

Maven:

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-annotation-processor</artifactId>
    <optional>true</optional>
</dependency>
```

Gradle (keeps it off your runtime classpath):

```groovy
annotationProcessor "io.github.microsphere-projects:microsphere-annotation-processor"
```

Versions come from the BOM import ([Getting Started](getting-started.md#3-maven)).

> [!IMPORTANT]
> `microsphere-annotation-processor` **shades `microsphere-java-core` into its own jar** so the processor is
> self-contained, and keeps `microsphere-lang-model` as a normal transitive dependency. Its own build compiles with
> `-proc:none` to avoid self-processing. If you also depend on `microsphere-java-core` directly, expect duplicate
> `io.microsphere.*` classes at the *processor* classpath level — harmless for javac, but do not put the processor
> jar on your runtime classpath.

### 1.2 What it does, round by round

```java
public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
        writeMetadata();          // one file, written on the final round
    } else {
        resolveMetadata(roundEnv); // append entries
    }
    return false;                 // annotations are NOT claimed
}
```

* `resolveMetadata` visits **every root element** in the round
  (`roundEnv.getRootElements()`, not `getElementsAnnotatedWith`) with
  `ConfigurationPropertyJSONElementVisitor`, which is a `JSONElementVisitor` from `microsphere-lang-model`.
  Walking roots means a class annotated *and* classes it references in the same compilation are handled.
* `process` returns `false`, so other processors can still see `@ConfigurationProperty`.
* `writeMetadata` runs once on `processingOver()` and writes the accumulated array, pretty-printed with two-space
  indentation, through `ResourceProcessor` into `CLASS_OUTPUT`.

Field-level resolution rules (all applied by the visitor):

| JSON key | Source when the annotation attribute is left at its default |
|---|---|
| `name` | the annotated field's **constant value** |
| `type` | `field.asType()` rendered via `TypeUtils.getTypeName(...)` |
| `description` | `Elements.getDocComment(field)` |
| `metadata.declaredClass` / `declaredField` | always derived from the field and its enclosing type |
| `metadata.sources` | the annotation's `source()` array |

### 1.3 Output

```
META-INF/microsphere/configuration-properties.json
```

(`ResourceConstants.CONFIGURATION_PROPERTY_METADATA_RESOURCE`.) Contents: one JSON object per annotated field, in the
shape documented in [Configuration Property Metadata](configuration-metadata.md#1-the-data-model).

### 1.4 How the output is assembled

After the annotation-derived entries, the processor appends whatever the runtime SPI chain produces:

```java
ConfigurationPropertyLoader.loadAll()          // all registered loaders, priority-sorted
    .forEach(property -> json.append(generator.generate(property)).append(","));
```

The generator is resolved in the visitor's constructor with `loadFirstService(ConfigurationPropertyGenerator.class)`
(`DefaultConfigurationPropertyGenerator` is registered by `microsphere-java-core`).

> [!NOTE]
> Consequence: the metadata in your jar can contain entries you never annotated — they come from
> `AdditionalMetadataResourceConfigurationPropertyLoader` reading
> `META-INF/microsphere/additional-configuration-properties.json` on the **processor's** classpath, and from any
> `ConfigurationPropertyLoader` you register alongside the processor. The module's own test does exactly this with
> `TestConfigurationPropertyLoader`. This is the supported way to inject properties that cannot be expressed as
> annotated fields.

The writer helpers are `FilerProcessor` (safe `Filer` callbacks) and `ResourceProcessor` (writes a resource into
`CLASS_OUTPUT`, i.e. `target/classes`).

---

## 2. `microsphere-lang-model` — helpers over `javax.lang.model.*`

16 types in `io.microsphere.lang.model.element` and `io.microsphere.lang.model.util`.

### 2.1 The call style

Ten of them are `interface` types that `extends io.microsphere.util.Utils` and declare **`static` interface methods**
(not default methods):

```java
public interface ElementUtils extends Utils {
    static boolean isClass(ElementKind kind) { ... }
    // ...
}
```

There is no `INSTANCE` field and no `of(...)` factory — you call them statically, usually with a static import:

```java
import static io.microsphere.lang.model.util.AnnotationUtils.getAnnotation;
import static io.microsphere.lang.model.util.ElementUtils.matchesElementType;
```

> [!NOTE]
> Do not confuse these with `io.microsphere.reflect.TypeUtils` (runtime `java.lang.reflect.Type`) or
> `javax.lang.model.util.Types` (the JDK's own). The `lang.model` family operates on compile-time mirrors.

### 2.2 Type inventory

| Type | Kind | Purpose |
|---|---|---|
| `element.StringAnnotationValue` | `class implements AnnotationValue` (`@Immutable`) | supply a `String` annotation value outside a real `AnnotationMirror` — `(String value)`, `getValue()`, `accept(AnnotationValueVisitor, P)` → `visitString` |
| `util.ElementUtils` | interface | `ElementKind`/`ElementType` predicates and element filtering |
| `util.AnnotationUtils` | interface | ~50 statics for finding and reading `AnnotationMirror`s |
| `util.TypeUtils` | interface | ~130 statics over `TypeMirror`/`DeclaredType`/`TypeElement` |
| `util.ClassUtils` | interface | `getClassName(TypeMirror)`, `loadClass(TypeMirror)`, `loadClass(String)` |
| `util.MethodUtils` / `FieldUtils` / `ConstructorUtils` / `MemberUtils` | interface | member-element queries |
| `util.MessagerUtils` | interface | javac diagnostics that also log |
| `util.LoggerUtils` | interface | `trace/debug/info/warn/error(String format, Object... args)` |
| `util.ExecutableElementComparator` | `class implements Comparator<ExecutableElement>` | `public static final ExecutableElementComparator INSTANCE` (private ctor) |
| `util.JSONElementVisitor` | `abstract class extends ElementKindVisitor6<Boolean, StringBuilder>` | the base for AST→JSON visitors |
| `util.AnnotatedElementJSONElementVisitor` | `abstract class extends JSONElementVisitor` | same, restricted to elements annotated with one annotation type |
| `util.JSONAnnotationValueVisitor` | `class extends SimpleAnnotationValueVisitor6<StringBuilder, ExecutableElement>` | render an annotation value as JSON |
| `util.ResolvableAnnotationValueVisitor` | `class extends SimpleAnnotationValueVisitor6<Object, ExecutableElement>` | resolve annotation values to Java objects |

### 2.3 `ElementUtils`

```java
static boolean isClass(ElementKind) / isInterface / isDeclaredType / isField / isExecutable / isMember
                    / isInitializer / isVariable
static ElementKind toElementKind(ElementType)
static boolean matchesElementType(ElementKind, ElementType)
static boolean matchesElementType(ElementKind, ElementType...)
static boolean matchesElementType(Element, ElementType...)
static boolean matchesElementKind(Element, ElementKind)
static boolean isPublicNonStatic(Element)
static boolean hasModifiers(Element, Modifier...)
static <E extends Element> List<E> filterElements(List<E>, Predicate<? super E>...)
static boolean matchParameterTypes(ExecutableElement, Type...)
static boolean matchParameterTypes(List<? extends VariableElement>, Type...)
static boolean matchParameterTypeNames(List<? extends VariableElement>, CharSequence...)
```

`matchesElementType(Element, ElementType...)` is the guard every annotation processor needs — it answers "may this
annotation legally appear here?" without hand-writing the switch.

### 2.4 `AnnotationUtils` (representative)

```java
static AnnotationMirror getAnnotation(AnnotatedConstruct, Class<? extends Annotation>)
static AnnotationMirror getAnnotation(AnnotatedConstruct, CharSequence annotationTypeName)
static List<AnnotationMirror> getAnnotations(...)                       // several overloads
static AnnotationMirror findAnnotation(TypeMirror | Element, Class | CharSequence)
static AnnotationMirror findMetaAnnotation(Element, Class | CharSequence)   // meta-annotations
static boolean isAnnotationPresent(Element, Class | CharSequence)
static List<AnnotationMirror> findAnnotations(AnnotatedConstruct, Predicate<? super AnnotationMirror>...)
static Map<String, Object> getAttributesMap(AnnotationMirror)           // + overloads
static <T> T getAttribute(AnnotationMirror, String attributeName)
static <T> T getAttribute(AnnotationMirror, String attributeName, T ifAbsent)
static <T> T getValue(AnnotationMirror)                                 // the single "value" member
static ElementType[] getElementTypes(AnnotationMirror | DeclaredType)
static String getAttributeName(ExecutableElement)
static boolean matchesAttributeMethod(ExecutableElement, String)
static boolean matchesAttributeValue(AnnotationValue, AnnotationValue) / (AnnotationValue, Object)
static boolean matchesDefaultAttributeValue(ExecutableElement, AnnotationValue)
```

`getAttributesMap(...)` is the fast way to read an annotation into a `Map<String, Object>` without visiting every
`AnnotationValue` yourself, and `findMetaAnnotation` is what you need to honour nickname-style annotations such as
the ones in [microsphere-java-annotations](annotations.md#1-reference).

### 2.5 `MessagerUtils` and `LoggerUtils`

```java
// each in two forms: (ProcessingEnvironment, pattern, args...) and (Messager, pattern, args...)
static void printNote(...)
static void printWarning(...)
static void printMandatoryWarning(...)
static void printError(...)
static void printMessage(Diagnostic.Kind kind, ...)
```

They route the same text both to javac's `Messager` and to `LoggerUtils`, so a build shows the message and your
processor's own log file has a record. Messages use `{}` placeholders via `FormatUtils.format`.

### 2.6 `JSONElementVisitor` — writing an AST→JSON visitor

```java
public abstract class JSONElementVisitor extends ElementKindVisitor6<Boolean, StringBuilder> {

    public JSONElementVisitor()                       // super(FALSE)

    @Override public final Boolean visitPackage(PackageElement, StringBuilder)
    @Override public final Boolean visitVariable(VariableElement, StringBuilder)
    @Override public final Boolean visitExecutable(ExecutableElement, StringBuilder)
    @Override public final Boolean visitType(TypeElement, StringBuilder)
    @Override public final Boolean visitTypeParameter(TypeParameterElement, StringBuilder)

    protected boolean supports(Element element)                  // override point
    protected boolean supportsPackage(PackageElement) / supportsVariable / supportsExecutable
    protected boolean supportsType(TypeElement) / supportsTypeParameter(TypeParameterElement)
    protected boolean doVisitPackage(PackageElement, StringBuilder)
    protected boolean doVisitTypeParameter(TypeParameterElement, StringBuilder)
    protected boolean visitMembers(List<? extends Element> members, StringBuilder builder)
}
```

The `visit*` methods are `final` — the template walks the tree, decides membership through `supports*`, and calls
your `doVisit*` hooks. So you override `supportsVariable` + `doVisit*`, never `visitVariable`.

```java
public class AnnotatedElementJSONElementVisitor extends JSONElementVisitor {

    protected final ProcessingEnvironment processingEnv;
    protected final Elements elements;
    protected final String annotationClassName;
    protected final DeclaredType annotationType;
    protected final TypeElement annotationTypeElement;
    protected final ElementType[] elementTypes;

    protected AnnotatedElementJSONElementVisitor(ProcessingEnvironment processingEnv, String annotationClassName)

    public final String getAnnotationClassName()
    @Override protected boolean supports(Element element)   // matchesElementType(element, elementTypes)
}
```

`ConfigurationPropertyJSONElementVisitor` (package-private in the processor module) is a subclass of
`AnnotatedElementJSONElementVisitor` bound to `"io.microsphere.annotation.ConfigurationProperty"` — the pattern to
copy for your own annotation.

`ExecutableElementComparator.INSTANCE` orders methods by simple name, then parameter count, then parameter type
names — deterministic output for generated metadata and tests.

---

## 3. `microsphere-jdk-tools` — compiling from inside a JVM

`io.microsphere.jdk.tools.compiler.Compiler` wraps `javax.tools.JavaCompiler` for in-process compilation, which is
how processor tests run.

```java
public class Compiler {

    public static final String[] DEFAULT_OPTIONS = {
            "-parameters", "-Xlint:-unchecked", "-nowarn", "-Xlint:deprecation" };

    public Compiler()                                       // target = <root>/target/generated-classes
    public Compiler(File targetDirectory)
    public Compiler(File defaultSourceDirectory, File targetDirectory)

    // Fluent, each returns this
    public Compiler options(String... options)
    public Compiler sourcePaths(File... sourcePaths)
    public Compiler sourcePaths(Class<?>... sourceClasses)
    public Compiler sourcePath(Class<?> sourceClass)
    public Compiler processors(Processor... processors)
    public Compiler diagnosticListener(DiagnosticListener<? super JavaFileObject> diagnosticListener)
    public Compiler locale(Locale locale)
    public Compiler charset(Charset charset)

    public boolean compile(Class<?>... sourceClasses) throws IOException

    public JavaCompiler getJavaCompiler()
    public StandardJavaFileManager getJavaFileManager() throws IOException
    public DiagnosticListener<? super JavaFileObject> getDiagnosticListener()
    public Locale getLocale()
    public Charset getCharset()
    public List<String> getOptions()
    public Set<Processor> getProcessors()

    public static File detectSourcePath(Class<?> sourceClass)
    public static File detectRootDirectory(Class<?> sourceClass)
    public static File detectClassPath(Class<?> sourceClass)
    public static String resolveJavaSourceFileRelativePath(Class<?> sourceClass)
}
```

### 3.1 Usage

```java
Compiler compiler = new Compiler()
        .options("-parameters", "-proc:only")
        .sourcePaths(MyConfiguration.class)
        .processors(new ConfigurationPropertyAnnotationProcessor())
        .diagnosticListener(diagnostic -> System.out.println(diagnostic.getMessage(Locale.getDefault())))
        .charset(StandardCharsets.UTF_8);

boolean success = compiler.compile(MyConfiguration.class);
// generated: <moduleRoot>/target/generated-classes/META-INF/microsphere/configuration-properties.json
```

### 3.2 Constraints you must respect

* Requires a **JDK**, not a JRE: the constructor asserts `ToolProvider.getSystemJavaCompiler()` is non-null with the
  message *"No Java compiler available. Ensure this process is running on a JDK (not just a JRE)."*
* Source discovery is **file-system based**: `detectRootDirectory(Class)` walks up from the class's code location
  (`.../target/classes` → module root) and expects sources under `src/main/java` / `src/test/java`. Classes loaded
  from a jar, or from bootstrap/inline-compiled locations, cannot be compiled — `detectSourcePath(Test.class)` is
  `null`.
* `detectClassPath(Class)` throws `UnsupportedOperationException` when the class has no `CodeSource` (bootstrap
  classes).
* Default target directory is `<root>/target/generated-classes`, created with `mkdirs()` by the constructor.
* `getOptions()` returns an unmodifiable list and defaults to `DEFAULT_OPTIONS`; `options(...)` **adds** to it rather
  than replacing, so pass `-proc:none` if you want no processing.

---

## 4. Testing a processor

Two options, in increasing fidelity:

1. **Direct**: use `Compiler` yourself — construct it, add your processor, compile classes that carry your
   annotation, then assert on the generated resource. Good for "does it produce the file".
2. **Harness**: extend `AbstractAnnotationProcessingTest` from `microsphere-annotation-test`, which compiles the test
   class before each test method and injects live `ProcessingEnvironment`/`Elements`/`Types`/`RoundEnvironment`
   instances. See [Testing Support](testing.md#3-annotation-processing-harness).

For the harness to see your processor, register it in `src/test/resources/META-INF/services/javax.annotation.processing.Processor`
— the harness adds every `Processor` found by `ServiceLoader` on the test classpath.

---

## 5. See also

* [Annotations](annotations.md) — the `@ConfigurationProperty` contract being processed
* [Configuration Property Metadata](configuration-metadata.md) — the runtime reader/loader/generator chain
* [Testing Support](testing.md) — the JUnit 5 processing harness
* [Reference](reference.md#1-spi-registry) — service file inventory

[← Previous: Configuration Property Metadata](configuration-metadata.md) · [Index](README.md) · [Next: Testing Support →](testing.md)
