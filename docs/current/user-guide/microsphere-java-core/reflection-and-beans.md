# Reflection, types and beans

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-java-core` (`0.3.20-SNAPSHOT`, `release` branch) |
| **Packages** | `io.microsphere.reflect` — 19 main-source types · `io.microsphere.reflect.generics` — 2 · `io.microsphere.internal.reflect` — 1 · `io.microsphere.beans` — 4 (26 total) |
| **JDK level** | Compiled at Java 8; CI-tested on JDK 8 / 11 / 17 / 21 / 25 |
| **Depends on other `core` packages** | `util` (`ArrayUtils`, `StringUtils`, `Assert`, `ClassUtils`, `Utils` marker), `lang` (`Version`, `Deprecation`, `MutableInteger`), `collection` (`ListUtils`, `Maps`, `SetUtils`), `logging`, `io.event` (`ConfigurationProperty` annotation) |
| **External dependencies** | none — pure JDK (`javax.annotation-api`, `slf4j-api`, `commons-logging` are optional and unrelated here) |
| **Used by** | `convert`, `classloading`, `io.scanner`, `metadata`, `json`, `net`, `management` — i.e. this is the group almost everything else leans on |

All `file:line` references in this page are relative to `microsphere-java-core/src/main/java/io/microsphere/`
(main sources) or `microsphere-java-core/src/test/java/io/microsphere/` (tests), read from the `release` worktree.
After the first mention they are shortened to `reflect/Foo.java:40-55`, `beans/Foo.java:40-55`,
`internal/reflect/Foo.java:40-55`. Every one of the 26 types carries the javadoc `@since 1.0.0` tag — there is no
`@Since` **annotation** usage anywhere in `reflect`, `reflect.generics`, `internal.reflect` or `beans`.

---

## 1. What this group is for

**Reach for `io.microsphere.reflect` whenever you write code that has to survive a JDK upgrade.**
Plain `java.lang.reflect` gives you two sharp edges: `setAccessible(true)` throws
`InaccessibleObjectException` on JDK 16+ (JEP 396), and `java.lang.reflect.Type` is an interface hierarchy you
cannot ask sensible questions of (`Class<? extends List<String>>`? generic supertype? is this a wildcard?). This
group answers both. `AccessibleObjectUtils.trySetAccessible(...)` (`reflect/AccessibleObjectUtils.java:97-104`)
picks the right mechanism per JDK and never throws, and `JavaType` (`reflect/JavaType.java:92-1328`) wraps any
`Type` with memoised `getSuperType()` / `getInterfaces()` / `getGenericTypes()`.

**Reach for `io.microsphere.beans` when you need "give me this object as a `Map<String, Object>`".**
`BeanUtils.resolvePropertiesAsMap(bean)` (`beans/BeanUtils.java:204`) walks JavaBeans getters recursively into an
unmodifiable map, with a depth guard so a self-referencing bean cannot blow the stack (`:452-472`, `:457`).

**Reach for the definition family (`ReflectiveDefinition` → `MemberDefinition` → `ExecutableDefinition` →
`MethodDefinition` / `ConstructorDefinition`, plus `FieldDefinition` and `ClassDefinition`) when the method you want
to call may not exist on the JVM you land on.** A definition is a `Serializable` description of a member ("since
version X, declared in class Y, named Z") that resolves lazily, so `isPresent()` tells you whether the member exists
on this JVM (`reflect/ReflectiveDefinition.java:91-106`, `reflect/MemberDefinition.java:166-177`).

> [!IMPORTANT]
> This is the **only** layer in `core` that is allowed to touch JDK reflection internals. Every internal-access path
> in it has a documented fallback (see §5). Do not add `setAccessible` calls to other packages — route them here.

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
| `artifactId` | `microsphere-java-core` | `microsphere-java-core/pom.xml:14` |
| Version | `${revision}`, flattened; `0.3.20-SNAPSHOT` on `release` | `pom.xml:14`, `:54`; `microsphere-java-core/pom.xml:15` |
| Newest published tag | `0.3.20` | `docs/current/user-guide/microsphere-java-core/README.md:70` |
| Java level | 8 (sources must build on JDK 8) | CI matrix `java: [ '8', '11', '17', '21', '25' ]`, `.github/workflows/maven-build.yml:25`, `distribution: 'temurin'` `:33` |
| CI surefire flag | `-Dsurefire.useSystemClassLoader=false` | `.github/workflows/maven-build.yml:43` |
| Spring Framework (test-only, used by `JavaTypeTest`) | `5.3.39` on `[1.8,17)`, newer line otherwise | `microsphere-java-parent/pom.xml:54-62`; test-scope dep `microsphere-java-core/pom.xml:88-92` |
| Version stamp on all 26 types | javadoc `@since 1.0.0` | e.g. `reflect/AccessibleObjectUtils.java:42`, `reflect/JavaType.java:90`, `beans/BeanUtils.java:85`, `internal/reflect/ConstantPoolUtils.java:42` |

### 2.1 The accessibility matrix (the centrepiece)

`AccessibleObjectUtils` decides *by capability, not by version number*: at class-initialisation time it looks up the
two JDK 9 methods through `MethodHandle`s and stores `null` when they do not exist
(`reflect/AccessibleObjectUtils.java:62` for `canAccess`, `:68` for `trySetAccessible`). Everything below is
derived from whether those handles are non-`null`.

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

| Method | `null` argument | Throws | Caching |
|---|---|---|---|
| `trySetAccessible(ao)` | On JDK 9+ the handle call fails and `false` is returned (`:170-178`); on JDK 8 `ao.isAccessible()` raises NPE from inside, which is caught as `Throwable` only on the handle path | none — `catch (Throwable)` at `:174` | the two `MethodHandle`s are `static final`, resolved once (`:62`, `:68`) |
| `canAccess(o, ao)` | `isPublic(null)` is treated as non-public by `MemberUtils`, so the handle path runs | none, unless `ao` itself is `null` on JDK 8 where `accessibleObject.isAccessible()` at `:148` NPEs | — |
| `setAccessible(ao)` (pkg-private) | NPE from `:158`, **not** caught (only `RuntimeException` from `:161` is) | none declared | — |

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

The by-name overloads (`invokeMethod(Object, String, Object...)` and friends) do not return "nothing found" — they
throw `IllegalArgumentException` when no method matches (`:836-840`).

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
> `IllegalArgumentException`. Several `setFieldValue` javadocs additionally list `IllegalAccessException` in
> `@throws` (e.g. `:876-878`, `:966-968`) but the implementation catches it at `:1030-1031`, so that checked
> exception never escapes the method.

### 3.4 `ConstructorUtils` — `reflect/ConstructorUtils.java`

Two philosophies, split by verb, exactly as `MethodUtils` does it:

| Verb | Not found | Throws declared | Line |
|---|---|---|---|
| `getConstructor` / `getDeclaredConstructor` | — | translated through `ExecutableUtils.execute(...)` into a `RuntimeException` | `:205-231` |
| `findConstructor` / `findConstructors` / `findDeclaredConstructors` | returns `null` / empty list (`NOT_FOUND_CONSTRUCTOR = null`, `:53`) | none | `:264-271` |
| `newInstance(Class, Object...)` | `IllegalArgumentException` ("no matching constructor") | `IllegalArgumentException` | `:324-332` |
| `newInstance(boolean forceAccess, Constructor, Object...)` | — | `IllegalStateException` on access failure, `RuntimeException` unwrapped from the target | `:393-411` |

`ExecutableUtils` (`reflect/ExecutableUtils.java`) is the shared exception translator: `execute` overloads at `:100`,
`:157`, `:215`; the mapping itself at `:225-237`; parameter-type matching (`assignable` / `equivalent` / `non-null`
modes) at `:249-266`.

### 3.5 `JavaType` — `reflect/JavaType.java` (1329 lines)

```java
public final class JavaType implements Serializable {     // identity fields :112-116, volatile caches :120-124
    public static final JavaType[] EMPTY_JAVA_TYPE_ARRAY = new JavaType[0];        // :98
    public static final JavaType OBJECT_JAVA_TYPE = from(Object.class, CLASS);      // :104
    public static final JavaType NULL_JAVA_TYPE = from(null, UNKNOWN);              // :110
    @Nullable public JavaType getSuperType()      // :311-318  memoised
    @Nonnull  public JavaType[] getInterfaces()   // :341-349  memoised
    @Nonnull  public JavaType[] getGenericTypes() // :397-405  memoised
    public JavaType as(Class<?> targetClass)       // :454-465  walks supertypes :1096-1121 then interfaces :1123-1138
    public boolean equals(Object o) / hashCode()   // :771-783 — identity = (type, kind, source)
}
```

Factories and their `null` behaviour — this table is the part people get wrong:

| Factory | On `null` / not found | Line |
|---|---|---|
| `fromField(Field)` | `NULL_JAVA_TYPE` | `:849` |
| `fromField(Class, String)` | `NULL_JAVA_TYPE` (no field → `null` field → `NULL_JAVA_TYPE`) | `:818` region, verified by test |
| `fromMethodReturnType(Method)` | `NULL_JAVA_TYPE` | `:911` |
| `fromMethodParameters(Method)` | dereferences the method — **no** null guard | `:974` |
| `fromMethodParameter(Method, int)` | dereferences the method — **no** null guard | `:1005` |
| `fromMethodParameters(Class, String, Class...)` | `EMPTY_JAVA_TYPE_ARRAY` when the method is not found | `:943` |

`JavaType.Kind` (`:1162-1327`) is an internal strategy enum — one constant per `java.lang.reflect.Type` shape:
`CLASS` `:1167`, `PARAMETERIZED_TYPE` `:1201`, `TYPE_VARIABLE` `:1265`, `WILDCARD_TYPE` `:1270`,
`GENERIC_ARRAY_TYPE` `:1275`, `UNKNOWN` `:1280`. Selection is first-match: `valueOf(Type)` (`:1316-1325`) asks each
constant `isType(Type)` in declaration order and returns the first `true`.

### 3.6 `TypeUtils` — `reflect/TypeUtils.java` (1579 lines)

The generic-resolution engine, and the owner of the second-biggest cache in the group:

| Item | Value | Line |
|---|---|---|
| Cache-size property name | `microsphere.reflect.resolved-generic-types.cache.size` | `:93` |
| Default | `"256"` (`:98`), parsed at `:103`, read via `getInteger` at `:114`, annotated `@ConfigurationProperty` at `:108-113` | |
| Cache field | `private static final ConcurrentMap<MultipleType, List<Type>> resolvedGenericTypesCache = newConcurrentHashMap(RESOLVED_GENERIC_TYPES_CACHE_SIZE)` | `:116` |
| Core resolution | `protected static List<Type> doResolveActualTypeArguments(Type type, Class baseClass)` | `:581-632` |
| Public entry | `resolveActualTypeArguments(Type, Class)` wraps the result in `unmodifiableList` | `:575-579` |

The `findTypes` family is one protected method plus five flag-tuned public wrappers
(`protected static List<Type> findTypes(Type, includeSelf, includeHierarchicalTypes, includeGenericSuperclass,
includeGenericInterfaces, Predicate...)` at `:1092-1101`; `null` or `Object` type → `emptyList()` at `:1097-1099`):

| Public method | Line | self | hierarchical | generic superclass | generic interfaces |
|---|---|---|---|---|---|
| `findAllGenericSuperclasses` | `:935` (calls `:936`) | no | yes | yes | no |
| `findAllGenericInterfaces` | `:962` (calls `:963`) | no | yes | no | yes |
| `findParameterizedTypes` | `:991` (calls `:992`) | yes | no | yes | yes |
| `findAllParameterizedTypes` | `:1019` → delegates to `findAllTypes` at `:1020` | (as `findAllTypes`) | | | |
| `findHierarchicalTypes` | `:1049` (calls `:1050`) | no | yes | yes | yes |
| `findAllTypes` | `:1083` (calls `:1084`) | yes | yes | yes | yes |

Also useful: `getRawType` `:311`, `getRawClass` `:341`, `asClass` `:1299-1308`, `getComponentType` `:1509-1517`, and
`getTypeNames(Type...)` `:1568-1574` which is the rare **throwing** case — it calls `assertNoNullElements`.

### 3.7 `reflect.generics` — 2 types

`ParameterizedTypeImpl` (`reflect/generics/ParameterizedTypeImpl.java`, 242 lines) is a maintained fork of the JDK's
internal `sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl` (`@see` at `:35`). It is the only way to
*build* a `ParameterizedType` portably. Private constructor `:46-53`; factories `of(Class, Type[])` `:90` and
`of(Class, Type[], Type ownerType)` `:119`; `getActualTypeArguments()` defensively clones `:140-142`; `toString()`
`:208-241`. Arity mismatch raises `MalformedParameterizedTypeException` from
`validateConstructorArguments` `:55-64`.

`TypeArgument` (`reflect/generics/TypeArgument.java`, 88 lines) is the immutable `(Type, int index)` pair the
resolution algorithm passes around; the constructor asserts both arguments `:38-43` and `create(Type, int)` is the
factory `:85-87`.

### 3.8 `internal.reflect` — 1 type

`ConstantPoolUtils` (`internal/reflect/ConstantPoolUtils.java`, 263 lines) reads a class's constant pool through the
JDK-internal `ConstantPool` object. It resolves the class **by name, old first**: `sun.reflect.ConstantPool` then
`jdk.internal.reflect.ConstantPool` (`:255-259`, constants `:51` and `:56`), exposing `CONSTANT_POOL_CLASS` `:62`
and ~18 `Method` handles `:67-147` found through `MethodUtils.findMethod`.

| Guard | Behaviour | Line |
|---|---|---|
| `invoke(targetClass, method, args)` | `catch (Throwable)` → logs at `trace`, returns `null` | `:240-249` |
| `getConstantPool(targetClass)` | **outside** the try block — an access failure here propagates | `:241` vs `:251-253` |

This is deliberately the only package in `core` that reaches for `sun.*`, and it is namespaced `internal.*` to say so.

### 3.9 `beans` — 4 types

| Type | Role | Key facts |
|---|---|---|
| `BeanUtils` (`beans/BeanUtils.java`, 576 lines) | facade: `Class` → `BeanMetadata`, bean → `Map` | max-resolved-depth property `microsphere.bean.properties.max-resolved-depth`, default `100` (`:91`, `:108`, `:113`, `:124`); metadata cache size property `microsphere.bean.metadata.cache.size`, default `64` (`:129`, `:134`, `:139`, `:149`); `beanMetadataCache` `:151`; `getBeanMetadata` `:285`; `findWriteMethod` `:321`; `findPropertyDescriptor` `:369` |
| `BeanMetadata` (`beans/BeanMetadata.java`, 152 lines) | immutable wrapper around `BeanInfo` | `Introspector.getBeanInfo(beanClass, Object.class)` inside `execute` at `:55`; property map built with uncapitalised keys and wrapped unmodifiable `:66-76`; `equals` is **by bean class only** `:112-118`; factory `of` `:147-150` |
| `BeanProperty` (`beans/BeanProperty.java`, 137 lines) | one named property of one bean; mutable value, immutable identity | identity fields final `:39-49`; `of(Object, String)` `:122-136` |
| `ConfigurationProperty` (`beans/ConfigurationProperty.java`, 357 lines) | plain mutable model of a configuration property + nested `Metadata` | `name` final `:69`; `type` stored as a **`String`**, set from a `Class` via `getTypeName` `:136-144`; `equals` covers all seven fields `:177-190`; `Metadata.getSources()`/`getTargets()` lazily create linked hash sets `:257-287` |

`resolvePropertiesAsMap` details you will actually hit:

| Situation | Result | Line |
|---|---|---|
| `bean == null` | `emptyMap()` (the shared instance) | `:453-455` |
| recursion depth ≥ `maxResolvedDepth` | `emptyMap()` for that sub-tree — the walk stops silently | `:457-459` |
| property with no read method | value `null` | `:476-478` |
| every readable property | `trySetAccessible(readMethod)` is called **unconditionally**, regardless of `forceAccess` | `:479` |
| result map | `unmodifiableMap` | `:471` |

---

## 4. Examples mined from the tests

### 4.1 `trySetAccessible` returns success; `setAccessible` returns the previous flag

`reflect/AccessibleObjectUtilsTest.java:66-77` and `:98-113`:

```java
private static final Method[] methods = String.class.getMethods();                       // :51
private static final Method tryCanAccessMethod =
        findMethod(AccessibleObjectUtils.class, "tryCanAccess", Object.class, AccessibleObject.class); // :58
private static final Constructor abstractProcessorConstructor =
        findConstructor(AbstractProcessor.class);                                        // :63

@Test
void testSetAccessible() {
    for (Method method : methods) {
        assertEquals(method.isAccessible(), setAccessible(method));   // previous status, not new status
    }
}

@Test
void testTrySetAccessibleOnNonPublicMembers() {
    assertTrue(trySetAccessible(tryCanAccessMethod));        // private method of an in-project class
    assertTrue(trySetAccessible(abstractProcessorConstructor)); // protected ctor of java.compiler
}

@Test
void testTrySetAccessibleOnFailed() {
    assertFalse(trySetAccessible(null, null));               // handle path swallows the failure -> false
}
```

The two "non-public member" cases are the interesting ones: `tryCanAccessMethod` is `private` in the same module,
`AbstractProcessor`'s constructor is `protected` in another named module — both succeed on `:106-107`, while
`canAccess` still says no until you actually flip the flag (`:89-95`, including
`assertFalse(canAccess(test, cloneMethod))` for `Object#clone`). And `handleInaccessibleObjectExceptionIfFound` is
asserted **not** to throw for an unrelated `RuntimeException` (`:122-124`).

### 4.2 `JavaType`: one assertion block, everything the model promises

`reflect/JavaTypeTest.java:63-67` declares the fixtures and `:249-323` is the assertion block used by
`testFromMethodReturnType`, `testFromMethodParameters` and `testFromField`:

```java
private static HashMap<Integer, List<String>> mapField;                                   // :63
private static HashMap<Integer, List<String>> fromValue(HashMap<Integer, List<String>> v) { return v; } // :65

JavaType javaType = fromMethodReturnType(getClass(), "fromValue", HashMap.class);          // :92
assertEquals(Kind.PARAMETERIZED_TYPE, javaType.getKind());                                 // :251
assertNull(javaType.getSource());                                                          // :256
assertTrue(javaType.isRootSource());                                                       // :259
assertEquals(HashMap.class, javaType.toClass());                                           // :262
assertEquals(3, javaType.getInterfaces().length);   // Map, Cloneable, Serializable         // :264-268

JavaType cloneable = javaType.as(Cloneable.class);                                         // :271
assertEquals(CLASS, cloneable.getKind());
assertEquals(javaType, cloneable.getSource());     // `as` records where you came from       // :276
assertEquals(0, cloneable.getGenericTypes().length);                                       // :279

JavaType mapType = javaType.as(Map.class);                                                 // :282
assertEquals(2, mapType.getGenericTypes().length);                                         // :290
assertEquals(Integer.class, mapType.getGenericType(0).toClass());                          // :291
```

The cross-check at `:235-247` resolves the same type with Spring's
`ResolvableType.forType(...).resolveGenerics()` and asserts the two agree — the reason `spring-core` is a test-scope
dependency (`microsphere-java-core/pom.xml:88-92`). `:100-152` pins the null contract from §3.5:

```java
assertSame(NULL_JAVA_TYPE, fromMethodReturnType(getClass(), "fromValue", String.class));      // :102 no such method
assertSame(NULL_JAVA_TYPE, fromMethodReturnType(null));                                        // :105
assertSame(EMPTY_JAVA_TYPE_ARRAY, fromMethodParameters(getClass(), "fromValue", String.class)); // :123
assertEquals(NULL_JAVA_TYPE, fromField(getClass(), "notFoundField")); assertEquals(NULL_JAVA_TYPE, fromField(null)); // :147,:150
```

### 4.3 Generic type-variable resolution across a deep hierarchy

`reflect/TypeUtilsTest.java:750-781` exercises the private core directly, using the project's own generic fixtures:

```java
assertValues(doResolveActualTypeArguments(StringIntegerToBoolean.class, BiFunction.class),
        String.class, Integer.class, Boolean.class);                                       // :754
assertValues(doResolveActualTypeArguments(StringToIntegerConverter.class, Converter.class),
        String.class, Integer.class);                                                      // :760
assertValues(doResolveActualTypeArguments(B.class, Comparable.class), B.class);            // :763
assertValues(doResolveActualTypeArguments(C.class, Comparable.class), B.class);            // :766
assertValues(doResolveActualTypeArguments(D.class, C.class), String.class);                // :769
assertValues(doResolveActualTypeArguments(E.class, Comparable.class), B.class);            // :772
assertValues(doResolveActualTypeArguments(StringIntegerBooleanHashMap.class, Map.class),
        String.class, Integer.class);                                                      // :775
```

`C`/`D`/`E` reaching `Comparable`'s type variable through several hops is the whole point: the answer is propagated
from the supertype by matching **type-variable names** (`reflect/TypeUtils.java:706-729`). The public API behaves
identically (`reflect/TypeUtilsTest.java:330-348`), `null` in either slot returns the shared empty list (`:784-787`),
and `resolveTypeArguments(Class)` collapses to `emptyList()` for `null` (`:721-723`), every primitive (`:725-730`)
and every array class (`:732-737`).

### 4.4 Banned methods, cache clearing, and the two `findMethod` shapes

`reflect/MethodUtilsTest.java:115-126` plus `:173-194` show the mandatory hygiene for a cached lookup and both ban
routes — `BANNED_METHODS_PROPERTY_NAME` is `microsphere.reflect.banned-methods` (`:133`; declaration
`reflect/MethodUtils.java:121-125`):

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
```

`:395-482` documents the two `findMethod` shapes precisely — the *no-parameter-type* form means "zero parameters",
not "any overload":

```java
assertNotNull(findMethod(Object.class, "getClass"));                 // :397
assertNull(findMethod(Object.class, "equals"));                      // :408 — no zero-arg equals exists
assertNotNull(findMethod(Object.class, "equals", Object.class));     // :413
assertNull(findMethod(Object.class, "equals", int.class));           // :420
assertNull(findMethod(int.class, "equals", int.class));              // :481 — primitive classes have no members
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

`reflect/FieldUtilsTest.java:184-201` is the accessibility stress test, written **per JDK** because the field's type
changed in JDK 9 (`compact strings`):

```java
String value = "Hello,World";
if (CURRENT_JAVA_VERSION.le(JAVA_VERSION_8)) {
    assertArrayEquals(value.toCharArray(), getFieldValue(true, value, "value", char[].class)); // :188
} else {
    assertArrayEquals(value.getBytes(), getFieldValue(true, value, "value", byte[].class));     // :190
}
```

`:210-245` pins each failure mode, matching §3.3 exactly:

```java
assertThrows(IllegalArgumentException.class, () -> getFieldValue(true, "test", field));   // :213 wrong owner
assertThrows(IllegalStateException.class, () -> getFieldValue(this, "value", String.class)); // :218 private field, forceAccess=false
assertThrows(NullPointerException.class, () -> getFieldValue(null, (String) null));        // :223
assertNull(setFieldValue(test, "notFoundField", null));                                     // :240
assertThrows(IllegalArgumentException.class, () -> setFieldValue(test, "privateField", 1)); // :245
```

`getDeclaredField` is the throwing counterpart: `:148-166` asserts `RuntimeException` for a missing field **and** for
every inherited one, because it is deliberately non-hierarchical; `:169-175` reads JDK-internal statics successfully
(`getStaticFieldValue(System.class, "in")`).

### 4.6 Constructors: `find` returns `null`, `get` throws, `newInstance` throws

`reflect/ConstructorUtilsTest.java:59-106` isolates visibility as the only variable — four fixtures, each with one
zero-arg constructor at a different level:

```java
assertEquals(2, findConstructors(PublicConstructorClass.class).size());                     // :61
assertEquals(0, findConstructors(ProtectedConstructorClass.class).size());                  // :62
assertEquals(1, findDeclaredConstructors(PrivateConstructorClass.class).size());            // :72
assertThrows(RuntimeException.class, () -> getConstructor(PrivateConstructorClass.class));  // :80
assertNotNull(findConstructor(PrivateConstructorClass.class));                               // :96
assertNotNull(newInstance(true, PrivateConstructorClass.class));                             // :102 forceAccess=true works
```

`:108-123` finishes the mapping: unmatched parameter types → `IllegalArgumentException` (`:110-112`), private
constructor without `forceAccess` → `IllegalStateException` (`:117`), a throwing constructor → `RuntimeException`
(`:122`).

### 4.7 Building a `ParameterizedType` by hand

`reflect/generics/ParameterizedTypeImplTest.java:46-77`, `:147`:

```java
topLevelType = of(Map.class, actualTypeArguments);                    // :48  -> Map<String,Integer>
nestedType   = of(Map.Entry.class, actualTypeArguments, Map.class);    // :49  -> Map.Entry<String,Integer>
complexType  = of(Map.Entry.class, actualTypeArguments, topLevelType); // :50
assertEquals(topLevelType, complexType.getOwnerType());                // :76
assertThrows(MalformedParameterizedTypeException.class, () -> of(Map.class, new Type[]{String.class})); // :55 arity
assertEquals("java.lang.String", of(String.class).toString());          // :147
```

### 4.8 Bean → `Map`, and the depth guard

`beans/BeanUtilsTest.java:107-132` resolves a `ConfigurationProperty` (including its nested `Metadata`) into a map
and asserts the uncapitalised property keys. `:134-171` is the recursive case: a `TestBean` holding a `Data[]` comes
back with each array element itself converted to a `Map<String, Object>` (`:163-170`), while non-POJO leaves stay
untouched — `StringBuilder` (`:159`), `AtomicInteger` (`:160`), `TimeUnit` (`:161`) and `Class` (`:162`). `:67-192`
covers the edges:

```java
assertSame(emptyMap(), resolvePropertiesAsMap(null));                                       // :175
assertThrows(RuntimeException.class, () -> resolvePropertiesAsMap(new Object()));            // :181
assertNotNull(resolvePropertiesAsMap(new FileChangedEvent(newRandomTempFile(), MODIFIED)));  // :186 depth guard fires, result still valid
assertSame(beanMetadata, getBeanMetadata(TestBean.class));                                   // :71 cached identity
assertNull(findPropertyDescriptor(beanMetadata, "notFound"));                                // :85
assertNull(findWriteMethod(beanMetadata, "booleanValue"));                                   // :102 read-only property
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
`reflect/AccessibleObjectUtils.java:204-208`:
`It's require to add JVM Options '--add-opens=<module>/<package>=ALL-UNNAMED' for running`. For this group that is
usually `--add-opens=java.base/java.lang=ALL-UNNAMED` (`String.value`, `Class.getConstantPool`) or
`--add-opens=java.base/java.lang.invoke=ALL-UNNAMED`. The reactor build does **not** add them — they live in the
external `microsphere-build` parent (`pom.xml:8`), so a consuming project must.

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
9. **`getTypeNames(Type...)` throws on `null` elements** (`reflect/TypeUtils.java:1568-1574`) — the exception to the
   group's neutral-value degradation policy.
10. **Two internal limits worth knowing.** `ParameterizedTypeImpl`'s bounds loop in `validateConstructorArguments` is
    empty (`reflect/generics/ParameterizedTypeImpl.java:61-63`), so only arity is validated (`:55-64`); and
    `ConstantPoolUtils` calls `getConstantPool(...)` **outside** its `catch (Throwable)`
    (`internal/reflect/ConstantPoolUtils.java:241` vs `:243-247`), so an unreachable `Class.getConstantPool` throws
    instead of returning `null`. `reflect/Modifier.java` is only an enum wrapper over the `int` bit masks
    (`matchesAny`/`matchesAll`, `:402-437`) and caches nothing.

---

## 7. See also

* [Language abstractions](language-abstractions.md) — `Version` / `Deprecation` (used by the definition family),
  `MutableInteger` (used by the bean depth guard), `Prioritized`, `ThrowableSupplier.execute` (the exception wrapper
  behind `getConstructor`).
* [Type conversion](type-conversion.md) — consumes `JavaType` / `TypeUtils` to pick converters.
* [Class loading and artifacts](classloading-and-artifacts.md) — the other JDK-internals consumer; shares the
  `--add-opens` story.
* [Utilities and constants](util-and-constants.md) — `Utils` marker, `Assert`, `ArrayUtils`, `StringUtils`, the
  `MICROSPHERE_PROPERTY_NAME_PREFIX` these packages build their system-property names from.
* Developer guide, same slice: [../../developer-guide/microsphere-java-core/reflection-and-beans.md](../../developer-guide/microsphere-java-core/reflection-and-beans.md) —
  the accessibility decision tree, the generic-resolution recursion, and the cache inventory.
* Group index: [README.md](README.md) · Module index:
  [../../developer-guide/microsphere-java-core/README.md](../../developer-guide/microsphere-java-core/README.md)
