# Collections and Filters

**Module**: `io.github.microsphere-projects:microsphere-java-core`
**Packages**: `io.microsphere.collection`, `io.microsphere.filter`

---

## 1. What is in the package

| Kind | Types |
|---|---|
| Static utilities (`abstract class X implements Utils`) | `CollectionUtils`, `ListUtils`, `SetUtils`, `MapUtils`, `QueueUtils`, `EnumerationUtils`, `PropertiesUtils`, `Iterators` |
| Immutable-factory facades | `Lists`, `Maps`, `Sets` (JDK-9-style `of` semantics that also work on JDK 8) |
| Concrete collections | `ArrayStack`, `SingletonDeque`, `SingletonIterator`, `SingletonEnumeration`, `DefaultEntry`, `ImmutableEntry` |
| Read-only wrappers | `ReadOnlyIterator`, `UnmodifiableIterator`, `UnmodifiableQueue`, `UnmodifiableDeque`, `EmptyDeque`, `EmptyIterator`, `EmptyIterable` |
| Delegating wrappers | `DelegatingIterator`, `DelegatingQueue`, `DelegatingDeque`, `ReversedDeque` |
| Support | `AbstractDeque`, `IterableAdapter`, `EnumerationIteratorAdapter` |

> [!NOTE]
> There is **no** `DequeUtils`, no `Queues` and no `Entries` class. Deque and Queue factories both live in
> `QueueUtils`; entry factories live in `MapUtils` (`ofEntry`, `immutableEntry`).

---

## 2. `CollectionUtils`

```java
public static boolean isEmpty(Collection<?> collection)
public static boolean isNotEmpty(Collection<?> collection)
public static int size(Collection<?> collection)
public static int size(Iterable<?> iterable)

public static <E> Iterable<E> toIterable(Collection<E> collection)
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

public static <T> T first(Collection<T> collection)
public static <T> T first(Iterable<T> iterable)
public static <T> T first(Iterator<T> iterator)
```

Every method is null-safe; `isEmpty(null)` is `true`, `size(null)` is `0`, `first(null)` is `null`.

```java
List<String> names = null;
if (CollectionUtils.isEmpty(names)) { /* no NPE */ }

CollectionUtils.first(asList("a", "b"));   // "a"
CollectionUtils.addAll(target, "x", "y");  // returns 2
```

`emptyIterator()` / `emptyIterable()` / `emptyQueue()` / `emptyDeque()` return shared singletons
(`EmptyIterator.INSTANCE`, `EmptyIterable.INSTANCE`, `EmptyDeque.INSTANCE`) — never mutate them.

---

## 3. `ListUtils`, `SetUtils`, `MapUtils`, `QueueUtils`, `EnumerationUtils`

### `ListUtils`

```java
public static boolean isList(Object obj | Class<?> type)
public static <E> E first(List<E> list)
public static <E> E last(List<E> list)
public static <E> List<E> of(E... elements)
public static <E> List<E> ofList(E... elements)
public static <E> List<E> ofList(Iterable<E> iterable)
public static <E> List<E> ofList(Iterator<E> iterator)
public static <E> List<E> ofList(Enumeration<E> enumeration)
public static <E> ArrayList<E> newArrayList()
public static <E> ArrayList<E> newArrayList(int capacity)
public static <E> ArrayList<E> newArrayList(Iterable<E> | Iterator<E> | Enumeration<E> source)
public static <E> LinkedList<E> newLinkedList(...)
public static <E> List<E> ofArrayList(E... elements)
public static <E> List<E> ofLinkedList(E... elements)
public static <E> CopyOnWriteArrayList<E> newCopyOnWriteArrayList(...)
public static <T> void forEach(List<T> list, Consumer<T> consumer)
public static <T> void forEach(List<T> list, BiConsumer<Integer, T> consumer)   // index-aware
public static <T> boolean addIfAbsent(List<T> list, T element)
```

### `SetUtils`

```java
public static boolean isSet(Object obj | Class<?> type)
public static <E> Set<E> of(E... elements)
public static <E> Set<E> ofSet(E... elements)
public static <E> Set<E> ofSet(Enumeration<E> | Iterable<E> | Collection<E> source, E... others)
public static <E> HashSet<E> newHashSet(...)
public static <E> LinkedHashSet<E> newLinkedHashSet(...)
public static <E> TreeSet<E> newTreeSet(...)
public static <E> Set<E> newFixedHashSet(int capacity)
public static <E> Set<E> newFixedLinkedHashSet(int capacity)
public static <E> Set<E> newCopyOnWriteArraySet(...)
public static <E> Set<E> newConcurrentSkipListSet(...)
```

### `MapUtils`

```java
public static final float MIN_LOAD_FACTOR;
public static final float FIXED_LOAD_FACTOR = 1.00f;   // used by the newFixed* factories

public static boolean isMap(Object obj | Class<?> type)
public static boolean isEmpty(Map<?, ?> map);  public static boolean isNotEmpty(Map<?, ?> map)
public static int size(Map<?, ?> map)

// Immutable entries
public static <K, V> Map<K, V> of(K k1, V v1) ... of(k5, v5)      // 1..5 pairs
public static Map of(Object... kvPairs)                            // raw type
public static <K, V> Map<K, V> of(Map.Entry<K, V>... entries)
public static <K, V> Map<K, V> ofMap(...)

// Mutable constructors
public static <K, V> HashMap<K, V> newHashMap(...)
public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(...)                 // incl. accessOrder variants
public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(...)
public static <K, V> TreeMap<K, V> newTreeMap(...)
public static <K, V> WeakHashMap<K, V> newWeakHashMap(...)
public static <K, V> ConcurrentSkipListMap<K, V> newConcurrentSkipListMap(...)
public static <K, V> Map<K, V> newFixedHashMap(int capacity)
public static <K, V> Map<K, V> newLinkedHashMap(int capacity) /* newFixedLinkedHashMap */

public static <K, V> Map<K, V> shallowCloneMap(Map<K, V> map)
public static <E, K, V> Map<K, V> toFixedMap(Collection<E> values, Function<E, K> keyExtractor, ...)
public static <K, V> Map.Entry<K, V> ofEntry(K key, V value)
public static <K, V> Map.Entry<K, V> immutableEntry(K key, V value)

// Nested/flat conversion
public static Map<String, Object> flattenMap(Map<String, Object> map)
public static Map<String, Object> nestedMap(Map<String, Object> map)
```

`newFixedHashMap(int)` / `newFixedLinkedHashMap(int)` pre-size the map with `FIXED_LOAD_FACTOR` so inserting exactly
`capacity` entries never triggers a resize — useful in hot paths.

`flattenMap` / `nestedMap` convert between dotted keys (`"a.b.c"`) and nested maps, which is how property-style
configuration is normalized.

### `QueueUtils`

```java
public static final Deque<?> EMPTY_DEQUE;

public static boolean isQueue(Object obj | Class<?> type)
public static boolean isDeque(Iterable<?> iterable)
public static <E> Queue<E> emptyQueue();  public static <E> Deque<E> emptyDeque()
public static <E> Queue<E> unmodifiableQueue(Queue<E> queue)
public static <E> Deque<E> unmodifiableDeque(Deque<E> deque)
public static <E> Queue<E> singletonQueue(E element)
public static <E> Deque<E> singletonDeque(E element)
public static <E> Deque<E> reversedDeque(Deque<E> deque)

public static <E> Queue<E> ofQueue(E... elements)
public static <E> ArrayDeque<E> newArrayDeque(...)
public static <E> PriorityQueue<E> newPriorityQueue(...)
public static <E> ConcurrentLinkedQueue<E> newConcurrentLinkedQueue(...)
public static <E> LinkedBlockingQueue<E> newLinkedBlockingQueue(...)
public static <E> ArrayBlockingQueue<E> newArrayBlockingQueue(int capacity, ...)
public static <E> PriorityBlockingQueue<E> newPriorityBlockingQueue(...)
public static <E> DelayQueue<E> newDelayQueue(...)
public static <E> SynchronousQueue<E> newSynchronousQueue(...)
public static <E> LinkedTransferQueue<E> newLinkedTransferQueue(...)
```

### `EnumerationUtils` and `PropertiesUtils`

```java
// EnumerationUtils
public static boolean isEnumeration(Object obj | Class<?> type)
public static <E> Enumeration<E> of(E... elements)
public static <E> Enumeration<E> ofEnumeration(E... elements)

// PropertiesUtils
public static Properties newProperties()
public static Properties newProperties(Properties defaults)
public static Properties loadProperties(String... propertiesValue)     // "a=1", "b=2"
public static Map<String, Object> flatProperties(Map<String, Object> map)
```

### `Lists` / `Maps` / `Sets` / `Iterators`

`Lists.ofList(...)`, `Sets.ofSet(...)`, `Maps.ofMap(...)` produce **immutable** collections and mirror the JDK 9
`List.of` / `Set.of` / `Map.of` arities (up to 10 arguments plus a varargs form). On JDK 8 the implementation
falls back through `MethodHandles`, so the same source compiles and runs everywhere the library does.
`Iterators` exposes one method: `static boolean equals(Iterator<?> one, Iterator<?> another)`.

---

## 4. Concrete and wrapper types

### `ArrayStack<E> extends ArrayList<E>`

A `List` that is also a stack — you get index access *and* LIFO operations:

```java
public ArrayStack()
public ArrayStack(int initialCapacity)
public E push(E item)
public E pop()
public E peek() throws EmptyStackException
public boolean empty()
public int search(Object o)
```

```java
ArrayStack<String> stack = new ArrayStack<>();
stack.push("a"); stack.push("b");
stack.peek();   // "b"
stack.pop();    // "b"
stack.get(0);   // "a"  — it is still a List
```

### `SingletonDeque<E>` / `SingletonIterator<E>` / `SingletonEnumeration<E>`

Exactly-one collections with no allocation of a backing array:

```java
Deque<String> deque = new SingletonDeque<>("only");
deque.peekFirst();   // "only"
deque.pollLast();    // "only", deque is now empty
```

`SingletonIterator` / `SingletonDeque` extend `ReadOnlyIterator` / `AbstractDeque`; all mutating `Deque` methods that
would add elements throw.

### Read-only and delegating wrappers

| Type | Declaration | Contract |
|---|---|---|
| `ReadOnlyIterator<E>` | `public abstract class implements Iterator<E>`; `public final void remove()` throws | subclass implements `hasNext()`/`next()` |
| `UnmodifiableIterator<E>` | `UnmodifiableIterator(Iterator<E>)` | `remove()` throws |
| `UnmodifiableQueue<E>` / `UnmodifiableDeque<E>` | `UnmodifiableQueue(Queue<E>)` / `UnmodifiableDeque(Deque<E>)` | every mutator throws |
| `DelegatingIterator<E>` | `implements Iterator<E>, DelegatingWrapper`; `DelegatingIterator(Iterator<E>)`; all methods `final`; `Object getDelegate()` | pure pass-through — extend and override selectively |
| `DelegatingQueue<E>` / `DelegatingDeque<E>` | `DelegatingQueue(Queue<E>)` / `DelegatingDeque(Deque<E>)`; `getDelegate()` | same, for queues and deques |
| `ReversedDeque<E>` | `extends DelegatingDeque<E>`; `ReversedDeque(Deque<E>)`; `static <T> Deque<T> of(Deque<T>)` | first/last are swapped, iteration order reversed |
| `DefaultEntry<K, V>` | `implements Map.Entry<K, V>`; `(K, V)`; `static of(K, V)`; `getKey/getValue/setValue` | mutable entry |
| `ImmutableEntry<K, V>` | `extends DefaultEntry<K, V>`; `static of(K, V)`; `setValue` throws | the entry `MapUtils.immutableEntry` returns |
| `IterableAdapter<E>` / `EnumerationIteratorAdapter<E>` | bridge `Iterable`↔`Iterator`↔`Enumeration` | prefer the `CollectionUtils` factories |

> [!TIP]
> The `Delegating*` types implement [`DelegatingWrapper`](language-abstractions.md), so callers can recover the
> wrapped collection with `Wrapper.tryUnwrap(x, ArrayDeque.class)`.

---

## 5. `io.microsphere.filter`

### The core contract

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

`Filter` *is* a `java.util.function.Predicate`, so it plugs straight into Streams and `Collection` APIs — the
difference is only the method name you implement.

### Combining filters

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
List<Class> scanned = FilterUtils.filter(classes,
        FilterOperator.AND,
        type -> type.isAnnotationPresent(Service.class),
        type -> !Modifier.isAbstract(type.getModifiers()));
```

### Ready-made `Class` / `JarEntry` / `String` filters

| Filter | Constructor / instance | Selects |
|---|---|---|
| `ClassFilter` | `@FunctionalInterface interface ClassFilter extends Filter<Class<?>>` | implement inline |
| `PackageNameClassFilter` | `PackageNameClassFilter(String packageName, boolean includedSubPackages)` | classes in a package (optionally recursive) |
| `PackageNameClassNameFilter` | `PackageNameClassNameFilter(String packageName, boolean includedSubPackages)` | class **names** (`Filter<String>`) in a package |
| `TrueClassFilter` | `public static final TrueClassFilter INSTANCE` | everything |
| `ClassFileJarEntryFilter` | `public static final ClassFileJarEntryFilter INSTANCE` | `.class` entries in a JAR |
| `JarEntryFilter` | `interface JarEntryFilter extends Filter<JarEntry>` | implement for entry selection |

### File filters (`io.microsphere.io.filter`)

```java
public interface IOFileFilter extends FileFilter, FilenameFilter

TrueFileFilter.INSTANCE          // accept(File), accept(File, String)
DirectoryFileFilter.INSTANCE     // accept(File file)
NameFileFilter(String name) / NameFileFilter(String name, boolean caseSensitive)
FileExtensionFilter.of(String extension)
```

Because `IOFileFilter` implements both `FileFilter` and `FilenameFilter`, the same instance works for
`File.listFiles(...)` and for `java.io.FileFilter`-style scanning — which is exactly how
`SimpleFileScanner` ([I/O, Files and Watching](io-and-file-watch.md)) accepts filters.

---

## 6. See also

* [Language Abstractions](language-abstractions.md) — `Predicates` (`and`/`or`/`alwaysTrue`) and `Streams`
* [I/O, Files and Watching](io-and-file-watch.md) — scanners that consume these filters
* [Class Loading and Artifacts](classloading-and-artifacts.md) — `JarEntryFilter` in JAR introspection

[← Previous: Core Utilities](core-utilities.md) · [Index](README.md) · [Next: Language Abstractions →](language-abstractions.md)
