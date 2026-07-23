package Framework.LazberryRegistryFramework;

import Framework.LazberryRegistryFramework.Annotation.ConfigObject;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

/**
 * Maps YAML ConfigurationSections directly into Type-Safe Java Records or POJOs.
 */
@Slf4j
public final class ConfigObjectMapper {
	private static final @NotNull String icon = LazberryRegistryFramework.icon();

	@SuppressWarnings("unchecked")
	public static <T> T mapConfigObject(Class<T> clazz) {
		ConfigObject configObject = clazz.getAnnotation(ConfigObject.class);
		if (configObject == null)
			throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @ConfigObject");

		FileConfiguration config;
		if ("config.yml".equalsIgnoreCase(configObject.file())) {
			config = LazberryRegistryFramework.plugin().getConfig();
		} else {
			File configFile = new File(LazberryRegistryFramework.plugin().getDataFolder(), configObject.file());
			if (!configFile.exists()) {
				LazberryRegistryFramework.plugin().saveResource(configObject.file(), false);
			}
			config = YamlConfiguration.loadConfiguration(configFile);
		}

		String path = configObject.path();
		ConfigurationSection section = path.isEmpty() ? config : config.getConfigurationSection(path);

		if (section == null) {
			log.warn("{} Configuration path '{}' not found in {}. Instantiating with defaults/nulls.",
					icon, path, configObject.file());
			section = config.createSection(path.trim().isEmpty() ? "default" : path);
		}

		try {
			if (clazz.isRecord()) {
				Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
				Parameter[] parameters = constructor.getParameters();
				Object[] args = new Object[parameters.length];

				for (int i = 0; i < parameters.length; i++) {
					String paramName = parameters[i].getName();
					Class<?> paramType = parameters[i].getType();
					args[i] = extractValue(section, paramName, paramType);
				}

				constructor.setAccessible(true);
				return (T) constructor.newInstance(args);
			} else {
				Constructor<T> constructor = clazz.getDeclaredConstructor();
				constructor.setAccessible(true);
				T instance = constructor.newInstance();

				for (var field : clazz.getDeclaredFields()) {
					field.setAccessible(true);
					String fieldName = field.getName();
					if (section.contains(fieldName)) {
						Object val = extractValue(section, fieldName, field.getType());
						field.set(instance, val);
					}
				}
				return instance;
			}
		} catch (Exception e) {
			log.error("{} Failed to map @ConfigObject for class {}", icon, clazz.getSimpleName(), e);
			throw new RuntimeException("Config mapping failure: " + clazz.getName(), e);
		}
	}

	private static @Nullable Object extractValue(ConfigurationSection section, String key, Class<?> type) {
		if (type == String.class) return section.getString(key, "");
		if (type == int.class || type == Integer.class) return section.getInt(key, 0);
		if (type == long.class || type == Long.class) return section.getLong(key, 0L);
		if (type == double.class || type == Double.class) return section.getDouble(key, 0.0);
		if (type == boolean.class || type == Boolean.class) return section.getBoolean(key, false);
		if (type == java.util.List.class) return section.getList(key);

		return section.get(key);
	}
}
