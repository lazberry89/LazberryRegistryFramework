package Framework.Utils;

import Framework.LazberryRegistryFramework.LazberryRegistryFramework;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * <h2>GlowUtils (Client-Side Outlining & Dynamic Glow Rendering Subsystem)</h2>
 * <p>
 * Provides high-level convenience mechanics to manipulate vanilla entity glowing effects and change
 * outlining colors dynamically utilizing the server-wide native {@link Scoreboard} team parameters.
 * </p>
 * <p>
 * Includes an advanced non-native workaround to simulate glowing effects on standard static {@link Block}
 * architectures by wrapping them within transparent, non-collidable entities.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see org.bukkit.scoreboard.Scoreboard
 * @see org.bukkit.scoreboard.Team
 * @see net.kyori.adventure.text.format.NamedTextColor
 */
public final class GlowUtils {

	/**
	 * Instantiation of this utility class is strictly prohibited as it maintains no internal state.
	 * Any attempt to instantiate this class via reflection will explicitly throw an {@link UnsupportedOperationException}
	 * to guarantee absolute structural integrity.
	 */
	@ApiStatus.Internal
	private GlowUtils() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Activates a glowing outline around an {@link Entity} and overrides its default white shader color
	 * by assigning it to a color-mapped scoreboard team asset.
	 * <p>
	 * <b>Operational Scoreboard Mapping:</b>
	 * Minecraft changes glow outline colors exclusively based on the entity's active {@link Team} prefix color parameters.
	 * This method lazy-registers unique color teams formatted as {@code glow_[COLOR_NAME]} directly into the
	 * server's main scoreboard matrix to prevent unnecessary team instantiation duplicates.
	 * </p>
	 * <b>Example Usage:</b>
	 * <pre>{@code
	 * GlowUtils.glow(targetEntity, NamedTextColor.YELLOW); // Outlines target in yellow light
	 * }</pre>
	 *
	 * @param entity The non-null destination {@link Entity} targeted for the glowing overlay shader.
	 * @param color  The target Kyori {@link NamedTextColor} establishing the outlining spectrum.
	 */
	public static void glow(@NotNull Entity entity, @NotNull NamedTextColor color) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		String teamName = "glow_" + color;
		Team team = scoreboard.getTeam(teamName);

		if (team == null) {
			team = scoreboard.registerNewTeam(teamName);
			team.color(color);
		}

		team.addEntity(entity);
		entity.setGlowing(true);
	}

	/**
	 * Terminates an {@link Entity}'s glowing outline status and detaches it from its current scoreboard team
	 * to ensure aggressive memory leak mitigation.
	 * <p>
	 * <b>Memory Management Guardrail:</b>
	 * Simply setting {@code setGlowing(false)} leaves the entity's UUID entry registered inside the scoreboard
	 * team data pools. Over time, this leads to extensive RAM leaks and massive scoreboard bloating.
	 * This pipeline proactively searches for the entry's parent team and wipes the UUID string from memory.
	 * </p>
	 * <b>Example Usage:</b>
	 * <pre>{@code
	 * GlowUtils.clearGlow(targetEntity); // Completely purges glow state and metadata entries
	 * }</pre>
	 *
	 * @param entity The non-null target {@link Entity} undergoing aesthetic normalization.
	 */
    public static void clearGlow(@NotNull Entity entity) {
        entity.setGlowing(false);

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getEntryTeam(entity.getUniqueId().toString());

        if (team != null) {
            team.removeEntry(entity.getUniqueId().toString());
        }
    }

	/**
	 * Simulates a custom glowing outline effect over a static structural {@link Block} by deploying an invisible
	 * entity proxy layer mapped with an automated self-destruct scheduler task boundary.
	 * <p>
	 * <b>Architectural Proxy Hack Mechanics:</b>
	 * 1. Extracts the exact localized vector parameters of the block target.<br>
	 * 2. Spawns an immutable native {@link Shulker} entity whose hitboxes align perfectly with a 1x1x1 solid block coordinate.<br>
	 * 3. Configures the shulker proxy to be entirely passive: disables AI, silences audio packets, locks generic
	 * collision states, sets transparency parameters, and resets raw peeking bounds to 0.<br>
	 * 4. Applies the custom team shader overlay via {@link #glow(Entity, NamedTextColor)}.<br>
	 * 5. Synchronously registers an asynchronous Bukkit scheduler callback pipeline destined to permanently purge
	 * the proxy entity from world memory layers once the designated {@code duration} frame limits expire.
	 * </p>
	 * <b>Example Usage:</b>
	 * <pre>{@code
	 * GlowUtils.glowBlock(diamondBlock, NamedTextColor.RED, 20); // Highlights block in red for exactly 1 second (20 ticks)
	 * }</pre>
	 *
	 * @param block    The non-null targeted world terrain {@link Block} instance.
	 * @param color    The dynamic color matrix applied to the block outline.
	 * @param duration The temporal lifespan threshold of the highlighting effect represented in server ticks.
	 */
	public static void glowBlock(@NotNull Block block, @NotNull NamedTextColor color, int duration) {
		Location loc = block.getLocation();
		loc.getWorld().spawn(loc, Shulker.class, s -> {
			s.setAI(false);
			s.setSilent(true);
			s.setInvulnerable(true);
			s.setInvisible(true);
			s.setCollidable(false);
			s.setPeek(0);
			GlowUtils.glow(s, color);
			Bukkit.getScheduler().runTaskLater(LazberryRegistryFramework.plugin(), s::remove, duration);
		});
	}
}
