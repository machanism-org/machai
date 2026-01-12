package org.machanism.machai.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectProcessorTest {
    @TempDir
    File tempDir;

    private File projectDir;

    @BeforeEach
    void setup() {
        projectDir = tempDir;
    }

    static class TestProjectProcessor extends ProjectProcessor {
        boolean processFolderCalled = false;
        ProjectLayout processedLayout = null;

        @Override
        public void processFolder(ProjectLayout processor) {
            processFolderCalled = true;
            processedLayout = processor;
        }
    }

    @Test
    @Disabled
    void scanFolderProcessesModulesWhenPresent() throws IOException {
        // Arrange
        TestProjectProcessor processor = spy(new TestProjectProcessor());
        ProjectLayout mockLayout = mock(ProjectLayout.class);
        List<String> modules = Arrays.asList("mod1", "mod2");
        when(mockLayout.getModules()).thenReturn(modules);
        doReturn(mockLayout).when(processor).getProjectLayout(projectDir);

        // Act
        processor.scanFolder(projectDir);

        // Assert
        verify(processor, times(modules.size())).processModule(eq(projectDir), anyString());
        verify(processor, never()).processFolder(any());
    }

    @Test
    void scanFolderProcessesEntireFolderWhenNoModules() throws IOException {
        // Arrange
        TestProjectProcessor processor = spy(new TestProjectProcessor());
        ProjectLayout mockLayout = mock(ProjectLayout.class);
        when(mockLayout.getModules()).thenReturn(null);
        doReturn(mockLayout).when(processor).getProjectLayout(projectDir);

        // Act
        processor.scanFolder(projectDir);

        // Assert
        verify(processor, times(1)).processFolder(eq(mockLayout));
    }

    @Test
    void processModuleInvokesScanFolderOnSubdir() throws IOException {
        // Arrange
        TestProjectProcessor processor = spy(new TestProjectProcessor());
        String module = "modA";
        File subDir = new File(projectDir, module);
        assertTrue(subDir.mkdirs());

        // Act
        processor.processModule(projectDir, module);

        // Assert
        verify(processor, times(1)).scanFolder(subDir);
    }

    @Test
    void getProjectLayoutThrowsFileNotFoundIfAbsent() {
        // Arrange
        TestProjectProcessor processor = new TestProjectProcessor();
        File nonExistent = new File(tempDir, "notexist");

        // Assert
        assertThrows(Exception.class, () -> processor.getProjectLayout(nonExistent));
    }
}
