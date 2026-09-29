# Annotations

> Read this page in: [中文](../zh/annotations.md) · [English](annotations.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-java-annotations` |
| Packages | `io.microsphere.annotation`, `io.microsphere.annotation.concurrent` |
| Contents | eight `@Documented` annotation types |
| Runtime cost | none, except `@ConfigurationProperty` — the only one with a build-time toolchain |

These annotations are the vocabulary the rest of the library's Javadoc and tooling speak.
Seven of them are pure documentation signals; `@ConfigurationProperty` turns annotated fields
into machine-readable metadata through the annotation processor.

---

## 1. The eight annotations

| Annotation | `@Target` | `@Retention` | Members |
|---|---|---|---|
| `@Since` | `TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE` | `RUNTIME` | `String value()` (required) · `String module() default ""` |
| `@Experimental` | `ANNOTATION_TYPE, CONSTRUCTOR, FIELD, METHOD, TYPE` | `SOURCE` | `String description() default ""` |
| `@Immutable` | *(none declared)* | `RUNTIME` | marker |
| `@Nonnull` | *(none declared)* | `RUNTIME` | marker; meta-annotated `@javax.annotation.Nonnull` + `@TypeQualifierNickname` |
| `@Nullable` | *(none declared)* | `RUNTIME` | marker; meta-annotated `@javax.annotation.Nonnull(when = When.MAYBE)` + `@TypeQualifierNickname` |
| `@ConfigurationProperty` | **`FIELD` only** | `RUNTIME` | see [§3](#3-configurationproperty--the-one-with-a-toolchain) |
| `@ThreadSafe` (`concurrent`) | `TYPE` | `CLASS` | marker |
| `@NotThreadSafe` (`concurrent`) | `TYPE` | `CLASS` | marker |

> [!NOTE]
> Three details that surprise people:
> 1. `@Nonnull` / `@Nullable` / `@Immutable` declare **no `@Target`**, so they are legal almost
>    anywhere — but this module enforces nothing; they exist for readers and for static-analysis
>    tools that understand JSR-305 nicknames (that is why `com.google.code.findbugs:jsr305` is an
>    `optional` dependency of the module).
> 2. `@Experimental` is `RetentionPolicy.SOURCE`: it disappears at compile time and is purely a
>    review signal.
> 3. `@ThreadSafe` / `@NotThreadSafe` are `CLASS` retention, so tools reading bytecode can see
>    them, but runtime reflection cannot.

---

## 2. Annotating your own API

```java
import io.microsphere.annotation.Experimental;
import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.annotation.Since;
import io.microsphere.annotation.concurrent.NotThreadSafe;

@Since("1.0.0")
@Immutable
public final class Artifact {

    @Nonnull
    public String getArtifactId() { /* ... */ return null; }

    @Nullable
    public String getVersion() { /* ... */ return null; }

    @Experimental(description = "Shape may change before 0.4")
    public boolean matches(Artifact other) { /* ... */ return false; }
}

@NotThreadSafe   // e.g. a profiler built on the non-thread-safe StopWatch
public class MyProfiler { /* ... */ }
```

> [!IMPORTANT]
> The declared name is `@Nonnull` — the `n` after `No` is lowercase, mirroring
> JSR-305's `javax.annotation.Nonnull`. Writing `@NonNull` (second capital) does not compile.

`@Since` accepts a module qualifier, which tags an API against a released module version —
for example, on a method such as `JarUtils.resolveJarAbsolutePath(URL)`:

```java
@Since(value = "1.2.0", module = "microsphere-java-core")
public static String resolveJarAbsolutePath(URL jarURL) { /* ... */ }
```

Both members are plain strings. The framework's own Javadoc additionally uses the `@since 1.0.0`
Javadoc tag on essentially every public type, so treat `@Since` as documentation-grade metadata,
not something a build step verifies.

`@ThreadSafe` / `@NotThreadSafe` live in `io.microsphere.annotation.concurrent` and carry the JCIP
meaning: a `@ThreadSafe` class needs no caller-side synchronization, a `@NotThreadSafe` class does.
They apply to types only (`@Target(TYPE)`).

---

## 3. `@ConfigurationProperty` — the one with a toolchain

The [annotation processor](annotation-processing.md) reads this annotation and emits
machine-readable metadata; `io.microsphere.metadata` reads that metadata back at runtime.
Its full member set, exactly as declared:

```java
public @interface ConfigurationProperty {

    String name() default "";

    Class<?> type() default String.class;

    String defaultValue() default "";

    boolean required() default false;

    String description() default "";

    String[] source() default {};

    String SYSTEM_PROPERTIES_SOURCE     = "system-properties";
    String ENVIRONMENT_VARIABLES_SOURCE = "environment-variables";
    String APPLICATION_SOURCE           = "application";
}
```

### 3.1 Where it goes

`@Target(FIELD)` — **on fields only**, in practice on `static final` constant fields that hold a
tuned value. Placing it on a type is a compile error. This is the real declaration in
`io.microsphere.io.IOUtils` (`microsphere-java-core`):

```java
/**
 * The buffer size for I/O
 */
@ConfigurationProperty(
        name = BUFFER_SIZE_PROPERTY_NAME,
        defaultValue = DEFAULT_BUFFER_SIZE_PROPERTY_VALUE,
        description = "The buffer size for I/O",
        source = SYSTEM_PROPERTIES_SOURCE
)
public static final int BUFFER_SIZE = getInteger(BUFFER_SIZE_PROPERTY_NAME, DEFAULT_BUFFER_SIZE);
```

Note what is **not** written: `type`. Every member defaults to "empty-ish", and the processor
fills the gap for you:

| Member | If you omit it |
|---|---|
| `name` | the field's **constant value** is used — annotating `public static final String KEY = "a.b.c"` needs no `name` |
| `type` | the field's declared type (`field.asType()`) |
| `description` | the field's **Javadoc** (`Elements.getDocComment`) |
| `declaredClass` / `declaredField` in the emitted metadata | always derived from the annotated element |
| `source` | emitted as provided — an empty set if unset |

### 3.2 Multiple keys in one holder

Annotate each field you want exposed; the processor visits every annotated element and writes one JSON
object per field. `microsphere-java-test`'s `ConfigurationPropertyModel` is the canonical example
— five annotated fields whose `name`s are `microsphere.annotation.processor.model.<x>`.

---

## 4. What you get out of it

Compiling a module that uses `@ConfigurationProperty` (with the processor on the compile
classpath) produces `META-INF/microsphere/configuration-properties.json` inside your jar.
At runtime, `io.microsphere.metadata.ConfigurationPropertyLoader.loadAll()` — the static SPI entry
point — aggregates every loader into a list of `io.microsphere.beans.ConfigurationProperty`
objects. See [Configuration Property Metadata](configuration-metadata.md) for reading and
filtering that list.

---

## 5. Similarly named packages elsewhere

Two other `io.microsphere.*annotation*` packages exist and are not part of this module:

* `io.microsphere.test.annotation` — in `microsphere-java-test`: `@TestAnnotation`, a fixture that
  exercises every annotation attribute shape ([Testing Support](testing.md)).
* `io.microsphere.annotation.processor` — in `microsphere-annotation-processor`: the processor
  implementation ([Annotation Processing](annotation-processing.md)).

> [!TIP]
> If you only need `@Since` / `@Immutable` / `@Nonnull` / `@Nullable` / `@Experimental`, adding
> `microsphere-java-core` is enough: it depends on `microsphere-java-annotations` transitively.
> You need the annotations artifact directly only for a module that stays annotation-only.

---

## See also

* [Getting Started](getting-started.md) — which artifact to add.
* [Core Utilities](core-utilities.md) — where the thread-safety claims behind `@ThreadSafe` / `@NotThreadSafe` apply.
* [Annotation Processing](annotation-processing.md) — turning `@ConfigurationProperty` into JSON metadata.
* [Configuration Property Metadata](configuration-metadata.md) — reading that metadata at runtime.
* [Reference](reference.md) — the property keys these annotations document.

[← Handbook index](../README.md) · [Next: Core Utilities →](core-utilities.md)
