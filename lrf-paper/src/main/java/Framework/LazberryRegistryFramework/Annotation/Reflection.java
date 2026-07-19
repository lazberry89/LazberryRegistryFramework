package Framework.LazberryRegistryFramework.Annotation;

import Framework.InitializeType;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>Reflection</h2>
 * <p>
 * Binds a target class or specific method to a precise lifecycle initialization phase within the LRF container,
 * governing when its underlying reflective invocation sequence is executed.
 * </p>
 * <p>
 * Unlike standard component markers, this annotation is multi-targeted (supporting both {@link ElementType#TYPE}
 * and {@link ElementType#METHOD}), allowing the framework core to perform coarse-grained class-level environment
 * sorting or fine-grained individual routine execution mapping during boot operations.
 * </p>
 * <p>
 * <b>Engine Integration Dynamics:</b>
 * The centralized bootstrap architecture (specifically {@code Reflections#invokeReflections}) evaluates
 * this metadata against the current running {@link InitializeType} state matrix. If the contextual phase match
 * is satisfied, the framework intercepts the designated token and dynamically executes the structural class instantiation
 * or method invocation under a guarded runtime boundary.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see Framework.LazberryRegistryFramework.Reflections
 * @see Framework.InitializeType
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Reflection {

	/**
	 * Declares the precise initialization lifecycle phase that triggers the processing execution of the decorated element.
	 * <p>
	 * The framework's reflection dispatcher utilizes this value as a routing parameter. During a specific boot or cleanup
	 * step (e.g., configuring event handling maps or shutting down background routines), only the operations associated
	 * with the matching {@link InitializeType} parameter will be admitted into the active reflection execution queue.
	 * </p>
	 *
	 * @return The non-null {@link InitializeType} lifecycle state token mapping out execution timing conditions.
	 */
    @NotNull InitializeType type();
}
