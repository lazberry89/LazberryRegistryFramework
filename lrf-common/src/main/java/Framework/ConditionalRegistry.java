package Framework;

/**
 * <h2>LRF Functional Condition Evaluator Interface</h2>
 *
 * <h3>1. Motivation & System Architecture</h3>
 * In modular server environments, certain systems or components should only be activated under specific run-time scenarios
 * (e.g., specific mini-game subtypes, test modes, feature toggles, or database configurations). Hardcoding these checks
 * within the component ruins its reusability and breaks clean architecture.
 * <p>
 * {@code ConditionalRegistry} provides a functional abstraction contract for <b>conditional component registration</b>.
 * By linking this interface with the framework's {@code @Conditional} annotation, developers can dynamically determine
 * whether a target component is eligible to be instantiated and managed by the IoC container.
 *
 * <h3>2. Operational Principle</h3>
 * The LRF scanner scans metadata of target candidate classes before instantiation. If a candidate is marked with a
 * conditional strategy class that implements this contract, the engine dynamically instantiates this evaluator via reflection
 * and executes {@link #matches()}.
 * <ul>
 * <li><b>Returns {@code true}:</b> The component passes filtering criteria and proceeds directly into the IoC assembly loop.</li>
 * <li><b>Returns {@code false}:</b> The component is discarded early, ensuring no memory allocation or dependency footprint.</li>
 * </ul>
 *
 * <h3>3. Concrete Usage Example</h3>
 * <pre>{@code
 * // 1. Define the custom environment matching strategy
 * public final class DevEnvironmentCondition implements ConditionalRegistry {
 *      @Override
 *      public boolean matches() {
 *          // Resolves true only if the server is explicitly flagged as a development instance
 *          return System.getProperty("server.env", "prod").equalsIgnoreCase("dev");
 *      }
 * }
 *
 * // 2. Apply the matching strategy to a core system
 * @Registry.Include
 * @Conditional(DevEnvironmentCondition.class)
 * public class DebugLoggerService {
 *
 *      public DebugLoggerService() {
 *          // Instantiated only if matches() returns true
 *      }
 * }
 * }</pre>
 *
 * @author Lazberry (LRF Architecture Team)
 * @version 1.1.0
 * @see FunctionalInterface
 */
@FunctionalInterface
public interface ConditionalRegistry {
    /**
     * Evaluates the custom structural or environmental conditions to determine registration eligibility.
     *
     * @return {@code true} if the current structural context meets the criteria to safely boot the component;
     * {@code false} to safely drop and exclude the class from the global IoC bean ecosystem.
     */
    boolean matches();
}
