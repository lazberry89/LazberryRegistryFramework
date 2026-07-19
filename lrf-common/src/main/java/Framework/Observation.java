package Framework;

import lombok.Getter;
import org.jetbrains.annotations.TestOnly;

@TestOnly
@Deprecated
public enum Observation {
	HIGH(50_000L),
	AVERAGE(5_000_000L),
	LOW(50_000_000L),
	MONITOR(500_000_000L);

	private final @Getter long thresholdNanos;

	Observation(long time) {
		this.thresholdNanos = time;
	}
}
