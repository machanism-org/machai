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

/**
 * Builds {@link org.machanism.machai.schema.Bindex} documents from a
 * {@link org.machanism.machai.project.layout.ProjectLayout}.
 *
 * <p>This package provides a small orchestration layer that coordinates:</p>
 *
 * <ul>
 *   <li>collection of project context via a {@code ProjectLayout} (sources, resources, manifests, etc.);</li>
 *   <li>optional use of an existing/origin {@code Bindex} for incremental regeneration; and</li>
 *   <li>invocation of an LLM provider (via {@link org.machanism.machai.ai.manager.GenAIProvider}) to produce the
 *       final {@code Bindex} model.</li>
 * </ul>
 *
 * <p>The primary entry point is {@link org.machanism.machai.bindex.builder.BindexBuilder}, with concrete builders
 * supplying layout- and ecosystem-specific behavior (for example Maven, JavaScript/TypeScript, and Python).</p>
 */
package org.machanism.machai.bindex.builder;
