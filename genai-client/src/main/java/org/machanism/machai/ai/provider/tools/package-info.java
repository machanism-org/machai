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
 * Provides an AI provider implementation that exposes registered application tools for
 * structured invocation.
 *
 * <p>
 * This package contains the tools-oriented provider used by the MachAI provider layer to
 * collect prompts, register {@link org.machanism.machai.ai.tools.ToolFunction} instances,
 * and execute those functions from structured request data. The primary implementation,
 * {@link org.machanism.machai.ai.provider.tools.ToolsProvider}, extends the common provider
 * abstraction and supports YAML-based tool call descriptions that identify a tool name and
 * supply the parameters passed to the selected tool.
 * </p>
 *
 * <p>
 * Classes in this package are intended for internal provider orchestration where host-defined
 * tool functions need to be made available through the same provider lifecycle used by other
 * AI integrations. A typical YAML tool call contains a {@code tool} entry naming the registered
 * function and a {@code params} entry containing the function arguments.
 * </p>
 *
 * <p>
 * Example YAML request consumed by the provider:
 * </p>
 *
 * <pre>
 * tool: exampleTool
 * params:
 *   name: sample
 *   enabled: true
 * </pre>
 *
 * @see org.machanism.machai.ai.provider.tools.ToolsProvider
 * @see org.machanism.machai.ai.tools.ToolFunction
 * @see org.machanism.machai.ai.tools.ParamDescriptor
 */
package org.machanism.machai.ai.provider.tools;
