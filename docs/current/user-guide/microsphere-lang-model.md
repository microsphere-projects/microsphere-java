# Language Model (`javax.lang.model`) Utilities

| | |
|---|---|
| **Artifact** | `io.github.microsphere-projects:microsphere-lang-model` (`0.3.20-SNAPSHOT` on `release`; tag `0.3.20` published) |
| **Packages** | `io.microsphere.lang.model.element` (1 type) and `io.microsphere.lang.model.util` (15 types) — **16 main-source types** |
| **Wraps** | `javax.lang.model` — `Element`, `TypeMirror`, `DeclaredType`, `AnnotationMirror`, `AnnotationValue`, `Elements`, `Types`, `Messager`, `ProcessingEnvironment` |
| **JDK level** | No `maven.compiler.*` / `<release>` property anywhere in the reactor; bytecode level is inherited from the external build parent `microsphere-build:0.3.16`. CI-tested on Temurin JDK 8 / 11 / 17 / 21 / 25 |
| **Compile dependency** | `microsphere-java-core` (`microsphere-lang-model/pom.xml:24-28`, version `${revision}`) |
| **Test-only dependencies** | `microsphere-jdk-tools` (`:44-49`), `microsphere-annotation-test` (`:52-57`), plus JUnit 5, Logback, `javax.ws.rs-api`, `jaxws-api`, `spring-context`, `spring-web` |
| **Used by** | `microsphere-annotation-processor` — this module is the build-time-metadata half of that pair |

All `file:line` references are relative to `microsphere-lang-model/src/main/java/io/microsphere/` (main sources) or
`microsphere-lang-model/src/test/java/io/microsphere/` (tests), read from the **`release` branch worktree**. After the
first mention they shorten to `lang/model/util/TypeUtils.java:60-88`. References into other modules keep the module
name, e.g. `microsphere-java-core/src/main/java/io/microsphere/reflect/TypeUtils.java:1543`.

---

## 1. Who needs this module, and what it removes

**You need this module if you write an annotation processor** — a `javax.annotation.processing.AbstractProcessor`, a
build-time metadata generator, an API-doc extractor, or anything that runs under `javac` and inspects source structure
rather than loaded classes.

At that point in the build you do **not** have `Class`, `Method`, `Field`. You have `Element`, `TypeMirror`,
`ExecutableElement`, `AnnotationMirror` and `AnnotationValue`, and the only ways to ask them anything are the two
`javax.lang.model.util` services `Elements` and `Types`, obtained from `ProcessingEnvironment`. The raw API is verbose
in four specific ways, and this module removes each:

* **Kind tests are untyped and not null-safe.** `element.getKind() == ElementKind.CLASS` misses `ENUM` and `RECORD`.
  `ElementUtils` gives null-safe `isClass`/`isInterface`/`isField`/`isExecutable`/`isMember`/`isVariable`/
  `isInitializer` (`lang/model/util/ElementUtils.java:87-296`).
* **`TypeMirror` vs. `java.lang.reflect.Type` comparison does not exist.** No `Elements`/`Types` method answers "is
  this `TypeMirror` the same type as `java.lang.String`?". `TypeUtils.isSameType` does, in four overloads
  (`lang/model/util/TypeUtils.java:177-266`).
* **Annotation attribute extraction is a four-level manual walk**: `AnnotationMirror` → `getAnnotationType()` →
  attribute `ExecutableElement`s → `getElementValues()` → `AnnotationValue` → `getValue()`; and defaults are *absent*
  from `getElementValues()` unless you re-read `ExecutableElement.getDefaultValue()`.
  `AnnotationUtils.getElementValues` does the whole walk and merges defaults
  (`lang/model/util/AnnotationUtils.java:1509-1529`).
* **Diagnostics need two channels.** `Messager` writes into the build output; a `Logger` writes into your processor's
  own log. `MessagerUtils.printMessage` feeds both from one call (`lang/model/util/MessagerUtils.java:292-309`).

**Do not reach for it when** you already have loaded `Class` objects — that is `io.microsphere.reflect` in
`microsphere-java-core`, which is faster and has no `ProcessingEnvironment` requirement. Nor at runtime in a
`jlink`-stripped image: `javax.lang.model` lives in the `java.compiler` module and may be absent.

---

## 2. Coordinates and dependencies

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-lang-model</artifactId>
    <version>0.3.20</version> <!-- newest published tag; 0.3.20-SNAPSHOT is this same line's working version -->
</dependency>
```

| Fact | Value | Evidence (`release`) |
|---|---|---|
| `groupId` | `io.github.microsphere-projects` | root `pom.xml:12` |
| `artifactId` | `microsphere-lang-model` | `microsphere-lang-model/pom.xml:14` |
| Version property | `${revision}` | `pom.xml:15` |
| Value of `${revision}` on `release` | `0.3.20-SNAPSHOT` | root `pom.xml:54` |
| Newest published tag | `0.3.20` | release line of the `release` branch |
| Build parent | `microsphere-build:0.3.16` (external) | no `maven.compiler.*` / `<release>` property in the reactor |
| Compile dependency | `microsphere-java-core`, `${revision}` | `pom.xml:24-28` |
| Test dependencies | JUnit 5 `:31-41`, `microsphere-jdk-tools` `:44-49`, `microsphere-annotation-test` `:52-57`, Logback `:60-64`, `javax.ws.rs-api` `:67-71`, `jaxws-api` `:74-78`, `spring-context` `:81-85`, `spring-web` `:87-91` | same |
| CI matrix | Temurin JDK `8, 11, 17, 21, 25` | `.github/workflows/maven-build.yml:25`, `distribution: 'temurin'` `:33`, `-Dsurefire.useSystemClassLoader=false` `:43` |

> [!IMPORTANT]
> `microsphere-jdk-tools` and `microsphere-annotation-test` are **`<scope>test</scope>`** (`pom.xml:48`, `:56`). They
> do not appear on your compile classpath, and your processor will not get them transitively. You need them only to run
> this module's own test suite (see §10, pitfall 1).

`microsphere-java-core` is the **only** compile-scope dependency, and this module uses it heavily: `io.microsphere.util.*`,
`io.microsphere.collection.*`, `io.microsphere.lang.function.*`, `io.microsphere.reflect.*`, `io.microsphere.json.JSONUtils`,
`io.microsphere.text.FormatUtils`, `io.microsphere.logging.*`, `io.microsphere.constants.SymbolConstants` and
`io.microsphere.annotation.*` are all imported from there.

---

## 3. Version information and compatibility

| Question | Answer | Evidence |
|---|---|---|
| Release line | `release` branch, `${revision}` = `0.3.20-SNAPSHOT`, tag `0.3.20` published | root `pom.xml:54` |
| `@Since` of all 16 types | `1.0.0` | `lang/model/util/ElementUtils.java:56`, `lang/model/util/TypeUtils.java:67`, `lang/model/util/AnnotationUtils.java:99`, `lang/model/element/StringAnnotationValue.java:30`, `lang/model/util/MessagerUtils.java:41`, `lang/model/util/ExecutableElementComparator.java:53` |
| Package namespace | **`javax.lang.model`, never `jakarta.lang.model`** | every main source imports `javax.lang.model.*`, e.g. `lang/model/util/ElementUtils.java:24-28`; there is no `jakarta.*` import in `src/main/java` |
| `javax.lang.model` availability | `Element`/`Types`/`Elements`/`ElementVisitor`/`AnnotationValueVisitor` are JDK 6+; `ElementKindVisitor6`, `SimpleAnnotationValueVisitor6` and `AbstractElementVisitor6` — the bases this module extends — are **JDK 6** and deprecated since **JDK 9** in favour of the `...8`/`...14` variants | `lang/model/util/JSONElementVisitor.java:27-28,57`, `lang/model/util/JSONAnnotationValueVisitor.java:25,72`, `lang/model/util/ResolvableAnnotationValueVisitor.java:27,74` |
| JDK range actually tested | 8, 11, 17, 21, 25 | `.github/workflows/maven-build.yml:25` |
| Spring version | irrelevant to `src/main/java` (no Spring import); test-only `spring-context`/`spring-web` | `pom.xml:81-91`; parent properties `microsphere-java-parent/pom.xml:21-24` (`microsphere-bom.version` `0.3.13` `:22`, `spring-framework.version` `7.0.9` `:23`), BOM imports `:31-37` and `:40-46`, profile `java8-16` `:54-62` with `<jdk>[1.8,17)</jdk>` `:57` pinning Spring `5.3.39` `:60` |
| `RECORD` / `BINDING_VARIABLE` kinds | matched by **name**, not by enum constant, so the module still compiles on JDK 8 | `lang/model/util/ElementUtils.java:358-359` |

> [!NOTE]
> Answering the package question directly: **yes, this module compiles against `javax.*`, not `jakarta.*`.** The JDK
> never renamed `javax.lang.model` — the Jakarta EE namespace change applied to Jakarta EE APIs, not to the Java
> Language Model. There is no migration path to "jakarta.lang.model", and a `jakarta.annotation`-based processor still
> inspects its elements through the `javax.lang.model` API this module wraps. The only Jakarta-flavoured imports anywhere
> in the module are test-scoped and still `javax`-based (`pom.xml:67-78`).

---

## 4. Element lookup and navigation — `ElementUtils`

`ElementUtils` is an `interface` extending `io.microsphere.util.Utils` (`lang/model/util/ElementUtils.java:58`), so it
is a namespace for `static` methods, not an injectable service.

### 4.1 Kind predicates (all null-safe, all return `boolean`)

| Method | `true` for | Evidence |
|---|---|---|
| `isClass(ElementKind)` | `CLASS`, `ENUM`, `RECORD` (delegates to `ElementKind#isClass()`) | `:87-89` |
| `isInterface(ElementKind)` | `INTERFACE`, `ANNOTATION_TYPE` | `:118-120` |
| `isDeclaredType(ElementKind)` | either of the two above | `:149-151` |
| `isField(ElementKind)` | `FIELD`, `ENUM_CONSTANT` | `:180-182` |
| `isExecutable(ElementKind)` | `METHOD`, `CONSTRUCTOR`, `STATIC_INIT`, `INSTANCE_INIT` (own `switch`, **not** `ElementKind#isExecutable()`) | `:216-227` |
| `isMember(ElementKind)` | `isField \|\| isExecutable` | `:256-258` |
| `isInitializer(ElementKind)` | `STATIC_INIT`, `INSTANCE_INIT` | `:287-296` |
| `isVariable(ElementKind)` | `ENUM_CONSTANT`, `FIELD`, `PARAMETER`, `LOCAL_VARIABLE`, `EXCEPTION_PARAMETER`, `RESOURCE_VARIABLE`, plus `BINDING_VARIABLE` by **name** | `:347-362` |

### 4.2 `ElementType` ↔ `ElementKind` bridge and signature helpers

`toElementKind(ElementType)` maps `TYPE`/`TYPE_USE` → `CLASS`, otherwise `ElementKind.valueOf(name)`, and returns
`OTHER` for `null` (`:389-400`). Three `matchesElementType` overloads build on it — `(ElementKind, ElementType)`
`:430-432`, `(ElementKind, ElementType...)` `:461-472`, `(Element, ElementType...)` `:500-502` — and
`matchesElementKind(Element, ElementKind)` `:526-528` is the direct form.

The remaining group is modifier/filter/signature: `isPublicNonStatic` `:550-552`, `hasModifiers(Element, Modifier...)`
`:578-589`, `filterElements(List, Predicate...)` `:622-633` (AND-combined, returns `unmodifiableList`),
`matchParameterTypes(ExecutableElement, Type...)` `:656-659`, `matchParameterTypes(List<? extends VariableElement>,
Type...)` `:682-684`, `matchParameterTypeNames(List, CharSequence...)` `:714-733` — compares parameter count first,
then each parameter's type name.

### 4.3 Example

Condensed from `lang/model/util/ElementUtilsTest.java:100-136`; `echoMethod` is `TestServiceImpl#echo(String)`, resolved
once in `beforeTest` (`ElementUtilsTest.java:94-98`):

```java
// ElementUtilsTest.java:102-103
assertTrue(matchesElementKind(echoMethod, METHOD));
assertFalse(matchesElementKind(this.echoMethod, FIELD));

// ElementUtilsTest.java:114-117 — Integer#valueOf(String) is public *static* and must not match
methodsIn(getDeclaredMembers(testTypeElement)).forEach(method -> assertTrue(isPublicNonStatic(method)));
assertFalse(isPublicNonStatic(findMethod(getTypeElement(Integer.class), "valueOf", String.class)));

// ElementUtilsTest.java:127-129
List<? extends Element> members = getAllDeclaredMembers(testTypeElement.asType());
List<VariableElement> fields = fieldsIn(members);
assertTrue(hasModifiers(fields.get(0), PROTECTED));

// ElementUtilsTest.java:336-337 — parameter types compared by type name
assertTrue(matchParameterTypeNames(this.echoMethod.getParameters(), "java.lang.String"));
assertFalse(matchParameterTypeNames(this.echoMethod.getParameters(), "java.lang.Object"));
```

_Source: `lang/model/util/ElementUtilsTest.java:100-136,335-338`_

---

## 5. Type utilities — `TypeUtils`

`TypeUtils` (`lang/model/util/TypeUtils.java:69`) is the largest type in the module: roughly a hundred `static` methods
in five families, most of them present in two flavours, one taking `Element` and one taking `TypeMirror`.

| Family | Representative methods | Evidence |
|---|---|---|
| Type-shape predicates | `isSimpleType`, `isSameType`, `isArrayType`, `isEnumType`, `isClassType`, `isPrimitiveType`, `isInterfaceType`, `isAnnotationType`, `isTypeElement`, `isDeclaredType` | `:120-628` (pairs at `:120/:149`, `:177/:203/:235/:261`, `:289/:314`, `:340/:367`, `:387/:408`, `:428/:448`, `:468/:489`, `:509/:530`, `:555/:579`, `:604/:628`) |
| Converters | `ofTypeElement(Element/TypeMirror/DeclaredType)`, `ofDeclaredType(Element/TypeMirror)`, `ofTypeMirrors`, `ofTypeElements`, `ofDeclaredTypes` | `:652-789` |
| Supertype traversal | `getTypeElementOfSuperclass`, `getAllTypeElementsOfSuperclasses`, `getTypeElementsOfInterfaces`, `getAllTypeElements`, `findTypeElements`, `getDeclaredTypeOfSuperclass`, `getAllDeclaredTypes`, `findDeclaredTypes` | `:1013-2272` (`getAllTypeElements` `:1169`, `findTypeElements` `:1360-1365`) |
| `ProcessingEnvironment` bridges | `getTypeMirrors(env, Type...)`, `getTypeMirror(env, Type)`, `getTypeElement(env, Type/TypeMirror/CharSequence)`, `getDeclaredType(env, Type/TypeMirror/CharSequence)` | `:2287-2433` |
| Names and finder | `toString(TypeMirror)`, `getTypeName(TypeMirror)`, `typeElementFinder(...)` | `:2445-2505` |

The name-based comparison core is five lines (`:261-266`):

```java
static boolean isSameType(TypeMirror type, CharSequence typeName) {
    if (type == null && typeName == null) {
        return true;
    }
    return Objects.equals(valueOf(type), valueOf(typeName));
}
```

`SIMPLE_TYPE_NAMES` (`:77-82`) is the immutable name list this module treats as "simple", and `isSimpleType` is
`SIMPLE_TYPE_NAMES.contains(type.toString())` (`:149-151`). Two `TypeElement` accessors are cached as method handles at
`:88` and `:94-97`.

### 5.1 Example

`TypeUtils` is used throughout the test base `lang/model/util/UtilTest.java:55-92`, which wraps the
`ProcessingEnvironment`-dependent lookups so individual tests do not repeat them:

```java
// UtilTest.java:59-69
protected TypeMirror getTypeMirror(Type type) {
    return TypeUtils.getTypeMirror(processingEnv, type);
}
protected TypeElement getTypeElement(Type type) {
    return TypeUtils.getTypeElement(processingEnv, type);
}

// UtilTest.java:87-88 — Element[] built from Types via TypeMirror then TypeElement
return getTypeMirrors(types).stream().map(TypeUtils::ofTypeElement).toArray(Element[]::new);

// JSONElementVisitorTest.java:237-240 — a TypeMirror back to a TypeElement, then its type parameters
TypeMirror superclass = getTypeElement(StringArrayList.class).getSuperclass();
TypeElement superTypeElement = ofTypeElement(superclass);
List<? extends TypeParameterElement> typeParameters = superTypeElement.getTypeParameters();
```

_Source: `lang/model/util/UtilTest.java:55-92`, `lang/model/util/JSONElementVisitorTest.java:236-245`_

---

## 6. Annotation introspection

### 6.1 `AnnotationUtils`

`AnnotationUtils` (`lang/model/util/AnnotationUtils.java:101`) carries four constants worth knowing:
`VALUE_ATTRIBUTE_NAME = "value"` (`:106`), `DEFAULT_ANNOTATION_VALUE_VISITOR` — a shared `ResolvableAnnotationValueVisitor`
(`:111`), `EMPTY_ELEMENT_TYPE_ARRAY` (`:117`) and `WITH_DEFAULT = true` (`:119`).

| What you want | Call | Evidence |
|---|---|---|
| The first mirror of a given annotation | `getAnnotation(AnnotatedConstruct, Class \| CharSequence)` | `:141-146`, `:169-175` |
| Directly present mirrors only | `getAnnotations(construct[, class \| name])` | `:197-202`, `:226-231`, `:256-261` |
| Mirrors **including inherited ones** | `getAllAnnotations(Element \| TypeMirror \| ProcessingEnvironment, ...)` | `:282-287` … `:480-485` |
| Search the hierarchy for one annotation | `findAnnotation(TypeMirror \| Element, Class \| CharSequence)` | `:509-514`, `:539-544`, `:572-577`, `:606-612` |
| Meta-annotations (annotations on annotations) | `findMetaAnnotation`, `isAnnotationPresent` (present **or** meta-present) | `:640-645`, `:674-692`, `:719-724`, `:752-757` |
| Predicate filtering | `findAnnotations` (direct), `findAllAnnotations` (hierarchical) | `:790-807`, `:822-827`, `:831-847`, `:863-875`, `:892-897`, `:914-919` |
| All attribute values, defaults merged | `getElementValues(...)` | `:1373-1375`, `:1425-1427`, `:1464-1466`, `:1509-1529` |
| Same as a `Map<String, Object>` | `getAttributesMap(...)` | `:1190-1192`, `:1235-1237`, `:1272-1274`, `:1315-1329` |
| One attribute | `getElementValue` / `getAttribute` / `getValue` | `:1571-1602`, `:1646-1656`, `:1695-1697`, `:1738-1741`, `:1773-1782`, `:1811-1813` |
| Compare | `matchesAnnotationType`, `matchesAnnotationTypeName`, `matchesAttributeMethod`, `matchesAttributeValue`, `matchesDefaultAttributeValue` | `:947-952`, `:980-985`, `:1052-1054`, `:1081-1091`, `:1117-1119`, `:1145-1150` |
| `@Target` of an annotation | `getElementTypes(AnnotationMirror \| DeclaredType)` | `:1841-1843`, `:1871-1875` |

The default-merging rule lives in one loop (`:1518-1527`): the map is built from `getDeclaredMethods(annotationType)`,
`annotation.getElementValues()` supplies the explicit value, and when `withDefault` is `true` and the explicit value is
`null`, `attributeMethod.getDefaultValue()` is substituted. Attributes with neither an explicit value nor a default are
**absent** from the result map.

### 6.2 `StringAnnotationValue` — the Wrapper

`StringAnnotationValue` (`lang/model/element/StringAnnotationValue.java:32-49`) is a hand-written `AnnotationValue`
implementation over a `String`, so a value you synthesised yourself can be fed into code that only accepts
`AnnotationValue`. It is `@Immutable` (`:32`), stores one `final String` (`:35,37-39`), `getValue()` returns it
(`:41-44`), and `accept` hard-wires the double dispatch:

```java
@Override
public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
    return v.visitString(this.value, p);
}
```

Because `accept` bypasses the JDK's own type-switch, `StringAnnotationValue` is accepted by *any*
`AnnotationValueVisitor`, including the JDK's and both visitors below.

### 6.3 The two `AnnotationValue` visitors

| Visitor | Base class | Returns | Purpose | Evidence |
|---|---|---|---|---|
| `ResolvableAnnotationValueVisitor` | `SimpleAnnotationValueVisitor6<Object, ExecutableElement>` | `Object` | turn an attribute value into a runtime Java object | `lang/model/util/ResolvableAnnotationValueVisitor.java:74` |
| `JSONAnnotationValueVisitor` | `SimpleAnnotationValueVisitor6<StringBuilder, ExecutableElement>` | the shared `StringBuilder` | append `"name":value` JSON for an attribute | `lang/model/util/JSONAnnotationValueVisitor.java:72` |

`ResolvableAnnotationValueVisitor` has three constructors (`:88-99`) controlled by two booleans: `classValuesAsString`
(`:84`) and `nestedAnnotationsAsMap` (`:86`). `visitType` returns either the `TypeMirror` string or a loaded `Class`
(`:146-149`); `visitEnumConstant` loads the enum class and calls `Enum.valueOf` (`:151-155`); `visitAnnotation` builds a
`LinkedHashMap<String, Object>` and, unless `nestedAnnotationsAsMap` is set or the JDK hook is missing, reflects into
`sun.reflect.annotation.AnnotationParser.annotationForMap` (`:157-174`, hook resolved at `:76-78`). `visitArray` needs
the attribute method to learn the component type (`:176-195`).

`JSONAnnotationValueVisitor` is a thin writer: every scalar visit is one
`append(jsonBuilder, getAttributeName(attributeMethod), value)` call (`:81-133`). Three cases differ — `visitType` writes
the **type name** (`:135-139`); `visitEnumConstant` writes the **simple name** (`:141-145`); `visitAnnotation` /
`visitArray` recurse with a **child** `JSONAnnotationValueVisitor` over a fresh `StringBuilder`, then append the finished
fragment through `doAppend` (`:147-184`, `:191-195`). `visitUnknown` returns the builder untouched (`:186-189`).

> [!NOTE]
> The JSON output is produced through `io.microsphere.json` types: the visitors import `JSONUtils.append` and
> `JSONUtils.appendName` (`lang/model/util/JSONAnnotationValueVisitor.java:36-37`) and take brace/bracket/comma symbols
> from `io.microsphere.constants.SymbolConstants` (`:31-35`). They write **into `io.microsphere.json`'s string-building
> layer**, not into `JSONObject`/`JSONArray` objects — note that the sibling `JSONElementVisitor` builds onto a raw
> `StringBuilder` and imports no `io.microsphere.json` type at all. `appendName` skips blank names entirely
> (`microsphere-java-core/src/main/java/io/microsphere/json/JSONUtils.java:500-508`), which is precisely why `visitArray`
> passes `null` as the attribute method (`JSONAnnotationValueVisitor.java:176`): array elements must render namelessly.
> See [JSON](../microsphere-java-core/json.md).

### 6.4 The two element visitors

`JSONElementVisitor` (`lang/model/util/JSONElementVisitor.java:57`) extends
`ElementKindVisitor6<Boolean, StringBuilder>` with the fallback set to `FALSE` (`:59-61`). Its `visit*` methods are
`final` and implement a support-then-decide gate: `visitPackage` `:63-69`, `visitVariable` `:71-77`, `visitExecutable`
`:79-85`, `visitType` `:87-101`, `visitTypeParameter` `:103-109`. The extension points are the `protected` hooks
`supports(Element)` (`:130-132`, default `true`), the kind-specific `supportsXxx` family (`:143-197`) and
`doVisitPackage` / `doVisitTypeParameter` (`:206-219`). `visitMembers(List, StringBuilder)` (`:111-119`) re-enters
`accept` for every enclosed element.

`AnnotatedElementJSONElementVisitor` (`lang/model/util/AnnotatedElementJSONElementVisitor.java:88`) is the
"only elements annotated with X" filter: its constructor resolves the annotation once and fails fast on bad input
(`:102-112`), and `supports(Element)` narrows traversal to the annotation's own `@Target` set (`:124-126`).

### 6.5 Examples

`JSONAnnotationValueVisitorTest.java:50-56,128-134` — get the attribute map for `@TestAnnotation`, pick one attribute,
visit it, compare the JSON text:

```java
this.jsonBuilder = new StringBuilder();
this.visitor = new JSONAnnotationValueVisitor(jsonBuilder);
this.testAnnotationAttributes = getElementValues(testTypeElement, TestAnnotation.class);

void testVisit(String attributeName, String expectedJson) {
    Map.Entry<ExecutableElement, AnnotationValue> elementValue =
            getElementValue(this.testAnnotationAttributes, attributeName);
    StringBuilder jsonBuilder = visitor.visit(elementValue.getValue(), elementValue.getKey());
    assertEquals(expectedJson, jsonBuilder.toString());
}
```

The asserted outputs (`:58-121`) are the ground truth for the emitted structure:

| Attribute | Emitted JSON | Kind |
|---|---|---|
| `"z"` | `"z":true` | boolean `:59-61` |
| `"c"` | `"c":"b"` | char, quoted `:69-71` |
| `"d"` / `"f"` | `"d":1.0` / `"f":1.0` | double / float `:73-81` |
| `"type"` | `"type":"io.microsphere.test.service.GenericTestService"` | `Class` value → type name `:103-106` |
| `"timeUnit"` | `"timeUnit":"HOURS"` | enum constant → simple name `:108-111` |
| `"since"` | `"since":{"module":"","value":"1.0.0"}` | nested annotation → object `:113-116` |
| `"properties"` | `"properties":[{"name":"key","type":"java.lang.String",…},{"name":"key2",…}]` | array of annotations `:118-121` |
| unknown | *(nothing)* — the builder is returned unchanged | `:123-126` |

_Source: `lang/model/util/JSONAnnotationValueVisitorTest.java:50-56,58-134`_

For `JSONElementVisitor`, `JSONElementVisitorTest.java:57-137` shows the correct subclassing shape — override the JDK
`visitTypeAsClass` / `visitVariableAsField` / `visitExecutableAsMethod` methods, not `visitType` / `visitVariable` /
`visitExecutable`, which are `final`:

```java
this.visitor = new JSONElementVisitor() {
    @Override protected boolean supports(Element e) { return supported; }
    @Override public Boolean visitTypeAsClass(TypeElement e, StringBuilder sb) {
        jsonBuilder.append("visitTypeAsClass");
        return TRUE;
    }
};
// JSONElementVisitorTest.java:215-219
TypeElement typeElement = getTypeElement(testClass);
assertTrue(visitor.visitType(typeElement, jsonBuilder));
assertTrue(jsonBuilder.toString().startsWith("visitTypeAsClass"));
```

_Source: `lang/model/util/JSONElementVisitorTest.java:57-137,214-219`_

---

## 7. Member utilities

Five types share one shape — `getDeclaredX` (this type only) / `getAllDeclaredX` (whole hierarchy) / `findX` /
`findDeclaredX(s)` / `filterDeclaredX(s)` / `isX` / `matchesX` — and each is an `interface` of `static` methods taking
`TypeElement` or `TypeMirror`.

| Type | Wraps | Key methods (evidence, all in `lang/model/util/`) |
|---|---|---|
| `ClassUtils` | `TypeMirror` → `java.lang.Class` | `getClassName(TypeMirror)` `ClassUtils.java:57`, `loadClass(TypeMirror)` `:78`, `loadClass(String)` `:98` |
| `FieldUtils` | `VariableElement` | `getDeclaredField(Element/TypeMirror, String)` `FieldUtils.java:107,135`, `getDeclaredFields` `:161,187`, `getAllDeclaredFields` `:213,239`, `findField` `:267,294`, `findDeclaredFields` `:330,368`, `findAllDeclaredFields` `:407,445`, `filterDeclaredFields` `:488`, `isEnumMemberField` `:524`, `isNonStaticField` `:554`, `isField` `:580,618`, `getNonStaticFields` `:644,674`, `getAllNonStaticFields` `:704,734`, `equalsFieldName` `:760` |
| `MethodUtils` | `ExecutableElement` (methods) | `getDeclaredMethods` `MethodUtils.java:86,111`, `getAllDeclaredMethods` `:136,161`, `findDeclaredMethods` `:196,231`, `findAllDeclaredMethods` `:264,300,415,451`, `findPublicNonStaticMethods` `:338,373`, `filterDeclaredMethods` `:487`, `isMethod` `:522`, `isPublicNonStaticMethod` `:546`, `findMethod` (6 overloads) `:575,604,635,664,699,728`, `getOverrideMethod(env, …)` `:764`, `filterMethods` `:804`, `getMethodName` `:841`, `getReturnTypeName` `:866`, `getMethodParameterTypeMirrors` `:896`, `getMethodParameterTypeNames` `:938`, `matches` `:967,994`, `matchesMethodName` `:1021`, `matchesMethod` `:1048,1089`, `getEnclosingElement` `:1127`, `methodPredicateForExcludedTypes` `:1152` |
| `ConstructorUtils` | `ExecutableElement` (constructors) | `getDeclaredConstructors(TypeElement/TypeMirror)` `ConstructorUtils.java:87,123`, `findConstructor` `:151,179`, `findDeclaredConstructors` `:220,239`, `filterDeclaredConstructors` `:278` |
| `MemberUtils` | any `Element` member | `getDeclaredMembers(TypeMirror/TypeElement)` `MemberUtils.java:98,122`, `getAllDeclaredMembers` `:146,170`, `getDeclaredMembers(..., boolean includeHierarchicalTypes)` `:196,222`, `findDeclaredMembers` `:250,278,388,419`, `findAllDeclaredMembers` `:310,349` |

`getMethodName` (`MethodUtils.java:841-843`) and `getReturnTypeName` (`:866-868`) are the two null-tolerant accessors in
this family: `method == null ? null : method.getSimpleName().toString()`.

`ClassUtils.loadClass` is the bridge most likely to surprise you — it resolves through
`io.microsphere.util.ClassLoaderUtils`, so **the class must be on the processor's own classpath**, not merely on the
source path being compiled. `ResolvableAnnotationValueVisitor.visitType` and `visitEnumConstant` both depend on it
(`lang/model/util/ResolvableAnnotationValueVisitor.java:148,153`), which is why resolving a `Class`-valued attribute of a
class generated in the same compilation round fails.

> [!TIP]
> `TypeElement` and `TypeMirror` overloads are not redundant. Use the `TypeMirror` form when you came from
> `Element.asType()` or from an annotation's `DeclaredType`; use the `TypeElement` form when you already hold one. Both
> terminate in the same traversal, and both return `emptyList()` on `null`.

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

_Source: `lang/model/util/ElementUtilsTest.java:94-98`,
`lang/model/util/ExecutableElementComparatorTest.java:38-54`_

---

## 8. Diagnostics — `MessagerUtils` and `LoggerUtils`

### 8.1 `LoggerUtils`

`lang/model/util/LoggerUtils.java:30-53` declares one shared `Logger` named `"microsphere-lang-model"` (`:32`, from
`io.microsphere.logging.LoggerFactory.getLogger`) and five static pass-throughs: `trace` `:34`, `debug` `:38`,
`info` `:42`, `warn` `:46`, `error` `:50`, each `(String format, Object... args)`.

### 8.2 `MessagerUtils`

`lang/model/util/MessagerUtils.java:43` exposes five pairs of methods — one `ProcessingEnvironment` overload and one
`Messager` overload each:

| Level | `Diagnostic.Kind` | `MessagerUtils` | `LoggerUtils` level | Evidence |
|---|---|---|---|---|
| note | `NOTE` | `printNote(env\|messager, …)` | `info` | `:63-65`, `:103-105` |
| warning | `WARNING` | `printWarning(…)` | `warn` | `:127-129`, `:150-152` |
| mandatory warning | `MANDATORY_WARNING` | `printMandatoryWarning(…)` | `warn` | `:174-176`, `:197-199` |
| error | `ERROR` | `printError(…)` | `error` | `:221-223`, `:244-246` |
| any | `Kind` (parameter) | `printMessage(env\|messager, kind, …)` | see below | `:268-270`, `:292-309` |

The whole mapping is one method (`:292-309`): the pattern is formatted **once** via
`io.microsphere.text.FormatUtils.format(pattern, args)`, sent to the `Messager`, then re-sent to the logger **with the raw
pattern and args** so the logging backend can do its own parameterisation. `ERROR` → `error`, `WARNING` and
`MANDATORY_WARNING` → `warn`, `NOTE` → `info`, and **every other kind** (`OTHER`, and any `Diagnostic.Kind` added by a
future JDK) → `debug` (`:306-307`).

> [!WARNING]
> The placeholder syntax is `{}`, not `{0}` and not `String.format`. The javadoc says "supports `String#format` syntax"
> and its examples use `{0}` (`lang/model/util/MessagerUtils.java:56-61`, `:78-82`), but the implementation calls
> `FormatUtils.format`, whose default placeholder is literally `"{}"`
> (`microsphere-java-core/src/main/java/io/microsphere/text/FormatUtils.java:17`, `:38-39`). Write `"Hello, {}!"` — as
> the tests do (`lang/model/util/MessagerUtilsTest.java:55-56`) — or your argument will never be substituted.

There is no null guard anywhere in the chain: `printX(processingEnv, …)` dereferences `processingEnv.getMessager()`
(`:64`, `:128`, `:175`, `:222`, `:269`) and `printMessage(messager, …)` dereferences `messager.printMessage(...)` (`:294`)
and `kind` in the `switch` (`:295`). See §10, pitfall 8.

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

_Source: `lang/model/util/MessagerUtilsTest.java:47-81`_

---

## 9. Ordering — `ExecutableElementComparator`

`lang/model/util/ExecutableElementComparator.java:55` is a `Comparator<ExecutableElement>` with a private constructor
(`:62`) and one public singleton, `INSTANCE` (`:60`) — so callers always share the same stateless instance.

The documented rule (`:27-33`) is implemented in four steps:

1. **Identity/`equals` shortcut** — `if (e1.equals(e2)) return 0;` (`:68-70`).
2. **Simple name, lexicographic** — `CharSequenceComparator.INSTANCE.compare(e1.getSimpleName(), e2.getSimpleName())` (`:73`).
3. **Parameter count** — `ps1.size() - ps2.size()` (`:77-80`).
4. **Per-parameter type names, lexicographic, first difference wins** — comparing `asType().toString()` (`:82-89`).

The result is a stable, name-first ordering that groups overloads together and, inside each overload group, sorts by
arity and then by parameter type names. Use it whenever you generate ordered output from `getDeclaredMethods(...)` so
generated metadata is reproducible across `javac` runs.

It does **not** consult `io.microsphere.lang.Prioritized`: a grep across all 16 main types finds no reference to that
interface. The Developer Guide still records the `Prioritized` constant-inversion warning because it affects the companion
`annotation-processor` module.

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

_Source: `lang/model/util/ExecutableElementComparatorTest.java:21-65`_

---

## 10. Common pitfalls

1. **Every `TypeMirror` you obtain requires a live `ProcessingEnvironment`.** `TypeUtils.getTypeElement(env, …)`
   (`lang/model/util/TypeUtils.java:2350-2381`), `getDeclaredType(env, …)` (`:2401-2433`) and `getTypeMirror(env, …)`
   (`:2310`) delegate to `Elements.getTypeElement` / `Types.getDeclaredType`; with `null` they return `null` or
   `emptyList()` — silently. `AnnotatedElementJSONElementVisitor` is the one type that refuses `null` outright, through
   three `assertNotNull` calls (`lang/model/util/AnnotatedElementJSONElementVisitor.java:103`, `:104`, `:110`). This
   module's tests only run because `microsphere-annotation-test` supplies a real processing environment — `UtilTest`
   extends `AbstractAnnotationProcessingTest` (`lang/model/util/UtilTest.java:20,48`). **Port a test to your own project
   without that dependency and nothing will resolve.**
2. **A `TypeElement` looked up for a class *generated in the current round* may be `null`.** `Elements.getTypeElement`
   cannot see sources not yet entered. `AnnotatedElementJSONElementVisitor.java:110` turns exactly that into a hard
   failure, `"The annotation can't be found by name : …"`, so pass a *pre-existing* annotation class.
3. **`isSameType` is a string comparison, not a `Types#isSameType` call.** `lang/model/util/TypeUtils.java:261-266`
   compares `String.valueOf(typeMirror)` with the expected name. Consequences: (a) it works with no `Types` instance, so
   you can call it anywhere; (b) it is **not** equivalence-preserving for types whose `toString()` is not the canonical
   binary name — type-variable usages, capture types and contained types can compare `false` where
   `javax.lang.model.util.Types.isSameType` would return `true`. For declared types and primitives it is reliable. Also
   `isSameType(null, null)` returns `true` (`:262-264`), an "equal nulls" rule that differs from every sibling predicate,
   all of which return `false` on `null`.
4. **`isSimpleType` compares `toString()` against a name list.** `lang/model/util/TypeUtils.java:149-151` — so a declared
   type whose name happens to be in `SIMPLE_TYPE_NAMES` (`:77-82`) is "simple" regardless of where that class came from.
   Classpath-independent and classpath-surprising at the same time.
5. **`getElementValues` includes defaults; `getElementValue` prefers explicit values.**
   `lang/model/util/AnnotationUtils.java:1509-1529` merges `getDefaultValue()` for anything absent and skips an attribute
   with neither explicit value nor default. `getElementValue(annotation, name, true)` (`:1571-1602`) re-scans declared
   methods only in the `withDefault && annotationValue == null` branch (`:1587-1595`) and returns `null` when the resolved
   value is still `null` (`:1597-1599`) — so "attribute absent" and "attribute = null" are indistinguishable.
   `getAttribute` adds a further hazard: it dispatches unchecked through
   `annotationValue.accept(DEFAULT_ANNOTATION_VALUE_VISITOR, attributeMethod)` (`:1781`) using the shared visitor, so the
   two feature flags of §6.3 are whatever the static instance was built with, not yours.
6. **Visitor default methods are inherited from JDK classes that are deprecated and half-implemented.** `JSONElementVisitor`
   extends `ElementKindVisitor6` (`lang/model/util/JSONElementVisitor.java:28,57`) and
   `JSONAnnotationValueVisitor` / `ResolvableAnnotationValueVisitor` extend `SimpleAnnotationValueVisitor6`
   (`lang/model/util/JSONAnnotationValueVisitor.java:25,72`, `lang/model/util/ResolvableAnnotationValueVisitor.java:27,74`).
   `ElementKindVisitor6` is deprecated since JDK 9, and `SimpleAnnotationValueVisitor6.visit()` falls through to
   `visitUnknown` for kinds added after JDK 6. Concretely: `JSONElementVisitor.visitType` calls `super.visitType` (`:93`),
   which dispatches to `visitTypeAsRecord` — and neither `visitTypeAsRecord` nor a `doVisitType` hook is overridden here,
   so a `record` yields **no output and `false`** unless you override it yourself.
7. **`JSONElementVisitor` does not guard `null` elements.** `supports(Element)` returns `true` unconditionally
   (`:130-132`), so with the default `supports`, `visitVariable(null, sb)` / `visitExecutable(null, sb)` /
   `visitType(null, sb)` fall through `super.visitXxx` into `ElementKindVisitor6`, which dereferences the element. Every
   `*OnUnsupported` test in `lang/model/util/JSONElementVisitorTest.java:146-251` passes only because the test overrides
   `supports` to return `false`; they pass `null` at `:177`, `:197`, `:250`. **Always override `supports`.**
8. **`MessagerUtils` throws `NullPointerException` when `Messager` is `null`.** `printMessage` calls
   `messager.printMessage(kind, message)` with no guard (`lang/model/util/MessagerUtils.java:294`) and `kind` is
   dereferenced by the `switch` at `:295`. The `ProcessingEnvironment` overloads are strictly worse:
   `processingEnv.getMessager()` at `:64`, `:128`, `:175`, `:222`, `:269` NPEs on a `null` environment. This contradicts
   the module's otherwise silent-degradation policy (§10.13). Guard yourself: `if (processingEnv != null) printNote(...)`.
9. **`ExecutableElementComparator.compare` has no null tolerance either.** `e1.equals(e2)` at
   `lang/model/util/ExecutableElementComparator.java:68` NPEs if `e1` is `null`; `e2 == null` with non-null `e1` survives
   step 1 then NPEs at `:73` on `e2.getSimpleName()`. Filter `null`s out before sorting.
10. **Nothing in this module caches `Element` lookups.** The only static state is two `static final` fields resolved once
    at class-initialisation in `ResolvableAnnotationValueVisitor` — `ANNOTATION_PARSER_CLASS` and `annotationForMapMethod`
    (`lang/model/util/ResolvableAnnotationValueVisitor.java:76-78`) — plus the immutable `SIMPLE_TYPE_NAMES` list
    (`lang/model/util/TypeUtils.java:77-82`) and the two cached `TypeElement` method handles (`:88`, `:94-97`).
    `TypeUtils.getTypeElement(env, name)`, `MemberUtils.getDeclaredMembers` and the traversal in
    `TypeUtils.getAllTypeElements` (`:1169`) recompute on every call. In a processor handling thousands of types, hoist
    `TypeElement` lookups into your own `Map`.
11. **`getTypeName` reaches into the `TypeMirror` reflectively.** `lang/model/util/TypeUtils.java:2456-2478` calls
    `invokeMethod(true, type, "getTypeArguments")` (`:2466`) because `TypeMirror` declares no such method, then appends
    the raw `List<TypeMirror>` between `<` and `>` (`:2470-2472`). Two artefacts follow: the bracketed form appears only
    when the *element* declares type parameters **and** the type carries arguments, and the rendering is `List.toString()`
    of type mirrors rather than a canonical name. Use `toString(TypeMirror)` (`:2445-2448`) when you want the plain string.
12. **`ResolvableAnnotationValueVisitor` can hand you a `Class` that is not loadable.** `visitType` and `visitEnumConstant`
    call `ClassUtils.loadClass` (`:148`, `:153` → `lang/model/util/ClassUtils.java:78`), and `visitArray` needs the
    attribute method to derive the component type (`lang/model/util/ResolvableAnnotationValueVisitor.java:177-195`):
    `(ArrayType) attributeMethod.getReturnType()` (`:193`) is an unchecked cast, so a `null` attribute method NPEs before
    the cast matters. `JSONAnnotationValueVisitor` avoids this entirely by never needing it for array elements.

### 10.13 Where the silent-degradation policy does not hold

The library's policy is **silent degradation** — missing input yields `null`, `false`, an empty list or an empty map
rather than an exception. The verified exceptions in this module are exactly:

| Site | Behaviour | Evidence (`lang/model/util/`) |
|---|---|---|
| `AnnotatedElementJSONElementVisitor` constructor | throws on `null` `processingEnv`, `annotationClassName`, or unresolvable annotation | `AnnotatedElementJSONElementVisitor.java:103,104,110` |
| `MessagerUtils` | NPE on `null` `Messager` / `ProcessingEnvironment` / `Kind` | `MessagerUtils.java:64,128,175,222,269,294,295` |
| `ExecutableElementComparator.compare` | NPE on `null` elements | `ExecutableElementComparator.java:68,73` |
| `TypeUtils.findTypeElements` | declares `throws IllegalArgumentException` when a filter element is `null` | `TypeUtils.java:1356,1360-1365`, `:2034,2064` |
| `TypeUtils.typeElementFinder` | javadoc declares `IllegalArgumentException` "if any parameter is invalid or if assertions fail" | `TypeUtils.java:2498-2505` |
| `ResolvableAnnotationValueVisitor.getComponentType` | unchecked `(ArrayType)` cast on a possibly-`null` attribute method | `ResolvableAnnotationValueVisitor.java:189-195` |

Everything else — `ElementUtils`, `AnnotationUtils`, `MemberUtils`, `FieldUtils`, `MethodUtils`, `ConstructorUtils`,
`ClassUtils`, `JSONAnnotationValueVisitor`, `StringAnnotationValue`, `LoggerUtils` — degrades. The full throw-site
enumeration is in the [Developer Guide](../developer-guide/microsphere-lang-model.md).

---

## 11. See also

* [Annotation Processor module](../microsphere-annotation-processor.md) — the framework that drives this module inside
  `javac`; `AnnotatedElementJSONElementVisitor` is its natural extension point.
* [JDK Tools module](../microsphere-jdk-tools.md) — a test-scoped dependency here (`pom.xml:44-49`) that supplies the
  in-JVM `ToolProvider` driver letting this module's tests obtain a real `ProcessingEnvironment`.
* [Annotation Test module](../microsphere-annotation-test.md) — the test-scoped harness
  `io.microsphere.test.annotation.processing.AbstractAnnotationProcessingTest` that `UtilTest` extends
  (`lang/model/util/UtilTest.java:20,48`), and the source of the `io.microsphere.test.*` fixtures.
* [microsphere-java-core README](../microsphere-java-core/README.md) — the module index and release-line version table.
* [JSON](../microsphere-java-core/json.md) — `JSONUtils.append` / `appendName`, the emitting layer behind
  `JSONAnnotationValueVisitor` (`lang/model/util/JSONAnnotationValueVisitor.java:36-37`).
* [Developer Guide — Language Model](../developer-guide/microsphere-lang-model.md) — the 16-type inventory, visitor
  dispatch diagrams, source analysis of type resolution and `AnnotationValue` trees, and the thread-safety review.
