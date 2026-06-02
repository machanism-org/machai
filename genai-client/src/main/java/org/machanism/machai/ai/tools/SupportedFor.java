package org.machanism.machai.ai.tools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SupportedFor {

	Class<?>[] value();

}
