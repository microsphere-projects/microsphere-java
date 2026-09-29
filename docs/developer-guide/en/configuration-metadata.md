# Configuration Property Metadata

> Read this page in: [中文](../zh/configuration-metadata.md) · [English](configuration-metadata.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Packages | `io.microsphere.metadata` (SPI) · `io.microsphere.beans` (data model) · `io.microsphere.annotation` (the annotation) |
| Module | `microsphere-java-core` (+ `microsphere-java-annotations`, `microsphere-annotation-processor`) |
| Data document | `META-INF/microsphere/configuration-properties.json` |
| Merge channel | `META-INF/microsphere/additional-configuration-properties.json` |
| SPI chain | `ConfigurationPropertyGenerator` → resource file → `ConfigurationPropertyLoader` + `ConfigurationPropertyReader` |

This is the runtime half of the `@ConfigurationProperty` story: a JSON document describing every
documented configuration property, plus the SPI chain that produces, loads and parses it.

---

## 1. The annotation and the data model

`io.microsphere.annotation.ConfigurationProperty` (`@Retention(RUNTIME)`, `@Target(FIELD)`) marks a
property-name constant:

| Member | Default | Notes |
|---|---|---|
| `name()` | `""` | when omitted, the processor uses the field's **constant value** |
| `type()` | `String.class` | when omitted, the processor uses the field's type |
| `defaultValue()` | `""` | |
| `description()` | `""` | when blank, the processor falls back to the field's Javadoc |
| `required()` | `false` | |
| `source()` | `{}` | use the constants `SYSTEM_PROPERTIES_SOURCE` = `"system-properties"`, `ENVIRONMENT_VARIABLES_SOURCE` = `"environment-variables"`, `APPLICATION_SOURCE` = `"application"` |

The runtime model is `io.microsphere.beans.ConfigurationProperty` — fields `name`, `type` (stored as
a **String**), `value`, `defaultValue`, `required`, `description`, plus a nested `Metadata` object
(`sources`, `targets`, `declaredClass`, `declaredField`). The JSON shape mirrors it one-to-one; this
is a real entry from the file shipped by `microsphere-java-core`:

```json
[
  {
    "name": "microsphere.io.buffer.size",
    "type": "int",
    "defaultValue": "2048",
    "required": false,
    "description": "The buffer size for I/O",
    "metadata": {
      "sources": ["system-properties"],
      "declaredClass": "io.microsphere.io.IOUtils",
      "declaredField": "BUFFER_SIZE_PROPERTY_NAME"
    }
  }
]
```

`value`, `defaultValue` and `description` are emitted only when non-null; `metadata.sources` uses
the source constants above.

---

## 2. Resource locations

Defined in `io.microsphere.constants.ResourceConstants`:

| Constant | Value |
|---|---|
| `METADATA_RESOURCE` | `META-INF/` |
| `MICROSPHERE_METADATA_RESOURCE` | `META-INF/microsphere/` |
| `CONFIGURATION_PROPERTY_METADATA_FILE_NAME` | `configuration-properties.json` |
| `ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_FILE_NAME` | `additional-configuration-properties.json` |
| `CONFIGURATION_PROPERTY_METADATA_RESOURCE` | **`META-INF/microsphere/configuration-properties.json`** |
| `ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE` | **`META-INF/microsphere/additional-configuration-properties.json`** |

> [!IMPORTANT]
> These paths are **not** Spring Boot's `spring-configuration-metadata.json` /
> `additional-spring-configuration-metadata.json`, so a Spring project will not pick them up by
> itself. `microsphere-java-core` ships a filled-in
> `META-INF/microsphere/configuration-properties.json` describing its own properties.

---

## 3. The compile-time half

`microsphere-annotation-processor`'s `ConfigurationPropertyAnnotationProcessor` visits each
`@ConfigurationProperty` field, builds a `ConfigurationProperty` from the annotation attributes
(filling `name`/`type`/`description` from the field itself when defaulted), serializes it through
the first `ConfigurationPropertyGenerator` service found, then parses the accumulated array and
pretty-prints it with `JSONArray.toString(2)` into
`META-INF/microsphere/configuration-properties.json` of **your** jar. See
[Annotation Processing](annotation-processing.md) for details.

---

## 4. The three runtime SPIs

```java
public interface ConfigurationPropertyReader extends Prioritized {
    default List<ConfigurationProperty> read(InputStream inputStream) throws Throwable // decodes with DEFAULT_CHARSET
    default List<ConfigurationProperty> read(Reader reader) throws Throwable           // stringifies via IOUtils.copyToString
    List<ConfigurationProperty> read(String content) throws Throwable                  // implement this one
}

public interface ConfigurationPropertyLoader extends Prioritized {
    @Nullable List<ConfigurationProperty> load() throws Throwable;
    @Nonnull @Immutable static List<ConfigurationProperty> loadAll();
}

public interface ConfigurationPropertyGenerator extends Prioritized {
    String generate(ConfigurationProperty configurationProperty) throws IllegalArgumentException;
}
```

Implementations shipped in `io.microsphere.metadata`:

| Role | Class | Priority | Notes |
|---|---|---|---|
| Reader | `DefaultConfigurationPropertyReader` | `MIN_PRIORITY` | parses the JSON array via `JSONUtils.readValues` + `io.microsphere.json` |
| Loader | `ClassPathResourceConfigurationPropertyLoader` | — | **abstract** base: any classpath resource, first or all occurrences |
| Loader | `MetadataResourceConfigurationPropertyLoader` | `MIN_PRIORITY` | loads `CONFIGURATION_PROPERTY_METADATA_RESOURCE`; **not SPI-registered** |
| Loader | `AdditionalMetadataResourceConfigurationPropertyLoader` | `MIN_PRIORITY + 9` | loads `ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE` |
| Generator | `DefaultConfigurationPropertyGenerator` | `MIN_PRIORITY` | hand-built JSON, keys in documented order, skips null optionals |
| Generator | `ReflectiveConfigurationPropertyGenerator` | — | delegates to `JSONUtils.writeBeanAsString(configurationProperty)` |

The registered providers in `microsphere-java-core/src/main/resources/META-INF/services/`:

```
io.microsphere.metadata.ConfigurationPropertyReader     -> io.microsphere.metadata.DefaultConfigurationPropertyReader
io.microsphere.metadata.ConfigurationPropertyLoader     -> io.microsphere.metadata.AdditionalMetadataResourceConfigurationPropertyLoader
io.microsphere.metadata.ConfigurationPropertyGenerator  -> io.microsphere.metadata.DefaultConfigurationPropertyGenerator
```

> [!IMPORTANT]
> Only the **additional**-resource loader is SPI-registered. Out of the box,
> `ConfigurationPropertyLoader.loadAll()` therefore reads
> `META-INF/microsphere/additional-configuration-properties.json`, **not**
> `configuration-properties.json`. To read the main resource, instantiate
> `MetadataResourceConfigurationPropertyLoader` yourself or register it in your own services file.

> [!NOTE]
> Only `ConfigurationPropertyLoader` has a static aggregator (`loadAll()`). A reader is obtained
> through `ServiceLoaderUtils.loadFirstService(ConfigurationPropertyReader.class)`.

---

## 5. Loading at runtime

```java
List<ConfigurationProperty> properties = ConfigurationPropertyLoader.loadAll();

for (ConfigurationProperty property : properties) {
    System.out.printf("%s (%s) = %s  [%s]%n",
            property.getName(),
            property.getType(),
            property.getDefaultValue(),
            String.join(",", property.getMetadata().getSources()));
}
```

`loadAll()` is a static interface method; its behaviour, read from source:

1. `ServiceLoaderUtils.loadServicesList(ConfigurationPropertyLoader.class)` — priority-sorted;
2. call `load()` on every loader, appending non-empty results into a `LinkedList`;
3. catch `Throwable` **per loader**, log an error, and continue with the next one;
4. return an **unmodifiable** list. No `throws` — failures never escape.

Because loaders are independent, one broken loader cannot stop discovery.

### Extending `ClassPathResourceConfigurationPropertyLoader`

```java
protected ClassPathResourceConfigurationPropertyLoader(String resourceName)                       // loadedAll = false
protected ClassPathResourceConfigurationPropertyLoader(String resourceName, ClassLoader classLoader)
protected ClassPathResourceConfigurationPropertyLoader(String resourceName, boolean loadedAll)
protected ClassPathResourceConfigurationPropertyLoader(String resourceName, ClassLoader classLoader, boolean loadedAll)

@Override public final List<ConfigurationProperty> load() throws Throwable    // FINAL
```

With `loadedAll = true` it iterates `classLoader.getResources(resourceName)` and merges every
classpath occurrence — the behaviour you want for per-module metadata; with `false` it reads only
the first resource stream. Missing resources are trace-logged and skipped. Parsing is done by a
hard-coded `DefaultConfigurationPropertyReader`.

```java
public class AppConfigPropertiesLoader extends ClassPathResourceConfigurationPropertyLoader {

    public AppConfigPropertiesLoader() {
        super("META-INF/microsphere/app-configuration-properties.json", true);
    }

    @Override
    public int getPriority() {
        return Prioritized.NORMAL_PRIORITY;      // ahead of the built-in loaders
    }
}
```

```
# META-INF/services/io.microsphere.metadata.ConfigurationPropertyLoader
com.example.config.AppConfigPropertiesLoader
```

---

## 6. Merging and hand-written metadata

The intended way to add properties you did not annotate (third-party switches, legacy keys) is to
maintain `META-INF/microsphere/additional-configuration-properties.json` yourself: it is an array
of the objects described in [§1](#1-the-annotation-and-the-data-model), and
`AdditionalMetadataResourceConfigurationPropertyLoader` — registered in core — contributes it to
`loadAll()` at runtime, merging occurrences from every jar on the classpath.

> [!WARNING]
> `DefaultConfigurationPropertyReader` requires, for every element: a `metadata` object (accessed
> unconditionally) and a `required` boolean (unboxed without a null check). A document omitting
> either throws an NPE during `loadAll()` — and because `loadAll()` swallows loader failures with an
> error log, your entries silently disappear rather than crash the call. Always emit all seven
> keys, exactly as `DefaultConfigurationPropertyGenerator` does.

> [!NOTE]
> The annotation processor does **not** read the additional resource; the merge happens only at
> runtime through the registered loader.

---

## 7. Generating metadata from a bean

```java
ConfigurationPropertyGenerator generator =
        ServiceLoaderUtils.loadFirstService(ConfigurationPropertyGenerator.class);
String json = generator.generate(property);   // property is a ConfigurationProperty instance
```

Both generators take a `ConfigurationProperty` and produce its JSON text:
`DefaultConfigurationPropertyGenerator` builds it manually with `JSONUtils.append*` in the key
order `name, type, value, defaultValue, required, description, metadata`, skipping null optionals;
`ReflectiveConfigurationPropertyGenerator` serializes the same object generically via
`JSONUtils.writeBeanAsString`. All three SPIs are `Prioritized`, so
`loadFirstService(...)` respects your override:

```java
public class CompactGenerator implements ConfigurationPropertyGenerator {
    @Override public String generate(ConfigurationProperty property) { /* ... */ return "{}"; }
    @Override public int getPriority() { return Prioritized.MAX_PRIORITY; }
}
```

---

## 8. The whole chain

```
@ConfigurationProperty on a field                        (annotations module)
        |  compile time: ConfigurationPropertyAnnotationProcessor
        |  via the first ConfigurationPropertyGenerator service
        v
META-INF/microsphere/configuration-properties.json       (your jar)
        |  runtime: ConfigurationPropertyLoader.loadAll()
        |  -> registered loaders -> ClassPathResourceConfigurationPropertyLoader
        |  -> DefaultConfigurationPropertyReader (io.microsphere.json)
        v
List<ConfigurationProperty>
        +  META-INF/microsphere/additional-configuration-properties.json
           (merged at runtime by AdditionalMetadataResourceConfigurationPropertyLoader)
```

---

## See also

* [JSON](json.md) — the parser `DefaultConfigurationPropertyReader` uses
* [Annotations](annotations.md) — what you can annotate and which defaults are filled in
* [Annotation Processing](annotation-processing.md) — the compile-time half
* [Reference](reference.md) — all service files

[← Handbook index](../README.md) · [Previous: JSON](json.md) · [Next: Annotation Processing →](annotation-processing.md)
