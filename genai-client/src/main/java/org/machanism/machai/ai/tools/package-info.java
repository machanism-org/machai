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
 * Defines the annotations, descriptors, and service-provider contracts used to
 * expose Java methods as AI-accessible tools and reusable prompt definitions.
 *
 * <p>
 * The package forms the metadata and discovery layer for Machai tool
 * integration. Methods annotated with {@link org.machanism.machai.ai.tools.Tool}
 * are treated as callable functions that may be registered with an AI provider,
 * while methods annotated with {@link org.machanism.machai.ai.tools.Prompt}
 * describe prompt content associated with a conversation
 * {@link org.machanism.machai.ai.tools.Role}. Method arguments can be documented
 * with {@link org.machanism.machai.ai.tools.Param}, and the same parameter
 * information can be represented as structured data with
 * {@link org.machanism.machai.ai.tools.ParamDescriptor}.
 * </p>
 *
 * <p>
 * Tool collections implement {@link org.machanism.machai.ai.tools.FunctionTools}
 * and are discovered by {@link org.machanism.machai.ai.tools.FunctionToolsLoader}
 * through Java's {@link java.util.ServiceLoader} mechanism. During application
 * startup or provider configuration, discovered implementations are filtered by
 * {@link org.machanism.machai.ai.tools.SupportedFor} when present and then
 * registered with {@link org.machanism.machai.ai.provider.Genai} as available
 * tools and prompts. Individual executable callbacks may also be represented by
 * {@link org.machanism.machai.ai.tools.ToolFunction}, which accepts JSON
 * parameters, project context, and runtime configuration.
 * </p>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * public final class ProjectTools implements FunctionTools {
 *     @Tool(description = "Reads project metadata.")
 *     public String readMetadata(@Param(description = "Metadata key to read.") String key) {
 *         return loadValue(key);
 *     }
 *
 *     @Prompt(description = "Creates a concise project summary.", role = Role.USER)
 *     public String summarizeProject() {
 *         return "Summarize the current project structure and notable files.";
 *     }
 * }
 * }</pre>
 *
 * <p>
 * Classes in this package are intentionally lightweight: annotations provide
 * runtime metadata, descriptors make parameter details available to provider
 * implementations, and loader utilities connect discovered tool providers to
 * the AI runtime without embedding tool-specific business logic in the provider
 * itself.
 * </p>
 */
package org.machanism.machai.ai.tools;
