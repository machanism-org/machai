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
 * Provides the service contracts and loading infrastructure used to expose
 * host-defined function tools to generative AI providers.
 *
 * <p>This package contains the SPI for contributing tool sets, the functional
 * callback type used to execute individual tool invocations, and the loader that
 * discovers tool contributors through Java's {@link java.util.ServiceLoader}
 * mechanism.</p>
 *
 * <p>Implementations of {@link org.machanism.machai.ai.tools.FunctionTools}
 * register one or more provider-facing tools against a
 * {@link org.machanism.machai.ai.provider.Genai} instance. The
 * {@link org.machanism.machai.ai.tools.FunctionToolsLoader} is responsible for
 * discovering those implementations on the classpath, supplying an optional
 * {@link org.machanism.macha.core.commons.configurator.Configurator}, and
 * applying the contributed tools to the target provider. Runtime execution of an
 * individual tool is represented by
 * {@link org.machanism.machai.ai.tools.ToolFunction}.</p>
 *
 * <h2>Typical usage</h2>
 *
 * <pre>{@code
 * Configurator configurator = ...;
 * Genai provider = ...;
 *
 * FunctionToolsLoader loader = new FunctionToolsLoader();
 * loader.applyTools(provider, configurator);
 * }</pre>
 *
 * <p>This package is intended for integration code that exposes controlled local
 * capabilities, such as file system access, HTTP operations, or command
 * execution, to an AI provider in a structured and discoverable manner.</p>
 */
package org.machanism.machai.ai.tools;
