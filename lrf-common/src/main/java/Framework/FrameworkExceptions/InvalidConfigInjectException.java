package Framework.FrameworkExceptions;

/**
 * <h2>InvalidConfigInjectException</h2>
 * <p>
 * Thrown by the configuration injection subsystem when a data resolution, type casting, or mapping
 * assignment fault occurs during property injection into an IoC managed component.
 * </p>
 * <h3>Architectural Context & Trigger Mechanics</h3>
 * <p>
 * Within the compilation pipeline of {@code Framework.LazberryRegistryFramework.DependencyContainer#getOrCreateBean(Class)},
 * if a constructor parameter is decorated with the {@code Framework.LazberryRegistryFramework.Annotation.ConfigValue} marker,
 * the execution flow routes the target key token directly to {@code ConfigInjection#resolve}.
 * </p>
 * <p>
 * If the requested absolute path does not exist inside the active configuration structures, or if the extracted raw value
 * cannot be successfully cast or assigned to the matching parameter type definition, this exception is explicitly
 * thrown to fail-fast, preventing incomplete configuration states from paralyzing business logic handlers downstream.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see java.lang.RuntimeException
 */
public class InvalidConfigInjectException extends RuntimeException {

	/**
	 * Constructs a new invalid configuration injection exception detailing the property metadata mismatch.
	 *
	 * @param message The non-null diagnostic error log tracing the missing configuration path node
	 * or structural type assignment discrepancy.
	 */
    public InvalidConfigInjectException(String message) {
        super(message);
    }
}
