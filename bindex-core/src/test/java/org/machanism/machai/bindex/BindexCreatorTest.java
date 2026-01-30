package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

class BindexCreatorTest {

	@TempDir
	File tempDir;

	@Test
	void update_setsFlagAndReturnsSameInstance() {
		// Arrange
		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		BindexCreator creator = new BindexCreator(provider);

		// Act
		BindexCreator returned = creator.update(true);

		// Assert
		assertSame(creator, returned);
	}

	@Test
	void processFolder_whenUpdateFalseAndNoExistingBindex_builderStillBuildsDueToCondition() throws Exception {
		// Arrange
		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		BindexCreator creator = new BindexCreator(provider).update(false);
		ProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir);

		BindexBuilder builder = Mockito.mock(BindexBuilder.class);
		when(builder.getProjectLayout()).thenReturn(layout);
		when(builder.origin(any())).thenReturn(builder);
		when(builder.genAIProvider(any())).thenReturn(builder);
		when(builder.build()).thenReturn(null);

		try (MockedStatic<BindexBuilderFactory> mocked = Mockito.mockStatic(BindexBuilderFactory.class);
				MockedConstruction<ObjectMapper> mapperConstruction = mockConstruction(ObjectMapper.class)) {
			mocked.when(() -> BindexBuilderFactory.create(any(ProjectLayout.class))).thenReturn(builder);

			// Act
			creator.processFolder(layout);

			// Assert
			assertFalse(new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME).exists());
			assertEquals(0, mapperConstruction.constructed().size());
		}
	}

	@Test
	void processFolder_whenUpdateTrueAndBuilderReturnsNull_doesNotWriteFile() throws Exception {
		// Arrange
		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		BindexCreator creator = new BindexCreator(provider).update(true);
		ProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir);

		BindexBuilder builder = Mockito.mock(BindexBuilder.class);
		when(builder.getProjectLayout()).thenReturn(layout);
		when(builder.origin(any())).thenReturn(builder);
		when(builder.genAIProvider(any())).thenReturn(builder);
		when(builder.build()).thenReturn(null);

		try (MockedStatic<BindexBuilderFactory> mocked = Mockito.mockStatic(BindexBuilderFactory.class);
				MockedConstruction<ObjectMapper> mapperConstruction = mockConstruction(ObjectMapper.class)) {
			mocked.when(() -> BindexBuilderFactory.create(any(ProjectLayout.class))).thenReturn(builder);

			// Act
			creator.processFolder(layout);

			// Assert
			assertFalse(new File(tempDir, BindexProjectProcessor.BINDEX_FILE_NAME).exists());
			assertEquals(0, mapperConstruction.constructed().size());
		}
	}

	@Test
	void processFolder_whenUpdateTrueAndWriteFails_wrapsIOExceptionAsIllegalArgumentException() throws IOException {
		// Arrange
		GenAIProvider provider = Mockito.mock(GenAIProvider.class);
		BindexCreator creator = new BindexCreator(provider).update(true);
		ProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir);

		BindexBuilder builder = Mockito.mock(BindexBuilder.class);
		when(builder.getProjectLayout()).thenReturn(layout);
		when(builder.origin(any())).thenReturn(builder);
		when(builder.genAIProvider(any())).thenReturn(builder);
		Bindex bindex = new Bindex();
		bindex.setId("g:a:1");
		bindex.setName("a");
		bindex.setVersion("1");
		when(builder.build()).thenReturn(bindex);

		try (MockedStatic<BindexBuilderFactory> mocked = Mockito.mockStatic(BindexBuilderFactory.class);
				MockedConstruction<ObjectMapper> mapperConstruction = mockConstruction(ObjectMapper.class,
						(mapper, context) -> doThrow(new IOException("io")).when(mapper).writeValue(any(File.class), any()))) {
			mocked.when(() -> BindexBuilderFactory.create(any(ProjectLayout.class))).thenReturn(builder);

			// Act + Assert
			assertThrows(IllegalArgumentException.class, () -> creator.processFolder(layout));
			assertEquals(1, mapperConstruction.constructed().size());
		}
	}
}
