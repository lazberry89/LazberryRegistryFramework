package Framework.LazberryRegistryFramework;

import Framework.Annotation.*;
import Framework.ConditionalRegistry;
import Framework.LazberryRegistryFramework.Annotation.Commands;
import Framework.LazberryRegistryFramework.Annotation.ConfigObject;
import Framework.LazberryRegistryFramework.Annotation.Listeners;
import Framework.LazberryRegistryFramework.Annotation.Task;
import Framework.Utils.ServerUtils;
import com.google.common.reflect.ClassPath;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <h2>PackageScanner (Internal IoC Component Scanner & Filter Pipeline)</h2>
 * <p>
 * A package-private framework engine designed exclusively to execute classpath scanning procedures.
 * It traverses the designated root package structure using Guava's {@link ClassPath} utilities to
 * discover, validate, and isolate eligible bean candidates for the IoC lifecycle.
 * </p>
 * <h3>Architectural Boundary & Performance Note:</h3>
 * <p>
 * Complete classpath metadata evaluation is a computationally heavy operation involving dynamic disk I/O
 * and target class-loading. Consequently, this class is locked strictly to <b>package-private access</b>
 * and must only be invoked exactly once during the server boot sequence via {@link Reflections}.
 * Under no circumstances should this scanner be referenced from external runtime business logic.
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @see Reflections
 * @see DependencyContainer
 * @see Framework.ConditionalRegistry
 */
@Slf4j
final class PackageScanner {
    private static final @NotNull String icon = LazberryRegistryFramework.icon();

	/**
	 * Executes the core component scanning loop, filtering out invalid or incompatible structures, 
	 * evaluating conditional predicates, and orchestrating target class initialization loops.
	 * <p>
	 * <b>Multi-Stage Filtering Pipeline Mechanics:</b>
	 * <ol>
	 * <li><b>Structural Sanity:</b> Discards pure Java interfaces and abstract class blueprints.</li>
	 * <li><b>Consumption Guard:</b> Skips classes tagged with {@link ConsumableClass}, identifying them as reusable templates.</li>
	 * <li><b>Server Environment Check:</b> Invokes {@link ServerUtils#unCompatibleWithCurrentServer(Class)} to drop modules incompatible with the current active NMS/Spigot version.</li>
	 * <li><b>Annotation Matching:</b> Collects classes possessing target LRF anchors ({@code @Component.Include}, {@code @Commands}, {@code @Listeners}, etc.) or constructors marked with {@link Inject}.</li>
	 * <li><b>Conditional Evaluation:</b> Instantiates localized {@link ConditionalRegistry} matchers if {@link Conditional} is present. The class is permanently dropped from the boot cycle if {@code condition.matches()} yields false.</li>
	 * </ol>
	 * </p>
	 *
	 * @param classPath Guava's analyzed classpath metadata containing current classloader binary streams.
	 * @see DependencyContainer#getOrCreateBean(Class)
	 * @see StructuralLog#logAssemblyFailure(Set, Class, Throwable)
	 */
    static void buildAndInjectBeans(@NotNull ClassPath classPath) {
        log.info("{} Building Bean Container and resolving constructor dependencies...", icon);

        List<Class<?>> targetClasses = new ArrayList<>();
        for (var classInfo : classPath.getTopLevelClassesRecursive(LazberryRegistryFramework.rootPackage())) {
            try {
                Class<?> clazz = classInfo.load();
                if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) continue;

                if (clazz.isAnnotationPresent(ConsumableClass.class)) {
                    if (LazberryRegistryFramework.isDebug()) log.debug("{} Skipping @ConsumableClass: {}", icon, clazz.getSimpleName());
                    continue;
                }

				if (clazz.isAnnotationPresent(Virtual.class)) {
					if (LazberryRegistryFramework.isDebug()) log.debug("{} Skipping @Virtual component: {}", icon, clazz.getSimpleName());
					continue;
				}

                if (ServerUtils.unCompatibleWithCurrentServer(clazz)) {
                    if (LazberryRegistryFramework.isDebug()) log.debug("{} Skipping incompatible class: {}", icon, clazz.getSimpleName());
                    continue;
                }

                boolean hasInjectConstructor = false;
                for (var constructor : clazz.getDeclaredConstructors()) {
                    if (constructor.isAnnotationPresent(Inject.class)) {
                        hasInjectConstructor = true;
                        break;
                    }
                }

                if (clazz.isAnnotationPresent(Component.Include.class) ||
                        clazz.isAnnotationPresent(Component.Exclude.class) ||
                        clazz.isAnnotationPresent(Commands.class) ||
                        clazz.isAnnotationPresent(Listeners.class) ||
                        clazz.isAnnotationPresent(Task.class) ||
                        hasInjectConstructor) {

                    if (clazz.isAnnotationPresent(Conditional.class)) {
                        var conditionalAnno = clazz.getAnnotation(Conditional.class);
                        try {
                            ConditionalRegistry condition = conditionalAnno.value().getDeclaredConstructor().newInstance();

                            if (!condition.matches()) {
                                if (LazberryRegistryFramework.isDebug()) log.info("{} Skipping disabled component: {}", icon, clazz.getSimpleName());
                                continue;
                            }
                        } catch (Exception e) {
                            log.error("{} Failed to evaluate condition for {}", icon, clazz.getSimpleName(), e);
                            continue;
                        }
                    }

                    targetClasses.add(clazz);
                }

	            if (clazz.isAnnotationPresent(ConfigObject.class)) {
		            Object configBean = ConfigObjectMapper.mapConfigObject(clazz);
		            DependencyContainer.registerInstance(clazz, configBean);

		            log.info("{} Registered @ConfigObject bean: {}", LazberryRegistryFramework.icon(), clazz.getSimpleName());
	            }
            } catch (Exception e) {
                if (LazberryRegistryFramework.isDebug()) log.warn("{} Failed to load class for scanning: {}", icon, classInfo.getName());
            }
        }

        for (Class<?> targetClass : targetClasses) {
            try {
                DependencyContainer.getOrCreateBean(targetClass);
            } catch (Exception e) {
                StructuralLog.logAssemblyFailure(DependencyContainer.constructionStack(), targetClass, e);
                log.error("{} Error StackTrace:", icon, e);
            }
        }
    }

	/**
	 * Fallback lookup utility reserved exclusively for testing vectors or legacy contextual resolution.
	 * Searches classpath to pair interface requests with practical concrete implementation variants.
	 *
	 * @param interfaceType Abstract target type or contract interface requiring an implementation match.
	 * @return The first conforming concrete class token detected; or {@code null} if resolution bounds fail.
	 * @deprecated Legacy interface-matching loop slated for removal in upcoming versions.
	 * Use specialized declarative explicit mapping configurations instead.
	 */
    @TestOnly
    @Deprecated(since = "1.21.11", forRemoval = true)
    private static Class<?> findImplementation(Class<?> interfaceType) {
        try {
            ClassPath classPath = ClassPath.from(PackageScanner.class.getClassLoader());
            for (var classInfo : classPath.getTopLevelClassesRecursive(LazberryRegistryFramework.rootPackage())) {
                Class<?> candidate = classInfo.load();
                if (candidate.isInterface() || Modifier.isAbstract(candidate.getModifiers())) continue;
                if (candidate.isAnnotationPresent(ConsumableClass.class)) {
                    continue;
                }
                if (interfaceType.isAssignableFrom(candidate)) {
                    if (candidate.isAnnotationPresent(Component.Include.class) ||
                            candidate.isAnnotationPresent(Component.Exclude.class) ||
                            candidate.isAnnotationPresent(Commands.class)) {
                        return candidate;
                    }
                }
            }
        } catch (Exception e) {
            log.error("{} Error occurred while finding implementation for {}", icon, interfaceType.getSimpleName(), e);
        }
        return null;
    }
}