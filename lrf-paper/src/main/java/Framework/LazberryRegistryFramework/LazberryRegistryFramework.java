package Framework.LazberryRegistryFramework;

import Framework.Global;
import Framework.InitializeType;
import Framework.LazberryRegistryFramework.Annotation.GracefulShutdown;
import Framework.Local;
import Framework.ServerType;
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
 * <b>Lazberry Component Framework (LRF)</b>. By extending {@link JavaPlugin}, it encapsulates the entire
 * Bukkit/Spigot runtime lifecycle, automating IoC container creation, dependency injection, proxy synthesization,
 * scheduled task management, and prioritized graceful shutdowns with zero boilerplate requirements.
 * </p>
 *
 * <h3>Key Framework Features & Architectural Philosophy</h3>
 * <ul>
 * <li>
 * <b>Zero-Boilerplate Abstract Bootstrapping:</b>
 * Plugins implementing LRF inherit from this abstract master class instead of raw {@link JavaPlugin}.
 * Native lifecycle methods {@link #onEnable()} and {@link #onDisable()} are locked with {@code final} to enforce
 * strict boot/teardown encapsulation. Developers instead hook into optional lifecycle callbacks:
 * {@link #onLrfEnable()} and {@link #onLrfDisable()}.
 * </li>
 * <li>
 * <b>Implicit Package Topology Discovery:</b>
 * The framework automatically intercepts the exact runtime package namespace of the concrete subclass during
 * {@link #boot(JavaPlugin, Class)}. This eliminates human configuration errors and establishes implicit reflection
 * scanning boundaries without hardcoded package string parameters.
 * </li>
 * <li>
 * <b>Automated Lifecycle Symmetrical Inversion:</b>
 * startup and teardown phases are strictly inverted to prevent resource fragmentation. During plugin termination,
 * active repeating tasks registered via {@link ScheduleProcessor} are systematically cancelled, and handlers
 * decorated with {@link GracefulShutdown} are executed in prioritized order before purging
 * the underlying IoC container map.
 * </li>
 * <li>
 * <b>Server Context Partitioning Matrix:</b>
 * Pre-registers the framework's native environment contexts, {@link Global} and {@link Local}, into the
 * {@link ServerType} registry prior to scanning. This enables conditional evaluation subsystems to determine
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
 * <h3>Boot & Cleanup Pipeline Mechanics (How It Works)</h3>
 * <p>
 * The automated execution lifecycle transitions through a multi-tiered pipeline:
 * <ol>
 * <li><b>Context Allocation:</b> Registers core server types and captures the subclass package location.</li>
 * <li><b>Resource Mapping:</b> Builds Guava's {@link ClassPath} from the plugin's dedicated {@link ClassLoader}.</li>
 * <li><b>IoC & AOP Injection:</b> Delegates class references to {@link PackageScanner} to construct dependency graphs
 * and wrap beans with ByteBuddy proxies.</li>
 * <li><b>Post-Processing Hooks:</b> Automatically binds active {@link ScheduleProcessor} timers and registers {@link ShutdownRegistry} hooks.</li>
 * <li><b>Developer Hook Dispatch:</b> Triggers {@link #onLrfEnable()} for custom user-level setup code.</li>
 * </ol>
 * During plugin termination, the reverse sequence cancels scheduled tasks, flushes graceful shutdown queues,
 * and releases static references to guarantee complete garbage collection on plugin hot-reloads.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see ScheduleProcessor
 * @see ShutdownRegistry
 * @see PackageScanner
 * @see DependencyContainer
 * @see ServerType
 */
@Slf4j
public abstract class LazberryRegistryFramework extends JavaPlugin {
    private static final @NotNull String VERSION = "LRF_26.7.19";
    private static final @NotNull String success = "§a[SUCCESS]";
    private static final @NotNull String failure = "§c[FAILURE]";
    private static @NotNull String packageName = "";
    private static @NotNull @Getter @Setter String defaultChannel = "";
    private static boolean debugMode = true;
    private static boolean drawStructure = true;
	private static @Nullable JavaPlugin plugin;

	/**
	 * Primary entry point invoked by Bukkit upon plugin enablement.
	 * <p>
	 * <b>Inviolable Lifecycle Lock:</b> This method is declared {@code final} to guarantee that the LRF IoC
	 * bootstrap engine, package scanning pipeline, and AOP proxies are loaded in exact sequence before invoking
	 * user code. To insert custom enable logic, override {@link #onLrfEnable()}.
	 * </p>
	 */
	@Override
	public final void onEnable() {
		boot(this, getClass());
		onLrfEnable();
	}

	/**
	 * Primary teardown point invoked by Bukkit upon plugin disablement.
	 * <p>
	 * <b>Graceful Cleanup Guarantee:</b> This method is declared {@code final} to enforce safe teardown ordering.
	 * It first dispatches {@link #onLrfDisable()}, cancels active {@link ScheduleProcessor} task loops, executes
	 * prioritized {@link ShutdownRegistry} handlers, and purges reflection contexts to prevent JVM memory leaks.
	 * </p>
	 */
	@Override
	public final void onDisable() {
		try {
			onLrfDisable();
		} finally {
			ScheduleProcessor.cancelAllSchedules();
			ShutdownRegistry.executeShutdownSequence();
			cleanUp(this, getClass());
		}
	}

	/**
	 * User-definable lifecycle hook invoked immediately <b>AFTER</b> the LRF framework has completed container setup.
	 * <p>
	 * Override this method in your main plugin class to perform post-boot operations, such as registering custom
	 * listeners or outputting startup banner logs.
	 * </p>
	 */
	protected abstract void onLrfEnable();

	/**
	 * User-definable lifecycle hook invoked immediately <b>BEFORE</b> the LRF framework initiates task cancellation
	 * and graceful shutdown routines.
	 * <p>
	 * Override this method in your main plugin class to execute custom pre-teardown logic.
	 * </p>
	 */
	protected abstract void onLrfDisable();

	/**
	 * Retrieves the encapsulated live root {@link JavaPlugin} instance.
	 * <p>
	 * <b>Defensive Access Guard:</b>
	 * To prevent asynchronous race conditions or premature access from early-loading classes, this getter explicitly
	 * verifies initialization states, throwing an exception if called before the boot process has completed.
	 * </p>

	 * @return The non-null root {@link JavaPlugin} context instance.
	 * @throws IllegalStateException If the framework has not yet been initialized via {@link #setPluginInstance(JavaPlugin)}.
	 */
	public static @NotNull JavaPlugin plugin() {
		if (plugin == null) throw new IllegalStateException(icon() + " Framework is not initialized, call setPluginInstance() first.");
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
	 * once at boot, guaranteeing predictable mapping across all multithreaded reflection queries.
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
	 * Returns the colorized prefix token representing the framework identifier [LFR].
	 *
	 * @return Legacy color-coded console prefix token.
	 */
    @Contract(pure = true)
    public static @NotNull String icon() {
        return "§6[LFR]";
    }

	/**
	 * Returns the colorized prefix token representing the IoC container system tag [IoC].
	 *
	 * @return Legacy color-coded console system tag.
	 */
    @Contract(pure = true)
    public static @NotNull String IoC() {
        return "§a[IoC]";
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

        String icon = icon();
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
	 * <b>Absolute Memory Leak Refusal:</b>
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
