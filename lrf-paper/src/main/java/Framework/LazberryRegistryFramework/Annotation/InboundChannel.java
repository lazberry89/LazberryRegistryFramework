package Framework.LazberryRegistryFramework.Annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>InboundChannel</h2>
 * <p>
 * Binds a framework component to a specific network message ingress pipeline, transforming the target
 * class into an authorized subscriber capable of intercepting and processing cross-server incoming packets.
 * </p>
 * <p>
 * Classes decorated with this annotation are evaluated by the framework's networking bootstrap layer,
 * which dynamically registers them to the underlying platform's packet listener registry (such as
 * Bukkit's {@code PluginMessageListener}) matching the declared channel token.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see OutboundChannel
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InboundChannel {

	/**
	 * Declares the unique single network channel identifier from which the decorated component
	 * expects to receive incoming serialized payloads.
	 * <p>
	 * The network engine maps this exact string token to register incoming traffic filters, routing
	 * matched packets directly to the component's internal buffer decoding handlers.
	 * </p>
	 *
	 * @return The non-null structural string representing the targeting ingress channel pathway.
	 */
    @NotNull String value();
}
