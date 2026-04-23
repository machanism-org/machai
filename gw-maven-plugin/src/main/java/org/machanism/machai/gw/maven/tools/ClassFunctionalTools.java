package org.machanism.machai.gw.maven.tools;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.project.MavenProject;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

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

	/**
	 * Class loader built from the Maven project classpath and output directories.
	 */
	private URLClassLoader classLoader;

	/**
	 * Creates a new instance backed by the supplied Maven project's classpath.
	 *
	 * @param project the Maven project used to resolve classpath and output
	 *                directories
	 * @throws IllegalArgumentException if the class loader or class path cannot be
	 *                                  created
	 */
	public ClassFunctionalTools(MavenProject project) {
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
			    + "Specify the 'className' property to obtain all available details for the class.",
			    this::getClassInfo,
			    "className:string:required:Fully qualified class name to retrieve information."
			);
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
	private String getClassInfo(Object... args) {
		JsonNode props = (JsonNode) args[0];
		if (logger.isInfoEnabled()) {
			logger.info("Get classInfo: {}", Arrays.toString(args));
		}

		String className = props.get("className").asText();

		StringBuilder info = new StringBuilder();
		try {
			Class<?> clazz = classLoader.loadClass(className);

			// Class name and modifiers
			info.append("Class: ").append(clazz.getName()).append("\n");
			info.append("Modifiers: ").append(Modifier.toString(clazz.getModifiers())).append("\n");

			// Superclass
			if (clazz.getSuperclass() != null) {
				info.append("Superclass: ").append(clazz.getSuperclass().getName()).append("\n");
			}

			// Interfaces
			Class<?>[] interfaces = clazz.getInterfaces();
			if (interfaces.length > 0) {
				info.append("Interfaces: ").append(Arrays.toString(
						Arrays.stream(interfaces).map(Class::getName).toArray())).append("\n");
			}

			// Fields (exclude private)
			info.append("Fields:\n");
			for (Field field : clazz.getDeclaredFields()) {
				if (!Modifier.isPrivate(field.getModifiers())) {
					info.append("  ").append(Modifier.toString(field.getModifiers()))
							.append(" ").append(field.getType().getName())
							.append(" ").append(field.getName()).append("\n");
				}
			}

			// Constructors
			info.append("Constructors:\n");
			for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
				info.append("  ").append(Modifier.toString(constructor.getModifiers()))
						.append(" ").append(constructor.getName())
						.append(Arrays.toString(constructor.getParameterTypes())).append("\n");
			}

			// Methods (exclude private)
			info.append("Methods:\n");
			for (Method method : clazz.getDeclaredMethods()) {
				if (!Modifier.isPrivate(method.getModifiers())) {
					info.append("  ").append(Modifier.toString(method.getModifiers()))
							.append(" ").append(method.getReturnType().getName())
							.append(" ").append(method.getName())
							.append(Arrays.toString(method.getParameterTypes())).append("\n");
				}
			}

			// Annotations
			info.append("Annotations:\n");
			for (Annotation annotation : clazz.getDeclaredAnnotations()) {
				info.append("  ").append(annotation.toString()).append("\n");
			}

		} catch (ClassNotFoundException e) {
			info.append("Class not found: ").append(className).append("\n");
		}
		return info.toString();
	}
}
