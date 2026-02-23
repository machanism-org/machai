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
 * Provider-neutral entry point for interacting with generative-AI models.
 *
 * <p>
 * This package provides the shared, top-level API used by the GenAI client to talk to different model vendors
 * through a single contract. A provider is resolved from a {@code Provider:Model} identifier, configured from the
 * application {@code Configurator}, and then used as a session-like object to accumulate instructions/prompts,
 * optional attachments, and optional host-side tools before producing a response.
 * </p>
 *
 * <h2>Core concepts</h2>
 * <ul>
 *   <li>
 *     <strong>Provider contract</strong> – {@link org.machanism.machai.ai.manager.GenAIProvider} defines the common
 *     operations supported by integrations (prompting, file inputs, tool registration, execution, and usage).
 *   </li>
 *   <li>
 *     <strong>Provider resolution</strong> – {@link org.machanism.machai.ai.manager.GenAIProviderManager} creates a
 *     concrete provider instance from either a short provider name (for example {@code OpenAI}) or a
 *     fully-qualified class name, and applies the selected model.
 *   </li>
 *   <li>
 *     <strong>Host-side tools</strong> – {@link org.machanism.machai.ai.tools.FunctionToolsLoader} discovers optional
 *     tool installers via {@link java.util.ServiceLoader} and registers curated local capabilities (such as file,
 *     web, or command helpers) with a provider via {@link org.machanism.machai.ai.manager.GenAIProvider#addTool}.
 *   </li>
 *   <li>
 *     <strong>Usage accounting</strong> – {@link org.machanism.machai.ai.manager.Usage} reports token counts for the
 *     last execution when supported by the underlying provider.
 *   </li>
 * </ul>
 *
 * <h2>Typical workflow</h2>
 * <ol>
 *   <li>Resolve a provider/model using
 *   {@link org.machanism.machai.ai.manager.GenAIProviderManager#getProvider(String, org.machanism.macha.core.commons.configurator.Configurator)}.</li>
 *   <li>(Optional) Register host-side tools using {@link org.machanism.machai.ai.tools.FunctionToolsLoader}.</li>
 *   <li>Add instructions/prompts and any supported attachments or tools.</li>
 *   <li>Execute with {@link org.machanism.machai.ai.manager.GenAIProvider#perform()} and read
 *   {@link org.machanism.machai.ai.manager.GenAIProvider#usage()}.</li>
 *   <li>Close the provider when finished.</li>
 * </ol>
 *
 * <h2>Subpackages</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.manager} – provider contracts, provider/model resolution, and usage reporting.</li>
 *   <li>{@code org.machanism.machai.ai.provider.*} – concrete provider implementations (for example OpenAI, Gemini, Claude).</li>
 *   <li>{@code org.machanism.machai.ai.tools} – optional host-side tool installers and security helpers.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * FunctionToolsLoader.getInstance().setConfiguration(conf);
 * FunctionToolsLoader.getInstance().applyTools(provider);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this repository.");
 *
 * String answer = provider.perform();
 * Usage usage = provider.usage();
 *
 * provider.close();
 * }</pre>
 */
package org.machanism.machai.ai;
