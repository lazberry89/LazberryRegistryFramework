package Framework.LazberryRegistryFramework;

import Framework.Annotation.Inject;
import Framework.FrameworkExceptions.CircularDependencyException;
import Framework.FrameworkExceptions.NotCompatibleWithServerException;
import Framework.FrameworkExceptions.NotValidInitializeTimingException;
import Framework.FrameworkExceptions.VirtualClassInjectException;
import Framework.LazberryRegistryFramework.Annotation.ConfigValue;
import Framework.Utils.ServerUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * <h2>DependencyContainer (Core IoC / DI Assembly Matrix & Singleton Component Engine)</h2>
 * <p>
 * This class serves as the ultimate runtime heart, master dependency injector, and definitive single
 * source of truth for the <b>Lazberry Component Framework (LRF)</b>. It governs the internal Inversion of Control (IoC)
 * application context, orchestrating the recursive structural resolution, instantiation, and lifecycle management
 * of all managed singleton beans.
 * </p>
 *
 * <h3>Core Injection Mechanics & Internal Architecture</h3>
 * <ul>
 * <li>
 * <b>Recursive Topological Graph Resolution:</b>
 * When a target bean is requested via {@link #getOrCreateBean(Class)}, the engine dynamically inspects its structural
 * constructor parameters via Java Reflection. It then recursively builds sub-dependency graphs down to the terminal
 * leaf nodes before rolling back up to instantiate the parent object.
 * </li>
 * <li>
 * <b>Strict Structural Loop Isolation (Circular Dependency Guard):</b>
 * To prevent catastrophic, infinite thread-loop crashes during stack traversal, the engine records active type targets
 * inside a strict, order-retaining {@link LinkedHashSet} boundary ({@code CONSTRUCTION_STACK}). If a sub-dependency
 * requests a type currently present in this execution trail, a fatal {@link CircularDependencyException} is instantly thrown.
 * </li>
 * <li>
 * <b>Configuration Injection Bridge (Value Mapping):</b>
 * Integrates directly with the framework's external data tier via {@link ConfigInjection}. If a constructor parameter
 * is intercepted with the {@link ConfigValue} metadata, the engine bypasses standard bean lookup and injects pre-parsed
 * configuration assets into the compilation slot.
 * </li>
 * <li>
 * <b>Lifecycle Interception Hook Integration:</b>
 * Immediately after an object is materialized via reflection but <i>prior</i> to its structural publication into the global
 * cache, the container evaluates if the object implements {@link LrfInitializer}. If matched, it fires {@link LrfInitializer#afterPropertiesSet()}
 * to guarantee that the bean is fully primed and synchronized.
 * </li>
 * </ul>
 *
 * <h3>Thread Safety & Boundary Restrictions</h3>
 * <p>
 * All active singleton instances are securely locked inside a deterministic {@link LinkedHashMap} matrix, preserving
 * natural resolution order characteristics. Direct mutable reference accessors are strictly locked to package-private boundaries,
 * ensuring that rogue external threads cannot tamper with or mutate the framework's underlying context registry state.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see PackageScanner
 * @see ServerUtils
 * @see LrfInitializer
 * @see StructuralLog
 */
@Slf4j
public final class DependencyContainer {
    private static final @NotNull Map<Class<?>, Object> BEAN_CONTAINER = new LinkedHashMap<>(30);
    private static final @NotNull Set<Class<?>> CONSTRUCTION_STACK = new LinkedHashSet<>(30);
    private static final @NotNull String icon = LazberryRegistryFramework.icon();

	/**
	 * Package-private gateway exposing the internal bean map storage directly to core framework bootsTrappers.
	 * <p>
	 * <b>Strict Isolation Protocol:</b>
	 * This method does not return a shallow copy; it exposes the exact structural memory address of the production container.
	 * Consequently, it must <b>NEVER</b> be accessed by domain business layers. Access is strictly limited to
	 * {@link Reflections} and {@link ManagerInjection} for framework booting pipelines.
	 * </p>
	 *
	 * @return The absolute live non-null {@link Map} tracking active class tokens to their respective singleton instances.
	 */
    @Contract(pure = true)
    static @NotNull Map<Class<?>, Object> getContainer() {
        return BEAN_CONTAINER;
    }

	/**
	 * Public portal that fetches a fully managed singleton bean from the container matching or inheriting the requested class type.
	 * <p>
	 * <b>Polymorphic Safe Casting:</b>
	 * Utilizes Java Generics to automatically handle type conversions downstream, removing the need for messy explicit
	 * manual casting block statements in business logic modules.
	 * </p>
	 *
	 * @param clazz Target class type token or interface specification to find inside the active registry.
	 * @param <T>   The implicit generic type parameter inferred by the contextual assignment.
	 * @return A fully prepared, safely cast instance matching the request; or {@code null} if no compatible beans exist.
	 */
    @SuppressWarnings("unchecked")
    public static @Nullable <T> T getBean(@NotNull Class<T> clazz) {
        for (var entry : BEAN_CONTAINER.entrySet()) {
            if (clazz.isInstance(entry.getValue()) ||clazz.isAssignableFrom(entry.getKey())) return (T) entry.getValue();
        }
        return null;
    }

	/**
	 * Directly injects an externally constructed object instance into the primary bean registry cache matrix.
	 * <p>
	 * <b>Early Context Insertion:</b>
	 * This portal is typically exploited to register non-managed structural singletons (such as the main {@link org.bukkit.plugin.Plugin}
	 * instance or native Spigot handles) prior to running the main classpath scanning phases, ensuring early availability for DI parameters.
	 * </p>
	 *
	 * @param clazz    The explicit type token acting as the index key inside the IoC registry.
	 * @param instance The non-null live object assigned to represent the type key.
	 */
    public static void registerInstance(@NotNull Class<?> clazz, @NotNull Object instance) {
        BEAN_CONTAINER.put(clazz, instance);
        if (LazberryRegistryFramework.isDebug()) log.info("{} Pre-registered external bean: {}", icon, clazz.getSimpleName());
    }

	/**
	 * Returns an isolated, safe copy of the current compilation stack tracking circular reference paths.
	 * Open exclusively to package utilities for diagnostic tracking, error reporting, and system health checks.
	 *
	 * @return A detached structural snapshot duplicate of the active construction type stack trace.
	 */
    static @NotNull Set<Class<?>> constructionStack() {
        return new LinkedHashSet<>(CONSTRUCTION_STACK);
    }

	/**
	 * The master cryptographic assembly line of the framework. It evaluates the cache, intercepts initialization blocks,
	 * and performs recursive, transactional constructor dependency injections over incoming type tokens.
	 * <p>
	 * <b>Transactional Resolution Algorithm Phase Matrix:</b>
	 * <ol>
	 * <li><b>Cache Hit Evaluation:</b> Scans the container to see if a type assignable to the target already exists. If found, returns it immediately.</li>
	 * <li><b>Abstraction Guardrail:</b> Intercepts direct abstract class or interface injection requests, throwing a {@link VirtualClassInjectException}.</li>
	 * <li><b>Server Context Verification:</b> Discards execution if {@link ServerUtils} declares the type incompatible with the active NMS environment.</li>
	 * <li><b>Loop Reference Interception:</b> Verifies the current type token is not already in the {@code CONSTRUCTION_STACK}, defusing cyclic locks.</li>
	 * <li><b>Parameter Extraction Loop:</b> Evaluates the prioritized constructor via {@link #getConstructor(Class)}, looping over parameters to resolve them recursively or fetch config data via {@link ConfigInjection}.</li>
	 * <li><b>Materialization & Lifecycle Trigger:</b> Invokes the constructor via reflection, evaluates the object for {@link LrfInitializer} lifecycle hooks, and caches the finalized bean.</li>
	 * </ol>
	 * </p>
	 *
	 * @param clazz The concrete target class token slated for full dependency compilation and instantiation.
	 * @return A fully initialized, dependency-satisfied singleton instance; or {@code null} if processing parameters fail.
	 * @throws VirtualClassInjectException     If attempting to directly construct a non-concrete interface/abstract model.
	 * @throws NotCompatibleWithServerException If the target type fails active server version compatibility rules.
	 * @throws CircularDependencyException    If a cyclical cross-reference graph loop is intercepted inside the path trace.
	 * @throws Exception                      If general reflection operations or factory pipelines throw inner compilation failures.
	 */
    static @Nullable Object getOrCreateBean(@NotNull Class<?> clazz) throws Exception {
        for (var entry : BEAN_CONTAINER.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                return entry.getValue();
            }
        }

        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            throw new VirtualClassInjectException(
                    "[LRF-Strict] Cannot inject interface or abstract class directly: " + clazz.getName() +
                            ". Please inject a concrete implementation or register it manually via registerExternalBean."
            );
        }

        if (ServerUtils.unCompatibleWithCurrentServer(clazz))
            throw new NotCompatibleWithServerException(clazz.getSimpleName() + " is NOT compatible with the current server type!");

        if (CONSTRUCTION_STACK.contains(clazz))
            throw new CircularDependencyException("Circular dependency detected involving: " + clazz.getSimpleName());

        int currentDepth = CONSTRUCTION_STACK.size();
        StructuralLog.logDependencyStart(currentDepth, clazz);

        CONSTRUCTION_STACK.add(clazz);
        Object instance = null;

        try {
            if (clazz.isEnum()) {
                Field instanceField = clazz.getDeclaredField("INSTANCE");
                instance = instanceField.get(null);
            } else {
                Constructor<?> targetConstructor = getConstructor(clazz);

                java.lang.reflect.Parameter[] parameters = targetConstructor.getParameters();
                Object[] paramInstances = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    java.lang.reflect.Parameter param = parameters[i];
                    Class<?> paramType = param.getType();

                    if (param.isAnnotationPresent(ConfigValue.class)) {
                        paramInstances[i] = ConfigInjection.resolve(
                                param.getAnnotation(ConfigValue.class),
                                paramType,
                                clazz
                        );
                        continue;
                    }
                    paramInstances[i] = getOrCreateBean(paramType);
                }

                instance = targetConstructor.newInstance(paramInstances);
                StructuralLog.logAssemblySuccess(currentDepth, clazz, parameters.length);

				instance = LrfProxyFactory.createProxy(clazz, instance);

                if (instance instanceof LrfInitializer) {
                    try {
                        ((LrfInitializer) instance).afterPropertiesSet();

                        StructuralLog.logLifecycleSuccess(currentDepth, clazz);
                    } catch (NotValidInitializeTimingException e) {
                        log.error("{} Initialization timing error for: {}", icon, clazz.getSimpleName(), e);
                        throw e;
                    }
                }
            }

            if (instance != null) BEAN_CONTAINER.put(clazz, instance);
            return instance;

        } finally {
            CONSTRUCTION_STACK.remove(clazz);
        }
    }

	/**
	 * Resolves the most appropriate target constructor for dependency injection based on framework rules.
	 * <p>
	 * <b>Prioritization Logic Matrix:</b>
	 * Automatically scans declaring constructor elements, prioritizing one decorated with the {@link Inject}
	 * annotation. If none are explicitly marked, it falls back to locating a standard public no-argument constructor.
	 * </p>
	 *
	 * @param clazz Target class type token undergo constructor metadata analysis.
	 * @return An accessible, prepared {@link Constructor} handle ready for invocation.
	 * @throws NoSuchMethodException If no marked or fallback default no-arg constructors exist on the target object.
	 */
    private static @NotNull Constructor<?> getConstructor(@NotNull Class<?> clazz) throws NoSuchMethodException {
        Constructor<?> targetConstructor = null;
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                targetConstructor = constructor;
                break;
            }
        }

        if (targetConstructor == null) {
            targetConstructor = clazz.getDeclaredConstructor();
        }

        targetConstructor.setAccessible(true);
        return targetConstructor;
    }
}
