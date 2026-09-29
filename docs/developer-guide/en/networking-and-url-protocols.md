# Networking and URL Protocols

> Read this page in: [中文](../zh/networking-and-url-protocols.md) · [English](networking-and-url-protocols.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Module | `microsphere-java-core` |
| Packages | `io.microsphere.net`, `io.microsphere.net.classpath`, `io.microsphere.net.console` |
| Protocols added to `java.net.URL` | `classpath:` and `console:` |
| Single wiring call | `ServiceLoaderURLStreamHandlerFactory.attach()` |
| Built-in handler list | `microsphere-java-core/src/main/resources/META-INF/services/io.microsphere.net.ExtendableProtocolURLStreamHandler` |
| JVM constraint | `URL.setURLStreamHandlerFactory` accepts **one call per JVM**; `URLUtils.attachURLStreamHandlerFactory` composes instead of failing |

`io.microsphere.net` does two independent things. `URLUtils` is a stateless helper for parsing,
building, and encoding URLs, including archive (`jar:`/`war:`) inspection and query/matrix
parameter resolution. The rest of the package is an extensible `URLStreamHandler` mechanism: the
abstract `ExtendableProtocolURLStreamHandler` adds the `classpath:` and `console:` protocols and
supports **sub-protocol chaining** (a protocol plus colon-separated sub-protocols such as
`console:text://host/path`), letting one handler fan out to many connection kinds.

---

## 1. `URLUtils`

`URLUtils` is a `public abstract class URLUtils implements Utils` with a private constructor —
you only ever call its static methods. The constants that matter for handler work:

| Constant | Value / Meaning |
|---|---|
| `HANDLER_PACKAGES_PROPERTY_NAME` | `"java.protocol.handler.pkgs"` — the JDK's handler-package search property |
| `DEFAULT_HANDLER_PACKAGE_PREFIX` | `"sun.net.www.protocol"` — the JDK's builtin prefix, forbidden for custom handlers |
| `HANDLER_PACKAGES_SEPARATOR_CHAR` | `'|'` |
| `HANDLER_CONVENTION_CLASS_NAME` | `"Handler"` — the class-name convention per protocol sub-package |
| `SUB_PROTOCOL_MATRIX_NAME` | `"_sp"` — matrix-parameter name carrying the sub-protocol list |
| `DEFAULT_ENCODING` | taken from `SystemUtils.FILE_ENCODING` (`file.encoding`), used by `encode`/`decode` |
| `FILE_URL_PREFIX` | `"file:/"` |

Representative methods (all verified against source):

```java
// parsing and building
URL url = URLUtils.ofURL("jar:file:/libs/app.jar!/META-INF/MANIFEST.MF");  // IllegalArgumentException if malformed
URLUtils.buildURI("context", "sub", "resource.yml");                       // "/context/sub/resource.yml"
// archive inspection
URLUtils.isArchiveURL(url);             // true: jar|zip|war|ear protocols, or a readable jar file: URL
URLUtils.resolveArchiveFile(url);       // File(/libs/app.jar) — null if absent
URLUtils.resolveArchiveEntryPath(url);  // "META-INF/MANIFEST.MF"
// parameters: unmodifiable Map<name, List<value>>
URLUtils.resolveQueryParameters("http://h/p?a=1&a=2&b=3");  // {a=[1, 2], b=[3]}
URLUtils.resolveMatrixParameters("/p;a=1;b=2?q");           // {a=[1], b=[2]}
URLUtils.buildMatrixString("k", "v1", "v2");                // ";k=v1;k=v2"
URLUtils.resolveSubProtocols("custom:sub1:sub2://host/path"); // [sub1, sub2]
// handler registration
URLUtils.attachURLStreamHandlerFactory(factory);            // see §2
URLUtils.registerURLStreamHandler(handler);                 // keyed by handler.getProtocol()
URLUtils.close(connection);                                 // HttpURLConnection.disconnect(); ignores other types
```

> [!NOTE]
> Sub-protocols are stored in the `_sp` matrix parameter (`SUB_PROTOCOL_MATRIX_NAME`), so
> `resolveMatrixParameters` and `buildMatrixString` round-trip exactly what the sub-protocol
> parser produces.

`attachURLStreamHandlerFactory` exists because the JDK method it wraps
(`java.net.URL.setURLStreamHandlerFactory`) throws an `Error` on a second call — the global
factory slot is write-once per JVM. The Microsphere version composes instead:

1. no factory installed yet → install the given one directly;
2. current factory is already a `CompositeURLStreamHandlerFactory` → append to it;
3. a foreign factory is installed → wrap it plus yours in a new composite, reflectively clear
   `URL.factory` (`clearURLStreamHandlerFactory`), re-install the composite, then append.

That is why application code should always go through `URLUtils.attachURLStreamHandlerFactory(...)`
rather than the JDK method directly.

---

## 2. Stream handler factories and `attach()`

| Class | Key members | Use |
|---|---|---|
| `MutableURLStreamHandlerFactory<H extends URLStreamHandler>` | `(Map<String, H>)` ctor; `addURLStreamHandler(String, H)`; `removeURLStreamHandler(String)`; `getURLStreamHandler(String)`; `getHandlers()`; `clearHandlers()` | protocol → handler registry you control at runtime. **Not thread-safe** (documented in its javadoc) |
| `CompositeURLStreamHandlerFactory` | `addURLStreamHandlerFactory(factory)` → `this`; `final createURLStreamHandler(String)`; `protected getFactories()`, `protected getComparator()` | first child returning a non-null handler wins; children ordered by `getComparator()` (default `Prioritized.COMPARATOR`) |
| `DelegatingURLStreamHandlerFactory` | `(URLStreamHandlerFactory delegate)`; `protected final getDelegate()` | intercept handler resolution |
| `StandardURLStreamHandlerFactory` | `createURLStreamHandler(String)` | re-exposes the JDK's own protocol set as a factory |
| `ServiceLoaderURLStreamHandlerFactory` | `public static void attach()` | **the entry point** |

`ServiceLoaderURLStreamHandlerFactory.attach()` is the single call that wires everything:

1. loads every `java.net.URLStreamHandlerFactory` from SPI (`java.util.ServiceLoader`),
2. loads every `io.microsphere.net.ExtendableProtocolURLStreamHandler` from SPI and pre-populates
   a fallback `MutableURLStreamHandlerFactory` with them, keyed by `getProtocol()`,
3. composes both groups into a `CompositeURLStreamHandlerFactory`,
4. installs it via `URLUtils.attachURLStreamHandlerFactory(...)`.

> [!IMPORTANT]
> Nothing registers these handlers as a side effect of loading the jar. The module's
> `src/main/resources` ships **no** `META-INF/services/java.net.URLStreamHandlerFactory` file —
> one exists only in the module's *test* resources (pointing at `StandardURLStreamHandlerFactory`).
> The static method is `attach()`, and you must call it once during startup.

> [!WARNING]
> Step 3 of `attachURLStreamHandlerFactory` reads and writes the private static field
> `URL.factory` reflectively (force-access via `FieldUtils`). On JDK 16+, where strong
> encapsulation is the default (JEP 396), a blocked access is reported by the library with the
> exact JVM option to add: `--add-opens java.base/java.net=ALL-UNNAMED`. This path only matters
> when some other code has already installed a global factory.

---

## 3. `classpath:` protocol

Registered in `META-INF/services/io.microsphere.net.ExtendableProtocolURLStreamHandler`:

```
io.microsphere.net.classpath.Handler
io.microsphere.net.console.Handler
```

Protocol names match `io.microsphere.constants.ProtocolConstants`: `CLASSPATH_PROTOCOL = "classpath"`,
`CONSOLE_PROTOCOL = "console"`.

`io.microsphere.net.classpath.Handler` resolves a URL by joining its authority and path into a
resource name, stripping leading slashes, and asking the class loader that loaded the Microsphere
core classes (`ClassLoaderUtils.getClassLoader(Handler.class)`) via `getResource`:

```java
ServiceLoaderURLStreamHandlerFactory.attach();

URL url = new URL("classpath://META-INF/microsphere/configuration-properties.json");
try (InputStream in = url.openStream()) { /* ... */ }

// authority + path are concatenated, so the split point does not matter;
// leading-slash forms are normalized as well
URL other = new URL("classpath:////META-INF/services/io.microsphere.convert.Converter");
```

A miss throws `IOException("No Resource[classpath='...'] was not found!")` — the protocol never
silently returns an empty stream. A hit delegates to the connection of the *found* resource URL,
so reading the same resource from inside a jar works exactly like `jar:` does.

> [!NOTE]
> Resolution uses the loader of the `Handler` class, not the thread-context class loader. In a
> flat application classpath the two usually see the same resources; in container/module setups,
> keep that distinction in mind.

---

## 4. `console:` protocol

```java
public class io.microsphere.net.console.Handler extends ExtendableProtocolURLStreamHandler {
    @Override public URLConnection openConnection(URL url, Proxy proxy) throws IOException
        // → new ConsoleURLConnection(url)
}
public class ConsoleURLConnection extends URLConnection {
    @Override public void connect()                 // no-op
    @Override public InputStream getInputStream()   // System.in
    @Override public OutputStream getOutputStream() // System.out
}
```

Any console URL maps onto the process's standard streams; host, port, and path are all ignored:

```java
ServiceLoaderURLStreamHandlerFactory.attach();

URL out = new URL("console://localhost:12345/abc");
try (OutputStream os = out.openConnection().getOutputStream()) {
    os.write("printed via a URL\n".getBytes());
}
```

> [!WARNING]
> The protocol is `console` — **there are no `out:` or `err:` protocols** in this codebase, and
> `getOutputStream()` always returns `System.out`, never `System.err`. The `console://host:port`
> authority form used in tests is convention, not requirement: the streams are chosen regardless
> of what the URL says.

---

## 5. Sub-protocol chaining

A URL such as `console:text://host/path` is one protocol plus a list of sub-protocols
(`["text"]`). The base class makes this transparent:

```
console:text://host/path        →  parseURL/reformSpec  →  console://host/path;_sp=text
           (sub-protocols become the "_sp" matrix parameter)
console://...;_sp=text  →  openConnection(URL)[final] → openConnection(URL, Proxy)
           → resolveSubProtocols = ["text"] → first SubProtocolURLConnectionFactory whose
             supports() is true and create() returns non-null wins
           → none matched: openFallbackConnection(URL, Proxy), which returns null by default
```

The dispatch lives in the default implementation of `openConnection(URL, Proxy)`, which subclasses
may not override if they want chaining. The `final` methods are `openConnection(URL)`, `parseURL`,
`equals`, `hostsEqual`, `hashCode`, `toExternalForm` — parsing and equality of chained URLs are not
overridable by design. Extension points:

```java
public abstract class ExtendableProtocolURLStreamHandler extends URLStreamHandler {
    public ExtendableProtocolURLStreamHandler()   // protocol = last package segment; appends package to java.protocol.handler.pkgs
    public ExtendableProtocolURLStreamHandler(String protocol)
    public void init()   // fills factories via the hook, sorts by Prioritized.COMPARATOR, registers itself
    protected void initSubProtocolURLConnectionFactories(List<SubProtocolURLConnectionFactory> factories)  // empty by default
    public void customizeSubProtocolURLConnectionFactories(Consumer<List<SubProtocolURLConnectionFactory>> customizer)
    @Override public URLConnection openConnection(URL u, Proxy p) throws IOException  // the chaining dispatch
    protected URLConnection openFallbackConnection(URL url, Proxy proxy) throws IOException  // null by default
    protected List<String> resolveSubProtocols(URL url)   // also: resolveAuthority(URL), resolvePath(URL)
    public final String getProtocol()
}
public interface SubProtocolURLConnectionFactory {
    boolean supports(URL url, List<String> subProtocols);
    URLConnection create(URL url, List<String> subProtocols, Proxy proxy) throws IOException;
}
```

`CompositeSubProtocolURLConnectionFactory` (`add(...)`, `add(varargs)`, `remove(...)`) aggregates
several factories behind one `SubProtocolURLConnectionFactory`, and `DelegatingURLConnection`
wraps a produced connection while forwarding `connect`, timeouts, content, and header calls.

> [!IMPORTANT]
> Two facts correct what older documentation claims: (1) `init()` does **not** scan any SPI for
> `SubProtocolURLConnectionFactory` — it invokes the empty-by-default
> `initSubProtocolURLConnectionFactories(List)` hook, so factories appear only because a subclass
> overrides that hook; (2) `attach()` constructs SPI-registered handlers but never calls `init()`
> on them, so chaining handlers must be initialized explicitly
> (`new MyHandler(); handler.init();` or `customizeSubProtocolURLConnectionFactories(...)`).

> [!NOTE]
> The built-in `classpath` and `console` handlers override `openConnection(URL, Proxy)` directly
> (per the class javadoc, "if there is no requirement to support the sub-protocol, the subclass
> only needs to override `openConnection(URL, Proxy)`"). They therefore never run the chaining
> dispatch: `console:text://...` opens a plain `ConsoleURLConnection` and `text` is only visible
> as the parsed `_sp` matrix parameter, not as a behavior selector.

---

## 6. Registering your own protocol

Two registration paths exist, and they are independent:

* **Handler convention** — the no-arg `ExtendableProtocolURLStreamHandler` constructor appends the
  handler's parent package (e.g. `io.microsphere.net`) to the `java.protocol.handler.pkgs` system
  property, so the JDK can later find `<package>.<protocol>.Handler` by reflection; the protocol
  name is the last package segment. The constructor enforces the conventions: top-level class,
  simple name `Handler`, not inside `sun.net.www.protocol`. This only helps *after* the handler
  class has been constructed once — it cannot bootstrap the very first `classpath:` URL.
* **Explicit attachment** — `ServiceLoaderURLStreamHandlerFactory.attach()` (or
  `URLUtils.registerURLStreamHandler(...)`) puts the handler into a globally installed
  `MutableURLStreamHandlerFactory`. This is the reliable route; call it at startup.

A chaining-capable protocol (plain URLs plus sub-protocols):

```java
package com.example.net.vfs;           // protocol name is derived from the package: "vfs"

import io.microsphere.net.ExtendableProtocolURLStreamHandler;
import io.microsphere.net.SubProtocolURLConnectionFactory;

public class Handler extends ExtendableProtocolURLStreamHandler {

    @Override   // contributes the factory that answers "vfs:gzip://..."
    protected void initSubProtocolURLConnectionFactories(List<SubProtocolURLConnectionFactory> factories) {
        factories.add(new VfsGzipSubProtocolFactory());
    }

    @Override   // reached when no factory matched, including plain "vfs://..." URLs
    protected URLConnection openFallbackConnection(URL url, Proxy proxy) throws IOException {
        String path = resolvePath(url);
        if (path == null || path.isEmpty()) {
            return null;                // null keeps the fail-fast behavior
        }
        return new VfsURLConnection(url, path);
    }
}
```

```
# src/main/resources/META-INF/services/io.microsphere.net.ExtendableProtocolURLStreamHandler
com.example.net.vfs.Handler
```

Then, at startup: `ServiceLoaderURLStreamHandlerFactory.attach();` followed by `init()` on the
chaining handler (see the IMPORTANT callout in §5). If you do not need sub-protocols, override
`openConnection(URL, Proxy)` instead — the `console` handler is the minimal reference
implementation.

---

## See also

* [Core Utilities](core-utilities.md) — `ProtocolConstants`, `SeparatorConstants.ARCHIVE_ENTRY_SEPARATOR` (`"!/"`), `ClassPathUtils`
* [I/O and File Watching](io-and-file-watch.md) — reading from the URLs these handlers produce
* [Class Loading and Artifacts](classloading-and-artifacts.md) — archive URLs as `Artifact` sources
* [Reference](reference.md) — all SPI files and system properties in one place

[← Handbook index](../README.md) · [Previous: I/O and File Watching](io-and-file-watch.md) · [Next: Logging →](logging.md)
