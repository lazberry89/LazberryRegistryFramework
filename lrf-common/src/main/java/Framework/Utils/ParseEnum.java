package Framework.Utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <h2>ParseEnum</h2>
 * <p>
 * A high-performance component utilizing Java's immutable record structure to safely and fluently
 * deserialize raw string payloads into designated {@link Enum} constant references.
 * </p>
 * <p>
 * This utility isolates reflection-based lookup constraints and encapsulates defensive exception-handling
 * protocols. It eliminates the overhead of explicit try-catch blocks across the application domain when
 * mapping external configuration metadata or network packets to strict systemic enumerations.
 * </p>
 *
 * @param clazz The token representing the target enum class specification.
 * @author Lazberry (LRF Architecture Team)
 * @see java.lang.Enum
 */
public record ParseEnum(@NotNull Class<? extends Enum<?>> clazz) {

	/**
	 * Initializes an immutable instance of the parsing utility scoped strictly to the provided enumeration context.
	 * <p>
	 * <b>Fluent API Blueprint:</b>
	 * <pre>{@code
	 * ParseEnum.of(TargetEnum.class).parse("IDENTIFIER");
	 * }</pre>
	 * </p>
	 *
	 * @param enumClass The target enumeration token undergoing parsing operations.
	 * @return A newly initialized non-null {@link ParseEnum} utility container instance.
	 */
	@Contract(value = "_ -> new", pure = true)
	public static @NotNull ParseEnum of(Class<? extends Enum<?>> enumClass) {
		return new ParseEnum(enumClass);
	}

	/**
	 * Evaluates and parses the provided string value into its structural counterpart within the defined execution scope.
	 * <p>
	 * <b>Normalization Pipeline:</b>
	 * To maximize compatibility with external configuration files, the incoming text string is automatically
	 * trimmed of whitespace boundaries and normalized to uppercase representations prior to systemic evaluation via
	 * {@link Enum#valueOf(Class, String)}. If lookup parameters fail or an input discrepancy is captured,
	 * the underlying exception state is handled defensively, returning a safe {@code null} reference.
	 * </p>
	 *
	 * @param value The raw text identifier representing the targeted enum constant.
	 * @param <E>   The generic boundary type expanding over the target enumeration.
	 * @return The matched structural {@link Enum} instance reference; or {@code null} if evaluation validation fails.
	 */
	@SuppressWarnings("unchecked")
	@Contract("null -> null")
	public <E extends Enum<E>> @Nullable E parse(@Nullable String value) {
		if (value == null || value.isEmpty()) return null;
		try {
			return Enum.valueOf((Class<E>) clazz, value.toUpperCase().trim());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Executes the parsing routine over the targeted text, gracefully substituting a provided fallback default reference
	 * if the operational resolution cycle fails.
	 *
	 * @param value The raw configuration string target to evaluate.
	 * @param def   The immutable default object parameter utilized to defuse resolution exceptions.
	 * @param <E>   The explicit type bound of the target enumeration component.
	 * @return The successfully matched enumeration constant, or the provided fallback object if mapping parameters collapse.
	 */
	public <E extends Enum<E>> @NotNull E parseOrDefault(@Nullable String value, @NotNull E def) {
		E result = parse(value);
		return result != null ? result : def;
	}
}
