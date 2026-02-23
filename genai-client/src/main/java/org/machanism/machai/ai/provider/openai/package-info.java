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
 * Provides an OpenAI-backed implementation of MachAI's
 * {@link org.machanism.machai.ai.manager.GenAIProvider} abstraction.
 *
 * <p>
 * This package integrates the OpenAI Java SDK with MachAI's provider lifecycle. The primary entry point is
 * {@link org.machanism.machai.ai.provider.openai.OpenAIProvider}, which assembles and executes requests via the
 * OpenAI Responses API (including system instructions, user prompt content, optional file references, and tool
 * registrations) and returns the assistant's final text.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Collect per-request inputs such as prompts, instructions, and referenced files.</li>
 *   <li>Register function tools and dispatch model-requested tool calls to application handlers.</li>
 *   <li>Execute requests through OpenAI and expose the final response text.</li>
 *   <li>Capture and report token usage to
 *       {@link org.machanism.machai.ai.manager.GenAIProviderManager}.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
 * provider.instructions("You are a concise assistant.");
 * provider.prompt("Summarize this text...");
 * String answer = provider.perform();
 * }
 * </pre>
 *
 * <p>
 * <strong>Thread-safety:</strong> provider instances are not thread-safe because they maintain mutable per-request
 * state. Use one instance per request or synchronize externally.
 * </p>
 */
package org.machanism.machai.ai.provider.openai;
