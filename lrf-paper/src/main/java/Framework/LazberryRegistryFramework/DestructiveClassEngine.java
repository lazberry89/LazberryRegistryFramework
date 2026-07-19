package Framework.LazberryRegistryFramework;

import Framework.LazberryRegistryFramework.Annotation.SelfDestruct;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <h2>DestructiveClassEngine (Temporal Bean Lifecycle & Self-Destruction Engine)</h2>
 * <p>
 * A critical high-performance subsystem responsible for managing the automated, time-delayed decommissioning
 * and cleanup of dynamic, short-lived (transient) components or phased singletons within the LRF container.
 * </p>
 * <h3>The "Safe Explosion" Garbage-Collection Mechanics:</h3>
 * <p>
 * Managing dynamic events or short-duration instances (e.g., temporary mini-game state trackers, active instance sessions,
 * or dynamic event reward routines) in Bukkit usually results in memory leaks if listeners are left registered or tasks
 * keep ticking. This engine intercepts objects marked with {@link SelfDestruct}, tracking their exact server tick
 * lifespan via Bukkit's Scheduler pipeline. Upon expiration, it unhooks them completely from the native server core.
 * </p>
 * <h3>Double-Track Memory Isolation Strategy:</h3>
 * <ul>
 * <li><b>Singleton Eviction Track:</b> Monitors full dependency-managed beans inside the global container,
 * smoothly unregistering and removing the class token to prevent obsolete singleton creep.</li>
 * <li><b>Transient Segment Track:</b> Tracks raw, ad-hoc created object matrices inside a thread-safe
 * {@link Collections#synchronizedList(List)} pool, allowing temporary components to boot up, run tasks,
 * and vanish without bloating the dependency graphs.</li>
 * </ul>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see Destructible
 * @see DependencyContainer
 * @see Framework.LazberryRegistryFramework.Annotation.SelfDestruct
 */
@Slf4j
public final class DestructiveClassEngine {
    private static final @NotNull String icon = LazberryRegistryFramework.icon(false);
    private static final List<Object> TRANSIENT_COMPONENTS = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Examines a newly resolved singleton bean class, setting up a timed self-destruction task if the class
	 * is explicitly decorated with the {@link SelfDestruct} metadata.
	 * <p>
	 * <b>Operational Workflow:</b>
	 * After the requested delay tick parameter lapses, the engine verifies the presence of the class token.
	 * If valid, it triggers the comprehensive uncoupling phase before permanently dropping the type mapping
	 * from the primary {@link DependencyContainer}.
	 * </p>
	 *
	 * @param clazz    The target bean type token used as the tracking key inside the IoC cache.
	 * @param instance The non-null live object entry slated for upcoming eviction.
	 * @param plugin   The root {@link JavaPlugin} instance utilized to anchor the delayed Bukkit scheduler task.
	 */
    public static void registerDestruction(@NotNull Class<?> clazz, @NotNull Object instance, @NotNull JavaPlugin plugin) {
        if (!clazz.isAnnotationPresent(SelfDestruct.class)) return;

        long delayTicks = clazz.getAnnotation(SelfDestruct.class).value();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                if (!DependencyContainer.getContainer().containsKey(clazz)) return;

                executeDestructionSequence(instance);
                DependencyContainer.getContainer().remove(clazz);

                if (LazberryRegistryFramework.isDebug()) log.info("{} [SelfDestruct-Singleton] Evicted: {}", icon, clazz.getSimpleName());
            } catch (Exception e) {
                log.error("{} [SelfDestruct] Error during destruction of {}", icon, clazz.getSimpleName(), e);
            }
        }, delayTicks);
    }

	/**
	 * Registers and instantly activates an unmanaged, short-lived transient instance into the framework's time-locked tracking.
	 * <p>
	 * <b>Strict Component Guardrail:</b>
	 * Transient components passed to this method <b>must</b> possess the {@link SelfDestruct} annotation,
	 * otherwise a strict runtime exception is thrown. During registration, the engine automatically detects if
	 * the object is an {@code instanceof} {@link Listener} or {@link Tasks}, instantly binding them to the server
	 * pipeline before triggering the expiration timer.
	 * </p>
	 * <b>Example Dynamic Invocation:</b>
	 * <pre>{@code
	 * @SelfDestruct(1200L) // Safe explode after 1 minute (1200 ticks)
	 * public class ActiveGameSession implements Listener, Destructible {
	 *      @Override
	 *      public void onDestroy() {
	 *          log.info("Session wiped from RAM.");
	 *      }
	 * }
	 *
	 * // In business logic:
	 * DestructiveClassEngine.registerTransient(new ActiveGameSession(), plugin);
	 * }</pre>
	 *
	 * @param instance The target unmanaged instance undergoing time-locked tracking and deployment.
	 * @param plugin   The root {@link JavaPlugin} framework context provider.
	 * @throws IllegalArgumentException If the transient object's class lacks a valid {@link SelfDestruct} annotation.
	 */
    public static void registerTransient(@NotNull Object instance, @NotNull JavaPlugin plugin) {
        Class<?> clazz = instance.getClass();
        if (!clazz.isAnnotationPresent(SelfDestruct.class)) {
            throw new IllegalArgumentException("[LRF-Strict] Transient component must have @SelfDestruct!");
        }

        long delayTicks = clazz.getAnnotation(SelfDestruct.class).value();

        TRANSIENT_COMPONENTS.add(instance);

        if (instance instanceof Listener) Bukkit.getPluginManager().registerEvents((Listener) instance, plugin);
        if (instance instanceof Tasks) ((Tasks) instance).startTask(plugin);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!TRANSIENT_COMPONENTS.contains(instance)) return;

            try {
                executeDestructionSequence(instance);
                TRANSIENT_COMPONENTS.remove(instance);

                log.info("{} [SelfDestruct-Transient] Safe exploded: {}", icon, clazz.getSimpleName());
            } catch (Exception e) {
                log.error("{} [SelfDestruct] Error during transient destruction", icon, e);
            }
        }, delayTicks);
    }

	/**
	 * Forcefully aborts, closes, and de-allocates all active temporary components cached within the transient vector.
	 * <p>
	 * <b>Emergency Disconnect Guard:</b>
	 * This method is triggered during framework shutdowns or reloads. It acquires an exclusive lock on the
	 * {@code TRANSIENT_COMPONENTS} vector, ensuring all remaining instances gracefully invoke their respective
	 * termination handlers before wiping the registry to prevent memory leaks during hot-swaps.
	 * </p>
	 */
    public static void abortAllTransient() {
        log.info("{} [SelfDestruct] Shutdown detected, removing all temporary instances..", icon);
        synchronized (TRANSIENT_COMPONENTS) {
            for (Object instance : TRANSIENT_COMPONENTS) {
                try {
                    executeDestructionSequence(instance);
                } catch (Exception e) {
                    log.error("{} Failed to abort component: {}", icon, instance.getClass().getSimpleName(), e);
                }
            }
            TRANSIENT_COMPONENTS.clear();
        }
    }

	/**
	 * Poly-morphic internal unlinking engine that analyzes an object's structural interfaces and gracefully
	 * untangles it from active server pipeline registers.
	 * <p>
	 * <b>Multi-Contract Uncoupling Pipeline:</b>
	 * <ul>
	 * <li><b>Destructible:</b> Forwards execution directly to the custom {@link Destructible#onDestroy()} block.</li>
	 * <li><b>Tasks:</b> Invokes {@link Tasks#stopTask()} to safely cancel ticking schedulers.</li>
	 * <li><b>Listener:</b> Invokes {@link HandlerList#unregisterAll(Listener)} to pull the listener out of Bukkit's event baking array.</li>
	 * </ul>
	 * </p>
	 *
	 * @param instance The object undergoing termination processing layers.
	 */
    private static void executeDestructionSequence(Object instance) {
        if (instance instanceof Destructible) ((Destructible) instance).onDestroy();
        if (instance instanceof Tasks) ((Tasks) instance).stopTask();
        if (instance instanceof Listener) HandlerList.unregisterAll((Listener) instance);
    }
}

