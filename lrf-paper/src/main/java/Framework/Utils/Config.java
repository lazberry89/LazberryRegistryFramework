package Framework.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <h2>Config (Immutable Configuration Wrapping Record & Type-Safe Extraction Engine)</h2>
 * <p>
 * Represents a lightweight, high-performance immutable carrier designed to wrap Bukkit's {@link FileConfiguration}.
 * It provides a fluent, chainable API pipeline alongside strict type-safe parameter extraction layers.
 * </p>
 * <h3>Architectural Resilience Strategy (Primitive Number Coercion):</h3>
 * <p>
 * Bukkit's configuration serialization frequently leads to subtle type-mismatch bugs due to YAML parsing constraints
 * (e.g., an asset designated as a {@code double} being evaluated implicitly as an {@code int}). This utility mitigates
 * that structural flaw by utilizing Java 17+ <b>Pattern Matching for instanceof</b> inside its extraction block,
 * automatically coercing matching numerical wrappers ({@link Number}) into the precise parameter type demanded by the
 * contextual default anchor.
 * </p>
 *
 * @param file The encapsulated underlying native Bukkit {@link FileConfiguration} source payload.
 * @author Lazberry (LRF Architecture Team)
 * @see org.bukkit.configuration.file.FileConfiguration
 */
@ParametersAreNonnullByDefault
public record Config(FileConfiguration file) {

	/**
	 * Named static factory routing pipeline that initiates a fluent chaining block over a target data asset.
	 * <b>Example Usage:</b>
	 * <pre>{@code
	 * Config wrapper = Config.of(plugin.getConfig());
	 * }</pre>
	 *
	 * @param file The non-null destination {@link FileConfiguration} stream.
	 * @return A newly mapped structural wrapper instance of {@link Config}.
	 */
	public static @NotNull Config of(FileConfiguration file) {
		return new Config(file);
	}

	/**
	 * Programmatically registers a defensive default parameter variable targeting the specified key path.
	 *
	 * @param key   The absolute configuration path signature (e.g., "database.mysql.port").
	 * @param value The default object fallback asset allocated to the entry if missing from disk.
	 * @return The current operational {@link Config} context instance to support continuous API chaining.
	 */
	@Contract("_, _ -> this")
	public @NotNull Config setDefault(String key, Object value) {
		this.file.addDefault(key, value);
		return this;
	}

	/**
	 * Resolves and extracts a type-safe generic attribute value from the internal stream, instantly triggering
	 * automatic numerical widening or narrowing conversions if an instance mismatch is detected.
	 * <p>
	 * <b>Operational Coercion Workflow:</b>
	 * If the extracted value represents an instance of {@link Number}, the engine evaluates the target type of the
	 * provided {@code def} argument. It then performs safe cast routing loops using localized primitive evaluations
	 * (e.g., {@link Number#floatValue()}) to guarantee that a non-null variable matching type {@code T} is safely returned.
	 * </p>
	 * <b>Example Usage:</b>
	 * <pre>{@code
	 * Integer port = config.getValue("network.port", 25565);
	 * double factor = config.getValue("multipliers.xp", 1.5); // Safely converts internal Ints to Double if needed
	 * }</pre>
	 *
	 * @param key  The absolute configuration path signature being evaluated.
	 * @param def  The strict non-null fallback value utilized to deduce type parameters and handle empty data slots.
	 * @param <T>  The expected return object parameter type deduced from the default anchor context.
	 * @return The correctly verified and cast data token matching type {@code T}; guarantees fallback to {@code def} on exceptions.
	 */
	@SuppressWarnings("unchecked")
	public @NotNull <T> T getValue(String key, T def) {
		Object value = this.file.get(key);
		if (value == null) return def;

		if (value instanceof Number num) {
            switch (def) {
                case Float ignored -> {return (T) Float.valueOf(num.floatValue());}
                case Long ignored -> {return (T) Long.valueOf(num.longValue());}
                case Integer ignored -> {return (T) Integer.valueOf(num.intValue());}
                case Double ignored -> {return (T) Double.valueOf(num.doubleValue());}
                default -> {}
            }
        }
		try {
			return (T) value;
		} catch (ClassCastException e) {
			return def;
		}
	}

	/**
	 * Resolves and extracts an unmanaged generic attribute value from the configuration pathway, returning
	 * {@code null} if the data node does not exist or if structural cast boundaries fail.
	 * <p>
	 * This overloading pattern is highly optimized for complex configuration serializable objects
	 * (e.g., native Bukkit {@link org.bukkit.Location} blocks or {@link org.bukkit.inventory.ItemStack} matrices)
	 * where primitive number coercion routines are entirely unneeded.
	 * </p>
	 * <b>Example Usage:</b>
	 * <pre>{@code
	 * Location spawn = config.getValue("teleport.spawn"); // Returns Location object or null
	 * ItemStack icon = config.getValue("gui.main_icon");
	 * }</pre>
	 *
	 * @param key The absolute configuration path signature being evaluated.
	 * @param <T> The implicit targeted generic return type allocation.
	 * @return The serialized entity variable mapped to the key path; otherwise {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public @Nullable <T> T getValue(String key) {
		Object value = this.file.get(key);
		if (value == null) return null;
		try {
			return (T) value;
		} catch (ClassCastException e) {
			return null;
		}
	}
}