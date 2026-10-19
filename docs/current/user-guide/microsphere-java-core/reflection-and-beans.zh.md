# 反射、类型与 beans

| | |
|---|---|
| **构件** | `io.github.microsphere-projects:microsphere-java-core`（`0.3.20-SNAPSHOT`，`release` 分支） |
| **包** | `io.microsphere.reflect` — 19 个主源文件类型 · `io.microsphere.reflect.generics` — 2 个 · `io.microsphere.internal.reflect` — 1 个 · `io.microsphere.beans` — 4 个（共 26 个） |
| **JDK 级别** | 以 Java 8 编译；CI 在 JDK 8 / 11 / 17 / 21 / 25 上测试 |
| **依赖的其他 `core` 包** | `util`（`ArrayUtils`、`StringUtils`、`Assert`、`ClassUtils`、`Utils` 标记接口）、`lang`（`Version`、`Deprecation`、`MutableInteger`）、`collection`（`ListUtils`、`Maps`、`SetUtils`）、`logging`、`io.event`（`ConfigurationProperty` 注解） |
| **外部依赖** | 无——纯 JDK（`javax.annotation-api`、`slf4j-api`、`commons-logging` 都是可选的，且与此处无关） |
| **被谁使用** | `convert`、`classloading`、`io.scanner`、`metadata`、`json`、`net`、`management`——也就是说，几乎一切都依赖这个包组 |

本页所有 `file:line` 引用都相对于 `microsphere-java-core/src/main/java/io/microsphere/`（主源码）或
`microsphere-java-core/src/test/java/io/microsphere/`（测试），来自 `release` 工作树，首次完整出现后缩写为
`reflect/MethodUtils.java:40-55`。全部 26 个类型都带有 javadoc `@since 1.0.0`；这四个包中没有任何 `@Since` **注解**的使用。

---

## 1. 这个包组是做什么用的

**当你的代码必须扛得住 JDK 升级时，就应该使用 `io.microsphere.reflect`。** 原生 `java.lang.reflect` 有两个尖锐的坑：
`setAccessible(true)` 在 JDK 16+（JEP 396）会抛 `InaccessibleObjectException`；而 `java.lang.reflect.Type`
是一个你无法问出合理问题（父泛型类型？是通配符吗？）的接口体系。
`AccessibleObjectUtils.trySetAccessible(...)`（`reflect/AccessibleObjectUtils.java:97-104`）会按 JDK 选择正确的机制且
永不抛异常；`JavaType`（`reflect/JavaType.java:92`）把任意 `Type` 包装起来，并对父类型 / 接口 / 泛型类型做记忆化缓存。

**当你需要“把这个对象变成 `Map<String, Object>`”时应使用 `io.microsphere.beans`。**
`BeanUtils.resolvePropertiesAsMap(bean)`（`beans/BeanUtils.java:204`）递归遍历 JavaBeans getter，产出一个带深度护栏的
不可修改 Map，因此自引用的 bean 不可能把栈打爆（`:452-472`、`:457`）。

**当你想要的成员可能不存在于目标 JVM 上时，应使用定义族（`ReflectiveDefinition` → `MemberDefinition` →
`ExecutableDefinition` → `MethodDefinition` / `ConstructorDefinition`，外加 `FieldDefinition` 与 `ClassDefinition`）。**
定义是一个 `Serializable` 的描述（“自版本 X 起、位于类 Y、名为 Z”），按需惰性解析，于是 `isPresent()`
以声明式的方式回答问题（`reflect/ReflectiveDefinition.java:91-106`、`reflect/MemberDefinition.java:166-177`）。

> [!IMPORTANT]
> 这是 `core` 中**唯一**被允许触碰 JDK 反射内部的层，并且每条内部访问路径都有文档化的降级方案（见 §5）——请把 `setAccessible` 需求路由到这里，而不是散落到其他包。

---

## 2. 坐标与版本支持

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

| 事实 | 值 | 证据（`release`） |
|---|---|---|
| 坐标 | `io.github.microsphere-projects:microsphere-java-core` | `microsphere-java-core/pom.xml:13-15` |
| 版本 | `${revision}`，展平后；`release` 上是 `0.3.20-SNAPSHOT`；最新已发布标签 `0.3.20` | `pom.xml:14`、`:54`；`docs/current/user-guide/microsphere-java-core/README.md:70` |
| Java 级别 | 8（源码必须能在 JDK 8 上构建）；CI 运行 `-Dsurefire.useSystemClassLoader=false` | CI 矩阵 `java: [ '8', '11', '17', '21', '25' ]`、`.github/workflows/maven-build.yml:25`、`distribution: 'temurin'` `:33`、参数 `:43` |
| Spring Framework（仅测试用，`JavaTypeTest` 依赖） | `[1.8,17)` 上用 `5.3.39`，其余用更新的分支 | `microsphere-java-parent/pom.xml:54-62`；test 作用域依赖 `microsphere-java-core/pom.xml:88-92` |
| 全部 26 个类型的版本标记 | javadoc `@since 1.0.0` | 例如 `reflect/AccessibleObjectUtils.java:42`、`reflect/JavaType.java:90`、`beans/BeanUtils.java:85`、`internal/reflect/ConstantPoolUtils.java:42` |

### 2.1 可访问性矩阵（本页核心）

`AccessibleObjectUtils` 的判定是*基于能力，而非版本号*：在类初始化阶段它通过 `MethodHandle` 查找两个 JDK 9 方法，
不存在时存入 `null`（`canAccess` 见 `:62`，`trySetAccessible` 见 `:68`）；下表的一切行为都由这些 handle 是否非 `null` 推导而来。

| 操作 | JDK 8 | JDK 9 – 15 | JDK 16+（JEP 396） | 说明 |
|---|---|---|---|---|
| `trySetAccessible(AccessibleObject)` | 走 `setAccessible(true)` 兜底路径（`:99-100`、`:157-168`） | 通过 `MethodHandle` 调 `AccessibleObject.trySetAccessible()`（`:102`、`:170-178`） | 同一条 handle 路径；对 JDK 内部成员返回 `false` 而非抛异常 | 返回值表示**成功与否**，在 9+ 路径上绝不是“先前的状态” |
| `setAccessible(AccessibleObject)`（包私有） | 始终被 JDK 8 分支使用 | 仅在拿不到 handle 时使用 | 仅在拿不到 handle 时使用 | 返回**先前**的可访问标志（`:158`、`:167`）；`setAccessible(true)` 抛出的 `RuntimeException` 会在记录日志后被吞掉（`:163-165`） |
| `canAccess(Object, AccessibleObject)` | `:144-146` 的 public 短路；否则因 `tryCanAccess` 返回 `null` 而退回到 `isAccessible()`（`:147-148`、`:184-194`） | 真正的 `AccessibleObject.canAccess(Object)`（`:147`） | 真正的 `canAccess`，因此模块规则被精确遵守 | 在 16+ 上，未开放 JDK 包的成员即便 `trySetAccessible` 之后 `canAccess` 仍为 `false` |
| `InaccessibleObjectException` 的处理 | 该类未被加载；识别靠**类名字符串**，因此依然有效（`reflect/ReflectionUtils.java:165`、`:537-538`） | 仅由 legacy 路径抛出 | 由 legacy 路径抛出，在 `:163` 被捕获并转换为一条记录 `--add-opens` 提示的日志（`:196-211`） | 从 `setAccessible` 出发，库从不把它重新抛给你的调用方 |
| 必须强制访问时所需的 JVM 参数 | 无 | 通常无 | `--add-opens=<module>/<package>=ALL-UNNAMED`，在 `:204-208` 打印到错误日志 | 该提示语是从异常文本里截取 `"module "` 与 `"opens "` 拼出来的（`:199-200`） |

> [!WARNING]
> 两个 `trySetAccessible` 重载的**返回语义不同**。public 的那个
> （`reflect/AccessibleObjectUtils.java:97-104`）回答*该对象现在是否可访问*；包私有的
> `setAccessible`（`:157-168`）回答*调用之前它是否可访问*。测试正是依赖这一区分——见 §4.1。


### 2.2 `0.3.20` 的发布日期与变更摘要

| 条目 | 值 | 依据 |
|---|---|---|
| 文档所在线 | `release` 分支，`${revision}` = `0.3.20-SNAPSHOT` | 根 `pom.xml:54` |
| 最新已发布版本 | `0.3.20`，标签日期 **2026-10-16** | 标签 `0.3.20` 的提交信息为 `chore: merge main into release [skip ci]`，提交时间 `2026-10-16T03:43:20Z` |
| 上一版本 | `0.3.19`，标签日期 **2026-08-30** | 标签 `0.3.19`，提交时间 `2026-08-30T12:51:37Z` |
| 变更日志来源 | 仓库根目录的 `release-notes.md` | 本项目不发布 `CHANGELOG.md`；`release-notes.md` 的 `v0.3.16`–`v0.3.20` 段落写着 *Release notes generation failed* 并保留原始提交列表，因此下文一律以提交主题引用，并逐条与 `release` 源码核对。 |

`0.3.20` 在这一区域没有新增 API 面——`0.3.19...0.3.20` 区间是一次加固性发布。它实际改变了什么、以及这些改动触及本页源码的程度如下：

* **空值契约。** 本页覆盖范围内 20 / 26 个主源码在签名上声明了 `@Nonnull` / `@Nullable`。区间内点名本区域的主题：`d62b4c8a Add nullability annotations in bean APIs` / `34e27823 Refine nullability contracts across core APIs`。
* **空输入与行为修复。** `8fce81e2 Remove redundant throws from BeanUtils` / `28e46fca Remove unused valueType from BeanUtils converters` / `e6c91d8a Guard null getter in BeanUtils resolveProperty` / `90168797 Resolve type in array assertion` / `3220952e Handle missing fields in type assertions`。

> [!NOTE]
> 如何读这份提交清单：`90168797` 与 `3220952e` 是测试侧提交；它们断言本页所述的泛型解析行为，而非修改该行为。

---

## 3. API 参考

### 3.1 `AccessibleObjectUtils` — `reflect/AccessibleObjectUtils.java`

```java
public abstract class AccessibleObjectUtils implements Utils {                    // :44
    public static boolean  trySetAccessible(AccessibleObject ao)                   // :97
    public static boolean  canAccess(Object object, AccessibleObject ao)            // :142
    static boolean setAccessible(AccessibleObject ao)                               // :157
    static boolean trySetAccessible(MethodHandle mh, AccessibleObject ao)           // :170
    static Boolean tryCanAccess(Object o, AccessibleObject ao)                      // :180
    static Boolean tryCanAccess(MethodHandle mh, Object o, AccessibleObject ao)     // :184
    static void handleInaccessibleObjectExceptionIfFound(Throwable e)               // :196
    private AccessibleObjectUtils() {}                                              // :213
}
```

空值与失败行为：在 JDK 9+ 上，`null` 或不可访问的 `AccessibleObject` 会让 handle 调用失败并使
`trySetAccessible` 返回 `false`，因为整个方法体被 `catch (Throwable)` 包裹（`:170-178`）；两个
`MethodHandle` 都是 `static final`，只在 `:62` 与 `:68` 解析一次。包私有的 `setAccessible`（`:157-168`）只捕获
`:161` 处 `setAccessible(true)` 抛出的 `RuntimeException`——`:158` 处 `isAccessible()` 抛出的
`NullPointerException` 不在捕获范围。`canAccess`（`:142-149`）通过 `MemberUtils.isPublic` 把 `null` 成员视为非
public，随后当 `tryCanAccess` 返回 `null`（JDK 8，`:184-194`）时回退到 `:148` 的 `accessibleObject.isAccessible()`，
后者在 JDK 8 上对 `null` 同样会 NPE。

### 3.2 `MethodUtils` — `reflect/MethodUtils.java`（1874 行）

每一次查找都由三个静态缓存支撑：

| 缓存 | 声明 | 键 | 清除方式 |
|---|---|---|---|
| 已解析方法 | `private final static ConcurrentMap<MethodKey, Method> methodsCache = newConcurrentHashMap(256)`（`:171`） | `MethodKey`（`:1827-1869`） | `clearMethodsCache()`（`:1715`） |
| 被禁用方法 | `bannedMethodsCache`（`:184`） | 由签名字符串构造的 `MethodKey` | `clearBannedMethodsCache()`（`:1738`） |
| 每个类的声明方法 | `declaredMethodsCache`（`:186`） | `Class<?>` | `clearDeclaredMethodsCache()`（`:1745`） |

```java
@Nullable public static Method findMethod(@Nullable Class targetClass, @Nullable String methodName,
        @Nullable Class<?>... parameterTypes)                       // :629-632 (banned check, then methodsCache.computeIfAbsent)
@Nullable public static Method findDeclaredMethod(...)              // :658-686
public static List<Method> findMethods(Class, boolean includeSelf, boolean includeSuperClass, Predicate...)  // :539-568
@Nullable public static <R> R invokeMethod(boolean forceAccess, @Nullable Object instance, @Nonnull Method method,
        @Nonnull Object... arguments)                               // :1097-1123
public static boolean overrides(Method overrider, Method overridden) // :1165-1223
@Nullable public static Method findFunctionalInterfaceMethod(Class<?>) // :1342-1368
@Nonnull public static String buildSignature(Class, String, Class...)  // :1423-1456
public static void banMethod(Class, String, Class...)                  // :263-269
```

`invokeMethod` 行为（`:1097-1123`）：

| 场景 | 结果 | 行号 |
|---|---|---|
| `method == null` | `assertNotNull` → 抛出带 `"The 'method' must not be null"` 的 `NullPointerException` | `:1098` |
| `forceAccess == true` | 在调用**之前**尝试 `trySetAccessible(method)`；其失败本身并不致命 | `:1103-1104` |
| `IllegalAccessException` / `IllegalArgumentException` | 重新包装为 `IllegalArgumentException`，在 `finally` 之后抛出 | `:1107-1108`、`:1118-1120` |
| `InvocationTargetException` | **目标**异常被解包并以 `RuntimeException` 抛出 | `:1109-1110` |
| void 方法 | 返回 `null` | `:1092`、`:1099` |

按名称的重载（`invokeMethod(Object, String, Object...)` 等）在没有匹配方法时抛出 `IllegalArgumentException`
（`:836-840`）；不存在“找不到”的返回值。

### 3.3 `FieldUtils` — `reflect/FieldUtils.java`（1091 行）

```java
@Nullable public static Field findField(Object object, String fieldName)          // :78
@Nullable public static Field findField(Class, String, Class, Predicate, boolean) // :109-124
@Nonnull  public static Set<Field> findAllFields(Class, Predicate...)             // :415-422
@Nonnull  public static Set<Field> findAllDeclaredFields(Class, Predicate...)     // :456-463
public static Field getDeclaredField(Class, String)                               // :492-494 (throws)
@Nullable public static <V> V getFieldValue(boolean forceAccess, @Nullable Object instance,
        @Nullable Field field)                                                    // :815-844
@Nullable public static <V> V setFieldValue(boolean forceAccess, @Nullable Object instance,
        @Nullable Field field, @Nullable V value)                                 // :1013-1044
public static void assertFieldMatchType(Object, String, Class)                    // :1079-1087
```

| 场景 | 结果 | 行号 |
|---|---|---|
| `getFieldValue` 中 `field == null` | 返回 `null`——不抛异常 | `:818-820` |
| `getFieldValue` 中 `IllegalAccessException` | 包装为 **`IllegalStateException`** | `:830-831` |
| 对 JDK 内部字段使用 `forceAccess` | `:827` 处执行 `trySetAccessible(field)`；失败则 `IllegalAccessException` → `IllegalStateException` | `:826-831` |
| `getFieldValue(..., defaultValue)` | 对读到的值应用 `defaultIfNull` | `:651-655` |
| `setFieldValue` 且 `field == null` | 返回 `null` | `:1015` |
| `setFieldValue` 且当前值已等于新值 | **跳过**写入，返回既有值 | `:1027-1029` |
| `setFieldValue` 中 `IllegalAccessException` / `IllegalArgumentException` | 包装为 **`IllegalArgumentException`** | `:1030-1031` |

> [!NOTE]
> 值得记住的不对称：读失败表现为 `IllegalStateException`，写失败表现为 `IllegalArgumentException`。
> 多个 `setFieldValue` 的 javadoc 仍在 `@throws` 里列了 `IllegalAccessException`（`:876-878`、`:966-968`），
> 但 `:1030-1031` 已把它捕获，因此该受检异常永远不会逃出方法。

### 3.4 `ConstructorUtils` — `reflect/ConstructorUtils.java`

与 `MethodUtils` 完全一致地按动词划分出两种哲学：

| 动词 | 找不到时 | 抛出的异常 | 行号 |
|---|---|---|---|
| `getConstructor` / `getDeclaredConstructor` | — | `RuntimeException`，由 `ExecutableUtils.execute(...)` 转换 | `:205-231` |
| `findConstructor` / `findConstructors` / `findDeclaredConstructors` | `null` / 空列表（`NOT_FOUND_CONSTRUCTOR = null`，`:53`） | 无 | `:264-271` |
| `newInstance(Class, Object...)` | — | 无匹配时抛 `IllegalArgumentException` | `:324-332` |
| `newInstance(boolean forceAccess, Constructor, Object...)` | — | 访问失败抛 `IllegalStateException`；目标异常解包为 `RuntimeException` | `:393-411` |

`reflect/ExecutableUtils.java` 是共用的异常转换器：`execute` 重载见 `:100`、`:157`、`:215`；异常映射见 `:225-237`；参数类型匹配（可赋值 / 等价 / 非空三种模式）见 `:249-266`。

### 3.5 `JavaType` — `reflect/JavaType.java`（1329 行）

```java
public class JavaType implements Serializable {         // identity fields :112-116, volatile caches :120-124
    public static final JavaType[] EMPTY_JAVA_TYPE_ARRAY = new JavaType[0];   // :98
    public static final JavaType OBJECT_JAVA_TYPE = from(Object.class, CLASS); // :104
    public static final JavaType NULL_JAVA_TYPE = from(null, UNKNOWN);         // :110
    @Nullable public JavaType getSuperType();          // :311-318 memoised
    @Nonnull  public JavaType[] getInterfaces();       // :341-349 memoised
    @Nonnull  public JavaType[] getGenericTypes();     // :397-405 memoised
    public JavaType as(Class<?> targetClass);          // :454-465 supertypes :1096-1121 then interfaces :1123-1138
    public boolean equals(Object o);                   // :771-783 identity = (type, kind, source)
}
```

工厂方法的空值行为——最容易踩错的部分：

| 工厂 | 传入 `null` / 找不到时 | 行号 |
|---|---|---|
| `fromField(Field)` / `fromField(Class, String)` | `NULL_JAVA_TYPE` | `:849`；字段查找路径自 `:818` 起 |
| `fromMethodReturnType(Method)` / `(Class, String, Class...)` | `NULL_JAVA_TYPE` | `:911` |
| `fromMethodParameters(Class, String, Class...)` | `EMPTY_JAVA_TYPE_ARRAY` | `:943` |
| `fromMethodParameters(Method)` `:974` 与 `fromMethodParameter(Method, int)` `:1005` | **没有空值守卫**——直接解引用 | `:974`、`:1005` |

`JavaType.Kind`（`:1162-1327`）是一个内部策略枚举，每个 `java.lang.reflect.Type` 形态对应一个常量：`CLASS` `:1167`、
`PARAMETERIZED_TYPE` `:1201`、`TYPE_VARIABLE` `:1265`、`WILDCARD_TYPE` `:1270`、`GENERIC_ARRAY_TYPE` `:1275`、`UNKNOWN` `:1280`。
`valueOf(Type)`（`:1316-1325`）返回第一个 `isType(Type)` 为 `true` 的常量。

### 3.6 `TypeUtils` — `reflect/TypeUtils.java`（1579 行）

泛型解析引擎。缓存容量来自
`microsphere.reflect.resolved-generic-types.cache.size`（`:93`），默认值 `"256"`（`:98`，解析于 `:103`，读取于 `:114`，
并以 `@ConfigurationProperty` 标注，`:108-113`），写入
`private static final ConcurrentMap<MultipleType, List<Type>> resolvedGenericTypesCache`（`:116`）。核心解析是
`protected static List<Type> doResolveActualTypeArguments(Type, Class)`（`:581-632`）；public 的
`resolveActualTypeArguments(Type, Class)`（`:577-579`）把它包成 `unmodifiableList`。

`findTypes` 家族是一个 protected 方法（`:1092-1101`；类型为空或为 `Object` 时在 `:1097-1099` 返回 `emptyList()`）
加上一组按标志位配置的 public 包装：

| public 方法 | 行号 | 含自身 | 含层次类型 | 含泛型父类 | 含泛型接口 |
|---|---|---|---|---|---|
| `findAllGenericSuperclasses` | `:935` → `:936` | 否 | 是 | 是 | 否 |
| `findAllGenericInterfaces` | `:962` → `:963` | 否 | 是 | 否 | 是 |
| `findParameterizedTypes` | `:991` → `:992` | 是 | 否 | 是 | 是 |
| `findHierarchicalTypes` | `:1049` → `:1050` | 否 | 是 | 是 | 是 |
| `findAllTypes` | `:1083` → `:1084` | 是 | 是 | 是 | 是 |
| `findAllParameterizedTypes` | `:1019` → 在 `:1020` 委派给 `findAllTypes` | | | | |

此外：`getRawType` `:311`、`getRawClass` `:341`、`asClass` `:1299-1308`、`getComponentType` `:1509-1517`，以及 `getTypeNames(Type...)` `:1568-1574`——罕见的**抛异常**情形（`assertNoNullElements`）。

### 3.7 `reflect.generics` — 2 个类型

`ParameterizedTypeImpl`（`reflect/generics/ParameterizedTypeImpl.java`，242 行）是 JDK 内部类
`sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl` 的维护分支（`@see` 见 `:35`），也是*构造*
`ParameterizedType` 的唯一可移植手段：私有构造器 `:46-53`，工厂 `of(Class, Type[])` `:90` 与
`of(Class, Type[], Type ownerType)` `:119`，`getActualTypeArguments()` 中的防御性克隆 `:140-142`，`toString()`
`:208-241`，以及 `validateConstructorArguments`（`:55-64`）在实参数量不匹配时抛出的 `MalformedParameterizedTypeException`。
`TypeArgument`（`reflect/generics/TypeArgument.java`，88 行）是解析算法传递的不可变 `(Type, int index)` 二元组——构造器断言见 `:38-43`，工厂 `create(Type, int)` 见 `:85-87`。

### 3.8 `internal.reflect` — 1 个类型

`ConstantPoolUtils`（`internal/reflect/ConstantPoolUtils.java`，263 行）通过 JDK 内部的 `ConstantPool`
读取某个类的常量池，并**按名字、先旧后新**地解析它：`:255-259` 处先试 `sun.reflect.ConstantPool`（`:51`），再试
`jdk.internal.reflect.ConstantPool`（`:56`）；对外暴露 `CONSTANT_POOL_CLASS` `:62` 以及约 18 个通过
`MethodUtils.findMethod` 定位的 `Method` 句柄（`:67-147`）。`invoke(targetClass, method, args)` 捕获 `Throwable`、
以 `trace` 级别记日志并返回 `null`（`:240-249`），但 `getConstantPool(...)` 位于该 try **之外**（`:241` 对比
`:251-253`），因此那里的访问失败会向上抛出。它是 `core` 中唯一刻意去触碰 `sun.*` 的包，并用 `internal.*`
命名空间来表明这一点。

### 3.9 `beans` — 4 个类型

| 类型 | 角色 | 关键事实 |
|---|---|---|
| `BeanUtils`（576 行） | 门面：`Class` → `BeanMetadata`，bean → `Map` | `microsphere.bean.properties.max-resolved-depth` 默认 `100`（`:91`、`:108`、`:113`、`:124`）；`microsphere.bean.metadata.cache.size` 默认 `64`（`:129`、`:134`、`:139`、`:149`）；`beanMetadataCache` `:151`；`getBeanMetadata` `:285`；`findWriteMethod` `:321`；`findPropertyDescriptor` `:369` |
| `BeanMetadata`（152 行） | `BeanInfo` 的不可变包装 | 在 `execute` 中调用 `Introspector.getBeanInfo(beanClass, Object.class)`，见 `:55`；首字母小写且不可修改的属性 Map `:66-76`；`equals` **只按 bean 类**比较 `:112-118`；`of` `:147-150` |
| `BeanProperty`（137 行） | 某个 bean 的某个属性；值可变、标识不可变 | final 标识字段 `:39-49`；`of(Object, String)` `:122-136` |
| `ConfigurationProperty`（357 行） | 配置属性的可变模型 + 嵌套 `Metadata` | `name` 为 final `:69`；`type` 是一个 **`String`**，由 `Class` 经 `getTypeName` 设置 `:136-144`；`equals` 覆盖全部七个字段 `:177-190`；`Metadata.getSources()` / `getTargets()` 惰性创建的 linked hash set `:257-287` |

`resolvePropertiesAsMap`（`beans/BeanUtils.java`）的边界情形：`bean == null` → `emptyMap()`（`:453-455`）；深度 ≥
`maxResolvedDepth` → 该子树返回 `emptyMap()`，且静默（`:457-459`）；没有读方法的属性 → `null`（`:476-478`）；
每个可读属性都会**无条件**执行 `trySetAccessible(readMethod)`，与 `forceAccess` 无关（`:479`）；返回的 Map 不可修改（`:471`）。

---

## 4. 取自真实测试的示例

### 4.1 `trySetAccessible` 返回成功与否；`setAccessible` 返回先前的标志

测试夹具见 `reflect/AccessibleObjectUtilsTest.java:51`、`:58`、`:63`；测试体见 `:66-124`：

```java
private static final Method[] methods = String.class.getMethods();                       // :51
private static final Method tryCanAccessMethod =
        findMethod(AccessibleObjectUtils.class, "tryCanAccess", Object.class, AccessibleObject.class); // :58
private static final Constructor abstractProcessorConstructor = findConstructor(AbstractProcessor.class); // :63

for (Method method : methods) { assertEquals(method.isAccessible(), setAccessible(method)); } // :67-69 previous, not new status
assertEquals(setAccessible(tryCanAccessMethod), tryCanAccessMethod.isAccessible());            // :74
assertEquals(setAccessible(abstractProcessorConstructor), abstractProcessorConstructor.isAccessible()); // :76
assertTrue(trySetAccessible(tryCanAccessMethod));                                              // :106 private, same module
assertTrue(trySetAccessible(abstractProcessorConstructor));                                    // :107 protected, java.compiler
assertFalse(trySetAccessible(null, null));                                                     // :112 handle path swallows -> false
assertNull(tryCanAccess(null, null));                                                          // :117 no handle on JDK 8 -> null
assertFalse(canAccess(null, tryCanAccessMethod));                                              // :90
assertFalse(canAccess(test, cloneMethod));                                                     // :94 Object#clone stays inaccessible
assertDoesNotThrow(() -> handleInaccessibleObjectExceptionIfFound(new RuntimeException("For testing"))); // :123
```

`tryCanAccessMethod` 是同模块内的 `private` 方法，`AbstractProcessor` 的构造器是另一个命名模块中的 `protected`
成员；两者在 `:106-107` 都成功，而 `canAccess` 在标志被翻转之前仍然回答否（`:89-95`）。

### 4.2 `JavaType`：§3.5 每条承诺背后的断言块

`reflect/JavaTypeTest.java:63-67` 声明了 `HashMap<Integer, List<String>> mapField` 与
`fromValue(HashMap<Integer, List<String>)`；`:249-323` 是由 `testFromMethodReturnType`（`:91-98`）、
`testFromMethodParameters`（`:110-119`）和 `testFromField`（`:140-143`）共同驱动的共享断言块：

```java
JavaType javaType = fromMethodReturnType(getClass(), "fromValue", HashMap.class);        // :92
assertEquals(Kind.PARAMETERIZED_TYPE, javaType.getKind());                                // :251
assertNull(javaType.getSource());  assertTrue(javaType.isRootSource());                    // :256, :259
assertEquals(HashMap.class, javaType.toClass());                                           // :262
assertEquals(3, javaType.getInterfaces().length);   // Map, Cloneable, Serializable        // :264-268
JavaType cloneable = javaType.as(Cloneable.class);                                          // :271
assertEquals(javaType, cloneable.getSource());   // `as` records where you came from       // :276
assertEquals(0, cloneable.getGenericTypes().length);                                        // :279
JavaType mapType = javaType.as(Map.class);                                                  // :282
assertEquals(2, mapType.getGenericTypes().length);                                          // :290
assertEquals(Integer.class, mapType.getGenericType(0).toClass());                            // :291
```

`:235-247` 把同一个类型与 Spring 的 `ResolvableType.forType(...).resolveGenerics()` 交叉校验——这正是
`spring-core` 处于 test 作用域的原因（`microsphere-java-core/pom.xml:88-92`）。`:100-152` 锁定了空值契约：

```java
assertSame(NULL_JAVA_TYPE, fromMethodReturnType(getClass(), "fromValue", String.class));      // :102 no such method
assertSame(NULL_JAVA_TYPE, fromMethodReturnType(null));                                        // :105
assertSame(EMPTY_JAVA_TYPE_ARRAY, fromMethodParameters(getClass(), "fromValue", String.class)); // :123
assertEquals(NULL_JAVA_TYPE, fromField(getClass(), "notFoundField"));                          // :147
assertEquals(NULL_JAVA_TYPE, fromField(null));                                                 // :150
```

### 4.3 跨深层继承层次的泛型类型变量解析

`reflect/TypeUtilsTest.java:750-781` 直接对项目的泛型夹具调用包私有的核心方法：

```java
assertValues(doResolveActualTypeArguments(StringIntegerToBoolean.class, BiFunction.class), String.class, Integer.class, Boolean.class);   // :754
assertValues(doResolveActualTypeArguments(StringToIntegerConverter.class, Converter.class), String.class, Integer.class);                  // :760
assertValues(doResolveActualTypeArguments(B.class, Comparable.class), B.class);            // :763
assertValues(doResolveActualTypeArguments(C.class, Comparable.class), B.class);            // :766
assertValues(doResolveActualTypeArguments(D.class, C.class), String.class);                 // :769
assertValues(doResolveActualTypeArguments(E.class, Comparable.class), B.class);             // :772
assertValues(doResolveActualTypeArguments(StringIntegerBooleanHashMap.class, Map.class), String.class, Integer.class);                      // :775
```

要点正是 `C`/`D`/`E` 经过多重跳变抵达 `Comparable` 的类型变量：答案是按**类型变量名**匹配从父类型传播而来的
（`reflect/TypeUtils.java:706-729`）。public API 行为完全一致（`reflect/TypeUtilsTest.java:330-348`），任一参数为
`null` 时返回共享的空列表（`:784-787`），而 `resolveTypeArguments(Class)` 对 `null`（`:721-723`）、所有基本类型
（`:725-730`）与所有数组类（`:732-737`）都收敛为 `emptyList()`。

### 4.4 被禁用的方法、缓存清理，以及 `findMethod` 的两种形态

`reflect/MethodUtilsTest.java:115-126` 与 `:173-199` 展示了对缓存型查找而言必须做的清理，以及两条禁用路径
（`BANNED_METHODS_PROPERTY_NAME` = `microsphere.reflect.banned-methods`，测试见 `:133`，声明见
`reflect/MethodUtils.java:121-125`）；`:395-482` 记录了 `findMethod` 的两种形态——*不带参数类型*的那种含义是
“零参数”，而不是“任意重载”：

```java
@AfterAll
static void afterAll() {
    System.getProperties().remove(BANNED_METHODS_PROPERTY_NAME);
    clearMethodsCache(); clearBannedMethodsCache(); clearDeclaredMethodsCache();   // :123-125
}
String signature = buildSignature(String.class, "substring", int.class);            // :175
setProperty(BANNED_METHODS_PROPERTY_NAME, signature + " | " + signature2);          // :177
initBannedMethods();                                                                // :178
assertNull(findMethod(String.class, "substring", int.class));                        // :179
banMethod(declaringClass, methodName, parameterTypes);                                // :197

assertNotNull(findMethod(Object.class, "getClass"));                 // :397
assertNull(findMethod(Object.class, "equals"));                      // :408 — no zero-arg equals exists
assertNotNull(findMethod(Object.class, "equals", Object.class));      // :413
assertNull(findMethod(Object.class, "equals", int.class));            // :420
assertNull(findMethod(int.class, "equals", int.class));                // :481 — primitive classes have no members
```

而 `:485-512` 把 `invokeMethod` 的失败模式一一对应到 §3.2 的表格：

```java
assertEquals(test, invokeMethod(test, "toString"));                                    // :487
assertArrayEquals(values, invokeMethod(true, values, "clone"));                         // :494 protected Object#clone on an array
assertThrows(IllegalArgumentException.class, () -> invokeMethod("test", (Method) null)); // :499
assertThrows(IllegalArgumentException.class, () -> invokeMethod("test", "notFound"));    // :500
assertThrows(IllegalArgumentException.class, () -> invokeMethod("test", method, "abc")); // :506 wrong arity
assertThrows(RuntimeException.class, () -> invokeMethod(new ReflectionTest(), "errorMethod")); // :511 unwrapped target
```

### 4.5 字段访问，包括 JDK 内部的 `String.value`

`reflect/FieldUtilsTest.java:184-201` 是可访问性压力测试，是**按 JDK 分支**编写的，因为该字段的类型在 JDK 9 的紧凑字符串改造中发生了变化：

```java
String value = "Hello,World";
if (CURRENT_JAVA_VERSION.le(JAVA_VERSION_8)) {
    assertArrayEquals(value.toCharArray(), getFieldValue(true, value, "value", char[].class)); // :188
} else {
    assertArrayEquals(value.getBytes(), getFieldValue(true, value, "value", byte[].class));     // :190
}
```

`:210-245` 锁定了每一种失败模式，与 §3.3 完全对应；`:148-175` 则展示了会抛异常、且刻意不做层次查找的
`getDeclaredField`（字段不存在**以及**字段被继承时都抛 `RuntimeException`），紧挨着的是对 JDK 内部静态字段的成功读取
（`getStaticFieldValue(System.class, "in")`）：

```java
assertThrows(IllegalArgumentException.class, () -> getFieldValue(true, "test", field));      // :213 wrong owner
assertThrows(IllegalStateException.class, () -> getFieldValue(this, "value", String.class));  // :218 private field, forceAccess=false
assertThrows(NullPointerException.class, () -> getFieldValue(null, (String) null));          // :223
assertNull(setFieldValue(test, "notFoundField", null));                                       // :240
assertThrows(IllegalArgumentException.class, () -> setFieldValue(test, "privateField", 1));   // :245
assertThrows(RuntimeException.class, () -> getDeclaredField(ReflectionTestExt.class, "privateField")); // :160
```

### 4.6 构造器：`find` 返回 `null`，`get` 抛异常，`newInstance` 抛异常

`reflect/ConstructorUtilsTest.java:59-106` 把可见性隔离为唯一变量——四个夹具，各自在不同可见级别上有一个无参构造器。
`:108-123` 随后梳理了各类失败（参数类型不匹配 → `IllegalArgumentException` `:110-112`；私有构造器且未传
`forceAccess` → `IllegalStateException` `:117`；构造器自身抛异常 → `RuntimeException` `:122`）：

```java
assertEquals(2, findConstructors(PublicConstructorClass.class).size());                     // :61
assertEquals(0, findConstructors(ProtectedConstructorClass.class).size());                   // :62
assertEquals(1, findDeclaredConstructors(PrivateConstructorClass.class).size());             // :72
assertThrows(RuntimeException.class, () -> getConstructor(PrivateConstructorClass.class));   // :80
assertNotNull(findConstructor(PrivateConstructorClass.class));                                // :96
assertNotNull(newInstance(true, PrivateConstructorClass.class));                              // :102 forceAccess=true works
```

### 4.7 手工构造一个 `ParameterizedType`

`reflect/generics/ParameterizedTypeImplTest.java:46-77`、`:147`：

```java
topLevelType = of(Map.class, actualTypeArguments);                     // :48  -> Map<String,Integer>
nestedType   = of(Map.Entry.class, actualTypeArguments, Map.class);    // :49  -> Map.Entry<String,Integer>
complexType  = of(Map.Entry.class, actualTypeArguments, topLevelType);  // :50
assertEquals(topLevelType, complexType.getOwnerType());                 // :76
assertThrows(MalformedParameterizedTypeException.class, () -> of(Map.class, new Type[]{String.class})); // :55 arity
assertEquals("java.lang.String", of(String.class).toString());           // :147
```

### 4.8 bean → `Map`，以及深度护栏

`beans/BeanUtilsTest.java:107-132` 把一个 `ConfigurationProperty`（连同其嵌套的 `Metadata`）解析为以首字母小写属性名
为键的 Map。`:134-171` 是递归场景：持有 `Data[]` 的 `TestBean` 返回后，每个元素自身也被转成了 `Map<String, Object>`
（`:163-170`），而非 POJO 的叶子节点原样保留——`StringBuilder`（`:159`）、`AtomicInteger`（`:160`）、`TimeUnit`（`:161`）、`Class`（`:162`）。`:67-192` 覆盖了边界：

```java
assertSame(emptyMap(), resolvePropertiesAsMap(null));                                        // :175
assertThrows(RuntimeException.class, () -> resolvePropertiesAsMap(new Object()));             // :181
assertNotNull(resolvePropertiesAsMap(new FileChangedEvent(newRandomTempFile(), MODIFIED)));   // :186 depth guard fires, result still valid
assertSame(beanMetadata, getBeanMetadata(TestBean.class));                                    // :71 cached identity
assertNull(findPropertyDescriptor(beanMetadata, "notFound"));                                  // :85
assertNull(findWriteMethod(beanMetadata, "booleanValue"));                                    // :102 read-only property
```

`beans/BeanPropertyTest.java:59-118` 展示了 `BeanProperty.of` 以及它的 `equals` 覆盖了哪些内容：

```java
beanProperty = of(this, "value");                             // :61
assertEquals(TEST_VALUE, beanProperty.getValue());            // :66  read eagerly via the getter
beanProperty.setValue("new-value"); assertEquals("new-value", beanProperty.getValue()); // :67-68 value is mutable
assertEquals(BeanPropertyTest.class, beanProperty.getBeanClass());                       // :78 exact class; anonymous subclass differs :105-107
assertNotEquals(this.beanProperty, of(bean, "value"));                                     // :103 different *value* -> not equal
```

---

## 5. 兼容性矩阵

| 能力 | JDK 8 | JDK 9–15 | JDK 16–17 | JDK 21 / 25 | 证据（`release`） |
|---|---|---|---|---|---|
| 对自有类使用 `trySetAccessible` | ✅ 经 `setAccessible(true)` | ✅ 经 handle | ✅ 经 handle | ✅ 经 handle | `reflect/AccessibleObjectUtils.java:97-104`、`:170-178` |
| 对 JDK 内部成员使用 `trySetAccessible`（`String.value`、`AbstractProcessor.<init>`） | ✅ | ✅ | ⚠️ 返回 `false`，记录 JEP 396 提示，**不抛异常** | ⚠️ 同上 | `:157-168`、`:196-211`；`reflect/AccessibleObjectUtilsTest.java:106-107` |
| `canAccess(o, ao)` 的精确语义 | ⚠️ 由 `isAccessible()` 近似 | ✅ | ✅ | ✅ | `:142-149`、`:184-194` |
| 识别 `InaccessibleObjectException` | ✅ 按类**名**比较 | ✅ | ✅ | ✅ | `reflect/ReflectionUtils.java:165`、`:537-538` |
| 调用方类探测 | ✅ `sun.reflect.Reflection` | ⚠️ 该方法已被移除，`isSupportedSunReflectReflection()` 为 `false` | ⚠️ 同上 | ✅ 走 `StackWalker` 路径 | `reflect/ReflectionUtils.java:79`、`:90`、`:100`、`:117`、`:124`、`:168-181`；`reflect/ReflectionUtilsTest.java:177-185` |
| `StackWalker` 句柄被填充 | ❌ `null` | ✅ | ✅ | ✅ | `reflect/ReflectionUtils.java:117`、`:124`、`:184-200`；`reflect/ReflectionUtilsTest.java:166-175` 与 `:92-98` |
| `ConstantPoolUtils` 读取 | ✅ `sun.reflect.ConstantPool` | ✅ `jdk.internal.reflect.ConstantPool` | ⚠️ 需要常量池仍可达；失败以 `trace` 记日志 | ⚠️ 同上 | `internal/reflect/ConstantPoolUtils.java:255-259`、`:240-249` |
| `JavaType` / `TypeUtils` / `ParameterizedTypeImpl` | ✅ | ✅ | ✅ | ✅ | 纯 `java.lang.reflect.Type` API，不访问内部实现 |
| 对 JDK 内部 bean 调用 `BeanUtils.resolvePropertiesAsMap` | ✅ | ✅ | ⚠️ 取决于 §2.1 | ⚠️ 同上 | `beans/BeanUtils.java:479`（无条件 `trySetAccessible`） |

**何时需要 `--add-opens`。** 只有当你*强制*访问某个未开放该包的 JDK 成员时才需要：对 `java.base` 内部成员使用
`getFieldValue`/`setFieldValue`/`invokeMethod` 且 `forceAccess = true`，或对这类 bean 调用
`BeanUtils.resolvePropertiesAsMap`。库会在 `reflect/AccessibleObjectUtils.java:204-208` 打印出确切参数：
`It's require to add JVM Options '--add-opens=<module>/<package>=ALL-UNNAMED' for running`。对本包组而言通常是
`--add-opens=java.base/java.lang=ALL-UNNAMED`（`String.value`、`Class.getConstantPool`）或
`--add-opens=java.base/java.lang.invoke=ALL-UNNAMED`。reactor 构建**不会**添加它们——这些参数位于外部的 `microsphere-build` 父 POM（`pom.xml:8`），因此必须由消费方项目自己加上。

---

## 6. 陷阱与已知限制

1. **`findMethod(Class, name)` 不带参数类型时不会匹配重载。** 它的含义是“该名称的零参数方法”
   （`reflect/MethodUtils.java:629-632`；`reflect/MethodUtilsTest.java:395-423`）。
2. **被禁用的方法会让 `findMethod` 静默返回 `null`。** `microsphere.reflect.banned-methods`
   （`reflect/MethodUtils.java:121-125`，由 `initBannedMethods()` `:215-235` 应用）在 `:631` 处短路；任何涉及它的
   测试结束后都必须清空这三个缓存（`reflect/MethodUtilsTest.java:120-126`）。
3. **读失败与写失败的包装类型不同**——`IllegalStateException` 对比 `IllegalArgumentException`
   （`reflect/FieldUtils.java:830-831`、`:1030-1031`）。稳妥做法是捕获 `RuntimeException`。
4. **有两个 `JavaType` 工厂会解引用 `null` 方法。** `fromMethodParameters(Method)` `:974` 与
   `fromMethodParameter(Method, int)` `:1005` 缺少基于 `Class` 的重载所具备的守卫（`:911`、`:943`）。
5. **`JavaType.equals` 把 `source` 也算进去。** 同一个 `Type` 上的两个 `JavaType` 可能因为其中一个来自
   `as(...)` 而不相等（`reflect/JavaType.java:454-465`、`:771-783`；`reflect/JavaTypeTest.java:276`）。请比较 `getType()`。
6. **`BeanMetadata.equals` 忽略属性**——相等性只看 bean 类（`beans/BeanMetadata.java:112-118`）。
7. **`resolvePropertiesAsMap` 会静默截断。** 达到 `microsphere.bean.properties.max-resolved-depth`（默认 100，
   `beans/BeanUtils.java:108`、`:124`）时该分支返回 `emptyMap()`（`:457-459`）；最终得到的部分结果仍然合法，
   参见 `beans/BeanUtilsTest.java:185-192`。
8. **`BeanUtils` 总是对 getter 强制开放访问**（`beans/BeanUtils.java:479`），因此对 JDK 内部的 bean 会出现 §2.1
   所述的日志噪声，且没有关闭开关。
9. **`getTypeNames(Type...)` 遇到 `null` 元素会抛异常**（`reflect/TypeUtils.java:1568-1574`）——这是本包组“中性值降级”策略的例外。
10. **两个值得知道的内部限制。** `ParameterizedTypeImpl` 中 `validateConstructorArguments` 的 bounds 循环是空的
    （`reflect/generics/ParameterizedTypeImpl.java:61-63`），因此只校验实参数量（`:55-64`）；
    `ConstantPoolUtils` 调用 `getConstantPool(...)` 的位置在其 `catch (Throwable)` **之外**
    （`internal/reflect/ConstantPoolUtils.java:241` 对比 `:243-247`），所以不可达的 `Class.getConstantPool` 会抛异常而不是返回 `null`。
    `reflect/Modifier.java` 只是对 `int` 位掩码的枚举封装（`matchesAny`/`matchesAll`，`:402-437`），不做任何缓存。

---

## 7. 另见

* [语言抽象](./language-abstractions.zh.md) — `Version` / `Deprecation`（定义族）、`MutableInteger`（bean 深度护栏）、`ThrowableSupplier.execute`（`getConstructor` 背后的机制）。
* [类型转换](./type-conversion.zh.md) — 消费 `JavaType` / `TypeUtils` 来选择转换器。
* [类加载与构件](./classloading-and-artifacts.zh.md) — 另一个 JDK 内部实现的使用方；`--add-opens` 的情况相同。
* [工具与常量](./util-and-constants.zh.md) — `Utils` 标记接口、`Assert`，以及本包组拼装系统属性名所用的 `MICROSPHERE_PROPERTY_NAME_PREFIX`。
* 同主题的开发者指南：[../../developer-guide/microsphere-java-core/reflection-and-beans.zh.md](../../developer-guide/microsphere-java-core/reflection-and-beans.zh.md) — 可访问性决策树、泛型解析递归、缓存清单。
* 包组索引 [./README.zh.md](./README.zh.md) · 模块索引 [../../developer-guide/microsphere-java-core/README.zh.md](../../developer-guide/microsphere-java-core/README.zh.md)
