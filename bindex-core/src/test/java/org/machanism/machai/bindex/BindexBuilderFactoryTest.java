package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.bindex.builder.JScriptBindexBuilder;
import org.machanism.machai.bindex.builder.MavenBindexBuilder;
import org.machanism.machai.bindex.builder.PythonBindexBuilder;
import org.machanism.machai.project.layout.JScriptProjectLayout;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.project.layout.PythonProjectLayout;

/**
 * Unit tests for {@link BindexBuilderFactory}.
 * 
 * <p>Verifies builder creation logic for different {@link ProjectLayout} implementations and error conditions.</p>
 *
 * @author Viktor Tovstyi
 */
class BindexBuilderFactoryTest {
    /**
     * Verifies creation of {@link MavenBindexBuilder} for Maven layout.
     */
    @Test
    void createReturnsMavenBindexBuilderForMavenProjectLayout() throws Exception {
        MavenProjectLayout layout = mock(MavenProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(new File("."));
        BindexBuilder builder = BindexBuilderFactory.create(layout);
        assertTrue(builder instanceof MavenBindexBuilder);
    }

    /**
     * Verifies creation of {@link JScriptBindexBuilder} for JScript layout.
     */
    @Test
    void createReturnsJScriptBindexBuilderForJScriptProjectLayout() throws Exception {
        JScriptProjectLayout layout = mock(JScriptProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(new File("."));
        BindexBuilder builder = BindexBuilderFactory.create(layout);
        assertTrue(builder instanceof JScriptBindexBuilder);
    }

    /**
     * Verifies creation of {@link PythonBindexBuilder} for Python layout.
     */
    @Test
    void createReturnsPythonBindexBuilderForPythonProjectLayout() throws Exception {
        PythonProjectLayout layout = mock(PythonProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(new File("."));
        BindexBuilder builder = BindexBuilderFactory.create(layout);
        assertTrue(builder instanceof PythonBindexBuilder);
    }

    /**
     * Verifies creation of generic {@link BindexBuilder} for layouts with existing directory.
     */
    @Test
    void createReturnsGenericBindexBuilderWhenProjectDirExists() throws Exception {
        ProjectLayout layout = mock(ProjectLayout.class);
        File dir = new File(".");
        when(layout.getProjectDir()).thenReturn(dir);
        BindexBuilder builder = BindexBuilderFactory.create(layout);
        assertTrue(builder instanceof BindexBuilder);
    }

    /**
     * Verifies exception thrown if the project directory does not exist.
     */
    @Test
    void createThrowsFileNotFoundExceptionIfDirDoesNotExist() {
        ProjectLayout layout = mock(ProjectLayout.class);
        File file = mock(File.class);
        when(layout.getProjectDir()).thenReturn(file);
        when(file.exists()).thenReturn(false);
        assertThrows(FileNotFoundException.class, () -> {
            BindexBuilderFactory.create(layout);
        });
    }
}
