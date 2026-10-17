# 类加载与构件探测（Class Loading and Artifact Detection）

| | |
|---|---|
| **构件（Artifact）** | `io.github.microsphere-projects:microsphere-java-core`（`0.3.20-SNAPSHOT`，`release` 分支） |
| **包** | `io.microsphere.classloading` —— 16 个主源码类型 |
| **JDK 级别** | 以 Java 8 编译；CI 在 JDK 8 / 11 / 17 / 21 / 25 上测试 |
| **对 `io.microsphere.util` 的依赖** | `ClassLoaderUtils`（`findAllClassPathURLs`、`getClassResource`、`getClassLoader`、`removeClassPathURL`）、`ClassPathUtils.getBootstrapClassPaths()`、`ServiceLoaderUtils.loadServicesList`、`ObjectUtils`、`StringUtils`、`ArrayUtils`、`Assert`、`util.jar.JarUtils` |
| **使用的其它 `core` 包** | `io.microsphere.net.URLUtils`（归档文件/基础路径解析）、`io.microsphere.reflect`（字段与方法访问）、`io.microsphere.io` 与 `io.scanner`、`io.microsphere.logging`、`io.microsphere.constants`、`io.microsphere.lang.Prioritized`、`io.microsphere.annotation.*`（来自 `microsphere-java-annotations`） |
| **外部依赖** | 无 —— 探测过程仅使用 JDK 与 SPI |

本包回答一个运行期问题：*某个类、资源或 classpath 条目究竟来自哪个构件（groupId / artifactId / version）？*
同时提供了在「某构件不允许出现」时把 classpath 条目移除的能力（`BannedArtifactClassLoadingExecutor`）。

---

## 1. 版本信息与兼容区间

### 1.1 各 JDK 下 classpath 条目的读取方式

`ArtifactDetector` 从不自行解析 `java.class.path`。它调用 `ClassLoaderUtils.findAllClassPathURLs(classLoader)`，
该方法逐级遍历类加载器父链，并把每个加载器交给唯一的全局静态 `ServiceLoadingURLClassPathHandle`
（`microsphere-java-core/src/main/java/io/microsphere/util/ClassLoaderUtils.java:94`、`:1550-1564`）。该门面会挑选
**一个**已注册的 `URLClassPathHandle`——按优先级排序后第一个 `supports()` 返回 `true` 的实现
（`classloading/ServiceLoadingURLClassPathHandle.java:36-43`）。

| | JDK 8 | JDK 11 | JDK 17 | JDK 21 | JDK 25 |
|---|---|---|---|---|---|
| 系统类加载器类型 | `sun.misc.Launcher$AppClassLoader`（是 `URLClassLoader`） | `jdk.internal.loader.ClassLoaders$AppClassLoader` | 同左 | 同左 | 同左 |
| 胜出的 Handle | `ClassicURLClassPathHandle`（`sun.misc.URLClassPath`） | `ModernURLClassPathHandle`（`jdk.internal.loader.URLClassPath`） | Modern | Modern | Modern |
| 读取的 URL 字段 | `urls` | `unopenedUrls` | `unopenedUrls` | `unopenedUrls` | `unopenedUrls` |
| 条目来源 | classpath URL 解析 + 反射调用 `URLClassPath#getURLs()` | 反射调用 `URLClassPath#getURLs()`；`jrt:`／module-path 条目仅以加载器暴露的 URL 形式出现，**不是**通过 JPMS 模块读取 | 同左 | 同左 | 同左 |
| `removeURL(...)` 的作用对象 | `URLClassLoader.ucp` | `BuiltinClassLoader.ucp`（需要 `jdk.internal.loader` 处于打开状态） | 同左 | 同左 | 同左 |
| 两个 Handle 都无法解析其类时 | — | 退化为 `NoOpURLClassPathHandle`：`getURLs()` 返回共享空数组，`removeURL()` 返回 `false` | 同左 | 同左 | 同左 |
| 胜出的解析器顺序 | Maven(1) → Manifest(5) → ArchiveFile(9) | 完全相同 | 完全相同 | 完全相同 | 完全相同 |

需要明确的两个事实：

* `ClassicURLClassPathHandle.supports()` 的语义就是「`sun.misc.URLClassPath` 能否被加载」，
  `ModernURLClassPathHandle.supports()` 则是「`jdk.internal.loader.URLClassPath` 能否被加载」
  （`AbstractURLClassPathHandle.java:114-117`、`:178-186`）。测试从外部固化了同一条规则：
  `assertEquals(CURRENT_JAVA_VERSION.le(JAVA_VERSION_8), handle.supports())`
  （`.../test/java/io/microsphere/classloading/ClassicURLClassPathHandleTest.java:44-47`）以及
  `assertEquals(CURRENT_JAVA_VERSION.ge(JAVA_VERSION_9), handle.supports())`（`ModernURLClassPathHandleTest.java:41-43`）。
* **本包没有任何类型读取 JPMS 模块描述符。** JDK 9+ 上所有信息仍然来自加载器的 `URLClassPath`，因此 module-path 与
  `jrt:` 条目只有在加载器把它们暴露为 URL 时才可见。JDK 库过滤随之在 9+ 上退化：
  `ArtifactDetector.removeJdkClassPathURLs(...)`（`ArtifactDetector.java:164-175`）会剔除位于
  `ClassPathUtils.getBootstrapClassPaths()` 中或包含 `java.home` 的条目；而 JDK 9+ 上
  `RuntimeMXBean.isBootClassPathSupported()` 为 `false`，引导类路径集合为空
  （`ClassPathUtils.java:68-70`、`:111-112`），于是只剩 `JAVA_HOME` 包含性判断仍然有效。

### 1.2 配置项：系统属性与其它可调参数

| 名称 | 读取者 | 默认值 | 说明 |
|---|---|---|---|
| `microsphere.artifact-id.manifest-attribute-names` | `ManifestArtifactResourceResolver`（`:128-134`） | `Bundle-Name,Automatic-Module-Name,Implementation-Title` | 逗号分隔，取第一个非 null 属性 |
| `microsphere.artifact-version.manifest-attribute-names` | `ManifestArtifactResourceResolver`（`:147-153`） | `Bundle-Version,Implementation-Version` | 逗号分隔，取第一个非 null 属性 |
| `microsphere.service-loader.cached` | `ServiceLoaderUtils`（`:104-111`），影响 `URLClassPathHandle` 的发现 | `false` | `ArtifactDetector` 显式以 `cached = true` 请求解析器（`ArtifactDetector.java:92`） |
| `java.home` | `ArtifactDetector.JAVA_HOME_PATH`（`:26`、`:80`） | — | 非 JDK 过滤依据，进程内只捕获一次 |
| `file.encoding` | `BannedArtifactClassLoadingExecutor.ENCODING`（`:21`、`:69`） | — | 读取 `META-INF/banned-artifacts` 使用的字符集 |

> [!IMPORTANT]
> manifest 属性列表与 `JAVA_HOME` 都是在**静态初始化块**中求值的（`ManifestArtifactResourceResolver.java:162-164`、
> `ArtifactDetector.java:80`）。必须用命令行 `-D` 方式设置；类加载之后再调用 `System.setProperty` 不会生效。

```bash
java -Dmicrosphere.artifact-version.manifest-attribute-names=Implementation-Version,Specification-Version \
     -jar app.jar
```

### 1.3 `@Since` 版本沿革

16 个类型中有 14 个在 Javadoc 里标注了 `@since 1.0.0`（全部 `Artifact*`、`*URLClassPathHandle`、
`BannedArtifactClassLoadingExecutor`、`StreamArtifactResourceResolver`）。而 **`Artifact` 与
`ArtifactResourceResolver` 的源码中完全没有记录版本**，本包也未使用 `@Since` 注解，因此这两个类型的首个发布版本
无法从代码中确认。`release-notes.md` 记录了两处会影响行为的变化：**0.2.7** 重构了 `ArtifactDetector` 的探测 API
（`release-notes.md:5-15`）；**0.3.4** 将 `StreamArtifactResourceResolver` 与 `JarUtils` 一并重构
（`release-notes.md:135-152`）。

---

## 2. API 参考

以下签名均取自 `release` 分支源码。

```java
// Artifact.java —— 不可变值对象；"*" 为通配符，"?" 为 UNKNOWN
public class Artifact {
    public static final String WILDCARD;           // "*"
    public static final String UNKNOWN;            // "?"
    public Artifact(@Nonnull String artifactId, @Nullable String version, @Nullable URL location)
    public static Artifact create(@Nonnull String artifactId)                                  // version = UNKNOWN
    public static Artifact create(@Nonnull String artifactId, @Nullable String version)
    public static Artifact create(@Nonnull String artifactId, @Nullable String version, @Nullable URL location)
    @Nonnull public String getArtifactId()
    @Nullable public String getVersion()
    @Nullable public URL getLocation()
    public boolean matches(Artifact artifact)      // artifactId 且 version
    protected boolean matchesArtifactId(Artifact artifact)
    protected boolean matchesVersion(Artifact artifact)
    protected boolean matches(Artifact artifact, Function<Artifact, String> getterFunction)
}

// MavenArtifact.java —— 在身份与匹配规则中加入 groupId
public class MavenArtifact extends Artifact {
    public MavenArtifact(@Nonnull String groupId, @Nonnull String artifactId,
                         @Nullable String version, @Nullable URL location)
    public static MavenArtifact create(@Nonnull String groupId, @Nonnull String artifactId)      // version = UNKNOWN
    public static MavenArtifact create(String groupId, String artifactId, @Nullable String version)
    public static MavenArtifact create(String groupId, String artifactId, @Nullable String version, @Nullable URL location)
    @Nonnull public String getGroupId()
    @Override public boolean matches(Artifact artifact)   // groupId 且 artifactId 且 version
}

// ArtifactDetector.java —— 入口类；基于实例，没有静态快捷方式
public class ArtifactDetector {
    public ArtifactDetector()                                       // 使用本类的类加载器
    public ArtifactDetector(@Nullable ClassLoader classLoader)      // null -> getClassLoader(ArtifactDetector.class)
    @Nonnull @Immutable public List<Artifact> detect()              // 等价 detect(true)：包含 JDK 库
    @Nonnull @Immutable public List<Artifact> detect(boolean includedJdkLibraries)
    @Nonnull @Immutable public List<Artifact> detect(@Nullable Set<URL> classPathURLs)
    @Nullable public Artifact detect(@Nonnull Class<?> classInResource)
    @Nullable public Artifact detect(@Nonnull URL classPathURL)     // 第一个非 null 的解析器胜出
    protected Set<URL> getClassPathURLs(boolean includedJdkLibraries)
}

// ArtifactResourceResolver.java —— SPI
public interface ArtifactResourceResolver extends Prioritized {
    @Nullable Artifact resolve(@Nullable URL resourceURL);
}
public abstract class AbstractArtifactResourceResolver implements ArtifactResourceResolver {
    public AbstractArtifactResourceResolver(int priority)                       // 默认类加载器
    public AbstractArtifactResourceResolver(ClassLoader classLoader, int priority)
    @Override public final int getPriority()          // final：只能通过构造器设定
    protected final Logger logger; protected final ClassLoader classLoader; protected final int priority;
}
public abstract class StreamArtifactResourceResolver extends AbstractArtifactResourceResolver {
    public StreamArtifactResourceResolver(int priority)
    public StreamArtifactResourceResolver(ClassLoader classLoader, int priority)  // classLoader 为 null 抛 IllegalArgumentException
    @Override public final Artifact resolve(URL resourceURL)   // final：负责定位并打开元数据流
    protected abstract boolean isArtifactMetadata(String relativePath);
    protected abstract Artifact resolve(URL resourceURL, InputStream artifactMetadataData,
                                        ClassLoader classLoader) throws IOException;
}
public class MavenArtifactResourceResolver extends StreamArtifactResourceResolver {
    public static final int DEFAULT_PRIORITY = 1;
    public static final String MAVEN_POM_PROPERTIES_RESOURCE_PREFIX = "META-INF/maven/";
    public static final String MAVEN_POM_PROPERTIES_RESOURCE_SUFFIX = "/pom.properties";
    public MavenArtifactResourceResolver() / (int priority) / (ClassLoader, int)
}
public class ManifestArtifactResourceResolver extends StreamArtifactResourceResolver {
    public static final int DEFAULT_PRIORITY = 5;
    public ManifestArtifactResourceResolver() / (int priority) / (ClassLoader, int)
}
public class ArchiveFileArtifactResourceResolver extends AbstractArtifactResourceResolver {
    public static final int DEFAULT_PRIORITY = 9;
    public ArchiveFileArtifactResourceResolver() / (int priority) / (ClassLoader, int)
    @Override public Artifact resolve(URL resourceURL)   // 非 final
}

// URLClassPathHandle 家族
public interface URLClassPathHandle extends Prioritized {
    boolean supports();
    @Nonnull default URL[] getURLs(@Nullable ClassLoader classLoader)         // 经 findURLClassLoader，否则返回空数组
    default boolean initializeLoaders(@Nullable ClassLoader classLoader)      // findResource("just-for-initializing-loaders")
    boolean removeURL(@Nullable ClassLoader classLoader, @Nullable URL url);
    @Override default int getPriority() { return MIN_PRIORITY; }
}
public abstract class AbstractURLClassPathHandle implements URLClassPathHandle, Prioritized {
    public static final int DEFAULT_PRIORITY = MAX_PRIORITY + 99999;   // Integer.MIN_VALUE + 99999：最高优先级
    @Override public boolean supports()                                // URLClassPath 类可解析
    @Override public URL[] getURLs(ClassLoader classLoader)             // 反射调用 ucp.getURLs()
    @Override public final boolean removeURL(ClassLoader classLoader, URL url)  // 对 urls 加锁；仅参数为 null 才返回 false（见第 6 条）
    public final void setPriority(int priority); public final int getPriority();
    protected abstract String getURLClassPathClassName();
    protected abstract String getUrlsFieldName();
}
public class ClassicURLClassPathHandle extends AbstractURLClassPathHandle {}   // sun.misc.URLClassPath / "urls"
public class ModernURLClassPathHandle extends AbstractURLClassPathHandle {}    // jdk.internal.loader.URLClassPath / "unopenedUrls"
public class NoOpURLClassPathHandle implements URLClassPathHandle {}           // supports()=true，空 URL，remove 返回 false
public class ServiceLoadingURLClassPathHandle implements URLClassPathHandle {} // ClassLoaderUtils 使用的 SPI 门面

// BannedArtifactClassLoadingExecutor.java
public class BannedArtifactClassLoadingExecutor {
    public static final String CONFIG_LOCATION = "META-INF/banned-artifacts";
    public BannedArtifactClassLoadingExecutor() / (@Nullable ClassLoader classLoader)
    public void execute()          // 探测非 JDK 构件，并移除命中的 classpath 条目
}
```

### 2.1 已注册的 SPI 实现（依据 service 文件核实）

`microsphere-java-core/src/main/resources/META-INF/services/io.microsphere.classloading.ArtifactResourceResolver`

```
io.microsphere.classloading.MavenArtifactResourceResolver
io.microsphere.classloading.ManifestArtifactResourceResolver
io.microsphere.classloading.ArchiveFileArtifactResourceResolver
```

`microsphere-java-core/src/main/resources/META-INF/services/io.microsphere.classloading.URLClassPathHandle`

```
io.microsphere.classloading.ClassicURLClassPathHandle
io.microsphere.classloading.ModernURLClassPathHandle
io.microsphere.classloading.NoOpURLClassPathHandle
```

`StreamArtifactResourceResolver`、`AbstractArtifactResourceResolver` 与 `ServiceLoadingURLClassPathHandle`
**未被注册**——前两者是抽象基类，第三个由 `ClassLoaderUtils` 直接实例化。

| 解析器 | 优先级 | 读取内容 | 产出 |
|---|---|---|---|
| `MavenArtifactResourceResolver` | 1 | 归档**根目录下**第一个 `META-INF/maven/**/pom.properties` | `MavenArtifact`（含真实 `groupId`） |
| `ManifestArtifactResourceResolver` | 5 | `META-INF/MANIFEST.MF` 主属性 | `Artifact`；若无任何 artifact-id 属性命中则返回 `null` |
| `ArchiveFileArtifactResourceResolver` | 9 | 归档**文件名**，格式 `[artifactId]-[version].ext` | `Artifact`；文件名无连字符时 version 为 `null` |

---

## 3. 示例（改写自真实测试）

**探测某个类来自哪个构件** —— 见
`microsphere-java-core/src/test/java/io/microsphere/classloading/ArtifactDetectorTest.java:53-65`：

```java
ArtifactDetector detector = new ArtifactDetector();
Artifact self = detector.detect(ArtifactDetector.class);   // 处于展开的 target/classes 目录时为 null
Artifact jsr305 = detector.detect(javax.annotation.Nonnull.class);
// 测试断言：jsr305.getArtifactId() == "jsr305"，jsr305.getVersion() == "3.0.2"
```

**整体扫描 classpath，含或不含 JDK 条目** —— 同文件 `:46-51` 与 `:90-101`：

```java
List<Artifact> all = detector.detect();                    // detect(true)：包含 JDK 条目
List<Artifact> appOnly = detector.detect(false);           // 剔除引导类路径与 java.home 条目
appOnly.forEach(a -> System.out.printf("%s %s%n", a.getArtifactId(), a.getVersion()));
```

**直接解析单个归档** —— `MavenArtifactResourceResolverTest.java:16-24` 与
`StreamArtifactResourceResolverTest.java:46-67`：

```java
ArtifactResourceResolver resolver = new MavenArtifactResourceResolver();
Artifact artifact = resolver.resolve(classResourceUrlOf(javax.annotation.Nonnull.class));
assertTrue(artifact instanceof MavenArtifact);             // com.google.code.findbugs:jsr305:3.0.2
```

**文件名兜底** —— `ArchiveFileArtifactResourceResolverTest.java:50-63`：

```java
// 测试直接调用了包私有辅助方法
Artifact a = ArchiveFileArtifactResourceResolver.createArtifact("a-1.0.jar", null); // a / "1.0"
Artifact b = ArchiveFileArtifactResourceResolver.createArtifact("a.jar", null);     // a / null
```

**移除 classpath 条目** —— `ServiceLoadingURLClassPathHandleTest.java:61-69` 会把当前 classpath 复制进一个临时
`URLClassLoader`，再通过 handle 移除全部条目；`BaseURLClassPathHandleTest.java:58-69` 仅在
`handle.supports()` 成立时移除 `jmh-generator-annprocess` 所在的 jar。

**启动时禁用某构件** —— `BannedArtifactClassLoadingExecutorTest.java:38-42`：

```java
new BannedArtifactClassLoadingExecutor(classLoader).execute();
```

```
# src/main/resources/META-INF/banned-artifacts —— 每行必须是恰好三个以冒号分隔的字段
commons-logging:commons-logging:1.2
log4j:log4j:*
```

---

## 4. 陷阱

1. **阴影（shade）／胖 jar。** `StreamArtifactResourceResolver.findArtifactMetadataEntry(...)` 返回归档中**第一个**
   命中的条目（`StreamArtifactResourceResolver.java:179-182`），因此被 shade 进来的依赖中，哪个 `pom.properties`
   先被 `JarFile` 枚举到就上报哪个坐标——而不是你自己的构件。要么重定位元数据，要么接受 Manifest／文件名兜底。
2. **嵌套 jar 不可见。** `MavenArtifactResourceResolver.isArtifactMetadata(...)` 要求
   `relativePath.indexOf("META-INF/maven/") == 0`（`MavenArtifactResourceResolver.java:101-110`），Spring Boot 的
   `BOOT-INF/lib/*.jar` 条目永远不满足；而对 `jar:file:/app.jar!/BOOT-INF/lib/x.jar!/` 这类 URL，
   `URLUtils.resolveArchiveFile(...)` 得到的 `File` 并不存在，于是返回 `null`，而默认的
   `readArtifactMetadataDataFromResource(...)` 也返回 `null`（`StreamArtifactResourceResolver.java:112-139`）。
   结论：**嵌套条目探测不到任何构件**。
3. **OSGi 与其它启动器。** Bundle 通常只有 `MANIFEST.MF` 可用，因此识别完全依赖 `Bundle-Name`/`Bundle-Version`；
   若你的 bundle 使用 `Bundle-SymbolicName`，需要通过
   `microsphere.artifact-id.manifest-attribute-names` 加入，否则解析器返回 `null`
   （`ManifestArtifactResourceResolver.java:202-214`）。
4. **展开目录。** 从 `target/classes`、`build/classes` 或 IDE 输出目录加载的类通常得到 `null`
   （`ArtifactDetectorTest.java:58-59` 即为此断言）；文件名解析器直接拒绝目录
   （`ArchiveFileArtifactResourceResolver.java:86-89`）。
5. **JDK 9+ 的系统加载器不是 `URLClassLoader`。** `URLClassPathHandle.getURLs(...)` 的默认实现回落到
   `ClassLoaderUtils.findURLClassLoader(...)`，返回 `null` 时给出 `EMPTY_URL_ARRAY`。`Abstract` 系列通过读取私有
   `ucp` 字段绕开这一点，但 `ClassLoaderUtils.removeClassPathURL(...)` 仍会拒绝非 `SecureClassLoader`
   （`ClassLoaderUtils.java:1598-1603`）。
6. **强封装下 `removeURL` 是抛异常，而不是返回 `false`。** JDK 17/21/25 上 `jdk.internal.loader` 的私有内部默认不开放。
   若在不加 `--add-opens java.base/jdk.internal.loader=ALL-UNNAMED` 的情况下对应用类加载器调用 `removeURL(...)`
   （无论是直接调用还是经 `ClassLoaderUtils.removeClassPathURL(...)`），反射读取会被拒绝并向外抛出
   `IllegalStateException`（包装 `IllegalAccessException`，`FieldUtils.java:826-841`）；若加载器根本没有这个字段，
   `removeURL` 会在 `AbstractURLClassPathHandle.java:147` 抛 `NullPointerException`。只有参数为 `null` 时才干净地返回
   `false`。因此请加上 `--add-opens`，或在调用处 try/catch。
7. **`detect(true)` 会包含 JDK 条目**，其 id/version 多为 `UNKNOWN` 或来自 manifest，这是预期输出而非缺陷；
   需要只看应用依赖时使用 `detect(false)`。
8. **禁用清单会被提前截断。** 读取在遇到第一个空行时终止（`BannedArtifactClassLoadingExecutor.java:117-125`），
   且任何不足恰好三个字段的行都会抛 `IllegalArgumentException`（`:129-138`）。**不支持注释行**——`# ...` 会解析失败。
9. **`execute()` 不会报告禁用结果。** 命中的构件会经 `ClassLoaderUtils.removeClassPathURL(...)` 摘除（`:84-95`），
   而返回值被忽略：目标加载器不是 `SecureClassLoader` 时返回 `false`（静默空操作），但加载器内部被封装时则会**抛异常**
   （见第 6 条），因为 `execute()` 什么都不捕获。要把它当护栏用时请在集成测试里校验结果。
10. **通配符按字段生效。** `Artifact.WILDCARD` 只对单个字段短路（`Artifact.java:115-122`）——`*:*:*` 禁用一切，
    `foo:*:1.0` 禁用 `foo` 的任意版本。

---

## 5. 另见

* [工具类、常量与文本](util-and-constants.md) —— `ClassLoaderUtils`、`ClassPathUtils`、`ServiceLoaderUtils`
* [网络与 URL 协议](net-and-url-protocols.md) —— `URLUtils.resolveArchiveFile` / `resolveBasePath`
* [反射、类型与 JavaBean](reflection-and-beans.md) —— `Abstract*URLClassPathHandle` 所需的字段访问能力
* [语言抽象](language-abstractions.md) —— `Prioritized`，两组 SPI 的排序依据
* 开发者视角的深入解析：[类加载与构件探测 —— 开发者指南](../../../developer-guide/microsphere-java-core/zh/classloading-and-artifacts.md)
