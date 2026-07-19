package Framework;

/**
 * <h2>ServerManager (Core Domain Business Lifecycle Contract)</h2>
 * <p>
 * Defines the foundational structural contract for high-level core system components
 * (e.g., Database connection pools, Redis distributed locks, User session caches, and Global game state machines)
 * inside the framework lifecycle.
 * </p>
 * <h3>Deterministic Two-Phase Boot Integration:</h3>
 * <p>
 * Any functional class that implements this interface automatically participates in the deterministic
 * initialization pipeline governed by {@code Framework.LazberryRegistryFramework.ManagerInjection}.
 * This contract guarantees that critical data architectures are fully prepared and active
 * <b>before</b> any Bukkit event listeners, command executors, or external network packet routers
 * are opened to handle client traffic.
 * </p>
 * <h3>Runtime Resiliency Isolation:</h3>
 * <p>
 * Implementations of this interface are sequentially executed inside isolated runtime catch boundaries.
 * If a specific manager encounters a fatal crash during its boot sequence, the framework isolates the
 * failure state, logs the exception matrix, and aggressively proceeds to initialize subsequent managers
 * to maximize general server survivability.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 */
public interface ServerManager {

	/**
	 * Executes the mandatory structural setup and boot logic required by the implementing system manager.
	 * <p>
	 * <b>Invocation Pipeline & Thread Mechanics:</b>
	 * This method is triggered synchronously on the main server thread by {@code ManagerInjection.initializeManagers()}
	 * during the primary framework boot phase. Heavy dynamic operations (such as blocking I/O database
	 * handshakes or heavy config preloading) should be handled defensively here, ensuring that any initialization
	 * errors are explicitly thrown back up the stream to let the framework log the assembly failure context.
	 * </p>
	 */
	void init();
}
