# Utilities, Constants and Text

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-java-core` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Packages** | `io.microsphere.util` (34 types) · `io.microsphere.util.jar` (1) · `io.microsphere.constants` (8) · `io.microsphere.text` (1) · `io.microsphere.misc` (1) — 45 main-source types |
| **JDK level** | Compiled at Java 8; CI-tested on JDK 8 / 11 / 17 / 21 / 25 (Temurin) |
| **Compile dependency** | `microsphere-java-annotations` (`@Nonnull` / `@Nullable` / `@Immutable` / `@ConfigurationProperty`) — no other core package is required |
| **Marker convention** | Utility classes are `abstract` + private constructor + `implements io.microsphere.util.Utils`; see [`Utils`](#headline-types-the-marker-interface) |

This slice is the **foundation layer** of `microsphere-java-core`. It holds the low-level helpers that every other
package builds on: class-loader and resource resolution (`ClassLoaderUtils`), classpath introspection
(`ClassPathUtils`), the service-loading facade over `java.util.ServiceLoader` (`ServiceLoaderUtils`), version modelling
(`Version` + `VersionUtils`), array/string/assert primitives (`ArrayUtils`, `StringUtils`, `Assert`, `SystemUtils`),
the text placeholder formatter (`FormatUtils`), JAR access (`util.jar.JarUtils`), `sun.misc.Unsafe` access
(`misc.UnsafeUtils`) and the shared constant interfaces (`io.microsphere.constants`).

**Reach for these types when** you need a null-safe, JDK-version-portable way to: resolve a class or resource across a
class-loader hierarchy, enumerate or load SPI providers in priority order, compare a library or JDK version, split/blank
strings, build empty arrays, read JAR manifests, or reach `Unsafe` — without writing the fragile reflective code
yourself. If a helper here already does the job, prefer it over Apache Commons or hand-rolled reflection: the behaviour
is pinned by the library's own test suite and documented against the real sources.

> [!NOTE]
> These utilities deliberately **degrade rather than throw**: most lookups return `null` / an empty collection / a
> neutral `boolean` when the input is absent. This is by design (see [Pitfalls](#6-pitfalls-and-known-limits)), but a
> handful of methods *do* throw — every such case is flagged below and is backed by a line reference.

---

## 1. What this group is for and when to use it

| I want to… | Use | Why this rather than the JDK directly |
|---|---|---|
| Load a class by name, walking the full loader chain | `ClassLoaderUtils.loadClass` / `resolveClass` | Handles primitives, `null` loaders, and caches results (`util/ClassLoaderUtils.java:547`, `:1426`) |
| Find a `.class` / package / arbitrary resource `URL` | `ClassLoaderUtils.getResource(...)` + `ResourceType` | Normalises the name per resource kind before lookup (`util/ClassLoaderUtils.java:783`, `:843`, `:1931`) |
| List a loader's classpath entries | `ClassLoaderUtils.findAllClassPathURLs` | Delegates to a JDK-aware `URLClassPathHandle` (`util/ClassLoaderUtils.java:1550-1564`) |
| Load SPI providers in priority order | `ServiceLoaderUtils.loadServicesList` / `loadFirstService` | Sorts by `Prioritized`, optional caching (`util/ServiceLoaderUtils.java:890`, `:846`) |
| Know the running JDK version | `VersionUtils.CURRENT_JAVA_VERSION` | Parses `java.version` for legacy and modern schemes (`util/VersionUtils.java:266`, `:284`) |
| Compare library versions | `Version.ofVersion(String)` | Immutable, `Comparable`, lenient segment parsing (`util/Version.java:595`) |
| Read system properties safely | `SystemUtils.getSystemProperty` | Tolerates `SecurityException`, caches a copy (`util/SystemUtils.java:605`) |
| Split / blank-check strings | `StringUtils.split` / `isBlank` | `null`-safe, keeps empty tokens (`util/StringUtils.java:163`, `:81`) |
| Shared literal values | `io.microsphere.constants.*` | One source of truth for separators, protocols, symbols (`constants/Constants.java:13-14`) |
| Format `{} placeholders` | `text.FormatUtils.format` | SLF4J-style substitution (`text/FormatUtils.java:38`) |
| Open a JAR from a resource URL | `util.jar.JarUtils.toJarFile` | Resolves the archive path, returns `null` on miss (`util/jar/JarUtils.java:86`) |
| Reach `sun.misc.Unsafe` | `misc.UnsafeUtils` | Reflects `theUnsafe`, with offsets cached (`misc/UnsafeUtils.java:34`, `:564`) |

---

## 2. Coordinates and versioning

Maven coordinates (from the reactor POM):

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

| Field | Value | Evidence |
|---|---|---|
| Group ID | `io.github.microsphere-projects` | `pom.xml:12` |
| Artifact ID | `microsphere-java-core` | `microsphere-java-core/pom.xml` (module in `pom.xml:61`) |
| Version | `${revision}` = `0.3.20-SNAPSHOT` on `release` | `pom.xml:54` |
| Parent (build) | `microsphere-build:0.3.16` | `pom.xml:9` |
| Compile dependency | `microsphere-java-annotations` | `microsphere-java-core/pom.xml` (module `pom.xml:60`) |
| Bytecode target | Java 8 | no `maven.compiler.*` / `<release>` property exists anywhere in the reactor POMs, so the level is inherited from the external parent `microsphere-build:0.3.16`; cross-checked by the CI matrix below |
| CI JDK matrix | 8, 11, 17, 21, 25 (Temurin) | `.github/workflows/maven-build.yml:25`, `:33` |
| Consumable BOM | `microsphere-java-dependencies` (in-repo; manages exactly the 7 code-bearing modules) | `microsphere-java-dependencies/pom.xml` |
| Imported BOM | `microsphere-all-bom:0.3.13` + `spring-framework-bom` | `microsphere-java-parent/pom.xml:22`, `:33-36`, `:40-46` |
| Spring Framework compatible range | 5.3.39 on JDK `[1.8,17)` via the `java8-16` profile; 7.0.9 otherwise | `microsphere-java-parent/pom.xml:23`, `:55-62` |

> [!TIP]
> The CI runs `mvn ... -Drevision=0.0.1-SNAPSHOT ... -Dsurefire.useSystemClassLoader=false test`
> (`.github/workflows/maven-build.yml:38-45`). The `-Drevision` there is a build-time override, not the published
> version; the released `release`-branch version stays `0.3.20-SNAPSHOT`. The `useSystemClassLoader=false` flag is
> relevant to these utilities because several tests assert behaviour that depends on which class loader sees the test
> resources.

---

## 3. API reference

Full paths are given once; subsequent references shorten `microsphere-java-core/src/main/java/io/microsphere/` to the
last two segments (e.g. `util/ClassLoaderUtils.java`). Signatures are copied from the `release` sources.

### Headline types: the marker interface

```java
// util/Utils.java — an empty marker interface, @since 1.0.0
public interface Utils {}
```

`Utils` carries no methods. Its contract is conventional, not enforced: a "utility" class is declared `abstract`, given
a private constructor, and `implements Utils`. Nearly every class in this slice follows it — `ClassLoaderUtils`,
`ServiceLoaderUtils`, `ClassPathUtils`, `ArrayUtils`, `StringUtils`, `SystemUtils`, `NumberUtils`, `VersionUtils`,
`text.FormatUtils`, `util.jar.JarUtils`, `misc.UnsafeUtils`.

> [!WARNING]
> `util/Assert.java` is `abstract` with a private constructor but **does not** implement `Utils` — a deliberate
> exception, because it throws rather than degrades. `util/Version.java` is also *not* a utility: it is an immutable
> value object (`@Immutable`, `Comparable<Version>`, `Serializable`).

### `ClassLoaderUtils` (util/ClassLoaderUtils.java) — class + resource resolution

```java
public abstract class ClassLoaderUtils implements Utils {          // @since 1.0.0, private ctor
    protected static final ClassLoadingMXBean classLoadingMXBean; // :90
    private static final ConcurrentMap<String,Class<?>> loadedClassesCache = new ConcurrentHashMap<>(256); // :92
    private static final URLClassPathHandle urlClassPathHandle;    // :94 (ServiceLoadingURLClassPathHandle)

    @Nonnull public static ClassLoader getDefaultClassLoader();                          // :244
    @Nonnull public static ClassLoader getClassLoader(@Nullable Class<?> loadedClass);   // :298
    @Nullable public static Class<?> loadClass(@Nonnull ClassLoader classLoader, @Nonnull String className);            // :547
    @Nullable public static Class<?> loadClass(@Nonnull ClassLoader classLoader, @Nonnull String className, boolean cached); // :589
    @Nullable public static Class<?> doLoadClass(@Nullable ClassLoader classLoader, @Nullable String className);         // :620
    @Nonnull public static Set<URL> getResources(@Nullable ClassLoader classLoader, @Nonnull String name) throws IOException; // :707
    @Nonnull public static Set<URL> getResources(@Nullable ClassLoader classLoader, @Nonnull ResourceType type, @Nonnull String name) throws IOException; // :669
    @Nullable public static URL getResource(@Nonnull ClassLoader classLoader, @Nonnull String resourceName);            // :783
    @Nullable public static URL getResource(@Nullable ClassLoader classLoader, @Nonnull ResourceType resourceType, @Nonnull String resourceName); // :843
    @Nullable public static URL getClassResource(@Nullable ClassLoader classLoader, @Nonnull Class<?> classType);       // :1035
    @Nullable public static String getResourceAsString(@Nullable ClassLoader classLoader, @Nullable String name);       // :875
    @Nonnull public static Set<ClassLoader> getInheritableClassLoaders(@Nullable ClassLoader classLoader);              // :1069
    @Nonnull public static Set<URL> findAllClassPathURLs(@Nullable ClassLoader classLoader);                            // :1550
    public static boolean removeClassPathURL(@Nullable ClassLoader classLoader, @Nullable URL url);                       // :1598
    @Nullable public static ClassLoader findURLClassLoader(@Nonnull ClassLoader classLoader);                            // :1635
    @Nonnull public static URLClassLoader newURLClassLoader(@Nonnull URL[] urls, @Nonnull ClassLoader parent);           // :1667 (overloads :1699..:1781)
    @Nullable public static Class<?> resolveClass(@Nullable String className);                                            // :1426 (overloads :1466, :1511)
    public static boolean isPresent(@Nonnull String className);                                                            // :1369 / :1399
}
```

| Behaviour | Detail | Evidence |
|---|---|---|
| `null` class loader | `nullSafeClassLoader` substitutes the **system** class loader (`:218`); `getDefaultClassLoader` tries thread-context → this class's loader → default (`:244`) | `util/ClassLoaderUtils.java:218`, `:244` |
| Blank / unresolvable name | `doLoadClass` returns `null` for a blank name and swallows any `Throwable` during loading (logs at trace), then tries to resolve a primitive (`:620`) | `util/ClassLoaderUtils.java:620` |
| Caching | `loadClass(...,true)` memoises into `loadedClassesCache` keyed by `className + classLoader.hashCode()` (`buildCacheKey` `:1902`) | `util/ClassLoaderUtils.java:589`, `:1902` |
| Resource order | `getResource(cl,name)` iterates `ResourceType.values()` and returns the first non-`null`; `getResources` returns the first non-empty set (`:783`, `:707`) | `util/ClassLoaderUtils.java:707`, `:783` |
| Throwing methods | `getResources(...)` declares `IOException` and `NullPointerException` (`:669`, `:707`); `newURLClassLoader(...)` throws `IllegalArgumentException` on null URL elements (`assertNoNullElements`, `:1667`) | `util/ClassLoaderUtils.java:669`, `:1667` |
| Thread safety | `loadedClassesCache` is a `ConcurrentHashMap`; `classLoadingMXBean` and the handle are `static final` — safe to publish (`:90`, `:92`, `:94`) | `util/ClassLoaderUtils.java:90-94` |

`ResourceType` (`util/ClassLoaderUtils.java:1931`) is a nested enum that both validates and normalises a resource name
before the lookup:

| Constant | Accepts | Normalisation | Evidence |
|---|---|---|---|
| `DEFAULT` | anything (`supports()` = `true`) | as-is | `:1945` |
| `CLASS` | names ending in `.class`, or a dotted class name | dots → slashes | `:1983` |
| `PACKAGE` | names with no `/` or `\`, not a `.class` | dot-package form | `:2030` |
| `resolve(name)` | — | returns `null` when blank or unsupported, else normalises and strips a leading `/` | `:2086` |

### `ServiceLoaderUtils` (util/ServiceLoaderUtils.java) — SPI over `java.util.ServiceLoader`

```java
public abstract class ServiceLoaderUtils implements Utils {              // @since 1.0.0
    public static final String SERVICES_PROVIDER_LOCATION = "META-INF/services/";  // :119
    public static final String SERVICE_LOADER_CACHED_PROPERTY_NAME = /* "microsphere." */ "...service-loader.cached"; // :112
    private static final boolean SERVICE_LOADER_CACHED;                  // :138, parsed from -D, default "false" (:107)
    private static final ConcurrentMap<Class<?>, List<?>> servicesCache; // :140

    @Nonnull public static <T> List<T> loadServicesList(@Nonnull Class<T> serviceType, @Nullable ClassLoader cl, boolean cached); // :382..:468
    @Nonnull public static <T> T[] loadServices(@Nonnull Class<T> serviceType, @Nullable ClassLoader cl, boolean cached);          // :493..:576
    @Nullable public static <T> T loadFirstService(@Nonnull Class<T> serviceType, @Nullable ClassLoader cl, boolean cached);       // :602..:693
    @Nullable public static <T> T loadLastService(@Nonnull Class<T> serviceType, @Nullable ClassLoader cl, boolean cached);        // :719..:805
    @Nonnull public static Set<Class<T>> getServiceClasses(@Nonnull Class<T> serviceType, boolean failFast);                        // :262
    @Nonnull public static Set<URL> getServiceResoources(@Nonnull Class<?> serviceType, @Nonnull ClassLoader classLoader);         // :355 (note: source name has the typo)
}
```

| Behaviour | Detail | Evidence |
|---|---|---|
| Ordering | Providers are loaded then sorted with `Prioritized.COMPARATOR`; first/last are `ListUtils.first/last` of that sorted list (`:890`, `:828`) | `util/ServiceLoaderUtils.java:890` |
| Caching key | When `cached` (or the global flag is on), `servicesCache.computeIfAbsent(serviceType, ...)` — keyed by **service type only**, not class loader (`:846`) | `util/ServiceLoaderUtils.java:843-846` |
| Zero providers | `loadServicesAsList(type, cl)` throws `IllegalArgumentException` when the `ServiceLoader` iterator is empty (`:882-886`) — **this is a real throw, not an empty list** | `util/ServiceLoaderUtils.java:882-886` |
| `null` class loader | Falls back to `getClassLoader(...)` (`:873`) | `util/ServiceLoaderUtils.java:873` |
| Non-assignable / unloadable provider | `getServiceClasses(type, true)` throws `IllegalStateException` (`:270`, `:276`); with `failFast=false` it returns an empty set | `util/ServiceLoaderUtils.java:262` |

> [!WARNING]
> The method is named `getServiceResoources` (three `o`s) in the source — copy it exactly (`util/ServiceLoaderUtils.java:355`).
> The cache is keyed by service type only, so **do not** reuse `loadServicesList(type, loaderA, true)` and then
> `loadServicesList(type, loaderB, true)` expecting different results — the second call returns loaderA's cached list
> (`:846`).

### `ClassPathUtils` (util/ClassPathUtils.java) — classpath introspection

```java
public abstract class ClassPathUtils implements Utils {           // @since 1.0.0
    protected static final RuntimeMXBean runtimeMXBean;           // :58
    public static final Set<String> bootstrapClassPaths;          // :61 (immutable, computed once)
    public static final Set<String> classPaths;                   // :64
    @Nonnull public static Set<String> getBootstrapClassPaths();  // :127
    @Nonnull public static Set<String> getClassPaths();           // :149
    @Nullable public static String getRuntimeClassLocation(@Nonnull Class<?> classType); // :207
}
```

| Behaviour | Detail | Evidence |
|---|---|---|
| Bootstrap path on JDK 9+ | `initBootstrapClassPaths` calls `resolveClassPaths(runtimeMXBean.isBootClassPathSupported(), ...)`; when unsupported (JDK 9+) the set is **empty** | `util/ClassPathUtils.java:68`, `:94` |
| Application classpath | `initClassPaths` always splits `getClassPath()` on `File.pathSeparator` | `util/ClassPathUtils.java:73`, `:103` |
| One-time immutability | Both fields are `static final` sets computed at class-init — snapshot values, never refreshed | `util/ClassPathUtils.java:61`, `:64` |

### `Version` (util/Version.java) — immutable version value object

```java
@Immutable
public class Version implements Comparable<Version>, Serializable {   // @since 1.0.0
    private final int major;
    private final int minor;
    private final int patch;
    @Nullable private final String preRelease;                        // :95-102
    public Version(int major, int minor, int patch, @Nullable String preRelease); // :165
    public static Version of(int... versionParts);                    // :441..
    @Nonnull public static Version ofVersion(@Nonnull String version);// :595
    @Nonnull public static Version ofVersion(@Nonnull Class<?> classType); // :626-640
    public boolean gt(@Nullable Version v) /* ge/lt/le/eq + isGreaterThan/… */; // :220-317
    @Override public int compareTo(@Nonnull Version other);           // :372
    public static int getValue(@Nonnull String version);              // :661 (package-private helper)
    public enum Operator implements BiPredicate<Version,Version> { EQ,LT,LE,GT,GE; public static Operator of(String symbol); } // :671, :766
}
```

| Behaviour | Detail | Evidence |
|---|---|---|
| `ofVersion(String)` parsing | splits on `-` for pre-release, then on `.` for `major[.minor[.patch]]`; missing minor/patch default to `0`; extra dot segments are ignored | `util/Version.java:595` |
| Null comparison | every `gt/lt/le/ge/eq` returns `false` for a `null` argument (`Operator` handles `null`) | `util/Version.java:220-317`, `:671` |
| Pre-release rank | a release version ranks **after** its pre-release (`comparePreRelease`: `null` > non-null) | `util/Version.java:403` |
| Invalid input | `ofVersion(String)` throws `IllegalArgumentException` for null/blank; `getValue` throws `IllegalArgumentException` for non-numeric; constructor asserts non-negative and not all-zero | `util/Version.java:165`, `:595`, `:661` |
| From a class | `ofVersion(Class)` uses the caller's loader, else returns `VersionUtils.CURRENT_JAVA_VERSION` for bootstrap-loaded classes; throws `IllegalArgumentException` when the artifact has no version | `util/Version.java:626-640` |

### `VersionUtils` (util/VersionUtils.java) — JDK version constants and tests

```java
public abstract class VersionUtils implements Utils {                 // @since 1.0.0
    public static final SourceVersion LATEST_JAVA_VERSION;            // :39 (javax.lang.model.SourceVersion)
    public static final Version CURRENT_JAVA_VERSION;                 // :44
    public static final Version JAVA_VERSION_8;  /* …through JAVA_VERSION_24 */ // :49-129
    public static boolean testCurrentJavaVersion(@Nonnull String operator, @Nonnull Version version); // :156
    public static boolean testVersion(@Nullable Version base, @Nullable Operator op, @Nullable Version compared); // :248
    public static String currentJavaMajorVersion();                   // :266
    public static String detectJavaMajorVersion(@Nonnull String javaVersion); // :284
}
```

| Behaviour | Detail | Evidence |
|---|---|---|
| Legacy vs modern `java.version` | `detectJavaMajorVersion` treats a leading `"1"` segment as the legacy scheme and returns the next segment (`"1.8.0"` → `"8"`); otherwise returns the segment before the first dot (`"17.0.0"` → `"17"`) | `util/VersionUtils.java:284` |
| Null-safe comparison | `testVersion(...)` returns `false` if any argument is `null` | `util/VersionUtils.java:248` |
| Compile dependency | references `javax.lang.model.SourceVersion` (java.compiler) | `util/VersionUtils.java:39` |

### `StringUtils` (util/StringUtils.java), `ArrayUtils` (util/ArrayUtils.java), `Assert` (util/Assert.java), `SystemUtils` (util/SystemUtils.java)

```java
public abstract class StringUtils implements Utils {                    // :81 isBlank, :113 isNotBlank, :137/:163 split, :221 contains
    public static final String EMPTY = "";                              // :50
    public static final String[] EMPTY_STRING_ARRAY = {};               // :61
    public static final int INDEX_NOT_FOUND = -1;                       // :294
}
public abstract class ArrayUtils implements Utils {                    // :211 of, :375 ofArray, :396 nullSafeArray, :414.. length
    public static final boolean[] EMPTY_BOOLEAN_ARRAY;  /* ~24 EMPTY_* constants */ // :55..
    public static boolean contains(int[] array, int value);             // :1911 (binarySearch — array MUST be sorted)
    public static boolean contains(Object[] array, Object obj);         // :2096 (linear)
}
public abstract class Assert {                                          // :60 assertTrue … :149 assertNotNull … :348 assertNoNullElements (throws IAE)
    public static <T> T assertNotNull(@Nullable T object, String message, Object... args); // :131
}
public abstract class SystemUtils implements Utils {                    // :347 IS_OS_WINDOWS … :357 IS_JAVA_8 … :522 IS_LTS_JAVA_VERSION
    @Nullable public static String getSystemProperty(@Nonnull String key, @Nullable String defaultValue); // :605
}
```

| Type | Null handling | Throws? | Thread-safety |
|---|---|---|---|
| `StringUtils.isBlank` | `null`/empty/whitespace → `true` (`:81`) | no | stateless |
| `StringUtils.split(v, delim)` | `null`/empty value → `EMPTY_STRING_ARRAY`; `null` delim → single-element array of the whole string; empty tokens are kept (`:163`) | no | stateless |
| `ArrayUtils.contains(int[],int)` | returns `false` for `null` array | no | stateless — **but uses `Arrays.binarySearch`, so results are wrong unless the array is sorted** (`:1911`) |
| `Assert.*` | `null` object → throws `IllegalArgumentException` (`:131`) | yes — this class exists to throw | stateless; note `assertNoNullElements` skips `null`/empty arrays rather than failing (`:348`) |
| `SystemUtils.getSystemProperty` | returns the default when the property is missing or a `SecurityException` is caught (`:605`) | no (logs a warning) | `systemPropertiesCopy` is a plain non-`volatile` `Map`; `copySystemProperties()` is `synchronized (SystemUtils.class)` (`:528`, `:548`) |
| `SystemUtils` flags | `IS_JAVA_*` are computed from `java.specification.version` (`:357`, `:638`), not `java.version` | no | immutable `static final` |

> [!NOTE]
> `NumberUtils` (util/NumberUtils.java) is `abstract … implements Utils` with a private constructor and `@since 1.0.0`,
> but currently exposes **no public methods** — it is a reserved placeholder. Do not look for parsing helpers there.

### `text.FormatUtils`, `util.jar.JarUtils`, `misc.UnsafeUtils`

```java
public abstract class FormatUtils implements Utils {                    // text/FormatUtils.java
    public static final String DEFAULT_PLACEHOLDER = "{}";              // :17
    @Nullable public static String format(@Nullable String pattern, Object... args); // :38 → formatWithPlaceholder (:62)
}
public abstract class JarUtils implements Utils {                       // util/jar/JarUtils.java
    public static final String MANIFEST_RESOURCE_PATH = "META-INF/MANIFEST.MF"; // :63
    @Nullable public static JarFile toJarFile(@Nonnull URL url) throws NullPointerException, IllegalArgumentException; // :86
    @Nonnull @Immutable public static List<JarEntry> filter(@Nullable JarFile jarFile, @Nullable JarEntryFilter filter); // :193
}
public abstract class UnsafeUtils implements Utils {                    // misc/UnsafeUtils.java
    public static final String UNSAFE_CLASS_NAME = "sun.misc.Unsafe";   // :34
    private static final ConcurrentMap<String,Long> offsetCache = new ConcurrentHashMap<>(); // :559
}
```

| Type | Behaviour | Evidence |
|---|---|---|
| `FormatUtils.format` | blank pattern → returned as-is; zero args → pattern unchanged; each arg replaces the **first remaining** `{}`; surplus args ignored, surplus placeholders left literal (`:62`, loop `:73-80`) | `text/FormatUtils.java:38`, `:62-80` |
| `JarUtils.toJarFile` | `null` URL → `NullPointerException`; non-`jar:`/`file:` protocol → `IllegalArgumentException` (`assertJarURLProtocol` `:111`); unresolvable archive → `null` (logged, `IOException` swallowed) (`:86`) | `util/jar/JarUtils.java:86`, `:111`, `:168` |
| `JarUtils.filter` | `null` `JarFile` → shared `emptyList()` (`:193`) | `util/jar/JarUtils.java:193` |
| `UnsafeUtils` class-init | `getUnsafe()` reflects `theUnsafe`; on failure throws `UnsupportedOperationException`, which surfaces as `ExceptionInInitializerError` on JDKs without `sun.misc.Unsafe` (`:564`, `:570-572`) | `misc/UnsafeUtils.java:564`, `:570-572` |

### `io.microsphere.constants` — shared literal interfaces

Eight interfaces, all `@since 1.0.0`, aggregated by `Constants` so a single `import static io.microsphere.constants.Constants.*` pulls every literal (`constants/Constants.java:13-14`).

| Interface | Representative constants | Evidence |
|---|---|---|
| `SymbolConstants` | `COMMA = ","` `:184`, `SPACE = " "` `:189`, `DOT = "."` `:233`, `HYPHEN = "-"` `:308`, `EQUAL = "="` `:243`, `LESS_THAN_OR_EQUAL_TO = "<="` `:253`, `GREATER_THAN_OR_EQUAL_TO = ">="` `:263`; char twins (`COMMA_CHAR` `:30`, …) | `constants/SymbolConstants.java` |
| `SeparatorConstants` | `ARCHIVE_ENTRY_SEPARATOR = "!/"` `:26`, `FILE_SEPARATOR` `:31`, `PATH_SEPARATOR` `:36`, `LINE_SEPARATOR` `:41` (from `File`/`System`) | `constants/SeparatorConstants.java` |
| `PathConstants` | `SLASH = "/"` `:28`, `DOUBLE_SLASH` `:33`, `BACK_SLASH` `:38`, `SLASH_CHAR` `:18` | `constants/PathConstants.java` |
| `ProtocolConstants` | `FILE_PROTOCOL = "file"` `:24`, `JAR_PROTOCOL` `:49`, `CLASSPATH_PROTOCOL = "classpath"` `:64`, `CONSOLE_PROTOCOL = "console"` `:69` | `constants/ProtocolConstants.java` |
| `FileConstants` | `JAR = "jar"` `:26`, `FILE_EXTENSION = "."` `:51`, `CLASS_EXTENSION = ".class"` `:76` | `constants/FileConstants.java` |
| `PropertyConstants` | `MICROSPHERE_PROPERTY_NAME_PREFIX = "microsphere."` `:35`, `ENABLED_PROPERTY_NAME = "enabled"` `:30` | `constants/PropertyConstants.java` |
| `ResourceConstants` | `METADATA_RESOURCE = "META-INF/"` `:33`, `MICROSPHERE_METADATA_RESOURCE` `:38` | `constants/ResourceConstants.java` |
| `Constants` | aggregator only (`extends` the six others) | `constants/Constants.java:13-14` |

---

## 4. Runnable examples (adapted from the real tests)

All snippets are condensed from test bodies under `microsphere-java-core/src/test/java/io/microsphere/`. The citation
above each block names the file and lines.

**Parse, compare and rank versions** — `util/VersionTest.java:117-122`, `:190-221`, `:240-242`:

```java
Version v = Version.of("1.2.3");                 // of/ofVersion both parse
assertTrue(v.gt(Version.of(1, 2, 2)));           // strictly greater
assertFalse(v.gt(v));                            // not greater than itself
assertFalse(v.gt(null));                         // null-safe → false, never throws
Version alpha = Version.of("1.2.3-alpha");
assertTrue(v.gt(alpha));                         // release ranks above its pre-release
```

**Resolve a library version from one of its classes** — `util/VersionTest.java:124-137`, `:316-324`:

```java
Version jsr305 = Version.ofVersion(javax.annotation.Nullable.class); // "3.0.2"
assertEquals("3.0.2", jsr305.toString());
assertSame(VersionUtils.CURRENT_JAVA_VERSION, Version.ofVersion(String.class)); // bootstrap loader → running JDK
assertThrows(IllegalArgumentException.class, () -> Version.ofVersion(Version.class)); // no version metadata → throws
```

**Legacy vs modern `java.version` detection** — `util/VersionUtilsTest.java:142-150`:

```java
assertEquals("17", VersionUtils.detectJavaMajorVersion("17.0.0")); // modern
assertEquals("8",  VersionUtils.detectJavaMajorVersion("1.8.0"));  // legacy 1.x scheme
assertEquals("1",  VersionUtils.detectJavaMajorVersion("1"));      // no dot → as-is
```

**Load SPI providers in priority order** — `util/ServiceLoaderUtilsTest.java:81-105`, `:211-232`:

```java
List<EventListener> listeners = ServiceLoaderUtils.loadServicesList(EventListener.class, classLoader, true);
assertEquals(2, listeners.size());
// sorted by Prioritized: EchoEventListener2 wins the "first", EchoEventListener the "last"
assertSame(EchoEventListener2.class, ServiceLoaderUtils.loadFirstService(EventListener.class, classLoader).getClass());
assertSame(EchoEventListener.class,  ServiceLoaderUtils.loadLastService(EventListener.class, classLoader).getClass());
```

**A service with no provider throws (do not rely on an empty result)** — `util/ServiceLoaderUtilsTest.java:107-112`, `:59-63`:

```java
assertThrows(IllegalArgumentException.class, () -> ServiceLoaderUtils.loadServicesList(Set.class)); // zero providers
assertThrows(IllegalStateException.class, () -> ServiceLoaderUtils.getServiceClasses(Utils.class));  // non-assignable, failFast
assertEquals(java.util.Collections.emptySet(), ServiceLoaderUtils.getServiceClasses(Utils.class, false)); // failFast=false degrades
```

**Class-loading degrades to `null`** — `util/ClassLoaderUtilsTest.java:345-356`, `:376`, `:431-438`:

```java
assertNull(ClassLoaderUtils.loadClass(classLoader, "io.microsphere.not.found.class")); // unknown → null
assertNull(ClassLoaderUtils.loadClass(classLoader, ""));                               // blank → null
assertNull(ClassLoaderUtils.loadClass(classLoader, null, true));                       // null name → null (even cached)
assertNull(ClassLoaderUtils.getResourceAsString("not-found"));                         // missing resource → null
```

**Resource lookup by `ResourceType`** — `util/ClassLoaderUtilsTest.java:410-411`:

```java
Set<URL> urls = ClassLoaderUtils.getResources(classLoader, "io.microsphere.util"); // iterates ResourceType.values()
assertNotNull(urls);
```

**`StringUtils.split` keeps empty tokens and is null-safe** — `util/StringUtilsTest.java:89-112`, `:72-86`:

```java
assertTrue(StringUtils.isBlank("   "));                 // whitespace-only
assertSame(StringUtils.EMPTY_STRING_ARRAY, StringUtils.split(null, " "));
assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a,b,c", ","));
assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.split("a,,b,c", ",")); // empty tokens preserved
```

**`FormatUtils` placeholder substitution** — `text/FormatUtilsTest.java:34-53`:

```java
assertNull(FormatUtils.format(null));
assertEquals("A,B,C,D,E", FormatUtils.format("A,{},C,{},E", "B", "D"));
assertEquals("A,B,C,{},E", FormatUtils.format("A,{},C,{},E", "B")); // surplus placeholder stays literal
assertEquals("A,1,C,2,E", FormatUtils.format("A,{},C,{},E", 1, 2, 3)); // 3rd arg ignored
```

**Open a JAR from a resource URL** — `util/jar/JarUtilsTest.java:104-125`, `:99-102`:

```java
URL url = ClassLoaderUtils.getClassResource(classLoader, javax.annotation.Nonnull.class);
assertNotNull(JarUtils.toJarFile(url));                                    // resolves jsr305.jar
assertNull(JarUtils.toJarFile(new URL("jar:file:/path/to/missing.jar!/e")));// unresolvable → null
assertThrows(IllegalArgumentException.class, () -> JarUtils.toJarFile(new URL("http://github.com"))); // bad protocol
assertSame(java.util.Collections.emptyList(), JarUtils.filter(null, null)); // null jar → empty
```

---

## 5. Cross-JDK and compatibility matrix

| Concern | JDK 8 | JDK 9 / 11 | JDK 17 | JDK 21 | JDK 25 | Evidence |
|---|---|---|---|---|---|---|
| `ClassPathUtils.getBootstrapClassPaths()` | populated from `sun.misc` boot CP | **empty** (`isBootClassPathSupported()` = `false`) | empty | empty | empty | `util/ClassPathUtils.java:68`, `:94` |
| `SystemUtils.IS_JAVA_*` | `IS_JAVA_8` true | `IS_JAVA_9/11` … | `IS_JAVA_17` | `IS_JAVA_21` | no dedicated flag past `IS_JAVA_24` | `util/SystemUtils.java:357-517` |
| `VersionUtils.detectJavaMajorVersion("java.version")` | `"1.8.0_x"` → `"8"` | `"9.0.0"`/`"11.0.x"` → major | `"17.x"` | `"21.x"` | `"25.x"` (scheme unchanged) | `util/VersionUtils.java:284` |
| `misc.UnsafeUtils` static init | works (`theUnsafe` present) | works | works (JDK-internal, deprecated) | works | **may fail** → `ExceptionInInitializerError` if `sun.misc.Unsafe` is gone | `misc/UnsafeUtils.java:564`, `:570-572` |
| `ClassLoaderUtils.removeClassPathURL` | `URLClassLoader` (a `SecureClassLoader`) → mutable | system loader is **not** `URLClassLoader`; needs `SecureClassLoader` or returns `false` | same | same | same | `util/ClassLoaderUtils.java:1598-1603` |
| `VersionUtils.LATEST_JAVA_VERSION` | `RELEASE_8` | `RELEASE_11` | `RELEASE_17` | `RELEASE_21` | `RELEASE_25` (delegated to `SourceVersion.latest()`) | `util/VersionUtils.java:39` |

> [!NOTE]
> `IS_JAVA_*` and `CURRENT_JAVA_VERSION` come from different sources: the flags read `java.specification.version`
> (`matchesJavaVersion`, `util/SystemUtils.java:638`) while `CURRENT_JAVA_VERSION` reads `java.version` through
> `currentJavaMajorVersion()` (`util/VersionUtils.java:266`). Under the legacy scheme these agree, but they are
> computed independently.

---

## 6. Pitfalls and known limits

1. **`loadServicesList` / `loadServices` throw when there is no provider.** An empty `ServiceLoader` raises
   `IllegalArgumentException`, not an empty result (`util/ServiceLoaderUtils.java:882-886`). Guard with
   `getServiceClassNames(...)` first, or catch, if the service may be absent.
2. **The services cache ignores the class loader.** `servicesCache` is keyed by service type only
   (`util/ServiceLoaderUtils.java:846`); cached loads across different loaders share one list. Pass `cached = false`
   when loader isolation matters.
3. **`ArrayUtils.contains(int[], int)` and the other numeric overloads need a *sorted* array.** They delegate to
   `Arrays.binarySearch` (`util/ArrayUtils.java:1911-2069`); on an unsorted array the answer can be wrong. The
   `boolean[]` and `Object[]` overloads use a linear scan and are order-independent (`:1887`, `:2096`).
4. **`ClassPathUtils` snapshots are computed once.** `bootstrapClassPaths` and `classPaths` are `static final`
   (`util/ClassPathUtils.java:61`, `:64`) and never refresh; on JDK 9+ the boot set is empty by design, so filtering
   JDK libraries via the boot path only works on JDK 8.
5. **`Assert.assertNoNullElements` skips `null`/empty input.** It only inspects elements when the array/iterable is
   present and non-empty (`util/Assert.java:348`), so a `null` array passes. Use `Assert.assertNotNull` when `null`
   itself must fail.
6. **`UnsafeUtils` can blow up at class-load time.** If `sun.misc.Unsafe` is unavailable, `getUnsafe()` throws
   `UnsupportedOperationException` from a static initializer (`misc/UnsafeUtils.java:570-572`), which the JVM reports as
   `ExceptionInInitializerError` — not a value you can `catch` at the call site. Treat `UnsafeUtils` as JDK-version
   gated.
7. **`ofVersion(Class)` throws for unversioned artifacts.** A class whose archive carries no version metadata produces
   `IllegalArgumentException` (`util/Version.java:626-640`); exploded `target/classes` directories are the common case.
8. **`JarUtils.toJarFile` mixes returns and throws.** Missing archives return `null` (`:86`) but `null` URLs throw
   `NullPointerException` and bad protocols throw `IllegalArgumentException` (`assertJarURLProtocol`, `:111`) — the
   three outcomes are easy to conflate.
9. **Blank input degrades silently in class loading.** `doLoadClass` returns `null` for blank names and swallows any
   `Throwable` while loading (`util/ClassLoaderUtils.java:620`); a `null` result does not by itself prove the class
   does not exist (it could indicate a linkage error) — check logs at trace level if needed.
10. **`getAllLoadedClassesMap` uses the top loader for every key.** When mapping each inheritable loader to its loaded
    classes, the loop passes the *caller-supplied* loader rather than the iteration variable
    (`util/ClassLoaderUtils.java:1142-1144`), so the map values reflect one loader. Do not use it to attribute classes
    to their individual defining loaders.

---

## 7. See also

* [Class Loading and Artifacts](classloading-and-artifacts.md) — the `URLClassPathHandle` SPI that `ClassLoaderUtils` delegates to
* [Collections and Filters](collections-and-filters.md) — `Lists`/`Sets`/`Prioritized` used by `ServiceLoaderUtils` sorting
* [Networking and URL Protocols](net-and-url-protocols.md) — `URLUtils` behind `JarUtils` archive-path resolution
* [Language Abstractions](language-abstractions.md) — `io.microsphere.lang.Prioritized`, the ordering contract these utilities honour
* Developer-facing deep dive: [Utilities, Constants and Text — Developer Guide](../../developer-guide/microsphere-java-core/util-and-constants.md)
