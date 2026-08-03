package Framework.LazberryRegistryFramework;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class UserRepository<K extends Comparable<K>> implements Repository<K, User<K>> {
	private final UserManager<K> userManager;
	private final String url;
	private final String user;
	private final String password;

	@Builder
	public UserRepository(UserManager<K> userManager, String url, String user, String password) {
		this.userManager = userManager;
		this.url = url;
		this.user = user;
		this.password = password;
	}

	@Override
	public CompletableFuture<Void> saveAll(@NotNull Collection<User<K>> collection) {
		return null;
	}

	@Override
	public CompletableFuture<Void> saveAll() {
		return null;
	}

	@Override
	public CompletableFuture<User<K>> save(User<K> value) {
		return null;
	}

	@Override
	public CompletableFuture<Collection<User<K>>> load() {
		return null;
	}

	@Override
	public CompletableFuture<Optional<User<K>>> findById(@NotNull K key) {
		return null;
	}

	@Override
	public CompletableFuture<Boolean> exists(K key) {
		return null;
	}

	@Override
	public CompletableFuture<Boolean> delete(@NotNull K key) {
		return null;
	}
}
