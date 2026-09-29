# 注解

> 语言版本：[中文](annotations.md) · [English](../en/annotations.md)
> [← 手册目录](../README.md)

## 一览

| 项目 | 取值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-java-annotations` |
| 包 | `io.microsphere.annotation`、`io.microsphere.annotation.concurrent` |
| 内容 | 八个 `@Documented` 注解类型 |
| 运行期开销 | 无；只有 `@ConfigurationProperty` 带一套构建期工具链 |

这些注解是整个库的 Javadoc 与工具链使用的词汇表。其中七个纯粹是文档信号；
`@ConfigurationProperty` 则通过注解处理器把被标注的字段变成机器可读的元数据。

---

## 1. 八个注解

| 注解 | `@Target` | `@Retention` | 成员 |
|---|---|---|---|
| `@Since` | `TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE` | `RUNTIME` | `String value()`（必填）· `String module() default ""` |
| `@Experimental` | `ANNOTATION_TYPE, CONSTRUCTOR, FIELD, METHOD, TYPE` | `SOURCE` | `String description() default ""` |
| `@Immutable` | *（未声明）* | `RUNTIME` | 标记注解 |
| `@Nonnull` | *（未声明）* | `RUNTIME` | 标记注解；元标注 `@javax.annotation.Nonnull` + `@TypeQualifierNickname` |
| `@Nullable` | *（未声明）* | `RUNTIME` | 标记注解；元标注 `@javax.annotation.Nonnull(when = When.MAYBE)` + `@TypeQualifierNickname` |
| `@ConfigurationProperty` | **仅 `FIELD`** | `RUNTIME` | 见 [§3](#3-configurationproperty--唯一带工具链的注解) |
| `@ThreadSafe`（`concurrent` 包） | `TYPE` | `CLASS` | 标记注解 |
| `@NotThreadSafe`（`concurrent` 包） | `TYPE` | `CLASS` | 标记注解 |

> [!NOTE]
> 有三个细节容易让人意外：
> 1. `@Nonnull` / `@Nullable` / `@Immutable` **没有声明 `@Target`**，因此几乎可以出现在任何位置
>    ——但本模块不做任何强制检查；它们是给读者和识别 JSR-305 别名的静态分析工具看的
>    （这就是模块把 `com.google.code.findbugs:jsr305` 声明为 `optional` 依赖的原因）。
> 2. `@Experimental` 的保留策略是 `SOURCE`：编译后即消失，纯粹是评审信号。
> 3. `@ThreadSafe` / `@NotThreadSafe` 是 `CLASS` 保留，读取字节码的工具能看到，
>    运行期反射看不到。

---

## 2. 为自己的 API 加标注

```java
import io.microsphere.annotation.Experimental;
import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.annotation.Since;
import io.microsphere.annotation.concurrent.NotThreadSafe;

@Since("1.0.0")
@Immutable
public final class Artifact {

    @Nonnull
    public String getArtifactId() { /* ... */ return null; }

    @Nullable
    public String getVersion() { /* ... */ return null; }

    @Experimental(description = "Shape may change before 0.4")
    public boolean matches(Artifact other) { /* ... */ return false; }
}

@NotThreadSafe   // 例如基于非线程安全 StopWatch 构建的性能剖析器
public class MyProfiler { /* ... */ }
```

> [!IMPORTANT]
> 声明的名字是 `@Nonnull` —— `No` 后面的 `n` 是小写，与 JSR-305 的
> `javax.annotation.Nonnull` 一致。写成 `@NonNull`（第二个 N 大写）无法通过编译。

`@Since` 支持模块限定，用于把某个 API 对齐到已发布的模块版本——
例如标注在 `JarUtils.resolveJarAbsolutePath(URL)` 这类方法上：

```java
@Since(value = "1.2.0", module = "microsphere-java-core")
public static String resolveJarAbsolutePath(URL jarURL) { /* ... */ }
```

两个成员都是普通字符串。框架自身的 Javadoc 几乎在每个公开类型上另外写了 `@since 1.0.0`
标签，因此把 `@Since` 当作文档级元数据即可，没有任何构建步骤会校验它。

`@ThreadSafe` / `@NotThreadSafe` 位于 `io.microsphere.annotation.concurrent`，沿用 JCIP 的语义：
`@ThreadSafe` 的类不需要调用方额外同步，`@NotThreadSafe` 的类则相反。它们只能标注在类型上
（`@Target(TYPE)`）。

---

## 3. `@ConfigurationProperty` —— 唯一带工具链的注解

[注解处理器](annotation-processing.md)读取该注解并生成机器可读的元数据，
`io.microsphere.metadata` 在运行期把这些元数据读回来。其全部成员，与源码声明一致：

```java
public @interface ConfigurationProperty {

    String name() default "";

    Class<?> type() default String.class;

    String defaultValue() default "";

    boolean required() default false;

    String description() default "";

    String[] source() default {};

    String SYSTEM_PROPERTIES_SOURCE     = "system-properties";
    String ENVIRONMENT_VARIABLES_SOURCE = "environment-variables";
    String APPLICATION_SOURCE           = "application";
}
```

### 3.1 标注在哪里

`@Target(FIELD)` —— **只能标注字段**，通常标注在保存可调值的 `static final` 常量字段上。
标注在类型上是编译错误。下面是 `microsphere-java-core` 中 `io.microsphere.io.IOUtils`
的真实声明：

```java
/**
 * I/O 缓冲区大小
 */
@ConfigurationProperty(
        name = BUFFER_SIZE_PROPERTY_NAME,
        defaultValue = DEFAULT_BUFFER_SIZE_PROPERTY_VALUE,
        description = "The buffer size for I/O",
        source = SYSTEM_PROPERTIES_SOURCE
)
public static final int BUFFER_SIZE = getInteger(BUFFER_SIZE_PROPERTY_NAME, DEFAULT_BUFFER_SIZE);
```

注意**没有**写的成员：`type`。所有成员的默认值都是"空值"，缺什么由处理器补什么：

| 成员 | 省略时的行为 |
|---|---|
| `name` | 使用字段的**常量值** —— 标注 `public static final String KEY = "a.b.c"` 时无需写 `name` |
| `type` | 使用字段的声明类型（`field.asType()`） |
| `description` | 使用字段的 **Javadoc**（`Elements.getDocComment`） |
| 元数据中的 `declaredClass` / `declaredField` | 始终取自被标注的元素 |
| `source` | 按提供的值输出 —— 未设置则为空集合 |

### 3.2 一个宿主类里的多个键

想暴露哪个字段就标注哪个字段；处理器会访问每个被标注的元素，并为每个字段写出一个 JSON 对象。
`microsphere-java-test` 的 `ConfigurationPropertyModel` 是标准示例 —— 五个被标注字段，
`name` 分别为 `microsphere.annotation.processor.model.<x>`。

---

## 4. 你能得到什么

只要编译时 classpath 上有处理器，使用了 `@ConfigurationProperty` 的模块会在你的 jar 内产出
`META-INF/microsphere/configuration-properties.json`。运行期通过静态 SPI 入口
`io.microsphere.metadata.ConfigurationPropertyLoader.loadAll()` 汇总所有加载器，
得到 `io.microsphere.beans.ConfigurationProperty` 列表。读取和过滤该列表的方式见
[配置属性元数据](configuration-metadata.md)。

---

## 5. 其他同名相近的包

另有两处名字相近的 `io.microsphere.*annotation*` 包，不属于本模块：

* `io.microsphere.test.annotation` —— 在 `microsphere-java-test` 中：`@TestAnnotation`，
  一个覆盖各种注解属性形态的测试夹具（[测试支持](testing.md)）。
* `io.microsphere.annotation.processor` —— 在 `microsphere-annotation-processor` 中：
  处理器的实现（[注解处理](annotation-processing.md)）。

> [!TIP]
> 如果你只需要 `@Since` / `@Immutable` / `@Nonnull` / `@Nullable` / `@Experimental`，
> 引入 `microsphere-java-core` 就够了：它已传递依赖 `microsphere-java-annotations`。
> 只有希望某个模块只依赖注解本身时，才需要直接引入注解构件。

---

## 参见

* [快速开始](getting-started.md) —— 该引入哪个构件。
* [核心工具](core-utilities.md) —— `@ThreadSafe` / `@NotThreadSafe` 声明实际生效的地方。
* [注解处理](annotation-processing.md) —— 把 `@ConfigurationProperty` 变成 JSON 元数据。
* [配置属性元数据](configuration-metadata.md) —— 在运行期读取这些元数据。
* [参考手册](reference.md) —— 这些注解所记录的配置键汇总。

[← 手册目录](../README.md) · [下一篇：核心工具 →](core-utilities.md)
