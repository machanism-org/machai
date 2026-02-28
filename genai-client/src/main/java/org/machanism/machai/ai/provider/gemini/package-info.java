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
 * This package contains MachAI's {@link org.machanism.machai.ai.manager.GenAIProvider} implementation for Google
 * Gemini models: {@link org.machanism.machai.ai.provider.gemini.GeminiProvider}.
 * </p>
 *
 * <p>
 * The provider adapts MachAI's provider-agnostic contract (prompts, system instructions, tool registration,
 * file attachments, request execution, and usage reporting) into Gemini-specific request and response handling.
 * </p>
 *
 * <h2>Lifecycle</h2>
 * <p>
 * The typical lifecycle is:
 * </p>
 * <ol>
 * <li>Initialize the provider via {@link org.machanism.machai.ai.provider.gemini.GeminiProvider#init}.</li>
 * <li>Optionally set system instructions via
 * {@link org.machanism.machai.ai.provider.gemini.GeminiProvider#instructions}.</li>
 * <li>Provide prompt content via {@link org.machanism.machai.ai.provider.gemini.GeminiProvider#prompt} (and
 * optionally attach files/tools).</li>
 * <li>Execute the request via {@link org.machanism.machai.ai.provider.gemini.GeminiProvider#perform}.</li>
 * <li>Read usage information via {@link org.machanism.machai.ai.provider.gemini.GeminiProvider#usage}.</li>
 * </ol>
 *
 * <h2>Usage</h2>
 * <pre>
 * Configurator config = ...;
 * GeminiProvider provider = new GeminiProvider();
 * provider.init(config);
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this document.");
 * String response = provider.perform();
 * </pre>
 *
 * <p>
 * Note: at present, this integration is a placeholder; several operations are implemented as no-ops and others may
 * throw {@link org.apache.commons.lang.NotImplementedException} until the adapter is fully implemented.
 * </p>
 */
package org.machanism.machai.ai.provider.gemini;
