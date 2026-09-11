/* @guidance: >>> ${guidances}/package-info-javadoc.md */
/**
 * Provides function-tool implementations that expose controlled Java class
 * discovery and reflection metadata to Ghostwriter Maven plugin workflows.
 * <p>
 * {@link org.machanism.machai.gw.maven.tools.ClassFunctionalTools} is the
 * package entry point used by the plugin's function-tool infrastructure. It
 * registers Maven projects and serves requests to find classes by simple-name
 * regular expression or to describe a class. For each registered project,
 * {@link org.machanism.machai.gw.maven.tools.ClassInfoHolder} lazily builds a
 * class loader from the compile classpath and the main and test output
 * directories, indexes visible classes, and associates eligible classes with
 * their output path or dependency artifact coordinates.
 * </p>
 * <p>
 * Class-information responses include the resolved class name and modifiers,
 * direct superclass and interfaces, declared annotations, constructors, and
 * non-private fields and methods. When available, they also include the
 * originating classpath location, dependency coordinates, and the matching
 * source file beneath the project's compile source roots. Discovery searches
 * match regular expressions against simple class names and are deliberately
 * bounded by the function tool to prevent overly broad responses.
 * </p>
 * <p>
 * A caller must first register each Maven project before querying it. The
 * project base directory supplied to a query must be the same directory used
 * when the project was registered. Class metadata represents the classpath at
 * scan time; callers should register the project again after changing compiled
 * output, dependencies, or classpath configuration.
 * </p>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * MavenProject project = ...;
 * ClassFunctionalTools tools = new ClassFunctionalTools(project);
 * List&lt;String&gt; matches = tools.findClass(".*Mojo", project.getBasedir());
 * Map&lt;String, Object&gt; metadata = tools.getClassInfo(matches.get(0),
 *         project.getBasedir());
 * </pre>
 *
 * @see org.machanism.machai.gw.maven.tools.ClassFunctionalTools
 * @see org.machanism.machai.gw.maven.tools.ClassInfoHolder
 */
package org.machanism.machai.gw.maven.tools;
