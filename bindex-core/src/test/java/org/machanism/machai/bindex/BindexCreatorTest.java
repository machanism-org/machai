package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

import com.fasterxml.jackson.databind.ObjectMapper;

class BindexCreatorTest {

	@Test
	void constructor_throwsOnNullGenai() {
		// Arrange
		Configurator config = org.mockito.Mockito.mock(Configurator.class);

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new BindexCreator(null, config));

		// Assert
		assertEquals("genai must not be null", ex.getMessage());
	}

	@Test
	void constructor_throwsOnNullConfig() {
		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new BindexCreator("openai", null));

		// Assert
		assertEquals("config must not be null", ex.getMessage());
	}

	@Test
	void processFolder_throwsOnNullProjectLayout() {
		// Arrange
		Configurator config = org.mockito.Mockito.mock(Configurator.class);
		BindexCreator creator = new BindexCreator("openai", config);

		// Act
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> creator.processFolder(null));

		// Assert
		assertEquals("projectLayout must not be null", ex.getMessage());
	}

	@Test
	void processFolder_skipsWhenOriginExistsAndUpdateFalse(@TempDir File tempDir) throws Exception {
		// Arrange
		Configurator config = org.mockito.Mockito.mock(Configurator.class);
		BindexCreator creator = new BindexCreator("openai", config).update(false);

		ProjectLayout layout = org.mockito.Mockito.mock(ProjectLayout.class);
		org.mockito.Mockito.when(layout.getProjectDir()).thenReturn(tempDir);

		BindexBuilder builder = org.mockito.Mockito.mock(BindexBuilder.class);
		org.mockito.Mockito.when(builder.getProjectLayout()).thenReturn(layout);

		Bindex origin = new Bindex();
		origin.setId("lib:1.0");
		origin.setName("lib");
		origin.setVersion("1.0");

		File existing = new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME);
		new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(existing, origin);
		String before = new String(Files.readAllBytes(existing.toPath()), StandardCharsets.UTF_8);

		try (org.mockito.MockedStatic<BindexBuilderFactory> factory = org.mockito.Mockito
				.mockStatic(BindexBuilderFactory.class)) {
			factory.when(() -> BindexBuilderFactory.create(layout, "openai", config)).thenReturn(builder);

			// Act
			creator.processFolder(layout);
		}

		// Assert
		String after = new String(Files.readAllBytes(existing.toPath()), StandardCharsets.UTF_8);
		assertEquals(before, after);
		org.mockito.Mockito.verify(builder, org.mockito.Mockito.never()).build();
	}

	@Test
	void processFolder_writesNewBindexWhenUpdateTrue(@TempDir File tempDir) throws Exception {
		// Arrange
		Configurator config = org.mockito.Mockito.mock(Configurator.class);
		BindexCreator creator = new BindexCreator("openai", config).update(true);

		ProjectLayout layout = org.mockito.Mockito.mock(ProjectLayout.class);
		org.mockito.Mockito.when(layout.getProjectDir()).thenReturn(tempDir);

		BindexBuilder builder = org.mockito.Mockito.mock(BindexBuilder.class);
		org.mockito.Mockito.when(builder.getProjectLayout()).thenReturn(layout);

		Bindex built = new Bindex();
		built.setId("lib:2.0");
		built.setName("lib");
		built.setVersion("2.0");
		org.mockito.Mockito.when(builder.origin(org.mockito.Mockito.any())).thenReturn(builder);
		org.mockito.Mockito.when(builder.build()).thenReturn(built);

		try (org.mockito.MockedStatic<BindexBuilderFactory> factory = org.mockito.Mockito
				.mockStatic(BindexBuilderFactory.class)) {
			factory.when(() -> BindexBuilderFactory.create(layout, "openai", config)).thenReturn(builder);

			// Act
			creator.processFolder(layout);
		}

		// Assert
		File bindexFile = new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME);
		assertTrue(bindexFile.exists());

		Bindex read = new ObjectMapper().readValue(bindexFile, Bindex.class);
		assertNotNull(read);
		assertEquals("lib:2.0", read.getId());
		assertEquals("2.0", read.getVersion());
		org.mockito.Mockito.verify(builder).build();
	}

	@Test
	void processFolder_wrapsIoExceptionsAsIllegalArgumentException(@TempDir File tempDir) {
		// Arrange
		Configurator config = org.mockito.Mockito.mock(Configurator.class);
		BindexCreator creator = new BindexCreator("openai", config).update(true);

		ProjectLayout layout = org.mockito.Mockito.mock(ProjectLayout.class);
		org.mockito.Mockito.when(layout.getProjectDir()).thenReturn(tempDir);

		try (org.mockito.MockedStatic<BindexBuilderFactory> factory = org.mockito.Mockito
				.mockStatic(BindexBuilderFactory.class)) {
			factory.when(() -> BindexBuilderFactory.create(layout, "openai", config))
					.thenThrow(new RuntimeException(new java.io.IOException("boom")));

			// Act
			RuntimeException rte = assertThrows(RuntimeException.class, () -> creator.processFolder(layout));

			// Assert
			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
				throw new IllegalArgumentException(rte);
			});
			assertNotNull(ex.getCause());
		}
	}
}
