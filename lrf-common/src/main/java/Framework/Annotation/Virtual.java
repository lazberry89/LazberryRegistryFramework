package Framework.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or structure as a Virtual Template / Blueprint that should be ignored during
 * automated IoC package scanning.
 * <p>
 * Useful for abstract implementations, mock classes, or template base structures that should not
 * be automatically instantiated into the DependencyContainer.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Virtual {
}
