package Framework.Utils;

import Framework.LazberryRegistryFramework.LazberryRegistryFramework;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <h2>KeyUtils (Persistent Data Container Refraction Subsystem)</h2>
 * <p>
 * Provides centralized abstraction boundaries for manipulating Bukkit's {@link PersistentDataContainer} (PDC).
 * This utility significantly simplifies NBT-equivalent metadata injection and retrieval on game primitives.
 * </p>
 * <h3>Architectural Purpose & Type Safety Wrapper:</h3>
 * <p>
 * Native Bukkit PDC development requires boilerplate pairing of {@link NamespacedKey} and explicit
 * {@link PersistentDataType} constants. This engine dynamically resolves the appropriate primitive or
 * array data type mapper by evaluating raw class tokens ({@link Class}), eliminating generic parameter
 * misAlignments and cutting down runtime type casting bugs.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see org.bukkit.persistence.PersistentDataContainer
 * @see org.bukkit.persistence.PersistentDataType
 * @see org.bukkit.NamespacedKey
 */
@SuppressWarnings("unchecked")
public final class KeyUtils {

	/**
	 * Instantiation of this utility class is strictly prohibited as it maintains no internal state.
	 * Any attempt to instantiate this class via reflection will explicitly throw an {@link UnsupportedOperationException}
	 * to guarantee absolute structural integrity.
	 */
    @ApiStatus.Internal
    private KeyUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

	/**
	 * Lazy-lookup route providing the root framework plugin context required for namespacing.
	 */
	private static @NotNull JavaPlugin plugin() {
		return LazberryRegistryFramework.plugin();
	}

	/**
	 * Constructs a newly isolated {@link NamespacedKey} anchored explicitly under the current
	 * framework core's plugin identifier namespace.
	 * * <b>Example Usage:</b>
	 * <pre>{@code
	 * NamespacedKey customId = KeyUtils.get("custom_item_id");
	 * }</pre>
	 *
	 * @param value The localized non-null string key to register (e.g., "item_level").
	 * @return A newly initialized {@link NamespacedKey} instance bound to LRF's context.
	 */
    @Contract("_ -> new")
    public static @NotNull NamespacedKey get(@NotNull String value) {
        return new NamespacedKey(plugin(), value);
    }

	/**
	 * Extracts an encrypted metadata attribute from an {@link ItemStack} using explicit class token matching.
	 *
	 * @param item  The target source item stack container (nullable).
	 * @param key   The unique identifier key signature allocated to the metadata.
	 * @param clazz The explicit value class token used to deduce the underlying {@link PersistentDataType}.
	 * @param <V>   The expected return object parameter type.
	 * @return The extracted value context if mapped successfully; otherwise {@code null}.
	 */
    @Contract("null, _, _ -> null")
    public static <V> @Nullable V get(@Nullable ItemStack item, @NotNull NamespacedKey key, @NotNull Class<V> clazz) {
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataType<?, V> type = getDataType(clazz);
        return meta.getPersistentDataContainer().get(key, type);
    }

	/**
	 * Extracts an encrypted metadata attribute from an {@link ItemStack} utilizing a direct,
	 * unmanaged native {@link PersistentDataType} specifier.
	 *
	 * @param item The target source item stack container (nullable).
	 * @param key  The unique identifier key signature allocated to the metadata.
	 * @param type The exact native data type structure expected within the container byte map.
	 * @param <V>  The expected return object parameter type.
	 * @return The extracted value context if mapped successfully; otherwise {@code null}.
	 */
    @Contract("null, _, _ -> null")
    public static <V> @Nullable V get(@Nullable ItemStack item, @NotNull NamespacedKey key, @NotNull PersistentDataType<?, V> type) {
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(key, type);
    }

	/**
	 * Extracts an encrypted metadata attribute from an {@link ItemStack}, redirecting instantly
	 * to a non-null fallback default anchor if resolution yields empty results.
	 *
	 * @param item The target source item stack container (nullable).
	 * @param key  The unique identifier key signature allocated to the metadata.
	 * @param def  The non-null fallback baseline instance to apply upon key absence.
	 * @param <T>  The expected return object parameter type.
	 * @return The verified data stored inside the metadata space, or {@code def} if missing.
	 */
    @Contract("null, _, _ -> param3")
    public static <T> @NotNull T get(@Nullable ItemStack item, @NotNull NamespacedKey key, @NotNull T def) {
        T value = get(item, key, (Class<T>) def.getClass());
        return value == null ? def : value;
    }

	/**
	 * Evaluates whether an {@link ItemStack} contains an active metadata value matching the target key.
	 *
	 * @param item The target source item stack container (nullable).
	 * @param key  The unique identifier key signature being cross-examined.
	 * @return <b>true</b> if the metadata container is verified and holds the key; otherwise <b>false</b>.
	 */
    @Contract("null, _ -> false")
    public static boolean hasKey(@Nullable ItemStack item, @NotNull NamespacedKey key) {
        if (item == null) return false;
        var meta = item.getItemMeta();

        return meta != null
                && meta.getPersistentDataContainer().has(key);
    }

	/**
	 * Micro-translation router mapping Java object classes to native Bukkit {@link PersistentDataType} constants.
	 * Covers both standard object wrappers, native primitives, and continuous array streams.
	 *
	 * @param clazz The target type reflection matrix to cross-reference.
	 * @return A valid matching {@link PersistentDataType} reference.
	 * @throws IllegalArgumentException If the raw class token falls outside supported Bukkit PDC specifications.
	 */
    private static <V> PersistentDataType<?, V> getDataType(Class<V> clazz) {
        if (clazz == String.class) return (PersistentDataType<?, V>) PersistentDataType.STRING;
        if (clazz == Integer.class || clazz == int.class) return (PersistentDataType<?, V>) PersistentDataType.INTEGER;
        if (clazz == Double.class || clazz == double.class) return (PersistentDataType<?, V>) PersistentDataType.DOUBLE;
        if (clazz == Float.class || clazz == float.class) return (PersistentDataType<?, V>) PersistentDataType.FLOAT;
        if (clazz == Long.class || clazz == long.class) return (PersistentDataType<?, V>) PersistentDataType.LONG;
        if (clazz == Short.class || clazz == short.class) return (PersistentDataType<?, V>) PersistentDataType.SHORT;
        if (clazz == Byte.class || clazz == byte.class) return (PersistentDataType<?, V>) PersistentDataType.BYTE;
        if (clazz == Boolean.class || clazz == boolean.class) return (PersistentDataType<?, V>) PersistentDataType.BOOLEAN;

        if (clazz == int[].class) return (PersistentDataType<?, V>) PersistentDataType.INTEGER_ARRAY;
        if (clazz == long[].class) return (PersistentDataType<?, V>) PersistentDataType.LONG_ARRAY;
        if (clazz == byte[].class) return (PersistentDataType<?, V>) PersistentDataType.BYTE_ARRAY;

        throw new IllegalArgumentException("Unsupported PDC type. " + clazz.getName());
    }

	/**
	 * Programmatically injects or overwrites an arbitrary metadata field directly into an active {@link Entity}.
	 *
	 * @param entity The living target entity instance destined to receive the payload (nullable).
	 * @param key    The unique namespace key defining the slot identifier.
	 * @param value  The non-null raw object variable to persist.
	 * @param <V>    The object value type.
	 */
    public static <V> void set(@Nullable Entity entity, @NotNull NamespacedKey key, @NotNull V value) {
        if (entity == null) return;
        PersistentDataContainer container = entity.getPersistentDataContainer();
        PersistentDataType<?, V> type = getDataType((Class<V>) value.getClass());
        container.set(key, type, value);
    }

	/**
	 * Programmatically injects or overwrites an arbitrary metadata field directly inside an {@link ItemStack}.
	 * <p>
	 * <b>Crucial Mechanical Guardrail:</b>
	 * Changes done to {@link ItemMeta} are local copies. This pipeline implicitly invokes
	 * {@link ItemStack#setItemMeta(ItemMeta)} before boundary termination to flush memory layers back to the item stack.
	 * </p>
	 *
	 * @param item  The target item stack instance destined to receive the payload (nullable).
	 * @param key   The unique namespace key defining the slot identifier.
	 * @param value The non-null raw object variable to persist.
	 * @param <V>   The object value type.
	 */
    public static <V> void set(@Nullable ItemStack item, @NotNull NamespacedKey key, @NotNull V value) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        var container = meta.getPersistentDataContainer();
        PersistentDataType<?, V> type = getDataType((Class<V>) value.getClass());
        container.set(key, type, value);

        item.setItemMeta(meta);
    }

	/**
	 * Verifies whether an {@link ItemStack} possesses a precise assigned value bound under specific type parameters.
	 *
	 * @param item  The target item stack container to verify (nullable).
	 * @param key   The unique namespace key signature being checked.
	 * @param type  The exact native data type structure expected within the map.
	 * @param value The target object value to test against the internal match.
	 * @param <T>   Internal native type parameter representation.
	 * @param <V>   The value class type representation.
	 * @return <b>true</b> if the key exists and matches the provided value via equality; otherwise <b>false</b>.
	 */
    @Contract("null, _, _, _ -> false")
    public static <T, V> boolean hasKey(@Nullable ItemStack item, @NotNull NamespacedKey key, PersistentDataType<T, V> type, V value) {
        if (item == null) return false;

        var meta = item.getItemMeta();
        if (meta == null) return false;

        var container = meta.getPersistentDataContainer();

        if (!container.has(key, type)) return false;

        V actualValue = container.get(key, type);
        return value.equals(actualValue);
    }
}
