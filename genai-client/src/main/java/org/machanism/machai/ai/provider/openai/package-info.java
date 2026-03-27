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
 * This package provides the OpenAI-backed {@link org.machanism.machai.ai.manager.Genai} implementation
 * ({@link org.machanism.machai.ai.provider.openai.OpenAIProvider}). It builds OpenAI Responses API requests from
 * MachAI prompts, optional system instructions, file inputs, and registered tools, then converts the model output
 * into plain text results and MachAI token-usage metrics.
 * </p>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * Configurator cfg = ...;
 * OpenAIProvider provider = new OpenAIProvider();
 * provider.init(cfg);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this document.");
 * provider.addFile(new File("./docs/input.pdf"));
 *
 * String answer = provider.perform();
 * Usage usage = provider.usage();
 * </pre>
 *
 * <h2>Tools and function calling</h2>
 * <p>
 * Tools can be registered via {@link org.machanism.machai.ai.manager.Genai#addTool(String, String, org.machanism.machai.ai.manager.Genai.ToolFunction, String...)},
 * which exposes them to the model as OpenAI function tools. When the model requests a tool call, the provider
 * executes the matching handler locally, appends the tool output to the conversation, and continues until a final
 * response message is produced.
 * </p>
 *
 * <h2>Configuration</h2>
 * <p>
 * The provider is initialized via
 * {@link org.machanism.machai.ai.provider.openai.OpenAIProvider#init(org.machanism.macha.core.commons.configurator.Configurator)}
 * and reads the following configuration keys:
 * </p>
 * <ul>
 *   <li>{@code OPENAI_API_KEY} (required): OpenAI API key.</li>
 *   <li>{@code chatModel} (required): model identifier used for responses (for example {@code gpt-4.1}).</li>
 *   <li>{@code OPENAI_BASE_URL} (optional): base URL override for OpenAI-compatible endpoints.</li>
 *   <li>{@code GENAI_TIMEOUT} (optional): timeout in seconds for API requests.</li>
 *   <li>{@code MAX_OUTPUT_TOKENS} (optional): maximum output tokens per response.</li>
 *   <li>{@code MAX_TOOL_CALLS} (optional): maximum number of tool calls allowed per response.</li>
 * </ul>
 */
package org.machanism.machai.ai.provider.openai;
