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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class ClassFunctionalTools implements FunctionTools {

	private static final Logger logger = LoggerFactory.getLogger(ClassFunctionalTools.class);

	private static Map<String, Map<String, Map<String, List<ClassInfo>>>> scoppedClassMap = new HashMap<>();

	private static Map<String, List<ClassInfo>> classMap = new HashMap<>();

	private ImmutableSet<com.google.common.reflect.ClassPath.ClassInfo> classes;

	private URLClassLoader classLoader;

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

	@Override
	public void applyTools(Genai provider) {
		provider.addTool(
				"find_class",
				"Finds and returns a list of fully qualified Java class names that match the provided regular expression pattern. Use the 'className' property to specify the pattern for matching class short names.",
				this::findClass,
				"className:string:required:Regular expression pattern to match class short names.");

		provider.addTool(
				"get_class_info",
				"Retrieves detailed information about a Java class by its fully qualified name. Provide the 'className' property to get all class details.",
				this::getClassInfo,
				"className:string:required:Fully qualified class name to retrieve information.");
	}

	private String findClass(Object... args) {
		JsonNode props = (JsonNode) args[0];
		if (logger.isInfoEnabled()) {
			logger.info("Find class: {}", Arrays.toString(args));
		}

		String className = props.get("className").asText();

		Pattern pattern = Pattern.compile(className);
		List<com.google.common.reflect.ClassPath.ClassInfo> list = classes.stream()
				.filter(entry -> pattern.matcher(entry.getSimpleName()).matches())
				.collect(Collectors.toList());

		String join = "Class not found.";
		if (list != null) {
			List<String> collect = list.stream().map(e -> e.getName())
					.collect(Collectors.toList());
			join = StringUtils.join(collect, ",");
		}
		return join;
	}

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
