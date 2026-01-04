package org.machanism.machai.project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.machanism.machai.project.layout.ProjectLayout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

class DummyProjectProcessor extends ProjectProcessor {
    public boolean folderProcessed = false;

    @Override
    public void processFolder(ProjectLayout processor) {
        folderProcessed = true;
    }
}

@DisplayName("ProjectProcessor Tests")
public class ProjectProcessorTest {
    private DummyProjectProcessor processor;
    private File tempProjectDir;
    private ProjectLayout mockLayout;

    @BeforeEach
    void setUp() {
        processor = new DummyProjectProcessor();
        tempProjectDir = mock(File.class);
        mockLayout = mock(ProjectLayout.class);
    }

    @Test
    @DisplayName("Scan Folder with Modules")
    void testScanFolderWithModules() throws IOException {
        List<String> modules = Arrays.asList("module1", "module2");
        when(mockLayout.getModules()).thenReturn(modules);
        ProjectProcessor spyProcessor = spy(processor);
        doReturn(mockLayout).when(spyProcessor).getProjectLayout(tempProjectDir);
        doNothing().when(spyProcessor).processModule(any(File.class), any(String.class));
        spyProcessor.scanFolder(tempProjectDir);
        verify(spyProcessor, times(2)).processModule(any(File.class), any(String.class));
        assertFalse(processor.folderProcessed);
    }

    @Test
    @DisplayName("Scan Folder without Modules")
    void testScanFolderWithoutModules() throws IOException {
        when(mockLayout.getModules()).thenReturn(null);
        ProjectProcessor spyProcessor = spy(processor);
        doReturn(mockLayout).when(spyProcessor).getProjectLayout(tempProjectDir);
        spyProcessor.scanFolder(tempProjectDir);
        assertTrue(processor.folderProcessed);
    }

    @Test
    @DisplayName("ProcessModule calls scanFolder on subdirectory")
    void testProcessModuleCallsScanFolder() throws IOException {
        ProjectProcessor spyProcessor = spy(processor);
        File projectDir = mock(File.class);
        String module = "moduleX";
        File subDir = mock(File.class);
        when(projectDir.getPath()).thenReturn("/tmp/test");
        doNothing().when(spyProcessor).scanFolder(subDir);
        spyProcessor.processModule(projectDir, module);
        verify(spyProcessor).scanFolder(subDir);
    }
}
