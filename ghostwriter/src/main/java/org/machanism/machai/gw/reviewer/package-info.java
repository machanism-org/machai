/**
 * Provides reviewers that scan supported project artifacts for embedded
 * {@code @guidance} directives and transform matching files into normalized
 * prompt fragments for the Ghostwriter pipeline.
 *
 * <p>The primary contract is {@link org.machanism.machai.gw.reviewer.Reviewer},
 * a service-provider interface for format-specific review strategies.
 * Implementations detect guidance markers using the syntax of their target
 * formats and, when guidance is present, produce prompt fragments backed by the
 * {@code document-prompts} resource bundle.
 *
 * <p>This package contains reviewers for Java source, Markdown, HTML/XML,
 * TypeScript, Python, PlantUML, and standalone text guidance files. Most
 * reviewers read files as UTF-8 text, compute project-relative paths with
 * {@link org.machanism.machai.project.layout.ProjectLayout#getRelativePath(java.io.File, java.io.File)},
 * and return either the full source content or extracted guidance text in a
 * prompt template tailored to the reviewed format.
 *
 * <p>Specialized behavior includes handling {@code package-info.java} in
 * {@link org.machanism.machai.gw.reviewer.JavaReviewer}, support for dedicated
 * {@code @guidance.txt} files in
 * {@link org.machanism.machai.gw.reviewer.TextReviewer}, and format-aware
 * guidance detection for comments or string literals in the remaining
 * reviewers.
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
