# Core Utilities

**Module**: `io.github.microsphere-projects:microsphere-java-core`
**Packages**: `io.microsphere.util`, `io.microsphere.util.jar`, `io.microsphere.constants`, `io.microsphere.text`

Almost every class here follows one shape:

```java
public abstract class XxxUtils implements io.microsphere.util.Utils {
    private XxxUtils() {}          // non-instantiable
    public static ... // everything is static
}
```

`io.microsphere.util.Utils` is an **empty marker interface** (`public interface Utils {}`) used to group and discover
these helpers. Call the methods statically; never instantiate.

> [!NOTE]
> `NumberUtils` exists but is an empty placeholder — it declares **no** public methods. Do not plan around it.
> `ThrowableUtils` has exactly one method (`getRootCause`).

---

## 1. `StringUtils`

`public abstract class StringUtils implements Utils`

Constants: `EMPTY` (`""`), `EMPTY_STRING`, `EMPTY_STRING_ARRAY`, `INDEX_NOT_FOUND` (`-1`).

```java
// Blank checks
public static boolean isBlank(String value)
public static boolean isNotBlank(String value)
public static boolean isNumeric(String str)
public static boolean containsWhitespace(String str)

// Containment and prefixes
public static boolean contains(String value, CharSequence part)
public static boolean startsWith(String value, String part)
public static boolean endsWith(String value, String part)

// Splitting and replacement
public static String[] split(String value, char delimiter)
public static String[] split(String value, String delimiter)
public static String replace(String text, String searchString, String replacement)
public static String replace(String text, String searchString, String replacement, int max)

// Substrings
public static String substringBetween(String str, String tag)
public static String substringBetween(String str, String open, String close)
public static String substringBefore(String str, String separator)
public static String substringAfter(String str, String separator)
public static String substringBeforeLast(String str, String separator)
public static String substringAfterLast(String str, String separator)

// Whitespace and case
public static String trimWhitespace(String str)        // both ends
public static String trimLeadingWhitespace(String str)
public static String trimTrailingWhitespace(String str)
public static String trimAllWhitespace(String str)     // every occurrence
public static String capitalize(String str)
public static String uncapitalize(String str)

// Conversion
public static String[] toStringArray(Collection<String> collection)
public static String arrayToString(Object[] values, String delimiter)
```

> [!WARNING]
> `StringUtils` has **no** `hasText`, `hasLength`, `join`, `concat`, `defaultIfBlank`, `defaultString` or
> `equalsIgnorecase`, and no `CharSequence` overloads — every parameter is `String`.
> If you need a `CharSequence` comparison helper, use `CharSequenceComparator` / `CharSequenceUtils`; if you need
> `{}` interpolation, use `io.microsphere.text.FormatUtils` ([§8](#8-formatutils)).

Null handling is consistent: `isBlank(null)` is `true`; `split(null, ',')` returns an **empty array** (`EMPTY_STRING_ARRAY`),
not `null`; `split("abc", ',')` (delimiter absent) returns `["abc"]`; `split(value, nullDelimiter)` returns `[value]`;
and the trim/case methods return `null` or `""` input as-is.

```java
StringUtils.isBlank("  ")                       // true
StringUtils.split("a,b,c", ',')                 // ["a","b","c"]
StringUtils.split(null, ',')                    // String[0]
StringUtils.trimWhitespace("  ab  ")            // "ab"   (both ends)
StringUtils.trimLeadingWhitespace("  ab  ")     // "ab  "
StringUtils.trimAllWhitespace(" a b ")          // "ab"
StringUtils.capitalize("microsphere")           // "Microsphere"
StringUtils.uncapitalize("Microsphere")         // "microsphere"
StringUtils.substringBefore("key=value", "=")   // "key"
```

---

## 2. `ClassUtils`

68 static methods. The ones you will reach for:

```java
// Kind tests
public static boolean isPrimitive(Class<?>)
public static boolean isWrapperType(Object obj | Class<?>)
public static boolean isArray(Object obj | Class<?>)
public static boolean isEnum(Object obj | Class<?>)
public static boolean isNumber(Object obj | Class<?>)
public static boolean isCharSequence(Object obj | Class<?>)
public static boolean isSimpleType(Object obj | Class<?>)
public static boolean isInterface(Class<?>)
public static boolean isAbstractClass(Class<?>)
public static boolean isFinal(Class<?>)
public static boolean isConcreteClass(Class<?>)
public static boolean isGeneralClass(Class<?>)
public static boolean isTopLevelClass(Class<?>)
public static boolean isSyntheticClass(Object obj | Class<?>)
public static boolean isLambdaClass(Object obj | Class<?>)
public static boolean isLambdaClassName(String)
public static boolean isFunctionalInterface(Class<?>)
public static boolean isClass(Object)
public static boolean isDerived(Class<?> sourceType, Class<?>... superTypes)
public static boolean isAssignableFrom(Class<?> superType, Class<?> targetType)
public static boolean arrayTypeEquals(Class<?> one, Class<?> two)

// Primitive / wrapper mapping
public static Class<?> resolvePrimitiveType(Class<?> type)
public static Class<?> resolveWrapperType(Class<?> primitiveType)
public static Class<?> tryResolveWrapperType(Class<?> type)
public static Class<?> resolvePrimitiveClassForName(String className)

// Names
public static String resolvePackageName(Class<?> type)
public static String resolvePackageName(String className)
public static String resolveClassName(String resourceName)     // "io/a/B.class" -> "io.a.B"
public static String getTypeName(Object obj | Class<?> type)
public static String getSimpleName(Class<?> type)
public static Class<?> getType(Object obj)
public static Class<?>[] getTypes(Object... objects)
public static Class<?>[] getClasses(Object... objects)
public static Class<?> getTopComponentType(Object obj | Class<?> type)

// Hierarchy (order: nearest first, Object excluded from interfaces)
public static List<Class<?>> getAllSuperClasses(Class<?> type)
public static List<Class<?>> getAllInterfaces(Class<?> type)
public static List<Class<?>> getAllInheritedTypes(Class<?> type)
public static List<Class<?>> getAllInheritedClasses(Class<?> type)
public static List<Class<?>> getAllClasses(Class<?> type)
public static List<Class<?>> findAllSuperClasses(Class<?> type, Predicate<? super Class<?>>... filters)
public static List<Class<?>> findAllInterfaces(Class<?> type, Predicate<? super Class<?>>... filters)
public static List<Class<?>> findAllInheritedClasses(Class<?> type, Predicate<? super Class<?>>... filters)
public static List<Class<?>> findAllClasses(Class<?> type, Predicate<? super Class<?>>... filters)

// Classpath scanning by name (returns binary names, not loaded classes)
public static Set<String> findClassNamesInClassPath(String classPath, boolean recursive)
public static Set<String> findClassNamesInClassPath(File classPath, boolean recursive)
public static Set<String> findClassNamesInDirectory(File directory, boolean recursive)
public static Set<String> findClassNamesInJarFile(File jarFile, boolean recursive)

// Misc
public static <T> T cast(Object object, Class<T> castType)
public static ProtectionDomain getProtectionDomain(Class<?> type)
public static CodeSource getCodeSource(Class<?> type)
public static String getCodeSourceLocation(Class<?> type)
```

Constants: `PRIMITIVE_TYPES`, `WRAPPER_TYPES`, `PRIMITIVE_ARRAY_TYPES` (all `Set<Class<?>>`).

> [!NOTE]
> The `getAll*` methods walk the full hierarchy; the `findAll*` variants apply your predicates while walking, so they
> avoid building an intermediate list. Both return `List<Class<?>>` ordered nearest-first from the queried type.

```java
ClassUtils.getAllSuperClasses(ArrayList.class);    // superclasses of ArrayList, nearest first
ClassUtils.getAllInterfaces(HashMap.class);        // every interface HashMap implements, incl. inherited ones
ClassUtils.getAllClasses(ArrayList.class);         // superclasses + interfaces together
ClassUtils.findAllInterfaces(HashMap.class, Map.class::isAssignableFrom);
ClassUtils.resolveClassName("io/microsphere/util/StringUtils.class");  // "io.microsphere.util.StringUtils"
```

`getAllClasses(Class)` is the method the `Converter` SPI's automatic priority calculation uses, so hierarchy
shape has real consequences in [Type Conversion](type-conversion.md).

---

## 3. `ClassLoaderUtils`

```java
// Which loader?
public static ClassLoader getDefaultClassLoader()
public static ClassLoader getClassLoader(Class<?> type)
public static ClassLoader getCallerClassLoader()
public static ClassLoader nullSafeClassLoader(ClassLoader classLoader)   // null -> default

// Loading
public static Class<?> loadClass(ClassLoader loader, String className)
public static Class<?> loadClass(ClassLoader loader, String className, boolean cached)
public static Class<?> resolveClass(String className)
public static Class<?> resolveClass(String className, ClassLoader loader)
public static Class<?> resolveClass(String className, ClassLoader loader, boolean cached)
public static boolean isPresent(String className)
public static boolean isPresent(String className, ClassLoader loader)

// Already-loaded introspection (HotSpot only, via Instrumentation-like beans)
public static Class<?> findLoadedClass(ClassLoader loader, String className)
public static Set<Class<?>> findLoadedClasses(ClassLoader loader, String... packageNames)
public static Set<Class<?>> findLoadedClasses(ClassLoader loader, Iterable<String> packageNames)
public static Set<Class<?>> findLoadedClassesInClassPath(ClassLoader loader)
public static Set<Class<?>> findLoadedClassesInClassPaths(ClassLoader loader)
public static Set<Class<?>> getAllLoadedClasses(ClassLoader loader)
public static Set<Class<?>> getLoadedClasses(ClassLoader loader)
public static Map<String, Class<?>> getAllLoadedClassesMap(ClassLoader loader)
public static int  getLoadedClassCount()
public static long getUnloadedClassCount()
public static long getTotalLoadedClassCount()
public static boolean isVerbose()
public static void setVerbose(boolean verbose)

// Resources
public static URL getResource(String resource)
public static URL getResource(ClassLoader loader, String resource)
public static URL getResource(ClassLoader loader, ResourceType resourceType, String resource)
public static Set<URL> getResources(ClassLoader loader, String resource)
public static String getResourceAsString(String resource)
public static String getResourceAsString(ClassLoader loader, String resource)
public static URL getClassResource(ClassLoader loader, Class<?> type)
public static URL getClassResource(ClassLoader loader, String className)

// URLClassLoader plumbing
public static URLClassLoader findURLClassLoader(ClassLoader loader)
public static URLClassLoader resolveURLClassLoader(ClassLoader loader)
public static URLClassLoader newURLClassLoader(URL... urls)
public static URLClassLoader newURLClassLoader(ClassLoader parent, URL... urls)
public static URLClassLoader newURLClassLoader(boolean initializedLoaders, URL... urls)
public static URLClassLoader newURLClassLoader(ClassLoader parent, boolean initializedLoaders, URL... urls)
public static Set<URL> findAllClassPathURLs(ClassLoader loader)
public static boolean removeClassPathURL(ClassLoader loader, URL url)
```

`ResourceType` is a nested enum (`io.microsphere.util.ClassLoaderUtils.ResourceType`) that normalizes a resource name
before lookup — useful when you have a class name and want its `.class` resource.

> [!NOTE]
> `findAllClassPathURLs` and `removeClassPathURL` are what
> [Class Loading and Artifacts](classloading-and-artifacts.md) is built on. On JDK 9+ the "remove" path goes
> through the `URLClassPathHandle` SPI, so behaviour depends on which handle implementation is available.

---

## 4. `ClassPathUtils`

Small and focused:

```java
public static Set<String> getBootstrapClassPaths()
public static Set<String> getClassPaths()                       // java.class.path entries
public static URL getRuntimeClassLocation(String className)
public static URL getRuntimeClassLocation(Class<?> type)
```

`getRuntimeClassLocation(String)` resolves a class name through the **JVM's own bootstrap/platform loaders** and
returns where its class file actually lives (a `jrt:`/`jar:` URL on modern JDKs). `getRuntimeClassLocation(Class<?>)`
takes the same shortcut for an already-loaded class.

---

## 5. `ServiceLoaderUtils`

The framework's SPI front door. Every pluggable subsystem in `microsphere-java-core` goes through it.

```java
public static final boolean SERVICE_LOADER_CACHED;   // from "microsphere.service-loader.cached", default false

// Discovery without loading instances (class names / classes only)
public static <T> Set<Class<T>> getServiceClasses(Class<T> serviceType)
public static <T> Set<Class<T>> getServiceClasses(Class<T> serviceType, boolean failFast)
public static <T> Set<Class<T>> getServiceClasses(Class<T> serviceType, ClassLoader classLoader)
public static <T> Set<Class<T>> getServiceClasses(Class<T> serviceType, ClassLoader classLoader, boolean failFast)
public static Set<String> getServiceClassNames(Class<?> serviceType)
public static Set<String> getServiceClassNames(Class<?> serviceType, ClassLoader classLoader)
public static Set<URL> getServiceResoources(Class<?> serviceType, ClassLoader classLoader) throws IOException

// Loading instances
public static <S> List<S> loadServicesList(Class<S> serviceType) throws IllegalArgumentException
public static <S> List<S> loadServicesList(Class<S> serviceType, ClassLoader classLoader) throws IllegalArgumentException
public static <S> List<S> loadServicesList(Class<S> serviceType, boolean cached) throws IllegalArgumentException
public static <S> List<S> loadServicesList(Class<S> serviceType, ClassLoader classLoader, boolean cached) throws IllegalArgumentException

public static <S> S[] loadServices(Class<S> serviceType) throws IllegalArgumentException
public static <S> S[] loadServices(Class<S> serviceType, ClassLoader classLoader) throws IllegalArgumentException
public static <S> S[] loadServices(Class<S> serviceType, boolean cached) throws IllegalArgumentException
public static <S> S[] loadServices(Class<S> serviceType, ClassLoader classLoader, boolean cached) throws IllegalArgumentException

public static <S> S loadFirstService(Class<S> serviceType)
public static <S> S loadFirstService(Class<S> serviceType, ClassLoader classLoader)
public static <S> S loadFirstService(Class<S> serviceType, ClassLoader classLoader, boolean cached)
public static <S> S loadLastService(Class<S> serviceType)
public static <S> S loadLastService(Class<S> serviceType, ClassLoader classLoader)
public static <S> S loadLastService(Class<S> serviceType, ClassLoader classLoader, boolean cached)
```

Loading **always sorts** the result with `Prioritized.COMPARATOR` (ascending `getPriority()`), so the first element is
the highest-priority implementation — which is what `Converters`, `LoggerFactory`, `AbstractEventDispatcher`,
`ArtifactDetector` and `ProcessIdResolver` rely on. The `cached` flag only controls whether the loaded list is reused.

> [!WARNING]
> Three traps:
> 1. The methods are spelled `getServiceResoources` (three o's) in the source. It compiles as-is; just know the name
>    before you search for it.
> 2. `SERVICE_LOADER_CACHED` defaults to **`false`**, i.e. repeated `loadServicesList` calls re-read and
>    re-instantiate services unless you pass `cached = true` or set `-Dmicrosphere.service-loader.cached=true`.
> 3. `loadServices*` throws **`IllegalArgumentException`** when the service file declares **zero** implementations —
>    it is not an empty-list return. If a service may be absent, guard the call or use `getServiceClasses(...)`
>    (which returns an empty set) first.

```java
ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();

List<Converter> converters = ServiceLoaderUtils.loadServicesList(Converter.class, classLoader, true);
Converter first = ServiceLoaderUtils.loadFirstService(Converter.class);   // highest priority
```

---

## 6. `Assert`

`public abstract class Assert` (deliberately does **not** implement `Utils`). Every method is `public static void`
and throws on failure. Each has a `String` and a `Supplier<String>` message form.

```java
public static void assertTrue(boolean expression, String message)
public static void assertTrue(boolean expression, Supplier<String> messageSupplier)
public static void assertNull(Object object, ...)
public static void assertNotNull(Object object, ...)
public static void assertNotEmpty(String text, ...)
public static void assertNotEmpty(Object[] array, ...)
public static void assertNotEmpty(Collection<?> collection, ...)
public static void assertNotEmpty(Map<?, ?> map, ...)
public static void assertNoNullElements(Object[] array, ...)
public static void assertNoNullElements(Iterable<?> iterable, ...)
public static void assertArrayIndex(Object array, int index)
public static void assertArrayType(Object array)                       // must be an array
public static void assertFieldMatchType(Object object, String fieldName, Class<?> expectedType)
```

> [!NOTE]
> The name is `assertTrue`, not `isTrue`. Failures are plain exceptions built with the supplied message — useful
> inside library code that wants a cheap, dependency-free precondition.

---

## 7. `Version` and `VersionUtils`

`public class Version implements Comparable<Version>, Serializable`

```java
// Construction
public Version(int major)
public Version(int major, int minor)
public Version(int major, int minor, int patch)
public Version(int major, int minor, int patch, String preRelease)

public static Version of(int major)                       // overload family
public static Version of(int major, int minor)
public static Version of(int major, int minor, int patch)
public static Version of(int major, int minor, int patch, String preRelease)
public static Version of(String version)

public static Version ofVersion(int... majorMinorPatch)   // same arities as of(...)
public static Version ofVersion(String version)
public static Version ofVersion(Class<?> classInResource) // reads the artifact's own POM/manifest version
public static Version getVersion(Class<?> targetClass) throws IllegalArgumentException

// Accessors
public int getMajor();  public int getMinor();  public int getPatch();  public String getPreRelease()

// Comparison
public boolean gt(Version v);  public boolean ge(Version v)
public boolean lt(Version v);  public boolean le(Version v);  public boolean eq(Version v)
public boolean isGreaterThan(Version v);      public boolean isGreaterOrEqual(Version v)
public boolean isLessThan(Version v);         public boolean isLessOrEqual(Version v)
public boolean equals(Object obj);           public boolean equals(Version version)
public int compareTo(Version that);          public int hashCode();  public String toString()
```

Both `of(...)` and `ofVersion(...)` families exist; `ofVersion(Class)` is the one that derives a version from the
jar that contains a class, which is how modules report their own version.

Nested `public enum Version.Operator implements BiPredicate<Version, Version>`:

```java
GT, GE, LT, LE, EQ
public boolean test(Version v1, Version v2)     // per constant
public static Operator of(String symbol)        // ">=" -> GE
```

`VersionUtils`:

```java
public static final Version LATEST_JAVA_VERSION;   // from javax.lang.model.SourceVersion
public static final Version CURRENT_JAVA_VERSION;
public static final Version JAVA_VERSION_8, ... JAVA_VERSION_24;

public static boolean testCurrentJavaVersion(String operatorSymbol, Version version)
public static boolean testCurrentJavaVersion(Version.Operator operator, Version version)
public static boolean testVersion(String baseVersion, String operatorSymbol, String comparedVersion)
public static boolean testVersion(Version base, Version.Operator operator, Version compared)
```

```java
Version microsphere = Version.ofVersion("0.3.19");
microsphere.gt(Version.of(0, 3, 0));                              // true
VersionUtils.testCurrentJavaVersion(">=", Version.of(17));        // JDK-gated code path
```

---

## 8. `StopWatch`

`public class StopWatch` — **not thread-safe, not reusable across `reset()`** (there is no `reset`).

```java
public StopWatch(String id)                        // the only constructor

public void start(String taskName) throws IllegalArgumentException, IllegalStateException
public void start(String taskName, boolean reentrant) throws IllegalArgumentException, IllegalStateException
public void stop() throws IllegalStateException
public Task getCurrentTask()

public String getId()
public List<Task> getRunningTasks()
public List<Task> getCompletedTasks()
public long getTotalTimeNanos()
public long getTotalTime(TimeUnit timeUnit)
public String toString()                           // per-task report
```

Nested `public static class StopWatch.Task`:

```java
public static Task start(String taskName)
public static Task start(String taskName, boolean reentrant)
public void stop()
public String getTaskName()
public boolean isReentrant()
public long getStartTimeNanos()
public long getElapsedNanos()
```

> [!WARNING]
> There is **no** `prettyPrint()`, `reset()`, `isRunning()`, `getLastTaskTimeMillis()` and no no-arg constructor —
> those are Commons-Lang/Spring names. Use `toString()` for the report and `getTotalTime(TimeUnit)` for the number.
> `start(String)` is not reentrant; pass `reentrant = true` if you start the same task name from nested frames.

```java
StopWatch watch = new StopWatch("bootstrap");
watch.start("spi");
ServiceLoaderUtils.loadServicesList(Converter.class);
watch.stop();

watch.start("cache");
...
watch.stop();

System.out.println(watch.getTotalTime(TimeUnit.MILLISECONDS));
System.out.println(watch);   // id + per-task elapsed nanos
```

---

## 9. Other `util` classes worth knowing

| Class | Public surface |
|---|---|
| `AnnotationUtils` | Annotation lookup across the type hierarchy (`findAnnotation`, `getAnnotation`, `getAllAnnotationAttributes`-style helpers) — the reflection-side companion to `io.microsphere.lang.model.util.AnnotationUtils` |
| `ArrayUtils` | `EMPTY_*_ARRAY` constants for every primitive + object; `of(T...)`, `ofBooleans/ofBytes/ofChars/ofShorts/ofInts/ofLongs/ofFloats/ofDoubles(...)`, `length(...)`, `size(T[])`, `isEmpty/isNotEmpty(T[])`, `arrayEquals(T[],T[])`, `forEach(T[], Consumer<T>)`, `forEach(T[], BiConsumer<Integer,T>)`, `arrayToString(T[])`, `reverse(T[])` |
| `ObjectUtils` | Exactly three methods: `<S,T> T nullSafe(S source, Function<S,T> function)`, `<T> T defaultIfNull(T object, Supplier<T> supplier)`, `<T> T defaultIfNull(T object, T defaultValue)` |
| `ExceptionUtils` | `getStackTrace(Throwable)`, `wrap(T source, Class<TT> thrownType)`, six `create(...)` overloads, `throwTarget(T source, Class<TT> thrownType) throws TT` |
| `ThrowableUtils` | `getRootCause(Throwable)` only |
| `StackTraceUtils` | Stack-trace element extraction/inspection |
| `SizeUtils` | Byte-size formatting and parsing |
| `IterableUtils` | Iterable-level helpers bridging `Iterator`/`Iterable`/`Enumeration` |
| `ValueHolder<V>` | `ValueHolder()`, `ValueHolder(V)`, `static <V> ValueHolder<V> of(V)`, `getValue()`, `setValue(V)`, `reset()` — the mutable-capture you need inside lambdas (alternative to `MutableInteger` / `AtomicReference`) |
| `Compatible<T, R>` | Adapt a legacy type to a new API surface |
| `Configurer<T>` | `void configure(T object)`-style callback contract |
| `Functional<V>` | Single-method callable abstraction |
| `TypeFinder` | Find a `Type` by name/class |
| `BaseUtils` | Number/base conversion helpers |
| `PropertyResourceBundleUtils` + `PropertyResourceBundleControl` | Load bundles with the framework's charset rules |
| `HierarchicalClassComparator` | `Comparator<Class<?>>` ordering a class hierarchy |
| `CharSequenceComparator`, `CharSequenceUtils` | `CharSequence` comparison/utilities that `StringUtils` deliberately omits |
| `PriorityComparator` | `Comparator<Object>` tolerant of non-`Prioritized` entries |
| `ShutdownHookUtils` | see below |
| `SystemUtils` | see below |
| `io.microsphere.util.jar.JarUtils` | see [§12](#12-jarutils) |

### `ShutdownHookUtils`

Register ordinary `Runnable`s into the JVM shutdown sequence instead of fighting over `Runtime.addShutdownHook`:

```java
public static boolean addShutdownHookCallback(Runnable callback)
public static boolean removeShutdownHookCallback(Runnable callback)
public static Queue<Runnable> getShutdownHookCallbacks()
public static void registerShutdownHook()              // installs the single JVM hook
public static Set<Thread> getShutdownHookThreads()
public static Set<Thread> filterShutdownHookThreads(Predicate<? super Thread> filter)
public static Set<Thread> filterShutdownHookThreads(Predicate<? super Thread> filter, boolean removed)

public static final int DEFAULT_SHUTDOWN_HOOK_CALLBACKS_CAPACITY;
public static final int SHUTDOWN_HOOK_CALLBACKS_CAPACITY;                 // from microsphere.shutdown-hook.callbacks-capacity, default 512
public static final Predicate<? super Thread> SHUTDOWN_HOOK_CALLBACKS_THREAD_FILTER;
```

`ExecutorUtils.shutdownOnExit(...)` ([Concurrency](concurrency-process-jmx.md)) is built on top of this, and the
thread filter exists because shutdown hooks all run as threads whose names you can recognise.

### `SystemUtils`

Constants plus two accessors:

```java
public static String getSystemProperty(String key)
public static String getSystemProperty(String key, String defaultValue)
```

Resolved constants include `JAVA_CLASS_PATH`, `USER_NAME`, `JAVA_HOME`, `JAVA_VERSION`, `OS_NAME`, `OS_ARCH`,
`OS_VERSION`, `FILE_ENCODING`, `NATIVE_ENCODING`, `USER_DIR`, `USER_HOME`, `JAVA_IO_TMPDIR`, `IS_OS_WINDOWS`
(with `OS_NAME_WINDOWS_PREFIX = "Windows"`), and an `IS_JAVA_8 … IS_JAVA_19` boolean series. Each has a paired
`*_PROPERTY_KEY` string constant.

> [!NOTE]
> The `IS_JAVA_*` series stops before the newest JDKs the CI builds against (21, 25). Prefer
> `VersionUtils.testCurrentJavaVersion(">=", Version.of(21))` for forward-compatible checks.

---

## 10. `constants` package

Eight `public interface` holders, all composable through `Constants`:

```java
public interface Constants extends FileConstants, PathConstants, PropertyConstants,
                                   ProtocolConstants, SeparatorConstants, SymbolConstants {}
```

| Interface | Representative members |
|---|---|
| `SymbolConstants` | `AND "&"`, `AT "@"`, `COLON ":"`, `COMMA ","`, `DOT "."`, `EQUAL "="`, `EXCLAMATION "!"`, `HYPHEN "-"`, `PIPE`/`VERTICAL_BAR "\|"`, `QUESTION_MARK "?"`, `QUOTE "'"` (single), `DOUBLE_QUOTE "\""`, `SEMICOLON ";"`, `SLASH "/"`, `SPACE " "`, `UNDER_SCORE "_"`, `WILDCARD "*"` — plus `*_CHAR` counterparts (`DOT_CHAR '.'`, `QUOTE_CHAR '\''`, `AND_CHAR '&'`) |
| `SeparatorConstants` | `FILE_SEPARATOR = File.separator`, `PATH_SEPARATOR = File.pathSeparator`, `LINE_SEPARATOR = System.lineSeparator()` |
| `ProtocolConstants` | `HTTP_PROTOCOL`, `HTTPS_PROTOCOL`, `FILE_PROTOCOL`, `JAR_PROTOCOL`, `WAR_PROTOCOL`, `EAR_PROTOCOL`, `ZIP_PROTOCOL`, `FTP_PROTOCOL`, `CLASSPATH_PROTOCOL = "classpath"`, `CONSOLE_PROTOCOL = "console"` |
| `FileConstants` | `CLASS "class"`, `JAR "jar"`, `WAR`, `EAR`, `ZIP`, `FILE_EXTENSION DOT`, `CLASS_EXTENSION ".class"`, `JAR_EXTENSION`, `JAVA_EXTENSION ".java"` |
| `PathConstants` | `ARCHIVE_ENTRY_SEPARATOR "!/"`, `METADATA_RESOURCE "META-INF/"`, `MICROSPHERE_METADATA_RESOURCE "META-INF/microsphere/"` |
| `PropertyConstants` | `ENABLED_PROPERTY_NAME "enabled"`, `MICROSPHERE_PROPERTY_NAME_PREFIX "microsphere."`, `CONFIGURATION_PROPERTY_METADATA_FILE_NAME "configuration-properties.json"`, `ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_FILE_NAME "additional-configuration-properties.json"` |
| `ResourceConstants` | `CONFIGURATION_PROPERTY_METADATA_RESOURCE = "META-INF/microsphere/configuration-properties.json"`, `ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE = "META-INF/microsphere/additional-configuration-properties.json"` |

> [!IMPORTANT]
> `QUOTE` is a **single** quote; use `DOUBLE_QUOTE` for `"`. And the metadata resource paths are the
> `META-INF/microsphere/*.json` pair above — not `additional-spring-configuration-metadata.json`.

---

## 11. `text.FormatUtils`

SLF4J-style `{}` interpolation, used by the logging facade:

```java
public static final String DEFAULT_PLACEHOLDER = "{}";
public static String format(String pattern, Object... args)
public static String formatWithPlaceholder(String pattern, String placeholder, Object... args)
```

```java
FormatUtils.format("No Resource[{}] found in {}", "app.yml", "classpath:");
// "No Resource[app.yml] found in classpath:"
```

---

## 12. `JarUtils`

```java
public static JarFile toJarFile(URL jarURL)
public static String resolveRelativePath(URL resourceURL)
public static String resolveJarAbsolutePath(URL jarURL)
public static List<JarEntry> filter(JarFile jarFile, JarEntryFilter filter)
public static JarEntry findJarEntry(URL resourceURL)
public static boolean isDirectoryEntry(URL url)
public static void extract(File jarSourceFile, File targetDirectory)
public static void extract(File jarSourceFile, File targetDirectory, JarEntryFilter filter)
public static void extract(JarFile jarFile, File targetDirectory, JarEntryFilter filter)
public static void extract(URL jarURL, File targetDirectory, JarEntryFilter filter) throws IOException
```

`JarEntryFilter` is a `Filter<JarEntry>` (see [Collections and Filters](collections-and-filters.md)), and
`ClassFileJarEntryFilter.INSTANCE` is the usual argument:

```java
JarUtils.extract(new URL("jar:file:/libs/app.jar!/"), new File("target/unpacked"),
                 ClassFileJarEntryFilter.INSTANCE);
```

---

## 13. See also

* [Language Abstractions](language-abstractions.md) — `Prioritized`, which orders everything `ServiceLoaderUtils` returns
* [Reflection and Types](reflection-and-types.md) — `MethodUtils`, `FieldUtils`, `TypeUtils`
* [Reference](reference.md) — the full system-property table

[← Previous: Annotations](annotations.md) · [Index](README.md) · [Next: Collections and Filters →](collections-and-filters.md)
