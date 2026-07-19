package Framework.Utils;

import Framework.Annotation.Document;
import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <h2>ParseItem (Fail-Safe Material Text-Parsing Engine)</h2>
 * <p>
 * Provides robust utility mechanics to safely deserialize raw string data into Bukkit {@link Material}
 * instances, primarily utilized during external configuration asset loading sequences.
 * </p>
 * <h3>Architectural Purpose & Defensive Design:</h3>
 * <p>
 * In plugin ecosystems, reading asset or block types directly via native {@link Material#valueOf(String)}
 * introduces extreme instability due to human entry errors in configuration files (e.g., lowercase names,
 * obsolete item keys). This utility encapsulates data parsing dentro strict try-catch scopes to
 * suppress fatal runtime crashes, returning safe fallback values instead of throwing exceptions.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see ParseEnum
 * @see org.bukkit.Material
 */
@Deprecated(since = "26.2")
@Document(description = """
		This class is Deprecated due to ParseEnum.class.
		All of these functions are all moved, extended to
		ParseEnum utils.
		""")
public final class ParseItem {

	@ApiStatus.Internal
	private ParseItem() {
		throw new UnsupportedOperationException("Utility Class");
	}

	/**
	 * Evaluates and parses a nullable string token into a valid matching Bukkit {@link Material}.
	 * <p>
	 * <b>Operational Transformation Mechanics:</b>
	 * The incoming string is automatically canonicalized using {@link String#toUpperCase()} to comply with
	 * Bukkit's enum naming conventions. If the input token is null or does not match any registered
	 * game item key, the method intercepts the resulting {@link IllegalArgumentException} and gracefully returns {@code null}.
	 * </p>
	 * * <b>Example Usage:</b>
	 * <pre>{@code
	 * Material glass = ParseItem.parse("glass"); // Returns Material.GLASS
	 * Material unknown = ParseItem.parse("invalid_item_name"); // Returns null safely
	 * }</pre>
	 *
	 * @param value The target material's identification name payload (nullable).
	 * @return The corresponding non-null {@link Material} instance if successfully verified;
	 * otherwise {@code null}.
	 */
	@Contract("null -> null")
	public static @Nullable Material parse(@Nullable String value) {
		if (value == null) return null;
		try {
			return Material.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Evaluates and parses a nullable string token into a valid matching Bukkit {@link Material},
	 * automatically binding an explicit non-null fallback value upon resolution failure.
	 * <p>
	 * <b>Architectural Resilience Strategy (Fail-Safe):</b>
	 * This overloading pattern is highly recommended for loading critical plugin infrastructure assets
	 * (e.g., GUI menu background items). If an end-user corrupts the configuration file string, the engine
	 * guarantees structural continuity by instantly falling back to the designated {@code defaultItem}.
	 * </p>
	 * * <b>Example Usage:</b>
	 * <pre>{@code
	 * Material pane = ParseItem.parse("black_stained_glass_pane", Material.STONE); // Returns target pane
	 * Material broken = ParseItem.parse("corrupted_string_here", Material.STONE); // Returns Material.STONE safely
	 * }</pre>
	 *
	 * @param value       The target material's identification name payload (nullable).
	 * @param defaultItem The non-null fallback {@link Material} anchor returned if the parsing chain fails.
	 * @return The parsed target {@link Material} if valid; otherwise the provided {@code defaultItem}.
	 */
	@Contract("null, _ -> param2")
	public static @NotNull Material parse(@Nullable String value, @NotNull Material defaultItem) {
		if (value == null) return defaultItem;
		Material result;
		try {
			result = Material.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			result = defaultItem;
		}
		return result;
	}
}
