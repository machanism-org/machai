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
 * Provides OpenAI-based GenAI integration for MachAI.
 *
 * <p>This package contains the OpenAI-specific implementation of the
 * {@link org.machanism.machai.ai.provider.Genai} abstraction used by MachAI to
 * perform text generation and embedding requests through the OpenAI Java SDK.
 * Its primary entry point,
 * {@link org.machanism.machai.ai.provider.openai.OpenAIProvider}, translates
 * framework-level prompts, instructions, tools, and file inputs into OpenAI
 * Responses API requests and converts the resulting responses back into the
 * framework's expected structures.</p>
 *
 * <p>The provider supports conversational prompting, iterative function-tool
 * execution, optional built-in OpenAI tools such as web search and MCP server
 * access, embedding generation, token-usage tracking, and request logging for
 * troubleshooting and observability.</p>
 *
 * <h2>Typical responsibilities</h2>
 * <ul>
 * <li>Building and configuring an {@code OpenAIClient} instance from runtime configuration</li>
 * <li>Collecting user prompts, instructions, and file-based inputs for a response request</li>
 * <li>Registering Java callbacks as OpenAI function tools with JSON-schema parameters</li>
 * <li>Resolving model-issued tool calls and continuing the response loop until final output is produced</li>
 * <li>Generating embeddings and recording token usage statistics for completed requests</li>
 * </ul>
 *
 * <h2>Usage overview</h2>
 * <pre>
 * Configurator configurator = ...;
 * OpenAIProvider provider = new OpenAIProvider();
 * provider.init(configurator);
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize the attached document.");
 * String answer = provider.perform();
 * </pre>
 */
package org.machanism.machai.ai.provider.openai;
