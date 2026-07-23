package Framework.LazberryRegistryFramework.Monitoring;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe global repository storing execution telemetry and performance metrics
 * collected across all monitored LRF components.
 *
 * <h3>Architectural Overview</h3>
 * <p>
 * {@code PerformanceRegistry} acts as the central data aggregator for methods intercepted
 * via {@link Framework.LazberryRegistryFramework.Annotation.Monitor}. It maintains a concurrent
 * map of {@link MetricData} records mapped by unique signature keys formatted as {@code ClassName#MethodName()}.
 * </p>
 *
 * <h3>Thread Safety &amp; High Concurrency Design</h3>
 * <ul>
 * <li>
 * <b>Lock-Free Key Computation:</b> Uses {@link ConcurrentHashMap#computeIfAbsent}
 * to guarantee atomic creation of target metrics without global lock contention.
 * </li>
 * <li>
 * <b>Asynchronous Safe Submission:</b> Safe to invoke from both the main Bukkit thread
 * and background worker threads (e.g., methods decorated with {@code @Async}).
 * </li>
 * </ul>
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * // Submitting a metric manually (or via LrfProxyFactory)
 * PerformanceRegistry.submit("UserDataRepository", "saveData", 1_200_000L, 500_000L);
 *
 * // Printing diagnostic diagnostic performance report to console
 * PerformanceRegistry.broadcastMetricsDump();
 * }</pre>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see MetricData
 * @see Framework.LazberryRegistryFramework.Annotation.Monitor
 */
@Slf4j
public final class PerformanceRegistry {
	private static final @NotNull ConcurrentHashMap<String, @NotNull MetricData> registry = new ConcurrentHashMap<>();

	/**
	 * Submits execution latency metrics for a specific component method.
	 *
	 * @param className      The simple name of the target target class.
	 * @param methodName     The name of the executed method.
	 * @param elapsedNanos   The measured execution duration in nanoseconds.
	 * @param thresholdNanos The configured performance warning limit in nanoseconds.
	 */
	public static void submit(
			@NotNull String className,
			@NotNull String methodName,
			long elapsedNanos,
			long thresholdNanos
	) {
		String uniqueKey = className + "#" + methodName + "()";
        registry.computeIfAbsent(uniqueKey, key -> new MetricData(className, methodName))
                  .recordExecution(elapsedNanos, thresholdNanos);
	}

	/**
	 * Iterates through all registered component metrics and outputs a comprehensive
	 * performance diagnostic report to the logging pipeline.
	 */
	@Deprecated
	public static void broadcastMetricsDump() {
		if (registry.isEmpty()) return;

		log.info("\n===== LRF Performance Metrics Diagnostic Dump =====");
        registry.forEach((key, data) -> {
			log.info("📍 Target: {}", key);
	        log.info("   └─ Total Calls: {} times", data.getInvocationCount().get());
	        log.info("   └─ Avg Latency: {} ns", data.getAverageExecutionNanos());
	        log.info("   └─ Worst Spike: {} ns", data.getMaximumSpikeNanos());
	        log.info("   └─ Total Spikes: {} alerts", data.getSpikeCount().get());
		});
		log.info("=====================================================\n");
	}
}
