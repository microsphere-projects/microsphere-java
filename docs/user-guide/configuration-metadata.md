# Configuration Property Metadata

**Module**: `io.github.microsphere-projects:microsphere-java-core`
**Package**: `io.microsphere.metadata` (+ `io.microsphere.beans.ConfigurationProperty`)

The runtime half of the `@ConfigurationProperty` story: a JSON document that describes every documented property, and
a three-SPI chain that produces, loads and parses it.

---

## 1. The data model

`io.microsphere.beans.ConfigurationProperty` (see
[Reflection and Types](reflection-and-types.md#11-io-microsphere-beans--bean-introspection) for the full member list)
carries `name`, `type` (as a **String**), `value`, `defaultValue`, `required`, `description`, plus a nested `Metadata`
object with `sources`, `targets`, `declaredClass`, `declaredField`.

The JSON shape mirrors it one-to-one:

```json
[
  {
    "name": "microsphere.io.buffer.size",
    "type": "int",
    "defaultValue": "2048",
    "required": false,
    "description": "The buffer size for IO operations",
    "metadata": {
      "sources": ["system-properties"],
      "declaredClass": "io.microsphere.io.IOUtils",
      "declaredField": "BUFFER_SIZE_PROPERTY_NAME"
    }
  }
]
```

`value` is emitted only when non-null; `defaultValue`, `description` likewise. `metadata.sources` uses the
[`@ConfigurationProperty` source constants](annotations.md#3-configurationproperty):
`system-properties`, `environment-variables`, `application`.

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
> The paths are `META-INF/microsphere/*.json`. They are **not** Spring Boot's
> `spring-configuration-metadata.json` / `additional-spring-configuration-metadata.json`, so a Spring project will not
> pick them up by itself. `microsphere-java-core` ships a real, filled-in
> `META-INF/microsphere/configuration-properties.json` describing its own properties.

---

## 3. The three SPIs

```java
public interface ConfigurationPropertyReader extends Prioritized {

    default List<ConfigurationProperty> read(InputStream inputStream) throws Throwable
    default List<ConfigurationProperty> read(Reader reader) throws Throwable
    List<ConfigurationProperty> read(String content) throws Throwable          // the method you implement
}

public interface ConfigurationPropertyLoader extends Prioritized {
    @Nullable List<ConfigurationProperty> load() throws Throwable;
    @Nonnull @Immutable static List<ConfigurationProperty> loadAll();
}

public interface ConfigurationPropertyGenerator extends Prioritized {
    String generate(ConfigurationProperty configurationProperty) throws IllegalArgumentException;
}
```

> [!NOTE]
> Only `ConfigurationPropertyLoader` has a static aggregator (`loadAll()`). A reader is obtained through
> `ServiceLoaderUtils.loadFirstService(ConfigurationPropertyReader.class)` and then invoked directly.

> [!NOTE]
> `read(InputStream)` decodes with `CharsetUtils.DEFAULT_CHARSET`; `read(Reader)` stringifies via
> `IOUtils.copyToString`. Implement only `read(String)` and you inherit both.

### Implementations

| Role | Class | Priority | Notes |
|---|---|---|---|
| Reader | `DefaultConfigurationPropertyReader` | `MIN_PRIORITY` | parses the JSON array above with `io.microsphere.json` |
| Loader | `ClassPathResourceConfigurationPropertyLoader` | — | **abstract** base: any resource name, single or all occurrences |
| Loader | `MetadataResourceConfigurationPropertyLoader` | `MIN_PRIORITY` | concrete; `CONFIGURATION_PROPERTY_METADATA_RESOURCE` |
| Loader | `AdditionalMetadataResourceConfigurationPropertyLoader` | `MIN_PRIORITY + 9` | concrete; `ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE` |
| Generator | `DefaultConfigurationPropertyGenerator` | — | JSON emission via `JSONUtils` |
| Generator | `ReflectiveConfigurationPropertyGenerator` | — | builds from a bean via `JSONUtils.writeBeanAsString` |

The registered set lives in `microsphere-java-core/src/main/resources/META-INF/services/`:

```
io.microsphere.metadata.ConfigurationPropertyReader     -> DefaultConfigurationPropertyReader
io.microsphere.metadata.ConfigurationPropertyLoader     -> AdditionalMetadataResourceConfigurationPropertyLoader
io.microsphere.metadata.ConfigurationPropertyGenerator  -> DefaultConfigurationPropertyGenerator
```

---

## 4. Loading at runtime

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

`loadAll()` is a **static interface method** and its behaviour, read from source:

1. `ServiceLoaderUtils.loadServicesList(ConfigurationPropertyLoader.class)` — priority-sorted;
2. call `load()` on every loader, appending non-empty results into a `LinkedList`;
3. catch `Throwable` **per loader**, log an error, and continue with the next one;
4. return an **unmodifiable** list. No `throws` on `loadAll()` — failures never escape.

Because each loader is independent, you can add as many sources as you like, and one broken loader cannot stop
discovery.

### Loading your own resource

`ClassPathResourceConfigurationPropertyLoader` is the base to extend:

```java
protected ClassPathResourceConfigurationPropertyLoader(String resourceName)
protected ClassPathResourceConfigurationPropertyLoader(String resourceName, ClassLoader classLoader)
protected ClassPathResourceConfigurationPropertyLoader(String resourceName, boolean loadedAll)
protected ClassPathResourceConfigurationPropertyLoader(String resourceName, ClassLoader classLoader, boolean loadedAll)

@Override public final List<ConfigurationProperty> load() throws Throwable    // FINAL
```

`load()` is `final`. With `loadedAll = true` it iterates `classLoader.getResources(resourceName)` and merges every
occurrence on the classpath (the behaviour you want for per-module metadata); with `false` it reads only the first
resource stream. Missing resources are trace-logged and skipped, not errors.

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

## 5. Writing metadata by hand

If you do not want to depend on the annotation processor, maintain
`META-INF/microsphere/additional-configuration-properties.json` yourself — that resource is exactly what
`AdditionalMetadataResourceConfigurationPropertyLoader` contributes to `loadAll()`, and it is the channel the
processor uses to merge non-annotated properties into its output
([Annotation Processing](annotation-processing.md#4-how-the-output-is-assembled)).

A document is simply an array of the objects described in [§1](#1-the-data-model). The `name` is required; prefer
`type` as a plain class name (`"int"`, `"java.time.Duration"`) since it is stored as a string.

---

## 6. Generating a `ConfigurationProperty` from a bean

```java
public abstract class ConfigurationProperty implements /* io.microsphere.beans */ { ... }

// Generate JSON text for one property
ConfigurationPropertyGenerator generator = ServiceLoaderUtils.loadFirstService(ConfigurationPropertyGenerator.class);
String json = generator.generate(property);
```

`DefaultConfigurationPropertyGenerator` emits the keys in the documented order (`name`, `type`, `value`,
`defaultValue`, `required`, `description`, `metadata`) and skips null optionals.
`ReflectiveConfigurationPropertyGenerator` derives the same object from a bean's readable properties via
`BeanUtils.resolvePropertiesAsMap` — the right choice when your properties come from a configuration class rather
than annotated constants.

Both are `Prioritized`, so `loadFirstService(ConfigurationPropertyGenerator.class)` respects your override:

```java
public class CompactGenerator implements ConfigurationPropertyGenerator {
    @Override public String generate(ConfigurationProperty property) { /* ... */ return "{}"; }
    @Override public int getPriority() { return Prioritized.MAX_PRIORITY; }
}
```

---

## 7. How this fits the whole chain

```
@ConfigurationProperty on a static final field          (annotations module)
            |  compile time: ConfigurationPropertyAnnotationProcessor
            v
META-INF/microsphere/configuration-properties.json      (your jar)
            |  runtime: MetadataResourceConfigurationPropertyLoader -> DefaultConfigurationPropertyReader
            v
List<ConfigurationProperty>  <- ConfigurationPropertyLoader.loadAll()
            |  merge point for the annotation processor's extra entries
            v
IDE completion / diagnostics / your own config tooling
```

---

## 8. See also

* [Annotations](annotations.md#3-configurationproperty) — what you can annotate and which defaults are filled in
* [Annotation Processing](annotation-processing.md) — the compile-time half
* [JSON](json.md) — the parser `DefaultConfigurationPropertyReader` uses
* [Reference](reference.md#1-spi-registry) — all service files

[← Previous: JSON](json.md) · [Index](README.md) · [Next: Annotation Processing →](annotation-processing.md)
