package Framework.LazberryRegistryFramework.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be executed sequentially during plugin shutdown (onDisable).
 *
 * @author Lazberry (LRF Architecture Team)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GracefulShutdown {

	/**
	 * Execution order priority during shutdown. Lower numerical values execute first.
	 */
	int priority() default 100;
}
