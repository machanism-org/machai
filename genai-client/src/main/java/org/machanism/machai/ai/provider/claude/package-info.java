/*-
 * @guidance:
 *
 * **IMPORTANT: ADD OR UPDATE JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 *      - Do not use escaping in `{@code ...}` tags.    
 */

/**
 * Provides Anthropic Claude integration for the MachAI generative AI provider layer.
 *
 * <p>This package contains the Claude-specific implementation of the {@link org.machanism.machai.ai.provider.Genai}
 * contract used by MachAI. It adapts Claude models to the framework's common provider interface so callers can submit
 * prompts, configure system instructions, register tools, inspect usage, manage working-directory context, and request
 * embeddings through a consistent API.
 *
 * <p>The package currently centers on {@link org.machanism.machai.ai.provider.claude.ClaudeProvider}, which defines
 * the Claude provider entry point and the required method surface for integrating Anthropic services into the wider
 * MachAI runtime.
 *
 * <p>Client code typically interacts with this package through the
 * {@link org.machanism.machai.ai.provider.Genai} abstraction rather than referencing provider-specific implementation
 * details directly.
 */
package org.machanism.machai.ai.provider.claude;
