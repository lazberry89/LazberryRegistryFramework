package Framework.LazberryRegistryFramework.Monitoring;

import Framework.Annotation.ConsumableClass;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Getter
@ConsumableClass
public final class MetricData {
	private final @NotNull String className;
	private final @NotNull String methodName;
	private final @NotNull AtomicLong invocationCount = new AtomicLong(0);
	private final @NotNull AtomicLong spikeCount = new AtomicLong(0);
	private long totalExecutionNanos = 0L;
	private long maximumSpikeNanos = 0L;

	public MetricData(@NotNull String className, @NotNull String methodName) {
		this.className = className;
		this.methodName = methodName;
	}

	public synchronized void recordExecution(long elapsedNanos, long thresholdNanos) {
        invocationCount.incrementAndGet();
        totalExecutionNanos += elapsedNanos;

		if (elapsedNanos > maximumSpikeNanos) {
            maximumSpikeNanos = elapsedNanos;
		}

		if (elapsedNanos > thresholdNanos) {
            spikeCount.incrementAndGet();
		}
	}

	public synchronized long getAverageExecutionNanos() {
		long count = invocationCount.get();
		return count == 0 ? 0L : totalExecutionNanos / count;
	}
}
