package org.machanism.machai.gw.maven.tools;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.project.MavenProject;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Provides function tools for discovering project classes and retrieving
 * reflective details about them.
 * <p>
 * This implementation keeps per-project {@link ClassInfoHolder} instances keyed
 * by working directory so tool calls can resolve classes in the correct Maven
 * module context.
 * <p>
 * <b>Usage Restriction: aggregator = false</b>
 * <p>
 * This class is designed to be used only in Maven Mojo executions where
 * <code>aggregator = false</code>. When a Mojo is run as an aggregator
 * (<code>aggregator = true</code>), it operates at the reactor (multi-module)
 * level and may not have access to the correct, fully resolved classpath or
 * output directories for each individual module. Attempting to use this tool in
 * aggregator mode can result in incomplete or incorrect class discovery,
 * classloader conflicts, or missing class details, as the classpath may not
 * accurately reflect the context of a single module. For reliable and
 * predictable results, always use this tool in non-aggregator (per-module)
 * executions, where the MavenProject context is guaranteed to represent a
 * single module with its own classpath and output directories.
 */
public class ClassFunctionalTools implements FunctionTools {

	/**
	 * Message returned when a tool call is made for a project that has not been
	 * scanned and registered in this instance.
	 */
	private static final String FT_NOT_SUPPORTED_FOR_PROJECT_MSG = "The function tool don't support this function tool.";

	private static final Logger logger = LoggerFactory.getLogger(ClassFunctionalTools.class);

	/**
	 * Holds class metadata by Maven project base directory.
	 */
	private Map<File, ClassInfoHolder> classInfoProjectMap = new HashMap<File, ClassInfoHolder>();

	/**
	 * Creates a new instance backed by the supplied Maven project's classpath.
	 *
	 * @param project the Maven project used to resolve classpath and output
	 *                directories
	 */
	public ClassFunctionalTools(MavenProject project) {
		scanProjectClasses(project);
	}

	/**
	 * Creates an empty instance. Projects must be registered later through
	 * {@link #scanProjectClasses(MavenProject)} before tool invocations can resolve
	 * class information.
	 */
	public ClassFunctionalTools() {
	}

	/**
	 * Registers a project by scanning and caching class metadata for its base
	 * directory.
	 *
	 * @param project the Maven project to register
	 */
	public void scanProjectClasses(MavenProject project) {
		File basedir = project.getBasedir();

		ClassInfoHolder classInfoHolder = new ClassInfoHolder(project);
		classInfoProjectMap.put(basedir, classInfoHolder);
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
	 * @param params tool invocation arguments; the first argument is expected to be
	 *               a {@link JsonNode} containing a {@code className} property and
	 *               the second a {@link File} representing the current working
	 *               directory
	 * @return a comma-separated list of matching class names, {@code Class not
	 *         found.} when no matches are available, or the unsupported-project
	 *         message when no project context is registered
	 */
	private String findClass(Object... params) {
		File workingDir = (File) params[1];
		ClassInfoHolder classInfoHolder = classInfoProjectMap.get(workingDir);
		String classes = "Class not found.";
		if (classInfoHolder != null) {
			JsonNode props = (JsonNode) params[0];
			String className = props.get("className").asText();

			List<com.google.common.reflect.ClassPath.ClassInfo> list = classInfoHolder.findClasses(className);

			if (list != null) {
				List<String> collect = list.stream().map(e -> e.getName())
						.collect(Collectors.toList());
				classes = StringUtils.join(collect, ",");
			}

			if (logger.isInfoEnabled()) {
				logger.info("Find class: {}, Found: {}. {}", Arrays.toString(params), list.size(),
						StringUtils.abbreviate(classes, 120));
			}
		} else {
			classes = FT_NOT_SUPPORTED_FOR_PROJECT_MSG;
		}
		return classes;
	}

	/**
	 * Returns reflective information for the specified class.
	 * <p>
	 * The generated output may include modifiers, superclass, interfaces,
	 * non-private fields, constructors, non-private methods, annotations, class
	 * path, and dependency artifact coordinates.
	 *
	 * @param params tool invocation arguments; the first argument is expected to be
	 *               a {@link JsonNode} containing a {@code className} property and
	 *               the second a {@link File} representing the current working
	 *               directory
	 * @return a JSON object describing the requested class or containing an
	 *         {@code error} property when the class or project context cannot be
	 *         resolved
	 */
	private JsonObject getClassInfo(Object... params) {
		JsonObject info = new JsonObject();

		File workingDir = (File) params[1];
		ClassInfoHolder classInfoHolder = classInfoProjectMap.get(workingDir);
		if (classInfoHolder != null) {
			JsonNode props = (JsonNode) params[0];
			if (logger.isInfoEnabled()) {
				logger.info("Get classInfo: {}", Arrays.toString(params));
			}

			String className = props.get("className").asText();

			try {
				Class<?> clazz = classInfoHolder.loadClass(className);

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

				String path = classInfoHolder.getClassPath(className);
				info.addProperty("path", path);

				String id = classInfoHolder.getArtifactId(className);
				if (id != null) {
					info.addProperty("artifact", id);
				}

			} catch (ClassNotFoundException e) {
				info.addProperty("error", "Class not found: " + className);
			}
		} else {
			info.addProperty("error", FT_NOT_SUPPORTED_FOR_PROJECT_MSG);
		}
		return info;
	}

}
