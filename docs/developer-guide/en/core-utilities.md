# Core Utilities

> Read this page in: [中文](../zh/core-utilities.md) · [English](core-utilities.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Module | `io.github.microsphere-projects:microsphere-java-core` |
| Packages | `io.microsphere.util`, `io.microsphere.util.jar`, `io.microsphere.constants`, `io.microsphere.text` |
| Class shape | `public abstract class XxxUtils implements Utils` + private constructor; all methods static |
| `Utils` | empty marker interface (`public interface Utils {}`), used only to group the helpers |
| Coverage | string, class, class-loader, service-loading, assertion, version, timing and JAR utilities, plus shared constants |

Never instantiate a `XxxUtils` class — call the static methods. Exceptions by design: `Assert` does
**not** implement `Utils`; `StopWatch`, `ValueHolder` and `Version` are ordinary instantiable
classes. Two placeholders to avoid: `NumberUtils` declares **no** public methods, and `BaseUtils` is
`@Deprecated` and empty. The shorthand `getType(Object obj | Class<?> type)` below means two
overloads; brackets mark optional trailing parameters.

---

## 1. StringUtils

`public abstract class StringUtils implements Utils`. Constants: `EMPTY` (`""`), `EMPTY_STRING`
(alias of `EMPTY`), `EMPTY_STRING_ARRAY`, `INDEX_NOT_FOUND` (`-1`).

```java
public static boolean isBlank(String value)     public static boolean isNotBlank(String value)
public static boolean isNumeric(String str)     public static boolean containsWhitespace(String str)
// contains() is the only method that accepts a CharSequence argument
public static boolean contains(String value, CharSequence part)
public static boolean startsWith(String value, String part)   public static boolean endsWith(String value, String part)
public static String[] split(String value, char delimiter | String delimiter)
public static String replace(String text, String searchString, String replacement | ..., int max)
public static String substringBetween(String str, String tag | String open, String close)
public static String substringBefore(String str, String separator)   public static String substringAfter(String str, String separator)
public static String substringBeforeLast(String str, String separator)   public static String substringAfterLast(String str, String separator)
public static String trimWhitespace(String str)         // both ends; also trimLeadingWhitespace / trimTrailingWhitespace
public static String trimAllWhitespace(String str)      // every occurrence
public static String capitalize(String str)             public static String uncapitalize(String str)
public static String[] toStringArray(Collection<String> collection)
public static String arrayToString(Object[] values, String delimiter)
```

> [!WARNING]
> `StringUtils` has **no** `hasText`, `hasLength`, `join`, `concat`, `defaultIfBlank`,
> `defaultString` or `equalsIgnoreCase` — Commons-Lang names do not all exist here. For
> `CharSequence` operations use `CharSequenceUtils` / `CharSequenceComparator`; for `{}`
> interpolation use `FormatUtils` ([§10](#10-formatutils)).

Null handling is consistent: `isBlank(null)` is `true`; `split(null, ',')` returns an **empty array**
(not `null`); `split("abc", ',')` (delimiter absent) returns `["abc"]`; `split(value, nullDelimiter)`
returns `[value]`; trim/case methods return `null` or `""` input as-is.

```java
StringUtils.split("a,b,c", ',')                // ["a","b","c"]
StringUtils.split(null, ',')                   // String[0]
StringUtils.trimAllWhitespace(" a b ")         // "ab"
StringUtils.substringBefore("key=value", "=")  // "key"
```

---

## 2. ClassUtils

68 static methods. The ones you will reach for:

```java
// Kind tests; "Object obj | Class<?>" means both overloads exist
public static boolean isWrapperType(Object obj | Class<?>)   public static boolean isArray(Object obj | Class<?>)   public static boolean isEnum(Object obj | Class<?>)
public static boolean isNumber(Object obj | Class<?>)   public static boolean isCharSequence(Object obj | Class<?>)   public static boolean isSimpleType(Object obj | Class<?>)
public static boolean isPrimitive(Class<?>)   public static boolean isSyntheticClass(Object obj | Class<?>)   public static boolean isClass(Object)
public static boolean isLambdaClass(Object obj | Class<?>)   public static boolean isLambdaClassName(String)   public static boolean isFunctionalInterface(Class<?>)
public static boolean isAbstractClass(Class<?>)   public static boolean isFinal(Class<?>)   public static boolean isConcreteClass(Class<?>)
public static boolean isGeneralClass(Class<?>)   public static boolean isTopLevelClass(Class<?>)   public static boolean arrayTypeEquals(Class<?> one, Class<?> two)
public static boolean isDerived(Class<?> sourceType, Class<?>... superTypes)   public static boolean isAssignableFrom(Class<?> superType, Class<?> targetType)
// Primitive / wrapper mapping
public static Class<?> resolvePrimitiveType(Class<?> type)   public static Class<?> resolveWrapperType(Class<?> primitiveType)   public static Class<?> tryResolveWrapperType(Class<?> type)
public static Class<?> resolvePrimitiveClassForName(String className)
// Names and types
public static String resolvePackageName(Class<?> type | String className)   public static String resolveClassName(String resourceName)   // "io/a/B.class" -> "io.a.B"
public static String getTypeName(Object obj | Class<?> type)   public static String getSimpleName(Class<?> type)   public static Class<?> getTopComponentType(Object obj | Class<?> type)
public static Class<?> getType(Object obj)   public static Class<?>[] getTypes(Object... objects)   public static Class<?>[] getClasses(Object... objects)
// Hierarchy (ordered nearest-first; Object excluded from interfaces)
public static List<Class<?>> getAllSuperClasses(Class<?> type)   public static List<Class<?>> getAllInterfaces(Class<?> type)
public static List<Class<?>> getAllClasses(Class<?> type)        // superclasses + interfaces
public static List<Class<?>> findAllSuperClasses(Class<?> type, Predicate<? super Class<?>>... filters)   public static List<Class<?>> findAllInterfaces(Class<?> type, Predicate<? super Class<?>>... filters)
public static List<Class<?>> findAllClasses(Class<?> type, Predicate<? super Class<?>>... filters)
// Classpath scanning by name (binary names, classes are not loaded)
public static Set<String> findClassNamesInClassPath(String classPath | File classPath, boolean recursive)   public static Set<String> findClassNamesInJarFile(File jarFile, boolean recursive)
public static Set<String> findClassNamesInDirectory(File directory, boolean recursive)
// Misc
public static <T> T cast(Object object, Class<T> castType)   public static ProtectionDomain getProtectionDomain(Class<?> type)
public static CodeSource getCodeSource(Class<?> type)        // also getCodeSourceLocation(Class<?>)
```

Also present: `getAllInheritedTypes`, `getAllInheritedClasses`, `findAllInheritedClasses`.
Constants: `PRIMITIVE_TYPES`, `WRAPPER_TYPES`, `PRIMITIVE_ARRAY_TYPES`, `SIMPLE_TYPES` (all
`Set<Class<?>>`), `ARRAY_SUFFIX` (`"[]"`), `LAMBDA_CLASS_NAME_PREFIX` (`"$$Lambda"`).

> [!NOTE]
> `getAll*` walks the full hierarchy; `findAll*` applies your predicates **while** walking, avoiding an
> intermediate list. Both return `List<Class<?>>` ordered nearest-first. `getAllClasses(Class)` is
> what the `Converter` SPI's automatic priority calculation uses, so hierarchy shape has real
> consequences in [Type Conversion](type-conversion.md).

```java
ClassUtils.getAllSuperClasses(ArrayList.class);   // superclasses, nearest first
ClassUtils.getAllInterfaces(HashMap.class);       // every interface, incl. inherited ones
ClassUtils.findAllInterfaces(HashMap.class, Map.class::isAssignableFrom);
ClassUtils.resolveClassName("io/microsphere/util/StringUtils.class");  // "io.microsphere.util.StringUtils"
```

---

## 3. ClassLoaderUtils

```java
// Which loader?
public static ClassLoader getDefaultClassLoader()               // never null
public static ClassLoader getClassLoader(Class<?> type)         public static ClassLoader getCallerClassLoader()
public static ClassLoader nullSafeClassLoader(ClassLoader classLoader)   // null -> default
public static Set<ClassLoader> getInheritableClassLoaders(ClassLoader classLoader)
// Loading
public static Class<?> loadClass(ClassLoader loader, String className[, boolean cached])
public static Class<?> resolveClass(String className[, ClassLoader loader][, boolean cached])
public static boolean isPresent(String className[, ClassLoader loader])
// Already-loaded introspection (HotSpot only)
public static Class<?> findLoadedClass(ClassLoader loader, String className)
public static boolean isLoadedClass(ClassLoader loader, Class<?> type | String className)
public static Set<Class<?>> findLoadedClasses(ClassLoader loader, String... packageNames | Iterable<String> packageNames)
public static Set<Class<?>> findLoadedClassesInClassPath(ClassLoader loader)   // and ...InClassPaths
public static Set<Class<?>> getAllLoadedClasses(ClassLoader loader)            // and getLoadedClasses, getAllLoadedClassesMap
public static int getLoadedClassCount()      public static long getUnloadedClassCount()      public static long getTotalLoadedClassCount()
public static boolean isVerbose()            public static void setVerbose(boolean verbose)
// Resources
public static URL getResource(String resource[, ClassLoader loader][, ResourceType resourceType])
public static Set<URL> getResources(ClassLoader loader, String resource)
public static String getResourceAsString(String resource[, ClassLoader loader])
public static URL getClassResource(ClassLoader loader, Class<?> type | String className)
// URLClassLoader plumbing
public static URLClassLoader findURLClassLoader(ClassLoader loader)            // also resolveURLClassLoader
public static URLClassLoader newURLClassLoader(URL... urls)     // + parent and/or initializedLoaders leading params
public static Set<URL> findAllClassPathURLs(ClassLoader loader)
public static boolean removeClassPathURL(ClassLoader loader, URL url)
```

`ResourceType` is a nested enum (`io.microsphere.util.ClassLoaderUtils.ResourceType`) that normalizes
a resource name before lookup — useful when you have a class name and want its `.class` resource.

> [!IMPORTANT]
> The "already-loaded" group relies on HotSpot internals; on other JVMs expect empty results or
> failure, so never gate correctness on it. `findAllClassPathURLs` and `removeClassPathURL` are what
> [Class Loading and Artifacts](classloading-and-artifacts.md) is built on. On JDK 9+ the "remove"
> path goes through the `URLClassPathHandle` SPI, so behaviour depends on which handle implementation
> is available.

---

## 4. ClassPathUtils

```java
public static Set<String> getBootstrapClassPaths()
public static Set<String> getClassPaths()                       // java.class.path entries
public static URL getRuntimeClassLocation(String className | Class<?> type)
```

`getRuntimeClassLocation(String)` resolves a class name through the **JVM's own bootstrap/platform
loaders** and returns where its class file actually lives (a `jrt:`/`jar:` URL on modern JDKs); the
`Class<?>` overload is the shortcut for an already-loaded class.

---

## 5. ServiceLoaderUtils

The framework's SPI front door; every pluggable subsystem in `microsphere-java-core` goes through it.

```java
public static final String SERVICES_PROVIDER_LOCATION;   // "META-INF/services/"
public static final String SERVICE_LOADER_CACHED_PROPERTY_NAME;  // "microsphere.service-loader.cached"
public static final boolean SERVICE_LOADER_CACHED;       // from that property, default false
// Discovery without loading instances (class names / classes only)
public static <T> Set<Class<T>> getServiceClasses(Class<T> serviceType[, ClassLoader loader][, boolean failFast])
public static Set<String> getServiceClassNames(Class<?> serviceType[, ClassLoader loader])
public static Set<URL> getServiceResoources(Class<?> serviceType, ClassLoader loader) throws IOException
// Loading instances; result sorted by Prioritized.COMPARATOR (ascending getPriority())
public static <S> List<S> loadServicesList(Class<S> serviceType[, ClassLoader loader][, boolean cached]) throws IllegalArgumentException
public static <S> S[] loadServices(Class<S> serviceType[, ClassLoader loader][, boolean cached]) throws IllegalArgumentException
public static <S> S loadFirstService(Class<S> serviceType[, ClassLoader loader][, boolean cached]) throws IllegalArgumentException
public static <S> S loadLastService(Class<S> serviceType[, ClassLoader loader][, boolean cached]) throws IllegalArgumentException
```

The first element is the highest-priority implementation — which is what `Converters`,
`LoggerFactory`, `AbstractEventDispatcher`, `ArtifactDetector` and `ProcessIdResolver` rely on. The
`cached` flag only controls reuse of the loaded list; the returned `List` is unmodifiable.

> [!WARNING]
> Three traps:
> 1. The method is spelled `getServiceResoources` (three o's) in the source — search for that name.
> 2. `SERVICE_LOADER_CACHED` defaults to **`false`**: without `cached = true` (or
>    `-Dmicrosphere.service-loader.cached=true`), repeated `loadServicesList` calls re-read and
>    re-instantiate the services.
> 3. `loadServices*` / `loadFirstService` / `loadLastService` throw **`IllegalArgumentException`**
>    when the service file declares **zero** implementations — not an empty-list return. If a service
>    may be absent, call `getServiceClasses(...)` first (it returns an empty set).

```java
ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
List<Converter> converters = ServiceLoaderUtils.loadServicesList(Converter.class, classLoader, true);
Converter first = ServiceLoaderUtils.loadFirstService(Converter.class);   // highest priority
```

---

## 6. Assert

`public abstract class Assert` (deliberately does **not** implement `Utils`). Every assertion is
`public static void` and throws on failure; each has a `String` and a `Supplier<String>` message form.

```java
public static void assertTrue(boolean expression, String message | Supplier<String>)
public static void assertNull(Object object, ...)             public static void assertNotNull(Object object, ...)
public static void assertNotEmpty(String text | Object[] | Collection<?> | Map<?, ?>, ...)
public static void assertNotBlank(String text, ...)
public static void assertNoNullElements(Object[] array | Iterable<?> iterable, ...)
public static void assertArrayIndex(Object array, int index)  public static void assertArrayType(Object array)
public static void assertFieldMatchType(Object object, String fieldName, Class<?> expectedType)
```

> [!NOTE]
> The name is `assertTrue`, not `isTrue`. Failures are plain exceptions built with the supplied
> message — a cheap, dependency-free precondition check for library code.

---

## 7. Version and VersionUtils

`public class Version implements Comparable<Version>, Serializable`

```java
public Version(int major[, int minor[, int patch[, String preRelease]]])
public static Version of(int major[, int minor[, int patch[, String preRelease]]])     public static Version of(String version)
public static Version ofVersion(int major[, int minor[, int patch[, String preRelease]]])  // same arities as of(...)
public static Version ofVersion(String version | Class<?> classInResource)
public static Version getVersion(Class<?> targetClass) throws IllegalArgumentException
public int getMajor();   public int getMinor();   public int getPatch();   public String getPreRelease()
// Comparison; isGreaterThan/isGreaterOrEqual/isLessThan/isLessOrEqual are the worded aliases
public boolean gt(Version v)   public boolean ge(Version v)   public boolean lt(Version v)   public boolean le(Version v)   public boolean eq(Version v)
public boolean equals(Version version);   public int compareTo(Version that)
```

`ofVersion(Class)` derives a version from the jar that contains a class, which is how modules report
their own version. Nested `public enum Version.Operator implements BiPredicate<Version, Version>`:
constants `GT, GE, LT, LE, EQ` with `test(Version v1, Version v2)` and `static Operator of(String
symbol)` (`">="` -> `GE`). `public abstract class VersionUtils implements Utils` adds:

```java
public static final SourceVersion LATEST_JAVA_VERSION;   // a javax.lang.model.SourceVersion, not a Version
public static final Version CURRENT_JAVA_VERSION;        public static final Version JAVA_VERSION_8, ... JAVA_VERSION_24;
public static boolean testCurrentJavaVersion(String operatorSymbol | Version.Operator operator, Version version)
public static boolean testVersion(String baseVersion, String operatorSymbol, String comparedVersion)
public static boolean testVersion(Version base, Version.Operator operator, Version compared)
```

```java
Version microsphere = Version.ofVersion("0.3.19");
microsphere.gt(Version.of(0, 3, 0));                              // true
VersionUtils.testCurrentJavaVersion(">=", Version.of(17));        // JDK-gated code path
```

---

## 8. StopWatch

`public class StopWatch` — **not thread-safe**, and there is no `reset()`: one instance accumulates
tasks for its whole life.

```java
public StopWatch(String id)                        // the only constructor
public void start(String taskName[, boolean reentrant]) throws IllegalArgumentException, IllegalStateException
public void stop() throws IllegalStateException    public Task getCurrentTask()
public String getId()
public List<Task> getRunningTasks()                // also getCompletedTasks()
public long getTotalTimeNanos()                    public long getTotalTime(TimeUnit timeUnit)
public String toString()                           // per-task report
```

Nested `public static class StopWatch.Task`: `static Task start(String taskName[, boolean reentrant])`,
`stop()`, `getTaskName()`, `isReentrant()`, `getStartTimeNanos()`, `getElapsedNanos()`.

> [!WARNING]
> There is **no** `prettyPrint()`, `reset()`, `isRunning()`, `getLastTaskTimeMillis()` and no no-arg
> constructor — those are Commons-Lang/Spring names. Use `toString()` for the report and
> `getTotalTime(TimeUnit)` for the number. `start(String)` is not reentrant; pass `reentrant = true`
> if you start the same task name from nested frames.

```java
StopWatch watch = new StopWatch("bootstrap");
watch.start("spi");
ServiceLoaderUtils.loadServicesList(Converter.class);
watch.stop();
System.out.println(watch.getTotalTime(TimeUnit.MILLISECONDS));
System.out.println(watch);   // id + per-task elapsed nanos
```

---

## 9. The `constants` package

Eight `public interface` holders, composed by `Constants`:

```java
public interface Constants extends FileConstants, PathConstants, PropertyConstants,
                                   ProtocolConstants, SeparatorConstants, SymbolConstants {}
```

| Interface | Representative members |
|---|---|
| `SymbolConstants` | `AND "&"`, `AT "@"`, `COLON ":"`, `COMMA ","`, `DOT "."`, `EQUAL "="`, `HYPHEN "-"`, `PIPE`/`VERTICAL_BAR "\|"` (aliases), `QUOTE "'"` (single), `DOUBLE_QUOTE "\""`, `SEMICOLON ";"`, `SPACE " "`, `UNDER_SCORE "_"`, `WILDCARD "*"` — each with a `*_CHAR` counterpart (`DOT_CHAR '.'`, `QUOTE_CHAR '\''`) |
| `SeparatorConstants` | `FILE_SEPARATOR = File.separator`, `PATH_SEPARATOR = File.pathSeparator`, `LINE_SEPARATOR = System.lineSeparator()`, `ARCHIVE_ENTRY_SEPARATOR "!/"` |
| `PathConstants` | `SLASH "/"`, `DOUBLE_SLASH "//"`, `BACK_SLASH "\\"` — plus `SLASH_CHAR`, `BACK_SLASH_CHAR` |
| `ProtocolConstants` | `HTTP_PROTOCOL`, `HTTPS_PROTOCOL`, `FILE_PROTOCOL`, `JAR_PROTOCOL`, `WAR_PROTOCOL`, `EAR_PROTOCOL`, `ZIP_PROTOCOL`, `FTP_PROTOCOL`, `CLASSPATH_PROTOCOL = "classpath"`, `CONSOLE_PROTOCOL = "console"` |
| `FileConstants` | `CLASS "class"`, `JAR "jar"`, `WAR`, `EAR`, `ZIP`, `FILE_EXTENSION DOT`, `CLASS_EXTENSION ".class"`, `JAR_EXTENSION`, `JAVA_EXTENSION ".java"` |
| `PropertyConstants` | `ENABLED_PROPERTY_NAME "enabled"`, `MICROSPHERE_PROPERTY_NAME_PREFIX "microsphere."` |
| `ResourceConstants` | `METADATA_RESOURCE "META-INF/"`, `MICROSPHERE_METADATA_RESOURCE "META-INF/microsphere/"`, `CONFIGURATION_PROPERTY_METADATA_FILE_NAME "configuration-properties.json"`, its `ADDITIONAL_...` counterpart, and the two composed `*_METADATA_RESOURCE` paths |

> [!IMPORTANT]
> `QUOTE` is a **single** quote; use `DOUBLE_QUOTE` for `"`. The metadata resource paths live in
> `ResourceConstants` (not `PathConstants`): they are the `META-INF/microsphere/*.json` pair above —
> not `additional-spring-configuration-metadata.json`. `ResourceConstants` is **not** extended by
> `Constants`, so reference it directly.

---

## 10. FormatUtils

`io.microsphere.text.FormatUtils` — SLF4J-style `{}` interpolation, used by the logging facade:

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

## 11. JarUtils

`io.microsphere.util.jar.JarUtils` (`MANIFEST_RESOURCE_PATH` = `"META-INF/MANIFEST.MF"`):

```java
public static JarFile toJarFile(URL jarURL)        public static JarEntry findJarEntry(URL resourceURL)
public static String resolveRelativePath(URL resourceURL)      public static String resolveJarAbsolutePath(URL jarURL)
public static List<JarEntry> filter(JarFile jarFile, JarEntryFilter filter)
public static boolean isDirectoryEntry(URL url)
public static void extract(File jarSourceFile, File targetDirectory[, JarEntryFilter filter]) throws IOException
public static void extract(JarFile jarFile | URL jarResourceURL, File targetDirectory, JarEntryFilter jarEntryFilter) throws IOException
```

`JarEntryFilter` is a `Filter<JarEntry>` (see [Collections and Filters](collections-and-filters.md)),
and `ClassFileJarEntryFilter.INSTANCE` (in `io.microsphere.filter`) is the usual argument:

```java
JarUtils.extract(new File("libs/app.jar"), new File("target/unpacked"), ClassFileJarEntryFilter.INSTANCE);
```

---

## 12. Other utility classes worth knowing

| Class | Public surface |
|---|---|
| `AnnotationUtils` | Annotation lookup across the type hierarchy (`findAnnotation`, `getAnnotation`, attribute-map helpers) |
| `ArrayUtils` | `EMPTY_*` constants for every primitive and object array; `of(T...)`/`ofArray(T...)`, `ofBooleans` … `ofDoubles(...)`, `length/size(T[])`, `isEmpty/isNotEmpty(T[])`, `arrayEquals`, `contains`, `combine`, `asArray(Collection/Iterable/Enumeration, componentType)`, `newArray`, `reverse(T[])`, `toArrayReversed`, `forEach(T[], Consumer)` and `forEach(T[], BiConsumer<Integer,T>)`, `arrayToString` |
| `ObjectUtils` | Exactly three methods: `nullSafe(S source, Function<S,T>)`, `defaultIfNull(T, Supplier<T>)`, `defaultIfNull(T object, T defaultValue)` |
| `ExceptionUtils` | `getStackTrace(Throwable)`, `wrap(T source, Class<TT>)`, six `create(...)` overloads, `throwTarget(T source, Class<TT>) throws TT`; `ThrowableUtils` has `getRootCause(Throwable)` only |
| `StackTraceUtils` | Stack-trace element extraction/inspection |
| `SizeUtils` | `bytesSize(Class<?> type)` plus per-primitive constants (`BOOLEAN_BYTES_SIZE` … `DOUBLE_BYTES_SIZE`, `UNBOUND_BYTES_SIZE`) — it maps a type to its byte width; it does **not** format or parse human sizes |
| `IterableUtils` | `isIterable(Object/Class<?>)`, `iterate(Iterable, Consumer)`, `forEach(Iterable, Consumer)` |
| `ValueHolder<V>` | `ValueHolder()`, `ValueHolder(V)`, `static of(V)`, `getValue()`, `setValue(V)`, `reset()` — mutable capture inside lambdas (alternative to `AtomicReference`) |
| `Compatible<T, R>`, `Configurer<T>`, `Functional<V>` | Small functional-style contracts: adapt legacy types, configure objects, single-method callable |
| `TypeFinder<T>` | `classFinder(...)` / `genericTypeFinder(...)` with a nested `Include` enum controlling self/hierarchy inclusion |
| `PropertyResourceBundleUtils` + `PropertyResourceBundleControl` | Load bundles with an explicit encoding; honours the `java.util.PropertyResourceBundle.encoding` property |
| `CharSequenceComparator`, `CharSequenceUtils` | `CharSequence` operations `StringUtils` omits (`length`, `isEmpty`, `isNotEmpty`, `containsWhitespace`, `trimAllWhitespace`) |
| `HierarchicalClassComparator`, `PriorityComparator` | `Comparator<Class<?>>` ordering a hierarchy; `Comparator<Object>` tolerant of non-`Prioritized` entries (`PriorityComparator.INSTANCE`) |

### ShutdownHookUtils

Register ordinary `Runnable`s into the JVM shutdown sequence instead of fighting over
`Runtime.addShutdownHook`:

```java
public static boolean addShutdownHookCallback(Runnable callback)   public static boolean removeShutdownHookCallback(Runnable callback)
public static Queue<Runnable> getShutdownHookCallbacks()
public static void registerShutdownHook()              // installs the single JVM hook
public static Set<Thread> getShutdownHookThreads()     public static Set<Thread> filterShutdownHookThreads(Predicate<? super Thread> filter[, boolean removed])
public static final int DEFAULT_SHUTDOWN_HOOK_CALLBACKS_CAPACITY;    // 512
public static final int SHUTDOWN_HOOK_CALLBACKS_CAPACITY;            // microsphere.shutdown-hook.callbacks-capacity
public static final Predicate<? super Thread> SHUTDOWN_HOOK_CALLBACKS_THREAD_FILTER;  // matches ShutdownHookCallbacksThread
```

`ExecutorUtils.shutdownOnExit(...)` ([Concurrency](concurrency-process-jmx.md)) is built on top of
this; the thread filter exists because all callbacks run inside a dedicated
`ShutdownHookCallbacksThread`.

### SystemUtils

Constants plus three methods: `getSystemProperty(String key[, String defaultValue])` and
`copySystemProperties()`. Resolved constants include `JAVA_CLASS_PATH`, `USER_NAME`, `JAVA_HOME`,
`JAVA_VERSION`, `OS_NAME`, `OS_ARCH`, `OS_VERSION`, `FILE_ENCODING`, `NATIVE_ENCODING`, `USER_DIR`,
`USER_HOME`, `JAVA_IO_TMPDIR`, `IS_OS_WINDOWS` (with `OS_NAME_WINDOWS_PREFIX = "Windows"`), the
`IS_JAVA_8` … `IS_JAVA_24` boolean series and `IS_LTS_JAVA_VERSION`; each property has a paired
`*_PROPERTY_KEY` string constant.

> [!TIP]
> The `IS_JAVA_*` series is static data ending at 24; it will not cover a future JDK you adopt. For
> forward-compatible checks prefer `VersionUtils.testCurrentJavaVersion(">=", Version.of(21))`.

---

## See also

* [Language Abstractions](language-abstractions.md) — `Prioritized`, which orders everything `ServiceLoaderUtils` returns
* [Reflection and Types](reflection-and-types.md) — `MethodUtils`, `FieldUtils`, `TypeUtils`
* [Collections and Filters](collections-and-filters.md) — the `Filter` contract behind `JarEntryFilter`
* [Reference](reference.md) — the full system-property table

[← Handbook index](../README.md) · [Previous: Annotations](annotations.md) · [Next: Collections and Filters →](collections-and-filters.md)
