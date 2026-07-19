package Framework.LazberryRegistryFramework.Monitoring;

import Framework.LazberryRegistryFramework.LazberryRegistryFramework;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

@Slf4j
@Deprecated(forRemoval = true)
public final class TelemetryInterceptor {
	private static final @NotNull String icon = LazberryRegistryFramework.icon(false);

	public static Object execute(@Nullable Object targetInstance, @NotNull Method method, @Nullable Object... args) throws Exception {
		if (!method.isAnnotationPresent(Monitor.class)) {
			return method.invoke(targetInstance, args);
		}

		Monitor monitor = method.getAnnotation(Monitor.class);
		long threshold = monitor.lvl().getThresholdNanos();

		long startTime = System.nanoTime();
		try {
			return method.invoke(targetInstance, args);
		} finally {
			long endTime = System.nanoTime();
			long elapsed = endTime - startTime;
			String className = targetInstance == null ? "[static-method]" : targetInstance.getClass().getSimpleName();

			PerformanceRegistry.submit(className, method.getName(), elapsed, threshold);

			if (elapsed > threshold) {
				triggerSpikeWarning(className, method.getName(), elapsed, threshold);
			}
		}
	}

	private static void triggerSpikeWarning(String clazz, String method, long elapsed, long limit) {
		log.error("{} Performance Spike Detected in {}#{}()", icon, clazz, method);
		log.error("              └─ Execution Time : {} ns", elapsed);
		log.error("              └─ Allowed Limit  : {} ns", limit);
	}
}
