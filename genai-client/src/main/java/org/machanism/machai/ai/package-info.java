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
 * Provider-neutral public API for building and executing GenAI requests.
 *
 * <p>This package acts as the root namespace for the GenAI client and groups the provider-agnostic types and
 * subpackages used to:
 * <ul>
 *   <li>select and configure a concrete provider and model,</li>
 *   <li>submit prompts and system instructions,</li>
 *   <li>attach local or remote files for provider-side processing,</li>
 *   <li>register callable tools that a model may invoke during execution,</li>
 *   <li>execute requests and consume the resulting text response or embeddings.</li>
 * </ul>
 *
 * <p>Concrete provider integrations live under {@code org.machanism.machai.ai.provider}, while provider
 * discovery/selection and shared utilities are under {@code org.machanism.machai.ai.manager}.
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this text.");
 *
 * String response = provider.perform();
 * provider.close();
 * }
 * </pre>
 */
package org.machanism.machai.ai;
