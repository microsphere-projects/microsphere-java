# 事件分发（Event dispatching）

| | |
|---|---|
| **构件（Artifact）** | `io.github.microsphere-projects:microsphere-java-core`（`0.3.20-SNAPSHOT`，`release` 分支） |
| **包** | `io.microsphere.event` —— 10 个主源码类型 · 被 `io.microsphere.io.event`（3 个类型）与 `io.microsphere.io.StandardFileWatchService` 使用 |
| **JDK 级别** | 以 Java 8 编译；CI 在 JDK 8 / 11 / 17 / 21 / 25 上测试 |
| **依赖的其它 `core` 包** | `io.microsphere.lang`（`Prioritized`）、`io.microsphere.lang.invoke`（`LambdaUtils`）、`io.microsphere.reflect`（`TypeUtils`）、`io.microsphere.collection`（`MapUtils`、`ListUtils`、`SetUtils`、`MapUtils.newConcurrentHashMap`）、`io.microsphere.util`（`Assert`、`ClassLoaderUtils`、`ServiceLoaderUtils`）、`io.microsphere.lang.function`（`ThrowableAction`、`ThrowableConsumer`、`ThrowableFunction`）、`io.microsphere.logging`（`Logger`、`LoggerFactory`）、`io.microsphere.annotation`（属于独立的 `microsphere-java-annotations` 构件） |
| **外部依赖** | 无 —— 纯 JDK（`java.util.EventObject`、`java.util.ServiceLoader`、`java.util.concurrent.Executor`） |
| **被谁使用** | `io`（`StandardFileWatchService`）、`io.event`（`FileChangedEvent`、`FileChangedListener`、`LoggingFileChangedListener`） |

本页所有 `file:line` 引用均相对于 `microsphere-java-core/src/main/java/io/microsphere/`（主源码）或
`microsphere-java-core/src/test/java/io/microsphere/`（测试），读取自 `release` 工作树。
首次完整出现之后，路径缩写为 `event/EventDispatcher.java:40-55` / `event/EventDispatcherTest.java:40-55`。

---

## 1. 这个包解决什么问题

**当你想在一个普通 Java 对象内部做观察者式的通知，又不希望链路里出现容器、应用上下文或注解处理器时，就该用
`io.microsphere.event`。** 整个引擎只有 10 个类型（`event/Event.java`、`event/GenericEvent.java`、
`event/EventListener.java`、`event/ConditionalEventListener.java`、`event/GenericEventListener.java`、
`event/Listenable.java`、`event/EventDispatcher.java`、`event/AbstractEventDispatcher.java`、
`event/DirectEventDispatcher.java`、`event/ParallelEventDispatcher.java`），你必须构造的东西只有一个分发器对象：

```java
EventDispatcher dispatcher = EventDispatcher.newDefault();   // event/EventDispatcher.java:70-73
dispatcher.addEventListener(listener);                       // event/AbstractEventDispatcher.java:116-121
dispatcher.dispatch(new EchoEvent("Hello,World"));           // event/AbstractEventDispatcher.java:162-180
```

**与 Spring 的 `ApplicationEventPublisher` 对比 —— 只陈述代码能够证明的差异：**

| | `io.microsphere.event` | Spring `ApplicationEventPublisher` |
|---|---|---|
| 谁持有监听器 | `EventDispatcher` 实例自身，保存在实例级的 `ConcurrentMap` 中（`event/AbstractEventDispatcher.java:99`） | `ApplicationContext` |
| 监听器如何声明 | 实现 `EventListener<E>`（`event/EventListener.java:71-72`）或继承 `GenericEventListener`（`event/GenericEventListener.java:80`） | 在 bean 的**方法**上加注解 |
| 是否需要注解 | **本包根本没有任何注解类型** —— `io.microsphere.event` 声明的 `@interface` 数量为 0 | `@EventListener`、`@TransactionalEventListener`、`@ApplicationModuleListener` |
| 监听器能收到哪些事件 | 由监听器的泛型参数推导，入口是 `EventListener.findEventType(...)`（`event/EventListener.java:101-150`） | 由被注解方法的参数类型推导 |
| 启动时的额外监听器 | Java `ServiceLoader` —— `loadEventListenerInstances()`（`event/AbstractEventDispatcher.java:214-219`） | bean 扫描 |
| 线程模型 | 每个分发器一个 `Executor`：`DIRECT_EXECUTOR`（`event/EventDispatcher.java:63`）或调用方提供的实现（`event/EventDispatcher.java:81-84`） | `@Async` / 配置的 `ApplicationEventMulticaster` |
| 否决单个监听器 | `ConditionalEventListener.accept(E)`（`event/ConditionalEventListener.java:55`） | bean 上的 `@Conditional` / 方法内部 `if` |
| 中止传播 | **不支持 —— 见 §3.6** | 同样没有中止机制 |

**如果你需要事务性事件、跨线程的顺序保证，或者父子上下文层级的事件总线，就不要用它** —— 这些能力在这里都不存在；
每个分发器只有一个扁平的监听器注册表。

> [!NOTE]
> `event` 包同时也是 `io.microsphere.io.event` 的基础：`FileChangedEvent extends Event`
> （`io/event/FileChangedEvent.java:34`）、`FileChangedListener extends EventListener<FileChangedEvent>`
> （`io/event/FileChangedListener.java:95`）。`StandardFileWatchService` 为每个被监视目录用
> `EventDispatcher.parallel(...)` 建立真正的分发器（`io/StandardFileWatchService.java:278`），把
> `FileChangedListener` 实例注册上去（`io/StandardFileWatchService.java:271`），并在监视循环里分发事件
> （`io/StandardFileWatchService.java:247`）。

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
| 最新已发布标签 | `0.3.20` | 中文组索引中的版本线表（`docs/current/user-guide/microsphere-java-core/README.zh.md:68`） |
| 构建父工程 | `microsphere-build:0.3.16` | `pom.xml:6-10` |
| Java 级别 | 8（reactor 中没有 `maven.compiler.*` / `<release>` 属性，级别继承自外部父工程） | CI 矩阵 `java: [ '8' , '11' , '17' , '21' , '25' ]`，`.github/workflows/maven-build.yml:25`，`distribution: 'temurin'` `:33` |
| CI 命令 | `mvn --batch-mode --update-snapshots --file pom.xml -Drevision=0.0.1-SNAPSHOT -Dsurefire.useSystemClassLoader=false test --activate-profiles test,coverage` | `.github/workflows/maven-build.yml:38-45` |
| Spring Framework，JDK 17+ | `7.0.9` | `microsphere-java-parent/pom.xml:23` |
| Spring Framework，JDK `[1.8,17)` | `5.3.39`，来自 profile `java8-16` | `microsphere-java-parent/pom.xml:54-62`（激活条件 `<jdk>[1.8,17)</jdk>` 在 `:57`） |
| 导入的 BOM | `microsphere-all-bom:0.3.13`（`microsphere-java-parent/pom.xml:22`、`:31-37`）、`spring-framework-bom`（`:39-46`） | `microsphere-java-parent/pom.xml` |
| 与本页相关的字节码/API 约束 | 不使用 `record`、不使用 `var`、编译期不使用 `List.of`、不使用 `instanceof` 模式匹配 | `EventDispatcher.parallel(@Nonnull Executor)` 直接返回 `new ParallelEventDispatcher(executor)`，是一个普通工厂方法（`event/EventDispatcher.java:82-84`） |

> [!TIP]
> `java8-16` 是一个**构建期**属性开关，不是运行期开关；`io.microsphere.event` 完全不依赖 Spring，因此本页内容不会
> 在 Spring 5 与 Spring 7 之间发生变化。只有当你把面向 Spring 的其它 `microsphere-java-*` 模块加入同一个 reactor 时，
> 它才会产生影响。


### 2.1 `0.3.20` 的发布日期与变更摘要

| 条目 | 值 | 依据 |
|---|---|---|
| 文档所在线 | `release` 分支，`${revision}` = `0.3.20-SNAPSHOT` | 根 `pom.xml:54` |
| 最新已发布版本 | `0.3.20`，标签日期 **2026-10-16** | 标签 `0.3.20` 的提交信息为 `chore: merge main into release [skip ci]`，提交时间 `2026-10-16T03:43:20Z` |
| 上一版本 | `0.3.19`，标签日期 **2026-08-30** | 标签 `0.3.19`，提交时间 `2026-08-30T12:51:37Z` |
| 变更日志来源 | 仓库根目录的 `release-notes.md` | 本项目不发布 `CHANGELOG.md`；`release-notes.md` 的 `v0.3.16`–`v0.3.20` 段落写着 *Release notes generation failed* 并保留原始提交列表，因此下文一律以提交主题引用，并逐条与 `release` 源码核对。 |

`0.3.20` 在这一区域没有新增 API 面——`0.3.19...0.3.20` 区间是一次加固性发布。它实际改变了什么、以及这些改动触及本页源码的程度如下：

* **空值契约。** 本页覆盖范围内 2 / 10 个主源码在签名上声明了 `@Nonnull` / `@Nullable`。区间内点名本区域的主题：`45c4b6b7 Add nullability annotations to event APIs`。
* **该区间没有其它改动触及本页覆盖的区域。**

> [!NOTE]
> 如何读这份提交清单：只有 `EventDispatcher` 与 `Listenable` 两个源码带注解；该区间没有其它改动触及 `io.microsphere.event`。

---

## 3. API 参考

### 3.1 类型清单

| 类型 | 种类 | `@since` | 职责 |
|---|---|---|---|
| `Event`（`event/Event.java:29`） | 抽象类，`extends java.util.EventObject` | 1.0.0 | 携带 `final long timestamp`，在构造器里取 `System.currentTimeMillis()`（`:36`、`:44-47`），通过 `getTimestamp()` 暴露（`:49-51`） |
| `GenericEvent<S>`（`event/GenericEvent.java:42`） | 具体类 | 1.0.0 | 带类型的 source 访问器：`public S getSource()` 对 `super.getSource()` 做转型（`:48-50`） |
| `EventListener<E extends Event>`（`event/EventListener.java:72`） | `@FunctionalInterface`，继承 `java.util.EventListener` + `Prioritized` | 1.0.0 | `void onEvent(E event)`（`:205`）+ 默认 `int getPriority()` 返回 `NORMAL_PRIORITY`（`:214-216`） |
| `ConditionalEventListener<E extends Event>`（`event/ConditionalEventListener.java:47`） | 接口 | 1.0.0 | 增加 `boolean accept(E event)`（`:55`）—— 单个监听器级的否决 |
| `GenericEventListener`（`event/GenericEventListener.java:80`） | 抽象类，`implements EventListener<Event>` | 1.0.0 | 一个监听器、多个处理方法，按事件的运行时类型分发 |
| `Listenable<E extends EventListener<?>>`（`event/Listenable.java:49`） | 接口 | 1.0.0 | 注册 / 移除 / 查询契约，包含 `assertListener`（`:80-90`） |
| `EventDispatcher`（`event/EventDispatcher.java:58`） | 接口，`extends Listenable<EventListener<?>>` | 1.0.0 | `dispatch(Event)`（`:102`）、`DIRECT_EXECUTOR`（`:63`）、工厂方法（`:70-95`）、默认 `getExecutor()`（`:111-113`） |
| `AbstractEventDispatcher`（`event/AbstractEventDispatcher.java:93`） | 抽象类 | 1.0.0 | 注册表、缓存、匹配循环与 SPI 引导 |
| `DirectEventDispatcher`（`event/DirectEventDispatcher.java:56`） | `final` 类 | 1.0.0 | `super(DIRECT_EXECUTOR)`（`:58-60`）—— 同步、顺序执行 |
| `ParallelEventDispatcher`（`event/ParallelEventDispatcher.java:33`） | 类 | 1.0.0 | 无参构造器使用 `ForkJoinPool.commonPool()`（`:35-37`）；`ParallelEventDispatcher(Executor)`（`:39-41`） |

本包中**没有** `CancelDispatcherOperationException`，没有 `EventDispatcher` 构建器，也没有任何 `@interface`。
`Listenable` 的 javadoc 仍然宣称会拒绝 final 类（`event/Listenable.java:67-75`），但该检查已被注释掉
（`event/Listenable.java:83-89`）—— 见 §6。

### 3.2 注册、移除、查询

| 签名 | 声明位置 | `null` / 重复值行为 |
|---|---|---|
| `void addEventListener(E listener) throws NullPointerException, IllegalArgumentException` | `event/Listenable.java:100`；实现 `event/AbstractEventDispatcher.java:116-121` | `null` → `IllegalArgumentException`（不是 NPE），来自 `assertListener` → `Assert.assertNotNull`（`event/Listenable.java:81`、`util/Assert.java:149-153`）；重复注册被 `addIfAbsent` 丢弃（`event/AbstractEventDispatcher.java:119`、`:155-159`） |
| `default void addEventListeners(E listener, E... others)` | `event/Listenable.java:110-116` | 拼接后委托给 `Iterable` 重载 |
| `default void addEventListeners(Iterable<E> listeners)` | `event/Listenable.java:125-127` | 每个元素一次 `addEventListener` 调用 |
| `void removeEventListener(E listener)` | `event/Listenable.java:137`；实现 `event/AbstractEventDispatcher.java:124-127` | `listeners.remove(listener)` —— **基于 `equals()`** |
| `default void removeEventListeners(Iterable<E> listeners)` | `event/Listenable.java:148-150` | 同上，逐元素执行 |
| `default void removeAllEventListeners()` | `event/Listenable.java:157-159` | javadoc 写了 `@return a amount of removed listeners`，但方法返回类型是 `void` |
| `@Nonnull List<E> getAllEventListeners()` | `event/Listenable.java:167-168`；实现 `event/AbstractEventDispatcher.java:130-139` | `@Immutable`；去重、按优先级排序的快照，外层包了 `Collections.unmodifiableList` |

### 3.3 分发与执行策略

| 签名 | 声明位置 | 行为 |
|---|---|---|
| `void dispatch(Event event)` | `event/EventDispatcher.java:102`；实现 `event/AbstractEventDispatcher.java:162-180` | 向分发器的 `Executor` 提交**一个**任务，该任务内部顺序遍历匹配的监听器 |
| `@Nonnull static EventDispatcher newDefault()` | `event/EventDispatcher.java:70-73` | `new DirectEventDispatcher()` |
| `@Nonnull static EventDispatcher parallel(@Nonnull Executor executor)` | `event/EventDispatcher.java:81-84` | `new ParallelEventDispatcher(executor)`；`@Nonnull` 只是声明性约定 —— 工厂内部没有运行期 null 检查 |
| `@Nonnull static EventDispatcher of(@Nullable Executor executor)` | `event/EventDispatcher.java:92-95` | `executor == null ? newDefault() : parallel(executor)` —— 文档化的容错入口 |
| `Executor DIRECT_EXECUTOR = Runnable::run` | `event/EventDispatcher.java:63` | 在调用线程上直接运行任务 |
| `default Executor getExecutor()` | `event/EventDispatcher.java:111-113` | 对临时实现的 `EventDispatcher` 返回 `DIRECT_EXECUTOR` |
| `public final Executor getExecutor()` | `event/AbstractEventDispatcher.java:186-188` | `final` —— `AbstractEventDispatcher` 的子类**无法**更换自己的 executor；构造器传入的值即固定值（`:109-113`） |
| `protected AbstractEventDispatcher(Executor executor)` | `event/AbstractEventDispatcher.java:109-113` | `null` → `IllegalArgumentException`（`:110`、`util/Assert.java:149-153`）—— 由 `event/AbstractEventDispatcherTest.java:36-40` 证明 |

### 3.4 监听器匹配规则

一次 dispatch 只有在下面两条规则**同时**成立时才会把事件交给某个监听器：

| # | 规则 | 位置 | 说明 |
|---|---|---|---|
| 1 | **注册键**：监听器被存放在由泛型参数解析出的那一个 `Class<? extends Event>` 键下 | `event/AbstractEventDispatcher.java:190-192`（`doInListener` → `findEventType(listener)`），键表在 `:99` | 无法解析时 `findEventType` 返回 `null`，监听器随后**被静默地不注册**（`event/AbstractEventDispatcher.java:194-195`）—— 这是降级策略，而不是抛异常 |
| 2 | **投递判定**：`entry.getKey().isAssignableFrom(event.getClass())` | `event/AbstractEventDispatcher.java:168` | 因此注册在 `Event` 下的监听器会收到所有子类事件；注册在 `EchoEvent` 下的监听器**收不到**普通 `Event` —— `event/DirectEventDispatcherTest.java:111-124` |
| 3 | **否决**（仅 `ConditionalEventListener`） | `event/AbstractEventDispatcher.java:170-175` | `accept(...)` 为 false ⇒ 只跳过该监听器，其余照常执行 |

`findEventType` 的解析顺序（`event/EventListener.java:131-150`）：

| 场景 | 机制 | 位置 |
|---|---|---|
| lambda / 匿名函数式形式 | `LambdaUtils.resolveLambdaMethodParameterTypes(listenerClass, EventListener.class)`；解析出唯一参数类型时采纳 | `event/EventListener.java:135-138` |
| 普通类 | 遍历 `TypeUtils.getAllParameterizedTypes(...)`，第一个非 null 的 `ParameterizedType` 匹配胜出，否则递归父类 | `event/EventListener.java:141-146` |
| `ParameterizedType` 检查 | 原始类型可赋给 `EventListener`，第一个可赋给 `Event` 的实际类型参数胜出 | `event/EventListener.java:182-198` |
| `null` / 非监听器类 | 返回 `null`（`:134` 的 `isAssignableFrom` 守卫） | 由 `event/EventListenerTest.java:58-61` 证明 |

### 3.5 条件过滤与处理方法 —— 不存在任何注解

> [!IMPORTANT]
> `io.microsphere.event` 声明的注解类型数量为 **0** —— 该包 10 个主源码文件里没有任何 `@interface`，因此无法给出
> 逐注解的条件表，也没有任何成员携带 `@Since` 注解。`@since` 只以 Javadoc 标签形式出现，并且 10 个类型全部标注
> `1.0.0`（`event/Event.java:27`、`event/GenericEvent.java:40`、`event/EventListener.java:69`、
> `event/ConditionalEventListener.java:45`、`event/GenericEventListener.java:78`、`event/Listenable.java:47`、
> `event/EventDispatcher.java:56`、`event/AbstractEventDispatcher.java:91`、`event/DirectEventDispatcher.java:54`、
> `event/ParallelEventDispatcher.java:31`）。条件过滤改用接口与方法签名来表达：

| 机制 | 签名 | 语义 |
|---|---|---|
| `ConditionalEventListener` | `boolean accept(E event)`（`event/ConditionalEventListener.java:55`） | 对每个匹配的监听器，在 `onEvent` **之前**按优先级顺序调用（`event/AbstractEventDispatcher.java:170-177`）；返回 `false` 只跳过该监听器 |
| `GenericEventListener` | `public final void onEvent(Event event)`（`event/GenericEventListener.java:106`） | 扇出到构造器中收集到的处理方法（`:86-89`、`:95-104`） |

子类方法必须**同时**满足下面五条才会被当作处理方法（`event/GenericEventListener.java:128-151`）：

| # | 判定条件 | 行号 |
|---|---|---|
| 1 | 不是 `onEvent(Event)` 方法本身 | `:130` |
| 2 | 返回 `void` | `:134` |
| 3 | 不声明任何异常类型 | `:138-141` |
| 4 | 恰好一个参数 | `:144-146` |
| 5 | 该参数是 `Event` 或其子类 | `:150` |

`public` 可访问性是*隐式*强制的：候选集合来自 `getClass().getMethods()`（`event/GenericEventListener.java:98`），
它只返回 public 成员 —— 因此 `event/GenericEventListenerTest.java:73-75` 里的
`protected void notEvent(EchoEvent)` 永远不会被收集。**方法名不重要**（`event/GenericEventListenerTest.java:65`）。
而进入该 Map 的查找依据是**精确的运行时类型**，不是可赋值性：
`handleEventMethods.getOrDefault(event.getClass(), emptySet())`（`event/GenericEventListener.java:108`）。

### 3.6 优先级排序、“取消”语义与传播

`EventListener` 是 `Prioritized`（`event/EventListener.java:72`），而 `Prioritized.compareTo` 对
`getPriority()` 的原始 int 做升序比较（`lang/Prioritized.java:103-105`），所以**数值更小的先执行**：

| 常量 | 取值 | 含义 | 行号 |
|---|---|---|---|
| `MAX_PRIORITY` | `Integer.MIN_VALUE` | 最高优先级（最先执行） | `lang/Prioritized.java:81` |
| `NORMAL_PRIORITY` | `0` | 未覆写 `getPriority()` 的监听器的默认值 | `lang/Prioritized.java:91`、`event/EventListener.java:214-216` |
| `MIN_PRIORITY` | `Integer.MAX_VALUE` | 最低优先级（最后执行） | `lang/Prioritized.java:86` |

排序会发生两次：一次在注册表内部，每次结构变更后（`Collections.sort(listeners)`，
`event/AbstractEventDispatcher.java:201`）；另一次在每次分发时（`Stream.sorted()`，`:152`，依赖
`lang/Prioritized.java:103-105` 实现的 `Comparable`）。SPI 加载出来的监听器在注册前也排过序
（`event/AbstractEventDispatcher.java:215-217`），与全仓库“不信任 `ServiceLoader` 文件顺序”的规则一致。

**取消语义 —— 精确说明存在什么：** 什么都不存在。

* `release` 工作树中不存在 `CancelDispatcherOperationException`，也不存在任何其它取消/中止类型（`event/*.java` 就是
  §3.1 列出的那 10 个类型）。
* 监听器**无法中止传播**。`onEvent` 返回 `void`（`event/EventListener.java:205`），分发循环忽略所有返回值且从不
  break（`event/AbstractEventDispatcher.java:168-178`）。
* 真正存在的短路只有两处：
  1. `accept(...) == false` 恰好跳过一个监听器 —— `forEach` 内部的 `return;`，
     位于 `event/AbstractEventDispatcher.java:172-174`；
  2. 某个监听器**抛异常**会中止本次分发中剩余的监听器，因为 `event/AbstractEventDispatcher.java:177` 的
     `listener.onEvent(event)` 没有被任何 `try`/`catch` 包裹。使用 `DirectEventDispatcher` 时异常会沿着调用线程穿出
     `dispatch()`（`Runnable::run`，`event/EventDispatcher.java:63`）；使用真实 `Executor` 时异常在工作线程上抛出，
     调用方无法捕获。
* `StandardFileWatchService.stop()` 里取消事件循环（`io/StandardFileWatchService.java:192`）是对监视循环的
  `Future.cancel`，与本包的监听器契约无关。

---

## 4. 取自真实测试的示例

### 4.1 两个监听器、各自一种事件类型，优先级决定顺序

`event/DirectEventDispatcherTest.java:63-80` —— `EchoEventListener` 监听 `EchoEvent`；`EchoEventListener2` 监听
`Event` 且 `getPriority()` 返回 `-1`（`event/EchoEventListener2.java:43-46`），因此排在前面：

```java
dispatcher.addEventListener(echoEventListener);
dispatcher.addEventListener(echoEventListener2);
assertEquals(ofList(echoEventListener2, echoEventListener), dispatcher.getAllEventListeners()); // :68

dispatcher.addEventListener(echoEventListener);           // duplicate add
assertEquals(ofList(echoEventListener2, echoEventListener), dispatcher.getAllEventListeners()); // :71-72

dispatcher.removeEventListener(echoEventListener);
assertEquals(ofList((EventListener) echoEventListener2), dispatcher.getAllEventListeners());     // :75-76
```

### 4.2 匹配基于可赋值性，未命中的分发是 no-op

`event/DirectEventDispatcherTest.java:108-124`：

```java
dispatcher.addEventListener(echoEventListener);            // registered for EchoEvent
dispatcher.dispatch(new Event("Test") {});                 // :114-115
assertEquals(0, echoEventListener.getEventOccurs());       // :118  no listener ran

dispatcher.dispatch(new EchoEvent("Hello,World"));         // :121
assertEquals(1, echoEventListener.getEventOccurs());       // :124
```

同一测试（`:133-145`）表明注册在 `Event` 下的监听器会同时收到普通 `Event` 和 `EchoEvent`，即一次分发可以命中多个键
下的监听器。

### 4.3 条件过滤

`event/ConditionalEventListenerTest.java:39-76` —— `accept` 比较 source，`onEvent` 记录 source：

```java
OnlyHelloWorldEventListener listener = new OnlyHelloWorldEventListener();
eventDispatcher.addEventListener(listener);                    // :44

eventDispatcher.dispatch(new EchoEvent("1"));                  // :46
assertNull(listener.getSource());                               // :48  accept() returned false

eventDispatcher.dispatch(new EchoEvent("Hello,World"));        // :50
assertEquals("Hello,World", listener.getSource());             // :52

static class OnlyHelloWorldEventListener implements ConditionalEventListener<EchoEvent> {
    public boolean accept(EchoEvent event) { return "Hello,World".equals(event.getSource()); } // :64-66
    public void onEvent(EchoEvent event) { source = (String) event.getSource(); }               // :69-71
}
```

### 4.4 拥有多个处理方法的泛型监听器

`event/GenericEventListenerTest.java:48-83` —— 注意名为 `event(...)` 的处理方法、双参数重载以及 `protected` 方法：

```java
class MyGenericEventListener extends GenericEventListener {
    public void onEvent(EchoEvent echoEvent) { this.echoEvent = echoEvent; }          // :61-63  collected
    public void event(EchoEvent echoEvent) { assertEquals("Hello,World", echoEvent.getSource()); } // :65-67  collected
    public void event(EchoEvent echoEvent, Object arg) { this.echoEvent = echoEvent; } // :69-71  rejected (2 params)
    protected void notEvent(EchoEvent echoEvent) { this.echoEvent = echoEvent; }       // :73-75  rejected (not public)
}

eventDispatcher.dispatch(new EchoEvent("Hello,World"));
assertEquals(echoEvent, listener.getEchoEvent());                                      // :52-54
```

### 4.5 lambda 监听器自行解析事件类型

`event/DirectEventDispatcherTest.java:153-183`：

```java
EventListener<?> eventListener = event -> { eventHolder.setValue(event); counter.incrementAndGet(); };        // :159-162
EventListener<EchoEvent> echoEventEventListener = event -> { ... };                                           // :164-167

dispatcher.dispatch(new Event("Test") {});          // :175  only the Event-typed lambda fires
assertEquals(1, counter.get());                     // :177
dispatcher.dispatch(new EchoEvent("Hello,World"));  // :180  both fire
assertEquals(3, counter.get());                     // :182
```

`event/EventListenerTest.java:43-56` 证明了目标分别为 `Event.class`、`EchoEvent.class`、`FileChangedEvent.class` 的
lambda 都能被解析；`:34-41` 证明两个 `EchoEventListener` 类同样可以，其中包含继承自
`Vector<EventListener<Event>>` 的情形。

### 4.6 null 与失败路径

```java
assertThrows(IllegalArgumentException.class, () -> assertListener(null));           // event/ListenableTest.java:19
assertThrows(IllegalArgumentException.class, () -> new AbstractEventDispatcher(null) {}); // event/AbstractEventDispatcherTest.java:38
assertNull(findEventType((Class) null));                                            // event/EventListenerTest.java:60
assertDoesNotThrow(() -> dispatcher.doInListener((Class) null, eventListeners -> {})); // event/AbstractEventDispatcherTest.java:68-69
assertThrows(IllegalArgumentException.class, () -> new GenericEvent<>(null));        // event/GenericEventTest.java:46-48 (EventObject's own guard)
```

### 4.7 通过 SPI 加载的监听器

`event/EventDispatcherTest.java:47-51` 断言 `newDefault().getAllEventListeners().size() == 2` —— 这两个来自
`src/test/resources/META-INF/services/io.microsphere.event.EventListener`，其中列出了 `EchoEventListener` 与
`EchoEventListener2`。`event/AbstractEventDispatcherTest.java:42-47` 断言加载结果非空；`:49-59` 断言在 class loader
损坏时行为降级为 **no-op** 而非抛异常（因为 `loadEventListenerInstances()` 把 Throwable 吞进 `logger.trace`，
`event/AbstractEventDispatcher.java:215-218`）。

### 4.8 并行分发必须显式等待

`event/ParallelEventDispatcherTest.java:46-52`：

```java
eventDispatcher.dispatch(new EchoEvent("Hello,World"));
ForkJoinPool.commonPool().awaitTermination(1, SECONDS);   // :49 — the test must give the worker time
assertEquals(1, listener.getEventOccurs());               // :51
```

---

## 5. 兼容性说明

* **序列化**：`Event extends java.util.EventObject`（`event/Event.java:29`）并固定了
  `serialVersionUID = -1704315605423947137L`（`:31`）；`timestamp` 字段是 `final` 且非 transient（`:36`），因此会随事件
  一起序列化。测试里的 `EchoEventListener` 实现了 `Serializable`（`event/EchoEventListener.java:26`），但库本身对
  `EventListener` 没有任何 `Serializable` 要求。
* **JDK 8 到 25**：引擎只用到 `ServiceLoader`、`Executor`、`ConcurrentHashMap`、`LinkedList`、Stream 和
  `java.lang.reflect`，没有访问任何 JDK 内部实现，因此本包不需要 `--add-opens`。`findEventType` 的 lambda 分支
  （`event/EventListener.java:135-138`）确实会通过 `LambdaUtils` 反射读取生成的 lambda 类的声明方法
  （`lang/invoke/LambdaUtils.java:98-128`），那是对合成类的反射读取，并非受限的 `sun.*` 访问。
* **与 Spring 零耦合**：`event` 中没有任何类型引用 `org.springframework`。`java8-16` profile 的 Spring 版本切换
  （`microsphere-java-parent/pom.xml:54-62`）对本页描述的行为没有影响。
* **SPI 文件**：`release` 上 `core` 附带 13 个 `META-INF/services` 文件，其中包括
  `META-INF/services/io.microsphere.event.EventDispatcher`，内容为 `DirectEventDispatcher` 和
  `ParallelEventDispatcher`。**本仓库中没有任何代码通过 `ServiceLoader` 加载 `EventDispatcher`** —— 除
  `event/EventDispatcherTest.java:44` 的类身份断言之外不存在 `EventDispatcher.class` 的服务查找；该文件是为下游使用者
  发布的。主源码资源里没有 `META-INF/services/io.microsphere.event.EventListener`，只有测试资源里有（§4.7）。
* **日志的依赖方向**：`AbstractEventDispatcher` 持有 `Logger`
  （`event/AbstractEventDispatcher.java:21`、`:39`、`:97`），来自 `io.microsphere.logging.LoggerFactory`，因此分层上
  `event` 位于 `logging` *之上*；`logging` 自身并不 import `event`。

---

## 6. 陷阱与已知限制

1. **注册表只有一半是线程安全的。** `listenersCache` 是 `ConcurrentHashMap`
   （`event/AbstractEventDispatcher.java:99`，创建于 `collection/MapUtils.java:705-707`），但**它的 value 是普通
   `LinkedList`**（`event/AbstractEventDispatcher.java:197`、`collection/ListUtils.java:448-450`）。写操作持有
   `synchronized (mutex)`（`:196-202`）；分发时的读路径（`:146-152`）**完全不加锁**。因此在某次分发正在抽取监听器流
   时注册新监听器并不安全，而且没有测试覆盖这种场景。安全用法：启动阶段完成全部注册，之后再开始分发。
2. **每次分发取的是快照。** `Stream.sorted()`（`event/AbstractEventDispatcher.java:152`）是有状态操作，会在 `forEach`
   运行之前把上游全部抽取完，所以监听器集合在分发开始时就已固定。在监听器里再注册一个监听器不会影响进行中的这次分发，
   只影响下一次。
3. **异常不会被隔离。** `listener.onEvent(event)`（`event/AbstractEventDispatcher.java:177`）周围没有 `try`/`catch`；
   一个抛异常的监听器会“取消”其后所有监听器（见 §3.6）。这是引擎中唯一的中止传播手段，而且属于意外效果而非设计。
4. **`GenericEventListener` 的反射调用会重新抛成 `RuntimeException`。** `onEvent` 通过
   `ThrowableConsumer.execute` 调用每个处理方法（`event/GenericEventListener.java:109-111`），其默认处理器是
   `throw new RuntimeException(failure)`（`lang/function/ThrowableConsumer.java:104-106`、`:151-153`）—— 因此处理方法
   抛出的 `InvocationTargetException` 会被包装后上抛，并按第 3 条中止循环。
5. **处理方法按精确类型匹配。** `handleEventMethods.getOrDefault(event.getClass(), emptySet())`
   （`event/GenericEventListener.java:108`）不会向上查找父类：`GenericEventListener` 子类里声明的
   `public void onEvent(Event event)` 遇到 `EchoEvent` 时**不会**被调用，尽管分发器层面的规则（§3.4 第 2 条）是基于
   可赋值性的。
6. **无法解析的监听器会被静默丢弃。** 若 `findEventType` 返回 `null` —— 例如没有泛型信息的裸 `EventListener` ——
   `doInListener` 会不带任何警告地跳过注册（`event/AbstractEventDispatcher.java:194-195`），该监听器永远不触发。如果发现
   某个监听器“失灵”，请打印 `getAllEventListeners()`。
7. **`assertListener` 已不再拒绝 final 类。** final 检查被注释掉了（`event/Listenable.java:83-89`），而 javadoc 仍然
   宣称会抛异常（`:67-75`）。真正的拒绝只有 `null` → `IllegalArgumentException`（`:81`），由
   `event/ListenableTest.java:18-20` 断言。不要依据 javadoc 编写代码。
8. **`removeEventListener` 依赖 `equals`。** `listeners.remove(listener)`（`event/AbstractEventDispatcher.java:126`）
   与 `addIfAbsent` 里的 `contains` 判断（`:156`）都使用 `equals`，因此一个忽略对象身份的 `equals` 覆写 ——
   `event/EchoEventListener2.java:52-60` 只比较类 —— 会让两个不同实例互相顶替，第二次注册被视为重复。
9. **“并行”指的是一个任务，而不是 N 个任务。** `dispatch` 只向 executor 提交**一个** `Runnable`
   （`event/AbstractEventDispatcher.java:167`），监听器循环在该任务内部完成，所以 `ParallelEventDispatcher` 的并行发生在
   *多次 dispatch 之间*，永远不是*同一次 dispatch 的监听器之间*。监听器仍然顺序执行，而且 `dispatch()` 在任何监听器运行
   之前就已返回（§4.8）。
10. **`EventDispatcher.parallel(...)` 不做 null 检查**（`event/EventDispatcher.java:82-84`）；失败会稍后在
    `AbstractEventDispatcher` 构造器里以 `IllegalArgumentException` 暴露（`:110`）。当 executor 可能缺省时请改用
    `of(@Nullable Executor)`（`:92-95`）—— 那才是文档化的降级路径，`StandardFileWatchService` 也体现了这一意图：默认值
    就是 `DIRECT_EXECUTOR`（`io/StandardFileWatchService.java:151-153`）。
11. **`removeAllEventListeners()` 没有返回值。** 方法声明为 `default void`，而 javadoc 却承诺
    “a amount of removed listeners”（`event/Listenable.java:152-159`）。
12. **没有事件层级、没有载荷抽象、跨键没有顺序保证。** 注册在不同事件类下的监听器由 `ConcurrentHashMap` 的 stream 合并
    （`event/AbstractEventDispatcher.java:146-152`），随后只按优先级排序；不同键下优先级相同的监听器之间没有确定性的相对
    顺序。

---

## 7. 参见

* [语言抽象](./language-abstractions.zh.md) —— `Prioritized` 的排序算术、`LambdaUtils`，以及包装监听器异常的
  `Throwable*` 工具。
* [I/O、NIO、扫描与文件过滤器](./io-and-scanning.zh.md) —— `io.microsphere.io.event` 与 `StandardFileWatchService`，
  本引擎在模块内唯一的消费者。
* [日志](./logging.zh.md) —— `AbstractEventDispatcher` 用来吞掉 SPI 失败的 `Logger`。
* [集合与过滤器](./collections-and-filters.zh.md) —— `MapUtils.newConcurrentHashMap` 与 `ListUtils.newLinkedList`，
  监听器注册表正是由这两个集合搭成。
* [事件分发 —— 开发者指南](../../developer-guide/microsphere-java-core/events.zh.md) —— 分发循环、缓存失效、
  设计模式、线程安全分析与测试策略。
