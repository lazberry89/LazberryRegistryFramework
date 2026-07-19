package Framework.Annotation;

import Framework.ServerType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>Registry</h2>
 * <p>
 * Serves as the primary organizational namespace and structural container for the framework's
 * Inversion of Control (IoC) registration filtering metadata.
 * </p>
 * <p>
 * This annotation acts as a multi-layered sorting matrix during the classpath scanning phase executed by
 * {@code PackageScanner}. By encapsulating specialized sub-annotations, it enables declarative inclusion
 * and exclusion routing criteria based on the active structural {@link ServerType} environment.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see Framework.ServerType
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Registry {

	/**
	 * <h3>Registry.Include</h3>
	 * <p>
	 * Explicitly marks a target class token to be scanned, processed, and registered as a fully managed singleton bean
	 * inside the central {@code DependencyContainer} context pool under matching environmental parameters.
	 * </p>
	 * <p>
	 * If an array of {@link ServerType} classes is specified, the orchestration engine verifies the active infrastructure matrix
	 * prior to allocating resources. If the running server profile matches any declared token in this configuration matrix,
	 * the target component enters the dynamic assembly line pipeline.
	 * </p>
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@interface Include {

		/**
		 * Defines the array of targeting environment contexts under which the decorated component is
		 * permitted to be registered inside the IoC container layer.
		 *
		 * @return An array of {@link ServerType} class configurations validating registration boundaries.
		 */
		Class<? extends ServerType>[] value();
	}

	/**
	 * <h3>Registry.Exclude</h3>
	 * <p>
	 * Explicitly restricts and bars a target class component from entering the IoC compilation queue,
	 * purging its registration profile early if the running infrastructure meets the declared conditions.
	 * </p>
	 * <p>
	 * This metadata serves as a protective override mechanism to deactivate components (e.g., development utilities or
	 * proxy-specific listeners) when deployed into specific platform environments, preventing unnecessary background memory leaks.
	 * </p>
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@interface Exclude {

		/**
		 * Defines the array of targeting environment contexts under which the decorated component is
		 * strictly prohibited from initializing.
		 *
		 * @return An array of {@link ServerType} class tokens that trigger immediate component exclusion.
		 */
		Class<? extends ServerType>[] value();
	}
}
