package Framework.Utils;

import Framework.Annotation.Registry;
import Framework.Global;
import Framework.LazberryRegistryFramework.LazberryRegistryFramework;
import Framework.ServerType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * <h2>LRF Core Runtime Platform Verification Engine (Server Compatibility Utilities)</h2>
 * <p>
 * This class serves as the backbone utility enabling the Lazberry Registry Framework (LRF)
 * to dynamically filter and isolate independent components across a multi-platform
 * Minecraft server environment (e.g., BungeeCord, Paper, Velocity).
 * </p>
 * <p>
 * All methods are strictly designed as static blocks to provide foundational architectural stability,
 * ensuring platform compatibility can be safely evaluated at the absolute earliest stages of the engine initialization.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see ServerType
 * @see Registry
 */
public final class ServerUtils {

    /**
     * Instantiation of this utility class is strictly prohibited as it maintains no internal state.
     * Any attempt to instantiate this class via reflection will explicitly throw an {@link UnsupportedOperationException}
     * to guarantee absolute structural integrity.
     */
    @ApiStatus.Internal
    @Contract("-> fail")
    private ServerUtils() {
        throw new UnsupportedOperationException("Utility Class");
    }

    /**
     * [Lazy-Initialize Plugin Context Bridge]
     * <p>
     * Safely retrieves the main plugin context runtime instance using a lazy-evaluation mechanism.
     * </p>
     * <b>[Architectural Purpose]:</b>
     * During the server boot sequence, if the scanner fires before the main plugin instance is fully loaded
     * into memory, a fatal {@link NullPointerException} cycle will occur. To mitigate this risk, this method
     * dynamically evaluates the bridge only at the precise moment it is required, maximizing runtime safety.
     *
     * @return A guaranteed non-null, fully active {@link org.bukkit.plugin.java.JavaPlugin} main plugin instance.
     * @see org.bukkit.plugin.java.JavaPlugin
     */
    @Contract(value = "-> !null", pure = true)
    public static @NotNull JavaPlugin plugin() {
        return LazberryRegistryFramework.plugin();
    }

    /**
     * [Cross-Platform Component-to-Server Compatibility Matrix]
     * <p>
     * The ultimate filtering engine utilized by LRF to determine whether a given class is incompatible
     * with the currently running server environment, marking it for container isolation if necessary.
     * </p>
     * <b>[Operational Mechanics & Philosophy]:</b>
     * <ol>
     * <li><b>Blacklist Evaluation:</b> It first inspects the {@link Registry.Exclude} annotation on the target class.
     * If the current server type is matched within the exclusion array, the component is rejected immediately (returns true).</li>
     * <li><b>Whitelist Evaluation:</b> It then verifies the {@link Registry.Include} annotation. The method scans the
     * permitted server types using the `isServerTypeUnCompatible` algorithm. If not a single matching condition is satisfied,
     * it concludes the component is incompatible (returns true).</li>
     * <li><b>Global Defaulting:</b> If a pure component lacks both annotations, the LRF architectural philosophy
     * treats it as a universally allowed <b>GLOBAL</b> object, letting it pass through seamlessly (returns false).</li>
     * </ol>
     *
     * @param clazz The target component class currently being scanned for IoC container registration.
     * @return <b>true</b> if the component is incompatible with the current server runtime and must be skipped;
     * <b>false</b> if it is perfectly safe to proceed with injection.
     * @see Registry.Include
     * @see Registry.Exclude
     */
    public static boolean unCompatibleWithCurrentServer(@NotNull Class<?> clazz) {
	    ServerType current = getServerType(plugin());
	    Class<? extends ServerType> currentClass = current.getClass();

	    if (clazz.isAnnotationPresent(Registry.Exclude.class)) {
		    Registry.Exclude exclude = clazz.getAnnotation(Registry.Exclude.class);
		    for (Class<? extends ServerType> excludedType : exclude.value()) {
			    if (excludedType.isAssignableFrom(currentClass)) return true;
		    }
	    }

	    if (clazz.isAnnotationPresent(Registry.Include.class)) {
		    Registry.Include include = clazz.getAnnotation(Registry.Include.class);
		    boolean matched = false;
		    for (Class<? extends ServerType> targetType : include.value()) {
			    if (!isServerTypeUnCompatible(targetType, current)) {
				    matched = true;
				    break;
			    }
		    }
		    return !matched;
	    }
	    return false;
    }

    /**
     * [Micro-Validation Algorithm for Logical Platform Mismatch]
     * <p>
     * cross-references the virtual target server type ({@code targetType}) against the actual
     * active server runtime type ({@code currentType}) to calculate structural incompatibility.
     * </p>
     * <b>[Special Condition Handling]:</b>
     * If the target type is designated as {@link Framework.Global}, the engine executes a granular validation
     * check against the core subsystem configuration to determine if the active node enforces a global initializer phase
     * ({@link ServerType#requiresGlobalInitiator()}).
     *
     * @param targetType  The target platform specification declared within the component annotation.
     * @param currentType The active runtime platform type intercepted from the server environment.
     * @return <b>true</b> if a critical structural mismatch is detected; <b>false</b> if they are logically compatible.
     */
    @Contract(pure = true)
    private static boolean isServerTypeUnCompatible(@NotNull Class<? extends ServerType> targetType, @NotNull ServerType currentType) {
        if (targetType == currentType.getClass()) return false;
        return targetType != Global.class || !currentType.requiresGlobalInitiator();
    }

    /**
     * [Runtime Active Server Platform Identity Extractor]
     * <p>
     * Explicitly commits and dumps the local configuration asset (config.yml) to the disk space safely,
     * then extracts the user-defined string identifier (`server-type`) to parse it into a structured {@link ServerType}.
     * </p>
     * <b>[Defensive Engineering Guardrails]:</b>
     * By invoking `saveDefaultConfig()` prior to accessing the data pool, it structurally shields the system
     * from data-corruption or missing file crashes on fresh installations. If the key is null, corrupted,
     * or missing altogether, it triggers a robust fallback sequence returning the system default <b>"main"</b> node.
     *
     * @param plugin The main plugin context holding the target configuration stream.
     * @return The precise {@link ServerType} identity representing the role of this node in the network map.
     */
    public static @NotNull ServerType getServerType(@NotNull JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        return ServerType.getServerType(plugin.getConfig().getString("server-type", "main"));
    }
}
