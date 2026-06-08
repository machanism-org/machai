package org.machanism.machai.ai.tools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
	
	String NULL_VALUE = "___NULL_SENTINEL___";

	String name();

	String description();

	String defaultValue() default NULL_VALUE;

}
