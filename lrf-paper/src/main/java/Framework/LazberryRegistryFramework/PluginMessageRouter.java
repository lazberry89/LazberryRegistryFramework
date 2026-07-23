package Framework.LazberryRegistryFramework;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2>PluginMessageRouter (Centralized Inter-Proxy Message Multiplexer)</h2>
 * <p>
 * Acts as the centralized asynchronous event multiplexer and dynamic routing table for all inbound
 * platform plugin messaging channels. It bridges the native Bukkit {@link PluginMessageListener} layer
 * to LRF's decoupled, annotation-driven IoC container ecosystem.
 * </p>
 * <h3>Deep-Dive Architecture & Lifecycle Mechanics:</h3>
 * <ul>
 * <li><b>Decoupled Multiplexing:</b> Rather than forcing developers to register multiple native
 * sub-listeners to Bukkit's rigid messaging system, this class functions as a single unified
 * gateway endpoint on the server.</li>
 * <li><b>Dynamic Route Mapping:</b> Subscription paths are dynamically formed using a internal
 * multimap structure ({@code Map<String, List<PluginReceiver>>}). Multiple distinct plugin modules
 * can safely listen to the exact same channel without collision or cross-contamination.</li>
 * <li><b>Defensive Execution Boundaries:</b> Inbound packets are automatically decoded under strict
 * {@code StandardCharsets.UTF_8} parameters. The invocation loop for each individual registered receiver
 * is completely isolated within localized try-catch blocks, guaranteeing that a failure or exception
 * within one downstream module will never halt the global network relay pipeline.</li>
 * </ul>
 * @author Lazberry (LRF Architecture Team)
 * @see org.bukkit.plugin.messaging.PluginMessageListener
 * @see PluginReceiver
 * @see DependencyContainer
 */
@Slf4j
public final class PluginMessageRouter implements PluginMessageListener {
    private final Map<String, List<PluginReceiver>> routes = new HashMap<>();
    private static final String icon = LazberryRegistryFramework.icon();

	/**
	 * Registers an independent functional receiver subscription hook targeting a specified network channel signature.
	 * <p><b>Thread-Safety Note:</b> Since registrations predominantly happen during the synchronous server startup
	 * initialization phases via reflection scanners, this method optimizes mapping insertion routines using
	 * internal collection mapping parameters.</p>
	 *
	 * @param channel  The absolute network target path identifier (e.g., "lrf:data_sync").
	 * @param receiver The target reactive functional bean destined to process incoming data streams.
	 */
    public void registerRoute(@NotNull String channel, @NotNull PluginReceiver receiver) {
        routes.computeIfAbsent(channel, k -> new ArrayList<>()).add(receiver);
    }

	/**
	 * Low-level native interceptor callback triggered explicitly by the underlying server engine runtime
	 * whenever an external byte payload lands on an registered channel pathway.
	 * * <p><b>Operational Flow:</b>
	 * 1. Evaluates existing route tree maps to check for active listeners. If empty, the byte array is discarded instantly.
	 * 2. Transforms raw binary buffers into standardized UTF-8 Strings.
	 * 3. Conditionally outputs runtime diagnostics logs if {@link LazberryRegistryFramework#isDebug()} is asserted.
	 * 4. Safe-loops through active receivers, shielding the global runtime thread state from structural downstream crashes.
	 * </p>
	 *
	 * @param channel The dynamic channel key from which the payload originated.
	 * @param player  The sender context representing the player connection associated with this packet transaction.
	 * @param message The raw binary stream transmitted from proxy boundaries.
	 */
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        List<PluginReceiver> receivers = routes.get(channel);
        if (receivers == null || receivers.isEmpty()) return;

        String content = new String(message, StandardCharsets.UTF_8);

        if (LazberryRegistryFramework.isDebug()) {
            log.info("{} [Network Inbound] Channel: {} | Content Length: {}", icon, channel, content.length());
        }

        for (PluginReceiver receiver : receivers) {
            try {
                receiver.receive(content);
            } catch (Exception e) {
                log.error("{} Exception occurred in Receiver for channel: {}", icon, channel, e);
            }
        }
    }
}
