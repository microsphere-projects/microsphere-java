# 核心工具类

> 语言版本：[中文](core-utilities.md) · [English](../en/core-utilities.md)
> [← 手册目录](../README.md)

## 一览

| 项目 | 取值 |
|---|---|
| 所在模块 | `io.github.microsphere-projects:microsphere-java-core` |
| 涉及包 | `io.microsphere.util`、`io.microsphere.util.jar`、`io.microsphere.constants`、`io.microsphere.text` |
| 类形态 | `public abstract class XxxUtils implements Utils`，构造器 private，方法全部 static |
| `Utils` | 空标记接口（`public interface Utils {}`），仅用于归类和发现这些工具类 |
| 覆盖范围 | 字符串、Class、类加载器、SPI 加载、断言、版本判断、计时、JAR 处理等工具，以及共享常量 |

不要实例化任何 `XxxUtils`，直接调用静态方法即可。设计上有两个例外：`Assert` 并**不**实现 `Utils`；
`StopWatch`、`ValueHolder`、`Version` 是可实例化的普通类。另有两个占位类需要注意：`NumberUtils`
没有声明任何 public 方法，`BaseUtils` 已标记 `@Deprecated` 且是空类，两者都不应依赖。

```java
public abstract class StringUtils implements Utils {
    private StringUtils() {}         // 不可实例化
    public static boolean isBlank(String value) { ... }
}
```

本页使用签名简写：`getType(Object obj | Class<?> type)` 表示两个重载，分别接受 `Object` 和
`Class<?>`。

---

## 1. StringUtils

`public abstract class StringUtils implements Utils`

常量：`EMPTY`（`""`）、`EMPTY_STRING`（即 `EMPTY`）、`EMPTY_STRING_ARRAY`、`INDEX_NOT_FOUND`（`-1`）。

```java
// 空白判断
public static boolean isBlank(String value)
public static boolean isNotBlank(String value)
public static boolean isNumeric(String str)
public static boolean containsWhitespace(String str)

// 包含与前后缀
public static boolean contains(String value, CharSequence part)   // 唯一接受 CharSequence 的参数
public static boolean startsWith(String value, String part)
public static boolean endsWith(String value, String part)

// 切分与替换
public static String[] split(String value, char delimiter)
public static String[] split(String value, String delimiter)
public static String replace(String text, String searchString, String replacement)
public static String replace(String text, String searchString, String replacement, int max)

// 截取子串
public static String substringBetween(String str, String tag)
public static String substringBetween(String str, String open, String close)
public static String substringBefore(String str, String separator)
public static String substringAfter(String str, String separator)
public static String substringBeforeLast(String str, String separator)
public static String substringAfterLast(String str, String separator)

// 空白与大小写
public static String trimWhitespace(String str)        // 两端
public static String trimLeadingWhitespace(String str)
public static String trimTrailingWhitespace(String str)
public static String trimAllWhitespace(String str)     // 全部位置
public static String capitalize(String str)
public static String uncapitalize(String str)

// 转换
public static String[] toStringArray(Collection<String> collection)
public static String arrayToString(Object[] values, String delimiter)
```

> [!WARNING]
> `StringUtils` 没有 `hasText`、`hasLength`、`join`、`concat`、`defaultIfBlank`、`defaultString` 或
> `equalsIgnoreCase` —— Commons-Lang 的名字并非都在这里。`CharSequence` 操作请使用
> `CharSequenceUtils` / `CharSequenceComparator`；`{}` 插值请使用 `FormatUtils`
> （[第 10 节](#10-formatutils)）。

null 处理规则一致：`isBlank(null)` 为 `true`；`split(null, ',')` 返回**空数组**
（`EMPTY_STRING_ARRAY`）而不是 `null`；`split("abc", ',')`（分隔符不存在）返回 `["abc"]`；
`split(value, nullDelimiter)` 返回 `[value]`；trim/大小写系列方法对 `null` 或 `""` 原样返回。

```java
StringUtils.isBlank(" ")                       // true
StringUtils.split("a,b,c", ',')                // ["a","b","c"]
StringUtils.split(null, ',')                   // String[0]
StringUtils.trimWhitespace("  ab  ")           // "ab"   （两端）
StringUtils.trimLeadingWhitespace("  ab  ")    // "ab  "
StringUtils.trimAllWhitespace(" a b ")         // "ab"
StringUtils.capitalize("microsphere")          // "Microsphere"
StringUtils.substringBefore("key=value", "=")  // "key"
```

---

## 2. ClassUtils

共 68 个静态方法，常用如下：

```java
// 类型形态判断
public static boolean isPrimitive(Class<?>)
public static boolean isWrapperType(Object obj | Class<?>)
public static boolean isArray(Object obj | Class<?>)
public static boolean isEnum(Object obj | Class<?>)
public static boolean isNumber(Object obj | Class<?>)
public static boolean isCharSequence(Object obj | Class<?>)
public static boolean isSimpleType(Object obj | Class<?>)
public static boolean isInterface(Class<?>)
public static boolean isAbstractClass(Class<?>)
public static boolean isFinal(Class<?>)
public static boolean isConcreteClass(Class<?>)
public static boolean isGeneralClass(Class<?>)
public static boolean isTopLevelClass(Class<?>)
public static boolean isSyntheticClass(Object obj | Class<?>)
public static boolean isLambdaClass(Object obj | Class<?>)
public static boolean isLambdaClassName(String)
public static boolean isFunctionalInterface(Class<?>)
public static boolean isClass(Object)
public static boolean isDerived(Class<?> sourceType, Class<?>... superTypes)
public static boolean isAssignableFrom(Class<?> superType, Class<?> targetType)
public static boolean arrayTypeEquals(Class<?> one, Class<?> two)

// 基本类型 / 包装类型互转
public static Class<?> resolvePrimitiveType(Class<?> type)
public static Class<?> resolveWrapperType(Class<?> primitiveType)
public static Class<?> tryResolveWrapperType(Class<?> type)
public static Class<?> resolvePrimitiveClassForName(String className)

// 名称解析
public static String resolvePackageName(Class<?> type)
public static String resolvePackageName(String className)
public static String resolveClassName(String resourceName)     // "io/a/B.class" -> "io.a.B"
public static String getTypeName(Object obj | Class<?> type)
public static String getSimpleName(Class<?> type)
public static Class<?> getType(Object obj)
public static Class<?>[] getTypes(Object... objects)
public static Class<?>[] getClasses(Object... objects)
public static Class<?> getTopComponentType(Object obj | Class<?> type)

// 类型层级（由近及远排序；接口列表中不含 Object）
public static List<Class<?>> getAllSuperClasses(Class<?> type)
public static List<Class<?>> getAllInterfaces(Class<?> type)
public static List<Class<?>> getAllInheritedTypes(Class<?> type)
public static List<Class<?>> getAllInheritedClasses(Class<?> type)
public static List<Class<?>> getAllClasses(Class<?> type)
public static List<Class<?>> findAllSuperClasses(Class<?> type, Predicate<? super Class<?>>... filters)
public static List<Class<?>> findAllInterfaces(Class<?> type, Predicate<? super Class<?>>... filters)
public static List<Class<?>> findAllInheritedClasses(Class<?> type, Predicate<? super Class<?>>... filters)
public static List<Class<?>> findAllClasses(Class<?> type, Predicate<? super Class<?>>... filters)

// 按名称扫描 classpath（返回二进制类名，不加载类）
public static Set<String> findClassNamesInClassPath(String classPath, boolean recursive)
public static Set<String> findClassNamesInClassPath(File classPath, boolean recursive)
public static Set<String> findClassNamesInDirectory(File directory, boolean recursive)
public static Set<String> findClassNamesInJarFile(File jarFile, boolean recursive)

// 其他
public static <T> T cast(Object object, Class<T> castType)
public static ProtectionDomain getProtectionDomain(Class<?> type)
public static CodeSource getCodeSource(Class<?> type)
public static String getCodeSourceLocation(Class<?> type)
```

常量：`PRIMITIVE_TYPES`、`WRAPPER_TYPES`、`PRIMITIVE_ARRAY_TYPES`、`SIMPLE_TYPES`（均为
`Set<Class<?>>`），以及 `ARRAY_SUFFIX`（`"[]"`）和 `LAMBDA_CLASS_NAME_PREFIX`（`"$$Lambda"`）。

> [!NOTE]
> `getAll*` 会遍历完整层级；`findAll*` 在遍历过程中同步应用你传入的断言，避免生成中间列表。
> 两者都返回以查询类型为起点、由近及远排序的 `List<Class<?>>`。`getAllClasses(Class)` 正是
> `Converter` SPI 自动优先级计算所用的方法，因此层级形态会实际影响
> [类型转换](type-conversion.md) 的行为。

```java
ClassUtils.getAllSuperClasses(ArrayList.class);   // ArrayList 的父类链，由近及远
ClassUtils.getAllInterfaces(HashMap.class);       // HashMap 实现的全部接口，含继承而来
ClassUtils.getAllClasses(ArrayList.class);        // 父类 + 接口合并
ClassUtils.findAllInterfaces(HashMap.class, Map.class::isAssignableFrom);
ClassUtils.resolveClassName("io/microsphere/util/StringUtils.class");  // "io.microsphere.util.StringUtils"
```

---

## 3. ClassLoaderUtils

```java
// 取哪个类加载器？
public static ClassLoader getDefaultClassLoader()               // 永不返回 null
public static ClassLoader getClassLoader(Class<?> type)
public static ClassLoader getCallerClassLoader()
public static ClassLoader nullSafeClassLoader(ClassLoader classLoader)   // null -> 默认加载器
public static Set<ClassLoader> getInheritableClassLoaders(ClassLoader classLoader)

// 加载类
public static Class<?> loadClass(ClassLoader loader, String className)
public static Class<?> loadClass(ClassLoader loader, String className, boolean cached)
public static Class<?> resolveClass(String className)
public static Class<?> resolveClass(String className, ClassLoader loader)
public static Class<?> resolveClass(String className, ClassLoader loader, boolean cached)
public static boolean isPresent(String className)
public static boolean isPresent(String className, ClassLoader loader)

// 已加载类的内省（仅 HotSpot 可用）
public static Class<?> findLoadedClass(ClassLoader loader, String className)
public static boolean isLoadedClass(ClassLoader loader, Class<?> type)
public static boolean isLoadedClass(ClassLoader loader, String className)
public static Set<Class<?>> findLoadedClasses(ClassLoader loader, String... packageNames)
public static Set<Class<?>> findLoadedClasses(ClassLoader loader, Iterable<String> packageNames)
public static Set<Class<?>> findLoadedClassesInClassPath(ClassLoader loader)
public static Set<Class<?>> findLoadedClassesInClassPaths(ClassLoader loader)
public static Set<Class<?>> getAllLoadedClasses(ClassLoader loader)
public static Set<Class<?>> getLoadedClasses(ClassLoader loader)
public static Map<String, Class<?>> getAllLoadedClassesMap(ClassLoader loader)
public static int  getLoadedClassCount()
public static long getUnloadedClassCount()
public static long getTotalLoadedClassCount()
public static boolean isVerbose()
public static void setVerbose(boolean verbose)

// 资源
public static URL getResource(String resource)
public static URL getResource(ClassLoader loader, String resource)
public static URL getResource(ClassLoader loader, ResourceType resourceType, String resource)
public static Set<URL> getResources(ClassLoader loader, String resource)
public static String getResourceAsString(String resource)
public static String getResourceAsString(ClassLoader loader, String resource)
public static URL getClassResource(ClassLoader loader, Class<?> type)
public static URL getClassResource(ClassLoader loader, String className)

// URLClassLoader 管道
public static URLClassLoader findURLClassLoader(ClassLoader loader)
public static URLClassLoader resolveURLClassLoader(ClassLoader loader)
public static URLClassLoader newURLClassLoader(URL... urls)
public static URLClassLoader newURLClassLoader(ClassLoader parent, URL... urls)
public static URLClassLoader newURLClassLoader(boolean initializedLoaders, URL... urls)
public static URLClassLoader newURLClassLoader(ClassLoader parent, boolean initializedLoaders, URL... urls)
public static Set<URL> findAllClassPathURLs(ClassLoader loader)
public static boolean removeClassPathURL(ClassLoader loader, URL url)
```

`ResourceType` 是嵌套枚举（`io.microsphere.util.ClassLoaderUtils.ResourceType`），会在查找前规范化
资源名 —— 当你手上是一个类名、想要它的 `.class` 资源时特别有用。

> [!IMPORTANT]
> "已加载类"这一组方法依赖 HotSpot 内部机制，在其他 JVM 上可能返回空结果或直接失败，
> 切勿把程序正确性建立在其结果上。`findAllClassPathURLs` 与 `removeClassPathURL` 是
> [类加载与构建产物](classloading-and-artifacts.md) 的基础。JDK 9+ 上"移除"路径经由
> `URLClassPathHandle` SPI，行为取决于当前可用的实现。

---

## 4. ClassPathUtils

小而专：

```java
public static Set<String> getBootstrapClassPaths()
public static Set<String> getClassPaths()                       // java.class.path 条目
public static URL getRuntimeClassLocation(String className)
public static URL getRuntimeClassLocation(Class<?> type)
```

`getRuntimeClassLocation(String)` 通过 **JVM 自身的 bootstrap/platform 加载器**解析类名，返回其
class 文件的实际位置（现代 JDK 上是 `jrt:`/`jar:` URL）；`Class<?>` 重载则是对已加载类的快捷入口。

---

## 5. ServiceLoaderUtils

整个框架的 SPI 大门。`microsphere-java-core` 中每个可插拔子系统都经由它加载。

```java
public static final String SERVICES_PROVIDER_LOCATION;   // "META-INF/services/"
public static final String SERVICE_LOADER_CACHED_PROPERTY_NAME;  // "microsphere.service-loader.cached"
public static final boolean SERVICE_LOADER_CACHED;       // 取自该系统属性，默认 false

// 只发现、不实例化（拿类名 / Class）
public static <T> Set<Class<T>> getServiceClasses(Class<T> serviceType)
public static <T> Set<Class<T>> getServiceClasses(Class<T> serviceType, boolean failFast)
public static <T> Set<Class<T>> getServiceClasses(Class<T> serviceType, ClassLoader classLoader)
public static <T> Set<Class<T>> getServiceClasses(Class<T> serviceType, ClassLoader classLoader, boolean failFast)
public static Set<String> getServiceClassNames(Class<?> serviceType)
public static Set<String> getServiceClassNames(Class<?> serviceType, ClassLoader classLoader)
public static Set<URL> getServiceResoources(Class<?> serviceType, ClassLoader classLoader) throws IOException

// 加载实例
public static <S> List<S> loadServicesList(Class<S> serviceType) throws IllegalArgumentException
public static <S> List<S> loadServicesList(Class<S> serviceType, ClassLoader classLoader | boolean cached)
public static <S> List<S> loadServicesList(Class<S> serviceType, ClassLoader classLoader, boolean cached)

public static <S> S[] loadServices(Class<S> serviceType) throws IllegalArgumentException
public static <S> S[] loadServices(Class<S> serviceType, ClassLoader classLoader | boolean cached)
public static <S> S[] loadServices(Class<S> serviceType, ClassLoader classLoader, boolean cached)

public static <S> S loadFirstService(Class<S> serviceType) throws IllegalArgumentException
public static <S> S loadFirstService(Class<S> serviceType, ClassLoader classLoader | boolean cached)
public static <S> S loadFirstService(Class<S> serviceType, ClassLoader classLoader, boolean cached)

public static <S> S loadLastService(Class<S> serviceType) throws IllegalArgumentException
public static <S> S loadLastService(Class<S> serviceType, ClassLoader classLoader | boolean cached)
public static <S> S loadLastService(Class<S> serviceType, ClassLoader classLoader, boolean cached)
```

加载结果**总是**按 `Prioritized.COMPARATOR`（`getPriority()` 升序）排序，首元素即优先级最高的实现
—— `Converters`、`LoggerFactory`、`AbstractEventDispatcher`、`ArtifactDetector`、`ProcessIdResolver`
都建立在这一约定上。`cached` 参数只控制加载结果是否复用；返回的 `List` 是不可修改的。

> [!WARNING]
> 三个坑：
> 1. 方法名源码中拼作 `getServiceResoources`（三个 o），搜索时注意。
> 2. `SERVICE_LOADER_CACHED` 默认为 **`false`**：不传 `cached = true`、也不设置
>    `-Dmicrosphere.service-loader.cached=true` 时，重复调用 `loadServicesList` 会重新读取配置
>    并重新实例化。
> 3. 当服务文件中**没有任何**实现声明时，`loadServices*` / `loadFirstService` / `loadLastService`
>    抛出 **`IllegalArgumentException`**，而不是返回空列表。服务可能缺席时，先用
>    `getServiceClasses(...)`（返回空集合）判断，或包一层保护。

```java
ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();

List<Converter> converters = ServiceLoaderUtils.loadServicesList(Converter.class, classLoader, true);
Converter first = ServiceLoaderUtils.loadFirstService(Converter.class);   // 优先级最高
```

---

## 6. Assert

`public abstract class Assert`（有意**不**实现 `Utils`）。所有断言方法均为 `public static void`，
失败即抛出；每个方法都有 `String` 与 `Supplier<String>` 两种消息形式。

```java
public static void assertTrue(boolean expression, String message | Supplier<String>)
public static void assertNull(Object object, ...)
public static void assertNotNull(Object object, ...)
public static void assertNotEmpty(String text | Object[] | Collection<?> | Map<?, ?>, ...)
public static void assertNotBlank(String text, ...)
public static void assertNoNullElements(Object[] array | Iterable<?> iterable, ...)
public static void assertArrayIndex(Object array, int index)
public static void assertArrayType(Object array)                       // 必须是数组
public static void assertFieldMatchType(Object object, String fieldName, Class<?> expectedType)
```

> [!NOTE]
> 方法名是 `assertTrue`，不是 `isTrue`。失败时抛出用所给消息构造的普通异常，适合库代码中
> 零依赖的前置校验。

---

## 7. Version 与 VersionUtils

`public class Version implements Comparable<Version>, Serializable`

```java
// 构造
public Version(int major)
public Version(int major, int minor)
public Version(int major, int minor, int patch)
public Version(int major, int minor, int patch, String preRelease)

public static Version of(int major)
public static Version of(int major, int minor)
public static Version of(int major, int minor, int patch)
public static Version of(int major, int minor, int patch, String preRelease)
public static Version of(String version)
public static Version ofVersion(int... majorMinorPatch)   // 与 of(...) 各元数对应
public static Version ofVersion(String version)
public static Version ofVersion(Class<?> classInResource) // 读取该类所在构件的 POM/manifest 版本
public static Version getVersion(Class<?> targetClass) throws IllegalArgumentException

// 访问器
public int getMajor();  public int getMinor();  public int getPatch();  public String getPreRelease()

// 比较
public boolean gt(Version v);  public boolean ge(Version v)
public boolean lt(Version v);  public boolean le(Version v);  public boolean eq(Version v)
public boolean isGreaterThan(Version v);      public boolean isGreaterOrEqual(Version v)
public boolean isLessThan(Version v);         public boolean isLessOrEqual(Version v)
public boolean equals(Version version);       public int compareTo(Version that)
```

`of(...)` 与 `ofVersion(...)` 两个系列都存在；`ofVersion(Class)` 从包含该类的 jar 推导版本，
各模块上报自身版本用的就是它。

嵌套枚举 `public enum Version.Operator implements BiPredicate<Version, Version>`：

```java
GT, GE, LT, LE, EQ
public boolean test(Version v1, Version v2)     // 各常量各自实现
public static Operator of(String symbol)        // ">=" -> GE
```

`public abstract class VersionUtils implements Utils`：

```java
public static final SourceVersion LATEST_JAVA_VERSION;   // 类型是 javax.lang.model.SourceVersion，不是 Version
public static final Version CURRENT_JAVA_VERSION;
public static final Version JAVA_VERSION_8, ... JAVA_VERSION_24;

public static boolean testCurrentJavaVersion(String operatorSymbol, Version version)
public static boolean testCurrentJavaVersion(Version.Operator operator, Version version)
public static boolean testVersion(String baseVersion, String operatorSymbol, String comparedVersion)
public static boolean testVersion(Version base, Version.Operator operator, Version compared)
```

```java
Version microsphere = Version.ofVersion("0.3.19");
microsphere.gt(Version.of(0, 3, 0));                              // true
VersionUtils.testCurrentJavaVersion(">=", Version.of(17));        // 按 JDK 版本分支
```

---

## 8. StopWatch

`public class StopWatch` —— **非线程安全**，且没有 `reset()`：一个实例从生到死累积任务。

```java
public StopWatch(String id)                        // 唯一的构造器

public void start(String taskName) throws IllegalArgumentException, IllegalStateException
public void start(String taskName, boolean reentrant) throws IllegalArgumentException, IllegalStateException
public void stop() throws IllegalStateException
public Task getCurrentTask()

public String getId()
public List<Task> getRunningTasks()
public List<Task> getCompletedTasks()
public long getTotalTimeNanos()
public long getTotalTime(TimeUnit timeUnit)
public String toString()                           // 逐任务报告
```

嵌套类 `public static class StopWatch.Task`：

```java
public static Task start(String taskName)
public static Task start(String taskName, boolean reentrant)
public void stop()
public String getTaskName()
public boolean isReentrant()
public long getStartTimeNanos()
public long getElapsedNanos()
```

> [!WARNING]
> 没有 `prettyPrint()`、`reset()`、`isRunning()`、`getLastTaskTimeMillis()`，也没有无参构造器 ——
> 那些是 Commons-Lang/Spring 的名字。报告用 `toString()`，数值用 `getTotalTime(TimeUnit)`。
> `start(String)` 不可重入；若同名任务需要在嵌套帧中反复启动，请传 `reentrant = true`。

```java
StopWatch watch = new StopWatch("bootstrap");
watch.start("spi");
ServiceLoaderUtils.loadServicesList(Converter.class);
watch.stop();

watch.start("cache");
// ...
watch.stop();

System.out.println(watch.getTotalTime(TimeUnit.MILLISECONDS));
System.out.println(watch);   // id + 每个任务的耗时（纳秒）
```

---

## 9. constants 包

八个 `public interface` 常量载体，由 `Constants` 组合：

```java
public interface Constants extends FileConstants, PathConstants, PropertyConstants,
                                   ProtocolConstants, SeparatorConstants, SymbolConstants {}
```

| 接口 | 代表成员 |
|---|---|
| `SymbolConstants` | `AND "&"`、`AT "@"`、`COLON ":"`、`COMMA ","`、`DOT "."`、`EQUAL "="`、`EXCLAMATION "!"`、`HYPHEN "-"`、`PIPE`/`VERTICAL_BAR "\|"`（互为别名）、`QUESTION_MARK "?"`、`QUOTE "'"`（单引号）、`DOUBLE_QUOTE "\""`、`SEMICOLON ";"`、`SPACE " "`、`UNDER_SCORE "_"`、`WILDCARD "*"` —— 每个都有对应的 `*_CHAR` 常量（`DOT_CHAR '.'`、`QUOTE_CHAR '\''`、`AND_CHAR '&'`） |
| `SeparatorConstants` | `FILE_SEPARATOR = File.separator`、`PATH_SEPARATOR = File.pathSeparator`、`LINE_SEPARATOR = System.lineSeparator()`、`ARCHIVE_ENTRY_SEPARATOR "!/"` |
| `PathConstants` | `SLASH "/"`、`DOUBLE_SLASH "//"`、`BACK_SLASH "\\"`，以及 `SLASH_CHAR`、`BACK_SLASH_CHAR` |
| `ProtocolConstants` | `HTTP_PROTOCOL`、`HTTPS_PROTOCOL`、`FILE_PROTOCOL`、`JAR_PROTOCOL`、`WAR_PROTOCOL`、`EAR_PROTOCOL`、`ZIP_PROTOCOL`、`FTP_PROTOCOL`、`CLASSPATH_PROTOCOL = "classpath"`、`CONSOLE_PROTOCOL = "console"` |
| `FileConstants` | `CLASS "class"`、`JAR "jar"`、`WAR`、`EAR`、`ZIP`、`FILE_EXTENSION DOT`、`CLASS_EXTENSION ".class"`、`JAR_EXTENSION`、`JAVA_EXTENSION ".java"` |
| `PropertyConstants` | `ENABLED_PROPERTY_NAME "enabled"`、`MICROSPHERE_PROPERTY_NAME_PREFIX "microsphere."` |
| `ResourceConstants` | `METADATA_RESOURCE "META-INF/"`、`MICROSPHERE_METADATA_RESOURCE "META-INF/microsphere/"`、`CONFIGURATION_PROPERTY_METADATA_FILE_NAME "configuration-properties.json"`、`ADDITIONAL_CONFIGURATION_PROPERTY_METADATA_FILE_NAME "additional-configuration-properties.json"`，以及两个拼接好的 `*_METADATA_RESOURCE` 完整路径 |

> [!IMPORTANT]
> `QUOTE` 是**单**引号，双引号请用 `DOUBLE_QUOTE`。元数据资源路径定义在 `ResourceConstants`
> （而非 `PathConstants`）：就是上面那对 `META-INF/microsphere/*.json`，
> 不是 `additional-spring-configuration-metadata.json`。另外 `Constants` 并未继承
> `ResourceConstants`，需要直接引用它。

---

## 10. FormatUtils

`io.microsphere.text.FormatUtils` —— SLF4J 风格的 `{}` 插值，日志门面的底层实现：

```java
public abstract class FormatUtils implements Utils

public static final String DEFAULT_PLACEHOLDER = "{}";
public static String format(String pattern, Object... args)
public static String formatWithPlaceholder(String pattern, String placeholder, Object... args)
```

```java
FormatUtils.format("No Resource[{}] found in {}", "app.yml", "classpath:");
// "No Resource[app.yml] found in classpath:"
```

---

## 11. JarUtils

`io.microsphere.util.jar.JarUtils`：

```java
public static final String MANIFEST_RESOURCE_PATH;   // "META-INF/MANIFEST.MF"

public static JarFile toJarFile(URL jarURL)
public static String resolveRelativePath(URL resourceURL)
public static String resolveJarAbsolutePath(URL jarURL)
public static List<JarEntry> filter(JarFile jarFile, JarEntryFilter filter)
public static JarEntry findJarEntry(URL resourceURL)
public static boolean isDirectoryEntry(URL url)
public static void extract(File jarSourceFile, File targetDirectory) throws IOException
public static void extract(File jarSourceFile, File targetDirectory, JarEntryFilter filter) throws IOException
public static void extract(JarFile jarFile, File targetDirectory, JarEntryFilter filter) throws IOException
public static void extract(URL jarURL, File targetDirectory, JarEntryFilter filter) throws IOException
```

`JarEntryFilter` 是一个 `Filter<JarEntry>`（见[集合与过滤器](collections-and-filters.md)），
常用实参是 `io.microsphere.filter` 包中的 `ClassFileJarEntryFilter.INSTANCE`：

```java
JarUtils.extract(new URL("jar:file:/libs/app.jar!/"), new File("target/unpacked"),
                 ClassFileJarEntryFilter.INSTANCE);
```

---

## 12. 其他值得了解的 util 类

| 类 | 公开能力 |
|---|---|
| `AnnotationUtils` | 沿类型层级查找注解（`findAnnotation`、`getAnnotation`、注解属性 Map 系列辅助方法） |
| `ArrayUtils` | 各基本类型及对象数组的 `EMPTY_*` 常量；`of(T...)`/`ofArray(T...)`、`ofBooleans` … `ofDoubles(...)`、`length/size(T[])`、`isEmpty/isNotEmpty(T[])`、`arrayEquals(T[],T[])`、`contains(T[], T)`、`combine(...)`、`asArray(Collection/Iterable/Enumeration, componentType)`、`newArray(Class, int)`、`reverse(T[])`、`toArrayReversed(Collection, T[])`、`forEach(T[], Consumer)` 与 `forEach(T[], BiConsumer<Integer,T>)`、`arrayToString(T[])` |
| `ObjectUtils` | 恰好三个方法：`<S,T> T nullSafe(S source, Function<S,T> function)`、`<T> T defaultIfNull(T object, Supplier<T> supplier)`、`<T> T defaultIfNull(T object, T defaultValue)` |
| `ExceptionUtils` | `getStackTrace(Throwable)`、`wrap(T source, Class<TT> thrownType)`、六个 `create(...)` 重载、`throwTarget(T source, Class<TT> thrownType) throws TT` |
| `ThrowableUtils` | 只有 `getRootCause(Throwable)` |
| `StackTraceUtils` | StackTraceElement 的提取与检查 |
| `SizeUtils` | `bytesSize(Class<?> type)` 加各基本类型字节宽度常量（`BOOLEAN_BYTES_SIZE` … `DOUBLE_BYTES_SIZE`、`UNBOUND_BYTES_SIZE`）—— 它做的是"类型到字节宽度"的映射，**不**格式化或解析人类可读的大小 |
| `IterableUtils` | `isIterable(Object/Class<?>)`、`iterate(Iterable, Consumer)`、`forEach(Iterable, Consumer)` |
| `ValueHolder<V>` | `ValueHolder()`、`ValueHolder(V)`、`static of(V)`、`getValue()`、`setValue(V)`、`reset()` —— lambda 中的可变值捕获，可替代 `AtomicReference` |
| `Compatible<T, R>`、`Configurer<T>`、`Functional<V>` | 小型函数式契约：适配旧类型、配置对象、单方法可调用抽象 |
| `TypeFinder<T>` | `classFinder(...)` / `genericTypeFinder(...)`，配合嵌套 `Include` 枚举控制是否包含自身与层级类型 |
| `PropertyResourceBundleUtils` + `PropertyResourceBundleControl` | 以显式编码加载 ResourceBundle；遵循 `java.util.PropertyResourceBundle.encoding` 属性 |
| `CharSequenceComparator`、`CharSequenceUtils` | 补上 `StringUtils` 有意不做的 `CharSequence` 比较与工具（`length`、`isEmpty`、`isNotEmpty`、`containsWhitespace`、`trimAllWhitespace`） |
| `HierarchicalClassComparator` | 按类层级排序的 `Comparator<Class<?>>` |
| `PriorityComparator` | 可容忍非 `Prioritized` 元素的 `Comparator<Object>`，使用 `PriorityComparator.INSTANCE` |
| `ShutdownHookUtils` | 见下文 |
| `SystemUtils` | 见下文 |

### ShutdownHookUtils

把普通 `Runnable` 注册进 JVM 关闭序列，避免各处争抢 `Runtime.addShutdownHook`：

```java
public static boolean addShutdownHookCallback(Runnable callback)
public static boolean removeShutdownHookCallback(Runnable callback)
public static Queue<Runnable> getShutdownHookCallbacks()
public static void registerShutdownHook()              // 安装唯一的 JVM 钩子
public static Set<Thread> getShutdownHookThreads()
public static Set<Thread> filterShutdownHookThreads(Predicate<? super Thread> filter)
public static Set<Thread> filterShutdownHookThreads(Predicate<? super Thread> filter, boolean removed)

public static final int DEFAULT_SHUTDOWN_HOOK_CALLBACKS_CAPACITY;    // 512
public static final int SHUTDOWN_HOOK_CALLBACKS_CAPACITY;            // microsphere.shutdown-hook.callbacks-capacity
public static final Predicate<? super Thread> SHUTDOWN_HOOK_CALLBACKS_THREAD_FILTER;  // 匹配 ShutdownHookCallbacksThread
```

`ExecutorUtils.shutdownOnExit(...)`（见[并发](concurrency-process-jmx.md)）就构建在它之上；
线程过滤器之所以存在，是因为所有回调都运行在专门的 `ShutdownHookCallbacksThread` 中。

### SystemUtils

常量加三个方法：

```java
public static String getSystemProperty(String key)
public static String getSystemProperty(String key, String defaultValue)
public static void copySystemProperties()
```

已解析的常量包括 `JAVA_CLASS_PATH`、`USER_NAME`、`JAVA_HOME`、`JAVA_VERSION`、`OS_NAME`、
`OS_ARCH`、`OS_VERSION`、`FILE_ENCODING`、`NATIVE_ENCODING`、`USER_DIR`、`USER_HOME`、
`JAVA_IO_TMPDIR`、`IS_OS_WINDOWS`（对应 `OS_NAME_WINDOWS_PREFIX = "Windows"`），
`IS_JAVA_8` … `IS_JAVA_24` 布尔系列以及 `IS_LTS_JAVA_VERSION`。每个系统属性都有配对的
`*_PROPERTY_KEY` 字符串常量。

> [!TIP]
> `IS_JAVA_*` 系列是截止于 24 的静态数据，未来新 JDK 不会自动覆盖。要做前向兼容的判断，
> 优先使用 `VersionUtils.testCurrentJavaVersion(">=", Version.of(21))`。

---

## 参见

* [语言抽象](language-abstractions.md) —— `Prioritized`，`ServiceLoaderUtils` 返回顺序的制定者
* [反射与类型](reflection-and-types.md) —— `MethodUtils`、`FieldUtils`、`TypeUtils`
* [集合与过滤器](collections-and-filters.md) —— `JarEntryFilter` 背后的 `Filter` 契约
* [参考手册](reference.md) —— 完整的系统属性表

[← 手册目录](../README.md) · [上一篇：注解](annotations.md) · [下一篇：集合与过滤器 →](collections-and-filters.md)
