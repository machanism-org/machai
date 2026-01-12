/**
 * Provides Mojos and core classes for integrating Bindex with Maven builds.
 * <p>
 * <strong>Package Overview:</strong>
 * <ul>
 *   <li>This package supplies plugin goals for creating, updating, registering, and cleaning Bindex indexes in Maven projects.</li>
 *   <li>Mojos coordinate AI-powered indexing and metadata registration via Bindex, utilizing GenAIProvider integration and project layout analysis.</li>
 *   <li>Core operations are encapsulated in <code>Create</code>, <code>Update</code>, <code>Register</code>, and <code>Clean</code> goals.</li>
 *   <li>The abstract base class, {@link org.machanism.machai.maven.AbstractBindexMojo}, provides shared logic for AI interactions, resource management, and Maven integration.</li>
 *   <li>Plugin supports configuration of AI models and registration endpoints, operating on standard Maven project layouts, excluding packaging type "pom".</li>
 * </ul>
 *
 * <h2>Plugin Goals and Javadoc References</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.maven.Create} — Generates a new Bindex index for a suitable Maven project.
 *    <pre>
 *    {@code
 *    mvn org.machanism.machai:bindex-maven-plugin:create
 *    }
 *    </pre>
 *   </li>
 *   <li>{@link org.machanism.machai.maven.Update} — Updates the Bindex index and resources.
 *    <pre>
 *    {@code
 *    mvn org.machanism.machai:bindex-maven-plugin:update
 *    }
 *    </pre>
 *   </li>
 *   <li>{@link org.machanism.machai.maven.Register} — Registers project Bindex data with a configured endpoint.
 *    <pre>
 *    {@code
 *    mvn org.machanism.machai:bindex-maven-plugin:register
 *    }
 *    </pre>
 *   </li>
 *   <li>{@link org.machanism.machai.maven.Clean} — Cleans up Bindex temporary files generated during plugin execution.
 *    <pre>
 *    {@code
 *    mvn org.machanism.machai:bindex-maven-plugin:clean
 *    }
 *    </pre>
 *   </li>
 * </ul>
 *
 * <h2>Main Classes</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.maven.AbstractBindexMojo} — Common dependency and resource management for the plugin goals.</li>
 *   <li>{@link org.machanism.machai.maven.Create} — Mojo for index creation.</li>
 *   <li>{@link org.machanism.machai.maven.Update} — Mojo for updating index.</li>
 *   <li>{@link org.machanism.machai.maven.Register} — Mojo for index registration.</li>
 *   <li>{@link org.machanism.machai.maven.Clean} — Mojo for cleaning temporary files.</li>
 * </ul>
 *
 * <p>
 * For further details, see the Javadoc of each class for summaries and example code snippets demonstrating plugin usage.
 * </p>
 */
package org.machanism.machai.maven;

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

