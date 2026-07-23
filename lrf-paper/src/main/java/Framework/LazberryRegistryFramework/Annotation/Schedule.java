package Framework.LazberryRegistryFramework.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method for automated scheduled execution using Bukkit's Scheduler.
 *
 * @author Lazberry (LRF Architecture Team)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Schedule {

	/**
	 * Initial delay in server ticks before execution starts (1 sec = 20 ticks).
	 */
	long delay() default 0L;

	/**
	 * Execution interval period in server ticks.
	 * If set to -1, the task runs only once as a delayed task.
	 */
	long period() default -1L;

	/**
	 * Whether to execute asynchronously off the main server thread.
	 */
	boolean async() default false;
}
