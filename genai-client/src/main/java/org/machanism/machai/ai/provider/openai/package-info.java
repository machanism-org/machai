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
 * OpenAI provider implementation for MachAI.
 *
 * <p>
 * This package adapts MachAI's {@link org.machanism.machai.ai.manager.Genai} abstraction to the
 * OpenAI Java SDK, primarily via {@link org.machanism.machai.ai.provider.openai.OpenAIProvider}.
 * </p>
 *
 * <p>
 * Responsibilities include:
 * </p>
 * <ul>
 *   <li>Creating OpenAI Responses API requests from prompts, instructions, files, and conversation state.</li>
 *   <li>Registering function tools, translating MachAI tool definitions to OpenAI tool schemas, and dispatching
 *       tool calls to locally provided handlers.</li>
 *   <li>Extracting usage information (input, cached, and output tokens) and reporting it via
 *       {@link org.machanism.machai.ai.manager.GenaiProviderManager}.</li>
 *   <li>Obtaining embedding vectors for text via {@link org.machanism.machai.ai.provider.openai.OpenAIProvider#embedding(String, long)}.</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <p>
 * The provider is initialized from a {@link org.machanism.macha.core.commons.configurator.Configurator} and
 * expects at minimum:
 * </p>
 * <ul>
 *   <li>{@code chatModel}: model identifier passed to the OpenAI Responses API.</li>
 *   <li>{@code OPENAI_API_KEY}: API key for authenticating requests.</li>
 * </ul>
 * <p>
 * Optional settings include {@code OPENAI_BASE_URL}, {@code GENAI_TIMEOUT}, {@code MAX_OUTPUT_TOKENS}, and
 * {@code MAX_TOOL_CALLS}.
 * </p>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-5.1");
 * provider.instructions("You are a concise assistant.");
 * provider.prompt("Summarize this text...");
 * String answer = provider.perform();
 * }
 * </pre>
 */
package org.machanism.machai.ai.provider.openai;
