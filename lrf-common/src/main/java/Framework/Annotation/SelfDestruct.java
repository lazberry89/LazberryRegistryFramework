package Framework.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>SelfDestruct</h2>
 * <p>
 * Marks a target framework component as a temporary or phased resource, declaring a strict time-locked
 * lifespan after which the instance is automatically decommissioned and evicted from memory.
 * </p>
 * <p>
 * This annotation serves as the primary metadata trigger for {@code Framework.LazberryRegistryFramework.DestructiveClassEngine}.
 * It can be applied to both fully managed IoC singletons and short-lived unmanaged transient components
 * to enforce automated teardown pipelines without risking memory leaks.
 * </p>
 * <p>
 * <b>Engine Lifecycle Mechanics:</b>
 * Upon detection during registration (via {@code DestructiveClassEngine#registerDestruction} or
 * {@code DestructiveClassEngine#registerTransient}), the framework schedules a timed execution task utilizing
 * Bukkit's scheduler pipeline. Once the allocated time lapses, the engine executes polymorphically mapped
 * unlinking routines, unregistering active event listeners, stopping tasks, and invoking custom teardown hooks
 * defined by {@code Framework.LazberryRegistryFramework.Destructible}.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SelfDestruct {

	/**
	 * Defines the absolute lifespan duration of the decorated component, measured strictly in Minecraft server ticks.
	 * <p>
	 * The framework engine interprets this primitive value as a time delay baseline for the automated
	 * scheduler-driven destruction pipeline. For example, assigning a value of {@code 1200L} establishes a precise
	 * one-minute functional window (calculated at the standard platform execution baseline of 20 ticks per second)
	 * before the targeted instance triggers its safe explosion sequence.
	 * </p>
	 *
	 * @return The total number of server ticks allocated for the component's runtime operations.
	 */
    long value();
}
