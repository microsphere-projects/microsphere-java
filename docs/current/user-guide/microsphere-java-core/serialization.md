# Serialization

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-java-core` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Package** | `io.microsphere.io.serializer` — 18 main-source types |
| **JDK level** | Compiled at Java 8; CI-tested on JDK 8 / 11 / 17 / 21 / 25 |
| **Depends on other `core` packages** | `io.microsphere.reflect` (`JavaType`, `MethodUtils.invokeStaticMethod`, `TypeUtils.resolveTypeArgumentClasses`), `io.microsphere.io` (`FastByteArrayInputStream`, `FastByteArrayOutputStream`, `IOUtils`), `io.microsphere.util` (`SizeUtils`, `ClassLoaderUtils`, `ServiceLoaderUtils`, `ArrayUtils`), `io.microsphere.collection` (`ListUtils`, `MapUtils`), `io.microsphere.annotation.Nonnull` |
| **External dependencies** | none — pure JDK |
| **Used by** | `convert` — `ObjectToByteArrayConverter` and `ByteArrayToObjectConverter` call the `DefaultSerializer` / `DefaultDeserializer` singletons. The dependency runs `convert` → `io.serializer`, never the reverse. |

All `file:line` references in this page are relative to `microsphere-java-core/src/main/java/io/microsphere/`
(main sources) or `microsphere-java-core/src/test/java/io/microsphere/` (tests), read from the `release` worktree.
After the first mention they are shortened to `io/serializer/Foo.java:40-55`.

---

## 1. What this package is for

**Reach for `io.microsphere.io.serializer` when you need to turn one of a small, fixed set of value types into a
`byte[]` and back with a layout you can predict.** The package ships a `Serializer` / `Deserializer` SPI and nine
ready-made implementations for `boolean`, `byte`, `char`, `short`, `int`, `long`, `float`, `double` and `String`.
Each numeric one is a pure bit-layout codec with no allocation beyond the result array — for example
`IntegerSerializer` writes four big-endian bytes by hand (`io/serializer/IntegerSerializer.java:21-30`) and
`BooleanSerializer` writes one byte `1`/`0` (`io/serializer/BooleanSerializer.java:26-30`). If your goal is a stable
compact binary encoding of a scalar, these are the right tool.

**Reach for `Serializers` / `Deserializers` when the concrete type is only known at runtime** — a cache key encoder,
a generic record store, a wire protocol that tags fields by Java type. They are per-`ClassLoader` registries that
load every `Serializer` registered through `META-INF/services` and let you ask for the best match by type
(`io/serializer/Serializers.java:87-129`).

**Reach for the `AbstractSerializer<T>` base class when you are adding a codec.** It already handles `null`, already
validates the fixed byte width, and already discovers the type you encode, so a new codec is three small methods
(`io/serializer/AbstractSerializer.java:70-137`).

**Do not reach for it when:**

* **You need a real object graph.** The only general-purpose implementation, `DefaultSerializer`, is a thin wrapper
  over `java.io.ObjectOutputStream` (`io/serializer/DefaultSerializer.java:52-62`). It is JDK serialization — with
  the JDK's stream format, the JDK's `serialVersionUID` rules, and the JDK's deserialization attack surface. Use a
  schema-based format (Protobuf, Avro, JSON) instead.
* **You need untrusted input handling.** `DefaultDeserializer` calls `ObjectInputStream.readObject()` with no class
  filter of any kind. See §6 — this is the single most important caveat on the page.
* **You need a general text encoding.** `StringSerializer`/`StringDeserializer` are convenience codecs, not a
  charset framework; they are not registered in a way that lets you round-trip a `null`.
* **You need `Enum` encoding via the registry.** `EnumSerializer` exists and works, but it is not in the SPI files,
  so `Serializers` will never hand it to you. See §5.

---

## 2. Coordinates and version support

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

| Fact | Value | Evidence (`release`) |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | `pom.xml:12` |
| `artifactId` | `microsphere-java-core` | module `pom.xml` |
| Version property | `${revision}`, flattened | `pom.xml:14` |
| Value of `${revision}` on `release` | `0.3.20-SNAPSHOT` | `pom.xml:54` |
| Newest published tag | `0.3.20` | release-line table in the group index (`docs/current/user-guide/microsphere-java-core/README.md:70`) |
| Build parent | `microsphere-build:0.3.16` | `pom.xml:6-10` |
| Java level | 8, inherited from the external build parent; no `maven.compiler.*` / `<release>` property in the reactor | CI matrix `java: [ '8', '11', '17', '21', '25' ]`, `.github/workflows/maven-build.yml:25`, `distribution: 'temurin'` `:33`, build command `:38-45` |
| Spring Framework, JDK 17+ | `7.0.9` | `microsphere-java-parent/pom.xml:23` |
| Spring Framework, JDK `[1.8,17)` | `5.3.39` via profile `java8-16` | `microsphere-java-parent/pom.xml:54-62` |
| Imported BOMs | see `microsphere-java-parent/pom.xml:22`, `:33-36`, `:40-46` | same |
| `@Since` of every type in this package | `1.0.0` | `io/serializer/Serializer.java:38`, `AbstractSerializer.java:52`, `EnumSerializer.java:15`, `DefaultDeserializer.java:49` |

> [!TIP]
> Nothing in this package touches Spring, so the `java8-16` profile is irrelevant here. What *is* JDK-sensitive is the
> `DefaultSerializer` / `DefaultDeserializer` pair: their output is the JDK serialization stream, whose behaviour and
> default filter configuration changed across JDK 8 → 25. See §5.

---

## 3. API reference

### 3.1 The two SPI interfaces

`Serializer<S>` — one method, not a functional interface (`io/serializer/Serializer.java:40-42`):

```java
public interface Serializer<S> {
    byte[] serialize(S source) throws IOException;
}
```

`Deserializer<T>` — a `@FunctionalInterface`, so a lambda is a legal implementation (`io/serializer/Deserializer.java:40-43`):

```java
@FunctionalInterface
public interface Deserializer<T> {
    T deserialize(byte[] bytes) throws IOException;
}
```

Both contracts require implementations to be **thread-safe** (`io/serializer/Serializer.java:24-25`,
`io/serializer/Deserializer.java:24`). Every built-in implementation is stateless-after-construction, so the
public static singletons are safe to share.

### 3.2 Entry points

| Entry point | Signature | Notes |
|---|---|---|
| `new Serializers()` / `new Serializers(ClassLoader)` | `Serializers()` (`io/serializer/Serializers.java:68-70`), `Serializers(ClassLoader)` (`:64-66`) | the no-arg form uses `ClassLoaderUtils.getDefaultClassLoader()` |
| `Serializers#loadSPI` | `void loadSPI()` (`:72-79`) | must be called before any lookup; not `@Nonnull`-guarded, idempotent only by accident — see §6 |
| `Serializers#getMostCompatible` | `@Nullable Serializer<?> getMostCompatible(Class<?> serializedType)` (`:87-93`) | falls back to the `Object.class` entry |
| `Serializers#getHighestPriority` | `@Nullable <S> Serializer<S> getHighestPriority(Class<S> serializedType)` (`:102-105`) | `first(list)`; `null` on empty |
| `Serializers#getLowestPriority` | `@Nullable <S> Serializer<S> getLowestPriority(Class<S> serializedType)` (`:114-117`) | `last(list)`; `null` on empty |
| `Serializers#get` | `@Nonnull <S> List<Serializer<S>> get(Class<S> serializedType)` (`:126-129`) | returns `emptyList()`, never `null` |
| `Deserializers` | same five shapes (`io/serializer/Deserializers.java:64-129`) | see §5 for the caching asymmetry |
| `AbstractSerializer` singletons | `BOOLEAN_SERIALIZER`, `BYTE_SERIALIZER`, `CHARACTER_SERIALIZER`, `SHORT_SERIALIZER`, `INTEGER_SERIALIZER`, `LONG_SERIALIZER`, `FLOAT_SERIALIZER`, `DOUBLE_SERIALIZER` | e.g. `io/serializer/IntegerSerializer.java:13` |
| `DefaultSerializer.DEFAULT_SERIALIZER` | `public static final DefaultSerializer` (`io/serializer/DefaultSerializer.java:49`) | JDK-stream serializer for `Object` |
| `DefaultDeserializer.DEFAULT_DESERIALIZER` | `public static final DefaultDeserializer` (`io/serializer/DefaultDeserializer.java:53`) | JDK-stream deserializer for `Object` |
| `new EnumSerializer<>(Class<E>)` | `io/serializer/EnumSerializer.java:27-31` | per-enum instance; **no SPI registration** |

### 3.3 Supported types and byte layouts

| Type | Implementing class | Width | `calcBytesLength()` evidence | Byte order |
|---|---|---|---|---|
| `Boolean` | `BooleanSerializer` | 1 | `io/serializer/BooleanSerializer.java:21-23` (`BOOLEAN_BYTES_SIZE`) | n/a |
| `Byte` | `ByteSerializer` | 1 | `io/serializer/ByteSerializer.java:36-38` | n/a |
| `Character` | `CharacterSerializer` | 2 | `io/serializer/CharacterSerializer.java:34-36` | big-endian (`:39-45`) |
| `Short` | `ShortSerializer` | 2 | `io/serializer/ShortSerializer.java:16-18` | big-endian (`:21-27`) |
| `Integer` | `IntegerSerializer` | 4 | `io/serializer/IntegerSerializer.java:16-18` | big-endian (`:21-30`) |
| `Float` | `FloatSerializer` | 4 | `io/serializer/FloatSerializer.java:36-38` | big-endian, via `INTEGER_SERIALIZER` (`:41-44`) |
| `Long` | `LongSerializer` | 8 | `io/serializer/LongSerializer.java:16-18` | **little-endian** (`:21-34`) |
| `Double` | `DoubleSerializer` | 8 | `io/serializer/DoubleSerializer.java:21-23` | **little-endian**, via `LONG_SERIALIZER` (`:26-30`) |
| `String` | `StringSerializer` / `StringDeserializer` | variable | charset-encoded; `StringSerializer.java:57-59`, `StringDeserializer.java:69-71` | n/a |
| `Enum<E>` | `EnumSerializer` | 1 or 2 | `io/serializer/EnumSerializer.java:33-36` — `1` while the constant count is below `Byte.MAX_VALUE` (128), else `2` | big-endian for the 2-byte case (`:56`, `:69`) |
| `Object` (JDK stream) | `DefaultSerializer` / `DefaultDeserializer` | variable | `io/serializer/DefaultSerializer.java:52-62` | JDK stream format |

Widths come from `SizeUtils`: `UNBOUND_BYTES_SIZE = -1`, `BOOLEAN/BYTE = 1`, `SHORT/CHAR = 2`,
`INTEGER/FLOAT = 4`, `LONG/DOUBLE = 8` (`io/util/SizeUtils.java:38-78`).

> [!WARNING]
> The 2/4/8-byte families do **not** share a byte order. `Integer`/`Float` are big-endian, `Long`/`Double` are
> little-endian. This is visible directly in the source: `IntegerSerializer.java:24` puts the most-significant byte
> at index `0`, while `LongSerializer.java:24` puts the *least*-significant byte there. Any cross-type binary format
> you build on these must handle the switch explicitly. See §6.

### 3.4 Stream and `Closeable` ownership

You never see a stream. Both `DefaultSerializer` and `DefaultDeserializer` open their `FastByteArray*` and
`Object*Stream` objects inside a try-with-resources block scoped to the single method and close them before
returning (`io/serializer/DefaultSerializer.java:54-60`, `io/serializer/DefaultDeserializer.java:61-68`).

* The buffers are `io.microsphere.io.FastByteArrayOutputStream` / `FastByteArrayInputStream`, and **both have no-op
  `close()`** (`io/io/FastByteArrayOutputStream.java:188-189`, `io/io/FastByteArrayInputStream.java:157-158`). The
  `close()` call can never mask or suppress your real exception.
* `FastByteArrayOutputStream.toByteArray()` returns `Arrays.copyOf(buf, count)` — a fresh array
  (`io/io/FastByteArrayOutputStream.java:159-161`). The `byte[]` you get back is therefore **yours**: mutating it,
  or holding it after the serializer is GC'd, cannot corrupt anything.
* `FastByteArrayInputStream` is constructed over the caller's array **without copying it**
  (`io/io/FastByteArrayInputStream.java:81-83`, javadoc at `:73` "The buffer array is not copied"). Since the stream
  dies inside `deserialize`, this only means: do not mutate the input array while a `deserialize` call is in flight
  on another thread.
* `EnumSerializer` and the `AbstractSerializer` subclasses allocate a new array per call and retain nothing.

### 3.5 Null and invalid-input contract

This is where the package is deliberately inconsistent, and it matters more than the layouts:

| Call | Result | Evidence |
|---|---|---|
| `AbstractSerializer.serialize(null)` | `null` | `io/serializer/AbstractSerializer.java:71-77` |
| `AbstractSerializer.deserialize(null)` | `null` | `io/serializer/AbstractSerializer.java:80-84` |
| `AbstractSerializer.deserialize(wrongWidth)` | `null` — silently, no exception | `io/serializer/AbstractSerializer.java:87-89` |
| `EnumSerializer.serialize(null)` / `deserialize(null)` | `null` | `io/serializer/EnumSerializer.java:44-47`, `:64-67` |
| `DefaultDeserializer.deserialize(null or empty)` | `null` | `io/serializer/DefaultDeserializer.java:57-59` |
| `StringSerializer.serialize(null)` | **`NullPointerException`** | `io/serializer/StringSerializer.java:57-59` — `source.getBytes(charset)` with no guard |
| `StringDeserializer.deserialize(null)` | **`NullPointerException`** | `io/serializer/StringDeserializer.java:69-71` — `new String(null, charset)` |
| `StringDeserializer.deserialize(new byte[0])` | `""` | asserted in `StringSerializerAndDeserializerTest.java:60-64` |

The `wrongWidth → null` rule is a *degradation to a neutral value*, in line with the library-wide policy: a
mis-sized array never throws, it just gives you `null`. Which means the caller must check.

---

## 4. Examples

Every snippet below is condensed from an actual test body in
`microsphere-java-core/src/test/java/io/microsphere/io/serializer/`.

### 4.1 Registry lookup, then a round trip

`SerializersAndDeserializersTest.java:40-46` — the two registries are constructed and loaded separately:

```java
serializers = new Serializers();
deserializers = new Deserializers();
serializers.loadSPI();
deserializers.loadSPI();
```

`SerializersAndDeserializersTest.java:49-56` — `String` resolves to the charset codecs, not the JDK stream:

```java
Serializer<String> serializer = serializers.getHighestPriority(String.class);
String value = "Test";
byte[] bytes = serializer.serialize(value);
assertArrayEquals(value.getBytes(StandardCharsets.UTF_8), bytes);   // 4 bytes, not a JDK header
Deserializer<String> deserializer = deserializers.getHighestPriority(String.class);
assertEquals(value, deserializer.deserialize(bytes));
```

`SerializersAndDeserializersTest.java:59-65` — `getMostCompatible` returns a raw-type serializer, so the call site
is untyped and the cast is deferred to you:

```java
Serializer serializer = serializers.getMostCompatible(Integer.class);
Integer value = 1;
byte[] bytes = serializer.serialize(value);
Deserializer deserializer = deserializers.getMostCompatible(Integer.class);
assertEquals(value, deserializer.deserialize(bytes));
```

### 4.2 The registry is empty until you load it

`SerializersTest.java:56-67` and `DeserializersTest.java:44-53` both show the two-phase contract. Before
`loadSPI()`, `get(...)` is an empty list and every single-value lookup is `null`; afterwards all are non-null:

```java
// SerializersTest.java:58-66
for (Class<?> supportedClass : SUPPORTED_CLASSES) {
    assertEquals(emptyList(), this.serializers.get(supportedClass));  // before loadSPI()
}
this.serializers.loadSPI();
for (Class<?> supportedClass : SUPPORTED_CLASSES) {
    assertNotNull(this.serializers.get(supportedClass));              // after loadSPI()
}
```

The set `SerializersTest.java:37-47` treats as supported is the registry's whole contract surface —
`Boolean`, `Character`, `Short`, `Integer`, `Long`, `Float`, `Double`, `String`, and `Object`:

```java
private static final Class<?>[] SUPPORTED_CLASSES = {
        Boolean.class, Character.class, Short.class, Integer.class, Long.class,
        Float.class, Double.class, String.class, Object.class
};
```

`Byte` and `Enum` are absent — `Byte` is in the SPI file but untested here, `Enum` is not in the file at all.

### 4.3 Using a scalar codec directly

`IntegerSerializerTest.java:31-41` — subclasses of the shared test base just name a singleton and a value; the base
class `AbstractSerializerTest.java:33-51` does the round trip, asserts `calcBytesLength() == getBytesLength()`, and
asserts `getTargetType()` resolved to the expected class:

```java
class IntegerSerializerTest extends AbstractSerializerTest<Integer> {

    @Override                                              // IntegerSerializerTest.java:33-36
    protected AbstractSerializer<Integer> getSerializer() {
        return INTEGER_SERIALIZER;
    }

    @Override                                              // IntegerSerializerTest.java:38-41
    protected Integer getValue() {
        return MAX_VALUE;                                  // static import of Integer.MAX_VALUE
    }
}
```

`AbstractSerializerTest.java:28-31,59-63` — the same base class runs a **`null` value through every codec**, which
is how the `null → null` contract above is enforced:

```java
@Test
void testNull() throws IOException {
    test(this::getNullValue);      // getNullValue() returns null by default (AbstractSerializerTest.java:61-63)
}
```

### 4.4 Charset-parameterised `String` codecs

`StringSerializerAndDeserializerTest.java:48-57` — pass a `Charset` to both halves; nothing else changes:

```java
Charset charset = StandardCharsets.ISO_8859_1;
StringSerializer customSerializer = new StringSerializer(charset);
StringDeserializer customDeserializer = new StringDeserializer(charset);
String value = "Test";
byte[] bytes = customSerializer.serialize(value);
assertArrayEquals(value.getBytes(charset), bytes);
assertEquals(value, customDeserializer.deserialize(bytes));
```

`StringSerializerAndDeserializerTest.java:66-73` — UTF-16 round-trips too, but only because the *same* `Charset`
object was used on both sides:

```java
Charset charset = StandardCharsets.UTF_16;
assertEquals(value, new StringDeserializer(charset).deserialize(new StringSerializer(charset).serialize(value)));
```

### 4.5 JDK serialization for a general object

`DefaultSerializerAndDeserializerTest.java:37-46` — note the source object is typed `Object` and holds a `String`
first, then an `Integer`. Both survive, because the JDK stream carries the class descriptor:

```java
Object value = "Test";
byte[] bytes = serializer.serialize(value);
assertEquals(value, deserializer.deserialize(bytes));

value = 1;
bytes = serializer.serialize(value);
assertEquals(value, deserializer.deserialize(bytes));
```

`DefaultDeserializerTest.java:18-23` — the empty/null-input degradation, asserted directly:

```java
private DefaultDeserializer deserializer = DefaultDeserializer.DEFAULT_DESERIALIZER;

@Test
void testDeserializeOnNull() throws IOException {
    assertNull(deserializer.deserialize(null));
}
```

### 4.6 `EnumSerializer` widths

`EnumSerializerTest.java:41-56` — `TimeUnit` has 8 constants, well under 128, so one byte carries the `ordinal()`,
and both `null` directions return `null`:

```java
EnumSerializer<TimeUnit> serializer = new EnumSerializer<>(TimeUnit.class);
assertEquals(1, serializer.getBytesLength());
byte[] bytes = serializer.serialize(timeUnit);
assertEquals(timeUnit, serializer.deserialize(bytes));
assertNull(serializer.serialize(null));
assertNull(serializer.deserialize(null));
```

`EnumSerializerTest.java:59-69,72-93` — the nested `E` enum declares exactly 200 constants, pushing the width to 2:

```java
EnumSerializer<E> serializer = new EnumSerializer<>(E.class);
assertEquals(2, serializer.getBytesLength());   // 200 constants > Byte.MAX_VALUE (127)
```

### 4.7 Writing your own codec

Condensed from the javadoc example at `io/serializer/AbstractSerializer.java:22-48`, which is the shape the nine
built-in codecs follow:

```java
public class IntegerSerializer extends AbstractSerializer<Integer> {

    public static final IntegerSerializer INTEGER_SERIALIZER = new IntegerSerializer();

    @Override
    protected int calcBytesLength() {
        return INTEGER_BYTES_SIZE;   // 4 — enables the width guard in AbstractSerializer:87-89
    }

    @Override
    protected byte[] doSerialize(Integer value) {
        ByteBuffer buffer = ByteBuffer.allocate(INTEGER_BYTES_SIZE);
        buffer.putInt(value);
        return buffer.array();
    }

    @Override
    protected Integer doDeserialize(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }
}
```

Variable-length codecs return `UNBOUND_BYTES_SIZE` (`-1`) from `calcBytesLength()`, which disables the width guard.

---

## 5. Compatibility notes

### 5.1 JDK serialization behaviour across JDKs

`DefaultSerializer` and `DefaultDeserializer` add no format of their own — they call
`new ObjectOutputStream(...).writeObject(source)` (`io/serializer/DefaultSerializer.java:54-59`) and
`new ObjectInputStream(...).readObject()` (`io/serializer/DefaultDeserializer.java:61-65`). Consequences:

* Byte output is the JDK stream format (`0xAC 0xED` magic + version + class descriptors). It is stable across the
  JDKs in the CI matrix only because it is the JDK's own stable format; it is **not** stable against a
  `serialVersionUID` change or a class-path difference in the peer JVM.
* `serialize` requires the argument be `Serializable`. The code does not check: it delegates to
  `ObjectOutputStream`, so a non-serializable argument surfaces as `java.io.NotSerializableException`, an
  `IOException` (`io/serializer/DefaultSerializer.java:52` declares `throws IOException` and adds no catch).
* `DefaultDeserializer` behaves differently: its `catch (Exception e) { throw new IOException(e); }`
  (`io/serializer/DefaultDeserializer.java:66-68`) collapses **every** exception — `ClassNotFoundException`,
  `InvalidClassException`, even a `RuntimeException` thrown from a target's `readObject` — into a single
  `IOException`. If you need to distinguish them, catch `IOException` and inspect `getCause()`.
* JDK-deserialization hardening that arrived after Java 8 (`ObjectInputFilter`, the `jdk.io.objects.*` globals) is a
  **JDK** feature, not a library feature. On JDK 17+ you can configure it globally; the library neither enables nor
  reads such configuration. §6 covers this.

### 5.2 `serialVersionUID` handling

There is **none** in this package. A grep across all 18 sources for `serialVersionUID` returns nothing. The
serializable marker interfaces that `DefaultSerializer`/`DefaultDeserializer` reference are imported only for javadoc
`@see` links (`io/serializer/DefaultSerializer.java:23`, `io/serializer/DefaultDeserializer.java:23`). Practical
effect: if you serialize a class with `DefaultSerializer` and later change that class's shape without declaring a
`serialVersionUID`, the JDK recomputes it and `DefaultDeserializer` throws `InvalidClassException` — surfaced as an
`IOException` per §5.1. Pinning a `serialVersionUID` is entirely your responsibility.

### 5.3 The two registries are not symmetric

This is easy to trip over:

* **Serializer SPI file** registers `StringSerializer` at line 9; the **Deserializer SPI file** registers
  `StringDeserializer` at line 9. Asymmetric class names, symmetric behaviour — a `StringSerializer` is registered
  as a `Serializer` only, and vice versa, because each implements only one interface
  (`io/serializer/StringSerializer.java:44`, `io/serializer/StringDeserializer.java:56`).
* `Serializers.loadSPI()` requests a **cached** service load (`io/serializer/Serializers.java:73`, third argument
  `true`); `Deserializers.loadSPI()` does not (`io/serializer/Deserializers.java:73`, two-argument overload). The
  caching default is `SERVICE_LOADER_CACHED`, parsed from the system property `microsphere.service-loader.cached`
  and **`false` by default** (`io/util/ServiceLoaderUtils.java:107`, `:112`, `:138`, `:410-412`). Calling
  `Deserializers.loadSPI()` twice therefore re-reads and re-instantiates the providers, and — because
  `loadSPI` *appends* to the per-type list (`io/serializer/Deserializers.java:76-77`) — the second call **doubles**
  every registered list. Calling `Serializers.loadSPI()` twice is safe only as long as the cache is enabled.
* Sorting is applied identically in both cases, so this is purely a caching/repetition difference.
  `ServiceLoaderUtils.loadServicesAsList` always calls `sort(serviceList, Prioritized.COMPARATOR)`
  (`io/util/ServiceLoaderUtils.java:890`). None of the 18 types implements `Prioritized`, so the comparator returns
  `0` for every pair (`io/lang/Prioritized.java:71-75`) and `Collections.sort` being stable, the SPI file's
  declaration order is preserved. **First match is therefore file order, not a computed priority.**

### 5.4 The `Object.class` fallback

`getMostCompatible` has a two-step rule (`io/serializer/Serializers.java:87-93`):

```java
Serializer<?> serializer = getHighestPriority(serializedType);
if (serializer == null) {
    serializer = getLowestPriority(Object.class);
}
```

Because `DefaultSerializer implements Serializer<Object>` (`io/serializer/DefaultSerializer.java:47`) and the registry
keys each provider by its first resolved type argument (`io/serializer/Serializers.java:74-75`), `Object.class` is
populated with `DefaultSerializer` — the last entry in the SPI file. **So `getMostCompatible` never returns `null`
after a successful `loadSPI()`, for *any* type.** Ask for `getMostCompatible(SomeDomainObject.class)` and you get
`DefaultSerializer` silently. `getLowestPriority(Object.class)` returns the *last* `Object`-keyed entry, which the
file order makes `DefaultSerializer` / `DefaultDeserializer`.

`getMostCompatible` is a convenience you probably do not want. Use `getHighestPriority` and handle `null` when you
need "only the exact codec, or nothing".

---

## 6. Pitfalls and known limits

> [!SECURITY]
> **`DefaultDeserializer` performs unfiltered JDK deserialization. Do not point it at data you do not trust.**
>
> Verified from the source, on the `release` branch:
>
> * `DefaultDeserializer.deserialize` builds a plain `java.io.ObjectInputStream` over the caller's bytes and calls
>   `readObject()` — `io/serializer/DefaultDeserializer.java:61-65`. There is no `ObjectInputFilter`, no subclassed
>   `resolveClass`, no allow-list, no size cap, and no `LatestClock`/depth configuration. A grep across all 18 types
>   in `io/serializer/` for `ObjectInputFilter`, `resolveClass` and `BadPaddingException` returns **no matches**.
> * Consequently it does **not** guard against: gadget-chain instantiation, arbitrary class loading during
>   deserialization, `readObject`/`readResolve` side effects in target classes, deserialization bombs (deep or wide
>   object graphs exhausting heap), or `serialVersionUID`-based type confusion. It only guards against *empty input*
>   (`:57-59`).
> * The risk is reachable through public API beyond this package: `ByteArrayToObjectConverter` calls
>   `DEFAULT_DESERIALIZER.deserialize(...)` (`io/convert/ByteArrayToObjectConverter.java:22,41`) and that converter is
>   registered in `META-INF/services/io.microsphere.convert.Converter` (line 3), so any `Converters`-driven
>   `byte[] → Object` conversion routes untrusted bytes here.
>
> **Mitigation is yours to add, using the JDK's own mechanism.** As of Java 9 (backported to 8u121+) the platform
> provides `java.io.ObjectInputFilter`, settable per-stream via `ObjectInputStream.setObjectFilter(...)` or globally
> via the `jdk.io.objects.readData` / `jdk.serialFilter` system properties. This library's `ObjectInputStream` is
> created inside `DefaultDeserializer` and is not exposed, so **per-stream filtering is not possible through
> `DefaultDeserializer`** — the only configuration it will honour is the global `jdk.serialFilter` property. For
> anything genuinely untrusted, either set that global filter or replace the `Object`-keyed SPI entry with your own
> filtered `Deserializer`. The package gives you no other lever, and this page does not invent one.

Other real limits:

1. **`Long`/`Double` are little-endian, everything else is big-endian.** `IntegerSerializer.java:23-28` emits
   MSB-first; `LongSerializer.java:23-32` emits LSB-first. `FloatSerializer` inherits big-endian by delegating to
   `INTEGER_SERIALIZER` (`io/serializer/FloatSerializer.java:41-44`), and `DoubleSerializer` inherits little-endian by
   delegating to `LONG_SERIALIZER` (`io/serializer/DoubleSerializer.java:26-30`). So a 4-byte field and an 8-byte
   field in the same record use opposite orders. `CharacterSerializer.java:41-44` and `ShortSerializer.java:23-25`
   match the big-endian family.
2. **A wrong-width `deserialize` returns `null`, not an error.** `io/serializer/AbstractSerializer.java:87-89`. A
   truncated buffer is indistinguishable from a legitimately `null` value. If that ambiguity matters, check
   `getBytesLength()` (`:109-111`) against your buffer yourself.
3. **`StringSerializer` / `StringDeserializer` break the `null` contract.** Both NPE on `null` input
   (`StringSerializer.java:57-59`, `StringDeserializer.java:69-71`) while every other codec returns `null`. Since
   `String` *is* in the SPI files (`Serializer` file line 9, `Deserializer` file line 9), a
   `serializers.getHighestPriority(String.class).serialize(null)` call throws where the analogous `Integer` call
   does not.
4. **`Charset` is not encoded in the bytes.** `StringSerializer` writes exactly `source.getBytes(charset)` with no
   BOM, length prefix, or charset marker (`io/serializer/StringSerializer.java:57-59`). The `String` and `UTF_16`
   tests pass only because the same `Charset` object was supplied on both sides
   (`StringSerializerAndDeserializerTest.java:48-57,66-73`). Mixing charsets across a wire is silent corruption.
5. **`EnumSerializer` is not discoverable, and encodes `ordinal()` not `name()`.** It is absent from both SPI files,
   so you must construct it per enum type (`io/serializer/EnumSerializer.java:27-31`). It writes
   `e.ordinal()` (`:49-59`) and reads back via `enums[ordinal]` (`:69-70`), which means: reordering or removing enum
   constants silently reassigns the meaning of stored bytes, and an out-of-range ordinal throws
   `ArrayIndexOutOfBoundsException` rather than an `IOException`. `EnumSerializer` is also the only codec that does
   *not* extend `AbstractSerializer` (`:17`), so its width guard is hand-rolled.
6. **`Serializers` / `Deserializers` are not thread-safe.** The registry maps are plain `HashMap`
   (`io/serializer/Serializers.java:60` via `MapUtils.newHashMap()`, which returns `new HashMap<>()` at
   `io/collection/MapUtils.java:478-480`) and `loadSPI()` mutates them with a non-atomic
   `computeIfAbsent` + `add` (`io/serializer/Serializers.java:76-77`). Build the registry during startup on one
   thread; the read methods (`get`, `getHighestPriority`, `getMostCompatible`) are then safe to share because they
   only read.
7. **`loadSPI()` throws if the SPI file has no providers.** `ServiceLoaderUtils.loadServicesAsList` raises
   `IllegalArgumentException` when the iterator is empty (`io/util/ServiceLoaderUtils.java:882-886`). This is one of
   the package's few genuine throws, and it fires from `new Serializers().loadSPI()` if you strip the service files
   (e.g. through a shade plugin that drops `META-INF/services`). Note it is *not* thrown for an unknown type — that
   just gives `null`/`emptyList()`.
8. **`getMostCompatible` returns a raw `Serializer<?>`.** (`io/serializer/Serializers.java:87`) — you lose the type
   parameter and must cast at the call site, exactly as `SerializersAndDeserializersTest.java:60-64` does.
9. **The unchecked-exception sites are in `convert`, not here.** Every failure path inside `io.serializer` is a
   declared `IOException` or a neutral value. The one place a serialization failure becomes unchecked is
   `ByteArrayToObjectConverter.doConvert`, which catches `IOException` and throws `new RuntimeException(e)`
   (`io/convert/ByteArrayToObjectConverter.java:42-44`) — and casts the result to `Serializable` (`:41`), adding a
   `ClassCastException` path. Catch `RuntimeException` if you go through `Converters`.

---

## 7. See also

* [I/O, NIO, scanning and file filters](io-and-scanning.md) — `FastByteArrayInputStream` / `FastByteArrayOutputStream`,
  the buffers both `Default*` classes wrap, and the resource-close discipline of the `io` package.
* [Type conversion](type-conversion.md) — `ObjectToByteArrayConverter` and `ByteArrayToObjectConverter`, the two
  converters that expose this package's JDK-serialization path through the `Converter` SPI.
* [Reflection, types and beans](reflection-and-beans.md) — `JavaType`/`TypeUtils.resolveTypeArgumentClasses`, which
  decide the registry key for every provider, and `MethodUtils.invokeStaticMethod`, which `EnumSerializer` uses to
  call `values()`.
* [Class loading and artifacts](classloading-and-artifacts.md) — `ClassLoaderUtils.getDefaultClassLoader()` and
  `ServiceLoaderUtils`, which back the per-`ClassLoader` registry and its caching switch.
* [Developer Guide — Serialization](../../developer-guide/microsphere-java-core/serialization.md) — the selection
  algorithm, the registry loop, design patterns, thread-safety analysis and extension points.
