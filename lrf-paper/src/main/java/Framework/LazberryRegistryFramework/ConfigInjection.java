package Framework.LazberryRegistryFramework;

import Framework.FrameworkExceptions.InvalidConfigInjectException;
import Framework.LazberryRegistryFramework.Annotation.ConfigValue;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/**
 * <h2>LRF Declarative Configuration Injection Engine</h2>
 *
 * <h3>1. Motivation & Philosophy</h3>
 * In standard Bukkit/Paper plugin development, retrieving values from {@code config.yml} often leads to boilerplate
 * code scattering across various manager classes (e.g., {@code plugin.getConfig().getInt("path")}). This approach breaks
 * the inversion of control (IoC) principle and complicates testing.
 * <p>
 * {@code ConfigInjection} solves this by enabling <b>declarative configuration injection</b>. By parsing the {@link ConfigValue}
 * annotation placed directly on constructor parameters, the LRF IoC container automatically resolves and injects the
 * configured value during the bean assembly phase.
 *
 * <h3>2. Operational Principle & Mechanism</h3>
 * During the framework boot lifecycle, {@link DependencyContainer} inspects the constructor parameters of target registry classes.
 * If a parameter is annotated with {@code @ConfigValue}, the assembly loop bypasses the standard object dependency resolution
 * and routes execution to this engine.
 * <ol>
 * <li><b>Value Extraction:</b> Resolves the raw object from the Bukkit configuration wrapper using {@link ConfigValue#path()}.</li>
 * <li><b>Nullability Evaluation (Fail-Fast Policy):</b> If the value is completely missing or explicit null from the YML layout,
 * the engine evaluates the target parameter's type. Primitive types trigger an immediate fatal initialization halt, while
 * reference types gracefully accept null with a tracking warning.</li>
 * <li><b>Type-Safe Boxing Realignment:</b> Due to SnakeYML parsing ambiguity, numeric configurations might be loaded into unexpected
 * boxing states (e.g., Integer instead of Double). This engine bridges the casting gap via {@link Number} extraction.</li>
 * </ol>
 *
 * <h3>3. Rigid Guardrails (LRF-Strict Mode)</h3>
 * <ul>
 * <li><b>Primitive Null Anti-Contamination:</b> Primitive types (e.g., {@code int}, {@code double}, {@code boolean}) cannot hold a
 * {@code null} reference. Letting a null slip past would cause a messy {@link NullPointerException} or {@link IllegalArgumentException}
 * deep inside the reflection execution block. This engine implements a strict <b>Fail-Fast</b> guardrail that halts the entire
 * server initialization sequence, exposing the exact culprit class and missing YML path.</li>
 * <li><b>Type Safety Verification:</b> Prevents runtime misAlignments by matching YML-defined types with strict primitive/wrapper parameters
 * and safely wraps anomalous conditions into an explicit {@link InvalidConfigInjectException}.</li>
 * </ul>
 *
 * <h3>4. Concrete Usage Example</h3>
 * <pre>{@code
 * @Registry.Include
 * public class ArenaManager {
 *      private final SqlManager sqlManager;
 *      private final double maxDistance;
 *      private final String lobbyName;
 *
 *      @Inject
 *      public ArenaManager(
 *          SqlManager sqlManager,
 *          @ConfigValue(path = "arena.settings.max-distance") double maxDistance,
 *          @ConfigValue(path = "arena.settings.lobby-name") String lobbyName
 *          ) {
 *          this.sqlManager = sqlManager;
 *          this.maxDistance = maxDistance;
 *          this.lobbyName = lobbyName;
 *      }
 * }
 * }</pre>
 *
 * @author Lazberry (LRF Architecture Team)
 * @version 1.4.2
 * @see ConfigValue
 * @see DependencyContainer
 * @see InvalidConfigInjectException
 */
@Slf4j
public final class ConfigInjection {
    private static final @NotNull String icon = LazberryRegistryFramework.icon(false);

	private static @NotNull JavaPlugin plugin() {
		return LazberryRegistryFramework.plugin();
	}

    /**
     * Resolves and validates a single configuration value from the Bukkit runtime environment
     * for a targeted constructor parameter signature.
     * <p>
     * This method acts as the deterministic pipeline processor for configuration properties,
     * ensuring strict type conversion and preventing unboxing errors.
     *
     * @param annotation  The metadata carrier containing the absolute yml node string path.
     * @param paramType   The structural Java type metadata of the target injection site.
     * @param targetClass The encapsulating concrete bean class currently undergoing resolution (used for precise error tracking context).
     * @return The dynamically resolved object instance matched to the configuration node, or {@code null} if it is an optional reference field.
     * @throws InvalidConfigInjectException If a primitive parameter faces a missing configuration node, or if the underlying data structure
     * cannot be realigned to the requested parameter type.
     */
    static @Nullable Object resolve(@NotNull ConfigValue annotation, @NotNull Class<?> paramType, @NotNull Class<?> targetClass) {
        String path = annotation.path();
        Object rawValue = plugin().getConfig().get(path);
        if (rawValue == null) {
            if (paramType.isPrimitive()) {
                String errorMessage = String.format(
                        "[LRF-Strict] Fatal configuration error! In '%s', parameter requires primitive type '%s', but path '%s' is missing or null in config.yml!",
                        targetClass.getSimpleName(), paramType.getSimpleName(), path
                );
                log.error("{} {}", icon, errorMessage);
                throw new InvalidConfigInjectException(errorMessage);
            }
            log.warn("{} [LRF-Config] Optional field value is null for path: {} in class: {}", icon, path, targetClass.getSimpleName());
            return null;
        }

        try {
            if (paramType == int.class || paramType == Integer.class) return ((Number) rawValue).intValue();
            if (paramType == double.class || paramType == Double.class) return ((Number) rawValue).doubleValue();
            if (paramType == long.class || paramType == Long.class) return ((Number) rawValue).longValue();
            if (paramType == float.class || paramType == Float.class) return ((Number) rawValue).floatValue();
        } catch (ClassCastException e) {
            String typeErrorMessage = String.format(
                    "[LRF-Strict] Type mismatch! Path '%s' in config.yml cannot be cast to '%s' for class '%s'",
                    path, paramType.getSimpleName(), targetClass.getSimpleName()
            );
            log.error("{} {}", icon, typeErrorMessage);
            throw new InvalidConfigInjectException(typeErrorMessage);
        }

        return rawValue;
    }
}
