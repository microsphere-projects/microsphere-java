# Networking and URL Protocols

**Module**: `io.github.microsphere-projects:microsphere-java-core`
**Packages**: `io.microsphere.net`, `io.microsphere.net.classpath`, `io.microsphere.net.console`

Two things live here: a URL helper class (`URLUtils`), and an extensible `URLStreamHandler` mechanism that adds
`classpath:` and `console:` protocols and supports **sub-protocol chaining** (`console:text://...`).

---

## 1. `URLUtils`

```java
public abstract class URLUtils implements Utils {

    public static final String DEFAULT_ENCODING;                    // SystemUtils.FILE_ENCODING
    public static final URL[] EMPTY_URL_ARRAY;
    public static final String FILE_URL_PREFIX = "file:/";

    public static final String HANDLER_PACKAGES_PROPERTY_NAME = "java.protocol.handler.pkgs";
    public static final String DEFAULT_HANDLER_PACKAGE_PREFIX = "sun.net.www.protocol";
    public static final char HANDLER_PACKAGES_SEPARATOR_CHAR = '|';
    public static final String HANDLER_CONVENTION_CLASS_NAME = "Handler";
    public static final String SUB_PROTOCOL_MATRIX_NAME = "_sp";

    // Parsing and building
    @Nonnull public static URL ofURL(String url)
    @Nonnull public static String buildURI(String... paths)
    @Nonnull public static String normalizePath(String path)
    @Nonnull public static String encode(String value)
    @Nonnull public static String encode(String value, String encoding) throws IllegalArgumentException
    @Nonnull public static String decode(String value)
    @Nonnull public static String decode(String value, String encoding) throws IllegalArgumentException

    // Query and matrix parameters
    @Nonnull @Immutable public static Map<String, List<String>> resolveQueryParameters(String url)
    @Nonnull @Immutable public static Map<String, List<String>> resolveMatrixParameters(String url)
    @Nullable public static String buildMatrixString(Map<String, List<String>> matrixParameters)
    @Nonnull public static String buildMatrixString(String name, String... values)

    // Structure
    @Nullable public static String resolveProtocol(String url)
    @Nullable public static String getSubProtocol(String url)
    @Nonnull @Immutable public static List<String> resolveSubProtocols(URL url)
    @Nonnull @Immutable public static List<String> resolveSubProtocols(String url)
    public static String resolveAuthority(URL url)
    @Nullable public static String resolveAuthority(String authority)
    @Nonnull public static String resolvePath(URL url)
    @Nullable public static String resolveArchiveEntryPath(URL archiveFileURL)
    @Nullable public static String resolveBasePath(URL url)
    @Nullable public static File resolveArchiveFile(URL resourceURL)

    public static boolean isDirectoryURL(URL url)
    public static boolean isJarURL(URL url)
    public static boolean isArchiveURL(URL url)
    public static boolean isArchiveProtocol(String protocol)     // jar | zip | war | ear

    // Handler registration
    public static void attachURLStreamHandlerFactory(URLStreamHandlerFactory factory)
    @Nullable public static URLStreamHandlerFactory getURLStreamHandlerFactory()
    public static void registerURLStreamHandler(ExtendableProtocolURLStreamHandler handler)
    public static void registerURLStreamHandler(String protocol, URLStreamHandler handler)

    @Nonnull public static String toExternalForm(URL url)
    public static void close(URLConnection connection)            // HttpURLConnection.disconnect()
}
```

```java
URL jarUrl = URLUtils.ofURL("jar:file:/libs/app.jar!/META-INF/MANIFEST.MF");
URLUtils.isArchiveURL(jarUrl);                    // true
URLUtils.resolveArchiveFile(jarUrl);              // File(/libs/app.jar)
URLUtils.resolveArchiveEntryPath(jarUrl);         // "META-INF/MANIFEST.MF"

URLUtils.resolveQueryParameters("http://h/p?a=1&a=2&b=3");  // {a=[1,2], b=[3]}
URLUtils.buildURI("context", "sub", "resource.yml");
```

> [!NOTE]
> Matrix parameters use the `_sp` name convention (`SUB_PROTOCOL_MATRIX_NAME`), the same mechanism the sub-protocol
> resolver uses, so `resolveMatrixParameters` and `buildMatrixString` are round-trip consistent.

`attachURLStreamHandlerFactory` is the interesting one: `java.net.URL.setURLStreamHandlerFactory` can only be called
**once per JVM**, so this method composes instead of failing — if no factory exists it installs yours; if the current
factory is already a `CompositeURLStreamHandlerFactory` it appends; otherwise it reflectively clears `URL.factory`,
installs a composite, and appends. That is why application code should always call
`URLUtils.attachURLStreamHandlerFactory(...)` rather than the JDK method directly.

---

## 2. Handler factories

| Class | Declaration | Use |
|---|---|---|
| `MutableURLStreamHandlerFactory<H extends URLStreamHandler>` | `()`, `(Map<String, H>)`; `addURLStreamHandler(String, H)` → `this`; `removeURLStreamHandler(String)`; `getURLStreamHandler(String)`; `getHandlers()`; `clearHandlers()` → `this`; `createURLStreamHandler(String)` | protocol → handler registry you control at runtime. **Not thread-safe.** |
| `CompositeURLStreamHandlerFactory` | `()`, `(Collection<...>)`, `(Iterable<...>)`; `addURLStreamHandlerFactory(factory)` → `this`; `final createURLStreamHandler(String)`; `protected getFactories()`, `protected getComparator()` | first factory that returns a non-null handler wins; child factories are ordered by `getComparator()` |
| `DelegatingURLStreamHandlerFactory` | `(URLStreamHandlerFactory delegate)`; `createURLStreamHandler(String)`; `protected final getDelegate()` | intercept resolution |
| `StandardURLStreamHandlerFactory` | `createURLStreamHandler(String)` | delegates to the JDK's own protocol set |
| `ServiceLoaderURLStreamHandlerFactory` | `()`; `public static void attach()` | **the entry point** — see §3 |

`ServiceLoaderURLStreamHandlerFactory.attach()` is the single call that wires everything:

1. loads every `java.net.URLStreamHandlerFactory` implementation from SPI,
2. wraps them plus a fallback `MutableURLStreamHandlerFactory` pre-populated with every
   `META-INF/services/io.microsphere.net.ExtendableProtocolURLStreamHandler` entry (keyed by `getProtocol()`) inside a
   `CompositeURLStreamHandlerFactory`,
3. installs the result through `URLUtils.attachURLStreamHandlerFactory(...)`.

> [!IMPORTANT]
> The static method is `attach()`, not `install()`. And nothing registers these handlers automatically as a side
> effect of loading the jar — `microsphere-java-core` ships **no** `META-INF/services/java.net.URLStreamHandlerFactory`
> in `src/main/resources` (one exists only in the module's *test* resources, pointing at
> `StandardURLStreamHandlerFactory`). Call `ServiceLoaderURLStreamHandlerFactory.attach()` once during startup.

---

## 3. Built-in protocols

Registered in `microsphere-java-core/src/main/resources/META-INF/services/io.microsphere.net.ExtendableProtocolURLStreamHandler`:

```
io.microsphere.net.classpath.Handler
io.microsphere.net.console.Handler
```

Protocol names come from `ProtocolConstants`: `CLASSPATH_PROTOCOL = "classpath"`, `CONSOLE_PROTOCOL = "console"`
(alongside `file`, `http`, `https`, `ftp`, `jar`, `war`, `ear`, `zip`).

### `classpath:`

```java
public class io.microsphere.net.classpath.Handler extends ExtendableProtocolURLStreamHandler {
    @Override public URLConnection openConnection(URL u, Proxy proxy) throws IOException
}
```

Resolution joins the URL's authority and path into a resource name and asks the class loader for it; a miss throws
`IOException("No Resource[classpath='...'] was not found!")`.

```java
ServiceLoaderURLStreamHandlerFactory.attach();

URL url = new URL("classpath://META-INF/microsphere/configuration-properties.json");
try (InputStream in = url.openStream()) { /* ... */ }

// Leading-slash forms are normalized as well
URL other = new URL("classpath:////META-INF/services/io.microsphere.convert.Converter");
```

### `console:`

```java
public class io.microsphere.net.console.Handler extends ExtendableProtocolURLStreamHandler {
    @Override public URLConnection openConnection(URL url, Proxy proxy) throws IOException   // → ConsoleURLConnection
}

public class ConsoleURLConnection extends URLConnection {
    public ConsoleURLConnection(URL url)
    @Override public void connect()               // no-op
    @Override public InputStream getInputStream() // System.in
    @Override public OutputStream getOutputStream() // System.out
}
```

> [!WARNING]
> The protocol is `console` — **there are no `out:` or `err:` protocols** in this codebase. A console URL still needs
> an authority, e.g. `console://localhost:12345/abc`; the host/port are ignored, the connection just maps onto the
> process's standard streams. This is what makes `console:` usable as a portable "write to stdout via a URL" target in
> tooling.

---

## 4. Sub-protocol chaining

`ExtendableProtocolURLStreamHandler` splits a URL like `console:text://host/path` into the sub-protocol list
(`["console", "text"]`) via `URLUtils.resolveSubProtocols`, then asks its registered
`SubProtocolURLConnectionFactory` instances to build the connection.

```java
public abstract class ExtendableProtocolURLStreamHandler extends URLStreamHandler {

    public ExtendableProtocolURLStreamHandler()                 // also appends this package to java.protocol.handler.pkgs
    public ExtendableProtocolURLStreamHandler(String protocol)

    @Nonnull @Immutable public static Set<String> getHandlePackages()
    public static String getHandlePackagesPropertyValue()        // System.getProperty("java.protocol.handler.pkgs")

    public void init()                                          // loads SubProtocolURLConnectionFactory SPI + registers itself
    protected void initSubProtocolURLConnectionFactories(List<SubProtocolURLConnectionFactory> factories)   // override point
    public void customizeSubProtocolURLConnectionFactories(Consumer<List<SubProtocolURLConnectionFactory>> customizer)

    @Override protected final URLConnection openConnection(URL u) throws IOException
    @Override public URLConnection openConnection(URL u, Proxy p) throws IOException
    protected URLConnection openFallbackConnection(URL url, Proxy proxy) throws IOException   // returns null
    protected String reformSpec(URL url, String spec, int start, int end, int limit)
    protected List<String> resolveSubProtocols(URL url)
    protected String resolveAuthority(URL url)
    protected String resolvePath(URL url)
    public final String getProtocol()
}

public interface SubProtocolURLConnectionFactory {
    boolean supports(URL url, List<String> subProtocols);
    URLConnection create(URL url, List<String> subProtocols, Proxy proxy) throws IOException;
}

public class CompositeSubProtocolURLConnectionFactory implements SubProtocolURLConnectionFactory {
    public CompositeSubProtocolURLConnectionFactory()
    public CompositeSubProtocolURLConnectionFactory(Iterable<SubProtocolURLConnectionFactory> factories)
    public CompositeSubProtocolURLConnectionFactory add(SubProtocolURLConnectionFactory factory)
    public CompositeSubProtocolURLConnectionFactory add(SubProtocolURLConnectionFactory... factories)
    public boolean remove(SubProtocolURLConnectionFactory factory)
}

public class DelegatingURLConnection extends URLConnection {
    public DelegatingURLConnection(URLConnection delegate)
    // full delegation: connect, timeouts, content, headers, request properties
}
```

> [!IMPORTANT]
> `openConnection(URL)` is **`final`** — subclass extension happens in `openConnection(URL, Proxy)` (and the
> `resolve*` / `reformSpec` / `initSubProtocolURLConnectionFactories` hooks). `openFallbackConnection` returning
> `null` is what makes an unmatched URL fail fast rather than silently resolve to something else.
> `parseURL`, `equals`, `hostsEqual`, `hashCode` and `toExternalForm` are also `final`, so the sub-protocol parsing
> is not overridable by design.

Two registration paths exist, and they are independent:

* **Handler convention** — the `ExtendableProtocolURLStreamHandler` no-arg constructor appends its parent package to
  the `java.protocol.handler.pkgs` system property, so the JDK can find `<package>.<protocol>.Handler` by
  reflection. This only helps *after* the handler class has been constructed once.
* **Explicit attachment** — `ServiceLoaderURLStreamHandlerFactory.attach()` (or
  `URLUtils.registerURLStreamHandler(...)`) registers the handler in a `MutableURLStreamHandlerFactory`.
  This is the reliable route; call it at startup.

### Writing your own protocol

```java
package com.example.net.vfs;           // protocol name comes from the package: "vfs"

import io.microsphere.net.ExtendableProtocolURLStreamHandler;

public class Handler extends ExtendableProtocolURLStreamHandler {

    @Override
    public URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        String path = resolvePath(url);
        if (path == null || path.isEmpty()) {
            return openFallbackConnection(url, proxy);
        }
        return new VfsURLConnection(url, path);
    }

    @Override
    protected void initSubProtocolURLConnectionFactories(List<SubProtocolURLConnectionFactory> factories) {
        factories.add(new VfsGzipSubProtocolFactory());      // enables "vfs:gzip://..."
    }
}
```

Then register it:

```
# src/main/resources/META-INF/services/io.microsphere.net.ExtendableProtocolURLStreamHandler
com.example.net.vfs.Handler
```

and call `ServiceLoaderURLStreamHandlerFactory.attach()` once at startup. Because `openConnection(URL)` is final and
handles the chaining, sub-protocol support arrives for free from the factories you contribute.

---

## 5. See also

* [Core Utilities](core-utilities.md#10-constants-package) — `ProtocolConstants`, `PathConstants.ARCHIVE_ENTRY_SEPARATOR`
* [I/O, Files and Watching](io-and-file-watch.md) — reading from the URLs these handlers produce
* [Class Loading and Artifacts](classloading-and-artifacts.md) — archive URLs as `Artifact` sources
* [Reference](reference.md) — `java.protocol.handler.pkgs` interactions

[← Previous: I/O, Files and Watching](io-and-file-watch.md) · [Index](README.md) · [Next: Logging →](logging.md)
