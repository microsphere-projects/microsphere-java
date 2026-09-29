# 事件分发

> 语言版本：[中文](events.md) · [English](../en/events.md)
> [← 手册目录](../README.md)

## 一览

| 事实 | 取值 |
|---|---|
| 所属模块 | `io.github.microsphere-projects:microsphere-java-core` |
| 包名 | `io.microsphere.event` |
| 适用范围 | 仅 JVM 内的观察者模式——无消息代理、无远程分发 |
| 入口方法 | `EventDispatcher.newDefault()`、`EventDispatcher.parallel(Executor)`、`EventDispatcher.of(Executor)` |
| 执行顺序 | 由 `io.microsphere.lang.Prioritized` 决定——**整数越小越先执行** |
| 监听器自动注册 | `META-INF/services/io.microsphere.event.EventListener` |
| 额外依赖 | 无，只用 JDK |

整套模型只有三个类型：`Event`（带时间戳的 `java.util.EventObject`）、`EventListener<E>`（一个方法加
一个优先级）、`EventDispatcher`（按事件类型建立索引，并在某个 `Executor` 里执行监听器）。没有全局单例
分发器——每次调用工厂方法都会返回一个新实例，各自持有独立的监听器注册表。

---

## 1. 类型层次

```
Event (abstract, extends java.util.EventObject)
 ├── GenericEvent<S>
 └── FileChangedEvent                -> io-and-file-watch.md

EventListener<E extends Event> (functional interface; extends java.util.EventListener + Prioritized)
 ├── ConditionalEventListener<E>     先调 accept() 再决定是否调 onEvent()
 └── GenericEventListener            一个监听器，多个处理方法

Listenable<E extends EventListener<?>>        注册 / 注销 / 查询
 └── EventDispatcher extends Listenable<EventListener<?>>
      ├── AbstractEventDispatcher    统一的存储、排序与 SPI 加载
      │    ├── DirectEventDispatcher         final；当前线程执行
      │    └── ParallelEventDispatcher       基于 Executor
```

| 类型 | 职责 |
|---|---|
| `Event` | 事件负载；记录 `source` 与构造时刻的时间戳 |
| `EventListener<E>` | 订阅者；泛型参数 `E` **就是**订阅关系本身 |
| `ConditionalEventListener<E>` | 增加 `boolean accept(E)` 过滤 |
| `GenericEventListener` | 用反射把单个 `Event` 订阅分发到按事件类型划分的方法 |
| `Listenable<E>` | 注册契约，可脱离事件分发单独复用 |
| `EventDispatcher` | 注册 + `dispatch(Event)` |
| `AbstractEventDispatcher` | 唯一需要你继承的实现类 |

---

## 2. 定义事件

```java
public abstract class Event extends java.util.EventObject {

    public Event(Object source)      // source 不能为 null（EventObject 约定）
    public long getTimestamp()       // 构造时取 System.currentTimeMillis()
}
```

事件应做成不可变对象：字段用 `final`，不提供 setter，`source` 指向触发者。

```java
public class OrderPlaced extends Event {

    private final String orderId;
    private final BigDecimal amount;

    public OrderPlaced(Object source, String orderId, BigDecimal amount) {
        super(source);
        this.orderId = orderId;
        this.amount = amount;
    }

    public String getOrderId()   { return orderId; }
    public BigDecimal getAmount() { return amount; }
}
```

`getTimestamp()` 在 `Event` 的构造器里赋值，子类不要再记录一次时间。

如果只想携带一个负载而不愿新建事件类，用 `GenericEvent<S>`，它把 `getSource()` 的返回类型收窄为负载类型：

```java
Event event = new GenericEvent<>("order-001");   // GenericEvent#getSource() 返回 String
```

> [!NOTE]
> 订阅 `GenericEvent` 时写成 `EventListener<GenericEvent>`——分发器按监听器声明的事件类型建索引，负载
> 类型 `S` 对它没有意义。

---

## 3. 实现监听器

```java
@FunctionalInterface
public interface EventListener<E extends Event> extends java.util.EventListener, Prioritized {

    void onEvent(E event);

    default int getPriority()   // 返回 Prioritized.NORMAL_PRIORITY（0）

    static Class<? extends Event> findEventType(EventListener<?> listener)
    static Class<? extends Event> findEventType(Class<?> listenerClass)
    static Class<? extends Event> findEventType(ParameterizedType parameterizedType)
}
```

分发器在**注册时**从 `implements` 子句解析事件类型，因此 `E` 必须是具体类型：

```java
public class AuditListener implements EventListener<OrderPlaced> {

    @Override
    public void onEvent(OrderPlaced event) {
        audit.record(event.getOrderId(), event.getTimestamp());
    }

    @Override
    public int getPriority() {
        return Prioritized.MAX_PRIORITY;   // Integer.MIN_VALUE，排在所有监听器之前
    }
}
```

> [!WARNING]
> 使用裸类型 `implements EventListener`（或类型参数仍是类型变量）会解析出 `null` 事件类型，
> `AbstractEventDispatcher` 随后静默跳过注册——监听器既没被存入，也不会被调用。没有异常，也没有日志。

优先级沿用 `Prioritized` 语义，排序为**升序**，整数最小的先执行：

| 常量 | 取值 | 效果 |
|---|---|---|
| `Prioritized.MAX_PRIORITY` | `Integer.MIN_VALUE` | 最先执行 |
| `Prioritized.NORMAL_PRIORITY` | `0` | 默认值；排在所有负优先级监听器之后 |
| `Prioritized.MIN_PRIORITY` | `Integer.MAX_VALUE` | 最后执行 |

> [!NOTE]
> `EventListener.getPriority()` 的 Javadoc 声称默认值是 `Integer#MAX_VALUE`，实现里返回的是
> `NORMAL_PRIORITY`（`0`）。以代码为准。

Lambda 也可以，因为 `findEventType` 会借助 `io.microsphere.lang.invoke.LambdaUtils` 解析出唯一的参数类型；
但目标类型必须显式写出，且 Lambda 无法覆盖优先级，永远是普通优先级：

```java
dispatcher.addEventListener((EventListener<OrderPlaced>) event -> log.info("order {}", event.getOrderId()));
```

---

## 4. 取得分发器并派发

```java
public interface EventDispatcher extends Listenable<EventListener<?>> {

    Executor DIRECT_EXECUTOR = Runnable::run;   // 当前线程直接执行

    void dispatch(Event event);

    default Executor getExecutor() { return DIRECT_EXECUTOR; }

    @Nonnull static EventDispatcher newDefault()                        // -> DirectEventDispatcher
    @Nonnull static EventDispatcher parallel(@Nonnull Executor executor) // -> ParallelEventDispatcher
    @Nonnull static EventDispatcher of(@Nullable Executor executor)      // null 走 newDefault()，否则 parallel
}
```

> [!IMPORTANT]
> 工厂方法名只有 `newDefault()`、`parallel(Executor)`、`of(Executor)`，不存在 `getInstance()`、
> `create()`、`sequential(...)` 或 `builder()`。每次调用都会新建一个注册表，因此要把实例保存下来
> （`static final` 字段或交给 DI 管理为单例），而不是每次派发前调一次工厂。

同步（当前线程）派发：

```java
private static final EventDispatcher dispatcher = EventDispatcher.newDefault();

public void place(String orderId, BigDecimal amount) {
    dispatcher.dispatch(new OrderPlaced(this, orderId, amount));   // 所有监听器执行完才返回
}
```

异步派发——线程池的生命周期由你负责：

```java
ExecutorService pool = Executors.newFixedThreadPool(4,
        CustomizedThreadFactory.newThreadFactory("event-dispatch"));

ExecutorUtils.shutdownOnExit(pool);              // 注册 JVM 退出时的关闭回调

EventDispatcher dispatcher = EventDispatcher.parallel(pool);
```

`ParallelEventDispatcher` 还有一个无参构造器，使用 `ForkJoinPool.commonPool()`。测试时很方便，但它与并行
流共用同一个池，生产环境请传入自己的 `Executor`。`EventDispatcher.of(null)` 是同一选择的空安全分支：
它返回同步分发器，所以当 `Executor` 来自配置时，`of(executor)` 是最合适的写法。

---

## 5. 注册与注销监听器

```java
public interface Listenable<E extends EventListener<?>> {

    static void assertListener(EventListener<?> listener) throws IllegalArgumentException

    void addEventListener(E listener) throws NullPointerException, IllegalArgumentException;
    void removeEventListener(E listener) throws NullPointerException, IllegalArgumentException;

    default void addEventListeners(E listener, E... others)
    default void addEventListeners(Iterable<E> listeners)
    default void removeEventListeners(Iterable<E> listeners)
    default void removeAllEventListeners()

    @Nonnull List<E> getAllEventListeners();
}
```

```java
dispatcher.addEventListeners(auditListener, notifyListener, metricsListener);

dispatcher.getAllEventListeners();     // 不可变、去重、按优先级排序的快照

dispatcher.removeEventListener(auditListener);
dispatcher.removeAllEventListeners();
```

| 行为 | 可以依赖的细节 |
|---|---|
| 重复注册 | 被忽略——只在监听器不存在时才加入（依据 `equals`） |
| 同一实例订阅两种事件 | 做不到；一个监听器只会存入其唯一解析出的类型下 |
| 注销 | 必须传入同一实例；类型会重新解析，解析失败的监听器注销时是空操作 |
| `getAllEventListeners()` | `@Immutable` 快照，修改它不会影响注册表 |

> [!NOTE]
> `assertListener` 目前只判空。Javadoc 里描述的"监听器不能是 final 类或代理类"校验在源码中被注释掉了，
> 不要依赖它。

> [!WARNING]
> 注册表持有**强引用**。注册到长生命周期分发器上的监听器永远不会被回收，即使宿主对象（Servlet、窗口、
> request 作用域 Bean）本该不可达。这是内存泄漏而不是特性：请在 `finally` 块、关闭回调或宿主对象的销毁
> 方法里注销监听器。框架没有弱引用模式。

---

## 6. 用 `ConditionalEventListener` 过滤

```java
public interface ConditionalEventListener<E extends Event> extends EventListener<E> {

    boolean accept(E event);
}
```

`accept` 由分发器在调用 `onEvent` 之前、在派发线程上、对每个匹配类型的事件逐一求值；未通过的事件不会进入
`onEvent`。

```java
public class HighValueOrderListener
        implements EventListener<OrderPlaced>, ConditionalEventListener<OrderPlaced> {

    @Override
    public boolean accept(OrderPlaced event) {
        return event.getAmount().compareTo(BigDecimal.valueOf(10_000)) > 0;
    }

    @Override
    public void onEvent(OrderPlaced event) {
        riskTeam.alert(event.getOrderId());
    }
}
```

> [!TIP]
> 像上面这样同时声明两个接口：分发器只靠 `instanceof ConditionalEventListener` 判断，但显式写出
> `EventListener<E>` 能让订阅的事件类型在日后修改过滤条件时依然清晰。`accept` 要保持廉价且无副作用——
> 每个监听器对每个事件都会调用它一次。

---

## 7. 一个监听器多个处理器：`GenericEventListener`

```java
public abstract class GenericEventListener implements EventListener<Event> {

    public final void onEvent(Event event)                 // 通过反射路由到处理方法
    protected boolean isHandleEventMethod(Method method)   // 覆盖它以放宽或收紧筛选
}
```

继承它，并为每种事件写一个方法：

```java
public class OrderAnalytics extends GenericEventListener {

    public void onOrderPlaced(OrderPlaced event) {
        counters.increment("orders");
    }

    public void onOrderCancelled(OrderCancelled event) {
        counters.decrement("orders");
    }
}
```

一个方法要成为处理器，以下条件必须全部满足（由 `isHandleEventMethod` 对 `getClass().getMethods()` 逐项检查）：

| 规则 | 后果 |
|---|---|
| 不是 `onEvent(Event)` 自身 | 路由方法本身不会被当成处理器 |
| 返回 `void` | 有返回值的方法（链式、`Optional`）会被跳过 |
| 不声明任何异常 | 写了 `throws IOException` 就不合格，即使受检 |
| 参数恰好一个 | 无参数或多参数方法被忽略 |
| 参数是 `Event` 子类 | 参数为 `String`、`Integer` 的方法被忽略 |

> [!IMPORTANT]
> 两个路由细节，均来自源码：
> 1. `GenericEventListener` 解析出的事件类型是 `Event`，因此它存放在 `Event` 键下，会对**所有**派发的
>    事件调用一次。
> 2. `onEvent` 内部用 `handleEventMethods.get(event.getClass())` 查找方法——是**精确类型匹配**。声明为
>    父事件类型的处理器不会响应子类型事件，这与第 9 节分发器层面的匹配规则不同。
>
> `onEvent` 是 `final` 的，只能通过新增处理方法来扩展，不能覆盖。

---

## 8. 监听器的 SPI 自动加载

`AbstractEventDispatcher` 的构造器在最后调用 `loadEventListenerInstances()`：

```java
protected void loadEventListenerInstances() {
    execute(() -> loadServicesList(EventListener.class, getDefaultClassLoader(), true)
            .stream()
            .sorted()
            .forEach(this::addEventListener),
            e -> logger.trace(e.getMessage()));
}
```

应用侧要提供的文件名以 **`EventListener`** 命名，每行一个实现类，且这些类必须有公共无参构造器：

```
src/main/resources/META-INF/services/io.microsphere.event.EventListener
```

```
com.example.listener.AuditListener
com.example.listener.HighValueOrderListener
```

> [!IMPORTANT]
> 两个常见误区：
> 1. `microsphere-java-core` 自带的 `META-INF/services/io.microsphere.event.EventDispatcher`
>    （内容是 `DirectEventDispatcher` 和 `ParallelEventDispatcher`）**不会**被
>    `AbstractEventDispatcher` 读取，与监听器自动加载无关；只有 `...EventListener` 文件才起作用。
> 2. 失败被吞在 trace 级别。当 `...EventListener` 服务文件不存在时，`loadServicesList` 抛出
>    `IllegalArgumentException`，而 `loadEventListenerInstances` 只用 `logger.trace` 打印。所以文件名写错
>    或漏建文件的表现就是"监听器莫名其妙收不到事件"。接线阶段请打开 `io.microsphere` 的 TRACE 日志，
>    参见 [日志](logging.md)。

依赖 SPI 加载时需要注意的细节：

* 加载使用**默认类加载器**（`io.microsphere.util.ClassLoaderUtils#getDefaultClassLoader`），它看不到的监听器
  分发器也看不到。
* 这里传入的 `true` 是字面量，实例会被记入 `ServiceLoaderUtils` 的缓存；系统属性
  `microsphere.service-loader.cached`（常量
  `ServiceLoaderUtils.SERVICE_LOADER_CACHED_PROPERTY_NAME`，默认 `false`）并**不影响**监听器自动加载，它只
  为那些没有显式传缓存标志的 `ServiceLoaderUtils` 重载提供默认值。
* 由于按服务类型缓存，同一个类加载器下**每个分发器拿到的都是同一批监听器实例**，所以 SPI 监听器必须无状态
  且线程安全。
* 想在子类中禁用或替换 SPI 加载，就覆盖 `loadEventListenerInstances()`。
* 加载只在构造器里发生一次，先于分发器可用——你第一次调用 `addEventListener` 时，监听器集合已经排好序。

---

## 9. 分发机制与自定义分发器

存储结构是以事件类型为键的 `ConcurrentMap<Class<? extends Event>, List<EventListener>>`。

* **写路径**（`addEventListener` / `removeEventListener`）在 `synchronized (mutex)` 内执行：追加或移除后，
  用 `Collections.sort` 对该列表重新排序。排序发生在写入时，而不是派发时。
* **读路径**是无锁的：取出所有满足 `key.isAssignableFrom(event.getClass())` 的缓存项，把监听器合成一个流并
  `.sorted()`，对每个回调先过 `ConditionalEventListener.accept`，再调 `onEvent`——整个过程在
  `getExecutor().execute(...)` 里运行。

由于匹配使用 `isAssignableFrom`，订阅**父**事件类型的监听器会收到子类型事件；订阅子类型的监听器收不到父事件。

```java
public class MdcEventDispatcher extends AbstractEventDispatcher {

    public MdcEventDispatcher(Executor executor) {
        super(executor);       // executor 不能为 null
    }

    // getExecutor() 在 AbstractEventDispatcher 中是 final，所以上下文传递应包在 Executor 装饰器里，
    // 而不是通过覆盖方法实现：
    public static EventDispatcher newMdcDispatcher(Executor delegate) {
        Executor mdcAware = command -> delegate.execute(() -> {
            Map<String, String> context = MDC.getCopyOfContextMap();
            try {
                command.run();
            } finally {
                if (context != null) MDC.setContextMap(context); else MDC.clear();
            }
        });
        return new MdcEventDispatcher(mdcAware);
    }
}
```

`AbstractEventDispatcher` 提供的受保护扩展点：

| 成员 | 用途 |
|---|---|
| `protected final Logger logger` | 子类日志，与[日志](logging.md)保持一致 |
| `protected Stream<EventListener> sortedListeners()` | 按优先级遍历全部监听器 |
| `protected Stream<EventListener> sortedListeners(Predicate<Map.Entry<Class<? extends Event>, List<EventListener>>> filter)` | 遍历过滤后的子集 |
| `protected void doInListener(EventListener<?>, Consumer<Collection<EventListener>>)` | 在互斥锁保护下修改某个监听器列表 |
| `protected void loadEventListenerInstances()` | 替换或禁用 SPI 加载 |

> [!TIP]
> 大多数场景用 `EventDispatcher.parallel(executor)` 或 `of(executor)` 就够了。只有需要改变监听器加载方式或
> 拦截逻辑时才继承 `AbstractEventDispatcher`，并且记住 `getExecutor()` 是 `final`：`Executor` 通过构造器传入，
> 不能靠覆盖 getter 来换。

---

## 10. 线程安全与异常行为

| 操作 | 保证 |
|---|---|
| `dispatch(Event)` | 对 `ConcurrentMap` 做无锁读，可并发调用 |
| `addEventListener` / `removeEventListener` | 在内部互斥锁上同步；会阻塞同一类型的其他写操作 |
| `DirectEventDispatcher` 回调 | 在调用者线程按优先级顺序执行；异常抛给调用者 |
| `ParallelEventDispatcher` 回调 | 并发执行——监听器必须线程安全；异常留在任务里 |
| SPI 加载的监听器 | 每个服务类型/类加载器共享一组实例，在构造器中加载 |

`AbstractEventDispatcher` 的状态基于 `ConcurrentMap`，因此在另一线程派发的同时注册监听器是安全的；只是正在
进行的那次派发可能看不到新监听器。

> [!WARNING]
> 在 `DirectEventDispatcher` 中抛出的异常会中断该事件剩余监听器，并把异常抛到 `dispatch` 调用处。如果后续
> 监听器必须执行，请在 `onEvent` 内部自行捕获。使用 `ParallelEventDispatcher` 时失败完全不会反馈给调用者——
> 需要在监听器内部记日志，或包装 `Executor`。

共享的监听器请标注 [`@ThreadSafe` / `@NotThreadSafe`](annotations.md#1-reference)，把约定写在类型上而不是注释里。

---

## 11. 端到端示例

```java
import io.microsphere.event.*;
import io.microsphere.lang.Prioritized;

public class Orders {

    private static final EventDispatcher dispatcher = EventDispatcher.newDefault();

    static {
        dispatcher.addEventListeners(new AuditListener(), new HighValueOrderListener());
    }

    public static void place(String id, BigDecimal amount) {
        dispatcher.dispatch(new OrderPlaced(Orders.class, id, amount));
    }

    static class AuditListener implements EventListener<OrderPlaced> {

        @Override
        public void onEvent(OrderPlaced event) {
            System.out.printf("AUDIT %s at %d%n", event.getOrderId(), event.getTimestamp());
        }

        @Override
        public int getPriority() {
            return Prioritized.MAX_PRIORITY;   // 最先执行
        }
    }
}
```

```
AUDIT ORD-001 at 1759108800000
```

---

## 参见

* [I/O 与文件监听](io-and-file-watch.md) —— `FileChangedEvent` / `FileChangedListener`，框架自身的事件消费者
* [注解](annotations.md) —— 在事件与监听器类型上使用 `@ThreadSafe`、`@Immutable`
* [反射与类型系统](reflection-and-types.md) —— `findEventType` 如何解析泛型参数
* [日志](logging.md) —— 打开 TRACE 观察 SPI 加载过程
* [参考手册](reference.md) —— 完整的 SPI 文件清单，包括 `io.microsphere.event.EventDispatcher`

[← 手册目录](../README.md) · [上一篇：类型转换](type-conversion.md) · [下一篇：I/O 与文件监听 →](io-and-file-watch.md)
