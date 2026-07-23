package Framework.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enforces execution of the annotated method on the main Server Thread (Bukkit Primary Thread).
 * * <h3>Architectural Overview</h3>
 * <p>
 * This annotation serves as a thread context converter. When invoked from an asynchronous thread
 * (such as a database callback or an {@link Async} method), the LRF ByteBuddy interceptor
 * catches the invocation and schedules it to execute on the next server tick via the main Bukkit scheduler.
 * </p>
 * * <h3>Core Requirements &amp; Usage Constraints</h3>
 * <ul>
 * <li>
 * <b>Void Return Type Required:</b> Methods annotated with {@code @Sync} <b>MUST</b>
 * have a {@code void} return type when called from asynchronous contexts. Because thread
 * switching is deferred to the scheduler queue, the proxy immediately returns {@code null}.
 * </li>
 * <li>
 * <b>No-Argument Constructor Requirement:</b> Target classes using {@code @Sync} must expose
 * a accessible no-argument constructor to ensure dynamic proxy generation does not fail
 * during IoC container initialization.
 * </li>
 * <li>
 * <b>Pass-Through Optimization:</b> If the method is called while already running on the primary
 * server thread, LRF bypasses the scheduler and executes the method immediately in-place.
 * </li>
 * </ul>
 * * <h3>Usage Example</h3>
 * <pre>{@code
 * @Component
 * @NoArgsConstructor
 * public class UserDataNotifier {
 *
 *      @Sync
 *      public void notifyPlayer(UUID playerUuid, String message) {
 *          Safely thread-switched back to main Bukkit thread
 *          Player player = Bukkit.getPlayer(playerUuid);
 *
 *          if (player != null) {
 *              player.sendMessage(message);
 *          }
 *      }
 * }
 * }</pre>
 * * @author Lazberry (LRF Architecture Team)
 * @see Async
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Sync {
}
