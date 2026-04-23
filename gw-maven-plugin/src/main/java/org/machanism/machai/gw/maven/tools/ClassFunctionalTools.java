package org.machanism.machai.gw.maven.tools;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Provides function tools for discovering project classes and retrieving
 * reflective details about them.
 * <p>
 * This implementation builds a dedicated {@link URLClassLoader} from the Maven
 * project's compile classpath and output directories, then exposes helper tools
 * that can be registered with a {@link Genai} provider.
 */
public class ClassFunctionalTools implements FunctionTools {

	private static final Logger logger = LoggerFactory.getLogger(ClassFunctionalTools.class);

	/**
	 * All classes visible from the constructed project class loader.
	 */
	private ImmutableSet<com.google.common.reflect.ClassPath.ClassInfo> classes;

	private Map<String, String> classPathMap = new HashMap<>();

	/**
	 * Class loader built from the Maven project classpath and output directories.
	 */
	private URLClassLoader classLoader;

	private Map<String, String> artifactMap = new HashMap<>();

	/**
	 * Creates a new instance backed by the supplied Maven project's classpath.
	 *
	 * @param project the Maven project used to resolve classpath and output
	 *                directories
	 * @throws IllegalArgumentException if the class loader or class path cannot be
	 *                                  created
	 */
	public ClassFunctionalTools(MavenProject project) {
		loadClassList(project);
		loadClassLocations(project);
	}

	/**
	 * Registers class-related tools with the given AI provider.
	 *
	 * @param provider the provider that receives the tool registrations
	 */
	@Override
	public void applyTools(Genai provider) {
		provider.addTool(
				"find_class",
				"Use this tool to find fully qualified Java class names whose short names match the provided regular expression pattern. "
						+ "Specify the 'className' property to define the pattern for matching class short names.",
				this::findClass,
				"className:string:required:Regular expression pattern to match class short names.");

		provider.addTool(
				"get_class_info",
				"Use this tool to retrieve detailed information about a Java class by its fully qualified name. "
						+ "Specify the 'className' property to obtain all available details for the class. "
						+ "Returns a structured JSON object containing class name, modifiers, superclass, interfaces, fields, constructors, methods, annotations, and class path.",
				this::getClassInfo,
				"className:string:required:Fully qualified class name to retrieve information.");
	}

	/**
	 * Finds fully qualified class names whose simple names match the supplied
	 * regular expression.
	 *
	 * @param args tool invocation arguments; the first argument is expected to be a
	 *             {@link JsonNode} containing a {@code className} property
	 * @return a comma-separated list of matching class names, or {@code Class not
	 *         found.} when no matches are available
	 */
	private String findClass(Object... args) {
		JsonNode props = (JsonNode) args[0];
		String className = props.get("className").asText();

		Pattern pattern = Pattern.compile(className);
		List<com.google.common.reflect.ClassPath.ClassInfo> list = classes.stream()
				.filter(entry -> pattern.matcher(entry.getSimpleName()).matches())
				.collect(Collectors.toList());

		String classes = "Class not found.";
		if (list != null) {
			List<String> collect = list.stream().map(e -> e.getName())
					.collect(Collectors.toList());
			classes = StringUtils.join(collect, ",");
		}

		if (logger.isInfoEnabled()) {
			logger.info("Find class: {}, Found: {}. {}", Arrays.toString(args), list.size(),
					StringUtils.abbreviate(classes, 120));
		}

		return classes;
	}

	/**
	 * Returns reflective information for the specified class.
	 * <p>
	 * The generated output may include modifiers, superclass, interfaces,
	 * non-private fields, constructors, non-private methods, and annotations.
	 *
	 * @param args tool invocation arguments; the first argument is expected to be a
	 *             {@link JsonNode} containing a {@code className} property
	 * @return a formatted textual description of the requested class, or a not
	 *         found message when the class cannot be loaded
	 */
	private JsonObject getClassInfo(Object... args) {
		JsonNode props = (JsonNode) args[0];
		if (logger.isInfoEnabled()) {
			logger.info("Get classInfo: {}", Arrays.toString(args));
		}

		String className = props.get("className").asText();
		JsonObject info = new JsonObject();

		try {
			Class<?> clazz = classLoader.loadClass(className);

			// Class name and modifiers
			info.addProperty("className", clazz.getName());
			info.addProperty("modifiers", Modifier.toString(clazz.getModifiers()));

			// Superclass
			if (clazz.getSuperclass() != null) {
				info.addProperty("superclass", clazz.getSuperclass().getName());
			}

			// Interfaces
			Class<?>[] interfaces = clazz.getInterfaces();
			JsonArray interfacesArray = new JsonArray();
			for (Class<?> iface : interfaces) {
				interfacesArray.add(iface.getName());
			}
			info.add("interfaces", interfacesArray);

			// Fields (exclude private)
			JsonArray fieldsArray = new JsonArray();
			for (Field field : clazz.getDeclaredFields()) {
				if (!Modifier.isPrivate(field.getModifiers())) {
					JsonObject fieldObj = new JsonObject();
					fieldObj.addProperty("modifiers", Modifier.toString(field.getModifiers()));
					fieldObj.addProperty("type", field.getType().getName());
					fieldObj.addProperty("name", field.getName());
					fieldsArray.add(fieldObj);
				}
			}
			info.add("fields", fieldsArray);

			// Constructors
			JsonArray constructorsArray = new JsonArray();
			for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
				JsonObject ctorObj = new JsonObject();
				ctorObj.addProperty("modifiers", Modifier.toString(constructor.getModifiers()));
				ctorObj.addProperty("name", constructor.getName());
				JsonArray paramsArray = new JsonArray();
				for (Class<?> paramType : constructor.getParameterTypes()) {
					paramsArray.add(paramType.getName());
				}
				ctorObj.add("parameterTypes", paramsArray);
				constructorsArray.add(ctorObj);
			}
			info.add("constructors", constructorsArray);

			// Methods (exclude private)
			JsonArray methodsArray = new JsonArray();
			for (Method method : clazz.getDeclaredMethods()) {
				if (!Modifier.isPrivate(method.getModifiers())) {
					JsonObject methodObj = new JsonObject();
					methodObj.addProperty("modifiers", Modifier.toString(method.getModifiers()));
					methodObj.addProperty("returnType", method.getReturnType().getName());
					methodObj.addProperty("name", method.getName());
					JsonArray paramsArray = new JsonArray();
					for (Class<?> paramType : method.getParameterTypes()) {
						paramsArray.add(paramType.getName());
					}
					methodObj.add("parameterTypes", paramsArray);
					methodsArray.add(methodObj);
				}
			}
			info.add("methods", methodsArray);

			// Annotations
			JsonArray annotationsArray = new JsonArray();
			for (Annotation annotation : clazz.getDeclaredAnnotations()) {
				annotationsArray.add(annotation.toString());
			}
			info.add("annotations", annotationsArray);

			String path = classPathMap.get(className);
			info.addProperty("path", path);

			String id = artifactMap.get(className);
			if (id != null) {
				info.addProperty("artifact", id);
			}

		} catch (ClassNotFoundException e) {
			info.addProperty("error", "Class not found: " + className);
		}
		return info;
	}

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

	private void scanClassesByPath(Set<Artifact> artifacts)
			throws IOException {
		for (Artifact artifact : artifacts) {
			String id = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
			scanClassesByPath(artifact.getFile().getAbsolutePath(), id);
		}
	}

	public void scanClassesByPath(String path, String id) throws IOException {
		try {
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

}
