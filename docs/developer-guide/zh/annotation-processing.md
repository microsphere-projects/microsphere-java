# 注解处理

> 语言版本：[中文](annotation-processing.md) · [English](../en/annotation-processing.md)
> [← 手册目录](../README.md)

## 一览

| 项目 | 取值 |
|---|---|
| 相关模块 | `microsphere-annotation-processor`、`microsphere-lang-model`、`microsphere-jdk-tools` |
| 内置处理器 | `io.microsphere.annotation.processor.ConfigurationPropertyAnnotationProcessor` |
| 生成资源 | `META-INF/microsphere/configuration-properties.json`，写入 `CLASS_OUTPUT` |
| 自动发现 | 处理器 jar 内的服务文件 `META-INF/services/javax.annotation.processing.Processor` |
| 自构建开关 | 处理器模块以 `-proc:none` 编译自身源码 |
| 硬性要求 | 必须是 JDK（而非 JRE）；JDK 16+ 访问 `com.sun.tools.javac` 需要 `--add-opens` |

本页覆盖 `@ConfigurationProperty` 的编译期部分、处理器所依赖的 `javax.lang.model` 辅助层、如何编写并启用
你自己的注解处理器，以及用于测试处理器的进程内 Java 编译器（`microsphere-jdk-tools`）。测试框架本身见
[测试支持](testing.md)。

---

## 1. `ConfigurationPropertyAnnotationProcessor`

`microsphere-annotation-processor/src/main/java/io/microsphere/annotation/processor/`

```java
@SupportedAnnotationTypes(value = CONFIGURATION_PROPERTY_ANNOTATION_CLASS_NAME) // "io.microsphere.annotation.ConfigurationProperty"
public class ConfigurationPropertyAnnotationProcessor extends AbstractProcessor {
    @Override public SourceVersion getSupportedSourceVersion()   // SourceVersion.latestSupported()
    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)  // 返回 false
}
```

用于自动发现的服务文件位于
`microsphere-annotation-processor/src/main/resources/META-INF/services/javax.annotation.processing.Processor`：

```
io.microsphere.annotation.processor.ConfigurationPropertyAnnotationProcessor
```

包级私有的辅助类 `ConfigurationPropertyJSONElementVisitor` 继承自 `microsphere-lang-model` 的
`AnnotatedElementJSONElementVisitor`；写出文件的辅助类是 `FilerProcessor`（对 `Filer` 的安全回调封装）与
`ResourceProcessor`（在指定 `javax.tools.JavaFileManager.Location` 上读写资源）。

---

## 2. 在你的构建中启用

### 2.1 Maven：classpath 自动发现（默认）

由于 jar 中带有 `Processor` 服务文件，javac 会直接从 **编译 classpath** 发现处理器，无需配置编译插件：

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-annotation-processor</artifactId>
    <optional>true</optional> <!-- 仅编译期使用；版本由 BOM 管理 -->
</dependency>
```

### 2.2 Maven：`maven-compiler-plugin` 的 `<annotationProcessorPaths>`

当处理器 **不应** 出现在编译 classpath 上（例如你的处理器依赖一些不想参与编译的库），或编译插件显式配置了
`<proc>` 与处理器路径时使用：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>io.github.microsphere-projects</groupId>
                <artifactId>microsphere-annotation-processor</artifactId>
                <version>0.3.19</version> <!-- annotationProcessorPaths 不读取 dependencyManagement -->
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

> [!NOTE]
> 一旦设置了 `annotationProcessorPaths`，javac **只在该路径** 中查找处理器，classpath 自动发现随即失效。

### 2.3 Gradle

```groovy
dependencies {
    // 让处理器不进入编译与运行 classpath
    annotationProcessor "io.github.microsphere-projects:microsphere-annotation-processor"
}
```

版本通过 BOM（`implementation platform(...)`）解析，见 [快速开始](getting-started.md#32-gradle)。

### 2.4 自己编写处理器模块：`-proc:none`

*包含* 处理器的模块不能让处理器处理自己的源码。`microsphere-annotation-processor` 的 `pom.xml` 正是这样做的：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgument>-proc:none</compilerArgument>
    </configuration>
</plugin>
```

在 Gradle 中，对定义处理器的模块等价写法是
`tasks.withType(JavaCompile) { options.compilerArgs << "-proc:none" }`。

> [!IMPORTANT]
> `microsphere-annotation-processor` 会把 `microsphere-java-core` **shade 进自己的 jar**（maven-shade-plugin，
> `package` 阶段，只包含 `io.github.microsphere-projects:microsphere-java-core`），使处理器自成一体；
> `microsphere-lang-model` 仍是普通依赖。若你同时直接依赖 `microsphere-java-core`，处理器 classpath 层面会出现
> 重复的 `io.microsphere.*` 类——对 javac 无害，但不要把这个处理器 jar 放进运行期 classpath。

---

## 3. 逐轮（round）行为与产出

```java
public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
        writeMetadata();           // 只在最后一轮写出一个文件
    } else {
        resolveMetadata(roundEnv); // 追加条目
    }
    return false;                  // 不认领（claim）注解
}
```

* `resolveMetadata` 用 `ConfigurationPropertyJSONElementVisitor` 访问本轮 **全部根元素**
  （`roundEnv.getRootElements()`，而不是 `getElementsAnnotatedWith`）。遍历根元素意味着被注解的类与同一次编译
  中被其引用的类都会被处理。
* 返回 `false`，其他处理器仍能看到 `@ConfigurationProperty`。
* `writeMetadata` 只在 `processingOver()` 时执行一次：把累积的 `StringBuilder` 重新解析为 `JSONArray`，
  以两空格缩进美化（`jsonArray.toString(2)`），再通过 `ResourceProcessor` 写入 `CLASS_OUTPUT`（即 `target/classes`）。

字段级解析规则（由 visitor 的 `visitVariableAsField` 应用）：

| JSON 键 | 注解属性保持默认值时的取值来源 |
|---|---|
| `name` | 被标注字段的 **常量值**（`field.getConstantValue()`） |
| `type` | `field.asType()`，经 `TypeUtils.getTypeName(...)` 渲染 |
| `description` | `Elements.getDocComment(field)` |
| `metadata.declaredClass` / `declaredField` | 始终由字段及其外部类型推导 |
| `metadata.sources` | 注解的 `source()` 数组 |

注解成员：`name`、`type`（`Class<?>`，默认 `String.class`）、`defaultValue`、`required`、`description`、`source`。

在注解推导出的条目之后，处理器还会追加运行期 SPI 链产出的内容：`ConfigurationPropertyLoader.loadAll()`
（按优先级排序），再由第一个 `ConfigurationPropertyGenerator` 服务（`DefaultConfigurationPropertyGenerator`，
由 `microsphere-java-core` 注册）渲染；该 generator 在 visitor 构造器中解析。

> [!NOTE]
> 后果：生成文件里可能出现你没有标注过的条目——它们来自 **处理器 classpath** 上可见的 loader，包括读取
> `META-INF/microsphere/additional-configuration-properties.json` 的
> `AdditionalMetadataResourceConfigurationPropertyLoader`。与处理器一起注册你自己的
> `ConfigurationPropertyLoader`（本模块的测试就通过
> `src/test/resources/META-INF/services/io.microsphere.metadata.ConfigurationPropertyLoader` 注册了
> `io.microsphere.annotation.processor.TestConfigurationPropertyLoader`）是被支持的注入方式，用于表达无法写成
> 注解字段的属性。

输出路径：`META-INF/microsphere/configuration-properties.json`
（`ResourceConstants.CONFIGURATION_PROPERTY_METADATA_RESOURCE`）。JSON 结构见
[配置属性元数据](configuration-metadata.md)。

---

## 4. `microsphere-lang-model`——`javax.lang.model.*` 辅助层

`io.microsphere.lang.model.element` 与 `io.microsphere.lang.model.util` 两个包共 16 个类型。其中 10 个是
`interface`，它们 `extends io.microsphere.util.Utils` 并暴露 **静态接口方法**（Java 8+ 允许），以静态导入方式
调用——没有 `INSTANCE`，也没有工厂方法：

```java
import static io.microsphere.lang.model.util.AnnotationUtils.getAnnotation;
import static io.microsphere.lang.model.util.ElementUtils.matchesElementType;
```

> [!NOTE]
> 不要与 `io.microsphere.reflect.TypeUtils`（运行期 `java.lang.reflect.Type`）或 JDK 自带的
> `javax.lang.model.util.Types` 混淆。`lang.model` 系列操作的是编译期镜像。

| 类型 | 种类 | 用途 |
|---|---|---|
| `element.StringAnnotationValue` | `class implements AnnotationValue` | 在真实 `AnnotationMirror` 之外提供一个 `String` 注解值 |
| `util.ElementUtils` | interface | `ElementKind`/`ElementType` 断言、元素过滤、`matchParameterTypes(...)` |
| `util.AnnotationUtils` | interface | 查找与读取 `AnnotationMirror`：`getAnnotation`、`findMetaAnnotation`、`getAttributesMap`、`getAttribute(mirror, name, boolean withDefault)`、`getValue`、`getElementTypes`、`matchesDefaultAttributeValue` |
| `util.TypeUtils` | interface | 覆盖 `TypeMirror`/`DeclaredType`/`TypeElement` 的大规模静态方法集，含 `getTypeName`、`getTypeMirror(processingEnv, Type)`、`ofTypeElement` |
| `util.ClassUtils` | interface | `getClassName(TypeMirror)`、`loadClass(TypeMirror)`、`loadClass(String)` |
| `util.MethodUtils` / `FieldUtils` / `ConstructorUtils` / `MemberUtils` | interface | 成员元素查询（`findMethod`、`findField`、`findConstructor`） |
| `util.MessagerUtils` | interface | `printNote` / `printWarning` / `printMandatoryWarning` / `printError` / `printMessage`，各有 `(ProcessingEnvironment, ...)` 与 `(Messager, ...)` 两种形式——同时输出到 javac 与日志，占位符为 `{}` |
| `util.LoggerUtils` | interface | `trace/debug/info/warn/error(String format, Object... args)` |
| `util.ExecutableElementComparator` | `Comparator<ExecutableElement>` | `public static final INSTANCE`；按简单名、参数个数、参数类型名排序 |
| `util.JSONElementVisitor` | `abstract class extends ElementKindVisitor6<Boolean, StringBuilder>` | AST→JSON visitor 基类 |
| `util.AnnotatedElementJSONElementVisitor` | `abstract class extends JSONElementVisitor` | 同上，限定于标注某一种注解的元素 |
| `util.JSONAnnotationValueVisitor` | `SimpleAnnotationValueVisitor6` 子类 | 把注解值渲染为 JSON |
| `util.ResolvableAnnotationValueVisitor` | `SimpleAnnotationValueVisitor6` 子类 | 把注解值解析为 Java 对象 |

---

## 5. 编写你自己的处理器

### 5.1 Visitor：继承 `AnnotatedElementJSONElementVisitor`

`JSONElementVisitor` 把 `visitPackage/visitVariable/visitExecutable/visitType/visitTypeParameter` 全部声明为
**final**：模板负责遍历语法树，通过 `supports`/`supportsPackage`/`supportsVariable`/`supportsExecutable`/
`supportsType`/`supportsTypeParameter` 判定成员资格，再分派到你的 `doVisit*` 钩子（类型元素还有
`visitMembers(List<? extends Element>, StringBuilder)`）。因此你只覆写 `doVisit*` 与 `supports*`，绝不覆写 `visit*`。

```java
public class MyElementJSONVisitor extends AnnotatedElementJSONElementVisitor {

    public MyElementJSONVisitor(ProcessingEnvironment processingEnv) {
        super(processingEnv, "com.example.MyAnnotation");
    }

    @Override
    protected boolean doVisitType(TypeElement e, StringBuilder jsonBuilder) {
        // e.getEnclosedElements()、getAnnotation(...)、getAttributesMap(...) 等
        return true;
    }
}
```

`AnnotatedElementJSONElementVisitor` 暴露 `protected final` 字段 `processingEnv`、`elements`、
`annotationClassName`、`annotationType`、`annotationTypeElement`、`elementTypes`，其 `supports(Element)` 即
`matchesElementType(element, elementTypes)`——回答“这个注解允许出现在这里吗”。
`ConfigurationPropertyJSONElementVisitor` 正是把这一模式绑定到 `io.microsphere.annotation.ConfigurationProperty`，
并覆写了 `visitVariableAsField` 与 `supportsType`。

### 5.2 处理器本体

```java
@SupportedAnnotationTypes("com.example.MyAnnotation")
public class MyAnnotationProcessor extends AbstractProcessor {

    @Override public SourceVersion getSupportedSourceVersion() { return SourceVersion.latestSupported(); }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        StringBuilder json = new StringBuilder("[");
        roundEnv.getRootElements().forEach(root -> root.accept(new MyElementJSONVisitor(processingEnv), json));
        // 通过 new ResourceProcessor(processingEnv, StandardLocation.CLASS_OUTPUT) 写出 json
        return false; // true = 认领注解，后续处理器将看不到它
    }
}
```

注册方式：在 `src/main/resources/META-INF/services/javax.annotation.processing.Processor` 中写入
`com.example.MyAnnotationProcessor`，把 jar 放到使用方的 classpath（或 `annotationProcessorPaths` / Gradle 的
`annotationProcessor` 配置）中，并且处理器模块自身以 `-proc:none` 编译（见 §2.4）。

> [!WARNING]
> **JDK 16+ 与 `com.sun.tools.javac`。** 公共的 `javax.tools` / `javax.annotation.processing` API 不需要任何
> 特殊参数，但反射访问 javac 内部就不同：内置的 `FilerProcessor.getJavaFileManager()` 会读取 javac 内部类
> `JavacFiler` 的私有字段 `fileManager`；把 `ProcessingEnvironment` 强转为 `JavacProcessingEnvironment` 同样触碰
> 模块私有包。JDK 16+ 起 `jdk.compiler` 默认不再开放这些包，因此在这类 JDK 上运行处理器（或其框架测试）需要给
> *被分叉的编译器 / Surefire JVM* 加上类似
> `--add-opens jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED`
> 的参数（本仓库自身的 `java16+` 构建 profile 已为编译器分叉与 Surefire 开放了 `java.base/java.lang` 和
> `java.base/java.lang.invoke`——见 [快速开始](getting-started.md#61-jdk-16-的注意事项)）。优先使用
> `javax.tools` API，把内部访问做成可选且受保护的。

---

## 6. `microsphere-jdk-tools`——在 JVM 进程内编译

`io.microsphere.jdk.tools.compiler.Compiler` 包装了 `javax.tools.JavaCompiler`，用于进程内编译，处理器测试
正是这样运行的。链式 set 方法各自返回 `this`：

```java
public class Compiler {

    public static final String[] DEFAULT_OPTIONS = {
            "-parameters", "-Xlint:-unchecked", "-nowarn", "-Xlint:deprecation" };

    public Compiler()                                       // target = <Compiler.class 所在根目录>/target/generated-classes
    public Compiler(File targetDirectory)
    public Compiler(File defaultSourceDirectory, File targetDirectory)

    public Compiler options(String... options)              // 是「整体替换」而非追加
    public Compiler sourcePaths(File... sourcePaths)        // 追加
    public Compiler sourcePaths(Class<?>... sourceClasses)  // 每个类追加一个源码路径
    public Compiler sourcePath(Class<?> sourceClass)
    public Compiler processors(Processor... processors)     // 整体替换处理器集合
    public Compiler diagnosticListener(DiagnosticListener<? super JavaFileObject> diagnosticListener)
    public Compiler locale(Locale locale)
    public Compiler charset(Charset charset)

    public boolean compile(Class<?>... sourceClasses) throws IOException

    public static File detectSourcePath(Class<?> sourceClass)
    public static File detectRootDirectory(Class<?> sourceClass)
    public static File detectClassPath(Class<?> sourceClass)
    public static String resolveJavaSourceFileRelativePath(Class<?> sourceClass)
    // 另有 getter：getJavaCompiler、getJavaFileManager、getDiagnosticListener、getLocale、
    // getCharset、getOptions（不可修改）、getProcessors（不可修改）
}
```

```java
Compiler compiler = new Compiler()
        .options("-parameters", "-proc:only")
        .sourcePaths(MyConfiguration.class)
        .processors(new ConfigurationPropertyAnnotationProcessor())
        .diagnosticListener(diagnostic -> System.out.println(diagnostic.getMessage(Locale.getDefault())))
        .charset(StandardCharsets.UTF_8);

boolean success = compiler.compile(MyConfiguration.class);
```

必须遵守的约束：

* 需要 **JDK**，不是 JRE：构造器断言 `ToolProvider.getSystemJavaCompiler()` 非空，
  失败信息为 *"No Java compiler available. Ensure this process is running on a JDK (not just a JRE)."*
* 源码定位基于 **文件系统**：`detectRootDirectory(Class)` 从类的代码位置向上走
  （Maven 布局的 `.../target/classes` → 模块根目录），在 `src/main/java` / `src/test/java` 下找源码。
  从 jar 加载的类无法定位——`detectSourcePath(Test.class)` 返回 `null`（`CompilerTest` 有断言）。
* 对没有 `CodeSource` 的类（如 `java.lang.String` 这类 bootstrap 类），`detectClassPath(Class)` 抛
  `UnsupportedOperationException`。
* 默认目标目录与默认源码目录都取自 **`Compiler.class` 自身** 的位置（`<root>/target/generated-classes`，
  构造时用 `mkdirs()` 创建），而不是被编译类的位置。希望产物落在自己模块下时，请使用
  `Compiler(File targetDirectory)`。
* `options(...)` 会 **整体替换** 选项列表（构造器先用 `DEFAULT_OPTIONS` 填充）；要关闭处理请显式传
  `-proc:none`。

---

## 7. 测试你的处理器

两种方式，按保真度递增：

1. **直接驱动**：自己使用 `Compiler`——构造它、加入处理器、编译带你自己注解的类，然后断言生成的资源。
   适合验证“文件是否生成”。
2. **测试框架**：继承 `microsphere-annotation-test` 的 `AbstractAnnotationProcessingTest`；它在每个测试方法前
   编译测试类本身，并注入真实的 `ProcessingEnvironment`/`Elements`/`Types`/`RoundEnvironment`——完整契约、
   可运行示例与注意事项见 [测试支持](testing.md)。

要让框架看到你的处理器，把它注册到
`src/test/resources/META-INF/services/javax.annotation.processing.Processor`——框架会把测试 classpath 上
`ServiceLoader` 找到的每个 `Processor` 都加入编译任务。

---

## 参见

* [测试支持](testing.md) —— JUnit 5 注解处理测试框架与测试夹具
* [配置属性元数据](configuration-metadata.md) —— 运行期的 reader/loader/generator 链
* [注解](annotations.md) —— 被处理的 `@ConfigurationProperty` 契约
* [快速开始](getting-started.md) —— BOM 导入、JDK 矩阵与构建 profile
* [参考手册](reference.md) —— SPI 服务文件清单

[← 手册目录](../README.md) · [上一篇：配置属性元数据](configuration-metadata.md) · [下一篇：测试支持 →](testing.md)
