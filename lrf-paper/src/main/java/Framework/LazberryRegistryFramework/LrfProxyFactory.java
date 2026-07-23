package Framework.LazberryRegistryFramework;

import Framework.Annotation.Async;
import Framework.Annotation.Sync;
import Framework.Annotation.Transactional;
import Framework.LazberryRegistryFramework.Annotation.Monitor;
import Framework.LazberryRegistryFramework.Monitoring.PerformanceRegistry;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Master AOP (Aspect-Oriented Programming) Dynamic Proxy Engine for the Lazberry Registry Framework.
 *
 * <h3>Architectural Overview</h3>
 * <p>
 * {@code LrfProxyFactory} generates runtime subclasses for managed components using ByteBuddy.
 * Intercepted method invocations are processed through {@link LrfInterceptor} to transparently handle
 * thread context switches ({@link Async}, {@link Sync}), transactional synchronization ({@link Transactional}),
 * and real-time performance profiling ({@link Monitor}).
 * </p>
 *
 * <h3>Core Requirements &amp; Limitations</h3>
 * <ul>
 * <li>
 * <b>Mandatory No-Argument Constructor:</b> Every class target passed to {@link #createProxy(Class, Object)}
 * <b>MUST</b> expose a public or package-private no-argument constructor (e.g., via Lombok's {@code @NoArgsConstructor}).
 * Failing to declare a default constructor will cause reflection instantiation errors during proxy creation.
 * </li>
 * <li>
 * <b>Void Return Type Requirement for Async/Sync:</b> Methods decorated with {@link Async} or {@link Sync}
 * <b>MUST</b> return {@code void}. Because thread transitions defer execution to Bukkit scheduler queues,
 * the proxy interceptor returns {@code null} immediately upon interception.
 * </li>
 * <li>
 * <b>Fallback Guarantee:</b> If ByteBuddy fails to synthesize or load a proxy subclass,
 * the factory logs an error and returns the original raw instance to preserve component availability.
 * </li>
 * </ul>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see Async
 * @see Sync
 * @see Transactional
 * @see Monitor
 * @see PerformanceRegistry
 */
@Slf4j
public final class LrfProxyFactory {
	private static final @NotNull String icon = LazberryRegistryFramework.icon();
	private static final @NotNull JavaPlugin plugin = LazberryRegistryFramework.plugin();

	/**
	 * Synthesizes a ByteBuddy dynamic proxy wrapping the target bean instance.
	 *
	 * @param clazz    The concrete class token of the component to proxy.
	 * @param instance The raw, instantiated bean instance managed by the Dependency Container.
	 * @param <T>      The component type generic.
	 * @return An AOP-enhanced subclass proxy instance; or the original instance if synthesis fails.
	 */
	public static <T> T createProxy(Class<T> clazz, Object instance) {
		try {
			Class<? extends T> proxyClass = new ByteBuddy()
					.subclass(clazz)
					.method(ElementMatchers.any())
					.intercept(MethodDelegation.to(new LrfInterceptor(instance)))
					.make()
					.load(clazz.getClassLoader())
					.getLoaded();

			return proxyClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			log.error("{} Failed to create proxy for: {}", icon, clazz.getSimpleName(), e);
			return (T) instance;
		}
	}

	/**
	 * Runtime method interception handler responsible for executing cross-cutting concerns.
	 */
	public static class LrfInterceptor {
		private final Object target;

		/**
		 * Constructs an interceptor instance bound to the raw concrete component target.
		 *
		 * @param target The original underlying bean instance.
		 */
		public LrfInterceptor(Object target) {
			this.target = target;
		}

		/**
		 * Intercepts method invocations on the proxy instance, delegating execution based on
		 * LRF annotation metadata.
		 *
		 * @param method    The reflected method being invoked.
		 * @param args      The arguments passed to the method.
		 * @param superCall Callable handle to the superclass method.
		 * @return The execution result of the target method, or {@code null} if deferred asynchronously.
		 * @throws Exception If an error occurs during method reflection or execution.
		 */
		@RuntimeType
		public @Nullable Object intercept(@Origin Method method,
		                                  @AllArguments Object[] args,
		                                  @SuperCall Callable<?> superCall) throws Exception {

			boolean isAsync = method.isAnnotationPresent(Async.class);
			boolean isSync = method.isAnnotationPresent(Sync.class);

			if (isAsync && Bukkit.isPrimaryThread()) {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
					try {
						executeWithTransactionAndTelemetry(method, args, target);
					} catch (Exception e) {
						log.error("{} Execution failed in method {}", icon, method.getName(), e);
					}
				});
				return null;
			}

			if (isSync && !Bukkit.isPrimaryThread()) {
				Bukkit.getScheduler().runTask(plugin, () -> {
					try {
						executeWithTransactionAndTelemetry(method, args, target);
					} catch (Exception e) {
						log.error("{} Execution failed in method {}", icon, method.getName(), e);
					}
				});
				return null;
			}

			return executeWithTransactionAndTelemetry(method, args, target);
		}

		/**
		 * Primary execution wrapper that evaluates and acquires concurrency locks
		 * prior to delegating to the telemetry phase.
		 */
		private Object executeWithTransactionAndTelemetry(Method method, Object[] args, Object target) throws Exception {
			Transactional transactional = method.getAnnotation(Transactional.class);

			if (transactional == null) return executeWithTelemetry(method, args, target);

			ReentrantLock lock = TransactionLockRegistry.getLock(target, transactional.fair());
			lock.lock();
			try {
				return executeWithTelemetry(method, args, target);
			} finally {
				lock.unlock();
			}
		}

		/**
		 * Executes the target method while measuring high-precision nanosecond latency
		 * if a {@link Monitor} annotation is present.
		 */
		private Object executeWithTelemetry(Method method, Object[] args, Object target) throws Exception {
			Monitor monitor = method.getAnnotation(Monitor.class);
			if (monitor == null) return method.invoke(target, args);

			long threshold = monitor.lvl().getThresholdNanos();
			long startTime = System.nanoTime();

			try {
				return method.invoke(target, args);
			} finally {
				long elapsed = System.nanoTime() - startTime;

				String className = target.getClass().getSimpleName();
				if (className.contains("ByteBuddy")) className = target.getClass().getSuperclass().getSimpleName();

				PerformanceRegistry.submit(className, method.getName(), elapsed, threshold);

				if (elapsed > threshold) {
					triggerSpikeWarning(className, method.getName(), elapsed, threshold);
				}
			}
		}

		/**
		 * Logs a performance spike warning when execution time exceeds the configured threshold.
		 */
		private void triggerSpikeWarning(String clazz, String method, long elapsed, long limit) {
			log.error("{} Performance Spike Detected in {}#{}()", icon, clazz, method);
			log.error("              └─ Execution Time : {} ns", elapsed);
			log.error("              └─ Allowed Limit  : {} ns", limit);
		}
	}
}