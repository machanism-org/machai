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
 *     - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose, scope, and usage based on package-info.java files located on child folders.
 *     - Place the package-level Javadoc immediately before the `package` declaration.
 *
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` and &gt; in `<pre>` content for Javadoc. 
 *        Ensure that the code is properly escaped and formatted.
 */

/**
 * Provides Machai's generative AI integration layer, including provider abstraction,
 * provider resolution, tool registration, prompt metadata, and token-usage tracking.
 *
 * <p>This package is the root of the AI subsystem. It coordinates common contracts
 * for interacting with model providers, concrete integrations for hosted AI
 * services, runtime discovery of Java-based tools and prompts, and shared
 * management utilities used to select providers and record usage statistics.</p>
 *
 * <h2>Package structure</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.provider} defines the provider-facing API,
 *   shared base classes, adapters, type conversion helpers, and embedding-provider
 *   contracts used by application code and concrete integrations.</li>
 *   <li>{@link org.machanism.machai.ai.provider.impl} contains concrete provider
 *   implementations for OpenAI-compatible APIs, Anthropic Claude, CodeMie-backed
 *   routing, and local tool-only execution.</li>
 *   <li>{@link org.machanism.machai.ai.manager} resolves providers from configured
 *   model identifiers and stores token-usage records for reporting and inspection.</li>
 *   <li>{@link org.machanism.machai.ai.tools} defines annotations, descriptors,
 *   service-loader support, and execution contracts for exposing Java methods as
 *   AI-callable tools or prompt sources.</li>
 * </ul>
 *
 * <h2>Typical workflow</h2>
 * <p>Applications usually resolve a provider by model identifier, initialize it
 * with runtime configuration, attach instructions, prompts, tools, files, or MCP
 * servers as needed, execute a generation or embedding request, and then inspect
 * usage information recorded by the provider or manager components.</p>
 *
 * <pre>{@code
 * Configurator configurator = ...;
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4o-mini", configurator);
 * provider.addInstructions("Answer using the project context.");
 * provider.addPrompt("Summarize the selected source files.");
 * String response = provider.generate();
 * UsageStatistics.addUsage("OpenAI:gpt-4o-mini", provider.usage());
 * }</pre>
 *
 * <p>The root package does not define provider behavior directly; it groups the
 * subpackages that make provider-neutral AI workflows available to the rest of
 * the application.</p>
 */
package org.machanism.machai.ai;
