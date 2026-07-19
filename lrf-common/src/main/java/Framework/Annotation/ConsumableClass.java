package Framework.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>ConsumableClass</h2>
 * <p>
 * A strategic marker annotation indicating that the decorated class is a short-lived, transient,
 * or single-use component designed to be instantly consumed and discarded by the framework's processing engines.
 * </p>
 * <h3>Architectural Value & System Stability</h3>
 * <p>
 * Although this metadata does not enforce functional restrictions or trigger structural reflection logic
 * inside the core {@code DependencyContainer}, it acts as a critical documentation and static analysis anchor
 * that drastically enhances code readability and system safety:
 * <ul>
 * <li><b>Isolation from Singleton Abuse:</b> Explicitly warns developers that the target class is <b>NOT</b> a stateless
 * global singleton bean. It must never be injected into long-lived component constructors or stored in static caches,
 * preventing massive hidden memory leaks and unexpected mutation side-effects.</li>
 * <li><b>Immutable Thread Safety Signal:</b> Implies that the underlying object typically represents a transient payload
 * (such as high-throughput packet decoders, configuration snapshot mapping segments, or session-bound data structures).
 * Once processed, its reference boundary is dropped, ensuring safe garbage collection tracking.</li>
 * <li><b>Enhanced Clean Architecture:</b> Provides a clear semantic boundary between structural infrastructure controllers
 * and dynamic stateful domain objects, making the overall ecosystem self-documenting at a glance.</li>
 * </ul>
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConsumableClass {
}
