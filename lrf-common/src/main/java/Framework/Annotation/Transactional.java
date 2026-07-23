package Framework.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enforces mutual exclusion and thread-safety for the annotated method.
 *
 * <h3>Architectural Overview</h3>
 * <p>
 * When applied to a method, the LRF dynamic proxy wraps the invocation within a concurrency lock
 * (specifically, a {@link java.util.concurrent.locks.ReentrantLock}). This guarantees that only
 * one thread may execute the method (or any other {@code @Transactional} method on the same component instance)
 * at any given time, preventing race conditions during asynchronous operations.
 * </p>
 *
 * <h3>Usage Context</h3>
 * <p>
 * Highly recommended for methods that mutate shared in-memory states, collections, or configuration
 * properties when interacting with {@link Async} tasks.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {

	/**
	 * Determines the fairness policy of the underlying lock.
	 * <p>
	 * If set to {@code true}, threads will acquire the lock in the order they requested it (FIFO).
	 * If {@code false}, barge-in is permitted, which generally offers higher throughput at the cost
	 * of potential thread starvation.
	 * </p>
	 *
	 * @return True if the lock should be fair, false otherwise. Defaults to false.
	 */
	boolean fair() default false;
}
