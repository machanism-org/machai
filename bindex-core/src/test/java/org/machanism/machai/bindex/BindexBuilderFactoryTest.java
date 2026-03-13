package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.project.layout.ProjectLayout;

class BindexBuilderFactoryTest {

	private static ProjectLayout projectLayout(File projectDir) {
		return new ProjectLayout() {
			@Override
			public File getProjectDir() {
				return projectDir;
			}

			@Override
			public List<String> getSources() {
				return Collections.emptyList();
			}

			@Override
			public List<String> getDocuments() {
				return Collections.emptyList();
			}

			@Override
			public List<String> getTests() {
				return Collections.emptyList();
			}
		};
	}

	@Test
	void create_throwsOnNullProjectLayout() {
		// Arrange
		Configurator configurator = org.mockito.Mockito.mock(Configurator.class);

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> BindexBuilderFactory.create(null, "openai", configurator));

		// Assert
		assertEquals("projectLayout must not be null", ex.getMessage());
	}

	@Test
	void create_throwsOnNullGenai() {
		// Arrange
		ProjectLayout layout = projectLayout(new File("."));
		Configurator configurator = org.mockito.Mockito.mock(Configurator.class);

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> BindexBuilderFactory.create(layout, null, configurator));

		// Assert
		assertEquals("genai must not be null", ex.getMessage());
	}

	@Test
	void create_throwsOnNullConfigurator() {
		// Arrange
		ProjectLayout layout = projectLayout(new File("."));

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> BindexBuilderFactory.create(layout, "openai", null));

		// Assert
		assertEquals("configurator must not be null", ex.getMessage());
	}

	@Test
	void create_throwsFileNotFoundForUnknownLayoutWhenProjectDirDoesNotExist() {
		// Arrange
		ProjectLayout layout = projectLayout(new File("target/does-not-exist-12345"));
		Configurator configurator = org.mockito.Mockito.mock(Configurator.class);

		// Act
		java.io.FileNotFoundException ex = assertThrows(java.io.FileNotFoundException.class,
				() -> BindexBuilderFactory.create(layout, "openai", configurator));

		// Assert
		assertNotNull(ex.getMessage());
	}
}
