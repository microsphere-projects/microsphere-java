# Networking and URL protocols

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-java-core` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Packages** | `io.microsphere.net` — 10 main-source types · `io.microsphere.net.classpath` — 1 · `io.microsphere.net.console` — 2 (**13** total) |
| **JDK level** | there is **no** `maven.compiler.*` / `<release>` property anywhere in the reactor — the bytecode level is inherited from the external parent `microsphere-build:0.3.16`; CI-tested on Temurin JDK 8 / 11 / 17 / 21 / 25 |
| **Depends on other `core` packages** | `annotation` (`@Nonnull`/`@Nullable`/`@Immutable`), `collection` (`ListUtils`, `SetUtils`, `MapUtils`, `Lists`, `CollectionUtils.size`), `constants` (`SymbolConstants`, `PathConstants`, `ProtocolConstants`, `SeparatorConstants`), `lang.Prioritized`, `logging`, `reflect` (`FieldUtils`, `ConstructorUtils`), `text.FormatUtils`, `util` (`StringUtils`, `Assert`, `ArrayUtils`, `CharSequenceUtils`, `ClassUtils`, `ClassLoaderUtils`, `ClassPathUtils`, `ServiceLoaderUtils`, `SystemUtils`, `Utils`), `util.jar.JarUtils`, `io.IOUtils` |
| **External dependencies** | none — pure JDK (`java.net`, `java.io`, `java.util.ServiceLoader`) |
| **Used by** | `classloading` (`classloading/URLClassPathHandle.java:26` imports `URLUtils.EMPTY_URL_ARRAY`), `io.scanner` (`io/scanner/SimpleClassScanner.java:23-24`), `util.ClassLoaderUtils` (`:46`), `util.ClassUtils` (`:58`), `util.jar.JarUtils` (`:37-39`) |

All `file:line` references in this page are relative to `microsphere-java-core/src/main/java/io/microsphere/`
(main sources) or `microsphere-java-core/src/test/java/io/microsphere/` (tests), read from the `release` worktree.
After the first mention they are shortened to `net/Foo.java:40-55`.

---

## 1. What this group is for

**Reach for `io.microsphere.net` when you need a URL scheme the JDK does not ship, and you need it to survive the fact
that `java.net.URL.setURLStreamHandlerFactory(...)` may be called only once per JVM.** The package supplies five
`URLStreamHandlerFactory` implementations that compose instead of colliding
(`net/CompositeURLStreamHandlerFactory.java:56-135`, `net/MutableURLStreamHandlerFactory.java:64-139`,
`net/DelegatingURLStreamHandlerFactory.java:56-84`, `net/StandardURLStreamHandlerFactory.java:56-98`,
`net/ServiceLoaderURLStreamHandlerFactory.java:61-92`) plus the one entry point you should actually call:
`URLUtils.attachURLStreamHandlerFactory(factory)` (`net/URLUtils.java:1365-1389`) and its SPI-flavoured shortcut
`ServiceLoaderURLStreamHandlerFactory.attach()` (`net/ServiceLoaderURLStreamHandlerFactory.java:71-74`).

**Reach for `ExtendableProtocolURLStreamHandler` (`net/ExtendableProtocolURLStreamHandler.java:132-420`) when one scheme
must fan out to many dialects.** It adds *sub-protocols* to a URL — `jdbc:mysql://host/db`, `console:text://host:12345/abc` —
by rewriting them into a matrix parameter (`;_sp=mysql`) at parse time
(`:328-337`, `:370-392`) and then dispatching to the first `SubProtocolURLConnectionFactory` that both
`supports(...)` and produces a non-null connection for (`:271-286`). Two handlers ship in the box:
`classpath:` (`net/classpath/Handler.java:40-64`) and `console:` (`net/console/Handler.java:32-38` +
`net/console/ConsoleURLConnection.java:35-60`).

**Reach for `URLUtils` (`net/URLUtils.java:90-1724`) for URL/URI string work that `java.net.URL` refuses to do**: matrix
parameters (`:362-381`), sub-protocol extraction (`:1046-1050`, `:1131-1144`), archive-entry path and archive file
resolution for `jar:`/`file:` URLs (`:211-220`, `:274-301`), a JDK-identical `toExternalForm` that preserves matrix
segments (`:932-1016`), and the reflective read/write of the JDK's private global factory field (`:1423-1426`,
`:1641-1643`).

> [!IMPORTANT]
> Nothing in this group registers itself when the jar is merely on the classpath. There is **no**
> `META-INF/services/java.net.URLStreamHandlerFactory` in `microsphere-java-core`'s main resources — one exists only in
> the module's **test** resources (`src/test/resources/META-INF/services/java.net.URLStreamHandlerFactory`, pointing at
> `io.microsphere.net.StandardURLStreamHandlerFactory`). The main resources do declare
> `META-INF/services/io.microsphere.net.ExtendableProtocolURLStreamHandler` with the `classpath` and `console` handlers.
> **You must call `ServiceLoaderURLStreamHandlerFactory.attach()` once at startup** — see §7.

---

## 2. Coordinates and dependency

No extra artifact and no third-party library is required: all 13 types compile against `java.net` and `java.io` only.

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

---

## 3. Version information and compatibility

| Fact | Value | Evidence (`release`) |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | root `pom.xml:12` |
| `artifactId` | `microsphere-java-core` | module `pom.xml` |
| Version property | `${revision}`, flattened | root `pom.xml` |
| Value of `${revision}` on `release` | `0.3.20-SNAPSHOT` | root `pom.xml:54` |
| Newest published tag | `0.3.20` | release-line table in the group index (`docs/current/user-guide/microsphere-java-core/README.md:66-72`) |
| Imported BOM | `microsphere-all-bom:0.3.13` | `microsphere-java-parent/pom.xml:22`, `:33-36` |
| Spring BOM import | `spring-framework-bom` | `microsphere-java-parent/pom.xml:40-46` |
| Spring Framework, JDK 17+ | `7.0.9` | `microsphere-java-parent/pom.xml:23` |
| Spring Framework, JDK `[1.8,17)` | `5.3.39` via profile `java8-16` | `microsphere-java-parent/pom.xml:54-62` (activation `<jdk>[1.8,17)</jdk>` at `:57`) |
| CI matrix | Temurin JDK `8, 11, 17, 21, 25` | `.github/workflows/maven-build.yml:25`, `distribution: 'temurin'` at `:33` |
| Bytecode level | inherited from the external parent `microsphere-build:0.3.16`; the reactor declares no `maven.compiler.*` / `<release>` property | reactor POMs |
| Compatibility range of this group | JDK 8 through JDK 25 inclusive — but **the mechanism differs per JDK**, see §7.4 | `net/StandardURLStreamHandlerFactory.java:66-85` |

> [!NOTE]
> Every type in this group is javadoc-tagged `@since 1.0.0` (`net/URLUtils.java:88`,
> `net/ExtendableProtocolURLStreamHandler.java:130`, `net/classpath/Handler.java:38`,
> `net/console/ConsoleURLConnection.java:33`). That tag is a **source-level** marker and does not refer to the Maven
> `0.3.x` release line documented here; do not read it as "removed in 1.x" — no `1.x` line exists.

---

## 4. `classpath:` URLs

### 4.1 API

`io.microsphere.net.classpath.Handler` (`net/classpath/Handler.java:40`) is a top-level class named exactly `Handler`,
declared in package `io.microsphere.net.classpath`, and it overrides only
`openConnection(URL, Proxy)` (`:43-63`). Because it uses the inherited no-arg constructor, its protocol name is derived
from the **last segment of its own package** — `classpath` — by
`ExtendableProtocolURLStreamHandler#resolveConventionProtocol` (`net/ExtendableProtocolURLStreamHandler.java:174-177`)
and `getProtocol()` (`:412-414`).

Resolution rules, in order (`net/classpath/Handler.java:44-62`):

| Step | Code | Effect |
|---|---|---|
| 1 | `u.getAuthority()` + `u.getPath()` (`:44-45`) | both parts are read |
| 2 | `if (isNotEmpty(authority)) classPath = authority + path;` (`:49-51`) | authority and path are **concatenated**, so the slash split point is irrelevant |
| 3 | `while (classPath.indexOf(SLASH_CHAR) == 0) substring(1)` (`:53-55`) | every leading `/` is stripped — `classpath:////x/y` → `x/y` |
| 4 | `getClassLoader(Handler.class).getResource(classPath)` (`:57-58`) | the loader that loaded **microsphere core**, not the thread context loader |
| 5 | `throw new IOException("No Resource[classpath='" + classPath + "'] was not found!")` (`:59-61`) | missing resource is a hard failure, *not* silent degradation |
| 6 | `return url.openConnection()` (`:62`) | the returned connection is the real `file:`/`jar:` connection — `DelegatingURLConnection` is not involved |

### 4.2 Example

Derived from `net/classpath/HandlerTest.java:42-46` and `net/ServiceLoaderURLStreamHandlerFactoryTest.java:71-76`:

```java
ServiceLoaderURLStreamHandlerFactory.attach();                 // ServiceLoaderURLStreamHandlerFactoryTest.java:47, :73

String url = "classpath://META-INF/test.properties";           // HandlerTest.java:42
assertEquals("name = 测试名称", IOUtils.toString(new URL(url).openStream(), "UTF-8"));   // :75

// the authority/path split does not matter — this four-slash form resolves the same resource  (HandlerTest.java:44)
URL other = new URL("classpath:////META-INF/services/io.microsphere.event.EventListener");
assertNotNull(other.openConnection());                          // exercises Handler.java:49-55
```

_Source: net/classpath/HandlerTest.java:42-46, net/ServiceLoaderURLStreamHandlerFactoryTest.java:71-76_

A missing resource throws on open — `HandlerTest.java:58-62` asserts exactly that:

```java
URL url = new URL("classpath://META-INF/not-found.res");        // :46
assertThrows(IOException.class, url::openStream);               // :61
```

_Source: net/classpath/HandlerTest.java:46, :58-62_

---

## 5. `console:` URLs

### 5.1 API

`io.microsphere.net.console.Handler` (`net/console/Handler.java:32`) is the minimal possible subclass: it overrides
`openConnection(URL, Proxy)` and returns a fresh `ConsoleURLConnection` (`:35-37`). No sub-protocol factory is
registered, so the `console:` handler answers *any* sub-protocol directly.

`ConsoleURLConnection` (`net/console/ConsoleURLConnection.java:35`) extends `java.net.URLConnection` and implements the
three-method contract:

| Method | Body | Evidence |
|---|---|---|
| `ConsoleURLConnection(URL url)` | `super(url)` only — no socket, no stream opened | `net/console/ConsoleURLConnection.java:43-45` |
| `connect()` | empty — the connection is always "already open" | `:47-49` |
| `getInputStream()` | `System.in` (`java.lang.System.in`, static import `:26`) | `:51-54` |
| `getOutputStream()` | `System.out` (static import `:27`) | `:56-59` |

This is the library's canonical demonstration that a `URLConnection` need not touch the network at all: it lets
`Scanner`-style code, `URLUtils.close(...)` and stream plumbing treat standard I/O as a resource URL.

### 5.2 Example

From `net/console/ConsoleURLConnectionTest.java:23-43`:

```java
Handler handler = new Handler();                                                   // :25
ConsoleURLConnection connection = (ConsoleURLConnection) handler.openConnection(        // :26
        new URL("console://localhost:12345/abc"), NO_PROXY);

connection.connect();                                 // no-op, must not throw   — :31
assertSame(System.in, connection.getInputStream());   // :32, :37
assertSame(System.out, connection.getOutputStream()); // :42
```

_Source: net/console/ConsoleURLConnectionTest.java:23-43_

The same URL also round-trips through the JDK lookup when the handler package has been published to
`java.protocol.handler.pkgs` (`net/console/HandlerTest.java:38-44`, `:62-69`):

```java
public static final String TEST_CONSOLE_URL                = "console://localhost:12345/abc";           // :38
public static final String TEST_CONSOLE_URL_WITH_SP        = "console:text://localhost:12345/abc";      // :40
public static final String TEST_CONSOLE_URL_WITH_SP_PARAMS = TEST_CONSOLE_URL_WITH_SP + ";ref=top?n=1"; // :42
// :63 — url.openStream() and handler.openConnection(url, NO_PROXY).getInputStream() are the SAME object
```

_Source: net/console/HandlerTest.java:38-44, :61-69_

---

## 6. Custom and extendable protocol handlers

### 6.1 The two constructors, and the conventions they enforce

| Constructor | Behaviour | Evidence |
|---|---|---|
| `ExtendableProtocolURLStreamHandler()` | asserts three conventions, then derives the protocol from the package name **and** appends the parent package to the `java.protocol.handler.pkgs` system property | `net/ExtendableProtocolURLStreamHandler.java:146-151`, `:179-210`, `:212-235` |
| `ExtendableProtocolURLStreamHandler(String protocol)` | trusts you completely: no assertion, **no property registration** | `:153-155` |

The conventions checked by the no-arg path (`:185-210`, all `static` and package-visible so the tests can drive them):

1. the class must be top-level — local, anonymous and member classes are rejected (`:185-189`);
2. the simple class name must be exactly `Handler` (`:191-196`, against `URLUtils.HANDLER_CONVENTION_CLASS_NAME`, `net/URLUtils.java:149`);
3. the class must not sit in the default/builtin package `sun.net.www.protocol` (`:198-210`, prefix from `net/URLUtils.java:139`), nor in the unnamed top package (`:203-205`).

Violations raise `IllegalArgumentException` — the group's one deliberate non-degrading path besides the `classpath:`
not-found case.

### 6.2 Sub-protocols: rewriting and dispatch

`parseURL` is `final` (`:328-337`). When the spec still contains `://` after the protocol, the sub-protocol text between
them is lifted into a matrix parameter named `_sp` (`URLUtils.SUB_PROTOCOL_MATRIX_NAME`, `net/URLUtils.java:154`) and the
remainder is handed to `super.parseURL`. `jdbc:mysql://localhost:3307/mydb?charset=UTF-8#top` therefore becomes
`jdbc://localhost:3307/mydb;_sp=mysql?charset=UTF-8#top` (`:340-368` javadoc, implementation `:370-392`).

`openConnection(URL, Proxy)` (`:271-286`) walks the sorted factory list, and takes the **first** factory that both
`supports(u, subProtocols)` and returns a non-`null` connection; anything else falls through to
`openFallbackConnection(u, p)`, which **returns `null` by default** (`:297-299`).

| Member | Role | Evidence |
|---|---|---|
| `init()` | sorts the factories by `Prioritized.COMPARATOR` and registers the handler itself | `:237-247` |
| `initSubProtocolURLConnectionFactories(List)` | protected extension hook, empty by default | `:254-255` |
| `customizeSubProtocolURLConnectionFactories(Consumer)` | post-construction mutation of the live list | `:262-264` |
| `resolveSubProtocols(URL)` / `resolveAuthority(URL)` / `resolvePath(URL)` | `protected` conveniences over `URLUtils` | `:400-410` |
| `equals`, `hashCode`, `hostsEqual`, `toExternalForm`, `openConnection(URL)`, `parseURL` | `final` — overridden once, for all subclasses | `:266-269`, `:301-326`, `:328-337` |

`SubProtocolURLConnectionFactory` itself is a two-method contract (`net/SubProtocolURLConnectionFactory.java:68-92`):
`supports(URL, List<String>)` and `create(URL, List<String>, Proxy) throws IOException`.
`CompositeSubProtocolURLConnectionFactory` (`net/CompositeSubProtocolURLConnectionFactory.java:76-152`) aggregates them,
re-sorting after every `add`/`remove` (`:90-122`) and selecting the first supporter (`:135-151`).

### 6.3 Example

The test fixture handler is the smallest legal subclass (`net/test/Handler.java:27-37`) and the test proves the whole
loop (`net/ExtendableProtocolURLStreamHandlerTest.java:63-72`, `:157-172`, `:186-198`, `:232-235`):

```java
// net/test/Handler.java:27 — top-level, named Handler, package io.microsphere.net.test ⇒ protocol "test"
public class Handler extends ExtendableProtocolURLStreamHandler {
    public Handler() { super(); }                       // :29-31
    public Handler(String protocol) { super(protocol); } // :33-35
}
// ExtendableProtocolURLStreamHandlerTest.java
private static final String TEST_URL          = "test://localhost:12345/abc";        // :63
private static final String TEST_URL_WITH_SP  = "test:text://localhost:12345/abc";   // :65
// 1. no factory registered ⇒ URL.openConnection() is null (fallback default), and the URL string is untouched
URL url = new URL(TEST_URL);
assertNull(url.openConnection());                     // :159
assertEquals(TEST_URL, url.toString());               // :160
// 2. a supporting factory that returns null still falls through
handler.customizeSubProtocolURLConnectionFactories(factories -> {
    factories.add(0, new FalseSubProtocolURLConnectionFactory());   // supports() == false   :168
    factories.add(1, new NullSubProtocolURLConnectionFactory());    // create() == null      :169
});
assertNull(handler.openConnection(url));              // :171
// 3. a real factory makes the sub-protocol URL work
handler.customizeSubProtocolURLConnectionFactories(f -> f.add(new ConsoleSubProtocolURLConnectionFactory())); // :187-189
assertSame(url.openConnection(NO_PROXY).getInputStream(), handler.openConnection(new URL(TEST_CONSOLE_URL_WITH_SP), NO_PROXY).getInputStream()); // :196
// 4. sub-protocols are readable
assertEquals(ofList("text"), handler.resolveSubProtocols(new URL(TEST_URL_WITH_SP))); // :234
```

_Source: net/test/Handler.java:27-37, net/ExtendableProtocolURLStreamHandlerTest.java:63-72, :157-172, :186-198, :232-235_

Composing factories directly, from `net/CompositeSubProtocolURLConnectionFactoryTest.java:46-95`:

```java
CompositeSubProtocolURLConnectionFactory composite = new CompositeSubProtocolURLConnectionFactory();  // :48
ConsoleSubProtocolURLConnectionFactory instance = new ConsoleSubProtocolURLConnectionFactory();        // :54
composite.add(instance);                                                                               // :55
URL url = new URL("ftp://...");                                                                        // :57
List<String> subProtocols = emptyList();
if (composite.supports(url, subProtocols)) {                                                          // :59
    URLConnection c = composite.create(url, subProtocols, NO_PROXY);                                  // :60
    assertEquals(ConsoleURLConnection.class, c.getClass());                                           // :61
}
composite.remove(instance);
assertFalse(composite.supports(url, subProtocols));                                                   // :65
composite.add(instance, instance);                                                                    // varargs add de-duplicates (:96-109)
composite.remove(instance);
assertFalse(composite.supports(url, subProtocols));                                                   // :69
assertFalse(composite.remove(null));                                                                  // :75
```

_Source: net/CompositeSubProtocolURLConnectionFactoryTest.java:46-95_

---

## 7. Registering handlers: the five factories and `attach()`

### 7.1 Which factory for which job

| Class | Kind | Key members | Null / failure policy |
|---|---|---|---|
| `ServiceLoaderURLStreamHandlerFactory` (`net/ServiceLoaderURLStreamHandlerFactory.java:61`) | the **entry point**; extends `DelegatingURLStreamHandlerFactory` | `public static void attach()` (`:71-74`), `static Map<String, ExtendableProtocolURLStreamHandler> loadHandlers()` (`:84-91`) | SPI absence ⇒ empty list, no throw |
| `CompositeURLStreamHandlerFactory` (`net/CompositeURLStreamHandlerFactory.java:56`) | chain of factories, prioritised | `final createURLStreamHandler(String)` (`:74-86`), `addURLStreamHandlerFactory` (`:94-107`), `protected getFactories()`/`getComparator()` (`:114-125`) | `null` factory and `this` are ignored (`:95`); nested composites are **flattened** (`:97-100`); duplicates dropped by `contains` (`:101`) |
| `MutableURLStreamHandlerFactory<H>` (`net/MutableURLStreamHandlerFactory.java:64`) | protocol → handler registry | `addURLStreamHandler` (`:91-94`), `removeURLStreamHandler` (`:102-104`), `getURLStreamHandler` (`:112-114`), `getHandlers()` (`:121-123`), `clearHandlers()` (`:135-138`) | unknown protocol ⇒ `null`; documented **not thread-safe** (`:53-56`) |
| `DelegatingURLStreamHandlerFactory` (`net/DelegatingURLStreamHandlerFactory.java:56`) | single delegate wrapper | ctor guards with `assertNotNull` (`:66-69`), `protected final getDelegate()` (`:81-83`) | `null` delegate ⇒ `IllegalArgumentException` (`:67`) |
| `StandardURLStreamHandlerFactory` (`net/StandardURLStreamHandlerFactory.java:56`) | re-exposes the JDK's own protocols as a factory | `createURLStreamHandler(String)` (`:70-73`), package-visible test seam `createURLStreamHandler(Field, String)` (`:75-85`) | unknown protocol ⇒ `null` — except see pitfall 7 |

### 7.2 `attachURLStreamHandlerFactory` — the one call to make

`URLUtils.attachURLStreamHandlerFactory(factory)` (`net/URLUtils.java:1365-1389`) exists because the JDK slot is
write-once. Its three branches:

| Case | Action | Lines |
|---|---|---|
| `factory == null` | return immediately (no-op) | `:1366-1368` |
| no global factory yet | `URL.setURLStreamHandlerFactory(factory)` and return | `:1373-1375` |
| global factory is already a `CompositeURLStreamHandlerFactory` | append to it | `:1376-1377`, `:1388` |
| a foreign factory is installed | wrap old + new in a new composite, **reflectively null `URL.factory`**, install the composite, then append | `:1378-1385` |

```java
URLStreamHandlerFactory factory = new StandardURLStreamHandlerFactory();
attachURLStreamHandlerFactory(factory);
assertSame(factory, getURLStreamHandlerFactory());                    // URLUtilsTest.java:557 — installed directly
attachURLStreamHandlerFactory(factory);                               // second call must NOT hit the JDK's write-once slot
CompositeURLStreamHandlerFactory composite = (CompositeURLStreamHandlerFactory) getURLStreamHandlerFactory();
assertNotSame(factory, composite);                                    // :561
assertEquals(1, composite.getFactories().size());                     // :562 — the duplicate was rejected by contains()
assertSame(factory, composite.getFactories().get(0));                 // :563
assertEquals(CompositeURLStreamHandlerFactory.class, composite.getClass()); // :564
attachURLStreamHandlerFactory(null);                                  // :569 — silent no-op
```

_Source: net/URLUtilsTest.java:553-571_

For tests, `clearURLStreamHandlerFactory()` (`net/URLUtils.java:1641-1643`, `protected`) resets the slot via
`FieldUtils.setStaticFieldValue(true, URL.class, "factory", null)`; `URLUtilsTest.java:149-152` runs it in `@AfterEach`.

### 7.3 Registering one handler without a factory

```java
Handler handler = new io.microsphere.net.console.Handler();     // URLUtilsTest.java:580
registerURLStreamHandler(handler);                             // :581 → URLUtils.java:1461-1463
URL url = ofURL("console://localhost:12345/abc");              // :582, console/HandlerTest.java:38
assertSame(System.in, url.openStream());                       // :583 — the global factory now answers "console"
```

_Source: net/URLUtilsTest.java:578-584_

Mechanically, `registerURLStreamHandler(String, URLStreamHandler)` (`:1502-1506`) gets-or-creates a
`MutableURLStreamHandlerFactory` (`getMutableURLStreamHandlerFactory(true)`, `:1614-1624`), puts the handler under its
protocol, and attaches that factory. `getMutableURLStreamHandlerFactory()` without the flag returns `null` when nothing
is attached (`:1610-1612`, asserted by `URLUtilsTest.java:607-611`), and the lookup searches the current factory and then
the composite's children (`:1626-1639`, asserted at `:613-618` and `:626-633`).

### 7.4 Which technique runs on which JDK

| JDK | Technique actually used by this group | Evidence |
|---|---|---|
| 8 | `StandardURLStreamHandlerFactory` cannot find `URL.defaultFactory` (`findField` ⇒ `null`), so it builds the class name `sun.net.www.protocol.<protocol>.Handler`, resolves it and instantiates it reflectively | `net/StandardURLStreamHandlerFactory.java:68`, `:75-85`, `:87-93`; `net/URLUtils.java:139` |
| 9+ | the private static `URL.defaultFactory` field **exists**, so the JDK's own factory object is read and delegated to | `net/StandardURLStreamHandlerFactory.java:66-68` ("`URL#defaultFactory` static field since JDK 9+"), `:94-96` |
| all JDKs | `java.protocol.handler.pkgs` — the JDK's search property — is **appended** with the handler's parent package whenever an `ExtendableProtocolURLStreamHandler` is built by the no-arg constructor; the constant and its documented semantics live at `net/URLUtils.java:120-134` | `net/ExtendableProtocolURLStreamHandler.java:212-235`, `:170-172` |
| all JDKs | `URL.setURLStreamHandlerFactory` once per JVM — worked around by reading/writing `URL.factory` reflectively and composing | `net/URLUtils.java:1365-1389`, `:1423-1426`, `:1641-1643` |
| service loading | `URLStreamHandlerProvider` (Java 9+ `java.net.spi`) is **not used** by this release: registration is done through the factory + system-property routes above | absence of any `URLStreamHandlerProvider` reference in the `net` sources |

> [!WARNING]
> The reflective routes read and write private static fields of `java.net.URL`. On JDK 16+ strong encapsulation makes
> `setAccessible(true)` on `java.base/java.net` fail unless the JVM is started with
> `--add-opens java.base/java.net=ALL-UNNAMED`. The library does not add that flag for you; this group's own build
> supplies the equivalent openings for the test JVM. Budget for it when you attach a factory *after* another library
> already installed one (§7.2, fourth row).

---

## 8. URL and URI utilities (`URLUtils`)

`URLUtils` is `public abstract class ... implements Utils` with a `private` constructor
(`net/URLUtils.java:90`, `:1722-1723`) — a pure static facade.

### 8.1 Constants worth knowing

| Constant | Value / source | Line |
|---|---|---|
| `DEFAULT_ENCODING` | `SystemUtils.FILE_ENCODING`, i.e. the `file.encoding` system property — **not** a hard-coded `UTF-8` | `:97` (`util/SystemUtils.java:332`) |
| `EMPTY_URL_ARRAY` | aliases `ArrayUtils.EMPTY_URL_ARRAY` | `:103` |
| `FILE_URL_PREFIX` | `"file:/"` | `:113` |
| `HANDLER_PACKAGES_PROPERTY_NAME` | `"java.protocol.handler.pkgs"` | `:134` |
| `DEFAULT_HANDLER_PACKAGE_PREFIX` | `"sun.net.www.protocol"` | `:139` |
| `HANDLER_PACKAGES_SEPARATOR_CHAR` | `'|'` | `:144` |
| `HANDLER_CONVENTION_CLASS_NAME` | `"Handler"` | `:149` |
| `SUB_PROTOCOL_MATRIX_NAME` | `"_sp"` | `:154` |

### 8.2 API reference (selected)

| Method | Contract | Evidence |
|---|---|---|
| `ofURL(String)` | `@Nonnull`; wraps **any** exception in `IllegalArgumentException` | `:179-186` |
| `resolveArchiveEntryPath(URL)` | `@Nullable` — `null` when no `!/` present; decodes the entry path | `:211-220` |
| `resolveBasePath(URL)` / `resolvePath(URL)` | `@Nonnull`; base drops the archive entry, full path keeps it | `:244-248`, `:1315-1318` |
| `resolveArchiveFile(URL)` | `@Nullable`; `file:` → archive-directory search, otherwise existence check | `:274-301` |
| `resolveQueryParameters(String)` / `resolveMatrixParameters(String)` | `@Nonnull @Immutable` `Map<String, List<String>>`, unmodifiable | `:322-327`, `:362-381` |
| `normalizePath(String)` | `@Nullable`; blank in ⇒ returned unchanged; `\`→`/`, `//`→`/` | `:402-420` |
| `encode`/`decode(String[, encoding])` | `@Nonnull`; **all** failures surface as `IllegalArgumentException` | `:456-459`, `:497-506`, `:542-545`, `:581-590` |
| `isDirectoryURL(URL)` | `null` ⇒ `false` | `:622-635` |
| `isJarURL(URL)` / `isArchiveURL(URL)` | `null` ⇒ `NullPointerException` | `:667-682`, `:719-725` |
| `isArchiveProtocol(String)` | `jar`/`zip`/`war`/`ear`, case-sensitive | `:754-757` |
| `buildURI(String...)` | `@Nonnull`; empty/`null` array ⇒ `"/"` | `:788-809` |
| `buildMatrixString(Map)` | `@Nullable` | `:841-856` |
| `buildMatrixString(String, String...)` | annotated `@Nonnull` but can return `null` — see pitfall 5 | `:886-889`, `:1693-1698` |
| `toExternalForm(URL)` | `@Nonnull`; rebuilds protocol, `//authority`, path, matrix, query, fragment | `:932-1016` |
| `getSubProtocol(String)` / `resolveSubProtocols(URL\|String)` | first `_sp` value / full unmodifiable list | `:1046-1050`, `:1085-1089`, `:1131-1144` |
| `resolveProtocol(String)` | `@Nullable`; whitespace before the colon ⇒ `null` + trace log | `:1183-1201` |
| `resolveAuthority(URL\|String)` | matrix-truncated authority | `:1247-1249`, `:1282-1285` |
| `close(URLConnection)` | `HttpURLConnection.disconnect()` only; everything else (incl. `null`) is ignored | `:1528-1532` |

### 8.3 Example

```java
// parsing — URLUtilsTest.java
assertEquals("%2Fabc%2Fdef", encode("/abc/def"));                    // :291
assertEquals("/abc/def", decode("%2Fabc%2Fdef"));                     // :311
assertThrows(IllegalArgumentException.class, () -> encode("/abc/def", "unknown-encoding")); // :306
// matrix vs query parameters — net/URLUtilsTest.java:207-257
String queryURL = "http://localhost/?q=java&oq=java&sourceid=chrome&es_sm=122&ie=UTF-8";  // :121, :125
Map<String, List<String>> q = resolveQueryParameters(queryURL);
assertEquals(ofList("java"), q.get("q"));                                                  // :212
assertEquals(5, q.size());                                                                 // :211-218
Map<String, List<String>> m = resolveMatrixParameters("http://localhost/;q=java;oq=java");  // :127, :233-242
assertEquals(ofList("java"), m.get("oq"));                                                  // :237
// paths — :259-287, :419-435, :658-685
assertEquals("/abc/", normalizePath("//abc///"));                     // :278-281
assertEquals("/a/b/c", buildURI("a", "b", "c"));                      // :421-422
assertEquals("/", buildURI());                                        // :430-431
assertEquals("D:/test", resolvePathFromFile(ofURL("file:///D:/test"), true));  // :671-672
assertEquals("/dir/x.jar", resolvePathFromJar(ofURL("file:/dir/x.jar"), false)); // :664-666
// sub-protocols — :499-526
assertEquals("text", getSubProtocol("http://localhost/;_sp=text;_sp=properties")); // :501
assertEquals(ofList("text", "properties"), resolveSubProtocols(ofURL("http://localhost/;_sp=text;_sp=properties"))); // :511-515
new io.microsphere.net.console.Handler();                             // :520 — publishes io.microsphere.net
assertEquals(2, resolveSubProtocols(ofURL("console:text:properties://localhost")).size()); // :521-525
// round-trip against the JDK — :456-497
assertEquals(url.toExternalForm(), toExternalForm(url));              // :496
```

_Source: net/URLUtilsTest.java:109-131, :207-291, :306-322, :419-454, :456-526, :528-539, :658-691_

---

## 9. Wrapping an existing connection: `DelegatingURLConnection`

`net/DelegatingURLConnection.java:43-261` extends `URLConnection` and forwards **42** methods (`:52-260`) to a delegate — timeouts,
content metadata, all five header accessors, content, permission, both streams, the `doInput`/`doOutput`/`useCaches`/
`allowUserInteraction`/`ifModifiedSince`/`defaultUseCaches` flag pairs and the four request-property methods. `toString()`
forwards too (`:177-180`). The class is the base you extend to intercept one behaviour of a `jar:`/`file:`/`http:`
connection without re-implementing it; unlike `collection`'s `Delegating*` family it implements no `DelegatingWrapper`
marker and exposes no `getDelegate()` — the delegate is `private final` (`:45`).

```java
URL baseURL = getClass().getProtectionDomain().getCodeSource().getLocation();     // DelegatingURLConnectionTest.java:65
URL url = new URL(baseURL.toString() + "META-INF/test.properties");               // :66
URLConnection delegate = url.openConnection();                                    // :67
DelegatingURLConnection connection = new DelegatingURLConnection(delegate);       // :68
connection.connect();                                        // no-op for file:, must not throw   — :77
assertEquals(19, connection.getContentLength());              // :112
assertEquals("content/unknown", connection.getContentType()); // :122
assertEquals("19", connection.getHeaderField("content-length")); // :148
assertSame(System.out, connection.getOutputStream());         // after attach() on a console: URL   — :216-220
assertDoesNotThrow(() -> connection.setRequestProperty("key-1", "value-1")); // :305
```

_Source: net/DelegatingURLConnectionTest.java:63-69, :76-79, :110-150, :200-221, :301-312_

---

## 10. Common pitfalls

1. **`attach()` is not automatic.** Loading `microsphere-java-core` registers nothing; `new URL("classpath://…")` fails with
   `MalformedURLException` ("unknown protocol") until `ServiceLoaderURLStreamHandlerFactory.attach()`
   (`net/ServiceLoaderURLStreamHandlerFactory.java:71-74`) or `registerURLStreamHandler(...)` (`net/URLUtils.java:1461-1506`) has run.
2. **`URL.openConnection()` returning `null` is legal here and illegal in the JDK.** `openFallbackConnection` returns `null` by
   default (`net/ExtendableProtocolURLStreamHandler.java:297-299`), and the tests assert it
   (`net/ExtendableProtocolURLStreamHandlerTest.java:159`, `net/test/HandlerTest.java:57-59`); therefore
   `new URL("test://x").openStream()` throws `NullPointerException`, not `IOException`. Override `openFallbackConnection`.
3. **Subclass `openConnection(URL, Proxy)`, never `openConnection(URL)`.** The single-argument form is `final`
   (`net/ExtendableProtocolURLStreamHandler.java:266-269`) and delegates to the two-argument one with `Proxy.NO_PROXY`.
   The same applies to `equals`, `hashCode`, `hostsEqual`, `toExternalForm` and `parseURL` (`:301-337`).
4. **The `java.protocol.handler.pkgs` trick only happens on the no-arg constructor.** Using
   `ExtendableProtocolURLStreamHandler(String protocol)` (`net/ExtendableProtocolURLStreamHandler.java:153-155`) skips
   both the convention assertions and `appendHandlerPackage` (`:212-235`), so the JDK's own lookup will never find your
   class — you must attach a factory.
5. **`buildMatrixString(String, String...)` is annotated `@Nonnull` but returns `null`.** The overload
   (`net/URLUtils.java:886-889`) delegates to `buildString`, which returns `null` for an empty or `null` value array
   (`:1693-1698`); its own javadoc even prints `null` (`:874-879`). Use the `Map` overload, which is honestly
   `@Nullable` (`:841-842`), or guard the result.
6. **Null policy is mixed inside one class.** `isDirectoryURL(null)` ⇒ `false` (`net/URLUtils.java:622-625`),
   `close(null)` ⇒ no-op (`:1528-1532`), `attachURLStreamHandlerFactory(null)` ⇒ no-op (`:1366-1368`) — but
   `isJarURL(null)` and `isArchiveURL(null)` throw `NullPointerException` (`:668`, `:720`; asserted at
   `net/URLUtilsTest.java:379-382`), and `ofURL` of an unparseable string throws `IllegalArgumentException` (`:180-186`;
   asserted at `:159-162`).
7. **`StandardURLStreamHandlerFactory` dereferences the JDK field without a null guard.** It checks the `Field`
   (`net/StandardURLStreamHandlerFactory.java:88-93`) but then calls `factory.createURLStreamHandler(protocol)` on the value it
   read (`:94-95`). Where the read degrades to `null` the call raises `NullPointerException` instead of falling through to the
   JDK 8 class-name path (`:77-83`). Catch it, or install a composite that also holds a `MutableURLStreamHandlerFactory`.
8. **Ordering of factories is insertion order unless they implement `Prioritized`.** `CompositeURLStreamHandlerFactory.getComparator()`
   returns `Prioritized.COMPARATOR` (`net/CompositeURLStreamHandlerFactory.java:123-125`), and that comparator yields `0` for two
   non-`Prioritized` objects (`lang/Prioritized.java:61-76`), which `Collections.sort` treats as "keep the order". None of the
   shipped factories implements `Prioritized`, so the SPI order decides. Remember `MAX_PRIORITY` is `Integer.MIN_VALUE`
   (`lang/Prioritized.java:81`) — smaller means higher.
9. **`MutableURLStreamHandlerFactory` is not thread-safe and its `getHandlers()` view is live.**
   Documented at `net/MutableURLStreamHandlerFactory.java:53-56`; `getHandlers()` returns `handlers.values()`
   (`:121-123`), so mutating it mutates the registry. `addURLStreamHandler` never validates the protocol (`:91-94`), so a
   `null` key is accepted.
10. **`classpath:` resolves against microsphere's own class loader.**
    `getClassLoader(Handler.class)` (`net/classpath/Handler.java:57`) is the loader that loaded `core`, not the thread
    context loader; a resource visible only to a child loader will not resolve. A miss is an `IOException` (`:59-61`),
    the group's only eager failure besides the convention assertions.
11. **The `_sp` matrix is invisible in `URL.toString()` but visible in equality.** `parseURL` keeps the rewritten
    external form (`net/ExtendableProtocolURLStreamHandler.java:328-337`), and the final `equals`/`hashCode` compare the
    rebuilt external form (`:301-309`), so `test://localhost:12345/abc` and `test:text://localhost:12345/abc` are **not**
    equal while `hostsEqual` says they are (`:311-314`; asserted at
    `net/ExtendableProtocolURLStreamHandlerTest.java:206-223`).
12. **`clearURLStreamHandlerFactory()` does not un-cache the JDK's per-protocol handlers.** It writes only the
    `factory` field (`net/URLUtils.java:1641-1643`); once `java.net.URL` has resolved a protocol through
    `java.protocol.handler.pkgs` it keeps that instance for the life of the JVM, which is why the tests reset the
    *property*, not the cache (`net/AbstractExtendableProtocolURLStreamHandlerTest.java:69-72`).
13. **`CompositeSubProtocolURLConnectionFactory.create` fails loudly when nothing supports the URL.** `selectFactory` indexes the
    list with the `-1` returned by `selectFactoryIndex` (`net/CompositeSubProtocolURLConnectionFactory.java:130-138`)
    → `IndexOutOfBoundsException`; guard with `supports(...)` first (`net/CompositeSubProtocolURLConnectionFactoryTest.java:59-62`).
14. **`console:` writes to the process stdout.** `getOutputStream()` returns `System.out` itself
    (`net/console/ConsoleURLConnection.java:56-59`), so closing the stream closes the console, not a socket. Treat it as
    a shared resource.

---

## 11. See also

* [I/O, NIO, scanning and file filters](io-and-scanning.md) — `IOUtils.close` and the scanners that read `jar:`/`classpath:` URLs
* [Class loading and artifacts](classloading-and-artifacts.md) — the biggest consumer of `URLUtils` (`EMPTY_URL_ARRAY`)
* [Language abstractions](language-abstractions.md) — `Prioritized`, which orders every factory and sub-protocol chain in this group
* [Collections and filters](collections-and-filters.md) — `SetUtils.ofSet`, `MapUtils.toFixedMap` and `ListUtils.newLinkedList`, used throughout §7
* [Utilities and constants](util-and-constants.md) — `StringUtils.split`, `SymbolConstants`, `ProtocolConstants`, `SeparatorConstants.ARCHIVE_ENTRY_SEPARATOR`
* [Type conversion](type-conversion.md) — `URL`/`URI` converters that delegate into this package
* Developer-facing deep dive: [Networking and URL protocols — Developer Guide](../../developer-guide/microsphere-java-core/net-and-url-protocols.md)
* [中文用户指南](zh/net-and-url-protocols.md)
