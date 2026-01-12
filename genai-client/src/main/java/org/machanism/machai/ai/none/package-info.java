/**
 * Provides classes for disabling generative AI service integration and logging input requests
 * in the Machai framework when no external AI provider is required or available.
 * <p>
 * This package contains stub implementations for GenAI functionality, notably the {@link NoneProvider},
 * which is used in scenarios where generative AI operations are intentionally disabled, simulated,
 * or must be skipped due to compliance, testing, or fallback requirements.
 * <p>
 * Classes in this package maintain interface compatibility but do not interact with any external AI
 * services or LLMs. Requests and prompts may be logged locally (if configured), and all AI-specific
 * actions are non-operational or will throw exceptions if use of actual AI functionality is expected.
 * <p>
 * Typical scenarios include:
 * <ul>
 *   <li>Disabling generative AI features for security or compliance</li>
 *   <li>Implementing fallback logic when no provider is configured</li>
 *   <li>Logging requests for later review or manual processing</li>
 *   <li>Testing environments not connected to external services</li>
 * </ul>
 * <p>
 * Usage Example (in code):
 * <pre>
 * {@code
 *   GenAIProvider provider = new NoneProvider();
 *   provider.prompt("Describe the weather.");
 *   provider.perform(); // No AI service is called; input may be logged locally
 * }
 * </pre>
 * <p>
 * <b>Note:</b> All classes in this package include detailed Javadoc describing their purpose
 * and behavior. See individual class documentation for further examples and descriptions.
 */
package org.machanism.machai.ai.none;

/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE AND THIS `package-info.java`!**
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * 
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package�s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 *      
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * 
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 		- Use proper Markdown or HTML formatting for readability.
 * 
 * -  Update Javadoc with Code Changes:
 * 		- Revise Javadoc comments whenever code is modified to ensure documentation remains accurate and up to date.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */
