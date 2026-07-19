package Framework.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * <h2>TitleUtil (Adventure API Graphical Title Packaging Subsystem)</h2>
 * <p>
 * Provides high-level convenience mechanics to programmatically build and handle
 * Kyori Adventure {@link Title} matrices targeting client-side screen rendering.
 * </p>
 * <p>
 * This utility bridges traditional Minecraft game ticks (where 20 ticks roughly equal 1 second)
 * to modern Java 8+ time paradigms ({@link Duration}), while guaranteeing seamless
 * text components color translations via LRF's internal graphics processor.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see net.kyori.adventure.title.Title
 * @see net.kyori.adventure.text.Component
 * @see ColorUtils
 */
public final class TitleUtil {

	/**
	 * Instantiation of this utility class is strictly prohibited as it maintains no internal state.
	 * Any attempt to instantiate this class via reflection will explicitly throw an {@link UnsupportedOperationException}
	 * to guarantee absolute structural integrity.
	 */
    @ApiStatus.Internal
    private TitleUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

	/**
	 * Constructs a fully qualified, immutably bound Adventure {@link Title} instance with
	 * custom interpolation timings translated dynamically from engine ticks.
	 * <p>
	 * <b>Operational Conversion Flow:</b>
	 * The input animation frame values represented in standard server ticks are explicitly multiplied
	 * by {@code 50L} (since 1 server tick evaluates exactly to 50 milliseconds) to generate the structural
	 * precise {@link Duration} bounds expected by the underlying Kyori rendering pipeline.
	 * </p>
	 *
	 * @param title    The primary main header text payload supporting legacy or MiniMessage color formatting codes (nullable).
	 * @param subtitle The secondary sub-header text payload supporting legacy or MiniMessage color formatting codes (nullable).
	 * @param fadeIn   The temporal duration of the fade-in animation phase, designated strictly in server ticks.
	 * @param stay     The temporal duration the title remains statically active on screen, designated strictly in server ticks.
	 * @param fadeOut  The temporal duration of the fade-out animation phase, designated strictly in server ticks.
	 * @return A guaranteed non-null, fully assembled {@link Title} matrix configured for immediate client dispatch.
	 */
    public static @NotNull Title create(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
        Component mainComponent = (title != null) ?
                ColorUtils.chat(title) : Component.empty();

        Component subComponent = (subtitle != null) ?
                ColorUtils.chat(subtitle) : Component.empty();

        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );

        return Title.title(mainComponent, subComponent, times);
    }

	/**
	 * Overloaded convenience pipeline that constructs an immutable {@link Title} instance
	 * fallback-initialized with standardized default network animation bounds.
	 * <p>
	 * <b>Standard Default Boundary Presets:</b>
	 * <ul>
	 * <li><b>Fade-In:</b> 10 Ticks (~0.5 Seconds)</li>
	 * <li><b>Stay:</b> 40 Ticks (~2.0 Seconds)</li>
	 * <li><b>Fade-Out:</b> 10 Ticks (~0.5 Seconds)</li>
	 * </ul>
	 * </p>
	 *
	 * @param title    The primary main header text payload supporting legacy or MiniMessage color formatting codes.
	 * @param subtitle The secondary sub-header text payload supporting legacy or MiniMessage color formatting codes.
	 * @return A guaranteed non-null, pre-timed {@link Title} matrix configured for immediate client dispatch.
	 */
    public static @NotNull Title create(@NotNull String title, @NotNull String subtitle) {
        return create(title, subtitle, 10, 40, 10);
    }
}
