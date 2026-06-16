package org.machanism.machai.ai.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {

	String NULL = "___NULL___";

	String NOT_DEFINED = "___NOT_DEFINED___";

	String name() default NOT_DEFINED;

	String description();

	String defaultValue() default NOT_DEFINED;

}
