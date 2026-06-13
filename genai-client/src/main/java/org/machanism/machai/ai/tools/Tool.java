package org.machanism.machai.ai.tools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Tool {

	String NULL_VALUE = "___NULL_SENTINEL___";

	String name() default NULL_VALUE;

	String description();

}
