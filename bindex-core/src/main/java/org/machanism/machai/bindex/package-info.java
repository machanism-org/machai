/**
 * Core entry points for generating, assembling, and registering a Bindex (project index) used by
 * Machanism-based AI workflows.
 *
 * <p>This package provides orchestration and factory types to:
 *
 * <ul>
 *   <li>Assemble a Bindex for a project folder using a {@code GenAIProvider} and a {@code ProjectLayout}.</li>
 *   <li>Create or update Bindex documents and manifests for supported project layouts.</li>
 *   <li>Register Bindex artifacts into a backing store and perform retrieval/search via picker utilities.</li>
 * </ul>
 *
 * <p>Typical workflow:
 *
 * <ol>
 *   <li>Choose/construct a project layout.</li>
 *   <li>Run a creator/processor to generate Bindex artifacts for a folder.</li>
 *   <li>Optionally register generated artifacts in a database for later retrieval.</li>
 * </ol>
 *
 * <p>Example:
 *
 * <pre>
 * GenAIProvider provider = ...;
 * ProjectLayout layout = ...;
 *
 * BindexCreator creator = new BindexCreator(provider);
 * creator.processFolder(layout);
 *
 * try (BindexRegister register = new BindexRegister(provider, dbUrl)) {
 *     register.processFolder(layout);
 * }
 * </pre>
 *
 * <p>For layout-specific builders and prompt/context utilities, see
 * {@link org.machanism.machai.bindex.builder}.
 */
package org.machanism.machai.bindex;

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
 *		- Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
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
 */
