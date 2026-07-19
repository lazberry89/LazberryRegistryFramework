package Framework.FrameworkExceptions;

/**
 * <h2>NotCompatibleWithServerException</h2>
 * <p>
 * Thrown by the dependency injection container during the component verification phase when a target class
 * is detected to be structurally incompatible with the currently active server infrastructure or NMS version.
 * </p>
 * <h3>Architectural Context & Trigger Mechanics</h3>
 * <p>
 * Within the execution pipeline of {@code Framework.LazberryRegistryFramework.DependencyContainer#getOrCreateBean(Class)},
 * before allocating resources or parsing constructors, the engine invokes {@code Framework.Utils.ServerUtils#unCompatibleWithCurrentServer(Class)}.
 * If the target component dictates an environment boundary (e.g., restricted to a specific {@code ServerType}
 * or proprietary server platform features) that does not align with the active platform runtime metadata,
 * this exception is explicitly thrown to prevent unpredictable instantiation failures downstream.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see java.lang.RuntimeException
 * @see Framework.ServerType
 */
public class NotCompatibleWithServerException extends RuntimeException {

	/**
	 * Constructs a new server compatibility exception detailing the environmental infrastructure violation.
	 *
	 * @param message The non-null diagnostic error log tracing the mismatch between the component's required bounds
	 * and the active server profile.
	 */
    public NotCompatibleWithServerException(String message) {
        super(message);
    }
}
