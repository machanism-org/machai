package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.bindex.builder.JScriptBindexBuilder;
import org.machanism.machai.bindex.builder.MavenBindexBuilder;
import org.machanism.machai.bindex.builder.PythonBindexBuilder;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

class BindexBuilderFactoryTest {

	@TempDir
	File tempDir;

	@Test
	void create_whenMavenProjectLayout_returnsMavenBindexBuilder() throws Exception {
		// Arrange
		ProjectLayout layout = new MavenProjectLayout() {
			@Override
			public File getProjectDir() {
				return tempDir;
			}
		};

		// Act
		BindexBuilder builder = BindexBuilderFactory.create(layout);

		// Assert
		assertInstanceOf(MavenBindexBuilder.class, builder);
	}

	@Test
	void create_whenJScriptProjectLayout_returnsJScriptBindexBuilder() throws Exception {
		// Arrange
		ProjectLayout layout = new JScriptProjectLayout() {
			@Override
			public File getProjectDir() {
				return tempDir;
			}
		};

		// Act
		BindexBuilder builder = BindexBuilderFactory.create(layout);

		// Assert
		assertInstanceOf(JScriptBindexBuilder.class, builder);
	}

	@Test
	void create_whenPythonProjectLayout_returnsPythonBindexBuilder() throws Exception {
		// Arrange
		ProjectLayout layout = new PythonProjectLayout() {
			@Override
			public File getProjectDir() {
				return tempDir;
			}
		};

		// Act
		BindexBuilder builder = BindexBuilderFactory.create(layout);

		// Assert
		assertInstanceOf(PythonBindexBuilder.class, builder);
	}

	@Test
	void create_whenGenericProjectLayoutAndDirExists_returnsBaseBindexBuilder() throws Exception {
		// Arrange
		ProjectLayout layout = TestLayouts.projectLayout(tempDir);

		// Act
		BindexBuilder builder = BindexBuilderFactory.create(layout);

		// Assert
		assertInstanceOf(BindexBuilder.class, builder);
	}

	@Test
	void create_whenDirDoesNotExist_throwsFileNotFoundException() {
		// Arrange
		File missing = new File(tempDir, "missing-dir");
		ProjectLayout layout = TestLayouts.projectLayout(missing);

		// Act / Assert
		assertThrows(FileNotFoundException.class, () -> BindexBuilderFactory.create(layout));
	}
}
