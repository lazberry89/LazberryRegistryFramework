package Framework.FrameworkExceptions;

/**
 * <h2>VirtualClassInjectException</h2>
 * <p>
 * Thrown by the dependency injection and component registry layer when an invalid attempt is made
 * to register an abstract type (such as an interface or an abstract class) directly into the IoC container.
 * </p>
 * <p>
 * This runtime exception enforces the framework's strict architectural invariant which requires all
 * managed beans within the ecosystem to be concrete, instantiable classes. It serves as a defensive compile-time
 * and boot-time barrier to prevent structural runtime failure caused by unresolvable instantiation contracts.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see java.lang.RuntimeException
 */
public class VirtualClassInjectException extends RuntimeException {
	/**
	 * Constructs a new virtual class injection exception detailing the structural specification violation.
	 *
	 * @param message The non-null diagnostic string describing the exact class token that triggered the violation.
	 */
    public VirtualClassInjectException(String message) {
        super(message);
    }
}
