# Class Loading and Artifacts

**Module**: `io.github.microsphere-projects:microsphere-java-core`
**Package**: `io.microsphere.classloading`

Answers one question at runtime: *which artifact (groupId/artifactId/version) does this class, resource or classpath
entry come from?* — and provides the machinery to add or remove classpath entries across JDKs.

---

## 1. `Artifact` and `MavenArtifact`

```java
@Immutable
public class Artifact {

    public static final String WILDCARD;
    public static final String UNKNOWN = "?";

    public Artifact(@Nonnull String artifactId, @Nullable String version, @Nullable URL location)

    public static Artifact create(@Nonnull String artifactId)
    public static Artifact create(@Nonnull String artifactId, @Nullable String version)
    public static Artifact create(@Nonnull String artifactId, @Nullable String version, @Nullable URL location)

    @Nonnull public String getArtifactId()
    @Nullable public String getVersion()
    @Nullable public URL getLocation()

    public boolean matches(Artifact artifact)
    protected boolean matchesArtifactId(Artifact artifact)
    protected boolean matchesVersion(Artifact artifact)
    protected boolean matches(Artifact artifact, Function<Artifact, String> getterFunction)
}

public class MavenArtifact extends Artifact {
    public MavenArtifact(@Nonnull String groupId, @Nonnull String artifactId,
                         @Nullable String version, @Nullable URL location)
    public static MavenArtifact create(String groupId, String artifactId, String version, URL location)
    public static MavenArtifact create(String groupId, String artifactId, String version)
    public static MavenArtifact create(String groupId, String artifactId)
    @Nonnull public String getGroupId()
    @Override public boolean matches(Artifact artifact)
}
```

`matches` treats `WILDCARD` as "anything" per field, and `UNKNOWN` (`"?"`) is the placeholder resolvers use when a
manifest or POM has no version. This is what makes an exclusion pattern like `some-lib` + `*` workable.

```java
Artifact artifact = new ArtifactDetector().detect(MyService.class);
if (artifact != null && artifact.matches(Artifact.create("microsphere-java-core", null))) {
    // running against microsphere-java-core, any version
}
```

---

## 2. `ArtifactDetector` — the entry point

```java
public class ArtifactDetector {

    public ArtifactDetector()                                       // default class loader
    public ArtifactDetector(@Nullable ClassLoader classLoader)

    @Nonnull @Immutable public List<Artifact> detect()               // excludes JDK libraries
    @Nonnull @Immutable public List<Artifact> detect(boolean includedJdkLibraries)
    @Nonnull @Immutable public List<Artifact> detect(@Nullable Set<URL> classPathURLs)

    @Nullable public Artifact detect(@Nonnull Class<?> classInResource)
    @Nullable public Artifact detect(@Nonnull URL classPathURL)

    protected Set<URL> getClassPathURLs(boolean includedJdkLibraries)
}
```

> [!IMPORTANT]
> `ArtifactDetector` is **instance-based**; there are no static shortcuts. Construct it once — each `detect()` walks
> the whole classpath through every registered resolver.

How it works:

1. classpath URLs come from [`ClassLoaderUtils.findAllClassPathURLs(classLoader)`](core-utilities.md#3-classloaderutils);
2. every `ArtifactResourceResolver` from SPI is loaded and priority-sorted with `ServiceLoaderUtils.loadServicesList`;
3. for each URL the first resolver that returns a non-`null` `Artifact` wins.

```java
ArtifactDetector detector = new ArtifactDetector(classLoader);

List<Artifact> classpath = detector.detect(true);      // includes JDK modules
classpath.forEach(a -> System.out.printf("%s:%s %s%n",
        a.getArtifactId(), a.getVersion(), a.getLocation()));

Artifact mine = detector.detect(MyService.class);       // where does this class come from?
```

On JDK 9+ the `detect(true)` path sees `jrt:`-style entries, and the resolvers are the same — you will get `UNKNOWN`
versions for many JDK entries, which is expected rather than a bug.

---

## 3. The `ArtifactResourceResolver` SPI

```java
public interface ArtifactResourceResolver extends Prioritized {
    @Nullable Artifact resolve(@Nullable URL resourceURL);
}

public abstract class AbstractArtifactResourceResolver implements ArtifactResourceResolver {
    public AbstractArtifactResourceResolver(int priority)
    public AbstractArtifactResourceResolver(ClassLoader classLoader, int priority)
    @Override public final int getPriority()                  // fixed at construction
}

public abstract class StreamArtifactResourceResolver extends AbstractArtifactResourceResolver {
    public StreamArtifactResourceResolver(ClassLoader classLoader, int priority)
    @Override public final Artifact resolve(URL resourceURL)   // FINAL: opens the metadata stream for you
    @Nullable protected abstract Artifact resolve(URL resourceURL, InputStream artifactMetadataData,
                                                 ClassLoader classLoader) throws IOException;
}
```

Registered in `META-INF/services/io.microsphere.classloading.ArtifactResourceResolver`:

| Resolver | Reads | Produces |
|---|---|---|
| `MavenArtifactResourceResolver` | `META-INF/maven/**/pom.properties` inside the archive | `MavenArtifact` with real `groupId` |
| `ManifestArtifactResourceResolver` | the JAR `MANIFEST.MF` attributes | `Artifact` (see properties below) |
| `ArchiveFileArtifactResourceResolver` | the archive **file name** | `Artifact`, version parsed from the file name |

(Also present: `StreamArtifactResourceResolver` as the template base, and `AbstractArtifactResourceResolver`.)

> [!NOTE]
> `AbstractArtifactResourceResolver.getPriority()` is `final` — you set priority through the constructor, you do not
> override `getPriority()`. And `StreamArtifactResourceResolver.resolve(URL)` is `final`: subclasses implement the
> three-argument form that already has the metadata `InputStream`.

### Manifest attribute mapping

```java
// ManifestArtifactResourceResolver
"microsphere.artifact-id.manifest-attribute-names"
    default: "Bundle-Name,Automatic-Module-Name,Implementation-Title"

"microsphere.artifact-version.manifest-attribute-names"
    default: "Bundle-Version,Implementation-Version"
```

Both are comma-separated lists tried in order. If your artifacts carry `Specification-Version` or a custom
attribute, add it:

```bash
-Dmicrosphere.artifact-version.manifest-attribute-names=Implementation-Version,Bundle-Version,Specification-Version
```

### Adding your own resolver

```java
public class MyLockFileArtifactResolver extends AbstractArtifactResourceResolver {

    public MyLockFileArtifactResolver() {
        super(Prioritized.NORMAL_PRIORITY - 1);      // before the built-ins
    }

    @Override
    public Artifact resolve(URL resourceURL) {
        // inspect resourceURL, return null to let the next resolver try
        return null;
    }
}
```

```
# META-INF/services/io.microsphere.classloading.ArtifactResourceResolver
com.example.MyLockFileArtifactResolver
```

---

## 4. `URLClassPathHandle` — reading and mutating the classpath

```java
public interface URLClassPathHandle extends Prioritized {

    boolean supports();
    @Nonnull default URL[] getURLs(@Nullable ClassLoader classLoader)
    default boolean initializeLoaders(@Nullable ClassLoader classLoader)
    boolean removeURL(@Nullable ClassLoader classLoader, @Nullable URL url)

    @Override default int getPriority() { return MIN_PRIORITY; }
}

public abstract class AbstractURLClassPathHandle implements URLClassPathHandle, Prioritized {

    public static final int DEFAULT_PRIORITY = Prioritized.MAX_PRIORITY + 99999;

    public AbstractURLClassPathHandle()
    @Override public boolean supports()
    @Override public URL[] getURLs(ClassLoader classLoader)
    @Override public final boolean removeURL(ClassLoader classLoader, URL url)
    public final void setPriority(int priority)
    public final int getPriority()

    protected abstract String getURLClassPathClassName();
    protected abstract String getUrlsFieldName();
}
```

| Implementation | Target |
|---|---|
| `ClassicURLClassPathHandle` | `sun.misc.URLClassPath` (JDK 8 `URLClassLoader` hierarchy) |
| `ModernURLClassPathHandle` | `jdk.internal.loader.URLClassPath` (JDK 9+) |
| `NoOpURLClassPathHandle` | always-available no-op (lowest priority fallback) |
| `ServiceLoadingURLClassPathHandle` | the SPI-loading facade that picks among the handles |

`supports()` decides usability, so `NoOpURLClassPathHandle` exists to keep the chain non-empty;
`getURLs(...)`/`removeURL(...)` never throw when nothing supports the current JVM — they just do nothing. The two
abstract `protected` name hooks are why adding support for a future JDK is a ~20-line subclass.

Related, higher-level helpers (in `io.microsphere.util`):
[`ClassLoaderUtils.newURLClassLoader(...)`](core-utilities.md#3-classloaderutils) for adding entries to a new loader
and `ClassLoaderUtils.removeClassPathURL(ClassLoader, URL)` for removal through this SPI.

> [!WARNING]
> Mutating a class loader's classpath is inherently unsupported on JDK 9+ for the *system* loaders
> (`jdk.internal.loader.ClassLoaders$AppClassLoader` is not a `URLClassLoader`). These handles work for
> `URLClassLoader` instances you created; expect `removeURL` to be a no-op elsewhere.

---

## 5. `BannedArtifactClassLoadingExecutor`

Fails fast when a banned artifact is present on the classpath — a build-time rule enforced at runtime.

```java
public class BannedArtifactClassLoadingExecutor {

    public static final String CONFIG_LOCATION = "META-INF/banned-artifacts";

    public BannedArtifactClassLoadingExecutor()
    public BannedArtifactClassLoadingExecutor(@Nullable ClassLoader classLoader)

    public void execute()
}
```

Provide one artifact coordinate per line at `META-INF/banned-artifacts` in your application:

```
commons-logging
log4j:1.2.17
```

`execute()` runs `ArtifactDetector` over the class loader and throws when a detected `Artifact`
[`matches`](#1-artifact-and-mavenartifact) an entry. Because matching uses `WILDCARD`/`UNKNOWN` semantics, a bare
`artifactId` line bans every version of that artifact.

```java
new BannedArtifactClassLoadingExecutor(classLoader).execute();   // e.g. first line of main()
```

This is the runtime counterpart of Maven Enforcer's `banned-dependencies` rule, and it is the reason the framework
can detect *shaded* or *transitively supplied* duplicates that a build plugin cannot see.

---

## 6. See also

* [Core Utilities](core-utilities.md#3-classloaderutils) — `ClassLoaderUtils` / `ClassPathUtils`, the primitives under
  everything on this page
* [Networking and URL Protocols](networking-and-url-protocols.md) — how archive URLs are parsed
* [I/O, Files and Watching](io-and-file-watch.md#5-scanning) — `SimpleJarEntryScanner`, `JarUtils`
* [Reference](reference.md#1-spi-registry) — service file inventory

[← Previous: Concurrency, Processes and JMX](concurrency-process-jmx.md) · [Index](README.md) · [Next: JSON →](json.md)
