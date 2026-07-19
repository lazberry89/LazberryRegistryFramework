# Lazberry Registry Framework (LRF)
## Technical Architecture and Implementation Specification

### 1. Architectural Philosophy and Motivation
The Lazberry Registry Framework (LRF) is an enterprise-grade, high-performance Inversion of Control (IoC) and Dependency Injection (DI) engine specifically engineered for highly distributed, multi-proxy Minecraft server topologies. Traditional application frameworks like Spring or Guice introduce substantial runtime reflection overhead, large memory footprints, and lack native understanding of game-loop dynamics, tick-rate constraints, and server-proxy network layouts.

LRF solves these domain-specific challenges by enforcing a strict, deterministic compilation and bootstrapping pipeline. The framework operates on the principle of aggressive boot-time fail-fast mechanics, deterministic topological dependency resolution, and complete environmental isolation. By separating components into immutable singletons, transient consumer payloads, and platform-specific drivers, LRF guarantees optimal memory allocation and maximum throughput within execution threads.

---

### 2. Global Component Matrix and Package Structure

The ecosystem is partitioned into two primary layers: the root orchestration domain (`Framework`) and the engine implementation core (`Framework.LazberryRegistryFramework`).

#### 2.1. Root Domain Configuration (`Framework`)
* **`Framework.Annotation`**: Contains core structural markers governing the component registration boundaries (`@Registry`, `@Conditional`), instantiation selection (`@Inject`), and runtime semantic declarations (`@ConsumableClass`, `@Document`).
* **`Framework.FrameworkExceptions`**: A dedicated suite of runtime exceptions enforcing the strict invariants of the IoC container. Every exception represents a terminal boundary violation that aborts the bootstrapping sequence to prevent systemic corruption.
* **`Framework.Utils`**: Low-level, foundational primitives optimizing core operations such as type-safe enum parsing (`ParseEnum`), high-performance rich-text processing (`ColorUtils`), and unique identification vectors (`IDGenerator`).
* **Lifecycle and Topology Abstractions**: Definitive abstractions establishing whether the framework instance is operating on a localized game-server thread boundary (`Local`) or a centralized network proxy boundary (`Global`) via the `ServerType` and `ServerManager` registries.

#### 2.2. Engine Core (`Framework.LazberryRegistryFramework`)
* **`Framework.LazberryRegistryFramework.Annotation`**: Platform-specific hooks that programmatically bind components to Spigot/Bungee/Velocity lifecycle loops, including packet routers (`@InboundChannel`, `@OutboundChannel`), asynchronous schedulers (`@Task`), event consumers (`@Listeners`), command maps (`@Commands`), and automated metadata purgers (`@SelfDestruct`).
* **`Framework.LazberryRegistryFramework.Monitoring`**: A telemetric subsystem designed to profiles execution latencies, intercept execution bottlenecks, and classify execution spikes using microsecond-precision benchmarks defined by the `Observation` matrix.
* **Core Orchestration Engines**: The infrastructural components executing classpath scanning (`PackageScanner`), reflection caching (`Reflections`), constructor-level dependency mapping (`DependencyContainer`), dynamic configuration mapping (`ConfigInjection`), and inter-server packet orchestration (`PluginMessageRouter`).

---

### 3. The IoC Assembly Line and Lifecycle Mechanics

The heart of LRF is the `DependencyContainer`, which coordinates a transactional, recursive, top-down assembly line to compile concrete application components.

```
[Classpath Scanning] -> [Environmental Evaluation] -> [Topological Loop Verification] -> [Parameter Resolution] -> [Materialization & Lifecycle Hook Trigger]
```

#### 3.1. Topological Graph Resolution Strategy
When a component registration is initiated via `getOrCreateBean(Class<?>)`, the engine utilizes an explicit, order-retaining transactional resolution graph:
1.  **Cache Interception**: The internal `BEAN_CONTAINER` (backed by a deterministic `LinkedHashMap`) is scanned using assignability checks (`Class.isAssignableFrom`). This allows polymorphic lookups while ensuring a single memory address represents the unified type contract.
2.  **Strict Abstraction Barrier**: LRF completely bans the registration of non-concrete types. If an interface or abstract class token passes directly into the assembly line, the system raises a `VirtualClassInjectException`. This forces clear implementation mapping and eliminates ambiguous container states.
3.  **Cyclic Lock Defusion**: To prevent catastrophic, infinite execution loops during complex graph evaluation, the container records active targets inside the `CONSTRUCTION_STACK` (`LinkedHashSet`). If a nested parameter requests a type token already present in this execution trail, a `CircularDependencyException` is instantly raised, aborting the thread and exposing the exact cyclic dependency path.

#### 3.2. Priority Constructor Selection and Injection Pipeline
The engine prioritizes constructors decorated with the `@Inject` annotation. In its absence, it drops back to the default public zero-argument constructor handle.

Once the target constructor is selected, the parameters undergo a split-routing resolution:
* **Configuration Slots**: Parameters bearing the `@ConfigValue` annotation bypass the standard bean lookup layer. The engine routes the associated metadata to `ConfigInjection.resolve()`, which performs path extraction, fallback evaluation, and type-safe casting from external `yaml` configuration assets. Any breakdown in mapping or structure triggers an `InvalidConfigInjectException`.
* **Dependency Slots**: Parameters requiring object references recursively re-enter the `getOrCreateBean` pipeline, ensuring that leaf nodes are compiled first before rolling up to instantiate the parent structure via reflection.

#### 3.3. Post-Instantiation Initialization and Verification Boundaries
Immediately following object materialization, but prior to its global publication into the container cache, the instance is evaluated against the `LrfInitializer` contract. If matched, the engine invokes `afterPropertiesSet()`.

This step serves as an operational synchronization lock. If a bean attempts to interact with low-level platform handles or external components prior to its designated lifecycle window, the framework intercepts the sequence and throws a `NotValidInitializeTimingException`.

---

### 4. Environmental Partitioning and Conditional Routing

LRF provides a powerful, multi-layered declarative filtering model that completely strips or includes modules based on structural environment parameters.

#### 4.1. The `@Registry` Matrix (`Include` vs. `Exclude`)
The engine leverages `@Registry.Include` and `@Registry.Exclude` to handle multi-platform target compilation within a unified codebase. Each annotation accepts an array of classes extending `ServerType`.

During the initial boot-phase, `ServerType.REGISTRY` maps the structural identities of the running platform. The `PackageScanner` cross-references these arrays against the active profile:
* If a class is marked with `@Registry.Include(Local.class)` but the framework is executing on a `Global` proxy core, the component is completely omitted from the allocation queue.
* If `ServerUtils.unCompatibleWithCurrentServer(Class<?>)` catches a platform feature mismatch, a `NotCompatibleWithServerException` is triggered, terminating execution before invalid byte-code or missing NMS symbols can corrupt the active JVM process.

#### 4.2. Functional Guarding via `@Conditional`
For complex runtime environment tracking (e.g., feature toggles, specialized database states, or dynamic mini-game subtypes), LRF integrates the `@Conditional` annotation coupled with the `ConditionalRegistry` functional interface.

Before any structural compilation occurs, the framework uses reflection to instantiate the designated condition evaluator. It then runs the `matches()` method. If the evaluation yields `false`, the component is completely dropped from the container loop, guaranteeing a zero-byte memory footprint for unneeded infrastructure.

---

### 5. Defensive Exception Framework Architecture

The framework's reliability relies heavily on its dedicated exception architecture under `Framework.FrameworkExceptions`. Rather than utilizing generic Java exceptions, LRF utilizes highly specialized runtime exceptions to pinpoint configuration, structural, or sequence failures during the initialization phase:

| Exception Class | Root Cause Trigger | Architectural Purpose |
| :--- | :--- | :--- |
| `VirtualClassInjectException` | Direct injection attempt of an interface or abstract class token. | Guarantees that only concrete, instantiable singletons pollute the IoC context. |
| `CircularDependencyException` | Self-referential loop intercepted in the active constructor stack trace. | Prevents thread freezing and infinite recursion stack overflow crashes. |
| `NotCompatibleWithServerException` | Component environmental requirements fail the active platform check. | Prevents class-loading crashes caused by missing NMS versions or invalid server bindings. |
| `NotValidInitializeTimingException` | Initialization hook executed out-of-bounds from the current container phase. | Enforces strict chronological order across multi-module managers. |
| `InvalidConfigInjectException` | Missing configuration nodes or structural type mismatch during value resolution. | Ensures that corrupted or incomplete files fail early before runtime errors occur. |

---

### 6. Low-Level System and Utility Engines

The subsystem performance is supported by specialized utility engines that handle high-throughput operations:

* **`ParseEnum`**: Utilizes an immutable record design pattern. It caches enum parsing operations and internally encapsulates casing adjustments via a defensive `toUpperCase().trim()` pipeline. This mitigates the performance penalties typically caused by `IllegalArgumentException` handling.
* **`ColorUtils`**: Pre-configures and caches immutable `LegacyComponentSerializer` instances from the Kyori Adventure text framework. By handling both hex-based RGB arrays and traditional ampersand (`&`) notation through a centralized, reflection-free serializer, it minimizes garbage collection overhead in packet transmission layers.
* **`DestructiveClassEngine`**: Coordinates the framework teardown sequences. Components marked with `@SelfDestruct` or implementing `Destructible` are tracked through a cleanup registry. Upon plugin deactivation, it performs memory-purge routines, flushes outbound network channels, and clears the static registries to enable clean reload cycles.