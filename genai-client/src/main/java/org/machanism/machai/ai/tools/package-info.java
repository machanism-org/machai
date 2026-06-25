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
 * Defines the annotation model, descriptors, loading utilities, and execution contract used to expose
 * Java methods as AI-accessible tools and prompts.
 *
 * <p>
 * This package provides lightweight metadata annotations such as {@link org.machanism.machai.ai.tools.Tool},
 * {@link org.machanism.machai.ai.tools.Prompt}, and {@link org.machanism.machai.ai.tools.Param}. These annotations
 * describe callable methods, prompt-producing methods, and their parameters so provider integrations can discover
 * and register them at runtime.
 * </p>
 *
 * <p>
 * Tool collections implement {@link org.machanism.machai.ai.tools.FunctionTools}. Implementations are discovered by
 * {@link org.machanism.machai.ai.tools.FunctionToolsLoader} through Java's {@link java.util.ServiceLoader} mechanism
 * and are then registered with an AI provider. The optional {@link org.machanism.machai.ai.tools.SupportedFor}
 * annotation limits a tool collection to specific application classes when a single runtime contains multiple
 * applications or processors.
 * </p>
 *
 * <p>
 * Runtime tool execution is represented by {@link org.machanism.machai.ai.tools.ToolFunction}, which accepts structured
 * JSON parameters, the provider working directory, and configuration. Supporting types such as
 * {@link org.machanism.machai.ai.tools.ParamDescriptor}, {@link org.machanism.machai.ai.tools.Role}, and
 * {@link org.machanism.machai.ai.tools.SpecialException} provide parameter metadata, prompt role information, and
 * controlled task-flow signaling.
 * </p>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * public final class ProjectTools implements FunctionTools {
 *
 *     @Tool(description = "Reads a project-relative file.")
 *     public String readFile(@Param(description = "Path relative to the project root.") String path) {
 *         // Tool implementation
 *         return "...";
 *     }
 *
 *     @Prompt(description = "Creates a concise project summary.", role = Role.USER)
 *     public String summarizeProject() {
 *         return "Summarize this project for a new contributor.";
 *     }
 * }
 * }</pre>
 *
 * <p>
 * Consumers should keep annotated tool methods deterministic, clearly documented, and explicit about parameters so
 * generated tool schemas and prompt catalogs remain understandable to both providers and users.
 * </p>
 */
package org.machanism.machai.ai.tools;
