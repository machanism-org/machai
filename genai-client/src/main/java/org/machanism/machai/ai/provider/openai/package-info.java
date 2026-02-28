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
 * This package contains provider types that adapt the MachAI provider abstraction to the OpenAI APIs. It includes the
 * concrete {@link org.machanism.machai.ai.manager.GenAIProvider} implementation
 * ({@link org.machanism.machai.ai.provider.openai.OpenAIProvider}) and supporting classes for constructing requests,
 * handling tool calling, and mapping OpenAI responses back into MachAI types.
 * </p>
 *
 * <p>
 * Typical responsibilities include:
 * </p>
 * <ul>
 *   <li>Translating system instructions, prompts, and conversation state into OpenAI request payloads.</li>
 *   <li>Mapping MachAI tool definitions into OpenAI tool specifications and dispatching tool invocations.</li>
 *   <li>Collecting response metadata (for example token usage) for reporting through
 *       {@link org.machanism.machai.ai.manager.GenAIProviderManager}.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
 * provider.instructions("You are a concise assistant.");
 * provider.prompt("Summarize this text...");
 * String answer = provider.perform();
 * }
 * </pre>
 */
package org.machanism.machai.ai.provider.openai;
