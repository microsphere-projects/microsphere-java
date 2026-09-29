# I/O, Files and Watching

**Module**: `io.github.microsphere-projects:microsphere-java-core`
**Packages**: `io.microsphere.io`, `io.microsphere.io.event`, `io.microsphere.io.filter`, `io.microsphere.io.scanner`,
`io.microsphere.nio.charset`, `io.microsphere.nio.file`

> [!IMPORTANT]
> Package layout, which trips up people who assume Java conventions:
> * `FileWatchService` and `StandardFileWatchService` live in **`io.microsphere.io`**, not in `io.event`.
> * `FileChangedEvent` / `FileChangedListener` / `LoggingFileChangedListener` live in **`io.microsphere.io.event`**.
> * `IOFileFilter` and friends live in **`io.microsphere.io.filter`**.
> * `io.microsphere.io.filter` and the top-level `io.microsphere.filter` package are different things — see
>   [Collections and Filters](collections-and-filters.md#5-io-microsphere-filter).

---

## 1. `IOUtils`

```java
public abstract class IOUtils implements Utils {

    public static final int DEFAULT_BUFFER_SIZE = 2048;
    public static final String BUFFER_SIZE_PROPERTY_NAME = "microsphere.io.buffer.size";
    public static final int BUFFER_SIZE;                       // read once at class load

    public static String[] readLines(InputStream in) throws IOException
    public static String[] readLines(InputStream in, String encoding) throws IOException
    public static String[] readLines(InputStream in, Charset charset) throws IOException

    public static byte[] toByteArray(InputStream in) throws IOException

    public static String toString(InputStream in) throws IOException
    public static String toString(InputStream in, String encoding) throws IOException
    public static String toString(InputStream in, Charset charset) throws IOException
    public static String toString(Reader reader) throws IOException

    public static String copyToString(InputStream in) throws IOException
    public static String copyToString(InputStream in, String encoding) throws IOException
    public static String copyToString(InputStream in, Charset charset) throws IOException
    public static String copyToString(Reader reader) throws IOException

    public static int copy(InputStream in, OutputStream out) throws IOException
    public static int copy(Reader reader, Writer writer) throws IOException

    public static void close(Closeable closeable)
}
```

> [!NOTE]
> Read and copy methods **leave the stream open** — the caller owns it. `close(Closeable)` is the null-safe,
> `IOException`-suppressing closer, designed for `finally` blocks.
> `toString(InputStream)` and `copyToString(InputStream)` both exist and behave the same; the property
> `microsphere.io.buffer.size` (default `2048`) sets the internal buffer for every copy/read.

There is also a `@ConfigurationProperty` annotation on `BUFFER_SIZE_PROPERTY_NAME`, which is why
`microsphere.io.buffer.size` appears in the framework's own generated metadata — see
[Annotation Processing](annotation-processing.md).

```java
try (InputStream in = new FileInputStream("pom.xml")) {
    String content = IOUtils.toString(in);            // UTF-8 platform default is NOT assumed:
                                                       // charset forms are available explicitly
    IOUtils.copy(in, out);
}   // in closed by try-with-resources
```

### Fast in-memory streams

| Type | Constructors | Notes |
|---|---|---|
| `FastByteArrayInputStream` | `(byte[] buf)`, `(byte[] buf, int offset, int length)` | extends `ByteArrayInputStream` |
| `FastByteArrayOutputStream` | `()`, `(int size)` | extends `ByteArrayOutputStream`; `writeTo(OutputStream)`, `toByteArray()`, `toString(String charsetName)` |
| `StringBuilderWriter` | `()`, `(int capacity)`, `(StringBuilder builder)` | a `Writer` that appends into a `StringBuilder`; `getBuilder()` |

`StringBuilderWriter` is what `Compiler` ([Annotation Processing](annotation-processing.md)) uses to capture javac
output.

---

## 2. `FileUtils`

```java
public abstract class FileUtils implements Utils {

    public static final File[] EMPTY_FILE_ARRAY;

    @Nullable public static String resolveRelativePath(File parentDirectory, File targetFile)
    @Nullable public static String getFileExtension(String fileName)

    public static int deleteDirectory(File directory) throws IOException
    public static int cleanDirectory(File directory) throws IOException
    public static int forceDelete(File file) throws NoSuchFileException, IOException
    public static void forceDeleteOnExit(File file)
    public static void deleteDirectoryOnExit(File directory)

    public static File[] listFiles(File directory)
    public static boolean isSymlink(File file)
    public static File getCanonicalFile(File file)
}
```

The `delete*` / `clean*` methods return the **number of entries removed**, so you can assert on cleanup in tests.
`getCanonicalFile` wraps `IOException` instead of declaring it.

---

## 3. Charsets and `nio.file.Files`

```java
public abstract class CharsetUtils implements Utils {
    public static final Charset DEFAULT_CHARSET;   // from native.encoding / file.encoding, else UTF-8
}
```

`DEFAULT_CHARSET` is the charset `IOUtils.toString(InputStream)` and the metadata readers use. It is resolved once,
from the JVM's own encoding properties, which on JDK 18+ agrees with `Charset.defaultCharset()` but is stable on
older JDKs where `file.encoding` and `native.encoding` diverge.

```java
public abstract class io.microsphere.nio.file.Files implements Utils {
    @Nonnull public static String[] readLines(File file) throws IOException
    @Nonnull public static String[] readLines(File file, Charset charset) throws IOException
    @Nonnull public static String[] readLines(Path filePath) throws IOException
    @Nonnull public static String[] readLines(Path filePath, Charset charset) throws IOException
}
```

> [!WARNING]
> This `Files` class shadows `java.nio.file.Files` by name and is **only** a line-reading helper (it delegates to
> `IOUtils.readLines`). If you need copy/move/create, use `java.nio.file.Files` — import one or the other, never both.

There is no `Charsets` class in this framework.

---

## 4. File watching

### Contracts

```java
public interface FileWatchService {
    void watch(File file, FileChangedListener listener, FileChangedEvent.Kind... kinds);
    default void watch(File file, Iterable<FileChangedListener> listeners, FileChangedEvent.Kind... kinds);
}

@Immutable
public class FileChangedEvent extends Event {
    public FileChangedEvent(File file, Kind kind) throws IllegalArgumentException
    public File getFile()          // (File) getSource()
    public Kind getKind()
    public enum Kind { CREATED, MODIFIED, DELETED }
}

public interface FileChangedListener extends EventListener<FileChangedEvent> {
    @Override default void onEvent(FileChangedEvent event)   // routes by Kind
    default void onFileCreated(FileChangedEvent event)       // empty
    default void onFileModified(FileChangedEvent event)      // empty
    default void onFileDeleted(FileChangedEvent event)       // empty
}

public class LoggingFileChangedListener implements FileChangedListener  // logs each kind at debug
```

`FileChangedEvent` extends the framework's [`Event`](events.md#2-event), so it carries `getTimestamp()` for free, and
`FileChangedListener` is an ordinary `EventListener` — you can register it on an `EventDispatcher` too, though the
watch service delivers directly.

### `StandardFileWatchService`

```java
public class StandardFileWatchService implements FileWatchService, AutoCloseable {

    public static final String DEFAULT_THREAD_NAME_PREFIX = "microsphere-file-watch-service";
    public static final String THREAD_NAME_PREFIX_PROPERTY_NAME = "microsphere.file-watch-service.thread-name-prefix";
    public static final String THREAD_NAME_PREFIX;

    public StandardFileWatchService()                                        // listener callbacks: DIRECT_EXECUTOR
    public StandardFileWatchService(Executor eventHandlerExecutor)
    public StandardFileWatchService(Executor eventHandlerExecutor, ExecutorService eventLoopExecutor)

    public void start() throws Exception
    public void stop() throws Exception
    public boolean isStarted()
    @Override public void close() throws Exception

    @Override public void watch(File file, FileChangedListener listener, FileChangedEvent.Kind... kinds)
}
```

```java
try (StandardFileWatchService watcher = new StandardFileWatchService()) {

    watcher.watch(new File("config/application.yml"), event -> {
        switch (event.getKind()) {
            case CREATED:
            case MODIFIED:
                reload(event.getFile());
                break;
            case DELETED:
                clear();
                break;
        }
    }, FileChangedEvent.Kind.CREATED, FileChangedEvent.Kind.MODIFIED);

    watcher.start();
    // ... keep the reference alive; close() stops the loop and shuts down executors
}
```

Behaviour worth knowing:

* The **event loop** always runs on its own single-thread `ExecutorService` (daemon threads, named from
  `THREAD_NAME_PREFIX`); the **handler executor** decides where your listener runs. The no-arg constructor uses
  `DIRECT_EXECUTOR`, so callbacks run on the watch thread — one slow listener blocks all further events. Pass an
  `Executor` if handlers may block.
* Register directories with `watch(...)` **before** `start()` when you can: `start()` registers the accumulated
  directories with the JDK `WatchService`. A `start()` on an already-started instance throws
  `IllegalStateException("StandardFileWatchService has started")`.
* `close()` = `stop()` + close the JDK `WatchService` + shut down executors, which is why it is `AutoCloseable`.
* Only the **directory** containing a file can be watched (that is a `java.nio.file.WatchService` limit); the service
  translates events down to the specific file you registered.
* `KIND_OVERFLOW`-style loss on network filesystems is not recovered — macOS and NFS are unreliable here, as always.

---

## 5. Scanning

### The generic contract

```java
public interface Scanner<S, R> {
    @Nonnull Set<R> scan(S source) throws IllegalArgumentException, IllegalStateException;
    @Nonnull Set<R> scan(S source, Filter<R> filter) throws IllegalArgumentException, IllegalStateException;
}
```

> [!NOTE]
> The concrete scanners below do **not** implement `Scanner<S, R>` — their signatures differ because they need extra
> parameters (recursion, class loading) and use `IOFileFilter` / `JarEntryFilter` / `Predicate`. Treat `Scanner` as
> the vocabulary for scanners you write yourself.

### `SimpleClassScanner`

```java
public class SimpleClassScanner {
    public final static SimpleClassScanner INSTANCE;
    public SimpleClassScanner()

    @Nonnull @Immutable public Set<Class<?>> scan(ClassLoader classLoader, String packageName)
    @Nonnull @Immutable public Set<Class<?>> scan(ClassLoader classLoader, String packageName, boolean recursive)
    @Nonnull @Immutable public Set<Class<?>> scan(ClassLoader classLoader, String packageName,
                                                  boolean recursive, boolean requiredLoad)
    @Nonnull public Set<Class<?>> scan(ClassLoader classLoader, URL resourceInArchive,
                                       boolean requiredLoad, Predicate<? super Class<?>>... classFilters)
    @Nonnull public Set<Class<?>> scan(ClassLoader classLoader, File archiveFile,
                                       boolean requiredLoad, Predicate<? super Class<?>>... classFilters)
}
```

```java
// All classes in a package, loaded
Set<Class<?>> services = SimpleClassScanner.INSTANCE
        .scan(getClass().getClassLoader(), "com.example.spi", true);

// Only annotated ones, without forcing class loading
Set<Class<?>> rest = SimpleClassScanner.INSTANCE.scan(classLoader, "com.example.rest",
        true, method -> ClassUtils.isConcreteClass(method.getDeclaringClass()));
```

`requiredLoad = false` returns only what can be resolved without loading; the archive overloads scan a JAR/WAR URL or
file directly. For names-only scanning use
[`ClassUtils.findClassNamesInClassPath(...)`](core-utilities.md#2-classutils), which never loads classes.

### `SimpleFileScanner`

```java
public class SimpleFileScanner {
    public final static SimpleFileScanner INSTANCE;
    public SimpleFileScanner()

    @Nonnull @Immutable public Set<File> scan(File rootDirectory, boolean recursive)
    @Nonnull @Immutable public Set<File> scan(File rootDirectory, boolean recursive, IOFileFilter ioFileFilter)
}
```

```java
Set<File> yamls = SimpleFileScanner.INSTANCE.scan(new File("src/main/resources"), true,
        FileExtensionFilter.of("yml"));
```

Filters come from [`io.microsphere.io.filter`](collections-and-filters.md#5-io-microsphere-filter):
`TrueFileFilter.INSTANCE`, `DirectoryFileFilter.INSTANCE`, `NameFileFilter`, `FileExtensionFilter.of(...)`.

### `SimpleJarEntryScanner`

```java
public class SimpleJarEntryScanner {
    public static final SimpleJarEntryScanner INSTANCE;
    public SimpleJarEntryScanner()

    @Nonnull @Immutable public Set<JarEntry> scan(URL jarURL, boolean recursive) throws IOException
    @Nonnull @Immutable public Set<JarEntry> scan(URL jarURL, boolean recursive, JarEntryFilter jarEntryFilter) throws IOException
    public Set<JarEntry> scan(JarFile jarFile, boolean recursive) throws IOException
    @Nonnull @Immutable public Set<JarEntry> scan(JarFile jarFile, boolean recursive, JarEntryFilter jarEntryFilter) throws IOException
}
```

```java
Set<JarEntry> entries = SimpleJarEntryScanner.INSTANCE.scan(jarUrl, true,
        ClassFileJarEntryFilter.INSTANCE);
```

Combine with [`JarUtils`](core-utilities.md#12-jarutils) for extraction and
[`ArtifactDetector`](classloading-and-artifacts.md) for identifying what the JAR is.

---

## 6. See also

* [Collections and Filters](collections-and-filters.md) — `Filter`, `FilterOperator`, `JarEntryFilter`
* [Event Dispatching](events.md) — the `Event` / `EventListener` model `FileChangedEvent` builds on
* [Class Loading and Artifacts](classloading-and-artifacts.md) — turning JAR URLs into `Artifact`s
* [Reference](reference.md) — `microsphere.io.buffer.size`,
  `microsphere.file-watch-service.thread-name-prefix`

[← Previous: Event Dispatching](events.md) · [Index](README.md) · [Next: Networking and URL Protocols →](networking-and-url-protocols.md)
