package Framework.LazberryRegistryFramework;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({
		"Framework.LazberryRegistryFramework.Annotation.Component",
		"Framework.LazberryRegistryFramework.Annotation.Inject",
		"Framework.LazberryRegistryFramework.Annotation.Virtual",
		"Framework.LazberryRegistryFramework.Annotation.ConsumableClass",
		"Framework.LazberryRegistryFramework.Annotation.Listeners",
		"Framework.LazberryRegistryFramework.Annotation.Commands",
		"Framework.LazberryRegistryFramework.Annotation.ConfigObject",
		"Framework.LazberryRegistryFramework.Annotation.ConfigValue",
		"Framework.LazberryRegistryFramework.Annotation.GracefulShutdown",
		"Framework.LazberryRegistryFramework.Annotation.SelfDestruct",
		"Framework.LazberryRegistryFramework.Annotation.InboundChannel"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class LrfAnnotationProcessor extends AbstractProcessor {

	private Messager messager;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.messager = processingEnv.getMessager();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		getElements(roundEnv, "Framework.LazberryRegistryFramework.Annotation.Component")
				.forEach(e -> {
					validateComponent(e);
					validateInjectConstructors(e);
				});
		getElements(roundEnv, "Framework.LazberryRegistryFramework.Annotation.Virtual")
				.forEach(this::validateVirtual);
		getElements(roundEnv, "Framework.LazberryRegistryFramework.Annotation.ConsumableClass")
				.forEach(this::validateConsumable);
		getElements(roundEnv, "Framework.LazberryRegistryFramework.Annotation.Listeners")
				.forEach(this::validateListener);
		getElements(roundEnv, "Framework.LazberryRegistryFramework.Annotation.Commands")
				.forEach(this::validateCommand);
		getElements(roundEnv, "Framework.LazberryRegistryFramework.Annotation.ConfigObject")
				.forEach(this::validateConfigObject);
		getElements(roundEnv, "Framework.LazberryRegistryFramework.Annotation.ConfigValue")
			.forEach(this::validateConfigValue);
		getElements(roundEnv, "Framework.LazberryRegistryFramework.Annotation.GracefulShutdown")
				.forEach(e -> validateLifecycleMethod(e, "@GracefulShutdown"));
		getElements(roundEnv, "Framework.LazberryRegistryFramework.Annotation.SelfDestruct").stream()
				.filter(e -> ElementKind.METHOD.equals(e.getKind()))
				.forEach(e -> validateLifecycleMethod(e, "@SelfDestruct"));
		return true;
	}

	private void validateComponent(Element element) {
		if (element.getKind() == ElementKind.INTERFACE || element.getModifiers().contains(Modifier.ABSTRACT)) {
			error("@Component cannot be applied to an interface or abstract class.", element);
		}

		if (hasAnnotation(element, "Framework.LazberryRegistryFramework.Annotation.Virtual") ||
				hasAnnotation(element, "Framework.LazberryRegistryFramework.Annotation.ConsumableClass")) {
			error("@Component cannot be used together with @Virtual or @ConsumableClass.", element);
		}
	}

	private void validateVirtual(Element element) {
		boolean isAbstractOrInterface = element.getKind() == ElementKind.INTERFACE ||
				element.getModifiers().contains(Modifier.ABSTRACT);

		if (!isAbstractOrInterface) {
			error("@Virtual can only be applied to abstract classes or interfaces.", element);
		}

		checkNoInjectOrComponent(element, "@Virtual");
	}

	private void validateConsumable(Element element) {
		checkNoInjectOrComponent(element, "@ConsumableClass");
	}

	private void checkNoInjectOrComponent(Element element, String annotationName) {
		if (hasAnnotation(element, "Framework.LazberryRegistryFramework.Annotation.Component")) {
			error(annotationName + " classes cannot be annotated with @Component.", element);
		}

		for (Element enclosed : element.getEnclosedElements()) {
			if (enclosed.getKind() == ElementKind.CONSTRUCTOR &&
					hasAnnotation(enclosed, "Framework.LazberryRegistryFramework.Annotation.Inject")) {
				error(annotationName + " classes cannot have @Inject annotated constructors.", enclosed);
			}
		}
	}

	private void validateInjectConstructors(Element classElement) {
		List<? extends Element> constructors = classElement.getEnclosedElements().stream()
				.filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
				.filter(e -> hasAnnotation(e, "Framework.LazberryRegistryFramework.Annotation.Inject"))
				.toList();

		if (constructors.size() > 1)
			error("Class cannot have more than one @Inject annotated constructor.", classElement);

		for (Element constructor : constructors) {
			if (constructor.getModifiers().contains(Modifier.PRIVATE)) {
				error("@Inject constructor cannot be private. Use public or protected access modifier.", constructor);
			}
		}
	}

	private void validateConfigObject(Element element) {
		boolean isValidType = element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.RECORD;
		if (!isValidType || element.getModifiers().contains(Modifier.ABSTRACT)) {
			error("@ConfigObject can only be applied to concrete POJO classes or Records.", element);
		}
	}

	private void validateConfigValue(Element element) {
		if (element.getKind() == ElementKind.PARAMETER) {
			Element enclosingExecutable = element.getEnclosingElement();

			if (enclosingExecutable.getKind() == ElementKind.CONSTRUCTOR) {
				Element enclosingClass = enclosingExecutable.getEnclosingElement();
				boolean hasInject = hasAnnotation(enclosingExecutable, "Framework.LazberryRegistryFramework.Annotation.Inject");
				boolean isConfigObj = hasAnnotation(enclosingClass, "Framework.LazberryRegistryFramework.Annotation.ConfigObject");

				if (!hasInject && !isConfigObj) {
					error("@ConfigValue parameter must be inside an @Inject constructor or a @ConfigObject class.", element);
				}
			} else {
				error("@ConfigValue cannot be used on standard method parameters.", element);
			}
		} else if (element.getKind() != ElementKind.FIELD && element.getKind() != ElementKind.RECORD_COMPONENT) {
			error("@ConfigValue can only be applied to fields, record components, or constructor parameters.", element);
		}
	}

	private void validateLifecycleMethod(Element element, String annotationName) {
		if (element.getKind() == ElementKind.METHOD) {
			ExecutableElement method = (ExecutableElement) element;

			if (!method.getParameters().isEmpty()) {
				error(annotationName + " method must not have any parameters.", element);
			}

			if (method.getModifiers().contains(Modifier.STATIC)) {
				error(annotationName + " method cannot be static.", element);
			}
		}
	}

	private void validateListener(Element element) {
		checkAssignable(element, "org.bukkit.event.Listener", "@Listeners annotated class must implement org.bukkit.event.Listener.");
	}

	private void validateCommand(Element element) {
		checkAssignable(element, "org.bukkit.command.CommandExecutor", "@Commands annotated class must implement org.bukkit.command.CommandExecutor.");
	}

	private void checkAssignable(Element element, String targetInterfaceCanonicalName, String errorMessage) {
		TypeElement targetType = processingEnv.getElementUtils().getTypeElement(targetInterfaceCanonicalName);
		if (targetType != null) {
			TypeMirror targetMirror = targetType.asType();
			if (!processingEnv.getTypeUtils().isAssignable(element.asType(), targetMirror)) {
				error(errorMessage, element);
			}
		}
	}

	private Set<? extends Element> getElements(RoundEnvironment env, String className) {
		TypeElement typeElem = processingEnv.getElementUtils().getTypeElement(className);
		return typeElem != null ? env.getElementsAnnotatedWith(typeElem) : Set.of();
	}

	private boolean hasAnnotation(Element element, String annotationName) {
		return element.getAnnotationMirrors().stream()
				.anyMatch(m -> m.getAnnotationType().toString().equals(annotationName));
	}

	private void error(String msg, Element elem) {
		messager.printMessage(Diagnostic.Kind.ERROR, msg, elem);
	}
}