package Framework.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>Inject</h2>
 * <p>
 * Specifies the designated constructor to be utilized by the framework's dependency injection engine
 * when initializing an IoC managed singleton bean.
 * </p>
 * <p>
 * This annotation is strictly restricted to {@link ElementType#CONSTRUCTOR}. It serves as the primary routing
 * override marker during the constructor prioritization phase inside the IoC assembly loop.
 * </p>
 * <p>
 * <b>Constructor Prioritization Mechanics:</b>
 * When {@code Framework.LazberryRegistryFramework.DependencyContainer#getOrCreateBean(Class)} schedules a target type
 * for instantiation, it delegates compilation metadata analysis to an internal lookup routine. The engine loops through
 * all declaring constructors:
 * <ul>
 * <li>If a constructor decorated with this {@code @Inject} metadata is intercepted, the engine selects it immediately
 * as the definitive assembly line and begins resolving its parametric sub-dependency graphs.</li>
 * <li>If no constructors bear this marker, the container gracefully drops back to a default strategy, executing a lookup
 * for the standard public zero-argument constructor via {@link java.lang.Class#getDeclaredConstructor(Class...)}.</li>
 * </ul>
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see java.lang.reflect.Constructor
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
}
