# Type Conversion

> Read this page in: [中文](../zh/type-conversion.md) · [English](type-conversion.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Packages | `io.microsphere.convert`, `io.microsphere.convert.multiple`, `io.microsphere.io.serializer` |
| SPI files | `META-INF/services/io.microsphere.convert.Converter` (34 entries), `...convert.multiple.MultiValueConverter` (11), `...io.serializer.Serializer` / `Deserializer` (10 each) |
| Public entry points | static methods on the `Converter` and `MultiValueConverter` interfaces; instance-based `Serializers` / `Deserializers` |
| Ordering | every converter `extends Prioritized`; more specific converters win automatically |
| Type discovery | `S`/`T` resolved from the `implements` clause via `TypeUtils.resolveActualTypeArgumentClass` |

Three related SPIs: scalar conversion (`Converter`), collection/array conversion (`MultiValueConverter`),
and binary serialization (`Serializer` / `Deserializer`). All are loaded through
[`ServiceLoaderUtils`](core-utilities.md) and sorted by `Prioritized.COMPARATOR`.

---

## 1. `Converter<S, T>` — the scalar SPI

`io.microsphere.convert.Converter`:

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

> [!IMPORTANT]
> `Converter` is an **SPI**: implementations are registered in
> `META-INF/services/io.microsphere.convert.Converter` and loaded by Java `ServiceLoader`. The engine class
> `Converters` is **package-private** — never reference it. The public entry points are the two `static`
> methods on the interface (Java 8 interface statics): `Converter.getConverter(...)` and
> `Converter.convertIfPossible(...)`.

```java
Integer fortyTwo = Converter.convertIfPossible("42", Integer.class);       // 42
String text      = Converter.convertIfPossible(42, String.class);          // "42"
Duration five    = Converter.convertIfPossible("PT5S", Duration.class);    // PT5S

Converter<String, Integer> converter = Converter.getConverter(String.class, Integer.class);
Integer value = converter.convert("7");
```

`convertIfPossible` returns **`null`** when no converter matches (and NPEs on a `null` source — it calls
`source.getClass()`); `getConverter` returns the highest-priority match. The default `accept` compares
source/target via `isAssignableFrom` against `getSourceType()` / `getTargetType()`.

### Built-in converters

| From | To |
|---|---|
| `String` | `Boolean`, `Byte`, `Character`, `char[]`, `Short`, `Integer`, `Long`, `Float`, `Double`, `String`, `Class`, `Duration`, `InputStream` |
| `Number` | `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`, `Character` |
| `Object` | `String`, `Boolean`, `Byte`, `Character`, `Short`, `Integer`, `Long`, `Float`, `Double`, `byte[]`, `Optional` |
| `byte[]` | `Object` |
| `Map` | `Properties` |
| `Properties` | `String` |

`StringToClassConverter`, `StringToDurationConverter` and `StringToInputStreamConverter` are the ones people
are usually surprised to find already present.

> [!NOTE]
> `ObjectToOptionalConverter` is listed **twice** in the shipped services file. Java SPI deduplicates by
> class name at load time, so the effect is harmless, but do not copy the pattern.

---

## 2. Lookup, priority and caching

The package-private `io.microsphere.convert.Converters` does the work:

1. At class load, all registered implementations are loaded, **sorted by `Prioritized`**, and grouped into
   a `ConcurrentMap` keyed by `Map.Entry<sourceType, targetType>` from each converter's
   `getSourceType()` / `getTargetType()`.
2. `findConverter(sourceType, targetType)` then `computeIfAbsent`s over that map, filtering with
   `converter.accept(sourceType, targetType)` and returning the first (highest-priority) match.

Precedence between competing converters is decided by `AbstractConverter.resolvePriority()`, which computes
`-((getAllClasses(sourceType).size() << 16) | getAllClasses(targetType).size())` — a **negative**
(higher-than-normal) priority whose magnitude shrinks as the types get more derived. A converter for
`String → Integer` therefore beats a generic `Object → Integer` one automatically.

> [!WARNING]
> There is **no cache invalidation API**. No `Converters.invalidate()` exists, the per-pair cache has no
> size bound and no LRU, and the SPI list is loaded with `cached = true` explicitly — so
> `microsphere.service-loader.cached` does not affect it. A converter added to the classpath after
> `Converters` initializes (hot-redeploying container, OSGi refresh) is not picked up until the class loader
> is replaced. Restart the class loader; do not call a method that does not exist.

---

## 3. Writing a custom converter

### 3.1 Implementation

Prefer extending `AbstractConverter<S, T>` for anything non-trivial:

```java
public abstract class AbstractConverter<S, T> implements Converter<S, T> {

    protected final Logger logger;                       // io.microsphere.logging

    public AbstractConverter()                           // computes priority via resolvePriority()

    @Nullable public final T convert(@Nullable S source) // FINAL: null check + try/catch + rethrow unchecked
    @Nullable protected abstract T doConvert(@Nonnull S source) throws Throwable;

    protected Integer resolvePriority()
    @Override public int getPriority()
}
```

`convert` is `final`: it maps a `null` source to `null`, runs `doConvert`, and on any `Throwable` logs a
warning and rethrows wrapped in `RuntimeException`. Subclasses implement **`doConvert`**, which may throw
anything.

```java
public class StringToCurrencyConverter extends AbstractConverter<String, Currency> {

    @Override
    protected Currency doConvert(String source) {
        return Currency.getInstance(source.trim());     // Throwable is logged + wrapped for you
    }
}
```

Implementing `Converter` directly is fine for one-liners — it is a `@FunctionalInterface` — but then you own
null-handling and priority:

```java
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
> `S` and `T` must be **statically visible in the `implements` clause**, because the default
> `getSourceType()` / `getTargetType()` resolve them with `TypeUtils.resolveActualTypeArgumentClass`. A raw
> `implements Converter` or a lambda assigned to a `Converter` variable cannot be discovered by type pair —
> the resolved types would be `null`.

Extension points you may override:

| Method | Override when |
|---|---|
| `convert(S)` | you implement `Converter` directly (it is the functional method) |
| `doConvert(S)` | you extend `AbstractConverter` (recommended) |
| `accept(Class, Class)` | matching is broader than exact type pair (e.g. "any `CharSequence` source") |
| `getPriority()` | you must win or lose against a built-in |
| `getSourceType()` / `getTargetType()` | your class cannot express the types in its `implements` clause |

`StringConverter<T>` (`@FunctionalInterface`, `extends Converter<String, T>`) is a narrowing of the contract
for String-source converters.

### 3.2 Register via SPI

`src/main/resources/META-INF/services/io.microsphere.convert.Converter`:

```
com.example.convert.StringToCurrencyConverter
```

### 3.3 Use

```java
String s = Converter.convertIfPossible(UUID.randomUUID(), String.class);
// or, when you want the instance (e.g. to inspect priority)
Converter<UUID, String> c = Converter.getConverter(UUID.class, String.class);
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

Registered implementations (11): `StringToArrayConverter`, `StringToCollectionConverter`,
`StringToDequeConverter`, `StringToBlockingDequeConverter`, `StringToBlockingQueueConverter`,
`StringToQueueConverter`, `StringToTransferQueueConverter`, `StringToListConverter`, `StringToSetConverter`,
`StringToNavigableSetConverter`, `StringToSortedSetConverter`.

> [!NOTE]
> `StringToIterableConverter` and the base `StringToMultiValueConverter` exist on disk but are **not**
> registered, so requesting an `Iterable` target falls back to the registered list/set behaviour.

```java
String[] array = MultiValueConverter.convertIfPossible("a,b,c", String[].class, String.class);  // String[]
List<String> list = MultiValueConverter.convertIfPossible("a,b,c", List.class, String.class);   // [a, b, c]
Set<Integer> ids = MultiValueConverter.convertIfPossible("1,2,3", Set.class, Integer.class);    // [1, 2, 3]

MultiValueConverter<?> converter = MultiValueConverter.find(String.class, SortedSet.class);
```

Element values are converted by the scalar `Converter` SPI, so `Set<Integer>` above works because
`StringToIntegerConverter` is registered. `find` sorts candidates by `Prioritized` and returns the first
`accept`ing one — the same precedence rule as `Converter`.

> [!TIP]
> Unlike `Converters`, `MultiValueConverter.find` has **no result cache**: it loads and sorts the service
> list on every call (subject to `microsphere.service-loader.cached`, which defaults to `false`). In a hot
> loop, resolve the converter once via `find(...)` and reuse it.

This is the layer that turns a single configuration string into a typed collection — see
[Configuration Property Metadata](configuration-metadata.md).

---

## 5. `Serializer` / `Deserializer` — binary SPIs

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
| `META-INF/services/io.microsphere.io.serializer.Deserializer` | the eight numeric serializers (they implement both interfaces) plus `StringDeserializer`, `DefaultDeserializer` |

Semantics: `getHighestPriority(X)` = first match (smallest `getPriority()`); `getLowestPriority(X)` = last;
`getMostCompatible(X)` = highest-priority match, falling back to `getLowestPriority(Object.class)` — i.e. it
always returns something. `DefaultSerializer` / `DefaultDeserializer` (Java serialization) are the safety
net, not an error.

> [!WARNING]
> Two constraints worth knowing:
> 1. `EnumSerializer` exists in the source tree but is **not registered**, so enum serialization falls
>    through to `DefaultSerializer`. Register it yourself in your own services file if you need it.
> 2. The maps are per-instance (`Map<Class<?>, List<...>>` keyed by each implementation's first type
>    argument), and `loadSPI()` **appends** — calling it twice on the same instance duplicates entries.
>    Creating a new `Serializers` after adding services to the classpath is the way to pick them up; unlike
>    `Converters`, this cache is not JVM-global.

---

## 6. Choosing between the three

| Need | Use |
|---|---|
| One value in, one value out | `Converter<S, T>` |
| One delimited string in, a collection/array out | `MultiValueConverter<S>` |
| Value to/from bytes | `Serializer` / `Deserializer` |
| Whole-bean binding from JSON | `JSONUtils.readValue(...)` — see [JSON](json.md) |

---

## See also

* [Reflection and Types](reflection-and-types.md) — how `S`/`T` are discovered (`TypeUtils`)
* [Language Abstractions](language-abstractions.md) — `Prioritized` constants and the ordering trap
* [Core Utilities](core-utilities.md) — `ServiceLoaderUtils` and `microsphere.service-loader.cached`

[← Handbook index](../README.md) · [Previous: Reflection and Types](reflection-and-types.md) · [Next: Event Dispatching →](events.md)
