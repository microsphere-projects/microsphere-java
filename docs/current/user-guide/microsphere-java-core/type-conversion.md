# Type conversion

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-java-core` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Packages** | `io.microsphere.convert` — 38 main-source types · `io.microsphere.convert.multiple` — 14 main-source types (52 total) |
| **JDK level** | Compiled at Java 8; CI-tested on JDK 8 / 11 / 17 / 21 / 25 |
| **Depends on other `core` packages** | `io.microsphere.reflect` (`TypeUtils.resolveActualTypeArgumentClass`), `io.microsphere.lang.Prioritized`, `io.microsphere.util` (`ClassUtils`, `ClassLoaderUtils`, `ServiceLoaderUtils`, `ExceptionUtils`, `StringUtils`, `ArrayUtils`, `CharSequenceUtils`), `io.microsphere.collection` (`ListUtils`, `MapUtils`), `io.microsphere.constants.SymbolConstants`, `io.microsphere.logging`, `io.microsphere.annotation` (`@Nonnull` / `@Nullable`), `io.microsphere.io.serializer` (byte[] ⇄ Object), `io.microsphere.io` + `io.microsphere.nio.charset` (String→`InputStream`) |
| **Used by** | `metadata` (configuration-property coercion) and any application that must turn configuration strings into typed values |

All `file:line` references in this page are relative to `microsphere-java-core/src/main/java/io/microsphere/`
(main sources) or `microsphere-java-core/src/test/java/io/microsphere/` (tests), read from the `release` worktree.
After the first mention they are shortened to `convert/AbstractConverter.java:40-55` / `convert/multiple/StringToArrayConverter.java:40-55`.

---

## 1. What this package group is for

**Reach for `io.microsphere.convert` when you hold a value whose runtime type is not the type you need — most often a
`String` that came from configuration, a request parameter, or an environment variable, and you want the `Integer`,
`Boolean`, `Duration`, `char`, `Class`, or `InputStream` behind it.** The package ships a small `Converter<S, T>` SPI
(`convert/Converter.java:71-110`) with 34 concrete, self-registering implementations, a registry that caches the
selected strategy per `(sourceType, targetType)` pair (`convert/Converters.java:46-69`), and a first-match-after-sort
lookup driven by `io.microsphere.lang.Prioritized`. Two entry points cover almost every call site:
`Converter.getConverter(sourceType, targetType)` (`convert/Converter.java:135-137`) and the null-tolerant
`Converter.convertIfPossible(source, targetType)` (`convert/Converter.java:159-168`).

**Reach for `io.microsphere.convert.multiple` when the *result* is a container** — an array or a `Collection` /
`List` / `Set` / `SortedSet` / `NavigableSet` / `Queue` / `Deque` / `BlockingQueue` / `BlockingDeque` / `TransferQueue` —
whose *elements* still come from a single delimited `String`. `MultiValueConverter<S>`
(`convert/multiple/MultiValueConverter.java:36-91`) splits the source on commas and then recurses into the single-value
`Converter` SPI for each element (`convert/multiple/StringToIterableConverter.java:65-67`), so `StringToSetConverter`
turns `"1,2,3"` into a `HashSet<Integer>` by delegating every element to `StringToIntegerConverter`.

The design principle that ties the two packages together is **total, non-throwing degradation**: absent input and absent
converters produce `null`, never an exception (`convert/AbstractConverter.java:102-104`,
`convert/Converters.java:68`, `convert/multiple/MultiValueConverter.java:89`). Exceptions only surface when a converter
*is* found but the source value is genuinely unparseable — see the behaviour table in §3.3.

> [!NOTE]
> There is **no `String`→`Date` converter in this subsystem**. The test suite proves it:
> `convertIfPossible("1", Date.class)` asserts `null` (`convert/ConverterTest.java:50`). The only temporal converter is
> `StringToDurationConverter` (`convert/StringToDurationConverter.java:37-38`), which parses ISO-8601 durations via
> `java.time.Duration.parse`.

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
| `groupId` | `io.github.microsphere-projects` | root `pom.xml:12` |
| `artifactId` | `microsphere-java-core` | module `pom.xml` |
| Version property | `${revision}` | root `pom.xml:14` |
| Value of `${revision}` on `release` | `0.3.20-SNAPSHOT` | root `pom.xml:54` |
| Build parent | `microsphere-build:0.3.16` | root `pom.xml:6-10` |
| Java level | 8 (no `maven.compiler.*` / `<release>` property anywhere → level inherited from the external parent) | CI matrix `java: [ '8', '11', '17', '21', '25' ]`, `.github/workflows/maven-build.yml:25`, `distribution: 'temurin'` `:33`, build command `:38-45` |
| Spring Framework (irrelevant to this artifact at runtime, listed for reactor parity) | `7.0.9` default | `microsphere-java-parent/pom.xml:23` |
| Spring Framework under profile `java8-16` (`<jdk>[1.8,17)</jdk>`) | `5.3.39` | `microsphere-java-parent/pom.xml:54-62`, activation `:57` |
| Imported BOMs | `microsphere-all-bom:0.3.13` + `spring-framework-bom` | `microsphere-java-parent/pom.xml:22`, `:33-36`, `:40-46` |

> [!TIP]
> `microsphere-java-core` carries **no Spring dependency**, so nothing on this page behaves differently between the
> Spring 5 and Spring 7 profiles. The `java8-16` profile is a build-time property switch that matters only when
> Spring-aware modules join the same reactor.


### 2.1 Release date and what changed in `0.3.20`

| Item | Value | Evidence |
|---|---|---|
| Documented line | `release`, `${revision}` = `0.3.20-SNAPSHOT` | root `pom.xml:54` |
| Newest published release | `0.3.20`, tagged **2026-10-16** | the `0.3.20` tag resolves to `chore: merge main into release [skip ci]`, committed `2026-10-16T03:43:20Z` |
| Previous release | `0.3.19`, tagged **2026-08-30** | the `0.3.19` tag, committed `2026-08-30T12:51:37Z` |
| Changelog source | `release-notes.md` at the repository root | The project publishes no `CHANGELOG.md`; the `v0.3.16`–`v0.3.20` blocks of `release-notes.md` read *Release notes generation failed* and carry raw commit lists, so every entry below is quoted as a commit subject and checked against the `release` sources. |

`0.3.20` adds no new API surface for this area — the `0.3.19...0.3.20` range is a hardening release. What it does change, and how far that reaches the sources this page documents:

* **Nullability contracts.** 3 of 52 main sources in the scope of this page declare `@Nonnull` / `@Nullable`. Subjects in the range that name this area: `3c7729da Annotate converter nullability contracts`.
* **Null-input and behaviour fixes.** `88763a60 Handle null source in convertIfPossible` / `28e46fca Remove unused valueType from BeanUtils converters`.

> [!NOTE]
> Reading the commit list: Only `AbstractConverter`, `Converter` and `StringToInputStreamConverter` carry the annotations, so the `@Nonnull` / `@Nullable` contract on this page is still the exception rather than the rule across the 52 converter sources.

---

## 3. API reference

### 3.1 Entry points an application actually calls

The SPI contract (`convert/Converter.java:81-110`). `convert` is the only abstract method; the rest are `default`s that
read the class's own generic signature.

| Signature | Nullability / behaviour | Evidence |
|---|---|---|
| `boolean accept(Class<?> sourceType, Class<?> targetType)` | `default`: true when the converter's own `getSourceType()`/`getTargetType()` are assignable from the requested pair | `convert/Converter.java:81-83` |
| `@Nullable T convert(@Nullable S source)` | the single conversion; `@FunctionalInterface` method | `convert/Converter.java:91-92` |
| `default Class<S> getSourceType()` / `default Class<T> getTargetType()` | resolved from the generic type arguments via `reflect` | `convert/Converter.java:99-110` |
| `static <S,T> Converter<S,T> getConverter(Class<S>, Class<T>)` | returns the highest-priority accepting converter, **or `null`** | `convert/Converter.java:135-137` → `convert/Converters.java:62-69` |
| `static <T> T convertIfPossible(@Nullable Object source, Class<T> targetType)` | `null` source → `null`; no converter → `null`; otherwise `converter.convert(source)` | `convert/Converter.java:159-168` |

Multi-value contract (`convert/multiple/MultiValueConverter.java:45-90`):

| Signature | Nullability / behaviour | Evidence |
|---|---|---|
| `boolean accept(Class<S> sourceType, Class<?> multiValueType)` | per-target predicate | `convert/multiple/MultiValueConverter.java:45` |
| `Object convert(S source, Class<?> multiValueType, Class<?> elementType)` | builds the container; `null` source → `null` | `convert/multiple/MultiValueConverter.java:55`, `:35-37` |
| `static MultiValueConverter<?> find(Class<?> sourceType, Class<?> targetType)` | sorted + filter + `findFirst`, **or `null`** | `convert/multiple/MultiValueConverter.java:73-81` |
| `static <T> T convertIfPossible(Object source, Class<?> multiValueType, Class<?> elementType)` | `null` when no multi-value converter matches | `convert/multiple/MultiValueConverter.java:83-90` |

Concrete converters expose a public `INSTANCE` singleton you can call directly, bypassing the registry, e.g.
`StringToIntegerConverter.INSTANCE.convert("42")` (`convert/StringToIntegerConverter.java:32-37`). The full registered
set is listed in `src/main/resources/META-INF/services/io.microsphere.convert.Converter` (see §6 of the Developer Guide).

### 3.2 Converter-support matrix (source type → target types)

Every row is a registered, non-abstract converter. Multi-value targets require calling the `MultiValueConverter` entry
points (§3.1); the single-value `Converter` SPI does not produce them.

| Source type | Supported target types (single value) | Backing converters |
|---|---|---|
| `String` | `Boolean`, `Byte`, `Character`, `char[]`, `Short`, `Integer`, `Long`, `Float`, `Double`, `String`, `Duration`, `Class`, `InputStream` | `convert/StringTo*Converter.java`, `convert/StringToCharArrayConverter.java:31-34`, `convert/StringToDurationConverter.java:37-38`, `convert/StringToClassConverter.java:38-41`, `convert/StringToInputStreamConverter.java:55-59` |
| `String` (multi-value) | any array (`T[]`), `Collection`, `List`, `Set`, `SortedSet`, `NavigableSet`, `Queue`, `Deque`, `BlockingQueue`, `BlockingDeque`, `TransferQueue` | `convert/multiple/StringToArrayConverter.java`, `.../StringToCollectionConverter.java`, `.../StringToListConverter.java`, `.../StringToSetConverter.java`, `.../StringToSortedSetConverter.java`, `.../StringToNavigableSetConverter.java`, `.../StringToQueueConverter.java`, `.../StringToDequeConverter.java`, `.../StringToBlockingQueueConverter.java`, `.../StringToBlockingDequeConverter.java`, `.../StringToTransferQueueConverter.java` |
| `Number` | `Byte`, `Character`, `Short`, `Integer`, `Long`, `Float`, `Double` | `convert/NumberTo*Converter.java` |
| `Object` | `Boolean`, `byte[]`, `Byte`, `Character`, `Short`, `Integer`, `Long`, `Float`, `Double`, `Optional`, `String` | `convert/ObjectTo*Converter.java` |
| `byte[]` | `Object` (a `Serializable`) | `convert/ByteArrayToObjectConverter.java:38-45` |
| `Map` | `Properties` | `convert/MapToPropertiesConverter.java:32-37` |
| `Properties` | `String` | `convert/PropertiesToStringConverter.java:35-40` |

Notes on the matrix:
- `Object→T` targets are resolved by **recursion**: e.g. `ObjectToIntegerConverter` returns the value unchanged when it
  already *is* an `Integer`, otherwise it delegates to `Converter.convertIfPossible(source, Integer.class)`
  (`convert/ObjectToIntegerConverter.java:38-44`; same shape for `Boolean`/`Byte`/`Character`/`Double`/`Float`/`Long`/`Short`).
- `Object→String` is *not* recursive — it is `source.toString()` (`convert/ObjectToStringConverter.java:35-37`).
- `Object→Optional` always succeeds and never wraps `null` (`Optional.ofNullable`, `convert/ObjectToOptionalConverter.java:35-43`, priority `MIN_PRIORITY`).

### 3.3 Lossy / rounding / overflow behaviour

| Conversion | Input edge case | Result | Evidence |
|---|---|---|---|
| `String→Integer/Long/Short/Byte/Float/Double` | malformed **or** out of range | `NumberFormatException` (unchecked, propagates unchanged) | `valueOf` at `convert/StringToIntegerConverter.java:35-37`; propagation proven by `convert/StringToIntegerConverterTest.java:47-50` and rethrow-by-design `convert/AbstractConverter.java:107-111` + `util/ExceptionUtils.java:98-104` |
| `Number→Integer/Long/Short/Byte` | fractional `Number` | **truncated toward zero**, no rounding | `source.intValue()` `convert/NumberToIntegerConverter.java:36-41`; proven `convert/NumberToIntegerConverterTest.java:50-53` (`Double 1.0` → `Integer 1`) |
| `Number→Byte/Short` | value outside target range | **silent narrowing with wrap-around** (no exception) | `source.byteValue()` / `shortValue()` `convert/NumberToByteConverter.java:36-41`, `convert/NumberToShortConverter.java:40` |
| `Number→Float/Double` | large integral value | precision loss (widening, `floatValue`/`doubleValue`) | `convert/NumberToDoubleConverter.java:36-41`, `convert/NumberToFloatConverter.java:40` |
| `String→Boolean` | `"true"`/`"True"`/`"TRUE"` | `true`; any other non-empty text → `false`; empty or blank → **`null`** | `isNotEmpty(source) ? valueOf(source) : null` `convert/StringToBooleanConverter.java:35-38`; proven `convert/StringToBooleanConverterTest.java:49-55` |
| `String→Character` | 0 chars | `null` | `convert/StringToCharacterConverter.java:37-39`; proven `convert/StringToCharacterConverterTest.java:48-51` |
| `String→Character` | more than 1 char | `IllegalArgumentException` | `convert/StringToCharacterConverter.java:40-42`; proven `convert/StringToCharacterConverterTest.java:53-56` |
| `String→Duration` | non-ISO-8601 text | `DateTimeParseException` (strict, from `Duration.parse`) | `convert/StringToDurationConverter.java:37-38`; happy path `"PT12.345S"` `convert/StringToDurationConverterTest.java:22-30` |
| `String→Class` | unknown class name | `null` (resolve returns null, does not throw) | `resolveClass` `convert/StringToClassConverter.java:38-41` |
| Any `AbstractConverter` | `null` source | `null` (never reaches `doConvert`) | `convert/AbstractConverter.java:101-104` |
| `Map→Properties` | map containing a `null` value | `NullPointerException` (Properties extends `Hashtable`, which rejects `null` values; the wrapping in `AbstractConverter.convert` re-throws NPE unchanged) | `convert/MapToPropertiesConverter.java:32-37`; proven `convert/MapToPropertiesConverterTest.java:53-57` |
| `Object→Optional` | `null` source | **`Optional.empty()`** (this is the one registered converter that does *not* return `null` on `null` input, because it implements `Converter` directly rather than extending `AbstractConverter`) | `convert/ObjectToOptionalConverter.java:28-43`; proven `convert/ObjectToOptionalConverterTest.java:48-52` |

### 3.4 Calling patterns

There are four real ways to invoke the subsystem; pick the one that fits your failure policy.

**Direct singleton** — bypass the registry entirely, use when the pair is known at compile time and you want zero lookup:

```java
Integer n = StringToIntegerConverter.INSTANCE.convert("42"); // convert/StringToIntegerConverter.java:32-37
```

**Two-step (`getConverter` then `convert`)** — you get the strategy once and reuse it; `null` if no converter matches, so
this is where you insert your own default:

```java
Converter<String, Integer> c = Converter.getConverter(String.class, Integer.class); // convert/Converter.java:135
Integer n = (c == null) ? fallback : c.convert(text);                               // returns null only when the SPI has no match
```

**One-shot (`convertIfPossible`)** — null-tolerant convenience; `null` source or unsupported pair → `null`, no exceptions
for missing converters (but a *found* converter can still throw if its `doConvert` rejects the input):

```java
Integer n = Converter.convertIfPossible("123", Integer.class);   // convert/Converter.java:159-168
Duration d = Converter.convertIfPossible("PT2S", Duration.class); // convert/StringToDurationConverter.java:37-38
Date   dt = Converter.convertIfPossible("1", Date.class);         // null — no String→Date converter is registered (convert/ConverterTest.java:50)
```

**Multi-value** — split a delimited string into a container of some element type; the container class is fixed by the
selected converter, not by the requested interface:

```java
Set<Integer> s = MultiValueConverter.convertIfPossible("1,2,3", Set.class, Integer.class); // HashSet {1,2,3}; convert/multiple/MultiValueConverter.java:83-90
Integer[]  a = (Integer[]) MultiValueConverter.find(String.class, Integer[].class)
        .convert("1,2,3", Integer[].class, null);                                          // [1,2,3]; convert/multiple/StringToArrayConverter.java:36-49
```

**The null-vs-throw decision, in one line:** if no converter is found you get `null`; if a converter *is* found and its
`doConvert` throws, you get either the original unchecked exception (`NumberFormatException`,
`IllegalArgumentException`, `NullPointerException`) or a fresh `RuntimeException` (for checked exceptions like
`IOException` on the deserialization path) — see `convert/AbstractConverter.java:106-112` and
`util/ExceptionUtils.java:98-104`.

---

## 4. Examples mined from the tests

Every snippet below is condensed from a real test body on the `release` branch; the citation names the file and lines.

**Registry lookup returns the most specific converter for a pair** — iterating every registered converter and confirming
`getConverter` hands back an instance of the same class:

> `convert/ConverterTest.java:38-43`
```java
loadServicesList(Converter.class, Converter.class.getClassLoader()).stream().sorted().forEach(converter -> {
    assertSame(converter.getClass(), getConverter(converter.getSourceType(), converter.getTargetType()).getClass());
});
```

**`convertIfPossible` — including the "unsupported ⇒ null" rule** (note the `Date` case):

> `convert/ConverterTest.java:45-51`
```java
assertEquals(Integer.valueOf(2), convertIfPossible("2", Integer.class));
assertEquals(FALSE, convertIfPossible("false", Boolean.class));
assertEquals(Double.valueOf(1), convertIfPossible("1", Double.class));
assertNull(convertIfPossible("1", Date.class)); // no String->Date converter is registered
```

**String→Boolean leniency** — `"True"` is true, an arbitrary word is false, empty and `null` are `null`:

> `convert/StringToBooleanConverterTest.java:49-55`
```java
assertTrue(converter.convert("True"));
assertFalse(converter.convert("a"));
assertNull(converter.convert(""));
assertNull(converter.convert(null));
```

**String→Integer failure mode** — an unparseable string throws `NumberFormatException` (it is not swallowed into `null`):

> `convert/StringToIntegerConverterTest.java:47-50`
```java
assertThrows(NumberFormatException.class, () -> converter.convert("a"));
```

**Number→Integer truncation** — a fractional `Number` is truncated toward zero:

> `convert/NumberToIntegerConverterTest.java:50-53`
```java
assertEquals(Integer.valueOf(1), this.converter.convert(Double.valueOf(1.0)));
```

**Multi-value dispatch picks the most specific container** for the requested target type:

> `convert/multiple/MultiValueConverterTest.java:44-75`
```java
assertEquals(StringToArrayConverter.class,     MultiValueConverter.find(String.class, String[].class).getClass());
assertEquals(StringToCollectionConverter.class, MultiValueConverter.find(String.class, Collection.class).getClass());
assertEquals(StringToSetConverter.class,        MultiValueConverter.find(String.class, Set.class).getClass());
assertEquals(StringToDequeConverter.class,      MultiValueConverter.find(String.class, Deque.class).getClass());
```

**String→array** (comma-split, then element-wise `String→Integer`; a single token yields a one-element array, an empty
string yields the empty array, `null` yields `null`):

> `convert/multiple/StringToArrayConverterTest.java:53-59`
```java
assertArrayEquals(new Integer[]{123},    (Integer[]) converter.convert("123",   Integer[].class, Integer.class));
assertArrayEquals(new Integer[]{1,2,3},  (Integer[]) converter.convert("1,2,3", Integer[].class, null));
assertArrayEquals(EMPTY_INTEGER_OBJECT_ARRAY, (Integer[]) converter.convert("", Integer[].class, null));
assertNull(converter.convert(null, Integer[].class, null));
```

**String→Collection** builds a `List`-like `Collection`; **String→Set** builds a `HashSet` regardless of the
`multiValueType` argument (the concrete container is fixed by the converter, not by the requested interface):

> `convert/multiple/StringToCollectionConverterTest.java:88-107`
```java
Collection result = (Collection<Long>) converter.convert("1,2,3", Collection.class, Long.class);
assertEquals(ofList(1L, 2L, 3L), result);
assertEquals(emptyList(), converter.convert("", Collection.class, Integer.class));
assertNull(converter.convert(null, Collection.class, Integer.class));
```
> `convert/multiple/StringToSetConverterTest.java:89-106`
```java
Set result = (Set<Double>) converter.convert("1.0,2.0,3.0", Queue.class, Double.class); // still a HashSet
assertTrue(CollectionUtils.equals(new HashSet(ofList(1.0, 2.0, 3.0)), result));
assertNull(converter.convert("", Collection.class, null)); // null elementType -> no element converter -> null
```

**Widening a `Number` up to `Double`** — an `Integer` source becomes the equivalent `Double` (no truncation on widening):

> `convert/NumberToDoubleConverterTest.java:50-53`
```java
assertEquals(Double.valueOf(1.0), this.converter.convert(1)); // int 1 -> double 1.0
```

**Map→Properties on a `null`-valued map is a genuine throw site:**

> `convert/MapToPropertiesConverterTest.java:53-57`
```java
Map<String, Object> map = singletonMap("key", null);
assertThrows(NullPointerException.class, () -> this.converter.convert(map));
```

**Properties→String embeds a timestamp**, so the output is *not* stable across calls (the `#<date>` comment that
`Properties.store` inserts changes every run):

> `convert/PropertiesToStringConverterTest.java:57-66`
```java
return "#Generated by Microsphere" + LINE_SEPARATOR   // fixed banner, from store(writer, "Generated by Microsphere")
        + "#" + new Date() + LINE_SEPARATOR           // volatile timestamp line
        + "A=1" + LINE_SEPARATOR;
```

**`Object→Optional` and unsupported multi-value targets** return a neutral value rather than throwing:

> `convert/multiple/MultiValueConverterTest.java:77-81`
```java
assertEquals(ofSet(1, 2, 3), convertIfPossible("1,2,3", Set.class, Integer.class));
assertNull(convertIfPossible("1,2,3", Array.class, String.class)); // java.lang.reflect.Array is not a container type
```

---

## 5. Compatibility notes

- **`java.time` vs `Date`**: this subsystem converts to `java.time.Duration` (`convert/StringToDurationConverter.java`)
  but **not** to `java.util.Date` or any other `java.time` type. `Date`/`Instant`/`LocalDate` parsing is out of scope —
  `convertIfPossible("1", Date.class)` is `null` (`convert/ConverterTest.java:50`).
- **`Duration.parse` is strict** and requires ISO-8601 (`PT…`); it is available from Java 8 onward, so there is no
  version gate on it.
- **Character vs `char[]`**: `String→Character` accepts a *single* character only (`convert/StringToCharacterConverter.java:40-42`);
  use `String→char[]` (`convert/StringToCharArrayConverter.java:31-34`) for the whole string. Both target types are
  registered, so `getConverter(String.class, Character.class)` and `getConverter(String.class, char[].class)` resolve to
  different strategies.
- **`String→InputStream` charset**: the singleton `INSTANCE` uses the platform `DEFAULT_CHARSET`
  (`convert/StringToInputStreamConverter.java:43-45`); construct your own instance with an explicit `Charset`/encoding
  (`:47-53`) when byte-level determinism matters. This converter depends on `io.FastByteArrayInputStream` and
  `nio.charset.CharsetUtils`.
- **Serialization round-trip**: `byte[] ⇄ Object` (`convert/ByteArrayToObjectConverter.java`,
  `convert/ObjectToByteArrayConverter.java`) delegates to `io.microsphere.io.serializer`; the object side must be
  `Serializable` (`convert/ByteArrayToObjectConverter.java:39-45`).

---

## 6. Pitfalls and known limits

- **Unsupported conversion is `null`, not an exception.** `getConverter` returns `null` when no converter accepts the
  pair (`convert/Converters.java:68`), and `convertIfPossible` then returns `null` (`convert/Converter.java:164-167`).
  If your downstream code cannot tolerate `null`, check the converter (or the result) explicitly.
- **A *found* converter that fails still throws.** `AbstractConverter.convert` wraps `doConvert` in a `try/catch`, logs a
  warning, and rethrows via `ExceptionUtils.wrap(e, RuntimeException.class)` (`convert/AbstractConverter.java:106-112`).
  Because `wrap` returns the source unchanged when it is already a `RuntimeException` (`util/ExceptionUtils.java:98-100`),
  `NumberFormatException` / `IllegalArgumentException` propagate with their original type. `ByteArrayToObjectConverter`
  on garbage bytes throws `RuntimeException` (`convert/ByteArrayToObjectConverter.java:42-44`;
  proven `convert/ByteArrayToObjectConverterTest.java:49-52`).
- **`StringToArrayConverter` does not null-check the element converter.** It resolves
  `Converter.getConverter(String.class, componentType)` and dereferences it immediately
  (`convert/multiple/StringToArrayConverter.java:40-46`); if the array's *component type* has no registered `String`
  converter, this raises `NullPointerException`. The `Iterable`-based converters are safer: they check first and return
  `null` when the element converter is missing (`convert/multiple/StringToIterableConverter.java:43-44,60`).
- **The `multiValueType` argument is advisory for collections.** `StringToIterableConverter` fixes the concrete
  container in `createMultiValue` — `StringToSetConverter` returns a `HashSet` even when you pass `Queue.class`
  (`convert/multiple/StringToSetConverter.java:29-32`, `convert/multiple/StringToIterableConverter.java:47`). Do not
  assume you get back the exact class you asked for.
- **Comma is the only separator.** `StringToMultiValueConverter` splits on `SymbolConstants.COMMA_CHAR`
  (`convert/multiple/StringToMultiValueConverter.java:40`); strings containing commas cannot be expressed as multi-value
  without escaping, and there is no configurable delimiter here.
- **`Object`→number is a two-hop recursion.** `ObjectToIntegerConverter` falls back to `convertIfPossible`
  (`convert/ObjectToIntegerConverter.java:39-43`), which itself may route through another `Object→…` converter; a deeply
  ambiguous pair can therefore return `null` even though a direct `String→Integer` would have worked if you had requested
  that pair instead.
- **Priority is depth-derived, not hand-set.** `AbstractConverter.resolvePriority()` computes a negative value from the
  inheritance depth of the source and target types (`convert/AbstractConverter.java:139-148`), so more specific
  converters sort first. Multi-value converters override priority to `MIN_PRIORITY - depth`
  (`convert/multiple/StringToIterableConverter.java:74-78`) — which is exactly why `find(String.class, Set.class)` beats
  the general `Collection` converter (`convert/multiple/StringToSetConverterTest.java:113-116`).
- **The "null in → null out" rule has one exception.** `ObjectToOptionalConverter` does *not* extend
  `AbstractConverter`, so `convert(null)` returns `Optional.empty()` rather than `null`
  (`convert/ObjectToOptionalConverter.java:28-43`, `convert/ObjectToOptionalConverterTest.java:48-52`). If your code
  special-cases `null` results, do not assume it holds for the `Optional` target.
- **`Map→Properties` rejects `null` values.** Because `Properties` extends `Hashtable`,
  `MapToPropertiesConverter.convert(mapWithNullValue)` throws `NullPointerException`
  (`convert/MapToPropertiesConverter.java:32-37`, `convert/MapToPropertiesConverterTest.java:53-57`) — the only place in
  the single-value set where *input shape*, not just parseability, causes an unchecked throw.
- **`Properties→String` is non-deterministic.** `PropertiesToStringConverter` calls
  `source.store(writer, "Generated by Microsphere")` (`convert/PropertiesToStringConverter.java:37-39`), and `store`
  appends a `#<current date>` comment line, so byte-for-byte equality across calls is not guaranteed
  (`convert/PropertiesToStringConverterTest.java:57-66`).

---

## 7. See also

- [Language abstractions](./language-abstractions.md) — `Prioritized` ordering arithmetic that drives converter selection.
- [Reflection, types and beans](./reflection-and-beans.md) — `TypeUtils.resolveActualTypeArgumentClass`, used to derive
  `getSourceType()` / `getTargetType()` (`convert/Converter.java:99-110`).
- [Serialization](./serialization.md) — the `io.serializer` backend behind `byte[] ⇄ Object`
  (`convert/ByteArrayToObjectConverter.java`, `convert/ObjectToByteArrayConverter.java`).
- [Collections and filters](./collections-and-filters.md) — the container factories the multi-value converters call into.
- [Configuration property metadata](./configuration-metadata.md) — the main in-library consumer of type conversion.
- [Type conversion — Developer Guide](../../developer-guide/microsphere-java-core/type-conversion.md) — selection/caching
  internals, dispatch pipeline, and how to register your own converter through SPI.
