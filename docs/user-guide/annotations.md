# Annotations

**Module**: `io.github.microsphere-projects:microsphere-java-annotations`
**Packages**: `io.microsphere.annotation`, `io.microsphere.annotation.concurrent`
**File**: `microsphere-java-annotations/src/main/java/io/microsphere/annotation/`

Eight `@Documented` annotation types. They cost nothing at runtime (except `@ConfigurationProperty`) and are the
vocabulary the rest of the framework's Javadoc and tooling speak.

---

## 1. Reference

| Annotation | `@Target` | `@Retention` | Members |
|---|---|---|---|
| `@Since` | `TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE` | `RUNTIME` | `String value()` (required) · `String module() default ""` |
| `@Experimental` | `ANNOTATION_TYPE, CONSTRUCTOR, FIELD, METHOD, TYPE` | `SOURCE` | `String description() default ""` |
| `@Immutable` | *(none declared)* | `RUNTIME` | marker |
| `@Nonnull` | *(none declared)* | `RUNTIME` | marker; meta-annotated `@javax.annotation.Nonnull` + `@TypeQualifierNickname` |
| `@Nullable` | *(none declared)* | `RUNTIME` | marker; meta-annotated `@javax.annotation.Nonnull(when = When.MAYBE)` + `@TypeQualifierNickname` |
| `@ConfigurationProperty` | **`FIELD` only** | `RUNTIME` | see [§3](#3-configurationproperty) |
| `@ThreadSafe` (`concurrent`) | `TYPE` | `CLASS` | marker |
| `@NotThreadSafe` (`concurrent`) | `TYPE` | `CLASS` | marker |

> [!NOTE]
> Three details that surprise people:
> 1. `@Nonnull` / `@Nullable` / `@Immutable` declare **no `@Target`**, so they are legal almost anywhere — but they
>    carry no enforcement from this module; they exist for readers and for static-analysis tools that understand
>    JSR-305 nicknames (that is why `jsr305` is an `optional` dependency).
> 2. `@Experimental` is `RetentionPolicy.SOURCE`: it disappears at compile time and is purely a review signal.
> 3. `@ThreadSafe` / `@NotThreadSafe` are `CLASS` retention, so they are visible to tools reading bytecode-in-memory
>    but not via runtime reflection.

---

## 2. Usage

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

@NotThreadSafe   // StopWatch is genuinely not thread-safe; see Core Utilities
public class MyProfiler { /* ... */ }
```

`@Since` accepts a module qualifier, which is how you tag an API against a released module version:

```java
@Since(value = "1.2.0", module = "microsphere-java-core")
public static String resolveJarAbsolutePath(URL jarURL) { /* ... */ }
```

The values are plain strings; the framework's own Javadoc uses `@since 1.0.0` on essentially every public type,
so treat `@Since` as documentation-grade metadata rather than something enforced by a build step.

---

## 3. `@ConfigurationProperty`

This is the one annotation with a toolchain behind it: the
[annotation processor](annotation-processing.md) reads it and emits machine-readable metadata, and
`io.microsphere.metadata` reads that metadata back at runtime.

```java
public @interface ConfigurationProperty {

    String name() default "";

    Class<?> type() default String.class;

    String defaultValue() default "";

    boolean required() default false;

    String description() default "";

    String[] source() default {};

    String SYSTEM_PROPERTIES_SOURCE    = "system-properties";
    String ENVIRONMENT_VARIABLES_SOURCE = "environment-variables";
    String APPLICATION_SOURCE          = "application";
}
```

### 3.1 Where it goes

`@Target(FIELD)` — **on fields only**, and in practice on `static final` constant fields that hold the property key.
Placing it on a type is a compile error.

```java
public abstract class IOUtils implements Utils {

    @ConfigurationProperty(
        name         = "microsphere.io.buffer.size",
        type         = int.class,
        defaultValue = "2048",
        description  = "The buffer size for IO operations",
        source       = ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE
    )
    public static final int BUFFER_SIZE = getInteger(BUFFER_SIZE_PROPERTY_NAME, DEFAULT_BUFFER_SIZE);
}
```

Defaults are all "empty-ish", and the processor fills them in for you:

| Member | If you omit it |
|---|---|
| `name` | the field's **constant value** is used — so annotating `public static final String KEY = "a.b.c"` needs no `name` |
| `type` | the field's declared type (`field.asType()`) |
| `description` | the field's **Javadoc** (`Elements.getDocComment`) |
| `metadata.declaredClass` / `declaredField` | always derived from the annotated element |
| `source` | `metadata.sources` is emitted as provided (empty set if unset) |

### 3.2 Multiple keys in one holder

Annotate each constant field; the processor visits every root element in the round and writes one JSON object per
annotated field. `microsphere-java-test`'s `ConfigurationPropertyModel` is the canonical example — five annotated
fields named `microsphere.annotation.processor.model.<x>`.

### 3.3 What you get out of it

Compiling a module that uses `@ConfigurationProperty` produces
`META-INF/microsphere/configuration-properties.json` inside your jar. At runtime,
`ConfigurationPropertyLoader.loadAll()` returns those entries as `io.microsphere.beans.ConfigurationProperty`
objects — see [Configuration Property Metadata](configuration-metadata.md).

---

## 4. Relationship to `io.microsphere.annotation` in other modules

Two similarly named packages exist elsewhere and are unrelated to this module:

* `io.microsphere.test.annotation` — in `microsphere-java-test`: `@TestAnnotation`, a fixture that exercises every
  annotation attribute shape ([Testing Support](testing.md)).
* `io.microsphere.annotation.processor` — in `microsphere-annotation-processor`: the processor implementation
  ([Annotation Processing](annotation-processing.md)).

---

## 5. See also

* [Annotation Processing](annotation-processing.md) — turning these annotations into JSON metadata
* [Configuration Property Metadata](configuration-metadata.md) — reading that metadata at runtime
* [Reference](reference.md) — thread-safety notes that back `@ThreadSafe` / `@NotThreadSafe` claims

[← Previous: Getting Started](getting-started.md) · [Index](README.md) · [Next: Core Utilities →](core-utilities.md)
