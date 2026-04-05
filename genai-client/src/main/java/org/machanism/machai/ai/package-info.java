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
 * Provider-agnostic generative AI client API for MachAI.
 *
 * <p>This package defines the top-level abstractions used to work with large language models and related AI services
 * without coupling application code to a specific vendor SDK, transport, or authentication mechanism. It serves as
 * the entry point for configuring providers, building prompts and instructions, attaching host-executed tools, and
 * collecting usage information from completed interactions.
 *
 * <h2>Package scope</h2>
 * <ul>
 *   <li><strong>Manager infrastructure</strong> in {@code org.machanism.machai.ai.manager} resolves provider/model
 *   identifiers, initializes provider implementations, and aggregates token usage statistics.</li>
 *   <li><strong>Provider contracts and implementations</strong> in {@code org.machanism.machai.ai.provider} define the
 *   common {@link org.machanism.machai.ai.provider.Genai} API together with concrete integrations such as OpenAI,
 *   Gemini, CodeMie, and no-op/offline providers.</li>
 *   <li><strong>Host-side tool integration</strong> in {@code org.machanism.machai.ai.tools} exposes controlled local
 *   capabilities such as command execution and HTTP access that can be registered with compatible providers for tool
 *   or function calling.</li>
 * </ul>
 *
 * <h2>Typical usage flow</h2>
 * <ol>
 *   <li>Resolve a provider using a provider/model identifier such as {@code OpenAI:gpt-4o-mini}.</li>
 *   <li>Configure system instructions, user prompts, and any optional provider settings.</li>
 *   <li>Optionally discover and register host-side tools through
 *   {@link org.machanism.machai.ai.tools.FunctionToolsLoader}.</li>
 *   <li>Execute the request through the selected provider and inspect or aggregate usage metrics.</li>
 * </ol>
 *
 * <h2>Example</h2>
 * <pre>
 * Configurator conf = ...;
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * provider.instructions("You are a concise assistant.");
 * provider.prompt("Summarize this repository.");
 *
 * FunctionToolsLoader.getInstance().setConfiguration(conf);
 * FunctionToolsLoader.getInstance().applyTools(provider);
 *
 * String answer = provider.perform();
 * GenaiProviderManager.addUsage(provider.usage());
 * GenaiProviderManager.logUsage();
 * </pre>
 *
 * <p>The package is designed so higher-level application code can remain focused on prompt orchestration and result
 * handling while provider selection, provider-specific execution details, and host capability registration are managed
 * by the corresponding sub-packages.
 */
package org.machanism.machai.ai;
