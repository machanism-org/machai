/* @guidance: >>> ${guidances}/package-info-javadoc.md */
/**
 * Supplies Maven-integrated function tools for discovering Java classes and
 * inspecting their reflective metadata.
 *
 * <p>The package is organized around two collaborating types:</p>
 * <ul>
 * <li>{@link org.machanism.machai.gw.maven.tools.ClassFunctionalTools} is the
 *     function-tool facade exposed to callers. It associates each registered
 *     Maven project base directory with a metadata holder and provides the
 *     {@code find-class} and {@code get-class-info} operations.</li>
 * <li>{@link org.machanism.machai.gw.maven.tools.ClassInfoHolder} owns the
 *     project-specific class loader, classpath scan, class-origin mappings, and
 *     source-path lookup logic.</li>
 * </ul>
 *
 * <p>When a project is registered, the facade stores a holder under
 * {@link org.apache.maven.project.MavenProject#getBasedir()}. The holder lazily
 * creates a dedicated {@link java.net.URLClassLoader} using the project's
 * resolved compile classpath, test output directory, and main output directory.
 * Guava's classpath scanner then supplies the classes visible to discovery and
 * class loading. The registration map is mutable and unsynchronized; callers
 * that register projects or invoke tools concurrently must provide their own
 * synchronization.</p>
 *
 * <p>Origin metadata is collected separately from class discovery. The holder
 * scans the main output directory and resolved dependency artifacts, recording
 * paths and, for dependency classes, Maven coordinates when available. Only
 * loadable public and protected classes are recorded in those origin maps.
 * Missing or unloadable entries are skipped. Source lookup searches only the
 * project's compile source roots; a nested class is mapped to the source file
 * of its top-level class. Re-register a project after changing compiled output,
 * dependencies, or classpath configuration so subsequent requests use a fresh
 * holder.</p>
 *
 * <p>{@code findClass} applies a regular expression to the simple name of each
 * discovered class and returns fully qualified names. It rejects an
 * unregistered project, an invalid pattern, an empty result, or more than ten
 * matches. {@code getClassInfo} loads a fully qualified name and returns a map
 * containing the class name, modifiers, direct superclass, direct interfaces,
 * declared non-private fields and methods, all declared constructors,
 * annotations, and any available path, artifact, and source metadata. The
 * reflective member lists describe declarations on the requested class rather
 * than inherited members.</p>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * MavenProject project = ...;
 * ClassFunctionalTools tools = new ClassFunctionalTools(project);
 * List&lt;String&gt; matches = tools.findClass(&quot;.*Mojo&quot;, project.getBasedir());
 * Map&lt;String, Object&gt; metadata = tools.getClassInfo(matches.get(0),
 *         project.getBasedir());
 * </pre>
 *
 * @see org.machanism.machai.gw.maven.tools.ClassFunctionalTools
 * @see org.machanism.machai.gw.maven.tools.ClassInfoHolder
 */
package org.machanism.machai.gw.maven.tools;
