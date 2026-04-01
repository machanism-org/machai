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
 * MachAI provider integration for Google's Gemini models.
 *
 * <p>
 * This package provides an implementation of {@link org.machanism.machai.ai.manager.Genai} that adapts
 * MachAI's provider-agnostic request model (prompts, optional system instructions, optional tool registration,
 * and optional file/URL inputs) to Gemini.
 * </p>
 *
 * <p>
 * The main entry point is {@link org.machanism.machai.ai.provider.gemini.GeminiProvider}. At present, the
 * provider is a scaffold and most operations are not yet implemented.
 * </p>
 *
 * <h2>Typical usage</h2>
 *
 * <pre>
 * Genai genai = new GeminiProvider();
 * genai.init(conf);
 * genai.instructions("You are a helpful assistant.");
 * genai.prompt("Summarize the following text...");
 * String response = genai.perform();
 * </pre>
 */
package org.machanism.machai.ai.provider.gemini;
