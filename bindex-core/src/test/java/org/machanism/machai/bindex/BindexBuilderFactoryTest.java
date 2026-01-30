package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

class BindexBuilderFactoryTest {

	@TempDir
	File tempDir;

	@Test
	void create_whenDefaultLayoutAndProjectDirExists_returnsDefaultBindexBuilder() throws Exception {
		// Arrange
		ProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir);

		// Act
		BindexBuilder builder = BindexBuilderFactory.create(layout);

		// Assert
		assertInstanceOf(BindexBuilder.class, builder);
		assertEquals(tempDir.getCanonicalFile(), builder.getProjectLayout().getProjectDir().getCanonicalFile());
	}

	@Test
	void create_whenProjectDirDoesNotExist_throwsFileNotFoundException() {
		// Arrange
		File missingDir = new File(tempDir, "missing");
		ProjectLayout layout = new DefaultProjectLayout().projectDir(missingDir);

		// Act + Assert
		FileNotFoundException ex = assertThrows(FileNotFoundException.class, () -> BindexBuilderFactory.create(layout));
		assertEquals(missingDir.getAbsolutePath(), ex.getMessage());
	}
}
