package Framework;

import lombok.Getter;

@Getter
public enum Observation {
	HIGH(50_000L),
	AVERAGE(5_000_000L),
	LOW(50_000_000L),
	MONITOR(500_000_000L);

	private final long thresholdNanos;

	Observation(long time) {
		this.thresholdNanos = time;
	}
}
