package Framework.LazberryRegistryFramework;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MetadataRegistry {
	private static final Map<Class<?>, EntityMetadata> METADATA_CACHE = new ConcurrentHashMap<>();

	public static void register(@NotNull Class<?> clazz) {
		if (METADATA_CACHE.containsKey(clazz)) return;

		EntityMetadata metadata = new EntityMetadata(clazz);
		METADATA_CACHE.put(clazz, metadata);
		log.info("[LRF] Cached DB Entity metadata for Table: {}", metadata.getTableName());
	}

	public static EntityMetadata get(@NotNull Class<?> clazz) {
		return METADATA_CACHE.get(clazz);
	}

	public static Collection<EntityMetadata> getAll() {
		return METADATA_CACHE.values();
	}
}
