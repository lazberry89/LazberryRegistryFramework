package Framework.LazberryRegistryFramework;

import Framework.LazberryRegistryFramework.Annotation.GracefulShutdown;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Stores and manages shutdown handlers decorated with {@link GracefulShutdown}.
 */
@Slf4j
public final class ShutdownRegistry {
	private static final List<ShutdownTask> SHUTDOWN_TASKS = new ArrayList<>();
	private static final String icon = LazberryRegistryFramework.icon();

	private record ShutdownTask(Object target, Method method, int priority) {}

	public static void registerShutdownHandlers(Object instance, Class<?> clazz) {
		for (Method method : clazz.getDeclaredMethods()) {
			GracefulShutdown annotation = method.getAnnotation(GracefulShutdown.class);
			if (annotation == null) continue;

			method.setAccessible(true);
			SHUTDOWN_TASKS.add(new ShutdownTask(instance, method, annotation.priority()));
		}
	}

	public static void executeShutdownSequence() {
		if (SHUTDOWN_TASKS.isEmpty()) return;

		log.info("{} Starting Graceful Shutdown sequence...", icon);

		SHUTDOWN_TASKS.stream()
				.sorted(Comparator.comparingInt(ShutdownTask::priority))
				.forEach(task -> {
					try {
						log.info("{} Executing shutdown task: {}#{}() [Priority: {}]",
								icon, task.target().getClass().getSimpleName(), task.method().getName(), task.priority());
						task.method().invoke(task.target());
					} catch (Exception e) {
						log.error("{} Error during shutdown execution in {}#{}()",
								icon, task.target().getClass().getSimpleName(), task.method().getName(), e);
					}
				});

		SHUTDOWN_TASKS.clear();
		log.info("{} Graceful Shutdown sequence completed.", icon);
	}
}
