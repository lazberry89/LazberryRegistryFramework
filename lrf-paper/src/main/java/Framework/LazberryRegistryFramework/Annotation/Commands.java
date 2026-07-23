package Framework.LazberryRegistryFramework.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>Commands</h2>
 * <p>
 * Marks a target framework component as a managed command infrastructure executor, subjecting it
 * to automated command registry mapping and execution routing inside the platform environment.
 * </p>
 * <p>
 * This annotation is restricted to {@link ElementType#TYPE}, serving as a fundamental component identifier
 * during the classpath scanning phase. The core bootstrapping layer intercepts classes decorated with this metadata
 * and programmatically injects them into the underlying server's master command map (e.g., Bukkit's {@code SimpleCommandMap}),
 * eliminating the need for manual registration inside native plugin description files.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see Framework.LazberryRegistryFramework.Reflections
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Commands {

	/**
	 * Declares the primary target command label utilized to trigger the decorated execution handler component.
	 * <p>
	 * The framework's command dispatch subsystem maps this explicit string root token directly to the platform
	 * registry, routing user input matches to the component's internal command execution blocks.
	 * </p>
	 *
	 * @return The non-null structural command label string.
	 */
    String command();

	/**
	 * Defines an optional array of alternative command labels (aliases) capable of routing execution
	 * paths to the primary handler.
	 * <p>
	 * Defaults to an empty string array configuration matrix. When specified, each designated alias
	 * string is individually registered to the server environment as an alternate routing key pointing
	 * to the parent command identifier.
	 * </p>
	 *
	 * @return A non-null array of string configurations mapping out secondary command pathways.
	 */
    String[] aliases() default {};
}