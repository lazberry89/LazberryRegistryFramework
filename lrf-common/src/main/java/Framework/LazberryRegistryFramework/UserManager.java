package Framework.LazberryRegistryFramework;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class UserManager<I extends Comparable<I>> implements Registry<I, User<I>> {
	private final Map<I, User<I>> users = new ConcurrentHashMap<>();

	public UserManager() {}

	@Override
	public boolean register(@Nullable User<I> user) {
		if(user == null) return false;
		return users.putIfAbsent(user.getId(), user) == null;
	}

	@Override
	public boolean unregister(@Nullable I key) {
		if(key == null) return false;
		return users.remove(key) != null;
	}

	@Override
	public Optional<User<I>> get(@Nullable I key) {
		if(key == null) return Optional.empty();
		return Optional.ofNullable(users.get(key));
	}

	@Override
	public User<I> getOrRegister(@Nullable I key, Function<? super I, ? extends User<I>> mappingFunction) {
		if (key == null || mappingFunction == null) return null;
		return users.computeIfAbsent(key, mappingFunction);
	}

	@Override
	public boolean containsKey(@Nullable I key) {
		if (key == null) return false;
		return users.containsKey(key);
	}

	@Override
	public boolean exists(@Nullable User<I> value) {
		if (value == null) return false;
		return users.containsValue(value);
	}

	public boolean removeIf(Predicate<? super User<I>> filter) {
		if (users.isEmpty()) return false;
		return users.values().removeIf(filter);
	}

	public void forEach(Consumer<? super User<I>> consumer) {
		if (users.isEmpty()) return;
		users.values().forEach(consumer);
	}

	public Optional<User<I>> findFirst(Predicate<? super User<I>> predicate) {
		if (users.isEmpty()) return Optional.empty();
		return users.values().stream()
				.filter(predicate)
				.findFirst();
	}

	public Collection<User<I>> filter(Predicate<? super User<I>> predicate) {
		if (users.isEmpty()) return Collections.emptyList();
		return users.values().stream()
				.filter(predicate)
				.toList();
	}

	public Collection<User<I>> getAll() {
		return Collections.unmodifiableCollection(users.values());
	}

	public boolean updateIfPresent(@Nullable I key, Consumer<? super User<I>> action) {
		if (key == null || action == null) return false;
		return users.computeIfPresent(key, (k, user) -> {
			action.accept(user);
			return user;
		}) != null;
	}

	public int size() {
		return users.size();
	}

	public Map<I, User<I>> snapshot() {
		return Map.copyOf(users);
	}

	public void clear() {
		users.clear();
	}
}
