# JSON

> 语言版本：[中文](json.md) · [English](../en/json.md)
> [← 手册目录](../README.md)

## 一览

| 项目 | 说明 |
|---|---|
| 所属模块 | `microsphere-java-core` |
| 包名 | `io.microsphere.json` |
| 设计源流 | 手写的 `org.json` 风格解析器 |
| 第三方依赖 | 零依赖 —— 这正是它存在的原因 |
| 高层入口 | `JSONUtils`（静态辅助方法，负责与 Java 类型的绑定） |

`io.microsphere.json` 是一个紧凑、无依赖的 JSON 读写实现。它不是用来和 Jackson、Gson 竞争的数据
绑定库；它存在的意义，是让框架能够在 classpath 上没有任何第三方库的前提下，解析并输出自己的 JSON
资源，例如[配置属性元数据](configuration-metadata.md)一文中提到的配置文件。

---

## 1. 为什么手写解析器，以及它不做什么

`microsphere-java-core` 的编译期依赖为零（见[快速开始](getting-started.md)）。因此框架需要的 JSON
能力 —— 解析 `META-INF/microsphere/configuration-properties.json`、由注解处理器输出该文件 —— 全都
必须在仓库内自研。产出就是 `org.json` 风格的 `JSONObject` / `JSONArray` / `JSONTokener` 三件套，
外加负责写出的 `JSONStringer` 和负责绑定的 `JSONUtils`。

能得到什么、得不到什么：

| | `io.microsphere.json` | Jackson / Gson |
|---|---|---|
| 依赖 | 无 | 一个第三方库 |
| 树模型（`JSONObject` / `JSONArray`） | 有 | 有（`ObjectNode` 等） |
| 反射绑定任意领域类 | 基本可用（`readValueAsBean`，基于 setter） | 功能完备 |
| 泛型感知的嵌套集合绑定 | 无 | 有 |
| 命名/注解控制（`@JsonProperty` 风格） | 无 | 有 |
| 多态类型 | 无 | 有 |
| 解析错误 | 受检异常 `JSONException`（经 `JSONUtils` 则包装为 `IllegalArgumentException`） | 运行时异常 |

> [!WARNING]
> 复杂领域模型请使用真正的映射库。`io.microsphere.json` 适合框架自身的元数据、小规模载荷，以及
> 不允许引入 JSON 依赖的模块。

---

## 2. API 一览

| 类型 | 性质 |
|---|---|
| `JSONObject` | public 类（不是 `Map`） |
| `JSONArray` | public 类 |
| `JSONTokener` | public 类，即解析器 |
| `JSONStringer` | public 类，增量写出器 |
| `JSONException` | `public class JSONException extends Exception`（受检） |
| `JSONUtils` | `public abstract class JSONUtils implements Utils` |
| `JSON` | **包级私有类** —— 内部强转辅助，不应使用 |

> [!NOTE]
> `io.microsphere.json.JSON` 是包级私有的*类*，不是接口，`org.json` 中的同名 public 接口在这里不
> 成立。对外可用的能力全部在上述 public 类型上；辅助方法请用 `JSONUtils`。

---

## 3. `JSONObject`

```java
// 构造器
public JSONObject()
public JSONObject(Map copyFrom)
public JSONObject(String json) throws JSONException
public JSONObject(JSONTokener readFrom) throws JSONException
public JSONObject(JSONObject copyFrom, String[] names) throws JSONException

public static final Object NULL;                      // 显式 JSON null 的标记对象

// 写
put(String, boolean / int / long) · put(String, double)   // double 拒绝 NaN/Infinity
put(String, Object)            // value 为 null 时会删除该键
putOpt(String, Object)         // name 或 value 为 null 时静默跳过
accumulate(String, Object)     // 同名重复写入 -> JSONArray
remove(String)

// 读
length() · has(String) · isNull(String)
get(String) throws JSONException                       // 另有 getBoolean/getDouble/getInt/getLong/getString
opt(String)                                          // 另有 optBoolean/optDouble/optInt/optLong/optString，
                                                     //   均带 fallback 重载
getJSONArray(String) · getJSONObject(String)         // 另有 opt 版本

// 结构
keys() · names() · toJSONArray(JSONArray names)      // 投影：names -> values
toString() · toString(int indentSpaces)
```

来自源码的关键规则：

- **null 处理。** `JSONObject.NULL` 是显式 JSON `null` 的哨兵对象；`isNull(name)` 对两者都返回
  true。`put(name, null)` 会直接删除该键 —— 想存 JSON `null`，请放入 `JSONObject.NULL`。
- **非有限 double。** `put(String, double)` 拒绝 `NaN`/Infinity。`put(String, Object)` 会对所有
  `Number` 做有限性检查，因此指标数据中的 `NaN` 必须在调用 `put` 之前过滤掉。
- **抛异常还是回退值。** 类型化 `get*` 抛受检的 `JSONException`；`opt*` 返回 fallback（或该类型
  的零值）。

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
public JSONArray(Iterable<?> copyFrom)          // 涵盖 Collection
public JSONArray(Object array) throws JSONException      // 反射数组
public JSONArray(String json) throws JSONException
public JSONArray(JSONTokener readFrom) throws JSONException

put(boolean / double / int / long / Object) · put(int index, Object)
length() · isNull(int) · get(int) throws JSONException · opt(int)
getBoolean(int) // 另有按下标的类型化 getter 与 opt* 版本
remove(int)
toJSONObject(JSONArray names)     // names -> values 投影为 JSONObject
join(String separator) throws JSONException
toString() · toString(int indentSpaces)
```

```java
JSONArray versions = new JSONArray();
versions.put("0.3.18");
versions.put("0.3.19");
versions.join(",");          // "0.3.18,0.3.19"
```

`toJSONObject(names)` 与 `JSONObject.toJSONArray(names)` 这一对方法可以把表头/数据行列表转成记录；
`join(String)` 则是不带方括号输出标量数组的最廉价方式。

---

## 5. `JSONTokener` 与 `JSONStringer`

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

处理**不完整或拼接**的文档（一连串 JSON 对象）时应使用 `JSONTokener`，因为 `new JSONObject(String)`
要求输入是一个完整文档。`JSONStringer` 不必建树即可增量写出，适合载荷很大或本身就是流的场景。
两者的输入都是 `String`；请先将流读成字符串。

---

## 6. `JSONUtils` —— 解析与绑定辅助

```java
public abstract class JSONUtils implements Utils {

    // 长度 / null / 类型判断（空安全）
    length(JSONObject) · length(JSONArray)
    isEmpty(...) · isNotEmpty(...) · isNull(Object) · isNotNull(Object)
    isJSONObject(Object) · isJSONArray(Object)

    // 解析入口：把 JSONException 包装成 IllegalArgumentException
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
    String escape(String value)          // null/空串原样返回

    // 向调用方持有的 StringBuilder 低层写出；不抛异常
    StringBuilder appendName(StringBuilder, String)
    void appendValue(StringBuilder, Object)
    void append(StringBuilder, String name, <value>)   // 约 30 个重载：全部基本类型及包装类、
                                                       // String、Type、Object、各类数组
}
```

来自源码的绑定行为：

- `readValue(JSONObject, targetType)` 有分派逻辑：目标类型可是 `Map` 时走 `readValueAsMap`，
  其余走 `readValueAsBean`。
- `readValueAsBean` 用无参构造器创建实例，再通过各属性的 write method 写值，值经
  [`Converter`](type-conversion.md) SPI 转换 —— 因此自定义转换器同样影响 JSON 绑定。JSON 中多出的
  键会被静默忽略。
- `readValues(...)` 返回的是你传入的 `multipleClass` **容器**类型（例如 `List.class`），而不是由
  `V` 推断出的 `List<V>`；元素类型是第三个参数。
- `writeBeanAsString` 先通过 `BeanUtils.resolvePropertiesAsMap` 读出 bean 的可读属性，构造
  `JSONObject` 再输出。`writeValueAsString` 会包装任意对象，但只有包装结果是 `JSONObject` 或
  `JSONArray` 时才返回字符串，否则返回 `null`。
- `append*` 系列写入你自己的 `StringBuilder`，从不抛受检异常 —— 注解处理器正是靠它在不处理
  `JSONException` 的情况下增量拼出元数据 JSON。

```java
// 配置属性元数据 -> bean
JSONArray array = new JSONArray(readMetadata());
for (int i = 0; i < array.length(); i++) {
    JSONObject element = array.getJSONObject(i);
    String name = element.getString("name");
    boolean required = element.optBoolean("required", false);
}

// 对象图 -> JSON
String json = JSONUtils.writeBeanAsString(configurationProperty);
ConfigurationProperty back = JSONUtils.readValueAsBean(new JSONObject(json), ConfigurationProperty.class);
```

> [!TIP]
> 解析形状不完全可控的文档时优先用 `opt*` 系列 —— `get*` 系列对每个缺失的键都会抛受检的
> `JSONException`。

---

## 7. 参见

* [配置属性元数据](configuration-metadata.md) —— 本包负责解析的 JSON 文档，以及输出它的生成器 SPI
* [类型转换](type-conversion.md) —— `JSONUtils` 依赖的标量转换
* [核心工具](core-utilities.md) —— `Utils` 标记接口与空安全集合辅助

[← 手册目录](../README.md) · [上一篇：类加载与构件](classloading-and-artifacts.md) · [下一篇：配置属性元数据 →](configuration-metadata.md)
