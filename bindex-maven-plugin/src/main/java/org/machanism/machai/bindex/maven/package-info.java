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
 * Maven plugin integration for Machai Bindex.
 *
 * <p>
 * This package provides the Maven {@code Mojo} goals that expose the Machai Bindex
 * workflow to Maven builds.
 * </p>
 *
 * <h2>What the goals do</h2>
 * <ul>
 * <li>Determine whether the current module should be processed (modules with
 * {@code pom} packaging are skipped).</li>
 * <li>Resolve build-time configuration such as the GenAI provider/model identifier
 * (for example {@code OpenAI:gpt-5}).</li>
 * <li>Optionally load GenAI credentials from {@code settings.xml} when
 * {@code gw.genai.serverId} is configured (see {@link AbstractBindexMojo}).</li>
 * <li>Invoke core Bindex operations to create/update an index or publish metadata to
 * a registry.</li>
 * </ul>
 *
 * <h2>Provided goals</h2>
 * <ul>
 * <li>{@link org.machanism.machai.bindex.maven.Create} – create a new index</li>
 * <li>{@link org.machanism.machai.bindex.maven.Update} – update an existing index</li>
 * <li>{@link org.machanism.machai.bindex.maven.Register} – scan and register project metadata to a registry</li>
 * <li>{@link org.machanism.machai.bindex.maven.Clean} – remove temporary artifacts</li>
 * </ul>
 *
 * <h2>Command-line usage</h2>
 * <pre>
 * mvn org.machanism.machai:bindex-maven-plugin:create -Dgw.genai.model=OpenAI:gpt-5
 * mvn org.machanism.machai:bindex-maven-plugin:update -Dgw.genai.model=OpenAI:gpt-5
 * mvn org.machanism.machai:bindex-maven-plugin:register -Dgw.genai.model=OpenAI:gpt-5 -Dbindex.register.url=http://localhost:8080
 * mvn org.machanism.machai:bindex-maven-plugin:clean
 * </pre>
 */
package org.machanism.machai.bindex.maven;
