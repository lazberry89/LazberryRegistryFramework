package Framework.Utils;

import Framework.LazberryRegistryFramework.LazberryRegistryFramework;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Static Utility class for making entity stunned.
 * {@link EffectListener} is actually handling events,
 * and this class Only handles logic.
 * Also, consumer for lambda is componented to handle target Easier.
 * <pre>{@code
 * StunUtils.stun(target);
 * StunUtils.stun(target, 60, t -> {
 *     t.sendMessage("stun released."); //Consumer accepted when stun timer done with Valid target.
 * });
 * }</pre>
 * @see LivingEntity
 * @see Consumer
 * @see EntityMoveEvent
 * @see PlayerMoveEvent
 */
public final class StunUtils {
    private static final @NotNull Map<UUID, Long> stunMap = new HashMap<>();
    private static final @NotNull Map<UUID, String> reasonMap = new HashMap<>();
    private static final @NotNull Set<UUID> activeStunTimers = new HashSet<>();

    /**
     * Block constructor call from another class.
     * @see ApiStatus
     * @see UnsupportedOperationException
     */
    @ApiStatus.Internal
    @Contract("-> fail")
    private StunUtils() {
        throw new UnsupportedOperationException("Utility Class");
    }

    /**
     * When {@link LazberryRegistryFramework} is used in field, there would be possibility that
     * instance is null. This method is called only the utility is used, preventing {@link NullPointerException}
     * @return Plugin instance {@link JavaPlugin}
     * @see NullPointerException
     */
    @Contract(pure = true)
    private static @NotNull JavaPlugin plugin() {
        return LazberryRegistryFramework.plugin();
    }

    /**
     * Always, when someone is stunned there must be reason. This method returns why he/her/it is stunned.
     * <pre>{@code
     * String reason = StunUtils.getReason(uuid);
     * if (reason == null) {
     *     p.sendMessage("Target uuid is not stunned!");
     * }
     * }</pre>
     * @param uuid target's UniqueId
     * @return null if target is not stunned, else returns reason why target is stunned.
     * @see UUID
     * @see String
     */
    public static @Nullable String getReason(@NotNull UUID uuid) {
        if (!isStunned(uuid)) return null;
        return reasonMap.get(uuid);
    }

    /**
     * This method tells if target uuid is stunned.
     * <pre>{@code
     * if (StunUtils.isStunned(uuid)) {
     *     p.sendMessage("Target is stunned!");
     * }
     * }</pre>
     * @param uuid who you want to know if stunned
     * @return Whether target is stunned
     * @see Boolean
     * @see UUID
     */
    @Contract(value = "_ -> _", pure = true)
    public static boolean isStunned(@NotNull UUID uuid) {
        return activeStunTimers.contains(uuid);
    }

    /**
     * Overloaded method that tasks uuid instead of LivingEntity, not having lambda Consumer.
     * @param uuid target's UniqueId
     * @param period duration to stun
     * @param reason why the target is stunned
     */
    public static void stun(@NotNull UUID uuid, long period, @NotNull String reason) {
        boolean isTimerRunning = stunMap.containsKey(uuid);

        stunMap.put(uuid, stunMap.getOrDefault(uuid, 0L) + period);
        reasonMap.put(uuid, reason);
        activeStunTimers.add(uuid);

        if (isTimerRunning) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                Long current = stunMap.get(uuid);

                if (current == null || current <= 0L) {
                    release(uuid);

                    this.cancel();
                    return;
                }

                stunMap.put(uuid, current - 1);
            }
        }.runTaskTimer(plugin(), 0L, 1L);
    }

    /**
     * Stun target for an undetermined duration
     * <pre>{@code
     * StunUtils.stun(uuid, "I don't like him.");
     * }</pre>
     * @param uuid target's UniqueId
     * @param reason why the target is stunned.
     * @see UUID
     * @see String
     */
    public static void stun(@NotNull UUID uuid, @NotNull String reason) {
        activeStunTimers.add(uuid);
        reasonMap.put(uuid, reason);
    }

    /**
     * Release target whether the target is stunned or not.
     * <pre>{@code
     * StunUtils.release(uuid);
     * }</pre>
     * @param uuid target's UniqueId
     * @see String
     */
    public static void release(@NotNull UUID uuid) {
        stunMap.remove(uuid);
        activeStunTimers.remove(uuid);
        reasonMap.remove(uuid);
    }

    /**
     * Stuns target for intended time. Also, this method handles target when released with Consumer.
     * <pre>{@code
     * StunUtils.stun(target, 60L, "No reason.I don't like it.", t -> {
     *     t.teleport(loc);
     *     t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3, 1));
     *     t.remove();
     * });
     * }</pre>
     * @param target whom to apply stun effect
     * @param period duration of stun effect
     * @param reason why the target is stunned
     * @param onRelease when stunned target is valid and timer is done
     * @see BukkitRunnable#runTaskLater(Plugin, long)
     * @see LivingEntity
     * @see Consumer
     */
    public static void stun(@NotNull LivingEntity target, long period, @NotNull String reason, @Nullable Consumer<LivingEntity> onRelease) {
        var uuid = target.getUniqueId();
        boolean isTimerRunning = stunMap.containsKey(uuid);

        stunMap.put(uuid, stunMap.getOrDefault(uuid, 0L) + period);

        activeStunTimers.add(uuid);
        reasonMap.put(uuid, reason);

        if (isTimerRunning) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                Long current = stunMap.get(uuid);

                if (current == null || current <= 0L) {
                    release(uuid);

                    if (onRelease != null && target.isValid()) onRelease.accept(target);

                    this.cancel();
                    return;
                }

                stunMap.put(uuid, current - 1);
            }
        }.runTaskTimer(plugin(), 0L, 1L);
    }

    /**
     * Indicator used for a listener that handles stun logic.
     * @param uuid target's uniqueId
     * @param language Language to use
     * @return Component indicator message
     * @see EffectListener#EntityStunListener(EntityMoveEvent)
     * @see EffectListener#PlayerStunListener(PlayerMoveEvent)
     */
    public static @NotNull Component reasonIndicator(@NotNull UUID uuid, @NotNull Lang language) {
        String reason = getReason(uuid);
        if (reason == null) return ColorUtils.chat("");
        return switch (language) {
            case KOREAN -> ColorUtils.chat(Alert.YELLOW + reason + "(으)로 인해 움직임이 취소됨");
            case ENGLISH -> ColorUtils.chat(Alert.YELLOW + "Stunned due to " + reason);
        };
    }
}
