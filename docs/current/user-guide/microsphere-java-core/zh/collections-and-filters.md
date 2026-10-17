# 集合与过滤器（Collections and filters）

| | |
|---|---|
| **构件（Artifact）** | `io.github.microsphere-projects:microsphere-java-core`（`0.3.20-SNAPSHOT`，`release` 分支） |
| **包** | `io.microsphere.collection` —— 32 个主源码类型 · `io.microsphere.filter` —— 9 个主源码类型（合计 41 个） |
| **JDK 级别** | 以 Java 8 编译；CI 在 JDK 8 / 11 / 17 / 21 / 25 上测试 |
| **依赖的其它 `core` 包** | `io.microsphere.util`（`ArrayUtils.length`、`ObjectUtils.defaultIfNull`、`Assert.assertNotNull`、`ClassUtils.isAssignableFrom`、`Utils` 标记接口）、`io.microsphere.lang.DelegatingWrapper`、`io.microsphere.invoke.MethodHandlesLookupUtils`、`io.microsphere.lang.function.ThrowableSupplier`、`io.microsphere.constants`（分隔符常量） |
| **外部依赖** | 无 —— 纯 JDK |
| **被谁使用** | `reflect`、`beans`、`convert`、`io`、`io.scanner`、`classloading`、`json`、`metadata`（参见[开发者索引](../../../developer-guide/microsphere-java-core/zh/README.md)中的 `collection` 依赖边） |

本页所有 `file:line` 引用均相对于 `microsphere-java-core/src/main/java/io/microsphere/`（主源码）或
`microsphere-java-core/src/test/java/io/microsphere/`（测试），读取自 `release` 工作树。
首次完整出现之后，路径缩写为 `collection/Foo.java:40-55` / `filter/Foo.java:40-55`。

---

## 1. 这组能力解决什么问题

**当你必须把一个 `Iterator`、`Iterable`、`Queue` 或 `Deque` 交给别人，却又不知道来源是 `null`、空还是单元素时，
就该用 `io.microsphere.collection`。** `CollectionUtils` 中的每个工厂方法都是全函数
（`collection/CollectionUtils.java:43-687`）：输入 `null`，输出一个中性且不会抛异常的结果。
`isEmpty(null)` 为 `true`（`:58-60`），`size(null)` 为 `0`（`:348-350`），`first(null)` 为 `null`（`:541-551`），
`toIterable(null)` 返回共享的空迭代器（`:97-100`）。同一个包还提供了让迭代器或队列可以安全对外发布的四个包装家族
——`Delegating*`、`Unmodifiable*`、`Empty*`、`Singleton*`——以及与 `List.of` / `Set.of` / `Map.of` 等价的实现，
使为 Java 8 编译的代码在任何 JDK 上都能使用不可变集合（`Lists`、`Sets`、`Maps`）。

**当你需要一个有名字、可组合的谓词时，就该用 `io.microsphere.filter`。** `Filter<T>`（`filter/Filter.java:33-48`）
是一个 `@FunctionalInterface`，它继承 `java.util.Predicate<T>` 并把方法改名为 `accept(T)`，因此一个 filter 既是
lambda，也能直接交给 `Stream.filter(...)`；`FilterOperator`（`filter/FilterOperator.java:42-122`）在一组 filter 数组上
提供 AND / OR / XOR 组合；`FilterUtils.filter(...)`（`filter/FilterUtils.java:60-87`）把组合后的 filter 作用于任意
`Iterable`，并返回一个不可修改的 `List`。本包中的具体 filter 就是类路径扫描器所需的谓词：`ClassFilter`（`Class<?>`）、
`JarEntryFilter`（`JarEntry`）、`PackageNameClassNameFilter`（`String` 类名）、`ClassFileJarEntryFilter`、
`PackageNameClassFilter` 和 `TrueClassFilter`。

> [!NOTE]
> 四个包装家族**只覆盖 `Iterator` / `Iterable` / `Enumeration` / `Queue` / `Deque`**。本包里没有 `EmptyList`、
> `SingletonMap` 或 `UnmodifiableSet` 类——对这些类型，工具类直接委托给 JDK（`Collections.unmodifiableList`、`List.of`），
> 例如 `ListUtils.ofList(E...)`（`collection/ListUtils.java:189-196`）和 `SetUtils.ofSet(E...)`
> （`collection/SetUtils.java:153-155`）。

---

## 2. 坐标与版本支持

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- 最新已发布标签；0.3.20-SNAPSHOT 是同一版本线的开发版本 -->
</dependency>
```

| 事实 | 取值 | 证据（`release`） |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | `pom.xml:12` |
| `artifactId` | `microsphere-java-core` | 模块 `pom.xml` |
| 版本属性 | `${revision}`，构建时展平 | `pom.xml:14` |
| `release` 上 `${revision}` 的值 | `0.3.20-SNAPSHOT` | `pom.xml:54` |
| 最新已发布标签 | `0.3.20` | 中文组索引中的版本线表（`docs/current/user-guide/microsphere-java-core/zh/README.md:68`） |
| Java 级别 | 8（源码必须能在 JDK 8 上构建） | CI 矩阵 `java: [ '8', '11', '17', '21', '25' ]`，`.github/workflows/maven-build.yml:25`，`distribution: 'temurin'` `:33` |
| Spring Framework，JDK 17+ | `7.0.9` | `microsphere-java-parent/pom.xml:23` |
| Spring Framework，JDK `[1.8,17)` | `5.3.39`，来自 profile `java8-16` | `microsphere-java-parent/pom.xml:54-62`（激活条件 `<jdk>[1.8,17)</jdk>` 在 `:57`） |
| 与本页相关的字节码/API 约束 | 不使用 `record`、不使用 `var`、编译期不使用 `List.of` | `Lists.ofList()` 只通过 `MethodHandle` 解析 `List.of`（`collection/Lists.java:46-101`） |

> [!TIP]
> `java8-16` 是一个**构建期**属性开关，不是运行期开关；`microsphere-java-core` 自身并不依赖 Spring，因此本页内容不会
> 在 Spring 5 与 Spring 7 之间发生变化。当你把使用 Spring 的其它 `microsphere-java-*` 模块加入同一个 reactor 时，
> 它才会产生影响。

---

## 3. API 参考

### 3.1 `Iterator` / `Iterable` 家族

| 工厂 / 类 | 返回值 | 可变性 | null 许可 | 是否 `Serializable`？ |
|---|---|---|---|---|
| `CollectionUtils.emptyIterator()`（`collection/CollectionUtils.java:301-305`） | `Collections.emptyIterator()`——JDK 的共享实例，**不是** `EmptyIterator` | 只读 | 不适用 | JDK 实例 |
| `CollectionUtils.emptyIterable()`（`:327-331`） | `EmptyIterable.INSTANCE`（`collection/EmptyIterable.java:49`） | 只读 | 不适用 | 否 |
| `CollectionUtils.toIterable(Collection)`（`:97-100`） | 集合本身，或 `EmptyIterable.INSTANCE` | 与来源一致 | `null` → 空 | 否 |
| `CollectionUtils.toIterable(Iterator)`（`:120-123`） | `new IterableAdapter<>(iterator)`（`collection/IterableAdapter.java:47-59`） | 一次性（迭代器只能消费一次） | `null` → `EmptyIterator.INSTANCE`（`IterableAdapter.java:52`） | 否 |
| `CollectionUtils.toIterator(Enumeration)`（`:143-146`） | `new EnumerationIteratorAdapter<>(enumeration)`（`collection/EnumerationIteratorAdapter.java:55-72`） | 只读（`remove()` 抛异常） | `null` → `Collections.emptyEnumeration()`（`:60`） | 否 |
| `CollectionUtils.toIterable(Enumeration)`（`:166-169`） | `toIterable(toIterator(e))` | 只读 | `null` → 空 | 否 |
| `CollectionUtils.singletonIterator(E)`（`:215-219`） | `new SingletonIterator<>(element)`（`collection/SingletonIterator.java:39-71`） | 只读 | **`null` 元素会被保留并返回一次**（见陷阱 2） | 否 |
| `CollectionUtils.singletonIterable(E)`（`:188-192`） | `toIterable(singletonIterator(e))` | 只读 | 同上 | 否 |
| `CollectionUtils.singletonEnumeration(E)`（`:242-246`） | `new SingletonEnumeration<>(element)`（`collection/SingletonEnumeration.java:52-75`） | 只读 | `null` 会被保留并返回一次 | 否 |
| `CollectionUtils.unmodifiableIterator(Iterator)`（`:278-282`） | `new UnmodifiableIterator<>(iterator)`（`collection/UnmodifiableIterator.java:51-73`） | `remove()` → `IllegalStateException` | 首次使用时 `null` **不被**容忍（见陷阱 3） | 否 |
| `new DelegatingIterator<>(it)`（`collection/DelegatingIterator.java:41-88`） | 透传的 `Iterator` + `DelegatingWrapper` | 完全委托 | 构造函数允许 `null` 委托对象（`:45-47`），但使用时失败 | 否 |
| `EmptyIterator.INSTANCE`（`collection/EmptyIterator.java:63`） | 唯一共享实例 | 只读 | 不适用 | 否 |
| `Iterators.equals(a, b)`（`collection/Iterators.java:41-55`） | `boolean` | — | 两个参数都是 `@Nullable`，`null` ≠ 非 null | — |

### 3.2 `Queue` / `Deque` 家族

| 工厂 / 类 | 返回值 | 可变性 | null 许可 | 是否 `Serializable`？ |
|---|---|---|---|---|
| `CollectionUtils.emptyQueue()`（`:644-648`）→ `QueueUtils.emptyQueue()`（`collection/QueueUtils.java:125-129`） | 共享的 `QueueUtils.EMPTY_DEQUE` 的强制转换结果（`QueueUtils.java:52`） | `EmptyDeque` | 不适用 | **是**（`collection/EmptyDeque.java:63`） |
| `CollectionUtils.emptyDeque()`（`:678-682`）→ `QueueUtils.emptyDeque()`（`:144-148`） | 同一个共享实例 | 同上 | 不适用 | 是 |
| `QueueUtils.unmodifiableQueue(q)`（`:172-175`） | `new UnmodifiableQueue(queue)`（`collection/UnmodifiableQueue.java:65-207`） | 修改类操作 → `UnsupportedOperationException` | 构造函数不拒绝 `null`（`:71-73`） | 是（`:65`） |
| `QueueUtils.unmodifiableDeque(d)`（`:199-203`） | `new UnmodifiableDeque(deque)`（`collection/UnmodifiableDeque.java:60-153`） | 同上，另外 `getFirst/getLast/peekFirst/peekLast` 直接读取 | 允许传入 `null`，使用时失败 | 是（`:60`） |
| `QueueUtils.singletonQueue(e)` / `singletonDeque(e)`（`:225-229`、`:251-255`） | `new SingletonDeque<>(e)`（`collection/SingletonDeque.java:63-150`） | `offer*`/`poll*`/`push`/`removeFirstOccurrence` → UOE | 允许 `null` 元素，`size()` 仍为 `1` | 是（`:63`） |
| `QueueUtils.reversedDeque(d)`（`:289-291`）→ `ReversedDeque.of(d)`（`collection/ReversedDeque.java:46-52`） | `ReversedDeque` 视图（若参数已是 `ReversedDeque` 则原样返回） | 完全可变，方向镜像 | `null` → `IllegalArgumentException` —— `of` 会走到 `new ReversedDeque<>(null)`（`:50`），其 super 调用命中 `DelegatingQueue` 的守卫（`collection/DelegatingQueue.java:44-47`） | 否 |
| `QueueUtils.ofQueue(e...)`（`:317-321`） | `unmodifiableDeque(newArrayDeque(elements))` | 在全新副本上的只读视图 | `null` 元素 → `NullPointerException` —— 元素通过 `ArrayDeque.add` 逐个添加（`collection/QueueUtils.java:383-384`） | 是 |
| `new DelegatingQueue<>(q)`（`collection/DelegatingQueue.java:40-183`） | 透传的 `Queue` | 完全委托 | **`null` → `IllegalArgumentException`**（`:44-47`） | 否 |
| `new DelegatingDeque<>(d)`（`collection/DelegatingDeque.java:34-148`） | 透传的 `Deque` | 完全委托 | `null` → `IllegalArgumentException`（继承而来） | 否 |
| `AbstractDeque<E>`（`collection/AbstractDeque.java:82-143`） | 骨架基类 | 由子类定义 | 由子类定义 | 否 |

### 3.3 不可变 Entry、栈与枚举

| 类型 | 类别 | 关键行为 |
|---|---|---|
| `DefaultEntry<K,V>`（`collection/DefaultEntry.java:35-95`） | 可变 `Map.Entry` | `key` 是 `final` 的（`:37`），`value` 不是（`:39`）；`setValue` 返回旧值（`:69-74`）；`toString()` 为 `key + "=" + value`（`:91-94`）；工厂方法 `of`（`:55-57`） |
| `ImmutableEntry<K,V>`（`collection/ImmutableEntry.java:47-70`） | 只读 Entry | 继承 `DefaultEntry`，只覆写 `setValue` → `UnsupportedOperationException("ReadOnly Entry can't be modified")`（`:53-56`） |
| `MapUtils.ofEntry(k,v)` / `immutableEntry(k,v)`（`collection/MapUtils.java:1081`、`:1110`） | 工厂方法 | `Map.Entry` 创建背后的一对可变/不可变实现 |
| `ArrayStack<E>`（`collection/ArrayStack.java:41`） | `extends ArrayList<E>` | 在 `ArrayList` 之上实现栈操作 |
| `ArrayEnumeration<E>`（`collection/ArrayEnumeration.java:54`） | `implements Enumeration<E>` | 数组支撑的枚举 |
| `EnumerationUtils.of(E...)` / `ofEnumeration(E...)`（`collection/EnumerationUtils.java:96`、`:119`） | 工厂方法 | `isEnumeration` 重载在 `:55`、`:73` |

### 3.4 `*of*` 工厂（在 Java 8 上使用 Java 9 的不可变集合）

| 类 | 模拟目标 | 元数重载 | Java 8 回退实现 |
|---|---|---|---|
| `Lists`（`collection/Lists.java:41-533`） | `List.of` | `ofList()` … `ofList(e1..e10)` + `ofList(E...)`（`:119`、`:152`、`:513`） | `Collections.emptyList()`（`:123-132`）、`Collections.singletonList(e1)`（`:156-165`）、`ListUtils.of(elements)`（`:520-529`） |
| `Sets`（`collection/Sets.java`） | `Set.of` | `ofSet()` … `ofSet(e1..e10)` + `ofSet(E...)`（`:121`、`:158`、`:552`） | 同样的 `MethodHandle == null` 模式 |
| `Maps`（`collection/Maps.java`） | `Map.of` | `ofMap()` … `ofMap(k1..k10)` + `ofMap(Entry...)`（`:116`、`:150`、`:585`） | 同样模式 |
| `ListUtils.ofList(E...)`（`collection/ListUtils.java:189-196`） | `Collections.unmodifiableList(Arrays.asList(...))` | — | 在所有 JDK 上行为一致 |
| `MapUtils.of(...)`（`collection/MapUtils.java:207-388`） | 1–5 个键值对、`of(Object...)`、`of(Entry...)` | — | 手工构造 map |

### 3.5 真实签名（真实修饰符、真实注解）

```java
// collection/CollectionUtils.java — abstract + 私有构造函数：不可实例化，也不用于继承（:43, :684-685）
public abstract class CollectionUtils implements Utils {
    public static boolean isEmpty(@Nullable Collection<?> collection);                      // :58
    @Nonnull public static <E> Iterable<E>  toIterable(@Nullable Collection<E> c);          // :97-98
    @Nonnull public static <E> Iterator<E>  toIterator(@Nullable Enumeration<E> e);         // :143-144
    @Nonnull @Immutable public static <E> Iterator<E>  singletonIterator(@Nullable E e);    // :215-217
    @Nonnull @Immutable public static <E> Iterator<E>  unmodifiableIterator(@Nullable Iterator<E> it); // :278-280
    @Nonnull @Immutable public static <E> Iterator<E>  emptyIterator();                     // :301-303
    @Nonnull @Immutable public static <T> Queue<T>     emptyQueue();                        // :644-646
    @Nonnull public static int size(@Nullable Collection<?> c);                             // :348
    @Nullable public static <T> T first(@Nullable Collection<T> values);                    // :541-542
    public static <T> int addAll(@Nullable Collection<T> c, T... newValues);                // :454

// collection/QueueUtils.java — Queue/Deque 工厂接口面（:50）
    public static final Deque<?> EMPTY_DEQUE = EmptyDeque.INSTANCE;                          // :52
    @Nonnull @Immutable public static <E> Queue<E> emptyQueue();                             // :125-127
    @Nonnull @Immutable public static <E> Deque<E> emptyDeque();                             // :144-146
    @Nonnull public static <E> Queue<E> unmodifiableQueue(Queue<E> queue);                   // :172-173
    @Nonnull public static <E> Deque<E> unmodifiableDeque(Deque<E> deque);                   // :199-201
    @Nonnull @Immutable public static <E> Queue<E> singletonQueue(E element);                // :225-227
    @Nonnull @Immutable public static <E> Deque<E> singletonDeque(E element);                // :251-253
    public static <E> Deque<E> reversedDeque(Deque<E> deque);                                 // :289
    @Nonnull @Immutable public static <E> Queue<E> ofQueue(E... elements);                    // :317-319

// collection/DelegatingIterator.java — 每个方法都是 FINAL（:41-88）：这是一个封闭的透传装饰器
public class DelegatingIterator<E> implements Iterator<E>, DelegatingWrapper {
    private final Iterator<E> delegate;                                    // :43
    public DelegatingIterator(Iterator<E> delegate);                       // :45-47
    @Override public final boolean hasNext();                             // :49-52
    @Override public final E next();                                      // :54-57
    @Override public final void remove();                                 // :59-62
    @Override public final void forEachRemaining(Consumer<? super E> a);   // :64-67
    @Override public final Object getDelegate();                           // :69-72
    @Override public final int hashCode();  @Override public final boolean equals(Object o);
    @Override public final String toString();                              // :74-87

// collection/ReadOnlyIterator.java — 迭代器树中唯一会抛异常的切点
@Immutable public abstract class ReadOnlyIterator<E> implements Iterator<E> {   // :49-50
    @Override public final void remove() { throw new IllegalStateException("Read-Only"); }  // :52-55

// collection/DelegatingQueue.java — protected 委托字段、非 final 方法：便于子类扩展
public class DelegatingQueue<E> implements Queue<E>, DelegatingWrapper {         // :40
    protected final Queue<E> delegate;                                          // :42
    public DelegatingQueue(Queue<E> delegate) { assertNotNull(delegate, ...); }   // :44-47
    @Override public Queue<E> getDelegate();                                      // :179-182

// collection/DelegatingDeque.java — Java 21 的 Deque.reversed()，并带 Java 8 回退
    private static final MethodHandle reversedMethodHandle = findPublicVirtual(Deque.class, "reversed"); // :39
    public Deque<E> reversed();                                                  // :138-140
    protected Deque<E> reversed(MethodHandle reversedMethodHandle);             // :142-147

// filter/Filter.java — 谓词根接口
@FunctionalInterface public interface Filter<T> extends Predicate<T> {           // :33-34
    boolean accept(T filteredObject);                                            // :42
    @Override default boolean test(T t) { return accept(t); }                     // :44-47

// filter/FilterOperator.java
public enum FilterOperator { AND, OR, XOR;                                        // :42-92
    public abstract <T> boolean accept(T filteredObject, Filter<T>... filters);   // :102
    public final <T> Filter<T> createFilter(Filter<T>... filters);                // :111-120

// filter/FilterUtils.java
    @Nonnull @Immutable public static <E> List<E> filter(Iterable<E> iterable, Filter<E> filter);                    // :60-64
    @Nonnull @Immutable public static <E> List<E> filter(Iterable<E> iterable, FilterOperator op, Filter<E>... filters); // :75-87
```

---

## 4. 可运行示例（取自真实测试用例）

**包装一个 `Iterator` 并读回它的身份** — 来自
`microsphere-java-core/src/test/java/io/microsphere/collection/DelegatingIteratorTest.java:48-99`：

```java
List<String> list = ofArrayList("1", "2", "3");   // ListUtils.ofArrayList，测试导入见 :28
Iterator<String> iterator = list.iterator();
DelegatingIterator<String> delegatingIterator = new DelegatingIterator<>(iterator);

assertEquals("1", delegatingIterator.next());     // :62-64
assertThrows(NoSuchElementException.class, delegatingIterator::next);  // :65
assertSame(delegatingIterator.getDelegate(), iterator);               // :83
assertEquals(delegatingIterator, iterator);                           // :93  equals() 透传
assertEquals(delegatingIterator.toString(), iterator.toString());     // :98
```

`remove()` 在 `:69-74` 被逐个元素读取后再删除来覆盖——`ArrayList` 的迭代器本身就支持删除，因此
`DelegatingIterator` 完全不需要自己做任何判断。

**`SingletonIterator` 继承而来的只读契约** — `collection/ReadOnlyIteratorTest.java:41-75` 是共享基类；
`collection/SingletonIteratorTest.java:30-36` 只提供实例：

```java
class SingletonIteratorTest extends ReadOnlyIteratorTest {
    @Override protected Iterator<?> createIterator() { return singletonIterator(TEST_ELEMENT); }  // :33-35
}
// 由基类负责断言：
//   hasNext() 为 true，next() == TEST_ELEMENT，第二次 next() 抛 NoSuchElementException（:53-59）
//   remove() 抛 IllegalStateException                                                              （:62-64）
//   forEachRemaining(...) 消费该元素之后 hasNext() == false                                       （:66-75）
```

**`Empty*` 单例** — `collection/EmptyIteratorTest.java:22-40` 与 `collection/EmptyDequeTest.java:41-91`：

```java
EmptyIterator emptyIterator = new EmptyIterator();          // :24
assertFalse(emptyIterator.hasNext());                        // :29
assertThrows(NoSuchElementException.class, emptyIterator::next);    // :34
assertThrows(IllegalStateException.class, emptyIterator::remove);   // :39  <-- 不是 UnsupportedOperationException

EmptyDeque emptyDeque = INSTANCE;                            // :41
assertSame(emptyIterator(), emptyDeque.iterator());          // :45  同一个共享的 JDK 空迭代器
assertSame(emptyIterator(), emptyDeque.descendingIterator()); // :50
assertThrows(UnsupportedOperationException.class, () -> emptyDeque.offerFirst(null)); // :55
assertNull(emptyDeque.pollFirst());                          // :65  中性值，不抛异常
assertThrows(NoSuchElementException.class, emptyDeque::getFirst);  // :75
assertFalse(emptyDeque.removeLastOccurrence(null));          // :85
assertEquals(0, emptyDeque.size());                           // :90
```

**`UnmodifiableDeque` 的「修改 / 观察」分界** — 来自
`microsphere-java-core/src/test/java/io/microsphere/collection/UnmodifiableDequeTest.java:35-127`。该视图构建在另一个
包装器之上（`deque = new UnmodifiableDeque(new SingletonDeque(TEST_ELEMENT))`，`:41`），测试正是用这种方式在不借助 JDK
集合的情况下得到一个单元素双端队列：

```java
assertThrows(UnsupportedOperationException.class, () -> deque.addFirst(TEST_ELEMENT));    // :46
assertThrows(UnsupportedOperationException.class, () -> deque.offerLast(TEST_ELEMENT));   // :61
assertThrows(UnsupportedOperationException.class, deque::pollFirst);                       // :76
assertThrows(UnsupportedOperationException.class, deque::pop);                             // :121
assertEquals(TEST_ELEMENT, deque.getFirst());                     // :86  读取操作直接透传
assertEquals(TEST_ELEMENT, deque.peekLast());                     // :101
assertTrue(deque.descendingIterator() instanceof UnmodifiableIterator);                   // :126
```

`UnmodifiableQueueTest` 对 `Queue` 层面断言了同样的分界（`add`/`offer`/`poll` 抛异常，`peek`/`element` 正常读取）。

**用单个谓词做过滤** — 来自
`microsphere-java-core/src/test/java/io/microsphere/filter/FilterTest.java:37-52`：

```java
Filter<String> filter = value -> "true".equalsIgnoreCase(value);   // :39
assertFalse(filter.accept("test"));  assertTrue(filter.accept("true"));  // :44-45
assertFalse(filter.test("test"));    assertTrue(filter.test("true"));    // :50-51  Predicate 桥接
```

**组合 filter 并作用于集合** — 来自
`microsphere-java-core/src/test/java/io/microsphere/filter/FilterUtilsTest.java:26-40`：

```java
List<Integer> integerList = asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

List<Integer> result = filter(integerList, AND, filteredObject -> filteredObject % 2 == 0);
assertEquals(asList(0, 2, 4, 6, 8), result);          // :29-31

result = filter(integerList, filteredObject -> filteredObject % 2 == 0);   // 单 filter 重载 -> AND
assertEquals(asList(0, 2, 4, 6, 8), result);          // :33-35

result = filter(integerList, OR, filteredObject -> filteredObject % 2 == 1);
assertEquals(asList(1, 3, 5, 7, 9), result);          // :37-39
```

返回的列表是收集结果的 `Collections.unmodifiableList`（`filter/FilterUtils.java:78-86`），因此 `result.add(...)`
会抛异常。

**按包名过滤** — `filter/PackageNameClassNameFilter.java:43-60`，配合
`filter/PackageNameClassNameFilterTest.java` 与 `PackageNameClassFilterTest.java`：

```java
Filter<String> filter = new PackageNameClassNameFilter("io.microsphere", true);
filter.accept("io.microsphere.collection.Lists");        // true  — 包名完全相等
filter.accept("io.microsphere.filter.Filter");           // true  — 子包
filter.accept("io.other.Foo");                           // false
filter.accept("");                                       // false — 空白输入被短路（:51-53）
```

**容忍 null 的集合管道** — 在
`microsphere-java-core/src/test/java/io/microsphere/collection/CollectionUtilsTest.java` 中被直接断言（`testIsEmpty`
`:81`、`testToIterable` `:122`、`testSize` `:167`、`testEquals` `:191-195`、`testAddAllWithNullCollection` `:230`、
`testAddAllWithNullList` `:247`、`testAddAllWithNullIterable` `:252`、`testFirst` `:257`、`testEmptyQueue` `:272`、
`testEmptyDeque` `:278`）。这些行为由下面的代码行定义：

```java
isEmpty(null)                    // true   — collection/CollectionUtils.java:58-60
size((Collection) null)          // 0      — :348-350
size((Iterable) null)            // 0      — :374-377
first((Collection) null)         // null   — :541-545
addAll(null, "a", "b")           // 0      — :454-457
toIterable((Collection) null)    // EmptyIterable.INSTANCE — :97-100
equals(null, null)               // true   — :413-415（测试 :195）
equals(null, singletonList)      // false  — :421-423（测试 :192）
equals(emptyList, null)          // true   — :417-419，"null 与空集合等价"
```

---

## 5. 兼容性说明

| 能力 | Java 8 | Java 9 – 20 | Java 21+ |
|---|---|---|---|
| `Lists.ofList(...)`（`collection/Lists.java:119-529`） | 回退到 `Collections.emptyList` / `singletonList` / `ListUtils.of` | 通过 `MethodHandle` 调用真正的 `List.of` | 真正的 `List.of` |
| `Sets.ofSet(...)`、`Maps.ofMap(...)` | 回退路径，同样的模式 | 真正的 `Set.of` / `Map.of` | 真实实现 |
| `DelegatingDeque.reversed()`（`collection/DelegatingDeque.java:138-147`） | `MethodHandle` 为 `null` → `QueueUtils.reversedDeque(delegate)`，即 `ReversedDeque` 视图（`:143-144`） | 同样的回退（`Deque.reversed` 不存在） | 通过句柄调用 `Deque.reversed()`（`:146`） |
| `ReversedDeque`（`collection/ReversedDeque.java:40`） | 可移植的倒序视图；javadoc 说明它「based on JDK 21 `java.util.ReverseOrderDequeView`」（`:33-37`） | 同上 | 作为回退实现存在，而不是主路径 |
| `Collections.emptyIterator()` / `emptyEnumeration()` | JDK 7+ ——始终可用 | 同上 | 同上 |
| `SingletonDeque.hashCode` 使用的 `Objects.hashCode(Object)`（`:146-149`） | Java 8 可用 | 同上 | 同上 |

版本检测的工作方式：`MethodHandlesLookupUtils.findPublicStatic(List.class, "of", ...)` 在方法不存在时返回 `null`，
因此 `collection/Lists.java:46-101` 处的 `MethodHandle` 字段在 Java 8 上为 `null`，包私有的辅助方法
（`ofList0` … `ofListElements`，`:123-132` / `:156-165` / `:520-529`）会分支到 Java 8 回退实现。每个辅助方法还会捕获
`invokeExact` 抛出的 `Throwable` 并再次回退（`:127-131`），因此链接问题会降级而非崩溃——这与开发者索引中记载的库级策略
一致。

> [!NOTE]
> `ofList*` / `ofSet*` / `ofMap*` 辅助方法是**包私有**的，而不是 `private`。这是有意为之：
> `ListsTest` 传入一个 `null` 句柄以强制走 Java 8 分支（`assertEquals(emptyList(), ofList0(null))`，
> `collection/ListsTest.java:108`），并传入一个无关句柄以强制走 `catch (Throwable)` 分支
> （`collection/ListsTest.java:112-114`）。请不要在 `io.microsphere.collection` 之外的自己的代码里调用它们；
> 它们不属于 API。

---

## 6. 陷阱与已知限制

1. **`DelegatingIterator` 无法被特化。** 它的所有方法都是 `final`
   （`collection/DelegatingIterator.java:49-87`），包括返回 `Object` 的 `getDelegate()`。要增加行为，请再包一层，或者
   改用 `ReadOnlyIterator`；`EmptyIterator` 走的就是这条路线（`:58`、`:65-67`）。
2. **`singletonIterator(null)` 并不是空迭代器。** javadoc 声称「If the provided element is null, returns an iterator
   with no elements」（`collection/CollectionUtils.java:196`），但工厂实际调用 `new SingletonIterator<>(element)`
   （`:217-219`），而 `SingletonIterator` 不做任何 null 检查（`collection/SingletonIterator.java:45-47`；`hasNext`
   在 `:43` 初始为 `true`）。结果：`hasNext()` 为 `true`，`next()` 返回一次 `null`（`:54-61`）。
   `singletonEnumeration(null)` 同理（`:242-246` + `collection/SingletonEnumeration.java:58-74`），
   `SingletonDeque` 也同理——`size()` 仍然是 `1`（`:133-136`）。
3. **`unmodifiableIterator(null)` 不会返回空迭代器。** javadoc 说会（`collection/CollectionUtils.java:254`），
   但构造函数原样保存参数（`collection/UnmodifiableIterator.java:55-57`），而 `hasNext()` 会解引用它（`:59-62`）
   → 首次使用时抛 `NullPointerException`。对比 `IterableAdapter`，它确实回退到 `EmptyIterator.INSTANCE`
   （`collection/IterableAdapter.java:51-53`）；`EnumerationIteratorAdapter` 则回退到
   `Collections.emptyEnumeration()`（`:59-61`）。
4. **`new DelegatingQueue<>(null)` 抛的是 `IllegalArgumentException`，不是 `NullPointerException`。**
   守卫语句是 `assertNotNull(delegate, () -> "The 'delegate' argument must not be null!")`
   （`collection/DelegatingQueue.java:44-47`），而 `Assert.assertNotNull` 抛出 `IllegalArgumentException`
   （`util/Assert.java:149-153`）。如果你从 `Objects.requireNonNull` 迁移过来，请捕获
   `IllegalArgumentException`。
5. **`Empty*` 是「中性返回值」与「真实抛异常」的混合体——看表格，不要看类名。**
   `pollFirst`/`pollLast`/`peekFirst`/`peekLast` 返回 `null`，`remove*` 返回 `false`
   （`collection/EmptyDeque.java:89-127`），但 `offerFirst`/`offerLast` 抛 `UnsupportedOperationException`（`:79-87`），
   `getFirst`/`getLast` 抛 `NoSuchElementException`（`:99-107`）。在 `Queue` 视角上分界是
   `add(e)`/`offer(e)` → `UnsupportedOperationException`，`remove()`/`element()` → `NoSuchElementException`，
   `poll()`/`peek()` → `null`，`remove(Object)` → `false`——正是 `collection/EmptyQueueTest.java:50-78` 所断言的内容。
6. **`EmptyIterator.remove()` 抛 `IllegalStateException`。** 它自己的 javadoc 示例捕获的是
   `UnsupportedOperationException`（`collection/EmptyIterator.java:44-49`）；测试记录了事实——
   `assertThrows(IllegalStateException.class, emptyIterator::remove)`（`collection/EmptyIteratorTest.java:38-40`）——
   因为该调用转发给 `Collections.emptyIterator()`（`:65-67` → `:304`），而 JDK 实例抛的是 ISE。
   `UnmodifiableIterator`/`SingletonIterator` 同理：`IllegalStateException("Read-Only")`
   （`collection/ReadOnlyIterator.java:52-55`）。
7. **`ImmutableEntry.setValue` 是唯一会抛异常的 Entry 修改操作**，它抛
   `UnsupportedOperationException("ReadOnly Entry can't be modified")`（`collection/ImmutableEntry.java:53-56`）。
   `DefaultEntry` 完全可变（`:39`、`:69-74`），并且**不是** `Serializable`（`:35`），所以无法放入被序列化的 map 副本中。
8. **`PackageNameClassFilter` 在默认包上会抛异常。** `accept` 只防御了 `null` 类
   （`filter/PackageNameClassFilter.java:46-49`），随后调用 `filteredObject.getPackage().getName()`（`:50-51`）——
   对于 unnamed package 中的类，`getPackage()` 为 `null`，于是得到 `NullPointerException`。请优先使用基于 `String` 的
   `PackageNameClassNameFilter`，它的第一个分支就处理了空白输入（`:50-53`）。
9. **`ClassFileJarEntryFilter.INSTANCE.accept(null)` 抛 NPE**——`filter/ClassFileJarEntryFilter.java:43-46` 没有 null
   分支；并且它的构造函数是 `protected`（`:39-41`），所以不通过子类化就无法在包外创建第二个实例。
10. **`of*` 工厂的 null 许可取决于 JDK。** `Lists.ofList(null)` 在 Java 9+ 上抛 `NullPointerException`（调用的是真正的
    `List.of`，`collection/Lists.java:160-161`），而在 Java 8 上得到一个持有 `null` 的单元素列表，因为回退实现是
    `Collections.singletonList(e1)`（`:156-159`）。如果你需要在每个 JDK 上行为一致，请使用 `ListUtils.ofList(E...)`
    （`collection/ListUtils.java:189-196`）——它总是把 `Arrays.asList` 包进 `Collections.unmodifiableList`，
    因此允许 `null`。
11. **零个 filter 时 `FilterOperator` 接受一切，XOR 也一样。** 三个运算符都在 `length == 0` 时短路返回 `true`
    （`filter/FilterOperator.java:51-52`、`:68-69`、`:84-85`），并且 XOR 在折叠前把种子设为 `success = true`（`:86`）。
    所以 `FilterOperator.XOR.createFilter()` 返回的是接受一切的 filter，而不是拒绝一切的 filter——而
    `FilterUtils.filter(iterable, AND)` 在不传 filter 时会复制整个可迭代对象（`:82`）。
12. **`EmptyDeque` 是一个共享的、被强制转换的单例。** `QueueUtils.emptyQueue()` 与 `emptyDeque()` 都通过 unchecked
    cast 返回*同一个* `EMPTY_DEQUE` 实例（`collection/QueueUtils.java:52`、`:125-148`），因此
    `emptyQueue() == emptyDeque()`。它的类型是 `EmptyDeque<?>`（`collection/EmptyDeque.java:67`），
    `serialVersionUID` 为 `-1L`（`:65`）——不要把自己的协议依赖于一个稳定的 id。
13. **视图是活的，不是快照。** `UnmodifiableQueue`/`UnmodifiableDeque` 只持有普通引用
    （`collection/UnmodifiableQueue.java:69`、`collection/UnmodifiableDeque.java:62`），它们自己的 javadoc 也写明
    behaviour "is undefined if the underlying deque is modified directly while this view exists"
    （`collection/UnmodifiableDeque.java:33-36`）。`QueueUtils.ofQueue(e...)` 通过先复制来避免该问题（`:317-321`）。
14. **`Filter` 的组合不复用 `Predicate` 的组合器。** 从 `Predicate` 继承来的 `Filter.and/or/negate` 返回的是
    `Predicate` 而不是 `Filter`，因此你会失去有名字的 `accept` 方法；请改用
    `FilterOperator.createFilter(...)` 组合（`filter/FilterOperator.java:111-120`），它返回 `Filter`。

---

## 7. 参见

* [工具类、常量与文本（Utilities, constants and text）](util-and-constants.md) —— `ArrayUtils.length`、
  `ObjectUtils.defaultIfNull` 与 `Assert`，本页所有 null 容忍规则都依赖它们
* [语言抽象（Language abstractions）](language-abstractions.md) —— `DelegatingWrapper`，即 `Delegating*` 类实现的接口
* [反射、类型与 Bean（Reflection, types and beans）](reflection-and-beans.md) —— `collection` 中 `Map.Entry` 类型的主要消费者
* [I/O、NIO、扫描与文件过滤器（I/O, NIO, scanning and file filters）](io-and-scanning.md) ——
  `io.microsphere.io.filter`，真正存放 `File` 谓词的地方；`io.scanner.Scanner` 直接消费 `io.microsphere.filter.Filter`
* 面向开发者的深入解析：[集合与过滤器 — 开发者指南](../../../developer-guide/microsphere-java-core/zh/collections-and-filters.md)
* [English User Guide](collections-and-filters.md)
