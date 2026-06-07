package org.machanism.machai.ai.tools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

	String description();

	String defaultValue() default "";

	String name();

}
