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
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 *      - Do not use escaping in `{@code ...}` tags.    
 */

/**
 * Google Gemini provider integration for the MachAI framework.
 *
 * <p>
 * This package provides MachAI's {@link org.machanism.machai.ai.manager.Genai} implementation for
 * Google's Gemini models: {@link org.machanism.machai.ai.provider.gemini.GeminiProvider}.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 * <li>Collecting prompts and system instructions.</li>
 * <li>Managing tool registration (function calling) and dispatch.</li>
 * <li>Managing file inputs/attachments (local files and URL-based resources).</li>
 * <li>Executing requests and exposing response usage metrics.</li>
 * </ul>
 *
 * <p>
 * At runtime, the provider translates MachAI's provider-agnostic contract into Gemini-specific request and
 * response handling.
 * </p>
 *
 * <h2>Implementation status</h2>
 * <p>
 * The current implementation is a scaffold: several operations are no-ops and others throw
 * {@link org.apache.commons.lang.NotImplementedException} until the Gemini adapter is completed.
 * </p>
 *
 * <h2>Example</h2>
 * <pre>
 * Configurator config = ...;
 * GeminiProvider provider = new GeminiProvider();
 * provider.init(config);
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this document.");
 * String response = provider.perform();
 * </pre>
 */
package org.machanism.machai.ai.provider.gemini;
