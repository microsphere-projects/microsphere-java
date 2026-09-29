# I/O, Files and Watching

> Read this page in: [中文](../zh/io-and-file-watch.md) · [English](io-and-file-watch.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Module | `io.github.microsphere-projects:microsphere-java-core` |
| Packages | `io.microsphere.io`, `io.microsphere.io.event`, `io.microsphere.io.filter`, `io.microsphere.io.scanner`, `io.microsphere.nio.charset`, `io.microsphere.nio.file` |
| Stream copy buffer | system property `microsphere.io.buffer.size`, default `2048` |
| Watch thread naming | system property `microsphere.file-watch-service.thread-name-prefix`, default `microsphere-file-watch-service` |
| Watch event kinds | `FileChangedEvent.Kind`: `CREATED`, `MODIFIED`, `DELETED` |
| Java level | 8 (uses `java.nio.file.WatchService`; no third-party dependency) |

This page covers four things: reading and copying streams without leaks (`IOUtils`), file and charset helpers (`FileUtils`, `CharsetUtils`, `io.microsphere.nio.file.Files`), watching a directory for changes (`FileWatchService` + event model in `io.microsphere.io.event`), and scanning classes, files and JAR entries (`io.microsphere.io.scanner`).

> [!IMPORTANT]
> The package layout does not follow what you might guess from the names:
> `FileWatchService` and `StandardFileWatchService` live in **`io.microsphere.io`**, not in an `event` package.
> `FileChangedEvent` / `FileChangedListener` live in **`io.microsphere.io.event`**.
> `IOFileFilter` implementations live in **`io.microsphere.io.filter`**, which is a different thing from the top-level
> `io.microsphere.filter` package used by the generic `Scanner<S, R>` interface.

---

## 1. Reading and copying streams: `IOUtils`

`IOUtils` (`public abstract class IOUtils implements Utils`) is the null-safe, charset-explicit layer over raw streams:

```java
public static String[] readLines(InputStream in) throws IOException            // + (in, String) / (in, Charset)
public static byte[]   toByteArray(InputStream in) throws IOException
public static String   toString(InputStream in) throws IOException             // + (in, String) / (in, Charset) / (Reader)
public static String   copyToString(InputStream in) throws IOException         // + (in, String) / (in, Charset) / (Reader)
public static int      copy(InputStream in, OutputStream out) throws IOException
public static int      copy(Reader reader, Writer writer) throws IOException
public static void     close(Closeable closeable)
```

```java
try (InputStream in = new FileInputStream("pom.xml");
     ByteArrayOutputStream out = new ByteArrayOutputStream()) {

    String content = IOUtils.toString(in, StandardCharsets.UTF_8); // read; stream stays open
    int bytes = IOUtils.copy(in, out);                              // copy what remains
}   // try-with-resources closes; IOUtils never closes for you
```

Two rules that explain most misuse:

* **Read and copy methods leave the stream open.** The caller owns the stream. `toString(...)` and `copyToString(...)` behave the same; pick either.
* **`close(Closeable)` is for `finally` blocks.** It accepts `null`, swallows `IOException`, and never rethrows — use it when you cannot use try-with-resources.

All buffered operations use `IOUtils.BUFFER_SIZE`, read once at class load from the system property `microsphere.io.buffer.size` (default `IOUtils.DEFAULT_BUFFER_SIZE` = 2048). Set it on the JVM command line if you copy very large files.

### 1.1 Fast in-memory streams

| Type | Constructors | Notes |
|---|---|---|
| `FastByteArrayInputStream` | `(byte[] buf)`, `(byte[] buf, int offset, int length)` | extends `ByteArrayInputStream` |
| `FastByteArrayOutputStream` | `()`, `(int size)` | extends `ByteArrayOutputStream`; `writeTo(OutputStream)`, `toByteArray()`, `toString(String charsetName)` |
| `StringBuilderWriter` | `()`, `(int capacity)`, `(StringBuilder builder)` | a `Writer` that appends into a `StringBuilder`; retrieve it with `getBuilder()` |

`StringBuilderWriter` is how in-process javac runs capture compiler output — see the annotation-processing page.

---

## 2. File and charset helpers

### 2.1 `FileUtils`

```java
public static final File[] EMPTY_FILE_ARRAY;                                  // never-null return sentinel

@Nullable public static String resolveRelativePath(File parentDirectory, File targetFile)
@Nullable public static String getFileExtension(String fileName)

public static int  deleteDirectory(File directory) throws IOException
public static int  cleanDirectory(File directory) throws IOException          // contents only, keeps the directory
public static int  forceDelete(File file) throws NoSuchFileException, IOException
public static void forceDeleteOnExit(File file)
public static void deleteDirectoryOnExit(File directory)

public static File[] listFiles(File directory)          // never null; EMPTY_FILE_ARRAY on failure
public static boolean isSymlink(File file)
public static File getCanonicalFile(File file)          // wraps IOException instead of declaring it
```

The `delete*` / `clean*` methods return the **number of entries removed**, so tests can assert on cleanup instead of just calling and hoping.

### 2.2 `CharsetUtils`

`CharsetUtils.DEFAULT_CHARSET` is a `Charset` resolved once at class load (`public static final Charset DEFAULT_CHARSET = defaultCharset()`). It is the charset the no-charset `IOUtils.toString(InputStream)` / `readLines(InputStream)` overloads use, so on older JDKs where `file.encoding` and `native.encoding` diverge, behavior is at least stable and inspectable. When it matters, always pass an explicit `Charset` or encoding name.

### 2.3 `io.microsphere.nio.file.Files`

> [!WARNING]
> `io.microsphere.nio.file.Files` **shadows `java.nio.file.Files` by name** and is only a line-reading helper:
>
> ```java
> @Nonnull public static String[] readLines(File file) throws IOException
> @Nonnull public static String[] readLines(File file, Charset charset) throws IOException
> @Nonnull public static String[] readLines(Path filePath) throws IOException
> @Nonnull public static String[] readLines(Path filePath, Charset charset) throws IOException
> ```
>
> It returns `String[]`, not a `Stream`, and delegates to `IOUtils.readLines`. For copy/move/create operations use
> `java.nio.file.Files` — import one or the other, never both. There is no `Charsets` class in this framework.

---

## 3. Watching files: contracts

The event model lives in `io.microsphere.io.event` and builds on the framework's generic `Event` / `EventListener` pair (see the events page):

```java
public interface FileWatchService {
    void watch(File file, FileChangedListener listener, FileChangedEvent.Kind... kinds);
    default void watch(File file, Iterable<FileChangedListener> listeners, FileChangedEvent.Kind... kinds);
}

public class FileChangedEvent extends Event {          // immutable
    public FileChangedEvent(File file, Kind kind)      // IllegalArgumentException on null args
    public File getFile()                               // (File) getSource()
    public Kind getKind()
    public enum Kind { CREATED, MODIFIED, DELETED }
}

public interface FileChangedListener extends EventListener<FileChangedEvent> {
    @Override default void onEvent(FileChangedEvent event)   // routes by Kind to the three methods below
    default void onFileCreated(FileChangedEvent event)       // empty — override what you need
    default void onFileModified(FileChangedEvent event)      // empty
    default void onFileDeleted(FileChangedEvent event)       // empty
}
```

Because `FileChangedEvent` extends `Event`, every callback carries `getTimestamp()` for free, and a `FileChangedListener` is an ordinary `EventListener` — you can also register it on an `EventDispatcher`, though the watch service delivers to it directly. `LoggingFileChangedListener` is a ready-made implementation that logs each kind at debug level; useful while wiring a watcher.

---

## 4. `StandardFileWatchService`: lifecycle and usage

```java
public class StandardFileWatchService implements FileWatchService, AutoCloseable {

    public static final String DEFAULT_THREAD_NAME_PREFIX = "microsphere-file-watch-service";
    public static final String THREAD_NAME_PREFIX_PROPERTY_NAME = "microsphere.file-watch-service.thread-name-prefix";
    public static final String THREAD_NAME_PREFIX;              // read once at class load

    public StandardFileWatchService()                             // listener callbacks: DIRECT_EXECUTOR
    public StandardFileWatchService(Executor eventHandlerExecutor)
    public StandardFileWatchService(Executor eventHandlerExecutor, ExecutorService eventLoopExecutor)

    public void start() throws Exception
    public void stop() throws Exception
    public boolean isStarted()
    public void close() throws Exception
    public void watch(File file, FileChangedListener listener, FileChangedEvent.Kind... kinds)
}
```

```java
StandardFileWatchService watcher = new StandardFileWatchService();

// 1. register BEFORE start(): start() is what pushes directories into the JDK WatchService
watcher.watch(new File("config/application.yml"), event -> {
    switch (event.getKind()) {
        case CREATED:
        case MODIFIED: reload(event.getFile()); break;
        case DELETED:  clear();                 break;
    }
}, FileChangedEvent.Kind.CREATED, FileChangedEvent.Kind.MODIFIED);

// 2. start the event loop
watcher.start();

// 3. later: stop() ends the loop; close() = stop() + close JDK WatchService + shut down executors
watcher.close();
```

The threading model has two axes, and the constructor you pick decides both:

| Component | Where it runs | Consequence |
|---|---|---|
| Event loop (the `watchService.take()` loop) | Always its own **single-thread daemon `ExecutorService`**, threads named from `THREAD_NAME_PREFIX` | survives your listeners; shut down on JVM exit (`shutdownOnExit` in the constructor) |
| Listener callbacks | The **handler executor**. No-arg constructor → `DIRECT_EXECUTOR`, i.e. callbacks run on the watch thread | with `DIRECT_EXECUTOR`, one slow or blocked listener stops all further event delivery — pass an `Executor` if handlers may block |

> [!NOTE]
> Internally each watched directory gets its own `EventDispatcher.parallel(eventHandlerExecutor)`, so listeners are
> dispatched through the framework's event machinery rather than called ad hoc. This means listener priority and
> `EventListener` semantics apply as described on the events page.

---

## 5. What `java.nio.file.WatchService` really gives you

Everything below is visible in `StandardFileWatchService`'s source or is a documented platform behavior of the JDK `WatchService` it wraps. Read this before you rely on watching for correctness.

* **Only directories can be registered.** The service resolves `file.toPath().getParent()` when you pass a regular
  file (a directory argument is watched directly). Subdirectories are **not** watched — there is no recursion option;
  to follow a tree, register each directory yourself before `start()`.
* **You receive every event in the watched directory, not just your file.** The dispatch loop accepts an event when
  the registered path is a directory (which it always is) — so sibling-file `CREATED`/`MODIFIED`/`DELETED` events in
  the same directory are delivered to all listeners registered under it. Filter by `event.getFile()` in your listener.
* **Kinds are per directory, first registration wins.** `watch()` merges listeners per directory but keeps the
  `Kind...` of the **first** call for that directory; a later `watch()` on another file in the same directory does not
  widen the registered JDK kinds. Pass the union of kinds you care about on the first call, or omit kinds entirely —
  with no kinds the service registers all three (`ENTRY_CREATE`, `ENTRY_MODIFY`, `ENTRY_DELETE`).
* **`watch()` after `start()` has no effect on registration.** Directories are pushed to the JDK `WatchService` inside
  `start()` only. A `watch()` for a *new* directory after `start()` records metadata but never registers the path, so
  no events arrive for it. `start()` twice throws `IllegalStateException("StandardFileWatchService has started")`.
* **`OVERFLOW` kills the event loop.** When the OS event queue overflows (burst of changes, slow consumer), the JDK
  reports an `OVERFLOW` watch event. `StandardFileWatchService.toKind(...)` accepts only create/modify/delete and
  throws `IllegalArgumentException` for anything else, and the loop body has no `catch` — so the loop thread dies
  while `isStarted()` **still returns true**. If events stop arriving after a burst, suspect this. Mitigation: keep a
  non-`DIRECT_EXECUTOR` handler, watch fewer directories, and rebuild the service (new instance, `watch(...)`,
  `start()`) if delivery stops.
* **Platform differences still apply.** The service adds no latency control, no retry, and no coalescing recovery on
  top of the JDK implementation. Linux (inotify) watches directory inodes and stops following a directory renamed
  onto itself; macOS and network/NFS filesystems coalesce and drop events unreliably; Windows reports more events
  than needed (you will see spurious `MODIFIED`). Expect at-most-once, de-duplicated delivery — treat events as a
  hint to re-read state, not as the state change itself.
* **`stop()` is best-effort.** It waits for the event loop to terminate in 100 ms steps, then
  `Future.cancel(true)` interrupts it (unblocking `take()` with `InterruptedException`). `close()` additionally
  closes the JDK `WatchService`, clears the directory cache, and shuts down both executors — hence `AutoCloseable`.

> [!TIP]
> A reload-on-change pattern should be idempotent: call your reload logic in a debounced task, not per event, because
> one editor save commonly produces several `MODIFIED` events.

---

## 6. Scanning classes, files and JAR entries

### 6.1 The generic `Scanner<S, R>`

```java
public interface Scanner<S, R> {
    @Nonnull Set<R> scan(S source) throws IllegalArgumentException, IllegalStateException;
    @Nonnull Set<R> scan(S source, Filter<R> filter) throws IllegalArgumentException, IllegalStateException;
}
```

> [!NOTE]
> The concrete scanners below do **not** implement `Scanner<S, R>` — they need extra parameters (recursion, class
> loading) and use `IOFileFilter` / `JarEntryFilter` / `Predicate` instead of `io.microsphere.filter.Filter`.
> Treat `Scanner` as the vocabulary for scanners you write yourself.

### 6.2 `SimpleClassScanner`

```java
public class SimpleClassScanner {
    public final static SimpleClassScanner INSTANCE;

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
// All classes in a package, recursively, loaded
Set<Class<?>> services = SimpleClassScanner.INSTANCE
        .scan(getClass().getClassLoader(), "com.example.spi", true);

// Scan a JAR/WAR archive (URL or File), keeping only classes you accept
Set<Class<?>> rest = SimpleClassScanner.INSTANCE.scan(classLoader, jarUrl, true,
        clazz -> !Modifier.isAbstract(clazz.getModifiers()));
```

`requiredLoad = false` returns only classes resolvable without loading. For names-only scanning that never loads
anything, use `ClassUtils.findClassNamesInClassPath(...)` (see the core-utilities page).

### 6.3 `SimpleFileScanner` and `io.microsphere.io.filter`

```java
public class SimpleFileScanner {
    public final static SimpleFileScanner INSTANCE;

    @Nonnull @Immutable public Set<File> scan(File rootDirectory, boolean recursive)
    @Nonnull @Immutable public Set<File> scan(File rootDirectory, boolean recursive, IOFileFilter ioFileFilter)
}
```

```java
Set<File> yamls = SimpleFileScanner.INSTANCE.scan(new File("src/main/resources"), true,
        FileExtensionFilter.of("yml"));
```

Filters implement `io.microsphere.io.filter.IOFileFilter` (`accept(File)` and/or `accept(File, String)`):

| Filter | Meaning |
|---|---|
| `TrueFileFilter.INSTANCE` | accepts everything |
| `DirectoryFileFilter.INSTANCE` | accepts directories only |
| `NameFileFilter` | accepts files by name |
| `FileExtensionFilter.of("yml")` | accepts files by extension |

### 6.4 `SimpleJarEntryScanner`

```java
public class SimpleJarEntryScanner {
    public static final SimpleJarEntryScanner INSTANCE;

    @Nonnull @Immutable public Set<JarEntry> scan(URL jarURL, boolean recursive) throws IOException
    @Nonnull @Immutable public Set<JarEntry> scan(URL jarURL, boolean recursive, JarEntryFilter jarEntryFilter) throws IOException
    public Set<JarEntry> scan(JarFile jarFile, boolean recursive) throws IOException
    @Nonnull @Immutable public Set<JarEntry> scan(JarFile jarFile, boolean recursive, JarEntryFilter jarEntryFilter) throws IOException
}
```

```java
Set<JarEntry> classEntries = SimpleJarEntryScanner.INSTANCE.scan(jarUrl, true,
        entry -> entry.getName().endsWith(".class"));
```

`recursive` controls whether nested directories inside the JAR are enumerated. The scanner also declares
`NullPointerException` / `IllegalArgumentException` on null or non-JAR inputs — validate the URL before calling.
Combine with `JarUtils` for extraction and `ArtifactDetector` for identifying what the JAR is (see the classloading
page).

---

## See also

* [Event Dispatching](events.md) — the `Event` / `EventListener` / `EventDispatcher` model `FileChangedEvent` builds on.
* [Core Utilities](core-utilities.md) — `ClassUtils.findClassNamesInClassPath`, `JarUtils`.
* [Getting Started](getting-started.md) — module and dependency setup.
* [Reference](reference.md) — `microsphere.io.buffer.size`, `microsphere.file-watch-service.thread-name-prefix`.

[← Handbook index](../README.md) · [Previous: Event Dispatching](events.md) · [Next: Networking and URL Protocols →](networking-and-url-protocols.md)
