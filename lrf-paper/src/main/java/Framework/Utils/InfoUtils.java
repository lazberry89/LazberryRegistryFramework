package Framework.Utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Utility;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.helpers.MessageFormatter;

/**
 * <h1>InfoUtils</h1>
 * <p>
 * A high-level framework utility class designed to handle formatted messaging
 * (Information, Warnings, and Errors) directed to Minecraft players.
 * </p>
 * <p>
 * This utility seamlessly integrates <b>SLF4J asynchronous logging</b>,
 * <b>Kyori Adventure Text Components</b>, and automated audiovisual feedback
 * (custom sounds and chat prefixes) tailored to the severity level of the message.
 * </p>
 *
 * @author lazberry
 * @version 1.0.0
 * @see InfoLevel
 * @see ColorUtils
 */
@Slf4j
@UtilityClass
public final class InfoUtils {

	/**
	 * Sends an informational message to a player using SLF4J-style string formatting.
	 * The formatted message is also mirrored to the server console as an INFO log.
	 *
	 * <p><b>Example:</b></p>
	 * {@code InfoUtils.info(player, "Welcome back, {}! Level: {}", player.getName(), 50);}
	 *
	 * @param p       the target {@link Player} to receive the message; must not be null
	 * @param message the message string containing optional SLF4J placeholders ({@code "{}"})
	 * @param args    the dynamic arguments used to replace the placeholders in the message
	 */
	public @Utility void info(@NotNull Player p, @NotNull String message, Object... args) {
		sendMessage(InfoLevel.INFO, p, message, args);
	}

	/**
	 * Sends a warning message to a player using SLF4J-style string formatting.
	 * The formatted message is also mirrored to the server console as a WARN log.
	 *
	 * <p><b>Example:</b></p>
	 * {@code InfoUtils.warn(player, "Your inventory is almost full ({}%)", 85);}
	 *
	 * @param p       the target {@link Player} to receive the warning; must not be null
	 * @param message the warning string containing optional SLF4J placeholders ({@code "{}"})
	 * @param args    the dynamic arguments used to replace the placeholders
	 */
	public @Utility void warn(@NotNull Player p, @NotNull String message, Object... args) {
		sendMessage(InfoLevel.WARN, p, message, args);
	}

	/**
	 * Sends a critical error message to a player using SLF4J-style string formatting.
	 * The formatted message is also mirrored to the server console as an ERROR log.
	 *
	 * @param p       the target {@link Player} to receive the error notification; must not be null
	 * @param message the error description string containing optional SLF4J placeholders ({@code "{}"})
	 * @param args    the dynamic arguments used to replace the placeholders
	 */
	public @Utility void error(@NotNull Player p, @NotNull String message, Object... args) {
		sendMessage(InfoLevel.ERROR, p, message, args);
	}

	/**
	 * Sends a critical error message to a player and logs the associated exception stack trace to the console.
	 *
	 * <p><b>Example:</b></p>
	 * <pre>{@code
	 * try {
	 * loadData();
	 * } catch (Exception e) {
	 * InfoUtils.error(player, "Failed to load database row ID: {}", e, rowId);
	 * }
	 * }</pre>
	 *
	 * @param p       the target {@link Player} to receive the error notification; must not be null
	 * @param message the error description string containing optional SLF4J placeholders
	 * @param e       the {@link Throwable} exception to be tracked and logged; must not be null
	 * @param args    the dynamic arguments used to replace the placeholders
	 */
	public @Utility void error(@NotNull Player p, @NotNull String message, @NotNull Throwable e, Object... args) {
		sendMessage(InfoLevel.ERROR, p, message, args);
		traceException(e);
	}

	/**
	 * Sends a rich text Kyori {@link Component} to a player as an informational message.
	 * Automatically prepends the INFO prefix and triggers the corresponding sound effect.
	 *
	 * @param p         the target {@link Player} to receive the component; must not be null
	 * @param component the rich text {@link Component} to render in the player's chat
	 */
	public @Utility void info(@NotNull Player p, @NotNull Component component) {
		sendMessage(InfoLevel.INFO, p, component);
	}

	/**
	 * Sends a rich text Kyori {@link Component} to a player as a warning message.
	 * Automatically prepends the WARN prefix and triggers the corresponding warning sound effect.
	 *
	 * @param p         the target {@link Player} to receive the component; must not be null
	 * @param component the rich text {@link Component} to render in the player's chat
	 */
	public @Utility void warn(@NotNull Player p, @NotNull Component component) {
		sendMessage(InfoLevel.WARN, p, component);
	}

	/**
	 * Sends a rich text Kyori {@link Component} to a player as a critical error message.
	 * Automatically prepends the ERROR prefix and triggers the corresponding failure sound effect.
	 *
	 * @param p         the target {@link Player} to receive the component; must not be null
	 * @param component the rich text {@link Component} to render in the player's chat
	 */
	public @Utility void error(@NotNull Player p, @NotNull Component component) {
		sendMessage(InfoLevel.ERROR, p, component);
	}

	/**
	 * Sends a rich text Kyori {@link Component} error message to a player and prints the exception trace to the console.
	 *
	 * @param p         the target {@link Player} to receive the component; must not be null
	 * @param component the rich text {@link Component} to render in the player's chat
	 * @param e         the {@link Throwable} exception to log; must not be null
	 */
	public @Utility void error(@NotNull Player p, @NotNull Component component, @NotNull Throwable e) {
		sendMessage(InfoLevel.ERROR, p, component);
		traceException(e);
	}

	/**
	 * Extracts and outputs a streamlined summary of the given exception to the server console.
	 * Logs the root cause and the full error message using the SLF4J logger framework.
	 *
	 * @param e the {@link Throwable} exception instance to extract data from; must not be null
	 */
	public void traceException(@NotNull Throwable e) {
		log.error("Cause : {}\n\n Full Message : {}", e.getCause(), e.getMessage());
	}

	/**
	 * Internal helper method to process and transmit raw Kyori Components directly to the client.
	 * Appends the contextual prefix, plays the corresponding sound, and sends the action.
	 */
	private void sendMessage(@NotNull InfoLevel level, @NotNull Player p, @NotNull Component message) {
		Component txt = level.prefix().comp().appendSpace().append(message);
		p.sendMessage(txt);
		p.playSound(p, level.sound(), 1.0f, 1.0f);
	}

	/**
	 * Internal helper method to parse SLF4J placeholders, mirrors the result to the console
	 * via SLF4J logs, formats Legacy/MiniMessage color codes via {@link ColorUtils},
	 * and dispatches the final chat to the player alongside the level-specific audio cue.
	 */
	private void sendMessage(@NotNull InfoLevel level, @NotNull Player p, @NotNull String message, Object... args) {
		String formattedMessage = MessageFormatter.arrayFormat(message, args).getMessage();
		switch (level) {
			case INFO -> log.info(formattedMessage);
			case WARN -> log.warn(formattedMessage);
			case ERROR -> log.error(formattedMessage);
		}
		Component txt = level.prefix().comp().appendSpace().append(ColorUtils.chat(formattedMessage));
		p.sendMessage(txt);
		p.playSound(p, level.sound(), 1.0f, 1.0f);
	}
}