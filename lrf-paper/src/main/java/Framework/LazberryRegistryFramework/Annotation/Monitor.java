package Framework.LazberryRegistryFramework.Annotation;

import Framework.Annotation.Async;
import Framework.Observation;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables real-time telemetry profiling and performance spike detection for the target method.
 * * <h3>Architectural Overview</h3>
 * <p>
 * Methods decorated with {@code @Monitor} are wrapped by LRF's performance telemetry interceptor.
 * High-precision execution duration is measured in nanoseconds ({@code System.nanoTime()}).
 * Metrics such as invocation counts, average latency, and peak spikes are automatically aggregated
 * into the central performance registry.
 * </p>
 * * <h3>Behavior &amp; Thresholds</h3>
 * <ul>
 * <li>
 * <b>Observation Level:</b> Defines the nanosecond threshold via {@link Observation}.
 * If an execution duration exceeds the configured threshold limit, a performance spike warning
 * is logged automatically.
 * </li>
 * <li>
 * <b>Asynchronous Compatibility:</b> When combined with {@link Async}, the profiling boundary
 * accurately measures net execution time within the worker thread rather than thread queue latency.
 * </li>
 * <li>
 * <b>No-Argument Constructor Requirement:</b> Classes containing {@code @Monitor} methods
 * must declare a valid no-argument constructor to support AOP dynamic proxy instantiation.
 * </li>
 * </ul>
 * * <h3>Usage Example</h3>
 * <pre>{@code
 * @Component
 * @NoArgsConstructor
 * public class HeavyComputationService {
 *
 *      @Monitor(lvl = Observation.HIGH)
 *      public void processWorldData() {
 *          // Precision monitored; logs warning if execution exceeds Observation.HIGH threshold
 *      }
 * }
 * }</pre>
 * * @author Lazberry (LRF Architecture Team)
 * @see Framework.Observation
 * @see Framework.LazberryRegistryFramework.Monitoring.PerformanceRegistry
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitor {

	/**
	 * Specifies the observation threshold level used to determine performance spikes.
	 * @return The configured {@link Observation} threshold level. Defaults to {@link Observation#LOW}.
	 */
	@NotNull Observation lvl() default Observation.LOW;
}
