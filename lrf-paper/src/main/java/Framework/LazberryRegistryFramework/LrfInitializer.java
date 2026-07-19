package Framework.LazberryRegistryFramework;

import Framework.FrameworkExceptions.NotValidInitializeTimingException;

/**
 * Standard Functional Lifecycle Callback Hook for the LazberryRegistryFramework (LRF) Container.
 * * <p><h3>Overview & Core Purpose</h3>
 * In a managed IoC environment, standard Java constructors are highly discouraged for executing heavy
 * business initialization logic (e.g., loading config files, establishing database connections, or
 * warm-up caching). At the constructor phase, the framework has <i>not yet fully injected</i> neighboring
 * dependencies, which inevitably triggers catastrophic {@link NullPointerException}s if accessed prematurely.
 * * <p>By implementing {@code LrfInitializer}, a component explicitly instructs the LRF engine to trigger
 * the {@link #afterPropertiesSet()} routine <b>immediately after</b> all field/constructor injections
 * are perfectly resolved and instantiated, but <b>before</b> the bean is officially exposed to the global
 * {@code BEAN_CONTAINER} map.
 * * <p><h3>Operational Principle (How it works inside the Engine)</h3>
 * The LRF IoC engine handles this interface via the following deterministic pipeline:
 * <ol>
 * <li>{@link PackageScanner} scans the classpath and detects a valid registry class.</li>
 * <li>The engine recursively resolves and instantiates all required parameter dependencies via Reflection.</li>
 * <li>The target instance is created via {@code targetConstructor.newInstance(paramInstances)}.</li>
 * <li><b>[The Hook Phase]:</b> The engine checks if the newly created instance is an {@code instanceof LrfInitializer}.</li>
 * <li>If true, the engine safely intercepts the flow and invokes {@link #afterPropertiesSet()}.</li>
 * <li>If initialization completes without exceptions, the fully primed bean is stored into the singleton cache.</li>
 * </ol>
 * * <p><h3>Standard Usage Example</h3>
 * <pre>{@code
 * @Registry.Include(type = ServerType.GLOBAL)
 * public class ContentManager implements ServerManager, LrfInitializer {
 *      private ConfigurationHolder configHolder; // Fully injected by framework first
 *      private Map<String, String> internalCache;
 *
 *      @Inject
 *      public ContentManager(ConfigurationHolder configHolder, Map<String, String> cache) {
 *          this.configHolder = configHolder;
 *          this.internalCache = cache;
 *      }
 *
 *      @Override
 *      public void init() {
 *          // General Bukkit side enabler logic
 *      }
 *
 *      @Override
 *      public void afterPropertiesSet() throws NotValidInitializeTimingException {
 *          // 100% Safe to access configHolder here because injection is complete!
 *
 *          if (configHolder == null) {
 *              throw new NotValidInitializeTimingException("Framework injection failed critically.");
 *          }
 *      this.internalCache = new HashMap<>();
 *      log.info("ContentManager safely warmed up via LRF Lifecycle Hook.");
 *      }
 * }
 * }</pre>
 * * <p><h3>Defensive Exception Design</h3>
 * If the component detects that the server state is invalid, or if it is invoked manually by an external
 * rogue class outside the framework's legitimate booting timeline, it must strictly throw a
 * {@link NotValidInitializeTimingException}. This forces the LRF core engine to immediately halt
 * the plugin boot chain, safeguarding the server from running in a corrupted or half-baked state.
 * * @see PackageScanner
 * @see NotValidInitializeTimingException
 */
@FunctionalInterface
public interface LrfInitializer {

    /**
     * Invoked by the LRF core engine context after it has successfully set all factory properties
     * and satisfied all constructor/field dependency injection requirements.
     * * <p>Use this method to perform final complex state initializations or component warm-ups
     * that rely entirely on neighboring managed beans.
     * * @throws NotValidInitializeTimingException If the initialization logic is triggered during an incorrect
     * server state, or if mandatory configuration assets are corrupted.
     */
    void afterPropertiesSet() throws NotValidInitializeTimingException;
}