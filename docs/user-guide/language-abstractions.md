# Language Abstractions

**Module**: `io.github.microsphere-projects:microsphere-java-core`
**Packages**: `io.microsphere.lang`, `io.microsphere.lang.function`, `io.microsphere.lang.invoke`,
`io.microsphere.invoke`, `io.microsphere.misc`

Four small contracts and one exception-handling idiom carry most of the framework's design:
`Prioritized`, `Wrapper`, `Deprecation`, the `Throwable*` functional interfaces, and the `MethodHandle` helpers.

---

## 1. `Prioritized` — the ordering spine

```java
public interface Prioritized extends Comparable<Prioritized> {

    Comparator<Object> COMPARATOR;

    int MAX_PRIORITY    = Integer.MIN_VALUE;   // -2147483648
    int MIN_PRIORITY    = Integer.MAX_VALUE;   //  2147483647
    int NORMAL_PRIORITY = 0;

    default int getPriority() {
        return NORMAL_PRIORITY;
    }

    @Override
    default int compareTo(Prioritized that) {
        return Integer.compare(getPriority(), that.getPriority());
    }
}
```

**Semantics**: the *name* describes importance, the *value* is its inverse.
`MAX_PRIORITY == Integer.MIN_VALUE`, so a **smaller integer always wins and sorts first**.
Default priority is `NORMAL_PRIORITY` (`0`), not the minimum.

> [!WARNING]
> The Javadoc on `Prioritized.getPriority()` ("default is `MIN_PRIORITY`") and on
> `EventListener.getPriority()` ("default is `Integer#MAX_VALUE`") both contradict the code.
> The code returns `NORMAL_PRIORITY` (0); trust the code.

`COMPARATOR` is `Comparator<Object>` and is deliberately tolerant of heterogeneous lists: `Prioritized` objects sort
**before** non-`Prioritized` ones, and among two `Prioritized` objects it delegates to `compareTo`. Every SPI load in
the framework funnels through it (see `ServiceLoaderUtils`).

```java
public class FastConverter implements Converter<String, Integer>, Prioritized {
    @Override
    public int getPriority() {
        return Prioritized.MAX_PRIORITY + 1;    // almost always selected first
    }
}
```

### Adopting `Prioritized` in your own SPI

```java
public interface DataValidator extends Prioritized {
    ValidationResult validate(Object data);
}

List<DataValidator> validators = ServiceLoaderUtils.loadServicesList(DataValidator.class);
// already sorted ascending by getPriority(), i.e. most important first
for (DataValidator validator : validators) {
    if (!validator.validate(data).isValid()) {
        break;
    }
}
```

Leave a gap between your levels (`NORMAL_PRIORITY`, `NORMAL_PRIORITY + 5`, `NORMAL_PRIORITY + 10`, …) — that is the
convention this framework uses for `LoggerFactory`, `ProcessIdResolver` and `ConfigurationPropertyLoader`, so
third-party implementations can slot in between.

---

## 2. `Wrapper` and `DelegatingWrapper`

```java
public interface Wrapper {

    <T> T unwrap(Class<T> type) throws IllegalArgumentException;

    boolean isWrapperFor(Class<?> type);

    static <T> T tryUnwrap(Object object, Class<T> type)   // returns null unless object is a Wrapper that matches
}

public interface DelegatingWrapper extends Wrapper {

    Object getDelegate();

    // default unwrap(Class<T>) / isWrapperFor(Class<?>) implementations that walk getDelegate()
}
```

`tryUnwrap` returns **`null`** when the object is not a `Wrapper` or does not match the requested type — despite
what its Javadoc prose suggests, it does not hand back the original object. Always null-check.

> [!NOTE]
> This is the same contract as `java.sql.Wrapper`, which is why JDBC-style decorator chains work with it. The
> framework's own delegating types (`DelegatingIterator`, `DelegatingDeque`, `DelegatingBlockingQueue`, …) all
> implement `DelegatingWrapper`, so callers can always get the real collection back.

```java
DataSource real = Wrapper.tryUnwrap(metricsDataSource, HikariDataSource.class);
if (real != null) {
    real.setMaximumPoolSize(32);
}
```

`WrapperProcessor<W extends Wrapper>` is the companion callback interface — `W process(W wrapper)` — for code that
walks and rewrites wrapper chains.

---

## 3. `Deprecation`

A structured, machine-readable replacement for `@Deprecated` prose, used by the reflective definition types in
[Reflection and Types](reflection-and-types.md).

```java
public final class Deprecation implements Serializable {

    public static Builder builder()
    public static Deprecation of(String since)
    // plus of(...) overloads up to (since, replacement, reason, link, level)

    public Version getSince()
    public String getReplacement()
    public String getReason()
    public String getLink()
    public Level getLevel()

    public enum Level
    public static class Builder {
        public Builder since(String since)
        public Builder since(Version since)
        public Builder replacement(String replacement)
        public Builder reason(String reason)
        public Builder link(String link)
        public Builder level(Level level)
        public Deprecation build()
    }
}
```

```java
Deprecation note = Deprecation.builder()
        .since("0.3.0")
        .replacement("TypeUtils#resolveActualTypeArgumentClass")
        .reason("Raw Type lookup is ambiguous for nested generics")
        .link("https://github.com/microsphere-projects/microsphere-java/issues/221")
        .level(Deprecation.Level.REMOVAL)   // Level has DEFAULT and REMOVAL only
        .build();
```

---

## 4. Mutable holders for lambdas

Lambdas capture effectively-final locals; these types give you a legal mutable box.

```java
// io.microsphere.lang
public class MutableInteger extends Number {
    public MutableInteger(int value)
    public static MutableInteger of(int value)
    public int get()
    public MutableInteger set(int value)
    public int getAndSet(int newValue)
    public int getAndIncrement()
    public int getAndDecrement()
    public int getAndAdd(int delta)
    public int incrementAndGet()
    public int decrementAndGet()
    public int addAndGet(int delta)
    // intValue/longValue/floatValue/doubleValue
}

// io.microsphere.util
public class ValueHolder<V> {
    public ValueHolder()
    public ValueHolder(V value)
    public static <V> ValueHolder<V> of(V value)
    public V getValue()
    public void setValue(V value)
    public void reset()
}
```

`ValueHolder` is the general form (any type, `reset()` returns to the initial value); `MutableInteger` is the
`int`-specialised form that avoids boxing and satisfies `Number`. `ClassDataRepository.INSTANCE`
(`getAllPackageNamesInClassPaths()`) is a related utility: a per-class-loader cache of package names, used by the
classpath scanners.

---

## 5. Throwable-aware functional interfaces

`java.util.function` interfaces cannot throw checked exceptions. The `io.microsphere.lang.function` package adds
mirrors whose abstract method declares `throws Throwable`, plus `execute(...)` helpers that wrap failures.

### `ThrowableFunction<T, R>`

```java
@FunctionalInterface
public interface ThrowableFunction<T, R> {

    R apply(T t) throws Throwable;                       // the lambda target

    default R execute(T t) throws RuntimeException       // wraps Throwable in RuntimeException
    default R execute(T t, BiFunction<T, Throwable, R> exceptionHandler) throws RuntimeException
    default R handleException(T t, Throwable failure)    // default: throw new RuntimeException(failure)

    default <V> ThrowableFunction<V, R> compose(ThrowableFunction<? super V, ? extends T> before)
    default <V> ThrowableFunction<T, V> andThen(ThrowableFunction<? super R, ? extends V> after)

    static <T, R> R execute(T t, ThrowableFunction<T, R> function)
    static <T, R> R execute(T t, ThrowableFunction<T, R> function, BiFunction<T, Throwable, R> exceptionHandler)
}
```

### The family, side by side

| Interface | Abstract method | Instance `execute` | Static `execute` | Composition |
|---|---|---|---|---|
| `ThrowableFunction<T,R>` | `R apply(T t) throws Throwable` | `execute(T)`, `execute(T, BiFunction<T,Throwable,R>)` | yes (2 forms) | `andThen`, `compose` |
| `ThrowableBiFunction<T,U,R>` | `R apply(T first, U second) throws Throwable` | — | `execute(T, U, fn)`, `execute(T, U, fn, ExceptionHandler)` | nested `interface ExceptionHandler<T,U,R>` |
| `ThrowableConsumer<T>` | `void accept(T t) throws Throwable` | `execute(T)`, `execute(T, BiConsumer<T,Throwable>)` | yes (2 forms) | **none** |
| `ThrowableBiConsumer<T,U>` | `void accept(T t, U u) throws Throwable` | — | — | `andThen(ThrowableBiConsumer)` |
| `ThrowableSupplier<T>` | `T get() throws Throwable` | `execute()`, `execute(Function<Throwable,T>)` | yes (2 forms) | — |
| `ThrowableAction` | `void execute() throws Throwable` | `execute(Consumer<Throwable>)` | yes (2 forms) | — |

> [!IMPORTANT]
> The helper method is `execute`, and it converts failure to `RuntimeException` via `handleException`. For
> `ThrowableAction` the *abstract* method is `execute()` itself — do not confuse `action.execute()` (may throw
> `Throwable`) with `action.execute(handler)` (swallows and reports).

```java
// Checked I/O inside a stream
List<String> lines = paths.stream()
        .map(path -> {
            try {
                return IOUtils.toString(new FileInputStream(path));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        })
        .collect(toList());
```

With `ThrowableFunction` the same pipeline needs no try/catch:

```java
ThrowableFunction<String, String> read = path -> IOUtils.toString(new FileInputStream(path));

ThrowableFunction<String, String> readAndTrim = read.andThen(StringUtils::trimWhitespace);

String content = readAndTrim.execute("/etc/hosts", (path, failure) -> {
    logger.warn("Cannot read {}: {}", path, failure.getMessage());
    return "";
});
```

```java
ThrowableAction   closeQuietly = connection::close;
closeQuietly.execute(failure -> logger.error("Close failed", failure));

ThrowableSupplier<Properties> loader = () -> PropertiesUtils.loadProperties("a=1", "b=2");
Properties defaults = loader.execute(failure -> new Properties());
```

### `Streams` and `Predicates`

Both are `public interface` types holding `static` helpers (they carry the `Utils` marker pattern).

```java
// io.microsphere.lang.function.Streams
public static <T> Stream<T> stream(T... values)
public static <T> Stream<T> stream(Iterable<T> iterable)
public static <T> Stream<T> filterStream(T[] values, Predicate<? super T> predicate)
public static <T, S extends Iterable<T>> Stream<T> filterStream(S values, Predicate<? super T> predicate)
public static <T> List<T> filterList(T[] values, Predicate<? super T> predicate)
public static <T, S extends Iterable<T>> List<T> filterList(S values, Predicate<? super T> predicate)
public static <T> Set<T> filterSet(T[] values, Predicate<? super T> predicate)
public static <T, S extends Iterable<T>> Set<T> filterSet(S values, Predicate<? super T> predicate)
public static <T, S extends Iterable<T>> S filter(S values, Predicate<? super T> predicate)
public static <T> List<T> filterAllList(T[] values, Predicate<? super T>... predicates)   // AND
public static <T> Set<T> filterAllSet(T[] values, Predicate<? super T>... predicates)
public static <T> List<T> filterAnyList(T[] values, Predicate<? super T>... predicates)   // OR
public static <T> Set<T> filterAnySet(T[] values, Predicate<? super T>... predicates)
public static <T> T filterFirst(Iterable<T> values, Predicate<? super T>... predicates)

// io.microsphere.lang.function.Predicates
public static <T> Predicate<T> alwaysTrue()
public static <T> Predicate<T> alwaysFalse()
public static <T> Predicate<T>[] emptyArray()
public static <T> Predicate<? super T> and(Predicate<? super T>... predicates)
public static <T> Predicate<? super T> or(Predicate<? super T>... predicates)
```

`filter`/`filterList`/`filterSet` keep the source's shape (or force it), which removes the usual
`collect(toList())` boilerplate; `filterAll*` means "match every predicate" while `filterAny*` means "match at least
one".

---

## 6. Lambda and method-handle internals

### `LambdaUtils` (`io.microsphere.lang.invoke`)

```java
public abstract class LambdaUtils implements Utils {
    @Nullable public static List<Class<?>> resolveLambdaMethodParameterTypes(Object lambda)
    @Nullable public static List<Class<?>> resolveLambdaMethodParameterTypes(Class<?> lambdaClass)
    @Nullable public static List<Class<?>> resolveLambdaMethodParameterTypes(Object lambda, Class<?> functionalInterface)
    @Nullable public static List<Class<?>> resolveLambdaMethodParameterTypes(Class<?> lambdaClass, Class<?> functionalInterface)
}
```

Recovers the captured parameter types of a lambda instance — useful when a lambda is your configuration DSL.
Returns `null` when the JVM's internal `MethodType` lookup is unavailable (this is JDK-internals territory; on
JDK 16+ the build already adds the required `--add-opens`, see [Getting Started](getting-started.md#61-jdk-16-specifics)).

### `MethodHandleUtils` and `MethodHandlesLookupUtils` (`io.microsphere.invoke`)

```java
public abstract class MethodHandleUtils implements Utils {
    public static final Lookup PUBLIC_LOOKUP;
    // LookupMode bit flags: PACKAGE, MODULE, UNCONDITIONAL, ORIGINAL, ALL_MODES

    public static Lookup lookup(Class<?> type)
    public static Lookup lookup(Class<?> type, LookupMode... modes)
    public static MethodHandle findVirtual(Class<?> type, String name, Class<?>... parameterTypes)
    public static MethodHandle findStatic(Class<?> owner, String name, Class<?>... parameterTypes)
    public static Object handleInvokeExactFailure(Throwable failure, MethodHandle handle, Object... args)

    public enum LookupMode {
        static int getModes(LookupMode... modes)
    }
}

public abstract class MethodHandlesLookupUtils implements Utils {
    public static final MethodHandle NOT_FOUND_METHOD_HANDLE;   // null
    public static final Lookup PUBLIC_LOOKUP;
    public static MethodHandle findPublicVirtual(Class<?> type, String name, Class<?>... parameterTypes)
    public static MethodHandle findPublicStatic(Class<?> type, String name, Class<?>... parameterTypes)
}
```

Use these instead of raw `MethodHandles.lookup()` when you need to reach non-public members *and* want the code to
degrade gracefully across JDKs: `findPublic*` returns `null` (compare with `NOT_FOUND_METHOD_HANDLE`) rather than
throwing, while the `LookupMode` variants attempt privileged lookups and return a handle you can `invokeExact`.

### `UnsafeUtils` (`io.microsphere.misc`)

Exposes `sun.misc.Unsafe` array offsets/scales (`LONG_ARRAY_BASE_OFFSET`, `INT_ARRAY_BASE_OFFSET`,
`OBJECT_ARRAY_INDEX_SCALE`, …) and typed readers such as
`getLongVolatileFromArray(Object object, String fieldName, int index)`. This is for high-performance/collection
internals only; treat it as an implementation detail rather than a stable API.

---

## 7. See also

* [Reflection and Types](reflection-and-types.md) — `ClassDefinition` / `MethodDefinition` consume `Deprecation`
* [Type Conversion](type-conversion.md) — `AbstractConverter` auto-computes a `Prioritized` value from type depth
* [Reference](reference.md) — where `Prioritized.COMPARATOR` is applied

[← Previous: Collections and Filters](collections-and-filters.md) · [Index](README.md) · [Next: Reflection and Types →](reflection-and-types.md)
