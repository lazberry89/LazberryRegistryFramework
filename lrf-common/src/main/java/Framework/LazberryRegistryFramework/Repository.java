package Framework.LazberryRegistryFramework;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Repository<K, V> {
	CompletableFuture<Void> saveAll(@NotNull Collection<V> collection);
	CompletableFuture<Void> saveAll();
	CompletableFuture<V> save(V value);

	CompletableFuture<Collection<V>> load();
	CompletableFuture<Optional<V>> findById(@NotNull K key);
	CompletableFuture<Boolean> exists(K key);

	CompletableFuture<Boolean> delete(@NotNull K key);
}
