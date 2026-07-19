package Framework.Utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Fluent API Builder utility for creating and modifying {@link ItemStack} with ease.
 * Supports latest Minecraft features including Item Models and Persistent Data Containers.
 */
public final class ItemBuilder {
	private final @NotNull ItemStack item;
	private final @Nullable ItemMeta meta;
	private final @NotNull JavaPlugin plugin;

	/**
	 * Initiates ItemBuilder pattern.
	 * @param plugin XmasLegacy instance
	 * @param material What kind of Material to edit
	 */
	@ApiStatus.Internal
	private ItemBuilder(@NonNull JavaPlugin plugin, @NotNull Material material) {
		this.item = new ItemStack(material);
		this.meta = item.getItemMeta();
		this.plugin = plugin;
	}

	/**
	 * Overloaded method to flexibly use Utility with existing ItemStack.
	 * @param plugin XmasLegacy instance
	 * @param itemStack ItemStack to clone and edit
	 */
	@ApiStatus.Internal
	private ItemBuilder(@NonNull JavaPlugin plugin, @NotNull ItemStack itemStack) {
		this.item = itemStack.clone();
		this.meta = item.getItemMeta();
		this.plugin = plugin;
	}

	/**
	 * Replacing/Set display name of item.
	 * @param name display component name to change
	 * @return builder instance
	 */
	@Contract("_ -> this")
	public ItemBuilder setName(@NotNull Component name) {
		if (meta != null) {
			meta.displayName(name);
		}
		return this;
	}

	/**
	 * Replacing/Set lore of item.
	 * @param lore display component of lore to change
	 * @return builder instance
	 */
	@Contract("_ -> this")
	public ItemBuilder setLore(@NotNull Component... lore) {
		if (meta != null) {
			List<Component> loreList = new ArrayList<>(Arrays.asList(lore));
			meta.lore(loreList);
		}
		return this;
	}

	/**
	 * Make item show/hide glint override (Enchantment effect without real enchantments).
	 * @param glint glint flag (true to force show, false to force hide)
	 * @return builder instance
	 */
	@Contract("_ -> this")
	public ItemBuilder setGlint(boolean glint) {
		if (meta != null) {
			meta.setEnchantmentGlintOverride(glint);
		}
		return this;
	}

	/**
	 * Only for head item. Changing head owner skin.
	 * @param player target player for change
	 * @return builder instance
	 */
	@Contract("_ -> this")
	public ItemBuilder setHeadOwner(@NotNull OfflinePlayer player) {
		if (item.getType() == Material.PLAYER_HEAD && meta != null) {
			SkullMeta sm = (SkullMeta) meta;
			sm.setOwningPlayer(player);
			item.setItemMeta(sm);
		}
		return this;
	}

	/**
	 * Hides all vanilla item flags (e.g., tooltips, attributes, enchants).
	 * @return builder instance
	 */
	@Contract("-> this")
	public ItemBuilder hideAllFlags() {
		if (meta != null) {
			for (ItemFlag flag : ItemFlag.values()) {
				meta.addItemFlags(flag);
			}
		}
		return this;
	}

	/**
	 * Adds an attribute modifier targeting {@link EquipmentSlotGroup#MAINHAND}.
	 * @param attribute target attribute to modify
	 * @param amount double value of modification
	 * @param operation operation type
	 * @return builder instance
	 */
	@Contract("_, _, _ -> this")
	public ItemBuilder addAttribute(@NotNull Attribute attribute,
	                                double amount,
	                                @NotNull AttributeModifier.Operation operation) {
		if (meta != null) {
			NamespacedKey key = new NamespacedKey(this.plugin, attribute.getKey().getKey());
			AttributeModifier modifier = new AttributeModifier(key, amount, operation, EquipmentSlotGroup.MAINHAND);

			meta.addAttributeModifier(attribute, modifier);
		}
		return this;
	}

	/**
	 * Adds an attribute modifier targeting specific {@link EquipmentSlotGroup}.
	 * @param attribute target attribute to modify
	 * @param amount double value of modification
	 * @param operation operation type
	 * @param slot target slot group restriction
	 * @return builder instance
	 */
	@Contract("_, _, _, _ -> this")
	public ItemBuilder addAttribute(@NotNull Attribute attribute,
	                                double amount,
	                                @NotNull AttributeModifier.Operation operation,
	                                @NotNull EquipmentSlotGroup slot) {
		if (meta != null) {
			NamespacedKey key = new NamespacedKey(this.plugin, attribute.getKey().getKey());
			AttributeModifier modifier = new AttributeModifier(key, amount, operation, slot);

			meta.addAttributeModifier(attribute, modifier);
		}
		return this;
	}

	/**
	 * Convenience method to set generic attack damage on Main Hand.
	 * @param damage attack damage amount
	 * @return builder instance
	 */
	@Contract("_ -> this")
	public ItemBuilder setAttackDamage(double damage) {
		return addAttribute(Attribute.ATTACK_DAMAGE, damage, AttributeModifier.Operation.ADD_NUMBER);
	}

	/**
	 * Convenience method to set generic armor values on specific equipment slot.
	 * @param state armor state amount
	 * @param slot target slot group
	 * @return builder instance
	 */
	@Contract("_, _ -> this")
	public ItemBuilder setArmorState(double state, @NotNull EquipmentSlotGroup slot) {
		return addAttribute(Attribute.ARMOR, state, AttributeModifier.Operation.ADD_NUMBER, slot);
	}

	/**
	 * Sets the item to be unbreakable.
	 * @return builder instance
	 */
	@Contract("-> this")
	public ItemBuilder setUnbreakable() {
		if (meta != null) {
			meta.setUnbreakable(true);
		}
		return this;
	}

	/**
	 * Overrides maximum stack size of target item.
	 * @param size maximum stack count
	 * @return builder instance
	 */
	@Contract("_ -> this")
	public ItemBuilder setMaxStackSize(int size) {
		if (meta != null) {
			meta.setMaxStackSize(size);
		}
		return this;
	}

	/**
	 * Adds a specific enchantment to target item. Ignores max level restrictions.
	 * @param enchantment type of enchantment
	 * @param level level of enchantment
	 * @return builder instance
	 */
	@Contract("_, _ -> this")
	public ItemBuilder addEnchant(@NotNull Enchantment enchantment, int level) {
		if (meta != null) {
			meta.addEnchant(enchantment, level, true);
		}
		return this;
	}

	/**
	 * Adjusts the potion color override for potion-based items.
	 * @param color target potion color
	 * @return builder instance
	 */
	@Contract("_ -> this")
	public ItemBuilder customPotionColor(@NotNull Color color) {
		if (meta instanceof PotionMeta potionMeta) {
			potionMeta.setColor(color);
		}
		return this;
	}

	/**
	 * Adds multiple enchantments at once.
	 * @param enchantments map containing enchantments and levels
	 * @return builder instance
	 */
	@Contract("_ -> this")
	public ItemBuilder addEnchants(@NotNull Map<Enchantment, Integer> enchantments) {
		if (meta != null) {
			enchantments.forEach((enchant, level) -> meta.addEnchant(enchant, level, true));
		}
		return this;
	}

	/**
	 * Finalizes structural builder adjustments and overrides item amount.
	 * <p><b>NOTE:</b> This method breaks the chaining flow and returns raw {@link ItemStack}.</p>
	 * @param i target item amount
	 * @return finalized ItemStack instance
	 */
	public ItemStack setAmount(int i) {
		item.setAmount(i);
		return item;
	}

	/**
	 * Configures the new Item Model feature introduced in 1.21.4+.
	 * Uses NamespacedKey instead of old CustomModelData integer IDs.
	 * <pre>{@code
	 * builder.setItemModel("knight_sword");
	 * }</pre>
	 * @param modelKey item model target key string
	 * @return builder instance
	 */
	@Contract("_ -> this")
	public ItemBuilder setItemModel(@NotNull String modelKey) {
		if (meta != null) {
			NamespacedKey key = NamespacedKey.fromString(modelKey);
			if (key != null) {
				meta.setItemModel(key);
			}
		}
		return this;
	}

	/**
	 * Configures legacy custom model data ID.
	 * @param data integer ID
	 * @return builder instance
	 * @deprecated Deprecated since 1.21.5. Use {@link #setItemModel(String)} instead.
	 */
	@Contract("_ -> this")
	@Deprecated(since = "1.21.5", forRemoval = true)
	public ItemBuilder setCustomModelData(@NotNull Integer data) {
		if (meta != null) {
			meta.setCustomModelData(data);
		}
		return this;
	}

	/**
	 * Embeds metadata tag using {@link PersistentDataType#STRING}.
	 * @param key unique namespace tag key
	 * @param value metadata value string
	 * @return builder instance
	 */
	@Contract("_, _ -> this")
	public ItemBuilder setTag(@NotNull String key, @NotNull String value) {
		if (meta != null) {
			NamespacedKey nsk = new NamespacedKey(this.plugin, key);
			meta.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, value);
		}
		return this;
	}

	/**
	 * Embeds metadata tag using {@link PersistentDataType#INTEGER}.
	 * @param key unique namespace tag key
	 * @param value metadata value integer
	 * @return builder instance
	 */
	@Contract("_, _ -> this")
	public ItemBuilder setTag(@NotNull String key, @NotNull Integer value) {
		if (meta != null) {
			NamespacedKey nsk = new NamespacedKey(this.plugin, key);
			meta.getPersistentDataContainer().set(nsk, PersistentDataType.INTEGER, value);
		}
		return this;
	}

	/**
	 * Finalizes structure and applies all Meta modifiers into a single ItemStack.
	 * @return completed {@link ItemStack} instance
	 */
	@Contract("-> !null")
	public ItemStack build() {
		if (meta != null) {
			item.setItemMeta(meta);
		}
		return item;
	}

	/**
	 * Static factory method to start chaining process via {@link Material}.
	 * @param plugin XmasLegacy plugin instance
	 * @param material material type
	 * @return new ItemBuilder instance
	 */
	@Contract("_, _ -> new")
	public static @NotNull ItemBuilder of(@NotNull JavaPlugin plugin, @NotNull Material material) {
		return new ItemBuilder(plugin, material);
	}

	/**
	 * Static factory method to start chaining process via cloning existing {@link ItemStack}.
	 * @param plugin XmasLegacy plugin instance
	 * @param itemStack itemStack to clone
	 * @return new ItemBuilder instance
	 */
	@Contract("_, _ -> new")
	public static @NotNull ItemBuilder of(@NotNull JavaPlugin plugin, @NotNull ItemStack itemStack) {
		return new ItemBuilder(plugin, itemStack);
	}
}