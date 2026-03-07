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
 *      - Follow a consistent style and structure for all Javadoc comments.
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` as `&lt;` 
 *          and `&gt;` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */

/**
 * Maven plugin goals (Mojos) that integrate Machai/Bindex operations into a Maven build.
 *
 * <p>
 * This package contains Maven {@code @Mojo} implementations that bridge Maven's build lifecycle to the Machai/Bindex
 * tooling:
 * </p>
 * <ul>
 *   <li>Index generation and refresh via {@link org.machanism.machai.bindex.BindexCreator}.</li>
 *   <li>Project scanning and metadata publishing via {@link org.machanism.machai.bindex.BindexRegister}.</li>
 * </ul>
 *
 * <p>
 * Most goals extend {@link org.machanism.machai.bindex.maven.AbstractBindexMojo} to share configuration such as the
 * current {@link org.apache.maven.project.MavenProject}, the project base directory, and the configured AI provider/model
 * (via {@code -Dbindex.genai}, for example {@code OpenAI:gpt-5}). Projects with {@code pom} packaging (typically
 * parent/aggregator modules) are skipped.
 * </p>
 *
 * <h2>Provided goals</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.maven.Create create} &ndash; create a new Bindex index for the current module.</li>
 *   <li>{@link org.machanism.machai.bindex.maven.Update update} &ndash; update (refresh) an existing index.</li>
 *   <li>{@link org.machanism.machai.bindex.maven.Register register} &ndash; scan and publish metadata to a registry.</li>
 *   <li>{@link org.machanism.machai.bindex.maven.Clean clean} &ndash; remove temporary artifacts created by the tooling.</li>
 * </ul>
 *
 * <h2>Command-line usage</h2>
 * <pre>
 * mvn org.machanism.machai:bindex-maven-plugin:create -Dbindex.genai=OpenAI:gpt-5
 * mvn org.machanism.machai:bindex-maven-plugin:update -Dbindex.genai=OpenAI:gpt-5
 * mvn org.machanism.machai:bindex-maven-plugin:register -Dbindex.genai=OpenAI:gpt-5 -Dbindex.register.url=http://localhost:8080
 * mvn org.machanism.machai:bindex-maven-plugin:clean
 * </pre>
 */
package org.machanism.machai.bindex.maven;
