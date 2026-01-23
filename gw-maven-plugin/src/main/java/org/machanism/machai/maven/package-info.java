/**
 * Maven plugin goals for running the MachAI generative-workflow (GW) document processing as part of a Maven build.
 *
 * <p>
 * This package provides Mojos that can be executed from the command line or bound to Maven lifecycle phases.
 * The {@code gw} goal scans documentation sources under the project base directory and runs the MachAI workflow,
 * optionally using a configured GenAI provider/model. The {@code clean} goal removes temporary workflow artifacts
 * produced during processing.
 * </p>
 *
 * <h2>Goals</h2>
 * <ul>
 *   <li><b>{@code gw}</b> – scans configured documentation sources and runs the MachAI document workflow.</li>
 *   <li><b>{@code clean}</b> – deletes temporary workflow artifacts generated during document processing.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 *
 * <h3>Run from the command line</h3>
 * <pre>
 * mvn org.machanism.machai:gw-maven-plugin:gw -Dgw.genai=OpenAI:gpt-5 -Dgw.genai.serverId=genai
 * </pre>
 *
 * <h3>Configure in {@code pom.xml}</h3>
 * <pre>
 *  <plugin>
 *    <groupId>org.machanism.machai </groupId>
 *    <artifactId>gw-maven-plugin </artifactId>
 *    <version>${project.version} </version>
 *    <configuration>
 *      <genai>OpenAI:gpt-5 </genai>
 *      <serverId>genai </serverId>
 *    </configuration>
 *    <executions>
 *      <execution>
 *        <goals>
 *          <goal>gw </goal>
 *          <goal>clean </goal>
 *        </goals>
 *      </execution>
 *    </executions>
 *  </plugin>
 * </pre>
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
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 
 */
