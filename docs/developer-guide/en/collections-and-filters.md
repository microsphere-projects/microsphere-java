# Collections and Filters

> Read this page in: [中文](../zh/collections-and-filters.md) · [English](collections-and-filters.md)
> [← Handbook index](../README.md)

## At a glance

| Fact | Value |
|---|---|
| Module | `io.github.microsphere-projects:microsphere-java-core` |
| Packages | `io.microsphere.collection`, `io.microsphere.filter`, `io.microsphere.io.filter` |
| Naming rule | `of*` / `ofXxx` → unmodifiable result; `newXxx` → a real, mutable JDK instance |
| Null rule | checks and conversions (`isEmpty`, `size`, `first`, `toIterable`) accept `null`; `newXxx` factories mostly require a non-null source |
| Not present | no `DequeUtils`, no `Queues`, no `Entries` — deque factories live in `QueueUtils`, entry factories in `MapUtils` |

Everything here is static: the utility types are `public abstract class XxxUtils implements Utils` with a private
constructor. Filters (`Filter<T>`) are `Predicate<T>` sub-interfaces, so they work with Streams unchanged.

---

## 1. Choosing a factory

| You want | Call | What you get |
|---|---|---|
| A list written once, read many times | `ListUtils.of("a", "b", "c")` | unmodifiable `List`; `null` elements are allowed |
| A set built from an array or collection | `SetUtils.of(...)`, `SetUtils.ofSet(collection, others...)` | unmodifiable `Set`, duplicates dropped |
| A small map | `MapUtils.ofMap("k", 1, "k2", 2)` | unmodifiable `Map` in argument order |
| `List.of` / `Set.of` / `Map.of` semantics that also run on JDK 8 | `Lists.ofList(...)`, `Sets.ofSet(...)`, `Maps.ofMap(...)` | genuinely immutable on JDK 9+, unmodifiable copies below |
| A list you keep appending to | `ListUtils.newArrayList(16)` | mutable `ArrayList` |
| Exactly one element, no backing array | `CollectionUtils.singletonIterator(e)`, `QueueUtils.singletonDeque(e)` | read-only single-element types |
| An empty collection with no allocation | `CollectionUtils.emptyIterator()` / `emptyIterable()` / `emptyQueue()` / `emptyDeque()` | shared or JDK instances |
| A reversed view of a deque | `QueueUtils.reversedDeque(deque)` | `ReversedDeque` wrapper, no copy |
| To filter elements by a rule | `Streams.filterList(...)`, `FilterUtils.filter(...)` | new unmodifiable `List` |

---

## 2. Null-safe reads: `CollectionUtils`

```java
public static boolean isEmpty(Collection<?> collection)      // null -> true
public static boolean isNotEmpty(Collection<?> collection)
public static int size(Collection<?> collection)             // null -> 0
public static int size(Iterable<?> iterable)                 // null -> 0, otherwise it iterates

public static <E> Iterable<E> toIterable(Collection<E> collection)   // null -> EmptyIterable
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

public static <T> T first(Collection<T> collection)          // null / empty -> null
public static <T> T first(Iterable<T> iterable)
public static <T> T first(Iterator<T> iterator)
```

```java
List<String> names = null;
if (CollectionUtils.isNotEmpty(names)) {   // no NPE, no try/catch
    System.out.println(CollectionUtils.first(names));
}

int added = CollectionUtils.addAll(target, "x", "y");  // returns how many were really added
```

`CollectionUtils.equals(a, b)` treats `null` and empty as equal and ignores element order (size plus `containsAll`).
`addAll` returns `0` when the target collection is `null`, so it never throws.

> [!NOTE]
> `emptyIterator()` delegates to `Collections.emptyIterator()`. `emptyIterable()` returns the shared
> `EmptyIterable.INSTANCE`, and `emptyQueue()` / `emptyDeque()` both return `QueueUtils.EMPTY_DEQUE`
> (`EmptyDeque.INSTANCE`). Never mutate or store state in them.

> [!WARNING]
> `singletonIterator(null)` still produces an iterator with **one** element (`null`), and
> `unmodifiableIterator(null)` returns a wrapper whose `hasNext()` throws `NullPointerException`. The `@Nullable`
> annotations on those two methods do not match the implementation — pass a real element and a real iterator.

---

## 3. Immutable collections: `of` versus `new`

```java
public static <E> List<E> of(E... elements)                    // ListUtils
public static <E> List<E> ofList(E... elements)
public static <E> List<E> ofList(Iterable<E> iterable)         // null -> emptyList()
public static <E> List<E> ofList(Iterator<E> iterator)         // null -> emptyList()
public static <E> List<E> ofList(Enumeration<E> enumeration)   // null -> emptyList()
```

| Expression | Add | `set(i, e)` | `null` elements | Notes |
|---|---|---|---|---|
| `Arrays.asList("a", "b")` | throws | allowed | allowed | fixed size, but writable — a leaky "constant" |
| `ListUtils.of("a", "b")` | throws | throws | allowed | `unmodifiableList(asList(elements))` |
| `Lists.ofList("a", "b")` | throws | throws | JDK 9+: `NullPointerException`; JDK 8 fallback: allowed | dispatches to `List.of` through a cached `MethodHandle` |
| `ListUtils.newArrayList("a", "b")`-style `newXxx` | allowed | allowed | allowed | real mutable list |

Prefer `ListUtils.of` / `SetUtils.of` / `MapUtils.ofMap` for internal constants when you want null-tolerant,
unmodifiable data, and `Lists` / `Sets` / `Maps` when you want exactly the JDK 9 `of` contract on every JDK.

```java
List<String> tags = ListUtils.of("java", "sdk", null);   // fine, unmodifiable
Set<String> unique = SetUtils.ofSet(names, "extra");     // collection + varargs merged, deduplicated
Map<String, Integer> counts = MapUtils.ofMap("a", 1, "b", 2);
```

`Lists.ofList(...)`, `Sets.ofSet(...)`, `Maps.ofMap(...)` provide fixed-arity overloads (mirroring the JDK `of`
arities) plus a varargs form. If the JDK `of` method handle is missing or the invocation fails, they silently fall
back to `Collections.emptyList()` / `singletonList(...)` / `ListUtils.of(...)` — so the *immutability* guarantee
holds everywhere, but the *null rejection* only holds on JDK 9 and later.

---

## 4. Mutable collections: the `newXxx` factories

| Type | Factories |
|---|---|
| `ListUtils` | `newArrayList()`, `newArrayList(int)`, `newArrayList(Iterable \| Iterator \| Enumeration)`, `newLinkedList(...)`, `newCopyOnWriteArrayList()`, `newCopyOnWriteArrayList(Collection)`, `ofArrayList(E...)`, `ofLinkedList(E...)` |
| `SetUtils` | `newHashSet(...)`, `newLinkedHashSet(...)`, `newTreeSet()`, `newTreeSet(Comparator \| Collection \| SortedSet)`, `newFixedHashSet(int)`, `newFixedLinkedHashSet(int)`, `newCopyOnWriteArraySet(...)`, `newConcurrentSkipListSet(...)` |
| `MapUtils` | `newHashMap(...)`, `newLinkedHashMap(...)`, `newConcurrentHashMap(...)`, `newTreeMap(...)`, `newWeakHashMap(...)`, `newIdentityHashMap(...)`, `newConcurrentSkipListMap(...)`, `newFixedHashMap(int)`, `newFixedLinkedHashMap(int)` |
| `QueueUtils` | `newArrayDeque(...)`, `newPriorityQueue(...)`, `newConcurrentLinkedQueue(...)`, `newLinkedBlockingQueue(...)`, `newArrayBlockingQueue(int, ...)`, `newPriorityBlockingQueue(...)`, `newDelayQueue()`, `newSynchronousQueue()`, `newLinkedTransferQueue(...)` |

Two sizing details:

```java
Map<String, Object> map = MapUtils.newFixedHashMap(20);   // capacity 20, load factor MapUtils.FIXED_LOAD_FACTOR (1.00f)
Set<String> set = SetUtils.newFixedLinkedHashSet(20);
```

`newFixed*` set the load factor to `1.00f`, so filling exactly `size` elements does not resize the table — use them
when the element count is known in advance (class indexes, annotation metadata registries).

`ofArrayList(E...)` and `ofLinkedList(E...)` are the *mutable* array-to-list converters despite the `of` prefix;
both throw `IllegalArgumentException` when the array is `null` or empty.

> [!IMPORTANT]
> `ListUtils.newArrayList(Iterable)` / `newArrayList(Iterator)` and the matching `newLinkedList` overloads call
> `.iterator()` / `.hasNext()` on the argument immediately: a `null` source throws `NullPointerException`, even
> though their Javadoc promises an empty list. Wrap first — `ListUtils.newArrayList(CollectionUtils.toIterable(src))`
> — or use `ofList(...)` which is null-safe.

---

## 5. Map entries and flat/nested conversion

```java
public static <K, V> Map.Entry<K, V> ofEntry(K key, V value);        // DefaultEntry   — setValue works
public static <K, V> Map.Entry<K, V> immutableEntry(K key, V value); // ImmutableEntry — setValue throws
public static <K, V, E> Map<K, V> toFixedMap(Collection<E> values, Function<E, Map.Entry<K, V>> entryMapper);
public static <K, V> Map<K, V> shallowCloneMap(Map<K, V> source);
public static Map<String, Object> flattenMap(Map<String, Object> map);
public static Map<String, Object> nestedMap(Map<String, Object> map);
```

`MapUtils.of(k, v)` up to five pairs, `MapUtils.of(Object... values)`, `MapUtils.ofMap(Object... keyValuePairs)` and
`MapUtils.of(Map.Entry...)` all produce unmodifiable maps. `of(Object... values)` / `ofMap(Object...)` read the
arguments two at a time, so an **odd** number of arguments ends in `ArrayIndexOutOfBoundsException`.

`ImmutableEntry.setValue(...)` throws `UnsupportedOperationException("ReadOnly Entry can't be modified")`.
`shallowCloneMap` picks a JDK map type from the source (`LinkedHashMap`, `TreeMap`, `ConcurrentSkipListMap`,
`ConcurrentHashMap`, `IdentityHashMap`, otherwise `HashMap`) but does not copy nested values.

```java
Map<String, Object> flat = MapUtils.flattenMap(nested);
// {"a": {"b": {"c": 1}}}  ->  {"a.b.c": "1"}
Map<String, Object> back = MapUtils.nestedMap(flat);
```

> [!WARNING]
> `nestedMap` renders every leaf value with `valueOf(..., String)`, so the nested result holds `String` values even
> when the input held numbers. `flattenMap` (which is `PropertiesUtils.flatProperties`) only descends into `Map`
> values and keeps `String` values; for an empty input it returns the argument itself instead of a copy.

---

## 6. Single-element, empty, unmodifiable and delegating collections

| Type | Created by | Contract |
|---|---|---|
| `SingletonIterator<E>` | `CollectionUtils.singletonIterator(e)` | extends `ReadOnlyIterator`; one `next()`, then `NoSuchElementException` |
| `SingletonEnumeration<E>` | `CollectionUtils.singletonEnumeration(e)` | same, for `Enumeration` |
| `SingletonDeque<E>` | `QueueUtils.singletonQueue(e)`, `QueueUtils.singletonDeque(e)` | `size()` is always `1`; `peekFirst`/`peekLast`/`getFirst`/`getLast` return the element; **every** `offer*`, `poll*`, `remove*Occurrence` throws `UnsupportedOperationException` |
| `EmptyDeque` | `QueueUtils.EMPTY_DEQUE`, `QueueUtils.emptyQueue()`, `emptyDeque()` | shared instance, mutators throw |
| `EmptyIterator` / `EmptyIterable` | `EmptyIterator.INSTANCE`, `EmptyIterable.INSTANCE` | internal; prefer the `CollectionUtils` accessors |
| `UnmodifiableIterator<E>` | `CollectionUtils.unmodifiableIterator(iterator)` | `remove()` inherited from `ReadOnlyIterator` throws `IllegalStateException("Read-Only")` |
| `UnmodifiableQueue<E>` / `UnmodifiableDeque<E>` | `QueueUtils.unmodifiableQueue(q)` / `unmodifiableDeque(d)` (constructors are package-private) | read methods pass through, all mutators throw `UnsupportedOperationException` |
| `DelegatingIterator<E>` / `DelegatingQueue<E>` / `DelegatingDeque<E>` | `new DelegatingQueue<>(delegate)` etc. | pure pass-through; `getDelegate()` returns the wrapped object; `DelegatingIterator` methods are `final` |
| `ReversedDeque<E>` | `QueueUtils.reversedDeque(deque)`, `ReversedDeque.of(deque)` | first/last swapped, iteration reversed; `of` is idempotent — an already-reversed deque is returned as is |
| `IterableAdapter<T>` | `CollectionUtils.toIterable(iterator)` | exposes an `Iterator` as an `Iterable` (a `null` iterator becomes empty) |
| `EnumerationIteratorAdapter<E>` | `CollectionUtils.toIterator(enumeration)` | package-private constructor; `null` enumeration becomes empty |

```java
Deque<String> single = QueueUtils.singletonDeque("only");
single.peekFirst();          // "only"
single.pollLast();           // UnsupportedOperationException — a singleton deque cannot shrink
single.size();               // always 1

Queue<String> view = QueueUtils.unmodifiableQueue(new ArrayDeque<>(ListUtils.of("a")));
```

> [!TIP]
> The `Delegating*` types implement `DelegatingWrapper`, so a caller can always recover the real collection with
> `Wrapper.tryUnwrap(collection, ArrayDeque.class)` — see
> [Language Abstractions](language-abstractions.md#2-wrapper-and-delegatingwrapper).

---

## 7. `ArrayStack`: a `List` that is also a stack

`ArrayStack<E> extends ArrayList<E>`, so it keeps index access and random access while adding LIFO operations.

```java
public ArrayStack()
public ArrayStack(int initialCapacity)
public E push(E item)                      // returns item
public E pop()                             // EmptyStackException when empty
public E peek()                            // EmptyStackException when empty
public boolean empty()
public int search(Object o)                // 1-based distance from the top, -1 when absent
```

```java
ArrayStack<String> stack = new ArrayStack<>();
stack.push("a");
stack.push("b");
stack.peek();      // "b"
stack.pop();       // "b"
stack.get(0);      // "a" — still a List, add/addAll/removeAll from ArrayList all work
stack.search("a"); // 1: after the pop, "a" is the top element and the distance starts at 1
```

`peek()` and `pop()` throw `java.util.EmptyStackException`, not `null`; check `empty()` first.

---

## 8. `io.microsphere.filter`: predicates with an `accept` method

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

A `Filter` **is** a `Predicate`; only the method you implement differs, so `filter.stream().filter(myFilter)` works.

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
// FilterUtils.filter(...) returns an unmodifiable List and iterates the source eagerly.
```

> [!NOTE]
> With an empty filter array every operator returns `true` — including `XOR`, which is `true` when an even number of
> filters match. The operators do not short-circuit: each `accept` call runs all filters.

Ready-made filters:

| Type | How you obtain it | Selects |
|---|---|---|
| `ClassFilter` (`extends Filter<Class<?>>`) | implement it | classes |
| `JarEntryFilter` (`extends Filter<JarEntry>`) | implement it | JAR entries |
| `PackageNameClassFilter(String packageName, boolean includedSubPackages)` | `new` | classes in a package |
| `PackageNameClassNameFilter(String packageName, boolean includedSubPackages)` | `new` | class **names** as `Filter<String>` |
| `TrueClassFilter.INSTANCE` | constant | everything |
| `ClassFileJarEntryFilter.INSTANCE` | constant | `.class` entries |

`TrueClassFilter`, `ClassFileJarEntryFilter` and `PackageNameClassNameFilter` are the pieces the classpath and JAR
scanners use internally — see [Class Loading and Artifacts](classloading-and-artifacts.md).

---

## 9. File filters (`io.microsphere.io.filter`)

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

| Filter | Instance | Behavior |
|---|---|---|
| `TrueFileFilter.INSTANCE` | constant | accepts everything |
| `DirectoryFileFilter.INSTANCE` | constant | `file != null && file.isDirectory()` |
| `NameFileFilter(String name)` | constructor | exact file **name** match, case-sensitive by default; `NameFileFilter(name, false)` ignores case |
| `FileExtensionFilter.of(String extension)` | static factory (constructor is `protected`) | matches the extension; `"a/b.txt"` and `"b.txt"` both mean `txt` |

Because `IOFileFilter` implements both `FileFilter` and `FilenameFilter`, one instance serves `File.listFiles(...)`
and directory scanning alike — that is what the file watchers and scanners consume
([I/O, Files and Watching](io-and-file-watch.md)).

```java
IOFileFilter props = FileExtensionFilter.of(".properties");
File[] files = dir.listFiles(props);                 // as FileFilter
String[] names = dir.list((d, n) -> props.accept(new File(d, n)));  // as FilenameFilter
```

> [!WARNING]
> `NameFileFilter` and the `FileExtensionFilter` constructor dereference their argument — `null` throws
> `NullPointerException`. `FileExtensionFilter` rejects directories and files with no extension, and it compares
> case-insensitively on Windows and case-sensitively on other platforms.

---

## See also

* [Core Utilities](core-utilities.md) — `ArrayUtils`, `StringUtils`, `Assert`
* [Language Abstractions](language-abstractions.md) — `Wrapper`, `Prioritized`, `Predicates`, `Streams`
* [I/O, Files and Watching](io-and-file-watch.md) — the scanners that consume these filters
* [Class Loading and Artifacts](classloading-and-artifacts.md) — `JarEntryFilter` in JAR introspection

[← Handbook index](../README.md) · [Previous: Core Utilities](core-utilities.md) · [Next: Language Abstractions →](language-abstractions.md)
