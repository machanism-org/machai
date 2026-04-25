package org.machanism.machai.gw.maven.tools;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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
 * Provides function-tool integrations for discovering Java classes and reading
 * reflective metadata from the classpath of one or more Maven projects.
 * <p>
 * Instances maintain a cache of {@link ClassInfoHolder} objects keyed by Maven
 * project base directory, allowing tool invocations to resolve classes relative
 * to the current working project.
 */
public class ClassFunctionalTools implements FunctionTools {

	/**
	 * Message returned when a tool call is made for a project that has not been
	 * scanned and registered in this instance.
	 */
	private static final String FT_NOT_SUPPORTED_FOR_PROJECT_MSG = "The function tool don't support this function tool.";

	// SonarQube rule java:S1192: extracted duplicated JSON property names.
	private static final String CLASS_NAME_PROPERTY = "className";

	// SonarQube rule java:S1192: extracted duplicated JSON property names.
	private static final String MODIFIERS_PROPERTY = "modifiers";

	private static final Logger logger = LoggerFactory.getLogger(ClassFunctionalTools.class);

	/**
	 * Holds class metadata by Maven project base directory.
	 */
	private Map<File, ClassInfoHolder> classInfoProjectMap = new HashMap<>();

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
						+ "Specify the 'className' property to define the pattern for matching class short names. "
						+ "Note: The results reflect the initial state of the project and may become outdated after code or configuration changes.",
				this::findClass,
				"className:string:required:Regular expression pattern to match class short names.");

		provider.addTool(
				"get_class_info",
				"Use this tool to retrieve detailed information about a Java class by its fully qualified name. "
						+ "Specify the 'className' property to obtain all available details for the class. "
						+ "Returns a structured JSON object containing class name, modifiers, superclass, interfaces, fields, constructors, methods, annotations, and class path. "
						+ "Note: The information reflects the initial state of the project and may become outdated after code or configuration changes.",
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
	public String findClass(Object... params) {
		File workingDir = (File) params[1];
		ClassInfoHolder classInfoHolder = classInfoProjectMap.get(workingDir);
		String classes = "Class not found.";
		if (classInfoHolder != null) {
			JsonNode props = (JsonNode) params[0];
			String className = props.get(CLASS_NAME_PROPERTY).asText();

			List<com.google.common.reflect.ClassPath.ClassInfo> list = classInfoHolder.findClasses(className);
			if (list != null) {
				List<String> collect = list.stream().map(e -> e.getName())
						.collect(Collectors.toList());
				classes = StringUtils.join(collect, ",");
			}

			logFindClass(params, list, classes);
		} else {
			classes = FT_NOT_SUPPORTED_FOR_PROJECT_MSG;
		}
		return classes;
	}

	private void logFindClass(Object[] params, List<com.google.common.reflect.ClassPath.ClassInfo> list, String classes) {
		if (logger.isInfoEnabled()) {
			int foundSize = list == null ? 0 : list.size();
			logger.info("Find class: {}, Found: {}. {}", Arrays.toString(params), foundSize,
					StringUtils.abbreviate(classes, 120));
		}
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
	public JsonObject getClassInfo(Object... params) {
		JsonObject info = new JsonObject();

		File workingDir = (File) params[1];
		ClassInfoHolder classInfoHolder = classInfoProjectMap.get(workingDir);
		if (classInfoHolder != null) {
			JsonNode props = (JsonNode) params[0];
			if (logger.isInfoEnabled()) {
				logger.info("Get classInfo: {}", Arrays.toString(params));
			}

			String className = props.get(CLASS_NAME_PROPERTY).asText();

			try {
				Class<?> clazz = classInfoHolder.loadClass(className);
				populateClassInfo(info, classInfoHolder, className, clazz);
			} catch (ClassNotFoundException e) {
				info.addProperty("error", "Class not found: " + className);
			}
		} else {
			info.addProperty("error", FT_NOT_SUPPORTED_FOR_PROJECT_MSG);
		}
		return info;
	}

	private void populateClassInfo(JsonObject info, ClassInfoHolder classInfoHolder, String className, Class<?> clazz) {
		info.addProperty(CLASS_NAME_PROPERTY, clazz.getName());
		info.addProperty(MODIFIERS_PROPERTY, Modifier.toString(clazz.getModifiers()));
		addSuperclass(info, clazz);
		addInterfaces(info, clazz);
		addFields(info, clazz);
		addConstructors(info, clazz);
		addMethods(info, clazz);
		addAnnotations(info, clazz);
		addLocationMetadata(info, classInfoHolder, className);
	}

	private void addSuperclass(JsonObject info, Class<?> clazz) {
		if (clazz.getSuperclass() != null) {
			info.addProperty("superclass", clazz.getSuperclass().getName());
		}
	}

	private void addInterfaces(JsonObject info, Class<?> clazz) {
		JsonArray interfacesArray = new JsonArray();
		for (Class<?> iface : clazz.getInterfaces()) {
			interfacesArray.add(iface.getName());
		}
		info.add("interfaces", interfacesArray);
	}

	private void addFields(JsonObject info, Class<?> clazz) {
		JsonArray fieldsArray = new JsonArray();
		forEachNonPrivate(clazz.getDeclaredFields(), field -> {
			JsonObject fieldObj = new JsonObject();
			fieldObj.addProperty(MODIFIERS_PROPERTY, Modifier.toString(field.getModifiers()));
			fieldObj.addProperty("type", field.getType().getName());
			fieldObj.addProperty("name", field.getName());
			fieldsArray.add(fieldObj);
		});
		info.add("fields", fieldsArray);
	}

	private void addConstructors(JsonObject info, Class<?> clazz) {
		JsonArray constructorsArray = new JsonArray();
		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			JsonObject ctorObj = new JsonObject();
			ctorObj.addProperty(MODIFIERS_PROPERTY, Modifier.toString(constructor.getModifiers()));
			ctorObj.addProperty("name", constructor.getName());
			ctorObj.add("parameterTypes", toParameterTypes(constructor.getParameterTypes()));
			constructorsArray.add(ctorObj);
		}
		info.add("constructors", constructorsArray);
	}

	private void addMethods(JsonObject info, Class<?> clazz) {
		JsonArray methodsArray = new JsonArray();
		forEachNonPrivate(clazz.getDeclaredMethods(), method -> {
			JsonObject methodObj = new JsonObject();
			methodObj.addProperty(MODIFIERS_PROPERTY, Modifier.toString(method.getModifiers()));
			methodObj.addProperty("returnType", method.getReturnType().getName());
			methodObj.addProperty("name", method.getName());
			methodObj.add("parameterTypes", toParameterTypes(method.getParameterTypes()));
			methodsArray.add(methodObj);
		});
		info.add("methods", methodsArray);
	}

	private void addAnnotations(JsonObject info, Class<?> clazz) {
		JsonArray annotationsArray = new JsonArray();
		for (Annotation annotation : clazz.getDeclaredAnnotations()) {
			annotationsArray.add(annotation.toString());
		}
		info.add("annotations", annotationsArray);
	}

	private void addLocationMetadata(JsonObject info, ClassInfoHolder classInfoHolder, String className) {
		String path = classInfoHolder.getClassPath(className);
		info.addProperty("path", path);

		String id = classInfoHolder.getArtifactId(className);
		if (id != null) {
			info.addProperty("artifact", id);
		}

		String sourcePath = classInfoHolder.getSourcePath(className);
		if (sourcePath != null) {
			info.addProperty("sourcePath", sourcePath);
		}
	}

	private JsonArray toParameterTypes(Class<?>[] parameterTypes) {
		JsonArray paramsArray = new JsonArray();
		for (Class<?> paramType : parameterTypes) {
			paramsArray.add(paramType.getName());
		}
		return paramsArray;
	}

	private <T extends Member> void forEachNonPrivate(T[] members, Consumer<T> consumer) {
		for (T member : members) {
			if (!Modifier.isPrivate(member.getModifiers())) {
				consumer.accept(member);
			}
		}
	}

}
