package Framework.LazberryRegistryFramework;

import Framework.InitializeType;
import Framework.LazberryRegistryFramework.Annotation.*;
import com.google.common.reflect.ClassPath;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static Framework.LazberryRegistryFramework.ManagerInjection.collectManagers;
import static Framework.LazberryRegistryFramework.ManagerInjection.initializeManagers;

/**
 * Public execution room for LazberryRegistryFramework lifecycle triggers.
 * This class takes fully initialized beans from {@link PackageScanner}, then automatically
 * binds them to Bukkit EventManager, CommandMap, Task schedulers, and Custom Registry systems.
 * All method invocations are dynamically mapped by {@link InitializeType}.
 * * @see PackageScanner
 * @see InitializeType
 * @see Reflection
 */
@Slf4j
public final class Reflections {
	private static final @NotNull String icon = LazberryRegistryFramework.icon(false);

	private static @NotNull JavaPlugin plugin() {
		return LazberryRegistryFramework.plugin();
	}

	/**
	 * Entry point for the whole LRF IoC boot process. Pre-registers core platform contexts,
	 * triggers dependency scanning, and dynamically invokes all internal @Reflection-marked
	 * registry methods unless explicitly blacklisted.
	 * * @param classPath  Guava's analyzed classpath metadata.
	 * @param exceptions Specific initialize types to skip or ignore during this reflection loop.
	 */
	@Reflection(type = InitializeType.EXCEPTED)
	public static void invokeReflections(@NotNull ClassPath classPath, InitializeType... exceptions) {
		DependencyContainer.registerInstance(JavaPlugin.class, plugin());
		DependencyContainer.registerInstance(File.class, plugin().getDataFolder());

		PackageScanner.buildAndInjectBeans(classPath);
		collectManagers();
		initializeManagers();

		gatherRegisteredDestruction();

		var methods = Reflections.class.getDeclaredMethods();
		List<InitializeType> exceptionList = Arrays.asList(exceptions);

		for (Method method : methods) {
			if (!method.isAnnotationPresent(Reflection.class)) continue;

			Reflection annotation = method.getAnnotation(Reflection.class);
			if (annotation.type().equals(InitializeType.EXCEPTED)) continue;
			if (exceptionList.contains(annotation.type())) continue;

			try {
				method.setAccessible(true);
				method.invoke(null, classPath);
				log.info("{} Register {} has been invoked", icon, method.getName());
			} catch (Exception e) {
				log.error("{} Error occurred while invoking reflection for method {}", icon, method.getName(), e);
			}
		}
	}

	private static void gatherRegisteredDestruction() {
		for (var entry : DependencyContainer.getContainer().entrySet()) {
			DestructiveClassEngine.registerDestruction(entry.getKey(), entry.getValue(), plugin());
		}
	}

	/**
	 * Iterates through the IoC container and binds all beans implementing Bukkit's {@link Listener}
	 * interface directly to the server's PluginManager.
	 * * @param classPath Automatically provided classpath metadata (unused but kept for reflection invoke uniformity).
	 */
	@Reflection(type = InitializeType.LISTENERS)
	public static void registerListeners(@NotNull ClassPath classPath) {
		try {
			for (var entry : DependencyContainer.getContainer().entrySet()) {
				Class<?> clazz = entry.getKey();
				if (Listener.class.isAssignableFrom(clazz)) {
					Listener instance = (Listener) entry.getValue();
					Bukkit.getPluginManager().registerEvents(instance, plugin());
					log.info("{} Listener {} Automatically registered from IoC Container", icon, clazz.getSimpleName());
				}
			}
		} catch (Exception e) {
			log.error("{} Error occurred while registering all listeners", icon, e);
		}
	}

	/**
	 * Scans the container for {@link CommandExecutor} implementations that are marked with the
	 * {@link Commands} annotation, maps their aliases, and safely registers them into the Bukkit runtime.
	 * Automatically registers {@link TabCompleter} logic if the bean implements it.
	 * * @param classPath Automatically provided classpath metadata (unused but kept for reflection invoke uniformity).
	 */
	@Reflection(type = InitializeType.COMMANDS)
	public static void registerCommands(@NotNull ClassPath classPath) {
		try {
			for (var entry : DependencyContainer.getContainer().entrySet()) {
				Class<?> clazz = entry.getKey();
				if (CommandExecutor.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Commands.class)) {
					Commands autoCommand = clazz.getAnnotation(Commands.class);

					List<String> allCommands = new ArrayList<>();
					allCommands.add(autoCommand.command());
					allCommands.addAll(Arrays.asList(autoCommand.aliases()));

					Object commandInstance = entry.getValue();

					for (String cmdName : allCommands) {
						var pluginCommand = plugin().getCommand(cmdName);
						if (pluginCommand == null) continue;

						pluginCommand.setExecutor((CommandExecutor) commandInstance);

						if (TabCompleter.class.isAssignableFrom(clazz))
							pluginCommand.setTabCompleter((TabCompleter) commandInstance);

						plugin().getSLF4JLogger().info("{} Command {} Automatically registered from IoC Container", icon, cmdName);
					}
				}
			}
		} catch (Exception e) {
			log.error("{} Error occurred while registering all Commands/TabCompleter", icon, e);
		}
	}

	/**
	 * Gateway to turn on all periodic background tasks / schedulers.
	 * * @param classPath Provided classpath metadata context.
	 */
	@Reflection(type = InitializeType.TASKS_ON)
	public static void startTasks(@NotNull ClassPath ignored) {
		taskReflection(true);
	}

	/**
	 * Gateway to gracefully turn off and cancel all active background tasks / schedulers.
	 * * @param classPath Provided classpath metadata context, nullable during shutdown phase.
	 */
	@Reflection(type = InitializeType.TASKS_OFF)
	public static void stopTasks(@Nullable ClassPath ignored) {
		taskReflection(false);
	}

	/**
	 * Internal logic processor that filters container beans for the {@link Task} annotation
	 * and {@link Tasks} interface implementation, executing either start or stop routines.
	 * * @param enable True to run tasks, false to cancel them.
	 */
	private static void taskReflection(boolean enable) {
		for (var entry : DependencyContainer.getContainer().entrySet()) {
			Class<?> clazz = entry.getKey();
			if (!clazz.isAnnotationPresent(Task.class)) continue;
			if (!Tasks.class.isAssignableFrom(clazz)) continue;

			Tasks instance = (Tasks) entry.getValue();
			try {
				if (enable) {
					instance.startTask(plugin());
					log.info("{} Task {} started successfully.", icon, clazz.getSimpleName());
				} else {
					instance.stopTask();
					log.info("{} Task {} stopped successfully.", icon, clazz.getSimpleName());
				}
			} catch (Exception e) {
				log.error("{} Error occurred while processing task {}", icon, clazz.getSimpleName(), e);
			}
		}
	}

	@Reflection(type = InitializeType.NETWORKS)
	public static void registerPluginChannels(@NotNull ClassPath ignored) {
		var messenger = Bukkit.getMessenger();
		PluginMessageRouter messageRouter = DependencyContainer.getBean(PluginMessageRouter.class);
		if (messageRouter == null) {
			messageRouter = new PluginMessageRouter();
			DependencyContainer.registerInstance(PluginMessageRouter.class, messageRouter);
		}

		try {
			for (var entry : DependencyContainer.getContainer().entrySet()) {
				Class<?> clazz = entry.getKey();
				Object instance = entry.getValue();

				if (PluginReceiver.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(InboundChannel.class)) {
					InboundChannel inbound = clazz.getAnnotation(InboundChannel.class);
					String channelName = inbound.value();
					messenger.registerIncomingPluginChannel(plugin(), channelName, messageRouter);
					messageRouter.registerRoute(channelName, (PluginReceiver) instance);

					log.info("{} Incoming PluginChannel [{}] -> {} Automatically registered", icon, channelName, clazz.getSimpleName());
				}

				if (PluginSender.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(OutboundChannel.class)) {
					OutboundChannel outbound = clazz.getAnnotation(OutboundChannel.class);
					String[] channelNames = outbound.value();
					for (String channel : channelNames) {
						messenger.registerOutgoingPluginChannel(plugin(), channel);
					}

					log.info("{} Outbound PluginChannels {} -> {} Automatically activated", icon, Arrays.toString(channelNames), clazz.getSimpleName());
				}
			}
		} catch (Exception e) {
			log.error("{} Error occurred while registering Plugin Channels", icon, e);
		}
	}
}