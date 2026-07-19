package Framework.Utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

/**
 * <h2>BookUtils (Adventure-Native Written Book Generation Subsystem)</h2>
 * <p>
 * Provides high-level convenience factories to dynamically manufacture standardized
 * Minecraft {@link Material#WRITTEN_BOOK} assets natively supporting modern rich-text formatting models.
 * </p>
 * <h3>Architectural Purpose & Encapsulation:</h3>
 * <p>
 * Native Bukkit book manipulation requires multi-tiered programmatic steps: instantiating raw item stacks,
 * fetching generic meta layouts, force-casting into specialized {@link BookMeta} interfaces, and flushing
 * object vectors back to the item stream. This utility completely abstracts this boilerplate sequence into
 * a single transactional contract while ensuring strict null-safe execution guards.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see org.bukkit.inventory.meta.BookMeta
 * @see net.kyori.adventure.text.Component
 * @see org.bukkit.inventory.ItemStack
 */
@UtilityClass
public final class BookUtils {

	/**
	 * Constructs a fully compiled, display-ready {@link ItemStack} representation of a written book
	 * populated with interactive Kyori text structures.
	 * <p>
	 * <b>Rich-Text Capabilities Note:</b>
	 * Because the framework accepts native {@link Component} elements for author signatures, book titles,
	 * and page content arrays, the generated assets are fully capable of rendering client-side UI interactive features
	 * (e.g., hover tooltips, clickable text run-commands, and specialized custom web-URL linking redirects).
	 * </p>
	 * <b>Example Usage:</b>
	 * <pre>{@code
	 * Component author = Component.text("Lazberry", NamedTextColor.RED);
	 * Component title = Component.text("System Manual", NamedTextColor.GOLD);
	 * Component page1 = Component.text("Welcome to LRF Core Guidelines...");
	 * * ItemStack frameworkBook = BookUtils.create(author, title, page1);
	 * }</pre>
	 *
	 * @param author The rich-text {@link Component} establishing the structural ownership metadata of the book.
	 * @param title  The rich-text {@link Component} establishing the display header signature of the book item.
	 * @param pages  A non-null, sequential varargs array of {@link Component} objects representing independent book page layouts.
	 * @return A guaranteed non-null, metadata-flushed {@link ItemStack} configured for immediate inventory assignment.
	 */
    public @NotNull ItemStack create(@NotNull Component author, @NotNull Component title, @NotNull Component... pages) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta != null) {
            meta.author(author);
            meta.title(title);
            meta.addPages(pages);
            book.setItemMeta(meta);
        }
        return book;
    }
}
