# Lazberry Registry Framework (LRF)
> A lightweight, high-performance Inversion of Control (IoC) and Dependency Injection (DI) framework designed specifically for Spigot / Paper MC server plugins.

---

## 📌 Overview

**Lazberry Registry Framework (LRF)** simplifies Minecraft plugin development by providing a modern Spring-like declarative ecosystem tailored for high-concurrency Bukkit environments. 

By automating dependency graphs, command maps, event listener bindings, and asynchronous tasks using clean Java Annotations, **LRF** completely eliminates boilerplate initialization code in your `onEnable()` lifecycle.
## Installation Guide [![JitPack](https://jitpack.io/v/lazberry89/LazberryRegistryFramework.svg)](https://jitpack.io/#lazberry89/LazberryRegistryFramework)
`Gradle`
```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
		mavenCentral()
		maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.lazberry89:LazberryRegistryFramework:v1.0.7'
	annotationProcessor 'com.github.lazberry89:LazberryRegistryFramework:v1.0.7'
}
```

`gradle.kts`
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
		maven { url = uri("https://jitpack.io") }
	}
}

dependencies {
    implementation("com.github.lazberry89:LazberryRegistryFramework:v1.0.7")
	annotationProcessor("com.github.lazberry89:LazberryRegistryFramework:v1.0.7")
}
```

`maven`
```xml
<repositories>
    <repository>
    	<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>

<dependency>
    <groupId>com.github.lazberry89</groupId>
	<artifactId>LazberryRegistryFramework</artifactId>
	<version>v1.0.7</version>
</dependency>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.13.0</version>
            <configuration>
                <source>21</source>
                <target>21</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.github.lazberry89</groupId>
                        <artifactId>LazberryRegistryFramework</artifactId>
                        <version>v1.0.7</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Quick Start Guide for `Lazberry Registry Framework`

### 1. Framework rules
This framework is governed by `CORS` rule.
| Key | Principle | Description |
| :---: | :--- | :--- |
| **`C`** | **Concrete** | LRF only manages concrete classes and instances. Abstract entities like `interface` or `abstract class` are strictly dismissed during IoC processing unless marked as declarative models. |
| **`O`** | **OOP Invariant** | Object-Oriented Programming principles are strictly enforced. Vital lifecycle handles are locked via final encapsulation to preserve domain integrity and object boundaries. |
| **`R`** | **Runtime-Safety** | Enforces fail-fast mechanics during the boot phase. Dependency cycles, configuration mismatches, and platform incompatibilities are caught early to prevent runtime server crashes. |
| **`S`** | **Structural Isolation** | Isolates runtime singletons from template classes and proxies using `@Virtual` and `@ConsumableClass`, preventing memory pollution and garbage collection bottlenecks. |

---

### 2. Root Domain Annotations

| Annotation | Target | Description |
| :--- | :--- | :--- |
| `@Component` | `Class` | Registers target class as an IoC bean. Supports inner annotations (`@Include`, `@Exclude`) to control platform-specific scanning rules based on `ServerType`. |
| `@Inject` | `Constructor` | Primary constructor handle for IoC dependency resolution. Automatically injects required dependencies into the target component upon initialization. |
| `@ConsumableClass` | `Class` | Marks reusable template structures or DTOs (Data Transfer Objects) to explicitly exclude them from default singleton IoC bean instantiation. |
| `@Virtual` | `Class` | Applied to interfaces, abstract blueprints, or base templates to skip automatic IoC scanning and enforce concrete-only binding mechanics. |
| `@Conditional` | `Class` | Evaluates runtime conditions via `ConditionalRegistry` to dynamically include or exclude the target component from the IoC container. |
| `@Async` | `Method` | Marks methods to be executed asynchronously on background threads, isolating compute-heavy tasks from primary execution loops. |
| `@Sync` | `Method` | Enforces synchronous execution within the main server tick thread boundary. |
| `@Transactional` | `Method` | Wraps execution blocks in transactional boundaries to guarantee atomic state changes and safe rollback capabilities. |
| `@Document` | `Class` / `Field` | Attaches structural metadata and documentation tags to runtime components for internal framework introspection. |
| `@SelfDestruct` | `Class` / `Method` | Designates dynamic resources or temporary handlers to be unregistered and memory-purged during cleanup cycles. |

### 3. Engine & Platform Annotations

| Annotation | Target | Description |
| :--- | :--- | :--- |
| `@Commands` | `Class` | Registers command map handlers directly into the underlying Paper/Spigot command execution framework. |
| `@Listeners` | `Class` | Automatically registers Bukkit event listener methods to the active `PluginManager` during container initialization. |
| `@ConfigObject` | `Class` / `Record` | Binds a YAML configuration section directly to an immutable Java Record or POJO, auto-registering the mapped object into the IoC container. |
| `@ConfigValue` | `Parameter` | Injects primitive or structured values extracted from target YAML files directly into constructor parameters or fields. |
| `@GracefulShutdown` | `Method` / `Class` | Registers high-priority teardown tasks to dump memory caches, flush network buffers, and save state prior to server unload. |
| `@Schedule` | `Method` | Binds method execution loops to synchronous or asynchronous Bukkit scheduler channels with configurable delay/period constraints. |
| `@Task` | `Class` | Declares background worker tasks or scheduled jobs managed under the framework execution pool. |
| `@InboundChannel` | `Class` | Binds incoming proxy/server plugin messaging channels for inter-server network communication. |
| `@OutboundChannel` | `Class` | Binds outgoing proxy/server plugin messaging channels to dispatch network packets. |
| `@Monitor` | `Method` / `Class` | Attaches telemetric instrumentation to measure execution latency, microsecond spikes, and performance metrics. |
| `@Reflection` | `Method` | Bypasses standard access controls via optimized reflection handles for high-performance low-level field/method manipulation. |

### 4. Automation of DI in framework
```java
package com.lazberry.myplugin.service;

import Framework.Annotation.Component;
import Framework.Annotation.Inject;
import com.lazberry.myplugin.repository.UserRepository;
import com.lazberry.myplugin.config.PluginConfig;

@Component
public class UserService {

    private final UserRepository userRepository;
    private final PluginConfig pluginConfig;

    @Inject
    public UserService(UserRepository userRepository, PluginConfig pluginConfig) {
        this.userRepository = userRepository;
        this.pluginConfig = pluginConfig;
    }

    public void processUserData(String uuid) {
        if (pluginConfig.isLoggingEnabled()) {
            userRepository.findUser(uuid);
        }
    }
}
```
* **Role:**
  * Tells the LRF IoC container to manage `UserService` as a singleton bean.
  * `@Inject` instructs the framework to automatically resolve and supply required dependencies (`UserRepository`, `PluginConfig`) into the constructor at boot time.

* **Constraints:**
  * **Single Constructor Constraint:** If multiple constructors exist, `@Inject` must be explicitly placed on the intended constructor.
  * **Dependency Requirement:** All parameter types inside the `@Inject` constructor must also be registered as valid IoC components, `@ConfigObject` beans, or framework instances.
  * **Class Rule:** The target class must not be an `interface` or `abstract class`. The annotation that skips IoC scanning on virtual classes is `@Virtual` and `@ConsumableClass`.

 ### 5. Configuration Object Mapping Example

```java
package com.lazberry.myplugin.config;

import Framework.LazberryRegistryFramework.Annotation.ConfigObject;
import Framework.LazberryRegistryFramework.Annotation.ConfigValue;

// Automatically binds the 'database' section in config.yml to this immutable Record
@ConfigObject(path = "database")
public record DatabaseConfig(
    @ConfigValue(path = "host") String host,
    @ConfigValue(path = "port") int port,
    @ConfigValue(path = "username") String username,
    @ConfigValue(path = "password") String password,
    @ConfigValue(path = "max-pool-size") int maxPoolSize
) {}
```

```java
package com.lazberry.myplugin.service;

import Framework.Annotation.Component;
import Framework.Annotation.Inject;
import com.lazberry.myplugin.config.DatabaseConfig;

@Component
public class DatabaseService {

    private final DatabaseConfig dbConfig;

    @Inject
    public DatabaseService(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig; // Injected as a type-safe IoC bean automatically
    }

    public void connect() {
        String url = "jdbc:mysql://" + dbConfig.host() + ":" + dbConfig.port();
        System.out.println("Connecting to database: " + url);
    }
}
```
* **Role:**
  * Binds a YAML configuration path directly to a type-safe Java `record` or POJO without manual file-parsing logic.
  * Auto-registers the mapped object into the IoC container so it can be injected anywhere via `@Inject`.

* **Constraints:**
  * **Path Accuracy:** The `path` attribute in `@ConfigObject` must match the exact section key in your `config.yml`.
  * **Immutability:** Recommended to use with Java `record` for type-safety and immutability.
  * **Fallback / Defaults:** Keys missing in the YAML file must have default fallback values defined or nullable types.

### 6. Commands & Listeners Auto-Registration Example

```java
package com.lazberry.myplugin.listener;

import Framework.LazberryRegistryFramework.Annotation.Listeners;
import Framework.Annotation.Inject;
import com.lazberry.myplugin.service.UserService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

// Automatically registered to Bukkit's PluginManager upon startup
@Listeners
public class PlayerJoinListener implements Listener {

    private final UserService userService;

    @Inject
    public PlayerJoinListener(UserService userService) {
        this.userService = userService; // Inject dependencies seamlessly into listeners
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        userService.processUserData(event.getPlayer().getUniqueId().toString());
    }
}
```

```java
package com.lazberry.myplugin.command;

import Framework.LazberryRegistryFramework.Annotation.Commands;
import Framework.Annotation.Inject;
import com.lazberry.myplugin.service.UserService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// Automatically injected into Paper/Spigot command map
@Commands(command = "userinfo")
public class UserInfoCommand implements CommandExecutor {

    private final UserService userService;

    @Inject
    public UserInfoCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && args.length > 0) {
            userService.processUserData(args[0]);
            player.sendMessage("§aUser data processed successfully!");
            return true;
        }
        return false;
    }
}
```
### How it works & Constraints

* **Role:**
  * **`@Listeners`**: Eliminates the need to call `getServer().getPluginManager().registerEvents(...)` inside `onEnable()`. The framework scans and registers listener beans automatically.
  * **`@Commands`**: Auto-registers the target `CommandExecutor` directly to the server command map using the specified command name.

* **Constraints:**
  * **Interface Requirement:** `@Listeners` target classes must implement Bukkit's `Listener` interface.
  * **`plugin.yml` Registration:** Commands declared with `@Commands` must still be defined under the `commands:` section in `plugin.yml` (unless using a dynamic command map bridge).
  * **Singleton Life Cycle:** Both listeners and commands are treated as singleton components and support full dependency injection via `@Inject`.

### 7. Schedule & Async Execution Example

```java
package com.lazberry.myplugin.task;

import Framework.Annotation.Component;
import Framework.Annotation.Inject;
import Framework.Annotation.Async;
import Framework.LazberryRegistryFramework.Annotation.Schedule;
import com.lazberry.myplugin.service.UserService;

@Component
public class CacheCleanupTask {

    private final UserService userService;

    @Inject
    public CacheCleanupTask(UserService userService) {
        this.userService = userService;
    }

    // Runs asynchronously every 5 minutes (6000 ticks) after an initial 10-second delay (200 ticks)
    @Schedule(delay = 200, period = 6000, async = true)
    public void runPeriodicCachePurge() {
        System.out.println("[LRF] Starting automated background cache cleanup...");
        // Heavy operations here will not freeze the main server thread
    }

    // Simple asynchronous method execution invoked on demand
    @Async
    public void processHeavyDataLog(String logData) {
        // Runs immediately on a background thread pool
        System.out.println("[LRF Async Worker] Logging user activity: " + logData);
    }
}
```
### How it works & Constraints

* **Role:**
  * **`@Schedule`**: Automatically registers and triggers method execution loops using Bukkit's scheduler without manual `runTaskTimer()` calls.
  * **`@Async`**: Offloads method execution directly to a background thread pool to prevent main-thread lag spikes during compute-heavy or I/O operations.

* **Constraints:**
  * **Thread Safety**: Methods annotated with `@Async` or `@Schedule(async = true)` **must not** directly invoke Bukkit API methods that require main-thread execution (e.g., modifying entities or blocks directly).
  * **Component Bound**: Target methods must belong to a valid IoC-managed bean (e.g., annotated with `@Component`).
  * **Parameterless Schedules**: Scheduled methods (`@Schedule`) should generally have no parameter requirements so the framework worker can invoke them automatically.

### 8. Conditional Component Registration (`@Conditional`)

You can dynamically include or exclude components from the IoC container based on runtime environments, feature toggles, or server configurations.

##### Step 1: Implement the Condition Evaluator

```java
package com.lazberry.myplugin.condition;

import Framework.LazberryRegistryFramework.ConditionalRegistry;

// 1. Define your custom runtime condition evaluator
public final class DevEnvironmentCondition implements ConditionalRegistry {

    @Override
    public boolean matches() {
        // Returns true only when running on a development server environment
        return System.getProperty("server.env", "prod").equalsIgnoreCase("dev");
    }
}
```
#### Step 2: Apply `@Conditional` to the Component
```java
package com.lazberry.myplugin.service;

import Framework.Annotation.Component;
import Framework.Annotation.Conditional;
import com.lazberry.myplugin.condition.DevEnvironmentCondition;

// 2. The component is instantiated ONLY IF DevEnvironmentCondition#matches() returns true
@Component
@Conditional(DevEnvironmentCondition.class)
public class DebugLoggerService {

    public DebugLoggerService() {
        System.out.println("[LRF Debugger] Dev Environment detected. Initializing DebugLoggerService...");
    }

    public void logDebugInfo(String message) {
        System.out.println("[DEBUG] " + message);
    }
}
```
### How it works & Constraints

* **Role:**
  * **Zero-Footprint Filtering:** The LRF scanner evaluates `matches()` *before* instantiating the target component. If `false`, the component is completely dropped from the compilation queue—leaving zero memory overhead.
  * **Decoupled Architecture:** Eliminates hardcoded `if/else` checks inside initialization methods, keeping components clean and modular across different server environments (e.g., Lobby vs. Minigame servers).

* **Constraints:**
  * **Default Constructor Required:** The evaluator class implementing `ConditionalRegistry` **must** possess an accessible no-argument default constructor so the framework can instantiate it via reflection during the scan phase.
  * **Class Target Only:** `@Conditional` can only be applied to class-level declarations (`ElementType.TYPE`).
 
### 9. External & Third-Party Instance Registration

#### Step 1: Pre-Register External Handles (Bootstrapping)
```java
package com.lazberry.myplugin;

import Framework.LazberryRegistryFramework.DependencyContainer;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class MyPluginBootstrap {

    public static void initializeContainer(JavaPlugin plugin) {
        // Pre-registers native Bukkit context handles into the primary IoC registry
        DependencyContainer.registerInstance(JavaPlugin.class, plugin);
        DependencyContainer.registerInstance(File.class, plugin.getDataFolder());
        
        // Example: Registering a third-party library instance manually
        // DependencyContainer.registerInstance(JedisPool.class, new JedisPool("localhost", 6379));
    }
}
```

#### Step 2: Inject Registered Handles Anywhere

```java
package com.lazberry.myplugin.service;

import Framework.Annotation.Component;
import Framework.Annotation.Inject;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

@Component
public class StorageService {

    private final JavaPlugin plugin;
    private final File dataFolder;

    @Inject
    public StorageService(JavaPlugin plugin, File dataFolder) {
        // Automatically injected from the pre-registered external handles
        this.plugin = plugin;
        this.dataFolder = dataFolder;
    }

    public void printPluginInfo() {
        plugin.getLogger().info("Data folder path: " + dataFolder.getAbsolutePath());
    }
}
```

### How it works & Constraints

* **Role:**
  * **Early Context Insertion:** Allows non-managed external instances (such as Paper/Spigot handles, database pools, or external API clients) to be available across the entire IoC dependency graph via `@Inject`.
  * **Polymorphic Lookup:** `getBean(Class)` safely retrieves registered instances through type assignability checks without requiring manual explicit casting.

* **Constraints:**
  * **Bootstrapping Timing:** External instances must be registered using `registerInstance()` before invoking `PackageScanner.buildAndInjectBeans()` to guarantee dependency availability during constructor resolution.
  * **Key Overwrite:** Registering another instance with an identical class key will overwrite the previous entry inside the underlying container map.

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
│ LazberryRegistryFramework.onEnable() [LOCKED final]    │
├────────────────────────────────────────────────────────┤
│ 1. Context Allocation & Package Boundary Discovery     │
│ 2. ClassPath Scanning & Config Mapping (@ConfigObject) │
│ 3. IoC Graph Resolution & ByteBuddy Proxy Assembly     │
│ 4. Scheduler & Event Binding                           │
│ 5. Developer Hook Dispatch ────────────────────────────┼──► onLrfEnable()
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
