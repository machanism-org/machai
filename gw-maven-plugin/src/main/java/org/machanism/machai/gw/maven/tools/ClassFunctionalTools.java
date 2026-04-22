package org.machanism.machai.gw.maven.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.maven.project.MavenProject;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class ClassFunctionalTools implements FunctionTools {

	private static final Logger logger = LoggerFactory.getLogger(ClassFunctionalTools.class);

	private static Map<String, Map<String, Map<String, List<ClassInfo>>>> scoppedClassMap = new HashMap<>();

	private static Map<String, List<ClassInfo>> classMap = new HashMap<>();

	@Override
	public void applyTools(Genai provider) {
		provider.addTool(
				"find_class",
				"Retrieves a list of fully qualified class names for the specified class group. Provide the 'className' property to get all matching classes.",
				this::findClass,
				"className:string:required:Regular expression pattern to find all matching classes by short name.");
	}

	private String findClass(Object... args) {
		JsonNode props = (JsonNode) args[0];
		String className = props.get("className").asText();

		Pattern pattern = Pattern.compile(className);
		List<Entry<String, List<ClassInfo>>> list = classMap.entrySet().stream()
				.filter(entry -> pattern.matcher(entry.getKey()).matches())
				.collect(Collectors.toList());

		String join = "Class not found.";
		if (list != null) {
			List<String> collect = list.stream().map(e -> e.getKey())
					.collect(Collectors.toList());
			join = StringUtils.join(collect, ",");
		}
		return join;
	}

	public static void scanProjectClasses(MavenProject project) {
		try {

//			List<String> compileClasspathElements = project.getCompileClasspathElements();
//			List<String> runtimeClasspathElements = project.getRuntimeClasspathElements();
//			String testOutputDirectory = project.getBuild().getTestOutputDirectory();

			String outputDirectory = project.getBuild().getOutputDirectory();
			List<String> dependencyArtifacts = project.getArtifacts().stream().map(a -> a.getFile().toString())
					.collect(Collectors.toList());

			List<String> arrayList = new ArrayList<>();
//			arrayList.addAll(compileClasspathElements);
//			arrayList.addAll(runtimeClasspathElements);
//			arrayList.add(testOutputDirectory);
			arrayList.addAll(dependencyArtifacts);
			arrayList.add(outputDirectory);

			URL[] urls = arrayList.stream().map(p -> {
				try {
					return new File(p).toURI().toURL();
				} catch (MalformedURLException e) {
					throw new IllegalArgumentException(e);
				}
			}).collect(Collectors.toList())
					.toArray(new URL[0]);

			URLClassLoader classLoader = URLClassLoader.newInstance(urls);

//			scanClassesByPath(compileClasspathElements, "CompileClasspathElements", classLoader);
//			scanClassesByPath(runtimeClasspathElements, "RuntimeClasspathElements", classLoader);
//			scanClassesByPath(testOutputDirectory, "TestOutputDirectory", classLoader);
			scanClassesByPath(outputDirectory, "OutputDirectory", classLoader);
			scanClassesByPath(dependencyArtifacts, "DependencyArtifacts", classLoader);

		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		for (String scope : scoppedClassMap.keySet()) {
			Map<String, Map<String, List<ClassInfo>>> pathMap = scoppedClassMap.get(scope);
			for (String path : pathMap.keySet()) {
				Map<String, List<ClassInfo>> packages = pathMap.get(path);
				for (Entry<String, List<ClassInfo>> classInfoEntry : packages.entrySet()) {
					for (ClassInfo classInfo : classInfoEntry.getValue()) {
						// System.out.println(classInfo.getName());
					}
				}
			}
		}
	}

	private static void scanClassesByPath(List<String> pathList, String scope, URLClassLoader classLoader)
			throws IOException {
		for (String path : pathList) {
			scanClassesByPath(path, scope, classLoader);
		}
	}

	public static void scanClassesByPath(String path, String scope, URLClassLoader classLoader) throws IOException {
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
						ClassInfo classInfo = new ClassInfo(packageName, className, scope, path);
						scoppedClassMap
								.computeIfAbsent(scope, k -> new HashMap<>())
								.computeIfAbsent(path, k -> new HashMap<>())
								.computeIfAbsent((packageName), k -> new ArrayList<>())
								.add(classInfo);

						classMap.computeIfAbsent((className), k -> new ArrayList<>())
								.add(classInfo);
					}
				} catch (Throwable e) {
					logger.debug("Class {}, Error: {}", name, e.getMessage());
				}
			}
		}
	}

}
