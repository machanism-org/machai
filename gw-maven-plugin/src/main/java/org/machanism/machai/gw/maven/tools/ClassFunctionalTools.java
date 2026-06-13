package org.machanism.machai.gw.maven.tools;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.project.MavenProject;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.ToolParam;

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
	 * Finds fully qualified class names whose simple names match the supplied
	 * regular expression.
	 */
	@Tool(name = "find_class", description = "Use this tool to find fully qualified Java class names whose short names match the provided regular expression pattern. "
			+ "Specify the 'className' property to define the pattern for matching class short names. "
			+ "Note: The results reflect the initial state of the project and may become outdated after code or configuration changes.")
	public String findClass(
			@ToolParam(name = "className", description = "Regular expression pattern to match class short names.") String className,
			@ToolParam(name = "projectDir", description = "The project dir.") File projectDir) {
		ClassInfoHolder classInfoHolder = classInfoProjectMap.get(projectDir);
		String classes = "Class not found.";
		if (classInfoHolder != null) {
			List<com.google.common.reflect.ClassPath.ClassInfo> list = classInfoHolder.findClasses(className);

			if (list != null && !list.isEmpty()) {
				List<String> collect = list.stream().map(e -> e.getName())
						.collect(Collectors.toList());
				classes = StringUtils.join(collect, ",");
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
	 *               a {@link HashMap} containing a {@code className} property and
	 *               the second a {@link File} representing the current working
	 *               directory
	 * @return a HashMap describing the requested class or containing an
	 *         {@code error} property when the class or project context cannot be
	 *         resolved
	 */
	@Tool(name = "get_class_info", description = "Use this tool to retrieve detailed information about a Java class by its fully qualified name. "
			+ "Specify the 'className' property to obtain all available details for the class. "
			+ "Returns a structured JSON object containing class name, modifiers, superclass, interfaces, fields, constructors, methods, annotations, and class path. "
			+ "Note: The information reflects the initial state of the project and may become outdated after code or configuration changes.")
	public Map<String, Object> getClassInfo(
			@ToolParam(name = "className", description = "Fully qualified class name to retrieve information.") String className,
			@ToolParam(name = "projectDir", description = "The project dir.") File projectDir) {
		HashMap<String, Object> info = new HashMap<>();
		ClassInfoHolder classInfoHolder = classInfoProjectMap.get(projectDir);
		if (classInfoHolder != null) {
			try {
				Class<?> clazz = classInfoHolder.loadClass(className);
				populateClassInfo(info, classInfoHolder, className, clazz);
			} catch (ClassNotFoundException e) {
				info.put("error", "Class not found: " + className);
			}
		} else {
			info.put("error", FT_NOT_SUPPORTED_FOR_PROJECT_MSG);
		}
		return info;
	}

	private void populateClassInfo(Map<String, Object> info, ClassInfoHolder classInfoHolder, String className,
			Class<?> clazz) {
		info.put(CLASS_NAME_PROPERTY, clazz.getName());
		info.put(MODIFIERS_PROPERTY, Modifier.toString(clazz.getModifiers()));
		addSuperclass(info, clazz);
		addInterfaces(info, clazz);
		addFields(info, clazz);
		addConstructors(info, clazz);
		addMethods(info, clazz);
		addAnnotations(info, clazz);
		addLocationMetadata(info, classInfoHolder, className);
	}

	private void addSuperclass(Map<String, Object> info, Class<?> clazz) {
		if (clazz.getSuperclass() != null) {
			info.put("superclass", clazz.getSuperclass().getName());
		}
	}

	private void addInterfaces(Map<String, Object> info, Class<?> clazz) {
		List<String> interfacesList = Arrays.stream(clazz.getInterfaces())
				.map(Class::getName)
				.collect(Collectors.toList());
		info.put("interfaces", interfacesList);
	}

	private void addFields(Map<String, Object> info, Class<?> clazz) {
		List<Map<String, Object>> fieldsList = new ArrayList<>();
		forEachNonPrivate(clazz.getDeclaredFields(), field -> {
			Map<String, Object> fieldObj = new HashMap<>();
			fieldObj.put(MODIFIERS_PROPERTY, Modifier.toString(field.getModifiers()));
			fieldObj.put("type", field.getType().getName());
			fieldObj.put("name", field.getName());
			fieldsList.add(fieldObj);
		});
		info.put("fields", fieldsList);
	}

	private void addConstructors(Map<String, Object> info, Class<?> clazz) {
		List<Map<String, Object>> constructorsList = new ArrayList<>();
		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			Map<String, Object> ctorObj = new HashMap<>();
			ctorObj.put(MODIFIERS_PROPERTY, Modifier.toString(constructor.getModifiers()));
			ctorObj.put("name", constructor.getName());
			ctorObj.put("parameterTypes", toParameterTypes(constructor.getParameterTypes()));
			constructorsList.add(ctorObj);
		}
		info.put("constructors", constructorsList);
	}

	private void addMethods(Map<String, Object> info, Class<?> clazz) {
		List<Map<String, Object>> methodsList = new ArrayList<>();
		forEachNonPrivate(clazz.getDeclaredMethods(), method -> {
			Map<String, Object> methodObj = new HashMap<>();
			methodObj.put(MODIFIERS_PROPERTY, Modifier.toString(method.getModifiers()));
			methodObj.put("returnType", method.getReturnType().getName());
			methodObj.put("name", method.getName());
			methodObj.put("parameterTypes", toParameterTypes(method.getParameterTypes()));
			methodsList.add(methodObj);
		});
		info.put("methods", methodsList);
	}

	private void addAnnotations(Map<String, Object> info, Class<?> clazz) {
		List<String> annotationsList = Arrays.stream(clazz.getDeclaredAnnotations())
				.map(Annotation::toString)
				.collect(Collectors.toList());
		info.put("annotations", annotationsList);
	}

	private void addLocationMetadata(Map<String, Object> info, ClassInfoHolder classInfoHolder, String className) {
		String path = classInfoHolder.getClassPath(className);
		info.put("path", path);

		String id = classInfoHolder.getArtifactId(className);
		if (id != null) {
			info.put("artifact", id);
		}

		String sourcePath = classInfoHolder.getSourcePath(className);
		if (sourcePath != null) {
			info.put("sourcePath", sourcePath);
		}
	}

	private List<String> toParameterTypes(Class<?>[] parameterTypes) {
		return Arrays.stream(parameterTypes)
				.map(Class::getName)
				.collect(Collectors.toList());
	}

	private <T extends Member> void forEachNonPrivate(T[] members, Consumer<T> consumer) {
		for (T member : members) {
			if (!Modifier.isPrivate(member.getModifiers())) {
				consumer.accept(member);
			}
		}
	}

}
