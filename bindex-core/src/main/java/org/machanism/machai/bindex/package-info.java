/**
 * Entry points for generating, assembling, and registering a Bindex (project index) used by Machanism-based AI
 * workflows.
 *
 * <p>This package coordinates the end-to-end lifecycle of producing a {@code bindex.json} document for a project and
 * (optionally) registering that document in a backing store for later semantic retrieval.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li><strong>Build</strong>: choose an appropriate {@link org.machanism.machai.bindex.builder.BindexBuilder}
 *       implementation for a {@link org.machanism.machai.project.layout.ProjectLayout} via
 *       {@link org.machanism.machai.bindex.BindexBuilderFactory}.</li>
 *   <li><strong>Create/update</strong>: generate or update {@code bindex.json} on disk via
 *       {@link org.machanism.machai.bindex.BindexCreator}.</li>
 *   <li><strong>Assemble prompts/inputs</strong>: compose LLM inputs that incorporate one or more Bindex documents via
 *       {@link org.machanism.machai.bindex.ApplicationAssembly}.</li>
 *   <li><strong>Register/pick</strong>: register Bindex documents and perform semantic lookup via
 *       {@link org.machanism.machai.bindex.BindexRegister} and {@link org.machanism.machai.bindex.Picker}.</li>
 * </ul>
 *
 * <h2>Typical workflow</h2>
 * <ol>
 *   <li>Create or choose a {@link org.machanism.machai.project.layout.ProjectLayout} describing the project.</li>
 *   <li>Generate or update {@code bindex.json} with {@link org.machanism.machai.bindex.BindexCreator}.</li>
 *   <li>Optionally register the resulting Bindex with {@link org.machanism.machai.bindex.BindexRegister}.</li>
 *   <li>Optionally run semantic selection of relevant Bindexes with {@link org.machanism.machai.bindex.Picker}.</li>
 * </ol>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * GenAIProvider provider = ...;
 * ProjectLayout layout = ...;
 *
 * new BindexCreator(provider)
 *     .update(true)
 *     .processFolder(layout);
 *
 * try (BindexRegister register = new BindexRegister(provider, dbUrl)) {
 *     register.update(true).processFolder(layout);
 * }
 * }</pre>
 *
 * <p>For builder implementations and schema/prompt utilities, see {@link org.machanism.machai.bindex.builder}.
 */
package org.machanism.machai.bindex;

/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
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
 * 		- Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
