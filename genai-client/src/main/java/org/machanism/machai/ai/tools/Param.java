package org.machanism.machai.ai.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method parameter as a tool or prompt parameter within the AI provider framework.
 *
 * <p>
 * Parameters annotated with {@code @Param} are recognized as structured arguments for tool functions or prompts,
 * providing metadata such as the parameter's name, description, and default value.
 * </p>
 *
 * <ul>
 *   <li><b>name</b>: Optional parameter name. If not specified, {@link #NOT_DEFINED} is used as a placeholder.</li>
 *   <li><b>description</b>: Required description of the parameter's purpose and usage.</li>
 *   <li><b>defaultValue</b>: Optional default value for the parameter. If not specified, {@link #NOT_DEFINED} is used.</li>
 * </ul>
 *
 * <p>
 * The annotation is retained at runtime and applicable to method parameters only.
 * </p>
 *
 * <p>
 * The constant {@link #NULL} can be used to indicate a null value for the parameter.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {

    /**
     * Placeholder value indicating a null parameter value.
     */
    String NULL = "___NULL___";

    /**
     * Placeholder value indicating that the parameter name or default value is not defined.
     */
    String NOT_DEFINED = "___NOT_DEFINED___";

    /**
     * Optional parameter name.
     * If not specified, {@link #NOT_DEFINED} is used.
     *
     * @return the name of the parameter, or {@link #NOT_DEFINED} if not set
     */
    String name() default NOT_DEFINED;

    /**
     * Description of the parameter's purpose and usage.
     *
     * @return the parameter description
     */
    String description();

    /**
     * Optional default value for the parameter.
     * If not specified, {@link #NOT_DEFINED} is used.
     *
     * @return the default value of the parameter, or {@link #NOT_DEFINED} if not set
     */
    String defaultValue() default NOT_DEFINED;

}