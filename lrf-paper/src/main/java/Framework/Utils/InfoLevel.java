package Framework.Utils;

import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

/**
 * <h2>InfoLevel (Messaging Severity and Audiovisual Mapping Matrix)</h2>
 * <p>
 * Defines the strict operational tiers for in-game notifications, mapping structural severity levels
 * directly to unique chat visual prefixes ({@link Alert}) and native Minecraft client sound cues ({@link Sound}).
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see Alert
 * @see org.bukkit.Sound
 * @see InfoUtils
 */
public enum InfoLevel {
    ERROR(Alert.RED, Sound.BLOCK_NOTE_BLOCK_BASS),
    WARN(Alert.YELLOW, Sound.BLOCK_NOTE_BLOCK_BASS),
    INFO(Alert.GREEN, Sound.ENTITY_ARROW_HIT_PLAYER);

    private final @NotNull Alert prefix;
    private final @NotNull Sound sound;

    InfoLevel(@NotNull Alert prefix, @NotNull Sound sound) {
        this.prefix = prefix;
        this.sound = sound;
    }
	/** @return The designated non-null metadata alert prefix component handler. */
    public @NotNull Alert prefix() {
        return this.prefix;
    }
	/** @return The designated non-null client audio cue definition enum. */
    public @NotNull Sound sound() {
        return this.sound;
    }
}
