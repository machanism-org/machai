package org.machanism.machai.gw.maven.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

/**
 * Holds class discovery and lookup data for a single Maven project.
 * <p>
 * This class builds a dedicated {@link URLClassLoader} from the project's
 * compile classpath together with its main and test output directories. It then
 * scans visible classes and records the physical location and artifact
 * coordinates for public and protected classes that can be loaded from that
 * classpath.
 */
public class ClassInfoHolder {

	private static final Logger logger = LoggerFactory.getLogger(ClassInfoHolder.class);

	/**
	 * All classes visible from the constructed project class loader.
	 */
	private ImmutableSet<com.google.common.reflect.ClassPath.ClassInfo> classes;

	/**
	 * Maps fully qualified class names to the filesystem path or jar path from
	 * which they were resolved.
	 */
	private Map<String, String> classPathMap = new HashMap<>();

	/**
	 * Class loader built from the Maven project classpath and output directories.
	 */
	private URLClassLoader classLoader;

	/**
	 * Maps fully qualified class names to Maven artifact coordinates in the form
	 * {@code groupId:artifactId:version} when the class comes from a dependency.
	 */
	private Map<String, String> artifactMap = new HashMap<>();

	/**
	 * Maven project used as the source of classpath, output directory, and
	 * dependency metadata.
	 */
	private MavenProject project;

	/**
	 * Creates a holder for the supplied Maven project and eagerly initializes class
	 * discovery metadata.
	 *
	 * @param project the Maven project whose classpath and artifacts are inspected
	 */
	public ClassInfoHolder(MavenProject project) {
		this.project = project;
	}

	/**
	 * Lazily initializes class discovery state on first access.
	 */
	private void init() {
		if (classes == null) {
			loadClassList(project);
			loadClassLocations(project);
		}
	}

	/**
	 * Builds the project class loader and captures all classes visible from it.
	 *
	 * @param project the Maven project providing compile classpath and build output
	 *                directories
	 * @throws IllegalArgumentException if the class loader or class path cannot be
	 *                                  initialized
	 */
	private void loadClassList(MavenProject project) {
		try {
			List<String> compileClasspathElements = project.getCompileClasspathElements();
			String testOutputDirectory = project.getBuild().getTestOutputDirectory();

			String outputDirectory = project.getBuild().getOutputDirectory();

			List<String> arrayList = new ArrayList<>();
			arrayList.addAll(compileClasspathElements);
			arrayList.add(testOutputDirectory);
			arrayList.add(outputDirectory);

			URL[] urls = arrayList.stream().map(p -> {
				try {
					return new File(p).toURI().toURL();
				} catch (MalformedURLException e) {
					throw new IllegalArgumentException(e);
				}
			}).collect(Collectors.toList())
					.toArray(new URL[0]);

			classLoader = URLClassLoader.newInstance(urls);
			ClassPath classPath = ClassPath.from(classLoader);
			classes = classPath.getAllClasses();

		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Scans the project's output directory and resolved dependency artifacts to map
	 * classes back to their origin.
	 *
	 * @param project the Maven project whose output directory and dependencies are
	 *                scanned
	 * @throws IllegalArgumentException if class location scanning fails
	 */
	private void loadClassLocations(MavenProject project) {
		try {
			String outputDirectory = project.getBuild().getOutputDirectory();
			Set<Artifact> artifacts = project.getArtifacts();
			scanClassesByPath(outputDirectory, null);
			scanClassesByPath(artifacts);

		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Scans each resolved dependency artifact for loadable classes.
	 *
	 * @param artifacts the Maven artifacts to inspect
	 * @throws IOException if reading an artifact fails
	 */
	private void scanClassesByPath(Set<Artifact> artifacts)
			throws IOException {
		for (Artifact artifact : artifacts) {
			String id = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
			scanClassesByPath(artifact.getFile().getAbsolutePath(), id);
		}
	}

	/**
	 * Scans a directory of compiled classes or a jar file and records eligible
	 * classes that can be loaded by this holder's class loader.
	 * <p>
	 * Only public and protected classes are added to the internal path and artifact
	 * maps. Missing paths are ignored.
	 *
	 * @param path the directory or jar file to scan
	 * @param id   the dependency coordinates associated with the path, or
	 *             {@code null} for project output classes
	 * @throws IOException if the path cannot be read
	 */
	public void scanClassesByPath(String path, String id) throws IOException {
		try {
			init();

			File artifact = new File(path);

			List<String> classList = new ArrayList<>();

			if (artifact.isFile()) {
				try (JarFile jar = new JarFile(artifact)) {
					for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
						JarEntry entry = entries.nextElement();
						String name = entry.getName();
						classList.add(name);
					}
				}
			} else {
				Path pathDir = Paths.get(path);

				Files.walk(pathDir)
						.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".class"))
						.forEach(p -> classList
								.add(p.toString().replace("\\", "/").replace(path.replace("\\", "/") + "/", "")));
			}

			for (String name : classList) {
				if (Strings.CS.endsWith(name, ".class")
						&& !Strings.CS.startsWithAny(name, "META-INF", "module-info")) {
					try {
						name = StringUtils.substringBeforeLast(name, ".");
						String className = StringUtils.substringAfterLast(name, "/");
						String packageName = StringUtils.substringBeforeLast(name, "/");

						packageName = StringUtils.replaceChars(packageName, '/', '.');

						String fullClassName = packageName + "." + className;
						Class<?> class1 = classLoader.loadClass(fullClassName);

						int modifiers = class1.getModifiers();
						if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
							classPathMap.put(fullClassName, path);
							artifactMap.put(fullClassName, id);
						}
					} catch (Throwable e) {
						logger.debug("Class {}, Error: {}", name, e.getMessage());
					}
				}
			}
		} catch (NoSuchFileException e) {
			// TODO: handle exception
		}
	}

	/**
	 * Finds classes whose simple names match the supplied regular expression.
	 *
	 * @param className the regular expression applied to simple class names
	 * @return matching class metadata entries
	 */
	public List<ClassInfo> findClasses(String className) {
		init();

		Pattern pattern = Pattern.compile(className);
		return classes.stream()
				.filter(entry -> pattern.matcher(entry.getSimpleName()).matches())
				.collect(Collectors.toList());
	}

	/**
	 * Loads a class by its fully qualified name using the holder's class loader.
	 *
	 * @param className the fully qualified class name
	 * @return the resolved class
	 * @throws ClassNotFoundException if the class cannot be loaded
	 */
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		init();
		return classLoader.loadClass(className);
	}

	/**
	 * Returns the originating path for the supplied fully qualified class name.
	 *
	 * @param className the fully qualified class name
	 * @return the directory or jar path for the class, or {@code null} if unknown
	 */
	public String getClassPath(String className) {
		init();
		return classPathMap.get(className);
	}

	/**
	 * Returns the Maven artifact coordinates associated with the supplied fully
	 * qualified class name.
	 *
	 * @param className the fully qualified class name
	 * @return artifact coordinates in {@code groupId:artifactId:version} form, or
	 *         {@code null} when the class does not originate from a dependency
	 */
	public String getArtifactId(String className) {
		init();
		return artifactMap.get(className);
	}

}
