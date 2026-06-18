package org.machanism.machai.ai.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as a tool function within the AI provider framework.
 *
 * <p>
 * Methods annotated with {@code @Tool} are recognized as callable tools, typically exposed
 * for dynamic invocation or registration in tool catalogs. The annotation provides metadata
 * such as the tool's name and description.
 * </p>
 *
 * <ul>
 *   <li><b>name</b>: Optional tool name. If not specified, {@link #NOT_DEFINED} is used as a placeholder.</li>
 *   <li><b>description</b>: Required description of the tool's purpose and functionality.</li>
 * </ul>
 *
 * <p>
 * The annotation is retained at runtime and applicable to methods only.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Tool {

    /**
     * Placeholder value indicating that the tool name is not defined.
     */
    String NOT_DEFINED = "___NOT_DEFINED___";

    /**
     * Optional tool name.
     * If not specified, {@link #NOT_DEFINED} is used.
     *
     * @return the name of the tool, or {@link #NOT_DEFINED} if not set
     */
    String name() default NOT_DEFINED;

    /**
     * Description of the tool's purpose and functionality.
     *
     * @return the tool description
     */
    String description();
}