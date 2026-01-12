/**
 * Top-level API and SPI extension point for generative AI providers in Machanism AI client framework.
 * <p>
 * This package offers entry points and extensibility for connecting, managing, and using diverse AI models and services. It includes interfaces
 * for prompt handling, model selection, registration of external providers, and safe tool attachment. Sub-packages contain specialized adapters and utilities:
 * <ul>
 *   <li><code>manager</code> – Core interfaces, AI managers, command and file tool integration, and extensibility infrastructure.</li>
 *   <li><code>openAI</code> – Integration classes for OpenAI APIs (e.g., GPT, embeddings, file handling).</li>
 *   <li><code>none</code> – Stub providers for disabling services while preserving compatibility and enabling logging.</li>
 *   <li><code>web</code> – Remote/web AI orchestrator and communication adapters.</li>
 * </ul>
 * <p>
 * Usage illustrates provider selection and prompt execution:
 * <pre>
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4");
 * provider.prompt("Generate a short story.");
 * String result = provider.perform();
 * System.out.println(result);
 * </pre>
 * <p>
 * See also:
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProvider}</li>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProviderManager}</li>
 * </ul>
 */
package org.machanism.machai.ai;

/* @guidance:
*
* **IMPORTANT: ADD JAVADOC TO THIS `package-info.java`!**
*
* - Update `package-info.java` based on summary sub-packages information.
*/
