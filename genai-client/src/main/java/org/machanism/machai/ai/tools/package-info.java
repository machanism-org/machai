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
 * Defines the annotation model and runtime contracts used to expose Java methods
 * and service-provider implementations as AI-accessible tools and prompts.
 *
 * <p>
 * This package provides lightweight metadata annotations for describing callable
 * capabilities, including {@link org.machanism.machai.ai.tools.Tool tool methods},
 * {@link org.machanism.machai.ai.tools.Prompt prompts}, and their
 * {@link org.machanism.machai.ai.tools.Param parameters}. The metadata is retained
 * at runtime so provider implementations can discover annotated methods, build
 * tool or prompt descriptors, validate invocation arguments, and present clear
 * descriptions to an AI model or orchestration layer.
 * </p>
 *
 * <p>
 * Tool installation is centered on the {@link org.machanism.machai.ai.tools.FunctionTools}
 * service-provider interface and {@link org.machanism.machai.ai.tools.FunctionToolsLoader}.
 * Implementations can be discovered through Java's {@link java.util.ServiceLoader}
 * mechanism and registered with a provider. The optional
 * {@link org.machanism.machai.ai.tools.SupportedFor} annotation limits a tool set to
 * specific application classes when a capability should only be available in selected
 * runtime contexts.
 * </p>
 *
 * <p>
 * Direct executable tool callbacks can be represented by
 * {@link org.machanism.machai.ai.tools.ToolFunction}, which receives structured JSON
 * invocation parameters, the current project directory, and configuration data. Parameter
 * metadata may also be represented programmatically with
 * {@link org.machanism.machai.ai.tools.ParamDescriptor}. Conversation prompt roles are
 * modeled by {@link org.machanism.machai.ai.tools.Role}, and
 * {@link org.machanism.machai.ai.tools.SpecialException} is available for framework-level
 * control flow that should end a task without stopping the hosting application.
 * </p>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * public final class ProjectTools implements FunctionTools {
 *     @Tool(description = "Reads a project resource by relative path.")
 *     public String readResource(
 *             @Param(description = "Path relative to the project root.") String path) {
 *         return "resource content";
 *     }
 *
 *     @Prompt(description = "Creates a short project summary.", role = Role.ASSISTANT)
 *     public String summarizeProject() {
 *         return "Summarize the current project structure and key files.";
 *     }
 * }
 * }</pre>
 *
 * <p>
 * Classes in this package are intentionally small and framework-oriented. They define
 * stable contracts and descriptive metadata while leaving provider-specific discovery,
 * serialization, validation, and invocation behavior to higher-level components.
 * </p>
 */
package org.machanism.machai.ai.tools;
