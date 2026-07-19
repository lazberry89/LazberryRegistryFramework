package Framework.FrameworkExceptions;

/**
 * <h2>CircularDependencyException</h2>
 * <p>
 * Thrown by the dependency injection container when a cyclical cross-reference loop is detected within the
 * recursive component instantiation and topological graph resolution pipeline.
 * </p>
 * <h3>Architectural Context & Trigger Mechanics</h3>
 * <p>
 * During the execution trace of {@code Framework.LazberryRegistryFramework.DependencyContainer#getOrCreateBean(Class)},
 * the engine tracks the active injection path inside an order-retaining stack boundary ({@code CONSTRUCTION_STACK}).
 * </p>
 * <p>
 * If a sub-dependency or nested constructor parameter chain requests a class type token that is currently present
 * inside this execution trail, it indicates an unresolvable cyclic loop (e.g., Component A depends on Component B,
 * which in turn depends back on Component A). The framework intercepts this condition and explicitly throws this exception
 * to abort the transaction early, preventing a catastrophic {@link java.lang.StackOverflowError} from crashing the server thread.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see java.lang.RuntimeException
 */
public class CircularDependencyException extends RuntimeException {

	/**
	 * Constructs a new circular dependency exception detailing the blocked resolution path trace.
	 *
	 * @param message The non-null diagnostic error log detailing the self-referential graph loop
	 * and the offending class tokens captured in the active stack trail.
	 */
    public CircularDependencyException(String message) {
        super(message);
    }
}
