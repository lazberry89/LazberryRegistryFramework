package Framework.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.NotNull;

public enum Alert {
	XmasLegacy("&c&l[&a&lX&c&lm&a&la&c&ls&a&lL&c&le&a&lg&c&la&a&lc&c&ly&a&l]&r&f", ColorUtils.chat("&c&l[&a&lX&c&lm&a&la&c&ls&a&lL&c&le&a&lg&c&la&a&lc&c&ly&a&l]&r&f")),
	RED("&c&l[!]&r&f", ColorUtils.chat("&c&l[!]&r&f").hoverEvent(HoverEvent.showText(ColorUtils.chat("&c&l[!]&r&f - 에러, 문제를 표시할 때 사용되는 표기입니다.")))),
	YELLOW("&e&l[!]&r&f", ColorUtils.chat("&e&l[!]&r&f").hoverEvent(HoverEvent.showText(ColorUtils.chat("&e&l[!]&r&f - 경고, 중요 알림을 표시할 때 사용되는 표기입니다.")))),
	GREEN("&a&l[!]&r&f", ColorUtils.chat("&a&l[!]&r&f").hoverEvent(HoverEvent.showText(ColorUtils.chat("&a&l[!]&r&f - 성공적인 알림을 표시할 때 사용되는 표기입니다..")))),;

	Alert(@NotNull String prefix, @NotNull Component aliases) {
		this.prefix = prefix;
		this.aliases = aliases;
	}

	private final @NotNull String prefix;
	private final @NotNull Component aliases;

	@Override
	public @NotNull String toString() {
		return this.prefix;
	}

	public @NotNull Component comp() {
		return this.aliases;
	}
}
