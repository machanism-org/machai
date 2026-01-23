package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.bindex.builder.JScriptBindexBuilder;
import org.machanism.machai.bindex.builder.MavenBindexBuilder;
import org.machanism.machai.bindex.builder.PythonBindexBuilder;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

class BindexBuilderFactoryTest {

    @Test
    void create_returnsMavenBindexBuilderForMavenLayout() throws Exception {
        // Arrange
        MavenProjectLayout layout = new MavenProjectLayout();
        File dir = Files.createTempDirectory("maven-layout").toFile();
        layout.projectDir(dir);

        // Act
        BindexBuilder builder = BindexBuilderFactory.create(layout);

        // Assert
        assertInstanceOf(MavenBindexBuilder.class, builder);
        assertEquals(dir.getAbsolutePath(), builder.getProjectLayout().getProjectDir().getAbsolutePath());
    }

    @Test
    void create_returnsJScriptBindexBuilderForJScriptLayout() throws Exception {
        // Arrange
        JScriptProjectLayout layout = new JScriptProjectLayout();
        File dir = Files.createTempDirectory("js-layout").toFile();
        layout.projectDir(dir);

        // Act
        BindexBuilder builder = BindexBuilderFactory.create(layout);

        // Assert
        assertInstanceOf(JScriptBindexBuilder.class, builder);
    }

    @Test
    void create_returnsPythonBindexBuilderForPythonLayout() throws Exception {
        // Arrange
        PythonProjectLayout layout = new PythonProjectLayout();
        File dir = Files.createTempDirectory("py-layout").toFile();
        layout.projectDir(dir);

        // Act
        BindexBuilder builder = BindexBuilderFactory.create(layout);

        // Assert
        assertInstanceOf(PythonBindexBuilder.class, builder);
    }

    @Test
    void create_returnsGenericBindexBuilderWhenDirExistsAndLayoutIsGeneric() throws Exception {
        // Arrange
        ProjectLayout layout = new ProjectLayout() {
            private final File dir = Files.createTempDirectory("generic-layout").toFile();

            @Override
            public File getProjectDir() {
                return dir;
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
        };

        // Act
        BindexBuilder builder = BindexBuilderFactory.create(layout);

        // Assert
        assertInstanceOf(BindexBuilder.class, builder);
        assertNull(builder.getOrigin());
    }

    @Test
    void create_throwsFileNotFoundExceptionWhenDirDoesNotExist() {
        // Arrange
        File notExists = new File("build/tmp/does-not-exist-" + System.nanoTime());
        ProjectLayout layout = new ProjectLayout() {
            @Override
            public File getProjectDir() {
                return notExists;
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
        };

        // Act + Assert
        FileNotFoundException ex = assertThrows(FileNotFoundException.class, () -> BindexBuilderFactory.create(layout));
        assertEquals(notExists.getAbsolutePath(), ex.getMessage());
    }
}
