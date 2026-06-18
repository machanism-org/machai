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
 * Provides the OpenAI-based implementation of Machai generative AI services.
 *
 * <p>This package contains the {@link org.machanism.machai.ai.provider.openai.OpenAIProvider}
 * implementation, which adapts the framework-level
 * {@link org.machanism.machai.ai.provider.Genai} contract to the OpenAI Java SDK.
 * It is responsible for building response and embedding requests, mapping Machai
 * prompts and instructions to OpenAI inputs, and translating OpenAI usage metrics
 * into Machai usage statistics.</p>
 *
 * <p>The package supports conversational text generation, iterative function-tool
 * execution, optional built-in web search and MCP server tools, file-based inputs,
 * and embedding generation. During response processing, the provider can continue
 * request cycles until all model-issued tool calls are resolved and a final text
 * response is produced.</p>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 * <li>Creating and configuring OpenAI clients from runtime configuration</li>
 * <li>Submitting prompts, instructions, and structured response input items</li>
 * <li>Registering Java-backed tool functions as OpenAI function tools</li>
 * <li>Enabling optional OpenAI web search and MCP server integrations</li>
 * <li>Handling iterative tool-call execution and follow-up response requests</li>
 * <li>Requesting embeddings and recording token usage for reporting</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * Configurator configurator = ...;
 * OpenAIProvider provider = new OpenAIProvider();
 * provider.init("openai", configurator);
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize the attached document.");
 * String answer = provider.perform();
 * </pre>
 */
package org.machanism.machai.ai.provider.openai;
