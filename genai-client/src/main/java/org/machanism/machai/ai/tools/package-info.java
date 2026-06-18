/*-
 * @guidance:
 * **Task:**  
 * Scan the current folder and add comprehensive Javadoc comments to all Java classes and to the `package-info.java` file within this package.

 * **Instructions:**  
 * - For each Java class in this folder, generate and insert detailed Javadoc comments describing the class, its purpose, and its public methods and fields.
 * - If a `package-info.java` file exists, add or update its Javadoc to provide an overview of the package, its responsibilities, and any important usage notes.
 * - Ensure all Javadoc follows standard Java documentation conventions and is clear, concise, and informative.
 * - Do not modify any code logic—only add or improve Javadoc comments.
 * 
 * Would you like this prompt tailored for a specific LLM or code review tool? * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 *     - Do not use escaping in `{@code ...}` tags.   
 *     - When showing `${...}` variable placeholders, do not use escaping or wrap them in `{@code ...}`.
 */

/**
 * Provides annotations, descriptors, and service-provider contracts for exposing
 * application-defined functions and prompts to AI providers.
 *
 * <p>
 * This package defines the metadata model used by the Machai tool integration
 * layer. Methods annotated with {@link org.machanism.machai.ai.tools.Tool} can
 * be registered as callable tool functions, while methods annotated with
 * {@link org.machanism.machai.ai.tools.Prompt} can be registered as reusable
 * prompt definitions. Method parameters can be described with
 * {@link org.machanism.machai.ai.tools.Param}, and parameter metadata may be
 * represented programmatically by {@link org.machanism.machai.ai.tools.ParamDescriptor}.
 * </p>
 *
 * <p>
 * Tool providers implement {@link org.machanism.machai.ai.tools.FunctionTools}
 * and are discovered by {@link org.machanism.machai.ai.tools.FunctionToolsLoader}
 * through Java's {@link java.util.ServiceLoader}. Implementations may be limited
 * to specific application types with {@link org.machanism.machai.ai.tools.SupportedFor};
 * otherwise, they are treated as generally applicable. The loader registers
 * compatible tool and prompt definitions with a provider such as
 * {@link org.machanism.machai.ai.provider.Genai}.
 * </p>
 *
 * <p>
 * The package also includes {@link org.machanism.machai.ai.tools.ToolFunction},
 * a functional interface for executable tool callbacks, and
 * {@link org.machanism.machai.ai.tools.Role}, which identifies whether prompt
 * content is associated with the assistant or user side of an interaction.
 * </p>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * public final class ProjectTools implements FunctionTools {
 *     @Tool(description = "Reads project metadata.")
 *     public String readMetadata(@Param(description = "Metadata key to read.") String key) {
 *         return loadValue(key);
 *     }
 * }
 * }</pre>
 *
 * <p>
 * Classes in this package are intended to be lightweight integration primitives:
 * annotations provide runtime metadata, descriptors provide structured parameter
 * information, and loader utilities connect discovered implementations to the AI
 * provider runtime.
 * </p>
 */
package org.machanism.machai.ai.tools;
