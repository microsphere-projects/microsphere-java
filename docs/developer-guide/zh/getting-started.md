# 快速开始

> 语言版本：[中文](getting-started.md) · [English](../en/getting-started.md)
> [← 手册目录](../README.md)

## 一览

| 项目 | 取值 |
|---|---|
| 所有构件的 Group ID | `io.github.microsphere-projects` |
| 当前开发版本 | `0.3.20-SNAPSHOT`，最新发布版 `0.3.19` |
| Java 版本 | 以 **Java 8** 编译，在 JDK **8 / 11 / 17 / 21 / 25** 上测试 |
| 包根路径 | `io.microsphere.*` |
| 第三方依赖 | 编译期为零依赖，仅提供可选集成 |

`microsphere-java` 是一个基础类库，用来补齐 JDK 标准库的空白：空安全集合、反射、类型转换、类加载、
事件分发、日志门面、JSON 等，且不强加任何框架。如果只记一件事：引入 `microsphere-java-core`，
然后直接调用各 `XxxUtils` 的静态方法。

---

## 1. 环境要求

| 要求 | 说明 |
|---|---|
| Java | 8 及以上。所有模块均设置 `maven.compiler.source/target = 8`。 |
| 已测试的 JDK | 8、11、17、21、25（GitHub Actions 矩阵，`temurin`，`ubuntu-latest`） |
| Maven | 使用者 3.6+ 即可；仓库自身通过 `mvnw` 固定使用 Maven `3.9.16` |
| 构建父 POM | `io.github.microsphere-projects:microsphere-build:0.3.16`，是外部构件，从 Maven Central 解析 |

`microsphere-java-core` 不依赖任何框架。以下能力在你把相应库放到 classpath 后自动生效：

| classpath 中存在 | 效果 |
|---|---|
| `org.slf4j:slf4j-api` | `io.microsphere.logging` 委托给 SLF4J（优先级最高） |
| `commons-logging` | 回退委托，SLF4J 缺失时使用 |
| `javax.annotation-api`（JSR-250/305） | 为 `microsphere-java-annotations` 提供注解元模型支持 |
| JDK（而非 JRE） | `microsphere-jdk-tools` 与 `microsphere-annotation-test` 的必要条件 |

---

## 2. 该引入哪个模块

| 你的任务 | 引入的 artifactId |
|---|---|
| 应用中使用工具方法 | `microsphere-java-core` |
| 在 API 上标注 `@Since` / `@Nullable` / `@Nonnull` / `@ConfigurationProperty` | `microsphere-java-annotations` |
| 生成 IDE 可识别的配置元数据 | `microsphere-annotation-processor`（仅编译期） |
| 单元测试夹具（模型、服务） | `microsphere-java-test`（`test` 作用域） |
| 测试自己编写的注解处理器 | `microsphere-annotation-test`（`test` 作用域） |
| `javax.lang.model.*` 辅助方法 | `microsphere-lang-model` |
| 在进程内驱动 `javax.tools.JavaCompiler` | `microsphere-jdk-tools` |

`microsphere-java-core` 已经传递依赖 `microsphere-java-annotations`，因此多数项目只需一个依赖。

整个 reactor 构建九个模块：`microsphere-java-parent`（`pom`）是共享父 POM，
`microsphere-java-dependencies`（`pom`）是 BOM。BOM 精确管理以下七个 jar 模块：
`microsphere-java-annotations`、`microsphere-java-core`、`microsphere-jdk-tools`、
`microsphere-java-test`、`microsphere-annotation-test`、`microsphere-lang-model`、
`microsphere-annotation-processor`。

---

## 3. 添加依赖

### 3.1 Maven：导入 BOM

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-java-dependencies</artifactId>
            <version>0.3.19</version> <!-- 或任意已发布版本 -->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

随后只声明真正需要的模块，不必写版本号：

```xml
<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-java-core</artifactId>
    </dependency>

    <!-- 仅编译期使用：生成配置元数据 -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-annotation-processor</artifactId>
        <optional>true</optional>
    </dependency>

    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-java-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

不使用 BOM 时，需要自行固定每个版本，例如
`io.github.microsphere-projects:microsphere-java-core:0.3.19`。

### 3.2 Gradle

```groovy
dependencies {
    implementation platform("io.github.microsphere-projects:microsphere-java-dependencies:0.3.19")

    implementation "io.github.microsphere-projects:microsphere-java-core"

    annotationProcessor "io.github.microsphere-projects:microsphere-annotation-processor"

    testImplementation "io.github.microsphere-projects:microsphere-java-test"
    testImplementation "io.github.microsphere-projects:microsphere-annotation-test"
}
```

### 3.3 预期的传递依赖

* `microsphere-java-core` → `microsphere-java-annotations`（compile）；`javax.annotation-api`、
  `slf4j-api`、`commons-logging` 全部为 `optional`。
* `microsphere-lang-model`、`microsphere-jdk-tools` → `microsphere-java-core`。
* `microsphere-annotation-processor` → `microsphere-java-core` + `microsphere-lang-model`。它的构建会把
  `microsphere-java-core` **shade 进处理器 jar**，因此即使应用本身不依赖 core，处理器也能独立工作。
* `microsphere-java-test` 与 `microsphere-annotation-test` 把全部依赖声明为 `optional` 以便复用，
  也就是说 **需要你自己** 提供 JUnit Jupiter、Mockito、Logback、`javax.ws.rs-api`、`jaxws-api`、
  `spring-context` / `spring-web`，供引用这些类型的夹具使用。

> [!TIP]
> 由于这些可选依赖确实是可选的，测试中访问 Spring 或 JAX-RS 夹具时出现 `NoClassDefFoundError`，
> 通常意味着测试模块需要补上对应依赖，而不是 Microsphere 本身有问题。

---

## 4. 第一个调用

```java
import io.microsphere.collection.ListUtils;
import io.microsphere.convert.Converter;
import io.microsphere.util.ClassLoaderUtils;

Integer count = Converter.convertIfPossible("42", Integer.class);      // 经 Converter SPI 解析出转换器
List<String> tags = ListUtils.of("a", "b", "c");                       // 不可修改，且永不为 null
ClassLoader loader = ClassLoaderUtils.getClassLoader(tags.getClass()); // 空安全，不抛异常
```

整个库只有两种调用习惯。工具类都是 `public abstract class XxxUtils implements Utils` 且构造器为
private，因此你写的永远是 `XxxUtils.method(...)`。基于 SPI 的子系统把静态入口放在接口自身上，
例如 `Converter.getConverter(sourceType, targetType)`、`Converter.convertIfPossible(source, targetType)`，
而注册中心类（`Converters`）保持包级私有。

---

## 5. 与 Spring 的版本对齐

`microsphere-java-parent` 导入了 `spring-framework-bom`，并按 JDK 切换所解析的版本线：

| JDK | Spring Framework（构建期） |
|---|---|
| `[1.8, 17)` — profile `java8-16` | `5.3.39` |
| `[17, ∞)` | `7.0.9` |

> [!IMPORTANT]
> 这是**本仓库内部**的构建期对齐，并不是对你应用的约束。
> `microsphere-java-core` 完全不依赖 Spring；只有 `microsphere-java-test` 引用 Spring 类型，
> 且把这些依赖标记为 `optional`。你的 Spring Boot / Framework 版本照常生效，这里不会强迫你升级。

---

## 6. 从源码构建

只有在需要修改该库或运行其测试时才需要：

```bash
git clone https://github.com/microsphere-projects/microsphere-java.git
cd microsphere-java

./mvnw package -DskipTests                       # 首次快速构建
./mvnw test                                      # 完整测试
./mvnw test --activate-profiles test,coverage     # 与 CI 一致（附带 JaCoCo、failsafe 等）
./mvnw test -pl microsphere-java-core            # 只构建一个模块
./mvnw test -pl microsphere-java-core -Dtest=StringUtilsTest   # 只跑一个测试类
```

Windows 下使用 `mvnw.cmd`。wrapper 为 `only-script` 类型（`wrapperVersion=3.3.4`，
Maven `apache-maven-3.9.16`）。

### 6.1 JDK 16+ 的注意事项

以下处理由 `microsphere-build` 继承的 profile 完成，无需手工添加参数：

| profile | 激活条件 | 作用 |
|---|---|---|
| `java9+` | `[9,)` | 设置 `maven.compiler.release=${java.version}`（即 8） |
| `java16+` | `[16,)` | 以 `-J--add-opens=java.base/java.lang=ALL-UNNAMED` 与 `java.base/java.lang.invoke` 分叉编译；并通过 `jvm.argLine` 把同样的 `--add-opens` 传给 Surefire |
| `java9-15` | `[9,15]` | `jvm.argLine=--illegal-access=permit` |

如果你在自己的矩阵里加入了更新的 JDK 并遇到反射访问失败，把上面两条 `--add-opens` 加到自己的
`argLine` 即可。

### 6.2 CI 实际执行的命令

```bash
mvn --batch-mode --update-snapshots --file pom.xml \
    -Drevision=0.0.1-SNAPSHOT \
    -Dsurefire.useSystemClassLoader=false \
    test --activate-profiles test,coverage
```

发布由另一个工作流负责：在推送到 `release` 分支时，通过 `central-publishing-maven-plugin`
（`server-id: ossrh`、`autoPublish=true`）部署到 Maven Central。

### 6.3 测试类的命名

Surefire 包含 `**/*Test.java` 与 `**/*Tests.java`，并**排除 `**/Abstract*.java`** ——
这正是共享测试基类命名为 `AbstractAnnotationProcessingTest` 的原因。

---

## 7. 版本与发布说明

* 工作树中的 `${revision}` 为 `0.3.20-SNAPSHOT`，它决定所有模块的版本。
* 发布版会打一个纯版本号 git tag（例如 `0.3.19`），同时创建对应的 GitHub Release。
* `release-notes.md` 由发布工作流**追加**写入，因此最旧的条目（`v0.2.7`）在最上方，最新的在最下方；
  最新说明以 GitHub Release 为准。

> [!WARNING]
> `${revision}` 是 Maven 的 CI-friendly 属性，会在构建时被 flatten 展开。本地修改它会让 reactor 中
> 每个模块的版本同时变化，BOM 中的条目也不例外。

---

## 参见

* [注解](annotations.md) —— 五分钟读完，适用于任何代码库。
* [核心工具](core-utilities.md) —— 最常被引用的一个页面。
* [参考手册](reference.md) —— 汇总全部 SPI 文件、系统属性与可调参数。

[← 手册目录](../README.md) · [下一篇：注解 →](annotations.md)
