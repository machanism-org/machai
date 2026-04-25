/**
 * Provides helper types that expose Java class discovery and reflection metadata
 * as function tools for the Ghostwriter Maven plugin.
 * <p>
 * The package centers on scanning a Maven project's compile and output
 * classpaths, locating classes by simple-name pattern, and returning structured
 * metadata such as modifiers, members, annotations, class origin paths, source
 * file locations, and dependency artifact coordinates. These types are intended
 * to support AI-assisted workflows that need controlled access to project and
 * dependency class information during plugin execution.
 */
package org.machanism.machai.gw.maven.tools;
