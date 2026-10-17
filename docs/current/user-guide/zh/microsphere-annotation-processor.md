# 注解处理器（Annotation Processor）

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-annotation-processor`（`0.3.20-SNAPSHOT`，`release` 分支） |
| **主源码** | `io.microsphere.annotation.processor` 包下 4 个类型（`src/main/java/io/microsphere/annotation/processor/`） |
| **测试** | `ConfigurationPropertyAnnotationProcessorTest`、`ConfigurationPropertyJSONElementVisitorTest`、`FilerProcessorTest`、`ResourceProcessorTest` 以及 `TestConfigurationPropertyLoader` SPI 夹具 |
| **编译依赖** | `microsphere-java-core`（`pom.xml:24-28`，artifactId 在 `:26`）、`microsphere-lang-model`（`pom.xml:31-35`，artifactId 在 `:33`） |
| **测试依赖** | `microsphere-annotation-test`（`:51-56`，artifactId `:53`，scope `test` 在 `:55`）、`junit-jupiter`（`:38-42`）、`junit-jupiter-engine`（`:44-48`）、`logback-classic`（`:59-63`）、`javax.ws.rs-api`（`:66-70`）、`jaxws-api`（`:72-77`）、`spring-context`（`:80-84`）、`spring-web`（`:86-90`） |
| **Processor SPI 文件** | `src/main/resources/META-INF/services/javax.annotation.processing.Processor:1` |
| **打包特性** | 本模块执行 `maven-shade-plugin`（`pom.xml:105`），并把 `microsphere-java-core` 嵌入自身构件（`pom.xml:115`） |
| **JDK 级别** | 字节码级别继承自外部父 POM `microsphere-build:0.3.16`；CI 在 JDK 8 / 11 / 17 / 21 / 25 上验证 |

`microsphere-annotation-processor` 是 Microsphere 配置元数据特性的**编译期那一半**。它把每一个被
`io.microsphere.annotation.ConfigurationProperty` 标注的字段转换成一份 JSON 文档——
`META-INF/microsphere/configuration-properties.json`——并与你的类文件一起打进 jar。IDE 依靠这份文档提供属性名补全和
文档浮窗，运行时包 `io.microsphere.metadata` 则把它读回成 `ConfigurationProperty` 对象。运行时那一半（reader、loader
与 generator）的记录见[配置属性元数据](../microsphere-java-core/zh/configuration-metadata.md)；本页只讲 `javac` 内部
发生了什么。

| 半边 | 模块 | 方向 | 记录位置 |
|---|---|---|---|
| 编译期 | `microsphere-annotation-processor` | **写出** `META-INF/microsphere/configuration-properties.json` | 本页 |
| 运行时 | `microsphere-java-core` · `io.microsphere.metadata` | **读取**它，并把 `ConfigurationPropertyLoader` 的结果回灌进去 | [配置属性元数据](../microsphere-java-core/zh/configuration-metadata.md) |

---

## 1. 这个模块做什么

只发布一个处理器：`ConfigurationPropertyAnnotationProcessor`
（`annotation/processor/ConfigurationPropertyAnnotationProcessor.java:78`），它通过标准 Java EE 处理器 SPI 注册，
因此 `javac` 能在处理器路径上自动发现它
（`src/main/resources/META-INF/services/javax.annotation.processing.Processor:1`）。它的全部工作分四步：

1. 对每一个仍有工作的编译轮次，遍历该轮次的根元素
   （`ConfigurationPropertyAnnotationProcessor.java:107-110`），交给访问器
   （`annotation/processor/ConfigurationPropertyJSONElementVisitor.java:57`）为每个被标注的字段收集一个 JSON 对象，
   累加进同一个 `StringBuilder`。
2. 追加编译期 classpath 上所有已注册 `ConfigurationPropertyLoader` 贡献的属性
   （`ConfigurationPropertyAnnotationProcessor.java:134-140`）。
3. 在最后一轮，把累加文本解析成 `JSONArray`，以两空格缩进美化输出，并作为 class 输出资源写入
   （`ConfigurationPropertyAnnotationProcessor.java:149-156`）。
4. 把完整的生成文档以编译器 `NOTE` 的形式打印到构建日志，不开 jar 也能看到
   （`ConfigurationPropertyAnnotationProcessor.java:154`）。

输出文件归机器所有；手写的伴随文件是
`META-INF/microsphere/additional-configuration-properties.json`
（`microsphere-java-core/src/main/java/io/microsphere/constants/ResourceConstants.java:60`）。`microsphere-java-core`
当前发布的那 7 个条目正是本处理器的产物——见第 4 节。

> [!NOTE]
> 该处理器*不是*代码生成器：它从不产生 `.java` 文件，只产生一个 JSON 资源。模块里其余的东西
> （`FilerProcessor`、`ResourceProcessor`）只是为了让 `javac` 内部的资源 I/O 更安全。

---

## 2. 坐标与启用方式

GroupId 为 `io.github.microsphere-projects`（`pom.xml:12`），版本 `${revision}` = `0.3.20-SNAPSHOT`（`pom.xml:54`），
标签 `0.3.20` 已发布。有两种开启方式。

### 2.1 普通依赖——通过 service 文件自动发现

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-annotations</artifactId>
    <version>0.3.20</version>
</dependency>

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-annotation-processor</artifactId>
    <version>0.3.20</version>
    <scope>provided</scope>
</dependency>
```

`javac` 会扫描编译 classpath 上的 `META-INF/services/javax.annotation.processing.Processor`
并实例化其中列出的每个类，所以不需要任何编译器配置。这条路径有两个后果：处理器及其传递依赖会污染你的**编译**
classpath（因此建议 `provided`，让它们不进入运行时 classpath）；另外较新的 JDK 在通过 classpath 隐式启用注解处理时
会打印一条警告——用下面的显式 `annotationProcessorPaths` 可以消除它，或者在不需要注解处理的地方使用 `-proc:none`。

`microsphere-java-annotations` 依赖**不是可选项**：访问器会通过
`Elements.getTypeElement("io.microsphere.annotation.ConfigurationProperty")` 解析注解类型，
解析结果为 `null` 时立即失败
（`microsphere-lang-model/src/main/java/io/microsphere/lang/model/util/AnnotatedElementJSONElementVisitor.java:109-110`）。

### 2.2 显式 `annotationProcessorPaths`

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>io.github.microsphere-projects</groupId>
                <artifactId>microsphere-annotation-processor</artifactId>
                <version>0.3.20</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

| | `<annotationProcessorPaths>` | `<compilerArgument>` / `-processor` |
|---|---|---|
| 处理器 classpath | 与编译 classpath 隔离 | 与编译 classpath 共享 |
| 编译 classpath 上还需要注解 API 吗 | 需要，`provided` 即可 | 需要 |
| 是否要列出处理器自己的传递依赖 | 要，`annotationProcessorPaths` 会独立解析依赖树——但这里 `microsphere-java-core` 已被 **shade** 进处理器 jar（`pom.xml:105-121`），所以一个 `<path>` 条目就够 | 不需要 |
| 显式指定单个处理器 | 不必 | `-processor io.microsphere.annotation.processor.ConfigurationPropertyAnnotationProcessor` 可在存在其他处理器时强制启用它 |
| 可重现性 | 版本独立于项目依赖管理被固定 | 取决于编译 classpath 解析出的版本 |
| JDK 8 | 可用；`javax.annotation.processing` 在 `rt.jar`，`javac` 本体在 `tools.jar` | 相同 |

`<compilerArgument>` 是只容一个值的旧写法；需要多个参数时应改用
`<compilerArgs><arg>…</arg></compilerArgs>`；另外记住，有效配置中任何地方出现 `<proc>none</proc>`（或 `-proc:none`）
都会**彻底关掉**处理器，即使依赖已经存在。

### 2.3 本仓库自己的构建怎么做

两种都不是。把 reactor 里所有 `pom.xml` 翻一遍，找不到任何 `<annotationProcessorPaths>`；唯一的
`maven-compiler-plugin` 配置块就在本模块里：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgument>-proc:none</compilerArgument>
    </configuration>
</plugin>
```

_来源：`microsphere-annotation-processor/pom.xml:96-102`（`-proc:none` 在 `:100`）_

这是自举特例，不是可以照抄的范例：该模块编译的就是处理器本身，若在自己的源码上运行它就成了循环。reactor
其余模块只是因为依赖缺席而等效于关闭了注解处理——这正是 `microsphere-java-core` 的
`META-INF/microsphere/configuration-properties.json` 是一份**签入仓库、手工重新生成**的文件，而不是构建产物的原因。
测试夹具（第 7 节）是本仓库里处理器真正运行的唯一地方。

---

## 3. 版本信息与兼容范围

| | |
|---|---|
| **GroupId** | `io.github.microsphere-projects`（`pom.xml:12`） |
| **reactor 版本** | `${revision}` = `0.3.20-SNAPSHOT`（`pom.xml:54`）；标签 `0.3.20` 已发布 |
| **BOM** | `microsphere-java-parent/pom.xml` 导入 `microsphere-all-bom:0.3.13`（`:22`、`:31-37`） |
| **Spring** | 导入 `spring-framework-bom`（`microsphere-java-parent/pom.xml:40-46`）；默认 `7.0.9`（`:23`），`java8-16` profile 由 `<jdk>[1.8,17)</jdk>` 激活（`:54-62`，激活条件 `:57`，固定 `:60`）时为 `5.3.39`——仅测试作用域（`pom.xml:80-90`） |
| **JDK 矩阵** | Temurin 8、11、17、21、25（`.github/workflows/maven-build.yml:25`，`distribution: 'temurin'` 在 `:33`） |
| **`@since`** | 4 个主类型全部是 `1.0.0`（`ConfigurationPropertyAnnotationProcessor.java:75`、`ConfigurationPropertyJSONElementVisitor.java:55`、`FilerProcessor.java:74`、`ResourceProcessor.java:82`） |

支持的源码版本是*开放式*的：`getSupportedSourceVersion()` 返回 `latestSupported()`
（`ConfigurationPropertyAnnotationProcessor.java:162-165`），即在任意 JDK 上都声明该 JDK 提供的最新源码级别。
支持的注解类型来自类级别的 `@SupportedAnnotationTypes(CONFIGURATION_PROPERTY_ANNOTATION_CLASS_NAME)`
（`ConfigurationPropertyAnnotationProcessor.java:77`），其值是字符串
`"io.microsphere.annotation.ConfigurationProperty"`（`ConfigurationPropertyJSONElementVisitor.java:59`）。
支持的选项：没有——`getSupportedOptions()` **未被覆写**，因此 `AbstractProcessor` 的默认实现会去读取并不存在的
`@SupportedOptions` 注解并返回空集合；本处理器不识别任何 `-A` 参数。

> [!NOTE]
> 整个 reactor 中**不存在** `maven.compiler.*` 或 `<release>` 属性，字节码级别来自外部父 POM
> `io.github.microsphere-projects:microsphere-build:0.3.16`。这里存在一个值得知道的张力：*构件*是 Java 8 字节码、可在
> JDK 8+ 上加载，而*处理器*却声明 `latestSupported()`，于是在 JDK 25 上它会乐意处理 Java 25 源码。凡是在所声明的源码级别
> 与 Java 6 时代的 `ElementKindVisitor6` 基类
> （`microsphere-lang-model/src/main/java/io/microsphere/lang/model/util/JSONElementVisitor.java:57`）上都成立的东西都可以用；
> 新增的元素种类会被静默跳过（见开发者指南）。

---

## 4. 生成了什么

### 4.1 生成到哪里

只有一个文件、一个位置，永远如此：

| | |
|---|---|
| 资源名 | `META-INF/microsphere/configuration-properties.json`（`microsphere-java-core/src/main/java/io/microsphere/constants/ResourceConstants.java:54`，文件名 `:43`） |
| 写入位置 | `StandardLocation.CLASS_OUTPUT`（`ConfigurationPropertyAnnotationProcessor.java:46`、`:93`） |
| 效果 | 落到 `target/classes`，因此进入你的 jar，与 `.class` 文件并列 |
| 格式化 | `new JSONArray(text).toString(2)` —— 两空格缩进美化（`ConfigurationPropertyAnnotationProcessor.java:151-152`） |

无法改名或换位置：常量硬编码在 `ConfigurationPropertyAnnotationProcessor.java:39` 的静态导入里，并在 `:150`
直接传给写入器。

### 4.2 JSON 的形状

下面是 `microsphere-java-core` 中已生成文件的真实片段（全部 7 个条目都由本处理器产生）：

```json
[
  {
    "name": "microsphere.io.buffer.size",
    "type": "int",
    "defaultValue": "2048",
    "required": false,
    "description": "The buffer size for I/O",
    "metadata": {
      "sources": [
        "system-properties"
      ],
      "declaredClass": "io.microsphere.io.IOUtils",
      "declaredField": "BUFFER_SIZE"
    }
  }
]
```

_来源：`microsphere-java-core/src/main/resources/META-INF/microsphere/configuration-properties.json:30-43`_

| 键 | 何时装出 | 证据 |
|---|---|---|
| `name` | 总是 | `microsphere-java-core/src/main/java/io/microsphere/metadata/DefaultConfigurationPropertyGenerator.java:88` |
| `type` | 总是 | `:90-92` |
| `value` | 仅当非 `null`——而处理器从不设置它，因此生成的文件里不会出现 | `:94-98`；`ConfigurationPropertyJSONElementVisitor.java:73-99` 中没有 `setValue(...)` 调用 |
| `defaultValue` | 仅当注解显式给出 | `:100-104` + `ConfigurationPropertyJSONElementVisitor.java:85-87`、`:138-141` |
| `required` | 总是 | `:106-107` + `ConfigurationPropertyJSONElementVisitor.java:88-90` |
| `description` | 仅当非 `null`，包含从 Javadoc 取到的值 | `:109-113` + `ConfigurationPropertyJSONElementVisitor.java:130-136` |
| `metadata` | 总是 | `:115-117` |
| `metadata.sources` / `metadata.targets` | 仅当非空 | `:127-137` |
| `metadata.declaredClass` / `metadata.declaredField` | 总是，两者都取自字段 | `:139-149` + `ConfigurationPropertyJSONElementVisitor.java:98-99` |

已生成文件的最后一个条目对 IDE 作者最有启发：由于 `description` 会回落到原始 Javadoc
（`ConfigurationPropertyJSONElementVisitor.java:133`），`microsphere.reflect.banned-methods` 把整段
`<h3>`/`<pre>{@code …}` 带进了 JSON（`…/configuration-properties.json:90`），其中 `/` 被转义成 `\/`。

### 4.3 来自 loader 而非注解的属性

在写文件之前，处理器会调用 `ConfigurationPropertyLoader.loadAll()`
（`ConfigurationPropertyAnnotationProcessor.java:135`，静态导入在 `:44`），并把返回的每个属性交给访问器使用的同一个
`ConfigurationPropertyGenerator` 渲染（`:142-147`）。这就是扩展点：一个模块可以发布
`META-INF/services/io.microsphere.metadata.ConfigurationPropertyLoader`，让它的属性在编译期被合进生成文件。
本模块在测试作用域里就是这么做的：

```
microsphere-annotation-processor/src/test/resources/META-INF/services/io.microsphere.metadata.ConfigurationPropertyLoader
io.microsphere.annotation.processor.TestConfigurationPropertyLoader
```

对应的 loader 为每个 JDK 简单类型生成一个属性，包含随机的 `sources`/`targets`
（`io/microsphere/annotation/processor/TestConfigurationPropertyLoader.java:41-71`）。

> [!IMPORTANT]
> loader 贡献的属性是在 `if (!elements.isEmpty())` 代码块**内部**追加的
> （`ConfigurationPropertyAnnotationProcessor.java:116`，调用点 `:123`）。因此一个没有值得访问的根元素的模块只会产出
> `[]`，并静默丢弃你的 loader 本该贡献的全部内容。注解与 loader 必须处在同一个编译单元里才会合并。

---

## 5. 哪些被标注的元素会被访问

`@ConfigurationProperty` 声明了 `@Target(FIELD)` 与 `@Retention(RUNTIME)`
（`microsphere-java-annotations/src/main/java/io/microsphere/annotation/ConfigurationProperty.java:36-38`），因此候选元素
只有字段。在此之内，遍历由三层规则决定：

| 层次 | 规则 | 引用 |
|---|---|---|
| 轮次选择 | 该轮次的**每个根元素**都会交给访问器——而不是 `RoundEnvironment.getElementsAnnotatedWith(...)` | `ConfigurationPropertyAnnotationProcessor.java:108`、`:117-121` |
| 种类过滤 | `AnnotatedElementJSONElementVisitor.supports(Element)` 把元素种类与注解声明的 `@Target` 集合比对 | `microsphere-lang-model/.../AnnotatedElementJSONElementVisitor.java:111`、`:124-126` |
| 类型入口 | `supportsType` 被覆写为 `true`，因此类型总会被深入遍历 | `ConfigurationPropertyJSONElementVisitor.java:109-112` |
| 成员遍历 | `visitType` 先调用 `super.visitType`，随后遍历 `e.getEnclosedElements()` | `microsphere-lang-model/.../JSONElementVisitor.java:87-101`、`:111-119` |
| 字段载荷 | 仅当 `@ConfigurationProperty` 的镜像存在时，`visitVariableAsField` 才产出 JSON | `ConfigurationPropertyJSONElementVisitor.java:70-72`、`:106` |

对这些规则的实际解读：

* **只有声明的成员会被计入。** `getEnclosedElements()` 不包含继承来的字段，因此编译子类时父类上被标注的字段不会被写出
  （陷阱 6）。
* **局部变量、参数、枚举常量、record 组件都被忽略**——它们进入 `visitVariable`，但永远走不到被覆写的 `FIELD` 分支
  `visitVariableAsField`（`microsphere-lang-model/.../JSONElementVisitor.java:71-77`）。
* **依据是注解镜像，而不是编译期注解对象**：每个属性都是从 `AnnotationMirror` 按声明顺序读取的
  （`ConfigurationPropertyJSONElementVisitor.java:71`、`:74-97`），因此不需要反射被标注的类，那个类也不必可加载。
* **“未指定”意味着“推导”，不是“省略”**：缺省的 `name` 取字段的*常量值*——这就是
  `public static final String BUFFER_SIZE = "microsphere.io.buffer.size"` 无需写 `name` 的原因
  （`ConfigurationPropertyJSONElementVisitor.java:119-122`）；缺省的 `type` 取字段声明类型（`:124-128`）；缺省的
  `description` 取其 Javadoc（`:130-136`）。
* **`source` 是唯一数组类型的属性**，也是兜底分支（`:94-96`、`:143-152`）；三个常量 `system-properties`、
  `environment-variables`、`application`（`ConfigurationProperty.java:43`、`:48`、`:53`）就是你在 `metadata.sources`
  里会看到的值。

---

## 6. `FilerProcessor` 与 `ResourceProcessor`

两个 helper 把 `javax.annotation.processing.Filer` 的行为隔离起来，而它们的失败方式不同——这关系到陷阱 2 与陷阱 8。

| 类型 | 可见性 | 职责 | 失败行为 |
|---|---|---|---|
| `FilerProcessor`（`annotation/processor/FilerProcessor.java:76`） | `public` | 从 `ProcessingEnvironment` 取得 `Filer` 并对其执行回调（`:92-96`）；额外暴露底层 `JavaFileManager`（`:103-106`） | 默认处理器打印**mandatory warning** 并返回 `null`（`:84-90`） |
| `ResourceProcessor`（`annotation/processor/ResourceProcessor.java:84`） | `public` | 绑定一个 `Location` 与 `moduleAndPackage`（`:90-94`），按读写方向 + 名字缓存 `FileObject`（`:98`、`:123-130`），存在性检查（`:136-142`、`:254-256`），并提供 `InputStream`/`Reader`/`CharSequence`（`:156-213`）与 `OutputStream`/`Writer`（`:215-252`）的类型化访问 | 默认处理器告警**并且**重新抛出为 unchecked 异常（`:112-118`、`:235-240`） |

处理器自身只用写路径：`processInResourceWriter(CONFIGURATION_PROPERTY_METADATA_RESOURCE, …)`，Location 为 `CLASS_OUTPUT`
（`ConfigurationPropertyAnnotationProcessor.java:150`、`:93`）。`FilerProcessor` 的 `getJavaFileManager()`
通过反射读取 `Filer` 实现的私有字段 `fileManager`（`FilerProcessor.java:105`）——这是本模块唯一把 `javac`
实现细节写进代码的 API。

`ResourceProcessor` 用两个包私有标志区分读写，`FOR_READING = false` 与 `FOR_WRITING = true`
（`ResourceProcessor.java:86`、`:88`），并且只有当 `FileObject.getLastModified() > 0` 时资源才算存在（`:254-256`）。

---

## 7. 在自己的构建中验证输出

三个层次，由廉价到昂贵。

1. **看构建日志。** 最后一轮处理器会输出一条 `NOTE`，内容就是*完整的*生成文档，包含 loader 贡献的条目
   （`ConfigurationPropertyAnnotationProcessor.java:154`）。`javac` 默认打印 note，
   Maven 会连同该模块的编译器输出一起显示。
2. **打开产物。** `META-INF/microsphere/configuration-properties.json` 属于 class 输出，因此在你的 jar 里：
   `unzip -p target/your.jar META-INF/microsphere/configuration-properties.json`。
   如果这个条目不存在，说明处理器*没有运行*（陷阱 1），而不是运行了却没找到东西——
   即便结果为空也会被写成 `[]`（`ConfigurationPropertyAnnotationProcessor.java:113`、`:126-131`）。
3. **在进程内运行它。** 四个测试类都继承 `AbstractAnnotationProcessingTest`
   （`io/microsphere/annotation/processor/ConfigurationPropertyAnnotationProcessorTest.java:34`），后者通过
   JUnit 5 的 `@ExtendWith(CompilerInvocationInterceptor.class)` 挂载
   （`microsphere-annotation-test/src/main/java/io/microsphere/test/annotation/processing/AbstractAnnotationProcessingTest.java:53`）。
   拦截器会先用进程内的 `io.microsphere.jdk.tools.compiler.Compiler` 编译测试类加上 `addCompiledClasses` 提供的类
   （`AbstractAnnotationProcessingTest.java:133-134`），注册一个测试驱动处理器，然后**追加测试 classpath 上发现的所有
   `Processor`**（`microsphere-annotation-test/src/main/java/io/microsphere/test/annotation/processing/CompilerInvocationInterceptor.java:54-63`）——
   所以本模块的处理器会被任何使用该夹具的测试自动跑一遍。JDK 8 上它驱动的那些类位于 `tools.jar`，这正是需要
   `microsphere-jdk-tools` 作为包装层的原因。

各测试套件钉住了周边契约：`ResourceProcessorTest` 通过 `SOURCE_PATH` 读回自己的源文件
（`io/microsphere/annotation/processor/ResourceProcessorTest.java:56`、`:66-68`、`:92-138`），
写入并删除随机 class-output 资源（`:151-189`，清理在 `:71-74`），
并用 `assertThrows(RuntimeException.class, …)` 断言重抛行为（`:82-90`）；
`FilerProcessorTest` 断言返回 `null` 的降级行为
（`io/microsphere/annotation/processor/FilerProcessorTest.java:58-62`）。

> [!WARNING]
> 仓库里**没有任何测试断言生成文件的内容**。唯一涉及内容的测试构造了一个空元素集合，然后对结果字符串调用
> `assertNotNull`（`ConfigurationPropertyAnnotationProcessorTest.java:37-43`）——它只是非空检查，并不是与 `[]` 的比较。
> 请把 4.2 节的形状（取自 `microsphere-java-core` 签入的产物）当作规格说明。

---

## 8. 常见陷阱

1. **处理器压根没运行。** `target/classes` 里没有 `META-INF/microsphere/configuration-properties.json`，说明处理器不在
   *处理器*路径上：依赖缺失；依赖虽是 `provided` 但模块同时配置了 `<annotationProcessorPaths>`（它会彻底取代 classpath
   发现机制）；或者 `-proc:none` 生效了——注意本模块自己的构建用的正是它（`pom.xml:100`），而共享父 POM 里的
   `<proc>none</proc>` 会向下传播。下游没有任何报错：运行时 loader 把缺失资源当作“无事可做”，见
   [配置属性元数据——常见陷阱](../microsphere-java-core/zh/configuration-metadata.md)。
2. **编译 classpath 上缺少 `microsphere-java-annotations`。** 访问器的父类会立刻解析注解类型并在失败时抛出
   （`AnnotatedElementJSONElementVisitor.java:110`）；由于这发生在 `init()` 里
   （`ConfigurationPropertyAnnotationProcessor.java:94`），表现为“processor failed to initialize”并让构建失败。
3. **编译期必须能发现 `ConfigurationPropertyGenerator`。** 访问器构造函数调用
   `loadFirstService(ConfigurationPropertyGenerator.class)`（`ConfigurationPropertyJSONElementVisitor.java:65`），而该
   API 是 `@Nonnull`，在没有声明任何实现时抛出 `IllegalArgumentException`
   （`microsphere-java-core/src/main/java/io/microsphere/util/ServiceLoaderUtils.java:599-602`）。这是本模块最响亮的失败：
   service 文件被剥掉意味着编译中断，而不是元数据降级。
4. **增量构建与陈旧资源。** `ResourceProcessor` 在 `ProcessingEnvironment` 生命周期内按方向 + 名字缓存 `FileObject`
   （`ResourceProcessor.java:98`、`:123-130`），所以一次编译内重复写入会复用句柄——但缓存不跨构建生效，而且
   `Filer.createResource` 是“创建”语义的 API，因此残留的输出目录加上第二个处理器实例就可能触发
   `FileAlreadyExistsException`。这条路径**并不静默**：默认处理器会包装后重抛
   （`ResourceProcessor.java:235-240`，经由 `:114-117`），而最先撞上它的正是
   `ConfigurationPropertyAnnotationProcessor.java:150-155` 的那个 `NOTE` 打印。有疑问就 `mvn clean`；并且永远不要手工
   编辑生成文件——下次编译会覆盖它。
5. **`microsphere-java-core` 的 shaded 副本。** `maven-shade-plugin` 绑定在 `package` 阶段（`pom.xml:103-121`，
   插件声明在 `:105`），其 `artifactSet` 只包含一个构件：
   `io.github.microsphere-projects:microsphere-java-core`（`:113-117`）。整个 `<configuration>` 块（`:112-118`）里
   **没有 `<relocations>`、没有 `<minimizeJar>`、没有 `<filters>`、没有 `<shadedArtifactAttached>`**，
   因此插件只做最低限度的事：core 的每个类都被**原包名**复制进处理器 jar，并且 shaded jar *取代*
   `target/microsphere-annotation-processor-<version>.jar` 成为主构件（shade 前的原始 jar 被移动为 `original-*.jar`）。
   由于没有任何重定位，对使用者的重复类后果是直接的：如果你的构建同时依赖
   `microsphere-annotation-processor` 与 `microsphere-java-core`，那么同一个
   `io.microsphere.metadata.DefaultConfigurationPropertyGenerator` 在 classpath 上存在两份，
   赢家取决于 classpath 顺序，而不是你声明的版本。两份副本还携带同样的 `META-INF/services/*` 条目，
   于是 `ServiceLoader` 会按类名去重，症状通常表现为*版本*错位（内嵌副本在构建时被固定到 `${revision}`，
   根 `pom.xml:54`）而非 `ClassNotFoundException`。实用结论：处理器保持 `provided` 作用域；不要从 core 里
   exclude 任何东西；如果你必须依赖另一个版本的 core，请记住内嵌的那一份就跟着处理器 jar 一起分发。
   同一配置的好处是：`annotationProcessorPaths` 只需要一个 `<path>` 条目，因为 core 已经在处理器构件内部（2.2 节）。
6. **继承与多模块聚合都不会被合并。** 在一次编译中，元数据累加在同一个 `StringBuilder` 里、跨轮次累积，最后写入
   *本模块*的 class 输出（`ConfigurationPropertyAnnotationProcessor.java:82`、`:112-132`、`:149-156`）。没有任何代码会去读取
   已存在的 `configuration-properties.json` 做合并：`processInResourceWriter` 用的是 `createResource`
   （`ResourceProcessor.java:127`），不是读-改-写。因此多模块构建会在同一个 classpath 路径上产出**每个 jar 一份文件**；
   运行时内置 loader 只读取第一个命中
   （`microsphere-java-core/src/main/java/io/microsphere/metadata/ClassPathResourceConfigurationPropertyLoader.java:89`），
   所以“跨模块聚合”这一行为在编译期和运行期都不存在。继承字段不可见也是同一个原因：只遍历 `getEnclosedElements()`
   （`microsphere-lang-model/.../JSONElementVisitor.java:96`）。
7. **同名属性出现两次不算错误。** 两个同名字段的注解会产生两个互相独立的 JSON 对象——因为缺省的 `name` 会静默取字段的常量值
   （`ConfigurationPropertyJSONElementVisitor.java:119-122`），这种情况很容易发生——代码里没有名字索引、没有覆盖、也没有告警；
   每个字段各调用一次 generator（`:101-103`），而 `resolveMetadata` 只是把末尾逗号替换成 `]`
   （`ConfigurationPropertyAnnotationProcessor.java:126-131`）。去重只发生在*单个属性内部*，即 `sources` 与 `targets`
   是保持插入序的 `LinkedHashSet`（`microsphere-java-core/src/main/java/io/microsphere/beans/ConfigurationProperty.java:258-263`、`:282-287`）。
   若要“一个名字一条”，请读回之后自行去重。
8. **常量值不是 `String` 的字段会炸得很响。** `resolveName` 把回落值直接强转成 `String`
   （`ConfigurationPropertyJSONElementVisitor.java:120-121`），而模型拒绝 `null` 名字
   （`microsphere-java-core/src/main/java/io/microsphere/beans/ConfigurationProperty.java:120`），所以给一个非 `String`
   常量字段加上 `@ConfigurationProperty` 而不写 `name = …`，异常会在访问器内部抛出，而不是跳过该字段。非 `String` 常量请务必显式写 `name`。

---

## 9. 参见

* [配置属性元数据 — 用户指南](../microsphere-java-core/zh/configuration-metadata.md) —— 运行时那一半：reader、loader、generator，以及文件缺失时会发生什么
* [配置属性元数据 — 开发者指南](../../developer-guide/microsphere-java-core/configuration-metadata.md) —— 运行时那一半的源码分析
* [注解处理器 — 开发者指南](../../developer-guide/zh/microsphere-annotation-processor.md) —— 轮次、访问器、`Filer`、缺陷清单
* [microsphere-lang-model](microsphere-lang-model.md) —— `javax.lang.model` 门面与 `JSONElementVisitor` 家族
* [microsphere-annotation-test](microsphere-annotation-test.md) —— 本页测试所使用的进程内 `javac` 夹具
* [microsphere-java-annotations](microsphere-java-annotations.md) —— 拥有 `@ConfigurationProperty` 的模块
* [microsphere-jdk-tools](microsphere-jdk-tools.md) —— 夹具用来编译的 `tools.jar`/`javax.tools` 包装层
* 英文版本：[English User Guide](../microsphere-annotation-processor.md)
