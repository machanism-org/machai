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
 * <p>This package defines the top-level abstractions used to interact with large language models and related AI
 * services without binding application code to a specific vendor SDK, transport, or authentication approach. It is
 * the root package for selecting providers, configuring requests, attaching host-executed tools, and collecting usage
 * information from completed interactions.
 *
 * <h2>Package scope</h2>
 * <ul>
 *   <li><strong>Manager infrastructure</strong> in {@code org.machanism.machai.ai.manager} resolves provider/model
 *   identifiers, initializes provider implementations, and aggregates usage statistics across requests.</li>
 *   <li><strong>Provider contracts and implementations</strong> in {@code org.machanism.machai.ai.provider} define the
 *   common {@link org.machanism.machai.ai.provider.Genai} API and supply concrete integrations for supported
 *   backends.</li>
 *   <li><strong>Host-side tool integration</strong> in {@code org.machanism.machai.ai.tools} exposes controlled local
 *   capabilities, such as command execution and HTTP access, that can be registered with compatible providers for
 *   tool or function calling.</li>
 * </ul>
 *
 * <h2>Typical usage flow</h2>
 * <ol>
 *   <li>Resolve a provider using a provider/model identifier such as {@code OpenAI:gpt-4o-mini}.</li>
 *   <li>Configure system instructions, user prompts, and any optional provider-specific settings.</li>
 *   <li>Optionally register host-side tools through
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
 * <p>The package is intended for higher-level application code that coordinates prompt construction and response
 * handling while delegating provider resolution, backend-specific execution, and capability registration to the
 * corresponding sub-packages.
 */
package org.machanism.machai.ai;
