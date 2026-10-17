# Reflection, types and beans

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-java-core` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Packages** | `io.microsphere.reflect` — 19 main-source types · `io.microsphere.reflect.generics` — 2 · `io.microsphere.internal.reflect` — 1 · `io.microsphere.beans` — 4 (26 total) |
| **JDK level** | Compiled at Java 8; CI-tested on JDK 8 / 11 / 17 / 21 / 25 |
| **Depends on other `core` packages** | `util` (`ArrayUtils`, `StringUtils`, `Assert`, `ClassUtils`, `Utils` marker), `lang` (`Version`, `Deprecation`, `MutableInteger`), `collection` (`ListUtils`, `Maps`, `SetUtils`), `logging`, `io.event` (`ConfigurationProperty` annotation) |
| **External dependencies** | none — pure JDK (`javax.annotation-api`, `slf4j-api`, `commons-logging` are optional and unrelated here) |
| **Used by** | `convert`, `classloading`, `io.scanner`, `metadata`, `json`, `net`, `management` — i.e. this is the group almost everything else leans on |

All `file:line` references are relative to `microsphere-java-core/src/main/java/io/microsphere/` (main) or
`microsphere-java-core/src/test/java/io/microsphere/` (tests), read from the `release` worktree, shortened to
`reflect/Foo.java:40-55` after the first mention. All 26 types carry javadoc `@since 1.0.0`; no `@Since` **annotation** is used here.

---

## 1. What this group is for

**Reach for `io.microsphere.reflect` whenever your code has to survive a JDK upgrade.** Plain `java.lang.reflect` has
two sharp edges: `setAccessible(true)` throws `InaccessibleObjectException` on JDK 16+ (JEP 396), and
`java.lang.reflect.Type` is an interface hierarchy you cannot ask sensible questions of (generic supertype? wildcard?).
`AccessibleObjectUtils.trySetAccessible(...)` (`reflect/AccessibleObjectUtils.java:97-104`) picks the right mechanism per
JDK and never throws; `JavaType` (`reflect/JavaType.java:92`) wraps any `Type` with memoised super type / interfaces / generic types.

**Reach for `io.microsphere.beans` when you need "this object as a `Map<String, Object>`".**
`BeanUtils.resolvePropertiesAsMap(bean)` (`beans/BeanUtils.java:204`) walks JavaBeans getters recursively into an
unmodifiable map with a depth guard, so a self-referencing bean cannot blow the stack (`:452-472`, `:457`).

**Reach for the definition family (`ReflectiveDefinition` → `MemberDefinition` → `ExecutableDefinition` →
`MethodDefinition` / `ConstructorDefinition`, plus `FieldDefinition` and `ClassDefinition`) when the member you want
may not exist on the JVM you land on.** A definition is a `Serializable` description ("since version X, in class Y,
named Z") resolved lazily, so `isPresent()` answers the question declaratively
(`reflect/ReflectiveDefinition.java:91-106`, `reflect/MemberDefinition.java:166-177`).

> [!IMPORTANT]
> This is the **only** layer in `core` allowed to touch JDK reflection internals, and every internal-access path has a documented fallback (§5) — route `setAccessible` needs here rather than adding them to other packages.

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
| Coordinates | `io.github.microsphere-projects:microsphere-java-core` | `microsphere-java-core/pom.xml:13-15` |
| Version | `${revision}`, flattened; `0.3.20-SNAPSHOT` on `release`; newest published tag `0.3.20` | `pom.xml:14`, `:54`; `docs/current/user-guide/microsphere-java-core/README.md:70` |
| Java level | 8 (sources must build on JDK 8); CI runs `-Dsurefire.useSystemClassLoader=false` | CI matrix `java: [ '8', '11', '17', '21', '25' ]`, `.github/workflows/maven-build.yml:25`, `distribution: 'temurin'` `:33`, flag `:43` |
| Spring Framework (test-only, used by `JavaTypeTest`) | `5.3.39` on `[1.8,17)`, newer line otherwise | `microsphere-java-parent/pom.xml:54-62`; test-scope dep `microsphere-java-core/pom.xml:88-92` |
| Version stamp on all 26 types | javadoc `@since 1.0.0` | e.g. `reflect/AccessibleObjectUtils.java:42`, `reflect/JavaType.java:90`, `beans/BeanUtils.java:85`, `internal/reflect/ConstantPoolUtils.java:42` |

### 2.1 The accessibility matrix (the centrepiece)

`AccessibleObjectUtils` decides *by capability, not by version number*: at class-initialisation time it looks up the two JDK 9
methods through `MethodHandle`s and stores `null` when they do not exist (`:62` for `canAccess`, `:68` for `trySetAccessible`); everything below follows from whether those handles are non-`null`.

| Operation | JDK 8 | JDK 9 – 15 | JDK 16+ (JEP 396) | Notes |
|---|---|---|---|---|
| `trySetAccessible(AccessibleObject)` | `setAccessible(true)` fallback path (`:99-100`, `:157-168`) | `AccessibleObject.trySetAccessible()` via `MethodHandle` (`:102`, `:170-178`) | same handle path; returns `false` instead of throwing for JDK-internal members | Return value is **success**, never "previous status" on the 9+ path |
| `setAccessible(AccessibleObject)` (package-private) | always used by the 8 branch | used only when no handle is available | used only when no handle is available | Returns the **previous** accessible flag (`:158`, `:167`); a `RuntimeException` from `setAccessible(true)` is swallowed after logging (`:163-165`) |
| `canAccess(Object, AccessibleObject)` | public short-circuit at `:144-146`; otherwise falls back to `isAccessible()` because `tryCanAccess` returns `null` (`:147-148`, `:184-194`) | real `AccessibleObject.canAccess(Object)` (`:147`) | real `canAccess`, so module rules are honoured exactly | For a member of a non-open JDK package on 16+, `canAccess` is `false` even after `trySetAccessible` |
| `InaccessibleObjectException` handling | class not loaded; detection is by **name string**, so it still works (`reflect/ReflectionUtils.java:165`, `:537-538`) | thrown only by the legacy path | thrown by the legacy path, caught at `:163` and converted into a logged `--add-opens` hint (`:196-211`) | The library never rethrows it to your caller from `setAccessible` |
| Required JVM option when you must force access | none | none normally | `--add-opens=<module>/<package>=ALL-UNNAMED`, printed in the error log at `:204-208` | The message is built by parsing `"module "` and `"opens "` out of the exception text (`:199-200`) |

> [!WARNING]
> The two `trySetAccessible` overloads have **different return semantics**. The public one
> (`reflect/AccessibleObjectUtils.java:97-104`) reports *whether the object is accessible now*; the package-private
> `setAccessible` (`:157-168`) reports *whether it was accessible before the call*. The tests rely on exactly this
> split — see §4.1.

---

## 3. API reference

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

Null and failure behaviour: on JDK 9+ a `null` or unreachable `AccessibleObject` makes the handle call fail and
`trySetAccessible` returns `false`, because the whole body is wrapped in `catch (Throwable)` (`:170-178`); the two
`MethodHandle`s are `static final`, resolved once at `:62` and `:68`. The package-private `setAccessible` (`:157-168`) only
catches the `RuntimeException` thrown by `setAccessible(true)` at `:161` — the `NullPointerException` from `isAccessible()` at
`:158` is not. `canAccess` (`:142-149`) treats a `null` member as non-public via `MemberUtils.isPublic`, then, when `tryCanAccess`
yields `null` (JDK 8, `:184-194`), falls back to `accessibleObject.isAccessible()` at `:148`, which likewise NPEs on `null` there.

### 3.2 `MethodUtils` — `reflect/MethodUtils.java` (1874 lines)

Three static caches back every lookup:

| Cache | Declaration | Key | Cleared by |
|---|---|---|---|
| resolved methods | `private final static ConcurrentMap<MethodKey, Method> methodsCache = newConcurrentHashMap(256)` (`:171`) | `MethodKey` (`:1827-1869`) | `clearMethodsCache()` (`:1715`) |
| banned methods | `bannedMethodsCache` (`:184`) | `MethodKey` from a signature string | `clearBannedMethodsCache()` (`:1738`) |
| declared methods per class | `declaredMethodsCache` (`:186`) | `Class<?>` | `clearDeclaredMethodsCache()` (`:1745`) |

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

`invokeMethod` behaviour (`:1097-1123`):

| Situation | Result | Line |
|---|---|---|
| `method == null` | `assertNotNull` → `NullPointerException` with `"The 'method' must not be null"` | `:1098` |
| `forceAccess == true` | `trySetAccessible(method)` is attempted **before** the call; failure is not fatal by itself | `:1103-1104` |
| `IllegalAccessException` / `IllegalArgumentException` | rewrapped as `IllegalArgumentException`, thrown after the `finally` | `:1107-1108`, `:1118-1120` |
| `InvocationTargetException` | the **target** exception is unwrapped and thrown as `RuntimeException` | `:1109-1110` |
| void method | returns `null` | `:1092`, `:1099` |

The by-name overloads (`invokeMethod(Object, String, Object...)` and friends) throw `IllegalArgumentException` when no
method matches (`:836-840`); there is no "not found" result.

### 3.3 `FieldUtils` — `reflect/FieldUtils.java` (1091 lines)

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

| Situation | Result | Line |
|---|---|---|
| `field == null` in `getFieldValue` | returns `null` — no exception | `:818-820` |
| `IllegalAccessException` in `getFieldValue` | wrapped as **`IllegalStateException`** | `:830-831` |
| `forceAccess` on a JDK-internal field | `trySetAccessible(field)` at `:827`; if it fails you get `IllegalAccessException` → `IllegalStateException` | `:826-831` |
| `getFieldValue(..., defaultValue)` | `defaultIfNull` applied to the read value | `:651-655` |
| `setFieldValue` with `field == null` | returns `null` | `:1015` |
| `setFieldValue` when the current value already equals the new one | write is **skipped**, the existing value is returned | `:1027-1029` |
| `IllegalAccessException`/`IllegalArgumentException` in `setFieldValue` | wrapped as **`IllegalArgumentException`** | `:1030-1031` |

> [!NOTE]
> Asymmetry worth remembering: a failed **read** surfaces as `IllegalStateException`, a failed **write** as
> `IllegalArgumentException`. Several `setFieldValue` javadocs also list `IllegalAccessException` in `@throws`
> (`:876-878`, `:966-968`), but `:1030-1031` catches it, so that checked exception never escapes the method.

### 3.4 `ConstructorUtils` — `reflect/ConstructorUtils.java`

Two philosophies split by verb, exactly as `MethodUtils`:

| Verb | Not found | Throws | Line |
|---|---|---|---|
| `getConstructor` / `getDeclaredConstructor` | — | `RuntimeException`, translated by `ExecutableUtils.execute(...)` | `:205-231` |
| `findConstructor` / `findConstructors` / `findDeclaredConstructors` | `null` / empty list (`NOT_FOUND_CONSTRUCTOR = null`, `:53`) | none | `:264-271` |
| `newInstance(Class, Object...)` | — | `IllegalArgumentException` when nothing matches | `:324-332` |
| `newInstance(boolean forceAccess, Constructor, Object...)` | — | `IllegalStateException` on access failure; target exception unwrapped as `RuntimeException` | `:393-411` |

`reflect/ExecutableUtils.java` is the shared translator: `execute` overloads `:100`, `:157`, `:215`; exception mapping `:225-237`; parameter-type matching in assignable / equivalent / non-null modes `:249-266`.

### 3.5 `JavaType` — `reflect/JavaType.java` (1329 lines)

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

Factory null behaviour — the part people get wrong:

| Factory | On `null` / not found | Line |
|---|---|---|
| `fromField(Field)` / `fromField(Class, String)` | `NULL_JAVA_TYPE` | `:849`; field-lookup path from `:818` |
| `fromMethodReturnType(Method)` / `(Class, String, Class...)` | `NULL_JAVA_TYPE` | `:911` |
| `fromMethodParameters(Class, String, Class...)` | `EMPTY_JAVA_TYPE_ARRAY` | `:943` |
| `fromMethodParameters(Method)` `:974` and `fromMethodParameter(Method, int)` `:1005` | **no null guard** — dereferences | `:974`, `:1005` |

`JavaType.Kind` (`:1162-1327`) is an internal strategy enum, one constant per `java.lang.reflect.Type` shape: `CLASS` `:1167`,
`PARAMETERIZED_TYPE` `:1201`, `TYPE_VARIABLE` `:1265`, `WILDCARD_TYPE` `:1270`, `GENERIC_ARRAY_TYPE` `:1275`, `UNKNOWN` `:1280`.
`valueOf(Type)` (`:1316-1325`) returns the first constant whose `isType(Type)` is `true`.

### 3.6 `TypeUtils` — `reflect/TypeUtils.java` (1579 lines)

The generic-resolution engine. Cache size comes from
`microsphere.reflect.resolved-generic-types.cache.size` (`:93`), default `"256"` (`:98`, parsed `:103`, read `:114`,
annotated `@ConfigurationProperty` `:108-113`), into
`private static final ConcurrentMap<MultipleType, List<Type>> resolvedGenericTypesCache` (`:116`). Core resolution is
`protected static List<Type> doResolveActualTypeArguments(Type, Class)` (`:581-632`); the public
`resolveActualTypeArguments(Type, Class)` (`:577-579`) wraps it in `unmodifiableList`.

The `findTypes` family is one protected method (`:1092-1101`; `null` or `Object` type → `emptyList()` at
`:1097-1099`) plus flag-tuned public wrappers:

| Public method | Line | self | hierarchical | generic superclass | generic interfaces |
|---|---|---|---|---|---|
| `findAllGenericSuperclasses` | `:935` → `:936` | no | yes | yes | no |
| `findAllGenericInterfaces` | `:962` → `:963` | no | yes | no | yes |
| `findParameterizedTypes` | `:991` → `:992` | yes | no | yes | yes |
| `findHierarchicalTypes` | `:1049` → `:1050` | no | yes | yes | yes |
| `findAllTypes` | `:1083` → `:1084` | yes | yes | yes | yes |
| `findAllParameterizedTypes` | `:1019` → delegates to `findAllTypes` at `:1020` | | | | |

Also: `getRawType` `:311`, `getRawClass` `:341`, `asClass` `:1299-1308`, `getComponentType` `:1509-1517`, and `getTypeNames(Type...)` `:1568-1574`, the rare **throwing** case (`assertNoNullElements`).

### 3.7 `reflect.generics` — 2 types

`ParameterizedTypeImpl` (`reflect/generics/ParameterizedTypeImpl.java`, 242 lines) is a maintained fork of the JDK's internal
`sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl` (`@see` at `:35`) and the only portable way to *build* a
`ParameterizedType`: private constructor `:46-53`, factories `of(Class, Type[])` `:90` and `of(Class, Type[], Type ownerType)`
`:119`, defensive clone in `getActualTypeArguments()` `:140-142`, `toString()` `:208-241`, and `MalformedParameterizedTypeException`
on arity mismatch from `validateConstructorArguments` `:55-64`. `TypeArgument` (`reflect/generics/TypeArgument.java`, 88 lines) is the
immutable `(Type, int index)` pair the resolution algorithm passes around — constructor asserts `:38-43`, factory `create(Type, int)` `:85-87`.

### 3.8 `internal.reflect` — 1 type

`ConstantPoolUtils` (`internal/reflect/ConstantPoolUtils.java`, 263 lines) reads a class's constant pool through the JDK-internal
`ConstantPool`, resolving it **by name, old first**: `sun.reflect.ConstantPool` (`:51`) then `jdk.internal.reflect.ConstantPool`
(`:56`) at `:255-259`, exposing `CONSTANT_POOL_CLASS` `:62` and ~18 `Method` handles `:67-147` located via `MethodUtils.findMethod`.
`invoke(targetClass, method, args)` catches `Throwable`, logs at `trace` and returns `null` (`:240-249`), but `getConstantPool(...)`
runs **outside** that try (`:241` vs `:251-253`) so an access failure there propagates. This is deliberately the only `core` package
that reaches for `sun.*`, and it is namespaced `internal.*` to say so.

### 3.9 `beans` — 4 types

| Type | Role | Key facts |
|---|---|---|
| `BeanUtils` (576 lines) | facade: `Class` → `BeanMetadata`, bean → `Map` | `microsphere.bean.properties.max-resolved-depth` default `100` (`:91`, `:108`, `:113`, `:124`); `microsphere.bean.metadata.cache.size` default `64` (`:129`, `:134`, `:139`, `:149`); `beanMetadataCache` `:151`; `getBeanMetadata` `:285`; `findWriteMethod` `:321`; `findPropertyDescriptor` `:369` |
| `BeanMetadata` (152 lines) | immutable wrapper around `BeanInfo` | `Introspector.getBeanInfo(beanClass, Object.class)` inside `execute` at `:55`; uncapitalised, unmodifiable property map `:66-76`; `equals` **by bean class only** `:112-118`; `of` `:147-150` |
| `BeanProperty` (137 lines) | one property of one bean; mutable value, immutable identity | final identity fields `:39-49`; `of(Object, String)` `:122-136` |
| `ConfigurationProperty` (357 lines) | mutable model of a configuration property + nested `Metadata` | `name` final `:69`; `type` is a **`String`** set from a `Class` via `getTypeName` `:136-144`; `equals` over all seven fields `:177-190`; `Metadata.getSources()`/`getTargets()` lazy linked hash sets `:257-287` |

`resolvePropertiesAsMap` (`beans/BeanUtils.java`) edge cases: `bean == null` → `emptyMap()` (`:453-455`); depth ≥
`maxResolvedDepth` → `emptyMap()` for that sub-tree, silently (`:457-459`); a property with no read method → `null`
(`:476-478`); every readable property gets `trySetAccessible(readMethod)` **unconditionally**, regardless of `forceAccess`
(`:479`); the returned map is unmodifiable (`:471`).

---

## 4. Examples mined from the tests

### 4.1 `trySetAccessible` returns success; `setAccessible` returns the previous flag

Fixtures at `reflect/AccessibleObjectUtilsTest.java:51`, `:58`, `:63`; bodies at `:66-124`:

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

`tryCanAccessMethod` is `private` in the same module and `AbstractProcessor`'s constructor is `protected` in another named module; both succeed at `:106-107` while `canAccess` still says no until the flag is flipped (`:89-95`).

### 4.2 `JavaType`: the assertion block behind every promise in §3.5

`reflect/JavaTypeTest.java:63-67` declares `HashMap<Integer, List<String>> mapField` and `fromValue(HashMap<Integer, List<String>)`;
`:249-323` is the shared assertion block driven by `testFromMethodReturnType` (`:91-98`), `testFromMethodParameters` (`:110-119`) and `testFromField` (`:140-143`):

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

`:235-247` cross-checks the same type against Spring's `ResolvableType.forType(...).resolveGenerics()` — the reason
`spring-core` is test-scope (`microsphere-java-core/pom.xml:88-92`). `:100-152` pins the null contract:

```java
assertSame(NULL_JAVA_TYPE, fromMethodReturnType(getClass(), "fromValue", String.class));      // :102 no such method
assertSame(NULL_JAVA_TYPE, fromMethodReturnType(null));                                        // :105
assertSame(EMPTY_JAVA_TYPE_ARRAY, fromMethodParameters(getClass(), "fromValue", String.class)); // :123
assertEquals(NULL_JAVA_TYPE, fromField(getClass(), "notFoundField"));                          // :147
assertEquals(NULL_JAVA_TYPE, fromField(null));                                                 // :150
```

### 4.3 Generic type-variable resolution across a deep hierarchy

`reflect/TypeUtilsTest.java:750-781` exercises the private core directly on the project's generic fixtures:

```java
assertValues(doResolveActualTypeArguments(StringIntegerToBoolean.class, BiFunction.class), String.class, Integer.class, Boolean.class);   // :754
assertValues(doResolveActualTypeArguments(StringToIntegerConverter.class, Converter.class), String.class, Integer.class);                  // :760
assertValues(doResolveActualTypeArguments(B.class, Comparable.class), B.class);            // :763
assertValues(doResolveActualTypeArguments(C.class, Comparable.class), B.class);            // :766
assertValues(doResolveActualTypeArguments(D.class, C.class), String.class);                 // :769
assertValues(doResolveActualTypeArguments(E.class, Comparable.class), B.class);             // :772
assertValues(doResolveActualTypeArguments(StringIntegerBooleanHashMap.class, Map.class), String.class, Integer.class);                      // :775
```

`C`/`D`/`E` reaching `Comparable`'s type variable through several hops is the point: the answer is propagated from the supertype
by matching **type-variable names** (`reflect/TypeUtils.java:706-729`). The public API behaves identically (`reflect/TypeUtilsTest.java:330-348`),
`null` in either slot returns the shared empty list (`:784-787`), and `resolveTypeArguments(Class)` collapses to `emptyList()` for `null`
(`:721-723`), every primitive (`:725-730`) and every array class (`:732-737`).

### 4.4 Banned methods, cache clearing, and the two `findMethod` shapes

`reflect/MethodUtilsTest.java:115-126` plus `:173-199` show the mandatory hygiene for a cached lookup and both ban routes
(`BANNED_METHODS_PROPERTY_NAME` = `microsphere.reflect.banned-methods`, test `:133`, declaration `reflect/MethodUtils.java:121-125`);
`:395-482` documents the two `findMethod` shapes — the *no-parameter-type* form means "zero parameters", not "any overload":

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

And `:485-512` maps `invokeMethod`'s failure modes onto the table in §3.2:

```java
assertEquals(test, invokeMethod(test, "toString"));                                    // :487
assertArrayEquals(values, invokeMethod(true, values, "clone"));                         // :494 protected Object#clone on an array
assertThrows(IllegalArgumentException.class, () -> invokeMethod("test", (Method) null)); // :499
assertThrows(IllegalArgumentException.class, () -> invokeMethod("test", "notFound"));    // :500
assertThrows(IllegalArgumentException.class, () -> invokeMethod("test", method, "abc")); // :506 wrong arity
assertThrows(RuntimeException.class, () -> invokeMethod(new ReflectionTest(), "errorMethod")); // :511 unwrapped target
```

### 4.5 Field access, including the JDK-internal `String.value`

`reflect/FieldUtilsTest.java:184-201` is the accessibility stress test, written **per JDK** because the field's type changed in JDK 9 (`compact strings`):

```java
String value = "Hello,World";
if (CURRENT_JAVA_VERSION.le(JAVA_VERSION_8)) {
    assertArrayEquals(value.toCharArray(), getFieldValue(true, value, "value", char[].class)); // :188
} else {
    assertArrayEquals(value.getBytes(), getFieldValue(true, value, "value", byte[].class));     // :190
}
```

`:210-245` pins each failure mode, matching §3.3 exactly, and `:148-175` shows the throwing, deliberately
non-hierarchical `getDeclaredField` (a `RuntimeException` for a missing field **and** for every inherited one) next to
successful reads of JDK-internal statics (`getStaticFieldValue(System.class, "in")`):

```java
assertThrows(IllegalArgumentException.class, () -> getFieldValue(true, "test", field));      // :213 wrong owner
assertThrows(IllegalStateException.class, () -> getFieldValue(this, "value", String.class));  // :218 private field, forceAccess=false
assertThrows(NullPointerException.class, () -> getFieldValue(null, (String) null));          // :223
assertNull(setFieldValue(test, "notFoundField", null));                                       // :240
assertThrows(IllegalArgumentException.class, () -> setFieldValue(test, "privateField", 1));   // :245
assertThrows(RuntimeException.class, () -> getDeclaredField(ReflectionTestExt.class, "privateField")); // :160
```

### 4.6 Constructors: `find` returns `null`, `get` throws, `newInstance` throws

`reflect/ConstructorUtilsTest.java:59-106` isolates visibility as the only variable — four fixtures, each with one zero-arg
constructor at a different level. `:108-123` then maps the failures (unmatched parameter types → `IllegalArgumentException` `:110-112`;
private constructor without `forceAccess` → `IllegalStateException` `:117`; a throwing constructor → `RuntimeException` `:122`):

```java
assertEquals(2, findConstructors(PublicConstructorClass.class).size());                     // :61
assertEquals(0, findConstructors(ProtectedConstructorClass.class).size());                   // :62
assertEquals(1, findDeclaredConstructors(PrivateConstructorClass.class).size());             // :72
assertThrows(RuntimeException.class, () -> getConstructor(PrivateConstructorClass.class));   // :80
assertNotNull(findConstructor(PrivateConstructorClass.class));                                // :96
assertNotNull(newInstance(true, PrivateConstructorClass.class));                              // :102 forceAccess=true works
```

### 4.7 Building a `ParameterizedType` by hand

`reflect/generics/ParameterizedTypeImplTest.java:46-77`, `:147`:

```java
topLevelType = of(Map.class, actualTypeArguments);                     // :48  -> Map<String,Integer>
nestedType   = of(Map.Entry.class, actualTypeArguments, Map.class);    // :49  -> Map.Entry<String,Integer>
complexType  = of(Map.Entry.class, actualTypeArguments, topLevelType);  // :50
assertEquals(topLevelType, complexType.getOwnerType());                 // :76
assertThrows(MalformedParameterizedTypeException.class, () -> of(Map.class, new Type[]{String.class})); // :55 arity
assertEquals("java.lang.String", of(String.class).toString());           // :147
```

### 4.8 Bean → `Map`, and the depth guard

`beans/BeanUtilsTest.java:107-132` resolves a `ConfigurationProperty` (including its nested `Metadata`) into a map keyed by
uncapitalised property names. `:134-171` is the recursive case: a `TestBean` holding a `Data[]` comes back with each element itself
converted to a `Map<String, Object>` (`:163-170`), while non-POJO leaves stay untouched — `StringBuilder` (`:159`), `AtomicInteger` (`:160`),
`TimeUnit` (`:161`), `Class` (`:162`). `:67-192` covers the edges:

```java
assertSame(emptyMap(), resolvePropertiesAsMap(null));                                        // :175
assertThrows(RuntimeException.class, () -> resolvePropertiesAsMap(new Object()));             // :181
assertNotNull(resolvePropertiesAsMap(new FileChangedEvent(newRandomTempFile(), MODIFIED)));   // :186 depth guard fires, result still valid
assertSame(beanMetadata, getBeanMetadata(TestBean.class));                                    // :71 cached identity
assertNull(findPropertyDescriptor(beanMetadata, "notFound"));                                  // :85
assertNull(findWriteMethod(beanMetadata, "booleanValue"));                                    // :102 read-only property
```

`beans/BeanPropertyTest.java:59-118` shows `BeanProperty.of` and what its `equals` covers:

```java
beanProperty = of(this, "value");                             // :61
assertEquals(TEST_VALUE, beanProperty.getValue());            // :66  read eagerly via the getter
beanProperty.setValue("new-value"); assertEquals("new-value", beanProperty.getValue()); // :67-68 value is mutable
assertEquals(BeanPropertyTest.class, beanProperty.getBeanClass());                       // :78 exact class; anonymous subclass differs :105-107
assertNotEquals(this.beanProperty, of(bean, "value"));                                     // :103 different *value* -> not equal
```

---

## 5. Compatibility matrix

| Capability | JDK 8 | JDK 9–15 | JDK 16–17 | JDK 21 / 25 | Evidence (`release`) |
|---|---|---|---|---|---|
| `trySetAccessible` on your own classes | ✅ via `setAccessible(true)` | ✅ via handle | ✅ via handle | ✅ via handle | `reflect/AccessibleObjectUtils.java:97-104`, `:170-178` |
| `trySetAccessible` on JDK-internal members (`String.value`, `AbstractProcessor.<init>`) | ✅ | ✅ | ⚠️ returns `false`, logs the JEP 396 hint, **does not throw** | ⚠️ same | `:157-168`, `:196-211`; `reflect/AccessibleObjectUtilsTest.java:106-107` |
| `canAccess(o, ao)` exact semantics | ⚠️ approximated by `isAccessible()` | ✅ | ✅ | ✅ | `:142-149`, `:184-194` |
| `InaccessibleObjectException` recognised | ✅ by class-**name** comparison | ✅ | ✅ | ✅ | `reflect/ReflectionUtils.java:165`, `:537-538` |
| Caller-class detection | ✅ `sun.reflect.Reflection` | ⚠️ method removed; `isSupportedSunReflectReflection()` is `false` | ⚠️ same | ✅ `StackWalker` path | `reflect/ReflectionUtils.java:79`, `:90`, `:100`, `:117`, `:124`, `:168-181`; `reflect/ReflectionUtilsTest.java:177-185` |
| `StackWalker` handles populated | ❌ `null` | ✅ | ✅ | ✅ | `reflect/ReflectionUtils.java:117`, `:124`, `:184-200`; `reflect/ReflectionUtilsTest.java:166-175` and `:92-98` |
| `ConstantPoolUtils` reads | ✅ `sun.reflect.ConstantPool` | ✅ `jdk.internal.reflect.ConstantPool` | ⚠️ needs the pool to remain reachable; failures log at `trace` | ⚠️ same | `internal/reflect/ConstantPoolUtils.java:255-259`, `:240-249` |
| `JavaType` / `TypeUtils` / `ParameterizedTypeImpl` | ✅ | ✅ | ✅ | ✅ | pure `java.lang.reflect.Type` API, no internal access |
| `BeanUtils.resolvePropertiesAsMap` on JDK-internal beans | ✅ | ✅ | ⚠️ depends on §2.1 | ⚠️ same | `beans/BeanUtils.java:479` (unconditional `trySetAccessible`) |

**When you need `--add-opens`.** Only when you *force* access to a member of a JDK package that does not open it:
`getFieldValue`/`setFieldValue`/`invokeMethod` with `forceAccess = true` against `java.base` internals, or
`BeanUtils.resolvePropertiesAsMap` against such a bean. The library prints the exact flag at
`reflect/AccessibleObjectUtils.java:204-208`: `It's require to add JVM Options '--add-opens=<module>/<package>=ALL-UNNAMED' for running`.
For this group that is usually `--add-opens=java.base/java.lang=ALL-UNNAMED` (`String.value`, `Class.getConstantPool`) or
`--add-opens=java.base/java.lang.invoke=ALL-UNNAMED`. The reactor build does **not** add them — they live in the external `microsphere-build` parent (`pom.xml:8`), so a consuming project must.

---

## 6. Pitfalls and known limits

1. **`findMethod(Class, name)` with no parameter types does not match overloads.** It means "a zero-parameter method
   of that name" (`reflect/MethodUtils.java:629-632`; `reflect/MethodUtilsTest.java:395-423`).
2. **Banned methods make `findMethod` return `null`, silently.** `microsphere.reflect.banned-methods`
   (`reflect/MethodUtils.java:121-125`, applied by `initBannedMethods()` `:215-235`) short-circuits at `:631`; any
   test touching it must clear all three caches afterwards (`reflect/MethodUtilsTest.java:120-126`).
3. **Read and write failures are wrapped differently** — `IllegalStateException` vs `IllegalArgumentException`
   (`reflect/FieldUtils.java:830-831`, `:1030-1031`). Catch `RuntimeException` to be safe.
4. **Two `JavaType` factories dereference a `null` method.** `fromMethodParameters(Method)` `:974` and
   `fromMethodParameter(Method, int)` `:1005` lack the guards the `Class`-based overloads have (`:911`, `:943`).
5. **`JavaType.equals` includes the `source`.** Two `JavaType`s over the same `Type` can be unequal when one came from
   `as(...)` (`reflect/JavaType.java:454-465`, `:771-783`; `reflect/JavaTypeTest.java:276`). Compare `getType()`.
6. **`BeanMetadata.equals` ignores the properties** — equality is by bean class alone (`beans/BeanMetadata.java:112-118`).
7. **`resolvePropertiesAsMap` truncates silently.** At `microsphere.bean.properties.max-resolved-depth` (default 100,
   `beans/BeanUtils.java:108`, `:124`) the branch returns `emptyMap()` (`:457-459`); see
   `beans/BeanUtilsTest.java:185-192` for the resulting partial-but-valid map.
8. **`BeanUtils` always forces access on getters** (`beans/BeanUtils.java:479`), so §2.1 log noise can appear for
   JDK-internal beans with no opt-out.
9. **`getTypeNames(Type...)` throws on `null` elements** (`reflect/TypeUtils.java:1568-1574`) — the exception to the group's neutral-value degradation policy.
10. **Two internal limits worth knowing.** `ParameterizedTypeImpl`'s bounds loop in `validateConstructorArguments` is
    empty (`reflect/generics/ParameterizedTypeImpl.java:61-63`), so only arity is validated (`:55-64`); and
    `ConstantPoolUtils` calls `getConstantPool(...)` **outside** its `catch (Throwable)`
    (`internal/reflect/ConstantPoolUtils.java:241` vs `:243-247`), so an unreachable `Class.getConstantPool` throws instead of
    returning `null`. `reflect/Modifier.java` is only an enum wrapper over the `int` bit masks (`matchesAny`/`matchesAll`, `:402-437`) and caches nothing.

---

## 7. See also

* [Language abstractions](language-abstractions.md) — `Version` / `Deprecation` (definition family),
  `MutableInteger` (bean depth guard), `ThrowableSupplier.execute` (behind `getConstructor`).
* [Type conversion](type-conversion.md) — consumes `JavaType` / `TypeUtils` to pick converters.
* [Class loading and artifacts](classloading-and-artifacts.md) — the other JDK-internals consumer; same `--add-opens` story.
* [Utilities and constants](util-and-constants.md) — `Utils` marker, `Assert`, and the `MICROSPHERE_PROPERTY_NAME_PREFIX` these packages build their system-property names from.
* Developer guide, same slice: [../../developer-guide/microsphere-java-core/reflection-and-beans.md](../../developer-guide/microsphere-java-core/reflection-and-beans.md) — accessibility decision tree, generic-resolution recursion, cache inventory.
* Group index [README.md](README.md) · module index [../../developer-guide/microsphere-java-core/README.md](../../developer-guide/microsphere-java-core/README.md)
