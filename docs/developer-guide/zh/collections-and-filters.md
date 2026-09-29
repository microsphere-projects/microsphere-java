# 集合与过滤器

> 语言版本：[中文](collections-and-filters.md) · [English](../en/collections-and-filters.md)
> [← 手册目录](../README.md)

## 一览

| 项目 | 取值 |
|---|---|
| 所属模块 | `io.github.microsphere-projects:microsphere-java-core` |
| 包 | `io.microsphere.collection`、`io.microsphere.filter`、`io.microsphere.io.filter` |
| 命名规律 | `of*` / `ofXxx` 返回不可修改结果；`newXxx` 返回真正可变的 JDK 实例 |
| null 策略 | 判断与转换方法（`isEmpty`、`size`、`first`、`toIterable`）接受 `null`；多数 `newXxx` 工厂要求源非空 |
| 不存在的类 | 没有 `DequeUtils`、`Queues`、`Entries`：队列/双端队列工厂在 `QueueUtils`，Entry 工厂在 `MapUtils` |

这里全部是静态方法：工具类都是 `public abstract class XxxUtils implements Utils` 且构造器为 private。
过滤器 `Filter<T>` 是 `Predicate<T>` 的子接口，因此可以直接用于 Stream。

---

## 1. 怎么选工厂

| 你想做什么 | 调用 | 得到什么 |
|---|---|---|
| 一次写入、多次读取的列表 | `ListUtils.of("a", "b", "c")` | 不可修改 `List`，允许 `null` 元素 |
| 由数组或集合构造的 Set | `SetUtils.of(...)`、`SetUtils.ofSet(collection, others...)` | 不可修改 `Set`，自动去重 |
| 少量键值对的 Map | `MapUtils.ofMap("k", 1, "k2", 2)` | 按参数顺序的不可修改 `Map` |
| 想要 JDK 9 `List.of` 语义，又要跑在 JDK 8 | `Lists.ofList(...)`、`Sets.ofSet(...)`、`Maps.ofMap(...)` | JDK 9+ 真不可变，低版本退化为不可修改副本 |
| 还要继续追加元素 | `ListUtils.newArrayList(16)` | 可变 `ArrayList` |
| 只有一个元素，且不想分配底层数组 | `CollectionUtils.singletonIterator(e)`、`QueueUtils.singletonDeque(e)` | 只读单元素集合 |
| 空集合，且不想分配对象 | `CollectionUtils.emptyIterator()` / `emptyIterable()` / `emptyQueue()` / `emptyDeque()` | 共享实例或 JDK 实例 |
| 双端队列的反序视图 | `QueueUtils.reversedDeque(deque)` | `ReversedDeque` 包装，不复制 |
| 按条件筛元素 | `Streams.filterList(...)`、`FilterUtils.filter(...)` | 新的不可修改 `List` |

---

## 2. 空安全读取：`CollectionUtils`

```java
public static boolean isEmpty(Collection<?> collection)      // null 返回 true
public static boolean isNotEmpty(Collection<?> collection)
public static int size(Collection<?> collection)             // null 返回 0
public static int size(Iterable<?> iterable)                 // null 返回 0，其他情况会遍历

public static <E> Iterable<E> toIterable(Collection<E> collection)   // null 返回 EmptyIterable
public static <E> Iterable<E> toIterable(Iterator<E> iterator)
public static <E> Iterable<E> toIterable(Enumeration<E> enumeration)
public static <E> Iterator<E> toIterator(Enumeration<E> enumeration)

public static <E> Iterable<E> singletonIterable(E element)
public static <E> Iterator<E> singletonIterator(E element)
public static <E> Enumeration<E> singletonEnumeration(E element)

public static <E> Iterator<E> unmodifiableIterator(Iterator<E> iterator)
public static <E> Iterator<E> emptyIterator()
public static <E> Iterable<E> emptyIterable()
public static <E> Queue<E> emptyQueue()
public static <E> Deque<E> emptyDeque()

public static boolean equals(Collection<?> one, Collection<?> another)
public static <T> int addAll(Collection<T> collection, T... elements)
public static <T> int addAll(Collection<T> collection, Iterable<T> elements)

public static <T> T first(Collection<T> collection)          // null 或空返回 null
public static <T> T first(Iterable<T> iterable)
public static <T> T first(Iterator<T> iterator)
```

```java
List<String> names = null;
if (CollectionUtils.isNotEmpty(names)) {   // 不会 NPE，也不需要 try/catch
    System.out.println(CollectionUtils.first(names));
}

int added = CollectionUtils.addAll(target, "x", "y");  // 返回真正添加成功的数量
```

`CollectionUtils.equals(a, b)` 把 `null` 与空集合视为相等，且不关心元素顺序（比较大小加 `containsAll`）。
`addAll` 在目标集合为 `null` 时返回 `0`，因此不会抛异常。

> [!NOTE]
> `emptyIterator()` 直接委托 `Collections.emptyIterator()`；`emptyIterable()` 返回共享的
> `EmptyIterable.INSTANCE`；`emptyQueue()` 与 `emptyDeque()` 都返回 `QueueUtils.EMPTY_DEQUE`
>（即 `EmptyDeque.INSTANCE`）。不要向它们写入数据。

> [!WARNING]
> `singletonIterator(null)` 得到的迭代器仍然会产出**一个**元素（即 `null`）；
> `unmodifiableIterator(null)` 返回的包装器在调用 `hasNext()` 时会抛 `NullPointerException`。
> 这两个方法上的 `@Nullable` 注解与实现并不一致，请传入真实元素和真实迭代器。

---

## 3. 不可变集合：`of` 与 `new` 的区别

```java
public static <E> List<E> of(E... elements)                    // ListUtils
public static <E> List<E> ofList(E... elements)
public static <E> List<E> ofList(Iterable<E> iterable)         // null 返回 emptyList()
public static <E> List<E> ofList(Iterator<E> iterator)         // null 返回 emptyList()
public static <E> List<E> ofList(Enumeration<E> enumeration)   // null 返回 emptyList()
```

| 表达式 | `add` | `set(i, e)` | `null` 元素 | 说明 |
|---|---|---|---|---|
| `Arrays.asList("a", "b")` | 抛异常 | 允许 | 允许 | 定长但可写，作为“常量”会泄漏 |
| `ListUtils.of("a", "b")` | 抛异常 | 抛异常 | 允许 | 即 `unmodifiableList(asList(elements))` |
| `Lists.ofList("a", "b")` | 抛异常 | 抛异常 | JDK 9+ 抛 `NullPointerException`；JDK 8 退化路径允许 | 通过缓存的 `MethodHandle` 调用 `List.of` |
| `newXxx` 系列，如 `ListUtils.newArrayList(...)` | 允许 | 允许 | 允许 | 真正可变的列表 |

需要“不可修改且容忍 null”的内部常量时，优先用 `ListUtils.of` / `SetUtils.of` / `MapUtils.ofMap`；
需要在任意 JDK 上都严格等同于 JDK 9 `of` 契约时，用 `Lists` / `Sets` / `Maps`。

```java
List<String> tags = ListUtils.of("java", "sdk", null);   // 允许，且不可修改
Set<String> unique = SetUtils.ofSet(names, "extra");     // 集合并入变参，自动去重
Map<String, Integer> counts = MapUtils.ofMap("a", 1, "b", 2);
```

`Lists.ofList(...)`、`Sets.ofSet(...)`、`Maps.ofMap(...)` 提供与 JDK `of` 对应的固定参数个数重载以及变参形式。
若拿不到 JDK 的 `of` method handle，或调用失败，它们会静默退化为 `Collections.emptyList()` /
`singletonList(...)` / `ListUtils.of(...)`：不可修改这一保证在所有 JDK 上都成立，
但“拒绝 null”只在 JDK 9 及以后成立。

---

## 4. 可变集合：`newXxx` 工厂

| 类型 | 工厂方法 |
|---|---|
| `ListUtils` | `newArrayList()`、`newArrayList(int)`、`newArrayList(Iterable \| Iterator \| Enumeration)`、`newLinkedList(...)`、`newCopyOnWriteArrayList()`、`newCopyOnWriteArrayList(Collection)`、`ofArrayList(E...)`、`ofLinkedList(E...)` |
| `SetUtils` | `newHashSet(...)`、`newLinkedHashSet(...)`、`newTreeSet()`、`newTreeSet(Comparator \| Collection \| SortedSet)`、`newFixedHashSet(int)`、`newFixedLinkedHashSet(int)`、`newCopyOnWriteArraySet(...)`、`newConcurrentSkipListSet(...)` |
| `MapUtils` | `newHashMap(...)`、`newLinkedHashMap(...)`、`newConcurrentHashMap(...)`、`newTreeMap(...)`、`newWeakHashMap(...)`、`newIdentityHashMap(...)`、`newConcurrentSkipListMap(...)`、`newFixedHashMap(int)`、`newFixedLinkedHashMap(int)` |
| `QueueUtils` | `newArrayDeque(...)`、`newPriorityQueue(...)`、`newConcurrentLinkedQueue(...)`、`newLinkedBlockingQueue(...)`、`newArrayBlockingQueue(int, ...)`、`newPriorityBlockingQueue(...)`、`newDelayQueue()`、`newSynchronousQueue()`、`newLinkedTransferQueue(...)` |

关于容量的两点：

```java
Map<String, Object> map = MapUtils.newFixedHashMap(20);   // 容量 20，负载因子 MapUtils.FIXED_LOAD_FACTOR（1.00f）
Set<String> set = SetUtils.newFixedLinkedHashSet(20);
```

`newFixed*` 把负载因子固定为 `1.00f`，因此装入正好 `size` 个元素不会触发扩容 ——
元素数量已知（类索引、注解元数据注册表）时很实用。

`ofArrayList(E...)` 与 `ofLinkedList(E...)` 虽然以 `of` 开头，返回的却是**可变**列表；
数组为 `null` 或空时两者都抛 `IllegalArgumentException`。

> [!IMPORTANT]
> `ListUtils.newArrayList(Iterable)` / `newArrayList(Iterator)` 以及对应的 `newLinkedList` 重载会立即对参数调用
> `.iterator()` / `.hasNext()`：传入 `null` 会抛 `NullPointerException`，尽管其 Javadoc 承诺返回空列表。
> 请先包一层 —— `ListUtils.newArrayList(CollectionUtils.toIterable(src))` —— 或改用空安全的 `ofList(...)`。

---

## 5. Map Entry 与扁平/嵌套转换

```java
public static <K, V> Map.Entry<K, V> ofEntry(K key, V value);        // DefaultEntry   —— 可以 setValue
public static <K, V> Map.Entry<K, V> immutableEntry(K key, V value); // ImmutableEntry —— setValue 抛异常
public static <K, V, E> Map<K, V> toFixedMap(Collection<E> values, Function<E, Map.Entry<K, V>> entryMapper);
public static <K, V> Map<K, V> shallowCloneMap(Map<K, V> source);
public static Map<String, Object> flattenMap(Map<String, Object> map);
public static Map<String, Object> nestedMap(Map<String, Object> map);
```

`MapUtils.of(k, v)`（最多五对）、`MapUtils.of(Object... values)`、`MapUtils.ofMap(Object... keyValuePairs)`、
`MapUtils.of(Map.Entry...)` 返回的都是不可修改 Map。`of(Object...)` / `ofMap(Object...)` 每次取两个参数，
因此参数个数为**奇数**时最终会抛 `ArrayIndexOutOfBoundsException`。

`ImmutableEntry.setValue(...)` 抛 `UnsupportedOperationException("ReadOnly Entry can't be modified")`。
`shallowCloneMap` 会依据源类型挑选 JDK Map 实现（`LinkedHashMap`、`TreeMap`、`ConcurrentSkipListMap`、
`ConcurrentHashMap`、`IdentityHashMap`，其余为 `HashMap`），但不会深拷贝值。

```java
Map<String, Object> flat = MapUtils.flattenMap(nested);
// {"a": {"b": {"c": 1}}}  ->  {"a.b.c": "1"}
Map<String, Object> back = MapUtils.nestedMap(flat);
```

> [!WARNING]
> `nestedMap` 对每个叶子值调用 `valueOf(..., String)`，所以即使原始值是数字，结果里也全是 `String`。
> `flattenMap`（即 `PropertiesUtils.flatProperties`）只递归 `Map` 类型的值、只保留 `String` 值，
> 并且入参为空时直接返回入参本身而不复制。

---

## 6. 单元素、空、不可修改与委托集合

| 类型 | 获取方式 | 约定 |
|---|---|---|
| `SingletonIterator<E>` | `CollectionUtils.singletonIterator(e)` | 继承 `ReadOnlyIterator`；一次 `next()` 之后抛 `NoSuchElementException` |
| `SingletonEnumeration<E>` | `CollectionUtils.singletonEnumeration(e)` | 同上，面向 `Enumeration` |
| `SingletonDeque<E>` | `QueueUtils.singletonQueue(e)`、`QueueUtils.singletonDeque(e)` | `size()` 恒为 `1`；`peekFirst`/`peekLast`/`getFirst`/`getLast` 返回该元素；**所有** `offer*`、`poll*`、`remove*Occurrence` 都抛 `UnsupportedOperationException` |
| `EmptyDeque` | `QueueUtils.EMPTY_DEQUE`、`QueueUtils.emptyQueue()`、`emptyDeque()` | 共享实例，修改方法抛异常 |
| `EmptyIterator` / `EmptyIterable` | `EmptyIterator.INSTANCE`、`EmptyIterable.INSTANCE` | 内部使用，建议走 `CollectionUtils` 的入口 |
| `UnmodifiableIterator<E>` | `CollectionUtils.unmodifiableIterator(iterator)` | `remove()` 继承自 `ReadOnlyIterator`，抛 `IllegalStateException("Read-Only")` |
| `UnmodifiableQueue<E>` / `UnmodifiableDeque<E>` | `QueueUtils.unmodifiableQueue(q)` / `unmodifiableDeque(d)`（构造器为包级私有） | 读取方法透传，所有修改方法抛 `UnsupportedOperationException` |
| `DelegatingIterator<E>` / `DelegatingQueue<E>` / `DelegatingDeque<E>` | `new DelegatingQueue<>(delegate)` 等 | 完全透传；`getDelegate()` 取回被包装对象；`DelegatingIterator` 的方法均为 `final` |
| `ReversedDeque<E>` | `QueueUtils.reversedDeque(deque)`、`ReversedDeque.of(deque)` | 首尾互换、迭代顺序反转；`of` 幂等 —— 已是反序视图时原样返回 |
| `IterableAdapter<T>` | `CollectionUtils.toIterable(iterator)` | 把 `Iterator` 当作 `Iterable` 暴露（`null` 迭代器变为空） |
| `EnumerationIteratorAdapter<E>` | `CollectionUtils.toIterator(enumeration)` | 构造器包级私有；`null` 枚举变为空 |

```java
Deque<String> single = QueueUtils.singletonDeque("only");
single.peekFirst();          // "only"
single.pollLast();           // UnsupportedOperationException —— 单元素队列不会变空
single.size();               // 恒为 1

Queue<String> view = QueueUtils.unmodifiableQueue(new ArrayDeque<>(ListUtils.of("a")));
```

> [!TIP]
> `Delegating*` 系列都实现了 `DelegatingWrapper`，调用方总能取回真实集合：
> `Wrapper.tryUnwrap(collection, ArrayDeque.class)` —— 参见
> [语言抽象](language-abstractions.md#2-wrapper-与-delegatingwrapper)。

---

## 7. `ArrayStack`：既是 `List` 又是栈

`ArrayStack<E> extends ArrayList<E>`，在保留下标访问与随机访问的同时补上 LIFO 操作。

```java
public ArrayStack()
public ArrayStack(int initialCapacity)
public E push(E item)                      // 返回 item 本身
public E pop()                             // 空栈抛 EmptyStackException
public E peek()                            // 空栈抛 EmptyStackException
public boolean empty()
public int search(Object o)                // 到栈顶的 1 起步距离，不存在时返回 -1
```

```java
ArrayStack<String> stack = new ArrayStack<>();
stack.push("a");
stack.push("b");
stack.peek();      // "b"
stack.pop();       // "b"
stack.get(0);      // "a" —— 它仍是 List，add/addAll/removeAll 都能用
stack.search("a"); // 1：弹出 "b" 后 "a" 位于栈顶，距离从 1 起算
```

`peek()` 与 `pop()` 抛的是 `java.util.EmptyStackException` 而不是返回 `null`，调用前先判断 `empty()`。

---

## 8. `io.microsphere.filter`：以 `accept` 为抽象方法的断言

```java
@FunctionalInterface
public interface Filter<T> extends Predicate<T> {

    boolean accept(T filteredObject);

    @Override
    default boolean test(T t) {
        return accept(t);
    }
}
```

`Filter` **就是** `Predicate`，只是你实现的方法名不同，因此 `stream().filter(myFilter)` 照常可用。

```java
public enum FilterOperator { AND, OR, XOR }

public abstract <T> boolean accept(T filteredObject, Filter<T>... filters);
public final <T> Filter<T> createFilter(Filter<T>... filters);

public abstract class FilterUtils implements Utils {
    public static <E> List<E> filter(Iterable<E> iterable, Filter<E> filter)
    public static <E> List<E> filter(Iterable<E> iterable, FilterOperator operator, Filter<E>... filters)
}
```

```java
List<Class> found = FilterUtils.filter(scannedClasses, FilterOperator.AND,
        type -> type.isAnnotationPresent(Service.class),
        type -> !Modifier.isAbstract(type.getModifiers()));
// FilterUtils.filter(...) 返回不可修改 List，并且会立即遍历数据源
```

> [!NOTE]
> 过滤器数组为空时，三种运算符都返回 `true` —— 包括 `XOR`（命中偶数个过滤器时同样为 `true`）。
> 运算符不会短路，每次 `accept` 都会跑完全部过滤器。

现成的过滤器：

| 类型 | 获取方式 | 筛选目标 |
|---|---|---|
| `ClassFilter`（`extends Filter<Class<?>>`） | 自行实现 | 类 |
| `JarEntryFilter`（`extends Filter<JarEntry>`） | 自行实现 | JAR 条目 |
| `PackageNameClassFilter(String packageName, boolean includedSubPackages)` | `new` | 某个包下的类 |
| `PackageNameClassNameFilter(String packageName, boolean includedSubPackages)` | `new` | 类**名**，即 `Filter<String>` |
| `TrueClassFilter.INSTANCE` | 常量 | 全部通过 |
| `ClassFileJarEntryFilter.INSTANCE` | 常量 | `.class` 条目 |

`TrueClassFilter`、`ClassFileJarEntryFilter`、`PackageNameClassNameFilter` 是类路径与 JAR 扫描器内部使用的零件，
详见 [类加载与构件](classloading-and-artifacts.md)。

---

## 9. 文件过滤器（`io.microsphere.io.filter`）

```java
@FunctionalInterface
public interface IOFileFilter extends FileFilter, FilenameFilter {

    @Override
    boolean accept(File file);

    @Override
    default boolean accept(File dir, String name) {
        return accept(new File(dir, name));
    }
}
```

| 过滤器 | 实例 | 行为 |
|---|---|---|
| `TrueFileFilter.INSTANCE` | 常量 | 全部接受 |
| `DirectoryFileFilter.INSTANCE` | 常量 | `file != null && file.isDirectory()` |
| `NameFileFilter(String name)` | 构造器 | 按文件**名**精确匹配，默认区分大小写；`NameFileFilter(name, false)` 忽略大小写 |
| `FileExtensionFilter.of(String extension)` | 静态工厂（构造器为 `protected`） | 按扩展名匹配；`"a/b.txt"` 与 `"b.txt"` 都表示 `txt` |

`IOFileFilter` 同时实现 `FileFilter` 与 `FilenameFilter`，所以同一个实例既能传给 `File.listFiles(...)`，
也能用于目录扫描 —— 文件监听与扫描器消费的正是它
（[I/O、文件与目录监听](io-and-file-watch.md)）。

```java
IOFileFilter props = FileExtensionFilter.of(".properties");
File[] files = dir.listFiles(props);                 // 作为 FileFilter 使用
String[] names = dir.list((d, n) -> props.accept(new File(d, n)));  // 作为 FilenameFilter 使用
```

> [!WARNING]
> `NameFileFilter` 与 `FileExtensionFilter` 的构造过程都会直接使用入参，传 `null` 会抛
> `NullPointerException`。`FileExtensionFilter` 会排除目录与没有扩展名的文件，并且在 Windows 上忽略大小写、
> 其他平台上区分大小写。

---

## 参见

* [核心工具](core-utilities.md) —— `ArrayUtils`、`StringUtils`、`Assert`
* [语言抽象](language-abstractions.md) —— `Wrapper`、`Prioritized`、`Predicates`、`Streams`
* [I/O、文件与目录监听](io-and-file-watch.md) —— 消费这些过滤器的扫描器
* [类加载与构件](classloading-and-artifacts.md) —— JAR 内省中的 `JarEntryFilter`

[← 手册目录](../README.md) · [上一篇：核心工具](core-utilities.md) · [下一篇：语言抽象 →](language-abstractions.md)
