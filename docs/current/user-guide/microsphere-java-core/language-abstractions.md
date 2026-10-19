# Language Abstractions

Small, cross-cutting vocabulary types that the rest of `microsphere-java` builds on: ordering (`Prioritized`),
layered objects (`Wrapper`), deprecation metadata (`Deprecation`), throwable-aware functional interfaces, and
method-handle / lambda introspection helpers.

| Item | Value |
|------|-------|
| Artifact | `io.github.microsphere-projects:microsphere-java-core` |
| Version | `0.3.20-SNAPSHOT` (`release` branch) |
| Packages | `io.microsphere.lang` (7 types), `io.microsphere.lang.function` (8), `io.microsphere.lang.invoke` (1), `io.microsphere.invoke` (2) |
| Types total | 18 |
| Java level | Language/API baseline **Java 8**; sources compile and tests run on JDK 8 / 11 / 17 / 21 / 25 |
| Compile dependency | `microsphere-java-annotations` only (plus JDK) |

---

## 1. What problem each abstraction solves

| Type | Problem it solves |
|------|-------------------|
| `Prioritized` | "Sort me, but I may not be comparable." Gives any object an integer priority plus a `Comparator` that tolerates non-prioritized and `null` elements, so heterogeneous collections can be ordered without `ClassCastException`. |
| `Wrapper` | "I have a proxy/decorator in front of the real object." A contract to reach the underlying implementation (the `java.sql.Wrapper` idea, generalised to any object graph). |
| `DelegatingWrapper` | Removes the boilerplate: implement one method (`getDelegate()`) and get `unwrap`/`isWrapperFor` for free. |
| `WrapperProcessor` | A typed hook (`<W extends Wrapper>`) so a framework can post-process wrapped beans without casting. |
| `Deprecation` | `@Deprecated` carries no data. `Deprecation` is a serialisable, immutable record of *since which version*, *what replaces it*, *why*, *link*, *level* — used by the library's reflective metadata model. |
| `ClassDataRepository` | A cached, eagerly-built index of the classpath: class name → class path, package → class names. Pay attention to its first-touch cost. |
| `MutableInteger` | A mutable `int` box so lambdas and helper methods can mutate a counter (Java has no pass-by-reference). |
| `Throwable*` interfaces | JDK functional interfaces cannot throw checked exceptions. These variants let you pass `Files::readString`-style calls into `map`/`forEach` and decide *later* how to handle failure. |
| `Predicates`, `Streams` | Null-safe, array/`Iterable`-friendly filtering that the JDK `Stream` API does not offer directly. |
| `MethodHandleUtils`, `MethodHandlesLookupUtils` | `Lookup.findVirtual` only sees what the caller may access. These build (and cache) a `Lookup` with wide access modes so private/protected/package methods become invokable, and degrade gracefully when the JDK refuses. |
| `LambdaUtils` | A compiled lambda erases its generic parameter types. This recovers the actual parameter types of the SAM at runtime. |

---

## 2. Version information and compatibility range

`@Since` values below are copied from the Javadoc of the source files themselves. Where a source states no version,
this page says so instead of guessing.

| Type | `@since` in source | Notes |
|------|--------------------|-------|
| `io.microsphere.lang.Prioritized` | `1.0.0` | Documented in the class Javadoc (`Prioritized.java:54`). |
| `io.microsphere.lang.Wrapper` | **not stated** | The interface Javadoc has `@see` tags but no `@since` and no `@author`. |
| `io.microsphere.lang.DelegatingWrapper` | `1.0.0` | |
| `io.microsphere.lang.WrapperProcessor` | `1.0.0` | |
| `io.microsphere.lang.Deprecation` | `1.0.0` | Author `Mercy`. |
| `io.microsphere.lang.MutableInteger` | `1.0.0` | |
| `io.microsphere.lang.ClassDataRepository` | `1.0.0` | |
| `io.microsphere.lang.function.ThrowableFunction` | `1.0.0` | |
| `io.microsphere.lang.function.ThrowableConsumer` | `1.0.0` | |
| `io.microsphere.lang.function.ThrowableSupplier` | `1.0.0` | |
| `io.microsphere.lang.function.ThrowableAction` | `1.0.0` | |
| `io.microsphere.lang.function.ThrowableBiConsumer` | `1.0.0` | |
| `io.microsphere.lang.function.ThrowableBiFunction` | **not stated** | No `@since` on the interface Javadoc. |
| `io.microsphere.lang.function.Predicates` | `1.0.0` | |
| `io.microsphere.lang.function.Streams` | `1.0.0` | |
| `io.microsphere.lang.invoke.LambdaUtils` | `1.0.0` | |
| `io.microsphere.invoke.MethodHandleUtils` | `1.0.0` | Class-level `@since 1.0.0`; some *members* carry JDK-level stamps (`@since 9` for `MODULE` / `UNCONDITIONAL`, `@since 16` for `ORIGINAL`). |
| `io.microsphere.invoke.MethodHandlesLookupUtils` | `1.0.0` | |

### Compatibility range

- **Language level: Java 8.** All throwable-aware interfaces use only `@FunctionalInterface`, `default` and `static`
  interface methods; `LambdaUtils` and the `invoke` package use only `java.lang.invoke` APIs available since Java 7/8
  (`MethodHandles`, `MethodType`, `LambdaMetafactory`). Nothing in these packages requires Java 9+ language features.
- **JDK internals touched.** `MethodHandleUtils` reflectively constructs `java.lang.invoke.MethodHandles.Lookup` via
  three private constructors (`MethodHandleUtils.java:157-167`):
  | Constructor | Availability | Field |
  |-------------|--------------|-------|
  | `Lookup(Class)` | since JDK 7 | `lookupConstructor1` — declared but **not used** by `newLookup` |
  | `Lookup(Class, int)` | since JDK 7 | `lookupConstructor2` — fallback path |
  | `Lookup(Class, Class, int)` | since JDK 14 | `lookupConstructor3` — preferred when present |
  `Lookup` lives in `java.lang.invoke`, exported by `java.base`, so **no `--add-opens` is required**; the constructor
  itself is private, and the library uses its own `newInstance(true, …)` (access-forcing) helper.
- **Access-mode constants** (`MODULE` `0x10`, `UNCONDITIONAL` `0x20`, `ORIGINAL` `0x40`) are computed locally by
  bit-shifting `Lookup.PACKAGE`, so they compile on Java 8 where `Lookup.MODULE`/`UNCONDITIONAL`/`ORIGINAL` do not
  exist as public fields. `LookupMode.TRUSTED(-1)` reproduces the JDK's retired `TRUSTED` bit numerically.
- **`LambdaUtils` requires the class constant pool.** It reads `sun.reflect.ConstantPool` (JDK ≤ 8 name) or
  `jdk.internal.reflect.ConstantPool` (JDK 9+ name) through the package-private `Class.getConstantPool()`
  (`ConstantPoolUtils.java:51-62, 251-259`). Everything there is wrapped in `try/catch (Throwable)`, so on any JDK or
  launcher configuration that forbids it, resolution simply returns an empty list rather than failing.
- **Deprecation status:** none of the 18 types is `@Deprecated`. The `Deprecation` class is a *description* of
  deprecation, not a deprecation itself.


### 2.1 Release date and what changed in `0.3.20`

| Item | Value | Evidence |
|---|---|---|
| Documented line | `release`, `${revision}` = `0.3.20-SNAPSHOT` | root `pom.xml:54` |
| Newest published release | `0.3.20`, tagged **2026-10-16** | the `0.3.20` tag resolves to `chore: merge main into release [skip ci]`, committed `2026-10-16T03:43:20Z` |
| Previous release | `0.3.19`, tagged **2026-08-30** | the `0.3.19` tag, committed `2026-08-30T12:51:37Z` |
| Changelog source | `release-notes.md` at the repository root | The project publishes no `CHANGELOG.md`; the `v0.3.16`–`v0.3.20` blocks of `release-notes.md` read *Release notes generation failed* and carry raw commit lists, so every entry below is quoted as a commit subject and checked against the `release` sources. |

`0.3.20` adds no new API surface for this area — the `0.3.19...0.3.20` range is a hardening release. What it does change, and how far that reaches the sources this page documents:

* **Nullability contracts.** 5 of 18 main sources in the scope of this page declare `@Nonnull` / `@Nullable`. Subjects in the range that name this area: `e1e83e85 Add nullability annotations to lang APIs` / `e0b17365 Add nullability annotations to function APIs` / `01918859 Annotate LambdaUtils return values` / `2cae6dba Refine nullability in invoke utilities`.
* **Null-input and behaviour fixes.** `0bc33ddc Annotate nullability in lang utilities`.

> [!NOTE]
> Reading the commit list: `0bc33ddc` says *lang utilities*, but the annotated sources in this page's scope are 5 of 18 (`ClassDataRepository`, `Deprecation`, `LambdaUtils`, and the `Predicates`/`Streams` helpers in `io.microsphere.lang.function`); the remaining part of that subject lands on `io.microsphere.util`, which is counted on the utilities page.

---

## 3. API reference

### 3.1 `io.microsphere.lang.Prioritized`

```java
public interface Prioritized extends Comparable<Prioritized> {

    Comparator<Object> COMPARATOR = (one, two) -> { /* see 3.1.1 */ };

    int MAX_PRIORITY   = MIN_VALUE;   // Integer.MIN_VALUE
    int MIN_PRIORITY   = MAX_VALUE;   // Integer.MAX_VALUE
    int NORMAL_PRIORITY = 0;

    default int getPriority() { return NORMAL_PRIORITY; }

    @Override
    default int compareTo(Prioritized that) { return compare(this.getPriority(), that.getPriority()); }
}
```

Note the deliberate inversion: `MAX_PRIORITY` *is* `Integer.MIN_VALUE`, because ordering is ascending — "highest
priority" means "sorted first", hence the smallest int.

#### 3.1.1 `COMPARATOR` behaviour (verified against tests)

`COMPARATOR` is a `Comparator<Object>`, not `Comparator<Prioritized>` — it accepts anything, including `null`.
From `microsphere-java-core/src/test/java/io/microsphere/lang/PrioritizedTest.java`:

```java
assertEquals(1, COMPARATOR.compare(minPriority, maxPriority)); // MIN_PRIORITY value wins over MAX_PRIORITY value
assertEquals(1, COMPARATOR.compare(null, prioritized));        // non-Prioritized sorts last
assertEquals(-1, COMPARATOR.compare(prioritized, null));
assertEquals(0, COMPARATOR.compare(null, "prioritized"));      // neither side is Prioritized
assertEquals(0, COMPARATOR.compare("prioritized", "prioritized"));
```

| `one` | `two` | result |
|-------|-------|--------|
| `Prioritized` | `Prioritized` | `Integer.compare(one.priority, two.priority)` |
| `Prioritized` | anything else (incl. `null`) | `-1` |
| anything else | `Prioritized` | `1` |
| neither | neither | `0` |

### 3.2 `io.microsphere.lang.Wrapper`

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
        return null;   // NOT the original object — see Pitfalls §5.1
    }
}
```

`isWrapperFor` is specified as the cheap predicate: *"If this method returns true then calling `unwrap` with the same
argument should succeed."*

### 3.3 `io.microsphere.lang.DelegatingWrapper`

```java
public interface DelegatingWrapper extends Wrapper {

    Object getDelegate();

    @Override
    default <T> T unwrap(Class<T> type) throws IllegalArgumentException {
        if (getClass().equals(type)) {
            return (T) this;                     // unwrap to myself
        } else {
            if (isWrapperFor(type)) {
                return (T) getDelegate();        // unwrap one level
            }
        }
        throw new IllegalArgumentException(getClass().getName() + " can't unwrap the given type '" +
                type.getName() + QUOTE);
    }

    @Override
    default boolean isWrapperFor(Class<?> type) {
        Object delegate = getDelegate();
        return type.isInstance(delegate);
    }
}
```

Usage (`microsphere-java-core/src/test/java/io/microsphere/lang/DelegatingWrapperTest.java`, helper class
`DelegatingWrapperImpl.java`):

```java
String delegate = "Hello";
DelegatingWrapper wrapper = new DelegatingWrapperImpl(delegate);
assertTrue(wrapper.isWrapperFor(String.class));
assertTrue(wrapper.isWrapperFor(CharSequence.class));  // supertype check, via Class#isInstance
assertEquals(delegate, wrapper.unwrap(String.class));
assertEquals(wrapper, wrapper.unwrap(DelegatingWrapperImpl.class));
assertThrows(IllegalArgumentException.class, () -> wrapper.unwrap(Integer.class));
```

### 3.4 `io.microsphere.lang.WrapperProcessor`

```java
public interface WrapperProcessor<W extends Wrapper> {
    W process(W wrapper);   // may return the same instance or a further-wrapped one
}
```

### 3.5 `io.microsphere.lang.Deprecation`

`@Immutable public final class Deprecation implements Serializable` with
`serialVersionUID = 6380988834632455932L`.

| Member | Signature |
|--------|-----------|
| fields | `@Nullable Version since`, `@Nullable String replacement`, `@Nullable String reason`, `@Nullable String link`, `@Nonnull Level level` |
| getters | `@Nullable Version getSince()`, `@Nullable String getReplacement()`, `@Nullable String getReason()`, `@Nullable String getLink()`, `@Nonnull Level getLevel()` |
| nested enum | `public enum Level { DEFAULT, REMOVAL }` |
| nested builder | `public static class Builder` with `protected Builder()`, `since(String)`, `since(@Nullable Version)`, `replacement(...)`, `reason(...)`, `link(...)`, `level(@Nullable Level)`, `build()` |
| factories | `static Deprecation.Builder builder()`, `static Deprecation of(String since)`, `of(String, String)`, `of(String, String, String)`, `of(String, String, String, String)`, `of(String since, String replacement, String reason, String link, Level level)` |
| constructors | package-private: `Deprecation(Deprecation source)` (copy) and `Deprecation(@Nullable Version, @Nullable String, @Nullable String, @Nullable String, @Nonnull Level)` |

Both constructors are package-private, so instances come only from `of(...)` or `builder()`. The varargs-style `of`
overloads chain, defaulting the missing tail to `null` and the level to `Level.DEFAULT`.
`Builder.level(null)` is **not** an error — it degrades to `DEFAULT` via `defaultIfNull`. `Builder.since(String)`
converts through `Version.of(since)`. `equals`/`hashCode` cover all five fields (`level` compared with `==`), and
`equals` short-circuits on reference identity.

The 5-argument `of` is **not** varargs — it is five discrete positional parameters, so argument order is silently
swappable (`of("1.0", null, null, null, Level.DEFAULT)`).

### 3.6 `io.microsphere.lang.MutableInteger`

`public class MutableInteger extends Number` holding a plain `private int value`.
API: `MutableInteger(int)`, `static MutableInteger of(int)`, `final int get()`, `final MutableInteger set(int)`
(fluent), `final int getAndSet(int)`, `final int getAndIncrement()`, `final int getAndDecrement()`,
`final int getAndAdd(int)`, `final int incrementAndGet()`, `final int decrementAndGet()`, `int addAndGet(int)`,
`intValue`/`longValue`/`floatValue`/`doubleValue`, `toString`, `equals`, `hashCode`.

The names mirror `AtomicInteger` but the fields are **not** volatile and there is **no** atomicity. See §5.4.

### 3.7 `io.microsphere.lang.ClassDataRepository`

`@Immutable public class ClassDataRepository` with a single public entry point,
`public static final ClassDataRepository INSTANCE`.
Public read API (all `@Nonnull @Immutable` unless noted):
`getAllPackageNamesInClassPaths()`, `getClassNamesInPackage(Package)`, `getClassNamesInPackage(String)`,
`getClassNamesInClassPath(String classPath, boolean recursive)`, `getClassPathToClassNamesMap()`,
`getAllClassNamesInClassPaths()`; plus `@Nullable String findClassPath(Class<?>)` and
`@Nullable String findClassPath(String className)`.
There is **no** public `getAllClassPaths()` — the class-path set is only observable through
`getClassPathToClassNamesMap()`'s keys.

The three backing maps are built **eagerly** in instance field initializers
(`ClassDataRepository.java:87-91`: `classPathToClassNamesMap`, `classNameToClassPathsMap`,
`packageNameToClassNamesMap`), so touching `ClassDataRepository.INSTANCE` for the first time performs a full
classpath scan (bootstrap class paths merged with the system ones, `:200-201`) before the first method returns.
Results include JDK classes, and the returned sets are `unmodifiableSet` views (`:101`, `:165`).

### 3.8 Throwable-aware functional interfaces (`io.microsphere.lang.function`)

All six are `@FunctionalInterface` and share one shape: a `throws Throwable` abstract method, an `execute(...)`
that never propagates the checked failure (it routes to `handleException`/`ExceptionHandler`), and a `static execute(...)`
convenience.

| Interface | Abstract method | Safe invocation | Failure routing |
|-----------|-----------------|-----------------|-----------------|
| `ThrowableFunction<T, R>` | `R apply(T t) throws Throwable` | `default R execute(T t) throws RuntimeException`<br>`default R execute(T t, BiFunction<T, Throwable, R> exceptionHandler)` | `default R handleException(T t, Throwable failure) { throw new RuntimeException(failure); }` |
| `ThrowableConsumer<T>` | `void accept(T t) throws Throwable` | `default void execute(T t)`<br>`default void execute(T t, BiConsumer<T, Throwable> exceptionHandler) throws NullPointerException` | `default void handleException(T t, Throwable failure)` |
| `ThrowableSupplier<T>` | `T get() throws Throwable` | `default T execute()`<br>`default T execute(Function<Throwable, T> exceptionHandler)` | `default T handleException(Throwable failure)` |
| `ThrowableAction` | `void execute() throws Throwable` | `default void execute(Consumer<Throwable> exceptionHandler)` | `default void handleException(Throwable failure)` |
| `ThrowableBiFunction<T, U, R>` | `R apply(T first, U second) throws Throwable` | `static R execute(T, U, ThrowableBiFunction<T,U,R>) throws NullPointerException`<br>`static R execute(T, U, ThrowableBiFunction<T,U,R>, ExceptionHandler<T,U,R>)` | nested `interface ExceptionHandler<T, U, R> { R handle(T first, U second, Throwable failure); }` + `ExceptionHandler DEFAULT_EXCEPTION_HANDLER` |
| `ThrowableBiConsumer<T, U>` | `void accept(T t, U u) throws Throwable` | `default ThrowableBiConsumer<T, U> andThen(ThrowableBiConsumer<? super T, ? super U> after) throws Throwable` | — |

`ThrowableFunction` additionally offers `static <T,R> R execute(T t, ThrowableFunction<T,R> function) throws
IllegalArgumentException`, `static execute(T, function, BiFunction handler)`, and composition:

```java
default <V> ThrowableFunction<V, R> compose(ThrowableFunction<? super V, ? extends T> before);
default <V> ThrowableFunction<T, V> andThen(ThrowableFunction<? super R, ? extends V> after);
```

Both reject `null` with `assertNotNull` and rethrow the underlying `Throwable` to the caller of the composed function
(no silent wrapping).

`ThrowableBiFunction` is the one the `invoke` package actually uses: its `DEFAULT_EXCEPTION_HANDLER` builds the message
`"It's failed to execute the function with arguments[{}, {}] is failed"` and throws `RuntimeException`. It is the only
one of the six whose *default* failure strategy is a static field rather than an overridable `handleException` method.

Example (`microsphere-java-core/src/test/java/io/microsphere/lang/function/ThrowableFunctionTest.java`):

```java
ThrowableFunction<String, Integer> parser = Integer::valueOf;
Integer ok  = parser.execute("123");                 // 123
Integer bad = parser.execute("invalid", (input, ex) -> -1);  // -1, no exception escapes
```

### 3.9 `Predicates` and `Streams`

```java
public interface Predicates {                                     // @since 1.0.0
    static <T> Predicate<T>[] emptyArray();                       // Predicates.java:65
    static <T> Predicate<T> alwaysTrue();                         // :87
    static <T> Predicate<T> alwaysFalse();                        // :109
    static <T> Predicate<? super T> and(Predicate<? super T>... predicates);  // :138
    static <T> Predicate<? super T> or(Predicate<? super T>... predicates);   // :179
}
```

`Streams` is a static utility *interface* (never implemented) with `@since 1.0.0`:
`stream(T... values)`, `stream(Iterable<T>)`; `filterStream`/`filterList`/`filterSet`/`filter` each in a `T[]` and an
`Iterable` form; multi-predicate `filterAll(...)`, `filterAllList(T[], Predicate...)`, `filterAllSet(T[], Predicate...)`,
`filterAny(...)`, `filterAnyList`, `filterAnySet`; and `filterFirst(Iterable<T>, Predicate...)`.
`filterAll`/`filterAny` take `Predicate<? super T>...` varargs, so passing zero predicates is legal and the result is
the unfiltered (or empty) view — check the behaviour you need before relying on it.

### 3.10 `io.microsphere.lang.invoke.LambdaUtils`

`public abstract class LambdaUtils implements Utils` with a `private` constructor (not instantiable).

```java
public static List<Class<?>> resolveLambdaMethodParameterTypes(@Nullable Object object);
public static List<Class<?>> resolveLambdaMethodParameterTypes(@Nullable Class<?> type);
public static List<Class<?>> resolveLambdaMethodParameterTypes(@Nullable Object object, @Nullable Class<?> functionalInterface);
public static List<Class<?>> resolveLambdaMethodParameterTypes(@Nullable Class<?> type, @Nullable Class<?> functionalInterface);

static Class<?>[] resolveParameterTypes(Method actualMethod, Class<?>... declaredParameterTypes); // package-private
```

Guarantee: never returns `null`, never throws — it returns `Collections.emptyList()` for every rejection path.

Guards, in source order (`LambdaUtils.java:98-129`): `isLambdaClass(type)`; `findFunctionalInterfaceMethod(...) != null`;
`functionalInterface.isAssignableFrom(type)`; then a reverse scan of the constant pool.
The two-argument overload without an explicit interface additionally requires
`findAllInterfaces(type, ClassUtils::isFunctionalInterface).size() == 1`.

Worked examples — all from `microsphere-java-core/src/test/java/io/microsphere/lang/invoke/LambdaUtilsTest.java`:

```java
// (1) parameter type recovered from a compiled lambda
EventListener<EchoEvent> e = event -> { };
List<Class<?>> ts = resolveLambdaMethodParameterTypes(e, EventListener.class);
assertEquals(1, ts.size());
assertEquals(EchoEvent.class, ts.get(0));

// (2) same lambda WITHOUT the explicit interface -> empty, because the single-functional-interface rule fails
assertTrue(resolveLambdaMethodParameterTypes(e).isEmpty());

// (3) primitive SAM parameters survive as primitives
LongFunction<Long> lf = v -> v;
assertEquals(long.class, resolveLambdaMethodParameterTypes(lf).get(0));

// (4) a method reference captured through LambdaMetafactory resolves too
Function<String, String> upperLambda = toUpperLambda();   // built with LambdaMetafactory + findVirtual(String.toUpperCase)
assertEquals(String.class, resolveLambdaMethodParameterTypes(upperLambda, Function.class).get(0));

// (5) wrong functional interface -> empty (no exception)
assertTrue(resolveLambdaMethodParameterTypes(volumeLambda, Function.class).isEmpty());
assertTrue(resolveLambdaMethodParameterTypes(BiFunction.class, String.class).isEmpty());
```

`resolveParameterTypes` is testable directly and signals mismatch by returning the identity `EMPTY_CLASS_ARRAY`
(the test asserts with `assertSame`, so do not compare it with `assertEquals` on a copy):

```java
Method method = findMethod(String.class, "valueOf", int.class);
assertSame(EMPTY_CLASS_ARRAY, resolveParameterTypes(method, Long.class));
```

### 3.11 `io.microsphere.invoke.MethodHandlesLookupUtils`

`public abstract class MethodHandlesLookupUtils implements Utils`, private constructor.

```java
public static final MethodHandle NOT_FOUND_METHOD_HANDLE = null;   // MethodHandlesLookupUtils.java:67
public static final Lookup PUBLIC_LOOKUP = publicLookup();

public static MethodHandle findPublicVirtual(Class<?> requestedClass, String methodName, Class... parameterTypes);
public static MethodHandle findPublicStatic(Class<?> requestedClass, String methodName, Class... parameterTypes);

protected static MethodHandle findPublic(Class<?> requestedClass, String methodName, Class[] parameterTypes,
                                         ThrowableBiFunction<Lookup, MethodType, MethodHandle> function);
protected static MethodHandle find(Lookup lookup, Class<?> requestedClass, String methodName, Class[] parameterTypes,
                                   ThrowableBiFunction<Lookup, MethodType, MethodHandle> function);
protected static MethodHandle findPublic(Method method, ThrowableBiFunction<Lookup, MethodType, MethodHandle> function);
protected static MethodHandle find(Lookup lookup, Method method,
                                   ThrowableBiFunction<Lookup, MethodType, MethodHandle> function);
```

`parameterTypes` is a **raw** `Class...` / `Class[]` (unparameterised) in every signature — a warning source if you
pass `Class<?>[]`.
Return contract: `null` when the method is not found *or* when the lookup throws; on a lookup failure it logs at
`trace` (`"The MethodHandle can't be found by Lookup[{}] on the method : {}"`) and returns `null`.

### 3.12 `io.microsphere.invoke.MethodHandleUtils`

`public abstract class MethodHandleUtils implements Utils`, private constructor.

```java
public static final int MODULE        = PACKAGE << 1;   // 0x10, @since 9
public static final int UNCONDITIONAL = PACKAGE << 2;   // 0x20, @since 9
public static final int ORIGINAL      = PACKAGE << 3;   // 0x40, @since 16
public static final int ALL_MODES = (PUBLIC | PRIVATE | PROTECTED | PACKAGE | MODULE | UNCONDITIONAL | ORIGINAL);
public static final Lookup PUBLIC_LOOKUP = publicLookup();

public enum LookupMode {
    PUBLIC, PRIVATE, PROTECTED, PACKAGE, MODULE, UNCONDITIONAL, ORIGINAL, ALL, TRUSTED;
    public static int getModes(LookupMode... lookupModes);
}

public static Lookup lookup(Class<?> requestedClass);                        // = lookup(rc, ALL)
public static Lookup lookup(Class<?> requestedClass, LookupMode... lookupModes);
public static MethodHandle findVirtual(Class<?> requestedClass, String methodName, Class... parameterTypes);
public static MethodHandle findStatic(Class<?> requestedClass, String methodName, Class... parameterTypes);
public static void handleInvokeExactFailure(Throwable e, MethodHandle methodHandle, Object... args);

protected static MethodHandle find(Class<?> requestedClass, String methodName, Class[] parameterTypes,
                                   ThrowableBiFunction<Lookup, MethodType, MethodHandle> function);
```

`findVirtual` / `findStatic` differ from the `MethodHandlesLookupUtils` equivalents only in that they widen the lookup:
public, non-caller-sensitive methods still go through `PUBLIC_LOOKUP`, everything else (private / protected / package)
gets a synthesised full-mode `Lookup`.

Tests (`microsphere-java-core/src/test/java/io/microsphere/invoke/MethodHandleUtilsTest.java`) prove this, since all
four visibilities return a handle that actually invokes:

```java
assertSame(lookup, lookup2);                                        // cached: same Lookup instance for same key
assertEquals(methodName, (String) findVirtual(getClass(), methodName, String.class).invokeExact(this));
assertSame(NOT_FOUND_METHOD_HANDLE, findVirtual(MethodHandleUtilsTest.class, "notFound"));
assertDoesNotThrow(() -> handleInvokeExactFailure(new Throwable("testing"), methodHandle));
```

`testFindVirtualOnPublicMethod` / `…OnProtectedMethod` / `…OnDefaultMethod` / `…OnPrivateMethod` and the matching
`testFindStaticOn…` cases cover all visibilities; `MethodHandleUtilsBenchmark.java` in the same directory is a
throughput benchmark, not an assertion test.

---

## 4. How `Prioritized` orders listeners, converters and services elsewhere

`Prioritized` is the library's ordering contract, so it is inherited by SPI interfaces rather than re-declared:

| Subsystem | Contract | How priority is used |
|-----------|----------|----------------------|
| Events | `io.microsphere.event.EventListener<E extends Event> extends java.util.EventListener, Prioritized` | `AbstractEventDispatcher` / `DirectEventDispatcher` sort listeners so low-priority-value (highest-priority) listeners run first; `GenericEventListener` carries the value. |
| Conversion | `io.microsphere.convert.Converter<S, T> extends Prioritized`; `io.microsphere.convert.multiple.MultiValueConverter<S> extends Prioritized` | A higher-priority converter wins when several claim the same source/target pair — `Converter`'s own Javadoc example returns `Prioritized.MAX_PRIORITY`. |
| SPI loading | `io.microsphere.util.ServiceLoaderUtils` | `sort(serviceList, COMPARATOR)` (`ServiceLoaderUtils.java:890`) after `ServiceLoader` iteration, so `META-INF/services` order never decides precedence. |
| Shutdown hooks | `io.microsphere.util.ShutdownHookUtils` | `new PriorityBlockingQueue<Runnable>(SHUTDOWN_HOOK_CALLBACKS_CAPACITY, COMPARATOR)` (`:114`). |
| Logging | `io.microsphere.logging.LoggerFactory implements Prioritized` | Picks the strongest available logging backend. |
| Class loading | `io.microsphere.classloading.URLClassPathHandle`, `AbstractURLClassPathHandle`, `ArtifactResourceResolver` | Chooses the JDK-generation handler. |
| Process / metadata | `io.microsphere.process.ProcessIdResolver`, `io.microsphere.metadata.ConfigurationPropertyReader` / `Loader` / `Generator` | Chain-of-responsibility style ordering; `ClassicProcessIdResolver` documents `NORMAL_PRIORITY + 9`. |

Because `COMPARATOR` tolerates non-`Prioritized` elements, all of the above can sort mixed collections — for example a
`ServiceLoader` result where some providers implement `Prioritized` and others do not.

---

## 5. Pitfalls

### 5.1 `Wrapper.tryUnwrap` returns `null`, not the original object
Its Javadoc says *"@return unwrapped instance if possible, or the original object"*, but the implementation
(`Wrapper.java:113-121`) returns `null` when the object is not a `Wrapper`, when `isWrapperFor` is false, and for a
`null` input. `WrapperTest` pins the real behaviour:
`assertNull(tryUnwrap("not a wrapper", String.class))`, `assertNull(tryUnwrap(null, String.class))`.
Always treat the result as nullable.

### 5.2 `getPriority()`'s Javadoc contradicts its code
`Prioritized.java:94-100` documents *"the default is `#MIN_PRIORITY` minimum one"* while the body returns
`NORMAL_PRIORITY` (`0`). Trust the code — an anonymous `new Prioritized() { }` reports `0`, which is what
`PrioritizedTest.testDefaultMethods()` asserts.

### 5.3 `MAX_PRIORITY` / `MIN_PRIORITY` look swapped — they are not
`MAX_PRIORITY == Integer.MIN_VALUE` and `MIN_PRIORITY == Integer.MAX_VALUE`. Priority is an *ordering rank*, ascending.
Writing `getPriority() { return MAX_PRIORITY; }` means "run me first", and `Integer.compare` on the raw ints is what
`compareTo` does. Never compare priority with `>` / `<` in your own code; use `compareTo` or `COMPARATOR`.

### 5.4 `MutableInteger` is not atomic
It reads like `AtomicInteger` (`getAndIncrement`, `incrementAndGet`, `getAndAdd`) but stores a non-volatile `private int
value` with no synchronisation. It is for single-threaded lambda capture and local counters. Concurrent use loses
updates and may not be visible across threads.

### 5.5 `DelegatingWrapper.unwrap` is not transitive by itself
The default `unwrap` returns `getDelegate()` after a single `isInstance` check — it does **not** recurse. A
`DelegatingWrapper` wrapping another `DelegatingWrapper` only unwraps one level unless you override
`isWrapperFor`/`unwrap` to delegate further. Also, `unwrap` uses `getClass().equals(type)` (exact class identity), so
unwrapping to a *superclass or interface* of the wrapper itself throws `IllegalArgumentException`.

### 5.6 `ThrowableBiFunction` swallows into a `RuntimeException` you cannot override per-instance
Unlike `ThrowableFunction`/`ThrowableConsumer`/`ThrowableSupplier`/`ThrowableAction`, which expose an overridable
`handleException`, `ThrowableBiFunction`'s fallback is the static `DEFAULT_EXCEPTION_HANDLER` and its 3-arg `execute`
is a **static** method. There is no `execute(first, second)` instance form. `MethodHandlesLookupUtils` therefore has
to pass an explicit handler lambda to get graceful degradation.

### 5.7 `ThrowableBiConsumer.andThen` still declares `throws Throwable`
Composing does not neutralise the checked exception: `default ThrowableBiConsumer<T, U> andThen(...) throws Throwable`.
You still need `try/catch` at the call site.

### 5.8 `LambdaUtils` silently returns an empty list
`resolveLambdaMethodParameterTypes(x)` (no explicit interface) returns empty whenever the lambda's interface closure is
not exactly one functional interface. Lambdas implementing `io.microsphere.event.EventListener` hit this because the
closure contains more than one candidate — see example (2) in §3.10. Pass the functional interface explicitly.
Also: the result is `Collections.emptyList()`, so `assertEquals(0, …)` works but the instance is shared and immutable.

### 5.9 Method-handle results are nullable
Both `MethodHandleUtils.findVirtual/findStatic` and `MethodHandlesLookupUtils.findPublicVirtual/findPublicStatic`
return `null` (`NOT_FOUND_METHOD_HANDLE`) rather than throwing. Because the constant is literally `null`, `assertSame`
and `== null` are equivalent — but the failure *reason* is logged only at `trace`/`warn`, so enable trace on
`io.microsphere.invoke` when a handle mysteriously disappears.

### 5.10 Caller-sensitive methods are excluded from the public fast path
`MethodHandleUtils.isCandidateMethod` requires `isPublic(method) && !isCallerSensitiveMethod(method)`.
`Class.forName`, `Proxy.newProxyInstance` and friends are caller-sensitive, so they are routed through the synthesised
`Lookup`. If a JDK version forbids `Lookup` construction, the whole lookup degrades to `null` — see §5.11.

### 5.11 JDK-version dependence of `lookup(Class)`
`newLookup` prefers `Lookup(Class, Class, int)` (JDK 14+) and otherwise uses `Lookup(Class, int)`. If neither private
constructor is reachable (strict module/access enforcement, or a future JDK removing them), `newInstance(true, …)`
returns `null` and every non-public `findVirtual`/`findStatic` call in that JVM returns `null`. There is no exception,
so guard the result.

### 5.12 The `LookupMode` Javadoc references JDK members you cannot compile against on Java 8
`Lookup#TRUSTED` and `Lookup#ALL_MODES` are cited via `@see` only; the code recomputes the bit values locally.
Copying the enum name into `--add-opens`-style reasoning is a mistake — the library needs no `java.base` *package*
opening because `Lookup` is exported; only the private constructor is forced.

### 5.13 `ClassDataRepository` costs a full classpath scan on first touch — and never refreshes
Its three maps are built eagerly in the singleton's field initializers (`ClassDataRepository.java:87-91`), so the first
reference to `ClassDataRepository.INSTANCE` triggers a scan of every bootstrap and system class path before returning.
That makes it a start-up-latency and memory item, not a cheap accessor — do not touch `INSTANCE` from a static
initialiser you care about. It is also a process-wide cache with no documented refresh or eviction API, so in
container-reload or multi-classloader scenarios the cached view goes stale for the rest of the JVM's life.

---

## 6. See also

- [Utilities, constants and text](./util-and-constants.md) — `io.microsphere.util.Version` used by `Deprecation`
- [Reflection, types and beans](./reflection-and-beans.md) — `ConstantPoolUtils` behind `LambdaUtils`; `ExecutableDefinition` consuming `Deprecation`
- [Type conversion](./type-conversion.md) and [Event dispatching](./events.md) — the two biggest consumers of `Prioritized`
- [Class loading and artifacts](./classloading-and-artifacts.md) — `Prioritized` handlers selected by priority
- Chinese version: [中文用户指南](./language-abstractions.zh.md)
- Developer-facing deep dive: [Language Abstractions — Developer Guide](../../developer-guide/microsphere-java-core/language-abstractions.md)
