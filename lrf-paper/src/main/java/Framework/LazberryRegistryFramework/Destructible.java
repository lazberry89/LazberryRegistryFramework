package Framework.LazberryRegistryFramework;

/**
 * <h2>Destructible (Functional Resource De-allocation & Object Disposal Contract)</h2>
 * <p>
 * Represents a standard lifecycle teardown hook for components that require explicit, custom cleanups
 * upon their programmatic eviction or self-destruction.
 * </p>
 * <h3>Integration with the Teardown Subsystem:</h3>
 * <p>
 * When monitored by {@link DestructiveClassEngine}, any class implementing this single-method interface
 * will automatically receive a terminal callback via {@link #onDestroy()} exactly when its {@code @SelfDestruct}
 * lifespan expires or the framework undergoes emergency shutdowns.
 * </p>
 * <b>Architectural Intended Usage:</b>
 * Use this method to flush internal data maps, save temporary stats to a persistent database, close localized
 * stream buffers, or dispatch final visual farewell particle/sound packets to players.
 *
 * @author Lazberry (LRF Architecture Team)
 * @see DestructiveClassEngine
 */
@FunctionalInterface
public interface Destructible {

	/**
	 * Triggered synchronously by the destruction engine when the instance is evicted from memory maps.
	 * All local, resource-heavy entities must be disposed of here to ensure absolute garbage collection fluidity.
	 */
    void onDestroy();
}
