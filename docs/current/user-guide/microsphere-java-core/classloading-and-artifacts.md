# Class Loading and Artifact Detection

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-java-core` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Package** | `io.microsphere.classloading` — 16 main-source types |
| **JDK level** | Compiled at Java 8; CI-tested on JDK 8 / 11 / 17 / 21 / 25 |
| **Depends on `io.microsphere.util`** | `ClassLoaderUtils` (`findAllClassPathURLs`, `getClassResource`, `getClassLoader`, `removeClassPathURL`), `ClassPathUtils.getBootstrapClassPaths()`, `ServiceLoaderUtils.loadServicesList`, `ObjectUtils`, `StringUtils`, `ArrayUtils`, `Assert`, `util.jar.JarUtils` |
| **Other `core` packages used** | `io.microsphere.net.URLUtils` (archive/base-path parsing), `io.microsphere.reflect` (field + method access), `io.microsphere.io` / `io.scanner`, `io.microsphere.logging`, `io.microsphere.constants`, `io.microsphere.lang.Prioritized`, `io.microsphere.annotation.*` (from `microsphere-java-annotations`) |
| **External dependencies** | none — detection is pure JDK + SPI |

This package answers one runtime question — *which artifact (groupId / artifactId / version) does this class, resource
or classpath entry come from?* — and adds the machinery to **remove** a classpath entry when an artifact must not be
present (`BannedArtifactClassLoadingExecutor`).

---

## 1. Version information and compatibility range

### 1.1 How classpath entries are read, per JDK

`ArtifactDetector` never parses `java.class.path` itself. It calls
`ClassLoaderUtils.findAllClassPathURLs(classLoader)`, which walks the class-loader parent chain and delegates each
loader to the single, statically-held `ServiceLoadingURLClassPathHandle`
(`microsphere-java-core/src/main/java/io/microsphere/util/ClassLoaderUtils.java:94`, `:1550-1564`). That facade picks
**one** registered `URLClassPathHandle` — the first whose `supports()` returns `true` after priority sorting
(`classloading/ServiceLoadingURLClassPathHandle.java:36-43`).

| | JDK 8 | JDK 11 | JDK 17 | JDK 21 | JDK 25 |
|---|---|---|---|---|---|
| System loader type | `sun.misc.Launcher$AppClassLoader` (a `URLClassLoader`) | `jdk.internal.loader.ClassLoaders$AppClassLoader` | same | same | same |
| Handle that wins | `ClassicURLClassPathHandle` (`sun.misc.URLClassPath`) | `ModernURLClassPathHandle` (`jdk.internal.loader.URLClassPath`) | Modern | Modern | Modern |
| URL field read | `urls` | `unopenedUrls` | `unopenedUrls` | `unopenedUrls` | `unopenedUrls` |
| Entry source | classpath URL parsing + `URLClassPath#getURLs()` reflection | `URLClassPath#getURLs()` reflection; `jrt:` / module-path entries surface as URLs from the loader, **not** from JPMS module reading | same | same | same |
| `removeURL(...)` target | `URLClassLoader.ucp` | `BuiltinClassLoader.ucp` (needs an open `jdk.internal.loader`) | same | same | same |
| If neither handle resolves its class | — | `NoOpURLClassPathHandle` → `getURLs()` returns the shared empty array, `removeURL()` returns `false` | same | same | same |
| Winning resolver order | Maven(1) → Manifest(5) → ArchiveFile(9) | identical | identical | identical | identical |

Two facts worth pinning down:

* `ClassicURLClassPathHandle.supports()` is exactly "`sun.misc.URLClassPath` is loadable" and
  `ModernURLClassPathHandle.supports()` is "`jdk.internal.loader.URLClassPath` is loadable"
  (`AbstractURLClassPathHandle.java:114-117`, `:178-186`). The test suite states the same rule from the outside:
  `assertEquals(CURRENT_JAVA_VERSION.le(JAVA_VERSION_8), handle.supports())`
  (`.../test/java/io/microsphere/classloading/ClassicURLClassPathHandleTest.java:44-47`) and
  `assertEquals(CURRENT_JAVA_VERSION.ge(JAVA_VERSION_9), handle.supports())`
  (`ModernURLClassPathHandleTest.java:41-43`).
* **No type in this package reads JPMS module descriptors.** On JDK 9+ everything still flows through the loader's
  `URLClassPath`, so module-path and `jrt:` entries are visible only insofar as the loader exposes them as URLs.
  JDK-library filtering therefore degrades on 9+: `ArtifactDetector.removeJdkClassPathURLs(...)`
  (`ArtifactDetector.java:164-175`) drops entries contained in `ClassPathUtils.getBootstrapClassPaths()` **or**
  containing `java.home`. On JDK 9+ `RuntimeMXBean.isBootClassPathSupported()` is `false`, so the boot set is empty
  (`ClassPathUtils.java:68-70`, `:111-112`) and only the `JAVA_HOME` containment check remains effective.

### 1.2 Configuration: system properties and other tunables

| Name | Read by | Default | Notes |
|---|---|---|---|
| `microsphere.artifact-id.manifest-attribute-names` | `ManifestArtifactResourceResolver` (`:128-134`) | `Bundle-Name,Automatic-Module-Name,Implementation-Title` | comma-separated, first non-null attribute wins |
| `microsphere.artifact-version.manifest-attribute-names` | `ManifestArtifactResourceResolver` (`:147-153`) | `Bundle-Version,Implementation-Version` | comma-separated, first non-null wins |
| `microsphere.service-loader.cached` | `ServiceLoaderUtils` (`:104-111`), affects `URLClassPathHandle` discovery | `false` | `ArtifactDetector` requests its resolvers with `cached = true` explicitly (`ArtifactDetector.java:92`) |
| `java.home` | `ArtifactDetector.JAVA_HOME_PATH` (`:26`, `:80`) | — | non-JDK filter; captured once per JVM |
| `file.encoding` | `BannedArtifactClassLoadingExecutor.ENCODING` (`:21`, `:69`) | — | charset used to read `META-INF/banned-artifacts` |

> [!IMPORTANT]
> The manifest attribute lists and `JAVA_HOME` are resolved in **static initializers**
> (`ManifestArtifactResourceResolver.java:162-164`, `ArtifactDetector.java:80`). Set them with `-D` on the command
> line; changing them later with `System.setProperty` has no effect on an already-loaded class.

```bash
java -Dmicrosphere.artifact-version.manifest-attribute-names=Implementation-Version,Specification-Version \
     -jar app.jar
```

### 1.3 `@Since` history

Fourteen of the sixteen types record Javadoc `@since 1.0.0` (all `Artifact*`, `*URLClassPathHandle`,
`BannedArtifactClassLoadingExecutor`, `StreamArtifactResourceResolver`). **`Artifact` and `ArtifactResourceResolver`
record no version at all in their source**, and the package uses no `@Since` annotation, so for those two the first
released version cannot be established from the code. `release-notes.md` records two later, behaviour-visible
changes: the `ArtifactDetector` detection API was refactored in **0.2.7** (`release-notes.md:5-15`), and
`StreamArtifactResourceResolver` was refactored together with `JarUtils` in **0.3.4** (`release-notes.md:135-152`).

---

## 2. API reference

Signatures below are copied from the sources on `release`.

```java
// Artifact.java — immutable value object; "*" = wildcard, "?" = UNKNOWN
public class Artifact {
    public static final String WILDCARD;           // "*"
    public static final String UNKNOWN;            // "?"
    public Artifact(@Nonnull String artifactId, @Nullable String version, @Nullable URL location)
    public static Artifact create(@Nonnull String artifactId)                                  // version = UNKNOWN
    public static Artifact create(@Nonnull String artifactId, @Nullable String version)
    public static Artifact create(@Nonnull String artifactId, @Nullable String version, @Nullable URL location)
    @Nonnull public String getArtifactId()
    @Nullable public String getVersion()
    @Nullable public URL getLocation()
    public boolean matches(Artifact artifact)      // artifactId AND version
    protected boolean matchesArtifactId(Artifact artifact)
    protected boolean matchesVersion(Artifact artifact)
    protected boolean matches(Artifact artifact, Function<Artifact, String> getterFunction)
}

// MavenArtifact.java — adds groupId to identity and matching
public class MavenArtifact extends Artifact {
    public MavenArtifact(@Nonnull String groupId, @Nonnull String artifactId,
                         @Nullable String version, @Nullable URL location)
    public static MavenArtifact create(@Nonnull String groupId, @Nonnull String artifactId)      // version = UNKNOWN
    public static MavenArtifact create(String groupId, String artifactId, @Nullable String version)
    public static MavenArtifact create(String groupId, String artifactId, @Nullable String version, @Nullable URL location)
    @Nonnull public String getGroupId()
    @Override public boolean matches(Artifact artifact)   // groupId AND artifactId AND version
}

// ArtifactDetector.java — the entry point; instance-based, no static shortcuts
public class ArtifactDetector {
    public ArtifactDetector()                                       // this class's class loader
    public ArtifactDetector(@Nullable ClassLoader classLoader)      // null -> getClassLoader(ArtifactDetector.class)
    @Nonnull @Immutable public List<Artifact> detect()              // detect(true): JDK libraries INCLUDED
    @Nonnull @Immutable public List<Artifact> detect(boolean includedJdkLibraries)
    @Nonnull @Immutable public List<Artifact> detect(@Nullable Set<URL> classPathURLs)
    @Nullable public Artifact detect(@Nonnull Class<?> classInResource)
    @Nullable public Artifact detect(@Nonnull URL classPathURL)     // first non-null resolver wins
    protected Set<URL> getClassPathURLs(boolean includedJdkLibraries)
}

// ArtifactResourceResolver.java — the SPI
public interface ArtifactResourceResolver extends Prioritized {
    @Nullable Artifact resolve(@Nullable URL resourceURL);
}
public abstract class AbstractArtifactResourceResolver implements ArtifactResourceResolver {
    public AbstractArtifactResourceResolver(int priority)                       // default class loader
    public AbstractArtifactResourceResolver(ClassLoader classLoader, int priority)
    @Override public final int getPriority()          // final: set it via the constructor
    protected final Logger logger; protected final ClassLoader classLoader; protected final int priority;
}
public abstract class StreamArtifactResourceResolver extends AbstractArtifactResourceResolver {
    public StreamArtifactResourceResolver(int priority)
    public StreamArtifactResourceResolver(ClassLoader classLoader, int priority)  // null classLoader -> IllegalArgumentException
    @Override public final Artifact resolve(URL resourceURL)   // final: locates and opens the metadata stream
    protected abstract boolean isArtifactMetadata(String relativePath);
    protected abstract Artifact resolve(URL resourceURL, InputStream artifactMetadataData,
                                        ClassLoader classLoader) throws IOException;
}
public class MavenArtifactResourceResolver extends StreamArtifactResourceResolver {
    public static final int DEFAULT_PRIORITY = 1;
    public static final String MAVEN_POM_PROPERTIES_RESOURCE_PREFIX = "META-INF/maven/";
    public static final String MAVEN_POM_PROPERTIES_RESOURCE_SUFFIX = "/pom.properties";
    public MavenArtifactResourceResolver() / (int priority) / (ClassLoader, int)
}
public class ManifestArtifactResourceResolver extends StreamArtifactResourceResolver {
    public static final int DEFAULT_PRIORITY = 5;
    public ManifestArtifactResourceResolver() / (int priority) / (ClassLoader, int)
}
public class ArchiveFileArtifactResourceResolver extends AbstractArtifactResourceResolver {
    public static final int DEFAULT_PRIORITY = 9;
    public ArchiveFileArtifactResourceResolver() / (int priority) / (ClassLoader, int)
    @Override public Artifact resolve(URL resourceURL)   // NOT final
}

// URLClassPathHandle family
public interface URLClassPathHandle extends Prioritized {
    boolean supports();
    @Nonnull default URL[] getURLs(@Nullable ClassLoader classLoader)         // via findURLClassLoader, else empty
    default boolean initializeLoaders(@Nullable ClassLoader classLoader)      // findResource("just-for-initializing-loaders")
    boolean removeURL(@Nullable ClassLoader classLoader, @Nullable URL url);
    @Override default int getPriority() { return MIN_PRIORITY; }
}
public abstract class AbstractURLClassPathHandle implements URLClassPathHandle, Prioritized {
    public static final int DEFAULT_PRIORITY = MAX_PRIORITY + 99999;   // Integer.MIN_VALUE + 99999: highest precedence
    @Override public boolean supports()                                // URLClassPath class resolvable
    @Override public URL[] getURLs(ClassLoader classLoader)             // ucp.getURLs() reflectively
    @Override public final boolean removeURL(ClassLoader classLoader, URL url)  // synchronized on urls; false only for null args (see pitfall 6)
    public final void setPriority(int priority); public final int getPriority();
    protected abstract String getURLClassPathClassName();
    protected abstract String getUrlsFieldName();
}
public class ClassicURLClassPathHandle extends AbstractURLClassPathHandle {}   // sun.misc.URLClassPath / "urls"
public class ModernURLClassPathHandle extends AbstractURLClassPathHandle {}    // jdk.internal.loader.URLClassPath / "unopenedUrls"
public class NoOpURLClassPathHandle implements URLClassPathHandle {}           // supports()=true, empty URLs, remove=false
public class ServiceLoadingURLClassPathHandle implements URLClassPathHandle {} // SPI facade used by ClassLoaderUtils

// BannedArtifactClassLoadingExecutor.java
public class BannedArtifactClassLoadingExecutor {
    public static final String CONFIG_LOCATION = "META-INF/banned-artifacts";
    public BannedArtifactClassLoadingExecutor() / (@Nullable ClassLoader classLoader)
    public void execute()          // detect non-JDK artifacts, remove matching classpath URLs
}
```

### 2.1 Registered SPI implementations (verified from the service files)

`microsphere-java-core/src/main/resources/META-INF/services/io.microsphere.classloading.ArtifactResourceResolver`

```
io.microsphere.classloading.MavenArtifactResourceResolver
io.microsphere.classloading.ManifestArtifactResourceResolver
io.microsphere.classloading.ArchiveFileArtifactResourceResolver
```

`microsphere-java-core/src/main/resources/META-INF/services/io.microsphere.classloading.URLClassPathHandle`

```
io.microsphere.classloading.ClassicURLClassPathHandle
io.microsphere.classloading.ModernURLClassPathHandle
io.microsphere.classloading.NoOpURLClassPathHandle
```

`StreamArtifactResourceResolver`, `AbstractArtifactResourceResolver` and `ServiceLoadingURLClassPathHandle` are **not**
registered — the first two are bases, the third is instantiated directly by `ClassLoaderUtils`.

| Resolver | Priority | Reads | Produces |
|---|---|---|---|
| `MavenArtifactResourceResolver` | 1 | the first `META-INF/maven/**/pom.properties` **at the archive root** | `MavenArtifact` (real `groupId`) |
| `ManifestArtifactResourceResolver` | 5 | `META-INF/MANIFEST.MF` main attributes | `Artifact`, or `null` if no artifact-id attribute matches |
| `ArchiveFileArtifactResourceResolver` | 9 | the archive **file name**, `[artifactId]-[version].ext` | `Artifact`; version `null` when the name has no hyphen |

---

## 3. Examples (adapted from the real tests)

**Detect the artifact that supplies a class** — from
`microsphere-java-core/src/test/java/io/microsphere/classloading/ArtifactDetectorTest.java:53-65`:

```java
ArtifactDetector detector = new ArtifactDetector();
Artifact self = detector.detect(ArtifactDetector.class);   // null in an exploded target/classes dir
Artifact jsr305 = detector.detect(javax.annotation.Nonnull.class);
// test asserts: jsr305.getArtifactId() == "jsr305", jsr305.getVersion() == "3.0.2"
```

**Full classpath scan, with and without JDK entries** — same file, `:46-51` and `:90-101`:

```java
List<Artifact> all = detector.detect();                    // detect(true) — JDK entries included
List<Artifact> appOnly = detector.detect(false);           // bootstrap + java.home entries removed
appOnly.forEach(a -> System.out.printf("%s %s%n", a.getArtifactId(), a.getVersion()));
```

**Resolving one archive directly** — `MavenArtifactResourceResolverTest.java:16-24` and
`StreamArtifactResourceResolverTest.java:46-67`:

```java
ArtifactResourceResolver resolver = new MavenArtifactResourceResolver();
Artifact artifact = resolver.resolve(classResourceUrlOf(javax.annotation.Nonnull.class));
assertTrue(artifact instanceof MavenArtifact);             // com.google.code.findbugs:jsr305:3.0.2
```

**File-name fallback** — `ArchiveFileArtifactResourceResolverTest.java:50-63`:

```java
// package-private helper exercised by the test
Artifact a = ArchiveFileArtifactResourceResolver.createArtifact("a-1.0.jar", null); // a / "1.0"
Artifact b = ArchiveFileArtifactResourceResolver.createArtifact("a.jar", null);     // a / null
```

**Remove a classpath entry** — `ServiceLoadingURLClassPathHandleTest.java:61-69` copies the current classpath into a
throw-away `URLClassLoader` and removes every entry through the handle; `BaseURLClassPathHandleTest.java:58-69`
removes the `jmh-generator-annprocess` jar from the test class loader only when `handle.supports()`.

**Ban an artifact at startup** — `BannedArtifactClassLoadingExecutorTest.java:38-42`:

```java
new BannedArtifactClassLoadingExecutor(classLoader).execute();
```

```
# src/main/resources/META-INF/banned-artifacts — exactly three colon-separated fields per line
commons-logging:commons-logging:1.2
log4j:log4j:*
```

---

## 4. Pitfalls

1. **Shaded / fat jars.** `StreamArtifactResourceResolver.findArtifactMetadataEntry(...)` returns the **first**
   matching entry of the archive (`StreamArtifactResourceResolver.java:179-182`), so a shaded jar reports whichever
   bundled `pom.properties` the `JarFile` enumeration happens to yield first — not your artifact's coordinates. Relocate
   the metadata or accept the Manifest/file-name fallback.
2. **Nested jars are invisible.** `MavenArtifactResourceResolver.isArtifactMetadata(...)` requires
   `relativePath.indexOf("META-INF/maven/") == 0` (`MavenArtifactResourceResolver.java:101-110`). Spring Boot's
   `BOOT-INF/lib/*.jar` entries never satisfy it, and for a `jar:file:/app.jar!/BOOT-INF/lib/x.jar!/` URL
   `URLUtils.resolveArchiveFile(...)` yields a `File` that does not exist, so `resolveArchiveFile` returns `null` and
   `readArtifactMetadataDataFromResource(...)` returns `null` by default
   (`StreamArtifactResourceResolver.java:112-139`). Result: **no** artifact for nested entries.
3. **OSGi / custom launchers.** Bundles usually expose only `MANIFEST.MF`, so the `Bundle-Name` / `Bundle-Version`
   attributes carry the identification; if your bundles use `Bundle-SymbolicName` instead, add it via
   `microsphere.artifact-id.manifest-attribute-names`, otherwise the resolver returns `null`
   (`ManifestArtifactResourceResolver.java:202-214`).
4. **Exploded directories.** Classes running from `target/classes`, `build/classes` or an IDE output dir typically
   resolve to `null` (asserted in `ArtifactDetectorTest.java:58-59`); the file-name resolver rejects directories
   outright (`ArchiveFileArtifactResourceResolver.java:86-89`).
5. **JDK 9+ system loaders are not `URLClassLoader`.** `URLClassPathHandle.getURLs(...)` default falls back to
   `ClassLoaderUtils.findURLClassLoader(...)`, which returns `null` → `EMPTY_URL_ARRAY`. The `Abstract` handles get
   around this by reading the private `ucp` field, but `ClassLoaderUtils.removeClassPathURL(...)` still refuses any
   loader that is not a `SecureClassLoader` (`ClassLoaderUtils.java:1598-1603`).
6. **Strong encapsulation makes `removeURL` throw, not return `false`.** The private `jdk.internal.loader` internals are
   not open by default on JDK 17/21/25. If you call `removeURL(...)` (directly or through
   `ClassLoaderUtils.removeClassPathURL(...)`) against the application loader without
   `--add-opens java.base/jdk.internal.loader=ALL-UNNAMED`, the reflective read is rejected and the call propagates
   `IllegalStateException` (wrapping `IllegalAccessException`, `FieldUtils.java:826-841`); if the loader simply has no
   such field, `removeURL` raises `NullPointerException` at `AbstractURLClassPathHandle.java:147`. Only `null`
   arguments produce a clean `false`. Launch with the `--add-opens` flag, or catch around the call.
7. **`detect(true)` includes JDK entries with `UNKNOWN`/manifest-derived ids.** This is expected output, not a bug;
   use `detect(false)` for an application-only view.
8. **Banned-artifacts parsing stops early.** Reading halts at the first blank line
   (`BannedArtifactClassLoadingExecutor.java:117-125`) and any line without exactly three fields throws
   `IllegalArgumentException` (`:129-138`). Comments are **not** supported — a `# ...` line will fail.
9. **`execute()` never reports a ban.** A matched artifact is dropped through
   `ClassLoaderUtils.removeClassPathURL(...)` (`:84-95`) and the return value is ignored. It returns `false` — a silent
   no-op — when the loader is not a `SecureClassLoader`, but it *throws* when the loader is one whose internals are
   encapsulated (see pitfall 6), because `execute()` catches nothing. Verify removals in integration tests if you rely
   on this as a guardrail.
10. **Wildcards are per-field.** `Artifact.WILDCARD` short-circuits one field only
    (`Artifact.java:115-122`) — `*:*:*` bans everything, `foo:*:1.0` bans `foo` at any version.

---

## 5. See also

* [Utilities, constants and text](util-and-constants.md) — `ClassLoaderUtils`, `ClassPathUtils`, `ServiceLoaderUtils`
* [Networking and URL protocols](net-and-url-protocols.md) — `URLUtils.resolveArchiveFile` / `resolveBasePath`
* [Reflection, types and beans](reflection-and-beans.md) — the field access the `Abstract*URLClassPathHandle` types need
* [Language abstractions](language-abstractions.md) — `Prioritized`, which orders both SPI families
* Developer-facing deep dive: [Class Loading and Artifact Detection — Developer Guide](../../developer-guide/microsphere-java-core/classloading-and-artifacts.md)
