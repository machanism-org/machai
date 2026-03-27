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
 * This package provides a {@link org.machanism.machai.ai.manager.Genai} implementation backed by Google's
 * Gemini models.
 * </p>
 *
 * <h2>Contents</h2>
 * <ul>
 * <li>{@link org.machanism.machai.ai.provider.gemini.GeminiProvider} - MachAI provider adapter for Gemini.</li>
 * </ul>
 *
 * <h2>Responsibilities</h2>
 * <p>
 * The provider is responsible for translating MachAI's provider-agnostic request state (prompt text, optional
 * system instructions, tool registration, file/URL attachments, and optional usage reporting) into Gemini API
 * calls.
 * </p>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * Configurator conf = ...;
 * GeminiProvider provider = new GeminiProvider();
 * provider.init(conf);
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this document.");
 * String response = provider.perform();
 * </pre>
 *
 * <p>
 * <strong>Implementation note:</strong> the current Gemini provider is a scaffold; many operations are not yet
 * implemented and may throw {@code NotImplementedException}.
 * </p>
 */
package org.machanism.machai.ai.provider.gemini;
