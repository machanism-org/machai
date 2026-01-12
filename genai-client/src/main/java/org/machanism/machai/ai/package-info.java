/**
 * Defines the top-level API interfaces and SPI extensions for generative AI providers in the Machanism AI client framework.
 * <p>
 * This root package serves as the entry point for interacting with various AI models, enabling prompt management, model selection,
 * integration of external AI services, flexible tool attachment, and streamlined extension of model capabilities through sub-packages:
 * <ul>
 *   <li><b>manager</b> – Core interfaces, managers, and system tool integrations for provider extensibility.</li>
 *   <li><b>openAI</b> – Integration with the OpenAI API, supporting prompts, embeddings, and file management.</li>
 *   <li><b>none</b> – Stub providers for disabling AI services while retaining interface compatibility and logging.</li>
 *   <li><b>web</b> – Classes supporting web-based/remote AI provider integration and orchestration.</li>
 * </ul>
 * <p>
 * The package is designed for extensibility, allowing developers to register new provider implementations, attach custom
 * command and file function tools, and ensure safe, efficient usage of generative AI capabilities in a variety of deployment scenarios. All
 * sub-packages and classes should include specialized Javadoc reflecting their behaviors and usage examples.
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4");
 * provider.prompt("Generate a short story.");
 * String result = provider.perform();
 * System.out.println(result);
 * </pre>
 * <p>
 * <b>See Also:</b>
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
