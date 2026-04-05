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
 * OpenAI provider integration for MachAI.
 *
 * <p>
 * This package contains the OpenAI-backed {@link org.machanism.machai.ai.provider.Genai}
 * implementation used by MachAI to submit prompts, optional system instructions, file inputs,
 * and locally registered tools through the OpenAI Responses API. It also exposes embedding
 * generation support and translates OpenAI usage data into MachAI usage metrics.
 * </p>
 *
 * <p>
 * The central type in this package is
 * {@link org.machanism.machai.ai.provider.openai.OpenAIProvider}, which manages request
 * construction, response parsing, tool-call execution, input logging, and client configuration.
 * Tool definitions added through the provider are exposed as OpenAI function tools, and tool-call
 * responses are fed back into the conversation until the model returns a final answer.
 * </p>
 *
 * <h2>Configuration overview</h2>
 * <p>
 * The provider is initialized from a configurator and supports settings such as
 * {@code OPENAI_API_KEY}, {@code chatModel}, {@code OPENAI_BASE_URL},
 * {@code GENAI_TIMEOUT}, {@code MAX_OUTPUT_TOKENS}, {@code MAX_TOOL_CALLS}, and
 * {@code embedding.model}.
 * </p>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * Configurator cfg = ...;
 * OpenAIProvider provider = new OpenAIProvider();
 * provider.init(cfg);
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize the attached document.");
 * String answer = provider.perform();
 * Usage usage = provider.usage();
 * </pre>
 */
package org.machanism.machai.ai.provider.openai;
