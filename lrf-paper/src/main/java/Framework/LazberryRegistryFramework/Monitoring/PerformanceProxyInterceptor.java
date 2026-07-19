package Framework.LazberryRegistryFramework.Monitoring;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Deprecated(forRemoval = true)
public final class PerformanceProxyInterceptor implements InvocationHandler {
	private final Object targetInstance;

	public PerformanceProxyInterceptor(Object targetInstance) {
		this.targetInstance = targetInstance;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Method concreteMethod = targetInstance.getClass().getMethod(method.getName(), method.getParameterTypes());
		return TelemetryInterceptor.execute(targetInstance, concreteMethod, args);
	}
}