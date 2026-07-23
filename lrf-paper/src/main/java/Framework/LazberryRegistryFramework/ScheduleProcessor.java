package Framework.LazberryRegistryFramework;

import Framework.LazberryRegistryFramework.Annotation.Schedule;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Scans managed beans for {@link Schedule} annotations and manages active Bukkit tasks.
 */
@Slf4j
public final class ScheduleProcessor {
	private static final @NotNull List<BukkitTask> REGISTERED_TASKS = new ArrayList<>();
	private static final @NotNull String icon = LazberryRegistryFramework.icon();
	private static final @NotNull JavaPlugin plugin = LazberryRegistryFramework.plugin();

	public static void processSchedules(Object instance, Class<?> clazz) {
		for (Method method : clazz.getDeclaredMethods()) {
			Schedule schedule = method.getAnnotation(Schedule.class);
			if (schedule == null) continue;

			if (method.getParameterCount() > 0) {
				log.error("{} @Schedule method {}#{}() must not accept parameters!", icon, clazz.getSimpleName(), method.getName());
				continue;
			}

			method.setAccessible(true);
			Runnable taskRunnable = () -> {
				try {
					method.invoke(instance);
				} catch (Exception e) {
					log.error("{} Exception in @Schedule task {}#{}()", icon, clazz.getSimpleName(), method.getName(), e);
				}
			};

			BukkitTask task;
			long delay = schedule.delay();
			long period = schedule.period();
			boolean isAsync = schedule.async();

			if (period > 0) {
				task = isAsync
						? Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, taskRunnable, delay, period)
						: Bukkit.getScheduler().runTaskTimer(plugin, taskRunnable, delay, period);
			} else {
				task = isAsync
						? Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, taskRunnable, delay)
						: Bukkit.getScheduler().runTaskLater(plugin, taskRunnable, delay);
			}

			REGISTERED_TASKS.add(task);
			if (LazberryRegistryFramework.isDebug()) {
				log.info("{} Scheduled task registered: {}#{}() [Async: {}, Period: {}t]",
						icon, clazz.getSimpleName(), method.getName(), isAsync, period);
			}
		}
	}

	public static void cancelAllSchedules() {
		if (REGISTERED_TASKS.isEmpty()) return;
		REGISTERED_TASKS.forEach(BukkitTask::cancel);
		REGISTERED_TASKS.clear();
		log.info("{} All scheduled tasks cancelled successfully.", icon);
	}
}
