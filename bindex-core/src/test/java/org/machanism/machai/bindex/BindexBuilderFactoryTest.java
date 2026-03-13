package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

class BindexBuilderFactoryTest {

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
		org.machanism.machai.project.layout.ProjectLayout layout = org.mockito.Mockito
				.mock(org.machanism.machai.project.layout.ProjectLayout.class);
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
		org.machanism.machai.project.layout.ProjectLayout layout = org.mockito.Mockito
				.mock(org.machanism.machai.project.layout.ProjectLayout.class);

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> BindexBuilderFactory.create(layout, "openai", null));

		// Assert
		assertEquals("configurator must not be null", ex.getMessage());
	}

	@Test
	void create_throwsFileNotFoundForUnknownLayoutWhenProjectDirDoesNotExist() throws Exception {
		// Arrange
		org.machanism.machai.project.layout.ProjectLayout layout = org.mockito.Mockito
				.mock(org.machanism.machai.project.layout.ProjectLayout.class);
		org.mockito.Mockito.when(layout.getProjectDir()).thenReturn(new File("target/does-not-exist-12345"));
		Configurator configurator = org.mockito.Mockito.mock(Configurator.class);

		// Act
		java.io.FileNotFoundException ex = assertThrows(java.io.FileNotFoundException.class,
				() -> BindexBuilderFactory.create(layout, "openai", configurator));

		// Assert
		assertNotNull(ex.getMessage());
	}
}
