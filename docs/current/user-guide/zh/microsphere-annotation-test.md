# Annotation Test：注解处理器测试框架

| | |
|---|---|
| **构件** | `io.github.microsphere-projects:microsphere-annotation-test`（`release` 分支为 `0.3.20-SNAPSHOT`，发布版本为 `0.3.20`） |
| **打包方式 / 名称** | `jar`，`Microsphere :: Annotation :: Test`（`microsphere-annotation-test/pom.xml:16`、`:18`） |
| **主代码类型** | 三个，全部位于 `io.microsphere.test.annotation.processing`（`microsphere-annotation-test/src/main/java/io/microsphere/test/annotation/processing/AbstractAnnotationProcessingTest.java:17`）：`AbstractAnnotationProcessingTest`（**public**，`:54`）、`AnnotationProcessingTestProcessor`（包级私有，`AnnotationProcessingTestProcessor.java:46`）、`CompilerInvocationInterceptor`（包级私有，`CompilerInvocationInterceptor.java:40`） |
| **编译期依赖** | `microsphere-java-test`（`microsphere-annotation-test/pom.xml:26`）、`microsphere-jdk-tools`（`:33`）—— 二者均为默认 compile 作用域，`${revision}` 见 `:27`、`:34` |
| **起始版本** | `1.0.0`（`AbstractAnnotationProcessingTest.java:51`） |
| **CI 测试 JDK** | Temurin 8 / 11 / 17 / 21 / 25（`.github/workflows/maven-build.yml:25`，`:33` 为 `distribution: 'temurin'`） |

本模块是注解处理技术栈的**测试支持构件**：它让一个 `@Test` 方法在**真实的 `javac` 注解处理轮次内部**运行，并把活动的
`ProcessingEnvironment`、`Elements`、`Types` 与 `RoundEnvironment` 作为字段交给你。你只需继承一个类，然后写普通断言。

> [!IMPORTANT]
> **只有一个类型是 public；另外两个不是 —— 而这就是你能编码引用的全部 API 表面。**
> `AbstractAnnotationProcessingTest` 是 `public abstract`（`AbstractAnnotationProcessingTest.java:54`），并且带有
> `@ExtendWith(CompilerInvocationInterceptor.class)`（`:53`）。`AnnotationProcessingTestProcessor`
> （`AnnotationProcessingTestProcessor.java:46`）与 `CompilerInvocationInterceptor`
> （`CompilerInvocationInterceptor.java:40`）都是包级私有 —— 你无法在自己的包里 import、注册或继承它们。
> 继承这个基类是唯一受支持的入口。

**面向谁：** 发布注解处理器的库作者，或者暴露 `javax.lang.model` 类型（`Element`、`TypeMirror`、`AnnotationMirror` 等）
工具方法的库作者。反射式的测试无法触及这些类型 —— 它们只在编译器运行期间存在。这个框架正是
`microsphere-lang-model` 与 `microsphere-annotation-processor` 用来测试自身的方式（见 §5）。

---

## 1. 你得到什么，什么时候需要它

当被测对象**只在编译期间存在**时，你需要本模块：

* **测试注解处理器。** 断言处理器产出了什么、它如何读取
  `RoundEnvironment#getElementsAnnotatedWith`、或它累积的元数据。
  `microsphere-annotation-processor` 正是这样做的
  （`microsphere-annotation-processor/src/test/java/io/microsphere/annotation/processor/ConfigurationPropertyAnnotationProcessorTest.java:34-43`）。
* **测试 `javax.lang.model` 工具类。** 任意类的 `TypeMirror` 在编译会话之外无法获得；`microsphere-lang-model` 的测试
  基类从注入的 `processingEnv` 构造它们
  （`microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/UtilTest.java:55-69`）。
* **用真实 `Filer`（而不是 mock）测试 `Filer` / 生成文件行为**
  （`microsphere-annotation-processor/src/test/java/io/microsphere/annotation/processor/FilerProcessorTest.java:51-55`）。

按类型来看，你得到什么：

| 类型 | 可见性 | 它为你做什么 |
|---|---|---|
| `AbstractAnnotationProcessingTest` | `public`（`:54`） | 你继承的基类：注入的状态（`:110-126`）、四个钩子（`:133`、`:150`、`:161`，外加具体方法 `initTestClass` 位于 `:136`）、27 个类型化 null / 空值常量（`:56-108`） |
| `AnnotationProcessingTestProcessor` | 包级私有（`:46`） | 被注入 `javac` 的 `AbstractProcessor`；它填充你的字段并调用你的测试方法（`:66-100`） |
| `CompilerInvocationInterceptor` | 包级私有（`:40`） | JUnit 5 的 `InvocationInterceptor`；它构建进程内 `Compiler`、收集待编译类、注册处理器并启动编译（`:43-64`） |

一句话概括流程：拦截器把测试类（加上 `addCompiledClasses` 返回的内容）作为编译单元，注入
`AnnotationProcessingTestProcessor` 以及所有通过 SPI 找到的 `Processor`，调用 `Compiler.compile(...)`
（`CompilerInvocationInterceptor.java:49-63`），于是你的 `@Test` 方法体就在该处理器 `process()` 内部被执行
（`AnnotationProcessingTestProcessor.java:75`）。

---

## 2. 坐标与依赖

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-annotation-test</artifactId>
    <version>0.3.20</version>
    <scope>test</scope>
</dependency>
```

请以 **test 作用域**声明它。仓库内两个使用方都是如此：
`microsphere-lang-model/pom.xml:52-57`（`<scope>test</scope>` 在 `:56`）与
`microsphere-annotation-processor/pom.xml:50-56`（`:55`）。该版本由仓库 BOM 统一管理，见
`microsphere-java-dependencies/pom.xml:50`。

### 2.1 版本与坐标，附证据

| 项目 | 值 | 证据 |
|---|---|---|
| Group id | `io.github.microsphere-projects` | 根 `pom.xml:12`，模块 `microsphere-annotation-test/pom.xml:13` |
| Artifact id | `microsphere-annotation-test` | `microsphere-annotation-test/pom.xml:14` |
| 源码线（release 分支） | `${revision}` = `0.3.20-SNAPSHOT` | `microsphere-annotation-test/pom.xml:15`，根 `pom.xml:54` |
| 示例中使用的发布版本 | `0.3.20` | 仓库 tag `0.3.20` |
| 模块 parent | `microsphere-java-parent`，`${revision}` | `microsphere-annotation-test/pom.xml:5-10` |
| 仓库 parent | `io.github.microsphere-projects:microsphere-build:0.3.16` | 根 `pom.xml:6-10` |
| 仓库构建顺序 | 9 个模块中的第 7 个，位于 `microsphere-java-test` 之后、`microsphere-lang-model` 之前 | 根 `pom.xml:58-66`（`microsphere-annotation-test` 在 `:64`） |

### 2.2 逐条照录的依赖清单

| 依赖 | POM 行号 | 作用域 / 标记 | 说明 |
|---|---|---|---|
| `microsphere-java-test` | `microsphere-annotation-test/pom.xml:24-28` | compile（无 `<scope>` 元素） | 面向测试的辅助类；但本模块主源码没有任何一处 import 它 |
| `microsphere-jdk-tools` | `:31-35` | compile（无 `<scope>` 元素） | 提供 `io.microsphere.jdk.tools.compiler.Compiler`，在 `CompilerInvocationInterceptor.java:19` 被 import |
| `org.junit.jupiter:junit-jupiter` | `:38-42` | `<optional>true</optional>`（`:41`） | 本模块*主源码*编译所依赖的 JUnit 5 API（`InvocationInterceptor`、`ExtendWith`）—— **是 optional，所以你必须自己声明 JUnit 5** |
| `org.mockito:mockito-core` | `:45-49` | optional（`:48`） | 本模块源码未使用 |
| `org.junit.jupiter:junit-jupiter-engine` | `:51-55` | optional（`:54`） | 运行测试需要 |
| `ch.qos.logback:logback-classic` | `:58-62` | optional（`:61`） | 编译输出 trace 的日志后端 |
| `javax.ws.rs:javax.ws.rs-api` | `:65-69` | optional（`:68`） | 处理器测试的夹具类 |
| `javax.xml.ws:jaxws-api` | `:72-76` | optional（`:75`） | 同上 |
| `org.springframework:spring-context` | `:79-83` | optional（`:82`） | 同上 |
| `org.springframework:spring-web` | `:85-89` | optional（`:88`） | 同上 |

由于 `<optional>` 依赖**不可传递**，引入本模块并不会给你带上 JUnit 5、Mockito、Logback、JAX-RS/JAX-WS API 与 Spring，
除非你自己声明。`microsphere-jdk-tools` 与 `microsphere-java-test` 则是传递性的（compile 作用域），
这正是本构件应当放在 test 作用域的原因。

---

## 3. 版本信息与 JDK 支持

### 3.1 字节码级别

| 项目 | 值 | 证据 |
|---|---|---|
| 三个类型的 `@since` | `1.0.0` | `AbstractAnnotationProcessingTest.java:51`、`AnnotationProcessingTestProcessor.java:43`、`CompilerInvocationInterceptor.java:38` |
| 发布线 | `release` 分支，`${revision}` = `0.3.20-SNAPSHOT` | 根 `pom.xml:54` |
| 发布 tag | `0.3.20` | 仓库 tag；group id 见根 `pom.xml:12` |
| 字节码级别 | **本仓库未声明** —— 从外部 parent `microsphere-build:0.3.16` 继承 | 根 `pom.xml:6-10`；整棵模块树中不存在任何 `maven.compiler.*` / `<release>` 属性，且 `microsphere-annotation-test/pom.xml` 完全没有 `<build>` 段（文件到 `:93` 结束） |

### 3.2 支持的 JDK，以及框架如何穿越模块系统

`com.sun.tools.javac` 与旧的 `jdk.tools`/`tools.jar` 世界在 JDK 9 之后截然不同：编译器迁入 `jdk.compiler` 模块，
`ToolProvider` 成为唯一受支持的入口，而未导出的 `com.sun.tools.javac.*` 包需要 `--add-exports`。本框架完全绕开了这些：

| 机制 | 代码 | 在 JDK 8 → 25 上的后果 |
|---|---|---|
| 模块内任何地方都不引用 JDK 内部类型 | 唯一的 JDK 工具 import 是 `io.microsphere.jdk.tools.compiler.Compiler`（`CompilerInvocationInterceptor.java:19`）；`com.sun.*`、`tools.jar`、`Class.forName`、`setAccessible`、`--add-exports` 在 `microsphere-annotation-test` 的**所有**源码文件中都不出现 | 无需导出、无需 open —— 同一个 jar 在 CI 的每个 JDK 上原样运行 |
| 编译器通过 `javax.tools` 获得 | `microsphere-jdk-tools` 调用 `ToolProvider.getSystemJavaCompiler()`（`microsphere-jdk-tools/src/main/java/io/microsphere/jdk/tools/compiler/Compiler.java:107`），为 `null` 时快速失败（`:108`） | JDK 8 解析 `lib/tools.jar`，JDK 9+ 解析 `jdk.compiler` 提供者；分支逻辑在 JDK 内部，不在这里 |
| 从不固定源版本 | `getSupportedSourceVersion()` 返回 `latestSupported()`（`AnnotationProcessingTestProcessor.java:102-105`，静态 import 在 `:37`） | CI 从 8 推进到 25 时不会出现 “supported source version RELEASE_x is less than -source” 警告 |
| 声明处理所有注解类型 | `@SupportedAnnotationTypes(WILDCARD)`（`AnnotationProcessingTestProcessor.java:45`；`WILDCARD` = `"*"`，见 `microsphere-java-core/src/main/java/io/microsphere/constants/SymbolConstants.java:313`） | 无论被编译源码携带哪些注解，该处理器都会参与轮次 |
| 处理器是被注入的，不是 `javac` 自己发现的 | `CompilerInvocationInterceptor.java:57-62` 构建列表；`Compiler.java:162` 调用 `task.setProcessors(...)` | JDK 9+ 上完全不涉及 `-processorpath` 与模块路径式处理器查找 |
| SPI 处理器来自**你自己的**类加载器 | `ClassLoader classLoader = testClass.getClassLoader();`（`:48`）然后 `load(Processor.class, classLoader)`（`:60`） | 即使处理器 jar 是模块化 jar，使用的仍是无名模块 / 测试类加载器 |
| 没有 `module-info.java` | `src/main/java/io/microsphere/test/annotation/processing/` 目录下恰好只有这三个文件 | 始终位于 classpath，因此不存在分包（split package）问题 |

> [!WARNING]
> **需要 JDK，不能是 JRE。** 编译由 `microsphere-jdk-tools` 中的
> `ToolProvider.getSystemJavaCompiler()` 驱动（`Compiler.java:107`）；在 JRE 上，或者不含 `jdk.compiler` 的
> `jlink` 镜像上，它返回 `null`，`Compiler` 的构造函数会抛出 `IllegalArgumentException`（`:108`），
> 你的测试方法体一次都不会执行。

> [!NOTE]
> CI 固定了运行时：Temurin `java: [ '8' , '11' , '17' , '21' , '25' ]`（`.github/workflows/maven-build.yml:25`、
> `:33`），并且构建命令带 `-Dsurefire.useSystemClassLoader=false`（`:43`）。本仓库的任何 POM 中**都没有**
> `maven-surefire-plugin` 配置，所以该开关只存在于 CI：如果你在 IDE 或其他保留 Surefire 系统类加载器的构建中运行这些
> 测试，SPI 处理器（`CompilerInvocationInterceptor.java:48`、`:60`）的类可见性判定会与 CI 不一致。

---

## 4. API 逐项走查

### 4.1 `AbstractAnnotationProcessingTest` —— 你要继承的类

```java
// AbstractAnnotationProcessingTest.java:53-54
@ExtendWith(CompilerInvocationInterceptor.class)
public abstract class AbstractAnnotationProcessingTest {
```

注入的状态在编译轮次期间被填充（`AnnotationProcessingTestProcessor.java:95-99`），全部是 `protected` 且非 `final`，
子类可自由读取（`AbstractAnnotationProcessingTest.java:110-126`）：

| 字段 | 声明 | 由谁填入 |
|---|---|---|
| `roundEnv` | `protected RoundEnvironment roundEnv`（`:110`） | 当前轮次（`AnnotationProcessingTestProcessor.java:95`） |
| `processingEnv` | `protected ProcessingEnvironment processingEnv`（`:112`） | `AbstractProcessor#processingEnv`（`AnnotationProcessingTestProcessor.java:90`、`:96`） |
| `elements` | `protected Elements elements`（`:114`） | `processingEnv.getElementUtils()`（`:91`、`:97`） |
| `types` | `protected Types types`（`:116`） | `processingEnv.getTypeUtils()`（`:92`、`:98`） |
| `testClass` | `protected Class<?> testClass`（`:118`） | `invocationContext.getTargetClass()`（`:93`、`:99` → `initTestClass`） |
| `testClassName` | `protected String testClassName`（`:120`） | `testClass.getName()`（`:138`） |
| `testTypeElement` | `protected TypeElement testTypeElement`（`:122`） | `elements.getTypeElement(testClassName)`（`:139`） |
| `testTypeMirror` | `protected TypeMirror testTypeMirror`（`:124`） | `testTypeElement.asType()`（`:140`） |
| `testDeclaredType` | `protected DeclaredType testDeclaredType`（`:126`） | `(DeclaredType) testTypeMirror` —— 未检查的强制转换（`:141`） |

四个可覆盖成员，外加一个具体辅助方法：

```java
// AbstractAnnotationProcessingTest.java:133-134 —— 默认为空方法体；在此添加额外编译单元
protected void addCompiledClasses(@Nonnull Set<Class<?>> compiledClasses) {
}

// AbstractAnnotationProcessingTest.java:136-142 —— 具体方法；把 test* 字段重新指向另一个类
protected void initTestClass(@Nonnull Class<?> testClass) {
    this.testClass = testClass;
    this.testClassName = testClass.getName();
    this.testTypeElement = this.elements.getTypeElement(this.testClassName);
    this.testTypeMirror = this.testTypeElement.asType();
    this.testDeclaredType = (DeclaredType) this.testTypeMirror;
}

// AbstractAnnotationProcessingTest.java:150-151 —— 每个非末轮次执行一次，在你的测试方法之前
protected void beforeTest(@Nonnull ReflectiveInvocationContext<Method> invocationContext, @Nonnull ExtensionContext extensionContext) {
}

// AbstractAnnotationProcessingTest.java:161-163 —— 始终执行，即使测试方法抛出异常
protected void afterTest(@Nonnull ReflectiveInvocationContext<Method> invocationContext, @Nonnull ExtensionContext extensionContext,
                         @Nullable Object result, @Nullable Throwable failure) {
}
```

钩子调用顺序（来自处理器）：`prepare(roundEnv)`（填字段，`AnnotationProcessingTestProcessor.java:70`）
→ `beforeTest`（`:73`）→ `invocation.proceed()`，即**你的测试方法**（`:75`）→ `finally` 中的 `afterTest`（`:79`）。
`result` 是你的 `void` 测试方法返回的内容（实际永远是 `null`），`failure` 是**未经**再次包装的原始 throwable
（`:71-79`）。

**27 个类型化常量**（`AbstractAnnotationProcessingTest.java:56-108`）的存在意义是：让测试可以向重载的
`javax.lang.model` 辅助方法传入*带类型的* `null` 而无需强制转换 —— 例如
`assertTrue(getDeclaredMethods(NULL_TYPE_ELEMENT).isEmpty())`
（`microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/MethodUtilsTest.java:108`）。它们都是普通的
`protected static final` 字段，本模块自己的测试逐个断言它们为 `null` 或空
（`microsphere-annotation-test/src/test/java/io/microsphere/test/annotation/processing/AnnotationProcessingTest.java:48-74`）。
注意其中若干是**裸类型**声明 —— `NULL_COLLECTION`（`:64`）、`NULL_LIST`（`:66`）、`NULL_CLASS`（`:90`）、
`NULL_TYPE`（`:82`）、`EMPTY_COLLECTION_ARRAY`（`:62`）、`NULL_PREDICATE_ARRAY`（`:96`）—— 这是刻意为之：
`:96` 的 `Predicate[]` 无法泛型化，而裸形式正是它们能传给任意重载的原因。

### 4.2 `AnnotationProcessingTestProcessor` —— 被注入的处理器（包级私有）

```java
// AnnotationProcessingTestProcessor.java:45-46
@SupportedAnnotationTypes(WILDCARD)
class AnnotationProcessingTestProcessor extends AbstractProcessor {
```

* 构造函数 `AnnotationProcessingTestProcessor.java:56-63`（包级私有）捕获四样东西：测试实例、JUnit 的
  `Invocation<Void>`、`ReflectiveInvocationContext<Method>` 和 `ExtensionContext`（`:48-54`）。
* `process(Set<? extends TypeElement>, RoundEnvironment)`（`:66-87`）是你的测试真正运行的地方。
  `if (!roundEnv.processingOver())` 这个守卫（`:69`）意味着 javac 的最后一个轮次里不会执行测试体；因此只走到末轮次的
  编译根本不会运行你的断言（见陷阱 3）。
* 它返回 `false`（`:86`）—— 框架不认领任何注解，好让同一轮次里的其他处理器继续工作。
* `prepare(RoundEnvironment)`（`:89-100`）写入 §4.1 表中列出的字段，并调用 `initTestClass(testClass)`（`:99`）。
* 失败路径（`:81-84`）：`getRootCause(failure)`（`:82`，`ThrowableUtils.java:47`）然后
  `throw wrap(cause, Error.class)`（`:83`，`ExceptionUtils.java:98-104`）。`AssertionError` 本身就是 `Error`，所以
  `wrap` 原样返回它（`ExceptionUtils.java:99-101`）；其它 throwable 则被重建为携带消息与 cause 的 `java.lang.Error`
  （`ExceptionUtils.java:304-308`）—— 原始类型丢失。

### 4.3 `CompilerInvocationInterceptor` —— JUnit 5 钩子（包级私有）

```java
// CompilerInvocationInterceptor.java:40-44
class CompilerInvocationInterceptor implements InvocationInterceptor {

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
```

逐行解读（`:45-63`）：

| 行号 | 发生了什么 |
|---|---|
| `:45` | `newLinkedHashSet()` —— 一个保持插入序、去重的待编译类集合（core `SetUtils.java:491`） |
| `:46` | 测试实例，取自 `invocationContext.getTarget().get()` |
| `:47-48` | `extensionContext.getTestClass().get()` 以及**它的**类加载器 |
| `:49` | 测试类自身永远是编译单元 |
| `:50` | `test.addCompiledClasses(compiledClassesSet)` —— 你的钩子在这里追加 |
| `:52` | 通过 core `ArrayUtils.EMPTY_CLASS_ARRAY` 摊平成 `Class<?>[]`（`ArrayUtils.java:157`） |
| `:54-55` | `new Compiler()` + `sourcePaths(compiledClasses)` —— 依据这些类的代码来源探测源码目录（`Compiler.java:121-134`） |
| `:57-58` | `AnnotationProcessingTestProcessor` 位于 `LinkedList` 首位 |
| `:60-61` | 测试类加载器可见的每一个 SPI `Processor` 依次追加 |
| `:62` | `compiler.processors(...)` —— 替换 `Compiler` 的默认空集合（`Compiler.java:136-139`） |
| `:63` | `compiler.compile(compiledClasses)` —— 完整往返；其 `boolean` 结果被丢弃 |

> [!TIP]
> 框架**请求**了一个顺序 —— `AnnotationProcessingTestProcessor` 在前，SPI 处理器追加在后
> （`CompilerInvocationInterceptor.java:57-61`）—— 但这个顺序并不保证能保留下来：
> `compiler.processors(...)` 会把数组重新装入一个 `Set`
> （`microsphere-jdk-tools/src/main/java/io/microsphere/jdk/tools/compiler/Compiler.java:136-139`），
> 经由 `Sets.ofSet`（`microsphere-java-core/src/main/java/io/microsphere/collection/Sets.java:552-557`、`:559-568`），
> JDK 9+ 上是 `Set.of`（迭代顺序未定义），JDK 8 的回退路径则是保持插入序的 `LinkedHashSet`
> （`microsphere-java-core/src/main/java/io/microsphere/collection/SetUtils.java:155-168`）。因此不要依赖你的处理器
> 排在框架处理器之前或之后：框架处理器声明了通配符（`AnnotationProcessingTestProcessor.java:45`），所以它参与每一个轮次，
> 并在自己的 `process()`（`:70`、`:89-100`）里填充你的字段，无论它排在哪个位置。

---

## 5. 一步步写出一个注解处理器测试

以下代码全部摘自 `release` 树中的使用方模块。

### 第 1 步 —— 以 test 作用域声明框架

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-annotation-test</artifactId>
    <version>0.3.20</version>
    <scope>test</scope>
</dependency>
```

（另外你自己还要声明 JUnit 5 —— `junit-jupiter` 在 `microsphere-annotation-test/pom.xml:41` 是 `<optional>`。）

### 第 2 步 —— 继承基类并编写普通断言

摘自 `microsphere-annotation-processor/src/test/java/io/microsphere/annotation/processor/ConfigurationPropertyAnnotationProcessorTest.java:34-43`：

```java
class ConfigurationPropertyAnnotationProcessorTest extends AbstractAnnotationProcessingTest {

    @Test
    void testResolveMetadataOnEmptySet() {
        ConfigurationPropertyAnnotationProcessor processor = new ConfigurationPropertyAnnotationProcessor();
        processor.init(super.processingEnv);
        processor.resolveMetadata(emptySet());
        String json = processor.toJSON();
        assertNotNull("[]", json);
    }
}
```

没有 `@ExtendWith`，没有编译器设置，没有 mock：方法体开始执行时 `super.processingEnv` 已经是一个真实环境
（`AnnotationProcessingTestProcessor.java:96`）。

### 第 3 步 —— 补上处理器必须看见的额外类

只以 class 文件形式存在的类型，在你的处理器所看到的模型里可能是不可见的；把它们声明为编译单元。
摘自 `microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/MethodUtilsTest.java:82-85`：

```java
@Override
protected void addCompiledClasses(Set<Class<?>> compiledClasses) {
    compiledClasses.add(PropertyConstants.class);
}
```

以及摘自 `microsphere-annotation-processor/src/test/java/io/microsphere/annotation/processor/ConfigurationPropertyJSONElementVisitorTest.java:46-55`：

```java
@Override
protected void addCompiledClasses(Set<Class<?>> compiledClasses) {
    compiledClasses.add(ManifestArtifactResourceResolver.class);
    compiledClasses.add(IOUtils.class);
    compiledClasses.add(StandardFileWatchService.class);
    compiledClasses.add(TypeUtils.class);
    compiledClasses.add(ServiceLoaderUtils.class);
    compiledClasses.add(MethodUtils.class);
    compiledClasses.add(ConfigurationProperty.class);
}
```

`microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/TypeUtilsTest.java:188-194` 对测试夹具
（`ArrayTypeModel`、`CollectionTypeModel`、`Color`、`MapTypeModel`）用的是同一套写法。

### 第 4 步 —— 在 `beforeTest` 里构造每个测试的对象，而不是用 `@BeforeEach`

摘自 `microsphere-annotation-processor/src/test/java/io/microsphere/annotation/processor/FilerProcessorTest.java:43-48`：

```java
private FilerProcessor processor;

@Override
protected void beforeTest(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) {
    this.processor = new FilerProcessor(super.processingEnv);
}
```

以及摘自 `microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/UtilTest.java:50-53` ——
把 `test*` 字段重新指向被分析的类，而不是测试类自身：

```java
@Override
protected void beforeTest(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) {
    initTestClass(TestServiceImpl.class);
}
```

### 第 5 步 —— 针对注入的模型进行断言

`UtilTest` 随后把整套模型能力暴露给它的 14 个子类
（`microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/UtilTest.java:55-92`），所以子类看起来就是普通的
单元测试 —— `microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/ClassUtilsTest.java:38-46`：

```java
@Test
void testGetClassName() {
    assertEquals(this.testClassName, getClassName(this.testTypeMirror));
}

@Test
void testLoadClassOnTypeMirror() {
    assertSame(this.testClass, loadClass(this.testTypeMirror));
}
```

### 第 6 步 —— 用 `afterTest` 做收尾与失败检查

摘自 `microsphere-annotation-test/src/test/java/io/microsphere/test/annotation/processing/AnnotationProcessingTest.java:84-90`：

```java
@Override
protected void afterTest(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext, Object result, Throwable failure) {
    super.afterTest(invocationContext, extensionContext, result, failure);
    if (failure != null) {
        assertEquals("For testing", failure.getMessage());
    }
}
```

`afterTest` 位于 `finally` 中（`AnnotationProcessingTestProcessor.java:78-80`），因此即使测试方法抛异常它也照样执行，
而且它拿到的是**原始** throwable —— 在 `wrap(..., Error.class)` 替换它之前（`:83`）。

### 第 7 步 —— 确认编译真的发生了

框架会丢弃 `compile()` 的返回值（`CompilerInvocationInterceptor.java:63`）。如果你的断言似乎根本没运行，
先把 `io.microsphere.jdk.tools.compiler.Compiler` 的日志级别开到 `TRACE` 跑一次，`javac` 被捕获的输出就在这一行
（`Compiler.java:165`），并确认源文件确实存在于磁盘上（§6 陷阱 2 与陷阱 3）。

---

## 6. 常见陷阱

1. **永远不要自己写 `@ExtendWith(CompilerInvocationInterceptor.class)`。** 它是包级私有的
   （`CompilerInvocationInterceptor.java:40`），而且基类已经声明了它
   （`AbstractAnnotationProcessingTest.java:53`）。你要扩展的是*测试*类。
2. **测试源码必须存在于磁盘上，并且符合 `target/classes` 布局。** 编译单元是由
   `Class` → `.java` 解析出来的（`Compiler.java:284-286`），手段是遍历该类代码来源的根目录（`Compiler.java:242-262`、
   `:264-271`）。找不到源码的类会被**静默丢弃**（`Compiler.java:209-215`），所以 `addCompiledClasses` 只对
   在同一仓库中由源码构建出来的类有意义 —— 而从打包好的 jar 运行的测试根本没有源码可编译。
3. **从未进入轮次的测试会“空过”。** `invocation.proceed()` 只在 `process()` 内部、且
   `!roundEnv.processingOver()` 时才会被调用（`AnnotationProcessingTestProcessor.java:69-80`）。如果编译先失败了 ——
   javac 报错、编译单元列表为空、`getSystemJavaCompiler()` 返回 `null`（`Compiler.java:107`）—— 拦截器就直接返回
   （`CompilerInvocationInterceptor.java:63` 忽略了返回值），JUnit 不会记录失败，而你的断言从未执行。
   把“绿色但可疑”当作信号，去检查 TRACE 日志（第 7 步）。
4. **方法体是每个非末轮次执行一次，而不是每个测试一次。** `process()` 是逐轮次回调查
   （`AnnotationProcessingTestProcessor.java:66`），每个非末轮次都会重新执行 `prepare` → `beforeTest` →
   `invocation.proceed()`（`:70-75`）。会产生新源码的处理器会引发更多轮次，于是你的测试体可能执行多次：
   保持幂等，不要往静态状态里累加。
5. **`processingEnv`、`elements`、`types`、`roundEnv` 在轮次开始前是 `null`。** 它们的赋值发生在
   `AnnotationProcessingTestProcessor.java:95-98`，也就是*在* `compiler.compile(...)` 期间
   （`CompilerInvocationInterceptor.java:63`）。JUnit 的 `@BeforeAll`/`@BeforeEach` 回调发生在
   `interceptTestMethod` 走到那一步之前，所以它们用不了这些字段 —— 那部分工作请放进 `beforeTest`
   （`AbstractAnnotationProcessingTest.java:150`）。
6. **构造函数与字段初始化器同样用不了模型。** 时机相同：测试实例由 JUnit 在编译开始之前构造
   （`CompilerInvocationInterceptor.java:46` 的 `invocationContext.getTarget().get()` 拿到的已经是一个活实例）。
7. **非断言异常的“类型”会丢失。** `throw wrap(getRootCause(failure), Error.class)`
   （`AnnotationProcessingTestProcessor.java:82-83`）会让 `AssertionError` 原样通过（`ExceptionUtils.java:99-101`），
   但把其它任何 throwable 重建为携带消息与 cause 的 `java.lang.Error`（`ExceptionUtils.java:102-103`、`:304-308`）。
   所以你在*整个测试*外层写 `catch (MyException e)` 是抓不到的；请在方法体内部使用 `assertThrows`。
8. **SPI 处理器会被无条件拉进来。** `load(Processor.class, classLoader)` 加 `forEach(processors::add)`
   （`CompilerInvocationInterceptor.java:60-61`）会注册测试 classpath 上所有在
   `META-INF/services/javax.annotation.processing.Processor` 中声明的处理器。这正是你的处理器被执行的原因；
   但这也意味着某个无关的第三方处理器可能开始往你的测试输出目录里生成文件。
9. **默认情况下生成物落在一个你并不控制的目录里。** `Compiler()` 把 `CLASS_OUTPUT` 与 `SOURCE_OUTPUT` 都指向
   `<root of Compiler.class>/target/generated-classes`（`Compiler.java:236-240`、`:176-177`）——
   在仓库构建中它是 `microsphere-jdk-tools` 模块的根目录，而当 jar 从磁盘解析时则落在本地仓库布局内部。
   请有意识地对待这一行为，或者清理那个目录。
10. **`initTestClass` 可能抛出裸 NPE。** `elements.getTypeElement(name)` 对编译器不认识的名字返回 `null`，
    而下一行就直接解引用（`AbstractAnnotationProcessingTest.java:139-140`）；`:141` 的 `(DeclaredType)` 转换是未检查的。
    传入名字里带 `$` 的嵌套类，或不在编译 classpath 上的类，就会踩到。
11. **只在 test 作用域依赖它。** 它的两个 compile 作用域依赖
    （`microsphere-annotation-test/pom.xml:24-28`、`:31-35`）如果进入生产依赖，会把 JUnit 相关代码和一个编译器驱动器
    一起拖进你的生产 classpath。

---

## 7. 参见

* [microsphere-jdk-tools](microsphere-jdk-tools.md) —— 本框架所驱动的 `javax.tools` 门面（`Compiler.java:107`、
  `:156-167`）
* [microsphere-annotation-processor](microsphere-annotation-processor.md) —— 用本框架测试的处理器（§5）
* [microsphere-lang-model](microsphere-lang-model.md) —— 基于本框架构建的 `UtilTest` 基类
  （`microsphere-lang-model/src/test/java/io/microsphere/lang/model/util/UtilTest.java:48`）
* [microsphere-java-test](microsphere-java-test.md) —— 那个 compile 作用域依赖
  （`microsphere-annotation-test/pom.xml:26`）
* [工具类、常量与文本 —— 用户指南](../microsphere-java-core/zh/util-and-constants.md) —— 本框架调用的 `core`
  辅助方法：`SymbolConstants.WILDCARD`（`microsphere-java-core/src/main/java/io/microsphere/constants/SymbolConstants.java:313`）、
  `SetUtils.newLinkedHashSet`（`microsphere-java-core/src/main/java/io/microsphere/collection/SetUtils.java:491`）、
  `ExceptionUtils.wrap`（`microsphere-java-core/src/main/java/io/microsphere/util/ExceptionUtils.java:98-104`）、
  `ThrowableUtils.getRootCause`（`microsphere-java-core/src/main/java/io/microsphere/util/ThrowableUtils.java:47`）
* [microsphere-annotation-test —— 开发者指南](../../developer-guide/zh/microsphere-annotation-test.md) —— 轮次往返
  时序图、钩子结构、设计模式与脆弱性评审
* 英文原版：[English User Guide](../microsphere-annotation-test.md)
