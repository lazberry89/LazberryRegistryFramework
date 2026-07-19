package Framework.LazberryRegistryFramework.Annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>ConfigValue</h2>
 * <p>
 * Binds a constructor or factory parameter directly to a specific external configuration data path,
 * enabling automated value injection during the IoC component materialization phase.
 * </p>
 * <p>
 * This annotation is strictly restricted to {@link ElementType#PARAMETER}, serving as a targeted interceptor
 * marker inside the framework's core assembly matrix. It instructs the dependency injection layer to bypass
 * standard object bean lookups for the decorated slot and instead resolve primitive or structured configuration assets.
 * </p>
 * <p>
 * <b>Dependency Resolution Integration:</b>
 * During recursive bean instantiation inside {@code DependencyContainer#getOrCreateBean(Class)}, the reflection
 * loop evaluates each constructor parameter. If this metadata is detected, the execution flow routes the request
 * directly to the external configuration subsystem via {@code ConfigInjection#resolve}, which extracts, parses,
 * and injects the corresponding value matching the declared data path prior to object compilation.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see Framework.LazberryRegistryFramework.DependencyContainer
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigValue {

	/**
	 * Declares the absolute configuration hierarchy route path utilized to extract the target data value.
	 * <p>
	 * The underlying configuration resolution engine parses this string using the platform's standard
	 * node separation layout (typically dot-notation, e.g., {@code "database.mysql.credentials.password"}),
	 * traversing the active configuration file to locate and cast the requested mapping node.
	 * </p>
	 *
	 * @return The non-null structural configuration path string mapping to the destination asset node.
	 */
    @NotNull String path();
}
