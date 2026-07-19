package Framework.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <h2>ColorUtils</h2>
 * <p>
 * A high-performance text processing utility designed to manage color code translation, serialization,
 * and deserialization operations leveraging the Kyori Adventure text framework.
 * </p>
 * <p>
 * This class abstracts the conversion mechanics between raw string data containing formatting codes
 * (including standard legacy formatting symbols and RGB hex color strings) and native immutable {@link Component}
 * instances, ensuring rich-text compliance across server platforms.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see net.kyori.adventure.text.Component
 * @see net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
 */
public class ColorUtils {

	private static final @NotNull LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.builder()
			.character('&')
			.hexColors()
			.build();

	private static final @NotNull LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.builder()
			.character(LegacyComponentSerializer.SECTION_CHAR)
			.hexColors()
			.build();

	/**
	 * Deserializes an incoming raw string message into a color-formatted rich {@link Component} instance.
	 * <p>
	 * <b>Operational Engine:</b>
	 * This method evaluates the text using a cached ampersand-based configuration capable of translating
	 * both legacy color tokens (e.g., {@code &a}, {@code &b}) and advanced Hexadecimal formatting sequences
	 * into atomic component trees. If the parameter matches a {@code null} bound, a standard empty component
	 * token is returned to prevent down-stream NPE anomalies.
	 * </p>
	 *
	 * @param message The raw string value containing ampersand configuration nodes to parse.
	 * @return A non-null structured Kyori {@link Component} representing the rich-text result.
	 */
	public static @NotNull Component chat(@Nullable String message) {
		if (message == null) return Component.empty();

		return AMPERSAND_SERIALIZER.deserialize(message);
	}

	/**
	 * Transmutes an incoming ampersand-formatted text payload into a raw platform-legacy section-formatted string.
	 * <p>
	 * <b>Architecture Constraint Notice:</b>
	 * This method has been marked as {@link Deprecated}. Modern platforms prioritize the application of rich
	 * {@link Component} entities directly. Converting back to raw section strings ({@code \u00A7}) strips
	 * semantic text metadata and violates cross-platform design specifications.
	 * </p>
	 *
	 * @param message The raw textual message string undergoing structural conversion.
	 * @return A non-null colorized legacy string sequence integrated with structural section keys.
	 * @deprecated Scheduled for absolute deprecation since specification version 26.1 in favor of the fluent
	 * component model via {@link #chat(String)}.
	 */
	@Deprecated(since = "26.1")
	public static @NotNull String chatStr(@Nullable String message) {
		if (message == null) return "";

		return SECTION_SERIALIZER.serialize(AMPERSAND_SERIALIZER.deserialize(message));
	}

	/**
	 * Serializes a structured rich-text {@link Component} node block back into an ampersand-formatted legacy string literal.
	 * <p>
	 * This routine reverses the ingestion pipeline, scanning the component structure to append matching format flags
	 * and hex color tokens, producing clean raw string representations suitable for external storage or internal database mapping.
	 * </p>
	 *
	 * @param component The target non-null immutable rich-text component entity to serialize.
	 * @return The resulting non-null ampersand-delimited layout string.
	 */
	public static @NotNull String toLegacy(@NotNull Component component) {
		return AMPERSAND_SERIALIZER.serialize(component);
	}
}

