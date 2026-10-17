# JDK Tools：进程内编译

| | |
|---|---|
| **构件** | `io.github.microsphere-projects:microsphere-jdk-tools`（`release` 分支上为 `0.3.20-SNAPSHOT`，已发布为 `0.3.20`） |
| **类型** | 主源码**只有一个**类型：`io.microsphere.jdk.tools.compiler.Compiler`（`src/main/java/io/microsphere/jdk/tools/compiler/Compiler.java:72`） |
| **依赖** | 仅 `microsphere-java-core`（`microsphere-jdk-tools/pom.xml:24-28`，版本 `${revision}`） |
| **CI 覆盖的 JDK** | Temurin 8 / 11 / 17 / 21 / 25（`.github/workflows/maven-build.yml:25`、`:33`） |
| **使用的编译器 API** | `javax.tools.ToolProvider.getSystemJavaCompiler()` —— 在 `Compiler.java:64` 导入，在 `:107` 调用 |
| **测试面** | `src/test/java/io/microsphere/jdk/tools/compiler/CompilerTest.java`（11 个测试方法） |

本模块提供的是**对 JDK 自带编译器的可移植访问能力**：同一段代码可以在 JDK 8 到 25 上执行进程内编译。
它是注解处理测试栈所坐落的那一层：把 `Class` 对象换算成源文件，在你当前的 JVM 内运行 `javac`，
并把工作交给注册的注解处理器。

> [!IMPORTANT]
> **本模块不触碰 `com.sun.tools.javac` 内部 API。** 主源码里没有 `tools.jar` 路径拼接，没有
> `Class.forName("com.sun.tools.javac.…")`，也没有任何反射。可移植性完全交给标准 `javax.tools` API，由 JDK 自己在
> 各版本上做适配。下面那张分 JDK 的行为表，全部源自这一个调用点以及保护它的判空检查。

---

## 1. 你得到什么，什么时候需要它

当**测试或工具必须在运行中的 JVM 内编译 Java 源文件**、而不是外部调用 `javac` 进程时，
你需要本模块：

* **注解处理器测试。** 用真实类跑你的处理器，并断言它的产物。`microsphere-annotation-test` 就是这么做的：
  `CompilerInvocationInterceptor` 构造一个 `Compiler`，把测试涉及的类作为源路径喂进去，注册被测处理器以及通过
  SPI 找到的每个 `Processor`，然后调用 `compile(...)`
  （`microsphere-annotation-test/src/main/java/io/microsphere/test/annotation/processing/CompilerInvocationInterceptor.java:54-63`）。
* **源码到字节的生成流水线** —— 生成文件、编译、加载。
* 必须在 CI 的五种 JDK 上都能跑的**“这个类还能按我的预期被编译吗”**检查。

具体能力如下：

| 能力 | 入口 |
|---|---|
| 定位 JDK 编译器，缺失时立刻失败 | `new Compiler(...)` → `Compiler.java:103-109` |
| 按**源文件**编译给定的一组类 | `compile(Class<?>...)` → `Compiler.java:156-167` |
| 注册注解处理器 | `processors(Processor...)` → `:136-139` |
| 收集诊断信息 | `diagnosticListener(...)` → `:141-144` |
| 发现任意类的项目根目录 / 源目录 | `detectRootDirectory`、`detectSourcePath` → `:264-271`、`:242-262` |
| 控制 javac 选项、locale、编码 | `options`、`locale`、`charset` → `:111-114`、`:146-154` |

那几个路径发现静态方法（`detectClassPath` / `detectRootDirectory` / `detectSourcePath`）才是本模块存在的理由：
`javax.tools` 编译的是 `File`，
而调用方手上通常只有 `Class`。
`Compiler` 把每个类映射为 `<name>.java`
（`resolveJavaSourceFileRelativePath`，`Compiler.java:284-286`），在配置的源路径里搜索（`:217-226`），
并丢弃找不到源文件的类（`:209-215`）。

---

## 2. 坐标与仓库内的使用方

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-jdk-tools</artifactId>
    <version>0.3.20</version>
</dependency>
```

`release` 上 `${revision}` 为 `0.3.20-SNAPSHOT`（根 `pom.xml:54`）；groupId 固定在根 `pom.xml:12`。该版本同样由
反应堆 BOM 管理（`microsphere-java-dependencies/pom.xml:38`）。

| 仓库内使用方 | 作用域 | POM 证据 |
|---|---|---|
| `microsphere-annotation-test` | compile | `microsphere-annotation-test/pom.xml:33` |
| `microsphere-lang-model` | **test** | `microsphere-lang-model/pom.xml:46`，`:48` 为 `<scope>test</scope>` |
| `microsphere-jdk-tools` 自身 | compile | `microsphere-jdk-tools/pom.xml:14` |

`microsphere-annotation-processor` 对本模块**没有直接**依赖；
它是通过 `microsphere-annotation-test`（测试作用域，`microsphere-annotation-processor/pom.xml:53`）
以及 `microsphere-lang-model`（`microsphere-annotation-processor/pom.xml:33`）
间接拿到的。

> [!NOTE]
> `microsphere-lang-model` 声明了该依赖，但该模块的源码与测试里都没有引用
> `io.microsphere.jdk.tools.compiler.Compiler`（已遍历其源码核实）。请把这条依赖边理解为“留给驱动编译的测试”，
> 而不是运行期要求。

传递依赖里你还会得到 `microsphere-java-core`，本模块大量使用它的工具类 —— `CollectionUtils`、`Lists`、`Sets`、
`SetUtils`、`FileConstants`、`SymbolConstants`、`StringBuilderWriter`、`SimpleFileScanner`、`LoggerFactory`、
`FormatUtils`、`Assert`、`ClassUtils`、`StringUtils`（`Compiler.java:19-63`）。
core 不需要运行期日志实现：它的门面选取第一个可用的
`LoggerFactory`（`microsphere-java-core/src/main/java/io/microsphere/logging/LoggerFactory.java:73-96`）。

---

## 3. 版本信息与兼容范围

### 3.1 哪里写的是 1.0.0，哪里是 0.3.20

| 项目 | 取值 | 证据 |
|---|---|---|
| `Compiler` 的 Javadoc `@since` | `1.0.0` | `Compiler.java:70` |
| 本文档对应的发布线 | `release` 分支，`${revision}` = `0.3.20-SNAPSHOT` | 根 `pom.xml:54` |
| 已发布的标签 | `0.3.20` | 仓库标签；groupId 见根 `pom.xml:12` |
| 字节码级别 | **本反应堆未声明** —— 继承自外部父 POM `microsphere-build:0.3.16`（根 `pom.xml:8-9`） | 整棵模块树里不存在任何 `maven.compiler.*` / `<release>` 属性 |

> [!WARNING]
> 不要为本模块引用某个 `-source`/`-target` 数字。`release` 分支没有配置：整个反应堆里唯一的
> `maven-compiler-plugin` 配置是 `microsphere-annotation-processor` 中的 `-proc:none`，而
> `microsphere-jdk-tools` 根本没有 `<build>` 段（`microsphere-jdk-tools/pom.xml` 到 `:66` 结束）。
> 实际级别来自 `microsphere-build:0.3.16`。

### 3.2 依据代码得出的分 JDK 行为

`Compiler.java` 里**没有版本分支** —— 没有 `if (javaVersion >= 9)`，没有 `JavaVersion`/`SystemUtils` 判断，也没有
`Runtime.version()` 调用。真正存在的两处间接分支就是全部故事：

1. `getSystemJavaCompiler()`（`Compiler.java:107`）—— 由 JDK 自己的 `javax.tools.ToolProvider` 解析，编译器实现
   不可达时返回 `null`；`Compiler` 随即抛异常（`:108`）。
2. `Lists.ofList` / `Sets.ofSet` —— 被 `options(...)`（`:112`）与 `processors(...)`（`:137`）使用。若
   `List.of`/`Set.of` 存在就绑定它们的方法句柄（`microsphere-java-core/src/main/java/io/microsphere/collection/Lists.java:101`、
   `Sets.java:99`），否则回退到 `unmodifiableList(asList(…))` / 链表散列集，且回退包在 `catch (Throwable)` 里
   （`Lists.java:520-529`、`Sets.java:559-568`）。这只改变 options/processors 的**实现类**，不改变 API 约定。

| | JDK 8 | JDK 11 | JDK 17 | JDK 21 | JDK 25 |
|---|---|---|---|---|---|
| `getSystemJavaCompiler()` 的来源（`Compiler.java:107`） | 由 `ToolProvider` 从 `<jdk>/lib/tools.jar` 定位到的 `com.sun.tools.javac.Main` | `jdk.compiler` 模块，`javax.tools.JavaCompiler` 服务 | 同左 | 同左 | 同左 |
| `tools.jar` | JDK 中有，JRE 中无 | **不存在** | 无 | 无 | 无 |
| `Compiler` 是否会去找 `tools.jar`？ | **不会** —— 在任何 JDK 上都只对返回的 `JavaCompiler` 判空（`:107-108`） | 不会 | 不会 | 不会 | 不会 |
| 本模块需要 `--add-exports` / `--add-opens` 吗 | 不需要 | 不需要 | 不需要 | 不需要 | 不需要 |
| 仅 JRE 的运行环境 | `null` → `IllegalArgumentException`（`:108`） | 镜像中缺 `jdk.compiler`（例如未包含它的 `jlink` 镜像）时为 `null` → 同样异常 | 同左 | 同左 | 同左 |
| `options(...)` / `processors(...)` 的集合实现（`:112`、`:137`） | `Arrays.asList` 支撑的不可变列表 / 链表散列集 | `List.of`/`Set.of`（不可变） | 不可变 | 不可变 | 不可变 |
| `compile(...)` 产出的字节码级别 | 运行 JDK 的默认级别，因为从不传 `--source`/`--release`（`DEFAULT_OPTIONS`，`:76-77`） | 同左 | 同左 | 同左 | 同左 |
| 源文件发现（`:242-271`） | 通过 `CodeSource` 位置，并假定 `target/classes` 布局 | 同左 | 同左 | 同左 | 同左 |

### 3.3 关于 `--add-exports`，精确回答

这一带最常流传的参数是 `--add-exports java.compiler/com.sun.tools.javac.*=ALL-UNNAMED`。针对 `release` 分支有两点更正：

* 包 `com.sun.tools.javac.*` **不**在模块 `java.compiler` 里（该模块只承载标准
  `javax.tools` / `javax.lang.model` / `javax.annotation.processing` API）。实现位于 **`jdk.compiler`**。
* `microsphere-jdk-tools` 从不读取这些包，所以在 CI 矩阵的任何 JDK 上都不需要这类参数。只有当**你的**代码或某个把
  `JavaCompiler` 向下转型为 `com.sun.tools.javac` 类型的测试框架介入时，参数才有意义。

> [!TIP]
> CI 就是证据：构建命令为
> `mvn --batch-mode --update-snapshots --file pom.xml -Drevision=0.0.1-SNAPSHOT -Dsurefire.useSystemClassLoader=false test --activate-profiles test,coverage`
> （`.github/workflows/maven-build.yml:38-45`）。既没有传 `--add-opens` / `--add-exports`，仓库里也**没有
> `.mvn/jvm.config`**（只有 `.mvn/wrapper/maven-wrapper.properties`）。因此 `CompilerTest` 必须、也确实能在五种
> 原版 JVM 上运行。若你自己的测试需要模块参数，那是你的代码的要求，与 `Compiler` 无关。

---

## 4. API 参考

以下签名逐字取自 `release`。类型为 `public class Compiler`（`Compiler.java:72`），设置方法均以 `this` 支持链式调用。

```java
public class Compiler {

    // Compiler.java:76-77
    public static final String[] DEFAULT_OPTIONS = {"-parameters", "-Xlint:-unchecked", "-nowarn", "-Xlint:deprecation"};

    // 构造器 — Compiler.java:95-109
    public Compiler()                                       // 源目录与目标目录均由 Compiler.class 探测
    public Compiler(File targetDirectory)                   // 源目录自动探测，目标目录显式指定
    public Compiler(File defaultSourceDirectory, File targetDirectory)
        // throws IllegalArgumentException("No Java compiler available. Ensure this process is running
        //         on a JDK (not just a JRE).")  — via Assert.assertNotNull, Compiler.java:108

    // 链式配置 — 全部返回 this
    public Compiler options(String... options)              // :111-114  整体替换列表（不追加）
    public Compiler sourcePaths(File... sourcePaths)        // :116-119  向集合追加
    public Compiler sourcePaths(Class<?>... sourceClasses)  // :121-126  追加每个类探测到的源路径
    public Compiler sourcePath(Class<?> sourceClass)        // :128-134  找不到源文件时为空操作
    public Compiler processors(Processor... processors)     // :136-139  整体替换集合
    public Compiler diagnosticListener(DiagnosticListener<? super JavaFileObject> l)  // :141-144
    public Compiler locale(Locale locale)                   // :146-149
    public Compiler charset(Charset charset)                // :151-154

    // 终结操作 — Compiler.java:156-167
    public boolean compile(Class<?>... sourceClasses) throws IOException;

    // 访问器 — Compiler.java:169-207
    public JavaCompiler getJavaCompiler();                                  // 构造之后永不为 null
    public StandardJavaFileManager getJavaFileManager() throws IOException;  // 调用方负责关闭
    public DiagnosticListener<? super JavaFileObject> getDiagnosticListener(); // 未设置则为 null
    public Locale getLocale();                                              // 未设置则为 null
    public Charset getCharset();                                            // 未设置则为 null
    public List<String> getOptions();                                       // 永不为 null；被清空后为空
    public Set<Processor> getProcessors();                                  // 永不为 null

    // 静态发现 — Compiler.java:228-286
    public static File detectSourcePath(Class<?> sourceClass);              // 找不到源文件返回 null（:242-262）
    public static File detectRootDirectory(Class<?> sourceClass);           // :264-271
    public static File detectClassPath(Class<?> sourceClass);               // 抛 UnsupportedOperationException（:273-282）
    public static String resolveJavaSourceFileRelativePath(Class<?> sourceClass); // :284-286
    static File defaultSourceDirectory()    // 包级私有，:228-230
    static File defaultRootDirectory()      // 包级私有，:232-234
    static File defaultTargetDirectory()    // 包级私有，:236-240，mkdirs() 不检查返回值
}
```

### 4.1 代码真正实现的约定

| 方法 | 来自源码的约定 |
|---|---|
| `compile(Class...)` | 每次调用新建一个 `StandardJavaFileManager`（`:159`），把 `SOURCE_PATH` 设为配置的源目录，`CLASS_OUTPUT` **与** `SOURCE_OUTPUT` 都设为那唯一的输出目录（`:175-177`），传入用于捕获输出的 `Writer`，调用 `task.setProcessors(getProcessors())` 并返回 `task.call()`（`:162-163`）。`true` 表示编译成功。 |
| `compile` 与缺失的源文件 | 找不到 `.java` 的类会被**静默丢弃** —— `.filter(Objects::nonNull)`（`:209-215`）。若全部被丢弃，javac 收到空的编译单元列表，`compile` 通常仍返回 `true`。 |
| `getOptions()` | 配置项的 `unmodifiableList` 视图；列表为空时返回 `emptyList()`（`:193-199`）。 |
| `getProcessors()` | `unmodifiableSet`；从未设置时为 `emptySet()`（`:201-207`）。`compile()` 总会调用 `task.setProcessors(getProcessors())`（`:162`），而 `javax.tools.CompilationTask` **仅**在集合非空时才执行注解处理 —— 因此字段保持未设置（`:85`）意味着*完全不处理*，而不是“让 javac 从 classpath 自行发现处理器”。请显式注册处理器；这正是 `CompilerInvocationInterceptor` 先走 SPI 加载它们的原因（`microsphere-annotation-test/.../CompilerInvocationInterceptor.java:60-62`）。 |
| `getJavaFileManager()` | 该访问器**不**负责关闭；`compile` 通过 try-with-resources 关闭（`:159`），直接调用者同样需要自行关闭。 |
| 可空性 | `Compiler.java` 完全没有 `@Nullable` / `@Nonnull` 注解（文件里唯一的 Javadoc 是 `:66-71` 的类注释）；因此上表就是契约，逐行读自代码。 |
| 线程安全 | 只有 `javaCompiler` 是 `final` 且安全发布（`:83`、`:107`）；其余字段都可变且无同步（`:85-93`）—— 见陷阱 7。 |

### 4.2 `DEFAULT_OPTIONS` 对你的影响

`-parameters` 会把方法参数名保留进 class 文件 —— 注解处理器以及任何基于反射读取元数据的代码都依赖它。
`-Xlint:-unchecked` 与 `-nowarn` 抑制警告输出，而 `-Xlint:deprecation` 要求输出过时提示；这组选项对 javac 并不矛盾，
但确实意味着警告很少进入被捕获的输出（陷阱 4）。

---

## 5. 可直接运行的示例

取自 `microsphere-jdk-tools/src/test/java/io/microsphere/jdk/tools/compiler/CompilerTest.java` —— 完整配置链加一次编译：

```java
Compiler compiler = new Compiler();

boolean ok = compiler.compile(Compiler.class, Test.class);   // ok == true

compiler.options()                                            // 清空 DEFAULT_OPTIONS
        .processors(new AbstractProcessor() {
            @Override
            public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                return false;
            }
        })
        .diagnosticListener(diagnostic -> { })
        .locale(getDefault())
        .charset(UTF_8);

ok = compiler.compile(Compiler.class);
```

出处：`CompilerTest.java:143-170`（`testCompile`）。相关断言：

* `CompilerTest.java:38-44` —— 无参构造得到非 null 的 `JavaCompiler`。
* `CompilerTest.java:86-93` —— `detectClassPath(Compiler.class)` 存在；`detectClassPath(String.class)` 抛
  `UnsupportedOperationException`。
* `CompilerTest.java:107-121` —— `sourcePaths(...)` 返回同一实例（链式），并同时接受 `File` 与 `Class` 变参。
* `CompilerTest.java:181-187` —— 源文件不在项目树中的类，`detectSourcePath` 返回 `null`
  （`assertNull(detectSourcePath(Test.class))`）。

> [!NOTE]
> `testCompile` 内的注释写着“with mocked compiler it's safe to call”（`CompilerTest.java:146`）。该测试并没有任何
> mock —— `CompilerTest.java:1-28` 未导入 Mockito 类型，`mockito-core` 只是被声明
> （`microsphere-jdk-tools/pom.xml:52-56`）。测试是**真的**在 `Compiler.java` 与 `CompilerTest.java` 上跑 `javac`。

---

## 6. 常见陷阱

1. **JDK 8 与 `tools.jar`：是“假定”，不是“检测”。** `Compiler` 只调用
   `ToolProvider.getSystemJavaCompiler()`（`:107`）并对结果判空（`:108`）。它从不读取 `java.home`，从不为
   `<jdk>/lib/tools.jar` 构建类加载器，也从不报告编译器*为什么*缺失。在 JDK 8 上这意味着：如果你跑在 JRE 上，
   或者你的构建把 `tools.jar` 排除在 `ToolProvider` 看不见的 uber/shade classpath 之外，你只会得到一条固定消息的
   异常，关于缺失 jar 的诊断信息一条也没有。
2. **失败方式是抛异常，而不是静默降级。** `assertNotNull` 是 core 的断言工具，抛
   `IllegalArgumentException`（`microsphere-java-core/src/main/java/io/microsphere/util/Assert.java:149-153`）。
   消息原文（`Compiler.java:108`）：
   `No Java compiler available. Ensure this process is running on a JDK (not just a JRE).`
   本库其余约定是安静降级（`detectSourcePath` 在 `:250-253` 返回 `null`，`searchJavaSourceFile` 在 `:225` 返回
   `null`）；该构造器是本模块**唯一**刻意的 fail-fast。请在构造点捕获，而不是包在 `compile` 外面。
3. **JRE 与 JDK 的区别。** JRE（以及未包含 `jdk.compiler` 的 `jlink` 镜像）触发上一条异常。注意不对称之处：异常发生在
   创建 `Compiler` 时，所以在 JRE 上先调用 `Compiler.detectSourcePath(...)` 是安全的 —— 只有构造器会失败。
4. **编译器输出只在 trace 级别可见。** `compile()` 把 javac 的输出写进 `StringBuilderWriter`（`:158`）然后丢弃；
   唯一出口是 `finally` 块中的 `logger.trace(...)`（`:165`）。如果你既没传 `diagnosticListener(...)`（字段初值为
   `null`，`:89`），日志级别又高于 `TRACE`，那么编译失败只会返回 `false`，不带任何解释。要么注册监听器，要么为
   `io.microsphere.jdk.tools.compiler.Compiler` 打开 trace。
5. **`sourcePaths(File...)` 是追加，`options(...)` 与 `processors(...)` 是替换。** `sourcePaths` 调用
   `CollectionUtils.addAll` 写入既有 `LinkedHashSet`（`:117`，core `CollectionUtils.java:454`），而 `options` 直接重新赋值
   （`:112`），`processors` 同样（`:137`）。因此不带参数调用 `options()` 会丢掉 `-parameters` —— 对依赖参数名的处理器
   是一次静默的行为变更。
6. **`SOURCE_OUTPUT` 与 `CLASS_OUTPUT` 都指向同一个输出目录**（`:176-177`）。生成的源文件与生成的 `.class` 落在同一棵
   目录树里，所以删除该目录的 `clean` 会连同你想检查的生成源码一起删掉。
7. **一个实例多线程不受支持。** 所有配置字段都是可变且非 `final` 的（`:79-93`）。两个线程在同一实例上调用
   `compile(...)`，同时第三个线程重新配置它，就可能让 `LinkedHashSet` 的修改（`sourcePaths`，`:117`）与
   `searchJavaSourceFile` 内部的迭代（`:219`）交织 —— `ConcurrentModificationException`。请为每个测试/每轮编译使用
   独立 `Compiler`，或者在外部加锁。
8. **默认输出目录取自*本模块*的位置，而不是你的项目。** `defaultTargetDirectory()` 是
   `<Compiler.class 的根>/target/generated-classes`（`:236-240`），其根来自
   `detectRootDirectory(Compiler.class)` = `classPath.parent.parent`（`:264-271`）。Maven 构建下它解析为
   `microsphere-jdk-tools/target/generated-classes`；而当类来自本地仓库中的 jar 时，两次 `getParentFile()` 会落在
   **仓库目录布局内部**。`dir.mkdirs()` 的返回值未被检查（`:238`）。如果默认值对你有意义，请显式传入输出目录 ——
   `new Compiler(new File("src/main/java"), new File("target/generated-classes"))`。
9. **找不到 class path 会抛异常。** 当类没有 `CodeSource` 时，`detectClassPath` 抛出
   `UnsupportedOperationException`，消息模板为
   `"The source {} is based on the file system, the class path can't be detected."` —— `{}` 是 core `FormatUtils`
   的占位符（`microsphere-java-core/src/main/java/io/microsphere/text/FormatUtils.java:17`、`:38`），由该类替换
   （`:276-281`，文本在 `:280`）。实际对应运行期镜像里的类，或不上报 code source 的类加载器。`detectSourcePath` 与
   `sourcePath(Class)` 是降级版本（`:250-253`）；`detectRootDirectory` 与 `Compiler()`/`Compiler(File)` 构造器不是 —— 经由
   `defaultSourceDirectory()` → `detectSourcePath` 是 null 安全的，但 `defaultRootDirectory()` → `detectClassPath`
   （`:232-234`、`:265`）会先抛出。
10. **源文件查找是一次递归、无边界的全盘扫描。** `detectSourcePath` 用
    `SimpleFileScanner.INSTANCE.scan(rootDirectory, true, …)` 扫描整个模块根目录（`:246-247`，core
    `io/scanner/SimpleFileScanner.java:57`、`:87`），匹配条件是 `getAbsolutePath().endsWith(relativePath)`（`:247`）。
    在大目录树上这是一次昂贵的遍历，而 `target/` 或生成源目录里的陈旧副本可能抢先命中 —— 结果取自
    `first(sourceFiles)`（`:249`），其顺序取决于 `File#listFiles()`。
11. **Windows 路径按字面处理。** `detectClassPath` 用 `new File(location.getPath())` 转换 code-source URL
    （`:277-278`），绕过了 `%XX` 解码与 URI 处理。项目路径包含空格或非 ASCII 字符时，请在 Windows 上先验证再依赖源文件发现。
12. **默认选项里没有 `-encoding`。** `charset` 字段初值为 `null`（`:93`），于是 `getStandardFileManager(...)`（`:174`）
    使用 JVM 默认编码。在默认平台编码非 UTF-8 的环境下，含非 ASCII 字面量的源文件可能改变 javac 的诊断，甚至损坏字符串常量。
    请调用 `.charset(UTF_8)`（`CompilerTest.java:164` 就是这么做的）。
13. **忘记 `.processors(...)` 会静默跳过你真正想测的东西。** `compile()` 无条件传入 `getProcessors()`（`:162`）；
    字段未设置时它是 `emptySet()`（`:201-207`），而 `CompilationTask` 只在集合非空时才做注解处理。最终现象是一次
    “成功的编译”，没有生成产物，处理器侧的断言也从未触发。
14. **编码是以对象而非名称传入的。** `getJavaFileManager()` 把 `getCharset()` ——
    `public Charset getCharset()`（`:189-191`）—— 交给 `JavaCompiler.getStandardFileManager(...)`，而按 `javax.tools`
    约定该位置应当是编码**名称**；本模块在设置方法（`:151-154`）与调用点（`:174`）之间不存在 `Charset` → 名称的转换。
    这是一个类型层面的不一致，在依赖非默认编码之前值得核实；预期的取值应当是名称（`getCharset().name()`），而
    `CompilerTest.java:164-167` 正是覆盖这条路径的代码。

---

## 7. 参见

* [microsphere-annotation-test](../microsphere-annotation-test.md) —— 驱动本模块的 JUnit 5 扩展
* [microsphere-annotation-processor](../microsphere-annotation-processor.md) —— 最常被它编译的那批处理器
* [microsphere-lang-model](../microsphere-lang-model.md) —— 同一工具链的 `javax.lang.model` 门面
* [类加载与构件识别 —— 用户指南](../microsphere-java-core/zh/classloading-and-artifacts.md) —— core 中同一主题的
  JDK 内部访问页面
* [microsphere-jdk-tools —— 开发者指南](../../developer-guide/zh/microsphere-jdk-tools.md) —— `Compiler.java`
  逐行分析与图示
* 英文版：[English User Guide](../microsphere-jdk-tools.md)
