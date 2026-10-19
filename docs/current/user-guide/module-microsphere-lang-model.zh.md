# 语言模型（`javax.lang.model`）工具集

| | |
|---|---|
| **构件** | `io.github.microsphere-projects:microsphere-lang-model`（`release` 线上为 `0.3.20-SNAPSHOT`；已发布标签 `0.3.20`） |
| **包** | `io.microsphere.lang.model.element`（1 个类型）与 `io.microsphere.lang.model.util`（15 个类型）——**共 16 个主源类型** |
| **包装对象** | `javax.lang.model` —— `Element`、`TypeMirror`、`DeclaredType`、`AnnotationMirror`、`AnnotationValue`、`Elements`、`Types`、`Messager`、`ProcessingEnvironment` |
| **JDK 级别** | 整个 reactor 中不存在任何 `maven.compiler.*` / `<release>` 属性；字节码级别继承自外部构建父工程 `microsphere-build:0.3.16`。CI 在 Temurin JDK 8 / 11 / 17 / 21 / 25 上验证 |
| **编译期依赖** | `microsphere-java-core`（`microsphere-lang-model/pom.xml:24-28`，版本 `${revision}`） |
| **仅测试依赖** | `microsphere-jdk-tools`（`:44-49`）、`microsphere-annotation-test`（`:52-57`），另有 JUnit 5、Logback、`javax.ws.rs-api`、`jaxws-api`、`spring-context`、`spring-web` |
| **被谁使用** | `microsphere-annotation-processor` —— 本模块是该配对中的构建期元数据一半 |

所有 `file:line` 引用均相对于 `microsphere-lang-model/src/main/java/io/microsphere/`（主源码）或
`microsphere-lang-model/src/test/java/io/microsphere/`（测试），读取自 **`release` 分支工作树**。首次提及之后简化为
`lang/model/util/TypeUtils.java:60-88`。跨模块引用保留模块名前缀，例如
`microsphere-java-core/src/main/java/io/microsphere/reflect/TypeUtils.java:1543`。

---

## 1. 谁需要本模块，它消除了什么

**如果你要编写注解处理器，就需要本模块** —— 无论是 `javax.annotation.processing.AbstractProcessor`、构建期元数据生成器、API 文档抽取器，还是任何运行在 `javac` 之下、检查源码结构而非已加载类的程序。

在构建的这个阶段你**并不**拥有 `Class`、`Method`、`Field`。你拥有的是 `Element`、`TypeMirror`、`ExecutableElement`、`AnnotationMirror` 和 `AnnotationValue`，而向它们提问的唯一途径是从 `ProcessingEnvironment` 取得的两个 `javax.lang.model.util` 服务 `Elements` 与 `Types`。原始 API 的冗长体现在四个具体方面，本模块逐一消除：

* **Kind 判断既无类型又不为空安全。** `element.getKind() == ElementKind.CLASS` 会漏掉 `ENUM` 和 `RECORD`，而 `ElementKind#isClass()` 遇到 `null` 会抛异常。`ElementUtils` 提供空安全的 `isClass`/`isInterface`/`isField`/`isExecutable`/`isMember`/`isVariable`/`isInitializer`（`lang/model/util/ElementUtils.java:87-296`）。
* **不存在 `TypeMirror` 与 `java.lang.reflect.Type` 的比较。** 没有任何 `Elements`/`Types` 方法能回答“这个 `TypeMirror` 是否就是 `java.lang.String`？”。`TypeUtils.isSameType` 可以，且有四个重载（`lang/model/util/TypeUtils.java:177-266`）。
* **注解属性提取是一趟四层手工遍历**：`AnnotationMirror` → `getAnnotationType()` → 属性 `ExecutableElement` → `getElementValues()` → `AnnotationValue` → `getValue()` —— 而且除非你重新读取 `ExecutableElement.getDefaultValue()`，默认值在 `getElementValues()` 中是*缺失*的。`AnnotationUtils.getElementValues` 完成整趟遍历并合并默认值（`lang/model/util/AnnotationUtils.java:1509-1529`）。
* **诊断需要两个通道。** `Messager` 写入构建输出；`Logger` 写入处理器自身的日志，而通常你希望用同一条消息模式同时得到两者。`MessagerUtils.printMessage` 正是如此（`lang/model/util/MessagerUtils.java:292-309`）。

**当**你已拥有加载好的 `Class` 对象时**不要使用本模块** —— 那属于 `microsphere-java-core` 中的 `io.microsphere.reflect`，它更快且不依赖 `ProcessingEnvironment`。同样，不要在 `jlink` 精简镜像的运行期使用它：`javax.lang.model` 位于 `java.compiler` 模块，可能不存在。

---

## 2. 坐标与依赖

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-lang-model</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

| 事实 | 取值 | 证据（`release`） |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | 根 `pom.xml:12` |
| `artifactId` | `microsphere-lang-model` | `microsphere-lang-model/pom.xml:14` |
| 版本属性 | `${revision}` | `pom.xml:15` |
| `release` 线上 `${revision}` 的值 | `0.3.20-SNAPSHOT` | 根 `pom.xml:54` |
| 最新已发布标签 | `0.3.20` | `release` 分支的发布线 |
| 构建父工程 | `microsphere-build:0.3.16`（外部） | reactor 中不存在 `maven.compiler.*` / `<release>` 属性 |
| 编译依赖 | `microsphere-java-core`，`${revision}` | `pom.xml:24-28` |
| 测试依赖 | JUnit 5 `:31-41`、`microsphere-jdk-tools` `:44-49`、`microsphere-annotation-test` `:52-57`、Logback `:60-64`、`javax.ws.rs-api` `:67-71`、`jaxws-api` `:74-78`、`spring-context` `:81-85`、`spring-web` `:87-91` | 同上 |
| CI 矩阵 | Temurin JDK `8, 11, 17, 21, 25` | `.github/workflows/maven-build.yml:25`、`distribution: 'temurin'` `:33`、`-Dsurefire.useSystemClassLoader=false` `:43` |

> [!IMPORTANT]
> `microsphere-jdk-tools` 与 `microsphere-annotation-test` 都是 **`<scope>test</scope>`**（`pom.xml:48`、`:56`）。它们不会出现在你的编译 classpath 上，你的处理器也不会传递性地得到它们。只有要运行本模块自身测试套件时才需要（见 §10 陷阱 1）。

`microsphere-java-core` 是**唯一**的 compile 作用域依赖，而本模块大量使用它：`io.microsphere.util.*`、`io.microsphere.collection.*`、`io.microsphere.lang.function.*`、`io.microsphere.reflect.*`、`io.microsphere.json.JSONUtils`、`io.microsphere.text.FormatUtils`、`io.microsphere.logging.*`、`io.microsphere.constants.SymbolConstants` 与 `io.microsphere.annotation.*` 全部来自那里。

---

## 3. 版本信息与兼容性

| 问题 | 回答 | 证据 |
|---|---|---|
| 发布线 | `release` 分支，`${revision}` = `0.3.20-SNAPSHOT`，已发布标签 `0.3.20` | 根 `pom.xml:54` |
| 全部 16 个类型的 `@Since` | `1.0.0` | `lang/model/util/ElementUtils.java:56`、`lang/model/util/TypeUtils.java:67`、`lang/model/util/AnnotationUtils.java:99`、`lang/model/element/StringAnnotationValue.java:30`、`lang/model/util/MessagerUtils.java:41`、`lang/model/util/ExecutableElementComparator.java:53` |
| 包命名空间 | **`javax.lang.model`，绝不存在 `jakarta.lang.model`** | 每个主源码都 import `javax.lang.model.*`，例如 `lang/model/util/ElementUtils.java:24-28`；`src/main/java` 中不存在任何 `jakarta.*` import |
| `javax.lang.model` API 可用性 | `Element`/`Types`/`Elements`/`ElementVisitor`/`AnnotationValueVisitor` 自 JDK 6 起存在；本模块所继承的基类 `ElementKindVisitor6`、`SimpleAnnotationValueVisitor6`、`AbstractElementVisitor6` 是 **JDK 6** 产物，并自 **JDK 9** 起被 `...8`/`...14` 变体取代而废弃 | `lang/model/util/JSONElementVisitor.java:27-28,57`、`lang/model/util/JSONAnnotationValueVisitor.java:25,72`、`lang/model/util/ResolvableAnnotationValueVisitor.java:27,74` |
| 实际测试过的 JDK 范围 | 8、11、17、21、25 | `.github/workflows/maven-build.yml:25` |
| Spring 版本 | 与 `src/main/java` 无关（无 Spring import）；`spring-context`/`spring-web` 仅测试用 | `pom.xml:81-91`；父工程属性 `microsphere-java-parent/pom.xml:21-24`（`microsphere-bom.version` `0.3.13` 在 `:22`，`spring-framework.version` `7.0.9` 在 `:23`）、BOM 导入 `:31-37` 与 `:40-46`、profile `java8-16` `:54-62` 中 `<jdk>[1.8,17)</jdk>` 在 `:57`，将 Spring 固定为 `5.3.39` 在 `:60` |
| `RECORD` / `BINDING_VARIABLE` kind | 按**名称**匹配而非按枚举常量，因此模块在 JDK 8 上仍可编译 | `lang/model/util/ElementUtils.java:358-359` |

> [!NOTE]
> 直接回答包命名空间问题：**是的，本模块针对 `javax.*` 编译，而不是 `jakarta.*`。** JDK 从未重命名 `javax.lang.model` —— Jakarta EE 的命名空间变更只作用于 Jakarta EE API，不作用于 Java 语言模型。因此不存在迁移到“jakarta.lang.model”的路径；即便使用 `jakarta.annotation` 的处理器，也仍然通过本模块所包装的 `javax.lang.model` API 检查其元素。模块中唯一带 Jakarta 色彩的 import 都是测试作用域且仍是 `javax` 的（`pom.xml:67-78`）。


### 3.1 `0.3.20` 的发布日期与变更摘要

| 条目 | 值 | 依据 |
|---|---|---|
| 文档所在线 | `release` 分支，`${revision}` = `0.3.20-SNAPSHOT` | 根 `pom.xml:54` |
| 最新已发布版本 | `0.3.20`，标签日期 **2026-10-16** | 标签 `0.3.20` 的提交信息为 `chore: merge main into release [skip ci]`，提交时间 `2026-10-16T03:43:20Z` |
| 上一版本 | `0.3.19`，标签日期 **2026-08-30** | 标签 `0.3.19`，提交时间 `2026-08-30T12:51:37Z` |
| 变更日志来源 | 仓库根目录的 `release-notes.md` | 本项目不发布 `CHANGELOG.md`；`release-notes.md` 的 `v0.3.16`–`v0.3.20` 段落写着 *Release notes generation failed* 并保留原始提交列表，因此下文一律以提交主题引用，并逐条与 `release` 源码核对。 |

`0.3.20` 在这一区域没有新增 API 面——`0.3.19...0.3.20` 区间是一次加固性发布。它实际改变了什么、以及这些改动触及本页源码的程度如下：

* **空值契约。** 本页覆盖范围内 8 / 16 个主源码在签名上声明了 `@Nonnull` / `@Nullable`。没有单独点名本区域的提交，由全库性的 `34e27823 Refine nullability contracts across core APIs` 覆盖。
* **该区间没有其它改动触及本页覆盖的区域。**

> [!NOTE]
> 如何读这份提交清单：该模块一半源码带注解——`AnnotatedElementJSONElementVisitor`、`AnnotationUtils`、`ConstructorUtils`、`ElementUtils`、`FieldUtils`、`MemberUtils`、`MethodUtils`、`TypeUtils`——但没有任何提交点名 `microsphere-lang-model`，契约改造在未单独提交的情况下覆盖了它。

---

## 4. 元素查找与导航 —— `ElementUtils`

`ElementUtils` 是一个继承 `io.microsphere.util.Utils` 的 `interface`（`lang/model/util/ElementUtils.java:58`），因此它是 `static` 方法的命名空间，而非可注入的服务。

### 4.1 Kind 谓词（全部空安全，全部返回 `boolean`）

| 方法 | 对以下值返回 `true` | 证据 |
|---|---|---|
| `isClass(ElementKind)` | `CLASS`、`ENUM`、`RECORD`（委托给 `ElementKind#isClass()`） | `:87-89` |
| `isInterface(ElementKind)` | `INTERFACE`、`ANNOTATION_TYPE` | `:118-120` |
| `isDeclaredType(ElementKind)` | 上述两类之一 | `:149-151` |
| `isField(ElementKind)` | `FIELD`、`ENUM_CONSTANT` | `:180-182` |
| `isExecutable(ElementKind)` | `METHOD`、`CONSTRUCTOR`、`STATIC_INIT`、`INSTANCE_INIT`（自有 `switch`，**不是** `ElementKind#isExecutable()`） | `:216-227` |
| `isMember(ElementKind)` | `isField \|\| isExecutable` | `:256-258` |
| `isInitializer(ElementKind)` | `STATIC_INIT`、`INSTANCE_INIT` | `:287-296` |
| `isVariable(ElementKind)` | `ENUM_CONSTANT`、`FIELD`、`PARAMETER`、`LOCAL_VARIABLE`、`EXCEPTION_PARAMETER`、`RESOURCE_VARIABLE`，外加按**名称**匹配的 `BINDING_VARIABLE` | `:347-362` |

### 4.2 `ElementType` ↔ `ElementKind` 桥接与签名辅助方法

`toElementKind(ElementType)` 将 `TYPE`/`TYPE_USE` 映射为 `CLASS`，其余走 `ElementKind.valueOf(name)`，`null` 返回 `OTHER`（`:389-400`）。三个 `matchesElementType` 重载基于它构建 —— `(ElementKind, ElementType)` `:430-432`、`(ElementKind, ElementType...)` `:461-472`、`(Element, ElementType...)` `:500-502` —— 而 `matchesElementKind(Element, ElementKind)` `:526-528` 是直接形式。其余一组是修饰符/过滤/签名：`isPublicNonStatic` `:550-552`、`hasModifiers(Element, Modifier...)` `:578-589`、`filterElements(List, Predicate...)` `:622-633`（AND 组合，返回 `unmodifiableList`）、`matchParameterTypes(ExecutableElement, Type...)` `:656-659`、`matchParameterTypes(List<? extends VariableElement>, Type...)` `:682-684` 以及 `matchParameterTypeNames(List, CharSequence...)` `:714-733` —— 后者先比较参数个数，再逐个比较参数类型名。

### 4.3 示例

取自 `lang/model/util/ElementUtilsTest.java:100-136`；`echoMethod` 是 `TestServiceImpl#echo(String)`，在 `beforeTest` 中一次性解析（`ElementUtilsTest.java:94-98`）：

```java
// ElementUtilsTest.java:102-103
assertTrue(matchesElementKind(echoMethod, METHOD));
assertFalse(matchesElementKind(this.echoMethod, FIELD));

// ElementUtilsTest.java:114-117 — Integer#valueOf(String) is public *static* and must not match
methodsIn(getDeclaredMembers(testTypeElement)).forEach(method -> assertTrue(isPublicNonStatic(method)));
assertFalse(isPublicNonStatic(findMethod(getTypeElement(Integer.class), "valueOf", String.class)));

// ElementUtilsTest.java:127-129
List<VariableElement> fields = fieldsIn(getAllDeclaredMembers(testTypeElement.asType()));
assertTrue(hasModifiers(fields.get(0), PROTECTED));

// ElementUtilsTest.java:336-337 — parameter types compared by type name
assertTrue(matchParameterTypeNames(this.echoMethod.getParameters(), "java.lang.String"));
assertFalse(matchParameterTypeNames(this.echoMethod.getParameters(), "java.lang.Object"));
```

_来源：`lang/model/util/ElementUtilsTest.java:100-136,335-338`_

---

## 5. 类型工具 —— `TypeUtils`

`TypeUtils`（`lang/model/util/TypeUtils.java:69`）是模块中最大的类型：约上百个 `static` 方法，分为五族，多数成对出现，一个接收 `Element`，另一个接收 `TypeMirror`。

| 族 | 代表方法 | 证据 |
|---|---|---|
| 类型形态谓词 | `isSimpleType`、`isSameType`、`isArrayType`、`isEnumType`、`isClassType`、`isPrimitiveType`、`isInterfaceType`、`isAnnotationType`、`isTypeElement`、`isDeclaredType` | `:120-628`（成对位于 `:120/:149`、`:177/:203/:235/:261`、`:289/:314`、`:340/:367`、`:387/:408`、`:428/:448`、`:468/:489`、`:509/:530`、`:555/:579`、`:604/:628`） |
| 转换器 | `ofTypeElement(Element/TypeMirror/DeclaredType)`、`ofDeclaredType(Element/TypeMirror)`、`ofTypeMirrors`、`ofTypeElements`、`ofDeclaredTypes` | `:652-789` |
| 父类型遍历 | `getTypeElementOfSuperclass`、`getAllTypeElementsOfSuperclasses`、`getTypeElementsOfInterfaces`、`getAllTypeElements`、`findTypeElements`、`getDeclaredTypeOfSuperclass`、`getAllDeclaredTypes`、`findDeclaredTypes` | `:1013-2272`（`getAllTypeElements` `:1169`、`findTypeElements` `:1360-1365`） |
| `ProcessingEnvironment` 桥接 | `getTypeMirrors(env, Type...)`、`getTypeMirror(env, Type)`、`getTypeElement(env, Type/TypeMirror/CharSequence)`、`getDeclaredType(env, Type/TypeMirror/CharSequence)` | `:2287-2433` |
| 名称与 finder | `toString(TypeMirror)`、`getTypeName(TypeMirror)`、`typeElementFinder(...)` | `:2445-2505` |

基于名称的比较核心只有五行（`:261-266`），`SIMPLE_TYPE_NAMES`（`:77-82`）是本模块视作“简单类型”的不可变名称列表 —— `isSimpleType` 就是 `SIMPLE_TYPE_NAMES.contains(type.toString())`（`:149-151`）。两个 `TypeElement` 遍历函数在 `:88` 与 `:94-97` 声明为 `public static` 的 `Function` 常量。

```java
static boolean isSameType(TypeMirror type, CharSequence typeName) {
    if (type == null && typeName == null) {
        return true;
    }
    return Objects.equals(valueOf(type), valueOf(typeName));
}
```

### 5.1 示例

测试基类 `lang/model/util/UtilTest.java:55-92` 各处使用 `TypeUtils`，把依赖 `ProcessingEnvironment` 的查找包起来，使单个测试不必重复它们。

```java
// UtilTest.java:59-69
protected TypeMirror getTypeMirror(Type type) { return TypeUtils.getTypeMirror(processingEnv, type); }
protected TypeElement  getTypeElement(Type type) { return TypeUtils.getTypeElement(processingEnv, type); }

// UtilTest.java:87-88 — Element[] built from Types via TypeMirror then TypeElement
return getTypeMirrors(types).stream().map(TypeUtils::ofTypeElement).toArray(Element[]::new);

// JSONElementVisitorTest.java:237-240 — a TypeMirror back to a TypeElement, then its type parameters
TypeMirror superclass = getTypeElement(StringArrayList.class).getSuperclass();
List<? extends TypeParameterElement> typeParameters = ofTypeElement(superclass).getTypeParameters();
```

_来源：`lang/model/util/UtilTest.java:55-92`、`lang/model/util/JSONElementVisitorTest.java:236-245`_

---

## 6. 注解自省

### 6.1 `AnnotationUtils`

`AnnotationUtils`（`lang/model/util/AnnotationUtils.java:101`）有四个值得记住的常量：`VALUE_ATTRIBUTE_NAME = "value"`（`:106`）、`DEFAULT_ANNOTATION_VALUE_VISITOR` —— 一个共享的 `ResolvableAnnotationValueVisitor`（`:111`）、`EMPTY_ELEMENT_TYPE_ARRAY`（`:117`）以及 `WITH_DEFAULT = true`（`:119`）。

| 你想要什么 | 调用 | 证据（`lang/model/util/AnnotationUtils.java`） |
|---|---|---|
| 指定注解的第一个 mirror | `getAnnotation(AnnotatedConstruct, Class \| CharSequence)` | `:141-146`、`:169-175` |
| 仅直接存在的 mirror | `getAnnotations(construct[, class \| name])` | `:197-202`、`:226-231`、`:256-261` |
| **包含继承而来**的 mirror | `getAllAnnotations(Element \| TypeMirror \| ProcessingEnvironment, ...)` | `:282-287` … `:480-485` |
| 在层级中查找某个注解 | `findAnnotation(TypeMirror \| Element, Class \| CharSequence)` | `:509-514`、`:539-544`、`:572-577`、`:606-612` |
| 元注解（注解之上的注解） | `findMetaAnnotation`、`isAnnotationPresent`（存在**或**元存在） | `:640-645`、`:674-692`、`:719-724`、`:752-757` |
| 谓词过滤 | `findAnnotations`（直接）、`findAllAnnotations`（跨层级） | `:790-807`、`:822-827`、`:831-847`、`:863-875`、`:892-897`、`:914-919` |
| 全部属性值并合并默认值 | `getElementValues(...)` | `:1373-1375`、`:1425-1427`、`:1464-1466`、`:1509-1529` |
| 以 `Map<String, Object>` 形式返回 | `getAttributesMap(...)` | `:1190-1192`、`:1235-1237`、`:1272-1274`、`:1315-1329` |
| 单个属性 | `getElementValue` / `getAttribute` / `getValue` | `:1571-1602`、`:1646-1656`、`:1695-1697`、`:1738-1741`、`:1773-1782`、`:1811-1813` |
| 比较 | `matchesAnnotationType`、`matchesAnnotationTypeName`、`matchesAttributeMethod`、`matchesAttributeValue`、`matchesDefaultAttributeValue` | `:947-952`、`:980-985`、`:1052-1054`、`:1081-1091`、`:1117-1119`、`:1145-1150` |
| 注解的 `@Target` | `getElementTypes(AnnotationMirror \| DeclaredType)` | `:1841-1843`、`:1871-1875` |

默认值合并规则只在一个循环里（`:1518-1527`）：map 由 `getDeclaredMethods(annotationType)` 构建，`annotation.getElementValues()` 提供显式值，当 `withDefault` 为 `true` 且显式值为 `null` 时用 `attributeMethod.getDefaultValue()` 替补。既无显式值又无默认值的属性会**缺席**于结果 map。

### 6.2 `StringAnnotationValue` —— Wrapper

`StringAnnotationValue`（`lang/model/element/StringAnnotationValue.java:32-49`）是手写的、基于 `String` 的 `AnnotationValue` 实现，因此你自己合成的值可以喂给只接受 `AnnotationValue` 的代码。它是 `@Immutable`（`:32`），持有一个 `final String`（`:35,37-39`），`getValue()` 返回它（`:41-44`），而 `accept` 硬编码了双重分派（`:46-49`）：

```java
@Override
public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
    return v.visitString(this.value, p);
}
```

由于 `accept` 绕开了 JDK 自身的类型 switch，`StringAnnotationValue` 可被*任何* `AnnotationValueVisitor` 接受，包括 JDK 的和下一小节的两个访问器。

### 6.3 两个 `AnnotationValue` 访问器

| 访问器 | 基类 | 返回 | 用途 | 证据 |
|---|---|---|---|---|
| `ResolvableAnnotationValueVisitor` | `SimpleAnnotationValueVisitor6<Object, ExecutableElement>` | `Object` | 把属性值解析为运行期 Java 对象 | `lang/model/util/ResolvableAnnotationValueVisitor.java:74` |
| `JSONAnnotationValueVisitor` | `SimpleAnnotationValueVisitor6<StringBuilder, ExecutableElement>` | 共享的 `StringBuilder` | 为某个属性追加 `"name":value` JSON | `lang/model/util/JSONAnnotationValueVisitor.java:72` |

`ResolvableAnnotationValueVisitor` 有三个构造器（`:88-99`），由两个布尔开关控制：`classValuesAsString`（`:84`）与 `nestedAnnotationsAsMap`（`:86`）。`visitType` 返回 `TypeMirror` 字符串或加载后的 `Class`（`:146-149`）；`visitEnumConstant` 加载枚举类并调用 `Enum.valueOf`（`:151-155`）；`visitAnnotation` 构建 `LinkedHashMap<String, Object>`，并且除非设置了 `nestedAnnotationsAsMap` 或 JDK 钩子缺失，会反射进入 `sun.reflect.annotation.AnnotationParser.annotationForMap`（`:157-174`，钩子在 `:76-78` 解析）；`visitArray` 需要属性方法才能得知组件类型（`:176-195`）。

`JSONAnnotationValueVisitor` 是一个薄薄的写入器：每个标量 visit 方法都是一次 `append(jsonBuilder, getAttributeName(attributeMethod), value)` 调用（`:81-133`）。三处不同 —— `visitType` 写入**类型名**（`:135-139`）；`visitEnumConstant` 写入**简单名**（`:141-145`）；`visitAnnotation` 与 `visitArray` 用新的 `StringBuilder` 上的**子** `JSONAnnotationValueVisitor` 递归，再通过 `doAppend` 追加完成片段（`:147-184`、`:191-195`）。`visitUnknown` 原样返回 builder（`:186-189`）。

> [!NOTE]
> JSON 输出经由 `io.microsphere.json` 类型产生：该访问器 import 了 `JSONUtils.append` 与 `JSONUtils.appendName`（`lang/model/util/JSONAnnotationValueVisitor.java:36-37`），花括号/方括号/逗号符号取自 `io.microsphere.constants.SymbolConstants`（`:31-35`）。它们写入的是 **`io.microsphere.json` 的字符串构建层**，而不是 `JSONObject`/`JSONArray` 对象 —— 并且要注意，兄弟类 `JSONElementVisitor` 直接追加到裸 `StringBuilder`，完全没有 import 任何 `io.microsphere.json` 类型。`appendName` 会整体跳过空白名称（`microsphere-java-core/src/main/java/io/microsphere/json/JSONUtils.java:500-508`），这正是 `visitArray` 把属性方法传成 `null` 的原因（`JSONAnnotationValueVisitor.java:176`）：数组元素必须无名渲染。参见 [JSON](./microsphere-java-core/json.zh.md)。

### 6.4 两个元素访问器

`JSONElementVisitor`（`lang/model/util/JSONElementVisitor.java:57`）继承 `ElementKindVisitor6<Boolean, StringBuilder>`，兜底值设为 `FALSE`（`:59-61`）。它的 `visit*` 方法都是 `final`，实现“先判断支持再决定”的门闸：`visitPackage` `:63-69`、`visitVariable` `:71-77`、`visitExecutable` `:79-85`、`visitType` `:87-101`、`visitTypeParameter` `:103-109`。扩展点是 `protected` 钩子 `supports(Element)`（`:130-132`，默认 `true`）、按 kind 划分的 `supportsXxx` 家族（`:143-197`）以及 `doVisitPackage` / `doVisitTypeParameter`（`:206-219`）；`visitMembers(List, StringBuilder)`（`:111-119`）会为每个被包含的元素重新进入 `accept`。

`AnnotatedElementJSONElementVisitor`（`lang/model/util/AnnotatedElementJSONElementVisitor.java:88`）就是“只处理被 X 注解的元素”的过滤器：其构造器一次性解析注解并对非法输入快速失败（`:102-112`），`supports(Element)`（`:124-126`）借助 `getElementTypes` 取得的 `@Target` 集合收窄遍历（`:111`）。

### 6.5 示例

`JSONAnnotationValueVisitorTest.java:50-56,128-134` —— 取得测试类型上 `@TestAnnotation` 的属性 map，挑一个属性，visit 它，比较 JSON 文本：

```java
this.jsonBuilder = new StringBuilder();
this.visitor = new JSONAnnotationValueVisitor(jsonBuilder);
this.testAnnotationAttributes = getElementValues(testTypeElement, TestAnnotation.class);

void testVisit(String attributeName, String expectedJson) {
    Map.Entry<ExecutableElement, AnnotationValue> elementValue = getElementValue(this.testAnnotationAttributes, attributeName);
    StringBuilder jsonBuilder = visitor.visit(elementValue.getValue(), elementValue.getKey());
    assertEquals(expectedJson, jsonBuilder.toString());
}
```

断言的输出（`:58-121`）就是所产生结构的权威事实：

| 属性 | 产生的 JSON | Kind |
|---|---|---|
| `"z"` | `"z":true` | boolean `:59-61` |
| `"c"` | `"c":"b"` | char，带引号 `:69-71` |
| `"d"` / `"f"` | `"d":1.0` / `"f":1.0` | double / float `:73-81` |
| `"type"` | `"type":"io.microsphere.test.service.GenericTestService"` | `Class` 值 → 类型名 `:103-106` |
| `"timeUnit"` | `"timeUnit":"HOURS"` | 枚举常量 → 简单名 `:108-111` |
| `"since"` | `"since":{"module":"","value":"1.0.0"}` | 嵌套注解 → 对象 `:113-116` |
| `"properties"` | `"properties":[{"name":"key","type":"java.lang.String",…},{"name":"key2",…}]` | 注解数组 `:118-121` |
| unknown | *（无输出）* —— builder 原样返回 | `:123-126` |

_来源：`lang/model/util/JSONAnnotationValueVisitorTest.java:50-56,58-134`_

至于 `JSONElementVisitor`，`JSONElementVisitorTest.java:57-137` 展示了正确的子类化形态 —— 覆写 JDK 的 `visitTypeAsClass` / `visitVariableAsField` / `visitExecutableAsMethod`，而不是 `final` 的 `visitType` / `visitVariable` / `visitExecutable`：

```java
this.visitor = new JSONElementVisitor() {
    @Override protected boolean supports(Element e) { return supported; }
    @Override public Boolean visitTypeAsClass(TypeElement e, StringBuilder sb) {
        jsonBuilder.append("visitTypeAsClass"); return TRUE;
    }
};
assertTrue(visitor.visitType(getTypeElement(testClass), jsonBuilder));   // JSONElementVisitorTest.java:215-219
assertTrue(jsonBuilder.toString().startsWith("visitTypeAsClass"));
```

_来源：`lang/model/util/JSONElementVisitorTest.java:57-137,214-219`_

---

## 7. 成员工具

五个类型共用一套形态 —— `getDeclaredX`（仅本类型）/ `getAllDeclaredX`（整个层级）/ `findX` / `findDeclaredX(s)` / `filterDeclaredX(s)` / `isX` / `matchesX` —— 且每个都是接收 `TypeElement` 或 `TypeMirror` 的 `static` 方法 `interface`。

| 类型 | 包装对象 | 关键方法（证据，均位于 `lang/model/util/`） |
|---|---|---|
| `ClassUtils` | `TypeMirror` → `java.lang.Class` | `getClassName(TypeMirror)` `ClassUtils.java:57`、`loadClass(TypeMirror)` `:78`、`loadClass(String)` `:98` |
| `FieldUtils` | `VariableElement` | `getDeclaredField(Element/TypeMirror, String)` `FieldUtils.java:107,135`、`getDeclaredFields` `:161,187`、`getAllDeclaredFields` `:213,239`、`findField` `:267,294`、`findDeclaredFields` `:330,368`、`findAllDeclaredFields` `:407,445`、`filterDeclaredFields` `:488`、`isEnumMemberField` `:524`、`isNonStaticField` `:554`、`isField` `:580,618`、`getNonStaticFields` `:644,674`、`getAllNonStaticFields` `:704,734`、`equalsFieldName` `:760` |
| `MethodUtils` | `ExecutableElement`（方法） | `getDeclaredMethods` `MethodUtils.java:86,111`、`getAllDeclaredMethods` `:136,161`、`findDeclaredMethods` `:196,231`、`findAllDeclaredMethods` `:264,300,415,451`、`findPublicNonStaticMethods` `:338,373`、`filterDeclaredMethods` `:487`、`isMethod` `:522`、`isPublicNonStaticMethod` `:546`、`findMethod`（6 个重载）`:575,604,635,664,699,728`、`getOverrideMethod(env, …)` `:764`、`filterMethods` `:804`、`getMethodName` `:841`、`getReturnTypeName` `:866`、`getMethodParameterTypeMirrors` `:896`、`getMethodParameterTypeNames` `:938`、`matches` `:967,994`、`matchesMethodName` `:1021`、`matchesMethod` `:1048,1089`、`getEnclosingElement` `:1127`、`methodPredicateForExcludedTypes` `:1152` |
| `ConstructorUtils` | `ExecutableElement`（构造器） | `getDeclaredConstructors(TypeElement/TypeMirror)` `ConstructorUtils.java:87,123`、`findConstructor` `:151,179`、`findDeclaredConstructors` `:220,239`、`filterDeclaredConstructors` `:278` |
| `MemberUtils` | 任意 `Element` 成员 | `getDeclaredMembers(TypeMirror/TypeElement)` `MemberUtils.java:98,122`、`getAllDeclaredMembers` `:146,170`、`getDeclaredMembers(..., boolean includeHierarchicalTypes)` `:196,222`、`findDeclaredMembers` `:250,278,388,419`、`findAllDeclaredMembers` `:310,349` |

`getMethodName`（`MethodUtils.java:841-843`）与 `getReturnTypeName`（`:866-868`）是这一族中容忍 null 的访问器 —— `method == null ? null : method.getSimpleName().toString()`。`ClassUtils.loadClass` 是最容易让人意外的那座桥：它经由 `io.microsphere.util.ClassLoaderUtils` 解析，因此**类必须位于处理器自身的 classpath 上**，而不仅仅是在被编译的源码路径上。`ResolvableAnnotationValueVisitor.visitType` 与 `visitEnumConstant` 都依赖它（`lang/model/util/ResolvableAnnotationValueVisitor.java:148,153`），这就是解析同一编译轮次中生成的类的 `Class` 型属性会失败的原因。

> [!TIP]
> `TypeElement` 与 `TypeMirror` 重载并非冗余。当你来自 `Element.asType()` 或注解的 `DeclaredType` 时用 `TypeMirror` 形式；当你已持有 `TypeElement` 时用后者。两者最终汇入同一遍历，且都在 `null` 时返回 `emptyList()`。

```java
// ElementUtilsTest.java:97 — resolve a method by name + parameter type *names*
this.echoMethod = findMethod(testTypeElement, "echo", "java.lang.String");

// ExecutableElementComparatorTest.java:41-43 — overloads resolved by parameter Type
TypeElement typeElement = getTypeElement(Integer.class);
ExecutableElement byInt    = findMethod(typeElement, "valueOf", int.class);
ExecutableElement byString = findMethod(typeElement, "valueOf", String.class);

// ExecutableElementComparatorTest.java:49-53 — 1-parameter vs 3-parameter append
TypeElement sb = getTypeElement(StringBuilder.class);
findMethod(sb, "append", char[].class);
findMethod(sb, "append", char[].class, int.class, int.class);
```

_来源：`lang/model/util/ElementUtilsTest.java:94-98`、`lang/model/util/ExecutableElementComparatorTest.java:38-54`_

---

## 8. 诊断 —— `MessagerUtils` 与 `LoggerUtils`

### 8.1 `LoggerUtils`

`lang/model/util/LoggerUtils.java:30-53` 声明了一个名为 `"microsphere-lang-model"` 的共享 `Logger`（`:32`，取自 `io.microsphere.logging.LoggerFactory.getLogger`）与五个 `static` 转发方法，签名均为 `(String format, Object... args)`：`trace` `:34`、`debug` `:38`、`info` `:42`、`warn` `:46`、`error` `:50`。

### 8.2 `MessagerUtils`

`lang/model/util/MessagerUtils.java:43` 暴露五对方法 —— 每对包含一个 `ProcessingEnvironment` 重载和一个 `Messager` 重载：

| 级别 | `Diagnostic.Kind` | `MessagerUtils` | `LoggerUtils` 级别 | 证据 |
|---|---|---|---|---|
| note | `NOTE` | `printNote(env\|messager, …)` | `info` | `:63-65`、`:103-105` |
| warning | `WARNING` | `printWarning(…)` | `warn` | `:127-129`、`:150-152` |
| mandatory warning | `MANDATORY_WARNING` | `printMandatoryWarning(…)` | `warn` | `:174-176`、`:197-199` |
| error | `ERROR` | `printError(…)` | `error` | `:221-223`、`:244-246` |
| 任意 | `Kind`（参数） | `printMessage(env\|messager, kind, …)` | 见下文 | `:268-270`、`:292-309` |

整套映射就在一个方法里（`:292-309`）：模式串先经 `io.microsphere.text.FormatUtils.format(pattern, args)` 格式化**一次**，发给 `Messager`，然后**带着原始模式串与参数**再发给 logger，以便日志后端自行完成参数化。`ERROR` → `error`，`WARNING` 与 `MANDATORY_WARNING` → `warn`，`NOTE` → `info`，而**其余任何 kind**（`OTHER`，以及未来 JDK 新增的 `Diagnostic.Kind`）→ `debug`（`:306-307`）。

> [!WARNING]
> 占位符语法是 `{}`，不是 `{0}`，也不是 `String.format`。javadoc 写着“支持 `String#format` 语法”，示例用的是 `{0}`（`lang/model/util/MessagerUtils.java:56-61`、`:78-82`），但实现调用的是 `FormatUtils.format`，其默认占位符字面上就是 `"{}"`（`microsphere-java-core/src/main/java/io/microsphere/text/FormatUtils.java:17`、`:38-39`）。请写 `"Hello, {}!"` —— 测试就是这么写的（`lang/model/util/MessagerUtilsTest.java:55-56`）—— 否则你的参数永远不会被替换。

整条链路上没有任何 null 防护：`printX(processingEnv, …)` 会解引用 `processingEnv.getMessager()`（`:64`、`:128`、`:175`、`:222`、`:269`），而 `printMessage(messager, …)` 会解引用 `messager.printMessage(...)`（`:294`）并在 `switch` 中解引用 `kind`（`:295`）。见 §10 陷阱 8。

```java
// MessagerUtilsTest.java:50-80
this.messager = this.processingEnv.getMessager();
assertDoesNotThrow(() -> printNote(this.processingEnv, "Hello, {}!", "printNote"));
assertDoesNotThrow(() -> printNote(this.messager, "Hello, {}!", "printNote"));
assertDoesNotThrow(() -> printWarning(this.processingEnv, "Hello, {}!", "printWarning"));
assertDoesNotThrow(() -> printMandatoryWarning(this.messager, "Hello, {}!", "printMandatoryWarning"));
assertDoesNotThrow(() -> printError(this.processingEnv, "Hello, {}!", "printError"));
// the generic form, with a Kind outside the four mapped ones
assertDoesNotThrow(() -> printMessage(this.messager, OTHER, "Hello, {}!", "printMessage"));
```

_来源：`lang/model/util/MessagerUtilsTest.java:47-81`_

---

## 9. 排序 —— `ExecutableElementComparator`

`lang/model/util/ExecutableElementComparator.java:55` 是一个 `Comparator<ExecutableElement>`，构造器为私有（`:62`），只暴露一个公有单例 `INSTANCE`（`:60`）—— 因此调用方始终共享同一个无状态实例。文档所述规则（`:27-33`）按四步实现：**同一性/`equals` 捷径** `if (e1.equals(e2)) return 0;`（`:68-70`）；接着**简单名按字典序**，由 `CharSequenceComparator.INSTANCE.compare(e1.getSimpleName(), e2.getSimpleName())` 完成（`:73`）；然后**参数个数** `ps1.size() - ps2.size()`（`:77-80`）；最后**逐参数类型名字典序，首个差异即返回**，比较的是 `asType().toString()`（`:82-89`）。

结果是一个稳定的、以名称优先的排序：把重载聚在一起，并在每个重载组内按元数、再按参数类型名排序。当你从 `getDeclaredMethods(...)` 生成有序输出时应当使用它，以使生成的元数据在多次 `javac` 之间可复现。它**并未**使用 `io.microsphere.lang.Prioritized`：对全部 16 个主类型 grep 均无该接口的引用。开发者指南仍记录了 `Prioritized` 常量反转的警告，因为它影响配套的 `annotation-processor` 模块。

```java
private final ExecutableElementComparator comparator = INSTANCE;   // :23

// :40-43 — same name, same arity, different parameter types → compare type names
TypeElement typeElement = getTypeElement(Integer.class);
assertEquals(int.class.getName().compareTo(String.class.getName()),
        comparator.compare(findMethod(typeElement, "valueOf", int.class),
                           findMethod(typeElement, "valueOf", String.class)));

// :48-53 — same name, different arity → the arity difference is returned verbatim
TypeElement sb = getTypeElement(StringBuilder.class);
assertEquals(-2, comparator.compare(findMethod(sb, "append", char[].class),
        findMethod(sb, "append", char[].class, int.class, int.class)));

// :59-60 — the same method reached from two different TypeElements compares 0
assertEquals(0, comparator.compare(getMethod("close"),
        findMethod(getTypeElement(AutoCloseable.class), "close")));
```

_来源：`lang/model/util/ExecutableElementComparatorTest.java:21-65`_

---

## 10. 常见陷阱

1. **你得到的每个 `TypeMirror` 都要求一个活的 `ProcessingEnvironment`。** `TypeUtils.getTypeElement(env, …)`（`lang/model/util/TypeUtils.java:2350-2381`）、`getDeclaredType(env, …)`（`:2401-2433`）与 `getTypeMirror(env, …)`（`:2310`）都委托给 `Elements.getTypeElement` / `Types.getDeclaredType`，传入 `null` 时它们静默地返回 `null` 或 `emptyList()`。`AnnotatedElementJSONElementVisitor` 是唯一拒绝 `null` 的类型，靠构造器中三处 `assertNotNull`（`lang/model/util/AnnotatedElementJSONElementVisitor.java:103`、`:104`、`:110`）。本模块的测试之所以能跑，是因为 `microsphere-annotation-test` 提供了真实的处理环境 —— `UtilTest` 继承 `AbstractAnnotationProcessingTest`（`lang/model/util/UtilTest.java:20,48`）。**把测试搬到你自己的工程而不加该测试依赖，什么都解析不出来。**
2. **为*当前轮次生成的*类查找 `TypeElement` 可能得到 `null`。** `Elements.getTypeElement` 看不见尚未 enter 的源码，而 `AnnotatedElementJSONElementVisitor.java:110` 恰好把这种情况变成硬失败，消息为 `"The annotation can't be found by name : …"`。请传入**已存在**的注解类。
3. **`isSameType` 是字符串比较，不是 `Types#isSameType` 调用。** `lang/model/util/TypeUtils.java:261-266` 比较 `String.valueOf(typeMirror)` 与期望名称，因此 (a) 它无需 `Types` 实例即可调用，(b) 对 `toString()` 不是规范二进制名的类型它**不是**等价的 —— 类型变量用法、capture 类型与内含类型会在 `javax.lang.model.util.Types.isSameType` 返回 `true` 的地方比较为 `false`。对声明类型与原始类型它是可靠的。另需注意 `isSameType(null, null)` 返回 `true`（`:262-264`），这条“null 相等”规则与所有兄弟谓词不同，后者对 `null` 一律返回 `false`。
4. **`isSimpleType` 拿 `toString()` 与名称列表比较。** `lang/model/util/TypeUtils.java:149-151` —— 只要声明类型的名字恰好在 `SIMPLE_TYPE_NAMES`（`:77-82`）中就是“简单类型”，与该类的实际来源无关。它既不依赖 classpath，又可能被 classpath 意外命中。
5. **`getElementValues` 含默认值；`getElementValue` 优先显式值。** `lang/model/util/AnnotationUtils.java:1509-1529` 会为缺席项合并 `getDefaultValue()`，而对既无显式值又无默认值的属性直接跳过。`getElementValue(annotation, name, true)`（`:1571-1602`）只在 `withDefault && annotationValue == null` 分支重新扫描声明的方法（`:1587-1595`），且解析结果仍为 `null` 时返回 `null`（`:1597-1599`）—— 于是“属性不存在”与“属性 = null”无法区分。`getAttribute` 还多一重风险：它通过 `annotationValue.accept(DEFAULT_ANNOTATION_VALUE_VISITOR, attributeMethod)`（`:1781`）分派，用的是共享静态实例，因此 §6.3 的两个特性开关取决于该实例的构造方式，而不是你的选择。
6. **访问器的默认方法继承自已废弃且实现不全的 JDK 类。** `JSONElementVisitor` 继承 `ElementKindVisitor6`（`lang/model/util/JSONElementVisitor.java:28,57`），`JSONAnnotationValueVisitor` / `ResolvableAnnotationValueVisitor` 继承 `SimpleAnnotationValueVisitor6`（`lang/model/util/JSONAnnotationValueVisitor.java:25,72`、`lang/model/util/ResolvableAnnotationValueVisitor.java:27,74`）。`ElementKindVisitor6` 自 JDK 9 起废弃，而 `SimpleAnnotationValueVisitor6.visit()` 对 JDK 6 之后新增的 kind 会落到 `visitUnknown`。具体地：`JSONElementVisitor.visitType` 调用 `super.visitType`（`:93`），后者分派到 `visitTypeAsRecord` —— 而这里既没有覆写 `visitTypeAsRecord` 也没有 `doVisitType` 钩子，因此 `record` **没有输出且返回 `false`**，除非你自己覆写。
7. **`JSONElementVisitor` 不防护 `null` 元素。** `supports(Element)` 无条件返回 `true`（`:130-132`），因此使用默认 `supports` 时，`visitVariable(null, sb)` / `visitExecutable(null, sb)` / `visitType(null, sb)` 会穿过 `super.visitXxx` 落入 `ElementKindVisitor6`，而后者会解引用元素。`lang/model/util/JSONElementVisitorTest.java:146-251` 中所有 `*OnUnsupported` 用例之所以通过，仅仅是因为测试覆写 `supports` 返回 `false`；它们在 `:177`、`:197`、`:250` 传入了 `null`。**务必覆写 `supports`。**
8. **`Messager` 为 `null` 时 `MessagerUtils` 抛 `NullPointerException`。** `printMessage` 无防护地调用 `messager.printMessage(kind, message)`（`lang/model/util/MessagerUtils.java:294`），`kind` 也在 `:295` 的 `switch` 中被解引用。`ProcessingEnvironment` 重载更糟：`:64`、`:128`、`:175`、`:222`、`:269` 处的 `processingEnv.getMessager()` 在环境为 `null` 时直接 NPE。这与模块整体的静默降级策略相矛盾（见 §10.13）。请自行防护：`if (processingEnv != null) printNote(processingEnv, …)`。
9. **`ExecutableElementComparator.compare` 同样不容忍 null。** `lang/model/util/ExecutableElementComparator.java:68` 的 `e1.equals(e2)` 在 `e1` 为 `null` 时 NPE；`e1` 非空而 `e2 == null` 能熬过第一步，随后在 `:73` 的 `e2.getSimpleName()` 上 NPE。排序前请先剔除 `null`。
10. **本模块不缓存任何 `Element` 查找。** 仅有的静态状态是 `ResolvableAnnotationValueVisitor` 中在类初始化时解析一次的 `static final` 字段 —— `ANNOTATION_PARSER_CLASS` 与 `annotationForMapMethod`（`lang/model/util/ResolvableAnnotationValueVisitor.java:76-78`）—— 加上不可变的 `SIMPLE_TYPE_NAMES` 列表（`lang/model/util/TypeUtils.java:77-82`）与两个 `TypeElement` 遍历 `Function` 常量（`:88`、`:94-97`）。`TypeUtils.getTypeElement(env, name)`、`MemberUtils.getDeclaredMembers` 以及 `TypeUtils.getAllTypeElements`（`:1169`）的层级遍历每次调用都重新计算。处理成千上万个类型时，请把 `TypeElement` 查找提升到你自己的 `Map` 里。
11. **`getTypeName` 通过反射伸手进 `TypeMirror`。** `lang/model/util/TypeUtils.java:2456-2478` 调用 `invokeMethod(true, type, "getTypeArguments")`（`:2466`），因为 `TypeMirror` 并未声明该方法，然后把裸的 `List<TypeMirror>` 包在 `<` 与 `>` 之间追加（`:2470-2472`）。于是有两个副作用：尖括号形式只有在*元素*声明了类型参数**且**该类型携带实参时才出现；渲染结果是 type mirror 的 `List.toString()`，而非规范名称。需要纯字符串时请用 `toString(TypeMirror)`（`:2445-2448`）。
12. **`ResolvableAnnotationValueVisitor` 可能交给你一个无法加载的 `Class`。** `visitType` 与 `visitEnumConstant` 调用 `ClassUtils.loadClass`（`:148`、`:153` → `lang/model/util/ClassUtils.java:78`），而 `visitArray` 需要属性方法推导组件类型（`lang/model/util/ResolvableAnnotationValueVisitor.java:177-195`）：`(ArrayType) attributeMethod.getReturnType()`（`:193`）是未检查转换，因此属性方法为 `null` 时，还没轮到转换就已经 NPE。`JSONAnnotationValueVisitor` 处理数组元素时完全不需要属性方法，从而绕开了这一点。

### 10.13 静默降级策略失效之处

本库的策略是**静默降级** —— 输入缺失时返回 `null`、`false`、空列表或空 map，而不是抛异常。本模块中经核实的例外恰好是：

| 位置 | 行为 | 证据（`lang/model/util/`） |
|---|---|---|
| `AnnotatedElementJSONElementVisitor` 构造器 | `processingEnv`、`annotationClassName` 为 `null` 或注解无法解析时抛出 | `AnnotatedElementJSONElementVisitor.java:103,104,110` |
| `MessagerUtils` | `Messager` / `ProcessingEnvironment` / `Kind` 为 `null` 时 NPE | `MessagerUtils.java:64,128,175,222,269,294,295` |
| `ExecutableElementComparator.compare` | 元素为 `null` 时 NPE | `ExecutableElementComparator.java:68,73` |
| `TypeUtils.findTypeElements` | 声明 `throws IllegalArgumentException`（过滤元素为 `null` 时） | `TypeUtils.java:1356,1360-1365`、`:2034,2064` |
| `TypeUtils.typeElementFinder` | javadoc 声明 `IllegalArgumentException`：“if any parameter is invalid or if assertions fail” | `TypeUtils.java:2498-2505` |
| `ResolvableAnnotationValueVisitor.getComponentType` | 对可能为 `null` 的属性方法做未检查 `(ArrayType)` 转换 | `ResolvableAnnotationValueVisitor.java:189-195` |

其余全部 —— `ElementUtils`、`AnnotationUtils`、`MemberUtils`、`FieldUtils`、`MethodUtils`、`ConstructorUtils`、`ClassUtils`、`JSONAnnotationValueVisitor`、`StringAnnotationValue`、`LoggerUtils` —— 都会降级。完整的抛出点清单见[开发者指南](../developer-guide/module-microsphere-lang-model.zh.md)。

---

## 11. 参见

* [注解处理器模块](./module-microsphere-annotation-processor.zh.md) —— 在 `javac` 内部驱动本模块的框架；`AnnotatedElementJSONElementVisitor` 是它天然的扩展点。
* [JDK Tools 模块](./module-microsphere-jdk-tools.zh.md) —— 此处为测试作用域依赖（`pom.xml:44-49`），提供 JVM 内的 `ToolProvider` 驱动，使本模块测试能获得真实的 `ProcessingEnvironment`。
* [Annotation Test 模块](./module-microsphere-annotation-test.zh.md) —— 测试作用域的骨架 `io.microsphere.test.annotation.processing.AbstractAnnotationProcessingTest`，即 `UtilTest` 的父类（`lang/model/util/UtilTest.java:20,48`），也是 `io.microsphere.test.*` 夹具的来源。
* [microsphere-java-core README](./microsphere-java-core/README.zh.md) —— 模块索引与发布线版本表。
* [JSON](./microsphere-java-core/json.zh.md) —— `JSONUtils.append` / `appendName`，即 `JSONAnnotationValueVisitor` 背后的输出层（`lang/model/util/JSONAnnotationValueVisitor.java:36-37`）。
* [开发者指南 —— 语言模型](../developer-guide/module-microsphere-lang-model.zh.md) —— 16 个类型的清单、访问器分派图、类型解析与 `AnnotationValue` 树的源码分析，以及线程安全评审。
* 英文版本：[English User Guide](./module-microsphere-lang-model.md)
