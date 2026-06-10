package org.machanism.machai.ai.tools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates which application classes a {@link FunctionTools} implementation supports.
 * <p>
 * Annotate a {@link FunctionTools} class with {@code @SupportedFor} to specify the set of application classes
 * (such as processors or providers) for which the tool is compatible. If the annotation is absent,
 * the tool is considered compatible with all application classes.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * @SupportedFor({ MyProcessor.class, AnotherProcessor.class })
 * public class MyFunctionTools implements FunctionTools {
 *     // ...
 * }
 * }</pre>
 *
 * @since 1.2.0
 * @author Viktor Tovstyi
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportedFor {

	/**
	 * The set of application classes for which the annotated {@link FunctionTools} implementation is compatible.
	 *
	 * @return array of supported application classes
	 */
	Class<?>[] value();

}