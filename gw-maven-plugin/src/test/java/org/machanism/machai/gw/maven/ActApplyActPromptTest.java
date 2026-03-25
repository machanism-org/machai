package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.junit.Test;
import org.mockito.Mockito;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.apache.commons.lang3.Strings;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import org.mockito.InjectMocks;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.machanism.machai.gw.processor.ActProcessor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertSame;
import java.util.Arrays;
import java.util.List;
import org.mockito.ArgumentCaptor;

public class ActApplyActPromptTest {

    private ActProcessor actProcessor;

	@Test
	public void applyActPrompt_whenSavedActExists_doesNotPromptAndKeepsValue() throws Exception {
		// Arrange
		Act act = new Act();
		Properties userProps = new Properties();
		userProps.setProperty(Ghostwriter.GW_ACT_PROP_NAME, "saved");

		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getUserProperties()).thenReturn(userProps);
		act.session = session;

		act.prompter = Mockito.mock(Prompter.class);
		Configurator conf = Mockito.mock(Configurator.class);

		// Act
		act.applyActPrompt(conf);

		// Assert
		Mockito.verifyNoInteractions(act.prompter);
		assertEquals("saved", userProps.getProperty(Ghostwriter.GW_ACT_PROP_NAME));
	}

	@Test
	public void applyActPrompt_whenNoSavedAct_andConfigProvidesAct_savesIt() throws Exception {
		// Arrange
		Act act = new Act();
		Properties userProps = new Properties();

		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getUserProperties()).thenReturn(userProps);
		act.session = session;

		act.prompter = Mockito.mock(Prompter.class);
		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(conf.get(Mockito.eq(Ghostwriter.GW_ACT_PROP_NAME), Mockito.isNull())).thenReturn("fromConf");

		// Act
		act.applyActPrompt(conf);

		// Assert
		assertEquals("fromConf", userProps.getProperty(Ghostwriter.GW_ACT_PROP_NAME));
		Mockito.verifyNoInteractions(act.prompter);
	}

	@Test
	public void applyActPrompt_whenNoSavedAct_andConfigMissing_promptsUserAndSaves() throws Exception {
		// Arrange
		Act act = Mockito.spy(new Act());
		Properties userProps = new Properties();

		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getUserProperties()).thenReturn(userProps);
		act.session = session;

		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(conf.get(Mockito.eq(Ghostwriter.GW_ACT_PROP_NAME), Mockito.isNull())).thenReturn(null);

		Mockito.doReturn("typed").when(act).readText("Act");

		// Act
		act.applyActPrompt(conf);

		// Assert
		assertEquals("typed", userProps.getProperty(Ghostwriter.GW_ACT_PROP_NAME));
	}

	@Test(expected = MojoExecutionException.class)
	public void applyActPrompt_whenPrompterThrows_wrapsIntoMojoExecutionException() throws Exception {
		// Arrange
		Act act = Mockito.spy(new Act());
		Properties userProps = new Properties();

		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getUserProperties()).thenReturn(userProps);
		act.session = session;

		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(conf.get(Mockito.eq(Ghostwriter.GW_ACT_PROP_NAME), Mockito.isNull())).thenReturn(null);

		Mockito.doThrow(new PrompterException("fail")).when(act).readText("Act");

		// Act
		act.applyActPrompt(conf);
	}

	@Test
	public void applyActPrompt_isSynchronizedAcrossThreads_usingSameInstance_doesNotLoseValue() throws Exception {
		// Arrange
		Act act = Mockito.spy(new Act());
		Properties userProps = new Properties();
		MavenSession session = Mockito.mock(MavenSession.class);
		Mockito.when(session.getUserProperties()).thenReturn(userProps);
		act.session = session;
		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(conf.get(Mockito.eq(Ghostwriter.GW_ACT_PROP_NAME), Mockito.isNull())).thenReturn(null);

		// Return different values if called multiple times; due to synchronization and userProps caching,
		// readText should be called at most once.
		Mockito.doReturn("v1").doReturn("v2").when(act).readText("Act");

		Runnable r = () -> {
			try {
				act.applyActPrompt(conf);
			} catch (MojoExecutionException e) {
				throw new RuntimeException(e);
			}
		};

		Thread t1 = new Thread(r);
		Thread t2 = new Thread(r);

		// Act
		t1.start();
		t2.start();
		t1.join();
		t2.join();

		// Assert
		Mockito.verify(act, Mockito.atMost(1)).readText("Act");
		assertEquals("v1", userProps.getProperty(Ghostwriter.GW_ACT_PROP_NAME));
	}

    @Test
    public void testSetDefaultPromptWhenActHasSpaceShouldExtractNameAndPrompt() throws IOException {
        // TestMate-4657c9f4e0e991c09c6f6295e8c50c3d
        // Given
        Configurator mockConfigurator = mock(Configurator.class);
        actProcessor = new ActProcessor(new File("."), mockConfigurator, "test-model");
        
        String input = "explain in French";
        String expectedName = "explain";
        String template = "Please %s";
        // The implementation takes substring from the whitespace index, so " in French"
        // "Please %s" replaced with " in French" results in "Please  in French"
        String expectedFinalPrompt = "Please  in French";
        
        try (MockedStatic<ActProcessor> mockedActProcessor = mockStatic(ActProcessor.class)) {
            mockedActProcessor.when(() -> ActProcessor.loadAct(eq(expectedName), any(Map.class), any()))
                    .thenAnswer(invocation -> {
                        Map<String, Object> properties = invocation.getArgument(1);
                        properties.put(Ghostwriter.INPUTS_PROPERTY_NAME, template);
                        return null;
                    });
            // When
            actProcessor.setDefaultPrompt(input);
            // Then
            mockedActProcessor.verify(() -> ActProcessor.loadAct(eq(expectedName), any(Map.class), any()));
            assertEquals(expectedFinalPrompt, actProcessor.getDefaultPrompt());
        }
    }

    @Test
    public void testSetDefaultPromptWhenLoadActThrowsIOExceptionShouldThrowIllegalArgument() throws IOException {
        // TestMate-98e3bdd61b5ea013c3f39f32d096e2c0
        // Arrange
        Configurator configurator = mock(Configurator.class);
        File projectDir = new File(".");
        String model = "test-model";
        ActProcessor processor = new ActProcessor(projectDir, configurator, model);
        String actName = "errorAct";
        try (MockedStatic<ActProcessor> mockedActProcessor = mockStatic(ActProcessor.class)) {
            mockedActProcessor.when(() -> ActProcessor.loadAct(eq(actName), any(Map.class), any()))
                    .thenThrow(new IOException("Disk failure"));
            // Act
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> processor.setDefaultPrompt(actName));
            // Assert
            assertEquals(IOException.class, exception.getCause().getClass());
            assertEquals("Disk failure", exception.getCause().getMessage());
            mockedActProcessor.verify(() -> ActProcessor.loadAct(eq(actName), any(Map.class), any()));
        }
    }

    @Test
public void testSetDefaultPromptWhenInputsIsNotStringShouldNotPerformReplacement() throws IOException {
    // TestMate-f003688e085e2e29064020cf7cdbe406
    // Arrange
    Configurator mockConfigurator = mock(Configurator.class);
    File projectDir = new File(".");
    String model = "test-model";
    ActProcessor processor = new ActProcessor(projectDir, mockConfigurator, model);
    String input = "complexAct user-input";
    String expectedName = "complexAct";
    List<String> nonStringInputs = Arrays.asList("instruction1", "instruction2");
    try (MockedStatic<ActProcessor> mockedActProcessor = mockStatic(ActProcessor.class)) {
        mockedActProcessor.when(() -> ActProcessor.loadAct(eq(expectedName), any(Map.class), any()))
                .thenAnswer(invocation -> {
                    Map<String, Object> properties = invocation.getArgument(1);
                    properties.put(Ghostwriter.INPUTS_PROPERTY_NAME, nonStringInputs);
                    return null;
                });
        // Act
        processor.setDefaultPrompt(input);
        // Assert
        mockedActProcessor.verify(() -> ActProcessor.loadAct(eq(expectedName), any(Map.class), any()));
        
        // Since applyActData is package-private and only processes Strings, 
        // a List value for INPUTS_PROPERTY_NAME is ignored during application.
        // Therefore, the defaultPrompt of the processor should remain null (unaffected).
        assertEquals(null, processor.getDefaultPrompt());
    }
}
}
