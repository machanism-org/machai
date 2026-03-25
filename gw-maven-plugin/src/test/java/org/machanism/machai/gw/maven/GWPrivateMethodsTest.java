package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.atLeastOnce;
import org.apache.maven.execution.MavenExecutionRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.lang.reflect.Field;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Settings;
import org.junit.Before;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import static org.mockito.Mockito.mockStatic;
import org.machanism.machai.project.ProjectLayoutManager;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.mockito.MockedStatic;

/**
 * Targets the private static helper methods in {@link GW} via reflection.
 */
public class GWPrivateMethodsTest {

    @Mock
    private MavenProject project;

    @Mock
    private MavenSession session;

	private static Method getResolveMethod() throws Exception {
		Method m = GW.class.getDeclaredMethod("resolveProjectByArtifactId", java.util.List.class, Model.class);
		m.setAccessible(true);
		return m;
	}

	private static Method getToCoordMethod() throws Exception {
		Method m = GW.class.getDeclaredMethod("toCoord", MavenProject.class);
		m.setAccessible(true);
		return m;
	}

	private static MavenProject newProject(String groupId, String artifactId, String version, File basedir) {
		MavenProject p = new MavenProject();
		p.setGroupId(groupId);
		p.setArtifactId(artifactId);
		p.setVersion(version);
		p.setFile(basedir == null ? null : new File(basedir, "pom.xml"));
		return p;
	}

	@Test
	public void resolveProjectByArtifactId_nullOrEmptyInputs_returnsNull() throws Exception {
		// Arrange
		Method resolve = getResolveMethod();
		Model model = new Model();
		model.setArtifactId("a");

		// Act + Assert
		assertNull(resolve.invoke(null, null, model));
		assertNull(resolve.invoke(null, Collections.emptyList(), model));
		assertNull(resolve.invoke(null, Collections.singletonList(newProject("g", "a", "1", new File("."))), null));
	}

	@Test
	public void resolveProjectByArtifactId_blankArtifactId_returnsNull() throws Exception {
		// Arrange
		Method resolve = getResolveMethod();
		Model model = new Model();
		model.setArtifactId("   ");

		// Act
		Object result = resolve.invoke(null,
				Collections.singletonList(newProject("g", "a", "1", new File("."))), model);

		// Assert
		assertNull(result);
	}

	@Test
	public void resolveProjectByArtifactId_singleMatch_returnsMatchingProject() throws Exception {
		// Arrange
		Method resolve = getResolveMethod();
		Model model = new Model();
		model.setArtifactId("target");
		MavenProject p1 = newProject("g", "other", "1", new File("module1"));
		MavenProject p2 = newProject("g", "target", "1", new File("module2"));

		// Act
		Object result = resolve.invoke(null, Arrays.asList(p1, p2), model);

		// Assert
		assertEquals(p2, result);
	}

	@Test
	public void resolveProjectByArtifactId_multipleMatches_throwsIllegalStateException() throws Exception {
		// Arrange
		Method resolve = getResolveMethod();
		Model model = new Model();
		model.setArtifactId("dup");
		MavenProject p1 = newProject("g", "dup", "1", new File("m1"));
		MavenProject p2 = newProject("g", "dup", "2", new File("m2"));

		// Act
		try {
			resolve.invoke(null, Arrays.asList(p1, p2), model);
			Assert.fail("Expected IllegalStateException due to multiple matching projects"); // Sonar java:S2699
		} catch (InvocationTargetException e) {
			// Assert
			if (!(e.getCause() instanceof IllegalStateException)) {
				throw e;
			}
		}
	}

	@Test
	public void toCoord_nullProject_returnsNullToken() throws Exception {
		// Arrange
		Method toCoord = getToCoordMethod();

		// Act
		Object coord = toCoord.invoke(null, new Object[] { null });

		// Assert
		assertEquals("<null>", coord);
	}

	@Test
	public void toCoord_missingFields_doesNotReturnLiteralNull() throws Exception {
		// Arrange
		Method toCoord = getToCoordMethod();
		MavenProject p = new MavenProject();

		// Act
		String coord = (String) toCoord.invoke(null, p);

		// Assert
		Assert.assertEquals(-1, coord.indexOf("null"));
		Assert.assertTrue(coord.contains(":"));
		Assert.assertTrue(coord.contains("@"));
	}

    @Test
    public void execute_whenStandardInvocation_configuresProcessorAndScans() throws Exception {
        // TestMate-d3c2e3ce333653f8d884e73269051bef
        // Given
        GW spyGw = spy(new GW());
        File basedir = new File("target/test-basedir");
        String defaultModel = "gpt-4-default";
        String overrideModel = "gpt-4-override";
        // Initialize mocks manually
        MavenProject mockProject = mock(MavenProject.class);
        MavenSession mockSession = mock(MavenSession.class);
        Settings mockSettings = mock(Settings.class);
        Log mockLog = mock(Log.class);
        MavenExecutionRequest mockRequest = mock(MavenExecutionRequest.class);
        // Inject fields into the spy using reflection (AbstractGWGoal fields)
        Field basedirField = AbstractGWGoal.class.getDeclaredField("basedir");
        basedirField.setAccessible(true);
        basedirField.set(spyGw, basedir);
        Field modelField = AbstractGWGoal.class.getDeclaredField("model");
        modelField.setAccessible(true);
        modelField.set(spyGw, defaultModel);
        Field settingsField = AbstractGWGoal.class.getDeclaredField("settings");
        settingsField.setAccessible(true);
        settingsField.set(spyGw, mockSettings);
        Field projectField = AbstractGWGoal.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(spyGw, mockProject);
        Field sessionField = AbstractGWGoal.class.getDeclaredField("session");
        sessionField.setAccessible(true);
        sessionField.set(spyGw, mockSession);
        spyGw.setLog(mockLog);
        PropertiesConfigurator config = mock(PropertiesConfigurator.class);
        when(config.get(Ghostwriter.GW_MODEL_PROP_NAME, defaultModel)).thenReturn(overrideModel);
        Mockito.doReturn(config).when(spyGw).getConfiguration();
        // Stubbing for logic inside GW.execute()
        // boolean nonRecursive = project.getModules().size() > 1 && modules.size() == 1;
        when(mockProject.getModules()).thenReturn(Collections.emptyList());
        List<MavenProject> allProjects = Collections.singletonList(mockProject);
        when(mockSession.getAllProjects()).thenReturn(allProjects);
        when(mockSession.isParallel()).thenReturn(false);
        when(mockSession.getRequest()).thenReturn(mockRequest);
        // Capture the processor passed to scanDocuments since it's an anonymous inner class 
        // and cannot be intercepted by Mockito.mockConstruction.
        ArgumentCaptor<GuidanceProcessor> processorCaptor = ArgumentCaptor.forClass(GuidanceProcessor.class);
        Mockito.doNothing().when(spyGw).scanDocuments(processorCaptor.capture());
        // When
        spyGw.execute();
        // Then
        GuidanceProcessor capturedProcessor = processorCaptor.getValue();
        
        // Verify the processor was passed to scanDocuments
        verify(spyGw).scanDocuments(any(GuidanceProcessor.class));
        
        // Verify configuration logic by checking the state of the captured processor
        // Since project.getModules().size() is 0, nonRecursive should be false
        assertFalse("Processor should be in recursive mode", capturedProcessor.isNonRecursive());
        
        // Verify that model was correctly passed from config to constructor
        // GuidanceProcessor stores the model in a private field, but we can check the logs 
        // or rely on the fact that the constructor was called with the 'overrideModel'.
        
        // Verify session interactions
        verify(mockSession, atLeastOnce()).getAllProjects();
        verify(mockSession).isParallel();
        verify(mockSession, never()).getRequest(); // isParallel is false, so getRequest shouldn't be called
    }

    @Test
    public void execute_whenProjectIsMultiModuleAndSessionIsSingle_setsNonRecursiveTrue() throws Exception {
        // TestMate-71c99318fd38d81b13420843fdc500c8
        // Given
        GW gw = new GW();
        GW spyGw = spy(gw);
        File basedir = new File("target/test-basedir");
        String testModel = "test-model";
        MavenProject mockProject = mock(MavenProject.class);
        MavenSession mockSession = mock(MavenSession.class);
        Log mockLog = mock(Log.class);
        Settings mockSettings = mock(Settings.class);
        PropertiesConfigurator mockConfig = mock(PropertiesConfigurator.class);
        Field basedirField = AbstractGWGoal.class.getDeclaredField("basedir");
        basedirField.setAccessible(true);
        basedirField.set(spyGw, basedir);
        Field modelField = AbstractGWGoal.class.getDeclaredField("model");
        modelField.setAccessible(true);
        modelField.set(spyGw, testModel);
        Field projectField = AbstractGWGoal.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(spyGw, mockProject);
        Field sessionField = AbstractGWGoal.class.getDeclaredField("session");
        sessionField.setAccessible(true);
        sessionField.set(spyGw, mockSession);
        Field settingsField = AbstractGWGoal.class.getDeclaredField("settings");
        settingsField.setAccessible(true);
        settingsField.set(spyGw, mockSettings);
        spyGw.setLog(mockLog);
        doReturn(mockConfig).when(spyGw).getConfiguration();
        when(mockConfig.get(Ghostwriter.GW_MODEL_PROP_NAME, testModel)).thenReturn(testModel);
        List<String> pomModules = Arrays.asList("module1", "module2");
        when(mockProject.getModules()).thenReturn(pomModules);
        List<MavenProject> sessionProjects = Collections.singletonList(mock(MavenProject.class));
        when(mockSession.getAllProjects()).thenReturn(sessionProjects);
        when(mockSession.isParallel()).thenReturn(false);
        ArgumentCaptor<GuidanceProcessor> processorCaptor = ArgumentCaptor.forClass(GuidanceProcessor.class);
        doNothing().when(spyGw).scanDocuments(processorCaptor.capture());
        // When
        spyGw.execute();
        // Then
        GuidanceProcessor capturedProcessor = processorCaptor.getValue();
        assertTrue("Processor should be in non-recursive mode when targeting a single module in a multi-module project",
                capturedProcessor.isNonRecursive());
        verify(spyGw).scanDocuments(any(GuidanceProcessor.class));
        verify(mockProject).getModules();
        verify(mockSession).getAllProjects();
    }

    @Test
    public void execute_whenProcessTerminationExceptionThrown_wrapsInMojoExecutionException() throws Exception {
        // TestMate-caf85ccb3ce5bafb41ef057a935ceafd
        // Arrange
        GW spyGw = spy(new GW());
        File basedir = new File("target/test-basedir");
        String testModel = "test-model";
        
        MavenProject mockProject = mock(MavenProject.class);
        MavenSession mockSession = mock(MavenSession.class);
        Settings mockSettings = mock(Settings.class);
        Log mockLog = mock(Log.class);
        PropertiesConfigurator mockConfig = mock(PropertiesConfigurator.class);
        Field basedirField = AbstractGWGoal.class.getDeclaredField("basedir");
        basedirField.setAccessible(true);
        basedirField.set(spyGw, basedir);
        Field modelField = AbstractGWGoal.class.getDeclaredField("model");
        modelField.setAccessible(true);
        modelField.set(spyGw, testModel);
        Field settingsField = AbstractGWGoal.class.getDeclaredField("settings");
        settingsField.setAccessible(true);
        settingsField.set(spyGw, mockSettings);
        Field projectField = AbstractGWGoal.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(spyGw, mockProject);
        Field sessionField = AbstractGWGoal.class.getDeclaredField("session");
        sessionField.setAccessible(true);
        sessionField.set(spyGw, mockSession);
        spyGw.setLog(mockLog);
        doReturn(mockConfig).when(spyGw).getConfiguration();
        when(mockConfig.get(Ghostwriter.GW_MODEL_PROP_NAME, testModel)).thenReturn(testModel);
        
        when(mockProject.getModules()).thenReturn(Collections.emptyList());
        List<MavenProject> allProjects = Collections.singletonList(mockProject);
        when(mockSession.getAllProjects()).thenReturn(allProjects);
        when(mockSession.isParallel()).thenReturn(false);
        ProcessTerminationException pte = new ProcessTerminationException("Killed", 130);
        doThrow(pte).when(spyGw).scanDocuments(any(GuidanceProcessor.class));
        // Act
        MojoExecutionException exception = assertThrows(MojoExecutionException.class, spyGw::execute);
        // Assert
        String expectedMessage = "Process terminated: Killed (exit code: 130)";
        assertEquals(expectedMessage, exception.getMessage());
        assertSame(pte, exception.getCause());
        verify(mockLog).error(expectedMessage);
        verify(spyGw).scanDocuments(any(GuidanceProcessor.class));
    }

    @Test
public void getProjectLayout_whenMavenProjectLayout_attachesEffectiveModelFromSession() throws Exception {
    // TestMate-9eb23775658050e489013f2c4f694330
    // Given
    GW spyGw = spy(new GW());
    File basedir = new File("target/test-basedir");
    String testModelName = "gpt-4-test";
    MavenProject mockProject = mock(MavenProject.class);
    MavenSession mockSession = mock(MavenSession.class);
    PropertiesConfigurator mockConfig = mock(PropertiesConfigurator.class);
    Field basedirField = AbstractGWGoal.class.getDeclaredField("basedir");
    basedirField.setAccessible(true);
    basedirField.set(spyGw, basedir);
    Field projectField = AbstractGWGoal.class.getDeclaredField("project");
    projectField.setAccessible(true);
    projectField.set(spyGw, mockProject);
    Field sessionField = AbstractGWGoal.class.getDeclaredField("session");
    sessionField.setAccessible(true);
    sessionField.set(spyGw, mockSession);
    doReturn(mockConfig).when(spyGw).getConfiguration();
    when(mockConfig.get(Ghostwriter.GW_MODEL_PROP_NAME, null)).thenReturn(testModelName);
    when(mockProject.getModules()).thenReturn(Collections.emptyList());
    when(mockSession.isParallel()).thenReturn(false);
    String artifactId = "test-artifact";
    Model effectiveModel = new Model();
    effectiveModel.setArtifactId(artifactId);
    MavenProject sessionProject = mock(MavenProject.class);
    when(sessionProject.getArtifactId()).thenReturn(artifactId);
    when(sessionProject.getModel()).thenReturn(effectiveModel);
    List<MavenProject> allProjects = Collections.singletonList(sessionProject);
    when(mockSession.getAllProjects()).thenReturn(allProjects);
    ArgumentCaptor<GuidanceProcessor> processorCaptor = ArgumentCaptor.forClass(GuidanceProcessor.class);
    doNothing().when(spyGw).scanDocuments(processorCaptor.capture());
    // When
    spyGw.execute();
    GuidanceProcessor capturedProcessor = processorCaptor.getValue();
    File projectDir = new File("target/project-dir");
    Model layoutModel = new Model();
    layoutModel.setArtifactId(artifactId);
    MavenProjectLayout mockLayout = mock(MavenProjectLayout.class);
    when(mockLayout.getModel()).thenReturn(layoutModel);
    when(mockLayout.projectDir(any(File.class))).thenReturn(mockLayout);
    ProjectLayout result;
    try (MockedStatic<ProjectLayoutManager> mockedManager = mockStatic(ProjectLayoutManager.class)) {
        mockedManager.when(() -> ProjectLayoutManager.detectProjectLayout(projectDir)).thenReturn(mockLayout);
        result = capturedProcessor.getProjectLayout(projectDir);
    }
    // Then
    assertSame(mockLayout, result);
    verify(mockLayout).model(effectiveModel);
    verify(mockLayout).projectDir(projectDir);
    // session.getAllProjects() is called twice: 
    // 1. In GW.execute() to set nonRecursive flag.
    // 2. In anonymous GuidanceProcessor.getProjectLayout() override to resolve matching project.
    verify(mockSession, Mockito.times(2)).getAllProjects();
}
}
