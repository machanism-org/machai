package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.ProjectLayout;
import org.mockito.Mockito;

class ProcessorCoverageBoostTest {

	@Test
	void ghostwriter_resolveProjectDir_prefersCommandLineOverConfig(@TempDir File tmp) throws Exception {
		Options options = new Options();
		Option projectDirOpt = new Option("d", Ghostwriter.PROJECT_DIR_PROP_NAME, true, "project dir");
		options.addOption(projectDirOpt);
		CommandLine cmd = new DefaultParser().parse(options, new String[] { "-d", tmp.getAbsolutePath() });
		PropertiesConfigurator config = Mockito.mock(PropertiesConfigurator.class);

		Method method = Ghostwriter.class.getDeclaredMethod("resolveProjectDir", CommandLine.class, Option.class,
				PropertiesConfigurator.class);
		method.setAccessible(true);

		File result = (File) method.invoke(null, cmd, projectDirOpt, config);

		assertEquals(tmp.getAbsolutePath(), result.getAbsolutePath());
	}

	@Test
	void ghostwriter_help_and_privateLoggingMethods_executeWithoutError() throws Exception {
		Ghostwriter.initializeConfiguration(new File("."));
		Method logInstructions = Ghostwriter.class.getDeclaredMethod("logInstructions", String.class);
		logInstructions.setAccessible(true);
		logInstructions.invoke(null, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");

		Method abbreviate = Ghostwriter.class.getDeclaredMethod("abbreviateInstructions", String.class);
		abbreviate.setAccessible(true);
		String abbreviated = (String) abbreviate.invoke(null,
				"abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");

		Ghostwriter.help(new Options());

		assertNotNull(abbreviated);
		assertTrue(abbreviated.length() <= 60);
	}

	@Test
	void aiFileProcessor_basicGettersSettersAndInputDefault(@TempDir File tmp) {
		AIFileProcessor processor = new AIFileProcessor(tmp, new PropertiesConfigurator(), "Provider:Model");

		assertFalse(processor.isLogInputs());
		processor.setLogInputs(true);
		assertTrue(processor.isLogInputs());

		processor.setModel("Other:Model");
		assertEquals("Other:Model", processor.getModel());
		assertEquals("Other:Model", processor.getProvider());

		processor.setProvider("Alias:Model");
		assertEquals("Alias:Model", processor.getModel());

		processor.setInteractive(true);
		assertTrue(processor.isInteractive());
		assertNull(processor.input());
	}

	@Test
	void aiFileProcessor_processFolder_wrapsProcessingException(@TempDir File tmp) {
		AIFileProcessor processor = new AIFileProcessor(tmp, new PropertiesConfigurator(), "Provider:Model") {
			@Override
			public String process(ProjectLayout projectLayout, File file, String instructions, String prompt) {
				throw new IllegalStateException("boom");
			}
		};
		ProjectLayout layout = Mockito.mock(ProjectLayout.class);
		Mockito.when(layout.getProjectDir()).thenReturn(tmp);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> processor.processFolder(layout));
		assertTrue(ex.getCause() instanceof IllegalStateException);
	}

	@Test
	void abstractFileProcessor_scanDocuments_and_simpleAccessors(@TempDir File tmp) throws Exception {
		class StubProcessor extends AbstractFileProcessor {
			boolean scanFolderCalled;

			StubProcessor(File projectDir) {
				super(projectDir, new PropertiesConfigurator());
			}

			@Override
			public void scanFolder(File projectDir) {
				scanFolderCalled = true;
				setScanDir(projectDir);
			}
		}

		StubProcessor processor = new StubProcessor(tmp);
		processor.scanDocuments(tmp);
		processor.setDegreeOfConcurrency(2);
		processor.setNonRecursive(true);
		processor.setExcludes(new String[] { "target" });
		processor.setModuleThreadTimeoutMinutes(1);

		assertTrue(processor.scanFolderCalled);
		assertEquals(tmp.getAbsolutePath(), processor.getProjectDir().getAbsolutePath());
		assertEquals(tmp.getAbsolutePath(), processor.getScanDir().getAbsolutePath());
		assertTrue(processor.isNonRecursive());
		assertEquals(1, processor.getModuleThreadTimeoutMinutes());
		assertArrayEquals(new String[] { "target" }, processor.getExcludes());
		assertNotNull(processor.getConfigurator());
	}

	private static void assertArrayEquals(String[] expected, String[] actual) {
		assertEquals(Arrays.asList(expected), Arrays.asList(actual));
	}

	@Test
	void abstractFileProcessor_processProjectDir_processesMatchingFiles(@TempDir File tmp) throws Exception {
		File child = new File(tmp, "a.txt");
		Files.write(child.toPath(), Collections.singletonList("data"), StandardCharsets.UTF_8);

		class StubProcessor extends AbstractFileProcessor {
			int count;

			StubProcessor(File projectDir) {
				super(projectDir, new PropertiesConfigurator());
			}

			@Override
			protected void processFile(ProjectLayout projectLayout, File file) {
				count++;
			}
		}

		StubProcessor processor = new StubProcessor(tmp);
		ProjectLayout layout = Mockito.mock(ProjectLayout.class);
		Mockito.when(layout.getProjectDir()).thenReturn(tmp);

		processor.processProjectDir(layout, ".");

		assertTrue(processor.count >= 1);
	}

	@Test
	void actProcessor_processParentFiles_and_processFile_coverExecution(@TempDir File tmp) throws Exception {
		File file = new File(tmp, "sample.txt");
		Files.write(file.toPath(), Collections.singletonList("data"), StandardCharsets.UTF_8);

		ActProcessor processor = new ActProcessor(tmp, new PropertiesConfigurator(), "Provider:Model") {
			int processed;

			@Override
			public String process(ProjectLayout projectLayout, File file, String instructions, String prompt) {
				processed++;
				return "done";
			}
		};
		processor.setDefaultPrompt("help test");
		processor.setPathMatcher(tmp.toPath().getFileSystem().getPathMatcher("glob:**/*.txt"));
		processor.setScanDir(tmp);

		ProjectLayout layout = Mockito.mock(ProjectLayout.class);
		Mockito.when(layout.getProjectDir()).thenReturn(tmp);
		Mockito.when(layout.getModules()).thenReturn(Collections.emptyList());

		processor.processParentFiles(layout);
		processor.processFile(layout, file);

		assertNotNull(processor);
	}

	@Test
	void guidanceProcessor_parseFile_and_processBehaviors(@TempDir File tmp) throws Exception {
		File javaFile = new File(tmp, "Example.java");
		Files.write(javaFile.toPath(), Collections.singletonList("class Example {}"), StandardCharsets.UTF_8);

		GuidanceProcessor processor = new GuidanceProcessor(tmp, "Provider:Model", new PropertiesConfigurator()) {
			@Override
			protected String process(ProjectLayout projectLayout, File file, String guidance) {
				return "processed";
			}
		};
		processor.setDefaultPrompt("Default prompt");

		assertNull(processor.parseFile(tmp, tmp));
		assertNull(processor.getReviewerForExtension("unknownext"));
		assertEquals("java", GuidanceProcessor.normalizeExtensionKey(".JAVA"));

		ProjectLayout layout = Mockito.mock(ProjectLayout.class);
		Mockito.when(layout.getProjectDir()).thenReturn(tmp);
		Mockito.when(layout.getModules()).thenReturn(Collections.emptyList());

		processor.processFile(layout, javaFile);
		processor.processParentFiles(layout);
	}

}
