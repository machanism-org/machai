package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.ProjectLayout;

class ProjectProcessorTest {

	@Test
	void scanFolder_whenModulesPresent_processesEachModule() throws IOException {
		// Arrange
		File projectDir = new File("src/test/resources");
		List<String> processedModules = new ArrayList<>();

		ProjectProcessor processor = new ProjectProcessor() {
			@Override
			public void processFolder(ProjectLayout processor) {
				throw new AssertionError("processFolder should not be invoked when modules are present");
			}

			@Override
			protected ProjectLayout getProjectLayout(File dir) throws FileNotFoundException {
				return new ProjectLayout() {
					@Override
					public List<String> getModules() {
						return Arrays.asList("moduleA", "moduleB");
					}

					@Override
					public List<String> getSources() {
						return null;
					}

					@Override
					public List<String> getDocuments() {
						return null;
					}

					@Override
					public List<String> getTests() {
						return null;
					}
				}.projectDir(dir);
			}

			@Override
			protected void processModule(File root, String module) {
				processedModules.add(module);
			}
		};

		// Act
		processor.scanFolder(projectDir);

		// Assert
		assertEquals(Arrays.asList("moduleA", "moduleB"), processedModules);
	}

	@Test
	void scanFolder_whenNoModules_processesFolder_andSwallowsProcessingException() throws IOException {
		// Arrange
		File projectDir = new File("src/test/resources");
		List<ProjectLayout> processedLayouts = new ArrayList<>();

		ProjectProcessor processor = new ProjectProcessor() {
			@Override
			public void processFolder(ProjectLayout processor) {
				processedLayouts.add(processor);
				throw new RuntimeException("boom");
			}

			@Override
			protected ProjectLayout getProjectLayout(File dir) throws FileNotFoundException {
				return new ProjectLayout() {
					@Override
					public List<String> getModules() {
						return null;
					}

					@Override
					public List<String> getSources() {
						return null;
					}

					@Override
					public List<String> getDocuments() {
						return null;
					}

					@Override
					public List<String> getTests() {
						return null;
					}
				}.projectDir(dir);
			}
		};

		// Act
		processor.scanFolder(projectDir);

		// Assert
		assertEquals(1, processedLayouts.size());
		assertTrue(processedLayouts.get(0).getProjectDir().getPath().endsWith("src" + File.separator + "test" + File.separator + "resources"));
	}
}
