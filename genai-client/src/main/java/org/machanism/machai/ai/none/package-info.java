/**
 * Provides stub classes and interfaces for disabling generative AI integrations and logging input requests
 * within the Machai framework when an external AI provider is not required or available.
 * <p>
 * This package includes the {@link NoneProvider}, a GenAIProvider implementation that stores requests locally
 * and maintains interface compatibility for scenarios where generative AI features are intentionally disabled,
 * simulated, or skipped due to compliance, testing, or fallback requirements.
 * <p>
 * Classes in this package do not interact with any external AI services or large language models (LLMs).
 * Requests and prompts may be logged locally if configured, and methods that require actual AI functionality
 * will throw exceptions or perform no operations.
 * <p>
 * Typical use cases include:
 * <ul>
 *   <li>Disabling generative AI features for security or compliance</li>
 *   <li>Implementing fallback logic when no provider is configured</li>
 *   <li>Logging requests for later review or manual processing</li>
 *   <li>Testing environments not connected to external services</li>
 * </ul>
 * <p>
 * Usage Example:
 * <pre>
 * {@code
 *   GenAIProvider provider = new NoneProvider();
 *   provider.prompt("Describe the weather.");
 *   provider.perform(); // No AI service is called; input may be logged locally
 * }
 * </pre>
 * <p>
 * <b>Note:</b> All classes in this package provide detailed Javadoc describing their purpose
 * and behavior. See individual class documentation for additional details and examples.
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
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
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
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */
