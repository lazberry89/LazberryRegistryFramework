# Lazberry Registry Framework (LRF)
## Technical Architecture and Implementation Specification (Rev. 2026.1)

### 1. Architectural Philosophy and Motivation
The Lazberry Registry Framework (LRF) is an enterprise-grade, high-performance Inversion of Control (IoC) and Dependency Injection (DI) engine specifically engineered for highly distributed, multi-proxy Minecraft server topologies. Traditional application frameworks like Spring or Guice introduce substantial runtime reflection overhead, large memory footprints, and lack native understanding of game-loop dynamics, tick-rate constraints, and server-proxy network layouts.

LRF solves these domain-specific challenges through **Deterministic Architectural Invariants**:

* **Concrete Enforcement (Anti-Virtual Invariant)**: The framework completely bans ambiguous IoC state mapping. Interfaces and abstract blueprints are strictly isolated as declarative models (`@Virtual`), forcing explicit concrete implementation binding to guarantee predictable memory layouts and zero runtime lookup ambiguity.
* **OOP Invariant Preservation via Encapsulation Locks**: Native Bukkit lifecycle hooks (`onEnable`, `onDisable`) are declared `final` at the master class level. This enforces safe, symmetrical teardown/startup mechanics, preventing user code from bypassing IoC container assembly or graceful shutdown queues.
* **Aggressive Fail-Fast Runtime-Safety**: Rather than allowing hidden errors or lazy initialization failures to surface mid-game (potentially crashing tick loops or corrupting player data), LRF executes top-down topological graph verification and configuration mapping at boot time. Any structural failure instantly aborts the bootstrapping sequence.

---

### 2. Main Bootstrapper & Subclass Specification (`JavaPlugin` Encapsulation)

To eliminate boilerplate setup and enforce strict lifecycle encapsulation, main plugin entry points must extend `LazberryRegistryFramework` directly instead of raw Bukkit `JavaPlugin`.

```
[Server Boot] 
      │
      ▼
┌────────────────────────────────────────────────────────┐
│ LazberryRegistryFramework.onEnable() [LOCKED final]   │
├────────────────────────────────────────────────────────┤
│ 1. Context Allocation & Package Boundary Discovery     │
│ 2. ClassPath Scanning & Config Mapping (@ConfigObject) │
│ 3. IoC Graph Resolution & ByteBuddy Proxy Assembly    │
│ 4. Scheduler & Event Binding                           │
│ 5. Developer Hook Dispatch ───────────────────────────┼──► onLrfEnable()
└────────────────────────────────────────────────────────┘
```

#### 2.1. Inviolable Lifecycle Locks (`final` Enforcement)
`LazberryRegistryFramework` locks native Bukkit entry points to prevent lifecycle fragmentation:
```java
@Override
public final void onEnable() {
    boot(this, getClass());
    onLrfEnable();
}

@Override
public final void onDisable() {
    try {
        onLrfDisable();
    } finally {
        ScheduleProcessor.cancelAllSchedules();
        ShutdownRegistry.executeShutdownSequence();
        cleanUp(this, getClass());
    }
}
```

#### 2.2. User Implementation Guide (Concrete Main Class)
Developers implement user-level setup and teardown logic via protected lifecycle hooks:

```java
package com.lazberry.myplugin;

import Framework.LazberryRegistryFramework.LazberryRegistryFramework;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MyPluginMain extends LazberryRegistryFramework {

    @Override
    protected void onLrfEnable() {
        // Invoked IMMEDIATELY AFTER LRF IoC Container and Proxy initialization
        log.info("MyPlugin successfully booted on LRF Core!");
    }

    @Override
    protected void onLrfDisable() {
        // Invoked IMMEDIATELY BEFORE Task Cancellation & IoC Container Teardown
        log.info("MyPlugin initiating safe shutdown sequence...");
    }
}
```

* **Implicit Package Topology Discovery**: Calling `boot(this, getClass())` automatically intercepts `mainClass.getPackageName()`. This establishes the implicit reflection scanning boundary without hardcoded package string parameters.

---

### 3. Global Component Matrix and Package Structure

#### 3.1. Root Domain Configuration (`Framework`)
* **`Framework.Annotation`**: Core structural markers governing component boundaries (`@Component.Include`, `@Conditional`), instantiation selection (`@Inject`), template classification (`@ConsumableClass`, `@Virtual`), and runtime semantic declarations (`@Document`, `@Transactional`).
* **`Framework.FrameworkExceptions`**: Terminal boundary violation exceptions that halt bootstrapping upon structural invariants breach.
* **`Framework.Utils`**: Low-level, high-throughput primitives (`ParseEnum`, `ColorUtils`, `IDGenerator`).
* **Lifecycle & Topology Abstractions**: Contextual environment markers (`Local`, `Global`) registered into `ServerType` prior to scanning.

#### 3.2. Engine Core (`Framework.LazberryRegistryFramework`)
* **`Framework.LazberryRegistryFramework.Annotation`**: Spigot/Paper specific hooks (`@Commands`, `@Listeners`, `@Schedule`, `@ConfigObject`, `@GracefulShutdown`, `@InboundChannel`, `@OutboundChannel`).
* **`Framework.LazberryRegistryFramework.Monitoring`**: Microsecond-precision telemetric subsystem (`PerformanceRegistry`, `MetricData`).
* **Core Orchestration Engines**: Classpath traversal (`PackageScanner`), reflection caching (`Reflections`), dependency graph assembly (`DependencyContainer`), type-safe config mapping (`ConfigObjectMapper`), AOP proxy generation (`LrfProxyFactory`), and network message routing (`PluginMessageRouter`).

---

### 4. Component Scanning, O/R Mapping, and Pipeline Filters

The `PackageScanner` pipeline enforces multi-stage filtering to isolate runtime components.

```
[Classpath Traversal] ──► [Interface / Abstract Check]
                                   │
                                   ▼
                       [Consumable / Virtual Filter]
                                   │
                                   ▼
                       [Server Compatibility Check]
                                   │
                                   ├─────────────────────────┐
                                   ▼                         ▼
                        [@ConfigObject Mapping]    [Component Collection]
                                   │                         │
                                   ▼                         ▼
                        [Direct IoC Registration]  [Conditional Verification]
                                                             │
                                                             ▼
                                                    [Target Assembly List]
```

#### 4.1. Configuration O/R Mapping (`@ConfigObject`)
To eliminate raw `FileConfiguration.get()` calls and guarantee type-safety, LRF features a zero-boilerplate YAML-to-POJO/Record mapping engine (`ConfigObjectMapper`).

* **Record Support**: Supports immutable Java Records (Java 16+) and standard POJOs.
* **Instantiation Phase**: Executed during scanning before constructor injection, registering mapped config instances into `DependencyContainer`.

```java
@ConfigObject(path = "database", file = "config.yml")
public record DatabaseConfig(
    String host,
    int port,
    String username,
    String password,
    int maxPoolSize
) {}
```

#### 4.2. Virtual Blueprint Guard (`@Virtual`)
Abstract templates, mock structures, or base classes meant for inheritance are annotated with `@Virtual`. `PackageScanner` drops these targets immediately, preventing incomplete blueprints from polluting the IoC container.

---

### 5. IoC Assembly Line & Topological Graph Resolution

The `DependencyContainer` manages a transactional, top-down assembly line to compile concrete application components.

#### 5.1. Resolution Strategy
1. **Cache Interception**: Scans `BEAN_CONTAINER` using `Class.isAssignableFrom` for single-address memory contract representation.
2. **Strict Abstraction Barrier**: Non-concrete types passed directly into the assembly line trigger a `VirtualClassInjectException`.
3. **Cyclic Lock Defusion**: The `CONSTRUCTION_STACK` (`LinkedHashSet`) tracks active construction chains. Detecting an active type token in a nested parameter immediately raises a `CircularDependencyException`.

#### 5.2. Constructor Selection & Parameter Injection
1. Constructor preference is given to `@Inject` decorated constructors, falling back to public zero-arg handles.
2. Parameters annotated with `@ConfigValue` route to `ConfigInjection.resolve()`.
3. Parameter classes annotated with `@ConfigObject` or standard `@Component` models recursively resolve via `DependencyContainer.getOrCreateBean()`.

---

### 6. Defensive Exception Framework Architecture

| Exception Class | Root Cause Trigger | Architectural Purpose |
| :--- | :--- | :--- |
| `VirtualClassInjectException` | Direct injection attempt of an interface, abstract class, or `@Virtual` token. | Guarantees that only concrete, instantiable singletons populate the IoC context. |
| `CircularDependencyException` | Self-referential loop intercepted in the active constructor stack trace. | Prevents thread freezing and infinite recursion stack overflow crashes. |
| `NotCompatibleWithServerException` | Component environmental requirements fail the active platform check. | Prevents class-loading crashes caused by missing NMS versions or invalid server bindings. |
| `NotValidInitializeTimingException` | Initialization hook executed out-of-bounds from the current container phase. | Enforces strict chronological order across multi-module managers. |
| `InvalidConfigInjectException` | Missing configuration nodes or structural type mismatch during value resolution. | Ensures that corrupted or incomplete files fail early before runtime errors occur. |

---

### 7. Teardown and Resource Purging Pipeline

To prevent JVM classloader memory leaks during hot-reloads (`/reload`), LRF enforces an inverted teardown sequence:

1. **User Teardown Dispatch**: Executes developer logic in `onLrfDisable()`.
2. **Task Freezing**: `ScheduleProcessor.cancelAllSchedules()` systematically cancels active asynchronous/synchronous task loops.
3. **Prioritized Shutdown**: Handlers tagged with `@GracefulShutdown` execute in prioritized sequence to dump memory caches and persist database state.
4. **Static Container Purge**: `cleanUp()` unregisters `ServerType` references and wipes static container maps, releasing classloader handles for complete Garbage Collection.
