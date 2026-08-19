/*-
 * @guidance:
 * Use your file-modification tools to update or add Javadoc comments to all Java classes and package-level files in this folder.
 *
 * 1. Class, Interface, Method, and Field Documentation
 *    - If there is no javadoc documentation defined, create it.
 *    - Review each Java file and add/update descriptive Javadoc for all public and protected classes, interfaces, methods, and fields.
 *    - Explain the direct functional purpose, all parameters (`@param`), return values (`@return`), and exceptions thrown (`@throws`).
 *    - **AI Metaprogramming Metadata:** If a class or method is annotated with `@Tool`, `@Prompt`, or `@Resource`, explicitly document its role as a "Functional AI Tool", "Prompt Template", or "Contextual Resource" respectively.
 * 2. Package-Level Documentation (`package-info.java`)
 *    - Analyze all Java files inside this folder.
 *    - Generate or update `package-info.java` with a comprehensive Javadoc block that explains the package's architecture, relationships, and usage.
 *    - Place this Javadoc immediately before the `package` declaration.
 * 3. Formatting, Examples, & Syntax Safety (Crucial)
 *      - Review the Java class source code and include comprehensive Javadoc comments for all classes,
 *           methods, and fields, adhering to established best practices.
 *      - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *           and any exceptions thrown.
 *      - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;`
 *           and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc.
 *      - Do not use escaping in `{@code ...}` tags. 
 *      - Escape the closing javadoc tag in javadoc content, as it was breaking javadoc compilation.
 */

/**
 * Provides the AI-facing integration layer for Bindex (bundle index) metadata.
 * <p>
 * The package has two cooperating parts. {@link BindexFunctionTools} is the
 * {@link org.machanism.machai.ai.tools.FunctionTools} implementation: its
 * {@code @Tool}-annotated methods retrieve descriptors by identifier or URL,
 * recommend libraries, and register descriptors through a configured
 * {@link org.machanism.machai.bindex.core.BindexRepository}. Its
 * {@code @Resource}-annotated method exposes the Bindex JSON Schema, while its
 * {@code @Prompt}-annotated method exposes the Markdown template used to
 * generate Bindex files. These annotations allow an AI host to discover the
 * operations, contextual resource, and prompt template without coupling the
 * host to the implementation details.
 * </p>
 * <p>
 * {@link GraphqlJsonFilter} is the payload-projection support component used
 * by the retrieval operation. It parses a GraphQL selection document and
 * copies the requested top-level fields from serialized Bindex data. Nested
 * selections are accepted by the parser but are not recursively projected;
 * for example, {@code { name version classification { languages } }} selects
 * the top-level {@code name}, {@code version}, and {@code classification}
 * fields.
 * </p>
 * <p>
 * A typical integration supplies a project directory and
 * {@link org.machanism.macha.core.commons.configurator.Configurator}, invokes
 * a discovered tool method, and optionally provides a GraphQL selection when
 * a smaller response is preferred. Registration delegates persistence and
 * recommendation to {@link org.machanism.machai.bindex.core.Picker}, keeping
 * repository access and AI-facing method metadata separate.
 * </p>
 *
 * @see BindexFunctionTools
 * @see GraphqlJsonFilter
 * @see org.machanism.machai.ai.tools.FunctionTools
 * @see org.machanism.machai.bindex.core.BindexRepository
 * @see org.machanism.machai.bindex.core.Picker
 */
package org.machanism.machai.bindex.ai.tools;
