package Framework.Annotation;

import Framework.ConditionalRegistry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>Conditional</h2>
 * <p>
 * Indicates that a component is candidate for IoC container registration only if all specified
 * {@link ConditionalRegistry} evaluation strategies match the current runtime environment state.
 * </p>
 * <h3>Architectural Value & Dynamic Filtering Mechanics</h3>
 * <p>
 * This annotation provides an elegant, non-intrusive declarative framework abstraction layer that
 * drastically boosts application flexibility and modular design:
 * <ul>
 * <li><b>Decoupled Environment Routing:</b> Instead of hardcoding continuous {@code if-else} environment checks
 * inside the component's internal initialization logic, developers can isolate environment evaluation into dedicated,
 * reusable {@link ConditionalRegistry} implementations.</li>
 * <li><b>Zero-Footprint Optimization:</b> During the classpath scanning phase, before allocating any metadata mirrors
 * or triggering constructor analysis inside {@code DependencyContainer}, the assembly engine dynamically instantiates
 * the designated condition class and invokes its matching routine. If it returns {@code false}, the component is instantly
 * dropped from the compilation queue, guaranteeing no memory footprint or reference allocations.</li>
 * <li><b>Feature Toggling & Microservices Adaptability:</b> Enables structural multi-module plugins to scale dynamically
 * across variable game server types (e.g., Minigames vs. Lobby systems) by activating proprietary services only when explicit
 * environment features are signaled.</li>
 * </ul>
 * </p>
 *
 * @author Lazberry (LRF Architecture Team)
 * @version 1.1.0
 * @see Framework.ConditionalRegistry
 * @see Framework.Annotation.Registry.Include
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Conditional {

	/**
	 * Designates the concrete {@link ConditionalRegistry} implementation class utilized to evaluate
	 * registration eligibility for the decorated component.
	 * <p>
	 * The evaluation engine leverages reflection to materialize this evaluator strategy, demanding
	 * that it possesses an accessible default zero-argument constructor to support programmatic invocation.
	 * </p>
	 *
	 * @return The token of the evaluation strategy class expanding over the conditional registration contract.
	 */
    Class<? extends ConditionalRegistry> value();
}
