package Framework.LazberryRegistryFramework;

import org.jetbrains.annotations.NotNull;

/**
 * <h2>PluginReceiver (Functional Network Inbound Reactor)</h2>
 * <p>
 * Represents a reactive functional boundary designed to intercept and process decrypted textual data streams
 * arriving from specialized network infrastructure pipelines.
 * </p>
 * <h3>IoC Operational Mechanics:</h3>
 * <p>
 * Beans annotated with {@link Framework.LazberryRegistryFramework.Annotation.InboundChannel} that implement
 * this interface are automatically intercepted during the LRF framework boot phase. The framework
 * extracts the specified channel signatures, initializes subscription trees, and hooks them directly
 * into the centralized routing subsystem.
 * </p>
 * @author Lazberry (LRF Architecture Team)
 * @see java.lang.FunctionalInterface
 * @see PluginMessageRouter
 */
@FunctionalInterface
public interface PluginReceiver {

	/**
	 * Executes localized logic handling sequences upon receiving an incoming network string payload.
	 * @param content The non-null incoming payload decrypted via UTF-8 boundaries.
	 */
    void receive(@NotNull String content);
}
