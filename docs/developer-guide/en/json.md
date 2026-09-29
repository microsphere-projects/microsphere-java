# JSON

> Read this page in: [中文](../zh/json.md) · [English](json.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Module | `microsphere-java-core` |
| Package | `io.microsphere.json` |
| Design lineage | hand-written parser in the `org.json` tradition |
| Third-party dependencies | none — this is why the parser exists |
| High-level entry point | `JSONUtils` (static helpers, binding to Java types) |

`io.microsphere.json` is a compact, dependency-free JSON reader/writer. It is not a databinding
library competing with Jackson or Gson; it exists so that the framework can parse and emit its own
JSON resources — for example the configuration-property metadata described in
[Configuration Property Metadata](configuration-metadata.md) — without any third-party library on
the classpath.

---

## 1. Why a hand-written parser, and what it does not do

`microsphere-java-core` has zero compile-scope dependencies (see
[Getting Started](getting-started.md)). Every capability the framework needs from JSON — parsing
`META-INF/microsphere/configuration-properties.json`, emitting it from the annotation processor —
therefore has to be implemented in-repo. The result is the `org.json`-style trio
`JSONObject` / `JSONArray` / `JSONTokener`, plus `JSONStringer` for writing and `JSONUtils` for
binding.

What you get, and what you do not:

| | `io.microsphere.json` | Jackson / Gson |
|---|---|---|
| Dependencies | none | one library |
| Tree model (`JSONObject` / `JSONArray`) | yes | yes (`ObjectNode` etc.) |
| Bind JSON to an arbitrary domain class by reflection | basic (`readValueAsBean`, setter-based) | full-featured |
| Generics-aware nested collection binding | no | yes |
| Naming/annotation controls (`@JsonProperty`-style) | no | yes |
| Polymorphic types | no | yes |
| Parser errors | checked `JSONException` (or `IllegalArgumentException` via `JSONUtils`) | runtime exceptions |

> [!WARNING]
> For a complex domain model, use a real mapper. Keep `io.microsphere.json` for the framework's own
> metadata, for small payloads, and for cases where adding a JSON dependency to your module is
> unacceptable.

---

## 2. API surface

| Type | Kind |
|---|---|
| `JSONObject` | public class (not a `Map`) |
| `JSONArray` | public class |
| `JSONTokener` | public class — the parser |
| `JSONStringer` | public class — incremental writer |
| `JSONException` | `public class JSONException extends Exception` (checked) |
| `JSONUtils` | `public abstract class JSONUtils implements Utils` |
| `JSON` | **package-private class** — internal coercion helpers, not for use |

> [!NOTE]
> `io.microsphere.json.JSON` is a package-private *class*, not an interface. The `org.json`
> equivalent (`public interface JSON`) does not apply here. Everything usable lives on the public
> types above; for helpers use `JSONUtils`.

---

## 3. `JSONObject`

```java
// Constructors
public JSONObject()
public JSONObject(Map copyFrom)
public JSONObject(String json) throws JSONException
public JSONObject(JSONTokener readFrom) throws JSONException
public JSONObject(JSONObject copyFrom, String[] names) throws JSONException

public static final Object NULL;                      // explicit JSON null marker

// Write
put(String, boolean / int / long) · put(String, double)   // double rejects NaN/Infinity
put(String, Object)            // null value REMOVES the key
putOpt(String, Object)         // no-op when name or value is null
accumulate(String, Object)     // repeated name -> JSONArray
remove(String)

// Read
length() · has(String) · isNull(String)
get(String) throws JSONException                       // + getBoolean/getDouble/getInt/getLong/getString
opt(String)                                          // + optBoolean/optDouble/optInt/optLong/optString,
                                                     //   each with a fallback form
getJSONArray(String) · getJSONObject(String)         // + opt forms

// Structure
keys() · names() · toJSONArray(JSONArray names)      // projection: names -> values
toString() · toString(int indentSpaces)
```

Key rules, read from source:

- **Null handling.** `JSONObject.NULL` is the sentinel for an explicit JSON `null`;
  `isNull(name)` is true for both a missing-mapped `null` and `NULL`. `put(name, null)` removes the
  key entirely — to store a JSON `null`, put `JSONObject.NULL`.
- **Non-finite doubles.** `put(String, double)` rejects `NaN`/Infinity. `put(String, Object)`
  checks *all* `Number` values for finiteness, so a `NaN` in a metric payload must be filtered
  before you call `put`.
- **Throw vs. fallback.** Typed `get*` throw the checked `JSONException`; `opt*` return the
  fallback (or the type's zero value) instead.

```java
JSONObject object = new JSONObject()
        .put("name", "microsphere")
        .put("major", 0)
        .put("snapshot", true)
        .accumulate("tag", "v0.3.19")
        .accumulate("tag", "release");

object.toString(2);
// {
//   "name": "microsphere",
//   "major": 0,
//   "snapshot": true,
//   "tag": ["v0.3.19","release"]
// }

JSONObject parsed = new JSONObject(jsonString);
int major = parsed.optInt("major", 0);
if (!parsed.isNull("preRelease")) { /* ... */ }
```

---

## 4. `JSONArray`

```java
public JSONArray()
public JSONArray(Enumeration<?> enumeration)
public JSONArray(Iterable<?> copyFrom)          // covers Collection
public JSONArray(Object array) throws JSONException      // reflection array
public JSONArray(String json) throws JSONException
public JSONArray(JSONTokener readFrom) throws JSONException

put(boolean / double / int / long / Object) · put(int index, Object)
length() · isNull(int) · get(int) throws JSONException · opt(int)
getBoolean(int) // + typed getters by index, opt* variants
remove(int)
toJSONObject(JSONArray names)     // names -> values projection into a JSONObject
join(String separator) throws JSONException
toString() · toString(int indentSpaces)
```

```java
JSONArray versions = new JSONArray();
versions.put("0.3.18");
versions.put("0.3.19");
versions.join(",");          // "0.3.18,0.3.19"
```

`toJSONObject(names)` and `JSONObject.toJSONArray(names)` are the pair that turns a header/row list
into records; `join(String)` renders a scalar array without the brackets.

---

## 5. `JSONTokener` and `JSONStringer`

```java
public JSONTokener(String in)

nextValue() · nextString(char quote) · readLiteral()
readObject() · readArray()
more() · hasNext() · next() · next(char) · next(int n) · nextClean()
nextTo(char) · nextTo(String) · skipPast(String) · skipTo(char) · back()
static dehexchar(char)

public JSONStringer()
public JSONStringer(int indentSpaces)
array() · endArray() · object() · endObject()
key(String) throws JSONException
value(Object / boolean / double / long)
toString()
```

Use `JSONTokener` for **partial or concatenated** documents (a stream of JSON objects), because
`new JSONObject(String)` demands one complete document. `JSONStringer` writes incrementally without
building a tree — useful when a payload is large or already streamed. Both take `String` input;
read a stream into a `String` first.

---

## 6. `JSONUtils` — parsing and binding helpers

```java
public abstract class JSONUtils implements Utils {

    // Size / null / type tests (null-safe)
    length(JSONObject) · length(JSONArray)
    isEmpty(...) · isNotEmpty(...) · isNull(Object) · isNotNull(Object)
    isJSONObject(Object) · isJSONArray(Object)

    // Parsing entry points: wrap JSONException into IllegalArgumentException
    JSONObject jsonObject(String json)
    JSONArray  jsonArray(String json)

    // JSON -> Java
    <V> V readValue(String json, Class<V> targetType)
    <V> V readValue(JSONObject jsonObject, Class<V> targetType)
    Map<String, Object> readValueAsMap(JSONObject jsonObject)
    <V> V readValueAsBean(JSONObject jsonObject, Class<V> beanClass)
    <V> V readValues(String json, Class<V> multipleClass, Class<?> elementClass)
    <V> V readValues(JSONArray jsonArray, Class<V> multipleClass, Class<?> elementClass)
    Object  readValues(JSONArray jsonArray, Type targetType)
    <E> E[] readArray(String json, Class<E> componentType)
    <E> E[] readArray(JSONArray jsonArray, Class<E> componentType)

    // Java -> JSON
    String writeValueAsString(Object object)
    String writeBeanAsString(Object javaBean)
    Class<?> determineElementClass(JSONArray jsonArray)
    String escape(String value)          // null/empty input returned as-is

    // Low-level emission into a StringBuilder you own; never throws
    StringBuilder appendName(StringBuilder, String)
    void appendValue(StringBuilder, Object)
    void append(StringBuilder, String name, <value>)   // ~30 overloads: every primitive +
                                                       // wrapper, String, Type, Object, arrays
}
```

Binding behaviour, read from source:

- `readValue(JSONObject, targetType)` dispatches: `Map`-assignable targets go through
  `readValueAsMap`, everything else through `readValueAsBean`.
- `readValueAsBean` creates the instance with a no-arg constructor and writes each key via its
  bean write method, converting values through the [`Converter`](type-conversion.md) SPI — so a
  custom converter also affects JSON binding for that type. Unknown keys are silently ignored.
- `readValues(...)` returns the **container** type you passed as `multipleClass` (e.g. `List.class`),
  not a `List<V>` inferred from `V`; the element type is the third argument.
- `writeBeanAsString` resolves the bean's readable properties (`BeanUtils.resolvePropertiesAsMap`)
  into a `JSONObject` and prints it. `writeValueAsString` wraps an arbitrary object, but returns
  `null` unless the wrap produces a `JSONObject` or `JSONArray`.
- The `append*` family writes into a `StringBuilder` and never throws checked exceptions — this is
  how the annotation processor builds metadata JSON incrementally without `JSONException` handling.

```java
// Configuration-property metadata -> beans
JSONArray array = new JSONArray(readMetadata());
for (int i = 0; i < array.length(); i++) {
    JSONObject element = array.getJSONObject(i);
    String name = element.getString("name");
    boolean required = element.optBoolean("required", false);
}

// Object graph -> JSON
String json = JSONUtils.writeBeanAsString(configurationProperty);
ConfigurationProperty back = JSONUtils.readValueAsBean(new JSONObject(json), ConfigurationProperty.class);
```

> [!TIP]
> Prefer the `opt*` accessors when parsing documents whose shape you do not fully control — the
> `get*` family throws a checked `JSONException` on every missing key.

---

## 7. See also

* [Configuration Property Metadata](configuration-metadata.md) — the JSON document this package
  parses and the generator SPI that emits it
* [Type Conversion](type-conversion.md) — the scalar conversions `JSONUtils` relies on
* [Core Utilities](core-utilities.md) — the `Utils` marker and null-safe collection helpers

[← Handbook index](../README.md) · [Previous: Class Loading and Artifacts](classloading-and-artifacts.md) · [Next: Configuration Property Metadata →](configuration-metadata.md)
