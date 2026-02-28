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
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` and &gt; in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted.
 */

/**
 * Provider-neutral entry point for invoking generative-AI models.
 *
 * <p>
 * This package defines the top-level namespace for the GenAI Client. It ties together:
 * </p>
 * <ul>
 *   <li>a common provider contract for chat-style inference, file inputs, embeddings, and tool registration,</li>
 *   <li>provider/model resolution based on a simple {@code Provider:Model} identifier, and</li>
 *   <li>host-side tool discovery for providers that support tool/function calling.</li>
 * </ul>
 *
 * <h2>Typical workflow</h2>
 * <pre>{@code
 * Configurator conf = ...;
 *
 * // Resolve a provider and model (format: "Provider:Model").
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * // Optionally register host-side tools with the provider.
 * FunctionToolsLoader loader = FunctionToolsLoader.getInstance();
 * loader.setConfiguration(conf);
 * loader.applyTools(provider);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this repository.");
 * String response = provider.perform();
 *
 * Usage usage = provider.usage();
 * GenAIProviderManager.addUsage(usage);
 * GenAIProviderManager.logUsage();
 * }</r
 * </pre>
 *
 * <h2>Key concepts</h2>
 * <ul>
 *   <li><b>Providers</b> are implementations of {@link org.machanism.machai.ai.manager.GenAIProvider} (for example,
 *       OpenAI, CodeMie, or a no-op provider) that adapt MachAI to a concrete backend.</li>
 *   <li><b>Model selection</b> is performed by {@link org.machanism.machai.ai.manager.GenAIProviderManager} by parsing
 *       {@code Provider:Model} (for example {@code OpenAI:gpt-4o-mini}). If the provider is omitted, the default
 *       {@code None} provider is used.</li>
 *   <li><b>Tools</b> are host-side functions registered via
 *       {@link org.machanism.machai.ai.tools.FunctionToolsLoader} and exposed to tool-calling capable providers
 *       through {@link org.machanism.machai.ai.manager.GenAIProvider#addTool(String, String, java.util.function.Function, String...)}.</li>
 * </ul>
 *
 * <h2>Subpackages</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.manager} – Provider contract, provider/model resolution, and usage aggregation.</li>
 *   <li>{@code org.machanism.machai.ai.provider} – Concrete provider implementations.</li>
 *   <li>{@code org.machanism.machai.ai.tools} – Host-side tool discovery and built-in tool sets (file/web/command).
 *   </li>
 * </ul>
 */
package org.machanism.machai.ai;
