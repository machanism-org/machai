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
 * Provider-agnostic public API for the Machanism generative AI (GenAI) client.
 *
 * <p>This package contains the core, provider-neutral types used to construct requests and consume responses
 * without coupling application code to any specific vendor SDK.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Defines shared request/response concepts that are common across supported providers.</li>
 *   <li>Exposes stable interfaces and value types intended for direct use by application code.</li>
 *   <li>Provides entry points used by provider implementations/managers to execute model requests.</li>
 * </ul>
 *
 * <h2>Related packages</h2>
 * <ul>
 *   <li><strong>Providers</strong>: concrete integrations under {@code org.machanism.machai.ai.provider}.</li>
 *   <li><strong>Management</strong>: provider discovery/selection under {@code org.machanism.machai.ai.manager}.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <ol>
 *   <li>Select a provider and model via the provider manager.</li>
 *   <li>Configure request inputs such as instructions and prompts (and optionally tools, depending on provider).</li>
 *   <li>Execute the request and consume the result.</li>
 * </ol>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini");
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this text.");
 * String response = provider.perform();
 * }
 * </pre>
 */
package org.machanism.machai.ai;
