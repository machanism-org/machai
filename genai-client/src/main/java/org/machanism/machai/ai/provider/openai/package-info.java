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
 * This package provides the OpenAI-backed implementation of MachAI's provider abstraction
 * ({@link org.machanism.machai.ai.manager.Genai}). Its primary entry point is
 * {@link org.machanism.machai.ai.provider.openai.OpenAIProvider}, which translates prompts, instructions, attached
 * inputs, and conversation state into OpenAI API requests and maps OpenAI responses back into MachAI result types.
 * </p>
 *
 * <p>
 * Responsibilities implemented in this package typically include:
 * </p>
 * <ul>
 *   <li>Building requests for chat/completions-style interactions (for example via the Responses API) from MachAI
 *       prompts and options.</li>
 *   <li>Registering MachAI function tools, translating their schemas into OpenAI tool definitions, and dispatching
 *       tool calls to local handlers.</li>
 *   <li>Collecting token usage (input, cached, and output) and reporting it through
 *       {@link org.machanism.machai.ai.manager.GenaiProviderManager}.</li>
 *   <li>Providing embeddings support via
 *       {@link org.machanism.machai.ai.provider.openai.OpenAIProvider#embedding(String, long)}.</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <p>
 * The provider is configured using a {@link org.machanism.macha.core.commons.configurator.Configurator}. Typical
 * configuration includes the OpenAI model identifier (for example, {@code chatModel}) and an API key supplied via
 * {@code OPENAI_API_KEY}. Optional settings may include a base URL ({@code OPENAI_BASE_URL}) and provider limits
 * such as timeouts and maximum output/tool-call counts.
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
