# I/O, NIO, scanning and file filters

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-java-core` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Packages** | `io.microsphere.io` — 7 · `io.microsphere.io.event` — 3 · `io.microsphere.io.scanner` — 4 · `io.microsphere.io.filter` — 5 · `io.microsphere.nio.file` — 1 · `io.microsphere.nio.charset` — 1 (**21 total**) |
| **JDK level** | Compiled at Java 8; CI-tested on JDK 8 / 11 / 17 / 21 / 25 |
| **Depends on other `core` packages** | `util` (`SystemUtils`, `ClassLoaderUtils`, `StringUtils`, `Utils`), `constants`, `text`, `collection`, `filter` (the generic `Filter<T>`), `event` (`EventDispatcher`), `logging`, `concurrent`, `annotation` |
| **External dependencies** | none — pure JDK (`java.io`, `java.nio.file`, `java.util.jar`) |
| **Used by** | `util.ClassUtils` (classpath scanning), `net.URLUtils`, `classloading.StreamArtifactResourceResolver`, `process.ProcessExecutor`, `util.ExceptionUtils` |

All `file:line` references are relative to `microsphere-java-core/src/main/java/io/microsphere/` (main sources) or
`microsphere-java-core/src/test/java/io/microsphere/` (tests), read from the `release` worktree.
After the first mention they are shortened — e.g. `io/scanner/SimpleFileScanner.java:87-108` becomes
`scanner/SimpleFileScanner.java:87-108` once the `io/` prefix has been established for that package.

---

## 1. What this group is for

**Reach for `io.microsphere.io` when you need an I/O helper that will not throw on a `null` stream and will not close
something you did not open.** `IOUtils` (`io/IOUtils.java:57`) is the whole surface: `toByteArray`, `toString`,
`copyToString`, `readLines`, `copy` and `close`. Every `copy*`/`toString` overload returns `null` for a `null` source
(`:398-400`, `:424-426`) instead of raising, and `close(null)` is a no-op (`:535-545`). Nothing in the class closes a
stream you passed in — the javadoc repeats "leaves the input stream open" (`:99`, `:191`, `:222`) and the code honours
it; `Files.readLines` is the one place in the slice that does own its stream (`nio/file/Files.java:167`).

**Reach for `io.microsphere.io.scanner` when you need "what is in this directory / this jar / this package".** There
are three concrete scanners, each a stateless singleton, each returning an unmodifiable `LinkedHashSet` so result order
follows the file system or archive order:

| Scanner | Source | Result | Singleton |
|---|---|---|---|
| `SimpleFileScanner` (`scanner/SimpleFileScanner.java:52`) | `File` + `boolean recursive` | `Set<File>` | `INSTANCE` (`:57`) |
| `SimpleJarEntryScanner` (`scanner/SimpleJarEntryScanner.java:60`) | `URL` or `JarFile` + `boolean recursive` | `Set<JarEntry>` | `INSTANCE` (`:65`) |
| `SimpleClassScanner` (`scanner/SimpleClassScanner.java:64`) | `ClassLoader` + package name, or archive `File`/`URL` | `Set<Class<?>>` | `INSTANCE` (`:69`) |

> [!IMPORTANT]
> `io.microsphere.io.filter` holds the **`File` predicates** (`IOFileFilter`, `DirectoryFileFilter`,
> `FileExtensionFilter`, `NameFileFilter`, `TrueFileFilter`). The generic `Filter<T>` SPI lives in the *other* package,
> `io.microsphere.filter`, and is documented in [Collections and filters](collections-and-filters.md). The two are not
> interchangeable: `SimpleFileScanner` takes an `IOFileFilter`, `Scanner.scan(S, Filter<R>)` takes a `Filter<R>`, and
> `SimpleJarEntryScanner` takes `JarEntryFilter` (from `io.microsphere.filter`). Keeping them straight is the single most
> common mistake in this area.

**Reach for `FileWatchService` / `StandardFileWatchService` when you want file-change events as `FileChangedEvent`s on
the library's own event bus**, rather than hand-writing a `WatchService` poll loop. The bridge is ~340 lines and it does
the three things that are easy to get wrong: it registers **directories** (not files, because JDK `WatchService` cannot
watch a regular file), it maps `StandardWatchEventKinds` to `FileChangedEvent.Kind`, and it dispatches through
`EventDispatcher` so your listeners get the same `Prioritized` ordering as everywhere else in `core`.

`StringBuilderWriter`, `FastByteArrayInputStream` and `FastByteArrayOutputStream` are the three in-memory `Writer` /
stream types the rest of `core` builds on — `util.ExceptionUtils` uses the former, `process.ProcessExecutor` and
`classloading.StreamArtifactResourceResolver` use the latter two (verified by imports).

---

## 2. Coordinates, version support and the platform matrix

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

| Fact | Value | Evidence (`release`) |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | `pom.xml:12` |
| Version property | `${revision}` | `pom.xml:14` |
| Value of `${revision}` on `release` | `0.3.20-SNAPSHOT` | `pom.xml:54` |
| Newest published tag | `0.3.20` | release-line table in the group index (`docs/current/user-guide/microsphere-java-core/README.md:70`) |
| Build parent | `microsphere-build:0.3.16` | `pom.xml:6-10` |
| Java level | no `maven.compiler.*` / `<release>` property exists in the reactor POMs — the Java 8 level is inherited from the external build parent | CI matrix `java: [ '8', '11', '17', '21', '25' ]`, `.github/workflows/maven-build.yml:25`, `distribution: 'temurin'` `:33`, build command `:38-45` |
| Spring Framework, JDK 17+ | `7.0.9` | `microsphere-java-parent/pom.xml:23` |
| Spring Framework, JDK `[1.8,17)` | `5.3.39` via profile `java8-16` | `microsphere-java-parent/pom.xml:54-62` (activation `<jdk>[1.8,17)</jdk>` at `:57`) |
| Imported BOMs | `microsphere-all-bom:0.3.13`, `spring-framework-bom` | `microsphere-java-parent/pom.xml:22`, `:31-37`, `:40-46` |

Nothing in this slice depends on Spring, so the `java8-16` profile changes nothing here. What **does** change behaviour
in this slice is the JDK and the operating system, because this is the layer that touches `WatchService`, `Path`/`Files`
and the platform charset.

### 2.1 Charset and line-separator defaults

| Behaviour | Where it comes from | What you get |
|---|---|---|
| `CharsetUtils.DEFAULT_CHARSET` | `Charset.defaultCharset()` (`nio/charset/CharsetUtils.java:43`) | the JVM default — **not** pinned to UTF-8 |
| `SystemUtils.FILE_ENCODING` | `getSystemProperty("file.encoding", DEFAULT_CHARSET.name())` (`util/SystemUtils.java:332`) | falls back to the default charset's name when the property is absent |
| `SystemUtils.NATIVE_ENCODING` | `getSystemProperty("native.encoding")` (`util/SystemUtils.java:337`) | only non-`null` on JDK 18+, where `native.encoding` was added |
| `IOUtils.readLines(InputStream)` | `readLines(in, FILE_ENCODING)` (`io/IOUtils.java:119-121`) | decodes with `file.encoding`, JDK-dependent before 18 |
| `IOUtils.readLines(in, charset)` line splitting | `split(content, LINE_SEPARATOR)` (`io/IOUtils.java:183-186`) where `LINE_SEPARATOR = System.lineSeparator()` (`constants/SeparatorConstants.java:41`) | splits on the **platform** separator only |
| `FileExtensionFilter` case sensitivity | `CASE_SENSITIVE = !IS_OS_WINDOWS` (`io/filter/FileExtensionFilter.java:59`) | `.TXT` matches `txt` only on Windows |

> [!WARNING]
> Two consequences of the rows above are easy to miss. First, `readLines` splits on `System.lineSeparator()`, so a
> `LF`-only file read on Windows returns **one** element, and a `CRLF` file read on Linux returns elements ending in
> `\r`. Second, `IOUtils.readLines(in, encoding)` calls `Charset.forName(encoding)` directly (`io/IOUtils.java:150-152`)
> — unlike `copyToString(in, encoding)`, which defaults a `null` encoding to `FILE_ENCODING` (`:346-349`). Passing `null`
> to the `String`-encoding `readLines` overload therefore throws, and passing `""` throws
> `IllegalCharsetNameException` — which `IOUtilsTest.java:117-119` records for the `toString` path.

### 2.2 `WatchService` per-JDK / per-platform semantics

| Concern | JDK 8 – 10 | JDK 11+ | JDK 18+ | Windows | Linux / macOS |
|---|---|---|---|---|---|
| `FileSystem.newWatchService()` (`io/StandardFileWatchService.java:176`) | available since JDK 7 — no gating | idem | idem | idem | idem |
| Backing mechanism | — | — | — | ReadDirectoryChangesW, events are **per-directory** and coalesce aggressively; `MODIFIED` often fires multiple times per write | inotify on Linux (watch descriptors are a finite resource), FSEvents on macOS |
| Watching a regular `File` | **not supported by the JDK at all** — the scanner works around it by registering `filePath.getParent()` (`:275`) | idem | idem | idem | idem |
| `native.encoding` available for `SystemUtils.NATIVE_ENCODING` | absent → `null` (`util/SystemUtils.java:337`) | absent → `null` | present | — | — |
| `Charset.defaultCharset()` | platform-dependent (e.g. Cp1252 / GBK on Windows) | platform-dependent | **UTF-8 by default** (JEP 400) | changes at 18 | unchanged |
| Event kinds registered | `ENTRY_CREATE/DELETE/MODIFY` only (`:133-137`) | idem | idem | idem | idem |

Because `defaultCharset()` became UTF-8 on JDK 18 (JEP 400), the *same* code that reads a GBK file correctly on JDK 11
will mis-decode it on JDK 21. If you rely on `IOUtils.toString(in)`, `IOUtils.readLines(in)` or `Files.readLines(file)`
without an explicit `Charset`, pin the charset or set `-Dfile.encoding` — the library does not choose UTF-8 for you
(`nio/charset/CharsetUtils.java:43` is literally `defaultCharset()`).

> [!NOTE]
> `CharsetUtils.DEFAULT_CHARSET`'s javadoc (`nio/charset/CharsetUtils.java:35-42`) says the default charset "looks up
> from … `native.encoding` and `file.encoding` … or the default charset is `UTF-8`". The implementation is a plain
> `Charset.defaultCharset()` call (`:43`); `NATIVE_ENCODING` is only referenced from `util/SystemUtils.java:337`, not
> from here. Trust the code, not that sentence.

---

## 3. API reference

### 3.1 Scanner entry points

| Call | Signature (real) | Notes |
|---|---|---|
| Scan a directory tree | `SimpleFileScanner.scan(File rootDirectory, boolean recursive)` (`scanner/SimpleFileScanner.java:72-74`) | delegates with `TrueFileFilter.INSTANCE` (`:73`) |
| Scan with a `File` predicate | `scan(File rootDirectory, boolean recursive, IOFileFilter ioFileFilter)` (`:87-108`) | returns `unmodifiableSet` (`:107`); the **root itself** is tested first (`:91-93`) |
| Scan a jar by URL | `SimpleJarEntryScanner.scan(URL jarURL, boolean recursive)` (`scanner/SimpleJarEntryScanner.java:80-82`) | `null` filter (`:81`) |
| Scan a jar by URL with filter | `scan(URL jarURL, boolean recursive, JarEntryFilter jarEntryFilter)` (`:96-100`) | resolves the relative path, then opens the jar (`:97-99`) |
| Scan an open `JarFile` | `scan(JarFile jarFile, boolean recursive[, JarEntryFilter])` (`:110-112`, `:125-127`) | delegates to the `protected` core overload (`:131-153`) |
| Entry-name helper | `boolean isFileEntry(String jarEntryName, String relativePath)` (`:155-161`) | package-private — used by `SimpleJarEntryScannerTest.java:58-61` |
| Scan a package, names only | `SimpleClassScanner.scan(ClassLoader cl, String packageName[, boolean recursive])` (`scanner/SimpleClassScanner.java:86-88`, `:103-105`) | `recursive=false` by default |
| Scan a package, optionally load | `scan(ClassLoader cl, String packageName, boolean recursive, boolean requiredLoad)` (`:121-158`) | `requiredLoad=false` → `findLoadedClass`, i.e. already-loaded classes only (`:151`) |
| Scan an archive | `scan(ClassLoader cl, File archiveFile, boolean requiredLoad, Predicate<? super Class<?>>... classFilters)` (`:168-179`) | varargs `Predicate`, applied by `filterAll` (`:178`) |
| Scan an archive by URL | `scan(ClassLoader cl, URL resourceInArchive, boolean requiredLoad, Predicate...)` (`:161-165`) | `resolveArchiveFile` first (`:163`) |
| The declared abstraction | `Scanner<S,R>.scan(S)` / `scan(S, Filter<R>)` (`scanner/Scanner.java:35`, `:47`) | see the note below |

> [!IMPORTANT]
> **`Scanner<S,R>` currently has no implementations.** A grep of the whole `release` worktree for
> `implements Scanner`, `extends Scanner` and `ClassPathScanner` returns nothing, and no
> `META-INF/services/io.microsphere.io.scanner.Scanner` file ships. The three `Simple*Scanner` classes are plain
> classes with their own concrete signatures (`scanner/SimpleFileScanner.java:52`, `scanner/SimpleJarEntryScanner.java:60`,
> `scanner/SimpleClassScanner.java:64`) — they are *not* `Scanner<S,R>` subtypes. Treat `Scanner` as the documented shape
> of the abstraction, not as the plugin interface you can register into; nothing in `core` consumes the interface.

### 3.2 `File` predicate reference

| Type | Shape | Null handling | Case / platform |
|---|---|---|---|
| `IOFileFilter` (`io/filter/IOFileFilter.java:53`) | `@FunctionalInterface` (`:52`) extending **both** `java.io.FileFilter` and `java.io.FilenameFilter` | implementor's choice | — |
| `DirectoryFileFilter` (`io/filter/DirectoryFileFilter.java:42`) | `INSTANCE` singleton (`:47`), `protected` ctor (`:49`) | `file != null && isDirectory()` (`:53-55`) — **null-safe** | — |
| `FileExtensionFilter` (`io/filter/FileExtensionFilter.java:57`) | `of(String extension)` factory (`:93-95`), `protected` ctor (`:63-66`) | `false` for `null` and for directories (`:69-72`); `false` for a blank extension (`:76-78`) | case-sensitive **unless** Windows (`:59`, `:83-85`) |
| `NameFileFilter` (`io/filter/NameFileFilter.java:28`) | `new NameFileFilter(name)` / `(name, caseSensitive)` (`:34-41`) | **not** null-safe: `file.getName()` at `:45` → `NullPointerException` | `caseSensitive` defaults to `true` (`:35`) |
| `TrueFileFilter` (`io/filter/TrueFileFilter.java:28`) | `INSTANCE` (`:33`), `protected` ctor (`:35`) | accepts `null` — both `accept(File)` (`:39-41`) and `accept(File,String)` (`:44-46`) return `true` unconditionally | — |

`IOFileFilter`'s second method is a bridge, not a second decision: the default `accept(File dir, String name)` builds a
`File` and forwards (`io/filter/IOFileFilter.java:58-61`). That is what lets one object serve both
`File.listFiles(FileFilter)` and `File.list(FilenameFilter)`.

### 3.3 Stream, reader and `File` helpers

| Helper | Behaviour | Evidence |
|---|---|---|
| `IOUtils.DEFAULT_BUFFER_SIZE` | `2048`, parsed from `"2048"` | `io/IOUtils.java:61`, `:66` |
| `IOUtils.BUFFER_SIZE` | `Integer.getInteger("microsphere.io.buffer.size", 2048)` — read **once** at class initialisation | `:80`, `:91` |
| `toByteArray(InputStream)` | `null` → `EMPTY_BYTE_ARRAY` (`:209-211`); otherwise copies through a `FastByteArrayOutputStream(BUFFER_SIZE)` (`:212-214`) | `:208-215` |
| `toString(in)` / `(in, encoding)` / `(in, charset)` / `(reader)` | thin aliases over the `copyToString` family | `:240-242`, `:268-270`, `:297-299`, `:323-325` |
| `copyToString(InputStream[, charset])` | `null` stream → `null`; **empty** stream → `""` | `:396-402` |
| `copyToString(Reader)` | `null` → `null`, else drains into a `StringBuilderWriter` | `:423-430` |
| `copy(InputStream, OutputStream)` / `copy(Reader, Writer)` | buffered loop + `flush()`; both stay open; returns the byte/char count | `:455-471`, `:496-512` |
| `close(Closeable)` | `null` → no-op; `IOException` swallowed and logged at `TRACE` | `:535-545` |
| `Files.readLines(File)` / `(File, Charset)` / `(Path)` / `(Path, Charset)` | opens `java.nio.file.Files.newInputStream` in try-with-resources and delegates to `IOUtils.readLines` | `nio/file/Files.java:69-71`, `:102-104`, `:133-135`, `:166-170` |
| `FileUtils.resolveRelativePath(File parent, File target)` | `null` if the parent is not a directory or the target is not under it; `""` when equal; always forward slashes | `io/FileUtils.java:66-81` |
| `FileUtils.getFileExtension(String)` | `substringAfterLast(name, ".")` — `"tar.gz"` → `"gz"`, `".hidden"` → `null` | `:100-102` |
| `FileUtils.deleteDirectory(File)` / `cleanDirectory(File)` / `forceDelete(File)` | return a **count** of deleted entries; `deleteDirectory` on a missing dir returns `0` | `:121-140`, `:162-168`, `:190-199` |
| `FileUtils.forceDeleteOnExit(File)` / `deleteDirectoryOnExit(File)` | schedule via `File#deleteOnExit` | `:216-222`, `:240-249` |
| `FileUtils.listFiles(File)` | `null` / missing / not-a-directory → the shared `EMPTY_FILE_ARRAY` (`:40`) | `:292-301` |
| `FileUtils.isSymlink(File)` | `Files.isSymbolicLink(file.toPath())` | `:317-319` |
| `FileUtils.getCanonicalFile(File)` | wraps the checked `IOException` through `ThrowableSupplier.execute` | `:339-341` |

### 3.4 `Closeable` ownership rules — who closes what

| API | Does it close your resource? | Do you have to close what it hands back? |
|---|---|---|
| `IOUtils.toByteArray`, `toString`, `copyToString`, `readLines` | **No** — the caller keeps ownership (`io/IOUtils.java:99`, `:129`, `:191`, `:222`) | n/a |
| `IOUtils.copy(in, out)` / `copy(reader, writer)` | **No** — "Both streams remain open after this operation" (`:435`, `:477`) | n/a |
| `IOUtils.close(Closeable)` | Yes, and it never throws (`:535-545`) | — |
| `nio.file.Files.readLines(File\|Path[, Charset])` | **Yes** — try-with-resources on the internal `InputStream` (`nio/file/Files.java:167`) | nothing to close |
| `SimpleFileScanner.scan(...)` | n/a — no streams are opened; only `File.listFiles()` (`scanner/SimpleFileScanner.java:95`) | — |
| `SimpleJarEntryScanner.scan(URL, ...)` | **No, and nothing does** — it calls `JarUtils.toJarFile(jarURL)` (`scanner/SimpleJarEntryScanner.java:98`), which opens a fresh `JarFile` per call (`util/jar/JarUtils.java:93`) and is never closed by the scanner | the returned `JarEntry`s keep no handle, but the archive handle is leaked on every call |
| `SimpleJarEntryScanner.scan(JarFile, ...)` | No — you opened it, you close it | yes |
| `SimpleClassScanner` | No streams; loads classes only when `requiredLoad` is `true` (`scanner/SimpleClassScanner.java:151`) | class-loading is not closable — treat `requiredLoad=true` as a permanent side effect |
| `StandardFileWatchService` | `close()` stops the loop, closes the JDK `WatchService` via `IOUtils.close` and clears the metadata cache (`io/StandardFileWatchService.java:200-207`) | yes — use try-with-resources; it is `AutoCloseable` (`:108`) |

The correct pattern for jar scanning is the one `ClassUtils` uses: open the archive yourself in try-with-resources and
call the `JarFile` overload (`util/ClassUtils.java:1448-1449`).

### 3.5 In-memory streams and writers

| Type | Base | Deltas worth knowing |
|---|---|---|
| `StringBuilderWriter` (`io/StringBuilderWriter.java:66`) | `java.io.Writer` | wraps a caller-supplied or fresh `StringBuilder` (`:73`, `:82`, `:93`); `close()` (`:198`) and `flush()` (`:206`) are no-ops; `getBuilder()` exposes the buffer (`:215`) |
| `FastByteArrayInputStream` (`io/FastByteArrayInputStream.java:67`) | `ByteArrayInputStream` | re-implements `read`/`skip`/`available`/`reset` without `synchronized` (`:104-154`); `close()` is a no-op (`:157-158`); `equals` compares the **whole backing array**, ignoring `pos`/`count` (`:161-166`), `hashCode` is `hash(buf)` (`:169-171`) |
| `FastByteArrayOutputStream` (`io/FastByteArrayOutputStream.java:55`) | `ByteArrayOutputStream` | capacity doubling with a `MAX_VALUE - 8` ceiling (`:63`, `:104-112`); `assertCapacity` throws `OutOfMemoryError` explicitly (`:114-118`); negative initial size → `IllegalArgumentException` (`:76-78`); overrides are **not** `synchronized` (`:123`, `:132`, `:145`, `:159`, `:166`); `close()` is a no-op and the buffer survives it (`:188-189`) |

---

## 4. Runnable examples (mined from the real test suite)

**Recursive scan with a `File` predicate** — from
`microsphere-java-core/src/test/java/io/microsphere/io/scanner/SimpleFileScannerTest.java:35-70`:

```java
SimpleFileScanner scanner = SimpleFileScanner.INSTANCE;                 // :31
File javaHome = new File(JAVA_HOME);                                    // :33

Set<File> all = scanner.scan(javaHome, true);                           // :37
assertFalse(all.isEmpty());
all = scanner.scan(javaHome, false);                                    // :40  non-recursive

Set<File> dirs = scanner.scan(javaHome, true, DirectoryFileFilter.INSTANCE);       // :46
assertFalse(dirs.isEmpty());

Set<File> bins = scanner.scan(javaHome, true, new NameFileFilter("bin"));          // :52
assertEquals(1, bins.size());                       // exactly one entry named "bin"

File tempDir = createRandomTempDirectory();                                         // :58-61
assertEquals(1, scanner.scan(tempDir, true).size());   // the root is always included
assertTrue(scanner.scan(tempDir, true).contains(tempDir));

File file = createRandomTempFile();                                                 // :66-69
assertEquals(1, scanner.scan(file, true).size());      // a plain File scans as itself
```

The "root is always included" assertion is the one to internalise: `SimpleFileScanner` tests `rootDirectory` against the
filter before descending (`io/scanner/SimpleFileScanner.java:91-93`), so `scan(emptyTempDir, true)` returns a one-element
set containing the directory.

**`FileExtensionFilter` — Windows vs everything else** —
`microsphere-java-core/src/test/java/io/microsphere/io/filter/FileExtensionFilterTest.java:28-79`:

```java
IOFileFilter filter = FileExtensionFilter.of("txt");     // :30

assertFalse(filter.accept(null));                       // :35
assertFalse(filter.accept(new File(JAVA_HOME)));         // :40  directories are rejected

File testFile = createTempFile("test", ".txt");
assertTrue(filter.accept(testFile));                     // :48
assertFalse(filter.accept(createTempFile("test", "")));  // :52  no extension

// the dot prefix is stripped by the constructor (:63-66)
assertTrue(FileExtensionFilter.of(".txt").accept(testFile));            // :66-68

// the FilenameFilter bridge (:58-61) — same object, different call shape
assertTrue(filter.accept(new File(JAVA_IO_TMPDIR), testFile.getName())); // :76
assertFalse(filter.accept(new File(JAVA_IO_TMPDIR), "file.xml"));         // :77

// matches() exposes the case flag directly, so the test is JDK-independent (:83-85)
assertTrue(filter.matches("txt", true));    assertFalse(filter.matches("TXT", true));   // :58-59
assertTrue(filter.matches("txt", false));   assertTrue(filter.matches("TXT", false));   // :60-61
```

**Reading lines and decoding** — `microsphere-java-core/src/test/java/io/microsphere/nio/file/FilesTest.java:52-65`
shows the `File` and `Path` overloads agreeing:

```java
Path testFilePath = Paths.get(TEST_FILE_RESOURCE.toURI());   // :54
String[] lines  = Files.readLines(testFilePath);             // :60
String[] lines2 = Files.readLines(testFile);                 // :61
assertArrayEquals(lines, lines2);
assertEquals(1, lines.length);
assertEquals("test", lines[0]);
```

**Buffer, null and `Closeable` semantics** —
`microsphere-java-core/src/test/java/io/microsphere/io/IOUtilsTest.java:63-221`:

```java
assertEquals(2048, IOUtils.DEFAULT_BUFFER_SIZE);   // :64
assertEquals(2048, IOUtils.BUFFER_SIZE);           // :65

assertEquals("Hello", IOUtils.toString(inputStream));                    // :93
assertNull(IOUtils.toString((InputStream) null));                         // :108
assertEquals("Hello", IOUtils.toString(inputStream, (String) null));       // :113  TEST_NULL_STRING is null (:57 of AbstractTestCase)
assertThrows(IllegalCharsetNameException.class,
        () -> IOUtils.toString(inputStream, ""));                          // :118
assertEquals("Hello", IOUtils.toString(inputStream, (Charset) null));      // :123  null Charset -> DEFAULT_CHARSET (:401)
assertNull(IOUtils.toString(null, (Charset) null));                        // :128
assertNull(IOUtils.toString(null, (String) null));                         // :133
assertEquals("Hello", IOUtils.toString(new StringReader("Hello")));        // :138

// close() is total
assertDoesNotThrow(() -> IOUtils.close(null));                             // :213
assertDoesNotThrow(() -> IOUtils.close(() -> { throw new IOException(); })); // :218-220
```

`IOUtilsTest.java:76-84` is the practical recipe for reading a `META-INF/services` file:

```java
URL resource = getResource("META-INF/services/io.microsphere.event.EventListener");  // :77
try (InputStream inputStream = resource.openStream()) {                               // :78
    String[] lines = IOUtils.readLines(inputStream);                                  // :79
    assertEquals(2, lines.length);
    assertEquals("io.microsphere.event.EchoEventListener", lines[0]);
}
```

**Byte-array streams** — `microsphere-java-core/src/test/java/io/microsphere/io/FastByteArrayInputStreamTest.java:46-139`
and `.../FastByteArrayOutputStreamTest.java:38-120`:

```java
FastByteArrayInputStream in  = new FastByteArrayInputStream(bytes);            // :36
FastByteArrayInputStream in2 = new FastByteArrayInputStream(bytes, 2, 5);      // :37

assertEquals('H', in.read());        assertEquals(-1, in.read() /* x5 */);      // :48-53
assertEquals(0, in.skip(-1));        assertEquals(4, in.skip(MAX_VALUE));        // :100-103  clamped, never negative
assertThrows(NullPointerException.class, () -> in.read(null, 0, 0));             // :80
assertThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[8], 0, MAX_VALUE)); // :94
assertEquals(in, in2);   // equals() looks only at the shared buf, so pos is ignored (:132 + io/FastByteArrayInputStream.java:161-166)

FastByteArrayOutputStream out = new FastByteArrayOutputStream(2);                // :30  grows 2 -> 4 -> 8 ...
assertThrows(IllegalArgumentException.class, () -> new FastByteArrayOutputStream(-1)); // :40
assertThrows(OutOfMemoryError.class, () -> out.assertCapacity(Integer.MAX_VALUE));     // :119
byte[] before = out.toByteArray();
out.close();
assertArrayEquals(before, out.toByteArray());   // :112-114  close() is a no-op; the buffer survives
```

**Watching files and directories** — `microsphere-java-core/src/test/java/io/microsphere/io/StandardFileWatchServiceTest.java`.
The kind-mapping contract (`:97-118`) and the one deliberate failure (`:120-123`):

```java
assertArrayEquals(ALL_WATCH_EVENT_KINDS, toWatchEventKinds());                  // :99
assertArrayEquals(ALL_WATCH_EVENT_KINDS, toWatchEventKinds(new Kind[0]));        // :100
assertArrayEquals(ALL_WATCH_EVENT_KINDS, toWatchEventKinds((Kind[]) null));      // :101  blank -> all kinds
assertArrayEquals(ofArray(ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE),
                  toWatchEventKinds(CREATED, MODIFIED, DELETED));                // :103  order preserved

assertEquals(CREATED, toKind(ENTRY_CREATE));   // :115
assertThrows(IllegalArgumentException.class, () -> toKind(OVERFLOW));            // :122
```

The end-to-end shape, condensed from `testFile` (`:126-163`) and `testDirectory` (`:166-213`) — note the ordering, which
is the API contract:

```java
try (StandardFileWatchService watchService = new StandardFileWatchService(commonPool())) {  // :135
    watchService.watch(targetFile, listener, Kind.values());   // :137  register BEFORE start()
    watchService.watch(targetFile, new LoggingFileChangedListener());  // :138
    watchService.start();                                      // :142
    assertTrue(watchService.isStarted());                      // :144
    assertThrows(IllegalStateException.class, watchService::start);  // :147
    Files.copy(sourcePath, targetFilePath, REPLACE_EXISTING);  // :154
    countDownLatch.await();                                    // :157  3 events: CREATED, MODIFIED, DELETED
    watchService.stop();                                       // :159
    assertFalse(watchService.isStarted());                     // :161
}
```

**Listener fan-out and the default routing** — `.../io/event/FileChangedListenerTest.java:45-103` and
`.../io/FileWatchServiceTest.java:22-31`:

```java
FileChangedListener listener = new DefaultFileChangedListener();   // :37 (an empty impl: io/event/DefaultFileChangedListener.java:26-27)
assertEquals(NORMAL_PRIORITY, listener.getPriority());             // :47  Prioritized default
assertThrows(NullPointerException.class, () -> listener.onEvent(null));  // :80  kind switch at io/event/FileChangedListener.java:97-110

// FileWatchService's Iterable overload just loops (io/FileWatchService.java:76-78)
FileWatchService service = (file, l, kinds) -> log("{}", file);     // :22-24
service.watch(file, new LoggingFileChangedListener(), values());    // :29
service.watch(file, ofList(new LoggingFileChangedListener()), values()); // :30
```

---

## 5. Compatibility notes

| Capability | Java 8 | Java 9 – 17 | Java 18+ | Notes |
|---|---|---|---|---|
| `java.nio.file.WatchService`, `Path`, `Files.newInputStream` | yes (JDK 7) | yes | yes | no gating in the source; used unconditionally |
| `Charset.defaultCharset()` (behind `DEFAULT_CHARSET`, `nio/charset/CharsetUtils.java:43`) | platform charset | platform charset | **UTF-8** (JEP 400) | the only row that silently changes results across the CI matrix |
| `SystemUtils.NATIVE_ENCODING` (`util/SystemUtils.java:337`) | `null` | `null` | non-`null` | not consumed by this slice's code paths |
| `System.lineSeparator()` (`constants/SeparatorConstants.java:41`) | `\r\n` on Windows, `\n` elsewhere | idem | idem | drives `IOUtils.readLines` splitting |
| `FileExtensionFilter` case rules (`io/filter/FileExtensionFilter.java:59`) | Windows → insensitive; else sensitive | idem | idem | identical to the NTFS case-insensitivity assumption |
| `Integer.getInteger` buffer size (`io/IOUtils.java:91`) | yes | yes | yes | set it before the first `IOUtils` class load or it is ignored |
| `ByteArrayOutputStream`/`ByteArrayInputStream` overrides | yes | yes | yes | `MAX_ARRAY_SIZE = MAX_VALUE - 8` mirrors the JDK's own limit (`io/FastByteArrayOutputStream.java:63`) |

Cross-platform, the two facts that actually bite are:

* **Windows `mklink` needs privileges.** The test helper creates symlinks with `mklink /D` on Windows and `ln -s`
  elsewhere (`microsphere-java-core/src/test/java/io/microsphere/AbstractTestCase.java:160-176`). `FileUtils.isSymlink`
  (`io/FileUtils.java:317-319`) and the symlink-aware deletion paths (`:128-130`, `:246-248`) therefore behave
  differently in a CI container than on a developer laptop.
* **Windows event coalescing.** `StandardFileWatchService` registers the *directory* of a watched file
  (`io/StandardFileWatchService.java:275`), so a single logical write commonly produces several `ENTRY_MODIFY` events on
  Windows. The test suite absorbs this with a `CountDownLatch(3)` plus explicit create→modify→delete choreography
  (`StandardFileWatchServiceTest.java:133-157`, `:216-250`) rather than by counting events.

---

## 6. Pitfalls and known limits

1. **`OVERFLOW` is not handled — it kills the watch loop silently.** Only `ENTRY_CREATE/DELETE/MODIFY` are ever
   registered (`io/StandardFileWatchService.java:133-137`), but the OS can still deliver `OVERFLOW` (e.g. when the
   Windows backlog is exceeded). The event loop at `:209-241` iterates `pollEvents()` (`:216`) and passes every kind
   straight through to `toKind` (`:245`), which throws `IllegalArgumentException` for anything unrecognised (`:324-327`)
   — the behaviour `StandardFileWatchServiceTest.java:120-123` pins. The loop has **no `catch`**, only
   `finally { watchKey.reset(); }` (`:233-237`), so the exception escapes the submitted `Callable`, is captured in
   `eventLoopFuture` (`:210`) and — because that future is never `get()`-ed — nobody notices. `isStarted()` keeps
   returning `true` (`:196-198`) while no further events are dispatched. If your process must survive event loss, poll
   the `Future` yourself or restart the service.
2. **`watch()` after `start()` never registers.** `start()` snapshots the metadata cache and calls
   `directoryPath.register(watchService, kinds)` once (`:178` → `:253-264`), and the `WatchService` reference is only
   stored afterwards (`:182`). A later `watch(...)` (`:267-272`) only mutates the cache. The JDK spec has no
   "register-into-a-live-WatchService" hook here, so the new path is silently never watched. Every test registers first
   (`StandardFileWatchServiceTest.java:137-142`, `:172-188`, `:298`).
3. **Watching a file reports its whole directory.** For a file, the cache key is `filePath.getParent()` (`:275`) and the
   dispatch guard is `isDirectory(dirPath, NOFOLLOW_LINKS) || metadata.filePaths.contains(filePath)` (`:225`). Since a
   parent directory *is* a directory, the first operand short-circuits and the `filePaths` set (`:270`, `:333`) never
   filters anything — your listener receives events for sibling files it never asked about. Same for the kinds: they are
   captured only when the cache entry is **created** (`:276-281`), so the first caller's `Kind...` silently applies to
   every later watcher of that directory.
4. **`Scanner<S,R>` is unimplemented.** See 3.1. Do not write `List<Scanner>` injection points expecting the
   `Simple*Scanner` beans; they are singletons of unrelated types (`scanner/SimpleFileScanner.java:57`,
   `scanner/SimpleJarEntryScanner.java:65`, `scanner/SimpleClassScanner.java:69`).
5. **`SimpleJarEntryScanner.scan(URL, ...)` leaks the archive handle.** It obtains a fresh `JarFile` via
   `JarUtils.toJarFile(jarURL)` (`scanner/SimpleJarEntryScanner.java:98`; allocation at `util/jar/JarUtils.java:93`) and
   closes nothing. Prefer the `JarFile` overload inside try-with-resources — the pattern `util/ClassUtils.java:1448-1449`
   uses. Also note `toJarFile` returns `null` on an unresolvable path or an `IOException` it only logs at `TRACE`
   (`util/jar/JarUtils.java:88-90`, `:94-98`), and `JarUtils.filter(null, …)` degrades to an empty list (`:194-196`), so
   a corrupt jar gives you an **empty set**, not an error.
6. **`SimpleJarEntryScanner`'s non-recursive rule is substring-based.** `isFileEntry` looks for
   `jarEntryName.indexOf(relativePath) == 0` then requires no further `/` after it
   (`scanner/SimpleJarEntryScanner.java:155-161`). `SimpleJarEntryScannerTest.java:58-61` documents the sharp edge:
   `"java/lang/String.class"` matches relative path `"java/lang/"` but **not** `"java/lang"` — you must pass a
   trailing-slash prefix.
7. **`SimpleClassScanner` returns nothing unless the classes are already loaded.** With the default
   `requiredLoad=false`, each name goes through `findLoadedClass` and `null` results are dropped
   (`scanner/SimpleClassScanner.java:150-155`). Pass `requiredLoad=true` to actually load (side effects, static
   initialisers, class-initialisation errors). `SimpleClassScannerTest.java:39-43` only gets 4 classes because it asks
   for `true`.
8. **`readLines(InputStream, String)` does not tolerate a `null` encoding.** `io/IOUtils.java:150-152` calls
   `Charset.forName` directly, while `copyToString(in, encoding)` applies `defaultIfNull(encoding, FILE_ENCODING)`
   (`:346-349`). The javadoc at `:145` promises the default for "null or blank" for the `readLines` overload; the code
   does not deliver it. Use the `Charset` overload (`:183-186`) or `copyToString`.
9. **`copy(in, out)` throws `NullPointerException`, not `IllegalArgumentException`.** It uses
   `Objects.requireNonNull` (`io/IOUtils.java:456-457`, import at `:47`), while its own javadoc states an IAE will be
   thrown (`:439`); same for `copy(Reader, Writer)` (`:497-498` vs `:480`). This is a deviation from the house
   `Assert.assertNotNull`-raises-IAE convention.
10. **`toString`/`copyToString` return `null` only for a `null` stream.** `copyToString` checks identity against
    `EMPTY_BYTE_ARRAY` (`io/IOUtils.java:398-400`), and `toByteArray` returns that shared instance **only** on the
    `null` branch (`:209-211`); for a real, empty stream the copy comes from
    `FastByteArrayOutputStream.toByteArray()` → `copyOf(buf, count)` (`io/FastByteArrayOutputStream.java:159-161`), a
    fresh array. So an empty resource yields `""`, and only `null` in gives `null` out — despite the "empty or null"
    wording at `:236`, `:293`, `:343`.
11. **`BUFFER_SIZE` is fixed at class-init.** `getInteger` runs once in the static field initialiser
    (`io/IOUtils.java:91`); `System.setProperty(IOUtils.BUFFER_SIZE_PROPERTY_NAME, "4096")` after first use has no
    effect, and a malformed value fails the class initialiser.
12. **`NameFileFilter` is the one non-null-safe predicate.** `accept` dereferences `file.getName()`
    (`io/filter/NameFileFilter.java:44-48`) → NPE on `null`, whereas `DirectoryFileFilter` (`:53-55`),
    `FileExtensionFilter` (`:69-72`) and `TrueFileFilter` (`:39-41`) all survive `null`. Because
    `SimpleFileScanner` hands the **root** to the filter first (`scanner/SimpleFileScanner.java:91`), passing a
    `NameFileFilter` with a `null` root throws rather than returning an empty set.
13. **`FileExtensionFilter` never matches a directory.** Directories are rejected before the name is even read
    (`io/filter/FileExtensionFilter.java:70-72`). Combined with `SimpleFileScanner`'s recursion — which adds a
    subdirectory to the result set *and* descends into it (`scanner/SimpleFileScanner.java:99-104`) — a
    `.class`-extension filter cannot be used to enumerate class directories.
14. **No symlink protection in the scanners.** `SimpleFileScanner` recurses on `subFile.isDirectory()`
    (`scanner/SimpleFileScanner.java:102`), which follows links, and never consults `FileUtils.isSymlink`
    (`io/FileUtils.java:317-319`). Only the *deletion* helpers are link-aware (`:128-130`, `:246-248`). A directory
    symlink loop under your root will recurse until the stack overflows.
15. **The `Fast*` streams are not thread-safe.** The overrides in `io/FastByteArrayOutputStream.java:123-183` carry no
    `synchronized` modifier, unlike the JDK methods they replace, and `io/FastByteArrayInputStream.java:104-154` mutate
    inherited `pos`/`mark` without synchronisation. They also share the JDK's protected `buf`/`count` fields, so
    subclassing or reflection on them is not safe.
16. **`deleteDirectory` counts, and only throws at the end.** A missing directory returns `0` (`io/FileUtils.java:122-124`),
    but a failed final `directory.delete()` raises `IOException` after the contents may already be gone (`:132-137`) — so
    a non-zero return plus an exception is a possible outcome, and the tree can be half-deleted.
    `FileUtilsTest.java:126-129` (IOException while deleting), `:131-136` (deleting a link) and `:219-265`
    (`testForceDeleteOnIOException`) exercise exactly these races.

---

## 7. See also

* [Networking and URL protocols](net-and-url-protocols.md) — `net.URLUtils` consumes `IOUtils`, and the
  `jar:`/`file:` protocol handling that feeds `SimpleJarEntryScanner.scan(URL, …)`
* [Class loading and artifacts](classloading-and-artifacts.md) — `classloading` consumes `FastByteArrayInputStream`;
  `util.ClassUtils.findClassNamesInClassPath` is the scanner's main client
* [Collections and filters](collections-and-filters.md) — where the **generic** `Filter<T>` SPI lives, i.e.
  `io.microsphere.filter`, as opposed to the `File` predicates in `io.microsphere.io.filter` used here
* [Event dispatching](events.md) — `EventDispatcher`, `EventListener` and `Prioritized`, which `io.event` bridges to
  file-change events
* [Serialization](serialization.md) — `io.microsphere.io.serializer`, the sibling package under `io.microsphere.io`
* [Utilities, constants and text](util-and-constants.md) — `SystemUtils.FILE_ENCODING`, `SeparatorConstants`,
  `StringUtils.split` and `ClassLoaderUtils`, which the helpers above delegate to
* Developer-facing deep dive: [I/O, NIO, scanning and file filters — Developer Guide](../../developer-guide/microsphere-java-core/io-and-scanning.md)
* [中文用户指南](zh/io-and-scanning.md)
