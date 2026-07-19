package Framework.LazberryRegistryFramework.Annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>OutboundChannel</h2>
 * <p>
 * Declares the designated proxy or network messaging pipelines through which the decorated component
 * is permitted to transmit outbound data packets inside a multi-server proxy ecosystem.
 * </p>
 * <p>
 * This annotation allows the framework's network communication layer to automatically register and
 * open specific communication egress tracks (e.g., standard BungeeCord sub-channels) upon component
 * instantiation, minimizing manual plugin channel orchestration boilerplate.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see InboundChannel
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OutboundChannel {

	/**
	 * Defines an array of target outbound channel identifiers utilized for data packet dispatching operations.
	 * <p>
	 * Defaults to {@code {"bungeecord::main"}}, representing the primary network infrastructure proxy pipeline
	 * unless explicitly overridden with custom network channel routing keys.
	 * </p>
	 *
	 * @return A non-null array of string identifiers mapping out destination network channels.
	 */
    @NotNull String[] value() default {"bungeecord::main"};
}
