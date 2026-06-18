package org.machanism.machai.ai.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as a prompt within the AI provider framework.
 *
 * <p>
 * Methods annotated with {@code @Prompt} are recognized as prompt definitions, typically used for
 * dynamic invocation, prompt cataloging, or role-based message handling. The annotation provides metadata
 * such as the prompt's name, description, and associated role.
 * </p>
 *
 * <ul>
 *   <li><b>name</b>: Optional prompt name. If not specified, {@link #NOT_DEFINED} is used as a placeholder.</li>
 *   <li><b>description</b>: Required description of the prompt's purpose and content.</li>
 *   <li><b>role</b>: The participant role associated with the prompt (default: {@link Role#ASSISTANT}).</li>
 * </ul>
 *
 * <p>
 * The annotation is retained at runtime and applicable to methods only.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Prompt {

    /**
     * Placeholder value indicating that the prompt name is not defined.
     */
    String NOT_DEFINED = "___NOT_DEFINED___";
    
    /**
     * Optional prompt name.
     * If not specified, {@link #NOT_DEFINED} is used.
     *
     * @return the name of the prompt, or {@link #NOT_DEFINED} if not set
     */
    String name() default NOT_DEFINED;

    /**
     * Description of the prompt's purpose and content.
     *
     * @return the prompt description
     */
    String description();

    /**
     * The participant role associated with the prompt.
     * Defaults to {@link Role#ASSISTANT}.
     *
     * @return the role for the prompt
     */
    Role role() default Role.ASSISTANT;

}