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
 * Provides the OpenAI-specific implementation of MachAI generative AI services.
 *
 * <p>This package contains the OpenAI-backed provider used to execute text
 * generation, tool-calling, file-assisted prompting, and embedding requests
 * through the OpenAI Java SDK. Its primary type,
 * {@link org.machanism.machai.ai.provider.openai.OpenAIProvider}, adapts the
 * framework-level {@link org.machanism.machai.ai.provider.Genai} contract to
 * the OpenAI Responses and Embeddings APIs.</p>
 *
 * <p>The implementation is responsible for translating prompts, instructions,
 * files, and registered Java tool functions into OpenAI request payloads,
 * executing iterative response loops when the model issues function calls, and
 * converting API usage details into MachAI usage statistics.</p>
 *
 * <h2>Core capabilities</h2>
 * <ul>
 * <li>Configuring OpenAI-compatible clients from runtime settings</li>
 * <li>Submitting conversational prompts and instruction text</li>
 * <li>Registering Java callbacks as OpenAI function tools with JSON-schema parameters</li>
 * <li>Enabling optional built-in tools such as web search and MCP servers</li>
 * <li>Handling multi-step tool-call resolution until a final response is produced</li>
 * <li>Generating embedding vectors and recording token usage information</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
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
