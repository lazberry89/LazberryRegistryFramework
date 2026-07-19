package Framework.LazberryRegistryFramework.Monitoring;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Deprecated(forRemoval = true)
public final class PerformanceRegistry {
	private static final @NotNull ConcurrentHashMap<String, @NotNull MetricData> registry = new ConcurrentHashMap<>();
	
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
