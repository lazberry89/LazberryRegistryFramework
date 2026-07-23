package Framework.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method or all methods within the target class
 * should be executed asynchronously off the main Server Thread (Bukkit Primary Thread).
 * * <h3>Architectural Overview</h3>
 * <p>
 * When a component managed by the Lazberry Registry Framework (LRF) invokes a method
 * marked with {@code @Async}, the core dynamic proxy intercepts the invocation via ByteBuddy.
 * If the call originates from the main thread, LRF dispatches the execution pipeline
 * to the asynchronous task scheduler pool.
 * </p>
 * * <h3>Core Requirements &amp; Usage Constraints</h3>
 * <ul>
 * <li>
 * <b>Void Return Type Required:</b> Methods annotated with {@code @Async} <b>MUST</b>
 * have a {@code void} return type. Because execution is offloaded asynchronously, the proxy
 * immediately returns {@code null} to the caller thread. Annotating non-void methods
 * will lead to unexpected {@code NullPointerException} occurrences at runtime.
 * </li>
 * <li>
 * <b>No-Argument Constructor Requirement:</b> Any managed class utilizing this annotation
 * must declare an accessible no-argument constructor (e.g., via Lombok's {@code @NoArgsConstructor}).
 * This allows ByteBuddy dynamic proxies to successfully instantiate subclass proxies.
 * </li>
 * <li>
 * <b>Thread Safety Notice:</b> Operations executing within an {@code @Async} context must
 * never directly invoke Bukkit World or Entity APIs that require main-thread synchronization.
 * Use {@link Sync} to safely transition back to the main thread when Bukkit API calls are necessary.
 * </li>
 * </ul>
 * * <h3>Usage Example</h3>
 * <pre>{@code
 * @Component
 * @NoArgsConstructor
 * public class DatabaseRepository {
 *
 *      @Async
 *      public void saveUserData(UUID uuid, String payload) {
 *          // Executed asynchronously in a background thread
 *          database.update(uuid, payload);
 *      }
 * }
 * }</pre>
 * * @author Lazberry (LRF Architecture Team)
 * @see Sync
 * @see Monitor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Async {
}
