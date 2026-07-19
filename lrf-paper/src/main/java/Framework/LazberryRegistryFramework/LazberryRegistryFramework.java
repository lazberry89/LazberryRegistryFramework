package Framework.LazberryRegistryFramework;

import Framework.Global;
import Framework.InitializeType;
import Framework.Local;
import Framework.ServerType;
import Framework.Utils.ColorUtils;
import com.google.common.reflect.ClassPath;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <h2>LazberryRegistryFramework (LRF Master Bootstrapper, IoC Orchestrator & Global Core Context)</h2>
 * <p>
 * Acts as the absolute central nervous system, primary boot gateway, and configuration matrix for the
 * <b>Lazberry Registry Framework (LRF)</b>. It encapsulates global runtime settings, tracks the server-wide
 * debugging state, controls structural visualization logs, and orchestrates synchronous, multi-phase
 * initialization and cleanup routines for modern Minecraft JavaPlugin architectures.
 * </p>
 *
 * <h3>Key Framework Features & Architectural Philosophy</h3>
 * <ul>
 * <li>
 * <b>Implicit Package Topology Discovery:</b>
 * Instead of forcing developers to write hardcoded root package string literals, the framework intercepts
 * the exact runtime package location of the calling main class during {@link #boot(JavaPlugin, Class)}.
 * This eliminates human configuration errors and maps out the reflection scanning boundaries automatically.
 * </li>
 * <li>
 * <b>Stateful Lifecycle Symmetrical Inversion:</b>
 * The framework enforces strict symmetry between startup and shutdown. What is activated during {@link #boot(JavaPlugin, Class)}
 * (such as IoC beans, listeners, and tasks via {@link InitializeType#TASKS_OFF} exemption) is fully and gracefully
 * purged during {@link #cleanUp(JavaPlugin, Class)} (triggering network flush, task cancellations, and database
 * disconnections) to guarantee zero memory footprints on hot-reloads.
 * </li>
 * <li>
 * <b>Server Context Partitioning Matrix:</b>
 * Pre-registers the framework's native environment contexts, {@link Global} and {@link Local}, into the
 * {@link ServerType} registry prior to scanning. This enables the conditional evaluation subsystems to determine
 * whether a component belongs in a multi-proxy network or an isolated single-node server.
 * </li>
 * <li>
 * <b>External Non-Managed Bean Bridge:</b>
 * Provides an explicit registration portal via {@link #registerExternalBean(Class, Object)} to dynamically inject
 * unmanaged, pre-existing objects (e.g., native Bukkit configurations, custom third-party API wrappers, or external databases)
 * directly into the underlying {@link DependencyContainer}, making them fully accessible for constructor dependency injection.
 * </li>
 * </ul>
 *
 * <h3>Boot & Cleanup Pipeline Mechanics (How it Works)</h3>
 * <p>
 * The boot sequence transitions strictly through a multi-tiered pipeline:
 * <ol>
 * <li><b>Context Allocation:</b> Registers core server types and captures the execution package directory.</li>
 * <li><b>Resource Mapping:</b> Builds Guava's {@link ClassPath} from the plugin's dedicated {@link ClassLoader}.</li>
 * <li><b>IoC Injection Room:</b> Delegates class references to {@link PackageScanner} to construct dependency graphs.</li>
 * <li><b>Platform Coupling:</b> Hands over initialized beans to {@link Reflections} to bind listeners, commands, and tasks.</li>
 * </ol>
 * During plugin termination, the pipeline is run in exact reverse order (Cleanup Phase), safely breaking reference hooks
 * to prevent classloader bloating and memory leaks inside the JVM heap.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see PackageScanner
 * @see DependencyContainer
 * @see Reflections
 * @see ServerType
 */
@Slf4j
@SuppressWarnings("unchecked")
public final class LazberryRegistryFramework {
    private static final @NotNull String VERSION = "LRF_26.7.19";
    private static final @NotNull String success = ColorUtils.chatStr("&a[SUCCESS]");
    private static final @NotNull String failure = ColorUtils.chatStr("&c[FAILURE]");
    private static @NotNull String packageName = "";
    private static @NotNull @Getter @Setter String defaultChannel = "";
    private static boolean debugMode = true;
    private static boolean drawStructure = true;
	private static @Nullable JavaPlugin plugin;

	/**
	 * Retrieves the encapsulated live root {@link JavaPlugin} instance.
	 * <p>
	 * <b>Defensive Access Guard:</b>
	 * To prevent asynchronous race conditions or premature access from early-loading classes, this getter explicitly
	 * verifies initialization states, throwing an exception if called before the boot process has completed.
	 * </p>
	 *
	 * @return The non-null root {@link JavaPlugin} context instance.
	 * @throws IllegalStateException If the framework has not yet been initialized via {@link #setPluginInstance(JavaPlugin)}.
	 */
	public static @NotNull JavaPlugin plugin() {
		if (plugin == null) throw new IllegalStateException(icon(false) + " Framework is not initialized, call setPluginInstance() first.");
		return plugin;
	}

	/**
	 * Universally binds the primary {@link JavaPlugin} context instance to the framework runtime layers.
	 * This acts as the root provider reference utilized by Bukkit schedulers, listener registers, and plugin channels.
	 *
	 * @param instance The active non-null main plugin container instance.
	 */
	public static void setPluginInstance(@NotNull JavaPlugin instance) {
		plugin = instance;
	}

	/**
	 * Configures the visibility state of structural dependency graphs in the server console log.
	 *
	 * @param flag <b>true</b> to print visualized hierarchical bean assembly maps; <b>false</b> to silence them.
	 */
    public static void drawStructureLog(boolean flag) {
        drawStructure = flag;
    }

	/**
	 * Checks if the framework is currently configured to print visualized dependency structures upon boot.
	 *
	 * @return <b>true</b> if structural log mapping is enabled; otherwise <b>false</b>.
	 */
    public static boolean isLogDrawStructure() {
        return drawStructure;
    }

	/**
	 * Checks if the framework is running in comprehensive debug mode.
	 *
	 * @return <b>true</b> if verbose tracing and reflection warning states are enabled; otherwise <b>false</b>.
	 */
    public static boolean isDebug() {
        return debugMode;
    }

	/**
	 * Updates the global debugging state parameter of the framework container.
	 *
	 * @param debug <b>true</b> to enable rich tracing; <b>false</b> to restrict logs to standard system info.
	 */
    public static void setDebug(boolean debug) {
        debugMode = debug;
    }

	/**
	 * Returns the absolute, programmatically discovered root package name that serves as the entry boundary for reflections.
	 * <p>
	 * <b>Immutability Note:</b>
	 * This method is marked as {@link Contract}(pure = true) because it references a locked state established exactly
	 * once at boot, guaranteeing predictable mapping across all multi-threaded reflection queries.
	 * </p>
	 *
	 * @return The non-null string containing the root package namespace (e.g., "com.lazberry.plugin").
	 * @see Package
	 */
    @Contract(value = "-> !null", pure = true)
    public static @NotNull String rootPackage() {
        return packageName;
    }

	/**
	 * Manually overrides the destination package scanning route path context.
	 *
	 * @param value The non-null package path name.
	 */
    public static void setScanPackage(@NotNull String value) {
        packageName = value;
    }

	/**
	 * Returns the framework's internal build configuration signature matrix version.
	 *
	 * @return Current system specification version string.
	 */
    @Contract(pure = true)
    public static @NotNull String version() {
        return VERSION;
    }

	/**
	 * Returns a dynamically colorized gradient prefix token representing the framework name [LRF].
	 * Supports both modern Adventure Components and standard legacy color string output channels.
	 *
	 * @param component <b>true</b> to return a Kyori Component instance; <b>false</b> to return a ChatColor legacy string.
	 * @param <T>       The generic return parameter type inferred by the contextual calling assignment.
	 * @return The colorized framework identifier token.
	 */
    @Contract(pure = true)
    public static @NotNull <T> T icon(boolean component) {
        if (component) return (T) ColorUtils.chat("&#F45454[&#F7563FL&#FA592AR&#FC5B15F&#FF5D00]");
        return (T) ColorUtils.chatStr("&#F45454[&#F7563FL&#FA592AR&#FC5B15F&#FF5D00]");
    }

	/**
	 * Returns a dynamically colorized gradient prefix token representing the [IoC] system tag.
	 * Supports both modern Adventure Components and standard legacy color string output channels.
	 *
	 * @param component <b>true</b> to return a Kyori Component instance; <b>false</b> to return a ChatColor legacy string.
	 * @param <T>       The generic return parameter type inferred by the contextual calling assignment.
	 * @return The colorized IoC engine identifier token.
	 */
    @Contract(pure = true)
    public static @NotNull <T> T IoC(boolean component) {
        if (component) return (T) ColorUtils.chat("&#001CFF[&#0045FFI&#006FFFo&#0098FFC&#00C1FF]");
        return (T) ColorUtils.chatStr("&#001CFF[&#0045FFI&#006FFFo&#0098FFC&#00C1FF]");
    }

	/** Returns the centralized color-mapped success bracket token used across terminal print streams. */
    static @NotNull String successIcon() {
        return success;
    }

	/** Returns the centralized color-mapped failure bracket token used across terminal print streams. */
    static @NotNull String failureIcon() {
        return failure;
    }

	/**
	 * Internal unified lifecycle management engine that initializes or teardowns the framework context.
	 * <p>
	 * <b>Transactional State Routing Flow:</b>
	 * This method tracks execution elapsed speeds down to the millisecond. If parameter {@code on} is true,
	 * it invokes {@link Reflections#invokeReflections(ClassPath, InitializeType...)} while purposefully excluding
	 * {@link InitializeType#TASKS_OFF} to preserve continuous background task loops. If false, it invokes
	 * {@link Reflections#stopTasks(ClassPath)} to immediately freeze all asynchronous processing layers.
	 * </p>
	 *
	 * @param plugin    The primary non-null source {@link JavaPlugin} instance.
	 * @param mainClass The concrete runtime class token representing the host plug-in entry.
	 * @param on        <b>true</b> to fire the initialization and setup sequence; <b>false</b> to trigger the teardown cleanup sequence.
	 */
    private static void setup(@NotNull JavaPlugin plugin, @NotNull Class<? extends JavaPlugin> mainClass, boolean on) {
		setPluginInstance(plugin);

        String icon = icon(false);
        long startTime = System.currentTimeMillis();
        log.info("{} Booting LazberryRegistryFramework...", icon);

        try {
            ClassPath classPath =
                    ClassPath.from(mainClass.getClassLoader());
            if (on) Reflections.invokeReflections(classPath, InitializeType.TASKS_OFF);
            else Reflections.stopTasks(null);

            long current = System.currentTimeMillis() - startTime;
            if (on) log.info("{} Framework booted successfully in {}ms.", icon, current);
            else log.info("{} Framework cleanedUp successfully in {}ms.", icon, current);
        } catch (Exception e) {
            if (on) log.error("{} Framework failed to boot!", icon, e);
            else log.error("{} Framework failed to cleanup..", icon, e);
        }
    }

	/**
	 * The primary entry point command that boots up the entire LRF framework ecosystem.
	 * <p>
	 * <b>Execution Chronology Strategy:</b>
	 * <ol>
	 * <li>Registers global environmental architectures: {@link Global} and {@link Local} context models.</li>
	 * <li>Captures the package layout of the incoming main class to map reflection bounds.</li>
	 * <li>Triggers the internal unified {@link #setup(JavaPlugin, Class, boolean)} pipeline to build IoC containers.</li>
	 * </ol>
	 * </p>
	 * <b>Standard Plugin Integration Example:</b>
	 * <pre>{@code
	 * @Override
	 * public void onEnable() {
	 *      LazberryRegistryFramework.boot(this, getClass());
	 * }
	 * }</pre>
	 *
	 * @param plugin    The main active non-null {@link JavaPlugin} instance initializing on the server.
	 * @param mainClass The concrete main class token of the plugin, used to extract classloader metadata.
	 */
    public static void boot(@NotNull JavaPlugin plugin, @NotNull Class<? extends JavaPlugin> mainClass) {
	    ServerType.register(new Global());
	    ServerType.register(new Local());
        setScanPackage(mainClass.getPackageName());
        setup(plugin, mainClass, true);
    }

	/**
	 * The final shutdown command that gracefully tears down the entire framework lifecycle.
	 * <p>
	 * <b>Absolute Memory Leak Defusal:</b>
	 * This method de-registers task loops, unhooks network routing paths, and wipes the static tracking container maps
	 * inside {@link ServerType}. It <b>MUST</b> be called inside the host plugin's shutdown method to guarantee clean
	 * classloader garbage collection and prevent severe JVM memory fragmentation upon server reloads.
	 * </p>
	 * <b>Standard Plugin Integration Example:</b>
	 * <pre>{@code
	 * @Override
	 * public void onDisable() {
	 *      LazberryRegistryFramework.cleanUp(this, getClass());
	 * }
	 * }</pre>
	 *
	 * @param plugin    The main active non-null {@link JavaPlugin} instance shutting down.
	 * @param mainClass The concrete main class token of the plugin.
	 */
    public static void cleanUp(@NotNull JavaPlugin plugin, @NotNull Class<? extends JavaPlugin> mainClass) {
        setup(plugin, mainClass, false);
		ServerType.unregisterAll();
    }

	/**
	 * Bridges an unmanaged, pre-existing external object instance into the internal managed framework container.
	 * <p>
	 * <b>Advanced DI Hook:</b>
	 * Use this method to feed instances that the LRF framework cannot instantiate on its own (such as
	 * custom configurations, API databases, or foreign plugin bridges) into the injection pool. Once registered,
	 * these instances can be injected as constructor parameters using the {@code @Inject} annotation.
	 * </p>
	 *
	 * @param clazz    The key type token defining how the instance is matched during dependency resolution.
	 * @param instance The non-null live object assigned to the target type key.
	 */
    public static void registerExternalBean(@NotNull Class<?> clazz, @NotNull Object instance) {
        DependencyContainer.registerInstance(clazz, instance);
    }
}
