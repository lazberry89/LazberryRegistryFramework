package Framework.LazberryRegistryFramework;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public interface Registry<K, V> {
	boolean register(@Nullable V value);
	Optional<V> get(@Nullable K key);
	V getOrRegister(@Nullable K key, Function<? super K, ? extends V> mappingFunction);
	boolean unregister(@Nullable K key);
	boolean containsKey(@Nullable K key);
	boolean exists(@Nullable V value);
}
