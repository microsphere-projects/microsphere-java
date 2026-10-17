# Collections and filters

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-java-core` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Packages** | `io.microsphere.collection` — 32 main-source types · `io.microsphere.filter` — 9 main-source types (41 total) |
| **JDK level** | Compiled at Java 8; CI-tested on JDK 8 / 11 / 17 / 21 / 25 |
| **Depends on other `core` packages** | `io.microsphere.util` (`ArrayUtils.length`, `ObjectUtils.defaultIfNull`, `Assert.assertNotNull`, `ClassUtils.isAssignableFrom`, `Utils` marker), `io.microsphere.lang.DelegatingWrapper`, `io.microsphere.invoke.MethodHandlesLookupUtils`, `io.microsphere.lang.function.ThrowableSupplier`, `io.microsphere.constants` (separator characters) |
| **External dependencies** | none — pure JDK |
| **Used by** | `reflect`, `beans`, `convert`, `io`, `io.scanner`, `classloading`, `json`, `metadata` (see `collection` edges in the [developer index](../../developer-guide/microsphere-java-core/README.md)) |

All `file:line` references in this page are relative to `microsphere-java-core/src/main/java/io/microsphere/`
(main sources) or `microsphere-java-core/src/test/java/io/microsphere/` (tests), read from the `release` worktree.
After the first mention they are shortened to `collection/Foo.java:40-55` / `filter/Foo.java:40-55`.

---

## 1. What this group is for

**Reach for `io.microsphere.collection` when you have to hand an `Iterator`, `Iterable`, `Queue` or `Deque` to someone
else and you do not know whether the source is `null`, empty, or one element long.** Every factory in
`CollectionUtils` (`collection/CollectionUtils.java:43-687`) is total: `null` in → a neutral, non-throwing result out.
`isEmpty(null)` is `true` (`:58-60`), `size(null)` is `0` (`:348-350`), `first(null)` is `null` (`:541-551`),
`toIterable(null)` is the shared empty iterable (`:97-100`). The same package supplies the four wrapper families that
make an iterator or a queue safe to publish — `Delegating*`, `Unmodifiable*`, `Empty*`, `Singleton*` — plus the
`List.of` / `Set.of` / `Map.of` equivalents that let code compiled for Java 8 use immutable collections on every JDK
(`Lists`, `Sets`, `Maps`).

**Reach for `io.microsphere.filter` when you need a named, composable predicate.** `Filter<T>` (`filter/Filter.java:33-48`)
is a `@FunctionalInterface` that extends `java.util.Predicate<T>` and renames the method to `accept(T)`, so a filter is
a lambda you can also hand to `Stream.filter(...)`; `FilterOperator` (`filter/FilterOperator.java:42-122`) adds AND / OR /
XOR composition over an array of filters, and `FilterUtils.filter(...)` (`filter/FilterUtils.java:60-87`) applies a
composed filter to any `Iterable` and returns an unmodifiable `List`. The concrete filters in the package are the
predicates the classpath scanners need: `ClassFilter` (`Class<?>`), `JarEntryFilter` (`JarEntry`),
`PackageNameClassNameFilter` (`String` class name), `ClassFileJarEntryFilter`, `PackageNameClassFilter` and
`TrueClassFilter`.

> [!NOTE]
> The four wrapper families exist for **`Iterator` / `Iterable` / `Enumeration` / `Queue` / `Deque` only**. There is no
> `EmptyList`, `SingletonMap` or `UnmodifiableSet` class in this package — for those, the utility classes delegate to
> the JDK (`Collections.unmodifiableList`, `List.of`) instead, e.g. `ListUtils.ofList(E...)`
> (`collection/ListUtils.java:189-196`) and `SetUtils.ofSet(E...)` (`collection/SetUtils.java:153-155`).

---

## 2. Coordinates and version support

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

| Fact | Value | Evidence (`release`) |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | `pom.xml:7-8` |
| `artifactId` | `microsphere-java-core` | module `pom.xml` |
| Version property | `${revision}`, flattened | `pom.xml:14` |
| Value of `${revision}` on `release` | `0.3.20-SNAPSHOT` | `pom.xml:54` |
| Newest published tag | `0.3.20` | release-line table in the group index (`docs/current/user-guide/microsphere-java-core/README.md:70`) |
| Java level | 8 (sources must build on JDK 8) | CI matrix `java: [ '8', '11', '17', '21', '25' ]`, `.github/workflows/maven-build.yml:25`, `distribution: 'temurin'` `:33` |
| Spring Framework, JDK 17+ | `7.0.9` | `microsphere-java-parent/pom.xml:23` |
| Spring Framework, JDK `[1.8,17)` | `5.3.39` via profile `java8-16` | `microsphere-java-parent/pom.xml:54-62` (activation `<jdk>[1.8,17)</jdk>` at `:57`) |
| Bytecode/API relevant to this page | no `record`, no `var`, no `List.of` at compile time | `Lists.ofList()` resolves `List.of` only through a `MethodHandle` (`collection/Lists.java:46-101`) |

> [!TIP]
> The `java8-16` profile is a **build-time** property switch, not a runtime one; `microsphere-java-core` itself has no
> Spring dependency, so nothing in this page changes between Spring 5 and Spring 7. It matters when you add
> `microsphere-java-*` modules that do use Spring to the same reactor.

---

## 3. API reference

### 3.1 `Iterator` / `Iterable` family

| Factory / class | Returns | Mutability | Null permission | `Serializable`? |
|---|---|---|---|---|
| `CollectionUtils.emptyIterator()` (`collection/CollectionUtils.java:301-305`) | `Collections.emptyIterator()` — the shared JDK instance, **not** `EmptyIterator` | read-only | n/a | JDK instance |
| `CollectionUtils.emptyIterable()` (`:327-331`) | `EmptyIterable.INSTANCE` (`collection/EmptyIterable.java:49`) | read-only | n/a | no |
| `CollectionUtils.toIterable(Collection)` (`:97-100`) | the collection itself, or `EmptyIterable.INSTANCE` | mirrors source | `null` → empty | no |
| `CollectionUtils.toIterable(Iterator)` (`:120-123`) | `new IterableAdapter<>(iterator)` (`collection/IterableAdapter.java:47-59`) | one-shot (an iterator is single-use) | `null` → `EmptyIterator.INSTANCE` (`IterableAdapter.java:52`) | no |
| `CollectionUtils.toIterator(Enumeration)` (`:143-146`) | `new EnumerationIteratorAdapter<>(enumeration)` (`collection/EnumerationIteratorAdapter.java:55-72`) | read-only (`remove()` throws) | `null` → `Collections.emptyEnumeration()` (`:60`) | no |
| `CollectionUtils.toIterable(Enumeration)` (`:166-169`) | `toIterable(toIterator(e))` | read-only | `null` → empty | no |
| `CollectionUtils.singletonIterator(E)` (`:215-219`) | `new SingletonIterator<>(element)` (`collection/SingletonIterator.java:39-71`) | read-only | **`null` element is kept and returned once** (see pitfall 2) | no |
| `CollectionUtils.singletonIterable(E)` (`:188-192`) | `toIterable(singletonIterator(e))` | read-only | as above | no |
| `CollectionUtils.singletonEnumeration(E)` (`:242-246`) | `new SingletonEnumeration<>(element)` (`collection/SingletonEnumeration.java:52-75`) | read-only | `null` kept, returned once | no |
| `CollectionUtils.unmodifiableIterator(Iterator)` (`:278-282`) | `new UnmodifiableIterator<>(iterator)` (`collection/UnmodifiableIterator.java:51-73`) | `remove()` → `IllegalStateException` | `null` **not** tolerated at first use (see pitfall 3) | no |
| `new DelegatingIterator<>(it)` (`collection/DelegatingIterator.java:41-88`) | pass-through `Iterator` + `DelegatingWrapper` | fully delegating | `null` delegate allowed by the ctor (`:45-47`) but fails on use | no |
| `EmptyIterator.INSTANCE` (`collection/EmptyIterator.java:63`) | one shared instance | read-only | n/a | no |
| `Iterators.equals(a, b)` (`collection/Iterators.java:41-55`) | `boolean` | — | both args `@Nullable`, `null` ≠ non-null | — |

### 3.2 `Queue` / `Deque` family

| Factory / class | Returns | Mutability | Null permission | `Serializable`? |
|---|---|---|---|---|
| `CollectionUtils.emptyQueue()` (`:644-648`) → `QueueUtils.emptyQueue()` (`collection/QueueUtils.java:125-129`) | cast of the shared `QueueUtils.EMPTY_DEQUE` (`QueueUtils.java:52`) | `EmptyDeque` | n/a | **yes** (`collection/EmptyDeque.java:63`) |
| `CollectionUtils.emptyDeque()` (`:678-682`) → `QueueUtils.emptyDeque()` (`:144-148`) | same shared instance | idem | n/a | yes |
| `QueueUtils.unmodifiableQueue(q)` (`:172-175`) | `new UnmodifiableQueue(queue)` (`collection/UnmodifiableQueue.java:65-207`) | mutations → `UnsupportedOperationException` | `null` not rejected by the ctor (`:71-73`) | yes (`:65`) |
| `QueueUtils.unmodifiableDeque(d)` (`:199-203`) | `new UnmodifiableDeque(deque)` (`collection/UnmodifiableDeque.java:60-153`) | idem, plus `getFirst/getLast/peekFirst/peekLast` read through | `null` allowed in, fails on use | yes (`:60`) |
| `QueueUtils.singletonQueue(e)` / `singletonDeque(e)` (`:225-229`, `:251-255`) | `new SingletonDeque<>(e)` (`collection/SingletonDeque.java:63-150`) | `offer*`/`poll*`/`push`/`removeFirstOccurrence` → UOE | `null` element allowed, `size()` is still `1` | yes (`:63`) |
| `QueueUtils.reversedDeque(d)` (`:289-291`) → `ReversedDeque.of(d)` (`collection/ReversedDeque.java:46-52`) | `ReversedDeque` view (or the argument if it is already one) | fully mutable, mirrored | `null` → `IllegalArgumentException` — `of` reaches `new ReversedDeque<>(null)` (`:50`) whose super call hits the `DelegatingQueue` guard (`collection/DelegatingQueue.java:44-47`) | no |
| `QueueUtils.ofQueue(e...)` (`:317-321`) | `unmodifiableDeque(newArrayDeque(elements))` | read-only view over a fresh copy | a `null` element → `NullPointerException` — elements are added one by one through `ArrayDeque.add` (`collection/QueueUtils.java:383-384`) | yes |
| `new DelegatingQueue<>(q)` (`collection/DelegatingQueue.java:40-183`) | pass-through `Queue` | fully delegating | **`null` → `IllegalArgumentException`** (`:44-47`) | no |
| `new DelegatingDeque<>(d)` (`collection/DelegatingDeque.java:34-148`) | pass-through `Deque` | fully delegating | `null` → `IllegalArgumentException` (inherited) | no |
| `AbstractDeque<E>` (`collection/AbstractDeque.java:82-143`) | skeletal base | subclass-defined | subclass-defined | no |

### 3.3 Immutable entries, stacks and enumerations

| Type | Kind | Key behaviour |
|---|---|---|
| `DefaultEntry<K,V>` (`collection/DefaultEntry.java:35-95`) | mutable `Map.Entry` | `key` is `final` (`:37`), `value` is not (`:39`); `setValue` returns the old value (`:69-74`); `toString()` is `key + "=" + value` (`:91-94`); factory `of` (`:55-57`) |
| `ImmutableEntry<K,V>` (`collection/ImmutableEntry.java:47-70`) | read-only entry | extends `DefaultEntry` and overrides only `setValue` → `UnsupportedOperationException("ReadOnly Entry can't be modified")` (`:53-56`) |
| `MapUtils.ofEntry(k,v)` / `immutableEntry(k,v)` (`collection/MapUtils.java:1081`, `:1110`) | factories | the mutable/immutable pair behind `Map.Entry` creation |
| `ArrayStack<E>` (`collection/ArrayStack.java:41`) | `extends ArrayList<E>` | stack operations on top of `ArrayList` |
| `ArrayEnumeration<E>` (`collection/ArrayEnumeration.java:54`) | `implements Enumeration<E>` | array-backed enumeration |
| `EnumerationUtils.of(E...)` / `ofEnumeration(E...)` (`collection/EnumerationUtils.java:96`, `:119`) | factories | `isEnumeration` overloads at `:55`, `:73` |

### 3.4 The `*of*` factories (Java 9 immutable collections on Java 8)

| Class | Emulates | Arity overloads | Java 8 fallback |
|---|---|---|---|
| `Lists` (`collection/Lists.java:41-533`) | `List.of` | `ofList()` … `ofList(e1..e10)` + `ofList(E...)` (`:119`, `:152`, `:513`) | `Collections.emptyList()` (`:123-132`), `Collections.singletonList(e1)` (`:156-165`), `ListUtils.of(elements)` (`:520-529`) |
| `Sets` (`collection/Sets.java`) | `Set.of` | `ofSet()` … `ofSet(e1..e10)` + `ofSet(E...)` (`:121`, `:158`, `:552`) | same `MethodHandle == null` pattern |
| `Maps` (`collection/Maps.java`) | `Map.of` | `ofMap()` … `ofMap(k1..k10)` + `ofMap(Entry...)` (`:116`, `:150`, `:585`) | same pattern |
| `ListUtils.ofList(E...)` (`collection/ListUtils.java:189-196`) | `Collections.unmodifiableList(Arrays.asList(...))` | — | identical on all JDKs |
| `MapUtils.of(...)` (`collection/MapUtils.java:207-388`) | 1–5 pairs, `of(Object...)`, `of(Entry...)` | — | hand-built map |

### 3.5 Signatures (real modifiers, real annotations)

```java
// collection/CollectionUtils.java — abstract + private ctor: never instantiable, never subclassed (:43, :684-685)
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

// collection/QueueUtils.java — the Queue/Deque factory surface (:50)
    public static final Deque<?> EMPTY_DEQUE = EmptyDeque.INSTANCE;                          // :52
    @Nonnull @Immutable public static <E> Queue<E> emptyQueue();                             // :125-127
    @Nonnull @Immutable public static <E> Deque<E> emptyDeque();                             // :144-146
    @Nonnull public static <E> Queue<E> unmodifiableQueue(Queue<E> queue);                   // :172-173
    @Nonnull public static <E> Deque<E> unmodifiableDeque(Deque<E> deque);                   // :199-201
    @Nonnull @Immutable public static <E> Queue<E> singletonQueue(E element);                // :225-227
    @Nonnull @Immutable public static <E> Deque<E> singletonDeque(E element);                // :251-253
    public static <E> Deque<E> reversedDeque(Deque<E> deque);                                 // :289
    @Nonnull @Immutable public static <E> Queue<E> ofQueue(E... elements);                    // :317-319

// collection/DelegatingIterator.java — every method is FINAL (:41-88): this is a closed-forwarding decorator
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

// collection/ReadOnlyIterator.java — the one throwing seam of the iterator tree
@Immutable public abstract class ReadOnlyIterator<E> implements Iterator<E> {   // :49-50
    @Override public final void remove() { throw new IllegalStateException("Read-Only"); }  // :52-55

// collection/DelegatingQueue.java — protected delegate, non-final methods: subclass-friendly
public class DelegatingQueue<E> implements Queue<E>, DelegatingWrapper {         // :40
    protected final Queue<E> delegate;                                          // :42
    public DelegatingQueue(Queue<E> delegate) { assertNotNull(delegate, ...); }   // :44-47
    @Override public Queue<E> getDelegate();                                      // :179-182

// collection/DelegatingDeque.java — Java 21 Deque.reversed() with a Java 8 fallback
    private static final MethodHandle reversedMethodHandle = findPublicVirtual(Deque.class, "reversed"); // :39
    public Deque<E> reversed();                                                  // :138-140
    protected Deque<E> reversed(MethodHandle reversedMethodHandle);             // :142-147

// filter/Filter.java — the predicate root
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

## 4. Runnable examples (mined from the real test suite)

**Wrapping an `Iterator` and reading its identity back** — from
`microsphere-java-core/src/test/java/io/microsphere/collection/DelegatingIteratorTest.java:48-99`:

```java
List<String> list = ofArrayList("1", "2", "3");   // ListUtils.ofArrayList, test import :28
Iterator<String> iterator = list.iterator();
DelegatingIterator<String> delegatingIterator = new DelegatingIterator<>(iterator);

assertEquals("1", delegatingIterator.next());     // :62-64
assertThrows(NoSuchElementException.class, delegatingIterator::next);  // :65
assertSame(delegatingIterator.getDelegate(), iterator);               // :83
assertEquals(delegatingIterator, iterator);                           // :93  equals() forwards
assertEquals(delegatingIterator.toString(), iterator.toString());     // :98
```

`remove()` is exercised at `:69-74` by removing each element after reading it — an `ArrayList` iterator supports it, so
`DelegatingIterator` never has to decide anything itself.

**The read-only contract, inherited by `SingletonIterator`** —
`collection/ReadOnlyIteratorTest.java:41-75` is the shared base; `collection/SingletonIteratorTest.java:30-36` only
supplies the instance:

```java
class SingletonIteratorTest extends ReadOnlyIteratorTest {
    @Override protected Iterator<?> createIterator() { return singletonIterator(TEST_ELEMENT); }  // :33-35
}
// asserted by the base class:
//   hasNext() is true, next() == TEST_ELEMENT, a second next() throws NoSuchElementException (:53-59)
//   remove() throws IllegalStateException                                                     (:62-64)
//   forEachRemaining(...) consumes the element and leaves hasNext() == false                  (:66-75)
```

**The `Empty*` singletons** — `collection/EmptyIteratorTest.java:22-40` and `collection/EmptyDequeTest.java:41-91`:

```java
EmptyIterator emptyIterator = new EmptyIterator();          // :24
assertFalse(emptyIterator.hasNext());                        // :29
assertThrows(NoSuchElementException.class, emptyIterator::next);    // :34
assertThrows(IllegalStateException.class, emptyIterator::remove);   // :39  <-- not UnsupportedOperationException

EmptyDeque emptyDeque = INSTANCE;                            // :41
assertSame(emptyIterator(), emptyDeque.iterator());          // :45  the SAME shared JDK empty iterator
assertSame(emptyIterator(), emptyDeque.descendingIterator()); // :50
assertThrows(UnsupportedOperationException.class, () -> emptyDeque.offerFirst(null)); // :55
assertNull(emptyDeque.pollFirst());                          // :65  neutral value, no throw
assertThrows(NoSuchElementException.class, emptyDeque::getFirst);  // :75
assertFalse(emptyDeque.removeLastOccurrence(null));          // :85
assertEquals(0, emptyDeque.size());                           // :90
```

**`UnmodifiableDeque` mutation/observation split** — from
`microsphere-java-core/src/test/java/io/microsphere/collection/UnmodifiableDequeTest.java:35-127`. The view is built on
top of another wrapper (`deque = new UnmodifiableDeque(new SingletonDeque(TEST_ELEMENT))`, `:41`), which is how the tests
get a one-element deque without a JDK collection:

```java
assertThrows(UnsupportedOperationException.class, () -> deque.addFirst(TEST_ELEMENT));    // :46
assertThrows(UnsupportedOperationException.class, () -> deque.offerLast(TEST_ELEMENT));   // :61
assertThrows(UnsupportedOperationException.class, deque::pollFirst);                       // :76
assertThrows(UnsupportedOperationException.class, deque::pop);                             // :121
assertEquals(TEST_ELEMENT, deque.getFirst());                     // :86  reads pass through
assertEquals(TEST_ELEMENT, deque.peekLast());                     // :101
assertTrue(deque.descendingIterator() instanceof UnmodifiableIterator);                   // :126
```

`UnmodifiableQueueTest` asserts the same split for the `Queue` level (`add`/`offer`/`poll` throw, `peek`/`element`
read through).

**Filtering with a single predicate** — from
`microsphere-java-core/src/test/java/io/microsphere/filter/FilterTest.java:37-52`:

```java
Filter<String> filter = value -> "true".equalsIgnoreCase(value);   // :39
assertFalse(filter.accept("test"));  assertTrue(filter.accept("true"));  // :44-45
assertFalse(filter.test("test"));    assertTrue(filter.test("true"));    // :50-51  Predicate bridge
```

**Composing filters and applying them to a collection** — from
`microsphere-java-core/src/test/java/io/microsphere/filter/FilterUtilsTest.java:26-40`:

```java
List<Integer> integerList = asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

List<Integer> result = filter(integerList, AND, filteredObject -> filteredObject % 2 == 0);
assertEquals(asList(0, 2, 4, 6, 8), result);          // :29-31

result = filter(integerList, filteredObject -> filteredObject % 2 == 0);   // single-filter overload -> AND
assertEquals(asList(0, 2, 4, 6, 8), result);          // :33-35

result = filter(integerList, OR, filteredObject -> filteredObject % 2 == 1);
assertEquals(asList(1, 3, 5, 7, 9), result);          // :37-39
```

The returned list is `Collections.unmodifiableList` of the collected elements
(`filter/FilterUtils.java:78-86`), so `result.add(...)` throws.

**Package-name filtering** — `filter/PackageNameClassNameFilter.java:43-60` with
`filter/PackageNameClassNameFilterTest.java` and `PackageNameClassFilterTest.java`:

```java
Filter<String> filter = new PackageNameClassNameFilter("io.microsphere", true);
filter.accept("io.microsphere.collection.Lists");        // true  — exact package
filter.accept("io.microsphere.filter.Filter");           // true  — sub-package
filter.accept("io.other.Foo");                           // false
filter.accept("");                                       // false — blank short-circuits (:51-53)
```

**Null-tolerant collection plumbing** — asserted directly in
`microsphere-java-core/src/test/java/io/microsphere/collection/CollectionUtilsTest.java` (`testIsEmpty` `:81`,
`testToIterable` `:122`, `testSize` `:167`, `testEquals` `:191-195`, `testAddAllWithNullCollection` `:230`,
`testAddAllWithNullList` `:247`, `testAddAllWithNullIterable` `:252`, `testFirst` `:257`, `testEmptyQueue` `:272`,
`testEmptyDeque` `:278`). The behaviour is defined by these lines:

```java
isEmpty(null)                    // true   — collection/CollectionUtils.java:58-60
size((Collection) null)          // 0      — :348-350
size((Iterable) null)            // 0      — :374-377
first((Collection) null)         // null   — :541-545
addAll(null, "a", "b")           // 0      — :454-457
toIterable((Collection) null)    // EmptyIterable.INSTANCE — :97-100
equals(null, null)               // true   — :413-415 (test :195)
equals(null, singletonList)      // false  — :421-423 (test :192)
equals(emptyList, null)          // true   — :417-419, "null and empty are the same"
```

---

## 5. Compatibility notes

| Capability | Java 8 | Java 9 – 20 | Java 21+ |
|---|---|---|---|
| `Lists.ofList(...)` (`collection/Lists.java:119-529`) | falls back to `Collections.emptyList` / `singletonList` / `ListUtils.of` | real `List.of` via `MethodHandle` | real `List.of` |
| `Sets.ofSet(...)`, `Maps.ofMap(...)` | fallback path, same pattern | real `Set.of` / `Map.of` | real |
| `DelegatingDeque.reversed()` (`collection/DelegatingDeque.java:138-147`) | `MethodHandle` is `null` → `QueueUtils.reversedDeque(delegate)` = `ReversedDeque` view (`:143-144`) | same fallback (`Deque.reversed` does not exist) | `Deque.reversed()` invoked through the handle (`:146`) |
| `ReversedDeque` (`collection/ReversedDeque.java:40`) | the portable reverse view; javadoc states it is "based on JDK 21 `java.util.ReverseOrderDequeView`" (`:33-37`) | idem | used as the fallback, not as the primary path |
| `Collections.emptyIterator()` / `emptyEnumeration()` | JDK 7+ — always available | idem | idem |
| `Objects.hashCode(Object)` used by `SingletonDeque.hashCode` (`:146-149`) | JDK 8 OK | idem | idem |

How the version detection works: `MethodHandlesLookupUtils.findPublicStatic(List.class, "of", ...)` returns `null` when
the method is absent, so the `MethodHandle` fields at `collection/Lists.java:46-101` are `null` on Java 8 and the
package-private helpers (`ofList0` … `ofListElements`, `:123-132` / `:156-165` / `:520-529`) branch to the JDK 8 fallback.
Every helper also catches `Throwable` from `invokeExact` and falls back again (`:127-131`), so a linkage problem
degrades rather than crashes — consistent with the library-wide policy in the developer index.

> [!NOTE]
> The `ofList*` / `ofSet*` / `ofMap*` helper methods are **package-private**, not `private`. That is deliberate:
> `ListsTest` passes a `null` handle to force the Java 8 branch (`assertEquals(emptyList(), ofList0(null))`,
> `collection/ListsTest.java:108`) and passes an unrelated handle to force the `catch (Throwable)` branch
> (`collection/ListsTest.java:112-114`). Do not call them from your own code outside `io.microsphere.collection`;
> they are not API.

---

## 6. Pitfalls and known limits

1. **`DelegatingIterator` cannot be specialised.** All of its methods are `final`
   (`collection/DelegatingIterator.java:49-87`), including `getDelegate()` which returns `Object`. To add behaviour,
   wrap it or use `ReadOnlyIterator` instead; `EmptyIterator` takes exactly that route (`:58`, `:65-67`).
2. **`singletonIterator(null)` is not empty.** The javadoc claims "If the provided element is null, returns an iterator
   with no elements" (`collection/CollectionUtils.java:196`), but the factory calls `new SingletonIterator<>(element)`
   (`:217-219`) and `SingletonIterator` performs no null check (`collection/SingletonIterator.java:45-47`; `hasNext`
   starts `true` at `:43`). Result: `hasNext()` is `true` and `next()` returns `null` once (`:54-61`). Same for
   `singletonEnumeration(null)` (`:242-246` + `collection/SingletonEnumeration.java:58-74`) and `SingletonDeque` —
   `size()` stays `1` (`:133-136`).
3. **`unmodifiableIterator(null)` does not return an empty iterator.** The javadoc says it does
   (`collection/CollectionUtils.java:254`), but the constructor stores the argument verbatim
   (`collection/UnmodifiableIterator.java:55-57`) and `hasNext()` dereferences it (`:59-62`) → `NullPointerException` on
   first use. Contrast `IterableAdapter`, which does default to `EmptyIterator.INSTANCE`
   (`collection/IterableAdapter.java:51-53`), and `EnumerationIteratorAdapter`, which defaults to
   `Collections.emptyEnumeration()` (`:59-61`).
4. **`new DelegatingQueue<>(null)` throws `IllegalArgumentException`, not `NullPointerException`.** The guard is
   `assertNotNull(delegate, () -> "The 'delegate' argument must not be null!")`
   (`collection/DelegatingQueue.java:44-47`), and `Assert.assertNotNull` raises `IllegalArgumentException`
   (`util/Assert.java:149-153`). Catch `IllegalArgumentException` if you migrate from `Objects.requireNonNull`.
5. **`Empty*` is a mix of neutral values and real throws — read the table, not the class name.**
   `pollFirst`/`pollLast`/`peekFirst`/`peekLast` return `null` and `remove*` return `false`
   (`collection/EmptyDeque.java:89-127`), but `offerFirst`/`offerLast` throw `UnsupportedOperationException` (`:79-87`)
   and `getFirst`/`getLast` throw `NoSuchElementException` (`:99-107`). On the `Queue` view the split is
   `add(e)`/`offer(e)` → `UnsupportedOperationException`, `remove()`/`element()` → `NoSuchElementException`,
   `poll()`/`peek()` → `null`, `remove(Object)` → `false` — exactly what
   `collection/EmptyQueueTest.java:50-78` asserts.
6. **`EmptyIterator.remove()` throws `IllegalStateException`.** Its own javadoc example catches
   `UnsupportedOperationException` (`collection/EmptyIterator.java:44-49`); the test records the truth —
   `assertThrows(IllegalStateException.class, emptyIterator::remove)` (`collection/EmptyIteratorTest.java:38-40`) —
   because the call forwards to `Collections.emptyIterator()` (`:65-67` → `:304`) and the JDK instance raises ISE.
   Same for `UnmodifiableIterator`/`SingletonIterator`: `IllegalStateException("Read-Only")`
   (`collection/ReadOnlyIterator.java:52-55`).
7. **`ImmutableEntry.setValue` is the only entry mutation that throws**, and it throws
   `UnsupportedOperationException("ReadOnly Entry can't be modified")` (`collection/ImmutableEntry.java:53-56`).
   `DefaultEntry` is fully mutable (`:39`, `:69-74`) and is **not** `Serializable` (`:35`), so it cannot be put into a
   serialized map copy.
8. **`PackageNameClassFilter` can throw on the default package.** `accept` guards only against a `null` class
   (`filter/PackageNameClassFilter.java:46-49`), then calls `filteredObject.getPackage().getName()` (`:50-51`) —
   `getPackage()` is `null` for classes in the unnamed package, giving `NullPointerException`. Prefer the
   `String`-based `PackageNameClassNameFilter`, whose first branch handles blank input (`:50-53`).
9. **`ClassFileJarEntryFilter.INSTANCE.accept(null)` throws NPE** — no null branch at
   `filter/ClassFileJarEntryFilter.java:43-46`; and its constructor is `protected` (`:39-41`), so you cannot create a
   second instance from outside the package without subclassing.
10. **Null-permission of the `of*` factories is JDK-dependent.** `Lists.ofList(null)` throws `NullPointerException` on
    Java 9+ (the real `List.of` is invoked, `collection/Lists.java:160-161`) but yields a one-element list holding `null`
    on Java 8, where the fallback is `Collections.singletonList(e1)` (`:156-159`). Use `ListUtils.ofList(E...)`
    (`collection/ListUtils.java:189-196`) if you need identical behaviour on every JDK — it always wraps
    `Arrays.asList` in `Collections.unmodifiableList` and therefore permits `null`.
11. **`FilterOperator` with zero filters accepts everything, including for XOR.** All three operators short-circuit
    `length == 0` to `true` (`filter/FilterOperator.java:51-52`, `:68-69`, `:84-85`), and XOR seeds `success = true`
    before folding (`:86`). So `FilterOperator.XOR.createFilter()` returns a filter that accepts everything, not
    nothing — and `FilterUtils.filter(iterable, AND)` with no filters copies the whole iterable (`:82`).
12. **`EmptyDeque` is a shared, cast singleton.** `QueueUtils.emptyQueue()` and `emptyDeque()` both return the *same*
    `EMPTY_DEQUE` instance through an unchecked cast (`collection/QueueUtils.java:52`, `:125-148`), so
    `emptyQueue() == emptyDeque()`. It is `EmptyDeque<?>` (`collection/EmptyDeque.java:67`), and `serialVersionUID` is
    `-1L` (`:65`) — do not depend on a stable id for your own protocol.
13. **Views are live, not snapshots.** `UnmodifiableQueue`/`UnmodifiableDeque` keep a plain reference
    (`collection/UnmodifiableQueue.java:69`, `collection/UnmodifiableDeque.java:62`) and their own javadoc states that
    behaviour "is undefined if the underlying deque is modified directly while this view exists"
    (`collection/UnmodifiableDeque.java:33-36`). `QueueUtils.ofQueue(e...)` avoids this by copying first
    (`:317-321`).
14. **`Filter` composition does not reuse `Predicate` combinators.** `Filter.and/or/negate` (inherited from
    `Predicate`) return `Predicate`, not `Filter`, so you lose the named `accept` method; compose with
    `FilterOperator.createFilter(...)` instead (`filter/FilterOperator.java:111-120`), which returns a `Filter`.

---

## 7. See also

* [Utilities, constants and text](util-and-constants.md) — `ArrayUtils.length`, `ObjectUtils.defaultIfNull` and
  `Assert`, which every null-tolerance rule in this page leans on
* [Language abstractions](language-abstractions.md) — `DelegatingWrapper`, the interface the `Delegating*` classes
  implement
* [Reflection, types and beans](reflection-and-beans.md) — the main consumer of `collection`'s `Map.Entry` types
* [I/O, NIO, scanning and file filters](io-and-scanning.md) — `io.microsphere.io.filter`, where the `File` predicates
  live; `io.scanner.Scanner` consumes `io.microsphere.filter.Filter` directly
* Developer-facing deep dive: [Collections and filters — Developer Guide](../../developer-guide/microsphere-java-core/collections-and-filters.md)
* [中文用户指南](zh/collections-and-filters.md)
