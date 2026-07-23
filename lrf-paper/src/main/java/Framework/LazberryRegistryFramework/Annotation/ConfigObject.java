package Framework.LazberryRegistryFramework.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Class or Record to be automatically mapped from a YAML configuration section.
 * <p>
 * Managed instances will be automatically instantiated and bound to the IoC container.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigObject {

	/**
	 * Path key within config.yml (e.g., "database", "settings.redis").
	 * If empty, the root section is mapped.
	 */
	String path() default "";

	/**
	 * Target YAML file name if different from the default "config.yml".
	 */
	String file() default "config.yml";
}
