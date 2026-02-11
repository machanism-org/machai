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
 * Provider-neutral public API for configuring and executing GenAI requests.
 *
 * <p>This package contains the top-level, provider-agnostic types that applications use to:
 *
 * <ul>
 *   <li>Select and configure an AI provider and model.</li>
 *   <li>Submit system instructions and user prompts.</li>
 *   <li>Attach local or remote files for provider-side processing.</li>
 *   <li>Register callable tools that a model may invoke during execution.</li>
 *   <li>Execute requests and consume the resulting text response or embeddings.</li>
 * </ul>
 *
 * <p>Concrete provider integrations are located under {@code org.machanism.machai.ai.provider}. Provider
 * discovery/selection and shared utilities are under {@code org.machanism.machai.ai.manager}.
 */
package org.machanism.machai.ai;
