/**
 * Top-level API and SPI extension point for generative AI providers in Machanism AI client framework.
 * <p>
 * This package offers entry points and extensibility for connecting, managing, and using diverse AI models and services. It includes interfaces
 * for prompt handling, model selection, registration of external providers, and safe tool attachment. Sub-packages contain specialized adapters and utilities:
 * <ul>
 *   <li><b>manager</b> – Core interfaces, AI managers, command and file tool integration, and extensibility infrastructure.</li>
 *   <li><b>openAI</b> – Integration classes for OpenAI APIs (e.g., GPT, embeddings, file handling).</li>
 *   <li><b>none</b> – Stub providers for disabling services while preserving compatibility and enabling logging.</li>
 *   <li><b>web</b> – Remote/web AI orchestrator and communication adapters.</li>
 * </ul>
 * <p>
 * <b>Sub-packages:</b>
 * <ul>
 *   <li><b>org.machanism.machai.ai.manager</b>: Contains core provider interfaces (e.g. {@link org.machanism.machai.ai.manager.GenAIProvider}), manager classes (e.g. {@link org.machanism.machai.ai.manager.GenAIProviderManager}), and extensibility/utility infrastructure for integrating new models and tools.</li>
 *   <li><b>org.machanism.machai.ai.openAI</b>: Contains integration classes for OpenAI APIs (GPT models, embeddings, file management, configuration).</li>
 *   <li><b>org.machanism.machai.ai.none</b>: Provides stub/no-op provider implementations to explicitly disable generative AI while maintaining system compatibility and audit logging.</li>
 *   <li><b>org.machanism.machai.ai.web</b>: Offers adapters for remote orchestrator/web-based communication and invocation of external AI services.</li>
 * </ul>
 * <p>
 * <b>Usage Example</b>
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
