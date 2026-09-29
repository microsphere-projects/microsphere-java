# Language Abstractions

> Read this page in: [中文](../zh/language-abstractions.md) · [English](language-abstractions.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Module | `io.github.microsphere-projects:microsphere-java-core` |
| Packages | `io.microsphere.lang`, `io.microsphere.lang.function`, `io.microsphere.lang.invoke`, `io.microsphere.invoke`, `io.microsphere.util` |
| Contracts | `Prioritized` (ordering), `Wrapper` / `DelegatingWrapper` (decorator chains), `Deprecation` (structured deprecation data) |
| Functional | six throwable-aware interfaces in `io.microsphere.lang.function`, plus `Predicates` and `Streams` |
| Low level | `LambdaUtils`, `MethodHandleUtils`, `MethodHandlesLookupUtils`, `UnsafeUtils` |
| Ordering rule | **a smaller priority value always wins** — `Prioritized.MAX_PRIORITY == Integer.MIN_VALUE` |

Four small contracts and one exception-handling idiom carry most of the framework's design. This page is the
"how do I plug in / unwrap / catch" reference for them.

---

## 1. `Prioritized` — the ordering spine

```java
public interface Prioritized extends Comparable<Prioritized> {

    Comparator<Object> COMPARATOR;                 // java.util.Comparator<Object>

    int MAX_PRIORITY    = Integer.MIN_VALUE;       // -2147483648
    int MIN_PRIORITY    = Integer.MAX_VALUE;       //  2147483647
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

The constant *names* describe importance, the *values* are their inverse: `MAX_PRIORITY` is the smallest integer, so
**lower numbers sort first and are chosen first**. Implement nothing and you get `NORMAL_PRIORITY` (`0`).

```java
public class FastConverter implements Converter<String, Integer>, Prioritized {

    @Override
    public int getPriority() {
        return Prioritized.MAX_PRIORITY + 1;    // nearly always selected first
    }
}
```

`COMPARATOR` is deliberately tolerant of heterogeneous lists:

| Situation | Result |
|---|---|
| both are `Prioritized` | `((Prioritized) one).compareTo((Prioritized) two)` |
| only the first is `Prioritized` | `-1` (the `Prioritized` object goes first) |
| only the second is `Prioritized` | `1` |
| neither is `Prioritized` | `0` — a stable sort (`List.sort`, `Collections.sort`) keeps the original order |

Every SPI load in the library funnels through it, so implementing `Prioritized` is enough to control your position:

```java
public interface DataValidator extends Prioritized {          // your own SPI
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

> [!TIP]
> Leave gaps between your levels (`NORMAL_PRIORITY`, `NORMAL_PRIORITY + 5`, `NORMAL_PRIORITY + 10`, ...). This is
> the convention used by `LoggerFactory`, `ProcessIdResolver` and `ConfigurationPropertyLoader`, so third-party
> implementations can slot in between without renumbering anything.

> [!WARNING]
> Two Javadoc comments contradict the code and you should ignore them: `Prioritized.getPriority()` claims the
> default is `MIN_PRIORITY`, and `EventListener.getPriority()` claims the default is `Integer#MAX_VALUE`. Both
> methods actually return `NORMAL_PRIORITY` (`0`).

---

## 2. `Wrapper` and `DelegatingWrapper`

```java
public interface Wrapper {

    <T> T unwrap(Class<T> type) throws IllegalArgumentException;

    boolean isWrapperFor(Class<?> type);

    static <T> T tryUnwrap(Object object, Class<T> type) {
        if (object instanceof Wrapper) {
            Wrapper wrapper = (Wrapper) object;
            if (wrapper.isWrapperFor(type)) {
                return wrapper.unwrap(type);
            }
        }
        return null;    // not a Wrapper, or no match
    }
}

public interface DelegatingWrapper extends Wrapper {

    Object getDelegate();

    // default unwrap(Class<T>) and isWrapperFor(Class<?>) — see the resolution rules below
}
```

`Wrapper.tryUnwrap(object, type)` returns **`null`** whenever `object` is not a `Wrapper`, or that wrapper does not
match the requested type. It never hands the original object back. Always null-check the result.

`DelegatingWrapper` resolves in this order:

1. `type` equals the wrapper's own class → returns `this`.
2. `getDelegate()` is an instance of `type` → returns the delegate.
3. otherwise → `IllegalArgumentException`.

```java
// decorated wraps a CacheDataSource, which itself wraps a RawDataSource
CacheDataSource cache = Wrapper.tryUnwrap(decorated, CacheDataSource.class);   // found — one hop away
RawDataSource raw = Wrapper.tryUnwrap(decorated, RawDataSource.class);         // null — two hops away
if (raw == null && cache != null) {
    raw = Wrapper.tryUnwrap(cache, RawDataSource.class);                       // walk deeper yourself
}
```

> [!NOTE]
> This mirrors `java.sql.Wrapper`, which is why JDBC-style decorator chains work with it. Resolution is
> **single-level**: a two-deep chain needs another `tryUnwrap` on the delegate (or a loop). The framework's own
> delegating types (`DelegatingIterator`, `DelegatingQueue`, `DelegatingDeque`, and the collection wrappers built
> on them) all implement `DelegatingWrapper`, so the real collection is always reachable —
> see [Collections and Filters](collections-and-filters.md#6-single-element-empty-unmodifiable-and-delegating-collections).

`WrapperProcessor<W extends Wrapper>` is the companion callback, `W process(W wrapper)`, for code that walks and
rewrites a wrapper chain:

```java
WrapperProcessor<DelegatingWrapper> processor = wrapper -> {
    Object delegate = wrapper.getDelegate();
    System.out.println(wrapper.getClass().getSimpleName() + " -> " + delegate);
    return wrapper;    // return the wrapper to keep the chain, or a replacement instance
};
```

---

## 3. `Deprecation` — structured `@Deprecated` data

`Deprecation` is a `final`, `Serializable`, immutable value object used by the reflective definition types
([Reflection and Types](reflection-and-types.md)) instead of free-form Javadoc prose.

```java
public final class Deprecation implements Serializable {

    public static Builder builder()
    public static Deprecation of(String since)
    public static Deprecation of(String since, String replacement)
    public static Deprecation of(String since, String replacement, String reason)
    public static Deprecation of(String since, String replacement, String reason, String link)
    public static Deprecation of(String since, String replacement, String reason, String link, Level level)

    @Nullable public Version getSince()          // io.microsphere.util.Version
    @Nullable public String getReplacement()
    @Nullable public String getReason()
    @Nullable public String getLink()
    @Nonnull  public Level getLevel()

    public enum Level { DEFAULT, REMOVAL }

    public static class Builder {
        public Builder since(String since)       // parsed through Version.of(String)
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
        .level(Deprecation.Level.REMOVAL)   // Level is DEFAULT or REMOVAL only
        .build();
```

`level(null)` falls back to `Level.DEFAULT`, so `level` is never `null`; the other four members are.
`since("0.3.0")` parses the string into a `Version` — an unparsable string fails at parse time, not at `build()`.
Two `Deprecation` instances are `equals` when all five members match.

---

## 4. Mutable holders used with lambdas

Lambdas may only capture effectively-final locals; these two types are the legal mutable boxes.

```java
// io.microsphere.lang
public class MutableInteger extends Number {
    public MutableInteger(int value)
    public static MutableInteger of(int value)
    public int get()
    public MutableInteger set(int value)          // returns this, so you can chain
    public int getAndSet(int newValue)
    public int getAndIncrement()
    public int getAndDecrement()
    public int getAndAdd(int delta)
    public int incrementAndGet()
    public int decrementAndGet()
    public int addAndGet(int delta)
    // intValue()/longValue()/floatValue()/doubleValue(), equals/hashCode
}

// io.microsphere.util
public class ValueHolder<V> {
    public ValueHolder()
    public ValueHolder(V value)
    public static <V> ValueHolder<V> of(V value)
    public V getValue()
    public void setValue(V value)
    public void reset()                            // sets the current value to null
}
```

```java
MutableInteger errors = MutableInteger.of(0);
listeners.forEach(listener -> {
    try {
        listener.run();
    } catch (Throwable failure) {
        errors.incrementAndGet();
    }
});
if (errors.intValue() > 0) { /* ... */ }

ValueHolder<Class<?>> found = new ValueHolder<>();
classes.forEach(type -> { if (predicate.test(type)) found.setValue(type); });
```

Both are plain (non-synchronized) objects: safe for single-threaded lambda plumbing, not a `AtomicInteger` /
`AtomicReference` substitute for concurrent code. `MutableInteger.equals` compares against another `MutableInteger`
only, so `equals(Integer)` is always `false`.

`ClassDataRepository.INSTANCE` (`io.microsphere.lang`) is the related per-classloader cache used by the classpath
scanners: `getAllPackageNamesInClassPaths()`, `findClassPath(Class | String)`,
`getClassNamesInClassPath(String classPath, boolean recursive)`, `getClassNamesInPackage(Package | String)`,
`getClassPathToClassNamesMap()`, `getAllClassNamesInClassPaths()`.

---

## 5. Throwable-aware functional interfaces

The `java.util.function` interfaces cannot throw checked exceptions. Every mirror in
`io.microsphere.lang.function` declares `throws Throwable` on its abstract method and, where useful, adds an
`execute(...)` helper that converts the failure.

```java
@FunctionalInterface
public interface ThrowableFunction<T, R> {

    R apply(T t) throws Throwable;                       // the lambda target

    default R execute(T t) throws RuntimeException        // delegates to handleException
    default R execute(T t, BiFunction<T, Throwable, R> exceptionHandler)
    default R handleException(T t, Throwable failure)    // throws new RuntimeException(failure)

    default <V> ThrowableFunction<V, R> compose(ThrowableFunction<? super V, ? extends T> before)
    default <V> ThrowableFunction<T, V> andThen(ThrowableFunction<? super R, ? extends V> after)

    static <T, R> R execute(T t, ThrowableFunction<T, R> function)
    static <T, R> R execute(T t, ThrowableFunction<T, R> function, BiFunction<T, Throwable, R> exceptionHandler)
}
```

| Interface | Abstract method | Instance helpers | Static helpers |
|---|---|---|---|
| `ThrowableFunction<T, R>` | `R apply(T) throws Throwable` | `execute(T)`, `execute(T, BiFunction<T, Throwable, R>)`, `handleException`, `compose`, `andThen` | `execute(t, fn)`, `execute(t, fn, handler)` |
| `ThrowableBiFunction<T, U, R>` | `R apply(T, U) throws Throwable` | none (no instance `execute`) | `execute(a, b, fn)`, `execute(a, b, fn, ExceptionHandler)`, nested `interface ExceptionHandler<T, U, R> { R handle(T, U, Throwable); }` |
| `ThrowableConsumer<T>` | `void accept(T) throws Throwable` | `execute(T)`, `execute(T, BiConsumer<T, Throwable>)`, `handleException` | `execute(t, consumer)` ×2 |
| `ThrowableBiConsumer<T, U>` | `void accept(T, U) throws Throwable` | `andThen(ThrowableBiConsumer)` — itself declared `throws Throwable` | none |
| `ThrowableSupplier<T>` | `T get() throws Throwable` | `execute()`, `execute(Function<Throwable, T>)`, `handleException` | `execute(supplier)` ×2 |
| `ThrowableAction` | `void execute() throws Throwable` | `execute(Consumer<Throwable>)`, `handleException` | `execute(action)` ×2 |

> [!IMPORTANT]
> For `ThrowableAction` the **abstract** method is `execute()` and it may propagate `Throwable`; the one-argument
> `execute(handler)` is the default method that swallows the failure and reports it. Do not confuse the two overloads:
> `action.execute()` needs a `throws Throwable` (or `catch (Throwable)`) at the call site,
> `action.execute(failure -> log(failure))` does not.

`ThrowableBiFunction` has no instance `execute`; it only offers the static form, whose default handler
(`DEFAULT_EXCEPTION_HANDLER`) rethrows as `RuntimeException` with a message naming both arguments.
`ThrowableBiConsumer` has no `execute` at all.

```java
// Checked I/O inside a stream, without try/catch in the lambda body
ThrowableFunction<String, String> read = path -> IOUtils.toString(new FileInputStream(path));
ThrowableFunction<String, String> readAndTrim = read.andThen(StringUtils::trimWhitespace);

String content = readAndTrim.execute("/etc/hosts", (path, failure) -> {
    logger.warn("Cannot read {}: {}", path, failure.getMessage());
    return "";
});

ThrowableAction closeQuietly = connection::close;
closeQuietly.execute(failure -> logger.error("Close failed", failure));

ThrowableSupplier<Properties> loader = () -> PropertiesUtils.loadProperties("a=1", "b=2");
Properties defaults = loader.execute(failure -> new Properties());
```

---

## 6. `Predicates` and `Streams`

`io.microsphere.lang.function.Predicates` and `Streams` are plain `public interface` types that expose only static
methods — they are not `Utils` subclasses, and you never implement or instantiate them.

```java
// Predicates
public static final Predicate[] EMPTY_PREDICATE_ARRAY;
public static <T> Predicate<T>[] emptyArray()                 // the shared array above
public static <T> Predicate<T> alwaysTrue()                   // a new lambda per call
public static <T> Predicate<T> alwaysFalse()
public static <T> Predicate<? super T> and(Predicate<? super T>... predicates)
public static <T> Predicate<? super T> or(Predicate<? super T>... predicates)

// Streams
public static <T> Stream<T> stream(T... values)
public static <T> Stream<T> stream(Iterable<T> iterable)
public static <T> Stream<T> filterStream(T[] values, Predicate<? super T> predicate)
public static <T, S extends Iterable<T>> Stream<T> filterStream(S values, Predicate<? super T> predicate)
public static <T> List<T> filterList(T[] values, Predicate<? super T> predicate)
public static <T, S extends Iterable<T>> List<T> filterList(S values, Predicate<? super T> predicate)
public static <T> Set<T> filterSet(T[] values, Predicate<? super T> predicate)
public static <T, S extends Iterable<T>> Set<T> filterSet(S values, Predicate<? super T> predicate)
public static <T, S extends Iterable<T>> S filter(S values, Predicate<? super T> predicate)
public static <T, S extends Iterable<T>> S filterAll(S values, Predicate<? super T>... predicates)
public static <T, S extends Iterable<T>> S filterAny(S values, Predicate<? super T>... predicates)
public static <T> List<T> filterAllList(T[] values, Predicate<? super T>... predicates)
public static <T> Set<T> filterAllSet(T[] values, Predicate<? super T>... predicates)
public static <T> List<T> filterAnyList(T[] values, Predicate<? super T>... predicates)
public static <T> Set<T> filterAnySet(T[] values, Predicate<? super T>... predicates)
public static <T> T filterFirst(Iterable<T> values, Predicate<? super T>... predicates)
```

`filter` / `filterAll` / `filterAny` keep the source's shape: a `List` in gives a `List` out, a `Set` in gives a `Set`
out, so you skip the `collect(toList())` boilerplate. `filterAll*` means "matches every predicate";
`filterAny*` means "matches at least one"; `filterFirst` combines the predicates with `Predicates.and` and returns
`null` when nothing matches.

```java
// One predicate per call; combine predicates with Predicates.and / Predicates.or
List<Class<?>> concrete = Streams.filterList(scanned, type -> !Modifier.isAbstract(type.getModifiers()));
Set<Class<?>> exported = Streams.filterSet(scanned, type -> Modifier.isPublic(type.getModifiers()));
Class<?> firstConcretePublic = Streams.filterFirst(scanned,
        type -> Modifier.isPublic(type.getModifiers()),
        type -> !Modifier.isAbstract(type.getModifiers()));   // filterFirst combines all predicates with AND
```

> [!WARNING]
> `and()` and `or()` both return `alwaysTrue()` for an empty predicate array, and both return the single argument
> unchanged for one predicate. So `Predicates.or()` with no predicates is *always true*, not always false — check the
> array length yourself when "no criteria" must mean "no match".

---

## 7. Lambdas and method handles

### `LambdaUtils` (`io.microsphere.lang.invoke`)

```java
public abstract class LambdaUtils implements Utils {
    public static List<Class<?>> resolveLambdaMethodParameterTypes(Object lambda)
    public static List<Class<?>> resolveLambdaMethodParameterTypes(Class<?> lambdaClass)
    public static List<Class<?>> resolveLambdaMethodParameterTypes(Object lambda, Class<?> functionalInterface)
    public static List<Class<?>> resolveLambdaMethodParameterTypes(Class<?> lambdaClass, Class<?> functionalInterface)
}
```

Recovers the erased parameter types of the lambda's actual method — handy when a lambda is your configuration DSL
and you need to know what it consumes. The declared parameters only carry `Object` for generic functional interfaces;
the resolved list carries the real types.

```java
ToLongBiFunction<String, Long> counter = (name, total) -> total;
List<Class<?>> types = LambdaUtils.resolveLambdaMethodParameterTypes(counter, ToLongBiFunction.class);
// [String, Long] — the erased parameter types of the lambda's own method
```

The methods are annotated `@Nullable` but in practice return an **empty** `List` when the input is `null`, is not a
lambda class, when the lambda type implements more than one functional interface, or when the given functional
interface is not assignable from the lambda class. Non-empty results depend on reading the class's constant pool,
i.e. JDK internals; on JDK 16+ the required `--add-opens` entries are already configured by the build — see
[Getting Started](getting-started.md#61-jdk-16-specifics).

### `MethodHandlesLookupUtils` and `MethodHandleUtils` (`io.microsphere.invoke`)

```java
public abstract class MethodHandlesLookupUtils implements Utils {
    public static final MethodHandle NOT_FOUND_METHOD_HANDLE;   // null
    public static final Lookup PUBLIC_LOOKUP;                   // MethodHandles.publicLookup()
    public static MethodHandle findPublicVirtual(Class<?> type, String name, Class<?>... parameterTypes)
    public static MethodHandle findPublicStatic(Class<?> type, String name, Class<?>... parameterTypes)
}

public abstract class MethodHandleUtils implements Utils {
    public static final int MODULE;         // access-mode bit flags
    public static final int UNCONDITIONAL;
    public static final int ORIGINAL;
    public static final int ALL_MODES;
    public static final Lookup PUBLIC_LOOKUP;

    public static Lookup lookup(Class<?> type)                       // ALL_MODES by default
    public static Lookup lookup(Class<?> type, LookupMode... modes)  // cached per (type, modes)
    public static MethodHandle findVirtual(Class<?> type, String name, Class<?>... parameterTypes)
    public static MethodHandle findStatic(Class<?> type, String name, Class<?>... parameterTypes)
    public static void handleInvokeExactFailure(Throwable failure, MethodHandle handle, Object... args)

    public enum LookupMode {                          // constants
        PUBLIC, PRIVATE, PROTECTED, PACKAGE, MODULE, UNCONDITIONAL, ORIGINAL, ALL, TRUSTED;
        public static int getModes(LookupMode... modes)
    }
}
```

Choose between them by how much access you need and how you want failure reported:

| Call | On failure | Use when |
|---|---|---|
| `MethodHandlesLookupUtils.findPublicVirtual / findPublicStatic` | returns `null` (compare with `NOT_FOUND_METHOD_HANDLE`), logged at trace | only public members are acceptable and absence is normal |
| `MethodHandleUtils.findVirtual / findStatic` | returns `null` when the method is not found; otherwise picks a public lookup or a privileged `Lookup` | you may need a private/protected handle |
| `MethodHandleUtils.lookup(type, modes)` | cached `Lookup`, constructed reflectively through a `MethodHandles.Lookup` constructor | you call `Lookup` APIs yourself |
| `MethodHandleUtils.handleInvokeExactFailure(...)` | logs a `warn` with the handle and the arguments, returns `void` | in the `catch (Throwable)` around `invokeExact` |

```java
MethodHandle handle = MethodHandleUtils.findStatic(StringUtils.class, "trimWhitespace", String.class);
if (handle != null) {
    try {
        String trimmed = (String) handle.invokeExact("  x  ");   // static handle: (String)String
    } catch (Throwable failure) {
        MethodHandleUtils.handleInvokeExactFailure(failure, handle, "  x  ");
    }
}
```

> [!IMPORTANT]
> `invokeExact` is signature-strict: the call site's static types (receiver, arguments, and the assignment target of
> the result) must match the handle's `MethodType` exactly, otherwise you get `WrongMethodTypeException`. Give the
> result a precise target type, or normalize the handle first with `asType(...)` / `invokeWithArguments(...)`.
> Prefer `MethodHandleUtils` / `MethodHandlesLookupUtils` over raw `MethodHandles.lookup()` when you want code that
> degrades gracefully across JDK 8 → 25 rather than throwing on the first restricted access.

### `UnsafeUtils` (`io.microsphere.misc`)

Exposes `sun.misc.Unsafe` array layout constants — `LONG_ARRAY_BASE_OFFSET`, `INT_ARRAY_BASE_OFFSET`,
`OBJECT_ARRAY_BASE_OFFSET`, `LONG_ARRAY_INDEX_SCALE`, `OBJECT_ARRAY_INDEX_SCALE`, and the matching offsets/scales for
`short`, `byte`, `boolean`, `double`, `float`, `char` — plus typed readers such as
`getLongVolatileFromArray(Object object, String fieldName, int index)` and `putDouble(Object, String, double)`.
This is for collection and high-performance internals; treat it as an implementation detail rather than a stable API.

---

## See also

* [Collections and Filters](collections-and-filters.md) — the `Delegating*` collections that implement `DelegatingWrapper`
* [Reflection and Types](reflection-and-types.md) — `ClassDefinition` / `MethodDefinition` consume `Deprecation`
* [Type Conversion](type-conversion.md) — `AbstractConverter` derives a `Prioritized` value from type depth
* [Events](events.md) — `EventListener` ordering through `Prioritized.COMPARATOR`

[← Handbook index](../README.md) · [Previous: Collections and Filters](collections-and-filters.md) · [Next: Reflection and Types →](reflection-and-types.md)
