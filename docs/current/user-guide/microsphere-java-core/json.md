# JSON

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-java-core` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Package** | `io.microsphere.json` — 8 main-source types |
| **JDK level** | Java 8 bytecode, inherited from the external build parent; CI-tested on JDK 8 / 11 / 17 / 21 / 25 |
| **Derived from** | a vendored **fork of `org.json`**, specifically Android's `org.json` implementation — `json/package-info.java:19` states "Fork the source code from org.json", and `JSON.java:2`, `JSONArray.java:2`, `JSONException.java:2`, `JSONObject.java:2`, `JSONStringer.java:2`, `JSONTokener.java:2` all carry `Copyright (C) 2010 The Android Open Source Project`. `JSONUtils.java:2-16` and `package-info.java:2-16` carry the Apache Software Foundation header and are original Microsphere code. |
| **Depends on other `core` packages** | `io.microsphere.beans` (`BeanUtils.resolvePropertiesAsMap`, `BeanUtils.findWriteMethod`, `BeanUtils.getBeanMetadata`, `BeanMetadata`), `io.microsphere.collection` (`ListUtils`, `MapUtils`, `EnumerationUtils`, `QueueUtils`, `SetUtils`), `io.microsphere.util` (`ClassUtils`, `CharSequenceUtils`, `StringUtils`, `ExceptionUtils`, `IterableUtils`, `Utils`), `io.microsphere.reflect` (`ConstructorUtils`, `MethodUtils`, `TypeUtils`), `io.microsphere.convert` (`Converter.convertIfPossible`), `io.microsphere.constants` (`SymbolConstants`), `io.microsphere.lang.function` (`ThrowableAction`), `io.microsphere.annotation.Nonnull` / `Nullable` (separate artifact) |
| **External dependencies** | none — pure JDK. There is **no** Jackson, Gson, `org.json`, `jakarta.json` or `javax.json` dependency for this package anywhere in the reactor; `microsphere-java-core/pom.xml:22-74` lists only `microsphere-java-annotations`, two optional logging back-ends and test-scope artifacts. |
| **Used by** | `io.microsphere.metadata` (core), `microsphere-lang-model`, `microsphere-annotation-processor`. Verified in §1 of the developer page. |

All `file:line` references in this page are relative to `microsphere-java-core/src/main/java/io/microsphere/`
(main sources) or `microsphere-java-core/src/test/java/io/microsphere/` (tests), read from the `release` worktree.
After the first mention they are shortened to `json/Foo.java:40-55`.

---

## 1. What this package is for

**Reach for `io.microsphere.json` when you need a small, zero-dependency JSON object model inside a library that
cannot afford to pull in a third-party JSON stack.** The package is a vendored fork of Android's `org.json`
(`json/package-info.java:19`), so its shape is the classic `JSONObject` / `JSONArray` / `JSONTokener` /
`JSONStringer` / `JSONException` set, plus one original utility class, `JSONUtils`, that bridges JSON to Java
collections and JavaBeans. Everything else in `microsphere-java-core` that emits or reads JSON uses these types —
`metadata` writes configuration-property documents with `JSONUtils.append` and reads them back with
`JSONUtils.readValues` (`metadata/DefaultConfigurationPropertyGenerator.java:29-30`,
`metadata/DefaultConfigurationPropertyReader.java:29,43`).

**Reach for `JSONTokener` when you parse a stream of values rather than one document** — it is the lexer plus the
recursive-descent parser, and `nextValue()` returns whichever JSON type comes next
(`json/JSONTokener.java:102-122`).

**Reach for `JSONUtils` when the JSON side should disappear into Java types** — `readValue(json, Person.class)`,
`readValues(json, List.class, Data.class)`, `readArray(json, String.class)`, `readValueAsMap(jsonObject)`,
`writeValueAsString(anyObject)`, `writeBeanAsString(bean)` (`json/JSONUtils.java:884`, `:1041`, `:1172`, `:948`,
`:1235`, `:1273`).

**Do not reach for it when:**

* **You need a production-grade JSON stack.** There is no streaming parser, no `JsonPointer`, no schema, no
  `BigDecimal` preservation, no configurable `Mapper`, and no type-token generic-binding API beyond
  `readValues(JSONArray, Type)` (`json/JSONUtils.java:1074`). The parser is the Android one, with Android's leniencies.
* **You need a strictly RFC-compliant reader.** `JSONTokener` deliberately accepts comments, `#`-comments, single
  quotes, `=`/`=>` as the key/value separator, `;` as an element separator, `0x`/`0` integer prefixes, and dangling
  separators that mean `null` (`json/JSONTokener.java:44-62`). A successful parse proves nothing about the input.
* **You need `null` to mean one thing.** The model carries two incompatible nulls — the Java `null` reference and the
  `JSONObject.NULL` sentinel (`json/JSONObject.java:101-114`, and the warning at `json/JSONArray.java:49-52`). See §10.
* **You need thread safety.** Nothing in the package is safe to share; three of the classes say so explicitly
  (`json/JSONTokener.java:64-68`, `json/JSONArray.java:71-74`, `json/JSONStringer.java:64-68`) and `JSONObject` is
  silent about it.
* **You want a `java.util.Map` or `java.util.List`.** `JSONObject` implements no interface
  (`json/JSONObject.java:83`) and `JSONArray` none (`json/JSONArray.java:76`); neither is `Serializable`. There is no
  `entrySet()`, no `Iterator` over values, no `List` view. Convert explicitly with `JSONUtils.readValueAsMap`
  (`json/JSONUtils.java:948-957`).

> [!IMPORTANT]
> **This package is the library's documented exception to the silent-degradation error policy.** Elsewhere
> `microsphere-java` returns a neutral value or no-ops; here, parsing and typed *getters* throw the checked
> `JSONException` (`json/JSONException.java:41-46`). The exact boundary is in §3.3 — and it is genuinely
> inconsistent between the four write/read paths, which is itself the single biggest thing to know about this package.

---

## 2. Coordinates

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

There is nothing to add for JSON specifically. `io.microsphere.json` ships inside `microsphere-java-core` and drags
in no transitive JSON artifact.

---

## 3. Version information and compatibility range

### 3.1 The facts

| Fact | Value | Evidence (`release`) |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | `pom.xml:12` |
| `artifactId` | `microsphere-java-core` | module `pom.xml:14` |
| Version property | `${revision}`, flattened | `pom.xml:14`, `:15` |
| Value of `${revision}` on `release` | `0.3.20-SNAPSHOT` | `pom.xml:54` |
| Newest published tag | `0.3.20` | release-line table in the group index (`docs/current/user-guide/microsphere-java-core/README.md:70`) |
| Build parent | `microsphere-build:0.3.16` | `pom.xml:6-10` |
| Java level | no `maven.compiler.*` / `<release>` property anywhere in the reactor; the bytecode level is inherited from the external parent `microsphere-build:0.3.16` | CI matrix `java: [ '8', '11', '17', '21', '25' ]`, `.github/workflows/maven-build.yml:25`, `distribution: 'temurin'` `:33` |
| Spring Framework, JDK 17+ | `7.0.9` | `microsphere-java-parent/pom.xml:23` |
| Spring Framework, JDK `[1.8,17)` | `5.3.39` via profile `java8-16`, activation `<jdk>[1.8,17)</jdk>` | `microsphere-java-parent/pom.xml:54-62`, activation `:57` |
| Imported BOMs | `microsphere-all-bom:0.3.13` (`:22`, `:33-36`), `spring-framework-bom` (`:40-46`) | `microsphere-java-parent/pom.xml` |
| `@since` of the original types | `1.0.0` | `json/package-info.java:22`, `json/JSONUtils.java:111` |
| `@since` of the forked types | **absent** — `JSONObject`, `JSONArray`, `JSONTokener`, `JSONStringer`, `JSONException`, `JSON` carry no `@author`/`@since` at all | grep of the package: only two `@since` tags exist |
| Grammar reference in the sources | RFC 4627 | `json/JSONTokener.java:29` |
| Escaping reference in the sources | RFC 7159 | `json/JSONUtils.java:121-129` |

### 3.2 Compatibility range

The whole package compiles and runs on the JDKs in the CI matrix (8, 11, 17, 21, 25) and uses no JDK-internal API,
no `sun.*`, no `Unsafe`, no JPMS `module-info` (there is none anywhere in the repository). Nothing in it reads a
system property or touches Spring, so the `java8-16` profile is irrelevant here.

The API surface is the Android `org.json` surface, which is stable upstream; treat it as **additive-only** —
your code should not depend on the package-private helpers (`json/JSON.java:25` is package-private, and
`JSONStringer.Scope` at `json/JSONStringer.java:81-118` is a package-private nested enum).

### 3.3 Throw or default: where the line actually sits

This is the table to memorise. "Neutral value" means `null`, a default, or the input unchanged.

| Path | On malformed / missing input | Evidence |
|---|---|---|
| `new JSONObject(String)` / `new JSONArray(String)` / `JSONTokener.nextValue()` | **throws checked `JSONException`** | `json/JSONObject.java:178-180`, `json/JSONArray.java:140-142`, `json/JSONTokener.java:102-122`; `json/JSONObjectTest.java:105-107`, `json/JSONTokenerTest.java:111-118` |
| `JSONObject.get(name)` / `JSONArray.get(index)` and every `getX` | **throws checked `JSONException`** | `json/JSONObject.java:393-399`, `json/JSONArray.java:333-343`; `json/JSONObjectTest.java:218-220` |
| every `optX` (`opt`, `optString`, `optInt`, `optJSONObject`, …) | **never throws** — returns `null`, `""`, `0`, `0L`, `false`, `NaN` or your `fallback` | `json/JSONObject.java:407-409`, `:436-452`, `:479-495`, `:521-537`, `:566-584`, `:609-625`, `:652-655`, `:682-685`; `json/JSONObjectTest.java:307-339` |
| `JSONObject.put(name, value)` | throws `JSONException` on a `null` name, and on `NaN`/infinite numbers; **removes the key** on a `null` value | `json/JSONObject.java:274-285`, `:340-350`; `json/JSONObjectTest.java:174-176`, `:537-541` |
| `JSONArray.put(double)` / `put(int, Object)` | throws `JSONException` on `NaN`/infinity | `json/JSONArray.java:189-192`, `:299-310`; `json/JSONArrayTest.java:450-461` |
| `JSONArray.put(Object)` | **no check at all** — the source admits it | `json/JSONArray.java:225-228`, admitted at `json/JSONObject.java:323-324` |
| `toString()` / `toString(int)` | throws **unchecked** `RuntimeException` wrapping `JSONException` | `json/JSONObject.java:762-766`, `json/JSONArray.java:713-717`, `lang/function/ThrowableAction.java:79-81` |
| `JSONUtils.jsonObject(String)` / `jsonArray(String)` | throws **unchecked** `IllegalArgumentException` (cause = the `JSONException`) | `json/JSONUtils.java:803-812`, `:843-852` |
| `JSONUtils.writeValueAsString(Object)` | returns **`null`** when the object is not wrappable | `json/JSONUtils.java:1234-1241`; `json/JSONUtilsTest.java:678-687` |
| `JSONObject.wrap(Object)` | returns **`null`** on any failure, swallowing the exception | `json/JSONObject.java:867-885`; `json/JSONObjectTest.java:518-522` |
| `JSONUtils.escape(String)` | returns the input unchanged; **`null` in → `null` out** | `json/JSONUtils.java:1342-1386`; `json/JSONUtilsTest.java:760-762` |

> [!WARNING]
> Four different failure vocabularies coexist in one package: checked `JSONException` (parse and `getX`), unchecked
> `RuntimeException` (`toString`), unchecked `IllegalArgumentException` (`JSONUtils.jsonObject`/`jsonArray`), and
> silent `null` (`wrap`, `writeValueAsString`, `readValues` on an unsupported target type). If you only catch
> `JSONException`, the last three will get you. §10 items 5, 8, 9 and 11 name the specific unchecked escapes.

---

## 4. The type inventory

| # | Type | Visibility | Role | Forked from AOSP? |
|---|---|---|---|---|
| 1 | `JSONObject` | `public` | ordered name→value map; the `NULL` sentinel; `wrap`, `quote`, `numberToString` | yes (`json/JSONObject.java:2`) |
| 2 | `JSONArray` | `public` | dense indexed sequence; same getters/coercion as `JSONObject` | yes (`json/JSONArray.java:2`) |
| 3 | `JSONTokener` | `public` | lexer + recursive-descent parser over one `String` | yes (`json/JSONTokener.java:2`) |
| 4 | `JSONStringer` | `public` | the only writer for `JSONObject`/`JSONArray`; scope stack + indent | yes (`json/JSONStringer.java:2`) |
| 5 | `JSONException` | `public` | the single checked exception of the package | yes (`json/JSONException.java:2`) |
| 6 | `JSON` | **package-private** | coercion helpers (`toBoolean`/`toDouble`/`toInteger`/`toLong`/`toString`), `checkDouble`, `typeMismatch` factories | yes (`json/JSON.java:2`, `:25`) |
| 7 | `JSONUtils` | `public abstract … implements Utils` | the Microsphere-original bridge: `StringBuilder` JSON writer + JSON↔collections/beans reader | no — ASF header, `@since 1.0.0` (`json/JSONUtils.java:2`, `:109-113`) |
| 8 | `package-info.java` | — | declares the fork: "Fork the source code from org.json" | — (`json/package-info.java:19-22`) |

---

## 5. Parsing text into `JSONObject` / `JSONArray`

### 5.1 The one-argument constructors

```java
JSONObject object = new JSONObject("{\"name\":\"Mercy\",\"age\":18}");
assertTrue("Mercy".equals(object.getString("name")));   // JSONException is checked — declare or catch
```

Condensed from `json/JSONObjectTest.java:110-114`, which also proves the lenient `=` separator:

```java
// json/JSONObjectTest.java:110-113
JSONObject jsonObject = new JSONObject("{ \"name\" : \"Mercy\" , \"age\" = 18 }");
assertEquals("Mercy", jsonObject.getString("name"));
assertEquals(18, jsonObject.getInt("age"));
```

`new JSONArray(String)` is the mirror (`json/JSONArray.java:140-142`), proven at `json/JSONArrayTest.java:98-104`:

```java
// json/JSONArrayTest.java:99-103
JSONArray jsonArray = new JSONArray("[ 1 ,2 , 3]");
assertEquals(3, jsonArray.length());
assertEquals(1, jsonArray.getInt(0));
```

Both constructors delegate to `JSONTokener` and then *steal* the parsed internal map/list
(`json/JSONObject.java:158-169`, `json/JSONArray.java:120-131`). If the first value in the string is not the right
JSON type, you get a `JSONException` from `JSON.typeMismatch` (`json/JSON.java:117-123`) — asserted at
`json/JSONArrayTest.java:92-95` (`{}` fed to `new JSONArray(tokener)`) and `json/JSONObjectTest.java:105-107`
(`Hello` fed to `new JSONObject(tokener)`).

### 5.2 `JSONTokener` directly

Use the tokener when the input holds more than one value, or when you want the lexer's own methods:

```java
// json/JSONTokenerTest.java:59-65 — the resource is microsphere-java-core/src/test/resources/test/json/data.json
JSONTokener jsonTokener = new JSONTokener(getResourceAsString("test/json/data.json"));
JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
assertEquals("Mercy", jsonObject.getString("name"));
```

`nextValue()` returns `JSONObject`, `JSONArray`, `String`, `Boolean`, `Integer`, `Long`, `Double`,
`JSONObject.NULL` or an unquoted `String` — see the javadoc at `json/JSONTokener.java:95-101` and the dispatch at
`:102-122`. Strings may be double- or single-quoted, and a bare word is accepted as a literal:

```java
// json/JSONTokenerTest.java:76-88
assertEquals("Hello", new JSONTokener("\"Hello\"").nextValue());
assertEquals("hello", new JSONTokener("'hello'").nextValue());
assertEquals("Hello", new JSONTokener("Hello").nextValue());   // unquoted literal → String
```

A leading UTF-8 BOM is stripped once, in the constructor (`json/JSONTokener.java:87-93`);
`json/JSONTokenerTest.java:43-46` constructs `new JSONTokener("\ufeffHello")`.

`readObject()` and `readArray()` are public and assume the opening brace was already consumed — that is why they
look odd when called directly (`json/JSONTokener.java:360-413`, `:415-461`). The tests call them that way:

```java
// json/JSONTokenerTest.java:92-98 — note: no leading "{"
JSONObject empty = new JSONTokener("}").readObject();                  // length 0
JSONObject one   = new JSONTokener("\"name\" : \"Mercy\" }").readObject();
assertEquals("Mercy", one.getString("name"));
```

### 5.3 The `JSONUtils` convenience parsers

If you would rather not deal with a checked exception:

```java
// json/JSONUtilsTest.java:508-512
JSONObject jsonObject = JSONUtils.jsonObject("{\"name\":\"John\", \"age\":30}");
assertEquals("John", jsonObject.getString("name"));
assertEquals(30, jsonObject.getInt("age"));

// json/JSONUtilsTest.java:521-526
JSONArray jsonArray = JSONUtils.jsonArray("[1,2,3]");
assertEquals(1, jsonArray.getInt(0));
```

Malformed input is re-wrapped as an unchecked `IllegalArgumentException` whose cause is the original `JSONException`
(`json/JSONUtils.java:803-812`, `:843-852`, via `util/ExceptionUtils.java:97-104` and `:304-308`):

```java
// json/JSONUtilsTest.java:516-518 and :529-531
assertThrows(IllegalArgumentException.class, () -> jsonObject("{name}"));
assertThrows(IllegalArgumentException.class, () -> jsonArray("[1,2,3"));
```

---

## 6. Building objects and arrays

`put` returns `this`, so construction chains fluently. Every `put` overload also declares `throws JSONException`
(`json/JSONObject.java:218-260`), even the `boolean`/`int`/`long` ones, because they route through `checkName`
(`:345-350`).

```java
// json/JSONObjectTest.java:123-136
long time = currentTimeMillis();
jsonObject.put("name", "Mercy")
          .put("age", 18)
          .put("workingTime", time)
          .put("sex", true)
          .put("salary", 1000.0)
          .put("salary", null)                      // removes the mapping, see §10 item 1
          .put("salary", Double.valueOf(1000.0));
assertEquals("Mercy", jsonObject.get("name"));
assertEquals(1000.0, jsonObject.getDouble("salary"));
assertTrue(jsonObject.getBoolean("sex"));
```

`putOpt` is the null-tolerant variant — it silently no-ops when either argument is `null`
(`json/JSONObject.java:296-301`, proven at `json/JSONObjectTest.java:140-145`).

`accumulate` turns a name into an array one value at a time; the second value for the same name promotes the first
into a `JSONArray` (`json/JSONObject.java:317-338`):

```java
// json/JSONObjectTest.java:155-170
jsonObject.accumulate("alias", "mercyblitz");
jsonObject.accumulate("alias", "mz");
assertEquals("mercyblitz", jsonObject.getJSONArray("alias").get(0));
assertEquals("mz", jsonObject.getJSONArray("alias").get(1));
```

Arrays append by index too, null-padding on the way
(`json/JSONArray.java:299-310`):

```java
// json/JSONArrayTest.java:143-148
jsonArray.put(0, true).put(1, 1).put(2, 2L).put(3, 3.0).put(4, "4");
assertEquals(3.0, jsonArray.get(3));
```

`remove` returns the displaced value or `null`, and never throws
(`json/JSONObject.java:359-361`, `json/JSONArray.java:366-371`; tests at `json/JSONObjectTest.java:179-184`,
`json/JSONArrayTest.java:187-198`). `isNull` and `has` are the two membership probes
(`json/JSONObject.java:370-373`, `:382-384`; `json/JSONArray.java:319-322`; tests `json/JSONObjectTest.java:187-203`,
`json/JSONArrayTest.java:152-158`).

---

## 7. Reading values: coercion and defaults

### 7.1 The getter matrix

`JSONObject` and `JSONArray` expose identical getter families; only the key type differs (`String` name vs `int`
index). The coercion itself lives in the package-private `JSON` helper.

| Getter | Accepts | On failure | `JSONObject` lines | `JSONArray` lines |
|---|---|---|---|---|
| `get` / `opt` | anything | `get` throws `JSONException`, `opt` returns `null` | `:393-399` / `:407-409` | `:333-343` / `:352-357` |
| `getBoolean` / `optBoolean` | `Boolean`, or the strings `"true"`/`"false"` case-insensitively | throw / `false` or `fallback` | `:420-427` / `:436-452` | `:382-389` / `:398-414` |
| `getDouble` / `optDouble` | any `Number`, or a parseable `String` | throw / `NaN` or `fallback` | `:463-470` / `:479-495` | `:425-432` / `:441-457` |
| `getInt` / `optInt` | any `Number` (narrowed), or a parseable `String` | throw / `0` or `fallback` | `:505-512` / `:521-537` | `:468-475` / `:484-500` |
| `getLong` / `optLong` | any `Number` (narrowed), or a parseable `String` | throw / `0L` or `fallback` | `:548-555` / `:566-584` | `:511-518` / `:527-543` |
| `getString` / `optString` | `String`, or **anything** — non-`null` values go through `String.valueOf` | throw / `""` or `fallback` | `:593-600` / `:609-625` | `:552-559` / `:568-584` |
| `getJSONArray` / `optJSONArray` | `JSONArray` only | throw / `null` | `:636-643` / `:652-655` | `:595-602` / `:611-614` |
| `getJSONObject` / `optJSONObject` | `JSONObject` only | throw / `null` | `:666-673` / `:682-685` | `:625-632` / `:641-644` |

Every one of those cells is asserted in the suite — e.g. `json/JSONObjectTest.java:307-339` for the no-fallback
`opt` forms and `:544-577` for the explicit-fallback forms; `json/JSONArrayTest.java:463-509` for the array side.

### 7.2 Coercion across types, in both directions

Numbers do not stay in the type you put in. `JSON.toDouble` / `toInteger` / `toLong` accept *any* `Number` and any
parseable `String` (`json/JSON.java:50-96`), so:

```java
// json/JSONArrayTest.java:226-232, 255-261, 285-291
jsonArray.put(1.0);  jsonArray.put("2.0");  jsonArray.put(3);
assertEquals(2.0, jsonArray.getDouble(1));   // String → double
assertEquals(2,   jsonArray.getInt(1));      // String → double → int
assertEquals(3.0, jsonArray.getDouble(2));   // int → double
assertEquals(3L,  jsonArray.getLong(2));     // double → long (lossy; javadoc says so at json/JSONObject.java:540-544)
```

and a non-numeric string simply fails:

```java
// json/JSONObjectTest.java:229-233, 242-246
jsonObject.put("value", "Hello");
assertThrows(JSONException.class, () -> jsonObject.getDouble("value"));
assertThrows(JSONException.class, () -> jsonObject.getInt("value"));
```

`getString` is the one getter that coerces *everything*, because `JSON.toString` falls through to
`String.valueOf(value)` for any non-`String` (`json/JSON.java:98-106`):

```java
// json/JSONTest.java:99-103
assertEquals("1", JSON.toString("1"));
assertEquals("1", JSON.toString(Integer.valueOf(1)));   // number → its text
assertNull(JSON.toString(null));
```

> [!NOTE]
> The pretty-printing parameter is named **`indentSpaces`**, not `indentFactor` — see
> `json/JSONObject.java:758-762` and `json/JSONArray.java:706-713`. It is a literal count of space characters per
> nesting level; there is no tab mode and no per-key ordering option.

---

## 8. Serialization and pretty printing

### 8.1 `toString()` and `toString(int indentSpaces)`

```java
// json/JSONObjectTest.java:419-420
assertEquals("{\"name\":\"Mercy\",\"age\":18,\"workingTime\":" + time + "}", jsonObject.toString());
assertEquals("{\n \"name\": \"Mercy\",\n \"age\": 18,\n \"workingTime\": " + time + "\n}",
             jsonObject.toString(1));

// json/JSONArrayTest.java:414-415
assertEquals("[1,2,3]", jsonArray.toString());
assertEquals("[\n 1,\n 2,\n 3\n]", jsonArray.toString(1));
```

Note the exact shape with `indentSpaces = 1`: a newline plus **one** space before each member, and `": "` between key
and value (`json/JSONStringer.java:388-397`, `:451-453`). The closing bracket is never indented. Indentation comes
from a pre-built space string per instance (`json/JSONStringer.java:136-144`) repeated once per stack depth
(`:394-396`).

`toString(0)` — and therefore `toString()` — is the compact form; `toString(negative)` is also compact, because the
`indent` field stays `null` for anything `<= 0` (`json/JSONStringer.java:137-143`).

### 8.2 `JSONStringer` directly, plus `quote`, `numberToString` and `join`

`JSONStringer` is public but mostly an implementation detail (`json/JSONStringer.java:33-35`). Its value dispatch is
worth reading because it defines what every non-scalar becomes:

```java
// json/JSONStringerTest.java:136-161 — same nine values, indented and compact
"[null,true,null,1,2,3,4,\"5\",\"java.lang.String\"]"
```

`Float 4.0f` prints as `4`, `Double 2.0` prints as `2` (`json/JSONObject.java:816-819`), a `StringBuilder` prints as
a quoted `toString()` (`json/JSONStringer.java:282-284`), and a `Class` prints as its type name
(`:280-281`).

The three remaining public encoders:

```java
// json/JSONObjectTest.java:437-442
assertEquals("0",   numberToString(Double.valueOf(0.0)));   // integral double → no ".0"
assertEquals("-0",  numberToString(NEGATIVE_ZERO));         // json/JSONObject.java:811-814
assertEquals("1.23", numberToString(Double.valueOf(1.23)));

// json/JSONObjectTest.java:457-464
assertEquals("\"Mercy\"", quote("Mercy"));
assertEquals("\"\"", quote(null));                          // json/JSONObject.java:832-834

// json/JSONArrayTest.java:402-407
jsonArray.put(1).put(2).put(3);
assertEquals("1,2,3", jsonArray.join(","));                 // json/JSONArray.java:679-690
```

`quote` and `join` both run through `JSONStringer`'s `Scope.NULL` bracketless mode
(`json/JSONObject.java:835-841`, `json/JSONArray.java:680-689`) so they emit bare values with no `{}`/`[]`.

### 8.3 `JSONUtils.append` — the second, hand-rolled writer

`JSONUtils` writes JSON straight into your `StringBuilder` with no object model in between. It has 34 `append`
overloads — one per primitive, wrapper, `String[]`, primitive array, object array and `Object`
(`json/JSONUtils.java:146-467`) — plus `appendName` (`:500-508`) and `appendValue` (`:469-486`). This is what
`metadata` uses (`metadata/DefaultConfigurationPropertyGenerator.java:88-117`).

```java
// json/JSONUtilsTest.java:121-124, 230-232, 205-208
StringBuilder builder = new StringBuilder();
append(builder, "name", 1);              // "name":1
append(builder, "name", "a");            // "name":"a"     (escaped via JSONUtils.escape)
append(builder, "name", String.class);   // "name":"java.lang.String"
```

`appendValue` dispatches on runtime type: array → `[...]`, `Map` → `{...}`, `Iterable` → `[...]`,
`Character`/`CharSequence`/`Enum` → quoted, `Type` → quoted type name, everything else → `StringBuilder.append(value)`
(`json/JSONUtils.java:469-486`, proven by `json/JSONUtilsTest.java:312-369`).

> [!WARNING]
> `JSONUtils.append` and `JSONObject.toString()` are **two different encoders with different escaping and different
> validation**. `append` performs no finiteness check and no name escaping; `toString()` performs both. Do not mix
> them in one document and expect the bytes to agree. §10 items 4, 6 and 13 give the exact divergences.

---

## 9. Converting between JSON and Java collections and beans

This is `JSONUtils`, and it is the reason `io.microsphere.json` is more than a fork.

### 9.1 Java → JSON string

```java
// json/JSONUtilsTest.java:663-675
String json = writeValueAsString(ofMap("key1", "value1", "key2", 2));
assertEquals(new JSONObject(map).toString(), json);

Object array = ofArray("value1", "value2");
assertEquals(new JSONArray(array).toString(), writeValueAsString(array));
```

`writeValueAsString` delegates to `JSONObject.wrap` and returns `null` when the wrap did not produce a JSON
container (`json/JSONUtils.java:1234-1241`) — so `writeValueAsString("Hello")` is `null`, not `"\"Hello\""`
(`json/JSONUtilsTest.java:678-681`).

`wrap`'s ladder is the whole conversion contract (`json/JSONObject.java:859-886`), and the suite pins every rung:

```java
// json/JSONObjectTest.java:467-515 (selected)
assertEquals(NULL, wrap(null));                                          // :469
assertEquals(new JSONArray(ofList(values)), wrap(ofList(values)));         // :479 Iterable
assertEquals(new JSONArray(ofArray(values)), wrap(ofArray(values)));       // :481 array
assertEquals(new JSONArray(ofEnumeration(values)), wrap(ofEnumeration(values))); // :483
assertEquals(new JSONObject(ofMap("name", "Mercy")), wrap(ofMap("name", "Mercy"))); // :485
assertEquals("java.lang.String", wrap(String.class));                     // :507
assertEquals("NEW", wrap(NEW));                                           // :509 enum → toString()
assertEquals(date.toString(), wrap(date));                                // :512 any java.* → toString()
assertEquals(new JSONObject(), wrap(this));                               // :514 JavaBean → properties
```

A bean goes through `BeanUtils.resolvePropertiesAsMap` (`json/JSONObject.java:881`), and `writeBeanAsString` is the
direct route for that (`json/JSONUtils.java:1272-1277`, proven at `json/JSONUtilsTest.java:689-694`).

### 9.2 JSON → collections and arrays

```java
// json/JSONUtilsTest.java:594-599  (List)
List<Data> dataList = ofList(createData());
String json = writeValueAsString(dataList);
List<Data> copy = readValues(json, List.class, Data.class);
assertEquals(dataList, copy);
```

`readValues(String, Class, Class)` supports exactly six target shapes and returns `null` for anything else
(`json/JSONUtils.java:1125-1141`): array (`isArray`), `List`, `Set`, `Queue`, `Enumeration`, `Iterable`
(otherwise). The `null` fallback is asserted at `json/JSONUtilsTest.java:639-645`:

```java
Object dataCopy = readValues(json, Object.class, Data.class);   // unsupported → null, no exception
assertNull(dataCopy);
```

The other entry points:

```java
// json/JSONUtilsTest.java:647-654  (typed array from a JSON string)
Data[] dataArrayCopy = readArray(json, Data.class);
assertArrayEquals(dataArray, dataArrayCopy);

// json/JSONUtilsTest.java:534-548  (JSONObject → Map, values converted where possible)
Map<String, Object> readValue = readValue(json, Map.class);
assertEquals(data.getName(), readValue.get("name"));
assertArrayEquals(data.getNames(), (String[]) readValue.get("names"));
```

`readValue(json, Map.class)` reaches `readValueAsMap` because of the `isAssignableFrom(Map.class, targetType)`
branch (`json/JSONUtils.java:917-922`, `:948-957`). There is a `Type`-aware overload too —
`readValues(JSONArray, Type)` (`json/JSONUtils.java:1073-1094`) — which infers the element class from the content
when the target is not parameterised, via `determineElementClass` (`:1302-1322`).

### 9.3 JSON → JavaBean

```java
// json/JSONUtilsTest.java:551-557
Data data = createData();
String json = writeValueAsString(data);
Data readValue = readValue(json, Data.class);   // Data has a no-arg constructor + setters
assertEquals(data, readValue);
```

and the multi-value bean, which is the interesting case because each property lands in a different collection type:

```java
// json/JSONUtilsTest.java:560-571
md.setStringList(ofList("a", "b", "c"));
md.setIntegerSet(ofSet(1, 2, 3));
md.setDataQueue(ofQueue(createData()));
md.setClassEnumeration(ofEnumeration(String.class, Integer.class));
md.setObjects(ofArray("Hello", 123, true, 45.67));
MultipleValueData readValue = readValueAsBean(jsonObject(writeValueAsString(md)), MultipleValueData.class);
assertEquals(md, readValue);
```

The mechanism is: instantiate with `ConstructorUtils.newInstance(true, beanClass)`, then for every JSON key find a
write method and convert the value to the *generic* parameter type
(`json/JSONUtils.java:988-1007`). Anything it cannot match is skipped quietly — see
`json/JSONUtilsTest.java:573-581`, where a `null`-valued key and an unrelated `"test-name"` key both disappear.

> [!TIP]
> Round-tripping a `char` bean property through JSON loses its type: `Character` is a wrapper so `wrap` returns it
> as-is (`json/JSONObject.java:864`), the writer quotes it (`json/JSONStringer.java:282-284`), and it comes back as a
> one-character `String`. The suite encodes exactly that at `json/JSONUtilsTest.java:546`
> (`String.valueOf(data.getSex())` compared against the read value). Likewise `short`/`byte` come back as `Integer`
> in the `Map` path (`:544-545`), and `float` as `Double` (`:542`).

---

## 10. Common pitfalls and known limits

1. **There are two nulls and they are not interchangeable.** `JSONObject.NULL` is a sentinel whose `equals`
   deliberately returns `true` for `null` (`json/JSONObject.java:101-114`, asserted at
   `json/JSONObjectTest.java:525-529`), while the Java `null` reference is *not* a legal map value. Consequences:
   `put(name, null)` **removes** the key (`json/JSONObject.java:275-278`), so `new JSONObject().put("a", null)`
   serialises as `{}` while `new JSONObject("{\"a\":null}")` serialises as `{"a":null}` and `has("a")` is `true`
   (`:382-384`) but `isNull("a")` is also `true` (`:370-373`, `json/JSONObjectTest.java:187-195`). `JSONArray.get(index)`
   throws on a raw `null` element yet succeeds on `NULL` (`json/JSONArray.java:333-343`, warned at `:49-52`, asserted
   at `json/JSONArrayTest.java:167-170`). Use `JSONUtils.isNull(Object)` (`json/JSONUtils.java:697-699`) if you want
   one predicate for both.

2. **`getX` is not uniform about *which* failure it reports.** `getInt`, `getLong`, `getDouble`, `getJSONArray` and
   `getJSONObject` all start from `get(name)`, which throws `"No value for <name>"` for a missing key
   (`json/JSONObject.java:393-399`, used at `:463`, `:505`, `:548`, `:636`, `:666`). `getBoolean` and `getString`
   start from `opt(name)` instead (`:421`, `:594`), so the same missing key produces `"Value at <name> is null."`
   from `JSON.typeMismatch` (`json/JSON.java:108-115`). If you match on the message, you will miss half the cases.

3. **`JSONObject.getString` reports the wrong target type.** At `json/JSONObject.java:597` it calls
   `typeMismatch(name, object, "long")` — a copy-paste defect; `json/JSONArray.java:556` (its twin) correctly passes
   `"String"`. The thrown exception is the right class, only the message says "cannot be converted to long".

4. **`NaN` / `Infinity` are rejected on one write path and written verbatim on the other.** The model path refuses
   them: `JSON.checkDouble` (`json/JSON.java:27-32`) is called from `put(String,double)`
   (`json/JSONObject.java:232-234`), `put(String,Object)` for any `Number` (`:279-283`), `accumulate` (`:324-326`),
   `JSONArray.put(double)` (`json/JSONArray.java:189-192`) and `put(int,Object)` (`:299-304`); tests at
   `json/JSONObjectTest.java:537-541` and `json/JSONArrayTest.java:450-461`. `JSONUtils.append` does none of this —
   `append(builder, name, 1.0)` goes straight to `StringBuilder.append(double)`
   (`json/JSONUtils.java:176-179`), and `append(builder, name, (Object) Double.NaN)` falls to the `else` branch of
   `appendValue` (`:483-485`). Both produce `NaN` / `Infinity`, which is **not valid JSON**, and no exception is
   raised. Compounding it, `JSON.toDouble` happily *parses* `"NaN"`, `"Infinity"` and `"-Infinity"` because it calls
   `Double.valueOf(String)` (`json/JSON.java:50-64`).

5. **`JSONArray.put(Object)` is unguarded, and the failure surfaces later as an unchecked exception.** The overload at
   `json/JSONArray.java:225-228` does no `Number` check; `json/JSONObject.java:323-324` even comments the reason
   ("check in accumulate, since array.put(Object) doesn't do any checking"). So `new JSONArray().put((Object) NaN)`
   succeeds and `toString()` afterwards throws `RuntimeException` wrapping `JSONException`
   (`json/JSONArray.java:713-717` → `lang/function/ThrowableAction.java:79-81`). No test covers this shape —
   `json/JSONArrayTest.java:450-461` only exercises the checked `put(double)` and `put(int,double)` overloads.

6. **Two encoders, two escaping rules.** `JSONStringer.string()` escapes `"`, `\`, **`/`**, the six named
   escapes and everything `<= 0x1F` (`json/JSONStringer.java:338-386`; `json/JSONStringerTest.java:188-194` pins
   `"\"\\\//\t\b\n\r\fa"` → `"\"\\\\\\/\\t\\b\\n\\r\\fa"` and U+001F → `\u001f`). `JSONUtils.escape()` escapes `"`,
   `\` and the control range, **not** `/`, but **does** escape `U+2028`/`U+2029` (`json/JSONUtils.java:115-144`,
   `:1342-1386`; `json/JSONUtilsTest.java:806-815`). So `JSONObject.toString()` yields `\/` where `JSONUtils.append`
   yields `/`, and the reverse for U+2028.

7. **`JSONUtils` never escapes property names, and never escapes `char` values.** `appendName` writes
   `'"' + name + '"' + ':'` with no escaping (`json/JSONUtils.java:500-508`), and
   `append(builder, name, 'a')` / `append(builder, name, Character.valueOf('a'))` write the character raw
   (`:181-186`, `:223-228`). A name containing `"` or a `char` value of `"` produces malformed JSON silently. The
   tests only use benign names (`json/JSONUtilsTest.java:372-384`, `:144-148`, `:192-196`).

8. **A malformed `\uXXXX` escape throws `NumberFormatException`, not `JSONException`.**
   `readEscapeCharacter` parses the four hex digits with `Integer.parseInt(hex, 16)` and catches nothing
   (`json/JSONTokener.java:254-260`); the javadoc acknowledges it at `:200` and `:248`. `"\uZZZZ"` therefore escapes a
   `catch (JSONException)`. The suite only covers the truncated variant, `"u123"`, which hits the *length* check and
   does throw `JSONException` (`json/JSONTokenerTest.java:223-227`).

9. **There is no nesting limit on either side, and no cycle guard.** The parser recurses
   `nextValue()` → `readObject()`/`readArray()` → `nextValue()` (`json/JSONTokener.java:102-122`, `:367-413`,
   `:423-461`) with no counter, and the writer recurses `JSONStringer.value(Object)` →
   `JSONObject.writeTo(stringer)` → `stringer.value(...)` (`json/JSONStringer.java:266-271`,
   `json/JSONObject.java:768-774`). Deeply nested input therefore dies with `StackOverflowError`, an `Error` that no
   `catch (JSONException | RuntimeException)` will intercept. `JSONStringer`'s class javadoc still claims "at most 20
   levels of nesting" (`json/JSONStringer.java:61-62`) while the field comment right below says the opposite —
   "Unlike the original implementation, this stack isn't limited to 20 levels of nesting" (`:120-123`). Cycles are
   also constructible by hand (`json/JSONArrayTest.java:338-341` puts an array inside itself) and `toString()` on such
   an array overflows.

10. **Numbers never become `BigDecimal` or `BigInteger`.** `readLiteral` tries `Integer`, then `Long`, then `Double`
    and falls back to an unquoted `String` (`json/JSONTokener.java:305-339`); the whole package contains zero
    references to `BigDecimal`/`BigInteger`. `21474836470` parses to a `Long`
    (`json/JSONTokenerTest.java:241`) but anything beyond `Long.MAX_VALUE` silently becomes a `Double` and loses
    precision. `JSON.toInteger` and `toLong` narrow with a C-style cast
    (`json/JSON.java:75`, `:91`), so `getInt("999999999999")` returns `Integer.MAX_VALUE` rather than failing —
    `JSONTest.java:84` already shows `"1.0"` becoming `1`.

11. **`jsonObject(null)` / `jsonArray(null)` throw `NullPointerException`, not the documented
    `IllegalArgumentException`.** `new JSONTokener(null)` accepts the `null` and stores it
    (`json/JSONTokener.java:87-93`, with the javadoc admitting it at `:84-85`); the first read dereferences it at
    `:501-503`. Since `JSONUtils.jsonObject` only catches `JSONException` (`json/JSONUtils.java:806-810`), the NPE
    escapes unwrapped and the `@throws IllegalArgumentException` contract at `:801` is not met. There is no test for
    the `null` input — `json/JSONTokenerTest.java:49-51` constructs the tokener with `null` and never reads from it.

12. **`wrap` and `readValues` swallow failures into `null`.** `JSONObject.wrap` wraps its entire dispatch in
    `catch (Exception ignored) { }` (`json/JSONObject.java:867-885`), so a map with a `null` key, a bean whose getter
    throws, or a self-`equals`-broken value all end up as `null` — asserted at `json/JSONObjectTest.java:518-522` and
    then re-serialized away by `JSONUtils.writeValueAsString` (`json/JSONUtils.java:1235-1241`,
    `json/JSONUtilsTest.java:678-687`). `readValues` similarly returns `null` for an unsupported target type
    (`:1125-1141`, `json/JSONUtilsTest.java:639-645`). Neither is a bug, but both mean **a `null` result tells you
    nothing about why**.

13. **`JSONUtils`' null handling is the opposite of the model's.** `appendValue` calls `value.getClass()` as its
    first statement (`json/JSONUtils.java:469-470`), so `append(builder, "k", (Object) null)` throws `NullPointerException`
    — and so does any `Map` or `Iterable` containing one `null` element, via `appendMap`/`appendIterable`
    (`:1388-1415`). The same holds for `determineElementClass`, which dereferences every element
    (`:1303-1310`), and for `readValueAsMap`/`readValueAsBean`, which call `jsonObject.length()` /
    `getBeanMetadata(...)` before any null check (`:948-957`, `:988-990`). No test in
    `json/JSONUtilsTest.java:319-334` asserts anything about the map case — that test has no assertion at all.

---

## 11. See also

* [Configuration property metadata](configuration-metadata.md) — `metadata` is the largest in-library consumer:
  `DefaultConfigurationPropertyGenerator` writes JSON with `JSONUtils.append`
  (`metadata/DefaultConfigurationPropertyGenerator.java:29-30,88-117`), `ReflectiveConfigurationPropertyGenerator`
  with `writeBeanAsString` (`metadata/ReflectiveConfigurationPropertyGenerator.java:24,78`), and
  `DefaultConfigurationPropertyReader` reads with `readValues(content, List.class, Map.class)`
  (`metadata/DefaultConfigurationPropertyReader.java:29,43`).
* [Reflection, types and beans](reflection-and-beans.md) — `BeanUtils.resolvePropertiesAsMap` is what makes
  `JSONObject.wrap` handle JavaBeans (`json/JSONObject.java:25,881`), and `ConstructorUtils` / `MethodUtils` /
  `TypeUtils` drive `readValueAsBean` (`json/JSONUtils.java:988-1007`).
* [Type conversion](type-conversion.md) — `Converter.convertIfPossible` is the leaf of every
  JSON→Java scalar conversion (`json/JSONUtils.java:63,1427-1442`).
* [Collections and filters](collections-and-filters.md) — `ListUtils.newArrayList` and `MapUtils.newLinkedHashMap`
  decide `JSONArray`'s storage and `JSONObject`'s key ordering (`json/JSONArray.java:84`, `json/JSONObject.java:122`).
* [Utilities and constants](util-and-constants.md) — `ClassUtils` supplies every type predicate used by `wrap`
  (`json/JSONObject.java:39-45`) and `SymbolConstants` supplies the punctuation used by `JSONUtils`
  (`json/JSONUtils.java:56-62`).
* [Serialization](serialization.md) — the neighbouring `io.microsphere.io.serializer` package, which covers binary
  `Object` ↔ `byte[]` and is the right choice when you do *not* want a text format.
* [Developer Guide — JSON](../../developer-guide/microsphere-java-core/json.md) — the tokener state machine, the
  `JSONObject` map choice, the writer's scope stack, the exception taxonomy, the design-pattern inventory, the
  package-dependency graph and the review notes.
