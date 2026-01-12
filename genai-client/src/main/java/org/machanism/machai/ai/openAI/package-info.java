/**
 * Provides integration with OpenAI API, enabling generative AI capabilities such as prompt management, model interaction,
 * file handling, and vector embedding functionalities.
 * <p>
 * The classes in this package abstract interactions with the OpenAI API, facilitating tasks such as:
 * <ul>
 *   <li>Sending text and file-based prompts to LLMs (Large Language Models).</li>
 *   <li>Obtaining model-generated responses in various formats.</li>
 *   <li>Uploading, referencing, and managing files for AI workflows.</li>
 *   <li>Creating and using embeddings for semantic search and similarity matching.</li>
 *   <li>Customizing tools to extend model capabilities and function execution.</li>
 *   <li>Managing session state, instructions, logging, and working directories.</li>
 * </ul>
 * <p>
 * This package is designed for developers who want to integrate advanced OpenAI features into their applications with a simplified API overlay,
 * supporting both synchronous and asynchronous operations, extensible tooling, and easy configuration.
 * <p>
 * <strong>Usage Example:</strong>
 * <pre>
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-5.1");
 * provider.prompt("What is the capital of France?");
 * String result = provider.perform();
 * System.out.println(result); // Output: Paris
 * </pre>
 *
 * Thread safety: Classes in this package are NOT thread-safe unless explicitly documented otherwise.
 */
package org.machanism.machai.ai.openAI;
