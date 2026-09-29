# JSON

**Module**: `io.github.microsphere-projects:microsphere-java-core`
**Package**: `io.microsphere.json`

A dependency-free JSON reader/writer in the `org.json` tradition, plus `JSONUtils` helpers that bind JSON to Java
types through the framework's [reflection and conversion](reflection-and-types.md) layers. This is what the metadata
resources are parsed with, and what the annotation processor emits with.

---

## 1. Public API surface

| Type | Kind |
|---|---|
| `JSONObject` | public class (not a `Map`) |
| `JSONArray` | public class |
| `JSONTokener` | public class (the parser) |
| `JSONStringer` | public class (streaming writer) |
| `JSONException` | `public class JSONException extends Exception` |
| `JSONUtils` | `public abstract class JSONUtils implements Utils` |
| `JSON` | **package-private class** (`class JSON`) — internal coercion helpers, not for use |

> [!NOTE]
> `io.microsphere.json.JSON` is a package-private *class*, not an interface and not part of the API — the
> `org.json` equivalent (`public interface JSON`) does not apply here. Use `JSONUtils` for everything it seems to
> offer.

---

## 2. `JSONObject`

```java
// Constructors
public JSONObject()
public JSONObject(Map copyFrom)
public JSONObject(String json) throws JSONException
public JSONObject(JSONTokener readFrom) throws JSONException
public JSONObject(JSONObject copyFrom, String[] names)

public static final Object NULL;                      // explicit JSON null marker

// Write
public JSONObject put(String name, boolean value) throws JSONException
public JSONObject put(String name, double value) throws JSONException      // rejects NaN/Infinity
public JSONObject put(String name, int value)
public JSONObject put(String name, long value)
public JSONObject put(String name, Object value) throws JSONException
public JSONObject putOpt(String name, Object value) throws JSONException  // skips null name/value
public JSONObject accumulate(String name, Object value) throws JSONException   // -> JSONArray on repeat
public Object remove(String name)

// Read
public int length()
public boolean isNull(String name)
public boolean has(String name)
public Object get(String name) throws JSONException
public Object opt(String name)
public boolean getBoolean(String name) throws JSONException      // + getDouble/getInt/getLong/getString
public JSONArray getJSONArray(String name) throws JSONException   // + getJSONObject
public boolean optBoolean(String name)  / optBoolean(String name, boolean fallback)
// ...optDouble / optInt / optLong / optString / optJSONArray / optJSONObject with fallback forms

// Structure
public Iterator keys()
public JSONArray names()
public JSONArray toJSONArray(JSONArray names)          // projection
public String toString()
public String toString(int indentSpaces)
```

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

Getters throw `JSONException`; `opt*` return the fallback (or the type's zero value). `put(String, double)` rejects
non-finite values — a `NaN` in a metric payload must be filtered before you call `put`.

---

## 3. `JSONArray`

```java
public JSONArray()
public JSONArray(Enumeration<?> enumeration)
public JSONArray(Iterable<?> copyFrom)          // covers Collection
public JSONArray(Object array) throws JSONException      // reflection array
public JSONArray(String json) throws JSONException
public JSONArray(JSONTokener readFrom) throws JSONException

public JSONArray put(boolean value) / put(double) / put(int) / put(long) / put(Object)
public JSONArray put(int index, Object value)
public int length()
public boolean isNull(int index)
public Object get(int index) throws JSONException / Object opt(int index)
public boolean getBoolean(int index) throws JSONException   // + typed getters by index, opt* variants
public Object remove(int index)
public JSONObject toJSONObject(JSONArray names)     // names -> values projection
public String join(String separator) throws JSONException
public String toString() / String toString(int indentSpaces)
```

```java
JSONArray versions = new JSONArray();
versions.put(Version.of(0, 3, 18).toString());
versions.put(Version.of(0, 3, 19).toString());
versions.join(",");          // "0.3.18,0.3.19"
```

`toJSONObject(names)` / `JSONObject.toJSONArray(names)` are the pair that turns a header/row list into records,
and `join(String)` is the cheapest way to render a scalar array without the brackets.

---

## 4. `JSONTokener` and `JSONStringer`

```java
public JSONTokener(String in)

public Object nextValue() throws JSONException
public String nextString(char quote) throws JSONException
public String readLiteral() throws JSONException
public JSONObject readObject() throws JSONException
public JSONArray readArray() throws JSONException
public boolean more()
public char next() / char next(char c) / String next(int n)
public boolean hasNext()
public char nextClean() throws JSONException
public String nextTo(char stop) / String nextTo(String stop)
public void skipPast(String to) / void skipTo(char to)
public void back()
public static int dehexchar(char c)

public JSONStringer()
public JSONStringer(int indentSpaces)
public JSONStringer array() / JSONStringer endArray()
public JSONStringer object() / JSONStringer endObject()
public JSONStringer key(String name) throws JSONException
public JSONStringer value(Object v) / value(boolean) / value(double) / value(long)
public String toString()
```

`JSONTokener` is what you want for **partial or concatenated** documents (stream of JSON objects), because
`new JSONObject(String)` demands a complete document. `JSONStringer` writes incrementally without building a tree —
useful when a payload is large or already streamed.

---

## 5. `JSONUtils` — the binding layer

```java
public abstract class JSONUtils implements Utils {

    // Size / null / type tests
    public static int length(JSONObject jsonObject)
    public static int length(JSONArray jsonArray)
    public static boolean isEmpty(JSONObject jsonObject) / boolean isEmpty(JSONArray jsonArray)
    public static boolean isNotEmpty(JSONObject jsonObject) / boolean isNotEmpty(JSONArray jsonArray)
    public static boolean isNull(Object object)
    public static boolean isNotNull(Object object)
    public static boolean isJSONObject(Object value)
    public static boolean isJSONArray(Object value)

    // Parsing entry points (IllegalArgumentException on malformed input, not JSONException)
    public static JSONObject jsonObject(String json) throws IllegalArgumentException
    public static JSONArray jsonArray(String json) throws IllegalArgumentException

    // Binding to Java
    public static <V> V readValue(String json, Class<V> targetType)
    public static <V> V readValue(JSONObject jsonObject, Class<V> targetType)
    public static Map<String, Object> readValueAsMap(JSONObject jsonObject)
    public static <V> V readValueAsBean(JSONObject jsonObject, Class<V> beanClass)
    public static <V> V readValues(String json, Class<V> multipleClass, Class<?> elementClass)
    public static <V> V readValues(JSONArray jsonArray, Class<V> multipleClass, Class<?> elementClass)
    public static Object readValues(JSONArray jsonArray, Type targetType)
    public static <E> E[] readArray(String json, Class<E> componentType)
    public static <E> E[] readArray(JSONArray jsonArray, Class<E> componentType)

    // Binding from Java
    public static String writeValueAsString(Object object)
    public static String writeBeanAsString(Object javaBean)
    public static Class<?> determineElementClass(JSONArray jsonArray)
    public static String escape(@Nullable String value)

    // Low-level emission into a StringBuilder
    public static StringBuilder appendName(StringBuilder jsonBuilder, String name)
    public static void appendValue(StringBuilder jsonBuilder, Object value)
    public static void append(StringBuilder jsonBuilder, String name, <value>)
    //   ~30 append overloads: every primitive + wrapper, String, Type, Object,
    //   every primitive array, every wrapper array, and a generic <T> T[]
}
```

> [!NOTE]
> `readValues(...)` returns the **container** type you asked for (`multipleClass`), not a `List<V>` — the element type
> is the third argument. `append(...)` writes into a `StringBuilder` you own and never throws, which is why the
> annotation processor can build JSON incrementally without `JSONException` handling.

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

`readValue` / `readValueAsBean` build targets through the same layers this guide covers elsewhere — the
[`Converter`](type-conversion.md#1-converter-s-t-spi) SPI for scalar targets and
[`BeanUtils`](reflection-and-types.md#11-io-microsphere-beans--bean-introspection) write methods for bean targets —
so a [custom converter](type-conversion.md#3-writing-a-custom-converter) also affects JSON binding for that type.

`writeBeanAsString` is exactly what `ReflectiveConfigurationPropertyGenerator` calls to emit metadata — see
[Configuration Property Metadata](configuration-metadata.md).

> [!WARNING]
> This is a compact JSON implementation, not a databinding library: no generics-aware nested collection binding, no
> `@JsonProperty`-style naming, no polymorphic types. For a complex domain model, generate a `JSONObject` explicitly
> or use a real mapper and keep `io.microsphere.json` for the framework's own metadata.

---

## 6. See also

* [Configuration Property Metadata](configuration-metadata.md) — `DefaultConfigurationPropertyReader` parses with these
  classes
* [Annotation Processing](annotation-processing.md) — the processor pretty-prints metadata via `JSONArray.toString(2)`
* [Type Conversion](type-conversion.md) — the scalar conversions `JSONUtils` relies on

[← Previous: Class Loading and Artifacts](classloading-and-artifacts.md) · [Index](README.md) ·
[Next: Configuration Property Metadata →](configuration-metadata.md)
