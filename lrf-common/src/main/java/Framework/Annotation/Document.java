package Framework.Annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>LRF Living Documentation Metadata</h2>
 * <p>
 * This annotation serves as a bridge between the source code and external architectural assets
 * (e.g., Notion specifications, GitHub Wiki, JIRA tickets, or internal engineering documents).
 * </p>
 * By embedding this directly onto structural components, developers can instantly trace the
 * business logic motivation or engineering rule sets directly from their IDE context.
 *
 * @author Lazberry (LRF Architecture Team)
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(value = RetentionPolicy.CLASS)
public @interface Document {
    @NotNull String description();
    @NotNull String url() default "";
}
