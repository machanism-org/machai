/**
 * Provides core classes for AI-powered project indexing, registration, and Bindex document generation.
 * <p>
 * This package contains the main mechanisms and service classes for the Machanism AI Bindex workflow, including:
 * <ul>
 *     <li>Assembly operations for Bindex projects via GenAIProvider and configuration prompts.</li>
 *     <li>Registration and update of Bindex documents with semantic queries and dependency management.</li>
 *     <li>Builder factories and project processors for generating and updating Bindex manifests across 
 *         multiple supported project layouts (Maven, JavaScript/TypeScript/Vue, Python, and generic).</li>
 *     <li>Picker logic for advanced semantic search, embedding, and MongoDB-integrated retrieval of Bindex instances.</li>
 * </ul>
 * <p>
 * Typical usage involves creating a builder or processor, supplying a GenAIProvider and layout, and calling 
 * the relevant assembly or registration methods to automate project analysis and Bindex creation.
 * <p>
 * Example workflow:
 * <pre>
 *     // Assemble Bindex from project and register
 *     GenAIProvider provider = ...;
 *     ProjectLayout layout = ...; // MavenProjectLayout, JScriptProjectLayout, PythonProjectLayout, etc.
 *     BindexCreator creator = new BindexCreator(provider);
 *     creator.processFolder(layout);
 *     try (BindexRegister register = new BindexRegister(provider, dbUrl)) {
 *         register.processFolder(layout);
 *     }
 * </pre>
 * <p>
 * See individual classes for usage and construction of semantic queries, builder chains,
 * prompt integration, and support for advanced project context aggregation.
 *
 * Classes in this package:
 * <ul>
 *     <li>{@link org.machanism.machai.bindex.ApplicationAssembly}</li>
 *     <li>{@link org.machanism.machai.bindex.BindexBuilderFactory}</li>
 *     <li>{@link org.machanism.machai.bindex.BindexCreator}</li>
 *     <li>{@link org.machanism.machai.bindex.BindexProjectProcessor}</li>
 *     <li>{@link org.machanism.machai.bindex.BindexRegister}</li>
 *     <li>{@link org.machanism.machai.bindex.Picker}</li>
 * </ul>
 * <p>
 * For project layout builders and extended context prompts, see {@link org.machanism.machai.bindex.builder}.
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
 * 
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 		- Use proper Markdown or HTML formatting for readability.
 * 
 * -  Update Javadoc with Code Changes:
 * 		- Revise Javadoc comments whenever code is modified to ensure documentation remains accurate and up to date.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */
