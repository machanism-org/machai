package org.machanism.machai.ai.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Resource {

	/**
	 * Placeholder value indicating that the prompt name is not defined.
	 */
	String NOT_DEFINED = "___NOT_DEFINED___";

	/**
	 * Optional prompt name. If not specified, {@link #NOT_DEFINED} is used.
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

	String uri();

	String mimeType() default NOT_DEFINED;

}