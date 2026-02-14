/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
 * 
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
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` as `&amp;gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */

/**
 * Provider-neutral public API for configuring and executing GenAI operations.
 *
 * <p>This package defines the provider-agnostic abstractions used by the GenAI Client library. It models
 * <em>what</em> an AI operation is (requests, options, tools, and results) rather than <em>how</em> a specific
 * provider performs it.
 *
 * <h2>Typical usage</h2>
 * <ol>
 *   <li>Select and configure a provider/model.</li>
 *   <li>Build a request from system instructions and user input, optionally registering tools/functions.</li>
 *   <li>Execute the operation and consume the returned result (generated text, tool calls, embeddings, etc.).</li>
 * </ol>
 *
 * <h2>Related packages</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.provider} for provider-specific implementations.</li>
 *   <li>{@code org.machanism.machai.ai.manager} for provider discovery/selection and management.</li>
 * </ul>
 */
package org.machanism.machai.ai;
