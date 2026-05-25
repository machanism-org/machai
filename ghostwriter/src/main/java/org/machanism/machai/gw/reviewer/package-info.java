/**
 * Provides {@link org.machanism.machai.gw.reviewer.Reviewer reviewer} implementations that inspect
 * project artifacts for embedded {@code @guidance} directives and convert matching files into prompt
 * fragments for the Ghostwriter review pipeline.
 *
 * <p>The package defines a format-oriented review layer that supports Java source, Markdown,
 * HTML/XML, TypeScript, Python, PlantUML, and dedicated text guidance files. Each reviewer detects
 * guidance markers according to the syntax rules of its target format and, when guidance is present,
 * produces a normalized prompt using entries from the {@code document-prompts} resource bundle.
 *
 * <p>The central contract is {@link org.machanism.machai.gw.reviewer.Reviewer}, a service-provider
 * interface that accepts a project root directory and a candidate file, then returns either a
 * formatted prompt fragment or {@code null} when the file does not contain actionable guidance.
 * Implementations typically read source content as UTF-8, resolve project-relative paths through
 * {@link org.machanism.machai.project.layout.ProjectLayout#getRelativePath(java.io.File, java.io.File)},
 * and tailor the generated prompt to the reviewed artifact type.
 *
 * <p>Notable special cases include package-level Java review in
 * {@link org.machanism.machai.gw.reviewer.JavaReviewer}, where {@code package-info.java} yields a
 * dedicated package prompt, and standalone {@code @guidance.txt} handling in
 * {@link org.machanism.machai.gw.reviewer.TextReviewer}, which treats the containing directory as
 * the contextual target of the guidance.
 *
 * @see org.machanism.machai.gw.reviewer.Reviewer
 */
package org.machanism.machai.gw.reviewer;

/*-
 * @guidance:
 *
 * **IMPORTANT: UPDATE OR ADD JAVADOC FOR ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
 *
 * - Update Existing Javadoc and Add Missing Javadoc:
 *      - Review all classes in the folder.
 *      - Update any existing Javadoc to ensure it is accurate, comprehensive, and follows best practices.
 *      - Add Javadoc to any classes, methods, or fields where it is missing.
 *      - Ensure that all Javadoc is up-to-date and provides clear, meaningful documentation.
 * - Use Clear and Concise Descriptions:
 *      - Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 *      - Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * - Include Usage Examples Where Helpful:
 *      - Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * - Maintain Consistency and Formatting:
 *      - Follow a consistent style and structure for all Javadoc comments.
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *      - Review the Java class source code and include comprehensive Javadoc comments for all classes,
 *           methods, and fields, adhering to established best practices.
 *      - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *           and any exceptions thrown.
 *      - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;`
 *           and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc.
 *      - Do not use escaping in `{@code ...}` tags.    
 * - Use the Java Version Defined in `pom.xml`:
 *      - All code improvements and Javadoc updates must be compatible with the Java version specified in the project's `pom.xml`.
 *      - Do not use features or syntax that require a higher Java version than defined in `pom.xml`.
 */
