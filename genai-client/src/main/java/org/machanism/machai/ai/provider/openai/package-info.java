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
 * OpenAI-backed provider implementation for the MachAI {@link org.machanism.machai.ai.manager.GenAIProvider}
 * abstraction.
 *
 * <p>
 * The types in this package adapt the OpenAI Java SDK to the MachAI provider interface. They construct request
 * inputs (prompts, optional instructions, and optional file inputs), execute calls through the OpenAI Responses
 * API, handle iterative tool/function calls, and report usage metrics.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Translate MachAI provider configuration into OpenAI request structures.</li>
 *   <li>Execute response requests and parse text/tool-call outputs.</li>
 *   <li>Register and dispatch function tools to application handlers.</li>
 *   <li>Create vector embeddings for input text.</li>
 *   <li>Report token usage to {@link org.machanism.machai.ai.manager.GenAIProviderManager}.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
 * provider.model("gpt-5.1");
 * provider.instructions("You are a concise assistant.");
 * provider.prompt("Summarize this text...");
 * String answer = provider.perform();
 * </pre>
 *
 * <p>
 * <strong>Thread-safety:</strong> Instances are not thread-safe; use one instance per request or synchronize
 * access externally.
 * </p>
 */
package org.machanism.machai.ai.provider.openai;
