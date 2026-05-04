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
 * Defines the host-side tool registration contracts used to expose executable capabilities to a
 * {@link org.machanism.machai.ai.provider.Genai} provider.
 *
 * <p>
 * This package contains the service-provider interface for contributing tool collections, a loader
 * that discovers and applies those contributions through Java's
 * {@link java.util.ServiceLoader} mechanism, and a functional contract representing executable tool
 * implementations. Together, these types provide the infrastructure required to bind local
 * application features such as file operations, HTTP integrations, or command execution into an AI
 * provider's tool invocation model.
 * </p>
 *
 * <p>
 * Configuration-aware implementations may resolve externalized values, including placeholders such
 * as ${...}, by delegating to a
 * {@link org.machanism.macha.core.commons.configurator.Configurator} before registering or invoking
 * tools.
 * </p>
 */
package org.machanism.machai.ai.tools;
