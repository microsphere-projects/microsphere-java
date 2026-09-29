# 参考手册

> 语言版本：[中文](reference.md) · [English](../en/reference.md)
> [← 手册目录](../README.md)

## 一览

| 项目 | 数值 |
|---|---|
| `microsphere-java-core` 中的 SPI 文件 | 13 个服务文件，86 行实现声明 → 69 个不同类名（`Converter` 文件中有一行重复；8 个基础类型序列化器同时出现在两个序列化文件里） |
| `microsphere-annotation-processor` 中的 SPI 文件 | 1 个 —— `javax.annotation.processing.Processor` |
| `microsphere.*` 系统属性 | 10 个键，全部在 `static` 初始化块中读取一次 |
| Microsphere 资源根目录 | `META-INF/microsphere/` |
| CI 覆盖的 JDK | 8、11、17、21、25（`temurin`，`ubuntu-latest`） |
| BOM 管理的构件 | 7 个 `jar` 模块，另有 2 个 `pom` 构件 |

> [!IMPORTANT]
> 所有 `microsphere.*` 可调参数都在所属类的静态初始化阶段被写入 `public static final` 字段
> （`System.getProperty` / `getInteger`）。之后再调用 `System.setProperty` 对当前 JVM 无效 —— 请在启动命令
> 中以 `-D` 传入，或确保在首次触达该类之前设置。

---

## 1. SPI 注册表

下表逐文件核对自 `microsphere-java-core/src/main/resources/META-INF/services/`。

| 接口（FQN） | 已注册实现 | 加载方式 | 说明 |
|---|---|---|---|
| `io.microsphere.classloading.ArtifactResourceResolver` | `MavenArtifactResourceResolver`、`ManifestArtifactResourceResolver`、`ArchiveFileArtifactResourceResolver` | `ArtifactDetector` → `loadServicesList(ArtifactResourceResolver.class, classLoader, true)` | 恒定缓存。优先级 1 / 5 / 9（各自的 `DEFAULT_PRIORITY` 常量）；只要某个 resolver 返回非空 `Artifact` 即停止 |
| `io.microsphere.classloading.URLClassPathHandle` | `ClassicURLClassPathHandle`、`ModernURLClassPathHandle`、`NoOpURLClassPathHandle` | `ServiceLoadingURLClassPathHandle` → `loadServicesList(URLClassPathHandle.class)` | 是否缓存取决于 `microsphere.service-loader.cached`。`Classic` 与 `Modern` 优先级同为 `MAX_PRIORITY + 99999`；`NoOp` 使用接口默认 `MIN_PRIORITY`，因此排在最后，只有当其余实现 `supports()` 为 `false` 时才会被选中 |
| `io.microsphere.convert.Converter` | 声明 35 行 / 去重 34 个类（`StringTo*`、`ObjectTo*`、`NumberTo*`、`ByteArrayToObjectConverter`、`MapToPropertiesConverter`、`PropertiesToStringConverter`、`StringToDurationConverter`、`StringToClassConverter`、`StringToInputStreamConverter`、`ObjectToOptionalConverter`） | `Converters.loadConvertersList()` → `loadServicesList(Converter.class, classLoader, true)` | 恒定缓存，随后按 `(sourceType, targetType)` 建索引写入 `Converters.convertersCache`。注意文件中 `io.microsphere.convert.ObjectToOptionalConverter` 出现两次 —— 第二条是重复项，无副作用 |
| `io.microsphere.convert.multiple.MultiValueConverter` | `StringToArrayConverter`、`StringToBlockingDequeConverter`、`StringToBlockingQueueConverter`、`StringToCollectionConverter`、`StringToDequeConverter`、`StringToListConverter`、`StringToNavigableSetConverter`、`StringToQueueConverter`、`StringToSetConverter`、`StringToSortedSetConverter`、`StringToTransferQueueConverter`（11 个） | `MultiValueConverter` 静态查找 → `loadServicesList(MultiValueConverter.class, classLoader)` | 是否缓存取决于 `microsphere.service-loader.cached` |
| `io.microsphere.event.EventDispatcher` | `DirectEventDispatcher`、`ParallelEventDispatcher` | 作为发现点注册；core 内部没有任何代码通过 service loader 加载 `EventDispatcher.class` | `EventDispatcher.newDefault()` 直接构造 `DirectEventDispatcher`（执行器为 `Runnable::run`）；`EventDispatcher.parallel(executor)` 构造 `ParallelEventDispatcher`。无参 `ParallelEventDispatcher` 使用 `ForkJoinPool.commonPool()` |
| `io.microsphere.io.serializer.Serializer` | `BooleanSerializer`、`ByteSerializer`、`CharacterSerializer`、`ShortSerializer`、`IntegerSerializer`、`LongSerializer`、`FloatSerializer`、`DoubleSerializer`、`StringSerializer`、`DefaultSerializer`（10 个） | `Serializers` → `loadServicesList(Serializer.class, classLoader, true)` | 恒定缓存。8 个基础类型实现继承 `AbstractSerializer<T>`，该类同时实现 `Serializer<T>` **和** `Deserializer<T>` |
| `io.microsphere.io.serializer.Deserializer` | 上述 8 个 `AbstractSerializer` 子类，外加 `StringDeserializer`、`DefaultDeserializer`（10 个） | `Deserializers` → `loadServicesList(Deserializer.class, classLoader)` | 是否缓存取决于 `microsphere.service-loader.cached` |
| `io.microsphere.logging.LoggerFactory` | `Sfl4jLoggerFactory`、`ACLLoggerFactory`、`JDKLoggerFactory`、`NoOpLoggerFactory` | `LoggerFactory.loadFactories()` 内调用 `java.util.ServiceLoader.load(LoggerFactory.class, classLoader)`，随后 `sort(factories, COMPARATOR)`，再 `removeIf(!isAvailable())`，取 `get(0)` | 由 JDK service loader 加载，**不是** `ServiceLoaderUtils`，因此缓存开关对其无效。可用性取决于委托类能否解析（`org.slf4j.Logger` → `org.apache.commons.logging.Log` → `java.util.logging.Logger`）；`NoOpLoggerFactory.isAvailable()` 硬编码为 `true`，保证委托链一定有结果 |
| `io.microsphere.metadata.ConfigurationPropertyGenerator` | `DefaultConfigurationPropertyGenerator` | `ConfigurationPropertyJSONElementVisitor` / 注解处理器 | 把 `ConfigurationProperty` 模型转换成写入元数据的 JSON 片段 |
| `io.microsphere.metadata.ConfigurationPropertyLoader` | `AdditionalMetadataResourceConfigurationPropertyLoader` | `ConfigurationPropertyLoader.loadAll()` → `loadServicesList(ConfigurationPropertyLoader.class)` | 每个 loader 的 `load()` 都被 `try/catch (Throwable)` 包裹，失败只记录 `error` 日志并跳过。`MetadataResourceConfigurationPropertyLoader`（读取 `configuration-properties.json`）确实存在，但**未**注册在此 —— 若要在运行期加载生成的文件，需自行注册 |
| `io.microsphere.metadata.ConfigurationPropertyReader` | `DefaultConfigurationPropertyReader` | `ClassPathResourceConfigurationPropertyLoader` 直接 `new DefaultConfigurationPropertyReader()` | 把 JSON 解析为 `io.microsphere.beans.ConfigurationProperty`；优先级 `MIN_PRIORITY` |
| `io.microsphere.net.ExtendableProtocolURLStreamHandler` | `io.microsphere.net.classpath.Handler`（`classpath:`）、`io.microsphere.net.console.Handler`（`console:`） | `ServiceLoaderURLStreamHandlerFactory.loadHandlers()` → `loadServicesList(ExtendableProtocolURLStreamHandler.class)`，按协议建索引 | 构造时校验约定：必须是顶层类、简单类名必须恰为 `Handler`、包名不得位于 `sun.net.www.protocol` 之下；同时把 handler 包前缀追加进 `java.protocol.handler.pkgs` |
| `io.microsphere.process.ProcessIdResolver` | `ModernProcessIdResolver`、`VirtualMachineProcessIdResolver`、`ClassicProcessIdResolver` | `ManagementUtils` → `loadServicesList(ProcessIdResolver.class)`，`.filter(supports()).findFirst()` | 是否缓存取决于 `microsphere.service-loader.cached`。优先级 1 / 5 / 9（`NORMAL_PRIORITY + n`），即 Modern 优先、Classic 兜底；全部失败时返回 `UNKNOWN_PROCESS_ID`（`-1`） |

### 排序语义（`io.microsphere.lang.Prioritized`）

`ServiceLoaderUtils.loadServicesAsList(...)` 使用 `Prioritized.COMPARATOR` 排序：`Prioritized` 实例按
`getPriority()` **升序**排列，并且始终排在非 `Prioritized` 实例**之前**。

| 常量 | 数值 | 效果 |
|---|---|---|
| `Prioritized.MAX_PRIORITY` | `Integer.MIN_VALUE` | 排最前 —— 名字表示"最高优先级"，数值却是最小整数 |
| `Prioritized.NORMAL_PRIORITY` | `0` | `getPriority()` 的默认值 |
| `Prioritized.MIN_PRIORITY` | `Integer.MAX_VALUE` | 排最后 —— 通常给兜底实现（`NoOpLoggerFactory` 即如此） |

> [!WARNING]
> 当服务文件解析不出任何实现时，`ServiceLoaderUtils.loadServicesAsList` 抛出 `IllegalArgumentException`。
> "零实现"是硬失败，而不是返回空列表。

---

## 2. 注册自己的 SPI 实现

1. 在 classpath 上实现服务接口。若接口继承 `Prioritized`
   （`LoggerFactory`、`ArtifactResourceResolver`、`URLClassPathHandle`、`ProcessIdResolver`、
   `ConfigurationPropertyLoader`、`ExtendableProtocolURLStreamHandler`），请覆写 `getPriority()`。
2. 提供 public 无参构造器 —— `java.util.ServiceLoader` 有此要求。
3. 新建文件 `src/main/resources/META-INF/services/<接口全限定名>`，每行写一个实现类 FQN。JDK loader 会忽略
   注释行（`#`）与空行。
4. 想覆盖内置实现，就自带一份同名服务文件：classpath 上所有 `META-INF/services/<type>` 都会被读取，
   `loadFirstService(...)` 在按优先级排序后取第一个。

```
src/main/resources/META-INF/services/io.microsphere.classloading.ArtifactResourceResolver
```

```
com.acme.AcmecArtifactResourceResolver
```

排序示例 —— 需要早于 `MavenArtifactResourceResolver`（优先级 `1`）执行的 resolver：

```java
public class AcmecArtifactResourceResolver extends AbstractArtifactResourceResolver {

    public AcmecArtifactResourceResolver() {
        super(0); // 数值小于 Maven resolver 的 DEFAULT_PRIORITY = 1 => 先被调用
    }

    @Override
    public Artifact resolve(URL resourceURL) {
        // 返回 null 表示把该 URL 交给下一个 resolver
        return null;
    }
}
```

对于 `ServiceLoaderUtils.getServiceClasses(...)` / `getServiceClassNames(...)`，实现类按名字解析后会校验其是否
属于服务类型；`failFast = true`（默认）时，无法加载或类型不符会抛 `IllegalStateException`，
`failFast = false` 时跳过该项。

---

## 3. 系统属性与可调参数

只列出源码中确实存在的键。"读取方"指在类初始化阶段读取该值的类。

| 键 | 默认值 | 读取方 | 作用 |
|---|---|---|---|
| `microsphere.service-loader.cached` | `false` | `io.microsphere.util.ServiceLoaderUtils` | 对未显式传 `cached` 参数的 `ServiceLoaderUtils` 调用启用进程级 `servicesCache` |
| `microsphere.io.buffer.size` | `2048` | `io.microsphere.io.IOUtils` | 流拷贝与读取资源时使用的缓冲区大小 |
| `microsphere.shutdown-hook.callbacks-capacity` | `512` | `io.microsphere.util.ShutdownHookUtils` | 存放 shutdown-hook 回调的优先队列初始容量 |
| `microsphere.reflect.resolved-generic-types.cache.size` | `256` | `io.microsphere.reflect.TypeUtils` | 泛型解析缓存的初始容量 |
| `microsphere.reflect.banned-methods` | *(未设置)* | `io.microsphere.reflect.MethodUtils` | 以竖线分隔的方法签名，例如 `java.lang.String#substring() \| java.lang.String#substring(int,int)`；命中的方法被视为不存在 |
| `microsphere.bean.properties.max-resolved-depth` | `100` | `io.microsphere.beans.BeanUtils` | 嵌套 bean 属性递归解析的深度上限 |
| `microsphere.bean.metadata.cache.size` | `64` | `io.microsphere.beans.BeanUtils` | `BeanMetadata` 缓存的初始容量 |
| `microsphere.file-watch-service.thread-name-prefix` | `microsphere-file-watch-service` | `io.microsphere.io.StandardFileWatchService` | 文件监听事件循环线程名前缀 |
| `microsphere.artifact-id.manifest-attribute-names` | `Bundle-Name,Automatic-Module-Name,Implementation-Title` | `io.microsphere.classloading.ManifestArtifactResourceResolver` | 以逗号分隔、按序取值的 `META-INF/MANIFEST.MF` 属性名，用作 artifact id |
| `microsphere.artifact-version.manifest-attribute-names` | `Bundle-Version,Implementation-Version` | `io.microsphere.classloading.ManifestArtifactResourceResolver` | 同上，用于 artifact version |
| `process.execution.timeout` | `30000`（毫秒） | `io.microsphere.process.ProcessExecutor` | 执行外部进程时的默认超时 |

库读取的标准 JDK 属性（不属于 microsphere，但行为依赖它们）：

| 键 | 读取方 | 作用 |
|---|---|---|
| `java.protocol.handler.pkgs` | `io.microsphere.net.URLUtils`、`io.microsphere.net.ExtendableProtocolURLStreamHandler` | 冒号分隔的包列表，JDK 在其中查找协议 handler；实例化 `ExtendableProtocolURLStreamHandler` 时 microsphere 会**追加**自己的 handler 包 |
| `java.util.PropertyResourceBundle.encoding` | `io.microsphere.util.PropertyResourceBundleUtils` | `PropertyResourceBundle` 的默认编码，未设置时回退到平台 file encoding |
| `java.security.policy` | `io.microsphere.security.SecurityUtils` | 定位安全相关辅助类使用的 policy 文件 |
| `java.version`、`java.specification.version`、`java.class.path`、`java.home` | `io.microsphere.util.SystemUtils`、`io.microsphere.classloading.ArtifactDetector` | JDK 版本判定、classpath 枚举、JDK 自身库过滤 |

---

## 4. 资源路径

| 路径 | 写入方 / 读取方 | 说明 |
|---|---|---|
| `META-INF/services/` | `ServiceLoaderUtils.SERVICES_PROVIDER_LOCATION` | 同时提供模式串 `META-INF/services/{}`（`SERVICE_PROVIDER_CONFIG_FILES_LOCATION_PATTERN`） |
| `META-INF/` | `ResourceConstants.METADATA_RESOURCE` | 元数据根目录 |
| `META-INF/microsphere/` | `ResourceConstants.MICROSPHERE_METADATA_RESOURCE` | Microsphere 元数据根目录 |
| `META-INF/microsphere/configuration-properties.json` | 由 `ConfigurationPropertyAnnotationProcessor` 通过 `Filer` 写入（`CLASS_OUTPUT`）；常量 `CONFIGURATION_PROPERTY_METADATA_RESOURCE`；由 `MetadataResourceConfigurationPropertyLoader` 读取 | `microsphere-java-core.jar` 自带一份，其中列出了第 3 节的每个 `@ConfigurationProperty` 键 |
| `META-INF/microsphere/additional-configuration-properties.json` | `AdditionalMetadataResourceConfigurationPropertyLoader`（已注册的 SPI） | 手工补充的元数据，合并进 loader 链；优先级 `MIN_PRIORITY + 9` |
| `META-INF/MANIFEST.MF` | `JarUtils.MANIFEST_RESOURCE_PATH`、`ManifestArtifactResourceResolver` | artifact id / version 的属性名可配置（见第 3 节） |
| `META-INF/maven/**/pom.properties` | `MavenArtifactResourceResolver`（`MAVEN_POM_PROPERTIES_RESOURCE_PREFIX` + `/pom.properties`） | 读取的键：`groupId`、`artifactId`、`version` |
| `META-INF/banned-artifacts` | `BannedArtifactClassLoadingExecutor.CONFIG_LOCATION` | 每行一个禁用构件，用于类加载校验 |

> [!NOTE]
> `io.microsphere.classloading.StreamArtifactResourceResolver` 是从归档中读取元数据条目的抽象基类，故意没有
> 出现在服务文件里 —— 只注册具体实现。

---

## 5. JDK 兼容矩阵

| 关注点 | JDK 8 | JDK 9 – 15 | JDK 16+ |
|---|---|---|---|
| `URLClassPath` 访问 | `ClassicURLClassPathHandle`（`sun.misc.URLClassPath`，字段 `urls`） | `ModernURLClassPathHandle`（`jdk.internal.loader.URLClassPath`，字段 `unopenedUrls`） | `ModernURLClassPathHandle`，并需要开放模块 |
| 两者都不支持时的兜底 | `NoOpURLClassPathHandle`（`supports()` 恒为 `true`，不返回 URL，`removeURL` 返回 `false`） | 同左 | 同左 |
| 进程号 | `VirtualMachineProcessIdResolver`（反射 `sun.management` 的 `jvm` 字段，优先级 5），随后 `ClassicProcessIdResolver`（优先级 9） | `ModernProcessIdResolver`（`java.lang.ProcessHandle`，优先级 1）胜出 | `ModernProcessIdResolver` 胜出；反射式 resolver 仅在加了 `--add-opens` 时可用 |
| 非法反射访问 | 不涉及 | `--illegal-access=permit`（构建 profile `java9-15`） | 强封装；对未开放包调用 `setAccessible` 抛 `InaccessibleObjectException` |
| 日志委托解析 | 完全一致 | 完全一致 | 完全一致（通过 `LoggerFactory` 的类加载器解析委托类） |

构建父 POM `io.github.microsphere-projects:microsphere-build:0.3.16` 按 `<jdk>` 区间激活 profile：

| Profile | 激活区间 | 行为 |
|---|---|---|
| `java8-16`（本仓库 `microsphere-java-parent`） | `[1.8,17)` | 老 JDK 下把 Spring Framework 固定为 `5.3.39`；`[17,)` 使用 `7.0.9` |
| `java9+` | `[9,)` | 设置 `maven.compiler.release`（仍为 `8`） |
| `java11+` | `[11,)` | 调整 Javadoc `<source>` 与相关工具版本 |
| `java9-15` | `[9,15]` | `jvm.argLine = --illegal-access=permit` |
| `java16+` | `[16,)` | 以 fork 方式给 `javac` 加 `-J--add-opens=java.base/java.lang=ALL-UNNAMED` 与 `-J--add-opens=java.base/java.lang.invoke=ALL-UNNAMED`；`jvm.argLine` 设为同样的两个参数，并传给 Surefire 的 `<argLine>@{jacoco.argLine} ${jvm.argLine}` |

CI（`.github/workflows/maven-build.yml`）在 `ubuntu-latest` 上以 `temurin` 构建矩阵 `['8','11','17','21','25']`，
命令为 `mvn ... -Drevision=0.0.1-SNAPSHOT -Dsurefire.useSystemClassLoader=false test --activate-profiles
test,coverage`。

> [!IMPORTANT]
> 若你在自己的矩阵中加入更新的 JDK 并遇到反射访问失败（涉及 `AbstractURLClassPathHandle`、
> `VirtualMachineProcessIdResolver`、`URLUtils.getURLStreamHandlerFactory()`），请把上述两个 `--add-opens`
> 加进自己的 `argLine`，并额外开放你实际访问的包，例如 `java.base/jdk.internal.loader`。

---

## 6. 模块与 BOM

所有构件的 `groupId` 均为 `io.github.microsphere-projects`；`${revision}` 当前为 `0.3.20-SNAPSHOT`。

| artifactId | 打包方式 | 用途 |
|---|---|---|
| `microsphere-java` | `pom` | reactor 根，聚合全部模块，继承外部父 POM `microsphere-build` |
| `microsphere-java-parent` | `pom` | 库模块的父 POM；导入 `microsphere-all-bom` 与 `spring-framework-bom`，增加 `java8-16` JDK profile |
| `microsphere-java-dependencies` | `pom` | BOM：为下面 7 个 jar 模块提供 `dependencyManagement` |
| `microsphere-java-annotations` | `jar` | 语义注解（`@Since`、`@Nullable`、`@Nonnull`、`@Immutable`、`@Experimental`、`@ThreadSafe`、`@ConfigurationProperty`） |
| `microsphere-java-core` | `jar` | 全部 `io.microsphere.*` 工具，以及第 1 节列出的所有 SPI |
| `microsphere-jdk-tools` | `jar` | `io.microsphere.jdk.tools.compiler.Compiler` —— 以编程方式调用 `javac` |
| `microsphere-java-test` | `jar` | JUnit 5 夹具与测试辅助类 |
| `microsphere-annotation-test` | `jar` | `AbstractAnnotationProcessingTest`，在测试进程内跑一次真实的注解处理编译 |
| `microsphere-lang-model` | `jar` | `javax.lang.model` 辅助能力（element、type、message） |
| `microsphere-annotation-processor` | `jar` | `ConfigurationPropertyAnnotationProcessor`，通过 `META-INF/services/javax.annotation.processing.Processor` 注册 |

BOM（`microsphere-java-dependencies`）只以 `${revision}` 管理这 7 个构件：`microsphere-java-annotations`、
`microsphere-java-core`、`microsphere-jdk-tools`、`microsphere-java-test`、`microsphere-annotation-test`、
`microsphere-lang-model`、`microsphere-annotation-processor`。第三方版本由 `microsphere-java-parent` 导入的
`microsphere-all-bom` 与 `spring-framework-bom` 提供。

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-java-dependencies</artifactId>
            <version>0.3.19</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

`microsphere-java-core` 把 `javax.annotation-api`、`slf4j-api`、`commons-logging` 声明为 `optional`
编译依赖；JUnit、Logback、Spring Core、JMH 只出现在 `test` scope。

---

## 7. 故障排查

| 现象 | 原因 | 解决办法 |
|---|---|---|
| `IllegalArgumentException: No Service interface[type : ...] implementation was defined in service loader configuration file[/META-INF/services/...]` | 服务文件缺失、命名不符，或不在 `ServiceLoaderUtils` 使用的那个类加载器上（`loadServicesAsList` 从不返回空列表） | 用接口的全限定**二进制**名命名文件；`mvn package` 后确认文件已在 jar 内；必要时改用带 `ClassLoader` 的重载 |
| `getServiceClasses` 抛 `IllegalStateException: The service class[name : '{}'] can't be loaded by {}` | 服务文件中某条实现类在该类加载器上不存在，而 `failFast` 为默认的 `true` | 删除失效条目、补齐依赖，或调用 `getServiceClasses(type, classLoader, false)` 跳过无法加载的项 |
| `ServiceConfigurationError`，或实现被静默忽略 | 类缺少 public 无参构造器，或者根本没有实现该服务接口 | 补上无参构造器；核对 `implements` 声明 —— `Prioritized` 子接口必须严格匹配 |
| 你的 `Prioritized` 实现总是排不到前面 | 优先级方向：数值越小越靠前。内置实现已占用 `0`、`1`、`5`、`9` 以及 `MAX_PRIORITY + 99999` | 返回比目标内置实现更小的数值，或直接使用 `Prioritized.MAX_PRIORITY` |
| `NoClassDefFoundError: org/slf4j/Logger`（或 `org/apache/commons/logging/Log`） | 可选依赖在编译期存在、运行期缺失，而代码直接触达了适配器类而不是门面 | 业务代码只使用门面 `io.microsphere.logging.LoggerFactory.getLogger`，或者把真正的 `slf4j-api` / `commons-logging` 依赖加回来 |
| 日志全部消失 | 没有任何委托类可解析，最终由 `NoOpLoggerFactory` 通过 `isAvailable()` 判定胜出 | 引入 SLF4J（`java.util.logging` 一定存在，检查是否被你自己屏蔽） |
| JDK 17/21/25 上抛 `InaccessibleObjectException` | `AbstractURLClassPathHandle`、`VirtualMachineProcessIdResolver`、`URLUtils.getURLStreamHandlerFactory()` 等路径对 JDK 内部成员调用 `setAccessible(true)` | 加上 `--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED`（构建的 `java16+` profile 正是这么做），并额外开放你访问的具体包 |
| `URL.setURLStreamHandlerFactory` 只能调用一次 / 抛 `IllegalStateException` | 其他库已经占用了 JVM 级 factory | 改用 `URLUtils.attachURLStreamHandlerFactory(factory)`：它反射读取现有 factory，把新旧两者包进 `CompositeURLStreamHandlerFactory`，清空后再安装；注意仍然注册两个普通 factory 依旧会失败 |
| `classpath:` / `console:` URL 抛未知协议 | 从未调用 `ServiceLoaderURLStreamHandlerFactory.attach()`，而 `java.protocol.handler.pkgs` 只在 JDK 自身的 handler 查找中生效 | 启动时调用一次 `ServiceLoaderURLStreamHandlerFactory.attach()`；并确保 `Handler` 是顶层类、类名恰为 `Handler`、位置为 `<package>.<protocol>.Handler` |
| `META-INF/microsphere/configuration-properties.json` 没有生成 | 处理器没跑：未配置 `<annotationProcessorPaths>`、使用了 `-proc:none`，或编译时缺少处理器 jar | 把 `microsphere-annotation-processor` 加入 `annotationProcessorPaths`；确认 `@SupportedAnnotationTypes` 能匹配到 `io.microsphere.annotation.ConfigurationProperty`；JDK 16+ 保留两个 `-J--add-opens` 编译参数 |
| 某个 `@ConfigurationProperty` 键没有出现在生成的元数据里 | 元数据由当轮 root elements 加上 `ConfigurationPropertyGenerator` SPI 生成，运行期才产生的键在编译期不可见 | 在常量上标注 `@ConfigurationProperty`，或提供一个 `ConfigurationPropertyLoader` 来补充 |
| 测试类从未执行 | Surefire 只包含 `**/*Test.java` 与 `**/*Tests.java`，并排除 `**/Abstract*.java` | 改名为 `FooTest` / `FooTests`；公共基类继续保持 `Abstract` 前缀 |
| `microsphere.*` 系统属性看起来没生效 | 值在 `static final` 初始化块里只读一次，通常早于你的 `System.setProperty` | 改为命令行 `-D`，或写入 `surefire.argLine` |
| JDK 9+ 上构件/classpath 探测结果为空 | `findAllClassPathURLs` 完全依赖 `URLClassPathHandle.getURLs`，进而依赖 `ClassLoaderUtils.findURLClassLoader` 向上寻找 `URLClassLoader`。当 `ModernURLClassPathHandle.supports()` 为 `false`（无法访问 `jdk.internal.loader.URLClassPath` 或 `ucp` 字段）且祖先加载器中没有 `URLClassLoader` 时结果为空，只有 `NoOpURLClassPathHandle` 命中 | 开放相应模块（`--add-opens java.base/jdk.internal.loader=ALL-UNNAMED`），或让代码运行在 `URLClassLoader` 下（这就是 CI 传 `-Dsurefire.useSystemClassLoader=false` 的原因），或自行准备 URL 后调用 `ArtifactDetector.detect(Set<URL>)` |

---

## 8. 延伸阅读

| 子系统 | 页面 |
|---|---|
| 服务加载、`XxxUtils` 约定 | [核心工具](core-utilities.md) |
| `Prioritized`、比较器、可抛异常的函数式接口 | [语言抽象](language-abstractions.md) |
| `Converter` / `MultiValueConverter` SPI | [类型转换](type-conversion.md) |
| `EventDispatcher` 与 `EventListener` 自动加载 | [事件分发](events.md) |
| `IOUtils.BUFFER_SIZE`、`StandardFileWatchService` | [I/O 与文件监听](io-and-file-watch.md) |
| `java.protocol.handler.pkgs`、协议 handler | [网络与 URL 协议](networking-and-url-protocols.md) |
| `LoggerFactory` 委托链 | [日志](logging.md) |
| `ProcessIdResolver`、`ProcessExecutor` 超时 | [并发、进程与 JMX](concurrency-process-jmx.md) |
| `ArtifactResourceResolver`、`URLClassPathHandle`、禁用构件 | [类加载与构件](classloading-and-artifacts.md) |
| Reader / Loader / Generator 链路 | [配置属性元数据](configuration-metadata.md) |
| 处理器注册与 `--add-opens` | [注解处理](annotation-processing.md) |
| Surefire 包含/排除规则 | [测试支持](testing.md) |

---

## 参见

* [快速开始](getting-started.md) —— 坐标、BOM 引入、第一次调用。
* [注解](annotations.md) —— `@ConfigurationProperty`，元数据生成的输入。
* 上游指南：`docs/user-guide/README.md`（仅英文，按模块组织的任务指南）。

[← 手册目录](../README.md) · [上一篇：测试支持](testing.md)
