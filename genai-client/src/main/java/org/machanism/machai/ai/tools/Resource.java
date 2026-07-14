package org.machanism.machai.ai.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to declare a method as an executable resource provider 
 * within the generative-AI tools system.
 * <p>
 * Resource-annotated methods return raw configurations, schemas, or instructional assets
 * (such as system guidelines or validation layouts) mapped to one or more URIs. 
 * The generative-AI provider uses these declarations to dynamically load context documents 
 * or schemas during generation processes.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * @Resource(
 *     uri = "file:///schemas/my-schema.json",
 *     description = "Validation schema for application descriptors.",
 *     mimeType = "application/json"
 * )
 * public String getMySchema() {
 *     return loadSchemaFile();
 * }
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Resource {

	/**
	 * Placeholder value indicating that a property is not defined.
	 */
	String NOT_DEFINED = "___NOT_DEFINED___";

	/**
	 * Description of the resource's purpose, structure, or content.
	 * <p>
	 * This metadata is shared with the generative-AI provider to help determine 
	 * when and why this resource should be loaded.
	 * </p>
	 *
	 * @return the resource description
	 */
	String description();

	/**
	 * One or more unique URIs associated with this resource.
	 * <p>
	 * The URIs serve as the primary identifiers (e.g., <code>"file:///schemas/bindex.json"</code>)
	 * used by generative models to request and locate the resource content.
	 * </p>
	 *
	 * @return an array of URIs mapping to this resource
	 */
	String[] uri();

	/**
	 * The MIME type of the resource content.
	 * <p>
	 * Defaults to {@link #NOT_DEFINED}. Providing a specific MIME type (such as 
	 * {@code "application/json"} or {@code "text/markdown"}) helps the AI provider 
	 * process and parse the returning payload correctly.
	 * </p>
	 *
	 * @return the MIME type of the resource, or {@link #NOT_DEFINED} if unspecified
	 */
	String mimeType() default NOT_DEFINED;

}