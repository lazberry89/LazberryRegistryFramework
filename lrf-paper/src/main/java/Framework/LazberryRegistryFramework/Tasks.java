package Framework.LazberryRegistryFramework;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * <h2>Tasks (IoC-Managed Framework Lifecycle Task Specification)</h2>
 * <p>
 * Enforces a strict functional contract for managing repeating background routines, async runnables,
 * or localized ticking engines that integrate into the Lazberry Component Framework (LRF).
 * </p>
 * * <h3>Framework Integration & Orchestration Mechanics:</h3>
 * <p>
 * This interface works in tandem with the {@link Framework.LazberryRegistryFramework.Annotation.Task} annotation.
 * During the framework boot and shutdown phases, the core reflection module
 * (specifically {@link Framework.LazberryRegistryFramework.Reflections#startTasks(com.google.common.reflect.ClassPath)}
 * and {@link Framework.LazberryRegistryFramework.Reflections#stopTasks(com.google.common.reflect.ClassPath)})
 * automatically scans the active IoC container, filters classes implementing this contract, and manages
 * their runtime lifecycle states safely.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see Framework.LazberryRegistryFramework.Reflections
 * @see org.bukkit.scheduler.BukkitScheduler
 */
public interface Tasks {

	/**
	 * Initializes, configures, and schedules the underlying repeated tasks or asynchronous worker loops.
	 * <p>
	 * <b>Engine Invocation Flow:</b>
	 * This boundary is explicitly fired by {@code Reflections.taskReflection(true)} during the
	 * {@link Framework.InitializeType#TASKS_ON} phase. The core engine passes the live root plugin context
	 * down the stream, allowing implementors to safely invoke Bukkit's scheduler factories
	 * (e.g., {@code Bukkit.getScheduler().runTaskTimer(plugin, ...)}).
	 * </p>
	 *
	 * @param plugin The active non-null root {@link JavaPlugin} instance running the LRF infrastructure.
	 */
	void startTask(@NotNull JavaPlugin plugin);

	/**
	 * Gracefully terminates all active thread iterations, cancelling scheduled tasks permanently.
	 * <p>
	 * <b>Crucial Memory Guardrail (Anti-Leak Requirements):</b>
	 * This boundary is explicitly fired by {@code Reflections.taskReflection(false)} during the
	 * {@link Framework.InitializeType#TASKS_OFF} phase (triggered on plugin disable). Implementors
	 * <b>must</b> cancel their active task IDs or unregister their asynchronous tasks here to prevent
	 * un-garbage-collected tasks from lingering in server memory after a reload.
	 * </p>
	 */
	void stopTask();
}
