package Framework.LazberryRegistryFramework;

import Framework.Annotation.Transactional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Internal concurrency management registry mapped to LRF singleton instances.
 * Allocates and caches {@link ReentrantLock} resources to fulfill {@link Transactional} requests.
 */
final class TransactionLockRegistry {
	private static final @NotNull ConcurrentHashMap<Object, ReentrantLock> instanceLocks = new ConcurrentHashMap<>();

	/**
	 * Retrieves or creates a ReentrantLock associated with the specific bean instance.
	 *
	 * @param target The underlying singleton component instance.
	 * @param fair   The fairness policy configured via annotation.
	 * @return A thread-safe lock instance bound to the target.
	 */
	@Contract("null, _ -> new")
	static @NotNull ReentrantLock getLock(Object target, boolean fair) {
		return instanceLocks.computeIfAbsent(target, key -> new ReentrantLock(fair));
	}
}
