# 配置属性元数据

> 语言版本：[中文](configuration-metadata.md) · [English](../en/configuration-metadata.md)
> [← 手册目录](../README.md)

## 一览

| 项目 | 说明 |
|---|---|
| 相关包 | `io.microsphere.metadata`（SPI）· `io.microsphere.beans`（数据模型）· `io.microsphere.annotation`（注解） |
| 所属模块 | `microsphere-java-core`（另涉及 `microsphere-java-annotations`、`microsphere-annotation-processor`） |
| 数据文档 | `META-INF/microsphere/configuration-properties.json` |
| 合并通道 | `META-INF/microsphere/additional-configuration-properties.json` |
| SPI 链路 | `ConfigurationPropertyGenerator` → 资源文件 → `ConfigurationPropertyLoader` + `ConfigurationPropertyReader` |

这是 `@ConfigurationProperty` 故事的运行期一半：一份描述所有已文档化配置属性的 JSON 文档，以及
生产、加载、解析它的 SPI 链路。

---

## 1. 注解与数据模型

`io.microsphere.annotation.ConfigurationProperty`（`@Retention(RUNTIME)`、`@Target(FIELD)`）用于
标注属性名常量：

| 成员 | 默认值 | 说明 |
|---|---|---|
| `name()` | `""` | 缺省时，处理器使用字段的**常量值** |
| `type()` | `String.class` | 缺省时，处理器使用字段的类型 |
| `defaultValue()` | `""` | |
| `description()` | `""` | 为空白时，处理器回退到字段的 Javadoc |
| `required()` | `false` | |
| `source()` | `{}` | 使用常量 `SYSTEM_PROPERTIES_SOURCE` = `"system-properties"`、`ENVIRONMENT_VARIABLES_SOURCE` = `"environment-variables"`、`APPLICATION_SOURCE` = `"application"` |

运行期模型是 `io.microsphere.beans.ConfigurationProperty`，字段为 `name`、`type`（以 **String**
存储）、`value`、`defaultValue`、`required`、`description`，外加嵌套的 `Metadata` 对象（`sources`、
`targets`、`declaredClass`、`declaredField`）。JSON 结构与它一一对应；下面是
`microsphere-java-core` 自带文件中的一个真实条目：

```json
[
  {
    "name": "microsphere.io.buffer.size",
    "type": "int",
    "defaultValue": "2048",
    "required": false,
    "description": "The buffer size for I/O",
    "metadata": {
      "sources": ["system-properties"],
      "declaredClass": "io.microsphere.io.IOUtils",
      "declaredField": "BUFFER_SIZE_PROPERTY_NAME"
    }
  }
]
```

`value`、`defaultValue`、`description` 仅在非 null 时输出；`metadata.sources` 使用上述 source 常量。

---

## 2. 资源路径

定义在 `io.microsphere.constants.ResourceConstants`：

| 常量 | 值 |
|---|---|
| `METADATA_RESOURCE` | `META-INF/` |
| `MICROSPHERE_METADATA_RESOURCE` | `META-INF/microsphere/` |
| `CONFIGURATION_PROPERTY_METADATA_FILE_NAME` | `configuration-properties.json` |
| `ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_FILE_NAME` | `additional-configuration-properties.json` |
| `CONFIGURATION_PROPERTY_METADATA_RESOURCE` | **`META-INF/microsphere/configuration-properties.json`** |
| `ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE` | **`META-INF/microsphere/additional-configuration-properties.json`** |

> [!IMPORTANT]
> 这些路径**不是** Spring Boot 的 `spring-configuration-metadata.json` /
> `additional-spring-configuration-metadata.json`，Spring 项目不会自动识别它们。
> `microsphere-java-core` 自带一份已填充内容的
> `META-INF/microsphere/configuration-properties.json`，描述其自身的属性。

---

## 3. 编译期的一半

`microsphere-annotation-processor` 的 `ConfigurationPropertyAnnotationProcessor` 访问每个
`@ConfigurationProperty` 字段，根据注解属性构建 `ConfigurationProperty`（`name`/`type`/`description`
缺省时从字段本身取值），通过找到的第一个 `ConfigurationPropertyGenerator` 服务序列化为 JSON，再用
`JSONArray.toString(2)` 美化输出，写入**你的** jar 中的
`META-INF/microsphere/configuration-properties.json`。详见
[注解处理](annotation-processing.md)。

---

## 4. 三个运行期 SPI

```java
public interface ConfigurationPropertyReader extends Prioritized {
    default List<ConfigurationProperty> read(InputStream inputStream) throws Throwable // 以 DEFAULT_CHARSET 解码
    default List<ConfigurationProperty> read(Reader reader) throws Throwable           // 经 IOUtils.copyToString 转为字符串
    List<ConfigurationProperty> read(String content) throws Throwable                  // 实现这一个即可
}

public interface ConfigurationPropertyLoader extends Prioritized {
    @Nullable List<ConfigurationProperty> load() throws Throwable;
    @Nonnull @Immutable static List<ConfigurationProperty> loadAll();
}

public interface ConfigurationPropertyGenerator extends Prioritized {
    String generate(ConfigurationProperty configurationProperty) throws IllegalArgumentException;
}
```

`io.microsphere.metadata` 提供的实现：

| 角色 | 类 | 优先级 | 说明 |
|---|---|---|---|
| Reader | `DefaultConfigurationPropertyReader` | `MIN_PRIORITY` | 经 `JSONUtils.readValues` + `io.microsphere.json` 解析上述 JSON 数组 |
| Loader | `ClassPathResourceConfigurationPropertyLoader` | — | **抽象**基类：任意 classpath 资源，读第一个或全部 |
| Loader | `MetadataResourceConfigurationPropertyLoader` | `MIN_PRIORITY` | 加载 `CONFIGURATION_PROPERTY_METADATA_RESOURCE`；**未做 SPI 注册** |
| Loader | `AdditionalMetadataResourceConfigurationPropertyLoader` | `MIN_PRIORITY + 9` | 加载 `ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_RESOURCE` |
| Generator | `DefaultConfigurationPropertyGenerator` | `MIN_PRIORITY` | 手工拼 JSON，按文档顺序输出键，跳过 null 的可选值 |
| Generator | `ReflectiveConfigurationPropertyGenerator` | — | 委托 `JSONUtils.writeBeanAsString(configurationProperty)` |

`microsphere-java-core/src/main/resources/META-INF/services/` 中注册的服务：

```
io.microsphere.metadata.ConfigurationPropertyReader     -> io.microsphere.metadata.DefaultConfigurationPropertyReader
io.microsphere.metadata.ConfigurationPropertyLoader     -> io.microsphere.metadata.AdditionalMetadataResourceConfigurationPropertyLoader
io.microsphere.metadata.ConfigurationPropertyGenerator  -> io.microsphere.metadata.DefaultConfigurationPropertyGenerator
```

> [!IMPORTANT]
> Loader 中只注册了 **additional** 资源那一个。因此开箱即用时，
> `ConfigurationPropertyLoader.loadAll()` 读取的是
> `META-INF/microsphere/additional-configuration-properties.json`，而**不是**
> `configuration-properties.json`。若要读取主资源，请自行实例化
> `MetadataResourceConfigurationPropertyLoader`，或把它写进你自己的 services 文件。

> [!NOTE]
> 只有 `ConfigurationPropertyLoader` 提供静态聚合方法 `loadAll()`。Reader 需通过
> `ServiceLoaderUtils.loadFirstService(ConfigurationPropertyReader.class)` 获取。

---

## 5. 运行期加载

```java
List<ConfigurationProperty> properties = ConfigurationPropertyLoader.loadAll();

for (ConfigurationProperty property : properties) {
    System.out.printf("%s (%s) = %s  [%s]%n",
            property.getName(),
            property.getType(),
            property.getDefaultValue(),
            String.join(",", property.getMetadata().getSources()));
}
```

`loadAll()` 是接口静态方法，源码行为如下：

1. `ServiceLoaderUtils.loadServicesList(ConfigurationPropertyLoader.class)` —— 按优先级排序；
2. 依次调用每个 loader 的 `load()`，把非空结果追加进一个 `LinkedList`；
3. **逐 loader** 捕获 `Throwable`，记录 error 日志后继续下一个；
4. 返回**不可修改**列表；方法签名无 `throws` —— 失败不会外泄。

各 loader 相互独立，一个坏掉的 loader 不会阻断整体发现。

### 扩展 `ClassPathResourceConfigurationPropertyLoader`

```java
protected ClassPathResourceConfigurationPropertyLoader(String resourceName)                       // loadedAll = false
protected ClassPathResourceConfigurationPropertyLoader(String resourceName, ClassLoader classLoader)
protected ClassPathResourceConfigurationPropertyLoader(String resourceName, boolean loadedAll)
protected ClassPathResourceConfigurationPropertyLoader(String resourceName, ClassLoader classLoader, boolean loadedAll)

@Override public final List<ConfigurationProperty> load() throws Throwable    // FINAL
```

`loadedAll = true` 时遍历 `classLoader.getResources(resourceName)`，合并 classpath 上每一处同名
资源 —— 分模块维护元数据时正是你要的行为；`false` 时只读第一个资源流。资源缺失时仅记录 trace
日志并跳过。解析工作由内部硬编码的 `DefaultConfigurationPropertyReader` 完成。

```java
public class AppConfigPropertiesLoader extends ClassPathResourceConfigurationPropertyLoader {

    public AppConfigPropertiesLoader() {
        super("META-INF/microsphere/app-configuration-properties.json", true);
    }

    @Override
    public int getPriority() {
        return Prioritized.NORMAL_PRIORITY;      // 排在内建 loader 之前
    }
}
```

```
# META-INF/services/io.microsphere.metadata.ConfigurationPropertyLoader
com.example.config.AppConfigPropertiesLoader
```

---

## 6. 合并与手写元数据

为未标注解的属性（三方开关、历史遗留键）补充元数据的推荐方式，是自行维护
`META-INF/microsphere/additional-configuration-properties.json`：它是 [§1](#1-注解与数据模型)
所述对象的数组，由 core 中注册的 `AdditionalMetadataResourceConfigurationPropertyLoader` 在运行期
合并 classpath 上每个 jar 的同名资源并贡献给 `loadAll()`。

> [!WARNING]
> `DefaultConfigurationPropertyReader` 对每个元素都要求存在 `metadata` 对象（无条件访问）和
> `required` 布尔值（无 null 检查直接拆箱）。缺任何一个都会在 `loadAll()` 期间抛 NPE ——
> 而由于 `loadAll()` 吞掉 loader 失败只记 error 日志，你的条目会悄悄消失而不是让调用崩溃。
> 请像 `DefaultConfigurationPropertyGenerator` 那样始终输出全部七个键。

> [!NOTE]
> 注解处理器**不会**读取 additional 资源；合并只发生在运行期，通过注册的 loader 完成。

---

## 7. 从 bean 生成元数据

```java
ConfigurationPropertyGenerator generator =
        ServiceLoaderUtils.loadFirstService(ConfigurationPropertyGenerator.class);
String json = generator.generate(property);   // property 是一个 ConfigurationProperty 实例
```

两个生成器都接收 `ConfigurationProperty` 并输出其 JSON 文本：
`DefaultConfigurationPropertyGenerator` 用 `JSONUtils.append*` 手工构建，键顺序为
`name, type, value, defaultValue, required, description, metadata`，并跳过 null 的可选值；
`ReflectiveConfigurationPropertyGenerator` 则通过 `JSONUtils.writeBeanAsString` 泛化地序列化同一个
对象。三个 SPI 都是 `Prioritized`，因此 `loadFirstService(...)` 会尊重你的覆盖：

```java
public class CompactGenerator implements ConfigurationPropertyGenerator {
    @Override public String generate(ConfigurationProperty property) { /* ... */ return "{}"; }
    @Override public int getPriority() { return Prioritized.MAX_PRIORITY; }
}
```

---

## 8. 完整链路

```
字段上的 @ConfigurationProperty                          （annotations 模块）
        |  编译期：ConfigurationPropertyAnnotationProcessor
        |  经由第一个 ConfigurationPropertyGenerator 服务
        v
META-INF/microsphere/configuration-properties.json       （你的 jar）
        |  运行期：ConfigurationPropertyLoader.loadAll()
        |  -> 已注册的 loader -> ClassPathResourceConfigurationPropertyLoader
        |  -> DefaultConfigurationPropertyReader（io.microsphere.json）
        v
List<ConfigurationProperty>
        +  META-INF/microsphere/additional-configuration-properties.json
           （运行期由 AdditionalMetadataResourceConfigurationPropertyLoader 合并）
```

---

## 参见

* [JSON](json.md) —— `DefaultConfigurationPropertyReader` 使用的解析器
* [注解](annotations.md) —— 可以标注什么、哪些默认值会被自动补齐
* [注解处理](annotation-processing.md) —— 编译期的另一半
* [参考手册](reference.md) —— 全部 SPI 服务文件

[← 手册目录](../README.md) · [上一篇：JSON](json.md) · [下一篇：注解处理 →](annotation-processing.md)
