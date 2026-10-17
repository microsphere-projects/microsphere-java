# 工具类、常量与文本处理

| | |
|---|---|
| **构件** | `io.github.microsphere-projects:microsphere-java-core`（`0.3.20-SNAPSHOT`，`release` 分支） |
| **包** | `io.microsphere.util`（34 个类型）· `io.microsphere.util.jar`（1 个）· `io.microsphere.constants`（8 个）· `io.microsphere.text`（1 个）· `io.microsphere.misc`（1 个）—— 共 45 个主源码类型 |
| **JDK 级别** | 以 Java 8 编译；CI 在 JDK 8 / 11 / 17 / 21 / 25（Temurin）上测试 |
| **编译依赖** | `microsphere-java-annotations`（`@Nonnull` / `@Nullable` / `@Immutable` / `@ConfigurationProperty`）—— 不依赖任何其他 core 包 |
| **标记约定** | 工具类均为 `abstract` + 私有构造函数 + `implements io.microsphere.util.Utils`；见 [`Utils`](#核心类型标记接口) |

本切片是 `microsphere-java-core` 的**基础层**。它收纳其他所有包所依赖的底层助手：类加载器与资源解析
（`ClassLoaderUtils`）、classpath 自省（`ClassPathUtils`）、对 `java.util.ServiceLoader` 的服务加载门面
（`ServiceLoaderUtils`）、版本建模（`Version` + `VersionUtils`）、数组/字符串/断言原语（`ArrayUtils`、`StringUtils`、
`Assert`、`SystemUtils`）、文本占位符格式化（`FormatUtils`）、JAR 访问（`util.jar.JarUtils`）、`sun.misc.Unsafe`
访问（`misc.UnsafeUtils`），以及共享常量接口（`io.microsphere.constants`）。

**当你需要一个空值安全、跨 JDK 版本可移植的方式来完成以下事情时，就应当使用这些类型：** 在类加载器层级中按名称解析类
或资源、按优先级顺序枚举或加载 SPI 提供者、比较某个库或 JDK 的版本、对字符串做切分/空白判断、构造空数组、读取 JAR
清单，或访问 `Unsafe` —— 而无需自己编写那些脆弱的反射代码。如果这里的某个助手已经能满足需求，就优先使用它而非 Apache
Commons 或手写的反射逻辑：其行为由库自身的测试套件锁定，并针对真实源码做了文档化。

> [!NOTE]
> 这些工具类**有意采取“降级而非抛异常”的策略**：当输入不存在时，大多数查找会返回 `null` / 空集合 / 中性的 `boolean`。
> 这是刻意的设计（见[陷阱](#6-陷阱与已知限制)），但确有少数方法*会*抛出异常 —— 下面每一处都已明确标注，并附行号引用。

---

## 1. 本组用途及使用时机

| 我想…… | 使用 | 相比直接用 JDK 的理由 |
|---|---|---|
| 按名称加载类，遍历完整的加载器链 | `ClassLoaderUtils.loadClass` / `resolveClass` | 处理基本类型、`null` 加载器，并缓存结果（`util/ClassLoaderUtils.java:547`、`:1426`） |
| 查找 `.class` / 包 / 任意资源的 `URL` | `ClassLoaderUtils.getResource(...)` + `ResourceType` | 查找前按资源种类规范化名称（`util/ClassLoaderUtils.java:783`、`:843`、`:1931`） |
| 列出某个加载器的 classpath 条目 | `ClassLoaderUtils.findAllClassPathURLs` | 委派给感知 JDK 版本的 `URLClassPathHandle`（`util/ClassLoaderUtils.java:1550-1564`） |
| 按优先级顺序加载 SPI 提供者 | `ServiceLoaderUtils.loadServicesList` / `loadFirstService` | 按 `Prioritized` 排序，可选缓存（`util/ServiceLoaderUtils.java:890`、`:846`） |
| 获知当前运行的 JDK 版本 | `VersionUtils.CURRENT_JAVA_VERSION` | 解析 `java.version`，兼容旧式与新式方案（`util/VersionUtils.java:266`、`:284`） |
| 比较库版本 | `Version.ofVersion(String)` | 不可变、`Comparable`、宽松的分段解析（`util/Version.java:595`） |
| 安全地读取系统属性 | `SystemUtils.getSystemProperty` | 容忍 `SecurityException`，并缓存一份副本（`util/SystemUtils.java:605`） |
| 切分 / 空白判断字符串 | `StringUtils.split` / `isBlank` | 空值安全，保留空 token（`util/StringUtils.java:163`、`:81`） |
| 共享字面量值 | `io.microsphere.constants.*` | 分隔符、协议、符号的单一事实来源（`constants/Constants.java:13-14`） |
| 格式化 `{} 占位符` | `text.FormatUtils.format` | SLF4J 风格的替换（`text/FormatUtils.java:38`） |
| 从资源 URL 打开一个 JAR | `util.jar.JarUtils.toJarFile` | 解析归档绝对路径，未命中返回 `null`（`util/jar/JarUtils.java:86`） |
| 访问 `sun.misc.Unsafe` | `misc.UnsafeUtils` | 反射获取 `theUnsafe`，并缓存偏移量（`misc/UnsafeUtils.java:34`、`:564`） |

---

## 2. 坐标与版本

Maven 坐标（来自反应器 POM）：

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- 最新已发布标签；0.3.20-SNAPSHOT 是同一版本线的开发版本 -->
</dependency>
```

| 字段 | 值 | 证据 |
|---|---|---|
| Group ID | `io.github.microsphere-projects` | `pom.xml:12` |
| Artifact ID | `microsphere-java-core` | `microsphere-java-core/pom.xml`（模块见 `pom.xml:61`） |
| Version | `${revision}`，在 `release` 上为 `0.3.20-SNAPSHOT` | `pom.xml:54` |
| 父（构建） | `microsphere-build:0.3.16` | `pom.xml:9` |
| 编译依赖 | `microsphere-java-annotations` | `microsphere-java-core/pom.xml`（模块 `pom.xml:60`） |
| 字节码目标 | Java 8 | 整个 reactor 的 POM 中不存在任何 `maven.compiler.*` / `<release>` 属性，因此编译级别继承自外部父 POM `microsphere-build:0.3.16`；并由下方 CI 矩阵交叉印证 |
| CI JDK 矩阵 | 8、11、17、21、25（Temurin） | `.github/workflows/maven-build.yml:25`、`:33` |
| 可直接引用的 BOM | `microsphere-java-dependencies`（仓库内；恰好管理 7 个含代码的模块） | `microsphere-java-dependencies/pom.xml` |
| 导入的 BOM | `microsphere-all-bom:0.3.13` + `spring-framework-bom` | `microsphere-java-parent/pom.xml:22`、`:33-36`、`:40-46` |
| Spring Framework 兼容范围 | JDK `[1.8,17)` 时经 `java8-16` profile 使用 5.3.39；其余为 7.0.9 | `microsphere-java-parent/pom.xml:23`、`:55-62` |

> [!TIP]
> CI 运行 `mvn ... -Drevision=0.0.1-SNAPSHOT ... -Dsurefire.useSystemClassLoader=false test`
> （`.github/workflows/maven-build.yml:38-45`）。那里的 `-Drevision` 是构建时覆盖值，并非发布版本；`release` 分支上
> 待发布的版本仍为 `0.3.20-SNAPSHOT`。`useSystemClassLoader=false` 标志与这些工具类相关，因为若干测试所断言的行为
> 取决于哪个类加载器能看到测试资源。

---

## 3. API 参考

完整路径只给出一次；后续引用将 `microsphere-java-core/src/main/java/io/microsphere/` 缩短为最后两段（例如
`util/ClassLoaderUtils.java`）。签名均摘自 `release` 源码。

### 核心类型：标记接口

```java
// util/Utils.java —— 一个空的标记接口，@since 1.0.0
public interface Utils {}
```

`Utils` 不携带任何方法。它的约定是惯例性的、而非强制的：一个“工具类”被声明为 `abstract`、给出私有构造函数、并
`implements Utils`。本切片几乎每个类都遵循它 —— `ClassLoaderUtils`、`ServiceLoaderUtils`、`ClassPathUtils`、
`ArrayUtils`、`StringUtils`、`SystemUtils`、`NumberUtils`、`VersionUtils`、`text.FormatUtils`、`util.jar.JarUtils`、
`misc.UnsafeUtils`。

> [!WARNING]
> `util/Assert.java` 是 `abstract` 且带私有构造函数，但**并未**实现 `Utils` —— 这是刻意的例外，因为它抛异常而非降级。
> `util/Version.java` 同样*不是*工具类：它是一个不可变值对象（`@Immutable`、`Comparable<Version>`、`Serializable`）。

### `ClassLoaderUtils`（util/ClassLoaderUtils.java）—— 类与资源解析

```java
public abstract class ClassLoaderUtils implements Utils {          // @since 1.0.0，私有构造函数
    protected static final ClassLoadingMXBean classLoadingMXBean; // :90
    private static final ConcurrentMap<String,Class<?>> loadedClassesCache = new ConcurrentHashMap<>(256); // :92
    private static final URLClassPathHandle urlClassPathHandle;    // :94（ServiceLoadingURLClassPathHandle）

    @Nonnull public static ClassLoader getDefaultClassLoader();                          // :244
    @Nonnull public static ClassLoader getClassLoader(@Nullable Class<?> loadedClass);   // :298
    @Nullable public static Class<?> loadClass(@Nonnull ClassLoader classLoader, @Nonnull String className);            // :547
    @Nullable public static Class<?> loadClass(@Nonnull ClassLoader classLoader, @Nonnull String className, boolean cached); // :589
    @Nullable public static Class<?> doLoadClass(@Nullable ClassLoader classLoader, @Nullable String className);         // :620
    @Nonnull public static Set<URL> getResources(@Nullable ClassLoader classLoader, @Nonnull String name) throws IOException; // :707
    @Nonnull public static Set<URL> getResources(@Nullable ClassLoader classLoader, @Nonnull ResourceType type, @Nonnull String name) throws IOException; // :669
    @Nullable public static URL getResource(@Nonnull ClassLoader classLoader, @Nonnull String resourceName);            // :783
    @Nullable public static URL getResource(@Nullable ClassLoader classLoader, @Nonnull ResourceType resourceType, @Nonnull String resourceName); // :843
    @Nullable public static URL getClassResource(@Nullable ClassLoader classLoader, @Nonnull Class<?> classType);       // :1035
    @Nullable public static String getResourceAsString(@Nullable ClassLoader classLoader, @Nullable String name);       // :875
    @Nonnull public static Set<ClassLoader> getInheritableClassLoaders(@Nullable ClassLoader classLoader);              // :1069
    @Nonnull public static Set<URL> findAllClassPathURLs(@Nullable ClassLoader classLoader);                            // :1550
    public static boolean removeClassPathURL(@Nullable ClassLoader classLoader, @Nullable URL url);                       // :1598
    @Nullable public static ClassLoader findURLClassLoader(@Nonnull ClassLoader classLoader);                            // :1635
    @Nonnull public static URLClassLoader newURLClassLoader(@Nonnull URL[] urls, @Nonnull ClassLoader parent);           // :1667（重载 :1699..:1781）
    @Nullable public static Class<?> resolveClass(@Nullable String className);                                            // :1426（重载 :1466、:1511）
    public static boolean isPresent(@Nonnull String className);                                                            // :1369 / :1399
}
```

| 行为 | 细节 | 证据 |
|---|---|---|
| `null` 类加载器 | `nullSafeClassLoader` 以**系统**加载器替代（`:218`）；`getDefaultClassLoader` 依次尝试线程上下文 → 本类加载器 → 默认加载器（`:244`） | `util/ClassLoaderUtils.java:218`、`:244` |
| 空白 / 无法解析的名称 | `doLoadClass` 对空白名返回 `null`，并在加载期间吞掉任何 `Throwable`（以 trace 记录），随后尝试解析基本类型（`:620`） | `util/ClassLoaderUtils.java:620` |
| 缓存 | `loadClass(...,true)` 以 `className + classLoader.hashCode()` 为键记忆到 `loadedClassesCache`（`buildCacheKey` `:1902`） | `util/ClassLoaderUtils.java:589`、`:1902` |
| 资源顺序 | `getResource(cl,name)` 遍历 `ResourceType.values()` 返回第一个非 `null`；`getResources` 返回第一个非空集合（`:783`、`:707`） | `util/ClassLoaderUtils.java:707`、`:783` |
| 抛异常的方法 | `getResources(...)` 声明 `IOException` 与 `NullPointerException`（`:669`、`:707`）；`newURLClassLoader(...)` 在 URL 数组含 `null` 元素时抛 `IllegalArgumentException`（`assertNoNullElements`，`:1667`） | `util/ClassLoaderUtils.java:669`、`:1667` |
| 线程安全 | `loadedClassesCache` 为 `ConcurrentHashMap`；`classLoadingMXBean` 与句柄均为 `static final` —— 可安全发布（`:90`、`:92`、`:94`） | `util/ClassLoaderUtils.java:90-94` |

`ResourceType`（`util/ClassLoaderUtils.java:1931`）是一个嵌套枚举，在查找前既校验又规范化资源名：

| 常量 | 接受 | 规范化 | 证据 |
|---|---|---|---|
| `DEFAULT` | 任意（`supports()` = `true`） | 原样 | `:1945` |
| `CLASS` | 以 `.class` 结尾的名称，或点分式类名 | 点号 → 斜杠 | `:1983` |
| `PACKAGE` | 不含 `/` 或 `\`、且非 `.class` 的名称 | 点分包名形式 | `:2030` |
| `resolve(name)` | —— | 当空白或不支持时返回 `null`，否则规范化并去掉前导 `/` | `:2086` |

### `ServiceLoaderUtils`（util/ServiceLoaderUtils.java）—— 基于 `java.util.ServiceLoader` 的 SPI

```java
public abstract class ServiceLoaderUtils implements Utils {              // @since 1.0.0
    public static final String SERVICES_PROVIDER_LOCATION = "META-INF/services/";  // :119
    public static final String SERVICE_LOADER_CACHED_PROPERTY_NAME = /* "microsphere." */ "...service-loader.cached"; // :112
    private static final boolean SERVICE_LOADER_CACHED;                  // :138，由 -D 解析，默认 "false"（:107）
    private static final ConcurrentMap<Class<?>, List<?>> servicesCache; // :140

    @Nonnull public static <T> List<T> loadServicesList(@Nonnull Class<T> serviceType, @Nullable ClassLoader cl, boolean cached); // :382..:468
    @Nonnull public static <T> T[] loadServices(@Nonnull Class<T> serviceType, @Nullable ClassLoader cl, boolean cached);          // :493..:576
    @Nullable public static <T> T loadFirstService(@Nonnull Class<T> serviceType, @Nullable ClassLoader cl, boolean cached);       // :602..:693
    @Nullable public static <T> T loadLastService(@Nonnull Class<T> serviceType, @Nullable ClassLoader cl, boolean cached);        // :719..:805
    @Nonnull public static Set<Class<T>> getServiceClasses(@Nonnull Class<T> serviceType, boolean failFast);                        // :262
    @Nonnull public static Set<URL> getServiceResoources(@Nonnull Class<?> serviceType, @Nonnull ClassLoader classLoader);         // :355（注意：源码名称本身拼写有误）
}
```

| 行为 | 细节 | 证据 |
|---|---|---|
| 排序 | 提供者被加载后用 `Prioritized.COMPARATOR` 排序；first/last 即该有序列表的 `ListUtils.first/last`（`:890`、`:828`） | `util/ServiceLoaderUtils.java:890` |
| 缓存键 | 当 `cached`（或全局标志开启）时，`servicesCache.computeIfAbsent(serviceType, ...)` —— **仅以服务类型为键**，不含类加载器（`:846`） | `util/ServiceLoaderUtils.java:843-846` |
| 零提供者 | 当 `ServiceLoader` 迭代器为空时，`loadServicesAsList(type, cl)` 抛 `IllegalArgumentException`（`:882-886`）—— **这是一次真实抛出，而非空列表** | `util/ServiceLoaderUtils.java:882-886` |
| `null` 类加载器 | 回退到 `getClassLoader(...)`（`:873`） | `util/ServiceLoaderUtils.java:873` |
| 不可赋值 / 无法加载的提供者 | `getServiceClasses(type, true)` 抛 `IllegalStateException`（`:270`、`:276`）；`failFast=false` 时返回空集合 | `util/ServiceLoaderUtils.java:262` |

> [!WARNING]
> 该方法在源码中名为 `getServiceResoources`（三个 `o`）—— 请原样复制（`util/ServiceLoaderUtils.java:355`）。
> 缓存仅以服务类型为键，因此**切勿**先调用 `loadServicesList(type, loaderA, true)`、再调用
> `loadServicesList(type, loaderB, true)` 并期望得到不同结果 —— 第二次调用返回的是 loaderA 缓存的列表（`:846`）。

### `ClassPathUtils`（util/ClassPathUtils.java）—— classpath 自省

```java
public abstract class ClassPathUtils implements Utils {           // @since 1.0.0
    protected static final RuntimeMXBean runtimeMXBean;           // :58
    public static final Set<String> bootstrapClassPaths;          // :61（不可变，仅计算一次）
    public static final Set<String> classPaths;                   // :64
    @Nonnull public static Set<String> getBootstrapClassPaths();  // :127
    @Nonnull public static Set<String> getClassPaths();           // :149
    @Nullable public static String getRuntimeClassLocation(@Nonnull Class<?> classType); // :207
}
```

| 行为 | 细节 | 证据 |
|---|---|---|
| JDK 9+ 的引导路径 | `initBootstrapClassPaths` 调用 `resolveClassPaths(runtimeMXBean.isBootClassPathSupported(), ...)`；当不支持时（JDK 9+）集合为**空** | `util/ClassPathUtils.java:68`、`:94` |
| 应用 classpath | `initClassPaths` 始终以 `File.pathSeparator` 切分 `getClassPath()` | `util/ClassPathUtils.java:73`、`:103` |
| 一次性不可变 | 两个字段都是类初始化时计算的 `static final` 集合 —— 快照值，永不刷新 | `util/ClassPathUtils.java:61`、`:64` |

### `Version`（util/Version.java）—— 不可变版本值对象

```java
@Immutable
public class Version implements Comparable<Version>, Serializable {   // @since 1.0.0
    private final int major;
    private final int minor;
    private final int patch;
    @Nullable private final String preRelease;                        // :95-102
    public Version(int major, int minor, int patch, @Nullable String preRelease); // :165
    public static Version of(int... versionParts);                    // :441..
    @Nonnull public static Version ofVersion(@Nonnull String version);// :595
    @Nonnull public static Version ofVersion(@Nonnull Class<?> classType); // :626-640
    public boolean gt(@Nullable Version v) /* ge/lt/le/eq + isGreaterThan/… */; // :220-317
    @Override public int compareTo(@Nonnull Version other);           // :372
    public static int getValue(@Nonnull String version);              // :661（包级助手）
    public enum Operator implements BiPredicate<Version,Version> { EQ,LT,LE,GT,GE; public static Operator of(String symbol); } // :671、:766
}
```

| 行为 | 细节 | 证据 |
|---|---|---|
| `ofVersion(String)` 解析 | 先按 `-` 切出预发布标识，再按 `.` 切出 `major[.minor[.patch]]`；缺失的 minor/patch 默认为 `0`；多余的点分段被忽略 | `util/Version.java:595` |
| 空值比较 | 每个 `gt/lt/le/ge/eq` 对 `null` 参数返回 `false`（`Operator` 处理 `null`） | `util/Version.java:220-317`、`:671` |
| 预发布排序 | 正式发布版本排在对应预发布版本**之后**（`comparePreRelease`：`null` > 非空） | `util/Version.java:403` |
| 非法输入 | `ofVersion(String)` 对 null/空白抛 `IllegalArgumentException`；`getValue` 对非数字抛 `IllegalArgumentException`；构造函数断言非负且非全零 | `util/Version.java:165`、`:595`、`:661` |
| 从类解析 | `ofVersion(Class)` 使用调用方的加载器；对引导加载器加载的类返回 `VersionUtils.CURRENT_JAVA_VERSION`；当构件无版本时抛 `IllegalArgumentException` | `util/Version.java:626-640` |

### `VersionUtils`（util/VersionUtils.java）—— JDK 版本常量与比较

```java
public abstract class VersionUtils implements Utils {                 // @since 1.0.0
    public static final SourceVersion LATEST_JAVA_VERSION;            // :39（javax.lang.model.SourceVersion）
    public static final Version CURRENT_JAVA_VERSION;                 // :44
    public static final Version JAVA_VERSION_8;  /* …直到 JAVA_VERSION_24 */ // :49-129
    public static boolean testCurrentJavaVersion(@Nonnull String operator, @Nonnull Version version); // :156
    public static boolean testVersion(@Nullable Version base, @Nullable Operator op, @Nullable Version compared); // :248
    public static String currentJavaMajorVersion();                   // :266
    public static String detectJavaMajorVersion(@Nonnull String javaVersion); // :284
}
```

| 行为 | 细节 | 证据 |
|---|---|---|
| 旧式与新式 `java.version` | `detectJavaMajorVersion` 将前导的 `"1"` 段视为旧式方案并返回下一段（`"1.8.0"` → `"8"`）；否则返回第一个点之前的段（`"17.0.0"` → `"17"`） | `util/VersionUtils.java:284` |
| 空值安全比较 | 任一参数为 `null` 时 `testVersion(...)` 返回 `false` | `util/VersionUtils.java:248` |
| 编译依赖 | 引用 `javax.lang.model.SourceVersion`（java.compiler 模块） | `util/VersionUtils.java:39` |

### `StringUtils`（util/StringUtils.java）、`ArrayUtils`（util/ArrayUtils.java）、`Assert`（util/Assert.java）、`SystemUtils`（util/SystemUtils.java）

```java
public abstract class StringUtils implements Utils {                    // :81 isBlank，:113 isNotBlank，:137/:163 split，:221 contains
    public static final String EMPTY = "";                              // :50
    public static final String[] EMPTY_STRING_ARRAY = {};               // :61
    public static final int INDEX_NOT_FOUND = -1;                       // :294
}
public abstract class ArrayUtils implements Utils {                    // :211 of，:375 ofArray，:396 nullSafeArray，:414.. length
    public static final boolean[] EMPTY_BOOLEAN_ARRAY;  /* 约 24 个 EMPTY_* 常量 */ // :55..
    public static boolean contains(int[] array, int value);             // :1911（binarySearch —— 数组必须已排序）
    public static boolean contains(Object[] array, Object obj);         // :2096（线性扫描）
}
public abstract class Assert {                                          // :60 assertTrue … :149 assertNotNull … :348 assertNoNullElements（抛 IAE）
    public static <T> T assertNotNull(@Nullable T object, String message, Object... args); // :131
}
public abstract class SystemUtils implements Utils {                    // :347 IS_OS_WINDOWS … :357 IS_JAVA_8 … :522 IS_LTS_JAVA_VERSION
    @Nullable public static String getSystemProperty(@Nonnull String key, @Nullable String defaultValue); // :605
}
```

| 类型 | 空值处理 | 是否抛异常 | 线程安全 |
|---|---|---|---|
| `StringUtils.isBlank` | `null`/空/全空白 → `true`（`:81`） | 否 | 无状态 |
| `StringUtils.split(v, delim)` | `null`/空值 → `EMPTY_STRING_ARRAY`；`null` 分隔符 → 含整串的单元数组；保留空 token（`:163`） | 否 | 无状态 |
| `ArrayUtils.contains(int[],int)` | 数组为 `null` 时返回 `false` | 否 | 无状态 —— **但使用 `Arrays.binarySearch`，除非数组已排序否则结果会错**（`:1911`） |
| `Assert.*` | 对象为 `null` 时抛 `IllegalArgumentException`（`:131`） | 是 —— 该类存在的意义就是抛异常 | 无状态；注意 `assertNoNullElements` 在数组/迭代器为 `null` 或空时是跳过而非失败（`:348`） |
| `SystemUtils.getSystemProperty` | 属性缺失或捕获到 `SecurityException` 时返回默认值（`:605`） | 否（记录一条警告） | `systemPropertiesCopy` 是普通的非 `volatile` `Map`；`copySystemProperties()` 以 `synchronized (SystemUtils.class)` 保护（`:528`、`:548`） |
| `SystemUtils` 标志 | `IS_JAVA_*` 由 `java.specification.version` 计算（`:357`、`:638`），而非 `java.version` | 否 | 不可变 `static final` |

> [!NOTE]
> `NumberUtils`（util/NumberUtils.java）是 `abstract … implements Utils`、带私有构造函数、`@since 1.0.0`，
> 但目前**不暴露任何公有方法** —— 它是一个预留占位类。不要在其中寻找解析助手。

### `text.FormatUtils`、`util.jar.JarUtils`、`misc.UnsafeUtils`

```java
public abstract class FormatUtils implements Utils {                    // text/FormatUtils.java
    public static final String DEFAULT_PLACEHOLDER = "{}";              // :17
    @Nullable public static String format(@Nullable String pattern, Object... args); // :38 → formatWithPlaceholder（:62）
}
public abstract class JarUtils implements Utils {                       // util/jar/JarUtils.java
    public static final String MANIFEST_RESOURCE_PATH = "META-INF/MANIFEST.MF"; // :63
    @Nullable public static JarFile toJarFile(@Nonnull URL url) throws NullPointerException, IllegalArgumentException; // :86
    @Nonnull @Immutable public static List<JarEntry> filter(@Nullable JarFile jarFile, @Nullable JarEntryFilter filter); // :193
}
public abstract class UnsafeUtils implements Utils {                    // misc/UnsafeUtils.java
    public static final String UNSAFE_CLASS_NAME = "sun.misc.Unsafe";   // :34
    private static final ConcurrentMap<String,Long> offsetCache = new ConcurrentHashMap<>(); // :559
}
```

| 类型 | 行为 | 证据 |
|---|---|---|
| `FormatUtils.format` | 空白模式串 → 原样返回；零参数 → 模式串不变；每个参数替换**剩余的第一个** `{}`；多余参数被忽略，多余占位符保持字面（`:62`，循环 `:73-80`） | `text/FormatUtils.java:38`、`:62-80` |
| `JarUtils.toJarFile` | `null` URL → `NullPointerException`；非 `jar:`/`file:` 协议 → `IllegalArgumentException`（`assertJarURLProtocol` `:111`）；无法解析的归档 → `null`（记录，吞掉 `IOException`）（`:86`） | `util/jar/JarUtils.java:86`、`:111`、`:168` |
| `JarUtils.filter` | `null` `JarFile` → 共享的 `emptyList()`（`:193`） | `util/jar/JarUtils.java:193` |
| `UnsafeUtils` 类初始化 | `getUnsafe()` 反射获取 `theUnsafe`；失败时抛 `UnsupportedOperationException`，在没有 `sun.misc.Unsafe` 的 JDK 上表现为 `ExceptionInInitializerError`（`:564`、`:570-572`） | `misc/UnsafeUtils.java:564`、`:570-572` |

### `io.microsphere.constants` —— 共享字面量接口

八个接口，全部 `@since 1.0.0`，由 `Constants` 聚合，使得一条 `import static io.microsphere.constants.Constants.*`
即可引入所有字面量（`constants/Constants.java:13-14`）。

| 接口 | 代表性常量 | 证据 |
|---|---|---|
| `SymbolConstants` | `COMMA = ","` `:184`、`SPACE = " "` `:189`、`DOT = "."` `:233`、`HYPHEN = "-"` `:308`、`EQUAL = "="` `:243`、`LESS_THAN_OR_EQUAL_TO = "<="` `:253`、`GREATER_THAN_OR_EQUAL_TO = ">="` `:263`；以及 char 孪生常量（`COMMA_CHAR` `:30` 等） | `constants/SymbolConstants.java` |
| `SeparatorConstants` | `ARCHIVE_ENTRY_SEPARATOR = "!/"` `:26`、`FILE_SEPARATOR` `:31`、`PATH_SEPARATOR` `:36`、`LINE_SEPARATOR` `:41`（取自 `File`/`System`） | `constants/SeparatorConstants.java` |
| `PathConstants` | `SLASH = "/"` `:28`、`DOUBLE_SLASH` `:33`、`BACK_SLASH` `:38`、`SLASH_CHAR` `:18` | `constants/PathConstants.java` |
| `ProtocolConstants` | `FILE_PROTOCOL = "file"` `:24`、`JAR_PROTOCOL` `:49`、`CLASSPATH_PROTOCOL = "classpath"` `:64`、`CONSOLE_PROTOCOL = "console"` `:69` | `constants/ProtocolConstants.java` |
| `FileConstants` | `JAR = "jar"` `:26`、`FILE_EXTENSION = "."` `:51`、`CLASS_EXTENSION = ".class"` `:76` | `constants/FileConstants.java` |
| `PropertyConstants` | `MICROSPHERE_PROPERTY_NAME_PREFIX = "microsphere."` `:35`、`ENABLED_PROPERTY_NAME = "enabled"` `:30` | `constants/PropertyConstants.java` |
| `ResourceConstants` | `METADATA_RESOURCE = "META-INF/"` `:33`、`MICROSPHERE_METADATA_RESOURCE` `:38` | `constants/ResourceConstants.java` |
| `Constants` | 仅聚合（`extends` 其余六个） | `constants/Constants.java:13-14` |

---

## 4. 可运行示例（改编自真实测试）

以下片段均摘编自 `microsphere-java-core/src/test/java/io/microsphere/` 下的测试体。每段代码上方的引用标注了文件名与行号。

**解析、比较与排序版本** —— `util/VersionTest.java:117-122`、`:190-221`、`:240-242`：

```java
Version v = Version.of("1.2.3");                 // of/ofVersion 均可解析
assertTrue(v.gt(Version.of(1, 2, 2)));           // 严格大于
assertFalse(v.gt(v));                            // 不大于自身
assertFalse(v.gt(null));                         // 空值安全 → false，绝不抛异常
Version alpha = Version.of("1.2.3-alpha");
assertTrue(v.gt(alpha));                         // 正式版排在其预发布版之上
```

**从某个类的 class 解析库版本** —— `util/VersionTest.java:124-137`、`:316-324`：

```java
Version jsr305 = Version.ofVersion(javax.annotation.Nullable.class); // "3.0.2"
assertEquals("3.0.2", jsr305.toString());
assertSame(VersionUtils.CURRENT_JAVA_VERSION, Version.ofVersion(String.class)); // 引导加载器 → 当前运行的 JDK
assertThrows(IllegalArgumentException.class, () -> Version.ofVersion(Version.class)); // 无版本元数据 → 抛异常
```

**旧式与新式 `java.version` 检测** —— `util/VersionUtilsTest.java:142-150`：

```java
assertEquals("17", VersionUtils.detectJavaMajorVersion("17.0.0")); // 新式
assertEquals("8",  VersionUtils.detectJavaMajorVersion("1.8.0"));  // 旧式 1.x 方案
assertEquals("1",  VersionUtils.detectJavaMajorVersion("1"));      // 无点号 → 原样
```

**按优先级顺序加载 SPI 提供者** —— `util/ServiceLoaderUtilsTest.java:81-105`、`:211-232`：

```java
List<EventListener> listeners = ServiceLoaderUtils.loadServicesList(EventListener.class, classLoader, true);
assertEquals(2, listeners.size());
// 按 Prioritized 排序：EchoEventListener2 为“first”，EchoEventListener 为“last”
assertSame(EchoEventListener2.class, ServiceLoaderUtils.loadFirstService(EventListener.class, classLoader).getClass());
assertSame(EchoEventListener.class,  ServiceLoaderUtils.loadLastService(EventListener.class, classLoader).getClass());
```

**没有提供者的服务会抛异常（不要依赖空结果）** —— `util/ServiceLoaderUtilsTest.java:107-112`、`:59-63`：

```java
assertThrows(IllegalArgumentException.class, () -> ServiceLoaderUtils.loadServicesList(Set.class)); // 零提供者
assertThrows(IllegalStateException.class, () -> ServiceLoaderUtils.getServiceClasses(Utils.class));  // 不可赋值，failFast
assertEquals(java.util.Collections.emptySet(), ServiceLoaderUtils.getServiceClasses(Utils.class, false)); // failFast=false 时降级
```

**类加载会降级为 `null`** —— `util/ClassLoaderUtilsTest.java:345-356`、`:376`、`:431-438`：

```java
assertNull(ClassLoaderUtils.loadClass(classLoader, "io.microsphere.not.found.class")); // 未知 → null
assertNull(ClassLoaderUtils.loadClass(classLoader, ""));                               // 空白 → null
assertNull(ClassLoaderUtils.loadClass(classLoader, null, true));                       // null 名称 → null（即使开启缓存）
assertNull(ClassLoaderUtils.getResourceAsString("not-found"));                         // 缺失资源 → null
```

**按 `ResourceType` 查找资源** —— `util/ClassLoaderUtilsTest.java:410-411`：

```java
Set<URL> urls = ClassLoaderUtils.getResources(classLoader, "io.microsphere.util"); // 遍历 ResourceType.values()
assertNotNull(urls);
```

**`StringUtils.split` 保留空 token 且空值安全** —— `util/StringUtilsTest.java:89-112`、`:72-86`：

```java
assertTrue(StringUtils.isBlank("   "));                 // 仅空白
assertSame(StringUtils.EMPTY_STRING_ARRAY, StringUtils.split(null, " "));
assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a,b,c", ","));
assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.split("a,,b,c", ",")); // 保留空 token
```

**`FormatUtils` 占位符替换** —— `text/FormatUtilsTest.java:34-53`：

```java
assertNull(FormatUtils.format(null));
assertEquals("A,B,C,D,E", FormatUtils.format("A,{},C,{},E", "B", "D"));
assertEquals("A,B,C,{},E", FormatUtils.format("A,{},C,{},E", "B")); // 多余的占位符保持字面
assertEquals("A,1,C,2,E", FormatUtils.format("A,{},C,{},E", 1, 2, 3)); // 第 3 个参数被忽略
```

**从资源 URL 打开 JAR** —— `util/jar/JarUtilsTest.java:104-125`、`:99-102`：

```java
URL url = ClassLoaderUtils.getClassResource(classLoader, javax.annotation.Nonnull.class);
assertNotNull(JarUtils.toJarFile(url));                                    // 解析到 jsr305.jar
assertNull(JarUtils.toJarFile(new URL("jar:file:/path/to/missing.jar!/e")));// 无法解析 → null
assertThrows(IllegalArgumentException.class, () -> JarUtils.toJarFile(new URL("http://github.com"))); // 错误协议
assertSame(java.util.Collections.emptyList(), JarUtils.filter(null, null)); // null jar → 空
```

---

## 5. 跨 JDK 与兼容性矩阵

| 关注点 | JDK 8 | JDK 9 / 11 | JDK 17 | JDK 21 | JDK 25 | 证据 |
|---|---|---|---|---|---|---|
| `ClassPathUtils.getBootstrapClassPaths()` | 从 `sun.misc` 引导 CP 填充 | **空**（`isBootClassPathSupported()` = `false`） | 空 | 空 | 空 | `util/ClassPathUtils.java:68`、`:94` |
| `SystemUtils.IS_JAVA_*` | `IS_JAVA_8` 为真 | `IS_JAVA_9/11` … | `IS_JAVA_17` | `IS_JAVA_21` | 超过 `IS_JAVA_24` 无专门标志 | `util/SystemUtils.java:357-517` |
| `VersionUtils.detectJavaMajorVersion("java.version")` | `"1.8.0_x"` → `"8"` | `"9.0.0"`/`"11.0.x"` → 主版本 | `"17.x"` | `"21.x"` | `"25.x"`（方案不变） | `util/VersionUtils.java:284` |
| `misc.UnsafeUtils` 静态初始化 | 可用（`theUnsafe` 存在） | 可用 | 可用（JDK 内部，已弃用） | 可用 | **可能失败** → 若 `sun.misc.Unsafe` 被移除则 `ExceptionInInitializerError` | `misc/UnsafeUtils.java:564`、`:570-572` |
| `ClassLoaderUtils.removeClassPathURL` | `URLClassLoader`（一个 `SecureClassLoader`）→ 可修改 | 系统加载器**不是** `URLClassLoader`；需为 `SecureClassLoader`，否则返回 `false` | 同左 | 同左 | 同左 | `util/ClassLoaderUtils.java:1598-1603` |
| `VersionUtils.LATEST_JAVA_VERSION` | `RELEASE_8` | `RELEASE_11` | `RELEASE_17` | `RELEASE_21` | `RELEASE_25`（委托给 `SourceVersion.latest()`） | `util/VersionUtils.java:39` |

> [!NOTE]
> `IS_JAVA_*` 与 `CURRENT_JAVA_VERSION` 来源不同：这些标志读取 `java.specification.version`
> （`matchesJavaVersion`，`util/SystemUtils.java:638`），而 `CURRENT_JAVA_VERSION` 通过 `currentJavaMajorVersion()`
> 读取 `java.version`（`util/VersionUtils.java:266`）。在旧式方案下二者一致，但它们是各自独立计算的。

---

## 6. 陷阱与已知限制

1. **`loadServicesList` / `loadServices` 在无提供者时抛异常。** 空的 `ServiceLoader` 会抛 `IllegalArgumentException`，
   而非返回空结果（`util/ServiceLoaderUtils.java:882-886`）。若服务可能缺失，先用 `getServiceClassNames(...)` 判空，
   或捕获异常。
2. **服务缓存忽略类加载器。** `servicesCache` 仅以服务类型为键（`util/ServiceLoaderUtils.java:846`）；跨不同加载器的
   缓存加载会共享同一个列表。当加载器隔离重要时请传 `cached = false`。
3. **`ArrayUtils.contains(int[], int)` 及其他数值重载需要*已排序*的数组。** 它们委托给 `Arrays.binarySearch`
   （`util/ArrayUtils.java:1911-2069`）；在未排序数组上结果可能出错。`boolean[]` 与 `Object[]` 重载使用线性扫描，与
   顺序无关（`:1887`、`:2096`）。
4. **`ClassPathUtils` 的快照仅计算一次。** `bootstrapClassPaths` 与 `classPaths` 为 `static final`
   （`util/ClassPathUtils.java:61`、`:64`）且永不刷新；在 JDK 9+ 上引导集合按设计为空，因此通过引导路径过滤 JDK 库
   仅在 JDK 8 上有效。
5. **`Assert.assertNoNullElements` 会跳过 `null`/空输入。** 仅当数组/迭代器存在且非空时才检查其元素
   （`util/Assert.java:348`），因此 `null` 数组会“通过”。若 `null` 本身必须失败，请用 `Assert.assertNotNull`。
6. **`UnsafeUtils` 可能在类加载时炸掉。** 若 `sun.misc.Unsafe` 不可用，`getUnsafe()` 会在静态初始化器中抛
   `UnsupportedOperationException`（`misc/UnsafeUtils.java:570-572`），JVM 会将其报告为 `ExceptionInInitializerError`
   —— 这是无法在调用点 `catch` 的值。请把 `UnsafeUtils` 视为受 JDK 版本门控。
7. **`ofVersion(Class)` 对无版本构件抛异常。** 其归档不携带版本元数据的类会产生 `IllegalArgumentException`
   （`util/Version.java:626-640`）；展开的 `target/classes` 目录是最常见的场景。
8. **`JarUtils.toJarFile` 混合了返回与抛出。** 缺失归档返回 `null`（`:86`），但 `null` URL 抛
   `NullPointerException`、错误协议抛 `IllegalArgumentException`（`assertJarURLProtocol`，`:111`）—— 三种结果很容易被混淆。
9. **空白输入在类加载中静默降级。** `doLoadClass` 对空白名返回 `null`，并在加载期间吞掉任何 `Throwable`
   （`util/ClassLoaderUtils.java:620`）；返回 `null` 本身并不能证明该类不存在（也可能是链接错误）—— 需要时在 trace 级别
   检查日志。
10. **`getAllLoadedClassesMap` 对每个键都使用顶层加载器。** 在为每个可继承加载器映射其已加载类时，循环传入的是*调用方
    提供的*加载器而非迭代变量（`util/ClassLoaderUtils.java:1142-1144`），因此 map 的值反映的是同一个加载器。不要用
    它来把类归属到各自的定义加载器。

---

## 7. 另请参阅

* [类加载与构件](classloading-and-artifacts.md) —— `ClassLoaderUtils` 所委派的 `URLClassPathHandle` SPI
* [集合与过滤器](collections-and-filters.md) —— `ServiceLoaderUtils` 排序所用的 `Lists`/`Sets`/`Prioritized`
* [网络与 URL 协议](net-and-url-protocols.md) —— `JarUtils` 归档路径解析背后的 `URLUtils`
* [语言抽象](language-abstractions.md) —— `io.microsphere.lang.Prioritized`，本组工具类所遵循的排序约定
* 面向开发者的深入解析：[工具类、常量与文本 —— 开发者指南](../../../developer-guide/microsphere-java-core/zh/util-and-constants.md)
