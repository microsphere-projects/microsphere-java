# 语言抽象（Language Abstractions）

`microsphere-java` 其余部分所依赖的小型、横切的基础词汇类型：排序（`Prioritized`）、分层对象（`Wrapper`）、
废弃元数据（`Deprecation`）、可抛异常的函数式接口，以及 method handle / lambda 内省辅助工具。

| 项目 | 值 |
|------|-----|
| Artifact | `io.github.microsphere-projects:microsphere-java-core` |
| 版本 | `0.3.20-SNAPSHOT`（`release` 分支） |
| 包 | `io.microsphere.lang`（7 个类型）、`io.microsphere.lang.function`（8 个）、`io.microsphere.lang.invoke`（1 个）、`io.microsphere.invoke`（2 个） |
| 类型总数 | 18 |
| Java 级别 | 语言/API 基线为 **Java 8**；源码在 JDK 8 / 11 / 17 / 21 / 25 上编译并通过测试 |
| 编译依赖 | 仅 `microsphere-java-annotations`（外加 JDK） |

---

## 1. 每个抽象解决的问题

| 类型 | 解决的问题 |
|------|-----------|
| `Prioritized` | “请给我排序，但我未必可比较。”为任意对象提供整型优先级，并附带一个能容忍非优先级元素与 `null` 的 `Comparator`，使异构集合可以排序而不抛 `ClassCastException`。 |
| `Wrapper` | “真实对象外面套了代理/装饰器。”一套获取底层实现的契约（把 `java.sql.Wrapper` 的思想推广到任意对象图）。 |
| `DelegatingWrapper` | 消除样板代码：只实现一个方法（`getDelegate()`）即可免费获得 `unwrap`/`isWrapperFor`。 |
| `WrapperProcessor` | 带类型参数的钩子（`<W extends Wrapper>`），让框架无需强制转换即可后处理被包装的 bean。 |
| `Deprecation` | `@Deprecated` 不携带任何数据。`Deprecation` 是可序列化、不可变的记录，包含*自哪个版本起*、*替代者*、*原因*、*链接*、*级别*——被本库的反射元数据模型使用。 |
| `ClassDataRepository` | 已缓存、急切构建的 classpath 索引：类名 → class path，包名 → 类名集合。需要注意首次访问的代价。 |
| `MutableInteger` | 可变的 `int` 盒子，使 lambda 与辅助方法能够修改计数器（Java 没有传引用）。 |
| `Throwable*` 接口 | JDK 的函数式接口无法抛出受检异常。这些变体允许把 `Files::readString` 这类调用传入 `map`/`forEach`，把失败处理策略*留到之后*决定。 |
| `Predicates`、`Streams` | 空安全、对数组/`Iterable` 友好的过滤能力，JDK `Stream` API 并不直接提供。 |
| `MethodHandleUtils`、`MethodHandlesLookupUtils` | `Lookup.findVirtual` 只能看到调用方有权访问的内容。这些工具构建（并缓存）具有宽访问模式的 `Lookup`，使 private/protected/package 方法可被调用，并在 JDK 拒绝时优雅降级。 |
| `LambdaUtils` | 编译后的 lambda 会擦除其泛型参数类型。该工具在运行时还原 SAM 的实际参数类型。 |

---

## 2. 版本信息与兼容范围

下表的 `@Since` 值直接抄自源码文件的 Javadoc。若源码未声明版本，本页会明确说明，而不去猜测。

| 类型 | 源码中的 `@since` | 说明 |
|------|-------------------|------|
| `io.microsphere.lang.Prioritized` | `1.0.0` | 见类 Javadoc（`Prioritized.java:54`）。 |
| `io.microsphere.lang.Wrapper` | **未声明** | 接口 Javadoc 只有 `@see` 标签，没有 `@since`，也没有 `@author`。 |
| `io.microsphere.lang.DelegatingWrapper` | `1.0.0` | |
| `io.microsphere.lang.WrapperProcessor` | `1.0.0` | |
| `io.microsphere.lang.Deprecation` | `1.0.0` | 作者 `Mercy`。 |
| `io.microsphere.lang.MutableInteger` | `1.0.0` | |
| `io.microsphere.lang.ClassDataRepository` | `1.0.0` | |
| `io.microsphere.lang.function.ThrowableFunction` | `1.0.0` | |
| `io.microsphere.lang.function.ThrowableConsumer` | `1.0.0` | |
| `io.microsphere.lang.function.ThrowableSupplier` | `1.0.0` | |
| `io.microsphere.lang.function.ThrowableAction` | `1.0.0` | |
| `io.microsphere.lang.function.ThrowableBiConsumer` | `1.0.0` | |
| `io.microsphere.lang.function.ThrowableBiFunction` | **未声明** | 接口 Javadoc 没有 `@since`。 |
| `io.microsphere.lang.function.Predicates` | `1.0.0` | |
| `io.microsphere.lang.function.Streams` | `1.0.0` | |
| `io.microsphere.lang.invoke.LambdaUtils` | `1.0.0` | |
| `io.microsphere.invoke.MethodHandleUtils` | `1.0.0` | 类级别为 `@since 1.0.0`；部分*成员*带有 JDK 级别标记（`MODULE`/`UNCONDITIONAL` 为 `@since 9`，`ORIGINAL` 为 `@since 16`）。 |
| `io.microsphere.invoke.MethodHandlesLookupUtils` | `1.0.0` | |

### 兼容范围

- **语言级别：Java 8。** 所有可抛异常的接口只使用 `@FunctionalInterface`、`default` 与 `static` 接口方法；
  `LambdaUtils` 与 `invoke` 包只使用 Java 7/8 起即可用的 `java.lang.invoke` API
  （`MethodHandles`、`MethodType`、`LambdaMetafactory`）。这些包中没有任何内容需要 Java 9+ 的语言特性。
- **触及的 JDK 内部实现。** `MethodHandleUtils` 通过反射构造 `java.lang.invoke.MethodHandles.Lookup` 的三个私有构造器
  （`MethodHandleUtils.java:157-167`）：
  | 构造器 | 可用版本 | 字段 |
  |--------|----------|------|
  | `Lookup(Class)` | JDK 7 起 | `lookupConstructor1` —— 已声明但 `newLookup` **未使用** |
  | `Lookup(Class, int)` | JDK 7 起 | `lookupConstructor2` —— 回退路径 |
  | `Lookup(Class, Class, int)` | JDK 14 起 | `lookupConstructor3` —— 存在时优先使用 |
  `Lookup` 位于 `java.lang.invoke`，由 `java.base` 导出，因此**不需要 `--add-opens`**；构造器本身是私有的，
  库使用自己的 `newInstance(true, …)`（强制访问）辅助方法。
- **访问模式常量**（`MODULE` `0x10`、`UNCONDITIONAL` `0x20`、`ORIGINAL` `0x40`）由本地对 `Lookup.PACKAGE` 做位移
  计算得出，因此在 `Lookup.MODULE`/`UNCONDITIONAL`/`ORIGINAL` 尚不是公开字段的 Java 8 上也能编译通过。
  `LookupMode.TRUSTED(-1)` 以数值方式复现了 JDK 已废弃的 `TRUSTED` 位。
- **`LambdaUtils` 需要类常量池。** 它通过包级私有的 `Class.getConstantPool()` 读取
  `sun.reflect.ConstantPool`（JDK ≤ 8 的名字）或 `jdk.internal.reflect.ConstantPool`（JDK 9+ 的名字）
  （`ConstantPoolUtils.java:51-62, 251-259`）。那里所有调用都包在 `try/catch (Throwable)` 中，所以在任何禁止该访问的
  JDK 或启动器配置下，解析只会返回空列表而不会失败。
- **废弃状态：** 这 18 个类型没有任何一个被 `@Deprecated` 标注。`Deprecation` 类是对废弃的*描述*，而不是废弃本身。


### 2.1 `0.3.20` 的发布日期与变更摘要

| 条目 | 值 | 依据 |
|---|---|---|
| 文档所在线 | `release` 分支，`${revision}` = `0.3.20-SNAPSHOT` | 根 `pom.xml:54` |
| 最新已发布版本 | `0.3.20`，标签日期 **2026-10-16** | 标签 `0.3.20` 的提交信息为 `chore: merge main into release [skip ci]`，提交时间 `2026-10-16T03:43:20Z` |
| 上一版本 | `0.3.19`，标签日期 **2026-08-30** | 标签 `0.3.19`，提交时间 `2026-08-30T12:51:37Z` |
| 变更日志来源 | 仓库根目录的 `release-notes.md` | 本项目不发布 `CHANGELOG.md`；`release-notes.md` 的 `v0.3.16`–`v0.3.20` 段落写着 *Release notes generation failed* 并保留原始提交列表，因此下文一律以提交主题引用，并逐条与 `release` 源码核对。 |

`0.3.20` 在这一区域没有新增 API 面——`0.3.19...0.3.20` 区间是一次加固性发布。它实际改变了什么、以及这些改动触及本页源码的程度如下：

* **空值契约。** 本页覆盖范围内 5 / 18 个主源码在签名上声明了 `@Nonnull` / `@Nullable`。区间内点名本区域的主题：`e1e83e85 Add nullability annotations to lang APIs` / `e0b17365 Add nullability annotations to function APIs` / `01918859 Annotate LambdaUtils return values` / `2cae6dba Refine nullability in invoke utilities`。
* **空输入与行为修复。** `0bc33ddc Annotate nullability in lang utilities`。

> [!NOTE]
> 如何读这份提交清单：`0bc33ddc` 的主题写的是 *lang utilities*，但本页范围内带注解的只有 18 个中的 5 个（`ClassDataRepository`、`Deprecation`、`LambdaUtils`，以及 `io.microsphere.lang.function` 里的 `Predicates`/`Streams`）；该主题的其余部分落在 `io.microsphere.util`，已由工具类页面计入。

---

## 3. API 参考

### 3.1 `io.microsphere.lang.Prioritized`

```java
public interface Prioritized extends Comparable<Prioritized> {

    Comparator<Object> COMPARATOR = (one, two) -> { /* 见 3.1.1 */ };

    int MAX_PRIORITY   = MIN_VALUE;   // Integer.MIN_VALUE
    int MIN_PRIORITY   = MAX_VALUE;   // Integer.MAX_VALUE
    int NORMAL_PRIORITY = 0;

    default int getPriority() { return NORMAL_PRIORITY; }

    @Override
    default int compareTo(Prioritized that) { return compare(this.getPriority(), that.getPriority()); }
}
```

请注意这里刻意反转的语义：`MAX_PRIORITY` *就是* `Integer.MIN_VALUE`，因为排序是升序的——“最高优先级”意味着
“排在最前”，因此取最小的 int。

#### 3.1.1 `COMPARATOR` 行为（已对照测试验证）

`COMPARATOR` 是 `Comparator<Object>` 而不是 `Comparator<Prioritized>`——它接受任何东西，包括 `null`。
下列断言来自 `microsphere-java-core/src/test/java/io/microsphere/lang/PrioritizedTest.java`：

```java
assertEquals(1, COMPARATOR.compare(minPriority, maxPriority)); // MIN_PRIORITY value wins over MAX_PRIORITY value
assertEquals(1, COMPARATOR.compare(null, prioritized));        // non-Prioritized sorts last
assertEquals(-1, COMPARATOR.compare(prioritized, null));
assertEquals(0, COMPARATOR.compare(null, "prioritized"));      // neither side is Prioritized
assertEquals(0, COMPARATOR.compare("prioritized", "prioritized"));
```

| `one` | `two` | 结果 |
|-------|-------|------|
| `Prioritized` | `Prioritized` | `Integer.compare(one.priority, two.priority)` |
| `Prioritized` | 其他任意值（含 `null`） | `-1` |
| 其他任意值 | `Prioritized` | `1` |
| 两者都不是 | 两者都不是 | `0` |

### 3.2 `io.microsphere.lang.Wrapper`

```java
public interface Wrapper {

    <T> T unwrap(Class<T> type) throws IllegalArgumentException;

    boolean isWrapperFor(Class<?> type);

    static <T> T tryUnwrap(Object object, Class<T> type) {
        if (object instanceof Wrapper) {
            Wrapper wrapper = (Wrapper) object;
            if (wrapper.isWrapperFor(type)) {
                return wrapper.unwrap(type);
            }
        }
        return null;   // 不是原始对象 —— 见 §5.1
    }
}
```

`isWrapperFor` 被规范定义为那个廉价判定方法：*“如果该方法返回 true，那么用同样的参数调用 `unwrap` 应当成功。”*

### 3.3 `io.microsphere.lang.DelegatingWrapper`

```java
public interface DelegatingWrapper extends Wrapper {

    Object getDelegate();

    @Override
    default <T> T unwrap(Class<T> type) throws IllegalArgumentException {
        if (getClass().equals(type)) {
            return (T) this;                     // unwrap to myself
        } else {
            if (isWrapperFor(type)) {
                return (T) getDelegate();        // unwrap one level
            }
        }
        throw new IllegalArgumentException(getClass().getName() + " can't unwrap the given type '" +
                type.getName() + QUOTE);
    }

    @Override
    default boolean isWrapperFor(Class<?> type) {
        Object delegate = getDelegate();
        return type.isInstance(delegate);
    }
}
```

用法（`microsphere-java-core/src/test/java/io/microsphere/lang/DelegatingWrapperTest.java`，辅助类
`DelegatingWrapperImpl.java`）：

```java
String delegate = "Hello";
DelegatingWrapper wrapper = new DelegatingWrapperImpl(delegate);
assertTrue(wrapper.isWrapperFor(String.class));
assertTrue(wrapper.isWrapperFor(CharSequence.class));  // supertype check, via Class#isInstance
assertEquals(delegate, wrapper.unwrap(String.class));
assertEquals(wrapper, wrapper.unwrap(DelegatingWrapperImpl.class));
assertThrows(IllegalArgumentException.class, () -> wrapper.unwrap(Integer.class));
```

### 3.4 `io.microsphere.lang.WrapperProcessor`

```java
public interface WrapperProcessor<W extends Wrapper> {
    W process(W wrapper);   // 可以返回同一个实例，也可以返回被进一步包装的实例
}
```

### 3.5 `io.microsphere.lang.Deprecation`

`@Immutable public final class Deprecation implements Serializable`，
`serialVersionUID = 6380988834632455932L`。

| 成员 | 签名 |
|------|------|
| 字段 | `@Nullable Version since`、`@Nullable String replacement`、`@Nullable String reason`、`@Nullable String link`、`@Nonnull Level level` |
| getter | `@Nullable Version getSince()`、`@Nullable String getReplacement()`、`@Nullable String getReason()`、`@Nullable String getLink()`、`@Nonnull Level getLevel()` |
| 嵌套枚举 | `public enum Level { DEFAULT, REMOVAL }` |
| 嵌套构建器 | `public static class Builder`，含 `protected Builder()`、`since(String)`、`since(@Nullable Version)`、`replacement(...)`、`reason(...)`、`link(...)`、`level(@Nullable Level)`、`build()` |
| 工厂方法 | `static Deprecation.Builder builder()`、`static Deprecation of(String since)`、`of(String, String)`、`of(String, String, String)`、`of(String, String, String, String)`、`of(String since, String replacement, String reason, String link, Level level)` |
| 构造器 | 包级私有：`Deprecation(Deprecation source)`（拷贝）与 `Deprecation(@Nullable Version, @Nullable String, @Nullable String, @Nullable String, @Nonnull Level)` |

两个构造器都是包级私有的，所以实例只能通过 `of(...)` 或 `builder()` 获得。`of` 的各个重载层层链式委托，
缺失的尾部默认为 `null`、级别默认为 `Level.DEFAULT`。`Builder.level(null)` **不是**错误——它通过 `defaultIfNull`
降级为 `DEFAULT`。`Builder.since(String)` 经由 `Version.of(since)` 转换。`equals`/`hashCode` 覆盖全部五个字段
（`level` 用 `==` 比较），且 `equals` 在引用相同时短路。

5 参数的 `of` **不是** varargs——它是五个独立的位置参数，因此参数顺序可以被静默写反
（`of("1.0", null, null, null, Level.DEFAULT)`）。

### 3.6 `io.microsphere.lang.MutableInteger`

`public class MutableInteger extends Number`，内部持有普通的 `private int value`。
API：`MutableInteger(int)`、`static MutableInteger of(int)`、`final int get()`、`final MutableInteger set(int)`
（链式）、`final int getAndSet(int)`、`final int getAndIncrement()`、`final int getAndDecrement()`、
`final int getAndAdd(int)`、`final int incrementAndGet()`、`final int decrementAndGet()`、`int addAndGet(int)`、
`intValue`/`longValue`/`floatValue`/`doubleValue`、`toString`、`equals`、`hashCode`。

方法名与 `AtomicInteger` 相似，但字段**不是** volatile，也**没有**原子性。见 §5.4。

### 3.7 `io.microsphere.lang.ClassDataRepository`

`@Immutable public class ClassDataRepository`，唯一公开入口是
`public static final ClassDataRepository INSTANCE`。
公开读取 API（除特别说明外均为 `@Nonnull @Immutable`）：
`getAllPackageNamesInClassPaths()`、`getClassNamesInPackage(Package)`、`getClassNamesInPackage(String)`、
`getClassNamesInClassPath(String classPath, boolean recursive)`、`getClassPathToClassNamesMap()`、
`getAllClassNamesInClassPaths()`；另有 `@Nullable String findClassPath(Class<?>)` 与
`@Nullable String findClassPath(String className)`。
**没有**公开的 `getAllClassPaths()` —— class path 集合只能通过 `getClassPathToClassNamesMap()` 的键观察到。

三个底层 map 是在实例字段初始化器中**急切构建**的
（`ClassDataRepository.java:87-91`：`classPathToClassNamesMap`、`classNameToClassPathsMap`、
`packageNameToClassNamesMap`），因此第一次触碰 `ClassDataRepository.INSTANCE` 时，会在任何方法返回之前
完成一次全 classpath 扫描（bootstrap class path 与系统 class path 合并，`:200-201`）。
结果中包含 JDK 类，返回的集合是 `unmodifiableSet` 视图（`:101`、`:165`）。

### 3.8 可抛异常的函数式接口（`io.microsphere.lang.function`）

六个接口都是 `@FunctionalInterface`，并共享同一形状：一个 `throws Throwable` 的抽象方法、一个永不向外传播受检失败的
`execute(...)`（把失败转交给 `handleException`/`ExceptionHandler`），以及一个 `static execute(...)` 便捷方法。

| 接口 | 抽象方法 | 安全调用 | 失败路由 |
|------|----------|----------|----------|
| `ThrowableFunction<T, R>` | `R apply(T t) throws Throwable` | `default R execute(T t) throws RuntimeException`<br>`default R execute(T t, BiFunction<T, Throwable, R> exceptionHandler)` | `default R handleException(T t, Throwable failure) { throw new RuntimeException(failure); }` |
| `ThrowableConsumer<T>` | `void accept(T t) throws Throwable` | `default void execute(T t)`<br>`default void execute(T t, BiConsumer<T, Throwable> exceptionHandler) throws NullPointerException` | `default void handleException(T t, Throwable failure)` |
| `ThrowableSupplier<T>` | `T get() throws Throwable` | `default T execute()`<br>`default T execute(Function<Throwable, T> exceptionHandler)` | `default T handleException(Throwable failure)` |
| `ThrowableAction` | `void execute() throws Throwable` | `default void execute(Consumer<Throwable> exceptionHandler)` | `default void handleException(Throwable failure)` |
| `ThrowableBiFunction<T, U, R>` | `R apply(T first, U second) throws Throwable` | `static R execute(T, U, ThrowableBiFunction<T,U,R>) throws NullPointerException`<br>`static R execute(T, U, ThrowableBiFunction<T,U,R>, ExceptionHandler<T,U,R>)` | 嵌套 `interface ExceptionHandler<T, U, R> { R handle(T first, U second, Throwable failure); }` + `ExceptionHandler DEFAULT_EXCEPTION_HANDLER` |
| `ThrowableBiConsumer<T, U>` | `void accept(T t, U u) throws Throwable` | `default ThrowableBiConsumer<T, U> andThen(ThrowableBiConsumer<? super T, ? super U> after) throws Throwable` | — |

`ThrowableFunction` 额外提供 `static <T,R> R execute(T t, ThrowableFunction<T,R> function) throws
IllegalArgumentException`、`static execute(T, function, BiFunction handler)`，以及组合能力：

```java
default <V> ThrowableFunction<V, R> compose(ThrowableFunction<? super V, ? extends T> before);
default <V> ThrowableFunction<T, V> andThen(ThrowableFunction<? super R, ? extends V> after);
```

两者都用 `assertNotNull` 拒绝 `null`，并把底层 `Throwable` 直接向外抛给组合函数的调用方（不做静默包装）。

`ThrowableBiFunction` 是 `invoke` 包真正使用的那一个：它的 `DEFAULT_EXCEPTION_HANDLER` 会构造消息
`"It's failed to execute the function with arguments[{}, {}] is failed"` 并抛出 `RuntimeException`。它是六个接口中
唯一把*默认*失败策略实现为静态字段、而不是可覆写的 `handleException` 方法的。

示例（`microsphere-java-core/src/test/java/io/microsphere/lang/function/ThrowableFunctionTest.java`）：

```java
ThrowableFunction<String, Integer> parser = Integer::valueOf;
Integer ok  = parser.execute("123");                 // 123
Integer bad = parser.execute("invalid", (input, ex) -> -1);  // -1, no exception escapes
```

### 3.9 `Predicates` 与 `Streams`

```java
public interface Predicates {                                     // @since 1.0.0
    static <T> Predicate<T>[] emptyArray();                       // Predicates.java:65
    static <T> Predicate<T> alwaysTrue();                         // :87
    static <T> Predicate<T> alwaysFalse();                        // :109
    static <T> Predicate<? super T> and(Predicate<? super T>... predicates);  // :138
    static <T> Predicate<? super T> or(Predicate<? super T>... predicates);   // :179
}
```

`Streams` 是一个静态方法*接口*（从不被实现），带 `@since 1.0.0`：
`stream(T... values)`、`stream(Iterable<T>)`；`filterStream`/`filterList`/`filterSet`/`filter` 各有 `T[]` 与
`Iterable` 两种形式；多谓词的 `filterAll(...)`、`filterAllList(T[], Predicate...)`、`filterAllSet(T[], Predicate...)`、
`filterAny(...)`、`filterAnyList`、`filterAnySet`；以及 `filterFirst(Iterable<T>, Predicate...)`。
`filterAll`/`filterAny` 接收 `Predicate<? super T>...` varargs，因此传入零个谓词是合法的，结果是未过滤（或空）的视图——
依赖它之前请先确认你需要的语义。

### 3.10 `io.microsphere.lang.invoke.LambdaUtils`

`public abstract class LambdaUtils implements Utils`，构造器为 `private`（不可实例化）。

```java
public static List<Class<?>> resolveLambdaMethodParameterTypes(@Nullable Object object);
public static List<Class<?>> resolveLambdaMethodParameterTypes(@Nullable Class<?> type);
public static List<Class<?>> resolveLambdaMethodParameterTypes(@Nullable Object object, @Nullable Class<?> functionalInterface);
public static List<Class<?>> resolveLambdaMethodParameterTypes(@Nullable Class<?> type, @Nullable Class<?> functionalInterface);

static Class<?>[] resolveParameterTypes(Method actualMethod, Class<?>... declaredParameterTypes); // package-private
```

保证：永不返回 `null`，永不抛异常——每一条拒绝路径都返回 `Collections.emptyList()`。

拒绝判定，按源码顺序（`LambdaUtils.java:98-129`）：`isLambdaClass(type)`；
`findFunctionalInterfaceMethod(...) != null`；`functionalInterface.isAssignableFrom(type)`；然后是常量池的逆向扫描。
不显式传入接口的重载还额外要求
`findAllInterfaces(type, ClassUtils::isFunctionalInterface).size() == 1`。

完整示例——全部取自 `microsphere-java-core/src/test/java/io/microsphere/lang/invoke/LambdaUtilsTest.java`：

```java
// (1) parameter type recovered from a compiled lambda
EventListener<EchoEvent> e = event -> { };
List<Class<?>> ts = resolveLambdaMethodParameterTypes(e, EventListener.class);
assertEquals(1, ts.size());
assertEquals(EchoEvent.class, ts.get(0));

// (2) same lambda WITHOUT the explicit interface -> empty, because the single-functional-interface rule fails
assertTrue(resolveLambdaMethodParameterTypes(e).isEmpty());

// (3) primitive SAM parameters survive as primitives
LongFunction<Long> lf = v -> v;
assertEquals(long.class, resolveLambdaMethodParameterTypes(lf).get(0));

// (4) a method reference captured through LambdaMetafactory resolves too
Function<String, String> upperLambda = toUpperLambda();   // built with LambdaMetafactory + findVirtual(String.toUpperCase)
assertEquals(String.class, resolveLambdaMethodParameterTypes(upperLambda, Function.class).get(0));

// (5) wrong functional interface -> empty (no exception)
assertTrue(resolveLambdaMethodParameterTypes(volumeLambda, Function.class).isEmpty());
assertTrue(resolveLambdaMethodParameterTypes(BiFunction.class, String.class).isEmpty());
```

`resolveParameterTypes` 可被直接测试，它通过返回**恒等**哨兵 `EMPTY_CLASS_ARRAY` 来表示不匹配
（测试用 `assertSame` 断言，因此不要对其副本使用 `assertEquals`）：

```java
Method method = findMethod(String.class, "valueOf", int.class);
assertSame(EMPTY_CLASS_ARRAY, resolveParameterTypes(method, Long.class));
```

### 3.11 `io.microsphere.invoke.MethodHandlesLookupUtils`

`public abstract class MethodHandlesLookupUtils implements Utils`，私有构造器。

```java
public static final MethodHandle NOT_FOUND_METHOD_HANDLE = null;   // MethodHandlesLookupUtils.java:67
public static final Lookup PUBLIC_LOOKUP = publicLookup();

public static MethodHandle findPublicVirtual(Class<?> requestedClass, String methodName, Class... parameterTypes);
public static MethodHandle findPublicStatic(Class<?> requestedClass, String methodName, Class... parameterTypes);

protected static MethodHandle findPublic(Class<?> requestedClass, String methodName, Class[] parameterTypes,
                                         ThrowableBiFunction<Lookup, MethodType, MethodHandle> function);
protected static MethodHandle find(Lookup lookup, Class<?> requestedClass, String methodName, Class[] parameterTypes,
                                   ThrowableBiFunction<Lookup, MethodType, MethodHandle> function);
protected static MethodHandle findPublic(Method method, ThrowableBiFunction<Lookup, MethodType, MethodHandle> function);
protected static MethodHandle find(Lookup lookup, Method method,
                                   ThrowableBiFunction<Lookup, MethodType, MethodHandle> function);
```

每个签名中的 `parameterTypes` 都是**裸类型** `Class...` / `Class[]`（未参数化）——若传入 `Class<?>[]` 会产生警告。
返回契约：方法未找到*或*查找抛出时返回 `null`；查找失败时会以 `trace` 级别记录
（`"The MethodHandle can't be found by Lookup[{}] on the method : {}"`）并返回 `null`。

### 3.12 `io.microsphere.invoke.MethodHandleUtils`

`public abstract class MethodHandleUtils implements Utils`，私有构造器。

```java
public static final int MODULE        = PACKAGE << 1;   // 0x10, @since 9
public static final int UNCONDITIONAL = PACKAGE << 2;   // 0x20, @since 9
public static final int ORIGINAL      = PACKAGE << 3;   // 0x40, @since 16
public static final int ALL_MODES = (PUBLIC | PRIVATE | PROTECTED | PACKAGE | MODULE | UNCONDITIONAL | ORIGINAL);
public static final Lookup PUBLIC_LOOKUP = publicLookup();

public enum LookupMode {
    PUBLIC, PRIVATE, PROTECTED, PACKAGE, MODULE, UNCONDITIONAL, ORIGINAL, ALL, TRUSTED;
    public static int getModes(LookupMode... lookupModes);
}

public static Lookup lookup(Class<?> requestedClass);                        // = lookup(rc, ALL)
public static Lookup lookup(Class<?> requestedClass, LookupMode... lookupModes);
public static MethodHandle findVirtual(Class<?> requestedClass, String methodName, Class... parameterTypes);
public static MethodHandle findStatic(Class<?> requestedClass, String methodName, Class... parameterTypes);
public static void handleInvokeExactFailure(Throwable e, MethodHandle methodHandle, Object... args);

protected static MethodHandle find(Class<?> requestedClass, String methodName, Class[] parameterTypes,
                                   ThrowableBiFunction<Lookup, MethodType, MethodHandle> function);
```

`findVirtual` / `findStatic` 与 `MethodHandlesLookupUtils` 对应方法的唯一差别在于它们放宽了查找范围：
public 且非 caller-sensitive 的方法仍走 `PUBLIC_LOOKUP`，其余（private / protected / package）则使用合成出的
全模式 `Lookup`。

测试（`microsphere-java-core/src/test/java/io/microsphere/invoke/MethodHandleUtilsTest.java`）证明了这一点，
因为四种可见性都能返回真正可调用的 handle：

```java
assertSame(lookup, lookup2);                                        // cached: same Lookup instance for same key
assertEquals(methodName, (String) findVirtual(getClass(), methodName, String.class).invokeExact(this));
assertSame(NOT_FOUND_METHOD_HANDLE, findVirtual(MethodHandleUtilsTest.class, "notFound"));
assertDoesNotThrow(() -> handleInvokeExactFailure(new Throwable("testing"), methodHandle));
```

`testFindVirtualOnPublicMethod` / `…OnProtectedMethod` / `…OnDefaultMethod` / `…OnPrivateMethod` 以及对应的
`testFindStaticOn…` 用例覆盖了全部可见性；同目录下的 `MethodHandleUtilsBenchmark.java` 是吞吐量基准，不是断言测试。

---

## 4. `Prioritized` 如何为库中其他部分的监听器、转换器与服务排序

`Prioritized` 是本库的排序契约，因此其他 SPI 接口是*继承*它而不是重新声明它：

| 子系统 | 契约 | 优先级的使用方式 |
|--------|------|------------------|
| 事件 | `io.microsphere.event.EventListener<E extends Event> extends java.util.EventListener, Prioritized` | `AbstractEventDispatcher` / `DirectEventDispatcher` 对监听器排序，使优先级值更低（即优先级更高）的监听器先执行；`GenericEventListener` 承载该值。 |
| 类型转换 | `io.microsphere.convert.Converter<S, T> extends Prioritized`；`io.microsphere.convert.multiple.MultiValueConverter<S> extends Prioritized` | 当多个转换器声明同一对源/目标类型时，优先级更高的转换器胜出——`Converter` 自己的 Javadoc 示例就返回 `Prioritized.MAX_PRIORITY`。 |
| SPI 加载 | `io.microsphere.util.ServiceLoaderUtils` | 在 `ServiceLoader` 迭代之后执行 `sort(serviceList, COMPARATOR)`（`ServiceLoaderUtils.java:890`），因此 `META-INF/services` 的书写顺序绝不决定优先级。 |
| 关闭钩子 | `io.microsphere.util.ShutdownHookUtils` | `new PriorityBlockingQueue<Runnable>(SHUTDOWN_HOOK_CALLBACKS_CAPACITY, COMPARATOR)`（`:114`）。 |
| 日志 | `io.microsphere.logging.LoggerFactory implements Prioritized` | 选出可用的最强日志后端。 |
| 类加载 | `io.microsphere.classloading.URLClassPathHandle`、`AbstractURLClassPathHandle`、`ArtifactResourceResolver` | 选出与当前 JDK 代际匹配的处理器。 |
| 进程 / 元数据 | `io.microsphere.process.ProcessIdResolver`、`io.microsphere.metadata.ConfigurationPropertyReader` / `Loader` / `Generator` | 责任链式排序；`ClassicProcessIdResolver` 在文档中说明使用 `NORMAL_PRIORITY + 9`。 |

由于 `COMPARATOR` 容忍非 `Prioritized` 元素，上述所有地方都能对混合集合排序——例如某个 `ServiceLoader` 结果中
部分实现者带 `Prioritized`、部分不带。

---

## 5. 陷阱

### 5.1 `Wrapper.tryUnwrap` 返回 `null`，而不是原始对象
它的 Javadoc 写着 *“@return unwrapped instance if possible, or the original object”*，但实现
（`Wrapper.java:113-121`）在对象不是 `Wrapper`、`isWrapperFor` 为 false、以及输入为 `null` 时都返回 `null`。
`WrapperTest` 锁定了真实行为：`assertNull(tryUnwrap("not a wrapper", String.class))`、
`assertNull(tryUnwrap(null, String.class))`。请始终把返回值当作可空。

### 5.2 `getPriority()` 的 Javadoc 与其代码自相矛盾
`Prioritized.java:94-100` 声称 *“the default is `#MIN_PRIORITY` minimum one”*，而方法体（`:99`）返回的是
`NORMAL_PRIORITY`（`0`）。请以代码为准——`new Prioritized() { }` 报告的是 `0`，这正是
`PrioritizedTest.testDefaultMethods()` 所断言的。

### 5.3 `MAX_PRIORITY` / `MIN_PRIORITY` 看起来像是写反了——其实没有
`MAX_PRIORITY == Integer.MIN_VALUE`，`MIN_PRIORITY == Integer.MAX_VALUE`。优先级是*排序名次*，升序。
写 `getPriority() { return MAX_PRIORITY; }` 的含义是“先执行我”，而 `compareTo` 做的正是对原始 int 调用
`Integer.compare`。切勿在自己的代码里用 `>` / `<` 比较优先级；请使用 `compareTo` 或 `COMPARATOR`。

### 5.4 `MutableInteger` 不是原子类型
它看起来像 `AtomicInteger`（`getAndIncrement`、`incrementAndGet`、`getAndAdd`），但保存的是一个非 volatile 的
`private int value`，且没有任何同步。它适用于单线程的 lambda 捕获与局部计数。并发使用会丢失更新，且跨线程可能不可见。

### 5.5 `DelegatingWrapper.unwrap` 本身不具备传递性
默认的 `unwrap` 只做一次 `isInstance` 检查后返回 `getDelegate()`——它**不会**递归。
一个包装了另一个 `DelegatingWrapper` 的 `DelegatingWrapper` 只能解开一层，除非你覆写 `isWrapperFor`/`unwrap`
继续向下委托。此外 `unwrap` 使用 `getClass().equals(type)`（精确类身份），因此把包装器自身解成它的
*父类或接口*会抛 `IllegalArgumentException`。

### 5.6 `ThrowableBiFunction` 吞掉的异常包装成的 `RuntimeException` 无法按实例覆写
与 `ThrowableFunction`/`ThrowableConsumer`/`ThrowableSupplier`/`ThrowableAction` 暴露可覆写的 `handleException`
不同，`ThrowableBiFunction` 的兜底逻辑是静态的 `DEFAULT_EXCEPTION_HANDLER`，其 3 参 `execute` 是一个**静态**方法，
并且没有 `execute(first, second)` 的实例形式。因此 `MethodHandlesLookupUtils` 必须显式传入处理函数 lambda
才能获得优雅降级。

### 5.7 `ThrowableBiConsumer.andThen` 仍然声明 `throws Throwable`
组合并不会消除受检异常：`default ThrowableBiConsumer<T, U> andThen(...) throws Throwable`。
调用处依然需要 `try/catch`。

### 5.8 `LambdaUtils` 会静默返回空列表
当 lambda 的接口闭包不恰好只含一个函数式接口时，`resolveLambdaMethodParameterTypes(x)`（不显式传接口）会返回空。
实现 `io.microsphere.event.EventListener` 的 lambda 会命中这一点，因为其闭包中包含多个候选——见 §3.10 的示例 (2)。
请显式传入函数式接口。另外：返回值是 `Collections.emptyList()`，所以 `assertEquals(0, …)` 可用，
但该实例是共享且不可变的。

### 5.9 method handle 的结果可空
`MethodHandleUtils.findVirtual/findStatic` 与 `MethodHandlesLookupUtils.findPublicVirtual/findPublicStatic`
都返回 `null`（即 `NOT_FOUND_METHOD_HANDLE`）而不抛异常。由于该常量字面就是 `null`，`assertSame` 与 `== null`
等价——但失败*原因*只在 `trace`/`warn` 级别输出，所以当某个 handle 神秘消失时，请为 `io.microsphere.invoke`
开启 trace。

### 5.10 caller-sensitive 方法被排除在 public 快速路径之外
`MethodHandleUtils.isCandidateMethod` 要求 `isPublic(method) && !isCallerSensitiveMethod(method)`。
`Class.forName`、`Proxy.newProxyInstance` 等属于 caller-sensitive，因此它们会走合成出的 `Lookup`。
如果某个 JDK 版本禁止构造 `Lookup`，整次查找会降级为 `null`——见 §5.11。

### 5.11 `lookup(Class)` 依赖 JDK 版本
`newLookup` 优先使用 `Lookup(Class, Class, int)`（JDK 14+），否则使用 `Lookup(Class, int)`。
如果这两个私有构造器都不可达（严格的模块/访问强制，或未来 JDK 将其移除），`newInstance(true, …)` 返回 `null`，
该 JVM 中所有针对非 public 成员的 `findVirtual`/`findStatic` 都会返回 `null`。这里不会抛异常，
请自行对结果做判空保护。

### 5.12 `LookupMode` 的 Javadoc 引用了在 Java 8 上无法编译的 JDK 成员
`Lookup#TRUSTED` 与 `Lookup#ALL_MODES` 仅通过 `@see` 引用；代码在本地重新计算了这些位值。
把枚举名理解为需要 `--add-opens` 之类的处理是个错误——库并不需要打开 `java.base` 的*包*，
因为 `Lookup` 是被导出的；被强制访问的只是它的私有构造器。

### 5.13 `ClassDataRepository` 首次触碰就要付出全量 classpath 扫描——而且永不刷新
它的三个 map 在单例的字段初始化器中被急切构建（`ClassDataRepository.java:87-91`），因此对
`ClassDataRepository.INSTANCE` 的第一次引用会在返回之前扫描所有 bootstrap 与系统 class path。
这使它成为一个启动延迟与内存问题，而不是廉价的访问器——不要在你在意的静态初始化器里触碰 `INSTANCE`。
它同时是进程级缓存，且没有提供文档化的刷新或失效 API，所以在容器热重载或多 classloader 场景下，
缓存视图会在该 JVM 的剩余生命周期内保持过期状态。

---

## 6. 另见

- [工具、常量与文本](./util-and-constants.zh.md) —— `Deprecation` 使用的 `io.microsphere.util.Version`
- [反射、类型与 bean](./reflection-and-beans.zh.md) —— `LambdaUtils` 背后的 `ConstantPoolUtils`；消费 `Deprecation` 的 `ExecutableDefinition`
- [类型转换](./type-conversion.zh.md) 与 [事件分发](./events.zh.md) —— `Prioritized` 最大的两个消费方
- [类加载与构件](./classloading-and-artifacts.zh.md) —— 按优先级挑选的 `Prioritized` 处理器
- 英文用户指南：[User Guide](./language-abstractions.md)
- 开发者视角的深入解析：[语言抽象 —— 开发者指南](../../developer-guide/microsphere-java-core/language-abstractions.zh.md)
