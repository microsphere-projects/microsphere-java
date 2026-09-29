# Type Conversion

**Module**: `io.github.microsphere-projects:microsphere-java-core`
**Packages**: `io.microsphere.convert`, `io.microsphere.convert.multiple`, `io.microsphere.io.serializer`

Three related SPIs: scalar conversion (`Converter`), collection/array conversion (`MultiValueConverter`), and
binary serialization (`Serializer` / `Deserializer`). All are resolved through
[`ServiceLoaderUtils`](core-utilities.md#5-serviceloaderutils) and ordered by
[`Prioritized`](language-abstractions.md#1-prioritized--the-ordering-spine).

---

## 1. `Converter<S, T>`

`microsphere-java-core/src/main/java/io/microsphere/convert/Converter.java`

```java
@FunctionalInterface
public interface Converter<S, T> extends Prioritized {

    @Nullable T convert(@Nullable S source);                       // the lambda target

    default boolean accept(Class<?> sourceType, Class<?> targetType)
    default Class<S> getSourceType()
    default Class<T> getTargetType()

    static <S, T> Converter<S, T> getConverter(Class<S> sourceType, Class<T> targetType)
    static <T> T convertIfPossible(Object source, Class<T> targetType)
}
```

> [!NOTE]
> `getConverter` and `convertIfPossible` are **`static` methods on the interface** (Java 8 interface statics), so you
> call `Converter.getConverter(...)` — you never reference the engine directly. The implementing class `Converters` is
> **package-private** and is not part of the public API.

### 1.1 Use

```java
Integer fortyTwo = Converter.convertIfPossible("42", Integer.class);       // 42
String text      = Converter.convertIfPossible(42, String.class);          // "42"
Duration five    = Converter.convertIfPossible("PT5S", Duration.class);    // PT5S

Converter<String, Integer> converter = Converter.getConverter(String.class, Integer.class);
Integer value = converter.convert("7");
```

`convertIfPossible` returns **`null`** when no converter matches; `getConverter` returns the highest-priority match.
Conversion failures surface as unchecked exceptions (see `AbstractConverter` in §1.3).

### 1.2 Built-in converters

Registered in `microsphere-java-core/src/main/resources/META-INF/services/io.microsphere.convert.Converter`
(34 entries). Grouped by source type:

| From | To |
|---|---|
| `String` | `Boolean`, `Byte`, `Character`, `char[]`, `Short`, `Integer`, `Long`, `Float`, `Double`, `String`, `Class`, `Duration`, `InputStream` |
| `Number` | `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`, `Character` |
| `Object` | `String`, `Boolean`, `Byte`, `Character`, `Short`, `Integer`, `Long`, `Float`, `Double`, `byte[]`, `Optional` |
| `byte[]` | `Object` |
| `Map` | `Properties` |
| `Properties` | `String` |

`StringToClassConverter`, `StringToDurationConverter` and `StringToInputStreamConverter` are the ones people are
usually surprised to find already present.

> [!NOTE]
> `ObjectToOptionalConverter` is listed **twice** in the services file. Java SPI deduplicates by class name at load
> time, so the effect is harmless, but do not copy the pattern.

### 1.3 `AbstractConverter<S, T>` — the safe base class

```java
public abstract class AbstractConverter<S, T> implements Converter<S, T> {

    protected final Logger logger;                       // io.microsphere.logging

    public AbstractConverter()                            // computes priority via resolvePriority()

    @Nullable public final T convert(@Nullable S source)  // FINAL: null check + try/catch + rethrow unchecked
    @Nullable protected abstract T doConvert(@Nonnull S source) throws Throwable;

    protected Integer resolvePriority()
    @Override public int getPriority()
    @Override public boolean equals(Object o)
    @Override public int hashCode()
}
```

Two consequences:

1. `convert` is `final`. Subclasses implement **`doConvert`**, which may throw anything — the base class turns a
   `null` source into `null`, and a failure into a `RuntimeException` after logging.
2. `resolvePriority()` computes `-( (getAllClasses(sourceType).size() << 16) | getAllClasses(targetType).size() )`,
   i.e. a **negative** (higher-than-normal) priority, so a more specific converter beats a general one automatically.
   Type-hierarchy depth therefore decides precedence — `ClassUtils.getAllClasses` from
   [Core Utilities](core-utilities.md#2-classutils) is the input.

`StringConverter<T>` is a narrowing of the contract:

```java
@FunctionalInterface
public interface StringConverter<T> extends Converter<String, T> {}
```

---

## 2. How lookup and caching work

`io.microsphere.convert.Converters` (package-private):

```java
class Converters implements Utils {
    static ConcurrentMap<Entry<Class<?>, Class<?>>, List<Converter>> initConvertersCache()
    static <S, T> Converter<S, T> findConverter(Class<S> sourceType, Class<T> targetType)
    static List<Converter> loadConvertersList()
}
```

1. At class load, all registered `Converter` implementations are loaded and sorted by `Prioritized.COMPARATOR`, then
   grouped into `convertersCache` keyed by `Map.Entry<sourceType, targetType>` from each converter's
   `getSourceType()` / `getTargetType()`.
2. `findConverter(S, T)` does `computeIfAbsent` over that map, filtering with `converter.accept(sourceType, targetType)`
   and returning the first (highest-priority) match. The filtered list is then cached forever.

> [!WARNING]
> There is **no cache invalidation API**. No `Converters.invalidate()` exists, the map has no size bound and no LRU,
> and it is not affected by `microsphere.service-loader.cached`. A converter added to the classpath after
> `Converters` has been initialized (for example in a hot-redeploying container) will not be picked up until the
> class loader is replaced. This is a real constraint for OSGi/Servlet-reload setups — restart the class loader, do
> not call a method that does not exist.

The default `accept` compares `getSourceType()` / `getTargetType`, both of which are resolved reflectively from the
converter class's own generic arguments:

```java
default Class<S> getSourceType() {
    return resolveActualTypeArgumentClass(getClass(), Converter.class, 0);
}
```

---

## 3. Writing a custom converter

### 3.1 Implementation

```java
package com.example.convert;

import io.microsphere.convert.Converter;
import io.microsphere.lang.Prioritized;

import java.util.UUID;

public class UUIDToStringConverter implements Converter<UUID, String> {

    @Override
    public String convert(UUID source) {
        return source == null ? null : source.toString();
    }

    @Override
    public int getPriority() {
        return Prioritized.MAX_PRIORITY + 1;      // beats ObjectToStringConverter
    }
}
```

> [!IMPORTANT]
> `S` and `T` must be **statically visible in the `implements` clause**, because `getSourceType()` / `getTargetType()`
> resolve them with `TypeUtils.resolveActualTypeArgumentClass`. A raw `implements Converter` or a lambda assigned to a
> `Converter` variable cannot be discovered by type pair — the resolved types would be `null`/`Object`.

Extension points you may override:

| Method | Override when |
|---|---|
| `convert(S)` | you implement `Converter` directly (it is the functional method) |
| `doConvert(S)` | you extend `AbstractConverter` (recommended: you get null-safety + logging + auto priority) |
| `accept(Class, Class)` | matching is broader than exact type pair (e.g. "any `CharSequence` source") |
| `getPriority()` | you must win or lose against a built-in |
| `getSourceType()` / `getTargetType()` | your class cannot express the types in its `implements` clause |

### 3.2 Register via SPI

`src/main/resources/META-INF/services/io.microsphere.convert.Converter`:

```
com.example.convert.UUIDToStringConverter
```

### 3.3 Use

```java
String s = Converter.convertIfPossible(UUID.randomUUID(), String.class);
// or, when you want the instance (e.g. to inspect priority)
Converter<UUID, String> c = Converter.getConverter(UUID.class, String.class);
```

Prefer extending `AbstractConverter` for anything non-trivial:

```java
public class StringToCurrencyConverter extends AbstractConverter<String, Currency> {

    @Override
    protected Currency doConvert(String source) {
        return Currency.getInstance(source.trim());     // Throwable is logged + wrapped for you
    }
}
```

---

## 4. `MultiValueConverter<S>` — delimited source to collections

`io.microsphere.convert.multiple.MultiValueConverter`:

```java
public interface MultiValueConverter<S> extends Prioritized {

    boolean accept(Class<S> sourceType, Class<?> multiValueType);
    Object convert(S source, Class<?> multiValueType, Class<?> elementType);

    default Class<S> getSourceType()

    static MultiValueConverter<?> find(Class<?> sourceType, Class<?> targetType)
    static <T> T convertIfPossible(Object source, Class<?> multiValueType, Class<?> elementType)
}
```

Registered implementations (11): `StringToArrayConverter`, `StringToCollectionConverter`, `StringToDequeConverter`,
`StringToBlockingDequeConverter`, `StringToBlockingQueueConverter`, `StringToQueueConverter`,
`StringToTransferQueueConverter`, `StringToListConverter`, `StringToSetConverter`,
`StringToNavigableSetConverter`, `StringToSortedSetConverter`.

> [!NOTE]
> `StringToIterableConverter` and the base `StringToMultiValueConverter` exist on disk but are **not** registered, so
> requesting an `Iterable` target falls back to the registered list/set behaviour.

```java
// "a,b,c" -> List<String>
String[] array = MultiValueConverter.convertIfPossible("a,b,c", String[].class, String.class);
List<String> list = MultiValueConverter.convertIfPossible("a,b,c", List.class, String.class);
Set<Integer> ids = MultiValueConverter.convertIfPossible("1,2,3", Set.class, Integer.class);

MultiValueConverter<?> converter = MultiValueConverter.find(String.class, SortedSet.class);
```

Element values are converted by the scalar `Converter` SPI, so `Set<Integer>` above works because
`StringToIntegerConverter` is registered. `find` sorts candidates by `Prioritized` and returns the first `accept`ing
one — the same precedence rule as `Converter`.

This is the layer that turns a single configuration string into a typed collection; combined with
[Configuration Property Metadata](configuration-metadata.md) it is how `String[] source()` style values are usable.

---

## 5. `Serializer` / `Deserializer`

`io.microsphere.io.serializer`:

```java
public interface Serializer<S> {
    byte[] serialize(S source) throws IOException;
}

public interface Deserializer<T> {
    T deserialize(byte[] bytes) throws IOException;
}
```

Lookup is **instance-based**, not static, and you must load the SPI explicitly:

```java
public class Serializers {
    public Serializers()
    public Serializers(ClassLoader classLoader)
    public void loadSPI()                                                 // no-op until called
    @Nonnull public <S> List<Serializer<S>> get(Class<S> serializedType)
    public <S> Serializer<S> getHighestPriority(Class<S> serializedType)
    public <S> Serializer<S> getLowestPriority(Class<S> serializedType)
    public Serializer<?> getMostCompatible(Class<?> serializedType)
}
```

`Deserializers` mirrors it with `Deserializer` types.

```java
Serializers serializers = new Serializers();
serializers.loadSPI();                       // required

byte[] bytes = serializers.getHighestPriority(String.class).serialize("microsphere");

Deserializers deserializers = new Deserializers();
deserializers.loadSPI();
String back = deserializers.getHighestPriority(String.class).deserialize(bytes);
```

Registered implementations:

| Services file | Implementations |
|---|---|
| `META-INF/services/io.microsphere.io.serializer.Serializer` | `BooleanSerializer`, `ByteSerializer`, `CharacterSerializer`, `ShortSerializer`, `IntegerSerializer`, `LongSerializer`, `FloatSerializer`, `DoubleSerializer`, `StringSerializer`, `DefaultSerializer` |
| `META-INF/services/io.microsphere.io.serializer.Deserializer` | the same eight primitive/wrapper types (they implement both interfaces) plus `StringDeserializer`, `DefaultDeserializer` |

> [!WARNING]
> `EnumSerializer` exists in the source tree but is **not registered**, so enum serialization falls through to
> `DefaultSerializer` (Java serialization). Register it yourself in your own services file if you need it.
>
> Semantics: `getHighestPriority(X)` = first match (smallest `getPriority()`); `getLowestPriority(X)` = last; and
> `getMostCompatible(X)` = highest-priority match, falling back to `getLowestPriority(Object.class)` — i.e. it always
> returns something. `DefaultSerializer`/`DefaultDeserializer` are therefore the safety net, not an error.

The maps are per-instance (`Map<Class<?>, List<...>>` keyed by each implementation's first type argument), so
creating a new `Serializers` after adding services to the classpath is the way to pick them up — unlike `Converters`,
this cache is not JVM-global.

---

## 6. Choosing between the three

| Need | Use |
|---|---|
| One value in, one value out | `Converter<S, T>` |
| One delimited string in, a collection/array out | `MultiValueConverter<S>` |
| Value to/from bytes | `Serializer` / `Deserializer` |
| Whole-bean binding from JSON | `JSONUtils.readValue(...)` — see [JSON](json.md) |

---

## 7. See also

* [Language Abstractions](language-abstractions.md#1-prioritized--the-ordering-spine) — priority constants and the
  ordering trap
* [Reflection and Types](reflection-and-types.md#6-typeutils-and-generic-resolution) — how `S`/`T` are discovered
* [Reference](reference.md#1-spi-registry) — the complete services registry

[← Previous: Reflection and Types](reflection-and-types.md) · [Index](README.md) · [Next: Event Dispatching →](events.md)
