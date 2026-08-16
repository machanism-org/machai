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
 * Contracts and runtime metadata for exposing Java capabilities as AI tools,
 * prompts, and resources.
 *
 * <p>Types in this package are deliberately provider-neutral. An application
 * declares capabilities on methods with runtime-retained annotations, while a
 * provider-specific integration discovers the annotations, describes the
 * callable operations, validates arguments, and invokes the methods.</p>
 *
 * <h2>Declaring capabilities</h2>
 * <ul>
 *   <li>{@link Tool} marks a method as an invokable tool and requires a human-readable
 *       description; {@link Tool#name()} optionally supplies its exposed name.</li>
 *   <li>{@link Prompt} marks a method as a prompt definition and associates it with a
 *       {@link Role}; its parameters may be described with {@link Param}.</li>
 *   <li>{@link Resource} marks a method that supplies content identified by one or more
 *       URIs, such as schemas, configuration, or instruction documents.</li>
 *   <li>{@link Param} supplies runtime parameter metadata, including a name, description,
 *       and optional default value.</li>
 * </ul>
 *
 * <h2>Provider integration</h2>
 * <p>{@link FunctionTools} is the service-provider interface used to group related
 * capabilities. {@link FunctionToolsLoader} discovers implementations with
 * {@link java.util.ServiceLoader}, optionally filters them with {@link SupportedFor},
 * and registers compatible implementations with a provider. {@link ToolFunction}
 * provides a functional abstraction for executing a tool with structured
 * {@link com.fasterxml.jackson.databind.JsonNode} parameters and runtime context.</p>
 *
 * <h2>Supporting types</h2>
 * <ul>
 *   <li>{@link ParamDescriptor} represents parameter metadata programmatically when an
 *       annotation is not sufficient.</li>
 *   <li>{@link Role} identifies the assistant and user conversation roles.</li>
 *   <li>{@link ErrorResultException} carries a structured, JSON-serializable tool error.</li>
 *   <li>{@link SpecialException} signals a framework-level special condition, such as the
 *       completion of a task without shutting down the host application.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * public final class ProjectTools implements FunctionTools {
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
 *         uri = {"file:///schemas/project-schema.json"},
 *         description = "Validation schema for project descriptors.",
 *         mimeType = "application/json"
 *     )
 *     public String getProjectSchema() {
 *         return loadSchemaFile();
 *     }
 * }
 * </pre>
 *
 * <p>Implementations should keep annotation descriptions stable and sufficiently
 * specific for both human readers and models. Discovery, serialization, validation,
 * and invocation policies remain the responsibility of the integrating provider.</p>
 */
package org.machanism.machai.ai.tools;
