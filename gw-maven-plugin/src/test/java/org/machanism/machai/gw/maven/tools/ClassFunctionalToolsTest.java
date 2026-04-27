package org.machanism.machai.gw.maven.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.machanism.machai.ai.provider.Genai;
import org.mockito.Mockito;

public class ClassFunctionalToolsTest {

	@Test
	public void scanProjectClassesAndFindClass_returnMatchingClassNames() throws Exception {
		MavenProject project = createProjectForMainSources();
		ClassFunctionalTools tools = new ClassFunctionalTools();

		tools.scanProjectClasses(project);
		HashMap<String, Object> props = new HashMap<>();
		props.put("className", "ClassInfoHolder");
		String classes = tools.findClass(props, project.getBasedir());

		assertTrue(classes.contains("org.machanism.machai.gw.maven.tools.ClassInfoHolder"));
	}

	@Test
	public void findClass_returnsNotFoundWhenNoClassesMatch() throws Exception {
		MavenProject project = createProjectForMainSources();
		ClassFunctionalTools tools = new ClassFunctionalTools(project);

		HashMap<String, Object> props = new HashMap<>();
		props.put("className", "UnknownType");
		String classes = tools.findClass(props, project.getBasedir());

		assertEquals("Class not found.", classes);
	}

	@Test
	public void findClass_returnsUnsupportedMessageForUnknownProject() throws Exception {
		ClassFunctionalTools tools = new ClassFunctionalTools();

		HashMap<String, Object> props = new HashMap<>();
		props.put("className", "Anything");
		String classes = tools.findClass(props, new File("missing-project"));

		assertEquals("The function tool don't support this function tool.", classes);
	}

	@Test
	public void getClassInfo_returnsDetailedMetadataForProjectClass() throws Exception {
		MavenProject project = createProjectForMainSources();
		ClassFunctionalTools tools = new ClassFunctionalTools(project);

		HashMap<String, Object> props = new HashMap<>();
		props.put("className", "org.machanism.machai.gw.maven.tools.ClassInfoHolder");
		HashMap<String, Object> info = tools.getClassInfo(props, project.getBasedir());

		assertEquals("org.machanism.machai.gw.maven.tools.ClassInfoHolder", info.get("className"));
		assertTrue(((String) info.get("modifiers")).contains("public"));
		assertTrue(info.containsKey("superclass"));
		assertTrue(info.containsKey("interfaces"));
		assertTrue(info.containsKey("fields"));
		assertTrue(info.containsKey("constructors"));
		assertTrue(info.containsKey("methods"));
		assertTrue(info.containsKey("annotations"));
		assertTrue(info.containsKey("path"));
		assertTrue(info.containsKey("sourcePath"));

		List<Map<String, Object>> fields = (List<Map<String, Object>>) info.get("fields");
		assertFalse(fields.toString().contains("artifactMap"));

		List<Map<String, Object>> methods = (List<Map<String, Object>>) info.get("methods");
		assertFalse(methods.toString().contains("isSupportedClassEntry"));
		assertTrue(methods.size() > 0);
	}

	@Test
	public void getClassInfo_returnsClassNotFoundErrorWhenTypeIsMissing() throws Exception {
		MavenProject project = createProjectForMainSources();
		ClassFunctionalTools tools = new ClassFunctionalTools(project);

		HashMap<String, Object> props = new HashMap<>();
		props.put("className", "missing.Type");
		HashMap<String, Object> info = tools.getClassInfo(props, project.getBasedir());

		assertEquals("Class not found: missing.Type", info.get("error"));
	}

	@Test
	public void getClassInfo_returnsUnsupportedErrorForUnknownProject() throws Exception {
		ClassFunctionalTools tools = new ClassFunctionalTools();

		HashMap<String, Object> props = new HashMap<>();
		props.put("className", "missing.Type");
		HashMap<String, Object> info = tools.getClassInfo(props, new File("missing-project"));

		assertEquals("The function tool don't support this function tool.", info.get("error"));
	}

	@Test
	public void applyTools_registersFindAndGetClassInfoTools() {
		ClassFunctionalTools tools = new ClassFunctionalTools();
		Genai provider = Mockito.mock(Genai.class);

		tools.applyTools(provider);

		Mockito.verify(provider).addTool(Mockito.eq("find_class"), Mockito.contains("fully qualified Java class names"),
				Mockito.any(), Mockito.contains("className:string:required"));
		Mockito.verify(provider).addTool(Mockito.eq("get_class_info"),
				Mockito.contains("retrieve detailed information"),
				Mockito.any(), Mockito.contains("className:string:required"));
	}

	@Test
	public void scanProjectClasses_registersProjectByBasedir() throws Exception {
		MavenProject project = createProjectForMainSources();
		ClassFunctionalTools tools = new ClassFunctionalTools();

		tools.scanProjectClasses(project);

		Field field = ClassFunctionalTools.class.getDeclaredField("classInfoProjectMap");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<File, ClassInfoHolder> map = (Map<File, ClassInfoHolder>) field.get(tools);
		assertTrue(map.containsKey(project.getBasedir()));
	}

	private static MavenProject createProjectForMainSources() {
		Model model = new Model();
		model.setBuild(new Build());
		MavenProject project = new MavenProject(model) {
			@Override
			public File getBasedir() {
				return new File(".");
			}

			@Override
			public java.util.List<String> getCompileClasspathElements() {
				return Collections.singletonList(new File("target/classes").getAbsolutePath());
			}
		};
		project.getBuild().setOutputDirectory(new File("target/classes").getAbsolutePath());
		project.getBuild().setTestOutputDirectory(new File("target/test-classes").getAbsolutePath());
		project.addCompileSourceRoot(new File("src/main/java").getAbsolutePath());
		return project;
	}
}