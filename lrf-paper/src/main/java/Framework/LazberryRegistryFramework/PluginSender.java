package Framework.LazberryRegistryFramework;

import org.jetbrains.annotations.NotNull;

/**
 * <h2>PluginSender (Functional Network Outbound Gateway)</h2>
 * <p>
 * Represents a high-level functional abstraction for dispatching raw outbound structural text data
 * across targeted infrastructure pipeline pathways (e.g., Minecraft Plugin Channels).
 * </p>
 * <h3>Architectural Purpose:</h3>
 * <p>
 * This interface decouples the core domain logic from the low-level serialization and platform-dependent
 * transmission byte buffers. By exposing a unified string-based functional interface, components can
 * broadcast messages across proxy environments (such as Velocity or BungeeCord) without holding
 * hard references to native server packet layers.
 * </p>
 * @author Lazberry (LRF Architecture Team)
 * @see java.lang.FunctionalInterface
 * @see PluginMessageRouter
 */
@FunctionalInterface
public interface PluginSender {

	/**
	 * Serializes and dispatches the specified textual payloads down the configured upstream pipe.
	 *
	 * @param content The non-null standardized raw text payload or JSON string to be transmitted.
	 */
    void send(@NotNull String content);
}
