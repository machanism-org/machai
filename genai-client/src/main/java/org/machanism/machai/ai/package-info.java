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
 * Provider-neutral API for interacting with generative-AI (GenAI) models.
 *
 * <p>This package defines the root API surface for the GenAI Client. It provides the types that let an application
 * select a concrete provider/model, build up a request (instructions, prompt, and optional inputs), optionally expose
 * host-side function tools, execute the request, and inspect usage information.
 *
 * <h2>Typical workflow</h2>
 * <ol>
 *   <li>Resolve a provider and model with
 *   {@link org.machanism.machai.ai.manager.GenAIProviderManager#getProvider(String, org.machanism.macha.core.commons.configurator.Configurator)}.</li>
 *   <li>Optionally install host-side tools via {@link org.machanism.machai.ai.tools.FunctionToolsLoader}.</li>
 *   <li>Build request state using {@link org.machanism.machai.ai.manager.GenAIProvider#instructions(String)} and
 *   {@link org.machanism.machai.ai.manager.GenAIProvider#prompt(String)} (and any provider-specific inputs).</li>
 *   <li>Execute with {@link org.machanism.machai.ai.manager.GenAIProvider#perform()} and read metrics with
 *   {@link org.machanism.machai.ai.manager.GenAIProvider#usage()}.</li>
 * </ol>
 *
 * <h2>Subpackages</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.manager} – provider contracts, provider resolution, and usage accounting.</li>
 *   <li>{@code org.machanism.machai.ai.provider.*} – concrete provider implementations (for example, OpenAI or other backends).</li>
 *   <li>{@code org.machanism.machai.ai.tools} – optional host-side function tool registration and installers.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * // Optional: install host-side tools, depending on the provider and host policy.
 * FunctionToolsLoader.getInstance().setConfiguration(conf);
 * FunctionToolsLoader.getInstance().applyTools(provider);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this repository.");
 * String answer = provider.perform();
 * Usage usage = provider.usage();
 *
 * provider.close();
 * }</pre>
 */
package org.machanism.machai.ai;
