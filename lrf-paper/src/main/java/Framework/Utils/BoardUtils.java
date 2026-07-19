package Framework.Utils;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * <h2>BoardUtils (Anti-Blink Dynamic Sidebar Scoreboard Subsystem)</h2>
 * <p>
 * Manages rapid, real-time sidebar scoreboard displays uniquely tailored per-player.
 * It encapsulates the entire Bukkit {@link Scoreboard} routing layer to support high-frequency updates.
 * </p>
 * <h3>Architectural Resilience & Anti-Blink Mechanics:</h3>
 * <p>
 * Native Bukkit scoreboard rendering refreshes lines by constantly destroying and re-registering score strings,
 * which triggers severe visual flickering (blinking) on the client side. This subsystem eliminates that issue
 * entirely by caching 15 immutable, localized invisible color code entries ({@code §0§r}, {@code §1§r}, etc.)
 * inside specific {@link Team} structures. Instead of modifying the score name, it updates the team's prefix component,
 * pushing clean packets to the client seamlessly.
 * </p>
 * <h3>Thread-Safe Session Lifecycle Caching:</h3>
 * <p>
 * This manager utilizes a {@link ConcurrentHashMap} database layer bound to the player's {@link UUID}.
 * This ensures multithreaded synchronization when fetching session profiles, preventing object instantiation
 * bloating and reducing main server thread bottlenecks.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see org.bukkit.scoreboard.Scoreboard
 * @see org.bukkit.scoreboard.Team
 * @see org.bukkit.scoreboard.Objective
 */
public final class BoardUtils {
	/** Global thread-safe session storage mapped to isolate active user scoreboard contexts during runtime. */
	private static final @NotNull Map<UUID, BoardUtils> CACHE = new ConcurrentHashMap<>();

	private final @NotNull @Getter Scoreboard scoreboard;
	private final @NotNull Objective objective;
	private final @NotNull @Getter Player player;
	private final @NotNull Map<Integer, Team> lines = new HashMap<>(20);

	/**
	 * Resolves and retrieves an active cached scoreboard profile or initiates a new initialization sequence
	 * if the destination context does not exist.
	 * <p>
	 * Supports a fluent functional setup consumer block to allow immediate transactional rendering.
	 * </p>
	 * <b>Example Usage:</b>
	 * <pre>{@code
	 * BoardUtils.getOrCreate(player, Component.text("§6[Stats]"), board -> {
	 * board.setLine(0, Component.text("Coins: " + playerCoins));
	 * board.setLine(1, Component.text("Ping: " + playerPing));
	 * });
	 * }</pre>
	 *
	 * @param player Target {@link Player} receiving the sidebar rendering packets.
	 * @param title  The rich text {@link Component} template deployed as the header title of the sidebar objective.
	 * @param setup  The functional configuration callback wrapper providing access to immediate mutable executions.
	 * @return A guaranteed non-null, fully synchronized {@link BoardUtils} profile instance.
	 */
	@Contract("_, _, _ -> !null")
	@CanIgnoreReturnValue
	public static @NotNull BoardUtils getOrCreate(@NotNull Player player, @NotNull Component title, @NotNull Consumer<BoardUtils> setup) {
		UUID uuid = player.getUniqueId();

		if (CACHE.containsKey(uuid)) {
			BoardUtils board = CACHE.get(uuid);
			board.updateTitle(title);
			board.edit(setup);
			return board;
		}

		BoardUtils board = new BoardUtils(player, title);
		setup.accept(board);
		CACHE.put(uuid, board);
		return board;
	}

	/**
	 * Terminate the target user's active sidebar scoreboard registration, flushing references from cache structures
	 * and reverting the client connection back to the main server scoreboard instantly.
	 * <p>
	 * <b>Crucial Memory-Leak Mitigation Guardrail:</b>
	 * This method <b>MUST</b> be called explicitly inside server network disconnect listener structures (e.g.,
	 * PlayerQuitEvent) to ensure dead player references to do not linger inside heap spaces indefinitely.
	 * </p>
	 *
	 * @param player The disconnecting or targeted client {@link Player} profile undergoing database erasure.
	 */
	public static void removeBoard(@NotNull Player player) {
		CACHE.remove(player.getUniqueId());
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}

	/**
	 * Exposes a transactional block allowing safe programmatic mutation adjustments over lines or titles using lambda chains.
	 *
	 * @param action Lambda execution container capturing this current mutable utility reference.
	 */
	public void edit(@NotNull Consumer<BoardUtils> action) {
		action.accept(this);
	}

	/**
	 * Internal framework constructor routing engine initializing the underlying scoreboard canvas
	 * and pre-baking team entry parameters.
	 * <p>
	 * <b>Structural Pre-Baking Strategy:</b>
	 * Automatically registers 15 default teams ({@code line_0} through {@code line_14}) and appends unique
	 * invisible markers to map out static rendering slots down the sidebar matrix.
	 * </p>
	 *
	 * @param player Target user receiving the generated canvas data.
	 * @param title  The header text component applied to the tracking objective.
	 */
	@ApiStatus.Internal
	private BoardUtils(@NotNull Player player, @NotNull Component title) {
		this.player = player;
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		this.objective = scoreboard.registerNewObjective("party_board", Criteria.DUMMY, title);
		this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		for (int i = 0; i < 15; i++) {
			Team team = scoreboard.registerNewTeam("line_" + i);
			String invisibleEntry = getInvisibleEntry(i);
			team.addEntry(invisibleEntry);
			this.lines.put(i, team);
		}

		player.setScoreboard(this.scoreboard);
	}

	/**
	 * Alters the current objective's core header title text on the client layout without causing visual synchronization delays.
	 *
	 * @param title New destination rich text {@link Component} to render.
	 */
	public void updateTitle(@NotNull Component title) {
		this.objective.displayName(title);
	}

	/**
	 * Injects or overwrites a specific line index row on the scoreboard display without triggering visual data blinking.
	 * <p>
	 * <b>Row Index Ordering Paradigm:</b>
	 * Line ranges must strictly fall inside boundary brackets {@code [0, 14]}. The score placement score value is
	 * calculated dynamically via formula: $score = 15 - line$, organizing the array top-to-bottom on the sidebar menu.
	 * </p>
	 *
	 * @param line The indexed integer slot designated to map the element string.
	 * @param text The rich text {@link Component} payload defining the display row text.
	 */
	public void setLine(int line, @NotNull Component text) {
		if (line < 0 || line > 14) return;

		Team team = lines.get(line);
		if (team != null) {
			team.prefix(text);

			String invisibleEntry = getInvisibleEntry(line);
			this.objective.getScore(invisibleEntry).setScore(15 - line);
		}
	}

	/**
	 * Purges and resets an active score tracking registration row, making it completely invisible to the recipient client.
	 *
	 * @param line The targeted index integer row destined for erasure.
	 */
	public void removeLine(int line) {
		if (line < 0 || line > 14) return;
		String invisibleEntry = getInvisibleEntry(line);
		this.scoreboard.resetScores(invisibleEntry);
	}

	/**
	 * Dynamic format compiler that generates an un-renderable color sequence string acting as a static anchor.
	 * <p>
	 * Combines section codes with localized hex representations: {@code §[HEX_LINE_INDEX]§r}.
	 * </p>
	 */
	private @NotNull String getInvisibleEntry(int line) {
		return "§" + Integer.toHexString(line) + "§r";
	}
}