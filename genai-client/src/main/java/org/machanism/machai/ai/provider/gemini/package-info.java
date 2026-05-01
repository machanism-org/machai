/*-
 * @guidance:
 *
 * **IMPORTANT: ADD OR UPDATE JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
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
 * Provides MachAI integration components for Google's Gemini models.
 *
 * <p>This package contains the Gemini-specific {@link org.machanism.machai.ai.provider.Genai}
 * implementation used by MachAI to translate provider-neutral AI operations into Gemini-backed
 * requests.</p>
 *
 * <p>The package currently centers on {@link org.machanism.machai.ai.provider.gemini.GeminiProvider},
 * which defines the expected lifecycle for provider initialization, prompt collection, instruction
 * handling, tool registration, embedding generation, request execution, working-directory
 * coordination, and usage reporting.</p>
 *
 * <p>At present, the implementation is primarily structural and documents the intended behavior of
 * the Gemini integration while several operations still act as placeholders pending full Gemini API
 * support.</p>
 *
 * <h2>Usage overview</h2>
 * <p>Typical usage consists of creating the provider, initializing it with configuration,
 * supplying instructions and prompts, and then executing the request.</p>
 *
 * <pre>
 * Genai genai = new GeminiProvider();
 * genai.init(conf);
 * genai.instructions("You are a helpful assistant.");
 * genai.prompt("Summarize the following text.");
 * String response = genai.perform();
 * </pre>
 */
package org.machanism.machai.ai.provider.gemini;
