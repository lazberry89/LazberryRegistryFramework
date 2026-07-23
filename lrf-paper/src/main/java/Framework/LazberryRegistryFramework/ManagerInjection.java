package Framework.LazberryRegistryFramework;

import Framework.ServerManager;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Core Orchestrator for ServerManager lifecycle initialization in LazberryRegistryFramework.
 * * <p><h3>Architecture & Operational Principle</h3>
 * This class acts as a dedicated control bridge between the raw IoC storage ({@link DependencyContainer})
 * and the Bukkit platform boot logic. In large-scale plugin environments, components extending
 * {@link ServerManager} contain critical domain business logic (e.g., Database pools, User caches,
 * Game states) that must be prepared strictly before event listeners or command maps start accepting traffic.
 * * <p>Rather than registering managers in an unpredictable classpath scanning sequence, this system
 * operates in a strictly separated two-phase deterministic pipeline:
 * <ol>
 * <li><b>Phase 1 (Collection):</b> Extracts fully dependency-resolved singleton beans from the IoC container
 * that are assignable to {@link ServerManager} and stores them into a tracked linear sequence.</li>
 * <li><b>Phase 2 (Sequential Boot):</b> Iterates through the gathered sequence and executes the mandatory
 * {@link ServerManager#init()} lifecycle hook inside a guarded try-catch block to guarantee whole server
 * boot resiliency.</li>
 * </ol>
 * * <p><h3>Related Framework Components</h3>
 * <ul>
 * <li>{@link PackageScanner}: The source data provider. Provides fully built bean references via {@link DependencyContainer#getContainer()}.</li>
 * <li>{@link Reflections}: The master lifecycle orchestrator that coordinates the timing of when this injection room is triggered.</li>
 * <li>{@link ServerManager}: The target component contract. Any class implementing this contract automatically participates in this phase.</li>
 * </ul>
 * @see ServerManager
 * @see PackageScanner
 * @see Reflections
 */
@Slf4j
final class ManagerInjection {
    private static final @NotNull List<ServerManager> ORDERED_MANAGERS = new ArrayList<>();
    private static final @NotNull String icon = LazberryRegistryFramework.icon();

    /**
     * Phase 1: Context Filtering & Collection Routine.
     * Clears any leftover historical reference in the internal sequence and queries the global
     * IoC bean map. Filters out types that do not inherit {@link ServerManager} and safely casts
     * the instantiated data into the localized tracking container.
     * * <p>This method maintains the natural insertion or resolution order guaranteed by the
     * {@link java.util.LinkedHashMap} layout inside {@link PackageScanner}.
     */
    static void collectManagers() {
        ORDERED_MANAGERS.clear();
        for (var entry : DependencyContainer.getContainer().entrySet()) {
            Class<?> clazz = entry.getKey();

            if (ServerManager.class.isAssignableFrom(clazz)) {
                ORDERED_MANAGERS.add((ServerManager) entry.getValue());
            }
        }
        log.info("{} Collected {} ServerManagers in safe initialization order.", icon, ORDERED_MANAGERS.size());
    }

    /**
     * Phase 2: Sequential Lifecycle Invocation Engine.
     * Iterates over the pre-collected ordered list of managers and triggers their {@code init()} logic.
     * <p><b>Resiliency Guardrail:</b> Each manager is wrapped in an individual isolated runtime
     * catch block. If a single database manager throws a connection exception, the framework catches the error
     * instantly, logs a critical state warning with the custom {@code icon}, and proceeds to initialize the
     * remaining managers in queue to maximize runtime survivability.
     */
    static void initializeManagers() {
        log.info("[IoC] Initializing ServerManagers sequentially...");
        for (ServerManager manager : ORDERED_MANAGERS) {
            try {
                manager.init();
                log.info("{} Initialized ServerManager: {}", icon, manager.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("{} Failed to initialize ServerManager: {}", icon, manager.getClass().getSimpleName(), e);
            }
        }
    }
}