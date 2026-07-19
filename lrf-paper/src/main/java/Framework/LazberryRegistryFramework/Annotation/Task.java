package Framework.LazberryRegistryFramework.Annotation;

import Framework.ServerType;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>Task</h2>
 * <p>
 * Specifies that a target class is a framework-managed background routine component, subjecting it
 * to the IoC lifecycle and automated scheduler orchestration loops.
 * </p>
 * <p>
 * This annotation serves as a primary scanning marker for the framework's core reflection engine.
 * Classes decorated with this annotation must strictly implement the {@code Framework.LazberryRegistryFramework.Tasks}
 * interface to ensure complete structural compatibility with the lifecycle boot pipelines.
 * </p>
 * <p>
 * <b>Reflection Orchestration Mechanics:</b>
 * During the framework initialization phase, {@code Reflections.invokeReflections(ClassPath, InitializeType...)}
 * scans the classpath, identifies components tagged with this annotation, and filters them based on the active
 * server infrastructure. Valid components are subsequently processed via {@code Reflections.taskReflection(true)},
 * which dynamically invokes their initialization routines.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see Framework.LazberryRegistryFramework.Reflections
 * @see Framework.LazberryRegistryFramework.Tasks
 * @see Framework.ServerType
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Task {
	/**
	 * Defines the array of targeting environment contexts under which the decorated task component is
	 * permitted to initialize and execute.
	 * <p>
	 * The core reflection layer evaluates the provided {@link ServerType} class tokens against the active
	 * server environment configuration. If the current server environment does not match any of the declared
	 * types in this matrix, the component is excluded from the IoC initialization queue, preventing unnecessary
	 * background thread allocation.
	 * </p>
	 *
	 * @return A non-null array of {@link ServerType} class configurations validating execution boundaries.
	 */

	@NotNull Class<? extends ServerType>[] type();
}
