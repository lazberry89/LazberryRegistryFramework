package Framework.FrameworkExceptions;

import org.jetbrains.annotations.NotNull;

/**
 * <h2>NotValidInitializeTimingException</h2>
 * <p>
 * Thrown by the framework's core lifecycle and dependency injection layers when a component's
 * post-instantiation routine is executed outside its permitted initialization phase boundary.
 * </p>
 * * <h3>Architectural Context & Trigger Mechanics</h3>
 * <p>
 * Within the {@code Framework.LazberryRegistryFramework.DependencyContainer#getOrCreateBean(Class)} assembly pipeline,
 * after a managed singleton object is successfully materialized via reflection, the container checks if the instance
 * implements the {@code Framework.LazberryRegistryFramework.LrfInitializer} interface contract.
 * </p>
 * <p>
 * If matched, the engine triggers {@code LrfInitializer#afterPropertiesSet()} to prime the component. If the target bean
 * attempts to access underlying systems (such as unmapped network channels via {@code OutboundChannel}, uninitialized
 * task loops via {@code Task}, or missing configurations) before the container enters the required
 * {@code Framework.InitializeType} state, this exception is explicitly thrown to fail-fast and preserve system integrity.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see java.lang.RuntimeException
 * @see Framework.InitializeType
 */
public class NotValidInitializeTimingException extends RuntimeException {

	/**
	 * Constructs a new initialization timing exception detailing the specific lifecycle synchronization fault.
	 * <p>
	 * When intercepted by the {@code DependencyContainer}, the exception payload is logged alongside the
	 * corresponding target class token to isolate the exact component violating phase boundaries.
	 * </p>
	 *
	 * @param message The non-null diagnostic error log tracing the precise phase mismatch or sequencing anomaly.
	 */
    public NotValidInitializeTimingException(@NotNull String message) {
        super(message);
    }
}
