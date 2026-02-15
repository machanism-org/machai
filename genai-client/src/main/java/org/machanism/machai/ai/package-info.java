/*-
 * @guidance:
 *
 * **IMPORTANT: UPDATE THIS `package-info.java`!**
 *
 * - Use Clear and Concise Descriptions:
 *     - Write meaningful summaries that explain the purpose, behavior, and usage of the package and its elements.
 *     - Avoid vague statements; be specific about functionality and intent.
 *
 * - Update `package-info.java`:
 *     - Analyze the source code within this package.
 *     - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose, scope, and usage.
 *     - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *     - Place the package-level Javadoc immediately before the `package` declaration.
 *
 * - Include Usage Examples Where Helpful:
 *     - Provide code snippets or examples in Javadoc comments for complex classes or methods, if relevant to the package.
 *
 * - Maintain Consistency and Formatting:
 *     - Follow a consistent style and structure for all Javadoc comments.
 *     - Use proper Markdown or HTML formatting for readability.
 *
 * - Add Javadoc:
 *     - Summarize the purpose and scope of child packages within the parent package-level Javadoc.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted.
 */

/**
 * Top-level API for integrating with generative-AI (GenAI) providers.
 *
 * <p>This package defines the public, provider-neutral abstractions used by the GenAI Client:
 *
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProvider} is the central contract implemented by
 *   each provider. It supports session-style prompting (instructions + prompts), attaching local or remote
 *   files, invoking a provider to obtain a textual response, and computing embeddings.</li>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProviderManager} resolves a provider from an identifier of the
 *   form {@code Provider:Model} (for example {@code OpenAI:gpt-4o-mini}) and initializes it with application
 *   configuration.</li>
 *   <li>Tool functions can be registered via {@link org.machanism.machai.ai.manager.GenAIProvider#addTool(String, String, java.util.function.Function, String...)},
 *   enabling providers to call back into the host application to perform actions such as reading/writing files,
 *   executing commands, or fetching web content.</li>
 * </ul>
 *
 * <h2>Subpackages</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.manager} – provider contract, provider resolution, and usage accounting.</li>
 *   <li>{@code org.machanism.machai.ai.provider.*} – concrete provider implementations (for example OpenAI, Web,
 *   CodeMie, or a no-op provider).</li>
 *   <li>{@code org.machanism.machai.ai.tools} – reusable tool installers that register common host-side functions
 *   (file system, command execution, web fetching) with a provider.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this repository.");
 * String answer = provider.perform();
 *
 * provider.close();
 * }</pre>
 */
package org.machanism.machai.ai;
