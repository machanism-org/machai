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
 *      - Generate comprehensive package-level Javadoc that clearly describes the package's overall purpose and usage.
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
 * and service-provider implementations as AI-accessible tools, prompts, and resources.
 *
 * <p>
 * This package provides lightweight metadata annotations for describing callable
 * capabilities, including {@link org.machanism.machai.ai.tools.Tool tool methods},
 * {@link org.machanism.machai.ai.tools.Prompt prompts},
 * {@link org.machanism.machai.ai.tools.Resource resources}, and their
 * {@link org.machanism.machai.ai.tools.Param parameters}. The metadata is retained
 * at runtime so provider implementations can discover annotated methods, build
 * tool, prompt, or resource descriptors, validate invocation arguments, and present
 * clear descriptions to an AI model or orchestration layer.
 * </p>
 *
 * <h2>Annotations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.tools.Tool} &ndash; Marks a method as a callable
 *       tool function, exposing it for dynamic invocation and registration in tool catalogs.
 *       Accepts an optional name and a required description.</li>
 *   <li>{@link org.machanism.machai.ai.tools.Prompt} &ndash; Marks a method as a prompt
 *       definition associated with a {@link org.machanism.machai.ai.tools.Role}. Supports
 *       optional naming and a required description.</li>
 *   <li>{@link org.machanism.machai.ai.tools.Resource} &ndash; Marks a method as a resource
 *       provider that maps configuration schemas, system guidelines, or instructional assets to
 *       one or more URIs, allowing the AI provider to dynamically load context documents.</li>
 *   <li>{@link org.machanism.machai.ai.tools.Param} &ndash; Annotates individual method
 *       parameters of tools and prompts with a name, description, and optional default value.</li>
 *   <li>{@link org.machanism.machai.ai.tools.SupportedFor} &ndash; Restricts a
 *       {@link org.machanism.machai.ai.tools.FunctionTools} implementation to a specific set
 *       of application classes, allowing selective tool registration per runtime context.</li>
 * </ul>
 *
 * <h2>Core Types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.tools.FunctionTools} &ndash; Service-provider interface
 *       (SPI) that implementations use to bundle related tools, prompts, and resources for
 *       installation into an AI provider.</li>
 *   <li>{@link org.machanism.machai.ai.tools.FunctionToolsLoader} &ndash; Discovers
 *       {@link org.machanism.machai.ai.tools.FunctionTools} implementations via
 *       {@link java.util.ServiceLoader} and applies them to a provider, filtered by application
 *       class compatibility.</li>
 *   <li>{@link org.machanism.machai.ai.tools.ToolFunction} &ndash; Functional interface that
 *       encapsulates the execution logic of a single tool, accepting structured JSON parameters
 *       and variable runtime context objects (working directory, configurator).</li>
 *   <li>{@link org.machanism.machai.ai.tools.ParamDescriptor} &ndash; Programmatic descriptor
 *       for a parameter, capturing its name, data type, required flag, description, and default
 *       value. Used when parameter metadata cannot be expressed via the {@code @Param}
 *       annotation alone.</li>
 *   <li>{@link org.machanism.machai.ai.tools.Role} &ndash; Enum that models the conversation
 *       roles ({@code ASSISTANT} and {@code USER}) used in prompt definitions.</li>
 *   <li>{@link org.machanism.machai.ai.tools.SpecialException} &ndash; Runtime exception for
 *       framework-level control flow that signals the end of a task without terminating the
 *       hosting application.</li>
 * </ul>
 *
 * <h2>Tool Discovery and Registration</h2>
 * <p>
 * Tool installation is centered on the {@link org.machanism.machai.ai.tools.FunctionTools}
 * SPI and {@link org.machanism.machai.ai.tools.FunctionToolsLoader}.
 * Implementations are discovered through Java's {@link java.util.ServiceLoader}
 * mechanism (via {@code META-INF/services} provider configuration) and registered with a
 * provider. The optional {@link org.machanism.machai.ai.tools.SupportedFor} annotation limits
 * a tool set to specific application classes when a capability should only be available in
 * selected runtime contexts.
 * </p>
 *
 * <h2>Typical Usage</h2>
 * <pre>
 * public final class ProjectTools implements FunctionTools {
 *
 *     @Tool(description = "Reads a project resource by relative path.")
 *     public String readResource(
 *             @Param(description = "Path relative to the project root.") String path) {
 *         return "resource content";
 *     }
 *
 *     @Prompt(description = "Creates a short project summary.", role = Role.USER)
 *     public String summarizeProject() {
 *         return "Summarize the current project structure and key files.";
 *     }
 *
 *     @Resource(
 *         uri = "file:///schemas/project-schema.json",
 *         description = "Validation schema for project descriptors.",
 *         mimeType = "application/json"
 *     )
 *     public String getProjectSchema() {
 *         return loadSchemaFile();
 *     }
 * }
 * </pre>
 *
 * <p>
 * Classes in this package are intentionally small and framework-oriented. They define
 * stable contracts and descriptive metadata while leaving provider-specific discovery,
 * serialization, validation, and invocation behavior to higher-level components.
 * </p>
 */
package org.machanism.machai.ai.tools;
