package Framework;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h2>ServerType</h2>
 * <p>
 * Defines the architectural environment and topological identity of the running server instance,
 * acting as a foundational contract for environmental partitioning across the framework.
 * </p>
 * <p>
 * Implementations of this interface (such as {@code Global} or {@code Local}) are utilized by conditional
 * scanning and reflection evaluation subsystems to determine whether specific components, managers, or tasks
 * are permitted to initialize within the current runtime infrastructure.
 * </p>
 * <p>
 * <b>Registry Management Mechanics:</b>
 * The centralized bootstrap subsystem programmatically initializes and binds core environmental types into
 * the static thread-safe {@link ConcurrentHashMap} container during the framework's primary boot phase,
 * ensuring universal availability for dynamic configuration lookup operations.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 */
public interface ServerType {

	/** * The high-throughput concurrent data registry maintaining active environment mappings,
	 * indexing standardized string keys to their respective structural {@link ServerType} instances.
	 */
	Map<String, ServerType> REGISTRY = new ConcurrentHashMap<>();

	/**
	 * Evaluates whether the current environment topology demands a global network infrastructure initiator core.
	 *
	 * @return {@code true} if a centralized network initiator is required; otherwise {@code false}.
	 */
	boolean requiresGlobalInitiator();

	/**
	 * Programmatically mutates the initiator requirement state parameters for this environmental configuration context.
	 *
	 * @param required {@code true} to enforce global initiator core activation; {@code false} to bypass.
	 */
	void setGlobalInitiatorRequired(boolean required);

	/**
	 * Retrieves the localized semantic name token identifying the target environment type configuration.
	 * <p>
	 * The value returned by this routine serves as the definitive registry lookup key and must be formatted
	 * consistently to support accurate string token matching pipelines.
	 * </p>
	 *
	 * @return A non-null string containing the unique identifier name of the server context.
	 */
	@NotNull String getName();

	/**
	 * Registers a concrete {@link ServerType} instance into the centralized environment mapping registry matrix.
	 * <p>
	 * To prevent casing discrepancies across distinct external configuration files, the target identifier name
	 * is normalized using lower-case conversions prior to storage allocation.
	 * </p>
	 *
	 * @param serverType The non-null environment context component undergoing integration tracking.
	 */
	static void register(@NotNull ServerType serverType) {
		REGISTRY.put(serverType.getName().toLowerCase(), serverType);
	}

	/**
	 * Converts an incoming raw configuration string descriptor into its corresponding registered {@link ServerType} reference.
	 * <p>
	 * <b>Defensive Fallback Protocol:</b>
	 * If the requested environment name does not match any compiled components inside the tracking registry,
	 * the system gracefully defaults to returning the global environment infrastructure context.
	 * </p>
	 *
	 * @param name The non-null raw text name extracted from configuration files to resolve.
	 * @return A non-null resolved {@link ServerType} matching the requested environment matrix parameters.
	 */
	@Contract("_ -> !null")
	static @NotNull ServerType getServerType(@NotNull String name) {
		ServerType matched = REGISTRY.get(name.toLowerCase());

		if (matched == null) return REGISTRY.getOrDefault("global", new Global());
		return matched;
	}

	/**
	 * Wipes and purges all recorded context elements from the centralized registry matrix.
	 * <p>
	 * This method is triggered during framework cleanup or reload sequences to isolate memory allocations
	 * and prepare the infrastructure layers for clean instantiation cycles.
	 * </p>
	 */
	static void unregisterAll() {
		REGISTRY.clear();
	}
}
